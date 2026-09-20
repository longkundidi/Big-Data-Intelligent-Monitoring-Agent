<template>
  <div class="tree-zone">
    <el-scrollbar>
      <el-tree
          v-if="visible"
          :data="treeData"
          class="lineyes treestyle"
          ref="tree"
          :indent="0"
          :accordion="true"
          :props="defaultProps"
          :expand-on-click-node="true"
          node-key="id"
          :default-expanded-keys="defaultExpandKeys"
          icon="none"
          :lazy="false"
          currentKey=""
          :show-checkbox="false"
          :check-strictly="true"
          :default-expand-all="false"
          :highlight-current="true"
          @node-click="handleNodeClick"
          @node-expand="handleNodeExpand"
      >
      </el-tree>
    </el-scrollbar>
  </div>
</template>

<script>

import {reqSonNodes} from "@/api/sw/model3d/configModel/table";

export default {
  name: "tree",
  data() {
    return {
      visible: false,
      treeData: [],
      expandKeys: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      defaultExpandKeys: [],
      windBomLists: [],
      nodeCode: '',
      nodeName: '',
      currentNode: '',
      showTooltip: true,      //  是否开启图标文字提示
      iconTooltipColor: 'black'   //  图标提示按钮颜色
    }

  },
  methods: {
    init(treeData, expandKeys, defaultExpandKeys) {
      this.visible = true
      this.treeData = treeData
      this.expandKeys = expandKeys
      this.defaultExpandKeys = defaultExpandKeys
    },

    handleNodeClick(data) {
      this.$emit('clickNode', data)
    },

    //  节点扩展消息响应
    async handleNodeExpand(data, node) {

      if ((node.level >= 2) || (this._sonNodeHasLoading(data))) {
        let response = await reqSonNodes({ nodeCode: data.nodeCode, turbineCode: data.turbineCode})
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }
    },

    /* 检查一个节点的子节点中，是否有待加载（Loading）状态的子节点
    * 这种情况只会发生在第二层节点复制后，它的子节点没有加载的情况下。这时，虽然节点层级小于globeParams.nodeLevel，但是仍然需要查询后台
    */
    _sonNodeHasLoading(node) {
      let hasLoading = false
      for (let i = 0; i < node.children.length; i++) {
        if (node.children[i].id === 'loading') {
          hasLoading = true
          break
        }
      }
      return hasLoading
    },

  }
}
</script>

<style lang="scss" scoped>
.tree-zone {
  ::v-deep{
    .treestyle {
      .el-tree-node {
        position: relative;
        padding-left: 16px;         //  需要配合:indent="0"，才能保证竖线对齐
      }
      .el-tree {
        background-color: Transparent;          /*背景透明*/
        color: black;                /*字体颜色：黑色*/
      }
      .el-tree-node__expand-icon.is-leaf {        /* 叶子节点隐藏图标  */
        display: none;
      }

      /*  下面的样式设置与连线有关    */
      .el-tree-node__children {
        padding-left: 18px;
      }
      .el-tree-node :last-child:before {
        height: 38px;
      }
      .el-tree > .el-tree-node:before {
        border-left: none;
      }
      .el-tree > .el-tree-node:after {
        border-top: none;
      }
      .el-tree-node:before {
        content: "";
        left: -4px;
        position: absolute;
        right: auto;
        border-width: 1px;
      }
      .el-tree-node:after {
        content: "";
        left: -4px;
        position: absolute;
        right: auto;
        border-width: 1px;
      }
    }
    .lineyes {
      .el-tree .el-tree-node__expand-icon.expanded {          /*节点图标不旋转*/
        -webkit-transform: rotate(0deg);
        transform: rotate(0deg);
      }
      .el-tree-node__expand-icon {
        font-size: 16px;                /*图标大小*/
      }
      .el-tree-node__expand-icon:before {         /*有子节点 且未展开*/
        content: "";
        background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
        display: block;
        width: 16px;
        height: 16px;
        background-size: cover;
      }
      .el-tree-node__expand-icon.expanded:before{     /*有子节点 且已展开*/
        content: "";
        background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
        display: block;
        width: 16px;
        height: 16px;
        background-size: cover;
      }
      .el-tree-node__content:hover {      /*鼠标滑过，修改背景色*/
        color: cyan;
        font-weight: bold;
        background-color: rgb(108, 108, 111) !important;
      }
      .el-tree-node:focus > .el-tree-node__content {          /*节点选中，节点获取焦点*/
        color: cyan;
        font-weight: bold;
        background-color: rgba(138, 194, 252, 0.53) !important;
      }
      /*                .el-tree-node.is-current > .el-tree-node__content {
                          color: black;
                      }*/
      .el-tree-node:before {          /*显示节点间连接的竖线*/
        border-left: 1px dashed black;
        bottom: 0px;
        height: 100%;
        top: -26px;
        width: 3px;
      }
      .el-tree-node:after {           /*显示节点间连接的横线*/
        border-top: 1px dashed black;
        height: 20px;
        top: 12px;
        width: 24px;
      }
    }

  }
  .custom-tree{
    display: flex;
    height: 100%;
    width: 100%;
    align-items: center;            /*垂直对齐*/
    .tree-btn{
      display: flex;
      height: 100%;
      width: 100%;
      align-items: center;            /*垂直对齐*/
      .btn{
        height: 100%;
        width: 30px;
      }
    }


  }
}
</style>
