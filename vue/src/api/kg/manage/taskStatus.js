import request from '@/router/axios';

const baseUrl= "/kg/taskStatus"

export function createTask (){
    return request({
        url: `${baseUrl}/createTask/`,
        method: 'get',
    })
}

export function checkTaskStatus (taskId){
    return request({
        url: `${baseUrl}/checkTaskStatus/` + taskId,
        method: 'get',
    })
}


