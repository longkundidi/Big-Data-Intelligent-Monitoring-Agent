<template>
  <div>
    <basic-container>
      <div class="extra">
        <el-container>
          <el-header>
            <div class="title">知识获取管理->结构化数据抽取</div>
          </el-header>

          <el-main>
            <Steps v-model:activeChange="active" class="steps"></Steps>
            <el-divider/>
            <div>
              <el-form ref="form"
                       :label-position="'left'"
                       :model="dbInfo"
                       label-width="auto">
                <el-row  justify="center">
                  <el-col :span="8">
                    <el-form-item label="连接名称">
                      <el-input v-model="dbInfo.name" style="width: 230px"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="数据库名称">
                      <el-input v-model="dbInfo.dataSource" style="width: 230px"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row  justify="center">
                  <el-col :span="8">
                    <el-form-item label="数据库类型">
                      <el-select v-model="dbInfo.dataSourceType" placeholder="Select" style="width: 230px">
                        <el-option
                            v-for="item in dataSourceTypes"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="连接地址">
                      <el-input v-model="dbInfo.ip" style="width: 230px"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row  justify="center">
                  <el-col :span="8">
                    <el-form-item label="用户名">
                      <el-input v-model="dbInfo.sourceName" style="width: 230px"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="密码">
                      <el-input v-model="dbInfo.sourcePassword" style="width: 230px" type="password"></el-input>
                    </el-form-item>
                  </el-col>

                </el-row>
                <el-row >
                  <el-col :offset="4" :span="8">
                    <el-form-item label="端口号">
                      <el-input v-model="dbInfo.port" style="width: 230px"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
            <el-divider/>
            <div class="form-button">
              <el-button class="normalBtn" @click="connection">下一步：连接</el-button>
            </div>
          </el-main>

        </el-container>
      </div>
    </basic-container>

    <el-dialog v-model="dialogTableVisible"
               :close-on-click-modal="false"
               center
               destroy-on-close
               :before-close="handleClose"
               title="选择需要的映射的关系表"
               width="800">
      <div class="inner-dialog">
        <div class="dialog-transfer">
          <el-transfer
              v-model="selectedValue"
              :data="tableData"
              :filter-method="filterMethod"
              :titles="['待选择（全选）', '已选择（全选）']"
              filter-placeholder=""
              filterable
          />
        </div>
        <div class="dialog-button">
            <el-button :icon="Check" class="normalBtn" @click="confirmSelected">确定</el-button>
            <el-button :icon="Close" type="info" @click="closeDialog">
              取消
            </el-button>
        </div>
      </div>
    </el-dialog>
  </div>

</template>
<script setup>
import {ref} from "vue";
import Steps from "./CustomSteps.vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import store from '@/store'
import {dbConnect, tableInfo} from "@/api/kg/knowledge-extra/stru-extra/d2r/db";
import {Check, Close} from "@element-plus/icons-vue";
const $router = useRouter()
let active = ref(1)
let dialogTableVisible = ref(false)
let selectedValue = ref([])
let tableData = ref([])
let tableInfoList = ref([])
const generateData = function (states) {
  let data = []
  states.forEach((dbInfo, index) => {
    data.push({
      label: `${dbInfo.tableName}(${dbInfo.tableComment})`,
      key: dbInfo.tableName,
    })
  })
  return data
}
let dataSourceTypes = ref([
  {
    label: 'MySQL',
    value: 'mysql',

  },
  {
    label: 'Oracle',
    value: 'oracle',
  },
  {
    label: 'PostgreSQL',
    value: 'postgresql',
  },
])
let dbInfo = ref({
  name: 'KG',
  port: "3306",
  driverClassName: 'com.mysql.cj.jdbc.Driver',
  dataSourceType: 'mysql',
  //不一定需要改
  ip: "192.168.65.59",
  dataSource: "fault_d2r",
  sourceUrl: 'jdbc:mysql://192.168.16.216:3306/knowledge-graph-five?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&allowMultiQueries=true',
  sourceName: 'root',
  sourcePassword: 'root',
});

const connection = async function () {
  try {
    dbInfoChange()
    let r = await dbConnect(dbInfo.value); // 使用 await 等待异步操作的结果,注意不要使用ref对象，得传递value
    if (r.data.code === 0) {
      ElMessage.success(r.data.msg);
      store.commit('SET_D2RDBINFO', dbInfo.value)
      try {
        let p = await tableInfo(
            {dbDatasourceName: dbInfo.value.name}
        )
        if (p.data.code === 0) {
          tableInfoList.value = p.data.data
          tableData = generateData(tableInfoList.value)
          dialogTableVisible.value = true
        }
      } catch (error) {
        dialogTableVisible.value = false
      }

    }
  } catch (error) {
    //采用R.failed()会拦截先输出msg，在抛出异常
  }
};

const dbInfoChange = function () {
  let {ip, port, dataSource} = dbInfo.value;
  switch (dbInfo.value.dataSourceType) {
    case 'oracle':
      dbInfo.value.driverClassName = 'oracle.jdbc.driver.OracleDriver';
      dbInfo.value.port = port || "1521";
      dbInfo.value.sourceUrl = `jdbc:oracle:thin:@${ip}:${port}:${dataSource}`;
      break;
    case 'postgresql':
      dbInfo.value.driverClassName = 'org.postgresql.Driver';
      dbInfo.value.port = port || "5432";
      dbInfo.value.sourceUrl = `jdbc:postgresql://${ip}:${port}/${dataSource}`;
      break;
    case 'mysql':
    default:
      dbInfo.value.driverClassName = 'com.mysql.cj.jdbc.Driver';
      dbInfo.value.port = port || "3306";
      dbInfo.value.sourceUrl = `jdbc:mysql://${ip}:${port}/${dataSource}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&allowMultiQueries=true`;
      break;
  }
}
const confirmSelected = function () {
  let d2rSelectedTable = []
  // d2rSelectedTB = tableInfoList.value.filter(tab => selectedValue.value.includes(tab.tableName));
  tableInfoList.value.forEach((tab) => {
        if (selectedValue.value.includes(tab.tableName)) {
          d2rSelectedTable.push(tab)
        }
      }
  )
  if(!d2rSelectedTable.length) {
    ElMessage.warning("请先选择映射的表！")
    return
  }
  store.commit('SET_D2RSelectedTB', d2rSelectedTable)
  store.commit('SET_D2RSelectedTableInfo', [])
  $router.push({
    path: `./tbMapping`,
    query: {}
  })
}
const closeDialog = function () {
  dialogTableVisible.value = !dialogTableVisible.value
}
const handleClose = function (done) {
  selectedValue.value = []
  tableData.value = []
  done()
}
const filterMethod = (query, item) => {
  return item.label.toLowerCase().includes(query.toLowerCase())
}
</script>

<style lang="scss" scoped>
.el-row {
  margin-top: 20px;
  margin-bottom: 20px;
}

.form-button {
  text-align: -webkit-center;
  margin-top: 50px;
  margin-bottom: 20px;
}
:deep .el-form-item__label{
  font-weight: bold;
}
.dialog-button {
  margin-top: 50px;
  margin-bottom: 20px;
}

.inner-dialog {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  justify-content: center; /* 垂直居中el-transfer */
  height: 100%; /* 容器高度填满对话框 */
  align-items: center;
}

.dialog-transfer {
  :deep .el-transfer-panel {
    width: 300px;
  }
}

.title {
  font-size: 20px;
  font-weight: bold;
}
</style>
