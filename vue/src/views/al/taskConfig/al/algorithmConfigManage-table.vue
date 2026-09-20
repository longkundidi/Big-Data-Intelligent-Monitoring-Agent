<template>
  <div>
    <el-table :data="tableData" style="width: 100%" :header-cell-style="{background:'#f4f4f5','text-align':'center'}"
              highlight-current-row border>
      <el-table-column width="250" label="组态名称" prop="modelName" align="center">
        <template #default="scope">
          <span>{{ scope.row.modelName }}</span>
        </template>
      </el-table-column>
      <el-table-column width="100" label="针对对象" prop="modelObject" align="center"/>
      <el-table-column width="600" label="组态流程" prop="taskProcess" align="center">
        <template #default="scope">
          <div class="process-container">
            <template v-for="(process, index) in scope.row.taskProcess" :key="index">
              <span class="process-item" >
                {{ index + 1 }}. {{ process.almodelName }}
              </span>
              <span class="arrow" v-if="index < scope.row.taskProcess.length - 1"></span>
            </template>
          </div>
        </template>
      </el-table-column>
      <el-table-column width="150" label="是否启用" prop="isService" align="center">
        <template #default="scope">
          <el-tooltip
              :content="scope.row.isDeployed==1 ? '请先完成审核' : ''"
              placement="top"
              :disabled="scope.row.isDeployed!=1">
            <div style="display: inline-flex; align-items: center">
              <el-switch
                  v-model="scope.row.isService"
                  :active-value="0"
                  :inactive-value="1"
                  :disabled="scope.row.isDeployed==1"
                  @change="val => handleSwitchChange(scope.row, val)"
              />
              <span
                  v-if="scope.row.isDeployed==1"
                  style="margin-left: 8px; color: #f56c6c; font-size: 12px">请先审核</span>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column align="center">
        <template #header>
          操作
          <el-button type="primary" icon="el-icon-plus" @click="createTaskConfig" style="margin-left: 20px">新建组态<i
              class="el-icon--right"/></el-button>
          <el-button type="success" icon="el-icon-refresh" @click="reSet" style="margin-left: 20px">刷新<i
              class="el-icon--right"/></el-button>
        </template>
        <template #default="scope">
          <el-button size="small" icon="el-icon-edit" type="warning" @click="handleEdit(scope.row)">
            修改
          </el-button>
          <el-button size="small" type="success" icon="el-icon-view" @click="handleCheckGraph(scope.row)">
            查看流程
          </el-button>
          <el-button v-if="scope.row.isDeployed==0" size="small" type="primary" plain icon="el-icon-check" >
            已审核
          </el-button>
          <el-button v-else size="small" type="primary" icon="el-icon-check" @click="handleCheck(scope.row)">
            执行审核
          </el-button>
          <el-button v-if="scope.row.isPublished==0" size="small" type="primary" plain icon="el-icon-check" >
            已发布
          </el-button>
          <el-button v-else size="small" type="primary" icon="el-icon-check" @click="updateIspublishedConfig(scope.row)">
            发布
          </el-button>
          <el-button size="small" icon="el-icon-delete" type="danger" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 流程图 -->
    <el-dialog
        append-to-body
        :close-on-click-modal="false"
        v-model="graphVisible"
        class="no-header-dialog"
        width="40%"
        height="40%"
        title="组态流程"
        @close="graphVisible = false"
        :show-close="true"
    >
      <div style="padding: 20px;">
        <img :src="'/api/al/file/'+processImage" style="width: 100%; height: 100%; object-fit: contain;">
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="graphVisible = false">确定</el-button>
        <el-button @click="graphVisible = false">取消</el-button>
      </span>
    </el-dialog>

    <!--分页组件-->
    <div class="el-footer" style="height: 10%;float: right;padding-right: 2%;margin-top: 10px">
      <el-form :inline="true">
        <el-form-item>
          <el-pagination
              v-if="pageshow"
              class="wl-pagination"
              background
              layout="total, prev, pager, next"
              v-model:current-page="currentPage"
              :page-sizes="[6, 10, 15, 20, 50, 100, 150, 200]"
              v-model:page-size="pageSize"
              :total="count"
              @current-change="handleCurrentChange"
              @size-change="handleSizeChange"
          />
        </el-form-item>
      </el-form>
    </div>

  </div>
</template>

<script setup>
import {ref, onMounted,} from 'vue';
import {useRouter} from 'vue-router';
import {
  getAlConfig,
  checkAlConfig,
  updateAlIspublishedConfig,
  deleteAlConfig
} from "@/api/al/taskConfig";
import {ElMessage,ElMessageBox} from "element-plus";


