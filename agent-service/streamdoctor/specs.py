"""Versioned chain specification helpers and built-in project templates."""

import copy
import json
import re
from typing import Any


def blank_spec(name="空白链路"):
    return {
        "schema_version": 1,
        "name": name,
        "hosts": [],
        "services": [],
        "nodes": [],
        "edges": [],
        "diagnostics": {
            "tools": [], "metrics": [], "runbook_tags": [], "quick_questions": []
        },
    }


BUILTIN_TEMPLATES = [
    {
        "id": "blank",
        "name": "空白项目",
        "description": "从空白规格开始，手动添加服务器、服务、节点和关系。",
        "category": "基础",
        "spec": blank_spec(),
    },
    {
        "id": "kafka-lag",
        "name": "Kafka 消费积压诊断",
        "description": "生产者、Topic 与消费组链路，重点观察位点和分区积压。",
        "category": "Kafka",
        "spec": {
            **blank_spec("Kafka 消费积压链路"),
            "services": [{"id": "kafka", "name": "Kafka 集群", "type": "kafka", "host_id": None, "port": 9092, "protocol": "kafka", "version": "", "credential_ref": None, "config": {}}],
            "nodes": [
                {"id": "producer", "name": "数据生产者", "type": "source", "service_id": None, "config": {}},
                {"id": "input-topic", "name": "输入 Topic", "type": "kafka_topic", "service_id": "kafka", "config": {"topic": "input-topic"}},
                {"id": "consumer-group", "name": "消费组", "type": "kafka_consumer", "service_id": "kafka", "config": {"consumer_group": "consumer-group"}},
            ],
            "edges": [
                {"id": "e1", "source": "producer", "target": "input-topic", "type": "produce"},
                {"id": "e2", "source": "input-topic", "target": "consumer-group", "type": "consume"},
            ],
            "diagnostics": {"tools": ["get_kafka_offsets"], "metrics": ["lag", "latest_offset", "committed_offset"], "runbook_tags": ["kafka"], "quick_questions": ["为什么消费积压持续增加？"]},
        },
    },
    {
        "id": "kafka-flink",
        "name": "Kafka + Flink 实时计算",
        "description": "输入 Topic、Flink Job 与输出 Topic 的标准流处理链路。",
        "category": "Flink",
        "spec": {
            **blank_spec("Kafka + Flink 实时计算链路"),
            "services": [
                {"id": "kafka", "name": "Kafka 集群", "type": "kafka", "host_id": None, "port": 9092, "protocol": "kafka", "version": "", "credential_ref": None, "config": {}},
                {"id": "flink", "name": "Flink 集群", "type": "flink", "host_id": None, "port": 8081, "protocol": "http", "version": "1.17", "credential_ref": None, "config": {}},
            ],
            "nodes": [
                {"id": "input-topic", "name": "输入 Topic", "type": "kafka_topic", "service_id": "kafka", "config": {"topic": "input-topic"}},
                {"id": "flink-job", "name": "Flink 作业", "type": "flink_job", "service_id": "flink", "config": {"job_name": "stream-job", "consumer_group": "stream-job-group"}},
                {"id": "output-topic", "name": "输出 Topic", "type": "kafka_topic", "service_id": "kafka", "config": {"topic": "output-topic"}},
            ],
            "edges": [
                {"id": "e1", "source": "input-topic", "target": "flink-job", "type": "consume"},
                {"id": "e2", "source": "flink-job", "target": "output-topic", "type": "produce"},
            ],
            "diagnostics": {"tools": ["get_kafka_offsets", "get_flink_status", "get_flink_metrics"], "metrics": ["lag", "records_in", "records_out", "backpressure", "checkpoint"], "runbook_tags": ["kafka", "flink"], "quick_questions": ["为什么 Flink 不再产生输出？"]},
        },
    },
    {
        "id": "flink-model",
        "name": "Flink + 模型推理",
        "description": "Flink 同步调用 HTTP 模型服务，适合当前电梯诊断场景。",
        "category": "模型推理",
        "spec": {
            **blank_spec("Flink 模型推理链路"),
            "services": [
                {"id": "kafka", "name": "Kafka 集群", "type": "kafka", "host_id": "data-host", "port": 9092, "protocol": "kafka", "version": "", "credential_ref": None, "config": {}},
                {"id": "flink", "name": "Flink 集群", "type": "flink", "host_id": "data-host", "port": 8081, "protocol": "http", "version": "1.17", "credential_ref": None, "config": {}},
                {"id": "model", "name": "模型服务", "type": "model", "host_id": "model-host", "port": 8000, "protocol": "http", "version": "", "credential_ref": None, "config": {"health_path": "/healthz"}},
            ],
            "hosts": [
                {"id": "data-host", "name": "数据平台服务器", "environment": "demo", "address": ""},
                {"id": "model-host", "name": "模型服务器", "environment": "demo", "address": ""},
            ],
            "nodes": [
                {"id": "input-topic", "name": "输入 Kafka", "type": "kafka_topic", "service_id": "kafka", "config": {"topic": "dc_algorithm_REGTCN"}},
                {"id": "flink-job", "name": "REGTCN Flink 作业", "type": "flink_job", "service_id": "flink", "config": {"job_name": "algorithm_REGTCN", "consumer_group": "REGTCN_kafka_group"}},
                {"id": "model-api", "name": "REGTCN 模型接口", "type": "http_endpoint", "service_id": "model", "config": {"path": "/createTask/"}},
                {"id": "output-topic", "name": "输出 Kafka", "type": "kafka_topic", "service_id": "kafka", "config": {"topic": "dc_algorithm_sink_REGTCN"}},
            ],
            "edges": [
                {"id": "e1", "source": "input-topic", "target": "flink-job", "type": "consume"},
                {"id": "e2", "source": "flink-job", "target": "model-api", "type": "call"},
                {"id": "e3", "source": "model-api", "target": "flink-job", "type": "response"},
                {"id": "e4", "source": "flink-job", "target": "output-topic", "type": "produce"},
            ],
            "diagnostics": {"tools": ["get_kafka_offsets", "get_flink_status", "get_flink_metrics", "get_model_health"], "metrics": ["lag", "backpressure", "model_latency", "model_errors"], "runbook_tags": ["kafka", "flink", "model"], "quick_questions": ["为什么模型变慢后链路出现积压？"]},
        },
    },
    {
        "id": "flink-storage",
        "name": "Flink + 数据库存储",
        "description": "Flink 处理结果写入 MySQL 或 Redis，关注下游写入和连接状态。",
        "category": "结果存储",
        "spec": {
            **blank_spec("Flink 结果入库链路"),
            "services": [
                {"id": "kafka", "name": "Kafka 集群", "type": "kafka", "host_id": None, "port": 9092, "protocol": "kafka", "version": "", "credential_ref": None, "config": {}},
                {"id": "flink", "name": "Flink 集群", "type": "flink", "host_id": None, "port": 8081, "protocol": "http", "version": "1.17", "credential_ref": None, "config": {}},
                {"id": "storage", "name": "结果数据库", "type": "mysql", "host_id": None, "port": 3306, "protocol": "mysql", "version": "", "credential_ref": None, "config": {"database": "", "table": "results"}},
            ],
            "nodes": [
                {"id": "input-topic", "name": "输入 Topic", "type": "kafka_topic", "service_id": "kafka", "config": {"topic": "input-topic"}},
                {"id": "flink-job", "name": "Flink 作业", "type": "flink_job", "service_id": "flink", "config": {"job_name": "stream-job"}},
                {"id": "result-table", "name": "结果表", "type": "database_table", "service_id": "storage", "config": {"table": "results"}},
            ],
            "edges": [
                {"id": "e1", "source": "input-topic", "target": "flink-job", "type": "consume"},
                {"id": "e2", "source": "flink-job", "target": "result-table", "type": "write"},
            ],
            "diagnostics": {"tools": ["get_kafka_offsets", "get_flink_status", "get_flink_metrics", "get_business_summary"], "metrics": ["lag", "records_out", "sink_errors", "latest_result_time"], "runbook_tags": ["kafka", "flink", "sink"], "quick_questions": ["为什么输出 Topic 有数据但结果表没有更新？"]},
        },
    },
    {
        "id": "microservice-observability",
        "name": "微服务 + Prometheus",
        "description": "网关、API、数据库与 Prometheus 的服务链路，定位可用性、延迟、错误率和资源饱和。",
        "category": "微服务",
        "spec": {
            **blank_spec("微服务可观测链路"),
            "hosts": [{"id": "app-host", "name": "应用服务器", "environment": "demo", "address": ""},
                      {"id": "db-host", "name": "数据库服务器", "environment": "demo", "address": ""}],
            "services": [
                {"id": "gateway", "name": "API 网关", "type": "gateway", "host_id": "app-host", "port": 8080, "protocol": "http", "version": "", "credential_ref": None, "config": {"health_path": "/health", "latency_threshold_ms": 500}},
                {"id": "api", "name": "业务 API", "type": "api", "host_id": "app-host", "port": 8088, "protocol": "http", "version": "", "credential_ref": None, "config": {"health_path": "/health", "latency_threshold_ms": 800}},
                {"id": "db", "name": "PostgreSQL", "type": "postgresql", "host_id": "db-host", "port": 5432, "protocol": "postgresql", "version": "", "credential_ref": None, "config": {}},
                {"id": "prometheus", "name": "Prometheus", "type": "prometheus", "host_id": "app-host", "port": 9090, "protocol": "http", "version": "", "credential_ref": None, "config": {"queries": {
                    "service.error_rate": {"query": "sum(rate(http_requests_total{status=~'5..'}[5m])) / sum(rate(http_requests_total[5m]))", "threshold": 0.05, "unit": "ratio", "component": "api"},
                    "service.latency_p95_ms": {"query": "histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket[5m])) by (le)) * 1000", "threshold": 800, "unit": "ms", "component": "api"},
                    "resource.cpu_utilization": {"query": "max(rate(process_cpu_seconds_total[5m]))", "threshold": 0.9, "unit": "ratio", "component": "api"}}}},
            ],
            "nodes": [{"id": "client", "name": "客户端", "type": "source", "service_id": None, "config": {}},
                      {"id": "gateway-node", "name": "API 网关", "type": "http_endpoint", "service_id": "gateway", "config": {}},
                      {"id": "api-node", "name": "业务 API", "type": "http_endpoint", "service_id": "api", "config": {}},
                      {"id": "db-node", "name": "业务数据库", "type": "database", "service_id": "db", "config": {}}],
            "edges": [{"id": "e1", "source": "client", "target": "gateway-node", "type": "call"},
                      {"id": "e2", "source": "gateway-node", "target": "api-node", "type": "call"},
                      {"id": "e3", "source": "api-node", "target": "db-node", "type": "query"}],
            "diagnostics": {"tools": ["get_service_health", "get_dependency_health", "get_prometheus_metrics"], "metrics": ["availability", "latency", "error_rate", "cpu"], "runbook_tags": ["http", "database"], "quick_questions": ["为什么接口变慢或出现 5xx？"]},
        },
    },
    {
        "id": "batch-etl",
        "name": "Airflow + Spark 批处理 ETL",
        "description": "调度、批处理与数据仓库链路，定位任务失败、运行超时、资源瓶颈和数据新鲜度问题。",
        "category": "批处理",
        "spec": {
            **blank_spec("批处理 ETL 链路"),
            "hosts": [{"id": "batch-host", "name": "计算服务器", "environment": "demo", "address": ""},
                      {"id": "warehouse-host", "name": "数仓服务器", "environment": "demo", "address": ""}],
            "services": [
                {"id": "airflow", "name": "Airflow", "type": "airflow", "host_id": "batch-host", "port": 8080, "protocol": "http", "version": "", "credential_ref": None, "config": {"health_path": "/health", "latency_threshold_ms": 1000}},
                {"id": "spark", "name": "Spark History Server", "type": "spark", "host_id": "batch-host", "port": 18080, "protocol": "http", "version": "", "credential_ref": None, "config": {"health_path": "/api/v1/applications", "latency_threshold_ms": 1500}},
                {"id": "warehouse", "name": "ClickHouse 数仓", "type": "clickhouse", "host_id": "warehouse-host", "port": 9000, "protocol": "clickhouse", "version": "", "credential_ref": None, "config": {}},
            ],
            "nodes": [{"id": "source", "name": "业务数据源", "type": "source", "service_id": None, "config": {}},
                      {"id": "dag", "name": "Airflow DAG", "type": "scheduler_job", "service_id": "airflow", "config": {"job_name": "daily_etl"}},
                      {"id": "spark-job", "name": "Spark 作业", "type": "batch_job", "service_id": "spark", "config": {}},
                      {"id": "warehouse-table", "name": "数仓结果表", "type": "database_table", "service_id": "warehouse", "config": {}}],
            "edges": [{"id": "e1", "source": "source", "target": "dag", "type": "schedule"},
                      {"id": "e2", "source": "dag", "target": "spark-job", "type": "submit"},
                      {"id": "e3", "source": "spark-job", "target": "warehouse-table", "type": "write"}],
            "diagnostics": {"tools": ["get_service_health", "get_dependency_health", "get_prometheus_metrics"], "metrics": ["job_status", "duration", "data_freshness", "resource"], "runbook_tags": ["scheduler", "spark", "storage"], "quick_questions": ["为什么今天的 ETL 没有按时产出？"]},
        },
    },
    {
        "id": "generic-service-chain",
        "name": "通用服务依赖链",
        "description": "适用于任意 HTTP 服务、缓存和数据库的依赖拓扑，通过受控健康检查和指标查询诊断。",
        "category": "通用",
        "spec": {
            **blank_spec("通用服务依赖链"),
            "hosts": [{"id": "service-host", "name": "服务主机", "environment": "demo", "address": ""}],
            "services": [{"id": "service", "name": "应用服务", "type": "api", "host_id": "service-host", "port": 8080, "protocol": "http", "version": "", "credential_ref": None, "config": {"health_path": "/health", "latency_threshold_ms": 800}},
                         {"id": "cache", "name": "Redis", "type": "redis", "host_id": "service-host", "port": 6379, "protocol": "redis", "version": "", "credential_ref": None, "config": {}}],
            "nodes": [{"id": "request", "name": "请求入口", "type": "source", "service_id": None, "config": {}},
                      {"id": "service-node", "name": "应用服务", "type": "http_endpoint", "service_id": "service", "config": {}},
                      {"id": "cache-node", "name": "缓存", "type": "cache", "service_id": "cache", "config": {}}],
            "edges": [{"id": "e1", "source": "request", "target": "service-node", "type": "call"},
                      {"id": "e2", "source": "service-node", "target": "cache-node", "type": "read_write"}],
            "diagnostics": {"tools": ["get_service_health", "get_dependency_health"], "metrics": ["availability", "latency"], "runbook_tags": ["http", "redis"], "quick_questions": ["哪个依赖导致服务不可用？"]},
        },
    },
]


