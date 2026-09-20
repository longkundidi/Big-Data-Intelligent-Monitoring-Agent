<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="70%"
      @close="closeDialog()"
      :show-close="true"
      class="el-dialog__header"
  >
    <div slot="title" style="font-size: 30px;text-align:center;padding-bottom: 2%">
      <span>模型修改</span>
    </div>


    <el-form ref="dataForm" :rules="dataRule" label-width="125px" @keyup.enter.native="dataFormSubmit()" >
      <el-row>
        <el-col :span="8">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="dataForm.modelName" :disabled="true"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型类型" prop="modelType">
            <el-select v-model="dataForm.modelType" style="width: 100%">
              <el-option
                  v-for="item in options"
                  :key="item.modelType"
                  :value="item.modelType"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="提供者" prop="modelProvider">
            <el-input v-model="dataForm.modelProvider"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="针对对象">
            <el-button class="normalBtn"
                       @click="selectGBomNode()">选择
            </el-button>
            <el-tag type="success" style="margin-left: 5px;">{{ dataForm.modelObject }}</el-tag>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型支持库" prop="modelLibrary">
            <el-input v-model="dataForm.modelLibrary"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="上传图标">
            <el-button class="normalBtn" @click="upload">上传</el-button>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="适用场景" prop="modelCondition">
            <el-input v-model="dataForm.modelCondition"
                      maxlength="50"
                      show-word-limit/>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型优点" prop="modelAdvantage">
            <el-input v-model="dataForm.modelAdvantage"
                      maxlength="50"
                      show-word-limit/>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型缺点" prop="modelDisadvantage">
            <el-input v-model="dataForm.modelDisadvantage"
                      maxlength="50"
                      show-word-limit/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型框架">
            <el-select v-model="dataFormone.modelFramework" style="width: 100%">
              <el-option
                  v-for="item in options5"
                  :key="item.modelFramework"
                  :value="item.modelFramework"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="设备">
            <el-select v-model="dataFormone.device" style="width: 100%">
              <el-option
                  v-for="item in options10"
                  :key="item.device"
                  :value="item.device"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练数据集">
            <el-input v-model="dataFormone.trainDataset"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="测试数据集">
            <el-input v-model="dataFormone.testDataset"/>
          </el-form-item>
        </el-col>

        <el-col :span="3" style="padding-left: 2%">
          <div><span>预处理：</span></div>
          <div>
            <el-radio-group v-model="dataFormone.ifPretreatment" @change="Changeyuchuli">
              <el-radio label="0">是</el-radio>
              <el-radio label="1">否</el-radio>
            </el-radio-group>
          </div>
        </el-col>

        <el-col :span="5">
          <el-form-item label="预处理方法">
            <el-input v-model="dataFormone.pretreatment" :disabled="btnstatus"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="样本类型">
            <el-input v-model="dataFormone.sampleType"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="样本尺寸">
            <el-input v-model="dataFormone.sampleSize"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练次数">
            <el-input v-model="dataFormone.trainTimes"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练批次">
            <el-input v-model="dataFormone.trainBatch"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="损失函数">
            <el-input v-model="dataFormone.lossFunction"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="正则化">
            <el-input v-model="dataFormone.regularization"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="学习率">
            <el-input v-model="dataFormone.learningRate"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="优化器">
            <el-input v-model="dataFormone.optimizer"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="训练结果">
            <el-input v-model="dataFormone.trainResults"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="测试结果">
            <el-input v-model="dataFormone.testResults"/>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="评价指标">
            <el-input v-model="dataFormone.metrics" />
          </el-form-item>
        </el-col>

      </el-row>
    </el-form>

    <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
      <el-button v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()" style="width: 8%">完成修改</el-button>
    </span>

    <uploadurl v-if="dialogVisible" ref="upload" @getIconUrl="getIconUrl"></uploadurl>

  </el-dialog>

  <el-dialog v-model="selectVisible" title="选择模型针对对象" width="50%" draggable>
    <div class="treeDialog">
      <!-- 单选框选择 gbom 树 -->
      <el-radio-group v-model="selectedSceneId">
        <el-radio
            v-for="item in gbomList"
            :key="item.id"
            :label="item.id"
        >
          {{ item.project }}
        </el-radio>
      </el-radio-group>
      <node-move-tree
          v-if="selectedSceneId"
          ref="refNodeTree"
          :scene-id="selectedSceneId"
          style="width: 100%; height: 90%"
      ></node-move-tree>
      <el-button class="normalBtn" @click.stop="doNodeSelect()">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {getDecisionsObj, putObj} from '/src/api/al/maintenanceDecisions/maintenanceDecisionsBase'
