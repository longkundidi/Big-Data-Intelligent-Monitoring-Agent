import request from '@/router/axios'

/**
 * 根据nodeName和turbineCode联合查询BOM树设备详情
 * @param {String} nodeName - 节点名称（风机名称）
 * @param {String} turbineCode - 风机编码
 * @returns {Promise}
 */
export function getConfigBomTreeByNodeNameAndTurbineCode(nodeName, turbineCode) {
    return request({
        url: '/model3d/configbomtree/byNodeNameAndTurbineCode',
        method: 'get',
        params: {
            nodeName,
            turbineCode
        }
    })
}

/**
 * 根据nodeName和proId查询BOM树设备详情
 * @param {String} nodeName - 节点名称（风机名称）
 * @param {String} proId - 项目ID
 * @returns {Promise}
 */
export function getConfigBomTreeByNodeNameAndProId(nodeName, proId) {
    return request({
        url: '/model3d/configbomtree/byNodeNameAndProId',
        method: 'get',
        params: {
            nodeName,
            proId
        }
    })
}

/**
 * 根据turbineCode查询BOM树设备详情
 * @param {String} turbineCode - 风机编码
 * @returns {Promise}
 */
export function getConfigBomTreeByTurbineCode(turbineCode) {
    return request({
        url: `/model3d/configbomtree/byTurbineCode/${turbineCode}`,
        method: 'get'
    })
}

/**
 * 根据nodeId查询BOM树设备详情
 * @param {String} nodeId - 节点ID
 * @returns {Promise}
 */
export function getConfigBomTreeById(nodeId) {
    return request({
        url: `/model3d/configbomtree/${nodeId}`,
        method: 'get'
    })
}

/**
 * 分页查询BOM树
 * @param {Page} page - 分页对象
 * @param {ConfigBomTree} configBomTree - 查询条件
 * @returns {Promise}
 */
export function getConfigBomTreePage(page, configBomTree) {
    return request({
        url: '/model3d/configbomtree/page',
        method: 'get',
        params: {
            ...page,
            ...configBomTree
        }
    })
}

/**
 * 创建BOM树
 * @param {ConfigBomTree} configBomTree - BOM树对象
 * @returns {Promise}
 */
export function createConfigBomTree(configBomTree) {
    return request({
        url: '/model3d/configbomtree',
        method: 'post',
        data: configBomTree
    })
}

/**
 * 更新BOM树
 * @param {ConfigBomTree} configBomTree - BOM树对象
 * @returns {Promise}
 */
export function updateConfigBomTree(configBomTree) {
    return request({
        url: '/model3d/configbomtree',
        method: 'put',
        data: configBomTree
    })
}

/**
 * 删除BOM树
 * @param {String} nodeId - 节点ID
 * @returns {Promise}
 */
export function deleteConfigBomTree(nodeId) {
    return request({
        url: `/model3d/configbomtree/${nodeId}`,
        method: 'delete'
    })
}
