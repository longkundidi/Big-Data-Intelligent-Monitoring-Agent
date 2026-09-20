<template>
  <div class="turbine-detail-container">
    <!-- 顶部信息栏 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click.native="goBack" class="clickable">场景设备数据检测</el-breadcrumb-item>
        <el-breadcrumb-item>{{ turbineName }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="header-info">
        <div class="info-item-select">
          <span class="label">风场：</span>
          <el-select v-model="farmName" placeholder="请选择风场" @change="handleFarmChange" size="small">
            <el-option
              v-for="farm in farmList"
              :key="farm.value"
              :label="farm.label"
              :value="farm.value">
            </el-option>
          </el-select>
        </div>
        <div class="info-item-select">
          <span class="label">机型：</span>
          <el-select v-model="turbineModel" placeholder="请选择机型" @change="handleModelChange" size="small">
            <el-option
              v-for="model in turbineModelList"
              :key="model.value"
              :label="model.label"
              :value="model.value">
            </el-option>
          </el-select>
        </div>
        <div class="info-item-select">
          <span class="label">设备：</span>
          <el-select v-model="turbineName" placeholder="请选择设备" @change="handleTurbineChange" size="small">
            <el-option
              v-for="turbine in turbineList"
              :key="turbine.value"
              :label="turbine.label"
              :value="turbine.value">
            </el-option>
          </el-select>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 左侧栏 -->
      <div class="left-panel">
        <!-- 设备基本信息 -->
        <div class="info-card">
          <h3 class="card-title">设备基本信息</h3>
          <div class="info-content">
            <div class="info-item">
              <span class="label">设备名称：</span>
              <span class="value">{{ deviceInfo.deviceName }}</span>
            </div>
            <div class="info-item">
              <span class="label">设备编号：</span>
              <span class="value">{{ deviceInfo.deviceNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">制造厂商：</span>
              <span class="value">{{ deviceInfo.manufacturer }}</span>
            </div>
            <div class="info-item">
              <span class="label">制动器类型：</span>
              <span class="value">{{ deviceInfo.brakeType }}</span>
            </div>
            <div class="info-item">
              <span class="label">额定载重：</span>
              <span class="value">{{ deviceInfo.ratedLoad }}</span>
            </div>
            <div class="info-item">
              <span class="label">额定速度：</span>
              <span class="value">{{ deviceInfo.ratedSpeed }}</span>
            </div>
            <div class="info-item">
              <span class="label">额定功率：</span>
              <span class="value">{{ deviceInfo.ratedPower }}</span>
            </div>
            <div class="info-item">
              <span class="label">生产日期：</span>
              <span class="value">{{ deviceInfo.productionDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">使用单位：</span>
              <span class="value">{{ deviceInfo.useUnit }}</span>
            </div>
            <div class="info-item">
              <span class="label">安装日期：</span>
              <span class="value">{{ deviceInfo.installDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">管理人员：</span>
              <span class="value">{{ deviceInfo.manager }}</span>
            </div>
          </div>
        </div>

        <!-- 设备故障与报警履历 -->
        <div class="alarm-history-card">
          <h3 class="card-title">设备故障与报警履历</h3>
          <el-table 
            :data="alarmHistory" 
            style="width: 100%"
            max-height="300"
            :border="false">
            <el-table-column prop="time" label="时间" width="140" align="center"></el-table-column>
            <el-table-column prop="type" label="问题" align="center"></el-table-column>
            <el-table-column prop="level" label="级别" width="80" align="center">
              <template #default="scope">
                <el-tag :type="getAlarmTagType(scope.row.level)" size="small">
                  {{ scope.row.level }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 中间区域 -->
      <div class="center-panel">
        <!-- 设备多源测点3D图 -->
        <div class="device-model-card">
          <h3 class="card-title">风机多源测点</h3>
          <div class="model-container">
            <img src="/img/wel/turbine-model.png" alt="风机模型" class="device-model-img" />
            <!-- 测点标注 -->
            <div class="sensor-point" style="top: 20%; left: 15%;">
              <span class="point-dot"></span>
              <span class="point-label">发电机A</span>
            </div>
            <div class="sensor-point" style="top: 35%; left: 10%;">
              <span class="point-dot"></span>
              <span class="point-label">齿轮箱A</span>
            </div>
            <div class="sensor-point" style="top: 50%; left: 25%;">
              <span class="point-dot"></span>
              <span class="point-label">齿轮箱B</span>
            </div>
            <div class="sensor-point" style="top: 65%; left: 50%;">
              <span class="point-dot"></span>
              <span class="point-label">主轴承</span>
            </div>
          </div>
          <div class="status-label">
            <h4>故障量与报警次数分析</h4>
          </div>
        </div>

        <!-- 历史故障统计 -->
        <div class="fault-stats-card">
          <h3 class="card-title">历史故障统计</h3>
          <div class="stats-content">
            <div class="stats-header">
              <span class="turbine-name">{{ turbineName }}</span>
              <span class="fault-count">故障次数：<span class="count-num">{{ totalFaultCount }}</span> 次</span>
            </div>
            <div class="stats-bar-container">
              <div class="stats-bar" :style="{ width: '100%', background: 'linear-gradient(to right, #00cc99 0%, #00cc99 60%, #ffa500 60%, #ffa500 100%)' }"></div>
            </div>
            <div class="fault-type-list">
              <div class="fault-type-item" v-for="(item, index) in faultTypes" :key="index">
                <span class="type-icon" :style="{ backgroundColor: item.color }"></span>
                <span class="type-name">{{ item.name }}</span>
                <span class="type-count">{{ item.count }}次</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧栏 -->
      <div class="right-panel">
        <!-- 特征趋势分析 -->
        <div class="trend-chart-card">
          <h3 class="card-title">特征趋势分析</h3>
          <div ref="trendChart" class="chart"></div>
        </div>

        <!-- 故障量与报警次数分析 -->
        <div class="alarm-chart-card">
          <div ref="alarmChart" class="chart"></div>
        </div>

        <!-- 边缘端信息统计 -->
        <div class="edge-info-card">
          <h3 class="card-title">边缘端信息统计</h3>
          <div class="edge-stats">
            <div class="stat-circle-group">
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#00cc99 ${cpuUsage * 3.6}deg, rgba(255,255,255,0.1) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ cpuUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">CPU利用率</p>
              </div>
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#ffa500 ${memoryUsage * 3.6}deg, rgba(255,255,255,0.1) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ memoryUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">内存占用</p>
              </div>
            </div>
            <div class="stat-circle-group">
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#3a97f8 ${gpuUsage * 3.6}deg, rgba(255,255,255,0.1) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ gpuUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">GPU利用率</p>
              </div>
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#fd626e ${diskUsage * 3.6}deg, rgba(255,255,255,0.1) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ diskUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">硬盘空间</p>
              </div>
            </div>
            <div class="stat-bar-list">
              <div class="stat-bar-item">
                <span class="bar-label">往返时延 (RTT)</span>
                <div class="bar-container">
                  <div class="bar-fill" :style="{ width: rttPercent + '%' }"></div>
                </div>
                <span class="bar-value">{{ rttValue }}ms</span>
              </div>
              <div class="stat-bar-item">
                <span class="bar-label">丢包率</span>
                <div class="bar-container">
                  <div class="bar-fill" :style="{ width: packetLoss * 10 + '%', background: '#fd626e' }"></div>
                </div>
                <span class="bar-value">{{ packetLoss }}%</span>
              </div>
              <div class="stat-bar-item">
                <span class="bar-label">心跳状态</span>
                <div class="bar-container">
                  <div class="bar-fill" :style="{ width: '100%', background: '#00cc99' }"></div>
                </div>
                <span class="bar-value heartbeat-icon">♥</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getOverviewOfFarms, getStateOfTree, getProjectId } from '@/api/sw/wel'
import { getDeviceBaseInfo } from '@/api/sw/turbineDetail'
import { getMetaModelIdByUserId } from '@/api/diagnosis/graphVis/graphVisPro'

export default {
  name: 'TurbineDetail',
  data() {
    return {
      turbineName: '',
      farmName: '',
      turbineModel: '',
      status: '',
      sceneId: null,         // 场景ID (resume的id)
      
      // 下拉框数据列表
      farmList: [],          // 风场列表
      turbineModelList: [],  // 机型列表
      turbineList: [],       // 设备列表
      treeData: [],          // 完整树形数据
      
      // 设备基本信息
      deviceInfo: {
        deviceName: '',
        deviceNo: '',
        manufacturer: '',
        brakeType: '',
        ratedLoad: '',
        ratedSpeed: '',
        ratedPower: '',
        productionDate: '',
        useUnit: '',
        installDate: '',
        manager: ''
      },
      
      // 报警历史
      alarmHistory: [],
      
      // 故障类型统计
      faultTypes: [],
      totalFaultCount: 0,
      
      // 边缘端信息
      cpuUsage: 0,
      memoryUsage: 0,
      gpuUsage: 0,
      diskUsage: 0,
      rttValue: 0,
      rttPercent: 0,
      packetLoss: 0
    }
  },
  
  mounted() {
    // 获取路由参数
    this.turbineName = this.$route.query.turbineName || ''
    this.farmName = this.$route.query.farmName || ''
    this.turbineModel = this.$route.query.turbineModel || ''
    this.status = this.$route.query.status || ''
    
    // 加载下拉框数据
    this.loadTreeData()
  },
  
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    
    // 加载树形数据并初始化下拉框
    async loadTreeData() {
      try {
        const userId = this.$store.state.user.userInfo.userId
        const res = await getOverviewOfFarms({ userId: userId })
        this.treeData = res.data.data
        
        // 初始化风场列表
        this.farmList = this.treeData.map(farm => ({
          label: farm.label,
          value: farm.label
        }))
        
        // 如果有路由参数，使用路由参数；否则选择第一个
        if (!this.farmName && this.farmList.length > 0) {
          this.farmName = this.farmList[0].value
        }
        
        // 根据选中的风场加载机型列表
        this.handleFarmChange(this.farmName)
      } catch (error) {
        console.error('加载数据失败', error)
      }
    },
    
    // 风场切换事件
    handleFarmChange(farmName) {
      const selectedFarm = this.treeData.find(farm => farm.label === farmName)
      if (selectedFarm && selectedFarm.children) {
        // 保存场景ID（如果数据中包含）
        if (selectedFarm.metaModelId || selectedFarm.sceneId || selectedFarm.id) {
          this.sceneId = selectedFarm.metaModelId || selectedFarm.sceneId || selectedFarm.id
          console.log('从风场数据中获取场景ID:', this.sceneId)
        }
        
        // 更新机型列表
        this.turbineModelList = selectedFarm.children.map(model => ({
          label: model.label,
          value: model.label,
          metaModelId: model.metaModelId || model.sceneId || model.id // 保存ID信息
        }))
        
        // 如果有路由参数，使用路由参数；否则选择第一个
        if (!this.turbineModel && this.turbineModelList.length > 0) {
          this.turbineModel = this.turbineModelList[0].value
          // 尝试从机型数据中获取sceneId
          if (this.turbineModelList[0].metaModelId && !this.sceneId) {
            this.sceneId = this.turbineModelList[0].metaModelId
          }
        }
        
        // 加载设备列表
        this.handleModelChange(this.turbineModel)
      } else {
        this.turbineModelList = []
        this.turbineList = []
      }
    },
    
    // 机型切换事件
    async handleModelChange(modelName) {
      if (!this.farmName || !modelName) return
      
      try {
        const res = await getStateOfTree({ 
          farmName: this.farmName, 
          turbineModel: modelName 
        })
        
        const turbines = res.data.data || []
        this.turbineList = turbines.map(turbine => ({
          label: turbine.name,
          value: turbine.name,
          status: turbine.status
        }))
        
        // 如果有路由参数，使用路由参数；否则选择第一个
        if (!this.turbineName && this.turbineList.length > 0) {
          this.turbineName = this.turbineList[0].value
          this.status = this.turbineList[0].status
        }
        
        // 加载设备详细数据
        if (this.turbineName) {
          this.loadAllData()
        }
      } catch (error) {
        console.error('获取设备列表失败', error)
      }
    },
    
    // 设备切换事件
    handleTurbineChange(turbineName) {
      const selectedTurbine = this.turbineList.find(t => t.value === turbineName)
      if (selectedTurbine) {
        this.status = selectedTurbine.status
      }
      // 重新加载所有数据
      this.loadAllData()
    },
    
    // 加载所有数据
    loadAllData() {
      this.loadDeviceInfo()
      this.loadAlarmHistory()
      this.loadFaultStats()
      this.loadEdgeInfo()
      
      // 渲染图表
      this.$nextTick(() => {
        this.renderTrendChart()
        this.renderAlarmChart()
      })
    },
    
    // 加载设备基本信息
    async loadDeviceInfo() {
      if (!this.turbineModel) {
        console.warn('设备编码(turbineModel)为空，无法加载设备信息')
        return
      }
      
      try {
        // 首先获取场景ID
        if (!this.sceneId) {
          console.log('开始获取场景ID, farmName:', this.farmName, 'turbineModel:', this.turbineModel)
          
          // 方法1: 尝试使用 getMetaModelIdByUserId 接口
          try {
            const userId = this.$store.state.user.userInfo.userId
            const metaRes = await getMetaModelIdByUserId({ userId, userRole: 'GENERAL_USER' })
            
            if (metaRes && metaRes.data && metaRes.data.code === 0 && metaRes.data.data && metaRes.data.data.metaModelId) {
              this.sceneId = metaRes.data.data.metaModelId
              console.log('使用 getMetaModelIdByUserId 获取到场景ID:', this.sceneId)
            } else {
              console.log('getMetaModelIdByUserId 未返回有效数据，尝试使用 getProjectId')
              
              // 方法2: 使用 getProjectId 接口（作为备用）
              const projectRes = await getProjectId({ 
                project: this.farmName, 
                productModel: this.turbineModel 
              })
              
              if (projectRes && projectRes.data && projectRes.data.data !== undefined) {
                this.sceneId = projectRes.data.data
                console.log('使用 getProjectId 获取到场景ID:', this.sceneId)
              }
            }
          } catch (metaError) {
            console.error('getMetaModelIdByUserId 调用失败，尝试使用 getProjectId:', metaError)
            
            // 备用方案
            const projectRes = await getProjectId({ 
              project: this.farmName, 
              productModel: this.turbineModel 
            })
            
            if (projectRes && projectRes.data && projectRes.data.data !== undefined) {
              this.sceneId = projectRes.data.data
              console.log('使用 getProjectId 获取到场景ID:', this.sceneId)
            }
          }
          
          // 如果仍然没有获取到sceneId
          if (!this.sceneId) {
            console.error('无法获取场景ID')
            this.$message.warning('获取场景ID失败，使用默认信息')
            this.setDefaultDeviceInfo()
            return
          }
        }
        
        console.log('准备调用设备信息接口, deviceCode:', this.turbineModel, 'sceneId:', this.sceneId)
        
        // 调用设备基本信息接口
        const res = await getDeviceBaseInfo({ 
          deviceCode: this.turbineModel,  // 设备编码对应 product_model
          usageUnit: this.farmName            // 场景ID对应 resume的id
        })
        
        console.log('设备信息接口响应:', res)
        
        // 安全访问响应数据
        if (res && res.data && res.data.data && Array.isArray(res.data.data) && res.data.data.length > 0) {
          // 取第一条数据
          const deviceData = res.data.data[0]
          this.deviceInfo = {
            deviceName: deviceData.deviceName || this.turbineName,
            deviceNo: deviceData.deviceNo || deviceData.deviceCode || '',
            manufacturer: deviceData.manufacturer || '',
            brakeType: deviceData.brakeType || '',
            ratedLoad: deviceData.ratedLoad || '',
            ratedSpeed: deviceData.ratedSpeed || '',
            ratedPower: deviceData.ratedPower || '',
            productionDate: deviceData.productionDate || '',
            useUnit: deviceData.useUnit || '',
            installDate: deviceData.installDate || '',
            manager: deviceData.manager || ''
          }
          console.log('设备信息加载成功:', this.deviceInfo)
        } else {
          console.warn('未获取到设备基本信息数据，响应数据:', res)
          this.setDefaultDeviceInfo()
        }
      } catch (error) {
        console.error('加载设备基本信息失败', error)
        const errorMsg = error && error.message ? error.message : '未知错误'
        this.$message.error(`加载设备信息失败: ${errorMsg}`)
        this.setDefaultDeviceInfo()
      }
    },
    
    // 设置默认设备信息
    setDefaultDeviceInfo() {
      this.deviceInfo = {
        deviceName: this.turbineName,
        deviceNo: '',
        manufacturer: '',
        brakeType: '',
        ratedLoad: '',
        ratedSpeed: '',
        ratedPower: '',
        productionDate: '',
        useUnit: '',
        installDate: '',
        manager: ''
      }
    },
    
    // 加载报警历史
    async loadAlarmHistory() {
      // TODO: 调用实际API
      // const res = await getAlarmHistory({ turbineName: this.turbineName })
      // this.alarmHistory = res.data.data
      
      // 模拟数据
      this.alarmHistory = [
        { time: '2021-09-11 14:06', type: '叶轮振动异常', level: '高' },
        { time: '2021-09-10 16:09', type: '齿轮箱温度过高', level: '中' },
        { time: '2021-09-09 17:43', type: '偏航系统故障', level: '低' },
        { time: '2021-09-08 09:54', type: '发电机电压波动', level: '中' },
        { time: '2021-09-07 09:54', type: '主轴承温度告警', level: '高' }
      ]
    },
    
    // 加载故障统计
    async loadFaultStats() {
      // TODO: 调用实际API
      // const res = await getFaultStats({ turbineName: this.turbineName })
      
      // 模拟数据
      this.totalFaultCount = 12
      this.faultTypes = [
        { name: '制动力不足', count: 7, color: '#fd626e' },
        { name: '问题过大', count: 2, color: '#ffa500' },
        { name: '未预设故障', count: 2, color: '#00cc99' },
        { name: '表面损坏或毛刺', count: 0, color: '#00cc99' },
        { name: '全部告知', count: 0, color: '#00cc99' },
        { name: '其他磨损', count: 0, color: '#3a97f8' }
      ]
    },
    
    // 加载边缘端信息
    async loadEdgeInfo() {
      // TODO: 调用实际API
      // const res = await getEdgeInfo({ turbineName: this.turbineName })
      
      // 模拟数据
      this.cpuUsage = 65
      this.memoryUsage = 75
      this.gpuUsage = 40
      this.diskUsage = 30
      this.rttValue = 55
      this.rttPercent = 55
      this.packetLoss = 2
    },
    
    // 渲染趋势图
    renderTrendChart() {
      if (!this.$refs.trendChart) return
      const chart = echarts.getInstanceByDom(this.$refs.trendChart) || echarts.init(this.$refs.trendChart)
      
      // 生成模拟数据
      const xData = []
      const data1 = []
      const data2 = []
      for (let i = 0; i <= 300; i += 10) {
        xData.push(i)
        data1.push(30 + Math.random() * 20)
        data2.push(35 + Math.random() * 15)
      }
      
      chart.setOption({
        grid: { left: '10%', right: '10%', top: 56, bottom: '15%', containLabel: true },
        xAxis: {
          type: 'category',
          data: xData,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisLabel: { color: '#fff' }
        },
        yAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisLabel: { color: '#fff' },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
        },
        series: [
          {
            name: '特征1',
            type: 'line',
            data: data1,
            smooth: true,
            lineStyle: { color: '#00cc99' },
            itemStyle: { color: '#00cc99' }
          },
          {
            name: '特征2',
            type: 'line',
            data: data2,
            smooth: true,
            lineStyle: { color: '#ffa500' },
            itemStyle: { color: '#ffa500' }
          }
        ],
        legend: {
          data: ['特征1', '特征2'],
          top: 12,
          right: 20,
          textStyle: { color: '#fff' }
        }
      })
    },
    
    // 渲染报警柱状图
    renderAlarmChart() {
      if (!this.$refs.alarmChart) return
      const chart = echarts.getInstanceByDom(this.$refs.alarmChart) || echarts.init(this.$refs.alarmChart)
      
      // 生成模拟数据
      const xData = []
      const normalData = []
      const abnormalData = []
      for (let i = 5; i <= 150; i += 5) {
        xData.push(i)
        normalData.push(Math.floor(Math.random() * 80) + 40)
        abnormalData.push(Math.floor(Math.random() * 100) + 50)
      }
      
      chart.setOption({
        grid: { left: '10%', right: '10%', top: '10%', bottom: '15%' },
        xAxis: {
          type: 'category',
          data: xData,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisLabel: { color: '#fff', fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisLabel: { color: '#fff' },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
        },
        series: [
          {
            name: '正常值',
            type: 'bar',
            data: normalData,
            itemStyle: { color: '#00cc99' },
            barWidth: '40%'
          },
          {
            name: '异常值',
            type: 'bar',
            data: abnormalData,
            itemStyle: { color: '#ffa500' },
            barWidth: '40%'
          }
        ],
        legend: {
          data: ['正常值', '异常值'],
          top: 0,
          right: 20,
          textStyle: { color: '#fff' }
        }
      })
    },
    
    getAlarmTagType(level) {
      const typeMap = {
        '高': 'danger',
        '中': 'warning',
        '低': 'info'
      }
      return typeMap[level] || 'info'
    }
  }
}
</script>

<style scoped lang="scss">
.turbine-detail-container {
  min-height: 100vh;
  height: 100vh;
  padding: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  background-attachment: fixed;
  overflow-y: auto;
  overflow-x: hidden;
}

.page-header {
  margin-bottom: 20px;
  
  ::v-deep(.el-breadcrumb) {
    font-size: 14px;
    margin-bottom: 10px;
    
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        color: rgba(255, 255, 255, 0.8);
      }
      
      &.clickable .el-breadcrumb__inner {
        cursor: pointer;
        
        &:hover {
          color: #00cc99;
        }
      }
    }
    
    .el-breadcrumb__separator {
      color: rgba(255, 255, 255, 0.5);
    }
  }
  
  .header-info {
    display: flex;
    gap: 30px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);
    
    .info-item {
      padding: 5px 15px;
      background: rgba(0, 204, 153, 0.2);
      border-radius: 4px;
      border: 1px solid rgba(0, 204, 153, 0.3);
    }
    
    .info-item-select {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .label {
        color: rgba(255, 255, 255, 0.9);
        font-size: 14px;
        white-space: nowrap;
      }
      
      ::v-deep(.el-select) {
        width: 180px;
        
        .el-input__wrapper {
          background: rgba(0, 204, 153, 0.15);
          border: 1px solid rgba(0, 204, 153, 0.4);
          box-shadow: none;
          
          &:hover {
            border-color: rgba(0, 204, 153, 0.6);
          }
        }
        
        .el-input__inner {
          color: #00cc99;
          font-size: 14px;
        }
        
        .el-select__caret {
          color: rgba(255, 255, 255, 0.6);
        }
      }
    }
  }
}

// 下拉框弹出层样式
::v-deep(.el-select-dropdown) {
  background: rgba(30, 50, 70, 0.95) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 204, 153, 0.3) !important;
  
  .el-select-dropdown__item {
    color: rgba(255, 255, 255, 0.8);
    
    &:hover {
      background: rgba(0, 204, 153, 0.2) !important;
      color: #00cc99;
    }
    
    &.selected {
      background: rgba(0, 204, 153, 0.3) !important;
      color: #00cc99;
      font-weight: bold;
    }
  }
  
  .el-popper__arrow::before {
    background: rgba(30, 50, 70, 0.95);
    border: 1px solid rgba(0, 204, 153, 0.3);
  }
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 20px;
  min-height: 0;
  height: auto;
  overflow: visible;
}

// 卡片通用样式
.card-title {
  margin: 0;
  padding: 10px 15px;
  background: url('/img/wel/title-bg.png') center / contain no-repeat;
  font-size: 16px;
  text-align: left;
  text-indent: 10px;
}

// 左侧面板
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: visible;
}

.info-card, .alarm-history-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.info-content {
  padding: 15px;
  max-height: none;
  overflow: visible;
  
  .info-item {
    display: flex;
    margin-bottom: 12px;
    font-size: 14px;
    
    .label {
      color: rgba(255, 255, 255, 0.7);
      min-width: 90px;
    }
    
    .value {
      color: #00cc99;
      flex: 1;
      word-break: break-word;
    }
  }
}

.alarm-history-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  
  ::v-deep(.el-table) {
    background: transparent !important;
    color: #fff;
  }
  
  ::v-deep(.el-table th),
  ::v-deep(.el-table tr),
  ::v-deep(.el-table td) {
    background: transparent !important;
    border: none !important;
    border-bottom: 1px solid rgba(65, 228, 187, 0.1) !important;
  }
  
  ::v-deep(.el-table__inner-wrapper::before) {
    display: none !important;
  }
}

