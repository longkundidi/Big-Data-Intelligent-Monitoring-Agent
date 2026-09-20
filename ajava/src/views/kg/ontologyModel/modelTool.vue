<template>
  <el-container id="buildercontainer">
    <el-header style="height: 3.5%">
      <div style="float: right;margin-top: -28px">
        <el-form :inline="true" size="small">
          <el-form-item>
            <el-button class="finishBtn" @click="finishEdit">修改完成</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-header>
    <el-container>
      <el-main>
        <div class="content" ref="containerRef">
          <div class="app-stencil" id="stencilContainer"></div>
          <div class="app-content" id="graphContainer"></div>
        </div>
      </el-main>
      <el-aside class="aside">
        <el-form ref="formNodeRef" :model="formNode" style="margin-bottom: 20px" title="节点信息">
          <el-form-item label="节点名称：" prop="name">
            <h3>{{ formNode.name }}</h3>
          </el-form-item>
          <el-form-item label="属性：">
          </el-form-item>
          <el-form-item>
            <el-table
                :data="tableData"
                border
                fit
                style="width: 100%;margin: auto">
              <el-table-column type="index" label="序号" align="center" width="100"/>
              <el-table-column prop="attr" label="属性" align="center"/>
            </el-table>
          </el-form-item>
        </el-form>
      </el-aside>
    </el-container>
    <el-dialog
        title="节点编辑"
        v-model="editNodevisible"
        width="20%"
        destroy-on-close
        :show-close="false">
      <el-form ref="formNodeRef" :model="formNode" style="margin-bottom: 20px" title="节点信息">
        <el-form-item label="节点名称：" prop="name">
          <el-input placeholder="请点击节点" v-model="formNode.name"/>
        </el-form-item>
        <el-form-item v-model="visible" v-if="visible" label="添加属性：">
          <el-button class="addiconbtn" @click="addDomain">
            <CirclePlus style="width: 2em; height: 2em"/>
          </el-button>
        </el-form-item>
        <el-form-item
            v-model="visible" v-if="visible"
            v-for="(domain, index) in formNode.domains"
            :key="index"
            :label="'属性'+(index+1)+'：'"
            style="display: flex;align-items: center;">
          <el-row style="width: 100%" :gutter="4">
            <el-col :span="20">
              <el-input v-model="formNode.domains[index]"></el-input>
            </el-col>
            <el-col :span="4">
              <el-button class="deleteiconbtn" @click.prevent="removeDomain(domain)">
                <Delete style="width: 1em; height: 1em"/>
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
        <div slot="footer"
             class="form-footer">
          <el-button class="cancelbtn2"
                     @click="cancelEditNode()">取 消
          </el-button>
          <el-button class="deletbtn1"
                     @click="deleteNode()">删 除
          </el-button>
          <el-button class="updatebtn1"
                     @click="submitNode()">确 认
          </el-button>
        </div>
      </el-form>
    </el-dialog>
    <el-dialog
        title="创建连接"
        v-model="createEdge"
        width="20%"
        destroy-on-close
        :show-close="false">
      <el-input placeholder="请输入连接关系的名字" v-model="currentlabel">
        <template #prepend>边名称：</template>
      </el-input>
      <div slot="footer"
           style="display: block;text-align: center;margin-top: 25px"
           class="dialog-footer">
        <el-button class="cancelbtn1"
                   @click="cancelCreateEdge()">取消
        </el-button>
        <el-button class="determinebtn1"
                   @click="submitEdge()">确定
        </el-button>
      </div>
    </el-dialog>
    <el-dialog
        title="编辑连接"
        v-model="editEdge"
        width="20%"
        destroy-on-close
        :show-close="false">
      <el-input :placeholder="currentlabel" v-model="currentlabel">
        <template #prepend>边名称：</template>
      </el-input>
      <div slot="footer"
           style="display: block;text-align: center;margin-top: 25px"
           class="dialog-footer">
        <el-button class="cancelbtn1"
                   @click="editEdge = false; currentlabel = ''">取消
        </el-button>
        <el-button class="deletbtn2"
                   @click="deleteEdge()">删除
        </el-button>
        <el-button class="determinebtn1"
                   @click="submitEditEdge()">确定
        </el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {Graph} from "@antv/x6";
