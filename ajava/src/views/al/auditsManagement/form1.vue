<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      @close="closeDialog()"
  >

    <div slot="title" style="display: flex;padding-bottom: 20px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>

      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">审核</span>
      </div>
    </div>

    <div v-if="alType!==''">
      <el-form ref="dataForm1" :model="dataForm1" :rules="dataRule" label-width="90px"
               @keyup.enter.native="dataForm1Submit()">

        <!--      <el-form-item label="算法名称" prop="alName">-->
        <!--        <el-input v-model="dataForm1.alName" placeholder="算法名称" />-->
        <!--      </el-form-item>-->

        <el-form-item label="算法名称" prop="alName">
          <el-input v-model="dataForm1.alName" placeholder="算法名称"/>
        </el-form-item>
        <el-form-item label="算法类型" prop="alType">
          <el-select v-model="dataForm1.alType" style="width: 100%">
            <el-option
                v-for="item in options"
                :key="item.alType"
                :value="item.alType"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="输入数据" prop="input">
          <el-input v-model="dataForm1.input" placeholder="输入数据"/>
        </el-form-item>
        <el-form-item label="输出数据" prop="output">
          <el-input v-model="dataForm1.output" placeholder="输出数据"/>
        </el-form-item>
        <el-form-item label="算法简介" prop="alBrief">
          <el-input v-model="dataForm1.alBrief" placeholder="算法简介"/>
        </el-form-item>
        <el-form-item label="算法适用" prop="alSuit">
          <el-input v-model="dataForm1.alSuit" placeholder="算法适用"/>
        </el-form-item>
        <el-form-item label="提供人员" prop="creator">
          <el-input v-model="dataForm1.creator" placeholder="提供人员"/>
        </el-form-item>
        <el-form-item label="算法原理图" style="margin-left: 20px">
          <el-image v-if="dataForm1.iconUrl!==null"
                    style="width: 50%;height:100%"
                    :src="'/api/al/file/'+dataForm1.iconUrl">

          </el-image>
          <el-image v-else
                    style="width: 100%;height:100%">
            <template #error>
              <div class="image-slot" style="width: 100%;height:100%">
                <img src="@/assets/upload.png" style="width: 80%;height:100%">
              </div>
            </template>
          </el-image>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
            <el-button style="width: 10%" v-if="canSubmit" class="normalBtn"
                       @click="dataForm1Submit()">完成修改</el-button>
            <el-button style="width: 10%" class="normalBtn" @click="visible = false">取消</el-button>
      </span>
    </div>

    <div v-else>
      <el-form ref="dataForm2_1" :rules="dataRule" label-width="90px" @keyup.enter.native="dataFormSubmit()">
        <el-row>
          <el-col :span="8">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="dataForm2_1.modelName" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="提供者" prop="modelProvider">
              <el-input v-model="dataForm2_1.modelProvider" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="针对对象" prop="modelObject">
              <el-input v-model="dataForm2_1.modelObject" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="算法类型" prop="modelType">
              <el-input v-model="dataForm2_1.modelType" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="调用机制" prop="modelInvoke">
              <el-input v-model="dataForm2_1.modelInvoke" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="运维服务" prop="modelFunction">
              <el-input v-model="dataForm2_1.modelFunction" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="算法支持库" prop="modelLibrary">
              <el-input v-model="dataForm2_1.modelLibrary" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="适用场景" prop="modelCondition">
              <el-input v-model="dataForm2_1.modelCondition" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="模型优点" prop="modelAdvantage">
              <el-input v-model="dataForm2_1.modelAdvantage" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="模型缺点" prop="modelDisadvantage">
              <el-input v-model="dataForm2_1.modelDisadvantage" disabled/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-form ref="dataForm2_2" :rules="dataRule" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="训练数据集">
              <el-input v-model="dataForm2_2.trainDataset" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="测试数据集">
              <el-input v-model="dataForm2_2.testDataset" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="模型框架">
              <el-input v-model="dataForm2_2.modelFramework" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="设备">
              <el-input v-model="dataForm2_2.device" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="预处理方法">
              <el-input v-model="dataForm2_2.pretreatment" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="样本类型">
              <el-input v-model="dataForm2_2.sampleType" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="样本尺寸">
              <el-input v-model="dataForm2_2.sampleSize" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="训练次数">
              <el-input v-model="dataForm2_2.trainTimes" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="训练批次">
              <el-input v-model="dataForm2_2.trainBatch" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="损失函数">
              <el-input v-model="dataForm2_2.lossFunction" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="正则化">
              <el-input v-model="dataForm2_2.regularization" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="学习率">
              <el-input v-model="dataForm2_2.learningRate" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="优化器">
              <el-input v-model="dataForm2_2.optimizer" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="训练结果">
              <el-input v-model="dataForm2_2.trainResults" disabled/>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="测试结果">
              <el-input v-model="dataForm2_2.testResults" disabled/>
            </el-form-item>
          </el-col>

        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer" style="text-align: center;display: block">
      <el-button @click="xiugai" class="normalBtn" style="margin-right: 8%;width: 8%">修改</el-button>
      <el-button style="width: 8%" class="normalBtn" @click="visible=false">返回</el-button>
    </span>

      <xiugai v-if="xiugaiVisible" ref="xiugai" @success="refreshGroup"/>

    </div>


  </el-dialog>
</template>

<script>
import {getByName} from "@/api/al/auditsManagement/auditsManagement";
import {putObj} from "@/api/al/dataCleaning/dataCleaning";
import xiugai from '../alFaultDiagnosis/alFaultDiagnosis-xiugai1.vue'
import {getDiagnosisObj} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisBase";
import {fetchList1} from "@/api/al/faultDiagnosisbase/alFaultDiagnosisOne";

export default {
  name: "form1",
  data() {
    return {
      id: 0,
      alType: '',
      xiugaiVisible: false,
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
  components: {
    xiugai,

  },
  mounted() {
  },
  methods: {
    init(row) {
      this.visible = true
      this.canSubmit = true
      this.alType = ''
      getByName({
        alModelName: row.alModelName,
        modelFunction: row.modelFunction,
        alModelType: row.alModelType
      }).then(response => {
        const {data} = response.data
        if (data.length !== undefined) {
          this.id = data[0].id
          this.dataForm2_1 = data[0]
          this.dataForm2_2 = data[1]
        } else {
          this.alType = data.alType
          this.dataForm1 = data
        }
      })
    },
    // 表单提交
    dataForm1Submit() {
      this.$refs['dataForm1'].validate((valid) => {
        if (valid) {
          this.canSubmit = false
          putObj(this.dataForm1).then(data => {
            this.$notify.success('修改成功')
            this.visible = false
            this.$emit("refreshDataList")
          }).catch(() => {
            this.canSubmit = true
          })
        }
      })


    },
    xiugai() {
      this.xiugaiVisible = true
      this.$nextTick(() => {
        this.$refs.xiugai.init(this.id)
      })

    },
    async refreshGroup() {
      let id = this.id
      getDiagnosisObj(id).then(response => {
        const {code, success, message, data} = response.data
        this.dataForm2_1 = data
      })
      this.getDomainone()
      this.$emit("refreshDataList")
    },
    getDomainone(page, params) {
      fetchList1(Object.assign({
        current: 0,
        size: 1
      }, params, "")).then(response => {
        const {code, success, message, data} = response.data
        this.dataForm2_2 = data.records[0]
        this.count = data.total
      })
    },
    // 重置表单
    closeDialog() {
      this.$refs['dataForm1'].resetFields()
    }
  }
}
</script>
