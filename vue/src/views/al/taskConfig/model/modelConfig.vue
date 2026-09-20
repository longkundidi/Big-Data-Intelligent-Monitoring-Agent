<template>
  <div style="display: flex; height: 100%;background:#ffffff;">
    <!-- 左侧树形结构 -->
    <div class="mytree">
      <Tree
          :treeData="treeData"
          @clickNode="handleNodeClick"
          @addShow="updateShow"
          @loadTreeData="loadTreeData"
      />
    </div>
    <!-- 中部泳道 -->
    <div class="vueflow-container">
      <!--按钮区域   -->
      <div class="button-container">
        <button class="reSetBtn" @click="resetView">重置</button>
        <button class="editWorktBtn" @click="running">组态运行</button>
        <el-tooltip class="item" effect="dark" content="暂无结果可查看，请先运行组态">
          <el-button v-if="modelResults.length < 2"
                     class="registerBtn custom-disabled">
            投票结果
          </el-button>
        </el-tooltip>
        <el-button v-if="modelResults.length >= 2"
                   class="registerBtn"
                   @click="getVotingResults">
          投票结果
        </el-button>
        <button class="printBtn" @click="saveConfig">组态保存</button>
      </div>
      <!--中间vue-flow区域-->
          <VueFlow
              ref="vueFlowInstance"
              :nodes="nodes"
              :edges="edges"
              class="basic-flow flow-card card"
              :default-viewport="{ zoom: 1.1, x: 50, y: 200 }"
              :min-zoom="0.2"
              :max-zoom="4"
          >
            <Background pattern-color="#aaa" :gap="13" />
            <template #node-parent="p">
              <ParentNode v-bind="p.data" :uniformHeight="p.data.uniformHeight" />
            </template>
            <template #node-child="c">
              <ChildNode v-bind="c.data" :nodeContext="c" @nodeClicked="handleChildNodeClick" @closeNode="deleteNode"/>
            </template>
            <template #node-hidden="h">
              <HiddenNode v-bind="h.data"/>
            </template>
          </VueFlow>
      <!-- 动态图表区域 -->
      <div class="charts-container" v-show="chartVisible" style="position: absolute; bottom: 0; left: 0; right: 0; z-index: 1;">
        <div v-for="(chart, index) in chartData" :key="index" class="chart-item">
          <DynamicChart
              :is-visible="chartVisible"
              :title="chart.title"
              :imageUrl="chart.imageUrl"
              :loading="chart.loading"
              :is-train="chart.isTrain"
              @clear="clear"
          />
        </div>
      </div>
    </div>
    <!--侧边栏选择数据集 -->
    <DataBar ref="dataRef" v-model:visible="dataVisible" :tableData="tableData" :isSCADA="isSCADA" :isTrain="isTrain" @update:modelObject="updateModelObject"/>
    <!-- 保存组态的组件 -->
    <Dialog ref="dialogRef" v-model:dialogVisible="dialogVisible" :nodesImage="nodesImage"/>

    <VotingResultDialog v-model:visible="voteVisible"  v-model:selectedRule="voteRule" :modelResults="modelResults" :votingResults="votingResults"/>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {useStore} from 'vuex';
import {useVueFlow, VueFlow} from '@vue-flow/core'
import {Background} from '@vue-flow/background'
import ParentNode from "../flow/ParentNode.vue";
import ChildNode from "../flow/ChildNode.vue";
import HiddenNode from "../flow/HiddenNode.vue";
import Tree from "../flow/TreeBar.vue"
import DataBar from '../flow/DataBar.vue';
import Dialog from './ModelDialogBar.vue'
import VotingResultDialog from './VoteDialog.vue'
import {getAlConfig, getVariablesByalModelNames, startConfig} from "@/api/al/taskConfig"; // 引入 SideBar 组件
import {ElMessage} from 'element-plus'
import {getObj} from "@/api/al/altest/altest";
import DynamicChart from "../flow/Chart.vue";
import {uploadIcon} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import domtoimage from 'dom-to-image';

// VueFlow 初始化
const {onInit} = useVueFlow()
const vueFlowInstance = ref(null);
const { fitView } = useVueFlow();

const parentNodes = ref([])
const nodes = ref([])
const edges = ref([])

// 数据结构
const showIds = ref(['1']);
const labelOrderMap = {
  "数据源": 1,
  "相关性分析": 2,
  "异常值检测": 3,
  "缺失值填充": 4,
  "数据标准化": 5,
  "特征提取": 6,
  "状态评估": 7,
  "故障诊断": 8,
};
const dataIdMap = {
  "状态评估": 'datasource-1',
  "故障诊断": 'datasource-2',
};
// 定义一个缓存对象，key 用唯一标识，比如 nodeData.rawData.code
const flowNodes = ref([]);

// 存储抽屉框的状态和选中的节点数据
const drawerVisible = ref(false);
const tableData = ref([]);
const selectedNodeLabel = ref('');
const isUpload = ref(false);
const isSCADA = ref(false);
const isTrain = ref(false);

const dialogVisible = ref(false);
const nodesImage = ref('')
const alConfigData = ref([]);

const dataRef = ref(null)
const dataVisible = ref(false);
const modelObject = ref({})
const dialogRef = ref(null)

