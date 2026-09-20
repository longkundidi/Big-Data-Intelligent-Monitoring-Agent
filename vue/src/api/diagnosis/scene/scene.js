import request from '@/router/axios'

const baseUrl = '/al';


export function fetchList() {
    return request({
        url: `${baseUrl}/alresumedata/getAllScene`,
        method: 'get',
    })
}


export function fetchProModelMap() {
    return request({
        url: `${baseUrl}/alresumedata/fetchProModelMap`,
        method: 'get',
    })
}

export function addScene(obj) {
    return request({
        url: `${baseUrl}/alresumedata/addScene`,
        method: 'post',
        data: obj,
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

export function addProductModel(id, productModelName) {
    return request({
        url: `${baseUrl}/alresumedata/addProductModel`,
        method: 'get',
        params: {id, productModelName}
    })
}

export function updateProductModel(modelId, productModelName) {
    return request({
        url: `${baseUrl}/alresumedata/updateProductModel`,
        method: 'get',
        params: {modelId, productModelName}
    })
}



export function buildSceneModelTree() {
    return request({
        url: `${baseUrl}/alresumedata/buildSceneModelTree`,
        method: 'get',
    })
}


export function deleteSceneNode(sceneId) {
    return request({
        url: `${baseUrl}/alresumedata/deleteSceneNode`,
        method: 'post',
        params: { sceneId }
    })
}

export function deleteModelNode(modelId) {
    return request({
        url: `${baseUrl}/alresumedata/deleteModelNode`,
        method: 'post',
        params: { modelId }
    })
}


//查询所有的场景元模型
export function getAllSceneMetaModels() {
    return request({
        url: `${baseUrl}/alresumedata/getAllSceneMetaModels`,
        method: 'get',
    })
}