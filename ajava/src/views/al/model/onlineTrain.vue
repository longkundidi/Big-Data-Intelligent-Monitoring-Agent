<template>
  <el-dialog v-model="dialogVisible" width="35%" @close="handleClose">
    <el-steps :active="activeStep" finish-status="success" align-center>
      <el-step title="在线训练"></el-step>
      <el-step title="保存模型"></el-step>
    </el-steps>
    <div v-if="activeStep === 0">
      <div class="button-class">
        <span>算法针对对象</span>
        <el-tag type="success" style="margin-left: 5px;">{{ currentObject }}</el-tag>
        <el-button class="normalBtn"
                   @click="selectGBomNode()">修改
        </el-button>
      </div>

      <div class="button-class">
        <div style="white-space: nowrap">
          <span>输入变量</span>
        </div>
        <div class="tags-class">
          <el-tag v-for="(variable, index) in selectedVariables"
                  :key="index">
            {{ variable.varName }}
          </el-tag>
        </div>

      </div>

      <div class="button-group">
        <el-upload
            action="#"
            :on-change="handleTrainDataChange"
            :auto-upload="false"
            :limit="1"
            accept=".csv"
            :file-list="trainFiles"
        >
          <el-button class="addBtn">上传训练数据集</el-button>
        </el-upload>
        <el-upload
            action="#"
            :on-change="handleTestDataChange"
            :auto-upload="false"
            :limit="1"
            accept=".csv"
            :file-list="testFiles"
        >
          <el-button class="addBtn">上传测试数据集</el-button>
        </el-upload>
        <el-button v-if="trainFiles.length >= 1 && testFiles.length >= 1"
                   class="editWorktBtn"
                   @click="startTraining">在线训练
        </el-button>
        <el-tooltip v-else
                    class="item"
                    effect="dark"
                    content="请先上传训练数据集和测试数据集">
          <el-button class="editWorktBtn custom-disabled">在线训练
          </el-button>
        </el-tooltip>
      </div>

      <el-progress v-if="isTraining"
                   :percentage="progress"
                   :stroke-width="15"
                   striped
                   striped-flow
                   :show-text="true"></el-progress>

      <el-alert
          v-if="!isTraining && Object.keys(metrics).length > 0"
          title="训练成功"
          type="success"
          show-icon
          :closable="false"
      >
        <template #default="scope">
          <div class="alert-content">
            <span class="metrics-title">评价指标结果</span>
            <div v-for="(value, key) in metrics.metrics" :key="key" class="metric-item">
              <span class="metric-key">{{ key }} :</span>
              <span class="metric-value">{{ value }}</span>
            </div>
          </div>
        </template>
      </el-alert>

      <el-alert
          v-if="!isTraining && this.trainResult.resultInfo"
          title="训练失败"
          type="error"
          show-icon
          :closable="false"
      >
        <template #default="scope">
          <div class="alert-content">
            <span class="metric-value">{{ this.trainResult.resultInfo }}</span>
          </div>
        </template>
      </el-alert>

    </div>
    <div v-else-if="activeStep === 1">
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="110px"
               @keyup.enter.native="dataFormSubmit()" style="padding-top: 30px;margin-right: 20px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="dataForm.modelName"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="模型图标">
              <el-button v-model="dataForm.modelIcon" class="normalBtn" @click="upload">上传</el-button>
              <el-tag type="success" v-if="geticon" style="margin-left: 2px;">{{ dataForm.modelIcon }}</el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="适用工况" prop="modelUsecase">
              <el-input v-model="dataForm.modelUsecase"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="JAR包大小" prop="jarSize">
              <el-input v-model="dataForm.jarSize"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运行所需资源" prop="runSize">
              <el-input v-model="dataForm.runSize"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="数据集说明" prop="datasetMemo">
              <el-input v-model="dataForm.datasetMemo"
                        maxlength="50"
                        show-word-limit/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="模型输入样例" prop="input">
              <el-input v-model="dataForm.input">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="模型输出样例" prop="output">
              <el-input v-model="dataForm.output">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button class="disMissBtn" @click="handleClose">取消</el-button>
      <el-button class="registerBtn" v-if="activeStep === 1" @click="dataFormSubmit">确定</el-button>
      <el-button class="registerBtn" v-if="activeStep === 0" @click="nextStep">保存模型</el-button>
    </span>
  </el-dialog>

  <el-dialog v-model="selectVisible" title="选择算法针对对象" width="50%" draggable>
    <div class="treeDialog" >
      <node-move-tree ref="refNodeTree" style="width: 100% ;height: 90%"></node-move-tree>
      <el-button class="normalBtn" @click.stop="doNodeSelect()">确定</el-button>
    </div>
  </el-dialog>

  <uploadicon v-if="uploadVisible" ref="uploadIcon" @getIconUrl="getIconUrl"></uploadicon>
