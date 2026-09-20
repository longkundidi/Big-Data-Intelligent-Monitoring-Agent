<template>
    <div class="execution">
        <basic-container>
            <avue-crud ref="crud"
                       v-model:page="page"
                       :data="tableData"
                       :permission="permissionList"
                       :table-loading="tableLoading"
                       :option="option"
                       @on-load="getList"
                       @search-change="searchChange"
                       @refresh-change="refreshChange"
                       @size-change="sizeChange"
                       @current-change="currentChange"
                       @row-update="handleUpdate"
                       @row-save="handleSave"
                       @row-del="rowDel"
                       @row-click='rowClick'
                       @tree-load="treeLoad"
                       :before-open="beforeOpen">
                <template #menu="{size,row,index}">
                    <el-button :size="size" class="normalBtn" @click="$refs.crud.rowAdd(row)"><el-icon><Plus/></el-icon>新增</el-button>
                    <el-button icon="el-icon-cloudy" :size="size" class="normalBtn" @click="editTree(row)" :style="{ display: row.visible }">管理</el-button>
                    <el-button icon="el-icon-cloudy" :size="size" class="normalBtn" @click="clipMark(row)" :style="{ display: row.visible }">剖切标注</el-button>
                    <el-button icon="el-icon-cloudy" :size="size" class="normalBtn" @click="roam(row)" :style="{ display: row.visible }">漫游</el-button>
                    <el-button icon="el-icon-cloudy" :size="size" class="normalBtn" @click="decomposite(row)" :style="{ display: row.visible }">模型分解</el-button>
                    <!--<el-button icon="el-icon-cloudy" :size="size" :type="type" @click="lodDemo(row)" :style="{ display: row.visible }">LOD</el-button>
                    <el-button icon="el-icon-cloudy" :size="size" :type="type" @click="meshClean(row)" :style="{ display: row.visible }">网格简化</el-button>
                  -->
                </template>
            </avue-crud>
        </basic-container>
    </div>
</template>

