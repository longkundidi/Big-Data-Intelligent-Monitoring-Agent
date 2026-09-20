<template>
  <div class="mycontainer">
    <div class="scene-model-tree" v-show="display">
      <div class="button-container">
        <el-button class="addBtn" @click="handleAdd">新增场景元模型</el-button>
        <el-button class="reSetBtn" @click="refreshTree">刷新</el-button>
      </div>
      <el-tree class="tyTree" ref="sceneTree" :data="treeData" :props="defaultProps" node-key="id" default-expand-all
               highlight-current @node-click="handleClick" :expand-on-click-node="false">
        <template #default="{ data }">
          <div class="custom-tree" @mouseover="mouseover(data)" @mouseleave="mouseout(data)">
            <span style="line-height: 16px;"> {{ data.label }} </span>
            <div class="tree-btn" v-show="data.myshow" @click.stop="">
              <span>&nbsp;&nbsp;&nbsp;&nbsp;</span>

              <el-tooltip v-if="data.bomModel === 'GBOM'" content="新增机型" placement="top" :disabled="showTooltip">
                <el-icon class="btn" @click="addNode(data)" color="black">
                  <Plus/>
                </el-icon>
              </el-tooltip>

              <!--              <el-tooltip content="节点编辑" placement="top">
                              <el-icon v-if="['GBOM', 'MBOM'].includes(data.bomModel)" class="btn" @click="editNode(data)"
                                       color="black">
                                <Edit/>
                              </el-icon>
                            </el-tooltip>-->

              <el-tooltip v-if="data.bomModel === 'MBOM'" content="复制机型" placement="top" :disabled="showTooltip">
                <el-icon class="btn" @click="copyNode(data)" color="black">
                  <DocumentCopy/>
                </el-icon>
              </el-tooltip>

              <el-tooltip v-if="['GBOM', 'MBOM'].includes(data.bomModel)" content="删除" placement="top"
                          :disabled="showTooltip">
                <el-icon class="btn" @click="deleteNode(data)"
                         color="red">
                  <Delete/>
                </el-icon>
              </el-tooltip>
              <el-tooltip content="图标帮助" placement="top" :disabled="false">
                <el-icon class="btn" @click="iconHelp()" :color="iconTooltipColor">
                  <QuestionFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-tree>
      <add v-model="addVisible" v-show="addVisible" ref="add" @onRefreshTree="refreshTree"/>
    </div>
    <div class="mytree">
      <variable-gbom-tree  ref="refTree"  v-if="!isSelectOrShowTree" @clickNode="clickNode" :node-id="nodeId" :scene-id="sceneId"
                          :node-type="nodeType" @onRefreshSceneTree="refreshTree"></variable-gbom-tree>
    </div>
    <div class="mytable">
      <variable-table ref="refTable" v-if="isActivate && !isSelect" :node-type="nodeType"></variable-table>
      <variable-table-select ref="refSelectTable" v-if="localIsSelect"
                             @update:selected-variables="handleSelectedVariables"></variable-table-select>
    </div>
  </div>
  <add-model v-model="addModelVisible" v-show="addModelVisible" ref="addModel" :scene-id="sceneId"
             @onRefreshTree="refreshTree"></add-model>
</template>

<script>
import variableGbomTree from './tree.vue'
import variableTable from './table.vue'
import variableTableSelect from './table-select.vue'
import {addProductModel, buildSceneModelTree, fetchList} from "@/api/diagnosis/scene/scene.js"
import {Check, Close, Delete, Edit, Plus} from '@element-plus/icons-vue'
import add from "@/views/scene/configSceneModel/sceneData-add.vue";
import {ElLoading, ElMessage} from "element-plus";
import {deleteSceneNode, deleteModelNode} from "@/api/diagnosis/scene/scene.js";
import {ElMessageBox} from 'element-plus';
import addModel from "./addModel.vue"
import {copyModelTreeAndVar} from "@/api/diagnosis/graphVis/dataIngestion";

let globeParams = {} // 全局变量

