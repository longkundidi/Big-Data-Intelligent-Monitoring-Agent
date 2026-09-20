<template>
  <div class="tablecon">
    <div class="table">
      <div class="title">组态模板</div>
      <avue-crud ref="crudCompositionRef" class="h-avue-crud"
                 v-model="compositionForm"
                 :option="compositionOption"
                 @on-load="getCompositionList"
                 v-model:page="compositionPage"
                 :data="compositionData"
                 @row-click="compositionRowClick">
        <template #menu-left="{}">
          <el-button type="primary"
                     class="addBtn"
                     @click="addModel('composition')">新增
          </el-button>
        </template>
        <template #compositionSelected="scope">
          &nbsp;&nbsp;&nbsp;<el-radio v-model="compositionRowIndex" :label="scope.index"><span></span></el-radio>
        </template>
        <template #relatedCompositionVar="scope">
          <el-button type="text" class="viewBtn" @click="compositionVarsClick(scope.row)">详情</el-button>
        </template>
        <template #menu="{size,row,index}">
          <el-button type="text" class="editBtn" @click="editModel(row, 'composition')">编辑</el-button>
          <el-button type="text" class="delBtn" @click="deleteModel(row)">删除</el-button>
        </template>
      </avue-crud>
    </div>
    <el-dialog v-model="dlgModel" :title="titleName" width="70%" draggable>
      <div v-if="varShow"><H2 style="font-size: 18px;"><strong>选择感知变量</strong></H2>
        <div class="sensor-zone">
          <avue-crud ref="crudSensor" :data="sensorData" :option="sensorOption" @selection-change="selectionChange">
          </avue-crud>
        </div>
      </div>
      <div>
        <H2 style="font-size: 18px;"><strong>算法配置</strong></H2>
        <avue-form v-model:defaults="defaults" :option="algoOption" v-model="algoForm" ref="refAlgoConfig">
        </avue-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="normalBtn" @click="cancel()">取 消</el-button>
          <el-button class="normalBtn" type="primary" @click="submit()">保 存</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="dlgVars" title="感知变量" width="70%" draggable>
      <avue-crud ref="crudRefVars" class="h-avue-crud"
                 v-model="varForm"
                 :option="varOption"
                 @on-load="getList"
                 v-model:page="varPage"
                 :data="varData">
      </avue-crud>
    </el-dialog>


  </div>
</template>
<script>
import {ref} from "vue";
import {
  compositionTableOption,
  sensorTableOption, compositionFormOption, varTableOption
} from '@/const/crud/sw/model3d/configModel/table'
import {
  fetchList, getVarsByNodeId, reqAddModel, delObj, getObj,
  getVarsByModelId, reqEditModel, pageByModelId, reqDomainConfigList
} from '@/api/sw/model3d/configModel/table.js'
import {ElMessageBox, ElMessage} from 'element-plus'

let globeParams = {}     //  声明一个全局参数对象

