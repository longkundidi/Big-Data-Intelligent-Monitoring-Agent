<!-- DataBar.vue -->
<template>
<div>
  <el-drawer
      v-model="visible"
      title="数据选择(默认使用节点挂载的数据集，请勾选节点)"
      :size="'30%'"
      @close="handleClose"
  >

    <el-form class="selectForm" ref="form" :model="formdata" style="margin-top: 10px; margin-left: 20px;">
      <el-form-item class="selectItem" prop="name">
        <el-select filterable clearable v-model="formdata.projectName" :popper-append-to-body="false"
                   placeholder="请选择场景" @clear="productModelList = [];">
          <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                     @click="formdata.projectName = item.project; findProductModels(item.project)">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item class="selectItem" prop="productModel">
        <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                   placeholder="请选择型号">
          <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                     @click="formdata.productModel = item.productModel; formdata.projectId = item.id;">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button class="normalBtn" @click="search()">查询</el-button>
      </el-form-item>
    </el-form>

    <template>

    </template>

    <div class="searchArea" v-if="treeData.length !== 0">
      <span style="font-size: 15px; margin-right: 10px">搜索组态</span>
      <el-input
          v-model="filterText"
          style="width: 440px"
          placeholder="请输入节点名称"
      />
    </div>

    <el-tree ref="tree" :data="treeData" :accordion="true"
             :props="defaultProps" :expand-on-click-node="true"
             :default-expand-all="false" node-key='id' :default-expanded-keys="defaultExpandKeys"
             :show-checkbox="true" :check-strictly="true" :lazy="false"
             @node-expand="handleNodeExpand" @check-change="handleCheckChange" :filter-node-method="filterNode"
    >
      <template  #default="{ node, data }">
        <div class="custom-tree">
          <span style="line-height: 16px;"> {{ data.name }} </span>
          <div class="tree-btn">
            <span>&nbsp&nbsp&nbsp&nbsp</span>
            <span v-if="isSCADA && data.scadaTestLatestDataset && data.scadaTrainLatestDataset">
            <el-tooltip content="查看数据集信息" placement="top">
              <el-icon class="btn" @click="checkDatasetInfo(node,data)" color="black"><View/></el-icon>
            </el-tooltip>
          </span>
            <span v-if="!isSCADA && data.cmsTrainLatestDataset && data.cmsTestLatestDataset">
            <el-tooltip content="查看数据集信息" placement="top">
              <el-icon class="btn" @click="checkDatasetInfo(node,data)" color="black"><View/></el-icon>
            </el-tooltip>
          </span>
            <span v-if="hasLatestDataset(data)">
              <el-tooltip :content="isLatestDataset(data) ? '当前使用最新数据集' : '当前使用历史数据集'" placement="top">
                <el-icon class="btn" @click="toggleDatasetVersion(data)" color="black">
                  <Switch />
                </el-icon>
              </el-tooltip>
            </span>
          </div>
        </div>
      </template>
    </el-tree>

    <div style="margin-top: 20px;display: flex;flex-direction: column;justify-content: center;align-items: center">
      <!-- 按钮行（移除不必要的 flex 嵌套） -->
      <div style="display: flex; align-items: center;justify-content: center">
        <el-button v-if="isSCADA && !isTrain" class="printBtn" @click="downTemplate">下载模板</el-button>

        <!-- 上传组件（移除 display:contents，改用常规布局） -->
        <el-upload
            :file-list="modeList"
            :http-request="modeUpload"
            :limit="1"
            :show-file-list="false"
            @change="handleFileChange"
            style="margin-left: 30px;"
        >
          <el-button v-if="isTrain" class="addBtn" @click="datasetType='train'" >重新上传训练集</el-button>
          <el-button v-if="isTrain" class="addBtn"  @click="datasetType='test'">重新上传测试集</el-button>
          <el-button v-else class="addBtn" @click="datasetType='test'" >上传数据集</el-button>
        </el-upload>

        <el-button
            class="auditBtn"
            @click="upload"
            style="margin-left: 30px;"
        >
          确定上传
        </el-button>
      </div>

      <!-- 文件名显示区域（固定在按钮正下方） -->
      <div
          v-if="selectedFileName"
          style="
          margin-top: 8px;
          padding: 4px 8px;
          background: #f5f7fa;
          border-radius: 4px;
          color: #409EFF;
          font-size: 12px;
          max-width: 300px;  /* 限制最大宽度 */
          overflow: hidden;  /* 隐藏超出的文本 */
          text-overflow: ellipsis;
          display: flex;
          justify-content: center; /* 水平居中 */
          align-items: center; /* 垂直居中 */
          text-align: center;
    "
      >
        <i class="el-icon-document" ></i>
        {{ selectedFileName }}
        <!-- 取消按钮 -->
        <el-button
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="cancelFile"
            style="margin-left: 10px; padding: 0; font-size: 12px; color: #409EFF;"
        >
        </el-button>
      </div>
    </div>
    <!-- 对话框 -->
    <el-dialog
        v-model="datasetInfoVisible"
        title="数据集信息"
        width="50%"
        :before-close="closeDIalog"
    >
      <div class="dialog-content" v-loading="loading" element-loading-text="加载中...">
        <el-table :data="datasetTableData" border style="width: 100%">
          <el-table-column prop="name" label="数据集信息" width="300"></el-table-column>
          <el-table-column prop="value" label="值"></el-table-column>
        </el-table>
        <br/>
        <el-table :data="varTableData" border style="width: 100%" max-height="400px" >
          <el-table-column prop="name" label="变量名" width="300"></el-table-column>
          <!-- 操作列 -->
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button v-if="isSCADA" link type="primary" @click="viewSCADAChart(tableData.trainPath,row.name)">查看数据</el-button>
              <el-button v-else link type="primary" @click="viewCMSChart(row.name)">查看数据</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 图片弹出框 -->
        <el-dialog v-model="chartVisible" width="60%">
          <div ref="chart" style="width: 100%; height: 400px;" v-loading="chartLoading" element-loading-text="加载中..."></div>
        </el-dialog>
        <br/>
        <div style="display: flex;justify-content:center;text-align: center; margin: 20px 0;"><el-button class="normalBtn"  @click="close()"> 关闭 </el-button></div>
      </div>
    </el-dialog>
  </el-drawer>
