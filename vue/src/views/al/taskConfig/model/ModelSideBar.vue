<!-- ModelSideBar.vue -->
<template>
<div>
  <el-drawer
      v-model="visible"
      :title="drawerTitle"
      :size="'30%'"
      @close="handleClose"
  >
    <div  v-show="tableData!==null && tableData.length !== 0">
      <!-- 表格 -->
      <el-table
          :data="tableData.slice((currentPage-1)*pageSize, currentPage*pageSize)"
          style="width: 100%"
          border
      >
        <el-table-column prop="varId" label="序号" width="100" header-align="center" align="center"></el-table-column>
        <el-table-column prop="varName" label="变量名称" width="432" header-align="center" align="center"></el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
        <el-pagination
            background
            layout="prev, pager, next"
            :total="tableData.length"
            :current-page.sync="currentPage"
            :page-size="pageSize"
            @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <div v-if="dataSource.includes(drawerTitle)" style="margin-top: 20px;display: flex;flex-direction: column;justify-content: center;align-items: center">
      <!-- 按钮行（移除不必要的 flex 嵌套） -->
      <div style="display: flex; align-items: center;justify-content: center">
        <el-button v-if="isSCADA" class="printBtn" @click="downTemplate">下载模板</el-button>

        <!-- 上传组件（移除 display:contents，改用常规布局） -->
        <el-upload
            :file-list="modeList"
            :http-request="modeUpload"
            :limit="1"
            :show-file-list="false"
        @change="handleFileChange"
        style="margin-left: 30px;"
        >
          <el-button class="addBtn">上传数据集</el-button>
        </el-upload>

        <el-button
            class="auditBtn"
            @click="upload"
            style="margin-left: 30px;"
        >
          确定上传
        </el-button>
      </div>

      <!-- 文件名显示区域（固定在按钮正下方） -->
      <div
          v-if="selectedFileName"
          style="
          margin-top: 8px;
          padding: 4px 8px;
          background: #f5f7fa;
          border-radius: 4px;
          color: #409EFF;
          font-size: 12px;
          display: inline-block;  /* 自适应宽度 */
          max-width: 300px;  /* 限制最大宽度 */
          overflow: hidden;  /* 隐藏超出的文本 */
          text-overflow: ellipsis;
          display: flex;
          justify-content: center; /* 水平居中 */
          align-items: center; /* 垂直居中 */
          text-align: center;
    "
      >
        <i class="el-icon-document" ></i>
        {{ selectedFileName }}
        <!-- 取消按钮 -->
        <el-button
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="cancelFile"
            style="margin-left: 10px; padding: 0; font-size: 12px; color: #409EFF;"
        >
        </el-button>
      </div>
    </div>

  </el-drawer>
</div>
</template>

<script setup>
import {ref,watch,computed} from 'vue';
import { ElDrawer,ElNotification  } from 'element-plus'; // 使用 Element Plus 的 Drawer 组件
import {uploadProgram} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import { useStore } from 'vuex';
import * as XLSX from "xlsx";

// 父组件传递的节点信息
const props = defineProps({
  visible: Boolean,
  nodeLabel: String,
  tableData: Array,
  isUpload: Boolean,
  isSCADA: Boolean
});
const emit = defineEmits(["update:visible"]);
const store = useStore();
// 控制抽屉显示
const visible = ref(props.visible);
const dataSource = ref(['SCADA数据', 'CMS数据']);
const drawerTitle = computed(() => props.nodeLabel);  //基于prop创建，否则不会更新


// 同步父组件的 visible 变化
watch(() => props.visible, (newVal) => {
  visible.value = newVal;
});

// 同步visible 的变化到父组件
watch(visible, (newVal) => {
  emit('update:visible', newVal);
});

const tableData = computed(() => props.tableData);
const isUpload = computed(() => props.isUpload);
const isSCADA = computed(() => props.isSCADA);
const currentPage = ref(1);
const pageSize = ref(15);

// 方法：处理分页变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
};

// 关闭抽屉
const handleClose = () => {
  visible.value = false;
};

const mode = ref({});
const modeList = ref([]);
const datasetUrl = ref('');

const modeUpload = (item) => {
  mode.value = item.file;
};
// 取消文件选择
const cancelFile = () => {
  selectedFileName.value = ''; // 清除文件名
  modeList.value = []; // 清空文件列表
};
const downTemplate = () => {

  // 提取所有 varName 作为表头
  const headers = tableData.value.map((item) => item.varName);
  headers.push("errorcode");

  // 创建一个空的数据行（可以根据需求填充数据）
  const dataRows = [
    headers.map(() => ""), // 生成一行空数据
  ];

  const worksheetData = [headers, ...dataRows];
  const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);

  // 创建工作簿并添加工作表
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, "Sheet1");

  // 导出 Excel 文件
  XLSX.writeFile(workbook, "template_SCADA.xlsx");

}

const selectedFileName = ref(''); // 用于保存上传的文件名

// 模拟上传组件的文件变化处理
const handleFileChange = (file, fileList) => {
  selectedFileName.value = file.name; // 将文件名赋值给 selectedFileName
};

// Function to handle upload action
const upload = () => {
  selectedFileName.value=''
  const file = new FormData();
  if (mode.value.uid === undefined) {
    ElNotification({
      title: 'Warning',
      message: '没有数据集',
      type: 'warning',
    });
  } else {
    file.append('file', mode.value);
     uploadProgram(file)
        .then((response) => {
          ElNotification({
            title: 'Success',
            message: '上传成功',
            type: 'success',
          });
          modeList.value = [];
          mode.value = {};
          datasetUrl.value = response.data.data;

          if(isSCADA.value==true){
            store.commit('setDatasetUrl', datasetUrl.value);
          }else{
            store.commit('setCmsUrl', datasetUrl.value);
          }
        })
        .catch(() => {
          ElNotification({
            title: 'Warning',
            message: '上传失败',
            type: 'warning',
          });
        });
  }
};
</script>

<style scoped>

</style>
