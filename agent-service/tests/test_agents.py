from streamdoctor.agents import ExecutionIntent, ExecutorAgent, ExecutorPolicy, ReviewerAgent


def test_reviewer_is_read_only_and_reports_observation_gaps():
    review = ReviewerAgent().review(
        {"classification": "degraded", "summary": "链路异常", "missing": ["模型日志"],
         "candidates": [{"title": "模型超时", "evidence": ["e-1"], "verify": "检查延迟"}]},
        [{"source": "model_health", "status": "unavailable"}],
    )
    assert review["may_execute"] is False
    assert review["permissions"] == ["observe", "review", "recommend"]
    assert review["unavailable_sources"] == ["model_health"]


def test_executor_policy_requires_an_explicit_mutation_request():
    advisory = ExecutorPolicy.parse("分析是否需要重启 Flink")
    assert advisory and advisory.action == "restart" and advisory.explicit is False
    explicit = ExecutorPolicy.parse("请重启 Flink")
    assert explicit and explicit.action == "restart" and explicit.target == "flink" and explicit.explicit is True
    slash = ExecutorPolicy.parse("/stop kafka")
    assert slash and slash.action == "stop" and slash.target == "kafka" and slash.explicit is True
    assert ExecutorPolicy.parse("执行 rm -rf /tmp") is None


def test_executor_calls_only_the_configured_action_runner(monkeypatch):
    captured = {}

    class Response:
        def raise_for_status(self):
            return None

        def json(self):
            return {"status": "ok", "details": [{"container": "streamdoctor-kafka", "status": "ok"}]}

    def fake_post(url, json, headers, timeout):
        captured.update({"url": url, "json": json, "headers": headers, "timeout": timeout})
        return Response()

    monkeypatch.setattr("streamdoctor.agents.httpx.post", fake_post)
    agent = ExecutorAgent("http://executor:8100", "secret")
    result = agent.execute(ExecutionIntent("restart", "kafka", True, "slash"), "project-1", "run-1")
    assert result["status"] == "ok"
    assert captured["json"] == {"action": "restart", "target": "kafka", "project_id": "project-1", "run_id": "run-1"}
    assert captured["headers"] == {"X-Executor-Token": "secret"}
