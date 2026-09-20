<template>
    <div class="tablecon">
        <avue-crud ref="crudRef" class="h-avue-crud"
                   v-model="form"
                   :option="option"
                   @on-load="getList"
                   @size-change="sizeChange"
                   @current-change="currentChange"
                   @row-save="rowSave"
                   @row-update="rowUpdate"
                   @row-del="rowDel"
                   v-model:page="page"
                   :data="data">
        </avue-crud>
    </div>
</template>
<script setup>
    import { ref, getCurrentInstance, defineExpose } from 'vue'
    import { tableOption } from '@/const/crud/sw/model3d/configFailureMode/table'
    let { proxy } = getCurrentInstance()        //获取this
    import { fetchList, putObj, addObj, delObj } from '@/api/sw/model3d/configFailureMode/table.js'
    import { ElMessageBox, ElMessage } from 'element-plus'

    let globeParams = {}     //  声明一个全局参数对象

    const crudRef = ref(null)
    let option = ref(tableOption)
    let data = ref([])
    let form = ref({})
    let page = ref({
        total: 0,          // 总页数
        currentPage: 1,     // 当前页数
        pageSize: 10        // 每页显示多少条
    })

    function normalizeRecord(item) {
        return {
            ...item,
            failureId: item.failureId || item.failure_id || item.id,
            failureName: item.failureName || item.failure_name,
            failureCode: item.failureCode || item.failure_code,
            nodeId: item.nodeId || item.node_id
        }
    }

    function resolveNodeId(node) {
        if (!node) return ''
        return node.nodeId || node.node_id || node.id || ''
    }

    //  修改每页条数
    function sizeChange(pageSize){
        page.value.pageSize = pageSize
    }

    //  翻页
    function currentChange(current){
        page.value.currentPage = current
    }

    //  分页或者修改每页条数时，自动调用
    async function getList(page, params) {
        if (!globeParams.curNode)
            globeParams.curNode = proxy.$parent.getCurNode()        //  调用父组件的方法，获取当前节点
        await _refreshTableByNodeId(resolveNodeId(globeParams.curNode))
    }

    //  新增
    async function rowSave (row, done, loading) {
        try {
            if (globeParams.curNode) {
                row.nodeId = resolveNodeId(globeParams.curNode)
            }
            if (!row.nodeId) {
                ElMessage.warning('请选择一个节点')
                loading()
                return
            }

            let res = await addObj(row)
            if (res.data.code === 0){
                ElMessage.success('新增成功')
                const returnedData = res.data.data || {}
                row.failureId = returnedData.failureId || returnedData.failure_id || returnedData.id
                row.hasChildren = false
            }
            done(row)
        } catch (error) {
            console.log(error)              // 处理错误
        }
    }

    async function rowDel (row, index, done) {
        try {
            await ElMessageBox.confirm(
                '该操作将删除当前节点关联的故障模式，是否继续?','提示',{
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
            const failureId = row.failureId || row.failure_id || row.id
            let res = await delObj(failureId)
            if (res.data.code === 0) {
                ElMessage.success('删除成功')
            }
            done(row)
        } catch (error) {
            console.log(error)              // 处理错误
        }
    }

    async function rowUpdate (row, index, done, loading) {
        try {
            let res = await putObj(row)
            if (res.data.code === 0) {
                ElMessage.success('编辑成功')
            }
            done(row)
        } catch (error) {
            console.log(error)              // 处理错误
        }
    }

    function setTableRecords(records, total = 0) {
        const normalizedRecords = (records || []).map((item) => {
            const normalized = normalizeRecord(item)
            normalized.hasChildren = false
            return normalized
        })
        data.value = normalizedRecords
        page.value.total = total
    }

    function isSuccessResponse(resData) {
        if (!resData) return false
        if (resData.code === 0 || resData.code === '0') return true
        if (resData.code === 200 || resData.code === '200') return true
        if (resData.success === true) return true
        return false
    }

    function extractRecordsAndTotal(resData) {
        const payload = resData && resData.data

        if (Array.isArray(payload)) {
            return { records: payload, total: payload.length }
        }

        if (payload && Array.isArray(payload.records)) {
            return { records: payload.records, total: payload.total || payload.records.length }
        }

        if (payload && Array.isArray(payload.list)) {
            return { records: payload.list, total: payload.total || payload.list.length }
        }

        if (payload && Array.isArray(payload.content)) {
            return { records: payload.content, total: payload.totalElements || payload.content.length }
        }

        if (payload && payload.result && Array.isArray(payload.result.records)) {
            return {
                records: payload.result.records,
                total: payload.result.total || payload.result.records.length
            }
        }

        return { records: [], total: 0 }
    }

    async function queryFailureRecords(query) {
        const res = await fetchList(query)
        if (!isSuccessResponse(res.data)) {
            return { records: [], total: 0 }
        }
        return extractRecordsAndTotal(res.data)
    }

    //  根据节点Id，刷新表格数据
    async function _refreshTableByNodeId(nodeId) {
        if (!nodeId) {
            data.value = []
            page.value.total = 0
            return
        }
        const { records, total } = await queryFailureRecords({
            nodeId,
            node_id: nodeId,
            pageSize: page.value.pageSize,
            current: page.value.currentPage
        })
        setTableRecords(records, total)
    }

    /*  外部调用接口函数定义
        根据节点id，查询节点关联的故障模式记录
     */
    const getFailureMode = async (node) => {
        globeParams.curNode = node                  //  缓存当前节点
        await _refreshTableByNodeId(resolveNodeId(node))
    }

    defineExpose({ getFailureMode })            //  暴露组件接口



</script>
<style lang="scss">
    @import '@/styles/my-avue-crud.scss';
    .tablecon {
        height: 100%;
        width: 100%;
    }
</style>