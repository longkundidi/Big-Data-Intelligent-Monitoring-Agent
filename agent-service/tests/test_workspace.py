import time
from pathlib import Path

from fastapi.testclient import TestClient

from streamdoctor import api
from streamdoctor.config import ROOT, Settings
from streamdoctor.diagnosis import Diagnostician
from streamdoctor.runtime import AgentRuntime
from streamdoctor.storage import Store


def client_for(tmp_path, monkeypatch, scan_root=None):
    settings = Settings(db_path=tmp_path / "workspace.db", topology_path=ROOT / "topology.json", poll_enabled=False,
                        model_api_key="", model_base_url="", model_name="", model_reasoning_effort="",
                        project_scan_root=scan_root)
    store = Store(settings.db_path)
    monkeypatch.setattr(api, "settings", settings)
    monkeypatch.setattr(api, "store", store)
    monkeypatch.setattr(api, "diagnostician", Diagnostician(settings, store))
    monkeypatch.setattr(api, "workspace_runtime", AgentRuntime(settings, store))
    return TestClient(api.app), store


def wait_run(client, run_id):
    for _ in range(160):
        result = client.get("/api/agent/runs/" + run_id).json()
        if result["status"] in {"completed", "failed", "cancelled"}:
            return result
        time.sleep(0.05)
    return result


def test_workspace_conversation_loop_and_sse(tmp_path, monkeypatch):
    client, store = client_for(tmp_path, monkeypatch)
    with client:
        conversation = client.post("/api/agent/projects/elevator-regtcn/conversations", json={"title": "链路检查"}).json()
        response = client.post("/api/agent/conversations/{}/messages".format(conversation["id"]), json={
            "content": "查看链路拓扑和积压趋势", "mode": "replay", "replay": "model_slow", "request_id": "one"})
        assert response.status_code == 200
        run = wait_run(client, response.json()["run_id"])
        assert run["status"] == "completed"
        assert len(run["evidence"]) >= 4
        assert {item["kind"] for item in run["artifacts"]} >= {"report", "chart"}
        assert any(event["type"] == "tool_finished" for event in run["events"])
        assert any(event["type"] == "investigation_decision" for event in run["events"])
        assert any(event["type"] == "investigation_stopped" for event in run["events"])
        report = next(event["payload"]["report"] for event in run["events"] if event["type"] == "message")
        assert report["classification"] == "model_slow"
        assert next(item for item in report["hypotheses"] if item["code"] == "model_slow")["status"] == "supported"
        assert "message" in client.get("/api/agent/runs/{}/events".format(run["id"])).text
        duplicate = client.post("/api/agent/conversations/{}/messages".format(conversation["id"]), json={
            "content": "查看链路拓扑和积压趋势", "request_id": "one"})
        assert duplicate.json()["duplicate"] is True


def test_model_catalog_and_conversation_selection_are_persisted(tmp_path, monkeypatch):
    client, _ = client_for(tmp_path, monkeypatch)
    with client:
        model_config = client.get("/api/agent/models").json()
        assert model_config["configured"] is False
        assert model_config["default_model"] == "gpt-5.6-luna"
        assert model_config["default_reasoning_effort"] == "medium"
        assert "api_key" not in model_config
        astra = next(item for item in model_config["models"] if item["id"] == "gpt-6-astra")
        assert astra["reasoning_efforts"] == ["low", "medium", "high", "xhigh", "max"]

        conversation = client.post("/api/agent/projects/elevator-regtcn/conversations", json={
            "title": "模型配置测试", "model_id": "gpt-5.6-sol", "reasoning_effort": "xhigh",
        }).json()
        assert conversation["model_id"] == "gpt-5.6-sol"
        assert conversation["reasoning_effort"] == "xhigh"

        updated = client.patch("/api/agent/conversations/{}".format(conversation["id"]), json={
            "model_id": "gpt-5.6-luna", "reasoning_effort": "low",
        })
        assert updated.status_code == 200
        assert updated.json()["model_id"] == "gpt-5.6-luna"
        assert updated.json()["reasoning_effort"] == "low"

        invalid = client.patch("/api/agent/conversations/{}".format(conversation["id"]), json={
            "model_id": "gpt-6-astra", "reasoning_effort": "none",
        })
        assert invalid.status_code == 422


