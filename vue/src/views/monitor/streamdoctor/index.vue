<template>
  <main class="doctor">
    <header class="doctor-header">
      <div><p class="eyebrow">STREAMDOCTOR / KAFKA · FLINK</p><h1>运行诊断</h1></div>
      <div class="header-actions">
        <el-tag :type="sampling.type">{{ sampling.label }}</el-tag>
        <el-button :icon="Refresh" title="立即连接 Kafka 和 Flink 采集一次" @click="collectNow" :loading="collecting">立即采集</el-button>
        <el-button :icon="Refresh" title="刷新数据" @click="refresh" :loading="loading" />
      </div>
    </header>

    <section class="doctor-band">
      <div class="section-title"><h2>{{ topology?.name || '电梯 REGTCN 实时监测链路' }}</h2><span>链路概况</span></div>
      <div class="topology" aria-label="数据链路拓扑">
        <template v-for="(node, index) in topology?.nodes || []" :key="node.id">
          <button class="node" :class="[nodeState(node.id), { selected: selectedNode === node.id }]" @click="selectNode(node)">
            <span class="node-index">{{ String(index + 1).padStart(2, '0') }} <i class="state-dot" /></span><strong>{{ node.label }}</strong>
            <small>{{ node.topic || node.type }}</small>
            <span class="node-state">{{ nodeStateLabel(node.id) }}</span>
          </button>
          <span v-if="index < topology.nodes.length - 1" class="connector" aria-hidden="true">→</span>
        </template>
      </div>
      <div class="runtime-grid">
        <div class="runtime-item"><span>Flink 集群</span><strong>{{ flinkCluster }}</strong><small>{{ flinkSlots }}</small></div>
        <div class="runtime-item"><span>目标 Job</span><strong>{{ runtime?.flink?.state || '未知' }}</strong><small>{{ topology?.flink_job_name }}</small></div>
        <div class="runtime-item"><span>输入 Topic</span><strong>{{ topicSummary(topology?.input_topic) }}</strong><small>{{ topology?.input_topic }}</small></div>
        <div class="runtime-item"><span>输出 Topic</span><strong>{{ topicSummary(topology?.output_topic) }}</strong><small>{{ topology?.output_topic }}</small></div>
        <div class="runtime-item"><span>消费组积压</span><strong>{{ lagSummary }}</strong><small>{{ topology?.consumer_group }}</small></div>
        <div class="runtime-item"><span>模型观测</span><strong>{{ runtime?.model?.state || '未接入' }}</strong><small>{{ runtime?.observed_at ? `采集于 ${formatTime(runtime.observed_at)}` : '等待首次采集' }}</small></div>
      </div>
      <div class="issue-panel">
        <div class="section-title"><h2>发现问题</h2><span>{{ runtime?.issues?.length || 0 }}</span></div>
        <div v-for="issue in runtime?.issues || []" :key="`${issue.code}-${issue.node_id}`" class="issue-row">
          <el-tag size="small" :type="severityType(issue.severity)">{{ severityName(issue.severity) }}</el-tag>
          <div><strong>{{ issue.title }}</strong><small>{{ issue.detail }}</small></div>
          <el-button size="small" @click="diagnoseIssue(issue)">交给 Agent 诊断</el-button>
        </div>
        <p v-if="runtime?.observed_at && !runtime.issues.length" class="healthy-line">当前采样未发现平台运行异常。</p>
        <p v-if="!runtime?.observed_at" class="muted">点击“立即采集”连接当前 Kafka 与 Flink。</p>
      </div>
      <div class="diagnose-form">
        <el-input v-model="question" placeholder="描述运行异常或点击链路组件" maxlength="1000" @keyup.enter="diagnose" />
        <el-button type="primary" :icon="Search" :loading="starting" @click="diagnose">开始诊断</el-button>
        <el-select v-model="selectedReplay" class="replay-select" aria-label="回放场景">
          <el-option v-for="item in replays" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button :icon="VideoPlay" :loading="starting" @click="diagnose(selectedReplay)">故障回放</el-button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
    </section>

    <div class="workspace">
      <aside class="incidents">
        <div class="section-title"><h2>诊断事件</h2><span>{{ incidents.length }}</span></div>
        <button v-for="item in incidents" :key="item.id" class="incident" :class="{ active: current?.id === item.id }" @click="open(item.id)">
          <span class="incident-line"><strong>{{ item.question }}</strong><el-tag size="small" :type="item.status === 'failed' ? 'danger' : item.status === 'open' ? 'success' : 'warning'">{{ statusName(item.status) }}</el-tag></span>
          <small>{{ item.mode.startsWith('replay:') ? '历史回放' : '实时诊断' }} · {{ formatTime(item.created_at) }}</small>
        </button>
        <el-empty v-if="!incidents.length" description="暂无诊断事件" :image-size="70" />
      </aside>

      <section class="result">
        <template v-if="current">
          <div class="result-head">
            <div><span class="eyebrow">{{ current.mode.startsWith('replay:') ? '历史回放 / 模拟数据' : '实时观测' }}</span><h2>{{ current.report?.summary || current.question }}</h2></div>
            <el-button :icon="Refresh" :disabled="current.status !== 'open' || current.report?.classification === 'healthy'" @click="recheck">恢复复查</el-button>
          </div>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="诊断报告" name="report">
              <template v-if="current.report?.facts">
                <p class="impact">影响范围：{{ current.report.impact }}</p>
                <div v-if="current.report.comparison" class="comparison">
                  <span>复查：{{ causeName(current.report.comparison.before) }} → {{ causeName(current.report.comparison.after) }}</span>
                  <span v-if="current.report.comparison.lag_before != null && current.report.comparison.lag_after != null">积压 {{ current.report.comparison.lag_before }} → {{ current.report.comparison.lag_after }}</span>
                  <span v-if="current.report.comparison.latency_before_ms != null && current.report.comparison.latency_after_ms != null">P95 {{ current.report.comparison.latency_before_ms }} → {{ current.report.comparison.latency_after_ms }} ms</span>
                  <el-tag size="small" :type="current.report.comparison.recovered || current.report.comparison.observation_restored ? 'success' : 'warning'">{{ current.report.comparison.recovered ? '故障已恢复' : current.report.comparison.observation_restored ? '观测已恢复，原故障未确认' : '仍需排查' }}</el-tag>
                </div>
                <h3>已观测事实</h3><p v-for="fact in current.report.facts" :key="fact" class="fact">{{ fact }}</p>
                <p v-if="!current.report.facts.length" class="muted">暂无线索支持确定结论。</p>
                <h3>候选原因</h3>
                <div v-for="(candidate, index) in current.report.candidates" :key="candidate.code" class="candidate">
                  <span class="rank">{{ String(index + 1).padStart(2, '0') }}</span><div><strong>{{ candidate.title }}</strong>
                    <p>证据 <button v-for="id in candidate.support" :key="id" class="evidence-link" @click="showEvidence(id)">#{{ id.slice(0, 8) }}</button></p>
                    <small>待验证：{{ candidate.verify }}</small></div>
                </div>
                <p v-if="!current.report.candidates.length" class="muted">未找到证据充分的故障原因。</p>
                <h3>建议操作</h3><ol><li v-for="action in current.report.actions" :key="action">{{ action }}</li></ol>
                <p class="muted">{{ current.report.verification }}</p>
                <div v-if="current.report.missing.length" class="missing"><strong>证据缺口</strong><p v-for="item in current.report.missing" :key="item">{{ item }}</p></div>
                <div class="feedback"><span>诊断有帮助吗？</span><el-button size="small" @click="feedback(true)">有帮助</el-button><el-button size="small" @click="feedback(false)">需纠正</el-button></div>
              </template>
              <p v-else-if="current.status === 'failed'" class="error">{{ current.report?.error || '诊断执行失败，请检查服务日志。' }}</p>
              <el-skeleton v-else :rows="5" animated />
            </el-tab-pane>
            <el-tab-pane label="执行时间线" name="timeline"><div v-for="event in timeline" :key="event.seq" class="timeline-row"><time>{{ formatTime(event.created_at) }}</time><strong>{{ eventName(event.kind) }}</strong><span>{{ event.payload.tool || event.payload.source || event.payload.reason || (event.kind === 'model_usage' ? `${event.payload.input_tokens} 输入 / ${event.payload.output_tokens} 输出 Token` : '') }}</span></div></el-tab-pane>
            <el-tab-pane :label="`证据 (${current.evidence.length})`" name="evidence">
              <div v-for="item in current.evidence" :key="item.id" :id="`evidence-${item.id}`" class="evidence-row">
                <div><strong>{{ toolName(item.source) }}</strong><el-tag size="small" :type="item.status === 'ok' ? 'success' : 'warning'">{{ item.status === 'ok' ? '已获取' : '未知' }}</el-tag></div>
                <small>{{ formatTime(item.collected_at) }} · #{{ item.id.slice(0, 8) }}</small><pre>{{ JSON.stringify(item.payload, null, 2) }}</pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </template>
        <el-empty v-else description="选择链路组件或创建诊断事件" />
      </section>
    </div>
    <el-dialog v-model="feedbackVisible" title="诊断反馈" width="min(480px, 92vw)">
      <el-form label-position="top">
        <el-form-item label="实际原因"><el-input v-model="feedbackForm.actual_cause" maxlength="500" placeholder="确认后的故障原因" /></el-form-item>
        <el-form-item label="补充说明"><el-input v-model="feedbackForm.notes" type="textarea" maxlength="1000" :rows="3" /></el-form-item>
        <el-checkbox v-model="feedbackForm.proposed_for_knowledge">建议收录案例（待审核）</el-checkbox>
      </el-form>
      <template #footer><el-button @click="feedbackVisible = false">取消</el-button><el-button type="primary" @click="saveFeedback">保存</el-button></template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, nextTick } from 'vue'
