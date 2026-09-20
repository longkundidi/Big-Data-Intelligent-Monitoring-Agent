<template>
  <div>
    <basic-container>
      <div class="algorithm-center">
        <el-container>
          <el-header class="title">算法中心</el-header>
          <el-container>
            <el-container>
              <el-main>
                <div class="image-with-text">
                  <img src="/img/icon/algorithm-center-1.png" alt="数据清洗转换算法">
                  <div>
                    <span class="number">{{alTotal1}}</span>
                    <br>
                    数据清洗转换算法
                  </div>
                </div>
              </el-main>
              <el-footer>
                已测试：<span class="number">{{dataCleanTested}}</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待测试：<span class="number">{{dataCleanUnTest}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><br>
                已审核：<span class="number">{{dataCleanAudited}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待审核：<span class="number">{{dataCleanUnAudit}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
              </el-footer>
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-main>
                <div class="image-with-text">
                  <img src="/img/icon/algorithm-center-2.png" alt="实时状态评估模型/算法">
                  <div>
                    <span class="number">{{alTotal2}}</span>
                    <br>
                    实时状态评估模型/算法
                  </div>
                </div>
              </el-main>
              <el-footer>
                已测试：<span class="number">{{stateTested}}</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待测试：<span class="number">{{stateUnTest}}</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><br>
                已审核：<span class="number">{{stateAudited}}</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待审核：<span class="number">{{stateUnAudit}}</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
              </el-footer>
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-main>
                <div class="image-with-text">
                  <img src="/img/icon/algorithm-center-3.png" alt="故障诊断模型/算法">
                  <div>
                    <span class="number">{{alTotal3}}</span>
                    <br>
                    故障诊断模型/算法
                  </div>
                </div>
              </el-main>
              <el-footer>
                已测试：<span class="number">{{diagnosisTested}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待测试：<span class="number">{{diagnosisUnTest}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><br>
                已审核：<span class="number">{{diagnosisAudited}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待审核：<span class="number">{{diagnosisUnAudit}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
              </el-footer>
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-main>
                <div class="image-with-text">
                  <img src="/img/icon/algorithm-center-4.png" alt="知识抽取推荐模型/算法">
                  <div>
                    <span class="number">{{alTotal4}}</span>
                    <br>
                    知识抽取推荐模型/算法
                  </div>
                </div>
              </el-main>
              <el-footer>
                已测试：<span class="number">{{extractionTested}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待测试：<span class="number">{{extractionUnTest}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><br>
                已审核：<span class="number">{{extractionAudited}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon><span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>
                待审核：<span class="number">{{extractionUnAudit}}</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon></el-footer>
            </el-container>
          </el-container>
        </el-container>
      </div>
      <br>
    </basic-container>
    <basic-container>
      <div class="config-center">
        <el-container>
          <el-header class="title">配置中心</el-header>
          <el-container>
            <el-container>
              <el-main>
                <div class="image-with-text"  style="position:relative;top:20px;" >
                  <img src="/img/icon/config-center-1.png" alt="配置1">
                  <div>
                    监测部套件： <span class="number">{{componentNum}}</span>
                    <br>
                    监测感知变量数： <span class="number">{{variableNum}}</span>
                  </div>
                </div>
              </el-main>
              <!--              <el-footer>-->
              <!--                配置完成：<span class="number">13</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                配置中：<span class="number">2</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                实例监测：<span class="number">156</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--              </el-footer>-->
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-main>
                <div class="image-with-text">
                  <img src="/img/icon/config-center-2.png" alt="故障监测">
                  <div>
                    监测部套件： <span class="number">{{componentNum}}</span>
                    <br>
                    故障模式： <span class="number">{{faultModeNum}}</span>
                    <br>
                    故障诊断模型： <span class="number">{{alTotal3}}</span>
                  </div>
                </div>
              </el-main>
              <!--              <el-footer>-->
              <!--                配置完成：<span class="number">9</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                配置中：<span class="number">1</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                实例监测：<span class="number">108</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--              </el-footer>-->
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-main>
                <div class="image-with-text" style="position:relative;top:20px;">
                  <img src="/img/icon/config-center-3.png" alt="定时任务">
                  <div>
                    定时任务： <span class="number">2</span>
                  </div>
                </div>
              </el-main>
              <!--              <el-footer>-->
              <!--                配置完成：<span class="number">2</span> <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                配置中：<span class="number">1</span>  <el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>-->
              <!--              </el-footer>-->
            </el-container>
          </el-container>
        </el-container>
      </div>
    </basic-container>
    <basic-container>
      <div class="exe-center">
        <el-container>
          <el-header class="title">执行中心</el-header>
          <el-container>
            <el-container>
              <el-main>
                <div class="image-with-text"  >
                  <img src="/img/icon/exe-center-1.png" alt="配置1">
                  <div>
                    流数据任务执行数： <span class="number">{{runningFlink}}</span><span style="mso-tab-count:1">&nbsp;&nbsp;</span><el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
                    <br>
                    流数据任务排队数： <span class="number">{{totalFlink}}</span><span style="mso-tab-count:1">&nbsp;&nbsp;</span><el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
                  </div>
                </div>
              </el-main>
              <el-footer>
                总插槽数 ：<span class="number">{{totalSlot}}</span>
                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
                可用插槽：<span class="number">{{availableSlot}}</span>
                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>
                <br>
                正常工作数：<span class="number">{{runningFlink}}</span>
                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>
                异常工作数：<span class="number" style="color:red;">{{failedFlink}}</span>
              </el-footer>
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-main>
                <div class="image-with-text">
                  <img src="/img/icon/exe-center-2.png" alt="故障监测">
                  <div>
                    微服务任务执行数： <span class="number">{{alTotal3}}</span><span style="mso-tab-count:1">&nbsp;&nbsp;</span><el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
                    <br>
                    微服务任务排队数： <span class="number">16</span><span style="mso-tab-count:1">&nbsp;&nbsp;</span><el-icon style="top: 3px" size="18"><ArrowRightBold /></el-icon>
                  </div>
                </div>
              </el-main>
              <!--              <el-footer>-->
              <!--                CPU ：<span class="number">{{totalSlot}}</span>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>-->
              <!--                内存：<span class="number">{{availableSlot}}</span>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                <br>-->
              <!--                工作虚拟机数：<span class="number">3</span>-->
              <!--                <span style="mso-tab-count:1">&nbsp;&nbsp;&nbsp;</span>-->
              <!--                异常虚拟机数：<span class="number">0</span>-->
              <!--              </el-footer>-->
            </el-container>
            <el-divider direction="vertical" />
            <el-container>
              <el-container class="warn-info">
                <el-aside width="80px">
                  <img src="/img/icon/exe-center-3.png" alt="故障监测" style="position:relative;top:40px;">
                </el-aside>
                <el-container>
                  <el-header style="text-align: left; height: 40px; padding: 1px; margin-left: 1px;">异常告警信息：</el-header>
                  <el-main >

                    <div class="infinite-list-wrapper" style="overflow: auto; height: 100px;">
                      <!--                      <ul
                                                v-infinite-scroll="load"
                                                class="list"
                                                :infinite-scroll-disabled="disabled"
                                            >
                                              <li v-for="i in count" :key="i" class="list-item">{{ i }}. ***风场22#风机主齿轮箱异响 8:10</li>
                                            </ul>-->
                      <p v-if="loading">Loading...</p>
                      <p v-if="noMore">No more</p>
                    </div>
                    <!--                    <textarea class="text-warn">***风场22#风机主齿轮箱异响 8:10-->
                    <!--***风场1#偏航系统在齿轮箱主轴油温超高 8:35-->
                    <!--***风场33#风机叶片结冰 9:02-->
                    <!--***风场4#风机机舱温度报警  10:35-->
                    <!--***风场26#风机主控系统电流异常 11:15-->
                    <!--                    </textarea>-->
                  </el-main>
                </el-container>
              </el-container>
            </el-container>
          </el-container>
        </el-container>
      </div>
    </basic-container>
  </div>
</template>

<script>

import { computed, ref } from 'vue'
import axios from "@/router/axios"
import {
  fetchStatusCount,
  getComponentCount,
  getFaultModeCount, getJobsCount,
  getVariableCount
} from "@/api/sw/wel";

const load = () => {
  loading.value = true
  setTimeout(() => {
    count.value += 2
    loading.value = false
  }, 2000)
}
export default {
  data() {
    return {
      alTotal1: null,
      alTotal2: null,
      alTotal3: null,
      alTotal4: null,
      dataCleanTested:null,
      dataCleanUnTest: null,
      dataCleanAudited:null,
      dataCleanUnAudit: null,
      stateTested:null,
      stateUnTest:null,
      stateAudited:null,
      stateUnAudit:null,
      diagnosisTested:null,
      diagnosisUnTest:null,
      diagnosisAudited:null,
      diagnosisUnAudit:null,
      extractionTested:null,
      extractionUnTest:null,
      extractionAudited:null,
      extractionUnAudit:null,
      componentNum: null,
      variableNum: null,
      faultModeNum: null,
      totalFlink: null,
      runningFlink: null,
      failedFlink: null,
      totalSlot: null,
      availableSlot: null,
      loading: false,
      count: 10
    };
  },

  computed: {
    noMore() {
      return this.count >= 20;
    },
    disabled() {
      return this.loading || this.noMore;
    }
  },

  created() {
    this.fetchAlTotal1();
    this.fetchAlTotal2();
    this.fetchAlTotal3();
    this.fetchAlTotal4();
    this.fetchClean();
    this.fetchState();
    this.fetchDiagnosis();
    this.fetchExtraction();
    this.fetchConfig();
    this.fetchFlink();
  },
  methods: {
    fetchAlTotal1() {
      axios.get('/al/dataCleaning/getTypeNum/数据清洗算法', {
        params: {
        }
      })
          .then(response => {
            // 请求成功时
            if (response.data.code === '200' && response.data.success) {
              this.alTotal1 = response.data.data[0] + response.data.data[1] + response.data.data[2] + response.data.data[3];
              // console.log('数据清洗转化算法：', this.alTotal1);
            } else {
              console.error('请求失败:', response.data.message);
            }
          })
          .catch(error => {
            // 请求失败时
            console.error('请求失败:', error);
          });
    },
    fetchAlTotal2() {
      axios.get('/al/stateEvaluation/page', {
        params: {
          current: 1,
          size: 100,
          modelTypeFirst: '数字孪生应用算法集',
          modelFunction: '实时状态评估'
        }
      })
          .then(response => {
            // 请求成功时
            if (response.data.code === '200' && response.data.success) {
              this.alTotal2 = response.data.data.total;
              // console.log('实时状态评估算法:', this.alTotal2);
            } else {
              console.error('请求失败:', response.data.message);
            }
          })
          .catch(error => {
            // 请求失败时
            console.error('请求失败:', error);
          });
    },
    fetchAlTotal3() {
      axios.get('/al/alFaultDiagnosisbase/page', {
        params: {
          current: 1,
          size: 100,
          modelTypeFirst: '数字孪生应用算法集',
          modelFunction: '故障诊断'
        }
      })
          .then(response => {
            // 请求成功时
            if (response.data.code === '200' && response.data.success) {
              this.alTotal3 = response.data.data.total;
              // console.log('故障诊断算法:', this.alTotal3);
            } else {
              console.error('请求失败:', response.data.message);
            }
          })
          .catch(error => {
            // 请求失败时
            console.error('请求失败:', error);
          });
    },
    fetchAlTotal4() {
      axios.get('/al/alKnowledgeExtraction/getTypeNum/文本数据提取算法', {
        params: {
        }
      })
          .then(response => {
            // 请求成功时
            if (response.data.code === '200' && response.data.success) {
              this.alTotal4 = response.data.data[0] + response.data.data[1];
              // console.log('知识抽取算法：', this.alTotal4);
            } else {
              console.error('请求失败:', response.data.message);
            }
          })
          .catch(error => {
            // 请求失败时
            console.error('请求失败:', error);
          });
    },

    fetchClean(){
      fetchStatusCount({altype: '数据清洗算法', isTest: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.dataCleanTested = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '数据清洗算法', isTest: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.dataCleanUnTest = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '数据清洗算法', isDeployed: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.dataCleanAudited = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '数据清洗算法', isDeployed: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.dataCleanUnAudit = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
    },

    fetchState(){
      fetchStatusCount({altype: '实时状态评估模型', isTest: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.stateTested = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '实时状态评估模型', isTest: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.stateUnTest = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '实时状态评估模型', isDeployed: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.stateAudited = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '实时状态评估模型', isDeployed: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.stateUnAudit = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
    },

    fetchDiagnosis(){
      fetchStatusCount({altype: '故障诊断模型', isTest: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.diagnosisTested = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '故障诊断模型', isTest: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.diagnosisUnTest = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '故障诊断模型', isDeployed: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.diagnosisAudited = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '故障诊断模型', isDeployed: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.diagnosisUnAudit = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
    },

    fetchExtraction(){
      fetchStatusCount({altype: '知识抽取推荐模型', isTest: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.extractionTested = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '知识抽取推荐模型', isTest: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.extractionUnTest = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '知识抽取推荐模型', isDeployed: true}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.extractionAudited = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
      fetchStatusCount({altype: '知识抽取推荐模型', isDeployed: false}).then( res => {
        if(res.data.code === '200' && res.data.success){
          this.extractionUnAudit = res.data.data
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
    },

    fetchConfig(){
      getComponentCount().then( res => {
        if(res.data.code === 0){
          this.componentNum = res.data.data
        }else {
          console.error('请求失败:', res.data.msg);
        }
      })
      getVariableCount().then( res => {
        if(res.data.code === 0){
          this.variableNum = res.data.data
        }else {
          console.error('请求失败:', res.data.msg);
        }
      })
      getFaultModeCount().then( res => {
        if(res.data.code === 0){
          this.faultModeNum = res.data.data
        }else {
          console.error('请求失败:', res.data.msg);
        }
      })

    },

    fetchFlink(){
      getJobsCount().then( res => {
        if(res.data.code === '200' && res.data.success){
          this.runningFlink = res.data.data.running
          this.failedFlink = res.data.data.failed
          this.totalFlink = this.runningFlink + this.failedFlink
          this.totalSlot = res.data.data.totalSlots
          this.availableSlot = res.data.data.availableSlots
        }else {
          console.error('请求失败:', res.data.message);
        }
      })
    }

  }
};
</script>

<style scoped="scoped" lang="scss">

.algorithm-center {
  position: relative;
  padding: 0  2px;
  font-size: 20px;
  text-align: center;
  color: #333;
}
.config-center {
  position: relative;
  padding: 0  2px;
  font-size: 20px;
  text-align: center;
  color: #333;
}
.exe-center {
  position: relative;
  padding: 0  2px;
  font-size: 20px;
  text-align: center;
  color: #333;
}
.exe-center {
  position: relative;
  padding: 0  2px;
  font-size: 20px;
  text-align: center;
  color: #333;
}

.title {
  text-align: left;
  font-weight: bold;
}

.el-divider {
  height: 150px;
  border-width: 1px;
  border-color: black;
}


.image-with-text {
  display: flex;
  justify-content: center;
  align-items: center;
  line-height: 35px;
  font-size: 18px;
  text-align: left;

}

.image-with-text img {
  margin-right: 20px; /* 调整图片与文字之间的间距 */
}

.image-with-text .number {
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 10px;
}

.el-footer {
  line-height: 30px;
  font-size: 17px;
}

.el-footer .number {
  font-weight: bold;
}

.text-warn {
  border: 1px solid black;
  padding: 1px;
  color: #ff0000;
  width: 450px;
  text-align: left;
}

.warn-info {
  width: 160px;
}
.infinite-list-wrapper {
  border: 1px solid black;
  border-radius: 5px;
  padding: 6px;
  height: 300px;
  text-align: center;
}
.infinite-list-wrapper .list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.infinite-list-wrapper .list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  width: 390px;
  //background: var(--el-color-danger-light-9);
  color: #ed3f14;
}
.infinite-list-wrapper .list-item + .list-item {
  margin-top: 10px;
}

@keyframes blink {
  to {
    opacity: 0;
  }
}
</style>
