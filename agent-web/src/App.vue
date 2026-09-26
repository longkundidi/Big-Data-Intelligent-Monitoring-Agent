<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  Activity, AlertTriangle, Archive, BookOpen, Bot, CheckCircle2, ChevronDown, CircleStop,
  Copy, Database, FileText, FolderPlus, FolderSearch, GitBranch, History, Layers, LayoutDashboard,
  MessageSquare, Pencil, Plus, PlugZap, RefreshCw, RotateCcw, Save, Search, Send, Server,
  PanelRightOpen, ShieldCheck, Sparkles, SquareTerminal, Trash2, Upload, Waypoints, Wrench, X,
} from 'lucide-vue-next'
import { useWorkspaceStore } from './stores/workspace'
import MiniChart from './components/visual/MiniChart.vue'
import SpecEditor from './components/spec/SpecEditor.vue'

type Section = 'overview' | 'resources' | 'diagnosis' | 'docs' | 'templates'

const workspace = useWorkspaceStore()
const input = ref('')
const rightTab = ref('overview')
const replay = ref('')
const mobilePanel = ref('chat')
const rightPanelOpen = ref(true)
const activeSection = ref<Section>('overview')
const selectedEvidence = ref<any>(null)
const showProjectDialog = ref(false)
const editingProject = ref(false)
const showResourceDialog = ref(false)
const showSpecDialog = ref(false)
const showTemplateDialog = ref(false)
const showRecoveryDialog = ref(false)
const showConversationDialog = ref(false)
const editingConversation = ref<any>(null)
const conversationTitle = ref('')
const selectedModelId = ref('')
const selectedReasoningEffort = ref('')
const selectedTemplate = ref<any>(null)
const specDraft = ref<any>(null)
const specChangeSummary = ref('更新链路规格')
const proposalText = ref('')
const advancedSpecJson = ref('')
const useAdvancedSpec = ref(false)
const documentInput = ref<HTMLInputElement | null>(null)
const runbookLoading = ref(false)
const runbookQuery = ref('')
const runbookComponent = ref('')
const runbookResults = ref<any[]>([])
const resourceTesting = ref<Record<string, any>>({})
const completedRecoveryActions = ref<number[]>([])
const projectForm = reactive({ name: '', description: '', templateId: 'blank' })
const resourceForm = reactive({ name: '', type: 'kafka', config: '' })
const templateForm = reactive({ id: '', name: '', description: '', category: '自定义', changeSummary: '创建模板', spec: null as any })
const copyData = <T,>(value: T): T => JSON.parse(JSON.stringify(value))

const panelTabs = [
  { id: 'overview', label: '概览', icon: LayoutDashboard },
  { id: 'topology', label: '拓扑', icon: GitBranch },
  { id: 'metrics', label: '指标', icon: Activity },
  { id: 'evidence', label: '证据', icon: ShieldCheck },
]
const sections: Array<{ id: Section; label: string; icon: any }> = [
  { id: 'overview', label: '项目概览', icon: LayoutDashboard },
  { id: 'resources', label: '资源与链路', icon: Waypoints },
  { id: 'diagnosis', label: '诊断与记忆', icon: ShieldCheck },
  { id: 'docs', label: '排障文档', icon: FileText },
]

const assistantMessages = computed(() => workspace.conversation?.messages?.filter((item: any) => item.role === 'assistant') || [])
const toolEvents = computed(() => workspace.events.filter((item: any) => item.type.startsWith('tool_') || item.type.startsWith('model_')))
const latestModelEvent = computed(() => [...workspace.events].reverse().find((item: any) => ['model_finished', 'model_skipped', 'model_failed'].includes(item.type)))
const latestReport = computed(() => assistantMessages.value.at(-1)?.context?.report || null)
const recoveryActions = computed<string[]>(() => latestReport.value?.actions || [])
const canResumeRun = computed(() => ['failed', 'interrupted', 'cancelled'].includes(workspace.activeRun?.status))
const activeConversations = computed(() => workspace.conversations.filter((item: any) => item.status === 'active'))
const archivedConversations = computed(() => workspace.conversations.filter((item: any) => item.status === 'archived'))
const proposedMemories = computed(() => workspace.memories.filter((item: any) => item.status === 'proposed'))
const chartArtifact = computed(() => workspace.activeRun?.artifacts?.find((item: any) => item.kind === 'chart'))
const topologyNodes = computed(() => workspace.project?.topology?.nodes || [])
const resources = computed(() => workspace.project?.resources || [])
const activeResourceCount = computed(() => resources.value.filter((item: any) => item.status !== 'archived').length)
const currentSpec = computed(() => workspace.project?.spec?.spec || null)
const specVersions = computed(() => workspace.project?.spec_versions || [])
const currentSpecVersion = computed(() => workspace.project?.spec?.version || 0)
const requiredConfigurationItems = computed(() => workspace.configuration?.items?.filter((item: any) => item.required) || [])
const missingConfigurationItems = computed(() => requiredConfigurationItems.value.filter((item: any) => item.status !== 'complete'))
const selectedModel = computed(() => workspace.modelConfig.models?.find((item: any) => item.id === selectedModelId.value))
const reasoningEfforts = computed<string[]>(() => selectedModel.value?.reasoning_efforts || [])

function syncModelSelection(source?: any) {
  selectedModelId.value = source?.model_id || workspace.modelConfig.default_model || ''
  const profile = workspace.modelConfig.models?.find((item: any) => item.id === selectedModelId.value)
  selectedReasoningEffort.value = source?.reasoning_effort || profile?.default_effort || workspace.modelConfig.default_reasoning_effort || ''
}
function executionEventLabel(event: any) {
  if (event.type.startsWith('model_')) return event.payload.model_id || '模型生成'
  return event.payload.tool || '准备上下文'
}
function configurationSourceLabel(source: string) {
  return source === 'project_spec' ? '项目规格' : source === 'environment' ? '环境变量' : '未配置'
}

function syncRoute() {
  if (!window.history?.replaceState || !workspace.currentProjectId) return
  const params = new URLSearchParams()
  params.set('project', workspace.currentProjectId)
  params.set('view', activeSection.value)
  if (workspace.conversation?.id) params.set('conversation', workspace.conversation.id)
  window.history.replaceState({}, '', `${window.location.pathname}?${params.toString()}`)
}

async function selectProject(id: string) {
  await workspace.selectProject(id)
  activeSection.value = 'overview'
  rightTab.value = 'overview'
  selectedEvidence.value = null
  mobilePanel.value = 'chat'
  syncRoute()
}

async function openConversation(id: string) {
  await workspace.openConversation(id)
  activeSection.value = 'overview'
  mobilePanel.value = 'chat'
  syncRoute()
}

function openConversationDialog(item?: any) {
  editingConversation.value = item || null
  conversationTitle.value = item?.title || ''
  syncModelSelection(item || workspace.conversation)
  showConversationDialog.value = true
}

