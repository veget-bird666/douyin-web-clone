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
  ],
})

export default router
