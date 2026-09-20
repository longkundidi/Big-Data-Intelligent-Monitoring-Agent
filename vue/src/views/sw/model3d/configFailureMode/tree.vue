<template>
    <div class="mycon">
        <sw-tree class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
                 :defaultProps="defaultProps" :expandNode="true"
                 :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                 :showCheckBox=false :checkStrictly=true :isLazy=false
                 @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand">
        </sw-tree>
    </div>
</template>
<script>
    import swTree from '@/components/myComponent/swTree.vue'
    import {
      reqTreeNodesBySceneId,
      reqSonNodesBySceneId
    } from '@/api/sw/model3d/configGbomTree/index.js'
    import { getMetaModelIdByUserId } from '@/api/diagnosis/graphVis/graphVisPro'


    let globeParams = {}     //  声明一个全局参数对象

    export default {
        name: 'failureGbomTree',

        data() {
            return {
                treeData: [],
                defaultProps: {
                    children: 'children',
                    label: 'name',
                    isLeaf: 'leaf'
                },
                sceneId: '',
                defaultExpandKeys: []
            }
        },

        components: {
            swTree
        },

        async mounted() {
            globeParams.nodeLevel = 3
            await this.projectInit()
            await this.getTreeNodes(globeParams.nodeLevel)
            this.$emit('clickNode', this.treeData[0])
        },

        methods: {
            async projectInit() {
                const userId = this.$store.state.user.userInfo.userId
                const response = await getMetaModelIdByUserId({ userId, userRole: 'GENERAL_USER' })
                if (response.data.code === 0) {
                    this.sceneId = response.data.data.metaModelId
                }
            },

            // 根据节点层级数，加载结构树的一组节点
            async getTreeNodes(nodeLevel) {
                try {
                    let response = await reqTreeNodesBySceneId(this.sceneId, nodeLevel)
                    if ((response.data.data)&&(response.data.data.length > 0)) {
                        this.treeData = []
                        this.expandKeys = []            //  缓存待扩展的节点
                        let rootNodes = response.data.data.filter(ele => ele.nodeType === 'Root' || ele.nodeType === 'Root-Leaf')
                        for (let item of rootNodes) {
                            this.treeData.push(item)
                            this.expandKeys.push(item.id)
                            await this.setChildren(item, response.data.data.filter(n => n.sceneId === item.sceneId))
                        }
                        this.defaultExpandKeys = this.expandKeys            //  扩展节点
                    } else
                        this.$message.error('没有找到GBOM树，请先创建GBOM树')
                } catch (error) {
                    console.log(error)
                }
            },

            // 递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
            setChildren(pNode, nodeList){
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

            // 节点扩展消息响应
            async handleNodeExpand(data, node, treeNode) {
                if(node.level >= globeParams.nodeLevel){
                    let response = await reqSonNodesBySceneId(this.sceneId, data.nodeCode)
                    if (response.data.data) {
                        let children = response.data.data.filter(item => item.sceneId === data.sceneId)
                        children.forEach((item) => {
                            item.children = item.leaf ? [] : [{id:'loading',name:'节点加载中...'}]
                        })
                        node.data.children = children
                    }
                }
            },

            //  节点点击消息响应
            handleNodeClick(data, node, treeNode, event){
                this.$emit('clickNode', data)
            },

        }


    }
</script>
<style lang="scss" scoped>
    .mycon {
        width: 100%;
        height: calc(100% - 15px);
        padding-top: 15px;
    }
</style>