<template>
  <div class="app-tabs" :class="{ 'is-mobile': isMobile }" v-show="showTabs && tabs.length > 0">
    <div class="tabs-container">
      <!-- Scroll Left Button -->
      <div 
        class="tab-scroll-btn left" 
        :class="{ 'is-disabled': scrollLeft <= 0 }"
        @click="scrollTabsLeft"
        v-if="showScrollButtons"
      >
        <el-icon><ArrowLeft /></el-icon>
      </div>

      <!-- Tabs Scroll Area -->
      <div class="tabs-scroll-container" ref="scrollContainer">
        <div class="tabs-wrapper" :style="{ transform: `translateX(${-scrollLeft}px)` }">
          <div
            v-for="(tab, index) in tabs"
            :key="tab.path"
            class="tab-item"
            :class="{
              'is-active': isTabActive(tab.path),
              'is-fixed': !tab.closable,
              'is-dragging': dragIndex === index
            }"
            @click="handleTabClick(tab)"
            @contextmenu.prevent="handleContextMenu($event, tab, index)"
            @mousedown="handleMouseDown($event, index)"
            draggable="false"
          >
            <!-- Tab Icon -->
            <el-icon v-if="tab.icon && showIcons" class="tab-icon">
              <component :is="tab.icon" />
            </el-icon>

            <!-- Tab Title -->
            <span class="tab-title" :title="tab.title">{{ tab.title }}</span>

            <!-- Close Button -->
            <div 
              v-if="tab.closable" 
              class="tab-close"
              @click.stop="handleTabClose(tab)"
            >
              <el-icon><Close /></el-icon>
            </div>

            <!-- Loading Indicator -->
            <div v-if="tab.loading" class="tab-loading">
              <el-icon class="loading-icon"><Loading /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- Scroll Right Button -->
      <div 
        class="tab-scroll-btn right" 
        :class="{ 'is-disabled': scrollLeft >= maxScrollLeft }"
        @click="scrollTabsRight"
        v-if="showScrollButtons"
      >
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- Tabs Actions -->
      <div class="tabs-actions">
        <!-- Refresh Current Tab -->
        <el-tooltip content="刷新当前页" placement="bottom">
          <el-button type="text" @click="refreshCurrentTab" class="action-btn">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </el-tooltip>

        <!-- Close All Tabs -->
        <el-dropdown trigger="click" @command="handleDropdownAction" class="action-dropdown">
          <el-button type="text" class="action-btn">
            <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="refresh">
                <el-icon><Refresh /></el-icon>
                刷新当前页
              </el-dropdown-item>
              <el-dropdown-item command="closeCurrent" :disabled="!canCloseCurrentTab">
                <el-icon><Close /></el-icon>
                关闭当前页
              </el-dropdown-item>
              <el-dropdown-item command="closeOthers">
                <el-icon><FolderDelete /></el-icon>
                关闭其他页
              </el-dropdown-item>
              <el-dropdown-item command="closeAll">
                <el-icon><Delete /></el-icon>
                关闭所有页
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- Context Menu -->
    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      @click.stop
    >
      <div class="context-menu-item" @click="contextAction('refresh')">
        <el-icon><Refresh /></el-icon>
        刷新页面
      </div>
      <div 
        class="context-menu-item" 
        :class="{ 'is-disabled': !contextMenu.tab?.closable }"
        @click="contextAction('close')"
      >
        <el-icon><Close /></el-icon>
        关闭页面
      </div>
      <div class="context-menu-item" @click="contextAction('closeOthers')">
        <el-icon><FolderDelete /></el-icon>
        关闭其他
      </div>
      <div class="context-menu-item" @click="contextAction('closeLeft')">
        <el-icon><Back /></el-icon>
        关闭左侧
      </div>
      <div class="context-menu-item" @click="contextAction('closeRight')">
        <el-icon><Right /></el-icon>
        关闭右侧
      </div>
      <div class="context-menu-item" @click="contextAction('closeAll')">
        <el-icon><Delete /></el-icon>
        关闭所有
      </div>
    </div>

    <!-- Overlay for context menu -->
    <div
      v-if="contextMenu.visible"
      class="context-menu-overlay"
      @click="hideContextMenu"
    ></div>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'
