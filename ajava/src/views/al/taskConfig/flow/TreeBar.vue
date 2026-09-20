<template>
  <div class="searchArea">
    <span style="font-size: 15px;margin-right: 10px">搜索组态</span>
    <el-input
        v-model="filterText"
        style="width: 240px"
        placeholder="请输入算法组态名称"
    />

    <el-button style="margin-left: 15px" class="reSetBtn" @click="refreshTree()">刷新</el-button>
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
            <template v-if="data.showPopover">
              <el-popover
                  trigger="hover"
                  placement="right"
                  effect="light"
                  :width="'auto'"
              >
                <template #default>
                  <div class="process-flow">
                    <template v-if="data.rawData?.taskProcess?.length">
                      <template v-for="(process, index) in data.rawData.taskProcess" :key="index">
                        <span class="process-item">
                          {{ index + 1 }}. {{ process.almodelName }}
                        </span>
                        <span class="arrow" v-if="index < data.rawData.taskProcess.length - 1"></span>
                      </template>
                    </template>
                    <template v-else>
                      <span>暂无流程信息</span>
                    </template>
                  </div>
                </template>

                <template #reference>
                  <span style="line-height: 16px;">{{ data.label }}</span>
                </template>
              </el-popover>
            </template>

            <template v-else>
              <!-- 不显示弹出框，直接显示 label -->
              <span style="line-height: 16px;">{{ data.label }}</span>
            </template>

            <!-- 操作按钮区域 -->
            <div class="tree-btn" v-show="data.myshow">
              <span>&nbsp;&nbsp;&nbsp;&nbsp;</span>
              <el-tooltip content="显示" placement="top">
                <el-icon v-show="data.showAdd" class="btn" color="black" @click="addShow($event, node, data)">
                  <Plus />
                </el-icon>
              </el-tooltip>
              <el-tooltip content="关闭" placement="top">
                <el-icon v-show="data.showClose" class="btn" color="black" @click="deleteShow($event, node, data)">
                  <Close />
                </el-icon>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>


</template>

<script>

import {Close, Plus} from "@element-plus/icons-vue";

export default {
  components: {Close, Plus},
  props: {
    treeData: {
      type: Array,
      required: true
    },
  },

  emits: ['clickNode', 'addShow', 'deleteShow', 'loadTreeData'],

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

    addShow(event, node, data) {
      event.stopPropagation()
      this.$emit('addShow', data.id);
    },

    deleteShow(event, node, data) {
      event.stopPropagation()
      this.$emit('deleteShow', data.id);
    },

    refreshTree(){
      this.filterText = ''
      this.$emit('loadTreeData', false)
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

.process-flow {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
}

.process-item {
  margin: 0 4px;
  white-space: nowrap;
  color: #409eff;
  font-size: 15px;
}

.arrow {
  display: inline-block;
  width: 0;
  height: 0;
  border-left: 6px solid transparent; /* 透明左边 */
  border-right: 6px solid transparent; /* 透明右边 */
  border-top: 8px solid rgba(128, 128, 128, 0.3); /* 蓝色的顶边，用来形成箭头 */
  margin: 0 5px; /* 箭头和文字之间的间距 */
  transform: rotate(-90deg);
}
</style>
