<template>
  <el-container style="position:relative;">
    <el-aside width="500px" style="margin-right: 15px;">
      <div class="mytree">
        <Tree
            :treeData="treeData"
            @clickNode="handleNodeClick"
            @clearInfo="clearInfo"
            @loadTreeData="getDataList"
            @createAlConfig="createAlConfig"
        >
        </Tree>
      </div>
    </el-aside>
    <el-main>
      <Info ref="refInfo"
            v-if="isActivate"
            @loadTreeData="getDataList"
            @clearInfo="clearInfo"
            :nodeData="currentNode"></Info>
    </el-main>
  </el-container>

</template>

<script setup>
import {ref, onMounted, computed} from 'vue';
import {useRouter} from 'vue-router';
import {
  getAlConfig,
} from "@/api/al/taskConfig";
import Tree from "./AlTreeview.vue"
import Info from "./Info.vue"
import {ElMessage} from "element-plus";

const tableData = ref([]);
const isActivate = ref(false);
const refInfo = ref(null);
const currentNode = ref(null);

const router = useRouter();

const getDataList = () => {
  //获取所有组态
  getAlConfig().then(res => {
    if (res.data.code === '200') {
      tableData.value = res.data.data.records;
    }
  });
};

const createAlConfig = () => {
  router.push({
    path: '/al/taskConfig/al/algorithmConfig',
  }).catch(() => {
    ElMessage.error('页面未找到');
  });
};

const treeData = computed(() => {
  const map = new Map();
  for (const item of tableData.value) {
    if (!map.has(item.modelType)) {
      map.set(item.modelType, []);
    }
    map.get(item.modelType).push({
      label: item.modelName,
      rawData: item, // 如果需要点击获取原始数据，保留引用
      level: 2
    });
  }
  return [...map.entries()].map(([modelType, children]) => ({
    label: modelType,
    children,
    level: 1
  }));
});

const handleNodeClick = (node) => {
  if (node.level > 1) {
    currentNode.value = node.rawData
    isActivate.value = true
  }
};

const clearInfo = () => {
  isActivate.value = false
  if (refInfo.value) {
    refInfo.value.clear();
  }
}

onMounted(() => {
  getDataList();
});

</script>

<style scoped>
.el-aside, .el-main {
  height: 100vh;
  background-color: white;
}

el-main {
  display: flex;
  justify-content: center;
  align-items: center;
}

.mytree {
  height: 100%;
  flex-direction: column;
  overflow-y: auto;
  display: flex;
  align-items: center;
}

</style>