export default {
  name: 'modelTable',

  setup() {
    let compositionOption = ref(compositionTableOption)
    let compositionData = ref([])
    let compositionForm = ref({})
    let compositionPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })

    let titleName = ref('')
    let dlgModel = ref(false)
    let varShow = ref(false)
    let sensorData = ref([])
    let sensorOption = ref(sensorTableOption)
    let algoForm = ref({})
    let algoOption = ref(compositionFormOption)
    let defaults = ref({})
    let compositionRowIndex = ref(0)
    let dlgVars = ref(false)
    let varForm = ref({})
    let varOption = ref(varTableOption)
    let varData = ref([])
    let varPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })


    return {
      compositionOption, compositionData, compositionForm, compositionPage,
      titleName, dlgModel, varShow, sensorData, sensorOption,
      algoForm, algoOption, defaults, compositionRowIndex,
      dlgVars, varForm, varOption, varData, varPage
    }
  },

  methods: {

    compositionRowClick(row) {
      this.compositionRowIndex = row.$index
    },

    //  显示表格时，自动调用
    async getList(page, params) {
      let res = await pageByModelId({
        modelId: this.curModelRow.modelId,
        pageSize: page.pageSize,
        current: page.currentPage
      })
      if (res.data.code === 0) {
        this.varData = res.data.data.records
        page.total = res.data.data.total
      }
    },

    //  查看感知变量详情（状态感知）
    compositionVarsClick(row) {
      this.curModelRow = row     //   缓存当前用户选中的行
      this.dlgVars = true        //  弹窗
      this.getList(this.varPage)
    },

    //  模型编辑按钮（状态感知、故障诊断）
    editModel(row, modelType) {
      let self = this
      this.titleName = '编辑组态模板'
      this.algoForm.modelType = modelType
      this.dlgModel = true        //  弹窗

      this.varShow = true
      //  加载感知变量列表，并勾选已经关联的变量
      getVarsByNodeId(row.nodeId).then(res => {          //  根据节点Id查询关联的感知变量，显示列表
        if (res.data.code === 0)
          self.sensorData = res.data.data             //  加载列表数据
        getVarsByModelId(row.modelId).then(resVars => { //  根据modelId查询感知变量
          if (resVars.data.code === 0) {
            self.varList = resVars.data.data
            // 根据数组varList的varId,在数组sensorData中找到相同的对象
            // 在下面的代码中，filter()方法遍历sensorData中的每个元素，并对每个元素应用一个条件。
            // 条件是使用some()方法在varList中查找是否存在与数组sensorData当前元素的varId相同的对象。如果存在相同的varId，则filter()方法将保留该元素，最终返回一个新的数组，其中包含与数组varList中的varId相匹配的对象。
            // some()方法是JavaScript数组的一个方法，它用于检测数组中是否至少有一个元素满足指定条件。
            // varList.some()可以理解为在数组varList中进行遍历。在数组遍历的过程中，some()方法会对数组中的每个元素都应用一次指定的测试函数，直到找到一个满足条件的元素，然后立即返回 true。如果没有找到满足条件的元素，则返回 false。
            let checkArray = self.sensorData.filter(itemA => self.varList.some(itemB => itemB.varId === itemA.varId))   //  生成勾选列表
            self.$nextTick(() => {      //  等待窗口弹出渲染成功后
              self.$refs.crudSensor.toggleSelection(checkArray)                         //  勾选一个数组
              //self.$refs.crudSensor.toggleRowSelection(this.sensorData[1],true)       //  勾选一个感知变量
            })
          }
        })
      })

      reqDomainConfigList().then(resAlgoList => {
        self.$nextTick(() => {      //  等待窗口弹出渲染成功后
          self.algoOption.column[1].dicData = resAlgoList.data.data       //  填写服务算法列表字典
          getObj(row.modelId).then(response => {                          //  根据modelId查询编辑行数据
            if (response.data.code === 0)
              self.algoForm = response.data.data
          })
        })
      })
    },


    //  删除按钮（状态感知模型）
    async deleteModel(row) {
      try {
        await this.$confirm('此操作将删除当前模型及其关联的组态模板, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response = await delObj({ modelId: row.modelId, modelType: 'composition'})
        if (response.data.code === 0) {
          ElMessage.success('删除成功')
          await this._refreshCompositionTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模型列表
        }
      } catch (error) {
        ElMessage.info('已取消删除')
      }
    },


    //  分页或者修改每页条数时，自动调用
    async getCompositionList(page, params) {
      if (!globeParams.curNode)
        globeParams.curNode = this.$parent.getCurNode()        //  调用父组件的方法，获取当前节点
      let res = await fetchList({
        nodeId: globeParams.curNode.id,
        modelType: 'composition',
        pageSize: page.pageSize,
        current: page.currentPage
      })
      if (res.data.code === 0) {
        this.compositionRowIndex = 0
        res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
        this.compositionData = res.data.data.records
        page.total = res.data.data.total
      }
    },

    async addModel(modelType) {
      let self = this
      this.titleName = '新增模板'
      this.algoForm.modelName = ''
      this.algoForm.modelId = null
      this.algoForm.modelType = modelType
      let res = await getVarsByNodeId(globeParams.curNode.id)
      if (res.data.code === 0) {
        this.sensorData = res.data.data      //  加载列表数据
      }
      this.dlgModel = true        //  弹窗
      let resAlgoList
      this.varShow = true
      resAlgoList = await reqDomainConfigList()
      await this.$nextTick(() => {      //  等待窗口弹出渲染成功后
        self.algoOption.column[1].dicData = resAlgoList.data.data
        self.algoForm.algoId = resAlgoList.data.data[0].value           //  第一个选项设置为默认
      })
    },


    selectionChange(val) {
      this.checkVars = val
    },

    //  提交保存
    async submit() {
      let self = this
      if (this.titleName === '新增模板')
        await this.modelAdd()
      else if (this.titleName === '编辑组态模板')
        await this.modelEdit()
      let keys = Object.keys(this.algoForm)           //  查询algoForm的所有变量
      keys.forEach(key => {
        self.algoForm[key] = null                   // 遍历属性名，清空每个属性值
      })
    },

    //  编辑模型（感知、诊断）
    async modelEdit() {
      let res = await reqEditModel(this.algoForm, this.checkVars)
      if (res.data.code === 0) {
        ElMessage.success('编辑成功')
        this.dlgModel = false
        await this._refreshCompositionTableByNodeId(globeParams.curNode.id)
      }
    },


    //  新增模型（感知、诊断）
    async modelAdd() {
      this.algoForm.nodeId = globeParams.curNode.id

      let res = await reqAddModel(this.algoForm, this.checkVars)
      if (res.data.code === 0) {
        if(res.data.msg === '可能没有选择感知变量'){
          ElMessage({
            message: "可能没有选择感知变量！",
            type: 'warning'
          })
        }else {
          ElMessage.success('新增成功')
        }

        this.dlgModel = false
        await this._refreshCompositionTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模型列表
      }
    },

    cancel() {
      this.dlgModel = false
    },

    getModels(node) {
      globeParams.curNode = node                  //  缓存当前节点
      this._refreshCompositionTableByNodeId(node.id)
    },

    //  根据节点Id，刷新状态感知数据
    async _refreshCompositionTableByNodeId(nodeId) {
      let res = await fetchList({
        nodeId: nodeId,
        modelType: 'composition',
        pageSize: this.compositionPage.pageSize,
        current: this.compositionPage.currentPage
      })
      if (res.data.code === 0) {
        res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
        this.compositionData = res.data.data.records
        this.compositionPage.total = res.data.data.total
      }
    },
  },

}
</script>
<style lang="scss" scoped>
@import 'styles/my-dialog.scss';
@import 'styles/my-avue-crud.scss';
@import 'styles/button/resource.scss';

.tablecon {
  height: 100%;
  width: 100%;
  display: flex;

  .table {
    height: 100%;
    width: 100%;

    .title {
      height: 50px;
      width: 100%;
      background-color: cadetblue;
      display: flex;
      color: white;
      letter-spacing: 2px; /* 设置字间距为 2 像素 */
      justify-content: center; /* 水平居中 */
      align-items: center; /* 垂直居中 */
    }
  }

  .split {
    height: 100%;
    width: 3px;
    /*background-color: #5a5e66;*/
  }

  .sensor-zone {
    height: 400px;
    overflow-y: auto;
  }

}
</style>
