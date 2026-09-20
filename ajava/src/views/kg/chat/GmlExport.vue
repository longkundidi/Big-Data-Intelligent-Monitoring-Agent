<template>
  <basic-container>
    <el-container >
      <el-header class="top-page">
        <span class="title">  知识服务->大模型语料导出</span>
        <div style="margin-right:10px;float: right;">
          <el-form :inline="true" size="small">
            <el-form-item>
              <el-button   icon="el-icon-download"

                           size="large"
                           @click="exportToExcel(allData,'大模型语料库')">
                导出语料
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-header>
      <el-main :style="{background: 'white',height:'99%'}">
        <div id="meta-dict" style="width: 100%;margin-top: 15px">
          <el-main>
            <el-table :data="tableData" border style="width: 100%"  :header-cell-class-name="getRowClass"
                      :cell-class-name="cellStyle" :row-style="{ height:'50px'} ">
              <el-table-column max-width="50px" label="问题" prop="question"></el-table-column>
              <el-table-column min-width="200px" label="答案" prop="answer"></el-table-column>
              <el-table-column max-width="40px" label="时间" prop="time"></el-table-column>
            </el-table>
            <el-pagination
                background
                small
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                v-model:currentPage="curPage"
                :page-sizes="[5, 10, 20]"
                :page-size="size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
            >
            </el-pagination>
          </el-main>
        </div>
      </el-main>
    </el-container>
  </basic-container>
</template>
<script setup>
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import {onMounted, reactive, ref} from "vue";
import {getGMLData,changeGmlflag,getAlLGMLData,delgml} from "../../../api/kg/chat/chat";
let allData=ref([]);
let curPage=ref(1);
let size=ref(5);
let tableData=ref([]);
let total = ref(0);
let id= ref(0);
let flag= ref(0);
// 表示是否筛选，0表示展示全部,1表示展示已通过，2表示展示未通过，3表示展示待审核
let passflag = ref(1);
const afterCommit = function (){
  renderTable();
}
//页面初始化
onMounted(()=>{
  renderTable()
})
//获取通过的数据
const renderTable=function (){
  getGMLData(passflag, curPage.value,size.value).then(r=>{
    tableData.value=r.data.data.records;
    total.value = r.data.data.total;
    curPage.value= r.data.data.current;
    size.value=r.data.data.size;
    allData.value=r.data.data.records;
  })
}
//变换页面大小
const handleSizeChange = function (num) {
  size.value=num
  renderTable();
};
//跳转到第几个页面
const handleCurrentChange = function (pageNum) {
  curPage.value = pageNum;
  renderTable();
};

const getRowClass=function (row, rowIndex) {
  return "theadStyle";
}
// 绑定样式属性
const cellStyle=function (row, rowIndex) {
  return "tdStyle";
}
const exportToExcel=function (tableData, fileName) {
  // console.log("111",tableData)
  // 将数据转换为所需格式
  const data = tableData.map(row => ({
    question: row.question,
    answer: row.answer,
    time: row.time,
  }));
  const worksheet = XLSX.utils.json_to_sheet(data, {
    header:['question', 'answer', 'time']
  });
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
  const excelBuffer = XLSX.write(workbook, {
    bookType: 'xlsx',
    type: 'array',
  });
  const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  saveAs(blob, `${fileName}.xlsx`);
}

</script>

<style lang="scss" scoped>
.el-pagination {
  margin-top: 30px;
}
.title {
  font-size: 20px;
  font-weight: bold;
}

</style>
