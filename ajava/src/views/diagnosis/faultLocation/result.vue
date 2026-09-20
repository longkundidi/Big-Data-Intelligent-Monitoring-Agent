<template>
  <div v-if="visibleChart" class="chartZone">
      <div ref="chart" :style="{width: '80%', height: '350px'}" ></div>
      <div>
        <el-button @click="viewPDF" class="viewBtn export-button">查看诊断报告</el-button>
        <pdf v-if="showDialog" ref="pdf" :chartData="chartData"  class="pdf-corner"></pdf>
      </div>
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
    chartData: Array,
    tableData: Object,
    resultParams: Object,
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
    //  初始化饼图
    initPieChart(){
      const chartInstance = echarts.init(this.$refs.chart)
      chartInstance.setOption({
        title: {
          text: '诊断结果图表',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            type: 'pie',
            radius: '50%',
            data: this.chartData,
            label: {
              show: true,
              position: 'outside',
              formatter: '{b}：{d}%', // 自定义标签格式，{b}是分类名，{c}是值，{d}是百分比
            },
            labelLine: {
              show: false, // 不显示连接线
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
      this.chartInstance = chartInstance
    },

    updatePie(){
      if (this.chartInstance) {
        this.chartInstance.setOption({
          series: [
            {
              data: this.chartData,
            }
          ]
        })
      }
    },

    updateTable(){
      // 根据当前页码和页面大小计算显示哪些数据
      this.$nextTick(()=>{
        if(this.visibleTable === true){
          const start = (this.resultPage.currentPage - 1) * this.resultPage.pageSize
          const end = start + this.resultPage.pageSize
          this.resultForm = this.tableData.slice(start, end)
          if(this.resultForm !=='undefined'){
            this.faultType=this.resultForm[0].faultType
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
        this.resultParams.faultType = this.faultType
        this.$refs.pdf.init(this.resultParams)
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
  margin-top: 50px;
  margin-left: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.tableZone {
  display: flex;
  justify-content: center;
  align-items: center;
}

.export-button {
  position: absolute; /* Position the button absolutely */
  top: 2px; /* Top position */
  right: 50px; /* Right position */
}
</style>
