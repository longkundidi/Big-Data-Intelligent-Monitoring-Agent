<template>
  <basic-container id="container" style="display: flex; flex-direction: column">
    <el-header class="top-page">
            <span class="top-text">
                维修智能推荐->维修方法列表搜索服务
            </span>
      <div style="float: right;display: flex;justify-content: space-around;width: 15%">
        <div>
          <el-button
              size="large"
              class="normalBtn"
              icon="el-icon-search"
              @click="query"
          >查询
          </el-button>
        </div>
        <div style="margin-left: 2%">
          <el-button
              class="viewBtn"
              size="large"
              @click="goToGraph"
          >图谱查看
          </el-button>
        </div>
      </div>
    </el-header>
    <el-container style="height: 900px">
      <el-aside width="18%" class="thisAside">
        <el-scrollbar>
          <div class="lineyes treestyle">
            <el-tree
                :data="treeData"
                :indent="0"
                ref="tree"
                :accordion="true"
                :props="defaultProps"
                :expand-on-click-node="false"
                node-key="id"
                :default-expanded-keys="defaultExpandKeys"
                icon="none"
                :lazy="false"
                currentKey=""
                :show-checkbox="false"
                :check-strictly="true"
                :default-expand-all="false"
                @node-click="handleNodeClick"
                @node-expand="handleNodeExpand"
            >
            </el-tree>
          </div>
        </el-scrollbar>
      </el-aside>
      <el-main class="thisMain">
        <el-form>
          <el-form-item>
            <el-tag type="danger">当前节点：{{ curStructLabel }}</el-tag>
          </el-form-item>
          <el-form-item style="margin-bottom: -15px">
            <el-tabs type="card" @tab-click="handleClick" v-model="activeName">
              <el-tab-pane v-for="(item,index) in allInstances" :key="index" :label="item" :name="item"
                           style="font-size: 18px"></el-tab-pane>
            </el-tabs>
          </el-form-item>
        </el-form>
        <el-table :data="fileTable.slice((page-1)*size,page*size)"
                  :border="true"
                  stripe
                  :fit="true"
                  :cell-style="tableCellStyle"
                  :header-cell-style="tableHeaderCellStyle"
                  :header-cell-class-name="getRowClass"
                  :cell-class-name="cellStyle"
                  :row-style="{ height: '50px' }"
        >
          <el-table-column
              :formatter="formatter"
              align="center"
              v-for="col in cols"
              :prop="col.prop" :label="col.label">
            <template #default="scope" v-if="col.type==='file'">
              <el-button
                  class="viewBtn"
                  size="small"
                  @click="showKg(scope.row,col.label)"
              >查看
              </el-button>
            </template>
            <template #default="scope" v-if="col.type==='link'">
              <el-button
                  class="viewBtn"
                  size="small"
                  @click="showKgLink(scope.row,col.label)"
              >查看
              </el-button>
            </template>
            <template #default="scope" v-if="col.type==='Instance'">
              <el-button
                  class="viewBtn"
                  size="small"
                  @click="nextLevel(scope.row,col.label)"
              >查看
              </el-button>
            </template>
            <template #default="scope" v-if="col.type==='Event'">
              <el-button
                  class="viewBtn"
                  size="small"
                  @click="nextLevel(scope.row,col.label)"
              >查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
            background
            @size-change="handleSizeChangeTable"
            @current-change="handleCurrentChangeTable"
            :current-page="page"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
        ></el-pagination>
      </el-main>
    </el-container>
  </basic-container>
  <kk-file-compent :showFileUrl="viewKgFileItem.showFileUrl" v-model:kkfileViewShowProp="fileVisible"></kk-file-compent>
  <el-dialog
      title="知识列表查看"
      v-model="visible"
      width="80%"
      append-to-body
      :before-close="close"
      :destroy-on-close="true"
  >
    <el-table :data="fileTableDialog.slice((pageDialog-1)*sizeDialog,pageDialog*sizeDialog)"
              :border="true"
              stripe
              :fit="true"
              :cell-style="tableCellStyle"
              :header-cell-style="tableHeaderCellStyle"
              :header-cell-class-name="getRowClass"
              :cell-class-name="cellStyle"
              :row-style="{ height: '30px' }"
    >
      <el-table-column
          :formatter="formatter"
          align="center"
          v-for="col in colsDialog"
          :prop="col.prop" :label="col.label">
        <template #default="scope" v-if="col.type==='file'">
          <el-button
              class="viewBtn"
              size="small"
              @click="showKg(scope.row,col.label)"
          >查看
          </el-button>
        </template>
        <template #default="scope" v-if="col.type==='link'">
          <el-button
              class="viewBtn"
              size="small"
              @click="showKgLink(scope.row,col.label)"
          >查看
          </el-button>
        </template>
        <template #default="scope" v-if="col.type==='Instance'">
          <el-button
              class="viewBtn"
              size="small"
              @click="nextLevel(scope.row,col.label)"
          >查看
          </el-button>
        </template>
        <template #default="scope" v-if="col.type==='Event'">
          <el-button
              class="viewBtn"
              size="small"
              @click="nextLevel(scope.row,col.label)"
          >查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        background
        @size-change="handleSizeChangeTableDialog"
        @current-change="handleCurrentChangeTableDialog"
        :current-page="pageDialog"
        :page-sizes="[10, 20, 30, 50]"
        :page-size="sizeDialog"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalDialog"
    ></el-pagination>
  </el-dialog>
  <el-dialog
      title="数据查询"
      v-model="visibleQuery"
      width="25%"
      append-to-body
      :before-close="close"
  >
    <el-form>
      <el-form-item label="查询类型">
        <el-select v-model="queryType">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="查询名称">
        <el-input
            size="small"
            v-model="queryData"
        >
        </el-input>
      </el-form-item>
    </el-form>
    <div slot="footer"
         class="dialog-footer">
      <el-button class="cancelbtn"
                 @click="closeQuery()">取 消
      </el-button>
      <el-button class="determinebtn"
                 @click="submitQuery()">保 存
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import {
  findAllFile,
  findNeoStructId, findQueryDataTables, getDifferentDomains,
  getTableInfo,
  reqAddRootNode,
  reqSonNodes,
  reqTreeNodes
} from "@/api/kg/searchService";
import BasicContainer from "components/BasicContainer/main.vue";
import {minioUrl} from '@/config/env.js'
import KkFileCompent from "components/kkFileView/kkFileCompent.vue";

