<template>
  <div style="display: flex; height: 100%;background:#ffffff;">
    <!-- 左侧树形结构 -->
    <div class="mytree">
      <Tree
          :treeData="treeData"
          @clickNode="handleNodeClick"
          @addShow="addShow"
          @deleteShow="deleteShow"
          @loadTreeData="loadTreeData"
      />
    </div>
    <!-- 中部泳道 -->
    <div class="vueflow-container">
      <!--按钮区域   -->
      <div class="button-container">
        <button class="reSetBtn" @click="resetView">重置</button>
        <button class="editWorktBtn" @click="onlineTrain">在线训练</button>
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
          <ChildNode v-bind="c.data" :nodeContext="c"   @nodeClicked="handleChildNodeClick"/>
        </template>
        <template #node-hidden="h">
          <HiddenNode v-bind="h.data"/>
        </template>
      </VueFlow>
      <!-- 动态图表区域 -->
      <div class="charts-container" v-show="chartVisible" style="position: absolute; bottom: 0; left: 0; right: 0; z-index: 1;">
        <div v-for="(chart, index) in chartData" :key="index" class="chart-item">
          <!-- 每个图表上方的进度条 -->
          <div v-if="chart.isTraining===1" class="progress-container">
            <el-icon class="progress-icon"><Loading /></el-icon>
            <el-progress
                :text-inside="true"
                :stroke-width="18"
                :percentage="chart.trainingProgress"
            />
            <span class="progress-label">{{ chart.progressText }}</span>
          </div>
          <!-- 原有图表组件 -->
          <DynamicChart
              :is-visible="chartVisible"
              :title="chart.title"
              :imageUrl="chart.imageUrl"
              :loading="chart.loading"
              :isTraining="chart.isTraining"
              :noPrecess="chart.noPrecess"
              :otherInfo="chart.otherInfo"
              @clear="clear"
          />
        </div>
      </div>
    </div>
    <!--接入数据集上传侧边栏 -->
    <SideBar ref="sideRef" v-model:visible="drawerVisible" :isSCADA="isSCADA" :isTrain="isTrain" @update:modelObject="updateModelObject"/>
    <!--接入算法参数配置上传侧边栏 -->
    <AlParamsConfigSideBar ref="alParamConfigRef" v-model:visible="alParamConfigVisible" v-model:alName="alName"  v-model:formdata="formdata" :wavePacketVisible="wavePacketVisible"/>
    <!-- 保存组态的组件 -->
    <Dialog ref="dialogRef" v-model:dialogVisible="dialogVisible" :nodes="nodes" :nodesImage="nodesImage" />
    <!-- 选择变量的组件 -->
    <VarSelect ref="varSelectRef" v-model:varSelectVisible="varSelectVisible"  @confirm="onRelevance"/>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, ref} from 'vue'
import {useStore} from 'vuex';
import {useVueFlow, VueFlow} from '@vue-flow/core'
import {Background} from '@vue-flow/background'
import ParentNode from "../flow/ParentNode.vue";
import ChildNode from "../flow/ChildNode.vue";
import HiddenNode from "../flow/HiddenNode.vue";
import Tree from "../flow/TreeBar.vue"
import SideBar from '../flow/DataBar.vue'; // 引入 SideBar 组件
import AlParamsConfigSideBar from './AlParamsConfigSideBar.vue'; // 引入 SideBar 组件
import Dialog from './AlDialogBar.vue'
import VarSelect from './VarSelectDialog.vue'
import { startTrain} from "@/api/al/taskConfig";
import {ElMessage} from 'element-plus'
import {getObj} from "@/api/al/altest/altest";
import DynamicChart from "../flow/Chart.vue";
import {uploadIcon} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import domtoimage from 'dom-to-image';
import {getDiagnosisObj1} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisOne";
import {getEvaluationObj1} from "@/api/al/stateEvaluation/stateEvaluationOne";
import {getFeatureExactObj1} from "@/api/al/featureExtraction/featureExtraction";
// VueFlow 初始化
const {onInit} = useVueFlow()
const vueFlowInstance = ref(null);
const { fitView, updateNodeInternals } = useVueFlow();

const parentNodes = ref([])
const nodes = ref([])
const edges = ref([])

// 数据结构
const treeData = ref([])
const showIds = ref(['1']);

//初始化算法列表
// 响应式变量存储每个分类的子节点
const cleanChildren = ref([]);
const missingValueFill = ref([]);
const anomalyDetection = ref([]);
const dataNormalization = ref([]);
const relevanceAnalysis = ref([]);
const evaluationChildren = ref([]);
const diagnosisChildren = ref([]);
const featureExtraction = ref([]);
// 存储抽屉框的状态和选中的节点数据
const drawerVisible = ref(false);
const tableData = ref([]);
const isUpload = ref(false);
const isSCADA = ref(false);
const wavePacketVisible = ref(false);
const isTrain = ref(true);

