<template>
  <el-container style="position:relative;">
    <el-aside width="500px" style="margin-right: 15px;">
      <div class="mytree">
        <tree
            :treeData="treeData"
            @loadTreeData="loadTreeData"
            @clickNode="clickNode"
            @eventNodeAdd="dlgAddSonNode"
            @metricManage="metricManage"
            @clearInfo="clearInfo"
        >
        </tree>
      </div>
    </el-aside>
    <el-main>
      <div>
        <info ref="refInfo"
              v-if="isActivate"
              @update-node-data="onUpdateNodeData"
              @update-tree="onUpdateTree"
              @refreshData="refreshData"
              @clearInfo="clearInfo"
              :nodeData="currentNode"></info>
        <model-info ref="refModelInfo"
                    v-if="isModelActivate"
                    @update-node-data="onUpdateNodeData"
                    @update-tree="onUpdateTree"
                    @clearInfo="clearInfo"
                    :nodeData="currentNode"
                    :parent-data="parentNode"></model-info>
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

  <classify-add v-if="currentComponent === 'classify'" ref="classifyAdd" @refreshDataList="refreshData(currentComponent)" />
  <evaluation-add v-if="currentComponent === 'evaluation'" ref="evaluationAdd" @refreshDataList="refreshData(currentComponent)" />
  <diagnosis-add v-if="currentComponent === 'diagnosis'" ref="diagnosisAdd" @refreshDataList="refreshData(currentComponent)" />
  <decision-add v-if="currentComponent === 'decision'" ref="decisionAdd" @refreshDataList="refreshData(currentComponent)" />
  <scheduling-add v-if="currentComponent === 'scheduling'" ref="schedulingAdd" @refreshDataList="refreshData(currentComponent)" />
  <transmit-add v-if="currentComponent === 'transmit'" ref="transmitAdd" @refreshDataList="refreshData(currentComponent)" />


</template>

<script>
import tree from "@/views/al/overview/VTreeview.vue"
import info from "@/views/al/model/infoView.vue"
import modelInfo from "./modelInfo.vue"
import ClassifyAdd from '@/views/al/conditionsClassification/conditionsClassification-add.vue'
import EvaluationAdd from '@/views/al/stateEvaluation/stateEvaluation-add.vue'
import DiagnosisAdd from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-add.vue'
import DecisionAdd from '@/views/al/maintenanceDecisions/maintenanceDecisions-add.vue'
import SchedulingAdd from '@/views/al/resourceScheduling/resourceScheduling-add.vue'
import TransmitAdd from '@/views/al/faultTransmit/faultTransmit-add.vue'
import {fetchDomainModel} from "@/api/al/domainModel";
import {addMetrics, deleteMetrics, editMetrics, getById, getPage, searchByAltypeOrName} from "@/api/al/sysMetrics";

const typeToIndex = {
  classify: 0,
  evaluation: 1,
  diagnosis: 2,
  decision: 3,
  scheduling: 4,
  transmit: 5
}

