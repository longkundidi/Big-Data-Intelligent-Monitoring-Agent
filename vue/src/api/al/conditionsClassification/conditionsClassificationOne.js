import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList1(query) {
  return request({
    url: `${baseUrl}/conditionsOne/page`,
    method: 'get',
    params: query
  })
}

export function addObj1(obj) {
  return request({
    url: `${baseUrl}/conditionsOne`,
    method: 'post',
    data: obj
  })
}

export function getClassificationObj1(name) {
  return request({
    url: `${baseUrl}/conditionsOne/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/conditionsOne/` + id,
    method: 'delete'
  })
}

export function putObj1(obj) {
  return request({
    url: `${baseUrl}/conditionsOne`,
    method: 'put',
    data: obj
  })
}
