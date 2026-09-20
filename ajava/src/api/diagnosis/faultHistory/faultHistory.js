import request from '@/router/axios'

//  根据任务Id查询数据
export function fetchList(query) {
    return request({
        url: '/model3d/configresumefaultdata/page',
        method: 'get',
        params: query
    })
}

export function getFaultMode(query) {
    return request({
        url: '/model3d/configfailuremode/getByBomNodeName',
        method: 'get',
        params: query
    })
}

export function addFaultData(obj) {
    return request({
        url: '/model3d/configresumefaultdata',
        method: 'post',
        data: obj
    })
}