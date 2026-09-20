<template>
    <div class="mycontainer">
        <div class="mytree">
            <modelBaseTree @checkNode="loadModel" @clickModelNode="clickNode" :mbObj="mbObj"></modelBaseTree>       <!--布局树组件-->
        </div>
        <!--<div class="mycontent">
            <baseModel ref="refModel" :mbObj="mbObj"></baseModel>                       &lt;!&ndash;布局模型组件&ndash;&gt;
        </div>-->
    </div>
</template>
<script>
    import { useRoute } from 'vue-router'
    import modelBaseTree from './tree.vue'           //  引用树组件
    //import baseModel from './model.vue'         //  引用模型组件

    let myRoute

    export default {
        data () {
            return {
                mbObj: JSON.parse(myRoute.query.mbObj),
            }
        },

        components: {
            modelBaseTree,      //  挂载树组件
            //baseModel           //  挂载模型渲染组件
        },

        setup() {
            myRoute = useRoute()

        },

        methods: {
            //  加载勾选节点的模型
            loadModel (arr, checked, type, node) {
                this.$refs.refModel.loadModel(arr)   //  调用子组件方法，加载勾选节点的模型
            },

            clickNode(data){
                this.$refs.refModel.setCurNode(data)
            }

        },


    }


</script>


<style lang="scss" scoped>
    .mycontainer {
        height: 100%;
        width: 100%;
        .mytree{
            height: 100%;
            width: 30%;
            background: cornflowerblue;
        }
    }
</style>