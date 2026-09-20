import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList2(query) {
  return request({
    url: `${baseUrl}/transmitTwo/page`,
    method: 'get',
    params: query
  })
}

export function addObj2(obj) {
  return request({
    url: `${baseUrl}/transmitTwo`,
    method: 'post',
    data: obj
  })
}

export function getTransmitObj2(name) {
  return request({
    url: `${baseUrl}/transmitTwo/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/transmitTwo/` + id,
    method: 'delete'
  })
}

export function putObj2(obj) {
  return request({
    url: `${baseUrl}/transmitTwo`,
    method: 'put',
    data: obj
  })
}
