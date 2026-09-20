import request from "@/router/axios"
import Qs from 'qs'

/**
 * 查询任务列表
 * @param {*} pageNum
 * @param {*} pageSize
 * @param {任务名称} jobName
 * @param {Flink运行任务编号} jobId
 * @param {任务类型} jobType
 * @param {任务状态} status
 * @param {开启状态} open
 * @returns
 */
export function getTasks(pageNum, pageSize, jobName, jobId, jobType, status, open) {
    return request({
        url: '/al/listTask',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            pageNum: pageNum,
            pageSize: pageSize,
            jobName: jobName,
            jobId: jobId,
            jobType: jobType,
            status: status,
            open: open
        }
    })
}
export function getAlAlgorithms(query){
    return request({
        url:'/al/flinkStatus/page',
        method:'get',
        params:query
    })
}

export function getElevatorOperationsOverview() {
    return request({
        url: '/al/flinkStatus/operations',
        method: 'get'
    })
}



/**
 * 启动任务
 * @param {任务编号} id
 * @returns
 */
export function startTask(id) {
    return request({
        url: '/flink/start',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            id: id
        }
    })
}

/**
 * 停止任务
 * @param {{taskId: (string|[{trigger: string, message: string, required: boolean}]|[{trigger: string, message: string, required: boolean}]|*)}} id
 * @param {备份编号} savepointId
 * @returns
 */
export function stopTask(id) {
    return request({
        url: '/flink/stop',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            id: id
        }
    })
}

/**
 * 从savepoint备份地址中启动任务
 * @param {任务编号} id
 * @param {备份编号} savepointId
 * @returns
 */
export function startSavepoint(id, savepointId) {
    return request({
        url: '/flink/start',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            id: id,
            savepointId: savepointId
        }
    })
}



/**
 * 复制任务
 * @param {任务编号} id
 * @returns
 */
export function copyConfig(id) {
    return request({
        url: '/flink/copyConfig',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            id: id
        }
    })
}

/**
 * 删除任务
 * @param {任务编号} id
 * @returns
 */
export function deleteTask(id) {
    return request({
        url: '/flink/delete',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            id: id
        }
    })
}

/**
 * 预校验SQL
 * @returns
 * @param flinkSql
 */
export function checkfSql(flinkSql) {
    return request({
        url: '/flink/checkfSql',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {
            flinkSql: flinkSql
        }
    })
}

/**
 * 新增任务
 * @param {任务对象} data
 * jobName: my_online
 * jobDesc: 我的测试任务
 * deployMode: LOCAL
 * alRunConfig:
 * alCheckpointConfig:  -checkpointInterval 300000 -checkpointDir file:///home/al/al-algorithm-platform-web/savepoint
 * alSql: --
 * jobType: 0
 * alarmTypes:
 * extJarPath:
 * customArgs:
 * customMainClass: com.xxxy.Demo
 * customJarUrl: http://test.coahuae.com/xxx.jar
 * @returns
 */
export function addConfig(data) {
    return request({
        url: '/flink/addConfig',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: data
    })
}

/**
 * 修改任务
 * @param {任务对象} data
 * id: 1
 * jobName: my_online
 * jobDesc: 我的测试任务
 * deployMode: LOCAL
 * alRunConfig:
 * alCheckpointConfig:  -checkpointInterval 300000 -checkpointDir file:///home/al/al-algorithm-platform-web/savepoint
 * alSql: --
 * jobType: 0
 * alarmTypes:
 * extJarPath:
 * customArgs:
 * customMainClass: com.xxxy.Demo
 * customJarUrl: http://test.coahuae.com/xxx.jar
 * @returns
 */
export function editConfig(data) {
    return request({
        url: '/flink/editConfig',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: data
    })
}

/**
 * 查询历史版本列表
 * @param {*} data (jobConfigId,jobName)
 * @returns
 */
export function getTaskHistory(data) {
    return request({
        url: '/flink/jobConfigHistoryPage',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: data
    })
}

/**
 * 查询历史版本详情
 * @param {*} id
 * @returns
 */
export function getTaskHistoryDetail(id) {
    return request({
        url: '/flink/jobConfigHistoryDetail',
        method: 'post',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: [function (data) {
            return Qs.stringify(data)
        }],
        data: {id: id}
    })
}

export function  getJobsCount(){
    return request({
        url:'/flink/flinkStatus/getJobsCount',
        method:'get'
    })
}
