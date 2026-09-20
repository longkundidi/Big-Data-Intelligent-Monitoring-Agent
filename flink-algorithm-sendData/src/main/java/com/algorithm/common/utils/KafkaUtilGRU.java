package com.algorithm.common.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class KafkaUtilGRU {

    private static final Properties props;

    static {
        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            throw new RuntimeException("KafkaUtilGRU 无法加载application.properties", e);
        }
    }

    private static final String kafkaServers = props.getProperty("kafka.servers");
//    private static final String topic = "dc_source_104";
//    private static final String fileName = "flink-algorithm-common/src/main/resources/CAE_based_for_hob.csv"; // CSV 文件路径

//    private static final String topic = "dc_source_158";
//    private static final String fileName = "flink-algorithm-common/src/main/resources/LSTMAE_pghk.csv"; // CSV 文件路径

    private static final String topic = props.getProperty("kafka.gru.topic");
    private static final String fileName = props.getProperty("file.gru.name");




    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        try (Stream<String> lines = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
            AtomicInteger i = new AtomicInteger();
            lines.forEach(ele -> {
                try {
                    String cleanedData = ele.replace("\uFEFF", "");
                    long currentTimeMillis = System.currentTimeMillis();
                    String jsonData = String.format("{\"dc_data\": \"%s\", \"dc_time\": %d , \"id\": 170}", cleanedData, currentTimeMillis);

                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, jsonData);
                    producer.send(record);
                    System.out.println("已发送数据: " + jsonData);
                    Thread.sleep(500);
                    i.getAndIncrement();
                    System.out.println("已发送" + i + "条");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        } finally {
            producer.flush();
            producer.close();
        }
    }

    public static void main(String[] args) throws Exception {
        writeToKafka();
    }

    public static KafkaProducer<String, String> initData() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }
}
