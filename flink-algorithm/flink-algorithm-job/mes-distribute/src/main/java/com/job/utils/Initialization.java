package com.job.utils;

import com.algorithm.common.model.MesDTO;
import com.algorithm.common.model.PropertyItem;
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
import java.util.Collections;

import static com.algorithm.common.constant.InfluxDBPropertiesConstants.*;
import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.PropertiesConstants.*;
import static com.job.utils.ExecutionEnvUtil.PARAMETER_TOOL;

public class Initialization {

    public static void initProperties() {
        //job
        jobName = PARAMETER_TOOL.get(jobName);
        //kafka
        kafkaBrokers = PARAMETER_TOOL.get(kafkaBrokers);
        kafkaGroupId = PARAMETER_TOOL.get(kafkaGroupId);
        kafkaTopics = PARAMETER_TOOL.get(kafkaTopics);
        //kafkaSink
        kafkaSinkBrokers = PARAMETER_TOOL.get(kafkaSinkBrokers, kafkaBrokers);
        //influxDB
        influxDBUrl = PARAMETER_TOOL.get(influxDBUrl);
        influxDBToken = PARAMETER_TOOL.get(influxDBToken);
        influxDBBucket = PARAMETER_TOOL.get(influxDBBucket);
        influxDBOrganization = PARAMETER_TOOL.get(influxDBOrganization);
    }

    public static DataStream<MesDTO> initDataStream(StreamExecutionEnvironment env) {
        initProperties();
        KafkaSource<MesDTO> source = KafkaSource.<MesDTO>builder().setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(kafkaBrokers)
                .setTopicPattern(java.util.regex.Pattern.compile(kafkaTopics))
                .setGroupId(kafkaGroupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SafeJsonDeserializationSchema())
                .build();
        return env.fromSource(source, WatermarkStrategy.noWatermarks(), jobName);
    }

    public static class SafeJsonDeserializationSchema implements DeserializationSchema<MesDTO> {
        private ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public MesDTO deserialize(byte[] message) throws IOException {
            try {
                return objectMapper.readValue(message, MesDTO.class);
            } catch (Exception e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
                String currentDate = formatter.format(Instant.now());
                long errorTime = Instant.now().toEpochMilli();
                String originalMessage = new String(message, StandardCharsets.UTF_8);
                PropertyItem errorItem = new PropertyItem("Error", "Deserialization failed: " + originalMessage + ", Error time: " + currentDate, "Error");
                return new MesDTO(errorTime, Collections.singletonList(errorItem));
            }
        }

        @Override
        public boolean isEndOfStream(MesDTO nextElement) {
            return false;
        }

        @Override
        public TypeInformation<MesDTO> getProducedType() {
            return TypeInformation.of(MesDTO.class);
        }
    }
}
