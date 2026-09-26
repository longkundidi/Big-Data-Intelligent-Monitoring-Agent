import pytest
from langchain_core.messages import AIMessage

from streamdoctor.evaluate import fixed_summary, fixtures, rule_baseline, run, summarize


def test_rule_baseline_abstains_on_missing_observations():
    cases = dict(fixtures())
    assert rule_baseline(cases["observability_down"]) == "insufficient_evidence"
    assert rule_baseline(cases["model_slow"]) == "model_slow"
    assert rule_baseline(cases["normal"]) == "healthy"


def test_fixed_summary_parses_structured_answer_and_rejects_unknown_label():
    class Model:
        def __init__(self, answer):
            self.answer = answer

        def invoke(self, prompt):
            assert "flink_status" in prompt[1].content
            return AIMessage(content=self.answer, usage_metadata={"input_tokens": 20, "output_tokens": 5,
                                                                   "total_tokens": 25})

    case = dict(fixtures())["model_slow"]
    labels, usage = fixed_summary(Model('{"top3":["model_slow","lag_increasing"]}'), case)
    assert labels == ["model_slow", "lag_increasing"] and usage["total_tokens"] == 25
    with pytest.raises(ValueError):
        fixed_summary(Model('{"top3":["not_a_class"]}'), case)


def test_smoke_metrics_do_not_claim_model_comparison():
    result = run(case="normal")
    assert set(result["metrics"]) == {"offline_graph_smoke"}
    assert result["metrics"]["offline_graph_smoke"]["top1"] == 1
    assert result["metrics"]["offline_graph_smoke"]["normal_false_positives"] == 0
    assert summarize(result["cases"]) == result["metrics"]
