//  节点编辑、添加
/*var validNodeSort = (rule, value, callback) => {debugger      //  验证节点排序
  if (value === undefined)
    callback(0)
}*/

export const nodeOption = {
  emptyBtn:false,       //  隐藏默认的清空按钮
  submitBtn:false,
  group: [
    {
      label: '基本信息',
      prop: 'basicInfo',
      align: 'center',
      icon: 'el-icon-edit-outline',
      column: [{
        label: '节点名称',
        prop: 'nodeName',
        align: 'center',
        span: 16,
        rules: [{
          required: true,
          trigger: 'blur',
          message: '名称是必须的'
        }]
      },
        {
          label: '排序',
          prop: 'nodeSort',
          labelTip: '排序可以输入整型/浮点型数据',
          span: 8,
          type: 'number',
          value: undefined,       //  默认不输入排序
          rules: [{
            required: false,
            trigger: 'blur',
            message: '排序可以输入整型/浮点型数据'
          }]
        },{
          label: '备注',
          prop: 'memo',
          align: 'center',
          span: 24
        },{
          label: '节点层级',
          prop: 'nodeLevel',
          "hide": true,
          display: false
        },
        {
          label: '模型文件',
          prop: 'modelFileurl',
          align: 'center',
          span: 17
        },{
          label: '',
          prop: 'uploadComponent',
          formslot:true,
          span: 7
        },
        {
          label: '',
          prop: 'progressComponent',
          formslot:true,
          span: 24
        },
      ]
    }, {
      label: '几何信息',
      prop: 'geoInfo',
      icon: 'el-icon-view',
      column: [{
        label: '位置X',
        prop: 'xcoordinate',
        type: 'number',
        precision: 2
      },{
        label: '旋转X',
        prop: 'xrotationAngle',
        type: 'number',
        precision: 2
      },{
        label: '位置Y',
        prop: 'ycoordinate',
        type: 'number',
        precision: 2
      },{
        label: '旋转Y',
        prop: 'yrotationAngle',
        type: 'number',
        precision: 2
      },{
        label: '位置Z',
        prop: 'zcoordinate',
        type: 'number',
        precision: 2
      },{
        label: '旋转Z',
        prop: 'zrotationAngle',
        type: 'number',
        precision: 2,
      }]
    }]

}


export const nodeEditOption = {
  emptyBtn:false,       //  隐藏默认的清空按钮
  submitBtn:false,
  group: [
    {
      label: '基本信息',
      prop: 'basicInfo',
      align: 'center',
      icon: 'el-icon-edit-outline',
      column: [{
        label: '节点名称',
        prop: 'nodeName',
        align: 'center',
        rules: [{
          required: true,
          trigger: 'blur',
          message: '名称是必须的'
        }]
      },
        {
          label: '排序',
          prop: 'nodeSort',
          align: 'center',
          type: 'input-number',
          addDisplay: false,
        },{
          label: '备注',
          prop: 'memo',
          align: 'center',
        },{
          label: '节点层级',
          prop: 'nodeLevel',
          "hide": true,
          display: false
        },
        {
          label: '模型文件',
          prop: 'modelFileurl',
          align: 'center',
        },{
          label: '',
          prop: 'text',
          formslot:true
        }, {
          label: '图片文件',
          prop: 'urlImg',
          align: 'center',
        },{
          label: '图片上传',
          prop: 'modelImg',
          type: 'upload',
          listType: 'picture-img',
          span: 9,
          limit:1,
          propsHttp: {
            url: 'data',
            name: 'resultMsg'
          },
          tip: '只能上传jpg/png图片',
          action: '/model3d/minio/uploadFile/modelimg'       //  minio服务器图片上传
        },
      ]
    }, {
      label: '几何信息',
      prop: 'geoInfo',
      icon: 'el-icon-view',
      column: [{
        label: '位置X',
        prop: 'xcoordinate',
        align: 'center',
        type: 'input-number',
        precision: 2
      },{
        label: '旋转X',
        prop: 'xrotationAngle',
        align: 'center',
        type: 'input-number',
        precision: 2
      },{
        label: '位置Y',
        prop: 'ycoordinate',
        align: 'center',
        type: 'input-number',
        precision: 2
      },{
        label: '旋转Y',
        prop: 'yrotationAngle',
        align: 'center',
        type: 'input-number',
        precision: 2
      },{
        label: '位置Z',
        prop: 'zcoordinate',
        align: 'center',
        type: 'input-number',
        precision: 2
      },{
        label: '旋转Z',
        prop: 'zrotationAngle',
        align: 'center',
        type: 'input-number',
        precision: 2,
      }]
    }]

}

export const nodeAddOption = {
  emptyBtn:false,       //  隐藏默认的清空按钮
  submitBtn:false,
  "column": [
    {
      "type": "input",
      "label": "节点Id",
      "prop": "nodeId",
      "hide": true,
      display: false
    },{
      label: '节点名称',
      prop: 'nodeName',
      align: 'center',
      rules: [{
        required: true,
        trigger: 'blur',
        message: '名称是必须的'
      }]
    },{
      label: '排序',
      prop: 'nodeSort',
      align: 'center',
      type: 'input-number',
      addDisplay: false,
    },{
      label: '备注',
      prop: 'memo',
      align: 'center',
    }
    ]
}
