<template>
  <basic-container>
    <div style="width: 100%;">
      <el-row>
        <el-col :span="16">
          <el-row :gutter="10">
            <el-col :span="6">
              <text style="color: #0c5aab;margin-right: 50px;font-size: 18px">实例概念颜色：蓝色</text>
            </el-col>
            <el-col :span="6">
              <text style="color: #FF5511;margin-right: 50px;font-size: 18px">元概念颜色：橙色</text>
            </el-col>
            <el-col :span="6">
              <text style="color: #0a9176;margin-right: 50px;font-size: 18px">事件颜色：绿色</text>
            </el-col>
            <el-col :span="6">
              <text style="color: #FFBB66;font-size: 18px">属性颜色：黄色</text>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="8">
          <el-button class="editBtn" @click="toEditOntology" size="large">编辑</el-button>
        </el-col>
      </el-row>
    </div>
    <div ref="net" id="net" style="height: 700px;width: 100%">
    </div>
  </basic-container>
</template>

<script setup>
import {nextTick, onMounted, ref} from "vue";
import {Network} from "vis-network";
import {DataSet} from "vis-data";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import BasicContainer from "components/BasicContainer/main.vue";
import {getOntodata} from "@/api/kg/ontologyModel";

let net = ref(null)
let network = null
let ontologyName = ''
let dataInfo = []
let data = {
  nodes: [],
  edges: [],
}
let poses = {}
const $route = useRoute();
const $router = useRouter();

onMounted(async () => {
  ontologyName = "维修知识本体";
  init();
})
//获取本体数据
const init = function () {
  nextTick(async () => {
    await getOntodata(ontologyName).then(r => {
      dataInfo = r.data.data;
      if (dataInfo.length === 0) {
        ElMessage({
          type: "warning",
          message: "本体不存在！",
        })
      }
      let visnet = buildVisNetOwl(dataInfo);
      data.nodes = visnet.nodes;
      data.edges = visnet.edges;
      refresh();
    })
  })
}

