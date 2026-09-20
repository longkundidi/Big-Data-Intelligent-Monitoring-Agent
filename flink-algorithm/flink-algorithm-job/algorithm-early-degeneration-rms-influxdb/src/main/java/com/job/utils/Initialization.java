package com.job.utils;

import com.job.dto.EarlyDegenerationRmsDTO;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.sink.InfluxDBSink;

import static com.algorithm.common.constant.InfluxDBPropertiesConstants.*;
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
        //influxDB
        influxDBUrl = PARAMETER_TOOL.get(influxDBUrl);
        influxDBToken = PARAMETER_TOOL.get(influxDBToken);
        influxDBBucket = PARAMETER_TOOL.get(influxDBBucket);
        influxDBOrganization = PARAMETER_TOOL.get(influxDBOrganization);
    }

    public static DataStream<EarlyDegenerationRmsDTO> initDataStream(StreamExecutionEnvironment env) {
        initProperties();
        KafkaSource<EarlyDegenerationRmsDTO> source = KafkaSource.<EarlyDegenerationRmsDTO>builder().setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(kafkaBrokers)
                .setTopics(kafkaTopics)
                .setGroupId(kafkaGroupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new JsonDeserializationSchema<>(EarlyDegenerationRmsDTO.class))
                .build();
        return env.fromSource(source, WatermarkStrategy.noWatermarks(), jobName + " Kafka Source");
    }

    public static InfluxDBSink<EarlyDegenerationRmsDTO> initInfluxDBSink(int bufferSize) {
        return InfluxDBSink.builder()
                .setInfluxDBSchemaSerializer(new EarlyDegenerationRmsDTOSerializer())
                .setInfluxDBUrl(influxDBUrl)
                .setInfluxDBToken(influxDBToken)
                .setInfluxDBBucket(influxDBBucket)
                .setInfluxDBOrganization(influxDBOrganization)
                .setWriteBufferSize(bufferSize)
                .build();
    }

}
