import * as THREE from 'three'

/**
 * threeJS扩展灯光类
 */
export class exLight {
  constructor(scene) {
    this._scene = scene                           //  场景对象是必须的
  }

  //  添加环境光
  addAmbientLight(clolr, intensity){
    this.ambientLight = new THREE.AmbientLight(clolr, intensity)
    this.ambientLight.name = 'defaultAmbientLight'
    this._scene.add(this.ambientLight)
  }

  //  删除环境光
  removeAmbientLight(){
    this.ambientLight.intensity = 0
    this._scene.removeChild('defaultAmbientLight')
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
    this._scene.add(dl)
  }

  //  添加缺省平行光
  addDefaultDirectionalLight(){
    this.directionalLight = new THREE.DirectionalLight('#fff', 1.5)
    this.directionalLight.name = 'defaultDirectionalLight'
    this.directionalLight.position.set(0,100, 0)         // 设置方向光源位置
    this._scene.add(this.directionalLight)
  }

  //  删除平行光, name:对象的名字
  removeDirectionalLight(name){
    this._scene.removeChild(name)
  }

  //  删除缺省平行光
  removeDefaultDirectionalLight(){
    this._scene.removeChild('defaultDirectionalLight')
  }

  //  设置平行光的颜色、位置、角度
  setDirectionalLight(name, clolr, position, intensity){
    this._scene.children.forEach(child=>{
      if (child.name === name){
        child.color = new THREE.Color(clolr)     // 方向光颜色更新
        child.intensity = intensity              // 方向光强度更新
        child.position.set(position.x,position.y,position.z)         // 方向光位置更新
      }
    })
  }

  //  设置缺省平行光的颜色、位置、角度
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
    this._scene.add(pl)
  }

  //  删除点光源
  removePointLight(name){
    this._scene.removeChild(name)
  }

  //  添加一个随相机移动的缺省点光源
  addPointLightFollowCamera(_intensity){
    let intensity = _intensity ? _intensity : 2
    this.pointLight = new THREE.PointLight('#fff', intensity)
    this.pointLight.position.set( 0, 0, 100)     // 设置光源位置
    this.pointLight.decay = 0                //  灯光衰减
    this.pointLight.distance = 0
    this.pointLight.name = 'defaultPointLight'
    this.pointLight.bias = {x:0,y:4,z:0}                   //  点光源随动相机的偏移量
    this._scene.add(this.pointLight)
  }
}
