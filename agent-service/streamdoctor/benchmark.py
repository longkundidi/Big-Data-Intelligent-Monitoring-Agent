"""Deterministic cross-domain benchmark for diagnosis and tool trajectories."""

from __future__ import annotations

import json
from pathlib import Path

from .general_diagnosis import general_report
from .investigation import EvidencePlanner


CASES_PATH = Path(__file__).resolve().parents[1] / "benchmark" / "cases.json"
SOURCE_TO_TOOL = {
    "service_health": "get_service_health",
    "dependency_health": "get_dependency_health",
    "prometheus_metrics": "get_prometheus_metrics",
    "logs": "get_component_logs",
}
TOOL_TO_SOURCE = {value: key for key, value in SOURCE_TO_TOOL.items()}
DOMAIN_CAPABILITIES = {
    "microservice": ["get_service_health", "get_prometheus_metrics"],
    "gateway": ["get_service_health", "get_prometheus_metrics"],
    "model-serving": ["get_service_health", "get_prometheus_metrics"],
    "database": ["get_dependency_health", "get_prometheus_metrics"],
    "cache": ["get_dependency_health"],
    "batch": ["get_service_health", "get_dependency_health", "get_prometheus_metrics"],
    "compute": ["get_prometheus_metrics"],
    "messaging": ["get_prometheus_metrics"],
    "data-quality": ["get_prometheus_metrics"],
    "observability": ["get_prometheus_metrics"],
}


def cases(split=None):
    values = json.loads(CASES_PATH.read_text(encoding="utf-8"))
    return [item for item in values if split is None or item["split"] == split]


def simulate_trajectory(case, evidence):
    """Run the real planner while revealing only evidence returned by selected tools."""
    planner = EvidencePlanner()
    snapshots = {item["source"]: item for item in evidence}
    capabilities = DOMAIN_CAPABILITIES.get(case["domain"], list(SOURCE_TO_TOOL.values()))
    observed, called, diagnostic_tools = [], set(), []
    for step in range(12):
        decision = planner.decide(case["id"], observed, called, step, "benchmark", capabilities)
        tool = decision.get("tool")
        if not tool:
            break
        called.add(tool)
        source = TOOL_TO_SOURCE.get(tool)
        if source:
            diagnostic_tools.append(tool)
            snapshot = snapshots.get(source)
            if snapshot:
                observed.append(snapshot)
            else:
                observed.append({"id": "{}-missing-{}".format(case["id"], source), "source": source,
                                 "status": "unavailable", "payload": {"error": "benchmark snapshot missing"},
                                 "collected_at": "2026-09-26T00:00:00Z",
                                 "window_start": "2026-09-25T23:55:00Z", "window_end": "2026-09-26T00:00:00Z"})
    return diagnostic_tools


def evaluate(split=None):
    rows = []
    for case in cases(split):
        evidence = []
        for index, item in enumerate(case["evidence"], start=1):
            evidence.append({**item, "id": "{}-e{}".format(case["id"], index),
                             "collected_at": "2026-09-26T00:00:00Z",
                             "window_start": "2026-09-25T23:55:00Z", "window_end": "2026-09-26T00:00:00Z"})
        report = general_report(evidence, case["id"], "benchmark")
        top3 = [item["code"] for item in report["candidates"][:3]] or [report["classification"]]
        valid_ids = {item["id"] for item in evidence if item["status"] == "ok"}
        citations = [source_id for candidate in report["candidates"] for source_id in candidate["support"]]
        actual_sequence = simulate_trajectory(case, evidence)
        actual_tools = set(actual_sequence)
        expected_tools = set(case["expected_tools"])
        rows.append({
            "id": case["id"], "domain": case["domain"], "split": case["split"],
            "expected": case["expected"], "top3": top3,
            "top1_correct": top3[0] == case["expected"], "top3_correct": case["expected"] in top3,
            "citations_valid": bool(citations) and set(citations).issubset(valid_ids) if report["candidates"] else None,
            "expected_tools": sorted(expected_tools), "actual_tools": sorted(actual_tools),
            "actual_trajectory": actual_sequence,
            "tool_true_positive": len(actual_tools & expected_tools),
            "tool_false_positive": len(actual_tools - expected_tools),
            "tool_false_negative": len(expected_tools - actual_tools),
        })
    count = len(rows)
    tp = sum(item["tool_true_positive"] for item in rows)
    fp = sum(item["tool_false_positive"] for item in rows)
    fn = sum(item["tool_false_negative"] for item in rows)
    normal = [item for item in rows if item["expected"] == "healthy"]
    abstain = [item for item in rows if item["expected"] == "insufficient_evidence"]
    diagnosable = [item for item in rows if item["expected"] not in {"healthy", "insufficient_evidence"}]
    by_domain = {}
    for domain in sorted({item["domain"] for item in rows}):
        group = [item for item in rows if item["domain"] == domain]
        by_domain[domain] = {"cases": len(group), "top1_accuracy": round(sum(item["top1_correct"] for item in group) / len(group), 4)}
    metrics = {
        "cases": count,
        "top1_accuracy": round(sum(item["top1_correct"] for item in rows) / count, 4) if count else 0,
        "top3_accuracy": round(sum(item["top3_correct"] for item in rows) / count, 4) if count else 0,
        "evidence_support_rate": round(sum(item["citations_valid"] is True for item in diagnosable) / len(diagnosable), 4) if diagnosable else 0,
        "normal_false_positive_rate": round(sum(item["top3"][0] not in {"healthy", "insufficient_evidence"} for item in normal) / len(normal), 4) if normal else 0,
        "abstention_accuracy": round(sum(item["top3"][0] == "insufficient_evidence" for item in abstain) / len(abstain), 4) if abstain else 0,
        "tool_precision": round(tp / (tp + fp), 4) if tp + fp else 0,
        "tool_recall": round(tp / (tp + fn), 4) if tp + fn else 0,
        "mean_tool_calls": round(sum(len(item["actual_trajectory"]) for item in rows) / count, 2) if count else 0,
    }
    return {"metrics": metrics, "by_domain": by_domain, "cases": rows,
            "dataset": {"name": "StreamDoctor Cross-Domain Seed", "version": "1.0", "synthetic": True,
                        "limitation": "人工合成快照只用于回归和架构比较，不代表生产故障准确率。"}}


if __name__ == "__main__":
    print(json.dumps(evaluate(), ensure_ascii=False, indent=2))
