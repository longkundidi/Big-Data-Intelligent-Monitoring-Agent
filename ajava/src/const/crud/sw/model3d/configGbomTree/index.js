export const nodeOption = {
    /*emptyBtn: false,       //  隐藏默认的清空按钮
    submitBtn: false,*/
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
    },{
        label: '节点排序',
        prop: 'swsort',
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
    }]

}

export const nodeAndValOption = {
    /*emptyBtn: false,       //  隐藏默认的清空按钮
    submitBtn: false,*/
    column: [{
        label: '节点名称',
        prop: 'nodeName',
        align: 'center',
        span: 13,
        rules: [{
            required: true,
            trigger: 'blur',
            message: '名称是必须的'
        }]
    },{
        label: '节点排序',
        prop: 'swsort',
        span: 10,
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
        span: 13
    },{
        label: '节点类型',
        prop: 'nodeType',
        span: 10,
        type: 'select',
        dicData: [
            {label: '零部件节点', value: 'Bom'},
            {label: 'SCADA测点', value: 'SCADA_param'},
            {label: 'CMS测点', value: 'CMS_param'}
        ],
        rules: [{
            required: true,
            trigger: 'blur',
            message: '类型是必须的'
        }]
    }
    ]

}