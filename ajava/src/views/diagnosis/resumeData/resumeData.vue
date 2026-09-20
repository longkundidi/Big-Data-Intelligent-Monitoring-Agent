<template>
  <div v-loading="loading" element-loading-text="正在执行操作...">
    <el-container class="main" style="height: 100vh;background:rgb(228, 231, 237)">
      <el-row style="width: 95%;height: 80%;padding: 10px;margin: 2px auto">
        <el-col :span="11">
          <div class="left">
            <el-card style="width: 100%;height: 100%">
              <!--顶部下拉框-->
              <el-header class="selectHeader">
                <div class="update-zone">
                  <el-button class="addBtn" style="margin-bottom:15px;margin-right: 15px" @click="dlgBomUpload = true; updateType = 'Bom'">导入风机结构树
                  </el-button>
                  <el-button class="addBtn" style="margin-bottom:15px;" @click="dlgValUpload = true; updateType = 'Val'">导入感知变量
                  </el-button>
                </div>
              </el-header>
              <!--风机BOM-->
              <div style="overflow-y: auto;">
                <h3 style="border-bottom: 1px solid rgba(128, 128, 128, 0.2);">风机BOM</h3>
                <el-form class="selectForm" ref="form" :model="formdata"
                         style="margin-top: 10px;margin-left: 20px;">
                  <el-form-item class="selectItem" prop="name">
                    <el-select filterable clearable v-model="formdata.projectName" :popper-append-to-body="false"
                               placeholder="请选择风场" @clear="productModelList = [];">
                      <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                                 @click="formdata.projectName = item.project; findProductModels(item.project,item.id)">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item class="selectItem" prop="productModel">
                    <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                               placeholder="请选择机型">
                      <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                                 @click="formdata.productModel = item.productModel;formdata.projectId = item.id;">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item >
                    <el-button class="normalBtn" @click="search()">查询
                    </el-button>
                  </el-form-item>
                </el-form>

                <sw-tree v-if="templateTreeShow===true"
                         class="tyTree" ref="lazyTree" :data="templateTree" :accordion="true"
                         :defaultProps="defaultProps" :expandNode="true" :proId="formdata.projectId"
                         :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                         :showCheckBox=false :checkStrictly=true :isLazy=false @getTreeNodes="getTreeNodes"
                         @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand"
                         @eventNodeUpload="dlgUploadSonNode" @eventNodeAdd="dlgAddSonNode" @eventNodeEdit="dlgEditNode"
                         @eventNodeDelete="dlgDeleteNode" @eventNodeCopy="dlgCopyNode"
                         @eventNodeMove="dlgMoveNode" @getInstanceTreeNodes="getInstanceTreeNodes">
                </sw-tree>
              </div>
            </el-card>
          </div>
        </el-col>
        <el-col :span="13">
          <div class="right">
            <el-card style="width: 100%;height: 100%;overflow-y: auto;">
              <h3 style="border-bottom: 1px solid rgba(128, 128, 128, 0.2);height: 30px">实例风机</h3>
              <in-tree v-if="instanceTreeShow===true"
                       class="tyTree" ref="inLazyTree" :data="instanceTree" :accordion="true"
                       :defaultProps="instanceProps" :expandNode="true"
                       :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="instanceExpandKeys"
                       :showCheckBox=false :checkStrictly=true :isLazy=false @getInstanceTreeNodes="getInstanceTreeNodes"
                       @eventNodeClick="handleNodeClick" @nodeExpand="handleInstanceNodeExpand"
                       @eventNodeAdd="dlgAddSonNode" @eventNodeEdit="dlgEditNode"
                       @eventNodeDelete="dlgDeleteNode" @eventNodeCopy="dlgCopyNode"
                       @eventNodeMove="dlgMoveNode">
              </in-tree>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </el-container>
  </div>

  <el-dialog v-model="dlgBomUpload" title="风机结构树导入" width="35%" draggable>
    <div class="importDlg">
      <div style="margin-top: 10px;">
        <span style="font-size: 15px;font-weight: bold;">选择风场：</span>
        <el-select v-model="bomSelectedValue" placeholder="请选择风场" style="width: 500px;">
          <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.project"
                     @click="bomSelectedValue = item.project; formdata.projectId = item.id" ></el-option>
        </el-select>
        <el-tooltip placement="top">
          <template #content>
            若下拉框中无待导入风机的风场，请在下面的输入框中新建风场。
          </template>
          <el-icon style="margin-left: 3px;">
            <QuestionFilled/>
          </el-icon>
        </el-tooltip>
      </div>
      <div style="margin-top: 15px;">
        <span style="font-size: 15px;font-weight: bold;">新建风场：</span>
        <el-input v-model="bomInputValue" placeholder="请输入新建风场名" style="width: 500px"></el-input>
      </div>
      <div style="display: flex;justify-content: center;margin-top: 20px">
        <el-upload
            action=""
            :on-change="handleBomChange"
            :show-file-list="false"
            :auto-upload="false">
          <el-button class="addBtn" style="margin-right: 10px">上传</el-button>
        </el-upload>
        <el-button class="disMissBtn" @click="dlgBomUpload = false; this.bomSelectedValue = ''; errorShow = false">取消</el-button>
      </div>
      <el-alert
          v-if="errorShow"
          title="导入错误提示"
          type="error"
          :description="alertDescription"
          show-icon
      ></el-alert>
    </div>
  </el-dialog>
  <el-dialog v-model="dlgValUpload" title="感知变量导入" width="35%" draggable>
    <div class="importDlg">
      <div style="margin-top: 10px;">
        <span style="font-size: 15px;font-weight: bold;">选择风场：</span>
        <el-select v-model="valSelectedPro" placeholder="请选择风场" style="width: 500px;">
          <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                     @click="valSelectedPro = item.project; findProductModels(item.project,item.id)" ></el-option>
        </el-select>
      </div>
      <div style="margin-top: 10px;">
        <span style="font-size: 15px;font-weight: bold;">选择机型：</span>
        <el-select v-model="valSelectedModel" placeholder="请选择机型" style="width: 500px;">
          <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                     @click="valSelectedModel = item.productModel;" ></el-option>
        </el-select>
      </div>
      <div style="display: flex;justify-content: center;margin-top: 20px">
        <el-upload
            action=""
            :on-change="handleValChange"
            :show-file-list="false"
            :auto-upload="false">
          <el-button class="addBtn" style="margin-right: 10px">上传</el-button>
        </el-upload>
        <el-button class="disMissBtn" @click="dlgValUpload = false; this.valSelectedPro = ''; this.valSelectedModel = ''">取消</el-button>
      </div>
    </div>
  </el-dialog>
  <el-dialog v-model="dlgNodeUploadDataset" :title="titleName"
             :close-on-click-modal="false" width="50%" draggable>
    <div style="margin-top: 20px;display: flex;flex-direction: column;justify-content: center;align-items: center">
      <el-form :model="form" label-width="auto" style="max-width: 600px">
        <el-form-item label="提供者">
          <el-input v-model="form.provider"  placeholder="请输入提供者姓名"/>
        </el-form-item>
      </el-form>
      <div  style="display: flex; align-items: center;justify-content: center">
          <!-- 上传组件（移除 display:contents，改用常规布局） -->
          <el-upload
              :file-list="modeList"
              :http-request="modeUpload"
              :limit="1"
              :show-file-list="false"
              @change="handleFileChange"
              style="margin-left: 30px;"
          >
            <el-button class="addBtn" @click="datasetType='CMS_train'">上传CMS训练集</el-button>
            <el-button class="addBtn" @click="datasetType='CMS_test'">上传CMS测试集</el-button>
            <el-button class="addBtn" @click="datasetType='SCADA_train'">上传SCADA训练集</el-button>
            <el-button class="addBtn" @click="datasetType='SCADA_test'">上传SCADA测试集</el-button>
          </el-upload>
        <!-- 新增：恢复上一版本按钮 -->
        <el-button
            class="delBtn"
            @click="showRevertDialog = true"
            style="margin-left: 30px;"
        >
          恢复到版本
        </el-button>
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
      {{ selectedFileName }}
        </span>
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
    <!-- 恢复版本选择对话框 -->
    <el-dialog
        v-model="showRevertDialog"
        title="选择要恢复的数据集类型"
        width="30%"
        append-to-body
    >
      <div style="margin-bottom: 20px;">
        <el-select v-model="selectedRevertType" placeholder="请选择数据集类型" style="width: 100%">
          <el-option label="CMS训练集" value="CMS_train"></el-option>
          <el-option label="CMS测试集" value="CMS_test"></el-option>
          <el-option label="SCADA训练集" value="SCADA_train"></el-option>
          <el-option label="SCADA测试集" value="SCADA_test"></el-option>
        </el-select>
      </div>
      <div style="text-align: right;">
        <el-button @click="showRevertDialog = false">取消</el-button>
        <el-button type="primary" @click="showConfirmRevertDialog">确定</el-button>
      </div>
    </el-dialog>
    <!-- 恢复确认对话框 -->
    <el-dialog
        v-model="showConfirmRevert"
        title="确认恢复操作"
        width="30%"
        append-to-body
        @close="selectedRevertType=''"
    >
      <div style="margin-bottom: 20px;">
        确定要将 <span style="color: #409eff">{{ formattedDatasetType }}</span> 回退到上一个版本吗？
      </div>
      <div style="text-align: right;">
        <el-button @click="showConfirmRevert = false;selectedRevertType=''">取消</el-button>
        <el-button type="danger" @click="revertToPreviousVersion">确认恢复</el-button>
      </div>
    </el-dialog>
  </el-dialog>
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
  <el-dialog v-model="dlgNodeMove" title="选择目标节点" width="50%" draggable>
    <div class="treeDialog">
      <node-move-template v-if="this.updateFlag === 'template'" ref="refNodeTemplate" :proId="formdata.projectId" style="width: 100% ;height: 90%"></node-move-template>
      <node-move-instance v-if="this.updateFlag === 'instance'" ref="refNodeInstance" :proId="formdata.projectId" :turbineCode="turbineCode" style="width: 100% ;height: 90%"></node-move-instance>
      <el-button class="normalBtn" @click.stop="doNodeMove()">确定</el-button>
    </div>
  </el-dialog>
  <el-dialog v-model="showPreviewDialog" title="数据预览" style="width: 35%;height: 60%" draggable>
    <div v-loading="loadingInstance" element-loading-text="正在导入...">
      <div class="dlgPreview">
        <el-card shadow="hover" style="width: 95%;">
          <el-table :data="previewData" stripe style="width: 100%">
            <el-table-column v-for="column in previewColumns" :key="column.prop" :prop="column.prop" :label="column.label" >
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
    <div class="footerPreview">
      <el-button class="normalBtn" @click="handleClose">取 消</el-button>
      <el-button class="normalBtn" @click="handleConfirmedData(previewData)">确 认</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {
  getProductModelBySceneName,
  getProductModels,
  getProjects
} from "@/api/diagnosis/graphVis/graphVisPro";
import swTree from './templateTree.vue'
import inTree from './instanceTree.vue'
import nodeMoveTemplate from './templateForNodeMove.vue'
import nodeMoveInstance from './instanceForNodeMove.vue'
import {ElMessage, ElNotification} from "element-plus";
import * as XLSX from 'xlsx/xlsx.mjs'
import {onMounted, ref} from "vue";
import {
  addProject, createProTemplate, importVal,
  reqAddInstanceSonNode,
  reqAddSonNode, reqCopyInstanceNode,
  reqCopyNode,
  reqDeleteInstanceNodes,
  reqDeleteNodes,
  reqInstanceObjById,
  reqInstanceSonNodes,
  reqInstanceTreeNodes, reqMoveInstanceNode, reqMoveNode,
  reqObjById,
  reqPutInstanceObj,
  reqPutObj,
  reqSonNodes,
  reqTreeNodes, uploadSonNode,revertDataset
} from "@/api/diagnosis/graphVis/dataIngestion";
import {hasDuplicateFieldValues} from "@/api/diagnosis/scene/utils/sceneUtils"
import {nodeAndValOption, nodeOption} from "@/const/crud/sw/model3d/configGbomTree";
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";



