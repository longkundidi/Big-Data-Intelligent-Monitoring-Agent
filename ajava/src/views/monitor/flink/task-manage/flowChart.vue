<template>
    <VueFlow :nodes="nodes" :edges="edges" style="height: 15vh; width:100%">
    </VueFlow>
</template>


<script setup>
import { ref,watch } from 'vue';
import { VueFlow } from '@vue-flow/core';

// 接收来自父组件的 props
let props = defineProps(['initialNodeLabel'])

let nodes = ref([
  // an input node, specified by using `type: 'input'`
  {
    id: "1",
    type: "input",
    position: { x: 80, y:25 },
    sourcePosition: 'right', // 设置源连接点位置为右侧
    targetPosition: 'left',  // 设置目标连接点位置为左侧
    // all nodes can have a data object containing any data you want to pass to the node
    // a label can property can be used for default nodes
    data:{label: '数据来自topic:'+props.initialNodeLabel.node1}
  },

  // default node, you can omit `type: 'default'` as it's the fallback type
  {
    id: "2",
    position: { x: 330, y: 25 },
    sourcePosition: 'right', // 设置源连接点位置为右侧
    targetPosition: 'left',  // 设置目标连接点位置为左侧
    data:{label:'执行算法:'+props.initialNodeLabel.node2}
  },
  // default node, you can omit `type: 'default'` as it's the fallback type
  {
    id: "3",
    type:"output",
    position: { x: 580, y: 25 },
    sourcePosition: 'right', // 设置源连接点位置为右侧
    targetPosition: 'left',  // 设置目标连接点位置为左侧
    data:{label:'数据写入topic:'+props.initialNodeLabel.node3}
  },
]);

let edges = ref([
  // set `animated: true` to create an animated edge path
  {
    id: "e1->2",
    source: "1",
    target: "2",
    type: 'straight', // 新创建的边也使用直
    label:'流入',
    markerEnd: 'arrowclosed', // 新创建的边也添加闭合箭头
    animated: true,
  },
  {
    id: "e2->3",
    source: "2",
    target: "3",
    type: 'straight', // 新创建的边也使用直线
    label:'流入',
    markerEnd: 'arrowclosed', // 新创建的边也添加闭合箭头
    animated: true,
  },

]);

// 假设有一个方法来更新 initialNodeLabels
function updateInitialNodeLabels(newLabels) {
  props.initialNodeLabels = newLabels;
}


// 监听 props 变化并更新节点的 label
watch(
    () => props.initialNodeLabel,
    (newLabels) => {
      updateInitialNodeLabels(newLabels);

      nodes.value.forEach(node => {
        if (newLabels[`node${node.id}`]) {
          console.log(node.id)
          if(node.id==1){
            node.data.label = '数据来自topic:'+newLabels[`node${node.id}`];
          }else if(node.id==3){
            node.data.label = '数据写入topic:'+newLabels[`node${node.id}`];
          }else{
            node.data.label =newLabels[`node${node.id}`];
          }

        }
      });
    },
    { immediate: true } // 立即执行一次以初始化
);
</script>

<style>
/* import the necessary styles for Vue Flow to work */
@import "@vue-flow/core/dist/style.css";

/* import the default theme, this is optional but generally recommended */
@import "@vue-flow/core/dist/theme-default.css";



</style>
