import request from '@/router/axios'

/**
 * 根据设备编码和场景ID获取设备基本信息
 * @param {Object} params - { 
 *   deviceCode: 设备编码(product_model), 
 *   sceneId: 场景ID（可选，优先使用），
 *   usageUnit: 使用单位（备用参数）
 * }
 * axios.baseURL(/api) + url(/al) + url(/devicebaseinfo/...) = /api/al/devicebaseinfo/...
 * 代理重写: /api/al → /lk/api
 * 网关转发: /lk/api/devicebaseinfo/... → 后端服务 /api/devicebaseinfo/...
 */
export function getDeviceBaseInfo(params) {
  const { deviceCode, sceneId, usageUnit } = params
  
  // 优先使用 sceneId + deviceCode 方式查询（更精确）
  if (sceneId) {
    console.log('[getDeviceBaseInfo] 使用 sceneId 方式查询: deviceCode=' + deviceCode + ', sceneId=' + sceneId)
    return request({
      url: `/al/devicebaseinfo/deviceCode/${deviceCode}/scene/${sceneId}`,
      method: 'get'
    })
  }
  
  // 备用方案：使用 usageUnit + deviceCode 查询
  console.log('[getDeviceBaseInfo] 使用 usageUnit 方式查询: deviceCode=' + deviceCode + ', usageUnit=' + usageUnit)
  return request({
    url: `/al/devicebaseinfo/usageUnit/${usageUnit}/deviceCode/${deviceCode}`,
    method: 'get'
  })
}

export function saveDeviceBaseInfo(data) {
  return request({
    url: '/al/devicebaseinfo/save',
    method: 'post',
    data
  })
}


/**
 * 获取风机设备基本信息（旧接口，保留兼容）
 * @param {Object} params - { turbineName: 风机名称 }
 */
export function getDeviceInfo(params) {
  return request({
    url: '/sw/turbine/getDeviceInfo',
    method: 'get',
    params
  })
}

/**
 * 获取风机报警历史记录
 * @param {Object} params - { turbineName: 风机名称, pageNum: 页码, pageSize: 每页数量 }
 */
export function getAlarmHistory(params) {
  return request({
    url: '/sw/turbine/getAlarmHistory',
    method: 'get',
    params
  })
}

/**
 * 获取风机故障统计数据
 * @param {Object} params - { turbineName: 风机名称, startTime: 开始时间, endTime: 结束时间 }
 */
export function getFaultStats(params) {
  return request({
    url: '/sw/turbine/getFaultStats',
    method: 'get',
    params
  })
}

/**
 * 获取风机边缘端信息统计
 * @param {Object} params - { turbineName: 风机名称 }
 */
export function getEdgeInfo(params) {
  return request({
    url: '/sw/turbine/getEdgeInfo',
    method: 'get',
    params
  })
}

/**
 * 获取风机特征趋势数据
 * @param {Object} params - { turbineName: 风机名称, featureType: 特征类型, timeRange: 时间范围 }
 */
export function getTrendData(params) {
  return request({
    url: '/sw/turbine/getTrendData',
    method: 'get',
    params
  })
}

/**
 * 获取风机各部件传感器测点数据
 * @param {Object} params - { turbineName: 风机名称 }
 */
export function getSensorPoints(params) {
  return request({
    url: '/sw/turbine/getSensorPoints',
    method: 'get',
    params
  })
}

/**
 * 获取风机报警/故障次数分析数据
 * @param {Object} params - { turbineName: 风机名称, type: 'alarm' | 'fault' }
 */
export function getAlarmFaultAnalysis(params) {
  return request({
    url: '/sw/turbine/getAlarmFaultAnalysis',
    method: 'get',
    params
  })
}
