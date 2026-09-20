import request from "@/router/axios";

const baseUrl = "al"

export function addProject(alResumeDataDtoList) {
    return request({
        url: `${baseUrl}/alresumedata`,
        method: 'post',
        data: alResumeDataDtoList
    })
}

//导入风机结构树，生成机型模板
export function createProTemplate(turbines) {
    return request({
        url: `/model3d/configBomTreeTemplate/createProTempalte`,
        method: 'post',
        data:turbines
    })
}

//导入模板风机感知变量
export function importVal(proName, productModel, obj) {
    return request({
        url: `/model3d/configBomPerceivedVariableTemplate/createProVarTempalte/` + proName + '/' + productModel,
        method: 'post',
        data: obj
    })
}

//导入场景元结构树感知变量
export function importSceneVal(sceneId, obj) {
    return request({
        url: `/model3d/configBomPerceivedVariableTemplate/createProVarTempalte/` + sceneId,
        method: 'post',
        data: obj
    })
}

//根据机型模板生成实例风机
export function createProInstance(query) {
    return request({
        url: `/model3d/configBomTreeTemplate/createProInstance`,
        method: 'post',
        params:query
    })
}

//根据机型生成实例对象
export function createObjInstance(query) {
    return request({
        url: `/model3d/configBomTreeTemplate/createObjInstance`,
        method: 'post',
        params:query
    })
}

//  根据节点编码查询子节点
export function reqSonNodes (proId, nodeCode) {
    return request({
        url: `/model3d/configBomTreeTemplate/getSonNodes/` + proId,
        method: 'get',
        params: {
            nodeCode: nodeCode
        }
    })
}

//  根据节点编码查询子节点
export function reqSonNodesByScene (proId, nodeCode) {
    return request({
        url: `/model3d/configBomTreeTemplate/reqSonNodesByScene/` + proId,
        method: 'get',
        params: {
            nodeCode: nodeCode
        }
    })
}

//  获取模板的所有树节点
export function reqTreeNodes (proId, nodeLevel) {
    return request({
        url: `/model3d/configBomTreeTemplate/getTreeNodes/` + proId,
        method: 'get',
        params: {
            nodeLevel: nodeLevel
        }
    })
}

//  获取模板的所有树节点
export function getTreeNodesByScene (proId, nodeLevel) {
    return request({
        url: `/model3d/configBomTreeTemplate/getTreeNodesByScene/` + proId,
        method: 'get',
        params: {
            nodeLevel: nodeLevel
        }
    })
}

export function uploadSonNode (nodeId, datasetType,datasetUrl,provider,datasetName) {
    return request({
        url: `/model3d/configBomTreeTemplate/uploadSonNode/` + nodeId,
        method: 'post',
        params: {
            datasetType:datasetType,
            datasetUrl: datasetUrl,
            provider:provider,
            datasetName:datasetName
        }
    })
}

export function replaceSonNode (nodeId, datasetType,datasetUrl,provider, replaceVersionType, datasetName) {
    return request({
        url: `/model3d/configBomTreeTemplate/replaceSonNode/` + nodeId,
        method: 'post',
        params: {
            datasetType:datasetType,
            datasetUrl: datasetUrl,
            provider:provider,
            replaceVersionType:replaceVersionType,
            datasetName:datasetName
        }
    })
}

export function revertDataset (nodeId, datasetType) {
    return request({
        url: `/model3d/configBomTreeTemplate/revertDataset/` + nodeId,
        method: 'post',
        params: {
            datasetType:datasetType,
        }
    })
}
//  添加子节点
export function reqAddSonNode (proId, parentNodeId, node) {
    return request({
        url: '/model3d/configBomTreeTemplate/addSonNode/' + proId + '/' + parentNodeId,
        method: 'post',
        data: node
    })
}

//  根据Id查询一个对象
export function reqObjById (proId, nodeId) {
    return request({
        url: '/model3d/configBomTreeTemplate/getConfigBomTreeTemp/' + proId + '/' + nodeId,
        method: 'get',
    })
}

//  节点编辑
export function reqPutObj (proId, nodeForm) {
    return request({
        url: '/model3d/configBomTreeTemplate/editNodeInfo/' + proId,
        method: 'put',
        data: nodeForm
    })
}

//  删除当前节点及其所有子节点
export function reqDeleteNodes (query) {
    return request({
        url: '/model3d/configBomTreeTemplate/delNode',
        method: 'get',
        params: query
    })
}

