<template>
  <div>
    <basic-container>
      <div class="extra">
        <el-container>
          <el-header class="title">
            知识获取管理->结构化数据抽取
          </el-header>
          <el-main style="height: 100%">
            <Steps v-model:activeChange="active" class="steps">
            </Steps>
            <el-divider/>
            <div style="margin-left: 10px ;float: right">
              <el-button class="normalBtn" size="small" @click="previous">上一步</el-button>
              <el-button class="normalBtn" size="small" @click="deleteAll">清除全部</el-button>
              <el-button class="normalBtn" size="small" @click="next">下一步</el-button>
            </div>
            <el-table
                ref="multipleTableRef"
                :data="allTableData"
                :row-key="row => row.tableName"
                border stripe
                style="width: 100%"
            >
              <el-table-column align="center" label="表名称" prop="tableName" style="width: 50px">
              </el-table-column>
              <el-table-column align="center" label="表备注" prop="tableComment">
              </el-table-column>
              <el-table-column align="center" label="对应实体或关系" width="300">
                <template #default="scope">
                  <span v-if="!(scope.row.tag)" style="color: red">未映射
                  </span>
                  <span v-else style="color: #67c23a">
                    {{ scope.row.target }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column align="center" label="状态" width="150">
                <template #default="scope">
                  <el-tag
                      :type="scope.row.tag ? 'success' : 'danger'"
                      disable-transitions
                  >{{ scope.row.tag ? '已映射' : '未映射' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column align="center" label="操作">
                <template #default="scope">
                  <el-button size="small" type="success"
                             @click="handleMapping(scope.row)"
                  >映射
                  </el-button>
                  <el-button size="small" type="danger"
                             @click="handleDeleteMap(scope.row)"
                  >清除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="ontology-show">
              <ontology-show></ontology-show>
            </div>
          </el-main>
        </el-container>
        <el-dialog v-model="dialogTableVisible"
                   :before-close="handleClose"
                   :close-on-click-modal="false"
                   destroy-on-close
                   width="800">
          <div class="inner-dialog">
            <el-descriptions :column="3" border direction="vertical" title="当前表信息">
              <el-descriptions-item label="表名称">{{ curTableInfo.tableName }}</el-descriptions-item>
              <el-descriptions-item label="表备注">{{ curTableInfo.tableComment }}</el-descriptions-item>
              <el-descriptions-item label="表对应类别(实体/关系表)">
                <el-cascader
                    v-model="curTableInfo.selectValue"
                    :options="options"
                    @change="getColsInflection(curTableInfo.selectValue)"
                />
              </el-descriptions-item>

            </el-descriptions>
            <el-divider border-style="dashed"/>
            <!--          <h4>主键信息</h4>-->
            <!--          <el-table :data="curTableInfo.primaryKeys" stripe style="width: 100%">-->
            <!--            <el-table-column prop="pkName" label="主键名称" />-->
            <!--            <el-table-column prop="columnName" label="字段" />-->
            <!--          </el-table>-->
            <h4>外键信息</h4>
            <el-table :data="curTableInfo.foreignKeys" stripe style="width: 100%">
              <el-table-column label="外键名称" prop="fkName" width="180"/>
              <el-table-column label="字段" prop="columnName" width="180"/>
              <el-table-column label="参考表" prop="refTableName" width="180"/>
              <el-table-column label="参考字段" prop="refColumnName"/>
            </el-table>
            <h4>字段信息</h4>
            <el-table :data="curTableInfo.fields" stripe style="width: 100%">
              <el-table-column label="字段" prop="columnName"/>
              <el-table-column label="字段注释" prop="columnComment"/>
              <el-table-column label="字段类型" prop="dataType"/>
              <el-table-column label="键类型">
                <template #default="scope">
                  <el-select
                      v-model="scope.row.keyType"
                      placeholder="请选择"
                      style="width: 80%"
                  >
                    <el-option
                        v-for="item in isForeignKeyOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="属性映射" >
                <template #default="scope">
                  <el-select v-if="scope.row.keyType===0"
                             v-model="scope.row.attributeMapping"
                             clearable
                             placeholder="Select"
                             style="width: 80%"
                  >
                    <el-option
                        v-for="item in curAttributes"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                  </el-select>
                  <el-cascader v-if="scope.row.keyType===1"
                               v-model="scope.row.attributeMapping"
                               :options="curAttributesOptions"
                               clearable
                  />
                  <el-input v-model="scope.row.nodeId" v-if="scope.row.keyType===2"  size="mini" @input="() => $forceUpdate()" placeholder="请输入内容"></el-input>
                </template>
              </el-table-column>
            </el-table>
            <div class="dialog-button">
              <el-button class="normalBtn" @click="confirmSelected">确定</el-button>
            </div>
          </div>
        </el-dialog>
      </div>
    </basic-container>
  </div>

</template>
<script setup>
import {onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import Steps from "@/views/kg/kgExtract/relationExtract/d2r/CustomSteps.vue";
import store from "@/store";
import {
  dbConnect,
  getOntologyInfos,
  getOntologyNodes,
  getTableView,
} from "@/api/kg/knowledge-extra/stru-extra/d2r/db";
import {ElMessage, ElMessageBox} from "element-plus";
import OntologyShow from "components/kg/ontologyShow.vue";


const isForeignKeyOptions = ref([
  {
    value: 0,
    label: '属性键',
  },
  {
    value: 1,
    label: '关系键',
  },
  {
    value: 2,
    label: '结构键',
  }

])
const options = ref(null)
let page = ref(1);
let size = ref(10);
let total = ref(100);
let active = ref(2);
let tableData = ref([]);
let dialogTableVisible = ref(false);
let allTableData = ref([]);
let curTableInfo = ref({})
const structNodeName = ref("元部套件模型")
const multipleTableRef = ref(null)
const ontologyNodeData = ref([])
const ontologyRelationData = ref([])
const ontologyName = "维修知识本体";
const $route = useRoute();
const $router = useRouter();
const d2rDBInfo = store.getters.d2rDBInfo
const selectedTableInfo = ref([]);
const curAttributes = ref([])
const curAttributesOptions = ref()

onMounted(async () => {
  allTableData.value = store.getters.d2rSelectedTable
  selectedTableInfo.value = store.getters.d2rSelectedTableInfo
  total.value = allTableData.value.length
  getTableData()
  ontologyRelationData.value = await getOntologyInfo()
  ontologyNodeData.value = await getOntologyNodeInfo()
  handleSelectShow()
});
const previous = function () {
  $router.push({path: './dbConnect'})

}
const deleteAll = function () {
  ElMessageBox.confirm(
      '确定要删除所有映射关系吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(async () => {
        selectedTableInfo.value = []
        allTableData.value.forEach(item => {
          item.tag = 0;
          item.target = undefined;
        })
        store.commit('SET_D2RSelectedTB', allTableData.value)
        store.commit('SET_D2RSelectedTableInfo', selectedTableInfo.value)
      })
}


const getTableData = function () {
  tableData.value = allTableData.value.slice(
      (page.value - 1) * size.value, page.value * size.value
  )
};


// 重置页面大小，每次重置会回到第一页
const handleMapping = async function (row) {
  dialogTableVisible.value = true
  const tableInfo = selectedTableInfo.value.find(info => info.tableName === row.tableName);
  if (tableInfo) {
    curTableInfo.value = tableInfo;
    getColsInflection(curTableInfo.value.selectValue)
  } else {
    curTableInfo.value = await getTableViewInfo(d2rDBInfo.name, row.tableName);
  }
};

/*生成选择表对应类别(实体表或关系表)的数据*/
const handleSelectShow = function () {
  let relations = ontologyRelationData.value.map(item => ({
    value: `${item.relation.sourceName}-${item.relation.relationName}-${item.relation.targetName}`,
    label: `${item.relation.sourceName}-${item.relation.relationName}-${item.relation.targetName}`
  }));
  let nodes = ontologyNodeData.value.map(item => ({
    value: item.name,
    label: item.name
  }));
  options.value = [
    {
      value: 'nodes',
      label: '实体表',
      children: nodes
    },
    {
      value: 'relations',
      label: '关系表',
      children: relations
    }
  ]
}
const next = function () {
  $router.push({
    path: "./showCypher"
  })
}

const getColsInflection = function (selectValue) {
  curAttributes.value = []
  let attributes = ontologyNodeData.value
      .filter(item => item.name === selectValue[selectValue.length - 1])
      .map(item => item.attributes);
  attributes.forEach(item => {
    item.forEach(attr => {
      if (attr !== "") {
        curAttributes.value.push({label: attr, value: attr})
      }
    })
  })
  curAttributes.value.push({label: "id", value: "id"}, {label: "name", value: "name"});
  curAttributesOptions.value = ontologyNodeData.value.map(item => {
    return {
      value: item.name,
      label: item.name,
      children: [
        {label: "id", value: "id"},
        {label: "name", value: "name"},
        ...item.attributes
            .filter(attr => attr !== "") // 过滤掉空字符串属性
            .map(attr => {
              return {
                value: attr,
                label: attr
              }
            })
      ]
    }
  });
}

const getTableViewInfo = async function (dbName, tableName) {
  try {
    let response = await getTableView(dbName, tableName)
    if (response.data.code === 0) {
      return response.data.data
    } else {
      return null
    }
  } catch (e) {
    ElMessage.info("尝试重新连接")
    let r = await dbConnect(d2rDBInfo)
    if (r.data.code === 0) {
      ElMessage.success(r.data.msg + " 请重新选择");
    }
    return null
  }
}
const getOntologyInfo = async function () {
  try {
    let response = await getOntologyInfos(ontologyName)
    if (response.data.code === 0) {
      return response.data.data
    } else {
      return null
    }
  } catch (e) {
    ElMessage.error(e)
  }
}
const getOntologyNodeInfo = async function () {
  try {
    let response = await getOntologyNodes(ontologyName)
    if (response.data.code === 0) {
      return response.data.data
    } else {
      return null
    }
  } catch (e) {
    ElMessage.error(e)
  }
}

const handleClose = function (done) {
  curTableInfo.value = {}
  done()
};

const confirmSelected = function () {
  if (curTableInfo.value.selectValue === undefined || curTableInfo.value.selectValue.length <= 0) {
    ElMessage.warning("请选择对应的本体映射")
    return
  }
  let undoMappings = curTableInfo.value.fields
      .filter(item => item.keyType!==2 && (item.attributeMapping === undefined || item.attributeMapping===null) )
      .map(item => item.columnName);

  function continueAction() {
    selectedTableInfo.value = selectedTableInfo.value.filter(item => item.tableName !== curTableInfo.value.tableName);
    selectedTableInfo.value.push(curTableInfo.value);
    dialogTableVisible.value = !dialogTableVisible.value
    for (const tableInfo of allTableData.value) {
      if (tableInfo.tableName === curTableInfo.value.tableName) {
        tableInfo.tag = 1;
        tableInfo.target = curTableInfo.value.selectValue[1];
      }
    }
    store.commit('SET_D2RSelectedTB', allTableData.value)
    store.commit('SET_D2RSelectedTableInfo', selectedTableInfo.value)
  }
  if (undoMappings.length > 0) {
    ElMessageBox.confirm(
        `有${undoMappings.join(', ')}字段没有映射. 是否继续?`,
        'Warning',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )
        .then(() => {
          continueAction();
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '确认失败',
          })
        })
  } else {
    continueAction();
  }

};

const handleDeleteMap = function (row) {
  row.tag = 0;
  row.target = undefined;
  selectedTableInfo.value = selectedTableInfo.value.filter(item => item.tableName !== row.tableName)
  store.commit('SET_D2RSelectedTB', allTableData.value)
  store.commit('SET_D2RSelectedTableInfo', selectedTableInfo.value)
};

</script>


<style lang="scss" scoped>

.ontology-show {
  width: 90%;
  height: 90%;
  overflow: hidden
}

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
