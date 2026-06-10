import { createRouter, createWebHistory } from 'vue-router'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/home' },
    {
      path: '/home',
      name: 'home',
      meta: { title: '推荐' },
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/recommend',
      name: 'recommend',
      meta: { title: '视频推荐' },
      component: () => import('../views/RecommendView.vue'),
    },
    {
      path: '/upload',
      name: 'upload',
      meta: { title: '发布视频' },
      component: () => import('../views/UploadView.vue'),
    },
    {
      path: '/my-videos',
      name: 'my-videos',
      meta: { title: '我的视频' },
      component: () => import('../views/MyVideosView.vue'),
    },
    {
      path: '/my-likes',
      name: 'my-likes',
      meta: { title: '我的点赞' },
      component: () => import('../views/MyLikesView.vue'),
    },
    {
      path: '/my-favorites',
      name: 'my-favorites',
      meta: { title: '我的收藏' },
      component: () => import('../views/MyFavoritesView.vue'),
    },
  ],
})

export default router