import { Refresh, Search, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTopology, collectObservations, listReplays, listIncidents, getIncident, createIncident, recheckIncident, sendFeedback } from '@/api/agent'
import store from '@/store'

const topology = ref(null), incidents = ref([]), current = ref(null), timeline = ref([]), replays = ref([])
const selectedNode = ref(''), question = ref('为什么数据链路没有继续产生结果？')
const selectedReplay = ref('model_slow')
const loading = ref(false), collecting = ref(false), starting = ref(false), error = ref(''), activeTab = ref('report')
const feedbackVisible = ref(false), feedbackForm = ref({ useful: false, actual_cause: '', notes: '', proposed_for_knowledge: false })
let aborter = null, timer = null
const sampling = computed(() => {
  const names = ['flink_status', 'flink_metrics', 'kafka_offsets', 'model_health']
  const latest = names.map(name => topology.value.samples?.[name]?.at(-1))
  if (!topology.value?.poll_enabled && !latest.some(Boolean)) return { type: 'info', label: '等待采集' }
  if (latest.some(item => !item)) return { type: 'info', label: '等待采样' }
  if (latest.some(item => Date.now() - Date.parse(item.collected_at) > 60000)) return { type: 'warning', label: '采样已过期' }
  const successful = latest.filter(item => item.status === 'ok').length
  if (!successful) return { type: 'danger', label: '观测失败' }
  if (successful < names.length) return { type: 'warning', label: '部分观测失败' }
  return { type: 'success', label: topology.value.poll_enabled ? '实时采样' : '手动采样' }
})
const runtime = computed(() => topology.value?.runtime)
const flinkCluster = computed(() => runtime.value?.flink?.cluster?.version ? `Flink ${runtime.value.flink.cluster.version}` : '未知')
const flinkSlots = computed(() => {
  const cluster = runtime.value?.flink?.cluster
  return cluster ? `${cluster.taskmanagers ?? '?'} TaskManager · ${cluster.slots_available ?? '?'}/${cluster.slots_total ?? '?'} Slot 可用` : '暂无集群信息'
})
const lagSummary = computed(() => runtime.value?.kafka?.total_lag == null ? '未知' : `${runtime.value.kafka.total_lag} 条`)
const statusName = status => ({ queued: '排队中', running: '诊断中', open: '待处理', failed: '失败', resolved: '已恢复' })[status] || status
const toolName = name => ({ topology: '链路拓扑', flink_status: 'Flink 状态', flink_metrics: 'Flink 指标', kafka_offsets: 'Kafka 位点', model_health: '模型服务', logs: '组件日志', runbook: '排障文档' })[name] || name
const causeName = name => ({ model_slow: '模型响应变慢', model_unhealthy: '模型服务异常', flink_stopped: 'Flink 作业停止', lag_increasing: '消费积压增加', source_stopped: '上游断流', invalid_data: '输入格式异常', healthy: '链路正常', insufficient_evidence: '证据不足' })[name] || name
const eventName = name => ({ queued: '已创建', running: '诊断开始', decision: '选择工具', tool: '收集证据', model_usage: '模型用量', open: '报告生成', failed: '执行失败', feedback: '人工反馈', review: '案例审核' })[name] || name
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN') : ''
const severityType = value => ({ critical: 'danger', warning: 'warning', unknown: 'info', healthy: 'success' })[value] || 'info'
const severityName = value => ({ critical: '严重', warning: '提醒', unknown: '待接入', healthy: '正常' })[value] || value
const nodeState = id => runtime.value?.nodes?.[id]?.state || 'unknown'
const nodeStateLabel = id => runtime.value?.nodes?.[id]?.label || '暂无观测'
const topicSummary = name => {
  const topic = runtime.value?.kafka?.topics?.[name]
  if (!topic) return '未知'
  return topic.exists ? `${topic.partition_count} 分区 · ${topic.latest_total} 条` : '不存在'
}

