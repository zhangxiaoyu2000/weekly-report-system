import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // State
  const appName = ref('周报系统')
  const version = ref('1.0.0')
  const loading = ref(false)
  const online = ref(navigator.onLine)
  const notifications = ref([])
  const globalSettings = ref({
    enableAnalytics: true,
    enablePushNotifications: false,
    maxFileUploadSize: 10, // MB
    sessionTimeout: 30, // minutes
    autoSave: true,
    autoSaveInterval: 60 // seconds
  })
  
  // Error state
  const errors = ref([])
  const criticalError = ref(null)
  
  // Feature flags
  const features = ref({
    darkMode: true,
    mobileLayout: true,
    tabsNavigation: true,
    breadcrumbNavigation: true,
    aiAnalysis: false,
    realTimeNotifications: false,
    exportToPDF: true,
    bulkOperations: false
  })
  
  // Performance metrics
  const metrics = ref({
    loadTime: 0,
    apiCalls: 0,
    errors: 0,
    userActions: 0
  })

  // Getters
  const isOnline = computed(() => online.value)
  const hasNotifications = computed(() => notifications.value.length > 0)
  const unreadNotifications = computed(() => 
    notifications.value.filter(n => !n.read).length
  )
  const hasCriticalError = computed(() => !!criticalError.value)
  const isFeatureEnabled = computed(() => (feature) => features.value[feature] || false)

  // Actions
  function setLoading(isLoading) {
    loading.value = isLoading
  }

  function setOnlineStatus(status) {
    online.value = status
  }

  function addNotification(notification) {
    const newNotification = {
      id: Date.now().toString(),
      timestamp: new Date().toISOString(),
      read: false,
      type: 'info', // info, success, warning, error
      ...notification
    }
    notifications.value.unshift(newNotification)
    
    // Limit notifications to 100
    if (notifications.value.length > 100) {
      notifications.value = notifications.value.slice(0, 100)
    }
  }

  function markNotificationAsRead(id) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  function markAllNotificationsAsRead() {
    notifications.value.forEach(n => n.read = true)
  }

  function removeNotification(id) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  function clearNotifications() {
    notifications.value = []
  }

  function addError(error) {
    const errorObj = {
      id: Date.now().toString(),
      timestamp: new Date().toISOString(),
      message: error.message || error,
      stack: error.stack,
      resolved: false,
      ...error
    }
    
    errors.value.unshift(errorObj)
    metrics.value.errors++
    
    // Limit errors to 50
    if (errors.value.length > 50) {
      errors.value = errors.value.slice(0, 50)
    }
  }

  function setCriticalError(error) {
    criticalError.value = {
      message: error.message || error,
      timestamp: new Date().toISOString(),
      ...error
    }
  }

  function clearCriticalError() {
    criticalError.value = null
  }

  function resolveError(id) {
    const error = errors.value.find(e => e.id === id)
    if (error) {
      error.resolved = true
    }
  }

  function clearErrors() {
    errors.value = []
    criticalError.value = null
  }

  function updateGlobalSettings(settings) {
    globalSettings.value = { ...globalSettings.value, ...settings }
    localStorage.setItem('globalSettings', JSON.stringify(globalSettings.value))
  }

  function toggleFeature(featureName) {
    if (features.value.hasOwnProperty(featureName)) {
      features.value[featureName] = !features.value[featureName]
      localStorage.setItem('features', JSON.stringify(features.value))
    }
  }

  function enableFeature(featureName) {
    if (features.value.hasOwnProperty(featureName)) {
      features.value[featureName] = true
      localStorage.setItem('features', JSON.stringify(features.value))
    }
  }

  function disableFeature(featureName) {
    if (features.value.hasOwnProperty(featureName)) {
      features.value[featureName] = false
      localStorage.setItem('features', JSON.stringify(features.value))
    }
  }

  function recordUserAction() {
    metrics.value.userActions++
  }

  function recordApiCall() {
    metrics.value.apiCalls++
  }

  function setLoadTime(time) {
    metrics.value.loadTime = time
  }

  function resetMetrics() {
    metrics.value = {
      loadTime: 0,
      apiCalls: 0,
      errors: 0,
      userActions: 0
    }
  }

  // Initialize app state
  function initializeApp() {
    try {
      // Load settings from localStorage
      const savedSettings = localStorage.getItem('globalSettings')
      if (savedSettings) {
        globalSettings.value = { ...globalSettings.value, ...JSON.parse(savedSettings) }
      }

      const savedFeatures = localStorage.getItem('features')
      if (savedFeatures) {
        features.value = { ...features.value, ...JSON.parse(savedFeatures) }
      }

      // Set up online/offline listeners
      window.addEventListener('online', () => setOnlineStatus(true))
      window.addEventListener('offline', () => setOnlineStatus(false))
      
      // Auto-save notifications to localStorage
      const savedNotifications = localStorage.getItem('notifications')
      if (savedNotifications) {
        notifications.value = JSON.parse(savedNotifications)
      }

    } catch (error) {
      console.warn('Error initializing app state:', error)
    }
  }

  // Save notifications to localStorage
  function saveNotifications() {
    try {
      localStorage.setItem('notifications', JSON.stringify(notifications.value))
    } catch (error) {
      console.warn('Error saving notifications:', error)
    }
  }

  // Reset app state
  function resetApp() {
    loading.value = false
    notifications.value = []
    errors.value = []
    criticalError.value = null
    metrics.value = {
      loadTime: 0,
      apiCalls: 0,
      errors: 0,
      userActions: 0
    }
    
    // Clear localStorage
    localStorage.removeItem('globalSettings')
    localStorage.removeItem('features')
    localStorage.removeItem('notifications')
  }

  return {
    // State
    appName,
    version,
    loading,
    online,
    notifications,
    globalSettings,
    errors,
    criticalError,
    features,
    metrics,
    
    // Getters
    isOnline,
    hasNotifications,
    unreadNotifications,
    hasCriticalError,
    isFeatureEnabled,
    
    // Actions
    setLoading,
    setOnlineStatus,
    addNotification,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    removeNotification,
    clearNotifications,
    addError,
    setCriticalError,
    clearCriticalError,
    resolveError,
    clearErrors,
    updateGlobalSettings,
    toggleFeature,
    enableFeature,
    disableFeature,
    recordUserAction,
    recordApiCall,
    setLoadTime,
    resetMetrics,
    initializeApp,
    saveNotifications,
    resetApp
  }
})