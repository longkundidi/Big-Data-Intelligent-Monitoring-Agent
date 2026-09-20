<template>
  <el-container id="containergraph" style="height: 100%;background:rgb(228, 231, 237)">
    <el-container class="containerProHeader">
      <el-header class="selectHeader">
        <el-form class="selectForm" ref="form" :model="formdata" label-width="30%"
                 style="margin-top: 20px; margin-left: 100px">
          <el-form-item class="selectItem">
            <h3>查询条件：</h3>
          </el-form-item>
          <el-form-item class="selectItem" label="项目" prop="name">
            <el-select filterable clearable v-model="formdata.project" :popper-append-to-body="false"
                       :placeholder="formdata.project" size="small">
              <el-option v-for="item in projectList" :key="item.id" :label="item.project" :value="item.id"
                         @click="formdata.programName = item.project; findProductModels(item.project,item.id)">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectItem" label="产品机型" prop="productModel">
            <el-select filterable clearable v-model="formdata.productModel" :popper-append-to-body="false"
                       :placeholder="formdata.productModel" size="small">
              <el-option v-for="item in productModelList" :key="item.id" :label="item.productModel" :value="item.id"
                         @click="formdata.productModel = item.productModel; findBOMs(formdata.programName, item.productModel)">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="selectItem" label="BOM" prop="BOMModel">
            <el-select filterable clearable v-model="formdata.BOMModel" :popper-append-to-body="false"
                       :placeholder="formdata.BOMModel" size="small">
              <el-option v-for="item in BOMModelList" :key="item.id" :label="item.bomModel" :value="item.id"
                         @click="formdata.BOMModel = item.bomModel;">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button class="normalBtn" size="mini" @click="search()">查询</el-button>
          </el-form-item>
        </el-form>
      </el-header>
      <el-container class="containerProMain">
        <el-aside width="13%" class="thisAside">
          <h2 style="margin: 5px 5px 5px 5px">{{ formdata.BOMModel }}</h2>
          <el-scrollbar>
            <el-tree
                v-if="BOMVisible"
                :data="treeData"
                class="lineyes treestyle"
                ref="tree"
                :accordion="true"
                :props="defaultProps"
                :expand-on-click-node="true"
                node-key="id"
                :default-expanded-keys="defaultExpandKeys"
                icon="none"
                :lazy="false"
                currentKey=""
                :show-checkbox="false"
                :check-strictly="true"
                :default-expand-all="false"
                :highlight-current="true"
                @node-click="handleNodeClick"
                @node-expand="handleNodeExpand"
            >
            </el-tree>
          </el-scrollbar>
        </el-aside>
        <el-container class="containerMainAside">
          <el-aside width="22%" class="mainAside">
            <div style="height: 20%;" @click="GLBVisible = true;">
              <img src='../../../assets/diagnosis/fengji.png' class="image" style="height: 100%;width: 100%" alt=""/>
            </div>
            <div v-if="sensoryDataVisible"
                 style="width:300px;margin:0 auto;display: flex;flex-direction: column;justify-content:center;align-items: center;margin-top: 20px;">
              <el-table :data="sensory_data" ref="tableRef" @selection-change="handleSelectionChange"
                        :default-sort="{ prop: 'ordinal', order: 'ascending' }" border
                        style="color: rgb(0, 57, 144) !important;padding: 3px;"
                        :header-row-style="{color:'rgb(0, 57, 144) !important'}"
              >
                <el-table-column type="selection" width="55" align="center"/>
                <el-table-column label="感知变量类型" width="245" align="center">
                  <template #default="scope">{{ scope.row }}</template>
                </el-table-column>
              </el-table>
              <el-button @click="handleGroupReview" plain class="viewBtn"
                         style="width: 150px;margin-top: 20px"
                         size="small"
              >查看数据
              </el-button>
            </div>
          </el-aside>
          <el-main class="thisMain">
            <div style="padding: 5px;display: flex;flex-direction: column;justify-content: center;">
              <div id="myChart" style="width:100%;background:white;height:80vh;">
                   <span style="float: right;" v-show="timePickerVisible">
                        <el-date-picker type="datetimerange" v-model="time" @change="changeData"
                                        placeholder="请选择一个时间" start-placeholder="Start date"
                                        end-placeholder="End date"/>
                   </span>
                <div style="width: 100%;height:700px;float: left;margin-top: 20px" id="prop" v-if="!isnoData"></div>
              </div>
              <div style="width:100%;margin-top: 55px;" v-if="!isnoData">
                <div style="display: flex;justify-content: center">
                  <el-table :data="break_tableData" stripe style="width: 640px;"
                            :header-cell-style="{ 'text-align': 'center', 'background-color': 'rgb(0, 57, 144)', 'color': 'white' }"
                            :cell-style="{ 'text-align': 'center' }">
                    <el-table-column prop="fault_time" label="故障发生时间" width="320"/>
                    <el-table-column prop="fault_name" label="故障数据" width="320"/>
                  </el-table>
                </div>
              </div>
            </div>
          </el-main>
        </el-container>
        <el-dialog :title="glb_title" v-model="GLBVisible" width="50%" @opened="openGLB" :before-close="closeGLB"
                   destroy-on-close>
          <div id="modelzone_open" ref="modelzone_open" style="height: 50vh">
          </div>
        </el-dialog>
      </el-container>
    </el-container>
  </el-container>
