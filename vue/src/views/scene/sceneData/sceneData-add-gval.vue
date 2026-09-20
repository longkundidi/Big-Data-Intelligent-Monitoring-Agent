<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="20%"
      :show-close="true"
      title="导入感知变量"
      @close="closeDialog">

    <template #footer>
      <div style="display: flex;justify-content: center;margin-top: 20px">
        <el-upload
            action=""
            :on-change="handleValChange"
            :show-file-list="false"
            :auto-upload="false">
          <el-button class="addBtn" style="margin-right: 10px">上传</el-button>
        </el-upload>
        <el-button class="disMissBtn" @click="closeDialog">取消</el-button>
      </div>
    </template>

    <el-dialog v-model="showPreviewDialog" title="数据预览" style="width: 35%;height: 60%" draggable>
      <div v-loading="loadingInstance" element-loading-text="正在导入...">
        <div class="dlgPreview">
          <el-card shadow="hover" style="width: 95%;">
            <el-table :data="previewData" stripe style="width: 100%">
              <el-table-column v-for="column in previewColumns" :key="column.prop" :prop="column.prop"
                               :label="column.label">
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
      <div class="footerPreview">
        <el-button class="normalBtn" @click="handleClose">取 消</el-button>
        <el-button class="normalBtn" @click="handleConfirmedData(previewData)">确 认</el-button>
      </div>
    </el-dialog>
  </el-dialog>

</template>

<script>
import {ElMessage, ElNotification} from "element-plus";
import * as XLSX from 'xlsx/xlsx.mjs'

export default {

  data() {
    return {
      visible: false,
      showPreviewDialog: false,
      loadingInstance: false,
      previewColumns: [],
      previewData: [],
      errorShow: false,
      bomSelectedValue: '',
      loading: false
    }
  },
  methods: {
    closeDialog() {
      this.visible = false;
      this.bomSelectedValue = '';
      this.errorShow = false;
      this.$emit('update:modelValue', false)
    },
    handleValChange(file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      if (fileContent) {
        if (fileType === 'xlsx' || fileType === 'xls') {
          this.importValFile(fileContent)
        } else {
          ElMessage({
            message: "附件格式错误，请重新上传！",
            type: 'warning'
          })
        }
      } else {
        ElMessage({
          message: "请上传附件！",
          type: 'warning'
        })
      }

    },
    importValFile(obj) {
      const reader = new FileReader()
      reader.readAsArrayBuffer(obj)
      reader.onload = () => {
        const buffer = reader.result
        const bytes = new Uint8Array(buffer)
        const length = bytes.byteLength
        let binary = ''
        for (let i = 0; i < length; i++) {
          binary += String.fromCharCode(bytes[i])
        }
        const wb = XLSX.read(binary, {
          type: 'binary'
        })
        const outData = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]])
        if (outData.length === 0) {
          this.$message({
            type: 'warning',
            message: '附件为空，请重新上传！'
          })
        } else {
          this.previewColumns = Object.keys(outData[0]).map(key => ({
            prop: key,
            label: key,
          }))
          this.previewData = outData
          this.showPreviewDialog = true
        }
      }
    },
    handleClose() {
      this.showPreviewDialog = false
      this.previewData = []
      ElMessage({
        showClose: true,
        message: '取消上传！',
      })
    },
    handleConfirmedData(outData) {
      this.$emit('outValData', outData)
      this.showPreviewDialog = false
      this.visible = false
      this.$emit('update:modelValue', false)
    },
  }
}
</script>


<style scoped lang="scss">

.dlgPreview {
  height: 450px;
  overflow: auto;
  margin-left: 20px;
}

.footerPreview {
  text-align: center;
  margin-top: 20px;

  .el-button {
    height: 30px;
  }
}

</style>