<template>
  <div>
    <el-table :data="tableData" style="width: 100%" :cell-style="{ textAlign: 'center' }"
              :header-cell-style="tableHeaderColor">
      <!--      <el-table-column label="序号" width="180">-->
      <!--        <template #default="scope">-->
      <!--          <div style="display: flex; align-items: center">-->
      <!--            <span style="margin-left: 10px">{{ scope.row.id+1 }}</span>-->
      <!--          </div>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <el-table-column label="算法/模型名称" width="380">
        <template #default="scope">
          <div>
            <span>{{ scope.row.alModelName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="算法/模型创建者" width="280">
        <template #default="scope">
          <div>
            <span>{{ scope.row.creator }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="算法/模型类型" width="300">
        <template #default="scope">
          <div>
            <span>{{ scope.row.alModelType }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="测试状态" width="280">
        <template #default="scope">
          <div>
            <el-switch inactive-text="否"
                       active-text="是"
                       :active-value="1"
                       :inactive-value="2"
                       v-model="scope.row.isPass" @click="changeCheck1(scope.row)"/>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button
              class="reSetBtn"
              @click="changeCheck1(scope.row)"
          >重新测试
          </el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <uploadProgram v-if="programVisible" ref="uploadProgram" @getDataList="getDataList"></uploadProgram>

  </div>
</template>

<script>
import {getTestNoPassPage} from "@/api/al/auditsManagement/auditsManagement";
import "@/styles/button/resource.scss";
import uploadProgram from './uploadprogram.vue'

export default {
  name: "auditsed",
  components: {
    uploadProgram
  },
  data() {
    return {
      tableHeaderColor: {
        background: '#337ecc',
        color: 'white',
        fontSize: '14px',
        textAlign: 'center',
      },
      tableData: [],
      currentPage: 1,
      pageSize: 6,
      searchForm: {},
      checkVisible1: false,
      checkVisible2: false,
      isCheck: true,
      programVisible: false,
      programUrl: ''
    }
  },
  mounted() {
    this.getDataList()
  },
  methods: {
    init(currentPage, pageSize, searchForm) {
      this.currentPage = currentPage
      this.pageSize = pageSize
      this.searchForm = searchForm
    },
    refresh(currentPage, pageSize, searchForm) {
      this.currentPage = currentPage
      this.pageSize = pageSize
      this.searchForm = searchForm
      this.getDataList()
    },
    onsearch(searchForm) {
      this.searchForm = searchForm
      this.getDataList()
    },
    getDataList() {
      getTestNoPassPage(Object.assign({
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }, this.searchForm)).then(response => {
        const {code, success, message, data} = response.data
        this.tableData = data.records
        this.$emit('getPage', data.current, data.total)

      })
    },

    //重新测试
    changeCheck1(row) {

      this.programVisible = true
      this.$nextTick(() => {
        this.$refs.uploadProgram.init(row.alModelName)
      })
    },

  }
}
</script>

<style scoped>
/*选中行的样式*/
.el-table {
  --el-table-row-hover-bg-color: #E1EAF3 !important;
}
</style>
