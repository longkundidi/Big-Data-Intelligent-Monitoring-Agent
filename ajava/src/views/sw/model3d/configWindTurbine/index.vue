<template>
  <el-container id="containergraph" style="height: 100vh;">
    <el-container class="containerProHeader">
      <el-header class="selectHeader">
        <el-form class="selectForm" ref="form" :model="formdata"
                 style="margin-top: 20px; margin-left: 200px">
          <el-form-item style="width: 200px;margin-left: 70px">
            <h3>查询条件：</h3>
          </el-form-item>
          <el-form-item class="selectItem" label="项目" prop="name" size="large" style="width: 300px">
            <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                       :placeholder="formdata.project" size="default" @clear="searchProject = false">
              <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                         @click="searchProject=true;formdata.project = item.project; formdata.projectId = item.id; findProductModels(item.project,item.id)"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectItem" label="产品机型" prop="productModel" style="width: 300px;margin-right: 20px">
            <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                       :placeholder="formdata.productModel">
              <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                         @click="formdata.productModel = item.productModel; formdata.project = item.project; formdata.projectId = item.id;">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item style="width: 500px;margin-left: 10px;">
            <el-button class="normalBtn" @click="search()">查询实例风机
            </el-button>
            <el-button class="normalBtn" @click="match()">数据完整性校验
            </el-button>
            <match ref="matchDlg" @showTree="showTree"></match>
          </el-form-item>
        </el-form>
      </el-header>
      <div class="mycontainer">
        <div class="mytree">
          <model-gbom-tree ref="treeRef" @clickNode="clickNode"></model-gbom-tree>
        </div>
        <div class="mytable">
          <model-table ref="refTable" v-if="isActivate"></model-table>                       <!--布局表格组件-->
        </div>
      </div>
    </el-container>
  </el-container>
</template>
<script>
import { ref } from 'vue'
import modelGbomTree from './tree.vue'
import modelTable from './table.vue'
import {getComponentNodes} from "@/api/sw/model3d/configModel/table";
import match from './match.vue'
import {getProductModelBySceneName, getProductModels, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";

let globeParams = {}     //  声明一个全局参数对象

export default {

  components: {
    modelGbomTree,            //  挂载树组件
    modelTable,              //  挂载表格组件
    match,
  },


  setup() {
    let matchDlg = ref(null)
    let treeRef = ref(null)
    let refTable = ref(null)
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

    return {
      matchDlg,
      treeRef,
      refTable,
      isActivate,
      formdata,
      searchProject,
      treeData,
      defaultExpandKeys,
      expandKeys,
      projectList,
      productModelList
    }
  },
  mounted() {
    this.projectInit()
    this.treeData = []
    this.defaultExpandKeys = []
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
      console.log("当前选择的项目 ID:", proId);  // 这里增加调试输出
      this.formdata.productModel = '';
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    clickNode(node) {
      globeParams.curNode = node
      if (this.isActivate)
        this.refTable.getModels(node)       //  根据节点id，调用子组件的方法，查询节点关联的感知模型
      else           //  如果是第一次页面加载
        this.isActivate = true          //  激活refTable
    },

    getCurNode() {
      return globeParams.curNode
    },

    async search() {
      this.isActivate = false
      this.treeData = []
      this.expandKeys = []
      this.defaultExpandKeys = []
      this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
      if (this.searchProject === false) {
        this.formdata = {}
      } else {
        await this.getTreeNodes(1)
        this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
      }
    },

    match() { //匹配操作
      this.isActivate = false
      this.treeData = []
      this.expandKeys = []
      this.defaultExpandKeys = []
      if (this.searchProject === false) {
        this.formdata = {}
      } else {
        this.matchDlg.init(this.formdata.project, this.formdata.productModel)
      }

    },

    async showTree(nodeLevel) {
      await this.getTreeNodes(nodeLevel)
      this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
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
    },

  },


}

</script>
<style lang="scss" scoped>
@import '@/styles/my-dialog.scss';
.mycontainer {
  height: 100%;
  width: 100%;
  display: flex;

  .mytree {
    height: 100%;
    width: 30%;
    background-color: -webkit-focus-ring-color;
    overflow-y: auto; /*  垂直滚动条   */
    background: rgba(255, 255, 255, 0.64);
    border: 1px solid rgba(43, 43, 43, 0.98);
  }

  .mytable {
    height: 100%;
    width: 70%;
    overflow-y: auto;
    margin-left: 5px;
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
  height: 3%;
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
  margin-right: 15px;
}
.normalBtn{
  height: 28px;
}
.treeDialog{
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  .el-button{
    background-color: cadetblue;
    width: 65px;
    height: 40px;
    margin-top: 15px;
    border-color: white;
    &:hover {
      background-color: chocolate; /* 设置悬浮时的背景色 */
    }
  }
}
</style>
