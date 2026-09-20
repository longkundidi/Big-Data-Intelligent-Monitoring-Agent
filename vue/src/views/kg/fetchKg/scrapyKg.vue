<template>
  <basic-container>
  <el-container style="display: flex; flex-direction: column" class="common-layout">
    <el-header class="top-page">
       <span class="title">
        知识总览服务->{{name}}知识查看
      </span>
      <div style="margin-left:50px;float: right">
        <el-form :inline="true">
          <el-form-item>
            <h3>当前结构:</h3>
          </el-form-item>
          <el-form-item label="">
            <el-input

                style="height: 27px"
                placeholder="请输入关键词"
                v-model="dataForm.key"
            ></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-button
                class="normalBtn"

                icon="el-icon-search"

                @click="renderTable()"
            >查询
            </el-button>
          </el-form-item>
          <el-form-item label="">
            <el-button
                class="normalBtn"

                icon="el-icon-refresh"

                @click="renderTable2()"
            >重置
            </el-button>
          </el-form-item>
          <el-form-item label="">
            <el-button
                type="info"


                icon="el-icon-back"
                @click="back">
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-header>
    <el-main style="margin-top: 0;padding: 0 10px;padding-bottom: 25px">
      <div class="top-text" >
        <div class = "select"   style="vertical-align: middle;margin-left: 20px;margin-right: 5px">
          <el-select v-model="value" placeholder="中国知网"  @change="selectChange">
            <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
                :disabled="item.disabled"
            />
          </el-select>
        </div>
      </div>
      <div class="main-frametype" style="">
        <div style="background: white; padding: 10px">
          <div style="float: right;padding-right: 30px">
          </div>
          <el-table
              :data="tableData"
              border
              style="width: 100%"
              :header-cell-class-name="getRowClass"
              :cell-class-name="cellStyle"
              :row-style="{ height: '50px' }"
          >
            <div v-for="data in tableData" :key="data" class="model_div">{{ data.strcture}}</div>

            <!--表格-->
            <el-table-column width="60px" align="center" label="序号" type="index">
            </el-table-column>
            <el-table-column prop="name" align="center" label="名称">
              <template #default="scope">
                <a
                    :href="scope.row.link"
                    target="_blank"
                    style="text-decoration: underline"
                >{{ scope.row.name }}</a
                >
              </template>
            </el-table-column>
            <el-table-column prop="autor" align="center" label="作者">
            </el-table-column>
            <el-table-column prop="abstracts" align="center" show-overflow-tooltip label="摘要">
            </el-table-column>
            <el-table-column prop="strcture" align="center" width="150px" label="结构">
            </el-table-column>
          </el-table>
          <!--分页-->
          <el-pagination
              background
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="page"
              :page-sizes="[10, 20, 30, 50]"
              :page-size="size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
          ></el-pagination>
        </div>
      </div>
    </el-main>
  </el-container>
  </basic-container>
</template>
<script>

import {getTableData} from "@/api/kg/fetchKg/scrapyKg";

export default {
  name:'scrapyKg',
  components: {

  },
  data() {
    return {
      dataForm: {key: ""},
      struct:'',
      tableData:[],
      page:1,
      size:10,
      total:10,
      type:'',
      name:'',
      source:'',
      value:'',
      options:[
        {
          value: 'cnki',
          label: '中国知网',
        },
        {
          value: 'vepsa',
          label: '维普中文',
        },
        {
          value: 'wangfan',
          label: '万方数据',
        },

      ]
    }
  },
  mounted() {
    this.type=this.$route.query.kgType
    if(this.type==='qikan') {
      this.name='期刊'
    }
    else{
      this.name='专利'
    }
    this.source="";//初始化时候不指定类型
    this.renderTable();
  },
  methods: {
    back(){
      this.$router.back(-1);
    },
    selectChange(val){
      this.source=val;
      this.page=1;
      this.renderTable();
    },
    renderTable() {
      this.struct = null;
      if (!!this.dataForm.key) {
        this.struct = this.dataForm.key
      }else {
        this.struct = ""
      }
      getTableData(this.page,this.size,this.type,this.source,this.struct).then(r => {
        r = r.data
        this.tableData = r.data.records;
        this.total = r.data.total;
        this.page = r.data.current;
        this.size = r.data.size
      })
    },
    renderTable2(){
      this.struct = ""
      this.source = ""
      this.dataForm.key = ""
      getTableData(this.page,this.size,this.type,this.source,this.struct).then(r => {
        this.tableData = r.data.records;
        this.total = r.data.total;
        this.page = r.data.current;
        this.size = r.data.size
      })
    },
    handleCurrentChange(current){
      this.page = current;
      this.renderTable();
    },
    handleSizeChange(newSize){
      this.page = 1;
      this.size = newSize;
      this.renderTable();
    },
    getRowClass(row, rowIndex){
      return "theadStyle";
    },
    cellStyle(row, rowIndex){
      return "tdStyle";
    }

  }

}

</script>

<style lang="scss" scoped>
.title {
  font-size: 20px;
  font-weight: bold;
}
.main-frametype {
  padding: 15px 20px;

  .formtop {
    display: flex;
    justify-content: space-between;
  }

  .p-btn-edit {
    border: 1px solid #6ca2f9;
    line-height: 16px;
    background: #0570cf;
    height: 16px;
    padding: 0 14px !important;
    color: #fff;

    &:hover {
      background: #0570cf;
      color: #fff;
    }
  }

  .p-btn-add {
    border: 1px solid #6ca2f9;
    line-height: 16px;
    background: #0570cf;
    height: 16px;
    padding: 0 14px !important;
    margin-bottom: 5px;
    color: #fff;

    &:hover {
      background: #0570cf;
      color: #fff;
    }
  }

  .p-btn-del {
    border: 1px solid #ff98a6;
    line-height: 16px;
    background: #cf4d75;
    height: 16px;
    padding: 0 14px !important;
    color: #fff;

    &:hover {
      background: #cf4d75;
      color: #fff;
    }
  }
  .el-pagination {
    margin-top: 30px;
  }
}
</style>
