<!--  树组件
功能：查询表m3_model_base_tree，实现模型库结构树的编辑与显示
-->
<template>
  <div>
    <lazy-sw-tree class="tyTree" ref="lazyTree" :data="treeData" :accordion="true"
                  :defaultProps="defaultProps" :expandNode="true"
                  :defaultExpandAll="false" nodeCurrentKey='id' :defaultKeys="defaultKeys"
                  :showCheckBox=true :checkStrictly=true
                  @loadTreeNode="loadNode" @checkNode="checkNode" @tynodeclick="handleNodeClick"
                  @eventadd="dlgAddSonNode" @eventupdata="dlgEditNode"
                  @eventDelete="dlgDeleteNode">
    </lazy-sw-tree>
    <el-dialog v-model = "dlgNodeAdd" :title="titleName"
               :before-close="handleClose" :close-on-click-modal="false"
               :width="dlgWidth" @opened="openDialog" draggable>
      <div class="content">
        <div ref="refInputZone" class="input-zone" :class="{'extend-input-zone':!isPreview,'shrink-input-zone':isPreview}">
          <avue-form ref="form" :option="nodeOption" v-model="nodeForm" @submit="handleSubmit">
            <template #uploadComponent="{disabled,size}" >
              <div  class="upload-btn">
                <el-upload ref="uploadRef"
                           :auto-upload="false"
                           :limit="1"
                           :show-file-list="false"
                           action="/api/model3d/minio/uploadFile/modelfile"
                           :headers="headers"
                           :on-change="handleChange"
                           :on-exceed="handleExceed"
                           :on-progress="handleUploadProgress"
                           :on-success="fileUpSuccess"
                           :on-error="fileUpError"

                >
                  <template #trigger>
                    <el-button class="normalBtn">选择文件</el-button>
                  </template>
                </el-upload>
              </div>
            </template>
            <template #progressComponent="{disabled,size}" >
              <div class="progress-tip" v-show="isLoadingTip">
                <el-progress type="circle" width="60" :percentage="loadingPercent" />
              </div>
            </template>
          </avue-form>

        </div>
        <div id="canvas" ref="refModelZone" v-if="isPreview" class="model-zone" :class="{'extend-model-zone':isPreview,'shink-model-zone':!isPreview}"
             :style="{height: modelZoneHeight + 'px'}">
        </div>
      </div>
      <div class="footer-zone" slot="footer">
        <el-button class="normalBtn" style="width: 85px;height: 35px;left: 200px;" @click.stop="$refs.form.submit()">保存</el-button>
        <div style="width: 200px;"></div>
        <el-button ref="refPreviewBtn"  class="normalBtn" style="width: 85px;height: 35px" :disabled="isPreviewDisabled" @click.stop="previewModel()">预览模型</el-button>
      </div>
    </el-dialog>

  </div>
