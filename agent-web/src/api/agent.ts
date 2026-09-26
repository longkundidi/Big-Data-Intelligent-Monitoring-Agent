import axios from 'axios'

const api = axios.create({ baseURL: '/api/agent', timeout: 15000 })

export const agentApi = {
  models: () => api.get('/models'),
  benchmark: (split?: 'dev' | 'test') => api.get('/benchmark', { params: { split } }),
  templates: () => api.get('/templates'),
  template: (id: string, version?: number) => api.get(`/templates/${id}`, { params: { version } }),
  createTemplate: (payload: Record<string, unknown>) => api.post('/templates', payload),
  publishTemplate: (id: string, payload: Record<string, unknown>) => api.post(`/templates/${id}/versions`, payload),
  restoreTemplate: (id: string, version: number) => api.post(`/templates/${id}/versions/${version}/restore`),
  cloneTemplate: (id: string, payload: Record<string, unknown>) => api.post(`/templates/${id}/clone`, payload),
  archiveTemplate: (id: string) => api.delete(`/templates/${id}`),
  projects: () => api.get('/projects'),
  createProject: (payload: Record<string, unknown>) => api.post('/projects', payload),
  project: (id: string) => api.get(`/projects/${id}`),
  updateProject: (id: string, payload: Record<string, unknown>) => api.patch(`/projects/${id}`, payload),
  archiveProject: (id: string) => api.delete(`/projects/${id}`),
  topology: (id: string) => api.get(`/projects/${id}/topology`),
  configuration: (id: string) => api.get(`/projects/${id}/configuration`),
  projectSpec: (id: string, versionId?: string) => api.get(`/projects/${id}/spec`, { params: { version_id: versionId } }),
  specVersions: (id: string) => api.get(`/projects/${id}/spec/versions`),
  publishSpec: (id: string, payload: Record<string, unknown>) => api.post(`/projects/${id}/spec/publish`, payload),
  restoreSpec: (id: string, versionId: string) => api.post(`/projects/${id}/spec/versions/${versionId}/restore`),
  proposeSpec: (id: string, payload: Record<string, unknown>) => api.post(`/projects/${id}/spec/propose`, payload),
  initializeProject: (id: string, payload: Record<string, unknown>) => api.post(`/projects/${id}/initialize`, payload),
  documents: (id: string) => api.get(`/projects/${id}/documents`),
  uploadDocument: (id: string, payload: Record<string, unknown>) => api.post(`/projects/${id}/documents`, payload),
  saveProjectAsTemplate: (id: string, payload: Record<string, unknown>) => api.post(`/projects/${id}/save-as-template`, payload),
  conversations: (projectId: string, includeArchived = false) => api.get(`/projects/${projectId}/conversations`, { params: { include_archived: includeArchived } }),
  createConversation: (projectId: string, payload: Record<string, unknown>) => api.post(`/projects/${projectId}/conversations`, payload),
  updateConversation: (id: string, payload: Record<string, unknown>) => api.patch(`/conversations/${id}`, payload),
  deleteConversation: (id: string) => api.delete(`/conversations/${id}`),
  resources: (projectId: string) => api.get(`/projects/${projectId}/resources`),
  createResource: (projectId: string, payload: Record<string, unknown>) => api.post(`/projects/${projectId}/resources`, payload),
  updateResource: (projectId: string, id: string, payload: Record<string, unknown>) => api.patch(`/projects/${projectId}/resources/${id}`, payload),
  archiveResource: (projectId: string, id: string) => api.delete(`/projects/${projectId}/resources/${id}`),
  testResource: (projectId: string, id: string) => api.post(`/projects/${projectId}/resources/${id}/test`),
  runbooks: (projectId: string, query = '', component?: string) => api.get(`/projects/${projectId}/runbooks`, { params: { query, component } }),
  conversation: (id: string) => api.get(`/conversations/${id}`),
  send: (conversationId: string, payload: Record<string, unknown>) => api.post(`/conversations/${conversationId}/messages`, payload),
  run: (id: string) => api.get(`/runs/${id}`),
  cancel: (id: string) => api.post(`/runs/${id}/cancel`),
  resume: (id: string) => api.post(`/runs/${id}/resume`),
  memories: (projectId: string) => api.get(`/projects/${projectId}/memories`),
  approveMemory: (projectId: string, id: string) => api.post(`/projects/${projectId}/memories/${id}/approve`),
  revokeMemory: (projectId: string, id: string) => api.post(`/projects/${projectId}/memories/${id}/revoke`),
  artifact: (id: string) => api.get(`/artifacts/${id}`),
}

export const streamRun = (runId: string, onEvent: (event: any) => void, onDone: () => void) => {
  const source = new EventSource(`/api/agent/runs/${runId}/events`)
  source.onmessage = (event) => onEvent(JSON.parse(event.data))
  source.onerror = () => { source.close(); onDone() }
  return source
}
