import request from '@/router/axios'

//  添加根节点
export function reqAddRootNode(obj) {
    return request({
        url: '/model3d/configgbomtree/addRootNode',
        method: 'post',
        data: obj
    })
}

//  获取模板的所有树节点
export function reqTreeNodes(nodeLevel) {
    return request({
        url: '/model3d/configgbomtree/getTreeNodes/' + nodeLevel,
        method: 'get'
    })
}

//  获取sceneId对应的所有树节点
export function reqTreeNodesBySceneId(sceneId, nodeLevel) {
    return request({
        url: '/model3d/configgbomtree/reqTreeNodesBySceneId',
        method: 'get',
        params: {
            sceneId,
            nodeLevel
        }
    })
}

//获取modelId对应对的所有树节点
export function reqTreeNodesByModelId(modelId, nodeLevel) {
    return request({
        url: '/model3d/configBomTreeTemplate/reqTreeNodesByModelId/',
        method: 'get',
        params: {
            modelId,
            nodeLevel
        }
    })
}

//  根据节点编码查询子节点
export function reqSonNodes(nodeCode) {
    return request({
        url: '/model3d/configgbomtree/getSonNodes/' + nodeCode,
        method: 'get',
    })
}

//  根据节点编码查询子节点
export function reqSonNodesBySceneId(sceneId, nodeCode) {
    return request({
        url: '/model3d/configgbomtree/reqSonNodesBySceneId',
        method: 'get',
        params: {
            sceneId,
            nodeCode
        }
    })
}

export function reqSonNodesByModelId(modelId, nodeCode) {
    return request({
        url: '/model3d/configBomTreeTemplate/reqSonNodesByModelId/',
        method: 'get',
        params: {
            modelId,
            nodeCode
        }
    })
}


//  通过sceneId得到所有节点id
export function getAllNodeIdsBySceneId(sceneId) {
    return request({
        url: '/model3d/configgbomtree/getAllNodeIdsBySceneId',
        method: 'get',
        params: {
            sceneId
        }
    })
}

//  添加子节点
export function reqAddSonNode(parentNodeId, node) {
    return request({
        url: '/model3d/configgbomtree/addSonNode/' + parentNodeId,
        method: 'post',
        data: node
    })
}


//  根据Id查询一个对象
export function reqObjById(nodeId) {
    return request({
        url: '/model3d/configgbomtree/' + nodeId,
        method: 'get',
    })
}

//  节点编辑
export function reqPutObj(obj) {
    return request({
        url: '/model3d/configgbomtree',
        method: 'put',
        data: obj
    })
}

//  删除当前节点及其所有子节点
export function reqDeleteNodes(nodeCode) {
    return request({
        url: '/model3d/configgbomtree/deleteNodes/' + nodeCode,
        method: 'get',
    })
}

//  删除当前节点及其所有子节点
export function reqDeleteNodesBySceneId(sceneId, nodeCode) {
    return request({
        url: '/model3d/configgbomtree/deleteNodesBySceneId/' + sceneId + '/' + nodeCode,
        method: 'get',
    })
}

// 根据节点编码复制一颗子树
export function reqCopyNode(nodeCode) {
    return request({
        url: '/model3d/configgbomtree/copyNodeByNodeCode/' + nodeCode,
        method: 'get',
    })
}

//  节点移动
export function reqMoveNode(sourceNodeCode, targetNodeCode) {
    return request({
        url: '/model3d/configgbomtree/moveNode/',
        method: 'post',
        params: {
            sourceNodeCode,
            targetNodeCode
        }
    })
}

//导入场景元结构树
export function createProGbomTree(turbines) {
    return request({
        url: `/model3d/configgbomtree/createProGbomTree`,
        method: 'post',
        data: turbines
    })
}

//查询型号proId对应的所有元结构树的节点
export function getGBomTreeBySceneIdAndProId(sceneId,proId, nodeLevel) {
    return request({
        url: `/model3d/configgbomtree/getGBomTreeBySceneIdAndProId`,
        method: 'get',
        params:{sceneId, proId, nodeLevel}
    })
}

//查询型号proId对应的所有元结构树的节点
export function getGBomSonTreeBySceneIdAndProId(sceneId,proId, nodeCode) {
    return request({
        url: `/model3d/configgbomtree/getGBomSonTreeBySceneIdAndProId`,
        method: 'get',
        params:{sceneId, proId, nodeCode}
    })
}
