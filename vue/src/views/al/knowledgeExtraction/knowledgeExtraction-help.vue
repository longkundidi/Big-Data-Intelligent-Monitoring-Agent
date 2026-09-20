<template>
  <el-dialog
    append-to-body
    :close-on-click-modal="false"
    :show-close="true"
    class="el-dialog__header"
    v-model="visible"
  >

    <div slot="title" style="display: flex;padding-bottom: 5%">
      <div slot="title" style="width:4%; display: flex;">
        <el-image
          src="arrow.png"
          fit="fill">
        </el-image>
      </div>
      <div slot="title" style="font-size: 24px;display: flex;padding-left: 1%">
        <span style="">模型使用帮助</span>
      </div>
    </div>

    <div style="display: flex">


      <div style="width: 15%;padding-left: 5%">
        <span style="font-weight: bold;font-size: 19px;color: red">文本数据提取：</span>
        <span style="font-size: 18px">又称自然语言处理，是以语言为对象，利用计算机技术来分析、理解和处理自然语言的一门学科,即把计算机作为语言研究的强大工具，在计算机的支持下对语言信息进行定量化的研究,并提供可供人与计算机之间能共同使用的语言描写。</span>
      </div>



      <div style="padding-left: 5%;display: flex;align-items: center">


          <div style="border-radius: 40px;border: solid 1px black;padding:10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">文本数据提取模型</span>
          </div>

        <div>
          <div style="width: 450px;border: solid 1px black;margin: 10px">
            <span style="font-weight: bold;font-size: 18px;color: blue">1.命名实体识别</span>
            <el-button style="float: right;margin: 5px;padding: 5px;color: white;background-color: #4A9FF9"  @click="see1()" >查看算法</el-button>
            <br/>
            <span style="display:block; text-indent: 2em">又称作“专名识别”，是指识别文本中具有特定意义的实体，主要包括人名、地名、机构名、专有名词等，如高速列车、转向架、中心距等。</span>
          </div>


          <div style="width: 450px;border: solid 1px black;margin: 10px">
            <span style="font-weight: bold;font-size: 18px;color: blue">2.分词模型</span>
            <el-button style="float: right;margin: 5px;padding: 5px;color: white;background-color: #4A9FF9"  @click="see2()">查看算法</el-button>
            <br/>
            <span style="display:block; text-indent: 2em">中文分词是自然语言处理中的一个重要组成部分，它是将连续的汉字序列切分成有意义的词语序列的过程。中文分词模型主要有基于规则、基于统计和基于深度学习的三种。</span>
          </div>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
<!--      <el-button style="width: 10%" v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()">注册</el-button>-->
      <el-button style="width: 10%;" class="normalBtn" @click="visible = false">关闭</el-button>
    </span>

  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      visible: false,
    }
  },
  components: {
  },
  methods: {
    init() {
      this.visible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
      })
    },
    see1()
    {
      this.$emit('label','first')
      this.visible = false
    },
    see2()
    {
      this.$emit('label','second')
      this.visible = false
    },
  }
}
</script>
