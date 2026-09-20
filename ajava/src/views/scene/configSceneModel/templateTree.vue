<template>
  <div class="update-zone">
    <el-button class="editBtn" v-if="nodeType === 'GBOM'" @click="updateByHand()">更新元结构树</el-button>
    <el-button class="editBtn" v-if="nodeType === 'MBOM'" @click="upProductModel()">更新机型结构树</el-button>
    <el-button class="editBtn" v-if="nodeType === 'MBOM'" @click="uploadDataset()">上传数据集</el-button>
    <el-button class="auditBtn" v-if="nodeType === 'MBOM'" @click="ManageDataset()">数据集管理</el-button>
    <!--    <el-button class="addBtn" @click="dlgBomCreate = true">新建实例风机</el-button>-->
  </div>

  <div class="tree-zone">
    <div v-show="display" :class="hasline ? 'treestyle lineyes' : 'treestyle'">
      <el-tree :data="data" :indent="0" ref="tree" :accordion="accordion" :props="defaultProps"
               :expand-on-click-node="expandNode" :default-checked-keys="defaultCheckedKeys" :node-key="nodeCurrentKey"
               :default-expanded-keys="defaultExpandKeys" icon="none" :lazy="isLazy" :highlight-current="highLight"
               :currentKey="currentKey" :show-checkbox="showCheckBox" :check-strictly="checkStrictly"
               :default-expand-all="defaultExpandAll" :load="loadTreeNode" @node-click="handleNodeClick"
               @node-expand="nodeExpand" @check-change="checkChange"
      >
        <template v-if="updateBom === true" #default="{ node, data }">
          <div class="custom-tree" @mouseover="mouseover(data)" @mouseleave="mouseout(data)">
            <span style="line-height: 16px;"> {{ data.name }} </span>
            <div class="tree-btn" v-show="data.myshow" @click.stop="()=>{}" >
              <span>&nbsp&nbsp&nbsp&nbsp</span>
              <el-tooltip content="节点添加" placement="top" :disabled="showTooltip">
                <el-icon v-show="data.showAdd" class="btn" @click="addNode(node,data)" color="black"><Plus/></el-icon>
              </el-tooltip>
              <el-tooltip content="节点编辑" placement="top" :disabled="showTooltip">
                <el-icon v-show="data.showEdit" class="btn" @click="editNode(node,data)" color="black"><Edit /></el-icon>
              </el-tooltip>
              <el-tooltip content="节点删除" placement="top" :disabled="showTooltip">
                <el-icon v-show="data.showRemove" class="btn" @click="deleteNode(data)" color="red"><Delete /></el-icon>
              </el-tooltip>
              <el-tooltip content="节点复制" placement="top" :disabled="showTooltip">
                <el-icon v-show="data.showCopy" class="btn" @click="copyNode(data)" color="black"><DocumentCopy /></el-icon>
              </el-tooltip>
              <el-tooltip content="节点移动" placement="top" :disabled="showTooltip">
                <el-icon v-show="data.showMove" class="btn" @click="moveNode(data)" color="black"><Switch /></el-icon>
              </el-tooltip>
              <el-tooltip content="图标帮助" placement="top" :disabled="false">
                <el-icon v-show="data.showHelp" class="btn" @click="iconHelp()" :color="iconTooltipColor"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </div>
        </template>

        <template v-if="upload === true" #default="{ node, data }">
          <div class="custom-tree" @mouseover="mouseover(data)" @mouseleave="mouseout(data)">
            <span style="line-height: 16px;"> {{ data.name }} </span>
            <div class="tree-btn" v-show="data.myshow2" @click.stop="()=>{}" >
              <span>&nbsp&nbsp&nbsp&nbsp</span>
                <el-tooltip content="查看数据集信息" placement="top" :disabled="showTooltip">
                  <el-icon v-show="data.showView" class="btn" @click="viewData(node,data)" color="black"><View/></el-icon>
                </el-tooltip>
<!--           <span v-if="hasLatestDataset(data)">
              <el-tooltip :content="isLatestDataset(data) ? '当前使用最新数据集' : '当前使用历史数据集'"
                          placement="top">
                <el-icon class="btn" @click="toggleDatasetVersion(data)" color="black">
                  <Switch/>
                </el-icon>
              </el-tooltip>
            </span>-->
              <el-tooltip content="上传数据集" placement="top" :disabled="showTooltip">
                <el-icon v-show="data.showAdd" class="btn" @click="uploadNode(node,data)" color="black"><Plus/></el-icon>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-tree>
    </div>
  </div>
