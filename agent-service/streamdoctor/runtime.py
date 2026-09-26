"""Bounded Agent execution loop used by the workspace conversations."""

import json
import threading
import time
import uuid
from concurrent.futures import ThreadPoolExecutor
from typing import Any

from .agents import ExecutorAgent, ReviewerAgent
from .coordination import AgentCoordinator
from .context import ContextAssembler
from .diagnosis import report_for
from .general_diagnosis import combine_reports, general_report
from .investigation import EvidencePlanner, causal_analysis
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
        self.reviewer = ReviewerAgent()
        self.planner = EvidencePlanner()
        self.executor_agent = ExecutorAgent(settings.executor_url, settings.executor_token)
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

    def _execute_tool(self, registry, run_id, tool_name, args, round_number):
        call_id = str(uuid.uuid4())
        self.store.add_tool_call(call_id, run_id, tool_name, args)
        self._emit(run_id, "tool_started", {"call_id": call_id, "tool": tool_name,
                                               "round": round_number, "agent_role": "reviewer"})
        try:
            result = registry.execute(tool_name, args)
            status = result.get("status", "ok") if isinstance(result, dict) else "ok"
            self.store.finish_tool_call(call_id, status, result)
            self._emit(run_id, "tool_finished", {"call_id": call_id, "tool": tool_name, "status": status,
                                                    "summary": self._summary(result), "agent_role": "reviewer"})
            return result
        except Exception as exc:
            result = {"status": "unavailable", "error": str(exc)[:250]}
            self.store.finish_tool_call(call_id, "unavailable", result)
            self._emit(run_id, "tool_finished", {"call_id": call_id, "tool": tool_name,
                                                    "status": "unavailable", "summary": result["error"],
                                                    "agent_role": "reviewer"})
            return result

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
        generic = general_report(evidence, question, "live")
        report = combine_reports(report, generic)
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
                ("system", "你是 StreamDoctor 的审查智能体。你只有读取、审查和提出建议的权限。只能根据给出的事实回答，区分事实、假设和建议；保留证据 ID；证据不足时明确说明未知。执行结果只能引用输入中的 execution 字段，不得声称自行执行了命令。"),
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
            coordinator = AgentCoordinator(self.store, run_id, lambda event_type, payload: self._emit(run_id, event_type, payload))
            evidence = []
            initial_order = self._tool_order(question)
            tool_names = []
            round_number = 0
            configuration_mode = "get_configuration_status" in initial_order
            if configuration_mode:
                decisions = [
                    {"tool": "get_configuration_status", "reason": "核对必填连接和链路标识"},
                    {"tool": "get_project_topology", "reason": "核对配置对应的项目拓扑"},
                ]
                for decision in decisions:
                    round_number += 1
                    tool_name = decision["tool"]
                    self._emit(run_id, "investigation_decision", {**decision, "round": round_number,
                                                                     "agent_role": "reviewer"})
                    result = self._execute_tool(registry, run_id, tool_name, {"query": question}, round_number)
                    tool_names.append(tool_name)
                    if isinstance(result, dict) and result.get("evidence_id"):
                        evidence.extend(item for item in self.store.run_evidence(run_id)
                                        if item["id"] == result["evidence_id"])
            elif initial_order:
                called = set()
                mode = "replay" if replay else "live"
                while True:
                    decision = self.planner.decide(
                        question, evidence, called, round_number, mode,
                        capabilities=registry.enabled_diagnostic_tools,
                    )
                    self._emit(run_id, "investigation_decision", {
                        **decision, "round": round_number + 1, "agent_role": "reviewer",
                    })
                    tool_name = decision.get("tool")
                    if not tool_name:
                        self._emit(run_id, "investigation_stopped", {
                            "reason": decision.get("reason"), "stop_reason": decision.get("stop_reason"),
                            "rounds": round_number, "tool_calls": len(called),
                        })
                        break
                    if self._is_cancelled(run_id):
                        return
                    if round_number >= self.MAX_TOOL_CALLS or time.monotonic() - started_monotonic > self.MAX_SECONDS:
                        self._emit(run_id, "budget_exhausted", {"tool_calls": round_number})
                        break
                    round_number += 1
                    called.add(tool_name)
                    tool_names.append(tool_name)
                    self.store.update_run_budget(run_id, {"tool_calls": round_number})
                    result = self._execute_tool(registry, run_id, tool_name, {"query": question}, round_number)
                    if isinstance(result, dict) and result.get("evidence_id"):
                        evidence.extend(item for item in self.store.run_evidence(run_id)
                                        if item["id"] == result["evidence_id"])
                    self._emit(run_id, "hypothesis_updated", {
                        "round": round_number, "hypotheses": self.planner.hypotheses(evidence, question, mode),
                    })
            if self._is_cancelled(run_id):
                return
            if not tool_names:
                answer = "你好，我是 StreamDoctor。你可以描述链路现象，或询问项目配置、Kafka 积压、Flink 作业和模型服务状态。"
                report = {"classification": "conversation", "summary": "普通对话，未执行诊断工具。",
                          "facts": [], "candidates": [], "missing": [], "actions": []}
                model_answer, usage, model_error = None, {"input_tokens": 0, "output_tokens": 0}, None
            else:
                if configuration_mode:
                    answer, report = self._configuration_answer(evidence)
                else:
                    answer, report = self._answer(question, evidence, bundle)
                    report["hypotheses"] = self.planner.hypotheses(
                        evidence, question, "replay" if replay else "live"
                    )

                review_task = coordinator.delegate("reviewer", "review_evidence", {
                    "question": question, "report": report, "evidence": evidence,
                })
                self._emit(run_id, "reviewer_started", {"agent_role": "reviewer", "task_id": review_task["id"],
                                                           "evidence_count": len(evidence)})
                review = self.reviewer.handle(review_task)
                report["review"] = review
                coordinator.complete(review_task["id"], review)
                self._emit(run_id, "reviewer_finished", {**review, "task_id": review_task["id"]})

                intent, blocked_reason = self.executor_agent.plan(question)
                if intent:
                    executor_task = coordinator.delegate("executor", "execute_action", {
                        "action": intent.action, "target": intent.target, "explicit_request": intent.explicit,
                        "source": intent.source, "project_id": project_id, "run_id": run_id,
                        "triggered_by_task_id": review_task["id"],
                    }, parent_task_id=review_task["id"])
                    action_record = self.store.create_execution_action(
                        run_id, project_id, intent.action, intent.target, intent.explicit,
                        status="blocked" if blocked_reason else "running", policy_reason=blocked_reason,
                    )
                    self._emit(run_id, "executor_plan", {
                        "agent_role": "executor", "action_id": action_record["id"], "action": intent.action,
                        "target": intent.target, "explicit_request": intent.explicit,
                    })
                    if blocked_reason:
                        execution = {"status": "blocked", "reason": blocked_reason, "action": intent.action,
                                     "target": intent.target}
                        self.store.finish_execution_action(action_record["id"], "blocked", execution)
                        coordinator.block(executor_task["id"], blocked_reason)
                        self._emit(run_id, "executor_blocked", {"agent_role": "executor", **execution})
                        report["execution"] = execution
                        answer += "\n执行智能体未执行：{}。".format(blocked_reason)
                    else:
                        self._emit(run_id, "executor_started", {"agent_role": "executor",
                                                                  "action_id": action_record["id"],
                                                                  "action": intent.action, "target": intent.target})
                        execution = self.executor_agent.handle(executor_task)
                        final_status = "completed" if execution.get("status") == "ok" else "failed"
                        self.store.finish_execution_action(action_record["id"], final_status, execution)
                        coordinator.complete(executor_task["id"], execution)
                        self._emit(run_id, "executor_finished", {"agent_role": "executor",
                                                                   "action_id": action_record["id"], **execution})
                        report["execution"] = execution
                        answer += "\n执行智能体：{} {}，结果 {}。".format(
                            intent.action, intent.target, execution.get("status"))
                        if execution.get("status") == "ok" and intent.action in {"start", "stop", "restart"}:
                            recheck_tools = {
                                "kafka": ["get_kafka_offsets"],
                                "flink": ["get_flink_status", "get_flink_metrics"],
                                "model": ["get_model_health"],
                                "pipeline": ["get_flink_status", "get_kafka_offsets", "get_model_health"],
                            }[intent.target]
                            for recheck_tool in recheck_tools:
                                round_number += 1
                                recheck_result = self._execute_tool(
                                    registry, run_id, recheck_tool,
                                    {"query": "执行后恢复复查", "force_refresh": True}, round_number,
                                )
                                if isinstance(recheck_result, dict) and recheck_result.get("evidence_id"):
                                    evidence.extend(item for item in self.store.run_evidence(run_id)
                                                    if item["id"] == recheck_result["evidence_id"])
                            recheck_report = report_for(evidence, question, "live")
                            recheck_task = coordinator.delegate("reviewer", "verify_recovery", {
                                "question": question, "report": recheck_report, "evidence": evidence,
                                "execution_result": execution,
                            }, parent_task_id=executor_task["id"])
                            recheck_review = self.reviewer.handle(recheck_task)
                            coordinator.complete(recheck_task["id"], recheck_review)
                            report["recheck"] = recheck_review
                            self._emit(run_id, "reviewer_recheck_finished", {**recheck_review,
                                                                              "task_id": recheck_task["id"]})
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
            if tool_names and not configuration_mode:
                analysis = causal_analysis(evidence, report)
                analysis_artifact = self.store.create_artifact(
                    run_id, "report", analysis, [item["id"] for item in evidence],
                    {"start": min((item["window_start"] for item in evidence), default=None),
                     "end": max((item["window_end"] for item in evidence), default=None)},
                )
                self._emit(run_id, "artifact", {"artifact": analysis_artifact})
                kafka_evidence = next((item for item in reversed(evidence)
                                       if item["source"] == "kafka_offsets" and item["status"] == "ok"), None)
                if kafka_evidence:
                    lag = kafka_evidence["payload"].get("total_lag")
                    previous_lag = kafka_evidence["payload"].get("previous_total_lag")
                    series = []
                    if previous_lag is not None:
                        series.append({"time": kafka_evidence["window_start"], "value": previous_lag})
                    if lag is not None:
                        series.append({"time": kafka_evidence["window_end"], "value": lag})
                    if series:
                        chart_artifact = self.store.create_artifact(
                            run_id, "chart", {"metric": "lag", "unit": "records", "series": series,
                                               "view": "kafka_lag_trend"}, [kafka_evidence["id"]],
                            {"start": kafka_evidence["window_start"], "end": kafka_evidence["window_end"]},
                        )
                        self._emit(run_id, "artifact", {"artifact": chart_artifact})
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