// 初始化 VueFlow
onInit((vueFlowInstance) => {
  vueFlowInstance.fitView()
})

// 数据加载
onMounted(async () => {
  await loadTreeData();
})

const loadTreeData = async () => {
  resetData()
  try {
    //获取所有组态
    getAlConfig().then(res => {
      if (res.data.code === '200') {
        alConfigData.value = res.data.data.records;
      }
    });

  } catch (error) {
    console.error('Error loading tree data:', error);
  }
};

const treeData = computed(() => {
  const map = new Map();
  for (const item of alConfigData.value) {
    if (!map.has(item.modelType)) {
      map.set(item.modelType, []);
    }
    map.get(item.modelType).push({
      label: item.modelName,
      rawData: item,
      level: 2,
      showAdd: false,
      showClose: false,
      showPopover: true
    });
  }

  return [...map.entries()].map(([modelType, children], index) => {
    const parentId = (labelOrderMap[modelType] || Infinity).toString();

    // 给子节点加 parentId
    const childrenWithParentId = children.map(child => ({
      ...child,
      parentId
    }));

    return {
      id: parentId,
      label: modelType,
      children: childrenWithParentId,
      level: 1,
      showAdd: true,
      showClose: false,
      showPopover: false
    };
  }).sort((a, b) => {
    return (labelOrderMap[a.label] || Infinity) - (labelOrderMap[b.label] || Infinity);
  });
});

// 生成 parentNodes
const generateParentNodes = (treeNodes) => {
  let nodeXPosition = 0;
  const nodeWidth = 150;

  // 数据源节点
  const extendedParentNodes = [
    {
      id: '1',
      type: 'parent',
      data: { label: '数据源', uniformHeight: '100px' },
      position: { x: nodeXPosition, y: 0 },
      draggable: false,
      style: { pointerEvents: 'none', zIndex: 0 }
    }
  ];

  const generatedParentNodes = treeNodes.map((item, index) => {
    nodeXPosition = (index + 1) * nodeWidth;
    return {
      id: item.id,
      type: 'parent',
      data: { label: item.label, uniformHeight: '100px' },
      position: { x: nodeXPosition, y: 0 },
      draggable: false,
      style: { pointerEvents: 'none', zIndex: 0 }
    };
  });

  return [...extendedParentNodes, ...generatedParentNodes];

};

watch(treeData, (newTreeData) => {
  if (newTreeData.length === 0) return; // 数据还没准备好就不操作
  parentNodes.value = generateParentNodes(newTreeData);

  // 初始化统一高度
  const initialHeight = getUniformHeight()
  parentNodes.value.forEach(node => {
    node.data.uniformHeight = initialHeight
  })
  nodes.value.splice(0, nodes.value.length, parentNodes.value[0])
  showIds.value = ['1']
}, { immediate: true });

// 当更新显示的节点时，修改 showIds
const updateShow = (id) => {
  return new Promise(resolve => {
    nextTick(() => {
      let idx = showIds.value.indexOf(id)
      if (idx > -1) {
        ElMessage.info("该模型泳道已存在！")
        return
      } else {
        showIds.value.push(id)
        showIds.value.sort()
        nodes.value = nodes.value.filter(node => node.type !== 'parent');
      }

      updateXPosition()
      resolve();
    })
  })
};

// 更新节点横坐标
const updateXPosition = () => {
  parentNodes.value.forEach((node) => {
    let i = showIds.value.indexOf(node.id);
    if (i > -1) {
      // 更新父节点位置
      node.position = { x: 150 * i, y: 0 };
      nodes.value.push(node);

      // 更新该父节点下的所有子节点位置
      nodes.value.forEach((childNode) => {
        if (childNode.parent === node.id) {
          // 让子节点的横坐标和父节点的横坐标一致
          childNode.position = { x: node.position.x + 25, y: childNode.position.y };
        }
      });
    }
  });
}

// 更新节点纵坐标
const updateYPosition = () => {

  const nodeMap = new Map(nodes.value.map(node => [node.id, node]));
  const groupedList = [];

  domainGroups.value.forEach(group => {
    const groupedByParent = new Map();

    group.forEach(id => {
      const node = nodeMap.get(id);
      if (node) {
        const parent = node.parent || 'undefined';
        const nodeCopy = JSON.parse(JSON.stringify(node));

        if (!groupedByParent.has(parent)) groupedByParent.set(parent, []);
        groupedByParent.get(parent).push(nodeCopy);
      }
    });

    groupedList.push(groupedByParent);
  });

  let baseY = 50;
  const yStep = 60;

  groupedList.forEach(groupMap => {
    let maxY = -Infinity;

    groupMap.forEach((nodeList, parentId) => {
      let y = baseY;

      nodeList.forEach((node, index) => {
        const realNode = nodes.value.find(n => n.id === node.id);
        if (realNode) {
          realNode.position = {
            ...realNode.position,
            y: y
          };
          maxY = Math.max(maxY, y);
          y += yStep;
        }
      });
    });

    if (Number.isFinite(maxY)) {
      baseY = maxY + 50;
    }
  });
}

const handleNodeClick = (nodeData) => {
  if (!nodeData.children || nodeData.children.length < 0)
    addNode(nodeData)
};

