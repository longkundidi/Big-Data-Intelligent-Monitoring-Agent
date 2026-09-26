import asyncio
import hashlib
import ipaddress
import json
import re
import uuid
from contextlib import asynccontextmanager

import jwt
from fastapi import Depends, FastAPI, Header, HTTPException, Request
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from .collectors import Collectors
from .benchmark import evaluate as evaluate_benchmark
from .configuration import inspect_configuration
from .config import ROOT, Settings
from .diagnosis import Diagnostician
from .models import (
    ConversationCreate, ConversationUpdate, MemoryCreate, MessageCreate, ProjectCreate, ProjectDocumentCreate,
    ProjectInitialize,
    ProjectSpecProposal, ProjectSpecPublish, ProjectUpdate, ResourceCreate, ResourceUpdate,
    TemplateClone, TemplateCreate, TemplatePublish,
)
from .model_catalog import public_catalog, validate_selection
from .project_init import discover_project
from .runbooks import search as search_runbooks
from .runtime import AgentRuntime
from .specs import blank_spec, infer_spec_from_text, normalize_spec, spec_topology
from .storage import Store


settings = Settings()
settings.db_path.parent.mkdir(parents=True, exist_ok=True)
store = Store(settings.db_path)
store.migrate_conversation_model_ids({"gpt-6-sol": "gpt-5.6-sol", "gpt-6-luna": "gpt-5.6-luna"})
diagnostician = Diagnostician(settings, store)
workspace_runtime = AgentRuntime(settings, store)
REPLAY_NAMES = ("model_slow", "normal", "job_failed", "source_stopped", "invalid_data", "observability_down", "lag_only")


async def require_access(request: Request):
    if settings.jwks_url:
        token = request.headers.get("authorization", "").removeprefix("Bearer ")
        try:
            key = jwt.PyJWKClient(settings.jwks_url).get_signing_key_from_jwt(token)
            request.state.claims = jwt.decode(token, key.key, algorithms=["RS256"], issuer=settings.jwt_issuer,
                                              audience=settings.jwt_audience)
        except Exception:
            raise HTTPException(401, "身份验证失败")
    elif request.client and request.client.host not in ("127.0.0.1", "::1", "testclient"):
        try:
            client_ip = ipaddress.ip_address(request.client.host)
            trusted = any(client_ip in ipaddress.ip_network(network) for network in settings.trusted_networks)
        except ValueError:
            trusted = False
        if not trusted:
            raise HTTPException(403, "未配置 AGENT_JWKS_URL 时只允许本机或受信网络访问")


def read_replay(name):
    if name not in REPLAY_NAMES:
        raise HTTPException(400, "未知的回放场景")
    return json.loads((ROOT / "replays" / (name + ".json")).read_text(encoding="utf-8"))


def runtime_instance():
    global workspace_runtime
    if workspace_runtime.settings is not settings or workspace_runtime.store is not store:
        workspace_runtime = AgentRuntime(settings, store)
    return workspace_runtime


def public_resource(resource):
    if not resource:
        return None
    safe = {**resource, "config": dict(resource.get("config") or {})}
    for key in ("password", "secret", "token", "api_key", "secret_key"):
        if key in safe["config"]:
            safe["config"][key] = "********"
    return safe


def project_topology(project_id):
    """Return a topology scoped to the selected project and its resources."""
    project = store.project(project_id)
    if not project:
        return None
    current_spec = store.project_spec(project_id)
    if current_spec:
        topology = spec_topology(current_spec["spec"], project_id)
        topology["spec_version_id"] = current_spec["id"]
        topology["spec_version"] = current_spec["version"]
        return topology
    resources = store.resources(project_id)
    if project["topology_id"] == settings.topology().get("id"):
        topology = json.loads(json.dumps(settings.topology(), ensure_ascii=False))
    else:
        topology = {"id": project["topology_id"], "name": project["name"] + "链路", "nodes": [], "edges": []}
        previous = None
        for resource in resources:
            node_id = resource["id"]
            topology["nodes"].append({"id": node_id, "label": resource["name"], "type": resource["type"], "status": resource["status"]})
            if previous:
                topology["edges"].append({"from": previous, "to": node_id})
            previous = node_id
    resource_ids = {item["id"] for item in resources}
    for node in topology.get("nodes", []):
        registry_key = node.get("registry_key")
        node["resource_id"] = registry_key if registry_key in resource_ids else node.get("resource_id")
    topology["project_id"] = project_id
    topology["resources"] = [public_resource(item) for item in resources]
    return topology


def run_incident(incident_id, replay=None):
    diagnostician.run(incident_id, replay)


OBSERVATION_SOURCES = ("flink_status", "flink_metrics", "kafka_offsets", "model_health")


