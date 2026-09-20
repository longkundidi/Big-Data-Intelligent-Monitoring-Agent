<template>
  <el-dialog
      append-to-body
      align-center
      :close-on-click-modal="false"
      v-model="visible"
      class="no-header-dialog"
      @close="closeDialog()"
      width="40%"
  >

    <div slot="title" style="display: flex;padding-bottom: 20px">
      <div slot="title" style="width:5%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>

      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">审核</span>
      </div>
    </div>

    <!--    <div v-if="alType!==''">-->
    <!--      <el-form ref="dataForm1" :model="dataForm1" :rules="dataRule" label-width="90px"-->
    <!--               @keyup.enter.native="dataForm1Submit()">-->

    <!--        &lt;!&ndash;      <el-form-item label="算法名称" prop="alName">&ndash;&gt;-->
    <!--        &lt;!&ndash;        <el-input v-model="dataForm1.alName" placeholder="算法名称" />&ndash;&gt;-->
    <!--        &lt;!&ndash;      </el-form-item>&ndash;&gt;-->

    <!--        <el-form-item label="算法名称" prop="alName">-->
    <!--          <el-input v-model="dataForm1.alName" placeholder="算法名称"/>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="算法类型" prop="alType">-->
    <!--          <el-select v-model="dataForm1.alType" style="width: 100%">-->
    <!--            <el-option-->
    <!--                v-for="item in options"-->
    <!--                :key="item.alType"-->
    <!--                :value="item.alType"-->
    <!--            />-->
    <!--          </el-select>-->
    <!--        </el-form-item>-->

    <!--        <el-form-item label="输入数据" prop="input">-->
    <!--          <el-input v-model="dataForm1.input" placeholder="输入数据"/>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="输出数据" prop="output">-->
    <!--          <el-input v-model="dataForm1.output" placeholder="输出数据"/>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="算法简介" prop="alBrief">-->
    <!--          <el-input v-model="dataForm1.alBrief" placeholder="算法简介"/>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="算法适用" prop="alSuit">-->
    <!--          <el-input v-model="dataForm1.alSuit" placeholder="算法适用"/>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="提供人员" prop="creator">-->
    <!--          <el-input v-model="dataForm1.creator" placeholder="提供人员"/>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="算法原理图" style="margin-left: 20px">-->
    <!--          <el-image v-if="dataForm1.iconUrl!==null"-->
    <!--                    style="width: 50%;height:100%"-->
    <!--                    :src="'/api/al/file/'+dataForm1.iconUrl">-->

    <!--          </el-image>-->
    <!--          <el-image v-else-->
    <!--                    style="width: 100%;height:100%">-->
    <!--            <template #error>-->
    <!--              <div class="image-slot" style="width: 100%;height:100%">-->
    <!--                <img src="@/assets/upload.png" style="width: 80%;height:100%">-->
    <!--              </div>-->
    <!--            </template>-->
    <!--          </el-image>-->
    <!--        </el-form-item>-->
    <!--      </el-form>-->
    <!--      <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">-->
    <!--            <el-button style="width: 10%" v-if="canSubmit" class="normalBtn"-->
    <!--                       @click="dataForm1Submit()">完成修改</el-button>-->
    <!--            <el-button style="width: 10%" class="normalBtn" @click="visible = false">取消</el-button>-->
    <!--      </span>-->
    <!--    </div>-->
    <div class="app-container" style="padding-bottom: 5%">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="测试"></el-step>
        <el-step title="通过"></el-step>
        <el-step title="部署"></el-step>
      </el-steps>
    </div>

    <div>
      <el-form ref="dataForm" :rules="dataRule" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="dataForm.modelName!==undefined" label="模型名称" prop="modelName">
              <el-input v-model="dataForm.modelName" disabled=""/>
            </el-form-item>
            <el-form-item v-else label="算法名称" prop="alName">
              <el-input v-model="dataForm.alName" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="程序包路径" prop="programUrl">
              <el-input v-model="dataForm.programUrl" disabled/>
            </el-form-item>
          </el-col>


          <el-col :span="12">
            <el-form-item label="部署要求" prop="deployRequire">
              <el-input v-model="dataForm.deployRequire" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item v-if="dataForm.modelInvoke!==undefined" label="调用方式" prop="modelInvoke">
              <el-input v-model="dataForm.modelInvoke" disabled/>
            </el-form-item>
            <el-form-item v-else label="调用方式" prop="invocation">
              <el-input v-model="dataForm.invocation" disabled/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
<!--      <el-button @click="xiugai" class="normalBtn" style="margin-right: 8%;width: 8%">修改</el-button>-->
      <el-button style="width: 15%;margin-top: 20px" class="normalBtn" @click="visible=false">返回</el-button>
    </span>
    </div>


  </el-dialog>
</template>

<script>
import {getByName} from "@/api/al/auditsManagement/auditsManagement";
import {putObj} from "@/api/al/dataCleaning/dataCleaning";
import {getDiagnosisObj} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {fetchList1} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisOne";

export default {
  name: "form",
  data() {
    return {
      id: 0,
      active: 0,
      alType: '',
      options:
          [
            {alType: '数据去重'},
            {alType: '缺失值填充'},
            {alType: '异常值检测'},
            {alType: '数据标准化'},
          ],
      visible: false,
      canSubmit: false,
      dialogVisible: false,
      dataForm1: {},
      dataForm2_1: {},
      dataForm2_2: {},
      dataForm: {},
      dataRule: {
        alType: [
          {required: true, message: '算法类型不能为空', trigger: 'blur'}
        ],
        alName: [
          {required: true, message: '算法名称不能为空', trigger: 'blur'}
        ]
      }
    }
  },

  methods: {
    init(row) {
      if (row.isPass === 2) {
        this.active = 0
      }
      if (row.isPass === 1) {
        this.active = 1
      }
      this.visible = true
      this.canSubmit = true
      this.alType = ''
      getByName({
        alModelName: row.alModelName,
        modelFunction: row.modelFunction,
        alModelType: row.alModelType
      }).then(response => {
        const {data} = response.data
        this.dataForm = data[0]
        // if (data.length !== undefined) {
        //   this.id = data[0].id
        //   this.dataForm2_1 = data[0]
        //   this.dataForm2_2 = data[1]
        // } else {
        //   this.alType = data.alType
        //   this.dataForm1 = data
        // }
      })
    },
    // 表单提交
    // dataForm1Submit() {
    //   this.$refs['dataForm1'].validate((valid) => {
    //     if (valid) {
    //       this.canSubmit = false
    //       putObj(this.dataForm1).then(data => {
    //         this.$notify.success('修改成功')
    //         this.visible = false
    //         this.$emit("refreshDataList")
    //       }).catch(() => {
    //         this.canSubmit = true
    //       })
    //     }
    //   })
    // },
    // xiugai() {
    //   this.xiugaiVisible = true
    //   this.$nextTick(() => {
    //     this.$refs.xiugai.init(this.id)
    //   })
    //
    // },
    // async refreshGroup() {
    //   let id = this.id
    //   getObj(id).then(response => {
    //     const {code, success, message, data} = response.data
    //     this.dataForm2_1 = data
    //   })
    //   this.getDomainone()
    //   this.$emit("refreshDataList")
    // },
    // getDomainone(page, params) {
    //   fetchList1(Object.assign({
    //     current: 0,
    //     size: 1
    //   }, params, "")).then(response => {
    //     const {code, success, message, data} = response.data
    //     this.dataForm2_2 = data.records[0]
    //     this.count = data.total
    //   })
    // },
    // 重置表单
    closeDialog() {
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
    }
  }
}
</script>