//region vue变量
const $router = useRouter();
const $route = useRoute();
//endregion

//region 结构树变量
let globeParams = {}     //  声明一个全局参数对象
let treeData = ref([])
let expandKeys = []      //  缓存待扩展的节点
let defaultExpandKeys = ref([])
let defaultProps = ref({
  children: 'children',
  label: 'name',
  isLeaf: 'leaf'
})
let curStruct = ''//节点id
let curStructLabel = ref('')
let tableInfoVo = {
  ontologyName: '',
  parentId: null,
  currentType: '',
}
let path = [] //path记录路径信息
//endregion

//region 主功能变量
let ontologyName = ''
let taskStatus = ''
let curLevel = ''
let mapObj = new Map()
let allFile = []
let fileTable = ref([])
let cols = ref([])
let total = ref(1)
let size = ref(10)
let page = ref(1)
let curDomain = ''
let activeName = ref('')
let allInstances = ref([])
//endregion

//region 弹窗功能变量
let fileVisible = ref(false)
const viewKgFileItem = reactive({
  showFileUrl: ""

})
let visible = ref(false)
let fileTableDialog = ref([])
let colsDialog = ref([])
let totalDialog = ref(1)
let sizeDialog = ref(10)
let pageDialog = ref(1)
let visibleQuery = ref(false)
let queryType = ref('')
let options = ref([])
let queryData = ref('')
//endregion

//region vue周期
onMounted(() => {
  ontologyName = "维修知识本体"
  tableInfoVo.ontologyName = ontologyName
  curStruct = $route.query.curStruct;  // 结构树id
  curStructLabel.value = $route.query.curStructLabel; //结构名称
  create()
})

//实现异步操作顺序执行
async function create() {
  //获取左侧结构树
  await initParams()
  await getTreeNodes(globeParams.nodeLevel)
  if (isEmpty(curStruct)) {
    curStruct = treeData.value[0].id
    curStructLabel.value = treeData.value[0].name
  }
  //根据当前结构树找到对应父节点id
  let second = await findNeoStructId(ontologyName, curStruct)
  tableInfoVo.parentId = second.data.data
  //得到与结构直接关联的实例数据
  let third = await getDifferentDomains(ontologyName)
  allInstances.value = third.data.data;
  queryTypeSelect(allInstances.value);
  curDomain = allInstances.value[0];
  activeName.value = curDomain;
  tableInfoVo.currentType = curDomain;
  curLevel = tableInfoVo.currentType;
  //获取当前实例数据形成表格数据
  refreshTable();
}// 形成左侧结构树与右侧表格数据
//endregion

