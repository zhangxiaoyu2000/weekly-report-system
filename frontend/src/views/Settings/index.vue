<template>
    <div class="min-h-screen bg-gray-50">
      <div class="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <!-- 页面标题 -->
        <div class="px-4 py-6 sm:px-0">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">系统设置</h1>
              <p class="mt-2 text-sm text-gray-600">自定义您的系统偏好设置</p>
            </div>
          </div>
        </div>

        <div class="space-y-6">
          <!-- 外观设置 -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">外观设置</h2>
            </div>
            <div class="px-6 py-6">
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">主题模式</label>
                    <p class="text-sm text-gray-500">选择浅色或深色主题</p>
                  </div>
                  <select v-model="settings.theme" @change="applyTheme" class="input w-32">
                    <option value="light">浅色</option>
                    <option value="dark">深色</option>
                    <option value="system">跟随系统</option>
                  </select>
                </div>
                
                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">紧凑布局</label>
                    <p class="text-sm text-gray-500">使用更紧凑的界面布局</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input
                      v-model="settings.compactMode"
                      type="checkbox"
                      class="sr-only peer"
                    />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- 通知设置 -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">通知设置</h2>
            </div>
            <div class="px-6 py-6">
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">桌面通知</label>
                    <p class="text-sm text-gray-500">允许显示桌面通知</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input
                      v-model="settings.desktopNotifications"
                      type="checkbox"
                      class="sr-only peer"
                    />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
                  </label>
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">邮件通知</label>
                    <p class="text-sm text-gray-500">接收重要更新的邮件通知</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input
                      v-model="settings.emailNotifications"
                      type="checkbox"
                      class="sr-only peer"
                    />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
                  </label>
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">项目状态通知</label>
                    <p class="text-sm text-gray-500">项目审核状态变更时通知</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input
                      v-model="settings.projectNotifications"
                      type="checkbox"
                      class="sr-only peer"
                    />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- 数据设置 -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">数据设置</h2>
            </div>
            <div class="px-6 py-6">
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">自动保存</label>
                    <p class="text-sm text-gray-500">编辑表单时自动保存草稿</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input
                      v-model="settings.autoSave"
                      type="checkbox"
                      class="sr-only peer"
                    />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
                  </label>
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">数据导出格式</label>
                    <p class="text-sm text-gray-500">默认的数据导出格式</p>
                  </div>
                  <select v-model="settings.exportFormat" class="input w-32">
                    <option value="xlsx">Excel</option>
                    <option value="csv">CSV</option>
                    <option value="pdf">PDF</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <!-- 安全设置 -->
          <div class="bg-white shadow rounded-lg">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">安全设置</h2>
            </div>
            <div class="px-6 py-6">
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">会话超时</label>
                    <p class="text-sm text-gray-500">空闲多长时间后自动退出</p>
                  </div>
                  <select v-model="settings.sessionTimeout" class="input w-32">
                    <option value="30">30分钟</option>
                    <option value="60">1小时</option>
                    <option value="120">2小时</option>
                    <option value="480">8小时</option>
                  </select>
                </div>

                <div class="flex items-center justify-between">
                  <div>
                    <label class="text-sm font-medium text-gray-700">双因素认证</label>
                    <p class="text-sm text-gray-500">启用双因素身份验证</p>
                  </div>
                  <button class="btn-secondary text-sm">
                    配置
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex justify-end space-x-3">
            <button @click="resetSettings" class="btn-secondary">
              重置默认
            </button>
            <button @click="saveSettings" class="btn-primary">
              保存设置
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
const settings = reactive({
  theme: 'light',
  compactMode: false,
  desktopNotifications: true,
  emailNotifications: true,
  projectNotifications: true,
  autoSave: true,
  exportFormat: 'xlsx',
  sessionTimeout: '60'
})

function applyTheme() {
  if (settings.theme === 'dark') {
    document.documentElement.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else if (settings.theme === 'light') {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  } else {
    // 跟随系统
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    if (prefersDark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    localStorage.setItem('theme', 'system')
  }
}

function loadSettings() {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    settings.theme = savedTheme
  }
  
  const savedSettings = localStorage.getItem('userSettings')
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings)
      Object.assign(settings, parsed)
    } catch (e) {
      console.error('Failed to load settings:', e)
    }
  }
}

function saveSettings() {
  localStorage.setItem('userSettings', JSON.stringify(settings))
  applyTheme()
  alert('设置已保存')
}

function resetSettings() {
  if (confirm('确定要重置所有设置到默认值吗？')) {
    Object.assign(settings, {
      theme: 'light',
      compactMode: false,
      desktopNotifications: true,
      emailNotifications: true,
      projectNotifications: true,
      autoSave: true,
      exportFormat: 'xlsx',
      sessionTimeout: '60'
    })
    localStorage.removeItem('userSettings')
    applyTheme()
    alert('设置已重置')
  }
}

onMounted(() => {
  loadSettings()
})
</script>