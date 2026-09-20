<template>
  <div class="turbine-detail-container" :class="['board-view-' + activeBoardView, { 'configuration-mode': configurationOnly }]">
    <template v-if="!configurationOnly">
    <header class="screen-header">
      <div class="screen-device-summary">
        <button class="back-overview-button" type="button" @click="goBack" aria-label="返回总览" title="返回总览">
          <i class="el-icon-arrow-left"></i>
        </button>
        <div class="screen-device-identity">
          <strong>{{ deviceInfo.deviceName || turbineName || '--' }}</strong>
          <span>{{ deviceInfo.deviceNo || turbineCode || '--' }}</span>
          <em>{{ farmName || '--' }} / {{ turbineModel || '--' }}</em>
        </div>
        <span class="status-badge" :class="healthStatusClass">{{ healthStatusLabel }}</span>
      </div>

      <div class="screen-controls">
        <div class="selector-row">
          <label class="info-item-select">
            <span class="label">场景</span>
            <el-select v-model="farmName" placeholder="请选择场景" @change="handleFarmChange" size="small">
              <el-option
                v-for="farm in farmList"
                :key="farm.value"
                :label="farm.label"
                :value="farm.value">
              </el-option>
            </el-select>
          </label>
          <label class="info-item-select">
            <span class="label">机型</span>
            <el-select v-model="turbineModel" placeholder="请选择机型" @change="handleModelChange" size="small">
              <el-option
                v-for="model in turbineModelList"
                :key="model.value"
                :label="model.label"
                :value="model.value">
              </el-option>
            </el-select>
          </label>
          <label class="info-item-select">
            <span class="label">设备</span>
            <el-select v-model="turbineName" placeholder="请选择设备" @change="handleTurbineChange" size="small">
              <el-option
                v-for="turbine in turbineList"
                :key="turbine.value"
                :label="turbine.label"
                :value="turbine.value">
              </el-option>
            </el-select>
          </label>
        </div>
        <div class="screen-switch" role="tablist">
          <button
            v-for="item in boardViews"
            :key="item.value"
            type="button"
            :class="{ active: activeBoardView === item.value }"
            @click="activeBoardView = item.value"
          >
            {{ item.label }}
          </button>
        </div>
        <button class="config-workbench-button" type="button" @click="goToConfigurationPage">
          <i class="el-icon-setting"></i>
          <span>前往配置</span>
        </button>
      </div>
    </header>

    <section class="screen-kpis">
      <div
        v-for="item in boardStats"
        :key="item.label"
        class="kpi-tile"
        :class="['tone-' + item.tone, { 'is-text-value': item.text }]"
      >
        <span class="kpi-label">{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <span class="kpi-unit">{{ item.unit }}</span>
      </div>
    </section>

    <section class="workflow-strip">
      <div
        v-for="step in workflowSteps"
        :key="step.key"
        class="workflow-node"
        :class="step.state"
      >
        <span class="workflow-index">{{ step.index }}</span>
        <span class="workflow-title">{{ step.title }}</span>
        <span class="workflow-value">{{ step.value }}</span>
      </div>
    </section>

    <div v-if="activeBoardView !== 'data'" class="content-wrapper" :class="'view-' + activeBoardView">
      <aside class="left-panel">
        <section class="screen-panel workflow-panel">
          <div class="panel-heading">
            <span class="panel-kicker">Configuration Flow</span>
            <h3>配置闭环</h3>
          </div>
          <div class="vertical-flow">
            <div
              v-for="step in workflowSteps"
              :key="step.key"
              class="flow-step"
              :class="step.state"
            >
              <span class="flow-dot"></span>
              <div class="flow-copy">
                <strong>{{ step.title }}</strong>
                <span>{{ step.value }}</span>
                <em>{{ step.meta }}</em>
              </div>
            </div>
          </div>
        </section>

        <section class="screen-panel device-profile">
          <div class="panel-heading compact">
            <span class="panel-kicker">Device Profile</span>
            <h3>电梯设备信息</h3>
          </div>
          <div class="device-overview">
            <div>
              <span>当前设备</span>
              <strong>{{ deviceInfo.deviceName || turbineName || '--' }}</strong>
              <em>{{ deviceInfo.deviceNo || turbineCode || '--' }}</em>
            </div>
            <b :class="healthStatusClass">{{ healthStatusLabel }}</b>
          </div>
          <div class="device-grid">
            <div class="profile-item">
              <span>所属场景</span>
              <strong>{{ farmName || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>产品机型</span>
              <strong>{{ turbineModel || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>制动器类型</span>
              <strong>{{ deviceInfo.brakeType || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>生产厂商</span>
              <strong>{{ deviceInfo.manufacturer || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>额定载重</span>
              <strong>{{ deviceInfo.ratedLoad || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>额定速度</span>
              <strong>{{ deviceInfo.ratedSpeed || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>额定功率</span>
              <strong>{{ deviceInfo.ratedPower || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>生产日期</span>
              <strong>{{ deviceInfo.productionDate || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>安装日期</span>
              <strong>{{ deviceInfo.installDate || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>安装负责人</span>
              <strong>{{ deviceInfo.manager || '--' }}</strong>
            </div>
            <div class="profile-item">
              <span>使用单位</span>
              <strong>{{ deviceInfo.useUnit || '--' }}</strong>
            </div>
          </div>
        </section>

        <section class="screen-panel alarm-ledger">
          <div class="panel-heading compact">
            <span class="panel-kicker">Alarm Ledger</span>
            <h3>近7天报警履历</h3>
          </div>
          <el-table
            :data="alarmHistory"
            style="width: 100%"
            max-height="260"
            :border="false"
            empty-text="近7天暂无报警信息">
            <el-table-column prop="time" label="时间" min-width="136" align="center"></el-table-column>
            <el-table-column prop="type" label="问题" min-width="96" align="center"></el-table-column>
            <el-table-column prop="resolved" label="状态" width="70" align="center"></el-table-column>
          </el-table>
        </section>
      </aside>

      <main class="center-panel">
        <section class="screen-panel model-panel">
          <div class="panel-heading model-heading">
            <div>
              <span class="panel-kicker">Sensing Layout</span>
              <h3>测点配置感知</h3>
            </div>
            <div class="active-node-pill">{{ selectedNodeLabel }}</div>
          </div>
          <div class="model-layout">
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
                >
                  <span class="point-label">{{ point.label }}</span>
                  <span class="point-dot" :class="{ active: isPointActive(point), related: isPointRelated(point), disabled: !point.node }"></span>
                  <span class="point-ring" :class="{ active: isPointActive(point), related: isPointRelated(point), disabled: !point.node }"></span>
                </div>
              </div>
            </div>

            <div class="gbom-tree-panel">
              <div class="gbom-tree-title">
                <span>实例化 GBOM 树</span>
                <strong>{{ gbomNodeCount }} 节点</strong>
              </div>
              <el-scrollbar v-if="elevatorGbomTree.length" class="gbom-tree-scroll">
                <el-tree
                  ref="gbomTreeRef"
                  :data="elevatorGbomTree"
                  node-key="id"
                  :props="gbomTreeProps"
                  :expand-on-click-node="true"
                  :default-expanded-keys="gbomExpandKeys"
                  :highlight-current="true"
                  @node-click="handleGbomNodeClick"
                  @node-expand="handleGbomNodeExpand"
                >
                  <template #default="{ data }">
                    <span
                      class="gbom-tree-node"
                      :class="{
                        mapped: isNodeMappedToPoint(data),
                        active: isNodeActive(data),
                        related: isNodeRelated(data)
                      }"
                      @mouseenter="handleGbomNodeMouseEnter(data)"
                      @mouseleave="handleGbomNodeMouseLeave"
                    >
                      <span v-if="isNodeMappedToPoint(data)" class="gbom-node-indicator"></span>
                      <span class="gbom-node-label">{{ getGbomNodeLabel(data) }}</span>
                    </span>
                  </template>
                </el-tree>
              </el-scrollbar>
              <div v-else class="gbom-empty">暂无该设备实例化 GBOM 树数据</div>
              <div class="gbom-variable-box">
                <div class="gbom-variable-title">
                  <span>监测任务</span>
                  <strong>{{ monitoringTaskOptions.length }} 项</strong>
                </div>
                <div v-if="perceivedVarLoading" class="gbom-variable-tip">监测任务加载中...</div>
                <div v-else-if="!monitoringTaskOptions.length" class="gbom-variable-tip">当前节点未配置监测任务</div>
                <el-scrollbar v-else class="gbom-variable-list">
                  <button
                    v-for="item in monitoringTaskOptions"
                    :key="item.key"
                    type="button"
                    class="gbom-variable-item gbom-task-item"
                    :class="{ active: item.variableKeys.includes(selectedVariable) }"
                    @click="selectMonitoringTask(item)"
                  >
                    <span>{{ item.label }}</span>
                    <em :class="item.statusClass">{{ item.statusText }}</em>
                  </button>
                </el-scrollbar>
              </div>
            </div>
          </div>
        </section>

        <section class="screen-panel relation-panel">
          <div class="panel-heading compact">
            <span class="panel-kicker">Monitoring Tasks</span>
            <h3>监测任务</h3>
          </div>
          <div v-if="!monitoringTaskCards.length" class="relation-empty">
            <strong>{{ selectedNodeLabel }}</strong>
            <span>暂无监测任务</span>
          </div>
          <el-scrollbar v-else class="relation-scroll">
            <div
              v-for="item in monitoringTaskCards"
              :key="item.key"
              class="relation-card"
              :class="{ active: item.active, running: item.isRunning }"
              role="button"
              tabindex="0"
              @click="selectMonitoringTask(item)"
              @keydown.enter.prevent="selectMonitoringTask(item)"
              @keydown.space.prevent="selectMonitoringTask(item)"
            >
              <span class="relation-name">{{ item.label }}</span>
              <span class="relation-meta">{{ item.taskId ? ('任务 ' + item.taskId) : '尚未生成任务' }}</span>
              <span class="relation-meta">{{ item.algoShortname || '未配置算法' }}</span>
              <span class="task-chip" :class="item.statusClass">{{ item.statusText }}</span>
              <button
                type="button"
                class="run-task-button"
                :class="{ running: item.isRunning }"
                :disabled="!item.canRun || item.actionLoading || item.isRunning"
                @click.stop="runMonitoringTask(item)"
              >
                {{ !item.taskId ? '未生成' : (item.actionLoading ? '启动中' : (item.isRunning ? '运行中' : '运行')) }}
              </button>
            </div>
          </el-scrollbar>
        </section>

        <section class="screen-panel alarm-detail-card">
          <div class="panel-heading compact">
            <span class="panel-kicker">Alarm Detail</span>
            <h3>近7天报警明细</h3>
          </div>
          <div v-if="!alarmDetailList.length" class="alarm-detail-empty">
            近7天暂无报警
          </div>
          <el-scrollbar v-else class="alarm-detail-scroll">
            <div
              v-for="item in alarmDetailList"
              :key="item.key"
              class="alarm-detail-item"
            >
              <div class="alarm-detail-main">
                <strong>{{ item.title }}</strong>
                <span>{{ item.nodeName }}</span>
              </div>
              <div class="alarm-detail-meta">
                <span>{{ item.time }}</span>
                <span>{{ item.modelName }}</span>
              </div>
              <div class="alarm-detail-actions">
                <em :class="item.statusClass">{{ item.statusText }}</em>
                <button
                  type="button"
                  class="alarm-diagnosis-button"
                  :class="{ 'is-view': item.diagnosisCompleted }"
                  :disabled="item.diagnosisProcessing"
                  @click="openAlarmDiagnosis(item)"
                >
                  <i :class="item.diagnosisCompleted ? 'el-icon-view' : 'el-icon-right'"></i>
                  <span>{{ item.diagnosisActionText }}</span>
                </button>
              </div>
            </div>
          </el-scrollbar>
        </section>
      </main>

      <aside class="right-panel">
        <section class="screen-panel trend-chart-card">
          <div class="panel-heading chart-heading">
            <div>
              <span v-if="activeBoardView !== 'fusion'" class="panel-kicker">Monitoring Trend</span>
              <h3>{{ activeBoardView === 'fusion' ? '实时监测运行状态' : '监测趋势' }}</h3>
            </div>
            <div v-if="activeBoardView !== 'fusion'" class="diagnosis-summary">
              <span>{{ selectedVariableName }}</span>
              <span>任务 {{ selectedTaskIdText }}</span>
              <span>{{ selectedAlgorithmName }}</span>
            </div>
          </div>
          <div
            v-show="activeBoardView === 'fusion'"
            class="monitor-runtime-overview"
            :class="monitorStatusClass"
          >
            <div class="monitor-status-hero">
              <div class="monitor-status-copy">
                <span class="monitor-live-dot"></span>
                <div>
                  <strong>{{ monitorStatusLabel }}</strong>
                  <span>{{ monitorLatestTimeLabel }}</span>
                </div>
              </div>
            </div>

            <div class="monitor-runtime-metrics">
              <div>
                <span>异常评分</span>
                <strong>{{ monitorScoreText }}</strong>
              </div>
              <div>
                <span>判定阈值</span>
                <strong>{{ monitorThresholdText }}</strong>
              </div>
              <div>
                <span>近30分钟</span>
                <strong class="monitor-count-summary">
                  <b>{{ monitoringSnapshot.normalCount }}</b><small>正常</small>
                  <b :class="{ danger: monitoringSnapshot.anomalyCount > 0 }">{{ monitoringSnapshot.anomalyCount }}</b><small>异常</small>
                </strong>
              </div>
            </div>

            <div class="monitor-window-section">
              <div class="monitor-window-heading">
                <span>最近30个窗口</span>
                <div class="monitor-window-legend" aria-label="窗口状态图例">
                  <span><i class="is-normal"></i>正常</span>
                  <span><i class="is-anomaly"></i>异常</span>
                  <span><i class="is-empty"></i>无数据</span>
                </div>
              </div>
              <div v-if="monitoringSnapshot.windowCount" class="monitor-window-track">
                <span
                  v-for="item in monitorWindowItems"
                  :key="item.key"
                  :class="item.isEmpty ? 'is-empty' : (item.isAlarm ? 'is-anomaly' : 'is-normal')"
                  :title="item.title"
                ></span>
              </div>
              <div v-else class="monitor-window-empty">等待首个监测窗口</div>
            </div>
          </div>
          <div
            v-show="activeBoardView !== 'fusion'"
            class="monitor-runtime-strip"
            :class="monitorStatusClass"
          >
            <div class="monitor-strip-status">
              <i></i>
              <div>
                <strong>{{ monitorStatusLabel }}</strong>
                <span>{{ monitorLatestTimeLabel }}</span>
              </div>
            </div>
            <div><span>异常评分</span><strong>{{ monitorScoreText }}</strong></div>
            <div><span>判定阈值</span><strong>{{ monitorThresholdText }}</strong></div>
            <div><span>近30分钟</span><strong>{{ monitoringSnapshot.normalCount }} 正常 / {{ monitoringSnapshot.anomalyCount }} 异常</strong></div>
          </div>
          <div v-show="activeBoardView !== 'fusion'" ref="trendChart" class="chart"></div>
        </section>

        <section class="right-lower-grid">
          <div class="screen-panel alarm-chart-card">
            <div class="panel-heading compact">
              <span class="panel-kicker">{{ activeBoardView === 'fusion' ? 'Edge Link' : 'Fault Profile' }}</span>
              <h3>{{ activeBoardView === 'fusion' ? '链路与采集状态' : '故障信息统计' }}</h3>
            </div>
            <div v-show="activeBoardView === 'fusion' && edgeIsOnline" class="edge-info-summary">
              <div class="edge-metric-grid">
                <div class="edge-metric-item">
                  <span>实时入站</span>
                  <strong>{{ edgeInboundRate }} <small>Mbps</small></strong>
                  <i><b :style="{ width: edgeInboundPercent + '%' }"></b></i>
                </div>
                <div class="edge-metric-item">
                  <span>实时出站</span>
                  <strong>{{ edgeOutboundRate }} <small>Mbps</small></strong>
                  <i><b :style="{ width: edgeOutboundPercent + '%' }"></b></i>
                </div>
                <div class="edge-metric-item">
                  <span>节点温度</span>
                  <strong>{{ edgeTemperature }} <small>°C</small></strong>
                  <i><b class="warm" :style="{ width: edgeTemperaturePercent + '%' }"></b></i>
                </div>
                <div class="edge-metric-item">
                  <span>采样速率</span>
                  <strong>{{ edgeSampleRate }} <small>Hz</small></strong>
                  <i><b class="sample" :style="{ width: edgeSamplePercent + '%' }"></b></i>
                </div>
              </div>
              <div class="edge-link-row">
                <span>往返时延 <strong>{{ rttValue }}ms</strong></span>
                <span>丢包率 <strong>{{ packetLoss }}%</strong></span>
                <span>活动任务 <strong>{{ edgeActiveTasks }}</strong></span>
              </div>
            </div>
            <div v-show="activeBoardView === 'fusion' && !edgeIsOnline" class="edge-link-offline">
              当前设备未建立边缘链路
            </div>
            <div v-show="activeBoardView !== 'fusion'" class="diagnosis-fault-summary">
              <div class="fault-summary-head">
                <span>累计故障</span>
                <strong>{{ totalFaultCount }}</strong>
                <em>次</em>
              </div>
              <div class="fault-summary-stack">
                <span
                  v-for="item in faultTypes"
                  :key="item.name"
                  :style="{ flex: item.count > 0 ? item.count : 0.45, backgroundColor: item.color }"
                  :title="`${item.name} ${item.count}次`"
                ></span>
              </div>
              <div class="fault-summary-list">
                <div class="fault-summary-item" v-for="item in faultTypes" :key="item.name">
                  <span class="type-icon" :style="{ backgroundColor: item.color }"></span>
                  <span class="type-name">{{ item.name }}</span>
                  <strong>{{ item.count }}次</strong>
                </div>
                <div v-if="!faultTypes.length" class="fault-summary-empty">暂无故障字典</div>
              </div>
            </div>
          </div>
        </section>

        <section class="screen-panel edge-info-card">
          <div class="panel-heading compact">
            <span class="panel-kicker">Edge Runtime</span>
            <h3>边缘端运行态势</h3>
          </div>
          <div class="edge-stats">
            <div class="stat-circle-group">
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#39d7f5 ${cpuUsage * 3.6}deg, rgba(255,255,255,0.08) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ cpuUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">CPU利用率</p>
              </div>
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#f5c84b ${memoryUsage * 3.6}deg, rgba(255,255,255,0.08) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ memoryUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">内存占用</p>
              </div>
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#4ea8ff ${gpuUsage * 3.6}deg, rgba(255,255,255,0.08) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ gpuUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">GPU利用率</p>
              </div>
              <div class="stat-circle">
                <div class="circle-progress" :style="{ background: `conic-gradient(#ff6b7a ${diskUsage * 3.6}deg, rgba(255,255,255,0.08) 0deg)` }">
                  <div class="circle-inner">
                    <span class="circle-value">{{ diskUsage }}%</span>
                  </div>
                </div>
                <p class="circle-label">硬盘空间</p>
              </div>
            </div>
            <div class="stat-bar-list">
              <div class="stat-bar-item">
                <span class="bar-label">往返时延</span>
                <div class="bar-container">
                  <div class="bar-fill" :style="{ width: rttPercent + '%' }"></div>
                </div>
                <span class="bar-value">{{ rttValue }}ms</span>
              </div>
              <div class="stat-bar-item">
                <span class="bar-label">丢包率</span>
                <div class="bar-container">
                  <div class="bar-fill danger" :style="{ width: packetLoss * 10 + '%' }"></div>
                </div>
                <span class="bar-value">{{ packetLoss }}%</span>
              </div>
              <div class="stat-bar-item">
                <span class="bar-label">心跳状态</span>
                <div class="bar-container">
                  <div class="bar-fill ok" :style="{ width: '100%' }"></div>
                </div>
                <span class="bar-value heartbeat-icon">♥</span>
              </div>
            </div>
          </div>
        </section>
      </aside>
    </div>

    <section v-else class="data-view-workspace">
      <section class="screen-panel data-waveform-panel">
        <div class="panel-heading chart-heading data-view-heading">
          <div>
            <span class="panel-kicker">CMS Vibration</span>
            <h3>{{ dataViewMode === 'realtime' ? '实时振动信号' : '报警历史振动信号' }}</h3>
          </div>
          <div class="data-mode-switch" role="tablist" aria-label="数据查看模式">
            <button
              v-for="item in dataViewModes"
              :key="item.value"
              type="button"
              :class="{ active: dataViewMode === item.value }"
              @click="dataViewMode = item.value"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div class="data-context-strip">
          <span>设备 <strong>{{ deviceInfo.deviceName || turbineName || '--' }}</strong></span>
          <span>测点 <strong>{{ cmsQueryNodeName || '--' }}</strong></span>
          <span>采样规模 <strong>{{ cmsWaveformSampleCountText }}</strong></span>
          <span>采样率 <strong>{{ cmsWaveformSampleRateText }}</strong></span>
        </div>

        <div class="data-waveform-shell">
          <div v-if="cmsWaveformLoading" class="data-waveform-empty">
            <i class="el-icon-loading"></i>
            <strong>正在读取CMS振动数据</strong>
          </div>
          <div v-else-if="!cmsWaveformAvailable" class="data-waveform-empty">
            <i class="el-icon-data-line"></i>
            <strong>{{ cmsWaveformMessage }}</strong>
            <span>{{ dataViewMode === 'realtime' ? '等待下一条30秒采样窗口' : '请选择其他报警记录重试' }}</span>
          </div>
          <template v-else>
            <div class="data-waveform-meta">
              <span>采集时间 <strong>{{ cmsWaveformMatchedTimeText }}</strong></span>
              <span v-if="dataViewMode === 'history'">报警偏差 <strong>{{ cmsWaveformOffsetText }}</strong></span>
              <span>峰峰值 <strong>{{ formatMonitorValue(cmsWaveformMetrics.peakToPeak) }}</strong></span>
              <span>RMS <strong>{{ formatMonitorValue(cmsWaveformMetrics.rms) }}</strong></span>
            </div>
            <div ref="cmsWaveformChart" class="data-waveform-chart"></div>
          </template>
        </div>
      </section>

      <aside class="screen-panel data-query-panel">
        <div class="panel-heading compact">
          <span class="panel-kicker">Waveform Query</span>
          <h3>{{ dataViewMode === 'realtime' ? '实时数据源' : '报警记录' }}</h3>
        </div>
        <div class="data-query-fields">
          <label>
            <span>当前设备</span>
            <strong>{{ deviceInfo.deviceName || turbineName || '--' }}</strong>
          </label>
          <label v-if="dataViewMode === 'history'" class="data-alarm-field">
            <span>近7天报警</span>
            <el-select
              v-model="cmsSelectedAlarmKey"
              class="data-alarm-select"
              placeholder="请选择报警记录"
              @change="loadCmsWaveform()"
            >
              <el-option
                v-for="alarm in cmsHistoryAlarmOptions"
                :key="alarm.key"
                :label="alarm.label"
                :value="alarm.key"
              ></el-option>
            </el-select>
          </label>
          <label>
            <span>测点</span>
            <strong>{{ cmsQueryNodeName || '--' }}</strong>
          </label>
          <label>
            <span>{{ dataViewMode === 'realtime' ? '数据时效' : '报警时间' }}</span>
            <strong>{{ dataViewMode === 'realtime' ? '最近45秒' : cmsSelectedAlarmTimeText }}</strong>
          </label>
          <label v-if="cmsWaveformAvailable">
            <span>匹配记录</span>
            <strong>{{ cmsWaveformMatchedTimeText }}</strong>
          </label>
          <label v-if="cmsWaveformAvailable">
            <span>测点标识</span>
            <strong>{{ cmsWaveformData.monitorPointId || '--' }}</strong>
          </label>
        </div>
        <button
          type="button"
          class="data-query-button"
          :disabled="cmsWaveformLoading || !cmsCanQuery"
          @click="loadCmsWaveform()"
        >
          <i :class="cmsWaveformLoading ? 'el-icon-loading' : 'el-icon-refresh'"></i>
          <span>{{ cmsWaveformLoading ? '读取中' : '刷新波形' }}</span>
        </button>
      </aside>
    </section>
    </template>

    <section v-else class="configuration-selection-screen">
      <header class="configuration-selection-toolbar">
        <div class="configuration-selection-title">
          <button type="button" class="config-route-back" @click="goToDeviceDetails" title="返回详情">
            <i class="el-icon-arrow-left"></i>
          </button>
          <div>
            <span>设备实例配置</span>
            <strong>{{ deviceInfo.deviceName || turbineName || '请选择设备' }}</strong>
          </div>
        </div>
        <div class="configuration-selection-selectors">
          <label>
            <span>场景</span>
            <el-select v-model="farmName" placeholder="请选择场景" @change="handleFarmChange">
              <el-option v-for="farm in farmList" :key="farm.value" :label="farm.label" :value="farm.value"></el-option>
            </el-select>
          </label>
          <label>
            <span>机型</span>
            <el-select v-model="turbineModel" placeholder="请选择机型" @change="handleModelChange">
              <el-option v-for="model in turbineModelList" :key="model.value" :label="model.label" :value="model.value"></el-option>
            </el-select>
          </label>
          <label>
            <span>设备</span>
            <el-select v-model="turbineName" placeholder="请选择设备" @change="handleTurbineChange">
              <el-option v-for="turbine in turbineList" :key="turbine.value" :label="turbine.label" :value="turbine.value"></el-option>
            </el-select>
          </label>
        </div>
      </header>

      <div class="configuration-selection-grid">
        <section class="configuration-device-panel">
          <div class="configuration-panel-heading">
            <div>
              <span>Sensing Layout</span>
              <h2>测点配置感知</h2>
            </div>
            <strong>{{ configurationPointOptions.length }} 个可选测点</strong>
          </div>
          <div class="configuration-sensing-layout">
            <div class="configuration-device-stage">
              <div class="configuration-model-stage">
                <img class="device-model-img" :src="deviceImageSrc" alt="电梯制动器测点示意图" />
                <div
                  v-for="point in imagePointBindings"
                  :key="point.id"
                  class="configuration-sensor-point"
                  :style="{ left: point.x + '%', top: point.y + '%' }"
                  :class="{
                    disabled: !point.node,
                    active: isPointActive(point),
                    related: isPointRelated(point)
                  }"
                  @mouseenter="handleImagePointMouseEnter(point)"
                  @mouseleave="handleImagePointMouseLeave"
                  @click="selectConfigurationPoint(point.node)"
                >
                  <span class="point-label">{{ point.label }}</span>
                  <span class="point-dot" :class="{ active: isPointActive(point), related: isPointRelated(point), disabled: !point.node }"></span>
                  <span class="point-ring" :class="{ active: isPointActive(point), related: isPointRelated(point), disabled: !point.node }"></span>
                </div>
              </div>
            </div>

            <div class="gbom-tree-panel configuration-gbom-panel">
              <div class="gbom-tree-title">
                <span>实例化 GBOM 树</span>
                <strong>{{ gbomNodeCount }} 节点</strong>
              </div>
              <el-scrollbar v-if="elevatorGbomTree.length" class="gbom-tree-scroll">
                <el-tree
                  :data="elevatorGbomTree"
                  node-key="id"
                  :props="gbomTreeProps"
                  :expand-on-click-node="true"
                  :default-expanded-keys="gbomExpandKeys"
                  :highlight-current="true"
                  :current-node-key="configPointId"
                  @node-click="selectConfigurationPoint"
                  @node-expand="handleGbomNodeExpand"
                >
                  <template #default="{ data }">
                    <span
                      class="gbom-tree-node"
                      :class="{
                        mapped: isNodeMappedToPoint(data),
                        active: configPointId === getNodeIdentity(data),
                        related: isNodeRelated(data)
                      }"
                      @mouseenter="handleGbomNodeMouseEnter(data)"
                      @mouseleave="handleGbomNodeMouseLeave"
                    >
                      <span v-if="isNodeMappedToPoint(data)" class="gbom-node-indicator"></span>
                      <span class="gbom-node-label">{{ getGbomNodeLabel(data) }}</span>
                    </span>
                  </template>
                </el-tree>
              </el-scrollbar>
              <div v-else class="gbom-empty">暂无该设备实例化 GBOM 树数据</div>
            </div>
          </div>
        </section>

        <aside class="configuration-point-panel">
          <div class="configuration-panel-heading compact">
            <div>
              <span>Configuration Overview</span>
              <h2>测点配置概览</h2>
            </div>
            <strong>{{ configPointId ? '已选择' : '待选择' }}</strong>
          </div>

          <div class="configuration-point-context" :class="{ empty: !configPointId }">
            <span>当前测点</span>
            <strong>{{ configPointId ? configActiveNodeName : '未选择测点' }}</strong>
            <em>{{ configPointId ? getConfigurationPointLabel(trendSelectedNode) : '从电梯图或 GBOM 树选择' }}</em>
          </div>

          <div class="configuration-overview-stats">
            <div>
              <span>状态感知模板</span>
              <p>
                <strong>{{ configRowsByType.perceived.length }}</strong>
                <em>项</em>
              </p>
            </div>
            <div>
              <span>故障诊断模板</span>
              <p>
                <strong>{{ configRowsByType.failure.length }}</strong>
                <em>项</em>
              </p>
            </div>
            <div>
              <span>运行中任务</span>
              <p>
                <strong>{{ activeTaskCount }}</strong>
                <em>项 / 共 {{ configuredTaskCount }} 项</em>
              </p>
            </div>
          </div>

          <div class="configuration-template-preview-grid">
            <section class="configuration-template-preview-section">
              <div class="configuration-template-preview-title">
                <span>状态感知模板</span>
                <strong>{{ configRowsByType.perceived.length }}</strong>
              </div>
              <div v-if="!configRowsByType.perceived.length" class="configuration-template-preview-empty">
                {{ configPointId ? '暂无状态感知模板' : '选择测点后加载' }}
              </div>
              <el-scrollbar v-else class="configuration-template-preview-list">
                <div v-for="row in configRowsByType.perceived" :key="row.__key" class="configuration-template-preview-row">
                  <div>
                    <strong>{{ row.modelName || row.templateName || ('模板 ' + (row.modelId || '--')) }}</strong>
                    <span>{{ row.algoShortname || getAlgorithmLabel(row.algoId) || '待配置算法' }}</span>
                  </div>
                  <em :class="row.__statusClass">{{ row.__statusText }}</em>
                </div>
              </el-scrollbar>
            </section>

            <section class="configuration-template-preview-section">
              <div class="configuration-template-preview-title">
                <span>故障诊断模板</span>
                <strong>{{ configRowsByType.failure.length }}</strong>
              </div>
              <div v-if="!configRowsByType.failure.length" class="configuration-template-preview-empty">
                {{ configPointId ? '暂无故障诊断模板' : '选择测点后加载' }}
              </div>
              <el-scrollbar v-else class="configuration-template-preview-list">
                <div v-for="row in configRowsByType.failure" :key="row.__key" class="configuration-template-preview-row">
                  <div>
                    <strong>{{ row.modelName || row.templateName || ('模板 ' + (row.modelId || '--')) }}</strong>
                    <span>{{ row.algoShortname || getAlgorithmLabel(row.algoId) || '待配置算法' }}</span>
                  </div>
                  <em :class="row.__statusClass">{{ row.__statusText }}</em>
                </div>
              </el-scrollbar>
            </section>
          </div>

          <div class="configuration-entry-flow">
            <div :class="{ done: !!configPointId }"><i>01</i><span>选择测点</span></div>
            <b></b>
            <div :class="{ active: !!configPointId }"><i>02</i><span>选择模板</span></div>
            <b></b>
            <div><i>03</i><span>变量与算法</span></div>
            <b></b>
            <div><i>04</i><span>保存运行</span></div>
          </div>

          <div class="configuration-selected-point">
            <span>配置入口</span>
            <strong>{{ configPointId ? configActiveNodeName : '尚未选择测点' }}</strong>
            <em v-if="configPointId">变量、任务与算法配置</em>
            <button type="button" :disabled="!configPointId" @click="openSelectedConfigWorkbench">
              配置此测点
            </button>
          </div>
        </aside>
      </div>
    </section>

    <el-dialog
      v-model="configWorkbenchVisible"
      :title="configurationOnly ? '' : '配置工作台'"
      width="86%"
      top="5vh"
      :fullscreen="configurationOnly"
      :modal="!configurationOnly"
      :show-close="!configurationOnly"
      :close-on-press-escape="!configurationOnly"
      :class="['config-workbench-dialog', { 'config-workbench-route': configurationOnly }]"
      append-to-body
      :close-on-click-modal="false"
    >
      <div class="config-dialog-body" v-loading="configWorkbenchLoading">
        <div v-if="configurationOnly" class="config-route-toolbar">
          <div class="config-route-title">
            <button type="button" class="config-route-back" @click="closeConfigurationWorkbench">
              <i class="el-icon-arrow-left"></i>
              <span>返回测点</span>
            </button>
            <div>
              <span>设备实例 / 配置中心</span>
              <strong>配置工作台</strong>
            </div>
          </div>
          <div class="config-route-selectors">
            <label>
              <span>场景</span>
              <el-select v-model="farmName" placeholder="请选择场景" @change="handleFarmChange">
                <el-option v-for="farm in farmList" :key="farm.value" :label="farm.label" :value="farm.value"></el-option>
              </el-select>
            </label>
            <label>
              <span>机型</span>
              <el-select v-model="turbineModel" placeholder="请选择机型" @change="handleModelChange">
                <el-option v-for="model in turbineModelList" :key="model.value" :label="model.label" :value="model.value"></el-option>
              </el-select>
            </label>
            <label>
              <span>设备</span>
              <el-select v-model="turbineName" placeholder="请选择设备" @change="handleTurbineChange">
                <el-option v-for="turbine in turbineList" :key="turbine.value" :label="turbine.label" :value="turbine.value"></el-option>
              </el-select>
            </label>
            <label class="config-point-select">
              <span>测点</span>
              <el-select
                v-model="configPointId"
                filterable
                placeholder="请选择测点"
                :loading="!configurationPointOptions.length && !!turbineName"
                @change="handleConfigurationPointChange"
              >
                <el-option
                  v-for="node in configurationPointOptions"
                  :key="getNodeIdentity(node)"
                  :label="getConfigurationPointLabel(node)"
                  :value="getNodeIdentity(node)"
                ></el-option>
              </el-select>
            </label>
          </div>
        </div>
        <div class="config-node-card">
          <div class="config-node-main">
            <span>当前测点</span>
            <strong>{{ configActiveNodeName }}</strong>
          </div>
          <div class="config-node-flow">
            <span>选择模板</span>
            <i></i>
            <span>配置变量</span>
            <i></i>
            <span>绑定算法</span>
            <i></i>
            <span>运行诊断</span>
          </div>
          <em>{{ configWorkbenchCountText }}</em>
        </div>

        <div class="config-main-grid">
          <aside class="config-template-panel">
            <div class="workbench-section-title">
              <span>{{ configTemplateTypeLabel }}</span>
              <button type="button" @click="startAddConfigTemplate()">
                <i class="el-icon-plus"></i>
                <span>新增模板</span>
              </button>
            </div>
            <div class="config-type-switch" role="tablist">
              <button
                v-for="item in configTemplateTypes"
                :key="item.value"
                type="button"
                :class="{ active: configTemplateType === item.value }"
                @click="switchConfigTemplateType(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
            <div class="workbench-sub-actions">
              <span>{{ configTaskRows.length }} 项</span>
              <button type="button" @click="loadConfigWorkbench">刷新</button>
            </div>
            <div v-if="!configTaskRows.length" class="config-empty config-template-empty">
              <strong>暂无{{ configTemplateTypeLabel }}</strong>
              <span>先新增模板，再为测点选择变量和算法。</span>
              <button type="button" @click="startAddConfigTemplate()">新增模板</button>
            </div>
            <el-scrollbar v-else class="config-template-scroll">
              <button
                v-for="row in configTaskRows"
                :key="row.__key"
                type="button"
                class="config-template-item"
                :class="{ active: configSelectedTaskKey === row.__key }"
                @click="selectConfigTask(row)"
              >
                <strong>{{ row.modelName || row.templateName || ('模板 ' + (row.modelId || '--')) }}</strong>
                <span>{{ row.algoShortname || getAlgorithmLabel(row.algoId) || '待选择算法' }}</span>
                <em :class="row.__statusClass">{{ row.__statusText }}</em>
              </button>
            </el-scrollbar>
          </aside>

          <section class="config-editor-panel">
            <div v-if="!configCurrentRow" class="config-empty large config-editor-empty">
              <strong>选择或新增模板后开始配置</strong>
              <span>工作台会在这里完成变量选择、算法绑定、保存和运行。</span>
              <div class="empty-workflow">
                <em>测点</em>
                <i></i>
                <em>变量</em>
                <i></i>
                <em>算法</em>
                <i></i>
                <em>运行</em>
              </div>
              <button type="button" @click="startAddConfigTemplate()">新增模板</button>
            </div>
            <template v-else>
              <div class="config-editor-head">
                <div>
                  <span>{{ configEditorModeText }}</span>
                  <strong>{{ configForm.modelName || configCurrentRow.modelName || '--' }}</strong>
                </div>
                <em :class="configSelectedRowStatusClass">{{ configSelectedRowStatusText }}</em>
              </div>

              <div class="config-workflow-mini">
                <span>测点</span>
                <i></i>
                <span>变量</span>
                <i></i>
                <span>任务</span>
                <i></i>
                <span>算法</span>
                <i></i>
                <span>运行诊断</span>
              </div>

              <div class="config-edit-layout">
                <section class="config-variable-panel">
                  <div class="workbench-section-title">
                    <span>可用感知变量</span>
                    <strong>{{ configSelectedVariableRows.length }}/{{ configVariableRows.length }}</strong>
                  </div>
                  <div v-if="!configVariableRows.length" class="config-empty">
                    当前测点没有可选感知变量
                  </div>
                  <el-checkbox-group v-else v-model="configSelectedVarKeys" class="config-variable-group">
                    <el-scrollbar class="config-variable-scroll">
                      <el-checkbox
                        v-for="variable in configVariableRows"
                        :key="getVariableKey(variable)"
                        :label="getVariableKey(variable)"
                        class="config-variable-item"
                      >
                        <span>{{ getVariableLabel(variable) }}</span>
                        <em>{{ variable.varUnit || variable.unit || variable.varType || '感知变量' }}</em>
                      </el-checkbox>
                    </el-scrollbar>
                  </el-checkbox-group>
                </section>

                <section class="config-form-panel">
                  <div class="workbench-section-title">
                    <span>算法配置</span>
                    <strong>{{ getAlgorithmLabel(configForm.algoId) || '未选择' }}</strong>
                  </div>
                  <el-form :model="configForm" label-position="top" class="config-form">
                    <div class="config-form-grid">
                      <el-form-item label="模板名称">
                        <el-input v-model="configForm.modelName" placeholder="请输入模板名称"></el-input>
                      </el-form-item>
                      <el-form-item label="服务类型">
                        <el-select
                          v-model="configForm.serviceType"
                          placeholder="请选择服务类型"
                          :disabled="configTemplateType === 'failure'"
                          @change="handleConfigServiceTypeChange"
                        >
                          <el-option label="状态评估" value="1"></el-option>
                          <el-option label="趋势预测" value="2"></el-option>
                          <el-option label="故障诊断" value="3"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item label="服务算法">
                        <el-select v-model="configForm.algoId" filterable placeholder="请选择服务算法">
                          <el-option
                            v-for="algo in configAlgoOptions"
                            :key="algo.value"
                            :label="algo.label"
                            :value="algo.value"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item label="指标要求">
                        <el-select v-model="configForm.indexRequirement">
                          <el-option label="状态指标" value="1"></el-option>
                          <el-option label="诊断指标" value="2"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item label="评估指标">
                        <el-select v-model="configForm.assessIndex">
                          <el-option label="健康度" value="1"></el-option>
                          <el-option label="异常度" value="2"></el-option>
                          <el-option label="趋势残差" value="3"></el-option>
                          <el-option label="故障类别" value="4"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item label="评估方法">
                        <el-radio-group v-model="configForm.assessType">
                          <el-radio label="1">阈值评估</el-radio>
                          <el-radio label="0">阶段评估</el-radio>
                        </el-radio-group>
                      </el-form-item>
                      <el-form-item v-if="configForm.assessType === '1'" label="阈值类型">
                        <el-select v-model="configForm.thresholdType">
                          <el-option label="专家阈值" value="1"></el-option>
                          <el-option label="自适应阈值" value="2"></el-option>
                          <el-option label="预测阈值" value="3"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item v-if="configForm.assessType === '1'" label="连续超限次数">
                        <el-input-number v-model="configForm.limitNumber" :min="1" :max="99"></el-input-number>
                      </el-form-item>
                      <el-form-item v-if="configForm.serviceType === '2'" label="故障判据">
                        <el-select v-model="configForm.failureCriterion">
                          <el-option label="残差超限" value="1"></el-option>
                          <el-option label="模型漂移" value="2"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item v-if="configForm.serviceType === '2'" label="趋势预测算法">
                        <el-select v-model="configForm.trendPrediction">
                          <el-option label="BLSTM" value="1"></el-option>
                          <el-option label="自定义" value="2"></el-option>
                        </el-select>
                      </el-form-item>
                      <el-form-item v-if="configForm.serviceType === '2'" label="预测超限次数">
                        <el-input-number v-model="configForm.resultLimitNum" :min="1" :max="99"></el-input-number>
                      </el-form-item>
                    </div>
                  </el-form>
                </section>
              </div>
            </template>
          </section>
        </div>
      </div>
      <template #footer>
        <div class="config-dialog-footer">
          <el-button @click="configurationOnly ? closeConfigurationWorkbench() : (configWorkbenchVisible = false)">
            {{ configurationOnly ? '返回测点' : '取消' }}
          </el-button>
          <el-button type="primary" :loading="configSaving" @click="saveConfigWorkbench()">保存配置</el-button>
          <el-button
            type="success"
            :loading="configRunning"
            :disabled="!configCanRunWorkbench"
            @click="saveAndRunConfigWorkbench"
          >保存并运行</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getOverviewOfFarms, getStateOfTree, getProjectId } from '@/api/sw/wel'
import { getMetaModelIdByUserId } from '@/api/diagnosis/graphVis/graphVisPro'
import {
  getAlarmList7d as getDiagnosisAlarmList7d,
  getDiagnosisRecords,
  getLatestCmsWaveform,
  getNearestCmsWaveform
} from '@/api/diagnosis/faultLocation'
import { getConfigBomTreeByNodeNameAndTurbineCode } from '@/api/sw/model3d/configBomTree'
import {
  getComponentNodes,
  getAllInstanceTreeNodes,
  reqSonNodes,
  reqAllInstanceSonNodes,
  fetchList as fetchConfigModelList,
  fetchTaskList,
  getVarsByNodeId,
  getVarsByTaskId,
  operateTask,
  reqAlgoList,
  reqAlgoList1,
  getVarsByBomNodeId,
  getVarsByModelId,
  getObj,
  getTask,
  createTemplate,
  reqAddModel,
  reqEditModel,
  reqEditTask
} from '@/api/sw/model3d/configModel/table'
import {
  fetchList as fetchVariableModelPage,
  getListByNodeId as getVariableListByNodeId
} from '@/api/sw/model3d/configPerceivedVariable/table'
import { reqTreeNodesBySceneId } from '@/api/sw/model3d/configGbomTree'
import { reqDataSet, getResultCountByTaskId, getFaultCountByTaskId, getDiagnosisFaultStats } from '@/api/diagnosis/stateAssessment/stateAssessment'

export default {
  name: 'TurbineDetail',
  props: {
    configurationOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      turbineName: '',
      farmName: '',
      turbineModel: '',
      status: '',
      nodeId: null,          // 节点ID（用于获取 configgbomtree 详情）
      sceneId: null,         // 场景ID (resume的id)
      turbineCode: null,     // 风机编码
      projectId: null,       // 项目ID（用于查询实例化GBOM树）
      currentModelProjectId: null,
      hasActivatedOnce: false,
      activeBoardView: 'fusion',
      boardViews: [
        { label: '总览大屏', value: 'fusion' },
        { label: '诊断结果', value: 'diagnosis' },
        { label: '数据查看', value: 'data' }
      ],
      dataViewMode: 'realtime',
      dataViewModes: [
        { label: '实时数据', value: 'realtime' },
        { label: '历史数据', value: 'history' }
      ],
      cmsWaveformLoading: false,
      cmsWaveformRequesting: false,
      cmsWaveformRequestId: 0,
      cmsWaveformData: null,
      cmsWaveformMessage: '等待读取CMS振动数据',
      cmsWaveformTimer: null,
      cmsSelectedAlarmKey: '',

      // 下拉框数据列表
      farmList: [],          // 风场列表
      turbineModelList: [],  // 机型列表
      turbineList: [],       // 设备列表
      treeData: [],          // 完整树形数据
      elevatorGbomTree: [],  // 实例化电梯GBOM树
      gbomExpandKeys: [],
      gbomTreeProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },

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
      alarmDetailList: [],

      // 故障类型统计
      faultTypes: [],
      totalFaultCount: 0,

      // 边缘端信息
      cpuUsage: 42,
      memoryUsage: 61,
      gpuUsage: 28,
      diskUsage: 37,
      rttValue: 23,
      rttPercent: 19,
      packetLoss: 0.2,
      edgeNodeName: 'EDGE-ELEV-A1-01',
      edgeLastUpdated: '--:--:--',
      edgeInboundRate: 18.6,
      edgeOutboundRate: 4.8,
      edgeTemperature: 46.8,
      edgeSampleRate: 128,
      edgeActiveTasks: 3,
      edgeMockTimer: null,
      edgeMockTick: 0,

      // 趋势图实时刷新控制
      trendRealtimeTimer: null,
      trendAnimationTimer: null,
      monitorClockTimer: null,
      monitorClock: Date.now(),
      monitoringFreshnessSeconds: 90,
      trendMaxPoints: 60,
      trendRevealStep: 8,
      trendHasInitialized: false,
      trendRequesting: false,
      trendLastSourceTime: '',
      trendTimeline: [],
      trendSelectedNode: null,
      trendTaskId: '',
      trendAlgoShortname: '',
      monitoringSnapshot: {
        score: null,
        threshold: null,
        anomalyFlag: null,
        latestTime: '',
        windowCount: 0,
        normalCount: 0,
        anomalyCount: 0,
        windows: []
      },
      selectedVariable: '',
      runningTaskKeys: [],
      configWorkbenchVisible: false,
      configWorkbenchLoading: false,
      configSaving: false,
      configRunning: false,
      configTemplateType: 'perceived',
      configTemplateTypes: [
        { label: '状态感知模板', value: 'perceived' },
        { label: '故障诊断模板', value: 'failure' }
      ],
      configRowsByType: {
        perceived: [],
        failure: []
      },
      configAlgoOptionsByType: {
        perceived: [],
        failure: []
      },
      configMode: 'edit',
      configTaskRows: [],
      configSelectedTaskKey: '',
      configCurrentRow: null,
      configModelNodeId: '',
      configMetaSceneId: '',
      configGbomNodeCache: {},
      configVariableRows: [],
      configSelectedVarKeys: [],
      configAlgoOptions: [],
      configForm: {
        modelId: '',
        taskId: '',
        nodeId: '',
        proId: '',
        modelName: '',
        serviceType: '1',
        indexRequirement: '1',
        assessIndex: '1',
        assessType: '1',
        thresholdType: '1',
        limitNumber: 3,
        algoId: '',
        failureCriterion: '1',
        trendPrediction: '1',
        resultLimitNum: 3,
        status: null,
        modId: 1
      },
      sensorVariableOptions: [],
      nodeTaskCandidates: [],
      monitoringTaskRows: [],
      variableTaskBindings: {},
      perceivedVarLoading: false,
      deviceImageSrc: '/img/myWel/elevator-brake-real.png',
      activeGbomNodeId: '',
      hoverGbomNodeId: '',
      configPointId: '',
      imagePointBindings: [],
      imagePointLayout: [
        {
          id: 'upper',
          x: 10.6,
          y: 63.8,
          calloutX: 12,
          calloutY: -30,
          lineWidth: 90,
          lineAngle: -63,
          calloutClass: 'top'
        },
        {
          id: 'left',
          x: 61.1,
          y: 66.2,
          calloutX: -66,
          calloutY: -20,
          lineWidth: 92,
          lineAngle: 42,
          calloutClass: 'left'
        },
        {
          id: 'center',
          x: 54.8,
          y: 22.1,
          calloutX: 61,
          calloutY: 4,
          lineWidth: 96,
          lineAngle: -26,
          calloutClass: 'right-bottom'
        },
        {
          id: 'right',
          x: 87.2,
          y: 57.8,
          calloutX: -74,
          calloutY: -18,
          lineWidth: 86,
          lineAngle: 30,
          calloutClass: 'right'
        }
      ],
      imagePointKeywordMap: {
        upper: ['抱闸', '制动臂', '线圈', '电磁铁', '上'],
        left: ['左', '制动器', '闸瓦', '制动块', '支架'],
        center: ['制动轮', '轮', '主轴', '盘', '中心'],
        right: ['右', '轴承', '支座', '端盖', '测点4']
      },
      imagePointFallbackKeywordMap: {
        upper: ['top', 'coil', 'line', 'electromagnet', 'sensor'],
        left: ['left', 'arm', 'shoe', 'clamp', 'sensor'],
        center: ['center', 'wheel', 'shaft', 'disk', 'encoder'],
        right: ['right', 'bearing', 'seat', 'cover', 'sensor4']
      }
    }
  },

  mounted() {
    // 获取路由参数
    this.turbineName = this.$route.query.turbineName || ''
    this.farmName = this.$route.query.farmName || ''
    this.turbineModel = this.$route.query.turbineModel || ''
    this.status = this.$route.query.status || ''
    this.nodeId = this.$route.query.nodeId || null  // 读取节点ID参数
    this.turbineCode = this.$route.query.turbineCode || null  // 读取turbineCode参数
    console.log(this.$route.query)

    // 加载下拉框数据
    this.loadTreeData()
    this.startMonitorClockTimer()
  },

  activated() {
    if (!this.hasActivatedOnce) {
      this.hasActivatedOnce = true
      return
    }
    if (!this.turbineCode) return
    this.loadAlarmHistory()
    this.loadFaultStats()
  },

  beforeUnmount() {
    this.stopTrendRealtimeTimer()
    this.stopMonitorClockTimer()
    this.stopEdgeMockTimer()
    this.stopCmsWaveformTimer()
    this.invalidateCmsWaveformRequest()
    this.disposeCmsWaveformChart()
  },

  computed: {
    edgeIsOnline() {
      return this.isEdgeOnlineStatus(this.status)
    },

    edgeIsAlarm() {
      const status = String(this.status || '').toLowerCase().trim()
      return ['abnormal', 'alarm', 'warning', 'fault', '报警', '异常'].includes(status)
    },

    edgeConnectionLabel() {
      if (!this.edgeIsOnline) return '边缘节点未接入'
      return this.edgeIsAlarm ? '边缘节点在线 · 报警' : '边缘节点在线'
    },

    edgeInboundPercent() {
      return Math.min(100, Math.round(this.edgeInboundRate / 40 * 100))
    },

    edgeOutboundPercent() {
      return Math.min(100, Math.round(this.edgeOutboundRate / 20 * 100))
    },

    edgeTemperaturePercent() {
      return Math.min(100, Math.round(this.edgeTemperature / 80 * 100))
    },

    edgeSamplePercent() {
      return Math.min(100, Math.round(this.edgeSampleRate / 256 * 100))
    },

    gbomNodeCount() {
      return this.getFlattenGbomNodes().length
    },

    mappedPointCount() {
      return this.imagePointBindings.filter(item => item.node).length
    },

    activeTaskCount() {
      return this.nodeTaskCandidates.filter(item => Number(item.status) === 1).length
    },

    configuredTaskCount() {
      return this.nodeTaskCandidates.length
    },

    monitoringTaskOptions() {
      const variableKeysByTaskId = new Map()
      this.sensorVariableOptions.forEach(option => {
        const taskId = String(this.variableTaskBindings[option.value]?.taskId || '')
        if (!taskId) return
        const keys = variableKeysByTaskId.get(taskId) || []
        keys.push(option.value)
        variableKeysByTaskId.set(taskId, keys)
      })

      const tasks = new Map()
      const displayRows = this.monitoringTaskRows.length ? this.monitoringTaskRows : this.nodeTaskCandidates
      displayRows.forEach((task, index) => {
        const taskId = String(task?.taskId || '')
        const modelId = String(task?.modelId || task?.configModelId || task?.modelID || '')
        const key = taskId ? `task-${taskId}` : `template-${modelId || index}`
        if (tasks.has(key)) return
        tasks.set(key, {
          key,
          taskId,
          label: task.taskName || task.modelName || task.templateName || (taskId ? `监测任务 ${taskId}` : `监测模板 ${index + 1}`),
          algoShortname: task.algoShortname || task.algoShortName || task.almodelShortName || '',
          modelId,
          bomNodeId: task.bomNodeId || task.nodeId || task.__sourceNodeId || '',
          nodeId: task.__sourceNodeId || task.nodeId || '',
          status: task.status,
          statusText: taskId ? this.getTaskStatusText(task.status) : '未生成',
          statusClass: taskId ? this.getTaskStatusClass(task.status) : 'is-idle',
          variableKeys: variableKeysByTaskId.get(taskId) || []
        })
      })

      return Array.from(tasks.values()).sort((a, b) => {
        const runningDiff = Number(Number(b.status) === 1) - Number(Number(a.status) === 1)
        return runningDiff || a.label.localeCompare(b.label)
      })
    },

    abnormalAlarmCount() {
      if (this.alarmDetailList.length) return this.alarmDetailList.length
      return this.alarmHistory.filter(item => item.type && item.type !== '无').length
    },

    selectedNodeLabel() {
      return this.trendSelectedNode ? this.getGbomNodeLabel(this.trendSelectedNode) : '未选择测点'
    },

    selectedBinding() {
      return this.variableTaskBindings[this.selectedVariable] || {}
    },

    selectedVariableName() {
      return this.getSelectedVariableDisplayName() || '未选择变量'
    },

    selectedTaskIdText() {
      return this.trendTaskId || this.selectedBinding.taskId || '--'
    },

    selectedAlgorithmName() {
      return this.trendAlgoShortname || this.selectedBinding.algoShortname || '未配置算法'
    },

    monitorLatestDate() {
      return this.parseMonitorTime(this.monitoringSnapshot.latestTime)
    },

    monitorDataAgeSeconds() {
      if (!this.monitorLatestDate) return null
      return Math.max(0, Math.floor((this.monitorClock - this.monitorLatestDate.getTime()) / 1000))
    },

    monitorIsStale() {
      return this.monitorDataAgeSeconds !== null
        && this.monitorDataAgeSeconds > this.monitoringFreshnessSeconds
    },

    monitorStatusClass() {
      if (!this.monitoringSnapshot.windowCount) return 'is-waiting'
      if (this.monitorIsStale) return 'is-stale'
      return Number(this.monitoringSnapshot.anomalyFlag) > 0 ? 'is-anomaly' : 'is-normal'
    },

    monitorStatusLabel() {
      if (!this.monitoringSnapshot.windowCount) return '等待监测数据'
      if (this.monitorIsStale) return '等待下一批数据'
      return Number(this.monitoringSnapshot.anomalyFlag) > 0 ? '检测到异常' : '监测正常'
    },

    monitorScoreText() {
      if (!this.monitoringSnapshot.windowCount) return '--'
      const value = Number(this.monitoringSnapshot.score)
      return Number.isFinite(value) ? value.toFixed(8) : '--'
    },

    monitorThresholdText() {
      if (!this.monitoringSnapshot.windowCount) return '--'
      const value = Number(this.monitoringSnapshot.threshold)
      return Number.isFinite(value) ? value.toFixed(8) : '--'
    },

    monitorLatestTimeLabel() {
      if (!this.monitorLatestDate) return '暂无窗口'
      const dateText = this.monitorLatestDate.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
      if (this.monitorDataAgeSeconds === null) return dateText
      if (this.monitorIsStale) return `${dateText}（等待更新）`
      return `${dateText}（${this.monitorDataAgeSeconds}秒前）`
    },

    monitorWindowItems() {
      const windows = Array.isArray(this.monitoringSnapshot.windows)
        ? this.monitoringSnapshot.windows.slice(-30)
        : []
      const emptyCount = Math.max(0, 30 - windows.length)
      const emptyItems = Array.from({ length: emptyCount }, (_, index) => ({
        key: `empty-${index}`,
        isEmpty: true,
        isAlarm: false,
        title: '该时间槽暂无监测数据'
      }))
      const dataItems = windows.map((item, index) => ({
        ...item,
        key: `${item.time || 'window'}-${index}`,
        isEmpty: false,
        title: `${this.formatMonitorTime(item.time)} · ${item.isAlarm ? '异常' : '正常'} · 评分 ${this.formatMonitorValue(item.metric)}`
      }))
      return [...emptyItems, ...dataItems]
    },

    configActiveNodeName() {
      const node = this.getConfigNode()
      return node ? this.getGbomNodeLabel(node) : '未选择测点'
    },

    configTemplateTypeLabel() {
      return this.configTemplateTypes.find(item => item.value === this.configTemplateType)?.label || '配置模板'
    },

    configWorkbenchCountText() {
      const perceivedCount = this.configRowsByType.perceived.length
      const failureCount = this.configRowsByType.failure.length
      return `状态 ${perceivedCount} / 诊断 ${failureCount}`
    },

    configEditorModeText() {
      if (this.configMode === 'add') return `新增${this.configTemplateTypeLabel}`
      if (this.configTemplateType === 'failure') return `编辑${this.configTemplateTypeLabel}`
      return this.configCurrentRow?.taskId ? '编辑感知任务' : '编辑状态感知模板'
    },

    configCanRunWorkbench() {
      return this.configTemplateType === 'perceived' && !!this.configCurrentRow
    },

    configSelectedVariableRows() {
      const keySet = new Set(this.configSelectedVarKeys.map(key => String(key)))
      return this.configVariableRows.filter(item => keySet.has(this.getVariableKey(item)))
    },

    configSelectedRowStatusText() {
      if (!this.configCurrentRow) return '未选择'
      return this.configCurrentRow.__statusText || this.getTaskStatusText(this.configCurrentRow.status)
    },

    configSelectedRowStatusClass() {
      if (!this.configCurrentRow) return 'is-idle'
      return this.configCurrentRow.__statusClass || this.getTaskStatusClass(this.configCurrentRow.status)
    },

    configurationPointOptions() {
      const nodes = this.getFlattenGbomNodes()
      const selectableNodes = nodes.filter(node => {
        const nodeType = String(node?.nodeType || '').toLowerCase()
        return this.getNodeIdentity(node) && nodeType !== 'root'
      })
      return selectableNodes.length ? selectableNodes : nodes
    },

    healthStatusLabel() {
      if (this.monitoringSnapshot.windowCount) return this.monitorStatusLabel
      if (this.edgeIsAlarm) return '异常关注'
      if (!this.turbineName) return '待选择'
      return this.edgeIsOnline ? '运行正常' : '未接入'
    },

    healthStatusClass() {
      if (this.monitoringSnapshot.windowCount) {
        if (this.monitorStatusClass === 'is-normal') return 'is-normal'
        if (this.monitorStatusClass === 'is-anomaly' || this.monitorStatusClass === 'is-stale') return 'is-warning'
        return 'is-idle'
      }
      if (this.edgeIsAlarm) return 'is-warning'
      if (!this.turbineName) return 'is-idle'
      return this.edgeIsOnline ? 'is-normal' : 'is-idle'
    },

    pipelineStateText() {
      if (this.trendTaskId && this.trendAlgoShortname) return '诊断链路已贯通'
      if (this.selectedVariable) return '变量已选，等待任务算法'
      if (this.activeGbomNodeId) return '测点已选，等待感知变量'
      return '等待测点配置'
    },

    boardStats() {
      return [
        { label: '监测状态', value: this.monitorStatusLabel, unit: '', tone: this.monitorStatusClass === 'is-normal' ? 'green' : (this.monitorStatusClass === 'is-anomaly' ? 'red' : 'amber'), text: true },
        { label: '当前评分', value: this.monitorScoreText, unit: '', tone: this.monitorStatusClass === 'is-anomaly' ? 'red' : 'cyan', text: true },
        { label: '监测窗口', value: this.monitoringSnapshot.windowCount, unit: '个', tone: 'green' },
        { label: '近7天报警', value: this.abnormalAlarmCount, unit: '条', tone: this.abnormalAlarmCount > 0 ? 'red' : 'green' }
      ]
    },

    workflowSteps() {
      const hasDevice = !!(this.farmName && this.turbineModel && this.turbineName)
      const hasPoint = !!this.activeGbomNodeId
      const hasMappedPoint = this.mappedPointCount > 0
      const hasVariable = !!this.selectedVariable
      const hasTask = !!this.trendTaskId
      const hasAlgorithm = !!this.trendAlgoShortname

      return [
        {
          key: 'device',
          index: '01',
          title: '场景设备',
          value: this.turbineName || '未选择设备',
          meta: `${this.farmName || '--'} / ${this.turbineModel || '--'}`,
          state: hasDevice ? 'done' : 'wait'
        },
        {
          key: 'point',
          index: '02',
          title: '测点配置',
          value: hasPoint ? this.selectedNodeLabel : `${this.mappedPointCount} 个候选测点`,
          meta: `GBOM ${this.gbomNodeCount} 节点`,
          state: hasPoint ? 'active' : (hasMappedPoint ? 'done' : 'wait')
        },
        {
          key: 'variable',
          index: '03',
          title: '感知变量',
          value: hasVariable ? this.selectedVariableName : `${this.sensorVariableOptions.length} 项变量`,
          meta: this.perceivedVarLoading ? '变量加载中' : '变量与测点绑定',
          state: hasVariable ? 'done' : (hasPoint && !this.sensorVariableOptions.length ? 'warn' : 'wait')
        },
        {
          key: 'task',
          index: '04',
          title: '任务开启',
          value: hasTask ? `任务 ${this.trendTaskId}` : `${this.configuredTaskCount} 个候选任务`,
          meta: `${this.activeTaskCount} 个运行中`,
          state: hasTask ? 'done' : (hasVariable ? 'warn' : 'wait')
        },
        {
          key: 'algorithm',
          index: '05',
          title: '算法配置',
          value: hasAlgorithm ? this.trendAlgoShortname : '未配置算法',
          meta: this.getTaskStatusText(this.selectedBinding.status),
          state: hasAlgorithm ? 'done' : (hasTask ? 'warn' : 'wait')
        },
        {
          key: 'result',
          index: '06',
          title: '数据诊断',
          value: hasTask && hasAlgorithm ? '实时曲线接入' : '等待链路贯通',
          meta: `近7天报警 ${this.abnormalAlarmCount} 条`,
          state: hasTask && hasAlgorithm ? 'active' : 'wait'
        }
      ]
    },

    monitoringTaskCards() {
      return this.monitoringTaskOptions.map(task => {
        const variableKey = task.variableKeys[0] || ''
        const binding = this.variableTaskBindings[variableKey] || {}
        const statusValue = Number(task.status)
        const taskBinding = {
          ...binding,
          taskId: task.taskId || binding.taskId || ''
        }
        const actionKey = this.getTaskActionKey(taskBinding, variableKey)
        const isRunning = statusValue === 1
        const canRun = !isRunning
          && !!task.taskId
          && !!variableKey
          && Number.isFinite(statusValue)
          && [0, 2].includes(statusValue)
          && !!(task.modelId || binding.modelId)
          && !!(task.bomNodeId || binding.bomNodeId)
        return {
          ...task,
          value: variableKey,
          taskId: task.taskId || binding.taskId || '',
          algoShortname: task.algoShortname || binding.algoShortname || '',
          modelId: task.modelId || binding.modelId || '',
          bomNodeId: task.bomNodeId || binding.bomNodeId || '',
          nodeId: task.nodeId || binding.nodeId || '',
          isRunning,
          canRun,
          actionLoading: this.runningTaskKeys.includes(actionKey),
          active: task.variableKeys.includes(this.selectedVariable)
        }
      })
    },

    cmsHistoryAlarmOptions() {
      return this.alarmDetailList.map(item => ({
        key: item.key,
        label: `${item.time} · ${item.raw?.nodeName || '未定位测点'} · ${item.title}`,
        item
      }))
    },

    cmsSelectedAlarm() {
      return this.cmsHistoryAlarmOptions.find(option => option.key === this.cmsSelectedAlarmKey)?.item || null
    },

    cmsQueryNodeName() {
      if (this.dataViewMode === 'history') {
        return this.cmsSelectedAlarm?.raw?.nodeName || ''
      }
      return this.trendSelectedNode?.nodeName
        || this.trendSelectedNode?.name
        || this.alarmDetailList[0]?.raw?.nodeName
        || ''
    },

    cmsCanQuery() {
      const hasContext = !!(this.farmName && this.turbineName && this.cmsQueryNodeName)
      return hasContext && (this.dataViewMode !== 'history' || !!this.cmsSelectedAlarm)
    },

    cmsWaveformAvailable() {
      return this.cmsWaveformData?.available === true
        && Array.isArray(this.cmsWaveformData?.values)
        && this.cmsWaveformData.values.length === 1024
    },

    cmsWaveformSampleCountText() {
      const count = Number(this.cmsWaveformData?.sampleCount)
      return `${Number.isFinite(count) && count > 0 ? count : 1024} 点/段`
    },

    cmsWaveformSampleRateText() {
      const rate = Number(this.cmsWaveformData?.sampleRateHz)
      return `${Number.isFinite(rate) && rate > 0 ? rate : 45} Hz`
    },

    cmsWaveformMatchedTimeText() {
      const date = this.parseMonitorTime(this.cmsWaveformData?.matchedTime)
      return date ? date.toLocaleString('zh-CN', { hour12: false }) : '--'
    },

    cmsSelectedAlarmTimeText() {
      return this.cmsSelectedAlarm?.time || '--'
    },

    cmsWaveformOffsetText() {
      const offset = Number(this.cmsWaveformData?.timeOffsetSeconds)
      return Number.isFinite(offset) ? `${offset.toFixed(1)} 秒` : '--'
    },

    cmsWaveformMetrics() {
      const values = this.cmsWaveformAvailable ? this.cmsWaveformData.values.map(Number) : []
      if (!values.length) return { rms: null, peakToPeak: null }
      const rms = Math.sqrt(values.reduce((sum, value) => sum + value * value, 0) / values.length)
      return {
        rms,
        peakToPeak: Math.max(...values) - Math.min(...values)
      }
    },

    topFaultTypes() {
      return this.faultTypes.slice(0, 5)
    }
  },

  watch: {
    activeBoardView(value) {
      if (value === 'fusion' && this.edgeIsOnline && !this.configurationOnly) {
        this.startEdgeMockTimer()
      } else {
        this.stopEdgeMockTimer()
      }
      if (value === 'data') {
        this.$nextTick(() => this.activateCmsWaveformView())
      } else {
        this.stopCmsWaveformTimer()
        this.invalidateCmsWaveformRequest()
        this.disposeCmsWaveformChart()
      }
      this.$nextTick(() => {
        this.resizeCharts()
      })
    },
    dataViewMode() {
      this.invalidateCmsWaveformRequest()
      this.disposeCmsWaveformChart()
      this.cmsWaveformData = null
      this.cmsWaveformMessage = '等待读取CMS振动数据'
      if (this.activeBoardView === 'data') {
        this.$nextTick(() => this.activateCmsWaveformView())
      }
    },
    status() {
      if (this.edgeIsOnline && this.activeBoardView === 'fusion' && !this.configurationOnly) {
        this.applyEdgeMockSnapshot(true)
        this.startEdgeMockTimer()
      } else {
        this.stopEdgeMockTimer()
        this.resetEdgeRuntimeData()
      }
    }
  },

  methods: {
    startMonitorClockTimer() {
      this.stopMonitorClockTimer()
      this.monitorClock = Date.now()
      this.monitorClockTimer = window.setInterval(() => {
        this.monitorClock = Date.now()
      }, 1000)
    },

    stopMonitorClockTimer() {
      if (!this.monitorClockTimer) return
      window.clearInterval(this.monitorClockTimer)
      this.monitorClockTimer = null
    },

    activateCmsWaveformView() {
      this.ensureCmsHistorySelection()
      this.loadCmsWaveform()
      if (this.dataViewMode === 'realtime') {
        this.startCmsWaveformTimer()
      } else {
        this.stopCmsWaveformTimer()
      }
    },

    ensureCmsHistorySelection() {
      if (this.dataViewMode !== 'history') return
      const stillExists = this.cmsHistoryAlarmOptions.some(option => option.key === this.cmsSelectedAlarmKey)
      if (!stillExists) {
        this.cmsSelectedAlarmKey = this.cmsHistoryAlarmOptions[0]?.key || ''
      }
    },

    startCmsWaveformTimer() {
      this.stopCmsWaveformTimer()
      if (this.activeBoardView !== 'data' || this.dataViewMode !== 'realtime') return
      this.cmsWaveformTimer = window.setInterval(() => {
        this.loadCmsWaveform({ silent: true })
      }, 10000)
    },

    stopCmsWaveformTimer() {
      if (!this.cmsWaveformTimer) return
      window.clearInterval(this.cmsWaveformTimer)
      this.cmsWaveformTimer = null
    },

    invalidateCmsWaveformRequest() {
      this.cmsWaveformRequestId += 1
      this.cmsWaveformRequesting = false
      this.cmsWaveformLoading = false
    },

    async loadCmsWaveform(options = {}) {
      if (this.cmsWaveformRequesting) return
      this.ensureCmsHistorySelection()
      if (!this.cmsCanQuery) {
        this.disposeCmsWaveformChart()
        this.cmsWaveformData = null
        this.cmsWaveformMessage = this.dataViewMode === 'history'
          ? '近7天没有可查询的报警记录'
          : '当前设备没有可查询的CMS测点'
        this.clearCmsWaveformChart()
        return
      }

      const mode = this.dataViewMode
      const alarmKey = this.cmsSelectedAlarmKey
      const request = {
        farmName: this.farmName,
        turbineName: this.turbineName,
        nodeName: this.cmsQueryNodeName,
        monitorPointId: ''
      }
      if (mode === 'history') {
        const alarmDate = this.parseAlarmTime(this.cmsSelectedAlarm?.raw?.dcTime)
        if (!alarmDate) {
          this.disposeCmsWaveformChart()
          this.cmsWaveformData = null
          this.cmsWaveformMessage = '报警时间格式无效'
          return
        }
        request.targetTime = alarmDate.toISOString()
      }

      const requestId = ++this.cmsWaveformRequestId
      this.cmsWaveformRequesting = true
      if (!options.silent) {
        this.disposeCmsWaveformChart()
      }
      this.cmsWaveformLoading = !options.silent
      try {
        const response = mode === 'history'
          ? await getNearestCmsWaveform(request, { silentError: options.silent })
          : await getLatestCmsWaveform(request, { silentError: options.silent })
        if (requestId !== this.cmsWaveformRequestId
          || mode !== this.dataViewMode
          || (mode === 'history' && alarmKey !== this.cmsSelectedAlarmKey)) return

        const payload = response?.data?.data || null
        const hasWaveform = payload?.available === true
          && Array.isArray(payload?.values)
          && payload.values.length === 1024
        if (!hasWaveform) {
          this.disposeCmsWaveformChart()
        }
        this.cmsWaveformData = payload
        this.cmsWaveformMessage = payload?.message
          || (mode === 'realtime' ? '最近45秒内没有新的CMS振动数据' : '报警时间附近没有CMS振动数据')
        this.cmsWaveformLoading = false
        await this.$nextTick()
        if (this.cmsWaveformAvailable) {
          this.renderCmsWaveformChart()
        } else {
          this.clearCmsWaveformChart()
        }
      } catch (error) {
        if (requestId !== this.cmsWaveformRequestId || mode !== this.dataViewMode) return
        this.disposeCmsWaveformChart()
        this.cmsWaveformLoading = false
        this.cmsWaveformData = null
        this.cmsWaveformMessage = 'CMS振动数据读取失败'
        this.clearCmsWaveformChart()
        if (!options.silent) {
          console.error('读取CMS振动数据失败', error)
        }
      } finally {
        if (requestId === this.cmsWaveformRequestId) {
          this.cmsWaveformRequesting = false
          this.cmsWaveformLoading = false
        }
      }
    },

    renderCmsWaveformChart() {
      const chartDom = this.$refs.cmsWaveformChart
      if (!chartDom || !this.cmsWaveformAvailable) return
      const values = this.cmsWaveformData.values.map(Number)
      const sampleRate = Number(this.cmsWaveformData.sampleRateHz) || 45
      const matchedTime = this.parseMonitorTime(this.cmsWaveformData.matchedTime)
      if (!matchedTime) return
      const endTime = matchedTime.getTime()
      const startTime = endTime - ((values.length - 1) / sampleRate * 1000)
      const seriesData = values.map((value, index) => [startTime + (index / sampleRate * 1000), value])
      const formatClock = (value, includeDate = false, includeMilliseconds = false) => {
        const date = new Date(Number(value))
        if (Number.isNaN(date.getTime())) return '--'
        const pad = (part, length = 2) => String(part).padStart(length, '0')
        const clock = `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
        const dateText = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
        const milliseconds = includeMilliseconds ? `.${pad(date.getMilliseconds(), 3)}` : ''
        return `${includeDate ? `${dateText} ` : ''}${clock}${milliseconds}`
      }
      const chart = echarts.getInstanceByDom(chartDom) || echarts.init(chartDom)
      chart.setOption({
        animation: false,
        grid: { left: 68, right: 30, top: 36, bottom: 64 },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' },
          formatter: params => {
            const point = params?.[0]?.value || []
            return `采集时间 ${formatClock(point[0], true, true)}<br/>振动值 ${this.formatMonitorValue(point[1])}`
          }
        },
        xAxis: {
          type: 'time',
          name: '采集时间',
          min: startTime,
          max: endTime,
          splitNumber: 5,
          minInterval: 1000,
          nameLocation: 'middle',
          nameGap: 34,
          axisLabel: {
            color: 'rgba(231, 248, 255, 0.66)',
            hideOverlap: true,
            formatter: value => formatClock(value)
          },
          axisLine: { lineStyle: { color: 'rgba(116, 224, 245, 0.26)' } },
          splitLine: { lineStyle: { color: 'rgba(116, 224, 245, 0.08)' } }
        },
        yAxis: {
          type: 'value',
          name: '振动幅值',
          nameTextStyle: { color: 'rgba(231, 248, 255, 0.66)' },
          axisLabel: { color: 'rgba(231, 248, 255, 0.66)' },
          axisLine: { show: true, lineStyle: { color: 'rgba(116, 224, 245, 0.26)' } },
          splitLine: { lineStyle: { color: 'rgba(116, 224, 245, 0.08)' } }
        },
        dataZoom: [
          { type: 'inside', xAxisIndex: 0 },
          {
            type: 'slider',
            xAxisIndex: 0,
            height: 18,
            bottom: 10,
            borderColor: 'rgba(116, 224, 245, 0.15)',
            backgroundColor: 'rgba(0, 13, 25, 0.48)',
            fillerColor: 'rgba(57, 215, 245, 0.16)',
            labelFormatter: value => formatClock(value),
            textStyle: { color: 'rgba(231, 248, 255, 0.58)' }
          }
        ],
        series: [{
          name: '原始振动信号',
          type: 'line',
          data: seriesData,
          showSymbol: false,
          sampling: 'lttb',
          lineStyle: { width: 1.2, color: '#39d7f5' },
          areaStyle: { color: 'rgba(57, 215, 245, 0.08)' }
        }]
      }, true)
      chart.resize()
    },

    clearCmsWaveformChart() {
      const chartDom = this.$refs.cmsWaveformChart
      const chart = chartDom ? echarts.getInstanceByDom(chartDom) : null
      if (chart) chart.clear()
    },

    disposeCmsWaveformChart() {
      const chartDom = this.$refs.cmsWaveformChart
      const chart = chartDom ? echarts.getInstanceByDom(chartDom) : null
      if (chart) chart.dispose()
    },

    resetMonitoringSnapshot() {
      this.monitoringSnapshot = {
        score: null,
        threshold: null,
        anomalyFlag: null,
        latestTime: '',
        windowCount: 0,
        normalCount: 0,
        anomalyCount: 0,
        windows: []
      }
    },

    parseMonitorTime(value) {
      if (value === undefined || value === null || value === '') return null
      if (value instanceof Date && !Number.isNaN(value.getTime())) return value
      if (typeof value === 'number') {
        const timestamp = value < 100000000000 ? value * 1000 : value
        const numberDate = new Date(timestamp)
        return Number.isNaN(numberDate.getTime()) ? null : numberDate
      }
      const text = String(value).trim()
      const normalized = text.includes('T') ? text : text.replace(' ', 'T')
      const hasTimezone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(normalized)
      // dc_time 是无时区的 MySQL DATETIME，业务写入值按北京时间生成。
      const date = new Date(hasTimezone ? normalized : `${normalized}+08:00`)
      return Number.isNaN(date.getTime()) ? null : date
    },

    formatMonitorTime(value) {
      const date = this.parseMonitorTime(value)
      if (!date) return '--'
      return date.toLocaleTimeString('zh-CN', { hour12: false })
    },

    formatMonitorValue(value) {
      const number = Number(value)
      return Number.isFinite(number) ? number.toFixed(6) : '--'
    },

    updateMonitoringSnapshot(displayPayload) {
      const windows = Array.isArray(displayPayload?.windows) ? displayPayload.windows : []
      if (!windows.length) {
        this.resetMonitoringSnapshot()
        return
      }
      const latest = windows[windows.length - 1]
      const anomalyCount = windows.filter(item => item.isAlarm).length
      this.monitoringSnapshot = {
        score: latest.metric,
        threshold: latest.threshold,
        anomalyFlag: latest.isAlarm ? 1 : 0,
        latestTime: latest.time,
        windowCount: windows.length,
        normalCount: windows.length - anomalyCount,
        anomalyCount,
        windows
      }
      this.monitorClock = Date.now()
    },

    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1)
        return
      }
      this.$router.push({ path: '/wel/index' })
    },

    getDeviceRouteQuery() {
      const query = {
        turbineName: this.turbineName || this.$route.query.turbineName,
        turbineCode: this.turbineCode || this.$route.query.turbineCode,
        farmName: this.farmName || this.$route.query.farmName,
        turbineModel: this.turbineModel || this.$route.query.turbineModel,
        projectId: this.projectId || this.currentModelProjectId || this.$route.query.projectId,
        status: this.status || this.$route.query.status,
        producer: this.$route.query.producer || this.deviceInfo.manufacturer
      }
      return Object.fromEntries(
        Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== '')
      )
    },

    goToConfigurationPage() {
      this.$router.push({
        path: '/scene/sceneInstantiation/configuration',
        query: this.getDeviceRouteQuery()
      })
    },

    goToDeviceDetails() {
      this.$router.push({
        path: '/scene/sceneInstantiation/details',
        query: this.getDeviceRouteQuery()
      })
    },

    getTaskActionKey(binding = {}, fallback = '') {
      return String(binding.taskId || binding.modelId || fallback || '')
    },

    isSuccessResponse(res) {
      const code = res?.data?.code
      return code === undefined || code === 0 || code === 200 || code === '0' || code === '200'
    },

    showTaskMessage(type, message) {
      if (!message) return
      const handler = ElMessage?.[type]
      if (typeof handler === 'function') {
        handler(message)
        return
      }
      ElMessage({ type, message })
    },

    async refreshCurrentVariableTasks() {
      if (!this.trendSelectedNode) return
      const currentValue = this.selectedVariable
      await this.loadPerceivedVariablesByNode(this.trendSelectedNode)
      if (this.sensorVariableOptions.some(item => item.value === currentValue)) {
        this.selectedVariable = currentValue
      }
    },

    async runMonitoringTask(item) {
      if (!item || item.isRunning || item.actionLoading) return
      if (!item.modelId || !item.bomNodeId) {
        this.showTaskMessage('warning', '缺少模型或测点信息，无法运行该监测任务')
        return
      }

      const actionKey = this.getTaskActionKey(item, item.value)
      if (!this.runningTaskKeys.includes(actionKey)) {
        this.runningTaskKeys = [...this.runningTaskKeys, actionKey]
      }

      this.selectedVariable = item.value
      this.activeBoardView = 'diagnosis'

      try {
        const res = await operateTask({ modelId: item.modelId, bomNodeId: item.bomNodeId })
        if (!this.isSuccessResponse(res)) {
          this.showTaskMessage('error', res?.data?.msg || '监测任务运行失败')
          return
        }

        this.showTaskMessage('success', res?.data?.msg || '监测任务已启动')
        await this.refreshCurrentVariableTasks()
        if (this.selectedVariable) {
          await this.handleVariableChange(this.selectedVariable)
        }
      } catch (error) {
        console.error('运行监测任务失败', error)
        this.showTaskMessage('error', error?.message || '监测任务运行失败')
      } finally {
        this.runningTaskKeys = this.runningTaskKeys.filter(key => key !== actionKey)
      }
    },

    getConfigNode() {
      if (this.trendSelectedNode) return this.trendSelectedNode
      const activeId = String(this.activeGbomNodeId || '')
      if (!activeId) return null
      return this.getFlattenGbomNodes().find(node => this.getNodeIdentity(node) === activeId) || null
    },

    getConfigNodeId(node = this.getConfigNode()) {
      if (!node) return ''
      return String(node.id ?? node.nodeId ?? '')
    },

    getConfigurationPointLabel(node) {
      const name = this.getGbomNodeLabel(node)
      const code = String(node?.nodeCode || '')
      return code && code !== name ? `${name} · ${code}` : name
    },

    async handleConfigurationPointChange(nodeId) {
      const node = this.configurationPointOptions.find(item => this.getNodeIdentity(item) === String(nodeId))
      if (!node) return
      await this.selectConfigurationPoint(node)
    },

    async selectConfigurationPoint(node) {
      const nodeId = this.getNodeIdentity(node)
      const selectableNode = this.configurationPointOptions.find(item => this.getNodeIdentity(item) === nodeId)
      if (!selectableNode) return
      this.configPointId = nodeId
      await this.handleGbomNodeClick(selectableNode, { isLeaf: !!selectableNode.leaf })
      await this.loadConfigWorkbench()
    },

    async openSelectedConfigWorkbench() {
      if (!this.configPointId) {
        this.showTaskMessage('warning', '请先选择测点')
        return
      }
      this.configWorkbenchVisible = true
      await this.loadConfigWorkbench()
    },

    closeConfigurationWorkbench() {
      this.configWorkbenchVisible = false
    },

    getConfigModelNodeId(node = this.getConfigNode()) {
      if (!node) return ''
      return String(
        node.nodeId
        ?? node.metaNodeId
        ?? node.modelNodeId
        ?? node.configNodeId
        ?? node.id
        ?? ''
      )
    },

    getDirectConfigModelNodeId(node = this.getConfigNode()) {
      if (!node) return ''
      const instanceNodeId = this.getConfigNodeId(node)
      const value = String(
        node.nodeId
        ?? node.metaNodeId
        ?? node.modelNodeId
        ?? node.configNodeId
        ?? node.gbomNodeId
        ?? ''
      )
      return value && value !== instanceNodeId ? value : ''
    },

    async resolveConfigMetaSceneId() {
      if (this.configMetaSceneId) return this.configMetaSceneId

      const userId = this.$store?.state?.user?.userInfo?.userId
      if (userId) {
        try {
          const res = await getMetaModelIdByUserId({ userId, userRole: 'GENERAL_USER' })
          const raw = res?.data?.data
          const sceneId = raw?.metaModelId || raw?.sceneId || raw?.id || raw
          if (this.isSuccessResponse(res) && sceneId) {
            this.configMetaSceneId = String(sceneId)
            return this.configMetaSceneId
          }
        } catch (error) {
          console.warn('config meta scene lookup failed', error)
        }
      }

      if (this.sceneId) {
        this.configMetaSceneId = String(this.sceneId)
      }
      return this.configMetaSceneId
    },

    async fetchConfigGbomNodes(nodeLevel = 99) {
      const sceneId = await this.resolveConfigMetaSceneId()
      if (!sceneId) return []

      const level = Math.max(Number(nodeLevel) || 3, 3)
      const cacheKey = `${sceneId}@@${level}`
      if (this.configGbomNodeCache.__key === cacheKey) {
        return Object.keys(this.configGbomNodeCache)
          .filter(key => key !== '__key')
          .map(key => this.configGbomNodeCache[key])
      }

      const res = await reqTreeNodesBySceneId(sceneId, level)
      if (!this.isSuccessResponse(res)) return []

      const rows = this.extractPayloadData(res)
      const list = Array.isArray(rows) ? rows : []
      const cache = { __key: cacheKey }
      list.forEach(item => {
        const nodeCode = String(item?.nodeCode || '').trim()
        if (nodeCode) cache[nodeCode] = item
      })
      this.configGbomNodeCache = cache
      return list
    },

    async resolveConfigTemplateNodeId(node = this.getConfigNode()) {
      if (!node) return ''

      const directNodeId = this.getDirectConfigModelNodeId(node)
      if (directNodeId) return directNodeId

      const nodeCode = String(node.nodeCode || '').trim()
      if (!nodeCode) return this.getConfigModelNodeId(node)

      const cachedNode = this.configGbomNodeCache[nodeCode]
      if (cachedNode) {
        return String(cachedNode.id || cachedNode.nodeId || '')
      }

      try {
        const nodeLevel = Math.max(Number(node.nodeLevel) || 3, 99)
        const gbomNodes = await this.fetchConfigGbomNodes(nodeLevel)
        const matchedNode = gbomNodes.find(item => String(item?.nodeCode || '').trim() === nodeCode)
        if (matchedNode) {
          return String(matchedNode.id || matchedNode.nodeId || '')
        }
      } catch (error) {
        console.warn('config template node lookup failed', error)
      }

      return this.getConfigModelNodeId(node)
    },

    async openConfigWorkbench() {
      const node = this.getConfigNode()
      if (!node || !this.getConfigNodeId(node)) {
        this.activeBoardView = 'flow'
        this.showTaskMessage('warning', '请先在 GBOM 树或设备测点图上选择一个测点')
        return
      }

      this.activeBoardView = 'flow'
      this.configWorkbenchVisible = true
      await this.loadConfigWorkbench()
    },

    async switchConfigTemplateType(type) {
      if (this.configTemplateType === type) return
      this.configTemplateType = type
      this.configTaskRows = this.configRowsByType[type] || []
      this.configAlgoOptions = this.configAlgoOptionsByType[type] || []
      const firstRow = this.configTaskRows[0]
      if (firstRow) {
        await this.selectConfigTask(firstRow)
      } else {
        this.resetConfigEditor()
      }
    },

    async startAddConfigTemplate(type = this.configTemplateType) {
      const node = this.getConfigNode()
      const nodeId = this.configModelNodeId || await this.resolveConfigTemplateNodeId(node)
      if (!nodeId) {
        this.showTaskMessage('warning', '请先选择一个测点')
        return
      }

      this.configModelNodeId = nodeId
      this.configTemplateType = type
      this.configTaskRows = this.configRowsByType[type] || []
      this.configAlgoOptions = this.configAlgoOptionsByType[type] || []
      this.configMode = 'add'
      this.configSelectedTaskKey = `new-${type}-${Date.now()}`
      this.configCurrentRow = {
        __key: this.configSelectedTaskKey,
        __isNew: true,
        __type: type,
        __statusText: '新增中',
        __statusClass: 'is-ready',
        modelType: type,
        nodeId
      }
      this.configSelectedVarKeys = []
      this.configForm = this.buildConfigForm({
        modelType: type,
        nodeId,
        modelName: type === 'failure' ? '故障诊断模板' : '状态感知模板',
        serviceType: type === 'failure' ? '3' : '1',
        assessIndex: type === 'failure' ? '4' : '1',
        indexRequirement: type === 'failure' ? '2' : '1',
        modId: type === 'failure' ? 4 : 1
      })
      this.configForm.algoId = this.configAlgoOptions[0]?.value || ''
    },

    resetConfigEditor() {
      this.configMode = 'edit'
      this.configCurrentRow = null
      this.configSelectedTaskKey = ''
      this.configSelectedVarKeys = []
      this.configForm = this.buildConfigForm({
        nodeId: this.configModelNodeId || this.getConfigModelNodeId(),
        modelType: this.configTemplateType
      })
    },

    extractPayloadData(res) {
      const raw = res?.data?.data
      if (Array.isArray(raw)) return raw
      if (Array.isArray(raw?.records)) return raw.records
      if (Array.isArray(raw?.list)) return raw.list
      return raw || []
    },

    async fetchConfigModelRows(modelNodeId, modelType) {
      if (!modelNodeId) return []
      const res = await fetchConfigModelList({
        nodeId: modelNodeId,
        modelType,
        pageSize: 1000,
        current: 1
      })
      if (!this.isSuccessResponse(res)) return []
      const rows = this.extractPayloadData(res)
      return Array.isArray(rows) ? rows : []
    },

    async fetchConfigTaskRows(instanceNodeId, modelNodeId, modelType = this.configTemplateType) {
      if (modelType === 'failure') {
        const failureModels = await this.fetchConfigModelRows(modelNodeId, 'failure')
        return failureModels.map(item => ({ ...item, taskId: null, __templateSource: 'model' }))
      }

      const [taskRes, modelRows] = await Promise.all([
        fetchTaskList({ nodeId: instanceNodeId, modelType: 'perceived' }).catch(error => {
          console.warn('状态感知任务读取失败', error)
          return null
        }),
        this.fetchConfigModelRows(modelNodeId, 'perceived')
      ])

      const taskRowsRaw = this.isSuccessResponse(taskRes) ? this.extractPayloadData(taskRes) : []
      const taskRows = Array.isArray(taskRowsRaw) ? taskRowsRaw : []
      const merged = []
      const modelIdSet = new Set()

      taskRows.forEach(item => {
        const modelId = String(item?.modelId || item?.configModelId || item?.modelID || '')
        if (modelId) modelIdSet.add(modelId)
        merged.push({ ...item, __templateSource: 'task' })
      })

      modelRows.forEach(item => {
        const modelId = String(item?.modelId || item?.configModelId || item?.modelID || '')
        if (modelId && modelIdSet.has(modelId)) return
        merged.push({ ...item, taskId: null, __templateSource: 'model' })
      })

      return merged
    },

    normalizeConfigRows(rows = [], modelType = this.configTemplateType) {
      return rows.map((item, index) => {
        const taskId = item?.taskId
        const hasTask = taskId !== null && taskId !== undefined && taskId !== ''
        const modelId = item?.modelId || item?.configModelId || item?.modelID || ''
        const isFailure = modelType === 'failure'
        return {
          ...item,
          modelId,
          modelType,
          __type: modelType,
          __key: `${modelType}@@${modelId || 'model'}@@${hasTask ? taskId : 'template'}@@${item?.nodeId || item?.bomNodeId || index}`,
          __hasTask: hasTask,
          __statusText: isFailure ? '诊断模板' : (hasTask ? this.getTaskStatusText(item.status) : '未生成任务'),
          __statusClass: isFailure ? 'is-ready' : (hasTask ? this.getTaskStatusClass(item.status) : 'is-idle')
        }
      }).sort((a, b) => {
        if (modelType === 'failure') return String(a.modelName || '').localeCompare(String(b.modelName || ''))
        if (a.__hasTask !== b.__hasTask) return a.__hasTask ? -1 : 1
        return Number(b.status === 1) - Number(a.status === 1)
      })
    },

    resolveConfigModelNodeId(fallbackNodeId, instanceNodeId, rows = []) {
      const fallback = String(fallbackNodeId || '')
      const instance = String(instanceNodeId || '')
      const isModelNodeId = (value) => {
        const id = String(value || '')
        return !!id && id !== instance && !id.startsWith('loading-')
      }
      const modelRow = rows.find(item => item?.__templateSource === 'model' && isModelNodeId(item?.nodeId))
      const anyModelNodeRow = rows.find(item => isModelNodeId(item?.nodeId))
      return String(modelRow?.nodeId || anyModelNodeRow?.nodeId || fallback || instance)
    },

    normalizeAlgoOptions(rawData = []) {
      const list = Array.isArray(rawData) ? rawData : []
      return list.map((item, index) => {
        const value = item?.value ?? item?.algoId ?? item?.id ?? item?.algorithmId ?? item?.almodelId ?? item?.modelId
        const label = item?.label
          || item?.algoName
          || item?.algorithmName
          || item?.almodelName
          || item?.almodelShortName
          || item?.shortName
          || item?.name
          || item?.modelName
          || value
        if (value === undefined || value === null || value === '') return null
        return {
          ...item,
          value: String(value),
          label: String(label || `算法 ${index + 1}`)
        }
      }).filter(Boolean)
    },

    getAlgorithmLabel(algoId) {
      if (algoId === undefined || algoId === null || algoId === '') return ''
      const target = String(algoId)
      const matched = this.configAlgoOptions.find(item => String(item.value) === target)
      return matched?.label || ''
    },

    async fetchConfigVariableRows(modelNodeId, instanceNodeId) {
      const loaderSpecs = []
      const addLoader = (key, loader) => {
        if (!key || loaderSpecs.some(item => item.key === key)) return
        loaderSpecs.push({ key, loader })
      }

      if (instanceNodeId) {
        addLoader(`bom:${instanceNodeId}`, async () => getVarsByBomNodeId(instanceNodeId))
      }
      if (modelNodeId) {
        addLoader(`modelPage:${modelNodeId}`, async () => fetchVariableModelPage({ nodeId: modelNodeId, pageSize: 1000, current: 1 }))
        addLoader(`modelList:${modelNodeId}`, async () => getVariableListByNodeId(modelNodeId))
        addLoader(`modelDirect:${modelNodeId}`, async () => getVarsByNodeId(modelNodeId))
      }

      const mergedRows = []
      for (const { loader } of loaderSpecs) {
        try {
          const res = await loader()
          if (!this.isSuccessResponse(res)) continue
          const rows = this.extractVariableList(res)
          const normalizedRows = this.normalizeConfigVariableRows(rows)
          if (normalizedRows.length) mergedRows.push(...normalizedRows)
        } catch (error) {
          console.warn('感知变量读取失败，继续尝试备用接口', error)
        }
      }

      return this.normalizeConfigVariableRows(mergedRows)
    },

    normalizeConfigVariableRows(rows = []) {
      const seen = new Set()
      return rows.filter(item => {
        const key = this.getVariableKey(item)
        if (!key || seen.has(key)) return false
        seen.add(key)
        return true
      })
    },

    getVariableKey(variable = {}) {
      const value = variable.varId ?? variable.id ?? variable.variableId ?? variable.varCode ?? variable.varName ?? variable.variableName ?? variable.name
      return String(value ?? '')
    },

    getVariableLabel(variable = {}) {
      return variable.varName || variable.variableName || variable.name || variable.label || this.getVariableKey(variable) || '未命名变量'
    },

    isSameVariable(a = {}, b = {}) {
      const aKey = this.getVariableKey(a)
      const bKey = this.getVariableKey(b)
      if (aKey && bKey && aKey === bKey) return true
      const aName = this.getVariableLabel(a)
      const bName = this.getVariableLabel(b)
      return !!aName && !!bName && aName === bName
    },

    syncConfigSelectedVars(selectedVars = []) {
      if (!Array.isArray(selectedVars) || !selectedVars.length) {
        this.configSelectedVarKeys = []
        return
      }

      this.configSelectedVarKeys = this.configVariableRows
        .filter(variable => selectedVars.some(selected => this.isSameVariable(variable, selected)))
        .map(variable => this.getVariableKey(variable))
        .filter(Boolean)
    },

    buildConfigForm(row = {}, detail = {}) {
      const source = { ...row, ...detail }
      const modelType = source.modelType || row.modelType || this.configTemplateType || 'perceived'
      const isFailure = modelType === 'failure'
      return {
        modelId: String(source.modelId || row.modelId || ''),
        taskId: source.taskId ?? row.taskId ?? '',
        nodeId: source.nodeId || row.nodeId || this.configModelNodeId || this.getConfigNodeId(),
        proId: source.proId || row.proId || this.currentModelProjectId || this.projectId || '',
        modelName: source.modelName || row.modelName || row.templateName || '',
        modelType,
        serviceType: String(source.serviceType || (isFailure ? '3' : '1')),
        indexRequirement: String(source.indexRequirement || (isFailure ? '2' : '1')),
        assessIndex: String(source.assessIndex || (isFailure ? '4' : '1')),
        assessType: String(source.assessType ?? '1'),
        thresholdType: String(source.thresholdType || '1'),
        limitNumber: Number(source.limitNumber || 3),
        algoId: source.algoId !== undefined && source.algoId !== null ? String(source.algoId) : '',
        failureCriterion: String(source.failureCriterion || '1'),
        trendPrediction: String(source.trendPrediction || '1'),
        resultLimitNum: Number(source.resultLimitNum || 3),
        status: source.status ?? row.status ?? null,
        modId: source.modId || (isFailure ? 4 : 1)
      }
    },

    handleConfigServiceTypeChange(value) {
      const serviceType = String(value || '1')
      this.configForm.serviceType = serviceType
      if (serviceType === '1') {
        this.configForm.indexRequirement = '1'
        this.configForm.assessIndex = '1'
        this.configForm.thresholdType = '1'
        this.configForm.modId = 1
      } else if (serviceType === '2') {
        this.configForm.indexRequirement = '1'
        this.configForm.assessIndex = '1'
        this.configForm.thresholdType = '3'
        this.configForm.failureCriterion = this.configForm.failureCriterion || '1'
        this.configForm.trendPrediction = this.configForm.trendPrediction || '1'
        this.configForm.resultLimitNum = Number(this.configForm.resultLimitNum || 3)
        this.configForm.modId = 3
      } else if (serviceType === '3') {
        this.configForm.indexRequirement = '2'
        this.configForm.assessIndex = '4'
        this.configForm.thresholdType = '1'
        this.configForm.modId = 4
      }
    },

    async loadConfigWorkbench() {
      const node = this.getConfigNode()
      const instanceNodeId = this.getConfigNodeId(node)
      const modelNodeId = await this.resolveConfigTemplateNodeId(node)
      if (!instanceNodeId || !modelNodeId) {
        this.configModelNodeId = ''
        return
      }

      this.configWorkbenchLoading = true
      try {
        const [perceivedRows, failureRows, perceivedAlgoRes, failureAlgoRes] = await Promise.all([
          this.fetchConfigTaskRows(instanceNodeId, modelNodeId, 'perceived'),
          this.fetchConfigTaskRows(instanceNodeId, modelNodeId, 'failure'),
          reqAlgoList(),
          reqAlgoList1()
        ])

        this.configRowsByType = {
          perceived: this.normalizeConfigRows(perceivedRows, 'perceived'),
          failure: this.normalizeConfigRows(failureRows, 'failure')
        }
        const resolvedModelNodeId = this.resolveConfigModelNodeId(
          modelNodeId,
          instanceNodeId,
          [...this.configRowsByType.perceived, ...this.configRowsByType.failure]
        )
        this.configModelNodeId = resolvedModelNodeId
        const variableRows = await this.fetchConfigVariableRows(resolvedModelNodeId, instanceNodeId)
        this.configVariableRows = variableRows
        this.configAlgoOptionsByType = {
          perceived: this.normalizeAlgoOptions(this.extractPayloadData(perceivedAlgoRes)),
          failure: this.normalizeAlgoOptions(this.extractPayloadData(failureAlgoRes))
        }
        this.configTaskRows = this.configRowsByType[this.configTemplateType] || []
        this.configAlgoOptions = this.configAlgoOptionsByType[this.configTemplateType] || []

        const selected = this.configTaskRows.find(item => item.__key === this.configSelectedTaskKey)
          || this.configTaskRows[0]
          || null

        if (selected) {
          await this.selectConfigTask(selected)
        } else {
          this.configCurrentRow = null
          this.configSelectedTaskKey = ''
          this.configSelectedVarKeys = []
          this.configForm = this.buildConfigForm({ nodeId: resolvedModelNodeId, modelType: this.configTemplateType })
        }
      } catch (error) {
        console.error('配置工作台加载失败', error)
        this.showTaskMessage('error', error?.message || '配置工作台加载失败')
      } finally {
        this.configWorkbenchLoading = false
      }
    },

    async selectConfigTask(row) {
      if (!row) return
      this.configMode = row.__isNew ? 'add' : 'edit'
      if (row.__type && this.configTemplateType !== row.__type) {
        this.configTemplateType = row.__type
        this.configAlgoOptions = this.configAlgoOptionsByType[row.__type] || []
      }
      this.configCurrentRow = row
      this.configSelectedTaskKey = row.__key
      this.configSelectedVarKeys = []

      try {
        const hasTask = row.taskId !== null && row.taskId !== undefined && row.taskId !== ''
        const isFailure = this.configTemplateType === 'failure'
        const [detailRes, selectedVarsRes] = await Promise.all([
          hasTask ? getTask({ taskId: row.taskId }) : getObj(row.modelId),
          isFailure
            ? Promise.resolve({ data: { code: 0, data: [] } })
            : (hasTask ? getVarsByTaskId({ taskId: row.taskId }) : getVarsByModelId(row.modelId))
        ])

        const detail = this.isSuccessResponse(detailRes) ? this.extractPayloadData(detailRes) : {}
        const selectedVars = this.extractVariableList(selectedVarsRes)
        this.configForm = this.buildConfigForm(row, Array.isArray(detail) ? detail[0] || {} : detail)
        this.configForm.algoId = this.configForm.algoId || this.configAlgoOptions[0]?.value || ''
        this.syncConfigSelectedVars(selectedVars)
      } catch (error) {
        console.error('配置模板详情读取失败', error)
        this.configForm = this.buildConfigForm(row)
        this.showTaskMessage('error', error?.message || '配置模板详情读取失败')
      }
    },

    async findGeneratedConfigTask(nodeId, modelId) {
      const modelNodeId = this.configModelNodeId || await this.resolveConfigTemplateNodeId(this.getConfigNode())
      const rows = await this.fetchConfigTaskRows(nodeId, modelNodeId, 'perceived')
      return rows.find(item => item?.taskId && String(item.modelId) === String(modelId)) || null
    },

    validateConfigWorkbench() {
      if (!this.configCurrentRow) {
        this.showTaskMessage('warning', '请先选择或新增一个模板')
        return false
      }
      if (this.configMode !== 'add' && !this.configForm.modelId) {
        this.showTaskMessage('warning', '缺少模板模型 ID，无法生成任务')
        return false
      }
      if (this.configTemplateType === 'perceived' && !this.configSelectedVariableRows.length) {
        this.showTaskMessage('warning', '请至少选择一个感知变量')
        return false
      }
      if (!this.configForm.modelName) {
        this.showTaskMessage('warning', '请填写模板名称')
        return false
      }
      if (!this.configForm.algoId) {
        this.showTaskMessage('warning', '请选择服务算法')
        return false
      }
      return true
    },

    extractModelIdFromResponse(res) {
      const data = res?.data?.data
      if (data && typeof data === 'object') {
        return data.modelId || data.id || data.configModelId || ''
      }
      return data || ''
    },

    async saveConfigWorkbench(options = {}) {
      const node = this.getConfigNode()
      const instanceNodeId = this.getConfigNodeId(node)
      const templateNodeId = this.configModelNodeId || await this.resolveConfigTemplateNodeId(node)
      if (!instanceNodeId || !templateNodeId) {
        this.showTaskMessage('warning', '请先选择一个测点')
        return false
      }
      this.configModelNodeId = templateNodeId
      if (!this.validateConfigWorkbench()) return false

      this.configSaving = true
      try {
        const isExistingPerceivedTask = this.configTemplateType === 'perceived' && !!this.configForm.taskId
        const selectedVars = this.configTemplateType === 'perceived'
          ? this.configSelectedVariableRows
          : []
        const form = {
          ...this.configForm,
          modelId: this.configForm.modelId || this.configCurrentRow.modelId,
          modelType: this.configTemplateType,
          nodeId: isExistingPerceivedTask ? instanceNodeId : templateNodeId,
          proId: this.configForm.proId || this.currentModelProjectId || this.projectId || ''
        }

        if (this.configMode === 'add' || !form.modelId) {
          const addRes = await reqAddModel(form, selectedVars)
          if (!this.isSuccessResponse(addRes)) {
            this.showTaskMessage('error', addRes?.data?.msg || '新增模板失败')
            return false
          }
          const createdModelId = this.extractModelIdFromResponse(addRes)
          if (createdModelId) form.modelId = createdModelId
          if (!form.modelId) {
            const latestModels = await this.fetchConfigModelRows(templateNodeId, this.configTemplateType)
            const matchedModel = latestModels.find(item => item?.modelName === form.modelName)
            if (matchedModel?.modelId) form.modelId = matchedModel.modelId
          }
          if (!form.modelId) {
            this.showTaskMessage('warning', '模板已保存，但未返回模板 ID，请刷新后再运行')
            return false
          }
          this.configMode = 'edit'
        } else if (this.configTemplateType === 'perceived' && form.taskId) {
          const editTaskRes = await reqEditTask(form, selectedVars)
          if (!this.isSuccessResponse(editTaskRes)) {
            this.showTaskMessage('error', editTaskRes?.data?.msg || '保存任务配置失败')
            return false
          }
        } else {
          const editModelRes = await reqEditModel(form, selectedVars)
          if (!this.isSuccessResponse(editModelRes)) {
            this.showTaskMessage('error', editModelRes?.data?.msg || '保存模板失败')
            return false
          }
        }

        if (options.generateTask && this.configTemplateType === 'perceived' && !form.taskId) {
          const createRes = await createTemplate({ modelId: form.modelId, bomNodeId: instanceNodeId })
          if (!this.isSuccessResponse(createRes)) {
            this.showTaskMessage('error', createRes?.data?.msg || '生成感知任务失败')
            return false
          }

          const taskRow = await this.findGeneratedConfigTask(instanceNodeId, form.modelId)
          if (!taskRow || !taskRow.taskId) {
            this.showTaskMessage('error', '生成任务后未找到任务实例')
            return false
          }

          form.taskId = taskRow.taskId
          form.nodeId = instanceNodeId
          form.proId = taskRow.proId || form.proId
          form.status = taskRow.status ?? 0

          const editTaskRes = await reqEditTask(form, selectedVars)
          if (!this.isSuccessResponse(editTaskRes)) {
            this.showTaskMessage('error', editTaskRes?.data?.msg || '保存任务配置失败')
            return false
          }
        }

        this.configForm = form
        if (!options.quiet) {
          this.showTaskMessage('success', '配置已保存')
        }

        await this.refreshAfterWorkbenchSave(form.taskId)
        return form
      } catch (error) {
        console.error('配置保存失败', error)
        this.showTaskMessage('error', error?.message || '配置保存失败')
        return false
      } finally {
        this.configSaving = false
      }
    },

    async saveAndRunConfigWorkbench() {
      if (this.configRunning) return
      if (this.configTemplateType !== 'perceived') {
        this.showTaskMessage('warning', '故障诊断模板暂不支持在此处直接运行')
        return
      }
      this.configRunning = true
      let actionKey = ''
      try {
        const form = await this.saveConfigWorkbench({ quiet: true, generateTask: true })
        if (!form) return

        const nodeId = this.getConfigNodeId()
        actionKey = this.getTaskActionKey(form, form.taskId)
        if (!this.runningTaskKeys.includes(actionKey)) {
          this.runningTaskKeys = [...this.runningTaskKeys, actionKey]
        }

        const res = await operateTask({ modelId: form.modelId, bomNodeId: nodeId })
        if (!this.isSuccessResponse(res)) {
          this.showTaskMessage('error', res?.data?.msg || '任务运行失败')
          return
        }

        this.showTaskMessage('success', res?.data?.msg || '任务已启动')
        if (!this.configurationOnly) {
          this.configWorkbenchVisible = false
          this.activeBoardView = 'diagnosis'
        }
        await this.refreshAfterWorkbenchSave(form.taskId, { selectTaskVariable: true })
      } catch (error) {
        console.error('保存并运行失败', error)
        this.showTaskMessage('error', error?.message || '保存并运行失败')
      } finally {
        this.configRunning = false
        if (actionKey) {
          this.runningTaskKeys = this.runningTaskKeys.filter(key => key !== actionKey)
        }
      }
    },

    async refreshAfterWorkbenchSave(taskId, options = {}) {
      const node = this.getConfigNode()
      if (node) {
        await this.loadPerceivedVariablesByNode(node)
      }

      if (taskId) {
        const taskKey = String(taskId)
        const matchedOption = this.sensorVariableOptions.find(item => String(item.value || '').endsWith(`@@${taskKey}`))
        if (matchedOption) {
          this.selectedVariable = matchedOption.value
          if (options.selectTaskVariable) {
            await this.handleVariableChange(matchedOption.value)
          }
        }
      }

      if (this.configWorkbenchVisible) {
        await this.loadConfigWorkbench()
        const matchedRow = this.configTaskRows.find(row => taskId && String(row.taskId) === String(taskId))
        if (matchedRow) {
          await this.selectConfigTask(matchedRow)
        }
      }
    },

    getTaskStatusText(status) {
      if (status === undefined || status === null || status === '') return '未启动'
      const statusValue = Number(status)
      if (statusValue === 1) return '运行中'
      if (statusValue === 0) return '已配置'
      if (statusValue === 2) return '已停止'
      return '未知状态'
    },

    getTaskStatusClass(status) {
      if (status === undefined || status === null || status === '') return 'is-idle'
      const statusValue = Number(status)
      if (statusValue === 1) return 'is-running'
      if (statusValue === 0) return 'is-ready'
      if (statusValue === 2) return 'is-stopped'
      return 'is-idle'
    },

    resizeCharts() {
      const chartDoms = [this.$refs.trendChart, this.$refs.alarmChart, this.$refs.cmsWaveformChart]
      chartDoms.forEach(dom => {
        if (!dom) return
        const chart = echarts.getInstanceByDom(dom)
        if (chart) chart.resize()
      })
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
          metaModelId: model.metaModelId || model.sceneId || model.id, // 保存ID信息
          projectId: model.id || model.metaModelId || model.sceneId
        }))

        // 如果有路由参数，使用路由参数；否则选择第一个
        if (!this.turbineModel && this.turbineModelList.length > 0) {
          this.turbineModel = this.turbineModelList[0].value
          this.currentModelProjectId = this.turbineModelList[0].projectId || null
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

      const selectedModel = this.turbineModelList.find(model => model.value === modelName)
      this.currentModelProjectId = selectedModel?.projectId || null

      try {
        const res = await getStateOfTree({
          farmName: this.farmName,
          turbineModel: modelName
        })

        const turbines = res.data.data || []
        this.turbineList = turbines.map(turbine => ({
          label: turbine.name,
          value: turbine.name,
          status: turbine.status,
          turbineCode: turbine.turbineCode
        }))

        // 如果有路由参数，使用路由参数；否则选择第一个
        if (!this.turbineName && this.turbineList.length > 0) {
          this.turbineName = this.turbineList[0].value
          this.status = this.turbineList[0].status
          this.turbineCode = this.turbineList[0].turbineCode || this.turbineCode
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
        this.turbineCode = selectedTurbine.turbineCode || null
      }
      // 重新加载所有数据
      this.loadAllData()
    },

    // 加载所有数据
    loadAllData() {
      this.stopTrendRealtimeTimer()
      this.stopEdgeMockTimer()
      this.stopCmsWaveformTimer()
      this.invalidateCmsWaveformRequest()
      this.disposeCmsWaveformChart()
      this.cmsWaveformData = null
      this.cmsWaveformMessage = '等待读取CMS振动数据'
      this.cmsSelectedAlarmKey = ''
      this.resetMonitoringSnapshot()
      this.resetTrendTimeline()
      this.trendHasInitialized = false
      this.trendSelectedNode = null
      this.trendTaskId = ''
      this.trendAlgoShortname = ''
      this.selectedVariable = ''
      this.sensorVariableOptions = []
      this.nodeTaskCandidates = []
      this.monitoringTaskRows = []
      this.variableTaskBindings = {}
      this.activeGbomNodeId = ''
      this.hoverGbomNodeId = ''
      this.configPointId = ''
      this.configModelNodeId = ''
      this.configMetaSceneId = ''
      this.configGbomNodeCache = {}
      this.configRowsByType = { perceived: [], failure: [] }
      this.configTaskRows = []
      this.configCurrentRow = null
      this.configSelectedTaskKey = ''
      this.configVariableRows = []
      this.configSelectedVarKeys = []
      this.imagePointBindings = []
      this.loadDeviceInfo()
      this.loadAlarmHistory()
      this.loadEdgeInfo()
      this.loadElevatorGbomTree().then(async () => {
        await this.loadFaultStats()
        if (!this.configurationOnly) {
          await this.initializeDefaultMonitoringContext()
        }
        if (this.activeBoardView === 'data') {
          await this.$nextTick()
          this.activateCmsWaveformView()
        }
      })

      // 渲染图表
      this.$nextTick(() => {
        this.renderNoVariableTrend()
        this.renderEmptyWeeklyCountChart()
      })
    },

    // 映射 ConfigBomTree 数据到 deviceInfo
    mapBomDataToDeviceInfo(bomData) {
      return {
        deviceName: bomData.nodeName || this.turbineName,
        deviceNo: bomData.nodeCode || bomData.turbineCode || '',
        manufacturer: bomData.manufacturer || '',
        brakeType: bomData.brakeType || '',
        ratedLoad: bomData.ratedLoad || '',
        ratedSpeed: bomData.ratedSpeed || '',
        ratedPower: bomData.ratedPower || '',
        productionDate: this.formatDate(bomData.productionDate) || '',
        useUnit: bomData.responsiblePerson || '',
        installDate: this.formatDate(bomData.installationDate) || '',
        manager: bomData.installationLeader || bomData.responsiblePerson || ''
      }
    },

    async loadElevatorGbomTree() {
      if (!this.farmName || !this.turbineModel || !this.turbineCode) {
        this.elevatorGbomTree = []
        this.gbomExpandKeys = []
        return
      }

      try {
        // 对齐状态评估逻辑：优先使用机型下拉项携带的id作为proId
        this.projectId = this.currentModelProjectId
        if (!this.projectId) {
          const projectRes = await getProjectId({ project: this.farmName, productModel: this.turbineModel })
          this.projectId = projectRes?.data?.data || null
        }
        if (!this.projectId) {
          this.elevatorGbomTree = []
          this.gbomExpandKeys = []
          return
        }

        let response = await getComponentNodes({ proId: this.projectId, nodeLevel: 3 })
        let allNodes = Array.isArray(response?.data?.data) ? response.data.data : []
        let matchedNodes = this.filterNodesBySelectedElevator(allNodes)
        if (!matchedNodes.length) {
          response = await getAllInstanceTreeNodes({ proId: this.projectId, nodeLevel: 3 })
          allNodes = Array.isArray(response?.data?.data) ? response.data.data : []
          matchedNodes = this.filterNodesBySelectedElevator(allNodes)
        }
        if (!matchedNodes.length) {
          this.elevatorGbomTree = []
          this.gbomExpandKeys = []
          return
        }

        let rootNodes = matchedNodes.filter(item => this.isRootNode(item))
        if (!rootNodes.length) {
          const levelList = matchedNodes
            .map(item => Number(item.nodeLevel))
            .filter(level => !Number.isNaN(level))
          const minLevel = levelList.length ? Math.min(...levelList) : 1
          rootNodes = matchedNodes.filter(item => Number(item.nodeLevel) === minLevel)
        }

        this.gbomExpandKeys = []
        this.elevatorGbomTree = rootNodes.map(item => {
          const rootNode = { ...item }
          this.gbomExpandKeys.push(rootNode.id)
          this.setGbomChildren(rootNode, matchedNodes)
          return rootNode
        })
        this.$nextTick(() => {
          this.refreshImagePointBindings()
        })
      } catch (error) {
        console.error('加载实例化电梯GBOM树失败', error)
        this.elevatorGbomTree = []
        this.gbomExpandKeys = []
        this.refreshImagePointBindings()
      }
    },

    setGbomChildren(parentNode, nodeList) {
      const { sonNodes, otherNodes } = this.getGbomChildrenByNodeCode(
        parentNode.nodeCode,
        nodeList,
        parentNode.id,
        parentNode.nodeId
      )
      if (!sonNodes.length) {
        if ((parentNode.nodeType === 'Mid') || (parentNode.nodeType === 'Root')) {
          parentNode.children = [{ id: `loading-${parentNode.id}`, name: '节点加载中...' }]
        }
        return
      }

      parentNode.children = sonNodes.map(item => ({ ...item }))
      parentNode.children.forEach(child => {
        this.gbomExpandKeys.push(child.id)
        this.setGbomChildren(child, otherNodes)
      })
    },

    getGbomChildrenByNodeCode(parentNodeCode, nodeList, parentNodeId, parentNodeNodeId) {
      const sonNodes = []
      const otherNodes = []
      const escapedNodeCode = String(parentNodeCode || '').replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      const regex = new RegExp(`^${escapedNodeCode}[-_/:.][^-_/:.]+$`)
      nodeList.forEach(item => {
        const parentRef = item.parentNodeId ?? item.parentId ?? item.pid ?? item.parent_id
        const byParentId = parentRef !== undefined && parentRef !== null
          && (String(parentRef) === String(parentNodeId) || String(parentRef) === String(parentNodeNodeId))
        const byNodeCode = regex.test(String(item.nodeCode || ''))

        if (byParentId || byNodeCode) {
          sonNodes.push(item)
        } else {
          otherNodes.push(item)
        }
      })
      return { sonNodes, otherNodes }
    },

    resolveNodeIdCandidates(nodeData) {
      const candidates = [nodeData?.id, nodeData?.nodeId, nodeData?.bomNodeId]
      const seen = new Set()
      return candidates.filter(id => {
        const key = String(id || '').trim()
        if (!key || seen.has(key)) return false
        seen.add(key)
        return true
      })
    },

    async handleGbomNodeExpand(data, node) {
      if (!this.hasLoadingChild(data) && node.level < 2) return
      try {
        let response = await reqSonNodes({ nodeCode: data.nodeCode, turbineCode: data.turbineCode || this.turbineCode })
        let children = Array.isArray(response?.data?.data) ? response.data.data : []
        if (!children.length) {
          response = await reqAllInstanceSonNodes({ nodeCode: data.nodeCode, turbineCode: data.turbineCode || this.turbineCode })
          children = Array.isArray(response?.data?.data) ? response.data.data : []
        }
        children.forEach(item => {
          item.children = item.leaf ? [] : [{ id: `loading-${item.id}`, name: '节点加载中...' }]
        })
        node.data.children = children
        this.refreshImagePointBindings()
      } catch (error) {
        console.error('加载GBOM子节点失败', error)
      }
    },

    async handleGbomNodeClick(data, node) {
      this.activeGbomNodeId = this.getNodeIdentity(data)
      this.trendSelectedNode = data
      this.configPointId = this.activeGbomNodeId
      await this.loadPerceivedVariablesByNode(data)

      if (!this.sensorVariableOptions.length) {
        this.stopTrendRealtimeTimer()
        this.renderNoVariableTrend('当前节点及其子节点未配置感知变量任务')
        this.renderEmptyWeeklyCountChart()
        return
      }

      await this.prepareLeafNodeTrendContext(data)
    },

    getFlattenGbomNodes() {
      const list = []
      const loop = (nodes = []) => {
        nodes.forEach(node => {
          if (!node || String(node.id || '').startsWith('loading-')) return
          list.push(node)
          if (Array.isArray(node.children) && node.children.length) {
            loop(node.children)
          }
        })
      }
      loop(this.elevatorGbomTree)
      return list
    },

    getNodeIdentity(nodeData) {
      return String(nodeData?.id || nodeData?.nodeId || '')
    },

    getGbomNodeLabel(nodeData) {
      return nodeData?.name || nodeData?.nodeName || nodeData?.nodeCode || '未命名节点'
    },

    normalizeSearchText(text) {
      return String(text || '').trim().toLowerCase()
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
        if (node.leaf) score += 1
        return { node, score }
      }).filter(item => item && item.score > 0)

      if (!scored.length) return null
      scored.sort((a, b) => b.score - a.score)
      return scored[0].node
    },

    getAvailableImageBindingNodes(allNodes = []) {
      if (!allNodes.length) return []
      const leafNodes = allNodes.filter(node => this.isGbomLeafNode(node, { isLeaf: !!node?.leaf }))
      const sourceNodes = leafNodes.length ? leafNodes : allNodes
      const measurePointHints = ['测点', '传感', 'sensor', 'temp', 'speed', 'vibration', 'current', 'voltage', 'position']
      const hintedNodes = sourceNodes.filter(node => {
        const text = this.normalizeSearchText(`${node.name || ''} ${node.nodeName || ''} ${node.nodeCode || ''}`)
        return measurePointHints.some(keyword => text.includes(keyword))
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
          label: matchedNode
            ? this.truncatePointLabel(this.getGbomNodeLabel(matchedNode))
            : '未匹配测点'
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

    isNodeMappedToPoint(nodeData) {
      const nodeId = this.getNodeIdentity(nodeData)
      if (!nodeId) return false
      return this.imagePointBindings.some(item => this.getNodeIdentity(item?.node) === nodeId)
    },

    isNodeActive(nodeData) {
      const nodeId = this.getNodeIdentity(nodeData)
      return !!nodeId && nodeId === this.activeGbomNodeId
    },

    isNodeRelated(nodeData) {
      const nodeId = this.getNodeIdentity(nodeData)
      if (!nodeId) return false
      return nodeId === this.activeGbomNodeId || nodeId === this.hoverGbomNodeId
    },

    handleGbomNodeMouseEnter(nodeData) {
      this.hoverGbomNodeId = this.getNodeIdentity(nodeData)
    },

    handleGbomNodeMouseLeave() {
      this.hoverGbomNodeId = ''
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

    async handleImagePointClick(point) {
      if (!point?.node) return
      const treeCurrentKey = this.resolveTreeCurrentKey(point.node)
      if (this.$refs.gbomTreeRef && treeCurrentKey !== undefined && treeCurrentKey !== null) {
        this.$refs.gbomTreeRef.setCurrentKey(treeCurrentKey)
      }
      await this.handleGbomNodeClick(point.node, { isLeaf: !!point.node?.leaf })
    },

    isGbomLeafNode(data, node) {
      const nodeType = String(data?.nodeType || '').toLowerCase()
      if (nodeType.includes('leaf')) return true
      if (typeof data?.leaf === 'boolean') return data.leaf
      if (typeof node?.isLeaf === 'boolean') return node.isLeaf
      return !Array.isArray(data?.children) || data.children.length === 0
    },

    async prepareLeafNodeTrendContext(nodeData) {
      try {
        this.stopTrendRealtimeTimer()

        if (!this.sensorVariableOptions.length) {
          this.selectedVariable = ''
          this.renderNoVariableTrend('当前节点及其子节点未配置感知变量任务')
          return
        }

        if (!this.selectedVariable) {
          this.selectedVariable = this.sensorVariableOptions[0].value
        }
        await this.handleVariableChange(this.selectedVariable)
      } catch (error) {
        console.error('加载特征趋势失败', error)
        this.stopTrendRealtimeTimer()
        this.renderNoVariableTrend('实时曲线读取失败')
      }
    },

    getTaskCandidateNodeIdList(nodeData) {
      const idSet = new Set(this.resolveNodeIdCandidates(nodeData).map(id => String(id)))
      const pushNodeIds = (node) => {
        this.resolveNodeIdCandidates(node).forEach(id => idSet.add(String(id)))
      }

      const loop = (nodes = []) => {
        nodes.forEach(child => {
          if (!child || String(child.id || '').startsWith('loading-')) return
          pushNodeIds(child)
          if (Array.isArray(child.children) && child.children.length) {
            loop(child.children)
          }
        })
      }

      if (Array.isArray(nodeData?.children) && nodeData.children.length) {
        loop(nodeData.children)
      }

      return Array.from(idSet)
    },

    async loadPerceivedVariablesByNode(nodeData) {
      const nodeIdList = this.getTaskCandidateNodeIdList(nodeData)
      if (!nodeIdList.length) {
        this.selectedVariable = ''
        this.sensorVariableOptions = []
        this.nodeTaskCandidates = []
        this.monitoringTaskRows = []
        this.variableTaskBindings = {}
        return
      }

      this.perceivedVarLoading = true
      try {
        this.monitoringTaskRows = []
        const mergedTaskList = []
        for (const nodeId of nodeIdList) {
          try {
            const taskRes = await fetchTaskList({ nodeId, modelType: 'perceived' })
            const taskListRaw = Array.isArray(taskRes?.data?.data) ? taskRes.data.data : []
            taskListRaw.forEach(item => {
              mergedTaskList.push({ ...item, __sourceNodeId: nodeId })
            })
          } catch (error) {
            console.warn('fetchTaskList 查询失败', nodeId, error)
          }
        }

        this.nodeTaskCandidates = mergedTaskList.filter(item => item?.taskId)

        const instanceNodeId = this.getConfigNodeId(nodeData)
        if (instanceNodeId) {
          try {
            const modelNodeId = await this.resolveConfigTemplateNodeId(nodeData)
            if (modelNodeId) {
              const configuredRows = await this.fetchConfigTaskRows(instanceNodeId, modelNodeId, 'perceived')
              this.monitoringTaskRows = Array.isArray(configuredRows) ? configuredRows : []
            }
          } catch (error) {
            console.warn('监测任务配置读取失败', error)
          }
        }

        const bindingMap = {}
        const optionList = []
        const optionKeySet = new Set()
        for (const task of this.nodeTaskCandidates) {
          try {
            const varsRes = await getVarsByTaskId({ taskId: task.taskId })
            const varList = this.extractVariableList(varsRes)
            varList.forEach(item => {
              const varName = item?.varName || item?.variableName || item?.name
              if (!varName) return
              const taskId = String(task.taskId)
              const optionValue = `${varName}@@${taskId}`
              if (optionKeySet.has(optionValue)) return
              optionKeySet.add(optionValue)

              const modelName = task.modelName || task.templateName || `任务${taskId}`
              optionList.push({
                label: `${varName} (${modelName})`,
                value: optionValue
              })

              bindingMap[optionValue] = {
                varName: String(varName),
                taskId: task.taskId,
                modelId: task.modelId || task.configModelId || task.modelID || '',
                bomNodeId: task.bomNodeId || task.nodeId || task.__sourceNodeId || '',
                algoShortname: task.algoShortname || task.algoShortName || task.almodelShortName || '',
                status: task.status,
                nodeId: task.__sourceNodeId
              }
            })
          } catch (error) {
            console.warn('getVarsByTaskId 查询失败', task.taskId, error)
          }
        }

        this.variableTaskBindings = bindingMap
        this.sensorVariableOptions = optionList
        const selectedStillExists = this.sensorVariableOptions.some(item => item.value === this.selectedVariable)
        this.selectedVariable = selectedStillExists
          ? this.selectedVariable
          : (this.sensorVariableOptions[0]?.value || '')
      } catch (e) {
        console.warn('按子节点任务读取感知变量失败', e)
        this.selectedVariable = ''
        this.sensorVariableOptions = []
        this.nodeTaskCandidates = []
        this.monitoringTaskRows = []
        this.variableTaskBindings = {}
      } finally {
        this.perceivedVarLoading = false
      }
    },

    async selectPerceivedVariable(variableKey) {
      if (!variableKey || this.selectedVariable === variableKey) return
      this.selectedVariable = variableKey
      await this.handleVariableChange(variableKey)
    },

    async initializeDefaultMonitoringContext() {
      const nodes = this.getFlattenGbomNodes()
      if (!nodes.length || this.trendTaskId) return

      const pointOne = nodes.find(node => /测点\s*1|point\s*1/i.test(this.getGbomNodeLabel(node)))
      const firstLeaf = nodes.find(node => {
        const children = Array.isArray(node?.children) ? node.children : []
        return node?.leaf === true || (!children.length && !String(node?.id || '').startsWith('loading-'))
      })
      const defaultNode = pointOne || firstLeaf || nodes[0]
      if (!defaultNode) return

      const treeCurrentKey = this.resolveTreeCurrentKey(defaultNode)
      this.$nextTick(() => {
        if (this.$refs.gbomTreeRef && treeCurrentKey !== undefined && treeCurrentKey !== null) {
          this.$refs.gbomTreeRef.setCurrentKey(treeCurrentKey)
        }
      })
      await this.handleGbomNodeClick(defaultNode, { isLeaf: !!defaultNode.leaf })
    },

    async selectMonitoringTask(task) {
      if (!task?.taskId) {
        this.showTaskMessage('warning', '该监测任务尚未生成实例，请前往配置页生成任务')
        return
      }
      const variableKey = task?.variableKeys?.[0]
      if (!variableKey) {
        this.showTaskMessage('warning', '该监测任务尚未绑定可用感知变量')
        return
      }
      await this.selectPerceivedVariable(variableKey)
    },

    extractVariableList(res) {
      const raw = res?.data?.data
      if (Array.isArray(raw)) return raw
      if (Array.isArray(raw?.records)) return raw.records
      if (Array.isArray(raw?.list)) return raw.list
      return []
    },

    getSelectedVariableDisplayName() {
      const binding = this.variableTaskBindings[this.selectedVariable]
      if (binding?.varName) return binding.varName
      const raw = String(this.selectedVariable || '')
      if (!raw) return ''
      const splitIndex = raw.lastIndexOf('@@')
      return splitIndex > -1 ? raw.slice(0, splitIndex) : raw
    },

    async handleVariableChange(variableKey) {
      if (!variableKey) {
        this.stopTrendRealtimeTimer()
        this.resetMonitoringSnapshot()
        this.resetTrendTimeline()
        this.renderNoVariableTrend('请选择感知变量')
        this.renderEmptyWeeklyCountChart()
        return
      }

      const binding = this.variableTaskBindings[variableKey]
      const taskId = binding?.taskId
      const algoShortname = binding?.algoShortname

      if (!taskId || !algoShortname) {
        this.stopTrendRealtimeTimer()
        this.resetMonitoringSnapshot()
        this.resetTrendTimeline()
        this.renderNoVariableTrend('该变量无可用感知变量任务')
        this.renderEmptyWeeklyCountChart()
        return
      }

      this.trendTaskId = taskId
      this.trendAlgoShortname = algoShortname
      this.resetMonitoringSnapshot()
      this.resetTrendTimeline()
      this.trendHasInitialized = false

      await this.fetchAndRenderWeeklyStats(taskId)
      await this.fetchAndRenderRealtimeTrend()
      this.startTrendRealtimeTimer()
    },

    buildRecent7DayLabels() {
      const labels = []
      const now = new Date()
      for (let i = 6; i >= 0; i -= 1) {
        const day = new Date(now)
        day.setDate(now.getDate() - i)
        labels.push(`${day.getMonth() + 1}/${day.getDate()}`)
      }
      return labels
    },

    normalizeWeeklyCountPayload(rawData, defaultCount = 0) {
      const labels = this.buildRecent7DayLabels()
      const countMap = {}

      if (Array.isArray(rawData)) {
        rawData.forEach((item, index) => {
          if (typeof item === 'number') {
            if (index < labels.length) {
              countMap[labels[index]] = item
            }
            return
          }

          if (!item || typeof item !== 'object') return
          const dateText = String(item.time || item.date || item.day || item.statDate || item.dcDate || '')
          const dateObj = dateText ? new Date(dateText.includes('T') ? dateText : dateText.replace(/-/g, '/')) : null
          const labelKey = dateObj && !Number.isNaN(dateObj.getTime())
            ? `${dateObj.getMonth() + 1}/${dateObj.getDate()}`
            : (item.label || '')
          const countVal = Number(item.count ?? item.resultCount ?? item.total ?? item.value ?? 0)
          if (labelKey && Number.isFinite(countVal)) {
            countMap[labelKey] = countVal
          }
        })
      } else if (rawData && typeof rawData === 'object') {
        Object.keys(rawData).forEach((dateKey) => {
          const dateObj = new Date(String(dateKey).includes('T') ? dateKey : String(dateKey).replace(/-/g, '/'))
          if (Number.isNaN(dateObj.getTime())) return
          const labelKey = `${dateObj.getMonth() + 1}/${dateObj.getDate()}`
          const countVal = Number(rawData[dateKey])
          if (Number.isFinite(countVal)) {
            countMap[labelKey] = countVal
          }
        })
      }

      return {
        labels,
        counts: labels.map(label => {
          const value = Number(countMap[label])
          return Number.isFinite(value) && value > 0 ? value : defaultCount
        })
      }
    },

    async fetchAndRenderWeeklyStats(taskId) {
      if (!taskId) {
        this.renderEmptyWeeklyCountChart()
        return
      }

      try {
        const [resultRes, faultRes] = await Promise.all([
          getResultCountByTaskId(taskId),
          getFaultCountByTaskId(taskId)
        ])

        if (resultRes?.data?.code !== 0 && faultRes?.data?.code !== 0) {
          this.renderEmptyWeeklyCountChart()
          return
        }

        const resultNormalized = this.normalizeWeeklyCountPayload(resultRes?.data?.data, 10000)
        const faultNormalized = this.normalizeWeeklyCountPayload(faultRes?.data?.data, 0)
        this.renderAlarmChart({
          labels: resultNormalized.labels,
          counts: resultNormalized.counts,
          faultCounts: faultNormalized.counts
        })
      } catch (error) {
        console.error('读取近7天统计数据失败', error)
        this.renderEmptyWeeklyCountChart()
      }
    },

    renderEmptyWeeklyCountChart() {
      const labels = this.buildRecent7DayLabels()
      this.renderAlarmChart({ labels, counts: [], faultCounts: [] })
    },

    startTrendRealtimeTimer() {
      this.stopTrendRealtimeTimer()
      this.trendRealtimeTimer = setInterval(() => {
        this.fetchAndRenderRealtimeTrend()
      }, 5000)
    },

    stopTrendRealtimeTimer() {
      if (this.trendRealtimeTimer) {
        clearInterval(this.trendRealtimeTimer)
        this.trendRealtimeTimer = null
      }
      this.stopTrendAnimationTimer()
    },

    stopTrendAnimationTimer() {
      if (this.trendAnimationTimer) {
        clearInterval(this.trendAnimationTimer)
        this.trendAnimationTimer = null
      }
    },

    formatTrendAxisLabel(value) {
      const date = this.parseMonitorTime(value)
      if (date) {
        const year = date.getFullYear()
        const month = date.getMonth() + 1
        const day = date.getDate()
        const hour = String(date.getHours()).padStart(2, '0')
        const minute = String(date.getMinutes()).padStart(2, '0')
        const second = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day}\n${hour}:${minute}:${second}`
      }
      return String(value || '')
    },

    buildTrendDisplayPayload(payload = {}) {
      const xDataRaw = Array.isArray(payload.xData) ? payload.xData : []
      const metricDataRaw = Array.isArray(payload.metricData) ? payload.metricData : []
      const thresholdDataRaw = Array.isArray(payload.thresholdData) ? payload.thresholdData : []
      const alarmFlagRaw = Array.isArray(payload.alarmFlagData) ? payload.alarmFlagData : []
      const metricLegend = payload.metricLegend || '评估指标'

      const minLen = Math.min(xDataRaw.length, metricDataRaw.length)
      if (!minLen) return null

      const merged = []
      for (let i = 0; i < minLen; i += 1) {
        const metric = Number(metricDataRaw[i])
        if (!Number.isFinite(metric)) continue
        const threshold = Number(thresholdDataRaw[i])
        const alarmFlag = Number(alarmFlagRaw[i])
        merged.push({
          time: xDataRaw[i],
          metric,
          threshold: Number.isFinite(threshold) ? threshold : null,
          alarmFlag: Number.isFinite(alarmFlag) ? alarmFlag : 0
        })
      }

      if (!merged.length) return null

      const hasExplicitAlarm = merged.some(item => item.alarmFlag > 0)
      merged.forEach((item, index) => {
        item.sourceIndex = index
        item.isAlarm = hasExplicitAlarm
          ? item.alarmFlag > 0
          : (typeof item.threshold === 'number' && item.metric >= item.threshold)
      })

      const maxPoints = Math.max(20, Number(this.trendMaxPoints) || 80)
      const sampleStep = Math.max(1, Math.ceil(merged.length / maxPoints))
      const sampledIndexSet = new Set()
      for (let i = 0; i < merged.length; i += sampleStep) {
        sampledIndexSet.add(i)
      }
      sampledIndexSet.add(merged.length - 1)

      // 报警点本身及相邻点不能被抽样丢掉，否则图例有“报警”但曲线上看不到。
      merged.forEach((item, index) => {
        if (!item.isAlarm) return
        if (index > 0) sampledIndexSet.add(index - 1)
        sampledIndexSet.add(index)
        if (index < merged.length - 1) sampledIndexSet.add(index + 1)
      })

      const sampled = Array.from(sampledIndexSet)
        .sort((a, b) => a - b)
        .map(index => merged[index])
      const windowed = sampled.slice(-Math.max(maxPoints, 120))
      const alarmData = windowed.map(item => (item.isAlarm ? item.metric : null))

      return {
        metricLegend,
        windows: windowed,
        xData: windowed.map(item => item.time),
        metricData: windowed.map(item => item.metric),
        thresholdData: windowed.map(item => item.threshold),
        alarmData
      }
    },

    resetTrendTimeline() {
      this.trendLastSourceTime = ''
      this.trendTimeline = []
      this.trendRequesting = false
    },

    getTrendTimeKey(value) {
      const date = this.parseMonitorTime(value)
      return date ? String(date.getTime()) : String(value || '')
    },

    buildTrendTimelinePayload(metricLegend = '异常评分 (rms_hi)') {
      const maxPoints = Math.max(20, Number(this.trendMaxPoints) || 60)
      const windows = this.trendTimeline.slice(-maxPoints)
      return {
        metricLegend,
        windows,
        xData: windows.map(item => item.time),
        metricData: windows.map(item => item.metric),
        thresholdData: windows.map(item => item.threshold),
        alarmData: windows.map(item => (item.isAlarm ? item.metric : null))
      }
    },

    syncTrendTimeline(displayPayload) {
      const sourceWindows = Array.isArray(displayPayload?.windows) ? displayPayload.windows : []
      const metricLegend = displayPayload?.metricLegend || '异常评分 (rms_hi)'
      if (!sourceWindows.length) return null

      const latestSource = sourceWindows[sourceWindows.length - 1]
      const latestSourceKey = this.getTrendTimeKey(latestSource.time)
      let newWindows = []

      if (!this.trendLastSourceTime) {
        newWindows = sourceWindows
      } else if (latestSourceKey !== this.trendLastSourceTime) {
        const previousIndex = sourceWindows.findIndex(item => (
          this.getTrendTimeKey(item.time) === this.trendLastSourceTime
        ))
        newWindows = previousIndex >= 0 ? sourceWindows.slice(previousIndex + 1) : [latestSource]
      }

      if (!newWindows.length) return null

      const existingKeys = new Set(this.trendTimeline.map(item => this.getTrendTimeKey(item.time)))
      newWindows.forEach(item => {
        const timeKey = this.getTrendTimeKey(item.time)
        if (!timeKey || existingKeys.has(timeKey)) return
        this.trendTimeline.push({ ...item })
        existingKeys.add(timeKey)
      })
      this.trendLastSourceTime = latestSourceKey

      const maxPoints = Math.max(20, Number(this.trendMaxPoints) || 60)
      this.trendTimeline = this.trendTimeline.slice(-maxPoints)
      return this.buildTrendTimelinePayload(metricLegend)
    },

    keepCurrentTrend() {
      this.stopTrendAnimationTimer()
      if (!this.trendHasInitialized || !this.trendTimeline.length) {
        this.renderNoVariableTrend('等待实时监测数据')
      }
    },

    playTrendAnimation(displayPayload) {
      this.stopTrendAnimationTimer()
      const total = Array.isArray(displayPayload?.xData) ? displayPayload.xData.length : 0
      if (!total) {
        this.renderNoVariableTrend('暂无实时数据')
        return
      }

      const step = Math.max(2, Number(this.trendRevealStep) || 6)
      let visibleCount = Math.min(step, total)
      this.renderTrendChart(displayPayload, visibleCount)

      if (visibleCount >= total) return

      this.trendAnimationTimer = setInterval(() => {
        visibleCount = Math.min(visibleCount + step, total)
        this.renderTrendChart(displayPayload, visibleCount)
        if (visibleCount >= total) {
          this.stopTrendAnimationTimer()
        }
      }, 200)
    },

    async fetchAndRenderRealtimeTrend() {
      if (!this.trendTaskId || !this.trendAlgoShortname) {
        this.resetMonitoringSnapshot()
        this.resetTrendTimeline()
        this.renderNoVariableTrend('该节点无可用状态评估任务')
        return
      }

      if (this.trendRequesting) return
      this.trendRequesting = true

      try {
        const res = await reqDataSet(this.trendTaskId, this.trendAlgoShortname)
        if (res?.data?.code !== 0 || !Array.isArray(res?.data?.data) || !res.data.data.length) {
          this.keepCurrentTrend()
          return
        }

        const first = res.data.data[0] || {}
        const xData = Array.isArray(first.dcTimeList) ? first.dcTimeList : []
        const metricData = Array.isArray(first.dc_dataList) ? first.dc_dataList : []
        const thresholdData = Array.isArray(first.thresholdList) ? first.thresholdList : []
        const alarmFlagData = Array.isArray(first.anomaly_flagList) ? first.anomaly_flagList : []

        if (!xData.length || !metricData.length) {
          this.keepCurrentTrend()
          return
        }

        const displayPayload = this.buildTrendDisplayPayload({
          xData,
          metricData,
          thresholdData,
          alarmFlagData,
          metricLegend: '异常评分 (rms_hi)'
        })
        if (!displayPayload) {
          this.keepCurrentTrend()
          return
        }

        const timelinePayload = this.syncTrendTimeline(displayPayload)
        if (!timelinePayload) return

        this.updateMonitoringSnapshot(displayPayload)

        if (!this.trendHasInitialized) {
          this.playTrendAnimation(timelinePayload)
          this.trendHasInitialized = true
        } else {
          this.stopTrendAnimationTimer()
          this.renderTrendChart(timelinePayload)
        }
      } catch (error) {
        console.error('读取实时趋势数据失败', error)
        this.keepCurrentTrend()
      } finally {
        this.trendRequesting = false
      }
    },

    hasLoadingChild(node) {
      const children = node?.children || []
      return children.some(item => String(item.id).startsWith('loading'))
    },

    isRootNode(node) {
      const nodeType = String(node.nodeType || '').toLowerCase()
      if (nodeType.includes('root')) return true
      if (Number(node.nodeLevel) === 1) return true
      const nodeCode = String(node.nodeCode || '')
      return nodeCode !== '' && !nodeCode.includes('-')
    },

    normalizeCompareText(text) {
      return String(text || '')
        .trim()
        .toLowerCase()
        .replace(/\s+/g, '')
        .replace(/[＃#]/g, '#')
    },

    filterNodesBySelectedElevator(allNodes) {
      const selectedCode = this.normalizeCompareText(this.turbineCode)
      const selectedName = this.normalizeCompareText(this.turbineName)

      // 1) 优先按turbineCode精确归一化匹配
      let matched = allNodes.filter(item => this.normalizeCompareText(item.turbineCode) === selectedCode)
      if (matched.length) return matched

      // 2) 兼容编码前缀/后缀差异（包含匹配）
      matched = allNodes.filter(item => {
        const code = this.normalizeCompareText(item.turbineCode)
        return code && selectedCode && (code.includes(selectedCode) || selectedCode.includes(code))
      })
      if (matched.length) return matched

      // 3) 按根节点名称兜底匹配，再按其turbineCode归并全树节点
      const roots = allNodes.filter(item => this.isRootNode(item))
      const matchedRoots = roots.filter(item => {
        const rootName = this.normalizeCompareText(item.name || item.nodeName)
        return rootName && selectedName && (rootName.includes(selectedName) || selectedName.includes(rootName))
      })

      if (!matchedRoots.length) return []

      const codes = new Set(matchedRoots.map(item => item.turbineCode).filter(Boolean))
      if (!codes.size) {
        return allNodes.filter(item => {
          const name = this.normalizeCompareText(item.name || item.nodeName)
          return name && selectedName && (name.includes(selectedName) || selectedName.includes(name))
        })
      }

      return allNodes.filter(item => codes.has(item.turbineCode))
    },

    // 加载设备基本信息（从 configbomtree 获取）
    async loadDeviceInfo() {
      if (!this.turbineName || !this.turbineCode) {
        console.warn('缺少必要参数：turbineName 或 turbineCode')
        this.setDefaultDeviceInfo()
        return
      }

      try {
        console.log('查询设备信息', { nodeName: this.turbineName, turbineCode: this.turbineCode })
        const res = await getConfigBomTreeByNodeNameAndTurbineCode(this.turbineName, this.turbineCode)

        if (res?.data) {
          this.deviceInfo = this.mapBomDataToDeviceInfo(res.data.data || res.data)
          console.log('✓ 设备信息加载成功')
          return
        }

        console.warn('未获取到设备信息')
        this.setDefaultDeviceInfo()
      } catch (error) {
        console.error('获取设备信息失败', error.message)
        this.setDefaultDeviceInfo()
      }
    },

    // 日期格式化辅助函数
    formatDate(dateString) {
      if (!dateString) return ''
      // 如果已是 YYYY-MM-DD 格式则直接返回
      if (typeof dateString === 'string' && dateString.includes('-')) {
        return dateString
      }
      // 如果是Date对象或毫秒数则转换
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) return ''
        return date.toISOString().split('T')[0]
      } catch (e) {
        return ''
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

    async resolveSelectedProjectId() {
      if (this.currentModelProjectId) {
        return this.currentModelProjectId
      }
      if (!this.farmName || !this.turbineModel) {
        return null
      }
      const projectRes = await getProjectId({
        project: this.farmName,
        productModel: this.turbineModel
      })
      return projectRes?.data?.data || null
    },

    parseAlarmTime(timeText) {
      if (!timeText) return null
      const numericValue = Number(timeText)
      if (Number.isFinite(numericValue)) {
        const numericDate = new Date(numericValue)
        if (!Number.isNaN(numericDate.getTime())) {
          return numericDate
        }
      }
      const text = String(timeText).trim()
      const normalized = text.includes('T') ? text : text.replace(' ', 'T')
      const hasTimezone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(normalized)
      const date = new Date(hasTimezone ? normalized : `${normalized}+08:00`)
      return Number.isNaN(date.getTime()) ? null : date
    },

    formatAlarmTime(timeText) {
      const date = this.parseAlarmTime(timeText)
      if (!date) return String(timeText || '')
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    },

    formatDayKey(dateInput) {
      const date = dateInput instanceof Date ? dateInput : this.parseAlarmTime(dateInput)
      if (!date) return ''
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    formatDayStartTime(dateInput) {
      const date = dateInput instanceof Date ? dateInput : this.parseAlarmTime(dateInput)
      if (!date) return ''
      return `${this.formatDayKey(date)} 00:00`
    },

    isAlarmWithinRecent7Days(timeText) {
      const date = this.parseAlarmTime(timeText)
      if (!date) return false
      const todayStart = new Date()
      todayStart.setHours(0, 0, 0, 0)
      const start = new Date(todayStart)
      start.setDate(todayStart.getDate() - 6)
      return date >= start
    },

    isTodayAlarm(timeText) {
      const date = this.parseAlarmTime(timeText)
      if (!date) return false
      const now = new Date()
      return date.getFullYear() === now.getFullYear()
        && date.getMonth() === now.getMonth()
        && date.getDate() === now.getDate()
    },

    isAlarmForSelectedTurbine(item) {
      const selectedCode = this.normalizeCompareText(this.turbineCode)
      const selectedName = this.normalizeCompareText(this.turbineName)
      const alarmCode = this.normalizeCompareText(item?.turbineCode)
      const alarmName = this.normalizeCompareText(item?.turbineName)

      if (selectedCode && alarmCode && (alarmCode === selectedCode || alarmCode.includes(selectedCode) || selectedCode.includes(alarmCode))) {
        return true
      }
      if (selectedName && alarmName && (alarmName === selectedName || alarmName.includes(selectedName) || selectedName.includes(alarmName))) {
        return true
      }
      return false
    },

    buildAlarmHistoryRow(item) {
      return {
        time: this.formatAlarmTime(item.dcTime),
        type: this.getAlarmProblemText(item),
        resolved: this.getAlarmResolvedText(item)
      }
    },

    buildNoAlarmHistoryRow(dateInput) {
      return {
        time: this.formatDayStartTime(dateInput),
        type: '无',
        resolved: '否'
      }
    },

    getAlarmProblemText(item = {}) {
      return item.alarmName
        || item.faultName
        || item.errorStatus
        || item.statusName
        || item.alarmType
        || item.type
        || `${item.turbineName || this.turbineName || '设备'}报警`
    },

    getAlarmResolvedText(item = {}) {
      const raw = item.resolved ?? item.isResolved ?? item.handleStatus ?? item.status
      if (raw === true || raw === 1 || raw === '1' || raw === 'resolved' || raw === '已解决') return '是'
      return '否'
    },

    getAlarmStatusClass(item = {}) {
      const text = String(this.getAlarmResolvedText(item))
      if (text === '是') return 'is-resolved'
      return 'is-active'
    },

    findDiagnosisRecordForAlarm(item = {}, records = []) {
      const alarmTaskId = String(item.taskId || '')
      const alarmNodeId = String(item.nodeId || '')
      const alarmTime = this.parseAlarmTime(item.dcTime || item.alarmTime || item.time)?.getTime()
      if (!alarmTaskId || !alarmNodeId || !Number.isFinite(alarmTime)) return null

      const matched = records.filter(record => {
        const recordTime = this.parseAlarmTime(record?.alarmTime)?.getTime()
        return String(record?.alarmTaskId || '') === alarmTaskId
          && String(record?.nodeId || '') === alarmNodeId
          && Number.isFinite(recordTime)
          && Math.abs(recordTime - alarmTime) <= 1500
      })
      return matched.find(record => Number(record?.diagnosisStatus) === 2)
        || matched.find(record => Number(record?.diagnosisStatus) === 1)
        || matched[0]
        || null
    },

    buildAlarmDetailRow(item = {}, index = 0, diagnosisRecords = []) {
      const timeText = this.formatAlarmTime(item.dcTime || item.alarmTime || item.time)
      const turbineText = item.turbineName || this.turbineName || '--'
      const nodeText = item.nodeName || item.partName || item.componentName || item.location || '未定位部件'
      const modelText = item.modelName || item.taskName || item.algoShortname || item.algoName || '未关联任务'
      const diagnosisRecord = this.findDiagnosisRecordForAlarm(item, diagnosisRecords)
      const diagnosisStatus = Number(diagnosisRecord?.diagnosisStatus || 0)
      return {
        key: `${item.id || item.alarmId || item.dcTime || 'alarm'}-${index}`,
        time: timeText || '--',
        title: this.getAlarmProblemText(item),
        turbineName: turbineText,
        nodeName: `${turbineText} / ${nodeText}`,
        modelName: modelText,
        statusText: this.getAlarmResolvedText(item) === '是' ? '已处理' : '报警中',
        statusClass: this.getAlarmStatusClass(item),
        diagnosisRecord,
        diagnosisCompleted: diagnosisStatus === 2,
        diagnosisProcessing: diagnosisStatus === 1,
        diagnosisActionText: diagnosisStatus === 2 ? '查看诊断' : (diagnosisStatus === 1 ? '诊断中' : '去诊断'),
        raw: item
      }
    },

    openAlarmDiagnosis(item) {
      if (!item || item.diagnosisProcessing) return
      const raw = item.raw || {}
      const monitor = {
        ...raw,
        taskId: raw.taskId || '',
        turbineName: raw.turbineName || this.turbineName,
        turbineCode: raw.turbineCode || this.turbineCode,
        nodeName: raw.nodeName || raw.partName || item.nodeName,
        nodeId: raw.nodeId || '',
        nodeCode: raw.nodeCode || '',
        modelName: raw.modelName || item.modelName,
        modelShortName: raw.modelShortName || raw.algoShortname || '',
        dcTime: raw.dcTime || raw.alarmTime || item.time,
        location: raw.location || raw.nodeName || raw.partName || item.nodeName,
        monitorPointId: raw.monitorPointId || ''
      }
      const routeObject = {
        monitor,
        farmName: this.farmName
      }
      if (item.diagnosisCompleted && item.diagnosisRecord?.id) {
        routeObject.diagnosisRecordId = item.diagnosisRecord.id
      }

      this.$router.push({
        path: '/diagnosis/faultLocation/runModel',
        query: { object: JSON.stringify(routeObject) }
      })
    },

    goToDiagnosis() {
      this.$router.push({
        path: '/diagnosis/faultLocation/index',
        query: {
          farmName: this.farmName,
          turbineModel: this.turbineModel,
          turbineName: this.turbineName,
          turbineCode: this.turbineCode
        }
      })
    },

    // 加载报警历史
    async loadAlarmHistory() {
      this.alarmHistory = []
      this.alarmDetailList = []
      const canUpdateAccessStatus = this.isEdgeOnlineStatus(this.status)

      if (!this.turbineName || !this.turbineModel || !this.farmName) {
        return
      }

      try {
        const projectId = await this.resolveSelectedProjectId()
        if (!projectId) {
          this.alarmDetailList = []
          return
        }

        const [res, diagnosisRes] = await Promise.all([
          getDiagnosisAlarmList7d({ proId: projectId }),
          getDiagnosisRecords({ turbineCode: this.turbineCode }).catch(error => {
            console.warn('读取故障诊断记录失败', error)
            return null
          })
        ])
        const rawAlarmList = Array.isArray(res?.data?.data) ? res.data.data : []
        const diagnosisRecords = Array.isArray(diagnosisRes?.data?.data) ? diagnosisRes.data.data : []
        const filteredList = rawAlarmList
          .filter(item => this.isAlarmForSelectedTurbine(item))
          .filter(item => this.isAlarmWithinRecent7Days(item?.dcTime))
          .sort((a, b) => {
            const timeA = this.parseAlarmTime(a?.dcTime)?.getTime() || 0
            const timeB = this.parseAlarmTime(b?.dcTime)?.getTime() || 0
            return timeB - timeA
          })

        this.alarmDetailList = filteredList.map((item, index) => (
          this.buildAlarmDetailRow(item, index, diagnosisRecords)
        ))
        if (this.activeBoardView === 'data' && this.dataViewMode === 'history') {
          this.ensureCmsHistorySelection()
          this.loadCmsWaveform()
        }

        const dailyAlarmMap = new Map()
        filteredList.forEach((item) => {
          const alarmDate = this.parseAlarmTime(item?.dcTime)
          const dayKey = this.formatDayKey(alarmDate)
          if (dayKey && !dailyAlarmMap.has(dayKey)) {
            dailyAlarmMap.set(dayKey, item)
          }
        })

        const dailyRows = []
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        for (let i = 0; i < 7; i += 1) {
          const day = new Date(today)
          day.setDate(today.getDate() - i)
          const dayKey = this.formatDayKey(day)
          const alarmItem = dailyAlarmMap.get(dayKey)
          dailyRows.push(alarmItem ? this.buildAlarmHistoryRow(alarmItem) : this.buildNoAlarmHistoryRow(day))
        }

        this.alarmHistory = dailyRows
        if (canUpdateAccessStatus) {
          this.status = filteredList.some(item => this.isTodayAlarm(item?.dcTime)) ? 'abnormal' : 'normal'
        }
      } catch (error) {
        console.error('获取报警履历失败', error)
        this.alarmHistory = []
        this.alarmDetailList = []
      }
    },

    // 加载故障统计
    async loadFaultStats() {
      this.totalFaultCount = 0
      this.faultTypes = []

      const instanceCode = String(this.turbineCode || '').trim()
      if (!instanceCode) return

      try {
        const statsResponse = await getDiagnosisFaultStats(instanceCode)
        if (instanceCode !== String(this.turbineCode || '').trim()) return

        const statsPayload = statsResponse?.data?.data || {}
        const statisticRows = Array.isArray(statsPayload.faultTypes) ? statsPayload.faultTypes : []
        const modeMap = new Map()
        statisticRows.forEach(row => {
          const code = String(row?.faultCode || row?.fault_code || '').trim()
          const name = String(row?.faultName || row?.fault_name || '').trim()
          const count = Number(row?.count || 0)
          if (!name) return
          const key = code || name
          modeMap.set(key, {
            code,
            name,
            count: Number.isFinite(count) ? count : 0
          })
        })

        this.totalFaultCount = Number(statsPayload.totalCount || 0)
        const colors = ['#fd626e', '#f5c84b', '#4ea8ff', '#8bd957', '#2ca6b8', '#b98cff', '#ff9f43']
        this.faultTypes = Array.from(modeMap.values())
          .sort((a, b) => (a.code || a.name).localeCompare(b.code || b.name, 'zh-CN'))
          .map((item, index) => ({
            ...item,
            color: colors[index % colors.length]
          }))
      } catch (error) {
        console.error('获取当前设备故障统计失败', error)
        this.faultTypes = []
      }
    },

    // 加载边缘端信息
    async loadEdgeInfo() {
      this.stopEdgeMockTimer()
      this.edgeMockTick = 0
      this.edgeNodeName = this.buildEdgeNodeName()
      if (!this.edgeIsOnline) {
        this.resetEdgeRuntimeData()
        return
      }
      this.applyEdgeMockSnapshot(true)
      if (!this.configurationOnly && this.activeBoardView === 'fusion') {
        this.startEdgeMockTimer()
      }
    },

    isEdgeOnlineStatus(rawStatus) {
      const status = String(rawStatus || '').toLowerCase().trim()
      return ['normal', 'abnormal', 'alarm', 'warning', 'fault', '正常', '报警', '异常'].includes(status)
    },

    resetEdgeRuntimeData() {
      this.cpuUsage = 0
      this.memoryUsage = 0
      this.gpuUsage = 0
      this.diskUsage = 0
      this.rttValue = 0
      this.rttPercent = 0
      this.packetLoss = 0
      this.edgeInboundRate = 0
      this.edgeOutboundRate = 0
      this.edgeTemperature = 0
      this.edgeSampleRate = 0
      this.edgeActiveTasks = 0
      this.edgeLastUpdated = '--:--:--'
    },

    buildEdgeNodeName() {
      const siteMatch = String(this.turbineCode || '').match(/\d+/)
      const deviceMatch = String(this.turbineName || '').match(/[A-Za-z]+\d+/)
      const siteCode = siteMatch ? siteMatch[0] : '01'
      const deviceCode = deviceMatch ? deviceMatch[0].toUpperCase() : 'E01'
      return `EDGE-${siteCode}-${deviceCode}`
    },

    clampEdgeValue(value, min, max) {
      return Math.min(max, Math.max(min, value))
    },

    driftEdgeValue(current, target, volatility, min, max) {
      const pull = (target - current) * 0.2
      const noise = (Math.random() - 0.5) * volatility
      return this.clampEdgeValue(current + pull + noise, min, max)
    },

    applyEdgeMockSnapshot(initial = false) {
      this.edgeMockTick += 1
      const wave = Math.sin(this.edgeMockTick / 3.2)
      const slowWave = Math.sin(this.edgeMockTick / 8.5)

      if (initial) {
        this.cpuUsage = 42
        this.memoryUsage = 61
        this.gpuUsage = 28
        this.diskUsage = 37
        this.rttValue = 23
        this.packetLoss = 0.2
        this.edgeInboundRate = 18.6
        this.edgeOutboundRate = 4.8
        this.edgeTemperature = 46.8
        this.edgeSampleRate = 128
        this.edgeActiveTasks = 3
      } else {
        this.cpuUsage = Math.round(this.driftEdgeValue(this.cpuUsage, 46 + wave * 9, 5.5, 24, 78))
        this.memoryUsage = Math.round(this.driftEdgeValue(this.memoryUsage, 62 + slowWave * 4, 2.2, 48, 76))
        this.gpuUsage = Math.round(this.driftEdgeValue(this.gpuUsage, 31 + wave * 11, 7, 8, 68))
        this.diskUsage = Math.round(this.driftEdgeValue(this.diskUsage, 38, 0.5, 35, 42))
        this.rttValue = Math.round(this.driftEdgeValue(this.rttValue, 24 + wave * 5, 5, 12, 48))

        const packetSpike = Math.random() > 0.96 ? 0.7 : 0
        this.packetLoss = Number(this.driftEdgeValue(this.packetLoss, 0.18 + packetSpike, 0.16, 0, 1.2).toFixed(1))
        this.edgeInboundRate = Number(this.driftEdgeValue(this.edgeInboundRate, 19 + wave * 5.5, 2.8, 8, 32).toFixed(1))
        this.edgeOutboundRate = Number(this.driftEdgeValue(this.edgeOutboundRate, 5 + slowWave * 1.8, 1.2, 1.5, 11).toFixed(1))
        this.edgeTemperature = Number(this.driftEdgeValue(this.edgeTemperature, 43 + this.cpuUsage * 0.1, 0.8, 42, 55).toFixed(1))
        this.edgeSampleRate = Math.round(this.driftEdgeValue(this.edgeSampleRate, 128 + wave * 2, 2, 124, 132))
        this.edgeActiveTasks = this.edgeMockTick % 18 >= 15 ? 4 : 3
      }

      this.rttPercent = Math.min(100, Math.round(this.rttValue / 120 * 100))
      this.edgeLastUpdated = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    },

    startEdgeMockTimer() {
      if (this.edgeMockTimer) return
      this.edgeMockTimer = window.setInterval(() => {
        this.applyEdgeMockSnapshot()
      }, 2200)
    },

    stopEdgeMockTimer() {
      if (!this.edgeMockTimer) return
      window.clearInterval(this.edgeMockTimer)
      this.edgeMockTimer = null
    },

    // 渲染趋势图
    renderTrendChart(payload = {}, visibleCount = 0) {
      if (!this.$refs.trendChart) return
      const chart = echarts.getInstanceByDom(this.$refs.trendChart) || echarts.init(this.$refs.trendChart)

      const xData = Array.isArray(payload.xData) ? payload.xData : []
      const metricData = Array.isArray(payload.metricData) ? payload.metricData : []
      const thresholdData = Array.isArray(payload.thresholdData) ? payload.thresholdData : []
      const alarmData = Array.isArray(payload.alarmData) ? payload.alarmData : []
      const metricLegend = payload.metricLegend || '评估指标'
      const showCount = visibleCount > 0 ? Math.min(visibleCount, xData.length) : xData.length

      const xAxisData = xData.slice(0, showCount)
      const metricSeriesData = metricData.slice(0, showCount)
      const thresholdSeriesData = thresholdData.slice(0, showCount)
      const alarmSeriesData = alarmData.slice(0, showCount)

      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'line', lineStyle: { color: 'rgba(255,255,255,0.35)' } },
          backgroundColor: 'rgba(20, 45, 52, 0.95)',
          borderColor: 'rgba(255,255,255,0.18)',
          textStyle: { color: '#fff' }
        },
        animationDuration: 260,
        animationDurationUpdate: 220,
        animationEasingUpdate: 'linear',
        grid: { left: 56, right: 42, top: 62, bottom: 96, containLabel: true },
        xAxis: {
          name: '时间',
          nameLocation: 'middle',
          nameGap: 72,
          nameTextStyle: { color: 'rgba(255,255,255,0.85)', fontSize: 12, align: 'center' },
          type: 'category',
          data: xAxisData,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisTick: { show: false },
          axisLabel: {
            color: '#fff',
            interval: Math.max(0, Math.ceil(showCount / 4) - 1),
            fontSize: 10,
            rotate: 18,
            margin: 14,
            hideOverlap: true,
            formatter: value => this.formatTrendAxisLabel(value)
          }
        },
        yAxis: {
          type: 'value',
          min: value => Math.min(0, value.min),
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisLabel: { color: '#fff' },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
        },
        series: [
          {
            name: metricLegend,
            type: 'line',
            data: metricSeriesData,
            smooth: true,
            symbol: 'circle',
            symbolSize: 4,
            connectNulls: true,
            lineStyle: { color: '#2cb4ff', type: 'solid', width: 2 },
            itemStyle: { color: '#2cb4ff' }
          },
          {
            name: '阈值',
            type: 'line',
            data: thresholdSeriesData,
            smooth: false,
            symbol: 'none',
            lineStyle: { color: '#ffa500' },
            itemStyle: { color: '#ffa500' }
          },
          {
            name: '报警',
            type: 'effectScatter',
            data: alarmSeriesData,
            symbol: 'circle',
            symbolSize: 12,
            rippleEffect: { brushType: 'stroke', scale: 3 },
            itemStyle: { color: '#fd626e' },
            label: {
              show: true,
              position: 'top',
              color: '#fd626e',
              fontWeight: 700,
              formatter: ({ value }) => (value === null || value === undefined ? '' : '报警')
            },
            z: 5
          }
        ],
        legend: {
          data: [metricLegend, '阈值', '报警'],
          top: 12,
          right: 20,
          itemGap: 16,
          textStyle: { color: '#fff' }
        }
      })
    },

    renderNoVariableTrend(title = '该节点未配置感知变量') {
      if (!this.$refs.trendChart) return
      this.stopTrendAnimationTimer()
      const chart = echarts.getInstanceByDom(this.$refs.trendChart) || echarts.init(this.$refs.trendChart)
      chart.clear()
      const xData = ['-', '-', '-', '-', '-', '-', '-']
      const flatData = [0, 0, 0, 0, 0, 0, 0]
      chart.setOption({
        title: { show: false },
        grid: { left: '10%', right: '5%', top: 52, bottom: '22%', containLabel: true },
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
        legend: {
          data: ['无感知变量'],
          top: 32,
          right: 20,
          textStyle: { color: '#fff' }
        },
        series: [
          {
            name: '无感知变量',
            type: 'line',
            data: flatData,
            smooth: false,
            lineStyle: { color: '#7f8c8d', type: 'dashed' },
            itemStyle: { color: '#7f8c8d' }
          }
        ]
      })
    },

    // 渲染近7天数据量柱状图
    renderAlarmChart(payload = null) {
      if (!this.$refs.alarmChart) return
      const chart = echarts.getInstanceByDom(this.$refs.alarmChart) || echarts.init(this.$refs.alarmChart)

      const fallbackLabels = this.buildRecent7DayLabels()
      const xData = Array.isArray(payload?.labels) ? payload.labels : fallbackLabels
      const hasCounts = Array.isArray(payload?.counts) && payload.counts.length > 0
      const hasFaultCounts = Array.isArray(payload?.faultCounts) && payload.faultCounts.length > 0
      const dataCount = hasCounts ? payload.counts : new Array(xData.length).fill(0)
      const faultCount = hasFaultCounts ? payload.faultCounts : new Array(xData.length).fill(0)

      chart.setOption({
        grid: { left: '10%', right: '12%', top: '18%', bottom: '20%' },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        xAxis: {
          type: 'category',
          data: xData,
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
          axisTick: { show: false },
          axisLabel: { color: '#fff', fontSize: 10 }
        },
        yAxis: [
          {
            type: 'value',
            name: '数量',
            nameTextStyle: { color: 'rgba(255,255,255,0.7)' },
            axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
            axisLabel: { color: '#fff' },
            splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
          },
          {
            type: 'value',
            name: '故障次数',
            position: 'right',
            nameTextStyle: { color: 'rgba(255,255,255,0.7)' },
            axisLine: { lineStyle: { color: 'rgba(255,255,255,0.3)' } },
            axisLabel: { color: '#dffcff' },
            splitLine: { show: false }
          }
        ],
        series: [
          {
            name: '监测数据量',
            type: 'bar',
            data: dataCount,
            yAxisIndex: 0,
            itemStyle: { color: '#2cb4ff' },
            barWidth: '34%'
          },
          {
            name: '故障次数',
            type: 'bar',
            data: faultCount,
            yAxisIndex: 1,
            itemStyle: { color: '#80d8ed' },
            barWidth: '34%'
          }
        ],
        legend: {
          data: ['监测数据量', '故障次数'],
          top: 0,
          left: 'center',
          right: 'auto',
          textStyle: { color: '#fff' }
        }
      })
    },

  }
}
</script>

<style scoped lang="scss">
.turbine-detail-container {
  --detail-bg: #061222;
  --detail-panel: rgba(5, 39, 65, 0.76);
  --detail-panel-strong: rgba(3, 24, 45, 0.88);
  --detail-line: rgba(122, 214, 240, 0.22);
  --detail-line-bright: rgba(168, 232, 248, 0.4);
  --detail-text: #ffffff;
  --detail-muted: rgba(255, 255, 255, 0.76);
  --detail-accent: #80d8ed;
  --detail-accent-strong: #2cb4ff;
  --detail-accent-soft: rgba(128, 216, 237, 0.12);
  min-height: 100%;
  height: 100%;
  padding: 20px;
  position: relative;
  color: var(--detail-text);
  background: transparent;
  overflow-y: auto;
  overflow-x: hidden;
}

.turbine-detail-container::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(1, 9, 22, 0.18) 0%, rgba(3, 21, 35, 0.12) 42%, rgba(1, 8, 18, 0.2) 100%),
    radial-gradient(circle at 78% 18%, rgba(58, 151, 248, 0.18), transparent 34%),
    linear-gradient(rgba(128, 216, 237, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(128, 216, 237, 0.02) 1px, transparent 1px);
  background-size: auto, auto, 36px 36px, 36px 36px;
  z-index: 0;
}

.page-header,
.content-wrapper {
  position: relative;
  z-index: 1;
}

.page-header {
  margin: -8px 0 12px;

  .header-info {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: 30px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);

    .info-item {
      padding: 5px 15px;
      background: var(--detail-accent-soft);
      border-radius: 4px;
      border: 1px solid var(--detail-line);
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
          background: rgba(3, 24, 45, 0.72);
          border: 1px solid rgba(122, 214, 240, 0.24);
          box-shadow: none;

          &:hover {
            border-color: var(--detail-line-bright);
          }
        }

        .el-input__inner {
          color: var(--detail-text);
          font-size: 14px;
        }

        .el-select__caret {
          color: rgba(255, 255, 255, 0.6);
        }
      }
    }
  }
}

/* Dropdown overlay */
::v-deep(.el-select-dropdown) {
  background: rgba(3, 24, 45, 0.96) !important;
  backdrop-filter: blur(10px);
  border: 1px solid var(--detail-line) !important;

  .el-select-dropdown__item {
    color: rgba(255, 255, 255, 0.8);

    &:hover {
      background: var(--detail-accent-soft) !important;
      color: var(--detail-accent);
    }

    &.selected {
      background: rgba(128, 216, 237, 0.18) !important;
      color: var(--detail-accent);
      font-weight: bold;
    }
  }

  .el-popper__arrow::before {
    background: rgba(3, 24, 45, 0.96);
    border: 1px solid var(--detail-line);
  }
}

.content-wrapper {
  display: grid;
  grid-template-columns: 0.82fr 1.08fr 2.1fr;
  gap: 18px;
  min-height: 0;
  height: auto;
  overflow: visible;
}

/* Shared card styles */
.card-title {
  margin: 0;
  padding: 10px 15px;
  font-size: 16px;
  text-align: left;
  text-indent: 10px;
}

/* Left panel */
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: visible;
}

.info-card, .alarm-history-card {
  background:
    linear-gradient(180deg, rgba(6, 45, 72, 0.72), rgba(3, 24, 45, 0.86)),
    rgba(255, 255, 255, 0.03);
  border-radius: 8px;

  .card-header {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 10px 12px 0;
  }

  .diagnosis-link-btn {
    padding: 0;
    color: var(--detail-accent);

    &:hover {
      color: var(--detail-text);
    }
  }
  backdrop-filter: blur(10px);
  border: 1px solid var(--detail-line);
  box-shadow: 0 18px 38px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.07);
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
      color: var(--detail-muted);
      min-width: 90px;
    }

    .value {
      color: var(--detail-text);
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
    border-bottom: 1px solid rgba(122, 214, 240, 0.12) !important;
  }

  ::v-deep(.el-table__inner-wrapper::before) {
    display: none !important;
  }
}

/* Center panel */
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: visible;
}

.device-model-card {
  flex: 2;
  background:
    linear-gradient(180deg, rgba(6, 45, 72, 0.72), rgba(3, 24, 45, 0.86)),
    rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--detail-line);
  box-shadow: 0 18px 38px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.07);
  display: flex;
  flex-direction: column;
}

.model-layout {
  display: grid;
  grid-template-columns: minmax(280px, 0.95fr) minmax(230px, 1.05fr);
  gap: 14px;
  flex: 1;
  min-height: 0;
  padding: 12px;
}

.model-container {
  flex: 1;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  min-height: 320px;
  background: rgba(2, 16, 34, 0.72);
  border: 1px solid var(--detail-line);
  border-radius: 8px;
  overflow: hidden;

  .model-stage {
    position: relative;
    width: min(95%, 531px);
    aspect-ratio: 531 / 784;
    max-width: 600px;
    background: transparent;
  }

  .device-model-img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
    user-select: none;
    pointer-events: none;
  }

  .sensor-point {
    position: absolute;
    width: 14px;
    height: 14px;
    transform: translate(-50%, -50%);
    cursor: pointer;
    transition: transform 0.18s ease;

    &.related {
      transform: translate(-50%, -50%) scale(1.06);
      z-index: 5;
    }

    &.disabled {
      cursor: default;
    }

    .point-dot {
      position: absolute;
      left: 0;
      top: 0;
      width: 14px;
      height: 14px;
      background: var(--detail-accent-strong);
      border-radius: 50%;
      border: 2px solid rgba(2, 27, 38, 0.75);
      box-shadow: 0 0 10px rgba(44, 180, 255, 0.9);
      z-index: 2;

      &.related {
        box-shadow: 0 0 13px rgba(128, 216, 237, 0.95);
      }

      &.active {
        background: #ffd54f;
        box-shadow: 0 0 16px #ffd54f;
      }

      &.disabled {
        background: #7f8c8d;
        box-shadow: none;
      }
    }

    .point-ring {
      position: absolute;
      left: -5px;
      top: -5px;
      width: 24px;
      height: 24px;
      border-radius: 50%;
      border: 1px solid rgba(128, 216, 237, 0.55);
      animation: pointPulse 1.8s ease-out infinite;
      z-index: 1;

      &.related {
        border-color: rgba(168, 232, 248, 0.8);
      }

      &.active {
        border-color: rgba(255, 213, 79, 0.8);
      }

      &.disabled {
        animation: none;
        border-color: rgba(127, 140, 141, 0.45);
      }
    }

  }
}

.gbom-tree-panel {
  display: flex;
  flex-direction: column;
  min-height: 260px;
  background: rgba(3, 24, 45, 0.62);
  border: 1px solid var(--detail-line);
  border-radius: 8px;
  overflow: hidden;
}

.gbom-tree-title {
  padding: 10px 12px;
  color: var(--detail-accent);
  font-size: 13px;
  border-bottom: 1px solid var(--detail-line);
  letter-spacing: 0.5px;
}

@keyframes pointPulse {
  0% {
    opacity: 0.85;
    transform: scale(0.7);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}

@media (max-width: 1366px) {
  .content-wrapper {
    grid-template-columns: 0.88fr 1fr 1.85fr;
    gap: 14px;
  }

  .model-layout {
    grid-template-columns: 1fr;
  }

  .model-container {
    min-height: 320px;

    .model-stage {
      width: 96%;
      height: auto;
    }
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .model-container {
    min-height: 260px;

    .model-stage {
      width: 98%;
      height: auto;
    }
  }
}

.gbom-tree-scroll {
  flex: 0 1 58%;
  height: 58%;
  min-height: 320px;
  max-height: 58%;
  padding: 8px 8px 10px 8px;
}

.gbom-variable-box {
  flex: 0 0 auto;
  padding: 8px 10px 10px 10px;
  border-top: 1px solid var(--detail-line);
  background: rgba(0, 0, 0, 0.12);
}

.gbom-variable-title {
  color: var(--detail-accent);
  font-size: 12px;
  margin-bottom: 6px;
}

.gbom-variable-tip {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  line-height: 28px;
  min-height: 28px;
}

.gbom-variable-list {
  max-height: 130px;
}

.gbom-variable-item {
  padding: 6px 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  border: 1px solid transparent;
  color: rgba(255, 255, 255, 0.88);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: var(--detail-accent);
    border-color: var(--detail-line-bright);
    background: var(--detail-accent-soft);
  }

  &.active {
    color: #031f2b;
    border-color: rgba(128, 216, 237, 0.85);
    background: linear-gradient(90deg, rgba(168, 232, 248, 0.92), rgba(128, 216, 237, 0.9));
    font-weight: 600;
  }
}

.gbom-tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  color: rgba(255, 255, 255, 0.92);
  font-size: 12px;

  .gbom-node-indicator {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: rgba(128, 216, 237, 0.9);
    box-shadow: 0 0 6px rgba(128, 216, 237, 0.85);
    flex-shrink: 0;
  }

  .gbom-node-label {
    max-width: 210px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &.mapped {
    color: #dff8ff;
  }

  &.related {
    color: #ffe8a1;
    font-weight: 600;
  }

  &.active {
    color: #ffd54f;
    font-weight: 700;
  }
}

.gbom-empty {
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
  padding: 16px;
}

::v-deep(.gbom-tree-panel .el-tree) {
  background: transparent;
  color: #fff;
}

::v-deep(.gbom-tree-panel .el-tree-node__content:hover) {
  background: var(--detail-accent-soft);
}

::v-deep(.gbom-tree-panel .el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(128, 216, 237, 0.18);
}

::v-deep(.gbom-tree-panel .el-tree-node__expand-icon) {
  color: var(--detail-accent);
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 10px rgba(44, 180, 255, 0.9);
  }
  50% {
    box-shadow: 0 0 20px rgba(44, 180, 255, 0.95);
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
  background:
    linear-gradient(180deg, rgba(6, 45, 72, 0.72), rgba(3, 24, 45, 0.86)),
    rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--detail-line);
  box-shadow: 0 18px 38px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.07);
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

/* Right panel */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: visible;
}