function selectNode(node) {
  selectedNode.value = node.id
  question.value = `诊断${node.label}及其上下游为何异常`
}

async function collectNow() {
  if (collecting.value) return
  collecting.value = true; error.value = ''
  try {
    await collectObservations()
    await refresh()
    ElMessage.success('Kafka 与 Flink 实时状态已更新')
  } catch (e) { error.value = e.message || '实时采集失败' }
  finally { collecting.value = false }
}

async function diagnoseIssue(issue) {
  selectedNode.value = issue.node_id
  question.value = `诊断“${issue.title}”：${issue.detail}`
  await diagnose()
}

async function refresh() {
  loading.value = true
  try {
    const [chain, list, scenarios] = await Promise.all([getTopology(), listIncidents(), listReplays()])
    topology.value = chain.data
    incidents.value = list.data
    replays.value = scenarios.data
    if (current.value) current.value = (await getIncident(current.value.id)).data
    error.value = ''
  } catch (e) { error.value = e.message || '无法连接诊断服务' }
  finally { loading.value = false }
}

async function open(id) {
  aborter?.abort()
  try {
    current.value = (await getIncident(id)).data
    timeline.value = []
    activeTab.value = 'report'
    if (current.value.status === 'running' || current.value.status === 'queued') follow(id)
    else timeline.value = await readEvents(id)
  } catch (e) { error.value = e.message }
}