</div>
</template>

<script setup>
import {ref, watch, computed , defineExpose ,nextTick} from 'vue';
import {ElDrawer, ElMessage, ElNotification} from 'element-plus'; // 使用 Element Plus 的 Drawer 组件
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import { useStore } from 'vuex';
import * as XLSX from "xlsx";
import * as echarts from 'echarts';
import {getProductModels, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
import {reqTreeNodes, reqSonNodes} from "@/api/diagnosis/graphVis/dataIngestion";
import JSZip from 'jszip'
import {Switch, View} from "@element-plus/icons-vue";
import {getAllSceneMetaModels} from "@/api/diagnosis/scene/scene";

// 父组件传递的节点信息
const props = defineProps({
  tableData: Array,
  visible: Boolean,
  isSCADA: Boolean,
  isTrain: Boolean
});
const emit = defineEmits(["update:visible", "update:modelObject"]);
const store = useStore();
// 控制抽屉显示
const visible = ref(props.visible);
const formdata = ref({
  projectName: '',
  productModel: '',
  projectId: ''
});
const productModelList = ref([]);
const projectList = ref([]);
const filterText = ref('');
const treeData = ref([]);
const defaultProps = ref({
  children: 'children',
  label: 'name',
  isLeaf: 'leaf'
});
const defaultExpandKeys = ref([]);
const expandKeys = ref([]);
let tree = ref(null);  // 生成一个懒加载树的代理对象
const chart = ref(null)
const globeParams = ref({ nodeLevel: 3 });  // 你可以根据需求初始化

const modelObject = ref({
  modelObject: '',
  objectId: '',
});

const showTooltip=ref(true)
const datasetInfoVisible=ref(false)
const chartVisible=ref(false)
const chartData = ref(null);
// 1. 用 ref 存储可修改的原始数据
const tableData = ref({
  provider: '',
  uploadTime: '',
  trainPath: '',
  testPath: '',
  trainLen: '',
  testLen: '',
});
const datasetTableData =computed(() => [
  { name: '提供者', value: tableData.value.provider },
  { name: '上传时间', value: tableData.value.uploadTime },
  { name: '训练集地址', value: tableData.value.trainPath },
  { name: '测试集地址', value:tableData.value.testPath },
  { name: '训练集长度', value:tableData.value.trainLen },
  { name: '测试集长度', value:tableData.value.testLen },
])

const varTableData =ref( [])
// 同步父组件的 visible 变化
watch(() => props.visible, (newVal) => {
  visible.value = newVal;
});

// 同步visible 的变化到父组件
watch(visible, (val) => {
  emit('update:visible', val);
  if (val === true) {
    // 拿已选中节点执行处理
    if (tree.value) {
      const checkedKeys = tree.value.getCheckedKeys() || [];
      if (checkedKeys.length > 0) {
        const data = tree.value?.getNode(checkedKeys[0])?.data;
        if (data) {
          handleCheckChange(data, true, false);
        }
      }
    }
  }
});

watch(filterText, (val) => {
  if (tree.value) {
    tree.value.filter(val)
  }
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.name.toLowerCase().indexOf(value.toLowerCase()) !== -1;
}

const isSCADA = computed(() => props.isSCADA);
const datasetType=ref('')
const isCheck = ref(false)

const projectInit = async () => {
  //获取项目
  const userId = store.state.user.userInfo.userId
  try {
    const response = await getAllSceneMetaModels({userId: userId, userRole: 'ROLE_SYS'});
    if (response.data.code === "200" || response.data.code === 200) {
      const res = response.data.data;
      // 去重并赋值给 projectList
      projectList.value = res.filter((item, index, arr) =>
          index === arr.findIndex((t) => t.project === item.project)
      );
    }
  } catch (error) {
    console.error('获取项目失败', error);
  }
}

const findProductModels = (proName) => {
  //切换项目的时候，需要初始化值
  treeData.value = []
  formdata.value.productModel = '';
  getProductModels(proName).then(response => {
    productModelList.value = response.data.data;
  })
}

const search = () => {
  //查询
  treeData.value = []
  getTreeNodes(globeParams.value.nodeLevel)
}

// 节点扩展消息响应
const handleNodeExpand = async (data, node, treeNode) => {
  if (node.level >= globeParams.value.nodeLevel) {
    const response = await reqSonNodes(formdata.value.projectId, data.nodeCode);
    if (response.data.data) {
      response.data.data.forEach((item) => {
        item.children = item.leaf ? [] : [{ id: 'loading', name: '节点加载中...' }];
      });
      node.data.children = response.data.data;
    }
  }
};

// 设置节点勾选为单选（一次只能勾选一个节点）
const handleCheckChange = (data, checked, indeterminate) => {
  if (!checked) return;

  const isScada = isSCADA.value;
  const typeLabel = isScada ? 'SCADA' : 'CMS';

  // 判断当前是否使用最新数据集，默认使用最新
  const useLatest = isLatestDataset(data);

  // 根据当前版本选择对应字段
  let trainDataset, testDataset;
  if (isScada) {
    trainDataset = useLatest ? data.scadaTrainLatestDataset : data.scadaTrainPreviousDataset;
    testDataset = useLatest ? data.scadaTestLatestDataset : data.scadaTestPreviousDataset;
  } else {
    trainDataset = useLatest ? data.cmsTrainLatestDataset : data.cmsTrainPreviousDataset;
    testDataset = useLatest ? data.cmsTestLatestDataset : data.cmsTestPreviousDataset;
  }

  // 校验数据集完整性
  if (!trainDataset && !testDataset) {
    ElMessage.warning(`${typeLabel}数据集不存在，请先上传!`);
    return;
  }
  if (!trainDataset) {
    ElMessage.warning(`${typeLabel}训练集不存在，请先上传!`);
    return;
  }
  if (!testDataset) {
    ElMessage.warning(`${typeLabel}测试集不存在，请先上传!`);
    return;
  }

  if (checked) {
    isCheck.value = checked;
    tree.value.setCheckedKeys([data.id]);
    tree.value.setCurrentKey(data.id);

    if (isScada) {
      store.commit('setScadaTrainUrl', trainDataset);
      store.commit('setScadaTestUrl', testDataset);
    } else {
      store.commit('setCmsTrainUrl', trainDataset);
      store.commit('setCmsTestUrl', testDataset);
    }

    modelObject.value.modelObject = data.name;
    modelObject.value.objectId = data.id;
    emit('update:modelObject', modelObject.value);
  }
};

// 根据节点层级数，加载结构树的一组节点
const getTreeNodes = async (nodeLevel) => {
  try {
    const response = await reqTreeNodes(formdata.value.projectId, nodeLevel);
    if (response.data.data) {
      treeData.value = [];
      expandKeys.value = [];  // 缓存待扩展的节点
      const rootNodes = response.data.data.filter(ele => ele.nodeType === 'Root' || ele.nodeType === 'Root-Leaf');
      for (let item of rootNodes) {
        treeData.value.push(item);
        expandKeys.value.push(item.id);
        await setChildren(item, response.data.data);
      }
      defaultExpandKeys.value = expandKeys.value;  // 扩展节点
    }
  } catch (error) {
    console.error(error);
  }
};

// 递归查询节点 pNode 的全部子节点，并装配成 el-tree 的数据结构
const setChildren = (pNode, nodeList) => {
  const res = getChildrenByNodeCode(pNode.nodeCode, nodeList);
  const children = res.sonNodes;
  if (children.length === 0) {
    if ((pNode.nodeType === 'Mid') || (pNode.nodeType === 'Root')) {  // 如果不是叶子节点，节点前显示 "+" 号
      expandKeys.value = expandKeys.value.filter(item => item !== pNode.id);  // 从扩展节点中删除它
      pNode.children = [{ id: 'loading', name: '节点加载中...' }];
    }
    return pNode;
  } else {
    pNode.children = children;
    children.forEach((item) => {
      expandKeys.value.push(item.id);  // 添加到扩展节点
      setChildren(item, res.otherNodes);
    });
  }
};

// 正则表达式，根据节点编码，查找它的下一层子节点
const getChildrenByNodeCode = (pNodeCode, nodeList) => {
  const sonNodes = [];
  const otherNodes = [];
  const regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$');
  nodeList.forEach((item) => {
    if (regex.test(item.nodeCode)) {  // 正则表达式判定 item 是不是 pNodeCode 的下一层子节点

      sonNodes.push(item);
    } else {
      otherNodes.push(item);
    }
  });
  return { sonNodes: sonNodes, otherNodes: otherNodes };
};

const getTree = () => {
  projectInit()
}

const loading = ref(false);
const knowDict=ref({})
const traincmsdata=ref([])
const checkDatasetInfo=async (node, data) => {
  datasetInfoVisible.value = true

  const latest = isLatestDataset(data)
  let varNameList = ['']

  if (isSCADA.value) {
    tableData.value.provider = data.scadaProvider
    if (latest) {
      tableData.value.trainPath = data.scadaTrainLatestDataset
      tableData.value.testPath = data.scadaTestLatestDataset
    } else {
      tableData.value.trainPath = data.scadaTrainPreviousDataset
      tableData.value.testPath = data.scadaTestPreviousDataset
    }

    let path = tableData.value.trainPath
    const startIndex = path.indexOf('program/') + 'program/'.length;
    const endIndexFull = path.lastIndexOf('/');
    tableData.value.uploadTime = path.slice(startIndex, endIndexFull);

    loading.value = true
    const [trainVarNameList, trainLen] = await downloadAndParseCSV1(tableData.value.trainPath)
    const [testVarNameList, testLen] = await downloadAndParseCSV1(tableData.value.trainPath)
    if(testLen!==0){
      loading.value = false
    }
    varNameList = trainVarNameList
    tableData.value.trainLen = trainLen
    tableData.value.testLen = testLen
    varTableData.value = []
    varTableData.value=varNameList.map(item=>({name:item.label}))

  } else {
    tableData.value.provider = data.cmsProvider
    if (latest) {
      tableData.value.trainPath = data.cmsTrainLatestDataset
      tableData.value.testPath = data.cmsTestLatestDataset
    } else {
      tableData.value.trainPath = data.cmsTrainPreviousDataset
      tableData.value.testPath = data.cmsTestPreviousDataset
    }

    const [traintotalLength,rawTraincmsdata,trainknowDict,trainfaultType]=await fetchAndUnzip(tableData.value.trainPath)
    tableData.value.trainLen = traintotalLength
    knowDict.value=trainknowDict

    traincmsdata.value=rawTraincmsdata
    const [testtotalLength,rawTestcmsdata,testknowDict,testfaultType]=await fetchAndUnzip(tableData.value.trainPath)
    tableData.value.testLen = testtotalLength
    varTableData.value = []
    trainfaultType.forEach(item=>{
      varTableData.value.push({name:item})
    })

    let path = tableData.value.trainPath
    const startIndex = path.indexOf('program/') + 'program/'.length;
    const endIndexFull = path.lastIndexOf('/');
    tableData.value.uploadTime = path.slice(startIndex, endIndexFull);
  }
}

const chartLoading = ref(false);
const downloadAndParseCSV1 = async (url) => {
  if (!url) {
    ElNotification.error('未获取到SCADA数据集');
    return [[], 0];  // 返回空数组和长度0，防止解构失败
  }

  try {

    const response = await axios.get(`/al/file/${url}`, { responseType: 'arraybuffer' });

    if (!response.data) {
      ElNotification.error('该路径下找不到数据集，请重新获取');
      return [[], 0];
    }

    const workbook = XLSX.read(response.data, { type: 'array' });
    const sheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[sheetName];
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });

    // 过滤并提取列名
    const transferData = jsonData[0]
        .filter(label => label !== 'errorcode')
        .map(label => ({ prop: label, label }));

    const dataLength = jsonData.length - 1;
    return [transferData, dataLength];
  } catch (error) {
    console.error('Error fetching and parsing file:', error);
    return [[], 0];
  }
};

