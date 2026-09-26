"""Validated, frontend-friendly visualization artifacts."""

from typing import Any


ALLOWED_KINDS = {"chart", "topology", "table", "log", "report"}


def make_artifact(kind: str, data: dict[str, Any], source_evidence_ids=None,
                  time_range=None) -> dict[str, Any]:
    if kind not in ALLOWED_KINDS:
        raise ValueError("不支持的可视化类型")
    if len(str(data)) > 500_000:
        raise ValueError("可视化数据过大")
    return {
        "kind": kind,
        "data": data,
        "source_evidence_ids": list(source_evidence_ids or []),
        "time_range": time_range,
    }
