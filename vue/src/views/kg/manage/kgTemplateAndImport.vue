<template>
  <basic-container>
  <el-container>
    <el-header class="top-page">
      <span class="title">知识获取管理->知识导入</span>
      <div style="margin-left:50px; float:right">
        <el-button
            type="primary"
            icon="el-icon-circle-plus-outline"
            class="add"
            style="float: left; margin-right: 10px; height: 40px; margin-bottom: 30px; width: 120px"
            @click="downloadTemplateExcel()"
        >下载本体模板
        </el-button>
        <el-upload
            :key="uploadkey"
            class="upload-demo"
            :headers="headers"
            action="/api/kg/kgknowledgeupload/upload"
            accept="csv"
            multiple
            :limit="1"
            :on-success="upload"
            :on-error="handleFileError"
            :show-file-list="false"
            style="float: left; margin-right: 0px "
        >

          <el-button
              type="primary"
              icon="el-icon-circle-plus-outline"
              class="add"
              style="float: left; margin-right: 0px; height: 40px; margin-bottom: 30px; width: 120px"
          >上传知识文件
          </el-button>
        </el-upload>
      </div>
    </el-header>
    <el-main :style="{background:'white',height:'99%'}">
      <div class="concept-main-frametype">
        <el-table
            :data="tableData"
            border
            style="width: 100%"
            :header-cell-class-name="getRowClass"
            :cell-class-name="cellStyle"
            :row-style="{ height: '50px' }"
            id="outTable"
        >
          <!--          <div v-for="data in tableData" :key="data" class="model_div">{{ data.struct}}</div>-->
          <el-table-column prop="center" align="center" label="序号" type="index" width="60">
          </el-table-column>
          <el-table-column prop="ontologyName" align="center" show-overflow-tooltip label="本体名称" >
          </el-table-column>
          <el-table-column prop="uploadName" align="center" label="上传知识文件名称" >
          </el-table-column>

          <el-table-column prop="tempFile"  align="center" show-overflow-tooltip label="知识文件下载">
            <template #default="{row}">
              <el-button
                  type="primary"
                  icon="el-icon-download"
                  class="add"
                  size="small"
                  @click="downloadToExcel(row.uploadFile)"
              >
                下载
              </el-button>
            </template>
          </el-table-column>

          <el-table-column prop="uploadPerson" align="center" label="上传人员" >
          </el-table-column>
          <el-table-column prop="uploadTime"  align="center" label="上传时间">
          </el-table-column>


        </el-table>
        <!--分页-->
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            style="margin-top: 20px;"
        ></el-pagination>
        <!--        <au-dlg ref="auDlg" @afterCommit="afterCommit"></au-dlg>-->
        <el-dialog
            v-model="visible"
            title="文件预览"
            :before-close="fileClose"
            width="1000px"
            center
        >
          <div v-loading="reviewLoading" element-loading-text="正在审核，请稍候...">
            <div class="el-dialog-div">
              <iframe :src='kkfileViewUrl' style="width: 100%;height: 100%"></iframe>
            </div>
            <div slot="footer"
                 class="dialog-footer"
                 style="text-align: center"
            >
              <el-button class="cancelbtn"
                         @click="checkNo()">上传取消</el-button>
              <el-button class="determinebtn"
                         @click="toCheck()">去审核</el-button>
            </div>
          </div>
        </el-dialog>
        <el-dialog
            title="领域字典审核"
            v-model="reviewVisible"
            width="80%"
            append-to-body
            :before-close="reviewClose"
        >
          <div v-loading="uploadLoading" element-loading-text="字典更新完成，正在保存数据到数据库...">
            <template v-if="instanceList.length > 0">
              <template v-for="(instance, index) in instanceList">
                <h2>{{ `${instance}` }}</h2>
                <el-table
                    class="reviewClass"
                    :data="reviewData[instance]"
                    :border="true"
                    :stripe="true"
                    fit
                    style="width: 100%"
                    height="250"
                    :header-cell-class-name="getRowClass"
                    :cell-class-name="cellStyle"
                    :row-style="{ height: '100px' }">
                  <el-table-column fixed prop="reviewword" align="center" label="待审核词">
                  </el-table-column>
                  <el-table-column prop="word" align="center" label="基准词分数">
                  </el-table-column>
                  <el-table-column prop="synonyms" align="center" label="同义词分数">
                  </el-table-column>
                  <el-table-column prop="radio" align="center" label="操作" width="250">
                    <template #default="scope">
                      <el-radio-group v-model="scope.row.radio" size="small">
                        <el-radio-button :label="1" size="small" border :disabled="scope.row.disabled">添加为新词语
                        </el-radio-button>
                        <el-radio-button :label="2" size="small" border :disabled="scope.row.disabled">添加为同义词
                        </el-radio-button>
                        <el-radio-button :label="3" size="small" border :disabled="scope.row.disabled">使用基准词语
                        </el-radio-button>
                      </el-radio-group>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </template>
            <template v-else>
              <div class="no-instances-text">没有需要进行审核的实例</div>
            </template>
            <div slot="footer"
                 class="dialog-footer"
                 align="center">
              <el-button class="cancelbtn"
                         @click="reviewClose">取 消
              </el-button>
              <el-button class="determinebtn"
                         @click="checkFinish">审核完成
              </el-button>
            </div>
          </div>
        </el-dialog>
      </div>
    </el-main>
  </el-container>
  </basic-container>
