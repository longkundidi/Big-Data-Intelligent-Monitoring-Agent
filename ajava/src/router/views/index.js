//import Layout from '@/page/index/dataMining.vue'
import Layout from '@/page/myIndex/index.vue'


export default [{
    path: '/wel',
    component: () => import('@/page/myIndex/index.vue'),
    /*component: () => import('@/page/index/dataMining.vue'),*/
    redirect: '/wel/index',
    children: [{
        path: 'index',
        name: '首页',
        meta: {
            i18n: 'dashboard'
        },
        component: () =>
            import(/* webpackChunkName: "views" */ '@/page/WelIndex.vue')
    },
        {
            path: '/model3d/modelBaseTree/index',
            name: '库结构树编辑',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/sw/model3d/modelBaseTree/index.vue'),
        },
        {
            path: '/kg/dbConnect',
            name: '数据库链接',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/kgExtract/relationExtract/d2r/ConnectDB.vue'),
        },
        {
            path: '/kg/fetchKg/scrapyKg',
            name: '外部知识获取-专利查看',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/fetchKg/scrapyKg.vue'),
        },
        {
            path: '/kg/tbMapping',
            name: '关系映射',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/kgExtract/relationExtract/d2r/TableMapping.vue'),
        },
        {
            path: '/kg/showCypher',
            name: '知识数据抽取',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/kgExtract/relationExtract/d2r/ShowCypher.vue'),
        },
        {
            path: '/kg/ontologyModel/modelTool',
            name: '本体编辑工具',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/ontologyModel/modelTool.vue'),
        },
        {
            path: '/kg/featureExtraction',
            name: '非结构抽取',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/kgExtract/featureExtract/Index.vue'),
        },
        {
            path: '/kg/nerResult',
            name: '非结构抽取结果',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/kgExtract/featureExtract/showData.vue'),
        },
        {
            path: '/kg/searchService/indexTable',
            name: '列表搜索服务',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/searchService/indexTable.vue'),
        },
        {
            path: '/kg/gmlExamine',
            name: '大模型语料审核',
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/kg/chat/GmlExamine.vue'),
        },
        {
            path: '/diagnosis/faultLocation/runModel',
            name: '运行诊断模型',
            meta: {
                isAuth: false,
                activeMenu: '/diagnosis/faultLocation/index'
            },
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/diagnosis/faultLocation/runModel.vue'),
        },
        {
            path: '/al/taskConfig/model/modelConfig',
            name: '新增模型组态',
            meta: {
                isAuth: false,
                activeMenu: '/al/taskConfig/model/modelConfigManage'
            },
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/al/taskConfig/model/modelConfig.vue'),
        },
        {
            path: '/al/taskConfig/al/algorithmConfig',
            name: '算法组态训练',
            meta: {
                isAuth: false,
                activeMenu: '/al/taskConfig/al/algorithmConfigManage'
            },
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/al/taskConfig/al/algorithmConfig.vue'),
        },
        {
            path: '/variableDictionary/index',
            name: '变量字典管理',
            meta: {
                isAuth: false,
                activeMenu: '/scene/configSceneModel/index'
            },
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/scene/variableDictionary/index.vue'),
        },
        {
            path: '/DatasetManage/index',
            name: '数据集管理',
            meta: {
                isAuth: false,
                activeMenu: '/scene/configSceneModel/index'
            },
            component: () =>
                import(/* webpackChunkName: "page" */ '@/views/DatasetManage/index.vue'),
        },
        {
            path: '/monitor/flink/task-manage/create_sql_streaming_task',
            name: '创建sql流任务',
            component: () => import( '@/views/monitor/flink/task-manage/sqltask.vue'),
            children: []

        },
        {
            path: '/monitor/flink/task-manage/edit_sql_streaming_task',
            name: '修改sql流任务',
            component: () => import('@/views/monitor/flink/task-manage/sqltask.vue'),
            children: []
        },
        {
            path: '/monitor/flink/task-manage/view_sql_streaming_task',
            name: '查看sql流任务',
            component: () => import('@/views/monitor/flink/task-manage/sqltask.vue'),
            children: []
        },
        {
            path: '/monitor/flink/task-manage/create_sql_batch_task',
            name: '创建sql批任务',
            component: () => import('@/views/monitor/flink/task-manage/sqltask.vue'),
            children: []
        },
        {
            path: '/monitor/flink/task-manage/edit_sql_batch_task',
            name: '修改sql批任务',
            component: () => import('@/views/monitor/flink/task-manage/sqltask.vue'),
            hidden: true,
            children: []
        },
        {
            path: '/monitor/flink/task-manage/view_sql_batch_task',
            name: '查看sql批任务',
            component: () => import('@/views/monitor/flink/task-manage/sqltask.vue'),
            hidden: true,
            children: []
        },
        {
            path: '/monitor/flink/task-manage/create_jar_task',
            name: '创建jar任务',
            component: () => import('@/views/monitor/flink/task-manage/jartask.vue'),
            hidden: true,
            children: []
        },
        {
            path: '/monitor/flink/task-manage/edit_jar_task',
            name: '修改jar任务',
            component: () => import('@/views/monitor/flink/task-manage/jartask.vue'),
            hidden: true,
            children: []
        },
        {
            path: '/monitor/flink/task-manage/view_jar_task',
            name: '查看jar任务',
            component: () => import('@/views/monitor/flink/task-manage/jartask.vue'),
            children: []
        },
        {
            path: '/monitor/flink/log-manage/logdetail',
            name: '查看日志详情',
            component: () => import('@/views/monitor/flink/log-manage/logdetail.vue'),
            children: []
        },
 /*       {
            path: '/scene/sceneData/sceneData',
            name: '场景元模型管理',
            component: () => import('@/views/scene/sceneData/sceneData.vue'),
            children: []
        },*/
        {
            path: '/scene/configSceneModel/index',
            name: '场景元模型管理',
            component: () => import('@/views/scene/configSceneModel/index.vue'),
            children: []
        },
        {
            path: '/turbineDetail',
            name: '风机详情',
            component: () => import('@/page/turbineDetail.vue'),
            meta: {
                keepAlive: false
            }
        }


    ]
}, {
    path: '/iframe',
    component: Layout,
    redirect: '/iframe',
    children: [{
        path: '',
        name: '',
        component: () =>
            import(/* webpackChunkName: "views" */ '@/components/Iframe/main.vue')
    }]
}, {
    path: '/info',
    component: Layout,
    redirect: '/info/index',
    children: [{
        path: 'index',
        name: '个人信息',
        component: () =>
            import (/* webpackChunkName: "page" */ '@/views/admin/user/info.vue')
    }]
}]
