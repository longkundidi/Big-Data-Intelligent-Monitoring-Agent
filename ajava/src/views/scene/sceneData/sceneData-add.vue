<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="50%"
      :show-close="true"
      title="新增场景"
      @close="closeDialog"
  >

    <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
      <div class="form-wrapper">
        <!-- 左侧表单 -->
        <div class="form-left">
          <el-form-item label="场景名称：" prop="project">
            <el-input v-model="formData.project" placeholder="请输入场景名称"/>
          </el-form-item>
          <el-form-item label="场景简介：" prop="sceneDesc">
            <el-input
                v-model="formData.description"
                type="textarea"
                :rows="4"
                placeholder="请输入场景简介"
            />
          </el-form-item>
        </div>

        <!-- 右侧上传 -->
        <div class="form-right">
          <el-upload
              :action="action"
              accept=".jpg,.png"
              :file-list="modeList"
              :http-request="() => {}"
              :before-upload="confirmUpload"
              :limit="1"
          >
            <div class="upload-box">
              <div class="upload-label">图片</div>
              <div class="upload-placeholder">+</div>
            </div>

            <el-tag type="success" v-if="formData.iconUrl" style="margin-left: 10px;">
              上传成功
            </el-tag>
          </el-upload>
        </div>
      </div>
    </el-form>

    <div class="selectHeader">
      <div class="update-zone">
        <el-button class="addBtn" style="margin-bottom:15px;margin-right: 15px"
                   @click="gBomVisible = true; updateType = 'Bom'">
          导入元结构树
        </el-button>
        <el-button class="addBtn" style="margin-bottom:15px;"
                   @click="gValVisible = true; updateType = 'Val'">
          导入感知变量
        </el-button>
      </div>
    </div>

    <upload-g-bom v-model="gBomVisible" v-if="gBomVisible" ref="uploadGBom" @outBomData="receiveBomData"></upload-g-bom>
    <upload-g-val v-model="gValVisible" v-if="gValVisible" ref="uploadGVal" @outValData="receiveValData"></upload-g-val>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {uploadIcon} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import uploadGBom from "./sceneData-add-gbom.vue"
import uploadGVal from "./sceneData-add-gval.vue"
import {createProGbomTree} from "@/api/sw/model3d/configGbomTree";
import {addScene} from "@/api/diagnosis/scene/scene";
import {importSceneVal} from "@/api/diagnosis/graphVis/dataIngestion";

