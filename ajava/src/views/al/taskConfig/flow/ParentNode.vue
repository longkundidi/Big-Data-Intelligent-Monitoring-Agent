<script setup>
import {Handle} from "@vue-flow/core";

const props = defineProps({
  label: String,
  // 移除childrenCount，改用uniformHeight
  uniformHeight: {
    type: String,
    default: '100px'
  }
})
</script>

<template>
  <Handle
      type="source"
      position="right"
      :style="{
        width: '0px',
        height: '0px',
        visibility: 'hidden',  // 完全隐藏
        position: 'absolute',  // 使其脱离文档流，不占空间
        pointerEvents: 'none'  // 禁止与此元素的交互
      }"
  />
  <div
      :style="{
      border: '1px solid rgba(0, 0, 0, 0.5)',
      width: '150px',
      height: uniformHeight,  // 直接使用统一高度
      transition: 'height 0.3s ease',
      overflow: 'hidden',
      borderLeft: label === '数据源' ? '1px solid rgba(0, 0, 0, 0.5)' : 'none',  // 根据label判断是否显示左边框
    }"
  >
    <div style="border-bottom: 1px solid rgba(0, 0, 0, 0.5); width: 150px; text-align: center">
      {{ label }}
    </div>
  </div>
  <Handle
      type="target"
      position="right"
      :connectable="false"
      :style="{
        width: '0px',
        height: '0px',
        visibility: 'hidden',  // 完全隐藏
        position: 'absolute',  // 使其脱离文档流，不占空间
        pointerEvents: 'none'  // 禁止与此元素的交互
      }"
  />
</template>
