<template>
  <div class="tablecon">
    <div class="selected-variables">
      <span style="margin-right: 8px;">当前结构树上所挂载的所有感知变量：</span>
      <el-tag v-for="(variable, index) in selectedVariables.slice(0, 10)"
              :key="index"
              closable
              @close="removeSelectedVariable(variable)">
        {{ variable.varName }}
      </el-tag>
      <span v-if="selectedVariables.length > 10" style="margin-left: 8px; color: #999;">
    ...更多（共 {{ selectedVariables.length }} 个）
  </span>
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
import {defineExpose, getCurrentInstance, nextTick, ref, defineEmits} from 'vue'
import {selectTableOption} from '@/const/crud/sw/model3d/configPerceivedVariable/table'
import {
  fetchList,
  getAllVariablesByNodes,
} from '@/api/sw/model3d/configPerceivedVariable/table.js'
import {onMounted} from 'vue'

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
  pageSize: 6        // 每页显示多少条
})

const props = defineProps({
  selectNodes: {
    type: Array,
    required: true
  }
})

onMounted(() => {
  selectAllVariablesByNodes()
})

//默认选中该元结构树所有变量
async function selectAllVariablesByNodes() {
  const res = await getAllVariablesByNodes(props.selectNodes)
  if (res.data.code === 0) {
    globeParams.selectedVariables = res.data.data; // Map<String, List<Variable>>
    updateAllSelectedVariables();
    // 组装一维变量数组
  } else {
    console.error('获取变量失败', res.data.message)
  }
}


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
  // 只有当 current node 存在时才加载数据
  if (!globeParams.curNode) {
    console.warn('table-select.vue: No current node selected for table data loading.');
    data.value = []; // 清空表格数据
    page.total = 0;  // 总条数设为0
    return;
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
    // 先拿到当前节点下所有已选变量
    let allSelected = globeParams.selectedVariables[nodeId] || [];
    // 当前页所有变量的id
    const currentPageIds = data.value.map(item => item.varId);
    // 过滤掉当前页的已选项（因为 selection 里会有最新的勾选状态）
    allSelected = allSelected.filter(item => !currentPageIds.includes(item.varId));
    // 合并当前页的 selection
    globeParams.selectedVariables[nodeId] = allSelected.concat(selection.map(item => ({...item})));
  }
  updateAllSelectedVariables();
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

function clearSelected() {
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
