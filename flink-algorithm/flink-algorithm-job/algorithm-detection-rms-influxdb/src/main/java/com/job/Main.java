package com.job;

import com.job.dto.DetectionRmsDTO;
import com.job.utils.Initialization;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.sink.InfluxDBSink;

import static com.algorithm.common.constant.PropertiesConstants.*;
//将数据写入influxdb中
public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<DetectionRmsDTO> input = Initialization.initDataStream(env);

        InfluxDBSink<DetectionRmsDTO> influxDBSink = Initialization.initInfluxDBSink(100);

        input.sinkTo(influxDBSink);

        env.execute(jobName);
    }
}
