<template>
  <el-dialog
      v-model="dialogVisible"
      append-to-body
      :close-on-click-modal="false"
      class="el-dialog__header"
      show-close
      width="40%"
      @close="handleClose"
  >

    <template #header>
      <div style="display: flex; padding-top: 10px; padding-bottom: 10px">
        <div style="width: 5%; display: flex;">
          <el-image src="arrow.png" fit="fill"></el-image>
        </div>
        <div style="font-size: 24px; display: flex; padding-left: 1%">
          <span>变量选择</span>
        </div>
      </div>
    </template>

    <div v-loading="loading" element-loading-text="加载中...">
      <div class="container">
        <!-- 左侧可用变量 -->
        <div class="list-container left">
          <div class="list-title-wrapper">
            <el-checkbox
                :indeterminate="isIndeterminate"
                v-model="checkAll"
                @change="handleCheckAllChange"
                class="check-all-checkbox"
            ></el-checkbox>
            <span class="list-title-text">可用变量</span>
          </div>

          <!-- 搜索框 -->
          <div class="search-wrapper">
            <el-input
                v-model="searchQuery"
                placeholder="搜索变量"
                clearable
                size="small"
                style="width: 80%; height: 30px"
            />
          </div>

          <el-checkbox-group v-model="leftSelected" class="list-body">
            <el-checkbox
                v-for="item in filteredTransferData"
                :key="item.prop"
                :value="item.label"
                :disabled="isSelected(item.label)"
            >
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 中间箭头按钮列 -->
        <div class="arrow-button-column">
          <el-button
              type="primary"
              size="small"
              @click="addToTarget"
              :disabled="leftSelected.length === 0 || targetVariable !== null"
              title="添加到基准变量"
              style="margin-top: 30px; margin-left: 12px"
          >
            <el-icon><DArrowRight /></el-icon>
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="removeFromTarget"
              :disabled="targetVariable === null"
              title="从基准变量移除"
          >
            <el-icon><DArrowLeft /></el-icon>
          </el-button>

          <el-button
              type="primary"
              size="small"
              @click="addToRelated"
              :disabled="leftSelected.length === 0"
              title="添加到相关变量"
              style="margin-top: 160px"
          >
            <el-icon><DArrowRight /></el-icon>
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="removeFromRelated"
              :disabled="relatedVariables.length === 0"
              title="从相关变量移除"
          >
            <el-icon><DArrowLeft /></el-icon>
          </el-button>
        </div>

        <!-- 右侧 目标变量和相关变量垂直排列 -->
        <div class="right-column">
          <div class="list-container target">
            <div class="list-title">基准变量（单选）</div>
            <el-radio-group v-model="targetVariable" class="list-body">
              <el-radio v-if="targetVariable" :value="targetVariable">
                {{ targetVariable }}
              </el-radio>
            </el-radio-group>
          </div>

          <div class="list-container related" style="margin-top: 20px;">
            <div class="list-title">相关变量（多选）</div>
            <el-checkbox-group v-model="relatedVariables" class="list-body">
              <el-checkbox
                  v-for="item in sortedRelatedVariables"
                  :key="item.name"
                  :value="item.name"
              >
                {{ item.name }}
                <!-- 显示相关性值 -->
                <span v-if="item.correlation !== null" style="font-size: 14px; color: #888;">
                  (相关性: {{ item.correlation.toFixed(4) }})
                </span>
              </el-checkbox>
            </el-checkbox-group>

          </div>
        </div>
      </div>
      <div class="footer-threshold">
        <span>相关性阈值：</span>
        <el-input
            v-model="correlationThreshold"
            size="small"
            style="width: 80%; height: 30px"
            placeholder="请输入0~1之间的数"
            type="number"
            min="0"
            max="1"
            step="0.01"
        />
      </div>
    </div>

    <!-- 分析结果展示区域 -->
    <div v-if="correlationImage && sortedCorrelations.length" class="analysis-result">
      <div>
        <el-image
            style="max-width: 100%; max-height: 100%; object-fit: contain;"
            :src="`/api/al/file/${correlationImage}`"
        />
      </div>
    </div>


    <template #footer>
      <div class="dialog-footer">
        <el-button class="disMissBtn" @click="handleCancel">取消</el-button>
        <el-button class="printBtn" @click="handleAnalysis">相关性分析</el-button>
        <el-button class="auditBtn" @click="handleConfirm">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, defineExpose, defineProps, nextTick, ref, watch} from 'vue';
import {ElButton, ElDialog, ElMessage, ElNotification} from 'element-plus';
import * as XLSX from "xlsx";
import {getObj} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {startConfig} from "@/api/al/taskConfig";

const props = defineProps({
  varSelectVisible: Boolean,
});

const dialogVisible = computed({
  get() {
    return props.varSelectVisible;
  },
  set(value) {
    emit('update:varSelectVisible', value);
  }
});
const emit = defineEmits(["update:varSelectVisible", "confirm"]);

const loading = ref(false);

