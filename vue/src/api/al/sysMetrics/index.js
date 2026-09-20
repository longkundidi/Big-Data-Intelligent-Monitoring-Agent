import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function getPage(query) {
    return request({
        url: `${baseUrl}/sysMetrics/page`,
        method: 'get',
        params: query
    })
}

export function getById(id) {
    return request({
        url: `${baseUrl}/sysMetrics/${id}`,
        method: 'get'
    })
}

export function searchByAltypeOrName(query) {
    return request({
        url: `${baseUrl}/sysMetrics/searchByAltypeOrName`,
        method: 'get',
        params:query
    })
}

export function addMetrics(obj) {
    return request({
        url: `${baseUrl}/sysMetrics/add`,
        method: 'post',
        data: obj
    })
}

export function deleteMetrics(id) {
    return request({
        url: `${baseUrl}/sysMetrics/delete/${id}`,
        method: 'delete',
    })
}

export function editMetrics(obj) {
    return request({
        url: `${baseUrl}/sysMetrics/edit`,
        method: 'put',
        data:obj
    })
}
