import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useLayoutStore = defineStore('layout', () => {
  // State
  const sidebarCollapsed = ref(false)
  const isDark = ref(false)
  const showTabs = ref(true)
  const showBreadcrumb = ref(true)
  const device = ref('desktop') // 'desktop', 'tablet', 'mobile'
  const layoutMode = ref('admin') // 'admin', 'simple', 'mobile'
  
  // Responsive breakpoints (px)
  const breakpoints = {
    mobile: 768,
    tablet: 1024
  }

  // Getters
  const isMobile = computed(() => device.value === 'mobile')
  const isTablet = computed(() => device.value === 'tablet')
  const isDesktop = computed(() => device.value === 'desktop')
  
  const currentLayout = computed(() => {
    if (device.value === 'mobile') return 'mobile'
    return layoutMode.value
  })

  // Actions
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    // Save to localStorage
    localStorage.setItem('sidebarCollapsed', sidebarCollapsed.value.toString())
  }

  function setSidebarCollapsed(collapsed) {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('sidebarCollapsed', collapsed.toString())
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    // Save to localStorage
    localStorage.setItem('isDark', isDark.value.toString())
    // Apply theme class to html element
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  function setTheme(dark) {
    isDark.value = dark
    localStorage.setItem('isDark', dark.toString())
    document.documentElement.classList.toggle('dark', dark)
  }

  function toggleTabs() {
    showTabs.value = !showTabs.value
    localStorage.setItem('showTabs', showTabs.value.toString())
  }

  function setShowTabs(show) {
    showTabs.value = show
    localStorage.setItem('showTabs', show.toString())
  }

  function toggleBreadcrumb() {
    showBreadcrumb.value = !showBreadcrumb.value
    localStorage.setItem('showBreadcrumb', showBreadcrumb.value.toString())
  }

  function setShowBreadcrumb(show) {
    showBreadcrumb.value = show
    localStorage.setItem('showBreadcrumb', show.toString())
  }

  function setDevice(deviceType) {
    device.value = deviceType
  }

  function setLayoutMode(mode) {
    layoutMode.value = mode
    localStorage.setItem('layoutMode', mode)
  }

  function updateDeviceType() {
    const width = window.innerWidth
    
    if (width < breakpoints.mobile) {
      setDevice('mobile')
      // Auto collapse sidebar on mobile
      if (!sidebarCollapsed.value) {
        setSidebarCollapsed(true)
      }
    } else if (width < breakpoints.tablet) {
      setDevice('tablet')
    } else {
      setDevice('desktop')
    }
  }

  function initLayout() {
    // Load from localStorage
    const savedCollapsed = localStorage.getItem('sidebarCollapsed')
    if (savedCollapsed !== null) {
      sidebarCollapsed.value = savedCollapsed === 'true'
    }

    const savedDark = localStorage.getItem('isDark')
    if (savedDark !== null) {
      isDark.value = savedDark === 'true'
      document.documentElement.classList.toggle('dark', isDark.value)
    }

    const savedTabs = localStorage.getItem('showTabs')
    if (savedTabs !== null) {
      showTabs.value = savedTabs === 'true'
    }

    const savedBreadcrumb = localStorage.getItem('showBreadcrumb')
    if (savedBreadcrumb !== null) {
      showBreadcrumb.value = savedBreadcrumb === 'true'
    }

    const savedLayoutMode = localStorage.getItem('layoutMode')
    if (savedLayoutMode) {
      layoutMode.value = savedLayoutMode
    }

    // Update device type based on screen size
    updateDeviceType()

    // Add window resize listener
    window.addEventListener('resize', updateDeviceType)
  }

  function resetLayout() {
    sidebarCollapsed.value = false
    isDark.value = false
    showTabs.value = true
    showBreadcrumb.value = true
    layoutMode.value = 'admin'
    
    // Clear localStorage
    localStorage.removeItem('sidebarCollapsed')
    localStorage.removeItem('isDark')
    localStorage.removeItem('showTabs')
    localStorage.removeItem('showBreadcrumb')
    localStorage.removeItem('layoutMode')
    
    // Remove theme class
    document.documentElement.classList.remove('dark')
  }

  // Layout configurations
  const layoutConfig = computed(() => {
    return {
      admin: {
        sidebar: true,
        header: true,
        tabs: showTabs.value,
        breadcrumb: showBreadcrumb.value,
        footer: false
      },
      simple: {
        sidebar: false,
        header: true,
        tabs: false,
        breadcrumb: false,
        footer: true
      },
      mobile: {
        sidebar: false, // Drawer instead
        header: true,
        tabs: false,
        breadcrumb: false,
        footer: false, // Bottom nav instead
        bottomNav: true,
        drawer: true
      }
    }
  })

  // Get current layout config
  const currentLayoutConfig = computed(() => {
    return layoutConfig.value[currentLayout.value] || layoutConfig.value.admin
  })

  return {
    // State
    sidebarCollapsed,
    isDark,
    showTabs,
    showBreadcrumb,
    device,
    layoutMode,
    breakpoints,
    
    // Getters
    isMobile,
    isTablet,
    isDesktop,
    currentLayout,
    layoutConfig,
    currentLayoutConfig,
    
    // Actions
    toggleSidebar,
    setSidebarCollapsed,
    toggleTheme,
    setTheme,
    toggleTabs,
    setShowTabs,
    toggleBreadcrumb,
    setShowBreadcrumb,
    setDevice,
    setLayoutMode,
    updateDeviceType,
    initLayout,
    resetLayout
  }
})