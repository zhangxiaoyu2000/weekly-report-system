import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTabsStore = defineStore('tabs', () => {
  // State
  const tabs = ref([])
  const activeTab = ref('')
  const cachedViews = ref(new Set())
  
  // Maximum number of tabs to keep
  const maxTabs = 10

  // Tabs that should not be closed
  const fixedTabs = ['/dashboard']

  // Getters
  const tabsCount = computed(() => tabs.value.length)
  const hasMultipleTabs = computed(() => tabs.value.length > 1)
  const canCloseCurrentTab = computed(() => {
    const currentTab = tabs.value.find(tab => tab.path === activeTab.value)
    return currentTab && !fixedTabs.includes(currentTab.path)
  })

  // Actions
  function addTab(route) {
    const { path, name, meta } = route
    
    // Skip if tab already exists
    if (tabs.value.some(tab => tab.path === path)) {
      activeTab.value = path
      return
    }

    // Skip tabs without title or marked as no-tab
    if (!meta?.title || meta.noTab) {
      return
    }

    const newTab = {
      path,
      name,
      title: meta.title,
      icon: meta.icon,
      closable: !fixedTabs.includes(path),
      timestamp: Date.now()
    }

    // If we're at max capacity, remove the oldest non-fixed tab
    if (tabs.value.length >= maxTabs) {
      const oldestRemovableIndex = tabs.value.findIndex(tab => 
        tab.closable && tab.path !== path
      )
      if (oldestRemovableIndex !== -1) {
        const removedTab = tabs.value[oldestRemovableIndex]
        tabs.value.splice(oldestRemovableIndex, 1)
        cachedViews.value.delete(removedTab.name)
      }
    }

    tabs.value.push(newTab)
    activeTab.value = path
    
    // Add to cached views if keepAlive is enabled
    if (meta.keepAlive !== false && name) {
      cachedViews.value.add(name)
    }

    // Save to sessionStorage
    saveTabsToStorage()
  }

  function removeTab(targetPath) {
    const targetIndex = tabs.value.findIndex(tab => tab.path === targetPath)
    if (targetIndex === -1) return

    const targetTab = tabs.value[targetIndex]
    
    // Don't remove fixed tabs
    if (!targetTab.closable) return

    // Remove from cached views
    if (targetTab.name) {
      cachedViews.value.delete(targetTab.name)
    }

    tabs.value.splice(targetIndex, 1)

    // If the closed tab was active, activate another tab
    if (activeTab.value === targetPath && tabs.value.length > 0) {
      // Try to activate the tab to the right, or the last tab
      const newActiveTab = tabs.value[targetIndex] || tabs.value[tabs.value.length - 1]
      activeTab.value = newActiveTab.path
    }

    saveTabsToStorage()
    return activeTab.value
  }

  function removeTabsByPattern(pattern) {
    const toRemove = tabs.value.filter(tab => {
      if (!tab.closable) return false
      if (typeof pattern === 'string') {
        return tab.path.includes(pattern)
      }
      if (pattern instanceof RegExp) {
        return pattern.test(tab.path)
      }
      return false
    })

    toRemove.forEach(tab => removeTab(tab.path))
    return toRemove.length
  }

  function removeOtherTabs(keepPath) {
    const toRemove = tabs.value.filter(tab => 
      tab.path !== keepPath && tab.closable
    )

    toRemove.forEach(tab => {
      if (tab.name) {
        cachedViews.value.delete(tab.name)
      }
    })

    tabs.value = tabs.value.filter(tab => 
      tab.path === keepPath || !tab.closable
    )

    activeTab.value = keepPath
    saveTabsToStorage()
  }

  function removeAllTabs() {
    // Keep only fixed tabs
    const fixedTabsData = tabs.value.filter(tab => !tab.closable)
    
    // Clear cached views for removed tabs
    tabs.value.forEach(tab => {
      if (tab.closable && tab.name) {
        cachedViews.value.delete(tab.name)
      }
    })

    tabs.value = fixedTabsData
    
    // Set active tab to first fixed tab or empty
    if (fixedTabsData.length > 0) {
      activeTab.value = fixedTabsData[0].path
    } else {
      activeTab.value = ''
    }

    saveTabsToStorage()
  }

  function clearTabs() {
    tabs.value = []
    activeTab.value = ''
    cachedViews.value.clear()
    saveTabsToStorage()
  }

  function refreshTab(path) {
    const tab = tabs.value.find(tab => tab.path === path)
    if (tab && tab.name) {
      // Remove from cached views to force re-render
      cachedViews.value.delete(tab.name)
      
      // Re-add after a short delay
      setTimeout(() => {
        cachedViews.value.add(tab.name)
      }, 100)
    }
  }

  function moveTab(from, to) {
    if (from < 0 || from >= tabs.value.length || to < 0 || to >= tabs.value.length) {
      return
    }
    
    const tab = tabs.value.splice(from, 1)[0]
    tabs.value.splice(to, 0, tab)
    saveTabsToStorage()
  }

  function setActiveTab(path) {
    if (tabs.value.some(tab => tab.path === path)) {
      activeTab.value = path
    }
  }

  function updateTabTitle(path, title) {
    const tab = tabs.value.find(tab => tab.path === path)
    if (tab) {
      tab.title = title
      saveTabsToStorage()
    }
  }

  function getTabByPath(path) {
    return tabs.value.find(tab => tab.path === path)
  }

  function getTabIndex(path) {
    return tabs.value.findIndex(tab => tab.path === path)
  }

  function isTabActive(path) {
    return activeTab.value === path
  }

  function isTabCached(name) {
    return cachedViews.value.has(name)
  }

  // Storage functions
  function saveTabsToStorage() {
    try {
      const tabsData = {
        tabs: tabs.value,
        activeTab: activeTab.value,
        cachedViews: Array.from(cachedViews.value)
      }
      sessionStorage.setItem('tabs-data', JSON.stringify(tabsData))
    } catch (error) {
      console.warn('Failed to save tabs to storage:', error)
    }
  }

  function loadTabsFromStorage() {
    try {
      const saved = sessionStorage.getItem('tabs-data')
      if (saved) {
        const tabsData = JSON.parse(saved)
        if (tabsData.tabs && Array.isArray(tabsData.tabs)) {
          tabs.value = tabsData.tabs
          activeTab.value = tabsData.activeTab || ''
          cachedViews.value = new Set(tabsData.cachedViews || [])
        }
      }
    } catch (error) {
      console.warn('Failed to load tabs from storage:', error)
    }
  }

  function clearTabsStorage() {
    try {
      sessionStorage.removeItem('tabs-data')
    } catch (error) {
      console.warn('Failed to clear tabs storage:', error)
    }
  }

  // Initialize tabs from storage
  function initTabs() {
    loadTabsFromStorage()
  }

  // Tab context menu actions
  const contextMenuActions = {
    refresh: (path) => refreshTab(path),
    close: (path) => removeTab(path),
    closeOthers: (path) => removeOtherTabs(path),
    closeAll: () => removeAllTabs(),
    closeLeft: (path) => {
      const index = getTabIndex(path)
      if (index > 0) {
        const toRemove = tabs.value.slice(0, index).filter(tab => tab.closable)
        toRemove.forEach(tab => removeTab(tab.path))
      }
    },
    closeRight: (path) => {
      const index = getTabIndex(path)
      if (index !== -1 && index < tabs.value.length - 1) {
        const toRemove = tabs.value.slice(index + 1).filter(tab => tab.closable)
        toRemove.forEach(tab => removeTab(tab.path))
      }
    }
  }

  return {
    // State
    tabs,
    activeTab,
    cachedViews,
    maxTabs,
    fixedTabs,
    
    // Getters
    tabsCount,
    hasMultipleTabs,
    canCloseCurrentTab,
    
    // Actions
    addTab,
    removeTab,
    removeTabsByPattern,
    removeOtherTabs,
    removeAllTabs,
    clearTabs,
    refreshTab,
    moveTab,
    setActiveTab,
    updateTabTitle,
    getTabByPath,
    getTabIndex,
    isTabActive,
    isTabCached,
    initTabs,
    
    // Storage
    saveTabsToStorage,
    loadTabsFromStorage,
    clearTabsStorage,
    
    // Context menu actions
    contextMenuActions
  }
})