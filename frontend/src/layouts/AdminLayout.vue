<template>
  <el-container class="admin-layout">
    <!-- Sidebar -->
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo">
        <h2 v-if="!sidebarCollapsed">周报系统</h2>
        <h2 v-else>周</h2>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        unique-opened
        :collapse="sidebarCollapsed"
        background-color="#001529"
        text-color="#ffffff"
        active-text-color="#1890ff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Dashboard /></el-icon>
          <template #title><span>仪表板</span></template>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Document /></el-icon>
          <template #title><span>周报管理</span></template>
        </el-menu-item>
        <el-menu-item index="/reports/create">
          <el-icon><EditPen /></el-icon>
          <template #title><span>创建周报</span></template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title><span>个人资料</span></template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- Main Container -->
    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <!-- Sidebar toggle -->
          <el-button 
            type="text" 
            @click="toggleSidebar"
            class="sidebar-toggle"
          >
            <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
          </el-button>
          
          <!-- Breadcrumb -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- Theme Toggle -->
          <el-tooltip content="切换主题" placement="bottom">
            <el-button 
              type="text" 
              @click="toggleTheme"
              class="theme-toggle"
            >
              <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- Fullscreen -->
          <el-tooltip content="全屏" placement="bottom">
            <el-button 
              type="text" 
              @click="toggleFullscreen"
              class="fullscreen-btn"
            >
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- User Dropdown -->
          <el-dropdown @command="handleUserAction" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :src="userAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- Tabs Navigation -->
      <div class="tabs-container" v-if="showTabs">
        <el-tabs 
          v-model="activeTab" 
          type="card" 
          closable 
          @tab-remove="removeTab"
          @tab-click="handleTabClick"
          class="route-tabs"
        >
          <el-tab-pane 
            v-for="tab in tabs" 
            :key="tab.path" 
            :label="tab.title" 
            :name="tab.path"
          />
        </el-tabs>
      </div>
      
      <!-- Main Content -->
      <el-main class="main-content">
        <transition name="fade-transform" mode="out-in">
          <router-view />
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useLayoutStore } from '@/stores/layout'
import { useTabsStore } from '@/stores/tabs'
import {
  Monitor as Dashboard,
  Document,
  Edit as EditPen,
  User,
  ArrowDown,
  Fold,
  Expand,
  Sunny,
  Moon,
  FullScreen
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const layoutStore = useLayoutStore()
const tabsStore = useTabsStore()

// Computed properties
const activeMenu = computed(() => route.path)
const userName = computed(() => userStore.userName || '用户')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')
const sidebarCollapsed = computed(() => layoutStore.sidebarCollapsed)
const sidebarWidth = computed(() => sidebarCollapsed.value ? '64px' : '250px')
const isDark = computed(() => layoutStore.isDark)
const showTabs = computed(() => layoutStore.showTabs)
const tabs = computed(() => tabsStore.tabs)
const activeTab = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

// Methods
const toggleSidebar = () => {
  layoutStore.toggleSidebar()
}

const toggleTheme = () => {
  layoutStore.toggleTheme()
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleUserAction = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      // Handle settings
      break
    case 'logout':
      userStore.logout()
      tabsStore.clearTabs()
      router.push('/login')
      break
  }
}

const removeTab = (targetName) => {
  tabsStore.removeTab(targetName)
  if (targetName === route.path) {
    const lastTab = tabs.value[tabs.value.length - 1]
    if (lastTab) {
      router.push(lastTab.path)
    } else {
      router.push('/dashboard')
    }
  }
}

const handleTabClick = (tab) => {
  router.push(tab.props.name)
}

// Watch route changes to add tabs
watch(
  () => route.path,
  (newPath) => {
    if (route.meta?.title && newPath !== '/login') {
      tabsStore.addTab({
        path: newPath,
        title: route.meta.title,
        name: route.name
      })
    }
  },
  { immediate: true }
)

onMounted(() => {
  // Apply theme class to document
  document.documentElement.classList.toggle('dark', isDark.value)
})

// Watch theme changes
watch(isDark, (newVal) => {
  document.documentElement.classList.toggle('dark', newVal)
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px !important;
}

.logo {
  padding: 20px;
  text-align: center;
  color: #ffffff;
  border-bottom: 1px solid #1f1f1f;
  white-space: nowrap;
  overflow: hidden;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  transition: all 0.3s;
}

.sidebar-menu {
  border-right: none;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.sidebar-toggle {
  margin-right: 16px;
  font-size: 18px;
}

.breadcrumb {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle,
.fullscreen-btn {
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  margin: 0 8px;
  font-size: 14px;
}

.tabs-container {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 24px;
}

.route-tabs {
  margin-bottom: -1px;
}

.route-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.route-tabs :deep(.el-tabs__nav) {
  border: none;
}

.route-tabs :deep(.el-tabs__item) {
  border: 1px solid #d9d9d9;
  border-bottom: none;
  margin-right: 4px;
  background-color: #fafafa;
}

.route-tabs :deep(.el-tabs__item.is-active) {
  background-color: #ffffff;
  border-color: #1890ff;
}

.main-content {
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
  padding: 24px;
}

/* Transitions */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* Dark theme styles */
:deep(.dark) .header {
  background-color: #1f1f1f;
  border-bottom-color: #303030;
  color: #ffffff;
}

:deep(.dark) .main-content {
  background-color: #141414;
}

:deep(.dark) .tabs-container {
  background-color: #1f1f1f;
  border-bottom-color: #303030;
}

:deep(.dark) .route-tabs .el-tabs__item {
  background-color: #262626;
  border-color: #434343;
  color: #ffffff;
}

:deep(.dark) .route-tabs .el-tabs__item.is-active {
  background-color: #1f1f1f;
  border-color: #1890ff;
}

/* Responsive design */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }
  
  .breadcrumb {
    display: none;
  }
  
  .tabs-container {
    padding: 0 16px;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .username {
    display: none;
  }
}
</style>