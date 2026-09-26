import json
import math
import os
import re
import socket
import time
from datetime import datetime, timedelta, timezone
from pathlib import Path

import httpx
from kafka import KafkaConsumer, TopicPartition


TOOLS = ("topology", "flink_status", "flink_metrics", "kafka_offsets", "model_health", "logs", "runbook",
         "service_health", "dependency_health", "prometheus_metrics")
LEGACY_TOOLS = TOOLS[:7]


class Collectors:
    def __init__(self, settings, store, replay=None, project_id=None):
        self.settings = settings
        self.store = store
        self.replay = replay
        self.project_id = project_id
        current = store.project_spec(project_id) if project_id else None
        self.spec = current["spec"] if current else None
        self.chain = self._chain_from_spec(self.spec) if self.spec else settings.topology()

    def _chain_from_spec(self, spec):
        topics = [item for item in spec.get("nodes", []) if item.get("type") == "kafka_topic"]
        jobs = [item for item in spec.get("nodes", []) if item.get("type") == "flink_job"]
        input_topic = topics[0].get("config", {}).get("topic", "") if topics else ""
        output_topic = topics[-1].get("config", {}).get("topic", "") if len(topics) > 1 else input_topic
        job = jobs[0] if jobs else {"config": {}}
        return {"id": self.project_id or "project", "flink_job_name": job.get("config", {}).get("job_name", ""),
                "consumer_group": job.get("config", {}).get("consumer_group", ""),
                "input_topic": input_topic, "output_topic": output_topic}

    def _service_url(self, service_type, fallback, default_protocol="http"):
        if not self.spec:
            return fallback
        hosts = {item["id"]: item for item in self.spec.get("hosts", [])}
        service = next((item for item in self.spec.get("services", []) if item.get("type") == service_type), None)
        if not service:
            return fallback
        host = hosts.get(service.get("host_id"), {})
        address = host.get("address")
        if not address:
            return fallback
        protocol = service.get("protocol") or default_protocol
        port = service.get("port")
        return "{}://{}{}".format(protocol, address, ":" + str(port) if port else "")

    def _kafka_servers(self):
        if not self.spec:
            return self.settings.kafka_servers
        hosts = {item["id"]: item for item in self.spec.get("hosts", [])}
        service = next((item for item in self.spec.get("services", []) if item.get("type") == "kafka"), None)
        if not service:
            return self.settings.kafka_servers
        host = hosts.get(service.get("host_id"), {})
        if not host.get("address"):
            return self.settings.kafka_servers
        return "{}:{}".format(host["address"], service.get("port") or 9092)

    def collect(self, name, question=""):
        if name not in TOOLS:
            raise ValueError("Unknown read-only tool")
        end = datetime.now(timezone.utc)
        start = end - timedelta(minutes=5)
        if self.replay is not None and name != "runbook":
            snapshot = self.replay.get(name)
            if snapshot is None:
                return "unavailable", {"error": "回放没有该项观测数据"}, start.isoformat(), end.isoformat()
            return snapshot["status"], snapshot["payload"], start.isoformat(), end.isoformat()
        try:
            result = getattr(self, name)(question)
            return "ok", result, start.isoformat(), end.isoformat()
        except Exception as exc:
            return "unavailable", {"error": str(exc)[:250]}, start.isoformat(), end.isoformat()

    def topology(self, question):
        return self.chain

    def _flink(self, path):
        flink_url = self._service_url("flink", self.settings.flink_url)
        if not flink_url:
            raise RuntimeError("未配置 FLINK_REST_URL")
        with httpx.Client(timeout=4) as client:
            response = client.get(flink_url.rstrip("/") + path)
            response.raise_for_status()
            return response.json()

    def _job(self):
        jobs = self._flink("/jobs/overview").get("jobs", [])
        matches = [job for job in jobs if job.get("name") == self.chain["flink_job_name"]]
        if not matches:
            return None
        return sorted(matches, key=lambda item: item.get("start-time", 0), reverse=True)[0]

    def flink_status(self, question):
        cluster = self._flink("/overview")
        jobs = self._flink("/jobs/overview").get("jobs", [])
        matches = [item for item in jobs if item.get("name") == self.chain["flink_job_name"]]
        job = sorted(matches, key=lambda item: item.get("start-time", 0), reverse=True)[0] if matches else None
        cluster_summary = {
            "version": cluster.get("flink-version"),
            "taskmanagers": cluster.get("taskmanagers"),
            "slots_total": cluster.get("slots-total"),
            "slots_available": cluster.get("slots-available"),
            "jobs_running": cluster.get("jobs-running"),
            "jobs_failed": cluster.get("jobs-failed"),
            "jobs_finished": cluster.get("jobs-finished"),
        }
        if not job:
            return {"state": "NOT_FOUND", "job_name": self.chain["flink_job_name"],
                    "cluster": cluster_summary}
        job_id = job["jid"]
        result = {"job_id": job_id, "job_name": job.get("name"), "state": job.get("state"),
                  "start_time": job.get("start-time"), "end_time": job.get("end-time"),
                  "duration_ms": job.get("duration"), "cluster": cluster_summary}
        for key, path in (("exceptions", "/jobs/{}/exceptions"), ("checkpoints", "/jobs/{}/checkpoints")):
            try:
                raw = self._flink(path.format(job_id))
                if key == "exceptions":
                    result[key] = [{"exceptionName": e.get("exceptionName"), "timestamp": e.get("timestamp"),
                                     "stacktrace": e.get("stacktrace", "")[:1200]} for e in raw.get("exceptionHistory", {}).get("entries", [])[:5]]
                    result["exception_summary"] = {
                        "count": len(raw.get("exceptionHistory", {}).get("entries", [])),
                        "latest_timestamp": result[key][0].get("timestamp") if result[key] else None,
                        "truncated": bool(raw.get("exceptionHistory", {}).get("truncated")),
                    }
                else:
                    counts = raw.get("counts", {})
                    latest = raw.get("latest", {}).get("completed") or raw.get("latest", {}).get("failed")
                    result[key] = counts
                    result["checkpoint_status"] = "disabled" if not counts.get("total") else "enabled"
                    result["checkpoint_summary"] = {
                        "latest_id": (latest or {}).get("id"),
                        "latest_status": (latest or {}).get("status"),
                        "latest_duration_ms": (latest or {}).get("end_to_end_duration"),
                        "latest_size_bytes": (latest or {}).get("checkpointed_size"),
                        "completed": counts.get("completed", 0),
                        "failed": counts.get("failed", 0),
                    }
            except Exception as exc:
                result[key + "_error"] = str(exc)[:150]
        return result

    def flink_metrics(self, question):
        job = self._job()
        if not job:
            return {"state": "NOT_FOUND", "vertices": []}
        job_id = job["jid"]
        details = self._flink("/jobs/{}".format(job_id))
        vertices = []
        metric_names = ("numRecordsInPerSecond,numRecordsOutPerSecond,numRecordsIn,numRecordsOut,"
                        "busyTimeMsPerSecond,backPressuredTimeMsPerSecond,idleTimeMsPerSecond")
        for vertex in details.get("vertices", [])[:30]:
            item = {"name": vertex.get("name"), "id": vertex.get("id"), "status": vertex.get("status")}
            try:
                raw = self._flink("/jobs/{}/vertices/{}/metrics?get={}".format(job_id, vertex["id"], metric_names))
                item["metrics"] = {entry["id"]: entry["value"] for entry in raw}
            except Exception as exc:
                item["metrics_error"] = str(exc)[:120]
            vertices.append(item)
        def values(name):
            result = []
            for vertex in vertices:
                try:
                    result.append(float(vertex.get("metrics", {}).get(name)))
                except (TypeError, ValueError):
                    pass
            return result

        input_rates = values("numRecordsInPerSecond")
        output_rates = values("numRecordsOutPerSecond")
        backpressure = values("backPressuredTimeMsPerSecond")
        busy = values("busyTimeMsPerSecond")
        metric_errors = sum("metrics_error" in vertex for vertex in vertices)
        return {"job_id": job_id, "vertices": vertices, "metric_errors": metric_errors,
                "summary": {
                    "vertex_count": len(vertices),
                    "observed_vertices": len(vertices) - metric_errors,
                    "input_rate_total": sum(input_rates) if input_rates else None,
                    "output_rate_total": sum(output_rates) if output_rates else None,
                    "max_backpressure_ms_per_second": max(backpressure) if backpressure else None,
                    "max_busy_ms_per_second": max(busy) if busy else None,
                    "backpressured_vertices": sum(value > 500 for value in backpressure),
                }}

    def kafka_offsets(self, question):
        kafka_servers = self._kafka_servers()
        if not kafka_servers:
            raise RuntimeError("未配置 KAFKA_SERVERS")
        servers = [host.strip() for host in kafka_servers.split(",") if host.strip()]
        consumer = KafkaConsumer(bootstrap_servers=servers, group_id=self.chain["consumer_group"],
                                 request_timeout_ms=8000, session_timeout_ms=6000,
                                 api_version_auto_timeout_ms=4000,
                                 consumer_timeout_ms=1000, enable_auto_commit=False)
        try:
            topic = self.chain["input_topic"]
            partitions = consumer.partitions_for_topic(topic)
            if partitions is None:
                raise RuntimeError("Topic 不存在或 Kafka 元数据不可用: " + topic)
            tps = [TopicPartition(topic, index) for index in sorted(partitions)]
            consumer.assign(tps)
            end_offsets = consumer.end_offsets(tps)
            rows = []
            for tp in tps:
                committed = consumer.committed(tp)
                latest = end_offsets[tp]
                rows.append({"partition": tp.partition, "latest": latest, "committed": committed,
                             "lag": max(0, latest - committed) if committed is not None else None})
            history = self.store.samples(self.chain["id"], "kafka_offsets", 1)
            previous_payload = history[-1]["payload"] if history and history[-1]["status"] == "ok" else {}
            previous = previous_payload.get("total_lag")
            topics = {}
            for topic_name in (self.chain["input_topic"], self.chain["output_topic"]):
                topic_partitions = consumer.partitions_for_topic(topic_name)
                if topic_partitions is None:
                    topics[topic_name] = {"exists": False, "partition_count": 0, "latest_total": None}
                    continue
                topic_tps = [TopicPartition(topic_name, index) for index in sorted(topic_partitions)]
                topic_ends = consumer.end_offsets(topic_tps)
                latest_total = sum(topic_ends[tp] for tp in topic_tps)
                previous_topic = previous_payload.get("topics", {}).get(topic_name, {})
                previous_latest = previous_topic.get("latest_total")
                topics[topic_name] = {
                    "exists": True,
                    "partition_count": len(topic_tps),
                    "latest_total": latest_total,
                    "latest_delta": latest_total - previous_latest if isinstance(previous_latest, int) else None,
                }
            known_lags = [row["lag"] for row in rows if row["lag"] is not None]
            total_lag = sum(known_lags) if len(known_lags) == len(rows) else None
            average_lag = (sum(known_lags) / len(known_lags)) if known_lags else None
            max_lag = max(known_lags) if known_lags else None
            return {"topic": topic, "consumer_group": self.chain["consumer_group"],
                    "bootstrap_connected": True, "topics": topics,
                    "partitions": rows, "total_lag": total_lag, "previous_total_lag": previous,
                    "lag_delta": total_lag - previous if isinstance(total_lag, int) and isinstance(previous, int) else None,
                    "summary": {"partition_count": len(rows), "unknown_committed_partitions": len(rows) - len(known_lags),
                                "max_partition_lag": max_lag, "average_partition_lag": average_lag,
                                "skew_ratio": (max_lag / average_lag) if average_lag else None,
                                "lagging_partitions": sum(value > 0 for value in known_lags)},
                    "note": "已提交位点可能滞后于实时消费；需结合 Flink 输入输出指标确认。"}
        finally:
            consumer.close()

    def model_health(self, question):
        data = {}
        probe_status = None
        model_url = self._service_url("model", self.settings.model_url)
        spec_model_url = self._service_url("model", "") if self.spec else ""
        if model_url and spec_model_url:
            service = next((item for item in self.spec.get("services", []) if item.get("type") == "model"), None)
            model_url = model_url.rstrip("/") + (service.get("config", {}).get("health_path", "/health") if service else "/health")
        if model_url:
            with httpx.Client(timeout=4) as client:
                response = client.get(model_url)
                response.raise_for_status()
                data = response.json()
            if not isinstance(data, dict) or not isinstance(data.get("status"), str):
                raise RuntimeError("模型健康端点未返回可识别的 JSON status")
            probe_status = data["status"].lower()
        log_path = os.environ.get("AGENT_MODEL_METRICS_LOG", "")
        if log_path:
            path = Path(log_path)
            if not path.is_file() or path.stat().st_size > 10 * 1024 * 1024:
                raise RuntimeError("模型调用指标日志不可用或超过 10MB")
            lines = path.read_text(encoding="utf-8", errors="replace").splitlines()[-300:]
            calls = []
            for line in lines:
                found = re.search(r"model_call ts_ms=(\d+) status=(\S+) duration_ms=([\d.]+)", line)
                if found and int(found.group(1)) >= int(time.time() * 1000) - 300000:
                    calls.append((found.group(2), float(found.group(3))))
            if calls:
                durations = sorted(duration for _, duration in calls)
                data = {**(data if isinstance(data, dict) else {}),
                        "latency_p95_ms": durations[math.ceil(len(durations) * .95) - 1],
                        "timeouts_5m": sum(status == "timeout_or_network_error" for status, _ in calls),
                        "errors_5m": sum(status != "ok" for status, _ in calls),
                        "sample_count": len(calls)}
        if not data:
            raise RuntimeError("未配置 MODEL_HEALTH_URL 或 AGENT_MODEL_METRICS_LOG")
        state = data.get("state")
        if not state:
            if probe_status is not None:
                state = "healthy" if probe_status in ("ok", "healthy") else "unhealthy"
            elif data.get("timeouts_5m") or data.get("errors_5m"):
                state = "degraded"
            else:
                state = "unknown"
        if (data.get("timeouts_5m") or data.get("errors_5m")) and state == "healthy":
            state = "degraded"
        metrics = data.get("metrics", data)
        return {"state": state, "metrics": metrics, "probe_status": probe_status}

    def service_health(self, question):
        """Probe HTTP endpoints declared in the versioned project specification."""
        if not self.spec:
            raise RuntimeError("项目规格中没有可探测的 HTTP 服务")
        hosts = {item["id"]: item for item in self.spec.get("hosts", [])}
        services = []
        for service in self.spec.get("services", []):
            protocol = str(service.get("protocol") or "").lower()
            config = service.get("config") or {}
            if protocol not in {"http", "https"} or not config.get("health_path"):
                continue
            host = hosts.get(service.get("host_id"), {})
            address = host.get("address")
            if not address:
                services.append({"service_id": service["id"], "name": service.get("name"),
                                 "type": service.get("type"), "state": "unavailable",
                                 "error": "项目规格未填写主机地址"})
                continue
            url = "{}://{}{}{}".format(protocol, address,
                                         ":" + str(service["port"]) if service.get("port") else "",
                                         str(config["health_path"])[:200])
            started = time.perf_counter()
            try:
                with httpx.Client(timeout=4, follow_redirects=False) as client:
                    response = client.get(url)
                latency = round((time.perf_counter() - started) * 1000, 2)
                state = "healthy" if 200 <= response.status_code < 400 else "unhealthy"
                services.append({"service_id": service["id"], "name": service.get("name"),
                                 "type": service.get("type"), "state": state,
                                 "status_code": response.status_code, "latency_ms": latency,
                                 "latency_threshold_ms": config.get("latency_threshold_ms", 1000)})
            except Exception as exc:
                services.append({"service_id": service["id"], "name": service.get("name"),
                                 "type": service.get("type"), "state": "unavailable",
                                 "latency_ms": round((time.perf_counter() - started) * 1000, 2),
                                 "error": str(exc)[:160]})
        if not services:
            raise RuntimeError("项目规格中没有配置 health_path 的 HTTP 服务")
        return {"services": services, "healthy": sum(item["state"] == "healthy" for item in services),
                "unhealthy": sum(item["state"] != "healthy" for item in services)}

    def dependency_health(self, question):
        """Perform bounded TCP checks for data stores declared by the project."""
        if not self.spec:
            raise RuntimeError("项目规格中没有可探测的依赖服务")
        supported = {"mysql", "postgresql", "redis", "clickhouse", "mongodb"}
        hosts = {item["id"]: item for item in self.spec.get("hosts", [])}
        services = []
        for service in self.spec.get("services", []):
            if str(service.get("type", "")).lower() not in supported:
                continue
            host = hosts.get(service.get("host_id"), {})
            address, port = host.get("address"), service.get("port")
            if not address or not port:
                services.append({"service_id": service["id"], "name": service.get("name"),
                                 "type": service.get("type"), "state": "unavailable",
                                 "error": "项目规格缺少主机地址或端口"})
                continue
            started = time.perf_counter()
            try:
                with socket.create_connection((address, int(port)), timeout=3):
                    pass
                services.append({"service_id": service["id"], "name": service.get("name"),
                                 "type": service.get("type"), "state": "healthy",
                                 "latency_ms": round((time.perf_counter() - started) * 1000, 2),
                                 "latency_threshold_ms": (service.get("config") or {}).get("latency_threshold_ms", 500)})
            except OSError as exc:
                services.append({"service_id": service["id"], "name": service.get("name"),
                                 "type": service.get("type"), "state": "unavailable",
                                 "latency_ms": round((time.perf_counter() - started) * 1000, 2),
                                 "error": str(exc)[:160]})
        if not services:
            raise RuntimeError("项目规格中没有支持的数据库或缓存服务")
        return {"services": services, "healthy": sum(item["state"] == "healthy" for item in services),
                "unhealthy": sum(item["state"] != "healthy" for item in services)}

    def prometheus_metrics(self, question):
        """Execute only PromQL queries stored in the project specification."""
        if not self.spec:
            raise RuntimeError("项目规格中没有 Prometheus 服务")
        hosts = {item["id"]: item for item in self.spec.get("hosts", [])}
        service = next((item for item in self.spec.get("services", [])
                        if str(item.get("type", "")).lower() == "prometheus"), None)
        if not service:
            raise RuntimeError("项目规格中没有 Prometheus 服务")
        host = hosts.get(service.get("host_id"), {})
        if not host.get("address"):
            raise RuntimeError("Prometheus 服务未配置主机地址")
        queries = (service.get("config") or {}).get("queries", {})
        if not isinstance(queries, dict) or not queries:
            raise RuntimeError("Prometheus 服务未配置受控 queries")
        protocol = str(service.get("protocol") or "http").lower()
        if protocol not in {"http", "https"}:
            raise RuntimeError("Prometheus 仅支持 http/https 协议")
        base_url = "{}://{}{}".format(protocol, host["address"],
                                       ":" + str(service["port"]) if service.get("port") else "")
        signals = []
        with httpx.Client(timeout=5) as client:
            for name, definition in list(queries.items())[:12]:
                definition = {"query": definition} if isinstance(definition, str) else definition
                query = definition.get("query") if isinstance(definition, dict) else None
                if not query or len(query) > 2000:
                    continue
                response = client.get(base_url.rstrip("/") + "/api/v1/query", params={"query": query})
                response.raise_for_status()
                body = response.json()
                results = body.get("data", {}).get("result", []) if body.get("status") == "success" else []
                values = []
                for result in results[:100]:
                    try:
                        values.append(float(result.get("value", [None, None])[1]))
                    except (TypeError, ValueError, IndexError):
                        pass
                value = max(values) if values else None
                threshold = definition.get("threshold")
                signals.append({"name": str(name)[:120], "value": value, "threshold": threshold,
                                "unit": definition.get("unit"), "component": definition.get("component", "project"),
                                "status": "warning" if value is not None and threshold is not None and value > float(threshold) else "ok",
                                "series_count": len(values)})
        return {"signals": signals, "query_count": len(signals), "provider": "prometheus"}

    def logs(self, question):
        path = Path(os.environ.get("AGENT_LOG_PATH", ""))
        if not str(path) or str(path) == "." or not path.is_file():
            raise RuntimeError("未配置可读的 AGENT_LOG_PATH")
        if path.stat().st_size > 10 * 1024 * 1024:
            raise RuntimeError("日志文件超过 10MB，请配置轮转后的有限日志")
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()[-300:]
        matches = [line[:500] for line in lines if re.search(r"error|timeout|fail|exception|warn", line, re.I)]
        return {"path": path.name, "lines": matches[-25:]}

    def runbook(self, question):
        from .runbooks import search
        return {"matches": search(question) + self.store.approved_cases(question)}
