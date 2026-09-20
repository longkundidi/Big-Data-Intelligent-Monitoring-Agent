package com.algorithm.common.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KafkaUtilResnet {

    private static final String kafkaServers = "192.168.16.219:9092";
    private static final String topic = "dc_CMS";

    private static final String fileName = "flink-algorithm-common/src/main/resources/test_for_diagnose.txt"; //  文件路径

    private static final String FarmName="普格海口风电场";
    private static final String TurbineName="风机#4";
    private static final String Part="发电机";
    private static final String Location="发电机非驱动端轴承";
    private static final String WaveLength="16.384K";
    private static final String DataType="TIMEWAVE";
    private static final String WaveDefDescription="TIMEWAVE";


    private static final long SampleRate=51200;


    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();
        //TODO 修改成读取txt文件，并且读取里面的每个值，其中里面的值是按照空格分割的，

        try {
            // 读取整个文件内容为一行
            String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8).trim();

            // 使用正则表达式按空格分割数据
            List<String> values = Pattern.compile("\\s+").splitAsStream(content).collect(Collectors.toList());
            System.out.println(values);
            AtomicInteger valueIndex = new AtomicInteger();
            values.forEach(value -> {
                try {
                    long currentTimeMillis = System.currentTimeMillis() / 1000;
                    // 假设每个值都要单独处理并发送到Kafka
                    String jsonData = String.format("{\"farmName\":\"%s\",\"turbineName\":\"%s\",\"part\":\"%s\",\"location\":\"%s\",\"sampleRate\": %d,\"waveLength\":\"%s\",\"dataType\":\"%s\",\"waveDefDescription\":\"%s\",\"acquisitionTime\":%d,\"dataFloat\":\"%s\"}",
                            FarmName, TurbineName, Part, Location, SampleRate, WaveLength, DataType, WaveDefDescription, currentTimeMillis, values);
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, jsonData);
                    producer.send(record);
                    System.out.println("已发送数据: " + jsonData);
                    // 根据需要决定是否保持原有的延时逻辑，这里注释掉了Thread.sleep
                     Thread.sleep(50000);
                    System.out.println("已发送 " + (valueIndex.get() ) + " 个值");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }  finally {
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
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 5 * 1024 * 1024);
        return new KafkaProducer<>(props);
    }
}