export default {
  components: {
    uploadGBom,
    uploadGVal
  },
  data() {
    return {
      visible: false,
      gBomVisible: false,
      gValVisible: false,
      updateType: '',
      action: '',
      modeList: [], // 用于存储选中的图片文件
      mode: {},
      formData: {
        project: '',
        description: '',
        iconUrl: '',
        productModel: '',
        bomModel: '',
      },
      outBomData: [],
      outValData: [],
      sceneId: '',
      rules: {
        project: [
          {required: true, message: '请输入场景名称', trigger: 'blur'},
          {min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    confirmUpload(file) {
      this.$confirm('确定要上传这张图片吗？', '确认上传', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 用户点击了"确认"
        this.mode = file;
        this.modeList = [{
          name: file.name,
          url: URL.createObjectURL(file)
        }];
        this.iconUpload();  // 调用上传接口
      }).catch(() => {
        // 用户点击了"取消"
        this.mode = {};
        this.modeList = [];
      });

      return false; // 阻止 el-upload 自动上传
    },
    closeDialog() {
      this.visible = false
      this.$emit('update:modelValue', false)
    },
    init() {
      this.visible = true
      this.formData.project = ''
      this.formData.description = ''
      this.formData.productModel = ''
      this.formData.bomModel = ''
    },

    modeUpload(item) {
      this.mode = item.file;
      // 只保留当前上传的图片
      this.modeList = [{
        name: item.file.name,
        url: URL.createObjectURL(item.file)
      }];
    },
    async iconUpload() {
      if (!this.mode.uid) {
        this.$notify.warning('没有选择图标')
        return
      }

      let file = new FormData()
      file.append('file', this.mode)

      try {
        const res = await uploadIcon(file)
        this.$notify.success('图标上传成功')
        this.formData.iconUrl = res.data.data
        this.modeList = []
        this.mode = {}
      } catch (e) {
        this.$notify.warning('图标上传失败')
      }
    },

/*    handleSubmit() {
      this.handleValData(this.outValData)
    },*/

    handleSubmit() {
      //必填校验
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          this.$notify.warning('必要内容未填写')
          return
        }
        // 检查是否已上传图标
        if (!this.formData.iconUrl) {
          this.$notify.warning('请上传场景图标')
          return
        }

        // 检查是否已导入产品结构树
        if (!this.outBomData || this.outBomData.length === 0) {
          this.$notify.warning('请导入产品结构树')
          return
        }

        // 检查是否导入感知变量
        if (!this.outValData || this.outValData.length === 0) {
          this.$notify.warning('请导入感知变量')
          return
        }

        const rootNode = this.outBomData.find(item => item.node_type === 'Root');
        if (!rootNode || rootNode.node_name !== this.formData.project) {
          this.$notify.warning("结构树根节点名称应与场景名称一致")
          return
        }
        addScene(this.formData).then(res => {
          this.sceneId = res.data.data
          if (this.sceneId === null) {
            this.$notify.warning(res.data.message || '创建场景失败')
            return
          }
          this.handleBomData(this.outBomData)
          this.handleValData(this.outValData)
          this.visible = false  // 确保关闭新增场景弹窗
          this.$emit('update:modelValue', false)  // 确保更新v-model
          this.$emit('refreshDataList')
        }).catch(error => {
          console.error('请求失败', error)
          this.$notify.error('请求异常')
        })
      });
    },
    receiveBomData(data) {
      this.outBomData = data
      if (this.outBomData) {
        this.$notify.success('结构树已加载')
      }

    },
    receiveValData(data) {
      this.outValData = data
      if (this.outValData) {
        this.$notify.success('感知变量已加载')
      }
    },
    handleBomData(outBomData) {
      const turbines = [];
      turbines.push(outBomData)
      turbines.forEach(subArray => {
        if (subArray.length > 0 && subArray[0].node_name) {
          subArray.forEach(item => {
            if (item.node_name) { // 确保只有具有node_name属性的对象才被赋予pro_id
              item.scene_id = this.sceneId
            }
          })
        }
      })

      try {
        createProGbomTree(turbines).then(res => {
          if (res.data.code === 0) {
            this.loadingInstance = false
            this.$notify.success('结构树导入成功')
            this.projectInit();
          }
        })
      } catch (error) {
        console.error('结构树导入失败:', error)
      }
    },
    handleValData(outValData) {
      outValData.forEach(item => {
        // 检查对象是否有usable字段
        if (!item.hasOwnProperty('usable')) {
          // 如果没有，则添加usable字段并设置其值为1
          item.usable = 1;
        }
      })

      this.loadingInstance = true
      try {
        importSceneVal(this.sceneId, outValData).then(res => {
          if (res.data.code === 0) {
            this.loadingInstance = false
            this.showPreviewDialog = false
            this.$notify.success('感知变量导入成功')
          }
        })
      } catch (error) {
        // 错误处理
        console.error('感知变量导入错误:', error)
      }
    },
  }
}
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  white-space: nowrap;
  justify-content: flex-start; /* 如果是flex布局，也让内容靠左 */
}

:deep(.el-form-item__content) {
  flex: 1;

  .el-input {
    width: 100%;
  }
}

:deep(.el-upload-list) {
  max-width: 100px;
  word-break: break-all;
}

:deep(.el-upload-list__item-name) {
  white-space: normal !important;
  word-break: break-word;
  max-width: 100px;
  line-height: 1.2;
  font-size: 12px;
}

.form-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.form-left {
  flex: 1;
  width: 80%;
}

.form-right {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-left: 20px;
  justify-content: flex-start;
}

.upload-box {
  width: 100px;
  height: 120px;
  border: 1px solid #999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.upload-label {
  margin-bottom: 4px;
  font-size: 14px;
}

.upload-placeholder {
  font-size: 28px;
  line-height: 1;
}
</style>
