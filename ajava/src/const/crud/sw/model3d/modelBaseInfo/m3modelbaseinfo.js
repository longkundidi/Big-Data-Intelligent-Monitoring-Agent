var validatePhotoPos = (rule, value, callback) => {
  let arrPhotoPos = []
  arrPhotoPos = value.replace(/\s*/g, "").split(',')       //  去除空格并转化为数组
  if (arrPhotoPos.length === 3) {
    for (const pos of arrPhotoPos) {
      if (pos === "" || pos === null || isNaN(pos)) {
        callback(new Error('相机位置参数错误'))
      }
    }
    callback()
  }
  else {
    callback(new Error('相机位置参数错误'))
  }
}

export const tableOption = {
  "border": true,
  "index": true,
  "indexLabel": "序号",
  "stripe": true,
  "menuAlign": "center",
  "align": "center",
  "searchMenuSpan": 6,
  lazy: true,
  rowKey: 'mbId',
  menuWidth: 820,
  indexWidth: 60,           /*序号列宽度*/
  "column": [
    {
      "type": "input",
      "label": "模型库id",
      "prop": "mbId",
      "hide": true,
      display: false
    },
    {
      "type": "input",
      "label": "记录编码，顶层节点编码：MT1，MT2，3，子节点编码：MT1-1,MT1-2等",
      "prop": "mbCode",
      "hide": true,
      display: false
    },
    {
      "type": "input",
      "label": "模型库名称",
      "prop": "mbName",
      labelWidth: '110',
      addDisplay: false,
      editDisplay: false,
      rules: [{
        required: true,
        trigger: 'blur',
        message: '模型库名称是必须的'
      }]
    },
    {
      "type": "input",
      "label": "场景图片URL地址",
      "prop": "imageUrl",
      "hide": true,
      display: false
    },
    {
      "type": "input",
      "label": "包含的模型个数",
      "prop": "modelNumber",
      "hide": true,
      display: false
    },
    {
      "type": "input",
      "label": "包含模型的总计字节数，单位：字节",
      "prop": "totalSize",
      "hide": true,
      display: false
    },
    {
      "type": "input",
      "label": "排序",
      "prop": "nodeSort",
      "hide": true,
      addDisplay: false,
      editDisplay: false,
    },
    {
      "type": "input",
      "label": "创建时间",
      "prop": "createTime",
      "hide": true,
      display: false
    },
    {
      "type": "input",
      "label": "创建者",
      "prop": "creator",
      display: false
    }
  ],
  group: [
    {
      align: 'center',
      column: [
        {
          "type": "input",
          "label": "模型库名称",
          "prop": "mbName",
          labelWidth: '110',
          rules: [{
            required: true,
            trigger: 'blur',
            message: '模型库名称是必须的'
          }]
        },
        {
          "type": "input",
          "label": "排序",
          "prop": "nodeSort",
          "hide": true
        },
      ]
    },
    {
      label: '相机参数',
      prop: 'photoPara',
      align: 'center',
      icon: 'el-icon-cloudy',
      column: [
        {
          type: 'input-number',
          "label": "相机垂直角度",
          labelWidth: '110',
          "prop": "photoFov",
          rules: [{
            required: true,
            trigger: 'blur',
            message: '相机参数是必须的'
          }]
        },
        {
          type: 'input-number',
          "label": "相机近端面",
          "prop": "photoNear",
          labelWidth: '110',
          rules: [{
            required: true,
            trigger: 'blur',
            message: '相机参数是必须的'
          }]
        },
        {
          type: 'input-number',
          "label": "相机远端面",
          "prop": "photoFar",
          labelWidth: '110',
          rules: [{
            required: true,
            trigger: 'blur',
            message: '相机参数是必须的'
          }]
        },
        {
          type: 'input',
          "label": "相机位置",
          "prop": "photoPos",
          labelWidth: '110',
          rules: [{
            required: true,
            validator: validatePhotoPos,
            trigger: 'blur',
            message: '相机参数是必须的'
          }]
        }
      ]
    }
  ]
}
