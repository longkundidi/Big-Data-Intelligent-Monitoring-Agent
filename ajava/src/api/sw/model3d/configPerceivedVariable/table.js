import request from '@/router/axios'

export function fetchList(query) {
    return request({
        url: '/model3d/configperceivedvariable/page',
        method: 'post',
        data: query
    })
}

export function getPageByNodeType(query) {
    return request({
        url: '/model3d/configperceivedvariable/getPageByNodeType',
        method: 'post',
        data: query
    })
}

export function getAllByNodeType(query) {
    return request({
        url: '/model3d/configperceivedvariable/getAllByNodeType',
        method: 'post',
        data: query
    })
}

export function getListByNodeId(nodeId) {
    return request({
        url: '/model3d/configperceivedvariable/getListByNodeId',
        method: 'get',
        params: {nodeId}
    })
}

//  新增
export function addObj(obj) {
    return request({
        url: '/model3d/configperceivedvariable',
        method: 'post',
        data: obj
    })
}

export function getObj(id) {
    return request({
        url: '/model3d/configperceivedvariable/' + id,
        method: 'get'
    })
}

//  删除
export function delObj(id) {
    return request({
        url: '/model3d/configperceivedvariable/' + id,
        method: 'delete'
    })
}

//  删除机型感知变量
export function delModelVar(id) {
    return request({
        url: '/model3d/configBomPerceivedVariableTemplate/' + id,
        method: 'delete'
    })
}

//  编辑
export function putObj(obj) {
    return request({
        url: '/model3d/configperceivedvariable',
        method: 'put',
        data: obj
    })
}

//  根据节点id查询关联的感知变量
export function getObjsByNodeId(nodeId) {
    return request({
        url: '/model3d/configperceivedvariable/' + nodeId,
        method: 'get'
    })
}

export function getAllVariablesByNodes(selectNodes) {
    return request({
        url: '/model3d/configperceivedvariable/getAllVariablesByNodes',
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        data: selectNodes
    });
}

// gBom感知变量匹配,返回未完全匹配的变量的模糊匹配
export function gBomVariablesMatch(sceneId, variableNames) {
    return request({
        url: '/model3d/configperceivedvariable/gBomVariablesMatch/' + sceneId,
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        data: variableNames
    })
}



