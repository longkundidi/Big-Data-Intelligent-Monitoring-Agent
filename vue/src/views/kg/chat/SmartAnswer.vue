<template>
  <div class="container">
<!--    <div v-show="leftPaneVisible" class="left-pane">-->
<!--      <button @click="toggleLeftPane" class="toggle-btn">关闭</button>-->
<!--      <h1>问答说明</h1>-->
<!--      <h2>本页面功能包含两种问答方式：知识库搜索式问答和大模型生成式问答。 其中大模型已通过领域知识进行了微调。知识库问答基于风电图谱数据。目前该模式可问答的领域如下，您可以点击领域本体和数据了解相关知识。</h2>-->
<!--&lt;!&ndash;      <el-tree :data="treeData" :props="treeProps" class="tree" style="font-size: 15px;line-height: 1.4;background-color: #F5F5F7FF;" default-expand-all>&ndash;&gt;-->
<!--&lt;!&ndash;        <template v-slot="{ node }">&ndash;&gt;-->
<!--&lt;!&ndash;     <span class="tree-node">&ndash;&gt;-->
<!--&lt;!&ndash;      <span v-if="node.data.isDomain">{{ node.data.label }}</span>&ndash;&gt;-->
<!--&lt;!&ndash;      <span v-else>&ndash;&gt;-->
<!--&lt;!&ndash;        <span class="spanclass">{{ node.data.kgtype}}</span>&ndash;&gt;-->
<!--&lt;!&ndash;        <a href="#" @click.prevent="openNetDlg(node.data.kgtype)" class="link" >本体</a>&ndash;&gt;-->
<!--&lt;!&ndash;        <span class="spanclass">以及</span>&ndash;&gt;-->
<!--&lt;!&ndash;        <a href="#" @click.prevent="showData(node.data.kgtype, node.data.baseTypeId)" class="link">数据</a>&ndash;&gt;-->
<!--&lt;!&ndash;      </span>&ndash;&gt;-->
<!--&lt;!&ndash;    </span>&ndash;&gt;-->
<!--&lt;!&ndash;        </template>&ndash;&gt;-->
<!--&lt;!&ndash;      </el-tree>&ndash;&gt;-->
<!--&lt;!&ndash;      <owl-net-dlg ref="owlNetDlg"></owl-net-dlg>&ndash;&gt;-->
<!--    </div>-->
    <div class="right-pane">
      <ChildComponent></ChildComponent>
    </div>
  </div>
  <el-dialog
      :title="title"
      v-model="visible"
      width="85%"
      append-to-body
      :before-close="closeFile"
      destroy-on-close
  >
    <div v-show="viewShow" id="officeDiv" style="height: 78vh" >
      <iframe :src='viewUrl' style="width: 100%;height: 100%"></iframe>
    </div>
  </el-dialog>
</template>

<script setup>

// import OwlNetDlg from "../../views/kg/base/OwlNetDlg.vue";
import {onMounted, ref} from "vue";
import ChildComponent from './GmlAnswer.vue';
// import {SI_get} from "../../hooks/useSessionStorage";
// import {getTitleData} from "../../service/kg/overview/kgoverview";
// Add this line to the script setup
let leftPaneVisible = ref(true);
let visible = ref(false);  //弹窗显隐控制
let viewShow=ref(false);
let viewUrl=ref('')
let title=ref('')
let owlNetDlg = ref(null);
let kgtype = ref('')
let structType = ref("");
let TitleData = ref([]);
let filtered_data=ref([]);
let groupedData=ref([]);
let domains=ref([]);
let treeData=ref([]);
let treeProps={
  children: 'children',
  label: 'label'
}
onMounted(() => {
  // structType=SI_get("structType")
  // if(isEmpty(structType)){
  //   structType="风电装备元结构树"
  // }

  // getTitleData(structType).then(r => {
  //   TitleData = r.data;
  //   console.log("Titledata",TitleData)
  //   filtered_data = TitleData.filter(item => item.kgdomain !== '测试域' && item.kgdomain !== '融合域')
  //       .map(item => {
  //         return {
  //           kgdomain: item.kgdomain,
  //           kgtype: item.kgtype,
  //           baseTypeId: item.baseTypeId
  //         }
  //       });
  //   console.log("filtereddata",filtered_data)
  //
  //   for (const item of filtered_data) {
  //     const domain = item.kgdomain;
  //     if (!(domain in groupedData)) {
  //       groupedData[domain] = [];
  //     }
  //     groupedData[domain].push(item);
  //     if (!domains.includes(domain)) {
  //       domains.push(domain);
  //     }
  //   }
  //   for(let domainItem of domains) {
  //     let children = groupedData[domainItem].map(item => ({
  //       label: `${item.kgtype} 本体 以及 数据`,
  //       kgtype: item.kgtype,
  //       baseTypeId: item.baseTypeId,
  //       isDomain: false
  //     }));
  //     treeData.push({
  //       label: domainItem,
  //       children: children,
  //       isDomain: true
  //     });
  //   }
  //   console.log("domains",domains)
  //   console.log("groupeddata",groupedData)
  // });
  // 组件被挂载后的逻辑
});
// Add this function to the script setup
function toggleLeftPane() {
  leftPaneVisible.value = !leftPaneVisible.value;
}
function isEmpty(obj){
  if(typeof obj == "undefined" || obj == null || obj == ""){
    return true;
  }else{
    return false;
  }
}
const showData = function (kgType, baseTypeId) {
  visible = true;
  viewShow = true;
  viewUrl ="/embed/graphVis?kgType=" + kgType + "&baseTypeId=" + baseTypeId;
  title = "图谱数据查看";
};
const openNetDlg = function (typename) {
  console.log("typenmae12",typename)
  owlNetDlg.init2(typename);
};
</script>

<style scoped>
.container {
  display: flex;
  height: 100%;
}
.link
{
  font-family: "宋体";
  font-weight:600;
}

.left-pane {
  padding-left: 2px;
  background-color:  #F5F5F7FF;
  flex: 2;
  width: 35%;
  height: 100%;
  /*border-right: 2px solid gray;*/
  /*padding-right: 10px;*/
}
/* Add this to your scoped styles */
.toggle-btn {
  font-family: "宋体";
  font-weight: 400;
  background-color: white;
  border: 1px solid gray;
  border-radius: 5px;
  padding: 5px 10px;
  cursor: pointer;
  margin: 10px;
  display: inline-block;
}
.right-pane {
  flex: 8;
  width: 65%;
  height: 100%;
  /*padding-left: 10px;*/
}
h1 {
  color: red;
  padding-top: 24px;
  font-size: 25px; /* 大字体 */
  text-align: center; /* 左对齐 */
  margin-bottom: 10px; /* 和下一个元素之间的距离 */
}

h2 {
  font-family: "宋体";
  padding-top: 10px;
  font-size: 17px; /* 较小的字体 */
  line-height: 1.7; /* 行间距设置为字体大小的1.5倍 */
  padding-bottom: 20px;
  font-weight: 400;
  text-indent: 2em;
}
h3 {
  padding-top: 10px;
  font-size: 17px; /* 较小的字体 */
  line-height: 1.3; /* 行间距设置为字体大小的1.5倍 */
  padding-bottom: 20px;
  font-weight: 600;
  /*background-color:#ADD8E6 ;*/
}
.tree{
  font-weight: bold;
  font-family: "宋体";

}
.spanclass
{

  font-weight: 400;
  /*font-weight: bold;*/
  font-family: "宋体";
}

</style>
