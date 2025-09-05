<template>
  <nav class="responsive-nav" :class="navClasses">
    <!-- Desktop Navigation -->
    <div class="desktop-nav desktop-only" v-if="!isMobile">
      <div class="nav-brand">
        <router-link to="/" class="brand-link">
          <img v-if="logo" :src="logo" :alt="appName" class="brand-logo">
          <span class="brand-text">{{ appName }}</span>
        </router-link>
      </div>

      <div class="nav-menu">
        <div class="nav-items">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="nav-item"
            :class="{ 'is-active': isActive(item) }"
          >
            <router-link
              v-if="!item.children"
              :to="item.path"
              class="nav-link"
              :class="{ 'is-disabled': item.disabled }"
              @click="handleItemClick(item)"
            >
              <el-icon v-if="item.icon" class="nav-icon">
                <component :is="item.icon" />
              </el-icon>
              <span class="nav-text">{{ item.title }}</span>
              <el-badge
                v-if="item.badge"
                :value="item.badge"
                class="nav-badge"
              />
            </router-link>

            <!-- Dropdown menu -->
            <el-dropdown
              v-else
              trigger="hover"
              class="nav-dropdown"
              @command="handleCommand"
            >
              <div class="nav-link dropdown-trigger">
                <el-icon v-if="item.icon" class="nav-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span class="nav-text">{{ item.title }}</span>
                <el-icon class="dropdown-arrow">
                  <ArrowDown />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="child in item.children"
                    :key="child.key"
                    :command="child"
                    :disabled="child.disabled"
                  >
                    <el-icon v-if="child.icon" class="dropdown-icon">
                      <component :is="child.icon" />
                    </el-icon>
                    {{ child.title }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div class="nav-actions">
          <slot name="actions"></slot>
        </div>
      </div>
    </div>

    <!-- Mobile Navigation -->
    <div class="mobile-nav mobile-only" v-if="isMobile">
      <!-- Mobile Header -->
      <div class="mobile-header">
        <button
          class="mobile-menu-btn"
          @click="toggleMobileMenu"
          :class="{ 'is-active': mobileMenuVisible }"
        >
          <span class="hamburger-line"></span>
          <span class="hamburger-line"></span>
          <span class="hamburger-line"></span>
        </button>

        <div class="mobile-brand">
          <router-link to="/" class="brand-link" @click="closeMobileMenu">
            <img v-if="logo" :src="logo" :alt="appName" class="brand-logo">
            <span class="brand-text">{{ appName }}</span>
          </router-link>
        </div>

        <div class="mobile-actions">
          <slot name="mobile-actions"></slot>
        </div>
      </div>

      <!-- Mobile Menu Overlay -->
      <transition name="mobile-menu">
        <div
          v-if="mobileMenuVisible"
          class="mobile-menu-overlay"
          @click="closeMobileMenu"
        ></div>
      </transition>

      <!-- Mobile Menu -->
      <transition name="mobile-menu-slide">
        <div v-if="mobileMenuVisible" class="mobile-menu">
          <div class="mobile-menu-content">
            <div class="mobile-menu-header">
              <div class="mobile-brand-large">
                <img v-if="logo" :src="logo" :alt="appName" class="brand-logo">
                <span class="brand-text">{{ appName }}</span>
              </div>
              <button class="mobile-menu-close" @click="closeMobileMenu">
                <el-icon><Close /></el-icon>
              </button>
            </div>

            <div class="mobile-menu-body">
              <div
                v-for="item in menuItems"
                :key="item.key"
                class="mobile-nav-item"
                :class="{ 'is-active': isActive(item) }"
              >
                <div v-if="item.children" class="mobile-nav-group">
                  <div class="mobile-nav-group-title">
                    <el-icon v-if="item.icon" class="nav-icon">
                      <component :is="item.icon" />
                    </el-icon>
                    <span>{{ item.title }}</span>
                  </div>
                  <div class="mobile-nav-group-items">
                    <router-link
                      v-for="child in item.children"
                      :key="child.key"
                      :to="child.path"
                      class="mobile-nav-link"
                      :class="{ 'is-disabled': child.disabled }"
                      @click="handleMobileItemClick(child)"
                    >
                      <el-icon v-if="child.icon" class="nav-icon">
                        <component :is="child.icon" />
                      </el-icon>
                      <span class="nav-text">{{ child.title }}</span>
                      <el-badge
                        v-if="child.badge"
                        :value="child.badge"
                        class="nav-badge"
                      />
                    </router-link>
                  </div>
                </div>

                <router-link
                  v-else
                  :to="item.path"
                  class="mobile-nav-link"
                  :class="{ 'is-disabled': item.disabled }"
                  @click="handleMobileItemClick(item)"
                >
                  <el-icon v-if="item.icon" class="nav-icon">
                    <component :is="item.icon" />
                  </el-icon>
                  <span class="nav-text">{{ item.title }}</span>
                  <el-badge
                    v-if="item.badge"
                    :value="item.badge"
                    class="nav-badge"
                  />
                </router-link>
              </div>
            </div>

            <div class="mobile-menu-footer">
              <slot name="mobile-footer"></slot>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- Tablet Navigation (if different from desktop) -->
    <div class="tablet-nav tablet-only" v-if="isTablet && tabletMode !== 'mobile'">
      <!-- Similar to desktop but with adjustments for tablet -->
      <div class="nav-brand">
        <router-link to="/" class="brand-link">
          <img v-if="logo" :src="logo" :alt="appName" class="brand-logo">
          <span class="brand-text">{{ appName }}</span>
        </router-link>
      </div>

      <div class="nav-menu compact">
        <div class="nav-items">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="nav-item"
            :class="{ 'is-active': isActive(item) }"
          >
            <router-link
              v-if="!item.children"
              :to="item.path"
              class="nav-link"
              @click="handleItemClick(item)"
            >
              <el-icon v-if="item.icon" class="nav-icon">
                <component :is="item.icon" />
              </el-icon>
              <span class="nav-text" v-if="!compactTablet">{{ item.title }}</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResponsive } from '@/composables/useResponsive'
