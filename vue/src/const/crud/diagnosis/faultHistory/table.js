export const tableOption = {
    addBtn: false,          //  隐藏新增按钮
    editBtn: false,         //  隐藏编辑、删除按钮
    delBtn: false,
    border: true,           //  显示表格线
    refreshBtn: false,
    dialogDrag: true,       //  弹窗可拖拽
    index: true,            //  显示序号
    indexLabel: "序号",
    indexWidth: 100,
    stripe: true,
    align: "center",
    menu:false,
    dialogCustomClass: 'h-win-avue-crud',       //  自定义样式
    dialogWidth: 900,       //  表格弹窗宽度
    rowKey: 'id',
    column: [
        { label: '故障信息id', prop: 'id', hide: true, display: false },
        { label: '故障部件id', prop: 'nodeId', hide: true, display: false },
        { label: '故障部件', prop: 'component', hide: true, display: false },
        { label: '故障名称', prop: 'faultName', slot: true },
        { label: '故障开始时间', prop: 'startTime', slot: true },
        { label: '故障结束时间', prop: 'endTime', slot: true  }
    ]
};