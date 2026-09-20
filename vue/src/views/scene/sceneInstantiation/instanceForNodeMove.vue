<template>
    <div class="subTree">
        <sw-tree class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
                 :defaultProps="defaultProps" :expandNode="true"
                 :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                 :showCheckBox=true :checkStrictly=true :isLazy=false
                 @nodeExpand="handleNodeExpand" @checkChange="handleCheckChange"
                 >
        </sw-tree>
    </div>
</template>
<script>
    import swTree from '@/components/myComponent/swTree.vue'
    import {onMounted, ref} from "vue";
    import {
      reqInstanceSonNodes,
      reqInstanceTreeNodes,
    } from "@/api/diagnosis/graphVis/dataIngestion";

    let globeParams = {}     //  声明一个全局参数对象
    let _tree

    export default {
        name: 'nodeMoveTemplate',

        data() {
            return {
                treeData: [],
                defaultProps: {
                    children: 'children',
                    label: 'name',
                    isLeaf: 'leaf'
                },
                defaultExpandKeys: [],

            }
        },

        props: {
          proId: 0,
          turbineCode: ''
        },

        components: {
            swTree,
        },

        setup() {
            let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
            onMounted(() => {
                _tree = lazyTree
            })
            return {lazyTree}
        },

        mounted() {
            globeParams.nodeLevel = 3       //    预先展开3层节点
            this.getTreeNodes(globeParams.nodeLevel)
        },

        methods: {
            //  节点扩展消息响应
            async handleNodeExpand(data, node, treeNode) {
                if(node.level >= globeParams.nodeLevel){
                    let response = await reqInstanceSonNodes(data.nodeCode, data.turbineCode)
                    if (response.data.data){
                        response.data.data.forEach((item) => {
                            item.children = item.leaf ? [] : [{id:'loading',name:'节点加载中...'}]
                        })
                        node.data.children = response.data.data
                    }
                }
            },

            //  设置节点勾选为单选（一次只能勾选一个节点）
            handleCheckChange(data, checked, indeterminate) {
                if (checked){
                    _tree.value.$refs.tree.setCheckedKeys([data.id])            //  调用el-tree自身的方法，将当前节点设为勾选
                    _tree.value.setNodeSelected(data.id)
                }

            },

            // 根据节点层级数，加载结构树的一组节点
            async getTreeNodes(nodeLevel) {
                try {
                    let response = await reqInstanceTreeNodes(this.proId, nodeLevel)
                    if (response.data.data) {
                        this.treeData = []
                        this.expandKeys = []            //  缓存待扩展的节点
                        let rootNodes = response.data.data
                            .filter(ele => (ele.nodeType === "Root" || ele.nodeType === "Root-Leaf") && ele.turbineCode === this.turbineCode)
                        for (let item of rootNodes) {
                            this.treeData.push(item)
                            this.expandKeys.push(item.id)
                            let children = response.data.data
                                .filter(ele => ele.turbineCode === this.turbineCode)
                            await this.setChildren(item, children)
                        }
                        this.defaultExpandKeys = this.expandKeys            //  扩展节点
                    }
                } catch (error) {
                    console.log(error)
                }
            },

            //  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
            setChildren(pNode, nodeList){
                let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList)
                let children = res.sonNodes
                if (children.length === 0){
                    if ((pNode.nodeType === 'Mid')||(pNode.nodeType === 'Root')){           //  如果不是叶子节点，节点前显示"+"号
                        this.expandKeys = this.expandKeys.filter(item => item !== pNode.id) //  从扩展节点中删除它
                        pNode.children = [{id:'loading',name:'节点加载中...'}]
                    }
                    return pNode
                }
                else{
                    pNode.children = children
                    children.forEach((item) => {
                        this.expandKeys.push(item.id)          //  添加到扩展节点
                        this.setChildren(item, res.otherNodes)
                    })
                }
            },

            //  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
            getChildrenByNodeCode(pNodeCode, nodeList){
                let sonNodes = []
                let otherNodes = []
                let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
                nodeList.forEach((item) => {
                    if (regex.test(item.nodeCode))          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
                        sonNodes.push(item)
                    else
                        otherNodes.push(item)
                })
                return {sonNodes:sonNodes, otherNodes:otherNodes}
            },

            //  返回用户当前选中的节点
            getCurrentNode(){
                return _tree.value.getCurrentNodeData()
            }

        }

    }

</script>
<style lang="scss" scoped>
    .subTree {
        width: 100%;
        height: 100%;
        background-color: cadetblue;
        margin-top: 15px;
        padding-top: 10px;
        padding-bottom: 10px;
    }

</style>