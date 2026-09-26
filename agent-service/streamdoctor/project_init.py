"""Safe project-directory discovery for the /init workspace command."""

import copy
import hashlib
import json
import re
from pathlib import Path
from urllib.parse import urlparse

from .specs import diff_specs, normalize_spec


SKIP_DIRECTORIES = {
    ".git", ".idea", ".vscode", ".venv", "__pycache__", "node_modules",
    "target", "dist", "build", "coverage", "logs", "state", "migration",
}
SAFE_EXACT_NAMES = {
    ".env.example", "pom.xml", "package.json", "pyproject.toml", "topology.json",
}
SAFE_PREFIXES = ("compose", "docker-compose", "application", "bootstrap")
SAFE_SUFFIXES = {".yml", ".yaml", ".json", ".properties", ".toml", ".xml"}
MAX_FILES = 100
MAX_FILE_BYTES = 256 * 1024
MAX_TOTAL_BYTES = 2 * 1024 * 1024

CONFIG_KEYS = {
    "KAFKA_SERVERS", "FLINK_REST_URL", "MODEL_HEALTH_URL", "MODEL_URL",
    "INPUT_TOPIC", "OUTPUT_TOPIC", "CONSUMER_GROUP", "FLINK_JOB_NAME", "JOB_NAME",
}


def _safe_file(path: Path) -> bool:
    name = path.name.lower()
    if name == ".env" or (name.startswith(".env.") and name != ".env.example"):
        return False
    if any(part.lower() in SKIP_DIRECTORIES for part in path.parts):
        return False
    return name in SAFE_EXACT_NAMES or name.startswith(SAFE_PREFIXES) or path.suffix.lower() in SAFE_SUFFIXES


def _clean_value(value: str) -> str:
    value = value.strip().strip("'\"").rstrip(",")
    default = re.fullmatch(r"\$\{[A-Z0-9_]+(?::-([^}]*))?}", value)
    if default:
        value = default.group(1) or ""
    if not value or value.startswith("${") or value.lower() in {"none", "null", "changeme"}:
        return ""
    return value[:500]


def _extract_text_values(text: str) -> dict[str, str]:
    found = {}
    for key in CONFIG_KEYS:
        patterns = (
            rf"(?im)^\s*(?:-\s*)?{re.escape(key)}\s*[:=]\s*([^\r\n#]+)",
            rf"env\(\s*[\"']{re.escape(key)}[\"']\s*,\s*[\"']([^\"']+)",
            rf"os\.getenv\(\s*[\"']{re.escape(key)}[\"']\s*,\s*[\"']([^\"']+)",
        )
        for pattern in patterns:
            match = re.search(pattern, text)
            if match:
                value = _clean_value(match.group(1))
                if value:
                    found[key] = value
                    break
    return found


def _extract_json_values(value, found: dict[str, str]):
    if isinstance(value, dict):
        for key, item in value.items():
            normalized = str(key).upper()
            if normalized in CONFIG_KEYS and isinstance(item, (str, int)):
                cleaned = _clean_value(str(item))
                if cleaned:
                    found.setdefault(normalized, cleaned)
            _extract_json_values(item, found)
    elif isinstance(value, list):
        for item in value:
            _extract_json_values(item, found)


def _scan_files(root: Path):
    files = []
    detected = {}
    total_bytes = 0
    candidates = [path for path in root.rglob("*") if path.is_file() and _safe_file(path)]
    candidates.sort(key=lambda path: (
        0 if path.name.lower() in SAFE_EXACT_NAMES or path.name.lower().startswith(("compose", "docker-compose")) else 1,
        len(path.relative_to(root).parts), path.relative_to(root).as_posix(),
    ))
    for path in candidates:
        if len(files) >= MAX_FILES or total_bytes >= MAX_TOTAL_BYTES:
            break
        try:
            size = path.stat().st_size
            if size > MAX_FILE_BYTES or total_bytes + size > MAX_TOTAL_BYTES:
                continue
            raw = path.read_bytes()
            text = raw.decode("utf-8", errors="replace")
        except OSError:
            continue
        values = _extract_text_values(text)
        if path.suffix.lower() == ".json":
            try:
                _extract_json_values(json.loads(text), values)
            except (ValueError, TypeError):
                pass
        for key, value in values.items():
            detected.setdefault(key, value)
        files.append({
            "path": path.relative_to(root).as_posix(),
            "size": size,
            "sha256": hashlib.sha256(raw).hexdigest(),
            "detected_keys": sorted(values),
        })
        total_bytes += size
    return files, detected


def _identifier(value: str) -> str:
    result = re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
    return result[:60] or "service"


def _endpoint(value: str, default_port: int):
    candidate = value.split(",", 1)[0].strip()
    parsed = urlparse(candidate if "://" in candidate else "service://" + candidate)
    return parsed.hostname or "", parsed.port or default_port, parsed.path or ""


def _unique_id(items, preferred):
    existing = {item.get("id") for item in items}
    if preferred not in existing:
        return preferred
    index = 2
    while f"{preferred}-{index}" in existing:
        index += 1
    return f"{preferred}-{index}"


