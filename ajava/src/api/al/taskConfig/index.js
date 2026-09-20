import request from '@/router/axios'

const baseUrl = '/al';


export function getConfigs(query) {
    return request({
        url: `${baseUrl}/domainmodelconfiguration/page`,
        method: 'get',
        params: query
    })
}

export function getAlConfig() {
    return request({
        url: `${baseUrl}/algorithmconfiguration/getAllConfigs`,
        method: 'get',
    })
}

export function checkConfig(obj,query) {
    return request({
        url: `${baseUrl}/domainmodelconfiguration/checkConfig`,
        method: 'put',
        data: obj,
        params: query
    })
}

export function checkAlConfig(obj,query) {
    return request({
        url: `${baseUrl}/algorithmconfiguration/checkConfig`,
        method: 'put',
        data: obj,
        params: query
    })
}

export function updateAlIspublishedConfig(obj) {
    return request({
        url: `${baseUrl}/algorithmconfiguration/updateIspublishedConfig`,
        method: 'put',
        data: obj
    })
}
export function saveModelConfig(obj) {
    return request({
        url: `${baseUrl}/domainmodelconfiguration/saveConfig`,
        method: 'post',
        data: obj
    })
}

export function saveAlConfig(obj) {
    return request({
        url: `${baseUrl}/algorithmconfiguration/saveConfig`,
        method: 'post',
        data: obj
    })
}

export function deleteConfig(query) {
    return request({
        url: `${baseUrl}/domainmodelconfiguration/deleteConfig`,
        method: 'delete',
        params: query
    })
}
export function deleteAlConfig(query) {
    return request({
        url: `${baseUrl}/algorithmconfiguration/deleteConfig`,
        method: 'delete',
        params: query
    })
}
export function exitModelConfigName(modelName) {
    return request({
        url: `${baseUrl}/domainmodelconfiguration/exitConfigName/` + modelName,
        method: 'get',
    })
}

export function exitAlConfigName(modelName) {
    return request({
        url: `${baseUrl}/algorithmconfiguration/exitConfigName/` + modelName,
        method: 'get',
    })
}

export function getVariablesByalModelNames(query) {
    return request({
        url: '/model3d/alInput/getVariablesByalModelNames',
        method: 'get',
        params: query
    })
}

export function startConfig(obj) {
    return request({
        url: `${baseUrl}/domainmodelconfiguration/startConfig`,
        method: 'post',
        timeout: 60000,
        data: obj
    })
}

export function startTrain(obj) {
    return request({
        url: `${baseUrl}/algorithmconfiguration/startConfig`,
        method: 'post',
        timeout: 60000,
        data: obj
    })
}

export function getObj(id) {
    return request({
        url: `${baseUrl}/altask/` + id,
        timeout: 60000,
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

export function removeAlInput(name, alClass) {
    return request({
        url: `model3d/alInput/` + name + `/` + alClass,
        method: 'delete',
    })
}