const updateParentNodes = (processNodes) => {
  const existingLabels = new Set(parentNodes.value.map(node => node.data.label));

  const newNodes = processNodes.filter(pNode => !existingLabels.has(pNode.label))
      .map((pNode) => ({ // 这里要传递 pNode 作为参数
        id: labelOrderMap[pNode.label] ? labelOrderMap[pNode.label].toString() : '',
        type: 'parent',
        data: { label: pNode.label, uniformHeight: '100px' },
        position: { x: 0, y: 0 },
        draggable: false,
        style: { pointerEvents: 'none', zIndex: 0 }
      }));

  parentNodes.value.push(...newNodes);

  parentNodes.value.sort((a, b) => {
    const orderA = labelOrderMap[a.data.label] || Infinity;
    const orderB = labelOrderMap[b.data.label] || Infinity;
    return orderA - orderB;
  });

  const initialHeight = getUniformHeight()
  parentNodes.value.forEach(node => {
    node.data.uniformHeight = initialHeight
  })

  return parentNodes.value;
}

const domainGroups = ref([]);

const deleteNode = (node) => {

  const codePrefix = node.id.split("-").slice(0, 2).join("-");
  let nodeData = null;

  const stack = [...treeData.value];

  while (stack.length > 0) {
    const current = stack.pop();

    if (current.rawData?.code === codePrefix) {
      nodeData = current;
      break;
    }

    if (current.children && current.children.length > 0) {
      stack.push(...current.children);
    }
  }

  if (nodeData) {
    const process = nodeData.rawData.taskProcess

    process.forEach(item => {
      const currentParent = nodes.value.find(node => node.data.label === item.almodelType);
      let currentChildIndex = nodes.value.findIndex(node => node.data.label === item.almodelName && node.group.includes(codePrefix))

      if (currentChildIndex > -1 && nodes.value[currentChildIndex].group.length === 1) {
        edges.value = edges.value.filter(edge => edge.source !== nodes.value[currentChildIndex].id && edge.target !== nodes.value[currentChildIndex].id);
        nodes.value.splice(currentChildIndex, 1)
      }
      else if (currentChildIndex > -1 && nodes.value[currentChildIndex].group.length > 1)
        nodes.value[currentChildIndex].group = nodes.value[currentChildIndex].group.filter(item => item !== nodeData.rawData.code)

      if (currentParent.data.label === '状态评估') {
        let scadaIndex = nodes.value.findIndex(node => node.data.label === 'SCADA数据')
        if (scadaIndex > -1 && nodes.value[scadaIndex].group.length === 1) {
          edges.value = edges.value.filter(edge => edge.source !== nodes.value[scadaIndex].id && edge.target !== nodes.value[scadaIndex].id);
          nodes.value.splice(scadaIndex, 1)
        }else if (scadaIndex > -1 && nodes.value[scadaIndex].group.length > 1)
          nodes.value[scadaIndex].group = nodes.value[scadaIndex].group.filter(item => item !== nodeData.rawData.code)
      } else if (currentParent.data.label === '故障诊断') {
        let cmsIndex = nodes.value.findIndex(node => node.data.label === 'CMS数据')
        if (cmsIndex > -1 && nodes.value[cmsIndex].group.length === 1) {
          edges.value = edges.value.filter(edge => edge.source !== nodes.value[cmsIndex].id && edge.target !== nodes.value[cmsIndex].id);
          nodes.value.splice(cmsIndex, 1)
        }else if (cmsIndex > -1 && nodes.value[cmsIndex].group.length > 1)
          nodes.value[cmsIndex].group = nodes.value[cmsIndex].group.filter(item => item !== nodeData.rawData.code)
      }

      let result = getLastChild(currentParent.id)
      if (result.count === 0) {
        nodes.value = nodes.value.filter(node => node.id !== currentParent.id);
        showIds.value = showIds.value.filter(id => id !== currentParent.id);
      }
    })
    flowNodes.value = flowNodes.value.filter(item => item !== nodeData.label);

    nodes.value = nodes.value.filter(node => node.type !== 'parent');
    updateXPosition()
    updateYPosition()
  } else
    ElMessage.error("删除组态出错！")

}

const addNode = async (nodeData) => {

  const process = nodeData.rawData.taskProcess

  if (flowNodes.value.includes(nodeData.label)) {  //已经存在当前的node列表，就删去
    ElMessage.warning("该组态已存在！")
    return
  } else {  //否则，添加
    const processNodes = Array.from(new Set(process.map(item => item.almodelType)))
        .map(type => ({ label: type }));
    parentNodes.value = updateParentNodes(processNodes);

    const existingSet = new Set(showIds.value);
    const newIds = processNodes
        .map(node => labelOrderMap[node.label])
        .filter(id => id !== undefined && !existingSet.has(id.toString()))
        .map(id => id.toString())
        .sort((a, b) => Number(a) - Number(b));
    for (const id of newIds)
      await updateShow(id)

    process.forEach((item, index) => {
      const isLast = index === process.length - 1;
      const currentParentNode = nodes.value.find(node => node.data.label === item.almodelType);
      addChildNode(currentParentNode, item, nodeData.rawData, isLast);
    });

    //  自动添加数据源组件
    addDataNode(nodeData.rawData)
    flowNodes.value.push(nodeData.label);

    addEdge()
    nodes.value = nodes.value.filter(node => node.type !== 'parent');
    updateXPosition()
    updateYPosition()

  }

  // VueFlow 重新渲染
  await nextTick(() => {
    const uniformHeight = getUniformHeight();
    nodes.value = nodes.value.map(node => {
      if (node.type === 'parent') {
        return {
          ...node,
          data: {
            ...node.data,
            uniformHeight: uniformHeight
          }
        };
      }
      return node;
    });
  });
}

