<template>
  <div class="tablecon">
    <div class="subtable">
      <div class="title">状态感知模板</div>
      <avue-crud ref="crudPerceivedRef" class="h-avue-crud"
                 v-model="perceivedForm"
                 :option="perceivedOption"
                 :data="perceivedData"
                 :page="perceivedPage"
                 :table-loading="perceivedLoading"
                 @size-change="sizeChange"
                 @current-change="currentChange"
                 @on-load="getPerceivedList">
        <template #modelName="scope">
          <span>{{ scope.row.modelName }}</span>
          <el-tag v-if="scope.row.isDefaultModel" style="margin-left: 5px;">默认模板</el-tag>
        </template>
        <template #status="scope">
          <el-tag v-if="scope.row.status === 0" type="primary" style="text-align: center">已生成任务</el-tag>
          <el-tag v-if="scope.row.status === 1" type="success" style="text-align: center">运行中</el-tag>
          <el-tag v-if="scope.row.status === null" type="warning">未生成任务</el-tag>
        </template>
        <template #relatedTPerceivedVar="scope">
          <el-button type="text" class="viewBtn" @click="perceivedVarsClick(scope.row)">详情</el-button>
        </template>
        <template #menu="{size,row,index}">
          <el-button v-if="row.status === 0"
                     type="text" class="editWorktBtn" @click="runTemplate(row)">运行
          </el-button>
          <el-button v-if="row.status === 0"
                     type="text" class="editBtn" @click="editModel(row)">编辑
          </el-button>
          <el-button v-if="row.status === 0"
                     type="text" class="delBtn" @click="deleteModel(row)">删除
          </el-button>
          <el-button v-if="row.status === 1"
                     type="text" class="disMissBtn" @click="stopTemplate(row)">停止
          </el-button>
          <el-button v-if="row.status === null"
                     type="text" class="addBtn" @click="executeTemplate(row)">生成
          </el-button>
        </template>
      </avue-crud>
    </div>
    <el-dialog v-model="dlgModel" :title="titleName" width="70%" draggable>
      <div><H2 style="font-size: 18px;"><strong>选择感知变量</strong></H2>
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
  sensorTableOption, algoFormOption, varTableOption, pTemplateTableOption
} from '@/const/crud/sw/model3d/configModel/table'
import {
  getObj,
  reqAlgoList,
  getVarsByModelId,
  fetchTaskList,
  varPageByTaskId,
  pageByModelId,
  getVarsByNodeId,
  getVarsByTaskId,
  reqEditTask,
  getTask,
  delTask,
  createTemplate,
  operateTask,
  stopTask,
  getVarsByBomNodeId
} from '@/api/sw/model3d/configModel/table.js'
import {ElMessageBox, ElMessage} from 'element-plus'

let globeParams = {}     //  声明一个全局参数对象

