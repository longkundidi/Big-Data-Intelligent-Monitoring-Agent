import request from '@/router/axios'

const baseUrl = "kg/scrapyKg/"
export function getTableData(curPage = 1, size = 10,type,source,struct) {
    let query = {
        curPage:curPage,
        size:size,
        type:type,
        source:source,
        struct:struct
    };
    return request({
        url:`${baseUrl}/table`,
        method:'get',
        params: query
    })
}

