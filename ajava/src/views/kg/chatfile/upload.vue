<template>
  <basic-container>
    <el-container >
      <el-header class="top-page">
        <span class="title">知识服务->知识文档管理</span>
        <span class="annotation">*页面运行请确保服务器已启动!</span>
      </el-header>
      <el-main :style="{background: 'white',height:'99%'}">

        <div style="margin-left:10px; padding: 20px;">
          <el-form :inline="true" style="font-size: 20px; display: flex; justify-content: space-between;align-items: center;">
            <!-- Left Side of the Form -->
            <div class="form-section">
              <el-form-item label="文档资源库选择:">
                <el-select v-model="curKgFile1" :popper-append-to-body="false" :placeholder="curKgFile1" style="width: 250px;" @change="handleKgFile1Change">
                  <el-option v-for="item in allFilebases" :key="item" :label="item" :value="item"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-upload
                    ref="uploadRef"
                    :action="uploadAction"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    :http-request="customRequest"
                    :on-remove="handleRemove"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError">
                  <el-button size="medium" type="success" icon="el-icon-upload" @click="manualUpload">
                  上传文件至当前文档资源库
                  <!-- 在按钮内显示加载图标 -->
                  <el-spinner v-if="loading" style="color: grey;"></el-spinner>
                </el-button>
                </el-upload>
              </el-form-item>
            </div>

            <!-- Right Side of the Form -->
            <div class="form-section">
              <el-form-item label="文档资源库创建:">
                <el-input v-model="newFileBase" placeholder="请输入文档库名称" style="width: 250px;"></el-input>
                <el-button size="medium" class="button-create" @click="confirmCreation" style="margin-left: 10px;">
                  <i class="el-icon-plus"></i> 确定新增
                </el-button>
              </el-form-item>
              <el-form-item label="文档资源库删除:">
                <el-select v-model="curKgFile2" :popper-append-to-body="false" :placeholder="curKgFile2" style="width: 250px;">
                  <el-option v-for="item in allFilebases" :key="item" :label="item" :value="item"></el-option>
                </el-select>
                <el-button size="medium" type="danger" @click="deletebase(curKgFile2)" style="margin-left: 10px;">
                  <i class="el-icon-delete"></i> 确定删除
                </el-button>
              </el-form-item>
            </div>
          </el-form>

        </div>
        <div id="meta-dict" style="width: 100%;margin-top: 15px">
          <el-main>
            <el-table :data="paginatedData" border style="width: 100%"  :header-cell-class-name="getRowClass"
                      :cell-class-name="cellStyle" :row-style="{ height:'50px'} ">
              <el-table-column min-width="50px" label="知识文件名称" prop="filename"></el-table-column>
              <el-table-column align="center" label="操作">
                <template #default="scope">
                  <el-button
                      size="medium"
                      icon="el-icon-download"
                      @click="downloadFile(scope.row.filename)">下载
                  </el-button>

                  <el-button
                      size="medium"
                      icon="el-icon-delete"
                      @click="deletefile(scope.row.filename)">删除
                  </el-button>
                </template>

              </el-table-column>
            </el-table>
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[5, 10, 20]"
                :page-size="pageSize"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper">
            </el-pagination>
          </el-main>
        </div>
      </el-main>
    </el-container>
  </basic-container>
</template>
<script setup>
import {ref, computed, watch, onMounted} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {getGMLData,changeGmlflag,getAlLGMLData,delgml} from "../../../api/kg/chat/chat";
import axios from "axios";
//知识库状态定义
// let allFilebases=ref(["全部","待审核","已通过","未通过"]);
//分页参数定义
let currentPage = ref(1);
let pageSize = ref(5);
let tableData = ref([]);
let total = computed(() => tableData.value.length);

let paginatedData = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return tableData.value.slice(startIndex, endIndex);
});