def _merge_endpoint(spec, kind, value, name, default_port, protocol):
    host, port, path = _endpoint(value, default_port)
    if not host:
        return None
    service = next((item for item in spec["services"] if item.get("type") == kind), None)
    if service is None:
        service = {
            "id": _unique_id(spec["services"], f"auto-{kind}"), "name": name, "type": kind,
            "host_id": None, "port": port, "protocol": protocol, "version": "",
            "credential_ref": None, "config": {},
        }
        spec["services"].append(service)
    linked_host = next((item for item in spec["hosts"] if item.get("id") == service.get("host_id")), None)
    if linked_host is None:
        linked_host = next((item for item in spec["hosts"] if item.get("address") == host), None)
    if linked_host is None:
        linked_host = {
            "id": _unique_id(spec["hosts"], f"auto-host-{_identifier(host)}"),
            "name": f"自动发现 {host}", "environment": "detected", "address": host,
        }
        spec["hosts"].append(linked_host)
    else:
        linked_host["address"] = host
    service.update({"host_id": linked_host["id"], "port": port, "protocol": protocol})
    service.setdefault("config", {})
    if kind == "model" and path:
        service["config"]["health_path"] = path
    return service


def _node(spec, node_type, preferred_id, name, service_id=None, occurrence=0):
    matches = [item for item in spec["nodes"] if item.get("type") == node_type]
    if len(matches) > occurrence:
        item = matches[occurrence]
    else:
        item = {"id": _unique_id(spec["nodes"], preferred_id), "name": name, "type": node_type,
                "service_id": service_id, "config": {}}
        spec["nodes"].append(item)
    item["name"] = name
    if service_id:
        item["service_id"] = service_id
    item.setdefault("config", {})
    return item


def _edge(spec, source, target, edge_type):
    existing = next((item for item in spec["edges"] if item.get("source") == source and item.get("target") == target), None)
    if existing:
        existing["type"] = edge_type
        return
    spec["edges"].append({
        "id": _unique_id(spec["edges"], f"auto-{_identifier(source)}-{_identifier(target)}"),
        "source": source, "target": target, "type": edge_type,
    })


def discover_project(root: Path, base_spec: dict, project_name: str):
    root = root.resolve()
    if not root.is_dir():
        raise ValueError("项目扫描目录不存在")
    files, detected = _scan_files(root)
    spec = normalize_spec(copy.deepcopy(base_spec))
    spec["name"] = spec.get("name") or project_name + "链路"

    kafka = _merge_endpoint(spec, "kafka", detected.get("KAFKA_SERVERS", ""), "Kafka 集群", 9092, "kafka") if detected.get("KAFKA_SERVERS") else None
    flink = _merge_endpoint(spec, "flink", detected.get("FLINK_REST_URL", ""), "Flink 集群", 8081, "http") if detected.get("FLINK_REST_URL") else None
    model_value = detected.get("MODEL_HEALTH_URL") or detected.get("MODEL_URL")
    model = _merge_endpoint(spec, "model", model_value, "模型服务", 8000, "http") if model_value else None

    input_node = output_node = job_node = model_node = None
    if detected.get("INPUT_TOPIC"):
        input_node = _node(spec, "kafka_topic", "auto-input-topic", "输入 Kafka", kafka and kafka["id"], 0)
        input_node["config"]["topic"] = detected["INPUT_TOPIC"]
    if detected.get("OUTPUT_TOPIC"):
        output_node = _node(spec, "kafka_topic", "auto-output-topic", "输出 Kafka", kafka and kafka["id"], 1)
        output_node["config"]["topic"] = detected["OUTPUT_TOPIC"]
    job_name = detected.get("FLINK_JOB_NAME") or detected.get("JOB_NAME")
    if job_name or detected.get("CONSUMER_GROUP") or flink:
        job_node = _node(spec, "flink_job", "auto-flink-job", "Flink 作业", flink and flink["id"])
        if job_name:
            job_node["config"]["job_name"] = job_name
        if detected.get("CONSUMER_GROUP"):
            job_node["config"]["consumer_group"] = detected["CONSUMER_GROUP"]
    if model:
        model_node = _node(spec, "http_endpoint", "auto-model-api", "模型接口", model["id"])
        if detected.get("MODEL_URL"):
            model_node["config"]["path"] = _endpoint(detected["MODEL_URL"], 8000)[2] or "/"

    if input_node and job_node:
        _edge(spec, input_node["id"], job_node["id"], "consume")
    if job_node and model_node:
        _edge(spec, job_node["id"], model_node["id"], "call")
        _edge(spec, model_node["id"], job_node["id"], "response")
    if job_node and output_node:
        _edge(spec, job_node["id"], output_node["id"], "produce")

    diagnostics = spec.setdefault("diagnostics", {})
    tool_names = diagnostics.setdefault("tools", [])
    tags = diagnostics.setdefault("runbook_tags", [])
    for kind, tool in ((kafka, "get_kafka_offsets"), (flink, "get_flink_status"), (flink, "get_flink_metrics"), (model, "get_model_health")):
        if kind and tool not in tool_names:
            tool_names.append(tool)
    for tag, service in (("kafka", kafka), ("flink", flink), ("model", model)):
        if service and tag not in tags:
            tags.append(tag)
    spec["discovery"] = {
        "source": "project_directory", "files": [item["path"] for item in files],
        "detected_keys": sorted(detected),
    }
    spec = normalize_spec(spec)
    changes = diff_specs(base_spec, spec)
    return {
        "spec": spec, "changes": changes, "files": files, "detected": detected,
        "summary": {"scanned_files": len(files), "detected_fields": len(detected), "changes": len(changes)},
    }
