<template>
  <div class="tablecon">
    <div class="subtable">
      <div class="title">提示信息</div>
      <avue-crud ref="crudFaultRef" class="h-avue-crud"
                 v-model="faultForm"
                 :option="faultOption"
                 :data="faultData"
                 :page="faultPage"
                 :table-loading="faultLoading"
                 @size-change="sizeChange"
                 @current-change="currentChange"
                 @on-load="getFaultList">
      </avue-crud>
    </div>
  </div>
</template>
<script>
import {ref} from "vue";
import {faultTableOption} from '@/const/crud/diagnosis/stateAssessment/table'
import {reqFaultSet} from "@/api/diagnosis/stateAssessment/stateAssessment";

let intervalId = null

export default {
  name: 'faultTable',

  setup() {
    let faultOption = ref(faultTableOption)
    let faultForm = ref({})
    let faultData = ref([])
    let faultTask = ref([])
    let faultPage = ref({
      total: 0,          // 总页数
      currentPage: 1,     // 当前页数
      pageSize: 10        // 每页显示多少条
    })
    let faultLoading = ref(false)
    let curTaskId = ref('')
    let curAlgoName = ref('')


    return {
      faultOption,
      faultForm,
      faultData,
      faultTask,
      faultPage,
      faultLoading,
      curTaskId,
      curAlgoName
    }
  },


  methods: {

    //  分页或者修改每页条数时
    sizeChange(val) {
      this.faultPage.currentPage = 1
      this.faultPage.pageSize = val
      this.getFaultList()
    },
    currentChange(val) {
      this.faultPage.currentPage = val
      this.getFaultList()
    },
    getFaultList() {
      if (!this.faultData.includes(undefined) && this.faultData.length !== 0) {
        this.faultData = []
        for (let i = this.faultPage.pageSize * (this.faultPage.currentPage - 1) + 1;
             i <= ((this.faultPage.total > (this.faultPage.pageSize * this.faultPage.currentPage)) ? (this.faultPage.pageSize * this.faultPage.currentPage) : (this.faultPage.total));
             i++) {
          this.faultData.push(this.faultTask[i - 1]);
        }
        this.faultLoading = false
      }

    },

    selectionChange(val) {
      this.checkVars = val
    },

    getTask(taskId, alName) {
      let self = this
      this.curTaskId = taskId
      this.curAlgoName = alName
      if (intervalId !== null) {     //  每次开始加载图表前，先关闭定时器
        clearInterval(intervalId)
        intervalId = null
      }
      let _timer = setTimeout(() => {
        self.$nextTick(() => {                    //  等待页面渲染完成，启动定时器，加载图表数据
          self.startTimer()
          clearTimeout(_timer)
        })
      }, 50)

    },

    //  启动定时器
    startTimer() {
      let self = this
      intervalId = setInterval(() => {
        if (intervalId) {                                          //  如果定时器没有被销毁
          self._refreshFaultTableByNodeId(this.curTaskId, this.curAlgoName)
        }
      }, 1000)
    },

    async _refreshFaultTableByNodeId(taskId, alName) {
      this.faultLoading = false
      let res = await reqFaultSet(taskId, alName)
      this.faultTask = []

      if (res.data.code === 0) {
        res.data.data.forEach(item => {
          item.hasChildren = false //  表格数据没有子节点
          this.faultTask.push(item)
        })
        this.faultTask.reverse()
        this.faultPage.total = res.data.data.length
        this.faultData = this.faultTask.slice(0, this.faultPage.pageSize)
        this.getFaultList()
      }
    },

    clear() {
      if (intervalId !== null) {
        clearInterval(intervalId)     //  关闭定时器
        intervalId = null
      }
      this.curTaskId = ''
      this.curAlgoName = ''
      this.faultTask = []
      this.faultData = []
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
}

.el-table__body {
  position: absolute;
  transition: all 500ms linear;
}
</style>
