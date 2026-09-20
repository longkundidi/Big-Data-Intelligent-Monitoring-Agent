<template>
  <div class="subTree">
    <sw-tree class="tyTree" ref="lazyTree" :data="modelTreeData" :accordion="true"
             :defaultProps="defaultProps" :expandNode="true"
             :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
             :showCheckBox="false" :checkStrictly="true" :isLazy="false"
             @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand" @eventNodeAdd="dlgUploadSonNode">
    </sw-tree>
  </div>
  <upload-dataset v-model="dlgNodeUploadDataset" v-show="dlgNodeUploadDataset" ref="uploadDataset"
                  :scene-id="sceneId"
                  :model-id="nodeId"
                  :currentUploadNodeData="currentUploadNodeData"
                  @refreshData="refreshTree"></upload-dataset>
  <!--  <el-dialog v-model="dlgNodeUploadDataset" :title="titleName"
               :close-on-click-modal="false" width="50%" draggable>
      <div style="margin-top: 20px;display: flex;flex-direction: column;justify-content: center;align-items: center">
        <el-form :model="form" label-width="auto" style="max-width: 600px">
          <el-form-item label="提供者">
            <el-input v-model="form.provider" placeholder="请输入提供者姓名"/>
          </el-form-item>
        </el-form>
        <div style="display: flex; align-items: center;justify-content: center">
          &lt;!&ndash; 上传组件（移除 display:contents，改用常规布局） &ndash;&gt;
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
          &lt;!&ndash; 新增：恢复上一版本按钮 &ndash;&gt;
  &lt;!&ndash;        <el-button
              class="delBtn"
              @click="showRevertDialog = true"
              style="margin-left: 30px;"
          >
            恢复到版本
          </el-button>&ndash;&gt;
          <el-button
              class="auditBtn"
              @click="upload"
              style="margin-left: 30px;"
          >
            确定上传
          </el-button>
        </div>
        &lt;!&ndash; 文件名显示区域（固定在按钮正下方） &ndash;&gt;
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
          &lt;!&ndash; 取消按钮 &ndash;&gt;
          <el-button size="mini" type="text" icon="el-icon-close" @click="cancelFile" style="margin-left: 10px; padding: 0; font-size: 12px; color: #409EFF;"></el-button>
        </div>
      </div>
      &lt;!&ndash; 恢复版本选择对话框 &ndash;&gt;
  &lt;!&ndash;    <el-dialog
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
      &lt;!&ndash; 恢复确认对话框 &ndash;&gt;
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
      </el-dialog>&ndash;&gt;
    </el-dialog>-->

</template>
<script>
import swTree from "components/myComponent/swTree.vue";
import {nodeAndValOption} from "@/const/crud/sw/model3d/configGbomTree";
import {ElNotification} from "element-plus";
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {reqSonNodesBySceneId} from "@/api/sw/model3d/configGbomTree";
import uploadDataset from "@/views/scene/configSceneModel/uploadDataset.vue";

let globeParams = {}     //  声明一个全局参数对象
let _tree

export default {
  data() {
    return {
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },

      dlgNodeUploadDataset: false, //控制上传数据集弹窗

      titleName: '',
      form: {name: ''},
      mode: {},
      modeList: [],
      selectedFileName: '',
      datasetUrl: '',
      datasetType: '',
      // 原有数据...
      showRevertDialog: false,
      showConfirmRevert: false,
      selectedRevertType: '',
      uploadDataSets: []
    }
  },
  components: {
    uploadDataset,
    swTree
  },
  props: {
    modelTreeData: {
      type: Array,
      required: true
    },
    defaultExpandKeys: {
      type: Array,
    },
    sceneId: {
      type: [Number, String],
      required: true
    },
  },
  async mounted() {
    globeParams.nodeLevel = 3       //    预先展开3层节点
  },
  created() {
    this.setShowAddTrue(this.modelTreeData);
  },
  computed: {
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
  methods: {
    setShowAddTrue(data) {
      data.forEach(item => {
        item.showAdd = true;
        if (item.children && item.children.length > 0) {
          this.setShowAddTrue(item.children); // 递归调用
        }
      });
    },
    handleNodeClick(data) {
    },
    //  节点扩展消息响应
    async handleNodeExpand(data, node, treeNode) {
      if (node.level >= globeParams.nodeLevel) {
        let response = await reqSonNodesBySceneId(this.sceneId, data.nodeCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    //  弹窗上传数据集
    dlgUploadSonNode(pNode, pData, flag) {
      this.currentUploadNodeData = pData
      this.titleName = "上传数据集"
      this.nodeOption = nodeAndValOption
      this.updateFlag = flag
      this.dlgNodeUploadDataset = true
      this.selectedFileName = ''; // 清除文件名
      this.modeList = []; // 清空文件列表
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
              // const res = await uploadSonNode(this.currentUploadNodeData.id, this.datasetType, this.datasetUrl, this.form.provider)
              this.uploadDataSets.push({
                node: this.currentUploadNodeData,
                datasetType: this.datasetType,
                datasetUrl: this.datasetUrl,
                provider: this.form.provider
              });
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
    getUploadDataSets() {
      return this.uploadDataSets
    }
    /*showConfirmRevertDialog() {
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
    },*/
  }
}
</script>

<style scoped lang="scss">
.subTree {
  width: 100%;
  height: calc(100% - 15px);
  padding-top: 15px;
}
</style>