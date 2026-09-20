<template>
  <div
      v-loading.fullscreen.lock="loading"
      element-loading-text="正在进行评价指标校验...">
    <el-dialog
        title="程序包上传"
        v-model="dialogVisible"
        append-to-body
        width="20%"
        style="position:relative;margin: 300px auto;"
        :show-close="true"
        class="el-dialog__header"
        @closed="handleClose">

      <div style="display: flex;flex-direction: column">
        <div style="display: flex; justify-content: center;">
          <el-upload
              style=""
              :action="action"
              :file-list="modeList"
              :http-request="modeUpload"
              :limit="1"
          >
            <el-button style="margin-left: 15px;" class="normalBtn">上传程序包</el-button>
          </el-upload>
          <el-button  style="margin-left:15px; " class="normalBtn" @click="upload">确定上传</el-button>
        </div>
        <el-tag type="success" v-if="isGetMetric" style="display: inline-block;height: 30px;line-height: 30px;vertical-align: middle;text-align: center;">{{ metircInfo }}</el-tag>
      </div>
    </el-dialog>
  </div>

</template>
<script>
import {autoGetMetrics, startJob, uploadProgram,getObj,getList} from "@/api/al/resourceScheduling/resourceSchedulingBase";
export default {
  data() {
    return {
      dialogVisible: false,
      loading:false,
      action: '',
      mode: {},
      modeList: [],
      programurl: '',
      sys_metrics:[],
      uploadSucess:false,
      isPass:false,
      metircInfo:'',
      isGetMetric:false
    };
  },
  methods: {
    init() {
      this.dialogVisible = true;
    },

    modeUpload: function (item) {
      this.mode = item.file
    },

    upload: function () {
      let file = new FormData()
      if (this.mode.uid === undefined) {
        this.$notify.warning('没有程序包')
      } else {
        file.append('file', this.mode)
        uploadProgram(file).then(response => {
          this.$notify.success('上传成功')
          this.modeList = []
          this.mode = {}
          this.programurl = response.data.data
          this.uploadSucess=true
          this.$emit('getprogramurl', this.programurl)
          this.$nextTick(()=>{
            this.getMetrics(this.programurl)
          })
        }).catch(() => {
          this.$notify.warning('上传失败')

        })
      }

    },
    getMetrics(url){
      if(this.uploadSucess!==true){
        this.$message.info(`请等待程序包上传成功后，再进行校验`);
      }else{
        this.loading=true
        this.getSysMetric()
        autoGetMetrics({programUrl:url}).then(res=>{
          let taskId = res.data.data[0]
          let alModelType = res.data.data[1]
          //校验评价指标
          startJob(taskId, alModelType,"自动获取评价指标").then(res=>{
            getObj(taskId).then(response => {
              this.testState = response.data.data.taskState
              this.loopResult(taskId)
            })
          })
        })
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

          this.testState = response.data.data.taskState
          if (this.testState !== 1) {
            this.testResult = JSON.parse(response.data.data.taskResult)
            if (this.testResult.includes("error")) {
              if (this.testResult.includes("File not found in MinIO")) {
                this.testResult_process.resultInfo = "测试未通过！未在MinIO中找到程序包！"
                this.testResult = JSON.parse(this.testResult)
                this.testResult_process.resultDetail = this.testResult.file
              } else if (this.testResult.includes("Failed to extract the file. This may be because the  file name does not match the zip file name, or there is no  file.")) {
                this.testResult_process.resultInfo = "测试未通过！提取模型文件失败，可能是存放评价指标的文件名与程序包名不相同或没有文件！"
              }  else {
                this.testResult_process.resultInfo = "发生意外错误！"
              }
              this.$message.error( this.testResult_process.resultInfo)
            } else {
              if (this.testResult.includes("success")) {
                this.metrics = JSON.parse(this.testResult).data
                if(this.metrics!==null){ //['F1', 'precise', 'accuracy', 'recall']
                  if(this.sys_metrics.every(element => this.metrics.includes(element))){
                    this.$message.success('通过校验，所有评价指标都已存在！');
                    this.metircInfo='通过校验，所有评价指标都已存在！'
                    this.loading=false
                    this.isPass=true
                    this.isGetMetric=true
                  }  else{
                    let miss_metrics= this.sys_metrics.filter(element => !this.metrics.includes(element));
                    this.$message.error(`未通过校验，缺少以下评价指标：${miss_metrics.join(', ')}, 请补充完整后再提交程序包`);
                    this.metircInfo=`未通过校验，缺少以下评价指标：${miss_metrics.join(', ')}, 请补充完整后再提交程序包`
                    this.loading=false
                    this.isPass=false
                    this.isGetMetric=true
                  }
                }
                this.$emit('getisPass', this.isPass)
              }
            }
            clearInterval(timer);

          }
        })
        if (i >= 60) {
          this.$message.warning('服务请求超时,请再次进行评价指标校验')
          this.loading = false
          clearInterval(timer);
        }
      }, 0)
    },
    //获取系统的评价指标列表
    getSysMetric(){
      getList({alType:"分类任务"}).then(res=>{
        this.sys_metrics=res.data.data
      })
    },

    handleClose() {
      this.modeList = []
      this.mode = {}
      this.programurl=''
      this.testResult={}
      this.isPass=false
      this.metircInfo=null
      this.dialogVisible=false
      this.isGetMetric=false

    },

  }
};
</script>

<style scoped>
::v-deep .el-dialog {
  display: flex;
  flex-direction: column;
  margin: 0 !important;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-height: calc(100% - 30px);
  max-width: calc(100% - 30px);
}

::v-deep .el-dialog .el-dialog__body {
  flex: 1;
  overflow: auto;
}
</style>
