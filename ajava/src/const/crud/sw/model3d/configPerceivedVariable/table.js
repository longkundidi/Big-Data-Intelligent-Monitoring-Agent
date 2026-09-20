export const tableOption = {
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 100,
    stripe: true,
    align: "center",
    menuWidth: 230,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'varId',
    column: [
        {label: '变量Id', prop: 'varId', hide: true, display: false},
        {
            type: 'select', label: '变量类型', prop: 'variableType',
            dicUrl: '/admin/dict/key/chain_config_variable-type',
            rules: [{
                required: true,
                trigger: 'blur',
                message: '变量类型是必须的'
            }]
        },
        {
            label: '变量名称', prop: 'varName', rules: [{
                required: true,
                trigger: 'blur',
                message: '变量名称是必须的'
            }]
        },
        {
            type: 'select', label: '数据类型', prop: 'dataType',
            dicUrl: '/admin/dict/key/chain_config_data-type',
            rules: [{
                required: true,
                trigger: 'blur',
                message: '数据类型是必须的'
            }]
        },
        {
            type: 'select', label: '单位', prop: 'dimension',
            dicUrl: '/admin/dict/key/chain_config_dimension', // 用于加载所有单位，供表格显示
        },
        {label: '采集频率', prop: 'collectionFrequency'},
        {label: '采集用途', prop: 'purpose', span: 24},
        {label: '关联节点id', prop: 'nodeId', hide: true, display: false}
    ]
}

export const selectTableOption = {
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 100,
    stripe: true,
    selection: true,
    align: "center",
    addBtn: false,        //  隐藏新增按钮
    menu: false,           //  隐藏操作栏
    menuWidth: 230,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'varId',
    column: [
        {label: '变量Id', prop: 'varId', hide: true, display: false},
        {
            label: '变量名称', prop: 'varName', rules: [{
                required: true,
                trigger: 'blur',
                message: '变量名称是必须的'
            }]
        },
        {
            type: 'select', label: '数据类型', prop: 'dataType',
            dicUrl: '/admin/dict/key/chain_config_data-type',
            rules: [{
                required: true,
                trigger: 'blur',
                message: '数据类型是必须的'
            }]
        },
        {
            type: 'select', label: '单位', prop: 'dimension',
            dicUrl: '/admin/dict/key/chain_config_dimension',
        },
        {label: '采集频率', prop: 'collectionFrequency'},
        {label: '采集用途', prop: 'purpose', span: 24},
        {label: '关联节点id', prop: 'nodeId', hide: true, display: false}
    ]
}