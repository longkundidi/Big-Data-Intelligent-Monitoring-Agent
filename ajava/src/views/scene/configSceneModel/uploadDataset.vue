<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="60%"
      :show-close="true"
      class="el-dialog__header high-dialog"
      @close="closeDialog()"
  >
    <div slot="title" style="display: flex;padding-bottom: 15px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>
      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">上传数据集</span>
      </div>
    </div>

    <div class="app-container">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="选择数据类型"></el-step>
        <el-step title="上传数据集"></el-step>
        <el-step v-if="datasetType === 'SCADA'" title="感知变量匹配"></el-step>
      </el-steps>
    </div>

    <div v-if="active===0">
      <div style="margin-top: 5%; margin-bottom: 2%; display: flex; flex-direction: column; align-items: center;">
        <el-form :model="form" label-width="auto" style="max-width: 600px; width: 100%;">
          <el-form-item label="数据集名称">
            <el-input v-model="form.uploadDatasetName" placeholder="请输入数据集名称"/>
          </el-form-item>
          <el-form-item label="提供者">
            <el-input v-model="form.provider" placeholder="请输入提供者姓名"/>
          </el-form-item>
        </el-form>

        <!--        <div style="margin-bottom: 15px;">请选择要上传的数据集类型</div>-->

        <el-space alignment="center" :size="40" direction="horizontal">
          <div
              style="display: flex; align-items: center; padding: 12px 20px; background-color: #f5f7fa; border-radius: 8px;">
            <el-radio v-model="datasetType" label="CMS">上传高采样数据</el-radio>
            <el-button type="primary" size="small" @click="downloadTemplate('CMS')" style="margin-left: 12px;">
              下载模板
            </el-button>
          </div>

          <div
              style="display: flex; align-items: center; padding: 12px 20px; background-color: #f5f7fa; border-radius: 8px;">
            <el-radio v-model="datasetType" label="SCADA">上传低采样数据</el-radio>
            <el-button type="primary" size="small" @click="downloadTemplate('SCADA')" style="margin-left: 12px;">
              下载模板
            </el-button>
          </div>
        </el-space>
      </div>
    </div>

    <div v-if="active===1">
      <div v-loading="uploadLoading" element-loading-text="正在上传...">
        <div class="upload-dataset-page">
          <div class="dataset-section">
            <h2>
              上传
              <template v-if="datasetType === 'CMS'">高采样</template>
              <template v-else-if="datasetType === 'SCADA'">低采样</template>
              训练集
            </h2>
            <el-table
                :data="trainVersions"
                border
                style="width: 100%"
                empty-text="无可用版本"
            >
              <el-table-column prop="label" label="版本" width="100"/>
              <el-table-column prop="datasetName" label="数据集"/>
              <el-table-column prop="provider" label="提供者"/>
              <el-table-column prop="uploadTime" label="上传时间"/>
              <el-table-column label="预览" width="100">
                <template #default="scope">
                  <el-button size="small" @click="previewDataset(scope.row)">预览</el-button>
                </template>
              </el-table-column>
              <el-table-column label="是否替换版本" width="180">
                <template #default="scope">
                  <el-radio
                      v-model="selectedReplaceTrainVersion"
                      :label="scope.row.label"
                      :disabled="trainVersions.length < 2"
                      @click.native="handleTrainRadioClick(scope.row.label)"
                  >
                    替换此版本
                  </el-radio>
                </template>
              </el-table-column>
            </el-table>
            <!-- 文件名显示区域（固定在按钮正下方） -->
            <div
                v-if="selectedFileNameTrain"
                style="
          margin-top: 8px;
          padding: 4px 8px;
          background: #f5f7fa;
          border-radius: 4px;
          color: #409EFF;
          font-size: 12px;
          max-width: 300px;  /* 限制最大宽度 */
          overflow: hidden;  /* 隐藏超出的文本 */
          display: flex;
          justify-content: center; /* 垂直居中 */
          align-items: center; /* 垂直居中 */
    "
            >
              <i class="el-icon-document"></i>
              <span
                  style="
            flex: 1;                  /* 占据剩余空间 */
            white-space: nowrap;      /* 禁止换行 */
            overflow: hidden;         /* 隐藏溢出 */
            text-overflow: ellipsis;  /* 显示省略号 */
            padding: 0 4px;           /* 添加间距 */
            "
              >
      {{ selectedFileNameTrain }}
        </span>
              <!-- 取消按钮 -->
              <el-button size="mini" type="text" icon="el-icon-close" @click="cancelFile('train')"
                         style="margin-left: 10px; padding: 0; font-size: 12px; color: #409EFF;"></el-button>
            </div>
            <div class="upload-btn" style="margin-top: 15px;">
              <el-upload
                  :file-list="modeListTrain"
                  :http-request="modeUploadTrain"
                  :limit="1"
                  :show-file-list="false"
                  @change="handleFileChangeTrain"
                  style="margin-left: 30px;"
              >
                <el-button type="primary"
                           :disabled="trainVersions.length >= 2 && !selectedReplaceTrainVersion"
                           @click="datasetTypeTrain = (datasetType === 'CMS' ? 'CMS_train' : (datasetType === 'SCADA' ? 'SCADA_train' : ''))">
                  上传训练集
                </el-button>

              </el-upload>
            </div>
          </div>

          <div class="dataset-section" style="margin-top: 40px;">
            <h2>
              上传
              <template v-if="datasetType === 'CMS'">高采样</template>
              <template v-else-if="datasetType === 'SCADA'">低采样</template>
              测试集
            </h2>
            <el-table
                :data="testVersions"
                border
                style="width: 100%"
                empty-text="无可用版本"
            >
              <el-table-column prop="label" label="版本" width="100"/>
              <el-table-column prop="datasetName" label="数据集"/>
              <el-table-column prop="provider" label="提供者"/>
              <el-table-column prop="uploadTime" label="上传时间"/>
              <el-table-column label="预览" width="100">
                <template #default="scope">
                  <el-button size="small" @click="previewDataset(scope.row)">预览</el-button>
                </template>
              </el-table-column>
              <el-table-column label="是否替换版本" width="180">
                <template #default="scope">
                  <el-radio
                      v-model="selectedReplaceTestVersion"
                      :label="scope.row.label"
                      :disabled="testVersions.length < 2"
                      @click.native="handleTestRadioClick(scope.row.label)"
                  >
                    替换此版本
                  </el-radio>
                </template>
              </el-table-column>
            </el-table>
            <!-- 文件名显示区域（固定在按钮正下方） -->
            <div
                v-if="selectedFileNameTest"
                style="
          margin-top: 8px;
          padding: 4px 8px;
          background: #f5f7fa;
          border-radius: 4px;
          color: #409EFF;
          font-size: 12px;
          max-width: 300px;  /* 限制最大宽度 */
          overflow: hidden;  /* 隐藏超出的文本 */
          display: flex;
          justify-content: center; /* 垂直居中 */
          align-items: center; /* 垂直居中 */
    "
            >
              <i class="el-icon-document"></i>
              <span
                  style="
            flex: 1;                  /* 占据剩余空间 */
            white-space: nowrap;      /* 禁止换行 */
            overflow: hidden;         /* 隐藏溢出 */
            text-overflow: ellipsis;  /* 显示省略号 */
            padding: 0 4px;           /* 添加间距 */
            "
              >
      {{ selectedFileNameTest }}
        </span>
              <!-- 取消按钮 -->
              <el-button size="small" type="text" icon="el-icon-close" @click="cancelFile('test')"
                         style="margin-left: 10px; padding: 0; font-size: 12px; color: #409EFF;"></el-button>
            </div>
            <div class="upload-btn" style="margin-top: 15px;">
              <el-upload
                  :file-list="modeListTest"
                  :http-request="modeUploadTest"
                  :limit="1"
                  :show-file-list="false"
                  @change="handleFileChangeTest"
                  style="margin-left: 30px;"
              >
                <el-button type="primary"
                           :disabled="testVersions.length >= 2 && !selectedReplaceTestVersion"
                           @click="datasetTypeTest = (datasetType === 'CMS' ? 'CMS_test' : (datasetType === 'SCADA' ? 'SCADA_test' : ''))">
                  上传测试集
                </el-button>

              </el-upload>
            </div>

          </div>
        </div>
      </div>
    </div>

    <div v-if="active === 2" class="variable-match-page">
      <div style="max-width: 600px; margin: 20px auto 20px auto;">
        <el-button type="primary" @click="startAutoMatch" :disabled="matching">开始匹配</el-button>
      </div>
      <div v-loading="uploadLoading" element-loading-text="正在上传...">
        <el-table :data="matchTable" border style="width: 100%; max-width: 600px; margin: 0 auto;"
                  :row-style="() => ({ height: '40px' })">
          <el-table-column prop="variable" label="变量"></el-table-column>
          <el-table-column label="匹配状态">
            <template #default="scope">
              <span v-if="scope.row.status === 'unmatched'">未匹配</span>
              <span v-else-if="scope.row.status === 'success'" style="color:#4db64d">匹配成功</span>
              <span v-else>匹配失败
              <span style="color: #409EFF; cursor: pointer; font-size: 12px;"
                    @click="openManualMatch(scope.row)">手动匹配</span>
            </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 手动匹配弹窗 -->
      <el-dialog v-model="manualMatchDialogVisible" title="手动匹配">
        <div style="padding: 10px 20px;">
          <!-- 待匹配变量 -->
          <div style="margin-bottom: 16px; font-size: 16px;">
            <span style="font-weight: bold; color: #606266;">待匹配变量：</span>
            <span style="color: #409EFF;">{{ manualMatchTarget }}</span>
          </div>

          <!-- 可选变量标题 -->
          <p style="font-weight: bold; margin-bottom: 12px; font-size: 15px;">选择要匹配的变量：</p>

          <!-- 单选框区域 -->
          <el-radio-group v-model="manualMatchSelected" size="large">
            <div style="display: flex; flex-wrap: wrap; gap: 10px;">
              <el-radio
                  v-for="item in manualMatchCandidates"
                  :key="item"
                  :label="item"
                  border
                  style="flex: 0 1 auto; min-width: 120px;"
              >
                {{ item }}
              </el-radio>
            </div>
          </el-radio-group>

          <!-- 按钮区域 -->
          <div style="text-align: right; margin-top: 30px;">
            <el-button @click="manualMatchDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmManualMatch">确定</el-button>
          </div>
        </div>
      </el-dialog>
    </div>

    <div v-if="active===0" slot="footer" style="text-align: center">
      <el-button style="margin-top: 12px;" class="normalBtn" @click="next">下一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="closeDialog()" v-if="active < 1">取消
      </el-button>
    </div>
    <div v-if="active===1" slot="footer" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button v-if="datasetType === 'SCADA'" style=" margin-top: 10px;" class="normalBtn"
                 @click="validateTrainAndTestFiles()">变量一致性校验
      </el-button>
      <el-button v-if="datasetType === 'SCADA'" style="margin-top: 12px;" class="normalBtn" @click="next">下一步
      </el-button>
      <el-button v-else style="margin-top: 12px;" class="normalBtn" @click="upload()">完成上传</el-button>
    </div>
    <div v-if="active===2" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="upload()">完成上传</el-button>
    </div>
  </el-dialog>

  <!-- 数据集信息对话框 -->
  <el-dialog
      v-model="datasetInfoVisible"
      title="数据集信息"
      width="50%"
      :before-close="closeDatasetInfoDialog"
  >
    <div class="dialog-content" v-loading="loading" element-loading-text="加载中...">
      <el-table :data="datasetTableData" border style="width: 100%">
        <el-table-column prop="name" label="数据集信息" width="300"></el-table-column>
        <el-table-column prop="value" label="值"></el-table-column>
      </el-table>
      <br/>
      <el-table :data="varTableData" border style="width: 100%" max-height="400px">
        <el-table-column prop="name" label="变量名" width="300"></el-table-column>
        <!-- 操作列 -->
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="datasetType === 'SCADA'" link type="primary"
                       @click="viewSCADAChart(tableData.datasetPath, row.name)">
              查看数据
            </el-button>
            <el-button v-else link type="primary" @click="viewCMSChart(row.name)">查看数据</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 图表弹出框 -->
      <el-dialog v-model="chartVisible" width="60%">
        <div ref="chart" style="width: 100%; height: 400px;" v-loading="chartLoading"
             element-loading-text="加载中..."></div>
      </el-dialog>
      <br/>
      <div style="display: flex;justify-content:center;text-align: center; margin: 20px 0;">
        <el-button class="normalBtn" @click="closeDatasetInfoDialog">关闭</el-button>
      </div>
    </div>
  </el-dialog>
