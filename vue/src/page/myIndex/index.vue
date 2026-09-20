<template>
  <div
    class="my-avue-contail"
    :class="{
      'my-avue--collapse': isCollapse,
      'elevator-board-shell': isElevatorBoardTheme,
      'route-board-fullscreen': isRouteBoardFullscreen
    }">
    <div class="my-avue-layout" :class="{'my-avue-layout--horizontal':isHorizontal}">
      <div class="my-top">
        <logo />      <!-- 顶部logo -->
        <top ref="top" />     <!-- 顶部导航栏 -->

      </div>
      <div class="my-down">
        <div v-show="validSidebar" class="my-avue-sidebar" :class="{'menu-collapse':isCollapse}">
          <!-- 左侧导航栏 -->
          <sidebar />
        </div>
        <div class="my-avue-main" :class="{'main-collapse':isCollapse}">
          <!-- 顶部标签卡 -->
          <tags />
          <!-- 主体视图层 -->
          <div v-show="!isSearch" v-if="isRefresh" id="avue-view">
            <router-view #="{ Component }">
              <keep-alive :include="$store.getters.tagsKeep">
                <component :is="Component" />
              </keep-alive>
            </router-view>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import { validatenull } from '@/util/validate'
import { mapGetters } from 'vuex'
import logo from './logo.vue'
import top from './top/index.vue'
import { checkToken } from '@/api/login.js'
import sidebar from './sidebar/index.vue'
import tags from './tags.vue'

/*
import search from './search.vue'



*/