// 表示是否筛选，0表示展示全部,1表示展示已通过，2表示展示未通过，3表示展示待审核
let allflag = ref(0);
let curKgFile1 = ref("")
let curKgFile2 = ref("")
let allFilebases = ref([]);
let newFileBase = ref("");
let basename = ref("");
//上传加载
let loading = ref(false);
//页面初始化
onMounted(()=>{
  getallFilebases()
  curKgFile1.value="fengji"
  curKgFile2.value=""
  getfiles(curKgFile1.value)
})
function handleSizeChange(newSize) {
  pageSize.value = newSize;
  currentPage.value = 1; // Reset to first page to avoid index out of bounds
}
// Handle current page change
function handleCurrentChange(newPage) {
  currentPage.value = newPage;
}
//下载、删除文档开始
function downloadFile(filename) {
  console.log("尝试下载的文档是:", filename);
  const params = {
    knowledge_base_name: curKgFile1.value,
    file_name: filename
  };

  axios({
    method: 'get',
    url: '/chat/knowledge_base/download_doc',
    params: params,
    responseType: 'blob'  // 重要：告诉axios返回的数据类型
  }).then(response => {
        // 创建一个链接元素，用于下载
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', filename);  // 保存的文件名
        document.body.appendChild(link);
        link.click();
        link.parentNode.removeChild(link);
        ElMessage.success('文件开始下载');
      })
      .catch(error => {
        console.error('下载文件时发生错误:', error);
        ElMessage.error('文件下载失败: ' + error.message);
      });
}
//开始删除文文件逻辑

