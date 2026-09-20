# REGTCN audit package

Build `REGTCN.zip` with `REGTCN.py` at the archive root. The script follows the
platform audit contract: it accepts no arguments, performs one real platform
model test, and prints exactly one JSON result to stdout.

The production `/createTask/` API and its InfluxDB 1024-point input contract are
unchanged. This package is only the auditable delivery artifact stored in MinIO.
