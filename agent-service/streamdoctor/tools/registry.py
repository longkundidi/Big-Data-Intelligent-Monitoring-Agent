"""Server-owned, read-only tools exposed to the Agent."""

import json
import time
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Callable

from ..artifacts import make_artifact
from ..collectors import Collectors
from ..configuration import inspect_configuration
from ..runbooks import search as search_runbooks
from ..specs import spec_topology


class ToolError(RuntimeError):
    pass


@dataclass(frozen=True)
class ToolSpec:
    name: str
    description: str
    handler: Callable[..., dict[str, Any]]
    timeout_seconds: float = 12
    max_result_chars: int = 50_000
    read_only: bool = True


class ToolRegistry:
    def __init__(self, settings, store, project_id: str, run_id: str, replay=None):
        self.settings = settings
        self.store = store
        self.project_id = project_id
        self.run_id = run_id
        self.replay = replay
        self.collector = Collectors(settings, store, replay=replay, project_id=project_id)
        self._cache: dict[str, tuple[float, dict[str, Any]]] = {}
        self.specs = self._build_specs()

    def _build_specs(self):
        return {
            "get_project_topology": ToolSpec("get_project_topology", "读取项目链路拓扑", self._topology),
            "get_configuration_status": ToolSpec("get_configuration_status", "检查项目链路规格与运行连接的配置完整度", self._configuration),
            "get_flink_status": ToolSpec("get_flink_status", "读取 Flink 集群和目标作业状态", lambda query="", **kwargs: self._observe("flink_status", query, **kwargs)),
            "get_flink_metrics": ToolSpec("get_flink_metrics", "读取 Flink 输入输出、忙碌度和反压指标", lambda query="", **kwargs: self._observe("flink_metrics", query, **kwargs)),
            "get_kafka_offsets": ToolSpec("get_kafka_offsets", "读取 Kafka Topic 位点和消费积压", lambda query="", **kwargs: self._observe("kafka_offsets", query, **kwargs)),
            "get_model_health": ToolSpec("get_model_health", "读取模型服务健康、延迟和错误统计", lambda query="", **kwargs: self._observe("model_health", query, **kwargs)),
            "get_component_logs": ToolSpec("get_component_logs", "读取服务端配置的有限日志片段", lambda query="", **kwargs: self._observe("logs", query, **kwargs)),
            "get_business_summary": ToolSpec("get_business_summary", "读取已配置业务接口的只读摘要", self._business),
            "search_runbooks": ToolSpec("search_runbooks", "检索排障文档和审核案例", self._runbooks),
            "search_project_memory": ToolSpec("search_project_memory", "检索当前项目已确认经验", self._memory),
            "get_evidence": ToolSpec("get_evidence", "读取本次运行已保存的证据", self._evidence),
            "create_chart_artifact": ToolSpec("create_chart_artifact", "从证据生成固定结构的指标图表", self._chart),
            "create_topology_artifact": ToolSpec("create_topology_artifact", "生成链路拓扑可视化", self._topology_artifact),
            "propose_memory": ToolSpec("propose_memory", "提议一条需要用户确认的项目记忆", self._propose_memory),
        }

    def _topology(self, **_):
        project = self.store.project(self.project_id)
        current = self.store.project_spec(self.project_id)
        if current:
            return {**spec_topology(current["spec"], self.project_id), "spec_version_id": current["id"],
                    "spec_version": current["version"]}
        base = self.settings.topology()
        if not project:
            return {"id": self.project_id, "nodes": [], "edges": []}
        if project["topology_id"] != base.get("id"):
            nodes = []
            edges = []
            previous = None
            for resource in self.store.resources(self.project_id):
                nodes.append({"id": resource["id"], "label": resource["name"], "type": resource["type"], "status": resource["status"]})
                if previous:
                    edges.append({"from": previous, "to": resource["id"]})
                previous = resource["id"]
            return {"id": project["topology_id"], "name": project["name"] + "链路", "project_id": self.project_id,
                    "nodes": nodes, "edges": edges}
        return {**base, "project_id": self.project_id, "resource_ids": [item["id"] for item in self.store.resources(self.project_id)]}

    def _configuration(self, **_):
        payload = inspect_configuration(self.settings, self.store, self.project_id)
        timestamp = datetime.now(timezone.utc).isoformat()
        evidence = self.store.add_run_evidence(
            self.run_id, "configuration_status", "ok", payload, timestamp, timestamp
        )
        return {"status": "ok", "data": payload, "evidence_id": evidence["id"],
                "collected_at": evidence["collected_at"], "window_start": timestamp, "window_end": timestamp}

    def _observe(self, source, query="", force_refresh=False, **_):
        cache_key = json.dumps([source, query], ensure_ascii=False, sort_keys=True)
        previous = self._cache.get(cache_key)
        if previous and not force_refresh and time.monotonic() - previous[0] < 15 and not self.replay:
            return {**previous[1], "cached": True}
        status, payload, window_start, window_end = self.collector.collect(source, query)
        evidence = self.store.add_run_evidence(self.run_id, source, status, payload, window_start, window_end)
        result = {"status": status, "data": payload, "evidence_id": evidence["id"],
                  "collected_at": evidence["collected_at"], "window_start": window_start, "window_end": window_end}
        if not self.replay:
            self._cache[cache_key] = (time.monotonic(), result)
        return result

    def _business(self, **_):
        return {"status": "unavailable", "data": {"error": "尚未配置只读业务摘要适配器"}}

    def _runbooks(self, query="", **_):
        payload = {"matches": search_runbooks(query) + self.store.approved_cases(query)}
        timestamp = datetime.now(timezone.utc).isoformat()
        evidence = self.store.add_run_evidence(self.run_id, "runbook", "ok", payload, timestamp, timestamp)
        return {"status": "ok", **payload, "data": payload, "evidence_id": evidence["id"],
                "collected_at": evidence["collected_at"], "window_start": timestamp, "window_end": timestamp}

    def _memory(self, query="", **_):
        return {"status": "ok", "matches": self.store.search_memories(self.project_id, query, 5)}

    def _evidence(self, evidence_id="", **_):
        for item in self.store.run_evidence(self.run_id):
            if item["id"] == evidence_id:
                return {"status": "ok", "evidence": item}
        raise ToolError("证据不属于当前运行")

    def _chart(self, evidence_ids=None, metric="lag", **_):
        evidence_ids = set(evidence_ids or [])
        items = [item for item in self.store.run_evidence(self.run_id) if not evidence_ids or item["id"] in evidence_ids]
        points = []
        for item in items:
            payload = item["payload"]
            value = payload.get("total_lag") if metric == "lag" else payload.get("metrics", {}).get(metric)
            if value is not None:
                points.append({"time": item["collected_at"], "value": value})
        artifact = self.store.create_artifact(self.run_id, "chart", {"metric": metric, "series": points}, list(evidence_ids))
        return {"status": "ok", "artifact_id": artifact["id"], "artifact": artifact}

    def _topology_artifact(self, **_):
        artifact = self.store.create_artifact(self.run_id, "topology", self.settings.topology())
        return {"status": "ok", "artifact_id": artifact["id"], "artifact": artifact}

    def _propose_memory(self, kind="fact", content="", source_ids=None, **_):
        if not content or len(content) > 2000:
            raise ToolError("记忆内容不能为空且不能超过 2000 字")
        memory = self.store.create_memory(self.project_id, kind, content, source_ids or [], status="proposed")
        return {"status": "proposed", "memory": memory}

    def execute(self, name: str, arguments: dict[str, Any] | None = None):
        if name not in self.specs:
            raise ToolError("未注册的工具")
        arguments = arguments or {}
        if not isinstance(arguments, dict):
            raise ToolError("工具参数必须是对象")
        # Models may choose a query and evidence IDs, but never a destination.
        forbidden = {key for key in arguments if key in {"url", "host", "sql", "command", "script", "javascript"}}
        if forbidden:
            raise ToolError("工具不接受目标地址、SQL 或命令参数")
        result = self.specs[name].handler(**arguments)
        if len(json.dumps(result, ensure_ascii=False)) > self.specs[name].max_result_chars:
            raise ToolError("工具结果超过大小限制")
        return result

    def as_openai_tools(self):
        return [{"type": "function", "function": {"name": spec.name, "description": spec.description,
                 "parameters": {"type": "object", "properties": {"query": {"type": "string"}}, "additionalProperties": True}}}
                for spec in self.specs.values()]
