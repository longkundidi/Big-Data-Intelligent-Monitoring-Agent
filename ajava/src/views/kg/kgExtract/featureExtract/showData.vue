<template>
  <div>
    <basic-container>
      <div class="extra">
        <el-container>
          <el-header class="title">
            知识获取管理->非结构化数据抽取
          </el-header>
          <Steps v-model:activeChange="active" class="steps"></Steps>
          <el-divider/>
          <el-main>
            <div v-if="progressVisible" class="demo-progress">
              <el-progress type="dashboard" :percentage="progress" :color="colors">
                <template #default="{ percentage }">
                  <span class="percentage-value">{{ progress }}%</span>
                  <span v-if="progress!==100" class="percentage-label">处理中</span>
                  <span v-else class="percentage-label">处理完成</span>
                </template>
              </el-progress>
            </div>
            <div>
              <el-table v-if="!progressVisible" :data="dataResult" style="width: 100%">
                <el-table-column type="expand" >
                  <template #default="props">
                    <div m="4">
                      <h4>实体数据</h4>
                      <el-table :data="props.row.entity" :border="false">
                        <el-table-column label="实体名称" prop="text" />
                        <el-table-column label="类型" prop="type" />
                        <el-table-column label="起始位置" prop="start" />
                        <el-table-column label="结束位置" prop="end" />
                      </el-table>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="句子"  prop="sentence" :style="{fontSize:'16px'}"/>
              </el-table>
            </div>
          </el-main>
        </el-container>
      </div>
    </basic-container>
  </div>
</template>
<script setup>
import {onMounted, ref} from "vue";
import axios from '@/router/axios.js'
import {ElMessage} from "element-plus";
import Steps from "@/views/kg/kgExtract/featureExtract/NerSteps.vue";
import {useRoute} from "vue-router";
let progress = ref(0)
let progressVisible = ref(true)
const $route = useRoute()
let active = ref(2)
let dataResult = ref([])
const colors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#1989fa', percentage: 60 },
  { color: '#6f7ad3', percentage: 80 },
  { color: '#0dd01c', percentage: 100 },
]
onMounted(async () => {
  const fileUrl = $route.query.fileUrl
  console.log(fileUrl)
  await fetchNerResult(fileUrl)
});

const fetchNerResult = async function (fileUrl) {
  try {
    const response = await axios.get('/kg/featureExtract/ner', { params: { fileUrl: fileUrl} });
    const taskId = response.data.data;
    checkStatus(taskId);
  } catch (error) {
    ElMessage.error(error)
  } finally {
  }
}
const checkStatus = function(taskId) {
  let count = 0; // 初始化轮询次数计数器
  const maxAttempts = 20; // 设置最大尝试次数，假设20次
  ElMessage.success("任务已发送！")
  const interval = setInterval(async () => {
    try {
      count++; // 每次轮询时增加计数
      progress.value = (count / maxAttempts) * 100; // 更新进度条
      const resultResponse = await axios.get(`/kg/featureExtract/ner/result`, { params: { taskId } });
      if (resultResponse.data) {
        if(resultResponse.data.data){
          // 任务完成并返回了结果
          clearInterval(interval);
          progress.value = 100; // 完成时设置进度为100%
          dataResult.value = resultResponse.data.data
          setTimeout(() => {
            progressVisible.value = false;
          }, 1500);
        }
        ElMessage.success(resultResponse.data.msg)
      }
      if (count >= maxAttempts) {
        // 达到最大尝试次数，停止轮询
        clearInterval(interval);
        ElMessage.warning('已达到最大尝试次数。');
      }
    } catch (error) {
      clearInterval(interval);
      ElMessage.error('发生错误，停止轮询。');
    }
  }, 10000); // 每10秒查询一次
};

</script>
<style scoped lang="scss">
.title {
  font-size: 20px;
  font-weight: bold;
}
.percentage-value {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}
.percentage-label {
  display: block;
  margin-top: 10px;
  font-size: 12px;
}
.demo-progress{
  display: flex;
  flex-direction: column;
  justify-content: center; /* 垂直居中 */
  align-items: center; /* 水平居中（如果需要） */
  height: 100%; /* 确保有足够的高度 */
  .el-progress--line {
    margin-bottom: 15px;
    max-width: 600px;
  }
  .el-progress--circle {
    margin-right: 15px;
  }
}
</style>
