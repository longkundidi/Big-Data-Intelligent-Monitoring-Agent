import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList2(query) {
  return request({
    url: `${baseUrl}/stateTwo/page`,
    method: 'get',
    params: query
  })
}

export function addObj2(obj) {
  return request({
    url: `${baseUrl}/stateTwo`,
    method: 'post',
    data: obj
  })
}

export function getEvaluationObj2(name) {
  return request({
    url: `${baseUrl}/stateTwo/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/stateTwo/` + id,
    method: 'delete'
  })
}

export function putObj2(obj) {
  return request({
    url: `${baseUrl}/stateTwo`,
    method: 'put',
    data: obj
  })
}