<script>
    import {fetchList, getObj, addObj, putObj, delObj, getSonNode} from '@/api/sw/model3d/modelBaseInfo/m3modelbaseinfo'
    import {tableOption} from '@/const/crud/sw/model3d/modelBaseInfo/m3modelbaseinfo'
    import {mapGetters} from 'vuex'
    import { ref, onMounted } from 'vue'
    import { useRouter } from 'vue-router'
    //import router from '@/router/index.js'

    let myCurd,myRouter

    export default {
        name: 'm3modelbaseinfo',
        data() {
            return {
                searchForm: {},
                tableData: [],
                page: {
                    total: 0, // 总页数
                    currentPage: 1, // 当前页数
                    pageSize: 20 // 每页显示多少条
                },
                tableLoading: false,
                option: tableOption,
                maps: new Map(),
                curMbCode: ''         //当前选中行的模板编码
            }
        },
        setup() {
            let crud = ref(null)        //  生成一个表格树的代理对象
            myRouter = useRouter()
            onMounted(()=>{
                myCurd = crud
            })
            return { crud }
        },

        computed: {
            ...mapGetters(['permissions']),
            permissionList() {
                return {
                    addBtn: this.validData(this.permissions.modelBaseInfo_m3modelbaseinfo_add, false),
                    delBtn: this.validData(this.permissions.modelBaseInfo_m3modelbaseinfo_del, false),
                    editBtn: this.validData(this.permissions.modelBaseInfo_m3modelbaseinfo_edit, false)
                };
            }
        },
        methods: {
            //  新增元模型库时，赋初始值
            beforeOpen(done, type) {
                if (["add"].includes(type)) {
                    this.$refs['crud'].tableForm.photoFov = 45
                    this.$refs['crud'].tableForm.photoNear = 0.01
                    this.$refs['crud'].tableForm.photoFar = 2000
                    this.$refs['crud'].tableForm.photoPos = '0,  0,  4.5'
                }
                done()
            },

            rowClick(row) {
                this.curMbCode = row.mbCode
            },
            getList(page, params) {
                let self = this
                this.tableLoading = true
                fetchList(Object.assign({
                    current: page.currentPage,
                    size: page.pageSize
                }, params, self.searchForm )).then(response => {
                    for (const item of response.data.data.records){
                        item.scenePara = JSON.parse(item.scenePara)
                        item.photoFar = item.scenePara.far
                        item.photoFov = item.scenePara.fov
                        item.photoNear = item.scenePara.near
                        item.photoPos = item.scenePara.photoPosX + ', ' + item.scenePara.photoPosY + ', ' + item.scenePara.photoPosZ
                        if ((item.nodeType === 'Root')||(item.nodeType === 'Mid')){
                            item.hasChildren = true    //   vue3中不能使用 this.$set
                            item.visible = 'none'                       //  只能在叶子节点上挂载模型库
                        }
                    }
                    self.tableData = response.data.data.records
                    self.page.total = response.data.data.total
                    self.tableLoading = false
                }).catch(() => {
                    this.tableLoading=false
                })
            },
            //  加载子节点
            treeLoad(tree, treeNode, resolve) {
                let self = this
                var mbCode = tree.mbCode                      //  加载子场景时，将子节点对象缓存起来
                this.maps.set(mbCode, {tree, treeNode, resolve})
                getSonNode(tree.mbCode).then(res => {
                    res.data.data.forEach(item => {
                        item.scenePara = JSON.parse(item.scenePara)
                        item.photoFov = item.scenePara.fov
                        item.photoNear = item.scenePara.near
                        item.photoFar = item.scenePara.far
                        item.photoPos = item.scenePara.photoPosX+', ' + item.scenePara.photoPosY+', ' + item.scenePara.photoPosZ
                        if (item.nodeType === 'Mid')
                            item.hasChildren = true         //  设置下级节点加载箭头
                    })
                    resolve(res.data.data)
                    this.tableLoading = false
                }).catch(() => {
                    this.tableLoading = false
                })
            },

            //  删除
            rowDel: function (row, index) {
                if ((row.nodeType === 'Leaf')||(row.nodeType === 'Root-Leaf')){
                    this.$confirm('该操作将删除模型库关联的所有结构树节点和模型，确认删除吗？', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(function () {
                        return delObj(row.mbId)
                    }).then(data => {     //  模板信息的删除，必须首先删除根节点以下的所有节点，然后，才可以删除模板自身
                        if (data.code === 401){
                            this.$message.success(data.data.message)
                        }else{
                            this.$message.success('删除成功')
                            myCurd.value.refreshTable()             //  重新渲染表格

                            //this.updateTree(row.mbCode)
                        }
                    }).catch(cancelorerror=>{})
                }else{
                    this.$message.error('请先删除子节点，然后删除该记录！')
                }
            },

            //  编辑
            handleUpdate: function (row, index, done,loading) {
                let arrPhotoPos = row.photoPos.replace(/\s*/g, "").split(',')       //  去除空格并转化为数组
                let scenePara = {
                    fov: row.photoFov, near: row.photoNear, far: row.photoFar,
                    photoPosX: Number(arrPhotoPos[0]), photoPosY: Number(arrPhotoPos[1]), photoPosZ: Number(arrPhotoPos[2])
                }
                row.scenePara = JSON.stringify(scenePara)
                putObj(row).then(data => {
                    this.$message.success('修改成功')
                    this.updateTree(row.mbCode)
                    done()
                }).catch(() => {
                    loading();
                });
            },

            //  新增-保存 按钮
            handleSave: function (row, done,loading) {
                let arrPhotoPos = row.photoPos.replace(/\s*/g, "").split(',')       //  去除空格并转化为数组
                let scenePara = {
                    fov: row.photoFov, near: row.photoNear, far: row.photoFar,
                    photoPosX: Number(arrPhotoPos[0]), photoPosY: Number(arrPhotoPos[1]), photoPosZ: Number(arrPhotoPos[2])
                }
                row.scenePara = JSON.stringify(scenePara)
                row.pmbCode = this.curMbCode      //  保存父实例的编码
                addObj(row).then(data => {
                    this.$message.success('添加成功')
                    if (row.pmbCode) {
                        var node = this.maps.get(row.pmbCode)
                        if (node !== undefined) {
                            this.treeLoad(node.tree, node.treeNode, node.resolve)
                        } else {
                            this.updateTree(row.pmbCode)      //  如果连续添加子节点，执行树更新
                        }
                    }
                    this.getList(this.page)
                    this.curMbCode = ''
                }).catch(() => {
                    loading();
                });
                done(row)
            },

            sizeChange(pageSize){
                this.page.pageSize = pageSize
            },
            currentChange(current){
                this.page.currentPage = current
            },
            searchChange(form, done) {
                this.searchForm = form
                this.page.currentPage = 1
                this.getList(this.page, form)
                done()
            },
            refreshChange() {
                this.getList(this.page)
            },

            getBaseInfoObj(mbObj){
                return {
                    mbId: mbObj.mbId,
                    mbName: mbObj.mbName,
                    mbCode: mbObj.mbCode,
                }
            },

            //  模型库结构树管理
            editTree(mbObj) {
                /*router.push({
                    path: '/model3d/modelBaseTree/index',
                    query: {
                        mbObj: JSON.stringify(this.getBaseInfoObj(mbObj))
                    }
                })*/
                myRouter.push({
                    path: '/model3d/modelBaseTree/index',
                    query: {
                        mbObj: JSON.stringify(this.getBaseInfoObj(mbObj))
                    }
                })
            },

            //  根据节点编码，找到它的父节点，刷新父节点
            updateTree(code) {
                if (code.indexOf("-") !== -1) {
                    let pNodeCode = code.substring(0, code.lastIndexOf("-"))      //  找到父节点编码
                    var node = this.maps.get(pNodeCode)
                    let proxyTree = myCurd.value.$refs.table.store.states.lazyTreeNodeMap.value[node.tree.mbId]
                    proxyTree = []          //将对应节点node.tree.mbId下的数据清空，从而实现数据的重新加载
                    //this.$set(this.$refs.crud.$refs.table.store.states.lazyTreeNodeMap, node.tree.mbId, [])   //Vue3不能使用this.$set
                    this.treeLoad(node.tree, node.treeNode, node.resolve)
                }
                this.getList(this.page)
            },

            //  剖切与标注功能演示
            clipMark(mbObj) {
                this.$router.push({
                    path: '/model3d/modelBaseTree/clipMark/index',
                    query: {
                        mbObj: mbObj
                    }
                }).catch(() => {
                    console.log('页面未找到')
                    this.$message.error(mbObj)
                })
            },

            //  漫游
            roam(mbObj) {
                this.$router.push({
                    path: '/model3d/modelBaseTree/roam/index',
                    query: {
                        mbObj: mbObj
                    }
                }).catch(() => {
                    console.log('页面未找到')
                    this.$message.error(mbObj)
                })
            },

            //  模型轻量化多级分解
            decomposite(mbObj) {
                this.$router.push({
                    path: '/model3d/modelDeco/index',
                    query: {
                        mbObj: mbObj
                    }
                }).catch(() => {
                    console.log('页面未找到')
                    this.$message.error(mbObj)
                })
            },


        }
    }
</script>


<!--
https://blog.csdn.net/ddx2019/article/details/107817856-->