const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const count = ref(0);
const graphVisible = ref(false);
const logPage = ref(1);
const addVisible = ref(false);
const pageshow = ref(true);
const processImage = ref('');

const router = useRouter();

const reSet = () => {
  getDataList()
};

const createTaskConfig = () => {
  router.push({
    path: '/al/taskConfig/al/algorithmConfig',
  }).catch(() => {
    ElMessage.error('页面未找到');
  });
};

const handleEdit = (row) => {
  addVisible.value = true;

};

const handleDelete =  (row) => {
  ElMessageBox.confirm("此操作将永久删除该任务, 是否继续?", "提示", {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteAlConfig({ code: row.code });
      if (res.data.code === '200') {
        await getDataList();
        ElMessage.success("删除成功"); // 修正：成功应该用success而非error
      }
    } catch (error) {
      ElMessage.error("删除失败: " + error.message); // 错误处理
    }
  }).catch(() => {
  // 用户点击取消的操作
    ElMessage.info("已取消删除");
  });
};

const handleCheckGraph = (config) => {
  graphVisible.value = true;
  processImage.value = config.modelIcon
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  getDataList();
};
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  getDataList();
};

const getDataList = () => {
  //获取所有组态
  getAlConfig({
    current: currentPage.value,
    size: pageSize.value
  }).then(res => {
    if (res.data.code === '200') {
      tableData.value = res.data.data.records;
      count.value = res.data.data.total;
    }
  });
};

onMounted(() => {
  getDataList();
});

let isCheck=ref(false)
const handleSwitchChange = async (row, newValue) => {
  try {
    if (row.isDeployed == 1) {
      ElMessage.warning("请先审核");
      // 立即恢复开关状态
      row.isService = row.isService === 0 ? 1 : 0;
      return;
    }
    const res = await checkAlConfig(row, { isCheck: false });

    if (res.data.code === '200') {
      ElMessage.success(newValue === 0 ? "组态已启用" : "组态已停用");
      // 确保本地数据更新
      row.isService = newValue;
    } else {
      // 操作失败时恢复状态
      row.isService = newValue === 0 ? 1 : 0;
    }
  } catch (error) {
    row.isService = row.isService === 0 ? 1 : 0;
    ElMessage.error("操作失败: " + error.message);
  } finally {
    await getDataList(); // 如果需要刷新数据
  }
};

const handleCheck = async (row) => {
  try {
    isCheck.value = true;
    const res = await checkAlConfig(row, { isCheck: isCheck.value });

    if (res.data.code === '200') {
      row.isDeployed = row.isDeployed === 1 ? 0 : 1; // 修复逻辑错误
      await getDataList(); // 确保数据刷新完成
      ElMessage.success("通过审核");
    }
  } catch (error) {
    ElMessage.error("审核失败: " + error.message);
  }
};
const updateIspublishedConfig = async (row) => {
  try {
    isCheck.value = true;
    const res = await updateAlIspublishedConfig(row);
    if (res.data.data.code === '200') {
      row.isPublished = row.isPublished === 1 ? 0 : 1; // 修复逻辑错误
      await getDataList(); // 确保数据刷新完成
      ElMessage.success("发布算法组态成功");
    }else if(res.data.data.code === '500'){
      ElMessage.warning(res.data.data.message);
    }
  } catch (error) {
    ElMessage.error( error.message);
  }
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.arrow {
  display: inline-block;
  width: 0;
  height: 0;
  border-left: 6px solid transparent; /* 透明左边 */
  border-right: 6px solid transparent; /* 透明右边 */
  border-top: 8px solid rgba(128, 128, 128, 0.3); /* 蓝色的顶边，用来形成箭头 */
  margin: 0 5px; /* 箭头和文字之间的间距 */
  transform: rotate(-90deg);
}

/* 确保流程项水平排列不换行 */
.process-container {
  display: flex;
  align-items: center;
  flex-wrap: nowrap; /* 禁止换行 */
  overflow-x: auto; /* 如果内容过多允许横向滚动 */
  white-space: nowrap; /* 防止文本换行 */

  mask-image: linear-gradient(
      to left,
      transparent 0%,
      black 20px,  /* 遮罩宽度 */
      black calc(100% - 20px),
      black 100%
  );
}

.process-item {
  margin: 0 4px;
  white-space: nowrap;
  color: #409eff;
}
</style>
