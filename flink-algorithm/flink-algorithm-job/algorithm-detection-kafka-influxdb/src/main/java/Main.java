import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.job.dto.KafkaMessage;
import com.job.dto.Property;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.util.Collector;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. 初始化 Flink 环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. 定义 Kafka Source
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.16.219:9092")
                .setTopics("dc_source_158")
                .setGroupId("bigdata-mes-group")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Step1: 解析 JSON
        DataStream<KafkaMessage> kafkaData = env
                .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
                .map(json -> {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(json, KafkaMessage.class);
                });

        //Step2: 扁平化 property
        DataStream<Property> flattened = kafkaData.flatMap((KafkaMessage message, Collector<Property> out) -> {
            for (Property p : message.property) {
                // 绑定时间戳，方便后续处理
                p.id = p.id + "|" + message.time;
                out.collect(p);
            }
        }).returns(Property.class);

        // Step3: 写入 InfluxDB
        flattened.addSink(new InfluxDBSink());

        env.execute("Kafka to InfluxDB Job");
    }

    // InfluxDB Sink
    public static class InfluxDBSink extends RichSinkFunction<Property> {
        private transient InfluxDBClient client;
        private transient WriteApiBlocking writeApi;

        @Override
        public void open(Configuration parameters) {
            String url = "http://192.168.16.219:8086";
            String token = "b-6wkycz9XMJJG_Ad49aYUk_KicySSGVSbSaa8RYp6EyObz961qKw0zH4Bp8D6MMT34vzm2a6JJ-PKg7uBOQUA==";  // 替换为你的实际 token
            String org = "bigdata";
            String bucket = "bigdata";

            client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            writeApi = client.getWriteApiBlocking();
        }

        @Override
        public void invoke(Property property, Context context) {
            // 从 id 中分离出 timestamp
            String[] parts = property.id.split("\\|");
            String deviceId = parts[0];
            long timestamp = Long.parseLong(parts[1]);

            Point point = Point.measurement("dc_measurements")
                    .addTag("device", deviceId)          // 设备ID作为tag
                    .addTag("key", property.key)         // 测点名称作为tag
                    .addField("value", Double.parseDouble(property.value)) // 测点值
                    .time(timestamp, WritePrecision.MS); // Kafka中的time字段

            writeApi.writePoint(point);
        }

        @Override
        public void close() {
            if (client != null) {
                client.close();
            }
        }
    }

}