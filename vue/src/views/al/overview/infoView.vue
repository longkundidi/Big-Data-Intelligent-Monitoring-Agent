<template>
  <el-row :gutter="10" class="centered-row">
    <el-col :span="8" style="margin-top: 20px;margin-bottom: 20px;">
      <el-image
          v-if="nodeData.iconUrl"
          style="width: 85%; height: 200px"
          :src="getImageSrc(nodeData)"
      >
        <template #error>
          <div class="image-slot" style="width: 100%;height:100%">
            <img src="@/assets/upload.png" style="width: 100%;height:100%">
          </div>
        </template>
      </el-image>
    </el-col>
    <el-col :span="12">
      <div class="container">
        <div class="content">
        <span :style="{ fontSize: '27px', color: '#444447', fontWeight: 'bold' }">
          {{nodeData.alName }}
        </span>
        </div>
        <div class="content">
          <span :style="{ fontWeight: 'bold', color: '#141415', fontSize: '15px' }">
            引用次数：{{ nodeData.alNum }}次
          </span>
        </div>
        <div class="content buttons-container">
          <el-button class="editBtn" @click="infoEdit()">修改算法</el-button>
          <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 && nodeData.isService===0"
                     class="editWorktBtn"
                     @click="alTest()">算法测试
          </el-button>
          <el-tooltip class="item" effect="dark" content="该算法已被停止使用,请先开启算法使用权限再测试">
            <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 && nodeData.isService===1"
                       class="editWorktBtn custom-disabled">算法测试
            </el-button>
          </el-tooltip>
          <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 "
                     class="viewBtn"
                     @click="alLog()">测试日志
          </el-button>
          <el-button class="delBtn" @click="deleteHandle(nodeData.id)">删除算法</el-button>
        </div>
      </div>
    </el-col>
  </el-row>

  <el-divider />

  <div class="centered-row">
    <el-table :data="tableData" size="large" border :show-header="false">
      <el-table-column prop="label" width="180" align="center" :fit="true">
        <template #default="scope">
            <span :style="{ fontWeight: 'bold', fontSize: '18px', color: '#909399'}">
              {{ scope.row.label }}
            </span>
        </template>
      </el-table-column>
      <el-table-column prop="value" :fit="true">
        <template #default="scope">
            <span :style="{ fontSize: '16px' }">
              {{ scope.row.value }}
            </span>
          <el-button class="normalBtn"
                     style="margin-left: 10px;"
                     v-if="scope.row.label==='JAR包大小'"
                     @click="downLoad_programUrl(nodeData.id,nodeData.parentType)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <clean-edit v-if="currentComponent === 'clean'" ref="cleanEdit" @refreshDataList="refreshData" />
  <mining-edit v-if="currentComponent === 'mining'" ref="miningEdit" @refreshDataList="refreshData" />
  <dimension-edit v-if="currentComponent === 'dimension'" ref="dimensionEdit" @refreshDataList="refreshData" />
  <features-edit v-if="currentComponent === 'features'" ref="featuresEdit" @refreshDataList="refreshData" />

  <clean-test v-if="cleanTestVisible" ref="cleanTest" @refreshDataList="refreshData" />
  <mining-test v-if="miningTestVisible" ref="miningTest" @refreshDataList="refreshData" />
  <dimension-test v-if="dimensionTestVisible" ref="dimensionTest" @refreshDataList="refreshData" />
  <features-test v-if="featuresTestVisible" ref="featuresTest" @refreshDataList="refreshData" />

  <clean-log v-if="cleanLogVisible" ref="cleanLog" @refreshDataList="refreshData" />
  <mining-log v-if="miningLogVisible" ref="miningLog" @refreshDataList="refreshData" />
  <dimension-log v-if="dimensionLogVisible" ref="dimensionLog" @refreshDataList="refreshData" />
  <features-log v-if="featuresLogVisible" ref="featuresLog" @refreshDataList="refreshData" />

</template>

<script>
import {delCleanObj, getCleanObj} from "@/api/al/dataCleaning/dataCleaning";
import {delExtractionObj, getExtractionObj} from "@/api/al/knowledgeExtraction/knowledgeExtraction";

import CleanEdit from '@/views/al/dataCleaning/dataCleaning-form.vue'
import MiningEdit from '@/views/al/dataMining/dataMining-form.vue'
import DimensionEdit from '@/views/al/dimensionalityReduction/dimensionalityReduction-form.vue'
import FeaturesEdit from '@/views/al/featureExtraction/featureExtraction-form.vue'

import CleanTest from '@/views/al/dataCleaning/dataCleaning-altest.vue'
import MiningTest from '@/views/al/dataMining/dataMining-altest.vue'
import DimensionTest from '@/views/al/dimensionalityReduction/dimensionalityReduction-altest.vue'
import FeaturesTest from '@/views/al/featureExtraction/featureExtraction-altest.vue'

