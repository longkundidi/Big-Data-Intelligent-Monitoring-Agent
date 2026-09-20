<template>
  <el-container id="containergraph" style="height: 100vh;">
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
            <div class="mytable">
              <model-table ref="refTable" v-if="isActivate" @getTaskId="getTaskId"></model-table>
              <!--布局表格组件-->
            </div>
          </div>
        </div>
        <div class="rightzone">
          <div class="rightup">
            <my-chart ref="refChart"></my-chart>
          </div>
          <div class="rightdown">
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
import {getComponentNodes} from "@/api/sw/model3d/configModel/table";
import {getProductModelBySceneName, getProductModels, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
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

    .leftup {
      width: 100%;
      height: 50%;
    }

    .leftdown {
      width: 100%;
      height: 50%;
    }
  }

  .rightzone {
    width: 60%;
    height: 100%;

    .rightup {
      width: 100%;
      height: 50%;
      background-color: -webkit-focus-ring-color;
      background: rgba(255, 255, 255, 0.64);
      border-top: 1px solid rgba(43, 43, 43, 0.98);
    }

    .rightdown {
      width: 100%;
      height: 50%;
      overflow-y: auto;
      border-top: 1px solid rgba(43, 43, 43, 0.98);
      border-bottom: 1px solid rgba(43, 43, 43, 0.98);
    }
  }

  .mytree {
    height: 100%;
    //width: 50%;
    background-color: -webkit-focus-ring-color;
    overflow-y: auto; /*  垂直滚动条   */
    background: rgba(255, 255, 255, 0.64);
    border: 1px solid rgba(43, 43, 43, 0.98);
  }

  .mytable {
    height: 100%;
    //width: 50%;
    overflow-y: auto;
    border: 1px solid rgba(43, 43, 43, 0.98);
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
