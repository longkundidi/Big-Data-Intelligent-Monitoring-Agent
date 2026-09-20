<template>
  <el-container style="position:relative;">
    <el-aside width="500px" style="margin-right: 15px;">
      <div class="mytree">
        <tree
            :treeData="treeData"
            @loadTreeData="loadTreeData"
            @clickNode="clickNode"
            @eventNodeAdd="dlgAddSonNode"
            @clearInfo="clearInfo"
            @metricManage="metricManage"
        >
        </tree>
        </div>
    </el-aside>
    <el-main>
      <div>
        <info ref="refInfo"
              v-if="isActivate"
              @update-node-data="onUpdateNodeData"
              @refreshData="refreshData"
              @clearInfo="clearInfo"
              :nodeData="currentNode"></info>
        <model-info ref="refModelInfo"
              v-if="isModelActivate"
              @update-node-data="onUpdateNodeData"
              @refreshData="refreshData"
              @clearInfo="clearInfo"
              :nodeData="currentNode"></model-info>
      </div>
    </el-main>

  </el-container>

  <el-dialog v-model="metricVisible" title="评价指标管理" width="40%" height="100%">
    <el-table :data="metricsTableData"  class="tableClass" >
      <el-table-column label="任务类型" prop="alType" />
      <el-table-column label="名称" prop="name" />
      <el-table-column align="right">
        <template #header>
          <div style="display: flex; align-items: center;">
            <el-input
                v-model="search_metric"
                size="small"
                placeholder="请输入任务类型或名称"
                @input="searchMetric"
                style="width: 200px; margin-right: 10px;"
            />
            <el-button class="addBtn"   @click="handleAdd">新增</el-button>
          </div>
        </template>
        <template #default="scope">
          <el-button  class="editBtn" @click="handleEdit(scope.$index, scope.row)">
            编辑
          </el-button>
          <el-button
              class="delBtn"
              @click="handleDelete(scope.$index, scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <div class="el-footer" style="display: flex;justify-content: flex-end;margin-top: 10px">
      <el-form :inline="true">
        <el-form-item>
          <el-pagination
              v-if="pageshow"
              class="wl-pagination"
              background
              layout="total, prev, pager, next"
              :current-page="currentPage"
              :page-sizes="[6, 10, 15, 20, 50, 100, 150, 200]"
              :page-size="pageSize"
              :total="count"
              @current-change="currentChangeHandle"
          />
        </el-form-item>
      </el-form>
    </div>

  </el-dialog>


  <el-dialog v-model="addDialog" title="评价指标" width="500">
    <el-form :model="metricForm">
      <el-form-item label="任务类型" label-width="140px">
        <el-input v-model="metricForm.alType" disabled/>
      </el-form-item>
      <el-form-item label="名称" label-width="140px">
        <el-input v-model="metricForm.name" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="addDialog = false;metricForm=''">取消</el-button>
        <el-button type="primary" @click="dataFormSubmit">确认</el-button>
      </div>
    </template>
  </el-dialog>

  <clean-add v-if="currentComponent === 'clean'" ref="cleanAdd" @refreshDataList="refreshData(currentComponent)" />
  <mining-add v-if="currentComponent === 'mining'" ref="miningAdd" @refreshDataList="refreshData(currentComponent)" />
  <dimension-add v-if="currentComponent === 'dimension'" ref="dimensionAdd" @refreshDataList="refreshData(currentComponent)" />
  <features-add v-if="currentComponent === 'features'" ref="featuresAdd" @refreshDataList="refreshData(currentComponent)" />


  <classify-add v-if="currentComponent === 'classify'" ref="classifyAdd" @refreshDataList="refreshData(currentComponent)" />
  <evaluation-add v-if="currentComponent === 'evaluation'" ref="evaluationAdd" @refreshDataList="refreshData(currentComponent)" />
  <diagnosis-add v-if="currentComponent === 'diagnosis'" ref="diagnosisAdd" @refreshDataList="refreshData(currentComponent)" />
  <decision-add v-if="currentComponent === 'decision'" ref="decisionAdd" @refreshDataList="refreshData(currentComponent)" />
  <scheduling-add v-if="currentComponent === 'scheduling'" ref="schedulingAdd" @refreshDataList="refreshData(currentComponent)" />
  <transmit-add v-if="currentComponent === 'transmit'" ref="transmitAdd" @refreshDataList="refreshData(currentComponent)" />

