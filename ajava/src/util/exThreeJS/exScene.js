import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { exThreeGroup } from '@/util/exThreeJS/exThreeGroup.js'
import { exModel } from '@/util/exThreeJS/exModel.js'

/**
 * 扩展threeJS场景类
 */
export class exScene extends THREE.Scene {

    //  构造函数
    constructor(canvas) {
        super()
        this.name = '我的场景'
        this.canvas = canvas                                  //  保存画布
        this.pointLightBias = {x:0,y:4,z:0}                   //  点光源随动相机的偏移量
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
        this._renderer.setClearColor( '#000' )
        this._renderer.toneMapping = THREE.ReinhardToneMapping
        this._renderer.toneMappingExposure = 2.3
        this.clearCanvas()
        this.canvas.appendChild(this._renderer.domElement)    //向画布元素中插入canvas对象
        //   3. 初始化控制器
        this.controls = new OrbitControls(this.camera, this._renderer.domElement)
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

    //  手动渲染
    doRender() {
        if (this.pointLight)
            this.pointLight.position.set(this.camera.position.x+this.pointLightBias.x, this.camera.position.y +this.pointLightBias.y,this.camera.position.z+this.pointLightBias.z)   //  点光源跟随相机移动
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
            this.theRenderer.setClearColor(random16Color(), 1.0)      //  产生一个随机背景色
        return bkColor
    }

    //  移动相机到模型组（modelGroup）附近
    moveCameraToModels(distanceFacter){
        let sceneBox = this.modelGroup.calcSceneBox()     //  计算模型场景的包围盒
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

    //  添加环境光
    addAmbientLight(clolr, intensity){
        this.ambientLight = new THREE.AmbientLight(clolr, intensity)
        this.ambientLight.name = 'defaultAmbientLight'
        this.add(this.ambientLight)
    }

    //  删除环境光
    removeAmbientLight(){
        this.ambientLight.intensity = 0
        this.removeChild('defaultAmbientLight')
    }

    //  设置环境光, clolr：光照颜色， intensity：光照强度
    setAmbientLight(clolr, intensity){
        this.ambientLight.color = new THREE.Color(clolr)
        this.ambientLight.intensity = intensity
    }

    //  添加平行光, clolr:颜色，position：位置，intensity：强度
    addDirectionalLight(name, clolr, position, intensity){
        let dl = new THREE.DirectionalLight(clolr, intensity)
        dl.name = name
        dl.position.set(position.x,position.y, position.z)         // 设置方向光源位置
        this.add(dl)
    }

    //  添加缺省平行光
    addDefaultDirectionalLight(){
        this.directionalLight = new THREE.DirectionalLight('#fff', 1.5)
        this.directionalLight.name = 'defaultDirectionalLight'
        this.directionalLight.position.set(0,100, 0)         // 设置方向光源位置
        this.add(this.directionalLight)
    }

    //  删除平行光, name:对象的名字
    removeDirectionalLight(name){
        this.removeChild(name)
    }

    //  删除缺省平行光
    removeDefaultDirectionalLight(){
        this.removeChild('defaultDirectionalLight')
    }

    setDirectionalLight(name, clolr, position, intensity){
        let self = this
        this.children.forEach(child=>{
            if (child.name === name){
                child.color = new THREE.Color(clolr)     // 方向光颜色更新
                child.intensity = intensity              // 方向光强度更新
                child.position.set(position.x,position.y,position.z)         // 方向光位置更新
            }
        })
    }

    setDefaultDirectionalLight(clolr, position, intensity){
        this.directionalLight.color = new THREE.Color(clolr)     // 方向光颜色更新
        this.directionalLight.intensity = intensity              // 方向光强度更新
        this.directionalLight.position.set(position.x,position.y,position.z)         // 方向光位置更新
    }

    //  添加点光源
    addPointLight(name, clolr, position, intensity){
        let pl = new THREE.PointLight(clolr, intensity)
        pl.position.set( position.x, position.y, position.z )     // 设置光源位置
        pl.decay = 0                //  灯光衰减
        pl.distance = 0
        pl.name = name
        this.add(pl)
    }

    //  删除点光源
    removePointLight(name){
        this.removeChild(name)
    }

    //  添加一个随相机移动的缺省点光源
    addPointLightFollowCamera(_intensity){
        let intensity = _intensity ? _intensity : 2
        this.pointLight = new THREE.PointLight('#fff', intensity)
        this.pointLight.position.set( 0, 0, 100)     // 设置光源位置
        this.pointLight.decay = 0                //  灯光衰减
        this.pointLight.distance = 0
        this.pointLight.name = 'defaultPointLight'
        this.add(this.pointLight)
    }


}

