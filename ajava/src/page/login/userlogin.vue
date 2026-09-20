<template>
  <div class="login-zone">
    <div class="title">故障智能诊断算法引擎</div>
    <el-form
        ref="loginForm"
        class="login-form"
        status-icon
        :rules="loginRules"
        :model="loginForm"
        label-width="0">
      <el-form-item prop="username">
        <el-input class="my-input"
                  v-model="loginForm.username"
                  auto-complete="off"
                  placeholder="请输入用户名"
                  @keyup.enter.native="handleLogin"
        >
          <template #prefix>
            <i class="icon-yonghu"></i>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input class="my-input"
                  v-model="loginForm.password"
                  size="small"
                  type="password"
                  auto-complete="off"
                  show-password
                  placeholder="请输入密码"
                  @keyup.enter.native="handleLogin"
        >
          <template #prefix>
            <i class="icon-mima"></i>
          </template>

        </el-input>
      </el-form-item>
      <el-form-item v-if="website.validateCode" prop="code">
        <el-input class="my-input"
                  v-model="loginForm.code"
                  :maxlength="code.len"
                  auto-complete="off"
                  placeholder="请输入验证码"
                  @keyup.enter.native="handleLogin">
          <template #prefix>
            <i class="icon-yanzhengma"></i>
          </template>
          <template #append>
            <div class="login-code">
            <span
                v-if="code.type === 'text'"
                class="login-code-img"
                @click="refreshCode"
            >{{ code.value }}</span
            >
              <img
                  v-else
                  :src="code.src"
                  class="login-code-img"
                  @click="refreshCode"
              />
            </div>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button
            type="primary"
            class="login-submit"
            @click.native.prevent="handleLogin">
          登录系统
        </el-button
        >
      </el-form-item>
    </el-form>
  </div>

</template>

<script>
import { randomLenNum } from '@/util'
import { mapGetters } from 'vuex'

export default {
  name: 'userlogin',
  data() {
    return {
      loginForm: {
        username: 'admin',        //  admin
        password: '123456',
        code: '',
        randomStr: ''
      },
      checked: false,
      code: {
        src: '/code',
        value: '',
        len: 4,
        type: 'image'
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { pattern: /^([a-z\u4e00-\u9fa5\d]*?)$/, message: '请输入小写字母', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度最少为6位', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      }
    }
  },
  created() {
    if (this.website.validateCode) {
      this.refreshCode()
    }
  },
  computed: {
    ...mapGetters(['tagWel', 'website'])
  },
  methods: {
    refreshCode() {
      this.loginForm.code = ''
      this.loginForm.randomStr = randomLenNum(this.code.len, true)
      this.code.type === 'text'
          ? (this.code.value = randomLenNum(this.code.len))
          : (this.code.src = `${this.baseUrl}/code?randomStr=${this.loginForm.randomStr}`)
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.$store
              .dispatch('LoginByUsername', this.loginForm)
              .then(() => {
                this.$router.push({ path: this.tagWel.value })
              })
              .catch(() => {
                this.refreshCode()
              })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
$my_input_height: 36px;
$my_input_color: white;
.login-zone{
  width: 100%;
  height: 100%;
  display: flex;
  flex-flow: column;
  justify-content: center;        /*  水平居中  */
  .title{
    width: 100%;
    height: 80px;
    text-align: center;     //  字体水平居中
    line-height: 50px;      //  字体垂直居中
    font-size: 26px;
    color: white;
    letter-spacing: 2px;
  }
  .login-form {
    width: 50%;
    height: 500px;
    margin-left: auto;
    margin-right: auto;
    ::v-deep{
      .el-form-item {
        margin-bottom: 32px;        /*  控制输入框的行间距   */
      }
      .el-input-group__append{
        background-color: #fdfdfd;
      }
      .el-form-item__error{
        font-size: 18px;           /*  验证码输入提示字体大小  */
      }
      .el-input__wrapper{
        background-color: $my_input_color;   /*  修改输入框的背景色   */
      }
    }
    .my-input{
      height: $my_input_height;
      line-height: $my_input_height;
    }
    .login-submit{
      font-size: 20px;
      letter-spacing: 2px;
      background-color: limegreen;
      border-color: transparent;
      height: $my_input_height;
    }
    .login-code-img{
      border: 0px;
      height: $my_input_height - 2px;
    }
  }
}

</style>
