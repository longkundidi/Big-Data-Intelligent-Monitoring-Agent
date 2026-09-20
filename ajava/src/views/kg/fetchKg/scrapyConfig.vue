<template>
  <basic-container>
  <el-container>
    <el-header class="top-page">
      <span class="title">知识获取管理->外部知识获取</span>
      <div style="margin-left:50px;float: right">
        <el-form :inline="true" @submit.prevent>
          <el-form-item label="">
            <el-input
                style="height: 27px"
                placeholder="请输入关键词"
                v-model="dataForm.key"
            ></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-button
                class="viewBtn"
                @click="renderTable()"
            >查询
            </el-button>
          </el-form-item>
          <el-form-item label="">
            <el-button
                class="reSetBtn"
                @click="renderTable2()"
            >重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-header>
    <el-main>
      <div class="main-frametype" style="">
        <div style="background: white; padding: 10px">
          <div style="float: right;padding-right: 30px">
          </div>
          <div class="select">
            <el-button  class="viewBtn" size="small"
                       style="float:right;margin-right: 30px;margin-bottom: 10px;"
                       @click="goScrapyKg('patent')">
              专利查看
            </el-button>
            <el-button  class="viewBtn" size="small"
                       style="float: right;margin-right: 45px;margin-bottom: 10px;"
                       @click="goScrapyKg('qikan')">期刊查看
            </el-button>
            <el-button
                class="addBtn"
                style="float: right;margin-right: 60px;margin-bottom: 10px;"
                size="small"
                @click="openDlgInsert(0)"
            >新增关键词
            </el-button>
          </div>
          <el-table
              :data="tableData"
              border
              style="width: 100%;margin-top: 20px"
              :header-cell-class-name="getRowClass"
              :cell-class-name="cellStyle"
              :row-style="{ height: '50px' }">
            <div v-for="data in this.tableData" :key="data" class="model_div">{{ data.keyword }}</div>
            <!--表格-->
            <el-table-column width="60px" align="center" label="序号" type="index">
            </el-table-column>
            <el-table-column prop="keyword" align="center" label="关键词">
            </el-table-column>
            <el-table-column align="center" label="操作">
              <template #default="scope">
                <el-button
                    class="editBtn"
                    size="small"
                    @click="openDlgUpdate(1,scope.row.id)"
                >
                  编辑
                </el-button>
                <el-button
                    v-if="scope.row.isori !== 1"
                    class="delBtn"
                    size="small"
                    @click="del(scope.row.id)"
                >
                  删除
                </el-button>
              </template>
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
          <au-dlg ref="auDlg"  @afterCommit="afterCommit"></au-dlg>
          <owl-net-dlg ref="owlNetDlg"></owl-net-dlg>
        </div>
      </div>
    </el-main>
  </el-container>
  </basic-container>
</template>
<script>
import {ElMessage, ElMessageBox} from "element-plus";
import {deleteById, getKeywordsTableData} from "@/api/kg/fetchKg/patent";
import AuDlg from "./scrapyconfig-add-or-update.vue";
import {createRouter as $router, useRouter} from "vue-router";
import swTree from "components/myComponent/swTree.vue";

export default {
  components: {
    swTree,
    AuDlg
  },
  data() {
    return {
      dataForm: {key: ""},
      struct: '',
      tableData: '',
      page: '1',
      size: '10',
      total: '10',
      auDlg: null,
      owlNetDlg: null,
    }
  },
  mounted() {
    this.renderTable()
  },
  methods: {
    //获取表格数据
    renderTable() {
      this.struct = null;
      if (!!this.dataForm.key) {
        this.struct = this.dataForm.key
      }
      getKeywordsTableData(this.page, this.size, this.struct).then(r => {
        r = r.data
        this.tableData = r.data.records;
        this.total = r.data.total;
        this.page = r.data.current;
        this.size = r.data.size
      })
    },
    renderTable2() {
      this.dataForm.key = "";
      getKeywordsTableData(this.page, this.size, this.struct).then(r => {
        this.tableData = r.data.records;
        this.total = r.data.total;
        this.page = r.data.current;
        this.size = r.data.size
      })
    },
    // 翻页
    handleCurrentChange(current) {
      this.page = current;
      this.renderTable();
    },
    // 重置页面大小，每次重置会回到第一页
    handleSizeChange(newSize) {
      this.page = 1;
      this.size = newSize;
      this.renderTable();
    },
    // 删除
    del(id) {
      ElMessageBox.confirm("此操作将永久删除该数据, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
          .then(() => {
            deleteById(id)
                .then(() => {
                  this.renderTable();
                  ElMessage({
                    type: "success",
                    message: "删除成功!",
                  });
                })
                .catch(() => {
                  ElMessage({
                    type: "info",
                    message: "删除失败",
                  });
                });
          })
          .catch(() => {
            ElMessage({
              type: "info",
              message: "已取消删除",
            });
          });
    },
    // 定义表头单元格的style的回调方法
    getRowClass(row, rowIndex) {
      return "theadStyle";
    },
    // 绑定样式属性
    cellStyle(row, rowIndex) {
      return "tdStyle";
    },
    // 打开弹窗
    //通过id号进行更新操作
    openDlgUpdate(type, id) {
      this.$refs.auDlg.init(type, id);
    },
    openDlgInsert(type, id) {
      this.$refs.auDlg.init(type, id);
    },
    afterCommit() {
      this.renderTable();
    },
    goScrapyKg(item) {
      this.$router.push({
        path: './scrapyKg',
        query: {
          kgType: item,
        }
      });
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
}
.el-pagination {
  margin-top: 30px;
}
.select {
  margin: 20px;
  position: relative;
}
</style>
