"""Role-separated agents for review and controlled remediation."""

from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Any

import httpx


@dataclass(frozen=True)
class ExecutionIntent:
    action: str
    target: str
    explicit: bool
    source: str


class ReviewerAgent:
    """Read-only reviewer. It can describe findings but cannot invoke actions."""

    role = "reviewer"
    permissions = ("observe", "review", "recommend")

    def review(self, report: dict[str, Any], evidence: list[dict[str, Any]]) -> dict[str, Any]:
        unavailable = [item for item in evidence if item.get("status") not in {"ok", "healthy"}]
        findings = []
        for candidate in report.get("candidates", [])[:3]:
            findings.append({
                "title": candidate.get("title", "待核查问题"),
                "basis": candidate.get("evidence", []),
                "verify": candidate.get("verify", ""),
            })
        return {
            "role": self.role,
            "permissions": list(self.permissions),
            "verdict": report.get("classification", "unknown"),
            "summary": report.get("summary", "证据审查完成"),
            "findings": findings,
            "observation_gaps": report.get("missing", [])[:5],
            "unavailable_sources": [item.get("source") for item in unavailable],
            "may_execute": False,
        }


class ExecutorPolicy:
    """Deterministic policy. Model output is never accepted as execution authority."""

    ACTIONS = {"status", "start", "stop", "restart"}
    TARGET_ALIASES = {
        "kafka": ("kafka", "卡夫卡"),
        "flink": ("flink", "flink作业", "flink集群"),
        "model": ("model", "模型服务", "模型"),
        "pipeline": ("pipeline", "整条链路", "全链路", "链路"),
    }
    ACTION_ALIASES = {
        "status": ("status", "状态"),
        "start": ("start", "启动", "开启"),
        "stop": ("stop", "停止", "关闭"),
        "restart": ("restart", "重启"),
    }
    EXPLICIT_PREFIXES = ("执行", "请", "帮我", "立即", "现在", "/")
    NON_EXECUTION_MARKERS = ("是否", "能否", "需不需要", "建议", "方案", "怎么", "如何", "分析")

    @classmethod
    def parse(cls, text: str) -> ExecutionIntent | None:
        normalized = re.sub(r"\s+", "", text.strip().lower())
        action = next((key for key, aliases in cls.ACTION_ALIASES.items() if any(alias in normalized for alias in aliases)), None)
        target = next((key for key, aliases in cls.TARGET_ALIASES.items() if any(alias in normalized for alias in aliases)), None)
        if not action or not target:
            return None
        slash = re.match(r"^/(status|start|stop|restart)(?:\s+|[-_:])?(kafka|flink|model|pipeline)$", text.strip(), re.I)
        imperative = any(normalized.startswith(alias) for aliases in cls.ACTION_ALIASES.values() for alias in aliases)
        explicit = bool(slash) or (
            (any(normalized.startswith(prefix) for prefix in cls.EXPLICIT_PREFIXES) or imperative)
            and not any(marker in normalized for marker in cls.NON_EXECUTION_MARKERS)
        )
        if slash:
            action, target = slash.group(1).lower(), slash.group(2).lower()
        return ExecutionIntent(action=action, target=target, explicit=explicit, source="slash" if slash else "natural_language")


class ExecutorAgent:
    """Executes only policy-approved actions through the isolated action runner."""

    role = "executor"
    permissions = ("status", "start", "stop", "restart")

    def __init__(self, base_url: str, token: str, timeout: float = 25):
        self.base_url = base_url.rstrip("/")
        self.token = token
        self.timeout = timeout

    def plan(self, text: str) -> tuple[ExecutionIntent | None, str | None]:
        intent = ExecutorPolicy.parse(text)
        if not intent:
            return None, None
        if intent.action not in ExecutorPolicy.ACTIONS:
            return None, "动作不在执行白名单"
        if not intent.explicit and intent.action != "status":
            return intent, "检测到处置建议，但用户没有明确要求执行"
        if not self.base_url or not self.token:
            return intent, "受控执行器尚未配置"
        return intent, None

    def execute(self, intent: ExecutionIntent, project_id: str, run_id: str) -> dict[str, Any]:
        headers = {"X-Executor-Token": self.token}
        payload = {"action": intent.action, "target": intent.target, "project_id": project_id, "run_id": run_id}
        try:
            response = httpx.post(self.base_url + "/v1/actions", json=payload, headers=headers, timeout=self.timeout)
            response.raise_for_status()
            result = response.json()
            return {"status": result.get("status", "unknown"), "action": intent.action,
                    "target": intent.target, "details": result.get("details", [])}
        except Exception as exc:
            return {"status": "unavailable", "action": intent.action, "target": intent.target,
                    "error": str(exc)[:300], "details": []}