//region 顶栏功能
const query = function () {
  visibleQuery.value = true
}
const goToGraph = function () {
  $router.push({
    path: "./index",
    query: {
      curStruct: curStruct,
      curStructLabel: curStructLabel.value,
    }
  })
}
//endregion

//region 结构树
const initParams = function () {
  globeParams.nodeLevel = 3       //    预先展开3层节点
}
//  节点点击消息响应
const handleNodeClick = function (data, node, treeNode, event) {
  curStruct = data.id
  curStructLabel.value = data.name
  findNeoStructId(ontologyName, curStruct).then(r => {
    tableInfoVo.parentId = r.data.data;
    path = []
    refreshTable();
  })
}
//  节点扩展消息响应
const handleNodeExpand = async function (data, node, treeNode) {
  if ((node.level >= globeParams.nodeLevel) || (_sonNodeHasLoading(data))) {
    let response = await reqSonNodes(data.nodeCode)
    if (response.data.data) {
      response.data.data.forEach((item) => {
        item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
      })
      node.data.children = response.data.data
    }
  }
}
/* 检查一个节点的子节点中，是否有待加载（Loading）状态的子节点
            *  这种情况只会发生在第二层节点复制后，它的子节点没有加载的情况下。这时，虽然节点层级小于globeParams.nodeLevel，但是仍然需要查询后台
            */
const _sonNodeHasLoading = function (node) {
  let hasLoading = false
  for (let i = 0; i < node.children.length; i++) {
    if (node.children[i].id === 'loading') {
      hasLoading = true
      break
    }
  }
  return hasLoading
}
// 根据节点层级数，加载结构树的一组节点
const getTreeNodes = async function (nodeLevel) {
  try {
    let response = await reqTreeNodes(nodeLevel)
    if ((response.data.data) && (response.data.data.length > 0)) {
      treeData.value = []
      expandKeys = []            //  缓存待扩展的节点
      let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
      for (let item of rootNodes) {
        treeData.value.push(item)
        expandKeys.push(item.id)
        await setChildren(item, response.data.data)
      }
      defaultExpandKeys.value = expandKeys            //  扩展节点
    } else {
      let confirmResult = await this.$confirm('首次编辑GBOM，系统未找到根节点，是否创建根节点?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      if (confirmResult) {
        let addRootNodeResponse = await reqAddRootNode({nodeName: '模版根节点', nodeCode: 'TM1'})
        if (addRootNodeResponse.data.code === 200) {
          this.$message.success('默认根节点创建成功')
          getTreeNodes(globeParams.nodeLevel)
        }
      }
    }
  } catch (error) {
    console.log(error)
  }
}
//  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
const setChildren = function (pNode, nodeList) {
  let res = getChildrenByNodeCode(pNode.nodeCode, nodeList)
  let children = res.sonNodes
  if (children.length === 0) {
    if ((pNode.nodeType === 'Mid') || (pNode.nodeType === 'Root')) {           //  如果不是叶子节点，节点前显示"+"号
      expandKeys = expandKeys.filter(item => item !== pNode.id) //  从扩展节点中删除它
      pNode.children = [{id: 'loading', name: '节点加载中...'}]
    }
    return pNode
  } else {
    pNode.children = children
    children.forEach((item) => {
      expandKeys.push(item.id)          //  添加到扩展节点
      setChildren(item, res.otherNodes)
    })
  }
}
//  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
const getChildrenByNodeCode = function (pNodeCode, nodeList) {
  let sonNodes = []
  let otherNodes = []
  let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
  nodeList.forEach((item) => {
    if (regex.test(item.nodeCode))          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
      sonNodes.push(item)
    else
      otherNodes.push(item)
  })
  return {sonNodes: sonNodes, otherNodes: otherNodes}
}
//endregion

