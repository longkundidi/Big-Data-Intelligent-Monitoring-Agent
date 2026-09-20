<template>
  <div v-loading="loading"
       element-loading-text="故障诊断中..."
       element-loading-background="rgba(0, 0, 0, 0.3)">
    <basic-container>
      <el-row style="border: 1px solid rgba(100, 100, 100, 0.3);">
        <el-col :span="12">
          <div class="tablecon">
            <div class="subtable">
              <div class="title">故障诊断模型</div>
              <avue-crud ref="crudDiagnosisAlarmRef" class="h-avue-crud"
                         :option="diagnosisOption"
                         @on-load="getDiagnosisList"
                         @size-change="sizeChange"
                         @current-change="currentChange"
                         v-model:page="diagnosisPage"
                         :data="diagnosisData"
                         @row-click="diagnosisRowClick">
                <template #diagnosisRadio="scope">
                  <el-radio v-model="diagnosisRowIndex" :label="scope.index"><span></span></el-radio>
                </template>
                <template #operate="scope">
                  <el-button class="editWorktBtn" @click="runModel(scope.row)">运行模型</el-button>
                </template>
              </avue-crud>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="tablecon">
            <div class="subtable">
              <div class="title">故障诊断结果</div>
              <result ref="refResult" :chartData="pieData" :tableData="tableData" :resultParams="resultParams" :visibleChart="showChart" :visibleTable="showTable"></result>
            </div>
          </div>
        </el-col>
      </el-row>
    </basic-container>
  </div>

</template>

<script>
import {getDiagnosisList, runModel} from "@/api/diagnosis/faultLocation";
import {failureModelOption} from "@/const/crud/diagnosis/faultLocation/table";
import result from './result.vue'
import {addSub_Minutes} from "@/util/date";
import {getObj} from "@/api/sw/model3d/configFailureMode/table";

export default {
  name: 'runDiagModel',

  data() {
    return {
      object: this.$route.query.object,       //  resumeTable页面传递的数据对象
      crudDiagnosisAlarmRef: null,
      diagnosisForm: [],
      diagnosisOption: failureModelOption,
      diagnosisPage: {
        total: 0,          // 总页数
        currentPage: 1,     // 当前页数
        pageSize: 10        // 每页显示多少条
      },
      diagnosisData: [],
      diagnosisRowIndex: null,
      resultParams: {},
      loading: false,
      showChart: false,
      showTable:false,
      resultData: {},
      pieData: [],
      tableData: {}
    }
  },

  components: {
    result,
  },

  mounted() {
    this.object = JSON.parse(this.object)
    this.resultParams = {
      //  监测点对象
      monitor: this.object.monitor,
      farmName: this.object.farmName
      // algorithmId: Number(this.object.algoConfig.algoId)              //  算法Id
    }
    this.getDiagnosisList()

  },
  methods: {
    //  修改每页条数
    sizeChange(pageSize){
      this.diagnosisPage.pageSize = pageSize
      this.updatePageData()
    },

    //  翻页
    currentChange(current){
      this.diagnosisPage.currentPage = current
      this.updatePageData()
    },

    //  分页或者修改每页条数时，自动调用
    getDiagnosisList() {
      if(Object.keys(this.resultParams).length !== 0){
        getDiagnosisList({taskId: this.resultParams.monitor.taskId}).then( res => {
          if (res.data.code === 0){
            this.diagnosisForm = res.data.data
            this.updatePageData()
          }
        })
      }
    },

    updatePageData() {
      // 根据当前页码和页面大小计算显示哪些数据
      const start = (this.diagnosisPage.currentPage - 1) * this.diagnosisPage.pageSize
      const end = start + this.diagnosisPage.pageSize
      this.diagnosisData = this.diagnosisForm.slice(start, end)
      this.diagnosisPage.total = this.diagnosisForm.length
    },

    diagnosisRowClick(row) {
      this.diagnosisRowIndex = row.$index
      this.resultParams.diagnosisModel = row.modelName
    },

    async runModel(row) {
      this.loading = true

      this.pieData = []
      this.tableData = []

      let endTime = new Date(this.resultParams.monitor.dcTime)
      let startTime = addSub_Minutes(this.resultParams.monitor.dcTime, -2)    //  报警点时间减2分钟
      let diagnoseInfo = {
        farmName: this.resultParams.farmName,
        turbineName: this.resultParams.monitor.turbineName,
        part: this.resultParams.monitor.nodeName,
        location: this.resultParams.monitor.location,
        algoId: row.algoId,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      }

      let maxPossibility = -Infinity
      let maxKey = null

      let res = await getObj(row.nodeId)
      let allFaultModes = res.data.data.map(item => item.failureName)

      runModel(diagnoseInfo).then(res => {

        let result = JSON.parse(res.data.data.taskResult)

        if (typeof result === 'object' && result !== null) {
          if ('message' in result) {
            this.tableData = {
              type: "正常",
              suggest: "暂无故障发生"
            }
            this.showTable = true
          } else {
            allFaultModes.forEach(item => {
              let curKey = null
              for (let key in result) {
                if (result.hasOwnProperty(key) && result[key].type === item) {
                  curKey = key
                  break
                }
              }
              if(curKey === null){
                let table = {
                  faultType: item,
                  possibility: 0.00,
                  suggest: ''
                }
                this.tableData.push(table)
              }else {
                let possibility = result[curKey].possibility
                if (possibility > maxPossibility) {
                  maxPossibility = possibility
                  maxKey = curKey
                }

                let chart = {
                  value: result[curKey].possibility * 100,
                  name: result[curKey].type
                }
                this.pieData.push(chart)
                let table = {
                  faultType: result[curKey].type,
                  possibility: result[curKey].possibility * 100,
                  suggest: result[curKey].suggest
                }
                this.tableData.push(table)
              }
            })

            this.tableData.sort((a, b) => b.possibility - a.possibility)
            
            let indexToRemove
            this.tableData.forEach((item, index) => {
              if (item.faultType === result[maxKey].type) {
                item.suggest = result[maxKey].suggest
                indexToRemove = index
              }
            })

            if (indexToRemove !== undefined) {
              const itemToMove = this.tableData.splice(indexToRemove, 1)[0]
              this.tableData.unshift(itemToMove)
            }

            this.showChart = true
            this.showTable = true
          }
        } else {
          this.$message.error('故障诊断失败！')
        }

        this.loading = false

      })
    },

  }
}
</script>

<style lang="scss" scoped>

.main {
  display: flex;
  flex-direction: row;
}

.left {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-content: center;
}

.right {
  height: 100vh;
  margin-left: 5px;
}

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

</style>
