<!--  历史评估结果图 -->
<template>
  <el-drawer
      v-model="showTable"
      title="故障记录"
      direction="rtl"
      size="30%"
  >
    <avue-crud :data="dataForm" :option="dataFormOption">
    </avue-crud>
  </el-drawer>

  <div class="stateChart">
    <div id="chart" ref="chartZone">
    </div>
  </div>
</template>

<script>

import * as echarts from 'echarts'
import {multLineOption} from '@/const/crud/diagnosis/stateAssessment/myChart.js'
import {getFaultByTime, reqHistoryDataSet} from "@/api/diagnosis/stateAssessment/stateAssessment";
import {faultOption} from "@/const/crud/diagnosis/stateAssessment/table";

export default {
  name: 'myChart',
  data() {
    return {
      isShow: false,
      chartData: {
        xCoordBuffer: [],
        yCoordBuffer: []
      },
      chartInstance: null,
      showTable: false,
      dataForm: [],
      dataFormOption: faultOption
    }
  },

  mounted() {
    this.$nextTick(() => {
      this.initEmptyChart('服役状态评估指标', ['评估指标', '报警'], 'KLD')
    })
  },

  methods: {

    initEmptyChart(title, legendData, yAxisName) {
      this.chartInstance = echarts.init(this.$refs.chartZone);
      const emptyOption = multLineOption(title, legendData, yAxisName)

      emptyOption.series = legendData.map((label, index) => {
        return {
          name: label,
          type: 'line',
          data: []
        }
      })
      emptyOption.xAxis.data = []
      this.chartInstance.setOption(emptyOption)
    },

    //  初始化多条折线图(Stacked Line Chart)
    initCharts(title, legendData, yAxisName, highlightRanges) {
      this.chartInstance = echarts.init(document.getElementById('chart'))
      const option = multLineOption(title, legendData, yAxisName)

      option.series = legendData.map((label, index) => {
        return {
          name: label,
          type: 'line',
          data: this.chartData.yCoordBuffer[index].buffer,
          markArea: {
            data: this.generateHighlightAreas(this.chartData.xCoordBuffer, highlightRanges)
          }
        }
      })

      // 扩展 xAxis.data
      option.xAxis.data = this.chartData.xCoordBuffer

      this.chartInstance.setOption(option)

    },

    generateHighlightAreas(xCoordBuffer, highlightRanges) {
      const highlightAreas = [];

      highlightRanges.forEach(([start, end]) => {
        // 查找最接近的横坐标时间点
        const startIndex = this.findClosestIndex(xCoordBuffer, start)
        const endIndex = this.findClosestIndex(xCoordBuffer, end)

        // 获取对应的时间点
        const startTime = xCoordBuffer[startIndex]
        const endTime = xCoordBuffer[endIndex]

        // 添加高亮区域
        highlightAreas.push([
          { xAxis: startTime },
          { xAxis: endTime }
        ])
      })

      return highlightAreas;
    },

    findClosestIndex(arr, target) {
      let closestIndex = 0
      let minDiff = Infinity

      // 确保目标和数组中的元素都是 Date 对象
      const targetDate = new Date(target)

      for (let i = 0; i < arr.length; i++) {
        const diff = Math.abs(targetDate - new Date(arr[i]))
        if (diff < minDiff) {
          minDiff = diff
          closestIndex = i
        }
      }

      return closestIndex
    },

    //  加载节点图表
    loadChart(nodeId, taskId, alName, startTime, endTime) {
      this.loadData(nodeId, taskId, alName, startTime, endTime)
    },

    loadData(nodeId, taskId, alName, startTime, endTime) {
      reqHistoryDataSet({
        taskId: taskId,
        algoShortname: alName,
        startDcTime: startTime,
        endDcTime: endTime
      }).then(res => {
        if (res.data.code === 0) {
          const dcData = res.data.data[0].dc_dataList
          const anomalyFlag = res.data.data[0].anomaly_flagList.map(flag => flag === 1 ? 15 : flag)
          const dcTime = res.data.data[0].dcTimeList

          this.chartData.xCoordBuffer = []
          this.chartData.yCoordBuffer = [
            {name: '评估指标', buffer: dcData},
            {name: '报警', buffer: anomalyFlag}
          ]

          dcTime.forEach((time, index) => {
            this.chartData.xCoordBuffer.push(time)
          })

          getFaultByTime({nodeId: nodeId, startTime: startTime, endTime: endTime}).then(res => {
            if (res.data.code === 0) {
              const heightArea = this.extractHighlightRanges(res.data.data)
              this.initCharts('服役状态评估指标', ['评估指标', '报警'], 'KLD', heightArea)
            }
          })

        }
      })
    },

    extractHighlightRanges(data) {
      // 如果 data 不是数组，则将其包装成数组
      if (!Array.isArray(data)) {
        data = [data];
      }

      return data.map(item => {
        // 提取 startTime 和 endTime
        const startTime = item.startTime.replace(' ', 'T')
        const endTime = item.endTime.replace(' ', 'T')

        // 返回格式化的数组
        return [startTime, endTime]
      })
    },

    loadTable(nodeId, startTime, endTime) {
      getFaultByTime({nodeId: nodeId, startTime: startTime, endTime: endTime}).then(res => {
        if (res.data.code === 0) {
          if(res.data.data.length !== 0){
            this.dataForm = res.data.data
          }else {
            this.dataForm = []
          }
        }
      })
      this.showTable = true
    },

    clearData() {
      this.chartData = {
        xCoordBuffer: [],
        yCoordBuffer: []
      }
      this.dataForm = []
      this.initEmptyChart('服役状态评估指标', ['评估指标', '报警'], 'KLD')
    }

  },


}
</script>
<style lang="scss" scoped>
.stateChart {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;

  .tagzone {
    display: flex; //  按行排列
    width: 100%;
    height: 41px;
    background: #fcfcfc;
    border-bottom: 1px solid #eeeded;

    span {
      display: block;
      padding: 0;
      width: 120px;
      height: 41px;
      font-size: 16px;
      text-align: center; /*文字水平居中*/
      line-height: 41px; /*文字垂直居中*/
    }

    ::v-deep .el-tabs__header {
      border: 0px;
    }
  }

  #chart {
    width: 100%;
    height: 100%;
  }
}

.stateTable {

}
</style>
