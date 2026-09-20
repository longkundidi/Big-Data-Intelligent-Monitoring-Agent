<template>
  <el-container id="containergraph" style="background-color: white; height: 100vh;">
    <el-container class="containerProHeader">
      <el-header class="selectHeader">
        <el-form class="selectForm" ref="form" :model="formdata" label-width="30%"
                 style="margin-top: 15px; margin-left: 200px">
          <el-form-item class="selectItem">
            <h3>查询条件：</h3>
          </el-form-item>
          <el-form-item class="selectItem" label="项目" prop="name" size="large" style="width: 300px">
            <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                       :placeholder="formdata.project" size="default" @clear="searchProject = false">
              <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                         @click="formdata.project=item.project; findProductModels(item.project,item.id)"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectItem" label="产品机型" prop="productModel" style="width: 300px;margin-right: 20px">
            <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                       :placeholder="formdata.productModel">
              <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                         @click="searchProject=true;formdata.productModel = item.productModel; formdata.projectId = item.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item style="width: 150px">
            <el-button class="normalBtn" size="large" :style="{ width: 'auto' }" @click="search()">查询
            </el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <div class="mycontainer">
        <div class="leftzone">
          <div class="leftup">
            <div class="mytree">
              <model-gbom-tree ref="treeRef" @clickNode="clickNode"></model-gbom-tree>
              <!--布局树组件-->
            </div>
          </div>
          <div class="leftdown">
            <fault-table ref="refFault" v-if="isActivate"></fault-table>
          </div>
        </div>
        <div class="rightzone">
          <div class="rightup">
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
            <div class="left">
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
            <div class="right">
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
.mycontainer {
  display: flex;
  height: 100vh; /*设置高度*/

  .leftzone {
    width: 40%;
    height: 100%;
    border-right: 1px solid #ccc;

    .leftup {
      width: 100%;
      height: 50%;
      border-top: 1px solid #ccc;
    }

    .leftdown {
      width: 100%;
      height: 50%;
      overflow-y: auto;
    }
  }

  .rightzone {
    width: 60%;
    height: 100%;
    display: flex;
    flex-direction: column; /* 让 rightzone 内的元素垂直排列 */
  }

  .rightup {
    width: 100%;
    height: 50%;
    position: relative;
  }

  .rightdown {
    width: 100%;
    height: 50%;
    display: flex; /* 让 left 和 right 横向排列 */
    overflow-y: auto;
  }

  .left {
    width: 50%;
    height: 100%;
    position: relative;
  }

  .right {
    width: 50%;
    height: 100%;
    border-left: 1px solid #ccc;
    position: relative;
  }


  .mytree {
    height: 100%;
    overflow-y: auto; /*  垂直滚动条   */
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
  }

  .arrow-left, .arrow-right {
    font-size: 15px;
    color: rgba(0, 0, 0, 0.6); /* 设置箭头透明度 */
    cursor: pointer;
    transition: color 0.3s;
  }

  .arrow-left:hover, .arrow-right:hover {
    color: rgba(0, 0, 0, 1); /* 悬停时箭头变为完全不透明 */
  }
}

.selectHeader {
  flex: 1;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  width: 80%;
  justify-content: space-between;
  height: 1%;
  line-height: 5%;
  margin: 1%;
  align-items: center;
}

.selectForm {
  width: 100%;
  display: flex;
  text-align: center;
  align-items: center;
}

.selectItem {
  width: 20%;
  line-height: 3%;
  margin-right: 10px;
}
</style>
