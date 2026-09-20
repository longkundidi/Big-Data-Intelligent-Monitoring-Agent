<!--  ThreeJS模型加载进度条组件  -->
<template>
  <div id="progcontainer">
    <div id="progressbar"></div>
  </div>
</template>
<script>

  export default {
    name: 'progressBar',

    methods: {

      //  获取容器
      getContainer(){
        return document.getElementById('progcontainer')
      },

      //  获取进度条
      getProgressBar(){
        return document.getElementById('progressbar')
      },

      //  刷新进度百分比
      setPercent(percent){
        this.progressbar.style.width = percent * 400 + "px"        //进度条元素长度
        this.progressbar.style.textIndent = percent * 400 + 5 + "px"          //缩进元素中的首行文本
        this.progressbar.innerHTML = Math.floor(percent * 100) + '%'       //进度百分比, Math.floor:小数加载进度取整
      },

      //  隐藏进度条
      hideProgressBar(){
        this.progContainer.style.display = 'none'     //  隐藏进度条
        return false
      },

      //  显示进度条
      showProgressBar(){
        this.progContainer.style.display = 'block'         //  显示进度条
        return true
      },

      //  获取当前进度条的显示状态
      isProgBarShow(){
        if (this.progContainer.style.display === 'block')
          return true
        else
          return false
      }



    },

    mounted () {
      this.progContainer = document.getElementById('progcontainer')
      this.progressbar = document.getElementById('progressbar')
      this.hideProgressBar()          //  默认隐藏进度条
    },
  }
</script>

<style lang="scss" scoped>
  #progcontainer {      /*定义进度条样式*/
    position: absolute;
    width: 400px;
    height: 16px;
    top: 50%;
    left:50%;
    margin-left: -200px;
    margin-top: -8px;
    border-radius: 8px;
    border: 1px solid #18c6a5;
    overflow: hidden;
    z-index: 90;
  }
  #progressbar {
    height: 100%;
    width: 0px;
    background: #18c6a5;
    color: #18c6a5;
    line-height: 15px;
    z-index: 91;
  }
</style>

