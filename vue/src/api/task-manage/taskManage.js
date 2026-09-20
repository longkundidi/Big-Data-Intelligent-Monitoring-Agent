import request from '@/router/axios'
import Qs from 'qs'

const baseUrl = '/al';

export function fetchList(query) {
  return request({
    url: `${baseUrl}/altask/page`,
    method: 'get',
    params: query
  })
}
export function getVMlist() {
  return request({
    url: `${baseUrl}/serverList/list`,
    method: 'get',
  })
}

export function updateIsService(obj) {
  return request({
    url: `${baseUrl}/altask/updateIsService`,
    method: 'post',
    data: obj
  })
}
export function getByName(page,query) {
  return request({
    url: `${baseUrl}/altask/getByName/` ,
    method: 'get',
    params:query,
    data:page
  })
}

export function addObj(obj) {
  return request({
    url: `${baseUrl}/altask`,
    method: 'post',
    data: obj
  })
}

export function getObj(id) {
  return request({
    url: `${baseUrl}/altask/` + id,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/altask/` + id,
    method: 'delete'
  })
}

export function putObj(obj) {
  return request({
    url: `${baseUrl}/altask`,
    method: 'put',
    data: obj
  })
}

export function startJob(id) {
  return request({
    url: `${baseUrl}/altask/startJob/` + id,
    method: 'get'
  })
}

export function altest(obj) {
  return request({
    url: `${baseUrl}/altask/altest`,
    method: 'post',
    data: obj
  })
}


export function altestmodel(obj) {
  return request({
    url: `${baseUrl}/altask/altestmodel`,
    method: 'post',
    data: obj
  })
}


export function getLog(query) {
  return request({
    url: `${baseUrl}/altask/getLog`,
    method: 'get',
    params: query
  })
}

export function textExtraction(obj) {
  return request({
    url: `${baseUrl}/altask/testModel`,
    method: 'post',
    data: obj
  })
}

export function containerStats(query) {
  return request({
    url: `${baseUrl}/resourceUsage/containerStats`,
    method: 'get',
    params: query
  })
}
