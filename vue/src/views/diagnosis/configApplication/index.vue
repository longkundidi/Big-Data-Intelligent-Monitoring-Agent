<template>
  <el-container id="containergraph" class="app-template-page">
    <el-container class="containerProHeader">
      <el-header class="selectHeader">
        <el-form class="selectForm" ref="form" :model="formdata" label-width="72px">
          <el-form-item class="selectItem query-title">
            <h3>应用模版配置</h3>
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
            <fault-table ref="refFault" v-if="isActivate"></fault-table>
          </div>
        </div>
        <div class="rightzone">
          <div class="rightup config-panel">
            <my-chart ref="refChart"></my-chart>
            <div class="arrow-container">
              <el-icon @click="changeIndex('left', '状态评估')" class="arrow-left">
                <DArrowLeft />
              </el-icon>
              <el-icon @click="changeIndex('right', '状态评估')" class="arrow-right">
                <DArrowRight />
              </el-icon>
            </div>
          </div>
          <div class="rightdown">
            <div class="left config-panel">
              <diagnosis-chart ref="refDiagnosisChart"></diagnosis-chart>
              <div class="arrow-container">
                <el-icon @click="changeIndex('left', '故障诊断')" class="arrow-left">
                  <DArrowLeft />
                </el-icon>
                <el-icon @click="changeIndex('right', '故障诊断')" class="arrow-right">
                  <DArrowRight />
                </el-icon>
              </div>
            </div>
            <div class="right config-panel">
              <preprocess-chart ref="refPreprocessChart"></preprocess-chart>
              <div class="arrow-container">
                <el-icon @click="changeIndex('left', '预处理')" class="arrow-left">
                  <DArrowLeft />
                </el-icon>
                <el-icon @click="changeIndex('right', '预处理')" class="arrow-right">
                  <DArrowRight />
                </el-icon>
              </div>
            </div>
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
import diagnosisChart from './chart/diagnosisChart'
import preprocessChart from './chart/preprocessChart'
import faultTable from './myFault.vue'
import {fetchCompositionTaskList, getComponentNodes} from "@/api/sw/model3d/configModel/table";
import {getProductModelBySceneName, getProductModels, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
import {ElMessage} from "element-plus";
import {DArrowLeft, DArrowRight} from "@element-plus/icons-vue";


let globeParams = {}     //  声明一个全局参数对象

export default {

  components: {
    DArrowRight,
    DArrowLeft,
    modelGbomTree,            //  挂载树组件
    modelTable,              //  挂载表格组件
    myChart,
    diagnosisChart,
    preprocessChart,
    faultTable
  },


  setup() {
    let treeRef = ref(null)
    let refChart = ref(null)
    let refDiagnosisChart = ref(null)
    let refPreprocessChart = ref(null)
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

    const curComposition = ref({})
    const enIndex = ref(0); // 图表1的index
    const preIndex = ref(0); // 图表2的index
    const failIndex = ref(0); // 图表3的index

    // 存储当前的taskId和algoName
    const curTaskId = ref('');
    const enAlgoName = ref('');
    const preAlgoName = ref('');
    const failAlgoName = ref('');

    return {
      treeRef,
      refChart,
      refDiagnosisChart,
      refPreprocessChart,
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
      curComposition,
      enIndex,
      preIndex,
      failIndex,
      curTaskId,
      enAlgoName,
      preAlgoName,
      failAlgoName
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
      this.getCompositions(node.id)
      this.getTaskId("预处理")
      this.getTaskId("状态评估")
    },

    //  根据节点Id，刷新状态感知模板数据
    async getCompositions(nodeId) {
      let res = await fetchCompositionTaskList({ nodeId: nodeId })
      let compositionTasks = res.data.data

      if (res.data.code === 0 && compositionTasks !== null) {
        const currentItem = compositionTasks[0];
        if (currentItem) {
          // 分组 process 内容
          // 设置分组后的 process 到 perceivedTask 中
          this.groupProcessByType(currentItem.process);
          this.curTaskId = currentItem.taskId
          this.enAlgoName = this.curComposition['状态评估'][0].almodelShortName;
          this.failAlgoName = this.curComposition['故障诊断'][0].almodelShortName;
          this.preAlgoName = this.curComposition['预处理'][0].almodelShortName;
        }
      }
    },

    // 根据 almodelType 分组 process 内容，并且排序
    groupProcessByType(processArray) {
      this.curComposition = {
        '状态评估': [],
        '故障诊断': [],
        '预处理': []
      };

      // 分组
      processArray.forEach(item => {
        if (item.almodelType === '状态评估') {
          this.curComposition['状态评估'].push(item);
        } else if (item.almodelType === '故障诊断') {
          this.curComposition['故障诊断'].push(item);
        } else {
          this.curComposition['预处理'].push(item);
        }
      });

      // 对每个分组按照 sequence 排序
      Object.keys(this.curComposition).forEach(groupKey => {
        this.curComposition[groupKey].sort((a, b) => a.sequence - b.sequence);
      });

    },

    changeIndex (direction, group) {

      if (Object.keys(this.curComposition).length === 0) {
        ElMessage.warning('请选择风机结构树节点！');
        return;
      }

      const updateIndex = (groupKey, index, maxIndex) => {
        if (direction === 'left' && index > 0) {
          index--;
        } else if (direction === 'right' && index < maxIndex) {
          index++;
        } else {
          ElMessage.warning(`已经是${direction === 'left' ? '第一个' : '最后一个'}${groupKey}模型！`)
          return null;
        }
        return { index, algoName: this.curComposition[groupKey][index].almodelShortName };
      };

      // 根据分组选择更新方式
      let result;
      if (group === '状态评估') {
        result = updateIndex(group, this.enIndex, this.curComposition[group].length - 1);
        if (result !== null) {
          this.enIndex = result.index;
          this.enAlgoName = result.algoName;
        }
      } else if (group === '故障诊断') {
        result = updateIndex(group, this.failIndex, this.curComposition[group].length - 1);
        if (result !== null) {
          this.failIndex = result.index;
          this.failAlgoName = result.algoName;
        }
      } else {
        result = updateIndex(group, this.preIndex, this.curComposition[group].length - 1);
        if (result !== null) {
          this.preIndex = result.index;
          this.preAlgoName = result.algoName;
        }
      }
      this.getTaskId(group)

    },

    getTaskId(group) {
      this.$nextTick(() => {
        let chartRef;
        let algoName;

        if (group === '状态评估') {
          chartRef = this.refChart;
          algoName = this.enAlgoName;
        } else if (group === '故障诊断') {
          chartRef = this.refDiagnosisChart;
          algoName = this.failAlgoName;
        } else {
          chartRef = this.refPreprocessChart;
          algoName = this.preAlgoName;
        }

        // Load the appropriate chart or show empty chart
        if (this.curTaskId !== '' && algoName !== '') {
          chartRef.loadChart(this.curTaskId, algoName, globeParams.curNode);
          if (group === '预处理') {
            this.refFault.getTask(this.curTaskId, algoName);
          }
        } else {
          chartRef.showEmptyChart(globeParams.curNode);
          if (group === '预处理') {
            this.refFault.clear();
          }
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
      this.refChart.clear()
      this.refPreprocessChart.clear()
      this.refFault.clear()
      if (this.searchProject === false) {
        this.formdata = {}
        ElMessage({
          message: "请选择查询条件！",
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
        let response = await getComponentNodes({proId: this.formdata.projectId, nodeLevel: nodeLevel})
        if ((response.data.data)&&(response.data.data.length > 0)) {
          this.treeData = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          rootNodes.sort((a, b) => {
            const lastPartA = a.turbineCode.split('-').pop()
            const lastPartB = b.turbineCode.split('-').pop()

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
            let children = response.data.data
                .filter(ele => ele.turbineCode === item.turbineCode)
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

    //  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
    setChildren(pNode, nodeList) {
      let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList)
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

    //正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
    getChildrenByNodeCode(pNodeCode, nodeList) {
      let sonNodes = []
      let otherNodes = []
      let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
      nodeList.forEach((item) => {
        if (regex.test(item.nodeCode))          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
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
    this.refPreprocessChart.clear()
    this.refFault.clear()
    //next()让其跳转
    next()
  }

}

</script>
<style lang="scss" scoped>
.app-template-page {
  height: calc(100vh - 88px);
  min-height: 720px;
  padding: 18px;
  color: #e8fbff;
  position: relative;
  background: #061222 url('/img/wel/bg-elevator.png') center bottom / cover no-repeat;
  overflow: hidden;
  box-sizing: border-box;
}

.app-template-page::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(1, 9, 22, 0.78) 0%, rgba(3, 21, 35, 0.52) 42%, rgba(1, 8, 18, 0.86) 100%),
    radial-gradient(circle at 24% 20%, rgba(65, 228, 187, 0.2), transparent 28%),
    radial-gradient(circle at 78% 18%, rgba(58, 151, 248, 0.22), transparent 34%);
}

.app-template-page::after {
  content: "";
  position: absolute;
  inset: 14px;
  z-index: 0;
  pointer-events: none;
  border: 1px solid rgba(65, 228, 187, 0.2);
  box-shadow:
    inset 0 0 26px rgba(65, 228, 187, 0.08),
    inset 0 0 80px rgba(58, 151, 248, 0.08);
}

.containerProHeader {
  display: flex;
  flex-direction: column;
  min-height: 0;
  width: 100%;
  position: relative;
  z-index: 1;
}

.mycontainer {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 16px;
  overflow: hidden;

  .leftzone {
    width: 36%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 16px;
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
    gap: 16px;
    min-width: 0;
  }

  .rightup {
    width: 100%;
    height: 52%;
    position: relative;
  }

  .rightdown {
    width: 100%;
    height: 48%;
    display: flex;
    gap: 16px;
    overflow: hidden;
  }

  .left {
    width: 50%;
    height: 100%;
    position: relative;
  }

  .right {
    width: 50%;
    height: 100%;
    position: relative;
  }

  .config-panel {
    min-height: 0;
    overflow: hidden;
    position: relative;
    background:
      linear-gradient(180deg, rgba(9, 43, 58, 0.58), rgba(2, 19, 34, 0.62)),
      rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(87, 217, 255, 0.22);
    border-radius: 8px;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.08),
      inset 0 0 42px rgba(65, 228, 187, 0.04),
      0 20px 52px rgba(0, 0, 0, 0.34);
    backdrop-filter: blur(12px) saturate(128%);
  }

  .config-panel::before {
    content: "";
    position: absolute;
    inset: 0;
    pointer-events: none;
    background:
      linear-gradient(90deg, rgba(65, 228, 187, 0.08), transparent 22%, transparent 78%, rgba(44, 180, 255, 0.06)),
      repeating-linear-gradient(180deg, rgba(255, 255, 255, 0.025) 0, rgba(255, 255, 255, 0.025) 1px, transparent 1px, transparent 38px);
    opacity: 0.8;
  }

  .panel-title {
    height: 44px;
    padding: 0 18px;
    display: flex;
    align-items: center;
    color: #dffcff;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.5px;
    border-bottom: 1px solid rgba(65, 228, 187, 0.24);
    background:
      linear-gradient(90deg, rgba(65, 228, 187, 0.24), rgba(44, 180, 255, 0.1), transparent),
      rgba(1, 18, 32, 0.45);
    box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.04);
  }

  .panel-title::before {
    content: "";
    width: 4px;
    height: 18px;
    margin-right: 10px;
    border-radius: 2px;
    background: #41e4bb;
    box-shadow: 0 0 12px rgba(65, 228, 187, 0.9);
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

  .arrow-container {
    position: absolute;
    top: 50%; /* 垂直居中 */
    width: 100%;
    display: flex;
    justify-content: space-between; /* 左右对齐 */
    transform: translateY(-50%); /* 确保垂直居中 */
    pointer-events: none;
  }

  .arrow-left, .arrow-right {
    margin: 0 8px;
    padding: 9px;
    font-size: 16px;
    color: rgba(220, 252, 255, 0.9);
    cursor: pointer;
    border-radius: 50%;
    background: rgba(2, 24, 42, 0.78);
    border: 1px solid rgba(65, 228, 187, 0.28);
    box-shadow: 0 0 16px rgba(65, 228, 187, 0.12);
    transition: all 0.2s;
    pointer-events: auto;
  }

  .arrow-left:hover, .arrow-right:hover {
    color: #fff;
    background: rgba(65, 228, 187, 0.32);
    box-shadow: 0 0 22px rgba(65, 228, 187, 0.28);
  }
}

.selectHeader {
  flex: 0 0 82px;
  display: flex;
  width: 100%;
  margin: 0 0 16px 0;
  align-items: center;
  padding: 0 22px;
  border-radius: 8px;
  background:
    linear-gradient(90deg, rgba(8, 47, 67, 0.72), rgba(5, 28, 47, 0.62) 44%, rgba(4, 24, 42, 0.5)),
    rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(87, 217, 255, 0.24);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.08),
    0 18px 44px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(12px);
  box-sizing: border-box;
}

.selectForm {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 18px;
}

.selectItem {
  width: 260px;
  margin: 0;
}

.query-title {
  width: auto;
  min-width: 180px;

  h3 {
    margin: 0 18px 0 0;
    color: #e8fbff;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 1.5px;
    white-space: nowrap;
    text-shadow: 0 0 16px rgba(65, 228, 187, 0.28);
  }

  h3::after {
    content: "";
    display: block;
    width: 64px;
    height: 2px;
    margin-top: 8px;
    background: linear-gradient(90deg, #41e4bb, transparent);
    box-shadow: 0 0 14px rgba(65, 228, 187, 0.8);
  }
}

.selectAction {
  margin: 0;
}

::v-deep(.el-form-item__label) {
  color: rgba(232, 251, 255, 0.84);
  font-weight: 500;
}

::v-deep(.el-select .el-input__wrapper) {
  background: rgba(1, 17, 31, 0.68);
  border: 1px solid rgba(87, 217, 255, 0.32);
  box-shadow: none;
  min-height: 38px;
}

::v-deep(.el-select .el-input__inner) {
  color: #e8fbff;
}

::v-deep(.el-button.normalBtn) {
  min-width: 84px;
  border: 0;
  border-radius: 4px;
  color: #031f2b;
  font-weight: 600;
  background: linear-gradient(90deg, #9af5db, #41e4bb);
  box-shadow: 0 0 18px rgba(65, 228, 187, 0.24);
}

::v-deep(.el-popper),
::v-deep(.el-select-dropdown) {
  background: rgba(4, 33, 56, 0.96) !important;
  border-color: rgba(65, 228, 187, 0.3) !important;
}

::v-deep(.el-select-dropdown__item) {
  color: rgba(232, 251, 255, 0.82);
}

::v-deep(.el-select-dropdown__item.hover),
::v-deep(.el-select-dropdown__item:hover),
::v-deep(.el-select-dropdown__item.selected) {
  color: #8de7cf;
  background: rgba(65, 228, 187, 0.16);
}
</style>
