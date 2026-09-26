import json
import math
import os
import random
import threading
import time
from collections import deque
from datetime import datetime, timezone

from fastapi import FastAPI
from pydantic import BaseModel


app = FastAPI(title="StreamDoctor REGTCN Demo Model", version="1.0.0")
_calls = deque(maxlen=1000)
_lock = threading.Lock()


class Task(BaseModel):
    taskId: int
    taskMsg: str
    taskResult: str | None = None
    taskState: int = 1
    taskReUrl: str = ""


def _recent_calls():
    cutoff = time.time() - 300
    with _lock:
        return [item for item in _calls if item[0] >= cutoff]


def _record(status: str, duration_ms: float):
    with _lock:
        _calls.append((time.time(), status, duration_ms))


def _percentile(values, percentile):
    if not values:
        return None
    ordered = sorted(values)
    index = max(0, math.ceil(len(ordered) * percentile) - 1)
    return round(ordered[index], 2)


@app.get("/healthz")
def health():
    calls = _recent_calls()
    durations = [item[2] for item in calls]
    errors = sum(item[1] != "ok" for item in calls)
    error_rate = round(errors / len(calls), 4) if calls else 0
    return {
        "status": "degraded" if error_rate >= 0.2 else "ok",
        "model": "REGTCN demo statistical detector",
        "transport": "json-values",
        "input_size": 1024,
        "observed_at": datetime.now(timezone.utc).isoformat(),
        "metrics": {
            "latency_p95_ms": _percentile(durations, 0.95),
            "errors_5m": errors,
            "timeouts_5m": 0,
            "sample_count": len(calls),
            "error_rate_5m": error_rate,
        },
    }


@app.post("/createTask/", response_model=Task)
def create_task(task: Task):
    started = time.monotonic()
    status = "error"
    try:
        delay_ms = max(0, int(os.getenv("MODEL_DELAY_MS", "20")))
        if delay_ms:
            time.sleep(delay_ms / 1000)
        error_rate = min(1.0, max(0.0, float(os.getenv("MODEL_ERROR_RATE", "0"))))
        if error_rate and random.random() < error_rate:
            raise RuntimeError("injected model failure")

        message = json.loads(task.taskMsg)
        values = [float(value) for value in message.get("values", [])]
        if len(values) != 1024:
            raise ValueError(f"taskMsg.values must contain 1024 samples, got {len(values)}")
        if not all(math.isfinite(value) for value in values):
            raise ValueError("taskMsg.values contains a non-finite sample")

        rms = math.sqrt(sum(value * value for value in values) / len(values))
        diff_rms = math.sqrt(
            sum((values[index] - values[index - 1]) ** 2 for index in range(1, len(values)))
            / (len(values) - 1)
        )
        threshold = float(os.getenv("MODEL_ANOMALY_THRESHOLD", "1.0"))
        result = {
            "is_anomaly": diff_rms > threshold,
            "anomaly_score": round(diff_rms, 8),
            "threshold": threshold,
            "signal_rms": round(rms, 8),
            "model": "REGTCN demo statistical detector",
        }
        task.taskResult = json.dumps(result, ensure_ascii=False)
        task.taskState = 2
        status = "ok"
    except Exception as exc:
        task.taskResult = json.dumps(
            {"message": str(exc), "error_type": type(exc).__name__},
            ensure_ascii=False,
        )
        task.taskState = 3
    finally:
        _record(status, (time.monotonic() - started) * 1000)
    return task
