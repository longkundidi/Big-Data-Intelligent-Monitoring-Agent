"""Cross-platform diagnosis over normalized operational signals."""

from __future__ import annotations

from typing import Any


ROOT_CAUSES = {
    "dependency_unavailable": ("依赖服务不可用", "检查目标服务进程、网络、DNS 与认证配置。"),
    "latency_degradation": ("服务延迟显著升高", "对比延迟首次升高时间、依赖耗时和实例资源。"),
    "error_rate_high": ("服务错误率升高", "按状态码和异常类型拆分错误，并关联最近发布。"),
    "resource_saturation": ("计算或存储资源饱和", "核查 CPU、内存、磁盘或连接池瓶颈。"),
    "queue_backlog": ("队列或任务积压增长", "比较生产与处理速率，定位最先变慢的下游。"),
    "scheduler_failure": ("调度或批处理任务失败", "查看失败任务、重试历史和上游依赖状态。"),
    "data_quality": ("输入数据质量异常", "抽样核对 schema、空值、格式和拒绝记录。"),
    "storage_bottleneck": ("存储写入或查询成为瓶颈", "检查连接、慢查询、写入延迟和磁盘状态。"),
}

CAUSE_PRIORITY = {
    "dependency_unavailable": 50, "flink_stopped": 50, "scheduler_failure": 45,
    "invalid_data": 45, "data_quality": 45, "model_unhealthy": 42, "model_slow": 40,
    "storage_bottleneck": 40, "resource_saturation": 35, "error_rate_high": 32,
    "latency_degradation": 30, "queue_backlog": 22, "lag_increasing": 20,
    "source_stopped": 20,
}


