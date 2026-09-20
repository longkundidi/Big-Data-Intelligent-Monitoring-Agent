<template>
  <div class="audit-page" v-loading="loading">
    <header class="audit-header">
      <div class="title-block">
        <el-icon class="title-icon"><CircleCheck /></el-icon>
        <div>
          <h2>模型-算法审核管理</h2>
          <p>{{ activeStageConfig.label }} · 共 {{ total }} 条</p>
        </div>
      </div>
    </header>

    <section class="workflow-bar">
      <el-tabs v-model="activeStage" class="workflow-tabs" @tab-change="handleStageChange">
        <el-tab-pane
          v-for="stage in stages"
          :key="stage.value"
          :name="stage.value"
          :label="stage.label"
        />
      </el-tabs>

      <el-form class="filter-form" :model="filters" @submit.prevent="handleSearch">
        <el-form-item label="名称">
          <el-input
            v-model.trim="filters.alModelName"
            clearable
            placeholder="请输入算法/模型名称"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.alModelType" clearable placeholder="请选择算法/模型类型">
            <el-option v-for="type in modelTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-section">
      <el-table
        :data="tableData"
        row-key="recordKey"
        class="audit-table"
        :header-cell-style="tableHeaderStyle"
        empty-text="当前阶段暂无数据"
      >
        <el-table-column prop="alModelName" label="算法/模型名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="modelFunction" label="所属功能" min-width="130" show-overflow-tooltip />
        <el-table-column prop="alModelType" label="算法/模型类型" min-width="150" show-overflow-tooltip />
        <el-table-column prop="creator" label="创建者" min-width="100" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.creator || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170">
          <template #default="scope">{{ scope.row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="当前状态" width="110" align="center">
          <template #default>
            <el-tag :type="activeStageConfig.tagType" effect="light">
              {{ activeStageConfig.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <el-button size="small" @click="openDetails(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>

            <el-button
              v-if="activeStage === 'pending_review'"
              size="small"
              type="primary"
              @click="approve(scope.row)"
            >
              <el-icon><CircleCheck /></el-icon>
              审核通过
            </el-button>

            <el-button
              v-if="activeStage === 'pending_test' || activeStage === 'test_failed'"
              size="small"
              type="primary"
              :loading="testingAlgorithmId === scope.row.algorithmId"
              @click="runTest(scope.row)"
            >
              <el-icon v-if="testingAlgorithmId !== scope.row.algorithmId"><VideoPlay /></el-icon>
              {{ activeStage === 'test_failed' ? '重新测试' : '开始测试' }}
            </el-button>

            <el-button
              v-if="activeStage === 'pending_deploy'"
              size="small"
              type="success"
              @click="openDeploy(scope.row)"
            >
              <el-icon><Promotion /></el-icon>
              部署
            </el-button>

            <template v-if="activeStage === 'deployed'">
              <el-button size="small" type="warning" @click="undeploy(scope.row)">
                <el-icon><SwitchButton /></el-icon>
                下线
              </el-button>
              <el-button size="small" @click="resetTest(scope.row)">
                <el-icon><RefreshLeft /></el-icon>
                重新测试
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="算法/模型详情" width="720px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="名称">{{ detailValue('modelName', 'alName', 'alModelName') }}</el-descriptions-item>
        <el-descriptions-item label="数据来源">{{ sourceLabel(selectedRow.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="所属功能">{{ detailValue('modelFunction', 'alType') }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailValue('modelType', 'alType', 'alModelType') }}</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ detailValue('modelProvider', 'creator') }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailValue('createTime') }}</el-descriptions-item>
        <el-descriptions-item label="针对对象">{{ detailValue('modelObject', 'alSuit') }}</el-descriptions-item>
        <el-descriptions-item label="调用方式">{{ detailValue('modelInvoke', 'invocation') }}</el-descriptions-item>
        <el-descriptions-item label="适用场景" :span="2">{{ detailValue('modelCondition', 'alBrief') }}</el-descriptions-item>
        <el-descriptions-item label="程序包" :span="2">{{ detailValue('programUrl') }}</el-descriptions-item>
        <el-descriptions-item label="调用地址" :span="2">{{ detailValue('modelUrl', 'alUrl', 'alModelUrl') }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="activeStage === 'pending_review'"
          type="primary"
          @click="approve(selectedRow, true)"
        >
          审核通过
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deployVisible" title="部署算法/模型" width="520px" destroy-on-close>
      <el-form label-width="110px" @submit.prevent="confirmDeploy">
        <el-form-item label="算法调用地址" required>
          <el-input v-model.trim="deployForm.alModelUrl" placeholder="请输入可访问的算法服务地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deployVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmDeploy">确认部署</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testVisible" title="自动测试" width="620px" :close-on-click-modal="false">
      <div class="test-result" v-loading="testRunning" element-loading-text="测试中...">
        <el-result
          v-if="!testRunning"
          :icon="testSucceeded ? 'success' : 'error'"
          :title="testSucceeded ? '测试通过' : '测试未通过'"
        >
          <template #sub-title>
            <pre>{{ testResultText }}</pre>
          </template>
        </el-result>
      </div>
      <template #footer>
        <el-button :disabled="testRunning" type="primary" @click="testVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  CircleCheck,
  Promotion,
  RefreshLeft,
  RefreshRight,
  Search,
  SwitchButton,
  VideoPlay,
  View
} from '@element-plus/icons-vue'
import {
  getAuditDetail,
  getAuditPage,
  transitionAuditStatus
} from '@/api/al/auditsManagement/auditsManagement'
import {autoTest, getObj, startJob} from '@/api/al/altest/altest'

const STAGES = [
  {value: 'pending_review', label: '待审核', tagType: 'warning'},
  {value: 'pending_test', label: '待测试', tagType: ''},
  {value: 'test_failed', label: '测试失败', tagType: 'danger'},
  {value: 'pending_deploy', label: '待部署', tagType: 'warning'},
  {value: 'deployed', label: '已部署', tagType: 'success'}
]

export default {
  name: 'AuditsManagement',
  components: {
    CircleCheck,
    Promotion,
    RefreshLeft,
    RefreshRight,
    Search,
    SwitchButton,
    VideoPlay,
    View
  },
  data() {
    return {
      stages: STAGES,
      activeStage: 'deployed',
      filters: {
        alModelName: '',
        alModelType: ''
      },
      modelTypes: [
        '统计学模型', '信号处理模型', '传统机器学习模型', '深度学习模型'
      ],
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      submitting: false,
      detailVisible: false,
      selectedRow: {},
      detailData: {},
      deployVisible: false,
      deployForm: {
        alModelUrl: ''
      },
      testVisible: false,
      testRunning: false,
      testSucceeded: false,
      testResultText: '',
      testingAlgorithmId: null,
      tableHeaderStyle: {
        background: '#337ecc',
        color: '#fff',
        fontWeight: '600',
        textAlign: 'center'
      }
    }
  },
  computed: {
    activeStageConfig() {
      return this.stages.find(stage => stage.value === this.activeStage) || this.stages[0]
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const response = await getAuditPage({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          stage: this.activeStage,
          ...this.filters
        })
        if (response.data.code !== '200') {
          throw new Error(response.data.message || '审核数据加载失败')
        }
        const page = response.data.data || {}
        this.tableData = (page.records || []).map(record => ({
          ...record,
          recordKey: `${record.sourceType}-${record.algorithmId}`
        }))
        this.total = Number(page.total || 0)
      } catch (error) {
        ElMessage.error(error.message || '审核数据加载失败')
      } finally {
        this.loading = false
      }
    },
    handleStageChange() {
      this.pageNum = 1
      this.loadData()
    },
    handleSearch() {
      this.pageNum = 1
      this.loadData()
    },
    resetFilters() {
      this.filters = {alModelName: '', alModelType: ''}
      this.pageNum = 1
      this.loadData()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.loadData()
    },
    sourceLabel(sourceType) {
      const labels = {
        state_evaluation: '状态评估算法',
        fault_diagnosis: '故障诊断算法'
      }
      return labels[sourceType] || '-'
    },
    detailValue(...keys) {
      for (const key of keys) {
        const value = this.detailData?.[key]
        if (value !== undefined && value !== null && value !== '') return value
      }
      return '-'
    },
    async openDetails(row) {
      this.selectedRow = row
      this.detailData = {...row}
      this.detailVisible = true
      try {
        const response = await getAuditDetail({
          algorithmId: row.algorithmId,
          sourceType: row.sourceType
        })
        const detail = response.data.data
        if (detail) this.detailData = {...row, ...detail}
      } catch (error) {
        ElMessage.warning('完整详情加载失败，已显示审核列表信息')
      }
    },
    async performTransition(row, action, extra = {}, successMessage = '操作成功') {
      const response = await transitionAuditStatus({
        algorithmId: row.algorithmId,
        sourceType: row.sourceType,
        action,
        ...extra
      })
      if (response.data.code !== '200') {
        throw new Error(response.data.message || '状态更新失败')
      }
      ElMessage.success(successMessage)
      await this.loadData()
    },
    async approve(row, closeDetail = false) {
      try {
        await ElMessageBox.confirm(`确认“${row.alModelName}”已满足审核要求？`, '审核确认', {
          confirmButtonText: '审核通过',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await this.performTransition(row, 'APPROVE', {}, '审核通过，已进入待测试阶段')
        if (closeDetail) this.detailVisible = false
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '审核失败')
      }
    },
    async runTest(row) {
      if (!row.programUrl) {
        ElMessage.warning('该算法未配置程序包，无法执行自动测试')
        return
      }
      this.testVisible = true
      this.testRunning = true
      this.testSucceeded = false
      this.testResultText = ''
      this.testingAlgorithmId = row.algorithmId
      try {
        const autoResponse = await autoTest(row.programUrl)
        const taskInfo = autoResponse.data.data
        if (!Array.isArray(taskInfo) || taskInfo.length < 2) {
          throw new Error(autoResponse.data.message || '自动测试任务创建失败')
        }
        const [taskId, alModelType] = taskInfo
        await startJob(taskId, alModelType, '自动测试')
        const task = await this.waitForTask(taskId)
        this.testResultText = this.formatTaskResult(task.taskResult)
        this.testSucceeded = this.isPackageTestPassed(task)
        await this.performTransition(row, this.testSucceeded ? 'TEST_PASS' : 'TEST_FAIL', {},
          this.testSucceeded ? '测试通过，已进入待部署阶段' : '测试未通过，已记录测试结果')
      } catch (error) {
        this.testSucceeded = false
        this.testResultText = error.message || '自动测试执行失败'
        try {
          await this.performTransition(row, 'TEST_FAIL', {}, '测试未通过，已记录测试结果')
        } catch (transitionError) {
          ElMessage.error(transitionError.message || '测试状态记录失败')
        }
      } finally {
        this.testRunning = false
        this.testingAlgorithmId = null
      }
    },
    async waitForTask(taskId) {
      for (let index = 0; index < 40; index++) {
        const response = await getObj(taskId)
        const task = response.data.data
        const state = Number(task?.taskState)
        const hasResult = task?.taskResult !== undefined && task?.taskResult !== null && task.taskResult !== ''
        if (task && state !== 0 && state !== 1 && hasResult) return task
        await new Promise(resolve => setTimeout(resolve, 3000))
      }
      throw new Error('自动测试超时')
    },
    parseTaskResult(taskResult) {
      let value = taskResult
      for (let index = 0; index < 2 && typeof value === 'string'; index++) {
        try {
          value = JSON.parse(value)
        } catch (error) {
          break
        }
      }
      return value
    },
    isPackageTestPassed(task) {
      if (Number(task?.taskState) !== 2) return false
      const result = this.parseTaskResult(task.taskResult)
      return result?.success === 'Algorithm executed successfully'
    },
    formatTaskResult(taskResult) {
      const value = this.parseTaskResult(taskResult)
      return typeof value === 'object' ? JSON.stringify(value, null, 2) : String(value || '未返回测试详情')
    },
    openDeploy(row) {
      this.selectedRow = row
      this.deployForm.alModelUrl = row.alModelUrl || ''
      this.deployVisible = true
    },
    async confirmDeploy() {
      if (!this.deployForm.alModelUrl) {
        ElMessage.warning('请输入算法调用地址')
        return
      }
      this.submitting = true
      try {
        await this.performTransition(this.selectedRow, 'DEPLOY', {
          alModelUrl: this.deployForm.alModelUrl
        }, '部署成功')
        this.deployVisible = false
      } catch (error) {
        ElMessage.error(error.message || '部署失败')
      } finally {
        this.submitting = false
      }
    },
    async undeploy(row) {
      try {
        await ElMessageBox.confirm(`确认下线“${row.alModelName}”？`, '下线确认', {
          confirmButtonText: '确认下线',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await this.performTransition(row, 'UNDEPLOY', {}, '已下线，算法回到待部署阶段')
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '下线失败')
      }
    },
    async resetTest(row) {
      try {
        await ElMessageBox.confirm(`确认将“${row.alModelName}”退回待测试阶段？`, '重新测试', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await this.performTransition(row, 'RESET_TEST', {}, '已退回待测试阶段')
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '状态更新失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.audit-page {
  min-height: calc(100vh - 130px);
  background: #fff;
  color: #303133;
}

.audit-header {
  display: flex;
  align-items: center;
  min-height: 82px;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
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
  background: #337ecc;
  color: #fff;
  font-size: 22px;
}

.title-block h2 {
  margin: 0;
  font-size: 22px;
  line-height: 1.35;
  letter-spacing: 0;
}

.title-block p {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
  letter-spacing: 0;
}

.workflow-bar {
  padding: 0 24px 18px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.workflow-tabs {
  margin-bottom: 12px;
}

:deep(.workflow-tabs .el-tabs__header) {
  margin: 0;
}

:deep(.workflow-tabs .el-tabs__content) {
  display: none;
}

.filter-form {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(220px, 320px) auto;
  gap: 14px;
  align-items: end;
}

.filter-form :deep(.el-form-item) {
  margin: 0;
}

.filter-form :deep(.el-input),
.filter-form :deep(.el-select) {
  width: 100%;
}

.filter-actions {
  white-space: nowrap;
}

.table-section {
  padding: 20px 24px 24px;
}

.audit-table {
  width: 100%;
  min-height: 420px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 18px;
}

.test-result {
  min-height: 230px;
}

.test-result pre {
  max-height: 220px;
  margin: 0;
  overflow: auto;
  color: #606266;
  font-family: Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  text-align: left;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 1100px) {
  .filter-form {
    grid-template-columns: 1fr 1fr;
  }

  .filter-actions {
    grid-column: 1 / -1;
  }
}

@media (max-width: 700px) {
  .audit-header,
  .workflow-bar,
  .table-section {
    padding-left: 14px;
    padding-right: 14px;
  }

  .filter-form {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    grid-column: auto;
  }
}
</style>
