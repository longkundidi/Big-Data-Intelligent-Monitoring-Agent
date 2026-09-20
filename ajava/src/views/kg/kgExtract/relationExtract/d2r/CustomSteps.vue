<template>
  <el-steps :active="active" align-center>
    <el-step title="Step 1" description="连接数据库" />
    <el-step title="Step 2" description="关系表与实体映射" />
    <el-step title="Step 3" description="关系知识数据抽取" />
  </el-steps>
</template>

<script setup>

import {toRefs, defineProps, ref,} from 'vue'
const props = defineProps({
  //子组件接收父组件传递过来的值
  activeChange: Number,
})
let active = 2;
const refProps =toRefs(props);
active=refProps.activeChange;
</script>

<style lang="scss" scoped>
.el-step{
  font-weight: bold;
  :deep .el-step__description{
    font-weight: bold;
  }
}
</style>
