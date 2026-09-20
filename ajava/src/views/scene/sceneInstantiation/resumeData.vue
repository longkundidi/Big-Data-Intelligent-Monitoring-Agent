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
                  <el-button class="addBtn" style="margin-bottom:15px;" @click="dlgSceneAdd = true; updateType = 'Val'">
                    新建场景
                  </el-button>
                </div>
              </el-header>
              <div style="overflow-y: auto;">
                <h3 style="border-bottom: 1px solid rgba(128, 128, 128, 0.2);"></h3>
                <el-form class="selectForm" ref="form" :model="formdata"
                         style="margin-top: 10px;margin-left: 20px;">
                  <el-form-item class="selectItem" prop="name">
                    <el-select filterable clearable v-model="formdata.projectName" :popper-append-to-body="false"
                               placeholder="请选择场景" @clear="productModelList = [];">
                      <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                                 @click="formdata.projectName = item.project; findProductModels(item.project,item.id)">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item class="selectItem" prop="productModel">
                    <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                               placeholder="请选择机型">
                      <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel"
                                 :value="item.id"
                                 @click="formdata.productModel = item.productModel;formdata.projectId = item.id;">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-button class="normalBtn" @click="search()">查询
                    </el-button>
                  </el-form-item>
                </el-form>
                <div class="import-data">
                  <el-button class="addBtn" v-if="instanceTreeShow===true" @click="dlgUploadUnitData = true">
                    导入机组数据
                  </el-button>
                  <el-button class="delBtn" v-if="instanceTreeShow===true" @click="handleDeleteScene">
                    删除该场景
                  </el-button>
                </div>

                <sw-tree v-if="templateTreeShow===true"
                         class="tyTree" ref="lazyTree" :data="templateTree" :accordion="true"
                         :defaultProps="defaultProps" :expandNode="true" :proId="formdata.projectId"
                         :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                         :showCheckBox=false :checkStrictly=true :isLazy=false @getTreeNodes="getTreeNodes"
                         @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand"
                         @getInstanceTreeNodes="getInstanceTreeNodes">
                </sw-tree>
              </div>
            </el-card>
          </div>
        </el-col>
        <el-col :span="13">
          <div class="right">
            <el-card style="width: 100%;height: 100%;overflow-y: auto;">
              <h3 style="border-bottom: 1px solid rgba(128, 128, 128, 0.2);height: 30px">实例对象</h3>
              <in-tree v-if="instanceTreeShow===true"
                       class="tyTree" ref="inLazyTree" :data="instanceTree" :accordion="true"
                       :defaultProps="instanceProps" :expandNode="true"
                       :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="instanceExpandKeys"
                       :showCheckBox=false :checkStrictly=true :isLazy=false
                       @getInstanceTreeNodes="getInstanceTreeNodes"
                       @eventNodeClick="handleNodeClick" @nodeExpand="handleInstanceNodeExpand"
                       @eventNodeView="handleViewData"
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

  <el-dialog v-model="dlgUploadUnitData" title="导入机组数据" :close-on-click-modal="false" width="35%" draggable>
    <div class="importDlg">
      <div style="margin-top: 10px;">
        <span style="font-size: 18px; font-weight: bold;">选择的场景：</span>
        <span style="font-size: 18px;">{{ formdata.projectName || '未选择' }}</span>
      </div>

      <div style="margin-top: 15px;">
        <span style="font-size: 18px; font-weight: bold;">选择的机型：</span>
        <span style="font-size: 18px;">{{ formdata.productModel || '未选择' }}</span>
      </div>
      <div class="update-zone" style="display: flex; align-items: center;">
        <el-upload
            action=""
            :on-change="uploadUnitData"
            :show-file-list="false"
            :auto-upload="false">
          <el-button class="addBtn" style="margin-right: 10px">导入机组数据</el-button>
        </el-upload>
        <el-button type="primary" plain size="small" @click="downloadTemplate" style="margin-left: 12px;">
          下载模板
        </el-button>
      </div>
      <el-dialog v-model="showPreviewDialog" title="数据预览" style="width: 35%;height: 60%" draggable>
        <div v-loading="loadingInstance" element-loading-text="数据校验...">
          <div class="dlgPreview">
            <el-card shadow="hover" style="width: 95%;">
              <el-table :data="previewData" stripe style="width: 100%">
                <el-table-column v-for="column in previewColumns" :key="column.prop" :prop="column.prop"
                                 :label="column.label">
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </div>
        <div class="footerPreview">
          <el-button class="normalBtn" @click="handleClose">取 消</el-button>
          <el-button class="normalBtn" @click="handleConfirmedData(uploadData)">确 认</el-button>
        </div>
      </el-dialog>
      <div style="display: flex;justify-content: center;margin-top: 20px">
        <el-button @click="dlgUploadUnitData = false">取消</el-button>
        <el-button type="primary" :loading="loadingInstance" @click="handleSubmitUnitData">保存</el-button>
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
  <el-dialog v-model="dlgSceneAdd" @open="fetchAllProductModels"  @close="resetSceneForm" title="新建场景" :close-on-click-modal="false" width="50%"
             draggable>
    <div class="importDlg">
      <div style="margin-top: 10px; display: flex; align-items: center;">
      <span style="width: 110px; font-size: 15px; font-weight: bold; flex-shrink: 0;">
        输入场景名称：
      </span>
        <el-input
            v-model="sceneName"
            placeholder="请输入场景名称"
            style="width: 750px;"
        />
      </div>
      <div style="margin-top: 10px; display: flex; align-items: center;">
      <span style="width: 110px; font-size: 15px; font-weight: bold; flex-shrink: 0;">
        选择机型：
      </span>
        <el-select
            v-model="valSelectedAllModel"
            multiple
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="4"
            placeholder="请选择机型"
            style="width: 750px;"
        >
          <el-option
              v-for="item in productAllModelList"
              :key="item.id"
              :label="item.productAllModel"
              :value="item.id"
          />
        </el-select>
      </div>
      <div style="display: flex;justify-content: center;margin-top: 20px">
        <!--        <el-button class="normalBtn" icon="el-icon-check" style="margin-right: 10px" @click="submitSceneData">确认</el-button>-->
        <el-button class="normalBtn" style="margin-right: 10px" @click="submitSceneData">
          <el-icon style="margin-right:5px;font-size: 18px;">
            <CircleCheck/>
          </el-icon>
          确认
        </el-button>

        <el-button class="disMissBtn"
                   @click="dlgSceneAdd = false; this.valSelectedPro = ''; this.valSelectedModel = ''">取消
        </el-button>
      </div>
    </div>
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
      <node-move-template v-if="this.updateFlag === 'template'" ref="refNodeTemplate" :proId="formdata.projectId"
                          style="width: 100% ;height: 90%"></node-move-template>
      <node-move-instance v-if="this.updateFlag === 'instance'" ref="refNodeInstance" :proId="formdata.projectId"
                          :turbineCode="turbineCode" style="width: 100% ;height: 90%"></node-move-instance>
      <el-button class="normalBtn" @click.stop="doNodeMove()">确定</el-button>
    </div>
  </el-dialog>
  <el-dialog v-model="showPreviewDialog" title="数据预览" style="width: 60%; height: 65%" draggable>
    <div v-loading="loadingInstance" element-loading-text="数据校验...">
      <div class="dlgPreview">
        <el-card shadow="hover" style="width: 95%;">
          <el-table :data="previewData" stripe style="width: 100%">
            <el-table-column v-for="column in previewColumns" :key="column.prop" :prop="column.prop"
                             :label="column.label">
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
  <el-dialog
      title="机组数据详情"
      v-model="showDataDialog"
      width="60%"
  >
    <el-descriptions
        :column="2"
        border
        class="custom-desc"
    >
      <!-- 不可编辑字段 -->
      <el-descriptions-item label="节点名称">{{ selectedNodeData.name || '—' }}</el-descriptions-item>
      <el-descriptions-item label="节点编码">{{ selectedNodeData.nodeCode || '—' }}</el-descriptions-item>
      <el-descriptions-item label="节点类型">{{ selectedNodeData.nodeType || '—' }}</el-descriptions-item>
      <el-descriptions-item label="节点层级">{{ selectedNodeData.nodeLevel || '—' }}</el-descriptions-item>
      <el-descriptions-item label="风机编号">{{ selectedNodeData.turbineCode || '—' }}</el-descriptions-item>

      <!-- 可编辑字段 -->
      <el-descriptions-item label="生产厂家">
        <template v-if="isEditing">
          <el-input v-model="editForm.manufacturer"/>
        </template>
        <template v-else>{{ selectedNodeData.manufacturer || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="采购时间">
        <template v-if="isEditing">
          <el-date-picker v-model="editForm.purchaseDate" type="date" placeholder="选择日期"/>
        </template>
        <template v-else>{{ selectedNodeData.purchaseDate || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="生产日期">
        <template v-if="isEditing">
          <el-date-picker v-model="editForm.productionDate" type="date" placeholder="选择日期"/>
        </template>
        <template v-else>{{ selectedNodeData.productionDate || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="安装时间">
        <template v-if="isEditing">
          <el-date-picker v-model="editForm.installationDate" type="date" placeholder="选择日期"/>
        </template>
        <template v-else>{{ selectedNodeData.installationDate || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="安装负责人">
        <template v-if="isEditing">
          <el-input v-model="editForm.installationLeader"/>
        </template>
        <template v-else>{{ selectedNodeData.installationLeader || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="维护人员">
        <template v-if="isEditing">
          <el-input v-model="editForm.maintenancePerson"/>
        </template>
        <template v-else>{{ selectedNodeData.maintenancePerson || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="更换人员">
        <template v-if="isEditing">
          <el-input v-model="editForm.replacementPerson"/>
        </template>
        <template v-else>{{ selectedNodeData.replacementPerson || '—' }}</template>
      </el-descriptions-item>

      <el-descriptions-item label="负责人">
        <template v-if="isEditing">
          <el-input v-model="editForm.responsiblePerson"/>
        </template>
        <template v-else>{{ selectedNodeData.responsiblePerson || '—' }}</template>
      </el-descriptions-item>
    </el-descriptions>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button v-if="!isEditing" class="editBtn" @click="startEdit">编辑</el-button>
      <el-button v-if="isEditing" type="text" class="normalBtn" @click="saveEdit">保存</el-button>
      <el-button v-if="isEditing" @click="cancelEdit">取消</el-button>
      <el-button v-else="isEditing" @click="showDataDialog = false">关闭</el-button>
    </template>
  </el-dialog>

</template>

<script>
import {
  getProductModelBySceneName, getProductModels,
  getProjects, saveScene, getModelStructure, uploadUnitData, updatePermission, getMetaModelIdByUserId,deleteSceneNode
} from "@/api/diagnosis/graphVis/graphVisPro";
import swTree from './templateTree.vue'
import inTree from './instanceTree.vue'
import nodeMoveTemplate from './templateForNodeMove.vue'
import nodeMoveInstance from './instanceForNodeMove.vue'
import {ElMessage, ElNotification,ElMessageBox} from "element-plus";
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
  uploadSonNode, getTreeNodesByScene, reqSonNodesByScene
} from "@/api/diagnosis/graphVis/dataIngestion";
import {hasDuplicateFieldValues, updateUnitData} from "@/api/diagnosis/scene/utils/sceneUtils"
import {nodeAndValOption, nodeOption} from "@/const/crud/sw/model3d/configGbomTree";
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {CircleCheck, View} from "@element-plus/icons-vue";


let __tree
let __inTree
let globeParams = {}     //  声明一个全局参数对象

export default {
  components: {
    View,
    CircleCheck,
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
        productModel: '',
        sceneId: ''  // 场景ID
      },
      metaModelName: '',
      modelName: '',
      metaModelId: '',
      sceneName: '',
      projectList: [],
      allProjectList: [],  // 保存完整的项目列表（不过滤）
      productModelList: [],
      valSelectedAllModel: [],        // 用户已选择的机型 ID 列表
      productAllModelList: [],         // 所有机型列表
      // dlgBomUpload: false,
      dlgValUpload: false,
      dlgSceneAdd: false,
      loadingInstance: false,
      dlgUploadUnitData: false,
      modelStructureList: [],
      uploadData: [], //要上传的机组数据
      showDataDialog: false,
      selectedNodeData: null,
      isEditing: false,
      editForm: {},
      detailTableData: [],
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
      loading: false,
      instanceRootNames:[],
    }
  },
  computed: {
    // 计算属性来格式化错误代码的描述信息
    alertDescription() {
      return this.errorCodes.join('、') + '机型中存在重复的节点编码，请重新上传！';
    },
  },
  setup() {
    let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
    let inLazyTree = ref(null)
    onMounted(() => {
      __tree = lazyTree
      __inTree = inLazyTree
    })
    return {lazyTree, inLazyTree}
  },

  mounted() {
    this.initParams()
    this.projectInit().then(() => {
      this.fetchAllProductModels()
    })
  },
  methods: {
    projectInit() {
      const userId = this.$store.state.user.userInfo.userId
      //获取元模型
      getMetaModelIdByUserId({userId: userId, userRole: 'GENERAL_USER'}).then(response => {
        if (response.data.code === 0) {
          this.metaModelId = response.data.data.metaModelId;
          this.metaModelName = response.data.data.metaModelName;
          console.log("getMetaModelIdByUserId:"+this.metaModelName);
        }
      })
      //获取项目
      getProjects({userId: userId, userRole: 'GENERAL_USER'}).then(response => {
        if (response.data.code === 0) {
          let res = response.data.data
          // 保存完整的项目列表
          this.allProjectList = res
          // 过滤重复的场景名称用于显示
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
      this.formdata.productModel = ''
      this.formdata.sceneId = proId  // 保存场景ID
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    fetchAllProductModels() {
      console.log("fetchAllProductModels:"+this.metaModelName);
      getProductModels(this.metaModelName)
          .then(res => {
            if (res.data && res.data.data) {
              // 假设接口字段就是 id 和 productAllModel
              this.productAllModelList = res.data.data.map(item => ({
                id: item.id,
                productAllModel: item.productModel
              }))
            } else {
              console.warn('未获取到机型数据')
            }
          })
          .catch(err => {
            console.error('获取机型数据失败:', err)
          })
    },

    submitSceneData() {
      if (!this.sceneName) {
        this.$message.warning('请输入风场名称')
        return
      }
      if (this.valSelectedAllModel.length === 0) {
        this.$message.warning('请选择至少一个机型')
        return
      }
      const payload = {
        sceneName: this.sceneName,
        modelIds: this.valSelectedAllModel,
        metaModelName: this.metaModelName,
        userId: this.$store.state.user.userInfo.userId
      }

      saveScene(payload)
          .then(res => {
            this.$message.success('保存成功')
            updatePermission(payload).then(res => {
              if (res.code === 200 || '200') {
                this.$message.success('权限更新成功')
                this.projectInit()
              } else {
                this.$message.warning('权限更新异常：' + res.message)
              }
            }).catch(err => {
              this.$message.error('权限更新失败：' + err.message)
            })
            this.sceneName = ''
            this.valSelectedAllModel = []
          })
          .catch(err => {
            this.$message.error('提交失败：' + err.message)
          })
      this.dlgSceneAdd = false
    },
    resetSceneForm() {
      this.sceneName = ''
      this.valSelectedAllModel = []
    },

    downloadTemplate() {
      const headers = ['node_name', 'node_code', 'node_type', 'production_date', 'purchase_date', 'installation_date', 'installation_leader',
        'responsible_person', 'component_model', 'manufacturer', 'maintenance_record', 'maintenance_person', 'fault_record',
        'fault_reporter', 'replacement_record', 'replacement_person']
      const dataRows = [['风机#1', 'TM1', 'Root',], ['轮毂', 'TM1-1', 'Mid']]
      const worksheetData = [headers, ...dataRows]
      const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, worksheet, 'unitdata_template')
      XLSX.writeFile(workbook, 'unitdata_template.xlsx')
    },

    uploadUnitData(file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      if (fileContent) {
        if (fileType === 'xlsx' || fileType === 'xls') {
          this.importBomFile(fileContent)
        } else {
          ElMessage({message: "附件格式错误，请重新上传！", type: 'warning'})
        }
      } else {
        ElMessage({message: "请上传附件！", type: 'warning'})
      }
    },

    handleSubmitUnitData() {
      if (!this.uploadData || this.uploadData.length === 0) {
        this.$message.warning('请先导入机组数据');
        return;
      }

      if (!this.formdata.projectId) {
        this.$message.warning('请先选择项目（机型）');
        return;
      }

      this.loadingInstance = true; // 开启 loading
      const groupedData = [];
      let currentInstance = [];

      this.uploadData.forEach(item => {
        // 添加机型ID（pro_id）
        const newItem = {
          ...item,
          pro_id: this.formdata.projectId
        };

        if (item.node_type === 'Root') {
          if (currentInstance.length > 0) {
            groupedData.push(currentInstance);
          }
          currentInstance = [newItem];
        } else {
          currentInstance.push(newItem);
        }
      });

      if (currentInstance.length > 0) {
        groupedData.push(currentInstance);
      }
      console.log(groupedData);
      // 调用后端接口
      uploadUnitData(groupedData)
          .then(res => {
            if (res.status === 200 || res.status === '200') {
              this.$message.success('上传成功');
              this.uploadData = [];
              this.dlgUploadUnitData = false;
            } else {
              this.$message.error(res.message || '上传失败');
            }
          })
          .catch(err => {
            console.error(err);
            this.$message.error('上传出错');
          }).finally(() => {
        this.loadingInstance = false; // 无论成功或失败都关闭 loading
      });
    },


    handleBomChange(file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      this.currentPro = this.bomInputValue || this.bomSelectedValue // 确保有最终值
      if (this.currentPro === '') {
        ElMessage({
          message: "请选择或新建风场！",
          type: 'warning'
        })
      } else {
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

    importBomFile(obj) {
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
        if (outData.length === 0) {
          ElMessage({
            message: "附件为空，请重新上传！",
            type: 'warning'
          })
        } else {
          // 转换日期字段：将Excel日期序列号转换为标准日期格式
          const dateFields = ['production_date', 'purchase_date', 'installation_date']
          outData.forEach(row => {
            dateFields.forEach(field => {
              if (row[field] !== undefined && row[field] !== null && row[field] !== '') {
                // 检查是否为数字类型（Excel日期序列号）
                if (typeof row[field] === 'number') {
                  // 将Excel日期序列号转换为JavaScript Date对象
                  const date = XLSX.SSF.parse_date_code(row[field])
                  if (date) {
                    // 格式化为 YYYY-MM-DD 格式
                    row[field] = `${date.y}-${String(date.m).padStart(2, '0')}-${String(date.d).padStart(2, '0')}`
                  }
                }
              }
            })
          })

          this.previewColumns = Object.keys(outData[0]).map(key => ({
            prop: key,
            label: key,
          }))
          this.uploadData = outData
          this.previewData = outData.filter(node =>
              ['Root', 'Mid', 'Leaf'].includes(node.node_type)
          )
          this.showPreviewDialog = true
        }

      }
    },

    handleValChange(file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      this.currentPro = this.valSelectedPro
      if (this.currentPro === '') {
        ElMessage({
          message: "请选择风场！",
          type: 'warning'
        })
      } else {
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

    importValFile(obj) {
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
        if (outData.length === 0) {
          this.$message({
            type: 'warning',
            message: '附件为空，请重新上传！'
          })
        } else {
          this.previewColumns = Object.keys(outData[0]).map(key => ({
            prop: key,
            label: key,
          }))
          this.uploadData = outData
          this.previewData = outData.filter(node =>
              ['Root', 'Mid', 'Leaf'].includes(node.node_type)
          )
          this.showPreviewDialog = true
        }

      }
    },

    handleClose() {
      this.showPreviewDialog = false
      this.previewData = []
      this.uploadData = [];
      ElMessage({
        showClose: true,
        message: '取消上传！',
      })
    },

    handleConfirmedData(outData) {
      this.loadingInstance = true

      getModelStructure(this.formdata.projectId).then(res => {
        if (res && res.data) {
          this.modelStructureList = res.data.data.filter(node =>
              ['Root', 'Mid', 'Leaf'].includes(node.nodeType)
          );

          const filteredOutData = outData.filter(node =>
              ['Root', 'Mid', 'Leaf'].includes(node.node_type)
          );

          // 校验 outData 中 Root 节点是否重复
          const duplicateRootNames = filteredOutData
              .filter(node => node.node_type === 'Root' && this.instanceRootNames.includes(node.node_name))
              .map(node => node.node_name);

          if (duplicateRootNames.length > 0) {
            this.$message.error(`Root 节点名称已存在：${[...new Set(duplicateRootNames)].join('、')}`);
            return;
          }

          // 将 outData 按 Root 拆成多个实例风机结构
          const instances = [];
          let currentInstance = [];

          for (const item of filteredOutData) {
            if (item.node_type === 'Root') {
              if (currentInstance.length > 0) {
                instances.push(currentInstance);
              }
              currentInstance = [item];
            } else {
              currentInstance.push(item);
            }
          }
          if (currentInstance.length > 0) {
            instances.push(currentInstance);
          }

          // 检查是否有重复的 Root node_name
          const rootNames = new Set();
          const duplicateRoots = [];

          instances.forEach(instance => {
            const rootNode = instance.find(node => node.node_type === 'Root');
            if (rootNode) {
              if (rootNames.has(rootNode.node_name)) {
                duplicateRoots.push(rootNode.node_name);
              } else {
                rootNames.add(rootNode.node_name);
              }
            }
          });

          if (duplicateRoots.length > 0) {
            this.$message.error(`存在重复 Root 节点名称：${[...new Set(duplicateRoots)].join('、')}`);
            return;
          }

          const modelTree = this.modelStructureList;

          // 实例风机是否是机型结构的子树
          const errors = [];

          instances.forEach((instance, index) => {
            const {isSubtree, reason} = matchSubtree(instance, modelTree)

            if (!isSubtree) {
              errors.push(`第 ${index + 1} 个实例风机结构不匹配：${reason}`);
            }
          });

          if (errors.length > 0) {
            this.$message.error(errors.join('；'));
            return;
          }

          // 校验通过
          this.$message.success("机组数据结构与机型结构一致");
          this.$emit('outBomData', outData);
          this.showPreviewDialog = false;
          this.visible = false;
          this.$emit('update:modelValue', false);
        } else {
          this.$message.error('获取机型结构失败，返回数据格式不正确');
        }
      }).catch(err => {
        console.error(err);
        this.$message.error('获取机型结构失败');
      }).finally(() => {
        this.loadingInstance = false; // 无论成功或失败都会执行
      });

      // 判断 instanceList 是否是 modelList 的前缀子树
      function matchSubtree(instance, modelTree) {
        for (const instNode of instance) {
          const matched = modelTree.some(modelNode => {
            const typeMatches = instNode.node_type === modelNode.nodeType;
            const codeMatches = instNode.node_code === modelNode.nodeCode;
            const nameMatches = instNode.node_type === "Root"
                ? true // Root 类型不比对 node_name
                : instNode.node_name === modelNode.nodeName;

            return typeMatches && codeMatches && nameMatches;
          });

          if (!matched) {
            const reason = `未匹配节点：node_name=${instNode.node_name},  node_code=${instNode.node_code}, node_type=${instNode.node_type}`;
            return {isSubtree: false, reason};
          }
        }
        return {isSubtree: true, reason: ""};
      }

    },

    handleBomData(outData) {

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

    search() {
      //查询
      this.templateTree = []
      this.instanceTree = []
      this.getTreeNodes(globeParams.nodeLevel)
      this.templateTreeShow = true
      this.getInstanceTreeNodes(1)
      this.instanceTreeShow = true
    },

    // 开始编辑
    startEdit() {
      this.editForm = {...this.selectedNodeData}; // 深拷贝一份
      this.isEditing = true;
    },

    // 取消编辑
    cancelEdit() {
      this.isEditing = false;
    },

    // 保存编辑（提交到后端）
    async saveEdit() {
      try {
        // 转换日期字段：将 Date 对象转换为 YYYY-MM-DD HH:mm:ss 格式
        const formData = {...this.editForm}
        const dateFields = ['productionDate', 'purchaseDate', 'installationDate']
        dateFields.forEach(field => {
          if (formData[field] && formData[field] instanceof Date) {
            const date = formData[field]
            formData[field] = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} 00:00:00`
          }
        })

        const res = await updateUnitData(formData);

        if (res.data.code === 0 || '0') {
          // 更新本地显示数据
          Object.assign(this.selectedNodeData, formData);
          this.isEditing = false;
          ElMessage.success("保存成功");
        } else {
          ElMessage.error(res.data.message || "保存失败");
        }
      } catch (e) {
        console.error(e);
        ElMessage.error("保存失败!!!");
      }
    },

    //  初始化全局参数
    initParams() {
      globeParams.nodeLevel = 3       //    预先展开3层节点
    },

    //  节点点击消息响应
    handleNodeClick(data, node, treeNode, event) {
    },

    //  模板风机节点扩展消息响应
    async handleNodeExpand(data, node, treeNode) {
      if ((node.level >= globeParams.nodeLevel) || (this._sonNodeHasLoading(data))) {
        let response = await reqSonNodesByScene(this.formdata.projectId, data.nodeCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showCopy = true
            item.showMove = true
            item.showHelp = true
            item.showView = true
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    //  实例风机节点扩展消息响应
    async handleInstanceNodeExpand(data, node, treeNode) {
      if ((node.level >= globeParams.nodeLevel) || (this._sonNodeHasLoading(data))) {
        let response = await reqInstanceSonNodes(data.nodeCode, data.turbineCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showCopy = true
            item.showMove = true
            item.showHelp = true
            item.showView = true
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    /* 检查一个节点的子节点中，是否有待加载（Loading）状态的子节点
            *  这种情况只会发生在第二层节点复制后，它的子节点没有加载的情况下。这时，虽然节点层级小于globeParams.nodeLevel，但是仍然需要查询后台
            */
    _sonNodeHasLoading(node) {
      let hasLoading = false
      for (let i = 0; i < node.children.length; i++) {
        if (node.children[i].id === 'loading') {
          hasLoading = true
          break
        }
      }
      return hasLoading
    },

    // 根据节点层级数，加载结构树的一组节点
    async getTreeNodes(nodeLevel) {
      try {
        let response = await getTreeNodesByScene(this.formdata.projectId, nodeLevel)
        this.modelName = response.data.data[0].name
        if ((response.data.data) && (response.data.data.length > 0)) {
          this.templateTree = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          for (let item of rootNodes) {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            item.showView = true
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
        if ((response.data.data) && (response.data.data.length > 0)) {
          this.instanceTree = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          this.instanceRootNames = response.data.data
              .filter(ele => ele.nodeType === "Root")
              .map(ele => ele.name)
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
            item.showView = true
            this.instanceTree.push(item)
            this.expandKeys.push(item.id)
            let children = response.data.data
                .filter(ele => ele.turbineCode === item.turbineCode)
            await this.setChildren(item, children)
          }
          this.instanceExpandKeys = this.expandKeys            //  扩展节点
        } else {
          this.instanceTree = []
        }
      } catch (error) {
        console.log(error)
      }
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
          item.showCopy = true
          item.showMove = true
          item.showHelp = true
          item.showView = true
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
    },
    //查看机组信息
    handleViewData(node, data) {
      this.selectedNodeData = data;
      this.showDataDialog = true;
    },
    //  弹窗添加子节点
    dlgAddSonNode(pNode, pData, flag) {
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
      if (this.updateFlag === 'template') {
        pNode = __tree.value.getCurrentNodeData()     // 获取父节点
      } else {
        pNode = __inTree.value.getCurrentNodeData()
      }
      try {
        let response
        if (this.updateFlag === 'template') {
          response = await reqAddSonNode(this.formdata.projectId, pNode.id, nodeForm)
        } else {
          response = await reqAddInstanceSonNode(pNode.id, nodeForm)
        }
        if (response.data.code === 0) {
          let newNode = response.data.data        //  给新添加的节点增加按钮属性
          newNode.showAdd = true
          newNode.showRemove = true
          newNode.showEdit = true
          newNode.showCopy = true
          newNode.showMove = true
          newNode.showHelp = true
          newNode.showView = true
          newNode.children = []          //  注意：新添加的节点都是叶子节点
          let res
          if (this.updateFlag === 'template') {
            res = await reqSonNodesByScene(this.formdata.projectId, pNode.nodeCode)
          } else {
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
              item.showView = true
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
      if (this.updateFlag === 'template') {
        reqObjById(this.formdata.projectId, data.id).then(response => {
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

    //  节点编辑
    async nodeEdit(nodeForm, done) {
      let response
      if (this.updateFlag === 'template') {
        response = await reqPutObj(this.formdata.projectId, nodeForm)
      } else {
        response = await reqPutInstanceObj(nodeForm)
      }
      if (response.data.code === 0) {
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
        if (this.updateFlag === 'template') {
          response = await reqDeleteNodes({proId: this.formdata.projectId, nodeCode: data.nodeCode})
        } else {
          response = await reqDeleteInstanceNodes(this.formdata.projectId, data.turbineCode, data.nodeCode)
        }
        if (response.data.code !== 0) {
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

    async dlgCopyNode(data, flag) {
      this.updateFlag = flag
      try {
        await this.$confirm('此操作将复制当前节点及其所有子节点到同一个父节点下, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response
        if (this.updateFlag === 'template') {
          response = await reqCopyNode(this.formdata.projectId, data.nodeCode)
        } else {
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
          newNode.showView = true
          newNode.children = newNode.nodeType === 'Leaf' ? [] : [{id: 'loading', name: '节点加载中...'}]          //  注意：这里新复制的节点可能没有完成子节点的加载
          if (this.updateFlag === 'template') {
            let pNode = __tree.value.getParentNode(data.id)         //  获取父节点
            __tree.value.addSonNode(pNode.data, newNode)
          } else {
            let pNode = __inTree.value.getParentNode(data.id)         //  获取父节点
            __inTree.value.addSonNode(pNode.data, newNode)
          }
          this.$message.success('节点复制成功')
        }
      } catch (error) {
        console.log(error)
      }
    },

    dlgMoveNode(data, flag) {
      this.sourceNode = data      //  保存源节点
      this.dlgNodeMove = true
      this.updateFlag = flag
      this.turbineCode = data.turbineCode
    },

    async doNodeMove() {
      let targetNode
      if (this.updateFlag === 'template') {
        targetNode = this.$refs.refNodeTemplate.getCurrentNode()        //  获取目标节点
      } else {
        targetNode = this.$refs.refNodeInstance.getCurrentNode()        //  获取目标节点
      }
      if (targetNode) {
        this.dlgNodeMove = false
        await this.$confirm('确定执行节点移动操作吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response
        if (this.updateFlag === 'template') {
          response = await reqMoveNode(this.formdata.projectId, this.sourceNode.nodeCode, targetNode.nodeCode)
        } else {
          response = await reqMoveInstanceNode(this.sourceNode.turbineCode, this.sourceNode.nodeCode, targetNode.nodeCode)
        }
        if (response.data.code === 200) {
          if (this.updateFlag === 'template') {
            await this.getTreeNodes(response.data.data.nodeLevel)                    //  更新结构树，注意：这里需要根据被移动节点的最新层级决定加载的节点级数，确保被移动节点被加载进结构树
            __tree.value.setNodeSelected(response.data.data.id)                      //  将被移动的节点设置为选中状态
          } else {
            await this.getInstanceTreeNodes(response.data.data.nodeLevel)
            __inTree.value.setNodeSelected(response.data.data.id)
          }
          this.$message.success('节点移动成功')
        }
      }
    },

    // 删除场景
    async handleDeleteScene() {
      if (!this.formdata.sceneId || !this.formdata.projectName) {
        this.$message.warning('请先选择场景')
        return
      }
      const userId = this.$store.state.user.userInfo.userId
      const sceneName = this.formdata.projectName || '未命名场景'

      // 获取相同场景名称下的所有sceneId
      const sameNameScenes = this.allProjectList.filter(item => item.project === sceneName)
      const sceneIds = sameNameScenes.map(item => item.id)

      // 构建确认消息
      let sceneIdsHtml = ''
      // if (sceneIds.length > 1) {
      //   sceneIdsHtml = `<br><strong>场景ID列表：</strong>${sceneIds.join(', ')}<br><span style="color:orange;">（共 ${sceneIds.length} 个相同名称的场景）</span>`
      // } else {
      //   sceneIdsHtml = `<br><strong>场景ID：</strong>${sceneIds[0] || '未知'}`
      // }

      const confirmMessage = `是否删除该场景？<br><br><strong>场景名称：</strong>${sceneName}${sceneIdsHtml}<br><br><span style="color:red;">点击确认会删除该场景及该场景下的所有实例对象，请谨慎操作！</span>`

      try {
        await ElMessageBox.confirm(confirmMessage, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        })

        this.loading = true
        // 传递场景ID数组和元模型ID给后端接口
        const response = await deleteSceneNode(sceneIds,sceneName,userId)

        if (response.data.code === 0 || response.data.code === '0') {
          this.$message.success('删除成功')
          // 清空相关数据
          this.formdata.projectId = ''
          this.formdata.projectName = ''
          this.formdata.productModel = ''
          this.formdata.sceneId = ''
          this.productModelList = []
          this.templateTree = []
          this.instanceTree = []
          this.templateTreeShow = false
          this.instanceTreeShow = false
          // 刷新项目列表
          this.projectInit()
        } else {
          this.$message.error(response.data.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败：' + (error.message || '未知错误'))
        }
      } finally {
        this.loading = false
      }
    },
  }
}
</script>

<style scoped lang="scss">

.import-data {
  text-align: right;
  margin-bottom: 5px;
  margin-top: 5px;
  margin-right: 5px;
}

.update-zone {
  text-align: left;
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
  align-items: flex-start;
}

.addBtn {
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

.importDlg {
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  margin-left: 20px;

  .el-button {
    height: 30px;
  }
}

.treeDialog {
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;

  .el-button {
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

.dlgPreview {
  height: 460px;
  overflow: auto;
  margin-left: 20px;
}

.footerPreview {
  text-align: center;
  margin-top: 20px;

  .el-button {
    height: 30px;
  }
}

.custom-desc {
  :deep(.el-descriptions__table) {
    table-layout: fixed;
    width: 100%;
  }

  :deep(.el-descriptions__cell) {
    width: 50%;
  }

  :deep(.el-descriptions__label) {
    width: 120px; /* 表头固定宽度 */
    font-size: 16px;
    font-weight: bold;
    background-color: #f5f7fa;
  }

  :deep(.el-descriptions__content) {
    width: calc(100% - 120px); /* 内容区域宽度为剩余空间 */
    font-size: 16px;
    color: #333;
  }
}
</style>
