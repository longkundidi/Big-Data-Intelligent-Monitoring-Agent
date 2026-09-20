<template>
  <div v-loading="loading" class="fl-container">
    <div v-if="true" class="biaotou" style="position: relative;border-bottom: solid 1px #d9d9d9;height: 12vh">
      <!--head区域-->
      <div>
        <div style="width:2%;position: absolute;padding-top: 2vh;padding-left: 1%">
          <el-image
              src="arrow.png"
              fit="fill">
          </el-image>
        </div>
        <span
            style="position: absolute;font-size: 22px;font-weight: bold;padding-top: 1%;padding-left:3.5%">模型-算法审核管理</span>

        <!--tab区域-->
        <div style="position: absolute;padding-left: 35px;padding-top: 70px">
          <el-tabs v-model="activeName" type="card" @tab-change="label" @tab-click="handleClick">
            <el-tab-pane label="未测试" name="first"/>
            <el-tab-pane label="已测试未通过" name="second"/>
            <el-tab-pane label="测试通过待部署" name="third"/>
            <el-tab-pane label="已部署" name="fourth"/>
          </el-tabs>
        </div>
        <!--搜索区域-->
        <div class="searchForm" style="position: relative;top: 70px">
          <el-form class="demo-form-inline" ref="searchForm" :model="searchForm" label-width="auto" :inline="true"
                   style="max-width: 90%">
            <el-form-item label="算法/模型名称">
              <el-input v-model="searchForm.alModelName" placeholder="请输入算法/模型名称"/>
            </el-form-item>
            <el-form-item label="算法/模型类型">
              <el-select v-model="searchForm.alModelType" placeholder="请选择算法/模型类型">
                <el-option label="数据去重" value="数据去重"/>
                <el-option label="缺失值填充" value="缺失值填充"/>
                <el-option label="异常值检测" value="异常值检测"/>
                <el-option label="数据标准化" value="数据标准化"/>
                <el-option label="命名实体识别模型" value="命名实体识别模型"/>
                <el-option label="分词模型" value="分词模型"/>
                <el-option label="统计学模型" value="统计学模型"/>
                <el-option label="信号处理模型" value="统计学模型"/>
                <el-option label="传统机器学习模型" value="传统机器学习模型"/>
                <el-option label="深度学习模型" value="深度学习模型"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button class="searchBtn" style="width: 1%;" @click="onSearch">搜索</el-button>
              <el-button class="reSetBtn" @click="resetForm">刷新</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>


      <!--main区域-->
      <div class="el-main" v-if="activeName=='first'">
        <el-card style="height: 100%;padding: 10px;margin: 10px">
          <un-audits v-if="unAudisVisible" ref="unAudits" @getPage="getPage"></un-audits>
        </el-card>
      </div>
      <div class="el-main" v-if="activeName=='second'">
        <el-card style="height: 100%;padding: 10px;margin: 10px">
          <auditsed v-if="auditsedVisible" ref="auditsed" @getPage="getPage"></auditsed>
        </el-card>
      </div>
      <div class="el-main" v-if="activeName=='third'">
        <el-card style="height: 100%;padding: 10px;margin: 10px">
          <un-quote v-if="unQuoteVisible" ref="unQuote" @getPage="getPage"></un-quote>
        </el-card>
      </div>
      <div class="el-main" v-if="activeName=='fourth'">
        <el-card style="height: 100%;padding: 10px;margin: 10px">
          <quetoed v-if="quetoedVisible" ref="quetoed" @getPage="getPage"></quetoed>
        </el-card>
      </div>
    </div>


    <!-- footer区域   -->
    <div class="el-footer" style="position:absolute;right:20px;bottom:20px;height: 10%;padding-right: 2%">
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


  </div>
</template>

<script>
import unAudits from "@/views/al/auditsManagement/unTest.vue";
import auditsed from "@/views/al/auditsManagement/tested.vue";
import unQuote from "@/views/al/auditsManagement/unDeploy.vue";
import quetoed from "@/views/al/auditsManagement/deployed.vue";

export default {
  components: {
    unAudits,
    auditsed,
    unQuote,
    quetoed
  },

  data() {
    return {
      loading: false,
      unAudisVisible: true,
      auditsedVisible: false,
      unQuoteVisible: false,
      quetoedVisible: false,
      searchForm: {
        alModelName: '',
        alModelType: ''
      },
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
  // mounted() {
  //   this.onSearch()
  // },
  methods: {
    onSearch() {//搜索
      if (this.activeName === "first") {
        this.$nextTick(() => {
        this.$refs.unAudits.onsearch(this.searchForm)
        })
      }
      if (this.activeName === "second") {
        this.$nextTick(() => {
        this.$refs.auditsed.onsearch(this.searchForm)
        })
      }
      if (this.activeName === "third") {
        this.$nextTick(() => {
        this.$refs.unQuote.onsearch(this.searchForm)
        })
      }
      if (this.activeName === "fourth") {
        this.$nextTick(() => {
        this.$refs.quetoed.onsearch(this.searchForm)
      })
      }

    },
    resetForm() {  //重置
      this.searchForm.alModelName = ''
      this.searchForm.alModelType = ''
      this.onSearch()
    },
    label(label) {//tabbar切换
      this.activeName = label
      let alType = ''
      if (label === 'first') {
        alType = '未审核'
        this.unAudisVisible = true
        this.$nextTick(() => {
          this.$refs.unAudits.init(this.currentPage, this.pageSize, this.searchForm)
        })
      }
      if (label === 'second') {
        alType = '已审核'
        this.auditsedVisible = true
        this.$nextTick(() => {
          this.$refs.auditsed.init(this.currentPage, this.pageSize, this.searchForm)
        })
      }
      if (label === 'third') {
        alType = '待引用'
        this.unQuoteVisible = true
        this.$nextTick(() => {
          this.$refs.unQuote.init(this.currentPage, this.pageSize, this.searchForm)
        })
      }
      if (label === 'fourth') {
        alType = '已引用'
        this.quetoedVisible = true
        this.$nextTick(() => {
          this.$refs.quetoed.init(this.currentPage, this.pageSize, this.searchForm)
      })
    }
    },

    getPage(current, total) {
      this.currentPage = current
      this.count = total
    },
    handleClick(tab, event) {
      this.currentPage = 1
      this.searchForm.alType = tab.label

    },
    // 当前页
    currentChangeHandle(val) {
      this.currentPage = val
      if (this.activeName === "first") {
        this.$refs.unAudits.refresh(this.currentPage, this.pageSize, this.searchForm)
      }
      if (this.activeName === "second") {
        this.$refs.auditsed.refresh(this.currentPage, this.pageSize, this.searchForm)
      }
      if (this.activeName === "third") {
        this.$refs.unQuote.refresh(this.currentPage, this.pageSize, this.searchForm)
      }
      if (this.activeName === "fourth") {
        this.$refs.quetoed.refresh(this.currentPage, this.pageSize, this.searchForm)
      }

    },


  }
}
</script>

<style lang="scss" scoped>
.el-main {
  min-height: 80vh;
  position: absolute;
  top: 125px;
  left: auto;
  width: 100%;
}

.demo-form-inline {
  position: relative;
  float: right;
}

.demo-form-inline .el-input {
  --el-input-width: 500px;
}

.demo-form-inline .el-select {
  --el-select-width: 220px;
}

</style>
