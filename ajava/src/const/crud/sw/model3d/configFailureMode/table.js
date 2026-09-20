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
    rowKey: 'failureId',
    column: [
        { label: '故障记录id', prop: 'failureId', hide: true, display: false },
        { label: '故障名称', prop: 'failureName',rules: [{
                required: true,
                trigger: 'blur',
                message: '故障名称是必须的'
            }] },
        { label: '故障编码', prop: 'failureCode', rules: [{
                required: true,
                trigger: 'blur',
                message: '故障编码是必须的'
            }]  },
        { label: '故障说明', prop: 'memo', span: 24 },
        { label: '关联节点id', prop: 'nodeId', hide: true, display: false }
    ]
};