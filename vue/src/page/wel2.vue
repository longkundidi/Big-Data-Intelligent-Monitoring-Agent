<template>
  <div class="dashboard-container" v-if="isComponentReady">
    <!-- 头部 -->
    <header class="header big-title">
      <h1>云边端协同的分布式电梯曳引机运行监控<br>与故障诊断平台系统</h1>
       <div class="time-display">
          <span class="time-date">{{ formattedDate }}</span>
          <span> </span>
          <span id="current-time" class="time-date"></span>
       </div>
    </header>

    <!-- 内容区 -->
    <div class="content">
      <!-- 左侧结构树 -->
      <el-container class="sidebar-left">
        <h3 class="title" style="text-align: left;">监测结构树</h3>
        <div class="search-wrapper">
          <input type="text" placeholder="搜索..." class="search_input" v-model="searchText">
           <button type="button" class="inner-icon-btn" @click="getTrees">
            🔍
           </button>
        </div>
        <el-tree
            ref="treeRef"
            :data="treeData"
            :props="defaultProps"
            default-expand-all
            node-key="id"
            highlight-current
            :show-line="false"
            @node-click="handleNodeClick"
            class="tree"
        ></el-tree>
      </el-container>

      <div style="display: flex;flex-direction:column;flex: 3;">
        <!-- 状态栏 -->
        <div class="status-bar" v-show="turbines.length!==0">
          <div v-for="status in statusList" :key="status.name" class="status-item">
            <span :style="{ backgroundColor: status.color }" class="status-icon"></span>
            <span class="status-text">{{ status.name }}：{{ status.count }}</span>
          </div>
        </div>

        <!-- 风机状态展示 -->
        <div class="center" v-show="turbines.length!==0">
          <div v-for="(turbine, index) in turbines" :key="index" class="turbine-item">
            <img :src="getTurbineImage(turbine.status)" class="turbine-icon"/>
            <p>{{ turbine.name }}</p>
          </div>
        </div>
        <div v-if="buildings.length !== 0" :key="buildings.length" class="ring-container">
          <div v-for="(building, index) in buildings" :key="index" class="ring-item" :style="getItemStyle(index)">
            <img src="/public/img/wel/写字楼.png" />
          </div>
        </div>

      </div>

      <!-- 右侧状态信息 -->
      <div class="right">
        <div class="status-info">
          <h3 class="title" style="text-align: left;">状态信息</h3>
          <div ref="statusChart" class="chart"></div>
        </div>

        <!-- 历史数据 -->
        <div class="history-data">
          <h3 class="title" style="text-align: left;">
            报警信息</h3>
          <el-table :data="turbines.length===0?[]:alamData" style="width: 100%;max-height: 500px;overflow-y: auto;" :border="false">
            <el-table-column prop="dcTime" label="时间" width="160" align="center"></el-table-column>
            <el-table-column prop="turbineName" label="实例对象名" align="center"></el-table-column>
            <el-table-column prop="components" label="零部件名称" width="120" align="center"></el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";
import logo from './myIndex/logo.vue'
import {getStateOfTree, getOverviewOfFarms, getAlarmList, getProjectId, searchNode} from "@/api/sw/wel"
import { data } from "autoprefixer";