.trend-chart-card, .alarm-chart-card, .edge-info-card {
  background:
    linear-gradient(180deg, rgba(6, 45, 72, 0.72), rgba(3, 24, 45, 0.86)),
    rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--detail-line);
  box-shadow: 0 18px 38px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.07);
}

.trend-chart-card {
  min-height: 480px;
  display: flex;
  flex-direction: column;
}

.alarm-chart-card {
  min-height: 240px;
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
    background: rgba(2, 16, 34, 0.68);
    border-radius: 10px;
    overflow: hidden;

    .bar-fill {
      height: 100%;
      background: var(--detail-accent-strong);
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

/* Smart-screen cockpit redesign */
.turbine-detail-container {
  --screen-bg: #07111a;
  --screen-panel: rgba(8, 28, 42, 0.86);
  --screen-panel-soft: rgba(10, 45, 62, 0.68);
  --screen-line: rgba(116, 224, 245, 0.22);
  --screen-line-strong: rgba(78, 168, 255, 0.42);
  --screen-text: #f1fbff;
  --screen-muted: rgba(231, 248, 255, 0.68);
  --screen-cyan: #39d7f5;
  --screen-green: #4ea8ff;
  --screen-amber: #f5c84b;
  --screen-red: #ff6b7a;
  min-height: 0;
  height: 100vh;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: var(--screen-text);
  letter-spacing: 0;
  background:
    linear-gradient(180deg, rgba(6, 16, 28, 0.96), rgba(4, 13, 22, 0.99)),
    linear-gradient(rgba(116, 224, 245, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(116, 224, 245, 0.028) 1px, transparent 1px);
  background-size: auto, 32px 32px, 32px 32px;
  overflow: hidden;
  box-sizing: border-box;
}

.turbine-detail-container::before {
  background:
    linear-gradient(180deg, rgba(55, 128, 224, 0.12), transparent 26%),
    linear-gradient(90deg, rgba(57, 215, 245, 0.08), transparent 42%, rgba(25, 86, 176, 0.08));
  opacity: 1;
}

.turbine-detail-container button {
  font: inherit;
  letter-spacing: 0;
}

.screen-header,
.screen-kpis,
.workflow-strip,
.content-wrapper {
  position: relative;
  z-index: 1;
}

.screen-header,
.screen-kpis,
.workflow-strip {
  flex: 0 0 auto;
}

.screen-header {
  display: grid;
  grid-template-columns: minmax(250px, 0.62fr) minmax(0, 1.38fr);
  gap: 12px;
  align-items: center;
  min-height: 44px;
  padding: 0 2px;
}

.screen-eyebrow,
.panel-kicker {
  display: block;
  color: var(--screen-green);
  font-size: 11px;
  font-weight: 700;
  line-height: 1.2;
  text-transform: uppercase;
  letter-spacing: 0;
}

.screen-device-summary {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 9px;
}

.back-overview-button {
  width: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 32px;
  min-height: 32px;
  padding: 0;
  border: 1px solid rgba(116, 224, 245, 0.3);
  border-radius: 6px;
  background: rgba(7, 24, 36, 0.74);
  color: var(--screen-cyan);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.back-overview-button:hover {
  color: #ffffff;
  border-color: rgba(78, 168, 255, 0.58);
  background: rgba(24, 89, 174, 0.5);
}

.screen-device-identity {
  min-width: 0;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: baseline;
  column-gap: 8px;
  row-gap: 2px;
}

.screen-device-identity strong,
.screen-device-identity span,
.screen-device-identity em {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.screen-device-identity strong {
  color: #ffffff;
  font-size: 17px;
  line-height: 1.2;
}

.screen-device-identity span {
  color: var(--screen-cyan);
  font-size: 11px;
}

.screen-device-identity em {
  grid-column: 1 / -1;
  color: var(--screen-muted);
  font-size: 11px;
  font-style: normal;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
  color: var(--screen-muted);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.status-badge.is-normal {
  color: #e4f3ff;
  border-color: rgba(78, 168, 255, 0.5);
  background: rgba(78, 168, 255, 0.14);
}

.status-badge.is-warning {
  color: #fff0d1;
  border-color: rgba(245, 200, 75, 0.5);
  background: rgba(245, 200, 75, 0.13);
}

.status-badge.is-idle {
  color: rgba(231, 248, 255, 0.66);
}

.screen-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
}

.selector-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 7px;
  min-width: 0;
}

.info-item-select {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
}

.info-item-select .label {
  color: var(--screen-muted);
  font-size: 12px;
  white-space: nowrap;
}

.info-item-select :deep(.el-select) {
  width: 124px;
}

.info-item-select :deep(.el-input__wrapper) {
  min-height: 32px;
  border-radius: 6px;
  background: rgba(3, 19, 30, 0.76);
  border: 1px solid rgba(116, 224, 245, 0.28);
  box-shadow: none;
}

.info-item-select :deep(.el-input__inner) {
  color: var(--screen-text);
  font-size: 13px;
}

.screen-switch {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border-radius: 8px;
  background: rgba(3, 17, 27, 0.8);
  border: 1px solid rgba(116, 224, 245, 0.2);
}

.screen-switch button {
  min-width: 76px;
  height: 30px;
  padding: 0 11px;
  border: 0;
  border-radius: 6px;
  color: var(--screen-muted);
  background: transparent;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}

.screen-switch button.active {
  color: #05141b;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
  font-weight: 700;
}

.config-workbench-button {
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 0 14px;
  border: 1px solid rgba(78, 168, 255, 0.55);
  border-radius: 7px;
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
  box-shadow: 0 8px 20px rgba(30, 122, 220, 0.22);
  transition: transform 0.16s ease, border-color 0.16s ease;
}

.config-workbench-button:hover {
  transform: translateY(-1px);
  border-color: rgba(216, 237, 255, 0.86);
}

.screen-kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 10px;
}

.kpi-tile {
  min-height: 60px;
  padding: 9px 12px;
  border-radius: 8px;
  border: 1px solid var(--screen-line);
  background:
    linear-gradient(180deg, rgba(15, 52, 72, 0.86), rgba(7, 26, 40, 0.92)),
    rgba(255, 255, 255, 0.02);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.05);
  box-sizing: border-box;
}

.kpi-label {
  display: block;
  color: var(--screen-muted);
  font-size: 11px;
  line-height: 1.2;
}

.kpi-tile strong {
  display: inline-block;
  margin-top: 4px;
  font-size: 24px;
  line-height: 1;
  color: #ffffff;
}

.kpi-unit {
  margin-left: 5px;
  color: var(--screen-muted);
  font-size: 12px;
}

.kpi-tile.tone-cyan strong { color: var(--screen-cyan); }
.kpi-tile.tone-green strong { color: var(--screen-green); }
.kpi-tile.tone-amber strong { color: var(--screen-amber); }
.kpi-tile.tone-red strong { color: var(--screen-red); }

.workflow-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(130px, 1fr));
  gap: 8px;
  padding: 8px;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 8px;
  background: rgba(4, 20, 32, 0.72);
}

