<template>
  <el-dialog
    v-model="visible"
    :title="editing ? '修改算法' : '新增算法'"
    width="760px"
    append-to-body
    :close-on-click-modal="false"
    @closed="reset"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="algorithm-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="算法分类">
            <el-input :model-value="typeLabel" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="算法名称" prop="modelName">
            <el-input v-model="form.modelName" maxlength="100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="算法短名" prop="modelShortName">
            <el-input v-model="form.modelShortName" maxlength="64" placeholder="例如 REGTCN" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型类型" prop="modelType">
            <el-select v-model="form.modelType" style="width: 100%">
              <el-option label="深度学习模型" value="深度学习模型" />
              <el-option label="传统机器学习模型" value="传统机器学习模型" />
              <el-option label="统计学模型" value="统计学模型" />
              <el-option label="信号处理模型" value="信号处理模型" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="针对对象" prop="modelObject">
            <el-select v-model="form.modelObject" allow-create filterable style="width: 100%">
              <el-option label="电梯" value="电梯" />
              <el-option label="风电机组" value="风电机组" />
              <el-option label="轴承" value="轴承" />
              <el-option label="齿轮箱" value="齿轮箱" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="执行方式" prop="executorType">
            <el-select v-model="form.executorType" style="width: 100%">
              <el-option label="HTTP JSON" value="HTTP_JSON" />
              <el-option v-if="algorithmType === 'evaluation'" label="InfluxDB电梯监测" value="ELEVATOR_INFLUX_MONITOR" />
              <el-option v-if="algorithmType === 'diagnosis'" label="InfluxDB电梯诊断" value="ELEVATOR_INFLUX_DIAGNOSIS" />
              <el-option v-if="editing && form.executorType === 'SPRING_BEAN'" label="内置执行器" value="SPRING_BEAN" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="调用地址" prop="modelUrl">
            <el-input v-model="form.modelUrl" placeholder="http://host:port/createTask/" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="提供人员">
            <el-input v-model="form.modelProvider" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支持库">
            <el-input v-model="form.modelLibrary" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="程序包">
            <div class="upload-row">
              <el-input v-model="form.programUrl" readonly />
              <el-upload :show-file-list="false" :http-request="uploadPackage" accept=".zip">
                <el-button :loading="uploading">上传 ZIP</el-button>
              </el-upload>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="适用场景">
            <el-input v-model="form.modelCondition" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型优点">
            <el-input v-model="form.modelAdvantage" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型局限">
            <el-input v-model="form.modelDisadvantage" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="输入示例">
            <el-input v-model="form.input" type="textarea" :rows="5" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="输出示例">
            <el-input v-model="form.output" type="textarea" :rows="5" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="部署要求">
            <el-input v-model="form.deployRequire" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { createAlgorithm, updateAlgorithm } from '@/api/al/algorithmManagement'
import { uploadProgram } from '@/api/al/faultDiagnosisbase/alFaultDiagnosisBase'

const emptyForm = () => ({
  modelName: '',
  modelShortName: '',
  modelType: '深度学习模型',
  modelObject: '电梯',
  modelProvider: '',
  modelLibrary: '',
  modelCondition: '',
  modelAdvantage: '',
  modelDisadvantage: '',
  modelUrl: '',
  backupModelUrl: null,
  executorType: 'HTTP_JSON',
  executorConfig: null,
  programUrl: '',
  input: '',
  output: '',
  deployRequire: '',
  modelFunction: '',
  isjson: 0
})

export default {
  name: 'AlgorithmForm',
  emits: ['saved'],
  data() {
    return {
      visible: false,
      editing: false,
      algorithmType: 'evaluation',
      form: emptyForm(),
      uploading: false,
      submitting: false,
      rules: {
        modelName: [{ required: true, message: '请输入算法名称', trigger: 'blur' }],
        modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
        modelObject: [{ required: true, message: '请选择针对对象', trigger: 'change' }],
        modelShortName: [
          { required: true, message: '请输入算法短名', trigger: 'blur' },
          { pattern: /^[A-Za-z0-9_-]+$/, message: '仅允许字母、数字、下划线和短横线', trigger: 'blur' }
        ],
        executorType: [{ required: true, message: '请选择执行方式', trigger: 'change' }]
      }
    }
  },
  computed: {
    typeLabel() {
      return this.algorithmType === 'evaluation' ? '状态评估算法' : '故障诊断算法'
    }
  },
  methods: {
    init(type, algorithm = null) {
      this.algorithmType = type
      this.editing = Boolean(algorithm && algorithm.id)
      this.form = this.editing ? { ...emptyForm(), ...algorithm } : emptyForm()
      this.form.modelFunction = this.typeLabel
      this.visible = true
    },
    async uploadPackage({ file }) {
      this.uploading = true
      try {
        const body = new FormData()
        body.append('file', file)
        const response = await uploadProgram(body)
        this.form.programUrl = response.data.data
        this.$message.success('程序包上传成功')
      } finally {
        this.uploading = false
      }
    },
    async submit() {
      await this.$refs.formRef.validate()
      this.submitting = true
      try {
        const payload = { ...this.form }
        delete payload.createTime
        delete payload.editTime
        const response = this.editing
          ? await updateAlgorithm(this.algorithmType, this.form.id, payload)
          : await createAlgorithm(this.algorithmType, payload)
        this.$message.success(this.editing ? '修改成功' : '新增成功')
        this.visible = false
        this.$emit('saved', this.algorithmType, response.data.data)
      } finally {
        this.submitting = false
      }
    },
    reset() {
      this.form = emptyForm()
      this.$refs.formRef?.clearValidate()
    }
  }
}
</script>

<style scoped>
.algorithm-form {
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 12px;
}

.upload-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  width: 100%;
}
</style>