async def collect_observations():
    collector = Collectors(settings, store)
    observations = {}
    for name in OBSERVATION_SOURCES:
        status, payload, _, _ = await asyncio.to_thread(collector.collect, name)
        store.add_sample(collector.chain["id"], name, status, payload)
        observations[name] = (status, payload)
    store.prune_samples()
    return collector.chain["id"], observations


def latest_samples(chain_id):
    return {name: store.samples(chain_id, name, 10) for name in OBSERVATION_SOURCES}


def runtime_view(chain, samples):
    latest = {name: values[-1] if values else None for name, values in samples.items()}
    node_states = {node["id"]: {"state": "unknown", "label": "暂无观测"} for node in chain["nodes"]}
    issues = []

    kafka = latest.get("kafka_offsets")
    if kafka and kafka["status"] == "ok":
        payload = kafka["payload"]
        topics = payload.get("topics", {})
        for node_id, topic_name in (("input", chain["input_topic"]), ("output", chain["output_topic"])):
            topic = topics.get(topic_name, {})
            if topic.get("exists"):
                node_states[node_id] = {"state": "healthy", "label": "Topic 可用"}
            else:
                node_states[node_id] = {"state": "critical", "label": "Topic 不存在"}
                issues.append({"code": "topic_missing", "severity": "critical", "node_id": node_id,
                               "title": "Kafka Topic 不存在", "detail": topic_name})
        if payload.get("total_lag") is None:
            issues.append({"code": "offset_unknown", "severity": "warning", "node_id": "input",
                           "title": "消费组尚无已提交位点", "detail": "当前无法仅凭 Kafka 位点计算积压，需结合 Flink 指标判断。"})
        elif payload.get("total_lag", 0) > 0:
            issues.append({"code": "consumer_lag", "severity": "warning", "node_id": "input",
                           "title": "Kafka 存在消费积压", "detail": "当前积压 {} 条。".format(payload["total_lag"])})
    elif kafka:
        issues.append({"code": "kafka_unavailable", "severity": "unknown", "node_id": "input",
                       "title": "Kafka 观测不可用", "detail": kafka["payload"].get("error", "采集失败")})

    flink = latest.get("flink_status")
    if flink and flink["status"] == "ok":
        state = flink["payload"].get("state")
        if state == "RUNNING":
            node_states["job"] = {"state": "healthy", "label": "作业运行中"}
        else:
            node_states["job"] = {"state": "critical", "label": "作业未运行"}
            issues.append({"code": "flink_not_running", "severity": "critical", "node_id": "job",
                           "title": "目标 Flink 作业未运行", "detail": "期望作业 {}，当前状态 {}。".format(
                               chain["flink_job_name"], state or "未知")})
    elif flink:
        issues.append({"code": "flink_unavailable", "severity": "unknown", "node_id": "job",
                       "title": "Flink 观测不可用", "detail": flink["payload"].get("error", "采集失败")})

    model = latest.get("model_health")
    if model and model["status"] == "ok":
        model_state = model["payload"].get("state", "unknown")
        state = "healthy" if model_state == "healthy" else "warning" if model_state == "degraded" else "critical"
        node_states["model"] = {"state": state, "label": "模型服务" + model_state}
        if model_state != "healthy":
            issues.append({"code": "model_" + model_state, "severity": state, "node_id": "model",
                           "title": "模型服务状态异常", "detail": "当前状态 {}。".format(model_state)})
    elif model:
        issues.append({"code": "model_observation_missing", "severity": "unknown", "node_id": "model",
                       "title": "模型服务尚未接入观测", "detail": model["payload"].get("error", "未配置健康端点")})

    observed_at = max((item["collected_at"] for item in latest.values() if item), default=None)
    priority = {"critical": 3, "warning": 2, "unknown": 1}
    overall = max((issue["severity"] for issue in issues), key=lambda value: priority.get(value, 0), default="healthy")
    return {"overall": overall, "observed_at": observed_at, "nodes": node_states, "issues": issues,
            "flink": (flink or {}).get("payload"), "kafka": (kafka or {}).get("payload"),
            "model": (model or {}).get("payload")}


async def monitor():
    while True:
        try:
            chain_id, observations = await collect_observations()
            state = observations["flink_status"]
            if state[0] == "ok" and state[1].get("state") not in ("RUNNING", None):
                kind = "flink_not_running"
            else:
                model = observations["model_health"]
                kind = "model_unhealthy" if model[0] == "ok" and model[1].get("state") in ("unhealthy", "degraded") else None
                kafka = observations["kafka_offsets"]
                if not kind and kafka[0] == "ok":
                    lag = kafka[1].get("total_lag")
                    previous = kafka[1].get("previous_total_lag")
                    if lag is not None and previous is not None and lag - previous >= 10:
                        kind = "lag_increasing"
            if kind:
                incident_id, created = store.create(chain_id, kind, "自动检测到{}，请诊断数据链路。".format(kind), deduplicate=True)
                if created:
                    asyncio.create_task(asyncio.to_thread(run_incident, incident_id))
        except Exception:
            # Failures in the poller must never be interpreted as healthy observations.
            pass
        await asyncio.sleep(15)


