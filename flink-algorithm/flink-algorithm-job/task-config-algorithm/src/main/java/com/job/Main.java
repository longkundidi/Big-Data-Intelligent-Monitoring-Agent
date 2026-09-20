package com.job;

import com.algorithm.common.model.TaskDTO;
import com.job.utils.Initialization;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.PropertiesConstants.jobName;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(3);

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                10, // 尝试重启的次数
                org.apache.flink.api.common.time.Time.of(30, java.util.concurrent.TimeUnit.SECONDS) // 重启间隔
        ));

        final OutputTag<String> errorFormOutputTag = new OutputTag<String>("side-output-error-form") {};

        DataStream<TaskDTO> input = Initialization.initDataStream(env);

        SingleOutputStreamOperator<TaskDTO> mainDataStream = input
                .process(new ProcessFunction<TaskDTO, TaskDTO>() {
                    @Override
                    public void processElement(TaskDTO value, Context ctx, Collector<TaskDTO> out) throws Exception {
                        String algorithmName;
                        algorithmName = getAlgorithmFromDB(value.getTask_id(), value.getTask_sequence());
                        // 验证数据格式
                        if (!validateTaskDTO(value, algorithmName)) {
                            // 格式不符合，通过侧输出流输出
                            ctx.output(errorFormOutputTag, value.getTask_data());
                            return;
                        }


                        try {
                            if (algorithmName != null) {
                                // 如果查询到了算法名称，将数据发送到主输出流
                                out.collect(value);
                            } else {
                                // 如果没有查询到算法名称，插入到task_config_result表中
                                insertIntoTaskConfigResult(value);
                            }
                        } catch (SQLException e) {
                            // 处理SQL异常
                            System.err.println("Failed to insert into database: " + e.getMessage());
                        }
                    }
                });

        //主输出流
        KafkaSink<TaskDTO> kafkaSink = KafkaSink.<TaskDTO>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector((TopicSelector<TaskDTO>) value -> {
                            try {
                                String algorithmName = getAlgorithmFromDB(value.getTask_id(), value.getTask_sequence());
                                if (algorithmName == null) {
                                    // 返回null以指示不发送消息
                                    return null;
                                }
                                return "task_config_" + algorithmName;
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .setValueSerializationSchema(new JsonSerializationSchema<>())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        //侧输出流-格式有误
        KafkaSink<String> errorFormSink  = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("task_config_error_form")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        mainDataStream.sinkTo(kafkaSink);
        mainDataStream.getSideOutput(errorFormOutputTag).sinkTo(errorFormSink);

        env.execute(jobName);
    }

    private static boolean validateTaskDTO(TaskDTO task, String algorithmName) {
        // 校验信息
        if (task.getTask_data() != null && task.getTask_data().startsWith("{\"error\"")) {
            return false;
        }
        if(algorithmName != null){
            try {
                String[] parts = task.getTask_data().split(",");
                for (String part : parts) {
                    Double.parseDouble(part);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return true;

    }

    private static String getAlgorithmFromDB(Long id, Integer sequence) throws SQLException {
        String query = "SELECT al_short_name FROM task_config WHERE task_id = ? AND sequence = ?";
        int retryCount = 5;
        for (int attempt = 0; attempt < retryCount; attempt++) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.16.219:3306/new_algorithom_Repository", "root", "728af18d87824a28b45f39106fe9de0e");
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setLong(1, id);
                pstmt.setInt(2, sequence);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("al_short_name");
                    } else {
                        // 返回一个特殊值，例如null，表示未找到记录
                        return null;
                    }
                }
            } catch (SQLException e) {
                if (attempt == retryCount - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000); // 等待1秒后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Thread interrupted while waiting to retry database connection", ie);
                }
            }
        }
        throw new SQLException("Unreachable code reached.");
    }

    private static void insertIntoTaskConfigResult(TaskDTO value) throws SQLException {
        String query = "INSERT INTO task_config_result (task_id, task_result, task_time) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.16.219:3306/new_algorithom_Repository", "root", "728af18d87824a28b45f39106fe9de0e");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, value.getTask_id());
            pstmt.setString(2, value.getTask_data());

            // 将UTC时间戳转换为上海时区的ZonedDateTime
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(value.getTask_time())
                    .atZone(ZoneId.of("Asia/Shanghai"));

            // 从ZonedDateTime直接获取LocalDateTime
            LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

            Timestamp timestamp = Timestamp.valueOf(localDateTime);

            pstmt.setTimestamp(3, timestamp);

            pstmt.executeUpdate();
        }
    }
}
