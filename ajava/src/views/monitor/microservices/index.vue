<template>
  <div class="fl-container"
       v-loading="vmLoading">

    <div class="area" v-if="vmVisible">
      <!-- 虚拟机状态显示 -->
      <div class="vm-total">
        <el-statistic
            title="虚拟机总数"
            :value="vmList.length"
        />
      </div>

      <div class="vm-status-container">
        <div v-for="(vm, index) in vmList" :key="vm.id" class="vm-icon-container" @click="toggleSelection(vm)" :class="{ selected: vm.isSelected }">
            <v-icon name="ri-computer-line" scale="4" :style="{ color: vm.isRunning ? '#2ECC70' : 'red' }"/>

          <div class="vm-info">
            <el-text class="vm-name mx-1" size="large">{{ vm.name }}</el-text>
            <el-text class="mx-1" type="primary" v-if="vm.isRunning">微服务运行个数: {{ vm.microservicesCount }}</el-text>
            <el-text class="mx-1" type="primary" v-if="vm.isRunning">内存剩余容量: {{ vm.freeMemory }} GB</el-text>
            <el-text class="mx-1" type="primary" v-if="vm.isRunning">CPU 占用率: {{ vm.cpuUsage }}%</el-text>
            <el-text class="mx-1" type="danger" v-if="!vm.isRunning">运行故障</el-text>
          </div>
        </div>
      </div>

      <!--搜索区域-->
      <div class="search-area">
        <el-form :model="queryform" :inline="true">
          <el-form-item>
            <el-input v-model="queryform.jobName" placeholder="算法名称(模糊查询)" class="wl-input"
                      @input="handleQuery(queryform.jobName)" >
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button class="searchBtn" @click="handleQuery(queryform.jobName)">查询</el-button>
            <el-button class="reSetBtn" @click="getDataList()">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="avue-crud">

      <el-table
          :data="dataList"
          border
          v-loading="loading"
      >
        <el-table-column
            prop="id"
            header-align="center"
            align="center"
            label="序号"
            width="60"
        >
        </el-table-column>

        <el-table-column
            prop="alName"
            header-align="center"
            align="center"
            label="执行算法"
        >
        </el-table-column>


        <el-table-column
            prop="useCase"
            header-align="center"
            align="center"
            label="请求场景"
        >
        </el-table-column>

        <el-table-column
            prop="alType"
            header-align="center"
            align="center"
            label="服务类型"
        >
        </el-table-column>

        <el-table-column
            prop="taskState"
            header-align="center"
            align="center"
            width="100px"
            label="服务状态"
        >
          <template #default="scope">
            <div class="change-icon">
              <el-tag v-if="scope.row.taskState === 0" type="warning">
                未执行
              </el-tag>
              <el-tag v-if="scope.row.taskState === 2" type="primary">
                执行成功
              </el-tag>
              <el-tag v-if="scope.row.taskState === 3 || scope.row.taskState === 1" type="danger">
                执行失败
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column
            prop="startTime"
            header-align="center"
            align="center"
            label="开始时间"
        >
        </el-table-column>
        <el-table-column
            prop="endTime"
            header-align="center"
            align="center"
            label="结束时间"
        >
        </el-table-column>

        <el-table-column
            prop="alNum"
            header-align="center"
            align="center"
            label="执行次数"
        >
        </el-table-column>

        <el-table-column
            prop="endTime"
            header-align="center"
            align="center"
            label="历史记录"
        >
          <template #default="scope">
          <el-button type="text" size="small" icon="el-icon-view" @click="seehistory(scope.row.alId,scope.row.alClass)">查看历史记录
          </el-button>
          </template>
        </el-table-column>



        <el-table-column
            header-align="center"
            align="center"
            label="操作"
        >
          <template #default="scope">
            <i style="margin-right: 5px" v-if="scope.row.isService===1"
               @click="startTask(scope.row)">
              <el-button link type="success" icon="el-icon-video-play">启用</el-button>
            </i>
            <i style="margin-right: 5px" v-if="scope.row.isService===0"
               @click="stopTask(scope.row)">
              <el-button link type="warning" icon="el-icon-switch-button">停用</el-button>
            </i>

            <el-button type="text" size="small" icon="el-icon-view" @click="seemsg(scope.row.taskMsg)">查看任务输入信息
            </el-button>

            <el-button v-if="scope.row.taskState ===2 " type="text" size="small"
                       icon="el-icon-zoom-in" @click="seemsg1(scope.row.taskResult)">执行结果
            </el-button>

            <el-button v-if="scope.row.taskState  === 3 || scope.row.taskState  === 1 " type="text" size="small"
                       icon="el-icon-zoom-in"  style="color: red" @click="seemsg1(scope.row.taskResult)">查看失败信息
            </el-button>
    <!--TODO 执行失败给以提示-->
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="avue-crud__pagination" style="float: right;padding-top: 1%">
      <el-pagination
          @size-change="sizeChangeHandle"
          @current-change="currentChangeHandle"
          :current-page="pageIndex"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          :total="totalPage"
          background
          layout="total, sizes, prev, pager, next, jumper"
      >
      </el-pagination>
    </div>
    <!-- 弹窗, 新增 / 修改 -->
    <table-form v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></table-form>
    <add v-if="addVisible" ref="add" @refreshDataList="getDataList"></add>
    <test-log v-if="testLogVisible" ref="testLog" @refreshDataList="getDataList"/>
    <el-dialog
        title="任务信息"
        v-model="dialogVisible"
        width="40%"
    >
      <JsonViewer :value=this.msg></JsonViewer>
    </el-dialog>

    <el-dialog
        title="执行结果"
        v-model="dialogVisible1"
        width="40%"
    >
      <json-viewer :value="this.result"></json-viewer>
    </el-dialog>

  </div>
