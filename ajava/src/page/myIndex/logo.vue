<template>
  <div class="my-avue-logo" v-show="getScreen(!isCollapse)">
    <!--    菜单扩展    -->
    <transition-group name="fade">
      <template v-if="getScreen(!isCollapse)">
        <div class="my-avue-logo_title">
<!--          <img src="/img/myWel/dongqi_logo.png" alt="logo"/>-->
          <img style="width: 80%; height: 80%; margin-top: 8px" src="/img/myWel/swjtu_logo.png" alt="logo"/>
        </div>

<!--        <span key="1" class="my-avue-logo_title"></span>-->
      </template>
    </transition-group>

  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
  name: 'logo',
  computed: {
    ...mapGetters(['isCollapse'])
  }
}
</script>
<style lang="scss" scoped>
  @import "@/styles/my-variables.scss";
  .my-avue-logo {
    transition: all .3s;
    width: $my_top_width;
    height: $my_top_height;
    overflow: hidden;
    text-align: center;
    &_title {                       /*这里采用了BEM命名约定，&_title中的&代表它的父容器class名（my-avue-logo），因此，&_title就是my-avue-logo_title*/
      display: inline-block;
      width: 100%;
      //width: 184px;
      height: 100%;
      //background-image: url("/img/myWel/logo.png");
      img {
        height: 100%;
        width: 100%;
      }
    }
  }

</style>
