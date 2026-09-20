<template>
  <div class="execution">
    <basic-container>
      <div class="extra">
        <el-container>
          <el-header>
          </el-header>
          <el-main>
            <div class="index-container" >
              <img
                  src="@/assets/kg/kg.png"
                  class="icon"
              />
              <span class="icon-text">维修知识抽取</span>
            </div>
            <el-row justify="space-around" class="content-row">
              <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                <el-card class="box-card" shadow="hover">
                  <template #header>
                    <div class="card-header" >
                      <span style="font-size: 20px;font-weight: bolder">结构化知识抽取</span>
                    </div>
                  </template>
                  <div>
                    <el-descriptions
                        :column="1"
                        border
                    >
                      <el-descriptions-item>
                        <template #label>
                          <div class="cell-item" >
                            功能描述
                          </div>
                        </template>
                        提取并关联具体实体和关系，以构建精细关联的知识图谱，便于理解复杂的维修过程和逻辑。
                      </el-descriptions-item>
                    </el-descriptions>
                    <el-descriptions
                        :column="1"
                        border
                    >
                      <el-descriptions-item>
                        <template #label>
                          <div class="cell-item" >操作流程</div>
                        </template>
                        连接数据库 —> 关系表与实体映射  —> 关系知识数据抽取
                      </el-descriptions-item>
                    </el-descriptions>
                    <el-row style="margin-top: 10px">
                      <el-button class="normalBtn" style="margin-right: 10px"
                                 @click="goRelationExtra()">关系数据库抽取
                      </el-button>
                    </el-row>
                  </div>
                </el-card>
              </el-col>

              <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                <el-card class="box-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span style="font-size: 20px;font-weight: bolder">非结构化知识抽取</span>
                    </div>
                  </template>
                  <div>
                    <el-descriptions
                        :column="1"
                        border
                    >
                      <el-descriptions-item>
                        <template #label>
                          <div class="cell-item" >
                            功能描述
                          </div>
                        </template>
                        从宽泛的数据中提炼出关键概念和普遍模式，支持对维修领域的深入理解和广义上的知识管理。
                      </el-descriptions-item>
                    </el-descriptions>
                    <el-descriptions
                        :column="1"
                        border
                    >
                      <el-descriptions-item>
                        <template #label>
                          <div class="cell-item" >操作流程</div>
                        </template>
                         上传抽取文件 —> 查看抽取结果 —> 结果审核 —> 知识入库
                      </el-descriptions-item>
                    </el-descriptions>
                    <el-row style="margin-top: 10px">
                      <el-button class="normalBtn" style="margin-right: 10px"
                                 @click="goFeatureExtraction()">模型训练抽取
                      </el-button>
                    </el-row>
                  </div>
                </el-card>
              </el-col>

            </el-row>
          </el-main>
          <el-footer>
          </el-footer>
        </el-container>
      </div>
    </basic-container>
  </div>
</template>

<script>
export default {
  name: "kgExtract"
}
</script>
<script setup>
import {useRouter} from "vue-router";
const $router = useRouter()
const goRelationExtra = function (){
  $router.push({
    path: `../dbConnect`,
    query: {}
  })
}
const goFeatureExtraction = function (){
  $router.push({
    path: `../featureExtraction`,
    query: {}
  })
}
</script>

<style scoped lang="scss">
.index-container{
  flex-direction: column;
  display: flex;
  align-items: center;
  justify-content: center;
  .icon {
    height: 80px;
  }
  .icon-text {
    font-family:"华文行楷",emoji;
    font-size: 40px!important;
    font-weight: bold;
    color: #606266;
    margin-top: 10px;
  }
}
.content-row{
  margin-top: 75px;
}

.box-card {
  background-color: white;
  height: 95%;
  margin-left: 20px;
  margin-top: 10px;
  margin-right: 10px;
  border-width: 1px;
  border-color: black;
  :deep .el-descriptions__label{
    width: 65px;
    height: 80px;
  }
}
.cell-item {
  font-size: 16px;
  color: #1e080b;
  align-items: center;
  justify-content: center;
  display: flex;
}


</style>
