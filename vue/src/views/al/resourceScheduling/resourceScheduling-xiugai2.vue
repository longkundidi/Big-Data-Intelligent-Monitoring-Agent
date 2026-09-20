<template>
  <el-dialog
    append-to-body
    :close-on-click-modal="false"
    v-model="visible"
    width="70%"
    @close="closeDialog()"
    :show-close="true"
    class="el-dialog__header"
  >
    <div slot="title" style="font-size: 30px;text-align:center;padding-bottom: 2%">
      <span>模型修改</span>
    </div>


    <el-form ref="dataForm"  :rules="dataRule" label-width="100px" @keyup.enter.native="dataFormSubmit()">
      <el-row>
        <el-col :span="8">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="dataForm.modelName"  :disabled="true" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型类型" prop="modelType">
            <el-select v-model="dataForm.modelType" style="width: 100%" >
              <el-option
                  v-for="item in options"
                  :key="item.modelType"
                  :value="item.modelType"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="提供者" prop="modelProvider">
            <el-input v-model="dataForm.modelProvider"   />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="针对对象" prop="modelObject">
            <el-input v-model="dataForm.modelObject"   />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="模型支持库" prop="modelLibrary">
            <el-input v-model="dataForm.modelLibrary"   />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="上传图标">
            <el-button class="normalBtn" @click="upload">上传</el-button>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="适用场景" prop="modelCondition">
            <el-input v-model="dataForm.modelCondition"
                      maxlength="50"
                      show-word-limit/>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型优点" prop="modelAdvantage">
            <el-input v-model="dataForm.modelAdvantage"
                      maxlength="50"
                      show-word-limit/>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="模型缺点" prop="modelDisadvantage">
            <el-input v-model="dataForm.modelDisadvantage"
                      maxlength="50"
                      show-word-limit/>
          </el-form-item>
        </el-col>

        <el-col :span="4" style="padding-top: 0.5%;padding-left: 3%">
          <div style="display: inline-block;" ><span style="font-size: 15px;vertical-align: middle;color: #606266;font-weight: 700">预处理：</span></div>
          <div style="display: inline-block">
            <el-radio-group v-model="dataFormtwo.ifPretreatment" @change="Changeyuchuli">
              <el-radio  label="0">是</el-radio>
              <el-radio  label="1">否</el-radio>
            </el-radio-group>
          </div>
        </el-col>

        <el-col :span="8">
          <el-form-item label="预处理方法"  >
            <el-input v-model="dataFormtwo.pretreatment" :disabled="btnstatus"  />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="样本类型"   >
            <el-input v-model="dataFormtwo.sampleType"   />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="样本尺寸" >
            <el-input v-model="dataFormtwo.sampleSize"   />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="指标要求" >
            <el-input v-model="dataFormtwo.indicatorRequire"   />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="阈值类型" >
            <el-select v-model="dataFormtwo.thresholdType" style="width: 100%" >
              <el-option
                v-for="item in options5"
                :key="item.thresholdType"
                :value="item.thresholdType"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="阈值描述" >
            <el-input v-model="dataFormtwo.thresholdDescription"  />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="超限次数" >
            <el-input v-model="dataFormtwo.overrunTimes"/>
          </el-form-item>
        </el-col>

      </el-row>
    </el-form>

    <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
      <el-button v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()" style="width: 8%">完成修改</el-button>
    </span>

    <uploadurl v-if="dialogVisible" ref="upload" @getIconUrl="getIconUrl"></uploadurl>

  </el-dialog>
</template>

<script>
import {getSchedulingObj,putObj} from '/src/api/al/resourceScheduling/resourceSchedulingBase'
import {fetchList2,putObj2} from "/src/api/al/resourceScheduling/resourceSchedulingTwo";
import uploadurl from './resourceScheduling-add-uploadicon.vue'


export default {
  data() {
    return {
      options: [
        { modelType: '信号处理模型' },
        { modelType: '统计学模型' },
      ],

      options5: [
        { thresholdType: '专家阈值' },
        { thresholdType: '自适应阈值' },
      ],
      dialogVisible:false,
      searchForm1: {
        modelName:''
      },
      dataFormtwo:{
      },
      visible: false,
      canSubmit: false,
      dataForm: {
      },
      btnstatus:false,

      dataRule: {
        modelTypeFirst:
          [
            { required: true, message: '模型类型不能为空', trigger: 'blur' }
          ],
      }
    }
  },
  components: {
    uploadurl
  },

  methods: {
    init(id) {
      this.visible = true
      this.canSubmit = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
      getSchedulingObj(id).then(response => {
        this.dataForm = response.data.data
        this.searchForm1.modelName = this.dataForm.modelName
        this.getDomaintwo()
      })
    },

    upload()
    {
      if(this.dataForm.modelName === '' || this.dataForm.modelTypeFirst === ''||this.dataForm.modelType === '' ||this.dataForm.modelFunction === ''){
        this.$notify.warning("必要参数未配置")
      }
      else
      {
        let modelName = this.dataForm.modelName
        this.dialogVisible = true
        this.$nextTick(() => {
          this.$refs.upload.init(modelName)
        })
      }
    },
    getIconUrl(iconUrl){
      this.dataForm.modelIcon=iconUrl
    },
    getDomaintwo(page, params) {
      fetchList2(Object.assign({
        current: this.currentPage,
        size: this.pageSize
      }, params, this.searchForm1)).then(response => {
        this.dataFormtwo = response.data.data.records[0]
        this.dataFormtwo.ifPretreatment = String(this.dataFormtwo.ifPretreatment);
        if (this.dataFormtwo.ifPretreatment === '1'){
          this.btnstatus = true
        }
        this.count = response.data.data.total
      })
    },

    Changeyuchuli(val)
    {
      this.btnstatus=(val !== '0');
      if(this.btnstatus === true){this.dataFormtwo.pretreatment = ''}
    },

    // 表单提交
    dataFormSubmit() {
      this.dataFormtwo.ifPretreatment = +this.dataFormtwo.ifPretreatment;
      this.dataFormtwo.modelName = this.dataForm.modelName
      this.canSubmit = false
      putObj(this.dataForm).then(response => {
        this.visible = false
        this.$emit('success')
      }).catch(() => {
        this.canSubmit = true;
      });

      putObj2(this.dataFormtwo).then(response => {
        this.$notify.success('修改成功')
        this.visible = false
        this.$emit('success')
      }).catch(() => {
        this.canSubmit = true;
      });
    },
    // 重置表单
    closeDialog() {
      this.$refs['dataForm'].resetFields()
      this.searchForm1.modelName = ''
    },

  }
}
</script>
