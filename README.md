# Big Data Intelligent Monitoring Agent

StreamDoctor 的运行诊断工作台位于 Vue 的 `/#/monitor/streamdoctor`。新增的 Python Agent 服务、环境配置、隔离回放和测试说明见 [agent-service/README.md](agent-service/README.md)。它在现有 Kafka/Flink 电梯链路上增加可追溯的证据采集与诊断。

独立的 Agent 工作台位于 `agent-web`，与原业务 Vue 分开运行，提供项目、持续会话、工具执行时间线、证据和可视化面板。它通过 Python FastAPI 的 `/api/agent` 接口工作，不依赖业务前端是否在线。

## 当前演示环境

当前 ECS 使用 `compose.infrastructure.yml` 运行 Kafka 3.9.1、Flink 1.17.2（1 个 TaskManager、2 个 Slot），使用 `compose.agent.yml` 运行 Agent，并使用 `compose.stateful.yml` 运行从 65.237 迁移的 MySQL、Redis、Nacos 和 MinIO。管理端口只绑定服务器回环地址，通过 SSH 隧道查看：

```powershell
ssh -N -L 4173:127.0.0.1:4173 -L 8081:127.0.0.1:8081 -L 8099:127.0.0.1:8099 root@8.160.166.210
```

保持隧道窗口运行后：

- `http://127.0.0.1:8081`：Flink 原生 Web UI。
- `http://127.0.0.1:8099/docs`：Agent API 文档。
- `http://127.0.0.1:4173`：独立 Agent 工作台，无需启动原业务 Java/Vue。

工作台当前没有单独登录页，通过 SSH 隧道后直接打开即可。需要查看 Nacos、MinIO 或数据库时，再按需增加对应端口转发，不要在阿里云安全组中直接开放管理端口。

### 当前状态（2026-09-26 实测）

- Kafka、Flink、Agent API、Agent Web、REGTCN 演示模型、MySQL、Redis、Nacos 和 MinIO 容器均在运行。
- Flink 作业 `algorithm_REGTCN` 为 `RUNNING`，1/1 Task 正常，输入和输出 Topic 均已创建。
- Agent 配置完整度为 9/9，Flink、Kafka 和模型健康探针均可用。
- 当前链路没有新输入，模型最近五分钟采样数为 0；图表不变化表示链路空闲，不等于服务故障。
- 对话大模型尚未配置 `AGENT_MODEL_API_KEY`，因此聊天运行会保留规则诊断结果并标明降级。REGTCN 演示模型只负责链路中的业务推理，不能替代对话大模型。

### ECS 重启后的启动方式

登录服务器后执行：

```bash
cd /opt/streamdoctor
docker compose -f compose.stateful.yml up -d
docker compose -f compose.infrastructure.yml up -d
docker compose -f compose.agent.yml up -d
docker compose -f compose.agent-web.yml up -d
docker exec streamdoctor-agent python -m streamdoctor.probe
```

`streamdoctor-kafka-init` 和 `streamdoctor-flink-job-submit` 是一次性初始化容器，执行成功后显示 `Exited (0)` 属于正常状态。最后一条探针应显示 Flink、Kafka、模型服务和配置检查均为 `ok`。

修改代码后只重建对应服务：

```bash
cd /opt/streamdoctor
docker compose -f compose.agent.yml up -d --build agent
docker compose -f compose.agent-web.yml up -d --build agent-web
```

查看运行状态与日志：

```bash
docker ps --filter name=streamdoctor
docker logs --tail 100 streamdoctor-agent
docker logs --tail 100 streamdoctor-agent-web
```

### 配置对话大模型

在服务器 `/opt/streamdoctor/.env` 中保存服务端配置，API Key 不要写进 Compose 文件或提交到 Git：

```dotenv
AGENT_MODEL_API_KEY=填写实际密钥
AGENT_MODEL_BASE_URL=填写兼容Responses-API的地址
AGENT_MODEL_NAME=gpt-6-astra
AGENT_REASONING_EFFORT=high
```

