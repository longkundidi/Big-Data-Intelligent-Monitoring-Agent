import json
import os
import tomllib
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def _codex_model_config():
    if os.getenv("AGENT_USE_CODEX_CONFIG", "true").lower() != "true":
        return {}
    path = Path.home() / ".codex" / "config.toml"
    try:
        data = tomllib.loads(path.read_text(encoding="utf-8"))
        provider_id = data.get("model_provider")
        provider = (data.get("model_providers") or {}).get(provider_id, {})
        if provider.get("wire_api") not in {None, "responses"}:
            return {}
        return {
            "model": data.get("model") or "",
            "reasoning_effort": data.get("model_reasoning_effort") or data.get("reasoning_effort") or "",
            "base_url": provider.get("base_url") or "",
        }
    except (OSError, ValueError, TypeError):
        return {}


CODEX_MODEL_CONFIG = _codex_model_config()


@dataclass(frozen=True)
class Settings:
    db_path: Path = Path(os.getenv("AGENT_DB_PATH", str(ROOT / "streamdoctor.db")))
    topology_path: Path = Path(os.getenv("AGENT_TOPOLOGY_PATH", str(ROOT / "topology.json")))
    flink_url: str = os.getenv("FLINK_REST_URL", "")
    kafka_servers: str = os.getenv("KAFKA_SERVERS", "")
    model_url: str = os.getenv("MODEL_HEALTH_URL", "")
    model_api_key: str = os.getenv("AGENT_MODEL_API_KEY") or os.getenv("OPENAI_API_KEY", "")
    model_base_url: str = os.getenv("AGENT_MODEL_BASE_URL") or os.getenv("OPENAI_BASE_URL") or CODEX_MODEL_CONFIG.get("base_url", "")
    model_name: str = os.getenv("AGENT_MODEL_NAME") or os.getenv("OPENAI_MODEL") or CODEX_MODEL_CONFIG.get("model", "")
    model_reasoning_effort: str = os.getenv("AGENT_REASONING_EFFORT") or os.getenv("OPENAI_REASONING_EFFORT") or CODEX_MODEL_CONFIG.get("reasoning_effort", "")
    model_config_source: str = "agent_env" if any(os.getenv(key) for key in ("AGENT_MODEL_API_KEY", "AGENT_MODEL_BASE_URL", "AGENT_MODEL_NAME")) else ("codex_config" if CODEX_MODEL_CONFIG else "openai_env")
    jwks_url: str = os.getenv("AGENT_JWKS_URL", "")
    jwt_issuer: str = os.getenv("AGENT_JWT_ISSUER", "")
    jwt_audience: str = os.getenv("AGENT_JWT_AUDIENCE", "")
    trusted_networks: tuple[str, ...] = tuple(
        item.strip() for item in os.getenv("AGENT_TRUSTED_NETWORKS", "").split(",") if item.strip()
    )
    poll_enabled: bool = os.getenv("AGENT_POLL_ENABLED", "false").lower() == "true"
    project_scan_root: Path | None = Path(os.getenv("AGENT_PROJECT_SCAN_ROOT")).resolve() if os.getenv("AGENT_PROJECT_SCAN_ROOT") else None

    def topology(self):
        return json.loads(self.topology_path.read_text(encoding="utf-8"))
