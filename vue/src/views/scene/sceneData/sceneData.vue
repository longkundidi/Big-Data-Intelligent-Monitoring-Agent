<template>
  <div class="scene-list">
    <el-button type="primary" @click="handleAdd" class="add-scene-btn">新增场景</el-button>
    <el-card
        v-for="(item, index) in pageSceneList"
        :key="index"
        class="scene-card"
        shadow="hover"
    >
      <div class="scene-content">
        <!-- 左侧图片 -->
        <el-image
            style="width: 35%; height: 200px; object-fit: contain;"
            :src="getImageSrc(item.iconUrl)"
            fit="contain"
        >
          <template #error>
            <div class="image-slot" style="width: 100%; height: 100%;">
              <img src="@/assets/upload.png" style="width: 100%; height: 100%;"/>
            </div>
          </template>
        </el-image>

        <!-- 中间信息 -->
        <div class="scene-info">
          <div class="title-model-row">
            <div class="scene-title">{{ item.project }}</div>
            <div class="scene-model">
              选择型号：
              <el-select v-model="item.productModel" clearable placeholder="请选择机型" style="width: 150px;">
                <el-option v-for="model in productModelMap[item.project]" :key="model.id" :label="model.productModel"
                           :value="model.id"
                ></el-option>
              </el-select>
            </div>
          </div>
          <div class="scene-desc">简介：{{ item.description }}</div>
        </div>

        <!-- 右侧操作按钮 -->
        <div class="scene-actions">
          <el-button type="primary" @click="edit(item)">编辑</el-button>
          <el-button type="success" @click="view(item)">查看</el-button>
          <el-button type="danger" @click="remove(item)">删除</el-button>
          <el-button type="info" @click="create(item)">新建对象</el-button>
        </div>
      </div>
    </el-card>

    <add v-model="addVisible" v-if="addVisible" ref="add" @refreshDataList="refreshData"/>

    <!-- 添加结构树查看弹窗组件 -->
    <!-- 修改结构树弹窗组件的使用部分 -->
    <scene-tree-dialog
      :visible.sync="treeDialogVisible"
      :selected-model="selectedModelId"
      :scene-id="selectedSceneId"
      @update:visible="treeDialogVisible = $event"
    />

    <div class="pagination">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="sceneList.length"
                     :page-size="pageSize" :current-page="currentPage" :page-sizes="[3, 6, 12]"
                     @current-change="handlePageChange" @size-change="handleSizeChange"/>
    </div>

    <el-dialog v-model="modelBomUpload" title="新建机型结构树" width="15%" align-center draggable>
      <div class="importDlg">
        <div style="display: flex;justify-content: center;margin-top: 20px">
          <el-upload
              action=""
              :on-change="handleBomChange"
              :show-file-list="false"
              :auto-upload="false">
            <el-button class="addBtn" style="margin-right: 10px">上传</el-button>
          </el-upload>
          <el-button class="disMissBtn" @click="modelBomUpload = false; errorShow = false">取消</el-button>
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

    <el-dialog v-model="ObjectBomUpload" title="新建实例对象" width="20%" align-center draggable @close="objectName = ''">
      <div class="createDlg" v-loading="loadingInstance">
        <span style="font-size: 15px; font-weight: bold;">新建对象：</span>
        <el-input
            v-model="objectName"
            placeholder="请输入新建实例对象名"
            style="width: 50%"
        ></el-input>
      </div>
      <div class="footerCreate">
        <el-button class="normalBtn" @click="ObjectBomUpload = false; objectName = ''">取 消</el-button>
        <el-button class="normalBtn" @click="createObject();">确 认</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="showPreviewDialog" title="数据预览" style="width: 35%;height: 60%" draggable>
      <div v-loading="loadingInstance" element-loading-text="正在导入...">
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

  </div>
</template>

<script>
import add from "@/views/scene/sceneData/sceneData-add.vue";
import {fetchList, fetchProModelMap} from "@/api/diagnosis/scene/scene";
import sceneTreeDialog from "@/views/scene/sceneData/sceneTreeDialog.vue";
import {ElMessage} from "element-plus";
import {hasDuplicateFieldValues} from "@/api/diagnosis/scene/utils/sceneUtils";
import {
  addProject,
  createObjInstance,
  createProInstance,
  createProTemplate
} from "@/api/diagnosis/graphVis/dataIngestion";
import * as XLSX from "xlsx";

