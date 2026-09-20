package com.job.utils;

import com.algorithm.common.utils.JsonUtil;
import com.influxdb.client.InfluxDBClientOptions;
import com.job.dto.ForecastRms;
import com.job.dto.ForecastRmsDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Calendar;
import java.util.Properties;

import static com.algorithm.common.utils.Tools.mockDoubleBetween;


public class ForecastRmsKafkaTest {

    private static final String topic = "algorithm_sink_forecast_rms";

    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        String monitorPointId = "P9800_1_002_obj3_4_s1";
        String serverId = "1613069187702624258";

        int count = 1000;
        for (int i = 1; i <= count; i++) {
            ForecastRms rms = new ForecastRms(mockDoubleBetween(0D, 2D), 3F, 0);
            ForecastRmsDTO dto = new ForecastRmsDTO(monitorPointId, serverId, Calendar.getInstance().getTimeInMillis(), rms);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, JsonUtil.toJson(dto));
            producer.send(record);
            System.out.println("已发送" + i + "条");
            if (i != count) {
                Thread.sleep(1 * 500);
            }
        }
        producer.flush();
        producer.close();
    }

    public static void main(String[] args) throws Exception {
        writeToKafka();
    }

    private static final String broker_list = "192.168.1.91:9094,192.168.1.91:9095,192.168.1.91:9096";

    public static KafkaProducer<String, String> initData() {
        Properties props = new Properties();
        props.put("bootstrap.servers", broker_list);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }
}
