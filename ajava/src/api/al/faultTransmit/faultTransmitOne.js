import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList1(query) {
  return request({
    url: `${baseUrl}/transmitOne/page`,
    method: 'get',
    params: query
  })
}

export function addObj1(obj) {
  return request({
    url: `${baseUrl}/transmitOne`,
    method: 'post',
    data: obj
  })
}

export function getTransmitObj1(name) {
  return request({
    url: `${baseUrl}/transmitOne/` + name,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/transmitOne/` + id,
    method: 'delete'
  })
}

export function putObj1(obj) {
  return request({
    url: `${baseUrl}/transmitOne`,
    method: 'put',
    data: obj
  })
}
