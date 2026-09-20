<template>
  <basic-container>
    <div class="extra">
      <el-container>
        <el-header class="title">
          知识获取管理->知识挖掘
        </el-header>
        <el-container>
          <el-aside class="thisAside">
            <h4 style="margin-left: 20px">
              当前节点：{{ currentNode.name }}
            </h4>
            <div class="treestyle">
              <el-tree
                  ref="treeRef"
                  :current-node-key="currentNode.id"
                  :data="treeData"
                  :expand-on-click-node="false"
                  :highlight-current="true"
                  :props="defaultProps"
                  default-expand-all
                  node-key="id"
                  @check-change="handleCheckChange"
                  @node-click="handleNodeClick"
              >
              </el-tree>
            </div>
          </el-aside>
          <el-container>
            <el-main>
              <div v-show="progressVisible" class="demo-progress">
                <el-progress :color="colors" :percentage="progress" :stroke-width="24" :text-inside="true"/>
              </div>
              <el-row>
                <el-col :span="20">
                  <el-row>
                    <el-row>
                      <el-col>
                        <el-card class="box-card" shadow="always">
                          <template #header>
                            <h3>聚类结果分析图</h3>
                          </template>
                          <div v-if="!progressVisible" class="card-container">
                            <img
                                :src="bestK"
                                class="card-images"
                                @click="onPreview(bestK)"
                            />
                            <img
                                :src="kmeans_result"
                                class="card-images"
                                @click="onPreview(kmeans_result)"
                            />
                            <el-image-viewer
                                v-if="showViewer"
                                @close="closeViewer"
                                :url-list="imgList"
                            />
                          </div>
                        </el-card>
                      </el-col>
                    </el-row>
                    <el-card>
                      <template #header>
                        <h3>知识挖掘结果</h3>
                      </template>
                      <div v-if="!progressVisible">
                        <div  v-for="(item, index) in miningData" :key="index">
                          <el-row class="cluster">
                            <el-descriptions
                                :column="3"
                                :title="'簇类' + (index + 1)"
                                border
                                class="descriptions"
                                size="large"
                            >
                              <el-descriptions-item>
                                <template #label>
                                  <div class="cell-item">
                                    <el-icon>
                                      <Tools/>
                                    </el-icon>
                                    聚类名称
                                  </div>
                                </template>
                                {{ item.clusterName }}
                              </el-descriptions-item>
                              <el-descriptions-item>
                                <template #label>
                                  <div class="cell-item">
                                    <el-icon>
                                      <Comment/>
                                    </el-icon>
                                    聚类词频
                                  </div>
                                </template>
                                {{ joinArr(item.wordfrequency) }}
                              </el-descriptions-item>
                              <el-descriptions-item>
                                <template #label>
                                  <div class="cell-item">
                                    <el-icon>
                                      <Histogram/>
                                    </el-icon>
                                    聚类故障类别
                                  </div>
                                </template>
                                {{ item.fault.length }}
                              </el-descriptions-item>
                            </el-descriptions>
                          </el-row>
                          <el-row>
                            <el-table :cell-style="{'text-align':'center'}"
                                      :data="item.fault"
                                      :header-cell-style="{'text-align':'center'}"
                                      border stripe>
                              <el-table-column type="expand">
                                <template #default="props">
                                  <div>
                                    <h3 style="margin-left: 30px">故障维修关联知识挖掘</h3>
                                    <h4 style="margin-left: 30px"> 故障名称：{{ props.row.faultName }}</h4>
                                    <el-table :data="props.row.maintenanceMeasures" :default-sort="{ prop: 'ration', order: 'descending' }" border
                                              stripe
                                    >
                                      <el-table-column label="维修措施" prop="measures"/>
                                      <el-table-column label="置信度" prop="ration" sortable width="100"/>
                                    </el-table>
                                  </div>
                                </template>
                              </el-table-column>
                              <el-table-column label="故障名称" prop="faultName"/>
                              <el-table-column label="聚类故障记录数" prop="faultNum" sortable/>
                              <!-- 使用插槽来自定义列的内容 -->
                              <el-table-column label="聚类数据展示">
                                <template v-slot="scope">
                                  <el-button @click="getFaultRecordByCaseUuid(scope.row.caseUuid)">查看故障记录
                                  </el-button>
                                </template>
                              </el-table-column>
                            </el-table>
                          </el-row>
                        </div>
                      </div>
                    </el-card>
                  </el-row>

                </el-col>
                <el-col :span="4">
                  <el-card class="box-card" shadow="always">
                    <template #header>
                      <h3>故障记录信息</h3>
                    </template>
                    <el-scrollbar height="1000px">
                      <el-link v-for="(item, index) in currentFaultRecord" :key="index" class="scrollbar-demo-item"
                               @click="handleClick(item)">
                        故障记录{{ index }}
                      </el-link>
                    </el-scrollbar>
                  </el-card>
                </el-col>
              </el-row>
            </el-main>
          </el-container>
        </el-container>
      </el-container>

    </div>
    <el-dialog v-model="isShowRecord">
      <el-descriptions :column="3"
                       border
                       direction="vertical"
                       title="故障记录">
        <el-descriptions-item label="项目名称">{{ curRecord.projectName }}</el-descriptions-item>
        <el-descriptions-item label="风机号">{{ curRecord.windTurbineNumber }}</el-descriptions-item>
        <el-descriptions-item label="机型">{{ curRecord.model }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ curRecord.faultDescription }}</el-descriptions-item>
        <el-descriptions-item label="初步原因分析">{{ curRecord.preliminaryCauseAnalysis }}</el-descriptions-item>
        <el-descriptions-item label="现场处理">{{ curRecord.onSiteHandling }}</el-descriptions-item>
        <el-descriptions-item label="具体处理建议">{{ curRecord.specificHandlingSuggestions }}</el-descriptions-item>
        <el-descriptions-item label="故障名">{{ curRecord.faultName }}</el-descriptions-item>
        <el-descriptions-item label="是否引起停机或限功率">{{ curRecord.causedShutdownOrPowerLimitation }}
        </el-descriptions-item>
        <el-descriptions-item label="故障时间">{{ curRecord.faultTime }}</el-descriptions-item>
        <el-descriptions-item label="部套_大部件型号">{{ curRecord.majorComponentModel }}</el-descriptions-item>
        <el-descriptions-item label="部件名称">{{ curRecord.componentName }}</el-descriptions-item>
        <el-descriptions-item label="维护措施">{{ curRecord.maintenanceMeasures }}</el-descriptions-item>
        <el-descriptions-item label="故障现象">{{ curRecord.faultPhenomenon }}</el-descriptions-item>
        <el-descriptions-item label="故障定位">{{ curRecord.faultLocation }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ curRecord.supplier }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="isShowClusterRecord" title="故障列表">

      <el-table :cell-style="{'text-align':'center'}"
                :data="clusterRecord"
                :header-cell-style="{'text-align':'center'}"
                border stripe>
        <el-table-column label="项目名称" prop="projectName"/>
        <el-table-column label="风机号" prop="windTurbineNumber"/>
        <el-table-column label="机型" prop="model"/>
        <el-table-column label="部件名称" prop="componentName"/>
        <el-table-column label="故障现象" prop="faultPhenomenon"/>
        <el-table-column label="故障定位" prop="faultLocation"/>
        <el-table-column label="供应商" prop="supplier"/>
      </el-table>
    </el-dialog>

  </basic-container>
