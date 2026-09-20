<template>
  <div class="stateChart">
    <div class="table-title">
      <span>故障诊断结果</span>
    </div>
    <div id="DiagnosisChart" class="chart"></div>
  </div>
</template>

<script >
import * as echarts from "echarts";


export default {
 data() {
   return {
     turbines: [
       {name: "风机#1", turbineCode: "21-DEW-G4000-155-WT1", status: "unaccessible"},
       {name: "风机#2", turbineCode: "21-DEW-G4000-155-WT2", status: "unaccessible"},
       {name: "风机#3", turbineCode: "21-DEW-G4000-155-WT4", status: "normal"},
       {name: "风机#4", turbineCode: "21-DEW-G4000-155-WT5", status: "normal"},
       {name: "风机#5", turbineCode: "21-DEW-G4000-155-WT6", status: "normal"},
       {name: "风机#6", turbineCode: "21-DEW-G4000-155-WT7", status: "normal"},
       {name: "风机#7", turbineCode: "21-DEW-G4000-155-WT8", status: "normal"},
       {name: "风机#8", turbineCode: "21-DEW-G4000-155-WT9", status: "normal"},
       {name: "风机#9", turbineCode: "21-DEW-G4000-155-WT10", status: "normal"},
       {name: "风机#10", turbineCode: "21-DEW-G4000-155-WT11", status: "normal"}
     ]
   }
 },
mounted() {
   this.loadChart()
},
  methods:{
    loadChart () {
      const chart = echarts.init(document.getElementById('DiagnosisChart'));

      //获取统计图的数据
      // 分别计算三种状态下的数据
      let normalCount = 0
      let abnormalCount = 0
      let unaccessCount = 0
      let chartData = []

      this.turbines.forEach(item => {
        switch (item.status) {
          case "normal":
            normalCount++
            break;
          case "abnormal":
            abnormalCount++
            break;
          case "unaccessible":
            unaccessCount++
            break;
        }
      })
      chartData.push(
          {value: unaccessCount, name: "齿轮箱轴承", itemStyle: {color: "rgb(58, 151, 248)"}},
          {value: normalCount, name: "发电机前轴承", itemStyle: {color: "#00cc99"}},
          {value: abnormalCount, name: "发电机后轴承", itemStyle: {color: "#fd626e"}},
      )
      chart.setOption({
        title: [
          {
            // text: "风场下风机状态饼状图", // **主标题**
            left: "center",
            top: "0%", // **让标题位于图表上方**
            textStyle: {
              fontSize: 18,
              color: "#000",
            },
          },
          {
            text: '故障类型', // 文字内容
            left: "48%",
            top: "42%", // 调整垂直位置
            textAlign: "center",
            textStyle: {
              fontSize: 16,
              fontWeight: "bold",
              color: "#000",
              rich: {
                count: {
                  fontSize: 18, // **单独设置风机数量大小**
                  fontWeight: "bold",
                  color: "#FFA500",
                  padding: [0, 0, 10, 0]
                },
              },
            },
          }
        ],
        tooltip: {trigger: "item"},
        legend: {
          orient: "horizontal",
          bottom: "0%",
          left: "center", // **让图例水平居中**
          textStyle: {color: "#000"},
        },
        series: [
          {
            type: "pie",
            radius: ["40%", "60%"],
            center: ["50%", "50%"],
            label: {
              show: true,
              color: "#000",
              fontSize: 12,
            },
            data: chartData
          },
        ],
      })
    }
  }
}


</script>

<style scoped lang="scss">
.stateChart {
  width: 100%;
  height: 100%;

  span {
    display: block;
    padding: 0;
    width: 120px;
    height: 41px;
    font-size: 16px;
    text-align: center; /*文字水平居中*/
    line-height: 41px; /*文字垂直居中*/
  }
  .table-title {
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
.chart {
  width: 100%;  /* 让 ECharts 占满父容器 */
  height: 80%; /* 确保有高度，不受限 */
  min-width: 300px; /* 设最小宽度 */
  min-height: 300px; /* 设最小高度 */
  z-index: 0;
  background-color: rgba(255, 255, 255, 0.5); /* 设置背景颜色来帮助调试 */
}

</style>
