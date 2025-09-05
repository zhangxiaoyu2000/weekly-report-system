<template>
  <div class="mobile-layout" :class="{ 'dark-theme': isDark }">
    <!-- Mobile Header -->
    <header class="mobile-header">
      <div class="header-content">
        <!-- Left Actions -->
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleDrawer"
            class="menu-toggle"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
          
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        
        <!-- Right Actions -->
        <div class="header-right">
          <el-button
            type="text"
            @click="toggleTheme"
            class="theme-toggle"
          >
            <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
          </el-button>
          
          <el-avatar 
            :size="32" 
            :src="userAvatar"
            @click="showUserMenu = true"
            class="user-avatar"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
        </div>
      </div>
    </header>

    <!-- Mobile Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :size="280"
      direction="ltr"
      class="mobile-drawer"
    >
      <template #header>
        <div class="drawer-header">
          <div class="user-profile">
            <el-avatar :size="60" :src="userAvatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-info">
              <h3>{{ userName }}</h3>
              <p>{{ userRole }}</p>
            </div>
          </div>
        </div>
      </template>
      
      <div class="drawer-menu">
        <el-menu
          :default-active="activeMenu"
          class="drawer-menu-list"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Dashboard /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><Document /></el-icon>
            <span>周报管理</span>
          </el-menu-item>
          <el-menu-item index="/reports/create">
            <el-icon><EditPen /></el-icon>
            <span>创建周报</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
        </el-menu>
        
        <div class="drawer-actions">
          <el-button @click="handleLogout" class="logout-btn" type="danger">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- Main Content -->
    <main class="mobile-main">
      <div class="content-wrapper">
        <transition name="slide-fade" mode="out-in">
          <router-view />
        </transition>
      </div>
    </main>

    <!-- Bottom Navigation -->
    <nav class="bottom-nav">
      <div class="nav-items">
        <div
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
          @click="navigateTo(item.path)"
        >
          <el-icon class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </div>
    </nav>

    <!-- User Menu Popup -->
    <el-dialog
      v-model="showUserMenu"
      title="用户菜单"
      width="90%"
      :show-close="false"
      class="user-menu-dialog"
    >
      <div class="user-menu-content">
        <div class="user-menu-header">
          <el-avatar :size="80" :src="userAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-details">
            <h3>{{ userName }}</h3>
            <p>{{ userRole }}</p>
          </div>
        </div>
        
        <div class="user-menu-actions">
          <el-button @click="goToProfile" class="menu-action-btn">
            <el-icon><User /></el-icon>
            个人资料
          </el-button>
          <el-button @click="goToSettings" class="menu-action-btn">
            <el-icon><Setting /></el-icon>
            设置
          </el-button>
          <el-button @click="handleLogout" type="danger" class="menu-action-btn">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showUserMenu = false" class="cancel-btn">取消</el-button>
      </template>
    </el-dialog>

    <!-- Loading Overlay -->
    <div v-if="loading" class="loading-overlay">
      <el-loading-spinner />
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useLayoutStore } from '@/stores/layout'
import {
  Monitor as Dashboard,
  Document,
  Edit as EditPen,
  User,
  Menu,
  Sunny,
  Moon,
  SwitchButton,
  Setting,
  HomeFilled,
  Plus
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const layoutStore = useLayoutStore()

// Reactive data
const drawerVisible = ref(false)
const showUserMenu = ref(false)
const loading = ref(false)

// Navigation items for bottom nav
const navItems = [
  { path: '/dashboard', label: '首页', icon: Dashboard },
  { path: '/reports', label: '周报', icon: Document },
  { path: '/reports/create', label: '创建', icon: Plus },
  { path: '/profile', label: '我的', icon: User }
]

// Computed properties
const activeMenu = computed(() => route.path)
const userName = computed(() => userStore.userName || '用户')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')
const userRole = computed(() => {
  const role = userStore.userRole
  return role === 'admin' ? '管理员' : role === 'manager' ? '主管' : '用户'
})
const isDark = computed(() => layoutStore.isDark)

const pageTitle = computed(() => {
  return route.meta?.title || '周报系统'
})

// Methods
const toggleDrawer = () => {
  drawerVisible.value = !drawerVisible.value
}

const toggleTheme = () => {
  layoutStore.toggleTheme()
}

const handleMenuSelect = () => {
  drawerVisible.value = false
}

const navigateTo = (path) => {
  if (path !== route.path) {
    router.push(path)
  }
}

const goToProfile = () => {
  showUserMenu.value = false
  router.push('/profile')
}

const goToSettings = () => {
  showUserMenu.value = false
  // Handle settings navigation
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  drawerVisible.value = false
  showUserMenu.value = false
}

onMounted(() => {
  // Apply theme class to document
  document.documentElement.classList.toggle('dark', isDark.value)
})

// Watch theme changes
watch(isDark, (newVal) => {
  document.documentElement.classList.toggle('dark', newVal)
})

// Close drawer when route changes
watch(() => route.path, () => {
  drawerVisible.value = false
})
</script>

<style scoped>
.mobile-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  overflow: hidden;
}

