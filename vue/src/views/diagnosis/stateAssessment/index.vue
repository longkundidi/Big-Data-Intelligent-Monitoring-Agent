<template>
  <el-container id="containergraph" class="state-assessment-page">
    <el-container class="containerProHeader">
      <el-header class="selectHeader">
        <el-form class="selectForm" ref="form" :model="formdata" label-width="72px">
          <el-form-item class="selectItem query-title">
            <h3>状态评估</h3>
          </el-form-item>
          <el-form-item class="selectItem" label="项目" prop="name" size="large">
            <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                       :placeholder="formdata.project" size="default" @clear="searchProject = false">
              <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                         @click="formdata.project=item.project; findProductModels(item.project,item.id)"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectItem" label="产品机型" prop="productModel">
            <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                       :placeholder="formdata.productModel">
              <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                         @click="searchProject=true;formdata.productModel = item.productModel; formdata.projectId = item.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectAction">
            <el-button class="normalBtn" size="large" :style="{ width: 'auto' }" @click="search()">查询
            </el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <div class="mycontainer">
        <div class="leftzone">
          <div class="leftup config-panel">
            <div class="panel-title">结构树</div>
            <div class="mytree">
              <model-gbom-tree ref="treeRef" @clickNode="clickNode"></model-gbom-tree>
              <!--布局树组件-->
            </div>
          </div>
          <div class="leftdown config-panel">
            <div class="mytable">
              <model-table ref="refTable" v-if="isActivate" @getTaskId="getTaskId"></model-table>
              <!--布局表格组件-->
            </div>
          </div>
        </div>
        <div class="rightzone">
          <div class="rightup config-panel">
            <my-chart ref="refChart"></my-chart>
          </div>
          <div class="rightdown config-panel">
            <fault-table ref="refFault" v-if="isActivate"></fault-table>
          </div>
        </div>
      </div>
    </el-container>
  </el-container>