def normalize_spec(spec: dict[str, Any]) -> dict[str, Any]:
    value = copy.deepcopy(spec or {})
    value.setdefault("schema_version", 1)
    value.setdefault("name", "未命名链路")
    for key in ("hosts", "services", "nodes", "edges"):
        value.setdefault(key, [])
        if not isinstance(value[key], list):
            raise ValueError(key + " 必须是数组")
    value.setdefault("diagnostics", {})
    for key in ("tools", "metrics", "runbook_tags", "quick_questions"):
        value["diagnostics"].setdefault(key, [])
    _validate_ids(value)
    return value


def _validate_ids(spec):
    for collection in ("hosts", "services", "nodes", "edges"):
        ids = [item.get("id") for item in spec[collection]]
        if any(not item for item in ids):
            raise ValueError(collection + " 中每一项都必须有 id")
        if len(ids) != len(set(ids)):
            raise ValueError(collection + " 中存在重复 id")
    host_ids = {item["id"] for item in spec["hosts"]}
    service_ids = {item["id"] for item in spec["services"]}
    node_ids = {item["id"] for item in spec["nodes"]}
    for service in spec["services"]:
        if service.get("host_id") and service["host_id"] not in host_ids:
            raise ValueError("服务 {} 引用了不存在的服务器".format(service["id"]))
    for node in spec["nodes"]:
        if node.get("service_id") and node["service_id"] not in service_ids:
            raise ValueError("节点 {} 引用了不存在的服务".format(node["id"]))
    for edge in spec["edges"]:
        if edge.get("source") not in node_ids or edge.get("target") not in node_ids:
            raise ValueError("关系 {} 引用了不存在的节点".format(edge["id"]))