def test_greeting_does_not_trigger_diagnostic_tools(tmp_path, monkeypatch):
    client, _ = client_for(tmp_path, monkeypatch)
    with client:
        conversation = client.post("/api/agent/projects/elevator-regtcn/conversations", json={"title": "普通问候"}).json()
        submitted = client.post("/api/agent/conversations/{}/messages".format(conversation["id"]), json={"content": "你好"}).json()
        run = wait_run(client, submitted["run_id"])
        assert run["status"] == "completed"
        assert not any(event["type"] == "tool_started" for event in run["events"])
        messages = client.get("/api/agent/conversations/{}".format(conversation["id"])).json()["messages"]
        assert "未执行诊断工具" in messages[-1]["context"]["report"]["summary"]


def test_mutating_action_is_audited_and_blocked_without_executor_config(tmp_path, monkeypatch):
    client, _ = client_for(tmp_path, monkeypatch)
    with client:
        conversation = client.post("/api/agent/projects/elevator-regtcn/conversations", json={"title": "执行边界"}).json()
        submitted = client.post("/api/agent/conversations/{}/messages".format(conversation["id"]), json={
            "content": "/restart flink", "mode": "replay", "replay": "normal",
        }).json()
        run = wait_run(client, submitted["run_id"])
        assert run["status"] == "completed"
        assert run["execution_actions"][0]["action"] == "restart"
        assert run["execution_actions"][0]["target"] == "flink"
        assert run["execution_actions"][0]["status"] == "blocked"
        assert [(task["agent_role"], task["task_type"], task["status"]) for task in run["subagent_tasks"]] == [
            ("reviewer", "review_evidence", "completed"),
            ("executor", "execute_action", "blocked"),
        ]
        assert [(message["from_agent"], message["to_agent"], message["message_type"])
                for message in run["agent_messages"]] == [
            ("coordinator", "reviewer", "delegation"),
            ("reviewer", "coordinator", "result"),
            ("coordinator", "executor", "delegation"),
            ("executor", "coordinator", "blocked"),
        ]
        assert any(event["type"] == "reviewer_finished" for event in run["events"])
        assert any(event["type"] == "executor_blocked" for event in run["events"])


def test_project_memory_requires_approval_and_is_isolated(tmp_path, monkeypatch):
    client, store = client_for(tmp_path, monkeypatch)
    with client:
        created = client.post("/api/agent/projects", json={"name": "第二项目"}).json()
        memory = client.post("/api/agent/projects/{}/memories".format(created["id"]), json={
            "kind": "fact", "content": "第二项目使用单独的 Topic"}).json()
        assert memory["status"] == "proposed"
        assert client.get("/api/agent/projects/{}/memories".format(created["id"])).json() == []
        approved = client.post("/api/agent/projects/{}/memories/{}/approve".format(created["id"], memory["id"]))
        assert approved.json()["status"] == "active"
        assert len(client.get("/api/agent/projects/{}/memories".format(created["id"])).json()) == 1
        assert client.get("/api/agent/projects/elevator-regtcn/memories").json() == []


