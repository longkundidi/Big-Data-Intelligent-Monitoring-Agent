# flink-algorithm-sendData 项目说明

## 1. 项目定位

这是一个基于 Spring Boot 的数据回放/模拟发送项目。项目会读取资源文件中的样本数据，封装为 JSON 后发送到 Kafka 或 MQTT，用于下游算法服务或流式处理服务联调。

典型用途：
- 算法输入模拟
- 消息链路联调
- 端到端冒烟验证

## 2. 技术栈

- Java 8
- Spring Boot 2.6.13
- Maven
- Kafka Producer（通过 flink-connector-kafka 依赖）
- Eclipse Paho MQTT Client

核心构建文件：
- pom.xml

## 3. 目录结构

- src/main/java/com/algorithm/common
  - FlinkAlgorithmSendDataApplication.java：标准 Spring Boot 启动类
  - Application.java：启动 Spring 后再拉起发送线程
- src/main/java/com/algorithm/common/utils
  - KafkaUtilCMS.java
  - KafkaUtilGRU.java
  - KafkaUtilGRUAE.java
  - KafkaUtilLSTM.java
  - KafkaUtilLSTMAE.java
  - KafkaUtilResnet.java
  - MqttUtilGRU.java
  - MqttUtilGRUAE.java
  - MqttUtilLSTM.java
  - MqttUtilLSTMAE.java
- src/main/resources
  - application.properties
  - LSTM_GRU_GRUAE.csv
  - LSTMAE_pghk_11.csv
  - LSTMAE_pghk.csv
  - test_for_diagnose.txt
- src/test/java/com/algorithm/common
  - FlinkAlgorithmSendDataApplicationTests.java（仅做上下文加载测试）

## 4. 运行模型

### 4.1 通用处理流程

1. 从 application.properties 读取配置
2. 初始化 KafkaProducer 或 MqttClient
3. 读取源文件（按行或整文件）
4. 组装带时间戳和固定 id 的 JSON
5. 按固定间隔发送消息

### 4.2 启动类职责

- FlinkAlgorithmSendDataApplication.java
  - 仅启动 Spring 容器
- Application.java
  - 启动 Spring 容器
  - 同时启动两个发送线程：
    - KafkaUtilLSTMAE.writeToKafka()
    - KafkaUtilResnet.writeToKafka()

## 5. 各工具类职责

### 5.1 Kafka 发送类

- KafkaUtilGRU / KafkaUtilGRUAE / KafkaUtilLSTM
  - 输入：CSV（通常是 LSTM_GRU_GRUAE.csv）
  - 输出：含 dc_data、dc_time、id 的 JSON
  - 发送间隔：约 300 到 500 ms
- KafkaUtilLSTMAE
  - 输入：classpath 文件（LSTMAE_pghk_11.csv）
  - 逻辑：无限循环回放
  - 发送间隔：500 ms
- KafkaUtilResnet
  - 输入：test_for_diagnose.txt
  - 逻辑：将波形值拆分后封装为结构化 JSON
  - 特点：设置了更大的 Kafka max.request.size
  - 发送间隔：50 s（当前实现）
- KafkaUtilCMS
  - 输入：代码内写死的本地 txt 文件名（非配置项）
  - 逻辑：按空白分隔后发送

### 5.2 MQTT 发送类

- MqttUtilGRU / MqttUtilGRUAE / MqttUtilLSTM / MqttUtilLSTMAE
  - 从 classpath 读取文件
  - 构造 JSON 并发布到 MQTT 主题
  - 多数实现为 while(true) 无限发送
  - 部分主题键未在配置中声明时会使用代码默认值

## 6. 配置项映射

application.properties 当前主要配置：

- Kafka
  - kafka.servers
  - kafka.cms.topic
  - kafka.gru.topic
  - kafka.gruae.topic
  - kafka.lstm.topic
  - kafka.lstmae.topic
  - kafka.resnet.topic
- MQTT
  - mqtt.server
  - mqtt.lstmae.topic
  - 说明：mqtt.gru.topic、mqtt.gruae.topic、mqtt.lstm.topic 当前未显式声明，代码会走默认主题值
- 文件
  - file.gru.name
  - file.gruae.name
  - file.lstm.name
  - file.lstmae.name
  - file.resnet.name

## 7. 构建与运行

### 7.1 构建

```bash
mvn clean package
```

### 7.2 作为 Spring Boot 启动

- 启动 FlinkAlgorithmSendDataApplication：只启动应用
- 启动 Application：会自动启动 LSTMAE 与 Resnet 两个发送线程

### 7.3 单独运行某个发送器

每个工具类基本都有 main 方法，可以独立启动，例如：
- KafkaUtilLSTM.main
- MqttUtilLSTMAE.main

## 8. 消息体示例

### 8.1 LSTM/GRU 类消息

```json
{"dc_data": "0.123,0.456,...", "dc_time": 1710000000000, "id": 216}
```

### 8.2 Resnet 类消息

```json
{
  "farmName": "...",
  "turbineName": "...",
  "part": "...",
  "location": "...",
  "sampleRate": 51200,
  "waveLength": "16.384K",
  "dataType": "TIMEWAVE",
  "waveDefDescription": "TIMEWAVE",
  "acquisitionTime": 1710000000,
  "dataFloat": "[...]"
}
```

## 9. 当前风险与注意事项

1. 文件读取方式不统一
- 部分类使用 Files.lines(Paths.get(fileName))，依赖运行时工作目录。
- 部分类使用 ClassPathResource，打包和 IDE 启动更稳定。

2. 硬编码较多
- 部分 id 与业务字段写死在代码中。
- KafkaUtilCMS 的文件名写死在类中，未配置化。

3. 线程和退出控制
- 多个发送器使用 while(true) 无限循环，缺少优雅停机机制。

4. 配置完整性
- MQTT 相关主题配置未全部落在 application.properties 中，依赖代码默认值。

## 10. 建议的下一步改造

- 统一为 classpath 优先读取，支持外部路径覆盖
- 提炼公共发送模板，减少重复代码
- 将固定 id/元数据迁移到配置
- 增加优雅停机、结构化日志、错误重试
- 补充文件读取和消息发送的集成测试
