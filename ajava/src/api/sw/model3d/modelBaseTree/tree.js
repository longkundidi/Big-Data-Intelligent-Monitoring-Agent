import request from '@/router/axios'

export function putObj (obj) {
  return request({
    url: '/model3d/m3modelbasetree',
    method: 'put',
    data: obj
  })
}

//  根据模板Id查询根节点
export function reqRootNode (mbId) {
  return request({
    url: '/model3d/m3modelbasetree/getRootNodeByMbId/' + mbId,
    method: 'get'
  })
}

// 查询所有根节点(节点类型为Root或Root-Leaf)
export function reqRootNodes() {
  return request({
    url: '/model3d/m3modelbasetree/getRootNodes',
    method: 'get',
  })
}

//  根据节点编码查询子节点
export function reqSonNodes (nodeCode) {
  return request({
    url: '/model3d/m3modelbasetree/getSonNodes/' + nodeCode,
    method: 'get',
  })
}

export function reqObjById (nodeId) {
  return request({
    url: '/model3d/m3modelbasetree/' + nodeId,
    method: 'get',
  })
}

//  添加根节点
export function reqAddRootNode (nodeName, mbId) {
  return request({
    url: '/model3d/m3modelbasetree/addRootNode',
    method: 'post',
    params: {
      nodeName,
      mbId
    }
  })
}

//  添加子节点
export function reqAddSonNode (parentNodeId, node) {
  return request({
    url: '/model3d/m3modelbasetree/addSonNode/' + parentNodeId,
    method: 'post',
    data: node
  })
}

//  根据当前节点编码，获取父节点Id
export function reqParentNodeId (nodeCode) {
  return request({
    url: '/model3d/m3modelbasetree/getParentNodeId/' + nodeCode,
    method: 'get',
  })
}

//  删除当前节点及其所有子节点，包括模型和图片
export function reqDeleteNodes (nodeCode) {
  return request({
    url: '/model3d/m3modelbasetree/deleteNodes/' + nodeCode,
    method: 'get',
  })
}

/*export function getNodeByCode (nodeCode) {
  return request({
    url: '/oper/mometatree/getNode/' + nodeCode,
    method: 'get',
  })
}*/

export function reqDeleteModel (modelUrl,imgUrl) {
  return request({
    url: '/model3d/minio/removeObject',         //  minio服务器模型、图片删除
    method: 'get',
    params: {
      modelUrl,
      imgUrl
    }
  })
}