// 下载和解析 CSV 数据
const downloadAndParseCSV2 = async (url, variableName) => {
  if (url === '' || url === null) {
    ElNotification.error('未获取到SCADA数据集');
    return;
  }

  try {
    chartLoading.value = true
    const response = await axios.get(`/al/file/${url}`, { responseType: 'arraybuffer' });

    if (response.data == null) {
      ElNotification.error('该路径下找不到数据集，请重新获取');
    } else {
      const workbook = XLSX.read(response.data, { type: 'array' });
      const sheetName = workbook.SheetNames[0];
      const worksheet = workbook.Sheets[sheetName];
      const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
      // 获取对应变量名的列数据
      const variableColumnIndex = jsonData[0].indexOf(variableName);
      const variableData = jsonData.slice(1,1001).map(row => row[variableColumnIndex]);

      chartLoading.value = false
      return variableData;
    }
  } catch (error) {
    console.error('Error fetching and parsing file:', error);
  }
};

// 点击查看图表
const viewSCADAChart = async (url, variableName) => {
  chartVisible.value = true;
  const data= await downloadAndParseCSV2(url, variableName);
  if (data.length!==0) {
    renderChart(data); // 渲染 ECharts 图表
  } else {
    ElMessage.info("没有数据可以展示");
    chartVisible.value = false; // 关闭弹窗
  }
};