配置后执行：

```bash
cd /opt/streamdoctor
chmod 600 .env
docker compose -f compose.agent.yml up -d --force-recreate agent
curl -s http://127.0.0.1:8099/api/agent/models
```

返回结果中的 `configured` 必须为 `true`。转发服务必须实现 `/v1/responses`；只有 Chat Completions 接口的转发地址不能直接用于当前 Agent Runtime。

## REGTCN 演示链路

`compose.infrastructure.yml` 会创建输入/输出 Topic，启动协议兼容的轻量模型，并幂等提交 `algorithm_REGTCN` Flink 作业：

```bash
docker compose -f compose.infrastructure.yml build regtcn-model flink-job-submit
docker compose -f compose.infrastructure.yml up -d kafka-init regtcn-model jobmanager taskmanager flink-job-submit
docker compose -f compose.agent.yml up -d --build agent
docker compose -f compose.agent-web.yml up -d --build agent-web
docker exec streamdoctor-agent python -m streamdoctor.probe
```

模型服务提供 `POST /createTask/` 和 `GET /healthz`。默认延迟为 20ms；可通过 `MODEL_DELAY_MS`、`MODEL_ERROR_RATE` 和 `MODEL_ANOMALY_THRESHOLD` 调整演示行为。修改后重新创建模型容器即可注入慢响应或错误：

```bash
MODEL_DELAY_MS=1500 docker compose -f compose.infrastructure.yml up -d --force-recreate regtcn-model
```

模型端口 `8873`、Flink `8081`、Agent `8099` 和工作台 `4173` 都只绑定服务器回环地址，不应直接开放公网安全组。

MySQL 初始化备份仅在 `mysql-data` 卷第一次创建时导入；不要为了重复启动删除该卷。Redis 使用 RDB 导入并启用 AOF，MinIO 对象保存在 `/opt/streamdoctor/state/minio`。服务器安全组无需开放这些管理端口。

当前链路配置为输入 Topic `dc_algorithm_REGTCN`、输出 Topic `dc_algorithm_sink_REGTCN`、消费组 `REGTCN_kafka_group`、目标作业 `algorithm_REGTCN`。工作台会区分实时、历史、模拟、过期、失败和观测缺失状态。

The repository contains two independent Java services and a separate Vue frontend.

| Directory | Maven root | Application class | Default port |
| --- | --- | --- | --- |
| `java` | `java/pom.xml` | `sw.ModelApp` (`model3d-biz`) | 7200 |
| `ajava` | `ajava/pom.xml` | `com.algorithm.web.StartApplication` (`flink-algorithm-web`) | 8180 |
| `vue` | Not part of either Maven build | Vue application | See `vue/package.json` |

Import both Maven roots into IntelliJ IDEA from this repository. The shared run configurations in `.run` select each service module and its own working directory. Use Java 8 and a Maven installation that can resolve the `com.pig4cloud:pig:3.6.6` parent and project dependencies.

Build each service independently:

```powershell
mvn -f java/pom.xml -DskipTests compile
mvn -f ajava/pom.xml -DskipTests compile
```

Both Maven roots set `file.encoding=UTF-8` in `.mvn/jvm.config`. After switching from an older GBK-based build, run `clean package` once to replace the compiler status files under `target`.

Both services depend on external Nacos configuration and other infrastructure. For local runs, set `NACOS_HOST` and `NACOS_PORT` when `pig-register:8848` is not reachable, and review the datasource, InfluxDB, MinIO, and Flink addresses for the target environment. The two services have distinct default HTTP ports, but they may still connect to the same external systems.

The IDE run configurations also set `-Dfile.encoding=UTF-8`. Maven JVM settings only affect builds; runtime needs UTF-8 separately when Nacos YAML contains Chinese text. Nacos configuration/discovery and the configured database must be reachable for the applications to start.
