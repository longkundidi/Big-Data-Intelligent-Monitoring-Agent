<template>
  <el-dialog
      v-model="kkfileViewShow"
      title="Tips"
      align-center
      :before-close="closeDialog"
  >
    <div id="officeDiv" style="height: 100%;width: 100%">
      <iframe :src='kkfileViewUrl' style="width: 100%;height: 100%"></iframe>
    </div>
  </el-dialog>
</template>

<script setup>
import {ref, watch} from "vue";
import {Base64} from "js-base64";
let kkfileViewUrl = ref("")
let kkfileViewShow = ref(false)
const props = defineProps({
  showFileUrl: String,
  kkfileViewShowProp: Boolean // 父组件传入的属性
});
watch(() => props.showFileUrl, (newValue, oldValue) => {
  kkfileViewUrl.value = kkfileUrl + 'onlinePreview?url=' + encodeURIComponent(Base64.encode(newValue))
})

const emit = defineEmits(['update:kkfileViewShowProp']);
watch(() => props.kkfileViewShowProp, (newValue, oldValue) => {
  kkfileViewShow.value = newValue
})
const closeDialog = function (){
  kkfileViewShow.value = false
  emit('update:kkfileViewShowProp', false);
}
</script>

<style scoped>
#officeDiv {
  height: 75vh !important;
}

</style>
