"""Read-only connectivity and payload check for a configured live chain."""
import json
import tempfile
from pathlib import Path

from .collectors import Collectors
from .config import Settings
from .storage import Store


def check(settings=None):
    settings = settings or Settings()
    with tempfile.TemporaryDirectory() as directory:
        collector = Collectors(settings, Store(Path(directory) / "probe.db"))
        results = {}
        for name in ("flink_status", "flink_metrics", "kafka_offsets", "model_health"):
            status, payload, _, _ = collector.collect(name)
            result = {"status": status}
            if status != "ok":
                result["error"] = payload.get("error", "观测不可用")
            elif name == "flink_status":
                result.update(state=payload.get("state"), job_id=payload.get("job_id"),
                              checkpoints=payload.get("checkpoint_status"),
                              exceptions_error=payload.get("exceptions_error"))
            elif name == "flink_metrics":
                result.update(job_id=payload.get("job_id"), vertices=len(payload.get("vertices", [])),
                              metric_errors=payload.get("metric_errors", 0))
            elif name == "kafka_offsets":
                result.update(partitions=len(payload.get("partitions", [])), total_lag=payload.get("total_lag"))
            else:
                result.update(state=payload.get("state"), probe_status=payload.get("probe_status"),
                              sample_count=payload.get("metrics", {}).get("sample_count"))
            results[name] = result
        return results


if __name__ == "__main__":
    observations = check()
    print(json.dumps(observations, ensure_ascii=False, indent=2))
    if any(value["status"] != "ok" for value in observations.values()):
        raise SystemExit(1)