const addDataNode = (config) => {
  //  自动添加数据源组件
  if (config.modelType === '状态评估') {
    if (!isNodeExist('SCADA数据')) {  // 判断是否已经存在
      nodes.value.push({
        id: dataIdMap['状态评估'],
        type: 'child',
        data: {label: 'SCADA数据'},
        parent: '1',
        position: {x: 25, y: 0},
        draggable: true,
        group: [config.code]
      });
    }
    else {
      let index = nodes.value.findIndex(node => node.data.label === 'SCADA数据')
      nodes.value[index].group.push(config.code)
    }
  }else if(config.modelType === '故障诊断'){
    if (!isNodeExist('CMS数据')) {  // 判断是否已经存在
      nodes.value.push({
        id: dataIdMap['故障诊断'],
        type: 'child',
        data: {label: 'CMS数据'},
        parent: '1',
        position: {x: 25, y: 0},
        draggable: true,
        group: [config.code]
      });
    }
    else {
      let index = nodes.value.findIndex(node => node.data.label === 'CMS数据')
      nodes.value[index].group.push(config.code)
    }
  }
}

const addChildNode = (parentNode, child, config, isLast) => {
  let x_interval = 25;
  let newNodePosition = { x: parentNode.position.x + x_interval, y: 0 };

  let exitNode = nodes.value.findIndex(node => node.data.label === child.almodelName);

  // 初始化 data 对象
  const nodeData = {
    label: child.almodelName,
    configUrl: child.configUrl,
    shortName: child.almodelShortName,
    showClose: isLast
  };

  // 如果是小波变换，附加小波参数
  if (config.pretreatment === '小波变换') {
    nodeData.waveMode = config.waveMode;
    nodeData.waveLet = config.waveLet;
    nodeData.maxLevel = config.maxLevel;
    nodeData.sliceLength = config.sliceLength;
    nodeData.n = config.n;
  }

  if (exitNode === -1 || isLast) {
    // 添加新子节点
    nodes.value.push({
      id: config.code + '-' + child.sequence,
      type: 'child',
      data: nodeData,
      parent: parentNode.id,
      position: newNodePosition,
      draggable: true,
      group: [config.code]
    });
  } else {
    nodes.value[exitNode].group.push(config.code);
  }
};

const getVotingResults = () => {
  voteVisible.value = true

  const modelType = modelResults.value[0]?.type;
  let result = [];

  if (modelType === 'diagnosis') {
    result = getDiagnosisVotingResults(modelResults.value);
  } else if (modelType === 'anomaly') {
    result = getAnomalyVotingResults(modelResults.value);
  }
  votingResults.value = result;
}

const getAnomalyVotingResults = (data) => {
  const sampleCount = data[0].outputs.length;
  const results = [];

  for (let i = 0; i < sampleCount; i++) {
    let voteCount = { 0: 0, 1: 0 }; // 正常:0, 异常:1
    let ratios = [];
    let thresholds = [];

    data.forEach(model => {
      const output = model.outputs[i];
      const resultIndex = output.result === '异常' ? 1 : 0;
      voteCount[resultIndex]++;
      thresholds.push(output.threshold);
      if (resultIndex === 1) {
        ratios.push(output.mse / output.threshold);
      }
    });

    // === 规则1：重构误差与阈值判断 ===
    const sumRatios = ratios.reduce((a, b) => a + b, 0);
    const avgThreshold = thresholds.reduce((a, b) => a + b, 0) / thresholds.length;
    const soft_vote = sumRatios > avgThreshold ? '异常' : '正常';

    // === 规则2：逻辑投票判断 ===
    let hard_vote = '';
    let note = '';

    if (voteCount[0] > voteCount[1]) {
      hard_vote = '正常';
    } else if (voteCount[1] > voteCount[0]) {
      hard_vote = '异常';
    } else {
      hard_vote = soft_vote;
      note = '硬投票平票，使用软投票结果';
    }

    results.push({
      index: i,
      hard_vote: hard_vote,
      soft_vote: soft_vote,
      note
    });
  }

  return results;
};

const getDiagnosisVotingResults = (data) => {
  const sampleCount = data[0].outputs.length;
  const votingResults = [];

  for (let i = 0; i < sampleCount; i++) {
    const hardVotes = {};
    const softScores = {};

    data.forEach(model => {
      const output = model.outputs[i];
      const result = output.result;
      const logics = output.logics;

      // 硬投票
      hardVotes[result] = (hardVotes[result] || 0) + 1;

      // 软投票
      for (const [label, prob] of Object.entries(logics)) {
        softScores[label] = (softScores[label] || 0) + prob;
      }
    });

    const maxVotes = Math.max(...Object.values(hardVotes));
    const hardWinners = Object.entries(hardVotes)
        .filter(([_, count]) => count === maxVotes)
        .map(([label]) => label);
    const hardVote = hardWinners[0];

    const softVote = Object.entries(softScores)
        .reduce((a, b) => a[1] > b[1] ? a : b)[0];

    const note = (hardWinners.length > 1) ? '硬投票平票，使用软投票结果' : '';

    votingResults.push({
      index: i,
      hard_vote: hardVote,
      soft_vote: softVote,
      note: note
    });
  }

  return votingResults;
};

