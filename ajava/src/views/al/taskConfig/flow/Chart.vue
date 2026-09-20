<template>
  <div class="chart-card" v-if="isVisible">
    <div class="close-btn" @click="closeCard">×</div>
    <h3 class="chart-title">{{ title }}</h3>
    <div class="chart-content"
         v-loading="loading && !isTrain"
         element-loading-text="执行中..."
         element-loading-background="rgba(0, 0, 0, 0.3)">
      <el-popover
          v-if="imageUrl !== ''"
          placement="top"
          trigger="hover"
          :width="popoverWidth"
      >
        <template #reference>
          <div class="image-container">
            <el-image
                v-if="!loading && imageUrl!==''"
                :src="`/api/al/file/${imageUrl}`"
                alt="Chart Image"
                class="chart-image"
            />
            <div class="overlay"></div>
            <div class="icon-group">
              <div class="eye-icon" v-if="!loading">
                <el-icon :size="20" color="#333">
                  <View />
                </el-icon>
              </div>
              <el-button v-if="noPrecess===true && isTraining===2" class="otherInfo-button normalBtn" @click="otherInfoVisible=true">查看其他</el-button>
            </div>
          </div>
        </template>
        <div class="popover-image-container">
          <el-image
              :src="'/api/al/file/'+imageUrl"
              :style="popoverImageStyle"
              @load="handlePopoverImageLoad"
              class="popover-image-content"
          />
        </div>
      </el-popover>
    </div>
    <!-- 对话框 -->
    <el-dialog
        v-model="otherInfoVisible"
        title="数据集信息与模型评估指标"
        width="50%"
        :before-close="handleClose"
    >
      <div class="dialog-content">
        <el-table :data="datasetTableData" border style="width: 100%">
          <el-table-column prop="name" label="数据集信息" width="150"></el-table-column>
          <el-table-column prop="value" label="值"></el-table-column>
        </el-table>
        <br/>
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="name" label="指标名称" width="150"></el-table-column>
          <el-table-column prop="value" label="值"></el-table-column>
        </el-table>
        <br/>
        <div style="display: flex;justify-content: flex-start">
          <el-button
              v-if="otherInfo.datasetUrl"
              class="normalBtn"
              @click="downloadDataset(otherInfo.datasetUrl)"
          >
            下载小波变换后的数据集
          </el-button>
          <div v-if="otherInfo.wavePacketImage" style="display: flex; justify-content: space-between; align-items: center;margin-left: 10px">

            <el-popover
                placement="top"
                width="500"
                trigger="hover"
            >
              <el-image
                  style="max-width: 100%; max-height: 100%; min-width: 100%; min-height: 100%; object-fit: contain;"
                  :src="`/api/al/file/${otherInfo.wavePacketImage}`"
              />
              <template #reference>
                <el-button class="viewBtn">小波变换结果</el-button>
              </template>
            </el-popover>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {View} from '@element-plus/icons-vue';
import {ElMessage} from "element-plus";
import {downLoad_Dataset_d} from "@/api/al/taskConfig";
import {useStore} from 'vuex';
const store = useStore();
const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true,
  },
  title: {
    type: String,
    required: true,
  },
  imageUrl: {
    type: String,
    default: "",
  },
  loading: {
    type: Boolean,
    default: false,
  },
  isTrain: {
    type: Boolean,
    default: true,
  },
  isTraining:{
    type: Number,
    default:0,
    validator: value => Number.isInteger(value)
  },
  noPrecess:{
    type: Boolean,
    default: false,
  },
  otherInfo: {
    type: Object,
    default: null,
  },

});

const title = computed(() => props.title);
const loading = ref(props.loading);
const imageUrl = ref(props.imageUrl);
const isTraining = ref(props.isTraining);
const noPrecess = ref(props.noPrecess);
const otherInfo = ref(props.otherInfo);

// 控制图表卡片的可见性
const isVisible = ref(props.isVisible);

const emit = defineEmits(["clear"]);
// 关闭图表卡片
const closeCard = () => {
  isVisible.value = false;
  emit('clear')
};

const popoverWidth = ref(500); // 默认宽度
const popoverImageStyle = ref({});

