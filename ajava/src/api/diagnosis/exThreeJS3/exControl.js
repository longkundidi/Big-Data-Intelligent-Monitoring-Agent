// import {OrbitControls} from "three/examples/jsm/controls/OrbitControls.js";
// import {MapControls} from "three/addons";

// /**
//  * threeJS控制器扩展类
//  */
// export class exControl {
//   constructor(camera, domElement, type) {
//      return this._initControls(camera, domElement, type)
//   }

//   _initControls(camera, domElement, type){
//     let controls = null
//     let controlType = type ? type : 'orbit'
//     switch (controlType) {
//       case 'orbit':
//         controls = new OrbitControls(camera, domElement)        //创建正交控件
//         break
//       case 'map':
//         controls = new MapControls(camera, domElement)
//         controls.enableDamping = false
//         controls.dampingFactor = 0.05
//         controls.screenSpacePanning = false
//         controls.minDistance = 100
//         controls.maxDistance = 600
//         controls.maxPolarAngle = Math.PI / 2
//         break
//     }
//     return controls
//   }
// }


import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

/**
 * threeJS控制器扩展类
 */
export class exControl {
  constructor(camera, domElement, type) {
    return this._initControls(camera, domElement, type);
  }

  _initControls(camera, domElement, type) {
    let controls = null;
    let controlType = type ? type : 'orbit';
    switch (controlType) {
      case 'orbit':
        controls = new OrbitControls(camera, domElement); // 创建正交控件
        break;
      case 'map':
        controls = new OrbitControls(camera, domElement); // 使用 OrbitControls 替代 MapControls
        controls.enableDamping = false;
        controls.dampingFactor = 0.05;
        controls.screenSpacePanning = false;
        controls.minDistance = 100;
        controls.maxDistance = 600;
        controls.maxPolarAngle = Math.PI / 2;
        break;
    }
    return controls;
  }
}
