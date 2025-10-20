<template>
    <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div class="space-y-6">
          <!-- Page Header -->
          <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
            <div class="flex items-center justify-between">
              <div>
                <h1 class="text-2xl font-bold text-gray-900 dark:text-white">周报面板</h1>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">查看所有已通过的周报</p>
              </div>
              <div class="flex items-center space-x-3">
                <div class="bg-green-100 dark:bg-green-900 px-3 py-1 rounded-full">
                  <span class="text-sm font-medium text-green-800 dark:text-green-200">管理员权限</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Reports Content -->
          <div>
            <!-- Loading State -->
            <div v-if="loading" class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
              <div class="animate-pulse">
                <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/4 mb-4"></div>
                <div class="space-y-3">
                  <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
                  <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4"></div>
                  <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2"></div>
                </div>
              </div>
            </div>

            <!-- Error State -->
            <div v-else-if="error" class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
              <div class="text-center">
                <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-white">加载失败</h3>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ error }}</p>
                <div class="mt-6">
                  <button @click="fetchAllReports" class="btn-primary">重新加载</button>
                </div>
              </div>
            </div>

            <!-- Reports Table -->
            <div v-else class="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
              <div class="px-4 py-5 sm:p-6">
                <div class="flex items-center justify-between mb-6">
                  <h2 class="text-lg font-medium text-gray-900 dark:text-white">
                    已通过周报 ({{ reports.length }} 条)
                  </h2>
                  <button @click="fetchAllReports" :disabled="loading" class="btn-secondary text-sm">
                    <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    刷新
                  </button>
                </div>

                <!-- Empty State -->
                <div v-if="reports.length === 0" class="text-center py-12">
                  <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  <h3 class="mt-2 text-sm font-medium text-gray-900 dark:text-white">暂无已通过周报</h3>
                  <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">系统中还没有任何已通过的周报记录</p>
                </div>

                <!-- Reports Grid -->
                <div v-else class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                  <div
                    v-for="report in reports"
                    :key="report.id"
                    class="border border-gray-200 dark:border-gray-600 rounded-lg p-4 hover:shadow-md transition-shadow duration-200"
                  >
                    <!-- Report Header -->
                    <div class="flex items-start justify-between mb-3">
                      <div class="flex items-center space-x-2">
                        <div class="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center">
                          <span class="text-sm font-medium text-white">
                            {{ report.authorName?.charAt(0) || '?' }}
                          </span>
                        </div>
                        <div>
                          <h3 class="text-sm font-medium text-gray-900 dark:text-white">
                            {{ report.authorName || '未知作者' }}
                          </h3>
                          <p class="text-xs text-gray-500 dark:text-gray-400">
                            已通过
                          </p>
                        </div>
                      </div>
                      <div class="text-xs text-gray-500 dark:text-gray-400">
                        {{ formatDate(report.createdAt || '') }}
                      </div>
                    </div>

                    <!-- Report Title -->
                    <div class="mb-3">
                      <div class="flex items-center text-xs text-gray-500 dark:text-gray-400 mb-1">
                        <svg class="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        标题
                      </div>
                      <p class="text-sm font-medium text-gray-900 dark:text-white truncate">
                        {{ report.title }}
                      </p>
                    </div>

                    <!-- Report Week -->
                    <div class="mb-3">
                      <h4 class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">周期</h4>
                      <p class="text-sm text-gray-700 dark:text-gray-300 line-clamp-2">
                        {{ formatDate(report.reportWeek) }}
                      </p>
                    </div>

                    <!-- Content Preview -->
                    <div>
                      <h4 class="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">内容预览</h4>
                      <p class="text-sm text-gray-700 dark:text-gray-300 line-clamp-3">
                        {{ report.content || '暂无内容' }}
                      </p>
                    </div>

                    <!-- View Details Button -->
                    <div class="mt-4 pt-3 border-t border-gray-100 dark:border-gray-600">
                      <button
                        @click="viewReportDetail(report)"
                        class="w-full text-center text-xs text-primary-600 hover:text-primary-700 dark:text-primary-400 font-medium"
                      >
                        查看详情 →
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { reportService, type WeeklyReport } from '@/services/api'

const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const reports = ref<WeeklyReport[]>([])

const getRoleDisplayName = (role: string | undefined): string => {
  if (!role) return ''
  
  const roleNames: Record<string, string> = {
    'ADMIN': '老板',
    'DEPARTMENT_MANAGER': '部门主管',
    'HR_MANAGER': '人事经理',
    'TEAM_LEADER': '团队负责人',
    'EMPLOYEE': '员工'
  }
  
  return roleNames[role] || role
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function fetchAllReports() {
  loading.value = true
  error.value = ''

  try {
    console.log('加载已通过周报列表...')
    const allReports = await reportService.getListWithTasks()
    
    // 只显示已通过（APPROVED）的周报
    reports.value = allReports.filter(report => 
      report.status === 'APPROVED'
    )
    
    console.log('已通过周报列表加载成功:', reports.value)
  } catch (err: any) {
    console.error('加载已通过周报失败:', err)
    error.value = '加载已通过周报列表失败，请重试'
  } finally {
    loading.value = false
  }
}

function viewReportDetail(report: WeeklyReport) {
  // TODO: 实现周报详情查看
  alert(`查看周报详情:\n\n标题: ${report.title}\n作者: ${report.authorName || '未知'}\n周期: ${formatDate(report.reportWeek)}\n状态: 已通过\n内容: ${report.content || '暂无内容'}`)
}

onMounted(() => {
  fetchAllReports()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>