export default {
  name: "wel1.vue",
  components: {
    logo,
  },
  data() {
    return {
      buildings:[],
      searchText:'',
      radiusX: 320,   // 横向半径（拉开左右）
      radiusZ: 420,   // 前后深度（决定远小近大）
      tilt: -12,      // 整体俯视角度（关键！）
      baseAngle: 0,
      isComponentReady: false,
      currentTime: '',        // 当前时间
      formattedDate: '',      // 格式化日期
      timer: null,           // 定时器引用
      isComponentReady: false,
      // 设备树数据
      treeData: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      turbines: [],
      farmName: '',
      turbineModel: "",
      alamData: [],
    }
  },
  computed: {
    statusList() {
      // 统计不同状态的风机数量
      let normalCount = 0, unaccessibleCount = 0, abnormalCount = 0;

      this.turbines.forEach(turbine => {
        if (turbine.status === "normal") normalCount++;
        if (turbine.status === "abnormal") abnormalCount++;
        if (turbine.status === "unaccessible") unaccessibleCount++;
      });

      return [
        {name: "待接入系统", count: unaccessibleCount, color: "rgb(58, 151, 248)"},
        {name: "正常", count: normalCount, color: "#00cc99"},
        {name: "报警", count: abnormalCount, color: "rgb(251, 97, 112)"}
      ];
    }
  },

  mounted() {
    this.getTrees(); // 先加载数据
    this.isComponentReady = true; // 确保数据加载后再渲染
    this.startTimeUpdate();
  },

  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.turbines = []
    this.alamData = []
    this.treeData = []
  },

  methods: {
    getItemStyle(index) {
      const count = this.turbines.length
      const angle = (360 / count) * index + this.baseAngle
      const rad = (angle * Math.PI) / 180

      // 椭圆环，而不是正圆
      const x = Math.sin(rad) * this.radiusX
      const z = Math.cos(rad) * this.radiusZ

      // 远小近大（压缩后方）
      let scale = (z + this.radiusZ) / (2 * this.radiusZ)
      scale = 0.35 + scale * 0.75   // ⭐ 这是“像你截图”的关键比例

      return {
        transform: `
          translate(-50%, -50%)
          translateX(${x}px)
          translateZ(${z}px)
          rotateX(${this.tilt}deg)
          scale(${scale})
        `,
        zIndex: Math.round(scale * 100),
        opacity: scale < 0.45 ? 0.35 : 1
      }
    },
    
    startTimeUpdate() {
      this.updateTime(); // 立即执行一次
      this.timer = setInterval(() => {
        this.updateTime();
      }, 1000);
    },

    // 更新时间显示
    updateTime() {
      const now = new Date();
      
      // 格式化时间：时:分:秒
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      this.currentTime = `${hours}:${minutes}:${seconds}`;
      
      // 格式化日期：年-月-日 星期X
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      this.formattedDate = `${year}年${month}月${day}日`; 
      
      // 更新 DOM 显示（纯 JS 方式，与 Vue 数据绑定并存）
      const timeElement = document.getElementById('current-time');
      if (timeElement) {
        timeElement.textContent = this.currentTime;
      }
    },
    searchByStr(searchContent){
      this.treeData=[];
      return searchNode({searchKey:searchContent});
    },
    async getTrees() {
      try {
        let res;
        const userId = this.$store.state.user.userInfo.userId;
        if (this.searchText.trim() === ''){
          res = await getOverviewOfFarms({ userId: userId });
        }
        else res = await this.searchByStr(this.searchText);
        this.treeData = this.addIndexAsId(res.data.data);

        if (this.treeData.length > 0) {
          const validFarm = this.treeData.find(farm => farm.children?.some(model => model.children?.length > 0));
          if (validFarm) {
            this.farmName = validFarm.label;
            const validModel = validFarm.children.find(model => model.children?.length > 0);
            if (validModel) {
              this.turbineModel = validModel.label;
              await this.$nextTick(() => {
                if (this.$refs.treeRef) {
                  this.$refs.treeRef.setCurrentKey(validModel.id)
                }
              })
            }
          }
        }
        await this.getTurbines();
      } catch (error) {
        console.error("加载数据失败", error);
      }
    },

    addIndexAsId(tree, parentIndex = '') {
      return tree.map((node, idx) => {
        const newId = `${parentIndex}-${idx}` // 生成唯一 ID
        const newNode = { ...node, id: newId }
        if (newNode.children && newNode.children.length > 0) {
          newNode.children = this.addIndexAsId(newNode.children, newId)
        }
        return newNode
      })
    },

    //获取节点
    handleNodeClick(node, treeNode, component) {
      this.alamData = []
      this.turbines = []
      console.log(node.children);
      console.log(treeNode.data.children);
      // 添加一个强制刷新的变量
      this.forceRenderKey = Date.now() // 使用时间戳作为唯一key
      // 判断是否是第二级节点（即：它的父节点是第一级）
      if (treeNode.level === 2) {
        this.turbineModel = node.label
        this.farmName = treeNode.parent.label
        this.buildings = treeNode.parent.data.children
      } else if(treeNode.level ===3) {
        this.turbineModel = treeNode.parent.label
        this.farmName = treeNode.parent.parent.label
        this.buildings = treeNode.parent.parent.data.children
      }else {
        this.$message.warning("请选择具体机型")
        this.buildings = treeNode.data.children
      }

    //  this.getTurbines()
      this.$nextTick(() => {
        this.getTurbines()
      })

    },

    //获取中部风机列表
    async getTurbines() {
      if (!this.farmName || !this.turbineModel) {
        console.warn("farmName 或 turbineModel 为空，不执行 API 请求")
        return
      }
      try {
        const res = await getStateOfTree({ farmName: this.farmName, turbineModel: this.turbineModel })
        this.turbines = Array.isArray(res?.data?.data) ? res.data.data : []

        if (this.turbines.length === 0) {
          this.alamData = []
        }

        await this.$nextTick(() => {
          this.renderChart()
          if (this.turbines.length !== 0) {
            this.getAlarms()
          }
        })
      } catch (error) {
        console.error("获取 `turbines` 失败", error)
      }

    },

    //根据状态对应相应图标
    getTurbineImage(status) {
      if (status === "normal") {
        return "img/wel/齿轮-正常.png"; // 正常
      } else if (status === "abnormal") {
        return "img/wel/齿轮-报警.png"; // 异常
      } else if (status === "unaccessible") {
        return "img/wel/齿轮-未接入.png"; // 未接入
      }
    },

    //获取饼状图数据
    renderChart() {
      if (!this.$refs.statusChart) {
        console.warn("ECharts 容器未找到");
        return;
      }
      const chart = echarts.init(this.$refs.statusChart);

      //获取统计图的数据
      // 分别计算三种状态下的数据
      let normalCount = 0
      let abnormalCount = 0
      let unaccessCount = 0
      let chartData = []

      this.turbines.forEach(item => {
        switch (item.status) {
          case "normal":
            normalCount++
            break;
          case "abnormal":
            abnormalCount++
            break;
          case "unaccessible":
            unaccessCount++
            break;
        }
      })
      chartData.push(
          {value: unaccessCount, name: "未接入", itemStyle: {color: "rgb(58, 151, 248)"}},
          {value: normalCount, name: "正常", itemStyle: {color: "#00cc99"}},
          {value: abnormalCount, name: "报警", itemStyle: {color: "#fd626e"}},
      )
      chart.setOption({
        title: [
          {
            text: "场景下设备状态饼状图", // **主标题**
            left: "center",
            top: "0%", // **让标题位于图表上方**
            textStyle: {
              fontSize: 18,
              color: "#ffffff",
            },
          },
          {
            text: `{count|${this.turbines.length}}\n设备总数`, // 文字内容
            left: "48%",
            top: "42%", // 调整垂直位置
            textAlign: "center",
            textStyle: {
              fontSize: 16,
              fontWeight: "bold",
              color: "#fff",
              rich: {
                count: {
                  fontSize: 18, // **单独设置风机数量大小**
                  fontWeight: "bold",
                  color: "#FFA500",
                  padding: [0, 0, 10, 0]
                },
              },
            },
          }
        ],
        tooltip: {trigger: "item"},
        legend: {
          orient: "horizontal",
          bottom: "0%",
          left: "center", // **让图例水平居中**
          textStyle: { color: "#fff" },
        },
        series: [
          {
            type: "pie",
            radius: ["40%", "60%"],
            center: ["50%", "50%"],
            label: {
              show: true,
              color: "#fff",
              fontSize: 12,
            },
            data: chartData
          },
        ],
      })

    },
    //获取报警信息
    getAlarms() {
      let projectId = 0
      getProjectId({project: this.farmName, productModel: this.turbineModel}).then(res => {
        projectId = res.data.data
        getAlarmList({proId: projectId}).then(res => {
          if (res.data.data.length !== 0) {
            res.data.data.forEach(item => {
              this.alamData.push({
                dcTime: item.dcTime,
                turbineName: item.turbineName,
                components: item.nodeName
              })
            })
          } else {
            this.alamData = []
          }
        })
      })

    }
  },
};
</script>