</template>

<script>
import {
  getBOMs,
  getFengji,
  getProjects,
  getVariablesByProject,
  getFaultInfoByProjectByFengji,
  getDynamicData,
  getProductModelBySceneName
} from '@/api/diagnosis/graphVis/graphVisPro'
import * as echarts from 'echarts';
import {Search} from "@element-plus/icons-vue";
import {exScene} from '@/api/diagnosis/exThreeJS3/exScene'
import {ElMessage} from "element-plus";
import {reqTreeNodesBySceneId, reqSonNodesBySceneId} from "@/api/sw/model3d/configGbomTree";

export default {
  components: {Search},
  data() {
    return {
      formdata: {
        project: '',
        productModel: '',
        BOMModel: '',
      },
      projectList: [],
      productModelList: [],
      BOMModelList: [],
      treedata: [],
      localDataVisible: true,
      sensoryDataVisible: false,
      isnoData: true,
      selectedItemIndex: -1,
      device_id: '',
      break_tableData: [],
      isSensory_data: true,
      timePickerVisible: false,
      BOMVisible: false,
      sensory_data: '',
      fengjiIdList: [],
      curFengjiNum: 0,
      selectProp: '',
      current_proname: '',
      current_proId: '',
      treeData: [],
      expandKeys: [],
      defaultExpandKeys: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf'
      },
      showTooltip: true,
      globeParams: {},
      time: [],
      GLBVisible: false,
      glb_title: "风机模型查看",
      threeParams_open: {},
      metaModelId: 29,
    }
  },
  mounted() {
    this.projectInit(); //获取项目
    this.localDataVisible = true;
    this.sensoryDataVisible = false; //挂载的时候初始化显示
    this.BOMVisible = false
  },
  methods: {

    projectInit() {
      const userId = this.$store.state.user.userInfo.userId
      //获取项目
      getProjects({userId: userId, userRole: 'GENERAL_USER'}).then(response => {
        if(response.data.code === 0){
          let res = response.data.data
          this.projectList = res.filter((item, index, arr) =>
              index === arr.findIndex((t) => t.project === item.project)
          )
        }
      })
    },

    findProductModels(proName, proId) {
      //切换项目的时候，需要初始化值
      this.treedata = []
      this.localDataVisible = true
      this.sensoryDataVisible = false
      this.BOMVisible = false
      this.timePickerVisible = false
      this.sensory_data = ''
      this.isnoData = true

      this.current_proname = proName
      this.current_proId = proId
      this.formdata.productModel = '';
      this.formdata.BOMModel = '';
      getProductModelBySceneName(proName).then(response => {
        this.productModelList = response.data.data;
      })
    },

    findBOMs(proName, modId) {
      //切换机型的时候，需要初始化值
      this.localDataVisible = true;
      this.sensoryDataVisible = false;
      this.BOMVisible = false
      this.formdata.BOMModel = '';

      getBOMs(proName, modId).then(r => {
        this.BOMModelList = r.data.data
      })
    },

    search() {
      this.time = []
      this.sensoryDataVisible = true
      this.BOMVisible = true
      this.timePickerVisible = true
      this.setTime()
      this.clearSearchData();

      getFengji(this.formdata.project).then(r => {
        this.fengjiIdList = r.data.data
        this.getSensory();
      })

      let nodeLevel = 1
      reqTreeNodesBySceneId(this.metaModelId, nodeLevel).then(response => {
        this.treeData = []
        this.expandKeys = []            //  缓存待扩展的节点
        let rootNodes = response.data.data.filter(ele => ele.nodeType === "Root" || ele.nodeType === "Root-Leaf")

        for (let i = 0; i < this.fengjiIdList.length; i++) {
          for (let item of rootNodes) {
            item.name = '风机号-' + this.fengjiIdList[i]
            this.expandKeys.push(item.id)
            this.setChildren(item, response.data.data)
            this.treeData.push(JSON.parse(JSON.stringify(item)))
          }
          this.defaultExpandKeys = this.expandKeys //  扩展节点
        }
      })
    },
    clearSearchData() {
      this.treedata = [];
      this.fengjiIdList = [];
      this.curFengjiNum = '';
    },

    setTime() {
      let date = new Date()
      // 通过时间戳计算
      let defalutStartTime = date.getTime() - 7 * 24 * 3600 * 1000 // 转化为时间戳
      let defalutEndTime = date.getTime()
      let startDateNs = new Date(defalutStartTime)
      let endDateNs = new Date(defalutEndTime)
      // 月，日 不够10补0
      defalutStartTime = startDateNs.getFullYear() + '-' + ((startDateNs.getMonth() + 1) >= 10 ? (startDateNs.getMonth() + 1) : '0' + (startDateNs.getMonth() + 1)) + '-' + (startDateNs.getDate() >= 10 ? startDateNs.getDate() : '0' + startDateNs.getDate() - 2)
      defalutEndTime = endDateNs.getFullYear() + '-' + ((endDateNs.getMonth() + 1) >= 10 ? (endDateNs.getMonth() + 1) : '0' + (endDateNs.getMonth() + 1)) + '-' + (endDateNs.getDate() >= 10 ? endDateNs.getDate() : '0' + endDateNs.getDate() - 2)
      this.time = [defalutStartTime, defalutEndTime]
    },

    /* 检查一个节点的子节点中，是否有待加载（Loading）状态的子节点
    * 这种情况只会发生在第二层节点复制后，它的子节点没有加载的情况下。这时，虽然节点层级小于globeParams.nodeLevel，但是仍然需要查询后台
    */
    _sonNodeHasLoading(node) {
      let hasLoading = false
      for (let i = 0; i < node.children.length; i++) {
        if (node.children[i].id === 'loading') {
          hasLoading = true
          break
        }
      }
      return hasLoading
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

    //正则表达式，根据节点编码，查找它的下一层子节点。将节点列表nodeList分解为2个数组：sonNodes——pNodeCode的子节点；otherNodes非子节点
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

//  节点点击消息响应
    handleNodeClick(node) {

      if (node.nodeType === 'Root') {
        this.getSensoryByNode(node)
      }

    },
//  节点扩展消息响应
    handleNodeExpand(data, node) {
      if ((node.level >= this.globeParams.nodeLevel) || (this._sonNodeHasLoading(data))) {
        reqSonNodesBySceneId(this.metaModelId, data.nodeCode).then(response => {
          if (response.data.data) {
            response.data.data.forEach((item) => {
              item.children = item.leaf ? [] : [{id: 'loading', name: '节点加载中...'}]
            })
            node.data.children = response.data.data
          }
        })

      }
    },

    getSensory() {
      getVariablesByProject(this.current_proId).then(r => { //获取项目对应的感知变量
        if (r.data.code === "200") {
          this.sensory_data = r.data.data
        }
      })
    },
    getSensoryByNode(node) {
      this.isnoData = false
      this.selectedItemIndex = 0;

      //获取点击的风机号
      const pattern1 = /\d+/;
      const match1 = node.name.match(pattern1);
      if (match1) {
        this.device_id = match1[0];
      }
      getFaultInfoByProjectByFengji(this.current_proId, this.device_id).then(r => {
        //获取故障数据
        if (r.data.code === "200") {
          this.break_tableData = r.data.data
        }
      })
    },

    handleSelectionChange(row) {  //感知变量多选操作
      this.selectProp = row  //获取多选结果
    },

    handleGroupReview() { //组合查看感知变量对应图表
      this.isnoData = false
      this.changeData(this.time)
    },

    changeData(newTime) {  //时间选择器
      // if (newTime == '') {
      //   let date = new Date()
      //   // 通过时间戳计算
      //   let defalutStartTime = date.getTime() - 7 * 24 * 3600 * 1000 // 转化为时间戳
      //   let defalutEndTime = date.getTime()
      //   let startDateNs = new Date(defalutStartTime)
      //   let endDateNs = new Date(defalutEndTime)
      //   // 月，日 不够10补0
      //   defalutStartTime = startDateNs.getFullYear() + '-' + ((startDateNs.getMonth() + 1) >= 10 ? (startDateNs.getMonth() + 1) : '0' + (startDateNs.getMonth() + 1)) + '-' + (startDateNs.getDate() >= 10 ? startDateNs.getDate() - 2 : '0' + startDateNs.getDate() - 2)
      //   defalutEndTime = endDateNs.getFullYear() + '-' + ((endDateNs.getMonth() + 1) >= 10 ? (endDateNs.getMonth() + 1) : '0' + (endDateNs.getMonth() + 1)) + '-' + (endDateNs.getDate() >= 10 ? endDateNs.getDate() - 2 : '0' + endDateNs.getDate() - 2)
      //   this.time = [defalutStartTime, defalutEndTime]
      //   newTime = [defalutStartTime, defalutEndTime]
      // }
      let startTime = newTime[0];
      let endTime = newTime[1];

      let s = startTime ? new Date(startTime) : null;
      let e = endTime ? new Date(endTime) : null;
      if (s) {
        s.setDate(s.getDate() + 1);
        s = s.toISOString();
      }

      // 如果结束时间不为空，则增加一天
      if (e) {
        e.setDate(e.getDate() + 1);
        e = e.toISOString();
      }
      const beijingOffset = 8 * 60 * 60 * 1000; // 东八区的偏移量（毫秒）

      // let reqs = [];
      let sensors = [];
      let times = [];

      let project = "Qiduntan"
      if (this.current_proname === "陕西大唐靖边天赐湾") {
        project = "Tianciwan"
      }
      if(this.current_proname === "中广核河南永城汉兴") {
        project = "Yongcheng"
      }
      if(this.current_proname === "普格海口风场") {
        project = "Puge"
      }
      let data_processed = []

      getDynamicData({
        project: project,
        deviceId: this.device_id,
        startTime: s,
        endTime: e
      }).then(res => { //获取所有感知变量数据
        const {data} = res.data
        if (!this.selectProp) {
          return null;
        }
        this.selectProp.forEach(variableName => {
          if (variableName === "三相电流") {
            sensors.push("A相电流", "B相电流", "C相电流")
          }
          if (variableName === "三相电压") {
            sensors.push("A相电压", "B相电压", "C相电压")
          }
          if (variableName === "机舱温度") {
            sensors.push("机舱温度")
          }
          if (variableName === "三相绕组温度") {
            sensors.push("U相绕组温度", "V相绕组温度", "W相绕组温度")
          }
          if (variableName === "低速轴转速") {
            sensors.push("低速轴转速")
          }
          if (variableName === "有功功率") {
            sensors.push("有功功率")
          }
          if (variableName === "主轴前轴承温度") {
            sensors.push("主轴前轴承温度")
          }
          if (variableName === "主轴后轴承温度") {
            sensors.push("主轴后轴承温度")
          }
        })
        if (data.length === 0) {
          ElMessage({
            message: "该时间段暂无数据，请重新选择时间段",
            type: 'warning'
          })
          return null;
        }

        data_processed = data.filter(item => sensors.includes(item.variable));  //只过滤包含所选感知变量的数据
        let groupedData = data_processed.reduce((newdata, olddata) => {  //按照感知变量类型进行分组
          const key = olddata.variable;
          if (!newdata[key]) {
            newdata[key] = [];
          }
          newdata[key].push(olddata);
          return newdata;
        }, {});

        let firstKey = Object.keys(groupedData)[0];
        if (!groupedData[firstKey]) {
          return null;
        }
        groupedData[firstKey].forEach(item => {
          //处理时间
          const date = new Date(item.mp_time);
          const beijingTime = new Date(date.getTime() + beijingOffset);
          times.push(beijingTime.toLocaleString('zh-CN', {timeZone: 'Asia/Shanghai'}));
        })

        let chartDom = document.getElementById('prop');
        let myChart = echarts.init(chartDom);


        let option = {
          legend: {
            data: sensors
          },
          dataZoom: [
            {
              start: 50,
              end: 100
            }
          ],

          tooltip: {
            trigger: 'axis',
            position: function (pt) {
              return [pt[0], '10%'];
            }
          },

          xAxis: {
            type: 'category',
            data: times,
          },

          yAxis: {
            type: 'value'
          },
          series: []
        };

        // let i = 0
        for (let key in groupedData) {
          let values = [];
          groupedData[key].forEach(item => {
            values.push(item.mp_data)
          })

          option.series.push({
            name: key,
            data: values,
            type: 'line'
          })
          // i = i + 1
        }
        option && myChart.setOption(option, true);
      })
    },

    openGLB() {
      this.threeParams_open.factor = Math.PI / 180      //  角度转弧度
      this.threeParams_open.scale = 0.001                //  位置坐标缩小1000
      this.threeParams_open.theScene = new exScene('modelzone_open')     //新建场景
      // threeParams.theScene.showWorldAxis(50)              //  显示世界坐标系（暂时不需要）
      let address = 'http://192.168.16.216/group1/M00/00/11/wKgQ2GVdz_aAZVTjAMgjyPj66FA155.glb'    //导入glb模型
      this.threeParams_open.theScene.modelHandler.loadModelByHttpUrl(address, prog => {
      }).then(glb => {
        this.threeParams_open.theScene.modelGroup.add(glb)        //  将模型对象添加到场景
        this.threeParams_open.theScene.doRender()      //  执行场景渲染
      })
    },
    closeGLB() {
      this.threeParams_open.theScene.clearModels()
      this.threeParams_open = {}
      this.GLBVisible = false
    }
  }
}


