<template>
  <el-row :gutter="10" class="centered-row">
    <el-col :span="8" style="margin-top: 20px;margin-bottom: 20px;">
      <el-image
          v-if="nodeData.modelIcon || nodeData.iconUrl"
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
          {{ nodeData.modelName }}
        </span>
        </div>
        <div class="content">
          <span :style="{ fontWeight: 'bold', color: '#141415', fontSize: '15px' }">
            引用次数：{{ nodeData.modelNum }}次
          </span>
        </div>
        <div class="content buttons-container">
<!--          <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 && nodeData.isService===0"-->
<!--                     class="editWorktBtn" @click="onlineTrain()">在线训练</el-button>-->
          <el-button class="editBtn" @click="infoEdit()">修改模型</el-button>
          <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 && nodeData.isService===0"
                     class="editWorktBtn"
                     @click="alModelTest()">模型测试
          </el-button>
          <el-tooltip class="item" effect="dark" content="该模型已被停止使用,请先开启模型使用权限再测试">
            <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 && nodeData.isService===1"
                       class="editWorktBtn custom-disabled">模型测试
            </el-button>
          </el-tooltip>
          <el-button v-if="nodeData.isPass === 0 && nodeData.isDeployed === 0 "
                     class="viewBtn"
                     @click="alModelLog()">测试日志
          </el-button>
          <el-button class="delBtn" @click="deleteHandle(nodeData.id)">删除模型</el-button>
        </div>
      </div>
    </el-col>
  </el-row>

  <el-divider />

  <div class="centered-row">
    <div class="wrapper">
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">模型类型</span>
          <span class="value">{{ nodeData.modelType }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">适用场景</span>
          <span class="value">{{ nodeData.modelCondition }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">针对对象</span>
          <span class="value">{{ nodeData.modelObject }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">模型支持库</span>
          <el-tooltip placement="top">
            <template #content> {{ nodeData.modelLibrary }} </template>
            <span class="value">{{ nodeData.modelLibrary }}</span>
          </el-tooltip>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">模型优点</span>
          <span class="value">{{ nodeData.modelAdvantage }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">模型缺点</span>
          <span class="value">{{ nodeData.modelDisadvantage }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">访问路径</span>
          <span class="value">{{ nodeData.modelUrl }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">运行所需资源</span>
          <span class="value">{{ nodeData.runSize}}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="8" class="table-item">
          <span class="label">JAR包大小</span>
          <div style="display: flex;justify-content: center;align-items: center">
            <span class="value" style="display: inline-block; ">{{ nodeData.jarSize}}</span>
            <el-tooltip class="item" effect="dark" :content="nodeData.programUrl" placement="top">
              <el-button class="normalBtn"
                         style="margin-left: 15px;"
                         @click="downLoad_programUrl(nodeData.id,nodeData.parentType)">下载</el-button>
            </el-tooltip>
          </div>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">提供人员</span>
          <span class="value">{{ nodeData.modelProvider }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">提供时间</span>
          <span class="value">{{ nodeData.createTime }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '传统机器学习模型' || nodeData.modelType === '深度学习模型'">
        <el-col :span="8" class="table-item">
          <span class="label">模型框架</span>
          <span class="value">{{ detailData.modelFramework }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">设备</span>
          <span class="value">{{ detailData.device}}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">训练数据集</span>
          <el-tooltip class="item" effect="dark" :content="detailData.trainDataset" placement="top">
            <el-button class="normalBtn"
                       style="margin-top: 10px; margin-left: 30px"
                       @click="previewDataSet(detailData.trainDataset)">预览</el-button>
          </el-tooltip>
          <el-button class="normalBtn"
                     style="margin-top: 10px; margin-left: 10px"
                     @click="downLoad_Dataset(detailData.trainDataset)">下载</el-button>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '传统机器学习模型' || nodeData.modelType === '深度学习模型'">
        <el-col :span="8" class="table-item">
          <span class="label">测试数据集</span>
          <el-tooltip class="item" effect="dark" :content="detailData.testDataset" placement="top">
            <el-button class="normalBtn"
                       style="margin-top: 10px; margin-left: 30px"
                       @click="previewDataSet(detailData.testDataset)">预览</el-button>
          </el-tooltip>
          <el-button class="normalBtn"
                     style="margin-top: 10px; margin-left: 10px"
                     @click="downLoad_Dataset(detailData.testDataset)">下载</el-button>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">预处理方法</span>
          <span class="value">{{ detailData.pretreatment }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">样本类型</span>
          <span class="value">{{ detailData.sampleType }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '传统机器学习模型' || nodeData.modelType === '深度学习模型'">
        <el-col :span="8" class="table-item">
          <span class="label">样本尺寸</span>
          <span class="value">{{ detailData.sampleSize }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">训练次数</span>
          <span class="value">{{ detailData.trainTimes }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">训练批次</span>
          <span class="value">{{ detailData.trainBatch }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '传统机器学习模型' || nodeData.modelType === '深度学习模型'">
        <el-col :span="8" class="table-item">
          <span class="label">损失函数</span>
          <span class="value">{{ detailData.lossFunction }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">正则化</span>
          <span class="value">{{ detailData.regularization }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">学习率</span>
          <span class="value">{{ detailData.learningRate }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '传统机器学习模型' || nodeData.modelType === '深度学习模型'">
        <el-col :span="8" class="table-item">
          <span class="label">优化器</span>
          <span class="value">{{ detailData.optimizer }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">训练结果</span>
          <span class="value">{{ detailData.trainResults }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">测试结果</span>
          <span class="value">{{ detailData.testResults }}</span>
        </el-col>
      </el-row>

      <el-row class="table-col" v-if="nodeData.modelType === '统计学模型' || nodeData.modelType === '信号处理模型'">
        <el-col :span="12" class="table-item">
          <span class="label">预处理方法</span>
          <span class="value">{{ detailData.pretreatment }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">指标要求</span>
          <span class="value">{{ detailData.indicatorRequire}}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '统计学模型' || nodeData.modelType === '信号处理模型'">
        <el-col :span="12" class="table-item">
          <span class="label">样本类型</span>
          <span class="value">{{ detailData.sampleType }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">样本尺寸</span>
          <span class="value">{{ detailData.sampleSize }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.modelType === '统计学模型' || nodeData.modelType === '信号处理模型'">
        <el-col :span="8" class="table-item">
          <span class="label">阈值类型</span>
          <span class="value">{{ detailData.thresholdType }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">阈值描述</span>
          <span class="value">{{ detailData.thresholdDescription }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">超限次数</span>
          <span class="value">{{ detailData.overrunTimes }}</span>
        </el-col>
      </el-row>

    </div>
  </div>

  <classify-edit1 v-if="currentComponent === 'classify1'" ref="classifyEdit1" @success="refreshData" />
  <classify-edit2 v-if="currentComponent === 'classify2'" ref="classifyEdit2" @success="refreshData" />
  <evaluation-edit1 v-if="currentComponent === 'evaluation1'" ref="evaluationEdit1" @success="refreshData" />
  <evaluation-edit2 v-if="currentComponent === 'evaluation2'" ref="evaluationEdit2" @success="refreshData" />
  <diagnosis-edit1 v-if="currentComponent === 'diagnosis1'" ref="diagnosisEdit1" @success="refreshData" />
  <diagnosis-edit2 v-if="currentComponent === 'diagnosis2'" ref="diagnosisEdit2" @success="refreshData" />
  <decision-edit1 v-if="currentComponent === 'decision1'" ref="decisionEdit1" @success="refreshData" />
  <decision-edit2 v-if="currentComponent === 'decision2'" ref="decisionsEdit2" @success="refreshData" />
  <scheduling-edit1 v-if="currentComponent === 'scheduling1'" ref="schedulingEdit1" @success="refreshData" />
  <scheduling-edit2 v-if="currentComponent === 'scheduling2'" ref="schedulingEdit2" @success="refreshData" />
  <transmit-edit1 v-if="currentComponent === 'transmit1'" ref="transmitEdit1" @success="refreshData" />
  <transmit-edit2 v-if="currentComponent === 'transmit2'" ref="transmitEdit2" @success="refreshData" />

  <classify-test1 v-if="classifyTestVisible1" ref="classifyTest1" @success="refreshData" />
  <classify-test2 v-if="classifyTestVisible2" ref="classifyTest2" @success="refreshData" />
  <evaluation-test1 v-if="evaluationTestVisible1" ref="evaluationTest1" @success="refreshData" />
  <evaluation-test2 v-if="evaluationTestVisible2" ref="evaluationTest2" @success="refreshData" />
  <diagnosis-test1 v-if="diagnosisTestVisible1" ref="diagnosisTest1" @success="refreshData" />
  <diagnosis-test2 v-if="diagnosisTestVisible2" ref="diagnosisTest2" @success="refreshData" />
  <decision-test1 v-if="decisionTestVisible1" ref="decisionTest1" @success="refreshData" />
  <decision-test2 v-if="decisionTestVisible2" ref="decisionTest2" @success="refreshData" />
  <scheduling-test1 v-if="schedulingTestVisible1" ref="schedulingTest1" @success="refreshData" />
  <scheduling-test2 v-if="schedulingTestVisible2" ref="schedulingTest2" @success="refreshData" />
  <transmit-test1 v-if="transmitTestVisible1" ref="transmitTest1" @success="refreshData" />
  <transmit-test2 v-if="transmitTestVisible2" ref="transmitTest2" @success="refreshData" />

  <classify-log v-if="classifyLogVisible" ref="classifyLog" @success="refreshData" />
  <evaluation-log v-if="evaluationLogVisible" ref="evaluationLog" @success="refreshData" />
  <diagnosis-log v-if="diagnosisLogVisible" ref="diagnosisLog" @success="refreshData" />
  <decision-log v-if="decisionLogVisible" ref="decisionLog" @success="refreshData" />
  <scheduling-log v-if="schedulingLogVisible" ref="schedulingLog" @success="refreshData" />
  <transmit-log v-if="transmitLogVisible" ref="transmitLog" @success="refreshData" />

  <el-dialog v-model="showPreviewDialog"
             title="数据集预览"
             style="width: 35%;height: 60%"
             draggable
             @close="closeDialog()">
    <div v-loading="loading" element-loading-text="数据加载中...">
    <div class="dlgPreview">
      <el-card shadow="hover" style="width: 95%;">
        <el-table :data="previewData" stripe style="width: 100%">
          <el-table-column v-for="column in previewColumns" :key="column.prop" :prop="column.prop" :label="column.label">
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    </div>

  </el-dialog>

<!--  <online-train v-if="trainVisible"-->
<!--                ref="onlineTrain"-->
<!--                :al-id="nodeData.id"-->
<!--                :type="nodeData.parentType"-->
<!--                :al-name="nodeData.modelName"-->
<!--                @refreshTree="refreshTree"/>-->
</template>

<script>
import {getDiagnosisObj1} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisOne";
import {getEvaluationObj1} from "@/api/al/stateEvaluation/stateEvaluationOne";

import {getDiagnosisObj2} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisTwo";
import {getEvaluationObj2} from "@/api/al/stateEvaluation/stateEvaluationTwo";
import * as XLSX from "xlsx";
import {
  delEvaluationInput,
  delEvaluationObj,
  downLoad_Dataset_s, getEvaluationObj,
} from "@/api/al/stateEvaluation/stateEvaluationBase";
import {delDiagnosisObj, downLoad_Dataset_d, getDiagnosisObj} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
// import onlineTrain from "@/views/al/model/onlineTrain.vue"
import {ElMessage} from "element-plus";

import ClassifyEdit1 from '@/views/al/conditionsClassification/conditionsClassification-xiugai1.vue'
import ClassifyEdit2 from '@/views/al/conditionsClassification/conditionsClassification-xiugai2.vue'
import EvaluationEdit1 from '@/views/al/stateEvaluation/stateEvaluation-xiugai1.vue'
import EvaluationEdit2 from '@/views/al/stateEvaluation/stateEvaluation-xiugai2.vue'
import DiagnosisEdit1 from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-xiugai1.vue'
import DiagnosisEdit2 from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-xiugai2.vue'
import DecisionEdit1 from '@/views/al/maintenanceDecisions/maintenanceDecisions-xiugai1.vue'
import DecisionEdit2 from '@/views/al/maintenanceDecisions/maintenanceDecisions-xiugai2.vue'
import SchedulingEdit1 from '@/views/al/resourceScheduling/resourceScheduling-xiugai1.vue'
import SchedulingEdit2 from '@/views/al/resourceScheduling/resourceScheduling-xiugai2.vue'
import TransmitEdit1 from '@/views/al/faultTransmit/faultTransmit-xiugai1.vue'
import TransmitEdit2 from '@/views/al/faultTransmit/faultTransmit-xiugai2.vue'

import ClassifyTest1 from '@/views/al/conditionsClassification/conditionsClassification-altest.vue'
import ClassifyTest2 from '@/views/al/conditionsClassification/conditionsClassification-altest-notjson.vue'
import EvaluationTest1 from '@/views/al/stateEvaluation/stateEvaluation-altest.vue'
import EvaluationTest2 from '@/views/al/stateEvaluation/stateEvaluation-altest-notjson.vue'
import DiagnosisTest1 from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-altest.vue'
import DiagnosisTest2 from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-altest-notjson.vue'
import DecisionTest1 from '@/views/al/maintenanceDecisions/maintenanceDecisions-altest.vue'
import DecisionTest2 from '@/views/al/maintenanceDecisions/maintenanceDecisions-altest-notjson.vue'
import SchedulingTest1 from '@/views/al/resourceScheduling/resourceScheduling-altest.vue'
import SchedulingTest2 from '@/views/al/resourceScheduling/resourceScheduling-altest-notjson.vue'
import TransmitTest1 from '@/views/al/faultTransmit/faultTransmit-altest.vue'
import TransmitTest2 from '@/views/al/faultTransmit/faultTransmit-altest-notjson.vue'

import ClassifyLog from '@/views/al/stateEvaluation/stateEvaluation-allog'
import EvaluationLog from '@/views/al/stateEvaluation/stateEvaluation-allog'
import DiagnosisLog from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-allog'
import DecisionLog from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-allog'
import SchedulingLog from '@/views/al/stateEvaluation/stateEvaluation-allog'
import TransmitLog from '@/views/al/alFaultDiagnosis/alFaultDiagnosis-allog'
import {getClassificationObj1} from "@/api/al/conditionsClassification/conditionsClassificationOne";
import {getClassificationObj2} from "@/api/al/conditionsClassification/conditionsClassificationTwo";
import {getDecisionsObj1} from "@/api/al/maintenanceDecisions/maintenanceDecisionsOne";
import {getDecisionsObj2} from "@/api/al/maintenanceDecisions/maintenanceDecisionsTwo";
import {
  delClassificationObj,
  getClassificationObj
} from "@/api/al/conditionsClassification/conditionsClassificationBase";
import {delDecisionsObj, getDecisionsObj} from "@/api/al/maintenanceDecisions/maintenanceDecisionsBase";
import {delSchedulingObj, getSchedulingObj} from "@/api/al/resourceScheduling/resourceSchedulingBase";
import {delTransmitObj, getTransmitObj} from "@/api/al/faultTransmit/faultTransmitBase";
import {getSchedulingObj1} from "@/api/al/resourceScheduling/resourceSchedulingOne";
import {getSchedulingObj2} from "@/api/al/resourceScheduling/resourceSchedulingTwo";
import {getTransmitObj1} from "@/api/al/faultTransmit/faultTransmitOne";
import {getTransmitObj2} from "@/api/al/faultTransmit/faultTransmitTwo";

export default {
  name: "infoView",
  props: {
    nodeData: {
      type: Object,
      required: true
    }
  },
  components: {
    // onlineTrain,

    ClassifyEdit1,
    ClassifyEdit2,
    EvaluationEdit1,
    EvaluationEdit2,
    DiagnosisEdit1,
    DiagnosisEdit2,
    DecisionEdit1,
    DecisionEdit2,
    SchedulingEdit1,
    SchedulingEdit2,
    TransmitEdit1,
    TransmitEdit2,

    ClassifyTest1,
    ClassifyTest2,
    EvaluationTest1,
    EvaluationTest2,
    DiagnosisTest1,
    DiagnosisTest2,
    DecisionTest1,
    DecisionTest2,
    SchedulingTest1,
    SchedulingTest2,
    TransmitTest1,
    TransmitTest2,

    ClassifyLog,
    EvaluationLog,
    DiagnosisLog,
    DecisionLog,
    SchedulingLog,
    TransmitLog

  },
  watch: {
    nodeData: {
      handler(newVal) {
        this.localNodeData = { ...newVal }
      },
      deep: true
    }
  },
  data() {
    return {
      localNodeData: { ...this.nodeData },
      detailData: {},
      programUrl:'',
      currentComponent: null,
      previewData: [],
      previewColumns: [],
      showPreviewDialog: false,
      loading: false,
      // trainVisible: false,
      classifyTestVisible1: false,
      classifyTestVisible2: false,
      evaluationTestVisible1: false,
      evaluationTestVisible2: false,
      diagnosisTestVisible1: false,
      diagnosisTestVisible2: false,
      decisionTestVisible1: false,
      decisionTestVisible2: false,
      schedulingTestVisible1: false,
      schedulingTestVisible2: false,
      transmitTestVisible1: false,
      transmitTestVisible2: false,
      classifyLogVisible: false,
      evaluationLogVisible: false,
      diagnosisLogVisible: false,
      decisionLogVisible: false,
      schedulingLogVisible: false,
      transmitLogVisible: false,
    }
  },
  methods: {
    getImageSrc(node) {
      if (node.modelIcon) {
        return `/api/al/file/${node.modelIcon}`
      } else if (node.iconUrl) {
        return `/api/al/file/${node.iconUrl}`
      }
      return ''
    },

    getDetailInfo(node){
      this.detailData = {}
      const parentTypeToFunctionMap = {
        diagnosis: {
          '传统机器学习模型': getDiagnosisObj1,
          '深度学习模型': getDiagnosisObj1,
          '统计学模型': getDiagnosisObj2,
          '信号处理模型': getDiagnosisObj2
        },
        evaluation: {
          '传统机器学习模型': getEvaluationObj1,
          '深度学习模型': getEvaluationObj1,
          '统计学模型': getEvaluationObj2,
          '信号处理模型': getEvaluationObj2
        },
        classify: {
          '传统机器学习模型': getClassificationObj1,
          '深度学习模型': getClassificationObj1,
          '统计学模型': getClassificationObj2,
          '信号处理模型': getClassificationObj2
        },
        decision: {
          '传统机器学习模型': getDecisionsObj1,
          '深度学习模型': getDecisionsObj1,
          '统计学模型': getDecisionsObj2,
          '信号处理模型': getDecisionsObj2
        },
        scheduling: {
          '传统机器学习模型': getSchedulingObj1,
          '深度学习模型': getSchedulingObj1,
          '统计学模型': getSchedulingObj2,
          '信号处理模型': getSchedulingObj2
        },
        transmit: {
          '传统机器学习模型': getTransmitObj1,
          '深度学习模型': getTransmitObj1,
          '统计学模型': getTransmitObj2,
          '信号处理模型': getTransmitObj2
        }
      }

      const func = parentTypeToFunctionMap[node.parentType]?.[node.modelType]

      if (func) {
        func(node.modelName).then(res => {
          if (res.data.code === "200") {
            this.detailData = res.data.data
          } else {
            console.warn('Request failed with code:', res.data.code)
          }
        }).catch(error => {
          console.error('Error fetching data:', error)
        })
      } else {
        console.warn(`No handler for parentType: ${node.parentType}, modelType: ${node.modelType}`)
      }

    },

    // onlineTrain() {
    //   this.trainVisible = true
    //   this.$nextTick(() => {
    //     this.$refs.onlineTrain.init()
    //   })
    // },

    previewDataSet(url){
      this.showPreviewDialog = true
      this.loading=true
      this.fetchAndParseFile(url)
    },

    fetchAndParseFile(minioUrl) {
      if(minioUrl===''){
        this.$notify.error('请先上传数据集，再下载数据集')
      }else {
        axios.get(`/al/file/${minioUrl}`, {responseType: 'arraybuffer'})
            .then(response => {
              if (response.data == null) {
                this.$notify.error('该路径下找不到数据集，请重新上传后再下载')
              } else {
                const workbook = XLSX.read(response.data, {type: 'array'})
                const sheetName = workbook.SheetNames[0]
                const worksheet = workbook.Sheets[sheetName]
                const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1})

                // 提取列名
                this.previewColumns = jsonData[0].map((_, index) => ({
                  prop: `col${index}`,
                  label: jsonData[0][index]
                }))

                // 提取数据行
                this.previewData = jsonData.slice(1).map(row => {
                  const rowData = {}
                  row.forEach((value, index) => {
                    rowData[`col${index}`] = value;
                  })
                  return rowData
                })
                this.loading = false

                this.loadingInstance = false
              }

            })
            .catch(error => {
              console.error('Error fetching and parsing file:', error)
              this.loadingInstance = false
            })
      }
    },

    //下载数据集
    downLoad_Dataset(datasetUrl){
      if(datasetUrl==='' || datasetUrl===null){
        this.$notify.info('请先上传数据集，再下载数据集')
      }else {
        downLoad_Dataset_s({filePath: datasetUrl}).then(res => {
          if (res.data === null) {//如果数据集不在minio
            this.$notify.error('该路径下找不到数据集，请重新上传后再下载')
          } else {
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            let filename = res.config.params.filePath
            link.setAttribute('download', filename.split('/').pop());
            document.body.appendChild(link);
            link.click();
            this.$notify.success('开始下载')
          }
        }).catch(error => {
          console.error('下载文件时发生错误:', error);
          ElMessage.error('文件下载失败: ' + error.message);
        });
      }
    },

    //下载程序包
    downLoad_programUrl(id,parentType){
      switch (parentType) {
        case 'classify':
          getClassificationObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case 'evaluation':
          getEvaluationObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case 'diagnosis':
          getDiagnosisObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case 'decision':
          getDecisionsObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case 'scheduling':
          getSchedulingObj(id).then(res=>{
            this.programUrl=res.data.data.programUrl
          })
          break
        case 'transmit':
          getTransmitObj(id).then(res=>{
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

    infoEdit() {
      const parentTypeToComponentMap = {
        evaluation: {
          '传统机器学习模型': { component: 'evaluation1', ref: 'evaluationEdit1' },
          '深度学习模型': { component: 'evaluation1', ref: 'evaluationEdit1' },
          '统计学模型': { component: 'evaluation2', ref: 'evaluationEdit2' },
          '信号处理模型': { component: 'evaluation2', ref: 'evaluationEdit2' }
        },
        diagnosis: {
          '传统机器学习模型': { component: 'diagnosis1', ref: 'diagnosisEdit1' },
          '深度学习模型': { component: 'diagnosis1', ref: 'diagnosisEdit1' },
          '统计学模型': { component: 'diagnosis2', ref: 'diagnosisEdit2' },
          '信号处理模型': { component: 'diagnosis2', ref: 'diagnosisEdit2' }
        },
        classify: {
          '传统机器学习模型': { component: 'classify1', ref: 'classifyEdit1' },
          '深度学习模型': { component: 'classify1', ref: 'classifyEdit1' },
          '统计学模型': { component: 'classify2', ref: 'classifyEdit2' },
          '信号处理模型': { component: 'classify2', ref: 'classifyEdit2' }
        },
        decision: {
          '传统机器学习模型': { component: 'decision1', ref: 'decisionEdit1' },
          '深度学习模型': { component: 'decision1', ref: 'decisionEdit1' },
          '统计学模型': { component: 'decision2', ref: 'decisionEdit2' },
          '信号处理模型': { component: 'decision2', ref: 'decisionEdit2' }
        },
        scheduling: {
          '传统机器学习模型': { component: 'scheduling1', ref: 'schedulingEdit1' },
          '深度学习模型': { component: 'scheduling1', ref: 'schedulingEdit1' },
          '统计学模型': { component: 'scheduling2', ref: 'schedulingEdit2' },
          '信号处理模型': { component: 'scheduling2', ref: 'schedulingEdit2' }
        },
        transmit: {
          '传统机器学习模型': { component: 'transmit1', ref: 'transmitEdit1' },
          '深度学习模型': { component: 'transmit1', ref: 'transmitEdit1' },
          '统计学模型': { component: 'transmit2', ref: 'transmitEdit2' },
          '信号处理模型': { component: 'transmit2', ref: 'transmitEdit2' }
        }
      }

      const config = parentTypeToComponentMap[this.nodeData.parentType]?.[this.nodeData.modelType]

      if (config) {
        this.currentComponent = config.component
        this.$nextTick(() => {
          this.$refs[config.ref]?.init(this.nodeData.id)
        })
      } else {
        console.error('错误的节点类型或模型类型', this.nodeData)
      }
    },

    refreshData() {
      const parentTypeToFunctionMap = {
        evaluation: {
          main: getEvaluationObj,
          '传统机器学习模型': getEvaluationObj1,
          '深度学习模型': getEvaluationObj1,
          '统计学模型': getEvaluationObj2,
          '信号处理模型': getEvaluationObj2
        },
        diagnosis: {
          main: getDiagnosisObj,
          '传统机器学习模型': getDiagnosisObj1,
          '深度学习模型': getDiagnosisObj1,
          '统计学模型': getDiagnosisObj2,
          '信号处理模型': getDiagnosisObj2
        },
        classify: {
          main: getClassificationObj,
          '传统机器学习模型': getClassificationObj1,
          '深度学习模型': getClassificationObj1,
          '统计学模型': getClassificationObj2,
          '信号处理模型': getClassificationObj2
        },
        decision: {
          main: getDecisionsObj,
          '传统机器学习模型': getDecisionsObj1,
          '深度学习模型': getDecisionsObj1,
          '统计学模型': getDecisionsObj2,
          '信号处理模型': getDecisionsObj2
        },
        scheduling: {
          main: getSchedulingObj,
          '传统机器学习模型': getSchedulingObj1,
          '深度学习模型': getSchedulingObj1,
          '统计学模型': getSchedulingObj2,
          '信号处理模型': getSchedulingObj2
        },
        transmit: {
          main: getTransmitObj,
          '传统机器学习模型': getTransmitObj1,
          '深度学习模型': getTransmitObj1,
          '统计学模型': getTransmitObj2,
          '信号处理模型': getTransmitObj2
        }
      }

      const config = parentTypeToFunctionMap[this.nodeData.parentType]

      if (config) {
        // 调用主函数获取基本数据
        config.main(this.nodeData.id).then(response => {
          this.localNodeData = response.data.data
          this.localNodeData.parentType = this.nodeData.parentType
          this.localNodeData.label = this.localNodeData.modelName
          this.$emit('update-node-data', this.localNodeData, 'modelName')
        }).catch(error => {
          console.error('Error fetching main data:', error)
        })

        // 根据modelType调用特定的详细数据获取函数
        const detailFunc = config[this.nodeData.modelType]
        if (detailFunc) {
          detailFunc(this.nodeData.modelName).then(response => {
            this.detailData = response.data.data
          }).catch(error => {
            console.error('Error fetching detailed data:', error)
          })
        } else {
          console.warn(`No handler for modelType: ${this.nodeData.modelType}`)
        }
      } else {
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
            case 'classify':
              delClassificationObj(id).then(resolve).catch(reject)
              break;
            case 'evaluation':
              delEvaluationObj(id)
                  .then(() => delEvaluationInput(this.nodeData.modelName, "alStateEvaluation"))
                  .then(resolve)
                  .catch(reject)
              break;
            case 'diagnosis':
              delDiagnosisObj(id).then(resolve).catch(reject)
              break;
            case 'decision':
              delDecisionsObj(id).then(resolve).catch(reject)
              break;
            case 'scheduling':
              delSchedulingObj(id).then(resolve).catch(reject)
              break;
            case 'transmit':
              delTransmitObj(id).then(resolve).catch(reject)
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

    alModelTest(){
      const parentTypeToTestComponentMap = {
        evaluation: {
          0: { visibleProp: 'evaluationTestVisible1', ref: 'evaluationTest1' },
          1: { visibleProp: 'evaluationTestVisible2', ref: 'evaluationTest2' }
        },
        diagnosis: {
          0: { visibleProp: 'diagnosisTestVisible1', ref: 'diagnosisTest1' },
          1: { visibleProp: 'diagnosisTestVisible2', ref: 'diagnosisTest2' }
        },
        classify: {
          0: { visibleProp: 'classifyTestVisible1', ref: 'classifyTest1' },
          1: { visibleProp: 'classifyTestVisible2', ref: 'classifyTest2' }
        },
        decision: {
          0: { visibleProp: 'decisionTestVisible1', ref: 'decisionTest1' },
          1: { visibleProp: 'decisionTestVisible2', ref: 'decisionTest2' }
        },
        scheduling: {
          0: { visibleProp: 'schedulingTestVisible1', ref: 'schedulingTest1' },
          1: { visibleProp: 'schedulingTestVisible2', ref: 'schedulingTest2' }
        },
        transmit: {
          0: { visibleProp: 'transmitTestVisible1', ref: 'transmitTest1' },
          1: { visibleProp: 'transmitTestVisible2', ref: 'transmitTest2' }
        }
      }

      const config = parentTypeToTestComponentMap[this.nodeData.parentType]?.[this.nodeData.isjson]

      if (config) {
        this[config.visibleProp] = true
        this.$nextTick(() => {
          this.$refs[config.ref]?.init(
              this.nodeData.id,
              this.nodeData.input,
              this.nodeData.output,
              this.nodeData.isjson
          )
        })
      } else {
        console.error('错误的节点类型或isjson值', this.nodeData)
      }
    },

    alModelLog(){
      const parentTypeToLogComponentMap = {
        evaluation: { visibleProp: 'evaluationLogVisible', ref: 'evaluationLog' },
        diagnosis: { visibleProp: 'diagnosisLogVisible', ref: 'diagnosisLog' },
        classify: { visibleProp: 'classifyLogVisible', ref: 'classifyLog' },
        decision: { visibleProp: 'decisionLogVisible', ref: 'decisionLog' },
        scheduling: { visibleProp: 'schedulingLogVisible', ref: 'schedulingLog' },
        transmit: { visibleProp: 'transmitLogVisible', ref: 'transmitLog' }
      }

      const config = parentTypeToLogComponentMap[this.nodeData.parentType]

      if (config) {
        this[config.visibleProp] = true
        this.$nextTick(() => {
          this.$refs[config.ref]?.init(this.nodeData.id)
        })
      } else {
        console.error('错误的节点类型', this.nodeData.parentType)
      }

    },

    closeDialog(){
      this.previewData=[]
    },

    refreshTree(){
      this.filterText = ''
      // this.$emit('update-tree', 2)
      // this.trainVisible = false
    },
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
}

.wrapper {
  margin-top: 10px;
  width: 83%;
  border: solid 1px #d9d9d9;

  .table-col:not(:last-child) {
    border-bottom: solid 1px #d9d9d9;
  }

  .table-item:not(:last-child) {
    border-right: solid 1px #d9d9d9;
  }

  .table-col {
    width: 950px;
    display: flex;
  }

  .table-item {
    height: 100%;
    display: flex;

    .label {
      display: flex;
      justify-content: center;
      background: #FAFAFA;
      color: #909399;
      padding: 10px 7px;
      border-right: solid 1px #d9d9d9;
      font-weight: bold;
      font-size: 18px;
      width: 100px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .value {
      padding: 3px;
      width: 100%;
      display: flex;
      align-items: center;
      word-wrap: break-word;
      color: #606266;
      font-size: 16px;
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.dlgPreview{
  height: 450px;
  overflow: auto;
  margin-left: 20px;
}

</style>
