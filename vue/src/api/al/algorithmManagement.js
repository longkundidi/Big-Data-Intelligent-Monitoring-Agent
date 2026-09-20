import request from '@/router/axios'

const baseUrl = '/al/algorithms'

export function getAlgorithm(type, id) {
  return request({
    url: `${baseUrl}/${type}/${id}`,
    method: 'get'
  })
}

export function createAlgorithm(type, data) {
  return request({
    url: `${baseUrl}/${type}`,
    method: 'post',
    data
  })
}

export function updateAlgorithm(type, id, data) {
  return request({
    url: `${baseUrl}/${type}/${id}`,
    method: 'put',
    data
  })
}

export function deleteAlgorithm(type, id) {
  return request({
    url: `${baseUrl}/${type}/${id}`,
    method: 'delete'
  })
}
