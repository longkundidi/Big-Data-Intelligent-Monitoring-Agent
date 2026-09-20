import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList1(query) {
  return request({
    url: `${baseUrl}/domainone/page`,
    method: 'get',
    params: query
  })
}

export function addObj1(obj) {
  return request({
    url: `${baseUrl}/domainone`,
    method: 'post',
    data: obj
  })
}

export function getDiagnosisObj1(name) {
  return request({
    url: `${baseUrl}/domainone/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/domainone/` + id,
    method: 'delete'
  })
}

export function putObj1(obj) {
  return request({
    url: `${baseUrl}/domainone`,
    method: 'put',
    data: obj
  })
}
