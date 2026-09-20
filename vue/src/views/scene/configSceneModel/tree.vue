<template>
  <div class="mycon">
    <template-tree
        class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
        :defaultProps="defaultProps" :expandNode="true" :proId="nodeId" :node-type="nodeType"
        :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
        :showCheckBox=false :checkStrictly=true :isLazy=false @getTreeNodes="getTreeNodes"
        @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand"
        @eventNodeUpload="dlgUploadSonNode" @eventNodeAdd="dlgAddSonNode" @eventNodeEdit="dlgEditNode"
        @eventNodeDelete="dlgDeleteNode" @upProductModel="dlgUpProductModel" @eventNodeView="dlgViewDataSet">
    </template-tree>
  </div>
  <el-dialog v-model="dlgNodeAddEdit" :title="titleName"
             :close-on-click-modal="false" width="50%" draggable>
    <div class="content">
      <div>
        <avue-form ref="form" :option="nodeOption" v-model="nodeForm"
                   @submit="handleSubmit">
        </avue-form>
      </div>
    </div>
  </el-dialog>
  <upload-dataset v-model="uploadDatasetVisible" v-show="uploadDatasetVisible" ref="uploadDataset"
                  :scene-id="sceneId"
                  :model-id="nodeId"
                  :currentUploadNodeData="currentUploadNodeData"
                  @refreshData="refreshData"></upload-dataset>
  <add-model v-model="addModelVisible" v-show="addModelVisible" ref="addModel" :scene-id="sceneId"
             :addOrUpdate="addOrUpdate" :model-id="nodeId" @onRefreshTree="refreshTree"></add-model>
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
            <el-button v-if="isSCADA" link type="primary" @click="viewSCADAChart(tableData.trainPath, row.name)">
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
import templateTree from './templateTree.vue'

import {
  reqSonNodesBySceneId,
  reqTreeNodesBySceneId,
  reqSonNodesByModelId,
  reqTreeNodesByModelId, reqAddSonNode, reqObjById, reqDeleteNodesBySceneId, reqPutObj
} from '@/api/sw/model3d/configGbomTree/index.js'
import {onMounted, ref} from 'vue'
import {nodeOption} from '@/const/crud/sw/model3d/configGbomTree'
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {revertDataset, uploadSonNode} from "@/api/diagnosis/graphVis/dataIngestion";
import addModel from "@/views/scene/configSceneModel/addModel.vue";
import {getProjectById} from "@/api/admin/user";
import uploadDataset from "./uploadDataset.vue"
import * as XLSX from "xlsx";
import * as echarts from 'echarts';
import JSZip from 'jszip';
import axios from 'axios';
import {ElNotification, ElMessage} from "element-plus";

let globeParams = {}     //  声明一个全局参数对象
let __tree

