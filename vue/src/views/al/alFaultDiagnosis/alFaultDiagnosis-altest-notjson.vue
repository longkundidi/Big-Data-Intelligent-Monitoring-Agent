<template>
  <el-dialog
      append-to-body
      width="30%"
      :close-on-click-modal="false"
      :show-close="true"
      class="el-dialog__header"
      @close="closeDialog()"
  >

    <div slot="title" style="font-size: 25px;text-align:center;padding-bottom: 5%">
      <span>模型测试</span>
    </div>

    <el-form ref="dataForm" :model="dataForm" label-width="140px" @keyup.enter.native="dataFormSubmit()"
             style="padding-bottom: 3%">

      <el-form-item label="模型输入举例：">
        <span>{{ this.input }}</span>
      </el-form-item>
      <el-form-item label="模型输出举例：">
        <span>{{ this.output }}</span>
      </el-form-item>

      <div class="tableTitle"><span class="midText">开始测试</span></div>
      <el-form-item label="测试数据：" prop="taskMsg" style="padding-top: 7%">
        <el-input type="textarea" :rows="4" style="width: 75%" v-model="dataForm.taskMsg" :disabled="huidiao"/>
      </el-form-item>
      <el-form-item label="输出数据：" prop="taskResult">
        <el-input type="textarea" :rows="4" style="width: 75%" v-model="dataForm.taskResult" :disabled="!huidiao"/>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button style="width: 20%" class="normalBtn" @click="dataFormSubmit()" v-if="!huidiao">开始测试</el-button>
      <el-button style="width: 20%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>
  </el-dialog>
</template>

<script>

import {startJob, getObj, FaultDiagnosisTest} from "/src/api/al/altest/altest";

export default {
  data() {
    return {
      visible: false,
      dataForm: {
        alId: '',
        taskMsg: '',
        taskResult: '',
        isjson: ''
      },
      huidiao: false,
      alid: '',
      input: '',
      output: ''
    }
  },
  methods: {
    init(id, input, output, isjson) {
      this.visible = true
      this.dataForm.alId = id
      this.input = input
      this.output = output
      this.dataForm.isjson = isjson
    },
    // 表单提交
    dataFormSubmit() {
      this.dataForm.taskResult = '""'
      FaultDiagnosisTest(this.dataForm).then(response => {
        this.alid = response.data.data[0]
        let alModelType = response.data.data[1]
        startJob(this.alid, alModelType).then(response => {
          getObj(this.alid).then(response => {
            this.dataForm.taskResult = response.data.data.taskResult
            this.huidiao = !this.huidiao
          })
        })
      }).catch(() => {
      })
    },
    // 重置表单
    closeDialog() {
      this.huidiao = false
      this.$refs['dataForm'].resetFields()
      this.$emit('refreshDataList')
    }
  },
}
</script>
<style>
.no-header-dialog .el-dialog__header {
  display: none;
}

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
