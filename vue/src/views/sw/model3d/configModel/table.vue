<template>
  <div class="tablecon">
    <div class="subtable">
      <div class="title">状态感知模板</div>
      <avue-crud ref="crudPerceivedRef" class="h-avue-crud"
                 v-model="perceivedForm"
                 :option="perceivedOption"
                 @on-load="getPerceivedList"
                 v-model:page="perceivedPage"
                 :data="perceivedData"
                 @row-click="perceivedRowClick">
        <template #menu-left="{}">
          <el-button type="primary"
                     class="addBtn"
                     @click="addModel('perceived')">新增
          </el-button>
        </template>
        <template #perceivedSelected="scope">
          &nbsp;&nbsp;&nbsp;<el-radio v-model="perceivedRowIndex" :label="scope.index"><span></span></el-radio>
        </template>
        <template #relatedPerceivedVar="scope">
          <el-button type="text" class="viewBtn" @click="perceivedVarsClick(scope.row)">详情</el-button>
        </template>
        <template #menu="{size,row,index}">
          <el-button type="text" class="editBtn" @click="editModel(row, 'perceived')">编辑</el-button>
          <br>
          <el-button type="text" class="delBtn" @click="deleteModel(row, 'perceived')">删除</el-button>
        </template>
      </avue-crud>
    </div>
    <div class="split"></div>
    <div class="subtable">
      <div class="title">故障诊断模板</div>
      <avue-crud ref="crudRef" class="h-avue-crud"
                 v-model="failureForm"
                 :option="failureOption"
                 @on-load="getFailureList"
                 v-model:page="failurePage"
                 :data="failureData"
                 @row-click="failureRowClick">
        <template #menu-left="{}">
          <el-button type="primary"
                     class="addBtn"
                     @click="this.algoForm = {}; addModel('failure')">新增
          </el-button>
        </template>
        <template #failureSelected="scope">
          &nbsp;&nbsp;&nbsp;<el-radio v-model="failureRowIndex" :label="scope.index"><span></span></el-radio>
        </template>
        <template #relatedFailureVar="scope">
          <el-button type="text" class="viewBtn" @click="failureVarsClick(scope.row)">详情</el-button>
        </template>
        <template #menu="{size,row,index}">
          <el-button type="text" class="editBtn" @click="editModel(row, 'failure')">编辑</el-button>
          <el-button type="text" class="delBtn" @click="deleteModel(row, 'failure')">删除</el-button>
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
  perceivedTableOption, failureTableOption,
  sensorTableOption, algoFormOption, varTableOption
} from '@/const/crud/sw/model3d/configModel/table'
import {
  fetchList, getVarsByNodeId, putObj, reqAddModel, delObj, getObj,
  reqAlgoList, reqAlgoList1, getVarsByModelId, reqEditModel, pageByModelId, reqDomainConfigList
} from '@/api/sw/model3d/configModel/table.js'
import {ElMessageBox, ElMessage} from 'element-plus'

let globeParams = {}     //  声明一个全局参数对象

