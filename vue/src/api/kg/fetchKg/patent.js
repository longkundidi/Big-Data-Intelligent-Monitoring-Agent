import request from '@/router/axios'

const baseUrl = '/kg/cnkikeyword'
export function getKeywordsTableData(curPage = 1, size = 10, key = ''){
    let query = {
        curPage:curPage,
        size:size,
        key:key
    }
    return request({
        url: `${baseUrl}/table`,
        method: 'get',
        params: query
    })
}

export function deleteById(id){
    return request({
        url: `${baseUrl}/` + id,
        method: 'delete'
    })
}
export function save(obj) {
    return request({
        url: `${baseUrl}/`,
        method: 'post',
        data: obj
    })
}
export function updateById(id, obj) {
    obj.id = id;
    return request({
        url: `${baseUrl}/`,
        method: 'put',
        data: obj
    })
}