<style scoped lang="scss">
.inner-icon-btn {
  position: absolute;
  right: 8px; /* 在input内部 */
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #666;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.search-wrapper {
  display: flex;
  justify-content: flex-end; /* 搜索框右对齐 */
  position: relative; //1
}
.inner-icon-btn {//1
  position: absolute;
  right: 8px; /* 在input内部 */
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #666;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.inner-icon-btn:hover {
  color: #409eff;
}
.search-input {
  width: 200px; /* 设置固定宽度 */
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}
.ring-container {
  position: relative;
  width: 100%;
  height: 300px;
  perspective: 1200px; /* 比之前更大，画面更平 */
  transform-style: preserve-3d;
}

.ring-item {
  position: absolute;
  top: 75%;           /* ⭐ 故意下移，符合你截图 */
  left: 50%;
  transform-style: preserve-3d;
}

.ring-item img {
  width: 78px;        /* 接近你截图里的比例 */
  height: auto;
}

.time-display{
  margin-top: 0; /* 让标题紧贴上方 */
  text-align: center;
  // background: url('/img/wel/big-title.png') center / cover no-repeat;
  padding: 12px 20px; /* 增大 padding，撑开背景 */
  text-indent: 20px; /* 让文字整体右移 */
  color: aqua;
}
.time-date{
  margin-top: 0; /* 让标题紧贴上方 */
  background: url('/img/wel/big-title.png') center / cover no-repeat;
  padding: 12px 20px; /* 增大 padding，撑开背景 */
  text-indent: 20px; /* 让文字整体右移 */
  color: aqua;
}
.big-title {
  margin-top: 0; /* 让标题紧贴上方 */
  text-align: center;
  background: url('/img/wel/big-title.png') center / cover no-repeat;
  padding: 12px 20px; /* 增大 padding，撑开背景 */
  text-indent: 20px; /* 让文字整体右移 */
  color: aqua;
}

