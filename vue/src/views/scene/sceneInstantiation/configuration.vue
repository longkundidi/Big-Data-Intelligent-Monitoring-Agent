<template>
  <turbine-detail :configuration-only="true" />
</template>

<script>
import TurbineDetail from '@/page/turbineDetail.vue'

export default {
  name: 'SceneInstantiationConfiguration',
  components: {
    TurbineDetail
  }
}
</script>
