<template>
  <div class="subTree">
    <sw-tree class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
             :defaultProps="defaultProps" :expandNode="true"
             :defaultExpandAll="false" nodeCurrentKey='id' :defaultExpandKeys="defaultExpandKeys"
             :showCheckBox="true" :checkStrictly="false" :isLazy="false"
             @nodeExpand="handleNodeExpand" @check="handleCheck">
    </sw-tree>
  </div>
</template>
<script>
import swTree from '@/components/myComponent/swTree.vue'
import {
  reqTreeNodesBySceneId,
  reqSonNodesBySceneId, getGBomTreeBySceneIdAndProId, getGBomSonTreeBySceneIdAndProId
} from '@/api/sw/model3d/configGbomTree/index.js'
import {onMounted, ref} from "vue";

let globeParams = {}     //  声明一个全局参数对象
let _tree

export default {
  name: 'SelectTree',

  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      defaultExpandKeys: [],
      updateData: [],
    }
  },

  components: {
    swTree,
  },
  props: {
    sceneId: {
      type: [String, Number],
      required: true
    },
    addOrUpdate: {
      type: String,
    },
    modelId: {
      type: [String, Number],
    }
  },
  watch: {
    sceneId(newVal) {
      console.log('addMode.vue传来的 sceneId 发生变化:', newVal)
      this.getTreeNodes(globeParams.nodeLevel)
    },
    /*    addOrUpdate: {
          handler(newVal) {
            console.log('addModel.vue传来的 addOrUpdate 发生变化:', newVal)
            if (newVal === 'update' && this.modelId) {
              this.setCheckedByUpdateData();
            }
          },
          immediate: true
        },
        modelId: {
          handler(newVal) {
            console.log('addMode.vue传来的 modelId 发生变化:', newVal)
            if (this.addOrUpdate === 'update' && newVal) {
              this.setCheckedByUpdateData();
            }
          },
          immediate: true
        }*/
  },

  setup() {
    let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
    onMounted(() => {
      _tree = lazyTree
    })
    return {lazyTree}
  },

  async mounted() {
    globeParams.nodeLevel = 3
    await this.getTreeNodes(globeParams.nodeLevel)
    if (this.addOrUpdate === 'update') {
      this.setCheckedByUpdateData()
    }
  },

  methods: {

    setCheckedByUpdateData() {
      getGBomTreeBySceneIdAndProId(this.sceneId, this.modelId, globeParams.nodeLevel).then((res) => {
        if (res.data.code === 0) {
          this.updateData = res.data.data;
          // 数据拿到后，刷新选中
          this.$nextTick(() => {
            if (_tree && _tree.value) {
              const tree = _tree.value.$refs.tree;
              // 只选中 updateData 里的节点
              const updateIds = this.updateData.filter(item => item.nodeLevel === 3).map(item => item.id);
              tree.setCheckedKeys(updateIds);
            }
          });
        }
      })
    },

    //  节点扩展消息响应
    async handleNodeExpand(data, node, treeNode) {
      if (node.level >= globeParams.nodeLevel) {
        let response = await reqSonNodesBySceneId(this.sceneId, data.nodeCode)
        if (response.data.data) {
          response.data.data.forEach((item) => {
            item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
          })
          node.data.children = response.data.data

          this.$nextTick(() => {
            if (_tree && _tree.value) {
              const tree = _tree.value.$refs.tree
              let expandIds
              if (this.addOrUpdate === 'add') {
                expandIds = response.data.data.map(node => node.id)
                const currentCheckedKeys = tree.getCheckedKeys()
                tree.setCheckedKeys([...currentCheckedKeys, ...expandIds])
              } else if (this.addOrUpdate === 'update') {
                getGBomSonTreeBySceneIdAndProId(this.sceneId, this.modelId, data.nodeCode).then((res) => {
                  if (res.data.code === 0) {
                    expandIds = res.data.data.map(node => node.id)
                    const currentCheckedKeys = tree.getCheckedKeys()
                    tree.setCheckedKeys([...currentCheckedKeys, ...expandIds])
                  }
                })
              }
            }
          })
        }
      }
    },

    // 处理节点选中状态变化
    handleCheck(data, checked) {
      if (_tree && _tree.value) {
        const tree = _tree.value.$refs.tree
        // 获取当前节点的所有子节点
        const getChildNodes = (node) => {
          let children = []
          if (node.children && node.children.length > 0) {
            node.children.forEach(child => {
              children.push(child)
              children = children.concat(getChildNodes(child))
            })
          }
          return children
        }

        // 获取当前节点的所有父节点
        const getParentNodes = (node) => {
          let parents = []
          let currentNode = tree.getNode(node.id)
          while (currentNode && currentNode.parent) {
            parents.push(currentNode.parent.data)
            currentNode = currentNode.parent
          }
          return parents
        }

        // 设置子节点的选中状态
        const childNodes = getChildNodes(data)
        childNodes.forEach(child => {
          tree.setChecked(child.id, checked)
        })

        // 更新父节点的选中状态
        const parentNodes = getParentNodes(data)
        parentNodes.forEach(parent => {
          const parentNode = tree.getNode(parent.id)
          if (parentNode) {
            const children = parentNode.childNodes
            const allChecked = children.every(child => child.checked)
            const someChecked = children.some(child => child.checked)
            tree.setChecked(parent.id, allChecked)
            tree.setIndeterminate(parent.id, someChecked && !allChecked)
          }
        })
      }
    },

    // 根据节点层级数，加载结构树的一组节点
    async getTreeNodes(nodeLevel) {
      try {
        let response = await reqTreeNodesBySceneId(this.sceneId, nodeLevel)
        if (response.data.data) {
          this.treeData = []
          this.expandKeys = []            //  缓存待扩展的节点
          let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")
          for (let item of rootNodes) {
            this.treeData.push(item)
            this.expandKeys.push(item.id)
            await this.setChildren(item, response.data.data)
          }
          this.defaultExpandKeys = this.expandKeys            //  扩展节点

          if (this.addOrUpdate === 'add') {
            // 数据加载完成后，设置所有节点为选中状态
            this.$nextTick(() => {
              if (_tree && _tree.value) {
                const tree = _tree.value.$refs.tree
                // 递归获取所有节点ID
                const getAllNodeIds = (nodes) => {
                  let ids = []
                  nodes.forEach(node => {
                    ids.push(node.id)
                    if (node.children && node.children.length > 0) {
                      ids = ids.concat(getAllNodeIds(node.children))
                    }
                  })
                  return ids
                }
                const allIds = getAllNodeIds(this.treeData)
                tree.setCheckedKeys(allIds)
              }
            })
          }
        }
      } catch (error) {
        console.log(error)
      }
    },

    //  递归查询节点pNode的全部子节点，并装配成el-tree的数据结构
    setChildren(pNode, nodeList) {
      let res = this.getChildrenByNodeCode(pNode.nodeCode, nodeList)
      let children = res.sonNodes
      if (children.length === 0) {
        if ((pNode.nodeType === 'Mid') || (pNode.nodeType === 'Root')) {           //  如果不是叶子节点，节点前显示"+"号
          this.expandKeys = this.expandKeys.filter(item => item !== pNode.id) //  从扩展节点中删除它
          pNode.children = [{id: 'loading', name: '节点加载中...'}]
        }
        return pNode
      } else {
        pNode.children = children
        children.forEach((item) => {
          this.expandKeys.push(item.id)          //  添加到扩展节点
          this.setChildren(item, res.otherNodes)
        })
      }
    },

    //  正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
    getChildrenByNodeCode(pNodeCode, nodeList) {
      let sonNodes = []
      let otherNodes = []
      let regex = new RegExp('^' + pNodeCode + '-[A-Za-z0-9]+$')
      nodeList.forEach((item) => {
        if (regex.test(item.nodeCode))          //  正则表达式判定item是不是pNodeCode的下一层子节点，如果是就放入数组sonNodes
          sonNodes.push(item)
        else
          otherNodes.push(item)
      })
      return {sonNodes: sonNodes, otherNodes: otherNodes}
    },

    //  返回用户当前选中的节点
    getCurrentNode() {
      if (!_tree.value) return [];
      const tree = _tree.value.$refs.tree;
      // 全选节点
      const checkedNodes = tree.getCheckedNodes();
      // 半选节点
      const halfCheckedKeys = tree.getHalfCheckedKeys();
      // 获取所有节点的map，方便通过id查找
      const allNodeMap = {};

      function traverse(nodes) {
        nodes.forEach(node => {
          allNodeMap[node.id] = node;
          if (node.children && node.children.length > 0) {
            traverse(node.children);
          }
        });
      }

      traverse(this.treeData);

      // 合并全选和半选节点
      const result = [...checkedNodes];
      halfCheckedKeys.forEach(key => {
        if (allNodeMap[key]) {
          result.push(allNodeMap[key]);
        }
      });
      return result;
    },

    clearSelected() {
      this.reqTreeNodesBySceneId(this.sceneId, globeParams.nodeLevel)
      if (_tree && _tree.value) {
        _tree.value.$refs.tree.setCheckedKeys([]) // 清除所有已勾选的节点
        _tree.value.$refs.tree.setCurrentKey(null) // 清除高亮的节点
      }
    }

  }

}

</script>
<style lang="scss" scoped>
.subTree {
  width: 100%;
  height: calc(100% - 15px);
  padding-top: 15px;
}

</style>