const viewCMSChart =  (name)=> {
  chartVisible.value = true;
  chartLoading.value = true
  const key=Object.keys(knowDict.value).find(k=>knowDict.value[k].type===name)
  let data=[]
  if(key!==undefined){
    const index=parseInt(key,10)
    data=traincmsdata.value[index].slice(0,1024)
}
  if (data.length !== 0) {
    nextTick(() => {
      renderChart(data);
      chartLoading.value = false;
    });
  } else {
    ElMessage.info("没有数据可以展示");
    chartVisible.value = false; // 关闭弹窗
  }
}

// 渲染 ECharts 图表
const renderChart = (data) => {
  const chartElement = chart.value
  let myChart = echarts.getInstanceByDom(chartElement);
  if (!myChart) {
    myChart = echarts.init(chartElement);
  }

  // 设置折线图配置
  const option = {
    title: {
      text: '数据可视化',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      name: '数据点',
      type: 'category',
      nameLocation:'middle',
      nameGap: 30,
      data: Array.from({ length: data.length }, (_, i) => i + 1), // x 轴是数据点的索引
    },
    yAxis: {
      name:'值',
      nameLocation:'middle',
      nameGap: 30,
      type: 'value',
    },
    series: [
      {
        data: data,
        type: 'line',
        smooth: true, // 平滑曲线
      },
    ],
  };

  myChart .setOption(option);
};