const uniformModelType = (groups) => {
  const foundTypes = new Set();      // 匹配到的 modelType

  // 深度优先遍历
  function dfs(arr) {
    for (const item of arr) {
      const rd = item.rawData;
      if (rd && groups.has(rd.code)) {
        foundTypes.add(rd.modelType);
      }
      if (item.children?.length) dfs(item.children);
    }
  }

  dfs(treeData.value);

  // 所有 code 都找到了且 modelType 唯一
  return foundTypes.size === 1 ? [...foundTypes][0]
      : false;
}

const saveConfig = async () => {
  if (!voteRule.value) {
    ElMessage.warning('未选择投票规则！')
    return
  }

  const groupSet = new Set();
  nodes.value.forEach(item => {
    if (Array.isArray(item.group)) {
      item.group.forEach(g => groupSet.add(g));
    }
  });

  const groups = JSON.stringify(Array.from(groupSet));
  const type = uniformModelType(groupSet)
  if (!type) {
    ElMessage.warning("需要选择同个领域下的组态！")
    return
  }

  dialogRef.value.updateValues(modelObject.value, groups, type, voteRule.value)
  dialogVisible.value = true;

  // 等待 DOM 更新
  await nextTick();

  // 调整视图以适应节点内容
  if (vueFlowInstance.value) {
    vueFlowInstance.value.fitView(); // 自动调整视图范围
    await new Promise(resolve => setTimeout(resolve, 10)); // 确保完成
  }

  try {
    const vueFlowContainer = vueFlowInstance.value.$el;

    // 使用 dom-to-image 截图
    domtoimage.toPng(vueFlowContainer, {
      backgroundColor: null, // 设置透明背景
      height: vueFlowContainer.offsetHeight,
      width: vueFlowContainer.offsetWidth,
      style: {
        transform: 'scale(1)', // 保证内容没有变形
      },
    })
        .then(function (dataUrl) {
          // 将截图数据转为 Blob 对象
          const blob = dataURLToBlob(dataUrl);

          // 创建 File 对象，文件名格式为 ac_<时间戳>.png
          const timestamp = Date.now();
          const fileName = `ac_${timestamp}.png`;
          const file = new File([blob], fileName, { type: 'image/png' });

          // 使用 FormData 封装文件
          const formData = new FormData();
          formData.append('file', file);

          // 调用上传方法
          uploadIcon(formData).then(uploadResponse => {
            if (uploadResponse.data.success) {
              nodesImage.value = uploadResponse.data.data;
            }
          }).catch((error) => {
            console.error('Error uploading image:', error);
          });
        })
        .catch(function (error) {
          console.error('Error capturing the image:', error);
        });
  } catch (error) {
    console.error('Error capturing or uploading the image:', error);
  }
};

// 将 Base64 数据转为 Blob
function dataURLToBlob(dataURL) {
  const arr = dataURL.split(',');
  const mime = arr[0].match(/:(.*?);/)[1];
  const bstr = atob(arr[1]);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);

  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }

  return new Blob([u8arr], { type: mime });
}

const fetchList = (url) => {
  return axios.get(url).then(response => response.data)
};

// 判断是否已经存在相同的数据源组件
const isNodeExist = (label) => {
  return nodes.value.some(node => node.data.label === label);
};

// 获取最后一个子节点
const getLastChild = (parentId) => {
  const childNodes = nodes.value.filter(node => node.parent === parentId);
  return {
    count: childNodes.length,
    lastChild: childNodes.length > 0 ? childNodes[childNodes.length - 1] : null
  };
};

const findPreviousParentNode = (currentNodeId) => {
  // 提取所有父节点并按 position.x 排序
  const parentNodes = nodes.value
      .filter(node => node.type === 'parent')
      .sort((a, b) => a.position.x - b.position.x);

  // 查找当前父节点的索引
  const currentIndex = parentNodes.findIndex(node => node.id === currentNodeId);

  // 获取前一个父节点
  return (currentIndex - 1) > 0 ? parentNodes[currentIndex - 1] : null;
}

const findNextParentNode = (currentNodeId) => {
// 提取所有父节点并按 position.x 排序
  const parentNodes = nodes.value
      .filter(node => node.type === 'parent')
      .sort((a, b) => a.position.x - b.position.x);

  // 查找当前父节点的索引
  const currentIndex = parentNodes.findIndex(node => node.id === currentNodeId);

  // 获取后一个父节点
  return (currentIndex + 1) < parentNodes.length ? parentNodes[currentIndex + 1] : null;
}

