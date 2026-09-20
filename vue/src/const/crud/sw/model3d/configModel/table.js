export const perceivedTableOption = {
    addBtn: false,          //  隐藏新增按钮
    editBtn: false,         //  隐藏编辑、删除按钮
    delBtn: false,
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 60,
    stripe: true,
    align: "center",
    menuWidth: 130,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'modelId',
    height: 620,            //  设置表格高度
    column: [{label: '感知模板id', prop: 'modelId', hide: true, display: false},
        {label: '选择', prop: 'perceivedSelected', width: 60, slot: true},
        {
            label: '名称', prop: 'modelName', rules: [{
                required: true,
                trigger: 'blur',
                message: '状态感知模板名称是必须的'
            }]
        },
        {label: '关联感知变量', prop: 'relatedPerceivedVar', width: 110, labelWidth: '110', slot: true},
    ]

}


export const failureTableOption = {
    addBtn: false,          //  隐藏新增按钮
    editBtn: false,         //  隐藏编辑、删除按钮
    delBtn: false,
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 60,
    stripe: true,
    align: "center",
    menuWidth: 200,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'modelId',
    height: 620,
    column: [{label: '诊断模板id', prop: 'modelId', hide: true, display: false},
        {label: '选择', prop: 'failureSelected', width: 60, slot: true},
        {
            label: '名称', prop: 'modelName', rules: [{
                required: true,
                trigger: 'blur',
                message: '故障诊断模板名称是必须的'
            }]
        }
    ]

}

export const compositionTableOption = {
    addBtn: false,          //  隐藏新增按钮
    editBtn: false,         //  隐藏编辑、删除按钮
    delBtn: false,
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 60,
    stripe: true,
    align: "center",
    menuWidth: 250,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'modelId',
    height: 620,            //  设置表格高度
    column: [{label: '模板id', prop: 'modelId', hide: true, display: false},
        {label: '选择', prop: 'compositionSelected', width: 60, slot: true},
        {
            label: '名称', prop: 'modelName', rules: [{
                required: true,
                trigger: 'blur',
                message: '组态模板名称是必须的'
            }]
        },
        {label: '关联感知变量', prop: 'relatedCompositionVar',labelWidth: '110', slot: true},
    ]

}


export const sensorTableOption = {
    border: true,
    index: true,
    indexLabel: "序号",
    stripe: true,
    menuAlign: "center",
    align: "center",
    maxHeight: "auto",
    selection: true,
    tip: false,            //  隐藏勾选清空按钮
    addBtn: false,        //  隐藏新增按钮
    header: false,         //  隐藏表格头部
    menu: false,           //  隐藏操作栏
    indexWidth: 70,      //   定义序号列宽
    column: [
        {
            type: 'input',
            label: '变量Id',
            prop: 'varId',
            hide: true,       //  列表显示时隐藏
        },
        {
            type: 'input',
            label: '变量名称',
            prop: 'varName'
        },
        {
            type: 'select',
            label: '数据类型',
            dicUrl: '/admin/dict/key/chain_config_data-type',
            dataType: 'string',
            prop: 'dataType',
        },
        {
            type: "select",
            label: "单位",
            dicUrl: '/admin/dict/key/chain_config_dimension',
            dataType: 'string',
            prop: "dimension"
        },
        {
            "type": "input",
            "label": "阈值下限",
            "prop": "min"
        },
        {
            "type": "input",
            "label": "阈值上限",
            "prop": "max"
        },
        {
            "type": "input",
            "label": "采集频率",
            "prop": "collectionFrequency",
        },
        {
            "type": "input",
            "label": "采集用途",
            "prop": "purpose"
        }, {
            type: 'input',
            label: '关联节点id',
            prop: 'nodeId',
            hide: true,       //  列表显示时隐藏
        }

    ]

}

