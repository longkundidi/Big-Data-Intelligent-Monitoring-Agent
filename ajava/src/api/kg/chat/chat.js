import request from "@/router/axios"
// import * as XLSX from 'xlsx';
// import { saveAs } from 'file-saver';
// const baseUrl = 'ontologyqa';
const baseUrl="eq"
export function chat(param){
    return request({
        url: `/kg/eq/chat`,
        method: 'post',
        data:param
    })
}
export function getAlLGMLData(allflag,curPage,size)
{
    let param = {
        allflag:allflag,
    }
    return request({
        url: `/kg/eq/getAllGMLData`,
        method: 'get',
        params:param
    })
}
export function getGMLData(allflag,curPage = 1, size = 10)
{
    let param = {
        allflag:allflag,
        curPage:curPage,
        size:size
};
    return request({
        url: `/kg/eq/getGMLData`,
        method: 'get',
        params:param
})
}
export function changeGmlflag(id,flag)
{
    let param = {
       id: id,
       flag:flag
    }
    return request({
        url: `/kg/eq/changeGmlflag`,
        method: 'get',
        params:param
    })
}
export const delgml=function (id)
{
    let param={
        id:id
    }
    return request({
        url: `/kg/eq/deletegml`,
        method: 'get',
        params:param
    })
}
export const sengmllogs=function (question,answer)
{
    let param = {
        question,
        answer,
    }
    return request({
        url: `/kg/eq/Gmllogs`,
        method: 'get',
        params:param
    })

}
// export const exportToExcel=function (tableData, fileName) {
//     console.log("111",tableData)
//     // 将数据转换为所需格式
//     const data = tableData.map(row => ({
//         question: row.question,
//         answer: row.answer,
//         time: row.time,
//     }));
//     const worksheet = XLSX.utils.json_to_sheet(data, {
//         header:['question', 'answer', 'time']
//     });
//     const workbook = XLSX.utils.book_new();
//     XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
//     const excelBuffer = XLSX.write(workbook, {
//         bookType: 'xlsx',
//         type: 'array',
//     });
//     const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
//     saveAs(blob, `${fileName}.xlsx`);
// }
