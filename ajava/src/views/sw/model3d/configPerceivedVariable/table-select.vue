<template>
  <div class="tablecon">
    <!-- 显示选中的变量 -->
    <div class="selected-variables">
      <el-tag v-for="(variable, index) in selectedVariables"
              :key="index"
              closable
              @close="removeSelectedVariable(variable)">
        {{ variable.varName }}
      </el-tag>
    </div>

    <avue-crud ref="crudRef" class="h-avue-crud"
               v-model="form"
               :option="option"
               @on-load="getList"
               @size-change="sizeChange"
               @current-change="currentChange"
               @selection-change="handleSelectionChange"
               v-model:page="page"
               :data="data">
    </avue-crud>
  </div>
</template>
<script setup>
import {defineExpose, getCurrentInstance, nextTick, ref, defineEmits } from 'vue'
import {selectTableOption} from '@/const/crud/sw/model3d/configPerceivedVariable/table'
import {fetchList} from '@/api/sw/model3d/configPerceivedVariable/table.js'

let {proxy} = getCurrentInstance()        //获取this

const emit = defineEmits(['update:selected-variables'])

let globeParams = {
  curNode: null, // 当前节点
  selectedVariables: {} // 存储每个节点下选择的变量
}     //  声明一个全局参数对象

const crudRef = ref(null)
let selectedVariables = ref([])
let option = ref(selectTableOption)
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
  {
    globeParams.curNode = proxy.$parent.getCurNode()        //  调用父组件的方法，获取当前节点
  }
  let res = await fetchList({nodeId: globeParams.curNode.id, pageSize: page.pageSize, current: page.currentPage})
  if (res.data.code === 0) {
    res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
    data.value = res.data.data.records
    page.total = res.data.data.total

    // 恢复选择
    restoreSelection(globeParams.curNode.id)
  }
}

async function handleSelectionChange(selection) {

  if (globeParams.curNode) {
    const nodeId = globeParams.curNode.id;
    globeParams.selectedVariables[nodeId] = selection.map(item => ({ ...item }));
  }

  // 更新所有选中的变量
  updateAllSelectedVariables()
}

// 更新所有选中的变量
function updateAllSelectedVariables() {
  selectedVariables.value = Object.values(globeParams.selectedVariables).flat()
  emit('update:selected-variables', selectedVariables.value)
}

// 删除选中的变量
function removeSelectedVariable(variable) {
  // 从所有节点中移除该变量
  for (const nodeId in globeParams.selectedVariables) {
    globeParams.selectedVariables[nodeId] = globeParams.selectedVariables[nodeId].filter(v => v.varId !== variable.varId)
  }

  // 更新所有选中的变量
  updateAllSelectedVariables()

  // 重新加载数据并恢复选择
  if (globeParams.curNode) {
    _refreshTableByNodeId(globeParams.curNode.id)
  }
}

//  根据节点Id，刷新表格数据
async function _refreshTableByNodeId(nodeId) {
  let res = await fetchList({nodeId: nodeId, pageSize: page.value.pageSize, current: page.value.currentPage})
  if (res.data.code === 0) {
    res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
    data.value = res.data.data.records
    page.value.total = res.data.data.total

    // 恢复选择
   restoreSelection(nodeId)
  }
}

// 恢复选择
function restoreSelection(nodeId) {
  const selectedVariables = globeParams.selectedVariables[nodeId] || []
  const selectedIds = new Set(selectedVariables.map(item => item.varId))

  const newSelectedRows = data.value.filter(item => selectedIds.has(item.varId))

  if (crudRef.value) {
    nextTick(() => {
      crudRef.value.toggleSelection(newSelectedRows)
    })
  }
}

function clearSelected(){
  globeParams.selectedVariables = {}
  selectedVariables.value = []
  // 重新加载数据并恢复选择
  if (globeParams.curNode) {
    _refreshTableByNodeId(globeParams.curNode.id)
  }
}

/*  外部调用接口函数定义
    根据节点id，查询节点关联的感知变量
 */
const getVariable = async (node) => {
  globeParams.curNode = node                  //  缓存当前节点
  await _refreshTableByNodeId(node.id)
}

defineExpose({getVariable, clearSelected})            //  暴露组件接口


</script>
<style lang="scss" scoped>
@import '@/styles/my-avue-crud.scss';

.tablecon {
  height: 100%;
  width: 100%;
}
</style>
