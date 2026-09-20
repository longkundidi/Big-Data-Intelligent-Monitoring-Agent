import request from '@/router/axios'

export function fetchList(query) {
    return request({
        url: '/model3d/configfailuremode/page',
        method: 'post',
        data: query
    })
}

//  新增
export function addObj(obj) {
    return request({
        url: '/model3d/configfailuremode',
        method: 'post',
        data: obj
    })
}

export function getObj(id) {
    return request({
        url: '/model3d/configfailuremode/' + id,
        method: 'get'
    })
}

//  删除
export function delObj(id) {
    return request({
        url: '/model3d/configfailuremode/' + id,
        method: 'delete'
    })
}

//  编辑
export function putObj(obj) {
    return request({
        url: '/model3d/configfailuremode',
        method: 'put',
        data: obj
    })
}

//  根据节点id查询故障模式记录
export function getObjsByNodeId(nodeId) {
    return request({
        url: '/model3d/configfailuremode/' + nodeId,
        method: 'get'
    })
}