def _number(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def normalized_signals(evidence: list[dict[str, Any]]):
    signals = []
    for item in evidence:
        if item.get("status") != "ok":
            continue
        payload = item.get("payload", {})
        for signal in payload.get("signals", []):
            if isinstance(signal, dict) and signal.get("name"):
                signals.append({**signal, "evidence_id": item["id"], "source": item["source"]})
        if item.get("source") in {"service_health", "dependency_health"}:
            for service in payload.get("services", []):
                state = service.get("state")
                if state in {"unhealthy", "unavailable"}:
                    signals.append({"name": "dependency.availability", "value": 0, "status": "critical",
                                    "component": service.get("name") or service.get("service_id"),
                                    "evidence_id": item["id"], "source": item["source"]})
                latency = _number(service.get("latency_ms"))
                threshold = _number(service.get("latency_threshold_ms"))
                if latency is not None and threshold and latency > threshold:
                    signals.append({"name": "service.latency", "value": latency, "threshold": threshold,
                                    "status": "warning", "component": service.get("name") or service.get("service_id"),
                                    "evidence_id": item["id"], "source": item["source"]})
    return signals


def classify_signals(signals: list[dict[str, Any]]):
    scores: dict[str, dict[str, Any]] = {}

    def support(code, signal, weight=1):
        entry = scores.setdefault(code, {"score": 0, "evidence_ids": [], "signals": []})
        entry["score"] += weight
        if signal.get("evidence_id") and signal["evidence_id"] not in entry["evidence_ids"]:
            entry["evidence_ids"].append(signal["evidence_id"])
        entry["signals"].append(signal.get("name"))

    for signal in signals:
        name = str(signal.get("name", "")).lower()
        value = _number(signal.get("value"))
        threshold = _number(signal.get("threshold"))
        status = signal.get("status")
        abnormal = status in {"warning", "critical", "error", "failed"} or (
            value is not None and threshold is not None and value > threshold
        )
        if not abnormal:
            continue
        if "availability" in name or name.endswith(".up") and value == 0:
            support("dependency_unavailable", signal, 3)
        if "latency" in name or "duration" in name:
            support("latency_degradation", signal, 2)
            if any(word in name for word in ("storage", "database", "query", "write")):
                support("storage_bottleneck", signal, 2)
        if "error_rate" in name or "errors" in name or "5xx" in name:
            support("error_rate_high", signal, 2)
        if any(word in name for word in ("cpu", "memory", "disk", "saturation", "pool")):
            support("resource_saturation", signal, 2)
        if any(word in name for word in ("backlog", "queue", "lag")):
            support("queue_backlog", signal, 2)
        if ("scheduler" in name and any(word in name for word in ("failed", "status", "error"))) or any(
                word in name for word in ("job.status", "task.failed", "dag.failed")):
            support("scheduler_failure", signal, 3)
        if any(word in name for word in ("schema", "invalid", "null_rate", "data.quality", "rejected")):
            support("data_quality", signal, 3)
        if any(word in name for word in ("storage", "database", "query", "write")):
            support("storage_bottleneck", signal, 1)

    return sorted(scores.items(), key=lambda item: (-item[1]["score"], item[0]))


def general_report(evidence: list[dict[str, Any]], question: str, mode="live"):
    signals = normalized_signals(evidence)
    ranked = classify_signals(signals)
    candidates = []
    for code, details in ranked[:3]:
        title, verify = ROOT_CAUSES[code]
        candidates.append({"code": code, "title": title, "support": details["evidence_ids"], "counter": [],
                           "verify": verify, "score": details["score"], "signals": details["signals"]})
    missing = [item["source"] + ": " + str(item.get("payload", {}).get("error", "不可用"))
               for item in evidence if item.get("status") != "ok"]
    healthy = bool(signals) and not candidates and all(item.get("status", "ok") == "ok" for item in signals)
    classification = candidates[0]["code"] if candidates else ("healthy" if healthy else "insufficient_evidence")
    return {
        "summary": "发现跨组件运行异常" if candidates else ("关键观测未发现异常" if healthy else "证据不足，暂不能定位根因"),
        "question": question, "mode": mode,
        "facts": ["{}={} ({})".format(item["name"], item.get("value"), item.get("component", "unknown"))
                  for item in signals if item.get("status") in {"warning", "critical", "error", "failed"}][:6],
        "impact": "根据项目拓扑继续确认受影响的下游节点" if candidates else "影响范围未知",
        "candidates": candidates, "missing": missing,
        "actions": [item["verify"] for item in candidates] or ["补齐服务健康、指标、日志或依赖观测后重试。"],
        "verification": "处理后重新采样相同信号，并与故障窗口比较。",
        "classification": classification, "signals": signals,
    }


def combine_reports(legacy: dict[str, Any], generic: dict[str, Any]):
    """Rank causes from specialized and generic adapters without losing provenance."""
    candidates = []
    seen = set()
    for item in legacy.get("candidates", []) + generic.get("candidates", []):
        if item.get("code") not in seen:
            candidates.append(item)
            seen.add(item.get("code"))
    candidates.sort(key=lambda item: (-CAUSE_PRIORITY.get(item.get("code"), 0), -len(item.get("support", []))))
    if candidates:
        classification = candidates[0]["code"]
        summary = "发现可能的跨组件故障源" if legacy.get("candidates") and generic.get("candidates") else (
            generic.get("summary") if generic.get("candidates") else legacy.get("summary"))
    elif legacy.get("classification") == "healthy" or generic.get("classification") == "healthy":
        classification, summary = "healthy", "关键观测未发现异常"
    else:
        classification, summary = "insufficient_evidence", "证据不足，暂不能定位根因"
    return {
        **legacy, "summary": summary, "classification": classification, "candidates": candidates,
        "facts": list(dict.fromkeys(legacy.get("facts", []) + generic.get("facts", []))),
        "missing": list(dict.fromkeys(legacy.get("missing", []) + generic.get("missing", []))),
        "actions": list(dict.fromkeys(legacy.get("actions", []) + generic.get("actions", []))),
        "signals": generic.get("signals", []),
    }