// 中间面板
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: visible;
}

.device-model-card {
  flex: 2;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
}

.model-container {
  flex: 1;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  
  .device-model-img {
    max-width: 80%;
    max-height: 80%;
    object-fit: contain;
  }
  
  .sensor-point {
    position: absolute;
    display: flex;
    align-items: center;
    gap: 5px;
    
    .point-dot {
      width: 12px;
      height: 12px;
      background: #00cc99;
      border-radius: 50%;
      box-shadow: 0 0 10px #00cc99;
      animation: pulse 2s infinite;
    }
    
    .point-label {
      font-size: 12px;
      background: rgba(0, 0, 0, 0.6);
      padding: 3px 8px;
      border-radius: 3px;
      white-space: nowrap;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 10px #00cc99;
  }
  50% {
    box-shadow: 0 0 20px #00cc99;
  }
}

.status-label {
  padding: 10px 20px;
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  
  h4 {
    margin: 0;
    font-size: 14px;
  }
}

.fault-stats-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stats-content {
  padding: 15px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  
  .turbine-name {
    font-size: 16px;
    font-weight: bold;
  }
  
  .fault-count {
    color: rgba(255, 255, 255, 0.8);
    
    .count-num {
      color: #fd626e;
      font-size: 18px;
      font-weight: bold;
    }
  }
}

.stats-bar-container {
  height: 30px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 15px;
  overflow: hidden;
  margin-bottom: 15px;
  
  .stats-bar {
    height: 100%;
    transition: width 0.3s;
  }
}

.fault-type-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.fault-type-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  
  .type-icon {
    width: 10px;
    height: 10px;
    border-radius: 2px;
  }
  
  .type-name {
    flex: 1;
  }
  
  .type-count {
    color: rgba(255, 255, 255, 0.7);
  }
}