</template>

<script>
import {
  csvDatatoReview,
  deleteByKgUploadId, getListAll,getTemplateUrl,
  saveFieldWordList,
  saveSynonymList,
  updateByFile
} from "@/api/kg/manage/kgknowledgeupload";
import {ElMessage, ElMessageBox} from "element-plus";
import store from '@/store';
import {Base64} from "js-base64";
import {checkTaskStatus, createTask} from "@/api/kg/manage/taskStatus";
import { minioUrl} from '@/config/env.js'

export default {
  data() {
    return {
      uploadkey: '0',
      structType: '',
      visible: false,
      dataForm: {key: ""},
      tableData: '',
      pageNum: '1',
      pageSize: '10',
      total: '10',
      UploadId: '',
      fileurl: minioUrl,
      csvUrl: '',
      kkfileViewUrl: '',
      kkfileUrl: 'http://192.168.16.216:8012/',
      updateByFileUploadName: '',
      updateByFileUploadFile: '',
      instanceList: {},
      reviewData: {},
      reviewVisible: false,
      benchmarkInfoDict: {},
      checkResult: {},
      reviewLoading: false,
      index: '',
      uploadLoading: false,
      taskStatus: '',
      ontologyName: '',
      headers: {
        Authorization: 'Bearer ' + store.getters.access_token
        }
    }
  },
  mounted() {
    //得到左侧结构树
    this.structType=store.getters.structTypeInfo;
    if(this.isEmpty(this.structType)){
      this.structType="风电装备元结构树"
    }//没有则默认采用风电装备元结构树
    //console.log("111： ", typeof this.reviewVisible)
    this.renderTable();
    this.ontologyName = "维修知识本体";
  },
  methods: {
    // 渲染表格
    renderTable() {
      getListAll(this.pageNum, this.pageSize).then((r) => {
        this.tableData = r.data.data.records;
        this.total = r.data.data.total;
      });
    },
    // 翻页
    handleCurrentChange(current) {
      this.pageNum = current;
      this.renderTable();
    },
    // 重置页面大小，每次重置会回到第一页
    handleSizeChange(newSize) {
      this.pageNum = 1;
      this.pageSize = newSize;
      this.renderTable();
    },
    del(uploadId) {
      ElMessageBox.confirm("是否删除?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        deleteByKgUploadId(uploadId)
            .then(() => {
              renderTable();
              ElMessage({
                type: "success",
                message: "删除成功!",
              });
            })
            .catch(() => {
              ElMessage({
                type: "info",
                message: "删除失败",
              });
            });
      })
          .catch(() => {
            ElMessage({
              type: "info",
              message: "已取消删除",
            });
          });
    },
    // 定义表头单元格的style的回调方法
    getRowClass(row, rowIndex) {
      return "theadStyle";
    },
    // 绑定样式属性
    cellStyle(row, rowIndex) {
      return "tdStyle";
    },
    //region 实现本体模板文件下载功能
    downloadTemplateExcel() {
      getTemplateUrl(this.ontologyName).then(r => {
        if (r.data.code === 0) {
          let ontologyTemplateUrl = r.data.data;
          let timestamp = "?timestamp=1234567890";
          window.open(this.fileurl + ontologyTemplateUrl + timestamp, '_blank');
        } else {
          ElMessage({
            type: "error",
            message: `错误信息是${r.data.msg}!`,
          });
        }
      })
    },
    //region 实现用户上传本体文件下载功能
    downloadToExcel(uploadFile){
      console.log("tempFile的信息是:", uploadFile)
      window.open(this.fileurl + uploadFile, '_blank');
      console.log("minioUrl的信息是:", minioUrl)

    },
    handleFileError(error){
      ElMessage({
        type: "error",
        message: `错误信息是${error.message}!`,
      });
    },
    //upload执行成功就上传文件到file_info表中,上传取消是控制用户上传修改后的模板文件
    upload(response, file) {
      this.uploadkey = Math.random();  //重新创建upload
      this.csvUrl = this.fileurl + response.data.url;
      this.UploadId = response.data.md5;
      this.visible = true;
      this.updateByFileUploadFile = response.data.url;
      this.updateByFileUploadName = file.name;
      this.show_kkfileView(this.csvUrl);
    },
    reviewClose() {
      this.reviewData = {};
      this.reviewVisible = false;
    },
    async toCheck() {
      try {
        this.reviewLoading = true;
        this.taskStatus = "inProcess";
        const taskRespond = await createTask();
        const taskId = taskRespond.data.data;
        const temp = csvDatatoReview({
          ontologyName: this.ontologyName, id: this.UploadId, uploadName: this.updateByFileUploadName,
          uploadFile: this.updateByFileUploadFile
        }, taskId);
        console.log("temp: ", temp)
        await this.checkReviewStatus(taskId);
      } catch (error) {
        this.taskStatus = "falseProcess";
        console.error("后端请求出错：", error)
        this.reviewLoading = false;
      }
      ;
    },
    lastStr(str) {
      this.index = str.lastIndexOf(":");
      str = str.substring(0, this.index);
      return str;
    },
    async checkFinish() {
      for (const instance in this.reviewData) {
        let tabledata = this.reviewData[instance];
        let wordList = [];
        let synonymList = [];
        for (let i in tabledata) {
          if (tabledata[i].radio === 1) { //添加领域字典
            wordList.push(tabledata[i].reviewword)
            if (wordList.length >= 200) {
              await saveFieldWordList({
                wordList: wordList,
                structType: this.structType,
                instanceType: instance
              }).then(r => {
                if (r.data.code === 0) {
                  ElMessage({
                    type: "success",
                    message: `已导入${wordList.length}条词汇到字典!`,
                  });
                  wordList = []
                } else {
                  ElMessage({
                    type: "error",
                    message: "批量添加领域字典失败!",
                  });
                }
              })
            }
          } else if (tabledata[i].radio === 2) { //添加到同义词
            synonymList.push({
              word: tabledata[i].reviewword,
              benchmarkWordId: this.benchmarkInfoDict[this.lastStr(tabledata[i].word)]
            })
            if (synonymList.length >= 100) {
              await saveSynonymList({
                synonymList: synonymList,
                structType: this.structType,
                instanceType: instance,
              }).then(r => {
                if (r.data.code === 0) {
                  ElMessage({
                    type: "success",
                    message: `已导入${synonymList.length}条词汇到同义词字典!`,
                  });
                  synonymList = []
                } else {
                  ElMessage({
                    type: "error",
                    message: "批量添加同义词失败!",
                  });
                }
              })
            }
            this.checkResult[tabledata[i].reviewword] = this.lastStr(tabledata[i].word)
          } else {
            this.checkResult[tabledata[i].reviewword] = this.lastStr(tabledata[i].word)
          }
        }
        if (wordList.length > 0) {
          await saveFieldWordList({
            wordList: wordList,
            structType: this.structType,
            instanceType: instance
          }).then(r => {
            if (r.data.code === 0) {
              ElMessage({
                type: "success",
                message: `已导入${wordList.length}条词汇！`,
              });
            } else {
              ElMessage({
                type: "error",
                message: "批量添加领域字典失败",
              });
            }
          })
        }
        if (synonymList.length > 0) {
          await saveSynonymList({
            synonymList: synonymList,
            structType: this.structType,
            instanceType: instance
          }).then(r => {
            if (r.data.code === 0) {
              ElMessage({
                type: "success",
                message: `已导入${synonymList.length}条词汇到同义词字典！`,
              });
            } else {
              ElMessage({
                type: "error",
                message: "批量添加同义词失败",
              });
            }
          })
        }
      }
      await this.checkPass();
    },
    //endregion

    //region 保存数据
    async checkPass() {
      try {
        this.uploadLoading = true;
        this.taskStatus = "inProcess";
        const taskRespond = await createTask();
        const taskId = taskRespond.data.data;
        const temp = updateByFile(
            {
              ontologyName: this.ontologyName, id: this.UploadId, uploadName: this.updateByFileUploadName,
              uploadFile: this.updateByFileUploadFile, structType: this.structType, checkResult: this.checkResult, taskId: taskId
            });
        await this.checkInteractionStatus(taskId);
      } catch (error) {
        this.taskStatus = "falseProcess";
        console.error("后端请求出错：", error)
        this.uploadLoading = false;
      }
    },

    //region 交互状态查询
    async checkInteractionStatus(taskId) {
      let waitTime = 0;
      while (this.taskStatus === "inProcess") {
        //轮询后端交互状态
        const response = await checkTaskStatus(taskId);
        if (response.data.data.status === 'endProcess') {
          this.taskStatus = 'endProcess';
          this.uploadLoading = false;
          await ElMessage({
            type: "success",
            message: "已保存到数据库，正在重新加载...",
            duration: 3000,
          });
          await this.reviewClose();
          await this.fileClose();
        } else if (response.data.data.status === 'inProcess') {
          await new Promise(resolve => setTimeout(resolve, 5000)); //等待5秒后再轮询
          waitTime = waitTime + 5;
          await ElMessage({
            type: "info",
            message: `数据保存中，已加载${waitTime}秒。`,
            duration: 2000,
          });
        } else {
          await ElMessage({
            type: "error",
            message: "保存数据出错，请刷新并重新上传！",
            duration: 3000,
          });
          this.taskStatus = "falseProcess";
          this.uploadLoading = false;
        }
      }
    },
    async checkReviewStatus(taskId) {
      let waitTime = 0;
      while (this.taskStatus === "inProcess") {
        //轮询后端交互状态
        console.log("taskId:",taskId)
        console.log("taskId type :",typeof taskId)
        const response = await checkTaskStatus(taskId);
        if (response.data.data.status === 'endProcess') {
          this.taskStatus = 'endProcess';
          this.reviewLoading = false;
          const r = JSON.parse(response.data.data.result);
          console.log("r: ", r)
          if (r === null || r === "") {
            ElMessage({
              type: "error",
              message: "上传文件未成功!",
            });
          } else if (r.reviewStruct !== undefined && r.reviewStruct !== null) {
            let notExist = JSON.stringify(r.reviewStruct);
            await ElMessageBox.alert(`存在不在元结构树的结构节点${notExist}！请将节点添加到元结构树中或者修改上传的excel文件后重新上传！`, '警告', {
              confirmButtonText: 'OK',
            });
            ElMessage({
              type: "error",
              message: "上传文件未成功!",
            });
          } else {
            let result = r;
            console.log("result:结果",r)
            this.instanceList = Object.keys(result);
            for (const j in this.instanceList) {
              let instancename = this.instanceList[j];
              let reviewInfo = result[instancename];
              let databyinstance = [];
              for (const reviewword in reviewInfo) {
                if (reviewInfo[reviewword].benchmarkWord === 0) { //只能新增
                  databyinstance.push({
                    "reviewword": reviewword,
                    "word": "字典内无相近词",
                    "synonyms": "无同义词",
                    "radio": 1,
                    "disabled": true,
                  })
                } else if (reviewInfo[reviewword].benchmarkWord === 1) { //只能使用基准词语
                  databyinstance.push({
                    "reviewword": reviewword,
                    "word": reviewword + ":1",
                    "synonyms": "无同义词",
                    "radio": 3,
                    "disabled": true,
                  })
                } else {
                  let benchmarkWordInfo = reviewInfo[reviewword].benchmarkWord;
                  let wordId = "";
                  let benchword = "";
                  for (const key in benchmarkWordInfo) {
                    if (key === "wordId") {
                      wordId = benchmarkWordInfo[key]
                    } else {
                      benchword = key;
                    }
                  }
                  this.benchmarkInfoDict[benchword] = wordId;
                  //设置阈值：分数>0.9->基准词；分数0.6~0.9->同义词；分数<0.6->新词
                  let grade = benchmarkWordInfo[benchword]
                  if (grade > 0.9) {
                    databyinstance.push({
                      "reviewword": reviewword,
                      "word": benchword + ":" + benchmarkWordInfo[benchword],
                      "synonyms": JSON.stringify(reviewInfo[reviewword].synonyms) === "{}" ? "无同义词" : JSON.stringify(reviewInfo[reviewword].synonyms),
                      "radio": 3,
                      "disabled": false,
                    })
                  } else if (grade < 0.6) {
                    databyinstance.push({
                      "reviewword": reviewword,
                      "word": benchword + ":" + benchmarkWordInfo[benchword],
                      "synonyms": JSON.stringify(reviewInfo[reviewword].synonyms) === "{}" ? "无同义词" : JSON.stringify(reviewInfo[reviewword].synonyms),
                      "radio": 1,
                      "disabled": false,
                    })
                  } else {
                    databyinstance.push({
                      "reviewword": reviewword,
                      "word": benchword + ":" + benchmarkWordInfo[benchword],
                      "synonyms": JSON.stringify(reviewInfo[reviewword].synonyms) === "{}" ? "无同义词" : JSON.stringify(reviewInfo[reviewword].synonyms),
                      "radio": 2,
                      "disabled": false,
                    })
                  }
                }
              }
              this.reviewData[instancename] = databyinstance;
            }
            this.reviewVisible = true;
          }
        } else if (response.data.data.status === 'inProcess') {
          await new Promise(resolve => setTimeout(resolve, 5000)); //等待5秒后再轮询
          waitTime = waitTime + 5;
          await ElMessage({
            type: "info",
            message: `数据审核中，已加载${waitTime}秒。`,
            duration: 2000,
          });
        } else {
          await ElMessage({
            type: "error",
            message: "审核数据出错，请刷新并重新审核！",
            duration: 3000,
          });
          this.taskStatus = "falseProcess";
          this.reviewLoading = false;
        }
      }
    },

    //endregion
    show_kkfileView(pathUrl) {
      this.kkfileViewUrl = this.kkfileUrl + 'onlinePreview?url=' + encodeURIComponent(Base64.encode(pathUrl))
    },
    fileClose() {
      this.renderTable()
      this.visible = false;
    },
    checkNo() {
      this.fileClose()
      ElMessage({
        type: "info",
        message: "文件上传取消",
      });
    },
    afterCommit() {
      this.renderTable();
    },
    //判断是否为空的办法
    isEmpty(obj) {
      return typeof obj == "undefined" || obj == null || obj === "";
    },
  }
}
</script>


