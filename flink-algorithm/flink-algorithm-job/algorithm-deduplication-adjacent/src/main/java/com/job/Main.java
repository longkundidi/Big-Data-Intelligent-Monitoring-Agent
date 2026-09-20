package com.job;

import com.job.utils.SensorDTO;
import com.job.utils.Initialization;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.job.utils.PropertiesConstants.*;
import static com.job.utils.Initialization.initKafkaSink;
//相邻去重算法，处理后的数据被直接写入 Kafka，便于后续分析或消费。
public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<SensorDTO> input = Initialization.initDataStream(env);

        input = input.map((MapFunction<SensorDTO, SensorDTO>) sensorDTO -> {

            String[] values = sensorDTO.getMpData().split(",");

            // 使用一个列表来存储去重后的数据
            List<String> deduplicatedValues = new ArrayList<String>();

            // 遍历输入数据数组
            for (int i = 0; i < values.length; i++) {
                // 如果当前数据与前一个数据不相同，将其添加到去重后的列表中
                if (i == 0 || !values[i].equals(values[i-1])) {
                    deduplicatedValues.add(values[i]);
                }
            }

            // 将去重后的数据列表按逗号连接成字符串作为输出结果
            String deduplicatedData = String.join(",", deduplicatedValues);


            sensorDTO.setMpData(deduplicatedData);
            return sensorDTO;

        });


        KafkaSink<SensorDTO> kafkaSink = initKafkaSink();

        input.sinkTo(kafkaSink);

        env.execute(jobName);
    }

}
