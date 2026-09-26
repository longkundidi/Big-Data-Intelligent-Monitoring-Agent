"""Server-side model catalog and conversation selection validation."""

from dataclasses import dataclass


@dataclass(frozen=True)
class ModelProfile:
    id: str
    name: str
    description: str
    reasoning_efforts: tuple[str, ...]
    default_effort: str

    def public(self):
        return {
            "id": self.id,
            "name": self.name,
            "description": self.description,
            "reasoning_efforts": list(self.reasoning_efforts),
            "default_effort": self.default_effort,
        }


OPENAI_MODELS = (
    ModelProfile(
        "gpt-6-astra",
        "GPT-6 Astra",
        "复杂诊断与高难度推理",
        ("low", "medium", "high", "xhigh", "max"),
        "high",
    ),
    ModelProfile(
        "gpt-5.6-sol",
        "GPT-5.6 Sol",
        "Agent 工作流与日常诊断",
        ("low", "medium", "high", "xhigh", "max", "ultra"),
        "medium",
    ),
    ModelProfile(
        "gpt-5.6-terra",
        "GPT-5.6 Terra",
        "平衡质量、速度与成本",
        ("low", "medium", "high", "xhigh", "max", "ultra"),
        "medium",
    ),
    ModelProfile(
        "gpt-5.6-luna",
        "GPT-5.6 Luna",
        "低成本快速巡检与摘要",
        ("low", "medium", "high", "xhigh", "max"),
        "medium",
    ),
    ModelProfile(
        "gpt-5.5",
        "GPT-5.5",
        "复杂诊断与通用 Agent 任务",
        ("low", "medium", "high", "xhigh"),
        "medium",
    ),
)

DEFAULT_MODEL_ID = "gpt-5.6-luna"


def catalog(settings):
    profiles = list(OPENAI_MODELS)
    configured_name = (settings.model_name or "").strip()
    if configured_name and configured_name not in {item.id for item in profiles}:
        profiles.append(ModelProfile(
            configured_name,
            configured_name,
            "服务端配置的 OpenAI 兼容模型",
            ("none", "minimal", "low", "medium", "high", "xhigh", "max"),
            settings.model_reasoning_effort or "medium",
        ))
    return profiles


def defaults(settings):
    profiles = catalog(settings)
    model_id = (settings.model_name or DEFAULT_MODEL_ID).strip()
    profile = next((item for item in profiles if item.id == model_id), profiles[0])
    effort = (settings.model_reasoning_effort or profile.default_effort).strip().lower()
    if effort not in profile.reasoning_efforts:
        effort = profile.default_effort
    return model_id, effort


def validate_selection(settings, model_id, reasoning_effort):
    profiles = catalog(settings)
    model_id = (model_id or defaults(settings)[0]).strip()
    profile = next((item for item in profiles if item.id == model_id), None)
    if not profile:
        raise ValueError("模型不在服务端允许目录中")
    effort = (reasoning_effort or profile.default_effort).strip().lower()
    if effort not in profile.reasoning_efforts:
        raise ValueError("模型 {} 不支持推理强度 {}".format(model_id, effort))
    return model_id, effort


def public_catalog(settings):
    default_model, default_effort = defaults(settings)
    return {
        "provider": "openai-compatible" if settings.model_base_url else "openai",
        "configuration_source": settings.model_config_source,
        "configured": bool(settings.model_api_key),
        "default_model": default_model,
        "default_reasoning_effort": default_effort,
        "models": [item.public() for item in catalog(settings)],
        "configuration_hint": None if settings.model_api_key else "服务端尚未配置 AGENT_MODEL_API_KEY，运行时将保留规则诊断结果。",
    }
