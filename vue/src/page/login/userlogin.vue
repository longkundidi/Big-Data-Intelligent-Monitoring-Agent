<template>
  <div class="login-zone">
    <div class="login-heading">
      <div class="title">故障智能诊断算法引擎</div>
      <span>账号登录</span>
    </div>
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
$input-height: 46px;

.login-zone {
  box-sizing: border-box;
  width: 100%;
  padding: 42px 42px 34px;
  border: 1px solid rgba(130, 180, 211, 0.3);
  border-radius: 6px;
  background: rgba(5, 20, 36, 0.92);
  box-shadow: 0 18px 48px rgba(0, 8, 18, 0.42);
  backdrop-filter: blur(8px);

  .login-heading {
    margin-bottom: 32px;
    padding-left: 16px;
    border-left: 3px solid #2d8fc5;

    .title {
      color: #f4f8fb;
      font-size: 25px;
      font-weight: 600;
      line-height: 1.4;
      letter-spacing: 0;
    }

    span {
      display: block;
      margin-top: 7px;
      color: rgba(220, 235, 245, 0.6);
      font-size: 13px;
    }
  }

  .login-form {
    width: 100%;
    margin: 0;

    :deep(.el-form-item) {
      margin-bottom: 20px;
    }

    :deep(.el-form-item:last-child) {
      margin-top: 8px;
      margin-bottom: 0;
    }

    :deep(.el-form-item__error) {
      padding-top: 4px;
      color: #ff8c96;
      font-size: 12px;
    }

    :deep(.el-input__wrapper) {
      min-height: $input-height;
      padding: 0 14px;
      border-radius: 4px;
      background: #f7fafc;
      box-shadow: 0 0 0 1px rgba(142, 170, 190, 0.18) inset;
      transition: box-shadow 0.18s ease;
    }

    :deep(.el-input__wrapper.is-focus) {
      box-shadow: 0 0 0 1px #2d8fc5 inset;
    }

    :deep(.el-input__inner) {
      color: #1c2e3d;
      font-size: 14px;
    }

    :deep(.el-input__prefix-inner) {
      color: #5c7485;
      font-size: 17px;
    }

    :deep(.el-input-group__append) {
      padding: 0;
      border-radius: 0 4px 4px 0;
      background: #f7fafc;
      box-shadow: none;
    }

    .my-input {
      min-height: $input-height;
    }

    .login-submit {
      width: 100%;
      height: $input-height;
      border: 1px solid #237eaf;
      border-radius: 4px;
      background: #237eaf;
      color: #fff;
      font-size: 15px;
      font-weight: 600;
      letter-spacing: 0;
      transition: background-color 0.18s ease, border-color 0.18s ease;

      &:hover,
      &:focus {
        border-color: #2e92c9;
        background: #2e92c9;
      }
    }

    .login-code-img {
      display: block;
      width: 104px;
      height: $input-height - 2px;
      padding: 0;
      border: 0;
      object-fit: cover;
    }
  }
}

@media (max-width: 700px) {
  .login-zone {
    padding: 34px 26px 28px;

    .login-heading {
      margin-bottom: 28px;

      .title {
        font-size: 22px;
      }
    }
  }
}
</style>
