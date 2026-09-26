"""Evidence-driven planning, hypothesis tracking, and causal artifacts."""

from __future__ import annotations

from typing import Any

from .diagnosis import numeric, report_for


SOURCE_TO_TOOL = {
    "flink_status": "get_flink_status",
    "flink_metrics": "get_flink_metrics",
    "kafka_offsets": "get_kafka_offsets",
    "model_health": "get_model_health",
    "logs": "get_component_logs",
    "runbook": "search_runbooks",
}


HYPOTHESIS_DEFINITIONS = (
    {"code": "flink_stopped", "title": "Flink 作业停止或反复失败", "requires": ["flink_status"]},
    {"code": "model_slow", "title": "模型服务延迟导致反压和积压", "requires": ["model_health", "flink_metrics", "kafka_offsets"]},
    {"code": "model_unhealthy", "title": "模型服务不可用或持续报错", "requires": ["model_health"]},
    {"code": "lag_increasing", "title": "Kafka 消费速度低于生产速度", "requires": ["kafka_offsets", "flink_metrics"]},
    {"code": "source_stopped", "title": "上游停止产生数据", "requires": ["kafka_offsets", "flink_metrics"]},
    {"code": "invalid_data", "title": "输入数据格式导致作业异常", "requires": ["flink_status", "logs"]},
)


def _by_source(evidence: list[dict[str, Any]]) -> dict[str, dict[str, Any]]:
    return {item["source"]: item for item in evidence}


class EvidencePlanner:
    """Selects the next bounded read-only check from current evidence."""

    MAX_STEPS = 10

    def hypotheses(self, evidence: list[dict[str, Any]], question: str, mode: str = "live"):
        report = report_for(evidence, question, mode)
        candidates = {item["code"]: item for item in report.get("candidates", [])}
        observed = _by_source(evidence)
        result = []
        for definition in HYPOTHESIS_DEFINITIONS:
            missing = [source for source in definition["requires"] if source not in observed]
            unavailable = [source for source in definition["requires"]
                           if source in observed and observed[source].get("status") != "ok"]
            candidate = candidates.get(definition["code"])
            if candidate:
                status = "supported"
            elif unavailable:
                status = "unknown"
            elif not missing:
                status = "not_supported"
            else:
                status = "pending"
            result.append({
                **definition, "status": status, "missing_sources": missing,
                "unavailable_sources": unavailable,
                "support_evidence_ids": (candidate or {}).get("support", []),
                "counter_evidence_ids": (candidate or {}).get("counter", []),
                "verify": (candidate or {}).get("verify", "补齐所需观测后再判断。"),
            })
        return result

    def decide(self, question: str, evidence: list[dict[str, Any]], called: set[str], step: int,
               mode: str = "live") -> dict[str, Any]:
        text = question.lower()
        if step >= self.MAX_STEPS:
            return {"tool": None, "reason": "达到取证轮次上限", "stop_reason": "step_budget"}
        observed = _by_source(evidence)
        report = report_for(evidence, question, mode)

        def choose(tool, reason, hypothesis_codes):
            if tool not in called:
                return {"tool": tool, "reason": reason, "hypothesis_codes": hypothesis_codes,
                        "stop_reason": None}
            return None

        decision = choose("get_project_topology", "先确认本次诊断的组件边界和依赖关系", [])
        if decision:
            return decision
        decision = choose("get_flink_status", "先判断计算作业是否存在、运行或失败", ["flink_stopped", "invalid_data"])
        if decision:
            return decision

        flink = observed.get("flink_status")
        flink_state = (flink or {}).get("payload", {}).get("state") if flink and flink.get("status") == "ok" else None
        if flink_state and flink_state != "RUNNING":
            decision = choose("get_component_logs", "作业未正常运行，补查异常日志", ["flink_stopped", "invalid_data"])
            if decision:
                return decision
            decision = choose("search_runbooks", "已发现作业状态异常，检索对应处置步骤", ["flink_stopped"])
            if decision:
                return decision
            return {"tool": None, "reason": "作业异常已有状态和排障依据", "stop_reason": "sufficient_fault_evidence"}

        decision = choose("get_kafka_offsets", "检查输入进度、分区积压及积压变化", ["lag_increasing", "source_stopped"])
        if decision:
            return decision
        decision = choose("get_flink_metrics", "用吞吐、忙碌度和反压区分上游停发与下游变慢", ["model_slow", "lag_increasing", "source_stopped"])
        if decision:
            return decision
        decision = choose("get_model_health", "检查同步模型调用是否限制 Flink 处理能力", ["model_slow", "model_unhealthy"])
        if decision:
            return decision

        if report.get("candidates") or any(word in text for word in ("日志", "异常", "报错", "timeout", "error")):
            decision = choose("get_component_logs", "为当前候选原因补充日志证据", [item["code"] for item in report.get("candidates", [])])
            if decision:
                return decision
        if report.get("candidates") or any(word in text for word in ("怎么排查", "原因", "处置", "恢复")):
            decision = choose("search_runbooks", "为已支持的候选原因检索版本化排障步骤", [item["code"] for item in report.get("candidates", [])])
            if decision:
                return decision

        baseline = {"get_flink_status", "get_kafka_offsets", "get_flink_metrics", "get_model_health"}
        stop_reason = "sufficient_diagnosis" if report.get("classification") != "insufficient_evidence" else "no_more_relevant_tools"
        reason = "关键观测已覆盖，停止继续调用工具" if baseline.issubset(called) else "没有更多可提高判断质量的工具"
        return {"tool": None, "reason": reason, "stop_reason": stop_reason}