.title {
  margin-top: 0; /* 让标题紧贴上方 */
  text-align: left;
  padding: 12px 20px; /* 增大 padding，撑开背景 */
  text-indent: 20px; /* 让文字整体右移 */
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  background: url("/img/wel/bg.png") no-repeat center center fixed;
  background-size: cover; /* 确保图片完整显示 */
  overflow: visible;
  color: white;
  font-family: Arial, sans-serif;
}

.header {
  display: flex;
  flex-direction: column; /* 使标题在上，logo+文字在下 */
  align-items: center; /* 标题水平居中 */
  justify-content: center;
  padding: 15px;
  font-size: 18px;
  position: relative;
}

/* LOGO 左对齐 */
.header-left {
  position: absolute;
  left: 20px; /* 靠左侧一定距离 */
  display: flex;
  align-items: center;
}

.content {
  display: flex;
  flex: 1;
  padding: 20px;
  height: 100vh; /* 让内容区域占满整个视口 */
}

/* 设备树整体容器 */
.sidebar-left {
  display: flex;
  flex-direction: column;
  width: 10%;
  height: 100vh; /* 固定高度，占据整个视口 */
  background: rgba(255, 255, 255, 0.1); /* 半透明背景 */
  padding: 15px;
  border-radius: 8px;
  overflow: hidden; /* 避免内部内容溢出 */
  flex: 1;
}


/* 设备树标题 */
.sidebar-left h3 {
  text-align: center;
  font-size: 18px;
  color: #ffffff;
  margin-bottom: 10px;
}

/* 设备树内容 */
.tree {
  height: calc(100vh - 120px); /* 让设备树占据剩余空间 */
  overflow-y: auto; /* 当内容超出时滚动 */
  background: transparent;
  color: white;
  font-size: 14px;
}

/* Vue 2 方式 */
::v-deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: rgba(78, 187, 207, 0.3) !important; /* 选中时背景颜色 */
  color: rgba(78, 187, 207, 1) !important; /* 选中时字体颜色 */
}

/* 设备树选中高亮 */
::v-deep(.el-tree-node__content:hover) {
  background-color: rgba(0, 204, 153, 0.2) !important;
}

/* 移除 el-tree 背景色 */
::v-deep(.el-tree) {
  background: transparent !important;
}

/* 确保所有 tree 节点背景都是透明的 */
::v-deep(.el-tree-node__content) {
  background: transparent !important;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: center;
  //width: 80%;
  gap: 30px;
  margin-bottom: 20px;
  background: url('/img/wel/status-bg.png') center / 500px 100% no-repeat;
  padding: 12px 20px; /* 增大 padding，撑开背景 */
}

.status-item {
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #fff;
}

.status-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 5px;
}


.center {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); /* 自动换行 */
  grid-auto-rows: minmax(50px, auto); /* 设置行高 */
  row-gap: 10px; /* 调整行间距 */
  column-gap: 15px;
  width: 100%; /* 让 grid 适应父容器 */
  max-width: 1000px; /* 控制最大宽度 */
  overflow: auto;

}

.turbine-item {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: center;
  border-radius: 8px;
}


.turbine-item p {
  margin: 0; /* 移除默认的上下边距 */
}

.turbine-icon {
  vertical-align: middle; /* 或者使用 top */
  font-size: 40px;
  display: block;
  margin: auto;
}

.right {
  flex: 1;
  width: 10%;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow-y: auto;

  .status-info {
    display: flex;
    flex-direction: column;
  }

  .chart {
    width: 100%;  /* 让 ECharts 占满父容器 */
    height: 100%; /* 确保有高度，不受限 */
    display: flex;
    justify-content: center; /* 确保子元素居中 */
    align-items: center;
    min-width: 300px; /* 设最小宽度 */
    min-height: 300px; /* 设最小高度 */
  }
}

.history-data {
  margin-top: 20px;
}

::v-deep(.el-table) {
  background: transparent !important;
  color: white;

}

::v-deep(.el-table__header) {
  background: transparent !important;
  color: white;
}

::v-deep(.el-table__body) {
  background: transparent !important;
}

::v-deep(.el-table th),
::v-deep(.el-table tr),
::v-deep(.el-table td) {
  border: none !important; /* 彻底移除所有边框 */
  background: transparent !important; /* 去除表格单元格的背景色 */
  border-bottom: 1px solid rgba(65, 228, 187, 0.1) !important; /* 修改底部边框颜色 */
}


/*去掉 Element UI 默认的表格底部横线*/
::v-deep(.el-table__inner-wrapper::before) {
  display: none !important;
}
::v-deep(.el-tree-node__label) {
  color: #ffffff !important;
}

</style>
