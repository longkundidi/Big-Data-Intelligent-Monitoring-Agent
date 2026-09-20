<template>
  <el-container id="containergraph" class="wind-turbine-page">
    <el-container class="containerProHeader">
      <el-header class="selectHeader">
        <el-form class="selectForm" ref="form" :model="formdata">
          <el-form-item class="query-title">
            <h3>查询条件：</h3>
          </el-form-item>
          <el-form-item class="selectItem" label="项目" prop="name" size="large">
            <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                       :placeholder="formdata.project" size="default" @clear="searchProject = false">
              <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.project"
                         @click="searchProject=true;formdata.project = item.project; formdata.projectId = ''; findProductModels(item.project,item.id)"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectItem" label="产品机型" prop="productModel">
            <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                       :placeholder="formdata.productModel">
              <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.productModel"
                         @click="formdata.productModel = item.productModel; formdata.projectId = item.id;">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="actionItem">
            <el-button class="normalBtn" @click="search()">查询实例电梯
            </el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <div class="mycontainer">
        <div class="mytree">
          <model-gbom-tree ref="treeRef" @clickNode="clickNode"></model-gbom-tree>
          <div v-if="treeLoading || treeData.length === 0" class="state-empty">
            <div class="state-empty__title">{{ treeLoading ? '正在加载实例树' : treeEmptyText }}</div>
            <div class="state-empty__desc">选择项目和产品机型后，系统会加载可配置的实例节点。</div>
          </div>
        </div>
        <div class="mytable">
          <model-table ref="refTable" v-if="isActivate"></model-table>                       <!--布局表格组件-->
          <div v-else class="table-empty">
            <div class="state-empty__title">请选择左侧实例节点</div>
            <div class="state-empty__desc">选中实例后可生成、编辑并运行状态感知任务。</div>
          </div>
        </div>
      </div>
    </el-container>
  </el-container>
