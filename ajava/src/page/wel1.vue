<template>
  <div class="dashboard-container" v-if="isComponentReady">
    <!-- 头部 -->
    <header class="header big-title">
      <h1>云端协同的分布式电梯曳引机运行监控<br>与故障诊断平台系统</h1>
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
          <div v-for="(turbine, index) in turbines" :key="index" class="turbine-item" @click="goToDetail(turbine)">
            <img :src="getTurbineImage(turbine.status)" class="turbine-icon"/>
            <p>{{ turbine.name }}</p>
          </div>
        </div>
        <div v-if="buildings.length" class="ring-container">
          <!-- 世界坐标：只画椭圆 -->
          <svg class="connection-svg" width="100%" height="100%">
            <defs>
              <filter id="ellipseGlow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="6" result="blur"/>
                <feMerge>
                  <feMergeNode in="blur"/>
                  <feMergeNode in="SourceGraphic"/>
                </feMerge>
              </filter>
            </defs>
            <ellipse
              :cx="svgCenter.x"
              :cy="svgCenter.y"
              :rx="svgRadius.x"
              :ry="svgRadius.y"
              class="ellipse-ring"
            />
            <ellipse
              :cx="svgCenter.x"
              :cy="svgCenter.y"
              :rx="svgRadius.x - 6"
              :ry="svgRadius.y - 6"
              class="ellipse-ring-inner"
            />
          </svg>
          <!-- 每个 building 是一个局部坐标系 -->
          <div
            v-for="(building, bIndex) in buildingsWithStatus"
            :key="building.id"
            class="ring-item"
            :class="{
              active: hoverBuildingIndex === bIndex,
              dimmed: hoverBuildingIndex !== null && hoverBuildingIndex !== bIndex
            }"
            :style="getItemStyle(bIndex)"
          >
            <!-- building 图片 -->
            <img class="building-img" src="/public/img/wel/写字楼.png" />

            <!-- 射线 + 状态图标（局部 SVG） -->
            <svg class="building-rays" width="140" height="140">
              <g transform="translate(70,70)">
                <template
                  v-for="(status, sIndex) in building.statuses"
                  :key="'status-' + bIndex + '-' + sIndex"
                >
                  <!-- 射线 -->
                  <path
                    :d="getStatusRayPath(bIndex, sIndex, building.statuses.length)"
                    class="status-ray"
                  />

                  <!-- 状态图标 -->
                  <image
                    :href="getTurbineImage(status)"
                    width="40"
                    height="40"
                    :transform="getStatusIconTransform(bIndex, sIndex, building.statuses.length)"
                  />
                </template>
              </g>
            </svg>
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
import {getStateOfTree, getOverviewOfFarms, getAlarmList, getProjectId, searchNode } from "@/api/sw/wel"
import { data } from "autoprefixer";
import { forEach } from "jszip";