</template>

<script>
import tree from "./VTreeview.vue"
import info from "./infoView.vue"
import CleanAdd from '@/views/al/dataCleaning/dataCleaning-add.vue'
import MiningAdd from '@/views/al/dataMining/dataMining-add.vue'
import DimensionAdd from '@/views/al/dimensionalityReduction/dimensionalityReduction-add.vue'
import FeaturesAdd from '@/views/al/featureExtraction/featureExtraction-add.vue'
import modelInfo from "@/views/al/model/infoView.vue"
import ClassifyAdd from '@/views/al/conditionsClassification/conditionsClassification-add.vue'
import EvaluationAdd from '@/views/al/stateEvaluation/stateEvaluation-add.vue'
import DiagnosisAdd from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-add.vue'
import DecisionAdd from '@/views/al/maintenanceDecisions/maintenanceDecisions-add.vue'
import SchedulingAdd from '@/views/al/resourceScheduling/resourceScheduling-add.vue'
import TransmitAdd from '@/views/al/faultTransmit/faultTransmit-add.vue'

import {addMetrics, deleteMetrics, editMetrics, getById, getPage, searchByAltypeOrName} from "@/api/al/sysMetrics";

let globeParams = {}     //  声明一个全局参数对象

const typeToIndex = {
  clean: 0,
  features: 1,
  mining: 2,
  dimension: 3,
  classify: 4,
  evaluation: 5,
  diagnosis: 6,
  decision: 7,
  scheduling: 8,
  transmit: 9
}