async function saveConversation() {
  const title = conversationTitle.value.trim()
  if (!title) return
  if (editingConversation.value) await workspace.updateConversation(editingConversation.value.id, {
    title, model_id: selectedModelId.value, reasoning_effort: selectedReasoningEffort.value,
  })
  else await workspace.newConversation(title, selectedModelId.value, selectedReasoningEffort.value)
  showConversationDialog.value = false
  activeSection.value = 'overview'
  mobilePanel.value = 'chat'
  syncRoute()
}

async function changeModel() {
  const profile = workspace.modelConfig.models?.find((item: any) => item.id === selectedModelId.value)
  selectedReasoningEffort.value = profile?.default_effort || 'medium'
  if (workspace.conversation) await workspace.updateConversation(workspace.conversation.id, {
    model_id: selectedModelId.value, reasoning_effort: selectedReasoningEffort.value,
  })
}

async function changeReasoningEffort() {
  if (workspace.conversation) await workspace.updateConversation(workspace.conversation.id, {
    model_id: selectedModelId.value, reasoning_effort: selectedReasoningEffort.value,
  })
}

async function archiveConversation(item: any) {
  await workspace.archiveConversation(item.id)
  syncRoute()
}

async function deleteConversation(item: any) {
  if (!window.confirm(`永久删除会话“${item.title}”及其消息、运行证据和报告？`)) return
  await workspace.deleteConversation(item.id)
  syncRoute()
}

async function submit() {
  const value = input.value.trim()
  if (!value || workspace.activeRun?.status === 'running') return
  input.value = ''
  mobilePanel.value = 'chat'
  if (value === '/init') {
    try {
      await workspace.initializeProject()
      activeSection.value = 'resources'
      openRightPanel()
      syncRoute()
    } catch (_) { /* The store exposes the server error in the sidebar. */ }
    return
  }
  await workspace.send(value, replay.value || undefined)
  replay.value = ''
}

function quick(value: string) { input.value = value; submit() }
function openRecovery() {
  completedRecoveryActions.value = []
  showRecoveryDialog.value = true
}
function toggleRecoveryAction(index: number) {
  completedRecoveryActions.value = completedRecoveryActions.value.includes(index)
    ? completedRecoveryActions.value.filter((item) => item !== index)
    : [...completedRecoveryActions.value, index]
}
async function requestActionPlan() {
  showRecoveryDialog.value = false
  await quick('根据最近一次诊断的证据，生成可执行的人工处置步骤，说明每一步风险、前置检查和回退方法，不要直接执行写操作。')
}
async function verifyRecovery() {
  showRecoveryDialog.value = false
  await quick('执行恢复复查：重新采集 Kafka、Flink、模型服务和输出链路的实时证据，与最近一次故障诊断对比，判断是否恢复以及是否仍有残留风险。')
}
function openRightPanel() { rightPanelOpen.value = true; mobilePanel.value = 'visual' }
function closeRightPanel() { rightPanelOpen.value = false; mobilePanel.value = 'chat' }
function evidence(item: any) { selectedEvidence.value = item; rightTab.value = 'evidence'; openRightPanel() }
function setSection(section: Section) {
  activeSection.value = section
  selectedEvidence.value = null
  if (section === 'docs' && !runbookResults.value.length) loadRunbooks()
  openRightPanel()
  syncRoute()
}

function openProjectDialog(edit = false, templateId = 'blank') {
  editingProject.value = edit
  projectForm.name = edit ? (workspace.project?.name || '') : ''
  projectForm.description = edit ? (workspace.project?.description || '') : ''
  projectForm.templateId = edit ? (workspace.project?.source_template_id || 'blank') : templateId
  showProjectDialog.value = true
}

async function saveProject() {
  if (!projectForm.name.trim()) return
  if (editingProject.value) {
    await workspace.updateProject({ name: projectForm.name.trim(), description: projectForm.description.trim() })
  } else {
    await workspace.createProject({
      name: projectForm.name.trim(),
      description: projectForm.description.trim(),
      topology_id: projectForm.templateId,
      template_id: projectForm.templateId,
    })
  }
  projectForm.name = ''
  projectForm.description = ''
  projectForm.templateId = 'blank'
  showProjectDialog.value = false
  activeSection.value = 'overview'
  syncRoute()
}

async function createResource() {
  if (!resourceForm.name.trim()) return
  let config: Record<string, unknown> = {}
  if (resourceForm.config.trim()) {
    try { config = JSON.parse(resourceForm.config) } catch { workspace.error = '资源配置必须是合法 JSON' ; return }
  }
  await workspace.createResource({ name: resourceForm.name.trim(), type: resourceForm.type, config })
  resourceForm.name = ''
  resourceForm.type = 'kafka'
  resourceForm.config = ''
  showResourceDialog.value = false
}

async function testResource(id: string) {
  resourceTesting.value[id] = { status: 'testing', detail: '正在执行只读连接检查…' }
  try { resourceTesting.value[id] = await workspace.testResource(id) }
  catch (error: any) { resourceTesting.value[id] = { status: 'unavailable', detail: error?.response?.data?.detail || '连接检查失败' } }
}

async function archiveResource(id: string) {
  await workspace.archiveResource(id)
}

async function archiveCurrentProject() {
  if (!workspace.currentProjectId || !window.confirm('归档当前项目？历史数据会保留。')) return
  await workspace.archiveProject(workspace.currentProjectId)
  activeSection.value = 'overview'
  syncRoute()
}

async function loadRunbooks() {
  if (!workspace.currentProjectId) return
  runbookLoading.value = true
  try { runbookResults.value = await workspace.runbooks(runbookQuery.value, runbookComponent.value || undefined) }
  finally { runbookLoading.value = false }
}

function openSpecEditor(spec = currentSpec.value) {
  if (!spec) return
  specDraft.value = copyData(spec)
  advancedSpecJson.value = JSON.stringify(specDraft.value, null, 2)
  specChangeSummary.value = `更新链路规格 v${currentSpecVersion.value + 1}`
  proposalText.value = ''
  useAdvancedSpec.value = false
  showSpecDialog.value = true
}

function applyAdvancedJson() {
  try {
    specDraft.value = JSON.parse(advancedSpecJson.value)
    workspace.error = ''
  } catch { workspace.error = '规格 JSON 格式不正确' }
}

async function publishSpecDraft() {
  if (!specDraft.value) return
  if (useAdvancedSpec.value) {
    try { specDraft.value = JSON.parse(advancedSpecJson.value) }
    catch { workspace.error = '规格 JSON 格式不正确'; return }
  }
  await workspace.publishSpec(specDraft.value, specChangeSummary.value || '更新链路规格')
  showSpecDialog.value = false
}

async function proposeFromText() {
  if (!proposalText.value.trim()) return
  const proposal = await workspace.proposeSpec(proposalText.value.trim())
  specDraft.value = proposal.spec
  advancedSpecJson.value = JSON.stringify(proposal.spec, null, 2)
  specChangeSummary.value = '根据对话描述更新规格'
}

