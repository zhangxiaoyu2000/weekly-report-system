<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">周报审核</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">管理员周报审核界面 - 三级审批工作流</p>
          </div>
        </div>
      </div>

      <!-- Tab 切换 -->
      <div class="mb-6 bg-white rounded-lg shadow p-1 inline-flex">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          :class="[
            'px-4 py-2 rounded-md font-medium text-sm transition-all duration-200',
            activeTab === tab.key 
              ? 'bg-blue-600 text-white shadow-sm' 
              : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
          ]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="ml-2 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-blue-100 bg-blue-800 rounded-full">
            {{ tab.count }}
          </span>
        </button>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredReports.length === 0" class="text-center py-12">
        <div class="text-6xl mb-4">📄</div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无{{ getTabLabel(activeTab) }}周报</h3>
        <p class="text-gray-500">当前没有需要处理的周报</p>
      </div>
      
      <!-- 周报列表 -->
      <div v-else class="space-y-6">
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
                    {{ report.reportWeek }} · 提交人: {{ report.author?.fullName || report.author?.username || report.authorName || '未知' }}
                  </p>
                </div>
              </div>
              <div class="flex items-center space-x-3">
                <span
                  :class="getStatusClass(report.approvalStatus || report.status)"
                  class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium"
                >
                  {{ getStatusText(report.approvalStatus || report.status) }}
                </span>
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
                      :key="task.task_id || task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-blue-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <div class="flex items-center justify-between">
                        <span class="font-medium text-gray-900">{{ task.taskDetails?.taskName || task.taskName || '任务名称' }}</span>
                      </div>
                      <div v-if="task.actual_result || task.actualResult" class="mt-2 text-sm text-gray-600">
                        <strong>实际结果：</strong>{{ task.actual_result || task.actualResult || '' }}
                      </div>
                      <div v-if="task.analysisofResultDifferences || task.AnalysisofResultDifferences || task.resultDifferenceAnalysis" class="mt-1 text-sm text-gray-500">
                        <strong>差异分析：</strong>{{ task.analysisofResultDifferences || task.AnalysisofResultDifferences || task.resultDifferenceAnalysis || '' }}
                      </div>
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
                      :key="task.project_id || task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-purple-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <div class="flex items-center justify-between">
                        <span class="font-medium text-gray-900">{{ task.projectDetails?.projectName || task.projectDetails?.name || task.projectName || '项目名称' }}</span>
                      </div>
                      <div v-if="task.phaseDetails?.phaseName" class="text-sm text-gray-600">
                        <strong>阶段：</strong>{{ task.phaseDetails.phaseName }}
                      </div>
                      <div v-if="task.actual_result || task.actualResult" class="mt-2 text-sm text-gray-600">
                        <strong>实际结果：</strong>{{ task.actual_result || task.actualResult || '' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 下周规划 -->
              <div class="space-y-4">
                <h3 class="text-lg font-semibold text-gray-900 flex items-center">
                  <div class="bg-blue-100 rounded-lg p-2 mr-3">
                    <svg class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  下周规划
                </h3>

                <!-- 日常性任务规划 -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                    <span class="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                    日常性任务规划
                    <span class="ml-2 text-sm text-gray-500">
                      ({{ getTaskCount(report, 'NEXT_WEEK_PLAN', 'ROUTINE') }})
                    </span>
                  </h4>
                  <div class="space-y-2">
                    <div
                      v-for="task in getTasksByType(report, 'NEXT_WEEK_PLAN', 'ROUTINE')"
                      :key="task.task_id || task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-blue-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <span class="font-medium text-gray-900">{{ task.taskDetails?.taskName || task.taskName || '任务名称' }}</span>
                    </div>
                  </div>
                </div>

                <!-- 发展性任务规划 -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <h4 class="font-medium text-gray-800 mb-3 flex items-center">
                    <span class="w-2 h-2 bg-purple-500 rounded-full mr-2"></span>
                    发展性任务规划
                    <span class="ml-2 text-sm text-gray-500">
                      ({{ getTaskCount(report, 'NEXT_WEEK_PLAN', 'DEVELOPMENT') }})
                    </span>
                  </h4>
                  <div class="space-y-2">
                    <div
                      v-for="task in getTasksByType(report, 'NEXT_WEEK_PLAN', 'DEVELOPMENT')"
                      :key="task.project_id || task.id"
                      @click="openTaskModal(task)"
                      class="bg-white rounded p-3 border-l-4 border-purple-500 cursor-pointer hover:bg-gray-50 transition-colors"
                    >
                      <span class="font-medium text-gray-900">{{ task.projectDetails?.projectName || task.projectDetails?.name || task.projectName || '项目名称' }}</span>
                      <div v-if="task.phaseDetails?.phaseName" class="text-sm text-gray-600">
                        <strong>阶段：</strong>{{ task.phaseDetails.phaseName }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 额外备注 -->
            <div v-if="report.additionalNotes" class="mt-6 bg-yellow-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-800 mb-2 flex items-center">
                <svg class="h-5 w-5 text-yellow-600 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                额外备注
              </h4>
              <p class="text-gray-700">{{ report.additionalNotes }}</p>
            </div>

            <!-- 发展机会 -->
            <div v-if="report.developmentOpportunities" class="mt-6 bg-green-50 rounded-lg p-4">
              <h4 class="font-medium text-gray-800 mb-2 flex items-center">
                <svg class="h-5 w-5 text-green-600 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                发展机会
              </h4>
              <p class="text-gray-700">{{ report.developmentOpportunities }}</p>
            </div>
          </div>

          <!-- AI分析结果 -->
          <div v-if="report.aiAnalysisResult" class="bg-blue-50 border-l-4 border-blue-400 p-4 mb-6">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div class="ml-3">
                <h4 class="text-sm font-medium text-blue-800">🤖 AI分析结果</h4>
                <div class="mt-2 text-sm text-blue-700">
                  {{ report.aiAnalysisResult }}
                </div>
                <div v-if="report.aiConfidence" class="mt-2 text-xs text-blue-600">
                  置信度: {{ Math.round(report.aiConfidence * 100) }}%
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮区域 -->
          <div class="bg-gray-50 px-6 py-4 flex items-center justify-between">
            <div class="text-sm text-gray-500">
              提交时间: {{ formatDate(report.createdAt) }}
            </div>
            
            <div v-if="canApprove(report.approvalStatus || report.status)" class="flex items-center space-x-3">
              <button 
                @click="approveReport(report.id, true)"
                class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-colors"
              >
                <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                批准通过
              </button>
              <button 
                @click="openRejectModal(report)"
                class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors"
              >
                <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                拒绝申请
              </button>
            </div>
            
            <div v-else-if="(report.approvalStatus || report.status) === 'ADMIN_APPROVED'" class="flex items-center text-green-600">
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span class="font-medium">管理员审核通过</span>
            </div>
            
            <div v-else-if="(report.approvalStatus || report.status) === 'ADMIN_REJECTED'" class="flex items-center text-red-600">
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span class="font-medium">管理员审核拒绝</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 任务详情模态框 -->
    <div v-if="showTaskModal" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[80vh] overflow-y-auto">
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-medium text-gray-900">任务详情</h3>
            <p class="text-sm text-gray-500">{{ selectedTask?.taskDetails?.taskName || selectedTask?.taskName || selectedTask?.projectDetails?.projectName || selectedTask?.projectDetails?.name || selectedTask?.projectName || '任务名称' }}</p>
          </div>
          <button @click="closeTaskModal" class="text-gray-400 hover:text-gray-600">
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="px-6 py-4" v-if="selectedTask">
          <!-- 日常性任务信息 -->
          <div v-if="isRoutineTask(selectedTask)" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">任务名称</label>
              <p class="mt-1 text-sm text-gray-900">{{ selectedTask.taskDetails?.taskName || selectedTask.taskName || '未指定' }}</p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700">负责人</label>
                <p class="mt-1 text-sm text-gray-900">{{ selectedTask.taskDetails?.personnelAssignment || selectedTask.personnelAssignment || '未指定' }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700">时间线</label>
                <p class="mt-1 text-sm text-gray-900">{{ selectedTask.taskDetails?.timeline || selectedTask.timeline || '未指定' }}</p>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700">量化指标</label>
              <p class="mt-1 text-sm text-gray-900">{{ selectedTask.taskDetails?.quantitativeMetrics || selectedTask.quantitativeMetrics || '未指定' }}</p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700">预期结果</label>
              <div class="mt-1 p-3 bg-green-50 rounded-lg border border-green-200">
                <p class="text-sm text-gray-700">{{ selectedTask.taskDetails?.expectedResults || selectedTask.expectedResults || '未填写预期结果' }}</p>
              </div>
            </div>

            <!-- 实际结果 * -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">实际结果 *</label>
              <div class="mt-1 p-3 bg-blue-50 rounded-lg border border-blue-200">
                <p class="text-sm text-gray-700">{{ selectedTask.actual_result || selectedTask.actualResult || selectedTask.actualResults || '暂未填写实际结果' }}</p>
              </div>
            </div>

            <!-- 结果差异分析 * -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">结果差异分析 *</label>
              <div class="mt-1 p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                <p class="text-sm text-gray-700">{{ selectedTask.analysisofResultDifferences || selectedTask.AnalysisofResultDifferences || selectedTask.resultDifferenceAnalysis || '暂未填写差异分析' }}</p>
              </div>
            </div>
          </div>

          <!-- 发展性任务信息 -->
          <div v-else-if="isDevelopmentTask(selectedTask)" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">任务名称</label>
              <p class="mt-1 text-sm text-gray-900">{{ selectedTask.projectDetails?.projectName || selectedTask.projectDetails?.name || selectedTask.projectName || '未指定' }}</p>
            </div>

            <!-- 🚀 项目信息 -->
            <div class="bg-purple-50 rounded-lg p-4 border border-purple-200">
              <h4 class="text-sm font-medium text-purple-800 mb-3">🚀 项目信息</h4>
              <div class="space-y-2 text-sm">
                <div>
                  <span class="font-medium text-gray-700">项目名称：</span>
                  <span class="text-gray-600">{{ selectedTask.projectDetails?.projectName || selectedTask.projectDetails?.name || selectedTask.projectName || '未关联项目' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">项目内容：</span>
                  <span class="text-gray-600">{{ selectedTask.projectDetails?.projectContent || selectedTask.projectDetails?.content || selectedTask.projectContent || '未填写项目内容' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">项目成员：</span>
                  <span class="text-gray-600">{{ selectedTask.projectDetails?.projectMembers || selectedTask.projectDetails?.members || selectedTask.projectMembers || '未指定项目成员' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">预期结果：</span>
                  <span class="text-gray-600">{{ selectedTask.projectDetails?.expectedResults || selectedTask.expectedResults || '未填写预期结果' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">时间线：</span>
                  <span class="text-gray-600">{{ selectedTask.projectDetails?.timeline || selectedTask.timeline || '未制定时间线' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">止损点：</span>
                  <span class="text-gray-600">{{ selectedTask.projectDetails?.stopLoss || selectedTask.stopLoss || '未设置止损点' }}</span>
                </div>
              </div>
            </div>

            <!-- 🎯 阶段信息 -->
            <div v-if="selectedTask.phaseDetails" class="bg-indigo-50 rounded-lg p-4 border border-indigo-200">
              <h4 class="text-sm font-medium text-indigo-800 mb-3">🎯 阶段信息</h4>
              <div class="space-y-2 text-sm">
                <div>
                  <span class="font-medium text-gray-700">阶段名称：</span>
                  <span class="text-gray-600">{{ selectedTask.phaseDetails?.phaseName || '未关联阶段' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">阶段描述：</span>
                  <span class="text-gray-600">{{ selectedTask.phaseDetails?.phaseDescription || '未填写阶段描述' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">负责成员：</span>
                  <span class="text-gray-600">{{ selectedTask.phaseDetails?.assignedMembers || '未指定负责成员' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">时间安排：</span>
                  <span class="text-gray-600">{{ selectedTask.phaseDetails?.timeline || '未制定时间安排' }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-700">预期结果：</span>
                  <span class="text-gray-600">{{ selectedTask.phaseDetails?.estimatedResults || '未填写预期结果' }}</span>
                </div>
              </div>
            </div>

            <!-- 实际结果 * -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">实际结果 *</label>
              <div class="mt-1 p-3 bg-blue-50 rounded-lg border border-blue-200">
                <p class="text-sm text-gray-700">{{ selectedTask.actual_result || selectedTask.actualResult || selectedTask.actualResults || '暂未填写实际结果' }}</p>
              </div>
            </div>

            <!-- 结果差异分析 * -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">结果差异分析 *</label>
              <div class="mt-1 p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                <p class="text-sm text-gray-700">{{ selectedTask.analysisofResultDifferences || selectedTask.AnalysisofResultDifferences || selectedTask.resultDifferenceAnalysis || '暂未填写差异分析' }}</p>
              </div>
            </div>

            <!-- 预期结果（下周规划任务） -->
            <div v-if="!isThisWeekTask(selectedTask) && selectedTask.expectedResults">
              <label class="block text-sm font-medium text-gray-700">预期结果</label>
              <div class="mt-1 p-3 bg-green-50 rounded-lg border border-green-200">
                <p class="text-sm text-gray-700">{{ selectedTask.expectedResults }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝理由模态框 -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>拒绝周报</h3>
          <button @click="closeRejectModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="report-info">
            <p><strong>周报标题:</strong> {{ selectedReport?.title }}</p>
            <p><strong>提交人:</strong> {{ selectedReport?.author?.fullName || selectedReport?.author?.username || selectedReport?.authorName || '未知' }}</p>
          </div>
          
          <div class="reject-reason">
            <label for="rejectReason">请填写拒绝理由 <span class="required">*</span></label>
            <textarea
              id="rejectReason"
              v-model="rejectReason"
              rows="4"
              placeholder="请详细说明拒绝该周报的原因..."
              class="reason-input"
              required
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeRejectModal" class="cancel-btn">取消</button>
          <button 
            @click="confirmReject" 
            :disabled="!rejectReason.trim() || rejecting"
            class="confirm-reject-btn"
          >
            {{ rejecting ? '处理中...' : '确认拒绝' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { marked } from 'marked'
import { weeklyReportAPI } from '@/services/api'

const authStore = useAuthStore()

const activeTab = ref('pending')
const allReports = ref([])

// 拒绝模态框相关状态
const showRejectModal = ref(false)
const selectedReport = ref(null)
const rejectReason = ref('')
const rejecting = ref(false)

// 任务详情模态框相关状态
const showTaskModal = ref(false)
const selectedTask = ref(null)

const tabs = computed(() => {
  const reports = allReports.value
  return [
    { 
      key: 'pending', 
      label: '待审批', 
      count: reports.filter(r => r.approvalStatus === 'ADMIN_REVIEWING' || r.status === 'ADMIN_REVIEWING').length 
    },
    { 
      key: 'approved', 
      label: '已通过', 
      count: reports.filter(r => r.approvalStatus === 'ADMIN_APPROVED' || r.status === 'ADMIN_APPROVED').length 
    },
    { 
      key: 'rejected', 
      label: '已拒绝', 
      count: reports.filter(r => r.approvalStatus === 'ADMIN_REJECTED' || r.status === 'ADMIN_REJECTED').length 
    }
  ]
})

const filteredReports = computed(() => {
  console.log('过滤周报数据:', { activeTab: activeTab.value, allReports: allReports.value })
  switch (activeTab.value) {
    case 'pending':
      return allReports.value.filter(r => r.approvalStatus === 'ADMIN_REVIEWING' || r.status === 'ADMIN_REVIEWING')
    case 'approved':
      return allReports.value.filter(r => r.approvalStatus === 'ADMIN_APPROVED' || r.status === 'ADMIN_APPROVED')
    case 'rejected':
      return allReports.value.filter(r => r.approvalStatus === 'ADMIN_REJECTED' || r.status === 'ADMIN_REJECTED')
    default:
      return allReports.value.filter(r => r.approvalStatus === 'ADMIN_REVIEWING' || r.status === 'ADMIN_REVIEWING')
  }
})

const getTabLabel = (tabKey: string) => {
  const tabMap = {
    'pending': '待审批',
    'approved': '已通过', 
    'rejected': '已拒绝'
  }
  return tabMap[tabKey] || ''
}

const getStatusText = (status: string) => {
  const statusMap = {
    'AI_ANALYZING': 'AI分析中',
    'AI_APPROVED': 'AI分析通过',
    'AI_REJECTED': 'AI分析不通过',
    'ADMIN_REVIEWING': '管理员审核中',
    'ADMIN_APPROVED': '管理员审核通过',
    'ADMIN_REJECTED': '管理员审核拒绝',
    'REJECTED': '已拒绝',
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'REVIEWED': '已审核',
    'PUBLISHED': '已发布',
    'APPROVED': '已批准'
  }
  return statusMap[status] || status
}

const getStatusClass = (status: string) => {
  const statusMap: Record<string, string> = {
    'AI_ANALYZING': 'bg-yellow-100 text-yellow-800',
    'AI_APPROVED': 'bg-green-100 text-green-800', 
    'AI_REJECTED': 'bg-red-100 text-red-800',
    'ADMIN_REVIEWING': 'bg-blue-100 text-blue-800',
    'ADMIN_APPROVED': 'bg-green-100 text-green-800',
    'ADMIN_REJECTED': 'bg-red-100 text-red-800',
    'SUPER_ADMIN_REVIEWING': 'bg-purple-100 text-purple-800',
    'SUPER_ADMIN_APPROVED': 'bg-green-100 text-green-800',
    'SUPER_ADMIN_REJECTED': 'bg-red-100 text-red-800',
    'APPROVED': 'bg-green-100 text-green-800',
    'REJECTED': 'bg-red-100 text-red-800'
  }
  return statusMap[status] || 'bg-gray-100 text-gray-800'
}

const canApprove = (status: string) => {
  return status === 'ADMIN_REVIEWING'
}

const hasManagerApproval = (report: any) => {
  return report.managerReviewer && !report.status.includes('MANAGER_REJECTED')
}

const hasAIAnalysis = (report: any) => {
  return report.aiAnalysisResult || report.aiAnalyzedAt
}

const hasAdminApproval = (report: any) => {
  return report.adminReviewer && !report.status.includes('ADMIN_REJECTED')
}

const hasSuperAdminApproval = (report: any) => {
  return report.status === 'APPROVED'
}

const getManagerStatus = (report: any) => {
  if (report.status === 'MANAGER_REJECTED') return '已拒绝'
  if (report.managerReviewer) return '已通过'
  if (report.status === 'PENDING_MGR_REVIEW') return '待审核'
  return '待处理'
}

const getAIStatus = (report: any) => {
  if (report.status === 'AI_REJECTED') return '分析不通过'
  if (report.aiAnalysisResult) return '分析完成'
  if (report.status === 'PENDING_AI_ANALYSIS') return '分析中'
  return '待分析'
}

const getAdminStatus = (report: any) => {
  if (report.status === 'ADMIN_REJECTED') return '已拒绝'
  if (report.adminReviewer) return '已通过'
  if (report.status === 'PENDING_ADMIN_REVIEW') return '待审核'
  return '待处理'
}

const getSuperAdminStatus = (report: any) => {
  if (report.status === 'SUPER_ADMIN_REJECTED') return '已拒绝'
  if (report.status === 'APPROVED') return '已通过'
  if (report.status === 'PENDING_SA_REVIEW') return '待审核'
  return '待处理'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取指定类型的任务数量
const getTaskCount = (report: any, section: string, taskType: string) => {
  if (!report?.content) return 0
  
  const tasks = getTasksByType(report, section, taskType)
  return tasks.length
}

// 获取指定类型的任务列表
const getTasksByType = (report: any, section: string, taskType: string) => {
  if (!report?.content) return []
  
  let tasks = []
  
  if (section === 'THIS_WEEK_REPORT') {
    if (taskType === 'ROUTINE') {
      // 修复字段名称不匹配问题：支持后端返回的驼峰格式和前端期望的下划线格式
      tasks = report.content.routineTasks || report.content.routine_tasks || report.content.Routine_tasks || []
    } else if (taskType === 'DEVELOPMENT') {
      // 修复字段名称不匹配问题：支持后端返回的驼峰格式和前端期望的下划线格式
      tasks = report.content.developmentalTasks || report.content.developmental_tasks || report.content.Developmental_tasks || []
    }
  } else if (section === 'NEXT_WEEK_PLAN') {
    if (report.nextWeekPlan) {
      if (taskType === 'ROUTINE') {
        // 修复字段名称不匹配问题：支持后端返回的驼峰格式和前端期望的下划线格式
        tasks = report.nextWeekPlan.routineTasks || report.nextWeekPlan.routine_tasks || report.nextWeekPlan.Routine_tasks || []
      } else if (taskType === 'DEVELOPMENT') {
        // 修复字段名称不匹配问题：支持后端返回的驼峰格式和前端期望的下划线格式
        tasks = report.nextWeekPlan.developmentalTasks || report.nextWeekPlan.developmental_tasks || report.nextWeekPlan.Developmental_tasks || []
      }
    }
  }
  
  return Array.isArray(tasks) ? tasks : []
}

const approveReport = async (reportId: number, approved: boolean, customComment?: string) => {
  try {
    let response;
    
    if (approved) {
      // 管理员审核通过
      response = await fetch(`/api/weekly-reports/${reportId}/admin-approve`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authStore.token}`,
          'Content-Type': 'application/json'
        }
      });
    } else {
      // 管理员拒绝
      const reason = customComment || '周报内容需要修改，请重新提交';
      response = await fetch(`/api/weekly-reports/${reportId}/reject`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authStore.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reason })
      });
    }

    if (response.ok) {
      const result = await response.json();
      alert(result.message || (approved ? '周报审批通过' : '周报已拒绝'));
      loadReports();
    } else {
      const error = await response.json();
      alert('操作失败: ' + (error.message || '请稍后重试'));
    }
  } catch (error) {
    console.error('审批操作失败:', error);
    alert('操作失败，请稍后重试');
  }
}

// 打开拒绝模态框
const openRejectModal = (report: any) => {
  selectedReport.value = report
  rejectReason.value = ''
  showRejectModal.value = true
}

// 关闭拒绝模态框
const closeRejectModal = () => {
  showRejectModal.value = false
  selectedReport.value = null
  rejectReason.value = ''
  rejecting.value = false
}

// 确认拒绝周报
const confirmReject = async () => {
  if (!selectedReport.value || !rejectReason.value.trim()) {
    alert('请填写拒绝理由')
    return
  }
  
  rejecting.value = true
  try {
    await approveReport(selectedReport.value.id, false, rejectReason.value.trim())
    closeRejectModal()
  } catch (error) {
    console.error('拒绝周报失败:', error)
  } finally {
    rejecting.value = false
  }
}

// 任务模态框函数
const openTaskModal = (task: any) => {
  selectedTask.value = task
  showTaskModal.value = true
}

const closeTaskModal = () => {
  showTaskModal.value = false
  selectedTask.value = null
}

// 判断任务类型和时间的辅助函数
const isRoutineTask = (task: any) => {
  // 检查是否为日常任务
  return task.task_id || task.taskDetails || task.taskName
}

const isDevelopmentTask = (task: any) => {
  // 检查是否为发展任务
  return task.project_id || task.projectDetails || task.projectName
}

const isThisWeekTask = (task: any) => {
  // 判断是否是本周汇报任务（有实际结果和差异分析）
  return task.actual_result || task.actualResult || task.actualResults || task.analysisofResultDifferences || task.AnalysisofResultDifferences || task.resultDifferenceAnalysis
}

const loadReports = async (status?: string) => {
  try {
    // 根据传入的状态参数或当前激活的tab调用API
    const targetStatus = status || getStatusByTab(activeTab.value)
    const result = await weeklyReportAPI.list(targetStatus)
    
    if (result.success && result.data) {
      // 直接使用API返回的数据，不进行额外解析
      allReports.value = result.data.sort((a, b) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      )
      console.log('加载的周报数据:', { status: targetStatus, count: allReports.value.length })
    } else {
      console.error('API返回错误:', result.message)
    }
  } catch (error) {
    console.error('加载周报列表失败:', error)
  }
}

// 获取tab对应的状态参数
const getStatusByTab = (tabKey: string) => {
  const statusMap = {
    'pending': 'ADMIN_REVIEWING',
    'approved': 'ADMIN_APPROVED', 
    'rejected': 'ADMIN_REJECTED'
  }
  return statusMap[tabKey]
}

// 渲染Markdown内容
const renderMarkdown = (content: string) => {
  // 处理非字符串类型的内容
  if (!content || typeof content !== 'string') {
    return content || '无'
  }
  
  if (content.trim() === '' || content === '无') {
    return content || '无'
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

// 监听activeTab变化，自动重新加载数据
watch(activeTab, (newTab) => {
  loadReports()
})

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.admin-reports {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  margin-bottom: 24px;
}

.header h1 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.subtitle {
  color: #6b7280;
  font-size: 1rem;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.tab-button {
  padding: 12px 24px;
  border: none;
  background: none;
  cursor: pointer;
  font-weight: 500;
  color: #6b7280;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-button.active {
  color: #3b82f6;
  border-bottom-color: #3b82f6;
}

.count {
  background: #ef4444;
  color: white;
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 0.75rem;
  min-width: 20px;
  text-align: center;
}

.no-data {
  text-align: center;
  padding: 80px 20px;
  color: #6b7280;
}

.no-data-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.no-data h3 {
  font-size: 1.25rem;
  color: #374151;
  margin-bottom: 8px;
}

.reports-grid {
  display: grid;
  gap: 20px;
}

.report-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s;
}

.report-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.card-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status.pending {
  background: #fef3c7;
  color: #92400e;
}

.status.approved {
  background: #d1fae5;
  color: #065f46;
}

.status.rejected {
  background: #fee2e2;
  color: #991b1b;
}

.card-content {
  margin-bottom: 20px;
}

.info-section p {
  margin: 12px 0;
  color: #374151;
  line-height: 1.5;
}

.ai-analysis {
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
}

.ai-analysis h4 {
  color: #0369a1;
  margin: 0 0 12px 0;
  font-size: 1rem;
}

.ai-content p {
  color: #1e40af !important;
  margin: 0;
  font-weight: 500;
}

.approval-progress {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
}

.approval-progress h4 {
  color: #374151;
  margin: 0 0 16px 0;
  font-size: 1rem;
}

.progress-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-step {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
}

.progress-step.active {
  background: #f0f9ff;
  border-radius: 6px;
  padding: 12px;
}

.step-marker {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  flex-shrink: 0;
}

.progress-step.completed .step-marker {
  background: #10b981;
  color: white;
}

.progress-step.active .step-marker {
  background: #3b82f6;
  color: white;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.step-title {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.step-status {
  font-size: 0.75rem;
  color: #6b7280;
}

.step-time {
  font-size: 0.75rem;
  color: #9ca3af;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.creator-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.creator {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.create-time {
  color: #6b7280;
  font-size: 0.75rem;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.approve-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
  font-size: 0.875rem;
}

.approve-btn:hover {
  background: #059669;
}

.reject-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
  font-size: 0.875rem;
}

.reject-btn:hover {
  background: #dc2626;
}

.pending-info, .approval-info, .rejection-info {
  display: flex;
  align-items: center;
}

.pending-text {
  color: #f59e0b;
  font-weight: 500;
  font-size: 0.875rem;
}

.approved-text {
  color: #10b981;
  font-weight: 500;
  font-size: 0.875rem;
}

.rejected-text {
  color: #ef4444;
  font-weight: 500;
  font-size: 0.875rem;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e5;
}

.modal-header h3 {
  margin: 0;
  color: #d32f2f;
  font-size: 1.125rem;
  font-weight: 600;
  flex: 1;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #000;
}

.modal-body {
  padding: 24px;
}

.report-info {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.report-info p {
  margin: 4px 0;
  font-size: 0.875rem;
}

.reject-reason {
  margin-bottom: 20px;
}

.reject-reason label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
}

.required {
  color: #d32f2f;
}

.reason-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.875rem;
  resize: vertical;
  box-sizing: border-box;
}

.reason-input:focus {
  outline: none;
  border-color: #d32f2f;
  box-shadow: 0 0 0 2px rgba(211, 47, 47, 0.2);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e5e5;
}

.cancel-btn {
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.confirm-reject-btn {
  padding: 8px 16px;
  background: #d32f2f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
}

.confirm-reject-btn:hover:not(:disabled) {
  background: #b71c1c;
}

.confirm-reject-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 内容项样式 */
.content-item {
  margin-bottom: 16px;
}

.content-item > p {
  margin-bottom: 8px !important;
  font-weight: 600;
}

/* Markdown内容样式 */
.markdown-content {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  margin-top: 4px;
}

.markdown-content h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 1rem 0 0.75rem 0;
  color: #1f2937;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.markdown-content h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0.875rem 0 0.5rem 0;
  color: #374151;
}

.markdown-content h3 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0.75rem 0 0.5rem 0;
  color: #4b5563;
}

.markdown-content h4 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0.625rem 0 0.375rem 0;
  color: #6b7280;
}

.markdown-content ul, .markdown-content ol {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}

.markdown-content li {
  margin: 0.25rem 0;
}

.markdown-content p {
  margin: 0.5rem 0;
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
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  padding: 0.75rem;
  overflow-x: auto;
  margin: 0.75rem 0;
}

.markdown-content blockquote {
  border-left: 4px solid #d1d5db;
  padding-left: 1rem;
  margin: 0.75rem 0;
  color: #6b7280;
  font-style: italic;
}
</style>