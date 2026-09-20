package com.job.utils;

import com.algorithm.common.model.TaskDTO;
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
    }

    public static DataStream<TaskDTO> initDataStream(StreamExecutionEnvironment env) {
        initProperties();

        KafkaSource<TaskDTO> source = KafkaSource.<TaskDTO>builder()
                .setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(kafkaBrokers)
                .setTopicPattern(java.util.regex.Pattern.compile(kafkaTopics))
                .setGroupId("task_config_group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new TaskConfigJsonDeserializationSchema())
                .build();

        return env.fromSource(source, WatermarkStrategy.noWatermarks(), jobName);
    }

    public static class TaskConfigJsonDeserializationSchema implements DeserializationSchema<TaskDTO> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        @Override
        public TaskDTO deserialize(byte[] message) throws IOException {
            try {
                return objectMapper.readValue(message, TaskDTO.class);
            } catch (Exception e) {
                //反序列化失败，构建错误信息的TaskDTO对象并返回
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
                String originalMessage = new String(message, StandardCharsets.UTF_8);
                String currentTime = formatter.format(Instant.now());
                return TaskDTO.builder()
                        .task_data(String.format("{\"error\":\"form error\",\"information\":\"%s\",\"time\":\"%s\"}", originalMessage, currentTime))
                        .build();
            }
        }
        @Override
        public boolean isEndOfStream(TaskDTO nextElement) {
            return false;
        }
        @Override
        public TypeInformation<TaskDTO> getProducedType() {
            return TypeInformation.of(TaskDTO.class);
        }
    }
}
