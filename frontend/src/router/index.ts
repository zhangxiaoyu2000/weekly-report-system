import { createRouter, createWebHistory, RouteRecordRaw, NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTabsStore } from '@/stores/tabs'
import { useLayoutStore } from '@/stores/layout'
import { ElLoading, ElMessage } from 'element-plus'

// Import layouts
import AdminLayout from '@/layouts/AdminLayout.vue'
import SimpleLayout from '@/layouts/SimpleLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'

// Legacy layout for compatibility
import Layout from '@/components/Layout.vue'

// Type definitions
interface RouteMeta {
  title?: string
  icon?: string
  requiresAuth?: boolean
  roles?: string[]
  keepAlive?: boolean
  noTab?: boolean
  layout?: 'admin' | 'simple' | 'mobile'
  guest?: boolean
  mobile?: boolean
  hideBreadcrumb?: boolean
  breadcrumbTitle?: string
}

// Route constants
export const ROUTE_NAMES = {
  LOGIN: 'Login',
  DASHBOARD: 'Dashboard',
  NOT_FOUND: 'NotFound'
} as const

export const PUBLIC_ROUTES = ['/login', '/register', '/forgot-password', '/404']
export const ADMIN_ONLY_ROUTES = ['/admin', '/settings']
export const MANAGER_ROUTES = ['/reports/approve']

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true, layout: 'admin' } as RouteMeta,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { 
          title: '仪表板', 
          icon: 'Dashboard',
          requiresAuth: true,
          keepAlive: true
        } as RouteMeta
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/Reports.vue'),
        meta: { 
          title: '周报管理', 
          icon: 'Document',
          requiresAuth: true,
          keepAlive: true
        } as RouteMeta
      },
      {
        path: 'reports/create',
        name: 'CreateReport',
        component: () => import('@/views/CreateReport.vue'),
        meta: { 
          title: '创建周报', 
          icon: 'EditPen',
          requiresAuth: true,
          roles: ['user', 'manager', 'admin']
        } as RouteMeta
      },
      {
        path: 'reports/:id',
        name: 'ReportDetail',
        component: () => import('@/views/ReportDetail.vue'),
        meta: { 
          title: '周报详情', 
          icon: 'View',
          requiresAuth: true,
          keepAlive: false
        } as RouteMeta,
        props: true
      },
      {
        path: 'reports/approve',
        name: 'ReportApprove',
        component: () => import('@/views/ReportApprove.vue'),
        meta: { 
          title: '周报审批', 
          icon: 'Check',
          requiresAuth: true,
          roles: ['manager', 'admin']
        } as RouteMeta
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { 
          title: '个人资料', 
          icon: 'User',
          requiresAuth: true,
          keepAlive: true
        } as RouteMeta
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { 
          title: '系统设置', 
          icon: 'Setting',
          requiresAuth: true,
          roles: ['admin']
        } as RouteMeta
      }
    ]
  },
  {
    path: '/mobile',
    component: MobileLayout,
    redirect: '/mobile/dashboard',
    meta: { requiresAuth: true, layout: 'mobile' } as RouteMeta,
    children: [
      {
        path: 'dashboard',
        name: 'MobileDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { 
          title: '首页', 
          requiresAuth: true,
          mobile: true,
          noTab: true
        } as RouteMeta
      },
      {
        path: 'reports',
        name: 'MobileReports',
        component: () => import('@/views/Reports.vue'),
        meta: { 
          title: '周报', 
          requiresAuth: true,
          mobile: true,
          noTab: true
        } as RouteMeta
      },
      {
        path: 'reports/create',
        name: 'MobileCreateReport',
        component: () => import('@/views/CreateReport.vue'),
        meta: { 
          title: '创建', 
          requiresAuth: true,
          mobile: true,
          noTab: true
        } as RouteMeta
      },
      {
        path: 'profile',
        name: 'MobileProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { 
          title: '我的', 
          requiresAuth: true,
          mobile: true,
          noTab: true
        } as RouteMeta
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: SimpleLayout,
    props: { showHeader: true, showFooter: false, centered: true },
    meta: { title: '登录', layout: 'simple', guest: true } as RouteMeta,
    children: [
      {
        path: '',
        component: () => import('@/views/Login.vue')
      }
    ]
  },
  {
    path: '/register',
    name: 'Register',
    component: SimpleLayout,
    props: { showHeader: true, showFooter: false, centered: true },
    meta: { title: '注册', layout: 'simple', guest: true } as RouteMeta,
    children: [
      {
        path: '',
        component: () => import('@/views/Register.vue')
      }
    ]
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: SimpleLayout,
    props: { showHeader: true, showFooter: false, centered: true },
    meta: { title: '忘记密码', layout: 'simple', guest: true } as RouteMeta,
    children: [
      {
        path: '',
        component: () => import('@/views/ForgotPassword.vue')
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: SimpleLayout,
    props: { showHeader: true, showFooter: true },
    meta: { title: '页面不存在', layout: 'simple' } as RouteMeta,
    children: [
      {
        path: '',
        component: () => import('@/views/404.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // Always scroll to top when navigating to a new route
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// Global loading state
let loading: any = null

// Permission check function
export function hasPermission(userRole: string, requiredRoles?: string[]): boolean {
  if (!requiredRoles || requiredRoles.length === 0) return true
  if (userRole === 'admin') return true
  return requiredRoles.includes(userRole)
}

// Route layout resolver
function resolveLayout(to: RouteLocationNormalized): string {
  const layoutStore = useLayoutStore()
  
  // Check if mobile device
  if (layoutStore.isMobile && !to.meta?.guest) {
    return 'mobile'
  }
  
  // Use route meta layout or default
  return to.meta?.layout || 'admin'
}

// Enhanced navigation guards
router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
  // Show loading for non-guest routes
  if (!to.meta?.guest) {
    loading = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }

  try {
    const userStore = useUserStore()
    const tabsStore = useTabsStore()
    const layoutStore = useLayoutStore()
    
    // Set page title
    document.title = to.meta?.title ? `${to.meta.title} - 周报系统` : '周报系统'
    
    // Initialize layout store
    if (!layoutStore.device) {
      layoutStore.initLayout()
    }
    
    // Handle mobile redirects
    if (layoutStore.isMobile && !to.path.startsWith('/mobile') && !PUBLIC_ROUTES.includes(to.path)) {
      const mobilePath = `/mobile${to.path === '/' ? '/dashboard' : to.path}`
      next(mobilePath)
      return
    }
    
    // Check if route requires authentication
    if (to.meta?.requiresAuth) {
      if (!userStore.isAuthenticated) {
        ElMessage.warning('请先登录')
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return
      }
      
      // Check role permissions
      if (to.meta?.roles) {
        const userRole = userStore.userRole
        if (!hasPermission(userRole, to.meta.roles)) {
          ElMessage.error('权限不足')
          next('/404')
          return
        }
      }
      
      // Add tab for authenticated routes
      if (!to.meta?.noTab && to.meta?.title) {
        tabsStore.addTab({
          path: to.path,
          name: to.name as string,
          meta: to.meta as RouteMeta
        })
      }
    }
    
    // Redirect authenticated users away from guest-only pages
    if (to.meta?.guest && userStore.isAuthenticated) {
      const redirectPath = to.query.redirect as string || (layoutStore.isMobile ? '/mobile' : '/')
      next(redirectPath)
      return
    }
    
    // Handle admin-only routes
    if (ADMIN_ONLY_ROUTES.some(route => to.path.startsWith(route))) {
      if (userStore.userRole !== 'admin') {
        ElMessage.error('需要管理员权限')
        next('/404')
        return
      }
    }
    
    // Handle manager routes
    if (MANAGER_ROUTES.some(route => to.path.startsWith(route))) {
      if (!['manager', 'admin'].includes(userStore.userRole)) {
        ElMessage.error('需要主管或管理员权限')
        next('/404')
        return
      }
    }
    
    // Proceed with navigation
    next()
    
  } catch (error) {
    console.error('Router navigation error:', error)
    ElMessage.error('页面加载失败')
    next('/404')
  }
})

router.afterEach((to: RouteLocationNormalized, from: RouteLocationNormalized) => {
  // Hide loading
  if (loading) {
    loading.close()
    loading = null
  }
  
  // Update layout based on route
  const layoutStore = useLayoutStore()
  const resolvedLayout = resolveLayout(to)
  if (layoutStore.layoutMode !== resolvedLayout) {
    layoutStore.setLayoutMode(resolvedLayout)
  }
  
  // Track page view (analytics could go here)
  if (typeof gtag !== 'undefined') {
    gtag('config', 'GA_TRACKING_ID', {
      page_path: to.path
    })
  }
})

// Handle router errors
router.onError((error) => {
  console.error('Router error:', error)
  if (loading) {
    loading.close()
    loading = null
  }
  ElMessage.error('页面加载失败，请刷新重试')
})

export default router