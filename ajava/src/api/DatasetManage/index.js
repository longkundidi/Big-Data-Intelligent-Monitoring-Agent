import request from '@/router/axios'

const baseUrl = '/al';


export function fetchList() {
    return request({
        url: `${baseUrl}/alresumedata/getAllScene`,
        method: 'get',
    })
}



export function addProductModel(id, productModelName) {
    return request({
        url: `${baseUrl}/alresumedata/addProductModel`,
        method: 'get',
        params: {id, productModelName}
    })
}




export function buildSceneModelTree() {
    return request({
        url: `${baseUrl}/alresumedata/buildSceneModelTree`,
        method: 'get',
    })
}




