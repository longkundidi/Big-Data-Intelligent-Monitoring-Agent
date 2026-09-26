"""Public request and response models for the StreamDoctor workspace."""

from typing import Any, Literal

from pydantic import BaseModel, Field


class ProjectCreate(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    description: str = Field(default="", max_length=2000)
    topology_id: str = Field(default="empty", max_length=120)
    template: bool = False
    template_id: str | None = Field(default=None, max_length=120)
    template_version: int | None = Field(default=None, ge=1)


class ProjectUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=120)
    description: str | None = Field(default=None, max_length=2000)
    status: Literal["active", "archived"] | None = None


class ResourceCreate(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    type: Literal["kafka", "flink", "model", "mysql", "postgresql", "redis", "clickhouse", "mongodb",
                  "prometheus", "api", "gateway", "airflow", "spark", "log", "business", "custom"]
    config: dict[str, Any] = Field(default_factory=dict)


class ResourceUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=120)
    config: dict[str, Any] | None = None
    status: Literal["active", "archived"] | None = None


class ConversationCreate(BaseModel):
    title: str = Field(default="新对话", min_length=1, max_length=200)
    model_id: str | None = Field(default=None, min_length=1, max_length=120)
    reasoning_effort: str | None = Field(default=None, min_length=1, max_length=20)


class ConversationUpdate(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=200)
    status: Literal["active", "archived"] | None = None
    model_id: str | None = Field(default=None, min_length=1, max_length=120)
    reasoning_effort: str | None = Field(default=None, min_length=1, max_length=20)


class MessageCreate(BaseModel):
    content: str = Field(min_length=1, max_length=10000)
    request_id: str | None = Field(default=None, max_length=120)
    resource_ids: list[str] = Field(default_factory=list, max_length=20)
    time_range: dict[str, str] | None = None
    mode: Literal["live", "replay"] = "live"
    replay: str | None = Field(default=None, max_length=80)


class MemoryCreate(BaseModel):
    kind: Literal["fact", "preference", "experience"] = "fact"
    content: str = Field(min_length=1, max_length=2000)
    source_ids: list[str] = Field(default_factory=list, max_length=20)


class ArtifactCreate(BaseModel):
    kind: Literal["chart", "topology", "table", "log", "report"]
    data: dict[str, Any]
    source_evidence_ids: list[str] = Field(default_factory=list)
    time_range: dict[str, str] | None = None


class TemplateCreate(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    description: str = Field(default="", max_length=2000)
    category: str = Field(default="自定义", max_length=80)
    spec: dict[str, Any]
    change_summary: str = Field(default="创建模板", max_length=500)


class TemplatePublish(BaseModel):
    name: str | None = Field(default=None, min_length=1, max_length=120)
    description: str | None = Field(default=None, max_length=2000)
    category: str | None = Field(default=None, max_length=80)
    spec: dict[str, Any]
    change_summary: str = Field(default="更新模板", min_length=1, max_length=500)


class TemplateClone(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    description: str | None = Field(default=None, max_length=2000)


class ProjectSpecPublish(BaseModel):
    spec: dict[str, Any]
    change_summary: str = Field(default="更新项目规格", min_length=1, max_length=500)
    source_type: Literal["manual", "template", "document", "conversation", "scan", "restore", "migration"] = "manual"
    source_ref: str | None = Field(default=None, max_length=500)
    base_version_id: str | None = Field(default=None, max_length=120)


class ProjectDocumentCreate(BaseModel):
    filename: str = Field(min_length=1, max_length=240)
    media_type: str = Field(default="text/plain", max_length=120)
    content: str = Field(min_length=1, max_length=500_000)


class ProjectSpecProposal(BaseModel):
    content: str = Field(min_length=1, max_length=20_000)
    source_type: Literal["conversation", "document"] = "conversation"


class ProjectInitialize(BaseModel):
    conversation_id: str | None = Field(default=None, max_length=120)
    request_id: str | None = Field(default=None, max_length=120)
