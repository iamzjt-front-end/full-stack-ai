import { createRouter, createWebHistory } from 'vue-router'
import Home from '../components/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/about',
      name: 'about',
      // 路由级代码拆分
      // 这将为此路由生成一个单独的块（About.[hash].js）
      // 当访问该路线时，它被延迟加载
      component: () => import('../components/About.vue'),
    },
  ],
})

export default router
