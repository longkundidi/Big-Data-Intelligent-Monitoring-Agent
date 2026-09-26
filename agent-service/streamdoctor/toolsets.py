"""Project-driven diagnostic toolsets inspired by mature AIOps agents."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class ToolsetDefinition:
    id: str
    service_types: tuple[str, ...]
    tools: tuple[str, ...]
    signals: tuple[str, ...]


TOOLSETS = (
    ToolsetDefinition("streaming", ("kafka", "flink"),
                      ("get_kafka_offsets", "get_flink_status", "get_flink_metrics"),
                      ("queue.backlog", "pipeline.throughput", "pipeline.backpressure", "checkpoint.status")),
    ToolsetDefinition("service", ("model", "http", "api", "gateway", "elasticsearch"),
                      ("get_service_health",),
                      ("service.availability", "service.latency")),
    ToolsetDefinition("metrics", ("prometheus",),
                      ("get_prometheus_metrics",),
                      ("service.error_rate", "service.latency", "resource.utilization", "queue.depth")),
    ToolsetDefinition("data-store", ("mysql", "postgresql", "redis", "clickhouse", "mongodb"),
                      ("get_dependency_health",),
                      ("dependency.availability", "dependency.connect_latency")),
    ToolsetDefinition("batch", ("airflow", "spark", "scheduler", "warehouse"),
                      ("get_service_health", "get_prometheus_metrics"),
                      ("job.status", "job.duration", "data.freshness")),
)


def enabled_toolsets(spec: dict | None):
    spec = spec or {}
    service_types = {str(item.get("type", "")).lower() for item in spec.get("services", [])}
    explicit = set(spec.get("diagnostics", {}).get("tools", []))
    result = []
    for definition in TOOLSETS:
        if service_types.intersection(definition.service_types) or explicit.intersection(definition.tools):
            result.append(definition)
    return result


def enabled_tools(spec: dict | None):
    explicit = list((spec or {}).get("diagnostics", {}).get("tools", []))
    inferred = [tool for toolset in enabled_toolsets(spec) for tool in toolset.tools]
    return list(dict.fromkeys(explicit + inferred))


def catalog(spec: dict | None):
    enabled = {item.id for item in enabled_toolsets(spec)}
    return [{"id": item.id, "enabled": item.id in enabled, "service_types": list(item.service_types),
             "tools": list(item.tools), "signals": list(item.signals)} for item in TOOLSETS]
