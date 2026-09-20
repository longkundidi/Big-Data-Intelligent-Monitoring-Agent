import request from "@/router/axios"

const baseUrl = '/kg/mining';

export function getAllFaultReports() {
    return request({
        url: `${baseUrl}/getAllFaultReports/` ,
        method: 'get',
    })
}

export function getFaultReportsByNode(nodeName) {
    return request({
        url: `${baseUrl}/getFaultReportsByNode/` ,
        method: 'get',
        params: {
            nodeName: nodeName
        }
    })
}

export function startMining(nodeName) {
    return request({
        url: `${baseUrl}/startMining/` ,
        method: 'get',
        params: {
            nodeName: nodeName
        }
    })
}

export function requestMiningResult(taskId) {
    return request({
        url: `${baseUrl}/requestMiningResult/` ,
        method: 'get',
        params: {
            taskId: taskId
        }
    })
}