const handlePopoverImageLoad = (e) => {
  const img = e.target;
  const naturalWidth = img.naturalWidth;
  const naturalHeight = img.naturalHeight;

  // 动态计算最佳显示尺寸（限制最大宽度800px，最大高度600px）
  const maxDisplayWidth = 900;
  const maxDisplayHeight = 600;

  let displayWidth = naturalWidth;
  let displayHeight = naturalHeight;

  // 根据图片原始比例调整
  if (naturalWidth > maxDisplayWidth || naturalHeight > maxDisplayHeight) {
    const ratio = Math.min(
        maxDisplayWidth / naturalWidth,
        maxDisplayHeight / naturalHeight
    );
    displayWidth = naturalWidth * ratio;
    displayHeight = naturalHeight * ratio;
  }

  // 设置popover宽度（留20px边距）
  popoverWidth.value = Math.min(displayWidth + 20, maxDisplayWidth);

  // 设置图片样式
  popoverImageStyle.value = {
    width: `${displayWidth}px`,
    height: `${displayHeight}px`,
    'max-width': '100%',
    'max-height': '100%'
  };
};


watch(() => props.loading, (newVal) => {
  loading.value = newVal;
});
watch(() => props.imageUrl, (newVal) => {
  imageUrl.value = newVal;
});
watch(() => props.isVisible, (newVal) => {
  isVisible.value = newVal;
});
watch(() => props.isTraining, (newVal) => {
  isTraining.value = newVal;
});
watch(() => props.noPrecess, (newVal) => {
  noPrecess.value = newVal;
});

watch(() => props.otherInfo, (newVal) => {
  otherInfo.value = newVal;
});

// 构造表格数据
const tableData = computed(() => {
  if (!otherInfo.value || !otherInfo.value.metrics) {
    return []
  }
  return [
    { name: '精确率', value: otherInfo.value.metrics.precision?.toFixed(4) ?? '-' },
    { name: '召回率', value: otherInfo.value.metrics.recall?.toFixed(4) ?? '-' },
    { name: 'F1分数', value: otherInfo.value.metrics.F1?.toFixed(4) ?? '-' }
  ]
})

const datasetTableData = computed(() => {
  if (!otherInfo.value || !otherInfo.value.datasetInfo) {
    return []
  }
  return [
    { name: '训练集数量', value: otherInfo.value.datasetInfo.trainset_len ?? '-' },
    { name: '测试集数量', value: otherInfo.value.datasetInfo.testset_len ?? '-' }
  ]
})


let otherInfoVisible=ref(false);
const checkOtherInfo=()=>{
  otherInfoVisible.value=true
}
// 关闭弹窗
const handleClose = (done) => {
  otherInfoVisible.value = false
  done()
}

// 下载函数
const downloadDataset = (url) => {
  downLoad_Dataset_d({filePath:url}).then(res=>{
    if(res.data===null){//如果数据集不在minio
      ElMessage.error('该路径下找不到数据集，请重新训练后再下载')
    }else{
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      let filename=res.config.params.filePath
      link.setAttribute('download', filename.split('/').pop());
      document.body.appendChild(link);
      link.click();
      ElMessage.success('开始下载数据集...')
    }
  }).catch(error => {
    console.error('下载文件时发生错误:', error);
    ElMessage.error('文件下载失败: ' + error.message);
  })
}
</script>

<style scoped>
.chart-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  width: 150px;
  height: 200px;
  display: flex;
  flex-direction: column;
  position: relative; /* 用于放置关闭按钮 */
}

.close-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  background-color: transparent;
  border: none;
  padding: 0;
}

.chart-title {
  font-size: 16px;
  margin-bottom: 8px;
  text-align: center;
}

.chart-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  background-color: #f9f9f9;
  border-radius: 4px;
  position: relative;
}

.image-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.chart-image {
  max-width: 100%;
  max-height: 100%;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-group {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center;
  justify-content: center;
  z-index: 3; /* 确保在 overlay 上层 */
  gap: 10px; /* 图标与按钮之间的间距 */
}
.eye-icon,
.otherInfo-button {
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.otherInfo-button {
  font-size: 12px;
  padding: 0;
  line-height: 1;
}
.eye-icon{
  border-radius: 50%;
}

.eye-icon:hover {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}
.dialog-content {
  padding: 10px;
}

.popover-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.popover-image-content {
  object-fit: contain;
  transition: all 0.3s ease;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .popover-image-content {
    max-width: 90vw !important;
    max-height: 90vh !important;
  }
}
</style>