const fetchAndUnzip=async (url)=> {
  loading.value=true
  const response = await axios.get(`/al/file/${url}`, { responseType: 'arraybuffer' })
  const zip = await JSZip.loadAsync(response.data)
  const folderContents  = {}
  for (const relativePath in zip.files) {
    const file = zip.files[relativePath]
    if (!file.dir && relativePath.endsWith('.txt')) {
      // 是 txt 文件，读取内容
      const content = await file.async('string')
      const lines = content.split(/\r?\n/)
      const contentWithoutFirstLine = lines.slice(1).join('\n')
    // 按空格分割，过滤空字符串
      const arr = contentWithoutFirstLine
          .split(/\s+/)
          .filter(s => s.length > 0)
          .map(s => parseInt(s, 10))
      // 提取文件夹路径（去掉最后的文件名）
      const folderPath = relativePath.includes('/')
          ? relativePath.substring(0, relativePath.lastIndexOf('/'))
          : ''  // 根目录文件放到 '' 分组

      // 初始化对应文件夹数组
      if (!folderContents[folderPath]) {
        folderContents[folderPath] = []
      }
      // 合并当前文件数组
      folderContents[folderPath].push(...arr)
    }
  }
  let totalLength = 0
  for (const key in folderContents) {
    totalLength += folderContents[key].length
  }
  const keys=Object.keys(folderContents)

  let jsonFile = null
  for (const relativePath in zip.files) {
    if (!zip.files[relativePath].dir && relativePath.endsWith('label_map.json')) {
      jsonFile = zip.files[relativePath]
      break
    }
  }

  if (!jsonFile) {
    throw new Error('压缩包内未找到 label_map.json')
  }

  const jsonStr = await jsonFile.async('string')
  const jsonData = JSON.parse(jsonStr)

  // 提取所有 keys 对应的 type
  const types = []
  const knowDict = jsonData.know_dict || {}

  for (const key in keys) {
    if (knowDict[key] && knowDict[key].type) {
      types.push(knowDict[key].type)
    }
  }

  loading.value=false
  return [totalLength,Object.values(folderContents),knowDict,types]
}
const closeDIalog=()=>{
  datasetInfoVisible.value=false
  chartVisible.value=false
  tableData.value = [ ];
  varTableData.value =[ ]
}

