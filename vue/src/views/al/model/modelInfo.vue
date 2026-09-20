<template>
  <el-row :gutter="10" class="centered-row">
    <el-col :span="8" style="margin-top: 20px;margin-bottom: 20px;">
      <el-image
          v-if="nodeData.modelIcon"
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
    <el-col :span="10">
      <div class="container">
        <div class="content">
        <span :style="{ fontSize: '27px', color: '#444447', fontWeight: 'bold' }">
          {{nodeData.modelName }}
        </span>
        </div>
        <div class="content">
          <span :style="{ fontWeight: 'bold', color: '#141415', fontSize: '15px' }">
            引用次数：{{ nodeData.modelNum }}次
          </span>
        </div>
        <div class="content buttons-container">
          <el-button class="editBtn" @click="infoEdit()">修改模型</el-button>
          <el-button v-if="nodeData.isService===0"
                     class="editWorktBtn"
                     @click="alModelTest()">模型测试
          </el-button>
          <el-tooltip class="item" effect="dark" content="该模型已被停止使用,请先开启模型使用权限再测试">
            <el-button v-if="nodeData.isService===1"
                       class="editWorktBtn custom-disabled">模型测试
            </el-button>
          </el-tooltip>
          <el-button class="viewBtn"
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
          <span class="value">{{ parentData.modelType }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">适用工况</span>
          <span class="value">{{ nodeData.modelUsecase }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">基础算法</span>
          <span class="value">{{ nodeData.basicAlgorithm }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">针对对象</span>
          <span class="value">{{ nodeData.modelObject }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">模型支持库</span>
          <el-tooltip placement="top">
            <template #content> {{ parentData.modelLibrary }} </template>
            <span class="value">{{ parentData.modelLibrary }}</span>
          </el-tooltip>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">访问路径</span>
          <span class="value">{{ nodeData.modelUrl }}</span>
        </el-col>
      </el-row>

      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">JAR包大小</span>
          <div style="display: flex;justify-content: center;align-items: center">
            <span class="value" style="display: inline-block; ">{{ nodeData.jarSize}}</span>
            <el-tooltip class="item" effect="dark" :content="nodeData.programUrl" placement="top">
              <el-button class="normalBtn"
                       style="margin-left: 15px;"
                       @click="downLoad_programUrl(nodeData.programUrl)">下载</el-button>
            </el-tooltip>
          </div>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">运行所需资源</span>
          <span class="value">{{ nodeData.runSize}}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="24" class="table-item">
          <span class="label">数据集说明</span>
          <span class="value">{{ nodeData.datasetMemo }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="8" class="table-item">
          <span class="label">样本类型</span>
          <span class="value">{{ detailData.sampleType }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">训练数据集</span>
          <el-tooltip class="item" effect="dark" :content="nodeData.trainDataset" placement="top">
            <el-button class="normalBtn"
                       style="margin-top: 10px; margin-left: 30px"
                       @click="previewDataSet(nodeData.trainDataset)">预览</el-button>
          </el-tooltip>
          <el-button class="normalBtn"
                     style="margin-top: 10px; margin-left: 10px"
                     @click="downLoad_Dataset(nodeData.trainDataset)">下载</el-button>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">测试数据集</span>
          <el-tooltip class="item" effect="dark" :content="nodeData.testDataset" placement="top">
            <el-button class="normalBtn"
                       style="margin-top: 10px; margin-left: 30px"
                       @click="previewDataSet(nodeData.testDataset)">预览</el-button>
          </el-tooltip>
          <el-button class="normalBtn"
                     style="margin-top: 10px; margin-left: 10px"
                     @click="downLoad_Dataset(nodeData.testDataset)">下载</el-button>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="8" class="table-item">
          <span class="label">模型框架</span>
          <span class="value">{{ detailData.modelFramework }}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">设备</span>
          <span class="value">{{ detailData.device}}</span>
        </el-col>
        <el-col :span="8" class="table-item">
          <span class="label">预处理方法</span>
          <span class="value">{{ detailData.pretreatment }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
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
      <el-row class="table-col">
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
      <el-row class="table-col">
        <el-col :span="8" class="table-item">
          <span class="label">优化器</span>
          <span class="value">{{ detailData.optimizer }}</span>
        </el-col>
        <el-col :span="16" class="table-item">
          <span class="label">评价指标结果</span>
          <el-tooltip placement="top">
            <template #content> {{ nodeData.metricsResult }} </template>
            <span class="value">{{ nodeData.metricsResult }}</span>
          </el-tooltip>
        </el-col>
      </el-row>

    </div>
  </div>

  <edit v-if="editVisible" ref="edit" @success="refreshData" />
  <test v-if="testVisible" ref="test" @success="refreshData" />
  <log v-if="logVisible" ref="log" @refreshDataList="refreshData" />

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

</template>

<script>
import {getDiagnosisObj1} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisOne";
import {getEvaluationObj1} from "@/api/al/stateEvaluation/stateEvaluationOne";

import edit from "./modelEdit.vue"
import test from "./modelTest.vue"
import log from "./modelLog.vue"

import * as XLSX from "xlsx";
import {
  downLoad_Dataset_s, delEvaluationInput,
} from "@/api/al/stateEvaluation/stateEvaluationBase";
import {downLoad_Dataset_d} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {ElMessage} from "element-plus";
import {delModelObj, getModelObj} from "@/api/al/domainModel";

export default {
  name: "infoView",
  props: {
    nodeData: {
      type: Object,
      required: true
    },
    parentData: {
      type: Object,
      required: true
    }
  },
  components: {
    edit,
    test,
    log
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
      editVisible: false,
      testVisible: false,
      logVisible: false,
      showPreviewDialog: false,
      loading: false

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
      if(node.parentType === 'diagnosis') {
        getDiagnosisObj1(node.basicAlgorithm).then(res => {
          if(res.data.code === "200"){
            this.detailData = res.data.data
          }
        })
      }else {
        getEvaluationObj1(node.basicAlgorithm).then(res => {
          if(res.data.code === "200"){
            this.detailData = res.data.data
          }
        })
      }

    },

    infoEdit() {
      this.editVisible = true
      this.$nextTick(() => {
        this.$refs.edit.init(this.nodeData.id)
      })
    },

    refreshData() {
      getModelObj(this.nodeData.id).then(response => {
        this.localNodeData = response.data.data
        this.localNodeData.label = this.localNodeData.modelName
        this.localNodeData.level = 3
        this.localNodeData.children = []
        switch (this.nodeData.parentType) {
          case 'evaluation':
            this.localNodeData.parentType = "evaluation"
            break
          case 'diagnosis':
            this.localNodeData.parentType = "diagnosis"
            break
          default:
            console.error('错误的节点类型', this.nodeData.parentType)
        }
        this.$emit('update-node-data', this.localNodeData, 'modelName')
      })
    },

    deleteHandle(id) {
      this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delModelObj(id).then(response => {
          if (this.nodeData.parentType === 'evaluation') {
            return delEvaluationInput(this.nodeData.modelName, "domainModel").then(() => {
              return response
            })
          }
          return response
        })
      }).then(data => {
        this.$message.success('删除成功')
        this.$emit('update-tree', this.parentData)
        this.$emit('clearInfo')
      }).catch(error => {
        this.$message.error('删除失败: ' + error.message || '未知错误')
      })
    },

    alModelTest(){
      this.testVisible = true
      this.$nextTick(() => {
        this.$refs.test.init(this.nodeData.id, this.nodeData.input, this.nodeData.output, this.nodeData.parentType)
      })
    },

    alModelLog(){
      this.logVisible = true
      this.$nextTick(() => {
        this.$refs.log.init(this.nodeData)
      })
    },

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
      const downLoad_Dataset = this.nodeData.parentType === 'evaluation' ? downLoad_Dataset_s : downLoad_Dataset_d
      if(datasetUrl==='' || datasetUrl===null){
        this.$notify.info('请先上传数据集，再下载数据集')
      }else {
        downLoad_Dataset({filePath: datasetUrl}).then(res => {
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
    downLoad_programUrl(programUrl){
      if(programUrl==='' || programUrl===null){
        this.$notify.info('请先上传程序包，再下载程序包')
      }else{
        downLoad_Dataset_d({filePath:programUrl}).then(res=>{
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
      this.previewData=[]
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
      display: flex;
      align-items: center;
      vertical-align: center;
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

.custom-disabled {
  background-color: #d3d3d3 !important;
  color: #8c8c8c !important;
  border-color: #d3d3d3 !important;
  cursor: not-allowed;
}

</style>
