<template>
  <div class="tree-zone">
    <div v-show="display" :class="hasline ? 'treestyle lineyes' : 'treestyle'">
      <el-tree :data="data" :indent="0" ref="tree" :accordion="accordion" @check="check" :props="defaultProps"
               :expand-on-click-node="expandNode"  :filter-node-method="filterNode"
               :default-checked-keys="defaultCheckedKeys" :node-key="nodeCurrentKey" :load="loadNode" lazy
               :default-expanded-keys="defaultKeys" @node-expand="nodeExpand" @node-collapse='nodeCollapse'
               :currentKey="currentKey" :show-checkbox="showCheckBox" :check-strictly="checkStrictly" :render-content="renderContent"
               @check-change="handleCheckChange" :default-expand-all="defaultExpandAll" @node-click="tyHandleNodeClick">

      </el-tree>
    </div>
  </div>

</template>
<script>
  let treeIdFromSon = ''

export default {
  data: function () {
    return {
      display: true,
      n: 0,
    }
  },
  props: {
    // 父级传给组件的数据，然后用$emit（‘方法名’，数据）返回父级数据
    data: Array,
    hasline: {
      Boolean,
      default: true // 默认有连线
    },
    defaultCheckedKeys: {
      type: Array,
      default: () => {
        return []
      }
    },
    showCheckBox: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    checkStrictly: {
      type: Boolean,
      default: () => {
        return false
      }
    },

    // 点击节点是否触发展开
    expandNode: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    defaultKeys: {
      type: Array,
      default: () => {
        return []
      }
    },
    defaultExpandAll: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    currentKey: {
      default: () => {
        return ''
      }
    },
    nodeCurrentKey: {
      type: String,
      default: () => {
        return 'id'
      }
    },
    defaultProps: {
      type: Object,
      default: () => {
        return {
          children: 'children',
          label: 'label',
          myshow: 'myshow'
        }
      }
    },
    searchKey: {
      type: String,
      default: () => {
        return 'name'
      }
    },
    // 判断刷新父节点还是子节点
    refreshParentNode: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    // 懒加载存储已加载树节点信息
    saveTreeIdList: {
      type: Array,
      default: () => {
        return []
      }
    },
    //是否每次只打开一个同级树节点展开
    accordion: {
      type: Boolean,
      default: () => {
        return false
      }
    }
  },
  watch: {
    // 监听点击模型的值,切换树选择
    data () {
      this.$nextTick(function () {
        this.$refs.tree.setCurrentKey(this.currentKey)
      })
    },
    // 监听默认选中的节点--定时器是因为接口速度较慢,后期可以优化
    currentKey () {
      setTimeout(() => {
        this.$nextTick(function () {
          this.$refs.tree.setCurrentKey(this.currentKey)
        })
      }, 1000)
    },
    defaultCheckedKeys (key) {
      this.$nextTick(function () {
        this.$refs.tree.setCheckedKeys(this.defaultCheckedKeys)
      })
    }
  },
  created () { },
  mounted () {
    // 复制到window
    window['sendTreeId'] = (id) => {
      this.getTreeId(id)
    }
    // 懒加载刷新
    var that = this

    /*this.$on('refreshNodeBy', (id) => {
      that.refreshNodeBy(id)
    })*/

  },
  methods: {
    //  控制树的显示和隐藏
    open (isDisplay) {
      this.display = isDisplay
    },
    // 获取子页面传递的id
    getTreeId (id) {
      treeIdFromSon = id
      this.$nextTick(function () {
        this.$refs.tree.setCurrentKey(treeIdFromSon)
      })
    },
    // 获取节点的选择状态
    check (checkedNodes) {
      let node = this.$refs.tree.getNode(checkedNodes.id)
      let res = this.$refs.tree.getCheckedNodes()
      let arr = []
      res.forEach((item) => {
        arr.push(item)
      })
      for (let i in arr) {
        if (arr[i].disabled === true) {
          arr.splice(i, 1)
        }
      }
      if (checkedNodes.checkType === false) {
        checkedNodes.checkType = true
      } else {
        checkedNodes.checkType = false
      }
      this.$emit('checkNode', arr, checkedNodes, checkedNodes.checkType, node)
    },
    refreshNodeBy (id) {
      let node = this.$refs.tree.getNode(id) // 通过节点id找到对应树节点对象
      if (this.refreshParentNode && node.parent.level !== 0) {
        node.parent.loaded = false
        node.parent.expand()
      } else {
        node.loaded = false
        node.expand() // 主动调用展开节点方法，重新查询该节点下的所有子节点
      }
    },
    // 递归展开树节点
    recursionRefreshNodeBy (arr) {
      this.$refs.tree.getNode(arr[0]).expand()
      if (this.n !== arr.length) {
        this.recursionTree(arr)
      }
    },
    recursionTree (arr) {
      setTimeout(() => {
        console.log(this.n)
        this.n++
        if (this.n !== arr.length) {
          this.$refs.tree.getNode(arr[this.n]).expand()
          this.recursionTree(arr)
        } else {
          return false
        }
      }, 1500)
    },
    // 懒加载
    loadNode (node, resolve) {
      this.$emit('loadTreeNode', node, resolve)
    },
    tyHandleNodeClick (data, node, resolve) {
      this.$emit('tynodeclick', data, node, resolve)
    },
    /*setaccordion () {
      this.isaccordion = true
      console.info(this.isaccordion)
    },*/
    // 清空
    resetChecked () {
      this.$refs.tree.setCheckedKeys([])
    },
    filterNode (value, data) {
      // this.$emit('filterNode', value, data)
      if (!value) return true
      return data[this.searchKey].indexOf(value) !== -1
    },
    handleCheckChange (data, checked, indeterminate) {
      this.$emit('handleCheckChange', data, checked, indeterminate)
    },


    nodeExpand (data, node) {
      this.$emit('nodeExpand', data, node)
    },
    nodeCollapse (data, node) {
      this.$emit('nodeCollapse', data, node)
    },

    renderContent (h, { node, data, store }) {
      // renderContent 开始
      return h(
        'span',
        {
          attrs: {
            class: 'contree'
          },
          style:
            'flex: 1; display: flex; align-items: center; justify-content: flex-start; font-size: 14px; padding-right: 8px;display: block;width: 100%;',
          on: {
            // 监听鼠标滑过
            mouseenter: () => {
              data.myshow = true
              // console.info('滑过=' + data.myshow)
            },
            // 监听鼠标离开
            mouseleave: () => {
              data.myshow = false
              // console.info('离开=' + data.myshow)
            }
          }
        },
        [
          h(
            // Mesh可见性
            'i',
            {
              attrs: {
                class: data.iconName
              },
              style: {
                display: data.iconName === '' || data.iconName === undefined ? 'none' : '',
                color: data.iconColor,
                marginLeft: '0px',
                marginRight: '0px'
              }
            }
          ),
          h(
            // 模型节点可下载模型文件显示
            'i',
            {
              attrs: {
                class: 'el-icon-download'
              },
              style: {
                display: data.modelNodeShow === true ? '' : 'none',
                color: '#409eff',
                marginLeft: '0px',
                marginRight: '5px'
              },
              on: {
                click: () => {
                  const self = this
                  let objdata = {
                    eldata: data
                  }
                  self.$emit('downLoad', objdata)
                }
              }
            }
          ),
          h(
            'span',
            {
              // 显示节点名称
              attrs: {
                class: 'nodename',
                title: node.label
              },
              style: {
                //color: data.iconName === 'el-icon-view' ? '#ffff99' : '#fff',     //  根据图标类型设置字体颜色。可以设置不同的颜色
                color: data.textColor !== undefined ? data.textColor : '#fff',     //  设置字体颜色,可以设置不同的颜色
                fontWeight: data.name ? 'bold' : '',
                padding: '0 0 0 5px'
              }
            },
            node.label
          ),
          h(
            'span',
            {
              attrs: {
                class: 'opzone'
              },
              style: {
                marginLeft: '15px',
                display: data.myshow || data.userShow ? '' : 'none'
              }
            },
            [
              h(
                // 删除压缩文件
                'i',
                {
                  class: ['el-icon-folder-delete'],
                  style: {
                    color: '#E10D0D',
                    marginLeft: '5px',
                    display: data.delShow === true ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('delFile', objdata)

                    }
                  }
                }
              ),
              h(
                // 向前翻页
                'i',
                {
                  class: ['el-icon-arrow-left'],
                  style: {
                    color: '#f56c6c',
                    marginLeft: '5px',
                    display: data.userShow && data.prePageShow ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventPrePage', objdata)
                    }
                  }
                }
              ),
              h(
                // 向后翻页
                'i',
                {
                  class: ['el-icon-arrow-right'],
                  style: {
                    color: '#f56c6c',
                    marginLeft: '5px',
                    display: data.userShow && data.latPageShow ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventLatPage', objdata)
                    }
                  }
                }
              ),
              h(
                // 添加
                'i',
                {
                  class: ['el-icon-plus'],
                  style: {
                    color: '#ffffff',
                    display: data.myshow && data.showAdd ? '' : 'none',
                    marginLeft: '0px',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventadd', objdata)
                    }
                  }
                }
              ),
              h(
                // 节点复制
                'i',
                {
                  class: ['el-icon-share'],
                  style: {
                    color: '#ffffff',
                    display: data.myshow && data.showCopy ? '' : 'none',
                    marginLeft: '0px',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventCopy', objdata)
                    }
                  }
                }
              ),
              h(
                // 修改
                'i',
                {
                  class: ['el-icon-edit'],
                  style: {
                    color: '#ffffff',
                    marginLeft: '5px',
                    display: data.myshow && data.showEdit ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventupdata', objdata)
                    }
                  }
                }
              ),
              h(
                // 删除
                'i',
                {
                  class: ['el-icon-delete'],
                  style: {
                    color: '#f56c6c',
                    marginLeft: '5px',
                    display: data.myshow && data.showRemove ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventDelete', objdata)
                      const parent = node.parent
                      const children = parent.data.children || parent.data
                      const index = children.findIndex(d => d.id === data.id)
                      children.splice(index, 1)
                    }
                  }
                }
              ),
              h(
                // 节点移动
                'i',
                {
                  class: ['el-icon-s-operation'],
                  style: {
                    color: '#ffffff',
                    marginLeft: '5px',
                    display: data.myshow && data.showMove ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventMove', objdata)
                    }
                  }
                }
              ),
              h(
                // 查看属性
                'i',
                {
                  class: ['el-icon-view'],
                  style: {
                    color: '#f56c6c',
                    marginLeft: '5px',
                    display: data.myshow && data.showAttr ? '' : 'none',
                    marginRight: '5px',
                    verticalAlign: 'middle'
                  },
                  on: {
                    click: () => {
                      const self = this
                      let objdata = {
                        eldata: data
                      }
                      self.$emit('eventView', objdata)
                    }
                  }
                }
              ),
            ]
          )
        ]
      )
    } // renderContent 结束
  }
}
</script>

