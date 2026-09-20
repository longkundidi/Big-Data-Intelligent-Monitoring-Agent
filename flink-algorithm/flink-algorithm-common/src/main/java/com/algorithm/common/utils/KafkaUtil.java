package com.algorithm.common.utils;

import com.algorithm.common.model.SensorDTO;
import com.algorithm.common.model.SensorOriginalDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/*
从指定的 CSV 文件中逐行读取数据。
每行数据封装为 SensorOriginalDTO 对象，并附加了时间戳、算法名、服务器 ID 等信息。
将每个 SensorOriginalDTO 对象转化为 JSON 格式，并通过 Kafka producer 发送到指定的 Kafka 主题。
 */
public class KafkaUtil {

    private static final String algorithmName = "detection_rms";

    private static final String filePrefix = "flink-algorithm-common/src/main/resources/";


//    private static final String fileName = filePrefix + "RMS_based_for_ballbearing.csv";

    private static String lastAlgorithmName = "MultiFeatureIndexFusionAE_based_for_hob";

//    private static String lastAlgorithmName = "Pearson_based_for_ballbearing";


//    private static String lastAlgorithmName = "Tianciwan_bearing_1";

    private static final String fileName = filePrefix + lastAlgorithmName + ".csv";
    private static String lastserverId = "1686675255581515778";
    private static String lasttaskId = "1686571497245806594";


    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        try (Stream<String> lines = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
            AtomicInteger i = new AtomicInteger();
            lines.forEach(ele -> {
                try {
                    String cleanedData = ele.replace("\uFEFF", "");
                    SensorOriginalDTO sensorDTO = new SensorOriginalDTO(monitorPointId, Calendar.getInstance().getTimeInMillis(), cleanedData, "string",lastAlgorithmName,lastserverId,lasttaskId);
//                    SensorOriginalDTO sensorDTO = new SensorOriginalDTO(monitorPointId, Calendar.getInstance().getTimeInMillis(), cleanedData, "string");


                    System.out.println(JsonUtil.toJson(sensorDTO));
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, JsonUtil.toJson(sensorDTO));
                    producer.send(record);
                    i.getAndIncrement();
                    System.out.println("已发送" + i + "条");
                    Thread.sleep(1 * 500);
//                    Thread.sleep(1 * 300);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
        producer.flush();
        producer.close();
    }

//    public static void writeToKafka() throws Exception {
//        KafkaProducer<String, String> producer = initData();
//
//        try (Stream<String> lines = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)) {
//            AtomicInteger i = new AtomicInteger();
//            lines.forEach(line -> {
//                String[] data = line.split(","); // 假设CSV文件以逗号分隔
//                try {
//                    String[] monitorPointIds = {"Tianciwan_bearing_1_Power", "Tianciwan_bearing_1_CabinTemperature", "Tianciwan_bearing_1_RotorSpeed", "Tianciwan_bearing_1_FrontBearingTemperature", "Tianciwan_bearing_1_RearBearingTemperature"};
//
//                    for (int j = 1; j < data.length; j++) {
//                        String currentMonitorPointId = monitorPointIds[j - 1];
//                        String trimmedDateStr = data[0].trim().replaceAll("[^\\x00-\\x7F]", "");  // 去除空白字符和特殊字符
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/d HH:mm");
//                        Date date = sdf.parse(trimmedDateStr);
//                        long timestamp = date.getTime();
//                        SensorOriginalDTO sensorDTO = new SensorOriginalDTO(currentMonitorPointId, timestamp, data[j], "double");
//                        System.out.println(JsonUtil.toJson(sensorDTO));
//                        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, JsonUtil.toJson(sensorDTO));
//                        producer.send(record);
//                        i.getAndIncrement();
//                        System.out.println("已发送" + i + "条");
//                        Thread.sleep(1 * 100);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//        producer.flush();
//        producer.close();
//    }




    public static void main(String[] args) throws Exception {
        writeToKafka();
    }

//    private static final String broker_list = "kafka1.bg.local:9094,kafka2.bg.local:9095,kafka3.bg.local:9096";
//    private static final String broker_list = "192.168.65.134:9092";
    private static final String broker_list = "192.168.16.219:9092";
//    private static final String broker_list = "202.115.65.23:9094,202.115.65.23:9095,202.115.65.23:9096";
    private static final String topic = "dc_source_1";
//    private static final String topic = "sensor_part_P";

//    private static final String monitorPointId = "Qiduntan_fan_22_CabinTemperature";

    private static final String monitorPointId = "P9800_1_002_obj3_4_s3";
//    private static final String monitorPointId = "P7600-001-obj1-1-s11";


    public static KafkaProducer<String, String> initData() {
        Properties props = new Properties();
        props.put("bootstrap.servers", broker_list);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }
}
