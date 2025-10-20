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
          <p class="text-xl text-primary-100 mb-8">加入我们的团队协作平台</p>
          <div class="space-y-4 text-primary-100">
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-3 text-primary-200" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              免费使用所有功能
            </div>
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-3 text-primary-200" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              无限制团队协作
            </div>
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-3 text-primary-200" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
              企业级数据安全
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel - Registration Form -->
    <div class="flex-1 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-20 xl:px-24 bg-white">
      <div class="mx-auto w-full max-w-sm lg:w-96">
        <div>
          <h2 class="text-3xl font-bold text-gray-900 mb-2">创建新账户</h2>
          <p class="text-gray-600 mb-8">填写以下信息创建您的账户</p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Error Display -->
          <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {{ error }}
          </div>

          <!-- Username Field -->
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
              用户名 *
            </label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="input"
              :class="{ 'border-red-300 focus:ring-red-500': errors.username }"
              placeholder="请输入用户名"
              autocomplete="username"
            />
            <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
          </div>

          <!-- Email Field -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
              邮箱地址 *
            </label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              required
              class="input"
              :class="{ 'border-red-300 focus:ring-red-500': errors.email }"
              placeholder="请输入邮箱地址"
              autocomplete="email"
            />
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
          </div>

          <!-- Full Name Field -->
          <div>
            <label for="fullName" class="block text-sm font-medium text-gray-700 mb-2">
              姓名 *
            </label>
            <input
              id="fullName"
              v-model="form.fullName"
              type="text"
              required
              class="input"
              :class="{ 'border-red-300 focus:ring-red-500': errors.fullName }"
              placeholder="请输入真实姓名"
              autocomplete="name"
            />
            <p v-if="errors.fullName" class="mt-1 text-sm text-red-600">{{ errors.fullName }}</p>
          </div>

          <!-- Department Field -->
          <div>
            <label for="department" class="block text-sm font-medium text-gray-700 mb-2">
              部门
            </label>
            <input
              id="department"
              v-model="form.department"
              type="text"
              class="input"
              placeholder="部门名称（可选）"
            />
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              密码 *
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
                autocomplete="new-password"
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
            <p class="mt-1 text-sm text-gray-500">密码至少需要8个字符，包含大写字母、小写字母、数字和特殊字符(@$!%*?&)</p>
          </div>

          <!-- Confirm Password Field -->
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-2">
              确认密码 *
            </label>
            <div class="relative">
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                required
                class="input pr-10"
                :class="{ 'border-red-300 focus:ring-red-500': errors.confirmPassword }"
                placeholder="请再次输入密码"
                autocomplete="new-password"
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <svg v-if="showConfirmPassword" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L8.464 8.464M14.121 14.121l1.415 1.415" />
                </svg>
                <svg v-else class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </button>
            </div>
            <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-600">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Terms Agreement -->
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input
                id="agreeTerms"
                v-model="agreeTerms"
                type="checkbox"
                required
                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
            </div>
            <div class="ml-3 text-sm">
              <label for="agreeTerms" class="text-gray-700">
                我同意
                <a href="#" class="text-primary-600 hover:text-primary-500 underline">服务条款</a>
                和
                <a href="#" class="text-primary-600 hover:text-primary-500 underline">隐私政策</a>
              </label>
            </div>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading || !agreeTerms"
            class="btn-primary w-full py-3 text-base font-medium"
            :class="{ 'opacity-50 cursor-not-allowed': loading || !agreeTerms }"
          >
            <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ loading ? '注册中...' : '立即注册' }}
          </button>

          <!-- Login Link -->
          <div class="text-center">
            <span class="text-gray-600">已经有账户？</span>
            <router-link
              to="/login"
              class="ml-1 text-primary-600 hover:text-primary-500 font-medium transition-colors duration-200"
            >
              立即登录
            </router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { RegisterRequest } from '@/types/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreeTerms = ref(false)

const form = reactive<RegisterRequest>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  fullName: '',
  firstName: '',
  lastName: '',
  department: '',
  role: 'MANAGER', // 默认注册用户为主管角色
  employeeId: '', // 保留但不显示在界面上
  phone: '', // 保留但不显示在界面上
  position: '' // 保留但不显示在界面上
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  fullName: ''
})

function validateForm() {
  // Reset errors
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })

  let isValid = true

  // Username validation
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (form.username.length < 3) {
    errors.username = '用户名至少需要3个字符'
    isValid = false
  }

  // Email validation
  if (!form.email.trim()) {
    errors.email = '请输入邮箱地址'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '请输入有效的邮箱地址'
    isValid = false
  }

  // Full name validation
  if (!form.fullName.trim()) {
    errors.fullName = '请输入姓名'
    isValid = false
  } else if (form.fullName.length < 2) {
    errors.fullName = '姓名至少需要2个字符'
    isValid = false
  }

  // Role is automatically set to MANAGER, no validation needed

  // Password validation - match backend requirements
  if (!form.password.trim()) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 8) {
    errors.password = '密码至少需要8个字符'
    isValid = false
  } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/.test(form.password)) {
    errors.password = '密码必须包含大写字母、小写字母、数字和特殊字符(@$!%*?&)'
    isValid = false
  }

  // Confirm password validation
  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

async function handleSubmit() {
  if (!validateForm()) return
  if (!agreeTerms.value) return

  loading.value = true
  error.value = ''

  try {
    const result = await authStore.register(form)

    if (result.success) {
      // 注册成功后跳转到主管默认页面
      await router.push('/app/reports')
    } else {
      error.value = result.message || '注册失败，请稍后重试'
    }
  } catch (err) {
    error.value = '注册时发生错误，请稍后重试'
    console.error('Register error:', err)
  } finally {
    loading.value = false
  }
}
</script>