// 右侧面板
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: visible;
}

.trend-chart-card, .alarm-chart-card, .edge-info-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.trend-chart-card {
  min-height: 250px;
  display: flex;
  flex-direction: column;
}

.alarm-chart-card {
  min-height: 250px;
  display: flex;
  flex-direction: column;
}

.chart {
  width: 100%;
  flex: 1;
  min-height: 200px;
}

.edge-info-card {
  min-height: 300px;
}

.edge-stats {
  padding: 15px;
}

.stat-circle-group {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.stat-circle {
  text-align: center;
  
  .circle-progress {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0 auto 10px;
    
    .circle-inner {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      justify-content: center;
      align-items: center;
      
      .circle-value {
        font-size: 16px;
        font-weight: bold;
      }
    }
  }
  
  .circle-label {
    font-size: 12px;
    margin: 0;
    color: rgba(255, 255, 255, 0.8);
  }
}

.stat-bar-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .bar-label {
    min-width: 100px;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.8);
  }
  
  .bar-container {
    flex: 1;
    height: 20px;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 10px;
    overflow: hidden;
    
    .bar-fill {
      height: 100%;
      background: #00cc99;
      transition: width 0.3s;
    }
  }
  
  .bar-value {
    min-width: 50px;
    text-align: right;
    font-size: 12px;
    
    &.heartbeat-icon {
      color: #fd626e;
      font-size: 16px;
      animation: heartbeat 1.5s infinite;
    }
  }
}

@keyframes heartbeat {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}
</style>