const dialogVisible = ref(false);
const varSelectVisible = ref(false);
const alParamConfigVisible = ref(false);
const alName = ref(null);
const formdata = ref({
  learningRate: '',
  trainEpoch: '',
  optimizer: '',
  trainBatch: '',
  wavelet:'',
  mode:'',
  maxlevel:'',
  sliceLength:'',
  n:''
});
const nodesImage = ref('')
const sideRef = ref(null)
const alParamConfigRef = ref(null)
const dialogRef = ref(null)
const modelObject = ref({})
const varSelectRef = ref(null)
const relevanceVars = ref([])
const relevanceImage= ref([])
const relevanceNode= ref(null)

// 初始化 VueFlow
onInit((vueFlowInstance) => {
  vueFlowInstance.fitView()
})

// 数据加载
onMounted(async () => {
  await loadTreeData(true);
})

const updateModelObject = (newModelObject) => {
  modelObject.value = newModelObject;
};

const onRelevance = (target, varList, imageUrl, config) => {
  relevanceVars.value = [target, ...varList.map(item => item.name), 'errorcode'];
  relevanceImage.value = imageUrl
  relevanceNode.value = config
  const node = nodes.value.find(node => node.data.label === config.label)
  if (node) {
    node.data = {
      ...node.data,
      trainResult: imageUrl
    }
  }
};