.mobile-layout.dark-theme {
  background-color: #141414;
  color: #ffffff;
}

/* Header Styles */
.mobile-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1001;
}

.dark-theme .mobile-header {
  background-color: #1f1f1f;
  border-bottom-color: #303030;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.header-content {
  height: 56px;
  padding: 0 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.dark-theme .page-title {
  color: #ffffff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-toggle,
.theme-toggle {
  font-size: 20px;
}

.user-avatar {
  cursor: pointer;
}

/* Drawer Styles */
.mobile-drawer :deep(.el-drawer__header) {
  padding: 0;
  margin-bottom: 0;
}

.drawer-header {
  padding: 24px 20px;
  background: linear-gradient(135deg, #1890ff, #69c0ff);
  color: white;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 500;
}

.user-info p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.drawer-menu {
  padding: 16px 0;
  height: calc(100% - 120px);
  display: flex;
  flex-direction: column;
}

.drawer-menu-list {
  flex: 1;
  border: none;
}

.drawer-menu-list .el-menu-item {
  height: 56px;
  line-height: 56px;
  margin: 0 16px 8px;
  border-radius: 8px;
}

.drawer-actions {
  padding: 16px;
  border-top: 1px solid #e8e8e8;
}

.logout-btn {
  width: 100%;
  height: 44px;
}

/* Main Content */
.mobile-main {
  flex: 1;
  overflow: auto;
  padding-bottom: 60px; /* Account for bottom nav */
}

.content-wrapper {
  padding: 16px;
  min-height: 100%;
}

/* Bottom Navigation */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.dark-theme .bottom-nav {
  background-color: #1f1f1f;
  border-top-color: #303030;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.3);
}

.nav-items {
  height: 100%;
  display: flex;
  align-items: center;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px 0;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.nav-item.active {
  color: #1890ff;
}

.dark-theme .nav-item {
  color: #999;
}

.dark-theme .nav-item.active {
  color: #69c0ff;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.nav-label {
  font-size: 10px;
  line-height: 1;
}

/* User Menu Dialog */
.user-menu-dialog :deep(.el-dialog) {
  margin-top: 15vh;
  border-radius: 12px;
}

.user-menu-content {
  text-align: center;
}

.user-menu-header {
  padding: 24px 0;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 24px;
}

.user-details {
  margin-top: 16px;
}

.user-details h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.user-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.user-menu-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.menu-action-btn {
  width: 100%;
  height: 48px;
  justify-content: flex-start;
  gap: 12px;
}

.cancel-btn {
  width: 100%;
  height: 44px;
}

/* Loading Overlay */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.dark-theme .loading-overlay {
  background-color: rgba(20, 20, 20, 0.8);
}

.loading-overlay p {
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

/* Transitions */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* Safe Area Support (for iPhone notch) */
@supports (padding-top: constant(safe-area-inset-top)) {
  .mobile-header {
    padding-top: constant(safe-area-inset-top);
  }
  
  .bottom-nav {
    padding-bottom: constant(safe-area-inset-bottom);
    height: calc(60px + constant(safe-area-inset-bottom));
  }
}

@supports (padding-top: env(safe-area-inset-top)) {
  .mobile-header {
    padding-top: env(safe-area-inset-top);
  }
  
  .bottom-nav {
    padding-bottom: env(safe-area-inset-bottom);
    height: calc(60px + env(safe-area-inset-bottom));
  }
}

/* Landscape orientation adjustments */
@media (orientation: landscape) and (max-height: 500px) {
  .bottom-nav {
    height: 50px;
  }
  
  .mobile-main {
    padding-bottom: 50px;
  }
  
  .nav-label {
    display: none;
  }
  
  .nav-icon {
    margin-bottom: 0;
  }
}
</style>