import json
import logging
import os
from typing import List, Optional

from fastapi import FastAPI
from pydantic import BaseModel

from inference import FaultDiagnoser


logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
logger = logging.getLogger("elevator-fault-diagnosis")

app = FastAPI(title="Elevator Fault Diagnosis", version="1.0.0")
diagnoser = FaultDiagnoser()


class Task(BaseModel):
    taskId: Optional[int] = None
    taskMsg: str
    taskResult: Optional[str] = None
    taskState: Optional[int] = 0
    taskReUrl: Optional[str] = None


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
        "model": "FFCNet",
        "input_size": 1024,
        "classes": 8,
        "device": "cpu",
        "transport": "json-values",
    }


@app.post("/createTask/")
def create_task(task: Task):
    try:
        task_message = TaskMessage.parse_obj(json.loads(task.taskMsg))
        if task_message.sampleCount != 1024:
            raise ValueError("taskMsg.sampleCount must be 1024")
        result = diagnoser.predict(task_message.values)
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