const close=()=>{
  datasetInfoVisible.value=false
  chartVisible.value=false
  tableData.value = [];
  varTableData.value =[ ]
}
const reset = () => {
  formdata.value = {
    projectName: '',
    productModel: '',
    projectId: ''
  }
  treeData.value = []
  filterText.value = ''
}

defineExpose({
  getTree,
  reset
})

// 关闭抽屉
const handleClose = () => {
  visible.value = false;
};

const mode = ref({});
const modeList = ref([]);
const datasetUrl = ref('');

const modeUpload = (item) => {
  mode.value = item.file;
};
// 取消文件选择
const cancelFile = () => {
  selectedFileName.value = ''; // 清除文件名
  modeList.value = []; // 清空文件列表
};
const downTemplate = () => {

  // 提取所有 varName 作为表头
  const headers = props.tableData.map((item) => item.varName);
  headers.push("errorcode");

  // 创建一个空的数据行（可以根据需求填充数据）
  const dataRows = [
    headers.map(() => ""), // 生成一行空数据
  ];

  const worksheetData = [headers, ...dataRows];
  const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);

  // 创建工作簿并添加工作表
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, "Sheet1");

  // 导出 Excel 文件
  XLSX.writeFile(workbook, "template_SCADA.xlsx");

}

const selectedFileName = ref(''); // 用于保存上传的文件名

// 模拟上传组件的文件变化处理
const handleFileChange = (file, fileList) => {
  selectedFileName.value = file.name; // 将文件名赋值给 selectedFileName
};

// Function to handle upload action
const upload = () => {
  selectedFileName.value=''
  const file = new FormData();
  if (mode.value.uid === undefined) {
    ElNotification({
      title: 'Warning',
      message: '没有数据集',
      type: 'warning',
    });
  } else {
    file.append('file', mode.value);
     uploadProgram(file)
        .then((response) => {
          ElNotification({
            title: 'Success',
            message: '上传成功',
            type: 'success',
          });
          modeList.value = [];
          mode.value = {};
          datasetUrl.value = response.data.data;

          if(isSCADA.value){
            if(datasetType.value === 'train'){
              store.commit('setScadaTrainUrl', datasetUrl.value);
            }else{
              store.commit('setScadaTestUrl', datasetUrl.value);
            }
          }else{
            if(datasetType.value === 'train'){
              store.commit('setCmsTrainUrl', datasetUrl.value);
            }else{
              store.commit('setCmsTestUrl', datasetUrl.value);
            }
          }
        })
        .catch(() => {
          ElNotification({
            title: 'Warning',
            message: '上传失败',
            type: 'warning',
          });
        });
  }
};

