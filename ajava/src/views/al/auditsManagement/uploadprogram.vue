<template>
  <el-dialog
      title="程序包上传"
      v-model="dialogVisible"
      append-to-body
      width="15%"
      style="position:relative;margin: 300px auto;"
      :show-close="true"
      class="el-dialog__header"
      :before-close="handleClose">

    <div style="position: relative">

      <el-upload
          style="display: inline-block;width: 50%"
          :action="action"
          :file-list="modeList"
          :http-request="modeUpload"
          :limit="1"
      >

        <el-button style="margin-left: 15px;" class="normalBtn">上传程序包</el-button>
      </el-upload>

      <el-button style="margin-right:15px; float: right" class="normalBtn" @click="upload">确定上传</el-button>
    </div>
  </el-dialog>
</template>
<script>
import {uploadProgram} from "@/api/al/stateEvaluation/stateEvaluationBase";
import {updateProgramUrl, updateTestStatus} from "@/api/al/auditsManagement/auditsManagement";
import {ElMessage} from "element-plus";

export default {
  data() {
    return {
      dialogVisible: false,

      action: '',
      mode: {},
      modeList: [],
      programUrl: '',
      alModelName: ''
    };
  },
  methods: {
    init(alModelName) {
      this.dialogVisible = true;
      this.alModelName = alModelName
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
          this.programUrl = response.data.data
          updateProgramUrl({
            alModelName: this.alModelName,
            programUrl: this.programUrl
          }).then( res => {
            updateTestStatus({
              alModelName: this.alModelName,
              programUrl: this.programUrl,
              isPass: 2
            }).then(response => {
              ElMessage({
                message: "重新测试",
                type: 'warning'
              })
              this.$emit('getDataList')
            })
          })

          this.dialogVisible = false
        }).catch(() => {
          this.$notify.warning('上传失败')
        })
      }
    },

    handleClose(done) {
      this.modeList = []
      this.mode = {}
      done();
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
