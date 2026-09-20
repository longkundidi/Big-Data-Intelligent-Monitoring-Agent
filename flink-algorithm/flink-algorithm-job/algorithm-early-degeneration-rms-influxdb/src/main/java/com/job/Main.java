package com.job;

import com.job.dto.EarlyDegenerationRmsDTO;
import com.job.utils.Initialization;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.influxdb.sink.InfluxDBSink;

import static com.algorithm.common.constant.PropertiesConstants.*;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<EarlyDegenerationRmsDTO> input = Initialization.initDataStream(env);

        InfluxDBSink<EarlyDegenerationRmsDTO> influxDBSink = Initialization.initInfluxDBSink(100);

        input.sinkTo(influxDBSink);

        env.execute(jobName);
    }
}