</template>
<script>
import {ref} from 'vue'
import modelGbomTree from './tree.vue'
import modelTable from './table.vue'
import myChart from './myChart.vue'
import faultTable from './myFault.vue'
import {fetchTaskList, getAllInstanceTreeNodes} from "@/api/sw/model3d/configModel/table";
import {getProductModelBySceneName, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
import {ElMessage} from "element-plus";


let globeParams = {}     //  声明一个全局参数对象

export default {

  components: {
    modelGbomTree,            //  挂载树组件
    modelTable,              //  挂载表格组件
    myChart,
    faultTable
  },


  setup() {
    let treeRef = ref(null)
    let refTable = ref(null)
    let refChart = ref(null)
    let refFault = ref(null)
    let isActivate = ref(false)
    let formdata = ref({
      project: '',
      projectId: '',
      productModel: '',
    })
    let searchProject = ref(false)
    let treeData = ref([])
    let defaultExpandKeys = ref([])
    let expandKeys = ref([])
    let projectList = ref([])
    let productModelList = ref([])
    let fengjiIdList = ref([])
    let windBomLists = ref([])
    let curTaskId = ref('')
    let curAlgoName = ref('')

    return {
      treeRef,
      refTable,
      refChart,
      refFault,
      isActivate,
      formdata,
      searchProject,
      treeData,
      defaultExpandKeys,
      expandKeys,
      projectList,
      productModelList,
      fengjiIdList,
      windBomLists,
      curTaskId,
      curAlgoName
    }
  },
  mounted() {
    this.projectInit()
    this.treeData = []
    this.defaultExpandKeys = []
    this.isActivate = true
  },
  methods: {
    projectInit() { //初始化项目列表
      const userId = this.$store.state.user.userInfo.userId
      getProjects({userId: userId, userRole: 'GENERAL_USER'}).then(response => {
        if(response.data.code === 0){
          let res = response.data.data
          this.projectList = res.filter((item, index, arr) =>
              index === arr.findIndex((t) => t.project === item.project)
          )
        }
      })
    },

    findProductModels(proName, proId) {
      //切换项目的时候，需要初始化值
      this.treedata = []
      this.formdata.productModel = '';
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    clickNode(node) {
      globeParams.curNode = node
      this.refTable.getModels(node)       //  根据节点id，调用子组件的方法，查询节点关联的感知模型
    },

    getTaskId(curTaskId, curAlgoName) {
      this.curTaskId = curTaskId
      this.curAlgoName = curAlgoName
      this.$nextTick(() => {
        if(curTaskId !== '' && curAlgoName !== ''){
          this.refChart.loadChart(curTaskId, curAlgoName, globeParams.curNode)       //  调用子组件myChart.vue方法，绘制折线图
          this.refFault.getTask(curTaskId, curAlgoName)
        }else {
          this.refChart.showEmptyChart(globeParams.curNode)
          this.refFault.clear()
        }
      })
    },

    getCurNode() {
      return globeParams.curNode
    },

    async search() {
      this.treeData = []
      this.expandKeys = []
      this.defaultExpandKeys = []
      this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
      this.refTable.clear()
      this.refChart.clear()
      this.refFault.clear()
      if (this.searchProject === false || !this.formdata.projectId || !this.formdata.productModel) {
        ElMessage({
          message: "请选择项目和产品机型后再查询！",
          type: 'warning'
        })
      } else {
        await this.getTreeNodes(1)
        this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
      }
    },

    // 根据节点层级数，加载结构树的一组节点
    async getTreeNodes(nodeLevel) {
      try {
        let response = await getAllInstanceTreeNodes({proId: this.formdata.projectId, nodeLevel: 99})
        if ((response.data.data)&&(response.data.data.length > 0)) {
          const allNodes = response.data.data
          const runningTurbineCodes = await this.getRunningTurbineCodes(allNodes)
          const visibleNodes = allNodes.filter(item => item.turbineCode && runningTurbineCodes.has(item.turbineCode))
          this.treeData = []
          this.expandKeys = []            //  缓存待扩展的节点
          if (visibleNodes.length === 0) {
            this.defaultExpandKeys = []
            return
          }
          let rootNodes = visibleNodes.filter(ele => this.isRootNode(ele))
          if (rootNodes.length === 0) {
            const levelList = visibleNodes
                .map(item => Number(item.nodeLevel))
                .filter(level => !Number.isNaN(level))
            if (levelList.length > 0) {
              const minLevel = Math.min(...levelList)
              rootNodes = visibleNodes.filter(item => Number(item.nodeLevel) === minLevel)
            } else {
              rootNodes = visibleNodes.slice(0, 1)
            }
          }
          rootNodes.sort((a, b) => {
            const lastPartA = String(a.turbineCode || a.nodeCode || '').split('-').pop()
            const lastPartB = String(b.turbineCode || b.nodeCode || '').split('-').pop()

            // 如果最后一部分是数字，尝试将其转换为整数进行比较
            const numA = parseInt(lastPartA.match(/\d+/), 10)
            const numB = parseInt(lastPartB.match(/\d+/), 10)

            if (!isNaN(numA) && !isNaN(numB)) {
              return numA - numB
            }

            // 如果无法转换为数字，则按字符串比较
            if (lastPartA < lastPartB) {
              return -1
            }
            if (lastPartA > lastPartB) {
              return 1
            }
            return 0
          })
          for (let item of rootNodes) {
            this.treeData.push(item)
            this.expandKeys.push(item.id)
            let children = visibleNodes
            if (item.turbineCode !== undefined && item.turbineCode !== null && item.turbineCode !== '') {
              children = visibleNodes.filter(ele => ele.turbineCode === item.turbineCode)
            }
            await this.setChildren(item, children)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点
        }else {
          this.treeData = []
        }
      } catch (error) {
        console.log(error)
      }
    },

    async getRunningTurbineCodes(nodes) {
      const taskNodes = nodes.filter(item => item.id && item.turbineCode)
      const checks = await Promise.all(taskNodes.map(async item => {
        try {
          const response = await fetchTaskList({nodeId: item.id, modelType: 'perceived'})
          const rows = Array.isArray(response.data.data) ? response.data.data : []
          return rows.some(row => row.status === 1) ? item.turbineCode : null
        } catch (error) {
          console.log(error)
          return null
        }
      }))
      return new Set(checks.filter(Boolean))
    },

    //  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
    setChildren(pNode, nodeList) {
      let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList, pNode.id, pNode.nodeId)
      let children = res.sonNodes
      if (children.length === 0) {
        if ((pNode.nodeType === 'Mid') || (pNode.nodeType === 'Root')) {           //  如果不是叶子节点，节点前显示"+"号
          this.expandKeys = this.expandKeys.filter(item => item !== pNode.id) //  从扩展节点中删除它
          pNode.children = [{id: 'loading', name: '节点加载中...'}]
        }
        return pNode
      } else {
        pNode.children = children
        children.forEach((item) => {
          this.expandKeys.push(item.id)          //  添加到扩展节点
          this.setChildren(item, res.otherNodes)
        })
      }
    },

    isRootNode(node) {
      const nodeType = String(node.nodeType || '').toLowerCase()
      if (nodeType.includes('root')) return true
      if (Number(node.nodeLevel) === 1) return true
      const nodeCode = String(node.nodeCode || '')
      return nodeCode !== '' && !nodeCode.includes('-')
    },

    //正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
    getChildrenByNodeCode(pNodeCode, nodeList, pNodeId, pNodeNodeId) {
      let sonNodes = []
      let otherNodes = []
      const escapedNodeCode = String(pNodeCode || '').replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      let regex = new RegExp('^' + escapedNodeCode + '[-_/:.][^-_/:.]+$')
      nodeList.forEach((item) => {
        const parentRef = item.parentNodeId ?? item.parentId ?? item.pid ?? item.parent_id
        const byParentId = parentRef !== undefined && parentRef !== null
            && (String(parentRef) === String(pNodeId) || String(parentRef) === String(pNodeNodeId))
        const byNodeCode = regex.test(String(item.nodeCode || ''))

        if (byParentId || byNodeCode)          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
          sonNodes.push(item)
        else
          otherNodes.push(item)
      })
      return {sonNodes: sonNodes, otherNodes: otherNodes}
    }

  },

  beforeRouteEnter(to, from, next){
    next(vm => {
      if(vm.curTaskId !== '' && vm.curAlgoName !== ''){
        vm.getTaskId(vm.curTaskId, vm.curAlgoName)
      }
    })
  },

  beforeRouteLeave(to, from, next) {
    this.refChart.clear()
    this.refFault.clear()
    //next()让其跳转
    next()
  }

}

</script>
<style lang="scss" scoped>
.state-assessment-page {
  height: 100vh;
  min-height: 0;
  padding: 12px;
  color: #ffffff;
  position: relative;
  background:
    radial-gradient(circle at 72% 12%, rgba(56, 175, 225, 0.16), transparent 30%),
    radial-gradient(circle at 18% 82%, rgba(78, 219, 207, 0.1), transparent 28%),
    linear-gradient(135deg, rgba(4, 30, 56, 0.94), rgba(2, 14, 30, 0.98));
  overflow: hidden;
  box-sizing: border-box;
}

.state-assessment-page::before {
  display: none;
}

.state-assessment-page::after {
  display: none;
}

.containerProHeader {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  width: 100%;
  position: relative;
  z-index: 1;
}

.mycontainer {
  display: flex;
  flex: 1;
  min-height: 0;
  height: 100%;
  width: 100%;
  gap: 12px;
  overflow: hidden;

  .leftzone {
    width: 36%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-width: 360px;

    .leftup {
      width: 100%;
      height: 52%;
    }

    .leftdown {
      width: 100%;
      height: 48%;
      overflow: hidden;
    }
  }

  .rightzone {
    width: 64%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-width: 0;

    .rightup {
      width: 100%;
      height: 52%;
      position: relative;
    }

    .rightdown {
      width: 100%;
      height: 48%;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
  }

  .config-panel {
    min-height: 0;
    overflow: hidden;
    position: relative;
    background:
      radial-gradient(circle at 32% 8%, rgba(78, 219, 207, 0.1), transparent 30%),
      linear-gradient(180deg, rgba(7, 50, 78, 0.72), rgba(3, 25, 48, 0.88));
    border: 1px solid rgba(122, 214, 240, 0.18);
    border-radius: 8px;
    box-shadow:
      0 16px 36px rgba(0, 8, 18, 0.24),
      inset 0 1px 0 rgba(255, 255, 255, 0.05);
  }

  .config-panel::before {
    display: none;
  }

  .panel-title {
    height: 44px;
    padding: 0 18px;
    display: flex;
    align-items: center;
    color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0;
    border-bottom: 1px solid rgba(122, 214, 240, 0.16);
    background: linear-gradient(90deg, rgba(42, 138, 177, 0.62), rgba(128, 216, 237, 0.08));
  }

  .panel-title::before {
    content: "";
    width: 4px;
    height: 18px;
    margin-right: 10px;
    border-radius: 2px;
    background: #80d8ed;
    box-shadow: 0 0 12px rgba(128, 216, 237, 0.55);
  }

  .mytree {
    height: calc(100% - 44px);
    overflow-y: auto;
    position: relative;
    z-index: 1;
  }

  .mytable {
    height: 100%;
    overflow-y: auto;
  }
}

.selectHeader {
  flex: 0 0 68px;
  display: flex;
  width: 100%;
  height: 68px;
  margin: 0;
  align-items: center;
  padding: 0 18px;
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(7, 48, 76, 0.64), rgba(3, 25, 48, 0.78));
  border: 1px solid rgba(122, 214, 240, 0.18);
  box-shadow: 0 12px 30px rgba(0, 8, 18, 0.22);
  box-sizing: border-box;
}

.selectForm {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 0;
}

.selectItem {
  flex: 0 0 300px;
  margin: 0;
}

.query-title {
  flex: 0 0 112px;
  margin: 0;
}

.query-title h3 {
  margin: 0;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0;
  white-space: nowrap;
}

.selectAction {
  flex: 1;
  margin: 0;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.82);
  font-weight: 500;
}

:deep(.selectHeader .el-form-item) {
  margin-bottom: 0;
}

:deep(.el-select .el-input__wrapper) {
  color: #ffffff;
  background: rgba(3, 24, 45, 0.72);
  border: 1px solid rgba(122, 214, 240, 0.24);
  box-shadow: 0 0 0 1px rgba(122, 214, 240, 0.14) inset;
  min-height: 32px;
}

:deep(.el-select .el-input__inner) {
  color: #ffffff;
}

:deep(.el-button.normalBtn) {
  min-width: 72px;
  height: 32px;
  padding: 0 14px;
  color: #ffffff;
  border: 1px solid rgba(122, 214, 240, 0.32);
  border-radius: 6px;
  font-weight: 500;
  background: linear-gradient(180deg, rgba(42, 122, 154, 0.82), rgba(13, 68, 98, 0.92));
  box-shadow: 0 8px 18px rgba(0, 10, 22, 0.18);
}

:deep(.el-popper),
:deep(.el-select-dropdown) {
  background: rgba(4, 33, 56, 0.96) !important;
  border-color: rgba(122, 214, 240, 0.3) !important;
}

:deep(.el-select-dropdown__item) {
  color: rgba(232, 251, 255, 0.82);
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover),
:deep(.el-select-dropdown__item.selected) {
  color: #80d8ed;
  background: rgba(42, 138, 177, 0.24);
}

.mytable :deep(.tablecon),
.rightdown :deep(.tablecon) {
  background: transparent;
  color: #ffffff;
  position: relative;
  z-index: 1;
}

.mytable :deep(.subtable .title),
.rightdown :deep(.subtable .title),
.rightup :deep(.table-title) {
  height: 44px;
  width: 100%;
  display: flex;
  color: #ffffff;
  letter-spacing: 0;
  justify-content: flex-start;
  align-items: center;
  padding: 0 18px;
  font-size: 16px;
  font-weight: 600;
  line-height: 44px;
  border-bottom: 1px solid rgba(122, 214, 240, 0.16);
  background: linear-gradient(90deg, rgba(42, 138, 177, 0.62), rgba(128, 216, 237, 0.08));
  box-sizing: border-box;
}

.mytable :deep(.subtable .title::before),
.rightdown :deep(.subtable .title::before),
.rightup :deep(.table-title::before) {
  content: "";
  width: 4px;
  height: 18px;
  margin-right: 10px;
  border-radius: 2px;
  background: #80d8ed;
  box-shadow: 0 0 12px rgba(128, 216, 237, 0.55);
}

.mytable :deep(.subtable),
.rightdown :deep(.subtable) {
  position: relative;
  display: flex;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
}

.rightup :deep(.stateChart .tagzone) {
  color: #ffffff;
  background: rgba(3, 24, 45, 0.38);
  border-bottom-color: rgba(122, 214, 240, 0.16);
}

.mytable :deep(.subtable .avue-crud),
.rightdown :deep(.subtable .avue-crud) {
  flex: 1;
  min-height: 0;
}

.mytable :deep(.subtable .avue-crud__header),
.rightdown :deep(.subtable .avue-crud__header) {
  position: static;
  min-height: 0;
  padding: 0;
  margin: 0;
  background: transparent !important;
}

.mytable :deep(.subtable .avue-crud__header),
.rightdown :deep(.subtable .avue-crud__header),
.mytable :deep(.subtable .avue-crud__menu),
.rightdown :deep(.subtable .avue-crud__menu),
.mytable :deep(.subtable .avue-crud__pagination),
.rightdown :deep(.subtable .avue-crud__pagination) {
  color: rgba(255, 255, 255, 0.86);
  background: transparent !important;
}

.mytable :deep(.subtable .avue-crud__right),
.rightdown :deep(.subtable .avue-crud__right) {
  margin: 0;
  display: flex;
  align-items: center;
}

.mytable :deep(.subtable .avue-crud__right .el-button),
.rightdown :deep(.subtable .avue-crud__right .el-button) {
  min-height: 28px;
  border-radius: 6px;
  color: #80d8ed !important;
  border: 1px solid rgba(122, 214, 240, 0.32) !important;
  background: rgba(42, 138, 177, 0.18) !important;
}

.mytable :deep(.subtable .avue-crud__right .el-button:hover),
.rightdown :deep(.subtable .avue-crud__right .el-button:hover) {
  background: rgba(42, 138, 177, 0.28) !important;
}

.mytable :deep(.subtable .avue-crud__body),
.rightdown :deep(.subtable .avue-crud__body) {
  flex: 1;
  min-height: 0;
}

.mytable :deep(.subtable .el-table),
.rightdown :deep(.subtable .el-table),
.mytable :deep(.subtable .el-table__expanded-cell),
.rightdown :deep(.subtable .el-table__expanded-cell) {
  color: rgba(255, 255, 255, 0.88) !important;
  background: transparent !important;
}

.mytable :deep(.subtable .el-table th.el-table__cell),
.mytable :deep(.subtable .el-table tr),
.mytable :deep(.subtable .el-table td.el-table__cell),
.rightdown :deep(.subtable .el-table th.el-table__cell),
.rightdown :deep(.subtable .el-table tr),
.rightdown :deep(.subtable .el-table td.el-table__cell) {
  color: rgba(232, 251, 255, 0.88) !important;
  background: transparent !important;
  border-bottom-color: rgba(122, 214, 240, 0.12) !important;
}

.mytable :deep(.subtable .el-table th.el-table__cell),
.rightdown :deep(.subtable .el-table th.el-table__cell) {
  color: #dffcff !important;
  background: rgba(42, 138, 177, 0.28) !important;
}

.mytable :deep(.subtable .el-tag),
.rightdown :deep(.subtable .el-tag) {
  border-color: rgba(122, 214, 240, 0.38);
  background: rgba(42, 138, 177, 0.18);
  color: #80d8ed;
}

.mytable :deep(.subtable .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell),
.rightdown :deep(.subtable .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background: rgba(42, 138, 177, 0.2) !important;
}

.rightup :deep(.stateChart) {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  color: #ffffff;
  position: relative;
  z-index: 1;
}

.rightup :deep(.stateChart > div:not(.table-title):not(#chartzone)) {
  min-height: 42px;
  padding: 6px 14px 0;
  align-items: center;
}

.rightup :deep(.stateChart > div:not(.table-title):not(#chartzone) .el-button) {
  margin-top: 0 !important;
  color: #80d8ed;
}

.rightup :deep(#chartzone) {
  flex: 1;
  min-height: 0;
}
</style>
