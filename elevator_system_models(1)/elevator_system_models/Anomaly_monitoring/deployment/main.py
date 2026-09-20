import json
import logging
import os
from typing import List, Optional

from fastapi import FastAPI
from pydantic import BaseModel

from inference import AnomalyMonitor


logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
logger = logging.getLogger("elevator-anomaly-monitoring")

app = FastAPI(title="Elevator Anomaly Monitoring", version="1.0.0")
monitor = AnomalyMonitor()


class Task(BaseModel):
    taskId: int
    taskMsg: str
    taskResult: Optional[str] = None
    taskState: int
    taskReUrl: str


class TaskMessage(BaseModel):
    monitorPointId: str
    startTime: str
    endTime: str
    sampleCount: int
    values: List[float]


@app.get("/healthz")
def health():
    return {
        "status": "ok",
        "model": "MaskedEPTNet",
        "input_size": 1024,
        "device": "cpu",
        "calibration_version": monitor.calibration["version"],
        "transport": "json-values",
    }


@app.post("/createTask/")
def create_task(task: Task):
    try:
        task_message = TaskMessage.parse_obj(json.loads(task.taskMsg))
        if task_message.sampleCount != 1024:
            raise ValueError("taskMsg.sampleCount must be 1024")
        result = monitor.predict(task_message.values)
        result.update(
            {
                "monitor_point_id": task_message.monitorPointId,
                "start_time": task_message.startTime,
                "end_time": task_message.endTime,
            }
        )
        task.taskResult = json.dumps(result, ensure_ascii=False)
        task.taskState = 2
    except Exception as exc:
        logger.exception("Task %s failed", task.taskId)
        task.taskResult = json.dumps(
            {"message": str(exc), "error_type": type(exc).__name__},
            ensure_ascii=False,
        )
        task.taskState = 3
    return task
