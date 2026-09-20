<template>
    <div class="mycontainer">
        <div class="gbom-manage-layout">
            <div class="gbom-tree-card">
                <sw-tree class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
                              :defaultProps="defaultProps" :expandNode="true"
                              :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
                              :showCheckBox=false :checkStrictly=true :isLazy=false
                              @eventNodeClick="handleNodeClick" @nodeExpand="handleNodeExpand"
                              @eventNodeAdd="dlgAddSonNode" @eventNodeEdit="dlgEditNode"
                              @eventNodeDelete="dlgDeleteNode" @eventNodeCopy="dlgCopyNode"
                              @eventNodeMove="dlgMoveNode">
                </sw-tree>
            </div>
            <div class="gbom-point-card">
                <div class="model-container">
                    <div class="model-stage">
                        <img
                            class="device-model-img"
                            :src="deviceImageSrc"
                            alt="电梯制动器测点示意图"
                        />
                        <div
                            v-for="point in imagePointBindings"
                            :key="point.id"
                            class="sensor-point"
                            :style="{ left: point.x + '%', top: point.y + '%' }"
                            :class="{
                                disabled: !point.node,
                                active: isPointActive(point),
                                related: isPointRelated(point)
                            }"
                            @mouseenter="handleImagePointMouseEnter(point)"
                            @mouseleave="handleImagePointMouseLeave"
                            @click="handleImagePointClick(point)"
                            :title="point.label"
                        >
                            <span class="point-ring" :class="{ active: isPointActive(point), related: isPointRelated(point), disabled: !point.node }"></span>
                            <span class="point-dot" :class="{ active: isPointActive(point), related: isPointRelated(point), disabled: !point.node }"></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <el-dialog v-model="dlgNodeAddEdit" :title="titleName"
                    :close-on-click-modal="false" width="50%" draggable>
            <div class="content">
                <div>
                    <avue-form ref="form" :option="nodeOption" v-model="nodeForm"
                               @submit="handleSubmit">
                    </avue-form>
                </div>
            </div>
        </el-dialog>
        <el-dialog v-model="dlgNodeMove" title="选择目标节点" width="50%" draggable @close="treeDialogClose">
            <div class="treeDialog" >
                <node-move-tree ref="refNodeTree" style="width: 100% ;height: 90%" :scene-id="sceneId"></node-move-tree>
                <el-button class="normalBtn" @click.stop="doNodeMove()">确定</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
    import swTree from '@/components/myComponent/swTree.vue'
    import {
      reqAddRootNode,
      reqTreeNodes,
      reqSonNodes,
      reqAddSonNode,
      reqObjById,
      reqPutObj,
      reqDeleteNodes,
      reqCopyNode,
      reqMoveNode,
      reqDeleteNodesBySceneId,
      reqSonNodesBySceneId,
      reqTreeNodesBySceneId
    } from '@/api/sw/model3d/configGbomTree/index.js'
    import { nodeOption } from '@/const/crud/sw/model3d/configGbomTree'
    import {onMounted, ref} from 'vue'
    import nodeMoveTree from './treeForNodeMove.vue'
    import {getMetaModelIdByUserId, getProjects} from "@/api/diagnosis/graphVis/graphVisPro";

    let __tree
    let globeParams = {}     //  声明一个全局参数对象

    export default {
        name: 'gbomTree',

        data() {
            return {
                treeData: [],
                defaultProps: {
                    children: 'children',
                    label: 'name',
                    isLeaf: 'leaf'
                },
                defaultExpandKeys: [],

                dlgNodeAddEdit: false,          //  控制新增、编辑按钮点击后，弹窗的显示和隐藏
                titleName: '',
                nodeOption: nodeOption,
                nodeForm: {},
                dlgNodeMove: false,             //  控制节点移动前，弹窗的显示和隐藏
                sceneId: '',
                expandKeys: [],
                deviceImageSrc: '/img/myWel/elevator-brake-real.png',
                activeGbomNodeId: '',
                hoverGbomNodeId: '',
                activeGbomNode: null,
                imagePointBindings: [],
                imagePointLayout: [
                    { id: 'upper', x: 10.6, y: 63.8 },
                    { id: 'left', x: 61.1, y: 66.2 },
                    { id: 'center', x: 54.8, y: 22.1 },
                    { id: 'right', x: 87.2, y: 57.8 }
                ],
                imagePointKeywordMap: {
                    upper: ['抱闸', '制动臂', '线圈', '电磁铁', '上', '测点1'],
                    left: ['左', '制动器', '闸瓦', '制动块', '支架', '测点2'],
                    center: ['制动轮', '轮', '主轴', '盘', '中心', '测点3'],
                    right: ['右', '轴承', '支座', '端盖', '测点4']
                },
                imagePointFallbackKeywordMap: {
                    upper: ['top', 'coil', 'line', 'electromagnet', 'sensor'],
                    left: ['left', 'arm', 'shoe', 'clamp', 'sensor'],
                    center: ['center', 'wheel', 'shaft', 'disk', 'encoder'],
                    right: ['right', 'bearing', 'seat', 'cover', 'sensor4']
                },

            }
        },

        components: {
            swTree,
            nodeMoveTree
        },

        setup() {
            let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
            onMounted(()=>{
                __tree = lazyTree
            })
            return { lazyTree }
        },
        mounted() {
              this.initParams()
              this.refreshImagePointBindings()
              this.projectInit().then(() => {
                this.getTreeNodes(globeParams.nodeLevel)
              })
        },
        methods: {
            //  初始化全局参数
            initParams(){
                globeParams.nodeLevel = 3       //    预先展开3层节点
            },

            //  节点点击消息响应
            handleNodeClick(data, node, treeNode, event){
                this.handleGbomNodeClick(data)
            },

            async projectInit() {
              const userId = this.$store.state.user.userInfo.userId
              return getMetaModelIdByUserId({ userId, userRole: 'GENERAL_USER' })
                  .then(response => {
                    if (response.data.code === 0) {
                      this.sceneId = response.data.data.metaModelId;
                      console.log("sceneId:", this.sceneId);
                    }
                  });
            },


          //  节点扩展消息响应
            async handleNodeExpand(data, node, treeNode) {
                if ((node.level >= globeParams.nodeLevel)||(this._sonNodeHasLoading(data))) {
                    let response = await reqSonNodesBySceneId(this.sceneId,data.nodeCode)
                    if (response.data.data) {
                        response.data.data.forEach((item) => {
                            item.showAdd = true
                            item.showRemove = true
                            item.showEdit = true
                            item.showCopy = true
                            item.showMove = true
                            item.showHelp = true
                            item.children = item.leaf ? [] : [{id:'loading',name:'节点加载中...'}]
                        })
                        // 只加载sceneId相同的子节点
                        node.data.children = response.data.data.filter(item => item.sceneId === data.sceneId)
                        this.$nextTick(() => this.refreshImagePointBindings())
                    }
                }
            },

            // 根据节点层级数，加载结构树的一组节点
            async getTreeNodes(nodeLevel) {
                try {
                    let response = await reqTreeNodesBySceneId(this.sceneId, nodeLevel)
                    if ((response.data.data)&&(response.data.data.length > 0)) {
                        this.treeData = []
                        this.expandKeys = []            //  缓存待扩展的节点
                        // 先找出所有根节点（sceneId不同的多棵树）
                        let rootNodes = response.data.data.filter(ele => (ele.nodeType === "Root" || ele.nodeType === "Root-Leaf"))
                        for (let item of rootNodes) {
                            item.showAdd = true
                            item.showRemove = true
                            item.showEdit = true
                            item.showHelp = true
                            this.treeData.push(item)
                            this.expandKeys.push(item.id)
                            // 递归时只处理同sceneId的节点
                            await this.setChildren(item, response.data.data.filter(n => n.sceneId === item.sceneId))
                        }
                        this.defaultExpandKeys = this.expandKeys            //  扩展节点
                        this.$nextTick(() => this.refreshImagePointBindings())
                    } else {
                        let confirmResult = await this.$confirm('首次编辑GBOM，系统未找到根节点，是否创建根节点?', '提示', {
                            confirmButtonText: '确定',
                            cancelButtonText: '取消',
                            type: 'warning'
                        })
                        if (confirmResult) {
                            let addRootNodeResponse = await reqAddRootNode({nodeName:'模版根节点',nodeCode:'TM1'} )
                            if (addRootNodeResponse.data.code === 200){
                                this.$message.success('默认根节点创建成功')
                                this.getTreeNodes(globeParams.nodeLevel)
                            }
                        }
                    }
                } catch (error) {
                    console.log(error)
                }
            },

            // 递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
            setChildren(pNode, nodeList){
                // 只递归同sceneId的节点
                let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList.filter(n => n.sceneId === pNode.sceneId))
                let children = res.sonNodes
                if (children.length === 0){
                    if ((pNode.nodeType === 'Mid')||(pNode.nodeType === 'Root')){
                        this.expandKeys = this.expandKeys.filter(item => item !== pNode.id)
                        pNode.children = [{id:'loading',name:'节点加载中...'}]
                    }
                    return pNode
                }
                else{
                    pNode.children = children
                    children.forEach((item) => {
                        item.showAdd = true
                        item.showRemove = true
                        item.showEdit = true
                        //item.showCopy = true
                        //item.showMove = true
                        item.showHelp = true
                        this.expandKeys.push(item.id)
                        this.setChildren(item, res.otherNodes)
                    })
                }
            },

            // 正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
            getChildrenByNodeCode(pNodeCode, nodeList){
                let sonNodes = []
                let otherNodes = []
                let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
                nodeList.forEach((item) => {
                    if (regex.test(item.nodeCode))
                        sonNodes.push(item)
                    else
                        otherNodes.push(item)
                })
                return {sonNodes:sonNodes, otherNodes:otherNodes}
            },


            //  弹窗添加子节点
            dlgAddSonNode (pNode, pData) {
                this.titleName = "新增子节点"
                this.nodeForm = {              //  节点新增弹框的表单对象
                    nodeId: null,
                    nodeName: '',
                    swsort: '',
                    memo: '',
                    sceneId: pData.sceneId
                },
                this.dlgNodeAddEdit = true
            },

            handleSubmit(nodeForm, done) {
                if (this.titleName === '新增子节点')
                    this.nodeAdd(nodeForm, done)
                else if (this.titleName === '编辑节点')
                    this.nodeEdit(nodeForm, done)
            },

            //  节点子添加
            async nodeAdd(nodeForm, done) {
                let pNode = __tree.value.getCurrentNodeData()     // 获取父节点
                try {
                    let response = await reqAddSonNode(pNode.id, nodeForm)
                    if (response.data.code === 200){
                        this.$message.success('子节点添加成功')
                        let newNode = response.data.data        //  给新添加的节点增加按钮属性
                        newNode.showAdd = true
                        newNode.showRemove = true
                        newNode.showEdit = true
                      //  newNode.showCopy = true
                       // newNode.showMove = true
                        newNode.showHelp = true
                        newNode.children = []          //  注意：新添加的节点都是叶子节点
                        pNode.children.push(newNode)
                        this.$nextTick(() => {
                            __tree.value.setNodeSelected(newNode.id)       //  结构树渲染成功后，将新添加的节点设置为选中状态
                        })
                    }
                    this.dlgNodeAddEdit = false
                    done()
                } catch (error) {
                    console.log(error)
                }
            },


            dlgEditNode(node, data) {
              if (data.usedCount !== 0) {
                this.$message.warning('当前节点被机型结构树使用，不可编辑')
                return
              }
                this.titleName = "编辑节点"
                reqObjById(data.id).then(response => {
                    this.nodeForm = response.data.data          //  填写可编辑字段
                    this.dlgNodeAddEdit = true
                })
            },

            //  节点编辑
            async nodeEdit(nodeForm, done) {
                let response = await reqPutObj(nodeForm)
                if (response.data.code === 200) {
                    this.$message.success('节点编辑成功')
                    this.dlgNodeAddEdit = false
                    if ((response.data.data.nodeType !== 'Root')&&(response.data.data.nodeType !== 'Root-Leaf')) {       //  如果存在父节点
                        let newData = {
                            id: response.data.data.id,
                            name: response.data.data.name,
                            memo: response.data.data.memo
                        }
                        __tree.value.updateNode(newData)            //  更新节点状态
                    }else
                        this.getTreeNodes(globeParams.nodeLevel)
                }
                done()
            },


            async dlgDeleteNode(data) {
              if (data.usedCount !== 0) {
                this.$message.warning('当前节点被机型结构树使用，不可删除')
                return
              }
                try {
                    await this.$confirm('此操作将删除当前节点及其所有子节点, 是否继续?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    let response = await reqDeleteNodesBySceneId(data.sceneId,data.nodeCode)
                    if (response.data.code !== 208) {
                        this.$message({
                            type: 'info',
                            message: response.data.message
                        })
                    } else {
                        if ((data.nodeType !== 'Root')&&(data.nodeType !== 'Root-Leaf')){       //  如果存在父节点
                            let pNode = __tree.value.removeSonNode(data.id)
                            __tree.value.setNodeSelected(pNode.data.id)             //  将父节点设置为选中状态
                        }else
                            this.treeData = []
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                    }
                } catch (error) {
                    this.$message({
                        type: 'info',
                        message: '已取消删除'
                    })
                }
            },


            async dlgCopyNode(data) {
                try {
                    await this.$confirm('此操作将复制当前节点及其所有子节点到同一个父节点下, 是否继续?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    let response = await reqCopyNode(data.nodeCode)
                    if (response.data.code === 200) {
                        let newNode = response.data.data        //  给新复制的节点增加按钮属性
                        newNode.showAdd = true
                        newNode.showRemove = true
                        newNode.showEdit = true
                       // newNode.showCopy = true
                       // newNode.showMove = true
                        newNode.showHelp = true
                        newNode.children = newNode.nodeType === 'Leaf' ? [] : [{id:'loading',name:'节点加载中...'}]          //  注意：这里新复制的节点可能没有完成子节点的加载
                        let pNode = __tree.value.getParentNode(data.id)         //  获取父节点
                        __tree.value.addSonNode(pNode.data, newNode)
                        this.$message.success('节点复制成功')
                    }
                }catch (error) {
                    console.log(error)
                }
            },

            dlgMoveNode(data) {
                this.sourceNode = data      //  保存源节点
                this.sceneId = data.sceneId
                this.dlgNodeMove = true
            },

            async doNodeMove(){
                let targetNode = this.$refs.refNodeTree.getCurrentNode()        //  获取目标节点
                if (targetNode){
                    this.dlgNodeMove = false
                    await this.$confirm('确定执行节点移动操作吗？', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    })
                    let response = await reqMoveNode(this.sourceNode.nodeCode,targetNode.nodeCode)
                    if (response.data.code === 200){
                        await this.getTreeNodes(response.data.data.nodeLevel)                    //  更新结构树，注意：这里需要根据被移动节点的最新层级决定加载的节点级数，确保被移动节点被加载进结构树
                        __tree.value.setNodeSelected(response.data.data.id)                      //  将被移动的节点设置为选中状态
                        this.$message.success('节点移动成功')
                    }
                }
            },
          getFlattenGbomNodes() {
              const list = []
              const loop = (nodes = []) => {
                  nodes.forEach(node => {
                      if (!node || String(node.id || '').startsWith('loading')) return
                      list.push(node)
                      if (Array.isArray(node.children) && node.children.length) {
                          loop(node.children)
                      }
                  })
              }
              loop(this.treeData)
              return list
          },

          getNodeIdentity(nodeData) {
              return String(nodeData?.id || nodeData?.nodeId || '')
          },

          getGbomNodeLabel(nodeData) {
              return nodeData?.name || nodeData?.nodeName || nodeData?.nodeCode || '未匹配测点'
          },

          normalizeSearchText(text) {
              return String(text || '').trim().toLowerCase()
          },

          isGbomLeafNode(nodeData) {
              const nodeType = String(nodeData?.nodeType || '').toLowerCase()
              if (nodeType.includes('leaf')) return true
              if (typeof nodeData?.leaf === 'boolean') return nodeData.leaf
              return !Array.isArray(nodeData?.children) || nodeData.children.length === 0
          },

          findBestNodeByKeywords(keywordList = [], allNodes = [], usedNodeIds = new Set()) {
              if (!keywordList.length || !allNodes.length) return null
              const normalizedKeywords = keywordList.map(keyword => this.normalizeSearchText(keyword)).filter(Boolean)
              if (!normalizedKeywords.length) return null

              const scored = allNodes.map(node => {
                  const nodeId = this.getNodeIdentity(node)
                  if (!nodeId || usedNodeIds.has(nodeId)) return null
                  const text = this.normalizeSearchText(`${node.name || ''} ${node.nodeName || ''} ${node.nodeCode || ''}`)
                  let score = 0
                  normalizedKeywords.forEach(keyword => {
                      if (text.includes(keyword)) score += 2
                  })
                  if (this.isGbomLeafNode(node)) score += 1
                  return { node, score }
              }).filter(item => item && item.score > 0)

              if (!scored.length) return null
              scored.sort((a, b) => b.score - a.score)
              return scored[0].node
          },

          getAvailableImageBindingNodes(allNodes = []) {
              if (!allNodes.length) return []
              const leafNodes = allNodes.filter(node => this.isGbomLeafNode(node))
              const sourceNodes = leafNodes.length ? leafNodes : allNodes
              const hints = ['测点', '传感', 'sensor', 'temp', 'speed', 'vibration', 'current', 'voltage', 'position']
              const hintedNodes = sourceNodes.filter(node => {
                  const text = this.normalizeSearchText(`${node.name || ''} ${node.nodeName || ''} ${node.nodeCode || ''}`)
                  return hints.some(keyword => text.includes(keyword))
              })
              return hintedNodes.length ? hintedNodes : sourceNodes
          },

          getFallbackNodeForImagePoint(candidateNodes = [], usedNodeIds = new Set()) {
              for (let i = 0; i < candidateNodes.length; i += 1) {
                  const node = candidateNodes[i]
                  const nodeId = this.getNodeIdentity(node)
                  if (!nodeId || usedNodeIds.has(nodeId)) continue
                  return node
              }
              return null
          },

          truncatePointLabel(label) {
              const text = String(label || '')
              return text.length > 18 ? `${text.slice(0, 18)}...` : text
          },

          refreshImagePointBindings() {
              const allNodes = this.getFlattenGbomNodes()
              const candidateNodes = this.getAvailableImageBindingNodes(allNodes)
              const usedNodeIds = new Set()
              this.imagePointBindings = this.imagePointLayout.map(layout => {
                  const keywordList = [
                      ...(this.imagePointKeywordMap[layout.id] || []),
                      ...(this.imagePointFallbackKeywordMap[layout.id] || [])
                  ]
                  let matchedNode = this.findBestNodeByKeywords(keywordList, candidateNodes, usedNodeIds)
                  if (!matchedNode) {
                      matchedNode = this.getFallbackNodeForImagePoint(candidateNodes, usedNodeIds)
                  }
                  const matchedNodeId = this.getNodeIdentity(matchedNode)
                  if (matchedNodeId) {
                      usedNodeIds.add(matchedNodeId)
                  }
                  return {
                      ...layout,
                      node: matchedNode,
                      label: matchedNode ? this.truncatePointLabel(this.getGbomNodeLabel(matchedNode)) : '未匹配测点'
                  }
              })
          },

          isPointActive(point) {
              const pointId = this.getNodeIdentity(point?.node)
              return !!pointId && pointId === this.activeGbomNodeId
          },

          isPointRelated(point) {
              const pointId = this.getNodeIdentity(point?.node)
              if (!pointId) return false
              return pointId === this.activeGbomNodeId || pointId === this.hoverGbomNodeId
          },

          handleImagePointMouseEnter(point) {
              this.hoverGbomNodeId = this.getNodeIdentity(point?.node)
          },

          handleImagePointMouseLeave() {
              this.hoverGbomNodeId = ''
          },

          resolveTreeCurrentKey(nodeData) {
              if (nodeData?.id !== undefined && nodeData?.id !== null) {
                  return nodeData.id
              }
              const nodeInnerId = String(nodeData?.nodeId || '')
              if (!nodeInnerId) return null
              const matched = this.getFlattenGbomNodes().find(item => String(item?.nodeId || '') === nodeInnerId)
              return matched?.id ?? null
          },

          handleImagePointClick(point) {
              if (!point?.node) return
              const treeCurrentKey = this.resolveTreeCurrentKey(point.node)
              if (__tree?.value && treeCurrentKey !== undefined && treeCurrentKey !== null) {
                  __tree.value.setNodeSelected(treeCurrentKey)
              }
              this.handleGbomNodeClick(point.node)
          },

          handleGbomNodeClick(data) {
              this.activeGbomNodeId = this.getNodeIdentity(data)
              this.activeGbomNode = data
          },

          treeDialogClose() {
              this.$refs.refNodeTree.clearSelected()
          }
        }

    }
