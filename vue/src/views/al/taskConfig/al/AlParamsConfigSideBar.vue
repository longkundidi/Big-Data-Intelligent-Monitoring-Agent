<!-- DataBar.vue -->
<template>
<div>
  <el-drawer
      v-model="visible"
      :size="'30%'"
      @close="handleClose"
  >

    <el-form ref="form" :model="formdata" label-width="100px" label-position="top" style="margin-top: 10px; margin-left: 20px;">
      <div style="display: flex; justify-content: center; background-color: rgba(128, 128, 128, 0.1); padding: 10px 0; width: 100%;margin: 10px 0">
        <span style="font-size: 16px; font-weight: bold; color: #333;">模型参数设置</span>
      </div>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="学习率:">
            <el-input v-model="formdata.learningRate" placeholder="请输入学习率"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="训练次数:">
            <el-input v-model="formdata.trainEpoch" placeholder="请输入训练次数"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="优化器:">
            <el-select v-model="formdata.optimizer" placeholder="请选择优化器">
              <el-option label="Adam" value="Adam"></el-option>
              <el-option label="SGD" value="SGD"></el-option>
              <el-option label="RMSprop" value="RMSprop"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="批次大小:">
            <el-input v-model="formdata.trainBatch" placeholder="请输入批次大小"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <div v-if="wavePacketVisible">
        <div style="display: flex; justify-content: center; background-color: rgba(128, 128, 128, 0.1); padding: 10px 0; width: 100%;margin: 10px 0">
          <span style="font-size: 16px; font-weight: bold; color: #333;">小波变换参数设置</span>
        </div>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="小波基类型:">
            <el-select v-model="formdata.wavelet" placeholder="请选择小波基类型">
              <el-option label="sym5" value="sym5"></el-option>
              <el-option label="haar" value="haar"></el-option>
              <el-option label="coif1" value="coif1"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="小波变换模式:">
            <el-select v-model="formdata.mode" placeholder="请选择小波变换模式">
              <el-option label="symmetric" value="symmetric"></el-option>
              <el-option label="zero" value="zero"></el-option>
              <el-option label="periodic" value="periodic"></el-option>
              <el-option label="reflect" value="reflect"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="最大分解层级:">
            <el-input v-model="formdata.maxlevel" placeholder="请输入最大分解层级"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="切片大小:">
            <el-input v-model="formdata.sliceLength" placeholder="请输入切片大小"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="提取层级:">
              <el-input v-model="formdata.n" placeholder="请输入提取的层级"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <div style="display: flex; justify-content: flex-end;align-items: center;">
      <el-button class="normalBtn"  @click="reset">重置</el-button>
      <el-button class="normalBtn" @click="submit">确定</el-button>
    </div>
  </el-drawer>
</div>
</template>

<script setup>
import {ref, computed, onMounted, onActivated, defineExpose} from 'vue';
import {ElDrawer, ElMessage} from 'element-plus'; // 使用 Element Plus 的 Drawer 组件
import { useStore } from 'vuex';

const props = defineProps({
  visible: Boolean,
  alName:String,
  formdata:Object,
  wavePacketVisible:Boolean
});
const emit = defineEmits(["update:visible","update:alName","update:wavePacketVisible","update:formdata"]);

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
});
const alName = computed({
  get: () => props.alName,
  set: (val) => emit('update:alName', val),
});
const formdata = computed({
  get: () => props.formdata,
  set: (val) => emit('update:formdata', val),
});
const wavePacketVisible = computed({
  get: () => props.wavePacketVisible,
  set: (val) => emit('update:wavePacketVisible', val),
});


const store=new useStore()


const submit=() =>  {
  let params={}
  params.alName=alName.value
  Object.assign(params, formdata.value);
  const hasValue = Object.values(formdata.value).some(val => val !== '' && val !== null && val !== undefined);
  store.commit('addModelParams', params);
  if(hasValue){
    ElMessage.success('参数配置成功')
    visible.value=false
  }
}

const reset=() =>  {
  formdata.value = {
    learningRate: '',
    trainEpoch: '',
    optimizer: '',
    trainBatch: '',
    wavelet:'',
    mode:'',
    maxlevel:'',
    sliceLength:'',
    n:''
  };
  ElMessage.success('重置成功！');
}

// 关闭抽屉
const handleClose = () => {
  visible.value = false;
  formdata.value = {
    learningRate: '',
    trainEpoch: '',
    optimizer: '',
    trainBatch: '',
    wavelet:'',
    mode:'',
    maxlevel:'',
    sliceLength:'',
    n:''
  };

};

// 页面首次加载或缓存页面再次激活时都清空
const clearModelParams=()=>{
  formdata.value = {
    learningRate: '',
    trainEpoch: '',
    optimizer: '',
    trainBatch: '',
    wavelet:'',
    mode:'',
    maxlevel:'',
    sliceLength:'',
    n:''
  };
  store.commit('clearModelParams')
}
onMounted(clearModelParams);
onActivated(clearModelParams);

defineExpose({
  clearModelParams
})
</script>

<style lang="scss" scoped>
::v-deep .el-drawer__header {
  color: #333; /* 修改字体颜色 */
  font-weight: bold; /* 修改字体加粗 */
  margin-bottom: 0;
}

</style>
