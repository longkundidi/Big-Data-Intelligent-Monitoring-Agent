import json
import math
import logging
import os
import time
from datetime import datetime, timedelta, timezone
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from pyflink.common import SimpleStringSchema, Types
from pyflink.datastream import MapFunction, RuntimeContext, StreamExecutionEnvironment
from pyflink.datastream.connectors import FlinkKafkaConsumer, FlinkKafkaProducer


JOB_NAME = "algorithm_REGTCN"
KAFKA_SERVERS = os.getenv("KAFKA_SERVERS", "kafka:9092")
SOURCE_TOPIC = os.getenv("INPUT_TOPIC", "dc_algorithm_REGTCN")
SINK_TOPIC = os.getenv("OUTPUT_TOPIC", "dc_algorithm_sink_REGTCN")
CONSUMER_GROUP_ID = os.getenv("CONSUMER_GROUP", "REGTCN_kafka_group")
MODEL_URL = os.getenv("MODEL_URL", "http://regtcn-model:8000/createTask/")
MONITOR_POINT_ID = "smart-home-aux1-a1-traction-point1"
SAMPLE_COUNT = 1024
BATCH_SECONDS = 30
HTTP_TIMEOUT_SECONDS = 30
LOGGER = logging.getLogger("streamdoctor.inference")


def parse_values(raw_data):
    if isinstance(raw_data, str):
        values = [float(value.strip()) for value in raw_data.split(",") if value.strip()]
    elif isinstance(raw_data, list):
        values = [float(value) for value in raw_data]
    else:
        raise ValueError("dc_data must be a comma-separated string or an array")

    if len(values) != SAMPLE_COUNT:
        raise ValueError(
            "dc_data must contain exactly {} values, got {}".format(
                SAMPLE_COUNT, len(values)
            )
        )
    if not all(math.isfinite(value) for value in values):
        raise ValueError("dc_data contains a non-finite value")
    return values


def format_utc(timestamp):
    return timestamp.astimezone(timezone.utc).isoformat(timespec="milliseconds").replace(
        "+00:00", "Z"
    )


def build_model_request(message, values):
    if "id" not in message:
        raise ValueError("id is required")
    if "dc_time" not in message:
        raise ValueError("dc_time is required")

    task_id = int(message["id"])
    dc_time = int(message["dc_time"])
    end_time = datetime.fromtimestamp(dc_time / 1000.0, tz=timezone.utc)
    start_time = end_time - timedelta(seconds=BATCH_SECONDS)
    task_message = {
        "monitorPointId": MONITOR_POINT_ID,
        "startTime": format_utc(start_time),
        "endTime": format_utc(end_time),
        "sampleCount": SAMPLE_COUNT,
        "values": values,
    }
    return {
        "taskId": task_id,
        "taskMsg": json.dumps(task_message, ensure_ascii=False, separators=(",", ":")),
        "taskState": 1,
        "taskReUrl": "",
    }


def invoke_model(task):
    started = time.monotonic()
    request = Request(
        MODEL_URL,
        data=json.dumps(task, ensure_ascii=False, separators=(",", ":")).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urlopen(request, timeout=HTTP_TIMEOUT_SECONDS) as response:
            response_body = response.read().decode("utf-8")
    except HTTPError as exc:
        LOGGER.error("model_call ts_ms=%d status=http_error duration_ms=%.1f code=%s", int(time.time() * 1000), (time.monotonic() - started) * 1000, exc.code)
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError("REGTCN HTTP {}: {}".format(exc.code, body)) from exc
    except URLError as exc:
        LOGGER.error("model_call ts_ms=%d status=timeout_or_network_error duration_ms=%.1f", int(time.time() * 1000), (time.monotonic() - started) * 1000)
        raise RuntimeError("REGTCN service request failed: {}".format(exc.reason)) from exc

    LOGGER.info("model_call ts_ms=%d status=ok duration_ms=%.1f", int(time.time() * 1000), (time.monotonic() - started) * 1000)

    response_task = json.loads(response_body)
    if response_task.get("taskState") != 2:
        raise RuntimeError(
            "REGTCN inference failed: {}".format(response_task.get("taskResult"))
        )

    task_result = response_task.get("taskResult")
    result = json.loads(task_result) if isinstance(task_result, str) else task_result
    if not isinstance(result, dict) or "is_anomaly" not in result:
        raise RuntimeError("REGTCN returned an invalid taskResult")
    return result


def format_model_result(result):
    is_anomaly = result.get("is_anomaly")
    if not isinstance(is_anomaly, bool):
        raise RuntimeError("REGTCN is_anomaly must be a boolean")

    try:
        rms_hi = float(result["anomaly_score"])
        threshold = float(result["threshold"])
    except (KeyError, TypeError, ValueError) as exc:
        raise RuntimeError("REGTCN returned an invalid score or threshold") from exc

    if not math.isfinite(rms_hi) or not math.isfinite(threshold):
        raise RuntimeError("REGTCN returned a non-finite score or threshold")

    return {
        "rms_hi": rms_hi,
        "threshold": threshold,
        "anomaly_flag": 1 if is_anomaly else 0,
    }


def transform_message(raw_message):
    message = json.loads(raw_message)
    values = parse_values(message.get("dc_data"))
    result = format_model_result(invoke_model(build_model_request(message, values)))

    output = dict(message)
    output["dc_data"] = result
    output["anomaly_flag"] = result["anomaly_flag"]
    return json.dumps(output, ensure_ascii=False, separators=(",", ":"))


class REGTCNMapFunction(MapFunction):
    def open(self, runtime_context: RuntimeContext):
        self.subtask_index = runtime_context.get_index_of_this_subtask()

    def map(self, value: str):
        return transform_message(value)


def sink_processing():
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(1)

    consumer = FlinkKafkaConsumer(
        topics=SOURCE_TOPIC,
        deserialization_schema=SimpleStringSchema(),
        properties={
            "bootstrap.servers": KAFKA_SERVERS,
            "group.id": CONSUMER_GROUP_ID,
            "auto.offset.reset": "latest",
        },
    )
    producer = FlinkKafkaProducer(
        topic=SINK_TOPIC,
        serialization_schema=SimpleStringSchema(),
        producer_config={"bootstrap.servers": KAFKA_SERVERS},
    )

    source = env.add_source(consumer).name("dc_algorithm_REGTCN")
    results = source.map(REGTCNMapFunction(), output_type=Types.STRING()).name(
        "REGTCN inference"
    )
    results.add_sink(producer).name("dc_algorithm_sink_REGTCN")
    env.execute(JOB_NAME)


if __name__ == "__main__":
    sink_processing()