//region 组织数据
const buildVisNetOwl = function (list) {
  let visnet = {
    nodes: [], edges: []
  };
  let nodeSet = new Set();
  for (const node of list) {
    if (!nodeSet.has(node.entityFromId)) {
      let labelF = node.entityFromName;
      let colorF = null;//颜色设置
      let shapeF = null;//形状设置
      if (node.entityFromName.length > 10) {
        labelF = node.entityFromName.substring(0, 10) + "..."
      }
      switch (node.entityFromTypes) {
        case "":
          colorF = "#FFBB66",
              shapeF = "box";
          break;
        case "Meta":
          colorF = "#FF5511",
              shapeF = null;
          break;
        case "Event":
          colorF = "#0a9176",
              shapeF = null;
          break;
        default:
          colorF = null,
              shapeF = null;
      }
      visnet.nodes.push({
        "id": node.entityFromId,
        "label": labelF,
        'color': colorF,
        "shape": shapeF
      });
      nodeSet.add(node.entityFromId);
    }
    if (!nodeSet.has(node.entityToId)) {
      let colorT = null;//颜色设置
      let shapeT = null;//形状设置
      let labelT = node.entityToName;
      if (node.entityToName.length > 10) {
        labelT = node.entityToName.substring(0, 10) + "..."
      }
      switch (node.entityToTypes) {
        case "":
          colorT = "#FFBB66",
              shapeT = "box";
          break;
        case "Meta":
          colorT = "#FF5511";//颜色设置
          shapeT = null;//形状设置
          break;
        case "Event":
          colorT = "#0a9176",
              shapeT = null;
          break;
        default:
          colorT = null;//颜色设置
          shapeT = null;//形状设置

      }
      visnet.nodes.push({
        "id": node.entityToId,
        "label": labelT,
        'color': colorT,
        "shape": shapeT
      });
      nodeSet.add(node.entityToId);
    }
    visnet.edges.push({
      "arrow": "to",
      "from": node.entityFromId,
      "to": node.entityToId,
      "label": node.relation
    });

  }
  return visnet;
}
const buildOntologyData = function (list) {
  let graphdata = {
    nodes: [], edges: []
  };
  let nodeSet = new Set();
  for (const node of list) {
    if (!nodeSet.has(node.entityFromId)) {
      let labelF = node.entityFromName;
      let nameF = node.entityFromName;
      let colorF = null;//颜色设置
      let fillcolorF = null;//填充色
      let shapeF = null;//形状设置
      let typeF = node.entityFromTypes;
      let attributeF = node.entityFromAtrribute;
      let widthF = null;
      let heightF = null;
      if (node.entityFromName.length > 10) {
        labelF = node.entityFromName.substring(0, 10) + "..."
      }
      switch (typeF) {
        case "":
          colorF = "#FFBB66";
          fillcolorF = 'rgba(255,187,102,0.6)';
          shapeF = "default-rect-node";
          widthF = 80;
          heightF = 30;
          typeF = "attribute";
          break;
        case "Meta":
          colorF = "#FF5511";
          fillcolorF = 'rgba(255,85,17,0.6)';
          shapeF = 'default-circle-node';
          widthF = 80;
          heightF = 80;
          break;
        case "Event":
          colorF = "#0a9176";
          fillcolorF = 'rgba(10,145,118,0.6)';
          shapeF = 'default-event-node';
          widthF = 80;
          heightF = 80;
          break;
        default:
          colorF = "#0c5aab";
          fillcolorF = 'rgba(12,90,171,0.6)'
          shapeF = 'default-circle-node';
          widthF = 80;
          heightF = 80;
      }
      if (typeF === "Event") {
        graphdata.nodes.push({
          id: node.entityFromId,
          shape: shapeF,
          label: labelF,
          width: widthF,
          height: heightF,
          data: {
            types: typeF,
            attribute: attributeF,
          },
          attrs: {
            body: {
              stroke: colorF,
              strokeWidth: 1,
              fill: fillcolorF,
              rx: 6,
              ry: 6,
            },
            text: {
              text: nameF,
            }
          },
        });
      } else {
        graphdata.nodes.push({
          id: node.entityFromId,
          shape: shapeF,
          label: labelF,
          width: widthF,
          height: heightF,
          data: {
            types: typeF,
            attribute: attributeF,
          },
          attrs: {
            body: {
              stroke: colorF,
              strokeWidth: 1,
              fill: fillcolorF,
              rx: 6,
              ry: 6,
            },
            text: {
              text: nameF,
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
        });
      }
      nodeSet.add(node.entityFromId);
    }
    if (!nodeSet.has(node.entityToId)) {
      let colorT = null;//颜色设置
      let fillcolorT = null;//填充色
      let shapeT = null;//形状设置
      let labelT = node.entityToName;
      let nameT = node.entityToName;
      let typeT = node.entityToTypes;
      let attributeT = node.entityToAtrribute;
      let widthT = null;
      let heightT = null;
      if (node.entityToName.length > 10) {
        labelT = node.entityToName.substring(0, 10) + "..."
      }
      switch (typeT) {
        case "":
          colorT = "#FFBB66";
          fillcolorT = 'rgba(255,187,102,0.6)';
          shapeT = "default-rect-node";
          widthT = 80;
          heightT = 30;
          typeT = "attribute";
          break;
        case "Meta":
          colorT = "#FF5511";
          fillcolorT = 'rgba(255,85,17,0.6)';
          shapeT = 'default-circle-node';
          widthT = 80;
          heightT = 80;
          break;
        case "Event":
          colorT = "#0a9176";
          fillcolorT = 'rgba(10,145,118,0.6)';
          shapeT = 'default-event-node';
          widthT = 80;
          heightT = 80;
          break;
        default:
          colorT = "#0c5aab";
          fillcolorT = 'rgba(12,90,171,0.6)';
          shapeT = 'default-circle-node';
          widthT = 80;
          heightT = 80;
      }
      graphdata.nodes.push({
        id: node.entityToId,
        shape: shapeT,
        label: labelT,
        width: widthT,
        height: heightT,
        data: {
          types: typeT,
          attribute: attributeT,
        },
        attrs: {
          body: {
            stroke: colorT,
            strokeWidth: 1,
            fill: fillcolorT,
            rx: 6,
            ry: 6,
          },
          text: {
            text: nameT,
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
      });
      nodeSet.add(node.entityToId);
    }
    let colorE = null; //边颜色设置
    switch (node.entityFromTypes) {
      case "":
        colorE = "#FFBB66";
        break;
      case "Meta":
        colorE = "#FF5511";
        break;
      case "Event":
        colorE = "#0a9176";
        break;
      default:
        colorE = "#0c5aab";
    }
    let tempEdges = graphdata.edges.filter(dict => {
      return dict.source === node.entityFromId && dict.target === node.entityToId;
    })
    let edgeIndex = graphdata.edges.indexOf(tempEdges[0]);
    if (tempEdges.length > 0) {
      graphdata.edges[edgeIndex].labels.push(node.relation)
    } else {
      graphdata.edges.push({
        source: node.entityFromId,
        target: node.entityToId,
        labels: [node.relation],
        attrs: {
          line: {
            stroke: colorE,
          },
        },
      });
    }
  }
  return graphdata;
}
//endregion

let options = null;
options = {
  autoResize: true, //网络将自动检测其容器的大小调整，并相应地重绘自身
  // 设置节点样式
  nodes: {
    mass: 1.5, //节点之间的斥力
    shape: "circle", //节点的外观。为circle时label显示在节点内，为dot时label显示在节点下方
    margin: 10
  },
  edges: {
    arrows: {to: true}, //箭头指向to
  },
  // 布局
  layout: {},
};
const refresh = () => {
  let d = {
    nodes: new DataSet(data.nodes),
    edges: new DataSet(data.edges)
  }

  nextTick(() => {
    network = new Network(net.value, d, options);
    network.once("afterDrawing", () => {
      poses = network.getPositions();
      network.focus(data.nodes[0].id, {scale: 1});
    });
    network.once("dragEnd", () => {
      poses = network.getPositions();
    });
  })
}

const toEditOntology = function () {
  let graphdata = buildOntologyData(dataInfo)
  //输入位置信息
  for (let i in graphdata.nodes) {
    graphdata.nodes[i].x = poses[graphdata.nodes[i].id].x;
    graphdata.nodes[i].y = poses[graphdata.nodes[i].id].y + 200;
  }
  $router.push({
    path: "./modelTool",
    query: {
      graphData: encodeURIComponent(JSON.stringify(graphdata)),
    }
  });
}



</script>

<style scoped>
.editbtn1 {
  line-height: 0;
  position: relative;
  left: 100px;
  bottom: 4px;
  background-color: #5a97d5 !important;
  color: #ffffff !important;
  border-color: #5a97d5 !important;
}
</style>
