<template>
    <div class="mycontainer">
        <div class="mytree">
            <failure-gbom-tree @clickNode="clickNode"></failure-gbom-tree>       <!--布局树组件-->
        </div>
        <div class="mytable">
            <failure-table ref="refTable" v-if="isActivate"></failure-table>                       <!--布局表格组件-->
        </div>
    </div>
</template>
<script>
    import { ref } from 'vue'
    import failureGbomTree from './tree.vue'
    import failureTable from './table.vue'

    let globeParams = {}     //  声明一个全局参数对象

    export default {

        components: {
            failureGbomTree,            //  挂载树组件
            failureTable                //  挂载表格b组件
        },



        setup() {
            let refTable = ref(null)
            let isActivate = ref(false)
            return { refTable, isActivate}
        },

        methods: {
            clickNode(node){
                globeParams.curNode = node                  //  缓存当前节点
                if (this.isActivate)
                    this.refTable.getFailureMode(node)            //  根据节点id，调用子组件的方法，查询节点关联的故障模式记录
                else           //  如果是第一次页面加载
                    this.isActivate = true          //  激活refTable
            },

            getCurNode(){
                return globeParams.curNode
            }
        },



    }

</script>
<style lang="scss" scoped>
    .mycontainer {
        height: 100%;
        width: 100%;
        display: flex;
        .mytree{
            height: 100%;
            width: 30%;
            background-color: -webkit-focus-ring-color;
            overflow-y: auto;           /*  垂直滚动条   */
        }
        .mytable{
            height: 100%;
            width: 70%;
            overflow-y: auto;
        }
    }
</style>