import json
from pathlib import Path

from streamdoctor.config import ROOT, Settings
from streamdoctor.diagnosis import Diagnostician, report_for
from streamdoctor.storage import Store


def scenario(name):
    return json.loads((ROOT / "replays" / (name + ".json")).read_text(encoding="utf-8"))


def configured(tmp_path):
    settings = Settings(db_path=tmp_path / "agent.db", topology_path=ROOT / "topology.json",
                        flink_url="", kafka_servers="", model_url="", model_api_key="", model_name="")
    return settings, Store(settings.db_path)


def test_replay_has_cited_root_cause_and_survives_reopen(tmp_path):
    settings, store = configured(tmp_path)
    incident_id, _ = store.create("elevator-regtcn", "manual", "为什么结果不再产生？", mode="replay:model_slow")
    Diagnostician(settings, store).run(incident_id, scenario("model_slow"))
    saved = Store(settings.db_path).get(incident_id)
    assert saved["status"] == "open"
    assert saved["report"]["classification"] == "model_slow"
    assert set(saved["report"]["candidates"][0]["support"]).issubset({item["id"] for item in saved["evidence"]})
    assert len(saved["evidence"]) == 7
    assert len(store.events(incident_id, 0)) > 7
    assert store.events(incident_id, store.events(incident_id, 0)[-1]["seq"]) == []
    Diagnostician(settings, Store(settings.db_path)).run(incident_id, scenario("model_slow"))
    assert len(Store(settings.db_path).get(incident_id)["evidence"]) == 7


def test_normal_and_unknown_not_called_fault(tmp_path):
    settings, store = configured(tmp_path)
    normal_id, _ = store.create("elevator-regtcn", "manual", "链路健康吗？", mode="replay:normal")
    Diagnostician(settings, store).run(normal_id, scenario("normal"))
    assert store.get(normal_id)["report"]["classification"] == "healthy"
    missing_id, _ = store.create("elevator-regtcn", "manual", "查看链路")
    Diagnostician(settings, store).run(missing_id, {})
    missing = store.get(missing_id)
    assert missing["report"]["classification"] == "insufficient_evidence"
    assert missing["report"]["missing"]


def test_deduplication_does_not_merge_closed_or_recheck(tmp_path):
    _, store = configured(tmp_path)
    first, created = store.create("elevator-regtcn", "flink_not_running", "自动告警", deduplicate=True)
    repeated, second_created = store.create("elevator-regtcn", "flink_not_running", "重复告警", deduplicate=True)
    assert created and not second_created and first == repeated
    store.set_status(first, "resolved")
    next_id, third_created = store.create("elevator-regtcn", "flink_not_running", "新告警", deduplicate=True)
    assert third_created and next_id != first


def test_prebuilt_open_source_agent_calls_registered_tool(tmp_path, monkeypatch):
    from langchain_core.language_models.fake_chat_models import FakeMessagesListChatModel
    from langchain_core.messages import AIMessage
    import streamdoctor.diagnosis as module

    class FakeToolModel(FakeMessagesListChatModel):
        def bind_tools(self, tools, **kwargs):
            return self

    fake = FakeToolModel(responses=[
        AIMessage(content="", tool_calls=[{"name": "flink_status", "args": {"query": "作业是否失败"}, "id": "call-1"}]),
        AIMessage(content="已经获取证据")])
    monkeypatch.setattr(module, "ChatOpenAI", lambda **kwargs: fake)
    settings, store = configured(tmp_path)
    settings = Settings(db_path=settings.db_path, topology_path=settings.topology_path,
                        model_api_key="test-only", model_name="fake-tool-model")
    incident_id, _ = store.create("elevator-regtcn", "manual", "检查 Flink 失败", mode="replay:job_failed")
    Diagnostician(settings, store).run(incident_id, scenario("job_failed"))
    saved = store.get(incident_id)
    assert saved["status"] == "open", saved
    assert [e["source"] for e in saved["evidence"]] == ["flink_status"]
    assert saved["report"]["classification"] == "flink_stopped"


def test_only_approved_feedback_enters_case_search(tmp_path):
    _, store = configured(tmp_path)
    first, _ = store.create("elevator-regtcn", "manual", "模型连接超时")
    second, _ = store.create("elevator-regtcn", "manual", "模型连接超时")
    store.set_status(first, "open", {"classification": "model_unhealthy"})
    store.set_status(second, "open", {"classification": "model_unhealthy"})
    store.feedback(first, {"useful": True, "actual_cause": "模型连接超时", "approved_for_knowledge": True})
    store.feedback(second, {"useful": True, "actual_cause": "模型连接超时", "approved_for_knowledge": False})
    cases = store.approved_cases("模型连接超时")
    assert len(cases) == 1
    assert cases[0]["id"] == "incident:" + first
