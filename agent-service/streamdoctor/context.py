"""Context assembly for a bounded, auditable Agent turn."""

import json
from dataclasses import dataclass
from typing import Any


@dataclass
class ContextBundle:
    project: dict[str, Any]
    spec: dict[str, Any] | None
    resources: list[dict[str, Any]]
    memories: list[dict[str, Any]]
    summary: str
    messages: list[dict[str, Any]]
    selected_resource_ids: list[str]
    time_range: dict[str, str] | None

    def as_prompt(self, question: str) -> str:
        return json.dumps({
            "project": self.project,
            "project_spec": self.spec,
            "resources": self.resources,
            "confirmed_memory": self.memories,
            "conversation_summary": self.summary,
            "recent_messages": self.messages,
            "selected_resource_ids": self.selected_resource_ids,
            "time_range": self.time_range,
            "question": question,
        }, ensure_ascii=False)


class ContextAssembler:
    def __init__(self, store, max_messages: int = 12, max_chars: int = 24000):
        self.store = store
        self.max_messages = max_messages
        self.max_chars = max_chars

    def assemble(self, project_id: str, conversation_id: str, question: str,
                 resource_ids: list[str] | None = None,
                 time_range: dict[str, str] | None = None) -> ContextBundle:
        project = self.store.project(project_id)
        if not project:
            raise ValueError("项目不存在")
        resources = self.store.resources(project_id)
        current_spec = self.store.project_spec(project_id)
        selected = set(resource_ids or [])
        selected_resources = [item for item in resources if not selected or item["id"] in selected]
        messages = self.store.messages(conversation_id, limit=self.max_messages)
        summary = self.store.conversation_summary(conversation_id) or ""
        memories = self.store.search_memories(project_id, question, limit=5)
        # Keep the latest turns intact and truncate only old message content.
        total = len(summary) + sum(len(str(item.get("content", ""))) for item in messages)
        if total > self.max_chars:
            summary = summary[:4000]
            budget = max(4000, self.max_chars - len(summary))
            trimmed: list[dict[str, Any]] = []
            used = 0
            for item in reversed(messages):
                content = str(item.get("content", ""))
                if used + len(content) > budget:
                    content = content[:max(200, budget - used)]
                trimmed.append({**item, "content": content})
                used += len(content)
                if used >= budget:
                    break
            messages = list(reversed(trimmed))
        return ContextBundle(project, current_spec, selected_resources, memories, summary, messages,
                             list(resource_ids or []), time_range)
