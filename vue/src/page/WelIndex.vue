<template>
  <basic-container v-if="isReady && currentComponent">
    <component :is="currentComponent" :key="componentKey"></component>
  </basic-container>
</template>

<script>
import AdminHome from '@/page/wel.vue';
import MonitorIndex from '@/page/wel1.vue';
import DefaultHome from '@/components/ErrorPage/404.vue';

export default {
  data() {
    return {
      componentKey: 0, // 组件切换时用来强制刷新
      isReady: false // 确保用户信息和角色确定后才渲染
    };
  },
  async created() {
    this.isReady = false; // 先隐藏页面
    await this.initializeUser();
  },
  computed: {
    currentComponent() {
      const roleCodes = this.$store.state.user.roleCodes || [];

      if (roleCodes.includes('ROLE_ADMIN')) {
        return AdminHome;
      } else if (roleCodes.includes('ROLE_ALGORITHM_ADMIN')) {
        return AdminHome;
      } else if (roleCodes.includes('ROLE_SALEMAN')) {
        return MonitorIndex;
      } else if (roleCodes.includes('ROLE_SYS')) {
        return AdminHome;
      } else if (roleCodes.includes('GENERAL_USER')) {
        return MonitorIndex;
      } else {
        return DefaultHome;
      }
    }
  },
  methods: {
    async initializeUser() {
      await this.$store.dispatch('GetUserInfo');

      // 等待 roleCodes 更新并确保有值
      let retries = 0;
      const maxRetries = 5; // 最大重试次数

      while (this.$store.state.user.roleCodes.length === 0 && retries < maxRetries) {
        retries++;
        await new Promise(resolve => setTimeout(resolve, 500)); // 每500ms检查一次
      }

      if (this.$store.state.user.roleCodes.length === 0) {
        // 如果最终没有获取到roleCodes，可以采取相应措施，比如跳转到登录页
        console.error('Role codes are not available!');
      }

      this.isReady = true; // 只有在 roleCodes 确定后，才允许渲染页面
    }
  }
};
</script>

<style scoped lang="scss">
// 覆盖 basic-container 的默认样式，确保背景透明
::v-deep(.avue-contail) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
}

::v-deep(.el-card) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

::v-deep(.el-card__body) {
  padding: 0 !important;
}
</style>
