import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList(query) {
    return request({
        url: `${baseUrl}/stateEvaluation/page`,
        method: 'get',
        params: query
    })
}

export function addObj(obj) {
    return request({
        url: `${baseUrl}/stateEvaluation`,
        method: 'post',
        data: obj
    })
}

export function addAlInput(obj) {
    return request({
        url: `model3d/alInput/saveAlInput`,
        method: 'post',
        data: obj
    })
}

export function getEvaluationObj(id) {
    return request({
        url: `${baseUrl}/stateEvaluation/` + id,
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
export function delEvaluationObj(id) {
    return request({
        url: `${baseUrl}/stateEvaluation/` + id,
        method: 'delete'
    })
}

export function delEvaluationInput(name, alClass) {
    return request({
        url: `model3d/alInput/` + name + `/` + alClass,
        method: 'delete'
    })
}

export function putObj(obj) {
    return request({
        url: `${baseUrl}/stateEvaluation`,
        method: 'put',
        data: obj
    })
}

export function updateEvaluationExamples(id, input, output) {
    return request({
        url: `${baseUrl}/stateEvaluation/${id}/examples`,
        method: 'put',
        data: { input, output }
    })
}

export function fetchList1(query) {
    return request({
        url: `${baseUrl}/stateEvaluation/page1`,
        method: 'get',
        params: query
    })
}

export function fetchList2(query) {
    return request({
        url: `${baseUrl}/stateEvaluation/page2`,
        method: 'get',
        params: query
    })
}

export function exitName(name) {
    return request({
        url: `${baseUrl}/stateEvaluation/exitName/` + name,
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
