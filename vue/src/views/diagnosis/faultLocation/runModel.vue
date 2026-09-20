<template>
  <div v-loading="loading"
       element-loading-text="故障诊断中..."
       element-loading-background="rgba(0, 0, 0, 0.3)">
    <basic-container>
      <el-row style="border: 1px solid rgba(100, 100, 100, 0.3);">
        <el-col :span="12">
          <div class="tablecon">
            <div class="subtable">
              <div class="title">故障诊断模型</div>
              <avue-crud ref="crudDiagnosisAlarmRef" class="h-avue-crud"
                         :option="diagnosisOption"
                         @on-load="getDiagnosisList"
                         @size-change="sizeChange"
                         @current-change="currentChange"
                         v-model:page="diagnosisPage"
                         :data="diagnosisData"
                         @row-click="diagnosisRowClick">
                <template #diagnosisRadio="scope">
                  <el-radio v-model="diagnosisRowIndex" :label="scope.index"><span></span></el-radio>
                </template>
                <template #operate="scope">
                  <el-button class="editWorktBtn" @click="runModel(scope.row)">运行模型</el-button>
                </template>
              </avue-crud>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="tablecon">
            <div class="subtable">
              <div class="title">故障诊断结果</div>
              <result ref="refResult" :chartData="pieData" :tableData="tableData" :resultParams="resultParams" :visibleChart="showChart" :visibleTable="showTable"></result>
            </div>
          </div>
        </el-col>
      </el-row>
    </basic-container>
  </div>

</template>

<script>
import {getDiagnosisList, getDiagnosisRecord, runModel} from "@/api/diagnosis/faultLocation";
import {failureModelOption} from "@/const/crud/diagnosis/faultLocation/table";
import result from './result.vue'
import {getObj} from "@/api/sw/model3d/configFailureMode/table";

