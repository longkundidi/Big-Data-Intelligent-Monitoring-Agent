/** When your routing table is too long, you can split it into small modules **/

import Layout from '../../../../../IdeaProjects/web/flink-algorithm-web-front/src/layout/index.vue'

const tableRouter = {
  path: '/table',
  component: Layout,
  redirect: '/table/complex-table',
  name: 'Table',
  meta: {
    title: 'Table',
    icon: 'table'
  },
  children: [
    {
      path: 'dynamic-table',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/table/dynamic-table/index.vue'),
      name: 'DynamicTable',
      meta: { title: 'dynamicTable' }
    },
    {
      path: 'drag-table',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/table/drag-table.vue'),
      name: 'DragTable',
      meta: { title: 'dragTable' }
    },
    {
      path: 'inline-edit-table',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/table/inline-edit-table.vue'),
      name: 'InlineEditTable',
      meta: { title: 'inlineEditTable' }
    },
    {
      path: 'complex-table',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/table/complex-table.vue'),
      name: 'ComplexTable',
      meta: { title: 'complexTable' }
    }
  ]
}
export default tableRouter
