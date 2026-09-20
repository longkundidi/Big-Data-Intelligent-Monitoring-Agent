package com.algorithm.common.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class KafkaUtilLSTM {

    private static final Properties props;

    static {
        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            throw new RuntimeException("KafkaUtilLSTM 无法加载application.properties", e);
        }
    }


    private static final String kafkaServers = props.getProperty("kafka.servers");
//    private static final String topic = "dc_source_104";
//    private static final String fileName = "flink-algorithm-common/src/main/resources/CAE_based_for_hob.csv"; // CSV 文件路径

//    private static final String topic = "dc_source_158";
//    private static final String fileName = "flink-algorithm-common/src/main/resources/LSTMAE_pghk.csv"; // CSV 文件路径

    private static final String topic = props.getProperty("kafka.lstm.topic");
    private static final String fileName = props.getProperty("file.lstm.name");




    public static void writeToKafka() throws Exception {
        try (Stream<String> lines = openLines();
             KafkaProducer<String, String> producer = initData()) {
            AtomicInteger i = new AtomicInteger();
            lines.forEach(ele -> {
                try {
                    String cleanedData = ele.replace("\uFEFF", "");
                    long currentTimeMillis = System.currentTimeMillis();
                    String jsonData = String.format("{\"dc_data\": \"%s\", \"dc_time\": %d , \"id\": 217}", cleanedData, currentTimeMillis);

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
            producer.flush();
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

    private static Stream<String> openLines() throws IOException {
        Path filePath = Paths.get(fileName);
        if (Files.exists(filePath)) {
            return Files.lines(filePath, StandardCharsets.UTF_8);
        }

        ClassPathResource resource = new ClassPathResource(fileName);
        if (resource.exists()) {
            InputStream inputStream = resource.getInputStream();
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return new java.io.BufferedReader(reader).lines();
        }

        throw new IOException("CSV file not found: " + fileName
                + ". Checked working directory and classpath resources.");
    }
}