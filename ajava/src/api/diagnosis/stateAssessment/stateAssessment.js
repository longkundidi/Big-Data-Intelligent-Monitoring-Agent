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