.workflow-node {
  position: relative;
  min-height: 58px;
  padding: 8px 10px 8px 34px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.035);
  overflow: hidden;
}

.workflow-node::before {
  content: "";
  position: absolute;
  left: 13px;
  top: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(231, 248, 255, 0.36);
}

.workflow-node.done::before { background: var(--screen-green); box-shadow: 0 0 12px rgba(78, 168, 255, 0.86); }
.workflow-node.active::before { background: var(--screen-cyan); box-shadow: 0 0 14px rgba(57, 215, 245, 0.9); }
.workflow-node.warn::before { background: var(--screen-amber); box-shadow: 0 0 12px rgba(245, 200, 75, 0.72); }

.workflow-index {
  color: rgba(231, 248, 255, 0.42);
  font-size: 11px;
  font-weight: 800;
}

.workflow-title,
.workflow-value {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workflow-title {
  margin-top: 2px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
}

.workflow-value {
  margin-top: 3px;
  color: var(--screen-muted);
  font-size: 12px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: minmax(250px, 0.72fr) minmax(360px, 1fr) minmax(420px, 1.16fr);
  gap: 12px;
  align-items: stretch;
  flex: 1 1 auto;
  min-height: 0;
  height: auto;
  overflow: hidden;
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.content-wrapper.view-fusion {
  grid-template-columns: minmax(300px, 0.82fr) minmax(560px, 1.76fr) minmax(250px, 0.62fr);
}

@media (min-width: 1181px) {
  .content-wrapper.view-fusion {
    max-height: calc(100vh - 180px);
  }
}

.content-wrapper.view-flow {
  grid-template-columns: minmax(300px, 0.72fr) minmax(680px, 1.58fr);
}

.content-wrapper.view-flow .right-panel {
  display: none;
}

.content-wrapper.view-diagnosis {
  grid-template-columns: minmax(300px, 0.72fr) minmax(780px, 1.9fr);
}

.content-wrapper.view-diagnosis .left-panel {
  display: none;
}

.board-view-flow .screen-kpis,
.board-view-diagnosis .workflow-strip,
.board-view-data .workflow-strip,
.content-wrapper.view-fusion .alarm-ledger,
.content-wrapper.view-fusion .alarm-detail-card,
.content-wrapper.view-fusion .relation-panel,
.content-wrapper.view-fusion .edge-info-card,
.content-wrapper.view-flow .alarm-ledger,
.content-wrapper.view-flow .alarm-detail-card,
.content-wrapper.view-diagnosis .model-panel {
  display: none;
}

.data-view-workspace {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(240px, 280px);
  gap: 12px;
  overflow: hidden;
}

.data-waveform-panel,
.data-query-panel {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.data-view-heading {
  align-items: center;
}

.data-mode-switch {
  display: inline-grid;
  grid-template-columns: repeat(2, minmax(82px, 1fr));
  gap: 3px;
  padding: 3px;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 7px;
  background: rgba(0, 12, 24, 0.46);
}

.data-mode-switch button {
  min-height: 30px;
  padding: 0 12px;
  border: 0;
  border-radius: 5px;
  color: var(--screen-muted);
  background: transparent;
  font-size: 12px;
  cursor: pointer;
}

.data-mode-switch button.active {
  color: #ffffff;
  background: rgba(57, 215, 245, 0.18);
  box-shadow: inset 0 0 0 1px rgba(57, 215, 245, 0.26);
}

.data-context-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px;
  margin: 0 12px 10px;
  border: 1px solid rgba(116, 224, 245, 0.14);
  border-radius: 7px;
  overflow: hidden;
  background: rgba(116, 224, 245, 0.08);
}

.data-context-strip span {
  min-width: 0;
  padding: 9px 10px;
  color: var(--screen-muted);
  background: rgba(2, 20, 34, 0.88);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.data-context-strip strong {
  margin-left: 5px;
  color: #eefbff;
  font-size: 12px;
}

.data-waveform-shell {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin: 0 12px 12px;
  border: 1px solid rgba(116, 224, 245, 0.15);
  border-radius: 7px;
  overflow: hidden;
  background: rgba(0, 13, 25, 0.38);
}

.data-waveform-meta {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
  background: rgba(57, 215, 245, 0.07);
}

.data-waveform-meta span {
  min-width: 0;
  padding: 9px 12px;
  color: var(--screen-muted);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.data-waveform-meta strong {
  margin-left: 5px;
  color: #eefbff;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.data-waveform-chart {
  flex: 1;
  min-height: 360px;
  width: 100%;
}

.data-waveform-empty {
  flex: 1;
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--screen-muted);
}

.data-waveform-empty i {
  color: rgba(57, 215, 245, 0.52);
  font-size: 32px;
}

.data-waveform-empty strong {
  color: #eefbff;
  font-size: 14px;
}

.data-waveform-empty span {
  font-size: 12px;
}

.data-query-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 12px;
}

.data-query-fields label {
  min-height: 58px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 8px 10px;
  border: 1px solid rgba(116, 224, 245, 0.14);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.035);
}

.data-query-fields span {
  color: var(--screen-muted);
  font-size: 11px;
}

.data-query-fields strong {
  min-width: 0;
  color: #eefbff;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.data-alarm-field {
  min-height: 74px !important;
}

.data-alarm-select {
  width: 100%;
}

.data-alarm-select :deep(.el-input__wrapper) {
  min-height: 34px;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 5px;
  background: rgba(0, 13, 25, 0.62);
  box-shadow: none;
}

.data-alarm-select :deep(.el-input__inner) {
  color: #eefbff;
  font-size: 12px;
}

.data-query-button {
  min-height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  margin: auto 12px 12px;
  border: 1px solid rgba(116, 224, 245, 0.16);
  border-radius: 7px;
  color: #eefbff;
  background: rgba(57, 215, 245, 0.14);
  font-size: 12px;
  cursor: pointer;
}

.data-query-button:hover:not(:disabled) {
  border-color: rgba(57, 215, 245, 0.52);
  background: rgba(57, 215, 245, 0.22);
}

.data-query-button:disabled {
  color: rgba(231, 248, 255, 0.38);
  background: rgba(57, 215, 245, 0.06);
  cursor: not-allowed;
}

.left-panel,
.center-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  overflow: hidden;
}

