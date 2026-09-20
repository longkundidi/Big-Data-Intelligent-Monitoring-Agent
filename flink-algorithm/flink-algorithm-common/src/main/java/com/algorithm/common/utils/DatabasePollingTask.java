package com.algorithm.common.utils;

import com.algorithm.common.model.SensorOriginalDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/*
从mysql定期读取数据并发送到kafka
这段代码的核心任务是：定期轮询 MySQL 数据库中的 al_algorithm 表，检查是否有新的数据记录。如果发现新的数据记录，代码将根据记录生成相应的文件名，读取文件并将每一行数据转换为 SensorOriginalDTO 对象，最终将这些数据发送到 Kafka 中的指定主题。这样可以实现从数据库到 Kafka 的数据流动。

主要技术点：
数据库轮询：定时查询数据库中的新数据。
Kafka Producer：将查询到的数据发送到 Kafka 消息队列。
定时任务调度：使用 ScheduledExecutorService 定期执行任务。
 */
public class DatabasePollingTask {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/flink_web";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "zYn691226";
    private static int lastMaxRecordId = 30;
    private static String lastAlgorithmName = null;
    private static String lastserverId = null;
    private static String lasttaskId = null;
    private static final String filePrefix = "flink-algorithm-common/src/main/resources/";

    private static String fileName = null;
    //    private static String fileName = filePrefix + "RMSGRU_based_for_windpower.csv";
    private static final String broker_list = "202.115.65.23:9094,202.115.65.23:9095,202.115.65.23:9096";
    private static final String topic = "sensor_part_P9800_1_002_obj3_4";
    private static final String monitorPointId = "P9800_1_002_obj3_4_s3";


    public static void main(String[] args) {
        //创建一个调度线程池，用于定期执行任务。
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        //设置定时任务，每隔 5 秒调用一次 pollDatabase 方法。该方法会轮询数据库并检查新数据。
        executor.scheduleAtFixedRate(DatabasePollingTask::pollDatabase, 0, 5, TimeUnit.SECONDS);
    }

    private static void pollDatabase() {
        try {
            // 建立数据库连接
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();

            // 执行查询语句
            ResultSet resultSet = statement.executeQuery("SELECT * FROM al_algorithm");


            // 获取最大记录ID
            int maxRecordId = 1;
            while (resultSet.next()) {
                int recordId = resultSet.getInt("id");
                String alName = resultSet.getString("al_name");
                String serverId = resultSet.getString("server_id");
                String taskId = resultSet.getString("task_id");

                if (recordId > maxRecordId) {
                    maxRecordId = recordId;
                    lastAlgorithmName = alName;
                    lastserverId = serverId;
                    lasttaskId = taskId;
                }
            }

            // 检查最大记录ID是否变化
            if (maxRecordId > lastMaxRecordId) {
                if (lastAlgorithmName != null) {
                    fileName = filePrefix + lastAlgorithmName + ".csv";
                    writeToKafka();
                }

//                writeToKafka();

                // 更新最大记录ID
                lastMaxRecordId = maxRecordId;
            }

            // 关闭资源
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToKafka() throws Exception {
        KafkaProducer<String, String> producer = initData();

        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            AtomicInteger i = new AtomicInteger();
            lines.forEach(ele -> {
                try {
//                    if (i.get() >= 10) {
//                        // 达到指定数量后退出循环
//                        return;
//                    }
                    SensorOriginalDTO sensorDTO = new SensorOriginalDTO(monitorPointId, Calendar.getInstance().getTimeInMillis(), ele, "string", lastAlgorithmName, lastserverId, lasttaskId);
//                    SensorOriginalDTO sensorDTO = new SensorOriginalDTO(monitorPointId, Calendar.getInstance().getTimeInMillis(), ele, "string");

                    System.out.println(JsonUtil.toJson(sensorDTO));
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, JsonUtil.toJson(sensorDTO));
                    producer.send(record);
                    int sentCount = i.incrementAndGet();
//                    i.getAndIncrement();
                    System.out.println("已发送" + sentCount + "条");
                    Thread.sleep(1 * 1000);
//                    Thread.sleep(1 * 300);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }


        /*int count = 101;
        for (int i = 1; i <= count; i++) {
            SensorOriginalDTO sensorOriginalDTO = new SensorOriginalDTO(monitorPointId, Calendar.getInstance().getTimeInMillis(), Integer.toString(i), "long");
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, JsonUtil.toJson(sensorOriginalDTO));
            producer.send(record);
            System.out.println("已发送" + i + "条");
            if (i != count) {
                Thread.sleep(1 * 500);
            }
        }*/
        producer.flush();
        producer.close();
    }

    public static KafkaProducer<String, String> initData() {
        Properties props = new Properties();
        props.put("bootstrap.servers", broker_list);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }
}
