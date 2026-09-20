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
              color: "#e8fbff",
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
              color: "#e8fbff",
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
          textStyle: {color: "rgba(232,251,255,0.78)"},
        },
        series: [
          {
            type: "pie",
            radius: ["40%", "60%"],
            center: ["50%", "50%"],
            label: {
              show: true,
              color: "#e8fbff",
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
  display: flex;
  flex-direction: column;
  min-height: 0;

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
.chart {
  width: 100%;  /* 让 ECharts 占满父容器 */
  flex: 1;
  min-height: 0;
  min-width: 0;
  z-index: 0;
  background-color: transparent;
}

</style>
