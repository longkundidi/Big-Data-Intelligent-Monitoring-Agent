<template>
  <basic-container id="container" style="display: flex; flex-direction: column">
    <el-header class="top-page">
            <span class="top-text">
                维修智能推荐->维修方法图谱搜索服务
            </span>
      <div style="float: right;display: flex;justify-content: space-around;width: 30%">
        <div style="display: flex;align-items:center;width: 80%">
          <text style="width: 80%">当前布局算法选择：</text>
          <el-select v-model="currentLayout" :popper-append-to-body="false" placeholder="请选择" size="default">
            <el-option
                v-for="item in layoutTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
                @click="g6LayoutTypes(item)">
            </el-option>
          </el-select>
        </div>
        <div style="margin-left: 2%">
          <el-button size="large" class="viewBtn"
                     @click="gotoTablePage">列表查看
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
        <div class="legend-tooltip" style="color: #1c1b1b;">
          <h3 style="font-size: 15px;">点击以下图例隐藏/显示该图例类型的节点和关系: </h3>
        </div>
        <div id="mychart" style="height:100%;background:white;margin-top: 0%;"></div>
      </el-main>
      <el-dialog
          :title="entity_title"
          v-model="NodeVisible"
          width="50%"
          :before-close="close"
      >
        <div style="font-size: 16px; line-height: 23px; text-align: left;">
          <div v-for="(item, index) in nodeData" :key="index" v-html="formatText(item)"></div>
        </div>
      </el-dialog>
    </el-container>
  </basic-container>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import * as echarts from 'echarts';
import {getGraphByStruct, reqAddRootNode, reqSonNodes, reqTreeNodes, getGraphNodeInfo} from "@/api/kg/searchService";
import {checkTaskStatus, createTask} from "@/api/kg/manage/taskStatus";
import BasicContainer from "components/BasicContainer/main.vue";

/*问答弹窗*/
let visible = ref(false);  //弹窗显隐控制
let title = ref('');
let NodeVisible = ref(false);
let entity_title = ref('');
let nodeData = ref([]);

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
let curStructLabel = ''
//endregion
// region 图变量
let myChart = null
let currentLayout = ref('')
let layoutTypes = ref([
  {label: "经典力导向布局", value: "force"},
  {label: "环形布局", value: "circular"},
])
let zoomSize = {} //zoomSize放缩大小
let des = ref([]);
let nodes = []
let edges = []
let name = []
let type = []
let nodesinfo = []
let colorType = []
let typeNumber = {}
let hasEncounteredName = false;
//endregion
//region 功能变量

let ontologyName = ''

let kgType = ''
let taskStatus = ''
let taskId = ''
let jsonData =ref( {});
//endregion
function findtext_id(text)
{
  return jsonData.value[text]
}
// region 点击事件
function addLinkEvent(id, text) {
  console.log("text",text)
  document.getElementById(id).addEventListener('click', function (event) {
    event.preventDefault();
    NodeVisible.value = false;
    id = findtext_id(text);

    // 添加调试信息
    console.log("点击事件触发，text:", text);
    console.log("找到的 id:", id);

    getGraphNodeInfo(id, kgType).then(r => {
      nodeData.value = r.data.data;

      // 检查 nodeData.value 是否存在且为数组
      if (nodeData.value && Array.isArray(nodeData.value)) {
        jsonData.value = nodeData.value.pop();

        for (let i = 0; i < nodeData.value.length; i++) {
          let item = nodeData.value[i];
          if (item.includes("名称:")) {
            name = item.split("名称:")[1].trim();
            console.log("name", name);
          }
          if (item.includes("类型:")) {
            type = item.split("类型:")[1].trim();
            console.log("type", type);
          }
        }

        console.log("name", name);
        console.log("type", type);
        entity_title.value = `${type}:${name}信息查看`;
      } else {
        console.error("nodeData.value 不存在或不是数组");
      }

    }).catch(error => {
      console.error("获取节点信息时发生错误：", error);
    });
    NodeVisible.value = true;
    // TypeVisible = false;
  });
}



