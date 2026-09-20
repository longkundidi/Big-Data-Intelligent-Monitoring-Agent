<template>
  <el-dialog
    append-to-body
    width="25%"
    :close-on-click-modal="false"
    v-model="visible"
    @close="closeDialog()"
    :show-close="true"
    class="el-dialog__header"
  >

    <div slot="title" style="font-size: 25px;text-align:center;padding-bottom: 8%">
      <span>模型测试</span>
    </div>


    <el-form ref="dataForm" :model="dataForm"  label-width="140px" @keyup.enter.native="dataFormSubmit()" style="padding-bottom: 8%">

      <el-form-item label="模型输入举例：">
        <span>{{this.input1}}</span>
      </el-form-item>
      <el-form-item label="模型输出举例：">
        <span>{{this.output1}}</span>
      </el-form-item>


      <div class="tableTitle"><span class="midText">开始测试</span></div>

      <el-form-item label="测试数据：" prop="input" style="padding-top: 7%">
        <el-input style="width: 75%" v-model="dataForm.input"  :disabled="huidiao"/>
      </el-form-item>

      <el-form-item label="输出数据：" prop="output">
        <el-input style="width: 75%" v-model="dataForm.output" :disabled="!huidiao" />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button style="width: 20%" class="normalBtn" @click="dataFormSubmit()" v-if="!huidiao">开始测试</el-button>
      <el-button style="width: 20%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>
  </el-dialog>
</template>

<script>

import {textExtraction} from "/src/api/al/altest/altest";
import {getExtractionObj} from "/src/api/al/knowledgeExtraction/knowledgeExtraction";

export default {
  data() {
    return {
      visible: false,
      dataForm: {
      },
      huidiao:false,
      alid:'',
      input1:'',
      output1:''
    }
  },
  methods: {
    init(id,input1,output1) {
      this.visible = true
      this.input1 = input1
      this.output1 = output1
      getExtractionObj(id).then(response => {
        this.dataForm = response.data.data
        this.dataForm.input = ''
        this.dataForm.output = ''
      })
    },
    // 表单提交
    dataFormSubmit() {
      textExtraction('算法测试',this.dataForm).then(response => {
        this.dataForm.output = response.data.data
        this.huidiao = !this.huidiao
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
