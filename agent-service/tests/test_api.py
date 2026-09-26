import time

from fastapi.testclient import TestClient

from streamdoctor import api
from streamdoctor.config import ROOT, Settings
from streamdoctor.diagnosis import Diagnostician
from streamdoctor.storage import Store


def test_api_replay_recheck_feedback_and_sse(tmp_path, monkeypatch):
    settings = Settings(db_path=tmp_path / "api.db", topology_path=ROOT / "topology.json",
                        model_api_key="", model_name="", poll_enabled=False)
    store = Store(settings.db_path)
    monkeypatch.setattr(api, "settings", settings)
    monkeypatch.setattr(api, "store", store)
    monkeypatch.setattr(api, "diagnostician", Diagnostician(settings, store))
    with TestClient(api.app) as client:
        assert client.get("/api/agent/topology").json()["id"] == "elevator-regtcn"
        scenarios = client.get("/api/agent/replays").json()
        assert len(scenarios) == 7 and {item["id"] for item in scenarios} == set(api.REPLAY_NAMES)
        assert all("expected" not in item for item in scenarios)
        created = client.post("/api/agent/incidents", json={"question": "为什么没有结果？", "replay": "model_slow"})
        assert created.status_code == 200
        incident_id = created.json()["id"]
        for _ in range(100):
            result = client.get("/api/agent/incidents/" + incident_id).json()
            if result["status"] in ("open", "failed"):
                break
            time.sleep(0.05)
        assert result["report"]["classification"] == "model_slow", result
        response = client.get("/api/agent/incidents/" + incident_id + "/events")
        assert "event: tool" in response.text
        assert client.post("/api/agent/incidents/" + incident_id + "/feedback", json={
            "useful": True, "actual_cause": "模型响应变慢", "proposed_for_knowledge": True,
            "approved_for_knowledge": True}).status_code == 200
        assert store.get(incident_id)["feedback"]["approved_for_knowledge"] is False
        assert not store.approved_cases("模型响应变慢")
        assert client.post("/api/agent/incidents/" + incident_id + "/review", json={"approved": True}).status_code == 200
        assert len(store.approved_cases("模型响应变慢")) == 1
        assert any(event["kind"] == "review" for event in store.events(incident_id))
        recheck = client.post("/api/agent/incidents/" + incident_id + "/recheck", json={"replay_after": "normal"})
        assert recheck.status_code == 200
        recheck_id = recheck.json()["id"]
        for _ in range(100):
            recovered = client.get("/api/agent/incidents/" + recheck_id).json()
            if recovered["status"] in ("open", "failed"):
                break
            time.sleep(0.05)
        assert recovered["report"]["classification"] == "healthy", recovered
        comparison = recovered["report"]["comparison"]
        assert comparison["before"] == "model_slow" and comparison["after"] == "healthy" and comparison["recovered"]
        assert comparison["lag_before"] == 220 and comparison["lag_after"] == 0
        assert client.get("/api/agent/incidents/" + incident_id).json()["status"] == "resolved"
        assert client.post("/api/agent/incidents/" + recheck_id + "/recheck", json={"replay_after": "../../outside"}).status_code == 400
        assert client.post("/api/agent/incidents", json={"question": "测试", "replay": "../../outside"}).status_code == 400


def test_observability_recheck_does_not_claim_business_recovery(tmp_path, monkeypatch):
    settings = Settings(db_path=tmp_path / "unknown.db", topology_path=ROOT / "topology.json",
                        model_api_key="", model_name="", poll_enabled=False)
    store = Store(settings.db_path)
    monkeypatch.setattr(api, "settings", settings)
    monkeypatch.setattr(api, "store", store)
    monkeypatch.setattr(api, "diagnostician", Diagnostician(settings, store))
    with TestClient(api.app) as client:
        created = client.post("/api/agent/incidents", json={"question": "观测失败", "replay": "observability_down"}).json()
        incident_id = created["id"]
        for _ in range(100):
            incident = client.get("/api/agent/incidents/" + incident_id).json()
            if incident["status"] == "open":
                break
            time.sleep(0.05)
        assert incident["report"]["classification"] == "insufficient_evidence"
        recheck_id = client.post("/api/agent/incidents/" + incident_id + "/recheck",
                                 json={"replay_after": "normal"}).json()["id"]
        for _ in range(100):
            recheck = client.get("/api/agent/incidents/" + recheck_id).json()
            if recheck["status"] == "open":
                break
            time.sleep(0.05)
        comparison = recheck["report"]["comparison"]
        assert comparison["observation_restored"] and not comparison["recovered"]
        assert store.get(incident_id)["status"] == "open"


def test_manual_collection_returns_runtime_issues(tmp_path, monkeypatch):
    settings = Settings(db_path=tmp_path / "collect.db", topology_path=ROOT / "topology.json",
                        model_api_key="", model_name="", poll_enabled=False)
    store = Store(settings.db_path)
    monkeypatch.setattr(api, "settings", settings)
    monkeypatch.setattr(api, "store", store)

    class FakeCollectors:
        def __init__(self, settings, store):
            self.chain = settings.topology()

        def collect(self, name):
            payloads = {
                "flink_status": {"state": "NOT_FOUND", "job_name": "algorithm_REGTCN",
                                 "cluster": {"version": "1.17.2", "taskmanagers": 1,
                                             "slots_total": 2, "slots_available": 2}},
                "flink_metrics": {"state": "NOT_FOUND", "vertices": []},
                "kafka_offsets": {"total_lag": None, "topics": {
                    "dc_algorithm_REGTCN": {"exists": True, "partition_count": 3, "latest_total": 0},
                    "dc_algorithm_sink_REGTCN": {"exists": True, "partition_count": 3, "latest_total": 0}}},
                "model_health": {"error": "未配置模型健康端点"},
            }
            status = "unavailable" if name == "model_health" else "ok"
            return status, payloads[name], "start", "end"

    monkeypatch.setattr(api, "Collectors", FakeCollectors)
    with TestClient(api.app) as client:
        response = client.post("/api/agent/observations/collect")
        assert response.status_code == 200
        result = response.json()
        assert result["collected"]["flink_status"] == "ok"
        assert result["runtime"]["nodes"]["input"]["state"] == "healthy"
        assert result["runtime"]["nodes"]["job"]["state"] == "critical"
        assert {issue["code"] for issue in result["runtime"]["issues"]} == {
            "offset_unknown", "flink_not_running", "model_observation_missing"}
