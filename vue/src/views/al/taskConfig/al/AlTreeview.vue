<template>
  <div class="searchArea">
    <span style="font-size: 15px;margin-right: 10px">搜索组态</span>
    <el-input
        v-model="filterText"
        style="width: 240px"
        placeholder="请输入算法组态名称"
    />

    <el-button style="margin-left: 15px" class="reSetBtn" @click="refreshTree()">刷新</el-button>
    <el-button style="margin-left: 8px" class="addBtn" @click="createAlConfig()">新建组态</el-button>
  </div>

  <div class="tree-zone">
    <el-scrollbar class="scrollbar-style">
      <el-tree
          :data="treeData"
          class="lineyes treestyle"
          ref="tree"
          :props="defaultProps"
          :expand-on-click-node="true"
          node-key="label"
          icon="none"
          :lazy="false"
          :show-line="true"
          :check-strictly="true"
          :default-expand-all="true"
          :highlight-current="true"
          @node-click="handleNodeClick"
          :filter-node-method="filterNode"
      >
        <template #default="{ node, data }">
          <div class="custom-tree" @mouseover="mouseover(data)" @mouseleave="mouseout(data)">
            <span style="line-height: 16px;"> {{ data.label }} </span>
          </div>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>


</template>

<script>

export default {
  props: {
    treeData: {
      type: Array,
      required: true
    }
  },

  emits: ['clickNode', 'clearInfo', 'loadTreeData', 'createAlConfig'],

  watch: {
    filterText(val) {
      this.$refs.tree.filter(val)
    }
  },

  data() {
    return {
      filterText: '',
      defaultProps: {
        children: 'children',
        label: 'label',
      },
    }
  },

  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },

    handleNodeClick(data) {
      this.$emit('clickNode', data)
    },

    mouseover (data) {               // 鼠标移
      data.myshow = true
     // vue3使用对象代理，不能用 this.$set(data, 'myshow', true)给对象赋值。因此，这里直接赋值
    },

    mouseout (data) {               // 鼠标移出
      data.myshow = false
    },

    refreshTree(){
      this.filterText = ''
      this.$emit('loadTreeData')
      this.$emit('clearInfo')
    },

    createAlConfig(){
      this.$emit('createAlConfig')
    },

  },
}
</script>

<style lang="scss" scoped>
.searchArea{
  margin-top: 20px;
  margin-bottom: 20px;
  align-items: center;
  justify-content: space-between;
  display: flex;
}

.tree-zone {

  height: 100vh;
  width: 100%;
  position: relative;

  .scrollbar-style{
    height: 100%;
    width: 100%;

    .el-tree-node {
      position: relative;
    }

    .el-tree {
      height: 100%;
      width: 100%;
      background-color: transparent; /* 背景透明 */
      color: black; /*字体颜色：黑色*/
    }

    .el-tree-node__expand-icon.is-leaf { /* 叶子节点隐藏图标  */
      display: none;
    }
    .el-tree-node.is-leaf.el-tree-node__content {
      padding-left: 0;
    }

  }
  :deep(.lineyes) {


    .el-tree-node__expand-icon {
      font-size: 16px; /*图标大小*/
    }

    .el-tree-node__expand-icon:before { /*有子节点 且未展开*/
      content: "";
      background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
      display: block;
      width: 16px;
      height: 16px;
      background-size: cover;
    }

    .el-tree-node__expand-icon.expanded:before { /*有子节点 且已展开*/
      content: "";
      background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
      display: block;
      width: 16px;
      height: 16px;
      background-size: cover;
    }

    .el-tree-node__content:hover { /*鼠标滑过，修改背景色*/
      color: cyan;
      font-weight: bold;
      background-color: rgb(108, 108, 111) !important;
    }

    .el-tree-node:focus > .el-tree-node__content { /*节点选中，节点获取焦点*/
      color: cyan;
      font-weight: bold;
      background-color: rgba(138, 194, 252, 0.53) !important;
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

.custom-tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
}

.registerBtn{
  background-color: rgba(49, 127, 195);
  &:hover{
    background-color: rgba(86, 150, 206);
    color: #fff;
  }
  &:focus{
    background-color: rgba(86, 150, 206);
    color: #fff;
  }
  &::before {
    content: '';
    display: inline-block;
    width: 16px;
    /* 图标的宽度 */
    height: 16px;
    /* 图标的高度 */
    background-image: url('@/styles/button/svg/registerBtn.svg');
    /* 相对路径到图标 */
    background-size: cover;
    margin-right: 5px;
    /* 图标和文本之间的间距 */
  }
}
</style>
