package com.algorithm.common.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KafkaUtilResnet {
    private static final Properties props;

    static {
        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            throw new RuntimeException("KafkaUtilResnet无法加载 application.properties", e);
        }
    }

    private static final String kafkaServers = props.getProperty("kafka.servers");
    private static final String topic = props.getProperty("kafka.resnet.topic");
    private static final String fileName = props.getProperty("file.resnet.name");

    private static final String FarmName = "普格海口风电场";
    private static final String TurbineName = "风机#4";
    private static final String Part = "发电机";
    private static final String Location = "发电机非驱动端轴承";
    private static final String WaveLength = "16.384K";
    private static final String DataType = "TIMEWAVE";
    private static final String WaveDefDescription = "TIMEWAVE";

    private static final long SampleRate = 51200;

    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        // 使用 Spring 的 Resource 来加载 classpath 中的文件，兼容 JAR 包
        Resource resource = new ClassPathResource(fileName);

        try (
                InputStream inputStream = resource.getInputStream();
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())
        ) {
            // 读取整个文件内容为一行字符串（默认按空格分隔）
            scanner.useDelimiter("\\A"); // 读取整个文件内容
            String content = scanner.hasNext() ? scanner.next().trim() : "";

            // 使用正则表达式按空格分割数据
            List<String> values = Pattern.compile("\\s+").splitAsStream(content).collect(Collectors.toList());
            System.out.println(values);

            AtomicInteger valueIndex = new AtomicInteger();
            values.forEach(value -> {
                try {
                    long currentTimeMillis = System.currentTimeMillis() / 1000;
                    String jsonData = String.format(
                            "{\"farmName\":\"%s\",\"turbineName\":\"%s\",\"part\":\"%s\",\"location\":\"%s\",\"sampleRate\": %d,\"waveLength\":\"%s\",\"dataType\":\"%s\",\"waveDefDescription\":\"%s\",\"acquisitionTime\":%d,\"dataFloat\":\"%s\"}",
                            FarmName, TurbineName, Part, Location, SampleRate, WaveLength, DataType, WaveDefDescription, currentTimeMillis, values);

                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, jsonData);
                    producer.send(record);
                    System.out.println("已发送数据: " + jsonData);

                    Thread.sleep(50000); // 原逻辑保留
                    System.out.println("已发送 " + (valueIndex.get()) + " 个值");
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

    public static KafkaProducer<String, String> initData() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 5 * 1024 * 1024);
        return new KafkaProducer<>(props);
    }
}
