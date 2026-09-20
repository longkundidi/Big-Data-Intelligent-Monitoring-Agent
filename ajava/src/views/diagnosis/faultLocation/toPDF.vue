<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      title="故障诊断报告"
      show-close
      style="width: 50%;"
      align-center
      center
  >
    <el-scrollbar height="850px" class="scroll-content">
      <!-- 封面 -->
      <div ref="coverPage" class="cover-page">
        <div class="logo-container">
          <img src="/img/myWel/swjtu.jpg" alt="Logo" style="width: 200px; height: auto;" />
        </div>
        <h1>机组故障诊断报告</h1>
        <h2>{{ pdfData.farmName }}</h2>
        <h3>{{ pdfData.monitor.turbineName }}</h3>
        <p style="text-align: center; margin-top: 150px;">
          报告生成时间：<span>{{ formattedDate }}</span>
          <br/><br/>
          报 告 审 核：<span>{{ pdfData.user }}</span>
        </p>
      </div>

      <!-- 内容页 -->
      <div ref="contentPage_1" class="content-page">
        <h1>1. 机型信息</h1>
        <table class="turbine-info-table">
          <thead>
          <tr>
            <th>风场名称</th>
            <th>风机名称</th>
            <th>风机编码</th>
            <th>故障部件</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>{{ pdfData.farmName }}</td>
            <td>{{ pdfData.monitor.turbineName }}</td>
            <td>{{ pdfData.monitor.turbineCode }}</td>
            <td>{{ pdfData.monitor.nodeName }}</td>
          </tr>
          </tbody>
        </table>

        <h1>2. 诊断结论</h1>
        <p class="indent-first-line">{{ pdfData.monitor.dcTime }}，基于 SCADA 数据的{{ pdfData.monitor.modelName }}报警显示异常，{{ pdfData.farmName }}{{ pdfData.monitor.turbineName }}风机发生故障。基于 CMS 数据的{{ pdfData.diagnosisModel }}诊断结果为{{ pdfData.monitor.nodeName }}位置发生故障。异常检测和故障诊断结果分别如图 1 和图 2 所示。</p>
        <div ref="evaluationChart" class="chart-container" ></div>
        <p class="graph-title">图 1 SCADA 预警模型报警图</p>
        <div ref="diagnosisChart" class="chart-container" ></div>
        <p class="graph-title">图 2 CMS 故障诊断模型诊断图</p>
      </div>

      <div ref="contentPage_2" class="content-page">
        <h1>3. 分析过程</h1>
        <h2>3.1.趋势、指标参数等证据说明</h2>
        <p class="indent-first-line">两分钟左右数据显示，测点总值突发性上升，幅值较之前的稳态阶段明显增加，大约达到数倍。如图 3 所示。另外，{{ pdfData.monitor.nodeName }}测点峭度指标趋势同步异常，也出现突发性高点，如图 4 所示。由此表明{{ pdfData.monitor.nodeName }}位置存在明显故障。</p>
        <div v-if="!signalResult" class="image-slot">
          加载中<span class="dot">...</span>
        </div>
        <el-image
            v-else
            style="width: 100%; height: 305px;"
            :src="'/api/al/file/' + signalResult + '/振动信号幅值.png'">
        </el-image>
        <p class="graph-title">图 3 设备振动信号幅值图</p>

        <div v-if="!signalResult" class="image-slot">
          加载中<span class="dot">...</span>
        </div>
        <el-image
            v-else
            style="width: 100%; height: 305px;"
            :src="'/api/al/file/' + signalResult + '/振动信号峭度.png'">
        </el-image>
        <p class="graph-title">图 4 设备振动信号峭度图</p>
      </div>

      <div ref="contentPage_3" class="content-page">
        <h2>3.2.趋势、指标参数等证据说明</h2>
        <p class="indent-first-line">如图 5 所示，由设备加速度时域波形可见，在{{ pdfData.monitor.nodeName }}测点采集的数据时域波形中存在清晰的周期性冲击特征，波形呈尖锐状，说明{{ pdfData.monitor.nodeName }}存在损伤。</p>
        <div v-if="!signalResult" class="image-slot">
          加载中<span class="dot">...</span>
        </div>
        <el-image
            v-else
            style="width: 100%; height: 305px;"
            :src="'/api/al/file/' + signalResult + '/最高峭度对应的振动信号幅值.png'">
        </el-image>
        <p class="graph-title">图 5 测点最高峭度对应的振动信号幅值</p>

        <p class="indent-first-line">频谱分析可以将一个时域信号分解成多个频率成分，从而揭示信号中的组成频率成分、噪声情况等。对设备加速度时域信号进行频谱分析，如图 6 所示。由图可知，在{{ pdfData.monitor.nodeName }}测点采集的信号的频谱中存在显著的峰值，说明{{ pdfData.monitor.nodeName }}存在损伤。</p>
        <div v-if="!signalResult" class="image-slot">
          加载中<span class="dot">...</span>
        </div>
        <el-image
            v-else
            style="width: 100%; height: 305px;"
            :src="'/api/al/file/' + signalResult + '/振动信号频谱.png'">
        </el-image>
        <p class="graph-title">图 6 测点振动信号频谱图</p>
      </div>

      <div ref="contentPage_4" class="content-page">
        <p class="indent-first-line">包络谱分析是对信号进行包络，再对获取的包络信号进行频谱分析，可以清楚地揭示高频信号中的低频调制特性（如转频等）。对设备加速度时域信号进行包络谱分析，如图 7 所示。由图可知，在{{ pdfData.monitor.nodeName }}测点采集的信号包络谱中存在显著的峰值及大量谐波成分，说明存在故障。</p>
        <div v-if="!signalResult" class="image-slot">
          加载中<span class="dot">...</span>
        </div>
        <el-image
            v-else
            style="width: 100%; height: 380px;"
            :src="'/api/al/file/' + signalResult + '/振动信号包络谱.png'">
        </el-image>
        <p class="graph-title">图 7 测点振动信号包络谱图</p>
        <h1>4. 维修建议</h1>
        <p v-if="suggests" class="indent-first-line">{{suggests}}</p>
        <p v-else style="color: #2d8cf0;text-align: center">正在加载中……</p>
        <p v-else style="color: #2d8cf0;text-align: center">暂无建议</p>
      </div>
    </el-scrollbar>

    <template #footer>
      <el-button
          @click="handleExport"
          class="printBtn"
      >导出</el-button>
    </template>
  </el-dialog>

