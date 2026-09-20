<template>
  <basic-container>
    <div class="extra">
      <el-container>
        <el-header>
          <div class="title">知识获取管理->结构化数据抽取</div>
        </el-header>
        <el-main>
          <Steps v-model:activeChange="active" class="steps"></Steps>
          <el-divider/>
          <div style="margin-left: 10px ;float: right; margin-bottom: 10px">
            <el-button class="normalBtn" @click="$router.push({path: './dbConnect'})">返回</el-button>
            <el-button class="normalBtn" @click="doCypherAll">执行语句</el-button>
          </div>
          <div>
            <h4>节点Cpyher语句</h4>
            <el-table
                :data="nodeCypher"
                :row-class-name="tableRowClassName"
                style="width: 100%"
            >
              <el-table-column label="cpyher语句" prop="value"/>
              <el-table-column fixed="right" label="操作" width="180">
                <template #default="scope">
                  <el-button
                      size="small"
                      class="editBtn"
                      @click.prevent="editCypher(scope.row)"
                  >修改
                  </el-button>
<!--                  <el-button-->
<!--                      size="small"-->
<!--                      class="printBtn"-->
<!--                      @click.prevent="doCypher(scope.row)"-->
<!--                  >执行-->
<!--                  </el-button>-->
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div>
            <h4>关系Cpyher语句</h4>
            <el-table
                :data="relationCypher"
                :row-class-name="tableRowClassName"
                style="width: 100%"
            >
              <el-table-column label="cpyher语句" prop="value"/>
              >
              <el-table-column fixed="right" label="操作" width="180">
                <template #default="scope">
                  <el-button
                      size="small"
                      class="editBtn"
                      @click.prevent="editCypher(scope.row)"
                  >修改
                  </el-button>
<!--                  <el-button-->
<!--                      size="small"-->
<!--                      class="printBtn"-->
<!--                      @click.prevent="doCypher(scope.row)"-->
<!--                  >执行-->
<!--                  </el-button>-->
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-divider/>

        </el-main>
      </el-container>
    </div>
  </basic-container>

  <el-dialog
      v-model="centerDialogVisible"
      title="修改"
      width="500"
      align-center
  >
    <el-form :model="formCypher" label-width="auto" style="max-width: 600px">
      <el-form-item label="Cypher语句">
        <el-input v-model="formCypher.value" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="centerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>


  <el-dialog
      v-model="dialogVisible"
      align-center
  >
    <div>
      <el-card>
        <h3>插入数据统计信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <h4>节点统计</h4>
              <el-table :data="nodeStats" style="width: 100%">
                <el-table-column prop="type" label="类型" width="180"></el-table-column>
                <el-table-column prop="count" label="数量"></el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <h4>关系统计</h4>
              <el-table :data="relationshipStats" style="width: 100%">
                <el-table-column prop="type" label="类型" width="180"></el-table-column>
                <el-table-column prop="count" label="数量"></el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
<!--      <el-card>-->
<!--        <h3>节点详细信息</h3>-->
<!--        <el-table :data="nodes" style="width: 100%">-->
<!--          <el-table-column prop="types" label="类型" width="180"></el-table-column>-->
<!--          <el-table-column prop="name" label="名称"></el-table-column>-->
<!--        </el-table>-->
<!--      </el-card>-->
<!--      <el-card>-->
<!--        <h3>关系详细信息</h3>-->
<!--        <el-table :data="relationshipDetails" style="width: 100%">-->
<!--          <el-table-column prop="type" label="类型" width="180"></el-table-column>-->
<!--          <el-table-column prop="startName" label="起始节点名称"></el-table-column>-->
<!--          <el-table-column prop="endName" label="结束节点名称"></el-table-column>-->
<!--        </el-table>-->
<!--      </el-card>-->
    </div>
  </el-dialog>
</template>

