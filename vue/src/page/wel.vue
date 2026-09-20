<template>
  <div class="overview-page" v-loading="loading">
    <header class="page-header">
      <div class="title-block">
        <el-icon class="title-icon"><DataAnalysis /></el-icon>
        <div>
          <h2>算法管理总览</h2>
          <p>{{ updateText }}</p>
        </div>
      </div>

      <el-button :disabled="loading" @click="loadDashboard">
        <el-icon><Refresh /></el-icon>
        {{ loading ? '刷新中' : '刷新' }}
      </el-button>
    </header>

    <el-alert
      v-if="errorMessage"
      class="load-alert"
      :title="errorMessage"
      type="warning"
      show-icon
      :closable="false"
    />

    <section class="content-section algorithm-section">
      <div class="section-header">
        <div>
          <h3>算法资源</h3>
          <p>状态评估与故障诊断算法</p>
        </div>
        <span class="section-total">共 {{ algorithmTotal }} 个算法</span>
      </div>

      <el-table
        :data="algorithms"
        class="algorithm-table"
        :header-cell-style="tableHeaderStyle"
        empty-text="暂无算法数据"
      >
        <el-table-column label="算法类型" min-width="220">
          <template #default="scope">
            <div class="algorithm-name">
              <el-icon><component :is="scope.row.icon" /></el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="算法总数" min-width="110" align="center">
          <template #default="scope"><strong class="table-number">{{ scope.row.total }}</strong></template>
        </el-table-column>
        <el-table-column prop="pendingReview" label="待审核" min-width="110" align="center" />
        <el-table-column prop="pendingTest" label="待测试" min-width="110" align="center" />
        <el-table-column prop="testFailed" label="测试失败" min-width="110" align="center" />
        <el-table-column prop="pendingDeploy" label="待部署" min-width="110" align="center" />
        <el-table-column prop="deployed" label="已部署" min-width="110" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.deployed" type="success" effect="light">{{ scope.row.deployed }}</el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="content-section execution-section">
      <div class="section-header">
        <div>
          <h3>执行中心</h3>
          <p>Flink 集群与作业运行状态</p>
        </div>
        <el-tag :type="clusterTagType" effect="light">{{ clusterStateText }}</el-tag>
      </div>

      <div class="execution-grid">
        <div v-for="item in executionItems" :key="item.label" class="execution-item">
          <span>{{ item.label }}</span>
          <div>
            <strong :class="`value--${item.tone}`">{{ item.value }}</strong>
            <small>{{ item.unit }}</small>
          </div>
        </div>
      </div>

      <div class="slot-row">
        <div class="slot-heading">
          <span>集群插槽使用情况</span>
          <span>已使用 {{ slotUsed }} / {{ execution.totalSlots }}，可用 {{ execution.availableSlots }}</span>
        </div>
        <el-progress
          :percentage="slotUsage"
          :stroke-width="8"
          :show-text="false"
          :color="slotProgressColor"
        />
      </div>
    </section>
  </div>
</template>

<script>
import { getAlgorithmDashboardCounts, getJobsCount } from '@/api/sw/wel'
import { DataAnalysis, FirstAidKit, Refresh, TrendCharts } from '@element-plus/icons-vue'