import { ArrowDown, Close } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  menuItems: {
    type: Array,
    required: true
  },
  appName: {
    type: String,
    default: '应用名称'
  },
  logo: {
    type: String,
    default: ''
  },
  variant: {
    type: String,
    default: 'primary', // primary, secondary, transparent
    validator: (value) => ['primary', 'secondary', 'transparent'].includes(value)
  },
  fixed: {
    type: Boolean,
    default: false
  },
  shadow: {
    type: Boolean,
    default: true
  },
  tabletMode: {
    type: String,
    default: 'desktop', // desktop, mobile, compact
    validator: (value) => ['desktop', 'mobile', 'compact'].includes(value)
  },
  compactTablet: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['item-click', 'menu-toggle'])

// Composition
const route = useRoute()
const router = useRouter()
const responsive = useResponsive()

// Reactive state
const mobileMenuVisible = ref(false)

// Computed
const { isMobile, isTablet } = responsive

const navClasses = computed(() => ({
  [`nav-${props.variant}`]: true,
  'nav-fixed': props.fixed,
  'nav-shadow': props.shadow,
  'mobile-menu-open': mobileMenuVisible.value
}))

// Methods
const isActive = (item) => {
  if (!item.path) return false
  
  if (item.exact) {
    return route.path === item.path
  }
  
  return route.path.startsWith(item.path)
}

const handleItemClick = (item) => {
  if (item.disabled) return
  
  emit('item-click', item)
  
  if (item.handler) {
    item.handler()
  }
}

const handleMobileItemClick = (item) => {
  handleItemClick(item)
  closeMobileMenu()
}

const handleCommand = (command) => {
  if (command.path) {
    router.push(command.path)
  }
  handleItemClick(command)
}

const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
  emit('menu-toggle', mobileMenuVisible.value)
  
  // Prevent body scroll when menu is open
  document.body.classList.toggle('mobile-menu-open', mobileMenuVisible.value)
}

const closeMobileMenu = () => {
  mobileMenuVisible.value = false
  emit('menu-toggle', false)
  document.body.classList.remove('mobile-menu-open')
}

// Handle clicks outside mobile menu
const handleClickOutside = (event) => {
  if (mobileMenuVisible.value && !event.target.closest('.mobile-menu')) {
    closeMobileMenu()
  }
}

// Handle escape key
const handleEscape = (event) => {
  if (event.key === 'Escape' && mobileMenuVisible.value) {
    closeMobileMenu()
  }
}

// Lifecycle
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleEscape)
  document.body.classList.remove('mobile-menu-open')
})
</script>

<style scoped>
.responsive-nav {
  position: relative;
  z-index: 1000;
}

.nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

.nav-shadow {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-primary {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
}

.nav-secondary {
  background-color: #f5f5f5;
  border-bottom: 1px solid #d9d9d9;
}

.nav-transparent {
  background-color: transparent;
  border-bottom: 1px solid transparent;
}

/* Desktop Navigation */
.desktop-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
}

.nav-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #333;
}

.brand-logo {
  height: 32px;
  width: auto;
  margin-right: 12px;
}

.brand-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.nav-menu {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: space-between;
  margin-left: 48px;
}

