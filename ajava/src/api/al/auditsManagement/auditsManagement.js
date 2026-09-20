import request from "@/router/axios";

const baseUrl = '/al'

export function getNoTestPage(query) {
    return request({
        url: `${baseUrl}/AuditsManagement/getNoTestPage`,
        method: 'get',
        params: query

    })
}

export function getTestNoPassPage(query) {
    return request({
        url: `${baseUrl}/AuditsManagement/getTestNoPassPage`,
        method: 'get',
        params: query

    })
}

export function getTestNoDeployPage(query) {
    return request({
        url: `${baseUrl}/AuditsManagement/getTestNoDeployPage`,
        method: 'get',
        params: query

    })
}

export function getDeployedPage(query) {
    return request({
        url: `${baseUrl}/AuditsManagement/getDeployedPage`,
        method: 'get',
        params: query

    })
}


export function updateTestStatus(obj) {
    return request({
        url: `${baseUrl}/AuditsManagement/updateTestStatus`,
        method: 'post',
        data: obj
    })

}

export function updateDeployStatus(obj) {
    return request({
        url: `${baseUrl}/AuditsManagement/updateDeployStatus`,
        method: 'post',
        data: obj
    })

}

export function updateCheckStatus(obj) {
    return request({
        url: `${baseUrl}/AuditsManagement/updateCheckStatus`,
        method: 'post',
        data: obj
    })

}

export function getByName(query) {
    return request({
        url: `${baseUrl}/AuditsManagement/getByName`,
        method: 'get',
        params: query
    })

}

export function updateProgramUrl(obj) {
    return request({
        url: `${baseUrl}/AuditsManagement/updateProgramUrl`,
        method: 'post',
        data: obj
    })

}
