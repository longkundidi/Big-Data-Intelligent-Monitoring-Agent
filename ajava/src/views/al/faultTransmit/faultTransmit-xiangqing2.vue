<template>
  <el-dialog
    append-to-body
    :close-on-click-modal="false"
    v-model="visible"
    :show-close="true"
    class="el-dialog__header"
    width="70%"
    @close="closeDialog()"
  >
    <div slot="title" style="font-size: 30px;text-align:center;padding-bottom: 2%">
      <span>模型详情</span>
    </div>


    <el-form ref="dataForm" :rules="dataRule" label-width="90px" @keyup.enter.native="dataFormSubmit()" >
      <el-row>
        <el-col :span="8">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="dataForm.modelName"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型类型" prop="modelType">
            <el-input v-model="dataForm.modelType"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="提供者" prop="modelProvider">
            <el-input v-model="dataForm.modelProvider"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="针对对象" prop="modelObject">
            <el-input v-model="dataForm.modelObject"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="模型支持库" prop="modelLibrary">
            <el-input v-model="dataForm.modelLibrary"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="适用场景" prop="modelCondition">
            <el-input v-model="dataForm.modelCondition"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型优点" prop="modelAdvantage">
            <el-input v-model="dataForm.modelAdvantage"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型缺点" prop="modelDisadvantage">
            <el-input v-model="dataForm.modelDisadvantage"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="预处理方法" >
            <el-input v-model="dataFormtwo.pretreatment"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="样本类型" >
            <el-input v-model="dataFormtwo.sampleType"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="样本尺寸" >
            <el-input v-model="dataFormtwo.sampleSize"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="指标要求" >
            <el-input v-model="dataFormtwo.indicatorRequire"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="阈值类型" >
            <el-input v-model="dataFormtwo.thresholdType"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="阈值描述" >
            <el-input v-model="dataFormtwo.thresholdDescription"  disabled />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="超限次数" >
            <el-input v-model="dataFormtwo.overrunTimes"  disabled />
          </el-form-item>
        </el-col>

      </el-row>
    </el-form>

    <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
      <el-button @click="xiugai" class="normalBtn" style="margin-right: 8%;width: 8%">修改</el-button>
      <el-button v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()" style="width: 8%">返回</el-button>
    </span>

    <xiugai v-if="xiugaiVisible" ref="xiugai" @success="refreshGroup"/>

  </el-dialog>
</template>

<script>
import {getTransmitObj} from '/src/api/al/faultTransmit/faultTransmitBase'
import xiugai from './faultTransmit-xiugai2.vue'
import {fetchList2} from '/src/api/al/faultTransmit/faultTransmitTwo'


export default {
  components: {
    xiugai
  },
  data() {
    return {
      searchForm1: {
        modelName:''
      },
      visible: false,
      canSubmit: false,
      dataForm: {

      },
      dataFormtwo:{
      },
      xiugaiVisible:false,

    }
  },


  methods: {

    async refreshGroup(){
      let id =this.dataForm.id
      getTransmitObj(id).then(response => {
        this.dataForm = response.data.data
        this.searchForm1.modelName = this.dataForm.modelName
        this.getDomaintwo()
      })
    },


    init(id) {
      this.visible = true
      this.canSubmit = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
      getTransmitObj(id).then(response => {
        this.dataForm = response.data.data
        this.searchForm1.modelName = this.dataForm.modelName
        this.getDomaintwo()
      })
    },

    getDomaintwo(page, params) {
      fetchList2(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm1)).then(response => {
        this.dataFormtwo = response.data.data.records[0]
        this.count = response.data.data.total
      })
    },

    // 表单提交
    dataFormSubmit() {
      this.canSubmit = false
      this.visible = false
      this.$emit('refreshDataList')
    },
    // 重置表单
    closeDialog() {
      this.$refs['dataForm'].resetFields()
      this.searchForm1.modelName = ''
    },


    xiugai()
    {
      let id =this.dataForm.id
      this.xiugaiVisible = true
      this.$nextTick(() => {
        this.$refs.xiugai.init(id)
      })
    },


    indexMethod(index)
    {
      return index+1;
    },
  }
}
</script>