export default {
  name: "overview",
  components: {
    tree,
    info,
    CleanAdd,
    MiningAdd,
    DimensionAdd,
    FeaturesAdd,
    modelInfo,
    ClassifyAdd,
    EvaluationAdd,
    DiagnosisAdd,
    DecisionAdd,
    SchedulingAdd,
    TransmitAdd
  },
  created() {
    this.loadTreeData()
  },
  data() {
    return {
      treeData: [],
      isActivate: false,
      currentNode: null,
      parentNode: null,
      currentComponent: null,
      isModelActivate: false,
      metricsTableData:[
        { alType: '类型1', name: '名称1' },
        { alType: '类型2', name: '名称2' },
      ],
      search_metric:'',
      metricVisible:false,
      addDialog:false,
      metricForm:{},
      count: 6,
      pageSize: 6,
      currentPage: 1,
      pageshow: true,
      currentAlType:'',
      isAdd:true,
      editForm:{}
    }
  },
  methods: {
    async loadTreeData() {
      try {
        const nodeTypes = [
          "clean", "features", "mining", "dimension",
          "classify", "evaluation", "diagnosis", "decision", "scheduling", "transmit"
        ];

        // 创建一个映射，储存所有子节点
        const childrenMap = {};
        for (const type of nodeTypes) {
          childrenMap[type] = await this.fetchNodeChildren(type)
        }

        // 使用动态生成的 treeData
        this.treeData = [
          { id: 0, label: `数据清洗算法(${childrenMap.clean.length}种)`, children: [
              { id: 'detection', label: '异常值检测', children: [], level: 2, showAdd: false },
              { id: 'filling', label: '缺失值填充', children: [], level: 2, showAdd: false },
              { id: 'standardize', label: '数据标准化', children: [], level: 2, showAdd: false },
              { id: 'relevance', label: '相关性分析', children: [], level: 2, showAdd: false },
            ], level: 1, showAdd: true, showMetrics: false },
          { id: 1, label: `特征提取算法(${childrenMap.features.length}种)`, children: [
              { id: 'timeFreq', label: '时频域特征提取', children: [], level: 2, showAdd: false },
              { id: 'text', label: '文本特征提取', children: [], level: 2, showAdd: false }
            ], level: 1, showAdd: true, showMetrics: false },
          { id: 2, label: `数据挖掘算法(${childrenMap.mining.length}种)`, children: childrenMap.mining, level: 1, showAdd: true, showMetrics: false },
          { id: 3, label: `数据降维算法(${childrenMap.dimension.length}种)`, children: childrenMap.dimension, level: 1, showAdd: true, showMetrics: false },
          { id: 4, label: `工况分类模型(${childrenMap.classify.length}种)`, children: childrenMap.classify, level: 1, showAdd: true, showMetrics: true },
          { id: 5, label: `状态评估模型(${childrenMap.evaluation.length}种)`, children: childrenMap.evaluation, level: 1, showAdd: true, showMetrics: true },
          { id: 6, label: `故障诊断模型(${childrenMap.diagnosis.length}种)`, children: childrenMap.diagnosis, level: 1, showAdd: true, showMetrics: true },
          { id: 7, label: `维修决策模型(${childrenMap.decision.length}种)`, children: childrenMap.decision, level: 1, showAdd: true, showMetrics: true },
          { id: 8, label: `资源调度模型(${childrenMap.scheduling.length}种)`, children: childrenMap.scheduling, level: 1, showAdd: true, showMetrics: true },
          { id: 9, label: `故障传递模型(${childrenMap.transmit.length}种)`, children: childrenMap.transmit, level: 1, showAdd: true, showMetrics: true }
        ];

        await Promise.all([
          ...this.treeData[0].children.map(parentNode => this.loadChildren(parentNode, childrenMap.clean)),
          ...this.treeData[1].children.map(parentNode => this.loadChildren(parentNode, childrenMap.features))
        ])
      } catch (error) {
        console.error('Error loading tree data:', error);
      }
    },


    fetchList(url) {
      return axios.get(url).then(response => response.data)
    },

    async fetchNodeChildren(type) {
      const apiConfig = {
        clean: { apiUrl: '/al/dataCleaning/list', labelField: 'alName' },
        mining: { apiUrl: '/al/dataMining/list', labelField: 'alName' },
        dimension: { apiUrl: '/al/dimensionalityReduction/list', labelField: 'alName' },
        features: { apiUrl: '/al/featureExtraction/list', labelField: 'alName' },
        classify: { apiUrl: '/al/conditionsClassification/list', labelField: 'modelName' },
        evaluation: { apiUrl: '/al/stateEvaluation/list', labelField: 'modelName' },
        diagnosis: { apiUrl: '/al/alFaultDiagnosisbase/list', labelField: 'modelName' },
        decision: { apiUrl: '/al/maintenanceDecisions/list', labelField: 'modelName' },
        scheduling: { apiUrl: '/al/resourceScheduling/list', labelField: 'modelName' },
        transmit: { apiUrl: '/al/faultTransmit/list', labelField: 'modelName' },
      }

      // 检查是否存在该 type 的配置
      const config = apiConfig[type]

      if (!config) {
        throw new Error('Invalid type provided for fetching node children.')
      }

      try {
        const response = await this.fetchList(config.apiUrl)

        if (response.code === "200") {
          // 处理返回的数据，转换成树节点格式
          return response.data.map(record => ({
            ...record,
            label: record[config.labelField], // 使用配置的 labelField
            parentType: type,
            showAdd: false,
            level: 3,
            model: config.labelField === 'modelName'
          }))
        } else {
          console.error(`Unexpected response code: ${response.message}`)
          return []
        }
      } catch (error) {
        console.error(`Error fetching ${type} node children:`, error)
        return []
      }
    },


    async loadChildren(parentNode, children) {
      try {
        // 根据 parentNode 的 id 过滤对应的子节点
        parentNode.children = children.filter(child => child.alType === parentNode.label)
        parentNode.label = `${parentNode.label} (${parentNode.children.length}种)`
      } catch (error) {
        console.error('Error loading feature extraction children:', error)
      }
    },

    clickNode(node) {
      if (node.level > 2) {
        this.currentNode = node

        this.isActivate = false
        this.isModelActivate = false

        if (!node.model) {
          this.isActivate = true
          this.$nextTick(() => {
            this.$refs.refInfo.updateTableData()
          })
        } else {
          this.isModelActivate = true
          this.$nextTick(() => {
            this.$refs.refModelInfo.getDetailInfo(node)
          })
        }
      }
    },

    onUpdateNodeData(updatedData, matchField) {

      // 更新树结构中的对应节点
      const updateInTree = (nodes, updatedData) => {
        for (let i = 0; i < nodes.length; i++) {

          if (nodes[i][matchField] === updatedData[matchField]) {
            nodes[i] = {...updatedData}
            return true
          } else if (nodes[i].children && nodes[i].children.length > 0) {
            // 递归查找子节点
            if (updateInTree(nodes[i].children, updatedData)) {
              return true
            }
          }
        }
        return false
      }

      for (const root of this.treeData) {
        if (root.children && updateInTree(root.children, updatedData)) {
          break
        }
      }

      this.clickNode(updatedData)
    },

    //  算法注册
    dlgAddSonNode (pNode, pData) {
      switch (pData.id) {
        case 0:
          this.currentComponent = 'clean'
          this.$nextTick(() => {
            this.$refs.cleanAdd.init()
          })
          break
        case 1:
          this.currentComponent = 'features'
          this.$nextTick(() => {
            this.$refs.featuresAdd.init()
          })
          break
        case 2:
          this.currentComponent = 'mining'
          this.$nextTick(() => {
            this.$refs.miningAdd.init()
          })
          break
        case 3:
          this.currentComponent = 'dimension'
          this.$nextTick(() => {
            this.$refs.dimensionAdd.init()
          })
          break
        case 4:
          this.currentComponent = 'classify'
          this.$nextTick(() => {
            this.$refs.classifyAdd.init()
          })
          break
        case 5:
          this.currentComponent = 'evaluation'
          this.$nextTick(() => {
            this.$refs.evaluationAdd.init()
          })
          break
        case 6:
          this.currentComponent = 'diagnosis'
          this.$nextTick(() => {
            this.$refs.diagnosisAdd.init()
          })
          break
        case 7:
          this.currentComponent = 'decision'
          this.$nextTick(() => {
            this.$refs.decisionAdd.init()
          })
          break
        case 8:
          this.currentComponent = 'scheduling'
          this.$nextTick(() => {
            this.$refs.schedulingAdd.init()
          })
          break
        case 9:
          this.currentComponent = 'transmit'
          this.$nextTick(() => {
            this.$refs.transmitAdd.init()
          })
          break
        default:
          console.error('错误的节点类型', pData.label)
      }
    },

    async refreshData(type) {
      let index = typeToIndex[type]

      if (index > -1 && index < 10) {
        try {
          switch (type) {
            case 'clean':
              await Promise.all(this.treeData[0].children.map(async parentNode => {
                const cleanChildren = await this.fetchNodeChildren("clean")
                await this.loadChildren(parentNode, cleanChildren)
              }))
              break
            case 'features':
              await Promise.all(this.treeData[1].children.map(async parentNode => {
                const featureChildren = await this.fetchNodeChildren("features")
                await this.loadChildren(parentNode, featureChildren)
              }))
              break
            default:
              this.treeData[index].children = await this.fetchNodeChildren(type)
              break
          }
        } catch (error) {
          console.error(`Error refreshing data for type ${type}:`, error)
        }
      } else {
        console.error('错误的节点类型', type)
      }
    },

    clearInfo(){
      this.isActivate = false
      this.isModelActivate = false
    },

    metricManage(pNode, pData){
      switch (pData.id) {
        case 0:
          this.currentComponent = 'classify'
          this.$nextTick(() => {
            this.currentAlType="分类任务"
          })
          break
        case 1:
          this.currentComponent = 'evaluation'
          this.$nextTick(() => {
            this.currentAlType="分类任务"
          })
          break
        case 2:
          this.currentComponent = 'diagnosis'
          this.$nextTick(() => {
            this.currentAlType="分类任务"
          })
          break
        case 3:
          this.currentComponent = 'decision'
          this.$nextTick(() => {
            this.currentAlType="分类任务"
          })
          break
        case 4:
          this.currentComponent = 'scheduling'
          this.$nextTick(() => {
            this.currentAlType="分类任务"
          })
          break
        case 5:
          this.currentComponent = 'transmit'
          this.$nextTick(() => {
            this.currentAlType="分类任务"
          })
          break
        default:
          console.error('错误的节点类型', pData.label)
      }
      this.$nextTick(() => {
        this.metricVisible = true;
        this.getMetrics(); // 确保在 metricVisible 更新后调用 getMetrics
      });
    },

    // 当前页
    currentChangeHandle(val) {
      this.currentPage = val
      this.getMetrics()
    },

    getMetrics(){
      getPage({
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        alType:this.currentAlType}).then(res=>{
        this.metricsTableData=res.data.data.records
        if(this.currentComponent==='extraction'){
          this.metricsTableData = res.data.data.records.filter(item=>item.name!=="accuracy")
        }
      })
    },

    //搜索评价指标
    searchMetric(){
      searchByAltypeOrName({content:this.search_metric}).then(res=>{
        this.metricsTableData=res.data.data
      })
    },

    //新增和编辑评价指标
    handleAdd(){
      this.addDialog=true
      this.isAdd=true
      this.metricForm.alType=this.currentAlType
    },

    handleEdit (index, row)  {
      this.addDialog=true
      this.isAdd=false
      this.editForm.id=row.id
      //获取指定id的指标详情
      getById(this.editForm.id).then(res=>{
        this.metricForm=res.data.data
      })
    },

    dataFormSubmit(){
      if(this.isAdd){
        addMetrics(this.metricForm).then(res=>{
          if(res.data.code==='200'){
            this.getMetrics()
            this.$message.success('新增评价指标成功')
          }
        }).catch((e)=>{
          this.$message.error(e)
        })
      }else{
        editMetrics({id:this.editForm.id,alType:this.metricForm.alType,name:this.metricForm.name}).then(res=>{
          if(res.data.code==='200'){
            this.getMetrics()
            this.$message.success('编辑评价指标成功')

          }
        }).catch((e)=>{
          this.$message.error(e)
        })
      }
      this.addDialog=false
      this.metricForm={}

    },

    //删除评价指标
    handleDelete  (index, row)  {
      deleteMetrics(row.id).then(res=>{
        if(res.data.code==='200'){
          this.getMetrics()
          this.$message.success('删除评价指标成功')
        }
      }).catch((e)=>{
        this.getMetrics()
        this.$message.error(e)
      })

    }


  }
}
</script>

<style lang="scss" scoped>
.el-aside, .el-main {
  height: 100vh;
  background-color: white;
}

el-main {
  display: flex;
  justify-content: center;
  align-items: center;
}

.mytree {
  height: 100%;
  flex-direction: column;
  overflow-y: auto;
  display: flex;
  align-items: center;
}

.table-container {
  position: fixed;
  top: 40%;
  left: 50%;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; /* 确保覆盖其他内容 */
  background-color: rgba(0, 0, 0, 0.3); /* 半透明背景 */
  width: 80%; /* 可以根据需要调整宽度 */
  max-width: 800px; /* 最大宽度限制 */
}
.tableClass {
  width: 100%;
  height: auto; /* 让表格高度自适应内容 */
  max-height:100%; /* 可选：设置最大高度，超过此高度时出现滚动条 */
  overflow-y: auto; /* 当内容超出最大高度时显示垂直滚动条 */
}

.wl-pagination {
  margin-top: 5px;
}
</style>
