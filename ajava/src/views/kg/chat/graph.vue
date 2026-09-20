<template>
  <div>
    <div ref="myChart" style="width: 380px; height: 380px; margin-left: 40px; margin-top: 0px"></div>
    <div id="legend" style="margin-left: 30px; margin-top: 10px; padding-top: 20px;"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default
{
  props: {
    flag: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      myChart: null // 声明一个变量来存储 ECharts 实例
    };
  },
  mounted() {
    console.log(this.flag, typeof this.flag);
    console.log(this.flag.event_count);
    if (!this.myChart) {
      this.myChart = echarts.init(this.$refs.myChart); // 使用 ref 来初始化 ECharts 实例
    }
    // 解析 neighbor_typeinstance 对象，并将其转换为适用于图表的数据格式
    const data = Object.keys(this.flag.neighbor_typeinstance).map((key) => ({
      name: this.flag.neighbor_typeinstance[key].split(':')[0], // 使用属性名称作为分类名称
      value: parseInt(this.flag.neighbor_typeinstance[key].split(':')[1], 10) // 解析数值部分
    }));
    // 从 this.flag.count 中获取总数
    const totalCount = this.flag.event_count;
    // 使用 flag 中的数据设置图表标题和数据
    let option = {
      title: {
        text: `${this.flag.event}在${this.flag.neighbor_type}方面的分布情况如下：`,
        subtext: `总数: ${totalCount}`,
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c} ({d}%)'
      },
      series: [
        {
          name: this.flag.event,
          type: 'pie',
          radius: '55%',
          data: data,
          label: {
            formatter: '{b}: {c} ({d}%)' // 显示名称、数值和百分比
          }
        }
      ],
      legend: {
        orient: 'horizontal', // 设置图例的方向为水平
        bottom: '0',
      }
    };
    this.myChart.setOption(option);
  },
  beforeDestroy() {
    // 在组件销毁前销毁 ECharts 实例
    if (this.myChart) {
      this.myChart.dispose();
    }
  }
};
</script>
