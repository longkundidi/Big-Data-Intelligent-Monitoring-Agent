import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList2(query) {
  return request({
    url: `${baseUrl}/schedulingTwo/page`,
    method: 'get',
    params: query
  })
}

export function addObj2(obj) {
  return request({
    url: `${baseUrl}/schedulingTwo`,
    method: 'post',
    data: obj
  })
}

export function getSchedulingObj2(name) {
  return request({
    url: `${baseUrl}/schedulingTwo/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/schedulingTwo/` + id,
    method: 'delete'
  })
}

export function putObj2(obj) {
  return request({
    url: `${baseUrl}/schedulingTwo`,
    method: 'put',
    data: obj
  })
}
