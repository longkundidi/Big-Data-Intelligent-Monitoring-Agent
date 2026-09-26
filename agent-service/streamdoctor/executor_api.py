"""Small Docker action runner with a closed action and target vocabulary."""

from __future__ import annotations

import http.client
import os
import secrets
import socket
import threading
from typing import Literal
from urllib.parse import quote

from fastapi import Depends, FastAPI, Header, HTTPException
from pydantic import BaseModel


DOCKER_SOCKET = os.getenv("DOCKER_SOCKET", "/var/run/docker.sock")
SHARED_TOKEN = os.getenv("EXECUTOR_SHARED_TOKEN", "")
TARGETS = {
    "kafka": ["streamdoctor-kafka"],
    "flink": ["streamdoctor-flink-jobmanager", "streamdoctor-flink-taskmanager", "streamdoctor-flink-job-submit"],
    "model": ["streamdoctor-regtcn-model"],
    "pipeline": ["streamdoctor-kafka", "streamdoctor-regtcn-model", "streamdoctor-flink-jobmanager",
                 "streamdoctor-flink-taskmanager", "streamdoctor-flink-job-submit"],
}


class ActionRequest(BaseModel):
    action: Literal["status", "start", "stop", "restart"]
    target: Literal["kafka", "flink", "model", "pipeline"]
    project_id: str
    run_id: str


class UnixConnection(http.client.HTTPConnection):
    def __init__(self):
        super().__init__("localhost", timeout=20)

    def connect(self):
        self.sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        self.sock.settimeout(self.timeout)
        self.sock.connect(DOCKER_SOCKET)


def docker_request(method: str, path: str) -> tuple[int, str]:
    connection = UnixConnection()
    try:
        connection.request(method, path, headers={"Host": "localhost"})
        response = connection.getresponse()
        return response.status, response.read(32_000).decode("utf-8", errors="replace")
    finally:
        connection.close()


def require_token(x_executor_token: str = Header(default="")):
    if not SHARED_TOKEN or not x_executor_token or not secrets.compare_digest(x_executor_token, SHARED_TOKEN):
        raise HTTPException(403, "执行器凭证无效")


def operate(container: str, action: str):
    name = quote(container, safe="")
    if action == "status":
        code, body = docker_request("GET", f"/v1.43/containers/{name}/json")
        state = "missing"
        if code == 200:
            import json
            state = (json.loads(body).get("State") or {}).get("Status", "unknown")
        return {"container": container, "status": state, "http_status": code}
    suffix = f"/{action}?t=15" if action in {"stop", "restart"} else "/start"
    code, body = docker_request("POST", f"/v1.43/containers/{name}{suffix}")
    return {"container": container, "status": "ok" if code in {204, 304} else "failed",
            "http_status": code, "error": body[:300] if code not in {204, 304} else None}


app = FastAPI(title="StreamDoctor Controlled Executor", dependencies=[Depends(require_token)])
operation_lock = threading.Lock()


@app.get("/health")
def health():
    return {"status": "ok", "targets": sorted(TARGETS), "actions": ["status", "start", "stop", "restart"]}


@app.post("/v1/actions")
def action(body: ActionRequest):
    containers = list(TARGETS[body.target])
    if body.action == "stop":
        containers.reverse()
    details = []
    with operation_lock:
        for container in containers:
            details.append(operate(container, body.action))
            if details[-1]["status"] == "failed":
                break
    states = {item["status"] for item in details}
    status = "ok" if states <= {"ok", "running", "exited", "created", "restarting", "dead"} else "failed"
    return {"status": status, "action": body.action, "target": body.target, "details": details,
            "project_id": body.project_id, "run_id": body.run_id}
