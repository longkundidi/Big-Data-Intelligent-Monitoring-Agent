<template>
  <div v-if="visibleChart" class="chartZone">
    <div class="chart-toolbar">
      <div class="chart-title">诊断概率分布</div>
      <el-button @click="viewPDF" class="viewBtn">查看诊断报告</el-button>
    </div>
    <div ref="chart" class="diagnosis-chart"></div>
    <pdf v-if="showDialog" ref="pdf" :chartData="chartData" class="pdf-corner"></pdf>
  </div>
  <div v-if="visibleTable" class="tableZone">
    <avue-crud :page="resultPage"
               :data="resultForm"
               @size-change="sizeChange"
               @current-change="currentChange"
               :option="resultFormOption">
    </avue-crud>
  </div>

</template>
<script>
import * as echarts from 'echarts'
import {resultOption} from "@/const/crud/diagnosis/faultLocation/table"
import pdf from "@/views/diagnosis/faultLocation/toPDF.vue"

export default {
  name: 'resultChart',
  components:{
    pdf
  },
  props: {
    chartData: {
      type: Array,
      default: () => []
    },
    tableData: {
      type: Array,
      default: () => []
    },
    resultParams: {
      type: Object,
      default: () => ({})
    },
    visibleChart: Boolean,
    visibleTable: Boolean
  },
  data(){
    return{
      showDialog:false,
      chartInstance: null,
      resultForm: [],
      resultFormOption: resultOption,
      resultPage: {
        total: 0,          // 总页数
        currentPage: 1,     // 当前页数
        pageSize: 10        // 每页显示多少条
      },
      faultType:''
    }
  },

  mounted() {
    if (this.visibleChart) {
      //  初始化饼图
      this.initPieChart()
    }
  },

  methods: {
    probabilityChartOption() {
      const colors = ['#57c7ff', '#76d39b', '#ffc857', '#ff7b72', '#9b8afb', '#5dd6c0', '#ff9f68', '#d47ee8']
      return {
        animationDuration: 500,
        color: colors,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: params => {
            const item = params[0]
            return `${item.name}<br/>诊断概率：${Number(item.value).toFixed(4)}%`
          }
        },
        grid: {
          left: 190,
          right: 72,
          top: 12,
          bottom: 32
        },
        xAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            color: '#a9c9d8',
            formatter: '{value}%'
          },
          splitLine: {
            lineStyle: { color: 'rgba(132, 196, 218, 0.15)' }
          }
        },
        yAxis: {
          type: 'category',
          inverse: true,
          data: this.chartData.map(item => item.name),
          axisTick: { show: false },
          axisLine: { show: false },
          axisLabel: {
            color: '#e7f5fb',
            fontSize: 13,
            width: 172,
            overflow: 'truncate'
          }
        },
        series: [{
          type: 'bar',
          data: this.chartData.map(item => Number(item.value)),
          barMaxWidth: 20,
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(113, 178, 204, 0.1)',
            borderRadius: 3
          },
          itemStyle: {
            borderRadius: [0, 3, 3, 0],
            color: params => colors[params.dataIndex % colors.length]
          },
          label: {
            show: true,
            position: 'right',
            color: '#e7f5fb',
            fontSize: 12,
            formatter: params => `${Number(params.value).toFixed(2)}%`
          }
        }]
      }
    },

    initPieChart(){
      if (!this.$refs.chart) return
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }
      const chartInstance = echarts.init(this.$refs.chart)
      chartInstance.setOption(this.probabilityChartOption())
      this.chartInstance = chartInstance
    },

    updatePie(){
      if (this.chartInstance) {
        this.chartInstance.setOption(this.probabilityChartOption(), true)
      }
    },

    updateTable(){
      // 根据当前页码和页面大小计算显示哪些数据
      this.$nextTick(()=>{
        if(this.visibleTable === true){
          const start = (this.resultPage.currentPage - 1) * this.resultPage.pageSize
          const end = start + this.resultPage.pageSize
          this.resultForm = this.tableData.slice(start, end)
          if(this.resultForm.length > 0){
            this.faultType=this.resultForm[0].faultType
          } else {
            this.faultType = ''
          }

          this.resultPage.total = this.tableData.length
        }else {
          this.resultForm = []
        }
      })
    },

    sizeChange(pageSize){
      this.resultPage.currentPage = 1
      this.resultPage.pageSize = pageSize
      this.updateTable()
    },
    currentChange(current){
      this.resultPage.currentPage = current
      this.updateTable()
    },

    viewPDF(){
      this.showDialog = true
      this.$nextTick(() => {
        const firstResult = this.resultForm[0] || {}
        this.$refs.pdf.init({
          ...this.resultParams,
          faultType: this.faultType,
          faultProbability: Number(firstResult.possibility || 0),
          suggest: firstResult.suggest || ''
        })
      })
    },

  },

  watch: {
    visibleChart(newVal) {
      if (newVal && !this.chartInstance) {
        this.$nextTick(() => {
          this.initPieChart()
        })
      }
    },
    chartData: {
      handler(newData) {
        this.updatePie()
      },
      deep: true,
    },
    tableData: {
      handler(newData) {
        this.updateTable()
      },
      deep: true,
    },
  }
}
</script>
<style lang="scss" scoped>
.chartZone {
  width: 100%;
  padding: 18px 20px 8px;
  box-sizing: border-box;
  position: relative;
}

.chart-toolbar {
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.chart-title {
  color: #e7f5fb;
  font-size: 18px;
  font-weight: 600;
}

.diagnosis-chart {
  width: 100%;
  height: 360px;
  min-width: 0;
}

.tableZone {
  width: 100%;
  padding: 0 16px 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.tableZone .avue-crud) {
  width: 100%;
}

:deep(.tableZone .el-table .cell) {
  padding: 0 12px;
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
}

:deep(.tableZone .el-table th.el-table__cell) {
  padding: 12px 0;
}

:deep(.tableZone .el-table td.el-table__cell) {
  padding: 11px 0;
}

@media (max-width: 1100px) {
  .diagnosis-chart {
    height: 400px;
  }
}
</style>
