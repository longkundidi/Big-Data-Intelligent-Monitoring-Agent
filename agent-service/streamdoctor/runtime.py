"""Bounded Agent execution loop used by the workspace conversations."""

import json
import threading
import time
import uuid
from concurrent.futures import ThreadPoolExecutor
from typing import Any

from .context import ContextAssembler
from .diagnosis import report_for
from .memory import MemoryManager
from .model_catalog import validate_selection
from .tools.registry import ToolError, ToolRegistry


class AgentRuntime:
    MAX_MODEL_CALLS = 8
    MAX_TOOL_CALLS = 20
    MAX_SECONDS = 120

    def __init__(self, settings, store):
        self.settings = settings
        self.store = store
        self.context = ContextAssembler(store)
        self.memory = MemoryManager(store)
        self.executor = ThreadPoolExecutor(max_workers=2, thread_name_prefix="streamdoctor-run")
        self._cancelled: set[str] = set()
        self._lock = threading.RLock()

    def create_and_submit(self, conversation_id, project_id, content, context=None, replay=None):
        active = self.store.active_run(conversation_id)
        if active:
            return self.store.run(active), False
        run_id = str(uuid.uuid4())
        conversation = self.store.conversation(conversation_id) or {}
        model_id, reasoning_effort = validate_selection(
            self.settings, conversation.get("model_id"), conversation.get("reasoning_effort")
        )
        budget = {"model_calls": 0, "tool_calls": 0, "max_model_calls": self.MAX_MODEL_CALLS,
                  "max_tool_calls": self.MAX_TOOL_CALLS, "max_seconds": self.MAX_SECONDS,
                  "model_id": model_id, "reasoning_effort": reasoning_effort}
        self.store.create_run(conversation_id, run_id, budget, model_id, reasoning_effort)
        self.store.run_event(run_id, "run_queued", {
            "conversation_id": conversation_id, "model_id": model_id,
            "reasoning_effort": reasoning_effort,
        })
        self.executor.submit(self.run, run_id, conversation_id, project_id, content, context or {}, replay)
        return self.store.run(run_id), True

    def stop(self, run_id):
        with self._lock:
            self._cancelled.add(run_id)
        run = self.store.run(run_id)
        if run and run["status"] in {"queued", "running"}:
            self.store.set_run(run_id, "cancelled")
            self.store.run_event(run_id, "run_cancelled", {})
        return self.store.run(run_id)

    def _is_cancelled(self, run_id):
        with self._lock:
            return run_id in self._cancelled

    def _emit(self, run_id, event_type, payload):
        return self.store.run_event(run_id, event_type, payload)

    @staticmethod
    def _tool_order(question: str):
        text = question.lower()
        normalized = "".join(character for character in text if character.isalnum())
        if normalized in {"你好", "您好", "hello", "hi", "嗨", "在吗", "你是谁"}:
            return []
        if any(word in text for word in ("配置完整", "链路配置", "怎么配置", "配置文件", "配置在哪", "查看配置")):
            return ["get_configuration_status", "get_project_topology"]
        order = ["get_project_topology", "get_flink_status", "get_kafka_offsets", "get_flink_metrics", "get_model_health"]
        if any(word in text for word in ("日志", "异常", "报错", "timeout", "error")):
            order.append("get_component_logs")
        if any(word in text for word in ("文档", "怎么排查", "经验", "原因")):
            order.append("search_runbooks")
        if any(word in text for word in ("图", "曲线", "趋势", "可视化", "积压", "拓扑")):
            order.append("create_topology_artifact")
            order.append("create_chart_artifact")
        return list(dict.fromkeys(order))

    def _answer(self, question, evidence, context):
        report = report_for(evidence, question, "live")
        classification = report.get("classification")
        answer = report.get("summary", "诊断完成") + "。"
        if report.get("facts"):
            answer += "\n已观测：" + "；".join(report["facts"][:4])
        if report.get("candidates"):
            answer += "\n优先核查：" + "；".join(item["title"] for item in report["candidates"][:3])
        if report.get("missing"):
            answer += "\n观测缺口：" + "；".join(report["missing"][:3])
        if context.memories:
            answer += "\n已参考当前项目的 {} 条已确认经验。".format(len(context.memories))
        return answer, report

    @staticmethod
    def _configuration_answer(evidence):
        item = next((entry for entry in evidence if entry.get("source") == "configuration_status"), None)
        configuration = item.get("payload", {}) if item else {}
        missing = [entry for entry in configuration.get("items", []) if entry.get("required") and entry.get("status") != "complete"]
        facts = ["{}：{}（{}）".format(entry["label"], entry.get("value"), entry.get("source"))
                 for entry in configuration.get("items", []) if entry.get("status") == "complete"]
        actions = [entry.get("fix") for entry in missing if entry.get("fix")]
        summary = "当前配置完整度 {}%，{}/{} 项必填配置完整。".format(
            configuration.get("score", 0), configuration.get("complete_required", 0),
            configuration.get("total_required", 0),
        )
        answer = summary
        if missing:
            answer += "\n仍需补充：" + "；".join(entry["label"] for entry in missing) + "。"
        answer += "\n查看与修改位置：网页“资源与链路 → 编辑规格”；部署连接在 compose.agent.yml 或 agent-service 启动环境变量中。"
        report = {"classification": "configuration_incomplete" if missing else "configuration_complete",
                  "summary": summary, "facts": facts, "candidates": [],
                  "missing": [entry["label"] for entry in missing], "actions": actions}
        return answer, report

    @staticmethod
    def _message_text(response):
        content = getattr(response, "content", "")
        if isinstance(content, str):
            return content.strip()
        if isinstance(content, list):
            chunks = []
            for block in content:
                if isinstance(block, dict) and block.get("type") in {"text", "output_text"}:
                    chunks.append(str(block.get("text", "")))
            return "\n".join(chunks).strip()
        return str(content).strip()

    def _model_synthesis(self, run_id, question, evidence, report, bundle, model_id, reasoning_effort):
        if not self.settings.model_api_key:
            return None, {"input_tokens": 0, "output_tokens": 0}, "服务端未配置 AGENT_MODEL_API_KEY"
        try:
            from langchain_openai import ChatOpenAI
            self._emit(run_id, "model_started", {
                "model_id": model_id, "reasoning_effort": reasoning_effort, "api": "responses",
            })
            model = ChatOpenAI(
                model=model_id,
                api_key=self.settings.model_api_key,
                base_url=self.settings.model_base_url or None,
                timeout=30,
                max_retries=1,
                use_responses_api=True,
                reasoning={"effort": reasoning_effort},
            )
            prompt = {
                "project": bundle.project.get("name"), "question": question,
                "project_spec": bundle.spec,
                "conversation": bundle.messages,
                "report": report, "evidence": [{"id": item["id"], "source": item["source"], "status": item["status"], "payload": item["payload"]} for item in evidence],
                "memory": bundle.memories,
            }
            response = model.invoke([
                ("system", "你是 StreamDoctor 运行诊断 Agent。只能根据给出的事实回答，区分事实、假设和建议；保留证据 ID；证据不足时明确说明未知，不执行任何操作。"),
                ("user", json.dumps(prompt, ensure_ascii=False)),
            ])
            usage = getattr(response, "usage_metadata", None) or {}
            return self._message_text(response) or None, {
                "input_tokens": usage.get("input_tokens", 0), "output_tokens": usage.get("output_tokens", 0),
                "model_id": model_id, "reasoning_effort": reasoning_effort,
            }, None
        except Exception as exc:
            return None, {
                "input_tokens": 0, "output_tokens": 0, "model_id": model_id,
                "reasoning_effort": reasoning_effort,
            }, str(exc)[:300]

    def run(self, run_id, conversation_id, project_id, question, context_data, replay=None):
        try:
            started_monotonic = time.monotonic()
            self.store.set_run(run_id, "running")
            run_config = self.store.run(run_id)
            model_id = run_config.get("model_id")
            reasoning_effort = run_config.get("reasoning_effort")
            self._emit(run_id, "run_started", {
                "project_id": project_id, "model_id": model_id,
                "reasoning_effort": reasoning_effort,
            })
            bundle = self.context.assemble(project_id, conversation_id, question,
                                           context_data.get("resource_ids", []), context_data.get("time_range"))
            self._emit(run_id, "context_ready", {"memory_count": len(bundle.memories), "message_count": len(bundle.messages),
                                                   "selected_resource_ids": bundle.selected_resource_ids,
                                                   "spec_version_id": bundle.spec.get("id") if bundle.spec else None})
            registry = ToolRegistry(self.settings, self.store, project_id, run_id, replay=replay)
            evidence = []
            tool_names = self._tool_order(question)
            for round_number, tool_name in enumerate(tool_names, start=1):
                if self._is_cancelled(run_id):
                    return
                if round_number > self.MAX_TOOL_CALLS or time.monotonic() - started_monotonic > self.MAX_SECONDS:
                    self._emit(run_id, "budget_exhausted", {"tool_calls": round_number - 1})
                    break
                self.store.update_run_budget(run_id, {"tool_calls": round_number})
                call_id = str(uuid.uuid4())
                args = {"query": question}
                self.store.add_tool_call(call_id, run_id, tool_name, args)
                self._emit(run_id, "tool_started", {"call_id": call_id, "tool": tool_name, "round": round_number})
                try:
                    result = registry.execute(tool_name, args)
                    status = result.get("status", "ok") if isinstance(result, dict) else "ok"
                    self.store.finish_tool_call(call_id, status, result)
                    self._emit(run_id, "tool_finished", {"call_id": call_id, "tool": tool_name, "status": status,
                                                            "summary": self._summary(result)})
                    if isinstance(result, dict) and result.get("evidence_id"):
                        evidence.extend(item for item in self.store.run_evidence(run_id)
                                        if item["id"] == result["evidence_id"])
                except Exception as exc:
                    result = {"status": "unavailable", "error": str(exc)[:250]}
                    self.store.finish_tool_call(call_id, "unavailable", result)
                    self._emit(run_id, "tool_finished", {"call_id": call_id, "tool": tool_name, "status": "unavailable",
                                                            "summary": result["error"]})
            if self._is_cancelled(run_id):
                return
            if not tool_names:
                answer = "你好，我是 StreamDoctor。你可以描述链路现象，或询问项目配置、Kafka 积压、Flink 作业和模型服务状态。"
                report = {"classification": "conversation", "summary": "普通对话，未执行诊断工具。",
                          "facts": [], "candidates": [], "missing": [], "actions": []}
                model_answer, usage, model_error = None, {"input_tokens": 0, "output_tokens": 0}, None
            else:
                if "get_configuration_status" in tool_names:
                    answer, report = self._configuration_answer(evidence)
                else:
                    answer, report = self._answer(question, evidence, bundle)
                model_answer, usage, model_error = self._model_synthesis(
                    run_id, question, evidence, report, bundle, model_id, reasoning_effort
                )
            if model_answer:
                answer = model_answer
                self.store.update_run_budget(run_id, {"model_calls": 1})
                self._emit(run_id, "model_usage", usage)
                self._emit(run_id, "model_finished", usage)
            elif model_error:
                event_type = "model_skipped" if not self.settings.model_api_key else "model_failed"
                self._emit(run_id, event_type, {
                    "model_id": model_id, "reasoning_effort": reasoning_effort,
                    "reason": model_error, "fallback": "rule_diagnosis",
                })
            if replay:
                report["mode"] = "replay"
                answer = "历史回放：" + answer
            if any(keyword in question for keyword in ("记住", "保存经验", "收录案例")) and report.get("candidates"):
                candidate = report["candidates"][0]
                memory = self.memory.propose(project_id, "experience", candidate["title"],
                                              [item["id"] for item in evidence])
                self._emit(run_id, "memory_proposal", {"memory": memory})
            artifacts = self.store.artifacts(run_id)
            self.store.add_message(conversation_id, "assistant", answer,
                                   {"run_id": run_id, "report": report, "artifact_ids": [item["id"] for item in artifacts]}, run_id=run_id)
            self.store.set_conversation_summary(conversation_id, self._summary_text(bundle, question, report))
            self._emit(run_id, "message", {"content": answer, "report": report})
            self._emit(run_id, "run_finished", {"status": "completed", "classification": report.get("classification"),
                                                  "artifact_ids": [item["id"] for item in artifacts]})
            self.store.set_run(run_id, "completed")
        except Exception as exc:
            self.store.set_run(run_id, "failed", str(exc)[:500])
            self._emit(run_id, "run_failed", {"error": str(exc)[:500]})

    @staticmethod
    def _summary(result):
        if not isinstance(result, dict):
            return str(result)[:300]
        payload = result.get("data", result)
        return json.dumps(payload, ensure_ascii=False)[:500]

    @staticmethod
    def _summary_text(bundle, question, report):
        return "问题：{}\n结论：{}\n事实：{}\n待验证：{}".format(
            question[:500], report.get("summary", ""), "；".join(report.get("facts", [])[:4]),
            "；".join(item.get("verify", "") for item in report.get("candidates", [])[:3]))[:6000]
