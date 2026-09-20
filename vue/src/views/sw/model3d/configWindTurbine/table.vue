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
        <avue-form :option="algoOption" v-model="algoForm" ref="refAlgoConfig">
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
  sensorTableOption, perceivedTaskFormOption, varTableOption, pTemplateTableOption
} from '@/const/crud/sw/model3d/configModel/table'
import {
  getObj,
  reqAlgoList,
  getVarsByModelId,
  fetchTaskList,
  varPageByTaskId,
  pageByModelId,
  getVarsByTaskId,
  getTask,
  delTask,
  operateTask,
  stopTask,
  prepareTaskVariables,
  saveTaskConfiguration
} from '@/api/sw/model3d/configModel/table.js'
import {ElMessage} from 'element-plus'

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
    let algoOption = ref(perceivedTaskFormOption)
    let dlgVars = ref(false)
    let varForm = ref({})
    let varOption = ref(varTableOption)
    let varData = ref([])
    let varPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })
    let submitMode = ref('edit')


    return {
      perceivedOption, perceivedData, perceivedTask, perceivedForm, perceivedPage, perceivedLoading,
      titleName, dlgModel, sensorData, sensorOption,
      algoForm, algoOption, dlgVars, varForm, varOption, varData, varPage, submitMode
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

    getMatchedVariableRows(sensorData = [], selectedVars = []) {
      return sensorData.filter(itemA => selectedVars.some(itemB => {
        const sameId = itemA.varId !== undefined && itemB.varId !== undefined
            && String(itemA.varId) === String(itemB.varId)
        const sameName = itemA.varName && itemB.varName && itemA.varName === itemB.varName
        return sameId || sameName
      }))
    },

    resolveCheckVars(relatedRows = []) {
      const instanceRows = Array.isArray(this.sensorData) ? this.sensorData : []
      const checkArray = this.getMatchedVariableRows(instanceRows, relatedRows)
      this.checkVars = checkArray
      this.$nextTick(() => {
        if (this.$refs.crudSensor) {
          this.$refs.crudSensor.toggleSelection(checkArray)
        }
      })
    },

    //  生成或编辑实例任务
    async editModel(row, mode = 'edit') {
      const currentBomNodeId = globeParams.curNode && globeParams.curNode.id
          ? globeParams.curNode.id
          : row.nodeId
      if (!currentBomNodeId) {
        ElMessage.warning('请先选择一个实例节点')
        return
      }

      this.submitMode = mode
      this.curModelRow = row
      this.checkVars = []
      this.sensorData = []
      this.varList = []
      this.titleName = mode === 'generate' ? '生成状态感知任务' : '编辑状态感知任务'
      this.dlgModel = true

      try {
        const relatedLoader = row.taskId === null
            ? getVarsByModelId(row.modelId)
            : getVarsByTaskId({taskId: row.taskId})
        const formLoader = row.taskId === null
            ? getObj(row.modelId)
            : getTask({taskId: row.taskId})
        const [instanceRes, relatedRes, algoRes, formRes] = await Promise.all([
          prepareTaskVariables(currentBomNodeId, row.modelId),
          relatedLoader,
          reqAlgoList(),
          formLoader
        ])

        if (instanceRes.data.code !== 0) throw new Error(instanceRes.data.msg || '准备实例变量失败')
        if (relatedRes.data.code !== 0) throw new Error(relatedRes.data.msg || '读取模型变量失败')
        if (formRes.data.code !== 0) throw new Error(formRes.data.msg || '读取任务配置失败')

        this.sensorData = Array.isArray(instanceRes.data.data) ? instanceRes.data.data : []
        this.varList = Array.isArray(relatedRes.data.data) ? relatedRes.data.data : []
        if (!this.sensorData.length) throw new Error('当前设备没有可用的实例变量')

        const algorithmColumn = this.algoOption.column.find(item => item.prop === 'algoId')
        if (algorithmColumn) {
          algorithmColumn.dicData = Array.isArray(algoRes.data.data) ? algoRes.data.data : []
        }
        this.algoForm = {
          ...(formRes.data.data || {}),
          taskId: row.taskId || null,
          nodeId: currentBomNodeId,
          modelId: row.modelId
        }
        this.resolveCheckVars(this.varList)
      } catch (error) {
        this.dlgModel = false
        ElMessage.error(error.message || '读取任务配置失败')
      }
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
      const saved = await this.modelEdit()
      if (!saved) return

      let keys = Object.keys(this.algoForm)           //  查询algoForm的所有变量
      keys.forEach(key => {
        self.algoForm[key] = null                   // 遍历属性名，清空每个属性值
      })
    },

    // 编辑模型
    async modelEdit() {
      if (!globeParams.curNode || !globeParams.curNode.id) {
        ElMessage.warning('请先选择一个实例节点')
        return false
      }

      if (!Array.isArray(this.checkVars) || this.checkVars.length === 0) {
        ElMessage.warning('请选择感知变量')
        return false
      }

      const dataDimension = Number(this.algoForm.dataDimension)
      if (!Number.isInteger(dataDimension) || dataDimension <= 0) {
        ElMessage.warning('请输入大于 0 的整数数据维度')
        return false
      }
      if (!this.algoForm.algoId) {
        ElMessage.warning('请选择服务算法')
        return false
      }

      const taskConfig = {
        ...this.algoForm,
        nodeId: globeParams.curNode.id,
        dataDimension
      }
      let res = await saveTaskConfiguration(taskConfig, this.checkVars)
      if (res.data.code === 0) {
        ElMessage.success(this.submitMode === 'generate' ? '生成任务成功' : '编辑成功')
        this.dlgModel = false
        await this._refreshPerceivedTableByNodeId(globeParams.curNode.id)     //  刷新状态感知模板列表
        return true
      }
      return false
    },

    cancel() {
      this.dlgModel = false
    },

    getModels(node) {
      globeParams.curNode = node                  //  缓存当前节点
      this._refreshPerceivedTableByNodeId(node.id)
    },

    async _fetchPerceivedRows(nodeId) {
      let res = await fetchTaskList({nodeId: nodeId, modelType: 'perceived'})
      if (res.data.code === 0) {
        return Array.isArray(res.data.data) ? res.data.data : []
      }
      return []
    },

    //  根据节点Id，刷新状态感知模板数据
    async _refreshPerceivedTableByNodeId(nodeId) {
      this.perceivedLoading = false
      const rows = await this._fetchPerceivedRows(nodeId)
      let taskList = []
      let modelList = []

      rows.forEach(item => {
        item.hasChildren = false //  表格数据没有子节点
        if (item.taskId === null) {
          modelList.push(item)
        } else {
          taskList.push(item)
        }
      })
      this.perceivedTask = [...taskList, ...modelList]
      this.perceivedPage.total = rows.length
      this.perceivedData = this.perceivedTask.slice(0, this.perceivedPage.pageSize)
    },

    async runTemplate(row){
      if (!globeParams.curNode || !globeParams.curNode.id) {
        ElMessage.warning('请先选择一个实例节点')
        return
      }
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
      if (!globeParams.curNode || !globeParams.curNode.id) {
        ElMessage.warning('请先选择一个实例节点')
        return
      }
      this.editModel(row, 'generate')
    },


  },
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
