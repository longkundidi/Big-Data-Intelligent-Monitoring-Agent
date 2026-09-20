<template>
  <div>
    <basic-container>
      <div class="extra">
        <el-container>
          <el-header class="title">
            知识获取管理->非结构化数据抽取
          </el-header>
          <Steps v-model:activeChange="active" class="steps"></Steps>
          <el-divider/>
<!--          <el-button @click="checkFiles"></el-button>-->
          <el-main>
            <el-upload
                action="/api/kg/files/uploadFile"
                class="upload-demo"
                drag
                multiple
                :limit="1"
                :on-success="handleAvatarSuccess"
            >
              <el-icon class="el-icon--upload">
                <upload-filled/>
              </el-icon>
              <div class="el-upload__text">
                拖动文件或<em>点击此处</em>上传
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  json/txt files with a size less than 500kb
                </div>
              </template>
            </el-upload>
          </el-main>
        </el-container>
        <viewKgFileCompent :showFileUrl="fileUrl" v-model:kkfileViewShowProp="show"></viewKgFileCompent>
      </div>
    </basic-container>
  </div>

</template>
<script setup>
import {ref} from "vue";
import {UploadFilled} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";
import Steps from "@/views/kg/kgExtract/featureExtract/NerSteps.vue";
import viewKgFileCompent from "@/components/kkFileView/kkFileCompent.vue"
import {useRouter} from "vue-router";
import {minioUrl} from "@/config/env";
let active = ref(1)
const $router = useRouter()
let fileUrl = ref('')
let show = ref(false)
const handleAvatarSuccess = function (response, uploadFile){
  if(response.code===0){
    ElMessage.success("上传成功!!")
    let fileUrl = response.data
    $router.push({
      path: `./nerResult`,
      query: { fileUrl: fileUrl }
    })
  }
}
const checkFiles = function (){
  fileUrl.value = minioUrl + 'kgfile/7fa17dd347da4ac09b78dae62990472c融合实体类别信息的知识图谱表示学习.pdf'
  show.value = true
}
</script>


<style lang="scss" scoped>
.title {
  font-size: 20px;
  font-weight: bold;
}

</style>
