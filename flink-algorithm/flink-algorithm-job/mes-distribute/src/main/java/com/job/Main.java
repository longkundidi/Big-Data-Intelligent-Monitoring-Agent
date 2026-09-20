package com.job;

import com.algorithm.common.model.*;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;

import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.PropertiesConstants.*;
import static com.job.utils.Initialization.initDataStream;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                10, // 尝试重启的次数
                org.apache.flink.api.common.time.Time.of(30, java.util.concurrent.TimeUnit.SECONDS) // 重启间隔
        ));

        final OutputTag<String> errorOutputTag = new OutputTag<String>("side-output-error") {}; // 侧输出流

        DataStream<MesDTO> input = initDataStream(env);

        SingleOutputStreamOperator<MesSinkDTO> transformedDataStream = input
                .process(new ProcessFunction<MesDTO, MesSinkDTO>() {
                    @Override
                    public void processElement(MesDTO mes, Context ctx, Collector<MesSinkDTO> out){
                        if (mes != null && !mes.getProperty().get(0).getKey().equals("Error")) {
                            for (PropertyItem item : mes.getProperty()) {
                                MesSinkDTO sinkItem = MesSinkDTO.builder()
                                        .time(mes.getTime())
                                        .key(item.getKey())
                                        .value(item.getValue())
                                        .id(item.getId())
                                        .build();
                                out.collect(sinkItem);
                            }
                        } else {
                            String errorMessage = mes.getProperty().get(0).getValue();
                            ctx.output(errorOutputTag, errorMessage);
                        }
                    }
                });


        KafkaSink<MesSinkDTO> kafkaSink = KafkaSink.<MesSinkDTO>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector((TopicSelector<MesSinkDTO>) item -> "mes_sink_" +item.getId())
                        .setValueSerializationSchema(new JsonSerializationSchema<>())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        transformedDataStream.sinkTo(kafkaSink);

        DataStream<String> errorStream = transformedDataStream.getSideOutput(errorOutputTag);

        KafkaSink<String> errorSink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("error_topic")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        errorStream.sinkTo(errorSink);

        env.execute(jobName);
    }
}
