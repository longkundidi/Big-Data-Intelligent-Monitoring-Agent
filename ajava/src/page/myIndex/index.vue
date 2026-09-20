<template>
  <div class="my-avue-contail" :class="{'my-avue--collapse':isCollapse,}">
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
    validSidebar() {
      return !((this.$route.meta || {}).menu === false || (this.$route.query || {}).menu === 'false')
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







</style>



