import request from "@/router/axios"
import Qs from "qs";


export function fetchStatusCount(query) {
    return request({
        url: `/al/AuditsManagement/getCount`,
        method: 'get',
        params: query
    })
}

export function getComponentCount() {
    return request({
        url: `/model3d/configPerceivedTask/getComponentCount`,
        method: 'get'
    })
}

export function getVariableCount() {
    return request({
        url: `/model3d/configperceivedvariable/getVariebleCount`,
        method: 'get'
    })
}

export function getFaultModeCount() {
    return request({
        url: `/model3d/configfailuremode/getFaultModeCount`,
        method: 'get'
    })
}

export function getJobsCount() {
    return request({
        url: `/al/flinkStatus/getJobsCount`,
        method: 'get'
    })
}

export function getStateOfTree(query) {
    return request({
        url: `/model3d/configmodel/getStateOfTree`,
        method: 'get',
        params: query

    })
}

export function getAlgorithmDashboardCounts() {
    return request({
        url: `/al/AuditsManagement/dashboardCounts`,
        method: 'get'
    })
}

export function getStateOfTreeByFarm(query, options = {}) {
    return request({
        url: `/model3d/configmodel/getStateOfTreeByFarm`,
        method: 'get',
        params: query,
        silentError: options.silentError
    })
}

export function getAccessStateOfTreeByFarm(query, options = {}) {
    return request({
        url: `/model3d/configmodel/getAccessStateOfTreeByFarm`,
        method: 'get',
        params: query,
        silentError: options.silentError
    })
}

export function searchNode(query) {
    return request({
        url: `/model3d/configmodel/searchNode`,
        method: 'get',
        params: query

    })
}

export function getOverviewOfFarms(query) {
    return request({
        url: `/model3d/configmodel/getOverviewOfFarms`,
        method: 'get',
        params: query
    })
}

export function getAlarmList(query) {
    return request({
        url: '/model3d/configalarminfo/page',
        method: 'post',
        params: query
    })
}

export function getProjectId(query, options = {}) {
    return request({
        url: `/al/alresumedata/getProjectId`,
        method: 'get',
        params: query,
        silentError: options.silentError
    })
}
