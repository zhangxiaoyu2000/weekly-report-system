<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- 背景遮罩 -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="close"></div>

      <!-- 居中显示的模态框 -->
      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
        <div>
          <div class="text-center">
            <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
              更新基本信息
            </h3>
          </div>

          <form @submit.prevent="handleSubmit" class="mt-6 space-y-4">
            <!-- 用户名 -->
            <div>
              <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                用户名
              </label>
              <input
                id="username"
                v-model="form.username"
                type="text"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
              />
            </div>

            <!-- 邮箱 -->
            <div>
              <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                邮箱
              </label>
              <input
                id="email"
                v-model="form.email"
                type="email"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
              />
            </div>


            <!-- 错误提示 -->
            <div v-if="error" class="text-red-600 text-sm">
              {{ error }}
            </div>

            <!-- 按钮 -->
            <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
              <button
                type="submit"
                :disabled="loading"
                class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:col-start-2 sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="loading" class="flex items-center">
                  <svg class="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  更新中...
                </span>
                <span v-else>更新</span>
              </button>
              <button
                type="button"
                @click="close"
                class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 dark:border-gray-600 shadow-sm px-4 py-2 bg-white dark:bg-gray-700 text-base font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:mt-0 sm:col-start-1 sm:text-sm"
              >
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/services/api'

interface Props {
  isOpen: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  email: ''
})

// 监听 isOpen 变化，重置表单
watch(() => props.isOpen, (isOpen) => {
  if (isOpen && authStore.user) {
    form.username = authStore.user.username || ''
    form.email = authStore.user.email || ''
    error.value = ''
  }
})

function close() {
  emit('close')
}

async function handleSubmit() {
  if (loading.value) return

  loading.value = true
  error.value = ''

  try {
    const response = await api.put<{ success: boolean; message: string; data: any; timestamp: string }>('/users/profile', {
      username: form.username,
      email: form.email
    })

    console.log('Profile update response:', response) // 调试日志

    if (response.success) {
      // Check if new tokens were provided (username changed)
      if (response.data && response.data.tokenRefreshed && response.data.accessToken) {
        console.log('Username changed, updating tokens...')
        
        // Update tokens in auth store
        await authStore.updateTokens(response.data.accessToken, response.data.refreshToken)
        
        // Update user data 
        if (response.data.user) {
          authStore.setUser(response.data.user)
        }
        
        alert('基本信息更新成功，用户名已更改，请重新登录以确保功能正常')
      } else {
        // Normal update without username change
        await authStore.fetchUser()
        alert('基本信息更新成功')
      }
      
      emit('success')
      close()
    } else {
      error.value = response.message || '更新失败'
    }
  } catch (err: any) {
    console.error('Update profile error:', err)
    error.value = err.response?.data?.message || '更新失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>