export default {
  name: 'modelTable',

  setup() {
    let perceivedOption = ref(perceivedTableOption)
    let perceivedData = ref([])
    let perceivedForm = ref({})
    let perceivedPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })

    let failureOption = ref(failureTableOption)
    let failureData = ref([])
    let failureForm = ref({})
    let failurePage = ref({
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
    let algoOption = ref(algoFormOption)
    let defaults = ref({})
    let perceivedRowIndex = ref(0)
    let failureRowIndex = ref(0)
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
      perceivedOption, perceivedData, perceivedForm, perceivedPage,
      failureOption, failureData, failureForm, failurePage,
      titleName, dlgModel, varShow, sensorData, sensorOption,
      algoForm, algoOption, defaults, perceivedRowIndex,
      failureRowIndex, dlgVars, varForm, varOption, varData, varPage
    }
  },

  methods: {

    perceivedRowClick(row) {
      this.perceivedRowIndex = row.$index
    },

    failureRowClick(row) {
      this.failureRowIndex = row.$index
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
    perceivedVarsClick(row) {
      this.curModelRow = row     //   缓存当前用户选中的行
      this.dlgVars = true        //  弹窗
      this.getList(this.varPage)
    },

    //  查看感知变量详情（故障诊断）
    failureVarsClick(row) {
      this.curModelRow = row     //   缓存当前用户选中的行
      this.dlgVars = true        //  弹窗
      this.getList(this.varPage)
    },

    //  模型编辑按钮（状态感知、故障诊断）
    editModel(row, modelType) {
      let self = this
      this.titleName = modelType === 'perceived' ? '编辑状态感知模板' : '编辑故障诊断模板'
      this.dlgModel = true        //  弹窗

      if (modelType === 'perceived') {
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

        reqAlgoList().then(resAlgoList => {
          self.$nextTick(() => {      //  等待窗口弹出渲染成功后
            self.algoOption.column[7].dicData = resAlgoList.data.data       //  填写服务算法列表字典
            console.log( '评估算法',resAlgoList.data.data )
            getObj(row.modelId).then(response => {                          //  根据modelId查询编辑行数据
              if (response.data.code === 0)
                self.algoForm = response.data.data
            })
          })
        })

      //  测试调用组态模型的接口
        reqDomainConfigList().then(resAlgoList => {
          self.$nextTick(() => {      //  等待窗口弹出渲染成功后
            let domainconfig= resAlgoList.data.data       //  填写服务算法列表字典
            console.log( '组态模型',domainconfig)

          })
        })
      } else {
        this.varShow = false
        reqAlgoList1().then(resAlgoList => {
          self.$nextTick(() => {      //  等待窗口弹出渲染成功后
            self.algoOption.column[7].dicData = resAlgoList.data.data       //  填写服务算法列表字典
            getObj(row.modelId).then(response => {                          //  根据modelId查询编辑行数据
              if (response.data.code === 0)
                self.algoForm = response.data.data
            })
          })
        })
      }
    },


    // 删除状态感知或故障诊断模板
    async deleteModel(row, modelType) {
      try {
        await this.$confirm('此操作将删除当前模型及其关联的模板, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch (error) {
        ElMessage.info('已取消删除')
        return
      }

      let response = null
      let requestError = null
      try {
        response = await delObj({ modelId: row.modelId, modelType })
      } catch (error) {
        requestError = error
      }

      try {
        if (modelType === 'failure') {
          await this._refreshFailureTableByNodeId(globeParams.curNode.id)
        } else {
          await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)
        }
      } catch (error) {
        console.error('删除后刷新模板列表失败', error)
      }

      const currentRows = modelType === 'failure' ? this.failureData : this.perceivedData
      const rowStillExists = currentRows.some(item => String(item.modelId) === String(row.modelId))
      if (response?.data?.code === 0 || !rowStillExists) {
        ElMessage.success('删除成功')
        return
      }

      ElMessage.error(response?.data?.msg || requestError?.response?.data?.msg || requestError?.message || '删除失败')
    },


    //  分页或者修改每页条数时，自动调用
    async getPerceivedList(page, params) {
      if (!globeParams.curNode)
        globeParams.curNode = this.$parent.getCurNode()        //  调用父组件的方法，获取当前节点
      let res = await fetchList({
        nodeId: globeParams.curNode.id,
        modelType: 'perceived',
        pageSize: page.pageSize,
        current: page.currentPage
      })
      if (res.data.code === 0) {
        this.perceivedRowIndex = 0
        res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
        this.perceivedData = res.data.data.records
        page.total = res.data.data.total
      }
    },


    async getFailureList(page, params) {
      if (!globeParams.curNode)
        globeParams.curNode = this.$parent.getCurNode()        //  调用父组件的方法，获取当前节点
      let res = await fetchList({
        nodeId: globeParams.curNode.id,
        modelType: 'failure',
        pageSize: page.pageSize,
        current: page.currentPage
      })
      if (res.data.code === 0) {
        this.failureRowIndex = 0
        res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
        this.failureData = res.data.data.records
        page.total = res.data.data.total
      }
    },


    async addModel(modelType) {
      let self = this
      if (!globeParams.curNode || !globeParams.curNode.id) {
        ElMessage.warning('请先在左侧结构树选择节点')
        return
      }
      this.titleName = '新增模板'
      this.algoForm.modelName = ''
      this.algoForm.modelId = null
      this.algoForm.assessType = '1'
      this.algoForm.modelType = modelType
      let res = await getVarsByNodeId(globeParams.curNode.id)
      if (res.data.code === 0) {
        this.sensorData = res.data.data      //  加载列表数据
      }
      this.dlgModel = true        //  弹窗
      let resAlgoList
      if (modelType === 'perceived') {
        this.varShow = true
        resAlgoList = await reqAlgoList()
      }else {
        this.varShow = false
        resAlgoList = await reqAlgoList1()
      }
      this.$nextTick(() => {      //  等待窗口弹出渲染成功后
        self.algoOption.column[7].dicData = resAlgoList.data.data
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
      else if ((this.titleName = '编辑状态感知模板') || (this.titleName = '编辑故障诊断模板'))
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
        if (this.algoForm.modelType === 'perceived')
          await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模型列表
        else if (this.algoForm.modelType === 'failure')
          await this._refreshFailureTableByNodeId(globeParams.curNode.id)       //  刷新故障诊断模型列表
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
        if (this.algoForm.modelType === 'perceived')
          await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模型列表
        else if (this.algoForm.modelType === 'failure')
          await this._refreshFailureTableByNodeId(globeParams.curNode.id)       //  刷新故障诊断模型列表
      }
    },

    cancel() {
      this.dlgModel = false
    },

    getModels(node) {
      globeParams.curNode = node                  //  缓存当前节点
      this._refreshPerceivedTableByNodeId(node.id)
      this._refreshFailureTableByNodeId(node.id)
    },

    //  根据节点Id，刷新状态感知数据
    async _refreshPerceivedTableByNodeId(nodeId) {
      let res = await fetchList({
        nodeId: nodeId,
        modelType: 'perceived',
        pageSize: this.perceivedPage.pageSize,
        current: this.perceivedPage.currentPage
      })
      if (res.data.code === 0) {
        res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
        this.perceivedData = res.data.data.records
        this.perceivedPage.total = res.data.data.total
      }
    },

    //  根据节点Id，刷新故障诊断数据
    async _refreshFailureTableByNodeId(nodeId) {
      let res = await fetchList({
        nodeId: nodeId,
        modelType: 'failure',
        pageSize: this.failurePage.pageSize,
        current: this.failurePage.currentPage
      })
      if (res.data.code === 0) {
        res.data.data.records.forEach(item => item.hasChildren = false)         //  表格数据没有子节点
        this.failureData = res.data.data.records
        this.failurePage.total = res.data.data.total
      }
    }


  },

  watch: {
    'algoForm.assessType'(val) {     //  监听弹出对话框的单选按钮，动态显示和隐藏输入框
      if (val == '0') {
        this.defaults.limitNumber.display = false
        this.defaults.thresholdType.display = false
      } else if (val == '1') {
        this.defaults.limitNumber.display = true
        this.defaults.thresholdType.display = true
      }
    },
    'algoForm.serviceType'(val) {      //  根据服务类型，动态设定默认值
      if (val == '1') {
        //  隐藏3个输入选项
        this.defaults.failureCriterion.display = false
        this.defaults.trendPrediction.display = false
        this.defaults.resultLimitNum.display = false
        //  设定默认值
        this.algoForm.indexRequirement = '1'
        this.algoForm.assessIndex = '1'
        this.algoForm.thresholdType = '1'
        this.algoForm.modId = 1
      } else if (val == '2') {
        this.algoForm.failureCriterion = '1'
        this.defaults.failureCriterion.display = true
        this.algoForm.trendPrediction = '1'
        this.defaults.trendPrediction.display = true
        this.algoForm.resultLimitNum = 3
        this.defaults.resultLimitNum.display = true
        this.algoForm.indexRequirement = '1'
        this.algoForm.assessIndex = '1'
        this.algoForm.thresholdType = '3'
        this.algoForm.modId = 3
      } else if (val == '3') {
        this.defaults.failureCriterion.display = false
        this.defaults.trendPrediction.display = false
        this.defaults.resultLimitNum.display = false
        this.algoForm.indexRequirement = '2'
        this.algoForm.assessIndex = '4'
        this.algoForm.thresholdType = '1'
        this.algoForm.modId = 4
      }
    }

  }

}
</script>
<style lang="scss" scoped>
@import '@/styles/my-dialog.scss';
@import '@/styles/my-avue-crud.scss';
@import '@/styles/button/resource.scss';

.tablecon {
  height: 100%;
  width: 100%;
  display: flex;

  .subtable {
    height: 100%;
    width: 50%;

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
