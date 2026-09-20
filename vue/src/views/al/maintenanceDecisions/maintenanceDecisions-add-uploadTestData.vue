<template>
  <el-dialog
      title="测试数据集上传"
      v-model="dialogVisible"
      append-to-body
      width="15%"
      :show-close="true"
      class="el-dialog__header"
      style="position:relative;margin: 300px auto;"
      :before-close="handleClose">

    <div style="position: relative">

      <el-upload
          style="display: inline-block;width: 50%"
          :action="action"
          :file-list="modeList"
          :http-request="modeUpload"
          :limit="1"
      >

        <el-button style="margin-left: 15px;" class="normalBtn">上传数据集</el-button>
      </el-upload>

      <el-button style="margin-right:15px; float: right" class="normalBtn" @click="upload">确定上传</el-button>
    </div>
  </el-dialog>
</template>
<script>
import {uploadProgram} from "@/api/al/maintenanceDecisions/maintenanceDecisionsBase";

export default {
  data() {
    return {
      dialogVisible: false,

      action: '',
      mode: {},
      modeList: [],
      testDataset: ''
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
        this.$notify.warning('没有测试数据集')
      } else {
        file.append('file', this.mode)
        uploadProgram(file).then(response => {
          this.$notify.success('上传成功')
          this.modeList = []
          this.mode = {}
          this.testDataset = response.data.data
          this.$emit('getTestDataSet', this.testDataset)
          this.dialogVisible = false
          // this.visible = false
          // this.$emit('refreshDataList')
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