async function uploadDocument(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const content = await file.text()
  const document = await workspace.uploadDocument(file.name, file.type || 'text/plain', content)
  specDraft.value = document.parse_result.spec
  advancedSpecJson.value = JSON.stringify(specDraft.value, null, 2)
  specChangeSummary.value = `根据文档 ${file.name} 更新规格`
  showSpecDialog.value = true
  ;(event.target as HTMLInputElement).value = ''
}

async function restoreSpec(versionId: string) {
  if (!window.confirm('使用该历史内容创建一个新的规格版本？')) return
  await workspace.restoreSpec(versionId)
}

async function openTemplateEditor(item?: any, fromProject = false) {
  const source = fromProject ? { spec: currentSpec.value, name: workspace.project?.name + '模板', description: workspace.project?.description || '', category: '自定义', builtin: 1 } : item ? await workspace.templateDetail(item.id) : await workspace.templateDetail('blank')
  selectedTemplate.value = item ? source : null
  templateForm.id = item?.id || ''
  templateForm.name = fromProject ? source.name : item?.builtin ? `${source.name} 副本` : item ? source.name : '新链路模板'
  templateForm.description = source.description || ''
  templateForm.category = source.category || '自定义'
  templateForm.changeSummary = item && !item.builtin ? `发布 ${source.name} 新版本` : fromProject ? '从项目规格创建模板' : '创建模板'
  templateForm.spec = copyData(source.spec)
  if (fromProject) {
    for (const host of templateForm.spec.hosts || []) host.address = ''
    for (const service of templateForm.spec.services || []) service.credential_ref = null
  }
  showTemplateDialog.value = true
}

async function saveTemplate() {
  if (!templateForm.name.trim() || !templateForm.spec) return
  const existing = selectedTemplate.value
  if (existing && !existing.builtin) {
    await workspace.publishTemplate(existing.id, { name: templateForm.name, description: templateForm.description, category: templateForm.category, spec: templateForm.spec, change_summary: templateForm.changeSummary })
  } else {
    await workspace.createTemplate({ name: templateForm.name, description: templateForm.description, category: templateForm.category, spec: templateForm.spec, change_summary: templateForm.changeSummary })
  }
  await workspace.loadTemplates()
  showTemplateDialog.value = false
}

async function restoreTemplateVersion(version: number) {
  if (!selectedTemplate.value || selectedTemplate.value.builtin || !window.confirm(`把 v${version} 的内容恢复为一个新版本？`)) return
  const restored = await workspace.restoreTemplate(selectedTemplate.value.id, version)
  selectedTemplate.value = await workspace.templateDetail(restored.id)
  templateForm.spec = copyData(selectedTemplate.value.spec)
  templateForm.changeSummary = `继续编辑 ${selectedTemplate.value.name}`
}

function resourceTypeLabel(type: string) {
  return ({ kafka: 'Kafka', flink: 'Flink', model: '模型服务', mysql: 'MySQL', log: '日志', business: '业务接口', custom: '自定义' } as Record<string, string>)[type] || type
}
function resourceIcon(type: string) { return type === 'kafka' || type === 'mysql' ? Database : type === 'flink' ? Activity : Server }
function resultLabel(status: string) { return ({ configured: '可用', ok: '可用', unknown: '未知', unavailable: '不可用', testing: '检查中' } as Record<string, string>)[status] || status }
function resultClass(status: string) { return status === 'configured' || status === 'ok' ? 'good' : status === 'unavailable' ? 'bad' : 'unknown' }
function templateServiceNames(item: any) {
  return (item.spec?.services || []).map((service: any) => resourceTypeLabel(service.type)).filter((value: string, index: number, all: string[]) => all.indexOf(value) === index)
}
function templateFlow(item: any) {
  const nodes = item.spec?.nodes || []
  if (!nodes.length) return ['等待配置链路节点']
  return nodes.slice(0, 4).map((node: any) => node.name)
}

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  await Promise.all([workspace.loadProjects(params.get('project') || undefined), workspace.loadTemplates(), workspace.loadModels()])
  if (params.get('project') && workspace.currentProjectId !== params.get('project')) await workspace.loadProjects()
  if (params.get('conversation') && workspace.conversations.some((item: any) => item.id === params.get('conversation'))) await openConversation(params.get('conversation') as string)
  if (['overview', 'resources', 'diagnosis', 'docs', 'templates'].includes(params.get('view') || '')) activeSection.value = params.get('view') as Section
  if (activeSection.value === 'docs') await loadRunbooks()
  syncModelSelection(workspace.conversation)
  syncRoute()
})

watch(() => [workspace.currentProjectId, workspace.conversation?.id, activeSection.value], syncRoute)
watch(() => workspace.conversation?.id, () => syncModelSelection(workspace.conversation))
</script>

