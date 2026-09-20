package com.job.utils;

import com.algorithm.common.model.DcDTO;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.algorithm.common.constant.InfluxDBPropertiesConstants.*;
import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.PropertiesConstants.*;
import static com.job.utils.ExecutionEnvUtil.PARAMETER_TOOL;

public class Initialization {

    public static void initProperties() {
        jobName = PARAMETER_TOOL.get(jobName);
        kafkaBrokers = PARAMETER_TOOL.get(kafkaBrokers);
        kafkaGroupId = PARAMETER_TOOL.get(kafkaGroupId);
        kafkaTopics = PARAMETER_TOOL.get(kafkaTopics);
        kafkaSinkBrokers = PARAMETER_TOOL.get(kafkaSinkBrokers, kafkaBrokers);
        influxDBUrl = PARAMETER_TOOL.get(influxDBUrl);
        influxDBToken = PARAMETER_TOOL.get(influxDBToken);
        influxDBBucket = PARAMETER_TOOL.get(influxDBBucket);
        influxDBOrganization = PARAMETER_TOOL.get(influxDBOrganization);
        System.out.printf("[dc-guard] Effective Influx config -> url=%s, bucket=%s, organization=%s, token=%s%n",
                influxDBUrl,
                influxDBBucket,
                influxDBOrganization,
                maskToken(influxDBToken));
    }

    private static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "<empty>";
        }
        if (token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    public static DataStream<DcDTO> initDataStream(StreamExecutionEnvironment env) {
        initProperties();

        KafkaSource<DcDTO> source = KafkaSource.<DcDTO>builder()
                .setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(kafkaBrokers)
                .setTopicPattern(java.util.regex.Pattern.compile(kafkaTopics))
                .setGroupId("dc_guard_group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new DcJsonDeserializationSchema())
                .build();

        return env.fromSource(source, WatermarkStrategy.noWatermarks(), jobName);
    }

    public static class DcJsonDeserializationSchema implements DeserializationSchema<DcDTO> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        @Override
        public DcDTO deserialize(byte[] message) throws IOException {
            try {
                return objectMapper.readValue(message, DcDTO.class);
            } catch (Exception e) {
                //反序列化失败，构建错误信息的DcDTO对象并返回
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
                String originalMessage = new String(message, StandardCharsets.UTF_8);
                String currentTime = formatter.format(Instant.now());
                return DcDTO.builder()
                        .dc_data(String.format("{\"error\":\"form error\",\"information\":\"%s\",\"time\":\"%s\"}", originalMessage, currentTime))
                        .build();
            }
        }
        @Override
        public boolean isEndOfStream(DcDTO nextElement) {
            return false;
        }
        @Override
        public TypeInformation<DcDTO> getProducedType() {
            return TypeInformation.of(DcDTO.class);
        }
    }

    public static InfluxDBClient getClient() {
        // 历史硬编码（保留为注释，便于回溯）：
        // String token = "b-6wkycz9XMJJG_Ad49aYUk_KicySSGVSbSaa8RYp6EyObz961qKw0zH4Bp8D6MMT34vzm2a6JJ-PKg7uBOQUA==";
        // return InfluxDBClientFactory.create("http://192.168.230.116:8086", token.toCharArray());
        return InfluxDBClientFactory.create(influxDBUrl, influxDBToken.toCharArray());
    }
}
