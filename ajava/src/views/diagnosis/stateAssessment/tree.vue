<template>
  <el-scrollbar>
    <el-tree
        v-if="visible"
        :data="treeData"
        class="lineyes treestyle"
        ref="tree"
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
      globeParams: {},
      nodeCode: '',
      nodeName: ''
    }

  },
  mounted() {

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
        let response = await reqSonNodes({nodeCode: data.nodeCode, turbineCode: data.turbineCode})
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

<style scoped>
:deep(.treestyle .el-tree-node) {
  position: relative;
  padding-left: 16px;
/ / 需要配合: indent = "0" ，才能保证竖线对齐
}

:deep(.treestyle .el-tree) {
  background-color: Transparent; /*背景透明*/
  color: #212020; /*字体颜色：黑灰色*/
}

:deep(.treestyle .el-tree-node__expand-icon.is-leaf) { /* 叶子节点隐藏图标  */
  display: none;
}

/*  下面的样式设置与连线有关    */
:deep(.treestyle .el-tree-node__children) {
  padding-left: 18px;
}

:deep(.treestyle .el-tree-node :last-child:before) {
  height: 38px;
}

.treestyle .el-tree > .el-tree-node:before {
  border-left: none;
}

.treestyle .el-tree > .el-tree-node:after {
  border-top: none;
}

:deep(.treestyle .el-tree-node:before) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.treestyle .el-tree-node:after) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.lineyes .el-tree .el-tree-node__expand-icon.expanded) { /*节点图标不旋转*/
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

:deep(.lineyes .el-tree-node__expand-icon) {
  font-size: 16px; /*图标大小*/
}

:deep(.lineyes .el-tree-node__expand-icon:before) { /*有子节点 且未展开*/
  content: "";
  background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__expand-icon.expanded:before) { /*有子节点 且已展开*/
  content: "";
  background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__content:hover) { /*鼠标滑过，修改背景色*/
  color: cyan;
  font-weight: bold;
  background-color: rgb(108, 108, 111) !important;
}

.lineyes .el-tree-node:focus > .el-tree-node__content { /*节点选中，节点获取焦点*/
  color: gold;
  font-weight: bold;
  background-color: rgba(138, 194, 252, 0.53) !important;
}

.lineyes .el-tree-node.is-current > .el-tree-node__content {
  color: gold;
}

:deep(.lineyes .el-tree-node:before) { /*显示节点间连接的竖线*/
  border-left: 1px dashed #dcdcdc;
  bottom: 0px;
  height: 100%;
  top: -26px;
  width: 3px;
}

:deep(.lineyes .el-tree-node:after) { /*显示节点间连接的横线*/
  border-top: 1px dashed #dcdcdc;
  height: 20px;
  top: 12px;
  width: 24px;
}
</style>