async function readEvents(id) {
  const response = await fetch(`/api/agent/incidents/${id}/events`, { headers: { Authorization: `Bearer ${store.getters.access_token}` } })
  if (!response.ok) return []
  const body = await response.text()
  return body.split('\n\n').map(block => block.split('\n').find(line => line.startsWith('data: '))).filter(Boolean).map(line => JSON.parse(line.slice(6)))
}

async function follow(id) {
  aborter = new AbortController()
  let lastId = 0
  try {
    while (!aborter.signal.aborted) {
      const response = await fetch(`/api/agent/incidents/${id}/events`, { signal: aborter.signal,
        headers: { Authorization: `Bearer ${store.getters.access_token}`, 'Last-Event-ID': String(lastId) } })
      if (!response.ok) throw new Error('无法获取诊断进度')
      const reader = response.body.getReader(), decoder = new TextDecoder()
      let buffer = ''
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const blocks = buffer.split('\n\n'); buffer = blocks.pop()
        for (const block of blocks) {
          const line = block.split('\n').find(item => item.startsWith('data: '))
          if (!line) continue
          const event = JSON.parse(line.slice(6))
          if (event.seq <= lastId) continue
          lastId = event.seq
          if (current.value?.id === id) {
            timeline.value.push(event)
            if (event.kind === 'tool' || event.kind === 'open' || event.kind === 'failed') current.value = (await getIncident(id)).data
          }
        }
      }
      if (current.value?.id !== id || ['open', 'failed', 'resolved'].includes(current.value.status)) break
    }
  } catch (e) { if (!aborter.signal.aborted) error.value = e.message }
  finally { await refresh() }
}

