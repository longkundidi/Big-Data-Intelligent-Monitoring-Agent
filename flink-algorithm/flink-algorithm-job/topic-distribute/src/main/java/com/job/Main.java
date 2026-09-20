package com.job;

import com.algorithm.common.model.SensorDTO;
import com.algorithm.common.model.SensorOriginalDTO;
import com.job.utils.Initialization;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.sink.InfluxDBSink;

import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.PropertiesConstants.jobName;
import static com.job.utils.Initialization.initDataStream;


public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<SensorOriginalDTO> input = initDataStream(env);

        InfluxDBSink<SensorOriginalDTO> influxDBSink = Initialization.initInfluxDBSink(10);

        input.sinkTo(influxDBSink);

        DataStream<SensorDTO> sensorDTODataStream = input.map((MapFunction<SensorOriginalDTO, SensorDTO>) sensorOriginalDTO -> SensorDTO.builder()
                .monitorPointId(sensorOriginalDTO.getMonitorPointId())
                .mpTime(sensorOriginalDTO.getMpTime())
                .algorithmName(sensorOriginalDTO.getAlgorithmName())
                .serverId(sensorOriginalDTO.getServerId())
                .taskId(sensorOriginalDTO.getTaskId())
                .mpData(sensorOriginalDTO.getMpData()).build());


        KafkaSink<SensorDTO> kafkaSink = KafkaSink.<SensorDTO>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector((TopicSelector<SensorDTO>) sensorDTO -> "sensor_mp_" + sensorDTO.getMonitorPointId())
                        .setValueSerializationSchema(new JsonSerializationSchema<>())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
        sensorDTODataStream.sinkTo(kafkaSink);

        env.execute(jobName);

    }
}
