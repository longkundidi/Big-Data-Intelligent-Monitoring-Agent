/** When your routing table is too long, you can split it into small modules**/

import Layout from '../../../../../IdeaProjects/web/flink-algorithm-web-front/src/layout/index.vue'

const chartsRouter = {
  path: '/charts',
  component: Layout,
  redirect: 'noRedirect',
  name: 'Charts',
  meta: {
    title: 'charts',
    icon: 'chart'
  },
  children: [
    {
      path: 'keyboard',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/charts/keyboard.vue'),
      name: 'KeyboardChart',
      meta: { title: 'keyboardChart', noCache: true }
    },
    {
      path: 'line',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/charts/line.vue'),
      name: 'LineChart',
      meta: { title: 'lineChart', noCache: true }
    },
    {
      path: 'mix-chart',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/charts/mix-chart.vue'),
      name: 'MixChart',
      meta: { title: 'mixChart', noCache: true }
    }
  ]
}

export default chartsRouter