export default {
  components: {
    Edit,
    Plus,
    Delete,
    add,
    variableGbomTree,
    variableTable,
    variableTableSelect,
    Check,
    Close,
    addModel,
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
      addVisible: false, //添加场景元结构树
      addModelVisible: false, //添加机型
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      nodeId: '',//当前选中的节点id
      sceneId: '', //当前选中的场景id
      modelId: '', //当前选中的机型id
      nodeType: 'GBOM', //当前选中节点的类型（GBOM或MBOM） nodeType必须要有值，先默认为GBOM
      isSelectOrShowTree: false, //// 默认显示 variable-gbom-tree, 为true显示select-tree

      display: true,
      showTooltip: true,      //  是否开启图标文字提示
      iconTooltipColor: 'black',   //  图标提示按钮颜色
      editingNodeId: null, // 当前正在编辑的节点ID
      addOrUpdate: 'add',

    }
  },

  watch: {
    isFirstSelectVal(newVal) {
      if (newVal && this.localIsSelect) {
        this.$nextTick(() => {
          this.$refs.refSelectTable.clearSelected()
        })
      }
    }
  },

  created() {
    this.fetchSceneModelTree()
  },

  methods: {
    clickNode(node) {
      globeParams.curNode = node // 缓存当前节点

      if (this.isActivate && !this.isSelect) {
        this.$refs.refTable.getVariable(node, this.nodeType)
      } else if (!this.isActivate && this.isSelect) {
        this.localIsSelect = true
        this.$nextTick(() => {
          this.$refs.refSelectTable.getVariable(node)
        })
      } else {
        this.isActivate = true
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
          this.treeData = res.data.data;

          this.$nextTick(() => {
            let curNode;

            if (!this.nodeId) {
              const firstNode = this.treeData[0];
              if (firstNode) {
                this.nodeId = firstNode.id;
              }
            }

            // el-tree 渲染完之后再通过 nodeId 拿当前节点数据
            const nodeObj = this.$refs.sceneTree.getNode(this.nodeId);
            if (nodeObj) {
              curNode = nodeObj.data;
            }

            this.$refs.sceneTree.setCurrentKey(this.nodeId);
            this.handleClick(curNode);  // 模拟点击
          });
        }
      }).catch(error => {
        console.error('获取场景机型树失败：', error);
        this.$message.error('获取场景机型树失败');
      });
    },

    handleClick(data) {
      globeParams.curNode = data // 缓存当前点击的节点
      console.log('点击 el-tree 节点：', data)
      this.nodeId = data.id

      // 根据节点类型设置标志位
      if (data.bomModel === 'GBOM' || data.bomModel === 'MBOM') {
        // 保存节点类型，用于传递给tree.vue组件
        this.nodeType = data.bomModel

        // 如果是GBOM，设置sceneId
        if (data.bomModel === 'GBOM') {
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
      this.$refs.refTree.refreshData()
    },

    handleAdd() {
      this.addVisible = true
      /*      this.nextTick(() => {
              this.$refs.add.init()
            })*/
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
      // 清空当前选中的节点
      // 发送事件通知其他组件更新
      // this.$emit('loadTreeData')
      // this.$emit('clearInfo')
    },

    mouseover(data) {
      data.myshow = true
    },
    mouseout(data) {               // 鼠标移出
      data.myshow = false
    },
    addNode(data) {
      this.sceneId = data.id
      this.addModelVisible = true
    },

    editNode(data) {

    },
    async copyNode(data) {
      let loading = null;
      try {
        const {value} = await this.$prompt('请输入复制机型的机型名称', '复制机型', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入机型名称',
          inputValidator: (val) => {
            return val ? true : '名称不能为空';
          }
        });

        // 用户确认后，显示 loading
        loading = ElLoading.service({
          lock: true,
          text: '正在复制...',
          target: document.querySelector('.mycontainer'),
        });

        let response = await addProductModel(data.id, value);
        if (response.data.code === 200 || response.data.code === "200") {
          const newId = response.data.data;
          await copyModelTreeAndVar(value, newId, data.id);  // 等待异步完成
          this.$message.success('机型复制成功');
          this.refreshTree();
        } else {
          this.$message.error(response.data.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(`网络异常或服务器错误: ${error.message}`);
        }
      } finally {
        // 无论成功或失败，都会关闭 loading
        if (loading) loading.close();
      }
    },
    async deleteNode(data) {
      const isSceneNode = data.bomModel === 'GBOM';

      // 如果是场景节点，且存在子节点，禁止删除
      if (isSceneNode && data.children?.length > 0) {
        ElMessageBox.alert('该元模型下存在机型，不能删除。', '提示', {
          confirmButtonText: '确定',
          type: 'warning'
        });
        return;
      }

      const confirmMessage = isSceneNode
          ? `确认删除该元模型吗？<br><span style="color:red;">点击确认将删除该元模型结构树和所挂载的感知变量，请谨慎操作！</span>`
          : `确认删除该机型吗？<br><span style="color:red;">点击确认将删除该机型的结构树和已上传的数据集，请谨慎操作！</span>`;

      try {
        await ElMessageBox.confirm(confirmMessage, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        });

        // 用户确认后，显示 loading
        const loading = ElLoading.service({
          lock: true,
          text: '正在删除...',
          target: document.querySelector('.mycontainer'),
          // background: 'rgba(0, 0, 0, 0.7)'
        });

        const res = await (isSceneNode ? deleteSceneNode(data.id) : deleteModelNode(data.id));

        loading.close();  // 关闭 loading

        if (res.data.code === "200") {
          ElMessage.success('删除成功');
          this.nodeId = ''
          this.refreshTree();
        } else {
          ElMessage.error(`错误信息: ${res.data.message || '删除失败'}`);
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(`网络异常或服务器错误: ${error.message}`);
        }
        // 用户点击“取消”时什么都不做
      }
    },

    //  是否显示图标文字提示
    iconHelp() {
      this.showTooltip = !this.showTooltip
      this.iconTooltipColor = this.showTooltip ? 'black' : 'yellow'
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
  width: 30%;
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
