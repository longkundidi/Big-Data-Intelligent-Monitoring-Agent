import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { MeshoptDecoder } from 'three/examples/jsm/libs/meshopt_decoder.module'

/**
 * threeJS模型处理类
 */
export class exModel {
    #modelServerUrl = '/linux-minio'                  //  minio模型服务器地址

    constructor(scene) {
        this._scene = scene                           //  场景对象是必须的
    }

    //  利用FileReader读取本地模型，并加载
    loadModelByFileReader(file, callback){
        let self = this
        return new Promise((resolve) => {
            let reader = new FileReader()
            reader.readAsDataURL(file)
            reader.onload= ( loadEvent )=> {
                self._loadModel(loadEvent.target.result).then(obj => {
                    resolve(obj)
                    //self.moveToCenter()         //  模型加载完毕后，将模型移到屏幕中心
                    //self.loading = false         //  关闭模型加载提示
                })
            }
            reader.onprogress = (prog)=>{       //  加载进度回调
                callback(prog.loaded/prog.total)
                //self.percent = (prog.loaded/prog.total)*100
            }
        })
    }

    //  通过文件流对象，加载一个GLB模型
    _loadModel(fileStream){
        let self = this
        return new Promise((resolve) => {
            let loader = new GLTFLoader()
            loader.setMeshoptDecoder(MeshoptDecoder)      //  设置解码器
            loader.load(fileStream, (obj) => {    //  这里也可以只直接输入一个文件流fileStream

                self._scene.modelGroup.add(obj.scene) // 将网格对象添加到场景
                resolve(obj.scene)
            })
        })
    }
}