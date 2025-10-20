<template>
    <div class="min-h-screen bg-gray-50">
      <div class="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <!-- 页面标题 -->
        <div class="px-4 py-6 sm:px-0">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">个人资料</h1>
              <p class="mt-2 text-sm text-gray-600">管理您的账户信息和个人设置</p>
            </div>
          </div>
        </div>

        <div class="space-y-6">
          <!-- 基本信息 -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">基本信息</h2>
            </div>
            <div class="px-6 py-6">
              <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">用户名</label>
                  <input
                    v-model="form.username"
                    type="text"
                    :disabled="!editing"
                    class="input"
                    :class="{ 'bg-gray-50': !editing }"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">邮箱</label>
                  <input
                    v-model="form.email"
                    type="email"
                    :disabled="!editing"
                    class="input"
                    :class="{ 'bg-gray-50': !editing }"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">全名</label>
                  <input
                    v-model="form.fullName"
                    type="text"
                    :disabled="!editing"
                    class="input"
                    :class="{ 'bg-gray-50': !editing }"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">角色</label>
                  <input
                    :value="getRoleDisplayName(form.role)"
                    type="text"
                    disabled
                    class="input bg-gray-50"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 密码修改 -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">修改密码</h2>
            </div>
            <div class="px-6 py-6">
              <div class="max-w-md space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">当前密码</label>
                  <input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    class="input"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">新密码</label>
                  <input
                    v-model="passwordForm.newPassword"
                    type="password"
                    class="input"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">确认新密码</label>
                  <input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    class="input"
                  />
                </div>
                <button @click="changePassword" class="btn-primary">
                  更新密码
                </button>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex justify-end space-x-3">
            <button
              v-if="!editing"
              @click="editing = true"
              class="btn-secondary"
            >
              编辑资料
            </button>
            <template v-else>
              <button @click="cancelEdit" class="btn-secondary">
                取消
              </button>
              <button @click="saveProfile" class="btn-primary">
                保存更改
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const editing = ref(false)

const form = reactive({
  username: '',
  email: '',
  fullName: '',
  role: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function getRoleDisplayName(role: string): string {
  const roleNames: Record<string, string> = {
    'ADMIN': '管理员',
    'MANAGER': '主管'
  }
  return roleNames[role] || role
}

function loadProfile() {
  if (authStore.user) {
    form.username = authStore.user.username || ''
    form.email = authStore.user.email || ''
    form.fullName = authStore.user.fullName || ''
    form.role = authStore.user.role || ''
  }
}

function cancelEdit() {
  editing.value = false
  loadProfile()
}

function saveProfile() {
  // TODO: 实现保存个人资料的API调用
  console.log('保存个人资料:', form)
  editing.value = false
  alert('个人资料已保存（功能待实现）')
}

function changePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    alert('新密码和确认密码不匹配')
    return
  }
  // TODO: 实现修改密码的API调用
  console.log('修改密码')
  alert('密码已更新（功能待实现）')
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

onMounted(() => {
  loadProfile()
})
</script>