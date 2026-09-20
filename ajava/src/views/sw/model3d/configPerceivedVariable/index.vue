<template>
    <div class="mycontainer">
        <div class="mytree">
            <variable-gbom-tree @clickNode="clickNode"></variable-gbom-tree>     <!--布局树组件-->
        </div>
        <div class="mytable">
            <variable-table ref="refTable" v-if="isActivate && !isSelect"></variable-table>
            <variable-table-select ref="refSelectTable"
                                   v-if="localIsSelect"
                                   @update:selected-variables="handleSelectedVariables"></variable-table-select>
        </div>
    </div>
</template>
<script>
    import { ref } from 'vue'
    import variableGbomTree from './tree.vue'
    import variableTable from './table.vue'
    import variableTableSelect from './table-select.vue'

    let globeParams = {}     //  声明一个全局参数对象

    export default {

        components: {
            variableGbomTree,            //  挂载树组件
            variableTable,                //  挂载表格组件
            variableTableSelect
        },

        props: {
            isSelect: {
               type: Boolean,
               default: false
            },
            isFirstSelectVal: {
               type: Boolean,
               required: true
            }
        },

        setup() {
            let refTable = ref(null)
            let refSelectTable = ref(null)
            let isActivate = ref(false)
            let localIsSelect = ref(false)
            return { refTable,refSelectTable,isActivate,localIsSelect }
        },

        watch: {
           isFirstSelectVal(newVal) {
              if(newVal && this.localIsSelect){
                 this.$nextTick(() => {
                     this.refSelectTable.clearSelected()
                 })
              }
           }
        },


        methods: {
            clickNode(node){
                globeParams.curNode = node                  //  缓存当前节点
                if (this.isActivate && !this.isSelect)
                  this.refTable.getVariable(node)            //  根据节点id，调用子组件的方法，查询节点关联的感知变量

                else if (!this.isActivate && this.isSelect)
                {
                  this.localIsSelect = true
                  this.$nextTick(() => {
                    this.refSelectTable.getVariable(node)
                  })

                }

                else
                {
                  this.isActivate = true
                }


            },

            getCurNode(){
                return globeParams.curNode
            },

            handleSelectedVariables(variables) {
                this.$emit('update:selected-variables', variables)
            },
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