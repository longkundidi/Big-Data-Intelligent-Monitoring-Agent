<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ points: Array<{ time?: string; value?: number }> }>()
const values = computed(() => props.points.map((point) => Number(point.value || 0)))
const max = computed(() => Math.max(...values.value, 1))
const coordinates = computed(() => values.value.map((value, index) => {
  const x = values.value.length < 2 ? 12 : 12 + (index * 276) / (values.value.length - 1)
  const y = 116 - (value / max.value) * 92
  return `${x},${y}`
}))
</script>

<template>
  <div class="mini-chart">
    <svg viewBox="0 0 300 130" role="img" aria-label="指标趋势图">
      <path d="M12 24H288M12 70H288M12 116H288" class="chart-grid" />
      <polyline v-if="coordinates.length" :points="coordinates.join(' ')" class="chart-line" />
      <circle v-for="point in coordinates" :key="point" :cx="point.split(',')[0]" :cy="point.split(',')[1]" r="3.5" class="chart-point" />
    </svg>
    <div class="chart-axis"><span>{{ points[0]?.time || '最近采样' }}</span><span>{{ points.at(-1)?.time || '' }}</span></div>
  </div>
</template>
