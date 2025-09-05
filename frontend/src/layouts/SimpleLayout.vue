<template>
  <div class="simple-layout" :class="{ 'dark-theme': isDark }">
    <!-- Simple Header -->
    <header class="simple-header" v-if="showHeader">
      <div class="header-content">
        <div class="logo-section">
          <h1 class="logo">周报系统</h1>
        </div>
        
        <div class="header-actions">
          <!-- Theme Toggle -->
          <el-tooltip content="切换主题" placement="bottom">
            <el-button 
              type="text" 
              @click="toggleTheme"
              class="theme-toggle"
              :class="{ 'is-dark': isDark }"
            >
              <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- Language Toggle (if needed) -->
          <el-dropdown trigger="click" class="language-dropdown" v-if="showLanguage">
            <el-button type="text">
              <el-icon><Globe /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>中文</el-dropdown-item>
                <el-dropdown-item>English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="simple-main">
      <div class="content-container" :class="contentClass">
        <transition name="fade-slide" mode="out-in">
          <router-view />
        </transition>
      </div>
    </main>

    <!-- Simple Footer -->
    <footer class="simple-footer" v-if="showFooter">
      <div class="footer-content">
        <p>&copy; {{ currentYear }} 周报系统. All rights reserved.</p>
        <div class="footer-links">
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">服务条款</a>
          <a href="#" class="footer-link">帮助中心</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useLayoutStore } from '@/stores/layout'
import {
  Sunny,
  Moon,
  Globe
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  showHeader: {
    type: Boolean,
    default: true
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  showLanguage: {
    type: Boolean,
    default: false
  },
  contentClass: {
    type: String,
    default: ''
  },
  centered: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const layoutStore = useLayoutStore()

// Computed properties
const isDark = computed(() => layoutStore.isDark)
const currentYear = computed(() => new Date().getFullYear())

// Methods
const toggleTheme = () => {
  layoutStore.toggleTheme()
}

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
.simple-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  transition: all 0.3s ease;
}

.simple-layout.dark-theme {
  background-color: #141414;
  color: #ffffff;
}

/* Header Styles */
.simple-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.dark-theme .simple-header {
  background-color: #1f1f1f;
  border-bottom-color: #303030;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section .logo {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.dark-theme .logo-section .logo {
  color: #69c0ff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle {
  font-size: 18px;
  color: #666;
}

.theme-toggle.is-dark {
  color: #ffd666;
}

.theme-toggle:hover {
  color: #1890ff;
}

/* Main Content Styles */
.simple-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.content-container {
  width: 100%;
  max-width: 400px;
}

.content-container.wide {
  max-width: 800px;
}

.content-container.full {
  max-width: 1200px;
}

/* Footer Styles */
.simple-footer {
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  padding: 24px 0;
  margin-top: auto;
}

.dark-theme .simple-footer {
  background-color: #1f1f1f;
  border-top-color: #303030;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.dark-theme .footer-content {
  color: #999;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-link {
  color: #666;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #1890ff;
}

.dark-theme .footer-link {
  color: #999;
}

.dark-theme .footer-link:hover {
  color: #69c0ff;
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive Design */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    height: 56px;
  }
  
  .logo-section .logo {
    font-size: 20px;
  }
  
  .simple-main {
    padding: 24px 16px;
  }
  
  .footer-content {
    padding: 0 16px;
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .footer-links {
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .content-container {
    max-width: 100%;
  }
  
  .header-actions {
    gap: 8px;
  }
  
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}

/* Print Styles */
@media print {
  .simple-header,
  .simple-footer {
    display: none;
  }
  
  .simple-main {
    padding: 0;
  }
  
  .content-container {
    max-width: none;
  }
}
</style>