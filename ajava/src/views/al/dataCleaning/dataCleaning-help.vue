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
        <span style="">算法使用帮助</span>
      </div>
    </div>

    <div style="display: flex">

      <div style="width: 15%;padding-top:10%">
        <span style="font-weight: bold;font-size: 19px;color: red">数据清洗：</span>
        <span style="font-size: 18px">是指对采集到的原始数据进行去重、去除异常值等操作,以确保
        后续处理和分析的准确性。数据清洗是数据采集处理过程中不可或缺的一环，其结果直接影响后续分析的准确性</span>
      </div>

      <div style="padding-left: 5%;display: flex;flex-direction: column;align-items: center">


        <div style="display: flex;align-items: center">
          <div style="border-radius: 40px;border: solid 1px black;padding:10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">原始数据</span>
          </div>
          <div style="width: 100px;height: 2px;background-color: white"></div>
          <div style="width: 450px;border: solid 1px white"></div>
        </div>

        <div style="width: 2px;height: 25px;background-color: black;margin-right: 83%;display: inline-block"></div>

        <div style="display: flex;align-items: center">
          <div style="border-radius: 40px;border: solid 1px black;padding:23px 10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">数据去重复</span>
          </div>
          <div style="width: 100px;height: 2px;background-color: black"></div>
          <div style="width: 450px;border: solid 1px black">
            <span style="font-weight: bold;font-size: 18px;color: blue">1.数据去重</span>
            <el-button style="float: right;margin: 5px;padding: 5px;color: white;background-color: #4A9FF9"  @click="see1()" >查看算法</el-button>
            <br/>
            <span style="display:block; text-indent: 2em">目前清晰重复值的基本思想是"排序和合并"。清洗重复值的方法主要有相似度计算和基于近邻排序算法等方法。</span>
          </div>
        </div>


        <div style="width: 2px;height: 25px;background-color: black;margin-right: 83%"></div>


        <div style="display: flex;align-items: center">
          <div style="border-radius: 40px;border: solid 1px black;padding:23px 10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">缺失值填充</span>
          </div>
          <div style="width: 100px;height: 2px;background-color: black"></div>
          <div style="width: 450px;border: solid 1px black">
            <span style="font-weight: bold;font-size: 18px;color: blue">2.缺失值填充</span>
            <el-button style="float: right;margin: 5px;padding: 5px;color: white;background-color: #4A9FF9"  @click="see2()">查看算法</el-button>
            <br/>
            <span style="display:block; text-indent: 2em">使用最接近缺失值的值替代缺失的值，包括使用属性的平均值、中间值等，或使用最可能的值(即通过回归等)填充缺失值。</span>
          </div>
        </div>

        <div style="width: 2px;height: 25px;background-color: black;margin-right: 83%"></div>

        <div style="display: flex;align-items: center">
          <div style="border-radius: 40px;border: solid 1px black;padding:23px 10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">异常值检测</span>
          </div>
          <div style="width: 100px;height: 2px;background-color: black"></div>
          <div style="width: 450px;border: solid 1px black">
            <span style="font-weight: bold;font-size: 18px;color: blue">3.异常值检测</span>
            <el-button style="float: right;margin: 5px;padding: 5px;color: white;background-color: #4A9FF9"  @click="see3()">查看算法</el-button>
            <br/>
            <span style="display:block; text-indent: 2em">异常值是指与其他观测值明显不同的数据点，检测和处理异常值对于确保数据质量和分析结果的准确性至关重要。</span>
          </div>
        </div>


        <div style="width: 2px;height: 25px;background-color: black;margin-right: 83%"></div>


        <div style="display: flex;align-items: center">
          <div style="border-radius: 40px;border: solid 1px black;padding:23px 10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">数据标准化</span>
          </div>
          <div style="width: 100px;height: 2px;background-color: black"></div>
          <div style="width: 450px;border: solid 1px black">
            <span style="font-weight: bold;font-size: 18px;color: blue">4.数据标准化</span>
            <el-button style="float: right;margin: 5px;padding: 5px;color: white;background-color: #4A9FF9"  @click="see4()">查看算法</el-button>
            <br/>
            <span style="display:block; text-indent: 2em">为消除特征之间量纲和取值范围的差异可能造成的影响，需要对数据进行标准化处理。</span>
          </div>
        </div>

        <div style="width: 2px;height: 25px;background-color: black;margin-right: 83%"></div>

        <div style="display: flex;align-items: center">
          <div style="border-radius: 40px;border: solid 1px black;padding: 10px;background-color: #2ad02a">
            <span style="font-size: 20px;color: black">清理后数据</span>
          </div>
          <div style="width: 100px;height: 2px;background-color: white"></div>
          <div style="width: 450px;border: solid 1px white"></div>
        </div>

      </div>
    </div>

    <span slot="footer" class="dialog-footer" style="display: flex;justify-content: center">
<!--      <el-button style="width: 10%" v-if="canSubmit" class="normalBtn" @click="dataFormSubmit()">注册</el-button>-->
      <el-button class="normalBtn" style="width: 10%" @click="visible = false">关闭</el-button>
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
    see3()
    {
      this.$emit('label','third')
      this.visible = false
    },
    see4()
    {
      this.$emit('label','fourth')
      this.visible = false
    },
  }
}
</script>
