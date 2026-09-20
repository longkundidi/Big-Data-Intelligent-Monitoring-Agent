import request from '@/router/axios'

const baseUrl = '/model3d';


export function fetchList(nodeId) {
    return request({
        url: `${baseUrl}/configBomTreeTemplate/getObj/${nodeId}`,
        method: 'get',
    })
}
export function updateDatasetInfo(nodeId,query,obj) {
    return request({
        url: `${baseUrl}/configBomTreeTemplate/updateDatasetInfo/${nodeId}`,
        method: 'post',
        data:obj,
        params:{type:query}
    })
}
