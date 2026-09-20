<template>
  <el-dialog
    append-to-body
    width="35%"
    :close-on-click-modal="false"
    v-model="visible"
    @close="closeDialog()"
    :show-close="true"
    class="el-dialog__header"
  >
    <div v-loading="loading">
      <div slot="title" style="font-size: 25px;text-align:center;padding-bottom: 8%">
        <span>算法测试</span>
      </div>

      <el-form ref="dataForm" :model="dataForm"  label-width="140px" @keyup.enter.native="dataFormSubmit()" style="padding-bottom: 8%">
        <el-form-item label="算法输入举例：">
          <span>{{this.input}}</span>
        </el-form-item>
        <el-form-item label="算法输出举例：">
          <span>{{this.output}}</span>
        </el-form-item>

        <div class="tableTitle"><span class="midText">开始测试</span></div>

        <el-form-item label="测试数据：" prop="taskMsg" style="padding-top: 7%">
          <el-input style="width: 75%" v-model="dataForm.taskMsg"  :disabled="huidiao"/>
        </el-form-item>
        <el-form-item label="下载路径：" prop="taskResult">
          <el-input style="width: 75%" v-model="dataForm.taskResult" :disabled="!huidiao" />
          <el-button class="normalBtn"
                     @click="downLoad_Dataset(dataForm.taskResult)" size="small" style="margin-left: 10px;">下载
          </el-button>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button style="width: 20%" class="normalBtn" @click="dataFormSubmit()" v-if="!huidiao">开始测试</el-button>
      <el-button style="width: 20%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>
    </div>

  </el-dialog>
</template>

<script>

import {startJob, getObj, featureExtractionTest} from "/src/api/al/altest/altest";
import {downLoad_Dataset_d} from "@/api/al/featureExtraction/featureExtraction";
import {ElMessage} from "element-plus";

export default {
  data() {
    return {
      visible: false,
      dataForm: {
        alId:'',
        taskMsg:'',
        taskResult:''
      },
      huidiao:false,
      alid:'',
      input:'',
      output:'',
      loading:false
    }
  },
  methods: {
    init(id,input,output) {
      this.visible = true
      this.dataForm.alId = id
      this.input = input
      this.output = output
    },
    // 表单提交
    dataFormSubmit() {
      this.loading=true
      this.dataForm.taskResult = '""'
      featureExtractionTest(this.dataForm).then(response => {
        this.alid = response.data.data[0]
        let alModelType = response.data.data[1]
        startJob(this.alid, alModelType,'算法测试').then(response =>{
          getObj(this.alid).then(response =>{
            this.loading=false
          this.dataForm.taskResult = response.data.data.taskResult
          this.huidiao = !this.huidiao
          }).catch(error => {
            console.error('getObj:', error); // 仅在控制台打印错误
            this.loading = false; // 避免一直显示加载状态
          })
        }).catch(error => {
          console.error('startJob:', error); // 仅在控制台打印错误
          this.loading = false; // 避免一直显示加载状态
        })
      }).catch(error => {
        console.error('featureExtractionTest:', error); // 仅在控制台打印错误
        this.loading = false; // 避免一直显示加载状态
      })
    },
    //下载数据集
    downLoad_Dataset(datasetUrl){
      datasetUrl=JSON.parse(datasetUrl).filepath
      if(datasetUrl==''){
        this.$notify.info('该路径未找到输出数据集，请先运行算法再下载')
      }else{
        downLoad_Dataset_d({filePath:datasetUrl}).then(res=>{

          if(res.data===null){//如果数据集不在minio
            this.$notify.error('该路径下找不到数据集，请重新上传后再下载')
          }else{
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            let filename=res.config.params.filePath
            link.setAttribute('download', filename.split('/').pop());
            document.body.appendChild(link);
            link.click();
            this.$nextTick(()=>{
              this.$notify.success('开始下载')
            })
          }
        }).catch(error => {
          console.error('下载文件时发生错误:', error);
          ElMessage.error('文件下载失败: ' + error.message);
        });
      }
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