</template>

<script>
import {fetchList,getVMlist, delObj, getByName, startJob, updateIsService} from '@/api/task-manage/taskManage'

import TableForm from './altask-form.vue'
import add from './altask-add.vue'
import JsonViewer from 'vue-json-viewer'
import testLog from './testLog.vue'
import DictTag from "components/DictTag/index.vue";
export default {
  data() {
    return {
      queryform: {
        jobName: ''
      },
      dataForm: {
        key: ''
      },
      testLogVisible:false,
      dataList: [],
      pageIndex: 1,
      pageSize: 5,
      totalPage: 0,
      addOrUpdateVisible: false,
      addVisible: false,
      dialogVisible: false,
      msg: {},
      result: '',
      dialogVisible1: false,
      loading: false,
      vmName:'219服务器',
      vmVisible: false,
      vmLoading: true,
      vmList:[
        {
          id: null,
          name: '',
          isRunning: false,
          microservicesCount: 0,
          freeMemory: 0,
          cpuUsage: 0,
          isSelected: false
        },
        {
          id: null,
          name: '',
          isRunning: false,
          microservicesCount: 0,
          freeMemory: 0,
          cpuUsage: 0,
          isSelected: false
        }
      ]
    }
  },
  components: {
    DictTag,
    TableForm,
    add,
    JsonViewer,
    testLog
  },
  mounted() {
    this.getDataList()
    this.getVMs()
    this.timer = setInterval(this.getVMs, 60 * 60 * 1000)
  },
  beforeDestroy() {
    // 清除定时任务
    clearInterval(this.timer)
  },

  methods: {

    set(id) {
      startJob(id).then(data => {
        this.$message.success('启动成功')
        this.getDataList()
      }).catch(() => {
      })
      // this.$confirm('手动启动任务?', {
      //   confirmButtonText: '确定',
      //   cancelButtonText: '取消',
      //   type: 'warning'
      // }).then(function() {
      //   return
      // }).then(data => {
      //   this.$message.success('启动成功')
      //   this.getDataList()
      // }).catch(() => {
      // })
    },

    indexMethod(index) {
      return index + 1
    },

    seemsg(taskMsg) {
      this.msg = JSON.parse(taskMsg)
      this.dialogVisible = true
    },

    seemsg1(taskResult) {
      this.result = JSON.parse(taskResult)
      this.dialogVisible1 = true
    },
    seehistory(alId,alClass){
      this.testLogVisible = true
      this.$nextTick(() => {
        this.$refs.testLog.init(alId,alClass)
      })
    },
    handleQuery(jobName) {
      let page={current: this.pageIndex,
        size: this.pageSize,}
      if(jobName===''){
      this.getDataList()
      }else{
        getByName(page,{ alModelName:jobName}).then(res=>{
          const {data}=res.data
          this.dataList =data.records
          this.totalPage = data.total
        })
      }

    },
    // 获取数据列表
    getDataList() {
      this.loading = true
      fetchList(Object.assign({
        current: this.pageIndex,
        size: this.pageSize,
        serverName: this.vmName
      })).then(response => {
        this.dataList = response.data.data.records
        this.totalPage = response.data.data.total
      }).catch(err => {
        console.log(err.msg)
      }).finally(() => {
        this.loading = false
      })
      // return new Promise((resolve, reject) =>
      //  )
    },

    // 每页数
    sizeChangeHandle(val) {
      this.pageSize = val
      this.pageIndex = 1
      this.getDataList()
    },
    // 当前页
    currentChangeHandle(val) {
      this.pageIndex = val
      this.getDataList()
    },
    // 新增 / 修改
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    },

    add() {
      this.addVisible = true
      this.$nextTick(() => {
        this.$refs.add.init()
      })
    },

    // 删除
    deleteHandle(id) {
      this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        return delObj(id)
      }).then(data => {
        this.$message.success('删除成功')
        this.getDataList()
      }).catch(() => {
      })
    },
    startTask(altaskVo){
      updateIsService(altaskVo).then(res=>{
        if(res.data.code==='200'){
          this.$message.success('开启任务成功')
        }else{
          this.$message.success('开启任务失败')
        }
        this.getDataList()
      })

    },
    stopTask(altaskVo){
      updateIsService(altaskVo).then(res=>{
          if(res.data.code==='200'){
            this.$message.success('关闭任务成功')
          }else{
            this.$message.success('关闭任务失败')
          }
          this.getDataList()
      })
    },
    getVMs(){
      getVMlist().then(res=>{
        this.vmList=res.data.data

        // const targetVM = this.vmList.find(vm => vm.name === '219服务器')
        // if (targetVM) {
        //   targetVM.isSelected = true
        // }

        if (this.vmName) {
          const selectedVm = this.vmList.find(vm => vm.name === this.vmName)
          selectedVm.isSelected = true
        }

        this.vmVisible = true
        this.vmLoading = false
      })
    },

    toggleSelection(selectedVm) {
      if (!selectedVm.isRunning) {
        this.$message.error('该虚拟机未正常运行，无法选择！');
        return
      }

      this.vmList.forEach(vm => {
        vm.isSelected = false
      })
      selectedVm.isSelected = true
      this.vmName = selectedVm.name
      this.pageIndex = 1
      this.pageSize = 5
      this.getDataList()
    },

  }
}
</script>

