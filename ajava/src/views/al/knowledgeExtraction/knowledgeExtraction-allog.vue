<template>
  <el-dialog
      append-to-body
      width="25%"
      :close-on-click-modal="false"
      v-model="visible"
      :show-close="true"
      class="el-dialog__header"
      @close="closeDialog()"
  >

    <div slot="title" style="font-size: 25px;text-align:center;padding-bottom: 5%">
      <span>测试日志</span>
    </div>


    <el-timeline v-infinite-scroll="load" class="infinite-list" style="overflow: auto;padding-bottom: 8%"
                 infinite-scroll-distance="1">
      <el-timeline-item v-for="data in list1" :timestamp="data.startTime" placement="top">
        <el-card shadow="hover" style="width: 95%">
          <p><span style="font-weight: bolder">测试数据:</span> {{ data.taskMsg }}</p><br>
          <p><span style="font-weight: bolder">测试结果:</span> {{ data.taskResult }}</p><br>
        </el-card>
      </el-timeline-item>
    </el-timeline>

    <span slot="footer" class="dialog-footer" style="padding-left: 80%;">
      <el-button style="width: 20%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>

  </el-dialog>
</template>

<script>

import {getLog} from "/src/api/al/altest/altest";

export default {
  data() {
    return {
      visible: false,
      pageSize: 4,
      logid: '',
      alClass: '',
      logPage: 1,
      list1: []
    }
  },
  methods: {
    init(id) {
      this.logid = id
      this.alClass = 'alKnowledgeExtraction'
      this.logPage = 1
      this.renderLogTable()
      this.visible = true
    },

    load() {
      this.logPage++
      this.renderLogTable()
    },

    renderLogTable(page, params) {
      getLog(Object.assign({
        curPage: this.logPage,
        size: this.pageSize,
        alId: this.logid,
        alClass: this.alClass
      }, params, this.searchForm1)).then(response => {
        this.list1.push(...response.data.data.records)
      })
    },

    // 重置表单
    closeDialog() {
      this.logPage = 1
      this.list1 = []
      this.logid = ''
      this.$emit('refreshDataList')
    }
  },
}
</script>
<style>
.tableTitle {
  position: relative;
  margin: 0 auto;
  width: 100%;
  height: 1px;
  background-color: #d4d4d4;
  text-align: center;
  font-size: 14px;
  color: rgba(101, 101, 101, 1);
}

.midText {
  position: absolute;
  left: 50%;
  background-color: #ffffff;
  padding: 0 15px;
  transform: translateX(-50%) translateY(-50%);
}

.infinite-list {
  height: 350px;
  padding: 0;
  margin: 0;
  list-style: none;

}

</style>
