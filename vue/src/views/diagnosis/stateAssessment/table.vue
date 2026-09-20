<template>
  <div class="tablecon">
    <div class="subtable">
      <div class="title">状态感知模板</div>
      <avue-crud ref="crudPerceivedRef" class="h-avue-crud"
                 v-model="perceivedForm"
                 :option="perceivedOption"
                 :data="perceivedData"
                 :page="perceivedPage"
                 :table-loading="perceivedLoading"
                 @size-change="sizeChange"
                 @current-change="currentChange"
                 @on-load="getPerceivedList"
                 @row-click="perceivedRowClick">
        <template #perceivedSelected="scope">
          &nbsp;&nbsp;&nbsp;<el-radio v-model="perceivedRowIndex" :label="scope.index"><span></span></el-radio>
        </template>
        <template #modelName="scope">
          <span>{{ scope.row.modelName }}</span>
          <el-tag v-if="scope.row.isDefaultModel" style="margin-left: 5px">默认模板</el-tag>
        </template>
      </avue-crud>
    </div>
  </div>
</template>
<script>
import {ref} from "vue";
import {simpleTemplateTableOption} from '@/const/crud/diagnosis/stateAssessment/table'
import {
  fetchTaskList
} from '@/api/sw/model3d/configModel/table.js'

let globeParams = {}     //  声明一个全局参数对象

export default {
  name: 'modelTable',

  setup() {
    let perceivedOption = ref(simpleTemplateTableOption)
    let perceivedData = ref([])
    let perceivedTask = ref([])
    let perceivedForm = ref({})
    let perceivedPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })
    let perceivedLoading = ref(false)
    let perceivedRowIndex = ref(0)
    let curTaskId = ref('')
    let curAlgoName = ref('')


    return {
      perceivedOption,
      perceivedData,
      perceivedTask,
      perceivedForm,
      perceivedPage,
      perceivedLoading,
      perceivedRowIndex,
      curTaskId,
      curAlgoName
    }
  },

  methods: {

    perceivedRowClick(row) {
      if (!row || !row.taskId) return
      this.perceivedRowIndex = row.$index
      this.curTaskId = row.taskId
      this.curAlgoName = row.algoShortname
      this.$emit('getTaskId', this.curTaskId, this.curAlgoName)   //  向父组件发送消息
    },

    //  分页或者修改每页条数时
    sizeChange(val) {
      this.perceivedPage.currentPage = 1
      this.perceivedPage.pageSize = val
      this.getPerceivedList()
    },
    currentChange(val) {
      this.perceivedPage.currentPage = val
      this.getPerceivedList()
    },
    getPerceivedList() {
      if (!this.perceivedData.includes(undefined) && this.perceivedData.length !== 0) {
        this.perceivedData = []
        for (let i = this.perceivedPage.pageSize * (this.perceivedPage.currentPage - 1) + 1;
             i <= ((this.perceivedPage.total > (this.perceivedPage.pageSize * this.perceivedPage.currentPage)) ? (this.perceivedPage.pageSize * this.perceivedPage.currentPage) : (this.perceivedPage.total));
             i++) {
          this.perceivedData.push(this.perceivedTask[i - 1]);
        }
        this.perceivedLoading = false
      }

    },

    selectionChange(val) {
      this.checkVars = val
    },

    getModels(node) {
      globeParams.curNode = node                  //  缓存当前节点
      this._refreshPerceivedTableByNodeId(node.id)
    },

    //  根据节点Id，刷新状态感知模板数据
    async _refreshPerceivedTableByNodeId(nodeId) {
      this.perceivedLoading = false
      const res = await fetchTaskList({nodeId: nodeId, modelType: 'perceived'})
      this.perceivedTask = []
      this.perceivedData = []
      this.perceivedPage.total = 0
      this.perceivedRowIndex = 0

      if (res.data.code === 0) {
        const rows = Array.isArray(res.data.data) ? res.data.data : []
        rows.forEach(item => {
          item.hasChildren = false
          if (item.status === 1) {
            this.perceivedTask.push(item)
          }
        })
        this.perceivedPage.total = this.perceivedTask.length
        if (this.perceivedTask.length > 0) {
          this.curTaskId = this.perceivedTask[0].taskId
          this.curAlgoName = this.perceivedTask[0].algoShortname
          this.perceivedData = this.perceivedTask.slice(0, this.perceivedPage.pageSize)
        } else {
          this.curTaskId = ''
          this.curAlgoName = ''
        }
      } else {
        this.curTaskId = ''
        this.curAlgoName = ''
      }
      this.$emit('getTaskId', this.curTaskId, this.curAlgoName)
    },

    clear() {
      this.perceivedTask = []
      this.perceivedData = []
    }


  }

}
</script>
<style lang="scss" scoped>
@import '@/styles/my-dialog.scss';
@import '@/styles/my-avue-crud.scss';
@import '@/styles/button/resource.scss';

.tablecon {
  height: 100%;
  width: 100%;
  display: flex;
  color: #e8fbff;

  .subtable {
    height: 100%;
    width: 100%;
    min-height: 0;
    display: flex;
    flex-direction: column;

    .title {
      height: 44px;
      width: 100%;
      display: flex;
      color: #ffffff;
      letter-spacing: 0;
      justify-content: flex-start; /* 水平居中 */
      align-items: center; /* 垂直居中 */
      padding: 0 18px;
      font-size: 16px;
      font-weight: 600;
      border-bottom: 1px solid rgba(122, 214, 240, 0.16);
      background: linear-gradient(90deg, rgba(42, 138, 177, 0.62), rgba(128, 216, 237, 0.08));
      box-sizing: border-box;
    }

    .title::before {
      content: "";
      width: 4px;
      height: 18px;
      margin-right: 10px;
      border-radius: 2px;
      background: #80d8ed;
      box-shadow: 0 0 12px rgba(128, 216, 237, 0.55);
    }
  }

  .split {
    height: 100%;
    width: 3px;
    /*background-color: #5a5e66;*/
  }

  .sensor-zone {
    height: 400px;
    overflow-y: auto;
  }

}

:deep(.el-table .el-table__cell.is-center) {
  text-align: left;
}

:deep(.h-avue-crud) {
  flex: 1;
  min-height: 0;
}

:deep(.avue-crud),
:deep(.el-table),
:deep(.el-table__expanded-cell) {
  background: transparent !important;
  color: #e8fbff;
}

:deep(.el-table th),
:deep(.el-table tr),
:deep(.el-table td) {
  background: transparent !important;
  color: rgba(232, 251, 255, 0.88);
  border-bottom-color: rgba(122, 214, 240, 0.12) !important;
}

:deep(.el-table__header-wrapper th) {
  color: #dffcff;
  background: rgba(42, 138, 177, 0.28) !important;
}

:deep(.el-table__body tr:hover > td) {
  background: rgba(42, 138, 177, 0.2) !important;
}

:deep(.el-pagination),
:deep(.avue-crud__pagination) {
  color: rgba(232, 251, 255, 0.78);
}

:deep(.el-tag) {
  border-color: rgba(122, 214, 240, 0.38);
  background: rgba(42, 138, 177, 0.18);
  color: #80d8ed;
}
</style>