// 数据源给 Transfer 组件
const transferData = ref([]);
const leftSelected = ref([]); // 左侧选中变量
const targetVariable = ref(null); // 目标变量（单选）
const relatedVariables = ref([]); // 相关变量（多选）
const searchQuery = ref('');
// 新增响应式数据和计算属性
const checkAll = ref(false);
const isIndeterminate = ref(false);

const dataUrl = ref('');
const currentConfig = ref(null);
const correlationThreshold = ref('');

let timer = ref(null);
let taskId=ref(null);
let correlationResult=ref(null);
let correlationImage=ref(null);

// 计算当前搜索过滤后可选且未被选中（即非右侧已选）的变量列表（供全选用）
const filteredAvailableLabels = computed(() => {
  // 过滤出搜索结果中没有被右侧选中（目标或相关）且是可用的变量
  return filteredTransferData.value
      .filter(item => !isSelected(item.label))
      .map(item => item.label);
});

// 监听 leftSelected 变化，更新全选框状态
watch(leftSelected, (val) => {
  const filtered = filteredAvailableLabels.value;
  if (val.length === 0) {
    checkAll.value = false;
    isIndeterminate.value = false;
  } else if (val.length === filtered.length) {
    checkAll.value = true;
    isIndeterminate.value = false;
  } else {
    checkAll.value = false;
    isIndeterminate.value = true;
  }
});

// 全选框变化事件
function handleCheckAllChange(val) {
  if (val) {
    // 选中搜索过滤后的所有可用变量
    leftSelected.value = [...new Set([...leftSelected.value, ...filteredAvailableLabels.value])];
  } else {
    // 取消选中搜索过滤后的所有变量（只清除搜索过滤后的变量，保留其它已选变量）
    leftSelected.value = leftSelected.value.filter(label => !filteredAvailableLabels.value.includes(label));
  }
}

// 计算过滤后的可用变量列表
const filteredTransferData = computed(() => {
  if (!searchQuery.value) {
    return transferData.value;
  }
  const query = searchQuery.value.toLowerCase();
  return transferData.value.filter(item => item.label.toLowerCase().includes(query));
});

// 判断变量是否已在右侧任意框中
function isSelected(label) {
  return label === targetVariable.value || relatedVariables.value.includes(label);
}

// 添加左侧选中到目标变量（只能单选）
function addToTarget() {
  if (leftSelected.value.length === 0 || targetVariable.value !== null) return;
  targetVariable.value = leftSelected.value[0];
  leftSelected.value = leftSelected.value.filter(v => v !== targetVariable.value);
  // 如果目标变量在相关变量中，移除
  relatedVariables.value = relatedVariables.value.filter(v => v !== targetVariable.value);
}

// 从目标变量移除回左侧
function removeFromTarget() {
  if (!targetVariable.value) return;
  leftSelected.value.push(targetVariable.value);
  targetVariable.value = null;
}

// 添加左侧选中到相关变量（多选）
function addToRelated() {
  if (leftSelected.value.length === 0) return;
  relatedVariables.value.push(...leftSelected.value);
  leftSelected.value = [];
  // 确保目标变量不在相关变量中
  relatedVariables.value = relatedVariables.value.filter(v => v !== targetVariable.value);
}

// 从相关变量移除回左侧
function removeFromRelated() {
  if (relatedVariables.value.length === 0) return;
  leftSelected.value.push(...relatedVariables.value);
  relatedVariables.value = [];
}

// 下载 CSV 文件并解析表头
const downloadAndParseCSV = async (url, data) => {
  if (transferData.value.length > 0)
    return
  if(url === '' || url === null){
    ElNotification.error('未获取到SCADA数据集')
  }else {
    loading.value = true
    axios.get(`/al/file/${url}`, {responseType: 'arraybuffer'})
        .then(response => {
          if (response.data == null) {
            ElNotification.error('该路径下找不到数据集，请重新获取')
          } else {
            const workbook = XLSX.read(response.data, {type: 'array'})
            const sheetName = workbook.SheetNames[0]
            const worksheet = workbook.Sheets[sheetName]
            const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1})

            // 提取列名
            transferData.value = jsonData[0]
                .filter((_, index) => jsonData[0][index] !== 'errorcode') // 过滤掉 label 为 errorcode 的项
                .map((_, index) => ({
                  prop: `col${index}`,
                  label: jsonData[0][index]
                }));
            dataUrl.value = url
            currentConfig.value = data
            loading.value = false
          }
        })
        .catch(error => {
          console.error('Error fetching and parsing file:', error)
        })
  }
};

const handleAnalysis = async () => {
  if (!targetVariable.value) {
    ElMessage.warning("未选择基准变量")
    return
  }
  else if (relatedVariables.length === 0) {
    ElMessage.warning("未选择相关变量")
    return
  }
  else if (!correlationThreshold.value) {
    ElMessage.warning("未设置相关性阈值")
    return
  }

  correlationResult.value = null;
  correlationImage.value = null

  let config = {
    ...currentConfig.value,
    taskMsg: JSON.stringify({
      targetVariable: targetVariable.value,
      relatedVariables: relatedVariables.value,
      correlationThreshold: correlationThreshold.value,
      programUrl: dataUrl.value,
    })
  }

  try {
    loading.value = true
    const startRes = await startConfig(config);
    if (startRes.data.data) {
      taskId.value = startRes.data.data
      // 等待 loopResult 完成
      await loopResult();
      await nextTick(); // 等待 DOM 更新

    }
  } catch (error) {
    console.error('请求算法出错:', error);
  }
};

