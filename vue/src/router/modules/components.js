/** When your routing table is too long, you can split it into small modules **/

import Layout from '../../../../../IdeaProjects/web/flink-algorithm-web-front/src/layout/index.vue'

const componentsRouter = {
  path: '/components',
  component: Layout,
  redirect: 'noRedirect',
  name: 'ComponentDemo',
  meta: {
    title: 'components',
    icon: 'component'
  },
  children: [
    {
      path: 'tinymce',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/tinymce.vue'),
      name: 'TinymceDemo',
      meta: { title: 'tinymce' }
    },
    {
      path: 'markdown',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/markdown.vue'),
      name: 'MarkdownDemo',
      meta: { title: 'markdown' }
    },
    {
      path: 'json-editor',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/json-editor.vue'),
      name: 'JsonEditorDemo',
      meta: { title: 'jsonEditor' }
    },
    {
      path: 'split-pane',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/split-pane.vue'),
      name: 'SplitpaneDemo',
      meta: { title: 'splitPane' }
    },
    {
      path: 'avatar-upload',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/avatar-upload.vue'),
      name: 'AvatarUploadDemo',
      meta: { title: 'avatarUpload' }
    },
    {
      path: 'dropzone',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/dropzone.vue'),
      name: 'DropzoneDemo',
      meta: { title: 'dropzone' }
    },
    {
      path: 'sticky',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/sticky.vue'),
      name: 'StickyDemo',
      meta: { title: 'sticky' }
    },
    {
      path: 'count-to',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/count-to.vue'),
      name: 'CountToDemo',
      meta: { title: 'countTo' }
    },
    {
      path: 'mixin',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/mixin.vue'),
      name: 'ComponentMixinDemo',
      meta: { title: 'componentMixin' }
    },
    {
      path: 'back-to-top',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/back-to-top.vue'),
      name: 'BackToTopDemo',
      meta: { title: 'backToTop' }
    },
    {
      path: 'drag-dialog',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/drag-dialog.vue'),
      name: 'DragDialogDemo',
      meta: { title: 'dragDialog' }
    },
    {
      path: 'drag-select',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/drag-select.vue'),
      name: 'DragSelectDemo',
      meta: { title: 'dragSelect' }
    },
    {
      path: 'dnd-list',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/dnd-list.vue'),
      name: 'DndListDemo',
      meta: { title: 'dndList' }
    },
    {
      path: 'drag-kanban',
      component: () => import('../../../../../IdeaProjects/web/flink-algorithm-web-front/src/views/components-demo/drag-kanban.vue'),
      name: 'DragKanbanDemo',
      meta: { title: 'dragKanban' }
    }
  ]
}

export default componentsRouter
