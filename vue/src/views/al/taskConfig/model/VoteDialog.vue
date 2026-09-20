<template>
  <el-dialog :model-value="visible" title="查看投票结果" width="600px" @close="handleClose">
    <div>
      <el-radio-group v-model="selectedRule">
        <template v-if="modelType === 'diagnosis'">
          <el-radio value="soft_vote">
            <el-icon><Document /></el-icon>
            软投票
            <el-tooltip content="将各模型对每类的概率累加，取总概率最大的类别作为最终投票结果" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-radio>
          <el-radio value="hard_vote">
            <el-icon><DocumentChecked /></el-icon>
            硬投票
            <el-tooltip content="取所有模型预测的类别索引，出现次数最多的作为最终投票结果，平票时转为软投票" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-radio>
        </template>

        <template v-else-if="modelType === 'anomaly'">
          <el-radio value="soft_vote">
            <el-icon><PieChart /></el-icon>
            软投票
            <el-tooltip content="取所有异常检测结果的 重构误差/阈值 之和与平均阈值比较决定是否异常" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-radio>
          <el-radio value="hard_vote">
            <el-icon><Histogram /></el-icon>
            硬投票
            <el-tooltip content="统计每个模型判断的正常/异常索引数量，平票时转为软投票" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-radio>
        </template>
      </el-radio-group>
    </div>

    <div class="pie-chart" v-if="selectedRule">
      <div ref="chartRef" style="width:100%; height:400px;" />
    </div>

    <template #footer>
      <div class="dialog-footer" style="text-align: center">
        <el-button class="editWorktBtn" @click="exportToExcel" :loading="exportLoading">
          导出投票日志
        </el-button>
        <el-button class="disMissBtn" @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { PieChart, DocumentChecked, Document, QuestionFilled, Histogram } from '@element-plus/icons-vue';
import * as echarts from 'echarts/core';
import { use } from 'echarts/core';
import { PieChart as PieChartComponent } from 'echarts/charts';
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import * as XLSX from "xlsx";
import {ElMessage} from "element-plus";

use([PieChartComponent, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const props = defineProps({
  visible: Boolean,
  modelResults: Array,
  votingResults: Array,
  selectedRule: { type: String, default: '' }
});

const emit = defineEmits([
  'update:visible',
  'update:selectedRule',
]);

const selectedRule = ref(props.selectedRule);
const modelType = computed(() => props.modelResults[0]?.type);

const chartRef = ref(null);
let chartInstance = null;
const exportLoading = ref(false);

const exportToExcel = () => {
  exportLoading.value = true;

  try {
    const models = props.modelResults;
    const votingResults = props.votingResults;
    const sampleCount = votingResults.length;

    const headers = ['index'];
    const headerMap = {};

    // 添加模型字段
    models.forEach(model => {
      const prefix = model.name;
      headers.push(`${prefix}_result`);
      headerMap[`${prefix}_result`] = `${prefix}_结果`;

      if (model.type === 'diagnosis') {
        const labels = Object.keys(model.outputs[0].logics);
        labels.forEach(label => {
          const key = `${prefix}_${label}`;
          headers.push(key);
          headerMap[key] = `${prefix}_${label}`;
        });
      } else if (model.type === 'anomaly') {
        headers.push(`${prefix}_mse`);
        headers.push(`${prefix}_threshold`);
        headerMap[`${prefix}_mse`] = `${prefix}_重构误差`;
        headerMap[`${prefix}_threshold`] = `${prefix}_阈值`;
      }
    });

    // 添加当前选择的投票规则字段
    headers.push('soft_vote', 'hard_vote', 'note');
    headerMap['soft_vote'] = '软投票结果';
    headerMap['hard_vote'] = '硬投票结果';
    headerMap['note'] = '备注';

    const rows = [];

    for (let i = 0; i < sampleCount; i++) {
      const row = { index: i };

      models.forEach(model => {
        const output = model.outputs[i];
        const prefix = model.name;
        row[`${prefix}_result`] = output.result;

        if (model.type === 'diagnosis') {
          Object.entries(output.logics).forEach(([label, prob]) => {
            row[`${prefix}_${label}`] = prob;
          });
        } else if (model.type === 'anomaly') {
          row[`${prefix}_mse`] = output.mse;
          row[`${prefix}_threshold`] = output.threshold;
        }
      });

      const vote = votingResults[i];
      row['soft_vote'] = vote.soft_vote;
      row['hard_vote'] = vote.hard_vote;
      row['note'] = vote.note || '';

      rows.push(row);
    }

    // 转换 header -> 中文列头
    const displayHeaders = headers.map(h => headerMap[h] || h);

    const worksheet = XLSX.utils.json_to_sheet(rows, { header: headers });
    XLSX.utils.sheet_add_aoa(worksheet, [displayHeaders], { origin: 'A1' });

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "投票日志");

    const fileName = `投票日志_${new Date().toISOString().slice(0, 10)}.xlsx`;
    XLSX.writeFile(workbook, fileName);

    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error(`导出失败: ${error.message}`);
  } finally {
    exportLoading.value = false;
  }
};

// 准备导出数据

watch(() => props.visible, val => {
  if (!val) {
    selectedRule.value = '';
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
  }
});

watch(selectedRule, async (val) => {
  await nextTick();
  if (!chartRef.value || !props.visible) return;

  emit('update:selectedRule', val);

  if (chartInstance) {
    chartInstance.dispose();
  }

  // 创建图表实例
  chartInstance = echarts.init(chartRef.value);

  const countMap = {};
  props.votingResults.forEach(result => {
    const key = result[selectedRule.value];
    countMap[key] = (countMap[key] || 0) + 1;
  });

  const option = {
    title: { text: '投票结果分布', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '投票结果',
        type: 'pie',
        radius: '50%',
        data: Object.entries(countMap).map(([name, value]) => ({ name, value })),
        label: {
          show: true,
          formatter: '{b}: {d}%', // 显示名称: 数值 (百分比)
          fontSize: 14
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
      }
    ],
    grid: {
      top: 50,
      right: 20,
      bottom: 10,
      left: 50
    },
  };

  chartInstance.setOption(option);
});

const handleClose = () => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }

  emit('update:visible', false);
}
</script>

<style scoped>
.pie-chart {
  margin-top: 20px;
}
</style>