import json
from pathlib import Path

from streamdoctor.investigation import EvidencePlanner, causal_analysis


ROOT = Path(__file__).resolve().parents[1]


def evidence_from_replay(name):
    replay = json.loads((ROOT / "replays" / (name + ".json")).read_text(encoding="utf-8"))
    result = []
    for source in ("flink_status", "kafka_offsets", "flink_metrics", "model_health", "logs"):
        snapshot = replay.get(source)
        if snapshot:
            result.append({
                "id": "evidence-" + source,
                "source": source,
                "status": snapshot["status"],
                "payload": snapshot["payload"],
                "collected_at": "2026-09-26T08:00:00+00:00",
                "window_start": "2026-09-26T07:55:00+00:00",
                "window_end": "2026-09-26T08:00:00+00:00",
            })
    return result


def test_planner_follows_dependencies_and_stops_with_reason():
    planner = EvidencePlanner()
    available = evidence_from_replay("model_slow")
    collected = []
    called = set()
    sequence = []
    source_for_tool = {
        "get_flink_status": "flink_status",
        "get_kafka_offsets": "kafka_offsets",
        "get_flink_metrics": "flink_metrics",
        "get_model_health": "model_health",
        "get_component_logs": "logs",
    }
    for step in range(10):
        decision = planner.decide("为什么结果变慢，查明原因和处置方法", collected, called, step, "replay")
        if not decision["tool"]:
            assert decision["stop_reason"] == "sufficient_diagnosis"
            break
        tool = decision["tool"]
        sequence.append(tool)
        called.add(tool)
        source = source_for_tool.get(tool)
        if source:
            collected.extend(item for item in available if item["source"] == source)

    assert sequence[:5] == ["get_project_topology", "get_flink_status", "get_kafka_offsets",
                            "get_flink_metrics", "get_model_health"]
    assert sequence[-1] == "search_runbooks"
    assert "get_component_logs" not in sequence
    model_hypothesis = next(item for item in planner.hypotheses(collected, "为什么变慢", "replay")
                            if item["code"] == "model_slow")
    assert model_hypothesis["status"] == "supported"
    assert len(model_hypothesis["support_evidence_ids"]) == 3


def test_planner_short_circuits_when_flink_is_stopped():
    planner = EvidencePlanner()
    stopped = evidence_from_replay("job_failed")
    flink = next(item for item in stopped if item["source"] == "flink_status")
    called = {"get_project_topology", "get_flink_status"}
    decision = planner.decide("为什么不再产生结果", [flink], called, 2, "replay")
    assert decision["tool"] == "get_component_logs"
    called.add(decision["tool"])
    logs = {"id": "evidence-logs", "source": "logs", "status": "unavailable",
            "payload": {"error": "回放没有日志快照"},
            "collected_at": "2026-09-26T08:00:00+00:00",
            "window_start": "2026-09-26T07:55:00+00:00", "window_end": "2026-09-26T08:00:00+00:00"}
    assert planner.decide("为什么不再产生结果", [flink, logs], called, 3, "replay")["tool"] == "search_runbooks"
    called.add("search_runbooks")
    stopped_decision = planner.decide("为什么不再产生结果", [flink, logs], called, 4, "replay")
    assert stopped_decision["tool"] is None
    assert stopped_decision["stop_reason"] == "sufficient_fault_evidence"
    assert "get_kafka_offsets" not in called


def test_causal_analysis_links_claims_to_evidence():
    evidence = evidence_from_replay("model_slow")
    planner = EvidencePlanner()
    report_hypotheses = planner.hypotheses(evidence, "为什么变慢", "replay")
    supported = next(item for item in report_hypotheses if item["code"] == "model_slow")
    report = {"classification": "model_slow", "candidates": [{
        "code": supported["code"], "title": supported["title"],
        "support": supported["support_evidence_ids"], "counter": [], "verify": supported["verify"],
    }]}
    result = causal_analysis(evidence, report)
    assert result["view"] == "causal_analysis"
    assert len(result["causal_chain"]) == 2
    assert {item["component"] for item in result["timeline"]} >= {"Kafka", "Flink", "模型服务"}
    assert {edge["relation"] for edge in result["edges"]} == {"supports"}
    assert "不宣称统计因果" in result["disclaimer"]
