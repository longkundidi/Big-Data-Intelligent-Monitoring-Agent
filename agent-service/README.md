# StreamDoctor

Kafka/Flink 运行诊断 Agent。运行时分为审查智能体和执行智能体：审查者只读取观测、发现问题并提出建议；执行者只接受用户明确提出的受控动作，并通过隔离执行器操作白名单组件。未配置模型时仍可使用规则流程和离线回放；配置模型后由审查智能体根据证据生成回答，模型输出本身不能授予执行权限。

工作区诊断采用有预算的假设驱动循环。审查智能体先确认拓扑和 Flink 作业状态，再根据已有证据决定是否继续检查 Kafka 位点、算子吞吐与反压、模型健康、日志和排障文档；作业已停止时会提前转向异常与处置依据。每轮保存“为什么调用该工具”、候选假设状态和最终停止原因。报告同时生成主张与证据关系、同一观测窗口的信号时间线和故障传播链；传播链明确标为相关性推断，不把同窗信号误报为统计因果。Flink 证据包含异常、Checkpoint、吞吐、忙碌度和反压摘要，Kafka 证据包含积压变化、分区倾斜与输入输出 Topic 位点变化。

工作区 API 还提供项目、持续会话、短期上下文、项目级长期记忆提议、Run 执行循环、SSE 事件和可视化 Artifact。独立网页在仓库的 `agent-web` 目录中，现有业务 Vue 页面仍保留兼容入口。

项目链路现在使用版本化规格保存。系统内置空白、Kafka 积压、Kafka + Flink、Flink + 模型、Flink + 数据库五套模板；模板和项目规格都保存服务器、服务、节点、关系及诊断能力。模板可以复制为自定义模板并继续发布版本，项目从指定模板版本复制独立规格，因此后续修改模板不会覆盖已有项目。项目规格每次发布保存完整快照和字段级变更，可恢复任意旧版本；每次 Agent Run 同时固定当时的规格版本 ID。

在项目会话输入 `/` 并选择 `/init`，会从 `AGENT_PROJECT_SCAN_ROOT` 指定的只读项目目录查找 Compose、`.env.example`、`topology.json`、应用 YAML/Properties、POM 和包清单，提取 Kafka/Flink/模型端点、Topic、消费组和 Job。扫描结果只发布为当前项目的新规格版本，不修改项目最初使用的原始模板；需要复用时再由用户选择“另存为模板”。扫描器忽略真实 `.env`、Git、依赖、构建产物和数据目录，不采集密码、Token 或 API Key。

## 双智能体与执行边界

- 审查智能体拥有 `observe/review/recommend` 权限，不能调用执行接口，也不能写配置或长期记忆。
- 执行智能体只支持 `status/start/stop/restart`，目标只允许 `kafka/flink/model/pipeline`。任意 Shell、容器名、URL、SQL 和脚本都不会进入执行器。
- 含“是否、建议、怎么、分析”等表达的处置讨论只生成建议。只有 `/restart flink`、`请重启 Flink` 这类明确指令才会执行。
- 每次计划、阻止、执行结果和恢复复查都写入 Run 事件及 `execution_actions` 审计表。
- 协调器通过持久化的 `subagent_tasks` 委派工作，通过 `agent_messages` 接收子智能体结果；执行后会创建关联的审查者复查任务。网页右侧直接展示这些委派和回传消息。
- `executor-service` 是唯一挂载 Docker Socket 的容器，不开放宿主机端口，并使用 `EXECUTOR_SHARED_TOKEN` 验证来自 Agent 的内部请求。部署前必须在服务器 `.env` 设置足够长的随机 Token。

## 启动

使用 Python 3.11 或 3.12。在本目录执行：

```powershell
py -3.12 -m venv .venv
.venv\Scripts\python -m pip install -e ".[test]" --index-url https://pypi.org/simple
.venv\Scripts\python -m uvicorn streamdoctor.api:app --host 127.0.0.1 --port 8099
```

浏览器访问 `http://127.0.0.1:8099/docs` 查看接口。Vue 开发环境通过 `/api/agent` 代理到此端口，访问 `/#/monitor/streamdoctor`。页面可选择七种隔离回放场景，生成报告后点击“恢复复查”切换到正常回放。回放数据明确标为模拟，不能当成真实实验结果。

## 模型配置

工作台按当前 Codex 提供方的模型配置支持 GPT-6 Astra、GPT-5.6 Sol、GPT-5.6 Terra、GPT-5.6 Luna、GPT-5.5 和对应推理强度。调用使用 Responses API；模型 ID 与推理强度会固化到 Run，运行时间线会明确记录调用成功、失败或规则降级。API Key 只从服务端环境变量读取，不进入 SQLite，也不会返回前端。专用变量 `AGENT_MODEL_*` 优先；未设置时兼容 OpenAI SDK 的环境变量，并默认复用 `~/.codex/config.toml` 中非敏感的模型、推理强度和 Responses 端点。设置 `AGENT_USE_CODEX_CONFIG=false` 可禁用该行为。

PowerShell 启动示例：

```powershell
$env:AGENT_MODEL_API_KEY = "你的 OpenAI API Key"
$env:AGENT_MODEL_NAME = "gpt-5.6-luna"
$env:AGENT_REASONING_EFFORT = "high"
.venv\Scripts\python -m uvicorn streamdoctor.api:app --host 127.0.0.1 --port 8099
```

使用 OpenAI 兼容服务时再设置 `AGENT_MODEL_BASE_URL`。兼容服务必须实现 `/v1/responses`，否则运行会显示模型调用失败并保留规则诊断结果。

65.237 主机的实测容量与部署边界见 [DEPLOYMENT-65.237.md](DEPLOYMENT-65.237.md)。

