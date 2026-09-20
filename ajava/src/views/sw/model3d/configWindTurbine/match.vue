<template>
  <el-dialog
      append-to-body
      :close-on-click-modal="true"
      v-model="visible"
      width="50%"
      @close="closeDialog()"
  >

    <div slot="title" style="display: flex;padding-bottom: 15px">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
            src="arrow.png"
            fit="fill">
        </el-image>
      </div>
      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">数据完整性校验</span>
      </div>
    </div>
    <div v-loading="loading" element-loading-text="加载中..." element-loading-background="rgba(0,0,0,0.3)">
      <div class="app-container">
        <el-steps :active="active" finish-status="success" align-center>
          <el-step title="结构树匹配"></el-step>
          <el-step title="感知变量匹配"></el-step>
        </el-steps>
      </div>


      <div v-if="activeForm === 'one'">
        <!--放零部件匹配信息表格-->
        <el-table :data="componentData" :border="true" :cell-style="{ textAlign: 'center' }"
                  :header-cell-style="tableHeaderColor" style="max-height: 400px; overflow-y: auto;">
          <el-table-column label="风机号">
            <template #default="scope">
              <div>
                <span>{{ scope.row.turbineName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="提示">
            <template #default="scope">
              <div v-if="scope.row.isBomPassed === 1">
                <span>零部件匹配成功</span>
              </div>
              <div v-else>
                <el-popover :visible="infoDlgVisible1" placement="top" :width="516" popper-class="pops">
                  <p style="display: block;height: 5px;margin-top: 20px;font-size: 20px">
                    该风机缺少与元结构树匹配的零部件，请重新导入。</p>
                  <template #reference>
                    <el-button type="text"
                               @click="infoDlgVisible1=true">查看详情
                    </el-button>
                  </template>
                </el-popover>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="匹配状态">
            <template #default="scope">
              <div>
                <el-tag v-if="scope.row.isBomPassed === 1">已匹配</el-tag>
                <el-tag v-else type="danger">未匹配成功</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="active===1 && activeForm === 'two'">
        <!--放感知变量匹配信息表格-->
        <el-table :data="variableData" style="width: 100%; max-height: 400px; overflow-y: auto;" :border="true" :cell-style="{ textAlign: 'center' }"
                  :header-cell-style="tableHeaderColor">
          <el-table-column label="风机号">
            <template #default="scope">
              <div>
                <span>{{ scope.row.turbineName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="提示">
            <template #default="scope">
              <div v-if="scope.row.isValPassed === 1">
                <span>感知变量匹配成功</span>
              </div>
              <div v-else>
                <el-button type="text"
                           @click="infoDlgVisible2=true; getInfoData(scope.row.variableNotMatches) ">查看详情
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="匹配状态">
            <template #default="scope">
              <div>
                <el-tag v-if="scope.row.isValPassed === 1">已匹配</el-tag>
                <el-tag v-else type="danger">未匹配成功</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="active===0" slot="footer" style="text-align: center">
        <el-button style="margin-top: 12px;" class="normalBtn" @click="infoDlgVisible1=false; next()">下一步</el-button>
        <el-button style="margin-top: 12px;" class="normalBtn"
                   @click="visible = false;activeForm = 'one';infoDlgVisible1=false"
                   v-if="active < 1">取消
        </el-button>
      </div>
      <div v-if="active===1" slot="footer" style="text-align: center">
        <el-button style="margin-top:12px;" class="normalBtn" @click="prev">上一步</el-button>
        <el-button v-if="canSubmit" class="normalBtn" @click="confirmSubmit()" style="margin-top:12px">完成
        </el-button>
      </div>
    </div>

    <el-drawer v-model="infoDlgVisible2"
               title="缺少以下感知变量，请重新导入"
               :close-on-click-modal="true"
               :show-close="true"
               width="50%"
               center>
      <el-table :data="infoData" style="width: 100%" :border="true" :cell-style="{ textAlign: 'center' }"
                :header-cell-style="tableHeaderColor">
        <el-table-column label="感知变量">
          <template #default="scope">
            <div>
              <span>{{ scope.row.variables }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="变量所在位置">
          <template #default="scope">
            <div>
              <span>{{ scope.row.nodeName }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </el-dialog>
</template>

<script>
import {generateTasks, getComponentMatch, getVariableMatch} from "@/api/sw/model3d/configModel/table"
import {Burger} from "@element-plus/icons-vue";
import DictTag from "components/DictTag/index.vue";
import {getMetaModelIdByUserId} from "@/api/diagnosis/graphVis/graphVisPro";

export default {
  data() {
    return {
      tableHeaderColor: {
        fontSize: '14px',
        textAlign: 'center',
      },
      active: 0,//步骤条初始化
      visible: false,
      canSubmit: false,
      activeForm: 'one',
      componentData: [],
      variableData: [],
      matchedTurbines: [],
      loading: false,
      currentPage: 1,
      pageSize: 6,
      infoDlgVisible1: false,
      infoDlgVisible2: false,
      proName: '',
      productModel: '',
      info: '',
      windBomLists: [],
      infoData: [],
      metaModelId: 29,
    }
  },
  components: {
    DictTag,
    Burger
  },
  mounted() {
    this.projectInit()
  },

  methods: {
    init(proName, productModel) {
      this.active = 0
      this.visible = true
      this.canSubmit = true
      this.loading = true
      this.proName = proName
      this.productModel = productModel
      this.getComponentMatchList()
      this.componentData = []
      this.variableData = []
      this.infoData = []
    },

    projectInit() {
      const userId = this.$store.state.user.userInfo.userId
      return getMetaModelIdByUserId({ userId, userRole: 'GENERAL_USER' })
          .then(response => {
            if (response.data.code === 0) {
              this.metaModelId = response.data.data.metaModelId;
              console.log("metaModelId:", this.metaModelId);
            }
          });
    },

    //根据项目获取零部件匹配信息
    getComponentMatchList() {

      getComponentMatch({proName: this.proName, productModel: this.productModel, sceneId:this.metaModelId}).then(res => {
        this.componentData = this.sortTurbine(res.data.data)
        this.windBomLists = this.componentData
        this.loading = false
      })
    },

    //根据项目获取感知变量匹配信息
    getVariableMatch() {
      getVariableMatch({proName: this.proName, productModel: this.productModel,  sceneId:this.metaModelId}).then(res => {
        this.variableData = this.sortTurbine(res.data.data)
        this.loading = false
      })
    },

    sortTurbine(turbines){
      return turbines.sort((a, b) => {
        const lastPartA = a.turbineCode.split('-').pop()
        const lastPartB = b.turbineCode.split('-').pop()

        // 如果最后一部分是数字，尝试将其转换为整数进行比较
        const numA = parseInt(lastPartA.match(/\d+/), 10)
        const numB = parseInt(lastPartB.match(/\d+/), 10)

        if (!isNaN(numA) && !isNaN(numB)) {
          return numA - numB
        }

        // 如果无法转换为数字，则按字符串比较
        if (lastPartA < lastPartB) {
          return -1
        }
        if (lastPartA > lastPartB) {
          return 1
        }
        return 0
      })
    },

    // 重置表单
    closeDialog() {

    },

    collectMatchedRows() {
      this.matchedTurbines = this.variableData
          .filter(row => row.isValPassed === 1)
          .map(row => row.turbineCode)
    },

    getInfoData(list) {
      this.infoData = []
      for (const item of list) {
        let stringList = ''
        for (let i = 0; i < item.variables.length; i++) {
          if (i === item.variables.length - 1) {
            stringList = stringList + item.variables[i]
          } else {
            stringList = stringList + item.variables[i] + ' 、'
          }
        }
        this.infoData.push({
          nodeName: item.nodeParent,
          variables: stringList
        })
      }
    },

    //下一页
    next() {
      this.active = 1
      this.activeForm = "two"
      this.loading = true
      this.getVariableMatch()
    },
    //上一页
    prev() {
      if (this.active > 0) this.active = 0;
      this.activeForm = "one"
    },
    //确定按钮
    async confirmSubmit() {
      this.active = 0
      this.activeForm = "one"
      this.visible = false


      //生成状态感知任务
      let nodeIdList = []
      this.windBomLists.forEach(windItem => {
        if (this.matchedTurbines.includes(windItem.turbineCode) && windItem.bomList !== null) {
          windItem.bomList.forEach(item => {
            if (item.leaf === true) {
              nodeIdList.push(item.id)
            }
          })
        }
      })

      if (nodeIdList.length !== 0) {
        await generateTasks(nodeIdList);
      }

      this.$emit("showTree", 1)
    }

  },

  watch: {
    variableData: {
      handler: function (newVal) {
        this.collectMatchedRows();
      },
      deep: true, // 深度监听，以便检测到数组内容的变化
    },
  },
}
</script>
<style>
.no-header-dialog {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-content: center;

}

.el-step__icon {
  top: -1px;
}

.pops {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-content: center;
  height: 150px;
  text-align: center;
  line-height: 150px;

}


</style>