<style lang="scss" scoped>
  .tree-zone {
    ::v-deep{
      .el-scrollbar .el-scrollbar__wrap {
        overflow-x: hidden;
      }
      .el-tree > .el-tree-node {
        min-width: 100%;
        display: inline-block;
      }

      .treestyle {
        .el-tree-node {
          position: relative;
          padding-left: 16px;
        }
        .el-tree {
          background-color: Transparent;
          color: #fff;
        }
        //消除叶子节点与连线间的间隙
        .el-tree-node__expand-icon.is-leaf {
          display: none;
        }

        .el-tree-node__children {
          padding-left: 16px;
        }

        .el-tree-node :last-child:before {
          height: 38px;
        }

        .el-tree > .el-tree-node:before {
          border-left: none;
        }

        .el-tree > .el-tree-node:after {
          border-top: none;
        }
        .el-tree-node:before {
          content: "";
          left: -4px;
          position: absolute;
          right: auto;
          border-width: 1px;
        }

        .el-tree-node:after {
          content: "";
          left: -4px;
          position: absolute;
          right: auto;
          border-width: 1px;
        }
      }
      .lineyes {
        // 显示节点间连接的竖线
        .el-tree-node:before {
          border-left: 1px dashed #dcdcdc;
          bottom: 0px;
          height: 100%;
          top: -26px;
          width: 3px;
        }
        // 显示节点间连接的横线
        .el-tree-node:after {
          border-top: 1px dashed #dcdcdc;
          height: 20px;
          top: 12px;
          width: 24px;
        }

        //鼠标滑过
        .el-tree-node__content:hover {
          font-weight: bold;
          background-color: rgb(108, 108, 111) !important;
          /*background-color: rgba(60, 63, 241, 0.16);*/
        }
        .el-tree .el-tree-node__expand-icon.expanded {
          -webkit-transform: rotate(0deg);
          transform: rotate(0deg);
        }

        /*    有子节点 且已展开    */
        /*.el-tree .is-expanded .el-tree-node__expand-icon .expanded:before {
          background: url('/img/lazytree/a1.svg') no-repeat 0 3px;
          content: '';
          display: block;
          width: 20px;
          height: 20px;
          font-size: 16px;
          background-size: 16px;
          padding-right: 18px;
        }*/

        /*    有子节点 且未展开   */
        /*.el-tree .el-tree-node.is-focusable .el-tree-node__expand-icon:before {
          background: url('/img/lazytree/circleplus.svg') no-repeat 0 3px;
          content: '';
          display: block;
          width: 20px;
          height: 20px;
          font-size: 16px;
          background-size: 16px;
          padding-right: 18px;
        }*/




        //
        // 改变选中节点样式
        .el-tree-node.is-current > .el-tree-node__content {
          background-color: rgb(137, 137, 141) !important;
          /*background-color: rgba(203, 203, 205, 0.65);*/
          font-weight: bold;
        }
        .el-icon-arrow-right :before {
          content: "\e6df";
        }
        .el-icon-arrow-down :after {
          content: "\E6E0";
        }

        //  节点获取焦点
        .el-tree-node:focus > .el-tree-node__content {
          background-color: rgba(138, 194, 252, 0.53) !important;
        }
      }






    }
/*    .custom-tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 14px;
      padding-right: 8px;
    }*/
  }

</style>


<!--
https://blog.csdn.net/qq_37916164/article/details/130762689
https://blog.csdn.net/baidu_38492843/article/details/125673247-->
<!--https://blog.csdn.net/yuey0809/article/details/128224673-->
