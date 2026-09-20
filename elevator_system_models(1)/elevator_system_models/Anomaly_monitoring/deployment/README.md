# Elevator anomaly monitoring deployment

The service performs one inference for each `POST /createTask/` call. The platform
loads the requested time range from InfluxDB, validates exactly 1024 finite values,
and sends those values directly to the model in JSON.

Runtime endpoints:

- `GET /healthz`
- `POST /createTask/`

The deployed production instance is:

- Image: `192.168.16.211/algorithm/elevator-anomaly-monitoring:0.0.1`
- Container: `elevator-anomaly-monitoring`
- Base URL: `http://192.168.16.219:8873`
- Registry alias: `REGTCN`
- Registry category: state evaluation

## Input contract

The business request supplied to the platform contains an InfluxDB point and an
ISO-8601 time range:

```json
{
  "monitorPointId": "elevator-point-id",
  "startTime": "2026-07-17T00:00:00Z",
  "endTime": "2026-07-17T00:00:02Z"
}
```

The platform sorts the InfluxDB records by timestamp and flattens scalar or
comma-separated values. The selected range must contain exactly 1024 finite
numeric values. It sends `sampleCount=1024` and `values` directly to the model;
MinIO is not involved. The model tensor shape is `[1, 1, 1024]`.

The internal `taskMsg` built by the platform is:

```json
{
  "monitorPointId": "elevator-point-id",
  "startTime": "2026-07-17T00:00:00Z",
  "endTime": "2026-07-17T00:00:02Z",
  "sampleCount": 1024,
  "values": [0.1, 0.2, 0.3]
}
```

The example `values` array is abbreviated; the actual request must contain 1024 values.

## Output contract

A successful synchronous response has `taskState=2`. `taskResult` contains:

```json
{
  "status": "normal",
  "is_anomaly": false,
  "anomaly_score": 0.00317842,
  "threshold": 1.02470964,
  "raw_reconstruction_error": 67.990562438965,
  "sample_count": 1024,
  "calibration_version": "masked-eptnet-45hz-v1"
}
```

Invalid data produces a structured business error before model invocation. A
range with 500 values, for example, is rejected as requiring exactly 1024.

## Runtime profile

- Device: CPU
- Limit: 2 CPU cores and 2 GiB memory
- Model SHA-256: `466c66f76dc854cf45cb75f17ffa1194b97cfc390c93a8cf8f9af3ff1d5a2261`
- Calibrated threshold: `1.0247096360987986`
- Validation AUC: `0.9482984`
- Validation accuracy: `0.9491667`
- Validation F1: `0.9706024`

Run `register-model.sql` against the algorithm repository database to insert or
update the `REGTCN` state-evaluation record. The script marks it as reviewed,
tested, and deployed using this database's inverse status convention
(`is_check=0`, `is_pass=0`, `is_deployed=0`).

The model is loaded once when the container starts. Inference is synchronous and
returns `taskState=2` with the completed result, or `taskState=3` with a structured
error. Calibration is generated offline by `calibrate.py` and copied into the image.

Production deployment uses `docker-compose.yaml`. The optional `.env` file only
controls the number of PyTorch CPU threads.
