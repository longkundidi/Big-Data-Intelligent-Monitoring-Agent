export const simpleTemplateTableOption = {
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
    menu: false,
    rowKey: 'modelId',
    height: 400,
    column: [{label: '模型id', prop: 'modelId', hide: true, display: false},
        {label: '选择', prop: 'perceivedSelected', width: 60, slot: true},
        {
            label: '名称',
            prop: 'modelName',
            rules: [{
                required: true,
                trigger: 'blur',
                message: '模板名称是必须的'
            }],
            slot: true}
    ]
};

export const faultTableOption = {
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
    menu: false,
    rowKey: 'taskId',
    height: 400,
    column: [{label: '任务id', prop: 'taskId', hide: true, display: false},
        {
            label: '错误类型',
            prop: 'error',
            slot: true
        },
        {
            label: '算法名称',
            prop: 'algoShortname',
            slot: true
        },
        {
            label: '详细信息',
            prop: 'information',
            slot: true
        },
        {
            label: '时间',
            prop: 'dcTime',
            slot: true
        }
    ]

}

export const faultOption = {
    border: true,
    stripe: true,
    align: "center",
    addBtn: false,        //  隐藏新增按钮
    header:false,         //  隐藏表格头部
    menu:false,
    column: [
        {
            label: '故障类型',
            prop: 'faultName',
            align: 'center',
        },
        {
            label: '故障开始时间',
            prop: 'startTime',
            align: 'center',
        },
        {
            label: '故障结束时间',
            prop: 'endTime',
            align: 'center',
        }
    ]
};