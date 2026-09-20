<template>
  <el-dialog
      append-to-body
      width="40%"
      :close-on-click-modal="false"
      v-model="visible"
      :show-close="true"
      class="el-dialog__header"
      @close="closeDialog()"
  >

    <div slot="title" style="display: flex;padding-bottom: 30px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>

      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">修改</span>
      </div>
    </div>

    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="125px"
             @keyup.enter.native="dataFormSubmit()">

      <el-form-item label="算法类型" prop="alType">
        <el-select v-model="dataForm.alType" style="width: 100%">
          <el-option
              v-for="item in options"
              :key="item.alType"
              :value="item.alType"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="输入数据" prop="input">
        <el-input v-model="dataForm.input" placeholder="输入数据"/>
      </el-form-item>
      <el-form-item label="输出数据" prop="output">
        <el-input v-model="dataForm.output" placeholder="输出数据"/>
      </el-form-item>
      <el-form-item label="算法适用" prop="alSuit">
        <el-input v-model="dataForm.alSuit"
                  maxlength="20"
                  show-word-limit
                  placeholder="算法适用"/>
      </el-form-item>
      <el-form-item label="算法简介" prop="alBrief">
        <el-input v-model="dataForm.alBrief"
                  maxlength="50"
                  show-word-limit
                  placeholder="算法简介"/>
      </el-form-item>
      <el-form-item label="提供人员" prop="creator">
        <el-input v-model="dataForm.creator" placeholder="提供人员"/>
      </el-form-item>
      <el-form-item label="算法原理图">
        <el-button class="normalBtn" @click="upload">上传</el-button>
      </el-form-item>

      <el-form-item label="算法访问路径">
        <el-input v-model="dataForm.alUrl" />
      </el-form-item>

      <el-form-item label="JAR包大小">
        <el-input v-model="dataForm.jarSize" />
      </el-form-item>

      <el-form-item label="运行所需资源大小">
        <el-input v-model="dataForm.runSize" />
      </el-form-item>


    </el-form>
    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button style="width: 13%" v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()">完成修改</el-button>
      <el-button style="width: 13%" class="normalBtn" @click="visible = false" v-if="!geticon">取消</el-button>
    </span>
    <uploadicon v-if="dialogVisible" ref="upload" @geticonurl="geticonurl"></uploadicon>
  </el-dialog>
</template>

<script>
import {getDimensionalityObj, putObj} from '/src/api/al/dimensionalityReduction/dimensionalityReduction'
import uploadicon from "./dimensionalityReduction-add-uploadicon.vue";

export default {
  data() {
    return {
      options:
          [
            {alType: '线性降维'},
            {alType: '非线性降维'}
          ],
      visible: false,
      canSubmit: false,
      dialogVisible: false,
      geticon: false,
      dataForm: {
        creator: '',
        id: '',
        alType: '',
        alName: '',
        input: '',
        output: '',
        alBrief: '',
        alSuit: '',
        alUrl:'',
        jarSize:0,
        runSize:0,
      },
      dataRule: {
        alType: [
          {required: true, message: '算法类型不能为空', trigger: 'blur'}
        ],
      }
    }
  },
  components: {
    uploadicon
  },
  methods: {
    init(id) {
      this.visible = true
      this.canSubmit = true
      this.geticon = false
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (id) {
          getDimensionalityObj(id).then(response => {
            this.dataForm = response.data.data
          })
        }
      })
    },
    geticonurl(iconurl) {
      this.geticon = true
      this.dataForm.iconUrl = iconurl
    },
    upload() {
      if (this.dataForm.alName === '') {
        this.$notify.warning("必要参数未配置")
      } else {
        let alName = this.dataForm.alName
        this.dialogVisible = true
        this.$nextTick(() => {
          this.$refs.upload.init(alName)
        })
      }
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.canSubmit = false
          putObj(this.dataForm).then(response => {
            this.$notify.success('修改成功')
            this.visible = false
            this.$emit('refreshDataList')
          }).catch(() => {
            this.canSubmit = true
          })
        }
      })
    },
    // 重置表单
    closeDialog() {
      this.$refs['dataForm'].resetFields()
    }
  }
}
</script>
<style>
.no-header-dialog .el-dialog__header {
  display: none;
}
</style>