const addEdge = () => {
  edges.value = [];

  const validChildren = nodes.value.filter(node => node.type === 'child');

  const groupMap = new Map();

  validChildren.forEach(node => {
    if (!node.group || node.group.length === 0) return;
    node.group.forEach(g => {
      if (!groupMap.has(g)) groupMap.set(g, []);
      groupMap.get(g).push(node);
    });
  });

  const connectedEdges = new Set();

  groupMap.forEach(nodeList => {
    nodeList.sort((a, b) => Number(a.parent) - Number(b.parent));
    for (let i = 0; i < nodeList.length - 1; i++) {
      const sourceNode = nodeList[i];
      const targetNode = nodeList[i + 1];
      const edgeKey = `${sourceNode.id}->${targetNode.id}`;

      if (!connectedEdges.has(edgeKey)) {
        edges.value.push(createEdge(sourceNode.id, targetNode.id, true, 'arrowclosed'));
        connectedEdges.add(edgeKey);
      }
    }
  });

  // 构造邻接表
  const adjacency = new Map();
  validChildren.forEach(node => {
    adjacency.set(node.id, []);
  });
  edges.value.forEach(edge => {
    adjacency.get(edge.source).push(edge.target);
    adjacency.get(edge.target).push(edge.source); // 如果是无向图，双向添加
  });

  const visited = new Set();
  domainGroups.value = []

  const dfs = (nodeId, group) => {
    visited.add(nodeId);
    group.push(nodeId);
    (adjacency.get(nodeId) || []).forEach(neighbor => {
      if (!visited.has(neighbor)) {
        dfs(neighbor, group);
      }
    });
  };

  validChildren.forEach(node => {
    if (!visited.has(node.id)) {
      const group = [];
      dfs(node.id, group);
      domainGroups.value.push(group);
    }
  });
};


const createEdge = (source, target, animated = false, markerEnd = null) => ({
  id: `${source}-${target}`,
  source,
  target,
  animated,
  type: "straight",
  ...(markerEnd && { markerEnd }),
});

const calculateChildrenCount = (parentId) => {
  return nodes.value.filter(node => node.parent === parentId).length;
};

// 在script setup部分正确定义函数
const getUniformHeight = () => {
  if (!parentNodes.value || parentNodes.value.length === 0) {
    return '100px'; // 默认高度
  }
// 计算最大子节点数
  const maxChildren = Math.max(
      ...parentNodes.value.map(node => calculateChildrenCount(node.id))
  );
// 更安全的高度计算（防止NaN或无限大）
  const calculatedHeight = Math.min(
      Math.max(maxChildren * 50 + 50, 300),
      1000 // 最大1000px
  );
  return `${calculatedHeight}px`;
};

const updateModelObject = (newModelObject) => {
  modelObject.value = newModelObject;
};

// 点击子节点时触发
const handleChildNodeClick = (child) => {
  isSCADA.value = false
  tableData.value = []
  let alModelNamelList = ref([])

  if(child.data.label === 'SCADA数据'){
    dataVisible.value = true
    isUpload.value = true
    isSCADA.value = true
    dataRef.value.getTree()

    nodes.value.forEach(item=>{
      let curParent = getNodeById(parentNodes.value, item['parent'])
      if(item['type'] === 'child' && curParent.data.label === '状态评估'){
        alModelNamelList.value.push(item.data.label)
      }
    })
  }
  else if(child.data.label === 'CMS数据'){
    dataVisible.value = true
    isUpload.value = true
    isSCADA.value = false
    dataRef.value.getTree()
    return
  }
  else {
    nodes.value.forEach(item=>{
      if(item.data.label === child.data.label){
        let curParent = getNodeById(parentNodes.value, item.parent)
        if (curParent.data.label === '状态评估') {
          isSCADA.value = false
          isUpload.value = false
          alModelNamelList.value.push(child.data.label)
          selectedNodeLabel.value = child.data.label
        }
      }
    })
  }

  // 根据alModelNamelList中的算法模型名称，获取变量，
  if(alModelNamelList.value.length !== 0){
    tableData.value=[]
    getVariablesByalModelNames({alModelNamelList: alModelNamelList.value}).then(res=>{
      if(res.data.data !== null && res.data.code === 0){
        tableData.value = res.data.data

        if(tableData.value && tableData.value.length !== 0){
          if (!isSCADA.value)
            drawerVisible.value = true
        }else{
          ElMessage({
            message: '暂无变量信息，请先添加感知变量',
            type: 'warning',
          })

        }
      }
    })
  }
};

const store = useStore();

// 使用 getter 来获取 datasetPath
const scadaTrainUrl = computed(() => store.getters['getScadaTrainUrl']);
const scadaTestUrl = computed(() => store.getters['getScadaTestUrl']);
const cmsTrainUrl = computed(() => store.getters['getCmsTrainUrl']);
const cmsTestUrl = computed(() => store.getters['getCmsTestUrl']);

let taskState=ref(0)
let altaskId=ref(null)
let result = ref(null);
let CMS_result = ref(null);

// In your setup() function or <script setup>
let i = ref(0);
let loading = ref(false);
let taskResult = ref( null);
let timer = ref(null);
let chartVisible = ref(true);

// 数据定义
const chartData = ref([]);
const modelResults = ref([]);
const voteVisible = ref(false)
const votingResults = ref([])
const voteRule = ref('');

let chartIndex = null
let newChartIndex = -1

let isRunning = ref(true);

const buildCodeMap = () => {
  const map = new Map();
  treeData.value.forEach(parent => {
    if (parent.children) {
      parent.children.forEach(child => {
        map.set(child.rawData.code, child);
      });
    }
  });
  return map;
}