</script>

<style scoped>
.containerProMain {
  height: 90%;
  width: 100%;
}

.top-page {
  background-color: #d3d3d3;
  background-size: cover;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  width: 100%;
  justify-content: space-between;
  height: 40px;
  line-height: 40px;
  margin: 5px;
  padding-left: 0;
}

.common-layout {
  margin-top: 5px;
}

/deep/ .el-main {
  padding-top: 4px;
}

.thisMain {
  background: white;
  height: 100%;
}

/deep/ .el-aside i {
  color: rgb(0, 57, 144) !important;
  margin-right: 0px !important;
}

.thisAside {
  background: rgba(255, 255, 255, 0.64);
  height: 800px !important;
  overflow-y: auto;
  border: 1px solid rgba(43, 43, 43, 0.98);
}

.containerMainAside {
  margin-left: 5px;
  height: 800px;
  border: 1px solid rgba(43, 43, 43, 0.98);
}

.mainAside {
  background: rgba(255, 255, 255, 0.64);
  height: 794px !important;
  margin: 2px 2px 2px 2px;
  border: 1px solid rgba(43, 43, 43, 0.98);
}

.changeBtn {
  background-color: transparent !important;
  color: rgb(0, 57, 144) !important;
  font-size: 12px !important;
  padding: 1px 2px 1px 2px !important;
  border-color: rgb(0, 57, 144) !important;
  width: 25%;
}