import {fetchList1, putObj1} from '/src/api/al/maintenanceDecisions/maintenanceDecisionsOne'
import uploadurl from './maintenanceDecisions-add-uploadicon.vue'
import nodeMoveTree from '@/views/sw/model3d/configGbomTree/treeForNodeMove'
import {ElMessage, ElMessageBox} from "element-plus";
import {getAllSceneMetaModels} from "@/api/diagnosis/scene/scene";



export default {
  data() {
    return {
      options: [
        {modelType: '深度学习模型'},
        {modelType: '传统机器学习模型'},
      ],

      options5: [
        {modelFramework: 'TensorFlow'},
        {modelFramework: 'Pytorch'},
        {modelFramework: 'keras'},
        {modelFramework: '无'},
      ],

      options10: [
        {device: 'CPU'},
        {device: 'GPU'},
      ],

      searchForm1: {
        modelName: ''
      },
      dataFormone: {},
      visible: false,
      canSubmit: false,
      dataForm: {},
      btnstatus: false,
      dialogVisible: false,
      dataRule: {
        modelTypeFirst:
            [
              {required: true, message: '模型类型不能为空', trigger: 'blur'}
            ],
      },
      selectVisible: false,
      isFirstSelectTree: false,
      selectedSceneId: null, // 选择的sceneId
      gbomList: [],
    }
  },
  components: {
    uploadurl,
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
      getDecisionsObj(id).then(response => {
        this.dataForm = response.data.data
        this.searchForm1.modelName = this.dataForm.modelName
        this.getDomainone()
      })
    },

    checkValues() {
      const hasOtherValues = Object.keys(this.dataFormone).some(key => {
        if (key === 'id' ||key === 'modelName' || key === 'ifTrain' || key === 'ifTest' || key === 'ifPretreatment' || key === 'modelFramework'|| key === 'device') {
          return false;
        }
        const value = this.dataFormone[key];
        return value !== undefined && value !== null && value !== '';
      });
      if (hasOtherValues) {
        this.dataFormone.ifTrain = 1
        this.dataFormone.ifTest = 1
      } else {
      }
    },

    getDomainone(page, params) {
      fetchList1(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm1)).then(response => {
        this.dataFormone = response.data.data.records[0]
        this.dataFormone.ifPretreatment = String(this.dataFormone.ifPretreatment);
        if (this.dataFormone.ifPretreatment === '1'){
          this.btnstatus = true
        }
        this.count = response.data.data.total
      })
    },

    Changeyuchuli(val) {
      this.btnstatus = (val !== '0');
      if (this.btnstatus === true) {
        this.dataFormone.pretreatment = ''
      }
    },

    upload() {
      if (this.dataForm.modelName === '' || this.dataForm.modelTypeFirst === '' || this.dataForm.modelType === '' || this.dataForm.modelFunction === '') {
        this.$notify.warning("必要参数未配置")
      } else {
        let modelName = this.dataForm.modelName
        this.dialogVisible = true
        this.$nextTick(() => {
          this.$refs.upload.init(modelName)
        })
      }
    },
    getIconUrl(iconUrl) {
      this.dataForm.modelIcon = iconUrl
    },
    // 表单提交
    dataFormSubmit() {
      this.checkValues()
      this.dataFormone.ifPretreatment = +this.dataFormone.ifPretreatment;
      this.dataFormone.modelName = this.dataForm.modelName
      this.canSubmit = false
      putObj(this.dataForm).then(response => {
        this.visible = false
        this.$emit('success')
      }).catch(() => {
        this.canSubmit = true;
      });

      putObj1(this.dataFormone).then(response => {
        this.$notify.success('修改成功')
        this.visible = false
        this.$emit('success')
      }).catch(() => {
        this.canSubmit = true;
      });

    },

    selectGBomNode(){
      this.selectVisible = true
      getAllSceneMetaModels().then(res => {
        this.gbomList = res.data.data
      })
      if(this.isFirstSelectTree){
        this.$nextTick(() => {
          this.$refs.refNodeTree.clearSelected()
        })
        this.isFirstSelectTree = false
      }
    },

    async doNodeSelect(){
      if (!this.selectedSceneId) {
        this.$message.warning("请先选择一个gbom树！");
        return;
      }
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
      this.searchForm1.modelName = ''
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
