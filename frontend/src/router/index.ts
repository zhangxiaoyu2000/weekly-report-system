import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/AppLayout.vue'

const router = createRouter({
  history: createWebHistory('/'),
  routes: [
    {
      path: '/',
      name: 'Home',
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore()
        if (!authStore.isAuthenticated) {
          next('/login')
        } else {
          const userRole = authStore.user?.role
          if (userRole === 'SUPER_ADMIN') {
            next('/app/super-admin-projects')
          } else if (userRole === 'ADMIN') {
            next('/app/admin-reports')
          } else if (userRole === 'MANAGER') {
            next('/app/create-report')
          } else {
            next('/login')
          }
        }
      }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    // 注册功能已禁用，用户由超级管理员在用户管理页面创建
    // {
    //   path: '/register',
    //   name: 'Register', 
    //   component: () => import('@/views/RegisterView.vue'),
    //   meta: { requiresGuest: true }
    // },
    {
      path: '/app',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        // Report Management
        {
          path: 'create-report',
          name: 'CreateReport',
          component: () => import('@/views/CreateReportView.vue'),
          meta: { requiresRoles: ['MANAGER'] }
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/ReportsView.vue'),
          meta: { requiresRoles: ['MANAGER'] }
        },
        {
          path: 'reports/:id/edit',
          name: 'EditReport',
          component: () => import('@/views/EditReportView.vue'),
          meta: { requiresRoles: ['MANAGER'] }
        },
        {
          path: 'all-reports',
          name: 'AllReports',
          component: () => import('@/views/AllReportsView.vue'),
          meta: { requiresRoles: ['ADMIN', 'SUPER_ADMIN'] }
        },
        {
          path: 'review-reports',
          name: 'ReviewReports',
          component: () => import('@/views/ReviewReportsView.vue'),
          meta: { requiresRoles: ['ADMIN', 'SUPER_ADMIN'] }
        },

        // Project Management
        {
          path: 'projects',
          name: 'Projects',
          component: () => import('@/views/ProjectsView.vue'),
          meta: { requiresRoles: ['MANAGER', 'ADMIN', 'SUPER_ADMIN'] }
        },
        {
          path: 'projects/create',
          name: 'CreateProject',
          component: () => import('@/views/CreateProjectView.vue'),
          meta: { requiresRoles: ['MANAGER'] }
        },
        {
          path: 'projects/:id',
          name: 'ProjectDetail',
          component: () => import('@/views/ProjectDetailView.vue'),
          meta: { requiresRoles: ['MANAGER', 'ADMIN', 'SUPER_ADMIN'] }
        },
        {
          path: 'projects/edit/:id',
          name: 'EditProject',
          component: () => import('@/views/EditProjectView.vue'),
          meta: { requiresRoles: ['MANAGER', 'ADMIN', 'SUPER_ADMIN'] }
        },

        {
          path: 'approval-history',
          name: 'ApprovalHistory',
          component: () => import('@/views/ApprovalHistoryView.vue'),
          meta: { requiresRoles: ['ADMIN'] }
        },

        // User Management (Super Admin Only)
        {
          path: 'user-management',
          name: 'UserManagement',
          component: () => import('@/views/UserManagementView.vue'),
          meta: { requiresRoles: ['SUPER_ADMIN'] }
        },

        // Super Admin Project and Report Management
        {
          path: 'super-admin-projects',
          name: 'SuperAdminProjects',
          component: () => import('@/views/SuperAdminProjectsView.vue'),
          meta: { requiresRoles: ['SUPER_ADMIN'] }
        },
        {
          path: 'super-admin-reports',
          name: 'SuperAdminReports',
          component: () => import('@/views/SuperAdminReportsView.vue'),
          meta: { requiresRoles: ['SUPER_ADMIN'] }
        },

        // Admin Project and Report Approval
        {
          path: 'project-approval',
          name: 'ProjectApproval',
          component: () => import('@/views/ProjectApprovalView.vue'),
          meta: { requiresRoles: ['ADMIN'] }
        },
        {
          path: 'admin-reports',
          name: 'AdminReports',
          component: () => import('@/views/AdminReportsView.vue'),
          meta: { requiresRoles: ['ADMIN'] }
        },

        // General Pages
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/ProfileView.vue')
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/SettingsView.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      redirect: '/login'
    }
  ]
})

// 导航守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 等待认证状态初始化
  if (!authStore.initialized) {
    console.log('⏳ Waiting for auth initialization...')
    await authStore.initialize()
  }
  
  const isAuthenticated = authStore.isAuthenticated
  const userRole = authStore.user?.role
  
  console.log('🔍 Router guard check:', {
    from: from.path,
    to: to.path,
    isAuthenticated,
    userRole,
    initialized: authStore.initialized,
    hasToken: !!authStore.token,
    hasUser: !!authStore.user,
    requiresAuth: !!to.meta.requiresAuth,
    requiresGuest: !!to.meta.requiresGuest,
    requiresRoles: to.meta.requiresRoles
  })
  
  // 检查认证要求
  if (to.meta.requiresAuth && !isAuthenticated) {
    console.log('🚫 Access denied - authentication required, redirecting to login')
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  // 检查访客页面要求  
  if (to.meta.requiresGuest && isAuthenticated) {
    // 根据用户角色重定向到适当的默认页面
    let defaultRoute = '/app/create-report'
    if (userRole === 'SUPER_ADMIN') {
      defaultRoute = '/app/super-admin-projects'
    } else if (userRole === 'ADMIN') {
      defaultRoute = '/app/admin-reports'
    }
    console.log('🔄 Already authenticated, redirecting to:', defaultRoute)
    next(defaultRoute)
    return
  }
  
  // 检查角色权限要求
  if (to.meta.requiresRoles && userRole) {
    const requiredRoles = to.meta.requiresRoles as string[]
    if (!requiredRoles.includes(userRole)) {
      // 如果用户没有权限访问页面，重定向到用户的默认页面
      let defaultRoute = '/app/create-report'
      if (userRole === 'SUPER_ADMIN') {
        defaultRoute = '/app/super-admin-projects'
      } else if (userRole === 'ADMIN') {
        defaultRoute = '/app/admin-reports'
      }
      console.log('🚫 Role access denied, redirecting to:', defaultRoute)
      next({ path: defaultRoute, query: { error: 'access_denied' } })
      return
    }
  }
  
  console.log('✅ Navigation allowed to:', to.path)
  next()
})

export default router