const loopResult = async () => {
  let i = 0;
  console.log("开始轮循请求");
  // 先清除之前的定时器
  if (timer.value) {
    clearInterval(timer.value);
  }
  // 创建一个 Promise 来处理轮循完成
  return new Promise((resolve, reject) => {
    timer.value = setInterval(() => {
      fun(timer.value, i++, resolve,reject);
    }, 3000);
  });
};

const fun = (timerInstance, count, resolve,reject) => {
  setTimeout(() => {
    console.log("开始轮循请求：");
    console.log("次数：" + count);
    getObj(taskId.value).then(response => {
      if (response.data.data.taskState === 2) {
        loading.value = false
        if (response.data.data.taskResult) {
          const parsed = JSON.parse(response.data.data.taskResult);
          if (Object.keys(parsed).length > 0) {
            correlationResult.value = parsed.correlations;
            correlationImage.value = parsed.plotUrl;
          } else {
            // 为空对象，做相应处理，提示没有符合条件的相关变量
            correlationResult.value = null;
            correlationImage.value = null
            ElMessage.info('无符合阈值的相关变量，请重新选择变量！');
          }
        }
        clearInterval(timerInstance);
        resolve();
         // 重要：立即返回避免后续执行

      }
    });

    if (count >= 40) {
      ElMessage.warning('分析超时');
      loading.value = false;
      clearInterval(timerInstance);
      reject(new Error('分析超时')); // 超时时调用 reject
    }
  }, 0);
};

const parsedCorrelations = computed(() => {
  if (!correlationResult.value) return {};
  try {
    return JSON.parse(correlationResult.value);
  } catch (e) {
    console.error('解析相关性结果失败', e);
    return {};
  }
});

const sortedCorrelations = computed(() => {
  const obj = parsedCorrelations.value || {};
  return Object.entries(obj)
      .sort((a, b) => Math.abs(b[1]) - Math.abs(a[1]))  // 按相关性值的绝对值降序排序
      .map(([key, value]) => ({ name: key, correlation: value }));
});

// 计算排序后的相关变量
const sortedRelatedVariables = computed(() => {
  return relatedVariables.value
      .map(item => {
        const correlation = sortedCorrelations.value.find(cor => cor.name === item);
        return { name: item, correlation: correlation ? correlation.correlation : null };
      })
      .sort((a, b) => b.correlation - a.correlation); // 按照相关性值降序排列
});


const handleConfirm = async () => {
  emit('confirm', targetVariable.value, sortedCorrelations.value, correlationImage.value, currentConfig.value);
  dialogVisible.value = false
}

const reset = () => {
  transferData.value = []
  targetVariable.value = null
  relatedVariables.value = []
  correlationThreshold.value = ''
  correlationResult.value = null
  if (timer.value) {
    clearInterval(timer.value);
  }
}

defineExpose({
  downloadAndParseCSV,
  reset
})

// 关闭对话框时的逻辑
const handleClose = () => {
  dialogVisible.value = false
  if (timer.value) {
    clearInterval(timer.value);
  }
};

const handleCancel = () => {
  reset();
  dialogVisible.value = false
};

</script>

<style lang="scss" scoped>

.treeDialog{
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  .el-button{
    background-color: cadetblue;
    width: 65px;
    height: 40px;
    margin-top: 15px;
    border-color: white;
    &:hover {
      background-color: chocolate; /* 设置悬浮时的背景色 */
    }
  }
}

.container {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.list-container.left .list-body {
  max-height: 355px;
  min-height: 355px;
  overflow-y: auto;
}

.list-container {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

/* 右侧目标变量 */
.list-container.target .list-body {
  max-height: 60px;
  min-height: 60px;
  overflow-y: auto;
}

/* 右侧相关变量 */
.list-container.related .list-body {
  max-height: 260px;
  min-height: 260px;
  overflow-y: auto;
}

.list-title {
  padding: 8px;
  font-weight: bold;
  border-bottom: 1px solid #dcdfe6;
  background: #f5f7fa;
  text-align: center;
}
.list-title-wrapper {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.check-all-checkbox {
  margin-left: 8px;
  /* 可根据需求调整大小和间距 */
}

.list-title-text {
  flex: 1;
  font-weight: bold;
  text-align: center;
  margin-right: 20px;
  font-size: 14px; /* 根据之前标题大小调整 */
}

.list-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.arrow-button-column {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  margin-right: 6px
}
.arrow-button-column .el-button {
  width: 30px;
  min-width: 30px;
  padding: 0;
}

.right-column {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.search-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.footer-threshold {
  display: flex;
  align-items: center;
  padding-top: 12px;
  font-size: 15px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.analysis-result {
  margin-top: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fafafa;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}


</style>
