import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useLayoutStore } from './layout'

export const useSettingsStore = defineStore('settings', () => {
  // State
  const userSettings = ref({
    // Appearance
    theme: 'light', // 'light', 'dark', 'auto'
    language: 'zh-CN',
    fontSize: 'medium', // 'small', 'medium', 'large'
    compactMode: false,
    animationsEnabled: true,
    
    // Layout
    sidebarCollapsed: false,
    showBreadcrumb: true,
    showTabs: true,
    tabsStyle: 'card', // 'card', 'border-card'
    fixedHeader: true,
    
    // Functionality
    autoSave: true,
    autoSaveInterval: 60, // seconds
    confirmBeforeDelete: true,
    showNotifications: true,
    soundEnabled: true,
    
    // Performance
    pageSize: 20,
    enableLazyLoading: true,
    cacheEnabled: true,
    preloadNextPage: false,
    
    // Privacy
    trackUsage: true,
    allowAnalytics: true,
    shareErrorReports: true,
    
    // Notifications
    emailNotifications: true,
    pushNotifications: false,
    notificationSound: 'default',
    desktopNotifications: false,
    
    // Editor preferences
    editorTheme: 'default',
    wordWrap: true,
    lineNumbers: true,
    autoComplete: true,
    
    // Date and Time
    dateFormat: 'YYYY-MM-DD',
    timeFormat: '24h', // '12h', '24h'
    timezone: 'Asia/Shanghai',
    
    // Export settings
    defaultExportFormat: 'pdf', // 'pdf', 'excel', 'word'
    includeCharts: true,
    includeMetadata: false
  })

  const systemSettings = ref({
    version: '1.0.0',
    buildDate: new Date().toISOString(),
    environment: 'production',
    apiVersion: 'v1',
    supportedLanguages: ['zh-CN', 'en-US'],
    supportedThemes: ['light', 'dark'],
    maxFileSize: 10 * 1024 * 1024, // 10MB
    sessionTimeout: 30 * 60 * 1000, // 30 minutes
    features: {
      multiLanguage: true,
      darkMode: true,
      exportPDF: true,
      realTimeUpdates: false,
      aiSuggestions: false
    }
  })

  const shortcuts = ref({
    toggleSidebar: 'Ctrl+B',
    search: 'Ctrl+K',
    save: 'Ctrl+S',
    newReport: 'Ctrl+N',
    toggleTheme: 'Ctrl+Shift+T',
    focusSearch: '/',
    openSettings: 'Ctrl+,',
    showHelp: '?'
  })

  // Getters
  const isDarkMode = computed(() => {
    if (userSettings.value.theme === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return userSettings.value.theme === 'dark'
  })

  const currentLanguage = computed(() => userSettings.value.language)
  const isCompactMode = computed(() => userSettings.value.compactMode)
  const animationsEnabled = computed(() => userSettings.value.animationsEnabled)
  const autoSaveEnabled = computed(() => userSettings.value.autoSave)
  const notificationsEnabled = computed(() => userSettings.value.showNotifications)

  // Computed settings groups for UI organization
  const appearanceSettings = computed(() => ({
    theme: userSettings.value.theme,
    language: userSettings.value.language,
    fontSize: userSettings.value.fontSize,
    compactMode: userSettings.value.compactMode,
    animationsEnabled: userSettings.value.animationsEnabled
  }))

  const layoutSettings = computed(() => ({
    sidebarCollapsed: userSettings.value.sidebarCollapsed,
    showBreadcrumb: userSettings.value.showBreadcrumb,
    showTabs: userSettings.value.showTabs,
    tabsStyle: userSettings.value.tabsStyle,
    fixedHeader: userSettings.value.fixedHeader
  }))

  const notificationSettings = computed(() => ({
    emailNotifications: userSettings.value.emailNotifications,
    pushNotifications: userSettings.value.pushNotifications,
    notificationSound: userSettings.value.notificationSound,
    desktopNotifications: userSettings.value.desktopNotifications
  }))

  // Actions
  function updateSetting(key, value) {
    if (userSettings.value.hasOwnProperty(key)) {
      userSettings.value[key] = value
      saveSetting(key, value)
      
      // Apply certain settings immediately
      applySetting(key, value)
    }
  }

  function updateSettings(settings) {
    Object.keys(settings).forEach(key => {
      if (userSettings.value.hasOwnProperty(key)) {
        userSettings.value[key] = settings[key]
      }
    })
    saveAllSettings()
    applyAllSettings()
  }

  function resetSettings() {
    const defaultSettings = {
      theme: 'light',
      language: 'zh-CN',
      fontSize: 'medium',
      compactMode: false,
      animationsEnabled: true,
      sidebarCollapsed: false,
      showBreadcrumb: true,
      showTabs: true,
      tabsStyle: 'card',
      fixedHeader: true,
      autoSave: true,
      autoSaveInterval: 60,
      confirmBeforeDelete: true,
      showNotifications: true,
      soundEnabled: true,
      pageSize: 20,
      enableLazyLoading: true,
      cacheEnabled: true,
      preloadNextPage: false,
      trackUsage: true,
      allowAnalytics: true,
      shareErrorReports: true,
      emailNotifications: true,
      pushNotifications: false,
      notificationSound: 'default',
      desktopNotifications: false,
      editorTheme: 'default',
      wordWrap: true,
      lineNumbers: true,
      autoComplete: true,
      dateFormat: 'YYYY-MM-DD',
      timeFormat: '24h',
      timezone: 'Asia/Shanghai',
      defaultExportFormat: 'pdf',
      includeCharts: true,
      includeMetadata: false
    }
    
    userSettings.value = { ...defaultSettings }
    saveAllSettings()
    applyAllSettings()
  }

  function toggleTheme() {
    const currentTheme = userSettings.value.theme
    const newTheme = currentTheme === 'light' ? 'dark' : 'light'
    updateSetting('theme', newTheme)
  }

  function setLanguage(language) {
    updateSetting('language', language)
  }

  function toggleCompactMode() {
    updateSetting('compactMode', !userSettings.value.compactMode)
  }

  function toggleAnimations() {
    updateSetting('animationsEnabled', !userSettings.value.animationsEnabled)
  }

  function updateShortcut(action, shortcut) {
    if (shortcuts.value.hasOwnProperty(action)) {
      shortcuts.value[action] = shortcut
      saveShortcuts()
    }
  }

  function resetShortcuts() {
    shortcuts.value = {
      toggleSidebar: 'Ctrl+B',
      search: 'Ctrl+K',
      save: 'Ctrl+S',
      newReport: 'Ctrl+N',
      toggleTheme: 'Ctrl+Shift+T',
      focusSearch: '/',
      openSettings: 'Ctrl+,',
      showHelp: '?'
    }
    saveShortcuts()
  }

  // Apply individual setting
  function applySetting(key, value) {
    switch (key) {
      case 'theme':
        applyTheme(value)
        break
      case 'fontSize':
        applyFontSize(value)
        break
      case 'animationsEnabled':
        applyAnimations(value)
        break
      case 'sidebarCollapsed':
        const layoutStore = useLayoutStore()
        layoutStore.setSidebarCollapsed(value)
        break
      case 'showTabs':
        const layoutStore2 = useLayoutStore()
        layoutStore2.setShowTabs(value)
        break
    }
  }

  // Apply all settings
  function applyAllSettings() {
    applyTheme(userSettings.value.theme)
    applyFontSize(userSettings.value.fontSize)
    applyAnimations(userSettings.value.animationsEnabled)
    
    // Apply layout settings
    const layoutStore = useLayoutStore()
    layoutStore.setSidebarCollapsed(userSettings.value.sidebarCollapsed)
    layoutStore.setShowTabs(userSettings.value.showTabs)
    layoutStore.setShowBreadcrumb(userSettings.value.showBreadcrumb)
  }

  function applyTheme(theme) {
    const htmlElement = document.documentElement
    
    if (theme === 'auto') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      htmlElement.classList.toggle('dark', prefersDark)
    } else {
      htmlElement.classList.toggle('dark', theme === 'dark')
    }
  }

  function applyFontSize(fontSize) {
    const htmlElement = document.documentElement
    htmlElement.classList.remove('font-small', 'font-medium', 'font-large')
    htmlElement.classList.add(`font-${fontSize}`)
  }

  function applyAnimations(enabled) {
    const htmlElement = document.documentElement
    htmlElement.classList.toggle('no-animations', !enabled)
  }

  // Persistence
  function saveSetting(key, value) {
    try {
      const saved = JSON.parse(localStorage.getItem('userSettings') || '{}')
      saved[key] = value
      localStorage.setItem('userSettings', JSON.stringify(saved))
    } catch (error) {
      console.warn('Error saving setting:', error)
    }
  }

  function saveAllSettings() {
    try {
      localStorage.setItem('userSettings', JSON.stringify(userSettings.value))
    } catch (error) {
      console.warn('Error saving all settings:', error)
    }
  }

  function saveShortcuts() {
    try {
      localStorage.setItem('shortcuts', JSON.stringify(shortcuts.value))
    } catch (error) {
      console.warn('Error saving shortcuts:', error)
    }
  }

  function loadSettings() {
    try {
      const saved = localStorage.getItem('userSettings')
      if (saved) {
        const parsedSettings = JSON.parse(saved)
        userSettings.value = { ...userSettings.value, ...parsedSettings }
      }

      const savedShortcuts = localStorage.getItem('shortcuts')
      if (savedShortcuts) {
        shortcuts.value = { ...shortcuts.value, ...JSON.parse(savedShortcuts) }
      }
    } catch (error) {
      console.warn('Error loading settings:', error)
    }
  }

  function clearSettings() {
    localStorage.removeItem('userSettings')
    localStorage.removeItem('shortcuts')
  }

  // Export/Import settings
  function exportSettings() {
    return {
      userSettings: userSettings.value,
      shortcuts: shortcuts.value,
      exportDate: new Date().toISOString(),
      version: systemSettings.value.version
    }
  }

  function importSettings(settingsData) {
    if (settingsData.userSettings) {
      userSettings.value = { ...userSettings.value, ...settingsData.userSettings }
    }
    if (settingsData.shortcuts) {
      shortcuts.value = { ...shortcuts.value, ...settingsData.shortcuts }
    }
    saveAllSettings()
    saveShortcuts()
    applyAllSettings()
  }

  // Initialize settings
  function initializeSettings() {
    loadSettings()
    applyAllSettings()
    
    // Watch for system theme changes
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', () => {
        if (userSettings.value.theme === 'auto') {
          applyTheme('auto')
        }
      })
    }
  }

  return {
    // State
    userSettings,
    systemSettings,
    shortcuts,
    
    // Getters
    isDarkMode,
    currentLanguage,
    isCompactMode,
    animationsEnabled,
    autoSaveEnabled,
    notificationsEnabled,
    appearanceSettings,
    layoutSettings,
    notificationSettings,
    
    // Actions
    updateSetting,
    updateSettings,
    resetSettings,
    toggleTheme,
    setLanguage,
    toggleCompactMode,
    toggleAnimations,
    updateShortcut,
    resetShortcuts,
    exportSettings,
    importSettings,
    initializeSettings,
    clearSettings
  }
})