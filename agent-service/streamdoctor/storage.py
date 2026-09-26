import json
import sqlite3
import threading
import uuid
from contextlib import contextmanager
from datetime import datetime, timedelta, timezone


def now():
    return datetime.now(timezone.utc).isoformat()


class Store:
    def __init__(self, path):
        self.path = str(path)
        self.lock = threading.RLock()
        with self.connection() as db:
            db.executescript("""
                CREATE TABLE IF NOT EXISTS incidents (
                    id TEXT PRIMARY KEY, chain_id TEXT NOT NULL, kind TEXT NOT NULL,
                    question TEXT NOT NULL, mode TEXT NOT NULL, status TEXT NOT NULL,
                    created_at TEXT NOT NULL, parent_id TEXT, report TEXT, feedback TEXT
                );
                CREATE TABLE IF NOT EXISTS evidence (
                    id TEXT PRIMARY KEY, incident_id TEXT NOT NULL, source TEXT NOT NULL,
                    collected_at TEXT NOT NULL, window_start TEXT NOT NULL, window_end TEXT NOT NULL,
                    status TEXT NOT NULL, payload TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS events (
                    seq INTEGER PRIMARY KEY AUTOINCREMENT, incident_id TEXT NOT NULL,
                    kind TEXT NOT NULL, created_at TEXT NOT NULL, payload TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS samples (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, chain_id TEXT NOT NULL,
                    source TEXT NOT NULL, collected_at TEXT NOT NULL, status TEXT NOT NULL,
                    payload TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_incidents_chain ON incidents(chain_id,kind,status);
                CREATE INDEX IF NOT EXISTS idx_evidence_incident ON evidence(incident_id);
                CREATE INDEX IF NOT EXISTS idx_events_incident ON events(incident_id,seq);
                CREATE INDEX IF NOT EXISTS idx_samples_chain ON samples(chain_id,source,collected_at);
                CREATE TABLE IF NOT EXISTS projects (
                    id TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT NOT NULL DEFAULT '',
                    topology_id TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'active',
                    created_at TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS resources (
                    id TEXT PRIMARY KEY, project_id TEXT NOT NULL, name TEXT NOT NULL,
                    type TEXT NOT NULL, config TEXT NOT NULL DEFAULT '{}', status TEXT NOT NULL DEFAULT 'active',
                    created_at TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_resources_project ON resources(project_id,status);
                CREATE TABLE IF NOT EXISTS conversations (
                    id TEXT PRIMARY KEY, project_id TEXT NOT NULL, title TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'active', created_at TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_conversations_project ON conversations(project_id,updated_at);
                CREATE TABLE IF NOT EXISTS messages (
                    id TEXT PRIMARY KEY, conversation_id TEXT NOT NULL, run_id TEXT,
                    role TEXT NOT NULL, content TEXT NOT NULL, context_json TEXT NOT NULL DEFAULT '{}',
                    request_id TEXT, created_at TEXT NOT NULL
                );
                CREATE UNIQUE INDEX IF NOT EXISTS idx_messages_request ON messages(conversation_id,request_id) WHERE request_id IS NOT NULL;
                CREATE TABLE IF NOT EXISTS conversation_summaries (
                    conversation_id TEXT PRIMARY KEY, content TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS runs (
                    id TEXT PRIMARY KEY, conversation_id TEXT NOT NULL, status TEXT NOT NULL,
                    budget_json TEXT NOT NULL, started_at TEXT, finished_at TEXT, error TEXT,
                    created_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_runs_conversation ON runs(conversation_id,created_at);
                CREATE TABLE IF NOT EXISTS run_events (
                    seq INTEGER PRIMARY KEY AUTOINCREMENT, run_id TEXT NOT NULL,
                    type TEXT NOT NULL, payload TEXT NOT NULL, created_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_run_events_run ON run_events(run_id,seq);
                CREATE TABLE IF NOT EXISTS tool_calls (
                    id TEXT PRIMARY KEY, run_id TEXT NOT NULL, tool_name TEXT NOT NULL,
                    arguments TEXT NOT NULL, status TEXT NOT NULL, result TEXT,
                    started_at TEXT NOT NULL, finished_at TEXT
                );
                CREATE INDEX IF NOT EXISTS idx_tool_calls_run ON tool_calls(run_id,started_at);
                CREATE TABLE IF NOT EXISTS memories (
                    id TEXT PRIMARY KEY, project_id TEXT NOT NULL, kind TEXT NOT NULL,
                    content TEXT NOT NULL, status TEXT NOT NULL, source_ids TEXT NOT NULL DEFAULT '[]',
                    created_at TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_memories_project ON memories(project_id,status);
                CREATE TABLE IF NOT EXISTS artifacts (
                    id TEXT PRIMARY KEY, run_id TEXT NOT NULL, kind TEXT NOT NULL,
                    data_json TEXT NOT NULL, source_evidence_ids TEXT NOT NULL DEFAULT '[]',
                    time_range TEXT, created_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS run_evidence (
                    id TEXT PRIMARY KEY, run_id TEXT NOT NULL, source TEXT NOT NULL,
                    collected_at TEXT NOT NULL, window_start TEXT NOT NULL, window_end TEXT NOT NULL,
                    status TEXT NOT NULL, payload TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_run_evidence_run ON run_evidence(run_id,source);
                CREATE TABLE IF NOT EXISTS chain_templates (
                    id TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT NOT NULL DEFAULT '',
                    category TEXT NOT NULL DEFAULT '自定义', status TEXT NOT NULL DEFAULT 'active',
                    builtin INTEGER NOT NULL DEFAULT 0, current_version INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS chain_template_versions (
                    id TEXT PRIMARY KEY, template_id TEXT NOT NULL, version INTEGER NOT NULL,
                    spec_json TEXT NOT NULL, change_summary TEXT NOT NULL DEFAULT '', created_at TEXT NOT NULL,
                    UNIQUE(template_id,version)
                );
                CREATE INDEX IF NOT EXISTS idx_template_versions ON chain_template_versions(template_id,version);
                CREATE TABLE IF NOT EXISTS project_spec_versions (
                    id TEXT PRIMARY KEY, project_id TEXT NOT NULL, version INTEGER NOT NULL,
                    template_id TEXT, template_version_id TEXT, spec_json TEXT NOT NULL,
                    change_summary TEXT NOT NULL DEFAULT '', source_type TEXT NOT NULL DEFAULT 'manual',
                    source_ref TEXT, created_at TEXT NOT NULL, UNIQUE(project_id,version)
                );
                CREATE INDEX IF NOT EXISTS idx_project_specs ON project_spec_versions(project_id,version);
                CREATE TABLE IF NOT EXISTS spec_changes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, project_spec_version_id TEXT NOT NULL,
                    path TEXT NOT NULL, change_type TEXT NOT NULL, old_value TEXT, new_value TEXT
                );
                CREATE INDEX IF NOT EXISTS idx_spec_changes_version ON spec_changes(project_spec_version_id,id);
                CREATE TABLE IF NOT EXISTS project_documents (
                    id TEXT PRIMARY KEY, project_id TEXT NOT NULL, filename TEXT NOT NULL,
                    media_type TEXT NOT NULL, content_hash TEXT NOT NULL, content TEXT NOT NULL,
                    parse_status TEXT NOT NULL, parse_result TEXT NOT NULL DEFAULT '{}', created_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_project_documents ON project_documents(project_id,created_at);
            """)
            self._ensure_column(db, "incidents", "project_id", "TEXT")
            self._ensure_column(db, "incidents", "conversation_id", "TEXT")
            self._ensure_column(db, "incidents", "run_id", "TEXT")
            self._ensure_column(db, "projects", "current_spec_version_id", "TEXT")
            self._ensure_column(db, "projects", "source_template_id", "TEXT")
            self._ensure_column(db, "runs", "spec_version_id", "TEXT")
            self._ensure_column(db, "conversations", "model_id", "TEXT")
            self._ensure_column(db, "conversations", "reasoning_effort", "TEXT")
            self._ensure_column(db, "runs", "model_id", "TEXT")
            self._ensure_column(db, "runs", "reasoning_effort", "TEXT")
            self._bootstrap_project(db)
            self._bootstrap_templates_and_specs(db)

    @staticmethod
    def _ensure_column(db, table, column, declaration):
        columns = {row[1] for row in db.execute("PRAGMA table_info(" + table + ")")}
        if column not in columns:
            db.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + declaration)

    def _bootstrap_project(self, db):
        row = db.execute("SELECT id FROM projects WHERE id=?", ("elevator-regtcn",)).fetchone()
        if not row:
            timestamp = now()
            db.execute("INSERT INTO projects(id,name,description,topology_id,status,created_at,updated_at) VALUES(?,?,?,?,?,?,?)",
                       ("elevator-regtcn", "电梯监测平台", "Kafka/Flink 电梯 REGTCN 实时监测与运行诊断。", "elevator-regtcn", "active", timestamp, timestamp))
        timestamp = now()
        defaults = [
            ("elevator-kafka", "输入与输出 Kafka", "kafka", {"role": "pipeline"}),
            ("elevator-flink", "REGTCN Flink 作业", "flink", {"job_name": "algorithm_REGTCN"}),
            ("elevator-model", "REGTCN 模型服务", "model", {"health": "server-side"}),
        ]
        for resource_id, name, resource_type, config in defaults:
            exists = db.execute("SELECT id FROM resources WHERE id=?", (resource_id,)).fetchone()
            if not exists:
                db.execute("INSERT INTO resources VALUES(?,?,?,?,?,?,?,?)", (resource_id, "elevator-regtcn", name, resource_type, json.dumps(config, ensure_ascii=False), "active", timestamp, timestamp))

    def _bootstrap_templates_and_specs(self, db):
        from .specs import BUILTIN_TEMPLATES, normalize_spec

        timestamp = now()
        for template in BUILTIN_TEMPLATES:
            exists = db.execute("SELECT id FROM chain_templates WHERE id=?", (template["id"],)).fetchone()
            if not exists:
                db.execute("INSERT INTO chain_templates(id,name,description,category,status,builtin,current_version,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?)",
                           (template["id"], template["name"], template["description"], template["category"], "active", 1, 1, timestamp, timestamp))
                db.execute("INSERT INTO chain_template_versions(id,template_id,version,spec_json,change_summary,created_at) VALUES(?,?,?,?,?,?)",
                           (template["id"] + "-v1", template["id"], 1,
                            json.dumps(normalize_spec(template["spec"]), ensure_ascii=False), "内置模板初始版本", timestamp))
        current = db.execute("SELECT current_spec_version_id FROM projects WHERE id=?", ("elevator-regtcn",)).fetchone()
        if current and not current["current_spec_version_id"]:
            template_version = db.execute("SELECT * FROM chain_template_versions WHERE template_id=? AND version=1", ("flink-model",)).fetchone()
            spec_id = str(uuid.uuid4())
            db.execute("INSERT INTO project_spec_versions(id,project_id,version,template_id,template_version_id,spec_json,change_summary,source_type,source_ref,created_at) VALUES(?,?,?,?,?,?,?,?,?,?)",
                       (spec_id, "elevator-regtcn", 1, "flink-model", template_version["id"], template_version["spec_json"],
                        "迁移现有电梯监测链路", "migration", "topology.json", timestamp))
            db.execute("UPDATE projects SET current_spec_version_id=?,source_template_id=? WHERE id=?",
                       (spec_id, "flink-model", "elevator-regtcn"))

    @contextmanager
    def connection(self):
        db = sqlite3.connect(self.path, timeout=10)
        db.row_factory = sqlite3.Row
        try:
            with db:
                yield db
        finally:
            db.close()

    # Workspace persistence -------------------------------------------------
    def templates(self, include_archived=True):
        query = "SELECT * FROM chain_templates"
        if not include_archived:
            query += " WHERE status='active'"
        query += " ORDER BY builtin DESC,category,name"
        with self.connection() as db:
            rows = [dict(row) for row in db.execute(query).fetchall()]
            for item in rows:
                version = db.execute(
                    "SELECT spec_json FROM chain_template_versions WHERE template_id=? AND version=?",
                    (item["id"], item["current_version"]),
                ).fetchone()
                item["spec"] = json.loads(version["spec_json"]) if version else None
            return rows

    def template(self, template_id, version=None):
        with self.connection() as db:
            row = db.execute("SELECT * FROM chain_templates WHERE id=?", (template_id,)).fetchone()
            if not row:
                return None
            item = dict(row)
            target_version = version or item["current_version"]
            version_row = db.execute("SELECT * FROM chain_template_versions WHERE template_id=? AND version=?",
                                     (template_id, target_version)).fetchone()
        if version_row:
            version_item = dict(version_row)
            version_item["spec"] = json.loads(version_item.pop("spec_json"))
            item["version_data"] = version_item
            item["spec"] = version_item["spec"]
        return item

    def template_versions(self, template_id):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM chain_template_versions WHERE template_id=? ORDER BY version DESC",
                              (template_id,)).fetchall()
        result = []
        for row in rows:
            item = dict(row)
            item["spec"] = json.loads(item.pop("spec_json"))
            result.append(item)
        return result

    def create_template(self, template_id, name, description, category, spec, change_summary="创建模板", builtin=False):
        from .specs import normalize_spec

        timestamp = now()
        spec = normalize_spec(spec)
        version_id = str(uuid.uuid4())
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO chain_templates(id,name,description,category,status,builtin,current_version,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?)",
                       (template_id, name, description, category, "active", int(builtin), 1, timestamp, timestamp))
            db.execute("INSERT INTO chain_template_versions(id,template_id,version,spec_json,change_summary,created_at) VALUES(?,?,?,?,?,?)",
                       (version_id, template_id, 1, json.dumps(spec, ensure_ascii=False), change_summary, timestamp))
        return self.template(template_id)

    def publish_template(self, template_id, spec, change_summary, metadata=None):
        from .specs import normalize_spec

        current = self.template(template_id)
        if not current:
            return None
        version = current["current_version"] + 1
        timestamp = now()
        spec = normalize_spec(spec)
        metadata = metadata or {}
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO chain_template_versions(id,template_id,version,spec_json,change_summary,created_at) VALUES(?,?,?,?,?,?)",
                       (str(uuid.uuid4()), template_id, version, json.dumps(spec, ensure_ascii=False), change_summary, timestamp))
            fields = {key: value for key, value in metadata.items() if key in {"name", "description", "category", "status"} and value is not None}
            fields.update({"current_version": version, "updated_at": timestamp})
            assignments = ",".join(key + "=?" for key in fields)
            db.execute("UPDATE chain_templates SET " + assignments + " WHERE id=?", (*fields.values(), template_id))
        return self.template(template_id)

    def set_template_status(self, template_id, status):
        with self.lock, self.connection() as db:
            db.execute("UPDATE chain_templates SET status=?,updated_at=? WHERE id=?", (status, now(), template_id))
        return self.template(template_id)

    def project_spec(self, project_id, version_id=None):
        with self.connection() as db:
            if version_id:
                row = db.execute("SELECT * FROM project_spec_versions WHERE id=? AND project_id=?", (version_id, project_id)).fetchone()
            else:
                row = db.execute("SELECT s.* FROM project_spec_versions s JOIN projects p ON p.current_spec_version_id=s.id WHERE p.id=?", (project_id,)).fetchone()
        if not row:
            return None
        item = dict(row)
        item["spec"] = json.loads(item.pop("spec_json"))
        item["changes"] = self.spec_changes(item["id"])
        return item

    def project_spec_versions(self, project_id):
        with self.connection() as db:
            rows = db.execute("SELECT id,project_id,version,template_id,template_version_id,change_summary,source_type,source_ref,created_at FROM project_spec_versions WHERE project_id=? ORDER BY version DESC",
                              (project_id,)).fetchall()
        result = []
        for row in rows:
            item = dict(row)
            item["changes"] = self.spec_changes(item["id"])
            result.append(item)
        return result

    def publish_project_spec(self, project_id, spec, change_summary, source_type="manual", source_ref=None,
                             template_id=None, template_version_id=None):
        from .specs import diff_specs, normalize_spec

        spec = normalize_spec(spec)
        current = self.project_spec(project_id)
        version = (current["version"] if current else 0) + 1
        changes = diff_specs(current["spec"] if current else None, spec)
        spec_id = str(uuid.uuid4())
        timestamp = now()
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO project_spec_versions(id,project_id,version,template_id,template_version_id,spec_json,change_summary,source_type,source_ref,created_at) VALUES(?,?,?,?,?,?,?,?,?,?)",
                       (spec_id, project_id, version, template_id, template_version_id, json.dumps(spec, ensure_ascii=False),
                        change_summary, source_type, source_ref, timestamp))
            for change in changes:
                db.execute("INSERT INTO spec_changes(project_spec_version_id,path,change_type,old_value,new_value) VALUES(?,?,?,?,?)",
                           (spec_id, change["path"], change["change_type"], json.dumps(change["old_value"], ensure_ascii=False),
                            json.dumps(change["new_value"], ensure_ascii=False)))
            db.execute("UPDATE projects SET current_spec_version_id=?,source_template_id=COALESCE(?,source_template_id),updated_at=? WHERE id=?",
                       (spec_id, template_id, timestamp, project_id))
        return self.project_spec(project_id, spec_id)

    def spec_changes(self, version_id):
        with self.connection() as db:
            rows = db.execute("SELECT path,change_type,old_value,new_value FROM spec_changes WHERE project_spec_version_id=? ORDER BY id",
                              (version_id,)).fetchall()
        result = []
        for row in rows:
            item = dict(row)
            item["old_value"] = json.loads(item["old_value"]) if item["old_value"] is not None else None
            item["new_value"] = json.loads(item["new_value"]) if item["new_value"] is not None else None
            result.append(item)
        return result

    def save_document(self, project_id, document_id, filename, media_type, content_hash, content, parse_result):
        timestamp = now()
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO project_documents(id,project_id,filename,media_type,content_hash,content,parse_status,parse_result,created_at) VALUES(?,?,?,?,?,?,?,?,?)",
                       (document_id, project_id, filename, media_type, content_hash, content, "parsed",
                        json.dumps(parse_result, ensure_ascii=False), timestamp))
        return self.document(document_id)

    def document(self, document_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM project_documents WHERE id=?", (document_id,)).fetchone()
        if not row:
            return None
        item = dict(row)
        item["parse_result"] = json.loads(item["parse_result"])
        return item

    def documents(self, project_id):
        with self.connection() as db:
            ids = [row["id"] for row in db.execute("SELECT id FROM project_documents WHERE project_id=? ORDER BY created_at DESC", (project_id,)).fetchall()]
        return [self.document(item) for item in ids]

    def projects(self, include_archived=True):
        query = "SELECT * FROM projects"
        params = ()
        if not include_archived:
            query += " WHERE status='active'"
        query += " ORDER BY updated_at DESC"
        with self.connection() as db:
            return [dict(row) for row in db.execute(query, params).fetchall()]

    def project(self, project_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM projects WHERE id=?", (project_id,)).fetchone()
        return dict(row) if row else None

    def create_project(self, project_id, name, description, topology_id):
        timestamp = now()
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO projects(id,name,description,topology_id,status,created_at,updated_at) VALUES(?,?,?,?,?,?,?)",
                       (project_id, name, description, topology_id, "active", timestamp, timestamp))
        return self.project(project_id)

    def update_project(self, project_id, values):
        allowed = {key: value for key, value in values.items() if key in {"name", "description", "status"} and value is not None}
        if not allowed:
            return self.project(project_id)
        allowed["updated_at"] = now()
        assignments = ",".join(key + "=?" for key in allowed)
        with self.lock, self.connection() as db:
            db.execute("UPDATE projects SET " + assignments + " WHERE id=?", (*allowed.values(), project_id))
        return self.project(project_id)

    def resources(self, project_id):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM resources WHERE project_id=? ORDER BY created_at", (project_id,)).fetchall()
        return [{**dict(row), "config": json.loads(row["config"])} for row in rows]

    def create_resource(self, project_id, resource_id, name, resource_type, config):
        timestamp = now()
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO resources VALUES(?,?,?,?,?,?,?,?)",
                       (resource_id, project_id, name, resource_type, json.dumps(config, ensure_ascii=False), "active", timestamp, timestamp))
        return next(item for item in self.resources(project_id) if item["id"] == resource_id)

    def update_resource(self, project_id, resource_id, values):
        allowed = {key: value for key, value in values.items() if key in {"name", "config", "status"} and value is not None}
        if not allowed:
            return next((item for item in self.resources(project_id) if item["id"] == resource_id), None)
        allowed["updated_at"] = now()
        assignments = ",".join(key + "=?" for key in allowed)
        with self.lock, self.connection() as db:
            db.execute("UPDATE resources SET " + assignments + " WHERE id=? AND project_id=?",
                       (*allowed.values(), resource_id, project_id))
        return next((item for item in self.resources(project_id) if item["id"] == resource_id), None)

    def conversations(self, project_id, limit=50, include_archived=False):
        status_clause = "" if include_archived else " AND c.status='active'"
        with self.connection() as db:
            rows = db.execute(
                "SELECT c.*,COUNT(m.id) AS message_count,MAX(m.created_at) AS last_message_at "
                "FROM conversations c LEFT JOIN messages m ON m.conversation_id=c.id "
                "WHERE c.project_id=?" + status_clause + " GROUP BY c.id ORDER BY c.updated_at DESC LIMIT ?",
                (project_id, limit),
            ).fetchall()
        return [dict(row) for row in rows]

    def conversation(self, conversation_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM conversations WHERE id=?", (conversation_id,)).fetchone()
        return dict(row) if row else None

    def migrate_conversation_model_ids(self, replacements):
        with self.lock, self.connection() as db:
            for old_id, new_id in replacements.items():
                db.execute("UPDATE conversations SET model_id=?,updated_at=? WHERE model_id=?", (new_id, now(), old_id))

    def create_conversation(self, project_id, conversation_id, title, model_id=None, reasoning_effort=None):
        timestamp = now()
        with self.lock, self.connection() as db:
            db.execute(
                "INSERT INTO conversations(id,project_id,title,status,created_at,updated_at,model_id,reasoning_effort) "
                "VALUES(?,?,?,?,?,?,?,?)",
                (conversation_id, project_id, title, "active", timestamp, timestamp, model_id, reasoning_effort),
            )
        return self.conversation(conversation_id)

    def update_conversation(self, conversation_id, title=None, status=None, model_id=None, reasoning_effort=None):
        fields = {}
        if title is not None:
            fields["title"] = title
        if status is not None:
            fields["status"] = status
        if model_id is not None:
            fields["model_id"] = model_id
        if reasoning_effort is not None:
            fields["reasoning_effort"] = reasoning_effort
        if not fields:
            return self.conversation(conversation_id)
        fields["updated_at"] = now()
        assignments = ",".join(key + "=?" for key in fields)
        with self.lock, self.connection() as db:
            db.execute("UPDATE conversations SET " + assignments + " WHERE id=?", (*fields.values(), conversation_id))
        return self.conversation(conversation_id)

    def delete_conversation(self, conversation_id):
        with self.lock, self.connection() as db:
            run_ids = [row["id"] for row in db.execute("SELECT id FROM runs WHERE conversation_id=?", (conversation_id,)).fetchall()]
            for run_id in run_ids:
                db.execute("DELETE FROM run_events WHERE run_id=?", (run_id,))
                db.execute("DELETE FROM tool_calls WHERE run_id=?", (run_id,))
                db.execute("DELETE FROM artifacts WHERE run_id=?", (run_id,))
                db.execute("DELETE FROM run_evidence WHERE run_id=?", (run_id,))
            db.execute("DELETE FROM runs WHERE conversation_id=?", (conversation_id,))
            db.execute("DELETE FROM messages WHERE conversation_id=?", (conversation_id,))
            db.execute("DELETE FROM conversation_summaries WHERE conversation_id=?", (conversation_id,))
            db.execute("DELETE FROM conversations WHERE id=?", (conversation_id,))
        return {"id": conversation_id, "deleted": True}

    def touch_conversation(self, conversation_id):
        with self.lock, self.connection() as db:
            db.execute("UPDATE conversations SET updated_at=? WHERE id=?", (now(), conversation_id))

    def add_message(self, conversation_id, role, content, context=None, request_id=None, run_id=None):
        existing = None
        if request_id:
            with self.connection() as db:
                existing = db.execute("SELECT * FROM messages WHERE conversation_id=? AND request_id=?", (conversation_id, request_id)).fetchone()
        if existing:
            return dict(existing), False
        message = {"id": str(uuid.uuid4()), "conversation_id": conversation_id, "run_id": run_id,
                   "role": role, "content": content, "context_json": json.dumps(context or {}, ensure_ascii=False),
                   "request_id": request_id, "created_at": now()}
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO messages VALUES(?,?,?,?,?,?,?,?)", tuple(message.values()))
            db.execute("UPDATE conversations SET updated_at=? WHERE id=?", (message["created_at"], conversation_id))
        return message, True

    def attach_message_run(self, message_id, run_id):
        with self.lock, self.connection() as db:
            db.execute("UPDATE messages SET run_id=? WHERE id=?", (run_id, message_id))

    def messages(self, conversation_id, limit=40):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM messages WHERE conversation_id=? ORDER BY created_at DESC LIMIT ?", (conversation_id, limit)).fetchall()
        result = []
        for row in reversed(rows):
            item = dict(row)
            item["context"] = json.loads(item.pop("context_json"))
            result.append(item)
        return result

    def set_conversation_summary(self, conversation_id, content):
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO conversation_summaries(conversation_id,content,updated_at) VALUES(?,?,?) ON CONFLICT(conversation_id) DO UPDATE SET content=excluded.content,updated_at=excluded.updated_at", (conversation_id, content, now()))

    def conversation_summary(self, conversation_id):
        with self.connection() as db:
            row = db.execute("SELECT content FROM conversation_summaries WHERE conversation_id=?", (conversation_id,)).fetchone()
        return row["content"] if row else ""

    def create_run(self, conversation_id, run_id, budget, model_id=None, reasoning_effort=None):
        with self.lock, self.connection() as db:
            project = db.execute("SELECT p.current_spec_version_id FROM projects p JOIN conversations c ON c.project_id=p.id WHERE c.id=?", (conversation_id,)).fetchone()
            db.execute(
                "INSERT INTO runs(id,conversation_id,status,budget_json,created_at,spec_version_id,model_id,reasoning_effort) "
                "VALUES(?,?,?,?,?,?,?,?)",
                (run_id, conversation_id, "queued", json.dumps(budget), now(),
                 project["current_spec_version_id"] if project else None, model_id, reasoning_effort),
            )
        return self.run(run_id)

    def run(self, run_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM runs WHERE id=?", (run_id,)).fetchone()
        if not row:
            return None
        item = dict(row)
        item["budget"] = json.loads(item.pop("budget_json"))
        return item

    def set_run(self, run_id, status, error=None):
        finished = now() if status in {"completed", "failed", "cancelled", "interrupted"} else None
        started = now() if status == "running" else None
        with self.lock, self.connection() as db:
            db.execute("UPDATE runs SET status=?,error=COALESCE(?,error),started_at=COALESCE(started_at,?),finished_at=COALESCE(?,finished_at) WHERE id=?", (status, error, started, finished, run_id))
        return self.run(run_id)

    def update_run_budget(self, run_id, values):
        current = self.run(run_id)
        if not current:
            return None
        budget = {**current["budget"], **values}
        with self.lock, self.connection() as db:
            db.execute("UPDATE runs SET budget_json=? WHERE id=?", (json.dumps(budget), run_id))
        return self.run(run_id)

    def active_run(self, conversation_id):
        with self.connection() as db:
            row = db.execute("SELECT id FROM runs WHERE conversation_id=? AND status IN ('queued','running') ORDER BY created_at DESC LIMIT 1", (conversation_id,)).fetchone()
        return row["id"] if row else None

    def run_event(self, run_id, event_type, payload):
        with self.lock, self.connection() as db:
            cursor = db.execute("INSERT INTO run_events(run_id,type,payload,created_at) VALUES(?,?,?,?)", (run_id, event_type, json.dumps(payload, ensure_ascii=False), now()))
            seq = cursor.lastrowid
        return {"seq": seq, "run_id": run_id, "type": event_type, "payload": payload}

    def run_events(self, run_id, after=0):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM run_events WHERE run_id=? AND seq>? ORDER BY seq", (run_id, after)).fetchall()
        return [{"seq": row["seq"], "run_id": row["run_id"], "type": row["type"], "payload": json.loads(row["payload"]), "created_at": row["created_at"]} for row in rows]

    def add_tool_call(self, call_id, run_id, name, arguments, status="running"):
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO tool_calls(id,run_id,tool_name,arguments,status,started_at) VALUES(?,?,?,?,?,?)", (call_id, run_id, name, json.dumps(arguments, ensure_ascii=False), status, now()))

    def finish_tool_call(self, call_id, status, result):
        with self.lock, self.connection() as db:
            db.execute("UPDATE tool_calls SET status=?,result=?,finished_at=? WHERE id=?", (status, json.dumps(result, ensure_ascii=False), now(), call_id))

    def create_memory(self, project_id, kind, content, source_ids, status="proposed"):
        memory_id = str(uuid.uuid4())
        timestamp = now()
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO memories VALUES(?,?,?,?,?,?,?,?)", (memory_id, project_id, kind, content, status, json.dumps(source_ids), timestamp, timestamp))
        return self.memory(memory_id)

    def memory(self, memory_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM memories WHERE id=?", (memory_id,)).fetchone()
        if not row:
            return None
        item = dict(row)
        item["source_ids"] = json.loads(item["source_ids"])
        return item

    def memories(self, project_id, include_inactive=False):
        query = "SELECT * FROM memories WHERE project_id=?"
        if not include_inactive:
            query += " AND status='active'"
        query += " ORDER BY updated_at DESC"
        with self.connection() as db:
            rows = db.execute(query, (project_id,)).fetchall()
        return [{**dict(row), "source_ids": json.loads(row["source_ids"])} for row in rows]

    def set_memory_status(self, memory_id, status):
        with self.lock, self.connection() as db:
            db.execute("UPDATE memories SET status=?,updated_at=? WHERE id=?", (status, now(), memory_id))
        return self.memory(memory_id)

    def search_memories(self, project_id, query, limit=5):
        items = self.memories(project_id)
        words = [word for word in query.lower().split() if word]
        def score(item):
            text = item["content"].lower()
            return sum(2 if word in text else 0 for word in words) + (1 if not words else 0)
        return [item for item in sorted(items, key=score, reverse=True)[:limit] if score(item) > 0]

    def create_artifact(self, run_id, kind, data, source_evidence_ids=None, time_range=None):
        artifact_id = str(uuid.uuid4())
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO artifacts VALUES(?,?,?,?,?,?,?)", (artifact_id, run_id, kind, json.dumps(data, ensure_ascii=False), json.dumps(source_evidence_ids or []), json.dumps(time_range, ensure_ascii=False) if time_range else None, now()))
        return self.artifact(artifact_id)

    def artifact(self, artifact_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM artifacts WHERE id=?", (artifact_id,)).fetchone()
        if not row:
            return None
        item = dict(row)
        item["data"] = json.loads(item.pop("data_json"))
        item["source_evidence_ids"] = json.loads(item["source_evidence_ids"])
        item["time_range"] = json.loads(item["time_range"]) if item["time_range"] else None
        return item

    def artifacts(self, run_id):
        with self.connection() as db:
            ids = [row["id"] for row in db.execute("SELECT id FROM artifacts WHERE run_id=? ORDER BY created_at", (run_id,)).fetchall()]
        return [self.artifact(item) for item in ids]

    def add_run_evidence(self, run_id, source, status, payload, window_start, window_end):
        item = {"id": str(uuid.uuid4()), "run_id": run_id, "source": source, "status": status,
                "payload": payload, "collected_at": now(), "window_start": window_start, "window_end": window_end}
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO run_evidence VALUES(?,?,?,?,?,?,?,?)", (item["id"], run_id, source, item["collected_at"], window_start, window_end, status, json.dumps(payload, ensure_ascii=False)))
        return item

    def run_evidence(self, run_id):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM run_evidence WHERE run_id=? ORDER BY collected_at,id", (run_id,)).fetchall()
        return [{**dict(row), "payload": json.loads(row["payload"])} for row in rows]

    def create(self, chain_id, kind, question, mode="live", parent_id=None, deduplicate=False):
        with self.lock, self.connection() as db:
            if deduplicate:
                row = db.execute("SELECT id FROM incidents WHERE chain_id=? AND kind=? AND status IN ('queued','running','open') AND parent_id IS NULL LIMIT 1", (chain_id, kind)).fetchone()
                if row:
                    return row["id"], False
            incident_id = str(uuid.uuid4())
            db.execute("INSERT INTO incidents(id,chain_id,kind,question,mode,status,created_at,parent_id) VALUES(?,?,?,?,?,?,?,?)",
                       (incident_id, chain_id, kind, question, mode, "queued", now(), parent_id))
            self.event(incident_id, "queued", {"question": question}, db)
            return incident_id, True

    def event(self, incident_id, kind, payload, db=None):
        if db is None:
            with self.lock, self.connection() as connection:
                return self.event(incident_id, kind, payload, connection)
        db.execute("INSERT INTO events(incident_id,kind,created_at,payload) VALUES(?,?,?,?)",
                   (incident_id, kind, now(), json.dumps(payload, ensure_ascii=False)))

    def set_status(self, incident_id, status, report=None):
        with self.lock, self.connection() as db:
            db.execute("UPDATE incidents SET status=?,report=COALESCE(?,report) WHERE id=?",
                       (status, json.dumps(report, ensure_ascii=False) if report else None, incident_id))
            self.event(incident_id, status, report or {}, db)

    def add_evidence(self, incident_id, source, status, payload, window_start, window_end):
        item = {"id": str(uuid.uuid4()), "source": source, "status": status, "payload": payload,
                "collected_at": now(), "window_start": window_start, "window_end": window_end}
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO evidence VALUES(?,?,?,?,?,?,?,?)",
                       (item["id"], incident_id, source, item["collected_at"], window_start, window_end,
                        status, json.dumps(payload, ensure_ascii=False)))
            self.event(incident_id, "tool", item, db)
        return item

    def add_sample(self, chain_id, source, status, payload):
        with self.lock, self.connection() as db:
            db.execute("INSERT INTO samples(chain_id,source,collected_at,status,payload) VALUES(?,?,?,?,?)",
                       (chain_id, source, now(), status, json.dumps(payload, ensure_ascii=False)))

    def samples(self, chain_id, source, limit=10):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM samples WHERE chain_id=? AND source=? ORDER BY id DESC LIMIT ?",
                              (chain_id, source, limit)).fetchall()
        return [{**dict(row), "payload": json.loads(row["payload"])} for row in reversed(rows)]

    def prune_samples(self):
        cutoff = (datetime.now(timezone.utc) - timedelta(days=7)).isoformat()
        with self.lock, self.connection() as db:
            db.execute("DELETE FROM samples WHERE collected_at<?", (cutoff,))

    def get(self, incident_id):
        with self.connection() as db:
            row = db.execute("SELECT * FROM incidents WHERE id=?", (incident_id,)).fetchone()
            if not row:
                return None
            evidence = db.execute("SELECT * FROM evidence WHERE incident_id=? ORDER BY collected_at,id", (incident_id,)).fetchall()
        result = dict(row)
        for field in ("report", "feedback"):
            result[field] = json.loads(result[field]) if result[field] else None
        result["evidence"] = [{**dict(item), "payload": json.loads(item["payload"])} for item in evidence]
        return result

    def list(self, limit=50):
        with self.connection() as db:
            rows = db.execute("SELECT id,chain_id,kind,question,mode,status,created_at,parent_id FROM incidents ORDER BY created_at DESC LIMIT ?", (limit,)).fetchall()
        return [dict(row) for row in rows]

    def events(self, incident_id, after=0):
        with self.connection() as db:
            rows = db.execute("SELECT * FROM events WHERE incident_id=? AND seq>? ORDER BY seq", (incident_id, after)).fetchall()
        return [{**dict(row), "payload": json.loads(row["payload"])} for row in rows]

    def feedback(self, incident_id, payload):
        with self.lock, self.connection() as db:
            db.execute("UPDATE incidents SET feedback=? WHERE id=?", (json.dumps(payload, ensure_ascii=False), incident_id))
            self.event(incident_id, "feedback", payload, db)

    def review_feedback(self, incident_id, approved):
        with self.lock, self.connection() as db:
            row = db.execute("SELECT feedback FROM incidents WHERE id=?", (incident_id,)).fetchone()
            if not row or not row["feedback"]:
                raise ValueError("反馈不存在")
            payload = json.loads(row["feedback"])
            payload["approved_for_knowledge"] = approved
            db.execute("UPDATE incidents SET feedback=? WHERE id=?", (json.dumps(payload, ensure_ascii=False), incident_id))
            self.event(incident_id, "review", {"approved": approved}, db)

    def approved_cases(self, question, limit=3):
        import jieba
        from rank_bm25 import BM25Okapi
        with self.connection() as db:
            rows = db.execute("SELECT id,question,report,feedback FROM incidents WHERE feedback IS NOT NULL AND report IS NOT NULL ORDER BY created_at DESC LIMIT 200").fetchall()
        cases = []
        for row in rows:
            feedback = json.loads(row["feedback"])
            if not feedback.get("approved_for_knowledge") or not feedback.get("actual_cause"):
                continue
            cases.append({"id": "incident:" + row["id"], "title": feedback["actual_cause"],
                          "body": row["question"] + " " + feedback.get("notes", ""),
                          "components": ["flink", "kafka", "model"], "version": "all", "source": "人工审核案例"})
        if not cases:
            return []
        tokens = [word for word in jieba.cut(question) if word.strip()]
        scores = BM25Okapi([list(jieba.cut(case["title"] + " " + case["body"])) for case in cases]).get_scores(tokens)
        scores = [max(0, float(score)) + sum(1 for token in tokens if len(token) > 1 and token in case["title"] + case["body"])
                  for score, case in zip(scores, cases)]
        ranked = sorted(enumerate(scores), key=lambda pair: pair[1], reverse=True)
        return [{**cases[index], "score": round(float(score), 3)} for index, score in ranked[:limit] if score > 0]
