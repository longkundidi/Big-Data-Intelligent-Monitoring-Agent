<template>
  <section v-loading="loading" class="algorithm-detail">
    <header class="detail-header">
      <div class="title-block">
        <div class="type-line">{{ typeLabel }}</div>
        <h2>{{ detail.modelName || '-' }}</h2>
        <div class="identity-line">
          <span>{{ detail.alCode || '-' }}</span>
          <span>{{ detail.modelShortName || '-' }}</span>
          <el-tag size="small" :type="status.type">{{ status.label }}</el-tag>
        </div>
      </div>
      <div class="actions">
        <el-button @click="openTest">模型测试</el-button>
        <el-button type="primary" @click="edit">修改</el-button>
        <el-button type="danger" plain @click="remove">删除</el-button>
      </div>
    </header>

    <el-divider />

    <div class="info-grid">
      <div v-for="item in fields" :key="item.label" class="info-item" :class="{ wide: item.wide }">
        <span class="label">{{ item.label }}</span>
        <span class="value">{{ item.value || '-' }}</span>
      </div>
    </div>

    <algorithm-form ref="editor" @saved="handleSaved" />

    <el-dialog v-model="testVisible" title="模型测试" width="680px" append-to-body :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="输入示例">
          <pre class="example">{{ detail.input || '-' }}</pre>
        </el-form-item>
        <el-form-item label="测试数据">
          <el-input v-model="testInput" type="textarea" :rows="7" />
        </el-form-item>
        <el-form-item label="输出结果">
          <pre class="result" :class="{ failed: testFailed }">{{ testResult }}</pre>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testVisible = false">关闭</el-button>
        <el-button type="primary" :loading="testing" @click="runTest">开始测试</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script>
import AlgorithmForm from './AlgorithmForm.vue'
import { deleteAlgorithm, getAlgorithm } from '@/api/al/algorithmManagement'
import { runModelTest } from '@/api/al/altest/altest'

export default {
  name: 'AlgorithmDetail',
  components: { AlgorithmForm },
  props: {
    nodeData: { type: Object, required: true }
  },
  emits: ['refreshData', 'clearInfo', 'update-node-data'],
  data() {
    return {
      loading: false,
      detail: {},
      testVisible: false,
      testing: false,
      testInput: '',
      testResult: '-',
      testFailed: false
    }
  },
  computed: {
    algorithmType() {
      return this.nodeData.parentType
    },
    typeLabel() {
      return this.algorithmType === 'evaluation' ? '状态评估算法' : '故障诊断算法'
    },
    status() {
      if (Number(this.detail.isCheck) === 1) return { label: '待审核', type: 'warning' }
      if (Number(this.detail.isPass) === 2) return { label: '待测试', type: 'warning' }
      if (Number(this.detail.isPass) === 1) return { label: '测试未通过', type: 'danger' }
      if (Number(this.detail.isDeployed) === 1) return { label: '待部署', type: 'warning' }
      return { label: '已部署', type: 'success' }
    },
    fields() {
      return [
        { label: '模型类型', value: this.detail.modelType },
        { label: '针对对象', value: this.detail.modelObject },
        { label: '执行方式', value: this.executorLabel(this.detail.executorType) },
        { label: '引用次数', value: `${this.detail.modelNum || 0} 次` },
        { label: '提供人员', value: this.detail.modelProvider },
        { label: '支持库', value: this.detail.modelLibrary },
        { label: '适用场景', value: this.detail.modelCondition, wide: true },
        { label: '调用地址', value: this.detail.modelUrl, wide: true },
        { label: '程序包', value: this.detail.programUrl, wide: true },
        { label: '输入示例', value: this.detail.input, wide: true },
        { label: '输出示例', value: this.detail.output, wide: true },
        { label: '部署要求', value: this.detail.deployRequire, wide: true }
      ]
    }
  },
  methods: {
    async getDetailInfo(node = this.nodeData) {
      this.loading = true
      try {
        const response = await getAlgorithm(node.parentType, node.id)
        this.detail = response.data.data || {}
      } finally {
        this.loading = false
      }
    },
    executorLabel(type) {
      return {
        HTTP_JSON: 'HTTP JSON',
        ELEVATOR_INFLUX_MONITOR: 'InfluxDB电梯监测',
        ELEVATOR_INFLUX_DIAGNOSIS: 'InfluxDB电梯诊断',
        SPRING_BEAN: '内置执行器'
      }[type] || type
    },
    edit() {
      this.$refs.editor.init(this.algorithmType, this.detail)
    },
    async handleSaved(type, algorithm) {
      this.detail = algorithm
      this.$emit('update-node-data', {
        ...algorithm,
        id: algorithm.id,
        label: algorithm.modelName,
        parentType: type,
        model: true,
        level: 3,
        showAdd: false
      }, 'id')
      this.$emit('refreshData', type)
    },
    async remove() {
      await this.$confirm(`确认删除“${this.detail.modelName}”？`, '删除算法', { type: 'warning' })
      await deleteAlgorithm(this.algorithmType, this.detail.id)
      this.$message.success('删除成功')
      this.$emit('clearInfo')
      this.$emit('refreshData', this.algorithmType)
    },
    openTest() {
      this.testInput = this.detail.input || ''
      this.testResult = '-'
      this.testFailed = false
      this.testVisible = true
    },
    async runTest() {
      this.testing = true
      this.testFailed = false
      try {
        const response = await runModelTest({
          alId: this.detail.id,
          algorithmType: this.algorithmType,
          taskMsg: this.testInput
        })
        const task = response.data.data || {}
        this.testFailed = Number(task.taskState) !== 2
        this.testResult = this.formatResult(task.taskResult)
      } catch (error) {
        this.testFailed = true
        this.testResult = error?.message || '模型测试请求失败'
      } finally {
        this.testing = false
      }
    },
    formatResult(value) {
      if (typeof value !== 'string') return JSON.stringify(value || {}, null, 2)
      try {
        return JSON.stringify(JSON.parse(value), null, 2)
      } catch (error) {
        return value
      }
    }
  }
}
</script>

<style scoped>
.algorithm-detail {
  min-height: 480px;
  padding: 28px 32px;
  background: #fff;
}

.detail-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.type-line {
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 14px;
}

h2 {
  margin: 0;
  color: #1f2937;
  font-size: 26px;
  letter-spacing: 0;
}

.identity-line {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
  color: #6b7280;
  font-size: 13px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-top: 1px solid #ebeef5;
  border-left: 1px solid #ebeef5;
}

.info-item {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  min-height: 52px;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.info-item.wide {
  grid-column: 1 / -1;
}

.label,
.value {
  padding: 15px 16px;
  line-height: 22px;
}

.label {
  background: #f7f8fa;
  color: #606266;
  font-weight: 600;
}

.value {
  min-width: 0;
  overflow-wrap: anywhere;
  color: #303133;
  white-space: pre-wrap;
}

.example,
.result {
  width: 100%;
  min-height: 72px;
  max-height: 220px;
  margin: 0;
  overflow: auto;
  padding: 12px;
  border: 1px solid #dcdfe6;
  background: #f7f8fa;
  color: #303133;
  font-family: Consolas, monospace;
  white-space: pre-wrap;
}

.result.failed {
  border-color: #f3b6b6;
  color: #b42318;
}

@media (max-width: 900px) {
  .algorithm-detail {
    padding: 20px;
  }

  .detail-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item.wide {
    grid-column: auto;
  }
}
</style>
