import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const permissions = ref([])

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '')
  const userRole = computed(() => userInfo.value?.role || 'user')

  // Actions
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function setPermissions(perms) {
    permissions.value = perms
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
  }

  function hasPermission(permission) {
    return permissions.value.includes(permission) || userRole.value === 'admin'
  }

  return {
    // State
    token,
    userInfo,
    permissions,
    // Getters
    isAuthenticated,
    userName,
    userRole,
    // Actions
    setToken,
    setUserInfo,
    setPermissions,
    logout,
    hasPermission
  }
})