def spec_topology(spec: dict[str, Any], project_id=None):
    value = normalize_spec(spec)
    return {
        "id": project_id or value.get("name", "chain"),
        "project_id": project_id,
        "name": value.get("name"),
        "nodes": [
            {"id": item["id"], "label": item.get("name", item["id"]), "type": item.get("type", "custom"),
             "service_id": item.get("service_id"), **(item.get("config") or {})}
            for item in value["nodes"]
        ],
        "edges": copy.deepcopy(value["edges"]),
    }


def diff_specs(old: dict[str, Any] | None, new: dict[str, Any]):
    changes = []

    def walk(before, after, path=""):
        if isinstance(before, dict) and isinstance(after, dict):
            for key in sorted(set(before) | set(after)):
                walk(before.get(key), after.get(key), path + "/" + key)
        elif isinstance(before, list) and isinstance(after, list):
            before_by_id = {item.get("id"): item for item in before if isinstance(item, dict) and item.get("id")}
            after_by_id = {item.get("id"): item for item in after if isinstance(item, dict) and item.get("id")}
            if len(before_by_id) == len(before) and len(after_by_id) == len(after):
                for item_id in sorted(set(before_by_id) | set(after_by_id)):
                    walk(before_by_id.get(item_id), after_by_id.get(item_id), path + "/" + item_id)
            elif before != after:
                changes.append({"path": path or "/", "change_type": "changed", "old_value": before, "new_value": after})
        elif before != after:
            kind = "added" if before is None else "removed" if after is None else "changed"
            changes.append({"path": path or "/", "change_type": kind, "old_value": before, "new_value": after})

    walk(old or {}, new)
    return changes