let __tree
let __inTree
let globeParams = {}     //  声明一个全局参数对象

export default {
  components: {
    swTree,
    inTree,
    nodeMoveTemplate,
    nodeMoveInstance
  },
  data() {
    return {
      formdata: {
        projectId: '',
        projectName: '',
        productModel: ''
      },
      projectList: [],
      productModelList: [],
      dlgBomUpload: false,
      dlgValUpload: false,
      loadingInstance: false,
      bomInputValue: '',
      bomSelectedValue: '',
      valSelectedPro: '',
      valSelectedModel: '',
      currentPro: '',
      finalWindFarmValue: '',
      templateTree: [],
      instanceTree: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      instanceProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      defaultExpandKeys: [],
      instanceExpandKeys: [],
      templateTreeShow: false,
      instanceTreeShow: false,
      dlgNodeAddEdit: false,          //  控制新增、编辑按钮点击后，弹窗的显示和隐藏
      dlgNodeUploadDataset: false,          //  控制新增、编辑按钮点击后，弹窗的显示和隐藏
      currentUploadNodeData:{},
      datasetType:'',
      mode :{},
      modeList: [],
      form:{name:''},
      selectedFileName:'',
      datasetUrl:'',
      // 原有数据...
      showRevertDialog: false,
      showConfirmRevert: false,
      selectedRevertType: '',
      titleName: '',
      nodeOption: null,
      nodeForm: {},
      updateFlag: '',
      dlgNodeMove: false,             //  控制节点移动前，弹窗的显示和隐藏
      errorShow: false,
      errorCodes: [],
      showPreviewDialog: false,
      previewColumns: [], // 定义列信息
      previewData: [], // 解析后的数据
      updateType: '',
      turbineCode: '',
      loading: false
    }
  },
  computed: {
    // 计算属性来格式化错误代码的描述信息
    alertDescription() {
      return this.errorCodes.join('、') + '机型中存在重复的节点编码，请重新上传！';
    },
    formattedDatasetType() {
      const type = this.selectedRevertType;
      if (type == null || type === '') return "未知数据集";
      const map = {
        'CMS_train': 'CMS训练集',
        'CMS_test': 'CMS测试集',
        'SCADA_train': 'SCADA训练集',
        'SCADA_test': 'SCADA测试集'
      };
      return map[String(type).trim()] || "未知数据集";
    }
  },
  setup() {
    let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
    let inLazyTree = ref(null)
    onMounted(()=>{
      __tree = lazyTree
      __inTree = inLazyTree
    })
    return { lazyTree, inLazyTree }
  },

  mounted() {
    this.projectInit(); //获取项目
    this.initParams()
  },
  methods: {
    projectInit() {
      //获取项目
      const userId = this.$store.state.user.userInfo.userId
      getProjects({userId: userId, userRole: 'GENERAL_USER'}).then(response => {
        if(response.data.code === 0){
          let res = response.data.data
          this.projectList = res.filter((item, index, arr) =>
              index === arr.findIndex((t) => t.project === item.project)
          )
        }
      })
    },

    findProductModels(proName, proId) {
      //切换项目的时候，需要初始化值
      this.templateTree = []
      this.instanceTree = []
      this.templateTreeShow = false
      this.instanceTreeShow = false
      this.formdata.productModel = '';
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    handleBomChange (file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      this.currentPro = this.bomInputValue || this.bomSelectedValue // 确保有最终值
      if(this.currentPro === ''){
        ElMessage({
          message: "请选择或新建风场！",
          type: 'warning'
        })
      }else {
        if (fileContent) {
          if (fileType === 'xlsx' || fileType === 'xls') {
            this.importBomFile(fileContent)
          } else {
            ElMessage({
              message: "附件格式错误，请重新上传！",
              type: 'warning'
            })
          }
        } else {
          ElMessage({
            message: "请上传附件！",
            type: 'warning'
          })
        }
      }

    },

    importBomFile (obj) {
      const reader = new FileReader()
      reader.readAsArrayBuffer(obj)
      reader.onload = () => {
        const buffer = reader.result
        const bytes = new Uint8Array(buffer)
        const length = bytes.byteLength
        let binary = ''
        for (let i = 0; i < length; i++) {
          binary += String.fromCharCode(bytes[i])
        }
        const wb = XLSX.read(binary, {
          type: 'binary'
        })
        const outData = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]])
        if (outData.length === 0){
          ElMessage({
            message: "附件为空，请重新上传！",
            type: 'warning'
          })
        } else {
          this.previewColumns = Object.keys(outData[0]).map(key => ({
            prop: key,
            label: key,
          }))
          this.previewData = outData
          this.showPreviewDialog = true
        }

      }
    },

    handleValChange (file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      this.currentPro = this.valSelectedPro
      if(this.currentPro === ''){
        ElMessage({
          message: "请选择风场！",
          type: 'warning'
        })
      }else {
        if (fileContent) {
          if (fileType === 'xlsx' || fileType === 'xls') {
            this.importValFile(fileContent)
          } else {
            ElMessage({
              message: "附件格式错误，请重新上传！",
              type: 'warning'
            })
          }
        } else {
          ElMessage({
            message: "请上传附件！",
            type: 'warning'
          })
        }
      }
    },

    importValFile (obj) {
      const reader = new FileReader()
      reader.readAsArrayBuffer(obj)
      reader.onload = () => {
        const buffer = reader.result
        const bytes = new Uint8Array(buffer)
        const length = bytes.byteLength
        let binary = ''
        for (let i = 0; i < length; i++) {
          binary += String.fromCharCode(bytes[i])
        }
        const wb = XLSX.read(binary, {
          type: 'binary'
        })
        const outData = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[0]])
        if (outData.length === 0){
          this.$message({
            type: 'warning',
            message: '附件为空，请重新上传！'
          })
        }else {
          this.previewColumns = Object.keys(outData[0]).map(key => ({
            prop: key,
            label: key,
          }))
          this.previewData = outData
          this.showPreviewDialog = true
        }

      }
    },

    handleClose() {
      this.showPreviewDialog = false
      this.previewData = []
      ElMessage({
        showClose: true,
        message: '取消上传！',
      })
    },

    handleConfirmedData(outData){
      if (this.updateType === 'Bom') {
        this.handleBomData(outData);
      } else if (this.updateType === 'Val') {
        this.handleValData(outData);
      } else {
        ElMessage({
          message: "上传文件出错，请重试！",
          type: 'warning'
        })
      }
    },

    handleBomData(outData){

      const turbines = []; // 用于存放分割后的数组段
      let currentSegment = []; // 临时数组，用于构建当前段落
      let projects = []

      // 遍历outData
      for (const item of outData) {
        if (item.node_type === 'Root') {
          let projectItem = {}
          projectItem.project = this.currentPro
          projectItem.productModel = item.node_name
          projects.push(projectItem)
          // 如果当前元素满足条件且当前段落不为空，则将当前段落添加到turbines并重置currentSegment
          if (currentSegment.length > 0) {

            turbines.push(currentSegment);
            currentSegment = [];
          }
          // 将满足条件的元素加入到新段落的开始
          currentSegment.push(item);
        } else {
          // 如果当前元素不满足条件，则加入到当前段落
          currentSegment.push(item);
        }
      }

      // 确保最后的段落也被加入到结果中
      if (currentSegment.length > 0) {
        turbines.push(currentSegment)
      }

      this.errorCodes = hasDuplicateFieldValues(turbines)
      if (this.errorCodes.length !== 0) {
        // 显示错误信息
        this.$nextTick(() => {
          this.errorShow = true
          this.showPreviewDialog = false
        })
        return
      }

      this.dlgBomUpload = false
      this.bomInputValue = ''
      this.bomSelectedValue = ''
      this.updateType = ''

      addProject(projects).then(async res => {
        let ids = res.data.data
        turbines.forEach(subArray => {
          if (subArray.length > 0 && subArray[0].node_name) {
            const matchedProductModel = ids.find(model => model.productModel === subArray[0].node_name);
            if (matchedProductModel) {
              subArray.forEach(item => {
                if (item.node_name) { // 确保只有具有node_name属性的对象才被赋予pro_id
                  item.pro_id = matchedProductModel.pro_id
                }
              })
            }
          }
        })

        this.loadingInstance = true
        try {
          await createProTemplate(turbines).then(res => {
            if (res.data.code === 0) {
              this.loadingInstance = false
              this.showPreviewDialog = false
              ElMessage({
                message: "导入成功！",
                type: 'success'
              })
              this.projectInit(); //获取项目
            }
          })
        } catch (error) {
          // 错误处理
          console.error('导入时发生错误:', error)
        }
      })
    },

    async handleValData(outData) {

      outData.forEach(item => {
        // 检查对象是否有usable字段
        if (!item.hasOwnProperty('usable')) {
          // 如果没有，则添加usable字段并设置其值为1
          item.usable = 1;
        }
      })

      this.loadingInstance = true
      try {
        await importVal(this.valSelectedPro, this.valSelectedModel, outData).then(res => {
          if (res.data.code === 0) {
            this.loadingInstance = false
            this.showPreviewDialog = false
            this.dlgValUpload = false
            this.valSelectedPro = ''
            this.valSelectedModel = ''
            ElMessage({
              message: "导入成功！",
              type: 'success'
            })
          }
        })
      } catch (error) {
        // 错误处理
        console.error('导入时发生错误:', error)
      }
    },

    search(){
      //查询
      this.templateTree = []
      this.instanceTree = []
      this.getTreeNodes(globeParams.nodeLevel)
      this.templateTreeShow = true
      this.getInstanceTreeNodes(1)
      this.instanceTreeShow = true
    },

    //  初始化全局参数
    initParams(){
      globeParams.nodeLevel = 3       //    预先展开3层节点
    },

    //  节点点击消息响应
    handleNodeClick(data, node, treeNode, event){
    },

    //  模板风机节点扩展消息响应
    async handleNodeExpand(data, node, treeNode) {
      if ((node.level >= globeParams.nodeLevel)||(this._sonNodeHasLoading(data))) {
        let response = await reqSonNodes(this.formdata.projectId, data.nodeCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showCopy = true
            item.showMove = true
            item.showHelp = true
            item.children = item.leaf ? [] : [{id:'loading',name:'节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    //  实例风机节点扩展消息响应
    async handleInstanceNodeExpand(data, node, treeNode) {
      if ((node.level >= globeParams.nodeLevel)||(this._sonNodeHasLoading(data))) {
        let response = await reqInstanceSonNodes(data.nodeCode, data.turbineCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showCopy = true
            item.showMove = true
            item.showHelp = true
            item.children = item.leaf ? [] : [{id:'loading',name:'节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    /* 检查一个节点的子节点中，是否有待加载（Loading）状态的子节点
            *  这种情况只会发生在第二层节点复制后，它的子节点没有加载的情况下。这时，虽然节点层级小于globeParams.nodeLevel，但是仍然需要查询后台
            */
    _sonNodeHasLoading(node){
      let hasLoading = false
      for (let i = 0; i < node.children.length; i++) {
        if (node.children[i].id === 'loading'){
          hasLoading = true
          break
        }
      }
      return hasLoading
    },

    // 根据节点层级数，加载结构树的一组节点
    async getTreeNodes(nodeLevel) {
      try {
        let response = await reqTreeNodes(this.formdata.projectId, nodeLevel)
        if ((response.data.data)&&(response.data.data.length > 0)) {
          this.templateTree = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          for (let item of rootNodes) {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            this.templateTree.push(item)
            this.expandKeys.push(item.id)
            await this.setChildren(item, response.data.data)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点
        }
      } catch (error) {
        console.log(error)
      }
    },

    // 根据节点层级数，加载实例风机结构树的一组节点
    async getInstanceTreeNodes(nodeLevel) {
      try {
        let response = await reqInstanceTreeNodes(this.formdata.projectId, nodeLevel)
        if ((response.data.data)&&(response.data.data.length > 0)) {
          this.instanceTree = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          rootNodes.sort((a, b) => {
            const lastPartA = a.turbineCode.split('-').pop()
            const lastPartB = b.turbineCode.split('-').pop()

            // 如果最后一部分是数字，尝试将其转换为整数进行比较
            const numA = parseInt(lastPartA.match(/\d+/), 10)
            const numB = parseInt(lastPartB.match(/\d+/), 10)

            if (!isNaN(numA) && !isNaN(numB)) {
              return numA - numB
            }

            // 如果无法转换为数字，则按字符串比较
            if (lastPartA < lastPartB) {
              return -1
            }
            if (lastPartA > lastPartB) {
              return 1
            }
            return 0
          })
          for (let item of rootNodes) {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            this.instanceTree.push(item)
            this.expandKeys.push(item.id)
            let children = response.data.data
                .filter(ele => ele.turbineCode === item.turbineCode)
            await this.setChildren(item, children)
          }
          this.instanceExpandKeys = this.expandKeys            //  扩展节点
        }else {
          this.instanceTree = []
        }
      } catch (error) {
        console.log(error)
      }
    },

    //  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
    setChildren(pNode, nodeList){
      let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList)
      let children = res.sonNodes
      if (children.length === 0){
        if ((pNode.nodeType === 'Mid')||(pNode.nodeType === 'Root')){           //  如果不是叶子节点，节点前显示"+"号
          this.expandKeys = this.expandKeys.filter(item => item !== pNode.id) //  从扩展节点中删除它
          pNode.children = [{id:'loading',name:'节点加载中...'}]
        }
        return pNode
      }
      else{
        pNode.children = children
        children.forEach((item) => {
          item.showAdd = true
          item.showRemove = true
          item.showEdit = true
          item.showCopy = true
          item.showMove = true
          item.showHelp = true
          this.expandKeys.push(item.id)          //  添加到扩展节点
          this.setChildren(item, res.otherNodes)
        })
      }
    },

    //  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
    getChildrenByNodeCode(pNodeCode, nodeList){
      let sonNodes = []
      let otherNodes = []
      let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
      nodeList.forEach((item) => {
        if (regex.test(item.nodeCode))          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
          sonNodes.push(item)
        else
          otherNodes.push(item)
      })
      return {sonNodes:sonNodes, otherNodes:otherNodes}
    },


    modeUpload  (item) {
      this.mode = item.file;
    },
    // 模拟上传组件的文件变化处理
    handleFileChange(file, fileList){
      this.selectedFileName = file.name; // 将文件名赋值给 selectedFileName
    },
    // 取消文件选择
    cancelFile () {
      this.selectedFileName = ''; // 清除文件名
      this.modeList = []; // 清空文件列表
    },
    upload ()  {
      this.selectedFileName=''
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
              const res = await uploadSonNode(this.currentUploadNodeData.id, this.datasetType, this.datasetUrl,this.form.provider)
            })
            .catch(() => {
              ElNotification({
                title: 'Warning',
                message: '上传失败',
                type: 'warning',
              });
            });
      }
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
      revertDataset(this.currentUploadNodeData.id,this.selectedRevertType).then(response => {
        if(response.data.data.code!=1){
          this.$message.success('已恢复到上一次的版本');
        }else{
          this.$message.error('恢复失败: ' + response.data.data.msg);
        }
        this.selectedRevertType=''

    })
    },
    //  弹窗上传数据集
    dlgUploadSonNode (pNode, pData, flag) {
      this.currentUploadNodeData=pData
      this.titleName = "上传数据集"
      this.nodeOption = nodeAndValOption
      this.updateFlag = flag
      this.dlgNodeUploadDataset = true
      this.selectedFileName = ''; // 清除文件名
      this.modeList = []; // 清空文件列表
    },

    //  弹窗添加子节点
    dlgAddSonNode (pNode, pData, flag) {
      this.titleName = "新增子节点"
      this.nodeOption = nodeAndValOption
      this.nodeForm = {              //  节点新增弹框的表单对象
        nodeId: null,
        nodeName: '',
        nodeType: '',
        swsort: '',
        memo: '',
      }
      this.updateFlag = flag
      this.dlgNodeAddEdit = true
    },

    handleSubmit(nodeForm, done) {
      if (this.titleName === '新增子节点')
        this.nodeAdd(nodeForm, done)
      else if (this.titleName === '编辑节点')
        this.nodeEdit(nodeForm, done)
    },

    //  节点子添加
    async nodeAdd(nodeForm, done) {
      let pNode
      if(this.updateFlag === 'template'){
        pNode = __tree.value.getCurrentNodeData()     // 获取父节点
      }else {
        pNode = __inTree.value.getCurrentNodeData()
      }
      try {
        let response
        if(this.updateFlag === 'template'){
          response = await reqAddSonNode(this.formdata.projectId, pNode.id, nodeForm)
        }else {
          response = await reqAddInstanceSonNode(pNode.id, nodeForm)
        }
        if (response.data.code === 0){
          let newNode = response.data.data        //  给新添加的节点增加按钮属性
          newNode.showAdd = true
          newNode.showRemove = true
          newNode.showEdit = true
          newNode.showCopy = true
          newNode.showMove = true
          newNode.showHelp = true
          newNode.children = []          //  注意：新添加的节点都是叶子节点
          let res
          if(this.updateFlag === 'template'){
            res = await reqSonNodes(this.formdata.projectId, pNode.nodeCode)
          }else {
            res = await reqInstanceSonNodes(pNode.nodeCode, pNode.turbineCode)
          }
          if (res.data.data) {
            res.data.data.forEach((item) => {
              item.showAdd = true
              item.showRemove = true
              item.showEdit = true
              item.showCopy = true
              item.showMove = true
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
        }
        this.dlgNodeAddEdit = false
        done()
      } catch (error) {
        console.log(error)
      }
    },


    dlgEditNode(node, data, flag) {
      this.titleName = "编辑节点"
      this.nodeOption = nodeOption
      this.updateFlag = flag
      if(this.updateFlag === 'template'){
        reqObjById(this.formdata.projectId, data.id).then(response => {
          this.nodeForm = response.data.data          //  填写可编辑字段
          this.dlgNodeAddEdit = true
        })
      }else {
        reqInstanceObjById(data.id).then(response => {
          this.nodeForm = response.data.data          //  填写可编辑字段
          this.dlgNodeAddEdit = true
        })
      }
    },

    //  节点编辑
    async nodeEdit(nodeForm, done) {
      let response
      if(this.updateFlag === 'template'){
        response = await reqPutObj(this.formdata.projectId, nodeForm)
      }else {
        response = await reqPutInstanceObj(nodeForm)
      }
      if (response.data.code === 0) {
        this.$message.success('节点编辑成功')
        this.dlgNodeAddEdit = false
        if ((response.data.data.nodeType !== 'Root')&&(response.data.data.nodeType !== 'Root-Leaf')) {       //  如果存在父节点
          let newData = {
            id: response.data.data.id,
            name: response.data.data.name,
            memo: response.data.data.memo
          }
          if(this.updateFlag === 'template'){
            __tree.value.updateNode(newData)            //  更新节点状态
          }else {
            __inTree.value.updateNode(newData)
          }
        }else{
          if(this.updateFlag === 'template'){
            await this.getTreeNodes(globeParams.nodeLevel)
          }else {
            await this.getInstanceTreeNodes(1)
          }
        }
      }
      done()
    },

    async dlgDeleteNode(data, flag) {
      this.updateFlag = flag
      try {
        await this.$confirm('此操作将删除当前节点及其所有子节点, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response
        this.loading = true
        if(this.updateFlag === 'template'){
          response = await reqDeleteNodes({proId: this.formdata.projectId, nodeCode: data.nodeCode})
        }else {
          response = await reqDeleteInstanceNodes(this.formdata.projectId, data.turbineCode, data.nodeCode)
        }
        if (response.data.code !== 0) {
          this.$message({
            type: 'error',
            message: '节点删除失败!'
          })
          this.loading = false
        } else {
          if ((data.nodeType !== 'Root')&&(data.nodeType !== 'Root-Leaf')){       //  如果存在父节点
            if(this.updateFlag === 'template'){
              let pNode = __tree.value.removeSonNode(data.id)
              __tree.value.setNodeSelected(pNode.data.id)             //  将父节点设置为选中状态
            }else {
              let pNode = __inTree.value.removeSonNode(data.id)
              __inTree.value.setNodeSelected(pNode.data.id)             //  将父节点设置为选中状态
            }
          }else{
            if(this.updateFlag === 'template'){
              this.templateTree = []
            }else {
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

    async dlgCopyNode(data, flag) {
      this.updateFlag = flag
      try {
        await this.$confirm('此操作将复制当前节点及其所有子节点到同一个父节点下, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response
        if(this.updateFlag === 'template'){
          response = await reqCopyNode(this.formdata.projectId, data.nodeCode)
        }else {
          response = await reqCopyInstanceNode({turbineCode: data.turbineCode, nodeCode: data.nodeCode})
        }
        if (response.data.code === 200) {
          let newNode = response.data.data        //  给新复制的节点增加按钮属性
          newNode.showAdd = true
          newNode.showRemove = true
          newNode.showEdit = true
          newNode.showCopy = true
          newNode.showMove = true
          newNode.showHelp = true
          newNode.children = newNode.nodeType === 'Leaf' ? [] : [{id:'loading',name:'节点加载中...'}]          //  注意：这里新复制的节点可能没有完成子节点的加载
          if(this.updateFlag === 'template'){
            let pNode = __tree.value.getParentNode(data.id)         //  获取父节点
            __tree.value.addSonNode(pNode.data, newNode)
          }else {
            let pNode = __inTree.value.getParentNode(data.id)         //  获取父节点
            __inTree.value.addSonNode(pNode.data, newNode)
          }
          this.$message.success('节点复制成功')
        }
      }catch (error) {
        console.log(error)
      }
    },

    dlgMoveNode(data, flag) {
      this.sourceNode = data      //  保存源节点
      this.dlgNodeMove = true
      this.updateFlag = flag
      this.turbineCode = data.turbineCode
    },

    async doNodeMove(){
      let targetNode
      if(this.updateFlag === 'template'){
        targetNode = this.$refs.refNodeTemplate.getCurrentNode()        //  获取目标节点
      }else {
        targetNode = this.$refs.refNodeInstance.getCurrentNode()        //  获取目标节点
      }
      if (targetNode){
        this.dlgNodeMove = false
        await this.$confirm('确定执行节点移动操作吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response
        if(this.updateFlag === 'template'){
          response = await reqMoveNode(this.formdata.projectId, this.sourceNode.nodeCode,targetNode.nodeCode)
        }else {
          response = await reqMoveInstanceNode(this.sourceNode.turbineCode, this.sourceNode.nodeCode,targetNode.nodeCode)
        }
        if (response.data.code === 200){
          if(this.updateFlag === 'template'){
            await this.getTreeNodes(response.data.data.nodeLevel)                    //  更新结构树，注意：这里需要根据被移动节点的最新层级决定加载的节点级数，确保被移动节点被加载进结构树
            __tree.value.setNodeSelected(response.data.data.id)                      //  将被移动的节点设置为选中状态
          }else {
            await this.getInstanceTreeNodes(response.data.data.nodeLevel)
            __inTree.value.setNodeSelected(response.data.data.id)
          }
          this.$message.success('节点移动成功')
        }
      }
    },

  }
}
</script>

<style lang="scss" scoped>

.update-zone{
  text-align:right;
  margin-bottom: 5px;
  margin-top: 15px;
  margin-right: 5px;
}

.selectHeader {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 3%;
  line-height: 5%;
  margin: 1%;
  align-items: center;
}

.addBtn{
  height: 28px;
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

.main {
  display: flex;
  flex-direction: row;
}

.left {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-content: center;
}

.right {
  height: 100vh;
  margin-left: 5px;
}

.el-dialog__header {
  background-color: #f5f7fa; /* 示例：改变背景颜色 */
  padding: 15px 20px; /* 示例：调整内边距 */
  font-size: 18px; /* 示例：改变字体大小 */
  color: #303133; /* 示例：改变文字颜色 */
}

.importDlg{
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  margin-left: 20px;
  .el-button {
    height: 30px;
  }
}

.treeDialog{
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  .el-button{
    background-color: cadetblue;
    width: 65px;
    height: 40px;
    margin-top: 15px;
    border-color: white;
    &:hover {
      background-color: chocolate; /* 设置悬浮时的背景色 */
    }
  }
}

.el-alert {
  margin: 20px 0 0;
}
.el-alert:first-child {
  margin: 0;
}

.dlgPreview{
  height: 450px;
  overflow: auto;
  margin-left: 20px;
}

.footerPreview{
  text-align: center;
  margin-top: 20px;
  .el-button {
    height: 30px;
  }
}

</style>