</script>
<style lang="scss" scoped>
    @import '@/styles/my-dialog.scss';
    .mycontainer {
        width: 100%;
        height: calc(100% - 15px);
        padding: 12px;
        background:
            radial-gradient(circle at 74% 18%, rgba(46, 166, 220, 0.16), transparent 32%),
            linear-gradient(135deg, rgba(4, 28, 54, 0.92), rgba(2, 13, 29, 0.96));
        overflow: hidden;
    }

    .gbom-manage-layout {
        display: grid;
        grid-template-columns: minmax(360px, 0.88fr) minmax(520px, 1.12fr);
        gap: 14px;
        height: 100%;
        min-height: 0;
    }

    .gbom-tree-card,
    .gbom-point-card {
        min-height: 0;
        border: 1px solid rgba(122, 214, 240, 0.16);
        border-radius: 8px;
        background: linear-gradient(180deg, rgba(7, 50, 78, 0.64), rgba(3, 25, 48, 0.82));
        box-shadow: 0 16px 36px rgba(0, 8, 18, 0.26), inset 0 1px 0 rgba(255, 255, 255, 0.05);
        overflow: hidden;
    }

    .gbom-tree-card {
        display: flex;
        flex-direction: column;
    }

    .gbom-point-card {
        position: relative;
        display: flex;
        align-items: stretch;
        justify-content: center;
        padding: 0;
        background:
            radial-gradient(circle at 56% 46%, rgba(50, 178, 205, 0.18), transparent 48%),
            linear-gradient(180deg, rgba(3, 57, 75, 0.74), rgba(1, 24, 34, 0.94));
    }

    .tyTree {
        flex: 1;
        min-height: 0;
        padding: 12px 10px;
        overflow: auto;
    }

    .model-container {
        position: relative;
        width: 100%;
        height: 100%;
        min-height: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 16px 18px;
        background:
            radial-gradient(circle at 52% 48%, rgba(30, 137, 154, 0.2), transparent 48%),
            linear-gradient(180deg, rgba(2, 43, 56, 0.78), rgba(1, 24, 31, 0.94));
    }

    .model-stage {
        position: relative;
        width: min(100%, 530px);
        aspect-ratio: 531 / 784;
        border-radius: 8px;
        overflow: hidden;
        background:
            radial-gradient(circle at 50% 45%, rgba(6, 49, 54, 0.32), transparent 55%),
            linear-gradient(180deg, #021d22, #011a20);
        box-shadow: 0 18px 34px rgba(0, 7, 14, 0.22);
    }

    .device-model-img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        display: block;
        user-select: none;
        pointer-events: none;
        filter: saturate(0.92) brightness(0.95) contrast(1.02);
    }

    .sensor-point {
        position: absolute;
        z-index: 4;
        width: 22px;
        height: 22px;
        transform: translate(-50%, -50%);
        cursor: pointer;
        transition: transform 0.18s ease, opacity 0.18s ease;

        &.disabled {
            cursor: default;
            opacity: 0.55;
        }

        &.active,
        &.related {
            transform: translate(-50%, -50%) scale(1.12);
        }
    }

    .point-dot {
        position: absolute;
        inset: 3px;
        z-index: 2;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: #7ee8d4;
        border: 1px solid rgba(255, 255, 255, 0.86);
        box-shadow: 0 0 12px rgba(126, 232, 212, 0.54);

        &.active {
            background: #ffd65a;
            box-shadow: 0 0 16px rgba(255, 214, 90, 0.7);
        }

        &.disabled {
            color: rgba(255, 255, 255, 0.82);
            background: rgba(126, 143, 148, 0.9);
            box-shadow: none;
        }
    }

    .point-ring {
        position: absolute;
        inset: -3px;
        z-index: 1;
        border-radius: 50%;
        border: 1px solid rgba(126, 232, 212, 0.72);
        animation: pointPulse 1.9s ease-out infinite;

        &.active {
            border-color: rgba(255, 214, 90, 0.86);
        }

        &.disabled {
            animation: none;
            border-color: rgba(255, 255, 255, 0.28);
        }
    }

    @keyframes pointPulse {
        0% {
            opacity: 0.9;
            transform: scale(0.75);
        }
        100% {
            opacity: 0;
            transform: scale(1.45);
        }
    }

    .gbom-tree-card :deep(.el-tree) {
        background: transparent;
        color: rgba(255, 255, 255, 0.9);
    }

    .gbom-tree-card :deep(.el-tree-node__content) {
        min-height: 30px;
        border-radius: 6px;
        color: rgba(255, 255, 255, 0.88);
    }

    .gbom-tree-card :deep(.el-tree-node__content:hover) {
        color: #ffffff;
        background: rgba(128, 216, 237, 0.1) !important;
    }

    .gbom-tree-card :deep(.el-tree-node.is-current > .el-tree-node__content) {
        color: #ffffff;
        background: rgba(128, 216, 237, 0.18) !important;
    }

    .gbom-tree-card :deep(.el-tree-node:before) {
        border-left-color: rgba(122, 214, 240, 0.24) !important;
    }

    .gbom-tree-card :deep(.el-tree-node:after) {
        border-top-color: rgba(122, 214, 240, 0.24) !important;
    }

    .gbom-tree-card :deep(.tree-btn .btn) {
        color: rgba(225, 250, 255, 0.88) !important;
    }

    .gbom-tree-card :deep(.tree-btn .btn:hover) {
        color: #7ee8d4 !important;
    }

    @media (max-width: 1280px) {
        .gbom-manage-layout {
            grid-template-columns: minmax(320px, 0.95fr) minmax(420px, 1.05fr);
        }

        .model-stage {
            width: min(100%, 470px);
        }
    }

    @media (max-height: 760px) {
        .model-stage {
            width: min(100%, 440px);
        }
    }

    .treeDialog{
        display: flex;
        flex-direction: column;
        justify-content: space-between; /* 将内容水平分布在容器中 */
        align-items: flex-end;
        .el-button{
            background-color: cadetblue;
            width: 65px;
            height: 40px;
            margin-top: 15px;
            border-color: white;
            &:hover {
                background-color: chocolate; /* 设置悬浮时的背景色 */
            }
        }
    }

</style>