</template>


<script>
import {ElMessage, ElNotification, ElLoading} from "element-plus";
import {gBomVariablesMatch} from "@/api/sw/model3d/configPerceivedVariable/table.js"
import * as XLSX from "xlsx";
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {
  getAllDataUrl,
  uploadSonNode,
  replaceSonNode
} from "@/api/diagnosis/graphVis/dataIngestion";
import * as echarts from 'echarts';
import JSZip from 'jszip';
import axios from 'axios';

let globeParams = {}     //  声明一个全局参数对象
let _tree

export default {
  name: 'uploadDataset',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    sceneId: {
      type: [Number, String],
      required: true
    },
    addOrUpdate: {
      type: String,
      default: 'add'
    },
    modelId: {
      type: [String, Number],
      default: ''
    },
    currentUploadNodeData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      active: 0,//步骤条初始化
      visible: false,
      newModelName: '',
      modelTreeData: [],
      selectNodes: [],
      selectedNodeId: '', // 当前选中的结构树节点id（步骤三用）
      modelTreeVariable: [], // 所有选中的变量，步骤三结束时汇总
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      defaultExpandKeys: [],
      loadingInstance: false,
      updateData: [], // 更新时前已选择的节点
      datasetType: '', // 数据集类型
      datasetTypeTrain: '', //要上传的训练集类型（区分是cms训练集还是scada训练集）
      datasetTypeTest: '', //要上传的测试集类型
      modeListTrain: [], //  训练集列表
      modeListTest: [], // 测试集列表
      selectedFileNameTrain: '', //训练集文件名显示
      selectedFileNameTest: '', //测试集文件名显示
      form: {name: ''},
      selectedReplaceTrainVersion: null,
      selectedReplaceTestVersion: null,
      trainVersions: [],
      testVersions: [],
      modeTrain: {},
      modeTest: {},
      uploadDataSets: [],
      currentNode: null,  // 添加当前节点属性
      isValidated: false,  // 变量一致性校验是否通过
      // 添加数据集预览相关变量
      datasetInfoVisible: false,
      chartVisible: false,
      loading: false,
      chartLoading: false,
      tableData: {
        provider: '',
        // trainPath: '',
        // testPath: '',
        datasetPath: '',
        // trainLen: 0,
        // testLen: 0,
        datasetLen: 0,
        uploadTime: ''
      },
      varTableData: [],
      knowDict: {},
      cmsdata: [],
      chart: null,
      matchTable: [],
      gbomFuzzyMap: {}, // { 变量名: [模糊匹配候选] }
      uploadedVariables: [], // 上传数据集的变量名
      matching: false,
      manualMatchDialogVisible: false,
      manualMatchTarget: '',
      manualMatchSelected: '',
      manualMatchCandidates: [],
      uploadLoading: false
    }
  },
  computed: {
    datasetTableData() {
      return [
        {name: '提供者', value: this.tableData.provider},
        // {name: '训练集路径', value: this.tableData.trainPath},
        // {name: '测试集路径', value: this.tableData.testPath},
        {name: '数据集路径', value: this.tableData.datasetPath},
        // {name: '训练集样本数', value: this.tableData.trainLen},
        // {name: '测试集样本数', value: this.tableData.testLen},
        {name: '数据集样本数', value: this.tableData.datasetLen},
        {name: '上传时间', value: this.tableData.uploadTime}
      ];
    },
  },
  watch: {
    modelValue: {
      immediate: true,
      handler(val) {
        this.visible = val;
        // 当弹窗打开时，如果当前在步骤1且有节点数据，则获取版本信息
        if (val && this.active === 1 && this.currentNode) {
          this.fetchVersions();
        }
      }
    },
    visible(val) {
      this.$emit('update:modelValue', val);
    },
    // 监听 currentUploadNodeData 的变化
    currentUploadNodeData: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.currentNode = newVal;
          // 当进入步骤1时，获取当前节点的数据集信息
          /*if (this.active === 1 && this.visible) {
            this.fetchVersions();
          }*/
        }
      }
    },
    datasetType() {
      this.selectedFileNameTrain = '';
      this.modeListTrain = [];
      this.modeTrain = {};
      this.selectedFileNameTest = '';
      this.modeListTest = [];
      this.modeTest = {};
      this.isValidated = false;
    },
    // 监听 active 的变化
    active(newVal) {
      this.currentNode = this.currentUploadNodeData;
      if (newVal === 1 && this.currentNode && this.visible) {
        // 当进入步骤1时，获取当前节点的数据集信息
        this.fetchVersions();
      }
      if (newVal === 2 && this.uploadedVariables.length === 0) {
        this.initMatchTable();
      }
    }
  },
  async mounted() {
    globeParams.nodeLevel = 3       //    预先展开3层节点
  },
  methods: {
    // 关闭弹窗
    closeDialog() {
      this.visible = false;
      this.datasetType = '';
      this.$emit('update:modelValue', false);
      this.resetDialogData();
      this.form.provider = '';
      this.form.uploadDatasetName = '';
      this.modeListTrain = []; // 清空训练集文件列表
      this.modeListTest = []; // 清空测试集文件列表
      this.selectedFileNameTrain = '';
      this.selectedFileNameTest = '';
      this.selectedReplaceTrainVersion = null; // 清空训练集版本选择
      this.selectedReplaceTestVersion = null; // 清空测试集版本选择
      this.currentNode = null;  // 清空当前节点
      this.trainVersions = []; // 清空训练集版本数据
      this.testVersions = []; // 清空测试集版本数据
      this.uploadedVariables = [];
      this.isValidated = false;
      this.matchTable = [];
    },

    // 新增：重置弹窗所有数据
    resetDialogData() {
      this.active = 0;
      this.datasetType = '';
      this.modelTreeData = [];
      this.modeListTrain = []; // 清空训练集文件列表
      this.modeListTest = []; // 清空测试集文件列表
      this.selectedFileNameTrain = '';
      this.selectedFileNameTest = '';
      this.selectedReplaceTrainVersion = null; // 清空训练集版本选择
      this.selectedReplaceTestVersion = null; // 清空测试集版本选择
      this.trainVersions = []; // 清空训练集版本数据
      this.testVersions = []; // 清空测试集版本数据
      this.uploadedVariables = [];
      this.isValidated = false;
      this.matchTable = [];
    },

    // 取消文件选择
    cancelFile(type) {
      if (type === 'train') {
        this.selectedFileNameTrain = '';
        this.modeListTrain = [];
        this.modeTrain = {};
      } else {
        this.selectedFileNameTest = '';
        this.modeListTest = [];
        this.modeTest = {};
      }
      this.isValidated = false;
    },

    //下载模板
    async downloadTemplate(type) {
      if (!['SCADA', 'CMS'].includes(type)) {
        this.$message.warning('无效的数据类型：' + type)
        return
      }

      if (type === 'SCADA') {
        // SCADA 数据模板（生成 Excel）
        const headers = ['timestamp', '变量1', '变量2', '变量3', '……', '变量n', 'errorcode']

        const dataRow = []

        const worksheetData = [headers, dataRow]
        const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, worksheet, 'SCADA模板')
        XLSX.writeFile(workbook, 'template_SCADA.xlsx')

      } else if (type === 'CMS') {
        try {
          let templateCMSUrl = "program/2025/6/18/template_CMS.zip";
          const response = await axios.get(`/al/file/${templateCMSUrl}`, {responseType: 'arraybuffer'});

          // 1. 创建 Blob 对象
          const blob = new Blob([response.data], {type: 'application/zip'});

          // 2. 创建临时链接
          const downloadUrl = window.URL.createObjectURL(blob);

          // 3. 创建 <a> 标签并触发下载
          const link = document.createElement('a');
          link.href = downloadUrl;
          link.download = 'template_CMS.zip';  // 设置下载文件名
          document.body.appendChild(link);
          link.click();

          // 4. 清理 DOM 和释放 URL
          document.body.removeChild(link);
          window.URL.revokeObjectURL(downloadUrl);
        } catch (error) {
          console.error('下载失败:', error);
          this.$message.error('模板下载失败，请稍后重试');
        }
      }
    },

    // 修改 fetchVersions 方法
    async fetchVersions() {
      if (!this.currentNode) {
        console.warn('没有选择节点');
        return;
      }

      try {
        // 根据当前选择的数据集类型获取数据
        const nodeId = this.currentNode.id;
        const dataType = this.datasetType.toLowerCase();
        const response = await getAllDataUrl({objectId: nodeId, dataType: dataType});

        // 初始化版本数组
        this.trainVersions = [];
        this.testVersions = [];

        if (response.data.code === 0 && response.data.data) {
          const data = response.data.data;
          // 处理训练集数据
          if (data[`${dataType}_train_latest_dataset`]) {
            this.trainVersions.push({
              label: '最新版本',
              datasetName: data[`${dataType}_train_latest_dataset`].split('/').pop(),
              provider: data.scada_provider || data.cms_provider || '',
              uploadTime: this.extractUploadTime(data[`${dataType}_train_latest_dataset`]),
              datasetUrl: data[`${dataType}_train_latest_dataset`]
            });
          }
          if (data[`${dataType}_train_previous_dataset`]) {
            this.trainVersions.push({
              label: '历史版本',
              datasetName: data[`${dataType}_train_previous_dataset`].split('/').pop(),
              provider: data.scada_provider || data.cms_provider || '',
              uploadTime: this.extractUploadTime(data[`${dataType}_train_previous_dataset`]),
              datasetUrl: data[`${dataType}_train_previous_dataset`]
            });
          }

          // 处理测试集数据
          if (data[`${dataType}_test_latest_dataset`]) {
            this.testVersions.push({
              label: '最新版本',
              datasetName: data[`${dataType}_test_latest_dataset`].split('/').pop(),
              provider: data.scada_provider || data.cms_provider || '',
              uploadTime: this.extractUploadTime(data[`${dataType}_test_latest_dataset`]),
              datasetUrl: data[`${dataType}_test_latest_dataset`]
            });
          }
          if (data[`${dataType}_test_previous_dataset`]) {
            this.testVersions.push({
              label: '历史版本',
              datasetName: data[`${dataType}_test_previous_dataset`].split('/').pop(),
              provider: data.scada_provider || data.cms_provider || '',
              uploadTime: this.extractUploadTime(data[`${dataType}_test_previous_dataset`]),
              datasetUrl: data[`${dataType}_test_previous_dataset`]
            });
          }
        }
      } catch (error) {
        console.error('获取数据集失败:', error);
        ElNotification({
          title: 'Error',
          message: '获取数据集版本信息失败',
          type: 'error'
        });
        this.trainVersions = [];
        this.testVersions = [];
      }
    },

    // 从文件路径中提取上传时间
    extractUploadTime(path) {
      if (!path) return '未知';
      const startIndex = path.indexOf('program/') + 'program/'.length;
      const endIndex = path.lastIndexOf('/');
      if (startIndex > -1 && endIndex > startIndex) {
        return path.slice(startIndex, endIndex);
      }
      return '未知';
    },

    // 预览数据集
    async previewDataset(row) {
      if (!row.datasetUrl) {
        ElNotification({
          title: 'Warning',
          message: '数据集地址不存在',
          type: 'warning'
        });
        return;
      }

      this.datasetInfoVisible = true;
      this.tableData.provider = row.provider;

      // 获取数据集路径
      this.tableData.datasetPath = row.datasetUrl;

      // 提取上传时间
      let path = this.tableData.datasetPath;
      const startIndex = path.indexOf('program/') + 'program/'.length;
      const endIndexFull = path.lastIndexOf('/');
      this.tableData.uploadTime = path.slice(startIndex, endIndexFull);

      // 加载数据集信息
      if (this.datasetType === 'SCADA') {
        this.loading = true;
        const [datasetVarNameList, datasetLen] = await this.downloadAndParseCSV1(this.tableData.datasetPath);
        if (datasetVarNameList && datasetLen) {
          this.tableData.datasetLen = datasetLen;
          this.varTableData = datasetVarNameList.map(item => ({name: item.label}));
        } else {
          this.varTableData = [];
          this.tableData.datasetLen = 0;
        }
        this.loading = false;
      } else {
        const [datasettotalLength, rawDatasetcmsdata, datasetknowDict, datasetfaultType] = await this.fetchAndUnzip(this.tableData.datasetPath);
        this.tableData.datasetLen = datasettotalLength;
        this.knowDict = datasetknowDict;
        this.cmsdata = rawDatasetcmsdata;
        this.varTableData = [];
        if (datasetfaultType && datasetfaultType.length > 0) {
          datasetfaultType.forEach(item => {
            this.varTableData.push({name: item});
          });
        }
      }
    },

    // 关闭数据集信息对话框
    closeDatasetInfoDialog() {
      this.datasetInfoVisible = false;
      this.chartVisible = false;
      this.tableData = {
        provider: '',
        // trainPath: '',
        // testPath: '',
        datasetPath: '',
        // trainLen: 0,
        // testLen: 0,
        datasetLen: 0,
        uploadTime: ''
      };
      this.varTableData = [];
    },

    // 下载并解析CSV文件
    async downloadAndParseCSV1(url) {
      if (!url) {
        ElNotification.error('未获取到SCADA数据集');
        return [[], 0];
      }
      try {
        const response = await axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'});

        if (!response.data) {
          ElNotification.error('该路径下找不到数据集，请重新获取');
          return [[], 0];
        }
        const fileType = url.endsWith('.csv') ? 'csv' : 'xlsx';
        return this.extractHeaderFromFileData(response.data, fileType);
      } catch (error) {
        console.error('下载并解析文件出错：', error);
        return [[], 0];
      }
    },

    // 解析csv/xlsx文件
    extractHeaderFromFileData(file, fileType) {
      try {
        const workbook = XLSX.read(file, {type: 'array'});
        const sheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[sheetName];
        const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1});

        const headers = jsonData[0] || [];
        const transferData = headers
            .filter(label => label !== 'errorcode')
            .map(label => ({prop: label, label}));

        const dataLength = jsonData.length - 1;

        return [transferData, dataLength];
      } catch (err) {
        console.error('解析文件头部出错：', err);
        return [[], 0];
      }
    },

    // 训练集与测试集一致性校验
    async validateTrainAndTestFiles() {
      this.isValidated = false;
      const trainFile = this.modeTrain;
      const testFile = this.modeTest;

      if (!trainFile?.name || !testFile?.name) {
        ElMessage.warning('请先上传训练集和测试集');
        this.isValidated = false;
        return;
      }
      /*const trainFileType = trainFile.name.endsWith('.csv') ? 'csv' : 'xlsx';
      const testFileType = testFile.name.endsWith('.csv') ? 'csv' : 'xlsx';

      if (trainFileType !== testFileType) {
        ElMessage.error('训练集和测试集文件类型不一致');
        return false;
      }*/
      // 启动加载动画
      const loading = ElLoading.service({
        lock: true,
        text: '正在进行变量一致性校验...',
        target: document.querySelector('.upload-dataset-page'),
      });
      // 等 DOM 更新，确保 loading 被渲染出来
      await this.$nextTick();

      // 再等一小段时间，确保 loading 能显示（比如 200ms）
      await new Promise(resolve => setTimeout(resolve, 100));

      try {

        if (!trainFile.name.endsWith('.xlsx') || !testFile.name.endsWith('.xlsx')) {
          ElMessage.error('请上传xlsx格式的训练集和测试集');
          this.isValidated = false;
          return false;
        }

        const trainBuffer = await trainFile.arrayBuffer();
        const testBuffer = await testFile.arrayBuffer();

        const [trainHeaders] = this.extractHeaderFromFileData(trainBuffer, 'xlsx');
        const [testHeaders] = this.extractHeaderFromFileData(testBuffer, 'xlsx');

        if (trainHeaders.length !== testHeaders.length) {
          ElMessage.error('变量数量不一致');
          this.isValidated = false;
          return false;
        }

        for (let i = 0; i < trainHeaders.length; i++) {
          if (trainHeaders[i].label !== testHeaders[i].label) {
            ElMessage.error(`第${i + 1}个变量名不一致：${trainHeaders[i].label} ≠ ${testHeaders[i].label}`);
            this.isValidated = false;
            return false;
          }
        }

        ElMessage.success('变量一致性校验通过');
        this.isValidated = true;
        this.uploadedVariables = [];
        return trainHeaders.map(h => h.label);

      } catch (err) {
        ElMessage.error('文件解析错误');
        console.error(err);
        this.isValidated = false;
        return false;
      } finally {
        // 关闭加载动画
        loading.close();
      }
    },

    async modifyDataSetHeader(headers) {
      this.modeTrain = await this._modifyFileHeader(this.modeTrain, headers);
      this.modeTest = await this._modifyFileHeader(this.modeTest, headers);
    },

    async _modifyFileHeader(file, headersMap) {
      const arrayBuffer = await file.arrayBuffer();
      const workbook = XLSX.read(arrayBuffer, {type: 'array'});

      const sheetName = workbook.SheetNames[0];
      const worksheet = workbook.Sheets[sheetName];

      const json = XLSX.utils.sheet_to_json(worksheet, {header: 1}); // 获取二维数组

      if (json.length === 0) return file;

      const oldHeader = json[0];
      json[0] = oldHeader.map(h => headersMap.has(h) ? headersMap.get(h) : h);

      const newSheet = XLSX.utils.aoa_to_sheet(json);
      const newWorkbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(newWorkbook, newSheet, sheetName);

      const newExcelBuffer = XLSX.write(newWorkbook, {
        bookType: 'xlsx',
        type: 'array',
        compression: true, // ✅ 开启压缩
      });

      // 返回新的 File 对象
      const newFile = new File([newExcelBuffer], file.name, {type: file.type});
      newFile.uid = file.uid;
      return newFile
    },


