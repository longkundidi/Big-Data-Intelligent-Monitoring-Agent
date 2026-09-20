package com.job.utils;

import com.algorithm.common.utils.JsonUtil;
import com.job.dto.EarlyDegenerationRms;
import com.job.dto.EarlyDegenerationRmsDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Calendar;
import java.util.Properties;

import static com.algorithm.common.utils.Tools.mockDoubleBetween;


public class EarlyDegenerationRmsKafkaTest {

    private static final String topic = "algorithm_sink_early_degeneration_rms";

    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        String monitorPointId = "P9800_1_002_obj3_4_s1";
        String serverId = "1613069187702624258";

        int count = 1000;
        for (int i = 1; i <= count; i++) {
            EarlyDegenerationRms rms = new EarlyDegenerationRms(mockDoubleBetween(0D, 5D), 6F, 0);
            EarlyDegenerationRmsDTO dto = new EarlyDegenerationRmsDTO(monitorPointId,serverId, Calendar.getInstance().getTimeInMillis(), rms);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, JsonUtil.toJson(dto));
            producer.send(record);
            System.out.println("已发送" + i + "条");
            if (i != count) {
                Thread.sleep(1 * 1000);
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
