<template>
  <el-dialog
    append-to-body
    width="35%"
    :close-on-click-modal="false"
    v-model="visible"
    :show-close="true"
    class="el-dialog__header"
    @close="closeDialog()"
  >

    <div slot="title" style="font-size: 25px;text-align:center;padding-bottom: 5%">
      <span>模型测试</span>
    </div>

    <div  v-loading="loading"
          element-loading-text="模型测试中"
          :element-loading-spinner="Check"
          element-loading-background="rgba(0, 0, 0, 0.3)">


    <el-form ref="dataForm" :model="dataForm"  label-width="140px" @keyup.enter.native="dataFormSubmit()"  style="padding-bottom: 5%"
            >

      <el-form-item label="模型输入举例：">
        <span>{{this.input}}</span>
      </el-form-item>

      <el-form-item label="模型输出举例：">
        <JsonViewer :value=this.output></JsonViewer>
      </el-form-item>


      <div class="tableTitle"><span class="midText">开始测试</span></div>

      <el-form-item label="测试数据：" prop="taskMsg" style="padding-top: 5%">
        <el-input type="textarea"  style="width: 75%" :rows="5" v-model="dataForm.taskMsg"  :disabled="huidiao"/>
      </el-form-item>

      <el-form-item label="输出数据：" prop="taskResult">
        <JsonViewer :value=this.dataForm.taskResult></JsonViewer>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button style="width: 20%" class="normalBtn" @click="dataFormSubmit()" v-if="!huidiao"  >开始测试</el-button>
      <el-button style="width: 20%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>

    </div>
  </el-dialog>
</template>

<script>

import {startJob, getObj, domainModelTest} from "/src/api/al/altest/altest";
import JsonViewer from 'vue-json-viewer'
export default {
  data() {
    return {
      visible: false,
      dataForm: {
        alId:'',
        taskMsg:'',
        taskResult:'',
        isjson: ''
      },
      loading:false,
      huidiao:false,
      alid:'',
      input:'',
      output:'',
      taskState:'',
      timer: null,
      startTime: 0 // 初始开始请求时间

    }
  },
  components: {
    JsonViewer
  },

  beforeDestroy () {
    clearTimeout(this.timer)
  },

  methods: {
    init(id, input, output, type) {
      this.visible = true
      this.dataForm.alId = id
      this.input = input
      this.output = JSON.parse(output)
      this.dataForm.isjson = type === 'evaluation' ? 1 : 0
    },

    loopResult () {
      let i=0;
      console.log("开始轮循请求");
      let  timer = setInterval(() => {
        this.fun(timer,i++)
      }, 3000)
      return true
    },
    fun (timer,i) {
      setTimeout(()=>{
        console.log("开始轮循请求：");
        console.log("次数：" + i);
        getObj(this.alid).then(response => {
          this.taskState = response.data.data.taskState
          if(this.taskState !== 1 )
          {
            this.loading = false
            // this.fullscreenLoading = false
            this.dataForm.taskResult = JSON.parse(response.data.data.taskResult)
            this.huidiao = !this.huidiao
            clearInterval(timer)
          }
        })
        if(i>=40){
          this.$message.warning('模型请求超时')
          this.loading = false
          this.huidiao = !this.huidiao
          // this.fullscreenLoading = false;
          clearInterval(timer)
        }
      }, 0)
    },

    // 表单提交
    dataFormSubmit() {
      this.loading = true
      this.dataForm.taskResult = '""'
      domainModelTest(this.dataForm).then(response => {
        if (response.data.code !== "200"){
          this.$message.warning('测试数据格式错误，请重试')
          this.loading = false
        }
        else
        {
          this.alid = response.data.data[0]
          let alModelType = response.data.data[1]
        startJob(this.alid,alModelType,'算法测试').then(response =>{
          getObj(this.alid).then(response =>{
            this.taskState = response.data.data.taskState
            this.loopResult()
          })
        })
        }
      }).catch(() => {
        this.loading = false
      })
    },
    // 重置表单
    closeDialog() {
      clearTimeout(this.timer)
      this.huidiao = false
      this.loading = false
      this.taskState = ''
      this.$emit('refreshDataList')
      this.$refs['dataForm'].resetFields()
    }
  },
}
</script>
<style>
.tableTitle {
  position: relative;
  margin: 0 auto;
  width: 100%;
  height: 1px;
  background-color: #d4d4d4;
  text-align: center;
  font-size: 14px;
  color: rgba(101, 101, 101, 1);
}
.midText {
  position: absolute;
  left: 50%;
  background-color: #ffffff;
  padding: 0 15px;
  transform: translateX(-50%) translateY(-50%);
}

</style>
