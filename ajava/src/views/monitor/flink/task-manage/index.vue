<template>
  <div v-loading="state.loading" class="container">
    <!--一、数据预览-->
    <el-row class="dataOverview" justify="space-between" :gutter="15" style="margin:10px 8px">
      <el-col :span="16">
        <el-card body-style="display: flex;justify-content: space-between;align-items: flex-start;" style="height: 300px;">
          <div style="flex: 1; margin-right: 15px;">
            <h2 class="text-lg font-bold">算法及任务总数</h2>
            <div class="bg-gray-200 p-4">
              <p style="font-size: 32px; color: #00ff00;">{{ totalTaskAndAlgorithm }}</p>
              <p>正在运行的算法及任务数：<span  style=" color: #00ff00;">{{state.runningJobs}}</span> | Flink集群总数：<span  style=" color: #00ff00;"> {{ state.taskManagers }}</span></p>
            </div>
          </div>

          <div style="flex: 1; text-align: center;">
           <div class="bg-gray-200 p-4 text-center">
             <chart :chart-options="state.cpuChartOptions" style="width: 100%; height: 280px;" />
           </div>
          </div>

          <div style="flex: 1; text-align: center;">
            <div class="bg-gray-200 p-4 text-center">
              <chart :chart-options="state.memoryChartOptions" style="width: 100%; height: 280px;"/>
            </div>
          </div>


        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card style="height: 300px;">
          <h2 class="text-lg font-bold">算法及任务执行总次数</h2>
          <div class="bg-gray-200 p-4">
            <p style="font-size: 32px; color: #00ff00;">{{ state.jobTotal }}</p>
            <p>任务历史失败次数：<span  style=" color: #00ff00;">{{state.jobFail}}</span></p>
          </div>
        </el-card>
      </el-col>
    </el-row>


    <el-row :span="24" type="flex" justify="space-between" style="margin:0 15px">
      <el-col :span="24" class="flex justify-center">
        <el-card>
          <!--tabbar-->
          <el-tabs type="border-card" v-model="state.activeName" @tab-change="handleClick">
            <el-tab-pane label="流数据监听任务列表" name="first"></el-tab-pane>
            <el-tab-pane label="模型-算法执行任务列表" name="second"></el-tab-pane>
          </el-tabs>
          <!-- 查询与新增 -->
          <div v-if="state.taskVisible" style="margin-top: 15px">
            <!--查询区域-->
            <el-form ref="state.queryform" :model="state.queryform" :inline="true">
              <el-form-item>
                <el-input v-if="state.queryform.jobId!==null" v-model="state.queryform.jobId" placeholder="Flink任务Id" class="wl-input" @input="searchTasks('',queryform.jobId,'')"/>
              </el-form-item>
              <el-form-item>
                <el-select v-model="state.queryform.jobType" placeholder="任务类型" class="wl-choose" clearable
                           @change="searchTasks('','',state.queryform.jobType)">
                  <el-option label="SQL流任务" value="0"/>
                  <el-option label="SQL批任务" value="2"/>
                  <el-option label="JAR包" value="1"/>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-input v-model="state.queryform.jobName" placeholder="任务名称(模糊查询)" class="wl-input"
                          @input="searchTasks(state.queryform.jobName,'','')">
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="searchTasks(state.queryform.jobName,state.queryform.jobId,state.queryform.jobType)">查询</el-button>
              </el-form-item>

              <el-form-item>
                <el-dropdown trigger="click">
                  <el-button type="primary" icon="el-icon-plus">
                    新建任务<i class="el-icon-arrow-down el-icon--right"></i>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="navigateTo('create',{jobTypeEnum: 'SQL_STREAMING'})">
                        <i class="iconfont my-icon-jiediansql"/> SQL流任务
                      </el-dropdown-item>
                      <el-dropdown-item @click="navigateTo('create',{jobTypeEnum: 'SQL_BATCH'})"><i class="iconfont my-icon-file-SQL"/>SQL批任务
                      </el-dropdown-item>

                      <el-dropdown-item @click="navigateTo('create',{jobTypeEnum: 'JAR'})"><i class="iconfont my-icon-suffix-jar"/>JAR任务
                      </el-dropdown-item>

                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-form-item>

              <el-form-item style="margin-left: 10px">
                <el-button class="reSetBtn" @click="fetchTasks();fetchJobsCount()">刷新</el-button>
              </el-form-item>

            </el-form>
            <!--flink任务列表    -->
            <el-table :data="state.taskList" :header-cell-style="{background:'#f4f4f5','text-align':'center'}"
                      class="wl-table" border>
              <el-table-column prop="id" :show-overflow-tooltip="true" label="任务id及类型" min-width="150"
                               align="center"
                               fixed>
                <template #default="scope">
                  <span style="margin-right:5px;">{{ scope.row.id }}</span>
                  <el-tooltip class="item" effect="dark" :content="getTaskTypeName(scope.row.jobTypeEnum)"
                              placement="right">
                    <i v-if="scope.row.jobTypeEnum==='SQL_STREAMING'" class="iconfont my-icon-jiediansql"
                       style="font-size:16px;"/>
                    <i v-if="scope.row.jobTypeEnum==='SQL_BATCH'" class="iconfont my-icon-file-SQL"
                       style="font-size:16px;"/>
                    <i v-if="scope.row.jobTypeEnum==='JAR'" class="iconfont my-icon-suffix-jar"
                       style="font-size:16px;"/>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column prop="jobName" label="任务名称" min-width="220"
                               align="center" fixed>
              </el-table-column>
              <el-table-column prop="jobDesc" :show-overflow-tooltip="true" label="任务描述" min-width="120"
                               align="center">
              </el-table-column>
              <el-table-column prop="useCase" :show-overflow-tooltip="true" label="任务请求场景" min-width="120"
                               align="center">
              </el-table-column>
              <el-table-column prop="status" label="状态" min-width="90" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.status===-2||scope.row.status==='UNKNOWN'" type="info" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-else-if="scope.row.status===-1||scope.row.status==='FAIL'" type="danger" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-else-if="scope.row.status===0||scope.row.status==='STOP'" type="warning" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-else-if="scope.row.status===1||scope.row.status==='RUN'" type="success" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-else-if="scope.row.status===2||scope.row.status==='STARTING'" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-else-if="scope.row.status===3||scope.row.status==='SUCCESS'" type="success" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-else type="info" size="mini">{{ getStatusDesc(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="日志" min-width="115" align="center">
                <template #default="scope">
                  <i v-if="scope.row.lastRunLogId!==null" @click="navigateToRunlog(scope.row)">
                    <el-button type="text" icon="el-icon-message">详情</el-button>
                  </i>
                </template>
              </el-table-column>

              <el-table-column prop="jobFreq" :show-overflow-tooltip="true" label="计算频次(次/秒)" min-width="120"
                               align="center">
              </el-table-column>
              <el-table-column prop="jobTotal" :show-overflow-tooltip="true" label="执行总次数" min-width="120"
                               align="center">
              </el-table-column>
              <el-table-column prop="jobFail" :show-overflow-tooltip="true" label="执行失败次数" min-width="120"
                               align="center">
              </el-table-column>
              <el-table-column prop="editTime" :show-overflow-tooltip="true" label="最后提交时间" min-width="100"
                               align="center">
                <template #default="scope">
                  <span>{{ formatDateTime(scope.row.editTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="operate" label="操作" min-width="210" fixed="right">
                <template #default="scope">
                  <i style="margin-right: 5px" v-if="scope.row.status!=='RUN'"
                     @click="startTasks(scope.row)">
                    <el-button link type="success" icon="el-icon-video-play">启动</el-button>
                  </i>
                  <i style="margin-right: 5px" v-if="scope.row.status==='RUN'"
                     @click="stopTasks(scope.row)">
                    <el-button link type="warning" icon="el-icon-switch-button">停止</el-button>
                  </i>
                  <i style="margin-right: 5px" @click="navigateTo('view',scope.row)">
                    <el-button link type="primary" icon="el-icon-view">查看</el-button>
                  </i>
                  <i v-if="scope.row.status!=='RUN'" style="margin-right: 5px"
                     @click="navigateTo('update',scope.row)">
                    <el-button type="text" icon="el-icon-edit">修改</el-button>
                  </i>
                  <i style="margin-right: 5px"
                     @click="copyConfigs(scope.row)">
                    <el-button link type="primary" icon="el-icon-document-copy">复制</el-button>
                  </i>
                  <i style="margin-right: 5px" v-if="scope.row.status!=='RUN'"
                     @click="deleteTasks(scope.row)">
                    <el-button link type="danger" icon="el-icon-delete">删除</el-button>
                  </i>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- flink算法列表 -->
          <div v-if="state.algorithmVisible" style="margin-top: 15px">
            <!--查询区域-->
            <el-form ref="state.queryform" :model="state.queryform" :inline="true">
              <el-form-item>
                <el-input v-model="state.queryform.jobId" placeholder="Flink任务Id" class="wl-input" @input="searchAlgorithms(state.queryform.jobId,'')"/>
              </el-form-item>
              <el-form-item>
                <el-input v-model="state.queryform.jobName" placeholder="算法名称(模糊查询)" class="wl-input"
                          @input="searchAlgorithms('',state.queryform.jobName)">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="searchAlgorithms(queryform.jobId,queryform.jobName)">查询</el-button>
              </el-form-item>
            </el-form>
            <el-table :data="state.algorithmList"
                      :header-cell-style="{background:'#f4f4f5', 'text-align':'center'}" class="wl-table"
                      border>
              <el-table-column prop="id" :show-overflow-tooltip="true" label="算法id" min-width="70"
                               align="center"
                               fixed>
              </el-table-column>
              <el-table-column prop="jobName" label="算法名称" min-width="220"
                               align="center" fixed/>
              <el-table-column prop="startTime" label="算法开启时间" min-width="120"
                               align="center">
                <template #default="scope">
                  <span>{{ formatDateTime(scope.row.startTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="算法运行时长" min-width="120"
                               align="center">
                <template #default="scope">
                  <span>{{ scope.row.duration }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="运行状态" min-width="90" align="center">
                <template #default="scope">
                  <el-tag v-if="scope.row.status==='CANCELED'" type="warning" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-if="scope.row.status==='RUNNING'" type="success" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-if="scope.row.status==='FAILED'" type="error" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                  <el-tag v-if="scope.row.status==='FINISHED'" type="info" size="mini">
                    {{ getStatusDesc(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="flowchart" label="流程图" min-width="150" align="center">
                <template #default="scope">
                <el-button type="text" @click="initChart(scope.row)">查看流程图</el-button>
                </template>
              </el-table-column>
              <el-table-column prop="inputTopic" label="输入topic" min-width="150"
                               align="center">
                <template #default="scope">
                  <span >{{ scope.row.inputTopic }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="outputTopic" label="输出topic" min-width="200"
                               align="center">
                <template #default="scope">
                  <span>{{ scope.row.outputTopic }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

        </el-card>
      </el-col>
    </el-row>

    <el-pagination
        v-if="state.pageshow"
        class="wl-pagination"
        background
        layout="total, sizes, prev, pager, next"
        :current-page="state.currentPage"
        :page-sizes="[5, 10, 20, 50, 100]"
        :page-size="state.pageSize"
        :total="state.count"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />

    <el-dialog
        v-model="dialogVisible"
        :title="查看流程图"
        center
        width="50%"
        height="40%"
        @close="closeDialog"
        >
      <flowchart v-if="flowChartVisible" :initialNodeLabel="initialNodeLabel"/>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated , watch ,onBeforeUnmount} from 'vue';
import { useRouter, useRoute,onBeforeRouteLeave } from 'vue-router';
import { getTasks, getAlAlgorithms, startTask, stopTask, copyConfig, deleteTask, getJobsCount } from '/src/api/task';
import { containerStats } from '@/api/task-manage/taskManage';
import chart from './chart.vue';
import flowchart from './flowChart.vue';

const router = useRouter();
const route = useRoute();

// 控制对话框显示状态
let flowChartVisible = ref(false);
let dialogVisible=ref(false);
// 定义要传递给子组件的初始节点标签
let initialNodeLabel = reactive({
  node1: "Initial Node 1",
  node2: "Initial Node 2",
  node3: "Initial Node 3",
});

// 其他响应式数据
const state = reactive({
  loading: false,
  tasks: 0,
  algorithms: 0,
  taskManagers: 3,
  runningJobs: 0,
  canceledJobs: 0,
  queryform: {
    jobId: '',
    jobName: '',
    jobType: '',
    open: '',
    status: ''
  },
  activeName: 'first',
  taskVisible: true,
  algorithmVisible: false,
  editVisible: false,
  editForm: {
    topicName: ''
  },
  editJobId: '',
  jobTotal: 421231,
  jobFail: 725,
  creator: '李恒',
  dataRule: {
    topicName: [
      { required: true, message: '算法topic名称不能为空', trigger: 'blur' }
    ]
  },
  count: 0,
  pageSize: 5,
  currentPage: 1,
  pageshow: true,
  algorithmlist:[],
  taskList:[],
  cpuChartData: [],
  memoryChartData: [],
  cpuTotal: null,
  memoryTotal: null,
  fetchInterval: null,
  cpuChartOptions: {
    title: {
      text: 'CPU Usage (0.00%)',
      subtext: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'CPU Usage',
        type: 'line',
        symbol: 'none',
        itemStyle: {
          color: 'rgba(134, 188, 220, 1)'
        },
        areaStyle: {
          color: 'rgba(169, 206, 230, 0.5)'
        },
        data: []
      }
    ]
  },
  memoryChartOptions: {
    title: {
      text: 'Memory Usage (0.00%)',
      subtext: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'Memory Usage',
        type: 'line',
        symbol: 'none',
        itemStyle: {
          color: 'rgba(134, 188, 220, 1)'
        },
        areaStyle: {
          color: 'rgba(169, 206, 230, 0.5)'
        },
        data: []
      }
    ]
  }
});

// 计算属性
const totalTaskAndAlgorithm = computed(() => state.tasks + state.algorithms);

// 生命周期钩子
onMounted(() => {
  fetchJobsCount();
  fetchTasks();
  getAlAlgorithmList(state.currentPage, state.pageSize, '', '');
  fetchUsageData();
  startFetchInterval();

});

onActivated(() => {
  if (route.query.context) {
    const context = JSON.parse(route.query.context);
    if (context.currentPage) { // 恢复分页状态
      state.count = context.count;
      state.currentPage = context.currentPage;
      state.pageSize = context.pageSize;
    }

    fetchUsageData();
    startFetchInterval();
  }

  fetchJobsCount();
  fetchTasks();
});

onBeforeRouteLeave((to, from, next) => {
  stopFetchInterval();
  next();
});

// 方法
function initChart(row) {
  dialogVisible.value=true
  if (!row || !row.inputTopic || !row.jobName || !row.outputTopic) {
    console.error('Row object is missing required properties');
    return;
  }
  initialNodeLabel.node1 = row.inputTopic;
  initialNodeLabel.node2 = row.jobName;
  initialNodeLabel.node3 = row.outputTopic;
  flowChartVisible.value = true;

}
function closeDialog() {
  dialogVisible.value=false;
  initialNodeLabel.node1 =null;
  initialNodeLabel.node2 =null;
  initialNodeLabel.node3 = null;
  flowChartVisible.value = false;
}
function navigateTo(flag, row) {
  router.push({
    path: getRouteTaskPath(flag, row.jobTypeEnum),
    query: {
      flag: flag,
      context: JSON.stringify(queryContent()),
      data: JSON.stringify(row)
    }
  });
}

function navigateToRunlog(row) {
  router.push({
    path: '/monitor/flink/log-manage/logdetail',
    query: {
      flag: 'tasklist',
      context: JSON.stringify(queryContent()),
      data: JSON.stringify(row)
    }
  });
}

// 数据预览
function fetchJobsCount() {
  getJobsCount().then(res => {
    const { data } = res.data;
    state.taskManagers = data.taskManagers;
    state.runningJobs = data.running;
    state.canceledJobs = data.canceled > 0 ? data.canceled : 0;
  });
}

function handleClick(tab, event) {
  if (state.activeName === 'first') {
    state.algorithmVisible = false;
    state.taskVisible = true;
    fetchTasks();
    fetchJobsCount();
  }
  if (state.activeName === 'second') {
    state.taskVisible = false;
    state.algorithmVisible = true;
    getAlAlgorithmList(state.currentPage, state.pageSize, '', '');
    fetchJobsCount();
  }
}

function queryContent() {
  return {
    count: state.count,
    currentPage: state.currentPage,
    pageSize: state.pageSize,
    jobId: state.queryform.jobId,
    jobName: state.queryform.jobName,
    jobType: state.queryform.jobType,
    open: state.queryform.open,
    status: state.queryform.status
  };
}

function getAlAlgorithmList(currentPage, pageSize, jobId, jobName) {
  getAlAlgorithms({
    currentPage: currentPage,
    pageSize: pageSize,
    jobId: jobId,
    jobName: jobName
  }).then(res => {
    const { data } = res.data;
    state.algorithmList = data.records;
    state.count = data.total;
    state.algorithms = state.count;
    console.log('算法的个数', state.algorithms);
  });
}

function searchTasks(jobName, jobId, jobType) {
  fetchTasks();
}

function searchAlgorithms(jobId, jobName) { // 查询算法列表
  state.pageshow = false;
  getAlAlgorithmList(state.currentPage, state.pageSize, jobId, jobName);
  state.$nextTick(() => {
    state.pageshow = true;
  }); // 解决界面页码不更新问题
}

function handleSizeChange(pageSize) { // 设置分页大小事件
  state.pageSize = pageSize;
  if (state.activeName === 'first') {
    fetchTasks();
  }
  if (state.activeName === 'second') {
    getAlAlgorithmList(state.currentPage, state.pageSize, '', '');
  }
}

function handleCurrentChange(pageno) { // 处理分页事件
  state.currentPage = pageno;
  if (state.activeName === 'first') {
    fetchTasks();
  }
  if (state.activeName === 'second') {
    getAlAlgorithmList(state.currentPage, state.pageSize, '', '');
  }
}

// 转换字符串状态为整数值
function convertStatusToInteger(statusStr) {
  const statusMap = {
    '1:RUNNING': 1,
    '0: STOP': 0,
    '-1:FAILED': -1,
  };
  return statusMap[statusStr];
}

function fetchTasks() { // 查询任务列表
  state.loading = true;
  const jobName = state.queryform.jobName.trim();
  const { jobId, jobType, status, open } = state.queryform;
  getTasks(state.currentPage, state.pageSize, jobName, jobId, jobType, convertStatusToInteger(status), open).then(response => {
    state.loading = false;
    const { code, success, message, data } = response.data;
    if (code !== '200' || !success) {
      this.$message({ type: 'error', message: (message || '请求数据异常！') });
      return;
    }
    state.taskList = data.data;
    state.count = data.total;
    state.tasks = state.count;
    console.log('task的个数', state.tasks);
  }).catch(error => {
    state.loading = false;
    this.$message({ type: 'error', message: '请求异常！' });
    console.log(error);
  });
}

function startTasks(row) { // 启动任务
    state.loading = true;
    const { id, jobName } = row;

    startTask(id).then(response => {
      state.loading = false;
      const { code, success, message, data } = response.data;
      if (code !== '200' || !success) {
        this.$message({ type: 'error', message: (message || '请求数据异常！') });
        return;
      }
      fetchTasks();
      this.$message({ type: 'info', message: `正在启动[${jobName}]，稍后请刷新！` });
    }).catch(error => {
      state.loading = false;
      this.$message({ type: 'error', message: '请求异常！' });
      console.log(error);
    });
}
function  stopTasks(row) { // 停止任务
      const {id, jobName} = row
      this.$confirm(`确定要停止任务【${jobName}】吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
         state.loading = true
        stopTask(id).then(response => {
           state.loading = false
          const {code, success, message, data} = response.data
          if (code !== '200' || !success) {
            this.$message({type: 'error', message: (message || '请求数据异常！')})
            return
          }
           fetchTasks()
          this.$message({type: 'success', message: `正在停止任务【${jobName}】，稍后请刷新！`})
        }).catch(error => {
           state.loading = false
          this.$message({type: 'error', message: '请求异常！'})
          console.log(error)
        })
      })
    }
function  copyConfigs(row) { // 复制
      const {id, jobName} = row
      state.loading = true
      copyConfig(id).then(response => {
        state.loading = false
        const {code, success, message, data} = response.data
        if (code !== '200' || !success) {
          this.$message({type: 'error', message: (message || '请求数据异常！')})
          return
        }
        fetchTasks()
        this.$message({type: 'success', message: `复制[${jobName}]成功！`})
      }).catch(error => {
        state.loading = false
        this.$message({type: 'error', message: '请求异常！'})
        console.log(error)
      })
    }
function  deleteTasks(row) { // 删除
      const {id, jobName} = row
      this.$confirm(`确定要删除[${jobName}]吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        state.loading = true
        deleteTask(id).then(response => {
          state.loading = false
          const {code, success, message, data} = response.data
          if (code !== '200' || !success) {
            this.$message({type: 'error', message: (message || '请求数据异常！')})
            return
          }
          fetchTasks()
          this.$message({type: 'success', message: `删除[${jobName}]成功！`})
        }).catch(error => {
          state.loading = false
          this.$message({type: 'error', message: '请求异常！'})
          console.log(error)
        })
      })
    }

// 定义方法
function formatDateTime(date) {
  const date1 = new Date(date);
  const year = date1.getUTCFullYear();
  const month = String(date1.getUTCMonth() + 1).padStart(2, '0');
  const day = String(date1.getUTCDate()).padStart(2, '0');
  const hours = String(date1.getUTCHours()).padStart(2, '0');
  const minutes = String(date1.getUTCMinutes()).padStart(2, '0');
  const seconds = String(date1.getUTCSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

function getStatusDesc(status) {
  switch (status) {
    case -2:
    case 'UNKNOWN':
      return '未知';
    case -1:
    case 'FAIL':
    case 'FAILED':
      return '运行失败';
    case 0:
    case 'STOP':
      return '已停止';
    case 1:
    case 'RUN':
    case 'RUNNING':
      return '运行中';
    case 2:
    case 'STARTING':
      return '启动中';
    case 3:
    case 'SUCCESS':
      return '提交成功';
    case 'FINISHED':
      return '已完成';
    case 'CANCELED':
      return '已取消运行';
    default:
      return '';
  }
}

function getTaskTypeName(tasktype) {
  switch (tasktype) {
    case 'SQL_STREAMING':
      return 'SQL流任务';
    case 'SQL_BATCH':
      return 'SQL批任务';
    case 'JAR':
      return 'JAR包';
    default:
      return tasktype;
  }
}

function getRouteTaskPath(flag, jobTypeEnum) {
  switch (flag) {
    case 'create':
      switch (jobTypeEnum) {
        case 'SQL_STREAMING':
          return '/monitor/flink/task-manage/create_sql_streaming_task';
        case 'SQL_BATCH':
          return '/monitor/flink/task-manage/create_sql_batch_task';
        case 'JAR':
          return '/monitor/flink/task-manage/create_jar_task';
        default:
          return '';
      }
    case 'update':
      switch (jobTypeEnum) {
        case 'SQL_STREAMING':
          return '/monitor/flink/task-manage/edit_sql_streaming_task';
        case 'SQL_BATCH':
          return '/monitor/flink/task-manage/edit_sql_batch_task';
        case 'JAR':
          return '/monitor/flink/task-manage/edit_jar_task';
        default:
          return '';
      }
    case 'view':
      switch (jobTypeEnum) {
        case 'SQL_STREAMING':
          return '/monitor/flink/task-manage/view_sql_streaming_task';
        case 'SQL_BATCH':
          return '/monitor/flink/task-manage/view_sql_batch_task';
        case 'JAR':
          return '/monitor/flink/task-manage/view_jar_task';
        default:
          return '';
      }
    default:
      return '';
  }
}

async function fetchUsageData() {
  try {
    const response = await containerStats({ serverName: "219服务器", alClass: "flink" });
    const { code, data, message, success } = response.data;
    if (code !== '200' || !success) {
      this.$message({ type: 'error', message: message || '请求数据异常！' });
    } else {
      const { cpuPercent, cpuTotal, memoryPercent, memoryTotal } = data;
      let now = new Date().toLocaleTimeString();

      // 更新 CPU 数据
      state.cpuChartData.push({ time: now, value: cpuPercent });
      state.cpuChartOptions.xAxis.data = state.cpuChartData.map(item => item.time);
      state.cpuChartOptions.series[0].data = state.cpuChartData.map(item => item.value);
      // 计算最大 CPU 使用率
      const maxCpuPercent = Math.max(...state.cpuChartData.map(item => item.value));
      state.cpuChartOptions.title.text = `CPU Usage (${maxCpuPercent.toFixed(2)}%)`;

      state.cpuTotal = cpuTotal;
      state.cpuChartOptions.title.subtext = `CPU核心数: ${cpuTotal}个`;

      // 更新内存数据
      state.memoryChartData.push({ time: now, value: memoryPercent });
      state.memoryChartOptions.title.text = `Memory Usage (${parseFloat(memoryPercent.toFixed(2))}%)`;
      state.memoryChartOptions.xAxis.data = state.memoryChartData.map(item => item.time);
      state.memoryChartOptions.series[0].data = state.memoryChartData.map(item => item.value);

      state.memoryTotal = memoryTotal;
      state.memoryChartOptions.title.subtext = `内存总容量: ${memoryTotal}`;

      // 限制数据集大小，以保持性能
      if (state.cpuChartData.length > 60) {
        state.cpuChartData.shift();
      }
      if (state.memoryChartData.length > 60) {
        state.memoryChartData.shift();
      }
    }
  } catch (error) {
    this.$message({ type: 'error', message: '请求数据异常！' });
    console.error('Error fetching usage data:', error);
  }
}

let fetchInterval = null;

function startFetchInterval() {
  if (!fetchInterval) {
    fetchInterval = setInterval(fetchUsageData, 1000);
  }
}

function stopFetchInterval() {
  if (fetchInterval) {
    clearInterval(fetchInterval);
    fetchInterval = null;
  }
}

// 生命周期钩子
onMounted(() => {
  startFetchInterval();
});

onBeforeUnmount(() => {
  stopFetchInterval();
});



</script>

<style scoped>
@import url('/src/assets/iconfont/iconfont.css');


.container {
  position: relative; /* 添加相对定位 */
  padding-bottom: 60px; /* 预留分页器的高度 */

}

.wl-pagination {
  position: absolute; /* 绝对定位 */
  bottom: 0; /* 距离底部为 0 */
  right: 0; /* 距离右侧为 0 */
}

.dataOverview {
  display: flex;
}

.wl-table {
  width: 100%;
  margin-top: 0px;
}

.wl-icon {
  margin-right: 2px;
}

.wl-operate {
  overflow: hidden;
}

.wl-input {
  width: 200px;
}

.wl-choose {
  width: 200px;
}

.wl-form-input {
  width: 360px;
}

.wl-search {
  background: #f5f7fa;
}

.fl-version-text {
  /*display: inline;
  white-space: nowrap;
  text-overflow: ellipsis;*/
}

.fl-version-span {
  color: #337ab7;
  text-decoration: underline;
}

.wl-search >>> .el-button {
  background: #f00;
}

.wl-deploy-edit {
  padding-left: 10px;
}

.wl-deploy-to {
  padding-left: 8px;
}

.wl-delete {
  color: #f56c6c;
}

.wl-pagination {
  margin-top: 5px;
}

.wl-myicon {
  font-size: 14px;
}

.wl-table-icon >>> span {
  margin-left: 2px;
}

.wl-radio-group >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background-color: #f5f7fa;
  border-color: #DCDFE6;
  -webkit-box-shadow: -1px 0 0 0 #DCDFE6;
  box-shadow: -1px 0 0 0 #DCDFE6;
}

.wl-radio-group >>> .el-radio-button__inner {
  font-size: 14px;
  color: #606266;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  font-size: 12px;
}

.el-icon-arrow-down {
  font-size: 12px;
}

.wl-table >>> .el-link [class*=el-icon-] + span {
  margin-left: 1px;
}

.wl-table >>> .el-link {
  margin-right: 2px;
  margin-left: 2px;
}


.center-links {
  display: flex;
  justify-content: center;
}

::v-deep .el-tabs--border-card > .el-tabs__content {
  padding: 0 !important;

}




</style>
