import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { agentApi } from '../api/agent'

export const useWorkspaceStore = defineStore('workspace', () => {
  const projects = ref<any[]>([])
  const templates = ref<any[]>([])
  const modelConfig = ref<any>({ configured: false, models: [], default_model: '', default_reasoning_effort: '' })
  const benchmark = ref<any>(null)
  const project = ref<any>(null)
  const configuration = ref<any>(null)
  const conversations = ref<any[]>([])
  const conversation = ref<any>(null)
  const memories = ref<any[]>([])
  const activeRun = ref<any>(null)
  const events = ref<any[]>([])
  const loading = ref(false)
  const error = ref('')
  let eventSource: EventSource | null = null

  const currentProjectId = computed(() => project.value?.id || '')

  function closeStream() {
    eventSource?.close()
    eventSource = null
  }

  function watchRun(runId: string) {
    closeStream()
    eventSource = new EventSource(`/api/agent/runs/${runId}/events`)
    const onEvent = (raw: MessageEvent) => {
      const value = JSON.parse(raw.data)
      events.value.push(value)
      if (value.type === 'message') {
        conversation.value?.messages.push({ id: crypto.randomUUID(), role: 'assistant', content: value.payload.content, created_at: new Date().toISOString(), context: value.payload })
      }
      if (value.type === 'run_finished' || value.type === 'run_failed' || value.type === 'run_cancelled') {
        closeStream()
        getRun(runId)
      }
    }
    for (const type of ['run_queued', 'run_started', 'context_ready', 'investigation_decision', 'hypothesis_updated', 'investigation_stopped', 'tool_started', 'tool_finished', 'agent_message', 'subagent_started', 'subagent_finished', 'reviewer_started', 'reviewer_finished', 'reviewer_recheck_finished', 'executor_plan', 'executor_started', 'executor_finished', 'executor_blocked', 'model_started', 'model_usage', 'model_finished', 'model_skipped', 'model_failed', 'artifact', 'memory_proposal', 'message', 'budget_exhausted', 'run_finished', 'run_failed', 'run_cancelled']) {
      eventSource.addEventListener(type, onEvent)
    }
    eventSource.onerror = () => { closeStream(); getRun(runId) }
  }

  async function loadProjects(preferredId?: string) {
    loading.value = true
    error.value = ''
    try {
      projects.value = (await agentApi.projects()).data
      const id = preferredId || project.value?.id || projects.value[0]?.id
      if (id && projects.value.some((item: any) => item.id === id)) await selectProject(id)
    } catch (e: any) {
      error.value = e?.response?.data?.detail || '无法连接 Agent 服务'
    } finally { loading.value = false }
  }

  async function loadTemplates() {
    templates.value = (await agentApi.templates()).data
    return templates.value
  }

  async function loadModels() {
    modelConfig.value = (await agentApi.models()).data
    return modelConfig.value
  }

  async function loadBenchmark() {
    benchmark.value = (await agentApi.benchmark()).data
    return benchmark.value
  }

  async function selectProject(id: string) {
    closeStream()
    const [projectResponse, conversationResponse, memoryResponse, configurationResponse] = await Promise.all([
      agentApi.project(id), agentApi.conversations(id, true), agentApi.memories(id), agentApi.configuration(id),
    ])
    project.value = projectResponse.data
    conversations.value = conversationResponse.data
    memories.value = memoryResponse.data
    configuration.value = configurationResponse.data
    conversation.value = null
    activeRun.value = null
    events.value = []
  }

  async function createProject(payload: { name: string; description?: string; topology_id?: string; template?: boolean; template_id?: string; template_version?: number }) {
    const response = await agentApi.createProject(payload)
    projects.value = [response.data, ...projects.value]
    await selectProject(response.data.id)
    return response.data
  }

  async function createTemplate(payload: Record<string, unknown>) {
    const response = await agentApi.createTemplate(payload)
    templates.value = [...templates.value, response.data]
    return response.data
  }

  async function templateDetail(id: string, version?: number) { return (await agentApi.template(id, version)).data }

  async function publishTemplate(id: string, payload: Record<string, unknown>) {
    const response = await agentApi.publishTemplate(id, payload)
    templates.value = templates.value.map((item: any) => item.id === id ? { ...item, ...response.data } : item)
    return response.data
  }

  async function restoreTemplate(id: string, version: number) {
    const response = await agentApi.restoreTemplate(id, version)
    await loadTemplates()
    return response.data
  }

  async function cloneTemplate(id: string, payload: Record<string, unknown>) {
    const response = await agentApi.cloneTemplate(id, payload)
    templates.value = [...templates.value, response.data]
    return response.data
  }

  async function publishSpec(spec: Record<string, unknown>, changeSummary: string, sourceType = 'manual', sourceRef?: string) {
    const current = project.value?.spec
    const response = await agentApi.publishSpec(currentProjectId.value, {
      spec, change_summary: changeSummary, source_type: sourceType, source_ref: sourceRef,
      base_version_id: current?.id,
    })
    project.value = (await agentApi.project(currentProjectId.value)).data
    await loadConfiguration()
    return response.data
  }

  async function restoreSpec(versionId: string) {
    const response = await agentApi.restoreSpec(currentProjectId.value, versionId)
    project.value = (await agentApi.project(currentProjectId.value)).data
    await loadConfiguration()
    return response.data
  }

  async function proposeSpec(content: string, sourceType = 'conversation') {
    return (await agentApi.proposeSpec(currentProjectId.value, { content, source_type: sourceType })).data
  }

  async function initializeProject() {
    if (!currentProjectId.value) return null
    if (!conversation.value) await newConversation('项目初始化')
    if (!conversation.value) return null
    error.value = ''
    try {
      const response = await agentApi.initializeProject(currentProjectId.value, {
        conversation_id: conversation.value.id,
        request_id: crypto.randomUUID(),
      })
      project.value = (await agentApi.project(currentProjectId.value)).data
      configuration.value = (await agentApi.configuration(currentProjectId.value)).data
      conversation.value = (await agentApi.conversation(conversation.value.id)).data
      conversations.value = (await agentApi.conversations(currentProjectId.value, true)).data
      return response.data
    } catch (e: any) {
      error.value = e?.response?.data?.detail || '项目初始化失败'
      throw e
    }
  }

  async function uploadDocument(filename: string, mediaType: string, content: string) {
    return (await agentApi.uploadDocument(currentProjectId.value, { filename, media_type: mediaType, content })).data
  }

  async function saveProjectAsTemplate(name: string, description = '') {
    const response = await agentApi.saveProjectAsTemplate(currentProjectId.value, { name, description })
    await loadTemplates()
    return response.data
  }

  async function updateProject(payload: { name?: string; description?: string; status?: string }) {
    if (!currentProjectId.value) return null
    const response = await agentApi.updateProject(currentProjectId.value, payload)
    project.value = { ...project.value, ...response.data }
    projects.value = projects.value.map((item: any) => item.id === currentProjectId.value ? { ...item, ...response.data } : item)
    return response.data
  }

  async function archiveProject(id: string) {
    await agentApi.archiveProject(id)
    projects.value = projects.value.map((item: any) => item.id === id ? { ...item, status: 'archived' } : item)
    if (currentProjectId.value === id) {
      const next = projects.value.find((item: any) => item.status === 'active')
      if (next) await selectProject(next.id)
    }
  }

  async function newConversation(title = '新对话', modelId?: string, reasoningEffort?: string) {
    if (!currentProjectId.value) return null
    const response = await agentApi.createConversation(currentProjectId.value, {
      title,
      model_id: modelId || modelConfig.value.default_model,
      reasoning_effort: reasoningEffort || modelConfig.value.default_reasoning_effort,
    })
    conversations.value.unshift(response.data)
    await openConversation(response.data.id)
    return response.data
  }

  async function updateConversation(id: string, payload: { title?: string; status?: 'active' | 'archived'; model_id?: string; reasoning_effort?: string }) {
    const response = await agentApi.updateConversation(id, payload)
    conversations.value = conversations.value.map((item: any) => item.id === id ? { ...item, ...response.data } : item)
    if (conversation.value?.id === id) conversation.value = { ...conversation.value, ...response.data }
    return response.data
  }

  async function archiveConversation(id: string) {
    await updateConversation(id, { status: 'archived' })
    if (conversation.value?.id === id) {
      closeStream()
      conversation.value = null
      activeRun.value = null
      events.value = []
    }
  }

  async function restoreConversation(id: string) {
    return updateConversation(id, { status: 'active' })
  }

  async function deleteConversation(id: string) {
    await agentApi.deleteConversation(id)
    conversations.value = conversations.value.filter((item: any) => item.id !== id)
    if (conversation.value?.id === id) {
      closeStream()
      conversation.value = null
      activeRun.value = null
      events.value = []
    }
  }

  async function openConversation(id: string) {
    closeStream()
    conversation.value = (await agentApi.conversation(id)).data
    const latestRun = conversation.value.active_run || [...(conversation.value.messages || [])].reverse().find((item: any) => item.run_id)?.run_id
    activeRun.value = latestRun ? await getRun(latestRun) : null
    events.value = activeRun.value?.events || []
  }

  async function getRun(id: string) {
    const result = (await agentApi.run(id)).data
    activeRun.value = result
    events.value = result.events || []
    return result
  }

  async function send(content: string, replay?: string) {
    if (!content.trim()) return
    if (!conversation.value) await newConversation()
    if (!conversation.value) return
    error.value = ''
    const requestId = crypto.randomUUID()
    conversation.value.messages.push({ id: requestId, role: 'user', content, created_at: new Date().toISOString(), context: {} })
    try {
      const result = await agentApi.send(conversation.value.id, { content, request_id: requestId, mode: replay ? 'replay' : 'live', replay })
      activeRun.value = await getRun(result.data.run_id)
      watchRun(result.data.run_id)
    } catch (e: any) {
      error.value = e?.response?.data?.detail || '消息发送失败，请重试'
    }
  }

  async function createResource(payload: { name: string; type: string; config: Record<string, unknown> }) {
    const response = await agentApi.createResource(currentProjectId.value, payload)
    project.value.resources = [...(project.value.resources || []), response.data]
    return response.data
  }

  async function updateResource(id: string, payload: Record<string, unknown>) {
    const response = await agentApi.updateResource(currentProjectId.value, id, payload)
    project.value.resources = (project.value.resources || []).map((item: any) => item.id === id ? response.data : item)
    return response.data
  }

  async function archiveResource(id: string) {
    const response = await agentApi.archiveResource(currentProjectId.value, id)
    project.value.resources = (project.value.resources || []).map((item: any) => item.id === id ? response.data : item)
  }

  async function testResource(id: string) { return (await agentApi.testResource(currentProjectId.value, id)).data }
  async function loadConfiguration() {
    if (!currentProjectId.value) return null
    configuration.value = (await agentApi.configuration(currentProjectId.value)).data
    return configuration.value
  }
  async function runbooks(query = '', component?: string) { return (await agentApi.runbooks(currentProjectId.value, query, component)).data }
  async function cancel() { if (activeRun.value) { await agentApi.cancel(activeRun.value.id); await getRun(activeRun.value.id) } }
  async function resume() {
    if (!activeRun.value) return
    const response = await agentApi.resume(activeRun.value.id)
    activeRun.value = await getRun(response.data.run_id)
    watchRun(response.data.run_id)
  }
  async function approve(id: string) { await agentApi.approveMemory(currentProjectId.value, id); memories.value = (await agentApi.memories(currentProjectId.value)).data }
  async function revoke(id: string) { await agentApi.revokeMemory(currentProjectId.value, id); memories.value = (await agentApi.memories(currentProjectId.value)).data }

  return {
    projects, templates, modelConfig, benchmark, project, configuration, conversations, conversation, memories, activeRun, events, loading, error, currentProjectId,
    loadProjects, loadTemplates, loadModels, loadBenchmark, selectProject, createProject, createTemplate, templateDetail, publishTemplate, restoreTemplate, cloneTemplate,
    publishSpec, restoreSpec, proposeSpec, initializeProject, uploadDocument, saveProjectAsTemplate,
    updateProject, archiveProject, newConversation, updateConversation, archiveConversation, restoreConversation, deleteConversation, openConversation, send, getRun,
    createResource, updateResource, archiveResource, testResource, loadConfiguration, runbooks, cancel, resume, approve, revoke,
  }
})
