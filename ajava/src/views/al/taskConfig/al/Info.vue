<template>
  <el-row :gutter="10" class="centered-row">
    <el-col :span="8" style="margin-right: 40px">
      <el-image
          v-if="nodeData.modelIcon"
          style="width: 120%; height: 120%"
          :src="getImageSrc(nodeData)"
      >
        <template #error>
          <div class="image-slot" style="width: 10%;height:100%">
            <img src="@/assets/upload.png" style="width: 100%;height:100%">
          </div>
        </template>
      </el-image>
    </el-col>
    <el-col :span="10">
      <div class="container">
        <div class="content">
          <span :style="{ fontSize: '27px', color: '#444447', fontWeight: 'bold' }">
            {{nodeData.modelName }}
          </span>
        </div>
        <div class="content buttons-container">
          <el-button v-if="nodeData.isDeployed === 0" class="auditBtn custom-disabled">
            已审核
          </el-button>
          <el-button v-else class="viewBtn" @click="handleCheck(nodeData)">
            执行审核
          </el-button>
          <el-button v-if="nodeData.isPublished === 0" class="auditBtn custom-disabled">
            已发布
          </el-button>
          <el-button v-else class="editWorktBtn" @click="updateIspublishedConfig(nodeData)">
            发布
          </el-button>
          <el-button class="delBtn" @click="handleDelete(nodeData)">
            删除
          </el-button>

          <el-tooltip
              :content="nodeData.isDeployed === 1 ? '请先完成审核' : ''"
              placement="top"
              :disabled="nodeData.isDeployed !== 1"
          >
            <el-button
                :class="nodeData.isDeployed === 1 ? 'editWorktBtn custom-disabled' : (nodeData.isService === 0 ? 'auditBtn' : 'disMissBtn')"
                @click="toggleService(nodeData)"
            >
              {{ nodeData.isService === 0 ? '启用' : '禁用' }}
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </el-col>
  </el-row>

  <el-divider />

  <div class="centered-row">
    <div class="wrapper">
      <el-row class="table-col">
        <el-col :span="24" class="table-item">
          <span class="label">组态流程</span>
          <span class="value">
            <div class="process-container">
              <template v-for="(process, index) in nodeData.taskProcess" :key="index">
              <span class="process-item">
                {{ index + 1 }}. {{ process.almodelName }}
              </span>
              <span class="arrow" v-if="index < nodeData.taskProcess.length - 1"></span>
            </template>
            </div>
          </span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">训练数据集</span>
          <span class="value">
            <el-button class="normalBtn"
                       style="margin-left: 100px"
                       @click="previewDataSet(nodeData.objectId, 'train')">预览</el-button>
            <el-button class="normalBtn"
                     style="margin-left: 10px"
                     @click="downLoad_Dataset(nodeData.objectId, 'train')">下载</el-button>
          </span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">测试数据集</span>
          <span class="value">
            <el-button class="normalBtn"
                       style="margin-left: 100px"
                       @click="previewDataSet(nodeData.objectId, 'test')">预览</el-button>
            <el-button class="normalBtn"
                     style="margin-left: 10px"
                     @click="downLoad_Dataset(nodeData.objectId, 'test')">下载</el-button>
          </span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="12" class="table-item">
          <span class="label">针对对象</span>
          <span class="value">{{ nodeData.modelObject }}</span>
        </el-col>
        <el-col :span="12" class="table-item">
          <span class="label">创建时间</span>
          <span class="value">{{ nodeData.createTime }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col">
        <el-col :span="6" class="table-item">
          <span class="label">学习率</span>
          <span class="value">{{ nodeData.learningRate }}</span>
        </el-col>
        <el-col :span="6" class="table-item">
          <span class="label">优化器</span>
          <span class="value">{{ nodeData.optimizer }}</span>
        </el-col>
        <el-col :span="6" class="table-item">
          <span class="label">训练次数</span>
          <span class="value">{{ nodeData.trainEpoch }}</span>
        </el-col>
        <el-col :span="6" class="table-item">
          <span class="label">训练批次</span>
          <span class="value">{{ nodeData.trainBatch }}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.pretreatment === '小波变换'">
        <el-col :span="6" class="table-item">
          <span class="label">最大分解层级</span>
          <span class="value">{{ nodeData.maxLevel }}</span>
        </el-col>
        <el-col :span="6" class="table-item">
          <span class="label">切片大小</span>
          <span class="value">{{ nodeData.sliceLength }}</span>
        </el-col>
        <el-col :span="6" class="table-item">
          <span class="label">小波基类型</span>
          <span class="value">{{ nodeData.waveLet}}</span>
        </el-col>
        <el-col :span="6" class="table-item">
          <span class="label">小波变换模式</span>
          <span class="value">{{ nodeData.waveMode}}</span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-if="nodeData.pretreatment === '小波变换'">
        <el-col :span="6" class="table-item">
          <span class="label">提取层级</span>
          <span class="value">{{ nodeData.n }}</span>
        </el-col>
        <el-col :span="18" class="table-item">
          <span class="label">训练指标</span>
          <span class="value">
            <span v-for="(value, key) in parsedTrainMetrics" :key="key" style="margin-right: 16px;">
              {{ key }}: {{ value }}
            </span>
          </span>
        </el-col>
      </el-row>
      <el-row class="table-col" v-else>
        <el-col :span="24" class="table-item">
          <span class="label">训练指标</span>
          <span class="value">
            <span v-for="(value, key) in parsedTrainMetrics" :key="key" style="margin-right: 16px;">
            {{ key }}: {{ value }}
            </span>
          </span>
        </el-col>
      </el-row>
    </div>
  </div>

  <el-dialog v-model="showPreviewDialog"
             title="数据集预览"
             style="width: 50%;height: 60%"
             draggable
             @close="closeDialog()">
    <div v-loading="loading" element-loading-text="数据加载中...">
      <div class="dlgPreview">
        <el-card shadow="hover" style="width: 95%;">
          <el-table :data="previewData" stripe style="width: 100%">
            <el-table-column v-for="column in previewColumns" :key="column.prop" :prop="column.prop" :label="column.label">
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>

  </el-dialog>

</template>

<script>
import {ElMessage, ElMessageBox} from "element-plus";
import {
  checkAlConfig,
  updateAlIspublishedConfig,
  deleteAlConfig,
  removeAlInput
} from "@/api/al/taskConfig";
import * as XLSX from "xlsx";
import {getDataUrl} from "@/api/diagnosis/graphVis/dataIngestion";
import {downLoad_Dataset_s} from "@/api/al/stateEvaluation/stateEvaluationBase";
import {downLoad_Dataset_d} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";

export default {
  name: "Info",
  props: {
    nodeData: {
      type: Object,
      required: true
    },
  },

  data() {
    return {
      isCheck: false,
      previewData: [],
      previewColumns: [],
      showPreviewDialog: false,
      loading: false,
      trainData: null,
      testData: null
    }
  },

  computed: {
  parsedTrainMetrics() {
    let metrics = {};
    try {
      if (typeof this.nodeData.trainMetrics === 'string') {
        metrics = JSON.parse(this.nodeData.trainMetrics);
      } else if (typeof this.nodeData.trainMetrics === 'object' && this.nodeData.trainMetrics !== null) {
        metrics = this.nodeData.trainMetrics;
      }
    } catch (e) {
      metrics = {};
    }
    // 只保留 F1, recall, precision
    return {
      F1: metrics.F1,
      recall: metrics.recall,
      precision: metrics.precision
    };
  }
},

  emits: ['loadTreeData', 'clearInfo'],

  methods: {
    getImageSrc(node) {
      return `/api/al/file/${node.modelIcon}`
    },

    async handleCheck(row) {
      try {
        this.isCheck = true;
        const res = await checkAlConfig(row, {isCheck: this.isCheck});

        if (res.data.code === '200') {
          row.isDeployed = row.isDeployed === 1 ? 0 : 1; // 修复逻辑错误
          this.refresh(false)
          ElMessage.success("通过审核");
        }
      } catch (error) {
        ElMessage.error("审核失败: " + error.message);
      }
    },

    async updateIspublishedConfig(row) {
      try {
        this.isCheck = true;
        const res = await updateAlIspublishedConfig(row);
        if (res.data.data.code === '200') {
          row.isPublished = row.isPublished === 1 ? 0 : 1; // 修复逻辑错误
          this.refresh(false)
          ElMessage.success("发布算法组态成功");
        } else if (res.data.data.code === '500') {
          ElMessage.warning(res.data.data.message);
        }
      } catch (error) {
        ElMessage.error(error.message);
      }
    },

    handleDelete(row) {
      ElMessageBox.confirm("此操作将永久删除该任务, 是否继续?", "提示", {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteAlConfig({ code: row.code, name: row.modelName });
          removeAlInput(row.modelName, 'algorithmConfig')
          if (res.data.code === '200') {
            this.refresh(true)
            ElMessage.success("删除成功");
          }
        } catch (error) {
          ElMessage.error("删除失败: " + error.message); // 错误处理
        }
      }).catch(() => {
        // 用户点击取消的操作
        ElMessage.info("已取消删除");
      });
    },

    async toggleService(row) {
      if (row.isDeployed === 1) {
        ElMessage.warning("请先审核");
        return;
      }

      const newValue = row.isService === 0 ? 1 : 0;
      try {
        const res = await checkAlConfig(row, { isCheck: false });
        if (res.data.code === '200') {
          ElMessage.success(newValue === 0 ? "组态已启用" : "组态已停用");
          row.isService = newValue;
        } else {
          this.$message.error("操作失败");
        }
      } catch (error) {
        ElMessage.error("操作失败: " + error.message);
      }
    },

    async previewDataSet(objectId, type) {
      if (!this.trainData || !this.testData) {
        let dataType = this.nodeData.modelType === '状态评估' ? 'scada' : 'cms'
        let res = await getDataUrl({objectId: objectId, dataType: dataType})
        if (res.data.code === 0) {
          this.trainData = res.data.data.scada_train_latest_dataset
          this.testData= res.data.data.scada_test_latest_dataset
        }
      }

      this.showPreviewDialog = true
      this.loading = true
      let url = type === 'train' ? this.trainData : this.testData
      this.fetchAndParseFile(url)
    },

    fetchAndParseFile(minioUrl) {
      if(minioUrl===''){
        this.$notify.error('请先上传数据集，再下载数据集')
      }else {
        axios.get(`/al/file/${minioUrl}`, {responseType: 'arraybuffer'})
            .then(response => {
              if (response.data == null) {
                this.$notify.error('该路径下找不到数据集，请重新上传后再下载')
              } else {
                const workbook = XLSX.read(response.data, {type: 'array'})
                const sheetName = workbook.SheetNames[0]
                const worksheet = workbook.Sheets[sheetName]
                const jsonData = XLSX.utils.sheet_to_json(worksheet, {header: 1})

                // 提取列名
                this.previewColumns = jsonData[0].map((_, index) => ({
                  prop: `col${index}`,
                  label: jsonData[0][index]
                }))

                // 提取数据行
                this.previewData = jsonData.slice(1).map(row => {
                  const rowData = {}
                  row.forEach((value, index) => {
                    rowData[`col${index}`] = value;
                  })
                  return rowData
                })
                this.loading = false

              }

            })
            .catch(error => {
              console.error('Error fetching and parsing file:', error)
            })
      }
    },

    //下载数据集
    async downLoad_Dataset(objectId, type) {
      const downLoad_Dataset = this.nodeData.modelType === '状态评估' ? downLoad_Dataset_s : downLoad_Dataset_d
      if (!this.trainData || !this.testData) {
        let dataType = this.nodeData.modelType === '状态评估' ? 'scada' : 'cms'
        let res = await getDataUrl({objectId: objectId, dataType: dataType})
        if (res.data.code === 0) {
          this.trainData = res.data.data.scada_train_latest_dataset
          this.testData = res.data.data.scada_test_latest_dataset
        }
      }
      let url = type === 'train' ? this.trainData : this.testData

      downLoad_Dataset({filePath: url}).then(res => {
        if (res.data === null) {//如果数据集不在minio
          this.$notify.error('该路径下找不到数据集，请重新上传后再下载')
        } else {
          const url = window.URL.createObjectURL(new Blob([res.data]));
          const link = document.createElement('a');
          link.href = url;
          let filename = res.config.params.filePath
          link.setAttribute('download', filename.split('/').pop());
          document.body.appendChild(link);
          link.click();
          this.$notify.success('开始下载')
        }
      }).catch(error => {
        console.error('下载文件时发生错误:', error);
        ElMessage.error('文件下载失败: ' + error.message);
      });
    },

    closeDialog() {
      this.showPreviewDialog = false
      this.previewData = []
    },

    clear() {
      this.trainData = null
      this.testData = null
    },

    refresh(ifDelete){
      this.$emit('loadTreeData')
      if (ifDelete)
        this.$emit('clearInfo')
    },

  }
}
</script>

