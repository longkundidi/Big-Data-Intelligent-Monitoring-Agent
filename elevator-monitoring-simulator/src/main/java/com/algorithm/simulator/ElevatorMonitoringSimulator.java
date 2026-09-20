package com.algorithm.simulator;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class ElevatorMonitoringSimulator {

    private ElevatorMonitoringSimulator() {
    }

    public static void main(String[] args) throws Exception {
        configureUtf8Console();
        Arguments arguments = Arguments.parse(args);
        if (arguments.help) {
            printHelp();
            return;
        }

        Config config = Config.load(arguments);
        LabeledSampleSource source = new LabeledSampleSource(config.randomSeed);
        printConfiguration(config, source);
        if (arguments.previewCount > 0) {
            previewSequence(source, arguments.previewCount);
            return;
        }

        InfluxBatchWriter influxWriter = new InfluxBatchWriter(config);
        KafkaBatchWriter kafkaWriter = new KafkaBatchWriter(config);
        AtomicLong batchSequence = new AtomicLong();
        AtomicReference<PendingBatch> pendingBatch = new AtomicReference<>();
        Runnable send = () -> {
            PendingBatch batch = pendingBatch.get();
            if (batch == null) {
                batch = new PendingBatch(batchSequence.incrementAndGet(), System.currentTimeMillis(),
                        source.nextWindow(), !config.kafkaEnabled);
                pendingBatch.set(batch);
            }
            try {
                if (!batch.influxWritten) {
                    batch.influxPayloadBytes = influxWriter.write(batch.window.getValues(), batch.timestampMillis);
                    batch.influxWritten = true;
                }
                if (!batch.kafkaWritten) {
                    KafkaWriteResult kafkaResult = kafkaWriter.write(batch.window.getValues(), batch.timestampMillis);
                    batch.kafkaPayloadBytes = kafkaResult.payloadBytes;
                    batch.kafkaPartition = kafkaResult.partition;
                    batch.kafkaOffset = kafkaResult.offset;
                    batch.kafkaWritten = true;
                }
                logSuccessfulWrite(config, batch);
                pendingBatch.compareAndSet(batch, null);
            }
            catch (Exception exception) {
                System.err.printf(Locale.ROOT,
                        "%s batch write failed; retained for retry: batch=%d timestamp=%d influxWritten=%s "
                                + "kafkaWritten=%s sequence=%d label=%d class=%s sample=%d error=%s%n",
                        Instant.now(), batch.batchNumber, batch.timestampMillis, batch.influxWritten,
                        batch.kafkaWritten, batch.window.getSequenceNumber(), batch.window.getLabel(),
                        batch.window.getClassCode(), batch.window.getSampleIndex(), exception.getMessage());
                if (arguments.once) {
                    throw new BatchWriteException(exception);
                }
            }
        };

        if (arguments.once) {
            try {
                send.run();
            }
            catch (BatchWriteException exception) {
                throw new IllegalStateException("One-time batch write failed", exception.getCause());
            }
            finally {
                kafkaWriter.close();
            }
            return;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "elevator-monitoring-simulator");
            thread.setDaemon(false);
            return thread;
        });
        CountDownLatch stopped = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
            finally {
                kafkaWriter.close();
            }
            stopped.countDown();
        }, "elevator-monitoring-simulator-shutdown"));

        executor.scheduleWithFixedDelay(send, 0, config.intervalSeconds, TimeUnit.SECONDS);
        stopped.await();
    }

    private static void configureUtf8Console() throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8.name()));
    }

    private static void previewSequence(LabeledSampleSource source, int count) {
        int[] labels = new int[8];
        for (int index = 0; index < count; index++) {
            SampleWindow window = source.nextWindow();
            labels[window.getLabel()]++;
            System.out.printf(Locale.ROOT,
                    "preview sequence=%d position=%d/31 label=%d class=%s className=%s sample=%d expectedAnomaly=%d%n",
                    window.getSequenceNumber(), window.getSequencePosition(), window.getLabel(),
                    window.getClassCode(), window.getClassName(), window.getSampleIndex(),
                    window.isFault() ? 1 : 0);
        }
        StringBuilder summary = new StringBuilder("preview summary");
        for (int label = 0; label < labels.length; label++) {
            summary.append(' ').append(label).append('=').append(labels[label]);
        }
        System.out.println(summary);
    }

    private static void logSuccessfulWrite(Config config, PendingBatch batch) {
        SampleWindow window = batch.window;
        System.out.printf(Locale.ROOT,
                "%s batch=%d timestamp=%d sequence=%d position=%d/31 point=%s values=%d label=%d class=%s "
                        + "className=%s sample=%d expectedAnomaly=%d influxBytes=%d kafkaTopic=%s "
                        + "kafkaBytes=%d kafkaPartition=%d kafkaOffset=%d mode=%s%n",
                Instant.ofEpochMilli(batch.timestampMillis), batch.batchNumber, batch.timestampMillis,
                window.getSequenceNumber(),
                window.getSequencePosition(), config.monitorPointId, window.getValues().length, window.getLabel(),
                window.getClassCode(), window.getClassName(), window.getSampleIndex(), window.isFault() ? 1 : 0,
                batch.influxPayloadBytes, config.kafkaEnabled ? config.kafkaTopic : "disabled",
                batch.kafkaPayloadBytes, batch.kafkaPartition, batch.kafkaOffset,
                config.dryRun ? "dry-run" : "written");
    }

    private static void printConfiguration(Config config, LabeledSampleSource source) {
        System.out.println("Elevator monitoring simulator started");
        System.out.println("  influx=" + config.influxUrl + " bucket=" + config.bucket + " org=" + config.org);
        System.out.println("  kafka=" + (config.kafkaEnabled
                ? config.kafkaServers + " topic=" + config.kafkaTopic + " id=" + config.kafkaDcId : "disabled"));
        System.out.println("  measurement=" + config.measurement + " field=" + config.field);
        System.out.println("  point=" + config.scene + "/" + config.productModel + "/"
                + config.elevatorInstance + "/" + config.gbomLevel2 + "/" + config.gbomLevel3);
        System.out.println("  monitorPointId=" + config.monitorPointId);
        System.out.println("  intervalSeconds=" + config.intervalSeconds + " windowSize="
                + LabeledSampleSource.WINDOW_SIZE + " sampleRateHz=45");
        System.out.println("  sequence=30 normal + 1 fault; fault classes shuffled 1..7");
        System.out.println("  source=" + source.description() + " randomSeed=" + config.randomSeed
                + " dryRun=" + config.dryRun);
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar elevator-monitoring-simulator.jar [--once] [--dry-run] [--preview=N]");
        System.out.println("  --once       write one sample to InfluxDB and Kafka, then exit");
        System.out.println("  --dry-run    generate and validate without writing InfluxDB or Kafka");
        System.out.println("  --preview=N  print N sample selections immediately without writing InfluxDB");
        System.out.println("  --dc-id=N    use id N and Kafka topic dc_source_N");
        System.out.println("Configuration is read from environment variables or -D system properties.");
        System.out.println("See README.md for the complete configuration list.");
    }

    private static final class InfluxBatchWriter {

        private final Config config;

        private InfluxBatchWriter(Config config) {
            this.config = config;
        }

        private int write(double[] values, long timestampMillis) throws IOException {
            validateValues(values);
            String payload = buildLineProtocol(values, timestampMillis);
            byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
            if (!config.dryRun) {
                post(payloadBytes);
            }
            return payloadBytes.length;
        }

        private String buildLineProtocol(double[] values, long timestampMillis) {
            StringBuilder line = new StringBuilder(24 * 1024);
            line.append(escapeMeasurement(config.measurement));
            appendTag(line, "monitor_point_id", config.monitorPointId);
            appendTag(line, "elevator_instance", config.elevatorInstance);
            appendTag(line, "gbom_level_2", config.gbomLevel2);
            appendTag(line, "gbom_level_3", config.gbomLevel3);
            line.append(' ').append(escapeFieldKey(config.field)).append("=\"");
            line.append('[').append(formatValues(values)).append("]\"").append(' ').append(timestampMillis);
            return line.toString();
        }

        private void post(byte[] payload) throws IOException {
            String writeUrl = config.influxUrl + "/api/v2/write?org=" + encode(config.org)
                    + "&bucket=" + encode(config.bucket) + "&precision=ms";
            HttpURLConnection connection = (HttpURLConnection) URI.create(writeUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.connectTimeoutMillis);
            connection.setReadTimeout(config.readTimeoutMillis);
            connection.setRequestProperty("Authorization", "Token " + config.token);
            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(payload.length);
            try {
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(payload);
                }
                int status = connection.getResponseCode();
                if (status < 200 || status >= 300) {
                    String response = readResponse(connection.getErrorStream());
                    throw new IOException("InfluxDB write returned HTTP " + status
                            + (response.isEmpty() ? "" : ": " + response));
                }
            }
            finally {
                connection.disconnect();
            }
        }

        private static void appendTag(StringBuilder line, String key, String value) {
            line.append(',').append(escapeTag(key)).append('=').append(escapeTag(value));
        }

        private static String escapeMeasurement(String value) {
            return value.replace("\\", "\\\\").replace(",", "\\,").replace(" ", "\\ ");
        }

        private static String escapeTag(String value) {
            return value.replace("\\", "\\\\").replace(",", "\\,")
                    .replace(" ", "\\ ").replace("=", "\\=");
        }

        private static String escapeFieldKey(String value) {
            return escapeTag(value);
        }

        private static String encode(String value) throws IOException {
            return URLEncoder.encode(value, "UTF-8");
        }

        private static String readResponse(InputStream input) throws IOException {
            if (input == null) {
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }

    }

    private static final class KafkaBatchWriter implements AutoCloseable {

        private final Config config;

        private final KafkaProducer<String, String> producer;

        private KafkaBatchWriter(Config config) {
            this.config = config;
            if (!config.kafkaEnabled || config.dryRun) {
                this.producer = null;
                return;
            }
            Properties properties = new Properties();
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaServers);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.ACKS_CONFIG, "all");
            properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "30000");
            properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
            this.producer = new KafkaProducer<>(properties);
        }

        private KafkaWriteResult write(double[] values, long timestampMillis) throws Exception {
            validateValues(values);
            String payload = "{\"dc_data\":\"" + formatValues(values) + "\",\"dc_time\":"
                    + timestampMillis + ",\"id\":" + config.kafkaDcId + "}";
            int payloadBytes = payload.getBytes(StandardCharsets.UTF_8).length;
            if (config.dryRun) {
                return new KafkaWriteResult(payloadBytes, -1, -1);
            }
            RecordMetadata metadata = producer.send(
                    new ProducerRecord<>(config.kafkaTopic, null, timestampMillis, null, payload))
                .get(30, TimeUnit.SECONDS);
            return new KafkaWriteResult(payloadBytes, metadata.partition(), metadata.offset());
        }

        @Override
        public void close() {
            if (producer != null) {
                producer.flush();
                producer.close();
            }
        }

    }

    private static void validateValues(double[] values) {
        if (values.length != LabeledSampleSource.WINDOW_SIZE) {
            throw new IllegalArgumentException("Batch must contain exactly 1024 values");
        }
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Batch contains NaN or infinity");
            }
        }
    }

    private static String formatValues(double[] values) {
        StringBuilder result = new StringBuilder(values.length * 12);
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                result.append(',');
            }
            result.append(String.format(Locale.ROOT, "%.8f", values[index]));
        }
        return result.toString();
    }

    private static final class Config {

        private static final String DEFAULT_INFLUX_TOKEN =
                "b-6wkycz9XMJJG_Ad49aYUk_KicySSGVSbSaa8RYp6EyObz961qKw0zH4Bp8D6MMT34vzm2a6JJ-PKg7uBOQUA==";

        private final String influxUrl;

        private final String org;

        private final String bucket;

        private final String token;

        private final boolean kafkaEnabled;

        private final String kafkaServers;

        private final String kafkaTopic;

        private final int kafkaDcId;

        private final String measurement;

        private final String field;

        private final String monitorPointId;

        private final String scene;

        private final String productModel;

        private final String elevatorInstance;

        private final String gbomLevel2;

        private final String gbomLevel3;

        private final long intervalSeconds;

        private final long randomSeed;

        private final int connectTimeoutMillis;

        private final int readTimeoutMillis;

        private final boolean dryRun;

        private Config(String influxUrl, String org, String bucket, String token, boolean kafkaEnabled,
                String kafkaServers, String kafkaTopic, int kafkaDcId, String measurement,
                String field, String monitorPointId, String scene, String productModel, String elevatorInstance,
                String gbomLevel2, String gbomLevel3, long intervalSeconds, long randomSeed,
                int connectTimeoutMillis, int readTimeoutMillis, boolean dryRun) {
            this.influxUrl = influxUrl;
            this.org = org;
            this.bucket = bucket;
            this.token = token;
            this.kafkaEnabled = kafkaEnabled;
            this.kafkaServers = kafkaServers;
            this.kafkaTopic = kafkaTopic;
            this.kafkaDcId = kafkaDcId;
            this.measurement = measurement;
            this.field = field;
            this.monitorPointId = monitorPointId;
            this.scene = scene;
            this.productModel = productModel;
            this.elevatorInstance = elevatorInstance;
            this.gbomLevel2 = gbomLevel2;
            this.gbomLevel3 = gbomLevel3;
            this.intervalSeconds = intervalSeconds;
            this.randomSeed = randomSeed;
            this.connectTimeoutMillis = connectTimeoutMillis;
            this.readTimeoutMillis = readTimeoutMillis;
            this.dryRun = dryRun;
        }

        private static Config load(Arguments arguments) {
            String influxUrl = setting("simulator.influx.url", "INFLUX_URL", "http://192.168.16.219:8086")
                    .replaceAll("/+$", "");
            boolean dryRun = arguments.dryRun || arguments.previewCount > 0;
            String token = setting("simulator.influx.token", "INFLUX_TOKEN", DEFAULT_INFLUX_TOKEN);
            if (isMissingOrPlaceholderToken(token)) {
                token = DEFAULT_INFLUX_TOKEN;
            }
            if (!dryRun && isMissingOrPlaceholderToken(token)) {
                throw new IllegalArgumentException(
                        "INFLUX_TOKEN must contain the actual InfluxDB token, not '<InfluxDB Token>'");
            }
            long intervalSeconds = positiveLong("SIMULATOR_INTERVAL_SECONDS",
                    setting("simulator.interval.seconds", "SIMULATOR_INTERVAL_SECONDS", "30"));
            int kafkaDcId = arguments.kafkaDcId == null
                    ? positiveInt("KAFKA_DC_ID", setting("simulator.kafka.dc.id", "KAFKA_DC_ID", "218"))
                    : arguments.kafkaDcId;
            return new Config(influxUrl,
                    setting("simulator.influx.org", "INFLUX_ORG", "bigdata"),
                    setting("simulator.influx.bucket", "INFLUX_BUCKET", "elevate"), token,
                    Boolean.parseBoolean(setting("simulator.kafka.enabled", "SIMULATOR_KAFKA_ENABLED", "true")),
                    setting("simulator.kafka.servers", "KAFKA_SERVERS", "192.168.16.219:9092"),
                    "dc_source_" + kafkaDcId, kafkaDcId,
                    setting("simulator.measurement", "SIMULATOR_MEASUREMENT", "智慧家园小区"),
                    setting("simulator.field", "SIMULATOR_FIELD", "奥克斯1"),
                    setting("simulator.monitor.point.id", "SIMULATOR_MONITOR_POINT_ID",
                            "smart-home-aux1-a1-traction-point1"),
                    setting("simulator.scene", "SIMULATOR_SCENE", "智慧家园小区"),
                    setting("simulator.product.model", "SIMULATOR_PRODUCT_MODEL", "奥克斯1"),
                    setting("simulator.elevator.instance", "SIMULATOR_ELEVATOR_INSTANCE", "电梯-A1"),
                    setting("simulator.gbom.level2", "SIMULATOR_GBOM_LEVEL_2", "曳引机"),
                    setting("simulator.gbom.level3", "SIMULATOR_GBOM_LEVEL_3", "测点1"),
                    intervalSeconds,
                    Long.parseLong(setting("simulator.random.seed", "SIMULATOR_RANDOM_SEED", "20260720")),
                    positiveInt("SIMULATOR_CONNECT_TIMEOUT_MS",
                            setting("simulator.connect.timeout.ms", "SIMULATOR_CONNECT_TIMEOUT_MS", "5000")),
                    positiveInt("SIMULATOR_READ_TIMEOUT_MS",
                            setting("simulator.read.timeout.ms", "SIMULATOR_READ_TIMEOUT_MS", "10000")),
                    dryRun);
        }

        private static boolean isMissingOrPlaceholderToken(String token) {
            String trimmed = token.trim();
            return trimmed.isEmpty() || (trimmed.startsWith("<") && trimmed.endsWith(">"));
        }

        private static String setting(String propertyName, String environmentName, String defaultValue) {
            String property = System.getProperty(propertyName);
            if (property != null) {
                return property;
            }
            String environment = System.getenv(environmentName);
            return environment == null ? defaultValue : environment;
        }

        private static long positiveLong(String name, String value) {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(name + " must be greater than zero");
            }
            return parsed;
        }

        private static int positiveInt(String name, String value) {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(name + " must be greater than zero");
            }
            return parsed;
        }

    }

    private static final class Arguments {

        private final boolean once;

        private final boolean dryRun;

        private final boolean help;

        private final int previewCount;

        private final Integer kafkaDcId;

        private Arguments(boolean once, boolean dryRun, boolean help, int previewCount, Integer kafkaDcId) {
            this.once = once;
            this.dryRun = dryRun;
            this.help = help;
            this.previewCount = previewCount;
            this.kafkaDcId = kafkaDcId;
        }

        private static Arguments parse(String[] args) {
            boolean once = false;
            boolean dryRun = false;
            boolean help = false;
            int previewCount = 0;
            Integer kafkaDcId = null;
            for (String argument : args) {
                if (argument.startsWith("--preview=")) {
                    previewCount = Integer.parseInt(argument.substring("--preview=".length()));
                    if (previewCount <= 0) {
                        throw new IllegalArgumentException("--preview must be greater than zero");
                    }
                    continue;
                }
                if (argument.startsWith("--dc-id=")) {
                    kafkaDcId = Integer.parseInt(argument.substring("--dc-id=".length()));
                    if (kafkaDcId <= 0) {
                        throw new IllegalArgumentException("--dc-id must be greater than zero");
                    }
                    continue;
                }
                switch (argument) {
                    case "--once":
                        once = true;
                        break;
                    case "--dry-run":
                        dryRun = true;
                        break;
                    case "--help":
                    case "-h":
                        help = true;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown argument: " + argument);
                }
            }
            return new Arguments(once, dryRun, help, previewCount, kafkaDcId);
        }

    }

    private static final class PendingBatch {

        private final long batchNumber;

        private final long timestampMillis;

        private final SampleWindow window;

        private boolean influxWritten;

        private boolean kafkaWritten;

        private int influxPayloadBytes;

        private int kafkaPayloadBytes;

        private int kafkaPartition = -1;

        private long kafkaOffset = -1;

        private PendingBatch(long batchNumber, long timestampMillis, SampleWindow window, boolean kafkaWritten) {
            this.batchNumber = batchNumber;
            this.timestampMillis = timestampMillis;
            this.window = window;
            this.kafkaWritten = kafkaWritten;
        }

    }

    private static final class KafkaWriteResult {

        private final int payloadBytes;

        private final int partition;

        private final long offset;

        private KafkaWriteResult(int payloadBytes, int partition, long offset) {
            this.payloadBytes = payloadBytes;
            this.partition = partition;
            this.offset = offset;
        }

    }

    private static final class BatchWriteException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private BatchWriteException(Throwable cause) {
            super(cause);
        }

    }

}