@asynccontextmanager
async def lifespan(app):
    for incident in store.list(100):
        if incident["status"] in ("running", "queued"):
            replay = read_replay(incident["mode"].removeprefix("replay:")) if incident["mode"].startswith("replay:") else None
            asyncio.create_task(asyncio.to_thread(run_incident, incident["id"], replay))
    task = asyncio.create_task(monitor()) if settings.poll_enabled else None
    yield
    if task:
        task.cancel()
        try:
            await task
        except asyncio.CancelledError:
            pass


app = FastAPI(title="StreamDoctor", version="0.1.0", lifespan=lifespan,
              dependencies=[Depends(require_access)])


class CreateIncident(BaseModel):
    question: str = Field(min_length=2, max_length=1000)
    chain_id: str = "elevator-regtcn"
    replay: str | None = None


class Feedback(BaseModel):
    useful: bool
    actual_cause: str = Field(default="", max_length=500)
    notes: str = Field(default="", max_length=1000)
    proposed_for_knowledge: bool = False


class Review(BaseModel):
    approved: bool


class RecheckRequest(BaseModel):
    replay_after: str | None = None


@app.get("/health")
def health():
    return {"status": "ok", "poll_enabled": settings.poll_enabled}


@app.get("/api/agent/topology")
def topology():
    chain = settings.topology()
    samples = latest_samples(chain["id"])
    return {**chain, "poll_enabled": settings.poll_enabled, "samples": samples,
            "runtime": runtime_view(chain, samples)}


@app.post("/api/agent/observations/collect")
async def collect_now():
    chain_id, observations = await collect_observations()
    chain = settings.topology()
    samples = latest_samples(chain_id)
    return {"collected": {name: status for name, (status, _) in observations.items()},
            "runtime": runtime_view(chain, samples)}


@app.get("/api/agent/replays")
def replays():
    return [{"id": name, "name": read_replay(name)["name"]} for name in REPLAY_NAMES]


@app.get("/api/agent/incidents")
def list_incidents():
    return store.list()


@app.post("/api/agent/incidents")
async def create_incident(body: CreateIncident):
    if body.chain_id != settings.topology()["id"]:
        raise HTTPException(400, "未知链路")
    replay = read_replay(body.replay) if body.replay else None
    incident_id, _ = store.create(body.chain_id, "manual", body.question,
                                  mode="replay:" + body.replay if body.replay else "live")
    asyncio.create_task(asyncio.to_thread(run_incident, incident_id, replay))
    return {"id": incident_id, "status": "queued"}


@app.get("/api/agent/incidents/{incident_id}")
def incident(incident_id: str):
    found = store.get(incident_id)
    if not found:
        raise HTTPException(404, "诊断事件不存在")
    return found