const running = async () => {

  if (nodes.value.some(node => node.data.label === "SCADA数据") && (scadaTrainUrl.value === null || scadaTrainUrl.value === '') && (scadaTestUrl.value === null || scadaTestUrl.value === '')) {
    ElMessage.warning("未上传SCADA数据")
    return
  } else if (nodes.value.some(node => node.data.label === "CMS数据") && (cmsTrainUrl.value === null || cmsTrainUrl.value === '') && (cmsTestUrl.value === null || cmsTestUrl.value === '')) {
    ElMessage.warning("未上传CMS数据")
    return
  }

  isRunning.value = true
  chartData.value = []
  newChartIndex = -1
  chartVisible.value = true
  modelResults.value = []
  const codeMap = buildCodeMap();

  const transformedNodes = transformNodes(nodes.value);
  const processedNodeIds = new Set();

  for (const item of transformedNodes) {
    if (!isRunning.value) break; // 如果 isRunning 为 false，退出循环

    // 展示相关性分析的结构图
    newChartIndex = showRelevanceResults(item, codeMap, newChartIndex)

    //查询感知变量
    let alNames = []
    let vars = []
    let newVars=[]
    alNames.push(codeMap.get(item.group).label)
    await getVariablesByalModelNames({ alModelNamelList: alNames }).then(res=> {
      if (res.data.data !== null && res.data.code === 0) {
        vars = res.data.data
        vars.forEach(item => {
          newVars.push(item['varName'])
        })
        newVars.push('errorcode')
      }
    })
    result.value = scadaTestUrl.value
    CMS_result.value = cmsTestUrl.value

    for (const child of item.children) {
      if (!isRunning.value) break;
      if (processedNodeIds.has(child.id)) {
        continue;
      }
      // 初始化图表数据
      chartIndex = chartData.value.findIndex(
          (chart) => chart.title === child.data.label
      );
      if (chartIndex === -1) {
        chartData.value.push({
          title:  child.data.label,
          imageUrl: "",
          loading: true,
          isTrain: false
        });

        newChartIndex = newChartIndex + 1
      }

      let childData = null
      let curParent = getNodeById(parentNodes.value, child.parent)

      // 新增：提取modelUrl
      let modelUrl = undefined;
      if (curParent.data.label === '状态评估' || curParent.data.label === '故障诊断') {
        // 查找taskProcess中almodelType为'状态评估'的trainResult
        const processItem = codeMap.get(item.group)?.rawData?.taskProcess?.find(tp =>
            tp.almodelType === '状态评估' || tp.almodelType === '故障诊断'
        );
        if (processItem && processItem.trainResult) {
          try {
            const trainResultObj = JSON.parse(processItem.trainResult);
            modelUrl = trainResultObj.modelUrl;
          } catch (e) {
            modelUrl = undefined;
          }
        }
      }

      if(curParent.data.label === '故障诊断'){
        childData = {
          ...child.data,
          taskMsg:JSON.stringify({ programUrl: CMS_result.value })
        };
      } else if(curParent.data.label === '状态评估'){
         childData = {
          ...child.data,
          taskMsg:JSON.stringify({ programUrl: result.value, varNameList: newVars, modelUrl: modelUrl })
        };
      } else if( child.data.label === '小波变换算法'){
       const {maxLevel,sliceLength,waveLet,waveMode,n}=child.data
        //根据id
        childData = {
          ...child.data,
          taskMsg: JSON.stringify({programUrl:cmsTestUrl.value,maxlevel:maxLevel,slice_length:sliceLength,wavelet:waveLet,mode:waveMode,n:n})
        };
      }
      else {
        childData = {
          ...child.data,
          taskMsg: JSON.stringify({scadaTrainUrl: '', scadaTestUrl: result.value, varNameList: newVars})
        };
      }


      try {
        const startRes = await startConfig(childData);

        if (startRes.data.data) {
          altaskId.value = startRes.data.data;
          // 等待 loopResult 完成
          await loopResult();
          await nextTick(); // 等待 DOM 更新

          if (taskState.value === 2 && taskResult.value.plotUrl) {
            if (taskResult.value.outputs) {

              const type = codeMap.get(item.group).rawData.modelType === '状态评估' ? 'anomaly' : 'diagnosis';

              modelResults.value.push({
                name: childData.label,
                type: type,
                outputs: taskResult.value.outputs
              });
            }

            // 更新对应图表的图片地址
            updateChart(newChartIndex, { imageUrl: taskResult.value.plotUrl, loading: false });
            await nextTick(); // 等待子组件渲染完成
            processedNodeIds.add(child.id);
          }
          if (taskState.value !== 2 || taskResult.value == null) {
            break;
          }
        }
      } catch (error) {
        console.error('请求算法出错:', error);
        break; // 出现错误时继续下一个任务
      }
    }
  }
};

const showRelevanceResults = (groupItem, codeMap, index) => {
  if (!groupItem?.group) return index;

  const codeItem = codeMap.get(groupItem.group);
  if (!codeItem) return index;

  const taskProcess = codeItem.rawData?.taskProcess || [];
  const relevanceProcess = taskProcess.find(tp => tp.almodelType === '相关性分析');

  if (!relevanceProcess?.trainResult) return index;

  chartData.value.push({
    title: `${codeItem.label}-${relevanceProcess.almodelName}`,
    imageUrl: relevanceProcess.trainResult,
    loading: false,
    isTrain: true,
  });

  return index + 1;
}

