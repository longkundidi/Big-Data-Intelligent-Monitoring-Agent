<template>
  <basic-container>
  <el-container >
    <el-header class="top-page">
      <span class="title">知识服务->大模型语料审核</span>
      <div style="margin-left:50px; float: right;">
        <el-form :inline="true">
          <el-form-item style="margin-right: 0">
            <span>审核状态选择：</span>
          </el-form-item>
          <el-form-item>
            <el-select v-model="curKgLabel" :popper-append-to-body="false" placeholder="请选择状态" style="width: 100px;">
              <el-option v-for="item in allStates" :key="item" :label="item" :value="item" @click="changeTypes(item)">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item style="margin-right: 0">
            <el-button type="normal" icon="el-icon-download" @click="exportToExcel(allData,'大模型语料库')">
              语料导出
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <span class="annotation">*点击语料导出按钮可导出审核通过的语料!</span>
    </el-header>
    <el-main :style="{background: 'white',height:'99%'}">
      <div id="meta-dict" style="width: 100%;margin-top: 15px">
          <el-main>
            <el-table :data="tableData" border style="width: 100%"  :header-cell-class-name="getRowClass"
                      :cell-class-name="cellStyle" :row-style="{ height:'50px'} ">
              <el-table-column max-width="50px" label="问题" prop="question"></el-table-column>
              <el-table-column min-width="200px" label="答案" prop="answer"></el-table-column>
              <el-table-column max-width="40px" label="时间" prop="time"></el-table-column>
              <el-table-column max-width="30px" align="center" label="审核状态">
                <template #default="scope">
                  <div v-if="scope.row.flag===1">已通过</div>
                  <div v-else-if="scope.row.flag===2">未通过</div>
                  <div v-else>待审核</div>
                </template>
              </el-table-column>
              <el-table-column align="center" label="操作">
                <template #default="scope">
                  <el-button v-if="scope.row.flag !== 1"
                             size="small"
                             icon="el-icon-circle-plus-outline"

                             @click="changeGml(scope.row.id,1)">通过
                  </el-button>
                  <el-button   v-if="scope.row.flag !== 2"
                               icon="el-icon-edit"

                               size="small"
                               @click="changeGml(scope.row.id,2)">不通过
                  </el-button>
                  <el-button   v-if="scope.row.flag !== 3"
                               icon="el-icon-delete"

                               size="small"
                               @click="delgmldata(scope.row.id)">删除
                  </el-button>
                </template>
              </el-table-column>
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
import {onMounted,ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {getGMLData, changeGmlflag, getAlLGMLData, delgml,} from "../../../api/kg/chat/chat";
import * as XLSX from "xlsx";
import { saveAs } from 'file-saver';
let allData=ref([]);
//审核状态定义
let allStates=ref(["全部","待审核","已通过","未通过"]);
//分页参数定义
let curPage=ref(1);
let size=ref(5);
let tableData=ref([]);
let total = ref(0);
let id= ref(0);
let passflag = ref(1);
//语料导出
const renderTable1=function (){
  getGMLData(passflag.value, curPage.value,size.value).then(r=>{
    // tableData.value=r.data.data.records;
    // total.value = r.data.data.total;
    // curPage.value= r.data.data.current;
    // size.value=r.data.data.size;
    allData.value=r.data.data.records;
  })
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
// 表示是否筛选，0表示展示全部,1表示展示已通过，2表示展示未通过，3表示展示待审核
let allflag = ref(0);
let curKgLabel = ref("")

//页面初始化
onMounted(()=>{
  curKgLabel.value="全部"

  //获取已经通过的数据用于导出
  renderTable1()
  allflag=0;
  //获取所有的数据用于显示
  renderTable()
})
//删除语料
const delgmldata=function (id)
{
  delgml(id)
  renderTable()
}
//切换不同审核状态
async function changeTypes(item){
  console.log("选择的方式是",item)
  if(item==="待审核")
  {curKgLabel.value="待审核"
    allflag=3;
  }
  if(item==="全部")
  {curKgLabel.value="全部"
    allflag=0;
  }
  if(item==="已通过")
  {curKgLabel.value="已通过"
    allflag=1;
  }
  if(item==="未通过")
  {curKgLabel.value="未通过"
    allflag=2;
  }
   await renderTable()
   await renderTable1()
}
//获取后端数据
const renderTable = async () => {
  try {
    const response = await getGMLData(allflag, curPage.value, size.value);
    const data = response.data.data;
    tableData.value = data.records;
    total.value = data.total;
    curPage.value = data.current;
    size.value = data.size;
  } catch (error) {
    console.error('Failed to fetch data:', error);
    // 可以在这里添加用户友好的错误处理逻辑，例如显示错误消息
  }
}

const handleSizeChange = function (num) {
  size.value=num
  renderTable();
};
const handleCurrentChange = function (pageNum) {
  curPage.value = pageNum;
  renderTable();
};

// 删除
const del = function (id) {
  ElMessageBox.confirm("是否删除该基准词?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteById(id)
        .then(() => {
          renderTable();
          ElMessage({
            type: "success",
            message: "删除成功!",
          });
        })
        .catch(() => {
          ElMessage({
            type: "info",
            message: "删除失败",
          });
        });
  }).catch(() => {
        ElMessage({
          type: "info",
          message: "已取消删除",
        });
      });
};

const getRowClass=function (row, rowIndex) {
  return "theadStyle";
}
// 绑定样式属性
const cellStyle=function (row, rowIndex) {
  return "tdStyle";
}
const changeGml=function (id,flag)
{
  changeGmlflag(id,flag).then(r=>{
    renderTable()
    //重新获取满足通过条件的数据
    renderTable1()
  })

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
.annotation {
  display: block; /* Makes the span take a full line */
  color: red; /* Sets the text color to red */
  font-size: 14px; /* Smaller font size */
  margin-top: 5px; /* Spacing above the annotation */
}
</style>
