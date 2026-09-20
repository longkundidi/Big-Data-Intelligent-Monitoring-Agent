<!--  实时状态图 -->
<template>
  <div class="stateChart">
    <div class="table-title">
      <span>状态评估指标</span>
    </div>
    <div v-if="isShow1" class="chart-actions">
      <el-button type="text"
                 style="margin-top: 7px;"
                 class="normalBtn"
                 @click="stopRefresh()">停止查看
      </el-button>
      <el-button type="text"
                 style="margin-top: 7px;"
                 class="normalBtn"
                 @click="stratTimer()">继续查看
      </el-button>
      <el-button type="text"
                 style="margin-top: 7px;"
                 class="normalBtn"
                 @click="isHistory = true">历史数据
      </el-button>
    </div>
    <div id="chartzone" v-if="isShow">
    </div>
  </div>
  <el-dialog v-model="isHistory"
             class="config-history-dialog"
             title="查看历史评估结果"
             @close="clearHistory()"
             width="45%"
             draggable>
    <div class="timeSelect">
      <el-date-picker
          v-model="time"
          type="datetimerange"
          :shortcuts="shortcuts"
          range-separator="To"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
      />
      <el-button class="viewBtn"
                 style="margin-left: 20px"
                 @click="HandleHistory()">查 看
      </el-button>
      <el-button class="printBtn"
                 @click="HandleFault()">故障记录
      </el-button>
    </div>
    <div class="historyChart">
      <history-chart ref="refHistory"></history-chart>
    </div>
  </el-dialog>
</template>

<script>

import * as echarts from 'echarts'
import {multLineOption} from '@/const/crud/diagnosis/stateAssessment/myChart.js'
import {reqDataSet} from "@/api/diagnosis/stateAssessment/stateAssessment";
import historyChart from "@/views/diagnosis/stateAssessment/historyChart.vue";
import {dateFormat} from "@/util/date";
import {ElMessage} from "element-plus";

let globeParams = {}

//  堆叠折线图
let multLineChart = null
let myOption = null
let intervalId = null

let dataNum = 3             //  每秒刷新数据个数
let maxChartBuffer = 1000    //  显示缓冲区的最大长度
let recvBuffer = {
  xCoordBuffer: null,       //  多条折线图的数据接收X坐标数据缓冲区（一维浮点数的数组）
  yCoordBuffer: null,       //  多条折线图的数据接收Y坐标数据缓冲区（一维对象数组，每一个对象保存一条曲线的Y坐标数据）
  // alarmBuffer: null,        //  故障报警数据缓冲区
  // warningBuffer: null,      //  故障预警数据缓冲区
  // declineBuffer: null,      //  退化提示数据缓冲区
  minBuffer: 10 * dataNum     //  接收缓冲区数据的最小长度（小于该数值，就需要拉取数据）
}
let curTaskId = ''
let curAlName = ''



