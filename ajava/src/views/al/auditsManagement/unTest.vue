<template>
  <div>
    <el-table :data="tableData" style="width: 100%" :cell-style="{ textAlign: 'center' }"
              :header-cell-style="tableHeaderColor">
      <el-table-column label="算法/模型名称" width="380">
        <template #default="scope">
          <div>
            <span>{{ scope.row.alModelName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="算法/模型创建者" width="280">
        <template #default="scope">
          <div>
            <span>{{ scope.row.creator }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="算法/模型类型" width="300">
        <template #default="scope">
          <div>
            <span>{{ scope.row.alModelType }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="测试状态" width="280">
        <template #default="scope">
          <div>
            <el-switch inactive-text="否"
                       active-text="是"
                       :active-value="0"
                       :inactive-value="2"
                       v-model="scope.row.isPass"
                       @click="changeCheck1(scope.row)"/>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button class="viewBtn" size="small" @click="checkHandle(scope.row)"
          >查看
          </el-button>
          <el-button class="viewBtn" size="small"
                     @click="testHandle(scope.row); dialogVisible = true;"
          >测试
          </el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
        v-model="dialogVisible"
        width="25%"
        style="height: 250px"
        align-center
        class="no-header-dialog"
        @close="closeDialog()">
      <div slot="title" style="font-size: 24px;text-align:center;padding-bottom: 9%;">
        <span>测试结果</span>
      </div>
      <div v-loading="loading"
           element-loading-text="测试中..."
           element-loading-background="rgba(0, 0, 0, 0.3)">
        <div style="font-size: 15px;text-align:center;padding-bottom: 2%;">
          <span v-if="testResult">{{ testResult_process }}</span>
          <span v-else>{{ testResult }}</span>
        </div>
        <span slot="footer" style="display: flex;justify-content: center;margin-top: 50px" v-if="testResult">
          <el-button type="primary" class="normalBtn"
                     @click="dialogVisible = false; this.getDataList()">确 定</el-button>
        </span>
      </div>
    </el-dialog>
    <table-form v-if="checkVisible" ref="form" @refreshDataList="getDataList"></table-form>
<!--    <table-form1 v-if="checkVisible1" ref="form1" @refreshDataList="getDataList"></table-form1>-->
<!--    <table-form2 v-if="checkVisible2" ref="form2" @refreshDataList="getDataList"></table-form2>-->
  </div>
</template>

<script>
import {getNoTestPage, updateTestStatus} from "@/api/al/auditsManagement/auditsManagement"
import tableForm from './form.vue'
// import tableForm1 from './form1.vue'
// import tableForm2 from './form2.vue'
import {ElMessage} from "element-plus";
import "@/styles/button/resource.scss";
import {autoTest, getObj, startJob} from "@/api/al/altest/altest";

export default {
  name: "unAudits",
  components: {
    tableForm,
    // tableForm1,
    // tableForm2
  },
  data() {
    return {
      tableHeaderColor: {
        background: '#337ecc',
        color: 'white',
        fontSize: '14px',
        textAlign: 'center',
      },
      searchForm: {},
      tableData: [
        {},
      ],
      currentPage: 1,
      pageSize: 6,
      // checkVisible1: false,
      // checkVisible2: false,
      checkVisible: false,
      testState: '',
      testResult: '',
      loading: false,
      dialogVisible: false,
      resultInfo: '',
      testResult_process: {
        resultInfo: '',
        resultDetail: ''
      }
    }
  },
  mounted() {
    this.getDataList()
  },
  methods: {
    init(currentPage, pageSize, searchForm) {
      this.currentPage = currentPage
      this.pageSize = pageSize
      this.searchForm = searchForm
    },
    refresh(currentPage, pageSize, searchForm) {
      this.currentPage = currentPage
      this.pageSize = pageSize
      this.searchForm = searchForm
      this.getDataList()
    },
    onsearch(searchForm) {
      this.searchForm = searchForm
      this.getDataList()
    },
    getDataList() {
      getNoTestPage(Object.assign({
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }, this.searchForm)).then(response => {
        const {code, success, message, data} = response.data
        this.tableData = data.records
        this.$emit('getPage', data.current, data.total)
      })
    },
    //查看
    checkHandle(row) {
      // const AllType = ['数据去重', '缺失值填充', '数据标准化', '关联模式挖掘', '命名实体识别模型']
      // let modelFunction = row.modelFunction
      // if (AllType.includes(modelFunction)) {
      //   this.checkVisible1 = true
      //   this.$nextTick(() => {
      //     this.$refs.form1.init(row);
      //   })
      // } else {
      //   //这里加另外一个form的调用逻辑
      //   this.checkVisible2 = true
      //   this.$nextTick(() => {
      //     this.$refs.form2.init(row);
      //   })
      // }
      this.checkVisible = true
      this.$nextTick(() => {
        this.$refs.form.init(row);
      })
    },

    //测试
    testHandle(row) {
      this.dialogVisible = true
      this.loading = true
      this.testResult_process = {}
      /*调取微服务接口*/
      autoTest(row.programUrl).then(response => {
        let taskId = response.data.data[0]
        let alModelType = response.data.data[1]
        startJob(taskId, alModelType,"自动测试").then(response => {
          getObj(taskId).then(response => {
            this.testState = response.data.data.taskState
            this.loopResult(taskId, row)
          })
        })
      }).catch(() => {
        this.loading = false
      })
    },

    loopResult(taskId, row) {
      let i = 0;
      console.log("开始轮循请求");
      let timer = setInterval(() => {
        this.fun(timer, i++, taskId, row)
      }, 3000)
      return true
    },
    fun(timer, i, taskId, row) {
      setTimeout(() => {
        console.log("开始轮循请求：");
        console.log("次数：" + i);
        getObj(taskId).then(response => {
          this.testState = response.data.data.taskState
          if (this.testState !== 1) {
            this.loading = false
            this.testResult = JSON.parse(response.data.data.taskResult)
            if (this.testResult.includes("error")) {
              if (this.testResult.includes("File not found in MinIO")) {
                this.testResult_process.resultInfo = "测试未通过！未在MinIO中找到程序包！"
                this.testResult = JSON.parse(this.testResult)
                this.testResult_process.resultDetail = this.testResult.file
              } else if (this.testResult.includes("Failed to extract file, possibly algorithm file does not have the same name as the Zip file or no algorithm file")) {
                this.testResult_process.resultInfo = "测试未通过！提取算法文件失败，可能是算法文件名与程序包名不相同或没有算法文件！"
              } else if (this.testResult.includes("Output was empty or invalid.")) {
                this.testResult_process.resultInfo = "测试未通过！算法文件输出为空或无效！"
              } else if (this.testResult.includes("Script execution failed")) {
                this.testResult_process.resultInfo = "测试未通过！算法文件执行失败！"
                this.testResult = JSON.parse(this.testResult)
                // 提取[Errno 2]后面的文本
                let errnoIndex = this.testResult.details.indexOf('[Errno 2]');
                if (errnoIndex !== -1) {
                  // 加上 '[Errno 2]' 的长度来开始截取正确的位置
                  this.testResult_process.resultDetail = this.testResult.details.slice(errnoIndex + '[Errno 2]'.length).trim();
                } else {
                  // 如果没有找到[Errno 2]，就使用整个详情
                  this.testResult_process.resultDetail = this.testResult.details;
                }
              } else {
                this.testResult_process.resultInfo = "发生意外错误！"
              }

              updateTestStatus({
                alModelName: row.alModelName,
                isPass: 1,
              }).then(response => {
                ElMessage({
                  message: "测试不通过",
                  type: 'error'
                })
              })
            } else {
              if (this.testResult.includes("success")) {
                this.testResult = JSON.parse(this.testResult)
                this.testResult_process.resultDetail = this.testResult.data
                ElMessage({
                  message: "测试通过",
                  type: 'success'
                })
              } else {
                this.testResult_process.resultInfo = "测试通过！但输出不是有效的JSON语句！"
                this.testResult = JSON.parse(this.testResult)
                this.testResult_process.resultDetail = this.testResult.output
                ElMessage({
                  message: "测试警告",
                  type: 'warning'
                })
              }

              updateTestStatus({
                alModelName: row.alModelName,
                isPass: 0,
              }).then(response => {
              })
            }
            clearInterval(timer);

          }
        })
        if (i >= 40) {
          this.$message.warning('测试超时')
          this.loading = false
          clearInterval(timer);
        }
      }, 0)
    },

    //变更测试状态
    changeCheck1(row) {
      updateTestStatus({
        alModelName: row.alModelName,
        isPass: 0,
      }).then(response => {
        ElMessage({
          message: "测试通过",
          type: 'success'
        })
        this.getDataList()
      })

    },
    // 关闭对话框
    closeDialog() {
      clearTimeout(this.timer)
      this.loading = false
      this.testResult = ''
      this.testState = ''
    }
  }
}
</script>

<style scoped>
/*鼠标悬停的样式*/
/*::v-deep .el-table__row:hover {*/
/*  background: #337ecc !important;*/
/*}*/
/*选中行的样式*/
.el-table {
  --el-table-row-hover-bg-color: #E1EAF3 !important;
  --el-table-row-hover-text-color: white;
}

</style>