def infer_spec_from_text(content: str, base_spec: dict[str, Any] | None = None):
    """Conservatively extract addresses and common identifiers into a draft spec."""
    spec = normalize_spec(base_spec or blank_spec("文档导入链路"))
    suggestions = []
    ips = list(dict.fromkeys(re.findall(r"(?<![\d.])(?:\d{1,3}\.){3}\d{1,3}(?![\d.])", content)))
    for index, address in enumerate(ips[:10], start=1):
        host_id = "document-host-{}".format(index)
        if not any(item.get("address") == address for item in spec["hosts"]):
            spec["hosts"].append({"id": host_id, "name": "文档服务器 {}".format(index), "environment": "unknown", "address": address})
            suggestions.append({"field": "/hosts/{}/address".format(host_id), "value": address, "source": "document"})
    patterns = {
        "topic": r"(?:topic|Topic|主题)[：:=\s]+([\w.-]+)",
        "job_name": r"(?:job|Job|作业)[：:=\s]+([\w.-]+)",
        "consumer_group": r"(?:group|消费组)[：:=\s]+([\w.-]+)",
    }
    extracted = {}
    for key, pattern in patterns.items():
        values = list(dict.fromkeys(re.findall(pattern, content)))
        if values:
            extracted[key] = values
            suggestions.append({"field": "/detected/" + key, "value": values, "source": "document"})
    return {"spec": spec, "suggestions": suggestions, "detected": extracted,
            "missing": [key for key in ("hosts", "services", "nodes", "edges") if not spec[key]]}


def spec_json(spec):
    return json.dumps(normalize_spec(spec), ensure_ascii=False, separators=(",", ":"))
