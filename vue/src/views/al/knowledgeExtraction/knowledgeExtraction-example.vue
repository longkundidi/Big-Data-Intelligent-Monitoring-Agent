<template>
  <el-dialog
    append-to-body
    width="35%"
    :close-on-click-modal="false"
    v-model="visible"
    :show-close="true"
    class="el-dialog__header"
    @close="closeDialog()"
  >

    <div slot="title" style="font-size: 25px;text-align:center;padding-bottom: 8%">
      <span>模型案例</span>
    </div>

    <el-image
      style="width: 100%;height:100%;padding-bottom:5%"
      :src="'/api/al/file/'+this.iconUrl"></el-image>


    <span slot="footer" class="dialog-footer;" style="padding-left: 80%;">
      <el-button style="width: 15%" class="normalBtn" @click="visible = false">返回</el-button>
    </span>
  </el-dialog>
</template>

<script>

export default {
  data() {
    return {
      visible: false,
      iconUrl:''
    }
  },
  methods: {
    init(iconUrl) {
      this.visible = true
      this.iconUrl = iconUrl
    },
    // 重置表单
    closeDialog() {
    }
  },
}
</script>
