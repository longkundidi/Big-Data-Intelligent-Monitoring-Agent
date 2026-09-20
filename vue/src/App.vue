<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>

</template>

<script>


import { provideViewer } from '@/features/vrmViewer/viewerContext';
import { computed } from 'vue';
import { useStore } from 'vuex';

import { messages } from '@/lang/'
export default {
  name: 'App',
  setup() {
    // 提供 Viewer 上下文
    provideViewer();

    // 使用 Vuex store
    const store = useStore();

    // 计算属性
    const locale = computed(() => {
      const languageType = store.getters.language;
      return messages[languageType];
    });

    // 返回到模板中使用
    return {
      locale,
    };
  },
};
</script>

<style>
html,
body,
#app {
  width: 100%;
  height: 100%;
}
</style>
