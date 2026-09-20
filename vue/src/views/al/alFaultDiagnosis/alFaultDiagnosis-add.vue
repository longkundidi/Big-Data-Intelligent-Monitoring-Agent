<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="50%"
      :show-close="true"
      class="el-dialog__header"
      @close="closeDialog()"
  >

    <div slot="title" style="display: flex;padding-bottom: 15px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>
      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">模型注册</span>
      </div>
    </div>

    <div class="app-container">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="基本信息配置"></el-step>
        <el-step title="模型参数配置"></el-step>
        <el-step title="模型部署与测试"></el-step>
      </el-steps>
    </div>


    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="125px"
             @keyup.enter.native="dataFormSubmit()" style="padding-top: 30px">
      <div v-if="active===0">
        <el-row>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="dataForm.modelName"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="模型类型" prop="modelType">
              <el-select v-model="dataForm.modelType" style="width: 100%">
                <el-option
                    v-for="item in options"
                    :key="item.modelType"
                    :value="item.modelType"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="提供者" prop="modelProvider">
              <el-input v-model="dataForm.modelProvider"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="针对对象">
              <model-object-select
                  v-model="dataForm.modelObject"
                  v-model:object-id="dataForm.objectId"
                  @change="getObject = true"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="模型支持库" prop="modelLibrary">
              <el-input v-model="dataForm.modelLibrary"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="模型图标">
              <el-button v-model="dataForm.modelIcon" class="normalBtn" @click="upload">上传</el-button>
              <el-tag type="success" v-if="geticon" style="margin-left: 3px;">{{ dataForm.modelIcon }}</el-tag>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="适用场景" prop="modelCondition">
              <el-input v-model="dataForm.modelCondition"
                        maxlength="50"
                        show-word-limit
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="模型优点" prop="modelAdvantage">
              <el-input v-model="dataForm.modelAdvantage"
                        maxlength="50"
                        show-word-limit
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="模型缺点" prop="modelDisadvantage">
              <el-input v-model="dataForm.modelDisadvantage"
                        maxlength="50"
                        show-word-limit
              />
            </el-form-item>
          </el-col>

        </el-row>
      </div>
      <div v-if="active===2">
        <el-row style="padding-top: 30px">
          <el-col :span="8">
            <el-form-item label="执行程序包">
              <el-button class="normalBtn"
                         @click="uploadprogram" size="large">上传
              </el-button>
              <el-tooltip placement="top">
                <template #content>
                  1、程序包需上传压缩包； <br/>
                  2、压缩包与模型执行文件同名； <br/>
                  3、压缩包内须有模型执行文件；<br/>
                  4、模型执行文件要有主函数入口if
                  __name__ =='__main__'，主函数要有输出（print等<br/>
                  5、程序包内必须要有存放评价指标的json文件，且该json文件与模型执行文件同同名
                </template>
                <el-icon>
                  <QuestionFilled/>
                </el-icon>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="评价指标举例：" >
               <JsonViewer :value=this.metric_example></JsonViewer>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署要求" prop="deployRequire">
              <el-input v-model="dataForm.deployRequire"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调用机制" prop="modelInvoke">
              <el-select v-model="dataForm.modelInvoke" style="width: 100%">
                <el-option
                    v-for="item in options2"
                    :key="item.modelInvoke"
                    :value="item.modelInvoke"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="模型输入样例" prop="input" label-width="110px">
              <el-input v-model="dataForm.input" style="width: 100%">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="模型输出样例" prop="output" label-width="110px">
              <el-input v-model="dataForm.output" style="width: 100%">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="模型访问路径">
                <el-input v-model="dataForm.modelUrl" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="JAR包大小">
                <el-input v-model="dataForm.jarSize" />
              </el-form-item>
            </el-col>
          </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="运行所需资源大小">
              <el-input v-model="dataFormone.runSize" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>



    <div v-if="active===1 && this.activeForm === 'one'">
      <el-form ref="dataFormone" :model="dataFormone" :rules="dataRule" label-width="90px"
               @keyup.enter.native="dataFormSubmit()" style="padding-top: 30px">
        <el-row>

          <el-col :span="8">
            <el-form-item label="模型框架" prop="modelFramework">
              <el-select v-model="dataFormone.modelFramework" style="width: 100%">
                <el-option
                    v-for="item in options5"
                    :key="item.modelFramework"
                    :value="item.modelFramework"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="设备" prop="device">
              <el-select v-model="dataFormone.device" style="width: 100%">
                <el-option
                    v-for="item in options6"
                    :key="item.device"
                    :value="item.device"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="4" style="padding-left: 2%">
            <div><span>重新训练：</span></div>
            <div>
              <el-radio-group v-model="dataFormone.ifTrain" @change="changeif1">
                <el-radio label="0">是</el-radio>
                <el-radio label="1">否</el-radio>
              </el-radio-group>
            </div>
          </el-col>
          <el-col :span="4" style="padding-left: 2%">
            <div><span>重新测试：</span></div>
            <div>
              <el-radio-group v-model="dataFormone.ifTest" @change="changeif2">
                <el-radio label="0">是</el-radio>
                <el-radio label="1">否</el-radio>
              </el-radio-group>
            </div>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="训练数据集">
              <el-button class="normalBtn"
                         @click="uploadTrainData" size="large">上传
              </el-button>
              <el-tag type="success" v-if="getTrain" style="margin-left: 2px;">{{ dataFormone.trainDataset }}</el-tag>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="测试数据集">
              <el-button class="normalBtn"
                         @click="uploadTestData" size="large">上传
              </el-button>
              <el-tag type="success" v-if="getTest" style="margin-left: 2px;">{{ dataFormone.testDataset }}</el-tag>
            </el-form-item>
          </el-col>

          <el-col :span="8" style="text-align: center">
            <div style="display: inline-block"><span>预处理：</span></div>
            <div style="display: inline-block">
              <el-radio-group v-model="dataFormone.ifPretreatment" @change="Changeyuchuli"
                              :disabled="btnstatus1 || btnstatus2">
                <el-radio label="0">是</el-radio>
                <el-radio label="1">否</el-radio>
              </el-radio-group>
            </div>
          </el-col>

          <el-col :span="16">
            <el-form-item label="预处理方法" prop="pretreatment">
              <el-input v-model="dataFormone.pretreatment" :disabled="yuchulistatus||btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="样本类型" prop="sampleType">
              <el-input v-model="dataFormone.sampleType" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="样本尺寸" prop="sampleSize">
              <el-input v-model="dataFormone.sampleSize" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="训练次数" prop="trainTimes">
              <el-input v-model="dataFormone.trainTimes" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="训练批次" prop="trainBatch">
              <el-input v-model="dataFormone.trainBatch" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="损失函数" prop="mlossFunction">
              <el-input v-model="dataFormone.lossFunction" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="正则化" prop="regularization">
              <el-input v-model="dataFormone.regularization" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="学习率" prop="learningRate">
              <el-input v-model="dataFormone.learningRate" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="优化器" prop="optimizer">
              <el-input v-model="dataFormone.optimizer" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="训练结果" prop="trainResults">
              <el-input v-model="dataFormone.trainResults" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="测试结果" prop="testResults">
              <el-input v-model="dataFormone.testResults" :disabled="btnstatus1 || btnstatus2"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="评价指标">
              <el-input v-model="dataFormone.metrics" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div v-if="active===1 && this.activeForm === 'two'">
      <el-form ref="dataFormtwo" :model="dataFormtwo" :rules="dataRule" label-width="90px"
               @keyup.enter.native="dataFormSubmit()" style="padding-top: 30px">
        <el-row>

          <el-col :span="8" style="text-align: center;padding-top: 1%">
            <div style="display: inline-block"><span>预处理：</span></div>
            <div style="display: inline-block">
              <el-radio-group v-model="dataFormtwo.ifPretreatment" @change="Changeyuchulitwo">
                <el-radio label="0">是</el-radio>
                <el-radio label="1">否</el-radio>
              </el-radio-group>
            </div>
          </el-col>

          <el-col :span="16">
            <el-form-item label="预处理方法" prop="pretreatment">
              <el-input v-model="dataFormtwo.pretreatment" :disabled="yuchulistatustwo"/>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="样本类型">
              <el-input v-model="dataFormtwo.sampleType"/>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="样本尺寸">
              <el-input v-model="dataFormtwo.sampleSize"/>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="指标要求">
              <el-input v-model="dataFormtwo.indicatorRequire"/>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="阈值类型">
              <el-select v-model="dataFormtwo.thresholdType" style="width: 100%">
                <el-option
                    v-for="item in options7"
                    :key="item.thresholdType"
                    :value="item.thresholdType"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="阈值描述">
              <el-input v-model="dataFormtwo.thresholdDescription"/>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="超限次数">
              <el-input v-model="dataFormtwo.overrunTimes"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div v-if="active===0" slot="footer" style="text-align: center">
      <el-button style="margin-top: 12px;" class="normalBtn" @click="next">下一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="visible = false;" v-if="active < 1">取消
      </el-button>
    </div>
    <div v-if="active===1" slot="footer" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="next">下一步</el-button>
    </div>
    <div v-if="active===2" slot="footer" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()" style="margin-top:12px">完成配置
      </el-button>
    </div>


    <uploadicon v-if="dialogVisible" ref="upload" @getIconUrl="getIconUrl"></uploadicon>
    <uploadprogram v-if="programVisible" ref="uploadprogram" @getprogramurl="getprogramurl" @getisPass="getisPass"></uploadprogram>
    <uploadtraindata v-if="trainDataVisible" ref="uploadtraindata" @getTrainDataSet="getTrainDataSet"></uploadtraindata>
    <uploadtestdata v-if="testDataVisible" ref="uploadtestdata" @getTestDataSet="getTestDataSet"></uploadtestdata>

  </el-dialog>
