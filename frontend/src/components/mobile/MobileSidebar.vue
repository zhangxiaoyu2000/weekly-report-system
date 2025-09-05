<template>
  <div class="mobile-sidebar-container">
    <!-- Backdrop -->
    <transition name="backdrop">
      <div
        v-if="visible"
        class="sidebar-backdrop"
        @click="handleClose"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
      ></div>
    </transition>

    <!-- Sidebar -->
    <transition name="sidebar" @enter="onEnter" @leave="onLeave">
      <aside
        v-if="visible"
        class="mobile-sidebar"
        :class="sidebarClasses"
        :style="sidebarStyles"
        ref="sidebarRef"
        @touchstart="handleSidebarTouchStart"
        @touchmove="handleSidebarTouchMove"
        @touchend="handleSidebarTouchEnd"
      >
        <!-- Sidebar Header -->
        <div class="sidebar-header" v-if="$slots.header || showDefaultHeader">
          <slot name="header">
            <div class="default-header">
              <div class="header-brand">
                <img v-if="logo" :src="logo" :alt="appName" class="brand-logo">
                <span class="brand-text">{{ appName }}</span>
              </div>
              <button class="close-btn" @click="handleClose" :aria-label="closeLabel">
                <el-icon><Close /></el-icon>
              </button>
            </div>
          </slot>
        </div>

        <!-- User Profile Section -->
        <div class="sidebar-profile" v-if="showProfile && userInfo">
          <div class="profile-avatar">
            <el-avatar :size="60" :src="userInfo.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="profile-info">
            <div class="profile-name">{{ userInfo.name }}</div>
            <div class="profile-role">{{ userInfo.role }}</div>
          </div>
          <div class="profile-actions" v-if="$slots['profile-actions']">
            <slot name="profile-actions"></slot>
          </div>
        </div>

        <!-- Sidebar Content -->
        <div class="sidebar-content">
          <!-- Navigation Menu -->
          <nav class="sidebar-nav" v-if="menuItems && menuItems.length">
            <div
              v-for="(item, index) in menuItems"
              :key="item.key || index"
              class="nav-section"
            >
              <!-- Section Title -->
              <div
                v-if="item.title && item.children"
                class="nav-section-title"
              >
                {{ item.title }}
              </div>

              <!-- Menu Items -->
              <div class="nav-items">
                <template v-if="item.children">
                  <div
                    v-for="child in item.children"
                    :key="child.key"
                    class="nav-item"
                    :class="{
                      'is-active': isActive(child),
                      'is-disabled': child.disabled
                    }"
                    @click="handleItemClick(child)"
                  >
                    <div class="nav-item-content">
                      <div class="nav-item-icon" v-if="child.icon">
                        <el-icon>
                          <component :is="child.icon" />
                        </el-icon>
                      </div>
                      <div class="nav-item-text">
                        <div class="nav-item-title">{{ child.title }}</div>
                        <div class="nav-item-subtitle" v-if="child.subtitle">
                          {{ child.subtitle }}
                        </div>
                      </div>
                      <div class="nav-item-extra">
                        <el-badge
                          v-if="child.badge"
                          :value="child.badge"
                          :type="child.badgeType || 'danger'"
                        />
                        <el-icon v-if="child.arrow" class="nav-arrow">
                          <ArrowRight />
                        </el-icon>
                      </div>
                    </div>
                  </div>
                </template>
                <div
                  v-else
                  class="nav-item"
                  :class="{
                    'is-active': isActive(item),
                    'is-disabled': item.disabled
                  }"
                  @click="handleItemClick(item)"
                >
                  <div class="nav-item-content">
                    <div class="nav-item-icon" v-if="item.icon">
                      <el-icon>
                        <component :is="item.icon" />
                      </el-icon>
                    </div>
                    <div class="nav-item-text">
                      <div class="nav-item-title">{{ item.title }}</div>
                      <div class="nav-item-subtitle" v-if="item.subtitle">
                        {{ item.subtitle }}
                      </div>
                    </div>
                    <div class="nav-item-extra">
                      <el-badge
                        v-if="item.badge"
                        :value="item.badge"
                        :type="item.badgeType || 'danger'"
                      />
                      <el-icon v-if="item.arrow" class="nav-arrow">
                        <ArrowRight />
                      </el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </nav>

          <!-- Custom Content Slot -->
          <div class="sidebar-custom" v-if="$slots.default">
            <slot></slot>
          </div>
        </div>

        <!-- Sidebar Footer -->
        <div class="sidebar-footer" v-if="$slots.footer || showDefaultFooter">
          <slot name="footer">
            <div class="default-footer">
              <div class="footer-actions">
                <el-button @click="handleSettings" type="text" class="footer-btn">
                  <el-icon><Setting /></el-icon>
                  <span>设置</span>
                </el-button>
                <el-button @click="handleLogout" type="text" class="footer-btn logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出</span>
                </el-button>
              </div>
            </div>
          </slot>
        </div>

        <!-- Drag Handle -->
        <div
          v-if="resizable"
          class="resize-handle"
          @touchstart="handleResizeStart"
          @mousedown="handleResizeStart"
        ></div>
      </aside>
    </transition>
  </div>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Close,
  User,
  ArrowRight,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  position: {
    type: String,
    default: 'left', // left, right
    validator: (value) => ['left', 'right'].includes(value)
  },
  width: {
    type: [String, Number],
    default: 280
  },
  maxWidth: {
    type: [String, Number],
    default: '80vw'
  },
  appName: {
    type: String,
    default: '应用名称'
  },
  logo: {
    type: String,
    default: ''
  },
  menuItems: {
    type: Array,
    default: () => []
  },
  userInfo: {
    type: Object,
    default: null
  },
  showProfile: {
    type: Boolean,
    default: true
  },
  showDefaultHeader: {
    type: Boolean,
    default: true
  },
  showDefaultFooter: {
    type: Boolean,
    default: true
  },
  closeOnItemClick: {
    type: Boolean,
    default: true
  },
  swipeToClose: {
    type: Boolean,
    default: true
  },
  resizable: {
    type: Boolean,
    default: false
  },
  theme: {
    type: String,
    default: 'light', // light, dark
    validator: (value) => ['light', 'dark'].includes(value)
  },
  closeLabel: {
    type: String,
    default: '关闭'
  }
})

