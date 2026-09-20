import request from '@/router/axios'

//  根据任务Id查询数据
export function reqDataSet(taskId, algoShortname) {
    return request({
        url: '/model3d/dcAlgorithm/getResultByTaskId',
        method: 'get',
        params: {
            taskId,
            algoShortname
        }
    })
}

// 根据任务Id统计当前时间（含今天）往前7天的数据量
export function getResultCountByTaskId(taskId) {
    return request({
        url: '/model3d/dcAlgorithm/getResultCountByTaskId',
        method: 'get',
        params: {
            taskId
        }
    })
}

// 根据任务Id按天统计近7天故障数量
export function getFaultCountByTaskId(taskId) {
    return request({
        url: '/model3d/dcAlarm/getFaultCountByTaskId',
        method: 'get',
        params: {
            taskId
        }
    })
}

export function getDiagnosisFaultStats(turbineCode) {
    return request({
        url: '/model3d/dcAlarm/getDiagnosisFaultStats',
        method: 'get',
        params: {
            turbineCode
        }
    })
}

export function reqFaultSet(taskId, algoShortname) {
    return request({
        url: '/model3d/dcError/getVarerrorByTaskId',
        method: 'get',
        params: {
            taskId,
            algoShortname
        }
    })
}

export function reqHistoryDataSet(query) {
    return request({
        url: '/model3d/dcalgorithmhistory/getHistoryDcData',
        method: 'get',
        params: query
    })
}

export function getFaultByTime(query) {
    return request({
        url: '/model3d/configresumefaultdata/getFaultByTime',
        method: 'get',
        params: query
    })
}