.changeBtn:hover {
  background-color: rgb(0, 57, 144) !important;
  color: white !important;
  font-size: 12px !important;
  padding: 1px 2px 1px 2px !important;
  border-color: rgb(0, 57, 144) !important;
  width: 25%;
}

/deep/ .el-form-item .el-input__inner {
  width: 97% !important;
}

/deep/ .el-input__suffix {
  right: 3%;
}

.selectHeader {
  flex: 1;
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  width: 80%;
  justify-content: space-between;
  height: 3%;
  line-height: 5%;
  margin: 1%;
  align-items: center;
}

.selectForm {
  width: 100%;
  display: flex;
  text-align: center;
  align-items: center;
  justify-content: space-between;
}

.selectItem {
  width: 20%;
}

:deep(.treestyle .el-tree-node) {
  position: relative;
  padding-left: 16px;
/ / 需要配合: indent = "0" ，才能保证竖线对齐
}

:deep(.treestyle .el-tree) {
  background-color: Transparent; /*背景透明*/
  color: #212020; /*字体颜色：黑灰色*/
}

:deep(.treestyle .el-tree-node__expand-icon.is-leaf) { /* 叶子节点隐藏图标  */
  display: none;
}

/*  下面的样式设置与连线有关    */
:deep(.treestyle .el-tree-node__children) {
  padding-left: 18px;
}