export default {
  name: 'myChart',
  data() {
    return {
      isShow: false,
      isShow1: false,
      isHistory: false,
      time: '',
      shortcuts: [
        {
          text: 'Last week',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setDate(start.getDate() - 7)
            return [start, end]
          },
        },
        {
          text: 'Last month',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setMonth(start.getMonth() - 1)
            return [start, end]
          },
        },
      ]
    }
  },

  components: {
    historyChart
  },

  methods: {

    //  加载节点图表
    loadChart(taskId, alName, node) {
      let self = this
      globeParams.curNode = node
      this.isShow = true
      if (intervalId !== null) {     //  每次开始加载图表前，先关闭定时器
        clearInterval(intervalId)
        intervalId = null
      }
      curTaskId = taskId                                //  缓存当前任务
      curAlName = alName

      this.isShow1 = true
      let _timer = setTimeout(() => {
        self.$nextTick(() => {                    //  等待页面渲染完成，启动定时器，加载图表数据
          this.initCharts('服役状态评估指标', ['评估指标', '阈值', '报警'], 'KLD')      //  初始化图表刻度
          this.initStackBuffer(['评估指标', '阈值', '报警'])
          self.stratTimer()
          clearTimeout(_timer)
        })
      }, 50)

    },

    showEmptyChart(node){
      globeParams.curNode = node
      this.isShow = true
      if (intervalId !== null) {     //  每次开始加载图表前，先关闭定时器
        clearInterval(intervalId)
        intervalId = null
      }
      this.$nextTick(() => {
        this.initCharts('服役状态评估指标', ['评估指标', '阈值', '报警'], 'KLD')      //  初始化图表刻度
        this.initStackBuffer(['评估指标', '阈值', '报警'])
      })
      this.isShow1 = true
    },


    /**
     * 初始化接收缓冲区
     * @param legendData: Y坐标曲线名称列表
     * 注意：xCoordBuffer保存数据采集时间；
     *      yCoordBuffer保存2个对象——评估指标和阈值。每一个对象内部包含一个缓冲区，用于存储Y坐标数据
     */
    initStackBuffer(legendData) {
      recvBuffer.xCoordBuffer = []        //  实时数据X坐标
      recvBuffer.yCoordBuffer = []        //  实时数据Y坐标
      // recvBuffer.alarmBuffer = []         //  故障报警
      // recvBuffer.declineBuffer = []       //  故障预警
      // recvBuffer.warningBuffer = []       //  早期退化
      legendData.forEach(el => {
        recvBuffer.yCoordBuffer.push({
          name: el,               //  缓冲区名称
          buffer: []              //  数据接收缓冲区
        })
      })
    },

    //  从后台拉取数据，放入接收缓冲区
    addDataToBuffer(serieNo) {
      let dcData = []
      let threshold = []
      let anomalyFlag = []
      let dcTime = []
      reqDataSet(curTaskId, curAlName).then((res) => {
        if (res.data.code === 0) {
          dcData = res.data.data[0].dc_dataList
          threshold = res.data.data[0].thresholdList
          anomalyFlag = res.data.data[0].anomaly_flagList
          dcTime = res.data.data[0].dcTimeList
          for (let i = 0; i < dcData.length; i++) {
            try {
              if ((typeof dcData[i] === 'number') &&
                  (typeof threshold[i] === 'number') &&
                  (typeof anomalyFlag[i] === 'number') &&
                  (typeof dcTime[i] === 'string')) {   //  如果X坐标值为字符串，Y坐标值为数值
                recvBuffer.yCoordBuffer[serieNo].buffer.push(dcData[i])        //  保存传感器实时数据
                recvBuffer.yCoordBuffer[serieNo + 1].buffer.push(threshold[i])
                recvBuffer.yCoordBuffer[serieNo + 2].buffer.push(anomalyFlag[i])

                if (recvBuffer.xCoordBuffer.length < recvBuffer.yCoordBuffer[serieNo].buffer.length)
                  recvBuffer.xCoordBuffer.push(dcTime[i])          //  保存X坐标数据
              }
            } catch (exception) {
              console.log('错误的数据格式')
            }
          }
        }
      })
    },

    //  启动定时器
    stratTimer() {
      let self = this
      intervalId = setInterval(() => {       //  每秒更新一次数据
        if (intervalId) {                                          //  如果定时器没有被销毁
          for (let i = 0; i < myOption.series.length - 1; i += 3) {     //  在每一次循环中，检查接收缓冲区，数据不够时，从后台拉取
            if (recvBuffer.yCoordBuffer[i].buffer.length < recvBuffer.minBuffer)
              self.addDataToBuffer(i)          //  如果缓冲区中没有了足够数据，从后台拉取数据，存入缓冲区
          }
          self.refreshChart()                       //  更新折线图
        }
      }, 1000)
    },

    //  将接收缓冲区的dataNum个数据写入图表
    refreshChart() {
      let flag = false
      for (let i = 0; i < recvBuffer.yCoordBuffer.length; i++) {
        if (recvBuffer.yCoordBuffer[i].buffer.length >= dataNum) {       //  如果缓冲区有可以更新的数据，刷新图表
          let echartSerie = myOption.series.find((el) => {           //  根据接收缓冲区名称在图表中找到相应的对象
            return el.name === recvBuffer.yCoordBuffer[i].name
          })
          if (echartSerie) {
            for (let j = 0; j < dataNum; j++)
              echartSerie.data.push(recvBuffer.yCoordBuffer[i].buffer[j])        //  移动接收缓冲区的数据到图表的显示缓冲区, 样例数据：echartSerie.data =  [120, 132, 101, 134, 90, 230, 210]
            if (echartSerie.data.length > maxChartBuffer)
              echartSerie.data.splice(0, echartSerie.data.length - maxChartBuffer)      //  删除前面的数据
            recvBuffer.yCoordBuffer[i].buffer.splice(0, dataNum)      //  删除前面的数据
            flag = true
          }
        }
      }
      if (flag) {      //  如果Y坐标进行了数据更新，则同步更新X坐标数据
        for (let k = 0; k < dataNum; k++)
          myOption.xAxis.data.push(recvBuffer.xCoordBuffer[k])       //  样例数据：myOption.xAxis.data = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        recvBuffer.xCoordBuffer.splice(0, dataNum)
        if (myOption.xAxis.data.length > maxChartBuffer)
          myOption.xAxis.data.splice(0, myOption.xAxis.data.length - maxChartBuffer)
      }
      multLineChart.setOption({       //  更新图表数据
        series: myOption.series,
        xAxis: myOption.xAxis
      })
    },

    //  初始化多条折线图(Stacked Line Chart)
    initCharts(title, legendData, yAxisName) {
      multLineChart = multLineChart ? multLineChart : echarts.init(document.getElementById('chartzone'))
      myOption = multLineOption(title, legendData, yAxisName)
      this.applyDarkChartTheme(myOption)
      myOption && multLineChart.setOption(myOption)
    },

    applyDarkChartTheme(option) {
      if (!option) return
      option.backgroundColor = 'transparent'
      if (option.title?.textStyle) option.title.textStyle.color = '#e8fbff'
      if (option.legend?.textStyle) option.legend.textStyle.color = 'rgba(232,251,255,0.8)'
      option.tooltip = {
        ...option.tooltip,
        backgroundColor: 'rgba(4, 33, 56, 0.95)',
        borderColor: 'rgba(65, 228, 187, 0.28)',
        textStyle: { color: '#e8fbff' }
      }
      const axisStyle = { color: 'rgba(232,251,255,0.7)' }
      option.xAxis = {
        ...option.xAxis,
        nameTextStyle: axisStyle,
        axisLabel: axisStyle,
        axisLine: { lineStyle: { color: 'rgba(141,231,207,0.35)' } },
        splitLine: { show: false }
      }
      option.yAxis = {
        ...option.yAxis,
        nameTextStyle: axisStyle,
        axisLabel: axisStyle,
        axisLine: { lineStyle: { color: 'rgba(141,231,207,0.35)' } },
        splitLine: { show: true, lineStyle: { color: 'rgba(65,228,187,0.1)' } }
      }
      option.color = ['#2cb4ff', '#ffa500', '#fd626e']
    },

    stopRefresh() {
      if (intervalId !== null) {
        clearInterval(intervalId)     //  关闭定时器
        intervalId = null
      }
    },
    clear() {
      if (intervalId !== null) {
        clearInterval(intervalId)     //  关闭定时器
        intervalId = null
      }
      multLineChart = null            //  页面关闭时，必须清除
      myOption = null                 //  页面关闭时，必须清除
      curTaskId = ''
      curAlName = ''
      this.isShow = false
      this.isShow1 = false
    },

    HandleHistory(){
      if(this.time === ''){
        ElMessage({
          message: "请选择时间！",
          type: 'warning'
        })
      }else {
        let startTime = dateFormat(this.time[0])
        let endTime = dateFormat(this.time[1])

        if(curAlName !== '' && curTaskId !== ''){
          this.$refs.refHistory.loadChart(globeParams.curNode.id, curTaskId, curAlName, startTime, endTime)
        }else {
          ElMessage({
            message: "请选择状态感知模板！",
            type: 'warning'
          })
        }
      }
    },

    HandleFault(){
      if(this.time === ''){
        ElMessage({
          message: "请选择时间！",
          type: 'warning'
        })
      }else {
        let startTime = dateFormat(this.time[0])
        let endTime = dateFormat(this.time[1])

        this.$refs.refHistory.loadTable(globeParams.curNode.id, startTime, endTime)
      }
    },

    clearHistory(){
      this.time = ''
      this.$refs.refHistory.clearData()
    }

  },


}
</script>
<style lang="scss" scoped>
.stateChart {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;

  .tagzone {
    display: flex; //  按行排列
    width: 100%;
    height: 41px;
    background: rgba(65, 228, 187, 0.08);
    border-bottom: 1px solid rgba(65, 228, 187, 0.16);

    span {
      display: block;
      padding: 0;
      width: 120px;
      height: 41px;
      font-size: 16px;
      text-align: center; /*文字水平居中*/
      line-height: 41px; /*文字垂直居中*/
      color: rgba(232, 251, 255, 0.86);
    }

    ::v-deep .el-tabs__header {
      border: 0px;
    }
  }

  #chartzone {
    width: 100%;
    flex: 1;
    min-height: 0;
  }

  .table-title {
    height: 44px;
    width: 100%;
    display: flex;
    color: #dffcff;
    letter-spacing: 0.5px;
    justify-content: flex-start;
    align-items: center;
    padding: 0 18px;
    font-weight: 600;
    border-bottom: 1px solid rgba(65, 228, 187, 0.24);
    background:
      linear-gradient(90deg, rgba(65, 228, 187, 0.24), rgba(44, 180, 255, 0.1), transparent),
      rgba(1, 18, 32, 0.45);
    box-sizing: border-box;
  }

  .table-title::before {
    content: "";
    width: 4px;
    height: 18px;
    margin-right: 10px;
    border-radius: 2px;
    background: #41e4bb;
    box-shadow: 0 0 12px rgba(65, 228, 187, 0.9);
  }
}
.chart-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 6px 14px 0;
}

