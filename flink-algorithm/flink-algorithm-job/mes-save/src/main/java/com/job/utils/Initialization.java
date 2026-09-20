package com.job.utils;


import com.algorithm.common.model.MesSinkDTO;
import com.algorithm.common.model.SensorOriginalDTO;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.sink.InfluxDBSink;

import static com.algorithm.common.constant.InfluxDBPropertiesConstants.*;
import static com.algorithm.common.constant.InfluxDBPropertiesConstants.influxDBOrganization;
import static com.algorithm.common.constant.PropertiesConstants.*;
import static com.algorithm.common.utils.ExecutionEnvUtil.PARAMETER_TOOL;
import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.*;

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

    public static DataStream<MesSinkDTO> initDataStream(StreamExecutionEnvironment env) {
        initProperties();

        KafkaSource<MesSinkDTO> source = KafkaSource.<MesSinkDTO>builder().setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(kafkaBrokers)
                .setTopicPattern(java.util.regex.Pattern.compile(kafkaTopics))
                .setGroupId(kafkaGroupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new JsonDeserializationSchema<>(MesSinkDTO.class))
                .build();
        return env.fromSource(source, WatermarkStrategy.noWatermarks(), jobName);
    }

    public static InfluxDBSink<MesSinkDTO> initInfluxDBSink(int bufferSize) {
        return InfluxDBSink.builder()
                .setInfluxDBSchemaSerializer(new MesSinkDTOSerializer())
                .setInfluxDBUrl(influxDBUrl)
                .setInfluxDBToken(influxDBToken)
                .setInfluxDBBucket(influxDBBucket)
                .setInfluxDBOrganization(influxDBOrganization)
                .setWriteBufferSize(bufferSize)
                .build();
    }

}
