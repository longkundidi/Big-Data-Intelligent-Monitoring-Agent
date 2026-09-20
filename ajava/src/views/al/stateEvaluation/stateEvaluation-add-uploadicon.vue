<template>
  <el-dialog
      title="图标上传"
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
          accept=".jpg,.png"
          :file-list="modeList"
          :http-request="modeUpload"
          :limit="1"
      >

        <el-button style="margin-left: 20px;" class="normalBtn">上传图片</el-button>
      </el-upload>

      <el-button style="margin-right:20px; float: right" class="normalBtn" @click="upload">确定上传</el-button>

    </div>
  </el-dialog>
</template>
<script>
import {uploadIcon} from "@/api/al/stateEvaluation/stateEvaluationBase";

export default {
  data() {
    return {
      dialogVisible: false,
      modelName: '',
      action: '',
      mode: {},
      modeList: []
    };
  },
  methods: {
    init(modelName) {
      this.dialogVisible = true;
      this.modelName = modelName
    },

    modeUpload: function (item) {
      this.mode = item.file
    },

    upload: function () {
      let file = new FormData()
      if (this.mode.uid === undefined) {
        this.$notify.warning('没有图标')
      } else {
        file.append('file', this.mode)
        uploadIcon(file).then(response => {
          this.$notify.success('上传成功')
          this.dialogVisible = false
          this.modeList = []
          this.mode = {}
          // this.visible = false
          this.$emit('getIconUrl', response.data.data)
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
:deep(.el-dialog) {
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

:deep(.el-dialog .el-dialog__body) {
  flex: 1;
  overflow: auto;
  padding: 0 !important;
}
</style>
