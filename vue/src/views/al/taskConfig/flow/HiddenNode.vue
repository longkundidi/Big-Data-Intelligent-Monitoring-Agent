
<script setup>
const props = defineProps(['label'])
</script>
<template>
  <div v-show="false" class="node">
    {{ label }}
  </div>
</template>

<style scoped>
.node {
  border: 1px black solid;
  width: 80px;
  text-align: center;
  background-color: white;
}
</style>