.screen-panel,
.info-card,
.alarm-history-card,
.device-model-card,
.trend-chart-card,
.alarm-chart-card,
.edge-info-card,
.fault-stats-card {
  position: relative;
  border-radius: 8px;
  border: 1px solid var(--screen-line);
  background:
    linear-gradient(180deg, rgba(10, 45, 62, 0.86), rgba(5, 22, 35, 0.94)),
    rgba(255, 255, 255, 0.02);
  box-shadow: 0 16px 34px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.055);
  overflow: hidden;
}

.screen-panel::before {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(78, 168, 255, 0.58), transparent);
  pointer-events: none;
}

.panel-heading {
  min-height: 54px;
  padding: 12px 14px 10px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
  background: linear-gradient(90deg, rgba(57, 215, 245, 0.14), rgba(38, 100, 210, 0.08), transparent);
  box-sizing: border-box;
}

.panel-heading.compact {
  min-height: 48px;
  align-items: center;
}

.panel-heading h3 {
  margin: 2px 0 0;
  color: #ffffff;
  font-size: 16px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: 0;
}

.workflow-panel {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.vertical-flow {
  flex: 1 1 auto;
  min-height: 0;
  padding: 12px 14px 14px;
  overflow: auto;
}

.flow-step {
  position: relative;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 10px;
  padding-bottom: 14px;
}

.flow-step:not(:last-child)::after {
  content: "";
  position: absolute;
  left: 8px;
  top: 18px;
  bottom: 0;
  width: 1px;
  background: rgba(116, 224, 245, 0.2);
}

.flow-dot {
  width: 17px;
  height: 17px;
  margin-top: 2px;
  border-radius: 50%;
  border: 1px solid rgba(231, 248, 255, 0.24);
  background: rgba(231, 248, 255, 0.08);
  z-index: 1;
}

.flow-step.done .flow-dot { background: var(--screen-green); box-shadow: 0 0 14px rgba(78, 168, 255, 0.72); }
.flow-step.active .flow-dot { background: var(--screen-cyan); box-shadow: 0 0 14px rgba(57, 215, 245, 0.82); }
.flow-step.warn .flow-dot { background: var(--screen-amber); box-shadow: 0 0 14px rgba(245, 200, 75, 0.68); }

.flow-copy {
  min-width: 0;
}

.flow-copy strong,
.flow-copy span,
.flow-copy em {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flow-copy strong {
  color: #ffffff;
  font-size: 13px;
  line-height: 1.25;
}

.flow-copy span {
  margin-top: 3px;
  color: var(--screen-cyan);
  font-size: 12px;
}

.flow-copy em {
  margin-top: 3px;
  color: var(--screen-muted);
  font-size: 11px;
  font-style: normal;
}

.device-profile {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.device-overview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin: 10px 12px 0;
  padding: 10px 12px;
  border-left: 2px solid var(--screen-cyan);
  background: linear-gradient(90deg, rgba(57, 215, 245, 0.12), rgba(78, 168, 255, 0.035));
}

.device-overview > div {
  min-width: 0;
}

.device-overview span,
.device-overview strong,
.device-overview em {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-overview span {
  color: var(--screen-muted);
  font-size: 11px;
}

.device-overview strong {
  margin-top: 3px;
  color: #ffffff;
  font-size: 15px;
  line-height: 1.2;
}

.device-overview em {
  margin-top: 4px;
  color: rgba(216, 237, 255, 0.64);
  font-size: 11px;
  font-style: normal;
}

.device-overview b {
  flex: 0 0 auto;
  padding: 5px 7px;
  border: 1px solid rgba(78, 168, 255, 0.46);
  border-radius: 4px;
  color: #e4f3ff;
  background: rgba(78, 168, 255, 0.14);
  font-size: 11px;
  line-height: 1.1;
  white-space: nowrap;
}

.device-overview b.is-warning {
  border-color: rgba(245, 200, 75, 0.5);
  color: #fff0d1;
  background: rgba(245, 200, 75, 0.12);
}

.device-overview b.is-idle {
  border-color: rgba(231, 248, 255, 0.22);
  color: rgba(231, 248, 255, 0.66);
  background: rgba(255, 255, 255, 0.05);
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 10px 12px 14px;
  min-height: 0;
  overflow: auto;
  align-content: start;
}

.profile-item {
  min-width: 0;
  min-height: 50px;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid rgba(116, 224, 245, 0.13);
  background: rgba(2, 14, 24, 0.36);
  box-sizing: border-box;
}

.profile-item:last-child {
  grid-column: 1 / -1;
}

.content-wrapper.view-fusion .profile-item {
  min-height: 44px;
  padding: 7px 9px;
}

.content-wrapper.view-fusion .profile-item strong {
  margin-top: 3px;
  font-size: 12px;
}

.profile-item span {
  display: block;
  color: var(--screen-muted);
  font-size: 11px;
}

.profile-item strong {
  display: block;
  margin-top: 4px;
  color: #ffffff;
  font-size: 13px;
  line-height: 1.2;
  word-break: break-word;
}

.alarm-ledger {
  flex: 0 0 auto;
}

.alarm-ledger :deep(.el-table),
.alarm-ledger :deep(.el-table tr),
.alarm-ledger :deep(.el-table th),
.alarm-ledger :deep(.el-table td) {
  color: rgba(241, 251, 255, 0.86) !important;
  background: transparent !important;
  border-color: rgba(116, 224, 245, 0.12) !important;
}

.alarm-ledger :deep(.el-table th.el-table__cell) {
  color: #dffcff !important;
  background: rgba(57, 215, 245, 0.13) !important;
}

.model-panel {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.model-heading {
  align-items: center;
}

.active-node-pill {
  max-width: 240px;
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #6bb8ff);
  font-size: 12px;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-layout {
  display: grid;
  grid-template-columns: minmax(280px, 0.92fr) minmax(280px, 1.08fr);
  gap: 12px;
  flex: 1;
  min-height: 0;
  padding: 12px;
}

.model-container {
  min-height: 0;
  padding: 12px;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(3, 18, 30, 0.86), rgba(4, 27, 40, 0.72)),
    linear-gradient(rgba(116, 224, 245, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(116, 224, 245, 0.03) 1px, transparent 1px);
  background-size: auto, 28px 28px, 28px 28px;
  border: 1px solid rgba(116, 224, 245, 0.18);
}

.model-container .model-stage {
  width: min(92%, 520px);
  aspect-ratio: 531 / 784;
  max-height: 100%;
}

.content-wrapper.view-fusion .model-container {
  align-items: flex-start;
}

.content-wrapper.view-fusion .model-container .model-stage {
  margin-top: 2px;
}

.model-container .sensor-point {
  width: 15px;
  height: 15px;
}

.model-container .sensor-point .point-dot {
  width: 15px;
  height: 15px;
  background: var(--screen-cyan);
  border-color: rgba(4, 18, 28, 0.9);
}

.model-container .sensor-point .point-ring {
  left: -7px;
  top: -7px;
  width: 29px;
  height: 29px;
  border-color: rgba(57, 215, 245, 0.72);
}

.point-label {
  position: absolute;
  left: 19px;
  top: -8px;
  max-width: 128px;
  padding: 3px 6px;
  border-radius: 5px;
  color: #07151c;
  background: rgba(215, 255, 245, 0.94);
  font-size: 11px;
  font-weight: 700;
  line-height: 1.25;
  opacity: 0;
  transform: translateY(4px);
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sensor-point:hover .point-label,
.sensor-point.active .point-label,
.sensor-point.related .point-label {
  opacity: 1;
  transform: translateY(0);
}

.gbom-tree-panel {
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  border-color: rgba(116, 224, 245, 0.18);
  background: rgba(2, 15, 25, 0.62);
}

.gbom-tree-title,
.gbom-variable-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--screen-cyan);
  letter-spacing: 0;
}

.gbom-tree-title strong,
.gbom-variable-title strong {
  color: var(--screen-green);
  font-size: 12px;
}

.gbom-tree-scroll {
  flex: 1 1 auto;
  min-height: 0;
  height: auto;
  max-height: none;
  padding: 8px;
}

.gbom-variable-box {
  border-top-color: rgba(116, 224, 245, 0.16);
}

.gbom-variable-list {
  max-height: 150px;
}

.content-wrapper.view-fusion .gbom-tree-scroll {
  flex: 0 1 54%;
  max-height: 54%;
}

.content-wrapper.view-fusion .gbom-variable-list {
  max-height: 126px;
}

.gbom-variable-item {
  width: 100%;
  min-height: 30px;
  display: block;
  text-align: left;
  border: 1px solid transparent;
  color: rgba(241, 251, 255, 0.9);
  background: rgba(255, 255, 255, 0.03);
}

.gbom-variable-item.active {
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
}

.gbom-task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.gbom-task-item > span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gbom-task-item em {
  flex: 0 0 auto;
  padding: 2px 5px;
  border-radius: 4px;
  color: rgba(231, 248, 255, 0.68);
  background: rgba(255, 255, 255, 0.08);
  font-size: 10px;
  font-style: normal;
  line-height: 1.2;
}

.gbom-task-item em.is-running {
  color: #06151c;
  background: var(--screen-green);
}

.gbom-task-item em.is-ready {
  color: #06151c;
  background: var(--screen-cyan);
}

.gbom-task-item em.is-stopped {
  color: #fff0d1;
  background: rgba(245, 200, 75, 0.24);
}

.gbom-tree-node .gbom-node-label {
  max-width: 230px;
}

.relation-panel {
  flex: 0 0 188px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.relation-scroll {
  flex: 1;
  min-height: 0;
  height: 100%;
  padding: 10px 12px 12px;
}

.relation-empty {
  flex: 1;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: var(--screen-muted);
  text-align: center;
}

.relation-empty strong {
  color: #ffffff;
}

.relation-card {
  width: 100%;
  min-height: 58px;
  display: grid;
  grid-template-columns: minmax(120px, 1.2fr) minmax(76px, 0.65fr) minmax(120px, 1fr) auto auto;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  padding: 10px;
  border-radius: 7px;
  border: 1px solid rgba(116, 224, 245, 0.16);
  color: var(--screen-text);
  background: rgba(3, 18, 30, 0.52);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.relation-card:hover,
.relation-card.active {
  border-color: rgba(78, 168, 255, 0.58);
  background: rgba(24, 89, 174, 0.46);
}

.relation-card.active {
  transform: translateX(2px);
}

.relation-card.running {
  border-color: rgba(78, 168, 255, 0.42);
  background: rgba(24, 89, 174, 0.34);
}

.relation-name,
.relation-meta {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.relation-name {
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
}

.relation-meta {
  color: var(--screen-muted);
  font-size: 12px;
}

.task-chip {
  justify-self: end;
  min-width: 58px;
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  border-radius: 999px;
  color: var(--screen-muted);
  background: rgba(255, 255, 255, 0.06);
  font-size: 11px;
  font-weight: 800;
}

.task-chip.is-running { color: #06151c; background: var(--screen-green); }
.task-chip.is-ready { color: #06151c; background: var(--screen-cyan); }
.task-chip.is-stopped { color: #fff2d8; background: rgba(245, 200, 75, 0.22); }
.task-chip.is-idle { color: rgba(231, 248, 255, 0.6); }

.run-task-button {
  justify-self: end;
  min-width: 62px;
  height: 28px;
  padding: 0 11px;
  border: 1px solid rgba(78, 168, 255, 0.5);
  border-radius: 6px;
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
  white-space: nowrap;
  transition: transform 0.16s ease, opacity 0.16s ease, border-color 0.16s ease;
}

.run-task-button:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: rgba(216, 237, 255, 0.8);
}

.run-task-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.run-task-button.running {
  color: rgba(231, 248, 255, 0.76);
  background: rgba(78, 168, 255, 0.16);
}

.trend-chart-card {
  flex: 1 1 470px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.chart-heading {
  align-items: center;
}

.diagnosis-summary {
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.diagnosis-summary span {
  max-width: 180px;
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  padding: 0 9px;
  border-radius: 999px;
  color: var(--screen-muted);
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(116, 224, 245, 0.15);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chart {
  width: 100%;
  flex: 1;
  min-height: 0;
}

.trend-chart-card .chart {
  min-height: 0;
}

.right-lower-grid {
  display: grid;
  grid-template-columns: minmax(280px, 1.25fr) minmax(220px, 0.75fr);
  gap: 12px;
  min-height: 0;
}

.content-wrapper.view-fusion .right-lower-grid {
  display: none;
}

.content-wrapper.view-diagnosis .center-panel {
  min-width: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
}

.content-wrapper.view-diagnosis .relation-panel {
  flex: initial;
  min-height: 0;
}

.content-wrapper.view-diagnosis .relation-panel,
.content-wrapper.view-diagnosis .alarm-detail-card {
  height: 100%;
  overflow: hidden;
}

.content-wrapper.view-diagnosis .relation-card {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
}

.content-wrapper.view-diagnosis .relation-name {
  grid-column: 1;
}

.content-wrapper.view-diagnosis .relation-meta {
  grid-column: 1;
}

.content-wrapper.view-diagnosis .task-chip {
  grid-column: 2;
  grid-row: 1;
}

.content-wrapper.view-diagnosis .run-task-button {
  grid-column: 2;
  grid-row: 2 / span 2;
}

.content-wrapper.view-diagnosis .right-panel {
  display: grid;
  grid-template-columns: minmax(500px, 1.7fr) minmax(210px, 0.5fr);
  grid-template-rows: minmax(0, 1fr) minmax(0, 0.86fr);
  gap: 12px;
}

.content-wrapper.view-diagnosis .trend-chart-card {
  grid-row: 1 / -1;
  min-height: 0;
}

.content-wrapper.view-diagnosis .right-lower-grid {
  grid-row: 1 / -1;
  grid-template-columns: 1fr;
  grid-template-rows: minmax(0, 1fr);
}

.content-wrapper.view-diagnosis .fault-stats-card {
  display: none;
}

.content-wrapper.view-diagnosis .edge-info-card {
  display: none;
  min-height: 0;
  overflow: hidden;
}

.alarm-chart-card,
.alarm-detail-card,
.fault-stats-card {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.alarm-chart-card .chart {
  min-height: 0;
}

.alarm-detail-card {
  flex: 1;
}

.alarm-detail-scroll {
  flex: 1;
  min-height: 0;
  height: 100%;
  padding: 10px 12px 12px;
}

.alarm-detail-empty {
  flex: 1;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--screen-muted);
  font-size: 13px;
}

.alarm-detail-item {
  position: relative;
  min-height: 78px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 6px 10px;
  align-items: start;
  margin-bottom: 9px;
  padding: 10px 10px 10px 13px;
  border: 1px solid rgba(255, 107, 122, 0.2);
  border-radius: 7px;
  background: linear-gradient(90deg, rgba(124, 33, 48, 0.3), rgba(7, 26, 40, 0.64));
}

.alarm-detail-item::before {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 999px;
  background: var(--screen-red);
  box-shadow: 0 0 12px rgba(255, 107, 122, 0.58);
}

.alarm-detail-main,
.alarm-detail-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.alarm-detail-main strong,
.alarm-detail-main span,
.alarm-detail-meta span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.monitor-runtime-overview {
  min-height: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding: 24px 26px 22px;
  overflow: hidden;
}

.monitor-status-hero {
  display: flex;
  align-items: center;
  min-height: 64px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
}

.monitor-status-copy {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 14px;
}

.monitor-live-dot {
  width: 15px;
  height: 15px;
  flex: 0 0 15px;
  border-radius: 50%;
  background: #f5c84b;
  box-shadow: 0 0 0 7px rgba(245, 200, 75, 0.12), 0 0 18px rgba(245, 200, 75, 0.62);
}

.monitor-runtime-overview.is-normal .monitor-live-dot {
  background: #58d68d;
  box-shadow: 0 0 0 7px rgba(88, 214, 141, 0.12), 0 0 18px rgba(88, 214, 141, 0.62);
  animation: monitor-pulse 1.8s ease-out infinite;
}

.monitor-runtime-overview.is-anomaly .monitor-live-dot {
  background: #ff6b7a;
  box-shadow: 0 0 0 7px rgba(255, 107, 122, 0.14), 0 0 18px rgba(255, 107, 122, 0.7);
}

.monitor-runtime-overview.is-stale .monitor-live-dot {
  background: #9aa9b7;
  box-shadow: 0 0 0 7px rgba(154, 169, 183, 0.12);
}

@keyframes monitor-pulse {
  0% { box-shadow: 0 0 0 0 rgba(88, 214, 141, 0.38), 0 0 18px rgba(88, 214, 141, 0.52); }
  75%, 100% { box-shadow: 0 0 0 10px rgba(88, 214, 141, 0), 0 0 18px rgba(88, 214, 141, 0.52); }
}

.monitor-status-copy > div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.monitor-status-copy span,
.monitor-runtime-metrics span,
.monitor-window-heading,
.monitor-runtime-strip span {
  color: var(--screen-muted);
  font-size: 12px;
}

.monitor-status-copy strong {
  color: #f3fbff;
  font-size: 28px;
  line-height: 1.15;
  letter-spacing: 0;
}

.monitor-runtime-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-top: 1px solid rgba(116, 224, 245, 0.12);
  border-bottom: 1px solid rgba(116, 224, 245, 0.12);
}

.monitor-runtime-metrics > div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 15px 18px;
}

.monitor-runtime-metrics > div + div {
  border-left: 1px solid rgba(116, 224, 245, 0.12);
}

.monitor-runtime-metrics strong {
  color: #f3fbff;
  font-size: 16px;
  font-variant-numeric: tabular-nums;
  overflow-wrap: anywhere;
}

.monitor-runtime-metrics strong.danger { color: #ff8995; }

.monitor-count-summary {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.monitor-count-summary b {
  color: #58d68d;
  font-size: 18px;
  font-weight: 700;
}

.monitor-count-summary b.danger { color: #ff8995; }

.monitor-count-summary small {
  margin-right: 8px;
  color: var(--screen-muted);
  font-size: 12px;
  font-weight: 400;
}

.monitor-window-section {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.monitor-window-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.monitor-window-legend {
  display: flex;
  align-items: center;
  gap: 14px;
}

.monitor-window-legend > span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: rgba(231, 248, 255, 0.54);
  font-size: 11px;
}

.monitor-window-legend i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #58d68d;
}

.monitor-window-legend i.is-anomaly { background: #ff6b7a; }
.monitor-window-legend i.is-empty { background: #667583; }

.monitor-window-track {
  height: 52px;
  display: grid;
  grid-template-columns: repeat(30, minmax(3px, 1fr));
  align-items: end;
  gap: 4px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(116, 224, 245, 0.12);
}

.monitor-window-track > span {
  height: 24px;
  border-radius: 2px;
  background: #58d68d;
  box-shadow: 0 0 8px rgba(88, 214, 141, 0.28);
}

.monitor-window-track > span.is-anomaly {
  height: 36px;
  background: #ff6b7a;
  box-shadow: 0 0 10px rgba(255, 107, 122, 0.46);
}

.monitor-window-track > span.is-empty {
  height: 14px;
  background: rgba(154, 169, 183, 0.22);
  box-shadow: none;
}

.monitor-window-empty {
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(116, 224, 245, 0.12);
  color: rgba(231, 248, 255, 0.46);
  font-size: 12px;
}

.monitor-runtime-strip {
  min-height: 64px;
  display: grid;
  grid-template-columns: minmax(190px, 1.25fr) repeat(3, minmax(120px, 0.75fr));
  align-items: center;
  border-top: 1px solid rgba(116, 224, 245, 0.14);
  border-bottom: 1px solid rgba(116, 224, 245, 0.14);
}

.monitor-runtime-strip > div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
}

.monitor-runtime-strip > div + div {
  border-left: 1px solid rgba(116, 224, 245, 0.12);
}

.monitor-strip-status {
  flex-direction: row !important;
  align-items: center;
}

.monitor-strip-status > i {
  width: 9px;
  height: 9px;
  flex: 0 0 9px;
  border-radius: 50%;
  background: #f5c84b;
}

.monitor-runtime-strip.is-normal .monitor-strip-status > i { background: #58d68d; }
.monitor-runtime-strip.is-anomaly .monitor-strip-status > i { background: #ff6b7a; }
.monitor-runtime-strip.is-stale .monitor-strip-status > i { background: #9aa9b7; }

.monitor-strip-status > div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.monitor-runtime-strip strong {
  color: #f3fbff;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  overflow-wrap: anywhere;
}

.alarm-detail-main strong {
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
}

.alarm-detail-main span {
  color: rgba(231, 248, 255, 0.72);
  font-size: 11px;
}

.alarm-detail-meta {
  grid-column: 1 / -1;
}

.alarm-detail-meta span {
  color: rgba(231, 248, 255, 0.52);
  font-size: 12px;
}

.alarm-detail-actions {
  display: flex;
  align-items: center;
  gap: 7px;
  white-space: nowrap;
}

.alarm-detail-item em {
  min-width: 56px;
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 11px;
  font-style: normal;
  font-weight: 800;
}

.alarm-detail-item em.is-active {
  color: #fff2f2;
  background: rgba(255, 107, 122, 0.28);
}

.alarm-detail-item em.is-resolved {
  color: #06151c;
  background: var(--screen-cyan);
}

.alarm-diagnosis-button {
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 7px;
  border: 1px solid rgba(116, 224, 245, 0.38);
  border-radius: 4px;
  color: #8be9f7;
  background: rgba(20, 88, 105, 0.28);
  font-size: 11px;
  cursor: pointer;
}

.alarm-diagnosis-button:hover {
  border-color: rgba(116, 224, 245, 0.72);
  color: #ffffff;
  background: rgba(34, 126, 145, 0.38);
}

.alarm-diagnosis-button.is-view {
  color: #8be4b0;
  border-color: rgba(88, 214, 141, 0.42);
  background: rgba(38, 112, 72, 0.26);
}

.alarm-diagnosis-button:disabled {
  color: rgba(231, 248, 255, 0.4);
  border-color: rgba(231, 248, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  cursor: default;
}

.fault-total {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
  align-items: baseline;
  padding: 14px 14px 8px;
}

.fault-total span {
  color: var(--screen-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fault-total strong {
  color: var(--screen-red);
  font-size: 34px;
  line-height: 1;
}

.fault-total em {
  color: var(--screen-muted);
  font-style: normal;
}

.fault-type-list {
  grid-template-columns: 1fr;
  gap: 8px;
  padding: 0 14px 14px;
}

.fault-type-item {
  min-height: 28px;
  padding: 0 6px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.035);
}

.historic-fault-overview {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: auto 34px minmax(0, 1fr);
  gap: 18px;
  padding: 22px 24px 24px;
}

.fault-overview-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: stretch;
}

.fault-device-name,
.fault-total-pill {
  min-height: 82px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(116, 224, 245, 0.2);
  border-radius: 8px;
  background: rgba(3, 18, 30, 0.48);
}

.fault-device-name {
  padding: 0 18px;
}

.fault-device-name span,
.fault-total-pill span {
  color: rgba(231, 248, 255, 0.58);
  font-size: 12px;
}

.fault-device-name strong {
  min-width: 0;
  margin-top: 8px;
  color: #ffffff;
  font-size: 24px;
  line-height: 1.15;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fault-total-pill {
  min-width: 150px;
  align-items: center;
  padding: 0 18px;
  background: linear-gradient(180deg, rgba(124, 33, 48, 0.4), rgba(4, 18, 31, 0.64));
}

.fault-total-pill strong {
  margin-top: 2px;
  color: #ff6b7a;
  font-size: 42px;
  line-height: 0.95;
}

.fault-total-pill em {
  color: rgba(231, 248, 255, 0.58);
  font-style: normal;
  font-size: 12px;
}

.fault-stack {
  display: flex;
  width: 100%;
  min-height: 34px;
  overflow: hidden;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.06);
}

.fault-stack span {
  min-width: 10px;
}

.fault-rank-grid {
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
  overflow: auto;
}

.fault-rank-item {
  min-width: 0;
  min-height: 58px;
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid rgba(116, 224, 245, 0.15);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.035);
}

.fault-rank-item .type-icon {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}

.fault-rank-item strong,
.fault-rank-item small {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fault-rank-item strong {
  color: #f4fbff;
  font-size: 14px;
}

.fault-rank-item small {
  margin-top: 3px;
  color: rgba(231, 248, 255, 0.46);
  font-size: 12px;
}

.fault-rank-item em {
  color: #ffffff;
  font-style: normal;
  font-size: 18px;
  font-weight: 900;
  white-space: nowrap;
}

.edge-info-summary {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 12px;
  padding: 12px 14px 14px;
}

.edge-metric-grid {
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.edge-metric-item {
  min-width: 0;
  min-height: 66px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  grid-template-rows: auto 8px;
  gap: 9px 8px;
  align-items: end;
  padding: 10px;
  border: 1px solid rgba(116, 224, 245, 0.14);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.035);
}

.edge-metric-item span {
  color: rgba(231, 248, 255, 0.62);
  font-size: 12px;
}

.edge-metric-item strong {
  color: #ffffff;
  font-size: 20px;
  line-height: 1;
}

.edge-metric-item strong small {
  color: rgba(231, 248, 255, 0.56);
  font-size: 10px;
  font-weight: 600;
}

.edge-metric-item i {
  grid-column: 1 / -1;
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
}

.edge-metric-item b {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #4ea8ff, #39d7f5);
  transition: width 0.8s ease;
}

.edge-metric-item b.warm {
  background: linear-gradient(90deg, #f5c84b, #ff9d57);
}

.edge-metric-item b.sample {
  background: linear-gradient(90deg, #6f7dff, #ae83ff);
}

.edge-link-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.edge-link-row span {
  min-width: 0;
  min-height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  border-radius: 7px;
  color: rgba(231, 248, 255, 0.68);
  background: rgba(78, 168, 255, 0.1);
  font-size: 12px;
  white-space: nowrap;
}

.edge-link-row strong {
  margin-left: 5px;
  color: #ffffff;
  font-weight: 900;
}

.diagnosis-fault-summary {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: auto 18px minmax(0, 1fr);
  gap: 8px;
  padding: 10px;
}

.fault-summary-head {
  min-height: 58px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: baseline;
  gap: 5px;
  padding: 10px;
  border: 1px solid rgba(255, 107, 122, 0.18);
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(124, 33, 48, 0.34), rgba(3, 18, 30, 0.62));
}

.fault-summary-head span {
  color: rgba(231, 248, 255, 0.66);
  font-size: 12px;
}

.fault-summary-head strong {
  color: #ff6b7a;
  font-size: 28px;
  line-height: 1;
}

.fault-summary-head em {
  color: rgba(231, 248, 255, 0.66);
  font-style: normal;
}

.fault-summary-stack {
  display: flex;
  min-height: 18px;
  overflow: hidden;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.06);
}

.fault-summary-stack span {
  min-width: 8px;
}

.fault-summary-list {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
  overflow: auto;
}

.fault-summary-item {
  min-height: 34px;
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr) auto;
  align-items: center;
  gap: 7px;
  padding: 0 8px;
  border: 1px solid rgba(116, 224, 245, 0.14);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.035);
}

.fault-summary-item .type-icon {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.fault-summary-item .type-name {
  min-width: 0;
  color: #f1fbff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fault-summary-item strong {
  color: #ffffff;
  font-size: 13px;
  white-space: nowrap;
}

.fault-summary-empty {
  min-height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(231, 248, 255, 0.5);
  font-size: 12px;
}

.historic-fault-card {
  overflow: hidden;
}

.historic-fault-board {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 16px 16px;
}

.historic-machine {
  position: relative;
  min-height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.machine-name {
  position: absolute;
  top: 6px;
  left: 50%;
  z-index: 2;
  min-width: 220px;
  max-width: 70%;
  min-height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translateX(-50%);
  padding: 0 18px;
  color: #eefbff;
  background: rgba(78, 128, 210, 0.82);
  font-size: 22px;
  font-weight: 800;
  line-height: 1.1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.machine-outline {
  position: relative;
  width: min(320px, 72%);
  height: 58px;
  margin-top: 30px;
  border: 2px solid rgba(216, 237, 255, 0.68);
  border-left-color: transparent;
  border-right-color: transparent;
}

.machine-outline::before,
.machine-outline::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 42px;
  height: 2px;
  background: rgba(216, 237, 255, 0.68);
}

.machine-outline::before {
  left: -42px;
}

.machine-outline::after {
  right: -42px;
}

.machine-count {
  position: absolute;
  left: 50%;
  top: 58px;
  z-index: 3;
  transform: translateX(-50%);
  color: #ff1734;
  font-size: 22px;
  font-weight: 800;
  font-style: italic;
  white-space: nowrap;
  text-shadow: 0 0 10px rgba(255, 23, 52, 0.32);
}

.machine-count strong {
  font-size: 28px;
}

.machine-dot {
  position: absolute;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.82);
}

.machine-dot:nth-child(1) { left: -35px; top: -8px; }
.machine-dot:nth-child(2) { left: -35px; top: 16px; }
.machine-dot:nth-child(3) { left: -35px; top: 40px; }
.machine-dot:nth-child(4) { left: 50%; top: -8px; transform: translateX(-50%); }
.machine-dot:nth-child(5) { left: 50%; bottom: -8px; transform: translateX(-50%); }
.machine-dot:nth-child(6) { right: -35px; top: -8px; }
.machine-dot:nth-child(7) { right: -35px; top: 16px; }
.machine-dot:nth-child(8) { right: -35px; top: 40px; }

.fault-band {
  display: flex;
  width: 100%;
  height: 28px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
}

.fault-band span {
  min-width: 10px;
}

.fault-band .band-main { flex: 7; background: #31ddbd; }
.fault-band .band-oil { flex: 1; background: #f4f20a; }
.fault-band .band-gap { flex: 2; background: #9bd84a; }
.fault-band .band-fit { flex: 2; background: #2d929e; }
.fault-band .band-wear { flex: 2; background: #239dc4; }

.fault-legend-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 18px;
}

.fault-legend-item {
  min-width: 0;
  min-height: 24px;
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  color: #f2fbff;
  font-size: 16px;
  font-weight: 700;
}

.fault-legend-item .type-icon {
  width: 16px;
  height: 16px;
  border-radius: 0;
}

.fault-legend-item .type-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fault-legend-item .type-count {
  color: #ffffff;
  font-style: italic;
  font-weight: 900;
  white-space: nowrap;
}

.edge-info-card {
  min-height: 0;
  flex: 0 0 auto;
}

.edge-stats {
  padding: 14px;
}

.edge-runtime-overview {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 22px;
  padding: 22px 24px 26px;
}

.edge-runtime-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 50px;
  padding: 8px 12px;
  border: 1px solid rgba(78, 168, 255, 0.18);
  border-radius: 7px;
  background: rgba(78, 168, 255, 0.07);
  box-sizing: border-box;
}

.edge-node-status {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.edge-node-status > i {
  flex: 0 0 9px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #46d89a;
  box-shadow: 0 0 0 4px rgba(70, 216, 154, 0.12), 0 0 14px rgba(70, 216, 154, 0.8);
  animation: edge-heartbeat 2.2s ease-in-out infinite;
}

.edge-node-status.warning > i {
  background: #ffb84d;
  box-shadow: 0 0 0 4px rgba(255, 184, 77, 0.12), 0 0 14px rgba(255, 184, 77, 0.8);
}

.edge-node-status.offline > i {
  background: #718392;
  box-shadow: 0 0 0 4px rgba(113, 131, 146, 0.1);
  animation: none;
}

.edge-node-status div {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.edge-node-status span {
  color: rgba(219, 240, 255, 0.58);
  font-size: 10px;
}

.edge-node-status strong {
  max-width: 160px;
  overflow: hidden;
  color: #f0f9ff;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.edge-runtime-meta-right {
  flex: 0 0 auto;
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 4px;
}

.edge-runtime-meta-right time {
  color: rgba(219, 240, 255, 0.54);
  font-family: Consolas, monospace;
  font-size: 10px;
}

.edge-offline-state {
  flex: 1;
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  color: rgba(219, 240, 255, 0.42);
}

.edge-offline-state i {
  margin-bottom: 4px;
  font-size: 34px;
}

.edge-offline-state strong {
  color: rgba(231, 248, 255, 0.72);
  font-size: 14px;
}

.edge-offline-state span {
  font-size: 11px;
}

.edge-link-offline {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 12px 14px 14px;
  border: 1px dashed rgba(116, 224, 245, 0.16);
  border-radius: 7px;
  color: rgba(231, 248, 255, 0.46);
  background: rgba(255, 255, 255, 0.02);
  font-size: 12px;
}

@keyframes edge-heartbeat {
  0%, 100% {
    opacity: 0.72;
    transform: scale(0.92);
  }
  48% {
    opacity: 1;
    transform: scale(1.08);
  }
}

.trend-chart-card .edge-runtime-overview .stat-circle-group {
  grid-template-columns: repeat(4, minmax(86px, 1fr));
  gap: 18px;
  margin-bottom: 4px;
}

.trend-chart-card .edge-runtime-overview .stat-circle .circle-progress {
  width: 96px;
  height: 96px;
}

.trend-chart-card .edge-runtime-overview .stat-circle .circle-progress .circle-inner {
  width: 70px;
  height: 70px;
}

.trend-chart-card .edge-runtime-overview .circle-value {
  font-size: 18px;
  font-weight: 900;
}

.trend-chart-card .edge-runtime-overview .circle-label {
  margin-top: 9px;
  color: #d8edff;
  font-size: 13px;
}

.trend-chart-card .edge-runtime-overview .stat-bar-list {
  gap: 14px;
}

.trend-chart-card .edge-runtime-overview .stat-bar-item {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr) 62px;
  gap: 12px;
}

.trend-chart-card .edge-runtime-overview .bar-label {
  min-width: 0;
}

.trend-chart-card .edge-runtime-overview .bar-container {
  flex: initial;
  height: 16px;
}

.content-wrapper.view-fusion .trend-chart-card {
  flex: 1 1 auto;
  min-height: 0;
}

.content-wrapper.view-fusion .edge-runtime-overview {
  justify-content: center;
  gap: 14px;
  padding: 16px 14px 20px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .stat-circle-group {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 10px;
  margin-bottom: 0;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .stat-circle .circle-progress {
  width: 76px;
  height: 76px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .stat-circle .circle-progress .circle-inner {
  width: 56px;
  height: 56px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .circle-value {
  font-size: 15px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .circle-label {
  margin-top: 6px;
  font-size: 12px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .stat-bar-list {
  gap: 12px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .stat-bar-item {
  grid-template-columns: 58px minmax(0, 1fr) 50px;
  gap: 8px;
  font-size: 12px;
}

.content-wrapper.view-fusion .trend-chart-card .edge-runtime-overview .bar-container {
  height: 12px;
}

.stat-circle-group {
  display: grid;
  grid-template-columns: repeat(4, minmax(70px, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.stat-circle .circle-progress {
  width: 68px;
  height: 68px;
}

.stat-circle .circle-progress .circle-inner {
  width: 50px;
  height: 50px;
  background: rgba(4, 15, 24, 0.82);
}

.stat-circle .circle-label {
  color: var(--screen-muted);
}

.stat-bar-list {
  gap: 10px;
}

.stat-bar-item .bar-container {
  height: 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
}

.stat-bar-item .bar-fill {
  background: linear-gradient(90deg, #4ea8ff, #39d7f5);
  transition: width 0.8s ease;
}

.stat-bar-item .bar-fill.danger {
  background: linear-gradient(90deg, #f5c84b, #ff6b7a);
}

.stat-bar-item .bar-fill.ok {
  background: linear-gradient(90deg, #4ea8ff, #d8edff);
}

.stat-bar-item .bar-value {
  color: var(--screen-text);
}

.heartbeat-icon {
  color: var(--screen-red);
}

.turbine-detail-container.configuration-mode {
  width: 100%;
  height: 100%;
  min-height: 0;
}

.configuration-selection-screen {
  position: relative;
  z-index: 1;
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.configuration-selection-toolbar {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 0 2px 12px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.18);
}

.configuration-selection-title {
  min-width: 220px;
  display: flex;
  align-items: center;
  gap: 11px;
}

.configuration-selection-title .config-route-back {
  width: 34px;
  min-width: 34px;
  padding: 0;
  justify-content: center;
}

.configuration-selection-title > div {
  min-width: 0;
}

.configuration-selection-title span,
.configuration-selection-title strong {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.configuration-selection-title span {
  color: rgba(216, 237, 255, 0.62);
  font-size: 12px;
}

.configuration-selection-title strong {
  margin-top: 4px;
  color: #ffffff;
  font-size: 20px;
  line-height: 1.2;
}

.configuration-selection-selectors {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(136px, 1fr));
  gap: 10px;
}

.configuration-selection-selectors label {
  min-width: 0;
}

.configuration-selection-selectors label > span {
  display: block;
  margin-bottom: 5px;
  color: rgba(216, 237, 255, 0.62);
  font-size: 12px;
}

.configuration-selection-selectors :deep(.el-select) {
  width: 100%;
}

.configuration-selection-selectors :deep(.el-input__wrapper) {
  min-height: 34px;
  border: 1px solid rgba(116, 224, 245, 0.25);
  border-radius: 6px;
  background: rgba(3, 19, 30, 0.76);
  box-shadow: none;
}

.configuration-selection-selectors :deep(.el-input__inner) {
  color: #eefaff;
}

.configuration-selection-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(440px, 0.8fr) minmax(500px, 1.2fr);
  gap: 12px;
}

.configuration-device-panel,
.configuration-point-panel {
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--screen-line);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(10, 45, 62, 0.86), rgba(5, 22, 35, 0.94)),
    rgba(255, 255, 255, 0.02);
  box-shadow: 0 16px 34px rgba(0, 8, 18, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.055);
}

.configuration-panel-heading {
  flex: 0 0 auto;
  min-height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
  background: linear-gradient(90deg, rgba(57, 215, 245, 0.12), rgba(78, 168, 255, 0.06), transparent);
  box-sizing: border-box;
}

.configuration-panel-heading span {
  display: block;
  color: var(--screen-green);
  font-size: 11px;
  font-weight: 700;
  line-height: 1.2;
  text-transform: uppercase;
}

.configuration-panel-heading h2 {
  margin: 3px 0 0;
  color: #ffffff;
  font-size: 17px;
  line-height: 1.2;
}

.configuration-panel-heading > strong {
  flex: 0 0 auto;
  color: var(--screen-cyan);
  font-size: 12px;
}

.configuration-sensing-layout {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 0.94fr) minmax(0, 1.06fr);
  gap: 12px;
  padding: 12px;
}

.configuration-device-stage {
  position: relative;
  min-height: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px 12px 14px;
  overflow: hidden;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(3, 18, 30, 0.84), rgba(4, 27, 40, 0.68)),
    linear-gradient(rgba(116, 224, 245, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(116, 224, 245, 0.03) 1px, transparent 1px);
  background-size: auto, 28px 28px, 28px 28px;
}

.configuration-model-stage {
  position: relative;
  width: min(92%, 320px);
  height: auto;
  max-height: 92%;
  aspect-ratio: 531 / 784;
  filter: drop-shadow(0 18px 28px rgba(0, 8, 18, 0.34));
}

.configuration-gbom-panel {
  height: 100%;
  min-height: 0;
}

.configuration-model-stage .device-model-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  user-select: none;
  pointer-events: none;
}

.configuration-sensor-point {
  position: absolute;
  width: 16px;
  height: 16px;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: transform 0.18s ease;
}

.configuration-sensor-point.active,
.configuration-sensor-point.related {
  z-index: 2;
  transform: translate(-50%, -50%) scale(1.08);
}

.configuration-sensor-point.disabled {
  cursor: default;
}

.configuration-sensor-point .point-dot {
  position: absolute;
  inset: 0;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(4, 18, 28, 0.9);
  border-radius: 50%;
  background: var(--screen-cyan);
  box-shadow: 0 0 12px rgba(57, 215, 245, 0.9);
}

.configuration-sensor-point .point-dot.active {
  background: var(--screen-amber);
  box-shadow: 0 0 15px rgba(245, 200, 75, 0.9);
}

.configuration-sensor-point .point-dot.disabled {
  background: rgba(231, 248, 255, 0.34);
  box-shadow: none;
}

.configuration-sensor-point .point-ring {
  position: absolute;
  left: -7px;
  top: -7px;
  width: 30px;
  height: 30px;
  border: 1px solid rgba(57, 215, 245, 0.72);
  border-radius: 50%;
  animation: pointPulse 1.8s ease-out infinite;
}

.configuration-sensor-point .point-ring.active {
  border-color: rgba(245, 200, 75, 0.9);
}

.configuration-sensor-point .point-ring.disabled {
  display: none;
}

.configuration-point-context {
  flex: 0 0 auto;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
  background: rgba(3, 18, 30, 0.34);
}

.configuration-point-context span,
.configuration-point-context strong,
.configuration-point-context em {
  display: block;
}

.configuration-point-context span {
  color: var(--screen-muted);
  font-size: 11px;
}

.configuration-point-context strong {
  margin-top: 6px;
  color: #ffffff;
  font-size: 18px;
  line-height: 1.25;
  word-break: break-word;
}

.configuration-point-context em {
  margin-top: 6px;
  color: var(--screen-cyan);
  font-size: 11px;
  font-style: normal;
}

.configuration-point-context.empty {
  opacity: 0.7;
}

.configuration-overview-stats {
  flex: 0 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
}

.configuration-overview-stats > div {
  min-width: 0;
  min-height: 82px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 12px 18px;
  box-sizing: border-box;
}

.configuration-overview-stats > div + div {
  border-left: 1px solid rgba(116, 224, 245, 0.12);
}

.configuration-overview-stats span {
  display: block;
  color: var(--screen-muted);
  font-size: 12px;
  line-height: 1.2;
}

.configuration-overview-stats p {
  min-width: 0;
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin: 7px 0 0;
}

.configuration-overview-stats em {
  min-width: 0;
  color: var(--screen-muted);
  font-size: 11px;
  font-style: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.configuration-overview-stats strong {
  flex: 0 0 auto;
  margin: 0;
  color: #ffffff;
  font-size: 28px;
  line-height: 1;
}

.configuration-template-preview-grid {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
}

.configuration-template-preview-section {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 12px;
}

.configuration-template-preview-section + .configuration-template-preview-section {
  border-left: 1px solid rgba(116, 224, 245, 0.12);
}

.configuration-template-preview-title {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 0 2px 9px;
  color: rgba(216, 237, 255, 0.78);
  font-size: 12px;
  font-weight: 700;
}

.configuration-template-preview-title strong {
  color: var(--screen-green);
}

.configuration-template-preview-list {
  flex: 1 1 auto;
  min-height: 0;
}

.configuration-template-preview-empty {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--screen-muted);
  font-size: 12px;
  text-align: center;
}

.configuration-template-preview-row {
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 7px;
  padding: 8px 9px;
  border: 1px solid rgba(116, 224, 245, 0.14);
  border-radius: 6px;
  background: rgba(3, 18, 30, 0.48);
}

.configuration-template-preview-row > div {
  min-width: 0;
}

.configuration-template-preview-row strong,
.configuration-template-preview-row span {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.configuration-template-preview-row strong {
  color: #ffffff;
  font-size: 12px;
}

.configuration-template-preview-row span {
  margin-top: 4px;
  color: var(--screen-muted);
  font-size: 10px;
}

.configuration-template-preview-row > em {
  flex: 0 0 auto;
  padding: 3px 5px;
  border-radius: 4px;
  color: rgba(231, 248, 255, 0.66);
  background: rgba(255, 255, 255, 0.08);
  font-size: 10px;
  font-style: normal;
}

.configuration-template-preview-row > em.is-running {
  color: #06151c;
  background: var(--screen-green);
}

.configuration-template-preview-row > em.is-ready {
  color: #06151c;
  background: var(--screen-cyan);
}

.configuration-template-preview-row > em.is-stopped {
  color: #fff0d1;
  background: rgba(245, 200, 75, 0.24);
}

.configuration-entry-flow {
  flex: 0 0 auto;
  min-height: 64px;
  display: grid;
  grid-template-columns: auto minmax(18px, 1fr) auto minmax(18px, 1fr) auto minmax(18px, 1fr) auto;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.16);
}

.configuration-entry-flow > div {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(216, 237, 255, 0.48);
  font-size: 11px;
  white-space: nowrap;
}

.configuration-entry-flow i {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 50%;
  color: rgba(216, 237, 255, 0.5);
  font-size: 9px;
  font-style: normal;
}

.configuration-entry-flow > div.done,
.configuration-entry-flow > div.active {
  color: #ffffff;
}

.kpi-tile.is-text-value strong {
  max-width: 100%;
  font-size: 18px;
  line-height: 1.15;
  overflow-wrap: anywhere;
}

.configuration-entry-flow > div.done i {
  color: #06151c;
  border-color: var(--screen-green);
  background: var(--screen-green);
}

.configuration-entry-flow > div.active i {
  color: #06151c;
  border-color: var(--screen-cyan);
  background: var(--screen-cyan);
}

.configuration-entry-flow > b {
  height: 1px;
  background: rgba(116, 224, 245, 0.18);
}

.configuration-selected-point {
  flex: 0 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(180px, auto);
  gap: 7px 20px;
  min-height: 112px;
  padding: 18px 20px;
  border-top: 1px solid rgba(116, 224, 245, 0.16);
  background: rgba(3, 18, 30, 0.48);
}

.configuration-selected-point > span {
  color: var(--screen-muted);
  font-size: 11px;
}

.configuration-selected-point > strong {
  grid-column: 1 / -1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #ffffff;
  font-size: 16px;
}

.configuration-selected-point > em {
  color: var(--screen-cyan);
  font-size: 11px;
  font-style: normal;
}

.configuration-selected-point > button {
  grid-column: 2;
  grid-row: 1 / span 3;
  align-self: center;
  min-width: 180px;
  min-height: 48px;
  padding: 0 20px;
  border: 1px solid rgba(78, 168, 255, 0.55);
  border-radius: 6px;
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.configuration-selected-point > button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

:global(.config-workbench-route.el-dialog),
:global(.config-workbench-route .el-dialog) {
  width: 100vw !important;
  height: 100vh;
  margin: 0 !important;
  display: flex;
  flex-direction: column;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

:global(.config-workbench-route .el-dialog__header),
:global(.config-workbench-route.el-dialog .el-dialog__header) {
  display: none;
}

:global(.config-workbench-route .el-dialog__body),
:global(.config-workbench-route.el-dialog .el-dialog__body) {
  flex: 1;
  min-height: 0;
  display: flex;
  padding: 18px 22px 0;
}

:global(.config-workbench-route .el-dialog__footer),
:global(.config-workbench-route.el-dialog .el-dialog__footer) {
  flex: 0 0 auto;
  padding: 14px 22px 16px;
}

:global(.config-workbench-route .config-dialog-body) {
  width: 100%;
  min-height: 0;
  max-height: none;
  height: 100%;
}

.turbine-detail-container:not(.configuration-mode) .workflow-strip,
.turbine-detail-container:not(.configuration-mode) .workflow-panel {
  display: none;
}

.config-route-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 22px;
  padding: 2px 2px 16px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.18);
}

.config-route-title {
  display: flex;
  align-items: center;
  gap: 13px;
  min-width: 220px;
}

.config-route-title > div > span {
  display: block;
  color: rgba(216, 237, 255, 0.62);
  font-size: 12px;
  line-height: 1.2;
}

.config-route-title > div > strong {
  display: block;
  margin-top: 5px;
  color: #ffffff;
  font-size: 22px;
  line-height: 1.1;
}

.config-route-back {
  height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  border: 1px solid rgba(116, 224, 245, 0.3);
  border-radius: 6px;
  color: #d8edff;
  background: rgba(78, 168, 255, 0.1);
  cursor: pointer;
}

.config-route-back:hover {
  color: #ffffff;
  border-color: rgba(143, 199, 255, 0.68);
  background: rgba(78, 168, 255, 0.2);
}

.config-route-selectors {
  display: grid;
  grid-template-columns: repeat(4, minmax(136px, 1fr));
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.config-route-selectors label {
  min-width: 0;
}

.config-route-selectors label > span {
  display: block;
  margin-bottom: 5px;
  color: rgba(216, 237, 255, 0.62);
  font-size: 12px;
}

.config-route-selectors :deep(.el-select) {
  width: 100%;
}

.config-route-selectors :deep(.el-input__wrapper) {
  min-height: 34px;
  border: 1px solid rgba(116, 224, 245, 0.25);
  border-radius: 6px;
  background: rgba(3, 19, 30, 0.76);
  box-shadow: none;
}

.config-route-selectors :deep(.el-input__inner) {
  color: #eefaff;
}

:global(.config-workbench-dialog),
:global(.config-workbench-dialog.el-dialog),
:global(.config-workbench-dialog .el-dialog) {
  border: 1px solid rgba(78, 168, 255, 0.42);
  border-radius: 8px;
  color: #eefaff;
  background:
    linear-gradient(180deg, rgba(9, 36, 58, 0.98), rgba(4, 17, 29, 0.99)),
    linear-gradient(rgba(116, 224, 245, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(116, 224, 245, 0.028) 1px, transparent 1px);
  background-size: auto, 30px 30px, 30px 30px;
  box-shadow: 0 28px 72px rgba(0, 0, 0, 0.48);
  overflow: hidden;
}

:global(.config-workbench-dialog .el-dialog__header),
:global(.config-workbench-dialog.el-dialog .el-dialog__header) {
  margin: 0;
  padding: 20px 26px 16px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.18);
  background:
    linear-gradient(90deg, rgba(78, 168, 255, 0.18), rgba(57, 215, 245, 0.035) 45%, transparent),
    rgba(4, 20, 34, 0.72);
}

:global(.config-workbench-dialog .el-dialog__title),
:global(.config-workbench-dialog.el-dialog .el-dialog__title) {
  color: #f1fbff;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0;
  text-shadow: 0 0 18px rgba(78, 168, 255, 0.32);
}

:global(.config-workbench-dialog .el-dialog__headerbtn),
:global(.config-workbench-dialog.el-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 20px;
}

:global(.config-workbench-dialog .el-dialog__close),
:global(.config-workbench-dialog.el-dialog .el-dialog__close) {
  color: rgba(231, 248, 255, 0.72);
  font-size: 20px;
}

:global(.config-workbench-dialog .el-dialog__body),
:global(.config-workbench-dialog.el-dialog .el-dialog__body) {
  padding: 18px 24px 0;
  background: transparent;
}

:global(.config-workbench-dialog .el-dialog__footer),
:global(.config-workbench-dialog.el-dialog .el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid rgba(116, 224, 245, 0.16);
  background: rgba(3, 16, 27, 0.78);
}

:global(.config-workbench-dialog .el-loading-mask) {
  background: rgba(3, 16, 26, 0.76);
}

.config-dialog-body {
  min-height: 600px;
  max-height: 74vh;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.config-node-card {
  min-height: 92px;
  display: grid;
  grid-template-columns: minmax(220px, 0.72fr) minmax(420px, 1fr) auto;
  align-items: center;
  gap: 18px;
  padding: 16px 18px;
  border: 1px solid rgba(116, 224, 245, 0.24);
  border-radius: 8px;
  background:
    linear-gradient(90deg, rgba(24, 89, 174, 0.44), rgba(7, 29, 46, 0.76)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.045), transparent);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.back-overview-button i {
  font-size: 16px;
}

.back-overview-button i {
  font-size: 16px;
}

.config-node-card span,
.workbench-section-title span {
  display: block;
  color: rgba(231, 248, 255, 0.64);
  font-size: 12px;
}

.config-node-main {
  min-width: 0;
}

.config-node-card strong {
  display: block;
  margin-top: 7px;
  color: #ffffff;
  font-size: 24px;
  line-height: 1.15;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.config-node-flow {
  min-width: 0;
  display: grid;
  grid-template-columns: auto 1fr auto 1fr auto 1fr auto;
  align-items: center;
  gap: 8px;
}

.config-node-flow span {
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  border: 1px solid rgba(143, 199, 255, 0.34);
  border-radius: 999px;
  color: #d8edff;
  background: rgba(78, 168, 255, 0.12);
  font-weight: 800;
  white-space: nowrap;
}

.config-node-flow i {
  height: 1px;
  background: linear-gradient(90deg, rgba(78, 168, 255, 0.72), rgba(57, 215, 245, 0.12));
}

.config-node-card em {
  flex: 0 0 auto;
  justify-self: end;
  min-width: 104px;
  min-height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 13px;
  border-radius: 999px;
  color: #06151c;
  background: #8fc7ff;
  font-style: normal;
  font-weight: 800;
  white-space: nowrap;
}

.config-main-grid {
  min-height: 0;
  flex: 1;
  display: grid;
  grid-template-columns: minmax(300px, 0.34fr) minmax(0, 1fr);
  gap: 16px;
}

.config-template-panel,
.config-editor-panel,
.config-variable-panel,
.config-form-panel {
  min-height: 0;
  border: 1px solid rgba(116, 224, 245, 0.22);
  border-radius: 8px;
  background: rgba(5, 21, 34, 0.84);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.045);
}

.config-template-panel,
.config-editor-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workbench-section-title {
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.14);
  background: rgba(255, 255, 255, 0.025);
}

.workbench-section-title strong {
  color: #8fc7ff;
  font-size: 12px;
  font-weight: 800;
}

.workbench-section-title button {
  min-height: 30px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0 11px;
  border: 1px solid rgba(78, 168, 255, 0.45);
  border-radius: 6px;
  color: #d8edff;
  background: rgba(78, 168, 255, 0.14);
  cursor: pointer;
  white-space: nowrap;
}

.workbench-section-title button span {
  color: inherit;
  font-weight: 800;
}

.config-type-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px 12px 0;
}

.config-type-switch button {
  height: 40px;
  border: 1px solid rgba(116, 224, 245, 0.18);
  border-radius: 6px;
  color: rgba(231, 248, 255, 0.68);
  background: rgba(255, 255, 255, 0.035);
  cursor: pointer;
  font-size: 15px;
  font-weight: 800;
}

.config-type-switch button.active {
  border-color: rgba(78, 168, 255, 0.62);
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
  font-weight: 800;
}

.workbench-sub-actions {
  min-height: 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 0 12px;
  color: rgba(231, 248, 255, 0.56);
  font-size: 12px;
}

.workbench-sub-actions button {
  height: 24px;
  padding: 0 9px;
  border: 1px solid rgba(116, 224, 245, 0.22);
  border-radius: 5px;
  color: #d8edff;
  background: transparent;
  cursor: pointer;
}

.config-template-scroll,
.config-variable-scroll {
  min-height: 0;
  height: 100%;
}

.config-template-item {
  width: calc(100% - 20px);
  min-height: 88px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 6px 10px;
  align-items: center;
  margin: 10px;
  padding: 11px 12px;
  border: 1px solid rgba(116, 224, 245, 0.16);
  border-radius: 7px;
  color: #f1fbff;
  background: rgba(255, 255, 255, 0.035);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.config-template-item:hover,
.config-template-item.active {
  border-color: rgba(78, 168, 255, 0.62);
  background: rgba(24, 89, 174, 0.38);
}

.config-template-item.active {
  transform: translateX(2px);
}

.config-template-item strong,
.config-template-item span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.config-template-item strong {
  grid-column: 1;
  font-size: 15px;
}

.config-template-item span {
  grid-column: 1;
  color: rgba(231, 248, 255, 0.66);
  font-size: 12px;
}

.config-template-item em,
.config-editor-head em {
  justify-self: end;
  min-width: 70px;
  min-height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 9px;
  border-radius: 999px;
  color: rgba(231, 248, 255, 0.72);
  background: rgba(255, 255, 255, 0.07);
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.config-template-item em {
  grid-column: 2;
  grid-row: 1 / span 2;
}

.config-template-item em.is-running,
.config-editor-head em.is-running {
  color: #06151c;
  background: #4ea8ff;
}

.config-template-item em.is-ready,
.config-editor-head em.is-ready {
  color: #06151c;
  background: #39d7f5;
}

.config-template-item em.is-stopped,
.config-editor-head em.is-stopped {
  color: #fff2d8;
  background: rgba(245, 200, 75, 0.22);
}

.config-empty {
  min-height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  color: rgba(231, 248, 255, 0.62);
  text-align: center;
}

.config-empty.large {
  min-height: 100%;
  font-size: 15px;
}

.config-empty strong {
  color: #f1fbff;
  font-size: 18px;
}

.config-empty span {
  color: rgba(231, 248, 255, 0.58);
  font-size: 13px;
}

.config-empty button,
.config-editor-empty button {
  min-height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(78, 168, 255, 0.52);
  border-radius: 6px;
  color: #06151c;
  background: linear-gradient(180deg, #d8edff, #4ea8ff);
  font-weight: 800;
  cursor: pointer;
}

.config-editor-empty {
  background:
    radial-gradient(circle at 50% 32%, rgba(78, 168, 255, 0.14), transparent 36%),
    rgba(5, 21, 34, 0.28);
}

.empty-workflow {
  width: min(560px, 86%);
  display: grid;
  grid-template-columns: auto 1fr auto 1fr auto 1fr auto;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
}

.empty-workflow em {
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 11px;
  border-radius: 999px;
  color: #d8edff;
  background: rgba(78, 168, 255, 0.14);
  font-style: normal;
  font-weight: 800;
}

.empty-workflow i {
  height: 1px;
  background: linear-gradient(90deg, rgba(78, 168, 255, 0.74), rgba(57, 215, 245, 0.16));
}

.config-editor-head {
  min-height: 78px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.14);
}

.config-editor-head span {
  color: rgba(231, 248, 255, 0.64);
  font-size: 12px;
}

.config-editor-head strong {
  display: block;
  margin-top: 5px;
  color: #ffffff;
  font-size: 18px;
  line-height: 1.2;
}

.config-workflow-mini {
  display: grid;
  grid-template-columns: auto 1fr auto 1fr auto 1fr auto 1fr auto;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(116, 224, 245, 0.12);
}

.config-workflow-mini span {
  min-width: 58px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  color: #06151c;
  background: #8fc7ff;
  font-size: 12px;
  font-weight: 800;
}

.config-workflow-mini i {
  height: 1px;
  background: linear-gradient(90deg, rgba(78, 168, 255, 0.72), rgba(57, 215, 245, 0.14));
}

.config-edit-layout {
  min-height: 0;
  flex: 1;
  display: grid;
  grid-template-columns: minmax(260px, 0.38fr) minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
}

.config-variable-panel,
.config-form-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.config-variable-group {
  min-height: 0;
  flex: 1;
  padding: 4px 10px 10px;
  overflow: hidden;
}

.config-variable-item {
  width: 100%;
  min-height: 50px;
  display: flex;
  align-items: center;
  margin: 8px 0;
  padding: 8px 10px;
  border: 1px solid rgba(116, 224, 245, 0.14);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.035);
  box-sizing: border-box;
}

:global(.config-workbench-dialog .config-variable-item .el-checkbox__label) {
  min-width: 0;
  flex: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  color: #eefaff;
}

:global(.config-workbench-dialog .config-variable-item .el-checkbox__label span) {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.config-workbench-dialog .config-variable-item .el-checkbox__label em) {
  color: rgba(231, 248, 255, 0.54);
  font-style: normal;
  font-size: 12px;
}

.config-form {
  min-height: 0;
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.config-form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 2px 14px;
}

:global(.config-workbench-dialog .el-form-item__label) {
  color: rgba(231, 248, 255, 0.7);
  font-size: 12px;
  line-height: 1.2;
}

:global(.config-workbench-dialog .el-input__wrapper),
:global(.config-workbench-dialog .el-select__wrapper) {
  min-height: 34px;
  border-radius: 6px;
  background: rgba(3, 19, 30, 0.84);
  border: 1px solid rgba(116, 224, 245, 0.2);
  box-shadow: none;
}

:global(.config-workbench-dialog .el-input__inner),
:global(.config-workbench-dialog .el-select__placeholder),
:global(.config-workbench-dialog .el-input-number .el-input__inner) {
  color: #f1fbff;
}

:global(.config-workbench-dialog .el-radio__label),
:global(.config-workbench-dialog .el-checkbox__label) {
  color: #d8edff;
}

:global(.config-workbench-dialog .el-input-number) {
  width: 100%;
}

.config-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:global(.config-workbench-dialog .config-dialog-footer .el-button),
:global(.config-workbench-dialog.el-dialog .config-dialog-footer .el-button) {
  min-width: 112px;
  min-height: 42px;
  border-radius: 7px;
  font-weight: 800;
}

@media (max-width: 1180px) {
  .screen-header {
    grid-template-columns: 1fr;
  }

  .configuration-selection-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .configuration-selection-selectors {
    width: 100%;
  }

  .configuration-selection-grid {
    grid-template-columns: minmax(0, 0.82fr) minmax(320px, 1.18fr);
  }

  .screen-controls {
    align-items: stretch;
  }

  .selector-row {
    justify-content: flex-start;
  }

  .right-lower-grid {
    grid-template-columns: minmax(320px, 1fr) minmax(260px, 0.8fr);
  }

  .monitor-status-hero {
    grid-template-columns: 1fr;
  }

  .monitor-runtime-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .monitor-runtime-strip > div:nth-child(3) {
    border-left: 0;
  }
}

@media (max-width: 1180px) {
  .screen-kpis,
  .workflow-strip {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }

  .content-wrapper,
  .content-wrapper.view-flow,
  .content-wrapper.view-diagnosis {
    grid-template-columns: 1fr;
  }

  .model-layout,
  .right-lower-grid {
    grid-template-columns: 1fr;
  }

  .content-wrapper.view-diagnosis .right-panel {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }

  .content-wrapper.view-diagnosis .trend-chart-card {
    grid-row: auto;
  }

  .data-view-workspace {
    grid-template-columns: 1fr;
    overflow: visible;
  }

  .data-query-panel {
    min-height: 280px;
  }

  .config-main-grid,
  .config-edit-layout,
  .config-form-grid {
    grid-template-columns: 1fr;
  }

  .config-route-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .config-route-selectors {
    width: 100%;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .config-dialog-body {
    max-height: 74vh;
  }

  .relation-card {
    grid-template-columns: 1fr;
  }

  .task-chip {
    justify-self: start;
  }
}

@media (max-width: 720px) {
  .turbine-detail-container {
    padding: 10px;
  }

  .screen-device-summary {
    align-items: flex-start;
  }

  .configuration-selection-screen {
    overflow: auto;
  }

  .configuration-selection-selectors,
  .configuration-selection-grid {
    grid-template-columns: 1fr;
  }

  .configuration-selection-grid {
    flex: 0 0 auto;
  }

  .configuration-device-panel {
    min-height: 690px;
  }

  .configuration-sensing-layout {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(320px, 1fr) minmax(300px, 1fr);
  }

  .configuration-point-panel {
    min-height: 720px;
  }

  .configuration-template-preview-grid {
    grid-template-columns: 1fr;
  }

  .configuration-template-preview-section + .configuration-template-preview-section {
    border-top: 1px solid rgba(116, 224, 245, 0.12);
    border-left: 0;
  }

  .configuration-entry-flow {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .configuration-entry-flow > div {
    flex-direction: column;
    text-align: center;
  }

  .configuration-entry-flow > b {
    display: none;
  }

  .configuration-selected-point {
    grid-template-columns: 1fr;
  }

  .configuration-selected-point > button {
    grid-column: 1;
    grid-row: auto;
    width: 100%;
  }

  .screen-device-identity {
    flex: 1;
  }

  .screen-controls,
  .selector-row {
    align-items: stretch;
    flex-direction: column;
  }

  .screen-kpis,
  .workflow-strip {
    grid-template-columns: 1fr;
  }

  .monitor-runtime-overview {
    gap: 16px;
    padding: 18px 14px;
  }

  .monitor-status-copy strong {
    font-size: 22px;
  }

  .monitor-runtime-metrics,
  .monitor-runtime-strip {
    grid-template-columns: 1fr;
  }

  .monitor-runtime-metrics > div + div,
  .monitor-runtime-strip > div + div {
    border-top: 1px solid rgba(116, 224, 245, 0.12);
    border-left: 0;
  }

  .monitor-window-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .monitor-window-track {
    gap: 2px;
  }

  .selector-row,
  .screen-switch,
  .config-workbench-button {
    width: 100%;
  }

  .info-item-select,
  .info-item-select :deep(.el-select) {
    width: 100%;
  }

  .screen-switch button {
    flex: 1;
    min-width: 0;
  }

  .config-workflow-mini {
    grid-template-columns: 1fr;
  }

  .config-workflow-mini i {
    display: none;
  }

  .config-route-title {
    min-width: 0;
  }

  .config-route-selectors {
    grid-template-columns: 1fr;
  }

  :global(.config-workbench-route .el-dialog__body),
  :global(.config-workbench-route.el-dialog .el-dialog__body) {
    padding: 12px 12px 0;
  }

  :global(.config-workbench-route .el-dialog__footer),
  :global(.config-workbench-route.el-dialog .el-dialog__footer) {
    padding: 12px;
  }

  .stat-circle-group {
    grid-template-columns: repeat(2, minmax(70px, 1fr));
  }

  .data-view-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .data-mode-switch {
    width: 100%;
  }

  .data-context-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .data-waveform-meta {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .data-waveform-chart,
  .data-waveform-empty {
    min-height: 300px;
  }
}
</style>
