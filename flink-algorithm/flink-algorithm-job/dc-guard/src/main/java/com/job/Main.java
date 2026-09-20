package com.job;

import com.algorithm.common.model.DcDTO;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.job.utils.Initialization;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.algorithm.common.constant.InfluxDBPropertiesConstants.influxDBBucket;
import static com.algorithm.common.constant.InfluxDBPropertiesConstants.influxDBOrganization;
import static com.algorithm.common.constant.KafkaSinkPropertiesConstants.kafkaSinkBrokers;
import static com.algorithm.common.constant.PropertiesConstants.jobName;
import static com.job.utils.ExecutionEnvUtil.PARAMETER_TOOL;
//基于 Apache Flink 的流处理应用程序，涉及数据流处理、侧输出流、以及数据写入 Kafka 和 InfluxDB 的操作
public class Main {
    // 数据库连接池
    private static final HikariDataSource dataSource;
    // 缓存算法名和变量数，避免频繁查询数据库
    private static final Map<Long, String> algoCache = new ConcurrentHashMap<>();
    private static final Map<Long, Integer> variableNumCache = new ConcurrentHashMap<>();
    private static final Map<Long, TaskHierarchy> hierarchyCache = new ConcurrentHashMap<>();

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(PARAMETER_TOOL.getRequired("spring.datasource.url"));
        config.setUsername(PARAMETER_TOOL.getRequired("spring.datasource.username"));
        config.setPassword(PARAMETER_TOOL.getRequired("spring.datasource.password"));
        config.setDriverClassName(PARAMETER_TOOL.get("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        dataSource = new HikariDataSource(config);

        // JVM关闭时安全释放连接池
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataSource != null) {
                dataSource.close();
            }
        }));
    }
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(3);

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                10, // 尝试重启的次数
                org.apache.flink.api.common.time.Time.of(30, java.util.concurrent.TimeUnit.SECONDS) // 重启间隔
        ));

        final OutputTag<String> errorFormOutputTag = new OutputTag<String>("side-output-error-form") {};
        final OutputTag<String> errorVariableOutputTag = new OutputTag<String>("side-output-error-variable") {};

        DataStream<DcDTO> input = Initialization.initDataStream(env);

        SingleOutputStreamOperator<DcDTO> mainDataStream = input
                .process(new ProcessFunction<DcDTO, DcDTO>() {
                    @Override
                    public void processElement(DcDTO value, Context ctx, Collector<DcDTO> out) throws Exception {
                        if (!validateDcDTO(value)) {
                            // 格式不符合，通过侧输出流输出
                            ctx.output(errorFormOutputTag, value.getDc_data());
                        } else {
                            // 检查变量数是否匹配
                            int expectedVariableNum = getExpectedVariableNum(value.getId());
                            int actualVariableNum = value.getDc_data().split(",").length;
                            if (expectedVariableNum != actualVariableNum) {
                                String errorInfo = String.format("{\"error\":\"传入变量维度错误\",\"information\":\"要求维度为%d,但是为%d\",\"algo_shortname\":\"%s\",\"id\":%d,\"dc_time\": %d}", expectedVariableNum, actualVariableNum, getAlgorithmFromDB(value.getId()) ,value.getId(), value.getDc_time());
//                              变量数有误，通过侧输出流输出
                                ctx.output(errorVariableOutputTag, errorInfo);
                            } else {

                                TaskHierarchy hierarchy = getTaskHierarchy(value.getId());
                                Point point = Point.measurement("dc");

                                // Keep the requested hierarchy order when constructing line protocol.
                                hierarchy.addTags(point);
                                point.addTag("task_id", value.getId().toString());
                                point.addTag("id", value.getId().toString());
                                point.time(value.getDc_time(), WritePrecision.MS);

                                String[] fields = value.getDc_data().split(",");
                                for (int i = 0; i < fields.length; i++) {
                                    point.addField(getAlgorithmFromDB(value.getId()) + "_sensor_" + i, Double.parseDouble(fields[i]));
                                }

                                WriteApiBlocking writeApi = Initialization.getClient().getWriteApiBlocking();
                                // 历史硬编码（保留为注释，便于回溯）：
                                // writeApi.writePoint("dc", "bigdata", point);
                                writeApi.writePoint(influxDBBucket, influxDBOrganization, point);

                                out.collect(value);

                            }
                        }
                    }
                });

        //主输出流
        KafkaSink<DcDTO> kafkaSink = KafkaSink.<DcDTO>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector((TopicSelector<DcDTO>) value -> {
                            try {
                                return "dc_algorithm_" + getAlgorithmFromDB(value.getId());
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
                        .setTopic("dc_error_form")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        //侧输出流-变量数不匹配
        KafkaSink<String> errorVariableSink  = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaSinkBrokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector(value -> "dc_error_variable_" + extractIdFromErrorMessage((String) value))
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        mainDataStream.sinkTo(kafkaSink);
        mainDataStream.getSideOutput(errorFormOutputTag).sinkTo(errorFormSink);
        mainDataStream.getSideOutput(errorVariableOutputTag).sinkTo(errorVariableSink);

        env.execute(jobName);
    }

    private static boolean validateDcDTO(DcDTO dc) {
        // 校验信息
        if (dc.getDc_data() != null && dc.getDc_data().startsWith("{\"error\"")) {
            return false;
        }
        try {
            String[] parts = dc.getDc_data().split(",");
            for (String part : parts) {
                Double.parseDouble(part);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getAlgorithmFromDB(Long id) throws SQLException {
        if (algoCache.containsKey(id)) {
            return algoCache.get(id);
        }
        // 从数据库中取调用算法名algorithm，并加入数据库重连机制
        String query = "SELECT algo_shortname FROM config_perceived_task WHERE task_id = ?";
        int retryCount = 5;
        for (int attempt = 0; attempt < retryCount; attempt++) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setLong(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    String algo = rs.next() ? rs.getString("algo_shortname") : "NoIdInMysql";
                    algoCache.put(id, algo);
                    return algo;
                }
            } catch (SQLException e) {
                if (attempt == retryCount - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);  // 等待1秒后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Thread interrupted while waiting to retry database connection", ie);
                }
            }
        }
        return "defaultAlgorithm";
    }

    private static int getExpectedVariableNum(Long id) throws SQLException {
        // 优先从缓存获取
        if (variableNumCache.containsKey(id)) {
            return variableNumCache.get(id);
        }
        // 从数据库中取调用感知变量数variable_num，并加入数据库重连机制
        String query = "SELECT variable_num FROM config_perceived_task WHERE task_id = ?";
        int retryCount = 5;
        for (int attempt = 0; attempt < retryCount; attempt++) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setLong(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    int num = rs.next() ? rs.getInt("variable_num") : 0;
                    variableNumCache.put(id, num);
                    return num;
                }
            } catch (SQLException e) {
                if (attempt == retryCount - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Thread interrupted while waiting to retry", ie);
                }
            }
        }
        return 0;
    }

    private static TaskHierarchy getTaskHierarchy(Long taskId) throws SQLException {
        if (hierarchyCache.containsKey(taskId)) {
            return hierarchyCache.get(taskId);
        }

        String query = "SELECT "
                + "COALESCE(project.project, 'unknown') AS scene, "
                + "COALESCE(project.product_model, 'unknown') AS device_category, "
                + "COALESCE(root_node.node_name, task_node.turbine_code, 'unknown') AS elevator_instance, "
                + "COALESCE(task_node.turbine_code, 'unknown') AS elevator_code, "
                + "COALESCE(task_node.node_id, task.node_id, 'unknown') AS gbom_node_id, "
                + "COALESCE(task_node.node_code, 'unknown') AS gbom_node_code, "
                + "COALESCE(task_node.node_name, 'unknown') AS gbom_node_name, "
                + "COALESCE(task_node.node_level, 0) AS gbom_level, "
                + "COALESCE(GROUP_CONCAT(CASE WHEN ancestor.node_level > 1 THEN ancestor.node_name END "
                + "ORDER BY ancestor.node_level SEPARATOR '/'), '/') AS gbom_path "
                + "FROM config_perceived_task task "
                + "LEFT JOIN al_resume_data project ON project.id = CAST(task.pro_id AS UNSIGNED) "
                + "LEFT JOIN config_bom_tree task_node ON task_node.node_id = task.node_id "
                + "LEFT JOIN config_bom_tree root_node ON root_node.turbine_code = task_node.turbine_code "
                + "AND root_node.node_level = 1 "
                + "LEFT JOIN config_bom_tree ancestor ON ancestor.turbine_code = task_node.turbine_code "
                + "AND (ancestor.node_code = task_node.node_code "
                + "OR task_node.node_code LIKE CONCAT(ancestor.node_code, '-%')) "
                + "WHERE task.task_id = ? "
                + "GROUP BY project.project, project.product_model, root_node.node_name, task_node.turbine_code, "
                + "task_node.node_id, task.node_id, task_node.node_code, task_node.node_name, task_node.node_level";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setLong(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                TaskHierarchy hierarchy = rs.next()
                        ? new TaskHierarchy(
                                rs.getString("scene"),
                                rs.getString("device_category"),
                                rs.getString("elevator_instance"),
                                rs.getString("elevator_code"),
                                rs.getString("gbom_path"),
                                rs.getString("gbom_node_id"),
                                rs.getString("gbom_node_code"),
                                rs.getString("gbom_node_name"),
                                rs.getInt("gbom_level"))
                        : TaskHierarchy.empty();
                hierarchyCache.put(taskId, hierarchy);
                return hierarchy;
            }
        }
    }

    private static final class TaskHierarchy {
        private final String scene;
        private final String deviceCategory;
        private final String elevatorInstance;
        private final String elevatorCode;
        private final String gbomPath;
        private final String gbomNodeId;
        private final String gbomNodeCode;
        private final String gbomNodeName;
        private final int gbomLevel;

        private TaskHierarchy(String scene, String deviceCategory, String elevatorInstance, String elevatorCode,
                              String gbomPath, String gbomNodeId, String gbomNodeCode, String gbomNodeName,
                              int gbomLevel) {
            this.scene = scene;
            this.deviceCategory = deviceCategory;
            this.elevatorInstance = elevatorInstance;
            this.elevatorCode = elevatorCode;
            this.gbomPath = gbomPath;
            this.gbomNodeId = gbomNodeId;
            this.gbomNodeCode = gbomNodeCode;
            this.gbomNodeName = gbomNodeName;
            this.gbomLevel = gbomLevel;
        }

        private static TaskHierarchy empty() {
            return new TaskHierarchy("unknown", "unknown", "unknown", "unknown", "/", "unknown", "unknown",
                    "unknown", 0);
        }

        private void addTags(Point point) {
            point.addTag("scene", scene);
            point.addTag("device_category", deviceCategory);
            point.addTag("elevator_instance", elevatorInstance);
            point.addTag("gbom_path", gbomPath);
            point.addTag("gbom_node_id", gbomNodeId);
            point.addTag("gbom_node_code", gbomNodeCode);
            point.addTag("gbom_node_name", gbomNodeName);
            point.addTag("gbom_level", String.valueOf(gbomLevel));
            point.addTag("elevator_code", elevatorCode);
            point.addTag("asset_path", scene + "/" + deviceCategory + "/" + elevatorInstance + "/" + gbomPath);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String extractIdFromErrorMessage(String errorMessage) {
        // 从错误信息中提取id
        try {
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("id").asText("unknown");

        } catch (IOException e) {
            return "unknown";
        }
    }
}
