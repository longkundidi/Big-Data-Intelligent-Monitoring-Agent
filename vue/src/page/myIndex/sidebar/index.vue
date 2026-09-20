<template>
  <el-scrollbar class="my-avue-menu">
    <div
        v-if="menu&&menu.length==0&&!isHorizontal"
        class="avue-sidebar--tip">{{ $t('menuTip') }}
    </div>
    <el-menu
        unique-opened
        :default-active="activeMenuPath"
        :mode="setting.sidebar"
        :collapse="getScreen(isCollapse)">
      <sidebar-item :menu="menu"></sidebar-item>
    </el-menu>
  </el-scrollbar>
</template>

<script>
import {mapGetters} from 'vuex'
import sidebarItem from './sidebarItem.vue'

export default {
  name: 'sidebar',
  components: {sidebarItem},
  inject: ['index'],
  computed: {
    ...mapGetters(['isHorizontal', 'setting', 'menu', 'tag', 'isCollapse', 'menuId']),
    activeMenuPath() {
      return this.$route.meta.activeMenu || this.$route.path;
    }
  },
  created() {
    this.index.openMenu()
  },
  mounted() {
    // console.log(this.menu)
  }
}
</script>
<style lang="scss" scoped>
@import "@/styles/my-variables.scss";

.my-avue-menu {
  height: 100%
}
</style>