.nav-items {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  position: relative;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  text-decoration: none;
  color: #666;
  border-radius: 4px;
  transition: all 0.3s;
  font-size: 14px;
}

.nav-link:hover {
  color: #1890ff;
  background-color: #f0f8ff;
}

.nav-item.is-active .nav-link {
  color: #1890ff;
  background-color: #e6f7ff;
}

.nav-link.is-disabled {
  color: #ccc;
  cursor: not-allowed;
}

.nav-icon {
  font-size: 16px;
}

.nav-text {
  white-space: nowrap;
}

.nav-badge {
  margin-left: 4px;
}

.dropdown-trigger {
  cursor: pointer;
}

.dropdown-arrow {
  font-size: 12px;
  transition: transform 0.3s;
}

.nav-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* Mobile Navigation */
.mobile-nav {
  position: relative;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  background-color: inherit;
}

.mobile-menu-btn {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.hamburger-line {
  width: 20px;
  height: 2px;
  background-color: #333;
  transition: all 0.3s;
  margin: 2px 0;
}

.mobile-menu-btn.is-active .hamburger-line:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.mobile-menu-btn.is-active .hamburger-line:nth-child(2) {
  opacity: 0;
}

.mobile-menu-btn.is-active .hamburger-line:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -6px);
}

.mobile-brand {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.mobile-brand .brand-link {
  color: #333;
}

.mobile-brand .brand-logo {
  height: 28px;
  margin-right: 8px;
}

.mobile-brand .brand-text {
  font-size: 16px;
}

.mobile-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1001;
}

.mobile-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 280px;
  height: 100vh;
  background-color: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 1002;
  overflow-y: auto;
}

.mobile-menu-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.mobile-brand-large .brand-logo {
  height: 32px;
  margin-right: 12px;
}

.mobile-brand-large .brand-text {
  font-size: 18px;
  font-weight: 600;
}

.mobile-menu-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  color: #666;
}

.mobile-menu-body {
  flex: 1;
  padding: 16px 0;
}

.mobile-nav-item {
  margin-bottom: 8px;
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s;
}

.mobile-nav-link:hover {
  background-color: #f5f5f5;
  color: #1890ff;
}

.mobile-nav-item.is-active .mobile-nav-link {
  background-color: #e6f7ff;
  color: #1890ff;
}

.mobile-nav-link.is-disabled {
  color: #ccc;
  cursor: not-allowed;
}

.mobile-nav-group-title {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  font-weight: 500;
  color: #666;
  background-color: #fafafa;
}

.mobile-nav-group-items {
  padding-left: 20px;
}

.mobile-menu-footer {
  padding: 16px;
  border-top: 1px solid #e8e8e8;
}

/* Tablet Navigation */
.tablet-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
}

.tablet-nav .nav-menu.compact .nav-items {
  gap: 16px;
}

.tablet-nav .nav-link {
  padding: 6px 12px;
  font-size: 13px;
}

/* Transitions */
.mobile-menu-enter-active, .mobile-menu-leave-active {
  transition: opacity 0.3s;
}

.mobile-menu-enter-from, .mobile-menu-leave-to {
  opacity: 0;
}

.mobile-menu-slide-enter-active, .mobile-menu-slide-leave-active {
  transition: transform 0.3s;
}

.mobile-menu-slide-enter-from, .mobile-menu-slide-leave-to {
  transform: translateX(-100%);
}

/* Dark theme support */
:deep(.dark) .nav-primary {
  background-color: #1f1f1f;
  border-bottom-color: #303030;
  color: #ffffff;
}

:deep(.dark) .brand-link,
:deep(.dark) .brand-text {
  color: #ffffff;
}

:deep(.dark) .nav-link {
  color: #cccccc;
}

:deep(.dark) .nav-link:hover {
  color: #69c0ff;
  background-color: #303030;
}

:deep(.dark) .nav-item.is-active .nav-link {
  color: #69c0ff;
  background-color: #1f4e79;
}

:deep(.dark) .mobile-menu {
  background-color: #1f1f1f;
}

:deep(.dark) .mobile-nav-link {
  color: #cccccc;
}

:deep(.dark) .mobile-nav-link:hover {
  background-color: #303030;
  color: #69c0ff;
}

:deep(.dark) .hamburger-line {
  background-color: #ffffff;
}

/* Global body class for mobile menu */
:global(body.mobile-menu-open) {
  overflow: hidden;
}

/* Safe area support */
@supports (padding-top: constant(safe-area-inset-top)) {
  .mobile-header {
    padding-top: constant(safe-area-inset-top);
  }
}

@supports (padding-top: env(safe-area-inset-top)) {
  .mobile-header {
    padding-top: env(safe-area-inset-top);
  }
}
</style>