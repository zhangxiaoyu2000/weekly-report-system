<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Navigation -->
    <nav class="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
      <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <!-- Logo -->
            <div class="flex-shrink-0 flex items-center">
              <router-link :to="homeRoute" class="flex items-center">
                <div class="rounded-lg mr-3">
                  <img 
                    src="@/assets/images/logo.jpg" 
                    alt="WeeklyReport Logo" 
                    class="h-10 w-16 rounded-lg object-cover"
                  />
                </div>
                <span class="text-xl font-bold text-gray-900 dark:text-white">WeeklyReport</span>
              </router-link>
            </div>
            
            <!-- Navigation Links -->
            <div class="hidden sm:ml-6 sm:flex sm:items-center sm:space-x-8">
              <router-link
                v-for="item in visibleNavigation"
                :key="item.name"
                :to="item.href"
                class="inline-flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
                :class="$route.path === item.href || $route.path.startsWith(item.href + '/') 
                  ? 'bg-primary-100 text-primary-700 dark:bg-primary-900 dark:text-primary-300' 
                  : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-300 dark:hover:bg-gray-700'"
              >
                <component :is="item.icon" class="h-5 w-5 mr-2" />
                {{ item.name }}
              </router-link>
            </div>
          </div>

          <div class="hidden sm:ml-6 sm:flex sm:items-center space-x-4">
            <!-- User Menu -->
            <div class="relative" ref="userMenuContainer">
              <button
                @click="toggleUserMenu"
                class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                <img
                  v-if="authStore.user?.avatar"
                  class="h-8 w-8 rounded-full"
                  :src="authStore.user.avatar"
                  :alt="authStore.user.username"
                />
                <div
                  v-else
                  class="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center"
                >
                  <span class="text-sm font-medium text-white">
                    {{ authStore.user?.username?.charAt(0).toUpperCase() }}
                  </span>
                </div>
              </button>

              <!-- User Dropdown -->
              <Transition
                enter-active-class="transition ease-out duration-200"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 z-10 mt-2 w-48 origin-top-right bg-white dark:bg-gray-800 rounded-md py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"
                  @click.stop
                >
                  <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">{{ authStore.user?.username }}</p>
                    <p class="text-sm text-gray-500 dark:text-gray-400">{{ authStore.user?.email }}</p>
                  </div>
                  <button
                    @click="openUpdateProfileModal"
                    class="flex items-center w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    更新基本信息
                  </button>
                  <button
                    @click="openChangePasswordModal"
                    class="flex items-center w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-6 6H5a6 6 0 01-6-6V9a6 6 0 016-6h2m8 6V9a2 2 0 00-2-2H9a2 2 0 00-2 2v10.5a2 2 0 002 2h6a2 2 0 002-2V9z" />
                    </svg>
                    修改密码
                  </button>
                  <button
                    @click="handleLogout"
                    class="flex items-center w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 border-t border-gray-200 dark:border-gray-700"
                  >
                    <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </Transition>
            </div>
          </div>

          <!-- Mobile menu button -->
          <div class="flex items-center sm:hidden">
            <button
              @click="showMobileMenu = !showMobileMenu"
              class="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path v-if="!showMobileMenu" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile menu -->
      <div v-if="showMobileMenu" class="sm:hidden">
        <div class="pt-2 pb-3 space-y-1">
          <router-link
            v-for="item in visibleNavigation"
            :key="item.name"
            :to="item.href"
            @click="showMobileMenu = false"
            class="flex items-center pl-3 pr-4 py-2 border-l-4 text-base font-medium transition-colors duration-200"
            :class="$route.path === item.href || $route.path.startsWith(item.href + '/') 
              ? 'bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900/50 dark:text-primary-300' 
              : 'border-transparent text-gray-600 hover:bg-gray-50 hover:border-gray-300 hover:text-gray-800 dark:text-gray-400 dark:hover:bg-gray-700'"
          >
            <component :is="item.icon" class="h-5 w-5 mr-3" />
            {{ item.name }}
          </router-link>
        </div>
        <div class="pt-4 pb-3 border-t border-gray-200 dark:border-gray-700">
          <div class="flex items-center px-4">
            <div class="flex-shrink-0">
              <img
                v-if="authStore.user?.avatar"
                class="h-10 w-10 rounded-full"
                :src="authStore.user.avatar"
                :alt="authStore.user.username"
              />
              <div
                v-else
                class="h-10 w-10 rounded-full bg-primary-600 flex items-center justify-center"
              >
                <span class="text-base font-medium text-white">
                  {{ authStore.user?.username?.charAt(0).toUpperCase() }}
                </span>
              </div>
            </div>
            <div class="ml-3">
              <div class="text-base font-medium text-gray-800 dark:text-white">{{ authStore.user?.fullName || authStore.user?.username }}</div>
              <div class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ getRoleDisplayName(authStore.user?.role) }}</div>
            </div>
          </div>
          <div class="mt-3 space-y-1">
            <button
              @click="openUpdateProfileModal"
              class="flex items-center w-full text-left px-4 py-2 text-base font-medium text-gray-500 hover:text-gray-800 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-gray-300"
            >
              <svg class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              更新基本信息
            </button>
            <button
              @click="openChangePasswordModal"
              class="flex items-center w-full text-left px-4 py-2 text-base font-medium text-gray-500 hover:text-gray-800 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-gray-300"
            >
              <svg class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-6 6H5a6 6 0 01-6-6V9a6 6 0 016-6h2m8 6V9a2 2 0 00-2-2H9a2 2 0 00-2 2v10.5a2 2 0 002 2h6a2 2 0 002-2V9z" />
              </svg>
              修改密码
            </button>
            <button
              @click="handleLogout"
              class="flex items-center w-full text-left px-4 py-2 text-base font-medium text-gray-500 hover:text-gray-800 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-gray-300"
            >
              <svg class="h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              退出登录
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="mx-auto max-w-7xl py-6 px-4 sm:px-6 lg:px-8 bg-gray-50 dark:bg-gray-900">
      <router-view />
    </main>

    <!-- 模态框 -->
    <UpdateProfileModal 
      :is-open="showUpdateProfileModal" 
      @close="showUpdateProfileModal = false"
      @success="handleProfileUpdateSuccess"
    />
    <ChangePasswordModal 
      :is-open="showChangePasswordModal" 
      @close="showChangePasswordModal = false"
      @success="handlePasswordChangeSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import UpdateProfileModal from './UpdateProfileModal.vue'
