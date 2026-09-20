import request from '@/router/axios'

const baseUrl = '/kg/kgknowledgeupload'

export function getListAll(pageNum = 1, pageSize = 10) {
    let query = {
        pageNum:pageNum,
        pageSize:pageSize,
    }
    return request({
        url: `${baseUrl}/list`,
        method: 'get',
        params: query
    })
}

export function getTemplateUrl(ontologyName){
    let query = {
        ontologyName:ontologyName,
    }
    return request({
        url: `${baseUrl}/getTemplateUrl/`,
        method: 'get',
        params: query
    })
}

export function updateByFile(obj){
    return request({
        url: `${baseUrl}/csvFile/`,
        method: 'put',
        data: obj
    })
}

export function deleteByKgUploadId (uploadId){
    return request({
        url: `${baseUrl}/` + uploadId,
        method: 'delete'
    })
}

export function csvDatatoReview (obj,taskId) {
    return request({
        url: `${baseUrl}/csvReview?taskId=${taskId}`,
        method: 'post',
        data: obj
    })
}

//领域字典管理
const baseUrl1 = '/kg/KgFieldDict'

//批量添加领域词汇
export function saveFieldWordList (obj) {
    return request({
        url: `${baseUrl1}/saveFieldWordList`,
        method: 'post',
        data: obj
    })
}

//批量新增同义词
export function saveSynonymList (obj){
    return request({
        url: `${baseUrl1}/saveSynonymList`,
        method: 'post',
        data: obj
    })
}