const datasetVersionMap = ref({})

const hasLatestDataset = (data) => {
  if (!data) return false;
  if (isSCADA.value) {
    return data.scadaTrainLatestDataset && data.scadaTestLatestDataset;
  } else {
    return data.cmsTrainLatestDataset && data.cmsTestLatestDataset;
  }
};

const toggleDatasetVersion = (data) => {
  // 当前状态（默认最新）
  const currentIsLatest = datasetVersionMap.value[data.id] !== false;

  // 目标状态是切换后的版本
  const targetIsLatest = !currentIsLatest;

  // 根据 isSCADA 判断对应的数据集字段
  let trainField, testField;
  if (isSCADA.value) {
    trainField = targetIsLatest ? 'scadaTrainLatestDataset' : 'scadaTrainPreviousDataset';
    testField = targetIsLatest ? 'scadaTestLatestDataset' : 'scadaTestPreviousDataset';
  } else {
    trainField = targetIsLatest ? 'cmsTrainLatestDataset' : 'cmsTrainPreviousDataset';
    testField = targetIsLatest ? 'cmsTestLatestDataset' : 'cmsTestPreviousDataset';
  }

  // 判断目标版本是否存在完整数据集
  if (!data[trainField] || !data[testField]) {
    ElMessage.warning(`切换失败，${targetIsLatest ? '最新' : '历史'}数据集不存在！`);
    return;
  }

  // 设置切换状态
  datasetVersionMap.value[data.id] = targetIsLatest;

  // 同步更新 store 里的数据集地址
  if (isSCADA.value) {
    store.commit('setScadaTrainUrl', data[trainField]);
    store.commit('setScadaTestUrl', data[testField]);
  } else {
    store.commit('setCmsTrainUrl', data[trainField]);
    store.commit('setCmsTestUrl', data[testField]);
  }
}


// 获取当前节点是否显示最新版本（默认true）
const isLatestDataset = (data) => {
  if (!data) return true
  return datasetVersionMap.value[data.id] !== false  // undefined 或 true 都视为最新
}
</script>

<style lang="scss" scoped>
.searchArea{
  margin-top: 20px;
  margin-bottom: 10px;
  margin-left: 20px;
  align-items: center;
  display: flex;
}

:deep(.el-tree-node__expand-icon svg) {
  display: none !important;
}

:deep(.el-tree-node__expand-icon) {
  font-size: 16px; /*图标大小*/
}

:deep(.el-tree-node__expand-icon):before {
  content: "";
  background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.el-tree-node__expand-icon.expanded):before {
  content: "";
  background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.el-tree-node__expand-icon.is-leaf) {
  display: none !important;
}

:deep(.el-tree-node__content:hover) { /*鼠标滑过，修改背景色*/
  color: cyan;
  font-weight: bold;
  background-color: rgb(108, 108, 111) !important;
}

:deep(.el-tree-node:focus > .el-tree-node__content) { /*节点选中，节点获取焦点*/
  color: cyan;
  font-weight: bold;
  background-color: rgba(138, 194, 252, 0.53) !important;
}

.custom-tree{
  display: flex;
  height: 100%;
  width: 100%;
  align-items: center;            /*垂直对齐*/
  .tree-btn{
    display: flex;
    height: 100%;
    width: 100%;
    align-items: center;            /*垂直对齐*/
    .btn{
      height: 100%;
      width: 30px;
    }
  }
}

.selectForm {
  display: flex;
  text-align: center;
  align-items: center;
}

.selectItem {
  width: 300px;
  margin-right: 10px !important;
}

::v-deep .el-drawer__header {
  color: #333; /* 修改字体颜色 */
  font-weight: bold; /* 修改字体加粗 */
  margin-bottom: 0;
}

</style>
