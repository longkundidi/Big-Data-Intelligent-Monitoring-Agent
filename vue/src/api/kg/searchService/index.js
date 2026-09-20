import request from '@/router/axios'
// import {get} from "../../../util/http/request";

const baseUrl = '/kg/searchService';

export function reqAddRootNode (obj) {
    return request({
        url: '/model3d/configgbomtree/addRootNode',
        method: 'post',
        data: obj
    })
}// 添加根节点

export function reqTreeNodes (nodeLevel) {
    return request({
        url: '/model3d/configgbomtree/getTreeNodes/'+ nodeLevel,
        method: 'get'
    })
}// 获取模板的所有树节点

export function reqSonNodes (nodeCode) {
    return request({
        url: '/model3d/configgbomtree/getSonNodes/' + nodeCode,
        method: 'get',
    })
}// 根据节点编码查询子节点

export function getGraphByStruct ( ontologyName, curStructId, taskId ){
    let obj= {
        ontologyName: ontologyName,
        curStructId: curStructId,
        taskId: taskId,
    }
    return request({
        url: `${baseUrl}/getGraphByStruct`,
        method: 'post',
        data: obj
    })
}// 获取与该结构节点相关的数据

export function findNeoStructId (ontologyName,nodeId){
    let params={
        ontologyName,
        nodeId,
    }
    return request({
        url: `${baseUrl}/findNeoStructId`,
        method: 'get',
        params: params,
    })
}// 查找当前结构id号

export function findAllFile (){
    return request({
        url: '/kg/kgOwlAttributeDict/findAllFile',
        method: 'get',
    })
}// 查找所有文件类型

export function getTableInfo (obj){
    return request({
        url: `${baseUrl}/tableInfo`,
        method: 'post',
        data: obj
    })
}// 获取表格信息

export function findQueryDataTables (ontologyName, queryType,queryData) {
    let params={
        ontologyName,
        queryType,
        queryData
    }
    return request({
        url: `${baseUrl}/findQueryDataTables`,
        method: 'get',
        params: params,
    })
}// 查询数据

export function getDifferentDomains (ontologyName) {
    let params={
        ontologyName,
    }
    return request({
        url: `${baseUrl}/getDifferentDomains`,
        method: 'get',
        params: params,
    })
}// 获取与结构直接相连的数据
export const getGraphNodeInfo=function (id,kgtype,structType){
    let params={
        id,
        kgtype,
        structType
    }
    return request({
        url: `${baseUrl}/getGraphNodeInfo`,
        method: 'get',
        params: params,
    })
}
