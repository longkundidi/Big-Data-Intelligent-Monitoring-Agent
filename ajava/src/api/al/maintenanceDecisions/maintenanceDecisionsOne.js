import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList1(query) {
  return request({
    url: `${baseUrl}/maintenanceOne/page`,
    method: 'get',
    params: query
  })
}

export function addObj1(obj) {
  return request({
    url: `${baseUrl}/maintenanceOne`,
    method: 'post',
    data: obj
  })
}

export function getDecisionsObj1(name) {
  return request({
    url: `${baseUrl}/maintenanceOne/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/maintenanceOne/` + id,
    method: 'delete'
  })
}

export function putObj1(obj) {
  return request({
    url: `${baseUrl}/maintenanceOne`,
    method: 'put',
    data: obj
  })
}
