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
              修改密码
            </h3>
          </div>

          <form @submit.prevent="handleSubmit" class="mt-6 space-y-4">
            <!-- 当前密码 -->
            <div>
              <label for="currentPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                当前密码
              </label>
              <input
                id="currentPassword"
                v-model="form.currentPassword"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
              />
            </div>

            <!-- 新密码 -->
            <div>
              <label for="newPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                新密码
              </label>
              <input
                id="newPassword"
                v-model="form.newPassword"
                type="password"
                required
                minlength="8"
                class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
              />
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                密码至少8位字符
              </p>
            </div>

            <!-- 确认新密码 -->
            <div>
              <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
                确认新密码
              </label>
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                type="password"
                required
                class="mt-1 block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:text-white"
                :class="{ 'border-red-500': form.confirmPassword && form.newPassword !== form.confirmPassword }"
              />
              <p v-if="form.confirmPassword && form.newPassword !== form.confirmPassword" class="mt-1 text-sm text-red-600">
                两次输入的密码不一致
              </p>
            </div>

            <!-- 错误提示 -->
            <div v-if="error" class="text-red-600 text-sm">
              {{ error }}
            </div>

            <!-- 按钮 -->
            <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
              <button
                type="submit"
                :disabled="loading || !isFormValid"
                class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:col-start-2 sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="loading" class="flex items-center">
                  <svg class="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  修改中...
                </span>
                <span v-else>修改密码</span>
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
import { ref, reactive, computed, watch } from 'vue'
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

const loading = ref(false)
const error = ref('')

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证
const isFormValid = computed(() => {
  return form.currentPassword && 
         form.newPassword && 
         form.confirmPassword && 
         form.newPassword === form.confirmPassword &&
         form.newPassword.length >= 8
})

// 监听 isOpen 变化，重置表单
watch(() => props.isOpen, (isOpen) => {
  if (isOpen) {
    form.currentPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    error.value = ''
  }
})

function close() {
  emit('close')
}

async function handleSubmit() {
  if (loading.value || !isFormValid.value) return

  if (form.newPassword !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await api.post<{ success: boolean; message: string; data: any; timestamp: string }>('/auth/change-password', {
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
      confirmNewPassword: form.confirmPassword  // 修正字段名
    })

    console.log('Password change response:', response) // 调试日志

    if (response.success) {
      emit('success')
      close()
      
      // 显示成功提示
      alert('密码修改成功')
    } else {
      error.value = response.message || '修改失败'
    }
  } catch (err: any) {
    console.error('Change password error:', err)
    error.value = err.response?.data?.message || '修改失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>