function deletefile(filename) {
  console.log("尝试删除的文档是:", filename);
  ElMessageBox.confirm(
      `您确定要删除文件 '${filename}' 吗？这个操作无法撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    // 用户确认删除
    const data = {
      "knowledge_base_name": curKgFile1.value,
      "file_names": [filename], // 确保这是一个数组
      "delete_content": false,
      "not_refresh_vs_cache": false
    };
    axios({
      method: 'post',
      url: '/chat/knowledge_base/delete_docs',
      data: data,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      }
    }).then(response => {
      getfiles(curKgFile1.value); // 刷新列表
      console.log('删除文件成功:', response);
      ElMessage.success('文件删除成功');
    }).catch(error => {
      console.error('删除文件时发生错误:', error);
      ElMessage.error('文件删除失败: ' + (error.response && error.response.data.message ? error.response.data.message : error.message));
    });

  }).catch(() => {
    // 用户取消删除
    ElMessage.info('取消删除');
  });
}


//下载、删除文档结束
const handleKgFile1Change = (newValue) => {
  console.log("Selected Knowledge Base: ", newValue);
  getfiles(newValue);
};

const uploadRef = ref(null);
const uploadAction = 'http://192.168.65.34:7861/knowledge_base/upload_docs';
// const curKgFile1 = ref('');
function beforeUpload(file) {
  // 这里可以加上文件检查逻辑，例如文件大小、格式等
  return true;
}
const handleUploadSuccess = async (response) => {
  // 上传成功处理逻辑删除
  // ElMessage.success('文件上传成功');
  await getfiles(curKgFile1.value); // 等待文件列表刷新
};
async function customRequest(options) {
  loading.value = true; // 开始上传时设置为true
  let loadingMessageInstance = ElMessage({ message: '文件正在上传并解析，请稍候...', duration: 0, type: 'info' }); // 显示持续的消息

  const formData = new FormData();
  formData.append('files', options.file);
  formData.append('knowledge_base_name', curKgFile1.value);
  formData.append('override', 'true');

  try {
    const response = await axios.post(uploadAction, formData, {
      headers: {
        'Accept': 'application/json',
      },
      timeout: 300000 // 设置超时时间为5分钟
    });
    console.log("response", response);

    if (response.data.code === 200) {
      options.onSuccess(response.data, options.file);
      loadingMessageInstance.close(); // 关闭消息
      ElMessage.success('文件上传并解析成功');
      // 这里可以添加轮询后端处理状态的逻辑
    } else {
      throw new Error('文件上传未成功');
    }
  } catch (error) {
    options.onError(error, options.file);
    loadingMessageInstance.close(); // 关闭消息
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('文件上传超时，请检查网络连接或文件大小。');
    } else {
      ElMessage.error('文件上传失败: ' + error.message);
    }
  } finally {
    loading.value = false; // 无论成功或失败，上传结束后设置为false
  }
}





function handleRemove(file, fileList) {
  // 文件移除逻辑
}


function handleUploadError(error, file, fileList) {
  // 文件上传错误处理
  ElMessage.error('文件上传失败');
}

function manualUpload() {
  if (!curKgFile1.value) {
    ElMessage.error('请先选择一个文档资源库');
    return;
  }
  uploadRef.value.submit(); // 触发上传
  // getfiles(curKgFile1.value)
}
// function beforeUpload(file) {
//   if (loading.value) {
//     ElMessage.warning('上一个文件还在上传，请稍后再试。');
//     return false;
//   }
//   return true;
// }

const submitUpload = () => {
  // 首先确保 curKgFile1 不为空
  if (!curKgFile1.value) {
    ElMessage.error('请先选择一个文档资源库');
    return;
  }
   console.log("fileList",fileList)
  // 检查是否选择了文件
  if (fileList.value.length === 0) {
    ElMessage.error('请先选择文件');
    return;
  }
  const file = fileList.value[0].raw;
  let formData = new FormData();
  formData.append('files', file);
  formData.append('knowledge_base_name', curKgFile1.value);
  formData.append('override', 'true');

   axios.post('http://192.168.65.34:7861/knowledge_base/upload_docs', formData, {
    headers: {
      'Accept': 'application/json',
    },
  }).then(response => {
        // 成功上传后的处理，可能还包括对文件的解析操作
        console.log('File uploaded successfully:', response);
        // ElMessage.success('文件上传成功');
        // 在这里调用文件解析的函数或逻辑
      })
      .catch(error => {
        // 上传失败处理逻辑
        console.error('Error during file upload:', error);
        // ElMessage.error('文件上传失败');
        ElMessage.info("文件正在上传，并进行向量化")
      });
};

const handleFileChange = (file, fileList) => {
  fileList.value = fileList; // 更新 fileList.value
  submitUpload(); // 文件选择后立即上传
};
//通过知识库名称查询对应的知识文件
async function getfiles(basename) {
  try {
    const response = await axios.get('/chat/knowledge_base/list_files', {
      params: {
        knowledge_base_name: basename
      }
    });
    tableData.value = response.data.data.map(file => ({
      filename: file, //
    }));
    console.log("tableData", tableData.value.length);
    total.value=tableData.value.length
    return response.data;
  } catch (error) {
    console.error(`Axios error! ${error.message}`);
    throw error;
  }
}


//删除对应的知识库操作，这里应该新增一个弹窗
const deletechose=function (item)
{
  console.log("选择的知识库名称为:",item)
  basename=item;
}
const deletebase = async (basename) => { // 将函数声明为异步
  console.log("请求删除的知识库名称为: ", basename);

  try {
    await ElMessageBox.confirm(
        `您确定要删除名称为 '${basename}' 的知识库吗？这个操作无法撤销。`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    );

    const response = await deletefilebase(basename); // 确保deletefilebase返回一个Promise
    if (response.data.code === 200) {
      await getallFilebases(); // 刷新知识库列表
      curKgFile1.value = ""; // 如果需要，重置当前选择
      curKgFile2.value = ""; // 清除删除选择
      ElMessage({
        type: 'success',
        message: response.data.msg,
      });
    } else {
      ElMessage({
        type: 'error',
        message: response.data.msg,
      });
    }
  } catch (error) {
    if (error !== 'cancel') { // 处理除取消MessageBox之外的错误
      console.error('删除知识库时发生错误:', error);
      ElMessage.error('删除知识库失败。');
    } else {
      ElMessage({
        type: 'info',
        message: '已取消删除'
      });
    }
  }
};



async function deletefilebase(basename)
{
  try {
    let param= basename;
    const response = await axios.post('/chat/knowledge_base/delete_knowledge_base', param,{
      headers: {
        'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
      }
    });
    console.log("删除草自拍",response.data)
    return response; // 返回响应数据
  } catch (error) {
    throw new Error(`Axios error! ${error.message}`); // 捕获错误并抛出
  }
}
const confirmCreation = () => {
  //这里应该加一个弹窗 进行确认
  console.log("新创建的文档库名称是: ", newFileBase.value);
  //开始向chatchat发送创建知识库请求
  createFilebase(newFileBase.value)
  //将对应的输入框取消掉
  newFileBase.value=""
  //更新最新的知识库
  // getallFilebases()
  // newFileBase=""
  // 这里可以添加代码来处理新创建的文档库，如发送到服务器
};
//向服务器chatchat发送请求
async  function getallFilebases()
{
  await getfilebases().then((r) => {
    console.log("响应：", r);
    //过滤掉一个默认的数据库
    const filteredData = r.data.filter(item => item !== 'samples' && item !== 'weixiufile');
    allFilebases.value= filteredData;
    console.log("allFilebases",allFilebases)
  })
}
async function getfilebases() {
  try {
    const response = await axios.get('/chat/knowledge_base/list_knowledge_bases', {
      headers: {
        'Content-Type': 'application/json' // 指定请求头的内容类型为 JSON
      }
    });
    console.log("filebases",response.data)
    return response.data; // 返回响应数据

  } catch (error) {
    throw new Error(`Axios error! ${error.message}`); // 捕获错误并抛出
  }
}
const createFilebase = async () => {
  let param = {
    "knowledge_base_name": newFileBase.value, // 使用.value来访问ref的值
    "vector_store_type": "faiss",
    "embed_model": "bge-large-zh"
  };

  try {
    const response = await axios.post('/chat/knowledge_base/create_knowledge_base', param, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    // ...处理响应...
    if (response.data.code === 200) {
      await getallFilebases(); // 创建后刷新列表
      newFileBase.value = ""; // 清空newFileBase的值
      ElMessage({
        type: "success",
        message: "知识库创建成功!"
      });
    } else {
      ElMessage({
        type: "error",
        message: response.data.msg
      });
    }
  } catch (error) {
    console.error("创建知识库时发生错误：", error);
    ElMessage({
      type: "error",
      message: `创建失败: ${error.message}`
    });
  }
};

async function createfilebase(param) {
  try {
    const response = await axios.post('/chat/knowledge_base/create_knowledge_base', param, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log("创建知识库", response.data);
    ElMessage({
      type: "success",
      message: "新增成功!",
    });
    return response.data; // 返回响应数据
  } catch (error) {
    ElMessage({
      type: "error",
      message: `创建失败: ${error.message}`,
    });
    throw new Error(`Axios error! ${error.message}`); // 捕获错误并抛出
  }
}




//获取后端数据


// 删除

//
// function  rendertable()
// {
//
// }
const getRowClass=function (row, rowIndex) {
  return "theadStyle";
}
// 绑定样式属性
const cellStyle=function (row, rowIndex) {
  return "tdStyle";
}

</script>

<style lang="scss" scoped>
.el-pagination {
  margin-top: 30px;
}

.form-section {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.button-create {
  background-color: #5cb85c; /* Green shade, adjust color as needed */
  color: white; /* White text color */
  border: none; /* No border */
}
.button-create:hover {
  background-color: #4cae4c; /* Slightly darker shade for hover */
}
.title {
  font-size: 24px;
  font-weight: bold;
}
.annotation {
  display: block; /* Makes the span take a full line */
  color: red; /* Sets the text color to red */
  font-size: 14px; /* Smaller font size */
  margin-top: 5px; /* Spacing above the annotation */
}
.el-spinner .el-icon-loading {
  color: blue; /* 设置为白色以与按钮颜色匹配 */
  font-size: 14px; /* 调整大小 */
}

.el-button {
  min-width: 120px; /* Ensures buttons have a uniform width */
}
</style>