const emit = defineEmits([
  'update:visible',
  'close',
  'item-click',
  'settings',
  'logout'
])

const route = useRoute()
const router = useRouter()
const sidebarRef = ref(null)

// Touch/swipe handling
const touchState = ref({
  startX: 0,
  startY: 0,
  currentX: 0,
  currentY: 0,
  isDragging: false,
  isVertical: false
})

const resizeState = ref({
  isResizing: false,
  startX: 0,
  startWidth: 0
})

// Computed
const sidebarClasses = computed(() => ({
  [`position-${props.position}`]: true,
  [`theme-${props.theme}`]: true,
  'is-resizing': resizeState.value.isResizing
}))

const sidebarStyles = computed(() => {
  const width = typeof props.width === 'number' ? `${props.width}px` : props.width
  const maxWidth = typeof props.maxWidth === 'number' ? `${props.maxWidth}px` : props.maxWidth
  
  return {
    width,
    maxWidth,
    [props.position]: '0'
  }
})

const isActive = (item) => {
  if (!item.path) return false
  
  if (item.exact) {
    return route.path === item.path
  }
  
  return route.path.startsWith(item.path)
}

// Methods
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const handleItemClick = (item) => {
  if (item.disabled) return

  emit('item-click', item)

  if (item.handler) {
    item.handler()
  } else if (item.path) {
    router.push(item.path)
  }

  if (props.closeOnItemClick) {
    handleClose()
  }
}

const handleSettings = () => {
  emit('settings')
}

const handleLogout = () => {
  emit('logout')
}

// Touch handling for swipe to close
const handleTouchStart = (e) => {
  if (!props.swipeToClose) return
  
  const touch = e.touches[0]
  touchState.value = {
    startX: touch.clientX,
    startY: touch.clientY,
    currentX: touch.clientX,
    currentY: touch.clientY,
    isDragging: true,
    isVertical: false
  }
}