:deep(.treestyle .el-tree-node :last-child:before) {
  height: 38px;
}

.treestyle .el-tree > .el-tree-node:before {
  border-left: none;
}

.treestyle .el-tree > .el-tree-node:after {
  border-top: none;
}

:deep(.treestyle .el-tree-node:before) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.treestyle .el-tree-node:after) {
  content: "";
  left: -4px;
  position: absolute;
  right: auto;
  border-width: 1px;
}

:deep(.lineyes .el-tree .el-tree-node__expand-icon.expanded) { /*节点图标不旋转*/
  -webkit-transform: rotate(0deg);
  transform: rotate(0deg);
}

:deep(.lineyes .el-tree-node__expand-icon) {
  font-size: 16px; /*图标大小*/
}

:deep(.lineyes .el-tree-node__expand-icon:before) { /*有子节点 且未展开*/
  content: "";
  background: url("/img/kgtree/circleplus.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__expand-icon.expanded:before) { /*有子节点 且已展开*/
  content: "";
  background: url("/img/kgtree/remove.svg") no-repeat 0 0px;
  display: block;
  width: 16px;
  height: 16px;
  background-size: cover;
}

:deep(.lineyes .el-tree-node__content:hover) { /*鼠标滑过，修改背景色*/
  color: cyan;
  font-weight: bold;
  background-color: rgb(108, 108, 111) !important;
}

.lineyes .el-tree-node:focus > .el-tree-node__content { /*节点选中，节点获取焦点*/
  color: gold;
  font-weight: bold;
  background-color: rgba(138, 194, 252, 0.53) !important;
}

.lineyes .el-tree-node.is-current > .el-tree-node__content {
  color: gold;
}

:deep(.lineyes .el-tree-node:before) { /*显示节点间连接的竖线*/
  border-left: 1px dashed #dcdcdc;
  bottom: 0px;
  height: 100%;
  top: -26px;
  width: 3px;
}

:deep(.lineyes .el-tree-node:after) { /*显示节点间连接的横线*/
  border-top: 1px dashed #dcdcdc;
  height: 20px;
  top: 12px;
  width: 24px;
}

.el-dialog__header .el-dialog__title {
  font-size: 30px;
}


</style>