//region 主要功能
const refreshTable = function () {
  //记录路径信息
  path.push(curLevel);
  //这个是记录跳转到下一级前的如何获取表格信息的操作
  mapObj.set(curStruct + curLevel, Object.assign({}, tableInfoVo));
  findAllFile().then((r) => {
    //获取所有文件类型
    allFile = r.data.data;
    getTableInfo(tableInfoVo).then((r) => {
      r = r.data
      fileTable.value = r.data.tableData;
      fileTable.value = convertFileType(fileTable.value, allFile);
      cols.value = r.data.cols;
      total.value = r.data.num;
    })
  })
}
const convertFileType = function (datas, allFile) {
  for (const data of datas) {
    for (const key in data) {
      if (allFile.indexOf(key) !== -1) {
        data[key] = minioUrl + data[key]
      }
    }
  }
  return datas;
} // 拼接上fileUrl
const handleClick = function (tab) {
  curDomain = tab.props.label;
  tableInfoVo.currentType = curDomain;
  curLevel = curDomain;
  path = []
  refreshTable();
}// 作用就是得到当前实例概念类型以及查询对应实例
const tableCellStyle = ({row, column, rowIndex, columnIndex}) => {
  return {
    color: '#000000',
    fontsize: '10px',
  }
}// 单元格样式
const tableHeaderCellStyle = ({row, column, rowIndex, columnIndex}) => {
  return {
    color: 'white',
    'background-color': 'rgb(0,57,144)',
    'font-size': '10px'
  }
}// 修改 table header cell的背景色
const getRowClass = (row, rowIndex) => {
  return "theadStyle";
}// 定义表头单元格的style的回调方法
const cellStyle = (row, rowIndex) => {
  return "tdStyle";
}// 绑定样式属性
const formatter = function (row, column, cellValue, index) {
  if (cellValue !== 'null' && Number(cellValue)) {
    return Math.trunc(Number(cellValue))
  } else if (cellValue !== 'null') {
    return cellValue;
  } else {
    return '';
  }
}// 解决数字出现.0的情况
const showKg = function (row, label) {
  if (row[label].indexOf('null') !== -1) {
    ElMessage({
      type: 'warning',
      message: '证件不存在！',
    })
  } else {
    fileVisible.value = true;
    viewKgFileItem.showFileUrl = row[label]
  }
}// 查看文档
const showKgLink = function (row, label) {
  if (row[label].indexOf('null') !== -1) {
    ElMessage({
      type: 'warning',
      message: '不存在地址！',
    })
  } else {
    window.open(row[label])
  }
}// 查看链接
const nextLevel = function (row, label) {
  tableInfoVo.parentId = row.id;
  tableInfoVo.currentType = label;
  curLevel = label;
  refreshTableDialog();
}// 实现跳转到下一层级
const handleSizeChangeTable = function (newSize) {
  page.value = 1;
  size.value = newSize;
}// 重置页面大小，每次重置会回到第一页，选择页面数量
const handleCurrentChangeTable = function (current) {
  page.value = current;
};// 翻页操作
//endregion

//region 弹窗功能
const fileClose = function () {
  fileVisible.value = false;
}// 关闭文件弹窗
const refreshTableDialog = function () {
  visible.value = true
  //记录路径信息
  path.push(curLevel);
  //这个是记录跳转到下一级前的如何获取表格信息的操作
  mapObj.set(curStruct + curLevel, Object.assign({}, tableInfoVo));
  findAllFile().then((r) => {
    //获取所有文件类型
    allFile = r.data.data;
    getTableInfo(tableInfoVo).then((r) => {
      r = r.data
      fileTableDialog.value = r.data.tableData;
      fileTableDialog.value = convertFileType(fileTableDialog.value, allFile);
      colsDialog.value = r.data.cols;
      totalDialog.value = r.data.num;
    })
  })
}
const handleSizeChangeTableDialog = function (newSize) {
  pageDialog.value = 1;
  sizeDialog.value = newSize;
}// 重置页面大小，每次重置会回到第一页，选择页面数量
const handleCurrentChangeTableDialog = function (current) {
  pageDialog.value = current;
}// 翻页操作
const closeQuery = function () {
  visibleQuery.value = false
  queryType.value = ''
  queryData.value = ''
}// 关闭查询弹窗
const submitQuery = function () {
  curDomain = queryType.value;
  activeName.value = curDomain;
  curStructLabel.value = "所有层级"
  findQueryDataTables(ontologyName, queryType.value, queryData.value).then((r) => {
    r = r.data
    if (r.data.num === 0) {
      ElMessage({
        type: "info",
        message: "暂未找到数据，列表初始化!",
      })
      tableInfoVo.currentType = curDomain;
      refreshTable();
    } else {
      ElMessage({
        type: "success",
        message: "数据查找成功!",
      })
      fileTable.value = r.data.tableData;
      fileTable.value = convertFileType(fileTable.value, allFile);
      cols.value = r.data.cols;
      total.value = r.data.num;
    }
    queryType.value = '';
    queryData.value = '';
    visibleQuery.value = false;
  })
}// 提交查询
const queryTypeSelect = function (allInstances) {
  for (let key in allInstances) {
    let queryObj = {
      value: allInstances[key],
      label: allInstances[key],
    }
    options.value.push(queryObj);
  }
}
//endregion