def causal_analysis(evidence: list[dict[str, Any]], report: dict[str, Any]) -> dict[str, Any]:
    observed = _by_source(evidence)
    timeline = []

    def add(source, component, signal, value, severity="info"):
        item = observed.get(source)
        if item:
            timeline.append({"source": source, "evidence_id": item["id"], "time": item["collected_at"],
                             "component": component, "signal": signal, "value": value, "severity": severity})

    model = (observed.get("model_health") or {}).get("payload", {})
    model_metrics = model.get("metrics", {})
    latency = numeric(model_metrics.get("latency_p95_ms"))
    timeouts = numeric(model_metrics.get("timeouts_5m"))
    if latency is not None or timeouts:
        add("model_health", "模型服务", "调用延迟与超时",
            "P95={}ms，超时={}次".format(latency if latency is not None else "未知", int(timeouts or 0)),
            "warning" if timeouts or model.get("state") == "degraded" else "info")

    vertices = (observed.get("flink_metrics") or {}).get("payload", {}).get("vertices", [])
    backpressure = [numeric(item.get("metrics", {}).get("backPressuredTimeMsPerSecond")) for item in vertices]
    backpressure = [value for value in backpressure if value is not None]
    if backpressure:
        add("flink_metrics", "Flink", "算子反压", "最高 {:.0f} ms/s".format(max(backpressure)),
            "warning" if max(backpressure) > 500 else "info")

    kafka = (observed.get("kafka_offsets") or {}).get("payload", {})
    lag = numeric(kafka.get("total_lag"))
    previous = numeric(kafka.get("previous_total_lag"))
    if lag is not None:
        add("kafka_offsets", "Kafka", "消费积压",
            "{} → {}".format(int(previous), int(lag)) if previous is not None else str(int(lag)),
            "warning" if lag > 0 else "info")

    flink = (observed.get("flink_status") or {}).get("payload", {})
    if flink.get("state"):
        add("flink_status", "Flink", "作业状态", flink["state"],
            "critical" if flink["state"] != "RUNNING" else "info")

    claims = []
    edges = []
    for index, candidate in enumerate(report.get("candidates", []), start=1):
        claim_id = "claim-{}".format(index)
        claims.append({"id": claim_id, "code": candidate["code"], "title": candidate["title"],
                       "status": "supported" if candidate.get("support") else "unverified",
                       "verify": candidate.get("verify")})
        edges.extend({"from": evidence_id, "to": claim_id, "relation": "supports"}
                     for evidence_id in candidate.get("support", []))
        edges.extend({"from": evidence_id, "to": claim_id, "relation": "counters"}
                     for evidence_id in candidate.get("counter", []))

    chain = []
    classification = report.get("classification")
    if classification == "model_slow":
        chain = [
            {"from": "模型调用延迟/超时", "to": "Flink 算子反压", "relation": "可能导致"},
            {"from": "Flink 处理能力下降", "to": "Kafka 消费积压增长", "relation": "传播为"},
        ]
    elif classification == "flink_stopped":
        chain = [{"from": "Flink 作业停止", "to": "输入 Topic 无法继续消费", "relation": "直接导致"}]
    elif classification == "source_stopped":
        chain = [{"from": "上游停止发送", "to": "Flink 输入与输出归零", "relation": "直接导致"}]

    return {
        "view": "causal_analysis", "classification": classification,
        "timeline": timeline, "claims": claims, "evidence": [
            {"id": item["id"], "source": item["source"], "status": item["status"]} for item in evidence
        ], "edges": edges, "causal_chain": chain,
        "disclaimer": "时间线表示同一观测窗口内的信号关联；没有独立时间证据时不宣称统计因果。",
    }