import {Stencil} from "@antv/x6-plugin-stencil";
import {ElMessage, ElMessageBox} from "element-plus";
import {CirclePlus, Delete} from "@element-plus/icons-vue";
import {useRoute, useRouter} from "vue-router";
import {
  addAttribute,
  addNode,
  addRelation,
  deleteAttribute,
  deleteMetaNode,
  deleteOtherNode, deleteRelation, editRelation,
  updateAttribute, updateNode, updateTempFile
} from "@/api/kg/ontologyModel";


let query = '' //接收父组件参数
let ontologyName = ''
let prename = '' //修改前节点名字
let preedgename = '' //修改前边的名字
let preattribute = '' //修改前属性信息
let nodeflag = 1 //0:新增节点;1:编辑节点
let visible = ref(false) //是否显示属性
let currentlabel = ref('')
let currentEdge = null
let containerRef = ref(null)
let graphContainer = null
let stencilContainer = null
let graph = null;
let stencil = null;
let nodeCurrent = null;
let formNodeRef = ref(null);
let formNode = ref({
  name: '',
  domains: [],
});
let data = {
  nodes: [
    {
      id: 'rootid',
      shape: 'default-circle-node',
      x: 300,
      y: 300,
      width: 60,
      height: 60,
      label: '结构',
      attrs: {
        body: {
          stroke: '#FF5511',
          strokeWidth: 1,
          fill: 'rgba(255,85,17,0.6)',
        },
        text: {
          text: '结构',
        }
      },
      ports: {
        items: [
          {
            id: 'port_1',
            group: 'top',
          },
          {
            id: 'port_2',
            group: 'bottom',
          },
          {
            id: 'port_3',
            group: 'left',
          },
          {
            id: 'port_4',
            group: 'right',
          },
        ],
      },
    },
  ],
}
const $route = useRoute();
const $router = useRouter();
//region 注册节点
Graph.registerNode(
    'default-circle-node',
    {
      inherit: 'circle',
      width: 80,
      height: 80,
      data: {
        types: 'Meta',
        attribute: '',
      },
      attrs: {
        body: {
          stroke: '#FF5511',
          strokeWidth: 1,
          fill: 'rgba(255,85,17,0.6)',
          rx: 6,
          ry: 6,
        },
      },
      ports: {
        groups: {
          top: {
            position: 'top',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          bottom: {
            position: 'bottom',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          left: {
            position: 'left',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          right: {
            position: 'right',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
        },
      },
    },
    true,
)
Graph.registerNode(
    'default-event-node',
    {
      inherit: 'circle',
      width: 80,
      height: 80,
      data: {
        types: 'Event',
        attribute: '',
        attributekey: '',
      },
      attrs: {
        body: {
          stroke: '#0a9176',
          strokeWidth: 1,
          fill: 'rgba(10,145,118,0.6)',
          rx: 6,
          ry: 6,
        },
      },
      ports: {
        groups: {
          top: {
            position: 'top',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          bottom: {
            position: 'bottom',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          left: {
            position: 'left',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          right: {
            position: 'right',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
        },
      },
    },
    true,
)
Graph.registerNode(
    'default-rect-node',
    {
      inherit: 'rect',
      width: 80,
      height: 30,
      data: {
        types: 'attribute',
      },
      attrs: {
        body: {
          stroke: '#FFBB66',
          strokeWidth: 1,
          fill: 'rgba(255,187,102,0.6)',
          rx: 6,
          ry: 6,
        },
      },
      ports: {
        groups: {
          top: {
            position: 'top',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          bottom: {
            position: 'bottom',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          left: {
            position: 'left',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
          right: {
            position: 'right',
            attrs: {
              circle: {
                magnet: true,
                stroke: '#8f8f8f',
                r: 3,
              },
            },
          },
        },
      },
    },
    true,
)
//endregion

onMounted(async () => {
  ontologyName = "维修知识本体";
  query = $route.query;
  data = JSON.parse(decodeURIComponent(query.graphData));
  init();
})
const init = function () {

  //region graph画布
  graphContainer = document.getElementById('graphContainer')
  graph = new Graph({
    container: graphContainer,
    width: containerRef.offsetWidth,
    height: containerRef.offsetHeight,
    panning: true,
    mousewheel: true,
    background: {
      color: "#eceef1",
    },
    connecting: {
      allowBlank: false,
      allowNode: true,
      allowLoop: true,
      allowPort: true,
      allowMulti: true,
    }
  })
  //endregion

  //region stencil工具栏
  stencil = new Stencil({
    title: '概念与属性',
    target: graph,
    stencilGraphHeight: 260,
    layoutOptions: {
      columns: 1,
      resizeToFit: true,
    },
    groups: [
      {
        name: 'group1',
        title: '概念类',
        collapsable: false,
      },
      {
        name: 'group2',
        title: '属性',
        collapsable: false,
      },
    ]
  });
  stencilContainer = document.getElementById('stencilContainer')
  stencilContainer.appendChild(stencil.container)
  const c1 = graph.createNode({
    shape: 'default-circle-node',
    label: '实例概念',
    data: {
      types: 'Instance',
      attribute: '',
    },
    attrs: {
      body: {
        stroke: '#0c5aab',
        strokeWidth: 1,
        fill: 'rgba(12,90,171,0.6)',
      },
    },
  })
  const c2 = graph.createNode({
    shape: 'default-circle-node',
    label: '元概念',
  })
  const c3 = graph.createNode({
    shape: 'default-event-node',
    label: '事件',
  })
  const r1 = graph.createNode({
    label: '属性',
    shape: 'default-rect-node',
  })
  stencil.load([c1, c2, c3], 'group1');
  stencil.load([r1], 'group2');
  //endregion

  //region 监听事件
  //监听节点点击事件
  graph.on("node:click", ({e, x, y, node, view}) => {
    tableData.value = [];
    nodeCurrent = node;
    formNode.value.name = nodeCurrent.getProp().attrs.text.text
    if (node.getProp().data.types !== 'attribute') {
      //获取修改之前信息
      prename = formNode.value.name;
      let attrlist = attributeList(prename);
      attrlist.forEach(i => {
        tableData.value.push({"attr": i.getProp().attrs.text.text});
      })
    } else {
      preattribute = formNode.value.name;
    }
    editNodevisible.value = true;
  })
  //监听节点创建事件
  graph.on("node:added", ({node}) => {
    node.setProp({
      ports: {
        items: [
          {
            id: 'port_1',
            group: 'top',
          },
          {
            id: 'port_2',
            group: 'bottom',
          },
          {
            id: 'port_3',
            group: 'left',
          },
          {
            id: 'port_4',
            group: 'right',
          },
        ],
      },
    })
    nodeCurrent = node;
    formNode.value.name = nodeCurrent.getProp().attrs.text.text
    nodeflag = 0;
    if (nodeflag === 0 && nodeCurrent.getProp().data.types !== 'attribute') {
      visible.value = true;
    }
    if (node.getProp().data.types !== 'attribute') {
      formNode.value.domains = [];
    }
    editNodevisible.value = true
  })
  //监听边双击事件
  graph.on("edge:dblclick", ({edge}) => {
    {
      preedgename = edge.getLabels()[0].attrs.label.text;
      if (preedgename !== "attribute") {
        currentlabel.value = preedgename;
        currentEdge = edge;
        editEdge.value = true;
      } else {
        ElMessage({
          type: "error",
          message: "属性边不能修改",
        })
      }
    }
  })
  //监听创建边事件
  graph.on("edge:connected", ({isNew, edge}) => {
    if (isNew) {
      currentEdge = edge;
      const source = edge.getSourceNode();
      const target = edge.getTargetNode();
      const sourcename = source.getProp().attrs.text.text;
      const targetname = target.getProp().attrs.text.text;
      const sourcetype = source.getProp().data.types;
      if (sourcetype === 'Meta') {
        edge.setAttrs({
          line: {stroke: '#FF5511'},
        });
        if (source.id === target.id) {
          edge.setProp({
            vertices: [
              {x: edge.getSourcePoint().x + 80, y: edge.getSourcePoint().y + 20},
              {x: edge.getSourcePoint().x + 80, y: edge.getSourcePoint().y + 40},
            ],
            connector: "smooth",
          });
          currentlabel.value = "子类";
          edge.appendLabel({
            attrs: {
              text: {
                text: currentlabel.value,
              },
            },
            position: {
              distance: 0.66,
              offset: -5,
            },
          })
        } else {
          currentlabel.value = "属于";
          edge.setLabels([{attrs: {label: {text: currentlabel.value}},}]);
        }
        createEdge.value = true;
      } else if (sourcetype === 'Instance') {
        edge.setAttrs({
          line: {stroke: '#0c5aab'},
        });
        if (source.id === target.id) {
          edge.setProp({
            vertices: [
              {x: edge.getSourcePoint().x + 80, y: edge.getSourcePoint().y + 20},
              {x: edge.getSourcePoint().x + 80, y: edge.getSourcePoint().y + 40},
            ],
            connector: "smooth",
          });
          currentlabel.value = "子类";
          edge.appendLabel({
            attrs: {
              text: {
                text: currentlabel.value,
              },
            },
            position: {
              distance: 0.66,
              offset: -5,
            },
          })
        } else {
          currentlabel.value = "实例";
          edge.setLabels([{attrs: {label: {text: currentlabel.value}},}]);
          createEdge.value = true;
        }
      } else if (sourcetype === 'Event') {
        edge.setAttrs({
          line: {stroke: '#0a9176'},
        });
        currentlabel.value = "来源";
        edge.setLabels([{attrs: {label: {text: currentlabel.value}},}]);
        createEdge.value = true;
      } else {
        edge.setAttrs({
          line: {stroke: '#FFBB66'},
        });
        edge.setLabels([{attrs: {label: {text: "attribute"}},}]);
        addAttribute({
          ontologyName: ontologyName, nodeName: targetname,
          attribute: sourcename
        }).then(r => {
          ElMessage({
            type: "success",
            message: r.data.msg,
          })
        })
      }
    }
  })
  //endregion

  //region 渲染图
  graph.fromJSON(data);
  mulEdge();
  graph.centerContent();
  //endregion
}

//处理特殊边的情况
const mulEdge = function () {
  //重新构造有多个关系的两节点
  let edges = graph.getEdges();
  for (let e in edges) {
    let item = edges[e];
    let labelLen = item.getLabels().length;
    if (labelLen > 1) { //多边的情况
      let currentEdgeId = edges[e].id;
      let sourceNode = edges[e].getSourceNode();
      let targetNode = edges[e].getTargetNode();
      let colorLine = edges[e].getAttrs().line.stroke;
      let gaplen = Math.abs((sourceNode.getProp().position.x - targetNode.getProp().position.x) / labelLen);
      for (let s = 0; s < labelLen; s++) {
        graph.addEdge({
          source: sourceNode.id,
          target: targetNode.id,
          labels: [edges[e].getLabelAt(s).attrs.label.text],
          attrs: {
            line: {
              stroke: colorLine,
            },
          },
          vertices: [
            {
              x: Math.min(sourceNode.getProp().position.x, targetNode.getProp().position.x) + gaplen * s,
              y: Math.min(sourceNode.getProp().position.y, targetNode.getProp().position.y) + gaplen * s,
            }
          ],
          connector: 'rounded',
        })
      }
      graph.removeEdge(currentEdgeId);
    }

    if (item.getSourceNode().id === item.getTargetNode().id) {
      item.setProp({
        vertices: [
          {x: item.getSourcePoint().x + 80, y: item.getSourcePoint().y + 20},
          {x: item.getSourcePoint().x + 80, y: item.getSourcePoint().y + 40},
        ],
        connector: "smooth",
      });
    }
  }
}
const attributeList = function (nname) {
  let edges = graph.getEdges();
  let attrlist = [];
  for (let i in edges) {
    let item = edges[i];
    if (item.getTargetNode().getAttrs().text.text === nname && item.getLabels()[0].attrs.label.text === 'attribute') {
      attrlist.push(item.getSourceNode())
    }
  }
  return attrlist;
}

const finishEdit = function () {
  //修改完成本体需要生成新的excel模板
  updateTempFile(ontologyName).then(r => {
    if (r.data.data === "成功") {
      ElMessage({
        type: "success",
        message: "excel模板同步修改成功",
      })
      $router.push({
        path: "./index",
      });
    } else {
      ElMessage({
        type: "error",
        message: "excel模板同步修改失败",
      })
    }
  })
}

const emit = defineEmits(['afterCommit']);
//region 节点操作
let tableData = ref([]);
let editNodevisible = ref(false) //节点编辑框
const cancelEditNode = function () {
  if (nodeflag === 0) { //新增的时候取消才需要从图中删除
    graph.removeNode(nodeCurrent.id);
    nodeflag = 1;
  }
  tableData.value = [];
  nodeCurrent = null;
  formNode.value = {};
  editNodevisible.value = false;
}
const addDomain = function () {
  formNode.value.domains.push('');
}
const removeDomain = function (domain) {
  const index = formNode.value.domains.indexOf(domain)
  if (index !== -1) {
    formNode.value.domains.splice(index, 1)
  }
}
const submitNode = function () {
  formNodeRef.value.validate((valid) => {
    if (valid) {
      let nodeId = nodeCurrent.id;
      let nodeProp = nodeCurrent.getProp();
      let nodename = formNode.value.name
      let nodetype = nodeProp.data.types
      let attrlist = formNode.value.domains
      let curNode = nodeCurrent;
      if (nodeflag === 0) {
        if (nodetype === 'attribute') {
          curNode.setProp({
            attrs: {text: {text: nodename}}
          });
        } else {
          let attributes = attrlist.toString();
          addNode({
            ontologyName: ontologyName, nodeName: nodename, nodeType: nodetype, attribute: attributes
          }).then(r => {
            r = r.data
            if (r.data.msg === "添加成功") {
              curNode.setProp({
                attrs: {text: {text: nodename}}
              });
              let x0 = curNode.getProp().position.x - 100;
              let y0 = curNode.getProp().position.y + 200;
              for (let i = 0; i < attrlist.length; i++) {
                const attributeNode = graph.addNode({
                  shape: 'default-rect-node',
                  x: x0 + i * 100,
                  y: y0,
                  label: attrlist[i],
                });
                graph.addEdge({
                  source: attributeNode.id,
                  target: nodeId,
                  labels: ['attribute'],
                  attrs: {
                    line: {stroke: '#FFBB66'},
                  },
                })
                preattribute = attrlist[i]
              }
              ElMessage({
                type: "success",
                message: r.data.msg,
              })
              nodeflag = 1;
            } else {
              ElMessage({
                type: "error",
                message: r.data.msg,
              })
              graph.removeNode(nodeId);
            }
            nodeflag = 1;
          })
        }
        visible.value = false;
      } else {
        if (nodetype === 'attribute') {
          let edges = graph.getEdges();
          let filteredge = edges.filter(edge => edge.getSourceNode().getAttrs().text.text === preattribute
              && edge.getLabels()[0].attrs.label.text === 'attribute');
          let targetname = filteredge[0].getTargetNode().getAttrs().text.text
          updateAttribute({
            ontologyName: ontologyName, nodeName: targetname, preAttribute: preattribute, attribute: nodename
          }).then(r => {
            r = r.data
            if (r.data.msg === "属性修改成功") {
              curNode.setProp({
                attrs: {text: {text: nodename}}
              });
              ElMessage({
                type: "success",
                message: r.data.msg,
              })
            } else {
              ElMessage({
                type: "error",
                message: r.data.msg,
              })
              graph.removeNode(nodeId);
            }
            nodeflag = 1;
          })
        } else {
          updateNode({
            ontologyName: ontologyName, nodeName: nodename, preName: prename
          }).then(r => {
            r = r.data
            if (r.data.msg === "修改成功") {
              curNode.setProp({
                attrs: {text: {text: nodename}}
              });
              ElMessage({
                type: "success",
                message: r.data.msg,
              })
              nodeflag = 1;
            } else {
              ElMessage({
                type: "error",
                message: r.data.msg,
              })
              graph.removeNode(nodeId);
            }
            nodeflag = 1;
          })
        }
      }
      formNode.value = {}; //清空表单
      formNode.value.domains = [];
      emit('afterCommit');
      ElMessage({
        type: "success",
        message: "提交成功",
      })
      editNodevisible.value = false;
    } else {
      ElMessage({
        type: "error",
        message: "提交失败",
      })
    }
  })
};
const deleteNode = function () {
  let tempnode = nodeCurrent;
  let nodename = formNode.value.name;
  let nodetype = nodeCurrent.getProp().data.types;
  if (nodetype === "Meta") { //删除元概念，只需要直接删除
    ElMessageBox.confirm(
        `将会删除名字为<${nodename}>的概念及所有关系与数据，是否继续？`,
        '删除警告',
        {
          confirmButtonText: "确认",
          cancelButtonText: "取消",
          type: 'warning',
        }
    )
        .then(() => {
          //去后端删除
          deleteMetaNode({ontologyName: ontologyName, nodeName: nodename}).then(r => {
            r = r.data
            if (r.data.msg === "删除成功") {
              //先删除其属性
              let attrlist = attributeList(nodename);
              for (let j in attrlist) {
                graph.removeNode(attrlist[j].id)
              }
              //删除该点
              graph.removeNode(tempnode.id);
              ElMessage({
                type: 'success',
                message: r.data.msg,
              })
            } else {
              ElMessage({
                type: 'error',
                message: r.data.msg,
              })
            }
          })
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '删除取消',
          })
        })
  } else if (nodetype === "attribute") { //删除属性
    let edges = graph.getEdges();
    let filteredge = edges.filter(edge => edge.getSourceNode().getAttrs().text.text === nodename
        && edge.getLabels()[0].attrs.label.text === 'attribute');
    let targetname = filteredge[0].getTargetNode().getAttrs().text.text;
    ElMessageBox.confirm(
        `将会删除名字为<${nodename}>的属性及其关系，是否继续？`,
        '删除警告',
        {
          confirmButtonText: "确认",
          cancelButtonText: "取消",
          type: 'warning',
        }
    )
        .then(() => {
          deleteAttribute({ontologyName: ontologyName, nodeName: targetname, attribute: nodename}).then(r => {
            r = r.data
            if (r.data.msg === "删除成功") {
              //删除该点
              graph.removeNode(tempnode.id);
              ElMessage({
                type: 'success',
                message: r.data.msg,
              })
            } else {
              ElMessage({
                type: 'error',
                message: r.data.msg,
              })
            }
          })
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '删除取消',
          })
        })
  } else {
    deleteOtherNode({ontologyName: ontologyName, nodeName: nodename}).then(r => { //删除实例概念节点
      r = r.data
      if (r.data.msg === "删除成功") {
        //先删除其属性
        let attrlist = attributeList(nodename);
        for (let k in attrlist) {
          graph.removeNode(attrlist[k].id)
        }
        //删除该点
        graph.removeNode(tempnode.id);
        ElMessage({
          type: 'success',
          message: r.data.msg,
        })
      } else {
        ElMessageBox.alert(
            r.data.msg,
            '删除警告',
        )
      }
    })
  }
  editNodevisible.value = false;
}
//endregion

//region 边操作
let createEdge = ref(false) //创建连线弹窗
let editEdge = ref(false) //编辑连线弹窗
const cancelCreateEdge = function () {
  graph.removeEdge(currentEdge.id);
  currentlabel.value = '';
  createEdge.value = false;
}
const submitEdge = function () {
  let editlabel = currentlabel.value;
  let tempedge = currentEdge;
  addRelation({
    ontologyName: ontologyName, sourceName: tempedge.getSourceNode().getProp().attrs.text.text,
    targetName: tempedge.getTargetNode().getProp().attrs.text.text, relationText: currentlabel.value
  }).then(r => {
    r = r.data
    if (r.data.msg === "关系添加成功") {
      tempedge.setLabels([{attrs: {label: {text: editlabel}},}]);
      ElMessage({
        type: "success",
        message: r.data.msg,
      })
    } else {
      ElMessage({
        type: "error",
        message: r.data.msg,
      })
      graph.removeEdge(tempedge.id);
    }
  })
  createEdge.value = false;
}
const submitEditEdge = function () {
  let editlabel = currentlabel.value;
  let tempedge = currentEdge;
  editRelation({
    ontologyName: ontologyName, sourceName: tempedge.getSourceNode().getProp().attrs.text.text,
    preRelation: preedgename, targetName: tempedge.getTargetNode().getProp().attrs.text.text,
    relationText: editlabel
  }).then(r => {
    r = r.data
    if (r.data.msg === "关系修改成功") {
      tempedge.setLabels([{attrs: {label: {text: editlabel}},}]);
      ElMessage({
        type: "success",
        message: r.data.msg,
      })
    } else {
      ElMessage({
        type: "error",
        message: r.data.msg,
      })
    }
  })
  editEdge.value = false;
}
const deleteEdge = function () {
  let tempe = currentEdge;
  let tempelabel = currentlabel.value;
  let sname = tempe.getSourceNode().getProp().attrs.text.text;
  let stype = tempe.getSourceNode().getProp().data.types;
  let tname = tempe.getTargetNode().getProp().attrs.text.text;
  let ttype = tempe.getTargetNode().getProp().data.types;
  ElMessageBox.confirm(
      `将会删除概念<${sname}>与概念<${tname}>之间的关系<${tempelabel}>，是否继续？`,
      '删除警告',
      {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: 'warning',
      }
  )
      .then(() => {
        deleteRelation({
          ontologyName: ontologyName, sourceName: sname, sourceType: stype,
          targetName: tname, targetType: ttype, relationText: tempelabel
        }).then(r => {
          r = r.data
          if (r.data.msg === "删除成功") {
            //删除这条边
            graph.removeEdge(tempe.id);
            ElMessage({
              type: 'success',
              message: r.data.msg,
            })
          } else {
            ElMessageBox.alert(
                r.data.msg,
                '删除警告',
            )
          }
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '删除取消',
        })
      })
  editEdge.value = false;
}
//endregion
</script>

<style scoped>
#buildercontainer {
  padding: 8px 10px;
  border-radius: 10px;
  box-sizing: border-box;
  height: 100%;

  &:first-child {
    padding-top: 0;
  }
}

#buildercontainer .el-form-item {
  margin: 20px 10px 10px 10px;
}

/deep/ .el-form-item__label {
  display: flex;
  align-items: center;
  padding: 0 0 0 0;
}

/deep/ .el-form-item__content {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 0 0 0;
}

.content {
  border: #efefef solid 1px;
  height: 100%;
  width: 100%;
  font-family: sans-serif;
  display: flex;
}

.app-stencil {
  width: 10%;
  border: 1px solid #f0f0f0;
  position: relative;
}

.app-content {
  flex: 1;
  height: 100%;
  margin-left: 5px;
  margin-right: 5px;
  box-shadow: 0 0 10px 1px #e9e9e9;
}

.aside {
  width: 18%;
  height: 100%;
  border: #eceef1 solid 1px;
  background: #eceef1;
  margin-right: 10px;
  margin-left: 5px;
}

.addiconbtn {
  background-color: transparent;
  border: transparent;
  color: rgb(18 84 153 / 77%);
  text-align: center;
}

.deleteiconbtn {
  display: flex;
  background-color: rgb(0, 57, 144);
  border: transparent;
  color: #f8f6f6;
  align-items: center;
}

.form-footer {
  display: inline-flex;
  align-items: center;
  margin-top: 30px;
  margin-left: 12%;
}

.cancelbtn2 {
  width: 50%;
  min-height: 30px;
  line-height: 0;
  margin: 0 5px 0 0;
  background-color: #f8f6f6;
  color: #597bcb;
  border-color: #f8f6f6;
}

.deletbtn1 {
  width: 50%;
  min-height: 30px;
  line-height: 0;
  margin: 0 5px 0 0;
  background-color: rgb(0, 57, 144);
  color: #ffffff;
  border-color: rgb(0, 57, 144);
}

.updatebtn1 {
  width: 50%;
  min-height: 30px;
  line-height: 0;
  margin: 0 5px 0 0;
  background-color: #597bcb;
  color: #f8f6f6;
  border-color: #597bcb;
}

.cancelbtn1 {
  width: 21%;
  min-height: 40px;
  line-height: 0;
  margin: 0 5px 0 0;
  background-color: #f8f6f6;
  color: #597bcb;
  border-color: #f8f6f6;
}

.deletbtn2 {
  width: 21%;
  min-height: 40px;
  line-height: 0;
  margin: 0 5px 0 0;
  background-color: rgb(0, 57, 144);
  color: #ffffff;
  border-color: rgb(0, 57, 144);
}

.determinebtn1 {
  width: 21%;
  min-height: 40px;
  line-height: 0;
  margin: 0 5px 0 0;
  background-color: #597bcb;
  color: #f8f6f6;
  border-color: #597bcb;
}

.dialog-footer {
  margin-top: 15px;
}

</style>
