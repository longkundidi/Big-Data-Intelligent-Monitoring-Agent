"""Project-scoped memory management with explicit user approval."""

from typing import Any


class MemoryManager:
    def __init__(self, store):
        self.store = store

    def search(self, project_id: str, query: str, limit: int = 5) -> list[dict[str, Any]]:
        return self.store.search_memories(project_id, query, limit)

    def propose(self, project_id: str, kind: str, content: str, source_ids: list[str] | None = None):
        return self.store.create_memory(project_id, kind, content, source_ids or [], status="proposed")

    def approve(self, memory_id: str):
        return self.store.set_memory_status(memory_id, "active")

    def revoke(self, memory_id: str):
        return self.store.set_memory_status(memory_id, "revoked")
