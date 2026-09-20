import request from "@/router/axios";

const baseUrl = "al"

//获取项目
export function getProjects(query) {
    return request({
        url: `model3d/alresumedata/getIdFarmsByUserId`,
        method: 'get',
        params: query
    })
}

//获取项目
export function getMetaModelIdByUserId(query) {
    return request({
        url: `model3d/configuserfarm/getMetaModelIdByUserId`,
        method: 'get',
        params: query
    })
}

//根据元模型获取产品机型信息
export function getProductModels(projectName) {
    return request({
        url: `${baseUrl}/alresumedata/getProductModels/` + projectName,
        method: 'get'
    })
}

//根据场景获取产品机型信息
export function getProductModelBySceneName(projectName) {
    return request({
        url: `${baseUrl}/alresumedata/getProductModelBySceneName/` + projectName,
        method: 'get'
    })
}

// 添加实例场景
export function saveScene(params) {
    return request({
        url: `${baseUrl}/alresumedata/saveScene`,
        method: 'get',
        params: params
    })
}

//新建场景后增加管理权限
export function updatePermission(params) {
    return request({
        url: `/model3d/configuserfarm/updatePermission`,
        method: 'get',
        params: params
    })
}

//获取BOM信息
export function getBOMs(proName, modId) {
    return request({
        url: `${baseUrl}/alresumedata/getBOMs/` + proName + `/` + modId,
        method: 'get'
    })
}

//获取风机号
export function getFengji(proId) {
    return request({
        url: `${baseUrl}/alresumedevicedata/getFengji/` + proId,
        method: 'get'
    })
}

export function getVariablesByProject(projectId) {
    return request({
        url: `${baseUrl}/alresumeperceiveddata/getVariablesByProject/` + projectId,
        method: 'get'
    })
}//获取感知变量

export function getDynamicData(query) {
    return request({
        url: `${baseUrl}/alresumeperceiveddata/getVarCharts`,
        method: 'get',
        params: query
    })
}//获取风机号的感知变量对应url


export function getFaultInfoByProjectByFengji(projectId,deviceId) {
    return request({
        url: `${baseUrl}/alresumefaultdata/getFaultInfoByProjectByFengji/` + projectId + `/` + deviceId,
        method: 'get'
    })
}//获取故障数据


export function reqTreeNodes(nodeLevel) {
    return request({
        url: '/model3d/configgbomtree/getTreeNodes/' + nodeLevel,
        method: 'get'
    })
}// 获取模板的所有树节点

export function reqSonNodes(nodeCode) {
    return request({
        url: '/model3d/configgbomtree/getSonNodes/' + nodeCode,
        method: 'get',
    })
}// 根据节点编码查询子节点

//获取机型结构
export function getModelStructure(projectId) {
    return request({
        url: '/model3d/configBomTreeTemplate/getModelStructure',
        method: 'get',
        params: {projectId},
    })
}

// 删除场景节点
export function deleteSceneNode(sceneIds,sceneName,userId) {
    return request({
        url: '/model3d/configBomTreeTemplate/deleteSceneNode',
        method: 'post',
        data: {
            sceneIds: sceneIds,
            sceneName:sceneName,
            userId: userId
        },
    })
}

//上传机组数据
export function uploadUnitData(data) {
    return request({
        url: '/model3d/configmodel/uploadUnitData', // 根据后端接口实际地址调整
        method: 'post',
        data: data
    });
}
