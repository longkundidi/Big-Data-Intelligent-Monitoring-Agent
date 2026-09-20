import request from '@/router/axios'

export function fetchAlgorithmMenuTree() {
  return request({
    url: '/al/algorithmMenu/tree',
    method: 'get'
  })
}
