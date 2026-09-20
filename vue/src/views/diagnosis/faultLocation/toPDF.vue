<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      title="电梯故障诊断报告"
      width="min(92vw, 980px)"
      show-close
      align-center
      center
  >
    <el-scrollbar height="850px" class="scroll-content">
      <div ref="coverPage" class="report-page cover-page">
        <div class="logo-container">
          <img src="/img/myWel/swjtu.jpg" alt="Logo" />
        </div>
        <h1>电梯故障诊断报告</h1>
        <h2>{{ pdfData.farmName }}</h2>
        <h3>{{ pdfData.monitor?.turbineName }}</h3>
        <div class="cover-meta">
          <p>数据来源：CMS 原始振动数据</p>
          <p>报告生成时间：{{ formattedDate }}</p>
          <p>报告审核：{{ pdfData.user || '-' }}</p>
        </div>
      </div>

      <div ref="contentPage1" class="report-page content-page">
        <h1>1. 设备与数据</h1>
        <table class="report-table">
          <thead>
          <tr>
            <th>小区名称</th>
            <th>电梯名称</th>
            <th>设备编码</th>
            <th>CMS 测点</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>{{ pdfData.farmName || '-' }}</td>
            <td>{{ pdfData.monitor?.turbineName || '-' }}</td>
            <td>{{ pdfData.monitor?.turbineCode || '-' }}</td>
            <td>{{ pdfData.monitor?.nodeName || '-' }}</td>
          </tr>
          </tbody>
        </table>

        <table class="report-table source-table">
          <tbody>
          <tr>
            <th>测点标识</th>
            <td>{{ signalInfo.monitorPointId || '-' }}</td>
            <th>样本数</th>
            <td>{{ signalInfo.sampleCount || '-' }}</td>
          </tr>
          <tr>
            <th>告警时间</th>
            <td>{{ formatTime(signalInfo.requestedTime || pdfData.monitor?.dcTime) }}</td>
            <th>匹配数据时间</th>
            <td>{{ formatTime(signalInfo.matchedTime) }}</td>
          </tr>
          <tr>
            <th>时间偏差</th>
            <td>{{ formatNumber(signalInfo.timeOffsetSeconds, 3) }} 秒</td>
            <th>数据一致性</th>
            <td>{{ hasSignalData ? '与诊断输入一致' : '缺少诊断输入数据' }}</td>
          </tr>
          </tbody>
        </table>

        <h1>2. 诊断结论</h1>
        <p class="paragraph" v-if="hasSignalData">
          {{ formatTime(signalInfo.requestedTime || pdfData.monitor?.dcTime) }}，状态监测算法基于 CMS 原始振动数据产生报警。
          平台在该告警时间前后 3 分钟内定位到时间最接近的 1024 点振动窗口，匹配偏差为
          {{ formatNumber(signalInfo.timeOffsetSeconds, 3) }} 秒。故障诊断模型对同一数据窗口进行分类，
          结果为“{{ pdfData.faultType || '未知' }}”，概率为 {{ formatNumber(pdfData.faultProbability, 4) }}%。
        </p>
        <p class="paragraph warning-text" v-else>
          本次任务未返回 FFCNet 实际使用的 1024 点 CMS 原始振动数据，报告不生成信号证据图，避免引用其他设备或其他时间的数据。
        </p>
        <div ref="diagnosisChart" class="chart-container diagnosis-chart"></div>
        <p class="graph-title">图 1 故障诊断类别概率</p>
      </div>

      <div ref="contentPage2" class="report-page content-page">
        <h1>3. CMS 原始振动数据分析</h1>
        <h2>3.1 原始波形</h2>
        <p class="paragraph">
          下图直接使用故障诊断任务中的 1024 个原始数值，横轴为窗口内采样点序号，纵轴保持源数据数值，不进行平滑或替换。
        </p>
        <div v-if="hasSignalData" ref="waveformChart" class="chart-container waveform-chart"></div>
        <div v-else class="data-unavailable">{{ signalError }}</div>
        <p class="graph-title">图 2 CMS 原始振动波形</p>

        <h2>3.2 时域统计特征</h2>
        <table v-if="hasSignalData" class="report-table metric-table">
          <thead>
          <tr>
            <th>指标</th>
            <th>计算值</th>
            <th>指标</th>
            <th>计算值</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>均值</td><td>{{ formatNumber(signalMetrics.mean) }}</td>
            <td>标准差</td><td>{{ formatNumber(signalMetrics.standardDeviation) }}</td>
          </tr>
          <tr>
            <td>有效值 RMS</td><td>{{ formatNumber(signalMetrics.rms) }}</td>
            <td>绝对峰值</td><td>{{ formatNumber(signalMetrics.peakAbsolute) }}</td>
          </tr>
          <tr>
            <td>峰峰值</td><td>{{ formatNumber(signalMetrics.peakToPeak) }}</td>
            <td>峰值因子</td><td>{{ formatNumber(signalMetrics.crestFactor) }}</td>
          </tr>
          <tr>
            <td>峭度</td><td>{{ formatNumber(signalMetrics.kurtosis) }}</td>
            <td>主频点</td><td>{{ signalMetrics.dominantBin ?? '-' }}</td>
          </tr>
          </tbody>
        </table>
        <p class="note">
          上述指标仅描述本次数据窗口的数值特征。源记录未包含振动单位、传感器量程和标定系数，因此报告不推断物理量单位，也不使用固定阈值代替现场标准。
        </p>
      </div>

      <div ref="contentPage3" class="report-page content-page">
        <h1>4. 频域分析</h1>
        <p class="paragraph">
          频谱由本次 1024 点 CMS 原始振动数据去均值、加 Hann 窗后执行 FFT 得到。
          当前记录只有“45Hz 工况”信息，没有可靠的采集采样率元数据，因此横轴使用频点序号，不将频点错误标记为实际 Hz。
        </p>
        <div v-if="hasSignalData" ref="spectrumChart" class="chart-container spectrum-chart"></div>
        <div v-else class="data-unavailable">{{ signalError }}</div>
        <p class="graph-title">图 3 CMS 原始振动幅值谱</p>
        <p v-if="hasSignalData" class="paragraph">
          本窗口最大非直流谱线位于频点 {{ signalMetrics.dominantBin }}，幅值为
          {{ formatNumber(signalMetrics.dominantMagnitude) }}。该结果用于描述当前窗口的频域分布；在补充真实采样率、转速和故障特征频率前，不据此自动宣称存在某一机械故障频率或谐波关系。
        </p>

        <h1>5. 维修建议</h1>
        <p v-if="suggests" class="paragraph">{{ suggests }}</p>
        <p v-else class="no-suggestion">暂无建议</p>

      </div>
    </el-scrollbar>

    <template #footer>
      <el-button @click="handleExport" class="printBtn">导出</el-button>
    </template>
  </el-dialog>
