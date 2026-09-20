<template>
  <el-select
      :model-value="modelValue"
      placeholder="请选择设备大类"
      style="width: 100%"
      @change="handleChange"
  >
    <el-option
        v-for="option in options"
        :key="option.code"
        :label="option.label"
        :value="option.label"
    />
  </el-select>
</template>

<script>
const DEVICE_CATEGORIES = [
  {label: '电梯', code: 'elevator'},
  {label: '风电', code: 'wind-power'},
  {label: '机床', code: 'machine-tool'},
  {label: '工业机器人', code: 'industrial-robot'},
  {label: '轨道交通', code: 'rail-transit'},
  {label: '通用工业设备', code: 'general-industrial-equipment'}
]

export default {
  name: 'ModelObjectSelect',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    objectId: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue', 'update:objectId', 'change'],
  data() {
    return {
      options: DEVICE_CATEGORIES
    }
  },
  methods: {
    handleChange(value) {
      const selected = this.options.find(option => option.label === value)
      this.$emit('update:modelValue', value)
      this.$emit('update:objectId', selected?.code || '')
      this.$emit('change', selected)
    }
  }
}
</script>