export default {
  name: "wel1.vue",
  components: {
    logo,
  },
  data() {
    return {
      hoverBuildingIndex: null,
      svgCenter: { x: 0, y: 0 },
      svgRadius: { x: 0, y: 0 },
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
    },

    buildingsWithStatus() {
      return this.buildings.map(building => {
        // 找出属于该 building 的 turbines
        const relatedTurbines = this.turbines.filter(t =>
          t.turbineCode && t.turbineCode.includes(building.label)
        )

        // status 去重
        const statusSet = new Set(
          relatedTurbines.map(t => t.status)
        )

        return {
          ...building,
          statuses: Array.from(statusSet).length
            ? Array.from(statusSet)
            : ['unaccessible']
        }
      })
    }
  },

  mounted() {
    this.getTrees()
    this.isComponentReady = true
    this.startTimeUpdate()

    window.addEventListener('resize', this.updateEllipse)
  },

  watch: {
    buildingsWithStatus() {
      this.$nextTick(this.updateEllipse)
    }
  },

  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.turbines = []
    this.alamData = []
    this.treeData = []
    window.removeEventListener('resize', this.updateEllipse)
  },

  methods: {
      updateEllipse() {
        const container = this.$el.querySelector('.ring-container')
        if (!container) return

        const { width, height } = container.getBoundingClientRect()

        const margin = 110 // ✅ 给 building + icon 的安全距离

        this.svgCenter = {
          x: width / 2,
          y: height / 2
        }

        this.svgRadius = {
          x: Math.max(width / 2 - margin, 240),
          y: Math.max(height / 2 - margin, 160)
        }
      },

    getEllipseOffset(index, count) {
      let angle

      if (count === 1) {
        angle = -Math.PI / 2
      } else if (count === 2) {
        angle = index === 0 ? -Math.PI : 0
      } else {
        angle = (2 * Math.PI / count) * index - Math.PI / 2
      }

      const rx = this.svgRadius.x
      const ry = this.svgRadius.y

      return {
        angle,
        x: Math.cos(angle) * rx,
        y: Math.sin(angle) * ry,
      }
    },

    getEllipseNormal(angle) {
      const rx = this.svgRadius.x
      const ry = this.svgRadius.y

      let nx = Math.cos(angle) / rx
      let ny = Math.sin(angle) / ry

      const len = Math.sqrt(nx * nx + ny * ny)
      nx /= len
      ny /= len

      return { nx, ny }
    },

    getStatusRayPath(buildingIndex, statusIndex, totalStatus) {
      const count = this.buildings.length
      const { angle } = this.getEllipseOffset(buildingIndex, count)
      const { nx, ny } = this.getEllipseNormal(angle)

      const spread = (statusIndex - (totalStatus - 1) / 2) * 10

      const startR = 32
      const rayLen = 70     // 👈 缩短
      const iconR = 92      // 👈 icon 一定比 ray 远

      const startX = nx * startR
      const startY = ny * startR

      const endX = nx * (startR + rayLen) + ny * spread
      const endY = ny * (startR + rayLen) - nx * spread

      const ctrlX = nx * (startR + rayLen * 0.5)
      const ctrlY = ny * (startR + rayLen * 0.5)

      return `M ${startX} ${startY} Q ${ctrlX} ${ctrlY} ${endX} ${endY}`
    },

    getStatusIconTransform(buildingIndex, i, total) {
      const count = this.buildingsWithStatus.length
      const { angle } = this.getEllipseOffset(buildingIndex, count)
      const { nx, ny } = this.getEllipseNormal(angle)

      const spread = (i - (total - 1) / 2) * 14
      const iconR = 92

      return `
        translate(${nx * iconR + ny * spread}, ${ny * iconR - nx * spread})
        translate(-9, -9)
      `
    },

    getItemStyle(index) {
      const count = this.buildings.length
      const { x, y, angle } = this.getEllipseOffset(index, count)

      const depth = Math.sin(angle)
      const scale = 0.7 + (depth + 1) / 2 * 0.4
      const opacity = 0.75 + (depth + 1) / 2 * 0.25

      const buildingHeight = 78 // 图片高度
      const lift = buildingHeight * 0.35 // 👈 向内抬一点

      return {
        transform: `
          translate(-50%, -50%)
          translate(${x}px, ${y - lift}px)
          scale(${scale})
        `,
        zIndex: Math.round(2000 + depth * 500),
        opacity
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
      // console.log(node)
      this.alamData = []
      this.turbines = []
      // 添加一个强制刷新的变量
      this.forceRenderKey = Date.now() // 使用时间戳作为唯一key
      // 判断是否是第二级节点（即：它的父节点是第一级）
      if (treeNode.level === 2) {
        this.turbineModel = node.label
        this.farmName = treeNode.parent.label
        this.buildings = treeNode.parent.data.children
      } else if(treeNode.level ===3) {
        this.buildings = treeNode.parent.parent.data.children
      }else {
        this.$message.warning("请选择具体机型")
        this.buildings = treeNode.data.children
      }

      console.log(this.buildings[0])

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
        // console.log(res.data.data)
        this.turbines = res.data.data

        if (this.turbines.length !== 0) {
          await this.$nextTick(() => {
            this.renderChart()
            this.getAlarms()
          })
        }
      } catch (error) {
        console.error("获取 `turbines` 失败", error)
      }
      console.log(this.turbines[0])

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

    },

    // 跳转到风机详情页面
    goToDetail(turbine) {
      this.$router.push({
        path: '/turbineDetail',
        query: {
          turbineName: turbine.name,
          farmName: this.farmName,
          turbineModel: this.turbineModel,
          status: turbine.status
        }
      })
    }
  },
};
</script>

<style scoped lang="scss">

@keyframes ellipse-flow {
  from {
    stroke-dashoffset: 0;
  }
  to {
    stroke-dashoffset: -300;
  }
}

