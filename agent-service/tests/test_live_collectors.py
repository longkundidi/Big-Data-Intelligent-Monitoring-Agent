from streamdoctor.collectors import Collectors
from streamdoctor.config import ROOT, Settings
from streamdoctor.diagnosis import report_for
from streamdoctor.probe import check
from streamdoctor.storage import Store


def configured(tmp_path, **kwargs):
    settings = Settings(db_path=tmp_path / "live.db", topology_path=ROOT / "topology.json", **kwargs)
    return settings, Store(settings.db_path)


def test_model_health_interprets_healthz_status_and_never_infers_health_from_logs(tmp_path, monkeypatch):
    settings, store = configured(tmp_path, model_url="http://model.test/healthz")
    collector = Collectors(settings, store)

    class Response:
        def raise_for_status(self):
            pass

        def json(self):
            return {"status": "error", "model": "REGTCN"}

    class Client:
        def __init__(self, **kwargs):
            pass

        def __enter__(self):
            return self

        def __exit__(self, *args):
            pass

        def get(self, url):
            assert url == "http://model.test/healthz"
            return Response()

    monkeypatch.setattr("streamdoctor.collectors.httpx.Client", Client)
    status, payload, _, _ = collector.collect("model_health")
    assert status == "ok" and payload["state"] == "unhealthy"

    settings, store = configured(tmp_path, model_url="")
    log = tmp_path / "model.log"
    log.write_text("model_call ts_ms=9999999999999 status=ok duration_ms=120\n", encoding="utf-8")
    monkeypatch.setenv("AGENT_MODEL_METRICS_LOG", str(log))
    assert Collectors(settings, store).model_health("")["state"] == "unknown"


def test_partial_flink_metrics_are_visible_as_missing_evidence(tmp_path, monkeypatch):
    settings, store = configured(tmp_path, flink_url="http://flink.test")
    collector = Collectors(settings, store)

    def flink(path):
        if path == "/jobs/overview":
            return {"jobs": [{"jid": "job-1", "name": "algorithm_REGTCN", "state": "RUNNING"}]}
        if path == "/jobs/job-1":
            return {"vertices": [{"id": "v1", "name": "inference", "status": "RUNNING"}]}
        raise RuntimeError("metric endpoint unavailable")

    monkeypatch.setattr(collector, "_flink", flink)
    status, payload, start, end = collector.collect("flink_metrics")
    assert status == "ok" and payload["metric_errors"] == 1
    evidence = [store.add_evidence("probe", "flink_metrics", status, payload, start, end)]
    assert "metric endpoint unavailable" in report_for(evidence, "检查链路", "live")["missing"][0]


def test_flink_status_includes_cluster_capacity_when_job_is_missing(tmp_path, monkeypatch):
    settings, store = configured(tmp_path, flink_url="http://flink.test")
    collector = Collectors(settings, store)

    def flink(path):
        if path == "/overview":
            return {"flink-version": "1.17.2", "taskmanagers": 1, "slots-total": 2,
                    "slots-available": 2, "jobs-running": 0, "jobs-failed": 0, "jobs-finished": 0}
        if path == "/jobs/overview":
            return {"jobs": []}
        raise AssertionError(path)

    monkeypatch.setattr(collector, "_flink", flink)
    status, payload, _, _ = collector.collect("flink_status")
    assert status == "ok"
    assert payload["state"] == "NOT_FOUND"
    assert payload["cluster"] == {"version": "1.17.2", "taskmanagers": 1, "slots_total": 2,
                                  "slots_available": 2, "jobs_running": 0, "jobs_failed": 0,
                                  "jobs_finished": 0}


def test_probe_reports_unavailable_without_using_persistent_db(tmp_path):
    settings = Settings(db_path=tmp_path / "must-not-create.db", topology_path=ROOT / "topology.json",
                        flink_url="", kafka_servers="", model_url="")
    result = check(settings)
    assert all(value["status"] == "unavailable" for value in result.values())
    assert not settings.db_path.exists()
