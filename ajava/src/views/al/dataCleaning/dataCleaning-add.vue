<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="false"
      v-model="visible"
      :show-close="true"
      class="el-dialog__header"
      show-close
      @close="closeDialog()"
  >

    <div slot="title" style="display: flex;padding-bottom: 15px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>
      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">算法注册</span>
      </div>
    </div>

    <!--    定义步骤-->
    <div class="app-container" style="padding-bottom: 3%">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="算法基础信息"></el-step>
        <el-step title="算法部署与测试"></el-step>
      </el-steps>

      <!--      定义表单-->
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="90px"
               @keyup.enter.native="dataFormSubmit()">

        <div v-show="active===0">
          <el-row style="padding-top: 30px">

            <el-col :span="12">
              <el-form-item label="算法名称" prop="alName">
                <el-input v-model="dataForm.alName"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="算法类型" prop="alType">
                <el-select v-model="dataForm.alType" style="width: 100%">
                  <el-option
                      v-for="item in options"
                      :key="item.alType"
                      :value="item.alType"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="算法适用" prop="alSuit">
                <el-input v-model="dataForm.alSuit"
                          maxlength="20"
                          show-word-limit
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="算法简介" prop="alBrief">
                <el-input v-model="dataForm.alBrief"
                          maxlength="50"
                          show-word-limit/>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="算法图片">
                <el-button class="normalBtn"
                           @click="upload">上传
                </el-button>
                <el-tag type="success" v-if="geticon" style="margin-left: 3px;">{{ dataForm.iconUrl }}</el-tag>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="提供人员" prop="creator">
                <el-input v-model="dataForm.creator"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-show="active===1">
          <el-row style="padding-top: 30px">
            <el-col :span="24">
              <el-form-item label="执行程序包">
                <el-button class="normalBtn"
                           @click="uploadprogram">上传
                </el-button>
                <el-tooltip placement="top">
                  <template #content>
                    1、程序包需上传压缩包； <br/>
                    2、压缩包与算法执行文件同名； <br/>
                    3、压缩包内须有算法执行文件；<br/>
                    4、算法执行文件要有主函数入口if
                    __name__ =='__main__'，主函数要有输出（print等
                  </template>
                  <el-icon>
                    <QuestionFilled/>
                  </el-icon>
                </el-tooltip>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部署要求" prop="deployRequire">
                <el-input v-model="dataForm.deployRequire"/>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="调用方式" prop="invocation" disabled="dataForm.alName">
                <el-radio-group v-model="dataForm.invocation">
                  <el-radio label="业务调用（微服务）"></el-radio>
                  <el-radio label="时间窗自动调用（Flink）"></el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="算法输入样例" prop="input" label-width="110px">
                <el-input v-model="dataForm.input" style="width: 100%">
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="算法输出样例" prop="output" label-width="110px">
                <el-input v-model="dataForm.output" style="width: 100%">
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

      </el-form>
    </div>

    <div slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
      <el-button v-if="active < 1" class="normalBtn" style="margin-bottom: 20px" @click="next()">下一步</el-button>
      <el-button v-if="active > 0" class="normalBtn" style="margin-bottom: 20px" @click="pre()">上一步</el-button>
      <el-button v-if="active === 1 && canSubmit" style="margin-bottom: 20px" class="normalBtn"
                 @click="dataFormSubmit()">注册
      </el-button>
      <el-button class="normalBtn" @click="visible = false;" v-if="active < 1">取消</el-button>
    </div>

    <uploadicon v-if="dialogVisible" ref="upload" @geticonurl="geticonurl"></uploadicon>
    <uploadprogram v-if="programVisible" ref="uploadprogram" @getprogramurl="getprogramurl"></uploadprogram>

  </el-dialog>
</template>

<script>
import {addObj, exitName} from '/src/api/al/dataCleaning/dataCleaning'
import uploadicon from "./dataCleaning-add-uploadicon.vue";
import uploadprogram from "./dataCleaning-add-uploadprogram.vue";

