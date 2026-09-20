import Layout from '../../../../../IdeaProjects/web/flink-algorithm-web-front/src/layout/index.vue'
import zh from "../../../../../IdeaProjects/web/flink-algorithm-web-front/src/lang/zh";

export const flinkRouter = [
  {
    path: '/flink/Main',
    redirect: 'noRedirect',
    name:'AlgorithmManage',
    meta: { title: '算法-模型镜像库管理', icon: 'el-icon-star-on' ,requiresAuth: false},
    component: Layout,
    children: [
      {
        path: '/flink/DataCleaning',
        name: 'DataCleaning',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/data-cleaning/DataCleaning.vue'),
        meta: { title: '数据清洗算法',requiresAuth: false },
        children: [
          // {
          //   path: '/base-algorithm-manage/view_jar',
          //   name: 'ViewJarTask1',
          //   component: (resolve) => require([`@/views/flink/task-manage/jartask1.vue`], resolve),
          //   hidden: true,
          //   meta: { title: '算法实例' ,requiresAuth: false },
          //   children: []
          // },
        ]
      },
      {
        path: '/flink/DataMining',
        name: 'DataMining',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/data-mining/DataMining.vue'),
        meta: { title: '数据挖掘算法',requiresAuth: false },
        children: [
          // {
          //   path: '/base-algorithm-manage/view_jar',
          //   name: 'ViewJarTask1',
          //   component: (resolve) => require([`@/views/flink/task-manage/jartask1.vue`], resolve),
          //   hidden: true,
          //   meta: { title: '算法实例',requiresAuth: false },
          //   children: []
          // },
        ]
      },
      {
        path: '/flink/textExtraction',
        name: 'TextExtraction',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/text-extraction/index.vue'),
        meta: { title: '文本数据提取模型' ,requiresAuth: false },
        children: [
        ]
      },
      {
        path: '/flink/digitalTwin',
        name: 'DigitalTwin',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/digital-twin/DigitalTwin.vue'),
        meta: { title: '数字孪生应用算法-模型' ,requiresAuth: false},
        children: [
        ]
      },
    ]
  },


  // 机理模型管理用到带字典的
  {
    path: '/flink/MechanismModel',
    redirect: 'noRedirect',
    name:'MechanismModel',
    meta: { title: '机理模型管理', icon: 'el-icon-s-data' },
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'BaseAlgorithmMange',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/mechanism-model/MechanismModel.vue'),
        meta: { title: '模型管理' },
        children: [
        ]
      },
      {
        path: 'Dict',//子路由第一层菜单路径要设置为index?
        name: 'Dict',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/dictionary/sys-dict-type.vue'),
        meta: { title: '字典管理'},
        children: [
          {
            path: '/flink/dict/details',
            name: 'Details',
            component: (resolve) => require([`@/views/flink/dictionary/sys-dict-data`], resolve),
            hidden: true,
            meta: { title: '字典详情' },
            children: []
          },
        ]
      }
    ]
  },


  // {
  //   path: '/flink/domainModel',
  //   redirect: 'noRedirect',
  //   name: 'DomainModel',
  //   meta:{title: '领域模型管理',icon: 'el-icon-menu'},
  //   component: Layout,
  //   children: [
  //     {
  //       path: '/flink/metadata',
  //       name: 'Metadata',
  //       component: () => import('@/views/flink/digital-twin/metadata'),
  //       meta: { title: '元数据处理算法管理' },
  //       children: [
  //       ]
  //     },
  //     {
  //       path: '/flink/dataFusion',
  //       name: 'DataFusion',
  //       component: () => import('@/views/flink/digital-twin/data-fusion'),
  //       meta: { title: '数据融合算法管理' },
  //       children: [
  //       ]
  //     },
  //     {
  //       path: '/flink/modelFusion',
  //       name: 'ModelFusion',
  //       component: () => import('@/views/flink/digital-twin/model-fusion'),
  //       meta: { title: '模型融合算法管理' },
  //       children: [
  //       ]
  //     },
  //     {
  //       path: '/flink/knowledgeMining',
  //       name: 'KnowledgeMining',
  //       component: () => import('@/views/flink/digital-twin/knowledge-mining'),
  //       meta: { title: '知识挖掘算法管理'},
  //       children: [
  //       ]
  //     },
  //     {
  //       path: '/flink/digitalTwin',
  //       name: 'DigitalTwin',
  //       component: () => import('@/views/flink/digital-twin/digital-twin'),
  //       meta: { title: '数字孪生应用算法管理' },
  //       children: [
  //       ]
  //     },
  //     {
  //       path: '/flink/dict',
  //       name: 'Dict',
  //       component: () => import('@/views/flink/dictionary/sys-dict-type'),
  //       meta: { title: '字典管理' },
  //       children: [
  //         {
  //         path: '/flink/dict/details',
  //         name: 'Details',
  //         component: (resolve) => require([`@/views/flink/dictionary/sys-dict-data`], resolve),
  //         hidden: true,
  //         meta: { title: '字典详情' },
  //         children: []
  //       },
  //       ]
  //     },
  //   ]
  // },

  // {
  //   path: '/flink',
  //   name: 'DictManage',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'index',//子路由第一层菜单路径要设置为index?
  //       name: 'Dict',
  //       component: () => import('@/views/flink/dictionary/sys-dict-type'),
  //       meta: { title: '字典管理', icon: 'documentation' },
  //       children: [
  //         {
  //           path: '/flink/dict/details',
  //           name: 'Details',
  //           component: (resolve) => require([`@/views/flink/dictionary/sys-dict-data`], resolve),
  //           hidden: true,
  //           meta: { title: '字典详情' },
  //           children: []
  //         },
  //       ]
  //     }
  //   ]
  // },

  //flink平台核心功能，暂且注释


  {
    path: '/flink/task-manage',
    redirect: 'noRedirect',
    name: 'TaskManage',
    meta: { title: '算法-模型任务管理', icon: 'list' },
    component: Layout,
    children: [
      {
        path: '/flink/task-manage/al/index',
        name: 'Task',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/task-manage/al/index.vue'),
        meta: { title: '微服务任务' },
        children: [
        ]
      },
      {
        path: 'index',
        name: 'FlinkTaskManage',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/task-manage/index.vue'),
        meta: { title: '流式任务(Flink)' },
        children: [
          {
            path: '/flink/task-manage/create_sql_streaming_task',
            name: 'CreateSqlStreamingTask',
            component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
            hidden: true,
            meta: { title: '创建SQL流任务' },
            children: []
          },
          {
            path: '/flink/task-manage/edit_sql_streaming_task',
            name: 'UpdateSqlStreamingTask',
            component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
            hidden: true,
            meta: { title: '编辑SQL流任务' },
            children: []
          },
          {
            path: '/flink/task-manage/view_sql_streaming_task',
            name: 'ViewSqlStreamingTask',
            component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
            hidden: true,
            meta: { title: '查看SQL流任务' },
            children: []
          },
          {
            path: '/flink/task-manage/create_sql_batch_task',
            name: 'CreateSqlBatchTask',
            component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
            hidden: true,
            meta: { title: '创建SQL批任务' },
            children: []
          },
          {
            path: '/flink/task-manage/edit_sql_batch_task',
            name: 'UpdateSqlBatchTask',
            component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
            hidden: true,
            meta: { title: '编辑SQL批任务' },
            children: []
          },
          {
            path: '/flink/task-manage/view_sql_batch_task',
            name: 'ViewSqlBatchTask',
            component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
            hidden: true,
            meta: { title: '查看SQL批任务' },
            children: []
          },
          {
            path: '/flink/task-manage/create_jar_task',
            name: 'CreateJarTask',
            component: (resolve) => require([`@/views/flink/task-manage/jartask.vue`], resolve),
            hidden: true,
            meta: { title: '创建JAR任务' },
            children: []
          },
          {
            path: '/flink/task-manage/edit_jar_task',
            name: 'UpdateJarTask',
            component: (resolve) => require([`@/views/flink/task-manage/jartask.vue`], resolve),
            hidden: true,
            meta: { title: '编辑JAR批任务' },
            children: []
          },
          {
            path: '/flink/task-manage/view_jar_task',
            name: 'ViewJarTask',
            component: (resolve) => require([`@/views/flink/task-manage/jartask.vue`], resolve),
            hidden: true,
            meta: { title: '查看JAR批任务' },
            children: []
          },
          {
            path: '/flink/log-manage/view_logdetail',
            name: 'ViewTaskLogDetail',
            component: (resolve) => require([`@/views/flink/log-manage/logdetail.vue`], resolve),
            hidden: true,
            meta: { title: '查看日志详情' },
            children: []
          }
        ]
      },


      // {
      //   path: '/flink/task-manage/history',
      //   name: 'HistoryTask',
      //   component: (resolve) => require([`@/views/flink/task-manage/history.vue`], resolve),
      //   meta: { title: '历史版本' },
      //   children: [
      //     {
      //       path: '/flink/task-manage/view_sql_streaming_task',
      //       name: 'ViewHistorySqlStreamingTask',
      //       component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
      //       hidden: true,
      //       meta: { title: '查看SQL流任务' },
      //       children: []
      //     },
      //     {
      //       path: '/flink/task-manage/view_sql_batch_task',
      //       name: 'ViewHistorySqlBatchTask',
      //       component: (resolve) => require([`@/views/flink/task-manage/sqltask.vue`], resolve),
      //       hidden: true,
      //       meta: { title: '查看SQL批任务' },
      //       children: []
      //     },
      //     {
      //       path: '/flink/task-manage/view_jar_task',
      //       name: 'ViewHistoryJarTask',
      //       component: (resolve) => require([`@/views/flink/task-manage/jartask.vue`], resolve),
      //       hidden: true,
      //       meta: { title: '查看JAR批任务' },
      //       children: []
      //     }
      //   ]
      // },
    ]
  },


  {
    path: '/flink/system-manage',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'SystemManage',
        component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/flink/system-manage/index.vue'),
        meta: { title: '系统设置', icon: 'el-icon-setting' }
      }
    ]
  }
]

  // {
  //   path: '/flink/log-manage',
  //   name: 'LogManage',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'index',
  //       name: 'FlinkLogManage',
  //       component: () => import('@/views/flink/log-manage/index'),
  //       meta: { title: '运行日志', icon: 'documentation' },
  //       children: [
  //         {
  //           path: '/flink/log-manage/view_logdetail',
  //           name: 'ViewLogDetail',
  //           component: (resolve) => require([`@/views/flink/log-manage/logdetail.vue`], resolve),
  //           hidden: true,
  //           meta: { title: '查看日志详情' },
  //           children: []
  //         }
  //       ]
  //     }
  //   ]
  // },

  // {
  //   path: '/walle/alarm-manage',
  //   redirect: 'noRedirect',
  //   name: 'AlarmManage',
  //   meta: { title: '告警管理', icon: 'el-icon-message-solid' },
  //   component: Layout,
  //   children: [
  //     {
  //       path: '/walle/alarm/alarmcfg',
  //       name: 'AlarmCfg',
  //       component: () => import('@/views/flink/alarm-manage/alarmcfg.vue'),
  //       meta: { title: '告警设置', icon: 'el-icon-s-tools' }
  //     },
  //     {
  //       path: '/walle/alarm/logs',
  //       name: 'AlarmLogs',
  //       component: () => import('@/views/flink/alarm-manage/index.vue'),
  //       meta: { title: '告警日志', icon: 'el-icon-document' }
  //     }
  //   ]
  // },


  // {
  //   path: '/flink/user',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'index',
  //       name: 'UserManage',
  //       meta: { title: '用户管理', icon: 'el-icon-user' },
  //       component: () => import('@/views/flink/user-manage')
  //     }
  //   ]
  // },


  // {
  //   path: '/flink/jarManage',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'index',
  //       name: 'jarManage',
  //       meta: { title: '三方jar管理', icon: 'el-icon-upload' },
  //       component: () => import('@/views/flink/upload')
  //     }
  //   ]
  // },



  // {
  //   path: '/flink/system-contact',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'index',
  //       name: 'Contact',
  //       component: () => import('@/views/flink/system-contact'),
  //       meta: { title: '联系方式', icon: 'el-icon-tickets' }
  //     }
  //   ]
  // }