function formatText(item) {
  item = String(item);
  if (item.includes("证件:") || item.includes("照片:")) {
    let temp = item.split(":")[0] + ":";
    let url = item.replace(temp, "").trim();
    if (!url.includes("暂无证件相关信息")) {
      let imageUrl = `http://${url}`;
      let image = `<img src="${imageUrl}" alt="Image" style="height: 200px; width: auto;">`;
      return `证件:${image}`;
    }
  } else if (!item.includes("名称") && !item.includes("包括") && (item.includes(".mp4") || item.includes(".pdf") || item.includes(".jpg") || item.includes(".doc") || item.includes(".xlsx") || item.includes(".ppt"))) {
    let temp = item.split(":")[0] + ":";
    let url1 = item.replace(temp, "").trim();
    url1 = `http://${url1}`;
    if (url1 !== "" && url1 !== "null") {
      hasEncounteredName = false;
      let link = `<a href="${url1}" target="_blank">${des}</a>`;
      return link;
    }
  } else if ((item.includes(".htm"))) {
    des = item.split(":")[0];
    let temp = item.split(":")[0] + ":";
    let url1 = item.replace(temp, "").trim();
    if (url1 !== "" && url1 !== "null") {
      let link = `<a href="${url1}" target="_blank">${des}</a>`;
      return link;
    }
  } else if ((item.startsWith("名称:"))) {
    return `<span style="font-size: 20px;margin-top: 30px; font-weight: bold;">${item}</span>`;
  } else if (item.includes("包括：")) {
    // 使用正则表达式分割 'flag；；；'，以避免错误分割文件名
    const entities = item.split("包括：")[1].trim().split(/flag；；；/);
    const formattedEntities = entities.map(entity => {
      const trimmedEntity = entity.trim();
      const id = Date.now() + Math.random().toString(16).substring(2);
      setTimeout(addLinkEvent.bind(null, id, trimmedEntity), 0);
      return `<a id="${id}" href="#" style="margin-top:1.30em;">${trimmedEntity}</a>`;
    }).join("; ");
    const itemComponents = item.split("包括：");
    return `${itemComponents[0]}：${formattedEntities}<br>`;
  } else {
    return item;
  }
}



const close=function ()
{
  nodeData.value = [];
  // graphData = [];
  // pageData = [];
  // currentPageNodeData = [];
  // type_title = "";
  NodeVisible.value = false
}
//endregion
//region vue周期
onBeforeUnmount(async () => {
  if (!myChart) {
    return
  }
  myChart.dispose();
  myChart = null;
})

onMounted(() => {
  ontologyName = "维修知识本体"
  kgType = "维修知识"
  initParams()
  getTreeNodes(globeParams.nodeLevel)
  curStruct = $route.query.curStruct;  // 结构树id
  curStructLabel = $route.query.curStructLabel; //结构名称
  if (!isEmpty(curStruct)) { //非空直接查询
    graphStruct();
  }
})
//endregion

