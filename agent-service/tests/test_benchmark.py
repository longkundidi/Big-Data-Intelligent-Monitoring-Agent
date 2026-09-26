from streamdoctor.benchmark import cases, evaluate
from streamdoctor.general_diagnosis import general_report
from streamdoctor.specs import BUILTIN_TEMPLATES
from streamdoctor.toolsets import enabled_tools


def test_cross_domain_benchmark_meets_regression_thresholds():
    result = evaluate()
    metrics = result["metrics"]
    assert metrics["cases"] >= 20
    assert metrics["top1_accuracy"] >= 0.9
    assert metrics["top3_accuracy"] >= 0.95
    assert metrics["evidence_support_rate"] == 1.0
    assert metrics["normal_false_positive_rate"] == 0.0
    assert metrics["abstention_accuracy"] == 1.0
    assert metrics["tool_precision"] >= 0.6
    assert metrics["tool_recall"] >= 0.9
    assert {item["domain"] for item in cases()} >= {
        "microservice", "database", "batch", "data-quality", "compute", "messaging",
    }


def test_generic_templates_enable_project_specific_toolsets():
    templates = {item["id"]: item["spec"] for item in BUILTIN_TEMPLATES}
    assert {"microservice-observability", "batch-etl", "generic-service-chain"}.issubset(templates)
    assert set(enabled_tools(templates["microservice-observability"])) >= {
        "get_service_health", "get_dependency_health", "get_prometheus_metrics",
    }
    assert set(enabled_tools(templates["batch-etl"])) >= {"get_service_health", "get_dependency_health"}


def test_general_report_abstains_when_observation_is_unavailable():
    evidence = [{"id": "e1", "source": "prometheus_metrics", "status": "unavailable",
                 "payload": {"error": "timeout"}}]
    report = general_report(evidence, "为什么没有产出")
    assert report["classification"] == "insufficient_evidence"
    assert report["candidates"] == []
    assert report["missing"] == ["prometheus_metrics: timeout"]
