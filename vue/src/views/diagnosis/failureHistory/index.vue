<template>
  <el-container>
    <el-header>
      <el-row style="margin-left: 350px;margin-top: 20px">
        <el-col :span="2" style="margin-right: 20px">
          <h3>查询条件：</h3>
        </el-col>
        <el-col :span="5" style="margin-top: 15px; margin-right: 30px">
          <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                     :placeholder="formdata.project">
            <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                       @click="formdata.project=item.project; findProductModels(item.project,item.id)"
            >
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="5" style="margin-top: 15px; margin-right: 30px">
          <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                     :placeholder="formdata.productModel" @clear="searchProId=false">
            <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                       @click="formdata.productModel = item.productModel; formdata.projectId = item.id; searchProId=true">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="4" style="margin-top: 17px;">
          <el-button class="normalBtn" size="large" :style="{ width: 'auto' }" @click="search()">查 询
          </el-button>
        </el-col>
      </el-row>
    </el-header>
  </el-container>
  <el-container>
    <el-aside>
      <div class="mytree">
        <tree ref="treeRef" @clickNode="clickNode"></tree>
      </div>
    </el-aside>
    <el-main>
      <div class="mytable">
        <fault-table ref="refTable" v-if="isActivate"></fault-table>
      </div>
    </el-main>
  </el-container>
</template>
<script>
import {ref} from 'vue'
import tree from './tree.vue'
import faultTable from './table.vue'
import {getProductModelBySceneName, getProductModels, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
import {getComponentNodes} from "@/api/sw/model3d/configModel/table";

let globeParams = {}     //  声明一个全局参数对象

export default {

  components: {
    faultTable,
    tree,
  },


  setup() {

    let formdata = ref({
      project: '',
      projectId: '',
      productModel: '',
    })
    let projectList = ref([])
    let productModelList = ref([])
    let searchProId = ref(false)
    let treeRef = ref(null)
    let treeData = ref([])
    let defaultExpandKeys = ref([])
    let expandKeys = ref([])
    let isActivate = ref(false)
    let refTable = ref(null)


    return {
      formdata,
      projectList,
      productModelList,
      searchProId,
      treeRef,
      treeData,
      defaultExpandKeys,
      expandKeys,
      isActivate,
      refTable

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
      this.formdata.productModel = '';
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    clickNode(node) {
      globeParams.curNode = node
      if (this.isActivate)
        this.refTable.getFaultInfos(node)       //  根据节点id，调用子组件的方法，查询节点关联的感知模型
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
      if (this.searchProId === false) {
        this.formdata = {}
      } else {
        await this.getTreeNodes(2)
        this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
        this.isActivate = true
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


  }

}

</script>
<style lang="scss" scoped>

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  background-color: white;
}

.el-header {
  background-color: white;
  text-align: center;
  height: 100px;
}
.el-container:first-of-type{
  height: 100px;
}

.el-container:not(:first-of-type) {
  height: 100%;
  display: flex;
  flex-direction: row;
  background-color: white;
}

.el-main {
  flex-grow: 1;
  background-color: white;
}

.el-aside {
  background-color: white;
  text-align: center;
  width: 500px;
}

</style>
