<template>
  <div v-loading="loading" class="fl-container">

    <div v-if="true" class="biaotou" style="position: relative;border-bottom: solid 1px #d9d9d9;height: 12vh">
      <div style="width:2%;position: absolute;padding-top: 2vh;padding-left: 1%">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>

      <span
          style="position: absolute;font-size: 22px;font-weight: bold;padding-top: 1%;padding-left:3.5%">数据挖掘算法</span>
    </div>

    <div v-if="true">
      <el-row style="" class="el-main">
        <el-col v-for="item in this.list" :span="8" style="height: 380px;padding: 15px">
          <el-card style="border:1px solid #d9d9d9;height: 100%;" shadow="hover">
            <div class="el-card__header" style="padding:  5px 0px 20px 0px;position: relative;">

              <div style="position: absolute;width: 40%;height:70%;padding-top:2%">
                <el-image v-if="item.iconUrl!==null"
                          style="width: 100%;height:100%"
                          :src="'/api/al/file/'+item.iconUrl"></el-image>
                <el-image v-else
                          style="width: 100%;height:100%">
                  <template #error>
                    <div class="image-slot" style="width: 100%;height:100%">
                      <img src="@/assets/upload.png" style="width: 80%;height:100%">
                    </div>
                  </template>
                </el-image>
              </div>

              <div style="padding-top: 4%;padding-left: 50%;position: absolute">
                <span style="font-size: 27px;color:#444447; font-weight: bold">{{ item.alName }}</span>
              </div>
              <div style="padding-left: 55%;padding-top:60px;position: absolute;width: 100%">
                <span style="font-weight: bold;color:#141415; font-size: 15px">引用次数：{{ item.alNum }}次</span>
                <div style="padding-left: 25%">
                  <el-button type="text" @click="addOrUpdateHandle(item.id)">修改</el-button>
                  <el-button type="text" @click="deleteHandle(item.id)">删除</el-button>
                </div>
              </div>
            </div>

            <div class="el-card__body" style="height: 100%;padding: 0!important">
              <div class="wrapper">
                <el-row class="table-col">
                  <el-col :span="12" class="table-item">
                    <span class="label">提供人员</span>
                    <span class="value">{{ item.creator }}</span>
                  </el-col>
                  <el-col :span="12" class="table-item">
                    <span class="label">提供时间</span>
                    <span class="value">{{ item.createTime }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">算法适用</span>
                    <span class="value">{{ item.alSuit }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">算法简介</span>
                    <span class="value">{{ item.alBrief }}</span>
                  </el-col>
                </el-row>
                <el-row class="table-col">
                  <el-col :span="24" class="table-item">
                    <span class="label">算法操作</span>
                    <div class="value">
                      <el-button style="padding: 7px!important;color: #46a6ff"  plain
                                 @click="seeExample(item.iconUrl)">算法原理图
                      </el-button>
                      <el-button v-if="item.isPass === 0 && item.isDeployed === 0 && item.isService===0" style="padding: 7px!important;color: #46a6ff" plain
                                 @click="altest(item.id,item.input,item.output)">算法测试
                      </el-button>
                      <el-tooltip class="item" effect="dark" content="该算法已被停止使用,请先开启算法使用权限再测试">
                        <el-button v-if="item.isPass === 0 && item.isDeployed === 0 && item.isService===1" style="padding: 7px!important;color: grey" plain
                                   disabled  @click="altest(item.id,item.input,item.output)">算法测试
                        </el-button>
                      </el-tooltip>
                      <el-button v-if="item.isPass === 0 && item.isDeployed === 0 " style="padding: 7px!important;color: #46a6ff"
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

      <table-form v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="refreshData"/>
      <add v-if="addVisible" ref="add" @refreshDataList="refreshData"/>
      <example v-if="exampleVisible" ref="example"/>
      <altest v-if="altestVisible" ref="altest" @refreshDataList="refreshData"/>
      <allog v-if="allogVisible" ref="allog" @refreshDataList="refreshData"/>
    </div>

  </div>
</template>

<script>
import {fetchList, delMiningObj, getTypeNum} from '/src/api/al/dataMining/dataMining'
import TableForm from './dataMining-form.vue'
import add from './dataMining-add.vue'
import example from './dataMining-example.vue'
import altest from './dataMining-altest.vue'

export default {
  components: {
    TableForm,
    add,
    example,
    altest,
    allog
  },

  data() {

    return {
      searchForm: {
        alType: '聚类'
      },
      allogVisible: false,
      loading: false,
      addOrUpdateVisible: false,
      addVisible: false,
      exampleVisible: false,
      altestVisible: false,
      list: [],
      count: 6,
      pageSize: 6,
      currentPage: 1,
      pageshow: true,
      activeName: 'first',
      firstalNum: '',
      secondalNum: '',
      thirdalNum: '',
      fourthalNum: '',
      totalNum: '',
      numtotal: 0
    }
  },
  mounted() {
    this.getDataList()
    this.getTypeNum()
  },

  methods: {
    getDataList(page, params) {
      fetchList(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm)).then(response => {
        this.list = response.data.data.records
        this.count = response.data.data.total
      })
    },

    getTypeNum() {
      getTypeNum("数据挖掘算法").then(response => {
        this.firstalNum = response.data.data[0]
        this.secondalNum = response.data.data[1]
        this.numtotal = this.firstalNum + this.secondalNum
      })
    },

    refreshData() {
      this.getDataList()
      this.getTypeNum()
    },

    label(label) {
      this.activeName = label
      let alType = ''
      if (label === 'first') {
        alType = '聚类'
      }
      if (label === 'second') {
        alType = '关联规则挖掘'
      }
      this.searchForm.alType = alType
      this.getDataList()
    },

    handleClick(tab, event) {
      this.currentPage = 1
      this.searchForm.alType = tab.label
      this.getDataList()
    },

    seeExample(iconUrl) {
      this.exampleVisible = true
      this.$nextTick(() => {
        this.$refs.example.init(iconUrl)
      })
    },

    allog(id) {
      this.allogVisible = true
      this.$nextTick(() => {
        this.$refs.allog.init(id)
      })
    },

    altest(id, input, output) {
      this.altestVisible = true
      this.$nextTick(() => {
        this.$refs.altest.init(id, input, output)
      })
    },

    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    },

    add() {
      this.addVisible = true
      this.$nextTick(() => {
        this.$refs.add.init()
      })
    },

    // 当前页
    currentChangeHandle(val) {
      this.currentPage = val
      this.getDataList()
    },

    deleteHandle(id) {
      this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        return delMiningObj(id)
      }).then(data => {
        this.$message.success('删除成功')
        this.getDataList()
        this.getTypeNum()
      }).catch(() => {
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
      padding: 3px;
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
  height: 120px
}
.registerBtn{
  min-width: 95px;
  height: 30px;
}

</style>
