<template>
  <div class="tablecon">
    <div class="subtable">
      <div class="title">故障履历数据</div>
      <avue-crud ref="crudRef" class="h-avue-crud"
                 v-model="form"
                 :option="option"
                 @on-load="getList"
                 @size-change="sizeChange"
                 @current-change="currentChange"
                 v-model:page="page"
                 :data="data">
        <template #menu-left="{}">
          <el-button type="primary"
                     class="addBtn"
                     @click="addFault">新增
          </el-button>
        </template>
      </avue-crud>
    </div>
  </div>
  <el-dialog
      v-model="dlgFault"
      :title="titleName"
      width="35%"
      @close="cancel()"
      draggable>
    <div style="margin-top: 20px;text-align: center">
      <span style="font-size: 15px;font-weight: bold;">故障名称：</span>
      <el-select v-model="valSelectedFault" placeholder="请选择故障名称" style="width: 500px;">
        <el-option v-for="item in faultList" :key="item.failureId" :label="item.failureName" :value="item.id"
                   @click="valSelectedFault = item.failureName;" ></el-option>
      </el-select>
    </div>
    <div style="margin-top: 10px;text-align: center">
      <span style="font-size: 15px;font-weight: bold;">故障时间：</span>
      <el-date-picker
          v-model="time"
          type="datetimerange"
          :shortcuts="shortcuts"
          range-separator="To"
          start-placeholder="故障开始时间"
          end-placeholder="故障结束时间"
          style="width: 480px;"
      />
    </div>
    <template #footer>
      <div style="text-align: center;">
        <el-button class="disMissBtn" @click="cancel()">取 消</el-button>
        <el-button class="addBtn" type="primary" @click="submit()">保 存</el-button>
      </div>
    </template>
  </el-dialog>
</template>
<script setup>
import { ref, defineExpose } from 'vue'
import { tableOption } from '@/const/crud/diagnosis/faultHistory/table'
import { ElMessageBox, ElMessage } from 'element-plus'
import {addFaultData, fetchList, getFaultMode} from '@/api/diagnosis/faultHistory/faultHistory.js'
import {dateFormat} from "@/util/date";


let globeParams = {}     //  声明一个全局参数对象

const crudRef = ref(null)
let option = ref(tableOption)
let data = ref([])
let form = ref({})
let page = ref({
  total: 0,          // 总页数
  currentPage: 1,     // 当前页数
  pageSize: 10        // 每页显示多少条
})
let titleName = ref('')
let dlgFault = ref(false)
let valSelectedFault = ref('')
let faultList = ref([])
let time = ref('')
let shortcuts = ref([
  {
    text: 'Last week',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: 'Last month',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
])

//  修改每页条数
function sizeChange(pageSize){
  page.value.pageSize = pageSize
}

//  翻页
function currentChange(current){
  page.value.currentPage = current
}

//  分页或者修改每页条数时，自动调用
async function getList(page, params) {
  if (globeParams.curNode !== undefined){
    let res = await fetchList({nodeId: globeParams.curNode.id, pageSize: page.pageSize, currentPage: page.currentPage})
    if (res.data.code === 0 && res.data.data.length !== 0){
      data.value = res.data.data
      page.value.total = res.data.data.length
  }
  }
}

//  新增
async function addFault(){
  titleName.value = '新增故障履历数据'
  if(globeParams.curNode === undefined){
    ElMessage.warning("请选择需要增加故障履历数据的节点！")
  }else {
    let res = await getFaultMode({nodeName: globeParams.curNode.name})
    if (res.data.code === 0){
      faultList.value = res.data.data
    }
    dlgFault.value = true
  }
}

async function cancel() {
  dlgFault.value = false
  valSelectedFault.value = ''
  time.value = ''
}

async function submit() {

  let res = await addFaultData({
    nodeId: globeParams.curNode.id,
    component: globeParams.curNode.name,
    faultName: valSelectedFault.value,
    startTime: dateFormat(time.value[0]),
    endTime: dateFormat(time.value[1])
  })
  if (res.data.code === 0) {
    ElMessage.success("新增成功！")
    await _refreshTableByNodeId(globeParams.curNode.id)
  }else {
    ElMessage.success("新增失败！")
  }
  dlgFault.value = false
  valSelectedFault.value = ''
  time.value = ''
}

//  根据节点Id，刷新表格数据
async function _refreshTableByNodeId(nodeId) {
  let res = await fetchList({nodeId: nodeId, pageSize: page.value.pageSize, currentPage: page.value.currentPage})
  if (res.data.code === 0 && res.data.data.length !== 0){
    data.value = res.data.data
    page.value.total = res.data.data.length
  }
}

/*  外部调用接口函数定义
    根据节点id，查询节点关联的故障模式记录
 */
const getFaultInfos = async (node) => {
  globeParams.curNode = node                  //  缓存当前节点
  await _refreshTableByNodeId(node.id)
}

defineExpose({ getFaultInfos })            //  暴露组件接口



</script>
<style lang="scss" scoped>
@import '@/styles/my-avue-crud.scss';
@import '@/styles/button/resource.scss';
.tablecon {
  height: 100%;
  width: 100%;
  display: flex;

  .subtable {
    height: 100%;
    width: 100%;

    .title {
      height: 50px;
      width: 100%;
      background-color: cadetblue;
      display: flex;
      color: white;
      letter-spacing: 2px; /* 设置字间距为 2 像素 */
      justify-content: center; /* 水平居中 */
      align-items: center; /* 垂直居中 */
    }
  }
}

.el-dialog__header{     /*   标题栏背景色   */
  padding: 10px;
  margin-right: -1px;    /*    保持窗体与标题栏宽度一致    */
  background-image: linear-gradient(to bottom,#1e90ff 0, #00ffff 100%);     /*    修改标题色   */
}
.el-dialog__title{            /*  标题栏字体样式   */
  font-size: 16px;
  font-weight: bold;
}
.el-dialog__headerbtn{        /*  关闭按钮样式   */
  top: 17px;
  right: 10px;
  width: 45px;
  height: 45px;
  /*background-image: linear-gradient(to bottom,#1e90ff 0, #00ffff 100%);*/
}
.el-dialog__body{         /*      窗体内容区     */
  padding-top: 0px;
  padding-bottom: 15px;
}
</style>