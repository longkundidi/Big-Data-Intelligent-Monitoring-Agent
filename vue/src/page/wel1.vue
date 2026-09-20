<template>
  <div class="dashboard-container" :style="dashboardStyle" v-if="isComponentReady">
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
           <button type="button" class="inner-icon-btn" @click="getTrees" aria-label="搜索">
            <i class="el-icon-search"></i>
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

      <div class="main-panel">
        <!-- 状态栏 -->
        <div class="status-bar" v-show="buildingItems.length!==0">
          <div v-for="status in statusList" :key="status.name" class="status-item">
            <span :style="{ backgroundColor: status.color }" class="status-icon"></span>
            <span class="status-text">{{ status.name }}：{{ status.count }}</span>
          </div>
        </div>

        <!-- 当前场景下的实例齿轮列表 -->
        <div class="center" v-show="buildingItems.length!==0">
          <div
            v-for="(turbine, index) in turbines"
            :key="turbine.turbineCode || turbine.code || turbine.name || index"
            class="turbine-item"
            @click="goToDetail(turbine)"
          >
            <img :src="getTurbineImage(getEffectiveTurbineStatus(turbine))" class="turbine-icon"/>
            <p :title="getTurbineDisplayName(turbine)">{{ getTurbineDisplayName(turbine) }}</p>
          </div>
        </div>

        <div
          v-if="houseItems.length"
          ref="ringContainer"
          class="ring-container"
          :class="{ paused: isRingHovered, dragging: isDraggingRing }"
          @mouseenter="handleRingMouseEnter"
          @mouseleave="handleRingMouseLeave"
          @mousedown.left.prevent="handleRingMouseDown"
        >
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
          <div v-if="buildingPageCount > 1" class="building-page-control" @mousedown.stop>
            <button type="button" @click.stop="prevBuildingPage">‹</button>
            <span>{{ buildingPage + 1 }} / {{ buildingPageCount }}</span>
            <button type="button" @click.stop="nextBuildingPage">›</button>
          </div>

          <!-- 每个 house 是一个场景入口 -->
          <div
            v-for="(house, bIndex) in houseItems"
            :key="house.id"
            class="ring-item"
            :class="{
              active: hoverBuildingIndex === bIndex,
              dimmed: hoverBuildingIndex !== null && hoverBuildingIndex !== bIndex,
              abnormal: house.status === 'abnormal',
              unaccessible: house.status === 'unaccessible',
              normal: house.status === 'normal'
            }"
            :style="getItemStyle(bIndex)"
            @mouseenter="hoverBuildingIndex = bIndex"
            @mouseleave="hoverBuildingIndex = null"
            @click="handleBuildingClick(house)"
          >
            <!-- building 图片 -->
            <img class="building-img" src="/img/wel/写字楼.png" />
            <div class="building-status-ring" :style="getBuildingStatusRingStyle(house)"></div>
            <div v-if="house.alarmCount" class="building-alarm-badge">{{ house.alarmCount }}</div>
            <div class="building-count-badge">{{ house.totalCount }}台</div>
            <div class="house-label" :title="house.fullLabel">{{ house.shortLabel }}</div>
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
          <el-table :data="turbines.length===0?[]:alamData" class="alarm-table" height="100%" :border="false" table-layout="fixed">
            <el-table-column prop="dcTime" label="时间" width="82" align="center" show-overflow-tooltip></el-table-column>
            <el-table-column prop="turbineName" label="实例对象名" min-width="96" align="center" show-overflow-tooltip></el-table-column>
            <el-table-column prop="components" label="零部件名称" width="82" align="center" show-overflow-tooltip></el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";
