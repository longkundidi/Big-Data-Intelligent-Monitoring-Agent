<!--
    设置indent=“0”，不然缩进会有问题，这里，还需要.el-tree-node { padding-left: 16px; }


-->
<template>
    <div class="tree-zone">
        <div v-show="display" :class="hasline ? 'treestyle lineyes' : 'treestyle'">
            <el-tree :data="data" :indent="0" ref="tree" :accordion="accordion" :props="defaultProps"
                     :expand-on-click-node="expandNode" :default-checked-keys="defaultCheckedKeys" :node-key="nodeCurrentKey" lazy
                     :default-expanded-keys="defaultKeys" icon="none"
                     :currentKey="currentKey" :show-checkbox="showCheckBox" :check-strictly="checkStrictly"
                     :default-expand-all="defaultExpandAll" :load="loadTreeNode" @node-click="handleNodeClick"
                     >
                <template #default="{ node, data }">
                    <div class="custom-tree" @mouseover="mouseover(data)" @mouseleave="mouseout(data)">
                        <span style="line-height: 16px;"> {{ data.name }} </span>
                        <div class="tree-btn" v-show="data.myshow" @click.stop="()=>{}" >
                            <span>&nbsp&nbsp&nbsp&nbsp</span>
                            <el-icon v-show="data.showAdd" class="btn" @click="addNode(node,data)" color="white"><Plus/></el-icon>
                            <el-icon v-show="data.showEdit" class="btn" color="white"><Edit /></el-icon>
                            <el-icon v-show="data.showRemove" class="btn" @click="deleteNode(data)" color="red"><Delete /></el-icon>
                        </div>
                    </div>

                </template>
            </el-tree>
        </div>
    </div>
</template>
<script>

    let globeParams = {}

    export default {
        name: 'lazySwTree',

        data() {
            return {
                display: true,
            }
        },

        // 父级传给组件的数据，
        props: {
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
            defaultKeys: {      //  默认展开的节点
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
                data.myshow = true                   // vue3使用对象代理，不能用 this.$set(data, 'myshow', true)给对象赋值。因此，这里直接赋值
            },

            mouseout (data) {               // 鼠标移出
                data.myshow = false
            },

            addNode (node, data) {          //  向父组件发送节点添加事件
                this.$refs.tree.setCurrentNode(data)            //  设置为当前节点
                this.$emit('eventadd', node, data)
            },

            deleteNode(data){
                this.$emit('eventDelete', data)
            },

            //  刷新当前节点nodeId，并展开它的子节点（用于子节点添加后的刷新）
            refreshExpand(nodeId){
                let node = this.$refs.tree.getNode(nodeId)
                node.loaded = false
                node.expand()
            },

            //  刷新节点nodeId的父节点，重新展开它的子节点（用于子节点删除后的刷新）
            refreshParentNode(sonNodeId){
                let parentNode = this.$refs.tree.getNode(sonNodeId).parent            //  获取父节点
                this.refreshExpand(parentNode.data.id)          //  刷新父节点
            },

            handleNodeClick (data, node, resolve) {debugger
                this.$emit('eventNodeClick', data, node, resolve)
            },




        },




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
                    color: #fff;                /*字体颜色：白色*/
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
                    background: url("/img/lazytree/circleplus.svg") no-repeat 0 0px;
                    display: block;
                    width: 16px;
                    height: 16px;
                    background-size: cover;
                }
                .el-tree-node__expand-icon.expanded:before{     /*有子节点 且已展开*/
                    content: "";
                    background: url("/img/lazytree/remove.svg") no-repeat 0 0px;
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
                    color: gold;
                    font-weight: bold;
                    background-color: rgba(138, 194, 252, 0.53) !important;
                }
                .el-tree-node:before {          /*显示节点间连接的竖线*/
                    border-left: 1px dashed #dcdcdc;
                    bottom: 0px;
                    height: 100%;
                    top: -26px;
                    width: 3px;
                }
                .el-tree-node:after {           /*显示节点间连接的横线*/
                    border-top: 1px dashed #dcdcdc;
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

<!--
https://blog.csdn.net/zero_wsh/article/details/130851724-->
