import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useTabsStore } from './tabs'
import { useLayoutStore } from './layout'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const permissions = ref([])
  const preferences = ref({
    language: 'zh-CN',
    theme: 'light',
    dateFormat: 'YYYY-MM-DD',
    timezone: 'Asia/Shanghai'
  })
  const loginHistory = ref([])
  const isLoading = ref(false)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || userInfo.value?.username || '')
  const userRole = computed(() => userInfo.value?.role || 'user')
  const userAvatar = computed(() => userInfo.value?.avatar || '')
  const userEmail = computed(() => userInfo.value?.email || '')
  const userDepartment = computed(() => userInfo.value?.department || '')
  
  const isAdmin = computed(() => userRole.value === 'admin')
  const isManager = computed(() => ['manager', 'admin'].includes(userRole.value))
  const isUser = computed(() => userRole.value === 'user')

  // Permission helpers
  const canCreateReports = computed(() => 
    hasPermission('reports:create') || ['user', 'manager', 'admin'].includes(userRole.value)
  )
  const canApproveReports = computed(() => 
    hasPermission('reports:approve') || ['manager', 'admin'].includes(userRole.value)
  )
  const canManageUsers = computed(() => 
    hasPermission('users:manage') || userRole.value === 'admin'
  )
  const canViewAnalytics = computed(() => 
    hasPermission('analytics:view') || ['manager', 'admin'].includes(userRole.value)
  )

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
      // Set axios default header if available
      if (window.axios) {
        window.axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      }
    } else {
      localStorage.removeItem('token')
      if (window.axios) {
        delete window.axios.defaults.headers.common['Authorization']
      }
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
    // Save user info to localStorage for persistence
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  function setPermissions(perms) {
    permissions.value = Array.isArray(perms) ? perms : []
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
  }

  function setPreferences(prefs) {
    preferences.value = { ...preferences.value, ...prefs }
    localStorage.setItem('userPreferences', JSON.stringify(preferences.value))
  }

  function updateProfile(profileData) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...profileData }
      setUserInfo(userInfo.value)
    }
  }

  function addLoginRecord(record) {
    loginHistory.value.unshift({
      ...record,
      timestamp: new Date().toISOString()
    })
    // Keep only last 10 login records
    if (loginHistory.value.length > 10) {
      loginHistory.value = loginHistory.value.slice(0, 10)
    }
    localStorage.setItem('loginHistory', JSON.stringify(loginHistory.value))
  }

  function logout() {
    // Clear user data
    token.value = ''
    userInfo.value = null
    permissions.value = []
    
    // Clear localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    
    // Clear other stores if available
    try {
      const tabsStore = useTabsStore()
      tabsStore.clearTabs()
      
      const layoutStore = useLayoutStore()
      layoutStore.resetLayout()
    } catch (error) {
      // Ignore errors if stores are not available
      console.warn('Error clearing stores:', error)
    }
    
    // Clear axios header
    if (window.axios) {
      delete window.axios.defaults.headers.common['Authorization']
    }
  }

  function hasPermission(permission) {
    if (!permission) return true
    if (userRole.value === 'admin') return true
    return permissions.value.includes(permission)
  }

  function hasAnyPermission(permissionList) {
    if (!permissionList || permissionList.length === 0) return true
    if (userRole.value === 'admin') return true
    return permissionList.some(permission => permissions.value.includes(permission))
  }

  function hasRole(role) {
    return userRole.value === role
  }

  function hasAnyRole(roles) {
    return roles.includes(userRole.value)
  }

  function setLoading(loading) {
    isLoading.value = loading
  }

  // Initialize user data from localStorage
  function initializeUser() {
    try {
      const savedUserInfo = localStorage.getItem('userInfo')
      if (savedUserInfo) {
        userInfo.value = JSON.parse(savedUserInfo)
      }

      const savedPermissions = localStorage.getItem('permissions')
      if (savedPermissions) {
        permissions.value = JSON.parse(savedPermissions)
      }

      const savedPreferences = localStorage.getItem('userPreferences')
      if (savedPreferences) {
        preferences.value = { ...preferences.value, ...JSON.parse(savedPreferences) }
      }

      const savedLoginHistory = localStorage.getItem('loginHistory')
      if (savedLoginHistory) {
        loginHistory.value = JSON.parse(savedLoginHistory)
      }

      // Set axios header if token exists
      if (token.value && window.axios) {
        window.axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      }
    } catch (error) {
      console.warn('Error initializing user data:', error)
    }
  }

  // Reset user state completely
  function resetUser() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    preferences.value = {
      language: 'zh-CN',
      theme: 'light',
      dateFormat: 'YYYY-MM-DD',
      timezone: 'Asia/Shanghai'
    }
    loginHistory.value = []
    isLoading.value = false
    
    // Clear all localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    localStorage.removeItem('userPreferences')
    localStorage.removeItem('loginHistory')
  }

  return {
    // State
    token,
    userInfo,
    permissions,
    preferences,
    loginHistory,
    isLoading,
    
    // Getters
    isAuthenticated,
    userName,
    userRole,
    userAvatar,
    userEmail,
    userDepartment,
    isAdmin,
    isManager,
    isUser,
    canCreateReports,
    canApproveReports,
    canManageUsers,
    canViewAnalytics,
    
    // Actions
    setToken,
    setUserInfo,
    setPermissions,
    setPreferences,
    updateProfile,
    addLoginRecord,
    logout,
    hasPermission,
    hasAnyPermission,
    hasRole,
    hasAnyRole,
    setLoading,
    initializeUser,
    resetUser
  }
})