<style lang="scss" scoped>

.title {
  font-size: 20px;
  font-weight: bold;
}
.add {
  font-size: 16px;
  font-weight: bold;
  background-color: rgba(0, 181, 164);
  border-color: rgba(0, 181, 164) !important; /* 使用 !important 覆盖其他样式 */
}

.no-instances-text {
  color: #2EC2B4;
  font-size: 20px;
}

.main-frametype {
  padding: 15px 20px;

  .top-page {
    background-color: #c8c7c7;
    background-size: cover;
    box-shadow: 2px 2px 1px #9c9c9c;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    justify-content: space-between;
    height: 60px;
    line-height: 60px;
    margin-bottom: 5px;
    border-bottom: 1px solid #ddd6d6;

  }

  .top-text {
    color: #070707;
    font-size: 22px;
    font-weight: bold;
    position: relative;
  }

  .formtop {
    display: flex;
    justify-content: space-between;
  }

  .p-btn-edit {
    border: 1px solid #6ca2f9;
    line-height: 16px;
    background: #0570cf;
    height: 16px;
    padding: 0 14px !important;
    color: #fff;

    &:hover {
      background: #0570cf;
      color: #fff;
    }
  }

  .p-btn-add {
    border: 1px solid #6ca2f9;
    line-height: 16px;
    background: #0570cf;
    height: 16px;
    padding: 0 14px !important;
    margin-bottom: 5px;
    color: #fff;

    &:hover {
      background: #0570cf;
      color: #fff;
    }
  }

  .p-btn-del {
    border: 1px solid #ff98a6;
    line-height: 16px;
    background: #cf4d75;
    height: 16px;
    padding: 0 14px !important;
    color: #fff;

    &:hover {
      background: #cf4d75;
      color: #fff;
    }
  }
}
.el-dialog-div{
  height: 800px;
  overflow: auto;
}
.el-radio-button {
  display: block;
}
.reviewClass {
  margin-top: 10px;
  margin-bottom: 10px;
}

.concept-main-frametype {
  background: white;
  padding: 10px;
  margin: 10px;
}

</style>