export default {
  data() {
    return {
      options:
          [
            {alType: '数据去重'},
            {alType: '缺失值填充'},
            {alType: '异常值检测'},
            {alType: '数据标准化'},
            {alType: '相关性分析'},
          ],
      active: 0,
      visible: false,
      canSubmit: false,
      dialogVisible: false,
      programVisible: false,
      geticon: false,
      getprogram: false,
      dataForm: {
        creator: '',
        alSuit: '',
        id: '',
        alName: '',
        alType: '',
        alBrief: '',
        input: '',
        output: '',
        alNum: '0',
        programUrl: '',
        deployInput: '',
        deployOutput: '',
        deployRequire: '',
        invocation: [],
        ispass: '1',
        isService:0

      },
      dataRule: {
        alName: [
          {required: true, message: '算法名称不能为空', trigger: 'blur'}
        ],
        alType: [
          {required: true, message: '算法类型不能为空', trigger: 'blur'}
        ],
        invocation: [
          {required: true, message: '调用机制不能为空', trigger: 'blur'}
        ],
        input: [
          {required: true, message: '算法输入样例不能为空', trigger: 'blur'}
        ],
        output: [
          {required: true, message: '算法输出样例不能为空', trigger: 'blur'}
        ]
      }
    }
  },
  components: {
    uploadicon,
    uploadprogram
  },
  methods: {
    init() {
      this.visible = true
      this.canSubmit = true
      this.geticon = false
      this.getprogram = false
      this.getObject = false
      this.active = 0
      this.$nextTick(() => {
        this.$refs.dataForm.resetFields()
      })
    },

    upload() {
      if (this.dataForm.alName === '') {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.alName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.dialogVisible = true
            this.$nextTick(() => {
              this.$refs.upload.init()
            })
          }
        }).catch(() => {
        })
      }
    },

    uploadprogram() {
      if (this.dataForm.alName === '') {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.alName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            this.programVisible = true
            this.$nextTick(() => {
              this.$refs.uploadprogram.init()
            })
          }
        }).catch(() => {
        })
      }
    },

    geticonurl(iconurl) {
      this.geticon = true
      this.dataForm.iconUrl = iconurl
    },

    getprogramurl(programurl) {
      this.getprogram = true
      this.dataForm.programUrl = programurl
    },

    next() {
      if (this.dataForm.alName === '' || this.dataForm.alType === '') {
        this.$message({
          showClose: true,
          message: '必要参数未配置',
          type: 'warning'
        })
      } else if (this.geticon === false) {
        this.$message({
          showClose: true,
          message: '算法图片未上传',
          type: 'warning'
        })
      } else {
        exitName(this.dataForm.alName).then(response => {
          if (response.data.code === '500') {
            this.$notify.warning('该名称已存在')
          } else {
            if (this.active++ > 1) this.active = 0
          }
        }).catch(() => {
        })
      }
    },

    pre() {
      if (this.active-- < 0) this.active = 0
    },

    // 表单提交
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          if (this.getprogram === true) {
            // this.dataForm.invocation = this.dataForm.invocation.join(',')
            this.canSubmit = false
            addObj(this.dataForm).then(response => {
              if (response.data.code === '500') {
                this.$message({
                  showClose: true,
                  message: '该名称已存在',
                  type: 'warning'
                })
                this.visible = false
                this.$emit('refreshDataList')
              } else {
                this.$notify.success('添加成功')
                this.visible = false
                this.$emit('refreshDataList')
              }
            }).catch(() => {
              this.canSubmit = true
            })
          } else {
            this.$message({
              showClose: true,
              message: '程序包未上传',
              type: 'warning'
            })
          }
        }
      })
    },
    // 重置表单
    closeDialog() {
      this.$refs.dataForm.resetFields()
      this.$emit('refreshDataList')
    }
  }
}
</script>
<style>
.el-step__icon {
  top: -1px;
}
</style>
