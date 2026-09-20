import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList(query) {
    return request({
        url: `${baseUrl}/dataCleaning/page`,
        method: 'get',
        params: query
    })
}

export function addObj(obj) {
    return request({
        url: `${baseUrl}/dataCleaning`,
        method: 'post',
        data: obj
    })
}

export function getCleanObj(id) {
    return request({
        url: `${baseUrl}/dataCleaning/` + id,
        method: 'get'
    })
}

export function delCleanObj(id) {
    return request({
        url: `${baseUrl}/dataCleaning/` + id,
        method: 'delete'
    })
}

export function putObj(obj) {
    return request({
        url: `${baseUrl}/dataCleaning`,
        method: 'put',
        data: obj
    })
}

export function getTasks(pageNum, pageSize, jobId, jobName, jobType, status, open) {
    return request({
        url: `${baseUrl}/listTask`,
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            pageNum: pageNum,
            pageSize: pageSize,
            jobId: jobId,
            jobName: jobName,
            jobType: jobType,
            status: status,
            open: open
        }
    })
}

export function getTypeNum(query) {
    return request({
        url: `${baseUrl}/dataCleaning/getTypeNum/` + query,
        method: 'get',
    })
}

export function exitName(name) {
    return request({
        url: `${baseUrl}/dataCleaning/exitName/` + name,
        method: 'get',
    })
}


export function uploadIcon(file) {
    return request({
        url: `${baseUrl}/file/uploadFile3`,
        method: 'post',
        data: file,
        config: {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    })
}

export function uploadProgram(alName, file) {
    return request({
        url: `${baseUrl}/file/uploadFile4/` + alName,
        method: 'post',
        data: file,
        config: {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    })
}