export default {
  name: "overview",
  components: {
    tree,
    info,
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
      isModelActivate: false,
      currentNode: null,
      parentNode: null,
      currentComponent: null,
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
        const classifyChildren = await this.fetchNodeChildren("classify")
        const evaluationChildren = await this.fetchNodeChildren("evaluation")
        const diagnosisChildren = await this.fetchNodeChildren("diagnosis")
        const decisionChildren = await this.fetchNodeChildren("decision")
        const schedulingChildren = await this.fetchNodeChildren("scheduling")
        const transmitChildren = await this.fetchNodeChildren("transmit")

        this.treeData = [
          { id: 0, label: `工况分类模型(${classifyChildren.length}种)`, children: classifyChildren, level: 1, showAdd: true, showMetrics:true },
          { id: 1, label: `状态评估模型(${evaluationChildren.length}种)`, children: evaluationChildren, level: 1, showAdd: true, showMetrics:true },
          { id: 2, label: `故障诊断模型(${diagnosisChildren.length}种)`, children: diagnosisChildren, level: 1, showAdd: true, showMetrics:true },
          { id: 3, label: `维修决策模型(${decisionChildren.length}种)`, children: decisionChildren, level: 1, showAdd: true, showMetrics:true },
          { id: 4, label: `资源调度模型(${schedulingChildren.length}种)`, children: schedulingChildren, level: 1, showAdd: true, showMetrics:true },
          { id: 5, label: `故障传递模型(${transmitChildren.length}种)`, children: transmitChildren, level: 1, showAdd: true, showMetrics:true }
        ]
      } catch (error) {
        console.error('Error loading tree data:', error)
      }
    },

    fetchList(url) {
      return axios.get(url).then(response => response.data)
    },

    async fetchNodeChildren(type) {
      let apiUrl
      switch (type) {
        case 'classify':
          apiUrl = '/al/conditionsClassification/list'
          break
        case 'evaluation':
          apiUrl = '/al/stateEvaluation/list'
          break
        case 'diagnosis':
          apiUrl = '/al/alFaultDiagnosisbase/list'
          break
        case 'decision':
          apiUrl = '/al/maintenanceDecisions/list'
          break
        case 'scheduling':
          apiUrl = '/al/resourceScheduling/list'
          break
        case 'transmit':
          apiUrl = '/al/faultTransmit/list'
          break
        default:
          throw new Error('Invalid type provided for fetching node children.')
      }

      try {
        const response = await this.fetchList(apiUrl)

        if(response.code === "200"){
          // 处理返回的数据，转换成树节点格式
          return response.data.map(record => ({
            ...record,
            label: record['modelName'],
            parentType: type,
            showAdd: false,
            level: 2
          }))
        }else {
          console.error(`Unexpected response code: ${response.message}`)
          return []
        }
      } catch (error) {
        console.error(`Error fetching ${type} node children:`, error)
        return []
      }
    },

    async fetchSubNodeChildren(node) {
      try {
        const response = await fetchDomainModel({
          basicAlgorithm: node.modelName,
          modelType: node.parentType
        })

        if (response.data.code === "200") {
          // 处理返回的数据，转换成树节点格式
          node.children = response.data.data.map(record => ({
            ...record,
            label: record.modelName,
            parentType: node.parentType,
            showAdd: false,
            children: [],
            level: node.level + 1
          }))
          this.$forceUpdate()
        } else {
          console.error(`Unexpected response code: ${response.data.message}`);
        }
      } catch (error) {
        console.error(`Error fetching sub nodes for ${node.parentType}:`, error);
      }
    },

    findParentNode(targetNode) {
      const { parentType, basicAlgorithm } = targetNode

      if (typeToIndex.hasOwnProperty(parentType)) {
        return this.findNodeInTree(this.treeData[typeToIndex[parentType]].children, basicAlgorithm)
      } else {
        return null
      }
    },

    findNodeInTree(nodes, basicAlgorithm) {
      for (const node of nodes) {
        if (node.modelName === basicAlgorithm) {
          return node
        }
        if (node.children && node.children.length > 0) {
          const result = this.findNodeInTree(node.children, basicAlgorithm)
          if (result) {
            return result
          }
        }
      }
      return null
    },


    clickNode(node) {
      if(node.level !== 1){
        this.currentNode = node
        if (node.level === 2) {
          this.fetchSubNodeChildren(node)
          this.isActivate = true
          this.isModelActivate = false
          this.$nextTick(() => {
            this.$refs.refInfo.getDetailInfo(node)
          })
        }

        if (node.level === 3){
          this.isActivate = false
          this.isModelActivate = true
          this.parentNode = this.findParentNode(node)
          this.$nextTick(() => {
            this.$refs.refModelInfo.getDetailInfo(node)
          })
        }
      }
    },

    onUpdateNodeData(updatedData) {
      // 更新树结构中的对应节点
      const updateInTree = (nodes, updatedData) => {
        for (let i = 0; i < nodes.length; i++) {
          let matchField = 'modelName'

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

    onUpdateTree(node){
      if(node === 2){
        this.fetchSubNodeChildren(this.currentNode)
      }else {
        this.fetchSubNodeChildren(node)
      }
    },

    dlgAddSonNode (pNode, pData) {
      switch (pData.id) {
        case 0:
          this.currentComponent = 'classify'
          this.$nextTick(() => {
            this.$refs.classifyAdd.init()
          })
          break
        case 1:
          this.currentComponent = 'evaluation'
          this.$nextTick(() => {
            this.$refs.evaluationAdd.init()
          })
          break
        case 2:
          this.currentComponent = 'diagnosis'
          this.$nextTick(() => {
            this.$refs.diagnosisAdd.init()
          })
          break
        case 3:
          this.currentComponent = 'decision'
          this.$nextTick(() => {
            this.$refs.decisionAdd.init()
          })
          break
        case 4:
          this.currentComponent = 'scheduling'
          this.$nextTick(() => {
            this.$refs.schedulingAdd.init()
          })
          break
        case 5:
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

      if (index > -1 || index < 10) {
        try {
          this.treeData[index].children = await this.fetchNodeChildren(type)
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
</style>
