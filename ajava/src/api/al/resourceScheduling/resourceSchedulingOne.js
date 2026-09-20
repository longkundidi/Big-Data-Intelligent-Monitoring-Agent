import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList1(query) {
  return request({
    url: `${baseUrl}/schedulingOne/page`,
    method: 'get',
    params: query
  })
}

export function addObj1(obj) {
  return request({
    url: `${baseUrl}/schedulingOne`,
    method: 'post',
    data: obj
  })
}

export function getSchedulingObj1(name) {
  return request({
    url: `${baseUrl}/schedulingOne/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/schedulingOne/` + id,
    method: 'delete'
  })
}

export function putObj1(obj) {
  return request({
    url: `${baseUrl}/schedulingOne`,
    method: 'put',
    data: obj
  })
}