本地默认不采集服务器、不调用模型；`.env.example` 仅作配置参考，不自动加载。ECS 容器部署通过内部地址 `http://jobmanager:8081` 与 `kafka:9092` 采集，并已设置 `AGENT_POLL_ENABLED=true`。链路配置在 `topology.json`，对应仓库的 `elevator-flink-monitoring/sink.py` 和 Java 服务注册信息。模型健康端点必须是 GET JSON；推理 `/createTask/` POST 地址不能充当探针。同步模型调用会输出带时间戳的 `model_call` 日志，可将其有限日志路径配置为 `AGENT_MODEL_METRICS_LOG`。Flink、Kafka 和模型探针失败均记为不可用。

启动自动采样前运行 `python -m streamdoctor.probe` 做只读连通性检查。仓库配置曾使用 Flink `http://192.168.16.219:8000`、Kafka `177.9.0.34:9092,177.9.0.32:9093,177.9.0.28:9094`、模型 `http://192.168.16.219:8873/healthz`；这些是待核对的部署地址，不应直接假定仍可用。探针使用临时数据库，不会生成正式诊断事件。健康端点不返回可识别的 JSON `status` 时报告不可用；非 `ok` 状态不会显示为健康。工作台区分未开启、过期、部分失败及全部失败的采样。

生产环境应由现有网关承接平台身份并转发 bearer JWT，设置 `AGENT_JWKS_URL`、`AGENT_JWT_ISSUER` 和 `AGENT_JWT_AUDIENCE` 进行验签；不配置 JWKS 时 API 仅接受本机连接。端口只绑定宿主机回环地址的容器部署，可以通过 `AGENT_TRUSTED_NETWORKS` 明确加入 Docker 私网 CIDR。审核案例接口在配置 JWKS 时额外要求 token 的 `scope` 包含 `agent:review`；本机离线演示可通过回环访问调用。不要把模型密钥写入前端。单实例 SQLite 适合面试演示，数据库和 checkpoint 文件需要放在持久化目录。事件与证据持久化，自动采样保留七天。

## 接口与运行状态

`GET /api/agent/topology` 返回配置、最近采样、节点状态和发现的问题；`POST /api/agent/observations/collect` 立即执行一次 Flink、Kafka 与模型服务采集；`GET /api/agent/replays` 列出隔离场景；`POST /api/agent/incidents` 接受 `question` 与可选 `replay`；`GET /api/agent/incidents/{id}` 返回报告和证据；`GET /api/agent/incidents/{id}/events` 使用 SSE 的 `Last-Event-ID` 断点续读；`POST /api/agent/incidents/{id}/recheck` 创建关联复查；`POST /api/agent/incidents/{id}/feedback` 保存人工纠正和收录建议；`POST /api/agent/incidents/{id}/review` 提交 `{ "approved": true }` 后才进入知识库，并记录审核事件。回放复查可指定 `{"replay_after":"normal"}`。诊断工具保持只读，只有受控执行器可以执行上述四类白名单动作。

工作区接口使用项目和会话维度：`/api/agent/projects` 管理被监测系统，`/api/agent/projects/{id}/conversations` 管理持续对话，`/api/agent/conversations/{id}/messages` 启动一次有预算的 Agent Run，`/api/agent/runs/{id}/events` 推送工具和报告事件，`/api/agent/projects/{id}/memories/{memory_id}/approve` 确认长期记忆，`/api/agent/artifacts/{id}` 返回受校验的拓扑、图表或表格数据。未配置模型时，Run 仍会按问题选择只读工具并生成规则诊断，明确显示观测缺口和历史回放状态。

模板接口位于 `/api/agent/templates`，支持新增、复制、发布新版本和把旧版本恢复为新版本。项目规格接口位于 `/api/agent/projects/{id}/spec`，支持发布、历史查询、恢复、从对话文本生成草稿以及导入 TXT、Markdown、JSON、YAML 文本文档；`POST /api/agent/projects/{id}/initialize` 执行 `/init` 的安全目录扫描。文档解析只生成待审核草稿，不会直接修改当前规格；项目另存为模板时默认清除主机地址和凭证引用。

归因规则不会把缺少的观测当作零值。Kafka 已提交位点有周期性延迟，需要结合 Flink 吞吐确认；Checkpoint 未开启显示 `disabled`，不能直接视为故障。日志文件仅允许服务端配置的有限本地文件，日志文本不会变为可执行工具。知识库为 `runbooks.json` 的 20 篇短文档，按中文分词与 BM25 检索，引用文档 ID。

## 验证

```powershell
.venv\Scripts\python -m pytest -q
.venv\Scripts\python -m streamdoctor.evaluate
.venv\Scripts\python -m streamdoctor.evaluate --case model_slow
```

当前评估覆盖七个手工构造的隔离回放样本，只是自检，不代表真实准确率。配置 `AGENT_MODEL_API_KEY`、`AGENT_MODEL_NAME` 和可选的 `AGENT_MODEL_BASE_URL` 后，可运行 `python -m streamdoctor.evaluate --compare --output comparison.json`，让规则、读取相同冻结快照的固定摘要模型、可自主调用只读工具的 Agent 共用案例和模型配置。输出 Top-1/Top-3、正常样本误报、证据不足时正确保留判断、引用有效性、耗时、工具次数与 Token；模型无用量元数据时 Token 字段为零，不能据此推断实际免费。手工合成样本仍不能替代隔离故障实验；正式准确率与故障恢复时间须待服务器恢复后采集，同一故障批次的相邻快照不得分到调试和测试两侧。现有前端工程较大，生产构建可用 `node --max-old-space-size=6144 node_modules/vite/bin/vite.js build`。
