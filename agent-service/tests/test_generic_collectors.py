from streamdoctor.collectors import Collectors
from streamdoctor.config import ROOT, Settings
from streamdoctor.storage import Store


def collector_with_spec(tmp_path, spec):
    settings = Settings(db_path=tmp_path / "generic.db", topology_path=ROOT / "topology.json",
                        flink_url="", kafka_servers="", model_url="")
    store = Store(settings.db_path)
    store.create_project("generic", "通用项目", "", "generic")
    store.publish_project_spec("generic", spec, "test", "manual")
    return Collectors(settings, store, project_id="generic")


def base_spec(services):
    return {"schema_version": 1, "name": "test", "hosts": [
        {"id": "host", "name": "host", "environment": "test", "address": "127.0.0.1"}],
        "services": services, "nodes": [], "edges": [],
        "diagnostics": {"tools": [], "metrics": [], "runbook_tags": [], "quick_questions": []}}


def test_http_health_uses_only_spec_endpoint_and_reports_latency(tmp_path, monkeypatch):
    collector = collector_with_spec(tmp_path, base_spec([{
        "id": "api", "name": "API", "type": "api", "host_id": "host", "port": 8080,
        "protocol": "http", "version": "", "config": {"health_path": "/health", "latency_threshold_ms": 500}}]))

    class Response:
        status_code = 200

    class Client:
        def __init__(self, **kwargs):
            assert kwargs == {"timeout": 4, "follow_redirects": False}
        def __enter__(self): return self
        def __exit__(self, *args): pass
        def get(self, url):
            assert url == "http://127.0.0.1:8080/health"
            return Response()

    monkeypatch.setattr("streamdoctor.collectors.httpx.Client", Client)
    result = collector.service_health("")
    assert result["healthy"] == 1 and result["unhealthy"] == 0
    assert result["services"][0]["state"] == "healthy"
    assert result["services"][0]["latency_threshold_ms"] == 500


def test_prometheus_executes_allowlisted_query_and_normalizes_signal(tmp_path, monkeypatch):
    collector = collector_with_spec(tmp_path, base_spec([{
        "id": "prom", "name": "Prometheus", "type": "prometheus", "host_id": "host", "port": 9090,
        "protocol": "http", "version": "", "config": {"queries": {
            "service.error_rate": {"query": "sum(rate(errors[5m]))", "threshold": 0.05,
                                   "unit": "ratio", "component": "api"}}}}]))

    class Response:
        def raise_for_status(self): pass
        def json(self):
            return {"status": "success", "data": {"result": [{"value": [1, "0.12"]}]}}

    class Client:
        def __init__(self, **kwargs): assert kwargs == {"timeout": 5}
        def __enter__(self): return self
        def __exit__(self, *args): pass
        def get(self, url, params):
            assert url == "http://127.0.0.1:9090/api/v1/query"
            assert params == {"query": "sum(rate(errors[5m]))"}
            return Response()

    monkeypatch.setattr("streamdoctor.collectors.httpx.Client", Client)
    signal = collector.prometheus_metrics("")["signals"][0]
    assert signal == {"name": "service.error_rate", "value": 0.12, "threshold": 0.05,
                      "unit": "ratio", "component": "api", "status": "warning", "series_count": 1}
