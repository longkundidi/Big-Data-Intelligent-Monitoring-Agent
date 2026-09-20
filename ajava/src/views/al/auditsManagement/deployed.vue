<template>
  <div>
    <el-table :data="tableData" style="width: 100%" :cell-style="{ textAlign: 'center' }"
              :header-cell-style="tableHeaderColor">
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
      <el-table-column label="部署状态" width="280">
        <template #default="scope">
          <div>
            <el-tag v-if="scope.row.isDeployed == 0">已部署</el-tag>
            <el-tag v-else type="danger">未部署</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="测试状态">
        <template #default="scope">
          <div>
            <el-switch inactive-text="否"
                       active-text="是"
                       :active-value="0"
                       :inactive-value="2"
                       v-model="scope.row.isPass"
                       @click="changeCheck(scope.row)"/>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>

import {getDeployedPage, updateDeployStatus, updateTestStatus} from "@/api/al/auditsManagement/auditsManagement";
import {ElMessage} from "element-plus";

export default {
  name: "unQuote",
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
      getDeployedPage(Object.assign({
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }, this.searchForm)).then(response => {
        const {code, success, message, data} = response.data
        this.tableData = data.records
        this.$emit('getPage', data.current, data.total)
      })
    },

    //变更测试状态
    changeCheck(row) {
      updateTestStatus({
        alModelName: row.alModelName,
        isPass: 2,
      }).then(response => {
        ElMessage({
          message: "重新测试",
          type: 'warning'
        })
        updateDeployStatus({
          alModelName: row.alModelName,
          isDeployed: 1,
        }).then(response => {
        })
        this.getDataList()
      })

    },
  }
}
</script>

<style scoped>
/*选中行的样式*/
.el-table {
  --el-table-row-hover-bg-color: #E1EAF3 !important;
  --el-table-row-hover-text-color: white;
}

::v-deep .el-scrollbar__view {
  width: 100%;
}

::v-deep .el-scrollbar__view .el-table__body {
  width: 100%;
}
</style>