</template>

<script>
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import * as echarts from 'echarts'
import {getUser} from '@/api/diagnosis/faultLocation'

export default {
  name: 'pdf',
  props: {
    chartData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      visible: false,
      pdfData: { monitor: {} },
      suggests: '',
      hasSignalData: false,
      signalError: '本次诊断任务没有可用的 1024 点 CMS 原始振动数据',
      signalValues: [],
      spectrumData: [],
      signalMetrics: {},
      diagnosisInstance: null,
      waveformInstance: null,
      spectrumInstance: null
    }
  },
  computed: {
    formattedDate() {
      return new Date().toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        hour12: false
      })
    },
    signalInfo() {
      return this.pdfData.cmsSignal || {}
    }
  },
  watch: {
    chartData: {
      handler() {
        this.updateDiagnosisChart()
      },
      deep: true
    }
  },
  beforeUnmount() {
    this.disposeCharts()
  },
  methods: {
    async init(pdfData) {
      this.visible = true
      this.pdfData = pdfData || { monitor: {} }
      this.suggests = this.pdfData.suggest || ''
      this.prepareSignalData(this.pdfData.cmsSignal?.values)

      if (!this.pdfData.user) {
        try {
          const response = await getUser()
          if (response.data.code === 0) {
            this.pdfData.user = response.data.data
          }
        } catch (error) {
          console.warn('报告审核人读取失败', error)
        }
      }

      await this.$nextTick()
      this.disposeCharts()
      this.initDiagnosisChart()
      if (this.hasSignalData) {
        this.initWaveformChart()
        this.initSpectrumChart()
      }
    },

    prepareSignalData(values) {
      const normalized = Array.isArray(values) ? values.map(Number) : []
      this.hasSignalData = normalized.length === 1024 && normalized.every(Number.isFinite)
      if (!this.hasSignalData) {
        this.signalValues = []
        this.spectrumData = []
        this.signalMetrics = {}
        return
      }

      this.signalValues = normalized
      const count = normalized.length
      const mean = normalized.reduce((sum, value) => sum + value, 0) / count
      const centered = normalized.map(value => value - mean)
      const variance = centered.reduce((sum, value) => sum + value * value, 0) / count
      const standardDeviation = Math.sqrt(variance)
      const rms = Math.sqrt(normalized.reduce((sum, value) => sum + value * value, 0) / count)
      const peakAbsolute = Math.max(...normalized.map(Math.abs))
      const peakToPeak = Math.max(...normalized) - Math.min(...normalized)
      const fourthMoment = centered.reduce((sum, value) => sum + Math.pow(value, 4), 0) / count
      const kurtosis = variance > 0 ? fourthMoment / Math.pow(variance, 2) : 0
      const crestFactor = rms > 0 ? peakAbsolute / rms : 0

      this.spectrumData = this.calculateSpectrum(centered)
      const dominant = this.spectrumData.slice(1).reduce((best, item) => item[1] > best[1] ? item : best, [0, 0])
      this.signalMetrics = {
        mean,
        standardDeviation,
        rms,
        peakAbsolute,
        peakToPeak,
        kurtosis,
        crestFactor,
        dominantBin: dominant[0],
        dominantMagnitude: dominant[1]
      }
    },

    calculateSpectrum(centeredValues) {
      const size = centeredValues.length
      const real = centeredValues.map((value, index) => {
        const hann = 0.5 * (1 - Math.cos((2 * Math.PI * index) / (size - 1)))
        return value * hann
      })
      const imaginary = new Array(size).fill(0)

      for (let index = 1, reversed = 0; index < size; index += 1) {
        let bit = size >> 1
        while (reversed & bit) {
          reversed ^= bit
          bit >>= 1
        }
        reversed ^= bit
        if (index < reversed) {
          ;[real[index], real[reversed]] = [real[reversed], real[index]]
          ;[imaginary[index], imaginary[reversed]] = [imaginary[reversed], imaginary[index]]
        }
      }

      for (let length = 2; length <= size; length <<= 1) {
        const angle = -2 * Math.PI / length
        const baseReal = Math.cos(angle)
        const baseImaginary = Math.sin(angle)
        for (let start = 0; start < size; start += length) {
          let weightReal = 1
          let weightImaginary = 0
          for (let offset = 0; offset < length / 2; offset += 1) {
            const evenIndex = start + offset
            const oddIndex = evenIndex + length / 2
            const oddReal = real[oddIndex] * weightReal - imaginary[oddIndex] * weightImaginary
            const oddImaginary = real[oddIndex] * weightImaginary + imaginary[oddIndex] * weightReal
            real[oddIndex] = real[evenIndex] - oddReal
            imaginary[oddIndex] = imaginary[evenIndex] - oddImaginary
            real[evenIndex] += oddReal
            imaginary[evenIndex] += oddImaginary
            const nextWeightReal = weightReal * baseReal - weightImaginary * baseImaginary
            weightImaginary = weightReal * baseImaginary + weightImaginary * baseReal
            weightReal = nextWeightReal
          }
        }
      }

      return Array.from({ length: size / 2 }, (_, index) => [
        index,
        2 * Math.hypot(real[index], imaginary[index]) / size
      ])
    },

    initDiagnosisChart() {
      if (!this.$refs.diagnosisChart) return
      this.diagnosisInstance = echarts.init(this.$refs.diagnosisChart)
      this.diagnosisInstance.setOption(this.probabilityChartOption())
    },

    updateDiagnosisChart() {
      if (this.diagnosisInstance) {
        this.diagnosisInstance.setOption(this.probabilityChartOption(), true)
      }
    },

    probabilityChartOption() {
      const colors = ['#2488c5', '#55a868', '#e5a73f', '#d95f59', '#7668b8', '#38a89d', '#e47f45', '#a65ab0']
      return {
        animation: false,
        grid: { left: 170, right: 62, top: 8, bottom: 24 },
        xAxis: {
          type: 'value', min: 0, max: 100,
          axisLabel: { color: '#555', fontSize: 10, formatter: '{value}%' },
          splitLine: { lineStyle: { color: '#e7e7e7' } }
        },
        yAxis: {
          type: 'category', inverse: true,
          data: this.chartData.map(item => item.name),
          axisTick: { show: false }, axisLine: { show: false },
          axisLabel: { color: '#222', fontSize: 10, width: 158, overflow: 'truncate' }
        },
        series: [{
          type: 'bar',
          data: this.chartData.map(item => Number(item.value)),
          barMaxWidth: 16,
          showBackground: true,
          backgroundStyle: { color: '#f1f3f5' },
          itemStyle: {
            borderRadius: [0, 2, 2, 0],
            color: params => colors[params.dataIndex % colors.length]
          },
          label: {
            show: true, position: 'right', color: '#222', fontSize: 10,
            formatter: params => `${Number(params.value).toFixed(2)}%`
          }
        }]
      }
    },

    initWaveformChart() {
      this.waveformInstance = echarts.init(this.$refs.waveformChart)
      this.waveformInstance.setOption({
        animation: false,
        grid: { left: 58, right: 24, top: 22, bottom: 48 },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'value', name: '采样点', min: 0, max: 1023,
          nameLocation: 'middle', nameGap: 30,
          axisLabel: { color: '#555' }
        },
        yAxis: {
          type: 'value', name: '原始值',
          axisLabel: { color: '#555' },
          splitLine: { lineStyle: { color: '#e7e7e7' } }
        },
        series: [{
          type: 'line', showSymbol: false, sampling: 'lttb',
          data: this.signalValues.map((value, index) => [index, value]),
          lineStyle: { color: '#2488c5', width: 1.2 }
        }]
      })
    },

    initSpectrumChart() {
      this.spectrumInstance = echarts.init(this.$refs.spectrumChart)
      this.spectrumInstance.setOption({
        animation: false,
        grid: { left: 62, right: 24, top: 22, bottom: 48 },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'value', name: '频点', min: 0, max: 511,
          nameLocation: 'middle', nameGap: 30,
          axisLabel: { color: '#555' }
        },
        yAxis: {
          type: 'value', name: '幅值',
          axisLabel: { color: '#555' },
          splitLine: { lineStyle: { color: '#e7e7e7' } }
        },
        series: [{
          type: 'line', showSymbol: false,
          data: this.spectrumData,
          areaStyle: { color: 'rgba(36, 136, 197, 0.14)' },
          lineStyle: { color: '#2488c5', width: 1.2 },
          markLine: {
            silent: true,
            symbol: 'none',
            data: [{ xAxis: this.signalMetrics.dominantBin, name: '最大谱线' }],
            lineStyle: { color: '#d95f59', type: 'dashed' },
            label: { formatter: '最大谱线：频点 {c}', color: '#8b2f2b' }
          }
        }]
      })
    },

    disposeCharts() {
      for (const key of ['diagnosisInstance', 'waveformInstance', 'spectrumInstance']) {
        if (this[key]) {
          this[key].dispose()
          this[key] = null
        }
      }
    },

    formatNumber(value, digits = 6) {
      const numeric = Number(value)
      return Number.isFinite(numeric) ? numeric.toFixed(digits) : '-'
    },

    formatTime(value) {
      if (!value) return '-'
      const normalized = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(String(value))
        ? String(value).replace(' ', 'T')
        : value
      const date = new Date(normalized)
      if (Number.isNaN(date.getTime())) return String(value)
      return date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
    },

    async handleExport() {
      try {
        const doc = new jsPDF('p', 'mm', 'a4')
        const pages = [this.$refs.coverPage, this.$refs.contentPage1, this.$refs.contentPage2, this.$refs.contentPage3]
        for (let index = 0; index < pages.length; index += 1) {
          if (index > 0) doc.addPage()
          const canvas = await html2canvas(pages[index], {
            scale: 2,
            backgroundColor: '#ffffff',
            useCORS: true
          })
          doc.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, 210, 297)
        }
        doc.save(`电梯故障诊断报告-${this.formattedDate.replace(/[/:]/g, '-')}.pdf`)
      } catch (error) {
        console.error('导出诊断报告失败', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.scroll-content {
  width: 100%;
  background: #e9eef2;
}

.report-page {
  width: 210mm;
  height: 297mm;
  margin: 0 auto 20px;
  box-sizing: border-box;
  background: #fff;
  color: #1f2933;
  overflow: hidden;
}

.cover-page {
  padding: 28mm 20mm;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  .logo-container {
    margin-top: 10mm;
    margin-bottom: 24mm;
  }

  img {
    width: 52mm;
    height: auto;
  }

  h1 {
    margin: 0 0 14mm;
    font-size: 32px;
    letter-spacing: 0;
  }

  h2, h3 {
    margin: 4mm 0;
    font-size: 22px;
    letter-spacing: 0;
  }
}

.cover-meta {
  margin-top: auto;
  margin-bottom: 18mm;
  font-size: 16px;
  line-height: 1.8;
}

.content-page {
  padding: 14mm 16mm;

  h1 {
    margin: 0 0 5mm;
    font-size: 25px;
    letter-spacing: 0;
  }

  h2 {
    margin: 6mm 0 3mm;
    font-size: 19px;
    letter-spacing: 0;
  }
}

.report-table {
  width: 100%;
  margin-bottom: 7mm;
  border-collapse: collapse;
  table-layout: fixed;

  th, td {
    border: 1px solid #aab7c0;
    padding: 8px;
    text-align: center;
    font-family: 'SimSun', serif;
    font-size: 14px;
    line-height: 1.45;
    word-break: break-word;
  }

  th {
    background: #edf3f6;
    color: #172b3a;
    font-weight: 600;
  }
}

.source-table th {
  width: 16%;
}

.source-table td {
  width: 34%;
}

.metric-table td:nth-child(odd) {
  background: #f7f9fa;
  font-weight: 600;
}

.paragraph, .note {
  margin: 2mm 0 4mm;
  font-family: 'SimSun', serif;
  font-size: 16px;
  line-height: 1.75;
  text-align: justify;
  text-indent: 2em;
}

.note {
  padding: 3mm 4mm;
  border-left: 3px solid #5b8fa8;
  background: #f4f8fa;
  font-size: 14px;
  text-indent: 0;
}

.warning-text {
  color: #9a3f35;
}

.chart-container {
  width: 100%;
  margin: 0;
}

.diagnosis-chart {
  height: 112mm;
}

.waveform-chart {
  height: 112mm;
}

.spectrum-chart {
  height: 108mm;
}

.graph-title {
  margin: 1mm 0 4mm;
  font-family: 'SimSun', serif;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  text-align: center;
}

.data-unavailable {
  height: 90mm;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #b8c3ca;
  color: #9a3f35;
  font-size: 16px;
}

.no-suggestion {
  color: #526978;
  text-align: center;
}
</style>
