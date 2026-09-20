<template>
  <div ref="chart" :style="{ width: '100%', height: '400px' }"></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  props: {
    chartOptions: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chartInstance: null
    };
  },
  mounted() {
    this.initChart();
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },
  methods: {
    initChart() {
      this.chartInstance = echarts.init(this.$refs.chart);
      this.updateChart();
    },
    updateChart() {
      if (this.chartInstance) {
        this.chartInstance.setOption(this.chartOptions);
      }
    }
  },
  watch: {
    chartOptions: {
      handler(newOptions) {
        this.updateChart();
      },
      deep: true
    }
  }
};
</script>

<style scoped>

</style>