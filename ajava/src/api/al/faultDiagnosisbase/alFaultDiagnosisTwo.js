import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList2(query) {
  return request({
    url: `${baseUrl}/domaintwo/page`,
    method: 'get',
    params: query
  })
}

export function addObj2(obj) {
  return request({
    url: `${baseUrl}/domaintwo`,
    method: 'post',
    data: obj
  })
}

export function getDiagnosisObj2(name) {
  return request({
    url: `${baseUrl}/domaintwo/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/domaintwo/` + id,
    method: 'delete'
  })
}

export function putObj2(obj) {
  return request({
    url: `${baseUrl}/domaintwo`,
    method: 'put',
    data: obj
  })
}