</template>
<script>
import { ref } from 'vue'
import modelGbomTree from './tree.vue'
import modelTable from './table.vue'
import {getAllInstanceTreeNodes} from "@/api/sw/model3d/configModel/table";
import {getProductModelBySceneName, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";
import {ElMessage} from "element-plus";

let globeParams = {}     //  声明一个全局参数对象

export default {

  components: {
    modelGbomTree,            //  挂载树组件
    modelTable,              //  挂载表格组件
  },


  setup() {
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
    let treeLoading = ref(false)
    let treeEmptyText = ref('请选择项目和产品机型后查询实例')

    return {
      treeRef,
      refTable,
      isActivate,
      formdata,
      searchProject,
      treeData,
      defaultExpandKeys,
      expandKeys,
      projectList,
      productModelList,
      treeLoading,
      treeEmptyText
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
      if (!node || node.id === 'loading') return
      globeParams.curNode = node
      if (this.isActivate && this.refTable) {
        this.refTable.getModels(node)       //  根据节点id，调用子组件的方法，查询节点关联的感知模型
      } else {           //  如果是第一次页面加载
        this.isActivate = true          //  激活refTable
        this.$nextTick(() => {
          if (this.refTable) {
            this.refTable.getModels(node)
          }
        })
      }
    },

    getCurNode() {
      return globeParams.curNode
    },

    async search() {
      this.isActivate = false
      globeParams.curNode = null
      this.treeLoading = false
      this.treeData = []
      this.expandKeys = []
      this.defaultExpandKeys = []
      if (this.treeRef) {
        this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
      }
      if (this.searchProject === false || !this.formdata.projectId || !this.formdata.productModel) {
        this.treeEmptyText = '请选择项目和产品机型后查询实例'
        ElMessage({
          message: "请选择项目和产品机型后再查询！",
          type: 'warning'
        })
      } else {
        this.treeLoading = true
        this.treeEmptyText = '正在加载实例树'
        const hasTree = await this.getTreeNodes(99)
        this.treeLoading = false
        if (this.treeRef) {
          this.treeRef.init(this.treeData, this.expandKeys, this.defaultExpandKeys)
        }
        if (hasTree) {
          const firstNode = this.findFirstSelectableNode(this.treeData)
          if (firstNode) {
            this.$nextTick(() => {
              if (this.treeRef && this.treeRef.$refs && this.treeRef.$refs.tree) {
                this.treeRef.$refs.tree.setCurrentKey(firstNode.id)
              }
              this.clickNode(firstNode)
            })
          }
        } else {
          this.treeEmptyText = '当前项目机型下暂无实例，请先完成实例化配置'
        }
      }
    },

    // 根据节点层级数，加载结构树的一组节点
    async getTreeNodes(nodeLevel) {
      try {
        const level = Number(nodeLevel) > 0 ? Number(nodeLevel) : 99
        let response = await getAllInstanceTreeNodes({proId: this.formdata.projectId, nodeLevel: level}, {silentError: true})
        const allNodes = Array.isArray(response.data.data) ? response.data.data : []
        if (allNodes.length > 0) {
          this.treeData = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = allNodes.filter(ele => this.isRootNode(ele))
          if (rootNodes.length === 0) {
            const levelList = allNodes
                .map(item => Number(item.nodeLevel))
                .filter(level => !Number.isNaN(level))
            if (levelList.length > 0) {
              const minLevel = Math.min(...levelList)
              rootNodes = allNodes.filter(item => Number(item.nodeLevel) === minLevel)
            } else {
              rootNodes = allNodes.slice(0, 1)
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
            let children = allNodes
            if (item.turbineCode !== undefined && item.turbineCode !== null && item.turbineCode !== '') {
              children = allNodes.filter(ele => ele.turbineCode === item.turbineCode)
            }
            await this.setChildren(item, children)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点
          return true
        }else {
          this.treeData = []
          this.expandKeys = []
          this.defaultExpandKeys = []
          return false
        }
      } catch (error) {
        console.log(error)
        this.treeData = []
        this.expandKeys = []
        this.defaultExpandKeys = []
        this.treeEmptyText = '实例树加载失败，请稍后重试'
        return false
      }
    },

    findFirstSelectableNode(nodes = []) {
      const backendSafeNode = this.findFirstNode(nodes, (node) => {
        const nodeType = String(node.nodeType || '').toLowerCase()
        const nodeCode = String(node.nodeCode || '')
        return !nodeType.includes('root') && nodeCode.includes('-')
      })
      if (backendSafeNode) return backendSafeNode

      const nonRootNode = this.findFirstNode(nodes, (node) => {
        const nodeType = String(node.nodeType || '').toLowerCase()
        return !nodeType.includes('root')
      })
      return nonRootNode || this.findFirstNode(nodes)
    },

    findFirstNode(nodes = [], matcher = () => true) {
      for (const node of nodes) {
        if (!node || !node.id || node.id === 'loading') {
          continue
        }
        if (matcher(node)) {
          return node
        }
        const child = this.findFirstNode(Array.isArray(node.children) ? node.children : [], matcher)
        if (child) {
          return child
        }
      }
      return null
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
      // 兼容多种编码分隔符，仅匹配“下一层”子节点。
      let regex = new RegExp('^' + escapedNodeCode + '[-_/:.][^-_/:.]+$')
      nodeList.forEach((item) => {
        const parentRef = item.parentNodeId ?? item.parentId ?? item.pid ?? item.parent_id
        const byParentId = parentRef !== undefined && parentRef !== null
            && (String(parentRef) === String(pNodeId) || String(parentRef) === String(pNodeNodeId))
        const byNodeCode = regex.test(String(item.nodeCode || ''))

        if (byParentId || byNodeCode)          //  判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
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
.wind-turbine-page {
  height: 100vh;
  padding: 12px;
  background:
      radial-gradient(circle at 72% 12%, rgba(56, 175, 225, 0.16), transparent 30%),
      radial-gradient(circle at 18% 82%, rgba(78, 219, 207, 0.1), transparent 28%),
      linear-gradient(135deg, rgba(4, 30, 56, 0.94), rgba(2, 14, 30, 0.98));
  overflow: hidden;
}

.containerProHeader {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mycontainer {
  flex: 1;
  min-height: 0;
  height: 100%;
  width: 100%;
  display: flex;
  gap: 12px;

  .mytree {
    flex: 0 0 31%;
    height: 100%;
    min-width: 320px;
    overflow: hidden;
    position: relative;
    border: 1px solid rgba(122, 214, 240, 0.2);
    border-radius: 8px;
    background:
        radial-gradient(circle at 32% 8%, rgba(78, 219, 207, 0.12), transparent 30%),
        linear-gradient(180deg, rgba(7, 50, 78, 0.72), rgba(3, 25, 48, 0.88));
    box-shadow: 0 16px 36px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.06);
  }

  .mytable {
    flex: 1;
    height: 100%;
    min-width: 0;
    overflow-y: auto;
    position: relative;
    border: 1px solid rgba(122, 214, 240, 0.18);
    border-radius: 8px;
    background: linear-gradient(180deg, rgba(6, 45, 72, 0.62), rgba(3, 24, 45, 0.82));
    box-shadow: 0 16px 36px rgba(0, 8, 18, 0.24), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  }
}

.state-empty,
.table-empty {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  text-align: center;
  color: rgba(232, 251, 255, 0.78);
  background:
      radial-gradient(circle at center, rgba(42, 138, 177, 0.16), transparent 42%),
      rgba(3, 24, 45, 0.3);
  box-sizing: border-box;
}

.state-empty__title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.state-empty__desc {
  max-width: 360px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(232, 251, 255, 0.68);
}

.selectHeader {
  flex: 0 0 68px;
  display: flex;
  width: 100%;
  height: 68px;
  margin: 0;
  padding: 0 18px;
  align-items: center;
  border: 1px solid rgba(122, 214, 240, 0.18);
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(7, 48, 76, 0.64), rgba(3, 25, 48, 0.78));
  box-shadow: 0 12px 30px rgba(0, 8, 18, 0.22);
}

.selectForm {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 0;
}

.query-title {
  flex: 0 0 112px;
  margin: 0;

  h3 {
    margin: 0;
    color: #ffffff;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0;
  }
}

.selectItem {
  flex: 0 0 300px;
  margin: 0;
}

.actionItem {
  flex: 1;
  margin: 0;
}

.normalBtn{
  height: 32px;
  padding: 0 14px;
  color: #ffffff;
  border: 1px solid rgba(122, 214, 240, 0.32);
  border-radius: 6px;
  background: linear-gradient(180deg, rgba(42, 122, 154, 0.82), rgba(13, 68, 98, 0.92));
  box-shadow: 0 8px 18px rgba(0, 10, 22, 0.18);

  &:hover {
    color: #ffffff;
    border-color: rgba(168, 232, 248, 0.52);
    background: linear-gradient(180deg, rgba(52, 145, 178, 0.9), rgba(17, 82, 114, 0.96));
  }
}

:deep(.selectHeader .el-form-item__label) {
  color: rgba(255, 255, 255, 0.82);
}

:deep(.selectHeader .el-input__wrapper),
:deep(.selectHeader .el-select__wrapper) {
  color: #ffffff;
  background: rgba(3, 24, 45, 0.72);
  border: 1px solid rgba(122, 214, 240, 0.24);
  box-shadow: 0 0 0 1px rgba(122, 214, 240, 0.14) inset;
}

:deep(.selectHeader .el-input__inner) {
  color: #ffffff;
}

:deep(.selectHeader .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.45);
}

.mytable :deep(.tablecon) {
  background: transparent;
}

.mytable :deep(.subtable .title) {
  height: 44px;
  color: #ffffff;
  background: linear-gradient(90deg, rgba(42, 138, 177, 0.62), rgba(128, 216, 237, 0.08));
  border-bottom: 1px solid rgba(122, 214, 240, 0.16);
}

@media (max-width: 1280px) {
  .selectForm {
    gap: 10px;
  }

  .query-title {
    flex-basis: 100px;
  }

  .selectItem {
    flex-basis: 250px;
  }

  .mycontainer .mytree {
    flex-basis: 34%;
    min-width: 280px;
  }
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