</template>

<script>
import {
  addObj, exitName,
} from '/src/api/al/faultDiagnosisbase/alFaultDiagnosisBase'
import {
  addObj1,
} from '/src/api/al/faultDiagnosisbase/alFaultDiagnosisOne'
import {
  addObj2,
} from '/src/api/al/faultDiagnosisbase/alFaultDiagnosisTwo'
import uploadicon from './alFaultDiagnosis-add-uploadicon.vue'
import uploadprogram from './alFaultDiagnosis-add-uploadprogram.vue'
import uploadtraindata from './alFaultDiagnosis-add-uploadTrainData.vue'
import uploadtestdata from './alFaultDiagnosis-add-uploadTestData.vue'

import JsonViewer from 'vue-json-viewer'
import ModelObjectSelect from '@/components/ModelObjectSelect.vue'

export default {
  data() {
    return {
      active: 0,//步骤条初始化
      options: [
        {modelType: '统计学模型'},
        {modelType: '信号处理模型'},
        {modelType: '传统机器学习模型'},
        {modelType: '深度学习模型'},
      ],
      options2: [
        {modelInvoke: '业务调用（微服务）'},
        {modelInvoke: '时间窗自动调用（Flink）'},
      ],
      options5: [
        {modelFramework: 'TensorFlow'},
        {modelFramework: 'Pytorch'},
        {modelFramework: 'keras'},
        {modelFramework: '无'},
      ],
      options6: [
        {device: 'CPU'},
        {device: 'GPU'}
      ],
      options7: [
        {thresholdType: '专家阈值'},
        {thresholdType: '自适应阈值'}
      ],
      searchForm1: {
        modelName: ''
      },
      programVisible: false,
      trainDataVisible: false,
      testDataVisible: false,
      dialogVisible: false,
      visible: false,
      canSubmit: false,
      geticon: false,
      getprogram: false,
      getTrain: false,
      getTest: false,
      getObject: false,
      ispass:false,
      dataForm: {
        modelName: '',
        modelProvider: '',
        modelObject: '',
        objectId: '',
        modelType: '',
        modelInvoke: '',
        modelTypeFirst: '数字孪生应用算法集',
        modelFunction: '故障诊断',
        modelLibrary: '',
        modelCondition: '',
        modelAdvantage: '',
        modelDisadvantage: '',
        modelNum: '0',
        modelIcon: '',
        programUrl: '',
        modelUrl: '',
        input: '',
        output: '',
        deployRequire: '',
        isService:0,
        jarSize:0,
        runSize:0,
      },
      dataFormone: {
        modelName: '',
        ifTrain: '1',
        ifTest: '1',
        ifPretreatment: '1',
        modelFramework: '',
        device: '',
        trainDataset: '',
        testDataset: '',
        pretreatment: '',
        sampleType: '',
        sampleSize: '',
        trainTimes: '',
        trainBatch: '',
        lossFunction: '',
        regularization: '',
        learningRate: '',
        optimizer: '',
        trainResults: '',
        testResults: '',
        metrics:''
      },
      dataFormtwo: {
        modelName: '',
        ifPretreatment: '1',
        pretreatment: '',
        sampleType: '',
        sampleSize: '',
        indicatorRequire: '',
        thresholdType: '',
        thresholdDescription: '',
        overrunTimes: '',
      },
      activeForm: '',
      btnstatus1: true,
      btnstatus2: true,
      yuchulistatus: true,
      yuchulistatustwo: true,
      dataRule: {
        modelName: [
          {required: true, message: '模型名称不能为空', trigger: 'blur'}
        ],
        modelType: [
          {required: true, message: '模型类型不能为空', trigger: 'blur'}
        ],
        modelFunction: [
          {required: true, message: '运维服务不能为空', trigger: 'blur'}
        ],
        modelInvoke: [
          {required: true, message: '调用机制不能为空', trigger: 'blur'}
        ],
        input: [
          {required: true, message: '模型输入样例不能为空', trigger: 'blur'}
        ],
        output: [
          {required: true, message: '模型输出样例不能为空', trigger: 'blur'}
        ]
      },
      metric_example:'{"metrics":[\"F1\", \"precise\", \"accuracy\", \"recall\"]}'

    }
  },
  components: {
    ModelObjectSelect,
    uploadicon,
    uploadprogram,
    uploadtraindata,
    uploadtestdata,
    JsonViewer
  },
  methods: {

    upload() {
      if (this.dataForm.modelName === undefined || this.dataForm.modelType === undefined) {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.modelName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.dialogVisible = true
            this.$nextTick(() => {
              this.$refs.upload.init(this.dataForm.modelName)
            })
          }
        }).catch(() => {
        })
      }
    },

    uploadprogram() {
      if (this.dataForm.modelName === '') {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.modelName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.programVisible = true
            this.$nextTick(() => {
              this.$refs.uploadprogram.init()
            })
          }
        }).catch(() => {
        })
      }
    },

    uploadTrainData() {
      if (this.dataForm.modelName === '') {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.modelName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.trainDataVisible = true
            this.$nextTick(() => {
              this.$refs.uploadtraindata.init()
            })
          }
        }).catch(() => {
        })
      }
    },

    uploadTestData() {
      if (this.dataForm.modelName === '') {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.modelName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.testDataVisible = true
            this.$nextTick(() => {
              this.$refs.uploadtestdata.init()
            })
          }
        }).catch(() => {
        })
      }
    },

    changeif1(val) {
      this.btnstatus1 = (val === '0');
    },
    changeif2(val) {
      this.btnstatus2 = (val === '0');
    },
    Changeyuchuli(val) {
      this.yuchulistatus = (val !== '0');
    },
    Changeyuchulitwo(val) {
      this.yuchulistatustwo = (val !== '0');
    },
    getIconUrl(iconUrl) {
      this.geticon = true
      this.dataForm.modelIcon = iconUrl
    },
    getprogramurl(programurl) {
      this.getprogram = true
      this.dataForm.programUrl = programurl
    },
    getisPass(ispass){
      this.ispass=ispass
    },
    getTrainDataSet(trainDataset) {
      this.getTrain = true
      this.dataFormone.trainDataset = trainDataset
    },
    getTestDataSet(testDataset) {
      this.getTest = true
      this.dataFormone.testDataset = testDataset
    },

    init(modelFunction = '故障诊断', modelTypeFirst = '数字孪生应用算法集') {
      this.active = 0
      this.visible = true
      this.canSubmit = true
      this.btnstatus1 = true
      this.btnstatus2 = true
      this.yuchulistatus = true
      this.geticon = false
      this.getprogram = false
      this.getObject = false
      this.getTrain = false
      this.getTest = false
      this.dataFormone.ifTrain = '0'
      this.dataFormone.ifTest = '0'
      this.dataFormone.ifPretreatment = '1'
      this.dataFormtwo.ifPretreatment = '1'
      this.dataForm = {}
      this.dataForm.modelTypeFirst = modelTypeFirst
      this.dataForm.modelFunction = modelFunction

    },

    // 重置表单
    closeDialog() {
      this.searchForm1.modelName = ''
      this.geticon = false
      this.getprogram = false
      this.reset()
      this.reset1()
      this.reset2()
      this.$emit('refreshDataList')
    },


    reset() {
      this.dataForm.modelName = ''
      this.dataForm.modelProvider = ''
      this.dataForm.modelObject = ''
      this.dataForm.objectId = ''
      this.dataForm.modelType = ''
      this.dataForm.modelInvoke = ''
      this.dataForm.modelTypeFirst = ''
      this.dataForm.modelFunction = ''
      this.dataForm.modelLibrary = ''
      this.dataForm.modelCondition = ''
      this.dataForm.modelAdvantage = ''
      this.dataForm.modelDisadvantage = ''
    },

    reset1() {
      this.dataFormone.modelName = ''
      this.dataFormone.ifTrain = ''
      this.dataFormone.ifTest = ''
      this.dataFormone.ifPretreatment = ''
      this.dataFormone.modelFramework = ''
      this.dataFormone.device = ''
      this.dataFormone.trainDataset = ''
      this.dataFormone.testDataset = ''
      this.dataFormone.pretreatment = ''
      this.dataFormone.sampleType = ''
      this.dataFormone.sampleSize = ''
      this.dataFormone.trainTimes = ''
      this.dataFormone.trainBatch = ''
      this.dataFormone.lossFunction = ''
      this.dataFormone.regularization = ''
      this.dataFormone.learningRate = ''
      this.dataFormone.optimizer = ''
      this.dataFormone.trainResults = ''
      this.dataFormone.testResults = ''
    },

    reset2() {
      this.dataFormtwo.modelName = ''
      this.dataFormtwo.ifPretreatment = ''
      this.dataFormtwo.pretreatment = ''
      this.dataFormtwo.sampleType = ''
      this.dataFormtwo.sampleSize = ''
      this.dataFormtwo.indicatorRequire = ''
      this.dataFormtwo.thresholdType = ''
      this.dataFormtwo.thresholdDescription = ''
      this.dataFormtwo.overrunTimes = ''
    },

    //下一页
    next() {
      if (this.dataForm.modelType === '' || this.dataForm.modelName === '' || this.dataForm.modelFunction === '') {
        this.active = 0
        this.$notify.warning('必要参数未配置')
      } else if (this.geticon === false) {
        this.$notify.warning('图标未上传')
      }else if (this.active === 1 && this.getTrain === false && (this.dataForm.modelType === '传统机器学习模型' || this.dataForm.modelType === '深度学习模型')) {
        this.$notify.warning('训练数据集未上传')
      } else if (this.active === 1 &&this.getTest === false && (this.dataForm.modelType === '传统机器学习模型' || this.dataForm.modelType === '深度学习模型')) {
        this.$notify.warning('测试数据集未上传')
      }else if (this.getObject === false) {
        this.$notify.warning('未选择模型针对对象')
      } else {
        exitName(this.dataForm.modelName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            if (this.active++ > 2) this.active = 0
            if (this.dataForm.modelType === '传统机器学习模型' || this.dataForm.modelType === '深度学习模型') {
              this.activeForm = 'one'
            } else {
              this.activeForm = 'two'
            }
          }
        }).catch(() => {
        })

      }
    },
    //上一页
    prev() {
      if (this.active > 0) this.active--;
    },
    dataFormSubmit() {
      this.dataFormone.modelName = this.dataForm.modelName
      this.dataFormtwo.modelName = this.dataForm.modelName

      if (this.dataForm.modelInvoke === undefined) {
        this.$notify.warning('必要参数未配置')
      } else if (this.getprogram === false) {
        this.$message({
          showClose: true,
          message: '程序包未上传',
          type: 'warning'
        })
      }else if(this.ispass===false){
        this.$message.error("请先补充完整评价指标")
      }
      else {
        if (this.activeForm === 'one') {
          this.dataFormone.ifPretreatment = +this.dataFormone.ifPretreatment;
          this.dataFormone.ifTrain = +this.dataFormone.ifTrain;
          this.dataFormone.ifTest = +this.dataFormone.ifTest;
          this.canSubmit = false
          addObj(this.dataForm).then(response => {
            this.$notify.success('添加成功')
            this.visible = false
            this.$emit('refreshDataList')
          }).catch(() => {
            this.canSubmit = true
          })
          addObj1(this.dataFormone).then(response => {
            this.visible = false
            this.$emit('refreshDataList')
          }).catch(() => {
            this.canSubmit = true
          })
        } else if (this.activeForm === 'two') {
          this.dataFormtwo.ifPretreatment = +this.dataFormtwo.ifPretreatment;
          this.canSubmit = false
          addObj(this.dataForm).then(response => {
            this.$notify.success('添加成功')
            this.visible = false
            this.$emit('refreshDataList')
          }).catch(() => {
            this.canSubmit = true
          })
          addObj2(this.dataFormtwo).then(response => {
            this.visible = false
            this.$emit('refreshDataList')
          }).catch(() => {
            this.canSubmit = true
          })
        }
      }
    },

  }
}
</script>
<style lang="scss" scoped>
.el-dialog__body {
  /*padding: 20px 20px 30px 20px;*/
}

.el-step__icon {
  top: -1px;
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

::v-deep .jv-container .jv-code {
  overflow: hidden;
  padding: 1px 20px;
}

</style>