//region 辅助功能
function isEmpty(obj) {
  if (typeof obj == "undefined" || obj == null || obj === "") {
    return true;
  } else {
    return false;
  }
}// 判断是否为空的办法
//endregion
</script>

<style scoped>
.top-page {
  display: flex;
  flex-direction: row;
  justify-content: space-between; /* 内容靠左 */
  align-items: center; /* 垂直居中 */
}

.top-text {
  font-size: 20px;
  font-weight: bold;
}

.thisMain {
  background: white;
  height: 100%;
  border-top: 1px solid #757373;
  border-right: 1px solid #757373;
  border-bottom: 1px solid #757373;
}

.thisAside {
  background: white;
  height: 100% !important;
  overflow-y: auto;
  border: 1px solid #757373;
}

.el-dialog__header .el-dialog__title {
  font-size: 30px;
}

:deep(.treestyle .el-tree-node) {
  position: relative;
  padding-left: 16px;
//  需要配合:indent="0"，才能保证竖线对齐
}

:deep(.treestyle .el-tree) {
  background-color: Transparent; /*背景透明*/
  color: #212020; /*字体颜色：黑灰色*/
}

:deep(.treestyle .el-tree-node__expand-icon.is-leaf) { /* 叶子节点隐藏图标  */
  display: none;
}

/*  下面的样式设置与连线有关    */
:deep(.treestyle .el-tree-node__children) {
  padding-left: 18px;
}

:deep(.treestyle .el-tree-node :last-child:before) {
  height: 38px;
}

.treestyle .el-tree > .el-tree-node:before {
  border-left: none;
}

.treestyle .el-tree > .el-tree-node:after {
  border-top: none;
}

:deep(.treestyle .el-tree-node:before) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.treestyle .el-tree-node:after) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.lineyes .el-tree .el-tree-node__expand-icon.expanded) { /*节点图标不旋转*/
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

:deep(.lineyes .el-tree-node__expand-icon) {
  font-size: 16px; /*图标大小*/
}

:deep(.lineyes .el-tree-node__expand-icon:before) { /*有子节点 且未展开*/
  content: "";
  background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__expand-icon.expanded:before) { /*有子节点 且已展开*/
  content: "";
  background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__content:hover) { /*鼠标滑过，修改背景色*/
  color: cyan;
  font-weight: bold;
  background-color: rgb(108, 108, 111) !important;
}

.lineyes .el-tree-node:focus > .el-tree-node__content { /*节点选中，节点获取焦点*/
  color: gold;
  font-weight: bold;
  background-color: rgba(138, 194, 252, 0.53) !important;
}

.lineyes .el-tree-node.is-current > .el-tree-node__content {
  color: gold;
}

:deep(.lineyes .el-tree-node:before) { /*显示节点间连接的竖线*/
  border-left: 1px dashed #dcdcdc;
  bottom: 0px;
  height: 100%;
  top: -26px;
  width: 3px;
}

:deep(.lineyes .el-tree-node:after) { /*显示节点间连接的横线*/
  border-top: 1px dashed #dcdcdc;
  height: 20px;
  top: 12px;
  width: 24px;
}

.custom-tree {
  display: flex;
  height: 100%;
  width: 100%;
  align-items: center; /*垂直对齐*/
}

.custom-tree .tree-btn {
  display: flex;
  height: 100%;
  width: 100%;
  align-items: center; /*垂直对齐*/
}

.custom-tree .tree-btn .btn {
  height: 100%;
  width: 30px;
}
</style>
