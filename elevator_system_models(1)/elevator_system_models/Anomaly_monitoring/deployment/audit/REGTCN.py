import json
import os
from datetime import datetime, timezone

import requests


PLATFORM_URL = os.getenv(
    "ALGORITHM_PLATFORM_URL",
    "http://192.168.65.237:9999/lk/api/model-test",
)
MONITOR_POINT_ID = os.getenv(
    "ELEVATOR_MONITOR_POINT_ID",
    "smart-home-aux1-a1-traction-point1",
)


def run_test():
    task_msg = {
        "monitorPointId": MONITOR_POINT_ID,
        "startTime": "2026-07-22T00:00:00Z",
        "endTime": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
    }
    response = requests.post(
        PLATFORM_URL,
        json={
            "alId": 196,
            "alClass": "alStateEvaluation",
            "taskMsg": json.dumps(task_msg, ensure_ascii=False),
        },
        timeout=60,
    )
    response.raise_for_status()
    payload = response.json()
    if str(payload.get("code")) != "200" or not payload.get("success"):
        raise RuntimeError(payload.get("message") or "Platform model test failed")

    task = payload.get("data") or {}
    if int(task.get("taskState", 3)) != 2:
        raise RuntimeError(task.get("taskResult") or "REGTCN task failed")
    result = json.loads(task.get("taskResult") or "{}")
    required = {"sample_count", "status", "is_anomaly", "anomaly_score", "threshold"}
    missing = sorted(required.difference(result))
    if missing:
        raise RuntimeError("REGTCN output missing fields: " + ", ".join(missing))
    if int(result["sample_count"]) != 1024:
        raise RuntimeError("REGTCN sample_count must be 1024")

    return {
        "model": "REGTCN",
        "status": "passed",
        "sample_count": 1024,
        "inference_result": result,
    }


if __name__ == "__main__":
    print(json.dumps(run_test(), ensure_ascii=False))
