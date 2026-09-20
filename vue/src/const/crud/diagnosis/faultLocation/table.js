export const tableOption = {
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
    height: 600,
    column: [
        {label: '风机编码', prop: 'turbineCode', hide: true, display: false},
        {label: '选择', prop: 'alarmRadio', width: 60, slot: true},
        {
            label: '实例对象名称',
            prop: 'turbineName',
            slot: true},
        {
            label: '零部件名称',
            prop: 'nodeName',
            slot: true},
        {
            label: '异常状态',
            prop: 'errorStatus',
            slot: true},
        {
            label: '报警发生时间',
            prop: 'dcTime',
            slot: true},
        {
            label: '状态感知任务',
            prop: 'modelName',
            width: 400,
            slot: true}
    ]
};

export const dataOption = {
    border: true,
    stripe: true,
    align: "center",
    addBtn: false,        //  隐藏新增按钮
    header:false,         //  隐藏表格头部
    menu:false,
    column: [
        {label: '选择', prop: 'dataRadio', width: 60, slot: true},
        {label: '风机编码', prop: 'turbineCode', hide: true, display: false},
        {
            label: '采集时间',
            prop: 'timeRange',
            align: 'center',
        },{
            label: '数据源',
            prop: 'turbineName',
            align: 'center',
        },{
            label: '信号类型',
            prop: 'dataType',
            align: 'center',
        },{
            label: '监测零部件',
            prop: 'nodeName',
            align: 'center',
        }
    ]
};

export const failureModelOption = {
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
    height: 620,
    column: [{label: '诊断模板id', prop: 'modelId', hide: true, display: false},
        {label: '选择', prop: 'diagnosisRadio', width: 60, slot: true},
        {
            label: '名称', prop: 'modelName', rules: [{
                required: true,
                trigger: 'blur',
                message: '故障诊断模板名称是必须的'
            }]
        },
        {
            label: "操作",
            prop: "operate",
            slot:true
        }
    ]
};

export const resultOption = {
    border: true,
    stripe: true,
    align: "center",
    addBtn: false,        //  隐藏新增按钮
    header:false,         //  隐藏表格头部
    menu:false,
    height: 430,
    column: [
        // {
        //     label: '故障风机',
        //     prop: 'turbineName',
        //     align: 'center',
        // },
        // {
        //     label: '故障部件',
        //     prop: 'nodeName',
        //     align: 'center',
        // },
        // {
        //     label: '故障时间',
        //     prop: 'faultTime',
        //     align: 'center',
        // },
        {
            label: '故障类型',
            prop: 'faultType',
            align: 'center',
            minWidth: 220,
        },
        {
            label: '故障概率(%)',
            prop: 'possibility',
            align: 'center',
            width: 150,
        },
        {
            label: '维修建议',
            prop: 'suggest',
            align: 'center',
            minWidth: 280,
        },
    ]
};
