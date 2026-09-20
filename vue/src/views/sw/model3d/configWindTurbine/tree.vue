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

import {reqAllInstanceSonNodes} from "@/api/sw/model3d/configModel/table";

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
        let response = await reqAllInstanceSonNodes({ nodeCode: data.nodeCode, turbineCode: data.turbineCode})
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data
        }
      }

      // if (node.level === 1) {
      //   this.windBomLists.forEach(windItem => {
      //     if (windItem.turbineName === node.data.name) {  //取出点击的求交集后的风机号的二级节点
      //       this.nodeName = node.data.name
      //       node.data.children = Object(windItem.bomList.filter(e => {
      //         e.children = e.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
      //         return e.nodeLevel === 2
      //       }))
      //     }
      //   })
      // } else {
      //   this._sonNodeHasLoading(data)
      //   let arr = []
      //   this.windBomLists.forEach(windItem => {
      //
      //     if (windItem.turbineName === this.nodeName) {
      //       let reg;
      //       windItem.bomList.forEach(item => {   //遍历所有交集list的元素
      //         if (item.nodeCode === node.data.nodeCode) {  //找到该交集list中与选中节点的nodecode相同的节点
      //           this.nodeCode = item.nodeCode
      //           //拿到选中节点的nodecode,然后从bomlist筛选出与nodecode匹配的nodelevel==3的子节点
      //           let B = item.nodeCode
      //           reg = new RegExp(B + "-\\d+$")
      //         }
      //       })
      //       windItem.bomList.forEach(item => {
      //         //匹配与nodecode相同的3级子节点
      //         let res = reg.test(item.nodeCode)
      //         if (res === true) {
      //           arr.push(item)
      //         }
      //       })
      //
      //       if (arr) {
      //         arr.forEach((item) => {
      //           item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
      //         })
      //         node.data.children = arr
      //       }
      //     }
      //   })
      // }
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
  height: 100%;
  padding: 10px 8px;
  background: transparent;

  :deep(.el-scrollbar) {
    height: 100%;
  }

  :deep(.el-scrollbar__view) {
    min-height: 100%;
  }

  :deep(.treestyle) {
    min-height: 100%;
    color: rgba(255, 255, 255, 0.9);
    background: transparent;
  }

  :deep(.treestyle .el-tree-node) {
    position: relative;
    padding-left: 16px;         //  需要配合:indent="0"，才能保证竖线对齐
  }

  :deep(.treestyle .el-tree-node__content) {
    min-height: 31px;
    margin: 2px 0;
    border-radius: 6px;
    color: rgba(255, 255, 255, 0.84);
    transition: color 0.18s ease, background-color 0.18s ease;
  }

  :deep(.treestyle .el-tree-node__content:hover) {
    color: #ffffff;
    background: rgba(128, 216, 237, 0.1) !important;
  }

  :deep(.treestyle .el-tree-node.is-current > .el-tree-node__content),
  :deep(.treestyle .el-tree-node:focus > .el-tree-node__content) {
    color: #ffffff;
    font-weight: 600;
    background: linear-gradient(90deg, rgba(42, 138, 177, 0.54), rgba(128, 216, 237, 0.1)) !important;
    box-shadow: inset 3px 0 0 #80d8ed;
  }

  :deep(.treestyle .el-tree-node__label) {
    color: inherit;
  }

  :deep(.treestyle .el-tree-node__expand-icon.is-leaf) {
    display: none;
  }

  :deep(.treestyle .el-tree-node__children) {
    padding-left: 18px;
  }

  :deep(.treestyle .el-tree-node :last-child:before) {
    height: 38px;
  }

  :deep(.treestyle > .el-tree-node:before) {
    border-left: none;
  }

  :deep(.treestyle > .el-tree-node:after) {
    border-top: none;
  }

  :deep(.treestyle .el-tree-node:before) {
    content: "";
    left: -4px;
    position: absolute;
    right: auto;
    border-width: 1px;
    border-left: 1px dashed rgba(122, 214, 240, 0.36);
    bottom: 0;
    height: 100%;
    top: -26px;
    width: 3px;
  }

  :deep(.treestyle .el-tree-node:after) {
    content: "";
    left: -4px;
    position: absolute;
    right: auto;
    border-width: 1px;
    border-top: 1px dashed rgba(122, 214, 240, 0.36);
    height: 20px;
    top: 13px;
    width: 24px;
  }

  :deep(.lineyes .el-tree-node__expand-icon.expanded) {
    -webkit-transform: rotate(0deg);
    transform: rotate(0deg);
  }

  :deep(.lineyes .el-tree-node__expand-icon) {
    font-size: 16px;
  }

  :deep(.lineyes .el-tree-node__expand-icon:before) {
    content: "";
    background: url("/img/kgtree/circleplus.svg") no-repeat 0 0;
    display: block;
    width: 16px;
    height: 16px;
    background-size: cover;
    filter: invert(93%) sepia(12%) saturate(980%) hue-rotate(154deg) brightness(102%);
    opacity: 0.9;
  }

  :deep(.lineyes .el-tree-node__expand-icon.expanded:before) {
    content: "";
    background: url("/img/kgtree/remove.svg") no-repeat 0 0;
    display: block;
    width: 16px;
    height: 16px;
    background-size: cover;
    filter: invert(93%) sepia(12%) saturate(980%) hue-rotate(154deg) brightness(102%);
    opacity: 0.9;
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