export default {
  name: 'modelTable',

  setup() {
    let perceivedOption = ref(pTemplateTableOption)
    let perceivedData = ref([])
    let perceivedTask = ref([])
    let perceivedForm = ref({})
    let perceivedPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })
    let perceivedLoading = ref(false)

    let titleName = ref('')
    let dlgModel = ref(false)
    let sensorData = ref([])
    let sensorOption = ref(sensorTableOption)
    let algoForm = ref({})
    let algoOption = ref(algoFormOption)
    let defaults = ref({})
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
      perceivedOption, perceivedData, perceivedTask, perceivedForm, perceivedPage, perceivedLoading,
      titleName, dlgModel, sensorData, sensorOption,
      algoForm, algoOption, defaults, dlgVars, varForm, varOption, varData, varPage
    }
  },

  methods: {

    //  显示感知变量表格时，自动调用
    async getList(page, params) {
      let res;
      if (this.curModelRow.taskId === null) {
        res = await pageByModelId({
          modelId: this.curModelRow.modelId,
          pageSize: page.pageSize,
          current: page.currentPage
        })
      } else {
        res = await varPageByTaskId({
          taskId: this.curModelRow.taskId,
          current: page.currentPage,
          pageSize: page.pageSize
        })
      }
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

    //  模板编辑按钮
    editModel(row) {
      let self = this
      this.titleName = '编辑状态感知模板信息'
      this.dlgModel = true        //  弹窗
      //  加载感知变量列表，并勾选已经关联的变量
      getVarsByBomNodeId(row.nodeId).then(res => {          //  根据节点Id查询关联的感知变量，显示列表
        if (res.data.code === 0)
          self.sensorData = res.data.data             //  加载列表数据
        if (row.taskId === null) {
          getVarsByModelId(row.modelId).then(resVars => { //  根据modelId查询感知变量
            if (resVars.data.code === 0) {
              self.varList = resVars.data.data
              // 根据数组varList的varId,在数组sensorData中找到相同的对象
              // 在下面的代码中，filter()方法遍历sensorData中的每个元素，并对每个元素应用一个条件。
              // 条件是使用some()方法在varList中查找是否存在与数组sensorData当前元素的varId相同的对象。如果存在相同的varId，则filter()方法将保留该元素，最终返回一个新的数组，其中包含与数组varList中的varId相匹配的对象。
              // some()方法是JavaScript数组的一个方法，它用于检测数组中是否至少有一个元素满足指定条件。
              // varList.some()可以理解为在数组varList中进行遍历。在数组遍历的过程中，some()方法会对数组中的每个元素都应用一次指定的测试函数，直到找到一个满足条件的元素，然后立即返回 true。如果没有找到满足条件的元素，则返回 false。
              let checkArray = self.sensorData.filter(itemA => self.varList.some(itemB => itemB.varName === itemA.varName))   //  生成勾选列表
              self.$nextTick(() => {      //  等待窗口弹出渲染成功后
                self.$refs.crudSensor.toggleSelection(checkArray)                         //  勾选一个数组
                //self.$refs.crudSensor.toggleRowSelection(this.sensorData[1],true)       //  勾选一个感知变量
              })
            }
          })
        } else {
          getVarsByTaskId({taskId: row.taskId}).then(resVars => { //  根据taskId查询模板感知变量
            if (resVars.data.code === 0) {
              self.varList = resVars.data.data
              let checkArray = self.sensorData.filter(itemA => self.varList.some(itemB => itemB.varName === itemA.varName))   //  生成勾选列表
              self.$nextTick(() => {      //  等待窗口弹出渲染成功后
                self.$refs.crudSensor.toggleSelection(checkArray)                         //  勾选一个数组
              })
            }
          })
        }
      })
      reqAlgoList().then(resAlgoList => {
        self.$nextTick(() => {      //  等待窗口弹出渲染成功后
          self.algoOption.column[7].dicData = resAlgoList.data.data       //  填写服务算法列表字典
          if (row.taskId === null) {
            getObj(row.modelId).then(response => {                          //  根据modelId查询编辑行数据
              if (response.data.code === 0)
                self.algoForm = response.data.data
            })
          } else {
            getTask({taskId: row.taskId}).then(response => {                          //  根据taskId查询编辑行数据
              if (response.data.code === 0)
                self.algoForm = response.data.data
            })
          }

        })
      })
    },


    //  删除按钮（状态感知模型）
    async deleteModel(row) {
      try {
        await this.$confirm('此操作将删除当前模板及其关联的感知变量, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let response = await delTask(row.taskId)
        if (response.data.code === 1) {
          ElMessage.warning('请先停止任务再删除模板')
        }
        if (response.data.code === 0) {
          ElMessage.success('删除成功')
          await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模板列表
        }
      } catch (error) {
        ElMessage.info('已取消删除')
      }
    },

    //  分页或者修改每页条数时
    sizeChange(val) {
      this.perceivedPage.currentPage = 1
      this.perceivedPage.pageSize = val
      this.getPerceivedList()
    },
    currentChange(val) {
      this.perceivedPage.currentPage = val
      this.getPerceivedList()
    },
    getPerceivedList() {
      if (!this.perceivedData.includes(undefined) && this.perceivedData.length !== 0) {
        this.perceivedData = []
        for (let i = this.perceivedPage.pageSize * (this.perceivedPage.currentPage - 1) + 1;
             i <= ((this.perceivedPage.total > (this.perceivedPage.pageSize * this.perceivedPage.currentPage)) ? (this.perceivedPage.pageSize * this.perceivedPage.currentPage) : (this.perceivedPage.total));
             i++) {
          this.perceivedData.push(this.perceivedTask[i - 1]);
        }
        this.perceivedLoading = false
      }

    },

    selectionChange(val) {
      this.checkVars = val
    },

    //  提交保存
    async submit() {
      let self = this
      await this.modelEdit()

      let keys = Object.keys(this.algoForm)           //  查询algoForm的所有变量
      keys.forEach(key => {
        self.algoForm[key] = null                   // 遍历属性名，清空每个属性值
      })
    },

    // 编辑模型
    async modelEdit() {
      let res = await reqEditTask(this.algoForm, this.checkVars)
      if (res.data.code === 0) {
        ElMessage.success('编辑成功')
        this.dlgModel = false
        await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模板列表
      }
    },

    cancel() {
      this.dlgModel = false
    },

    getModels(node) {
      globeParams.curNode = node                  //  缓存当前节点
      this._refreshPerceivedTableByNodeId(node.id)
    },

    //  根据节点Id，刷新状态感知模板数据
    async _refreshPerceivedTableByNodeId(nodeId) {
      this.perceivedLoading = false
      let res = await fetchTaskList({nodeId: nodeId, modelType: 'perceived'})
      console.log('模版匹配：',res.data.data)
      let taskList = []
      let modelList = []

      if (res.data.code === 0) {
        res.data.data.forEach(item => {
          item.hasChildren = false //  表格数据没有子节点
          if (item.taskId === null) {
            modelList.push(item)
          } else {
            taskList.push(item)
          }
        })
        this.perceivedTask = [...taskList, ...modelList]
        this.perceivedPage.total = res.data.data.length
        this.perceivedData = this.perceivedTask.slice(0, this.perceivedPage.pageSize)
      }
    },

    async runTemplate(row){
      let res = await operateTask({modelId: row.modelId, bomNodeId: globeParams.curNode.id})
      if (res.data.code === 0) {
        ElMessage.success(res.data.msg)
        await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模板列表
      }
    },

    async stopTemplate(row){
      let res = await stopTask({taskId: row.taskId})
      if (res.data.code === 0) {
        ElMessage.success(res.data.msg)
        await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模板列表
      }

    },

    async executeTemplate(row) {
      let res = await createTemplate({modelId: row.modelId, bomNodeId: globeParams.curNode.id})
      if (res.data.code === 0) {
        ElMessage.success(res.data.msg)
        await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模板列表
      }
    },


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

  :deep(.el-table .el-table__cell.is-center) {
    text-align: left;
  }
}
</style>