const handleTouchMove = (e) => {
  if (!props.swipeToClose || !touchState.value.isDragging) return
  
  const touch = e.touches[0]
  touchState.value.currentX = touch.clientX
  touchState.value.currentY = touch.clientY
  
  const deltaX = touch.clientX - touchState.value.startX
  const deltaY = touch.clientY - touchState.value.startY
  
  // Determine if this is a vertical scroll
  if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > 10) {
    touchState.value.isVertical = true
    return
  }
  
  // Prevent scrolling during horizontal swipe
  if (!touchState.value.isVertical && Math.abs(deltaX) > 10) {
    e.preventDefault()
  }
}

const handleTouchEnd = (e) => {
  if (!props.swipeToClose || !touchState.value.isDragging || touchState.value.isVertical) {
    touchState.value.isDragging = false
    return
  }
  
  const deltaX = touchState.value.currentX - touchState.value.startX
  const threshold = 50
  
  // Check swipe direction based on sidebar position
  if (props.position === 'left' && deltaX < -threshold) {
    handleClose()
  } else if (props.position === 'right' && deltaX > threshold) {
    handleClose()
  }
  
  touchState.value.isDragging = false
}

// Prevent backdrop touch events from propagating to sidebar
const handleSidebarTouchStart = (e) => {
  e.stopPropagation()
}

const handleSidebarTouchMove = (e) => {
  e.stopPropagation()
}

const handleSidebarTouchEnd = (e) => {
  e.stopPropagation()
}

// Resize handling
const handleResizeStart = (e) => {
  if (!props.resizable) return
  
  e.preventDefault()
  
  const startX = e.type === 'touchstart' ? e.touches[0].clientX : e.clientX
  
  resizeState.value = {
    isResizing: true,
    startX,
    startWidth: sidebarRef.value?.offsetWidth || props.width
  }
  
  const handleMouseMove = (e) => {
    if (!resizeState.value.isResizing) return
    
    const currentX = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX
    const delta = props.position === 'left' ? 
      currentX - resizeState.value.startX : 
      resizeState.value.startX - currentX
    
    const newWidth = Math.max(200, Math.min(400, resizeState.value.startWidth + delta))
    
    if (sidebarRef.value) {
      sidebarRef.value.style.width = `${newWidth}px`
    }
  }
  
  const handleMouseUp = () => {
    resizeState.value.isResizing = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
    document.removeEventListener('touchmove', handleMouseMove)
    document.removeEventListener('touchend', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
  document.addEventListener('touchmove', handleMouseMove, { passive: false })
  document.addEventListener('touchend', handleMouseUp)
}

// Animation hooks
const onEnter = (el) => {
  nextTick(() => {
    el.classList.add('entering')
  })
}

const onLeave = (el) => {
  el.classList.add('leaving')
}

// Watch for visibility changes
watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.body.classList.add('mobile-sidebar-open')
  } else {
    document.body.classList.remove('mobile-sidebar-open')
  }
})
</script>

<style scoped>
.mobile-sidebar-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  pointer-events: none;
}

.sidebar-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  pointer-events: all;
}

.mobile-sidebar {
  position: absolute;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  pointer-events: all;
  overflow: hidden;
}

.mobile-sidebar.position-left {
  left: 0;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.1);
}

.mobile-sidebar.position-right {
  right: 0;
  box-shadow: -2px 0 20px rgba(0, 0, 0, 0.1);
}

/* Header */
.sidebar-header {
  flex-shrink: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #f8f9fa;
}

.default-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.brand-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  padding: 8px;
  cursor: pointer;
  color: #666;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: #f0f0f0;
  color: #333;
}

/* Profile Section */
.sidebar-profile {
  flex-shrink: 0;
  padding: 24px 20px;
  border-bottom: 1px solid #e8e8e8;
  text-align: center;
  background: linear-gradient(135deg, #1890ff, #69c0ff);
  color: white;
}

.profile-avatar {
  margin-bottom: 12px;
}

.profile-info {
  margin-bottom: 12px;
}

.profile-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.profile-role {
  font-size: 14px;
  opacity: 0.8;
}

/* Content */
.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}

