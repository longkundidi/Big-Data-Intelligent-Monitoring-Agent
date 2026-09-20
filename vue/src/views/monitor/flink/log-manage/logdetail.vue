<template>
  <div v-loading="loading" class="fl-logdetail-container">
    <el-tooltip class="item" effect="dark" content="返回" placement="right">
      <el-icon class="fl-back" @click="handleBack">
        <DArrowLeft />
      </el-icon>
    </el-tooltip>
    <el-form ref="form" :model="form" :disabled="false" label-width="70px" class="fl-log">
      <el-row>
        <el-col :span="10">
          <el-form-item label="运行状态" prop="jobStatus">
            <el-input v-model="form.jobStatus" placeholder="运行状态" disabled/>
          </el-form-item>
        </el-col>
        <el-col :span="14">
          <el-form-item label="Flink客户端日志" prop="clinetJobUrl" label-width="120px">
            <el-link type="primary" target="_blank" :href="form.clinetJobUrl">{{ form.clinetJobUrl }}</el-link>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="10">
          <el-form-item label="运行模式" prop="deployMode">
            <el-input v-model="form.deployMode" placeholder="运行模式" disabled/>
          </el-form-item>
        </el-col>
        <el-col :span="14">
          <el-form-item label="Flink集群日志" prop="remoteLogUrl" label-width="120px">
            <el-link type="primary" target="_blank" :href="form.remoteLogUrl">{{ form.remoteLogUrl }}</el-link>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="日志内容" prop="localLog">
<!--            <codemirror ref="cm" :value="form.localLog" :options="cmOptions" class="fl-codemirror"/>-->
            <textarea id="editor" v-model="form.localLog" style="display:none;"></textarea>
            <div id="codemirror-log"></div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row class="fl-button-row">
        <el-col :span="24">
          <el-form-item label="">
            <el-button type="primary" @click="getLogDetail()">刷新</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import CodeMirror from "codemirror";
import 'codemirror/lib/codemirror.css'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/show-hint'
import 'codemirror/theme/mbo.css'
import 'codemirror/mode/sql/sql.js'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/sql-hint.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/selection/active-line'
import 'codemirror/addon/selection/selection-pointer'
import {logDetail} from "@/api/log";


export default {
  name: 'LogDetail',
  // components: {
  //   codemirror
  // },
  data() {
    return {
      loading: false,
      params: {
        flag: '',
        data: {},
        context: '' // 父页面传递过来的参加，返回时带给父页面恢复上下文
      },
      logid: '',
      form: {},
      cmOptions: {
        value: '',
        mode: 'text/x-sh', // flink/x-fsql, text/x-mysql, text/x-sh
        theme: 'default', // solarized light,base16-light,cobalt,default,mbo,cobalt
        readOnly: true,
        tabSize: 4,
        line: true,
        lineNumbers: true,
        extraKeys: {'Ctrl': 'autocomplete'} // 自定义快捷键
      },
      cmEditor: null
    }
  },
  mounted() {
    const params = this.$route.query
    this.params.flag = params.flag
    this.params.context =JSON.parse( params.context)
    this.params.data = JSON.parse(params.data)
    this.logid = JSON.parse(params.data).lastRunLogId

    this.getLogDetail()

    // 获取 DOM 元素
    const container = document.getElementById('codemirror-log');

    // 初始化 CodeMirror 实例
    this.cmEditor = CodeMirror(container, this.cmOptions)

  },
  methods: {
    handleBack() { // 返回
      if (this.params.flag === 'loglist') {
        this.$router.replace({name: 'FlinkLogManage', params: this.params.context})
      } else if (this.params.flag === 'tasklist') {
        this.$router.replace({name: 'flink监控', params: this.params.context})
      }
    },
    getLogDetail() { // 查询日志详情
      this.loading = true
      logDetail(this.logid).then(response => {
        this.loading = false
        const {code, success, message, data} = response.data
        if (code !== '200' || !success) {
          this.$message({type: 'error', message: (message || '请求数据异常！')})
          return
        }
        this.form = data || {}
        // 设置初始值
        this.cmEditor.setValue(this.form.localLog)
      }).catch(error => {
        this.loading = false
        this.$message({type: 'error', message: '请求异常！'})
        console.log(error)
      })
    }
  }
}
</script>

<style scoped>
.fl-logdetail-container {
  margin: 0px 20px;
}

.fl-back {
  color: #303133;
  font-size: 14px;
  margin-left: -20px;
  cursor: pointer;
}

.fl-back:hover {
  color: #a2a6af;
}

.fl-codemirror {
  border: 1px solid #C0C4CC;
}

.fl-log >>> .el-form-item {
  margin-bottom: 10px !important;
}

.fl-button-row >>> .el-form-item {
  margin-bottom: 0px !important;
}

.fl-log >>> .CodeMirror {
  height: calc(100vh - 205px);
  line-height: 150%;
  font-family: monospace, Helvetica Neue, Helvetica, Arial, sans-serif;
  font-size: 13px;
  background: #f5f7fa;
}

#codemirror-log {
  width: 100%;
}
</style>