</template>
<script>
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import {getUser, signalAnalysis} from "@/api/diagnosis/faultLocation";
import * as echarts from "echarts";
import axios from "axios";
import {multLineOptionNoTitle} from "@/const/crud/diagnosis/stateAssessment/myChart";
import {reqHistoryDataSet} from "@/api/diagnosis/stateAssessment/stateAssessment";
import {addSub_Minutes} from "@/util/date";
import {ElMessage} from "element-plus";

export default {
  name: "pdf",
  props: {
    chartData: Array,
  },
  data() {
    return {
      visible: false,
      pdfData: [],
      evaluationData: {
        xCoordBuffer: [],
        yCoordBuffer: []
      },
      evaluationInstance : null,
      diagnosisInstance: null,
      suggests: '',
      signalResult: null
    }
  },

  computed: {
    formattedDate() {
      const date = new Date()
      const options = { year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit',
        hour12: false }
      return date.toLocaleString(undefined, options)
    }
  },
  watch: {
    chartData: {
      handler(newData) {
        this.updatePie()
      },
      deep: true,
    },
  },

  methods: {
    async init(pdfData) {
      this.visible = true
      this.pdfData = pdfData
      this.signalResult = null
      this.suggests = ''
      await getUser().then((res) => {
        if (res.data.code === 0) {
          this.pdfData.user = res.data.data
        }
      })
      // 确保DOM元素已经更新后再初始化图表
      await this.$nextTick(() => {
        this.loadEvaluationData()
        this.initPieChart()
        this.getSignalAnalysis()
        this.getSuggests()
      })

    },

    async handleExport() {
      try {
        // 创建一个新的 PDF 文档
        const doc = new jsPDF('p', 'mm', 'a4')

        // 导出封面
        const coverCanvas = await html2canvas(this.$refs.coverPage)
        const coverImage = coverCanvas.toDataURL('image/png')
        doc.addImage(coverImage, 'PNG', 0, 0, 210, 297)

        // 添加新页
        doc.addPage();
        const contentCanvas_1 = await html2canvas(this.$refs.contentPage_1)
        const contentImage_1 = contentCanvas_1.toDataURL('image/png')
        doc.addImage(contentImage_1, 'PNG', 0, 0, 210, 297)

        // 添加新页
        doc.addPage();
        const contentCanvas_2 = await html2canvas(this.$refs.contentPage_2)
        const contentImage_2 = contentCanvas_2.toDataURL('image/png')
        doc.addImage(contentImage_2, 'PNG', 0, 0, 210, 297)

        doc.addPage();
        const contentCanvas_3 = await html2canvas(this.$refs.contentPage_3)
        const contentImage_3 = contentCanvas_3.toDataURL('image/png')
        doc.addImage(contentImage_3, 'PNG', 0, 0, 210, 297)

        doc.addPage();
        const contentCanvas_4 = await html2canvas(this.$refs.contentPage_4)
        const contentImage_4 = contentCanvas_4.toDataURL('image/png')
        doc.addImage(contentImage_4, 'PNG', 0, 0, 210, 297)

        // 保存 PDF
        doc.save(`故障诊断报告-` + this.formattedDate + `.pdf`)

      } catch (error) {
        console.error('Error exporting to PDF:', error)
      }
    },

    initEvaluationChart(legendData){
      this.evaluationInstance = echarts.init(this.$refs.evaluationChart)
      const option = multLineOptionNoTitle(legendData)
      option.series = legendData.map((label, index) => {
        return {
          name: label,
          type: 'line',
          data: this.evaluationData.yCoordBuffer[index].buffer
        }
      })

      // 扩展 xAxis.data
      option.xAxis.data = this.evaluationData.xCoordBuffer
      this.evaluationInstance.setOption(option)

    },

    loadEvaluationData() {

      // 获取当前时间和前一天的时间
      const now = new Date()
      const formatOptions = { timeZone: 'Asia/Shanghai', hour12: false }
      const endTime = now.toLocaleString('zh-CN', formatOptions).replace(/[/ :]/g, '-')

      const oneDayAgo = new Date(now)
      oneDayAgo.setHours(oneDayAgo.getHours() - 24)
      const startTime = oneDayAgo.toLocaleString('zh-CN', formatOptions).replace(/[/ :]/g, '-')

      reqHistoryDataSet({
        taskId: this.pdfData.monitor.taskId,
        algoShortname: this.pdfData.monitor.modelShortName,
        startDcTime: startTime,
        endDcTime: endTime
      }).then(res => {
        if (res.data.code === 0) {
          const dcData = res.data.data[0].dc_dataList
          const anomalyFlag = res.data.data[0].anomaly_flagList
          const dcTime = res.data.data[0].dcTimeList

          this.evaluationData.yCoordBuffer = [
            {name: '评估指标', buffer: dcData},
            {name: '报警', buffer: anomalyFlag}
          ]

          dcTime.forEach((time, index) => {
            this.evaluationData.xCoordBuffer.push(time)
          })

          this.initEvaluationChart(['评估指标', '报警'])
        }
      })
    },

    //  初始化饼图
    initPieChart(){
      const diagnosisInstance = echarts.init(this.$refs.diagnosisChart)
      diagnosisInstance.setOption({
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
            radius: '70%',
            center: ['50%', '50%'],
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
      this.diagnosisInstance = diagnosisInstance

    },
    updatePie(){
      if (this.diagnosisInstance) {
        this.diagnosisInstance.setOption({
          series: [
            {
              data: this.chartData,
            }
          ]
        })
      }
    },

    //获取维修建议
    async  postRequest_GLM3(param) {
      try {
        const response = await axios.post('/remote/v1/chat/completions', param, {
          headers: {
            'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
          }
        });
        return response.data; // 返回响应数据
      } catch (error) {
        throw new Error(`Axios error! ${error.message}`); // 捕获错误并抛出
      }
    },
    async getSuggests() {
      let param_gpt = {
        "model": "chatglm3-6b",
        "messages": [
          {
            "role": "system",
            "content": "You are ChatGLM3-fengji, a large language model trained by Zhipu.AI. Follow the user’s instructions carefully. Respond using markdown."
          },
          {
            "role": "user",
            "content": "请认真准确地回复下面问题：" + this.pdfData.faultType
          }
        ],
        "stream": false,
        "max_tokens": 100,
        "temperature": 0.1,
        "top_p": 0.8,

      }
      // await this.postRequest_GLM3(param_gpt).then((r) => {
      //   this.suggests=r.choices[0].message.content.replace(/(\*\*.*?\*\*)/g, '').trim()
      // })
    },

    async getSignalAnalysis() {
      let endTime = new Date(this.pdfData.monitor.dcTime)
      let startTime = addSub_Minutes(this.pdfData.monitor.dcTime, -2)
      let param = {
        farmName: this.pdfData.farmName,
        turbineName: this.pdfData.monitor.turbineName,
        part:this.pdfData.monitor.nodeName,
        location: this.pdfData.monitor.location,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
      }
      await signalAnalysis(param).then(res => {
        if(res.data.code === "200"){
          this.signalResult = JSON.parse(res.data.data.taskResult).filepath
        }else {
          ElMessage.error("信号分析失败！")
        }
      })
    }

  }
};
</script>
<style lang="scss" scoped>

.scroll-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.cover-page {
  text-align: center;
  width: 210mm; /* A4 宽度 */
  height: 297mm; /* A4 高度 */
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;

  .logo-container {
    margin-bottom: 20px;
  }

  h1 {
    font-size: 50px;
  }

  h1, h2, h3 {
    margin: 10px 0;
  }

  p {
    font-size: 16px;
    color: #555;
  }
}

.content-page {
  text-align: left;
  width: 220mm; /* A4 宽度 */
  height: 297mm; /* A4 高度 */
  padding: 20mm;
  box-sizing: border-box;

  .turbine-info-table {
    width: 100%;
    border-collapse: collapse;
  }

  .turbine-info-table th,
  .turbine-info-table td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: center;
    font-family: 'SimSun', serif;
    font-size: 16px;
  }

  .turbine-info-table th {
    background-color: #f2f2f2;
    font-family: 'SimSun', serif;
    font-size: 15px;
    color: #000;
  }

  h1 {
    font-size: 30px;
  }

  p {
    font-size: 20px;
    font-family: 'SimSun', serif;
    line-height: 1.5;
    color: #000;
    margin: 10px;
  }

  .indent-first-line {
    text-indent: 2em;
  }
}

.graph-title {
  font-weight: bold;
  text-align: center;
  line-height: 1;
  margin-bottom: 5px
}

.chart-container {
  width: 100%;
  height: 280px;
  margin: 0;
  padding: 0;
}

.image-slot {
  font-size: 20px;
  color: #2d8cf0;
  text-align: center;
}
.dot {
  display: inline-flex;
  width: 1em;
  overflow: hidden;
}


</style>