export default {
  components: {
    add,
    sceneTreeDialog
  },
  data() {
    return {
      sceneList: [],
      pageSceneList: [],
      addVisible: false,
      count: 6,
      pageSize: 3,
      currentPage: 1,
      productModelMap: {},
      bomSelectedModel: '',
      // 添加控制结构树弹窗的变量
      treeDialogVisible: false,
      selectedModelId: '',
      selectedSceneId: '', // 添加场景ID变量
      modelBomUpload: false,
      ObjectBomUpload: false,
      currentProName: '',
      currentSceId: null,
      currentModelId: null,
      objectName: '',
      errorShow: false,
      errorCodes: [],
      showPreviewDialog: false,
      loadingInstance: false,
      previewData: [], // 解析后的数据
      previewColumns: [], // 定义列信息
    };
  },
  computed: {
    pageSceneList() {
      const start = (this.currentPage - 1) * this.pageSize;
      return this.sceneList.slice(start, start + this.pageSize);
    },
    // 计算属性来格式化错误代码的描述信息
    alertDescription() {
      return this.errorCodes.join('、') + '机型中存在重复的节点编码，请重新上传！';
    },
  },
  mounted() {
    this.getSceneDataList()
    this.getProductModelMap()
  },

  methods: {
    create(item) {
      this.currentSceId = item.id;
      this.currentProName = item.project
      //选中型号，创建该型号的实例对象
      if (item.productModel) {
        this.ObjectBomUpload = true;
        this.currentModelId = item.productModel
      } else {
        //未选中型号，创建该场景的型号结构树
        this.modelBomUpload = true;
      }
    },
    handleBomChange(file, fileList) {
      this.errorShow = false
      const fileContent = file.raw
      const fileName = file.name
      const fileType = fileName.substring(fileName.lastIndexOf('.') + 1)
      if (this.currentProName === '') {
        ElMessage({
          message: "请选择或新建场景！",
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

    handleConfirmedData(outData) {
      if (this.modelBomUpload) {
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
    handleBomData(outData) {

      const turbines = []; // 用于存放分割后的数组段
      let currentSegment = []; // 临时数组，用于构建当前段落
      let projects = []

      // 遍历outData
      for (const item of outData) {
        if (item.node_type === 'Root') {
          let projectItem = {}
          projectItem.project = this.currentProName
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
            }
          })
        } catch (error) {
          // 错误处理
          console.error('导入时发生错误:', error)
        }
      })
      this.modelBomUpload = false
    },

    getImageSrc(iconUrl) {
      if (iconUrl) {
        return `/api/al/file/${iconUrl}`
      }
      return ''
    },
    handlePageChange(page) {
      this.currentPage = page;
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },
    handleAdd() {
      this.addVisible = true
      /*      this.nextTick(() => {
              this.$refs.add.init()
            })*/
    },
    refreshData() {
      this.getSceneDataList()
    },
    getSceneDataList() {
      fetchList().then(res => {
        this.sceneList = res.data.data
      })
    },
    getProductModelMap() {
      fetchProModelMap().then(res => {
        this.productModelMap = res.data.data
      })
    },
    // 添加查看结构树的方法
    view(item) {
      this.selectedModelId = item.productModel || '';
      this.selectedSceneId = item.id;
      this.treeDialogVisible = true;
    },
    async createObject() {
      this.loadingInstance = true
      try {
        await createObjInstance({
          sceId: this.currentSceId,
          modelId: this.currentModelId,
          nodeName: this.objectName
        }).then(res => {
          if (res.data.code === 0) {
            ElMessage({
              message: "创建成功",
              type: 'success'
            })
          }
        })
      } catch (error) {
        // 处理错误情况
        console.error('创建实例时发生错误:', error)
      } finally {
        this.loadingInstance = false
        this.ObjectBomUpload = false
        this.tuirbineName = ''
      }
    }
  }
};

</script>


<style lang="scss" scoped>
.scene-list {
  padding: 20px;
}

.add-scene-btn {
  margin-bottom: 16px;
  margin-left: 10%;
}

.scene-card {
  margin-bottom: 10px;
  width: 80%;
  margin-left: 10%;
  margin-right: 10%;

  :deep(.el-card__body) {
    padding: 8px;
  }
}

.scene-content {
  display: flex;
  align-items: stretch;
  min-height: 100px;

}

.scene-image {
  width: 150px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  margin-right: 20px;
}

.scene-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 0 15px;
  height: 100%;
  max-width: 50%;
  margin-top: 1%;
}

.title-model-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  margin-top: 0;
  width: 100%;
}

.scene-title {
  font-size: 18px;
  font-weight: bold;
}

.scene-model {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.model-input {
  width: 120px;
  margin-left: 8px;
}

.scene-desc {
  font-size: 13px;
  color: #666;
}

.scene-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: 1%;

  :deep(.el-button) {
    width: 100px;
    margin-left: 0;

    justify-content: center;
  }
}


.pagination {
  display: flex;
  justify-content: right;
  margin-top: 20px;
  margin-right: 10%;
}


.update-zone {
  text-align: right;
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
  height: 450px;
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

.createDlg {
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  margin-left: 20px;
}

.footerCreate {
  text-align: center;
  margin-top: 10px;

  .el-button {
    height: 30px;
  }
}


</style>