export default {
  name: 'index',
  components: {
    logo,
    top,
    tags,
    sidebar
    /*


    search,
    */
  },
  provide() {
    return {
      index: this
    }
  },
  data() {
    return {
      //刷新token锁
      refreshLock: false,
      //刷新token的时间
      refreshTime: ''
    }
  },
  computed: {
    ...mapGetters(['isHorizontal', 'isRefresh', 'isLock', 'isCollapse', 'isSearch', 'menu']),
    isElevatorBoardTheme() {
      const roleCodes = this.$store.state.user.roleCodes || []
      return roleCodes.includes('ROLE_SALEMAN') ||
        roleCodes.includes('ROLE_SALESMAN') ||
        roleCodes.includes('GENERAL_USER')
    },
    validSidebar() {
      return !((this.$route.meta || {}).menu === false || (this.$route.query || {}).menu === 'false')
    },
    isRouteBoardFullscreen() {
      return (this.$route.meta || {}).boardFullscreen === true ||
        (this.$route.query || {}).board === 'fullscreen'
    }
  },
  created() {
    //实时检测刷新token
    this.refreshToken()
  },
  destroyed() {
    clearInterval(this.refreshTime)
  },
  methods: {
    //打开菜单
    openMenu(item = {}) {
      this.$store.dispatch('GetMenu', item.id).then(data => {
        if (data.length !== 0) {
          this.$router.$avueRouter.formatRoutes(data, true)
        }
        //当点击顶部菜单做的事件
        if (!validatenull(item)) {
          let itemActive = {}
          const childItemActive = 0
          //vue-router路由
          if (item.path) {
            itemActive = item
          } else {
            if (this.menu[childItemActive].length === 0) {
              itemActive = this.menu[childItemActive]
            } else {
              itemActive = this.menu[childItemActive].children[childItemActive]
            }
          }
          this.$store.commit('SET_MENUID', item)
          this.$router.push({
            path: itemActive.path
          })
        }

      })
    },
    refreshToken() {
      this.refreshTime = setInterval(() => {
        checkToken(this.refreshLock, this.$store)
      }, 10000)
    }
  }
}
</script>
<style lang="scss" scoped>
  @import "@/styles/my-variables.scss";
  $my-avue-main_width: $my_sidebar_width + 10px;
  .my-avue-contail {
    width: 100%;
    height: 100%;
    background-size: 100%;
    background-repeat: no-repeat;
    &.elevator-board-shell {
      background:
        radial-gradient(circle at 50% 12%, rgba(45, 166, 220, 0.16), transparent 36%),
        linear-gradient(180deg, rgba(3, 22, 45, 0.88) 0%, rgba(4, 30, 55, 0.9) 46%, rgba(2, 14, 30, 0.94) 100%),
        url('/img/wel/eva.png') center center / cover no-repeat;
      color: #ffffff;
    }
    .my-avue-layout{
      transition: all .3s;       /* 设定元素变化时的过渡效果，0.3秒过渡*/
      display: flex;
      flex-direction: column;
      height: 100%;
      overflow: hidden;
      &--horizontal {
        flex-direction: column;
      }
      .my-top{
        display: flex;
        justify-content: space-between;
        width: 100%;
        height: $my_top_height;
        overflow: hidden;
        background-color: #0050a4;
      }
      .my-down{
        display: flex;
        width: 100%;
        height: calc(100% - #{$my_top_height});
        .my-avue-main{
          height: calc(100% - 15px);
          width: calc(100% - #{$my-avue-main_width});
          transition: all .3s;
          display: flex;
          flex-direction: column;
          background-color: whitesmoke;
          //border: 2px solid yellow;
          margin-top: 5px;
          margin-left: 5px;
          margin-right: 5px;
        }
        .main-collapse{
          width: calc(100% - #{$my_sidebar_collapse});
        }
        .my-avue-sidebar{
          width: calc(#{$my_sidebar_width} - 5px);
          margin-top: 5px;
          margin-left: 5px;
          height: calc(100% - 15px);
          transition: width .2s;
          //border: 2px solid yellow;
          background-color: #d6dce5;
        }
        .menu-collapse {
          width: $my_sidebar_collapse;        /*菜单收缩后，左边的宽度*/
        }
      }
    }
  }

  .my-avue-contail.elevator-board-shell {
    .my-avue-layout {
      background:
        linear-gradient(90deg, rgba(3, 20, 42, 0.94), rgba(7, 58, 88, 0.76) 45%, rgba(3, 20, 42, 0.94)),
        url('/img/wel/bg-elevator.png') center bottom / cover no-repeat;
    }

    .my-avue-layout .my-top {
      background: linear-gradient(90deg, rgba(4, 24, 50, 0.92), rgba(9, 72, 104, 0.84), rgba(4, 24, 50, 0.92));
      border-bottom: 1px solid rgba(122, 214, 240, 0.18);
      box-shadow: 0 12px 30px rgba(0, 8, 18, 0.34);
    }

    .my-avue-layout .my-down {
      background:
        radial-gradient(circle at 58% 16%, rgba(45, 166, 220, 0.14), transparent 32%),
        linear-gradient(180deg, rgba(5, 30, 57, 0.38), rgba(2, 16, 34, 0.72));
    }

    .my-avue-layout .my-down .my-avue-sidebar {
      background: linear-gradient(180deg, rgba(6, 43, 70, 0.78), rgba(3, 24, 46, 0.88));
      border: 1px solid rgba(122, 214, 240, 0.18);
      border-radius: 8px;
      box-shadow: 0 18px 42px rgba(0, 8, 18, 0.34), inset 0 1px 0 rgba(255, 255, 255, 0.07);
      overflow: hidden;
    }

    .my-avue-layout .my-down .my-avue-main {
      background: rgba(4, 27, 52, 0.56);
      border: 1px solid rgba(122, 214, 240, 0.16);
      border-radius: 8px;
      box-shadow: 0 18px 42px rgba(0, 8, 18, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.06);
      overflow: hidden;
    }
  }

  .my-avue-contail.route-board-fullscreen {
    .my-avue-layout .my-top,
    .my-avue-layout .my-down .avue-tags {
      display: none;
    }

    .my-avue-layout .my-down {
      height: 100%;
    }

    .my-avue-layout .my-down .my-avue-main {
      width: 100%;
      height: 100%;
      margin: 0;
      border: 0;
      border-radius: 0;
      background: #07111a;
    }

    .my-avue-layout .my-down .my-avue-main #avue-view {
      flex: 1;
      min-height: 0;
      overflow: auto;
    }
  }







</style>