</template>
<script>
  import store from '@/store'
  import lazySwTree from '@/components/myComponent/lazySwTree.vue'
  import { exScene } from '@/util/exThreeJS/exScene.js'


  import { reqRootNode, reqSonNodes, reqObjById, reqAddRootNode,
    reqAddSonNode, putObj, reqParentNodeId, reqDeleteNodes, reqDeleteModel } from '@/api/sw/model3d/modelBaseTree/tree.js'
  import { nodeOption } from '@/const/crud/sw/model3d/modelBaseTree/tree'
  import { ref, onMounted } from 'vue'

  let __tree
  let globeParams = {}     //  声明一个全局参数对象

  export default {
    name: 'modelBaseTree',

    data() {
      return {
        treeData: [],
        defaultProps: {
          children: 'children',
          label: 'name',
          isLeaf: 'leaf'
        },
        defaultKeys: [],
        titleName: '',
        nodeForm: {},
        nodeOption: nodeOption,
        dlgNodeAdd: false,          //  控制新增按钮点击后，弹窗的显示和隐藏

        // 上传文件参数
        dlgWidth: '50%',
        modelZoneHeight: 200,
        isPreview: false,          //   是否预览模型
        uploadUrl: '/model3d/minio/uploadFile/modelfile',      // glb模型文件上传minio的地址
        headers:{
          Authorization:"Bearer "+ store.getters.access_token
        },
        isPreviewDisabled: true,
        isLoadingTip: false,
        loadingPercent: 0
      }
    },
    components: {
      lazySwTree,
    },

    props: {
      mbObj: {}
    },

    setup() {
      let lazyTree = ref(null)        //  生成一个懒加载树的代理对象
      onMounted(()=>{
        __tree = lazyTree


      })
      return { lazyTree }
    },

    methods: {
      // 懒加载
      loadNode(node, resolve) {
        if (node.level === 0) {
          this.getRootNode(resolve)
        }
        if (node.level > 0) {
          this.getSonNodes(node, resolve)
          return resolve([])
        }
      },

      // 根据模板Id，查询根节点
      getRootNode(resolve) {
        let self = this
        if (this.mbObj.mbId){
          reqRootNode(this.mbObj.mbId).then(response => {
            if (response.data.data) {
              let node = response.data.data        //  在结构树上显示编辑按钮
              node.showAdd = true
              node.showRemove = false
              node.showEdit = true
              if (node.modelFileurl === 'noUrl')
                node.disabled = true
              resolve([node])
            }else{          //  首次编辑树，如果没有根节点，使用场景名称添加一个默认根节点
              self.$confirm('首次编辑该模型库, 系统未找到根节点，是否创建根节点?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                reqAddRootNode(self.mbObj.mbName, self.mbObj.mbId).then(response => {         //  添加根节点
                  self.$message.success('系统元模型库名称创建了一个默认根节点')
                  self.rootNodeRefresh (self.mbObj.mbId)
                })
              })
            }
          })
        }
      },

      rootNodeRefresh (mbId) {
        reqRootNode(mbId).then(response => {
          let node = response.data.data
          node.myshow = false
          node.showAdd = true
          node.showRemove = true
          node.showEdit = true
          if (node.modelFileurl === 'noUrl')
            node.disabled = true
          this.treeData = [node]      //  刷新根节点
        })
      },

      // 查询子节点
      getSonNodes (node, resolve) {
        if (node.data.nodeCode !== undefined) {
          reqSonNodes(node.data.nodeCode).then(response => {
            for (const node of response.data.data){     //  在结构树上显示编辑按钮
              node.showAdd = true
              node.showRemove = true
              node.showEdit = true
              if (node.modelFileurl === 'noUrl')
                node.disabled = true        //  禁用节点勾选
            }
            resolve(response.data.data)
          })
        }
      },

      //  弹窗添加子节点
      dlgAddSonNode () {
        this.titleName = "新增子节点"
        this.nodeForm = {              //  节点新增弹框的表单对象
          nodeId: null,
          nodeName: '',
          modelFileurl: 'noUrl',
          modelType: 'noType',
          xcoordinate: 0,
          ycoordinate: 0,
          zcoordinate: 0,
          xrotationAngle: 0,
          yrotationAngle: 0,
          zrotationAngle: 0
        },
        this.dlgNodeAdd = true
      },

      //  弹出新增/编辑对话框的提交按钮
      handleSubmit (nodeForm, done) {
        if (this.titleName === '新增子节点')
          this.nodeAdd(nodeForm, done)

      },

      //  节点子添加
      nodeAdd(nodeForm, done){
        this.isLoadingTip = true        //  显示上传进度提示
        this.$refs.uploadRef.submit()       //  文件上传后台minIo服务器,注意：minIo服务器的请求地址action需要添加 api 前缀
        setTimeout(() => {
          done()
        }, 3000)
      },

      //  用户在本地选择文件后触发
      handleChange(uploadFile, uploadFiles){
        this.nodeForm.modelFileurl = uploadFile.name      //  临时显示用户上传的文件名
        globeParams.uploadFile = uploadFile               //  缓存当前需要上传的模型文件，准备预览
        if (this.isPreview){      //  如果预览窗口打开，加载模型
          globeParams.scene.clearModels()                 //  清除场景内所有模型
          this.isLoadingTip = true
          globeParams.scene.modelHandler.loadModelByFileReader(globeParams.uploadFile.raw, (percent)=>{
            this.loadingPercent = (percent*100).toFixed(1)       //  回调，显示加载进度
          }).then(()=>{
            globeParams.scene.modelGroup.moveToCenter()
            globeParams.scene.moveCameraToModels(2.5)
            globeParams.scene.doRender()      //  模型加载成功后渲染
            this.isLoadingTip = false
          })
        }
      },

      //  每个节点只能上传一个文件，后面选择的文件覆盖前面的。注意：需要配合 :limit="1"
      handleExceed (files){
        this.$refs.uploadRef.clearFiles()
        this.$refs.uploadRef.handleStart(files[0])
      },

      //  窗口提前关闭，消息响应
      handleClose(done){
        globeParams.isInitScene = false     //  threeJS场景没有初始化
        this.isPreview = false
        this.dlgWidth = '50%'
        this.dlgNodeAdd = false
        setTimeout(() => {
          done()
        }, 500)
      },

      //  3D模型文件上传进度显示
      handleUploadProgress(event, file, fileList) {
        this.loadingPercent = ((event.loaded / event.total)*100).toFixed(1)
      },

      /**
       * glb模型文件上传成功时的钩子
       */
      fileUpSuccess(res, file, fileList) {
        this.isLoadingTip = false        //  隐藏上传进度提示
        if (this.nodeForm.nodeName === '')
          this.nodeForm.nodeName = res.data.match(/file\/(.*)\.[^.]+/)[1]      //  如果用户没有输入节点名，正则表达式，取模型文件名
        this.nodeForm.modelFileurl = res.data.match(/9000(.*)/)[1]             //  minio返回结果
        this.nodeForm.modelType = res.data.replace(/.+\./, "")                 //  获取文件后缀名。匹配 . 之前除换行符以外的所有字符, 并替换为""
        this.nodeForm.modelSize = file.size
        let curNodeId = this.$refs.lazyTree.$refs.tree.getCurrentKey()    //  获取当前节点Id
        reqAddSonNode(curNodeId, this.nodeForm).then(data => {     //  数据库添加节点
          this.$message.success('子节点添加成功')
          this.dlgNodeAdd = false
          __tree.value.refreshExpand(curNodeId)             //  刷新树的子节点，并展开子节点
        })

      },

      fileUpError(err, file, fileList) {
        this.$message.error('文件上传失败!')
      },

      previewModel(){       //  动态修改窗口大小和布局
        this.isPreview = !this.isPreview
        if (this.isPreview){
          this.dlgWidth = '70%'       //  如果需要预览模型，窗口扩大至70%
          this.$refs.refPreviewBtn.$el.innerText = '关闭预览'          //  动态修改按钮内容
          if (!globeParams.isInitScene)
            this.__initScene().then(()=>{       //  初始化场景成功后，加载模型
              this.isLoadingTip = true          //  显示进度条
              globeParams.scene.modelHandler.loadModelByFileReader(globeParams.uploadFile.raw, (percent)=>{
                this.loadingPercent = (percent*100).toFixed(1)       //  回调，显示加载进度
              }).then(()=>{
                globeParams.scene.modelGroup.moveToCenter()
                globeParams.scene.moveCameraToModels(2.5)
                globeParams.scene.doRender()      //  模型加载成功后渲染
                this.isLoadingTip = false         //  关闭进度条
              })
            })
        }
        else{
          this.dlgWidth = '50%'
          this.$refs.refPreviewBtn.$el.innerText = '预览模型'
        }
      },

      //    窗口打开后的回调
      openDialog(){
        this.modelZoneHeight = this.$refs.refInputZone.offsetHeight       //  动态设置模型显示区的高度
      },

      __initScene(){
        let self = this
        return new Promise((resolve) => {
          self.$nextTick(() => {        //  等窗口渲染完成后，获取画布的高和宽
            globeParams.canvas = document.getElementById('canvas')
            globeParams.scene = new exScene(globeParams.canvas)
            globeParams.scene.addAmbientLight('#efeeee', 0.5)
            globeParams.scene.addPointLightFollowCamera(4)
            globeParams.scene.setBKColor ('cornflowerblue')
            resolve(globeParams.scene)
          })
        })
      },

      dlgDeleteNode (data) {
        if (data.nodeLevel > 1){
          this.$confirm('此操作将删除当前节点及其所有子节点, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            reqDeleteNodes(data.nodeCode).then(response => {
              if (response.data.code === 500){
                this.$message({
                  type: 'info',
                  message: response.data.message
                })
              }else{
                __tree.value.refreshParentNode(data.id)       //  刷新父节点
                this.$message({
                  type: 'success',
                  message: '删除成功!'
                })
              }
            })
          }).catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            })
          })
        }else{
          this.$message({
            type: 'info',
            message: '根节点只能在项目删除时，进行删除！'
          })
        }
      }




    },

    watch: {
      'nodeForm.modelFileurl': {        //  监视nodeForm.modelFileurl字段，设置按钮的可用性
        handler(newValue, oldValue) {
          if ((newValue === '')||(newValue === 'noUrl'))
            this.isPreviewDisabled = true
          else
            this.isPreviewDisabled = false
        },
        immediate: true,
      }

    }
  }