<template>
  <div class="shell" :class="{ 'right-closed': !rightPanelOpen }">
    <aside class="sidebar" :class="{ 'mobile-hidden': mobilePanel !== 'projects' }">
      <div class="brand"><div class="brand-mark"><Sparkles :size="16" /></div><div><strong>StreamDoctor</strong><span>Agent workspace</span></div><button class="icon-button" title="工作区设置"><ChevronDown :size="15" /></button></div>
      <button class="new-project" @click="openProjectDialog()"><FolderPlus :size="16" /> 新建项目</button>
      <button v-if="workspace.project" class="project-init" :disabled="workspace.activeRun?.status === 'running'" @click="quick('/init')"><FolderSearch :size="15" />扫描项目配置<code>/init</code></button>
      <button class="template-library-button" :class="{ active: activeSection === 'templates' }" @click="setSection('templates')"><Layers :size="15" />模板库<span>{{ workspace.templates.length }}</span></button>
      <div class="sidebar-label row-label"><span>项目</span><span class="muted-count">{{ workspace.projects.length }}</span></div>
      <div class="project-list">
        <button v-for="item in workspace.projects" :key="item.id" class="project-item" :class="{ active: item.id === workspace.currentProjectId }" @click="selectProject(item.id)"><span class="project-dot" :class="item.status"></span><span>{{ item.name }}</span><span class="item-count">{{ item.status === 'active' ? '启用' : '已归档' }}</span></button>
      </div>
      <template v-if="workspace.project">
        <div class="sidebar-label row-label"><span>当前项目</span><span class="project-actions"><button class="icon-button" title="编辑当前项目" @click="openProjectDialog(true)"><Pencil :size="13" /></button><button class="icon-button" title="归档当前项目" @click="archiveCurrentProject"><Archive :size="13" /></button></span></div>
        <nav class="project-nav">
          <button v-for="item in sections" :key="item.id" class="nav-item" :class="{ active: activeSection === item.id }" @click="setSection(item.id)"><component :is="item.icon" :size="16" />{{ item.label }}<span v-if="item.id === 'resources'" class="nav-count">{{ activeResourceCount }}</span></button>
        </nav>
        <div class="sidebar-label row-label"><span>项目会话</span><span class="conversation-heading-actions"><em>{{ activeConversations.length }}</em><button class="mini-action" title="新建会话" @click="openConversationDialog()"><Plus :size="13" /></button></span></div>
        <div class="conversation-list"><div v-for="item in activeConversations" :key="item.id" class="conversation-row" :class="{ active: item.id === workspace.conversation?.id }"><button class="conversation-main" @click="openConversation(item.id)"><MessageSquare :size="14" /><span><strong>{{ item.title }}</strong><small>{{ item.message_count || 0 }} 条消息</small></span></button><div class="conversation-actions"><button title="重命名" @click="openConversationDialog(item)"><Pencil :size="11" /></button><button title="归档" @click="archiveConversation(item)"><Archive :size="11" /></button><button title="删除" class="danger" @click="deleteConversation(item)"><Trash2 :size="11" /></button></div></div><div v-if="!activeConversations.length" class="conversation-empty">当前项目还没有会话</div></div>
        <button class="new-conversation-link" @click="openConversationDialog()"><Plus :size="13" /> 新建会话</button>
        <details v-if="archivedConversations.length" class="archived-conversations"><summary>已归档 <span>{{ archivedConversations.length }}</span></summary><div v-for="item in archivedConversations" :key="item.id" class="conversation-row archived"><div class="conversation-main"><Archive :size="13" /><span><strong>{{ item.title }}</strong><small>{{ item.message_count || 0 }} 条消息</small></span></div><div class="conversation-actions visible"><button title="恢复" @click="workspace.restoreConversation(item.id)"><RotateCcw :size="11" /></button><button title="删除" class="danger" @click="deleteConversation(item)"><Trash2 :size="11" /></button></div></div></details>
      </template>
      <div class="sidebar-footer"><div class="status-pulse"></div><span>{{ workspace.error || 'Agent 服务在线' }}</span><span class="footer-version">v0.2</span></div>
    </aside>

    <main class="chat-panel" :class="{ 'mobile-hidden': mobilePanel !== 'chat' }">
      <header class="topbar"><div class="mobile-tabs"><button :class="{ active: mobilePanel === 'projects' }" @click="mobilePanel = 'projects'">项目</button><button :class="{ active: mobilePanel === 'chat' }" @click="mobilePanel = 'chat'">对话</button><button :class="{ active: mobilePanel === 'visual' }" @click="openRightPanel">详情</button></div><div class="context-pill"><span class="online-dot"></span>{{ workspace.project?.name || '选择一个项目' }}<ChevronDown :size="14" /></div><div class="top-actions"><button v-if="!rightPanelOpen" class="icon-button reopen-panel" title="打开项目详情" @click="openRightPanel"><PanelRightOpen :size="16" /></button><button class="icon-button" title="刷新项目" @click="workspace.loadProjects(workspace.currentProjectId)"><RefreshCw :size="16" /></button><button class="avatar">LK</button></div></header>
      <section class="conversation-head"><div><div class="eyebrow"><Bot :size="13" />运行诊断 Agent</div><h1>{{ workspace.conversation?.title || (activeSection === 'overview' ? '项目运行概览' : activeSection === 'templates' ? '链路模板库' : sections.find((item) => item.id === activeSection)?.label) }}</h1><p>{{ workspace.project?.description || '新建或选择一个项目开始' }}</p></div><div class="head-status"><span class="status-chip" :class="workspace.activeRun?.status || 'idle'">{{ workspace.activeRun?.status === 'running' ? '执行中' : workspace.activeRun?.status === 'completed' ? '已完成' : '就绪' }}</span><button v-if="workspace.activeRun?.status === 'running'" class="stop-button" @click="workspace.cancel"><CircleStop :size="14" />停止</button></div></section>
      <section class="messages" ref="messageList">
        <div v-if="!workspace.conversation" class="welcome"><div class="welcome-orb"><Sparkles :size="22" /></div><h2>{{ activeSection === 'overview' ? '今天要检查哪条链路？' : activeSection === 'templates' ? '选择或创建链路模板' : sections.find((item) => item.id === activeSection)?.label }}</h2><p>当前项目：{{ workspace.project?.name || '尚未选择项目' }}。Agent 会读取受控观测工具并把证据放到右侧。</p><div class="quick-grid workflow-grid"><button @click="quick('执行一次全链路健康巡检，检查 Kafka、Flink、模型服务和结果输出，报告异常与观测缺口。')"><Activity :size="17" /><span><em>01 · 发现</em>运行巡检<small>建立当前健康基线</small></span></button><button @click="quick('诊断当前数据链路异常：从现象开始检查上下游，定位候选根因并给出证据。')"><ShieldCheck :size="17" /><span><em>02 · 定位</em>故障诊断<small>按证据定位根因</small></span></button><button class="recovery-entry" @click="openRecovery"><Wrench :size="17" /><span><em>03 · 闭环</em>处置与恢复<small>执行处置后重新验证链路</small></span><RotateCcw :size="15" /></button></div><div class="workspace-links"><button @click="setSection('resources')"><Waypoints :size="14" />资源与链路</button><button @click="setSection('docs')"><FileText :size="14" />排障文档</button></div></div>
        <template v-else>
          <div v-for="message in workspace.conversation.messages" :key="message.id" class="message" :class="message.role"><div class="message-avatar">{{ message.role === 'user' ? '你' : 'SD' }}</div><div class="message-body"><div class="message-meta">{{ message.role === 'user' ? '你' : 'StreamDoctor' }}<span>{{ new Date(message.created_at).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }}</span></div><div class="message-content">{{ message.content }}</div><div v-if="message.role === 'assistant' && message.context?.report" class="inline-report"><span class="report-dot" :class="message.context.report.classification"></span><span>{{ message.context.report.summary }}</span><button @click="rightTab = 'evidence'; activeSection = 'overview'; openRightPanel()">查看证据 <ChevronDown :size="13" /></button></div></div></div>
          <div v-if="latestModelEvent" class="model-run-result" :class="latestModelEvent.type"><Bot :size="14" /><div><strong>{{ latestModelEvent.payload.model_id }} · {{ latestModelEvent.payload.reasoning_effort }}</strong><span v-if="latestModelEvent.type === 'model_finished'">模型生成完成</span><span v-else>{{ latestModelEvent.payload.reason }}，已使用规则诊断结果</span></div></div>
          <div v-if="workspace.activeRun?.status === 'running'" class="thinking-row"><div class="message-avatar bot-avatar"><Sparkles :size="14" /></div><div><div class="thinking-label">Agent 正在检查</div><div class="tool-stream"><span v-for="event in toolEvents.slice(-3)" :key="event.seq" class="tool-mini" :class="event.type"><span class="mini-dot"></span>{{ executionEventLabel(event) }}</span><span class="typing"><i></i><i></i><i></i></span></div></div></div>
        </template>
      </section>
      <section class="composer-wrap"><div class="quick-actions"><button @click="quick('执行一次全链路健康巡检')"><Activity :size="14" />运行巡检</button><button @click="quick('根据我接下来描述的现象开始故障诊断')"><ShieldCheck :size="14" />故障诊断</button><button @click="openRecovery"><Wrench :size="14" />处置与恢复</button><label class="replay-toggle"><input v-model="replay" value="model_slow" type="checkbox" />回放模型变慢</label></div><div class="composer"><textarea v-model="input" rows="1" placeholder="描述异常现象、时间范围或想检查的组件…" @keydown.enter.exact.prevent="submit"></textarea><button class="send-button" :disabled="!input.trim() || workspace.activeRun?.status === 'running'" @click="submit"><Send :size="17" /></button></div><div class="model-bar"><div class="model-select"><Bot :size="12" /><select v-model="selectedModelId" :disabled="workspace.activeRun?.status === 'running'" title="选择会话模型" @change="changeModel"><option v-for="model in workspace.modelConfig.models" :key="model.id" :value="model.id">{{ model.name }}</option></select></div><div class="model-select"><Sparkles :size="12" /><select v-model="selectedReasoningEffort" :disabled="workspace.activeRun?.status === 'running'" title="选择推理强度" @change="changeReasoningEffort"><option v-for="effort in reasoningEfforts" :key="effort" :value="effort">{{ effort }}</option></select></div><span class="model-state" :class="{ configured: workspace.modelConfig.configured }">{{ workspace.modelConfig.configured ? 'API 已配置' : '规则降级模式' }}</span></div><div class="composer-foot"><span><ShieldCheck :size="12" />只读工具 · 证据可追溯</span><span>Enter 发送 · Shift + Enter 换行</span></div></section>
    </main>

    <aside v-if="rightPanelOpen" class="visual-panel" :class="{ 'mobile-hidden': mobilePanel !== 'visual' }">
      <header class="visual-head"><div><div class="eyebrow">{{ activeSection === 'templates' ? 'TEMPLATE LIBRARY' : 'PROJECT SIGNALS' }}</div><h2>{{ activeSection === 'templates' ? '链路模板库' : (workspace.project?.name || '项目工作区') }}</h2></div><button class="icon-button" title="关闭项目详情" @click="closeRightPanel"><X :size="17" /></button></header>
      <div v-if="activeSection === 'overview'" class="tabs"><button v-for="tab in panelTabs" :key="tab.id" :class="{ active: rightTab === tab.id }" @click="rightTab = tab.id"><component :is="tab.icon" :size="14" />{{ tab.label }}</button></div>
      <div class="visual-content">
        <template v-if="activeSection === 'overview' && rightTab === 'overview'">
          <div class="signal-grid"><div class="signal-card"><span>链路状态</span><strong :class="latestReport?.classification === 'healthy' ? 'good' : 'warn'">{{ latestReport?.classification === 'healthy' ? '正常' : latestReport ? '需关注' : '待采样' }}</strong><small>来自最近一次运行</small></div><div class="signal-card"><span>执行步骤</span><strong>{{ workspace.events.length || 0 }}</strong><small>本次会话事件</small></div><div class="signal-card"><span>已确认记忆</span><strong>{{ workspace.memories.length }}</strong><small>项目级经验</small></div><div class="signal-card"><span>资源</span><strong>{{ activeResourceCount }}</strong><small>受控连接</small></div></div>
          <div class="section-head"><span>当前执行</span><span class="muted-count">{{ workspace.activeRun?.status || '未开始' }}</span></div><div class="run-card"><div class="run-icon"><SquareTerminal :size="16" /></div><div><strong>{{ workspace.activeRun ? 'Agent 任务记录' : '等待一次项目检查' }}</strong><p>{{ workspace.activeRun ? `${toolEvents.length} 个工具事件 · ${workspace.activeRun.status}` : '右侧会显示 Agent 生成的证据和 Artifact' }}</p></div><button v-if="canResumeRun" class="resume-run" @click="workspace.resume"><RotateCcw :size="12" />继续任务</button><span v-else class="run-state" :class="workspace.activeRun?.status || 'idle'"></span></div>
          <div class="section-head"><span>Agent 能力</span><span class="muted-count">只读</span></div><div class="capability-list"><div><Activity :size="15" /><span>运行指标与积压</span><em>Kafka · Flink</em></div><div><GitBranch :size="15" /><span>链路拓扑</span><em>可追溯</em></div><div><ShieldCheck :size="15" /><span>长期经验</span><em>确认后保存</em></div></div>
        </template>
        <template v-else-if="activeSection === 'overview' && rightTab === 'topology'">
          <div class="section-head"><span>数据链路</span><span class="status-text"><span class="online-dot"></span>{{ topologyNodes.length ? '已配置' : '待配置' }}</span></div><div v-if="topologyNodes.length" class="topology-card"><template v-for="(node, index) in topologyNodes" :key="node.id"><div class="flow-node" :class="node.status || (node.type === 'source' || node.type === 'kafka' ? 'healthy' : 'unknown')"><span>{{ String(index + 1).padStart(2, '0') }}</span><strong>{{ node.label }}</strong><small>{{ node.topic || node.job_name || node.registry_key || resourceTypeLabel(node.type) }}</small></div><div v-if="index < topologyNodes.length - 1" class="flow-line"></div></template></div><div v-else class="empty-note"><Waypoints :size="18" /><span>当前项目还没有资源，先到“资源与链路”添加 Kafka、Flink 或模型服务。</span></div>
        </template>
        <template v-else-if="activeSection === 'overview' && rightTab === 'metrics'">
          <div class="section-head"><span>最近运行指标</span><span class="muted-count">{{ chartArtifact ? chartArtifact.data.metric : '等待 Artifact' }}</span></div><div v-if="chartArtifact" class="chart-card"><div class="chart-card-head"><strong>{{ chartArtifact.data.metric === 'lag' ? 'Kafka 消费积压' : chartArtifact.data.metric }}</strong><span>来源：{{ chartArtifact.source_evidence_ids.length }} 条证据</span></div><MiniChart :points="chartArtifact.data.series" /></div><div v-else class="metric-placeholder"><Activity :size="22" /><strong>运行一次检查后显示趋势</strong><p>Agent 会把带时间范围的指标 Artifact 固定在这里。</p><button @click="quick('查看最近的积压和吞吐趋势')">请求趋势</button></div>
        </template>
        <template v-else-if="activeSection === 'overview' && rightTab === 'evidence'">
          <div class="section-head"><span>证据与执行</span><span class="muted-count">{{ workspace.activeRun?.evidence?.length || 0 }} 条</span></div><div v-if="selectedEvidence" class="evidence-detail"><button class="back-link" @click="selectedEvidence = null">← 返回证据列表</button><div class="evidence-title"><ShieldCheck :size="15" /><strong>{{ selectedEvidence.source }}</strong><span class="state-badge">{{ selectedEvidence.status }}</span></div><pre>{{ JSON.stringify(selectedEvidence.payload, null, 2) }}</pre></div><div v-else-if="workspace.activeRun?.evidence?.length" class="evidence-list"><button v-for="item in workspace.activeRun.evidence" :key="item.id" class="evidence-item" @click="evidence(item)"><div><span class="evidence-source">{{ item.source }}</span><span class="state-badge" :class="item.status">{{ item.status === 'ok' ? '已获取' : '不可用' }}</span></div><small>{{ new Date(item.collected_at).toLocaleString('zh-CN') }} · #{{ item.id.slice(0, 8) }}</small></button></div><div v-else class="empty-note"><ShieldCheck :size="18" /><span>完成一次诊断后，工具证据会出现在这里。</span></div>
        </template>

        <template v-else-if="activeSection === 'resources'">
          <div class="section-head first-section-head"><span>链路规格 v{{ currentSpecVersion }}</span><span class="spec-actions"><button class="small-primary" @click="openSpecEditor()"><Pencil :size="13" />编辑规格</button><button class="small-primary secondary" @click="documentInput?.click()"><Upload :size="13" />导入文档</button><button class="small-primary secondary" @click="openTemplateEditor(undefined, true)"><Copy :size="13" />另存模板</button></span></div>
          <input ref="documentInput" type="file" accept=".txt,.md,.json,.yaml,.yml" hidden @change="uploadDocument" />
          <div class="resource-hint">规格统一记录服务器、服务、链路节点和关系。发布后，新的诊断会固定使用该版本。</div>
          <section v-if="workspace.configuration" class="configuration-audit" :class="workspace.configuration.status"><header><div><span>配置完整度</span><strong>{{ workspace.configuration.score }}%</strong></div><button class="icon-button" title="重新检查配置" @click="workspace.loadConfiguration"><RefreshCw :size="14" /></button></header><div class="configuration-progress"><i :style="{ width: `${workspace.configuration.score}%` }"></i></div><p>{{ workspace.configuration.complete_required }}/{{ workspace.configuration.total_required }} 项必填配置完整<span v-if="missingConfigurationItems.length">，仍缺 {{ missingConfigurationItems.length }} 项</span></p></section>
          <div class="configuration-list"><article v-for="item in requiredConfigurationItems" :key="item.key" :class="item.status"><CheckCircle2 v-if="item.status === 'complete'" :size="14" /><AlertTriangle v-else :size="14" /><div><header><strong>{{ item.label }}</strong><span>{{ configurationSourceLabel(item.source) }}</span></header><code>{{ item.value ?? '未配置' }}</code><p v-if="item.status !== 'complete'">{{ item.fix }}</p></div></article></div>
          <details v-if="workspace.configuration?.files?.length" class="configuration-files"><summary>配置保存在哪里</summary><div v-for="file in workspace.configuration.files" :key="file.location"><FileText :size="13" /><span><strong>{{ file.name }}</strong><code>{{ file.location }}</code><small>{{ file.purpose }}；{{ file.edit }}</small></span></div></details>
          <div v-if="currentSpec" class="spec-overview">
            <div><strong>{{ currentSpec.hosts?.length || 0 }}</strong><span>服务器</span></div><div><strong>{{ currentSpec.services?.length || 0 }}</strong><span>服务</span></div><div><strong>{{ currentSpec.nodes?.length || 0 }}</strong><span>节点</span></div><div><strong>{{ currentSpec.edges?.length || 0 }}</strong><span>关系</span></div>
          </div>
          <div v-if="currentSpec?.hosts?.length" class="section-head"><span>部署位置</span><span class="muted-count">IP / 主机名可编辑</span></div>
          <div class="host-summary"><div v-for="host in currentSpec?.hosts || []" :key="host.id"><Server :size="14" /><span><strong>{{ host.name }}</strong><small>{{ host.address || '地址待填写' }} · {{ host.environment || '环境未知' }}</small></span></div></div>
          <div class="section-head"><span>项目资源</span><button class="small-primary" @click="showResourceDialog = true"><Plus :size="13" />兼容资源</button></div>
          <div v-if="resources.length" class="resource-list"><article v-for="item in resources" :key="item.id" class="resource-card" :class="{ archived: item.status === 'archived' }"><div class="resource-card-head"><div class="resource-icon"><component :is="resourceIcon(item.type)" :size="16" /></div><div><strong>{{ item.name }}</strong><span>{{ resourceTypeLabel(item.type) }}</span></div><span class="state-badge" :class="item.status === 'active' ? 'ok' : ''">{{ item.status === 'active' ? '启用' : '已归档' }}</span></div><p>{{ Object.keys(item.config || {}).length ? '已保存项目级配置' : '尚未填写连接参数' }}</p><div class="resource-actions"><button @click="testResource(item.id)"><PlugZap :size="13" />测试连接</button><button v-if="item.status === 'active'" class="quiet-danger" @click="archiveResource(item.id)"><Archive :size="13" />归档</button></div><div v-if="resourceTesting[item.id]" class="test-result" :class="resultClass(resourceTesting[item.id].status)"><CheckCircle2 v-if="resultClass(resourceTesting[item.id].status) === 'good'" :size="13" /><AlertTriangle v-else :size="13" />{{ resultLabel(resourceTesting[item.id].status) }}：{{ resourceTesting[item.id].detail }}</div></article></div><div v-else class="empty-note"><Database :size="18" /><span>还没有兼容资源。规格中的服务仍可由 Agent 读取。</span></div>
          <div class="section-head"><span>规格历史</span><span class="muted-count">{{ specVersions.length }} 个版本</span></div>
          <div class="version-list"><div v-for="version in specVersions" :key="version.id"><History :size="14" /><span><strong>v{{ version.version }} · {{ version.change_summary }}</strong><small>{{ version.source_type }} · {{ version.changes?.length || 0 }} 项字段变化 · {{ new Date(version.created_at).toLocaleString('zh-CN') }}</small></span><button v-if="version.id !== workspace.project?.current_spec_version_id" @click="restoreSpec(version.id)">恢复</button><em v-else>当前</em></div></div>
        </template>

        <template v-else-if="activeSection === 'diagnosis'">
          <div class="section-head first-section-head"><span>诊断与记忆</span><span class="muted-count">{{ workspace.memories.length }} 条已确认</span></div><div class="diagnosis-summary"><div><strong>{{ workspace.conversations.length }}</strong><span>历史会话</span></div><div><strong>{{ workspace.activeRun ? '1' : '0' }}</strong><span>当前运行</span></div><div><strong>{{ proposedMemories.length }}</strong><span>待确认记忆</span></div></div><div class="section-head"><span>最近会话</span></div><div v-if="workspace.conversations.length" class="history-list"><button v-for="item in workspace.conversations" :key="item.id" @click="openConversation(item.id)"><MessageSquare :size="14" /><span>{{ item.title }}</span><small>{{ new Date(item.updated_at).toLocaleString('zh-CN') }}</small></button></div><div v-else class="empty-note"><ShieldCheck :size="18" /><span>完成第一次诊断后，报告和运行证据会保存在这里。</span></div><div v-if="proposedMemories.length" class="memory-review"><div class="section-head"><span>待确认记忆</span><span class="muted-count">需要确认</span></div><div v-for="item in proposedMemories" :key="item.id" class="memory-card"><Sparkles :size="15" /><p>{{ item.content }}</p><div><button @click="workspace.approve(item.id)">保存到项目</button><button @click="workspace.revoke(item.id)">忽略</button></div></div></div>
        </template>

        <template v-else-if="activeSection === 'docs'">
          <div class="section-head first-section-head"><span>排障文档</span><span class="muted-count">BM25 检索</span></div><div class="docs-search"><Search :size="14" /><input v-model="runbookQuery" placeholder="搜索积压、反压、超时…" @keydown.enter="loadRunbooks" /><select v-model="runbookComponent" @change="loadRunbooks"><option value="">全部组件</option><option value="kafka">Kafka</option><option value="flink">Flink</option><option value="model">模型服务</option><option value="sink">结果存储</option></select><button @click="loadRunbooks">搜索</button></div><div v-if="runbookLoading" class="empty-note"><Search :size="18" /><span>正在检索项目排障文档…</span></div><div v-else-if="runbookResults.length" class="runbook-list"><article v-for="item in runbookResults" :key="item.id" class="runbook-card"><div><span class="runbook-id">{{ item.id }}</span><strong>{{ item.title }}</strong></div><p>{{ item.body }}</p><small>{{ item.components.join(' · ') }} · {{ item.version }}</small><button @click="quick(`请结合文档 ${item.id} 检查当前项目`)">交给 Agent 检查 <ChevronDown :size="13" /></button></article></div><div v-else class="empty-note"><BookOpen :size="18" /><span>没有匹配文档，换一个关键词试试。</span></div>
        </template>

        <template v-else-if="activeSection === 'templates'">
          <div class="section-head first-section-head"><span>链路模板</span><button class="small-primary" @click="openTemplateEditor()"><Plus :size="13" />新增模板</button></div>
          <div class="resource-hint">模板提供初始规格。创建项目后会复制为独立版本，修改模板不会覆盖已有项目。</div>
          <div class="template-list"><article v-for="item in workspace.templates" :key="item.id" class="template-card"><div class="template-card-head"><div class="resource-icon"><Layers :size="15" /></div><div><strong>{{ item.name }}</strong><span>{{ item.category }} · v{{ item.current_version }}</span></div><span class="state-badge" :class="item.builtin ? '' : 'ok'">{{ item.builtin ? '内置' : '自定义' }}</span></div><p>{{ item.description }}</p><div class="resource-actions"><button @click="openTemplateEditor(item)"><Pencil :size="13" />{{ item.builtin ? '复制并编辑' : '编辑发布' }}</button><button @click="openProjectDialog(false, item.id)"><FolderPlus :size="13" />用它建项目</button></div></article></div>
        </template>
      </div>
    </aside>
  </div>

  <div v-if="showConversationDialog" class="modal-backdrop" @click.self="showConversationDialog = false"><form class="modal conversation-modal" @submit.prevent="saveConversation"><div class="modal-head"><div><span class="eyebrow">{{ editingConversation ? 'CONVERSATION SETTINGS' : 'NEW CONVERSATION' }}</span><h3>{{ editingConversation ? '会话设置' : '新建项目会话' }}</h3><p>模型配置随会话保存，每次运行会记录实际模型和推理强度。</p></div><button type="button" class="icon-button" @click="showConversationDialog = false"><X :size="17" /></button></div><label>会话标题<input v-model="conversationTitle" autofocus maxlength="200" placeholder="例如：排查模型服务延迟" required /></label><div class="conversation-model-grid"><label>模型<select v-model="selectedModelId" @change="selectedReasoningEffort = selectedModel?.default_effort || 'medium'"><option v-for="model in workspace.modelConfig.models" :key="model.id" :value="model.id">{{ model.name }} · {{ model.description }}</option></select></label><label>推理强度<select v-model="selectedReasoningEffort"><option v-for="effort in reasoningEfforts" :key="effort" :value="effort">{{ effort }}</option></select></label></div><div class="model-config-note" :class="{ configured: workspace.modelConfig.configured }"><Bot :size="14" /><span>{{ workspace.modelConfig.configured ? '模型 API 已在服务端配置，发送消息时会调用所选模型。' : '尚未配置服务端 API Key。可先使用规则诊断，配置后无需修改会话。' }}</span></div><div class="modal-actions"><button type="button" class="secondary-button" @click="showConversationDialog = false">取消</button><button type="submit" class="primary-button"><Pencil v-if="editingConversation" :size="14" /><Plus v-else :size="14" />{{ editingConversation ? '保存设置' : '创建会话' }}</button></div></form></div>
  <div v-if="showProjectDialog" class="modal-backdrop" @click.self="showProjectDialog = false"><form class="modal project-modal" :class="{ 'editing-only': editingProject }" @submit.prevent="saveProject"><div class="modal-head"><div><span class="eyebrow">{{ editingProject ? 'EDIT PROJECT' : 'NEW PROJECT' }}</span><h3>{{ editingProject ? '编辑项目' : '创建诊断项目' }}</h3><p>{{ editingProject ? '更新项目名称与用途说明。' : '填写基本信息，并选择最接近当前数据链路的初始模板。' }}</p></div><button type="button" class="icon-button" @click="showProjectDialog = false"><X :size="17" /></button></div><div class="project-create-layout"><section class="project-form-fields"><div class="form-section-title"><span>项目信息</span><small>创建后仍可继续修改</small></div><label>项目名称<input v-model="projectForm.name" autofocus placeholder="例如：生产链路巡检" required maxlength="120" /></label><label>项目说明<textarea v-model="projectForm.description" rows="5" placeholder="描述数据来源、处理目标和影响范围…" maxlength="2000"></textarea></label><div v-if="!editingProject" class="project-template-note"><GitBranch :size="15" /><div><strong>模板只负责初始化</strong><span>服务器地址、Topic、Job 和节点关系会复制到项目规格，之后与模板独立演进。</span></div></div></section><section v-if="!editingProject" class="template-picker"><div class="form-section-title"><span>选择链路模板</span><small>{{ workspace.templates.length }} 套可用</small></div><div class="template-option-grid"><label v-for="item in workspace.templates" :key="item.id" :class="{ selected: projectForm.templateId === item.id }"><input v-model="projectForm.templateId" type="radio" :value="item.id" /><div class="template-option-body"><header><div><strong>{{ item.name }}</strong><small>{{ item.category }} · v{{ item.current_version }}</small></div><span class="template-radio-mark"></span></header><p>{{ item.description }}</p><div v-if="templateServiceNames(item).length" class="template-services"><span v-for="service in templateServiceNames(item)" :key="service">{{ service }}</span></div><div class="template-flow"><template v-for="(node, index) in templateFlow(item)" :key="node"><span>{{ node }}</span><i v-if="index < templateFlow(item).length - 1">→</i></template></div><footer><span>{{ item.spec?.nodes?.length || 0 }} 节点</span><span>{{ item.spec?.edges?.length || 0 }} 关系</span><span>{{ item.spec?.diagnostics?.tools?.length || 0 }} 工具</span></footer></div></label></div></section></div><div class="modal-actions"><button type="button" class="secondary-button" @click="showProjectDialog = false">取消</button><button type="submit" class="primary-button"><Pencil v-if="editingProject" :size="14" /><Plus v-else :size="14" />{{ editingProject ? '保存修改' : '创建项目' }}</button></div></form></div>
  <div v-if="showResourceDialog" class="modal-backdrop" @click.self="showResourceDialog = false"><form class="modal" @submit.prevent="createResource"><div class="modal-head"><div><span class="eyebrow">PROJECT RESOURCE</span><h3>添加资源</h3><p>连接信息保存在服务端，模型不能传入任意地址。</p></div><button type="button" class="icon-button" @click="showResourceDialog = false"><X :size="17" /></button></div><label>资源名称<input v-model="resourceForm.name" autofocus placeholder="例如：生产 Kafka 集群" required maxlength="120" /></label><label>资源类型<select v-model="resourceForm.type"><option value="kafka">Kafka</option><option value="flink">Flink</option><option value="model">模型服务</option><option value="mysql">MySQL</option><option value="log">日志</option><option value="business">业务接口</option><option value="custom">自定义</option></select></label><label>配置 JSON（可选）<textarea v-model="resourceForm.config" rows="5" placeholder='{"topic":"input-topic","consumer_group":"demo"}'></textarea></label><div class="modal-actions"><button type="button" class="secondary-button" @click="showResourceDialog = false">取消</button><button type="submit" class="primary-button"><Plus :size="14" />保存资源</button></div></form></div>
  <div v-if="showSpecDialog" class="modal-backdrop" @click.self="showSpecDialog = false"><form class="modal spec-modal" @submit.prevent="publishSpecDraft"><div class="modal-head"><div><span class="eyebrow">PROJECT SPECIFICATION</span><h3>编辑链路规格</h3><p>保存草稿不会影响当前诊断；发布后生成 v{{ currentSpecVersion + 1 }}。</p></div><button type="button" class="icon-button" @click="showSpecDialog = false"><X :size="17" /></button></div><div class="proposal-box"><textarea v-model="proposalText" rows="2" placeholder="也可以描述修改，例如：模型服务迁到 10.0.0.8，端口改为 9000"></textarea><button type="button" @click="proposeFromText"><Sparkles :size="13" />生成草稿</button></div><label class="advanced-toggle"><input v-model="useAdvancedSpec" type="checkbox" />高级 JSON 编辑</label><textarea v-if="useAdvancedSpec" v-model="advancedSpecJson" class="spec-json" rows="20" @blur="applyAdvancedJson"></textarea><SpecEditor v-else v-model="specDraft" /><label>本次修改说明<input v-model="specChangeSummary" required maxlength="500" /></label><div class="modal-actions"><button type="button" class="secondary-button" @click="showSpecDialog = false">取消</button><button type="submit" class="primary-button"><Save :size="14" />发布新版本</button></div></form></div>
  <div v-if="showRecoveryDialog" class="modal-backdrop" @click.self="showRecoveryDialog = false"><div class="modal recovery-modal"><div class="modal-head"><div><span class="eyebrow">RECOVERY WORKFLOW</span><h3>处置与恢复</h3><p>处置记录与恢复验证分开，复查会重新获取运行证据。</p></div><button type="button" class="icon-button" @click="showRecoveryDialog = false"><X :size="17" /></button></div><div class="recovery-steps"><div class="recovery-step done"><span>1</span><div><strong>故障诊断</strong><small>{{ latestReport ? latestReport.summary : '尚未形成诊断结论' }}</small></div></div><div class="recovery-line"></div><div class="recovery-step" :class="{ active: latestReport }"><span>2</span><div><strong>执行处置</strong><small>按建议人工处理并记录完成项</small></div></div><div class="recovery-line"></div><div class="recovery-step"><span>3</span><div><strong>恢复验证</strong><small>重新采样并对比故障前后</small></div></div></div><template v-if="latestReport"><div class="execution-mode"><Wrench :size="14" /><div><strong>人工确认模式</strong><small>当前项目未注册可写操作，Agent 只提供有证据的步骤，不会直接重启或修改服务。</small></div></div><div v-if="recoveryActions.length" class="recovery-checklist"><button v-for="(action, index) in recoveryActions" :key="index" type="button" :class="{ checked: completedRecoveryActions.includes(index) }" @click="toggleRecoveryAction(index)"><span>{{ completedRecoveryActions.includes(index) ? '✓' : index + 1 }}</span><p>{{ action }}</p></button></div><div v-else class="empty-note"><AlertTriangle :size="16" /><span>最近报告没有明确处置项，请先生成处置方案。</span></div><div class="modal-actions recovery-actions"><button type="button" class="secondary-button" @click="requestActionPlan"><FileText :size="14" />生成详细方案</button><button type="button" class="primary-button" :disabled="recoveryActions.length > 0 && !completedRecoveryActions.length" @click="verifyRecovery"><RotateCcw :size="14" />开始恢复验证</button></div></template><template v-else><div class="empty-recovery"><ShieldCheck :size="22" /><strong>先完成一次故障诊断</strong><p>诊断报告会提供候选根因、支持证据和处置建议。</p><button class="primary-button" @click="showRecoveryDialog = false; quick('诊断当前数据链路异常：检查上下游并定位根因。')">开始诊断</button></div></template></div></div>
  <div v-if="showTemplateDialog" class="modal-backdrop" @click.self="showTemplateDialog = false"><form class="modal spec-modal" @submit.prevent="saveTemplate"><div class="modal-head"><div><span class="eyebrow">TEMPLATE EDITOR</span><h3>{{ selectedTemplate && !selectedTemplate.builtin ? '编辑模板并发布新版本' : '创建自定义模板' }}</h3><p>内置模板会复制为自定义模板，已有项目不会被修改。</p></div><button type="button" class="icon-button" @click="showTemplateDialog = false"><X :size="17" /></button></div><div class="template-meta"><label>模板名称<input v-model="templateForm.name" required /></label><label>分类<input v-model="templateForm.category" /></label></div><label>模板说明<textarea v-model="templateForm.description" rows="2"></textarea></label><div v-if="selectedTemplate?.versions?.length" class="template-version-history"><strong>模板历史</strong><div><span v-for="version in selectedTemplate.versions" :key="version.id">v{{ version.version }} · {{ version.change_summary }}<button v-if="!selectedTemplate.builtin && version.version !== selectedTemplate.current_version" type="button" @click="restoreTemplateVersion(version.version)">恢复</button><em v-else-if="version.version === selectedTemplate.current_version">当前</em></span></div></div><SpecEditor v-model="templateForm.spec" /><label>版本说明<input v-model="templateForm.changeSummary" required maxlength="500" /></label><div class="modal-actions"><button type="button" class="secondary-button" @click="showTemplateDialog = false">取消</button><button type="submit" class="primary-button"><Save :size="14" />{{ selectedTemplate && !selectedTemplate.builtin ? '发布新版本' : '保存模板' }}</button></div></form></div>
</template>