@app.get("/api/agent/incidents/{incident_id}/events")
async def events(incident_id: str, last_event_id: str | None = Header(default=None)):
    if store.get(incident_id) is None:
        raise HTTPException(404, "诊断事件不存在")
    try:
        last = max(0, int(last_event_id or 0))
    except ValueError:
        last = 0

    async def generate():
        nonlocal last
        while True:
            for item in await asyncio.to_thread(store.events, incident_id, last):
                last = item["seq"]
                yield "id: {}\nevent: {}\ndata: {}\n\n".format(last, item["kind"], json.dumps(item, ensure_ascii=False))
            current = await asyncio.to_thread(store.get, incident_id)
            if current["status"] in ("open", "failed", "resolved"):
                break
            yield ": keepalive\n\n"
            await asyncio.sleep(1)

    return StreamingResponse(generate(), media_type="text/event-stream",
                             headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"})


@app.post("/api/agent/incidents/{incident_id}/recheck")
async def recheck(incident_id: str, body: RecheckRequest | None = None):
    original = store.get(incident_id)
    if not original:
        raise HTTPException(404, "诊断事件不存在")
    if original["status"] != "open":
        raise HTTPException(409, "请等待诊断完成")
    if body and body.replay_after and not original["mode"].startswith("replay:"):
        raise HTTPException(400, "实时诊断不能切换为回放数据")
    replay_name = (body.replay_after if body and body.replay_after else original["mode"].removeprefix("replay:")) if original["mode"].startswith("replay:") else None
    replay = read_replay(replay_name) if replay_name else None
    new_id, _ = store.create(original["chain_id"], "recheck", "恢复复查：" + original["question"],
                             mode="replay:" + replay_name if replay_name else "live", parent_id=incident_id)
    asyncio.create_task(asyncio.to_thread(run_incident, new_id, replay))
    return {"id": new_id, "status": "queued"}


@app.post("/api/agent/incidents/{incident_id}/feedback")
def feedback(incident_id: str, body: Feedback):
    if not store.get(incident_id):
        raise HTTPException(404, "诊断事件不存在")
    store.feedback(incident_id, {**body.model_dump(), "approved_for_knowledge": False})
    return {"saved": True}


@app.post("/api/agent/incidents/{incident_id}/review")
def review(incident_id: str, body: Review, request: Request):
    if settings.jwks_url:
        claims = getattr(request.state, "claims", {})
        scopes = set(claims.get("scope", "").split())
        if "agent:review" not in scopes:
            raise HTTPException(403, "缺少案例审核权限")
    found = store.get(incident_id)
    if not found or not found["feedback"] or not found["feedback"].get("actual_cause"):
        raise HTTPException(409, "请先提交实际原因")
    store.review_feedback(incident_id, body.approved)
    return {"approved": body.approved}


# Template and specification API -------------------------------------------
@app.get("/api/agent/templates")
def workspace_templates(include_archived: bool = False):
    return store.templates(include_archived)


@app.post("/api/agent/templates")
def workspace_create_template(body: TemplateCreate):
    template_id = re.sub(r"[^a-z0-9-]+", "-", body.name.lower()).strip("-") or str(uuid.uuid4())
    if store.template(template_id):
        template_id += "-" + uuid.uuid4().hex[:6]
    try:
        return store.create_template(template_id, body.name, body.description, body.category, body.spec, body.change_summary)
    except ValueError as exc:
        raise HTTPException(422, str(exc))


@app.get("/api/agent/templates/{template_id}")
def workspace_template(template_id: str, version: int | None = None):
    found = store.template(template_id, version)
    if not found:
        raise HTTPException(404, "模板不存在")
    return {**found, "versions": store.template_versions(template_id)}


@app.post("/api/agent/templates/{template_id}/versions")
def workspace_publish_template(template_id: str, body: TemplatePublish):
    found = store.template(template_id)
    if not found:
        raise HTTPException(404, "模板不存在")
    if found["builtin"]:
        raise HTTPException(409, "内置模板不能直接修改，请先复制模板")
    try:
        return store.publish_template(template_id, body.spec, body.change_summary,
                                      {"name": body.name, "description": body.description, "category": body.category})
    except ValueError as exc:
        raise HTTPException(422, str(exc))


@app.post("/api/agent/templates/{template_id}/versions/{version}/restore")
def workspace_restore_template(template_id: str, version: int):
    current = store.template(template_id)
    source = store.template(template_id, version)
    if not current or not source:
        raise HTTPException(404, "模板或版本不存在")
    if current["builtin"]:
        raise HTTPException(409, "内置模板不能直接修改，请先复制模板")
    return store.publish_template(
        template_id,
        source["spec"],
        "从 v{} 恢复".format(version),
        {"name": current["name"], "description": current["description"], "category": current["category"]},
    )


@app.post("/api/agent/templates/{template_id}/clone")
def workspace_clone_template(template_id: str, body: TemplateClone):
    source = store.template(template_id)
    if not source:
        raise HTTPException(404, "模板不存在")
    clone_id = re.sub(r"[^a-z0-9-]+", "-", body.name.lower()).strip("-") or str(uuid.uuid4())
    if store.template(clone_id):
        clone_id += "-" + uuid.uuid4().hex[:6]
    return store.create_template(clone_id, body.name, body.description or source["description"], source["category"],
                                 source["spec"], "复制自 {} v{}".format(template_id, source["current_version"]))


@app.delete("/api/agent/templates/{template_id}")
def workspace_archive_template(template_id: str):
    found = store.template(template_id)
    if not found:
        raise HTTPException(404, "模板不存在")
    if found["builtin"]:
        raise HTTPException(409, "内置模板不能归档")
    return store.set_template_status(template_id, "archived")


# Workspace API -------------------------------------------------------------
@app.get("/api/agent/projects")
def workspace_projects():
    return store.projects()


@app.post("/api/agent/projects")
def workspace_create_project(body: ProjectCreate):
    project_id = re.sub(r"[^a-z0-9-]+", "-", body.name.lower()).strip("-") or str(uuid.uuid4())
    if store.project(project_id):
        project_id += "-" + uuid.uuid4().hex[:6]
    template_id = body.template_id or ("flink-model" if body.template else None)
    template = store.template(template_id, body.template_version) if template_id else None
    if template_id and not template:
        raise HTTPException(404, "模板不存在")
    topology_id = template_id or body.topology_id or "empty"
    store.create_project(project_id, body.name, body.description, topology_id)
    if template:
        spec = normalize_spec(template["spec"])
        spec["name"] = body.name + "链路"
        version = store.publish_project_spec(project_id, spec, "从模板创建项目", "template",
                                             "{}:v{}".format(template_id, template["version_data"]["version"]),
                                             template_id, template["version_data"]["id"])
        hosts = {item["id"]: item for item in spec["hosts"]}
        for service in spec["services"]:
            host = hosts.get(service.get("host_id"), {})
            config = {**(service.get("config") or {}), "spec_service_id": service["id"],
                      "host": host.get("address", ""), "port": service.get("port"),
                      "protocol": service.get("protocol"), "version": service.get("version", "")}
            store.create_resource(project_id, str(uuid.uuid4()), service.get("name", service["id"]), service.get("type", "custom"), config)
    else:
        version = store.publish_project_spec(project_id, blank_spec(body.name + "链路"), "创建空白项目", "manual")
    return {**store.project(project_id), "spec_version_id": version["id"]}


@app.get("/api/agent/projects/{project_id}")
def workspace_project(project_id: str):
    project = store.project(project_id)
    if not project:
        raise HTTPException(404, "项目不存在")
    return {**project, "topology": project_topology(project_id), "spec": store.project_spec(project_id),
            "spec_versions": store.project_spec_versions(project_id),
            "resources": [public_resource(item) for item in store.resources(project_id)],
            "conversations": store.conversations(project_id, 20), "memories": store.memories(project_id)}


@app.get("/api/agent/projects/{project_id}/topology")
def workspace_project_topology(project_id: str):
    topology = project_topology(project_id)
    if not topology:
        raise HTTPException(404, "项目不存在")
    return topology


@app.get("/api/agent/projects/{project_id}/spec")
def workspace_project_spec(project_id: str, version_id: str | None = None):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    found = store.project_spec(project_id, version_id)
    if not found:
        raise HTTPException(404, "项目规格不存在")
    return found


@app.get("/api/agent/projects/{project_id}/spec/versions")
def workspace_project_spec_versions(project_id: str):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return store.project_spec_versions(project_id)


@app.post("/api/agent/projects/{project_id}/spec/publish")
def workspace_publish_project_spec(project_id: str, body: ProjectSpecPublish):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    current = store.project_spec(project_id)
    if body.base_version_id and current and body.base_version_id != current["id"]:
        raise HTTPException(409, "项目规格已被更新，请重新加载后比较变更")
    try:
        published = store.publish_project_spec(project_id, body.spec, body.change_summary,
                                               body.source_type, body.source_ref)
    except ValueError as exc:
        raise HTTPException(422, str(exc))
    return published


@app.post("/api/agent/projects/{project_id}/spec/versions/{version_id}/restore")
def workspace_restore_project_spec(project_id: str, version_id: str):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    target = store.project_spec(project_id, version_id)
    if not target:
        raise HTTPException(404, "规格版本不存在")
    return store.publish_project_spec(project_id, target["spec"], "恢复 v{} 的规格内容".format(target["version"]),
                                      "restore", version_id, target.get("template_id"), target.get("template_version_id"))


@app.post("/api/agent/projects/{project_id}/spec/propose")
def workspace_propose_project_spec(project_id: str, body: ProjectSpecProposal):
    current = store.project_spec(project_id)
    if not current:
        raise HTTPException(404, "项目规格不存在")
    return infer_spec_from_text(body.content, current["spec"])


@app.post("/api/agent/projects/{project_id}/initialize")
def workspace_initialize_project(project_id: str, body: ProjectInitialize):
    project = store.project(project_id)
    current = store.project_spec(project_id)
    if not project or not current:
        raise HTTPException(404, "项目或项目规格不存在")
    if not settings.project_scan_root:
        raise HTTPException(409, "未配置 AGENT_PROJECT_SCAN_ROOT，无法扫描项目目录")
    conversation = None
    if body.conversation_id:
        conversation = store.conversation(body.conversation_id)
        if not conversation or conversation["project_id"] != project_id:
            raise HTTPException(404, "当前项目中不存在该会话")
        if store.active_run(body.conversation_id):
            raise HTTPException(409, "会话正在执行诊断，请完成或停止后再初始化")
        _, created = store.add_message(body.conversation_id, "user", "/init",
                                       {"command": "init"}, body.request_id)
        if not created:
            return {"duplicate": True, "spec": current,
                    "configuration": inspect_configuration(settings, store, project_id)}
    try:
        result = discover_project(settings.project_scan_root, current["spec"], project["name"])
        published = current
        if result["changes"]:
            published = store.publish_project_spec(
                project_id, result["spec"],
                "通过 /init 扫描项目目录更新配置",
                "scan", "project-directory",
            )
    except (OSError, ValueError) as exc:
        raise HTTPException(422, str(exc)) from exc

    configuration = inspect_configuration(settings, store, project_id)
    summary = result["summary"]
    content = (
        "项目初始化完成：扫描 {scanned_files} 个配置文件，识别 {detected_fields} 个链路字段，"
        "生成 {changes} 项规格变化；当前规格为 v{version}，配置完整度 {score}%。"
    ).format(**summary, version=published["version"], score=configuration["score"])
    if not result["changes"]:
        content += " 未发现需要发布的新变化。"
    if conversation:
        store.add_message(body.conversation_id, "assistant", content, {
            "command": "init", "scan": summary,
            "detected": result["detected"], "files": result["files"],
            "spec_version": published["version"], "configuration": configuration,
        })
    return {
        "duplicate": False, "message": content, "scan": summary,
        "detected": result["detected"], "files": result["files"],
        "spec": published, "configuration": configuration,
    }


@app.get("/api/agent/projects/{project_id}/documents")
def workspace_project_documents(project_id: str):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return [{key: value for key, value in item.items() if key != "content"} for item in store.documents(project_id)]


@app.post("/api/agent/projects/{project_id}/documents")
def workspace_project_document(project_id: str, body: ProjectDocumentCreate):
    current = store.project_spec(project_id)
    if not current:
        raise HTTPException(404, "项目规格不存在")
    suffix = body.filename.rsplit(".", 1)[-1].lower() if "." in body.filename else "txt"
    if suffix not in {"txt", "md", "json", "yaml", "yml"}:
        raise HTTPException(415, "首版支持 TXT、Markdown、JSON 和 YAML 文本文档")
    parsed = infer_spec_from_text(body.content, current["spec"])
    document = store.save_document(project_id, str(uuid.uuid4()), body.filename, body.media_type,
                                   hashlib.sha256(body.content.encode("utf-8")).hexdigest(), body.content, parsed)
    return {key: value for key, value in document.items() if key != "content"}


@app.post("/api/agent/projects/{project_id}/save-as-template")
def workspace_project_save_template(project_id: str, body: TemplateClone):
    current = store.project_spec(project_id)
    if not current:
        raise HTTPException(404, "项目规格不存在")
    template_id = re.sub(r"[^a-z0-9-]+", "-", body.name.lower()).strip("-") or str(uuid.uuid4())
    if store.template(template_id):
        template_id += "-" + uuid.uuid4().hex[:6]
    spec = normalize_spec(current["spec"])
    for host in spec["hosts"]:
        host["address"] = ""
    for service in spec["services"]:
        service["credential_ref"] = None
    return store.create_template(template_id, body.name, body.description or "从项目另存的模板", "自定义", spec,
                                 "从项目 {} 规格 v{} 创建".format(project_id, current["version"]))


@app.patch("/api/agent/projects/{project_id}")
def workspace_update_project(project_id: str, body: ProjectUpdate):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return store.update_project(project_id, body.model_dump(exclude_none=True))


@app.delete("/api/agent/projects/{project_id}")
def workspace_archive_project(project_id: str):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return store.update_project(project_id, {"status": "archived"})


@app.get("/api/agent/projects/{project_id}/resources")
def workspace_resources(project_id: str):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return [public_resource(item) for item in store.resources(project_id)]


@app.get("/api/agent/projects/{project_id}/configuration")
def workspace_configuration(project_id: str):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return inspect_configuration(settings, store, project_id)


@app.post("/api/agent/projects/{project_id}/resources")
def workspace_create_resource(project_id: str, body: ResourceCreate):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    resource = store.create_resource(project_id, str(uuid.uuid4()), body.name, body.type, body.config)
    return public_resource(resource)


@app.patch("/api/agent/projects/{project_id}/resources/{resource_id}")
def workspace_update_resource(project_id: str, resource_id: str, body: ResourceUpdate):
    resource = next((item for item in store.resources(project_id) if item["id"] == resource_id), None)
    if not resource:
        raise HTTPException(404, "资源不存在")
    values = body.model_dump(exclude_none=True)
    if "config" in values:
        values["config"] = json.dumps(values["config"], ensure_ascii=False)
    updated = store.update_resource(project_id, resource_id, values)
    return public_resource(updated)


@app.delete("/api/agent/projects/{project_id}/resources/{resource_id}")
def workspace_archive_resource(project_id: str, resource_id: str):
    resource = next((item for item in store.resources(project_id) if item["id"] == resource_id), None)
    if not resource:
        raise HTTPException(404, "资源不存在")
    return public_resource(store.update_resource(project_id, resource_id, {"status": "archived"}))


@app.post("/api/agent/projects/{project_id}/resources/{resource_id}/test")
def workspace_test_resource(project_id: str, resource_id: str):
    resource = next((item for item in store.resources(project_id) if item["id"] == resource_id), None)
    if not resource:
        raise HTTPException(404, "资源不存在")
    # Connection tests are intentionally read-only and report configuration gaps
    # instead of attempting arbitrary addresses supplied by a model.
    required = {"kafka": "KAFKA_SERVERS", "flink": "FLINK_REST_URL", "model": "MODEL_HEALTH_URL"}
    setting_name = required.get(resource["type"])
    setting_attrs = {"KAFKA_SERVERS": "kafka_servers", "FLINK_REST_URL": "flink_url", "MODEL_HEALTH_URL": "model_url"}
    configured = bool(setting_name and getattr(settings, setting_attrs.get(setting_name, ""), ""))
    return {"resource_id": resource_id, "status": "configured" if configured else "unknown",
            "detail": "已配置服务端连接" if configured else "等待服务端连接适配器"}


@app.get("/api/agent/projects/{project_id}/runbooks")
def workspace_runbooks(project_id: str, query: str = "", component: str | None = None):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return search_runbooks(query or "Kafka Flink 模型", component=component, limit=20)


@app.get("/api/agent/models")
def workspace_models():
    """Return the safe model catalog without exposing credentials or endpoint secrets."""
    return public_catalog(settings)


@app.get("/api/agent/benchmark")
def workspace_benchmark(split: str | None = None):
    if split not in {None, "dev", "test"}:
        raise HTTPException(422, "split 只能是 dev 或 test")
    return evaluate_benchmark(split)


@app.get("/api/agent/projects/{project_id}/conversations")
def workspace_conversations(project_id: str, include_archived: bool = False):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return store.conversations(project_id, include_archived=include_archived)


@app.post("/api/agent/projects/{project_id}/conversations")
def workspace_create_conversation(project_id: str, body: ConversationCreate):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    try:
        model_id, effort = validate_selection(settings, body.model_id, body.reasoning_effort)
    except ValueError as exc:
        raise HTTPException(422, str(exc)) from exc
    return store.create_conversation(project_id, str(uuid.uuid4()), body.title, model_id, effort)


@app.get("/api/agent/conversations/{conversation_id}")
def workspace_conversation(conversation_id: str):
    conversation = store.conversation(conversation_id)
    if not conversation:
        raise HTTPException(404, "会话不存在")
    return {**conversation, "messages": store.messages(conversation_id),
            "summary": store.conversation_summary(conversation_id),
            "active_run": store.active_run(conversation_id)}


@app.patch("/api/agent/conversations/{conversation_id}")
def workspace_update_conversation(conversation_id: str, body: ConversationUpdate):
    conversation = store.conversation(conversation_id)
    if not conversation:
        raise HTTPException(404, "会话不存在")
    if body.status == "archived" and store.active_run(conversation_id):
        raise HTTPException(409, "运行中的会话不能归档，请先停止任务")
    model_id = body.model_id if body.model_id is not None else conversation.get("model_id")
    reasoning_effort = body.reasoning_effort if body.reasoning_effort is not None else conversation.get("reasoning_effort")
    if body.model_id is not None or body.reasoning_effort is not None:
        try:
            model_id, reasoning_effort = validate_selection(settings, model_id, reasoning_effort)
        except ValueError as exc:
            raise HTTPException(422, str(exc)) from exc
    return store.update_conversation(conversation_id, body.title, body.status, model_id, reasoning_effort)


@app.delete("/api/agent/conversations/{conversation_id}")
def workspace_delete_conversation(conversation_id: str):
    conversation = store.conversation(conversation_id)
    if not conversation:
        raise HTTPException(404, "会话不存在")
    if store.active_run(conversation_id):
        raise HTTPException(409, "运行中的会话不能删除，请先停止任务")
    return store.delete_conversation(conversation_id)


@app.post("/api/agent/conversations/{conversation_id}/messages")
def workspace_message(conversation_id: str, body: MessageCreate):
    conversation = store.conversation(conversation_id)
    if not conversation:
        raise HTTPException(404, "会话不存在")
    active = store.active_run(conversation_id)
    if active:
        return {"conversation_id": conversation_id, "run_id": active, "status": "running", "duplicate": True}
    message, created = store.add_message(conversation_id, "user", body.content,
                                         {"resource_ids": body.resource_ids, "time_range": body.time_range}, body.request_id)
    if not created:
        run_id = message.get("run_id")
        return {"conversation_id": conversation_id, "run_id": run_id, "status": "duplicate", "duplicate": True}
    replay = read_replay(body.replay) if body.mode == "replay" and body.replay else None
    run, _ = runtime_instance().create_and_submit(conversation_id, conversation["project_id"], body.content,
                                                  {"resource_ids": body.resource_ids, "time_range": body.time_range}, replay)
    store.attach_message_run(message["id"], run["id"])
    return {"conversation_id": conversation_id, "run_id": run["id"], "status": run["status"], "duplicate": False}


def run_detail(run_id: str):
    run = store.run(run_id)
    if not run:
        return None
    return {**run, "events": store.run_events(run_id), "evidence": store.run_evidence(run_id),
            "artifacts": store.artifacts(run_id), "execution_actions": store.execution_actions(run_id),
            "subagent_tasks": store.subagent_tasks(run_id), "agent_messages": store.agent_messages(run_id)}


@app.get("/api/agent/runs/{run_id}")
def workspace_run(run_id: str):
    found = run_detail(run_id)
    if not found:
        raise HTTPException(404, "运行记录不存在")
    return found


@app.post("/api/agent/runs/{run_id}/cancel")
def workspace_cancel_run(run_id: str):
    if not store.run(run_id):
        raise HTTPException(404, "运行记录不存在")
    return runtime_instance().stop(run_id)


@app.post("/api/agent/runs/{run_id}/resume")
def workspace_resume_run(run_id: str):
    run = store.run(run_id)
    if not run:
        raise HTTPException(404, "运行记录不存在")
    if run["status"] not in {"failed", "interrupted", "cancelled"}:
        raise HTTPException(409, "当前运行不需要恢复")
    conversation = store.conversation(run["conversation_id"])
    messages = store.messages(run["conversation_id"])
    question = next((item["content"] for item in reversed(messages) if item["role"] == "user"), "继续检查")
    new_run, _ = runtime_instance().create_and_submit(run["conversation_id"], conversation["project_id"], question, {})
    return {"run_id": new_run["id"], "status": new_run["status"], "resumed_from": run_id}


@app.get("/api/agent/runs/{run_id}/events")
async def workspace_run_events(run_id: str, request: Request, last_event_id: str | None = Header(default=None)):
    if not store.run(run_id):
        raise HTTPException(404, "运行记录不存在")
    try:
        last = max(0, int(last_event_id or request.headers.get("last-event-id", "0")))
    except ValueError:
        last = 0

    async def generate():
        nonlocal last
        while True:
            for item in await asyncio.to_thread(store.run_events, run_id, last):
                last = item["seq"]
                yield "id: {}\nevent: {}\ndata: {}\n\n".format(last, item["type"], json.dumps(item, ensure_ascii=False))
            current = await asyncio.to_thread(store.run, run_id)
            if current["status"] in {"completed", "failed", "cancelled", "interrupted"}:
                break
            if await request.is_disconnected():
                break
            yield ": keepalive\n\n"
            await asyncio.sleep(0.5)

    return StreamingResponse(generate(), media_type="text/event-stream", headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"})


@app.get("/api/agent/projects/{project_id}/memories")
def workspace_memories(project_id: str, include_inactive: bool = False):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return store.memories(project_id, include_inactive)


@app.post("/api/agent/projects/{project_id}/memories")
def workspace_create_memory(project_id: str, body: MemoryCreate):
    if not store.project(project_id):
        raise HTTPException(404, "项目不存在")
    return store.create_memory(project_id, body.kind, body.content, body.source_ids, "proposed")


@app.post("/api/agent/projects/{project_id}/memories/{memory_id}/approve")
def workspace_approve_memory(project_id: str, memory_id: str):
    memory = store.memory(memory_id)
    if not memory or memory["project_id"] != project_id:
        raise HTTPException(404, "记忆不存在")
    return store.set_memory_status(memory_id, "active")


@app.post("/api/agent/projects/{project_id}/memories/{memory_id}/revoke")
def workspace_revoke_memory(project_id: str, memory_id: str):
    memory = store.memory(memory_id)
    if not memory or memory["project_id"] != project_id:
        raise HTTPException(404, "记忆不存在")
    return store.set_memory_status(memory_id, "revoked")


@app.get("/api/agent/artifacts/{artifact_id}")
def workspace_artifact(artifact_id: str):
    found = store.artifact(artifact_id)
    if not found:
        raise HTTPException(404, "可视化数据不存在")
    return found