:deep(.chart-actions .el-button) {
  margin-top: 0 !important;
  color: #8de7cf;
}

.timeSelect{
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.historyChart{
  margin-top: 20px;
  width: 100%;
  height: 400px;
  padding: 10px;
  border: 1px solid rgba(65, 228, 187, 0.16);
  border-radius: 8px;
  background: rgba(2, 27, 38, 0.46);
  box-sizing: border-box;
}

:global(.config-history-dialog) {
  background: rgba(4, 33, 56, 0.96);
  border: 1px solid rgba(65, 228, 187, 0.24);
  box-shadow: 0 22px 70px rgba(0, 0, 0, 0.42);
  border-radius: 8px;
}

:global(.config-history-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 16px 20px 12px;
  border-bottom: 1px solid rgba(65, 228, 187, 0.18);
}

:global(.config-history-dialog .el-dialog__title),
:global(.config-history-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #e8fbff;
}

:global(.config-history-dialog .el-dialog__body) {
  padding: 16px 20px 20px;
  color: #e8fbff;
}

:global(.config-history-dialog .el-range-editor.el-input__wrapper),
:global(.config-history-dialog .el-input__wrapper) {
  background: rgba(2, 27, 38, 0.72);
  border: 1px solid rgba(65, 228, 187, 0.28);
  box-shadow: none;
}

:global(.config-history-dialog .el-range-input),
:global(.config-history-dialog .el-range-separator),
:global(.config-history-dialog .el-input__inner) {
  color: #e8fbff;
  background: transparent;
}

:global(.config-history-dialog .el-date-editor .el-range__icon) {
  color: #8de7cf;
}

:deep(.viewBtn),
:deep(.printBtn) {
  border-color: rgba(65, 228, 187, 0.45);
  background: rgba(65, 228, 187, 0.12);
  color: #8de7cf;
}
</style>
