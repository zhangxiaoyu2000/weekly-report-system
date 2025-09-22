<template>
  <div class="min-h-screen flex">
    <!-- Left Panel - Brand & Welcome -->
    <div class="flex-1 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-20 xl:px-24 bg-gradient-to-br from-primary-600 to-primary-800">
      <div class="mx-auto w-full max-w-sm lg:w-96">
        <div class="text-center">
          <div class="flex items-center justify-center mb-6">
            <div class="bg-white/10 backdrop-blur-sm rounded-2xl p-4">
              <svg class="h-12 w-12 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
          </div>
          <h1 class="text-3xl font-bold text-white mb-2">WeeklyReport</h1>
          <p class="text-xl text-primary-100 mb-8">现代化团队协作平台</p>
          <div class="space-y-4 text-primary-100">
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-3 text-primary-200" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              智能周报管理
            </div>
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-3 text-primary-200" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              项目协作跟踪  
            </div>
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-3 text-primary-200" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              数据可视化分析
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel - Login Form -->
    <div class="flex-1 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-20 xl:px-24 bg-white">
      <div class="mx-auto w-full max-w-sm lg:w-96">
        <div>
          <h2 class="text-3xl font-bold text-gray-900 mb-2">欢迎回来</h2>
          <p class="text-gray-600 mb-8">请登录您的账户继续使用</p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Error Display -->
          <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {{ error }}
          </div>

          <!-- Username Field -->
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
              用户名或邮箱
            </label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="input"
              :class="{ 'border-red-300 focus:ring-red-500': errors.username }"
              placeholder="请输入用户名或邮箱"
              autocomplete="username"
            />
            <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <div class="relative">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="input pr-10"
                :class="{ 'border-red-300 focus:ring-red-500': errors.password }"
                placeholder="请输入密码"
                autocomplete="current-password"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="showPassword" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L8.464 8.464M14.121 14.121l1.415 1.415" />
                </svg>
                <svg v-else class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <!-- Remember Me -->
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <input
                id="rememberMe"
                v-model="form.rememberMe"
                type="checkbox"
                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label for="rememberMe" class="ml-2 block text-sm text-gray-700">
                记住我
              </label>
            </div>
            <button
              type="button"
              @click="showForgotPasswordModal = true"
              class="text-sm text-primary-600 hover:text-primary-500 transition-colors duration-200 bg-transparent border-none cursor-pointer"
            >
              忘记密码？
            </button>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading"
            class="btn-primary w-full py-3 text-base font-medium"
            :class="{ 'opacity-50 cursor-not-allowed': loading }"
          >
            <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <!-- Register Link -->
          <div class="text-center">
            <span class="text-gray-600">需要账户？请联系系统管理员</span>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- 忘记密码弹窗 -->
  <div v-if="showForgotPasswordModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click.self="showForgotPasswordModal = false">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
      <div class="mt-3 text-center">
        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100">
          <svg class="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 15.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        <h3 class="text-lg leading-6 font-medium text-gray-900 mt-2">忘记密码</h3>
        <div class="mt-2 px-7 py-3">
          <p class="text-sm text-gray-500">
            如果您忘记了密码，请联系系统超级管理员进行密码重置。
          </p>
          <p class="text-sm text-gray-500 mt-2">
            超级管理员可以在用户管理页面为您重置密码。
          </p>
        </div>
        <div class="items-center px-4 py-3">
          <button 
            @click="showForgotPasswordModal = false"
            class="px-4 py-2 bg-primary-500 text-white text-base font-medium rounded-md w-full shadow-sm hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-300"
          >
            我知道了
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const showForgotPasswordModal = ref(false)

const form = reactive<LoginRequest & { rememberMe: boolean }>({
  username: '',
  password: '',
  rememberMe: false
})

const errors = reactive({
  username: '',
  password: ''
})

function validateForm() {
  errors.username = ''
  errors.password = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名或邮箱'
    return false
  }

  if (!form.password.trim()) {
    errors.password = '请输入密码'
    return false
  }

  if (form.password.length < 6) {
    errors.password = '密码至少需要6位字符'
    return false
  }

  return true
}

async function handleSubmit() {
  if (!validateForm()) return

  loading.value = true
  error.value = ''

  try {
    console.log('🔐 Attempting login...')
    const result = await authStore.login({
      username: form.username,
      password: form.password,
      rememberMe: form.rememberMe
    })

    console.log('📝 Login result:', result)

    if (result.success) {
      console.log('✅ Login successful!')
      
      // Small delay to ensure reactive state updates complete
      await new Promise(resolve => setTimeout(resolve, 100))
      
      console.log('🔍 Auth store state after delay:', {
        isAuthenticated: authStore.isAuthenticated,
        hasToken: !!authStore.token,
        hasUser: !!authStore.user,
        user: authStore.user
      })
      
      const redirect = route.query.redirect as string
      
      // 根据用户角色确定默认跳转页面
      let defaultRoute = '/app/reports' // 默认为主管页面
      if (authStore.user?.role === 'ADMIN') {
        defaultRoute = '/app/all-reports' // 管理员跳转到全局周报页面
      } else if (authStore.user?.role === 'MANAGER') {
        defaultRoute = '/app/reports' // 主管跳转到我的周报页面
      }
      
      const targetRoute = redirect || defaultRoute
      console.log('🎯 Redirect target:', targetRoute, '(User role:', authStore.user?.role, ')')
      
      try {
        await router.push(targetRoute)
        console.log('🚀 Router push completed')
      } catch (routerError) {
        console.error('🚨 Router push failed:', routerError)
      }
    } else {
      error.value = result.message || '登录失败，请检查用户名和密码'
    }
  } catch (err) {
    error.value = '登录时发生错误，请稍后重试'
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
}
</script>