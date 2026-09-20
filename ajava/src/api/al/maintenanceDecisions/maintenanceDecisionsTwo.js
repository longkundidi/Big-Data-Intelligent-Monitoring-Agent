import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList2(query) {
  return request({
    url: `${baseUrl}/maintenanceTwo/page`,
    method: 'get',
    params: query
  })
}

export function addObj2(obj) {
  return request({
    url: `${baseUrl}/maintenanceTwo`,
    method: 'post',
    data: obj
  })
}

export function getDecisionsObj2(name) {
  return request({
    url: `${baseUrl}/maintenanceTwo/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/maintenanceTwo/` + id,
    method: 'delete'
  })
}

export function putObj2(obj) {
  return request({
    url: `${baseUrl}/maintenanceTwo`,
    method: 'put',
    data: obj
  })
}
