# 65.237 部署容量核查

2026-09-21 从开发机以只读 SSH 命令检查 `192.168.65.237`。这是当时的快照，不代表高峰负载。

| 项目 | 实测 |
|---|---|
| CPU | 4 vCPU，Xeon E5-2620 v4；检查时 load average 0.07/0.11/0.08 |
| 内存 | 5.7 GiB 总量，约 1.6 GiB available；5.5 GiB swap 基本未用 |
| 根分区 | 50 GiB，已用 42 GiB，剩余 8.7 GiB；Docker Root Dir 为 `/var/lib/docker` |
| `/home` | 独立 244 GiB 分区，剩余约 242 GiB |
| GPU | `lspci` 仅见 VMware SVGA II；无 `nvidia-smi` |
| 容器 | 24 个运行中，包括 MySQL、Nacos、Redis、MinIO、网关和多个算法模型 |
| 资源隔离 | 抽样的 MySQL、Nacos、网关容器未设置内存或 CPU 限额 |
| 目标端口 | 本机未监听 Kafka 9092–9094、Flink 8000/8081 和模型 8873 |

从 65.237 请求 `http://192.168.16.219:8000/overview` 超时；TCP 连接 `192.168.16.219:9092` 也超时。从开发机直连 16.219 的相应端口同样超时。路由表显示 65.237 会通过 `192.168.65.254` 前往 16.219；这只证明路由选择，不证明网络策略放行。需由网络/服务器管理员核查 ACL、防火墙、服务监听地址和 Kafka advertised listeners，不能仅凭超时判断 16.219 的进程已停止。

## 结论

当前不建议把 Kafka、Flink 和新的模型服务一并部署在 65.237。CPU 空闲不等于有内存余量；单机 Kafka broker、Flink JobManager/TaskManager 加上业务作业的常驻内存会超过当前约 1.6 GiB 的余量。Kafka 数据与容器镜像默认写入仅剩 8.7 GiB 的根分区，有填满业务主机的风险。没有 GPU，不适合在这台主机上提供本地大语言模型推理。

优先保留 Kafka/Flink 在 16.219，打通 65.237 到其 REST/broker 端口。65.237 可考虑部署轻量的 StreamDoctor API（单实例、SQLite 放 `/home`、限制内存），使用外部模型 API 和现有推理服务；正式部署前仍需测量业务高峰内存与磁盘写入。现有小型算法模型已在本机运行，新增 CPU 小模型须逐个压测并设置容器资源限额，不能只依据空闲时的 `docker stats` 判断容量。

如确需迁移 Kafka/Flink，应先增加物理内存和根分区空间，或将 Docker 数据根目录、Kafka 日志与 Flink 状态分别规划到容量充足的独立数据盘；完成备份、端口规划和业务高峰容量测试后，再在隔离环境演练。不要在现有主机上直接迁移生产 Topic 或关闭业务容器来腾空间。

## 只读连通性复核

在网络放通后，从部署 Agent 的主机确认 Flink REST `/overview`、`/jobs/overview`，模型 `GET /healthz`，以及 Kafka bootstrap 地址和返回的 broker 广播地址都可达。设置 `FLINK_REST_URL`、`KAFKA_SERVERS`、`MODEL_HEALTH_URL` 后，先运行 `python -m streamdoctor.probe`；四项观测均成功、Flink 作业名与 Topic/消费组核对无误，再开启 `AGENT_POLL_ENABLED=true`。探针只读，并使用临时 SQLite，不会向真实 Kafka 写入消息。
