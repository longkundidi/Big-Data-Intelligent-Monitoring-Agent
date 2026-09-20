<template>
  <div v-loading="loading" class="fl-container">

    <div class="biaotou" style="position: relative;border-bottom: solid 1px #d9d9d9;height: 12vh">
      <div style="width:2%;height: 10%;position: absolute;padding-top: 2vh;padding-left: 1%">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>

      <span
          style="position: absolute;font-size: 22px;font-weight: bold;padding-top: 1%;padding-left:3.5%">实时状态评估模型</span>


      <div style="position: absolute;padding-left: 47%;padding-top: 4%;display: flex">
        <el-input style="padding-right: 10px" v-model="searchForm.modelObject" placeholder="请输入对象"/>
        <el-button style=" margin-right: 10px" class="searchBtn" @click="search1">搜索对象</el-button>

        <el-input style="padding-left: 30px;padding-right: 10px" v-model="searchForm.modelName"
                  placeholder="请输入模型"/>
        <el-button class="searchBtn" size="large" @click="search2">搜索模型</el-button>

        <el-button class="reSetBtn" @click="reset">刷新</el-button>

        <el-button @click="add" style="margin-left:50px" class="registerBtn">模型注册
        </el-button>
      </div>
    </div>

    <div>
      <el-row style="padding-top: 0!important;" class="el-main">
        <el-col v-for="item in this.list" :span="8" style="height: 470px;padding: 15px 15px 50px 15px">
          <el-card style="border:1px solid #d9d9d9;height: 110%;" shadow="hover">

            <div class="el-card__header" style="padding:  5px 0px 20px 0px;position: relative; height: 130px; ">

              <div style="position: absolute;width: 23%;height:80%;padding-left: 3%">
                <el-image v-if="item.modelIcon!==null"
                          style="width: 100%;height:100%"
                          :src="'/api/al/file/'+item.modelIcon"></el-image>
                <el-image v-else
                          style="width: 100%;height:100%">
                  <template #error>
                    <div class="image-slot" style="width: 100%;height:100%">
                      <img src="@/assets/upload.png" style="width: 100%;height:100%">
                    </div>
                  </template>
                </el-image>
              </div>


              <div style="padding-left: 30%; position: absolute;"><span
                  style="font-size: 25px;color:#444447;font-weight: bold">{{ item.modelName }}</span></div>
              <div style="padding-left: 30%;padding-top:60px;position: absolute;width: 100%">
                <div style="margin-top: 10px">
                  <span style="font-weight: bold;padding-top: 5px">引用次数：{{ item.modelNum }}次</span>
                </div>

                <div style="padding-left: 50%">
                  <el-button type="text" @click="addOrUpdateHandle(item.id,item.modelType)">详情</el-button>
                  <el-button type="text" @click="deleteHandle(item.id,item.modelType)">删除</el-button>
                </div>
              </div>
            </div>

            <div class="el-card__body" style="height: 100%;padding: 0!important">
              <div class="wrapper">
                <el-row class="table-col">
                  <el-col :span="12" class="table-item">
                    <span class="label">提供人员</span>
                    <span class="value">{{ item.modelProvider }}</span>
                  </el-col>

                  <el-col :span="12" class="table-item">
                    <span class="label">提供时间</span>
                    <span class="value">{{ item.createTime }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="12" class="table-item">
                    <span class="label">模型类型</span>
                    <span class="value">{{ item.modelType }}</span>
                  </el-col>
                  <el-col :span="12" class="table-item">
                    <span class="label">应用服务</span>
                    <span class="value">{{ item.modelFunction }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">适用场景</span>
                    <span class="value">{{ item.modelCondition }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">模型优点</span>
                    <span class="value">{{ item.modelAdvantage }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">模型缺点</span>
                    <span class="value">{{ item.modelDisadvantage }}</span>
                  </el-col>
                </el-row>

                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">模型操作</span>
                    <div class="value">
                      <el-button v-if="item.isPass === 0 && item.isDeployed === 0 && item.isService===0 " style="padding: 7px!important;color: #46a6ff" size="mini"
                                 plain @click="altest(item.id,item.input,item.output,item.isjson)">模型测试
                      </el-button>
                      <el-tooltip class="item" effect="dark" content="该模型已被停止使用,请先开启模型使用权限再测试">
                        <el-button v-if="item.isPass === 0 && item.isDeployed === 0 && item.isService===1" style="padding: 7px!important;color: grey" plain size="mini"
                                   disabled  @click="altest(item.id,item.input,item.output,item.isjson)">模型测试
                        </el-button>
                      </el-tooltip>
                      <el-button v-if="item.isPass === 0 && item.isDeployed === 0 " style="padding: 7px!important;color: #46a6ff" size="mini"
                                 plain @click="allog(item.id)">测试日志
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="el-footer" style="height: 10%;float: right;padding-right: 2%">
        <el-form :inline="true">

          <el-form-item>
            <el-pagination
                v-if="pageshow"
                class="wl-pagination"
                background
                layout="total, prev, pager, next"
                :current-page="currentPage"
                :page-sizes="[6, 10, 15, 20, 50, 100, 150, 200]"
                :page-size="pageSize"
                :total="count"
                @current-change="currentChangeHandle"
            />
          </el-form-item>
        </el-form>
      </div>

      <table-form1 v-if="addOrUpdateVisible1" ref="addOrUpdate1" @refreshDataList="getDataList"/>
      <table-form2 v-if="addOrUpdateVisible2" ref="addOrUpdate2" @refreshDataList="getDataList"/>
      <altest v-if="altestVisible" ref="altest" @refreshDataList="getDataList"/>
      <allog v-if="allogVisible" ref="allog" @refreshDataList="getDataList"/>
      <add v-if="addVisible" ref="add" @refreshDataList="refreshData"/>
      <altestnotjson v-if="altestnotjsonVisible" ref="altestnotjson" @refreshDataList="getDataList"/>

    </div>
  </div>
</template>

<script>
import {
  fetchList,
  delEvaluationObj,
  fetchList1,
  fetchList2
} from '/src/api/al/stateEvaluation/stateEvaluationBase'
import TableForm1 from './stateEvaluation-xiangqing1.vue'
import TableForm2 from './stateEvaluation-xiangqing2.vue'
import add from './stateEvaluation-add.vue'
import altest from './stateEvaluation-altest.vue'
import allog from './stateEvaluation-allog.vue'
import altestnotjson from './stateEvaluation-altest-notjson.vue'

export default {
  components: {
    TableForm1,
    TableForm2,
    add,
    altest,
    allog,
    altestnotjson
  },
  data() {

    return {
      searchForm: {
        modelTypeFirst: '数字孪生应用算法集',
        modelFunction: '实时状态评估',
        modelName: ''
      },
      allogVisible: false,
      loading: false,
      addOrUpdateVisible1: false,
      addOrUpdateVisible2: false,
      addVisible: false,
      formVisible: false,
      altestVisible: false,
      altestnotjsonVisible: false,
      list: [],
      count: 6,
      pageSize: 6,
      currentPage: 1,
      pageshow: true,
      activeName: 'first',
    }
  },
  mounted() {
    if (this.$route.query.modelTypeFirst === undefined) {
      this.getDataList()
    } else {
      this.searchForm.modelTypeFirst = this.$route.query.modelTypeFirst
      if (this.searchForm.modelTypeFirst === "动力学仿真模型") {
        this.activeName = "first"
      }
      if (this.searchForm.modelTypeFirst === "强度仿真模型") {
        this.activeName = "second"
      }
      if (this.searchForm.modelTypeFirst === "噪声仿真模型") {
        this.activeName = "third"
      }
      if (this.searchForm.modelTypeFirst === "流体仿真模型") {
        this.activeName = "fourth"
      }
      this.getDataList()
    }
  },

  methods: {
    handleClick(tab, event) {
      this.currentPage = 1
      this.getDataList()
    },

    allog(id) {
      this.allogVisible = true
      this.$nextTick(() => {
        this.$refs.allog.init(id)
      })
    },

    altest(id, input, output, isjson) {
      if (isjson === 0) {
        this.altestVisible = true
        this.$nextTick(() => {
          this.$refs.altest.init(id, input, output, isjson)
        })
      }
      if (isjson === 1) {
        this.altestnotjsonVisible = true
        this.$nextTick(() => {
          this.$refs.altestnotjson.init(id, input, output, isjson)
        })
      }
    },

    refreshData() {
      this.getDataList()
    },

    getDataList(page, params) {
      fetchList(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm)).then(response => {
        this.list = response.data.data.records
        this.count = response.data.data.total
      })
    },

    // 当前页
    currentChangeHandle(val) {
      this.currentPage = val
      this.getDataList()
    },

    deleteHandle(id, modelType) {
      this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        return delEvaluationObj(id)
      }).then(data => {
        this.$message.success('删除成功')
        this.getDataList()
      }).catch(() => {
      })
    },

    addOrUpdateHandle(id, modelType) {
      if (modelType === '传统机器学习模型' || modelType === '深度学习模型') {
        this.addOrUpdateVisible1 = true
        this.$nextTick(() => {
          this.$refs.addOrUpdate1.init(id)
        })
      }
      if (modelType === '统计学模型' || modelType === '信号处理模型') {
        this.addOrUpdateVisible2 = true
        this.$nextTick(() => {
          this.$refs.addOrUpdate2.init(id)
        })
      }
    },

    add() {
      this.addVisible = true
      this.$nextTick(() => {
        this.$refs.add.init()
      })
    },

    reset() {
      this.getDataList()
    },

    search1() {
      this.currentPage = 1
      this.getDataList1()
    },

    search2() {
      this.currentPage = 1
      this.getDataList2()
    },

    getDataList1(page, params) {
      fetchList1(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm)).then(response => {
        this.list = response.data.data.records
        this.count = response.data.data.total
      })
    },

    getDataList2(page, params) {
      fetchList2(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm)).then(response => {
        this.list = response.data.data.records
        this.count = response.data.data.total
      })
    },
  }
}
</script>