.ellipse-ring {
  fill: none;

  stroke: rgba(0, 255, 255, 0.7);
  stroke-width: 2.2;

  /* 科技感虚线 */
  stroke-dasharray: 12 18;

  /* 发光 */
  filter: url(#ellipseGlow);

  /* 柔和一点 */
  opacity: 0.9;
  animation: ellipse-flow 18s linear infinite;
}

.ellipse-ring-inner {
  fill: none;
  stroke: rgba(120, 255, 255, 0.9);
  stroke-width: 1;
  stroke-dasharray: 4 12;
  opacity: 0.6;
}

/* 默认状态 */
.status-ray {
  stroke: rgba(0, 255, 255, 0.55);
  stroke-width: 1.2;
  transition: all 0.25s ease;
}

.building-rays image {
  transition: transform 0.25s ease, filter 0.25s ease, opacity 0.25s ease;
}

/* hover 的 building */
.ring-item.active .status-ray {
  stroke: rgba(0, 255, 255, 1);
  stroke-width: 2.4;
  filter: drop-shadow(0 0 8px rgba(0,255,255,1));
}

.ring-item.active .building-rays image {
  filter: drop-shadow(0 0 10px rgba(0,255,255,0.9));
  transform: scale(1.15);
  opacity: 1;
}

/* 其他 building 变暗 */
.ring-item.dimmed {
  opacity: 0.35;
  transition: opacity 0.25s ease;
}

.building-img {
  position: relative;
  z-index: 2;
}

.status-ray {
  stroke: rgba(0, 255, 255, 0.9);
  stroke-width: 1.5;
  stroke-dasharray: 4 6;
  fill: none;
  filter: drop-shadow(0 0 6px rgba(0,255,255,0.8));
}

.building-rays {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  overflow: visible;
  z-index: 20;   /* 👈 提高 */
}

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
  height: 400px;
  perspective: 1200px;
  transform-style: preserve-3d;
  margin-top: 15%;
  overflow: visible !important;
}

.connection-svg {
  position: absolute;
  inset: 0;
  z-index: 1;
}

.ring-item {
  position: absolute;
  top: 50%;
  left: 50%;
  transform-style: preserve-3d;
  z-index: 10;
  overflow: visible;
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
  background: url('/img/wel/title-bg.png') center / contain no-repeat;
  padding: 12px 20px; /* 增大 padding，撑开背景 */
  text-indent: 20px; /* 让文字整体右移 */
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 最小高度为视口高度 */
  height: 100vh; /* 固定为视口高度，便于内部滚动 */
  background: url("/img/wel/bg.png") no-repeat center center fixed;
  background-size: cover; /* 确保图片完整显示 */
  overflow-y: auto; /* 允许纵向滚动 */
  overflow-x: hidden; /* 隐藏横向滚动 */
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
  gap: 20px; /* 添加间距 */
  min-height: 0; /* 允许flex收缩 */
  align-items: stretch;
  overflow-y: auto; /* 内容超出时在内容区滚动 */
  overflow-x: hidden;
}

/* 设备树整体容器 */
.sidebar-left {
  display: flex;
  flex-direction: column;
  flex: 0 0 220px;
  min-width: 200px; /* 设置最小宽度 */
  max-width: 260px; /* 设置最大宽度 */
  min-height: 0; /* 允许内部滚动 */
  background: rgba(255, 255, 255, 0.1); /* 半透明背景 */
  padding: 15px;
  border-radius: 8px;
  overflow-y: auto; /* 内容超出时滚动 */
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
  flex: 1; /* 占据剩余空间 */
  overflow-y: auto; /* 当内容超出时滚动 */
  background: transparent;
  color: white;
  font-size: 14px;
  min-height: 0; /* 确保flex子元素可以正确缩小 */
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
  z-index: 3000;
  opacity: 1;
}


.center {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); /* 自动换行 */
  grid-auto-rows: minmax(80px, auto); /* 设置行高 */
  row-gap: 15px; /* 调整行间距 */
  column-gap: 15px;
  width: 100%; /* 让 grid 适应父容器 */
  max-width: 1000px; /* 控制最大宽度 */
  overflow: visible; /* 显示全部内容 */
  padding: 10px; /* 添加内边距 */
}

.turbine-item {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: center;
  border-radius: 8px;
  cursor: pointer; /* 添加鼠标指针效果 */
  transition: all 0.3s ease; /* 添加过渡动画 */
}

.turbine-item:hover {
  transform: scale(1.1); /* 鼠标悬停时放大 */
  background: rgba(0, 204, 153, 0.2); /* 添加高亮背景 */
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
  flex: 0 0 360px;
  min-width: 300px; /* 设置最小宽度 */
  max-width: 420px; /* 设置最大宽度 */
  min-height: 0; /* 允许内部滚动 */
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow-y: auto; /* 内容超出时滚动 */
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 20px;

  .status-info {
    display: flex;
    flex-direction: column;
    flex-shrink: 0; /* 防止压缩 */
    min-height: 350px; /* 确保图表有足够空间 */
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
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
  min-height: 0; /* 允许内部滚动 */
  overflow-y: auto; /* 内容超出时滚动 */
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
