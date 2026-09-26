package com.streamdoctor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Collectors;

public final class RegtcnPipelineJob {
    private static final String JOB_NAME = "algorithm_REGTCN";

    private RegtcnPipelineJob() {}

    public static void main(String[] args) throws Exception {
        String brokers = env("KAFKA_SERVERS", "kafka:9092");
        String inputTopic = env("INPUT_TOPIC", "dc_algorithm_REGTCN");
        String outputTopic = env("OUTPUT_TOPIC", "dc_algorithm_sink_REGTCN");
        String groupId = env("CONSUMER_GROUP", "REGTCN_kafka_group");

        StreamExecutionEnvironment execution = StreamExecutionEnvironment.getExecutionEnvironment();
        execution.setParallelism(1);
        execution.setRestartStrategy(RestartStrategies.fixedDelayRestart(5, 5_000L));
        execution.enableCheckpointing(30_000);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(brokers)
                .setTopics(inputTopic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(outputTopic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        DataStream<String> results = execution
                .fromSource(source, WatermarkStrategy.noWatermarks(), inputTopic)
                .name("REGTCN Kafka input")
                .map(new ModelCall())
                .name("REGTCN model inference");
        results.sinkTo(sink).name("REGTCN Kafka output");
        execution.execute(JOB_NAME);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    public static final class ModelCall extends RichMapFunction<String, String> {
        private transient ObjectMapper mapper;
        private String modelUrl;

        @Override
        public void open(Configuration parameters) {
            mapper = new ObjectMapper();
            modelUrl = env("MODEL_URL", "http://regtcn-model:8000/createTask/");
        }

        @Override
        public String map(String raw) throws Exception {
            ObjectNode input = (ObjectNode) mapper.readTree(raw);
            long timestamp = requiredLong(input, "dc_time");
            int taskId = Math.toIntExact(requiredLong(input, "id"));
            ArrayNode values = parseValues(input.get("dc_data"));
            if (values.size() != 1024) {
                throw new IllegalArgumentException("dc_data must contain 1024 samples, got " + values.size());
            }

            Instant end = Instant.ofEpochMilli(timestamp);
            ObjectNode taskMessage = mapper.createObjectNode();
            taskMessage.put("monitorPointId", "smart-home-aux1-a1-traction-point1");
            taskMessage.put("startTime", end.minusSeconds(30).toString());
            taskMessage.put("endTime", end.toString());
            taskMessage.put("sampleCount", values.size());
            taskMessage.set("values", values);

            ObjectNode task = mapper.createObjectNode();
            task.put("taskId", taskId);
            task.put("taskMsg", mapper.writeValueAsString(taskMessage));
            task.put("taskState", 1);
            task.put("taskReUrl", "");

            JsonNode response = postJson(task);
            if (response.path("taskState").asInt() != 2) {
                throw new IOException("model rejected task: " + response.path("taskResult").asText());
            }
            JsonNode result = mapper.readTree(response.path("taskResult").asText());
            if (!result.has("is_anomaly") || !result.has("anomaly_score") || !result.has("threshold")) {
                throw new IOException("model returned an invalid taskResult");
            }

            ObjectNode diagnosis = mapper.createObjectNode();
            diagnosis.put("rms_hi", result.path("anomaly_score").asDouble());
            diagnosis.put("threshold", result.path("threshold").asDouble());
            diagnosis.put("anomaly_flag", result.path("is_anomaly").asBoolean() ? 1 : 0);
            input.set("dc_data", diagnosis);
            input.put("anomaly_flag", diagnosis.path("anomaly_flag").asInt());
            return mapper.writeValueAsString(input);
        }

        private ArrayNode parseValues(JsonNode raw) throws IOException {
            if (raw == null) {
                throw new IllegalArgumentException("dc_data is required");
            }
            if (raw.isArray()) {
                return (ArrayNode) raw;
            }
            if (!raw.isTextual()) {
                throw new IllegalArgumentException("dc_data must be a comma-separated string or array");
            }
            ArrayNode values = mapper.createArrayNode();
            for (String item : raw.asText().split(",")) {
                if (!item.trim().isEmpty()) {
                    values.add(Double.parseDouble(item.trim()));
                }
            }
            return values;
        }

        private long requiredLong(ObjectNode input, String field) {
            if (!input.hasNonNull(field)) {
                throw new IllegalArgumentException(field + " is required");
            }
            return input.path(field).asLong();
        }

        private JsonNode postJson(ObjectNode payload) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(modelUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(30_000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(mapper.writeValueAsBytes(payload));
            }
            int status = connection.getResponseCode();
            InputStream body = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String text;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                text = reader.lines().collect(Collectors.joining("\n"));
            } finally {
                connection.disconnect();
            }
            if (status < 200 || status >= 300) {
                throw new IOException("model HTTP " + status + ": " + text);
            }
            return mapper.readTree(text);
        }
    }
}
