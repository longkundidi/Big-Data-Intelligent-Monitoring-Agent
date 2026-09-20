<template>
  <div>
    <basic-container>
      <div class="extra">
        <el-container>
          <el-header class="title">
            知识获取管理->结构化数据抽取
          </el-header>
          <el-main>
            <el-table
                :data="tableData"
                stripe border
                style="width: 100%"

            >
              <div v-for="data in tableData" :key="data" class="model_div">{{ data.struct }}</div>
              <el-table-column align="center" label="知识域" prop="baseName" style="width: 50px">
              </el-table-column>
              <el-table-column align="center" label="知识类型" prop="typeName">
              </el-table-column>
              <el-table-column align="center" label="描述" prop="description" width="300">
              </el-table-column>
              <el-table-column align="center" label="创建时间" prop="updateTime" width="300">
              </el-table-column>
              <el-table-column
                  align="center"
                  header-align="center"
                  label="构建本体和数据"
                  min-width="100"
                  prop="tempFile"
              >
                <template #default="scope">
                  <el-button
                      @click="handleD2RClick(scope.row)"
                  >D2R
                  </el-button>
                  <el-button
                      @click="handleE2RClick(scope.row)"
                  >E2R
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-main>
          <el-footer>
            <div class="pagination">
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
          </el-footer>
        </el-container>
      </div>
    </basic-container>

    <el-dialog
        v-model="visibleWarn"
        :before-close="close"
        append-to-body
        style="display: block"
        title="警告"
        width="25%">
      <h4>上传的本体中存在本体字典中未包含的词汇：</h4>
      <el-checkbox
          v-model="checkAll"
          :indeterminate="isIndeterminate"
          @change="handleCheckAllChange">全选
      </el-checkbox>
      <el-checkbox-group
          v-model="checkedWords"
          @change="handleCheckedWordsChange"
      >
        <el-checkbox v-for="word in words" :key="word" :label="word">{{
            word
          }}
        </el-checkbox>
      </el-checkbox-group>
      <h4>请修改本体或将词汇导入本体字典！</h4>
      <div slot="footer"
           class="dialog-footer">
        <el-button class="cancelbtn"
                   @click="close()">取 消
        </el-button>
        <el-button class="determinebtn"
                   @click="submit()">导 入
        </el-button>
      </div>
    </el-dialog>
    <el-dialog
        v-model="visibleAttrInfo"
        append-to-body
        style="display: block"
        title="补充属性信息"
        width="60%">
      <el-table
          :cell-class-name="cellStyle"
          :data="attrData"
          :header-cell-class-name="getRowClass"
          :row-style="{ height: '100px' }"
          border="true"
          fit="true"
          stripe
          style="width: 100%"
      >
        <el-table-column align="center" label="节点名称" prop="nodeName" width="200">
        </el-table-column>
        <el-table-column align="center" label="属性名称" prop="attrName" width="200">
        </el-table-column>
        <el-table-column align="center" label="是否主属性" prop="isKey" width="300">
        </el-table-column>
        <el-table-column align="center" label="属性类型" prop="attrType">
          <template #default="scope">
            <el-select v-model="scope.row.attrType" placeholder="文本类型">
              <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer"
           class="dialog-footer">
        <el-button class="cancelbtn"
                   @click="closeAttrInfo()">取 消
        </el-button>
        <el-button class="determinebtn"
                   @click="submitAttrInfo()">确 定
        </el-button>
      </div>
    </el-dialog>
  </div>

</template>
<script>
export default {
  name: "d2r"
}
</script>

<script setup>
import {onMounted, ref} from "vue";
import {useRoute} from "vue-router";
let page = ref(1);
let size = ref(10);
let total = ref(100);
const $route = useRoute();

// region VUE相关
onMounted(() => {
  //得到左侧结构树
});


const handleCurrentChange = function (currentPage) {
  page.value = currentPage;
};

// 重置页面大小，每次重置会回到第一页
const handleSizeChange = function (newSize) {
  page.value = 1;
  size.value = newSize;
};
</script>

<style lang="scss" scoped>
.title{
  font-size: 20px;
  font-weight: bold;
}
.extra {
  .top-page {
    display: flex;
    flex-direction: row;
    justify-content: flex-start; /* 内容靠左 */
    align-items: center; /* 垂直居中 */
  }

  .pagination{
    margin-top: 30px;
  }

  .top-text {
    color: #070707;
    font-size: 22px;
    font-weight: bold;
    position: relative;
  }

}
</style>
