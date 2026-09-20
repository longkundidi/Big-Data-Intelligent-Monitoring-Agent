import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function exitModelName(name) {
    return request({
        url: `${baseUrl}/domainModel/exitName/` + name,
        method: 'get',
    })
}

export function addDomainModel(obj) {
    return request({
        url: `${baseUrl}/domainModel`,
        method: 'post',
        data: obj
    })
}

export function fetchDomainModel(query) {
    return request({
        url: `${baseUrl}/domainModel/getlistByBasicAlgorithm`,
        method: 'get',
        params: query
    })
}

export function fetchAllDomainModels() {
    return request({
        url: `${baseUrl}/domainModel/list`,
        method: 'get'
    })
}

export function putModelObj(obj) {
    return request({
        url: `${baseUrl}/domainModel`,
        method: 'put',
        data: obj
    })
}

export function getModelObj(id) {
    return request({
        url: `${baseUrl}/domainModel/` + id,
        method: 'get'
    })
}

export function delModelObj(id) {
    return request({
        url: `${baseUrl}/domainModel/` + id,
        method: 'delete'
    })
}

export function getCurrentObject(query) {
    return request({
        url: `${baseUrl}/domainModel/getCurrentObject`,
        method: 'get',
        params: query
    })
}

export function getCurrentVariables(query) {
    return request({
        url: `model3d/alInput/getCurrentVariables`,
        method: 'get',
        params: query
    })
}

export function variablesValidation(query) {
    return request({
        url: `model3d/alInput/ `,
        method: 'get',
        params: query
    })
}