import { useLayoutStore } from '@/stores/layout'
import {
  Close,
  ArrowLeft,
  ArrowRight,
  ArrowDown,
  Refresh,
  Loading,
  Delete,
  FolderDelete,
  Back,
  Right
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  showIcons: {
    type: Boolean,
    default: true
  }
})

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()
const layoutStore = useLayoutStore()

// Refs
const scrollContainer = ref(null)
const scrollLeft = ref(0)
const maxScrollLeft = ref(0)
const dragIndex = ref(-1)

// Context menu
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  tab: null,
  index: -1
})

// Computed
const tabs = computed(() => tabsStore.tabs)
const showTabs = computed(() => layoutStore.showTabs)
const isMobile = computed(() => layoutStore.isMobile)
const canCloseCurrentTab = computed(() => tabsStore.canCloseCurrentTab)
const showScrollButtons = computed(() => maxScrollLeft.value > 0)

// Methods
const isTabActive = (path) => tabsStore.isTabActive(path)

const handleTabClick = (tab) => {
  if (tab.path !== route.path) {
    router.push(tab.path)
  }
}

const handleTabClose = (tab) => {
  const newActivePath = tabsStore.removeTab(tab.path)
  if (newActivePath && newActivePath !== route.path) {
    router.push(newActivePath)
  }
}

const refreshCurrentTab = () => {
  tabsStore.refreshTab(route.path)
  // Force component reload
  window.location.reload()
}

const handleDropdownAction = (command) => {
  switch (command) {
    case 'refresh':
      refreshCurrentTab()
      break
    case 'closeCurrent':
      handleTabClose(tabs.value.find(tab => tab.path === route.path))
      break
    case 'closeOthers':
      tabsStore.removeOtherTabs(route.path)
      break
    case 'closeAll':
      tabsStore.removeAllTabs()
      const remainingTabs = tabsStore.tabs
      if (remainingTabs.length > 0) {
        router.push(remainingTabs[0].path)
      } else {
        router.push('/dashboard')
      }
      break
  }
}

// Context menu
const handleContextMenu = (event, tab, index) => {
  event.preventDefault()
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    tab,
    index
  }
}

const hideContextMenu = () => {
  contextMenu.value.visible = false
}

const contextAction = (action) => {
  const { tab } = contextMenu.value
  if (!tab) return

  switch (action) {
    case 'refresh':
      tabsStore.refreshTab(tab.path)
      if (tab.path === route.path) {
        window.location.reload()
      }
      break
    case 'close':
      if (tab.closable) {
        handleTabClose(tab)
      }
      break
    case 'closeOthers':
      tabsStore.removeOtherTabs(tab.path)
      if (tab.path !== route.path) {
        router.push(tab.path)
      }
      break
    case 'closeLeft':
      tabsStore.contextMenuActions.closeLeft(tab.path)
      break
    case 'closeRight':
      tabsStore.contextMenuActions.closeRight(tab.path)
      break
    case 'closeAll':
      tabsStore.removeAllTabs()
      const remainingTabs = tabsStore.tabs
      if (remainingTabs.length > 0) {
        router.push(remainingTabs[0].path)
      } else {
        router.push('/dashboard')
      }
      break
  }
  
  hideContextMenu()
}

// Scrolling
const updateScrollState = () => {
  if (!scrollContainer.value) return
  
  const container = scrollContainer.value
  const wrapper = container.querySelector('.tabs-wrapper')
  if (!wrapper) return
  
  const containerWidth = container.offsetWidth
  const wrapperWidth = wrapper.offsetWidth
  
  maxScrollLeft.value = Math.max(0, wrapperWidth - containerWidth)
  scrollLeft.value = Math.min(scrollLeft.value, maxScrollLeft.value)
}

const scrollTabsLeft = () => {
  scrollLeft.value = Math.max(0, scrollLeft.value - 200)
}

const scrollTabsRight = () => {
  scrollLeft.value = Math.min(maxScrollLeft.value, scrollLeft.value + 200)
}

const scrollToActiveTab = () => {
  nextTick(() => {
    const activeTabElement = scrollContainer.value?.querySelector('.tab-item.is-active')
    if (!activeTabElement) return
    
    const container = scrollContainer.value
    const tabRect = activeTabElement.getBoundingClientRect()
    const containerRect = container.getBoundingClientRect()
    
    if (tabRect.left < containerRect.left) {
      // Tab is to the left of visible area
      scrollLeft.value = Math.max(0, scrollLeft.value - (containerRect.left - tabRect.left) - 20)
    } else if (tabRect.right > containerRect.right) {
      // Tab is to the right of visible area
      scrollLeft.value = Math.min(maxScrollLeft.value, scrollLeft.value + (tabRect.right - containerRect.right) + 20)
    }
  })
}

