import request from '@/router/axios'

export function fetchList(query) {
    return request({
        url: '/model3d/configmodel/page',
        method: 'post',
        data: query
    })
}

//  新增
export function addObj(obj) {
    return request({
        url: '/model3d/configmodel',
        method: 'post',
        data: obj
    })
}

export function getObj(id) {
    return request({
        url: '/model3d/configmodel/' + id,
        method: 'get'
    })
}

//  删除
export function delObj(query) {
    return request({
        url: '/model3d/configmodel/delete',
        method: 'delete',
        params: query
    })
}

//  编辑
export function putObj(obj) {
    return request({
        url: '/model3d/configmodel',
        method: 'put',
        data: obj
    })
}

//  根据节点id查询关联的诊断模型
export function getObjsByNodeId(nodeId, modelType) {
    return request({
        url: '/model3d/configmodel',
        method: 'post',
        params: {
            nodeId,
            modelType
        }
    })
}

//  根据节点id查询关联的感知变量
export function getVarsByNodeId(nodeId) {
    return request({
        url: '/model3d/configperceivedvariable/' + nodeId,
        method: 'get'
    })
}

//  根据实例风机节点id查询关联的感知变量
export function getVarsByBomNodeId(nodeId, options = {}) {
    return request({
        url: '/model3d/configbomperceivedvariable/' + nodeId,
        method: 'get',
        silentError: options.silentError
    })
}

export function prepareTaskVariables(nodeId, modelId) {
    return request({
        url: '/model3d/configbomperceivedvariable/taskCandidates',
        method: 'get',
        params: { nodeId, modelId }
    })
}

export function reqAddModel(modelConfig, varList) {
    return request({
        url: '/model3d/configmodel/addService',
        method: 'post',
        data: [modelConfig, varList]
    })
}

export function reqEditModel(modelConfig, varList) {
    return request({
        url: '/model3d/configmodel/editService',
        method: 'post',
        data: [modelConfig, varList]
    })
}

//  请求服务算法列表
export function reqAlgoList() {
    return request({
        url: '/model3d/ai/getAllByBD',
        method: 'get',
    })
}

//  请求服务算法列表
export function reqAlgoList1() {
    return request({
        url: '/model3d/ai/getAllFaultDiagnosisByBD',
        method: 'get',
    })
}

//  请求服务算法列表(组态模型)
export function reqDomainConfigList() {
    return request({
        url: '/model3d/ai/getAllDomainConfigByBD',
        method: 'get',
    })
}

//  根据modelId在表config_model_variable中查询关联的感知变量
export function getVarsByModelId(modelId) {
    return request({
        url: '/model3d/configperceivedvariable/getIDByModelId/' + modelId,
        method: 'get'
    })
}

export function pageByModelId(query) {
    return request({
        url: '/model3d/configmodel/pageByModelId',
        method: 'post',
        data: query
    })
}

//获取项目
export function getProjects() {
    return request({
        url: `/al/alresumedata/getProjects`,
        method: 'get'
    })
}

//根据元模型Id获取零部件匹配信息
export function getComponentMatch(query) {
    return request({
        url: '/model3d/configmodel/componentMatch',
        method: 'get',
        params: query
    })
}


//根据元模型Id获取感知变量匹配信息
export function getVariableMatch(query) {
    return request({
        url: '/model3d/configmodel/variableMatch',
        method: 'get',
        params: query
    })
}


// 获取模板的所有树节点
export function getComponentNodes(query) {
    return request({
        url: '/model3d/configmodel/getComponentNodes/',
        method: 'get',
        params: query
    })
}

export function getAllInstanceTreeNodes(query, options = {}) {
    const { proId, ...params } = query
    return request({
        url: `/model3d/configmodel/getTreeNodes/${proId}`,
        method: 'get',
        params,
        silentError: options.silentError
    })
}

// 根据节点编码查询子节点
export function reqSonNodes(query) {
    return request({
        url: '/model3d/configmodel/getBomSons',
        method: 'get',
        params: query
    })
}

export function reqAllInstanceSonNodes(query) {
    return request({
        url: '/model3d/configmodel/getSonNodes',
        method: 'get',
        params: query
    })
}

export function generateTasks(obj) {
    return request({
        url: '/model3d/configPerceivedTask',
        method: 'post',
        data: obj
    })
}

export function generateCompositionTasks(obj) {
    return request({
        url: '/model3d/configPerceivedTask/saveComposition',
        method: 'post',
        data: obj
    })
}

export function fetchTaskList(query, options = {}) {
    return request({
        url: '/model3d/configPerceivedTask/getModelByNodeId',
        method: 'get',
        params: query,
        silentError: options.silentError
    })
}

export function fetchCompositionTaskList(query) {
    return request({
        url: '/model3d/configPerceivedTask/getCompositionTask',
        method: 'get',
        params: query
    })
}

export function varPageByTaskId(query) {
    return request({
        url: '/model3d/configPerceivedTaskVariable/getVariableByTaskId',
        method: 'get',
        params: query
    })
}

//  根据项目的节点id查询关联的感知变量
export function getVarsByTaskId(query) {
    return request({
        url: '/model3d/configPerceivedTaskVariable/getVarsByTaskId',
        method: 'get',
        params: query
    })
}

export function delTask(taskId) {
    return request({
        url: '/model3d/configPerceivedTask/' + taskId,
        method: 'delete'
    })
}

export function reqEditTask(modelConfig, varList) {
    return request({
        url: '/model3d/configPerceivedTask/editTaskByTaskId',
        method: 'put',
        data: [modelConfig, varList]
    })
}

export function saveTaskConfiguration(taskConfig, variables) {
    return request({
        url: '/model3d/configPerceivedTask/saveConfiguration',
        method: 'put',
        data: {
            taskId: taskConfig.taskId,
            nodeId: taskConfig.nodeId,
            modelId: taskConfig.modelId,
            modelName: taskConfig.modelName,
            algoId: taskConfig.algoId,
            dataDimension: taskConfig.dataDimension,
            variables
        }
    })
}

export function getTask(query) {
    return request({
        url: '/model3d/configPerceivedTask/getModelInfoByTaskId',
        method: 'get',
        params: query
    })
}

export function createTemplate(query) {
    return request({
        url: '/model3d/configPerceivedTask/createTemplate',
        method: 'post',
        params: query
    })
}

export function createCompositionTemplate(query) {
    return request({
        url: '/model3d/configPerceivedTask/createCompositionTemplate',
        method: 'post',
        params: query
    })
}

export function operateTask(query) {
    return request({
        url: '/model3d/configPerceivedTask/operateTask',
        method: 'post',
        params: query,
        timeout: 6000
    })
}

export function stopTask(query) {
    return request({
        url: '/model3d/configPerceivedTask/stopTask',
        method: 'put',
        params: query,
        timeout: 6000
    })
}

