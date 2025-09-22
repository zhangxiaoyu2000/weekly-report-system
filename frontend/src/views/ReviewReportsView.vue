<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">审核周报</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">审核已提交的周报，进行通过或拒绝操作</p>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="mb-6 bg-white dark:bg-gray-800 rounded-lg shadow p-4">
        <div class="flex flex-col sm:flex-row gap-4">
          <div class="flex-1">
            <input
              v-model="searchTerm"
              type="text"
              placeholder="搜索周报标题或作者..."
              class="input"
            />
          </div>
          <div>
            <select v-model="statusFilter" class="input">
              <option value="">所有状态</option>
              <option value="SUBMITTED">待审核</option>
              <option value="APPROVED">已通过</option>
              <option value="REJECTED">已拒绝</option>
            </select>
          </div>
          <div>
            <button @click="loadReports" class="btn-secondary">
              刷新
            </button>
          </div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="mb-6 grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="bg-yellow-100 rounded-lg p-3 mr-4">
              <svg class="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">待审核</p>
              <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ getCountByStatus('SUBMITTED') }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="bg-green-100 rounded-lg p-3 mr-4">
              <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">已通过</p>
              <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ getCountByStatus('APPROVED') }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="bg-red-100 rounded-lg p-3 mr-4">
              <svg class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">已拒绝</p>
              <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ getCountByStatus('REJECTED') }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="bg-blue-100 rounded-lg p-3 mr-4">
              <svg class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500 dark:text-gray-400">总数</p>
              <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ filteredReports.length }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="mb-6 bg-red-50 border border-red-200 rounded-md p-4">
        <div class="flex">
          <svg class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="ml-3">
            <p class="text-sm text-red-700">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-8">
        <div class="inline-flex items-center">
          <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          加载中...
        </div>
      </div>

      <!-- 周报列表 -->
      <div v-else-if="filteredReports.length > 0" class="space-y-6">
        <div
          v-for="report in filteredReports"
          :key="report.id"
          class="bg-white dark:bg-gray-800 rounded-lg shadow-lg overflow-hidden"
        >
          <!-- 周报标题头部 -->
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="bg-blue-100 rounded-lg p-2">
                  <svg class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ report.title }}</h3>
                  <div class="flex items-center space-x-4 mt-1 text-sm text-gray-500 dark:text-gray-300">
                    <span>作者: {{ report.authorName }}</span>
                    <span>{{ formatDate(report.reportWeek) }}</span>
                    <span v-if="report.submittedAt">提交时间: {{ formatDateTime(report.submittedAt) }}</span>
                  </div>
                </div>
              </div>
              <div class="flex items-center space-x-3">
                <span
                  :class="getStatusClass(report.status)"
                  class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium"
                >
                  {{ getStatusText(report.status) }}
                </span>
                <div class="flex items-center space-x-2">
                  <button
                    @click="viewReport(report)"
                    class="text-gray-600 hover:text-gray-800 transition-colors"
                    title="查看详情"
                  >
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 周报内容预览 -->
          <div class="px-6 py-4">
            <div class="text-gray-700 dark:text-gray-200 mb-4">
              <div class="line-clamp-3">{{ report.content || '暂无内容' }}</div>
            </div>

            <!-- 任务统计 -->
            <div v-if="report.tasks && report.tasks.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
              <div class="bg-blue-50 rounded-lg p-3">
                <div class="text-xs text-blue-600 font-medium">本周日常任务</div>
                <div class="text-lg font-semibold text-blue-900">
                  {{ getTaskCount(report, 'THIS_WEEK_REPORT', 'ROUTINE') }}
                </div>
              </div>
              <div class="bg-purple-50 rounded-lg p-3">
                <div class="text-xs text-purple-600 font-medium">本周发展任务</div>
                <div class="text-lg font-semibold text-purple-900">
                  {{ getTaskCount(report, 'THIS_WEEK_REPORT', 'DEVELOPMENT') }}
                </div>
              </div>
              <div class="bg-green-50 rounded-lg p-3">
                <div class="text-xs text-green-600 font-medium">下周日常任务</div>
                <div class="text-lg font-semibold text-green-900">
                  {{ getTaskCount(report, 'NEXT_WEEK_PLAN', 'ROUTINE') }}
                </div>
              </div>
              <div class="bg-orange-50 rounded-lg p-3">
                <div class="text-xs text-orange-600 font-medium">下周发展任务</div>
                <div class="text-lg font-semibold text-orange-900">
                  {{ getTaskCount(report, 'NEXT_WEEK_PLAN', 'DEVELOPMENT') }}
                </div>
              </div>
            </div>

            <!-- 审核操作按钮 -->
            <div v-if="report.status === 'SUBMITTED'" class="flex items-center justify-end space-x-3 pt-4 border-t border-gray-200">
              <button
                @click="openReviewModal(report, 'reject')"
                :disabled="reviewLoading"
                class="inline-flex items-center px-4 py-2 border border-red-300 text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50"
              >
                <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                拒绝
              </button>
              <button
                @click="openReviewModal(report, 'approve')"
                :disabled="reviewLoading"
                class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50"
              >
                <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                通过
              </button>
            </div>

            <!-- 审核意见显示 -->
            <div v-else-if="report.reviewComment" class="mt-4 p-3 bg-gray-50 rounded-lg">
              <div class="text-sm text-gray-600 mb-1">审核意见:</div>
              <div class="text-gray-800">{{ report.reviewComment }}</div>
              <div v-if="report.reviewedAt" class="text-xs text-gray-500 mt-2">
                审核时间: {{ formatDateTime(report.reviewedAt) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-12">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">暂无周报需要审核</h3>
        <p class="mt-1 text-sm text-gray-500">当前没有符合条件的周报</p>
      </div>
    </div>

    <!-- 审核模态框 -->
    <div v-if="showReviewModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">
            {{ reviewAction === 'approve' ? '通过' : '拒绝' }}周报
          </h3>
          <p class="text-sm text-gray-500 mt-1">{{ selectedReport?.title }}</p>
        </div>
        
        <div class="px-6 py-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            审核意见{{ reviewAction === 'reject' ? ' (必填)' : ' (选填)' }}:
          </label>
          <textarea
            v-model="reviewComment"
            rows="4"
            class="input resize-none"
            :placeholder="reviewAction === 'approve' ? '请输入通过理由或建议...' : '请说明拒绝原因...'"
            :required="reviewAction === 'reject'"
          ></textarea>
        </div>

        <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-end space-x-3">
          <button
            @click="closeReviewModal"
            :disabled="reviewLoading"
            class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
          >
            取消
          </button>
          <button
            @click="submitReview"
            :disabled="reviewLoading || (reviewAction === 'reject' && !reviewComment.trim())"
            :class="reviewAction === 'approve' 
              ? 'bg-green-600 hover:bg-green-700 focus:ring-green-500' 
              : 'bg-red-600 hover:bg-red-700 focus:ring-red-500'"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50"
          >
            <svg v-if="reviewLoading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ reviewAction === 'approve' ? '通过' : '拒绝' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 周报详情模态框 -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ selectedReport?.title }}</h3>
            <p class="text-sm text-gray-500 dark:text-gray-300">作者: {{ selectedReport?.authorName }} · {{ formatDate(selectedReport?.reportWeek) }}</p>
          </div>
          <button @click="closeDetailModal" class="text-gray-400 hover:text-gray-600">
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="px-6 py-4" v-if="selectedReport">
          <!-- 周报内容 -->
          <div class="mb-6">
            <h4 class="font-medium text-gray-900 dark:text-white mb-2">周报内容</h4>
            <div class="bg-gray-50 rounded-lg p-4 text-gray-700 markdown-content">
              <div v-html="renderMarkdown(selectedReport.content || '暂无内容')"></div>
            </div>
          </div>

          <!-- 任务详情 -->
          <div v-if="selectedReport.tasks && selectedReport.tasks.length > 0" class="space-y-6">
            <!-- 本周汇报 -->
            <div>
              <h4 class="font-medium text-gray-900 dark:text-white mb-3">本周汇报</h4>
              <div class="space-y-4">
                <div v-for="task in getTasksByType(selectedReport, 'THIS_WEEK_REPORT', 'ROUTINE')" 
                     :key="task.id" 
                     class="border border-blue-200 rounded-lg p-4 bg-blue-50">
                  <div class="font-medium text-blue-900">{{ task.taskName }}</div>
                  <div class="text-sm text-blue-700 mt-1">类型: 日常性任务</div>
                  <div v-if="task.actualResults" class="text-sm text-gray-600 mt-2">
                    实际结果: {{ task.actualResults }}
                  </div>
                </div>
                <div v-for="task in getTasksByType(selectedReport, 'THIS_WEEK_REPORT', 'DEVELOPMENT')" 
                     :key="task.id" 
                     class="border border-purple-200 rounded-lg p-4 bg-purple-50">
                  <div class="font-medium text-purple-900">{{ task.taskName }}</div>
                  <div class="text-sm text-purple-700 mt-1">类型: 发展性任务</div>
                  <div v-if="task.actualResults" class="text-sm text-gray-600 mt-2">
                    实际结果: {{ task.actualResults }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 下周规划 -->
            <div>
              <h4 class="font-medium text-gray-900 dark:text-white mb-3">下周规划</h4>
              <div class="space-y-4">
                <div v-for="task in getTasksByType(selectedReport, 'NEXT_WEEK_PLAN', 'ROUTINE')" 
                     :key="task.id" 
                     class="border border-green-200 rounded-lg p-4 bg-green-50">
                  <div class="font-medium text-green-900">{{ task.taskName }}</div>
                  <div class="text-sm text-green-700 mt-1">类型: 日常性任务</div>
                  <div v-if="task.expectedResults" class="text-sm text-gray-600 mt-2">
                    预期结果: {{ task.expectedResults }}
                  </div>
                </div>
                <div v-for="task in getTasksByType(selectedReport, 'NEXT_WEEK_PLAN', 'DEVELOPMENT')" 
                     :key="task.id" 
                     class="border border-orange-200 rounded-lg p-4 bg-orange-50">
                  <div class="font-medium text-orange-900">{{ task.taskName }}</div>
                  <div class="text-sm text-orange-700 mt-1">类型: 发展性任务</div>
                  <div v-if="task.expectedResults" class="text-sm text-gray-600 mt-2">
                    预期结果: {{ task.expectedResults }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 其他备注 -->
          <div v-if="selectedReport.additionalNotes" class="mt-6">
            <h4 class="font-medium text-gray-900 dark:text-white mb-2">其他备注</h4>
            <div class="bg-yellow-50 rounded-lg p-4 text-gray-700">
              {{ selectedReport.additionalNotes }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { reportService, weeklyReportAPI, type WeeklyReport } from '@/services/api'
import { marked } from 'marked'

const authStore = useAuthStore()

const loading = ref(false)
const reviewLoading = ref(false)
const searchTerm = ref('')
const statusFilter = ref('')
const error = ref('')

// 周报数据
const reports = ref<WeeklyReport[]>([])

// 模态框相关
const showReviewModal = ref(false)
const showDetailModal = ref(false)
const selectedReport = ref<WeeklyReport | null>(null)
const reviewAction = ref<'approve' | 'reject'>('approve')
const reviewComment = ref('')

const filteredReports = computed(() => {
  let filtered = reports.value

  if (searchTerm.value.trim()) {
    const term = searchTerm.value.toLowerCase()
    filtered = filtered.filter(report =>
      report.title.toLowerCase().includes(term) ||
      (report.authorName && report.authorName.toLowerCase().includes(term))
    )
  }

  if (statusFilter.value) {
    filtered = filtered.filter(report => report.status === statusFilter.value)
  }

  return filtered.sort((a, b) => {
    const dateA = new Date(a.submittedAt || a.createdAt || 0)
    const dateB = new Date(b.submittedAt || b.createdAt || 0)
    return dateB.getTime() - dateA.getTime()
  })
})

function getTasksByType(report: any, reportSection: string, taskType: string) {
  return report.tasks?.filter((task: any) =>
    task.reportSection === reportSection && task.taskType === taskType
  ) || []
}

function getTaskCount(report: any, reportSection: string, taskType: string) {
  return getTasksByType(report, reportSection, taskType).length
}

function getCountByStatus(status: string) {
  return reports.value.filter(report => report.status === status).length
}

function getStatusClass(status: string) {
  const classes: Record<string, string> = {
    
    
    APPROVED: 'bg-green-100 text-green-800',
    REJECTED: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function getStatusText(status: string) {
  const statusTexts: Record<string, string> = {
    
    SUBMITTED: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return statusTexts[status] || status
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function formatDateTime(date: string) {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 渲染Markdown内容
function renderMarkdown(content: string) {
  if (!content || content.trim() === '暂无内容') {
    return '暂无内容'
  }
  
  // 配置marked选项
  marked.setOptions({
    breaks: true, // 支持换行
    gfm: true, // 支持GitHub风格markdown
  })
  
  try {
    return marked.parse(content)
  } catch (error) {
    console.error('Markdown渲染失败:', error)
    return content // 如果渲染失败，返回原始文本
  }
}

function viewReport(report: WeeklyReport) {
  selectedReport.value = report
  showDetailModal.value = true
}

function closeDetailModal() {
  showDetailModal.value = false
  selectedReport.value = null
}

function openReviewModal(report: WeeklyReport, action: 'approve' | 'reject') {
  selectedReport.value = report
  reviewAction.value = action
  reviewComment.value = ''
  showReviewModal.value = true
}

function closeReviewModal() {
  showReviewModal.value = false
  selectedReport.value = null
  reviewComment.value = ''
}

async function submitReview() {
  if (!selectedReport.value) return
  
  if (reviewAction.value === 'reject' && !reviewComment.value.trim()) {
    error.value = '拒绝时必须填写拒绝原因'
    return
  }

  reviewLoading.value = true
  error.value = ''

  try {
    console.log('提交审核:', {
      reportId: selectedReport.value.id,
      action: reviewAction.value,
      comment: reviewComment.value
    })

    const response = await weeklyReportAPI.review(
      selectedReport.value.id!,
      reviewAction.value,
      reviewComment.value.trim() || undefined
    )

    if (response.success) {
      // 更新本地数据
      const index = reports.value.findIndex(r => r.id === selectedReport.value!.id)
      if (index !== -1) {
        reports.value[index] = response.data
      }

      // 显示成功消息
      const successMessage = document.createElement('div')
      successMessage.className = 'fixed top-4 right-4 bg-green-500 text-white px-4 py-2 rounded shadow-lg z-50'
      successMessage.textContent = `周报${reviewAction.value === 'approve' ? '通过' : '拒绝'}成功！`
      document.body.appendChild(successMessage)
      setTimeout(() => {
        document.body.removeChild(successMessage)
      }, 3000)

      closeReviewModal()
    } else {
      error.value = response.message
    }

  } catch (err: any) {
    console.error('审核失败:', err)
    error.value = err.message || '审核失败'
  } finally {
    reviewLoading.value = false
  }
}

async function loadReports() {
  loading.value = true
  error.value = ''
  try {
    console.log('加载审核周报列表...')
    const reportsWithTasks = await reportService.getListWithTasks()
    
    // 过滤出需要审核或已审核的周报（排除草稿）
    reports.value = reportsWithTasks.filter(report => 
      report.status !== 'DRAFT'
    )
    
    console.log('审核周报列表加载成功:', reports.value)
  } catch (err: any) {
    console.error('加载审核周报失败:', err)
    error.value = '加载审核周报列表失败，请重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.input {
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm;
}

.btn-secondary {
  @apply bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Markdown内容样式 */
.markdown-content {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
}

.markdown-content h1 {
  font-size: 1.875rem;
  font-weight: 700;
  margin: 1.5rem 0 1rem 0;
  color: #1f2937;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.markdown-content h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1.25rem 0 0.75rem 0;
  color: #374151;
}

.markdown-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 1rem 0 0.5rem 0;
  color: #4b5563;
}

.markdown-content h4 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0.875rem 0 0.5rem 0;
  color: #6b7280;
}

.markdown-content ul, .markdown-content ol {
  margin: 0.75rem 0;
  padding-left: 1.5rem;
}

.markdown-content li {
  margin: 0.25rem 0;
}

.markdown-content p {
  margin: 0.75rem 0;
  color: #374151;
}

.markdown-content strong {
  font-weight: 600;
  color: #1f2937;
}

.markdown-content em {
  font-style: italic;
  color: #6b7280;
}

.markdown-content code {
  background: #f3f4f6;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', 'Courier New', monospace;
  font-size: 0.875rem;
}

.markdown-content pre {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  padding: 1rem;
  overflow-x: auto;
  margin: 1rem 0;
}

.markdown-content blockquote {
  border-left: 4px solid #d1d5db;
  padding-left: 1rem;
  margin: 1rem 0;
  color: #6b7280;
  font-style: italic;
}

/* 深色模式支持 */
:global(.dark) .markdown-content h1,
:global(.dark) .markdown-content h2,
:global(.dark) .markdown-content h3,
:global(.dark) .markdown-content h4 {
  color: #f9fafb;
}

:global(.dark) .markdown-content p {
  color: #e5e7eb;
}

:global(.dark) .markdown-content strong {
  color: #f9fafb;
}

:global(.dark) .markdown-content code {
  background: #374151;
  color: #f9fafb;
}

:global(.dark) .markdown-content pre {
  background: #1f2937;
  border-color: #374151;
}
</style>