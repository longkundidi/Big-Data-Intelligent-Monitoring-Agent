# Elevator REGTCN Flink job

This PyFlink job consumes one complete 1024-sample elevator window from
`dc_algorithm_REGTCN`, invokes the deployed REGTCN service once, and publishes
the result to `dc_algorithm_sink_REGTCN`.

Input:

```json
{"dc_data":"1.0,2.0,...1024 values...","dc_time":1784715300480,"id":231}
```

Output:

```json
{
  "dc_data": {
    "rms_hi": 0.00317842,
    "threshold": 1.02470964,
    "anomaly_flag": 0
  },
  "dc_time": 1784715300480,
  "id": 231,
  "anomaly_flag": 0
}
```

The job uses the original Kafka timestamp as the window end and preserves it in
the output. Invalid input or a failed model call fails the record processing so
Flink can restart and retry it; the job never emits a fabricated normal result.

The deployable Java job under `src/main/java` is used by the ECS Compose stack.
It reads `KAFKA_SERVERS`, `INPUT_TOPIC`, `OUTPUT_TOPIC`, `CONSUMER_GROUP`, and
`MODEL_URL` from the container environment. Build it locally with:

```bash
mvn -B -DskipTests package
```

The Python implementation remains a readable protocol reference. Manual
PyFlink submission:

```bash
docker exec streamdoctor-flink-jobmanager /opt/flink/bin/flink run -d \
  --python /job/REGTCN/sink.py
```
