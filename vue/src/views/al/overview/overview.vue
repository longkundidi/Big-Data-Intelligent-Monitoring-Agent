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
        <algorithm-detail ref="refModelInfo"
              v-if="isModelActivate"
              @update-node-data="onUpdateNodeData"
              @refreshData="refreshData"
              @clearInfo="clearInfo"
              :nodeData="currentNode"></algorithm-detail>
        <domain-model-info ref="refDomainModelInfo"
              v-if="isDomainModelActivate"
              @update-node-data="onUpdateNodeData"
              @update-tree="refreshDomainChildren"
              @clearInfo="clearInfo"
              :nodeData="currentNode"
              :parent-data="parentNode"></domain-model-info>
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
  <algorithm-form ref="algorithmForm" @saved="onAlgorithmSaved" />
  <diagnosis-add v-if="currentComponent === 'domainAlgorithm'" ref="domainAlgorithmAdd" @refreshDataList="refreshData(currentComponent)" />
  <decision-add v-if="currentComponent === 'decision'" ref="decisionAdd" @refreshDataList="refreshData(currentComponent)" />
  <scheduling-add v-if="currentComponent === 'scheduling'" ref="schedulingAdd" @refreshDataList="refreshData(currentComponent)" />
  <transmit-add v-if="currentComponent === 'transmit'" ref="transmitAdd" @refreshDataList="refreshData(currentComponent)" />
  <online-train v-if="currentComponent === 'domainModel'"
                ref="onlineTrain"
                :al-id="parentNode.id"
                :type="parentNode.parentType"
                :al-name="parentNode.modelName"
                @refreshTree="refreshDomainChildren(parentNode)" />

</template>

<script>
import tree from "./VTreeview.vue"
import info from "./infoView.vue"
import CleanAdd from '@/views/al/dataCleaning/dataCleaning-add.vue'
import MiningAdd from '@/views/al/dataMining/dataMining-add.vue'
import DimensionAdd from '@/views/al/dimensionalityReduction/dimensionalityReduction-add.vue'
import FeaturesAdd from '@/views/al/featureExtraction/featureExtraction-add.vue'
import AlgorithmDetail from './AlgorithmDetail.vue'
import AlgorithmForm from './AlgorithmForm.vue'
import ClassifyAdd from '@/views/al/conditionsClassification/conditionsClassification-add.vue'
import DiagnosisAdd from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-add.vue'
import DecisionAdd from '@/views/al/maintenanceDecisions/maintenanceDecisions-add.vue'
import SchedulingAdd from '@/views/al/resourceScheduling/resourceScheduling-add.vue'
import TransmitAdd from '@/views/al/faultTransmit/faultTransmit-add.vue'
import DomainModelInfo from '@/views/al/model/modelInfo.vue'
import OnlineTrain from '@/views/al/model/onlineTrain.vue'
import {fetchDomainModel} from "@/api/al/domainModel";
import {fetchAlgorithmMenuTree} from '@/api/al/algorithmMenu'

import {addMetrics, deleteMetrics, editMetrics, getById, getPage, searchByAltypeOrName} from "@/api/al/sysMetrics";

let globeParams = {}     //  声明一个全局参数对象

