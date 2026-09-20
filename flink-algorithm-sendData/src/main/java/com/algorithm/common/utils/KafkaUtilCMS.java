package com.algorithm.common.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KafkaUtilCMS {

    private static final Properties props;

    static {
        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            throw new RuntimeException("KafkaUtilCMS 无法加载application.properties", e);
        }
    }

    private static final String kafkaServers = props.getProperty("kafka.servers");
    private static final String topic = props.getProperty("kafka.cms.topic");
    private static final String fileName = "华电小高山风电场 _35#风机_主轴垂直1V_128k 加速度波形(0.1-20000)_51200HZ_加速度_7RPM_20221018162400.txt";

    public static void writeToKafka() throws Exception {

        KafkaProducer<String, String> producer = initData();

        try {
            // 读取整个文件内容为一行
            String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8).trim();

            // 使用正则表达式按空格分割数据
            List<String> values = Pattern.compile("\\s+").splitAsStream(content).collect(Collectors.toList());
            AtomicInteger i = new AtomicInteger();

            values.forEach(ele -> {
                try {
                    long currentTimeMillis = System.currentTimeMillis();
//                String jsonArrayString = String.join(", ", values);
                    String jsonData = String.format("{\"dc_data\": \"%s\", \"dc_time\": %d , \"id\": 51200}", values, currentTimeMillis);

                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, jsonData);
                    producer.send(record);
                    System.out.println("已发送数据: " + jsonData);
                    Thread.sleep(5000);
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
        String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8).trim();

        // 使用正则表达式按空格分割数据
        List<String> values = Pattern.compile("\\s+").splitAsStream(content).collect(Collectors.toList());


        writeToKafka();

    }

    public static KafkaProducer<String, String> initData() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 5 * 1024 * 1024);
        return new KafkaProducer<>(props);
    }
}
