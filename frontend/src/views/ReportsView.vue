<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">我的周报列表</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">查看和管理您创建的所有周报</p>
          </div>
          <div class="flex items-center space-x-4">
            <router-link to="/app/create-report" class="btn-primary">
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              创建周报
            </router-link>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="mb-6 bg-white rounded-lg shadow p-4">
        <div class="flex flex-col sm:flex-row gap-4">
          <div class="flex-1">
            <input
              v-model="searchTerm"
              type="text"
              placeholder="搜索周报标题..."
              class="input"
            />
          </div>
          <div>
            <select v-model="statusFilter" class="input">
              <option value="">所有状态</option>
              
              
              <option value="AI_ANALYZING">AI分析中</option>
              <option value="AI_REJECTED">AI拒绝</option>
              <option value="PENDING_ADMIN_REVIEW">待管理员审核</option>
              <option value="ADMIN_APPROVED">审核完成</option>
              <option value="ADMIN_REJECTED">管理员拒绝</option>
              <option value="PENDING_SUPER_ADMIN_REVIEW">待超管审核</option>
              <option value="APPROVED">已批准</option>
              <option value="PUBLISHED">已发布</option>
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

      <!-- 周报面板列表 -->
      <div v-else-if="filteredReports.length > 0" class="space-y-6">
        <div
          v-for="report in filteredReports"
          :key="report.id"
          class="bg-white rounded-lg shadow-lg overflow-hidden"
        >
          <!-- 周报标题头部 -->
          <div class="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="bg-white/20 rounded-lg p-2">
                  <svg class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <div>
                  <h2 class="text-xl font-bold text-white">{{ report.title }}</h2>
                  <p class="text-blue-100 text-sm">
                    {{ formatDate(report) }} · {{ getStatusText(report.status) }}
                  </p>
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
                  <!-- AI拒绝状态 - 编辑按钮和强行提交按钮 -->
                  <template v-if="report.status === 'AI_REJECTED'">
                    <button
                      @click="editReport(report.id)"
                      class="text-white hover:text-blue-200 transition-colors"
                      title="编辑周报"
                    >
                      <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                    </button>
                    <button
                      @click="forceSubmitReport(report.id)"
                      class="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
                      title="强行提交到管理员审核"
                    >
                      强行提交
                    </button>
                  </template>
                  
                  <!-- 管理员拒绝状态 - 修改按钮 -->
                  <button
                    v-if="report.status === 'ADMIN_REJECTED'"
                    @click="editReport(report.id)"
                    class="text-white hover:text-blue-200 transition-colors"
                    title="修改周报"
                  >
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                  
                  <!-- AI分析中状态 - 原有编辑按钮 -->
                  <button
                    v-if="report.status === 'AI_ANALYZING'"
                    @click="editReport(report.id)"
                    class="text-white hover:text-blue-200 transition-colors"
                    title="编辑周报"
                  >
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002 2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 任务分类面板 -->
          <div class="p-6">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <!-- 本周汇报 -->
              <div class="space-y-4">
                <h3 class="text-lg font-semibold text-gray-900 flex items-center">
                  <div class="bg-green-100 rounded-lg p-2 mr-3">
                    <svg class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  本周汇报
                </h3>

                <!-- 日常性任务 -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                    <span class="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                    日常性任务
                    <span class="ml-2 text-sm text-gray-500">
                      ({{ getTaskCount(report, 'THIS_WEEK_REPORT', 'ROUTINE') }})
                    </span>
                  </h4>
                  <div class="space-y-2">
                    <div
                      v-for="task in getTasksByType(report, 'THIS_WEEK_REPORT', 'ROUTINE')"
                      :key="task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-blue-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <div class="flex items-center justify-between">
                        <span class="font-medium text-gray-900">{{ task.taskName }}</span>
                        <div class="flex items-center space-x-2">
                          <span v-if="task.progress" class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                            {{ task.progress }}%
                          </span>
                          <span v-if="task.priority > 7" class="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">
                            高优先级
                          </span>
                        </div>
                      </div>
                      <div v-if="task.actualResults" class="mt-2 text-sm text-gray-600">
                        <strong>实际结果：</strong>{{ task.actualResults }}
                      </div>
                    </div>
                    <div v-if="getTaskCount(report, 'THIS_WEEK_REPORT', 'ROUTINE') === 0" 
                         class="text-center py-4 text-gray-500 text-sm">
                      暂无日常性任务
                    </div>
                  </div>
                </div>

                <!-- 发展性任务 -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                    <span class="w-2 h-2 bg-purple-500 rounded-full mr-2"></span>
                    发展性任务
                    <span class="ml-2 text-sm text-gray-500">
                      ({{ getTaskCount(report, 'THIS_WEEK_REPORT', 'DEVELOPMENT') }})
                    </span>
                  </h4>
                  <div class="space-y-2">
                    <div
                      v-for="task in getTasksByType(report, 'THIS_WEEK_REPORT', 'DEVELOPMENT')"
                      :key="task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-purple-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <div class="flex items-center justify-between">
                        <span class="font-medium text-gray-900">{{ task.taskName }}</span>
                        <div class="flex items-center space-x-2">
                          <span v-if="task.progress" class="text-xs bg-purple-100 text-purple-800 px-2 py-1 rounded">
                            {{ task.progress }}%
                          </span>
                          <span v-if="task.priority > 7" class="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">
                            高优先级
                          </span>
                        </div>
                      </div>
                      <div v-if="task.actualResults" class="mt-2 text-sm text-gray-600">
                        <strong>实际结果：</strong>{{ task.actualResults }}
                      </div>
                    </div>
                    <div v-if="getTaskCount(report, 'THIS_WEEK_REPORT', 'DEVELOPMENT') === 0" 
                         class="text-center py-4 text-gray-500 text-sm">
                      暂无发展性任务
                    </div>
                  </div>
                </div>
              </div>

              <!-- 下周规划 -->
              <div class="space-y-4">
                <h3 class="text-lg font-semibold text-gray-900 flex items-center">
                  <div class="bg-orange-100 rounded-lg p-2 mr-3">
                    <svg class="h-5 w-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3a2 2 0 012-2h4a2 2 0 012 2v4m-6 0V3a2 2 0 012-2h4a2 2 0 012 2v4m-6 0h10l2 2v15a2 2 0 01-2 2H5a2 2 0 01-2-2V9l2-2z" />
                    </svg>
                  </div>
                  下周规划
                </h3>

                <!-- 日常性任务 -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                    <span class="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                    日常性任务
                    <span class="ml-2 text-sm text-gray-500">
                      ({{ getTaskCount(report, 'NEXT_WEEK_PLAN', 'ROUTINE') }})
                    </span>
                  </h4>
                  <div class="space-y-2">
                    <div
                      v-for="task in getTasksByType(report, 'NEXT_WEEK_PLAN', 'ROUTINE')"
                      :key="task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-blue-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <div class="flex items-center justify-between">
                        <span class="font-medium text-gray-900">{{ task.taskName }}</span>
                        <div class="flex items-center space-x-2">
                          <span v-if="task.expectedResults" class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                            已规划
                          </span>
                          <span v-if="task.priority > 7" class="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">
                            高优先级
                          </span>
                        </div>
                      </div>
                      <div v-if="task.expectedResults" class="mt-2 text-sm text-gray-600">
                        <strong>预期结果：</strong>{{ task.expectedResults }}
                      </div>
                    </div>
                    <div v-if="getTaskCount(report, 'NEXT_WEEK_PLAN', 'ROUTINE') === 0" 
                         class="text-center py-4 text-gray-500 text-sm">
                      暂无日常性任务
                    </div>
                  </div>
                </div>

                <!-- 发展性任务 -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                    <span class="w-2 h-2 bg-purple-500 rounded-full mr-2"></span>
                    发展性任务
                    <span class="ml-2 text-sm text-gray-500">
                      ({{ getTaskCount(report, 'NEXT_WEEK_PLAN', 'DEVELOPMENT') }})
                    </span>
                  </h4>
                  <div class="space-y-2">
                    <div
                      v-for="task in getTasksByType(report, 'NEXT_WEEK_PLAN', 'DEVELOPMENT')"
                      :key="task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-purple-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <div class="flex items-center justify-between">
                        <span class="font-medium text-gray-900">{{ task.taskName }}</span>
                        <div class="flex items-center space-x-2">
                          <span v-if="task.expectedResults" class="text-xs bg-purple-100 text-purple-800 px-2 py-1 rounded">
                            已规划
                          </span>
                          <span v-if="task.priority > 7" class="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">
                            高优先级
                          </span>
                        </div>
                      </div>
                      <div v-if="task.expectedResults" class="mt-2 text-sm text-gray-600">
                        <strong>预期结果：</strong>{{ task.expectedResults }}
                      </div>
                    </div>
                    <div v-if="getTaskCount(report, 'NEXT_WEEK_PLAN', 'DEVELOPMENT') === 0" 
                         class="text-center py-4 text-gray-500 text-sm">
                      暂无发展性任务
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- AI分析结果 -->
            <div v-if="report.aiAnalysisResult" class="mt-6 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg p-4 border border-blue-200">
              <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                <div class="bg-blue-100 rounded-lg p-2 mr-3">
                  <svg class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                  </svg>
                </div>
                🤖 AI智能分析结果
                <span v-if="report.aiConfidence" class="ml-2 text-sm">
                  <span :class="getAIConfidenceClass(report.aiConfidence)" class="px-2 py-1 rounded-full text-xs font-medium">
                    置信度: {{ Math.round((report.aiConfidence || 0) * 100) }}%
                  </span>
                </span>
              </h4>
              <div class="bg-white rounded-lg p-3 border border-blue-100">
                <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ report.aiAnalysisResult }}</p>
              </div>
              <div v-if="report.aiAnalysisCompletedAt" class="mt-2 text-xs text-gray-500 flex items-center">
                <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                分析完成时间: {{ formatAIAnalysisTime(report.aiAnalysisCompletedAt) }}
              </div>
            </div>

            <!-- 可发展性清单 -->
            <div v-if="report.developmentOpportunities" class="mt-6 bg-green-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-800 mb-2 flex items-center">
                <svg class="h-5 w-5 text-green-600 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                可发展性清单
              </h4>
              <p class="text-gray-700">{{ report.developmentOpportunities }}</p>
            </div>

            <!-- 其他备注 -->
            <div v-if="report.additionalNotes" class="mt-6 bg-yellow-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-800 mb-2 flex items-center">
                <svg class="h-5 w-5 text-yellow-600 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                </svg>
                其他备注
              </h4>
              <p class="text-gray-700">{{ report.additionalNotes }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-12">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">暂无周报</h3>
        <p class="mt-1 text-sm text-gray-500">还没有创建任何周报，点击上方按钮创建您的第一个周报</p>
        <div class="mt-6">
          <router-link to="/app/create-report" class="btn-primary">
            创建周报
          </router-link>
        </div>
      </div>
    </div>

    <!-- 任务详情模态框 -->
    <div v-if="showTaskModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[80vh] overflow-y-auto">
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-medium text-gray-900">任务详情</h3>
            <p class="text-sm text-gray-500">{{ selectedTask?.taskName }}</p>
          </div>
          <button @click="closeTaskModal" class="text-gray-400 hover:text-gray-600">
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="px-6 py-4" v-if="selectedTask">
          <!-- 日常性任务信息 -->
          <div v-if="selectedTask.taskType === 'ROUTINE'" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">任务名称</label>
              <p class="mt-1 text-sm text-gray-900">{{ selectedTask.taskName }}</p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700">负责人</label>
                <p class="mt-1 text-sm text-gray-900">{{ selectedTask.personnelAssignment || '未指定' }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700">时间线</label>
                <p class="mt-1 text-sm text-gray-900">{{ selectedTask.timeline || '未指定' }}</p>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700">量化指标</label>
              <p class="mt-1 text-sm text-gray-900">{{ selectedTask.quantitativeMetrics || '未指定' }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700">预期结果</label>
              <div class="mt-1 p-3 bg-green-50 rounded-lg border border-green-200">
                <p class="text-sm text-gray-700">{{ selectedTask.expectedResults || '未填写预期结果' }}</p>
              </div>
            </div>

            <!-- 实际结果 * -->
            <div v-if="selectedTask.reportSection === 'THIS_WEEK_REPORT'">
              <label class="block text-sm font-medium text-gray-700">实际结果 *</label>
              <div class="mt-1 p-3 bg-blue-50 rounded-lg border border-blue-200">
                <p class="text-sm text-gray-700">{{ selectedTask.actualResults || '暂未填写实际结果' }}</p>
              </div>
            </div>

            <!-- 结果差异分析 * -->
            <div v-if="selectedTask.reportSection === 'THIS_WEEK_REPORT'">
              <label class="block text-sm font-medium text-gray-700">结果差异分析 *</label>
              <div class="mt-1 p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                <p class="text-sm text-gray-700">{{ selectedTask.resultDifferenceAnalysis || '暂未填写差异分析' }}</p>
              </div>
            </div>
          </div>

          <!-- 发展性任务信息 -->
          <div v-else-if="selectedTask.taskType === 'DEVELOPMENT'" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">任务名称</label>
              <p class="mt-1 text-sm text-gray-900">{{ selectedTask.taskName }}</p>
            </div>

            <!-- 🚀 项目信息 -->
            <div class="bg-purple-50 rounded-lg p-4 border border-purple-200">
              <h4 class="text-sm font-medium text-purple-800 mb-3">🚀 项目信息</h4>
              <div class="space-y-2 text-sm">
                <div>
                  <span class="font-medium text-gray-700">项目名称：</span>
                  <span class="text-gray-600">{{ selectedTask.simpleProject?.projectName || '未关联项目' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">项目内容：</span>
                  <span class="text-gray-600">{{ selectedTask.simpleProject?.projectContent || '未填写项目内容' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">项目成员：</span>
                  <span class="text-gray-600">{{ selectedTask.simpleProject?.projectMembers || '未指定项目成员' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">预期结果：</span>
                  <span class="text-gray-600">{{ selectedTask.simpleProject?.expectedResults || '未填写预期结果' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">时间线：</span>
                  <span class="text-gray-600">{{ selectedTask.simpleProject?.timeline || '未制定时间线' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">止损点：</span>
                  <span class="text-gray-600">{{ selectedTask.simpleProject?.stopLoss || '未设置止损点' }}</span>
                </div>
              </div>
            </div>

            <!-- 🎯 阶段信息 -->
            <div class="bg-indigo-50 rounded-lg p-4 border border-indigo-200">
              <h4 class="text-sm font-medium text-indigo-800 mb-3">🎯 阶段信息</h4>
              <div class="space-y-2 text-sm">
                <div>
                  <span class="font-medium text-gray-700">阶段名称：</span>
                  <span class="text-gray-600">{{ selectedTask.projectPhase?.phaseName || '未关联阶段' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">阶段描述：</span>
                  <span class="text-gray-600">{{ selectedTask.projectPhase?.phaseDescription || '未填写阶段描述' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">负责成员：</span>
                  <span class="text-gray-600">{{ selectedTask.projectPhase?.assignedMembers || '未指定负责成员' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">时间安排：</span>
                  <span class="text-gray-600">{{ selectedTask.projectPhase?.timeline || '未制定时间安排' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">预期结果：</span>
                  <span class="text-gray-600">{{ selectedTask.projectPhase?.estimatedResults || '未填写预期结果' }}</span>
                </div>
              </div>
            </div>

            <!-- 实际结果 * -->
            <div v-if="selectedTask.reportSection === 'THIS_WEEK_REPORT'">
              <label class="block text-sm font-medium text-gray-700">实际结果 *</label>
              <div class="mt-1 p-3 bg-blue-50 rounded-lg border border-blue-200">
                <p class="text-sm text-gray-700">{{ selectedTask.actualResults || '暂未填写实际结果' }}</p>
              </div>
            </div>

            <!-- 结果差异分析 * -->
            <div v-if="selectedTask.reportSection === 'THIS_WEEK_REPORT'">
              <label class="block text-sm font-medium text-gray-700">结果差异分析 *</label>
              <div class="mt-1 p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                <p class="text-sm text-gray-700">{{ selectedTask.resultDifferenceAnalysis || '暂未填写差异分析' }}</p>
              </div>
            </div>

            <!-- 预期结果（下周规划任务） -->
            <div v-if="selectedTask.expectedResults && selectedTask.reportSection === 'NEXT_WEEK_PLAN'">
              <label class="block text-sm font-medium text-gray-700">预期结果</label>
              <div class="mt-1 p-3 bg-green-50 rounded-lg border border-green-200">
                <p class="text-sm text-gray-700">{{ selectedTask.expectedResults }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { reportService, type WeeklyReport } from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const searchTerm = ref('')
const statusFilter = ref('')
const error = ref('')

// 周报数据
const reports = ref<WeeklyReport[]>([])

// 任务详情模态框
const showTaskModal = ref(false)
const selectedTask = ref<any>(null)

const filteredReports = computed(() => {
  let filtered = reports.value

  if (searchTerm.value.trim()) {
    filtered = filtered.filter(report =>
      report.title.toLowerCase().includes(searchTerm.value.toLowerCase())
    )
  }

  if (statusFilter.value) {
    filtered = filtered.filter(report => report.status === statusFilter.value)
  }

  return filtered.sort((a, b) => {
    const dateA = new Date(a.reportWeek || a.createdAt || 0)
    const dateB = new Date(b.reportWeek || b.createdAt || 0)
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

function getStatusClass(status: string) {
  const classes: Record<string, string> = {
    
    SUBMITTED: 'bg-blue-100 text-blue-800',
    AI_ANALYZING: 'bg-purple-100 text-purple-800',
    AI_REJECTED: 'bg-red-100 text-red-800',
    PENDING_ADMIN_REVIEW: 'bg-orange-100 text-orange-800',
    ADMIN_APPROVED: 'bg-green-100 text-green-800',
    ADMIN_REJECTED: 'bg-red-100 text-red-800',
    PENDING_SUPER_ADMIN_REVIEW: 'bg-indigo-100 text-indigo-800',
    SUPER_ADMIN_REJECTED: 'bg-red-100 text-red-800',
    APPROVED: 'bg-green-100 text-green-800',
    PUBLISHED: 'bg-emerald-100 text-emerald-800',
    REJECTED: 'bg-red-100 text-red-800' // 兼容旧状态
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function getStatusText(status: string) {
  const statusTexts: Record<string, string> = {
    
    
    AI_ANALYZING: 'AI分析中',
    AI_REJECTED: 'AI拒绝',
    PENDING_ADMIN_REVIEW: '待管理员审核',
    ADMIN_APPROVED: '审核完成',
    ADMIN_REJECTED: '管理员拒绝',
    PENDING_SUPER_ADMIN_REVIEW: '待超管审核',
    SUPER_ADMIN_REJECTED: '超管拒绝',
    APPROVED: '已批准',
    PUBLISHED: '已发布',
    REJECTED: '已拒绝' // 兼容旧状态
  }
  return statusTexts[status] || status
}

function getAIConfidenceClass(confidence: number) {
  if (confidence >= 0.8) {
    return 'bg-green-100 text-green-800'
  } else if (confidence >= 0.6) {
    return 'bg-yellow-100 text-yellow-800'
  } else {
    return 'bg-red-100 text-red-800'
  }
}

function formatAIAnalysisTime(timestamp: string) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function formatDate(report: any) {
  // Use the year and weekNumber fields from the backend response
  if (report.year && report.weekNumber && report.reportWeek) {
    const reportDate = new Date(report.reportWeek)
    const monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', 
                        '七月', '八月', '九月', '十月', '十一月', '十二月']
    const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    
    const month = monthNames[reportDate.getMonth()]
    const dayOfWeek = dayNames[reportDate.getDay()]
    
    // Calculate which week of the month this is
    const firstDayOfMonth = new Date(reportDate.getFullYear(), reportDate.getMonth(), 1)
    const weekOfMonth = Math.ceil((reportDate.getDate() + firstDayOfMonth.getDay()) / 7)
    
    return `${month}第${weekOfMonth}周 (${dayOfWeek})`
  }
  
  // Fallback to original format if fields are missing
  return new Date(report.reportWeek || report.weekStart || Date.now()).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function viewReport(reportId: number) {
  // 跳转到周报详情页面
  router.push(`/app/reports/${reportId}`)
}

function editReport(reportId: number) {
  // 跳转到编辑周报页面
  router.push(`/app/reports/${reportId}/edit`)
}

async function forceSubmitReport(reportId: number) {
  // 强行提交周报到管理员审核
  try {
    console.log('强行提交周报:', reportId)
    
    // 确认对话框
    if (!confirm('确定要强行提交此周报到管理员审核吗？')) {
      return
    }
    
    await reportService.forceSubmit(reportId)
    
    // 提示成功并刷新列表
    alert('周报已强行提交到管理员审核')
    await loadReports()
    
  } catch (err: any) {
    console.error('强行提交周报失败:', err)
    const errorMessage = err.message || '强行提交失败，请重试'
    alert(`强行提交失败：${errorMessage}`)
  }
}

function openTaskModal(task: any) {
  selectedTask.value = task
  showTaskModal.value = true
}

function closeTaskModal() {
  showTaskModal.value = false
  selectedTask.value = null
}

async function loadReports() {
  loading.value = true
  error.value = ''
  try {
    console.log('加载周报列表...')
    const reportsWithTasks = await reportService.getListWithTasks()
    reports.value = reportsWithTasks
    console.log('周报列表加载成功:', reportsWithTasks)
  } catch (err: any) {
    console.error('加载周报失败:', err)
    error.value = '加载周报列表失败，请重试'
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
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-blue-500 focus:border-blue-500;
}

.btn-primary {
  @apply bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out inline-flex items-center;
}

.btn-secondary {
  @apply bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out;
}
</style>