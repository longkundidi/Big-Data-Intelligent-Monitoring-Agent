# Elevator fault diagnosis deployment

The FFCNet service performs one synchronous eight-class diagnosis per request.
The platform reads InfluxDB, sorts the records by timestamp, validates exactly
1024 finite values, and sends those values directly in JSON. This service does
not read or write MinIO.

Input model shape: `[1, 1, 1024]`.

Class mapping:

0. Normal
1. Partial brake-shoe wear
2. Full brake-shoe wear
3. Oil contamination
4. Foreign matter on the contact surface
5. Insufficient spring braking force
6. Excessive brake-shoe clearance
7. Incomplete brake-shoe contact

Runtime endpoints:

- `GET /healthz`
- `POST /createTask/`

Production deployment:

- Image: `192.168.16.211/algorithm/elevator-fault-diagnosis:0.0.1`
- Container: `elevator-fault-diagnosis`
- Base URL: `http://192.168.16.219:8874`
- Registry alias: `FFCNet`
- Resources: 2 CPU cores and 2 GiB memory
- Runtime: Python 3.10 and PyTorch 2.5.1 CPU
- Model SHA-256: `eac182c52864f973d48fd67364cc441611369740a8f89533f241a8f8b1413e07`

The 45Hz validation set contains 2400 samples. The deployment implementation
reproduces the original network output exactly and achieves 92.4167% accuracy
with 97.7316% mean top-1 confidence.

The model is loaded once at process startup and runs on CPU. A successful call
returns `taskState=2` with the class, confidence, and all eight probabilities.
A validation or inference error returns `taskState=3` with a structured error.

Run `register-model.sql` against the algorithm repository after the service is
deployed. The script is idempotent and marks the record as reviewed, tested, and
deployed using the repository's inverse status convention (`0/0/0`).
