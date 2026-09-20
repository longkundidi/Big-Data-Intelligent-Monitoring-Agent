import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'

/**
 * threeJS模型处理类
 */
export class exModel {
    #modelServerUrl = '/linux-minio'                  //  minio模型服务器地址
    #factor = Math.PI / 180      //  角度转弧度

    constructor(scene) {
        this._scene = scene                           //  场景对象是必须的
    }

    /*
    * 给定一个文件Url地址，例如：http://121.37.150.126:9000/modelfile/01010100093_1681782652977.glb
    * 下载模型到场景中
    * */
    loadModelByHttpUrl(url, callback){
      let self = this
      return new Promise((resolve) => {
        let loader = new GLTFLoader()
        loader.load(url, (obj) => {           //  这里也可以只直接输入一个文件流fileStream
          self._scene.modelGroup.add(obj.scene) // 将网格对象添加到场景
          resolve(obj.scene)
        },(xhr) => {
          callback((xhr.loaded / xhr.total)*100)
        })
      })
    }


    //  利用FileReader读取本地模型，并加载
    loadModelByFileReader(file, callback){
        let self = this
        return new Promise((resolve) => {
            let reader = new FileReader()
            reader.readAsDataURL(file)
            reader.onload= ( loadEvent )=> {
                self.loadModelByHttpUrl(loadEvent.target.result).then(obj => {
                    resolve(obj)
                })
            }
            reader.onprogress = (prog)=>{       //  加载进度回调
                callback(prog.loaded/prog.total)
                //self.percent = (prog.loaded/prog.total)*100
            }
        })
    }

    //  克隆模型，并设置模型位置、姿态
    cloneModel(glb, pose){
      let cloneObj = glb.clone()      //  克隆模型
      if (pose !== undefined)
        this.setModelPose(cloneObj, pose)   //  设置对象的角度和位置
      return cloneObj
    }

    //  设置模型的位置（x,y,z）和角度（rx,ry,rz）
    setModelPose(glb, pose){
      glb.rotation.set(pose.rx*this.#factor, pose.ry*this.#factor, pose.rz*this.#factor)
      glb.position.set(pose.x, pose.y, pose.z)
    }
}
