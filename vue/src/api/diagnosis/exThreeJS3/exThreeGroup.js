import * as THREE from "three";
/**
 * 扩展threeJS的Group类, 用于管理和存放3D模型
 */
export class exThreeGroup extends THREE.Group {

    //  构造函数
    constructor() {
        super()
        this.name = '我的场景模型组'
    }

    //  移动场景内的模型组到中心, 返回场景包围盒及中心点坐标
    moveToCenter(){
        let sceneBox = this.calcSceneBox()
        let center = new THREE.Vector3()
        sceneBox.getCenter(center)
        this.position.set(this.position.x-center.x ,this.position.y-center.y , this.position.z-center.z )
        return {sceneBox: sceneBox, center: center}
    }

    //  计算场景内包围所有模型的最大包围盒
    calcSceneBox(){
        let minX=Infinity,minY=Infinity,minZ=Infinity,maxX=-Infinity,maxY=-Infinity,maxZ=-Infinity
        for (const group of this.children) {
            let tempBox = new THREE.Box3().setFromObject(group)        //  计算模型的包围盒
            maxZ=Math.max(tempBox.max.z, maxZ)
            maxX=Math.max(tempBox.max.x, maxX)
            maxY=Math.max(tempBox.max.y, maxY)
            minZ=Math.min(tempBox.min.z, minZ)
            minX=Math.min(tempBox.min.x, minX)
            minY=Math.min(tempBox.min.y, minY)
        }
        let sceneBox = new THREE.Box3(new THREE.Vector3(minX,minY,minZ), new THREE.Vector3(maxX,maxY,maxZ));
        return sceneBox
    }
}
