<template>
  <div class="tablecon">
    <avue-crud ref="crudRef" class="h-avue-crud"
               v-model="form"
               :option="option"
               @on-load="getList"
               @size-change="sizeChange"
               @current-change="currentChange"
               @row-save="rowSave"
               @row-update="rowUpdate"
               @row-del="rowDel"
               v-model:page="page"
               :data="data">
    </avue-crud>
  </div>
</template>
<script setup>
import {ref, defineExpose, getCurrentInstance} from 'vue'
import {tableOption} from '@/const/crud/sw/model3d/configPerceivedVariable/table'
import {fetchList, putObj, addObj, delObj} from '@/api/sw/model3d/configPerceivedVariable/table.js'
import {ElMessageBox, ElMessage} from 'element-plus'

let {proxy} = getCurrentInstance()        //获取this

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

//  修改每页条数
function sizeChange(pageSize) {
  page.value.pageSize = pageSize
}

//  翻页
function currentChange(current) {
  page.value.currentPage = current
}

//  分页或者修改每页条数时，自动调用
async function getList(page, params) {
  if (!globeParams.curNode)
    globeParams.curNode = proxy.$parent.getCurNode()        //  调用父组件的方法，获取当前节点
  let res = await fetchList({nodeId: globeParams.curNode.id, pageSize: page.pageSize, current: page.currentPage})
  if (res.data.code === 0) {
    res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
    data.value = res.data.data.records
    page.total = res.data.data.total
  }
}


//  新增
async function rowSave(row, done, loading) {
  try {
    if (globeParams.curNode) {
      row.nodeId = globeParams.curNode.id
      let res = await addObj(row)
      if (res.data.code === 0) {
        ElMessage.success('新增成功')
        //  新增记录后，avue-crud组件会自动将新增的记录添加到data数组中，因此不需要刷新表格，只需要把缺失的属性不全即可。删除和编辑也是一样的
        row.failureId = res.data.data.failureId
        row.hasChildren = false
      }
    } else
      ElMessage.warning('请选择一个节点')
    done(row)
  } catch (error) {
    console.log(error)              // 处理错误
  }
}

async function rowDel(row, index, done) {
  try {
    await ElMessageBox.confirm(
        '该操作将删除当前节点关联的故障模式，是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
    let res = await delObj(row.varId)
    if (res.data.code === 0) {
      ElMessage.success('删除成功')
    }
    done(row)
  } catch (error) {
    console.log(error)              // 处理错误
  }
}

async function rowUpdate(row, index, done, loading) {
  try {
    let res = await putObj(row)
    if (res.data.code === 0) {
      ElMessage.success('编辑成功')
    }
    done(row)
  } catch (error) {
    console.log(error)              // 处理错误
  }
}

//  根据节点Id，刷新表格数据
async function _refreshTableByNodeId(nodeId) {
  let res = await fetchList({nodeId: nodeId, pageSize: page.value.pageSize, current: page.value.currentPage})
  if (res.data.code === 0) {
    res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
    data.value = res.data.data.records
    page.value.total = res.data.data.total
  }
}


/*  外部调用接口函数定义
    根据节点id，查询节点关联的感知变量
 */
const getVariable = async (node) => {
  globeParams.curNode = node                  //  缓存当前节点
  await _refreshTableByNodeId(node.id)
}

defineExpose({getVariable})            //  暴露组件接口


</script>
<style lang="scss" scoped>
@import '@/styles/my-avue-crud.scss';

.tablecon {
  height: 100%;
  width: 100%;
}
</style>