const getNodeById = (data, id) => {
  const item = data.find(element => element.id === id);
  return item ? item : null;
}

const loopResult = async () => {
  i.value = 0;
  console.log("开始轮循请求");
  // 先清除之前的定时器
  if (timer.value) {
    clearInterval(timer.value);
  }
  // 创建一个 Promise 来处理轮循完成
  return new Promise((resolve, reject) => {
    timer.value = setInterval(() => {
      fun(timer.value, i.value++, resolve,reject);
    }, 3000);
  });
};

const fun = (timerInstance, count, resolve,reject) => {
  setTimeout(() => {
    console.log("开始轮循请求：");
    console.log("次数：" + count);
    getObj(altaskId.value).then(response => {
      taskState.value = response.data.data.taskState;
      if (taskState.value === 2) {
        clearInterval(timerInstance);

        loading.value = false;
        taskResult.value = JSON.parse(response.data.data.taskResult);

        if (taskResult.value.scadaTestUrl)
          result.value = taskResult.value.scadaTestUrl
        if (taskResult.value.datasetUrl)
          CMS_result.value = taskResult.value.datasetUrl
        taskResult.value.plotUrl=JSON.parse(response.data.data.taskResult).plotUrl

        resolve();
        return; // 重要：立即返回避免后续执行

      }
    });

    if (count >= 100) {
      ElMessage.warning('模型请求超时');
      loading.value = false;
      clearInterval(timerInstance);
      reject(new Error('模型请求超时')); // 超时时调用 reject
      return;
    }
  }, 0);
};

const transformNodes = (rawNodes)=>{
  // 筛选所有子节点
  const excludedParentIds = rawNodes
      .filter(node => node.type === "parent" && (node.data.label === "相关性分析" || node.id === "1"))
      .map(node => node.id);
  const children = rawNodes.filter(
      node => node.type === "child"
          && !excludedParentIds.includes(node.parent)
  );

  // 按 group 分组
  const groupMap = new Map();

  children.forEach(child => {
    if (Array.isArray(child.group)) {
      child.group.forEach(g => {
        if (!groupMap.has(g)) {
          groupMap.set(g, []);
        }
        groupMap.get(g).push(child);
      });
    }
  });

  // 转换为数组结构
  const groupedChildren = [];
  groupMap.forEach((childList, groupName) => {
    groupedChildren.push({
      group: groupName,
      children: childList
    });
  });

  return groupedChildren;
}

const updateChart = (index, updates) => {
  const newData = [...chartData.value]; // 创建新数组
  newData[index] = { ...newData[index], ...updates };
  chartData.value = newData; // 触发响应式更新
};

const resetView = () => {
  if (vueFlowInstance.value) {
    nodes.value = nodes.value.filter((node) => {
      return node.type === 'parent' && node.id === '1'
    });
    edges.value = []

    fitView()
  }
  resetData()
  ElMessage.info('已重置！')
};

const resetData = () => {

  showIds.value = ['1']
  flowNodes.value = []
  domainGroups.value = []
  chartData.value = []
  modelResults.value = []
  chartIndex = -1
  store.commit('setScadaTrainUrl', '');
  store.commit('setScadaTestUrl', '');
  store.commit('setCmsTrainUrl', '');
  store.commit('setCmsTestUrl', '');
  if (timer.value) {
    clearInterval(timer.value);
  }
  isRunning.value = false
  if (dataRef.value)
    dataRef.value.reset()

};

const clear = () => {
  if (timer.value) {
    clearInterval(timer.value);
  }
  isRunning.value = false
  chartData.value = []
  chartVisible.value = false
  chartIndex = null
  ElMessage.info('已停止组态运行！')
};

</script>

<style scoped lang="scss">
.vueflow-container {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column; /* 设置为垂直排列 */
}

.button-container {
  display: flex;
  justify-content: flex-end; /* 水平排列并靠右 */
  gap: 10px; /* 按钮之间的间隔 */
  margin: 20px;
}

.basic-flow {
  flex: 1;
}

.mytree {
  margin-left: 15px;
  height: 100%;
  flex-direction: column;
  overflow-y: auto;
  display: flex;
  align-items: center;
}

.mytree {
  margin-left: 15px;
  height: 100%;
  flex-direction: column;
  overflow-y: auto;
  display: flex;
  align-items: center;
}

.charts-container {
  position: absolute;
  display: flex;
  flex-wrap: wrap;
  gap: 20px; /* 图表间距 */
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1;
}

.chart-item {
  position: relative;  /* 保持相对定位，作为定位参照 */
  /* 其他样式 */
}

.flow-card {
  padding: 0; /* 移除内边距让流程图撑满 */
  height: calc(100% - 30px); /* 计算高度留出按钮和饼图空间 */
}
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 16px;
  margin-bottom: 16px;
}

.reSetBtn,
.printBtn,
.editWorktBtn{
  height: 27px;
}

.custom-disabled {
  background-color: #d3d3d3 !important;
  color: #8c8c8c !important;
  border-color: #d3d3d3 !important;
  cursor: not-allowed;
}

</style>
