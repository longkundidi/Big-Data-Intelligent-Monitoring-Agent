"""Persistent task delegation and messaging for StreamDoctor subagents."""

from __future__ import annotations

from typing import Any, Callable


class AgentCoordinator:
    """Routes tasks and messages while keeping role boundaries auditable."""

    def __init__(self, store, run_id: str, event_sink: Callable[[str, dict[str, Any]], Any]):
        self.store = store
        self.run_id = run_id
        self.event_sink = event_sink

    def delegate(self, to_agent: str, task_type: str, payload: dict[str, Any],
                 from_agent: str = "coordinator", parent_task_id: str | None = None):
        task = self.store.create_subagent_task(
            self.run_id, to_agent, task_type, payload, parent_task_id=parent_task_id
        )
        message = self.store.add_agent_message(
            self.run_id, from_agent, to_agent, "delegation",
            {"task_id": task["id"], "task_type": task_type, "input": payload},
        )
        self.event_sink("agent_message", message)
        self.event_sink("subagent_started", {
            "task_id": task["id"], "agent_role": to_agent, "task_type": task_type,
            "parent_task_id": parent_task_id,
        })
        return task

    def complete(self, task_id: str, output: dict[str, Any]):
        task = self.store.finish_subagent_task(task_id, "completed", output)
        message = self.store.add_agent_message(
            self.run_id, task["agent_role"], "coordinator", "result",
            {"task_id": task_id, "task_type": task["task_type"], "output": output},
        )
        self.event_sink("agent_message", message)
        self.event_sink("subagent_finished", {
            "task_id": task_id, "agent_role": task["agent_role"],
            "task_type": task["task_type"], "status": "completed",
        })
        return task

    def block(self, task_id: str, reason: str):
        task = self.store.finish_subagent_task(task_id, "blocked", {"reason": reason})
        message = self.store.add_agent_message(
            self.run_id, task["agent_role"], "coordinator", "blocked",
            {"task_id": task_id, "task_type": task["task_type"], "reason": reason},
        )
        self.event_sink("agent_message", message)
        self.event_sink("subagent_finished", {
            "task_id": task_id, "agent_role": task["agent_role"],
            "task_type": task["task_type"], "status": "blocked", "reason": reason,
        })
        return task
