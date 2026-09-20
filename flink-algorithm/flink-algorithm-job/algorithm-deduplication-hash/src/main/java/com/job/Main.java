package com.job;

import com.algorithm.common.model.SensorDTO;
import com.job.utils.Initialization;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashSet;

import static com.algorithm.common.constant.PropertiesConstants.jobName;
import static com.job.utils.Initialization.initKafkaSink;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<SensorDTO> input = Initialization.initDataStream(env);

        input = input.map((MapFunction<SensorDTO, SensorDTO>) sensorDTO -> {


            String inputData = sensorDTO.getMpData();

            HashSet<String> hashSet = new HashSet<>();

            String[] elements = inputData.split(","); // 以逗号分隔输入数据

            // 将元素添加到哈希集合中，自动去重
            for (String element : elements) {
                hashSet.add(element);
            }

            StringBuilder result = new StringBuilder(); // 用于存储去重后的结果

            // 将去重后的元素以逗号分隔的字符串形式添加到结果中
            for (String element : hashSet) {
                result.append(element).append(",");
            }
            // 移除最后一个逗号
            result.setLength(result.length() - 1);

            sensorDTO.setMpData(String.valueOf(result));

            return sensorDTO;
        });


        KafkaSink<SensorDTO> kafkaSink = initKafkaSink();

        input.sinkTo(kafkaSink);

        env.execute(jobName);
    }

}
