<template>
  <div class="tablecon">
    <!-- 工具栏 -->
    <div class="table-header">
      <el-button class="addBtn" @click="handleAdd" v-if="nodeType !== 'MBOM'">新增</el-button>
      <el-button style="margin-left: 10px" class="auditBtn" @click="variableDict()">变量字典管理</el-button>
    </div>

    <!-- 主表格 -->
    <el-table
        :data="tableData"
        border
        stripe
        style="width: 100%" height="720">
      <el-table-column type="index" label="序号" width="100" align="center"/>
      <el-table-column prop="varName" label="变量名称" align="center" min-width="140"/>
      <el-table-column prop="variableType" label="变量类型" align="center">
        <template #default="scope">
          {{ getDictLabel(variableTypeOptions, scope.row.variableType) }}
        </template>
      </el-table-column>
      <el-table-column prop="dataType" label="数据类型" align="center">
        <template #default="scope">
          {{ getDictLabel(dataTypeOptions, scope.row.dataType) }}
        </template>
      </el-table-column>
      <el-table-column prop="dimension" label="单位" align="center">
        <template #default="scope">
          {{ getDictLabel(dimensionOptions, scope.row.dimension) }}
        </template>
      </el-table-column>
      <el-table-column prop="collectionFrequency" label="采集频率(Hz)" align="center"/>
      <el-table-column prop="purpose" label="采集用途" align="center" width="90" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" min-width="120" align="center" v-if="nodeType !== 'MBOM'">
        <template #default="scope">
          <el-button class="editBtnCustom" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button class="delBtnCustom" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '新增' : '编辑'"
        width="50%"
        class="h-win-avue-crud custom-dialog"
        @close="handleDialogClose"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
      >
        <!-- 第一行：变量名称 + 变量类型 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="变量类型" prop="variableType">
              <el-select
                  v-model="form.variableType"
                  placeholder="请选择变量类型"
                  clearable
                  style="width: 100%"
                  @change="handleVariableTypeChange"
              >
                <el-option
                    v-for="item in variableTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变量名称" prop="varName">
              <el-input v-model="form.varName" placeholder="请输入变量名称"/>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：数据类型 + 单位 + 采集频率 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="数据类型" prop="dataType">
              <el-select
                  v-model="form.dataType"
                  placeholder="请选择数据类型"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="item in currentDataTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位" prop="dimension">
              <el-select
                  v-model="form.dimension"
                  placeholder="请选择单位"
                  clearable
                  style="width: 100%"
              >
                <el-option
                    v-for="item in currentDimensionOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采集频率(Hz)" prop="collectionFrequency">
              <el-input-number
                  v-model="form.collectionFrequency"
                  :min="1"
                  :precision="2"
                  :step="0.1"
                  style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：采集用途 -->
        <el-form-item label="采集用途" prop="purpose">
          <el-input
              v-model="form.purpose"
              type="textarea"
              :rows="3"
              placeholder="请输入采集用途"
          />
        </el-form-item>
      </el-form>

      <template #footer>
    <span class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import {ref, reactive, onMounted, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  putObj,
  addObj,
  delObj,
  getPageByNodeType,
  getAllByNodeType,
  delModelVar
} from '@/api/sw/model3d/configPerceivedVariable/table.js'
import {getDictByKey, getUnitsByVarType, getDataUnitsByVarType} from "@/api/admin/dict"
import {useRouter} from "vue-router";

