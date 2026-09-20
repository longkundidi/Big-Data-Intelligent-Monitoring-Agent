<template>
    <div class="mycontainer">
        <sw-tree class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
                      :defaultProps="defaultProps" :expandNode="true"
                      :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                      :showCheckBox=false :checkStrictly=true :isLazy=false
                      @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand"
                      @eventNodeAdd="dlgAddSonNode" @eventNodeEdit="dlgEditNode"
                      @eventNodeDelete="dlgDeleteNode" @eventNodeCopy="dlgCopyNode"
                      @eventNodeMove="dlgMoveNode">
        </sw-tree>
        <el-dialog v-model="dlgNodeAddEdit" :title="titleName"
                    :close-on-click-modal="false" width="50%" draggable>
            <div class="content">
                <div>
                    <avue-form ref="form" :option="nodeOption" v-model="nodeForm"
                               @submit="handleSubmit">
                    </avue-form>
                </div>
            </div>
        </el-dialog>
        <el-dialog v-model="dlgNodeMove" title="选择目标节点" width="50%" draggable @close="treeDialogClose">
            <div class="treeDialog" >
                <node-move-tree ref="refNodeTree" style="width: 100% ;height: 90%" :scene-id="sceneId"></node-move-tree>
                <el-button class="normalBtn" @click.stop="doNodeMove()">确定</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
    import swTree from '@/components/myComponent/swTree.vue'
    import {
      reqAddRootNode,
      reqTreeNodes,
      reqSonNodes,
      reqAddSonNode,
      reqObjById,
      reqPutObj,
      reqDeleteNodes,
      reqCopyNode,
      reqMoveNode,
      reqDeleteNodesBySceneId,
      reqSonNodesBySceneId,
      reqTreeNodesBySceneId
    } from '@/api/sw/model3d/configGbomTree/index.js'
    import { nodeOption } from '@/const/crud/sw/model3d/configGbomTree'
    import {onMounted, ref} from 'vue'
    import nodeMoveTree from './treeForNodeMove.vue'
    import {getMetaModelIdByUserId, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";

    let __tree
    let globeParams = {}     //  声明一个全局参数对象

    export default {
        name: 'gbomTree',

        data() {
            return {
                treeData: [],
                defaultProps: {
                    children: 'children',
                    label: 'name',
                    isLeaf: 'leaf'
                },
                defaultExpandKeys: [],

                dlgNodeAddEdit: false,          //  控制新增、编辑按钮点击后，弹窗的显示和隐藏
                titleName: '',
                nodeOption: nodeOption,
                nodeForm: {},
                dlgNodeMove: false,             //  控制节点移动前，弹窗的显示和隐藏
                sceneId: '',

            }
        },

        components: {
            swTree,
            nodeMoveTree
        },

        setup() {
            let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
            onMounted(()=>{
                __tree = lazyTree
            })
            return { lazyTree }
        },
        mounted() {
              this.initParams()
              this.projectInit().then(() => {
                this.getTreeNodes(globeParams.nodeLevel)
              })
        },
        methods: {
            //  初始化全局参数
            initParams(){
                globeParams.nodeLevel = 3       //    预先展开3层节点
            },

            //  节点点击消息响应
            handleNodeClick(data, node, treeNode, event){
            },

            async projectInit() {
              const userId = this.$store.state.user.userInfo.userId
              return getMetaModelIdByUserId({ userId, userRole: 'GENERAL_USER' })
                  .then(response => {
                    if (response.data.code === 0) {
                      this.sceneId = response.data.data.metaModelId;
                      console.log("sceneId:", this.sceneId);
                    }
                  });
            },


          //  节点扩展消息响应
            async handleNodeExpand(data, node, treeNode) {
                if ((node.level >= globeParams.nodeLevel)||(this._sonNodeHasLoading(data))) {
                    let response = await reqSonNodesBySceneId(this.sceneId,data.nodeCode)
                    if (response.data.data) {
                        response.data.data.forEach((item) => {
                            item.showAdd = true
                            item.showRemove = true
                            item.showEdit = true
                            item.showCopy = true
                            item.showMove = true
                            item.showHelp = true
                            item.children = item.leaf ? [] : [{id:'loading',name:'节点加载中...'}]
                        })
                        // 只加载sceneId相同的子节点
                        node.data.children = response.data.data.filter(item => item.sceneId === data.sceneId)
                    }
                }
            },

            // 根据节点层级数，加载结构树的一组节点
            async getTreeNodes(nodeLevel) {
                try {
                    let response = await reqTreeNodesBySceneId(this.sceneId, nodeLevel)
                    if ((response.data.data)&&(response.data.data.length > 0)) {
                        this.treeData = []
                        this.expandKeys = []            //  缓存待扩展的节点
                        // 先找出所有根节点（sceneId不同的多棵树）
                        let rootNodes = response.data.data.filter(ele => (ele.nodeType === "Root" || ele.nodeType === "Root-Leaf"))
                        for (let item of rootNodes) {
                            item.showAdd = true
                            item.showRemove = true
                            item.showEdit = true
                            item.showHelp = true
                            this.treeData.push(item)
                            this.expandKeys.push(item.id)
                            // 递归时只处理同sceneId的节点
                            await this.setChildren(item, response.data.data.filter(n => n.sceneId === item.sceneId))
                        }
                        this.defaultExpandKeys = this.expandKeys            //  扩展节点
                    } else {
                        let confirmResult = await this.$confirm('首次编辑GBOM，系统未找到根节点，是否创建根节点?', '提示', {
                            confirmButtonText: '确定',
                            cancelButtonText: '取消',
                            type: 'warning'
                        })
                        if (confirmResult) {
                            let addRootNodeResponse = await reqAddRootNode({nodeName:'模版根节点',nodeCode:'TM1'} )
                            if (addRootNodeResponse.data.code === 200){
                                this.$message.success('默认根节点创建成功')
                                this.getTreeNodes(globeParams.nodeLevel)
                            }
                        }
                    }
                } catch (error) {
                    console.log(error)
                }
            },

            // 递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
            setChildren(pNode, nodeList){
                // 只递归同sceneId的节点
                let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList.filter(n => n.sceneId === pNode.sceneId))
                let children = res.sonNodes
                if (children.length === 0){
                    if ((pNode.nodeType === 'Mid')||(pNode.nodeType === 'Root')){
                        this.expandKeys = this.expandKeys.filter(item => item !== pNode.id)
                        pNode.children = [{id:'loading',name:'节点加载中...'}]
                    }
                    return pNode
                }
                else{
                    pNode.children = children
                    children.forEach((item) => {
                        item.showAdd = true
                        item.showRemove = true
                        item.showEdit = true
                        //item.showCopy = true
                        //item.showMove = true
                        item.showHelp = true
                        this.expandKeys.push(item.id)
                        this.setChildren(item, res.otherNodes)
                    })
                }
            },

            // 正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
            getChildrenByNodeCode(pNodeCode, nodeList){
                let sonNodes = []
                let otherNodes = []
                let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
                nodeList.forEach((item) => {
                    if (regex.test(item.nodeCode))
                        sonNodes.push(item)
                    else
                        otherNodes.push(item)
                })
                return {sonNodes:sonNodes, otherNodes:otherNodes}
            },


            //  弹窗添加子节点
            dlgAddSonNode (pNode, pData) {
                this.titleName = "新增子节点"
                this.nodeForm = {              //  节点新增弹框的表单对象
                    nodeId: null,
                    nodeName: '',
                    swsort: '',
                    memo: '',
                    sceneId: pData.sceneId
                },
                this.dlgNodeAddEdit = true
            },

            handleSubmit(nodeForm, done) {
                if (this.titleName === '新增子节点')
                    this.nodeAdd(nodeForm, done)
                else if (this.titleName === '编辑节点')
                    this.nodeEdit(nodeForm, done)
            },

            //  节点子添加
            async nodeAdd(nodeForm, done) {
                let pNode = __tree.value.getCurrentNodeData()     // 获取父节点
                try {
                    let response = await reqAddSonNode(pNode.id, nodeForm)
                    if (response.data.code === 200){
                        this.$message.success('子节点添加成功')
                        let newNode = response.data.data        //  给新添加的节点增加按钮属性
                        newNode.showAdd = true
                        newNode.showRemove = true
                        newNode.showEdit = true
                      //  newNode.showCopy = true
                       // newNode.showMove = true
                        newNode.showHelp = true
                        newNode.children = []          //  注意：新添加的节点都是叶子节点
                        pNode.children.push(newNode)
                        this.$nextTick(() => {
                            __tree.value.setNodeSelected(newNode.id)       //  结构树渲染成功后，将新添加的节点设置为选中状态
                        })
                    }
                    this.dlgNodeAddEdit = false
                    done()
                } catch (error) {
                    console.log(error)
                }
            },


            dlgEditNode(node, data) {
              if (data.usedCount !== 0) {
                this.$message.warning('当前节点被机型结构树使用，不可编辑')
                return
              }
                this.titleName = "编辑节点"
                reqObjById(data.id).then(response => {
                    this.nodeForm = response.data.data          //  填写可编辑字段
                    this.dlgNodeAddEdit = true
                })
            },

            //  节点编辑
            async nodeEdit(nodeForm, done) {
                let response = await reqPutObj(nodeForm)
                if (response.data.code === 200) {
                    this.$message.success('节点编辑成功')
                    this.dlgNodeAddEdit = false
                    if ((response.data.data.nodeType !== 'Root')&&(response.data.data.nodeType !== 'Root-Leaf')) {       //  如果存在父节点
                        let newData = {
                            id: response.data.data.id,
                            name: response.data.data.name,
                            memo: response.data.data.memo
                        }
                        __tree.value.updateNode(newData)            //  更新节点状态
                    }else
                        this.getTreeNodes(globeParams.nodeLevel)
                }
                done()
            },


            async dlgDeleteNode(data) {
              if (data.usedCount !== 0) {
                this.$message.warning('当前节点被机型结构树使用，不可删除')
                return
              }
                try {
                    await this.$confirm('此操作将删除当前节点及其所有子节点, 是否继续?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    let response = await reqDeleteNodesBySceneId(data.sceneId,data.nodeCode)
                    if (response.data.code !== 208) {
                        this.$message({
                            type: 'info',
                            message: response.data.message
                        })
                    } else {
                        if ((data.nodeType !== 'Root')&&(data.nodeType !== 'Root-Leaf')){       //  如果存在父节点
                            let pNode = __tree.value.removeSonNode(data.id)
                            __tree.value.setNodeSelected(pNode.data.id)             //  将父节点设置为选中状态
                        }else
                            this.treeData = []
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                    }
                } catch (error) {
                    this.$message({
                        type: 'info',
                        message: '已取消删除'
                    })
                }
            },


            async dlgCopyNode(data) {
                try {
                    await this.$confirm('此操作将复制当前节点及其所有子节点到同一个父节点下, 是否继续?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    let response = await reqCopyNode(data.nodeCode)
                    if (response.data.code === 200) {
                        let newNode = response.data.data        //  给新复制的节点增加按钮属性
                        newNode.showAdd = true
                        newNode.showRemove = true
                        newNode.showEdit = true
                       // newNode.showCopy = true
                       // newNode.showMove = true
                        newNode.showHelp = true
                        newNode.children = newNode.nodeType === 'Leaf' ? [] : [{id:'loading',name:'节点加载中...'}]          //  注意：这里新复制的节点可能没有完成子节点的加载
                        let pNode = __tree.value.getParentNode(data.id)         //  获取父节点
                        __tree.value.addSonNode(pNode.data, newNode)
                        this.$message.success('节点复制成功')
                    }
                }catch (error) {
                    console.log(error)
                }
            },

            dlgMoveNode(data) {
                this.sourceNode = data      //  保存源节点
                this.sceneId = data.sceneId
                this.dlgNodeMove = true
            },

            async doNodeMove(){
                let targetNode = this.$refs.refNodeTree.getCurrentNode()        //  获取目标节点
                if (targetNode){
                    this.dlgNodeMove = false
                    await this.$confirm('确定执行节点移动操作吗？', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    let response = await reqMoveNode(this.sourceNode.nodeCode,targetNode.nodeCode)
                    if (response.data.code === 200){
                        await this.getTreeNodes(response.data.data.nodeLevel)                    //  更新结构树，注意：这里需要根据被移动节点的最新层级决定加载的节点级数，确保被移动节点被加载进结构树
                        __tree.value.setNodeSelected(response.data.data.id)                      //  将被移动的节点设置为选中状态
                        this.$message.success('节点移动成功')
                    }
                }
            },
          treeDialogClose() {
              this.$refs.refNodeTree.clearSelected()
          }
        }

    }
</script>
<style lang="scss" scoped>
    @import '@/styles/my-dialog.scss';
    .mycontainer {
        width: 100%;
        height: calc(100% - 15px);
        padding-top: 15px;
        background-color: -webkit-focus-ring-color;
        overflow-y: auto;           /*  垂直滚动条   */
    }
    .treeDialog{
        display: flex;
        flex-direction: column;
        justify-content: space-between; /* 将内容水平分布在容器中 */
        align-items: flex-end;
        .el-button{
            background-color: cadetblue;
            width: 65px;
            height: 40px;
            margin-top: 15px;
            border-color: white;
            &:hover {
                background-color: chocolate; /* 设置悬浮时的背景色 */
            }
        }
    }

</style>