</template>

<script>
import Papa from 'papaparse'
import {getCleanObj} from "@/api/al/dataCleaning/dataCleaning";
import {addAlInput, getEvaluationObj, uploadProgram} from "@/api/al/stateEvaluation/stateEvaluationBase";
import {getDiagnosisObj, getObj, onlineTrain, startJob} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {getExtractionObj} from "@/api/al/knowledgeExtraction/knowledgeExtraction";

import nodeMoveTree from '@/views/sw/model3d/configGbomTree/treeForNodeMove'
import inputVariables from '@/views/sw/model3d/configPerceivedVariable/index'
import {ElMessage, ElMessageBox} from "element-plus";
import uploadicon from '@/views/al/stateEvaluation/stateEvaluation-add-uploadicon.vue'
import {
  addDomainModel,
  exitModelName,
  getCurrentObject,
  getCurrentVariables,
  variablesValidation
} from "@/api/al/domainModel";

export default {
  name: "onlineTrain",
  data() {
    return {
      dialogVisible: true,
      activeStep: 0,
      currentObject: '',
      currentObjectId: '',
      trainFiles: [],
      testFiles: [],
      trainHeaders: [],
      testHeaders: [],
      isTraining: false,
      progress: 0,
      programUrl: '',
      testResult: {},
      trainResult: {
        resultInfo: '',
        resultDetail: ''
      },
      metrics: {},
      modelProgram: '',
      dataForm: {
        modelName: '',
        modelType: '',
        basicAlgorithm: '',
        modelObject: '',
        objectId: '',
        modelUsecase: '',
        modelUrl: '',
        modelIcon: '',
        jarSize: '',
        runSize: '',
        trainDataset: '',
        testDataset: '',
        datasetMemo: '',
        metricsResult: '',
        programUrl: '',
        input: '',
        output: ''
      },
      dataRule: {
        modelName: [
          {required: true, message: '模型名称不能为空', trigger: 'blur'}
        ],
        modelUsecase: [
          {required: true, message: '适用工况不能为空', trigger: 'blur'}
        ],
        input: [
          {required: true, message: '模型输入样例不能为空', trigger: 'blur'}
        ],
        output: [
          {required: true, message: '模型输出样例不能为空', trigger: 'blur'}
        ]
      },
      selectVisible: false,
      selectedVariables: [],
      isFirstSelectTree: false,
      uploadVisible: false

    }
  },
  props: {
    alId: {
      type: [Number, String],
      required: true
    },
    alName: {
      type: String,
      required: true
    },
    type: {
      type: String,
      required: true
    }
  },

  components: {
    nodeMoveTree,
    inputVariables,
    uploadicon,
  },

  mounted() {
    getCurrentObject({
      alId: this.alId,
      type: this.type
    }).then(res => {
      this.currentObject = res.data.data.model_object
      this.currentObjectId = res.data.data.object_id
    })
    getCurrentVariables({
      alName: this.alName,
      type: this.type
    }).then(res => {
      this.selectedVariables = res.data.data
    })
  },

  methods: {

    init(){
      this.getObject = false
      this.geticon = false
      this.dataForm = {}
      this.metrics = {}
      this.isFirstSelectTree = true
      this.isFirstSelectVal = true
    },

    handleTrainDataChange(file, fileList) {
      let fileTemp = file.raw
      if (fileTemp && (fileTemp.type !== 'text/csv') && (!fileTemp.name.toLowerCase().endsWith('.csv'))) {
        this.$message({
          type: 'warning',
          message: '训练数据集文件格式错误，请删除后重新上传！'
        })
      }

      if (fileList.length > 1) {
        this.$message.warning('只能上传一个训练数据集文件，请删除多余的文件！')
        fileList.splice(1, fileList.length - 1)
      }
      this.trainFiles = fileList.map(file => file.raw)
    },
    handleTestDataChange(file, fileList) {
      let fileTemp = file.raw
      if (fileTemp && (fileTemp.type !== 'text/csv') && (!fileTemp.name.toLowerCase().endsWith('.csv'))) {
        this.$message({
          type: 'warning',
          message: '测试数据集文件格式错误，请删除后重新上传！'
        })
      }

      if (fileList.length > 1) {
        this.$message.warning('只能上传一个测试数据集文件，请删除多余的文件！')
        fileList.splice(1, fileList.length - 1)
      }
      this.testFiles = fileList.map(file => file.raw)
    },

    async datasetValidation() {
      const trainFile = this.trainFiles.length > 0 ? this.trainFiles[0] : null
      const testFile = this.testFiles.length > 0 ? this.testFiles[0] : null

      if (!trainFile || !testFile) {
        this.$message.warning('数据集文件为空，请重新上传！')
      } else {
        const readAndExtractHeaders = (file, type) => {
          return new Promise((resolve, reject) => {
            Papa.parse(file, {
              encoding: 'gb2312',
              header: true,
              dynamicTyping: true,
              complete: (results) => {
                const headers = results.meta.fields || []
                if (type === 'train') {
                  this.trainHeaders = headers
                } else if (type === 'test') {
                  this.testHeaders = headers
                }
                resolve(headers)
              },
              error: (error) => {
                this.$notify.error(`读取${type}数据集时发生错误，请重新上传！`)
                console.error('PapaParse error:', error)
                reject(error)
              }
            })
          })
        }

        try {
          await readAndExtractHeaders(trainFile, 'train')
          await readAndExtractHeaders(testFile, 'test')
        } catch (error) {
          console.error('Error reading files:', error)
        }
      }
    },

    removeHeadersFromFile(file){
      return new Promise((resolve, reject) => {
        Papa.parse(file, {
          encoding: 'gb2312',
          header: true,
          dynamicTyping: true,
          complete: (results) => {
            // 删除表头
            const dataWithoutHeaders = results.data.slice(1)

            // 将数据转换回 CSV 格式
            const csvContent = Papa.unparse(dataWithoutHeaders)

            const blob = new Blob([csvContent], { type: 'text/csv' })

            const newFile = new File([blob], file.name, { type: file.type })

            resolve(newFile)
          },
          error: (error) => {
            reject(error)
          }
        })
      })
    },

    async startTraining() {
      await this.datasetValidation()

      // 检查 trainHeaders 和 testHeaders 是否完全相同
      if (this.trainHeaders.length > 0 && this.testHeaders.length > 0) {
        if (JSON.stringify(this.trainHeaders) !== JSON.stringify(this.testHeaders)) {
          this.$notify.error('训练数据集和测试数据集的输入变量不一致，请重新上传！')
        } else {
          // 验证数据集中的变量是否能与元结构树感知变量匹配
          variablesValidation({
            varNames: this.trainHeaders,
            nodeId: this.currentObjectId
          }).then(res => {
            if (res.data.msg === "验证成功") {

              this.selectedVariables = res.data.data

              const setProgramUrl = () => {
                switch (this.type) {
                  case 'clean':
                    return getCleanObj(this.alId)
                  case 'extraction':
                    return getExtractionObj(this.alId)
                  case 'evaluation':
                    return getEvaluationObj(this.alId)
                  case 'diagnosis':
                    return getDiagnosisObj(this.alId)
                  default:
                    throw new Error('Unsupported type')
                }
              }

              setProgramUrl().then(async res => {
                this.programUrl = res.data.data.programUrl

                const trainFileWithoutHeaders = await this.removeHeadersFromFile(this.trainFiles[0])
                const testFileWithoutHeaders = await this.removeHeadersFromFile(this.testFiles[0])

                const formData = new FormData()
                formData.append('trainDataset', trainFileWithoutHeaders)
                formData.append('testDataset', testFileWithoutHeaders)
                formData.append('programUrl', this.programUrl)

                this.isTraining = true
                this.progress = 0

                onlineTrain(formData).then(res => {
                  if (res.data.code === '200') {
                    this.progress = 5
                    let taskId = res.data.data[0]
                    let alModelType = res.data.data[1]
                    // 校验评价指标
                    startJob(taskId, alModelType, "在线训练").then(res => {
                      getObj(taskId).then(response => {
                        this.testResult = JSON.parse(response.data.data.taskResult)
                        this.progress = 10
                        this.loopResult(taskId)
                      })
                    })
                  } else {
                    this.$message.error(res.data.message)
                  }
                })
              }).catch(error => {
                console.error('Error setting program URL:', error)
                this.$message.error('设置程序URL时出错，请稍后再试！')
              })

            }else {
              this.$notify.error(`数据集的变量与元结构树中${this.currentObject}节点上的感知变量不匹配，请重新上传数据集文件！`)
            }
          }).catch(error => {
            console.error('Error in variablesValidation:', error)
          })
        }
      }

    },

    loopResult(taskId) {
      let i = 0;
      let timer = setInterval(() => {
        this.fun(timer, i++, taskId)
      }, 3000)
      return true
    },
    fun(timer, i, taskId) {
      setTimeout(() => {
        getObj(taskId).then(response => {
          this.progress = Math.min(98, 23 + (i / 2) * (87 - 23))
          this.testResult = JSON.parse(response.data.data.taskResult)
          if (this.testResult) {
            if (this.testResult.includes("error")) {
              if (this.testResult.includes("File not found in MinIO")) {
                this.trainResult.resultInfo = "测试未通过！未在MinIO中找到程序包！"
                this.testResult = JSON.parse(this.testResult)
                this.trainResult.resultDetail = this.testResult.file
              } else if (this.testResult.includes("Failed to extract file, possibly algorithm file does not have the same name as the Zip file or no algorithm file")) {
                this.trainResult.resultInfo = "测试未通过！提取算法文件失败，可能是训练模型的文件名与程序包名不相同或没有文件！"
              }  else {
                this.trainResult.resultInfo = "发生意外错误！"
              }
              this.isTraining = false
              this.$message.error( this.trainResult.resultInfo)
            } else {
              if (this.testResult.includes("success")) {
                this.progress = 100
                this.metrics = JSON.parse(this.testResult).data
                this.modelProgram = JSON.parse(this.testResult).program
                this.isTraining = false
              }
            }
            clearInterval(timer)
          }
        })

        if (i >= 60) {
          this.$message.warning('服务请求超时,请重新进行在线训练')
          this.loading = false
          clearInterval(timer)
        }
      }, 0)
    },

    selectGBomNode(){
      this.selectVisible = true
      if(this.isFirstSelectTree){
        this.$nextTick(() => {
          this.$refs.refNodeTree.clearSelected()
        })
        this.isFirstSelectTree = false
      }
    },

    async doNodeSelect(){

      let targetNode = this.$refs.refNodeTree.getCurrentNode()        //  获取目标节点
      if (targetNode){
        this.dlgNodeMove = false
        ElMessageBox.confirm(
            '确定选择该部套件吗？',
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
        ).then(() => {
          this.currentObject = targetNode.name
          this.currentObjectId = targetNode.id
          this.selectVisible = false
        }).catch(() => {
          ElMessage({
            type: 'info',
            message: '取消',
          })
        })
      }
    },

    selectVariables(){
      this.selectVariablesVisible = true
      if(this.isFirstSelectVal){
        this.isFirstSelectVal = false
      }
    },

    handleSelectedVariables(variables) {
      this.selectedVariables = variables
    },

    doVariablesSelect(){
      this.selectVariablesVisible = false
    },

    upload() {
      if (this.dataForm.modelName === undefined || this.dataForm.modelUsecase === undefined) {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitModelName(this.dataForm.modelName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.uploadVisible = true
            this.$nextTick(() => {
              this.$refs.uploadIcon.init(this.dataForm.modelName)
            })
          }
        }).catch(() => {
        })
      }
    },

    getIconUrl(iconUrl) {
      this.geticon = true
      this.dataForm.modelIcon = iconUrl
    },

    async uploadDataSet(dataSet) {
      const file = new FormData()

      if (dataSet.uid === undefined) {
        this.$notify.warning('没有数据集文件')
        return null
      }

      file.append('file', dataSet)

      try {
        const response = await uploadProgram(file)
        return response.data.data
      } catch (error) {
        console.error('数据集文件上传失败:', error)
        return null
      }
    },

    async dataFormSubmit() {

      if (this.dataForm.modelName === '' || this.dataForm.modelUsecase === '') {
        this.$notify.warning('必要参数未配置')
        return
      } else if (!this.geticon) {
        this.$notify.warning('图标未上传')
        return
      }

      try {

        const trainDataset = await this.uploadDataSet(this.trainFiles[0])
        const testDataset = await this.uploadDataSet(this.testFiles[0])

        this.dataForm.basicAlgorithm = this.alName
        this.dataForm.modelType = this.type
        this.dataForm.modelObject = this.currentObject
        this.dataForm.objectId = this.currentObjectId
        this.dataForm.trainDataset = trainDataset
        this.dataForm.testDataset = testDataset
        this.dataForm.metricsResult = JSON.stringify(this.metrics)
        this.dataForm.programUrl = this.modelProgram

        addDomainModel(this.dataForm).then(response => {
          this.$notify.success('保存模型成功')
          this.dialogVisible = false
          this.$emit('refreshTree')
        })

        if (this.selectedVariables.length !== 0) {
          addAlInput({
            alName: this.dataForm.modelName,
            alClass: "domainModel",
            inputVariables: this.selectedVariables
          }).then(response => {

          })
        }
      }catch (error) {
        console.error('保存模型时发生错误:', error)
        this.$notify.error('保存模型时发生错误')
      }
    },

    nextStep() {
      if (this.activeStep === 0 && Object.keys(this.metrics).length === 0) {
        this.$message.error('请先完成训练')
        return
      }
      this.activeStep++
    },

    reset() {
      this.dataForm.modelName = ''
      this.dataForm.basicAlgorithm = ''
      this.dataForm.modelType = ''
      this.dataForm.modelObject = ''
      this.dataForm.objectId = ''
      this.dataForm.modelUsecase = ''
      this.dataForm.modelUrl = ''
      this.dataForm.jarSize = ''
      this.dataForm.runSize = ''
      this.dataForm.trainDataset = ''
      this.dataForm.testDataset = ''
      this.dataForm.modelIcon = ''
      this.dataForm.datasetMemo = ''
      this.dataForm.metricsResult = ''
      this.dataForm.programUrl = ''
      this.dataForm.input = ''
      this.dataForm.output = ''
    },

    handleClose() {
      this.dialogVisible = false
      this.geticon = false
      this.getObject = false
      this.metrics = {}
      this.reset()
      this.$emit('refreshTree')
    }
  }
}
</script>

<style lang="scss" scoped>

.button-class{
  display: flex;
  gap: 10px;
  margin: 20px;
}

.tags-class {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  width: 100%; /* 使标签容器占据剩余空间 */
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 50px;
  margin: 20px;
}

.custom-disabled {
  background-color: #d3d3d3 !important;
  color: #8c8c8c !important;
  border-color: #d3d3d3 !important;
  cursor: not-allowed;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 3rem; /* 可选：增加顶部间距 */
}

.alert-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.metrics-title {
  display: flex;
  font-weight: bold;
  padding: 3px;
  font-size: 1.3em;
  justify-content: center;
  color: #333;
}

.metric-item {
  display: flex;
  justify-content: center;
  align-items: center;
}

.metric-key {
  font-weight: bold;
  color: #333;
  font-size: 1.2em;
  padding: 5px;
  font-style: italic;
}

.metric-value {
  color: #666;
  font-size: 1.0em;
  margin-right: 5px;
}

.treeDialog{
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  .el-button{
    background-color: cadetblue;
    width: 65px;
    height: 40px;
    margin-top: 15px;
    border-color: white;
    &:hover {
      background-color: chocolate; /* 设置悬浮时的背景色 */
    }
  }
}

</style>