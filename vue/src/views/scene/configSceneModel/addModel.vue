<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      width="60%"
      :show-close="true"
      class="el-dialog__header high-dialog"
      @close="closeDialog()"
  >
    <div slot="title" style="display: flex;padding-bottom: 15px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>
      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span v-if="addOrUpdate === 'add'" style="">添加机型</span>
        <span v-else-if="addOrUpdate === 'update'" style="">修改机型结构</span>
      </div>
    </div>

    <div class="app-container">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="填写机型名称"></el-step>
        <el-step title="选择结构树"></el-step>
        <el-step title="感知变量配置"></el-step>
        <!--        <el-step v-if="addOrUpdate === 'add'" title="上传数据集"></el-step>-->
      </el-steps>
    </div>

    <div v-if="active===0">
      <div style="height: 50px; margin-top: 5%; text-align: center"><span>机型名称：</span>
        <el-input v-if="addOrUpdate === 'add'" v-model="newModelName" placeholder="请填写机型名称"
                  style="width:40%"></el-input>
        <el-input v-else-if="addOrUpdate === 'update'" v-model="newModelName" placeholder="请修改机型名称"
                  style="width:40%">{{ this.newModelName }}
        </el-input>
      </div>
    </div>

    <div v-if="active===1">
      <select-tree ref="selectTree" :scene-id="sceneId" :addOrUpdate="addOrUpdate" :model-id="modelId"></select-tree>
    </div>

    <div v-if="active===2" style="display: flex; height: 700px" v-loading="loadingInstance"
         element-loading-text="正在导入...">
      <div style="width: 30%;  overflow-y: auto;">
        <sw-tree class="tyTree" ref="lazyTree" :data="modelTreeData" :accordion="true"
                 :defaultProps="defaultProps" :expandNode="true"
                 :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                 :showCheckBox="false" :checkStrictly="true" :isLazy="false"
                 @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand">
        </sw-tree>
      </div>
      <table-select ref="SelectTable" :select-nodes="selectNodes" @update:selected-variables="onVariableSelect"/>
    </div>

    <!--    <div v-if="active===3">
          <div v-loading="loadingInstance" element-loading-text="正在导入...">
            <el-alert
                title="可点击节点＋上传数据集"
                type="info"
                center
                show-icon
                :closable="false"
                style="padding: 4px 8px; line-height: 1.2; margin: 0; color: #409EFF; background-color: #f1f6f6;"
            />
            <upload-data-set-tree ref="uploadDataSetTree" :scene-id="sceneId"
                                  :defaultExpandKeys="defaultExpandKeys"
                                  :model-tree-data="modelTreeData"></upload-data-set-tree>
          </div>
        </div>-->

    <div v-if="active===0" slot="footer" style="text-align: center">
      <el-button style="margin-top: 12px;" class="normalBtn" @click="next">下一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="closeDialog()" v-if="active < 1">取消
      </el-button>
    </div>
    <div v-if="active===1" slot="footer" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="next">下一步</el-button>
    </div>
    <!--    <div v-if="active===2 && addOrUpdate === 'add'" slot="footer" style="text-align: center">

          <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
          <el-button style="margin-top: 12px;" class="normalBtn" @click="next">下一步</el-button>
        </div>-->
    <div v-if="active===2 && addOrUpdate === 'add'" slot="footer" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button class="normalBtn" @click="dataSubmit()" style="margin-top:12px">完成配置
      </el-button>
    </div>


    <div v-if="active===2 && addOrUpdate === 'update'" slot="footer" style="text-align: center">
      <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
      <el-button style="margin-top: 12px;" class="normalBtn" @click="updateDataSubmit()">完成配置</el-button>
    </div>
    <!--
        <div v-if="active===3" slot="footer" style="text-align: center">
          <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
          <el-button class="normalBtn" @click="dataSubmit()" style="margin-top:12px">完成配置
          </el-button>
        </div>
    -->


  </el-dialog>
</template>


<script>
import SelectTree from "@/views/scene/configSceneModel/selectTree.vue";
import swTree from '@/components/myComponent/swTree.vue'
import TableSelect from './table-select.vue'
import {getGBomTreeBySceneIdAndProId, reqSonNodesBySceneId} from "@/api/sw/model3d/configGbomTree";
import uploadDataSetTree from "./uploadDataSetTree.vue"
import {addProductModel, updateProductModel} from "@/api/diagnosis/scene/scene";
import {
  addProductModelDataSets,
  addProductModelTree,
  addProductModelVariables, updateProductModelTree
} from "@/api/diagnosis/graphVis/dataIngestion";
import {ElMessage, ElMessageBox} from "element-plus";
import {getProjectById} from "@/api/admin/user";

