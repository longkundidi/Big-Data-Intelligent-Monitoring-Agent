<template>
  <div class="mycontainer">
    <div class="mytree">
      <model-gbom-tree @clickNode="clickNode"></model-gbom-tree>       <!--布局树组件-->
    </div>
    <div class="mytable">
      <model-table ref="refTable" v-if="isActivate"></model-table>                       <!--布局表格组件-->
    </div>
  </div>
</template>
<script>
import {ref} from 'vue'
import modelGbomTree from '../configModelComposition/tree.vue'
import modelTable from '../configModelComposition/table.vue'

let globeParams = {}     //  声明一个全局参数对象

export default {

  components: {
    modelGbomTree,            //  挂载树组件
    modelTable                //  挂载表格组件
  },


  setup() {
    let refTable = ref(null)
    let isActivate = ref(false)
    return {refTable, isActivate}
  },

  methods: {
    clickNode(node) {
      globeParams.curNode = node                  //  缓存当前节点
      if (this.isActivate)
        this.refTable.getModels(node)       //  根据节点id，调用子组件的方法，查询节点关联的感知模型
      else  //  如果是第一次页面加载
        this.isActivate = true          //  激活refTable

    },

    getCurNode() {
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

  .mytree {
    height: 100%;
    width: 30%;
    background-color: -webkit-focus-ring-color;
    overflow-y: auto; /*  垂直滚动条   */
  }

  .mytable {
    height: 100%;
    width: 70%;
    overflow-y: auto;
  }
}
</style>