export default {
  name: 'AdminAlgorithmOverview',
  components: {
    DataAnalysis,
    FirstAidKit,
    Refresh,
    TrendCharts
  },
  data() {
    return {
      loading: false,
      errorMessage: '',
      lastUpdated: '',
      tableHeaderStyle: {
        background: '#f5f7fa',
        color: '#606266',
        fontWeight: '600'
      },
      algorithms: [
        {
          key: 'state',
          name: '状态评估算法',
          icon: 'TrendCharts',
          total: 0,
          pendingReview: 0,
          pendingTest: 0,
          testFailed: 0,
          pendingDeploy: 0,
          deployed: 0
        },
        {
          key: 'diagnosis',
          name: '故障诊断算法',
          icon: 'FirstAidKit',
          total: 0,
          pendingReview: 0,
          pendingTest: 0,
          testFailed: 0,
          pendingDeploy: 0,
          deployed: 0
        }
      ],
      execution: {
        connected: false,
        running: 0,
        finished: 0,
        canceled: 0,
        failed: 0,
        totalSlots: 0,
        availableSlots: 0,
        taskManagers: 0
      }
    }
  },
  computed: {
    algorithmTotal() {
      return this.algorithms.reduce((total, item) => total + item.total, 0)
    },
    updateText() {
      return this.lastUpdated ? `数据更新时间 ${this.lastUpdated}` : '正在读取最新数据'
    },
    executionItems() {
      return [
        { label: '运行中作业', value: this.execution.running, unit: '个', tone: 'running' },
        { label: '异常作业', value: this.execution.failed, unit: '个', tone: 'failed' },
        { label: '已完成作业', value: this.execution.finished, unit: '个', tone: 'normal' },
        { label: '已取消作业', value: this.execution.canceled, unit: '个', tone: 'muted' },
        { label: 'TaskManager', value: this.execution.taskManagers, unit: '个', tone: 'normal' },
        { label: '总插槽', value: this.execution.totalSlots, unit: '个', tone: 'normal' }
      ]
    },
    slotUsed() {
      return Math.max(0, this.execution.totalSlots - this.execution.availableSlots)
    },
    slotUsage() {
      if (!this.execution.totalSlots) return 0
      return Math.min(100, Math.round(this.slotUsed / this.execution.totalSlots * 100))
    },
    slotProgressColor() {
      if (this.slotUsage >= 90) return '#f56c6c'
      if (this.slotUsage >= 70) return '#e6a23c'
      return '#409eff'
    },
    clusterStateText() {
      if (!this.execution.connected) return '集群未连接'
      return this.execution.failed > 0 ? '存在异常作业' : '集群运行正常'
    },
    clusterTagType() {
      if (!this.execution.connected) return 'info'
      return this.execution.failed > 0 ? 'warning' : 'success'
    }
  },
  created() {
    this.loadDashboard()
  },
  methods: {
    isSuccess(response) {
      const payload = response && response.data
      return Boolean(payload) && ['0', '200'].includes(String(payload.code)) && payload.success !== false
    },
    getResponseData(response) {
      if (!this.isSuccess(response)) {
        const message = response && response.data && (response.data.message || response.data.msg)
        throw new Error(message || '接口返回失败')
      }
      return response.data.data
    },
    async loadDashboard() {
      if (this.loading) return
      this.loading = true
      this.errorMessage = ''

      const results = await Promise.allSettled([
        this.fetchAlgorithmCounts(),
        this.fetchExecutionStatus()
      ])
      const failedCount = results.filter(item => item.status === 'rejected').length
      if (failedCount) {
        this.errorMessage = `${failedCount} 组数据加载失败，请检查服务连接后刷新`
      }

      this.lastUpdated = new Date().toLocaleString('zh-CN', { hour12: false })
      this.loading = false
    },
    async fetchAlgorithmCounts() {
      const response = await getAlgorithmDashboardCounts()
      const data = this.getResponseData(response) || {}
      this.applyAlgorithmCounts(this.algorithms[0], data.stateEvaluation)
      this.applyAlgorithmCounts(this.algorithms[1], data.faultDiagnosis)
    },
    applyAlgorithmCounts(algorithm, counts = {}) {
      algorithm.total = Number(counts.total) || 0
      algorithm.pendingReview = Number(counts.pendingReview) || 0
      algorithm.pendingTest = Number(counts.pendingTest) || 0
      algorithm.testFailed = Number(counts.testFailed) || 0
      algorithm.pendingDeploy = Number(counts.pendingDeploy) || 0
      algorithm.deployed = Number(counts.deployed) || 0
    },
    async fetchExecutionStatus() {
      const response = await getJobsCount()
      const data = this.getResponseData(response)
      if (!data) throw new Error('未获取到执行中心数据')

      this.execution = {
        connected: true,
        running: Number(data.running) || 0,
        finished: Number(data.finished) || 0,
        canceled: Number(data.canceled) || 0,
        failed: Number(data.failed) || 0,
        totalSlots: Number(data.totalSlots) || 0,
        availableSlots: Number(data.availableSlots) || 0,
        taskManagers: Number(data.taskManagers) || 0
      }
    }
  }
}
</script>

<style scoped lang="scss">
.overview-page {
  min-height: calc(100vh - 130px);
  color: #303133;
  background: #fff;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 82px;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  box-sizing: border-box;
}

.title-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: #fff;
  background: #337ecc;
  font-size: 21px;
}

.title-block h2 {
  margin: 0;
  font-size: 22px;
  line-height: 1.35;
  font-weight: 600;
  letter-spacing: 0;
}

.title-block p,
.section-header p {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
  letter-spacing: 0;
}

.page-header :deep(.el-button .el-icon) {
  margin-right: 6px;
}

.load-alert {
  width: auto;
  margin: 16px 24px 0;
}

.content-section {
  padding: 20px 24px 24px;
}

.algorithm-section {
  border-bottom: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  line-height: 1.4;
  font-weight: 600;
  letter-spacing: 0;
}

.section-total {
  color: #606266;
  font-size: 13px;
}

.algorithm-table {
  width: 100%;
}

.algorithm-name {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #303133;
  font-weight: 500;
}

.algorithm-name .el-icon {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  color: #337ecc;
  background: #ecf5ff;
  font-size: 17px;
}

.table-number {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.execution-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  border: 1px solid #e4e7ed;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.execution-item {
  min-width: 0;
  padding: 18px 20px;
  background: #fff;
  box-sizing: border-box;
}

.execution-item:not(:last-child) {
  border-right: 1px solid #e4e7ed;
}

.execution-item > span {
  display: block;
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}

.execution-item strong {
  color: #303133;
  font-size: 24px;
  line-height: 1;
  font-weight: 600;
}

.execution-item small {
  margin-left: 5px;
  color: #909399;
  font-size: 12px;
}

.execution-item .value--running {
  color: #67c23a;
}

.execution-item .value--failed {
  color: #f56c6c;
}

.execution-item .value--muted {
  color: #909399;
}

.slot-row {
  padding: 16px 20px 18px;
  border: 1px solid #e4e7ed;
  border-top: 0;
  border-radius: 0 0 4px 4px;
  background: #fafafa;
}

.slot-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
  color: #606266;
  font-size: 13px;
}

@media (max-width: 1200px) {
  .execution-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .execution-item:nth-child(3) {
    border-right: 0;
  }

  .execution-item:nth-child(-n + 3) {
    border-bottom: 1px solid #e4e7ed;
  }
}

@media (max-width: 700px) {
  .page-header,
  .content-section {
    padding-left: 14px;
    padding-right: 14px;
  }

  .page-header {
    align-items: flex-start;
    gap: 12px;
  }

  .load-alert {
    margin-left: 14px;
    margin-right: 14px;
  }

  .execution-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .execution-item:nth-child(3) {
    border-right: 1px solid #e4e7ed;
  }

  .execution-item:nth-child(2n) {
    border-right: 0;
  }

  .execution-item:nth-child(-n + 4) {
    border-bottom: 1px solid #e4e7ed;
  }

  .slot-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
