<template>
  <el-dialog
    append-to-body
    :close-on-click-modal="false"
    v-model="visible"
    width="70%"
    :show-close="true"
    class="el-dialog__header"
    @close="closeDialog()"
  >
    <div slot="title" style="font-size: 30px;text-align:center;padding-bottom: 2%">
      <span>模型详情</span>
    </div>

    <el-form ref="dataForm"  :rules="dataRule" label-width="125px" @keyup.enter.native="dataFormSubmit()">
      <el-row>
        <el-col :span="8">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="dataForm.modelName"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型类型" prop="modelType">
            <el-input v-model="dataForm.modelType"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="提供者" prop="modelProvider">
            <el-input v-model="dataForm.modelProvider"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="针对对象" prop="modelObject">
            <el-input v-model="dataForm.modelObject"  disabled />
          </el-form-item>
        </el-col>

<!--        <el-col :span="12">-->
<!--          <el-form-item label="调用机制" prop="modelInvoke">-->
<!--            <el-input v-model="dataForm.modelInvoke"  disabled />-->
<!--          </el-form-item>-->
<!--        </el-col>-->

<!--        <el-col :span="8">-->
<!--          <el-form-item label="运维服务" prop="modelFunction">-->
<!--            <el-input v-model="dataForm.modelFunction"  disabled />-->
<!--          </el-form-item>-->
<!--        </el-col>-->

        <el-col :span="12">
          <el-form-item label="模型支持库" prop="modelLibrary">
            <el-input v-model="dataForm.modelLibrary"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="适用场景" prop="modelCondition">
            <el-input v-model="dataForm.modelCondition"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型优点" prop="modelAdvantage">
            <el-input v-model="dataForm.modelAdvantage"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型缺点" prop="modelDisadvantage">
            <el-input v-model="dataForm.modelDisadvantage"  disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-form ref="dataForm1"  :rules="dataRule" label-width="125px" >
      <el-row>

        <el-col :span="8">
          <el-form-item label="模型框架" >
            <el-input v-model="dataFormone.modelFramework"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="设备" >
            <el-input v-model="dataFormone.device"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练数据集">
            <el-row>
              <el-col :span="16"> <!-- 输入框占据16个单位 -->
                <el-tooltip class="item" effect="dark" :content="dataFormone.trainDataset" placement="top">
                  <el-input v-model="dataFormone.trainDataset" disabled
                            style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                            :style="{ 'max-width': '100%' }"/>
                </el-tooltip>
              </el-col>
              <el-col :span="8"> <!-- 按钮占据8个单位 -->
                <el-button class="normalBtn"
                           @click="downLoad_Dataset(dataFormone.trainDataset)" size="small" style="margin-left: 10px;">下载
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="测试数据集">
            <el-row>
              <el-col :span="16"> <!-- 输入框占据16个单位 -->
                <el-tooltip class="item" effect="dark" :content="dataFormone.trainDataset" placement="top">
                  <el-input v-model="dataFormone.testDataset" disabled
                            style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                            :style="{ 'max-width': '100%' }"/>
                </el-tooltip>
              </el-col>
              <el-col :span="8"> <!-- 按钮占据8个单位 -->
                <el-button class="normalBtn"
                           @click="downLoad_Dataset(dataFormone.testDataset)" size="small" style="margin-left: 10px;">下载
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-col>


        <el-col :span="8">
          <el-form-item label="预处理方法" >
            <el-input v-model="dataFormone.pretreatment"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="样本类型" >
            <el-input v-model="dataFormone.sampleType"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="样本尺寸" >
            <el-input v-model="dataFormone.sampleSize"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练次数" >
            <el-input v-model="dataFormone.trainTimes"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练批次" >
            <el-input v-model="dataFormone.trainBatch"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="损失函数" >
            <el-input v-model="dataFormone.lossFunction"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="正则化" >
            <el-input v-model="dataFormone.regularization"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="学习率" >
            <el-input v-model="dataFormone.learningRate"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="优化器" >
            <el-input v-model="dataFormone.optimizer"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练结果" >
            <el-input v-model="dataFormone.trainResults"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="测试结果" >
            <el-input v-model="dataFormone.testResults"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="评价指标">
            <el-input v-model="dataFormone.metrics" disabled/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-col :span="8">
        <el-form-item label="模型访问路径">
          <el-input v-model="dataFormone.modelUrl" disabled/>
        </el-form-item>
      </el-col>

      <el-col :span="8">
        <el-form-item label="JAR包大小">
          <el-input v-model="dataFormone.jarSize" disabled/>
        </el-form-item>
      </el-col>

      <el-col :span="8">
        <el-form-item label="运行所需资源大小">
          <el-input v-model="dataFormone.runSize" disabled/>
        </el-form-item>
      </el-col>
    </el-form>



    <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
      <el-button @click="xiugai" class="normalBtn"  style="margin-right: 8%;width: 8%">修改</el-button>
      <el-button v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()" style="width: 8%">返回</el-button>
    </span>

    <xiugai v-if="xiugaiVisible" ref="xiugai" @success="refreshGroup"/>

  </el-dialog>