export const algoFormOption = {
    submitBtn: false,
    emptyBtn: false,
    column: [
        {
            label: '模板名称', prop: 'modelName', rules: [{
                required: true,
                trigger: 'blur',
                message: '模板名称是必须的'
            }]
        },
        {
            type: 'select',
            label: '服务类型',
            dicUrl: '/admin/dict/key/chain_config_service_type',
            dataType: 'string',
            prop: 'serviceType',
            value: '1'
        }, {
            type: 'select',
            label: '指标要求',
            dicUrl: '/admin/dict/key/chain_config_index_req',
            dataType: 'string',
            prop: 'indexRequirement',
            span: 8,
            value: '1'
        }, {
            type: 'select',
            label: '评估指标',
            dicUrl: '/admin/dict/key/chain_config_assess_index',
            dataType: 'string',
            prop: 'assessIndex',
            span: 8,
            value: '1'
            //placeholder: '系统默认值'
        }, {
            label: '评估方法',
            prop: 'assessType',
            type: 'radio',
            border: true,
            span: 8,
            dicData: [{
                label: '状态阈值评估',
                value: '1'
            }, {
                label: '服役阶段评估',
                value: '0'
            }]
        },
        {
            type: 'select',
            label: '阈值类型',
            dicUrl: '/admin/dict/key/chain_config_threshold_type',
            dataType: 'string',
            prop: 'thresholdType',
            span: 8,
            value: '1'
        }, {
            label: '连续超限次数',
            prop: 'limitNumber',
            labelWidth: '120',
            span: 8,
            type: 'input-number',
            value: 3
        }, {
            type: 'select',
            label: '服务算法',
            prop: 'algoId',
            dicData: [],
            props: {        // 配置接口数据对应字典中的label和value
                label: 'label',
                value: 'value',
            },
            dataType: 'string',
            span: 8,
        }, {
            type: 'select',
            label: '预测故障失效判据',
            prop: 'failureCriterion',
            dicData: [{
                label: '温度残差超过80',
                value: '1'
            }, {
                label: '123',
                value: '2'
            }],
            span: 8,
            labelWidth: '140',
            value: '1'
        }, {
            type: 'select',
            label: '趋势预测算法',
            prop: 'trendPrediction',
            dicData: [{
                label: 'BLSTM',
                value: '1'
            }, {
                label: '123',
                value: '2'
            }],
            span: 8,
            labelWidth: '140',
        }, {
            type: 'input-number',
            label: '预测结果连续超限次数',
            prop: 'resultLimitNum',
            span: 8,
            labelWidth: '160',
        }
    ]
}

export const perceivedTaskFormOption = {
    submitBtn: false,
    emptyBtn: false,
    labelWidth: 100,
    column: [
        {
            label: '任务名称',
            prop: 'modelName',
            span: 8,
            rules: [{
                required: true,
                trigger: 'blur',
                message: '任务名称不能为空'
            }]
        },
        {
            type: 'input-number',
            label: '数据维度',
            prop: 'dataDimension',
            span: 8,
            min: 1,
            precision: 0,
            controlsPosition: 'right',
            rules: [{
                required: true,
                trigger: 'change',
                message: '请输入模型单次输入的数据维度'
            }]
        },
        {
            type: 'select',
            label: '服务算法',
            prop: 'algoId',
            dicData: [],
            props: {
                label: 'label',
                value: 'value'
            },
            dataType: 'string',
            span: 8,
            rules: [{
                required: true,
                trigger: 'change',
                message: '请选择服务算法'
            }]
        }
    ]
}

export const compositionFormOption = {
    submitBtn: false,
    emptyBtn: false,
    column: [
        {
            label: '模板名称', prop: 'modelName', rules: [{
                required: true,
                trigger: 'blur',
                message: '模板名称是必须的'
            }]
        },
        {
            type: 'select',
            label: '服务组态',
            prop: 'algoId',
            dicData: [],
            props: {        // 配置接口数据对应字典中的label和value
                label: 'label',
                value: 'value',
            },
            dataType: 'string',
        }
    ]
}

export const varTableOption = {
    addBtn: false,          //  隐藏新增按钮
    menu: false,            //  隐藏操作栏
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
        {label: '阈值下限', prop: 'min'},
        {label: '阈值上限', prop: 'max'},
        {label: '采集频率', prop: 'collectionFrequency'},
        {label: '采集用途', prop: 'purpose', span: 24},
        {label: '关联节点id', prop: 'nodeId', hide: true, display: false}
    ]
};

export const pTemplateTableOption = {
    addBtn: false,          //  隐藏新增按钮
    editBtn: false,         //  隐藏编辑、删除按钮
    delBtn: false,
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 60,
    stripe: true,
    align: "center",
    menuWidth: 400,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'modelId',
    height: 620,
    column: [{label: '模型id', prop: 'modelId', hide: true, display: false},
        {
            label: '名称',
            prop: 'modelName',
            rules: [{
                required: true,
                trigger: 'blur',
                message: '模板名称是必须的'
            }],
            slot: true
        },
        {label: '运行状态', prop: 'status', width: 110, labelWidth: '110', slot: true},
        {label: '数据维度', prop: 'dataDimension', width: 110},
        {label: '变量数', prop: 'variableNum', width: 90},
        {label: '关联感知变量', prop: 'relatedTPerceivedVar', width: 150, labelWidth: '110', slot: true},
    ]
};

export const cTemplateTableOption = {
    addBtn: false,          //  隐藏新增按钮
    editBtn: false,         //  隐藏编辑、删除按钮
    delBtn: false,
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 60,
    stripe: true,
    align: "center",
    menuWidth: 400,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'modelId',
    height: 620,
    column: [{label: '模型id', prop: 'modelId', hide: true, display: false},
        {
            label: '名称',
            prop: 'modelName',
            rules: [{
                required: true,
                trigger: 'blur',
                message: '模板名称是必须的'
            }],
            slot: true
        },
        {label: '运行状态', prop: 'status', width: 110, labelWidth: '110', slot: true},
        {label: '关联感知变量', prop: 'relatedTCompositionVar', width: 150, labelWidth: '110', slot: true},
    ]
};
