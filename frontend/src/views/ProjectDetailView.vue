<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面头部 -->
      <div class="mb-6">
        <button 
          @click="$router.back()" 
          class="inline-flex items-center text-gray-600 hover:text-gray-800 dark:text-gray-300 dark:hover:text-white mb-4"
        >
          <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          返回项目列表
        </button>
        
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">项目详情</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">查看项目的详细信息和审批进度</p>
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

      <!-- 项目详情 -->
      <div v-else-if="project" class="space-y-6">
        <!-- 项目基本信息卡片 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ project.name }}</h2>
              <span :class="['status-badge', getStatusClass(project.approvalStatus)]">
                {{ getStatusText(project) }}
              </span>
            </div>
          </div>
          
          <div class="px-6 py-4 space-y-4">
            <div>
              <h3 class="text-sm font-medium text-gray-900 mb-2">项目内容</h3>
              <p class="text-gray-700">{{ project.description }}</p>
            </div>
            
            <div>
              <h3 class="text-sm font-medium text-gray-900 mb-2">项目成员</h3>
              <p class="text-gray-700">{{ project.members }}</p>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <h3 class="text-sm font-medium text-gray-900 mb-2">预期结果</h3>
                <p class="text-gray-700">{{ project.expectedResults }}</p>
              </div>
              <div>
                <h3 class="text-sm font-medium text-gray-900 mb-2">时间计划</h3>
                <p class="text-gray-700">{{ project.timeline }}</p>
              </div>
            </div>
            
            <div>
              <h3 class="text-sm font-medium text-gray-900 mb-2">止损点</h3>
              <p class="text-gray-700">{{ project.stopLoss }}</p>
            </div>
          </div>
        </div>

        <!-- 项目阶段性任务 -->
        <div v-if="projectPhases.length > 0" class="bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white flex items-center">
              <span class="mr-2">📋</span>
              项目阶段性任务
            </h2>
          </div>
          <div class="px-6 py-4">
            <div class="space-y-4">
              <div 
                v-for="phase in projectPhases" 
                :key="phase.id"
                class="border border-gray-200 rounded-lg p-4"
              >
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center">
                    <span class="text-lg font-medium text-gray-900">
                      {{ phase.phaseOrder ? `${phase.phaseOrder}. ` : '' }}{{ phase.phaseName }}
                    </span>
                    <span :class="['ml-3 px-2 py-1 text-xs font-medium rounded-full', getPhaseStatusClass(phase.status)]">
                      {{ getPhaseStatusText(phase.status) }}
                    </span>
                  </div>
                </div>
                
                <div v-if="phase.phaseDescription" class="mb-3">
                  <p class="text-gray-700">{{ phase.phaseDescription }}</p>
                </div>
                
                <div class="grid grid-cols-2 gap-4 text-sm">
                  <div v-if="phase.assignedMembers">
                    <span class="font-medium text-gray-900">负责人员:</span>
                    <span class="ml-2 text-gray-600">{{ phase.assignedMembers }}</span>
                  </div>
                  <div v-if="phase.timeline">
                    <span class="font-medium text-gray-900">时间线:</span>
                    <span class="ml-2 text-gray-600">{{ phase.timeline }}</span>
                  </div>
                  <div v-if="phase.estimatedResults">
                    <span class="font-medium text-gray-900">预期结果:</span>
                    <span class="ml-2 text-gray-600">{{ phase.estimatedResults }}</span>
                  </div>
                </div>
                
                <div v-if="phase.startDate || phase.endDate" class="mt-3 flex gap-4 text-sm text-gray-500">
                  <div v-if="phase.startDate">
                    <span class="font-medium">开始时间:</span>
                    <span class="ml-1">{{ formatDate(phase.startDate) }}</span>
                  </div>
                  <div v-if="phase.endDate">
                    <span class="font-medium">结束时间:</span>
                    <span class="ml-1">{{ formatDate(phase.endDate) }}</span>
                  </div>
                  <div v-if="phase.completionDate">
                    <span class="font-medium">完成时间:</span>
                    <span class="ml-1">{{ formatDate(phase.completionDate) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- AI分析结果 -->
        <div v-if="aiAnalysisResult || project.approvalStatus !== 'AI_ANALYZING'" class="bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white flex items-center">
              <span class="mr-2">🤖</span>
              AI分析结果
            </h2>
          </div>
          <div class="px-6 py-4">
            <!-- 显示完整AI分析结果 -->
            <div v-if="project.aiAnalysisResult" class="space-y-4">
              <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div class="mb-3">
                  <h4 class="font-medium text-blue-900 mb-2">分析内容</h4>
                  <div class="text-blue-800 prose prose-blue max-w-none" v-html="renderMarkdown(project.aiAnalysisResult.result)"></div>
                </div>
                <div class="grid grid-cols-2 gap-4 text-sm text-blue-700">
                  <div v-if="project.aiAnalysisResult.confidence">
                    <span class="font-medium">置信度:</span>
                    <span :class="['ml-2 font-medium', getConfidenceClass(project.aiAnalysisResult.confidence)]">
                      {{ Math.round(project.aiAnalysisResult.confidence * 100) }}%
                      <span v-if="project.aiAnalysisResult.confidence >= 0.7" class="ml-1">✅</span>
                      <span v-else class="ml-1">❌</span>
                    </span>
                  </div>
                  <div v-if="project.aiAnalysisResult.modelVersion">
                    <span class="font-medium">分析模型:</span>
                    <span class="ml-2">{{ project.aiAnalysisResult.modelVersion }}</span>
                  </div>
                  <div v-if="project.aiAnalysisResult.createdAt">
                    <span class="font-medium">分析时间:</span>
                    <span class="ml-2">{{ formatDate(project.aiAnalysisResult.createdAt) }}</span>
                  </div>
                  <div v-if="project.aiAnalysisResult.status">
                    <span class="font-medium">状态:</span>
                    <span class="ml-2">{{ getAnalysisStatusText(project.aiAnalysisResult.status) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <!-- 简化状态显示 -->
            <div v-else-if="project.approvalStatus === 'AI_APPROVED'" class="bg-green-50 border border-green-200 rounded-lg p-4">
              <p class="text-green-800">✅ AI分析已完成，项目评估通过</p>
            </div>
            <div v-else-if="project.approvalStatus === 'AI_REJECTED'" class="bg-red-50 border border-red-200 rounded-lg p-4">
              <p class="text-red-800">❌ AI分析未通过</p>
              <p v-if="project.rejectionReason" class="text-red-700 mt-2 text-sm">{{ project.rejectionReason }}</p>
            </div>
            <div v-else-if="project.approvalStatus === 'AI_ANALYZING'" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <p class="text-yellow-800">⏳ AI分析进行中...</p>
            </div>
            <div v-else class="bg-gray-50 border border-gray-200 rounded-lg p-4">
              <p class="text-gray-600">暂无AI分析结果</p>
            </div>
          </div>
        </div>

        <!-- 审批流程进度 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white flex items-center">
              <span class="mr-2">📋</span>
              审批进度
            </h2>
          </div>
          <div class="px-6 py-4">
            <div class="progress-timeline space-y-4">
              <div :class="['progress-step flex items-start gap-4 p-4 rounded-lg', { 
                'completed bg-green-50 border border-green-200': hasAIAnalysis(project), 
                'active bg-blue-50 border border-blue-200': isAIActive(project),
                'rejected bg-red-50 border border-red-200': project.approvalStatus === 'AI_REJECTED'
              }]">
                <div :class="['step-marker w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium', {
                  'bg-green-500 text-white': hasAIAnalysis(project),
                  'bg-blue-500 text-white': isAIActive(project),
                  'bg-red-500 text-white': project.approvalStatus === 'AI_REJECTED',
                  'bg-gray-300 text-gray-600': !hasAIAnalysis(project) && !isAIActive(project) && project.approvalStatus !== 'AI_REJECTED'
                }]">1</div>
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900">AI分析</h3>
                  <p class="text-sm text-gray-600">{{ getAIStatus(project) }}</p>
                </div>
              </div>

              <div :class="['progress-step flex items-start gap-4 p-4 rounded-lg', { 
                'completed bg-green-50 border border-green-200': hasAdminApproval(project), 
                'active bg-blue-50 border border-blue-200': isAdminActive(project),
                'rejected bg-red-50 border border-red-200': project.approvalStatus === 'ADMIN_REJECTED'
              }]">
                <div :class="['step-marker w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium', {
                  'bg-green-500 text-white': hasAdminApproval(project),
                  'bg-blue-500 text-white': isAdminActive(project),
                  'bg-red-500 text-white': project.approvalStatus === 'ADMIN_REJECTED',
                  'bg-gray-300 text-gray-600': !hasAdminApproval(project) && !isAdminActive(project) && project.approvalStatus !== 'ADMIN_REJECTED'
                }]">2</div>
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900">管理员审核</h3>
                  <p class="text-sm text-gray-600">{{ getAdminStatus(project) }}</p>
                  <p v-if="project.adminReviewedAt && hasAdminApproval(project)" class="text-xs text-gray-500 mt-1">
                    {{ formatDate(project.adminReviewedAt) }}
                  </p>
                  <!-- 管理员拒绝理由 -->
                  <div v-if="project.approvalStatus === 'ADMIN_REJECTED'" 
                       class="mt-2 p-2 bg-red-100 dark:bg-red-900 rounded text-sm">
                    <span class="font-medium text-red-700 dark:text-red-300">拒绝理由:</span>
                    <span class="text-red-600 dark:text-red-400 ml-1">{{ project.rejectionReason || '未提供拒绝原因' }}</span>
                  </div>
                </div>
              </div>

              <div :class="['progress-step flex items-start gap-4 p-4 rounded-lg', { 
                'completed bg-green-50 border border-green-200': hasSuperAdminApproval(project), 
                'active bg-blue-50 border border-blue-200': isSuperAdminActive(project),
                'rejected bg-red-50 border border-red-200': project.approvalStatus === 'SUPER_ADMIN_REJECTED'
              }]">
                <div :class="['step-marker w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium', {
                  'bg-green-500 text-white': hasSuperAdminApproval(project),
                  'bg-blue-500 text-white': isSuperAdminActive(project),
                  'bg-red-500 text-white': project.approvalStatus === 'SUPER_ADMIN_REJECTED',
                  'bg-gray-300 text-gray-600': !hasSuperAdminApproval(project) && !isSuperAdminActive(project) && project.approvalStatus !== 'SUPER_ADMIN_REJECTED'
                }]">3</div>
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900">超级管理员审核</h3>
                  <p class="text-sm text-gray-600">{{ getSuperAdminStatus(project) }}</p>
                  <p v-if="project.superAdminReviewedAt && hasSuperAdminApproval(project)" class="text-xs text-gray-500 mt-1">
                    {{ formatDate(project.superAdminReviewedAt) }}
                  </p>
                  <!-- 超级管理员拒绝理由 -->
                  <div v-if="project.approvalStatus === 'SUPER_ADMIN_REJECTED'" 
                       class="mt-2 p-2 bg-red-100 dark:bg-red-900 rounded text-sm">
                    <span class="font-medium text-red-700 dark:text-red-300">拒绝理由:</span>
                    <span class="text-red-600 dark:text-red-400 ml-1">{{ project.rejectionReason || '未提供拒绝原因' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 项目信息 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">项目信息</h2>
          </div>
          <div class="px-6 py-4">
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-900">创建者:</span>
                <span class="ml-2 text-gray-600">{{ project.createdByUsername || '未知用户' }}</span>
              </div>
              <div>
                <span class="font-medium text-gray-900">创建时间:</span>
                <span class="ml-2 text-gray-600">{{ formatDate(project.createdAt) }}</span>
              </div>
              <div v-if="project.updatedAt">
                <span class="font-medium text-gray-900">最后更新:</span>
                <span class="ml-2 text-gray-600">{{ formatDate(project.updatedAt) }}</span>
              </div>
            </div>
            
            <!-- 修改和强行提交按钮 -->
            <div v-if="canResubmitProject(project)" class="px-6 py-4 border-t border-gray-200">
              <div class="mb-3">
                <div class="text-sm text-gray-600">
                  <span v-if="project.approvalStatus === 'ADMIN_REJECTED'">
                    项目已被管理员拒绝，您可以修改项目内容后重新提交
                  </span>
                  <span v-else>
                    项目已被拒绝，您可以选择修改项目内容或强行提交给管理员
                  </span>
                </div>
              </div>
              <div class="flex items-center justify-end space-x-3">
                <button
                  @click="goToEdit"
                  class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                  修改项目
                </button>
                <!-- 只有在非管理员拒绝的状态下才显示强行提交按钮 -->
                <button
                  v-if="project.approvalStatus !== 'ADMIN_REJECTED'"
                  @click="handleForceSubmit"
                  :disabled="forceSubmitting"
                  class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 disabled:opacity-50"
                >
                  <svg v-if="forceSubmitting" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <svg v-else class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                  {{ forceSubmitting ? '提交中...' : '强行提交' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="text-center py-12">
        <div class="bg-red-50 border border-red-200 rounded-md p-4">
          <p class="text-red-700">{{ error }}</p>
          <button @click="fetchProject" class="mt-2 text-red-600 hover:text-red-800">重试</button>
        </div>
      </div>
    </div>
    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const project = ref<any>(null)
const projectPhases = ref<any[]>([])
// 创建者信息现在通过 project.createdByUsername 字段获取
const aiAnalysisResult = ref<string>('')
const loading = ref(false)
const error = ref('')
const forceSubmitting = ref(false)

const projectId = route.params.id

// 项目阶段数据从项目详情接口一起获取，不需要单独调用接口
const fetchProjectPhases = async () => {
  // 项目阶段数据已经在项目详情中获取，这里不需要再调用额外接口
  console.log('项目阶段数据来自项目详情接口')
}

// 创建者信息现在直接从项目详情接口的 createdByUsername 字段获取

// AI分析结果从项目详情接口一起获取
const fetchAIAnalysisResult = async (projectId: string) => {
  // AI分析结果已经在项目详情的aiAnalysisResult字段中，不需要单独获取
  console.log('AI分析结果来自项目详情接口')
}

// 获取项目详情
const fetchProject = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`/api/projects/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    if (response.ok) {
      const result = await response.json()
      if (result.success) {
        project.value = result.data
        
        // 创建者信息已通过 createdByUsername 字段获取
        
        // 设置项目阶段数据（从项目详情中获取）
        if (project.value.phases) {
          // 映射后端字段到前端期望的字段名称
          projectPhases.value = project.value.phases.map((phase: any) => ({
            id: phase.id,
            projectId: phase.projectId,
            phaseName: phase.phaseName,
            // 字段映射修复
            phaseDescription: phase.description || '',
            assignedMembers: phase.assignedMembers || '',
            timeline: phase.schedule || '',
            estimatedResults: phase.expectedResults || '',
            // 保留原字段以兼容
            description: phase.description || '',
            schedule: phase.schedule || '',
            expectedResults: phase.expectedResults || '',
            actualResults: phase.actualResults || '',
            resultDifferenceAnalysis: phase.resultDifferenceAnalysis || '',
            createdAt: phase.createdAt,
            updatedAt: phase.updatedAt,
            status: phase.status || 'PENDING',
            phaseOrder: phase.phaseOrder
          }))
          console.log('获取项目阶段成功:', projectPhases.value.length, '个阶段')
        }
        
        // 设置AI分析结果（从项目详情中获取）
        if (project.value.aiAnalysisResult) {
          aiAnalysisResult.value = project.value.aiAnalysisResult.result || project.value.aiAnalysisResult
          console.log('获取AI分析结果成功')
        }
      } else {
        error.value = result.message || '获取项目详情失败'
      }
    } else {
      error.value = '获取项目详情失败'
    }
  } catch (err) {
    console.error('获取项目详情失败:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 状态判断函数
const hasManagerApproval = (project: any) => {
  return project.managerReviewer && !project.status.includes('MANAGER_REJECTED')
}

const hasAIAnalysis = (project: any) => {
  return project.approvalStatus === 'AI_APPROVED' || 
         (project.approvalStatus !== 'AI_REJECTED' && project.approvalStatus !== 'AI_ANALYZING')
}

const hasAdminApproval = (project: any) => {
  // 如果项目重新提交，处于早期状态，管理员审核应该显示为未完成
  if (project.approvalStatus === 'AI_ANALYZING' || 
      project.approvalStatus === 'AI_REJECTED') {
    return false
  }
  
  // 管理员审核完成的条件：状态已经超越管理员审核阶段
  return project.approvalStatus === 'SUPER_ADMIN_REVIEWING' ||
         project.approvalStatus === 'SUPER_ADMIN_APPROVED' ||
         project.approvalStatus === 'SUPER_ADMIN_REJECTED'
}

const hasSuperAdminApproval = (project: any) => {
  return project.approvalStatus === 'SUPER_ADMIN_APPROVED'
}

// Active状态判断函数
const isManagerActive = (project: any) => {
  return project.status === 'PENDING_MANAGER_REVIEW' || 
         (project.status === 'SUBMITTED' && !project.managerReviewer)
}

const isAIActive = (project: any) => {
  return project.approvalStatus === 'AI_ANALYZING'
}

const isAdminActive = (project: any) => {
  return project.approvalStatus === 'ADMIN_REVIEWING'
}

const isSuperAdminActive = (project: any) => {
  return project.approvalStatus === 'SUPER_ADMIN_REVIEWING'
}

// 获取状态文本和样式 - 项目详情页右上角显示审核人+状态
const getStatusText = (project: any) => {
  const status = project.approvalStatus
  
  // 默认状态文本
  const statusTexts = {
    'AI_ANALYZING': 'AI分析中',
    'AI_APPROVED': 'AI分析通过',
    'AI_REJECTED': 'AI分析不通过',
    'ADMIN_REVIEWING': '管理员审核中',
    'ADMIN_APPROVED': '管理员审核通过',
    'ADMIN_REJECTED': '管理员拒绝',
    'SUPER_ADMIN_REVIEWING': '超级管理员审核中',
    'SUPER_ADMIN_APPROVED': '超级管理员审核通过',
    'SUPER_ADMIN_REJECTED': '超级管理员拒绝',
    'REJECTED': '已拒绝',
    'FINAL_APPROVED': '项目已批准'
  }
  return statusTexts[status as keyof typeof statusTexts] || status
}

const getStatusClass = (status: string) => {
  if (status === 'AI_APPROVED' || status === 'ADMIN_APPROVED' || status === 'SUPER_ADMIN_APPROVED' || status === 'FINAL_APPROVED') return 'status-approved'
  if (status && status.includes('REJECTED')) return 'status-rejected'
  if (status && (status.includes('ANALYZING') || status.includes('REVIEWING'))) return 'status-pending'
  return 'status-default'
}

// 获取各步骤状态
const getManagerStatus = (project: any) => {
  if (project.status === 'MANAGER_REJECTED') return '已拒绝'
  if (project.managerReviewer) return '已通过'
  if (project.status === 'PENDING_MANAGER_REVIEW' || 
      (project.status === 'SUBMITTED' && !project.managerReviewer)) return '待审核'
  return '待处理'
}

const getAIStatus = (project: any) => {
  if (project.approvalStatus === 'AI_REJECTED') return '分析不通过'
  if (project.approvalStatus === 'AI_APPROVED') return '分析完成'
  if (project.approvalStatus === 'AI_ANALYZING') return '分析中'
  return '等待分析'
}

const getAdminStatus = (project: any) => {
  // 最高优先级：如果项目状态在管理员审核之前，直接返回未到审核阶段
  const earlyStages = ['AI_ANALYZING', 'AI_REJECTED'];
  if (earlyStages.includes(project.approvalStatus)) {
    return '未到审核阶段'
  }
  
  // 只有状态确实到达管理员审核阶段或之后，才考虑显示审核人信息
  switch (project.approvalStatus) {
    case 'ADMIN_REJECTED':
      return '已拒绝'
    
    case 'ADMIN_REVIEWING':
      return '待审核'
    
    case 'ADMIN_APPROVED':
    case 'SUPER_ADMIN_REVIEWING':
    case 'SUPER_ADMIN_APPROVED':
    case 'SUPER_ADMIN_REJECTED':
      return '审核完成'
    
    default:
      return '等待AI分析'
  }
}

const getSuperAdminStatus = (project: any) => {
  // 强制检查：如果项目状态在超级管理员审核之前的任何阶段，都不显示审核人信息
  const earlyStages = ['AI_ANALYZING', 'AI_REJECTED', 'AI_APPROVED', 'ADMIN_REVIEWING', 'ADMIN_REJECTED'];
  if (earlyStages.includes(project.approvalStatus)) {
    return '未到审核阶段'
  }
  
  if (project.approvalStatus === 'SUPER_ADMIN_REJECTED') return '已拒绝'
  
  if (project.approvalStatus === 'SUPER_ADMIN_APPROVED') return '已通过'
  
  if (project.approvalStatus === 'SUPER_ADMIN_REVIEWING') return '待审核'
  if (hasAdminApproval(project)) return '等待审核'
  return '等待管理员审核'
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

// 获取AI分析状态文本
const getAnalysisStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '等待中',
    'IN_PROGRESS': '分析中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return statusMap[status] || status
}

// 渲染Markdown内容为HTML
const renderMarkdown = computed(() => {
  return (content: string) => {
    if (!content) return ''
    // 配置marked选项
    marked.setOptions({
      breaks: true,    // 支持换行
      gfm: true,       // 支持GitHub flavored markdown
    })
    return marked(content)
  }
})

// 获取阶段状态文本
const getPhaseStatusText = (status: string) => {
  const statusTexts: Record<string, string> = {
    'PENDING': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusTexts[status] || status
}

// 获取阶段状态样式
const getPhaseStatusClass = (status: string) => {
  const statusClasses: Record<string, string> = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'IN_PROGRESS': 'bg-blue-100 text-blue-800',
    'COMPLETED': 'bg-green-100 text-green-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return statusClasses[status] || 'bg-gray-100 text-gray-800'
}

// 获取置信度颜色样式 (基于0.7阈值)
const getConfidenceClass = (confidence: number) => {
  if (confidence >= 0.8) return 'text-green-600' // 高置信度 - 绿色
  if (confidence >= 0.7) return 'text-blue-600'  // 通过阈值 - 蓝色  
  if (confidence >= 0.5) return 'text-orange-600' // 中等置信度 - 橙色
  return 'text-red-600' // 低置信度 - 红色
}

// 重新提交相关函数
const canResubmitProject = (project: any) => {
  if (!project) return false
  const rejectedStatuses = ['AI_REJECTED', 'ADMIN_REJECTED', 'SUPER_ADMIN_REJECTED']
  return rejectedStatuses.includes(project.approvalStatus) && 
         project.createdBy === authStore.user?.id
}


const goToEdit = () => {
  // 跳转到项目编辑页面
  router.push(`/app/projects/edit/${projectId}`)
}

const handleForceSubmit = async () => {
  if (!confirm('确定要强行提交给管理员吗？这将跳过AI重新分析，直接进入管理员审核环节。')) {
    return
  }
  
  forceSubmitting.value = true
  try {
    const response = await fetch(`/api/simple/projects/${projectId}/force-submit`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    const result = await response.json()
    
    if (result.success) {
      alert('项目已强行提交给管理员审核！')
      // 重新获取项目数据以更新状态显示
      await fetchProject()
    } else {
      throw new Error(result.message || '强行提交失败')
    }
    
  } catch (err: any) {
    console.error('强行提交失败:', err)
    alert('强行提交失败: ' + err.message)
  } finally {
    forceSubmitting.value = false
  }
}


onMounted(() => {
  fetchProject()
})
</script>

<style scoped>
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-rejected {
  background: #fee2e2;
  color: #991b1b;
}

.status-default {
  background: #f3f4f6;
  color: #374151;
}

/* Markdown样式 */
.prose {
  color: inherit;
}

.prose h1, .prose h2, .prose h3, .prose h4, .prose h5, .prose h6 {
  color: inherit;
  font-weight: 600;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.prose h1 { font-size: 1.5em; }
.prose h2 { font-size: 1.3em; }
.prose h3 { font-size: 1.1em; }
.prose h4 { font-size: 1em; }

.prose p {
  margin-bottom: 1em;
  line-height: 1.6;
}

.prose ul, .prose ol {
  margin: 1em 0;
  padding-left: 2em;
}

.prose li {
  margin-bottom: 0.25em;
}

.prose strong {
  font-weight: 600;
}

.prose em {
  font-style: italic;
}

.prose code {
  background-color: #f3f4f6;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-size: 0.875em;
  font-family: ui-monospace, SFMono-Regular, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
}

.prose pre {
  background-color: #f3f4f6;
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  margin: 1em 0;
}

.prose pre code {
  background-color: transparent;
  padding: 0;
}

.prose blockquote {
  border-left: 4px solid #e5e7eb;
  padding-left: 1rem;
  margin: 1em 0;
  font-style: italic;
  color: #6b7280;
}

.prose table {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.prose th, .prose td {
  border: 1px solid #e5e7eb;
  padding: 0.5rem;
  text-align: left;
}

.prose th {
  background-color: #f9fafb;
  font-weight: 600;
}

.prose hr {
  border: none;
  height: 1px;
  background-color: #e5e7eb;
  margin: 2em 0;
}
</style>