// Mouse drag (for future tab reordering)
const handleMouseDown = (event, index) => {
  // Placeholder for drag functionality
  dragIndex.value = index
  
  const handleMouseUp = () => {
    dragIndex.value = -1
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mouseup', handleMouseUp)
}

// Lifecycle
onMounted(() => {
  updateScrollState()
  scrollToActiveTab()
  
  // Add global click listener to hide context menu
  document.addEventListener('click', hideContextMenu)
  
  // Add resize listener
  window.addEventListener('resize', updateScrollState)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
  window.removeEventListener('resize', updateScrollState)
})

// Watch for tab changes
watch(tabs, () => {
  nextTick(() => {
    updateScrollState()
    scrollToActiveTab()
  })
}, { deep: true })

watch(() => route.path, () => {
  nextTick(scrollToActiveTab)
})
</script>

<style scoped>
.app-tabs {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  user-select: none;
}

.tabs-container {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
}

.tab-scroll-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
  border-radius: 4px;
}

.tab-scroll-btn:hover:not(.is-disabled) {
  background-color: #f5f5f5;
  color: #1890ff;
}

.tab-scroll-btn.is-disabled {
  color: #ccc;
  cursor: not-allowed;
}

.tabs-scroll-container {
  flex: 1;
  overflow: hidden;
  margin: 0 8px;
}

.tabs-wrapper {
  display: flex;
  transition: transform 0.3s ease;
  gap: 4px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 12px;
  background-color: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
  position: relative;
  min-width: 80px;
}

.tab-item:hover {
  background-color: #e6f7ff;
  border-color: #91d5ff;
}

.tab-item.is-active {
  background-color: #1890ff;
  color: #ffffff;
  border-color: #1890ff;
}

.tab-item.is-fixed {
  background-color: #f0f0f0;
  border-color: #d9d9d9;
}

.tab-item.is-fixed.is-active {
  background-color: #1890ff;
  color: #ffffff;
}

.tab-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.tab-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  max-width: 120px;
}

.tab-close {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
  opacity: 0.6;
  transition: all 0.3s;
  margin-left: 4px;
}

.tab-close:hover {
  background-color: rgba(255, 255, 255, 0.2);
  opacity: 1;
}

.tab-item.is-active .tab-close:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.tab-loading {
  margin-left: 4px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.tabs-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 4px;
  font-size: 14px;
}

/* Context Menu */
.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1999;
}

.context-menu {
  position: fixed;
  background-color: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  z-index: 2000;
  min-width: 120px;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.context-menu-item:hover:not(.is-disabled) {
  background-color: #f5f5f5;
}

.context-menu-item.is-disabled {
  color: #ccc;
  cursor: not-allowed;
}

/* Mobile styles */
.app-tabs.is-mobile .tabs-container {
  padding: 0 8px;
  height: 36px;
}

.app-tabs.is-mobile .tab-item {
  height: 28px;
  padding: 0 8px;
  min-width: 60px;
}

.app-tabs.is-mobile .tab-title {
  max-width: 80px;
  font-size: 12px;
}

.app-tabs.is-mobile .tab-scroll-btn {
  width: 28px;
  height: 28px;
}

.app-tabs.is-mobile .action-btn {
  width: 28px;
  height: 28px;
}

/* Dark theme */
:deep(.dark) .app-tabs {
  background-color: #1f1f1f;
  border-bottom-color: #303030;
}

:deep(.dark) .tab-item {
  background-color: #262626;
  border-color: #434343;
  color: #ffffff;
}

:deep(.dark) .tab-item:hover {
  background-color: #434343;
  border-color: #69c0ff;
}

:deep(.dark) .tab-item.is-active {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.dark) .tab-scroll-btn {
  color: #999;
}

:deep(.dark) .tab-scroll-btn:hover:not(.is-disabled) {
  background-color: #303030;
  color: #69c0ff;
}

:deep(.dark) .context-menu {
  background-color: #1f1f1f;
  border-color: #434343;
  color: #ffffff;
}

:deep(.dark) .context-menu-item:hover:not(.is-disabled) {
  background-color: #303030;
}
</style>