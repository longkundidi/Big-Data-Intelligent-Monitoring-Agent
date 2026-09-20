<template>
  <div class="table-container">
    <!-- 工具栏 -->
    <div class="table-header">
      <el-button class="addBtn" @click="handleAdd">新增变量字典</el-button>
      <el-button class="reSetBtn" @click="loadDictData">刷新</el-button>
    </div>

    <!-- 变量字典表格 -->
    <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%" max-height="750px" height="750">
      <el-table-column label="序号" width="80" align="center">
        <template #default="scope">
          {{ scope.$index + 1 }} <!-- 从1开始的序号 -->
        </template>
      </el-table-column>

      <!-- 变量类型 -->
      <el-table-column prop="variableType" label="变量类型" align="center" min-width="60"/>

      <!-- 变量描述 -->
      <el-table-column prop="description" label="变量描述" align="center" min-width="180"/>

      <!-- 数据类型 -->
      <el-table-column label="数据类型" align="center" min-width="100">
        <template #default="scope">
          <el-tag
              v-for="item in scope.row.dataTypeLabels"
              :key="item.id"
              type="primary"
              style="margin: 2px;">
            {{ item.label }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 单位 -->
      <el-table-column label="单位" align="center" min-width="100">
        <template #default="scope">
          <el-tag
              v-for="item in scope.row.dimensionLabels"
              :key="item.id"
              type="success"
              style="margin: 2px;">
            {{ item.label }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 操作按钮 -->
      <el-table-column label="操作" width="300" align="center">
        <template #default="scope">
          <el-button class="editBtn" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button class="delBtn" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '新增变量字典' : '编辑变量字典'"
        width="50%"
        @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 变量类型 -->
        <el-form-item label="变量类型" prop="variableType">
          <el-input v-if="dialogType === 'add'" v-model="form.variableType" />
          <el-input v-else v-model="form.variableType" disabled />
        </el-form-item>

        <!-- 变量描述 -->
        <el-form-item label="变量描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入变量描述"/>
        </el-form-item>

        <!-- 数据类型 -->
        <el-form-item label="数据类型" prop="dataTypeLabels">
          <el-select
              v-model="form.dataTypeLabels"
              filterable
              placeholder="请选择数据类型"
              style="width: 300px;"
          >
            <el-option
                v-for="item in dataTypes"
                :key="item.id"
                :label="item.label"
                :value="item.label"
            />
          </el-select>
        </el-form-item>

        <!-- 单位 -->
        <el-form-item label="单位" prop="dimensionLabels">
          <div class="tags-container">
            <!-- 已存在的单位标签 -->
            <el-tag
                v-for="(item, index) in form.dimensionLabels"
                :key="item.id || index"
                type="success"
                closable
                @close="removeDimensionTag(index)">
              {{ item.label }}
            </el-tag>

            <!-- 添加新单位的输入框 -->
            <el-input
                v-if="dimensionInputVisible"
                ref="dimensionInputRef"
                v-model="newDimensionValue"
                size="small"
                style="width: 100px;"
                @keyup.enter="addDimensionTag"
                @blur="addDimensionTag"
            />
            <el-button v-else size="small" @click="showDimensionInput">+ 添加</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button class="disMissBtn" @click="handleDialogClose">取消</el-button>
        <el-button class="auditBtn" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {onMounted, reactive, ref, nextTick} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getDataUnitsByVarType, getDictByKey, getUnitsByVarType, updateVariableDict, isDictExist, saveVariableDict, deleteVariableDict} from "@/api/admin/dict"

export default {
  name: 'index',
  setup() {
    // 定义变量
    const loading = ref(true) // 存储表格数据
    const tableData = ref([]) // 存储表格数据
    const dialogVisible = ref(false) // 弹窗显示状态
    const dialogType = ref('add') // 弹窗类型：新增或编辑
    const formRef = ref(null) // 表单引用
    const form = reactive({
      id: '',
      variableType: '',
      description: '',
      dataTypeLabels: '',
      dimensionLabels: ''
    })

    // 字典数据
    const variableTypes = ref([]) // 变量类型字典
    const dataTypes = ref([]) // 数据类型字典
    const dimensions = ref([]) // 单位字典

    // 字典映射关系
    const dataParsedMap = ref({}) // 变量类型与数据类型的映射关系
    const parsedMap = ref({}) // 变量类型与单位的映射关系

    // 标签相关状态
    const dimensionInputVisible = ref(false)
    const newDimensionValue = ref('')
    const dimensionInputRef = ref(null)

    const rules = {
      variableType: [{ required: true, message: '变量类型是必填项', trigger: 'change' }],
      description: [{ required: true, message: '变量描述是必填项', trigger: 'blur' }],
    }

    onMounted(() => {
      loadDictData()
    })

    // 加载字典数据
    const loadDictData = async () => {
      loading.value = true;
      try {
        // 加载变量类型字典
        const variableTypeRes = await getDictByKey('chain_config_variable-type')
        if (variableTypeRes.data.code === 0) {
          variableTypes.value = variableTypeRes.data.data
        }

        // 加载数据类型字典
        const dataTypeRes = await getDictByKey('chain_config_data-type')
        if (dataTypeRes.data.code === 0) {
          dataTypes.value = dataTypeRes.data.data
        }

        // 加载所有单位字典（用于表格显示）
        const dimensionRes = await getDictByKey('chain_config_dimension')
        if (dimensionRes.data.code === 0) {
          dimensions.value = dimensionRes.data.data
        }

        // 加载变量类型与数据类型的映射关系
        const dataUnitsMapRes = await getDataUnitsByVarType()
        if (dataUnitsMapRes.data.code === 0) {
          dataParsedMap.value = dataUnitsMapRes.data.data
        }

        // 加载变量类型与单位的映射关系
        const unitsMapRes = await getUnitsByVarType()
        if (unitsMapRes.data.code === 0) {
          parsedMap.value = unitsMapRes.data.data
        }

        // 加载变量字典表格数据
        await loadTableData();
      } catch (error) {
        console.error('加载字典数据失败：', error)
        ElMessage.error('加载字典数据失败')
      } finally {
        loading.value = false
      }
    }

    // 获取表格数据
    const loadTableData = async () => {
      try {
        // 填充表格数据
        tableData.value = variableTypes.value.map(variable => {
          return {
            id: variable.id,
            variableType: variable.label,
            description: variable.description || '',
            dataTypeLabels: dataParsedMap.value[variable.value]?.map(item => ({
              id: item.id,
              label: item.label
            })) || [],
            dimensionLabels: parsedMap.value[variable.value]?.map(item => ({
              id: item.id,
              label: item.label
            })) || []
          }
        })
      } catch (error) {
        console.error('加载表格数据失败：', error)
        ElMessage.error('加载表格数据失败')
      }
    }

    // 获取字典标签
    const getDictLabel = (options, value) => {
      const item = options.find(opt => opt.value === value)
      return item ? item.label : value
    }

    // 显示单位输入框
    const showDimensionInput = () => {
      dimensionInputVisible.value = true
      nextTick(() => {
        dimensionInputRef.value.focus()
      })
    }

    // 添加单位标签
    const addDimensionTag = () => {
      if (newDimensionValue.value) {
        // 检查是否已存在相同label的标签
        const exists = form.dimensionLabels.some(item => item.label === newDimensionValue.value)
        if (!exists) {
          form.dimensionLabels.push({
            id: '',
            label: newDimensionValue.value
          })
        } else
          ElMessage.warning("已存在该单位！")
      }
      newDimensionValue.value = ''
      dimensionInputVisible.value = false
    }

    // 删除单位标签
    const removeDimensionTag = (index) => {
      form.dimensionLabels.splice(index, 1)
    }

    // 新增
    const handleAdd = () => {
      dialogType.value = 'add'
      form.id = ''
      form.variableType = ''
      form.description = ''
      form.dataTypeLabels = ''
      form.dimensionLabels = []
      dialogVisible.value = true
    }

    // 编辑
    const handleEdit = (row) => {
      dialogType.value = 'edit'
      form.id = row.id
      form.variableType = row.variableType
      form.description = row.description
      form.dataTypeLabels = row.dataTypeLabels.length
          ? row.dataTypeLabels[0].label
          : ''
      form.dimensionLabels = row.dimensionLabels ? [...row.dimensionLabels] : []
      dialogVisible.value = true
    }

    // 删除
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定删除该变量字典吗？', '删除提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })

        const variableTypeItem = variableTypes.value.find(item => item.id === row.id);

        // 处理数据类型关联
        const existingDataTypes = dataParsedMap.value?.[variableTypeItem?.value] ?? [];
        const dataTypeRelations = processRelations(
            row.id,
            [],
            existingDataTypes
        );

        // 处理单位关联
        const existingDimensions = parsedMap.value?.[variableTypeItem?.value] ?? [];
        const dimensionRelations = processRelations(
            row.id,
            [],
            existingDimensions
        );

        let variableDict = {
          variableType: variableTypeItem,
          toAddDataTypes: dataTypeRelations.toAdd,
          toRemoveDataTypes: dataTypeRelations.toRemove,
          toAddUnits: dimensionRelations.toAdd,
          toRemoveUnits: dimensionRelations.toRemove
        }

        const res = await deleteVariableDict(variableDict)
        if (res.data.code === 0) {
          ElMessage.success('删除成功')
          await loadDictData()
        } else {
          ElMessage.error(res.data.msg)
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    const processRelations = (formId, currentItems, existingItems) => {
      const existingItemIds = existingItems.map(item => item.id);
      const existingItemLabels = existingItems.map(item => item.label);

      // 需要新增的关联（包括可能需要新增的项）
      const toAdd = currentItems
          .filter(item => !existingItemIds.includes(item.id) && !existingItemLabels.includes(item.label))
          .map(item => item.label);

      // 需要删除的关联
      const toRemove = existingItems
          .filter(item => !currentItems.some(ci => ci.id === item.id))
          .map(item => ({
            parentId: formId,
            sonId: item.id
          }));

      return { toAdd, toRemove };
    };

    // 表单提交
    const handleSubmit = async () => {
      if (formRef.value) {
        try {
          await formRef.value.validate()

          // 获取变量类型
          let variableTypeItem
          if (dialogType.value === 'add')
            variableTypeItem = {
              dictKey: 'chain_config_variable-type',
              label: form.variableType,
              description: form.description
            }
          else
            variableTypeItem = variableTypes.value.find(item => item.id === form.id);

          // 处理数据类型关联
          const existingDataTypes = dataParsedMap.value?.[variableTypeItem?.value] ?? [];
          const selectedDataTypeObjs = form.dataTypeLabels
              ? [{ id: '', label: form.dataTypeLabels }]
              : []
          const dataTypeRelations = processRelations(
              form.id,
              selectedDataTypeObjs,
              existingDataTypes
          );

          // 处理单位关联
          const existingDimensions = parsedMap.value?.[variableTypeItem?.value] ?? [];
          const dimensionRelations = processRelations(
              form.id,
              form.dimensionLabels,
              existingDimensions
          );

          let variableDict = {
            variableType: variableTypeItem,
            toAddDataTypes: dataTypeRelations.toAdd,
            toRemoveDataTypes: dataTypeRelations.toRemove,
            toAddUnits: dimensionRelations.toAdd,
            toRemoveUnits: dimensionRelations.toRemove
          }

          if (dialogType.value === 'add') {
            // 新增操作
            try {
              const ifRes = await isDictExist({variableType: form.variableType})
              if (ifRes.data.code === 0) {
                if (ifRes.data.data) {
                  ElMessage.warning('该变量类型已存在！')
                  return;
                }
              } else {
                ElMessage.error('变量类型校验失败！')
                return;
              }

              const res = await saveVariableDict(variableDict)
              if (res.data.code === 0) {
                ElMessage.success('新增成功')
                dialogVisible.value = false
                await loadDictData()
              } else {
                ElMessage.error(res.data.msg)
              }
            } catch (error) {
              ElMessage.error('新增失败')
              console.error('新增失败:', error)
            }
          } else {
            // 编辑操作
            try {
              const res = await updateVariableDict(variableDict)
              if (res.data.code === 0) {
                ElMessage.success('编辑成功')
                dialogVisible.value = false
                await loadDictData()
              } else {
                ElMessage.error(res.data.msg)
              }
            } catch (error) {
              ElMessage.error('编辑失败')
              console.error('编辑失败:', error)
            }
          }
        } catch (error) {
          console.error('表单验证失败：', error)
        }
      }
    }

    const handleDialogClose = () => {
      dialogVisible.value = false
      dimensionInputVisible.value = false
      newDimensionValue.value = ''
    }

    return {
      loading,
      tableData,
      dialogVisible,
      dialogType,
      form,
      formRef,
      variableTypes,
      dataTypes,
      dimensions,
      rules,
      loadDictData,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleDialogClose,
      getDictLabel,
      dimensionInputVisible,
      newDimensionValue,
      dimensionInputRef,
      showDimensionInput,
      addDimensionTag,
      removeDimensionTag,
    }
  }
}
</script>

<style scoped>
.table-container {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.table-header {
  margin-top: 10px;
  margin-bottom: 10px;
  margin-left: 5px;
}

.el-table {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.el-table__body-wrapper {
  overflow-y: auto; /* 启用垂直滚动条 */
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  background-color: white;
}

.tags-container .el-tag {
  margin: 2px; /* 调整边距 */
}

.tags-container .el-button {
  margin: 2px;
  line-height: 30px;
}
</style>