import ChangePasswordModal from './ChangePasswordModal.vue'

const router = useRouter()
const authStore = useAuthStore()

const showUserMenu = ref(false)
const showMobileMenu = ref(false)
const userMenuContainer = ref<HTMLElement | null>(null)

// 模态框状态
const showUpdateProfileModal = ref(false)
const showChangePasswordModal = ref(false)

// Navigation items
const navigation = [
  // 主管专用页面
  { 
    name: '创建周报', 
    href: '/app/create-report',
    roles: ['MANAGER'], // 主管创建周报
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M12 6v6m0 0v6m0-6h6m-6 0H6'
      })
    ])
  },
  { 
    name: '我的周报列表', 
    href: '/app/reports',
    roles: ['MANAGER'], // 主管查看自己的周报列表
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
      })
    ])
  },
  { 
    name: '项目管理', 
    href: '/app/projects',
    roles: ['MANAGER'], // 主管管理项目
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10'
      })
    ])
  },
  
  // 管理员专用页面
  { 
    name: '项目审核', 
    href: '/app/project-approval',
    roles: ['ADMIN'], // 管理员审核项目
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
      })
    ])
  },
  { 
    name: '周报审核', 
    href: '/app/admin-reports',
    roles: ['ADMIN'], // 管理员审核周报
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
      })
    ])
  },

  // 超级管理员专用页面
  { 
    name: '项目管理', 
    href: '/app/super-admin-projects',
    roles: ['SUPER_ADMIN'], // 超级管理员项目管理
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10'
      })
    ])
  },
  { 
    name: '周报管理', 
    href: '/app/super-admin-reports',
    roles: ['SUPER_ADMIN'], // 超级管理员周报管理
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
      })
    ])
  },
  { 
    name: '用户管理', 
    href: '/app/user-management',
    roles: ['SUPER_ADMIN'], // 超级管理员用户管理
    icon: h('svg', {
      fill: 'none',
      viewBox: '0 0 24 24', 
      stroke: 'currentColor',
      class: 'h-5 w-5'
    }, [
      h('path', {
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
        'stroke-width': '2',
        d: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z'
      })
    ])
  },
]

// Filter navigation based on user role
const visibleNavigation = computed(() => {
  const userRole = authStore.user?.role
  if (!userRole) return []
  
  return navigation.filter(item => item.roles.includes(userRole))
})

// Determine home route based on user role
const homeRoute = computed(() => {
  const userRole = authStore.user?.role
  if (userRole === 'SUPER_ADMIN') {
    return '/app/super-admin-projects'
  } else if (userRole === 'ADMIN') {
    return '/app/admin-reports'
  } else if (userRole === 'MANAGER') {
    return '/app/create-report'
  }
  return '/login'
})

function getRoleDisplayName(role: string | undefined): string {
  if (!role) return ''
  
  const roleNames: Record<string, string> = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'MANAGER': '主管'
  }
  
  return roleNames[role] || role
}


function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function closeUserMenu() {
  showUserMenu.value = false
}

async function handleLogout() {
  showUserMenu.value = false
  showMobileMenu.value = false
  await authStore.logout()
  router.push('/login')
}

// 处理外部点击关闭用户菜单
function handleClickOutside(event: Event) {
  if (userMenuContainer.value && !userMenuContainer.value.contains(event.target as Node)) {
    closeUserMenu()
  }
}

// 模态框控制方法
function openUpdateProfileModal() {
  showUserMenu.value = false
  showMobileMenu.value = false
  showUpdateProfileModal.value = true
}

function openChangePasswordModal() {
  showUserMenu.value = false
  showMobileMenu.value = false
  showChangePasswordModal.value = true
}

function handleProfileUpdateSuccess() {
  // 显示成功提示
  alert('基本信息更新成功')
}

function handlePasswordChangeSuccess() {
  // 密码修改成功的提示已在组件内处理
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>