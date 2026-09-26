"""Explain effective project configuration and report static completeness."""

import os


def inspect_configuration(settings, store, project_id):
    current = store.project_spec(project_id)
    spec = current["spec"] if current else {}
    hosts = {item.get("id"): item for item in spec.get("hosts", [])}
    services = {item.get("type"): item for item in spec.get("services", [])}
    service_list = spec.get("services", [])
    service_types = {item.get("type") for item in service_list}
    nodes = spec.get("nodes", [])

    def service_endpoint(kind, fallback, default_protocol="http"):
        service = services.get(kind)
        host = hosts.get(service.get("host_id"), {}) if service else {}
        address = host.get("address")
        if address:
            protocol = service.get("protocol") or default_protocol
            port = service.get("port")
            value = "{}://{}{}".format(protocol, address, ":" + str(port) if port else "")
            if kind == "model":
                value += (service.get("config") or {}).get("health_path", "/health")
            return value, "project_spec"
        return fallback, "environment" if fallback else "missing"

    kafka_service = services.get("kafka")
    kafka_host = hosts.get(kafka_service.get("host_id"), {}) if kafka_service else {}
    if kafka_host.get("address"):
        kafka_servers = "{}:{}".format(kafka_host["address"], kafka_service.get("port") or 9092)
        kafka_source = "project_spec"
    else:
        kafka_servers = settings.kafka_servers
        kafka_source = "environment" if kafka_servers else "missing"

    flink_url, flink_source = service_endpoint("flink", settings.flink_url)
    model_url, model_source = service_endpoint("model", settings.model_url)
    topic_nodes = [item for item in nodes if item.get("type") == "kafka_topic"]
    job_node = next((item for item in nodes if item.get("type") == "flink_job"), None)
    input_topic = (topic_nodes[0].get("config") or {}).get("topic") if topic_nodes else ""
    output_topic = (topic_nodes[-1].get("config") or {}).get("topic") if len(topic_nodes) > 1 else ""
    job_config = (job_node or {}).get("config") or {}

    items = []

    def add(key, label, value, source, required, fix, category):
        items.append({
            "key": key, "label": label, "value": value or None, "source": source,
            "required": required, "status": "complete" if value else ("missing" if required else "optional"),
            "fix": fix, "category": category,
        })

    if "kafka" in service_types:
        add("kafka_servers", "Kafka Bootstrap Servers", kafka_servers, kafka_source, True,
            "在链路规格填写 Kafka 所属主机地址，或设置 KAFKA_SERVERS。", "运行连接")
    if "flink" in service_types:
        add("flink_url", "Flink REST URL", flink_url, flink_source, True,
            "在链路规格填写 Flink 所属主机地址，或设置 FLINK_REST_URL。", "运行连接")
    if "model" in service_types:
        add("model_health_url", "模型健康端点", model_url, model_source, True,
            "在链路规格填写模型主机和 health_path，或设置完整的 MODEL_HEALTH_URL。", "运行连接")
    for service in service_list:
        kind = service.get("type")
        if kind in {"api", "gateway", "airflow", "spark", "prometheus", "mysql", "postgresql", "redis", "clickhouse", "mongodb"}:
            host = hosts.get(service.get("host_id"), {})
            endpoint = "{}:{}".format(host.get("address"), service.get("port")) if host.get("address") and service.get("port") else None
            add("service_{}".format(service.get("id")), "{} 连接".format(service.get("name", kind)), endpoint,
                "project_spec" if endpoint else "missing", True,
                "在项目规格填写该服务的主机地址和端口。", "运行连接")
            if kind in {"api", "gateway", "airflow", "spark"}:
                health_path = (service.get("config") or {}).get("health_path")
                add("health_{}".format(service.get("id")), "{} 健康路径".format(service.get("name", kind)), health_path,
                    "project_spec" if health_path else "missing", True,
                    "在服务扩展配置填写 health_path。", "诊断能力")
            if kind == "prometheus":
                queries = (service.get("config") or {}).get("queries")
                add("prometheus_queries", "Prometheus 受控查询", len(queries) if isinstance(queries, dict) and queries else None,
                    "project_spec" if queries else "missing", True,
                    "在 Prometheus 服务扩展配置填写 queries。", "诊断能力")
    add("log_path", "组件日志目录", os.getenv("AGENT_LOG_PATH", ""),
        "environment" if os.getenv("AGENT_LOG_PATH") else "missing", False,
        "需要日志诊断时设置只读 AGENT_LOG_PATH。", "运行连接")
    if "kafka" in service_types:
        add("input_topic", "输入 Topic", input_topic, "project_spec" if input_topic else "missing", True,
            "在链路规格的输入 Kafka 节点填写 config.topic。", "链路标识")
        add("output_topic", "输出 Topic", output_topic, "project_spec" if output_topic else "missing", True,
            "在链路规格的输出 Kafka 节点填写 config.topic。", "链路标识")
    if "flink" in service_types:
        add("flink_job", "Flink Job 名称", job_config.get("job_name"), "project_spec" if job_config.get("job_name") else "missing", True,
            "在 Flink 作业节点填写 config.job_name。", "链路标识")
        add("consumer_group", "Kafka 消费组", job_config.get("consumer_group"), "project_spec" if job_config.get("consumer_group") else "missing", "kafka" in service_types,
            "在 Flink 作业节点填写 config.consumer_group。", "链路标识")
    add("nodes", "链路节点", len(nodes) if nodes else None, "project_spec" if nodes else "missing", True,
        "在链路规格中至少添加一个节点。", "拓扑结构")
    add("edges", "节点关系", len(spec.get("edges", [])) if spec.get("edges") else None,
        "project_spec" if spec.get("edges") else "missing", True,
        "在链路规格中添加节点之间的关系。", "拓扑结构")

    required = [item for item in items if item["required"]]
    complete = sum(item["status"] == "complete" for item in required)
    status = "complete" if complete == len(required) else "missing" if complete == 0 else "partial"
    return {
        "project_id": project_id,
        "spec_version": current.get("version") if current else None,
        "status": status,
        "score": round(complete / len(required) * 100) if required else 100,
        "complete_required": complete,
        "total_required": len(required),
        "items": items,
        "files": [
            {"name": "项目链路规格", "location": "SQLite project_spec_versions", "purpose": "主机、服务、Topic、Job、消费组、节点和关系", "edit": "网页 > 资源与链路 > 编辑规格"},
            {"name": "Agent 部署环境", "location": "compose.agent.yml / 进程环境变量", "purpose": "Kafka、Flink、模型健康和日志的运行连接", "edit": "修改环境变量后重启 agent-service"},
            {"name": "旧拓扑兜底", "location": "agent-service/topology.json", "purpose": "没有项目规格时的兼容拓扑", "edit": "当前项目已有版本化规格，不建议在这里修改"},
        ],
    }