def test_project_resources_and_navigation_data_are_scoped(tmp_path, monkeypatch):
    client, store = client_for(tmp_path, monkeypatch)
    with client:
        created = client.post("/api/agent/projects", json={
            "name": "独立链路", "description": "只属于这个项目", "topology_id": "empty"}).json()
        detail = client.get("/api/agent/projects/{}".format(created["id"])).json()
        assert detail["resources"] == []
        assert detail["topology"]["nodes"] == []

        resource = client.post("/api/agent/projects/{}/resources".format(created["id"]), json={
            "name": "测试 Kafka", "type": "kafka", "config": {"topic": "isolated"}}).json()
        topology = client.get("/api/agent/projects/{}/topology".format(created["id"])).json()
        assert topology["nodes"] == []
        assert topology["project_id"] == created["id"]
        spec = detail["spec"]["spec"]
        spec["nodes"].append({"id": "isolated-topic", "name": "隔离 Topic", "type": "kafka_topic",
                              "service_id": None, "config": {"topic": "isolated"}})
        published = client.post("/api/agent/projects/{}/spec/publish".format(created["id"]), json={
            "spec": spec, "change_summary": "添加隔离 Topic", "base_version_id": detail["spec"]["id"]})
        assert published.status_code == 200
        topology = client.get("/api/agent/projects/{}/topology".format(created["id"])).json()
        assert topology["nodes"][0]["id"] == "isolated-topic"
        assert client.post("/api/agent/projects/{}/resources/{}/test".format(created["id"], resource["id"])).json()["status"] == "unknown"
        assert client.get("/api/agent/projects/elevator-regtcn/resources").json()
        assert client.get("/api/agent/projects/{}/runbooks?query=积压".format(created["id"])).status_code == 200

        configuration = client.get("/api/agent/projects/{}/configuration".format(created["id"]))
        assert configuration.status_code == 200
        assert configuration.json()["status"] == "partial"
        missing = {item["key"] for item in configuration.json()["items"] if item["status"] == "missing"}
        assert {"kafka_servers", "flink_url", "model_health_url"}.issubset(missing)
        assert configuration.json()["files"][0]["location"] == "SQLite project_spec_versions"

        templated = client.post("/api/agent/projects", json={
            "name": "演示模板", "template": True, "topology_id": "elevator-regtcn"}).json()
        assert len(client.get("/api/agent/projects/{}/resources".format(templated["id"])).json()) == 3


def test_template_spec_history_restore_and_document_draft(tmp_path, monkeypatch):
    client, _ = client_for(tmp_path, monkeypatch)
    with client:
        templates = client.get("/api/agent/templates").json()
        assert {item["id"] for item in templates} >= {"blank", "kafka-lag", "kafka-flink", "flink-model", "flink-storage"}

        source = client.get("/api/agent/templates/flink-model").json()
        custom = client.post("/api/agent/templates", json={
            "name": "测试链路模板", "description": "版本测试", "category": "测试",
            "spec": source["spec"], "change_summary": "创建测试模板"}).json()
        changed_spec = custom["spec"]
        changed_spec["hosts"][0]["address"] = "10.0.0.8"
        updated = client.post("/api/agent/templates/{}/versions".format(custom["id"]), json={
            "spec": changed_spec, "change_summary": "填写服务器地址"}).json()
        assert updated["current_version"] == 2
        restored = client.post("/api/agent/templates/{}/versions/1/restore".format(custom["id"])).json()
        assert restored["current_version"] == 3

        project = client.post("/api/agent/projects", json={
            "name": "模板项目", "template_id": custom["id"], "template_version": 2}).json()
        detail = client.get("/api/agent/projects/{}".format(project["id"])).json()
        assert detail["spec"]["spec"]["hosts"][0]["address"] == "10.0.0.8"

        project_spec = detail["spec"]["spec"]
        project_spec["hosts"][0]["address"] = "10.0.0.9"
        published = client.post("/api/agent/projects/{}/spec/publish".format(project["id"]), json={
            "spec": project_spec, "change_summary": "迁移服务器", "base_version_id": detail["spec"]["id"]}).json()
        assert any(change["path"].endswith("/address") for change in published["changes"])

        document = client.post("/api/agent/projects/{}/documents".format(project["id"]), json={
            "filename": "deployment.md", "media_type": "text/markdown",
            "content": "备用服务器 10.0.0.20，Topic: elevator-input，消费组: monitor-group"}).json()
        assert document["parse_status"] == "parsed"
        assert "10.0.0.20" in {host["address"] for host in document["parse_result"]["spec"]["hosts"]}