// 根据节点编码复制一颗子树
export function reqCopyNode (proId, nodeCode) {
    return request({
        url: '/model3d/configBomTreeTemplate/copySonNode/' + proId,
        method: 'put',
        params:{
            nodeCode: nodeCode
        }
    })
}

//  节点移动
export function reqMoveNode (proId, sourceNodeCode, targetNodeCode) {
    return request({
        url: '/model3d/configBomTreeTemplate/moveNode/' + proId,
        method: 'post',
        params: {
            sourceNodeCode,
            targetNodeCode
        }
    })
}

//  获取实例风机的所有树节点
export function reqInstanceTreeNodes (proId, nodeLevel) {
    return request({
        url: `/model3d/configmodel/getTreeNodes/` + proId,
        method: 'get',
        params: {
            nodeLevel: nodeLevel
        }
    })
}

//  根据节点编码查询实例风机子节点
export function reqInstanceSonNodes (nodeCode, turbineCode) {
    return request({
        url: `/model3d/configmodel/getSonNodes`,
        method: 'get',
        params: {
            nodeCode: nodeCode,
            turbineCode: turbineCode
        }
    })
}

//  添加实例风机子节点
export function reqAddInstanceSonNode (parentNodeId, node) {
    return request({
        url: '/model3d/configmodel/addSonNode/' + parentNodeId,
        method: 'post',
        data: node
    })
}

//  根据Id查询一个实例风机对象
export function reqInstanceObjById (nodeId) {
    return request({
        url: '/model3d/configmodel/getConfigBomTree/' + nodeId,
        method: 'get',
    })
}

//  实例风机节点编辑
export function reqPutInstanceObj (obj) {
    return request({
        url: '/model3d/configmodel/editNodeInfo',
        method: 'put',
        data: obj
    })
}

//  删除实例风机当前节点及其所有子节点
export function reqDeleteInstanceNodes (proId, turbineCode, nodeCode) {
    return request({
        url: '/model3d/configmodel/delNode/' + proId,
        method: 'get',
        params: {
            turbineCode: turbineCode,
            nodeCode: nodeCode
        }
    })
}

// 根据节点编码复制一颗实例风机子树
export function reqCopyInstanceNode (query) {
    return request({
        url: '/model3d/configmodel/copySonNode',
        method: 'put',
        params: query
    })
}

//  实例风机节点移动
export function reqMoveInstanceNode (turbineCode, sourceNodeCode, targetNodeCode) {
    return request({
        url: '/model3d/configmodel/moveNode',
        method: 'post',
        params: {
            turbineCode,
            sourceNodeCode,
            targetNodeCode
        }
    })
}

export function getDataUrl (query) {
    return request({
        url: `/model3d/configBomTreeTemplate/getDataUrl`,
        method: 'get',
        params: query
    })
}

//获取最新和历史版本的数据集url
export function getAllDataUrl (query) {
    return request({
        url: `/model3d/configBomTreeTemplate/getAllDataUrl`,
        method: 'get',
        params: query
    })
}

export function getDataProvider (query) {
    return request({
        url: `/model3d/configBomTreeTemplate/getDataProvider`,
        method: 'get',
        params: query
    })
}

//添加机型结构树
export function addProductModelTree(proId, selectNodes) {
    return request({
        url: '/model3d/configBomTreeTemplate/addProductModelTree/' + proId,
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        data: selectNodes
    });
}

//添加机型结构树
export function updateProductModelTree(proId, selectNodes) {
    return request({
        url: '/model3d/configBomTreeTemplate/updateProductModelTree/' + proId,
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        data: selectNodes
    });
}

//复制机型
export function copyModelTreeAndVar(copyName, newId, proId) {
    return request({
        url: '/model3d/configBomTreeTemplate/copyModelTreeAndVar',
        method: 'get',
        params: {copyName, newId, proId}
    });
}

//添加机型感知变量
export function addProductModelVariables(proId, modelTreeVariable) {
    return request({
        url: '/model3d/configBomTreeTemplate/addProductModelVariables/' + proId,
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        data: modelTreeVariable
    });
}

//添加机型感知变量
export function addProductModelDataSets(proId, uploadDataSets) {
    return request({
        url: '/model3d/configBomTreeTemplate/addProductModelDataSets/' + proId,
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        data: uploadDataSets
    });
}