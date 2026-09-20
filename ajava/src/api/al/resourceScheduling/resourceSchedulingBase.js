import request from "@/router/axios"

const baseUrl = '/al';

export function fetchList(query) {
    return request({
        url: `${baseUrl}/resourceScheduling/page`,
        method: 'get',
        params: query
    })
}

export function addObj(obj) {
    return request({
        url: `${baseUrl}/resourceScheduling`,
        method: 'post',
        data: obj
    })
}

export function getSchedulingObj(id) {
    return request({
        url: `${baseUrl}/resourceScheduling/` + id,
        method: 'get'
    })
}

export function downLoad_Dataset_s(query) {
    return request({
        url: `${baseUrl}/file/downLoad_Dataset/`,
        method: 'get',
        responseType: 'blob',
        params:query
    })
}
export function delSchedulingObj(id) {
    return request({
        url: `${baseUrl}/resourceScheduling/` + id,
        method: 'delete'
    })
}

export function putObj(obj) {
    return request({
        url: `${baseUrl}/resourceScheduling`,
        method: 'put',
        data: obj
    })
}

export function fetchList1(query) {
    return request({
        url: `${baseUrl}/resourceScheduling/page1`,
        method: 'get',
        params: query
    })
}

export function fetchList2(query) {
    return request({
        url: `${baseUrl}/resourceScheduling/page2`,
        method: 'get',
        params: query
    })
}

export function exitName(name) {
    return request({
        url: `${baseUrl}/resourceScheduling/exitName/` + name,
        method: 'get',
    })
}


export function uploadIcon(file) {
    return request({
        url: `${baseUrl}/file/uploadFile2`,
        method: 'post',
        data: file,
        config: {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    })
}

export function uploadProgram(file) {
    return request({
        url: `${baseUrl}/file/uploadFile4`,
        method: 'post',
        data: file,
        config: {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    })
}

export function autoGetMetrics(query) {
    return request({
        url: `${baseUrl}/altask/autoGetMetrics/` ,
        method: 'get',
        params:query
    })
}
export function startJob(id,alModelType,useCase) {
    return request({
        url: `${baseUrl}/altask/startJob`,
        params: {
            id: id,
            alModelType:alModelType,
            useCase:useCase
        },
        method: 'get',
        timeout: 60000
    })
}

export function getObj(id) {
    return request({
        url: `${baseUrl}/altask/` + id,
        method: 'get'
    })
}
export function getList(query) {
    return request({
        url: `${baseUrl}/sysMetrics/list`,
        method: 'get',
        params:query
    })
}
