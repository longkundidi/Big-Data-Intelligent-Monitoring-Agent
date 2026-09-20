<template>
  <div class="mycontainer">
    <div class="scene-model-tree" v-show="display">
      <div class="button-container">
        <el-button class="reSetBtn" @click="refreshTree">刷新</el-button>
      </div>
      <el-tree class="tyTree" ref="sceneTree" :data="treeData" :props="defaultProps" node-key="id" default-expand-all
               highlight-current @node-click="handleClick" :expand-on-click-node="false">
      </el-tree>

    </div>
    <div class="mytree">
      <model-tree v-if="!isSelectOrShowTree" @clickNode="clickNode" :node-id="nodeId" :scene-id="sceneId" :node-type="nodeType"></model-tree>
    </div>
    <div class="mytable">
      <dataset-table ref="refTable" v-if="tableVisible" :node-type="nodeType"></dataset-table>
    </div>
  </div>

</template>

<script>
import ModelTree from './tree.vue'
import DatasetTable  from './table.vue'
import {addProductModel, buildSceneModelTree, fetchList} from "@/api/DatasetManage/index.js"
import {Check, Close, Delete, Edit, Plus} from '@element-plus/icons-vue'


let globeParams = {} // 全局变量

export default {
  components: {
    Edit,
    Plus,
    Delete,
    ModelTree,
    DatasetTable,
    Check,
    Close,
  },

  props: {
    isSelect: {
      type: Boolean,
      default: false
    },
    isFirstSelectVal: {
      type: Boolean,
      required: true
    }
  },

  data() {
    return {
      refTable: null,
      refSelectTable: null,
      isActivate: false,
      localIsSelect: false,
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      nodeId: '',//当前选中的节点id
      sceneId: '', //当前选中的场景id
      modelId: '', //当前选中的机型id
      nodeType: 'GBOM', //当前选中节点的类型（GBOM或） nodeType必须要有值，先默认为GBOM
      display: true,
      tableVisible:false
    }
  },

  created() {
    this.fetchSceneModelTree()
  },

  methods: {
    async clickNode(node) {
      globeParams.curNode = node // 缓存当前节点
      //显示数据集信息
      if (node.nodeType === 'Leaf') {
        this.tableVisible = true
        await this.$nextTick();
        this.$refs.refTable.getInit(node)
      }
    },

    getCurNode() {
      return globeParams.curNode
    },

    handleSelectedVariables(variables) {
      this.$emit('update:selected-variables', variables)
    },

    fetchSceneModelTree() {
      buildSceneModelTree().then(res => {
        if (res.data.data && Array.isArray(res.data.data)) {
          this.treeData = res.data.data
          // 默认选中第一个节点（如果存在）
          const firstNode = this.treeData[0]
          if (firstNode) {
            this.nodeId = firstNode.id
            // 设置 el-tree 默认选中项
            this.$nextTick(() => {
              this.$refs.sceneTree.setCurrentKey(this.nodeId)
              //手动调用一次点击处理逻辑，模拟点击
              this.handleClick(firstNode)
            })
          }
        }
      }).catch(error => {
        console.error('获取场景机型树失败：', error)
        this.$message.error('获取场景机型树失败')
      })
    },

    handleClick(data) {
      globeParams.curNode = data // 缓存当前点击的节点
      this.nodeId = data.id

      // 根据节点类型设置标志位
      if (data.bomModel === 'GBOM' || data.bomModel === 'MBOM') {
        // 保存节点类型，用于传递给tree.vue组件
        this.nodeType = data.bomModel

        // 如果是GBOM，设置sceneId
        if (data.bomModel === 'GBOM') {
          this.tableVisible=false
          this.sceneId = data.id
          this.isSelectOrShowTree = false // 显示variable-gbom-tree
        }
        // 如果是MBOM，设置modelId
        else if (data.bomModel === 'MBOM') {
          this.modelId = data.id
          // 获取父节点
          const node = this.$refs.sceneTree.getNode(data.id)
          if (node && node.parent && node.parent.data) {
            this.sceneId = node.parent.data.id
          } else {
            this.sceneId = ''
          }
          this.isSelectOrShowTree = false // 显示variable-gbom-tree
        }
      }
    },

    refreshData() {
      this.getSceneDataList()
    },

    getSceneDataList() {
      fetchList().then(res => {
        this.sceneList = res.data.data
      })
    },

    refreshTree() {
      // 重新获取场景模型树数据
      this.fetchSceneModelTree()
      // 重置相关状态
      this.isActivate = true
      this.localIsSelect = false
    },
  }
}
</script>

<style lang="scss" scoped>
.mycontainer {
  height: 100%;
  width: 100%;
  display: flex;
}

.add-scene-btn {
  margin-bottom: 16px;
  margin-left: 10%;
}

.refresh-scene-btn {
  margin-bottom: 16px;
  margin-left: 10%;
}

.scene-model-tree {
  height: 100%;
  width: 20%;
  background-color: white;
  overflow-y: hidden;
  border-right: 2px solid #edf3fd; /* 灰色边框 */

  .button-container {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    padding: 16px 20px;
    gap: 12px;
    border-bottom: 1px solid #ebeef5;
  }

  .add-scene-btn {
    margin: 0;
    font-size: 14px;
    height: 32px;
    padding: 0 16px;

    .el-icon {
      margin-right: 4px;
    }
  }

  .refresh-scene-btn {
    margin: 0;
    font-size: 14px;
    height: 32px;
    padding: 0 16px;
  }

  .custom-refresh-btn {
    background-color: #1abc9c !important; // 绿色
    color: #fff !important;
    border: none !important;
    border-radius: 18px !important; // 稍微小一点
    font-size: 14px;
    height: 32px; // 更小
    padding: 0 14px; // 更窄
    display: flex;
    align-items: center;

    .el-icon {
      margin-right: 4px; // 更小
      font-size: 16px; // 更小
    }
  }
}

.mytree {
  height: 100%;
  width: 20%;
  //background-color: -webkit-focus-ring-color;
  background-color: white;
  overflow-y: auto;
  border-right: 2px solid #edf3fd; /* 灰色边框 */
}

.mytable {
  height: 100%;
  width: 70%;
  background-color: white;
  overflow-y: auto;
}

.tyTree {
  width: 100%;
  height: 100%;
}

.custom-tree {
  display: flex;
  height: 100%;
  width: 100%;
  align-items: center; /*垂直对齐*/
  .tree-btn {
    display: flex;
    height: 100%;
    width: 100%;
    align-items: center; /*垂直对齐*/
    .btn {
      height: 100%;
      width: 30px;
    }
  }
}

.mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

:deep(.el-tree-node__content) {
  .input-container {
    display: inline-flex;
    align-items: center;

    .el-input {
      width: 200px;
    }

    .el-button {
      // padding: 2px;
      //margin-left: 2px;
    }
  }
}
</style>
