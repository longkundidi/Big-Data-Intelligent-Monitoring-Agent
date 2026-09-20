
<script setup>
import {Handle} from "@vue-flow/core";
import {CircleClose} from "@element-plus/icons-vue";

const props = defineProps({
  label: String,
  configUrl: String,
  shortName: String,
  nodeContext: Object,
  pretreatment: String,
  trainResult: String,
  showClose: Boolean,
  waveMode: String,
  waveLet: String,
  maxLevel: Number,
  sliceLength: Number,
  n: Number
});
const emit = defineEmits(['nodeClicked', 'closeNode']);  // 定义事件

// 点击事件
const handleClick = () => {
  emit('nodeClicked', props.nodeContext); // 传递节点的label
};

const handleClose = (e) => {
  e.stopPropagation(); // 防止触发 node click
  emit('closeNode', props.nodeContext);
};
</script>
<template>
  <Handle
      type="target"
      position="left"
      :style="{ width: '1px', height: '1px' }"
  />

  <div class="node" @click="handleClick">
    {{ label }}
  </div>

  <Handle
      type="source"
      position="right"
      :style="{ width: '1px', height: '1px' }"
  />

  <div
      v-if="props.showClose"
      class="close-wrapper"
      @click.stop="handleClose"
  >
    <el-icon class="handle-icon">
      <CircleClose />
    </el-icon>
  </div>
</template>


<style scoped>
.node {
  border: 1px solid rgba(0, 0, 255, 0.6);
  width: 100px;
  text-align: center;
  background-color: white;
  border-radius: 3px;
  font-size: 15px;  /* 设置字体大小 */
}

.close-wrapper {
  position: absolute;
  right: -20px;  /* 根据连接点偏移 */
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  cursor: pointer;
}

.handle-icon {
  font-size: 15px;
  color: rgba(0, 0, 255, 0.6);
  cursor: pointer;
}
</style>
