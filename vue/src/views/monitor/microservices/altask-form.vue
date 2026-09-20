<template>
  <el-dialog
    :title="'修改'"
    append-to-body
    :close-on-click-modal="false"
    @close="closeDialog()"
    v-model="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">


      <el-form-item label="算法Id" prop="alId">
        <el-input v-model="dataForm.alId" placeholder="算法Id"></el-input>
      </el-form-item>
<!--      -->
<!--    <el-form-item label="任务状态" prop="taskState">-->
<!--        <el-input v-model="dataForm.taskState" placeholder="任务状态"></el-input>-->
<!--    </el-form-item>-->

      <el-form-item label="任务状态" prop="taskState">
        <el-select v-model="dataForm.taskState" style="width: 100%">
          <el-option
            v-for="item in options"
            :label="item.name"
            :key="item.id"
            :value="item.id"
          />
        </el-select>
      </el-form-item>


    <el-form-item label="任务信息" prop="taskMsg">
        <el-input v-model="dataForm.taskMsg" placeholder="任务信息"></el-input>
    </el-form-item>
    <el-form-item label="任务结果" prop="taskResult">
        <el-input v-model="dataForm.taskResult" placeholder="任务结果"></el-input>
    </el-form-item>


    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()" v-if="canSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
    import {getObj, addObj, putObj} from '@/api/task-manage/taskManage'

    export default {
    data () {
      return {
        visible: false,
        canSubmit: false,
        dataForm: {
          id: '',
          taskId: '',
          taskState: '',
          alId: '',
          taskMsg: '',
          taskResult: '',
          createTime: '',
          editTime: '',
          isDeleted: '',
          creator: '',
          editor: '',
        },
        dataRule: {
          taskId: [
            { required: true, message: '任务编号不能为空', trigger: 'blur' }
          ],

          taskState: [
            { required: true, message: '任务状态不能为空', trigger: 'blur' }
          ],

          alId: [
            { required: true, message: '算法Id不能为空', trigger: 'blur' }
          ],
        },
        options:[
          {
            id:0,
            name:'未执行'
          },
          {
            id: 1,
            name:'执行中'
          },
          {
            id: 2,
            name:'已结束'
          },
          {
            id: 3,
            name:'执行失败'
          }
        ]
      }
    },
    methods: {
      init (id) {
        this.visible = true;
        this.canSubmit = true;
        this.$nextTick(() => {
            this.$refs['dataForm'].resetFields()
            if (id) {
            getObj(id).then(response => {
                this.dataForm = response.data
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.canSubmit = false;
            if (this.dataForm.id) {
                putObj(this.dataForm).then(data => {
                    this.$notify.success('修改成功')
                    this.visible = false
                    this.$emit('refreshDataList')
                }).catch(() => {
                    this.canSubmit = true;
                });
            }
          }
        })
      },
      //重置表单
      closeDialog() {
          this.$refs["dataForm"].resetFields()
      }
    }
  }
</script>
