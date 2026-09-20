<template>
  <el-steps :active="active" align-center>
    <el-step title="Step 1" description="上传文件" />
    <el-step title="Step 2" description="查看抽取结果" />
    <el-step title="Step 4" description="结果审核" />
    <el-step title="Step 5" description="知识入库" />
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