import logo from './myIndex/logo.vue'
import {getAccessStateOfTreeByFarm, getStateOfTreeByFarm, getOverviewOfFarms, getAlarmList, getProjectId, searchNode } from "@/api/sw/wel"
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
      autoRotateSpeed: 0.0025,
      maxVisibleBuildings: 8,
      buildingPage: 0,
      ringRafId: null,
      ellipseRafId: null,
      ringResizeObserver: null,
      statusChartRafId: null,
      statusChartResizeObserver: null,
      isRingHovered: false,
      isDraggingRing: false,
      hasDragged: false,
      dragStartX: 0,
      dragStartAngle: 0,
      dashboardHeight: '',
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
      selectedModels: [],
      turbineContextMap: {},
      sceneOverviewMap: {},
      alamData: [],
    }
  },
  computed: {
    statusList() {
      // 统计不同状态的风机数量
      let normalCount = 0, unaccessibleCount = 0, abnormalCount = 0;

      this.turbines.forEach(turbine => {
        const status = this.getEffectiveTurbineStatus(turbine)
        if (status === "normal") normalCount++;
        if (status === "abnormal") abnormalCount++;
        if (status === "unaccessible") unaccessibleCount++;
      });

      return [
        {name: "待接入系统", count: unaccessibleCount, color: "rgb(58, 151, 248)"},
        {name: "正常", count: normalCount, color: "#00cc99"},
        {name: "报警", count: abnormalCount, color: "rgb(251, 97, 112)"}
      ];
    },

    buildingItems() {
      return (this.treeData || []).map((scene, index) => {
        const fullLabel = scene.label || `场景${index + 1}`
        const overview = this.getSceneOverview(scene)
        return {
          id: scene.id || fullLabel,
          scene,
          fullLabel,
          shortLabel: this.formatHouseLabel(fullLabel),
          totalCount: overview.totalCount,
          alarmCount: overview.alarmCount,
          unaccessibleCount: overview.unaccessibleCount,
          normalCount: overview.normalCount,
          status: overview.status
        }
      })
    },

    alarmBuildingCount() {
      return this.buildingItems.filter(item => item.status === 'abnormal').length
    },

    buildingPageCount() {
      return Math.max(1, Math.ceil(this.buildingItems.length / this.maxVisibleBuildings))
    },

    houseItems() {
      const start = this.buildingPage * this.maxVisibleBuildings
      return this.buildingItems.slice(start, start + this.maxVisibleBuildings)
    },

    dashboardStyle() {
      return this.dashboardHeight ? { height: this.dashboardHeight } : {}
    }
  },

  mounted() {
    this.getTrees()
    this.isComponentReady = true
    this.startTimeUpdate()
    this.startRingAutoRotate()

    window.addEventListener('resize', this.scheduleUpdateEllipse)
    window.addEventListener('resize', this.scheduleStatusChartResize)
    window.addEventListener('resize', this.syncViewportFitHeight)
    this.$nextTick(() => {
      this.syncViewportFitHeight()
      this.updateEllipse()
      this.setupRingResizeObserver()
      this.setupStatusChartResizeObserver()
    })
  },

  watch: {
    houseItems() {
      this.$nextTick(() => {
        this.setupRingResizeObserver()
        this.scheduleUpdateEllipse()
      })
    },
    alamData() {
      this.$nextTick(() => {
        this.renderChart()
        this.setupStatusChartResizeObserver()
      })
    },
    buildingPageCount(count) {
      if (this.buildingPage >= count) {
        this.buildingPage = 0
      }
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
    this.stopRingAutoRotate()
    this.removeDragListeners()
    this.teardownRingResizeObserver()
    this.teardownStatusChartResizeObserver()
    if (this.ellipseRafId) {
      window.cancelAnimationFrame(this.ellipseRafId)
      this.ellipseRafId = null
    }
    if (this.statusChartRafId) {
      window.cancelAnimationFrame(this.statusChartRafId)
      this.statusChartRafId = null
    }
    window.removeEventListener('resize', this.scheduleUpdateEllipse)
    window.removeEventListener('resize', this.scheduleStatusChartResize)
    window.removeEventListener('resize', this.syncViewportFitHeight)
  },

  methods: {
      syncViewportFitHeight() {
        if (!this.$el || typeof window === 'undefined') return
        const rect = this.$el.getBoundingClientRect()
        const available = Math.floor(window.innerHeight - rect.top - 6)
        this.dashboardHeight = `${Math.max(560, available)}px`
      },

      scheduleUpdateEllipse() {
        if (typeof window === 'undefined') return
        if (this.ellipseRafId) return
        this.ellipseRafId = window.requestAnimationFrame(() => {
          this.ellipseRafId = null
          this.updateEllipse()
        })
      },

      setupRingResizeObserver() {
        this.teardownRingResizeObserver()
        if (typeof ResizeObserver === 'undefined') return
        const container = this.$refs.ringContainer || this.$el?.querySelector('.ring-container')
        if (!container) return
        this.ringResizeObserver = new ResizeObserver(() => {
          this.scheduleUpdateEllipse()
        })
        this.ringResizeObserver.observe(container)
      },

      teardownRingResizeObserver() {
        if (!this.ringResizeObserver) return
        this.ringResizeObserver.disconnect()
        this.ringResizeObserver = null
      },

      scheduleStatusChartResize() {
        if (typeof window === 'undefined') return
        if (this.statusChartRafId) return
        this.statusChartRafId = window.requestAnimationFrame(() => {
          this.statusChartRafId = null
          if (!this.$refs.statusChart) return
          const chart = echarts.getInstanceByDom(this.$refs.statusChart)
          if (chart) chart.resize()
        })
      },

      setupStatusChartResizeObserver() {
        this.teardownStatusChartResizeObserver()
        if (typeof ResizeObserver === 'undefined') return
        const container = this.$refs.statusChart
        if (!container) return
        this.statusChartResizeObserver = new ResizeObserver(() => {
          this.scheduleStatusChartResize()
        })
        this.statusChartResizeObserver.observe(container)
      },

      teardownStatusChartResizeObserver() {
        if (!this.statusChartResizeObserver) return
        this.statusChartResizeObserver.disconnect()
        this.statusChartResizeObserver = null
      },

      startRingAutoRotate() {
        if (this.ringRafId) return
        const rotate = () => {
          if (!this.isRingHovered && !this.isDraggingRing && this.houseItems.length > 1) {
            this.baseAngle += this.autoRotateSpeed
          }
          this.ringRafId = window.requestAnimationFrame(rotate)
        }
        this.ringRafId = window.requestAnimationFrame(rotate)
      },

      stopRingAutoRotate() {
        if (!this.ringRafId) return
        window.cancelAnimationFrame(this.ringRafId)
        this.ringRafId = null
      },

      removeDragListeners() {
        window.removeEventListener('mousemove', this.handleRingMouseMove)
        window.removeEventListener('mouseup', this.handleRingMouseUp)
      },

      handleRingMouseEnter() {
        this.isRingHovered = true
      },

      handleRingMouseLeave() {
        this.isRingHovered = false
      },

      handleRingMouseDown(event) {
        if (!this.houseItems.length) return
        this.isDraggingRing = true
        this.hasDragged = false
        this.dragStartX = event.clientX
        this.dragStartAngle = this.baseAngle
        this.hoverBuildingIndex = this.getFrontHouseIndex()
        window.addEventListener('mousemove', this.handleRingMouseMove)
        window.addEventListener('mouseup', this.handleRingMouseUp)
      },

      handleRingMouseMove(event) {
        if (!this.isDraggingRing) return
        const deltaX = event.clientX - this.dragStartX
        if (Math.abs(deltaX) > 3) {
          this.hasDragged = true
        }
        this.baseAngle = this.dragStartAngle + deltaX * 0.008
        this.hoverBuildingIndex = this.getFrontHouseIndex()
      },

      handleRingMouseUp() {
        if (!this.isDraggingRing) return
        this.isDraggingRing = false
        this.removeDragListeners()

        // 给 click 一个最短的抑制窗口，避免拖动后触发点击跳转
        if (this.hasDragged) {
          window.setTimeout(() => {
            this.hasDragged = false
          }, 0)
        }
      },

      getFrontHouseIndex() {
        const count = this.houseItems.length
        if (!count) return null

        let bestIndex = 0
        let maxDepth = -Infinity
        for (let i = 0; i < count; i++) {
          const { angle } = this.getEllipseOffset(i, count)
          const depth = Math.sin(angle)
          if (depth > maxDepth) {
            maxDepth = depth
            bestIndex = i
          }
        }
        return bestIndex
      },

      formatHouseLabel(label) {
        const text = String(label || '')
        if (text.length <= 10) return text
        return `${text.slice(0, 10)}...`
      },

      getTurbineDisplayName(turbine = {}) {
        return turbine.name || turbine.turbineName || turbine.label || turbine.turbineCode || turbine.code || '未命名实例'
      },

      getSceneOverview(scene = {}) {
        const sceneName = scene.label || ''
        const savedOverview = this.sceneOverviewMap[sceneName] || {}
        const treeTotal = Array.isArray(scene.children) ? scene.children.length : 0
        const totalCount = savedOverview.totalCount !== undefined ? savedOverview.totalCount : treeTotal
        const alarmCount = savedOverview.alarmCount || 0
        const unaccessibleCount = savedOverview.unaccessibleCount || 0
        return {
          totalCount,
          normalCount: savedOverview.normalCount || 0,
          unaccessibleCount,
          alarmCount,
          status: alarmCount > 0 ? 'abnormal' : (unaccessibleCount > 0 ? 'unaccessible' : 'normal')
        }
      },

      countTurbineStatuses(turbines = [], alarmRows = [], farmName = this.farmName) {
        return turbines.reduce((counts, turbine) => {
          const status = this.getEffectiveTurbineStatus({...turbine, farmName}, alarmRows)
          counts.totalCount += 1
          if (status === 'abnormal') counts.alarmCount += 1
          else if (status === 'unaccessible') counts.unaccessibleCount += 1
          else counts.normalCount += 1
          return counts
        }, {
          totalCount: 0,
          normalCount: 0,
          unaccessibleCount: 0,
          alarmCount: 0
        })
      },

      getBuildingStatusRingStyle(house) {
        const total = Math.max(1, house.totalCount || 0)
        const alarmCount = Math.min(house.alarmCount || 0, total)
        const unaccessCount = Math.min(house.unaccessibleCount || 0, Math.max(0, total - alarmCount))
        const alarmDeg = Math.round(alarmCount / total * 360)
        const unaccessDeg = Math.round(unaccessCount / total * 360)
        const alarmEnd = alarmDeg
        const unaccessEnd = alarmDeg + unaccessDeg
        return {
          background: `conic-gradient(
            #fd626e 0deg ${alarmEnd}deg,
            #3a97f8 ${alarmEnd}deg ${unaccessEnd}deg,
            #00cc99 ${unaccessEnd}deg 360deg
          )`
        }
      },

      prevBuildingPage() {
        this.buildingPage = (this.buildingPage - 1 + this.buildingPageCount) % this.buildingPageCount
        this.hoverBuildingIndex = null
      },

      nextBuildingPage() {
        this.buildingPage = (this.buildingPage + 1) % this.buildingPageCount
        this.hoverBuildingIndex = null
      },

      handleBuildingClick(house) {
        if (this.hasDragged) return
        const scene = house && house.scene
        if (!scene) {
          this.$message.warning('该场景暂无可加载的数据')
          return
        }
        this.selectScene(scene)
      },

      updateEllipse() {
        const container = this.$refs.ringContainer || this.$el.querySelector('.ring-container')
        if (!container) return

        const { width, height } = container.getBoundingClientRect()

        const xMargin = Math.max(96, Math.min(136, width * 0.12))
        const yMargin = Math.max(58, Math.min(88, height * 0.16))

        this.svgCenter = {
          x: width / 2,
          y: height * 0.58
        }

        this.svgRadius = {
          x: Math.max(width / 2 - xMargin, 220),
          y: Math.max(height / 2 - yMargin, 128)
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

      angle += this.baseAngle

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

    getItemStyle(index) {
      const count = this.houseItems.length
      const { x, y, angle } = this.getEllipseOffset(index, count)

      const depth = Math.sin(angle)

      return {
        transform: `
          translate(-50%, -100%)
          translate(${x}px, ${y}px)
          scale(1)
        `,
        zIndex: Math.round(2000 + depth * 500),
        opacity: 1
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
        this.turbineContextMap = {};
        this.sceneOverviewMap = {};
        if (this.searchText.trim() === ''){
          res = await getOverviewOfFarms({ userId: userId });
        }
        else res = await this.searchByStr(this.searchText);
        const rawTree = this.addIndexAsId(res.data.data);
        this.treeData = this.flattenFarmTree(rawTree);

        if (this.treeData.length > 0) {
          const validFarm = this.treeData.find(farm => farm.children?.length > 0);
          if (validFarm) {
            this.farmName = validFarm.label;
            this.buildings = validFarm.children || []
            this.selectedModels = this.collectModelsByFarm(validFarm)
            this.turbineModel = this.selectedModels[0] || ''
            await this.$nextTick(() => {
              if (this.$refs.treeRef) {
                this.$refs.treeRef.setCurrentKey(validFarm.id)
              }
            })
          }
        }
        await this.refreshSceneOverviews()
        await this.getTurbines();
      } catch (error) {
        console.error("加载数据失败", error);
      }
    },

    addIndexAsId(tree, parentIndex = '') {
      return tree.map((node, idx) => {
        const newId = `${parentIndex}-${idx}` // 生成唯一 ID
        const newNode = { ...node, sourceId: node.id, id: newId }
        if (newNode.children && newNode.children.length > 0) {
          newNode.children = this.addIndexAsId(newNode.children, newId)
        }
        return newNode
      })
    },

    //获取节点
    flattenFarmTree(tree) {
      return (tree || []).map((farm) => {
        const instanceChildren = []
        ;(farm.children || []).forEach((modelNode) => {
          const modelName = modelNode.label
          const modelId = modelNode.projectId || modelNode.proId || modelNode.productModelId || modelNode.id || ''
          const hasInstanceChildren = Array.isArray(modelNode.children) && modelNode.children.length
          const children = hasInstanceChildren
            ? modelNode.children
            : (this.isInstanceNode(modelNode) ? [modelNode] : [])
          children.forEach((instanceNode) => {
            const turbineModel = instanceNode.turbineModel || instanceNode.productModel || modelName
            const enrichedNode = {
              ...instanceNode,
              farmName: farm.label,
              turbineModel,
              turbineModelId: instanceNode.turbineModelId || instanceNode.productModelId || instanceNode.projectId || modelId
            }
            this.rememberTurbineContext(enrichedNode, farm.label, turbineModel)
            instanceChildren.push(enrichedNode)
          })
        })
        return {
          ...farm,
          children: instanceChildren
        }
      })
    },

    isInstanceNode(node = {}) {
      return Boolean(
        node.turbineCode ||
        node.code ||
        node.name ||
        node.turbineName ||
        node.status ||
        node.producer
      )
    },

    collectModelsByFarm(farmNode) {
      return [...new Set(
        (farmNode?.children || [])
          .map(item => item.turbineModel || item.productModel || item.label)
          .filter(Boolean)
      )]
    },

    getTurbineIdentityKeys(turbine = {}) {
      return [
        turbine.turbineCode,
        turbine.code,
        turbine.name,
        turbine.turbineName,
        turbine.label
      ].map(value => String(value || '').trim()).filter(Boolean)
    },

    buildTurbineContextKey(farmName, identity) {
      return `${String(farmName || '').trim()}@@${String(identity || '').trim()}`
    },

    rememberTurbineContext(turbine = {}, farmName, turbineModel) {
      if (!farmName || !turbineModel) return
      const context = {
        farmName,
        turbineModel,
        turbineModelId: turbine.turbineModelId || turbine.productModelId || turbine.projectId || '',
        turbineCode: turbine.turbineCode || turbine.code || '',
        turbineName: turbine.name || turbine.turbineName || turbine.label || ''
      }
      this.getTurbineIdentityKeys(turbine).forEach((identity) => {
        const contextKey = this.buildTurbineContextKey(farmName, identity)
        const oldContext = this.turbineContextMap[contextKey]
        if (
          oldContext &&
          (oldContext.turbineModel !== context.turbineModel || oldContext.turbineCode !== context.turbineCode)
        ) {
          this.turbineContextMap[contextKey] = { ...oldContext, ambiguous: true }
          return
        }
        this.turbineContextMap[contextKey] = context
      })
    },

    getTurbineContext(turbine = {}) {
      const farmName = turbine.farmName || this.farmName
      const keys = this.getTurbineIdentityKeys(turbine)
      for (const identity of keys) {
        const context = this.turbineContextMap[this.buildTurbineContextKey(farmName, identity)]
        if (context?.ambiguous) return null
        if (context) return context
      }
      return null
    },

    getSceneByFarmName(farmName = this.farmName) {
      return (this.treeData || []).find(scene => scene.label === farmName) || null
    },

    buildTurbineStateLookup(turbines = [], farmName = this.farmName) {
      const lookup = {}
      turbines.forEach((turbine) => {
        this.getTurbineIdentityKeys(turbine).forEach((identity) => {
          lookup[this.buildTurbineContextKey(turbine.farmName || farmName, identity)] = turbine
        })
      })
      return lookup
    },

    mergeTurbineStateList(baseTurbines = [], stateTurbines = [], farmName = this.farmName) {
      const stateLookup = this.buildTurbineStateLookup(stateTurbines, farmName)
      const source = Array.isArray(baseTurbines) && baseTurbines.length ? baseTurbines : stateTurbines
      return source.map((turbine) => {
        const matchedState = this.getTurbineIdentityKeys(turbine)
          .map(identity => stateLookup[this.buildTurbineContextKey(farmName, identity)])
          .find(Boolean)
        return {
          ...(matchedState || {}),
          ...turbine,
          farmName,
          status: matchedState?.status ?? turbine.status
        }
      })
    },

    async getAccessStateByFarm(farmName) {
      try {
        return await getAccessStateOfTreeByFarm({ farmName }, {silentError: true})
      } catch (error) {
        console.error(`获取 ${farmName} 快速接入状态失败，回退旧接口`, error)
        return getStateOfTreeByFarm({ farmName }, {silentError: true})
      }
    },

    getFarmNodeByTreeNode(node, treeNode) {
      let current = treeNode
      while (current && current.level > 1) {
        current = current.parent
      }
      if (current && current.data) {
        return current.data
      }
      return node || {}
    },

    handleNodeClick(node, treeNode, component) {
      this.forceRenderKey = Date.now()
      const farmNode = this.getFarmNodeByTreeNode(node, treeNode)
      this.selectScene(farmNode)
    },

    selectScene(scene = {}) {
      if (!scene.label) return
      this.forceRenderKey = Date.now()
      this.farmName = scene.label || ''
      this.buildings = scene.children || []
      this.selectedModels = this.collectModelsByFarm(scene)
      this.turbineModel = this.selectedModels[0] || ''

      this.$nextTick(() => {
        if (this.$refs.treeRef && scene.id) {
          this.$refs.treeRef.setCurrentKey(scene.id)
        }
        this.getTurbines()
      })
    },

    async refreshSceneOverviews() {
      const scenes = this.treeData || []
      if (!scenes.length) return

      const overviewEntries = await Promise.all(scenes.map(async (scene) => {
        const farmName = scene.label
        if (!farmName) return null

        try {
          const [stateRes, alarmRows] = await Promise.all([
            this.getAccessStateByFarm(farmName),
            this.fetchAlarmRows(farmName, this.collectModelsByFarm(scene))
          ])
          const stateTurbines = Array.isArray(stateRes?.data?.data) ? stateRes.data.data : []
          const turbines = this.mergeTurbineStateList(scene.children || [], stateTurbines, farmName)
          const counts = this.countTurbineStatuses(turbines, alarmRows, farmName)
          const alarmCount = Array.isArray(alarmRows) && alarmRows.length
            ? this.countAlarmTurbines(turbines, alarmRows)
            : counts.alarmCount

          return [farmName, {
            ...counts,
            totalCount: counts.totalCount || (Array.isArray(scene.children) ? scene.children.length : 0),
            alarmCount: Math.max(counts.alarmCount, alarmCount)
          }]
        } catch (error) {
          console.error(`获取场景 ${farmName} 概览失败`, error)
          return null
        }
      }))

      const nextOverview = { ...this.sceneOverviewMap }
      overviewEntries.filter(Boolean).forEach(([farmName, overview]) => {
        nextOverview[farmName] = overview
      })
      this.sceneOverviewMap = nextOverview
    },

    async getTurbines() {
      if (!this.farmName) {
        console.warn("farmName 为空，不执行 API 请求")
        return
      }
      try {
        const res = await this.getAccessStateByFarm(this.farmName)
        const stateList = Array.isArray(res?.data?.data) ? res.data.data : []
        const scene = this.getSceneByFarmName(this.farmName)
        const list = this.mergeTurbineStateList(scene?.children || [], stateList, this.farmName)
        this.turbines = list.map(item => {
          const farmName = item.farmName || this.farmName
          const context = this.getTurbineContext({ ...item, farmName })
          const turbineModel = item.turbineModel || item.productModel || context?.turbineModel || this.turbineModel
          const enrichedItem = {
            ...item,
            farmName,
            turbineModel,
            turbineModelId: item.turbineModelId || item.productModelId || item.projectId || context?.turbineModelId || ''
          }
          this.rememberTurbineContext(enrichedItem, farmName, turbineModel)
          return enrichedItem
        })
        this.alamData = []

        if (!this.selectedModels.length) {
          this.selectedModels = [...new Set(this.turbines.map(item => item.turbineModel).filter(Boolean))]
          this.turbineModel = this.selectedModels[0] || ''
        }

        if (this.turbines.length === 0) {
          this.alamData = []
        } else {
          await this.getAlarms(this.selectedModels)
        }

        await this.$nextTick(() => {
          this.renderChart()
        })
      } catch (error) {
        console.error("获取 `turbines` 失败", error)
      }
    },

    normalizeStatus(rawStatus) {
      const status = String(rawStatus || '').toLowerCase().trim()
      if (["normal", "online", "running", "ok", "正常"].includes(status)) return "normal"
      if (["abnormal", "alarm", "fault", "warning", "报警", "异常"].includes(status)) return "abnormal"
      if (["unaccessible", "offline", "disconnected", "not_access", "未接入", "离线"].includes(status)) return "unaccessible"
      return "unaccessible"
    },

    getTurbineNameCandidates(turbine = {}) {
      return [
        turbine.name,
        turbine.turbineName,
        turbine.label,
        turbine.turbineCode,
        turbine.code
      ].map(value => String(value || '').trim()).filter(Boolean)
    },

    isTurbineMatchedAlarm(turbine = {}, alarmRows = []) {
      if (!Array.isArray(alarmRows) || !alarmRows.length) return false
      const names = this.getTurbineNameCandidates(turbine)
      if (!names.length) return false
      return alarmRows.some(item => {
        const alarmName = String(item?.turbineName || '').trim()
        return alarmName && names.some(name => alarmName === name || alarmName.includes(name) || name.includes(alarmName))
      })
    },

    isTurbineInAlarm(turbine = {}) {
      return this.isTurbineMatchedAlarm(turbine, this.alamData)
    },

    countAlarmTurbines(turbines = [], alarmRows = []) {
      if (Array.isArray(alarmRows) && alarmRows.length) {
        const matchedCount = turbines.filter(turbine => this.isTurbineMatchedAlarm(turbine, alarmRows)).length
        const alarmNameCount = new Set(
          alarmRows.map(item => String(item?.turbineName || '').trim()).filter(Boolean)
        ).size
        return Math.max(matchedCount, alarmNameCount)
      }
      return turbines.filter(turbine => this.normalizeStatus(turbine.status) === 'abnormal').length
    },

    getEffectiveTurbineStatus(turbine = {}, alarmRows = this.alamData) {
      if (this.isTurbineMatchedAlarm(turbine, alarmRows)) return 'abnormal'
      const normalized = this.normalizeStatus(turbine.status)
      if (normalized === 'abnormal') return 'abnormal'
      if (normalized === 'unaccessible') return 'unaccessible'
      return 'normal'
    },

    //根据状态对应相应图标
    getTurbineImage(status) {
      const normalized = this.normalizeStatus(status)
      if (normalized === "normal") {
        return "img/wel/齿轮-正常.png"; // 正常
      } else if (normalized === "abnormal") {
        return "img/wel/齿轮-报警.png"; // 异常
      } else if (normalized === "unaccessible") {
        return "img/wel/齿轮-未接入.png"; // 未接入
      }
      return "img/wel/齿轮-未接入.png"
    },

    //获取饼状图数据
    renderChart() {
      if (!this.$refs.statusChart) {
        console.warn("ECharts 容器未找到");
        return;
      }
      const chart = echarts.getInstanceByDom(this.$refs.statusChart) || echarts.init(this.$refs.statusChart);

      //获取统计图的数据
      // 分别计算三种状态下的数据
      let normalCount = 0
      let abnormalCount = 0
      let unaccessCount = 0
      let chartData = []

      this.turbines.forEach(item => {
        switch (this.getEffectiveTurbineStatus(item)) {
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
              fontSize: 15,
              color: "#ffffff",
            },
          },
          {
            text: `{count|${this.turbines.length}}\n设备总数`, // 文字内容
            left: "50%",
            top: "42%", // 调整垂直位置
            textAlign: "center",
            textStyle: {
              fontSize: 12,
              lineHeight: 14,
              fontWeight: "bold",
              color: "#fff",
              rich: {
                count: {
                  fontSize: 18, // **单独设置风机数量大小**
                  lineHeight: 20,
                  fontWeight: "bold",
                  color: "#FFA500",
                  padding: [0, 0, 2, 0]
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
          itemWidth: 10,
          itemHeight: 8,
          itemGap: 8,
          textStyle: { color: "#fff", fontSize: 11 },
        },
        series: [
          {
            type: "pie",
            radius: ["44%", "66%"],
            center: ["50%", "51%"],
            avoidLabelOverlap: true,
            label: {
              show: true,
              color: "#fff",
              fontSize: 10,
              width: 48,
              overflow: 'truncate'
            },
            labelLine: {
              show: true,
              length: 6,
              length2: 4,
              smooth: 0.2
            },
            data: chartData
          },
        ],
      })
      this.scheduleStatusChartResize()

    },
    async fetchAlarmRows(farmName, modelList = []) {
      const models = Array.isArray(modelList) && modelList.length
        ? modelList
        : []
      if (!farmName || models.length === 0) {
        return []
      }

      const alarmGroups = await Promise.all(models.map(async (model) => {
        const projectRes = await getProjectId({project: farmName, productModel: model})
        const projectId = projectRes?.data?.data
        if (!projectId) return []
        const alarmRes = await getAlarmList({proId: projectId})
        const alarmList = Array.isArray(alarmRes?.data?.data) ? alarmRes.data.data : []
        return alarmList.map(item => ({
          dcTime: item.dcTime,
          turbineName: item.turbineName,
          components: item.nodeName
        }))
      }))

      const uniqueMap = new Map()
      alarmGroups.flat().forEach((item) => {
        const key = `${item.dcTime || ''}_${item.turbineName || ''}_${item.components || ''}`
        if (!uniqueMap.has(key)) {
          uniqueMap.set(key, item)
        }
      })
      return Array.from(uniqueMap.values())
    },

    //获取报警信息
    async getAlarms(modelList = []) {
      const models = Array.isArray(modelList) && modelList.length
        ? modelList
        : (this.turbineModel ? [this.turbineModel] : [])
      if (!this.farmName || models.length === 0) {
        this.alamData = []
        return
      }

      try {
        this.alamData = await this.fetchAlarmRows(this.farmName, models)
      } catch (error) {
        console.error("获取报警信息失败", error)
        this.alamData = []
      }
    },

    // 跳转到风机详情页面
    goToDetail(turbine) {
      const context = this.getTurbineContext(turbine) || {}
      const farmName = context.farmName || turbine.farmName || this.farmName
      const turbineModel = context.turbineModel || turbine.turbineModel || turbine.productModel || this.turbineModel
      const turbineCode = turbine.turbineCode || turbine.code || context.turbineCode
      const turbineName = turbine.name || turbine.turbineName || turbine.label || context.turbineName

      if (!farmName || !turbineModel || !turbineCode) {
        this.$message.warning("该电梯缺少场景、型号或编码信息，无法精准进入详情页")
        return
      }

      this.$router.push({
        path: '/scene/sceneInstantiation/details',
        query: {
          turbineName,
          turbineCode,
          farmName,
          turbineModel,
          projectId: context.turbineModelId || turbine.turbineModelId || turbine.productModelId || turbine.projectId || '',
          status: turbine.status,
          producer: turbine.producer
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

/* 其他 building 变暗 */
.ring-item.dimmed {
  opacity: 0.35;
  transition: opacity 0.25s ease;
}

.building-img {
  position: relative;
  z-index: 2;
  opacity: 0.95;
  filter: saturate(0.88) brightness(0.9) contrast(1.05) drop-shadow(0 12px 24px rgba(2, 24, 46, 0.62));
  border-radius: 8px;
  transition: transform 0.25s ease, filter 0.25s ease, opacity 0.25s ease;
}

.house-label {
  position: absolute;
  left: 50%;
  top: calc(100% + 6px);
  transform: translateX(-50%);
  max-width: 140px;
  padding: 3px 10px;
  border-radius: 999px;
  border: 1px solid rgba(80, 208, 245, 0.42);
  background: linear-gradient(180deg, rgba(7, 49, 74, 0.78) 0%, rgba(3, 24, 42, 0.88) 100%);
  color: #c9f5ff;
  font-size: 12px;
  line-height: 1.2;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-shadow: 0 4px 14px rgba(5, 36, 54, 0.45);
  pointer-events: none;
}

.building-page-control {
  position: absolute;
  left: 50%;
  top: 8px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
  transform: translateX(-50%);
  color: #c9f5ff;
  font-size: 13px;

  button {
    width: 26px;
    height: 24px;
    border: 1px solid rgba(80, 208, 245, 0.45);
    border-radius: 4px;
    background: rgba(4, 33, 56, 0.72);
    color: #dffaff;
    cursor: pointer;
  }

  button:hover {
    border-color: rgba(110, 236, 255, 0.8);
    color: #fff;
  }
}

.building-status-ring {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 1;
  width: 96px;
  height: 96px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  opacity: 0.78;
  filter: blur(0.1px) drop-shadow(0 0 10px rgba(0, 210, 255, 0.18));
  pointer-events: none;
}

.building-status-ring::after {
  content: "";
  position: absolute;
  inset: 5px;
  border-radius: 50%;
  background: rgba(3, 21, 35, 0.86);
}

.building-alarm-badge,
.building-count-badge {
  position: absolute;
  z-index: 4;
  min-width: 24px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.32);
  pointer-events: none;
}

.building-alarm-badge {
  right: -18px;
  top: -12px;
  background: #fd626e;
  color: #fff;
  font-weight: 700;
}

.building-count-badge {
  left: 50%;
  bottom: -2px;
  transform: translateX(-50%);
  background: rgba(6, 28, 49, 0.92);
  border: 1px solid rgba(117, 198, 255, 0.48);
  color: #dffaff;
}

.ring-item.abnormal .building-img {
  filter: saturate(0.92) brightness(0.9) contrast(1.05) drop-shadow(0 0 18px rgba(253, 98, 110, 0.46));
}

.ring-item.abnormal .building-status-ring {
  animation: buildingAlarmPulse 1.8s ease-in-out infinite;
}

.ring-item.unaccessible .building-img {
  filter: saturate(0.82) brightness(0.86) contrast(1.02) drop-shadow(0 0 14px rgba(58, 151, 248, 0.32));
}

@keyframes buildingAlarmPulse {
  0%, 100% {
    opacity: 0.64;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.96;
    transform: translate(-50%, -50%) scale(1.08);
  }
}

.inner-icon-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #8fc7ff;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}
.search-wrapper {
  display: flex;
  justify-content: flex-end; /* 搜索框右对齐 */
  position: relative; //1
}
.inner-icon-btn {//1
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #8fc7ff;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.inner-icon-btn:hover {
  color: #ffffff;
  background: rgba(78, 168, 255, 0.16);
}
.search-input {
  width: 200px; /* 设置固定宽度 */
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.search_input {
  width: 100%;
  height: 34px;
  padding: 0 36px 0 12px;
  border: 1px solid rgba(104, 190, 255, 0.26);
  border-radius: 7px;
  color: #e8f7ff;
  background: rgba(2, 16, 30, 0.56);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.search_input::placeholder {
  color: rgba(215, 237, 255, 0.44);
}

.search_input:focus {
  border-color: rgba(78, 168, 255, 0.72);
  background: rgba(3, 21, 39, 0.76);
  box-shadow: 0 0 0 2px rgba(78, 168, 255, 0.13);
}
.ring-container {
  position: relative;
  width: 100%;
  flex: 1 1 430px;
  min-height: 360px;
  height: auto;
  perspective: 1200px;
  transform-style: preserve-3d;
  margin-top: 18px;
  overflow: visible !important;
  user-select: none;
  cursor: grab;
}

.ring-container.dragging {
  cursor: grabbing;
}

.connection-svg {
  position: absolute;
  inset: 0;
  z-index: 1;
}

.ring-item {
  position: absolute;
  top: 58%;
  left: 50%;
  transform-style: preserve-3d;
  transform-origin: 50% 100%;
  z-index: 10;
  overflow: visible;
  cursor: pointer;
}

.ring-item::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 92%;
  transform: translate(-50%, -50%);
  width: 84px;
  height: 84px;
  background: radial-gradient(circle, rgba(28, 98, 124, 0.28) 0%, rgba(28, 98, 124, 0.07) 60%, rgba(28, 98, 124, 0) 100%);
  border-radius: 50%;
  z-index: 1;
  pointer-events: none;
}

.ring-item:hover .building-img {
  filter: saturate(0.9) brightness(0.94) contrast(1.04) drop-shadow(0 12px 24px rgba(0, 214, 255, 0.28));
  transform: translateY(-1px);
}

.ring-item:hover .house-label,
.ring-item.active .house-label {
  border-color: rgba(110, 236, 255, 0.76);
  color: #e7fdff;
  background: linear-gradient(180deg, rgba(12, 64, 92, 0.82) 0%, rgba(4, 33, 56, 0.9) 100%);
}

.ring-item img {
  width: 82px;
  height: 82px;
  object-fit: contain;
}

.time-display{
  position: static;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 0;
  text-align: center;
  padding: 0;
  color: #9dd8ff;
}
.time-date{
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  margin-top: 0;
  padding: 0 10px;
  color: #bce7ff;
  font-size: 13px;
  letter-spacing: 0;
  text-shadow: 0 0 12px rgba(78, 168, 255, 0.32);
}
.big-title {
  margin-top: 0;
  text-align: center;
  color: #dff7ff;
}

.title {
  position: relative;
  margin: 0 0 10px;
  text-align: left;
  padding: 0 0 0 12px;
  color: #dff7ff;
  font-size: 16px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: 0;
  text-indent: 0;
}

.title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 1px;
  bottom: 1px;
  width: 3px;
  border-radius: 999px;
  background: #4ea8ff;
  box-shadow: 0 0 12px rgba(78, 168, 255, 0.72);
}

.dashboard-container {
  --panel-bg: linear-gradient(180deg, rgba(8, 34, 58, 0.82), rgba(3, 16, 30, 0.92));
  --panel-border: rgba(104, 190, 255, 0.24);
  --panel-border-strong: rgba(104, 190, 255, 0.42);
  --screen-text: #e8f7ff;
  --screen-muted: rgba(215, 237, 255, 0.64);
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  box-sizing: border-box;
  background:
    linear-gradient(180deg, rgba(2, 12, 24, 0.3), rgba(2, 12, 24, 0.72)),
    linear-gradient(rgba(102, 190, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(102, 190, 255, 0.035) 1px, transparent 1px),
    url("/img/wel/eva.png") no-repeat center center fixed;
  background-size: auto, 32px 32px, 32px 32px, cover;
  overflow-y: hidden;
  overflow-x: hidden;
  color: var(--screen-text);
  font-family: Arial, "Microsoft YaHei", sans-serif;
}

.dashboard-container::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(90deg, rgba(78, 168, 255, 0.08), transparent 16%, transparent 84%, rgba(78, 168, 255, 0.08)),
    linear-gradient(180deg, rgba(3, 17, 32, 0.18), transparent 36%, rgba(3, 17, 32, 0.36));
  z-index: 0;
}

.header {
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  min-height: 106px;
  padding: 16px 24px 12px;
  font-size: 18px;
  position: relative;
}

.header h1 {
  margin: 0;
  color: #eefbff;
  font-size: clamp(24px, 2.45vw, 34px);
  line-height: 1.26;
  font-weight: 800;
  letter-spacing: 0;
  text-shadow: 0 0 20px rgba(78, 168, 255, 0.42);
}

/* LOGO 左对齐 */
.header-left {
  position: absolute;
  left: 20px; /* 靠左侧一定距离 */
  display: flex;
  align-items: center;
}

.content {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  box-sizing: border-box;
  padding: 8px 18px 18px;
  gap: 14px;
  min-height: 0;
  align-items: stretch;
  overflow-y: hidden;
  overflow-x: hidden;
}

/* 设备树整体容器 */
.sidebar-left {
  display: flex;
  flex-direction: column;
  flex: 0 0 240px;
  min-width: 200px; /* 设置最小宽度 */
  max-width: 270px; /* 设置最大宽度 */
  min-height: 0; /* 允许内部滚动 */
  background: var(--panel-bg);
  border: 1px solid var(--panel-border);
  box-shadow: 0 18px 46px rgba(0, 8, 20, 0.34), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  padding: 15px;
  border-radius: 8px;
  overflow-y: auto;
}


/* 设备树标题 */
.sidebar-left h3 {
  text-align: left;
  font-size: 16px;
  color: #dff7ff;
  margin-bottom: 12px;
}

/* 设备树内容 */
.tree {
  flex: 1; /* 占据剩余空间 */
  overflow-y: auto; /* 当内容超出时滚动 */
  background: transparent;
  color: #e8f7ff;
  font-size: 14px;
  min-height: 0; /* 确保flex子元素可以正确缩小 */
  margin-top: 12px;
  padding-right: 4px;
}

/* Vue 2 方式 */
::v-deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: rgba(78, 168, 255, 0.2) !important;
  color: #dff7ff !important;
  box-shadow: inset 3px 0 0 rgba(78, 168, 255, 0.85);
}

/* 设备树选中高亮 */
::v-deep(.el-tree-node__content:hover) {
  background-color: rgba(78, 168, 255, 0.13) !important;
}

/* 移除 el-tree 背景色 */
::v-deep(.el-tree) {
  background: transparent !important;
}

/* 确保所有 tree 节点背景都是透明的 */
::v-deep(.el-tree-node__content) {
  background: transparent !important;
  min-height: 32px;
  border-radius: 6px;
  margin: 2px 0;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin: 0 auto 10px;
  width: min(720px, 92%);
  border: 1px solid rgba(104, 190, 255, 0.22);
  border-radius: 8px;
  background:
    linear-gradient(90deg, rgba(9, 42, 74, 0.34), rgba(9, 42, 74, 0.72), rgba(9, 42, 74, 0.34)),
    url('/img/wel/status-bg.png') center / 500px 100% no-repeat;
  padding: 9px 14px;
  box-shadow: 0 12px 30px rgba(0, 10, 24, 0.22);
}

.status-item {
  display: flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  color: #dff7ff;
  background: rgba(255, 255, 255, 0.045);
  border: 1px solid rgba(104, 190, 255, 0.12);
  font-size: 14px;
}

.status-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 7px;
  z-index: 3000;
  opacity: 1;
  box-shadow: 0 0 12px currentColor;
}

.main-panel {
  display: flex;
  flex-direction: column;
  flex: 1 1 0;
  min-width: 0;
  padding: 0 4px;
}


.center {
  display: grid;
  grid-template-columns: repeat(auto-fill, 96px); /* 每行最多 8 个 */
  grid-auto-rows: minmax(74px, auto); /* 设置行高 */
  justify-items: center;
  justify-content: center;
  row-gap: 12px; /* 调整行间距 */
  column-gap: 12px;
  width: 100%; /* 让 grid 适应父容器 */
  max-width: 872px; /* 8 * 96 + 7 * 12 + 20 padding */
  height: 154px;
  max-height: 154px;
  margin: 0 auto;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px;
  border: 1px solid rgba(104, 190, 255, 0.18);
  border-radius: 8px;
  background: rgba(3, 18, 34, 0.44);
  box-sizing: border-box;
}

.turbine-item {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  width: 96px;
  min-height: 68px;
  text-align: center;
  padding: 6px 7px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer; /* 添加鼠标指针效果 */
  background: rgba(255, 255, 255, 0.025);
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.turbine-item:hover {
  transform: translateY(-2px);
  border-color: rgba(78, 168, 255, 0.46);
  background: rgba(24, 89, 174, 0.28);
  box-shadow: 0 10px 22px rgba(0, 12, 28, 0.28);
}


.turbine-item p {
  margin: 0; /* 移除默认的上下边距 */
  width: 100%;
  color: rgba(239, 252, 255, 0.94);
  font-size: 12px;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.turbine-icon {
  vertical-align: middle; /* 或者使用 top */
  width: 38px;
  height: 38px;
  object-fit: contain;
  display: block;
  margin: auto;
  filter: drop-shadow(0 8px 16px rgba(0, 16, 32, 0.35));
}

.right {
  flex: 0 1 390px;
  min-width: 340px; /* 设置最小宽度 */
  max-width: 420px; /* 设置最大宽度 */
  min-height: 0; /* 允许内部滚动 */
  max-height: 100%;
  box-sizing: border-box;
  padding: 0;
  background: transparent;
  border-radius: 8px;
  overflow-y: hidden;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 10px;

  .title {
    flex-shrink: 0;
    padding: 0 0 0 12px;
  }

  .status-info,
  .history-data {
    display: flex;
    flex-direction: column;
    padding: 14px;
    border: 1px solid var(--panel-border);
    border-radius: 8px;
    background: var(--panel-bg);
    box-shadow: 0 18px 46px rgba(0, 8, 20, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.05);
    overflow: hidden;
  }

  .status-info {
    flex: 0 0 clamp(230px, 34vh, 320px);
    min-height: 0;
  }

  .chart {
    width: 100%;  /* 让 ECharts 占满父容器 */
    height: 100%;
    min-height: 0;
    min-width: 0;
    display: flex;
    justify-content: center; /* 确保子元素居中 */
    align-items: center;
  }
}

.history-data {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
  min-height: 0; /* 允许内部滚动 */
  overflow: hidden;

  .title {
    padding: 0 0 0 12px;
  }
}

.alarm-table {
  flex: 1;
  min-height: 0;
  width: 100%;
}

::v-deep(.el-table) {
  background: transparent !important;
  color: #e8f7ff;

}

::v-deep(.el-table__header) {
  background: transparent !important;
  color: #dff7ff;
}

::v-deep(.el-table__body) {
  background: transparent !important;
}

::v-deep(.alarm-table .el-table__inner-wrapper) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

::v-deep(.alarm-table .el-table__header-wrapper) {
  flex-shrink: 0;
}

::v-deep(.alarm-table .el-table__body-wrapper) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

::v-deep(.alarm-table .el-table__cell) {
  padding: 5px 0 !important;
}

::v-deep(.alarm-table .cell) {
  padding: 0 5px !important;
  font-size: 12px;
  line-height: 1.2;
  word-break: break-all;
}

::v-deep(.alarm-table th .cell) {
  font-size: 13px;
  line-height: 1.25;
  font-weight: 700;
  color: #dff7ff;
}

::v-deep(.el-table th),
::v-deep(.el-table tr),
::v-deep(.el-table td) {
  border: none !important; /* 彻底移除所有边框 */
  background: transparent !important; /* 去除表格单元格的背景色 */
  border-bottom: 1px solid rgba(104, 190, 255, 0.12) !important;
}


/*去掉 Element UI 默认的表格底部横线*/
::v-deep(.el-table__inner-wrapper::before) {
  display: none !important;
}
::v-deep(.el-tree-node__label) {
  color: #e8f7ff !important;
}

</style>
