import * as THREE from 'three'
import { exThreeGroup } from './exThreeGroup.js'
import { exModel } from './exModel.js'
import { exLight } from './exLight.js'
import { exControl } from './exControl.js'

/**
 * 扩展threeJS场景类
 */
export class exScene extends THREE.Scene {

    //  构造函数
    constructor(modelzone) {
        super()
        this.name = '我的场景'
        this.canvas = document.getElementById(modelzone)

        this.exLight = new exLight(this)
        this.exLight.addAmbientLight("#fff", 1)   //  默认添加一个环境光

        this.exLight.addDefaultDirectionalLight()
        this.exLight.addPointLightFollowCamera()

        //   1. 添加默认相机
        let width = this.canvas.offsetWidth         //窗口宽度
        let height = this.canvas.offsetHeight       //窗口高度
        this.camera = new THREE.PerspectiveCamera(45, width / height, 0.01, 2000)
        this.camera.position.set(0, 0, 4.5)
        //   2. 创建渲染器
        this._renderer = new THREE.WebGLRenderer({
            antialias: true,                        //  抗锯齿
        })
        this._renderer.setSize(width, height)       // 设置渲染区域尺寸
        this._renderer.setClearColor( '#ebeef5' )
        this._renderer.toneMapping = THREE.ReinhardToneMapping
        this._renderer.toneMappingExposure = 2.3
        this.clearCanvas()
        this.canvas.appendChild(this._renderer.domElement)    //向画布元素中插入canvas对象
        //   3. 初始化控制器
        this.controls = new exControl(this.camera, this._renderer.domElement)   //  默认正交控制器
        //   4. 监听鼠标、键盘事件
        this.controls.addEventListener('change', this.doRender.bind(this))
        window.addEventListener('resize', this.doRender.bind(this))
        //   5. 添加一个模型组
        this.modelGroup = new exThreeGroup()
        this.add(this.modelGroup)
        //   6. 实例化一个模型对象
        this.modelHandler = new exModel(this)
        this.doRender()             //  执行一次渲染
    }


    /*_animate() {
      this._renderer.setAnimationLoop()
    }*/

    //  手动渲染
    doRender() {
        if (this.pointLight)
            this.pointLight.position.set(this.camera.position.x + this.pointLight.bias.x,
              this.camera.position.y + this.pointLight.bias.y,
              this.camera.position.z + this.pointLight.bias.z)   //  点光源跟随相机移动
        this._renderer.render(this, this.camera)    //指定场景、相机作为参数

    }

    //  手动清除画布
    clearCanvas(){
        if (this.canvas.childNodes.length >= 1)
            this.canvas.removeChild(this.canvas.firstChild)
    }

    //  设置场景背景色
    setBKColor (color){
        let bkColor = color
        if (bkColor !== undefined)
            this._renderer.setClearColor(color, 1.0)
        else
            this.theRenderer.setClearColor(this._random16Color(), 1.0)      //  产生一个随机背景色
        return bkColor
    }

    showWorldAxis(size) {
      let s = size ? size : 50
      this.add(new THREE.AxesHelper(size ? size : 50))
    }

    //  将模型组移动到坐标原点，并将相机移动到模型附近，相机瞄准坐标原点
    moveToCenter(distanceFacter){debugger
      let sceneBox = this.modelGroup.moveToCenter().sceneBox  //  将模型组移动到场景中心
      this.moveCameraToModels(distanceFacter, sceneBox)       //  设置相机位置到模型组附近
    }

    //  移动相机到模型组（modelGroup）附近
    moveCameraToModels(distanceFacter, box){
        let sceneBox = box ? box : this.modelGroup.calcSceneBox()     //  计算模型场景的包围盒
        let target = new THREE.Vector3(0,0,0)
        //  计算相机位置
        let maxLen = Math.max(sceneBox.max.x - sceneBox.min.x, sceneBox.max.y - sceneBox.min.y, sceneBox.max.z - sceneBox.min.z)    //  计算最大边长
        let rr = maxLen + Math.log(1 + maxLen) * distanceFacter       //  包围球半径
        let dd = Math.cos(30*Math.PI/180)*rr
        let xx = dd * Math.cos(45*Math.PI/180) + target.x       //  相机X坐标
        let yy = Math.sin(30*Math.PI/180)*rr + target.y         //  相机Y坐标
        let zz = dd * Math.sin(45*Math.PI/180) + target.z       //  相机Z坐标
        this.camera.position.set(xx, yy, zz)
        this.camera.lookAt(new THREE.Vector3(target.x, target.y, target.z))      //  相机瞄准原点
        //  摄像机位置修改后，同步到轨道控制器
        this.controls.target.set(target.x, target.y, target.z)                   //  控制器围绕原点旋转
        this.controls.update()
    }

    //  重置模型组，清除场景模型
    clearModels(){
        let self = this
        this.children.forEach(child=>{
            if (child.name === '我的场景模型组'){
                self.remove(child)
                self.modelGroup = new exThreeGroup()
                self.add(self.modelGroup)
            }
        })
    }

    //  删除children中的对象
    removeChild(name){
        let self = this
        this.children.forEach(child=>{
            if (child.name === name)
                self.remove(child)
        })
    }

    _random16Color(){
      var r=Math.round(255*Math.random()).toString(16) ;
      var g=Math.round(255*Math.random()).toString(16);
      var b=Math.round(255*Math.random()).toString(16);
      var rgb2="#"+r+g+b;
      return rgb2;
    }
}



