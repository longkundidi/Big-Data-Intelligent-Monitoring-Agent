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
  position: relative;
  box-sizing: border-box;
  width: 100%;
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 40px clamp(40px, 7vw, 120px);
  overflow: auto;
  background-color: #07182a;
  background-image:
    linear-gradient(90deg, rgba(3, 13, 24, 0.04) 0%, rgba(3, 13, 24, 0.12) 48%, rgba(3, 13, 24, 0.56) 100%),
    url("/img/myLogin/loginBK-elevator.png");
  background-size: cover;
  background-position: center;

  .login-input {
    position: relative;
    z-index: 1;
    width: min(420px, 100%);
  }
}

@media (max-width: 700px) {
  .login-con {
    justify-content: center;
    padding: 24px 18px;
    background-position: 38% center;

    &::before {
      content: "";
      position: fixed;
      inset: 0;
      background: rgba(3, 13, 24, 0.38);
    }
  }
}
</style>

