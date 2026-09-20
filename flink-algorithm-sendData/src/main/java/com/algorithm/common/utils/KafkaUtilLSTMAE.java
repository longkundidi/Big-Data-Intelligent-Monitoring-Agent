package com.algorithm.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class KafkaUtilLSTMAE {

    private static final Properties props;

    static {
        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            throw new RuntimeException("KafkaUtilLSTMAE无法加载 application.properties", e);
        }
    }

    private static final String kafkaServers = props.getProperty("kafka.servers");
    private static final String topic = props.getProperty("kafka.lstmae.topic");
    private static final String fileName = props.getProperty("file.lstmae.name");

    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        // 使用 Spring 的 Resource 来加载文件
        Resource resource = new ClassPathResource(fileName);

        while (true) { // 无限循环
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                    Stream<String> lines = reader.lines()
            ) {
                AtomicInteger i = new AtomicInteger();
                lines.forEach(ele -> {
                    try {
                        String cleanedData = ele.replace("\uFEFF", "");
                        long currentTimeMillis = System.currentTimeMillis();
                        String jsonData = String.format("{\"dc_data\": \"%s\", \"dc_time\": %d , \"id\": 218}", cleanedData, currentTimeMillis);

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
            } catch (Exception e) {
                e.printStackTrace();
            }
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
