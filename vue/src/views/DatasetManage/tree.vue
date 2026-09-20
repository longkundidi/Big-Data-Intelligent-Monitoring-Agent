<template>
  <div class="mycon">
    <template-tree
        class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
        :defaultProps="defaultProps" :expandNode="true" :proId="nodeId" :node-type="nodeType"
        :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
        :showCheckBox=false :checkStrictly=true :isLazy=false @getTreeNodes="getTreeNodes"
         @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand"
    >
    </template-tree>
  </div>
</template>
<script>
import templateTree from './templateTree.vue'

import {
  reqSonNodesBySceneId,
  reqTreeNodesBySceneId,
  reqSonNodesByModelId,
  reqTreeNodesByModelId
} from '@/api/sw/model3d/configGbomTree/index.js'
import {onMounted, ref} from 'vue'
import {nodeOption} from '@/const/crud/sw/model3d/configGbomTree'



import * as XLSX from "xlsx";
import * as echarts from 'echarts';
import JSZip from 'jszip';
import axios from 'axios';
import {ElNotification, ElMessage} from "element-plus";

let globeParams = {}     //  声明一个全局参数对象
let __tree

export default {
  components: {
    templateTree
  },
  name: 'variableGbomTree',

  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      titleName: '',
      nodeOption: nodeOption,
      nodeForm: {},
      updateFlag: '',
      dlgNodeAddEdit: false,          //  控制新增、编辑按钮点击后，弹窗的显示和隐藏
      dlgNodeUploadDataset: false,          // 控制上传数据集弹窗
      uploadDatasetVisible: false,
      defaultExpandKeys: [],
      currentUploadNodeData: {},
      datasetType: '',


    }
  },
  props: {
    nodeId: {
      type: [String, Number],
      required: true
    },
    nodeType: {
      type: String,
      default: 'GBOM'
    },
    sceneId: {
      type: [Number, String],
      required: true
    },
  },
  watch: {
    nodeId(newVal) {
      console.log('父组件传来的 nodeId 发生变化:', newVal)
      // 重置templateTree的状态
      if (this.$refs.lazyTree) {
        this.$refs.lazyTree.updateBom = false
        this.$refs.lazyTree.upload = false
      }
      this.getTreeNodes(globeParams.nodeLevel)
    },
    nodeType(newVal) {
      console.log('父组件传来的 nodeType 发生变化:', newVal)
      // 重置templateTree的状态
      if (this.$refs.lazyTree) {
        this.$refs.lazyTree.updateBom = false
        this.$refs.lazyTree.upload = false
      }
      this.getTreeNodes(globeParams.nodeLevel)
    },
  },


  setup() {
    let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
    onMounted(() => {
      __tree = lazyTree
    })
    return {lazyTree}
  },
  async mounted() {
    this.$options._isMounted = true  // 标记组件已挂载
    globeParams.nodeLevel = 3       //    预先展开3层节点
    this.getTreeNodes(globeParams.nodeLevel)
  },

  methods: {
    /**
     * 树节点处理
     */
    // 根据节点层级数，加载结构树的一组节点
    getTreeNodes(nodeLevel) {
      // 根据节点类型调用不同的接口
      const apiCall = this.nodeType === 'GBOM'
          ? reqTreeNodesBySceneId(this.nodeId, nodeLevel)
          : reqTreeNodesByModelId(this.nodeId, nodeLevel);
      apiCall.then(response => {
        if ((response.data.data) && (response.data.data.length > 0)) {
          this.treeData = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          for (let item of rootNodes) {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            item.showView = this.nodeType === 'MBOM' &&
                (
                    (item.scadaTestLatestDataset && item.scadaTrainLatestDataset) ||
                    (item.cmsTrainLatestDataset && item.cmsTestLatestDataset)
                );
            this.treeData.push(item)
            this.expandKeys.push(item.id)
            this.setChildren(item, response.data.data)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点

          // 数据加载完成后，如果在mounted中调用，则发送点击事件
          if (this.$options._isMounted) {
            if (this.treeData && this.treeData.length > 0) {
              this.$emit('clickNode', this.treeData[0])
            }
          }
        } else {
          console.log(`没有找到${this.nodeType}树，请先创建${this.nodeType}树`)
        }
      }).catch(error => {
        this.treeData = []
        this.expandKeys = []
        console.log(error)
      })
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
          item.showAdd = true
          item.showRemove = true
          item.showEdit = true
          item.showHelp = true
          item.showView = this.nodeType === 'MBOM' &&
              (
                  (item.scadaTestLatestDataset && item.scadaTrainLatestDataset) ||
                  (item.cmsTrainLatestDataset && item.cmsTestLatestDataset)
              );
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
        // 根据节点类型调用不同的接口
        let response;
        if (this.nodeType === 'GBOM') {
          response = await reqSonNodesBySceneId(this.nodeId, data.nodeCode);
        } else if(this.nodeType === 'MBOM') {
          response = await reqSonNodesByModelId(this.nodeId, data.nodeCode);
        }

        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.showAdd = true
            item.showRemove = true
            item.showEdit = true
            item.showHelp = true
            item.showView = this.nodeType === '' &&
                (
                    (item.scadaTestLatestDataset && item.scadaTrainLatestDataset) ||
                    (item.cmsTrainLatestDataset && item.cmsTestLatestDataset)
                );
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    //  节点点击消息响应
    handleNodeClick(data, node, treeNode, event) {
      this.$emit('clickNode', data)
    },
    refreshTree() {
      this.getTreeNodes(globeParams.nodeLevel)
    }
  }


}
</script>
<style lang="scss" scoped>
.mycon {
  width: 100%;
  height: calc(100% - 15px);
  padding-top: 10px;
}
</style>