// 查看SCADA图表
    async viewSCADAChart(url, variableName) {
      this.chartVisible = true;
      const data = await this.downloadAndParseCSV2(url, variableName);
      if (data && data.length !== 0) {
        this.$nextTick(() => {
          this.renderChart(data);
        });
      } else {
        ElMessage.info("没有数据可以展示");
        this.chartVisible = false;
      }
    },

    // 查看CMS图表
    viewCMSChart(name) {
      this.chartVisible = true;
      this.chartLoading = true;
      const key = Object.keys(this.knowDict).find(k => this.knowDict[k].type === name);
      let data = [];
      if (key !== undefined) {
        const index = parseInt(key, 10);
        data = this.cmsdata[index].slice(0, 1024);
      }
      if (data.length !== 0) {
        this.$nextTick(() => {
          this.renderChart(data);
          this.chartLoading = false;
        });
      } else {
        ElMessage.info("没有数据可以展示");
        this.chartVisible = false;
      }
    },

    // 渲染图表
    renderChart(data) {
      const chartElement = this.$refs.chart;
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
          data: Array.from({length: data.length}, (_, i) => i + 1),
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
            smooth: true,
          },
        ],
      };

      myChart.setOption(option);
    },

    // 下载并解压ZIP文件
    async fetchAndUnzip(url) {
      this.loading = true;
      const response = await axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'});
      const zip = await JSZip.loadAsync(response.data);
      const folderContents = {};

      for (const relativePath in zip.files) {
        const file = zip.files[relativePath];
        if (!file.dir && relativePath.endsWith('.txt')) {
          // 是 txt 文件，读取内容
          const content = await file.async('string');
          const lines = content.split(/\r?\n/);
          const contentWithoutFirstLine = lines.slice(1).join('\n');
          // 按空格分割，过滤空字符串
          const arr = contentWithoutFirstLine
              .split(/\s+/)
              .filter(s => s.length > 0)
              .map(s => parseInt(s, 10));
          // 提取文件夹路径（去掉最后的文件名）
          const folderPath = relativePath.includes('/')
              ? relativePath.substring(0, relativePath.lastIndexOf('/'))
              : '';  // 根目录文件放到 '' 分组

          // 初始化对应文件夹数组
          if (!folderContents[folderPath]) {
            folderContents[folderPath] = [];
          }
          // 合并当前文件数组
          folderContents[folderPath].push(...arr);
        }
      }

      let totalLength = 0;
      for (const key in folderContents) {
        totalLength += folderContents[key].length;
      }
      const keys = Object.keys(folderContents);

      let jsonFile = null;
      for (const relativePath in zip.files) {
        if (!zip.files[relativePath].dir && relativePath.endsWith('label_map.json')) {
          jsonFile = zip.files[relativePath];
          break;
        }
      }

      if (!jsonFile) {
        this.loading = false;
        return [totalLength, Object.values(folderContents), {}, []];
      }

      const jsonStr = await jsonFile.async('string');
      const jsonData = JSON.parse(jsonStr);

      // 提取所有 keys 对应的 type
      const types = [];
      const knowDict = jsonData.know_dict || {};

      for (const key in knowDict) {
        if (knowDict[key] && knowDict[key].type) {
          types.push(knowDict[key].type);
        }
      }

      this.loading = false;
      return [totalLength, Object.values(folderContents), knowDict, types];
    },

    /**
     * 数据集上传过程
     */
    // 修改handleFileChangeTrain方法
    handleFileChangeTrain(file, fileList) {
      this.selectedFileNameTrain = file.name;
    },
    // 修改handleFileChangeTest方法
    handleFileChangeTest(file, fileList) {
      this.selectedFileNameTest = file.name;
    },
    // 修改modeUploadTrain方法
    modeUploadTrain(item) {
      this.modeTrain = item.file;
    },
    // 修改modeUploadTest方法
    modeUploadTest(item) {
      this.modeTest = item.file;
    },
    // 处理训练集版本单选框点击
    handleTrainRadioClick(label) {
      if (this.selectedReplaceTrainVersion === label) {
        this.selectedReplaceTrainVersion = null;
      }
    },

    // 处理测试集版本单选框点击
    handleTestRadioClick(label) {
      if (this.selectedReplaceTestVersion === label) {
        this.selectedReplaceTestVersion = null;
      }
    },
    async upload() {
      // 感知变量匹配校验
      if (this.datasetType === 'SCADA' && this.active === 2 && this.matchTable && this.matchTable.some(row => row.status !== 'success')) {
        this.$message.warning('请先完成感知变量匹配');
        return;
      }

      if (!this.form.provider?.trim()) {
        ElNotification({
          title: '警告',
          message: '请填写提供者姓名',
          type: 'warning',
        });
        return;
      }
      if(this.datasetType === 'CMS') {
        const trainFile = this.modeTrain;
        const testFile = this.modeTest;
        if (!trainFile.name.endsWith('.zip') || !testFile.name.endsWith('.zip')) {
          ElMessage.error('请上传zip格式的训练集和测试集');
          this.isValidated = false;
          return false;
        }
      }

      try {
        // 检查是否上传了训练集和测试集
        if (this.modeTrain.uid === undefined && this.modeTest.uid === undefined) {
          this.$message.warning('没有上传数据集')
          return;
        } else if (this.modeTrain.uid === undefined && this.modeTest.uid !== undefined) {
          this.$message.warning('请上传训练集')
          return;
        } else if (this.modeTrain.uid !== undefined && this.modeTest.uid === undefined) {
          this.$message.warning('请上传测试集')
          return;
        }
        this.uploadLoading = true;
        // 分别处理训练集和测试集的上传
        if (this.modeTrain.uid !== undefined) {
          const trainFile = new FormData();
          trainFile.append('file', this.modeTrain);
          const trainResponse = await uploadProgram(trainFile);
          const trainDatasetUrl = trainResponse.data.data;

          // 确定要替换的训练集版本类型
          let replaceTrainVersionType = null;
          if (this.selectedReplaceTrainVersion) {
            const trainVersion = this.trainVersions.find(v => v.label === this.selectedReplaceTrainVersion);
            if (trainVersion) {
              replaceTrainVersionType = trainVersion.label === '最新版本' ? 'train_latest' : 'train_previous';
            }
          }

          // 根据是否有替换版本选择不同的上传接口
          if (replaceTrainVersionType) {
            // 使用替换版本的接口
            await replaceSonNode(
                this.currentUploadNodeData.id,
                this.datasetTypeTrain,
                trainDatasetUrl,
                this.form.provider,
                replaceTrainVersionType,
                this.form.uploadDatasetName,
            );
          } else {
            // 使用普通上传接口
            await uploadSonNode(
                this.currentUploadNodeData.id,
                this.datasetTypeTrain,
                trainDatasetUrl,
                this.form.provider,
                this.form.uploadDatasetName
            );
          }

          // 清空训练集相关数据
          this.modeListTrain = [];
          this.modeTrain = {};
          this.datasetTypeTrain = '';
          this.selectedReplaceTrainVersion = null;
        }

        if (this.modeTest.uid !== undefined) {
          const testFile = new FormData();
          testFile.append('file', this.modeTest);
          const testResponse = await uploadProgram(testFile);
          const testDatasetUrl = testResponse.data.data;

          // 确定要替换的测试集版本类型
          let replaceTestVersionType = null;
          if (this.selectedReplaceTestVersion) {
            const testVersion = this.testVersions.find(v => v.label === this.selectedReplaceTestVersion);
            if (testVersion) {
              replaceTestVersionType = testVersion.label === '最新版本' ? 'test_latest' : 'test_previous';
            }
          }

          // 根据是否有替换版本选择不同的上传接口
          if (replaceTestVersionType) {
            // 使用替换版本的接口
            await replaceSonNode(
                this.currentUploadNodeData.id,
                this.datasetTypeTest,
                testDatasetUrl,
                this.form.provider,
                replaceTestVersionType,
                this.form.uploadDatasetName,
            );
          } else {
            // 使用普通上传接口
            await uploadSonNode(
                this.currentUploadNodeData.id,
                this.datasetTypeTest,
                testDatasetUrl,
                this.form.provider,
                this.form.uploadDatasetName,
            );
          }

          // 清空测试集相关数据
          this.modeListTest = [];
          this.modeTest = {};
          this.datasetTypeTest = '';
          this.selectedReplaceTestVersion = null;

        }

        ElNotification({
          title: 'Success',
          message: '上传成功',
          type: 'success',
        });
        this.uploadLoading = false;
        this.uploadedVariables = [];
        this.matchTable = [];
        this.closeDialog();
        this.$emit('refreshData')
      } catch (error) {
        console.error('上传失败:', error);
        ElNotification({
          title: 'Warning',
          message: '上传失败',
          type: 'warning',
        });
      }
    },

    //上一页
    prev() {
      if (this.active > 0) {
        this.active--;
        // 当从步骤1返回步骤0时，清空版本数据
        if (this.active === 0) {
          this.trainVersions = [];
          this.testVersions = [];
        }
      }
    },
    //下一页
    next() {
      if (this.active === 0) {
        if (!this.datasetType && !this.form.provider?.trim()) {
          this.$message.warning('请填写提供者姓名并选择数据类型')
          return;
        } else if (!this.datasetType) {
          this.$message.warning('请选择数据类型')
          return
        } else if (!this.form.provider?.trim()) {
          this.$message.warning('请填写提供者姓名')
          return;
        }
      }
      if (this.active === 1) {
        if (this.modeTrain.uid === undefined && this.modeTest.uid === undefined) {
          this.$message.warning('请上传训练集和测试集')
          return;
        } else if (this.modeTrain.uid === undefined && this.modeTest.uid !== undefined) {
          this.$message.warning('请上传训练集')
          return;
        } else if (this.modeTrain.uid !== undefined && this.modeTest.uid === undefined) {
          this.$message.warning('请上传测试集')
          return;
        }
        if (!this.isValidated) {
          this.$message.warning('请先完成变量一致性校验')
          return;
        }
      }
      this.active++;
    },

    // 下载并解析CSV数据（用于图表显示）
    async downloadAndParseCSV2(url, variableName) {
      if (url === '' || url === null) {
        ElNotification.error('未获取到SCADA数据集');
        return;
      }

      try {
        this.chartLoading = true;
        const response = await axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'});

        if (response.data == null) {
          ElNotification.error('该路径下找不到数据集，请重新获取');
          return;
        }

        const workbook = XLSX.read(response.data, {type: 'array'});
        const sheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[sheetName];
        const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1});

        // 获取对应变量名的列数据
        const variableColumnIndex = jsonData[0].indexOf(variableName);
        if (variableColumnIndex === -1) {
          ElNotification.error('未找到指定的变量名');
          return;
        }

        const variableData = jsonData.slice(1, 1001).map(row => row[variableColumnIndex]);
        this.chartLoading = false;
        return variableData;
      } catch (error) {
        console.error('Error fetching and parsing file:', error);
        this.chartLoading = false;
        return null;
      }
    },

    /**
     * 感知变量匹配
     */
    async initMatchTable() {
      // 提取上传数据集变量名（以训练集为准）
      let headers = [];
      if (this.modeTrain && this.modeTrain.name) {
        const trainBuffer = await this.modeTrain.arrayBuffer();
        const [trainHeaders] = this.extractHeaderFromFileData(trainBuffer, 'xlsx');
        headers = trainHeaders.map(h => h.label);
      }
      this.uploadedVariables = headers;
      // 初始化表格，全部为未匹配
      this.matchTable = headers.map(v => ({
        variable: v,
        status: 'unmatched',
        matchedTo: '',
        candidates: []
      }));
    },
    // 自动匹配（点击按钮时调用后端）
    async startAutoMatch() {
      this.matching = true;
      try {
        const res = await gBomVariablesMatch(this.sceneId, this.uploadedVariables);
        let fuzzyMap = {};
        if (res.data && res.data.code === 0) {
          fuzzyMap = res.data.data || {};
        }
        this.gbomFuzzyMap = fuzzyMap;
        // 更新表格状态
        this.matchTable = this.uploadedVariables.map(v => {
          if (fuzzyMap.hasOwnProperty(v)) {
            return {
              variable: v,
              status: 'fail',
              matchedTo: '',
              candidates: fuzzyMap[v] || []
            };
          } else {
            return {
              variable: v,
              status: 'success',
              matchedTo: v,
              candidates: []
            };
          }
        });
      } catch (e) {
        this.$message.error('感知变量自动匹配失败');
      }
      this.matching = false;
    },

    openManualMatch(row) {
      this.manualMatchTarget = row.variable;
      this.manualMatchSelected = '';
      this.manualMatchCandidates = this.gbomFuzzyMap[row.variable] || [];
      this.manualMatchDialogVisible = true;
    },
    // 确认手动匹配
    async confirmManualMatch() {
      if (!this.manualMatchSelected) {
        this.$message.warning('请选择一个可匹配变量');
        return;
      }
      this.manualMatchDialogVisible = false;
      const loading = ElLoading.service({
        lock: true,
        text: '正在进行匹配',
        target: document.querySelector('.variable-match-page'),
      });

      // 等 DOM 更新，确保 loading 被渲染出来
      await this.$nextTick();

      // 再等一小段时间，确保 loading 能显示（比如 200ms）
      await new Promise(resolve => setTimeout(resolve, 100));
      try {

        // 1. 构造 headers map
        const headers = new Map();
        headers.set(this.manualMatchTarget, this.manualMatchSelected);
        // 2. 修改表头
        await this.modifyDataSetHeader(headers);


        // 3. 更新表格状态
        const row = this.matchTable.find(r => r.variable === this.manualMatchTarget);
        if (row) {
          row.status = 'success';
          row.matchedTo = this.manualMatchSelected;
        }
      } finally {
        loading.close();
      }

    },
  }
}

</script>

<style scoped lang="scss">
.high-dialog {
  min-height: 800px;
}

.upload-dataset-page {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 40px;
  background: #ffffff;
}

.dataset-section {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
}

.upload-btn {
  margin-top: 16px;
  text-align: right;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 20px 0;
  font-size: 14px;
}
</style>