import CleanLog from '@/views/al/dataCleaning/dataCleaning-allog.vue'
import MiningLog from '@/views/al/dataMining/dataMining-allog.vue'
import DimensionLog from '@/views/al/dimensionalityReduction/dimensionalityReduction-allog.vue'
import FeaturesLog from '@/views/al/featureExtraction/featureExtraction-allog.vue'

import {downLoad_Dataset_d} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {ElMessage} from "element-plus";
import {delDimensionalityObj, getDimensionalityObj} from "@/api/al/dimensionalityReduction/dimensionalityReduction";
import {delFeatureObj, getFeatureObj} from "@/api/al/featureExtraction/featureExtraction";
import {delMiningObj, getMiningObj} from "@/api/al/dataMining/dataMining";

export default {
  name: "infoView",
  props: {
    nodeData: {
      type: Object,
      required: true
    }
  },
  components: {
    CleanEdit,
    MiningEdit,
    DimensionEdit,
    FeaturesEdit,
    CleanTest,
    MiningTest,
    DimensionTest,
    FeaturesTest,
    CleanLog,
    MiningLog,
    DimensionLog,
    FeaturesLog

  },
  watch: {
    nodeData: {
      handler(newVal) {
        this.localNodeData = { ...newVal }
        this.updateTableData()
      },
      deep: true
    }
  },
  data() {
    return {
      localNodeData: { ...this.nodeData },
      tableData: [
        { label: '算法类型', value: this.nodeData.alType },
        { label: '算法适用', value: this.nodeData.alSuit },
        { label: '算法简介', value: this.nodeData.alBrief },
        { label: '输入数据', value: this.nodeData.input },
        { label: '输出数据', value: this.nodeData.output },
        { label: '提供人员', value: this.nodeData.creator },
        { label: '提供时间', value: this.nodeData.createTime },
        { label: '访问路径', value: this.nodeData.alUrl },
        { label: 'JAR包大小', value: this.nodeData.jarSize },
        { label: '运行所需资源', value: this.nodeData.runSize },
      ],
      programUrl:'',
      currentComponent: null,
      cleanTestVisible: false,
      miningTestVisible: false,
      dimensionTestVisible: false,
      featuresTestVisible: false,
      cleanLogVisible: false,
      miningLogVisible: false,
      dimensionLogVisible: false,
      featuresLogVisible: false,
      loading: false

    }
  },
  methods: {
    getImageSrc(node) {
      if (node.iconUrl) {
        return `/api/al/file/${node.iconUrl}`
      }
      return ''
    },

    updateTableData() {
      this.tableData = [
        { label: '算法类型', value: this.localNodeData.alType },
        { label: '算法适用', value: this.localNodeData.alSuit },
        { label: '算法简介', value: this.localNodeData.alBrief },
        { label: '输入数据', value: this.localNodeData.input },
        { label: '输出数据', value: this.localNodeData.output },
        { label: '提供人员', value: this.localNodeData.creator },
        { label: '提供时间', value: this.localNodeData.createTime },
        { label: '访问路径', value: this.localNodeData.alUrl },
        { label: 'JAR包大小', value: this.localNodeData.jarSize },
        { label: '运行所需资源', value: this.localNodeData.runSize },
      ]
    },

    infoEdit() {
      switch (this.nodeData.parentType) {
        case 'clean':
          this.currentComponent = 'clean'
          this.$nextTick(() => {
            this.$refs.cleanEdit.init(this.nodeData.id)
          })
          break
        case 'mining':
          this.currentComponent = 'mining'
          this.$nextTick(() => {
            this.$refs.miningEdit.init(this.nodeData.id)
          })
          break
        case 'dimension':
          this.currentComponent = 'dimension'
          this.$nextTick(() => {
            this.$refs.dimensionEdit.init(this.nodeData.id)
          })
          break
        case 'features':
          this.currentComponent = 'features'
          this.$nextTick(() => {
            this.$refs.featuresEdit.init(this.nodeData.id)
          })
          break
        default:
          console.error('错误的节点类型', this.nodeData.parentType)
      }
    },
    refreshData() {
      switch (this.nodeData.parentType) {
        case 'clean':
          getCleanObj(this.nodeData.id).then(response => {
            this.localNodeData = response.data.data
            this.localNodeData.parentType = "clean"
            this.localNodeData.label = this.localNodeData.alName
            this.updateTableData()
            this.$emit('update-node-data', this.localNodeData, 'alName')
          })
          break
        case 'mining':
          getMiningObj(this.nodeData.id).then(response => {
            this.localNodeData = response.data.data
            this.localNodeData.parentType = "mining"
            this.localNodeData.label = this.localNodeData.alName
            this.updateTableData()
            this.$emit('update-node-data', this.localNodeData, 'alName')
          })
          break
        case 'dimension':
          getDimensionalityObj(this.nodeData.id).then(response => {
            this.localNodeData = response.data.data
            this.localNodeData.parentType = "dimension"
            this.localNodeData.label = this.localNodeData.alName
            this.updateTableData()
            this.$emit('update-node-data', this.localNodeData, 'alName')
          })
          break
        case 'features':
          getFeatureObj(this.nodeData.id).then(response => {
            this.localNodeData = response.data.data
            this.localNodeData.parentType = "features"
            this.localNodeData.label = this.localNodeData.alName
            this.updateTableData()
            this.$emit('update-node-data', this.localNodeData, 'alName')
          })
          break
        default:
          console.error('错误的节点类型', this.nodeData.parentType)
      }
    },

    deleteHandle(id) {
      this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 创建一个 Promise 来处理不同类型的删除操作
        return new Promise((resolve, reject) => {
          switch (this.nodeData.parentType) {
            case 'clean':
              delCleanObj(id).then(resolve).catch(reject)
              break;
            case 'mining':
              delMiningObj(id).then(resolve).catch(reject)
              break;
            case 'dimension':
              delDimensionalityObj(id).then(resolve).catch(reject)
              break;
            case 'features':
              delFeatureObj(id).then(resolve).catch(reject)
              break;
            default:
              reject(new Error('错误的节点类型'))
          }
        })
      }).then(data => {
        this.$message.success('删除成功')
        this.$emit('refreshData', this.nodeData.parentType)
        this.$emit('clearInfo')
      }).catch(error => {
        this.$message.error('删除失败: ' + (error.message || '未知错误'))
      })
    },

    alTest(){
      switch (this.nodeData.parentType) {
        case 'clean':
          this.cleanTestVisible = true
          this.$nextTick(() => {
            this.$refs.cleanTest.init(this.nodeData.id, this.nodeData.input, this.nodeData.output)
          })
          break
        case 'mining':
          this.miningTestVisible = true
          this.$nextTick(() => {
            this.$refs.miningTest.init(this.nodeData.id, this.nodeData.input, this.nodeData.output)
          })
          break
        case 'dimension':
          this.dimensionTestVisible = true
          this.$nextTick(() => {
            this.$refs.dimensionTest.init(this.nodeData.id, this.nodeData.input, this.nodeData.output)
          })
          break
        case 'features':
          this.featuresTestVisible = true
          this.$nextTick(() => {
            this.$refs.featuresTest.init(this.nodeData.id, this.nodeData.input, this.nodeData.output)
          })
          break
        default:
          console.error('错误的节点类型', this.nodeData.parentType)
      }
    },

    alLog(){
      switch (this.nodeData.parentType) {
        case 'clean':
          this.cleanLogVisible = true
          this.$nextTick(() => {
            this.$refs.cleanLog.init(this.nodeData.id)
          })
          break
        case 'mining':
          this.miningLogVisible = true
          this.$nextTick(() => {
            this.$refs.miningLog.init(this.nodeData.id)
          })
          break
        case 'dimension':
          this.dimensionLogVisible = true
          this.$nextTick(() => {
            this.$refs.dimensionLog.init(this.nodeData.id)
          })
          break
        case 'features':
          this.featuresLogVisible = true
          this.$nextTick(() => {
            this.$refs.featuresLog.init(this.nodeData.id)
          })
          break
        default:
          console.error('错误的节点类型', this.nodeData.parentType)
      }
    },

    //下载程序包
    downLoad_programUrl(id,parentType){
      switch (parentType) {
        case 'clean':
          getCleanObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case  'mining':
          getMiningObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case 'dimension':
          getDimensionalityObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case  'features':
          getFeatureObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
      }
      if(this.programUrl==='' || this.programUrl===null){
        this.$notify.info('请先上传程序包，再下载程序包')
      }else{
        downLoad_Dataset_d({filePath:this.programUrl}).then(res=>{
          if(res.data===null){//如果数据集不在minio
            this.$notify.error('该路径下找不到程序包，请重新上传后再下载')
          }else{
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            let filename=res.config.params.filePath
            link.setAttribute('download', filename.split('/').pop());
            document.body.appendChild(link);
            link.click();
            this.$notify.success('开始下载')
          }
      }).catch(error => {
        console.error('下载文件时发生错误:', error);
        ElMessage.error('文件下载失败: ' + error.message);
      })
      }
    },


    closeDialog(){

    }


  }
}
</script>

<style lang="scss" scoped>
.el-row {
  text-align: center;
}
.el-row:last-child {
  margin-bottom: 0;
}

.container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 40px;
  margin-top: 20px;
}

.content {
  text-align: center;
}

.buttons-container {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.centered-row {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin: 0 auto;
}

.custom-disabled {
  background-color: #d3d3d3 !important;
  color: #8c8c8c !important;
  border-color: #d3d3d3 !important;
  cursor: not-allowed;
}

</style>
