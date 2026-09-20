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
      let res = await fetchTaskList({nodeId: nodeId, modelType: 'perceived'})
      this.perceivedTask = []

      if (res.data.code === 0 && res.data.data !== null) {
        this.perceivedRowIndex = 0
        res.data.data.forEach(item => {
          item.hasChildren = false //  表格数据没有子节点
          if (item.status === 1) {
            this.perceivedTask.push(item)
          }
        })
        this.perceivedPage.total = res.data.data.length
        if (this.perceivedTask.length === 0) {
          this.curTaskId = ''
          this.curAlgoName = ''
        } else {
          this.curTaskId = this.perceivedTask[0].taskId
          this.curAlgoName = this.perceivedTask[0].algoShortname
        }
        this.$emit('getTaskId', this.curTaskId, this.curAlgoName)   //  向父组件发送消息
        this.perceivedData = this.perceivedTask.slice(0, this.perceivedPage.pageSize)
      }
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

  .subtable {
    height: 100%;
    width: 100%;

    .title {
      height: 40px;
      width: 100%;
      background-color: cadetblue;
      display: flex;
      color: white;
      letter-spacing: 2px; /* 设置字间距为 2 像素 */
      justify-content: center; /* 水平居中 */
      align-items: center; /* 垂直居中 */
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
</style>
