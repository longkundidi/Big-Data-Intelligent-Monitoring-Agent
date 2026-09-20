<template>
  <basic-container>
    <el-row style="margin-left: 350px">
      <el-col :span="2" style="margin-right: 20px">
        <h3>查询条件：</h3>
      </el-col>
      <el-col :span="5" style="margin-top: 15px; margin-right: 30px">
        <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                   :placeholder="formdata.project">
          <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                     @click="formdata.project=item.project; findProductModels(item.project,item.id)"
          >
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="5" style="margin-top: 15px; margin-right: 30px">
        <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                   :placeholder="formdata.productModel" @clear="searchProId=false">
          <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                     @click="formdata.productModel = item.productModel; formdata.projectId = item.id">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="4" style="margin-top: 17px;">
        <el-button class="normalBtn" size="large" :style="{ width: 'auto' }" @click="performDiagnostics()">查 询
        </el-button>
      </el-col>
    </el-row>
    <div class="tablecon" style="margin-top: 20px">
      <div class="subtable">
        <div class="title">故障报警信息</div>
        <avue-crud ref="crudFaultAlarmRef" class="h-avue-crud"
                   :option="alarmOption"
                   :page="alarmPage"
                   :data="alarmData"
                   @on-load="getAlarmList"
                   @size-change="sizeChange"
                   @current-change="currentChange"
                   @row-click="alarmRowClick">
          <template #alarmRadio="scope">
            &nbsp;&nbsp;&nbsp;<el-radio v-model="alarmRowIndex" :label="scope.index"><span></span></el-radio>
          </template>
          <template #errorStatus="scope">
            <el-tag type="danger" style="text-align: center">出现报警</el-tag>
          </template>
          <template #menu="{size,row,index}">
            <el-button class="editWorktBtn" @click="loadData(row)">执行诊断</el-button>
          </template>
        </avue-crud>
      </div>
    </div>

    <el-dialog title="选择测点"
               v-model="dlgLoadLocation"
               draggable
               width="25%">
      <el-select v-model="dataSelectedOption" filterable clearable placeholder="请选择">
        <el-option
            v-for="item in dataSelected"
            :key="item.value || item"
            :label="item.label || item"
            :value="item.value || item">
        </el-option>
      </el-select>
      <div class="dlgFooter">
          <span slot="footer">
          <el-button class="auditBtn" @click="confirm()">确 认</el-button>
          <el-button class="disMissBtn" @click="close()">取 消</el-button>
        </span>
      </div>
    </el-dialog>

  </basic-container>