<style scoped>
.fl-container {
  display: flex;
  flex-direction: column;
}

.area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1em;
}

.change-icon {
  font-size: 26px;
}


.avue-crud {
  padding-left: 1%;
  padding-right: 1%;
}

.search-area {
  justify-content: flex-end; /* 将搜索区域对齐到最右边 */
  display: flex;
}

.vm-total {
  margin-right: 20px;
  text-align: center;
  padding: 10px; /* 内边距，可根据需要调整 */
  border: 1px solid #ccc; /* 边框 */
  border-radius: 8px; /* 圆角 */
}

.vm-status-container {
  display: flex;
  gap: 3em; /* 两个虚拟机状态之间的间距 */
  flex: 1; /* 使虚拟机状态显示区域占据剩余空间 */
  /*justify-content: center; !* 使虚拟机状态显示区域居中 *!*/
}

.vm-info {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.vm-info .vm-name {
  font-size: 1.1em;
  font-weight: bold;
  margin-bottom: 0.2em;
}

.vm-info .el-text {
  display: block;
  margin: 0.1em 0;
  justify-content: flex-start;
}


.vm-icon-container {
  display: flex;
  align-items: center;
  margin-bottom: 8px; /* 可根据需要调整间距 */
  cursor: pointer; /* 鼠标指针变为手型 */
  transition: background-color 0.3s ease; /* 平滑过渡效果 */
  padding: 10px; /* 内边距，可根据需要调整 */
  border: 1px solid #ccc; /* 边框 */
  border-radius: 8px; /* 圆角 */
}

.vm-icon-container:hover {
  background-color: #f0f0f0; /* 鼠标悬停时的背景颜色 */
}

.vm-icon-container.selected {
  background-color: #e0e0e0; /* 选中时的背景颜色 */
}

.vm-icon-container span {
  margin-bottom: 4px; /* 可根据需要调整间距 */
}

.vm-icon-container .v-icon {
  font-size: 48px; /* 图标大小，可以根据需要调整 */
}

</style>
