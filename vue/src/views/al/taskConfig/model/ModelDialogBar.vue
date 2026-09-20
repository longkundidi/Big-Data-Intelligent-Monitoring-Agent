<template>
  <el-dialog
      v-model="props.dialogVisible"
      append-to-body
      :close-on-click-modal="false"
      class="el-dialog__header"
      show-close
      width="35%"
      @close="handleClose"
  >

    <template #header>
      <div style="display: flex; padding-top: 15px; padding-bottom: 15px">
        <div style="width: 5%; display: flex;">
          <el-image src="arrow.png" fit="fill"></el-image>
        </div>
        <div style="font-size: 24px; display: flex; padding-left: 1%">
          <span>保存组态</span>
        </div>
      </div>
    </template>

    <el-form
        ref="dataFormRef"
        :model="dataForm"
        :rules="dataRule"
        label-width="110px"
        style="margin-right: 20px"
    >
      <el-row>
        <el-form-item label="多组态名称" prop="modelName">
          <el-input v-model="dataForm.modelName"
                    placeholder="请输入名称"
                    style="width: 500px"/>
        </el-form-item>
      </el-row>

      <el-row>
        <el-form-item label="针对对象">
          <el-tag type="success" style="margin-left: 3px;">
            {{ dataForm.modelObject }}
          </el-tag>
        </el-form-item>
      </el-row>
    </el-form>

    <template #footer>
      <el-button class="normalBtn" @click="handleClose">取消</el-button>
      <el-button class="normalBtn" @click="dataFormSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, nextTick, defineExpose} from 'vue';
import { ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElRow, ElTag, ElMessageBox, ElMessage, ElNotification } from 'element-plus';
import {exitModelConfigName, saveModelConfig} from "@/api/al/taskConfig";

const props = defineProps({
  dialogVisible: Boolean,
  nodesImage: String,
});
const emit = defineEmits(["update:dialogVisible"]);

const selectVisible = ref(false);
const modelSequence = ref('');
const modelType = ref('');
const modelVote = ref('');

// 数据表单
const dataForm = ref({
  modelName: '',
  modelObject: '',
  objectId: '',
});

const updateValues = (object, sequence, type, vote) => {
  dataForm.value.modelObject = object.modelObject
  dataForm.value.objectId = object.objectId

  modelSequence.value = sequence
  modelType.value = type
  modelVote.value = vote
}

defineExpose({
  updateValues
})

// 表单校验规则
const dataRule = ref({
  modelName: [{ required: true, message: '请输入组态名称', trigger: 'blur' }],
});

// 表单引用
const dataFormRef = ref(null);

const reset = () => {
  dataForm.value.modelName = ''
  dataForm.value.modelObject = ''
  dataForm.value.objectId = ''

  if (dataFormRef.value) {
    dataFormRef.value.resetFields(); // 清除校验状态和错误提示
  }
}

// 提交表单
const dataFormSubmit = () => {
  if (!dataForm.value.modelName) {
    ElMessage.warning('请输入组态名称！')
    return
  }

  exitModelConfigName(dataForm.value.modelName).then(res => {
    if (res.data.code === '200' && res.data.data === true){
      ElNotification.warning('该名称已存在!')
    }else {
      const configData = {
        ...dataForm.value, // 基本信息
        imageUrl: props.nodesImage,
        modelType: modelType.value,
        modelSequence: modelSequence.value,
        modelVote: modelVote.value
      };

      saveModelConfig(configData).then(res => {
        if (res.data.code === '200'){
          ElMessage.success('保存成功！')
          handleClose()
        }else {
          ElMessage.error('保存失败：' + res.data.message)
        }
      })

      handleClose()
    }
  })
};

// 关闭对话框时的逻辑
const handleClose = () => {
  reset();
  emit('update:dialogVisible', false);
};
</script>

<style lang="scss" scoped>

.treeDialog{
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 将内容水平分布在容器中 */
  align-items: flex-end;
  .el-button{
    background-color: cadetblue;
    width: 65px;
    height: 40px;
    margin-top: 15px;
    border-color: white;
    &:hover {
      background-color: chocolate; /* 设置悬浮时的背景色 */
    }
  }
}
</style>
