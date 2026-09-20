import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList2(query) {
  return request({
    url: `${baseUrl}/conditionsTwo/page`,
    method: 'get',
    params: query
  })
}

export function addObj2(obj) {
  return request({
    url: `${baseUrl}/conditionsTwo`,
    method: 'post',
    data: obj
  })
}

export function getClassificationObj2(name) {
  return request({
    url: `${baseUrl}/conditionsTwo/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/conditionsTwo/` + id,
    method: 'delete'
  })
}

export function putObj2(obj) {
  return request({
    url: `${baseUrl}/conditionsTwo`,
    method: 'put',
    data: obj
  })
}