def test_project_init_scans_safe_config_and_versions_only_the_project(tmp_path, monkeypatch):
    scan_root = tmp_path / "project"
    scan_root.mkdir()
    (scan_root / ".env").write_text("INPUT_TOPIC=must-not-be-read\n", encoding="utf-8")
    (scan_root / ".env.example").write_text("INPUT_TOPIC=elevator-input\nOUTPUT_TOPIC=elevator-output\n", encoding="utf-8")
    (scan_root / "compose.infrastructure.yml").write_text(
        """services:
  job:
    environment:
      KAFKA_SERVERS: kafka:9092
      FLINK_REST_URL: http://jobmanager:8081
      MODEL_HEALTH_URL: http://model:8000/healthz
      CONSUMER_GROUP: elevator-group
      FLINK_JOB_NAME: elevator-job
""", encoding="utf-8")
    client, _ = client_for(tmp_path, monkeypatch, scan_root)
    with client:
        project = client.post("/api/agent/projects", json={"name": "自动发现项目"}).json()
        conversation = client.post("/api/agent/projects/{}/conversations".format(project["id"]),
                                   json={"title": "初始化"}).json()
        initialized = client.post("/api/agent/projects/{}/initialize".format(project["id"]), json={
            "conversation_id": conversation["id"], "request_id": "init-one",
        })
        assert initialized.status_code == 200
        result = initialized.json()
        assert result["scan"]["detected_fields"] == 7
        assert ".env" not in {item["path"] for item in result["files"]}
        assert result["detected"]["INPUT_TOPIC"] == "elevator-input"
        detail = client.get("/api/agent/projects/{}".format(project["id"])).json()
        assert detail["spec"]["source_type"] == "scan"
        assert detail["spec"]["version"] == 2
        assert client.get("/api/agent/templates/blank").json()["current_version"] == 1
        messages = client.get("/api/agent/conversations/{}".format(conversation["id"])).json()["messages"]
        assert [item["role"] for item in messages] == ["user", "assistant"]
        assert "项目初始化完成" in messages[-1]["content"]
        duplicate = client.post("/api/agent/projects/{}/initialize".format(project["id"]), json={
            "conversation_id": conversation["id"], "request_id": "init-one",
        }).json()
        assert duplicate["duplicate"] is True
        assert client.get("/api/agent/projects/{}".format(project["id"])).json()["spec"]["version"] == 2


def test_conversation_management_and_cascade_delete(tmp_path, monkeypatch):
    client, store = client_for(tmp_path, monkeypatch)
    with client:
        conversation = client.post("/api/agent/projects/elevator-regtcn/conversations", json={"title": "待排查问题"}).json()
        renamed = client.patch("/api/agent/conversations/{}".format(conversation["id"]), json={"title": "模型延迟排查"})
        assert renamed.status_code == 200
        assert renamed.json()["title"] == "模型延迟排查"

        response = client.post("/api/agent/conversations/{}/messages".format(conversation["id"]), json={
            "content": "检查模型延迟", "mode": "replay", "replay": "model_slow"})
        run = wait_run(client, response.json()["run_id"])
        assert run["status"] == "completed"

        archived = client.patch("/api/agent/conversations/{}".format(conversation["id"]), json={"status": "archived"})
        assert archived.json()["status"] == "archived"
        visible = client.get("/api/agent/projects/elevator-regtcn/conversations").json()
        assert conversation["id"] not in {item["id"] for item in visible}
        all_items = client.get("/api/agent/projects/elevator-regtcn/conversations?include_archived=true").json()
        saved = next(item for item in all_items if item["id"] == conversation["id"])
        assert saved["message_count"] == 2

        assert client.patch("/api/agent/conversations/{}".format(conversation["id"]), json={"status": "active"}).status_code == 200
        assert client.delete("/api/agent/conversations/{}".format(conversation["id"])).json()["deleted"] is True
        assert client.get("/api/agent/conversations/{}".format(conversation["id"])).status_code == 404
        assert store.run(run["id"]) is None
        assert store.run_events(run["id"]) == []