const saveConfig = async () => {
  if (nodes.value.some(node => node.data.label === "SCADA数据") && (scadaTrainUrl.value === null || scadaTrainUrl.value === '') && (scadaTestUrl.value === null || scadaTestUrl.value === '')) {
    ElMessage.warning("未选择SCADA数据进行组态训练！")
    return
  } else if (nodes.value.some(node => node.data.label === "CMS数据") && (cmsTrainUrl.value === null || cmsTrainUrl.value === '') && (cmsTestUrl.value === null || cmsTestUrl.value === '')) {
    ElMessage.warning("未选择CMS数据进行组态训练！")
    return
  }else if (nodes.value.findIndex(node => node.data.label === '相关性分析') > -1 && relevanceVars.value.length === 0) {
    ElMessage.warning("未进行相关性分析")
    return
  }

  dialogRef.value.updateValues(modelObject.value, allModelParams.value, relevanceVars.value, trainMetrics)
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

const loadTreeData = async (isFirst) => {
  try {
    treeData.value = []
    missingValueFill.value = []
    anomalyDetection.value = []
    dataNormalization.value = []
    relevanceAnalysis.value = []

    // 获取所有节点的子节点
    cleanChildren.value = await fetchNodeChildren("clean")
    classifyElements(cleanChildren.value);

    evaluationChildren.value = await fetchNodeChildren("evaluation");
    diagnosisChildren.value = await fetchNodeChildren("diagnosis");
    featureExtraction.value = await fetchNodeChildren('feature')

    // 生成树形数据，去掉不需要的节点（数据源）
    treeData.value = [
      {
        id: '2',
        label: '相关性分析',
        children: normalizeChildren(relevanceAnalysis.value, '2'),
        showAdd: true,
        showClose: true,
        isPretreatment: true,
      },
      {
        id: '3',
        label: '异常值检测',
        children: normalizeChildren(anomalyDetection.value, '3'),
        showAdd: true,
        showClose: true,
        isPretreatment: true,
      },
      {
        id: '4',
        label: '缺失值填充',
        children: normalizeChildren(missingValueFill.value, '4'),
        showAdd: true,
        showClose: true,
        isPretreatment: true,
      },
      {
        id: '5',
        label: '数据标准化',
        children: normalizeChildren(dataNormalization.value, '5'),
        showAdd: true,
        showClose: true,
        isPretreatment: true,
      },
      {
        id: '6',
        label: '特征提取',
        children: normalizeChildren(featureExtraction.value, '6'),
        showAdd: true,
        showClose: true,
        isPretreatment: true,
      },
      {
        id: '7',
        label: '状态评估',
        children: normalizeChildren(evaluationChildren.value, '7'),
        showAdd: true,
        showClose: true,
        isPretreatment: false,
      },
      {
        id: '8',
        label: '故障诊断',
        children: normalizeChildren(diagnosisChildren.value, '8'),
        showAdd: true,
        showClose: true,
        isPretreatment: false,
      },
    ];

    if (isFirst) {
      // 初始化横坐标
      let nodeXPosition = 0;  // 设置初始位置为 0
      let nodeWidth = 150;

      // 保留“数据源”作为泳道节点
      const extendedParentNodes = [
        {
          id: '1', // 数据源
          type: 'parent',
          data: {label: '数据源',uniformHeight: '100px'},
          position: {x: nodeXPosition, y: 0},
          draggable: false,
          style: { pointerEvents: 'none', zIndex: 0 }
        },
      ];

      // 更新 treeData 和 parentNodes
      parentNodes.value = [
        ...extendedParentNodes,
        ...treeData.value.map(item => {
          const parentNode = {
            id: item.id,
            type: 'parent',
            data: {label: item.label, uniformHeight: '100px'},
            isPretreatment: item.isPretreatment,
            position: {x: nodeXPosition + nodeWidth, y: 0},
            draggable: false,
            style: { pointerEvents: 'none', zIndex: 0 }
          };

          // 为下一个父节点增加 nodeWidth 的间距
          nodeXPosition += nodeWidth;

          return parentNode;
        })
      ];

      // 初始化统一高度
      const initialHeight = getUniformHeight()
      parentNodes.value.forEach(node => {
        node.data.uniformHeight = initialHeight
      })
      nodes.value.push(parentNodes.value[0])
    }
  } catch (error) {
    console.error('Error loading tree data:', error);
  }
};


const classifyElements = (algorithm) => {
  algorithm.forEach(item => {
    if (item.alType === '缺失值填充') {
      missingValueFill.value.push(item);
    } else if (item.alType === '异常值检测') {
      anomalyDetection.value.push(item);
    } else if (item.alType === '数据标准化') {
      dataNormalization.value.push(item);
    } else if (item.alType === '相关性分析') {
      relevanceAnalysis.value.push(item)
    }
  });
};

const normalizeChildren = (children, id) => {
  return children.map(item => ({
    label: item.alName || item.modelName, // 统一使用 name 属性
    code: item.alCode, // 统一使用 code 属性
    configUrl:item.trainUrl || item.configUrl,
    saveUrl: item.configUrl,
    shortName:item.alShortName || item.modelShortName,
    parentId: id,
    showAdd: false,
    showClose: false,
    pretreatment:item.pretreatment || null
  }));
};

const fetchNodeChildren = async (type) => {
  let apiUrl;
  switch (type) {
    case 'clean':
      apiUrl = '/al/dataCleaning/list'
      break;
    case 'evaluation':
      apiUrl = '/al/stateEvaluation/list'
      break;
    case 'diagnosis':
      apiUrl = '/al/alFaultDiagnosisbase/list'
      break;
    case 'feature':
      apiUrl = '/al/featureExtraction/list'
      break;
    default:
      throw new Error('Invalid type provided for fetching node children.')
  }

  try {
    const response = await fetchList(apiUrl);

    if (response.code === "200") {
      // 处理返回的数据，转换成树节点格式
      return response.data.map(record => ({
        ...record,
        label: record['modelName'],
        parentType: type,
        showAdd: false,
        level: 2
      }));
    } else {
      console.error(`Unexpected response code: ${response.message}`);
      return [];
    }
  } catch (error) {
    console.error(`Error fetching ${type} node children:`, error);
    return [];
  }
};

const fetchList = (url) => {
  return axios.get(url).then(response => response.data)
};

// 删除连接到前后泳道的边
const deleteEdges = (id, direction) => {
  const parent = direction === 'previous' ? findPreviousParentNode(id) : findNextParentNode(id);
  if (parent) {
    const childNodes = nodes.value.filter(node => node.parent === parent.id);
    childNodes.forEach(childNode => {
      edges.value = edges.value.filter(edge =>
          direction === 'previous' ? edge.source !== childNode.id : edge.target !== childNode.id
      );
    });
  }
}

const updateEdges = (id) => {

  nodes.value = nodes.value.filter((node) => {
    // 保留不需要删除的节点
    return !(node.type === 'child' && node.parent === id);
  });

  const nodeExists = (nodeId) => nodes.value.some(node => node.id === nodeId);
  // 先清理无效边
  edges.value = edges.value.filter(edge => {
    return nodeExists(edge.source) && nodeExists(edge.target);
  });

  const previousParent = findPreviousParentNode(id)
  const afterParent = findNextParentNode(id)

  if (previousParent && afterParent) {
    const previousChildren = nodes.value.filter(node => node.parent === previousParent.id);
    const afterChildren = nodes.value.filter(node => node.parent === afterParent.id);

    if (previousChildren.length > 0 && afterChildren.length > 0) {
      if (previousChildren.length === 1) {
        afterChildren.forEach(childNode => {
          edges.value.push(createEdge(previousChildren[0].id, childNode.id, true, 'arrowclosed'));
        });
      } else {
        previousChildren.forEach(childNode => {
          edges.value.push(createEdge(childNode.id, previousParent.id, true));
        });
        afterChildren.forEach(childNode => {
          edges.value.push(createEdge(previousParent.id, childNode.id, true, 'arrowclosed'));
        });
      }
    }
  }
}

const getNodeById = (data, id) => {
  const item = data.find(element => element.id === id);
  return item ? item : null;
}

// 当更新显示的节点时，修改 showIds
const addShow = (id) => {
  nextTick(() => {
    let idx = showIds.value.indexOf(id)
    const curParent = getNodeById(parentNodes.value, id)
    if (idx > -1) {
      ElMessage.info("该领域泳道已存在!");
      return
    } else {
      const exist = nodes.value.some(node => node.type === 'parent' && node.isPretreatment === false);

      if (curParent.isPretreatment || !exist) {
        showIds.value.push(id);
        showIds.value.sort();
        nodes.value = nodes.value.filter(node => node.type !== 'parent');
      } else {
        ElMessage.warning("只能选择一个领域泳道!");
        return
      }
    }

    // 更新节点位置
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
    if(idx < 0) {
      deleteEdges(id, 'previous')
      deleteEdges(id, 'after')
    }
  })
};

const deleteShow = (id) => {
  nextTick(() => {
    let idx = showIds.value.indexOf(id)
    const curParent = getNodeById(parentNodes.value, id)
    if (idx > -1) {
      showIds.value.splice(idx, 1)
      updateEdges(id);
      nodes.value = nodes.value.filter((node) => {
        // 保留不需要删除的节点
        return node.type !== 'parent';
      });

      if (curParent.data.label === '状态评估') {
        const relevance = parentNodes.value.find(node => node.data.label === '相关性分析')
        const relevanceNodes = nodes.value.filter(node => node.parent === relevance.id);
        if (relevanceNodes.length === 0)
          nodes.value = nodes.value.filter(node => node.data.label !== 'SCADA数据');
      } else if (curParent.data.label === '相关性分析') {
        const evaluation = parentNodes.value.find(node => node.data.label === '状态评估')
        const evaluationNodes = nodes.value.filter(node => node.parent === evaluation.id);
        if (evaluationNodes.length === 0)
          nodes.value = nodes.value.filter(node => node.data.label !== 'SCADA数据');
      } else if (curParent.data.label === '故障诊断') {
        nodes.value = nodes.value.filter((node) => {
          // 保留不需要删除的节点
          return node.data.label !== 'CMS数据' ;
        });
      }
    } else {
      ElMessage.warning("该领域泳道还未添加!");
      return
    }

    // 更新节点位置
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
  })
}

const handleNodeClick = (nodeData) => {
  if (!nodeData.children || nodeData.children.length < 0)
    addNode(nodeData);
};

const isParentIn = async (child) => {
  const parentId = child.parentId;

  for (const item of nodes.value) {
    if (item.id === parentId) {
      return true;
    }
  }

  // 如果没有找到匹配的父节点，返回 false
  return false;
}

const addNode = async (nodeData) => {

  if(!await isParentIn(nodeData)){
    ElMessage.warning("为选择该算法对应的泳道！")
    return
  }

  let x_interval = 25
  let exitNode = nodes.value.findIndex(node => node.id === nodeData.code)
  const currentParentNode = nodes.value.find(node => node.id === nodeData.parentId);  // 查找当前父节点
  let result = null

  if (exitNode > -1) {  //已经存在当前的node列表，就删去

    // 删除与该节点相关的所有边
    edges.value = edges.value.filter(edge => edge.source !== nodeData.code && edge.target !== nodeData.code);

    nodes.value.splice(exitNode, 1)

    if (currentParentNode.data.label === '状态评估'){
      const relevance = parentNodes.value.find(node => node.data.label === '相关性分析')
      const relevanceNodes = nodes.value.filter(node => node.parent === relevance.id);
      if (relevanceNodes.length === 0)
        nodes.value = nodes.value.filter(node => node.data.label !== 'SCADA数据');
    }else if (currentParentNode.data.label === '故障诊断') {
      nodes.value = nodes.value.filter(node => node.data.label !== 'CMS数据');
    }else if (currentParentNode.data.label === '相关性分析') {
      const evaluation = parentNodes.value.find(node => node.data.label === '状态评估')
      const evaluationNodes = nodes.value.filter(node => node.parent === evaluation.id);
      if (evaluationNodes.length === 0)
        nodes.value = nodes.value.filter(node => node.data.label !== 'SCADA数据');
    }
  } else {  //否则，添加

    let newNodePosition = {x: currentParentNode.position.x + x_interval, y: 50};

    const childNodesToRemove = nodes.value.filter(node => node.parent === nodeData.parentId);
    if (childNodesToRemove.length > 0) {
      // 如果有现有子节点，删除所有这些子节点
      childNodesToRemove.forEach(node => {
        const nodeIndex = nodes.value.findIndex(n => n.id === node.id);
        if (nodeIndex !== -1) {
          nodes.value.splice(nodeIndex, 1);  // 删除该节点
        }
      });

      // 删除与这些子节点相关的所有边
      edges.value = edges.value.filter(edge => !childNodesToRemove.some(node => edge.source === node.id || edge.target === node.id));

      await nextTick(() => {
        updateNodeInternals();
      });
    }

    // 添加新子节点
    nodes.value.push({
      id: nodeData.code,
      type: 'child',
      data: {
        label: nodeData.label,
        configUrl: nodeData.configUrl,
        saveUrl: nodeData.saveUrl,
        shortName: nodeData.shortName,
        pretreatment:nodeData.pretreatment
      },
      parent: nodeData.parentId,
      position: newNodePosition,
      draggable: false
    });

    //  自动添加数据源组件
    const DS_MAP = {
      '状态评估':      { id: 'datasource-1', label: 'SCADA数据' },
      '相关性分析':    { id: 'datasource-1', label: 'SCADA数据' },
      '故障诊断':      { id: 'datasource-2', label: 'CMS数据' }
    };

    const match = DS_MAP[currentParentNode.data.label];
    if (match && !isNodeExist(match.label)) {
      const dataNode = {
        id: match.id,
        type: 'child',
        data: { label: match.label },
        parent: '1',
        position: { x: x_interval, y: 50 },
        draggable: false
      };
      nodes.value.push(dataNode);
      addEdge(parentNodes.value[0], dataNode);
    }

    // 处理新增节点的连线
    addEdge(currentParentNode, nodeData);
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

// 判断是否已经存在相同的数据源组件
const isNodeExist = (label) => {
  return nodes.value.some(node => node.data.label === label);
};

const findPreviousParentNode = (currentNodeId) => {
  // 提取所有父节点并按 position.x 排序
  const parentNodes = nodes.value
      .filter(node => node.type === 'parent')
      .sort((a, b) => a.position.x - b.position.x);

  // 查找当前父节点的索引
  const currentIndex = parentNodes.findIndex(node => node.id === currentNodeId);

  // 获取前一个父节点
  return (currentIndex - 1) > -1 ? parentNodes[currentIndex - 1] : null;
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

const addEdge = (parentNode, currentNode) => {
  const previousParentNode = findPreviousParentNode(parentNode.id)
  const afterParentNode = findNextParentNode(parentNode.id)

  if (previousParentNode){
    const childNodes = nodes.value.filter(node => node.parent === previousParentNode.id);

    if(childNodes.length > 0){
      childNodes.forEach(childNode => {
        if (childNode.data.label === '小波变换算法') {
          if (currentNode.pretreatment === '小波变换')
            edges.value.push(createEdge(childNode.id, currentNode.code || currentNode.id, true, 'arrowclosed'));
        } else
          edges.value.push(createEdge(childNode.id, currentNode.code || currentNode.id, true, 'arrowclosed'));
      });
    }
  }

  if(afterParentNode){
    const childNodes = nodes.value.filter(node => node.parent === afterParentNode.id);

    if(childNodes.length > 0) {
      childNodes.forEach(childNode => {
        if (afterParentNode.data.label === '故障诊断') {
          if (childNode.data.pretreatment === '小波变换')
            edges.value.push(createEdge(currentNode.code || currentNode.id, childNode.id, true, 'arrowclosed'));
        } else
          edges.value.push(createEdge(currentNode.code || currentNode.id, childNode.id, true, 'arrowclosed'));
      });
    }
  }
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
      Math.max(maxChildren * 50 + 50, 300), // 最小100px
      1000 // 最大1000px
  );
  return `${calculatedHeight}px`;
};

let wavelet = ref('')
let mode = ref('')
let maxlevel = ref('')
let sliceLength = ref('')
let n = ref('')
// 点击子节点时触发
const handleChildNodeClick = async (child) => {
  isSCADA.value = false
  tableData.value = []
  alName.value = child.data.shortName

  //查询默认参数
  let learningRate = ''
  let optimizer = ''
  let trainBatch = ''
  let trainTimes = ''
  let otherParams = null
  let data = null

  if (child.id.startsWith('se-')) {
    const result =  await getEvaluationObj1(child.data.label)
    data=result.data.data
    let { learningRate: lr, optimizer: opt, trainBatch: batch, trainTimes: epoch, ...otherParams } = data
    learningRate=lr
    optimizer=opt
    trainBatch=batch
    trainTimes=epoch
  }
  if (child.id.startsWith('fd-')) {
    const result = await getDiagnosisObj1(child.data.label)
    data=result.data.data
    let { learningRate: lr, optimizer: opt, trainBatch: batch, trainTimes: epoch, ...otherParams } = data
    learningRate=lr
    optimizer=opt
    trainBatch=batch
    trainTimes=epoch
    const waveObj=nodes.value.find(item=>item.id.startsWith('fe-'))
    if(waveObj){
        const result = await getFeatureExactObj1({name:waveObj.data.label})
        data=result.data.data
        let { n:nn,wavelet: wa, mode: mo, maxlevel: mal, sliceLength: sl, ...otherParams } = data
         wavelet.value = wa
         mode.value = mo
         maxlevel.value = mal
         sliceLength.value = sl
         n.value = nn
      if(wavelet.value && mode.value && maxlevel.value && sliceLength.value){
        Object.assign(formdata.value, {
          wavelet: wavelet.value,
          mode: mode.value,
          maxlevel: maxlevel.value,
          sliceLength: sliceLength.value,
          n: n.value
        });
      }
    }

  }



  if (learningRate && optimizer && trainBatch && trainTimes) {
    Object.assign(formdata.value, {
      learningRate: learningRate,
      trainEpoch: trainTimes,
      optimizer: optimizer,
      trainBatch: trainBatch
    });
  }


  const currentParam = store.getters.getAllModelParams.find(item => item.alName === alName.value);
  if (currentParam) {
    // 找到则复制属性给 formdata（保持响应性）
    Object.assign(formdata.value, currentParam);
  }
  const currentNode = nodes.value.find(node => node.data.label === child.data.label)
  const curParent = getNodeById(parentNodes.value, currentNode.parent)

  if (child.data.label === 'SCADA数据') {
    // 在 SCADA 数据节点点击时，直接设置选中节点和显示抽屉
    drawerVisible.value = true  // 直接显示抽屉框
    isUpload.value = true
    isSCADA.value = true
    sideRef.value.getTree()
  } else if (child.data.label === 'CMS数据') {
    drawerVisible.value = true // 显示抽屉框
    isUpload.value = true
    isSCADA.value = false
    sideRef.value.getTree()
  } else if (curParent.data.label === '相关性分析') {
    varSelectVisible.value = true;
    varSelectRef.value.downloadAndParseCSV(scadaTrainUrl.value, currentNode.data)
  } else if (child.id.startsWith('se') || child.id.startsWith('fd')) {
    alParamConfigVisible.value = true;
    if (child.data.pretreatment === '小波变换') {
      wavePacketVisible.value = true
    } else {
      wavePacketVisible.value = false
    }
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

// In your setup() function or <script setup>
let i = ref(0);
let loading = ref(false);
let taskResult = ref( null);
let timer = ref(null);
let chartVisible = ref(false);

// 数据定义
const chartData = ref([]);


let chartIndex = null

let isRunning = ref(true);
const allModelParams = computed(() => store.getters['getAllModelParams']);
const trainMetrics = new Map();
let waveDatasetUrl=ref('')
const onlineTrain = async () => {
  if (allModelParams.value.length === 0) {
    ElMessage.warning('如果需要配置模型参数，请点击画布中的模型填写参数信息');
    return;
  }

  // 检查数据上传情况
  if (nodes.value.some(node => node.data.label === "SCADA数据") && !scadaTrainUrl.value && !scadaTestUrl.value) {
    ElMessage.warning("未上传SCADA数据");
    return;
  } else if (nodes.value.some(node => node.data.label === "CMS数据") && !cmsTrainUrl.value && !cmsTestUrl.value) {
    ElMessage.warning("未上传CMS数据");
    return;
  } else if (nodes.value.findIndex(node => node.data.label === '相关性分析') > -1 && relevanceVars.value.length === 0) {
    ElMessage.warning("未进行相关性分析");
    return;
  }

    chartData.value = []
    trainMetrics.clear()

    if (nodes.value.findIndex(node => node.data.label === '相关性分析') > -1) {
      if (relevanceVars.value.length === 0) {
        ElMessage.warning("未进行相关性分析")
        return
      } else {
        chartData.value.push({
          title: relevanceNode.value.label,
          imageUrl: relevanceImage,
          loading: false,
          isTraining: 0,
          isTrain: true,
          trainingProgress: 0,
          progressText: '处理中',
          noPrecess:false
        });
      }
    }
    isRunning.value = true


    const transformedNodes = transformNodes(nodes.value);
    //初始化scada数据集
    result.value = {
      scadaTrainUrl: scadaTrainUrl.value,
      scadaTestUrl: scadaTestUrl.value
    };

    for (const item of transformedNodes) {
      if (!isRunning.value) break;

      for (const child of item.children) {
        if (!isRunning.value) break;

        const curParent = getNodeById(parentNodes.value, child.parent);
        let params = {};
        const filteredParam = allModelParams.value.find(item => item.alName === child.data.shortName);
        if (filteredParam) {
          const {alName, ...remainingParams} = filteredParam;
          params = remainingParams;
        } else {
          params = {
            learningRate: '',
            trainEpoch: '',
            optimizer: '',
            trainBatch: ''
          };
        }

        let childData;
        var newChartIndex = chartData.value.findIndex(chart => chart.title === child.data.label);
        // if(child.data.pretreatment!=='小波变换'){
        //
        //
        // }

        // 图表初始化
          chartData.value.push({
            title: child.data.label,
            imageUrl: "",
            loading: true,
            isTraining: 0,
            noPrecess:true,
            trainingProgress: 0,
            progressText: '',
            otherInfo: null
          });
          newChartIndex = chartData.value.length - 1;

        // 故障诊断 + 小波变换处理逻辑
        if (curParent.data.label === '故障诊断') {
          if (  child.data.pretreatment === '小波变换') {
            try {
              var waveletConfig= store.getters.getWavePackt
              if(waveletConfig===null){
                ElMessage.warning('需要选择小波变换提取算法');
                return;
              }

              // 图表初始化
              var newChartIndex = chartData.value.findIndex(chart => chart.title === child.data.label);
              chartVisible.value = true  //新加的
              updateChart(newChartIndex, {isTraining: 1, progressText: "训练中", trainingProgress: 0,otherInfo: {wavePacketImage:store.getters.getWavePacketImage || ''}});
              childData = {
                ...child.data,
                taskMsg: JSON.stringify({
                  programUrl: waveDatasetUrl.value,
                  ...params
                })
              };

            } catch (error) {
              console.error("小波变换失败", error);
              continue;
            }
          }
            else if(child.data.pretreatment !== '小波变换') {
            // 图表初始化
            let newChartIndex = chartData.value.findIndex(chart => chart.title === child.data.label);
            if (newChartIndex === -1 && child.data.label !== '小波变换算法') {
              chartData.value.push({
                title: child.data.label,
                imageUrl: "",
                loading: true,
                isTraining: 0,
                noPrecess:true,
                trainingProgress: 0,
                progressText: '',
                otherInfo: null
              });
              newChartIndex = chartData.value.length - 1;
            }
            chartVisible.value = true  //新加的
            updateChart(newChartIndex, {isTraining: 1, progressText: '训练中'});
            childData = {
              ...child.data,
              taskMsg: JSON.stringify({
                cmsTrainUrl: cmsTrainUrl.value,
                cmsTestUrl: cmsTestUrl.value,
                ...params
              })
            };
          }

        } else if (curParent.data.label==='缺失值填充' || curParent.data.label === '状态评估') {
          if(curParent.data.label === '缺失值填充'){
            childData = {
              ...child.data,
              taskMsg: JSON.stringify({
                scadaTrainUrl: result.value.scadaTrainUrl,
                scadaTestUrl: result.value.scadaTestUrl,
                varNameList: relevanceVars.value,
                ...params
              })
            };

            chartVisible.value = true  //新加的
            updateChart(newChartIndex, {isTraining: 1, progressText: '缺失值填充中',noPrecess:false});
            result.value= await runMissingValueFill(childData,newChartIndex)
            continue;
          }
          updateChart(newChartIndex, {isTraining: 1, progressText: '训练中'});
          const resultData = typeof result.value === 'string' ? JSON.parse(result.value) : result.value;

          childData = {
            ...child.data,
            taskMsg: JSON.stringify({
              scadaTrainUrl: resultData.scadaTrainUrl || scadaTrainUrl.value,
              scadaTestUrl: resultData.scadaTestUrl || scadaTestUrl.value,
              varNameList: relevanceVars.value,
              ...params
            })
          };

        }
        else if (child.data.label === '小波变换算法') {  //小波变换的预处理
          store.commit('setWavePacket', {
            configUrl: child.data.configUrl,
            shortName: child.data.shortName
          });
          var waveletConfig = store.getters.getWavePacketParamsByAlName(child.data.shortName);

          const datasetUrl = await runWaveletTransform(waveletConfig, newChartIndex);
          waveDatasetUrl.value=datasetUrl
          continue;

        }

        // 执行模型训练
        try {
          const startRes = await startTrain(childData);
          altaskId.value = startRes.data.data;

          const taskResult = await waitForTaskCompletion(altaskId.value, newChartIndex);

          if (child.data.label !== '小波变换算法') {
            if (taskResult.plotUrl) {
              updateChart(newChartIndex, {
                imageUrl: taskResult.plotUrl,
                loading: false
              });
            }
          }
        } catch (error) {
          console.error('模型训练失败:', error);
          ElMessage.error('模型训练失败');
        }
      }
    }

};

// 统一轮询任务完成的函数
const waitForTaskCompletion = async (taskId, chartIndex) => {
  return new Promise((resolve, reject) => {
    if (timer.value) clearInterval(timer.value);

    let count = 0;
    const totalSteps = 9000;

    timer.value = setInterval(async () => {
      try {
        const res = await getObj(taskId);
        taskState.value = res.data.data.taskState;

        let cappedProgress = Math.min(95, ((count / totalSteps) * 100).toFixed(2));

        if (taskState.value === 2) {
          cappedProgress = 100;
        }

        updateChart(chartIndex, {
          trainingProgress: cappedProgress,
        });

        if (taskState.value === 2) {
          clearInterval(timer.value);
          const result = JSON.parse(res.data.data.taskResult);
          let otherInfo = {}
          if (result.metrics) {
            otherInfo.metrics = result.metrics
            trainMetrics.set(chartData.value[chartIndex].title, result.metrics)
          }

          if (result.datasetInfo)
            otherInfo.datasetInfo = result.datasetInfo
          if(result.datasetUrl){
            otherInfo.datasetUrl = result.datasetUrl
          }
          updateChart(chartIndex, { loading: false, isTraining: 2 ,otherInfo: { ...chartData.value[chartIndex].otherInfo,...otherInfo}});

          resolve(result);
        } else if (count >= totalSteps) {
          ElMessage.warning("任务请求超时");
          updateChart(chartIndex, { loading: false });
          clearInterval(timer.value);
          reject(new Error("任务请求超时"));
        }

        count=count+20;
      } catch (error) {
        console.error("轮询异常:", error);
        clearInterval(timer.value);
        updateChart(chartIndex, { loading: false });
        reject(error);
      }
    }, 2000);
  });
};

// 执行小波变换的方法
const runWaveletTransform = async (waveletConfig, chartIndex) => {
  const wavePacketParams = {
    n: waveletConfig.n || '',
    wavelet: waveletConfig.wavelet || "",
    mode: waveletConfig.mode || "",
    maxlevel: waveletConfig.maxlevel || '',
    slice_length: waveletConfig.sliceLength || '',
  };
  let waveletConfigUrl_shortname=store.getters.getWavePackt
  const childData = {
    ...waveletConfigUrl_shortname,
    taskMsg: JSON.stringify({
      cmsTrainUrl: cmsTrainUrl.value,
      cmsTestUrl: cmsTestUrl.value,
      ...wavePacketParams
    }),
  };

  try {
    const startRes = await startTrain(childData);
    altaskId.value = startRes.data.data;

    chartVisible.value = true  //新加的
    updateChart(chartIndex, { isTraining: 1, progressText: "小波变换中",noPrecess:false });
    const result = await waitForTaskCompletion(altaskId.value, chartIndex);
    store.commit('setWavePacketImage',result.wavePacketImage)
    store.commit('addModelParams', {
      alName: store.getters.getWavePackt.shortName,
      datasetUrl: result.datasetUrl,
      wavePacketImage: result.wavePacketImage
    });
    updateChart(chartIndex, { loading: false, isTraining: 2 , progressText: "",imageUrl:result.wavePacketImage });
    return result.datasetUrl;
  } catch (error) {
    console.error("小波变换失败:", error);
    throw error;
  }
};


// 执行缺失值填充的方法
const runMissingValueFill = async (childData, chartIndex) => {
  try {
    const startRes = await startTrain(childData);
    altaskId.value = startRes.data.data;

    const result = await waitForTaskCompletion(altaskId.value, chartIndex);
    updateChart(chartIndex, {imageUrl: result.plotUrl, loading: false});
    return result;
  } catch (error) {
    console.error("缺失值填充失败:", error);
    throw error;
  }
};

// 转换节点结构
const transformNodes = (rawNodes) => {
  const parents = rawNodes.filter(
      node => node.type === "parent" && node.data.label !== "相关性分析" && node.id !== "1"
  );

  return parents
      .map(parent => {
        const children = rawNodes.filter(
            node => node.type === "child" && node.parent === parent.id
        );
        if (children.length > 0) {
          return { ...parent, children };
        }
        return null;
      })
      .filter(Boolean);
};

// 更新图表方法
const updateChart = (index, updates) => {
  const newData = [...chartData.value];
  newData[index] = { ...newData[index], ...updates };
  chartData.value = newData;
};

const resetView = () => {
  if (vueFlowInstance.value) {
    nodes.value = nodes.value.filter((node) => {
      return node.type === 'parent'
    });
    edges.value = []
    fitView(); // 重置视口为适合所有节点的位置
  }
  chartData.value = []
  chartIndex = -1
  store.commit('setScadaTrainUrl', '');
  store.commit('setScadaTestUrl', '');
  store.commit('setCmsTrainUrl', '');
  store.commit('setCmsTestUrl', '');
  if (timer.value) {
    clearInterval(timer.value);
  }
  if (varSelectRef.value)
    varSelectRef.value.reset()
  if (sideRef.value)
    sideRef.value.reset()
  if (alParamConfigRef.value)
    alParamConfigRef.value.clearModelParams()
  isRunning.value = false
  ElMessage.info('已重置！')
};

const clear = () => {
  if (timer.value) {
    clearInterval(timer.value);
  }
  isRunning.value = false
  chartData.value = []
  chartVisible.value = false
  chartIndex = null
  ElMessage.info('已停止组态训练！')
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

/* 让进度条容器绝对定位，覆盖整个 chart-item */
.progress-container {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;  /* 充满父容器 */
  display: flex;
  align-items: center;
  justify-content: center; /* 水平居中 */
  gap: 8px;
  background-color: rgba(255, 255, 255, 0.7); /* 半透明背景，遮盖图表但不完全隐藏 */
  z-index: 10; /* 保证层级高 */
  pointer-events: none; /* 让进度条不阻塞下层事件，如果需要交互可去掉 */
}

.progress-icon {
  animation: rotating 2s linear infinite;
}
.el-progress {
  width: 100px !important;  /* 缩小进度条宽度 */
}
.el-progress__text {
  color: grey !important;
}


.progress-label {
  font-size: 12px;
  color: var(--el-color-primary);
  margin: 0;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

</style>
