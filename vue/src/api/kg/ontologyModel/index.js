import request from "@/router/axios"

const baseUrl = '/kg/ontologymodel';

export function getOntodata(ontologyName) {
    return request({
        url: `${baseUrl}/getOntodata/` + ontologyName,
        method: 'get',
    })
}
export function updateNode(obj) {
    return request({
        url: `${baseUrl}/updateNode`,
        method: 'post',
        data: obj
    })
}
export function addNode(obj) {
    return request({
        url: `${baseUrl}/addNode`,
        method: 'post',
        data: obj
    })
}

export const addAttribute = function (obj) {
    return request({
        url: `${baseUrl}/addAttribute`,
        method: 'post',
        data: obj
    })
}

export const updateAttribute = function (obj) {
    return request({
        url: `${baseUrl}/updateAttribute`,
        method: 'post',
        data: obj
    })
}

export const addRelation = function (obj) {
    return request({
        url: `${baseUrl}/addRelation`,
        method: 'post',
        data: obj
    })
}

export const editRelation = function (obj) {
    return request({
        url: `${baseUrl}/editRelation`,
        method: 'post',
        data: obj
    })
}

export const deleteMetaNode = function (obj) {
    return request({
        url: `${baseUrl}/deleteMetaNode`,
        method: 'post',
        data: obj
    })
}

export const deleteAttribute = function (obj) {
    return request({
        url: `${baseUrl}/deleteAttribute`,
        method: 'post',
        data: obj
    })
}

export const deleteOtherNode = function (obj) {
    return request({
        url: `${baseUrl}/deleteOtherNode`,
        method: 'post',
        data: obj
    })
}

export const deleteRelation = function (obj) {
    return request({
        url: `${baseUrl}/deleteRelation`,
        method: 'post',
        data: obj
    })
}

export const updateTempFile = function (ontologyName) {
    return request({
        url: `${baseUrl}/updateTempFile/` + ontologyName,
        method: 'get',
    })
}