export default {
  components: {
    uploadDataset,
    addModel,
    templateTree
  },
  name: 'variableGbomTree',

  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      titleName: '',
      nodeOption: nodeOption,
      nodeForm: {},
      updateFlag: '',
      dlgNodeAddEdit: false,          //  控制新增、编辑按钮点击后，弹窗的显示和隐藏
      dlgNodeUploadDataset: false,          // 控制上传数据集弹窗
      uploadDatasetVisible: false,
      defaultExpandKeys: [],
      currentUploadNodeData: {},
      datasetType: '',
      mode: {},
      modeList: [],
      form: {name: ''},
      selectedFileName: '',
      datasetUrl: '',
      // 原有数据...
      showRevertDialog: false,
      showConfirmRevert: false,
      selectedRevertType: '',
      addOrUpdate: 'update',
      addModelVisible: false,

      // 数据集信息相关
      datasetInfoVisible: false,
      chartVisible: false,
      loading: false,
      chartLoading: false,
      isSCADA: false,
      tableData: {
        provider: '',
        trainPath: '',
        testPath: '',
        trainLen: 0,
        testLen: 0,
        uploadTime: ''
      },
      datasetTableData: [],
      varTableData: [],
      knowDict: {},
      traincmsdata: [],
      chart: null,
    }
  },
  props: {
    nodeId: {
      type: [String, Number],
      required: true
    },
    nodeType: {
      type: String,
      default: 'GBOM'
    },
    sceneId: {
      type: [Number, String],
      required: true
    },
  },
  watch: {
    nodeId(newVal) {
      console.log('父组件传来的 nodeId 发生变化:', newVal)
      // 重置templateTree的状态
      if (this.$refs.lazyTree) {
        globeParams.nodeLevel = 3
        this.$refs.lazyTree.updateBom = false
        this.$refs.lazyTree.upload = false
      }
      this.getTreeNodes(globeParams.nodeLevel)
    },
    nodeType(newVal) {
      console.log('父组件传来的 nodeType 发生变化:', newVal)
      // 重置templateTree的状态
      if (this.$refs.lazyTree) {
        globeParams.nodeLevel = 3
        this.$refs.lazyTree.updateBom = false
        this.$refs.lazyTree.upload = false
      }
      this.getTreeNodes(globeParams.nodeLevel)
    },
  },

  computed: {
    datasetTableData() {
      return [
        {name: '提供者', value: this.tableData.provider},
        {name: '训练集路径', value: this.tableData.trainPath},
        {name: '测试集路径', value: this.tableData.testPath},
        {name: '训练集样本数', value: this.tableData.trainLen},
        {name: '测试集样本数', value: this.tableData.testLen},
        {name: '上传时间', value: this.tableData.uploadTime}
      ];
    },
  },

  setup() {
    let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
    onMounted(() => {
      __tree = lazyTree
    })
    return {lazyTree}
  },
  async mounted() {
    this.$options._isMounted = true  // 标记组件已挂载
    globeParams.nodeLevel = 3       //    预先展开3层节点
    this.getTreeNodes(globeParams.nodeLevel)
    console.log("tree:" + this.nodeId)
  },

  methods: {
    /**
     * 处理弹窗
     */
    dlgUploadSonNode(pNode, pData, flag) {
      if (this.nodeType === 'GBOM') {
        this.$message.warning('请在机型结构树上传数据集')
        return
      }
      this.currentUploadNodeData = pData
      this.titleName = "上传数据集"
      this.updateFlag = flag
      // this.dlgNodeUploadDataset = true
      this.uploadDatasetVisible = true
      this.selectedFileName = ''; // 清除文件名
      this.modeList = []; // 清空文件列表
    },
    //  弹窗添加子节点
    dlgAddSonNode(pNode, pData, flag) {
      this.titleName = "新增子节点"
      this.nodeForm = {              //  节点新增弹框的表单对象
        nodeId: null,
        nodeName: '',
        swsort: '',
        memo: '',
        sceneId: this.nodeId
      }
      this.updateFlag = flag
      this.dlgNodeAddEdit = true
    },
    dlgEditNode(node, data, flag) {
      if (data.usedCount !== 0) {
        this.$message.warning('当前节点被机型结构树使用，不可编辑')
        return
      }
      this.titleName = "编辑节点"
      this.nodeOption = nodeOption
      this.updateFlag = flag
      if (this.updateFlag === 'template') {
        reqObjById(data.id).then(response => {
          this.nodeForm = response.data.data          //  填写可编辑字段
          this.dlgNodeAddEdit = true
        })
      } else {
        reqInstanceObjById(data.id).then(response => {
          this.nodeForm = response.data.data          //  填写可编辑字段
          this.dlgNodeAddEdit = true
        })
      }
    },
    async dlgDeleteNode(data, flag) {
      if (data.usedCount !== 0) {
        this.$message.warning('当前节点被机型结构树使用，不可删除')
        return
      }
      this.updateFlag = flag
      try {
        await this.$confirm('此操作将删除当前节点及其所有子节点, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response
        this.loading = true
        if (this.updateFlag === 'template') {
          response = await reqDeleteNodesBySceneId(this.nodeId, data.nodeCode)
        } else {
          response = await reqDeleteInstanceNodes(this.formdata.projectId, data.turbineCode, data.nodeCode)
        }
        if (response.data.code !== 208) {
          this.$message({
            type: 'error',
            message: '节点删除失败!'
          })
          this.loading = false
        } else {
          if ((data.nodeType !== 'Root') && (data.nodeType !== 'Root-Leaf')) {       //  如果存在父节点
            if (this.updateFlag === 'template') {
              let pNode = __tree.value.removeSonNode(data.id)
              __tree.value.setNodeSelected(pNode.data.id)             //  将父节点设置为选中状态
            } else {
              let pNode = __inTree.value.removeSonNode(data.id)
              __inTree.value.setNodeSelected(pNode.data.id)             //  将父节点设置为选中状态
            }
          } else {
            if (this.updateFlag === 'template') {
              this.templateTree = []
            } else {
              await this.getInstanceTreeNodes(1)
            }
          }
          this.loading = false
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
        }
      } catch (error) {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      }
    },
    dlgUpProductModel() {
      this.addModelVisible = true
    },


    handleSubmit(nodeForm, done) {
      if (this.titleName === '新增子节点')
        this.nodeAdd(nodeForm, done)
      else if (this.titleName === '编辑节点')
        this.nodeEdit(nodeForm, done)
    },
    //节点添加
    async nodeAdd(nodeForm, done) {
      let pNode
      if (this.updateFlag === 'template') {
        pNode = __tree.value.getCurrentNodeData()     // 获取父节点
      } else {
        pNode = __inTree.value.getCurrentNodeData()
      }
      try {
        let response
        if (this.updateFlag === 'template') {
          response = await reqAddSonNode(pNode.id, nodeForm)
        } else {
          response = await reqAddInstanceSonNode(pNode.id, nodeForm)
        }
        if (response.data.code === 200 || response.data.code === "200") {
          let newNode = response.data.data        //  给新添加的节点增加按钮属性
          newNode.showAdd = true
          newNode.showRemove = true
          newNode.showEdit = true
          newNode.showHelp = true
          newNode.children = []          //  注意：新添加的节点都是叶子节点
          let res
          if (this.updateFlag === 'template') {
            res = await reqSonNodesBySceneId(this.nodeId, newNode.nodeCode);
          } else {
            res = await reqInstanceSonNodes(pNode.nodeCode, pNode.turbineCode)
          }
          if (res.data.data) {
            res.data.data.forEach((item) => {
              item.showAdd = true
              item.showRemove = true
              item.showEdit = true
              item.showHelp = true
              item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
            })
          }
          pNode.children = res.data.data
          await this.$nextTick(() => {
            if (this.updateFlag === 'template') {
              __tree.value.setNodeSelected(newNode.id)       //  结构树渲染成功后，将新添加的节点设置为选中状态
            } else {
              __inTree.value.setNodeSelected(newNode.id)
            }
          })
          this.$message.success('子节点添加成功')
          this.refreshData()
        }
        this.dlgNodeAddEdit = false
        done()
      } catch (error) {
        console.log(error)
      }
    },
    //  节点编辑
    async nodeEdit(nodeForm, done) {
      let response
      if (this.updateFlag === 'template') {
        response = await reqPutObj(nodeForm)
      } else {
        response = await reqPutInstanceObj(nodeForm)
      }
      if (response.data.code === 200 || response.data.code === "200") {
        this.$message.success('节点编辑成功')
        this.dlgNodeAddEdit = false
        if ((response.data.data.nodeType !== 'Root') && (response.data.data.nodeType !== 'Root-Leaf')) {       //  如果存在父节点
          let newData = {
            id: response.data.data.id,
            name: response.data.data.name,
            memo: response.data.data.memo
          }
          if (this.updateFlag === 'template') {
            __tree.value.updateNode(newData)            //  更新节点状态
          } else {
            __inTree.value.updateNode(newData)
          }
        } else {
          if (this.updateFlag === 'template') {
            await this.getTreeNodes(globeParams.nodeLevel)
          } else {
            await this.getInstanceTreeNodes(1)
          }
        }
      }
      this.dlgNodeAddEdit = false
      done()
    },

    /*
        refreshData() {
          this.getTreeNodes()
        },
    */

    /**
     * 处理数据集上传的过程
     */
    modeUpload(item) {
      this.mode = item.file;
    },
    // 模拟上传组件的文件变化处理
    handleFileChange(file, fileList) {
      this.selectedFileName = file.name; // 将文件名赋值给 selectedFileName
    },
    // 取消文件选择
    cancelFile() {
      this.selectedFileName = ''; // 清除文件名
      this.modeList = []; // 清空文件列表
    },
    upload() {
      if (!this.form.provider?.trim()) {
        ElNotification({
          title: '警告',
          message: '请填写提供者姓名',
          type: 'warning',
        });
        return;
      }
      this.selectedFileName = ''
      const file = new FormData();
      if (this.mode.uid === undefined) {
        ElNotification({
          title: 'Warning',
          message: '没有数据集',
          type: 'warning',
        });
      } else {
        file.append('file', this.mode);
        uploadProgram(file)
            .then(async (response) => {
              ElNotification({
                title: 'Success',
                message: '上传成功',
                type: 'success',
              });
              this.modeList = [];
              this.mode = {};
              this.datasetUrl = response.data.data;
              const res = await uploadSonNode(this.currentUploadNodeData.id, this.datasetType, this.datasetUrl, this.form.provider)
            })
            .catch(() => {
              ElNotification({
                title: 'Warning',
                message: '上传失败',
                type: 'warning',
              });
            });
      }
      this.dlgNodeUploadDataset = false
    },
    showConfirmRevertDialog() {
      if (!this.selectedRevertType) {
        this.$message.warning('请选择要恢复的数据集类型');
        return;
      }
      this.showRevertDialog = false;
      this.showConfirmRevert = true;
    },
    revertToPreviousVersion() {
      this.showConfirmRevert = false;
      // 调用API恢复上一版本
      revertDataset(this.currentUploadNodeData.id, this.selectedRevertType).then(response => {
        if (response.data.data.code !== 1) {
          this.$message.success('已恢复到上一次的版本');
        } else {
          this.$message.error('恢复失败: ' + response.data.data.msg);
        }
        this.selectedRevertType = ''

      })
    },

    /**
     * 树节点处理
     */
    // 根据节点层级数，加载结构树的一组节点
    getTreeNodes(nodeLevel) {
      globeParams.nodeLevel = nodeLevel
      // 根据节点类型调用不同的接口
      const apiCall = this.nodeType === 'GBOM'
          ? reqTreeNodesBySceneId(this.nodeId, nodeLevel)
          : reqTreeNodesByModelId(this.nodeId, nodeLevel);
      apiCall.then(response => {
        if ((response.data.data) && (response.data.data.length > 0)) {
          this.treeData = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          for (let item of rootNodes) {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            item.showView = this.nodeType === 'MBOM' &&
                (
                    (item.scadaTestLatestDataset && item.scadaTrainLatestDataset) ||
                    (item.cmsTrainLatestDataset && item.cmsTestLatestDataset)
                );
            this.treeData.push(item)
            this.expandKeys.push(item.id)
            this.setChildren(item, response.data.data)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点

          // 数据加载完成后，如果在mounted中调用，则发送点击事件
          if (this.$options._isMounted) {
            if (this.treeData && this.treeData.length > 0) {
              this.$emit('clickNode', this.treeData[0])
            }
          }
        } else {
          console.log(`没有找到${this.nodeType}树，请先创建${this.nodeType}树`)
        }
      }).catch(error => {
        this.treeData = []
        this.expandKeys = []
        console.log(error)
      })
    },

    //  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
    setChildren(pNode, nodeList) {
      let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList)
      let children = res.sonNodes
      if (children.length === 0) {
        if ((pNode.nodeType === 'Mid') || (pNode.nodeType === 'Root')) {           //  如果不是叶子节点，节点前显示"+"号
          this.expandKeys = this.expandKeys.filter(item => item !== pNode.id) //  从扩展节点中删除它
          pNode.children = [{id: 'loading', name: '节点加载中...'}]
        }
        return pNode
      } else {
        pNode.children = children
        children.forEach((item) => {
          item.showAdd = true
          item.showRemove = true
          item.showEdit = true
          item.showHelp = true
          item.showView = this.nodeType === 'MBOM' &&
              (
                  (item.scadaTestLatestDataset && item.scadaTrainLatestDataset) ||
                  (item.cmsTrainLatestDataset && item.cmsTestLatestDataset)
              );
          this.expandKeys.push(item.id)          //  添加到扩展节点
          this.setChildren(item, res.otherNodes)
        })
      }
    },

    //  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
    getChildrenByNodeCode(pNodeCode, nodeList) {
      let sonNodes = []
      let otherNodes = []
      let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
      nodeList.forEach((item) => {
        if (regex.test(item.nodeCode))          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
          sonNodes.push(item)
        else
          otherNodes.push(item)
      })
      return {sonNodes: sonNodes, otherNodes: otherNodes}
    },

    //  节点扩展消息响应
    async handleNodeExpand(data, node, treeNode) {
      if (node.level >= globeParams.nodeLevel) {
        // 根据节点类型调用不同的接口
        let response;
        if (this.nodeType === 'GBOM') {
          response = await reqSonNodesBySceneId(this.nodeId, data.nodeCode);
        } else {
          response = await reqSonNodesByModelId(this.nodeId, data.nodeCode);
        }

        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            item.showView = this.nodeType === 'MBOM' &&
                (
                    (item.scadaTestLatestDataset && item.scadaTrainLatestDataset) ||
                    (item.cmsTrainLatestDataset && item.cmsTestLatestDataset)
                );
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    //  节点点击消息响应
    handleNodeClick(data, node, treeNode, event) {
      this.$emit('clickNode', data)
    },
    // 查看数据集信息
    async dlgViewDataSet(node, data) {
      this.datasetInfoVisible = true
      // this.tableData.provider = data.provider

      // 判断是SCADA还是CMS数据集
      this.isSCADA = data.scadaTrainLatestDataset && data.scadaTestLatestDataset

      // 获取最新数据集路径
      if (this.isSCADA) {
        this.tableData.provider = data.scadaProvider
        this.tableData.trainPath = data.scadaTrainLatestDataset
        this.tableData.testPath = data.scadaTestLatestDataset
      } else {
        this.tableData.provider = data.cmsProvider
        this.tableData.trainPath = data.cmsTrainLatestDataset
        this.tableData.testPath = data.cmsTestLatestDataset
      }

      // 提取上传时间
      let path = this.tableData.trainPath
      const startIndex = path.indexOf('program/') + 'program/'.length;
      const endIndexFull = path.lastIndexOf('/');
      this.tableData.uploadTime = path.slice(startIndex, endIndexFull);

      // 加载数据集信息
      if (this.isSCADA) {
        this.loading = true
        const [trainVarNameList, trainLen] = await this.downloadAndParseCSV1(this.tableData.trainPath)
        const [testVarNameList, testLen] = await this.downloadAndParseCSV1(this.tableData.testPath)
        if (testLen !== 0) {
          this.loading = false
        }
        this.tableData.trainLen = trainLen
        this.tableData.testLen = testLen
        this.varTableData = trainVarNameList.map(item => ({name: item.label}))
      } else {
        const [traintotalLength, rawTraincmsdata, trainknowDict, trainfaultType] = await this.fetchAndUnzip(this.tableData.trainPath)
        this.tableData.trainLen = traintotalLength
        this.knowDict = trainknowDict

        this.traincmsdata = rawTraincmsdata
        const [testtotalLength] = await this.fetchAndUnzip(this.tableData.testPath)
        this.tableData.testLen = testtotalLength
        this.varTableData = []
        trainfaultType.forEach(item => {
          this.varTableData.push({name: item})
        })
      }
    },

    // 关闭数据集信息对话框
    closeDatasetInfoDialog() {
      this.datasetInfoVisible = false
      this.chartVisible = false
      this.tableData = {
        provider: '',
        trainPath: '',
        testPath: '',
        trainLen: 0,
        testLen: 0,
        uploadTime: ''
      }
      this.varTableData = []
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
    },

    // 下载并解析CSV数据（用于图表显示）
    async downloadAndParseCSV2(url, variableName) {
      if (url === '' || url === null) {
        ElNotification.error('未获取到SCADA数据集');
        return;
      }

      try {
        this.chartLoading = true
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

          this.chartLoading = false
          return variableData;
        }
      } catch (error) {
        console.error('Error fetching and parsing file:', error);
      }
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
        data = this.traincmsdata[index].slice(0, 1024);
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

    refreshTree() {
      this.getTreeNodes(globeParams.nodeLevel)
      this.$emit("onRefreshSceneTree")
    },
    refreshData() {
      this.getTreeNodes(globeParams.nodeLevel)
    }
  }


}
</script>
<style lang="scss" scoped>
.mycon {
  width: 100%;
  height: calc(100% - 15px);
  padding-top: 10px;
}
</style>