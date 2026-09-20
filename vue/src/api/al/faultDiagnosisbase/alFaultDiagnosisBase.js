import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList(query) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/page`,
        method: 'get',
        params: query
    })
}

export function addObj(obj) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase`,
        method: 'post',
        data: obj
    })
}

export function getDiagnosisObj(id) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/` + id,
        method: 'get'
    })
}

export function downLoad_Dataset_d(query) {
    return request({
        url: `${baseUrl}/file/downLoad_Dataset/`,
        method: 'get',
        responseType: 'blob',
        params:query
    })
}

export function delDiagnosisObj(id) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/` + id,
        method: 'delete'
    })
}

export function putObj(obj) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase`,
        method: 'put',
        data: obj
    })
}

export function updateDiagnosisExamples(id, input, output) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/${id}/examples`,
        method: 'put',
        data: { input, output }
    })
}

export function fetchList1(query) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/page1`,
        method: 'get',
        params: query
    })
}

export function fetchList2(query) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/page2`,
        method: 'get',
        params: query
    })
}

export function exitName(name) {
    return request({
        url: `${baseUrl}/alFaultDiagnosisbase/exitName/` + name,
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

export function onlineTrain(data) {
    return request({
        url: `${baseUrl}/altask/onlineTrain` ,
        method: 'post',
        data: data,
        config: {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
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
