<template>
  <div class="tablecon">
    <!-- 主表格 -->
    <el-table v-if="nodeType === 'MBOM'" :data="tableData" border stripe style="width: 100%" height="auto"
              scroll-x="true">
      <el-table-column type="index" label="序号" width="100" align="center" fixed="left"/>
      <el-table-column prop="type" label="数据集类型" align="center" fixed="left" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'CMS' ? 'primary' : 'success'">
            {{ scope.row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="名称" align="center" width="200">
        <template #default="{ row }">
          {{ row[`${row.type.toLowerCase()}DatasetName`] || "暂无信息" }}
        </template>
      </el-table-column>
      <el-table-column label="提供者" align="center">
        <template #default="{ row }">
          {{ row[`${row.type.toLowerCase()}Provider`] || '暂无信息' }}
        </template>
      </el-table-column>
      <el-table-column label="采集来源" align="center" width="200">
        <template #default="{ row }">
          {{ row[`${row.type.toLowerCase()}Source`] || '暂无信息' }}
        </template>
      </el-table-column>
      <el-table-column label="采集频率" align="center" width="100">
        <template #default="{ row }">
          {{ row[`${row.type.toLowerCase()}CollectionFrequency`] || '暂无信息' }}
        </template>
      </el-table-column>

      <el-table-column label="采集时间" align="center" width="200">
        <template #default="{ row }">
          <span
              v-if="
              row[`${row.type.toLowerCase()}CollectionStartDate`] &&
              row[`${row.type.toLowerCase()}CollectionEndDate`]
            "
          >
            {{ row[`${row.type.toLowerCase()}CollectionStartDate`] }}
            –
            {{ row[`${row.type.toLowerCase()}CollectionEndDate`] }}
          </span>
          <span v-else>暂无信息</span>
        </template>
      </el-table-column>

      <el-table-column label="数据类型" align="center">
        <template #default="{ row }">
          {{ row[`${row.type.toLowerCase()}DataType`] || '暂无信息' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button class="editBtnCustom" @click="handleCheck(scope.row)">详情</el-button>
          <el-button class="editBtnCustom" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 对话框 -->
    <el-dialog
        v-model="datasetInfoVisible"
        title="数据集信息"
        width="50%"
        :before-close="closeDIalog"
    >
      <div class="dialog-content" v-loading="loading" element-loading-text="加载中...">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="名称">{{ formData.datasetName }}</el-descriptions-item>
          <el-descriptions-item label="提供者">{{ formData.provider }}</el-descriptions-item>
          <el-descriptions-item label="采集来源">{{ formData.source }}</el-descriptions-item>
          <el-descriptions-item label="采集频率">{{ formData.collectionFrequency }}</el-descriptions-item>
          <el-descriptions-item label="采集时间" v-if="formData.collectionStartDate && formData.collectionEndDate">
            {{ formData.collectionStartDate }}-{{ formData.collectionEndDate }}
          </el-descriptions-item>
          <el-descriptions-item label="采集时间" v-else></el-descriptions-item>
          <el-descriptions-item label="数据类型">{{ formData.dataType }}</el-descriptions-item>
          <el-descriptions-item label="变量个数">{{ formData.fieldCount }}</el-descriptions-item>
          <el-descriptions-item label="存储大小">{{ formData.storageSize }}</el-descriptions-item>
          <el-descriptions-item label="缺失情况">{{ formData.missingCondition }}</el-descriptions-item>
          <el-descriptions-item label="上传时间" :span="3">{{ formData.uploadTime }}</el-descriptions-item>
          <el-descriptions-item label="训练集地址" :span="2">
            <el-text truncated>{{ formData.trainPath }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="历史版本" :span="2">
            <el-text truncated>{{ formData.previous_trainPath }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="测试集地址" :span="2">
            <el-text truncated>{{ formData.testPath }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="历史版本" :span="2">
            <el-text truncated>{{ formData.previous_testPath }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="训练集长度">{{ formData.trainLen }}</el-descriptions-item>
          <el-descriptions-item label="测试集长度">{{ formData.testLen }}</el-descriptions-item>
        </el-descriptions>
        <br/>
        <el-table :data="varTableData" border style="width: 100%" max-height="400px">
          <el-table-column prop="name" label="数据集变量名" width="300"></el-table-column>
          <!-- 操作列 -->
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button v-if="isSCADA" link type="primary" @click="viewSCADAChart(formData.trainPath,row.name)">
                查看数据
              </el-button>
              <el-button v-else link type="primary" @click="viewCMSChart(row.name)">查看数据</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 图片弹出框 -->
        <el-dialog v-model="chartVisible" width="60%">
          <div ref="chart" style="width: 100%; height: 400px;" v-loading="chartLoading"
               element-loading-text="加载中..."></div>
        </el-dialog>
        <br/>
        <div style="display: flex;justify-content:center;text-align: center; margin: 20px 0;">
          <el-button class="normalBtn" @click="close()"> 关闭</el-button>
        </div>
      </div>
    </el-dialog>


    <el-dialog
        v-model="datasetInfoEditVisible"
        title="编辑数据集信息"
        width="50%"
        :before-close="closeDIalog"
    >
      <div class="dialog-content" v-loading="loading" element-loading-text="加载中...">
        <!-- 数据集信息表单（紧凑型三列布局） -->
        <div class="dataset-form">
          <!-- 第一行 -->
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="名称">
                <el-input v-model="formData.datasetName"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="提供者">
                <el-input v-model="formData.provider"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="采集来源">
                <el-input v-model="formData.source"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="采集频率">
                <el-input v-model="formData.collectionFrequency">
                  <template #append>Hz</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="数据类型">
                <el-input v-model="formData.dataType"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="变量个数">
                <el-input v-model="formData.fieldCount" disabled/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="缺失情况">
                <el-input v-model="formData.missingCondition"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="采集开始日期">
                <el-date-picker
                    v-model="formData.collectionStartDate"
                    type="date"
                    placeholder="选择开始日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="采集结束日期">
                <el-date-picker
                    v-model="formData.collectionEndDate"
                    type="date"
                    placeholder="选择结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%;"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="上传时间">
                <el-input v-model="formData.uploadTime" disabled/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="存储大小">
                <el-input v-model="formData.storageSize">
                  <template #append>MB</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="训练集地址">
                <el-tooltip :content="formData.trainPath" placement="top" :disabled="!formData.trainPath">
                  <el-input v-model="formData.trainPath" class="long-path-input" disabled/>
                </el-tooltip>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="训练集长度">
                <el-input v-model="formData.trainLen" disabled/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="历史版本地址">
                <el-tooltip :content="formData.previous_trainPath" placement="top"
                            :disabled="!formData.previous_trainPath">
                  <el-input v-model="formData.previous_trainPath" class="long-path-input" disabled/>
                </el-tooltip>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="历史版本长度">
                <el-input v-model="formData.previous_trainLen" disabled/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="测试集地址">
                <el-tooltip :content="formData.testPath" placement="top" :disabled="!formData.testPath">
                  <el-input v-model="formData.testPath" class="long-path-input" disabled/>
                </el-tooltip>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="测试集长度">
                <el-input v-model="formData.testLen" readonly disabled/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="历史版本地址">
                <el-tooltip :content="formData.previous_testPath" placement="top"
                            :disabled="!formData.previous_testPath">
                  <el-input v-model="formData.previous_testPath" class="long-path-input" disabled/>
                </el-tooltip>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="历史版本长度">
                <el-input v-model="formData.previous_testLen" readonly disabled/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <br/>
        <el-table :data="varTableData" border style="width: 100%" max-height="400px">
          <el-table-column prop="name" label="数据集变量名" width="300"></el-table-column>
          <!-- 操作列 -->
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button v-if="isSCADA" link type="primary" @click="viewSCADAChart(formData.trainPath,row.name)">
                查看数据
              </el-button>
              <el-button v-else link type="primary" @click="viewCMSChart(row.name)">查看数据</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 图片弹出框 -->
        <el-dialog v-model="chartVisible" width="60%">
          <div ref="chart" style="width: 100%; height: 400px;" v-loading="chartLoading"
               element-loading-text="加载中..."></div>
        </el-dialog>
        <br/>
        <div style="display: flex;justify-content: center;align-items: center">
          <el-button class="normalBtn" @click="dataSubmit()" style="margin-right: 20px ;"> 保存</el-button>
          <el-button class="normalBtn" @click="close()"> 关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {ref, reactive, onMounted, watch, nextTick, computed} from 'vue'
import {ElMessage, ElMessageBox, ElNotification} from 'element-plus'
import {
  putObj,
  addObj,
  delObj,
  getAllByNodeType,
  delModelVar
} from '@/api/sw/model3d/configPerceivedVariable/table.js'
import {fetchList, updateDatasetInfo} from '@/api/DatasetManage/table'
import * as XLSX from "xlsx";
import * as echarts from "echarts";
import JSZip from "jszip";


export default {
  name: 'ConfigSceneModelTable',
  props: {
    nodeType: {
      type: String,
      required: true
    }
  },
  setup(props) {
    // 数据定义
    const tableData = ref([])
    const dialogVisible = ref(false)
    const dialogType = ref('add')
    const formRef = ref(null)
    const currentRow = ref(null)
    const globeParams = reactive({
      curNode: null
    })
    let datasetInfoVisible = ref(false)
    let datasetInfoEditVisible = ref(false)
    const chartVisible = ref(false)
    const formData = ref([])
    const isSCADA = ref(false)
    const varTableData = ref([])
    // 分页配置
    const page = reactive({
      total: 0,
      currentPage: 1,
      pageSize: 10
    })

    // 表单数据
    const form = reactive({
      varId: '',
      varName: '',
      variableType: '',
      dataType: '',
      dimension: '',
      collectionFrequency: 1.0,
      purpose: '',
      nodeId: ''
    })


    let nodeType = props.nodeType
    watch(() => props.nodeType, (newVal) => {
      nodeType = newVal;
    });

    const loadTableData = async () => {
      try {
        if (!globeParams.curNode) return;

        const res = await fetchList(globeParams.curNode.id);
        if (res.data.code === 0) {
          const rawData = res.data.data;
          const commonProps = {}; // 存储非 CMS/SCADA 的公共属性
          const cmsData = {type: 'CMS'}; // 标识 CMS 数据
          const scadaData = {type: 'SCADA'}; // 标识 SCADA 数据

          // 遍历原始数据，分类存储
          Object.keys(rawData).forEach(key => {
            if (key.startsWith('cms')) {
              cmsData[key] = rawData[key]; // CMS 相关数据
            } else if (key.startsWith('scada')) {
              scadaData[key] = rawData[key]; // SCADA 相关数据
            } else {
              commonProps[key] = rawData[key]; // 公共属性
            }
          });

          // 合并公共属性到 CMS 和 SCADA 数据
          Object.assign(cmsData, commonProps);
          Object.assign(scadaData, commonProps);

          // 构建表格数据数组
          tableData.value = [cmsData, scadaData];
        }
      } catch (error) {
        console.error('加载表格数据失败：', error);
        ElMessage.error('加载表格数据失败');
        tableData.value = []; // 出错时清空数据
      }
    };


    let loading = ref(false);
    let knowDict = ref({})
    let traincmsdata = ref([])
    const handleCheck = (row) => {
      datasetInfoVisible.value = true;
      getDatasetInfo(row)
    }
    let global_row = ref('')
    const handleEdit = (row) => {
      datasetInfoEditVisible.value = true
      global_row.value = row
      getDatasetInfo(row)
    }
    const getDatasetInfo = async (row) => {
      let prefix = row.type.toLowerCase()
      // formData.value.provider = row.provider;
      // formData.value.dataSource = row.dataSource;
      formData.value.provider = row[`${prefix}Provider`];
      formData.value.source = row[`${prefix}Source`];
      formData.value.collectionFrequency = row[`${prefix}CollectionFrequency`];
      formData.value.collectionStartDate = row[`${prefix}CollectionStartDate`];
      formData.value.collectionEndDate = row[`${prefix}CollectionEndDate`];
      formData.value.dataType = row[`${prefix}DataType`];
      formData.value.fieldCount = row[`${prefix}FieldCount`];
      formData.value.storageSize = row[`${prefix}StorageSize`];
      formData.value.missingCondition = row[`${prefix}MissingCondition`];
      formData.value.datasetName = row[`${prefix}DatasetName`];
      formData.value.trainPath = row[`${prefix}TrainLatestDataset`];
      formData.value.testPath = row[`${row.type.toLowerCase()}TestLatestDataset`]
      formData.value.previous_trainPath = row[`${row.type.toLowerCase()}TrainPreviousDataset`]
      formData.value.previous_testPath = row[`${row.type.toLowerCase()}TestPreviousDataset`]

      let varNameList = ['']
      isSCADA.value = row.scadaTrainLatestDataset && row.scadaTestLatestDataset;  // 判断是SCADA还是CMS数据集
      // 提取上传时间
      const path = formData.value.trainPath;
      const startIndex = path.indexOf('program/') + 'program/'.length;
      const endIndexFull = path.lastIndexOf('/');
      formData.value.uploadTime = path.slice(startIndex, endIndexFull);
      // 加载数据集信息
      if (isSCADA.value) {
        loading.value = true;
        const [trainVarNameList, trainLen] = formData.value.trainPath
            ? await downloadAndParseCSV1(formData.value.trainPath)
            : [[], 0];

        const [testVarNameList, testLen] = formData.value.testPath
            ? await downloadAndParseCSV1(formData.value.testPath)
            : [[], 0];

        const [previous_trainVarNameList, previous_trainLen] = formData.value.previous_trainPath
            ? await downloadAndParseCSV1(formData.value.previous_trainPath)
            : [[], 0];

        const [previous_testVarNameList, previous_testLen] = formData.value.previous_testPath
            ? await downloadAndParseCSV1(formData.value.previous_testPath)
            : [[], 0];
        if (testLen !== 0) {
          loading.value = false;
        }
        varNameList = trainVarNameList
        if (varNameList.length && varNameList.length !== 0) {
          formData.value.fieldCount = varNameList.length
        }

        formData.value.trainLen = trainLen;
        formData.value.testLen = testLen;
        formData.value.previous_trainLen = previous_trainLen;
        formData.value.previous_testLen = previous_testLen;
        varTableData.value = []
        varTableData.value = varNameList.map(item => ({name: item.label}))
      } else {
        const [traintotalLength, rawTraincmsdata, trainknowDict, trainfaultType] = formData.value.trainPath
            ? await fetchAndUnzip(formData.value.trainPath)
            : [0, [], {}, []];

        const [previous_traintotalLength, previous_rawTraincmsdata, previous_trainknowDict, previous_trainfaultType] = formData.value.previous_trainPath
            ? await fetchAndUnzip(formData.value.previous_trainPath)
            : [0, [], {}, []];

        formData.value.trainLen = traintotalLength;
        formData.value.previous_trainLen = previous_traintotalLength;
        knowDict.value = trainknowDict;
        traincmsdata.value = rawTraincmsdata;

        const [testtotalLength] = formData.value.testPath
            ? await fetchAndUnzip(formData.value.testPath)
            : [0];

        const [previous_testtotalLength] = formData.value.previous_testPath
            ? await fetchAndUnzip(formData.value.previous_testPath)
            : [0];
        formData.value.testLen = testtotalLength;
        formData.value.previous_testLen = previous_testtotalLength;
        varTableData.value = []
        trainfaultType.forEach(item => {
          varTableData.value.push({name: item})
        })
        if (varTableData.value.length && varTableData.value.length !== 0) {
          formData.value.fieldCount = varTableData.value.length
        }
      }
    };

    // 下载并解析CSV文件
    const chartLoading = ref(false);
    const downloadAndParseCSV1 = async (url) => {
      if (!url) {
        ElNotification.error('未获取到SCADA数据集');
        return [[], 0];  // 返回空数组和长度0，防止解构失败
      }

      try {

        const response = await axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'});

        if (!response.data) {
          ElNotification.error('该路径下找不到数据集，请重新获取');
          return [[], 0];
        }

        const workbook = XLSX.read(response.data, {type: 'array'});
        const sheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[sheetName];
        const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1});

        // 过滤并提取列名
        const transferData = jsonData[0]
            .filter(label => label !== 'errorcode')
            .map(label => ({prop: label, label}));

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
        const response = await axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'});

        if (response.data == null) {
          ElNotification.error('该路径下找不到数据集，请重新获取');
        } else {
          const workbook = XLSX.read(response.data, {type: 'array'});
          const sheetName = workbook.SheetNames[0];
          const worksheet = workbook.Sheets[sheetName];
          const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1});
          // 获取对应变量名的列数据
          const variableColumnIndex = jsonData[0].indexOf(variableName);
          const variableData = jsonData.slice(1, 1001).map(row => row[variableColumnIndex]);
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
      const data = await downloadAndParseCSV2(url, variableName);
      if (data.length !== 0) {
        renderChart(data); // 渲染 ECharts 图表
      } else {
        ElMessage.info("没有数据可以展示");
        chartVisible.value = false; // 关闭弹窗
      }
    };

    const viewCMSChart = (name) => {
      chartVisible.value = true;
      chartLoading.value = true
      const key = Object.keys(knowDict.value).find(k => knowDict.value[k].type === name)
      let data = []
      if (key !== undefined) {
        const index = parseInt(key, 10)
        data = traincmsdata.value[index].slice(0, 1024)
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
    const chart = ref(null)
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
          nameLocation: 'middle',
          nameGap: 30,
          data: Array.from({length: data.length}, (_, i) => i + 1), // x 轴是数据点的索引
        },
        yAxis: {
          name: '值',
          nameLocation: 'middle',
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

      myChart.setOption(option);
    };

    const fetchAndUnzip = async (url) => {
      if (url === '' || url === null) {
        ElNotification.error('未获取到CMS数据集');
        return;
      }
      loading.value = true
      const response = await axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'})
      const zip = await JSZip.loadAsync(response.data)
      const folderContents = {}
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
      const keys = Object.keys(folderContents)

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

      loading.value = false
      return [totalLength, Object.values(folderContents), knowDict, types]
    }
    const closeDIalog = () => {
      datasetInfoVisible.value = false
      datasetInfoEditVisible.value = false
      chartVisible.value = false
      formData.value.value = [];
      varTableData.value = []
    }

    const close = () => {
      datasetInfoVisible.value = false
      datasetInfoEditVisible.value = false
      chartVisible.value = false
      formData.value = [];
      varTableData.value = []
    }
    const dataSubmit = async () => {
      let updateFormdata = {}
      let prefix = global_row.value.type.toLowerCase()

      updateFormdata = {
        // provider: formData.value.provider,
        dataSource: formData.value.dataSource,
        [`${prefix}Provider`]: formData.value.provider,
        [`${prefix}Source`]: formData.value.source,
        [`${prefix}CollectionFrequency`]: formData.value.collectionFrequency,
        [`${prefix}DataType`]: formData.value.dataType,
        [`${prefix}FieldCount`]: formData.value.fieldCount,
        [`${prefix}StorageSize`]: formData.value.storageSize,
        [`${prefix}CollectionStartDate`]: formData.value.collectionStartDate,
        [`${prefix}CollectionEndDate`]: formData.value.collectionEndDate,
        [`${prefix}MissingCondition`]: formData.value.missingCondition,
        [`${prefix}DatasetName`]: formData.value.datasetName

      }


      const res = await updateDatasetInfo(globeParams.curNode.id, prefix, updateFormdata)
      if (res.data.code == 0) {
        await loadTableData()
        ElMessage.success('更新成功');
      } else {
        ElMessage.error('更新失败');
      }
      datasetInfoEditVisible.value = false
    }
    // 分页处理
    const handleSizeChange = (val) => {
      page.pageSize = val
      loadTableData()
    }

    const handleCurrentChange = (val) => {
      page.currentPage = val
      loadTableData()
    }

    // 对外暴露的刷新方法
    const getInit = async (node) => {
      globeParams.curNode = node
      if (nodeType === 'MBOM') {
        await loadTableData()
      }
    }

    return {
      // 数据
      tableData,
      dialogVisible,
      dialogType,
      formRef,
      form,
      page,
      datasetInfoVisible,
      chartVisible,
      chartLoading,
      formData,
      isSCADA,
      varTableData,
      loading,
      knowDict,
      traincmsdata,
      chart,
      datasetInfoEditVisible,
      // 方法
      handleCheck,
      handleEdit,
      getDatasetInfo,
      handleSizeChange,
      handleCurrentChange,
      viewSCADAChart,
      viewCMSChart,
      renderChart,
      downloadAndParseCSV1,
      downloadAndParseCSV2,
      getInit,
      close,
      closeDIalog,
      dataSubmit
    }
  }
}
</script>

<style lang="scss" scoped>
.tablecon {
  height: 100%;
  width: 100%;
  overflow-y: hidden;


  .table-header {
    margin-top: 14px;
    margin-bottom: 16px;
    margin-left: 5px;
  }

  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  :deep(.custom-dialog) {
    .el-dialog__body {
      padding: 2px 4px;
    }

    .el-form-item {
      margin-bottom: 20px;
    }

    .dialog-footer {
      padding: 20px 0;
      text-align: right;
    }

    .el-dialog__title {
      font-size: 2em; /* 或者使用具体像素，如 32px，接近 h1 的大小 */
      font-weight: bold; /* h1 默认加粗 */
      line-height: 1.2;
    }
  }

  ::v-deep(.el-table thead) {
    font-weight: bold;
  }
}

.editBtnCustom {
  color: rgba(69, 159, 252); /* 字体蓝色 */
  border: none; /* 去掉边框 */
  background: transparent; /* 背景透明，防止有默认背景 */
  cursor: pointer; /* 鼠标变成手型，更像按钮 */
  padding: 0; /* 根据需要调整内边距 */
}

.delBtnCustom {
  color: rgba(69, 159, 252); /* 字体蓝色 */
  border: none; /* 去掉边框 */
  background: transparent; /* 背景透明，防止有默认背景 */
  cursor: pointer; /* 鼠标变成手型，更像按钮 */
  padding: 0; /* 根据需要调整内边距 */
}

/* 确保单元格内容超出时显示省略号 */
.cell-content {
  width: 100%;
}

.truncate-text {
  white-space: nowrap; /* 禁止换行 */
  overflow: hidden; /* 隐藏溢出内容 */
  text-overflow: ellipsis; /* 显示省略号 */
  max-width: 100%; /* 限制最大宽度 */
  cursor: default; /* 悬浮时显示默认光标（可选） */
}

.dialog-content {
  padding: 20px;
}

.dataset-form {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
}

.el-form-item {
  margin-bottom: 12px;
}

.el-form-item__label {
  font-weight: bold;
  color: #606266;
}

.long-path-input .el-input__inner {
  font-family: monospace;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-col {
    span: 24 !important;
  }

  .long-path-input .el-input__inner {
    font-size: 10px;
  }
}

</style>