</script>
<style lang="scss" scoped>
  /*      修改对话框窗体样式     */
  ::v-deep{
    .el-input-number .el-input__inner{        /*    数字类字段居左显示   */
      text-align: left;
    }
    .el-dialog__header{     /*   标题栏背景色   */
      padding: 10px;
      margin-right: 0px;    /*    保持窗体与标题栏宽度一致    */
      background-image: linear-gradient(to bottom,#1e90ff 0, #00ffff 100%);     /*    修改标题色   */
    }
    .el-dialog__title{            /*  标题栏字体样式   */
      font-size: 16px;
      font-weight: bold;
    }
    .el-dialog__headerbtn{        /*  关闭按钮样式   */
      top: 0px;
      right: 0px;
      width: 45px;
      height: 45px;
      /*background-image: linear-gradient(to bottom,#1e90ff 0, #00ffff 100%);*/
    }
    .el-dialog__headerbtn .el-dialog__close{      /*  按钮颜色  */
      color: whitesmoke;
    }
    .el-dialog__headerbtn .el-dialog__close:hover{    /*  鼠标滑过按钮颜色  */
      color: red;
    }
    .el-dialog__body{         /*      窗体内容区     */
      padding-top: 0px;
      padding-bottom: 15px;
    }


  }

  .content{
    width: 100%;
    height: 100%;
    display: flex;
    .input-zone{
      height: 100%;
      .upload-btn{
        width: 145px;
        height: 35px;
        text-align: right;
      }
      .menu-btn{
        width: 100%;
        display: flex;
        justify-content: center;
      }
      .progress-tip{
        width: 100%;
        height: 60px;
        text-align: center;
      }
    }
    .model-zone{
      background-color: #ed3f14;
    }
    .extend-input-zone{
      width: 100%;
    }
    .shrink-input-zone{
      width: 70%;
    }
    .extend-model-zone{
      width: 30%;
      #canvas{
        width: 100%;
      }
    }
    .shink-model-zone{
      width: 0;
    }
  }
  .footer-zone{
    display: flex;
    justify-content: center;          /*   按钮居中    */
  }



</style>

<!--
https://blog.csdn.net/callBack_____/article/details/125215143?share_token=ad0ec4de-3f51-4228-ae97-1186b820c13d-->