let globeParams = {}     //  声明一个全局参数对象
let _tree

export default {
  name: 'addModel',
  components: {swTree, TableSelect, SelectTree, uploadDataSetTree},
  data() {
    return {
      active: 0,//步骤条初始化
      visible: false,
      newModelName: '',
      modelTreeData: [],
      selectNodes: [],
      selectedNodeId: '', // 当前选中的结构树节点id（步骤三用）
      modelTreeVariable: [], // 所有选中的变量，步骤三结束时汇总


      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      defaultExpandKeys: [],
      loadingInstance: false,
      updateData: [], // 更新时前已选择的节点
    }
  },
  props: {
    sceneId: {
      type: [Number, String],
      required: true
    },
    addOrUpdate: {
      type: String,
      default: 'add'
    },
    modelId: {
      type: [String, Number],
      default: ''
    }
  },
  watch: {
    active(newVal) {
      if (newVal === 2) {
        this.$nextTick(() => {
          _tree = this.$refs.lazyTree?.$refs?.tree
          // 在进入步骤2时，获取sw-tree中当前选中的节点，并传递给table-select
          const currentNode = this.$refs.lazyTree?.getCurrentNodeData();
          if (currentNode && this.$refs.SelectTable) {
            this.$refs.SelectTable.getVariable(currentNode);
          } else {
            console.warn("addModel.vue: Could not find current node or SelectTable ref when entering step 2.");
          }
        })
      }
    },
    modelId(newVal) {
      this.getProductModelName(newVal)
    }
  },
  async mounted() {
    globeParams.nodeLevel = 3       //    预先展开3层节点
  },
  methods: {
    closeDialog() {
      this.visible = false;
      this.$emit('update:modelValue', false)
      this.resetDialogData();
    },
    // 新增：重置弹窗所有数据
    resetDialogData() {
      this.active = 0;
      if (this.addOrUpdate === 'add') {
        this.newModelName = '';
      }
      this.modelTreeData = [];
    },
    async dataSubmit() {
      /*const uploadDataSets = this.$refs.uploadDataSetTree.getUploadDataSets();*/

      // 校验基础数据
      if (!this.newModelName) {
        ElMessage.warning('请输入机型名称');
        return;
      }

      if (!this.selectNodes || this.selectNodes.length === 0) {
        ElMessage.warning('请选择至少一个结构节点');
        return;
      }
      this.loadingInstance = true;
      try {
        // 1. 保存机型名称
        const modelRes = await addProductModel(this.sceneId, this.newModelName);
        const code = modelRes.data.code;
        const proId = modelRes.data.data;

        if (code !== 0 && code !== 200 && code !== '200') {
          throw new Error(modelRes.data.message || '保存机型失败');
        }

        // 2. 保存机型结构
        const treeRes = await addProductModelTree(proId, this.selectNodes);
        if (treeRes.data.code !== 0) {
          throw new Error(treeRes.data.message || '保存机型结构失败');
        }

        // 3. 保存机型感知变量（失败提示但不终止）
        const varRes = await addProductModelVariables(proId, this.modelTreeVariable);
        if (varRes.data.code !== 0) {
          ElMessage.error(varRes.data.message || '保存感知变量失败');
        }

        // 4. 保存数据集（失败提示但不终止）
        /*const dataSetRes = await addProductModelDataSets(proId, uploadDataSets);
        if (dataSetRes.data.code !== 0) {
          ElMessage.error(dataSetRes.data.message || '保存数据集失败');
        }*/
        ElMessage.success('新增机型成功！');
        this.$emit('onRefreshTree')
      } catch (err) {
        ElMessage.error(`保存失败：${err.message}`);
        console.error(err);
      } finally {
        // 最后关闭弹窗
        this.closeDialog();
        this.loadingInstance = false;
      }
    },

    //上一页
    prev() {
      if (this.active > 0) this.active--;
    },
    //下一页
    next() {
      if (this.active === 0) {
        if (!this.newModelName.trim()) {
          this.$message.warning('请输入型号名称')
          return
        }
        if (this.addOrUpdate === 'update') {
          this.active++
          ElMessageBox.alert(
              '<span style="color: red;">取消勾选节点将会同时删除该节点下已有的数据集，请谨慎操作！</span>',
              '重要提醒',
              {
                confirmButtonText: '已知晓',
                type: 'warning',
                dangerouslyUseHTMLString: true, // 👈 启用 HTML 渲染
                showClose: false,
                closeOnClickModal: false,
                closeOnPressEscape: false,
              }
          ).catch((error) => {
            console.warn(error)
          })
          return // 你仍然需要 return 来阻止后续逻辑
        }
      }
      if (this.active === 1) {
        const nodes = this.$refs.selectTree.getCurrentNode()
        this.selectNodes = nodes.filter(ele => ele.id !== "loading")
        if (!this.selectNodes || this.selectNodes.length === 0) {
          ElMessage.warning('请选择至少一个结构节点');
          return;
        }
        if (nodes && (nodes.length > 0)) {
          this.modelTreeData = []
          this.expandKeys = []            //  缓存待扩展的节点

          let rootNodes = nodes.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          for (let item of rootNodes) {
            this.modelTreeData.push(item)
            this.expandKeys.push(item.id)
            this.setChildren(item, nodes)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点
        } else {
          console.log('没有选择节点')
        }
      }
      /*if (this.active === 2 && this.addOrUpdate === 'add') {
        ElMessage({
          message: '可点击节点＋上传数据集',
          type: 'success',       // success | warning | info | error
          duration: 5000         // 停留时间，单位是毫秒（默认3000ms）
        })

      }*/
      this.active++
    },

    //处理更新的内容
    async getProductModelName(nodeId) {
      // 1. 在发起请求前，严格检查 nodeId 是否有效
      if (!nodeId) {
        this.newModelName = ''; // 如果 nodeId 为空，直接清空机型名称并返回
        return;
      }

      try {
        const res = await getProjectById(nodeId);
        if (res && res.data && res.data.data) {
          this.newModelName = res.data.data.productModel || '';
        } else {
          this.newModelName = ''; // 如果数据结构不符合预期，也清空机型名称
        }
      } catch (error) {
        this.newModelName = ''; // 发生错误时清空机型名称
      }
    },
    async updateDataSubmit() {
      // 校验基础数据
      if (!this.newModelName) {
        ElMessage.warning('请输入机型名称');
        return;
      }
      if (!this.selectNodes || this.selectNodes.length === 0) {
        ElMessage.warning('请选择至少一个结构节点');
        return;
      }
      this.loadingInstance = true;
      try {
        // 1. 修改机型名称
        const modelRes = await updateProductModel(this.modelId, this.newModelName);
        const code = modelRes.data.code;

        if (code !== 0 && code !== 200 && code !== '200') {
          throw new Error(modelRes.data.message || '保存机型失败');
        }

        // 2. 修改机型结构
        const treeRes = await updateProductModelTree(this.modelId, this.selectNodes);
        if (treeRes.data.code !== 0) {
          throw new Error(treeRes.data.message || '保存机型结构失败');
        }

        // 3. 修改机型感知变量（失败提示但不终止）
        const varRes = await addProductModelVariables(this.modelId, this.modelTreeVariable);
        if (varRes.data.code !== 0) {
          ElMessage.error(varRes.data.message || '保存感知变量失败');
        }

        ElMessage.success('修改机型成功！');
        this.$emit('onRefreshTree')
        // 最后关闭弹窗
        this.closeDialog();

      } catch (err) {
        ElMessage.error(`保存失败：${err.message}`);
        console.error(err);
      } finally {
        // 最后关闭弹窗
        this.closeDialog();
        this.loadingInstance = false;
      }
    },

    /**
     * 下面是Tree方法
     */
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
    //  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
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
    //  节点扩展消息响应
    async handleNodeExpand(data, node, treeNode) {
      if (node.level >= globeParams.nodeLevel) {
        let response = await reqSonNodesBySceneId(this.sceneId, data.nodeCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },
    /**
     * 配置感知变量
     */
    //  节点点击消息响应
    handleNodeClick(data) {
      globeParams.curNode = data
      this.$refs.SelectTable.getVariable(data)
    },
    getCurNode() {
      return globeParams.curNode
    },
    onVariableSelect(variables) {
      this.modelTreeVariable = variables
    },
  }
}

</script>

<style scoped lang="scss">
.high-dialog {
  min-height: 800px;
}
</style>