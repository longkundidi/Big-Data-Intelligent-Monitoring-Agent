package com.job.utils;

import com.algorithm.common.model.SensorDTO;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkTopics;
import static com.algorithm.common.constant.PropertiesConstants.*;
import static com.algorithm.common.utils.ExecutionEnvUtil.PARAMETER_TOOL;


public class Initialization {

    public static void initProperties() {
        //job
        jobName = PARAMETER_TOOL.get(jobName);
        //kafka
        kafkaBrokers = PARAMETER_TOOL.get(kafkaBrokers);
        kafkaGroupId = PARAMETER_TOOL.get(kafkaGroupId);
        kafkaTopics = PARAMETER_TOOL.get(kafkaTopics);
        //kafkaSink
        kafkaSinkTopics = PARAMETER_TOOL.get(kafkaSinkTopics);
        kafkaSinkBrokers = PARAMETER_TOOL.get(kafkaSinkBrokers);
    }

    public static DataStream<SensorDTO> initDataStream(StreamExecutionEnvironment env) {
        initProperties();
        KafkaSource<SensorDTO> source = KafkaSource.<SensorDTO>builder().setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(kafkaBrokers)
                .setTopics(kafkaTopics)
                .setGroupId(kafkaGroupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new JsonDeserializationSchema<>(SensorDTO.class))
                .build();
        return env.fromSource(source, WatermarkStrategy.noWatermarks(), jobName + " Kafka Source");
    }

    public static KafkaSink<SensorDTO> initKafkaSink() {
        return KafkaSink.<SensorDTO>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(kafkaSinkTopics)
                        .setValueSerializationSchema(new JsonSerializationSchema<SensorDTO>())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

}