async function diagnose(replay) {
  if (starting.value) return
  starting.value = true; error.value = ''
  try {
    const scenario = replays.value.find(item => item.id === replay)
    const response = await createIncident({ question: scenario ? `诊断${scenario.name}` : question.value, replay: scenario?.id || null })
    await refresh(); await open(response.data.id)
  } catch (e) { error.value = e.message }
  finally { starting.value = false }
}

async function recheck() {
  try {
    const result = await recheckIncident(current.value.id,
      current.value.mode.startsWith('replay:') ? { replay_after: 'normal' } : {})
    await refresh(); await open(result.data.id)
  }
  catch (e) { error.value = e.message }
}

function feedback(useful) {
  feedbackForm.value = { useful, actual_cause: '', notes: '', proposed_for_knowledge: false }
  feedbackVisible.value = true
}

async function saveFeedback() {
  try {
    await sendFeedback(current.value.id, feedbackForm.value)
    feedbackVisible.value = false
    ElMessage.success('反馈已记录')
  } catch (e) { error.value = e.message }
}

async function showEvidence(id) {
  activeTab.value = 'evidence'; await nextTick()
  document.getElementById(`evidence-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

onMounted(async () => { await refresh(); timer = setInterval(() => { if (!current.value || current.value.status === 'open') refresh() }, 15000) })
onBeforeUnmount(() => { aborter?.abort(); clearInterval(timer) })
</script>

<style scoped>
.doctor { color:#26343b; background:#f5f7f7; min-height:100%; padding:20px 24px; font-size:14px; }
.doctor-header,.section-title,.header-actions,.result-head,.incident-line { display:flex; justify-content:space-between; align-items:center; gap:12px; }
.doctor-header { padding:0 0 16px; border-bottom:1px solid #dce3e3; }
.doctor h1 { font-size:25px; margin:3px 0 0; line-height:1.25; }
.doctor h2 { font-size:16px; margin:0; line-height:1.4; }
.doctor h3 { font-size:14px; margin:20px 0 10px; }
.eyebrow,.section-title>span { font-size:11px; color:#6f8589; letter-spacing:0; }
.doctor-band { padding:20px 0 22px; border-bottom:1px solid #dce3e3; }
.topology { display:flex; align-items:stretch; gap:5px; margin:17px 0; overflow-x:auto; padding-bottom:8px; }
.node { background:#fff; border:1px solid #dce3e3; border-radius:5px; padding:11px 12px; min-width:132px; width:15%; min-height:78px; text-align:left; cursor:pointer; color:inherit; display:flex; flex-direction:column; gap:4px; }
.node:hover,.node.selected { border-color:#178282; background:#f1fafa; }
.node-index { font-size:11px; color:#14817f; }
.node strong { font-size:13px; line-height:1.25; }
.node small { color:#778b91; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:100%; }
.node-index { display:flex; align-items:center; justify-content:space-between; width:100%; }
.state-dot { width:8px; height:8px; border-radius:50%; background:#9aa8ab; }
.node.healthy .state-dot { background:#2b956f; }.node.warning .state-dot { background:#d6952f; }.node.critical .state-dot { background:#c75240; }
.node.critical { border-color:#e2afa7; }.node.warning { border-color:#e5c78f; }
.node-state { margin-top:auto; font-size:11px; color:#687b80; }
.connector { align-self:center; font-size:19px; color:#82999b; flex-shrink:0; }
.runtime-grid { display:grid; grid-template-columns:repeat(6,minmax(0,1fr)); border:1px solid #dce3e3; background:#fff; margin:4px 0 16px; }
.runtime-item { min-width:0; padding:12px; border-right:1px solid #e3e8e8; display:flex; flex-direction:column; gap:5px; }
.runtime-item:last-child { border-right:0; }.runtime-item span,.runtime-item small { color:#778b91; font-size:11px; }
.runtime-item strong { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; font-size:14px; }
.issue-panel { margin:0 0 16px; padding:14px 0; border-top:1px solid #dce3e3; border-bottom:1px solid #dce3e3; }
.issue-row { display:grid; grid-template-columns:65px minmax(0,1fr) auto; align-items:center; gap:10px; padding:10px 0; border-top:1px solid #e5eaea; }
.issue-row:first-of-type { margin-top:9px; }.issue-row div { min-width:0; display:flex; flex-direction:column; gap:4px; }
.issue-row small { color:#778b91; overflow-wrap:anywhere; }.healthy-line { color:#28795f; margin:10px 0 0; }
.diagnose-form { display:flex; gap:10px; }
.diagnose-form .el-input { max-width:580px; }
.replay-select { width:245px; flex-shrink:0; }
.workspace { display:grid; grid-template-columns:minmax(230px,280px) minmax(0,1fr); min-height:480px; }
.incidents { border-right:1px solid #dce3e3; padding:22px 15px 20px 0; }
.incident { display:block; width:100%; text-align:left; border:0; border-bottom:1px solid #e5eaea; background:transparent; padding:15px 8px; cursor:pointer; color:#26343b; }
.incident:hover,.incident.active { background:#e9f3f2; }
.incident-line strong { max-width:175px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; font-size:13px; }
.incident small { display:block; color:#819096; margin-top:7px; }
.result { min-width:0; padding:22px 0 24px 24px; }
.result-head { margin-bottom:18px; }
.impact { border-left:3px solid #d99c34; padding:7px 12px; background:#fff8e9; }
.comparison { display:flex; flex-wrap:wrap; align-items:center; gap:8px 16px; border-left:3px solid #16817d; padding:7px 12px; background:#ecf7f5; }
.fact { margin:6px 0; }
.candidate { display:flex; gap:13px; padding:13px 0; border-bottom:1px solid #dce3e3; }
.candidate p { margin:7px 0; color:#64757b; }
.candidate small,.muted { color:#788b90; }
.rank { font-size:17px; color:#16817d; }
.evidence-link { border:0; color:#057b7d; background:none; cursor:pointer; margin-right:9px; }
.missing { padding:10px; background:#fff8ea; border-left:2px solid #d49a3b; }
.missing p { margin:5px 0; }
.feedback { display:flex; align-items:center; gap:9px; padding-top:18px; border-top:1px solid #dce3e3; }
.timeline-row { display:flex; gap:16px; padding:11px 0; border-bottom:1px solid #dce3e3; }
.timeline-row time { color:#7c9295; min-width:155px; }
.evidence-row { padding:14px 0; border-bottom:1px solid #dce3e3; }
.evidence-row>div { display:flex; gap:12px; align-items:center; }
.evidence-row small { color:#7c9295; }
pre { font-size:12px; line-height:1.5; max-height:260px; overflow:auto; background:#eaf0f0; border-radius:4px; padding:12px; }
.error { color:#b84b39; }
@media (max-width:1100px) { .runtime-grid { grid-template-columns:repeat(3,minmax(0,1fr)); }.runtime-item:nth-child(3) { border-right:0; }.runtime-item:nth-child(-n+3) { border-bottom:1px solid #e3e8e8; } }
@media (max-width:800px) { .doctor { padding:15px; }.doctor-header { align-items:flex-start; }.header-actions { flex-wrap:wrap; justify-content:flex-end; }.workspace { display:block; }.incidents { border-right:0; border-bottom:1px solid #dce3e3; padding-right:0; }.result { padding-left:0; }.diagnose-form { flex-wrap:wrap; }.diagnose-form .el-input { max-width:none; flex-basis:100%; }.replay-select { width:min(100%,245px); }.node { min-width:125px; }.runtime-grid { grid-template-columns:repeat(2,minmax(0,1fr)); }.runtime-item:nth-child(3) { border-right:1px solid #e3e8e8; }.runtime-item:nth-child(even) { border-right:0; }.runtime-item:nth-child(-n+4) { border-bottom:1px solid #e3e8e8; }.issue-row { grid-template-columns:58px minmax(0,1fr); }.issue-row .el-button { grid-column:2; justify-self:start; }.timeline-row { flex-wrap:wrap; gap:5px 12px; } }
</style>