<style lang="scss" scoped>
.el-row {
  text-align: center;
}
.el-row:last-child {
  margin-bottom: 0;
}

.container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 40px;
  margin-top: 20px;
}

.content {
  text-align: center;
}

.buttons-container {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.centered-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.wrapper {
  margin-top: 10px;
  width: 83%;
  border: solid 1px #d9d9d9;

  .table-col:not(:last-child) {
    border-bottom: solid 1px #d9d9d9;
  }

  .table-item:not(:last-child) {
    border-right: solid 1px #d9d9d9;
  }

  .table-col {
    width: 950px;
    display: flex;
  }

  .table-item {
    height: 100%;
    display: flex;

    .label {
      display: flex;
      justify-content: center;
      background: #FAFAFA;
      color: #909399;
      padding: 10px 7px;
      border-right: solid 1px #d9d9d9;
      font-weight: bold;
      font-size: 18px;
      width: 100px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .value {
      padding: 3px;
      display: flex;
      align-items: center;
      vertical-align: center;
      word-wrap: break-word;
      color: #606266;
      font-size: 16px;
      flex: 1;
      white-space: nowrap;
      overflow: auto;
      text-overflow: ellipsis;
    }
  }
}

.dlgPreview{
  height: 450px;
  overflow: auto;
  margin-left: 20px;
}

.custom-disabled {
  background-color: #d3d3d3 !important;
  color: #8c8c8c !important;
  border-color: #d3d3d3 !important;
  cursor: not-allowed;
}

.process-container {
  display: flex;
  align-items: center;
  flex-wrap: nowrap; /* 禁止换行 */
  overflow-x: auto; /* 如果内容过多允许横向滚动 */
  white-space: nowrap; /* 防止文本换行 */
}

.process-item {
  margin: 0 4px;
  white-space: nowrap;
  color: #409eff;
  font-size: 16px;
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
</style>
