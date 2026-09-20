<template>
  <div class="login-con">
    <div class="login-input">
      <userLogin></userLogin>
    </div>
  </div>
</template>
<script>
  import userLogin from './userlogin.vue'
  import { mapGetters } from 'vuex'

  export default {
    name: 'login',
    components: {
      userLogin
    },
    data() {
      return {

      }
    },
    computed: {
      ...mapGetters(['website'])
    }
  }
</script>
<style lang="scss" scoped>
  .login-con {
    width: 100%;
    height: 100%;
    position: relative;
    background-image: url("/img/myLogin/loginBK.png");
    background-size: cover;
    .login-input{
      width: 40%;
      height: 55%;          /*  控制登录框在屏幕的高度位置 */
      position: absolute;
      right: 0;
      bottom: 0;
    }
  }
</style>