</template>
<script setup>
import BasicContainer from "components/BasicContainer/main.vue";
import {onBeforeUnmount, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import {reqTreeNodes} from "@/api/kg/searchService";
import {getFaultReportsByNode, requestMiningResult, startMining} from "@/api/kg/mining";
import {minioUrl} from "@/config/env";
const showViewer = ref(false)
const imgList = ref ([])
const isShowClusterRecord = ref(false)
const clusterRecord = ref([])
let progress = ref(0)
let progressVisible = ref(true)
const colors = [
  {color: '#6fb8d3', percentage: 0},
  {color: '#f56c6c', percentage: 20},
  {color: '#e6a23c', percentage: 40},
  {color: '#1989fa', percentage: 60},
  {color: '#6f7ad3', percentage: 80},
  {color: '#0dd01c', percentage: 100},
]

let miningData = ref(null)
const bestK = ref("")
const kmeans_result = ref("")
const closeViewer = function (){
  showViewer.value = false
  imgList.value = []
}
const onPreview = function (url) {
  showViewer.value = true
  imgList.value.push(url)
}

let isShowRecord = ref(false)
let treeData = ref([])
let ontologyName = ref("维修知识本体")
let kgType = ref("维修知识")
const currentNode = ref({})
let treeRef = ref(null)
let defaultProps = ref({
  children: 'children',
  label: 'name',
  isLeaf: 'leaf'
})


let currentFaultRecord = ref([])
let curRecord = ref({})
const route = useRoute()
onMounted(async () => {
  await getTreeNodes(3)
  await getFaultRecord(currentNode.value.name)
  await fetchNerResult(currentNode.value.name)
})

const getFaultRecord = async function (nodeName) {
  try {
    let response = await getFaultReportsByNode(nodeName)
    if (response.data.code === 0) {
      currentFaultRecord.value = response.data.data
    } else {
      ElMessage.error(response.data.msg)
    }
  } catch (error) {
    ElMessage.error(error)
  }
}

const getFaultRecordByCaseUuid = async function (caseUuidList) {
  clusterRecord.value = currentFaultRecord.value.filter((item) => caseUuidList.includes(item.uuid))
  isShowClusterRecord.value = true

}
const getTreeNodes = async function (nodeLevel) {
  try {
    let response = await reqTreeNodes(nodeLevel)
    if ((response.data.data) && (response.data.data.length > 0)) {
      treeData.value = []
      let data = response.data.data
      for (const treeNode of data) {
        if (treeNode.name === '变桨电机') {
          currentNode.value = treeNode
        }
      }
      treeData.value = convertToTree(data)
    }
  } catch (error) {
  }
}
const joinArr = function (arr) {
  return arr.join(' ')
}
const convertToTree = function (data) {
  const map = new Map();
  // 将所有节点添加到map中
  data.forEach(item => {
    map.set(item.nodeCode, {...item, children: []});
  });
  const tree = [];
  // 构建树结构
  data.forEach(item => {
    const nodeCodeParts = item.nodeCode.split('-');
    if (nodeCodeParts.length === 1) {
      // 如果节点没有父节点，直接加入树中
      tree.push(map.get(item.nodeCode));
    } else {
      // 否则将节点添加到父节点的children中
      const parentCode = nodeCodeParts.slice(0, -1).join('-');
      const parent = map.get(parentCode);
      if (parent) {
        parent.children.push(map.get(item.nodeCode));
      }
    }
  });
  return tree;
}
const handleCheckChange = (data, checked, indeterminate) => {
  console.log(data, checked, indeterminate);
}

const handleNodeClick = async function (data, node, treeNode, event) {
  currentNode.value = data
  progress.value = 0
  progressVisible.value = true
  await getFaultRecord(data.name)
  await fetchNerResult(data.name)
}
const handleClick = function (item) {
  curRecord.value = item
  isShowRecord.value = true
}


const fetchNerResult = async function (nodeName) {
  try {
    const response = await startMining(nodeName);
    const taskId = response.data.data;
    checkStatus(taskId);
  } catch (error) {
    ElMessage.error(error)
  }
}
const checkStatus = function (taskId) {
  let count = 0; // 初始化轮询次数计数器
  const maxAttempts = 20; // 设置最大尝试次数，假设20次
  ElMessage.success("任务已发送！")
  const interval = setInterval(async () => {
    try {
      count++; // 每次轮询时增加计数
      progress.value = (count / maxAttempts) * 100; // 更新进度条
      const resultResponse = await requestMiningResult(taskId);
      if (resultResponse.data) {
        if (resultResponse.data.data) {
          // 任务完成并返回了结果
          clearInterval(interval);
          progress.value = 100; // 完成时设置进度为100%
          miningData.value = resultResponse.data.data
          setTimeout(() => {
            kmeans_result.value = minioUrl + `kgfile/kmeans_result.png?t=${Date.now()}`
            bestK.value = minioUrl + `kgfile/best_k.png?t=${Date.now()}`
            progressVisible.value = false;
          }, 1500);
        }
        ElMessage.success(resultResponse.data.msg)
      }
      if (count >= maxAttempts) {
        // 达到最大尝试次数，停止轮询
        clearInterval(interval);
        progress.value = 0;
        ElMessage.warning('已达到最大尝试次数。');
      }
    } catch (error) {
      clearInterval(interval);
      ElMessage.error('发生错误，停止轮询。');
      progress.value = 0;
    }
  }, 10000); // 每10秒查询一次
};
</script>


<style lang="scss" scoped>
.extra {
  .top-page {
    display: flex;
    flex-direction: row;
    justify-content: flex-start; /* 内容靠左 */
    align-items: center; /* 垂直居中 */
  }

  .title {
    font-size: 20px;
    font-weight: bold;
  }

  .pagination {
    margin-top: 30px;
  }

  .top-text {
    color: #070707;
    font-size: 22px;
    font-weight: bold;
    position: relative;
  }

}

.treestyle {
  margin-top: -20 px;
}

.thisAside {
  background: white;
  height: 100% !important;
  overflow-y: auto;
  border: 1px solid #757373;
}

.box-card {
  border: 1px solid #ccc; /* 边框 */
}

.scrollbar-demo-item {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-bottom: 1px solid; // 添加一条下划线
  margin-bottom: 15px; // 增加下划线和文字之间的间距
}

.card-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.descriptions {
  width: 100%;
  margin-bottom: 30px;
}

.card-images {
  width: 45%;
}

.demo-progress .el-progress--line {
  margin-bottom: 15px;
  max-width: 80%;
}
</style>