.nav-section {
  margin-bottom: 24px;
}

.nav-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 0 20px 8px;
}

.nav-item {
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.nav-item:hover {
  background-color: #f5f5f5;
}

.nav-item.is-active {
  background-color: #e6f7ff;
  border-right: 3px solid #1890ff;
}

.nav-item.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-item-content {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  gap: 12px;
}

.nav-item-icon {
  flex-shrink: 0;
  font-size: 18px;
  color: #666;
  width: 24px;
  display: flex;
  justify-content: center;
}

.nav-item.is-active .nav-item-icon {
  color: #1890ff;
}

.nav-item-text {
  flex: 1;
  min-width: 0;
}

.nav-item-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.nav-item.is-active .nav-item-title {
  color: #1890ff;
}

.nav-item-subtitle {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.nav-item-extra {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-arrow {
  font-size: 14px;
  color: #ccc;
  transition: transform 0.3s;
}

.nav-item:hover .nav-arrow {
  transform: translateX(2px);
  color: #999;
}

/* Footer */
.sidebar-footer {
  flex-shrink: 0;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background-color: #f8f9fa;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.footer-btn {
  flex: 1;
  justify-content: flex-start;
  gap: 8px;
  height: 40px;
  font-size: 14px;
}

.footer-btn.logout {
  color: #ff4d4f;
}

.footer-btn.logout:hover {
  background-color: #fff2f0;
  border-color: #ffccc7;
}

/* Resize Handle */
.resize-handle {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;
  background-color: transparent;
  transition: background-color 0.3s;
}

.position-left .resize-handle {
  right: -2px;
}

.position-right .resize-handle {
  left: -2px;
}

.resize-handle:hover,
.is-resizing .resize-handle {
  background-color: #1890ff;
}

/* Dark Theme */
.theme-dark {
  background-color: #1f1f1f;
  color: #ffffff;
}

.theme-dark .sidebar-header {
  background-color: #262626;
  border-bottom-color: #434343;
}

.theme-dark .sidebar-footer {
  background-color: #262626;
  border-top-color: #434343;
}

.theme-dark .brand-text {
  color: #ffffff;
}

.theme-dark .nav-item-title {
  color: #ffffff;
}

.theme-dark .nav-item-icon {
  color: #cccccc;
}

.theme-dark .nav-item:hover {
  background-color: #303030;
}

.theme-dark .nav-item.is-active {
  background-color: #1f4e79;
  border-right-color: #69c0ff;
}

.theme-dark .nav-item.is-active .nav-item-title,
.theme-dark .nav-item.is-active .nav-item-icon {
  color: #69c0ff;
}

.theme-dark .close-btn {
  color: #cccccc;
}

.theme-dark .close-btn:hover {
  background-color: #303030;
  color: #ffffff;
}

/* Transitions */
.backdrop-enter-active,
.backdrop-leave-active {
  transition: opacity 0.3s ease;
}

.backdrop-enter-from,
.backdrop-leave-to {
  opacity: 0;
}

.sidebar-enter-active,
.sidebar-leave-active {
  transition: transform 0.3s ease;
}

.sidebar-enter-from.position-left,
.sidebar-leave-to.position-left {
  transform: translateX(-100%);
}

.sidebar-enter-from.position-right,
.sidebar-leave-to.position-right {
  transform: translateX(100%);
}

/* Global body class */
:global(body.mobile-sidebar-open) {
  overflow: hidden;
}

/* Safe area support */
@supports (padding-top: constant(safe-area-inset-top)) {
  .mobile-sidebar {
    padding-top: constant(safe-area-inset-top);
  }
}

@supports (padding-top: env(safe-area-inset-top)) {
  .mobile-sidebar {
    padding-top: env(safe-area-inset-top);
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .nav-item.is-active {
    outline: 2px solid #1890ff;
    outline-offset: -2px;
  }
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .mobile-sidebar,
  .sidebar-backdrop,
  .nav-item,
  .close-btn,
  .nav-arrow {
    transition: none;
  }
}
</style>