<!--  <el-dialog v-model="dlgBomCreate" title="新建实例风机" width="35%" draggable>
    <div class="createDlg" v-loading="loadingInstance">
      <span style="font-size: 15px;font-weight: bold;">新建风机：</span>
      <el-input v-model="tuirbineName" placeholder="请输入新建风机名" style="width: 500px"></el-input>
    </div>
    <div class="footerCreate">
      <el-button class="normalBtn" @click="dlgBomCreate = false; tuirbineName = ''">取 消</el-button>
      <el-button class="normalBtn" @click="createTurbine(); ">确 认</el-button>
    </div>
  </el-dialog>-->

</template>
<script>

import {createProInstance} from "@/api/diagnosis/graphVis/dataIngestion";
import {ElMessage} from "element-plus";
import {Switch, View} from "@element-plus/icons-vue";
import {useRouter} from "vue-router";


let globeParams = {}

export default {
  name: 'lazySwTree',
  components: {Switch, View},


  data() {
    return {
      updateBom: false,
      upload: false,
      dlgBomCreate: false,
      loadingInstance: false,
      tuirbineName: '',
      display: true,
      showTooltip: true,      //  是否开启图标文字提示
      iconTooltipColor: 'black',   //  图标提示按钮颜色
    }
  },

  // 父级传给组件的数据，
  props: {
    proId: 0,
    nodeType: {
      type: String,
      default: 'GBOM'
    },
    hasline: {
      Boolean,
      default: true           // 默认有连线
    },
    data: Array,
    accordion: {        //是否手风琴模式：是否每次只打开一个同级树节点
      type: Boolean,
      default: () => {
        return false
      }
    },
    isLazy: {        //是否手风琴模式：是否每次只打开一个同级树节点
      type: Boolean,
      default: () => {
        return false
      }
    },
    defaultProps: {
      type: Object,
      default: () => {
        return {
          children: 'children',
          label: 'name',
          isLeaf: 'leaf',
        }
      }
    },
    expandNode: {       // 点击节点是否触发展开，默认不展开
      type: Boolean,
      default: () => {
        return false
      }
    },
    defaultCheckedKeys: {       //默认勾选的节点的 key 的数组
      type: Array,
      default: () => {
        return []
      }
    },
    nodeCurrentKey: {       //指定节点的key，作为每个节点的唯一标识
      type: String,
      default: () => {
        return 'id'
      }
    },
    defaultExpandKeys: {      //  默认展开的节点
      type: Array,
      default: () => {
        return []
      }
    },
    currentKey: {
      default: () => {
        return ''
      }
    },
    showCheckBox: {     //  是否显示CheckBox，默认不显示
      type: Boolean,
      default: () => {
        return false
      }
    },
    highLight: {
      type: Boolean,
      default: () => {
        return true
      }
    },
    checkStrictly: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    defaultExpandAll: {
      type: Boolean,
      default: () => {
        return false
      }
    },

  },

  methods: {
    // 懒加载树节点
    loadTreeNode (node, resolve) {
      this.$emit('loadTreeNode', node, resolve)
    },

    mouseover (data) {               // 鼠标移入
                        // vue3使用对象代理，不能用 this.$set(data, 'myshow', true)给对象赋值。因此，这里直接赋值
      if(this.upload===true){
        data.myshow2 = true
      }else{
        data.myshow = true
      }
    },

    mouseout (data) {               // 鼠标移出
      if(this.upload===true){
        data.myshow2 = false
      }else{
        data.myshow = false
      }

    },

    //  向父组件发送节点相关事件
    uploadNode (node, data) {
      this.$refs.tree.setCurrentNode(data)            //  设置为当前节点
      this.$emit('eventNodeUpload', node, data, 'template')
    },

    addNode (node, data) {
      this.$refs.tree.setCurrentNode(data)            //  设置为当前节点
      this.$emit('eventNodeAdd', node, data, 'template')
    },

    viewData(node, data) {
      this.$refs.tree.setCurrentNode(data)
      this.$emit('eventNodeView', node, data, 'template')
    },

    editNode (node, data) {
      this.$emit('eventNodeEdit', node, data, 'template')
    },

    deleteNode(data){
      this.$emit('eventNodeDelete', data, 'template')
    },

    copyNode(data){
      this.$emit('eventNodeCopy', data, 'template')
    },

    moveNode(data){
      this.$emit('eventNodeMove', data, 'template')
    },

    //  是否显示图标文字提示
    iconHelp(){
      this.showTooltip = !this.showTooltip
      this.iconTooltipColor = this.showTooltip ? 'black' : 'yellow'
    },

    //  懒加载，刷新当前节点nodeId，并展开它的子节点（用于子节点添加后的刷新）
    /**
     * 重新加载，并展开节点
     * 用于懒加载时，子节点添加后的刷新。非懒加载时，需要预先将子节点放在children中调用该方法扩展节点
     * @param nodeId：待刷新的节点id
     */
    refreshExpand(nodeId){
      let node = this.$refs.tree.getNode(nodeId)
      //node.loaded = false
      node.expand()
    },

    //  懒加载，刷新节点nodeId的父节点，重新展开它的子节点（用于子节点删除后的刷新）
    refreshParentNode(sonNodeId){
      let parentNode = this.$refs.tree.getNode(sonNodeId).parent            //  获取父节点
      this.refreshExpand(parentNode.data.id)          //  刷新父节点
    },

    handleNodeClick (data, node, treeNode, event) {
      this.$emit('eventNodeClick', data, node, treeNode, event)
    },

    nodeExpand (data, node, treeNode) {
      this.$emit('nodeExpand', data, node, treeNode)
    },

    checkChange(data, checked, indeterminate) {
      this.$emit('checkChange', data, checked, indeterminate)
    },

    //  节点收缩
    nodeCollapse(nodeId){
      let node = this.$refs.tree.getNode(nodeId)
      node.collapse()
    },

    //  返回当前节点数据
    getCurrentNodeData(){
      return  this.$refs.tree.getCurrentNode()
    },

    //  返回当前节点
    getCurrentNode(){
      let curNodeKey = this.$refs.tree.getCurrentKey()
      return this.$refs.tree.getNode(curNodeKey)
    },

    //  获取父节点
    getParentNode(nodeId){
      return this.$refs.tree.getNode(nodeId).parent            //  获取父节点
    },

    //  将节点设置为选中状态
    setNodeSelected(nodeId){
      this.$refs.tree.setCurrentKey(nodeId)
    },

    //  给pNode添加一个子节点(节点复制后的更新)
    addSonNode(pNode, sonNode){
      let self = this
      pNode.children.push(sonNode)
      setTimeout(() => self.$refs.tree.setCurrentKey(sonNode.id), 200)        //  将新增的节点设为选中状态
    },

    //  删除data中的一个子节点（节点删除后的状态更新）
    removeSonNode(nodeId){
      let pNode = this.$refs.tree.getNode(nodeId).parent                              //  获取父节点
      pNode.data.children = pNode.data.children.filter(item => item.id !== nodeId)    //  在父节点的children过滤出子节点，并删除它
      return pNode
    },

    /*更新节点信息（节点编辑后的状态更新）
    * 输入：nodeData：节点id以及节点需要更新的属性
    * */
    updateNode(nodeData){
      let node = this.$refs.tree.getNode(nodeData.id)
      for (let key in nodeData) {             //  遍历对象的每一个属性，并更新属性
        if (nodeData.hasOwnProperty(key)) {
          node.data[key] = nodeData[key]
        }
      }
      this.$refs.tree.setCurrentKey(node.data.id)     //  将当前编辑的节点设为选中状态
    },
    upProductModel(){
      this.$emit('upProductModel', true)
    },
    uploadDataset(){
      this.upload = !this.upload
      this.currentNode = this.getCurrentNodeData()
      if(this.currentNode){
        this.$emit('getTreeNodes', this.currentNode.nodeLevel)
      }else {
        this.$emit('getTreeNodes', 3)
      }
    },
    ManageDataset(){
      this.$router.push({
        path: '/DatasetManage/index',  // 注意：path 不能用 @，应该是 vue-router 的路由 path
      }).catch(() => {
        ElMessage.error('页面未找到');
      });
    },
    updateByHand(){
      this.updateBom = !this.updateBom
      this.currentNode = this.getCurrentNodeData()
      if(this.currentNode){
        this.$emit('getTreeNodes', this.currentNode.nodeLevel)
      }else {
        this.$emit('getTreeNodes', 3)
      }
    },

    async createTurbine() {

      this.loadingInstance = true
      try {
        await createProInstance({proId: this.proId, nodeName: this.tuirbineName}).then(res => {
          if (res.data.code === 0) {
            this.$emit('getInstanceTreeNodes', 1)
            ElMessage({
              message: "成功新建风机实例！",
              type: 'success',
            })
          }
        })
      } catch (error) {
        // 处理错误情况
        console.error('创建实例时发生错误:', error)
      }finally {
        this.loadingInstance = false
        this.dlgBomCreate = false
        this.tuirbineName = ''
      }
    }
  },




}
</script>
<style lang="scss" scoped>

.update-zone {
  text-align: right;
  margin: 5px 5px;
  display: flex;
  justify-content: flex-end;
  gap: 10px; /* 按钮间距 */
}

.createDlg{
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  margin-left: 20px;
}
.footerCreate{
  text-align: center;
  margin-top: 10px;
  .el-button {
    height: 30px;
  }
}

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
      .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {  /*高亮当前节点*/
        background-color: rgba(138, 194, 252, 0.53) !important;
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
