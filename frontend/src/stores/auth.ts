import { defineStore } from 'pinia'
import { ref, computed, readonly, type Ref } from 'vue'
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth'
import { authAPI } from '@/services/api'

// 强制使用相对路径，利用Vite代理配置
const API_BASE_URL = '/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const initialized = ref(false)
  const isOffline = ref(false) // 标记是否处于离线状态

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isSuperAdmin = computed(() => user.value?.role === 'SUPER_ADMIN')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isManager = computed(() => user.value?.role === 'MANAGER')
  
  // Role-based permission checks for three-tier system
  const canCreateProject = computed(() => 
    isSuperAdmin.value || isAdmin.value || isManager.value
  )
  const canApproveProject = computed(() => 
    isSuperAdmin.value || isAdmin.value
  )
  const canFinalApproveProject = computed(() => isSuperAdmin.value)
  const canViewAllReports = computed(() => 
    isSuperAdmin.value || isAdmin.value
  )
  const canViewAllProjects = computed(() => 
    isSuperAdmin.value || isAdmin.value
  )
  const canManageUsers = computed(() => isSuperAdmin.value)

  // Initialize from localStorage
  async function initialize() {
    try {
      const storedToken = localStorage.getItem('auth_token')
      const storedRefreshToken = localStorage.getItem('refresh_token')
      const storedUser = localStorage.getItem('auth_user')

      console.log('🔄 Initializing auth store:', {
        hasToken: !!storedToken,
        hasUser: !!storedUser,
        hasRefreshToken: !!storedRefreshToken
      })

      if (storedToken && storedUser) {
        token.value = storedToken
        refreshToken.value = storedRefreshToken
        user.value = JSON.parse(storedUser)

        console.log('🔄 Auth state loaded:', {
          tokenSet: !!token.value,
          userSet: !!user.value,
          userId: user.value?.id,
          userRole: user.value?.role
        })

        // 简单验证token格式和有效期
        if (isTokenExpired(storedToken)) {
          console.log('❌ Token expired, attempting refresh...')
          // 尝试刷新token
          const refreshSuccess = await refreshTokens()
          if (!refreshSuccess) {
            console.log('❌ Token refresh failed, clearing auth state')
            clearAuthState()
          } else {
            console.log('✅ Token refreshed successfully')
          }
        } else {
          // Token appears valid, proceed with network verification but be fault-tolerant
          console.log('🔍 Token format valid, attempting network verification...')
          try {
            const isValid = await verifyToken()
            console.log('🔍 Token verification result:', isValid)
            
            if (!isValid) {
              console.log('❌ Token invalid, clearing auth state')
              clearAuthState()
            } else {
              isOffline.value = false // 连接成功，标记为在线
            }
          } catch (error) {
            console.log('⚠️ Token verification failed due to network/server error, keeping existing auth state:', error)
            // 网络错误时不清除token，允许用户继续使用
            // 但可以设置一个标志来表示网络状态
            if (error instanceof Error && 
                (error.message.includes('Unable to connect to server') || 
                 error.message.includes('Request timeout') ||
                 error.message.includes('ERR_CONNECTION_REFUSED') ||
                 (error.name === 'TypeError' && error.message === 'Failed to fetch'))) {
              console.log('🌐 Backend server appears to be offline, but keeping auth state for offline usage')
              isOffline.value = true
            } else {
              isOffline.value = false
            }
          }
        }
      } else {
        console.log('⚠️ No stored auth data found')
      }
    } catch (error) {
      console.error('Failed to initialize auth:', error)
      clearAuthState()
    } finally {
      initialized.value = true
      console.log('✅ Auth initialization complete')
    }
  }

  function clearAuthState() {
    // Clear local state
    user.value = null
    token.value = null
    refreshToken.value = null

    // Clear localStorage
    localStorage.removeItem('auth_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('auth_user')
  }

  // Helper function to check if JWT token is expired
  function isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const currentTime = Date.now() / 1000
      return payload.exp < currentTime
    } catch (error) {
      console.log('🔍 Unable to parse token, treating as expired:', error)
      return true // If we can't parse it, treat it as expired
    }
  }

  async function login(credentials: LoginRequest): Promise<{ success: boolean; message?: string }> {
    try {
      // Transform frontend data structure to match backend expectations
      const backendCredentials = {
        usernameOrEmail: credentials.username,  // Map username to usernameOrEmail
        password: credentials.password
      }

      const result = await authAPI.login(backendCredentials)

      if (result.success) {
        token.value = result.data.accessToken || result.data.token  // Handle both accessToken and token
        refreshToken.value = result.data.refreshToken
        user.value = result.data.user

        // Store in localStorage
        localStorage.setItem('auth_token', result.data.accessToken || result.data.token)
        if (result.data.refreshToken) {
          localStorage.setItem('refresh_token', result.data.refreshToken)
        }
        localStorage.setItem('auth_user', JSON.stringify(result.data.user))

        console.log('🔄 Auth state after login:', {
          tokenSet: !!token.value,
          userSet: !!user.value,
          isAuthenticated: isAuthenticated.value,
          user: user.value
        })

        return { success: true }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('Login error:', error)
      return { success: false, message: 'Network error occurred' }
    }
  }

  async function register(userData: RegisterRequest): Promise<{ success: boolean; message?: string }> {
    try {
      // Transform frontend data structure to match backend expectations
      const backendUserData: any = {
        username: userData.username,
        email: userData.email,
        password: userData.password,
        confirmPassword: userData.confirmPassword,
        fullName: userData.fullName,
        role: userData.role || 'MANAGER'
      }
      
      // Only include optional fields if they have values
      if (userData.employeeId && userData.employeeId.trim()) {
        backendUserData.employeeId = userData.employeeId.trim()
      }
      if (userData.phone && userData.phone.trim()) {
        backendUserData.phone = userData.phone.trim()
      }
      if (userData.position && userData.position.trim()) {
        backendUserData.position = userData.position.trim()
      }
      // TODO: Map department to departmentId when department management is implemented

      const result = await authAPI.register(backendUserData)

      if (result.success) {
        token.value = result.data.accessToken || result.data.token  // Handle both accessToken and token
        refreshToken.value = result.data.refreshToken
        user.value = result.data.user

        // Store in localStorage
        localStorage.setItem('auth_token', result.data.accessToken || result.data.token)
        if (result.data.refreshToken) {
          localStorage.setItem('refresh_token', result.data.refreshToken)
        }
        localStorage.setItem('auth_user', JSON.stringify(result.data.user))

        return { success: true }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('Register error:', error)
      return { success: false, message: 'Network error occurred' }
    }
  }

  async function logout() {
    try {
      if (token.value) {
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token.value}`,
            'Content-Type': 'application/json',
          },
        })
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      clearAuthState()
    }
  }

  async function verifyToken(): Promise<boolean> {
    if (!token.value) {
      console.log('🔍 No token to verify')
      return false
    }

    try {
      console.log('🔍 Verifying token with endpoint: /api/health/authenticated')
      
      // 使用Promise.race实现更稳定的超时控制，减少超时时间
      const timeoutPromise = new Promise<never>((_, reject) => {
        setTimeout(() => reject(new Error('Request timeout')), 3000) // 3秒超时，快速失败
      })
      
      // 使用127.0.0.1而不是localhost，可能绕过某些代理配置
      const fetchPromise = fetch('/api/health/authenticated', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Content-Type': 'application/json',
          // 代理绕过相关头部
          ...(import.meta.env.VITE_BYPASS_PROXY === 'true' ? {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
          } : {}),
        },
        // 如果启用了缓存禁用，添加cache控制
        ...(import.meta.env.VITE_DISABLE_CACHE === 'true' ? {
          cache: 'no-store'
        } : {}),
      })

      const response = await Promise.race([fetchPromise, timeoutPromise])
      console.log('🔍 Token verification response status:', response.status)

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          console.log('❌ Token verification failed - unauthorized:', response.status)
          return false
        } else {
          console.log('⚠️ Token verification failed with non-auth error:', response.status)
          // 对于非认证错误，不判定为token无效
          throw new Error(`HTTP ${response.status}`)
        }
      }

      const result = await response.json()
      console.log('✅ Token verified successfully:', result)
      return true
    } catch (error) {
      console.error('🔍 Token verification error:', error)
      
      // 检查具体的网络错误类型
      if (error instanceof Error) {
        if (error.message === 'Request timeout') {
          console.log('⚠️ Token verification timeout - assuming network issue')
        } else if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
          console.log('⚠️ Network connection failed (TypeError: Failed to fetch) - likely proxy or server unavailable')
        } else if (error.message.includes('ERR_CONNECTION_REFUSED') ||
                   error.message.includes('net::ERR_CONNECTION_REFUSED') ||
                   error.message.includes('network') ||
                   error.message.includes('fetch')) {
          console.log('⚠️ Network connectivity issue during token verification')
        }
      }
      
      throw error // 重新抛出错误让调用者处理
    }
  }

  async function refreshTokens(): Promise<boolean> {
    if (!refreshToken.value) return false

    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          refreshToken: refreshToken.value,
        }),
      })

      const result: AuthResponse = await response.json()

      if (result.success) {
        token.value = result.data.token
        refreshToken.value = result.data.refreshToken

        localStorage.setItem('auth_token', result.data.token)
        localStorage.setItem('refresh_token', result.data.refreshToken)

        return true
      }

      return false
    } catch (error) {
      console.error('Token refresh error:', error)
      return false
    }
  }

  async function fetchUser(): Promise<boolean> {
    if (!token.value) return false

    try {
      const response = await fetch('/api/users/profile', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.value}`,
          'Content-Type': 'application/json',
        },
      })

      if (response.ok) {
        const result = await response.json()
        if (result.success) {
          user.value = result.data
          localStorage.setItem('auth_user', JSON.stringify(result.data))
          return true
        }
      }

      return false
    } catch (error) {
      console.error('Fetch user error:', error)
      return false
    }
  }

  async function updateTokens(newAccessToken: string, newRefreshToken?: string): Promise<void> {
    token.value = newAccessToken
    localStorage.setItem('auth_token', newAccessToken)
    
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
      localStorage.setItem('refresh_token', newRefreshToken)
    }
    
    console.log('✅ Tokens updated successfully')
  }

  function setUser(newUser: User): void {
    user.value = newUser
    localStorage.setItem('auth_user', JSON.stringify(newUser))
    console.log('✅ User data updated successfully')
  }

  return {
    user: readonly(user),
    token: readonly(token),
    initialized: readonly(initialized),
    isOffline: readonly(isOffline),
    isAuthenticated,
    isSuperAdmin,
    isAdmin,
    isManager,
    canCreateProject,
    canApproveProject,
    canFinalApproveProject,
    canViewAllReports,
    canViewAllProjects,
    canManageUsers,
    initialize,
    login,
    register,
    logout,
    verifyToken,
    refreshTokens,
    fetchUser,
    updateTokens,
    setUser,
    clearAuthState,
  }
})