</template>
<script>
import {ref} from 'vue'
import {getProductModelBySceneName, getProductModels, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
import {tableOption, dataOption} from "@/const/crud/diagnosis/faultLocation/table";
import {getAlarmList} from "@/api/diagnosis/faultLocation";
import {fetchTaskList, getVarsByTaskId} from "@/api/sw/model3d/configModel/table";

export default {


  setup() {

    let formdata = ref({
      project: '',
      projectId: '',
      productModel: '',
    })
    let projectList = ref([])
    let productModelList = ref([])
    let searchProId = ref(false)
    const crudFaultAlarmRef = ref(null)
    let alarmOption = ref(tableOption)
    let alarmData = ref([])
    let allAlarms = ref([])
    let alarmPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })
    let alarmRowIndex = ref(null)
    let dlgLoadData = ref(false)
    let dataForm = ref([])
    let dataFormOption = ref(dataOption)
    let dataRowIndex = ref(null)
    let dlgLoadLocation = ref(false)
    let dataSelected = ref([])
    let dataSelectedOption = ref(null)

    return {
      formdata,
      projectList,
      productModelList,
      searchProId,
      crudFaultAlarmRef,
      alarmOption,
      alarmData,
      allAlarms,
      alarmPage,
      alarmRowIndex,
      dlgLoadData,
      dataForm,
      dataFormOption,
      dataRowIndex,
      dlgLoadLocation,
      dataSelected,
      dataSelectedOption
    }
  },

  mounted() {
    this.projectInit()
  },

  methods: {
    projectInit() { //初始化项目列表
      const userId = this.$store.state.user.userInfo.userId
      getProjects({userId: userId, userRole: 'GENERAL_USER'}).then(response => {
        if(response.data.code === 0){
          let res = response.data.data
          this.projectList = res.filter((item, index, arr) =>
              index === arr.findIndex((t) => t.project === item.project)
          )
        }
      })
    },

    findProductModels(proName, proId) {
      //切换项目的时候，需要初始化值
      this.treedata = []
      this.formdata.productModel = '';
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    //  修改每页条数
    sizeChange(pageSize){
      this.alarmPage.currentPage = 1
      this.alarmPage.pageSize = pageSize
      this.getAlarmList()
    },

    //  翻页
    currentChange(current){
      this.alarmPage.currentPage = current
      this.getAlarmList()
    },

    //  分页或者修改每页条数时，自动调用
    getAlarmList() {

      if(this.searchProId === true){
        getAlarmList({ proId: this.formdata.projectId }).then(res => {
          if (res.data.code === 0){
            this.allAlarms = res.data.data
            this.updatePageData()
          }
        })
      }else {
        this.allAlarms = []
        this.alarmData = []
      }
    },

    updatePageData() {
      // 根据当前页码和页面大小计算显示哪些数据
      const start = (this.alarmPage.currentPage - 1) * this.alarmPage.pageSize
      const end = start + this.alarmPage.pageSize
      this.alarmData = this.allAlarms.slice(start, end)
      this.alarmPage.total = this.allAlarms.length
    },

    performDiagnostics(){
      this.searchProId = true
      this.getAlarmList()
    },

    extractVariableList(res) {
      const raw = res?.data?.data
      if (Array.isArray(raw)) return raw
      if (Array.isArray(raw?.records)) return raw.records
      if (Array.isArray(raw?.list)) return raw.list
      return []
    },

    async loadData(row) {
      this.alarmRowIndex = row.$index
      if (this.alarmRowIndex === null || this.alarmRowIndex === undefined) {
        this.$message.warning('请选择一行数据')
        return
      }

      const alarm = this.alarmData[this.alarmRowIndex]
      this.dataSelectedOption = null
      this.dataSelected = []
      this.dlgLoadLocation = true

      if (!alarm?.nodeId) {
        this.$message.error("该报警节点缺少节点ID，无法查询感知变量任务！")
        this.dlgLoadLocation = false
        return
      }

      try {
        const taskRes = await fetchTaskList({nodeId: alarm.nodeId, modelType: 'perceived'})
        const taskListRaw = Array.isArray(taskRes?.data?.data) ? taskRes.data.data : []
        const taskList = taskListRaw.filter(item => {
          return item?.taskId && [0, 1, '0', '1'].includes(item?.status)
        })

        const optionList = []
        const optionKeySet = new Set()
        for (const task of taskList) {
          try {
            const varsRes = await getVarsByTaskId({taskId: task.taskId})
            const varList = this.extractVariableList(varsRes)
            varList.forEach(item => {
              const varName = item?.varName || item?.variableName || item?.name
              if (!varName) return
              const optionKey = String(varName)
              if (optionKeySet.has(optionKey)) return
              optionKeySet.add(optionKey)

              const modelName = task.modelName || task.templateName || `任务${task.taskId}`
              optionList.push({
                label: `${varName} (${modelName})`,
                value: String(varName),
                taskId: task.taskId
              })
            })
          } catch (error) {
            console.warn('getVarsByTaskId 查询失败', task.taskId, error)
          }
        }

        if (optionList.length === 0) {
          this.$message.error("该部件无可用感知变量任务，无法进行故障诊断！")
          this.dlgLoadLocation = false
        } else {
          this.dataSelected = optionList
        }
      } catch (error) {
        console.warn('按节点读取感知变量任务失败', alarm.nodeId, error)
        this.$message.error("测点数据加载失败，无法进行故障诊断！")
        this.dlgLoadLocation = false
      }
    },

    confirm() {
      if (this.dataSelectedOption !== null && this.dataSelectedOption !== '') {
        let _selectAlarm = {...this.alarmData[this.alarmRowIndex], location: this.dataSelectedOption}

        // 关闭对话框
        this.dlgLoadLocation = false

        // 打开新标签页
        this.openRunModel({monitor: _selectAlarm, farmName: this.formdata.project})
        this.alarmRowIndex = null
      } else {
        this.$message.warning('请选择一项数据')
      }
    },

    alarmRowClick(row) {
      this.alarmRowIndex = row.$index
    },

    openRunModel(paramsObj) {
      const paramsJson = JSON.stringify(paramsObj)
      this.$router.push({
        path: '/diagnosis/faultLocation/runModel',
        query: {
          object: paramsJson
        }
      }).catch(() => {
        console.log('页面未找到')
        this.$message.error(paramsObj)
      })
    },

    close(){
      this.dlgLoadLocation = false
      this.dataSelectedOption = null
    }


  }

}

</script>
<style lang="scss" scoped>
.tablecon {
  height: 100%;
  width: 100%;
  display: flex;

  .subtable {
    height: 100%;
    width: 100%;

    .title {
      height: 40px;
      width: 100%;
      background-color: cadetblue;
      display: flex;
      color: white;
      letter-spacing: 2px; /* 设置字间距为 2 像素 */
      justify-content: center; /* 水平居中 */
      align-items: center; /* 垂直居中 */
    }
  }
}

.el-table__body {
  position: absolute;
  transition: all 500ms linear;
}

.dlgFooter{
  text-align: center;
  margin-top: 20px;
  .el-button {
    height: 30px;
  }
}

</style>