export default {
  name: 'runDiagModel',

  data() {
    return {
      object: this.$route.query.object,       //  resumeTable页面传递的数据对象
      crudDiagnosisAlarmRef: null,
      diagnosisForm: [],
      diagnosisOption: failureModelOption,
      diagnosisPage: {
        total: 0,          // 总页数
        currentPage: 1,     // 当前页数
        pageSize: 10        // 每页显示多少条
      },
      diagnosisData: [],
      diagnosisRowIndex: null,
      resultParams: {},
      loading: false,
      showChart: false,
      showTable:false,
      resultData: {},
      pieData: [],
      tableData: {}
    }
  },

  components: {
    result,
  },

  async mounted() {
    try {
      this.object = JSON.parse(this.object)
      this.resultParams = {
        monitor: this.object.monitor,
        farmName: this.object.farmName
      }
      try {
        await this.getDiagnosisList()
      } catch (error) {
        console.warn('读取故障诊断模型列表失败', error)
      }
      if (this.object.diagnosisRecordId) {
        await this.loadDiagnosisRecord(this.object.diagnosisRecordId)
      }
    } catch (error) {
      console.error('初始化故障诊断页面失败', error)
      this.$message.error(error?.message || '故障诊断页面参数无效')
    }
  },
  methods: {
    //  修改每页条数
    sizeChange(pageSize){
      this.diagnosisPage.pageSize = pageSize
      this.updatePageData()
    },

    //  翻页
    currentChange(current){
      this.diagnosisPage.currentPage = current
      this.updatePageData()
    },

    //  分页或者修改每页条数时，自动调用
    async getDiagnosisList() {
      if(Object.keys(this.resultParams).length !== 0){
        const res = await getDiagnosisList({taskId: this.resultParams.monitor.taskId})
        if (res.data.code === 0){
          this.diagnosisForm = res.data.data
          this.updatePageData()
        }
      }
    },

    updatePageData() {
      // 根据当前页码和页面大小计算显示哪些数据
      const start = (this.diagnosisPage.currentPage - 1) * this.diagnosisPage.pageSize
      const end = start + this.diagnosisPage.pageSize
      this.diagnosisData = this.diagnosisForm.slice(start, end)
      this.diagnosisPage.total = this.diagnosisForm.length
    },

    diagnosisRowClick(row) {
      this.diagnosisRowIndex = row.$index
      this.resultParams.diagnosisModel = row.modelName
    },

    parseTaskResult(taskResult) {
      if (taskResult === null || taskResult === undefined) return null
      if (typeof taskResult === 'object') return taskResult
      const taskResultText = String(taskResult).trim()
      if (!taskResultText || taskResultText === '""') return null
      try {
        return JSON.parse(taskResultText)
      } catch (error) {
        console.warn('taskResult 解析失败', taskResult, error)
        return null
      }
    },

    buildDisplayDataFromResult(result) {
      if (Array.isArray(result.probabilities)) {
        return this.buildFfcNetDisplayData(result)
      }

      const tableData = []
      const pieData = []

      Object.keys(result).forEach(key => {
        const item = result[key]
        if (!item || typeof item !== 'object') return

        const faultType = item.type || key
        const possibility = Number(item.possibility || 0) * 100
        const suggest = item.suggest || ''

        tableData.push({
          faultType,
          possibility,
          suggest
        })

        pieData.push({
          name: faultType,
          value: possibility
        })
      })

      tableData.sort((a, b) => b.possibility - a.possibility)
      pieData.sort((a, b) => b.value - a.value)

      return {
        tableData,
        pieData
      }
    },

    buildFfcNetDisplayData(result) {
      const selectedCode = Number(result.fault_code)
      const isFault = result.is_fault === true
      const rows = result.probabilities
        .map(item => {
          const code = Number(item?.code)
          const probability = Number(item?.probability)
          if (!Number.isFinite(code) || !Number.isFinite(probability)) return null

          let suggest = '-'
          if (code === selectedCode) {
            suggest = isFault
              ? `重点检查${item.name || result.fault_name || '对应部件'}并结合现场复核`
              : '当前窗口未识别到故障，继续保持监测'
          }
          return {
            code,
            faultType: item.name || `类别${code}`,
            possibility: Number((probability * 100).toFixed(4)),
            suggest
          }
        })
        .filter(Boolean)
        .sort((a, b) => b.possibility - a.possibility)

      return {
        tableData: rows,
        pieData: rows.map(item => ({ name: item.faultType, value: item.possibility }))
      }
    },

    applyCmsSignal(taskMessage, fallbackTime = '') {
      if (Array.isArray(taskMessage?.values) && taskMessage.values.length === 1024) {
        this.resultParams.cmsSignal = {
          values: taskMessage.values.map(Number),
          sampleCount: Number(taskMessage.sampleCount || taskMessage.values.length),
          monitorPointId: taskMessage.monitorPointId || '',
          requestedTime: taskMessage.requestedTime || fallbackTime,
          matchedTime: taskMessage.matchedTime || taskMessage.endTime || '',
          timeOffsetSeconds: Number(taskMessage.timeOffsetSeconds || 0)
        }
        return
      }
      this.resultParams.cmsSignal = null
    },

    showDiagnosisResult(result) {
      this.showChart = false
      this.showTable = false
      this.pieData = []
      this.tableData = []

      if (!result || typeof result !== 'object') {
        throw new Error('诊断记录没有可用的结果数据')
      }
      if ('message' in result) {
        this.tableData = [{
          faultType: '正常',
          possibility: 100.00,
          suggest: result.message || '暂无故障发生'
        }]
        this.showTable = true
        return
      }

      const displayData = this.buildDisplayDataFromResult(result)
      this.tableData = displayData.tableData
      this.pieData = displayData.pieData
      this.showChart = this.pieData.length > 0
      this.showTable = this.tableData.length > 0
    },

    async loadDiagnosisRecord(recordId) {
      this.loading = true
      try {
        const response = await getDiagnosisRecord(recordId)
        const payload = response?.data?.data || {}
        const record = payload.record || {}
        if (Number(record.diagnosisStatus) !== 2) {
          throw new Error('该次诊断尚未完成，暂时无法查看结果')
        }

        const taskMessage = this.parseTaskResult(payload.taskMsg)
        const diagnosisResult = this.parseTaskResult(record.resultJson || payload.taskResult)
        this.resultParams.diagnosisModel = record.algorithmShortName || '故障诊断模型'
        this.resultParams.diagnosisRecord = record
        this.applyCmsSignal(taskMessage, record.alarmTime || '')
        this.showDiagnosisResult(diagnosisResult)

        const modelIndex = this.diagnosisForm.findIndex(item => (
          String(item?.algoId || '') === String(record.algorithmId || '')
        ))
        if (modelIndex >= 0) this.diagnosisRowIndex = modelIndex
      } finally {
        this.loading = false
      }
    },

    parseAlarmTime(value) {
      const text = String(value || '').trim()
      const normalized = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(?:\.\d+)?$/.test(text)
        ? text.replace(' ', 'T')
        : text
      const alarmTime = new Date(normalized)
      if (Number.isNaN(alarmTime.getTime())) {
        throw new Error('报警时间格式无效，无法定位诊断数据')
      }
      return alarmTime
    },

    async runModel(row) {
      this.loading = true
      this.showChart = false
      this.showTable = false
      this.pieData = []
      this.tableData = []

      try {
        const endTime = this.parseAlarmTime(this.resultParams.monitor.dcTime)
        const startTime = new Date(endTime.getTime() - 2 * 60 * 1000)
        const alarmTime = endTime.toISOString()
        let diagnoseInfo = {
          farmName: this.resultParams.farmName,
          turbineName: this.resultParams.monitor.turbineName,
          turbineCode: this.resultParams.monitor.turbineCode,
          part: this.resultParams.monitor.nodeName,
          location: this.resultParams.monitor.location,
          algoId: row.algoId,
          taskId: String(this.resultParams.monitor.taskId || ''),
          alarmTime,
          monitorPointId: this.resultParams.monitor.monitorPointId || '',
          nodeId: String(this.resultParams.monitor.nodeId || ''),
          nodeCode: this.resultParams.monitor.nodeCode || '',
          startTime: startTime.toISOString(),
          endTime: alarmTime
        }

        // 这里保留故障模式接口调用，避免影响原有链路上的其他副作用；
        // 但当前展示逻辑不再依赖“配置故障类型”和算法返回 type 严格一一匹配。
        await getObj(row.nodeId)

        const response = await runModel(diagnoseInfo)
        const taskInfo = response?.data?.data || {}
        const taskState = taskInfo?.taskState
        const taskResultText = taskInfo?.taskResult
        const result = this.parseTaskResult(taskResultText)
        const taskMessage = this.parseTaskResult(taskInfo?.taskMsg)

        this.applyCmsSignal(taskMessage, alarmTime)

        if (taskState === 3) {
          const errorMessage = typeof result === 'object' && result?.message
            ? result.message
            : (taskResultText || '故障诊断执行失败！')
          this.$message.error(errorMessage)
          return
        }

        if (!result || typeof result !== 'object') {
          this.$message.error(taskResultText || '故障诊断未返回有效结果！')
          return
        }
        this.showDiagnosisResult(result)
      } catch (error) {
        console.error('故障诊断执行异常', error)
        this.$message.error(
          error?.response?.data?.msg
          || error?.response?.data?.message
          || error?.message
          || '故障诊断失败！'
        )
      } finally {
        this.loading = false
      }
    },

  }
}
</script>

<style lang="scss" scoped>

.main {
  display: flex;
  flex-direction: row;
}

.left {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-content: center;
}

.right {
  height: 100vh;
  margin-left: 5px;
}

.tablecon {
  height: 100%;
  width: 100%;
  display: flex;

  .subtable {
    height: 100%;
    width: 100%;

    .title {
      height: 40px;
      width: 100%;
      background-color: cadetblue;
      display: flex;
      color: white;
      letter-spacing: 2px; /* 设置字间距为 2 像素 */
      justify-content: center; /* 水平居中 */
      align-items: center; /* 垂直居中 */
    }
  }
}

.el-table__body {
  position: absolute;
  transition: all 500ms linear;
}

</style>