export default {
  name: 'ConfigSceneModelTable',
  props: {
    nodeType: {
      type: String,
      required: true
    }
  },
  setup(props) {
    // 数据定义
    const tableData = ref([])
    const dialogVisible = ref(false)
    const dialogType = ref('add')
    const formRef = ref(null)
    const currentRow = ref(null)
    const globeParams = reactive({
      curNode: null
    })

    // 分页配置
    const page = reactive({
      total: 0,
      currentPage: 1,
      pageSize: 10
    })

    // 表单数据
    const form = reactive({
      varId: '',
      varName: '',
      variableType: '',
      dataType: '',
      dimension: '',
      collectionFrequency: 1.0,
      purpose: '',
      nodeId: ''
    })

    // 表单校验规则
    const rules = {
      varName: [{required: true, message: '变量名称是必须的', trigger: 'blur'}],
      variableType: [{required: true, message: '变量类型是必须的', trigger: 'change'}],
    }

    // 字典选项
    const variableTypeOptions = ref([])
    const dataTypeOptions = ref([])
    const dimensionOptions = ref([])
    const currentDimensionOptions = ref([])
    const currentDataTypeOptions = ref([])
    const dataParsedMap = ref({})
    const parsedMap = ref({})

    const router = useRouter();

    const variableDict = () => {
      router.push({
        path: '/variableDictionary/index',
      }).catch(() => {
        ElMessage.error('页面未找到');
      });
    };

    // 加载字典数据
    const loadDictData = async () => {
      try {
        // 加载变量类型字典
        const variableTypeRes = await getDictByKey('chain_config_variable-type')
        if (variableTypeRes.data.code === 0) {
          variableTypeOptions.value = variableTypeRes.data.data
        }

        // 加载数据类型字典
        const dataTypeRes = await getDictByKey('chain_config_data-type')
        if (dataTypeRes.data.code === 0) {
          dataTypeOptions.value = dataTypeRes.data.data
        }

        // 加载所有单位字典（用于表格显示）
        const dimensionRes = await getDictByKey('chain_config_dimension')
        if (dimensionRes.data.code === 0) {
          dimensionOptions.value = dimensionRes.data.data
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
      } catch (error) {
        console.error('加载字典数据失败：', error)
        ElMessage.error('加载字典数据失败')
      }
    }

    // 获取字典标签
    const getDictLabel = (options, value) => {
      const item = options.find(opt => opt.value === value)
      return item ? item.label : value
    }

    // 加载表格数据
    const loadTableData = async () => {
      try {
        if (!globeParams.curNode) {
          return
        }
        const res = await getAllByNodeType({
          nodeType: props.nodeType,
          nodeId: globeParams.curNode.id,

        })
        if (res.data.code === 0) {
          tableData.value = res.data.data
        }
      } catch (error) {
        console.error('加载表格数据失败：', error)
        ElMessage.error('加载表格数据失败')
      }
    }

    // 变量类型变化处理
    const handleVariableTypeChange = (value) => {
      const unitList = parsedMap.value[value] || []
      const dataUnitList = dataParsedMap.value[value] || []
      currentDimensionOptions.value = unitList
      currentDataTypeOptions.value = dataUnitList
      form.dimension = unitList.length > 0 ? unitList[0].value : ''
      form.dataType = dataUnitList.length > 0 ? dataUnitList[0].value : ''
    }

    // 新增按钮点击
    const handleAdd = () => {
      dialogType.value = 'add'
      dialogVisible.value = true
      Object.keys(form).forEach(key => {
        form[key] = (key === 'collectionFrequency') ? 1.0 : ''
      })
      if (globeParams.curNode) {
        form.nodeId = globeParams.curNode.id
      }
      currentDimensionOptions.value = []
      currentDataTypeOptions.value = []
    }

    // 编辑按钮点击
    const handleEdit = (row) => {
      dialogType.value = 'edit'
      dialogVisible.value = true
      currentRow.value = row
      Object.assign(form, row)
      // 设置当前可选的单位列表
      const unitList = parsedMap.value[form.variableType] || []
      const dataUnitList = dataParsedMap.value[form.variableType] || []
      currentDimensionOptions.value = unitList
      currentDataTypeOptions.value = dataUnitList
    }

    // 删除按钮点击
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('该操作将删除当前节点及机型对应节点关联的感知变量，是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        let res = null
        if (props.nodeType === 'GBOM') {
          res = await delObj(row.varId)
        } else {
          res = await delModelVar(row.id)
        }

        if (res.data.code === 0) {
          ElMessage.success('删除成功')
          await loadTableData()
        }
      } catch (error) {
        if (error !== 'cancel')
          console.error('删除失败：', error)
      }
    }

    // 表单提交
    const handleSubmit = async () => {
      if (!formRef.value) return

      try {
        await formRef.value.validate()
        let res = null

        if (dialogType.value === 'add') {
          res = await addObj(form)
        } else {
          res = await putObj(form)
        }

        if (res.data.code === 0) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
          dialogVisible.value = false
          await loadTableData()
        }
      } catch (error) {
        console.error('提交失败：', error)
      }
    }

    // 弹窗关闭处理
    const handleDialogClose = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
      currentDimensionOptions.value = []
      currentDataTypeOptions.value = []
    }

    // 分页处理
    const handleSizeChange = (val) => {
      page.pageSize = val
      loadTableData()
    }

    const handleCurrentChange = (val) => {
      page.currentPage = val
      loadTableData()
    }

    // 对外暴露的刷新方法
    const getVariable = async (node, nodeType) => {
      globeParams.curNode = node
      await loadTableData()
    }

    // 初始化
    onMounted(() => {
      loadDictData()
    })

    return {
      // 数据
      tableData,
      dialogVisible,
      dialogType,
      formRef,
      form,
      rules,
      page,
      variableTypeOptions,
      dataTypeOptions,
      dimensionOptions,
      currentDimensionOptions,
      currentDataTypeOptions,
      // 方法
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleDialogClose,
      handleSizeChange,
      handleCurrentChange,
      handleVariableTypeChange,
      getDictLabel,
      getVariable,
      variableDict
    }
  }
}
</script>

<style lang="scss" scoped>
.tablecon {
  height: 100%;
  width: 100%;
  overflow-y: hidden;


  .table-header {
    margin-top: 14px;
    margin-bottom: 16px;
    margin-left: 5px;
  }

  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  :deep(.custom-dialog) {
    .el-dialog__body {
      padding: 2px 4px;
    }

    .el-form-item {
      margin-bottom: 20px;
    }

    .dialog-footer {
      padding: 20px 0;
      text-align: right;
    }

    .el-dialog__title {
      font-size: 2em; /* 或者使用具体像素，如 32px，接近 h1 的大小 */
      font-weight: bold; /* h1 默认加粗 */
      line-height: 1.2;
    }
  }

  ::v-deep(.el-table thead) {
    font-weight: bold;
  }
}

.editBtnCustom {
  color: rgba(69, 159, 252); /* 字体蓝色 */
  border: none; /* 去掉边框 */
  background: transparent; /* 背景透明，防止有默认背景 */
  cursor: pointer; /* 鼠标变成手型，更像按钮 */
  padding: 0; /* 根据需要调整内边距 */
}

.delBtnCustom {
  color: rgba(69, 159, 252); /* 字体蓝色 */
  border: none; /* 去掉边框 */
  background: transparent; /* 背景透明，防止有默认背景 */
  cursor: pointer; /* 鼠标变成手型，更像按钮 */
  padding: 0; /* 根据需要调整内边距 */
}
</style>
