<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="45%"
      @close="closeDialog()"
      :show-close="true"
      class="el-dialog__header"
  >
    <div slot="title" style="font-size: 30px;text-align:center;padding-bottom: 2%">
      <span>模型修改</span>
    </div>


    <el-form ref="dataForm" label-width="110px" @keyup.enter.native="dataFormSubmit()" style="margin-right: 20px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="dataForm.modelName" :disabled="true"/>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="适用场景" prop="modelUsecase">
            <el-input v-model="dataForm.modelUsecase" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="数据集说明" prop="datasetMemo">
            <el-input v-model="dataForm.datasetMemo"/>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="运行所需资源" prop="runSize">
            <el-input v-model="dataForm.runSize"/>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="针对对象">
            <el-button class="normalBtn"
                       @click="selectGBomNode()">选择
            </el-button>
            <el-tag type="success" style="margin-left: 5px;">{{ dataForm.modelObject }}</el-tag>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="模型图标">
            <el-button class="normalBtn" @click="upload">上传</el-button>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型输入样例" prop="input">
            <el-input v-model="dataForm.input">
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型输出样例" prop="output">
            <el-input v-model="dataForm.output">
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="访问路径" prop="output">
            <el-input v-model="dataForm.modelUrl">
            </el-input>
          </el-form-item>
        </el-col>

      </el-row>

    </el-form>

    <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
      <el-button v-if="canSubmit" class="editBtn" @click="dataFormSubmit()">完成修改</el-button>
    </span>

    <upload-icon v-if="dialogVisible" ref="upload" @getIconUrl="getIconUrl"></upload-icon>

  </el-dialog>

  <el-dialog v-model="selectVisible" title="选择算法针对对象" width="50%" draggable>
    <div class="treeDialog" >
      <node-move-tree ref="refNodeTree" style="width: 100% ;height: 90%"></node-move-tree>
      <el-button class="normalBtn" @click.stop="doNodeSelect()">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import uploadIcon from '@/views/al/stateEvaluation/stateEvaluation-add-uploadicon.vue'
import nodeMoveTree from '@/views/sw/model3d/configGbomTree/treeForNodeMove'
import {ElMessage, ElMessageBox} from "element-plus";
import {getModelObj, putModelObj} from "@/api/al/domainModel";


export default {
  data() {
    return {
      visible: false,
      canSubmit: false,
      dataForm: {},
      dialogVisible: false,
      selectVisible: false,
      isFirstSelectTree: false,
    }
  },
  components: {
    uploadIcon,
    nodeMoveTree
  },
  methods: {
    init(id) {
      this.visible = true
      this.canSubmit = true
      this.isFirstSelectTree = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
      getModelObj(id).then(response => {
        this.dataForm = response.data.data
      })
    },

    upload() {
      let modelName = this.dataForm.modelName
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.upload.init(modelName)
      })
    },
    getIconUrl(iconUrl) {
      this.dataForm.modelIcon = iconUrl
    },
    // 表单提交
    dataFormSubmit() {
      this.canSubmit = false
      putModelObj(this.dataForm).then(response => {
        this.$notify.success('修改成功')
        this.visible = false
        this.$emit('success')
      }).catch(() => {
        this.canSubmit = true
      })

    },

    selectGBomNode(){
      this.selectVisible = true
      if(this.isFirstSelectTree){
        this.$nextTick(() => {
          this.$refs.refNodeTree.clearSelected()
        })
        this.isFirstSelectTree = false
      }
    },

    async doNodeSelect(){

      let targetNode = this.$refs.refNodeTree.getCurrentNode()        //  获取目标节点
      if (targetNode){
        this.dlgNodeMove = false
        ElMessageBox.confirm(
            '确定选择该部套件吗？',
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
        ).then(() => {
          this.dataForm.modelObject = targetNode.name
          this.dataForm.objectId = targetNode.id
          this.selectVisible = false
        }).catch(() => {
          ElMessage({
            type: 'info',
            message: '取消',
          })
        })
      }
    },

    // 重置表单
    closeDialog() {
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
    },

  }
}
</script>

<style lang="scss" scoped>
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
</style>