<script setup>
import {onMounted, ref} from "vue";
import Steps from "./CustomSteps.vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import store from '@/store'
import {
  dbConnect,
  doCypherAllPost,
  doCypherPost,
  getOntologyMapping,
  getTableView
} from "@/api/kg/knowledge-extra/stru-extra/d2r/db";
const dialogVisible = ref(false)
const formCypher = ref({})
const cypher = ref({})
const $router = useRouter()
const nodeCypher = ref([])
const relationCypher = ref([])
let active = ref(3)
let selectedTableInfo = ref(store.getters.d2rSelectedTableInfo)
const d2rDBInfo = store.getters.d2rDBInfo
onMounted(async () => {
  await getCypher()
});
const centerDialogVisible = ref(false)
const getCypher = async function () {
  try {
    let res = await getOntologyMapping(selectedTableInfo.value, d2rDBInfo.name)
    if (res.data.code === 0) {
      ElMessage.success("转换成功！")
      cypher.value = res.data.data
      nodeCypher.value = cypher.value.nodes
      relationCypher.value = cypher.value.relations
      nodeCypher.value = nodeCypher.value.map(item => {
        return {value: item}
      })
      relationCypher.value = relationCypher.value.map(item => {
        return {value: item}
      })
    }
  } catch (e) {
    ElMessage.info("尝试重新连接数据源")
    let r = await dbConnect(d2rDBInfo)
    if (r.data.code === 0) {
      ElMessage.success(r.data.msg);
    }
  }
}
const nodes = ref([])
const relationships = ref([])
const relationshipStats = ref([])
const nodeStats = ref([])
const relationshipDetails = ref([])
const calculateStats = function (datas) {
  nodes.value = datas["nodes"]
  relationships.value = datas["relationships"]
  const nodeTypeMap = {};
  nodes.value.forEach(node => {
    if (!nodeTypeMap[node.types]) {
      nodeTypeMap[node.types] = { count: 0 };
    }
    nodeTypeMap[node.types].count++;
  });
  nodeStats.value = Object.keys(nodeTypeMap).map(type => ({
    type,
    count: nodeTypeMap[type].count,
  }));
  // 节点统计
  const relationshipTypeMap = {};
  relationships.value.forEach(rel => {
    if (!relationshipTypeMap[rel.r._type]) {
      relationshipTypeMap[rel.r._type] = { count: 0 };
    }
    relationshipTypeMap[rel.r._type].count++;
  });
  relationshipStats.value = Object.keys(relationshipTypeMap).map(type => ({
    type,
    count: relationshipTypeMap[type].count,
  }));

  // 关系详细信息
  relationshipDetails.value = relationships.value.map(rel => ({
    type: rel.r._type,
    startName: rel.m.name.trim(),
    endName: rel.n.name.trim(),
  }));
}
const doCypherAll = async function () {
  let res = await doCypherAllPost({
    nodeCypher:nodeCypher.value,
    relationCypher:relationCypher.value
  });
  if (res.data.code === 0) {
    console.log(res.data.data)
    ElMessage.success("插入成功")
    let datas = res.data.data
    dialogVisible.value = true
    calculateStats(datas)
  }else {
    ElMessage.warning(res.data.msg)
  }
}
const selectedRow = ref(null)
const editCypher = function (row ){
  centerDialogVisible.value = true
  selectedRow.value = row
  formCypher.value = {...row}
}
const confirmEdit = function (){
  centerDialogVisible.value = false
  Object.assign(selectedRow.value, formCypher.value);
}
const tableRowClassName = function (row,rowIndex){
  if (rowIndex === 1) {
    return 'warning-row'
  } else if (rowIndex === 3) {
    return 'success-row'
  }
  return ''
}

const doCypher = async function (row) {
  let res = await doCypherPost(row.value);
  if (res.data.code===0){
    console.log(res.data.data)
  }
}

</script>

<style lang="scss" scoped>
.extra {
  .title {
    font-size: 20px;
    font-weight: bold;
  }

  .top-page {
    display: flex;
    flex-direction: row;
    justify-content: flex-start; /* 内容靠左 */
    align-items: center; /* 垂直居中 */
  }

  .top-text {
    color: #070707;
    font-size: 22px;
    font-weight: bold;
    position: relative;
  }

  .pagination {
    margin-top: 30px;
  }

  .inner-dialog {
    display: flex;
    flex-direction: column; /* 垂直堆叠子元素 */
    .dialog-button {
      margin-top: 10px;
      margin-bottom: 5px;
      align-self: flex-end; /* 对齐按钮到右边 */
    }
  }
}
</style>