<style lang="scss" scoped>
.wrapper {
  margin-top: 5%;
  width: 100%;
  border: solid 1px #d9d9d9;

  .table-col:not(:last-child) {
    border-bottom: solid 1px #d9d9d9;
  }

  .table-item:not(:last-child) {
    border-right: solid 1px #d9d9d9;
  }

  .table-col {
    height: 100%;
    display: flex;
  }

  .table-item {
    height: 100%;
    display: flex;

    > div {
      display: flex;
    }

    .label {
      display: flex;
      background: #FAFAFA;
      color: #909399;
      padding: 10px 7px;
      border-right: solid 1px #d9d9d9;
    }

    .value {
      justify-content: center;
      width: 100%;
      display: flex;
      align-items: center;
      word-wrap: break-word;
      color: #606266;
      font-size: 14px;
      flex: 1;
    }
  }
}


.el-main {
  min-height: 80vh;
}

.fl-container {
  width: 100%;
  height: 95vh;
}

.fl-container.main {
  margin: 20px;
}

.wl-pagination {
  margin-top: 5px;
}

.fl-container.el-form-item {
  margin-bottom: 25px !important;
}

.wl-table.el-link [class*=el-icon-] + span {
  margin-left: 1px;
}

.wl-table.el-link {
  margin-right: 2px;
  margin-left: 2px;
}

.el-card__header {
  height: 100px
}

.searchBtn {
  min-width: 95px;
  height: 30px;
}

.registerBtn {
  min-width: 95px;
  height: 30px;
}

.reSetBtn {
  height: 30px;
}

</style>
