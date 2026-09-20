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
<!--        <JsonViewer :value=this.input></JsonViewer>-->
        <div style="padding-top: 5%">{{this.input}}</div>
      </el-form-item>

      <el-form-item label="模型输出举例：">
        <JsonViewer :value=this.output></JsonViewer>
      </el-form-item>


      <div class="tableTitle"><span class="midText">开始测试</span></div>

      <el-form-item label="测试数据：" prop="taskMsg" style="padding-top: 5%">
        <el-input type="textarea" style="width: 75%" :rows="5" v-model="dataForm.taskMsg" :disabled="loading"/>
      </el-form-item>

      <el-form-item label="输出数据：" prop="taskResult">
        <JsonViewer :value=this.dataForm.taskResult></JsonViewer>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button style="width: 20%" class="normalBtn" :loading="loading" :disabled="loading" @click="dataFormSubmit()">开始测试</el-button>
      <el-button style="width: 20%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>

    </div>
  </el-dialog>
</template>

<script>

import {runModelTest} from "/src/api/al/altest/altest";
import JsonViewer from 'vue-json-viewer'


export default {
  data() {
    return {
      visible: false,
      dataForm: {
        alId:'',
        taskMsg:'',
        taskResult:'',
        isjson:''
      },
      loading:false,
      input:'',
      output:'',
      taskState:''

    }
  },
  components: {
    JsonViewer
  },

  methods: {
    init(id,input,output,isjson) {
      this.visible = true
      this.dataForm.alId = id
      this.input = input
      this.output = JSON.parse(output)
      this.dataForm.isjson = isjson
    },

    // 表单提交
    dataFormSubmit() {
      this.loading = true
      this.dataForm.taskResult = {}
      runModelTest({
        alId: this.dataForm.alId,
        alClass: 'alStateEvaluation',
        taskMsg: this.dataForm.taskMsg
      }).then(response => {
        const task = response.data.data
        this.taskState = task.taskState
        this.dataForm.taskResult = this.parseResult(task.taskResult)
        if (task.taskState !== 2) {
          this.$message.error(this.dataForm.taskResult.message || '模型测试失败')
        }
      }).catch(error => {
        this.dataForm.taskResult = {message: error?.message || '模型测试请求失败'}
      }).finally(() => {
        this.loading = false
      })
    },
    parseResult(value) {
      if (typeof value !== 'string') return value || {}
      try {
        return JSON.parse(value)
      } catch (error) {
        return {message: value}
      }
    },
    // 重置表单
    closeDialog() {
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