</template>

<script>
import {getEvaluationObj, addObj,downLoad_Dataset_s} from '/src/api/al/stateEvaluation/stateEvaluationBase'
import xiugai from './stateEvaluation-xiugai1.vue'
import {fetchList1} from '/src/api/al/stateEvaluation/stateEvaluationOne'
import {ElMessage} from "element-plus";


export default {
  components: {
    xiugai
  },
  data() {
    return {
      searchForm1: {
        modelName:''
      },
      visible: false,
      canSubmit: false,
      dataForm: {
        modelName:'',
        modelProvider:'',
        modelObject:'',
        modelType:'',
        modelInvoke:'',
        modelTypeFirst:'',
        modelFunction:'',
        modelLibrary:'',
        modelCondition:'',
        modelAdvantage:'',
        modelDisadvantage:''
      },
      dataFormone:{
        modelName:'',
        modelFramework:'',
        device:'',
        trainDataset:'',
        testDataset:'',
        pretreatment:'',
        sampleType:'',
        sampleSize:'',
        trainTimes:'',
        trainBatch:'',
        lossFunction:'',
        regularization:'',
        learningRate:'',
        optimizer:'',
        trainResults:'',
        testResults:'',
        metrics:'',
        modelUrl:'',
        jarSize:0,
        runSize:0,
      },
      xiugaiVisible:false,


      dataRule: {
        // modelName: [
        //   { required: true, message: '模型名称不能为空', trigger: 'blur' }
        // ],
      }
    }
  },


  methods: {
    async refreshGroup(){
      let id =this.dataForm.id
      getEvaluationObj(id).then(response => {
        this.dataForm = response.data.data
        this.searchForm1.modelName = this.dataForm.modelName
        this.getDomainone()
      })
    },

    init(id) {
      this.visible = true
      this.canSubmit = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })

      getEvaluationObj(id).then(response => {
        this.dataForm = response.data.data
        this.searchForm1.modelName = this.dataForm.modelName
        this.$nextTick(()=>{
          this.getDomainone()}
        )
      })
    },
    getDomainone(page, params) {
      fetchList1(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm1)).then(response => {
        this.dataFormone = response.data.data.records[0]
        this.count = response.data.data.total
      })
    },
    //下载数据集
    downLoad_Dataset(datasetUrl) {
      if(datasetUrl==''){
        this.$notify.info('请先上传数据集，再下载数据集')
      }else{
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
    // 表单提交
    dataFormSubmit() {
      this.canSubmit = false
      this.visible = false
      this.$emit('refreshDataList')
    },
    // 重置表单
    closeDialog() {
      this.$nextTick(()=> {
      this.$refs['dataForm'].resetFields()
      this.$refs['dataForm1'].resetFields()
      })
      this.dataForm=''
      this.dataFormone=''
    },

    xiugai()
    {
      let id =this.dataForm.id
      this.xiugaiVisible = true
      this.$nextTick(() => {
        this.$refs.xiugai.init(id)
      })
    },

  }
}
</script>
