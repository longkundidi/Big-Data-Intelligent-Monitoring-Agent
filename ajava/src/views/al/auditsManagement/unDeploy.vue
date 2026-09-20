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
      <el-table-column label="部署状态">
        <template #default="scope">
          <div>

            <el-switch inactive-text="否"
                       active-text="是"
                       :active-value="0"
                       :inactive-value="1"
                       v-model="scope.row.isDeployed"
                       @change="toggleSwitch(scope.row)"/>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
        v-model="dialogVisible"
        title="提示"
        width="25%"
        style="height: 170px"
        align-center
        :show-close="true"
        @close="closeDialog()"
    >
      <el-form ref="dataForm" style="margin-top: 20px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="算法调用路径" prop="alModelUrl" label-width="120px">
              <el-input v-model="dataForm.alModelUrl"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" style="display: flex;justify-content: center;margin-top: 5px">
          <el-button type="primary" class="normalBtn"
                     @click="comFirm()">确定</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<script>

import {getTestNoDeployPage, updateDeployStatus} from "@/api/al/auditsManagement/auditsManagement";
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
      dataForm: {
        alModelUrl: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 6,
      searchForm: {},
      dialogVisible: false,
      row: ""
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
      getTestNoDeployPage(Object.assign({
        pageNum: this.currentPage,
        pageSize: this.pageSize
      }, this.searchForm)).then(response => {
        const {code, success, message, data} = response.data
        this.tableData = data.records
        this.$emit('getPage', data.current, data.total)

      })
    },
    toggleSwitch(row) {
      if (row.isDeployed === 0) {
        this.row = {...row}
        this.dialogVisible = true
      }
    },
    closeDialog() {
      this.dialogVisible = false
    },
    comFirm() {
      if (this.dataForm.alModelUrl === "") {
        ElMessage.warning("请先填入算法路径")
      } else {
        //更新alModelurl和部署状态
        updateDeployStatus({
          alModelName: this.row.alModelName,
          alModelUrl: this.dataForm.alModelUrl,
          isDeployed: 0,
        }).then(response => {
          ElMessage({
            message: "部署通过",
            type: 'success'
          })

          this.closeDialog()
          this.getDataList()
        })
      }

    }
  }
}
</script>

<style scoped>
/*选中行的样式*/
.el-table {
  --el-table-row-hover-bg-color: #E1EAF3 !important;
}
</style>