//region 结构树
const initParams = function () {
  globeParams.nodeLevel = 3       //    预先展开3层节点
}
//  节点点击消息响应
const handleNodeClick = function (data, node, treeNode, event) {
  curStruct = data.id
  curStructLabel = data.name
  graphStruct();
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

//region 布局方式切换
const g6LayoutTypes = function (item) {
  currentLayout.value = item.label;
  myChart = echarts.init(document.getElementById('mychart'));
  myChart.clear()
  if (item.value === "force") {
    zoomSize = 2;
  } else {
    zoomSize = 1;
  }
  drawEcharts(colorType, nodes, edges, item.value, zoomSize);
}
//endregion

//region 图布局
//初始化图
const initEcharts = function () {
  myChart = echarts.init(document.getElementById('mychart'));
  myChart.clear()
  myChart.showLoading();
  nodes = [];
  edges = [];
  colorType = [];
  typeNumber = {};
  nodesinfo = [];
}
//绘制echarts图形
const drawEcharts = function (colorType, nodes, edges, layout, zoomSize) {
  let arrayEcharts = []
  colorType.forEach(type => {
    arrayEcharts.push({name: type})
  })
  const option = {
    title: {
      top: "top",
      left: "left"
    },
    tooltip: {},
    legend: [
      {
        backgroundColor: '#ffffff',
        top: '1%',
        type: 'scroll',
        data: arrayEcharts.map(function (a) {
          return a.name;
        })
      }
    ],
    series: [
      {
        name: '知识图谱可视化',
        type: 'graph',
        top: 20,
        layout: layout,
        data: nodes,
        links: edges,
        categories: arrayEcharts,
        roam: true,
        zoom: zoomSize,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        labelLayout: {
          hideOverlap: false//是否隐藏下层重叠的节点的标签
        },
        tooltip: {//nodesinfo
          formatter: function (params) {
            let curname = params.data;
            let count = 0;
            for (let i in curname) {
              count++;
            }//长度三的才是节点
            if (count === 3) {
              let res = ''
              // console.log("nodeinfo: ", nodesinfo)
              let templist = nodesinfo.filter(nodeinfo => (nodeinfo.id.toString() === curname.id))
              // console.log("templist: ", templist)
              if (templist.length > 0) {
                let tempitem = templist[0].attr
                res = `名称：${tempitem["name"]}<br/>`
                for (let key in tempitem) {
                  if (key !== "name" && key !== "code" && key !== "types") {
                    res = res + `${key}：${tempitem[key]}<br/>`
                  }
                }
                return res
              }
              return params.name
            }
            let tempdata = params.data
            let source = tempdata['source']
            let target = tempdata['target']
            let start = ''
            let end = ''
            for (let i = 0; i < nodes.length; i++) {
              if (nodes[i].id === source) {
                start = nodes[i].name
              }
              if (nodes[i].id === target) {
                end = nodes[i].name
              }
            }
            return start + '->' + end
          }
        },
        scaleLimit: {
          min: 0.5,
          max: 3
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        },
        force: {
          initLayout: 'circular',
          repulsion: 50,
          gravity: 0.1,
          layoutAnimation: true,
          friction: 0.1
        },
        cursor: "pointer",
        legendHoverLink: true,
        hoverAnimation: true,
        symbol: "roundRect", //图形 'circle', 'rect', 'roundRect', 'triangle', 'diamond', 'pin', 'arrow'
        draggable: true, //节点是否可以拖拽
        symbolSize: 10, //设置节点大小
        edgeSymbol: ['', 'arrow'], //箭头指向
        emphasis: {
          scale: 1.8,
          focus: 'adjacency',
        },
        left: 0,
        animation: true,
        animationEasing: 'cubicOut',
        animationDuration: 1500,
        animationEasingUpdate: "quinticInOut",
      }
    ]
  };
  option && myChart.setOption(option);
  //随着屏幕大小调节图表
  window.addEventListener("resize", () => {
    myChart.resize();
  });
  myChart.off();
  myChart.on("click", function (params) {
    if (params.dataType === "node") { //点击节点才处理
      // 通过后端获取到当前节点的基本属性、关系等信息进行处理
      let id=params.data.id;
      getGraphNodeInfo(id, kgType).then(r => {
        nodeData.value = r.data.data;
        jsonData.value = nodeData.value.pop();
        console.log("jsondata",jsonData)
        // 遍历 nodeData 除了最后一个元素
        for (let i = 0; i < nodeData.value.length; i++) {
          console.log("进入循环！")
          let item = nodeData.value[i];
          if (item.includes("名称:"))
          {
            name = item.split("名称:")[1].trim();
          }
          if (item.includes("类型:"))
          {
            type = item.split("类型:")[1].trim();
          }
        }
        // 设置 entity_title
        entity_title.value = `${type}:${name}信息查看`;
        // 在此处你可以使用 jsonData 进行其他处理
      });
      NodeVisible.value=true;
      // 打开弹窗展示信息
    }
  })
}
//endregion

//region 功能实现
//以结构节点查询对应数据
const graphStruct = async function () { //以结构节点查询对应数据
  initEcharts(); //初始化图谱
  try {
    taskStatus = "inProcess";
    const taskRespond = await createTask();
    taskId = taskRespond.data.data
    const temp = getGraphByStruct(ontologyName, curStruct, taskId);
    await checkInteractionStatus(taskId);
  } catch (error) {
    taskStatus = "falseProcess";
    console.error("后端请求出错：", error)
    myChart.hideLoading();
  }
}
const drawGraphByStruct = function (r) {
  r = JSON.parse(r);
  if (nodes.length !== r.nodes.length) {
    nodes = r.nodes;
    if (nodes.length === 0) {
      ElMessage({
        message: `${curStructLabel}结构还未导入数据！`,
        type: 'warning',
      });
    } else {
      edges = r.edges;
      colorType = r.types;
      typeNumber = r.typeNumber;
      nodesinfo = r.info;
      drawEcharts(colorType, nodes, edges, 'force', 2);
    }
  }
}
//交互状态查询
const checkInteractionStatus = async function (taskId) {
  let waitTime = 0;
  let t = 0;
  while (taskStatus === "inProcess") {
    //轮询后端交互状态
    let response = await checkTaskStatus(taskId)
    response = response.data
    if (response.data.status === "endProcess") {
      await ElMessage({
        type: "success",
        message: "查询完成！",
      });
      if (response.data.result !== null && response.data.result !== "") {
        if (nodes.length === 0) { //第一次渲染
          myChart.hideLoading();
        }
        drawGraphByStruct(response.data.result); //去渲染
      } else {
        ElMessage({
          message: `${curStructLabel}结构还未导入数据！`,
          type: 'warning',
        });
        myChart.hideLoading();
      }
      taskStatus = 'endProcess';
    } else if (response.data.status === "inProcess") {
      t = 3000
      await new Promise(resolve => setTimeout(resolve, t)); //等待2秒后再轮询
      waitTime = waitTime + t / 1000;
      await ElMessage({
        type: "info",
        message: `正在查询，已加载${waitTime}秒。`,
        duration: 2000,
      });
      if (response.data.result !== null && response.data.result !== "") {
        if (nodes.length === 0) { //第一次渲染
          myChart.hideLoading();
        }
        drawGraphByStruct(response.data.result); //去渲染
      }
    } else {
      await ElMessage({
        type: "error",
        message: "查询出错",
        duration: 3000,
      });
      taskStatus = "falseProcess";
      myChart.hideLoading();
    }
  }
}
//endregion
const gotoTablePage = function () {//携带图谱类型，当前域，结构树id
  $router.push({
    path: './indexTable',
    query: {
      curStruct: curStruct,
      curStructLabel: curStructLabel,
    }
  });
}

//判断是否为空的办法
function isEmpty(obj) {
  return typeof obj == "undefined" || obj == null || obj === "";
}
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