export default {
  name: "overview",
  components: {
    tree,
    info,
    CleanAdd,
    MiningAdd,
    DimensionAdd,
    FeaturesAdd,
    AlgorithmDetail,
    AlgorithmForm,
    ClassifyAdd,
    DiagnosisAdd,
    DecisionAdd,
    SchedulingAdd,
    TransmitAdd,
    DomainModelInfo,
    OnlineTrain
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
      isDomainModelActivate: false,
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
        const response = await fetchAlgorithmMenuTree()
        if (response.data.code !== '200') {
          throw new Error(response.data.message || '基础算法菜单加载失败')
        }

        const legacyIds = { evaluation: 5, diagnosis: 6 }
        this.treeData = (response.data.data || []).map(category => {
          const children = (category.children || [])
            .filter(child => child.algorithm)
            .map(child => ({
              ...child.algorithm,
              id: child.algorithmId,
              menuId: child.id,
              menuCode: child.code,
              label: child.name,
              parentType: category.algorithmType,
              showAdd: false,
              level: 3,
              model: true
            }))

          return {
            id: legacyIds[category.algorithmType],
            menuId: category.id,
            menuCode: category.code,
            label: `${category.name}(${children.length}种)`,
            parentType: category.algorithmType,
            children,
            level: 1,
            showAdd: true,
            showMetrics: true
          }
        })
      } catch (error) {
        console.error('Error loading tree data:', error);
      }
    },

    clickNode(node) {
      if (node.level > 2) {
        this.currentNode = node

        this.isActivate = false
        this.isModelActivate = false
        this.isDomainModelActivate = false

        if (node.domainModel) {
          this.isDomainModelActivate = true
          this.parentNode = this.findDomainParentNode(node)
          this.$nextTick(() => {
            this.$refs.refDomainModelInfo.getDetailInfo(node)
          })
        } else if (node.domainBase) {
          this.isModelActivate = true
          this.$nextTick(() => {
            this.$refs.refModelInfo.getDetailInfo(node)
            this.fetchDomainChildren(node)
          })
        } else if (!node.model) {
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

    async fetchDomainChildren(node) {
      try {
        const response = await fetchDomainModel({
          basicAlgorithm: node.modelName,
          modelType: node.parentType
        })

        if (response.data.code === "200") {
          node.children = response.data.data.map(record => ({
            ...record,
            label: record.modelName,
            parentType: node.parentType,
            domainModel: true,
            showAdd: false,
            children: [],
            level: 4
          }))
        }
      } catch (error) {
        console.error(`Error fetching domain model children for ${node.modelName}:`, error)
      }
    },

    refreshDomainChildren(node = this.parentNode) {
      if (node) {
        this.fetchDomainChildren(node)
      }
    },

    findDomainParentNode(targetNode) {
      const findNode = nodes => {
        for (const node of nodes) {
          if (node.domainBase && node.modelName === targetNode.basicAlgorithm && node.parentType === targetNode.parentType) {
            return node
          }
          if (node.children && node.children.length) {
            const found = findNode(node.children)
            if (found) return found
          }
        }
        return null
      }

      return findNode(this.treeData) || {}
    },

    onUpdateNodeData(updatedData, matchField) {

      // 更新树结构中的对应节点
      const updateInTree = (nodes, updatedData) => {
        for (let i = 0; i < nodes.length; i++) {

          if (nodes[i][matchField] === updatedData[matchField]
              && (!updatedData.parentType || nodes[i].parentType === updatedData.parentType)) {
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
      if (pData.id && String(pData.id).startsWith('domain-')) {
        this.currentComponent = 'domainAlgorithm'
        this.$nextTick(() => {
          this.$refs.domainAlgorithmAdd.init(pData.modelFunction, '数字孪生算法-模型')
        })
        return
      }

      if (pData.domainBase) {
        this.parentNode = pData
        this.currentComponent = 'domainModel'
        this.$nextTick(() => {
          this.$refs.onlineTrain.init()
        })
        return
      }

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
            this.$refs.algorithmForm.init('evaluation')
          })
          break
        case 6:
          this.currentComponent = 'diagnosis'
          this.$nextTick(() => {
            this.$refs.algorithmForm.init('diagnosis')
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
      if (type === 'domainModel') {
        await this.refreshDomainChildren(this.parentNode)
        return
      }

      if (type === 'domainAlgorithm') {
        await this.loadTreeData()
        return
      }

      if (type === 'evaluation' || type === 'diagnosis') {
        await this.loadTreeData()
        return
      }

      console.error('错误的节点类型', type)
    },

    async onAlgorithmSaved(type, algorithm) {
      await this.loadTreeData()
      const category = this.treeData.find(item => item.parentType === type)
      const node = category?.children?.find(item => String(item.id) === String(algorithm.id))
      if (node) {
        this.clickNode(node)
      }
    },

    clearInfo(){
      this.isActivate = false
      this.isModelActivate = false
      this.isDomainModelActivate = false
    },

    metricManage(pNode, pData){
      if (pData.parentType !== 'evaluation' && pData.parentType !== 'diagnosis') {
        console.error('错误的节点类型', pData.label)
        return
      }

      this.currentComponent = pData.parentType
      this.currentAlType = "分类任务"
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
