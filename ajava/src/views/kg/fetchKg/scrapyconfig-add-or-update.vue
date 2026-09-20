<template>
  <div>
    <el-dialog
        :title="title"
        v-model="visible"
        width="25%"
        append-to-body
        :before-close="close"
    >
      <el-form :model="formdata" label-width="90px" ref="form" style="margin-top: -5px" @submit.prevent>
        <el-form-item label="关键词" prop="keyword"
                      :rules="{required: true, trigger: 'blur', message: '关键词不能为空'}">
          <el-input v-model="formdata.keyword" placeholder="关键词"></el-input>
        </el-form-item>
        <div slot="footer"
             class="dialog-footer"
             style="margin-top: 25px">
          <el-button class="cancelbtn"
                     @click="close()">取 消
          </el-button>
          <el-button class="determinebtn"
                     @click="submit()">保 存
          </el-button>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>


import {ElMessage} from "element-plus";
import {save, updateById} from "@/api/kg/fetchKg/patent";

export default {
  name:'scracyConfigDlg',

  components: {

  },
  data() {
    return {
      title: "新增",
      visible: false,
      mode: 0,
      form: null,
      formdata: {},
      flag: false,
      emit:['afterCommit'],
    }
  },
  mounted() {

  },
  methods: {
    async init(type, id) {
      this.mode = type;
      this.formdata.id = id;
      if (this.mode === 1) {
        this.title = '编辑'
      } else {
        this.title = '新增';
      }
      this.visible = true;
    },
    //关闭弹窗时清空缓存
    close() {
      this.$refs.form.resetFields();
      this.formdata = {};
      this.visible = false;
    },
    submit(){
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.mode === 0) {
            save(this.formdata).then(r => {
              r = r.data
              this.flag = r.data;
              this.visible = false;
              this.$emit('afterCommit');
              if (this.flag === true) {
                ElMessage({
                  type: "success",
                  message: "新增成功!",
                });
              } else {
                ElMessage({
                  type: "info",
                  message: "新增失败，已存在字典里面",
                });
              }

            });
          } else {
            updateById(this.formdata.id, this.formdata)
                .then(r => {
                  this.$emit('afterCommit');
                  this.visible = false;
                });
          }
        }
      })
    },
  }

}
</script>
<style scoped>

</style>