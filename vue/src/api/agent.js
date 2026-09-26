import request from '@/router/axios'

const path = '/agent'

export const getTopology = () => request.get(`${path}/topology`)
export const collectObservations = () => request.post(`${path}/observations/collect`)
export const listReplays = () => request.get(`${path}/replays`)
export const listIncidents = () => request.get(`${path}/incidents`)
export const getIncident = id => request.get(`${path}/incidents/${id}`)
export const createIncident = data => request.post(`${path}/incidents`, data)
export const recheckIncident = (id, data = {}) => request.post(`${path}/incidents/${id}/recheck`, data)
export const sendFeedback = (id, data) => request.post(`${path}/incidents/${id}/feedback`, data)
