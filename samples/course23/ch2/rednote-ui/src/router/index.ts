import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      // 需要认证的路由
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/auth/register',
      name: 'register',
      // 当访问该路径时，它被延迟加载
      component: () => import('../views/RegistrationForm.vue'),
    },
    {
      path: '/auth/login',
      name: 'login',
      component: () => import('../views/LoginForm.vue'),
    },
    {
      path: '/user/profile',
      // 重定向到指定用户ID的页面
      redirect: to => {
        // 获取用户ID需要到全局守卫中处理
        return { name: 'profile-placeholder'}
      },
      meta: {
        requiresAuth: true
      }
    },
    // 临时占位路由，用于在全局守卫中处理重定向
    {
      path: '/user/profile-placeholder',
      name: 'profile-placeholder',
      component: { template: '<div>Loading...</div>' },
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/user/profile/:userId',
      name: 'user-profile',
      component: () => import('../views/UserProfile.vue'),
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/user/edit',
      name: 'user-profile-edit',
      component: () => import('../views/UserProfileEdit.vue'),
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/user/change-password',
      name: 'user-change-password',
      component: () => import('../views/UserChangePassword.vue'),
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/note/publish',
      name: 'note-publish',
      component: () => import('../views/NotePublish.vue'),
      meta: {
        requiresAuth: true,
        requiresRole: 'USER'
      }
    },
    {
      path: '/note/:noteId',
      name: 'note-detail',
      component: () => import('../views/NoteDetail.vue'),
      meta: {
        requiresAuth: true,
        requiresRole: 'USER'
      }
    },
    {
      path: '/note/:noteId/edit',
      name: 'note-edit',
      component: () => import('../views/NoteEdit.vue'),
      meta: {
        requiresAuth: true,
        requiresRole: 'USER'
      }
    },
    {
      path: '/explore',
      name: 'explore',
      component: () => import('../views/Explore.vue'),
      meta: {
        requiresAuth: true,
        requiresRole: 'USER'
      }
    },
    {
      path: '/403-error',
      name: 'forbidden',
      component: () => import('../views/Forbidden.vue')
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: {
        requiresAuth: true,
        requiresRole: 'ADMIN'
      },
      children: [
        {
          path: '', 
          name: 'admin-redirect-dashboard',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../components/AdminDashboard.vue'),
        },
        {
          path: 'user',
          name: 'admin-user',
          component: () => import('../components/AdminUser.vue'),
        },
        {
          path: 'note',
          name: 'admin-note',
          component: () => import('../components/AdminNote.vue'),
        },
        {
          path: 'comment',
          name: 'admin-comment',
          component: () => import('../components/AdminComment.vue'),
        },
        {
          path: 'user/:userId/edit',
          name: 'admin-user-edit',
          component: () => import('../components/AdminUserEdit.vue'),
        }
      ]
    },
  ],
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  console.log('路由切换before：' + from.name?.toString() + ' -> ' + to.name?.toString())

  // 获取useAuthStore实例
  const authStore = useAuthStore()

  // 检查是否需要认证
  if (to.meta.requiresAuth && !authStore.getIsAuthenticated) {
    // 跳转到登录页面
    return next({ name: 'login' })
  } 

  console.log('authStore.getUser', authStore.getUser)
  console.log('authStore.getIsAuthenticated', authStore.getIsAuthenticated)
  // 如果用户已登录，但没有加载用户信息，则先加载用户信息
  if (authStore.getIsAuthenticated && !authStore.getUser) {
    try {
      await authStore.fetchUser()
    } catch (error) {
      authStore.logout()
      next({ name: 'login' })
    }
  }

  // 校验角色
  if(to.meta.requiresRole && !authStore.hasRole(to.meta.requiresRole)) {
    next({ name: 'forbidden' })
  }

  // 获取用户ID
  if (to.name === 'profile-placeholder' && authStore.getUser) {
    next({ name: 'user-profile', params: { userId: (authStore.getUser as any).userId } })
  } else if (to.name === 'home' && authStore.hasRole('USER')) {
    // 跳转从Home到Explore页面
    next({ name: 'explore'})
  } else if (to.name === 'home' && authStore.hasRole('ADMIN')) {
    // 跳转从Home到Admin页面
    next({ name: 'admin'})
  } else {
    next()
  }

})

export default router
