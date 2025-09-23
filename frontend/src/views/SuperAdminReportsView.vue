<template>
  <div class="super-admin-reports">
    <div class="header">
      <h1>周报面板</h1>
      <p class="subtitle">超级管理员周报查看界面 - 查看已批准的周报</p>
    </div>

    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-button', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="count">{{ tab.count }}</span>
      </button>
    </div>

    <div class="content">
      <div v-if="filteredReports.length === 0" class="no-data">
        <div class="no-data-icon">📄</div>
        <h3>暂无{{ getTabLabel(activeTab) }}周报</h3>
        <p>当前没有符合条件的周报</p>
      </div>
      
      <div v-else class="reports-grid">
        <div 
          v-for="report in filteredReports" 
          :key="report.id"
          class="report-card"
        >
          <div class="card-header">
            <h3>{{ report.title || '无标题' }}</h3>
            <span :class="['status', getStatusClass(report.approvalStatus || report.status)]">
              {{ getStatusText(report.approvalStatus || report.status) }}
            </span>
          </div>
          
          <div class="card-content">
            <div class="info-section">
              <p><strong>报告周:</strong> {{ report.reportWeek }}</p>
              <p><strong>用户ID:</strong> {{ report.userId }}</p>
              
              <!-- 本周汇报内容 -->
              <div v-if="report.content" class="content-item">
                <p><strong>本周汇报:</strong></p>
                
                <!-- 日常性任务 -->
                <div v-if="report.content.routineTasks && report.content.routineTasks.length > 0" class="task-section">
                  <h4>日常性任务 ({{ report.content.routineTasks.length }}项)</h4>
                  <div v-for="task in report.content.routineTasks" :key="task.task_id" class="task-item clickable" @click="openTaskModal(task)">
                    <p><strong>任务:</strong> {{ task.taskDetails?.taskName || '未知任务' }}</p>
                    <p v-if="task.actual_result"><strong>实际结果:</strong> {{ task.actual_result }}</p>
                    <p v-if="task.AnalysisofResultDifferences"><strong>差异分析:</strong> {{ task.AnalysisofResultDifferences }}</p>
                  </div>
                </div>
                
                <!-- 发展性任务 -->
                <div v-if="report.content.developmentalTasks && report.content.developmentalTasks.length > 0" class="task-section">
                  <h4>发展性任务 ({{ report.content.developmentalTasks.length }}项)</h4>
                  <div v-for="task in report.content.developmentalTasks" :key="task.project_id" class="task-item clickable" @click="openTaskModal(task)">
                    <p><strong>项目:</strong> {{ task.projectDetails?.projectName || '未知项目' }}</p>
                    <p v-if="task.phaseDetails?.phaseName"><strong>阶段:</strong> {{ task.phaseDetails.phaseName }}</p>
                    <p v-if="task.actual_result"><strong>实际结果:</strong> {{ task.actual_result }}</p>
                    <p v-if="task.AnalysisofResultDifferences"><strong>差异分析:</strong> {{ task.AnalysisofResultDifferences }}</p>
                  </div>
                </div>
              </div>
              
              <!-- 下周规划 -->
              <div v-if="report.nextWeekPlan" class="content-item">
                <p><strong>下周规划:</strong></p>
                
                <!-- 下周日常性任务 -->
                <div v-if="report.nextWeekPlan.routineTasks && report.nextWeekPlan.routineTasks.length > 0" class="task-section">
                  <h4>下周日常性任务 ({{ report.nextWeekPlan.routineTasks.length }}项)</h4>
                  <div v-for="task in report.nextWeekPlan.routineTasks" :key="task.task_id" class="task-item clickable" @click="openTaskModal(task)">
                    <p><strong>任务:</strong> {{ task.taskDetails?.taskName || '未知任务' }}</p>
                  </div>
                </div>
                
                <!-- 下周发展性任务 -->
                <div v-if="report.nextWeekPlan.developmentalTasks && report.nextWeekPlan.developmentalTasks.length > 0" class="task-section">
                  <h4>下周发展性任务 ({{ report.nextWeekPlan.developmentalTasks.length }}项)</h4>
                  <div v-for="task in report.nextWeekPlan.developmentalTasks" :key="task.project_id" class="task-item clickable" @click="openTaskModal(task)">
                    <p><strong>项目:</strong> {{ task.projectDetails?.projectName || '未知项目' }}</p>
                    <p v-if="task.phaseDetails?.phaseName"><strong>阶段:</strong> {{ task.phaseDetails.phaseName }}</p>
                  </div>
                </div>
              </div>
              
              <!-- 额外备注（如果有） -->
              <div v-if="report.additionalNotes" class="content-item">
                <p><strong>备注说明:</strong></p>
                <div class="markdown-content" v-html="renderMarkdown(report.additionalNotes)"></div>
              </div>
              
              <!-- 发展机会（如果有） -->
              <div v-if="report.developmentOpportunities" class="content-item">
                <p><strong>发展机会:</strong></p>
                <div class="markdown-content" v-html="renderMarkdown(report.developmentOpportunities)"></div>
              </div>
            </div>

            <div v-if="report.aiAnalysisResult" class="ai-analysis">
              <h4>🤖 AI分析结果</h4>
              <div class="ai-content">
                <p>{{ report.aiAnalysisResult }}</p>
                <div v-if="report.aiConfidence" class="ai-confidence">
                  <small>置信度: {{ Math.round(report.aiConfidence * 100) }}%</small>
                </div>
              </div>
            </div>

            <!-- 审批完成状态 -->
            <div class="approval-complete">
              <h4>✅ 审批状态</h4>
              <div class="status-info">
                <span class="completed-badge">已完成审批流程</span>
                <div class="approval-details">
                  <p>该周报已通过完整的审批流程，包括AI分析和管理员审核</p>
                  <p v-if="report.adminReviewedAt">管理员审核时间: {{ formatDate(report.adminReviewedAt) }}</p>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-actions">
            <div class="creator-info">
              <span class="creator">提交人ID: {{ report.userId || '未知' }}</span>
              <span class="create-time">{{ formatDate(report.createdAt) }}</span>
            </div>
            
            <div class="approval-info">
              <span class="approved-text">✅ 周报已批准入库</span>
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

            <!-- 实际结果 -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">实际结果</label>
              <div class="mt-1 p-3 bg-blue-50 rounded-lg border border-blue-200">
                <p class="text-sm text-gray-700">{{ selectedTask.actual_result || selectedTask.actualResult || selectedTask.actualResults || '暂未填写实际结果' }}</p>
              </div>
            </div>

            <!-- 结果差异分析 -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">结果差异分析</label>
              <div class="mt-1 p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                <p class="text-sm text-gray-700">{{ selectedTask.analysisofResultDifferences || selectedTask.AnalysisofResultDifferences || selectedTask.resultDifferenceAnalysis || '暂未填写差异分析' }}</p>
              </div>
            </div>
          </div>

          <!-- 发展性任务信息 -->
          <div v-else-if="isDevelopmentTask(selectedTask)" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">项目名称</label>
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

            <!-- 实际结果 -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">实际结果</label>
              <div class="mt-1 p-3 bg-blue-50 rounded-lg border border-blue-200">
                <p class="text-sm text-gray-700">{{ selectedTask.actual_result || selectedTask.actualResult || selectedTask.actualResults || '暂未填写实际结果' }}</p>
              </div>
            </div>

            <!-- 结果差异分析 -->
            <div v-if="isThisWeekTask(selectedTask)">
              <label class="block text-sm font-medium text-gray-700">结果差异分析</label>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { weeklyReportAPI } from '@/services/api'

const authStore = useAuthStore()

const activeTab = ref('all')
const allReports = ref([])

// 任务详情模态框相关状态
const showTaskModal = ref(false)
const selectedTask = ref(null)

const tabs = computed(() => {
  const reports = allReports.value
  return [
    { 
      key: 'all', 
      label: '全部周报', 
      count: reports.length 
    },
    { 
      key: 'thisweek', 
      label: '本周', 
      count: reports.filter(r => isThisWeekReport(r.reportWeek)).length 
    }
  ]
})

const filteredReports = computed(() => {
  console.log('过滤周报数据:', { activeTab: activeTab.value, allReports: allReports.value })
  switch (activeTab.value) {
    case 'all':
      return allReports.value
    case 'thisweek':
      return allReports.value.filter(r => isThisWeekReport(r.reportWeek))
    default:
      return allReports.value
  }
})

const isThisWeekReport = (reportWeek: string) => {
  if (!reportWeek) return false
  // 获取当前周的格式 (例如: "2024年第1周")
  const now = new Date()
  const currentYear = now.getFullYear()
  const startOfYear = new Date(currentYear, 0, 1)
  const days = Math.floor((now.getTime() - startOfYear.getTime()) / (24 * 60 * 60 * 1000))
  const currentWeek = Math.ceil((days + startOfYear.getDay() + 1) / 7)
  const currentWeekString = `${currentYear}年第${currentWeek}周`
  
  return reportWeek.includes(currentWeekString) || reportWeek.includes(`第${currentWeek}周`)
}

const getTabLabel = (tabKey: string) => {
  const tabMap = {
    'all': '全部周报',
    'thisweek': '本周'
  }
  return tabMap[tabKey] || ''
}

const getStatusText = (status: string) => {
  const statusMap = {
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'REVIEWED': '已审核',
    'PUBLISHED': '已发布',
    'PENDING_MANAGER_REVIEW': '等待主管审核',
    'PENDING_ADMIN_REVIEW': '等待管理员审核',
    'PENDING_SUPER_ADMIN_REVIEW': '等待超级管理员审核',
    'APPROVED_BY_MANAGER': '主管已批准',
    'APPROVED_BY_ADMIN': '管理员已批准',
    'APPROVED_BY_SUPER_ADMIN': '超级管理员已批准',
    'REJECTED_BY_MANAGER': '主管已拒绝',
    'REJECTED_BY_ADMIN': '管理员已拒绝',
    'REJECTED_BY_SUPER_ADMIN': '超级管理员已拒绝',
    'RESUBMITTED': '已重新提交',
    'AI_ANALYZING': 'AI分析中',
    'ADMIN_APPROVED': '审核完成',
    'APPROVED': '已批准',
    'ADMIN_REJECTED': '管理员已拒绝',
    'SUPER_ADMIN_REJECTED': '超级管理员已拒绝'
  }
  return statusMap[status] || status
}

const getStatusClass = (status: string) => {
  if (status === 'APPROVED' || status === 'PUBLISHED' || status === 'ADMIN_APPROVED') return 'approved'
  if (status.includes('REJECTED')) return 'rejected'
  if (status.includes('PENDING') || status === 'AI_ANALYZING') return 'pending'
  return 'default'
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


const loadReports = async () => {
  try {
    console.log('🔍 开始加载ADMIN_APPROVED状态的周报...')
    // 只获取ADMIN_APPROVED状态的周报
    const result = await weeklyReportAPI.list('ADMIN_APPROVED')
    
    console.log('🔍 API响应结果:', result)
    
    if (result.success && result.data) {
      // 直接使用API返回的数据，显示主管提交的原始内容
      allReports.value = result.data.sort((a, b) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      )
      console.log('✅ 加载的已批准周报数据数量:', allReports.value.length)
      console.log('✅ 周报详细数据:', allReports.value)
    } else {
      console.error('❌ API返回错误:', result.message)
      console.error('❌ 完整响应:', result)
    }
  } catch (error) {
    console.error('❌ 加载周报列表失败:', error)
  }
}

// 渲染Markdown内容
const renderMarkdown = (content: string) => {
  if (!content || content.trim() === '' || content === '无') {
    return content || '无'
  }
  
  try {
    // 简单的Markdown转HTML（基础支持）
    return content
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/^### (.*$)/gim, '<h4>$1</h4>')
      .replace(/^## (.*$)/gim, '<h3>$1</h3>')
      .replace(/^# (.*$)/gim, '<h2>$1</h2>')
      .replace(/^- (.*$)/gim, '<li>$1</li>')
      .replace(/(\d+)\. (.*$)/gm, '<div class="task-item"><strong>$1. $2</strong></div>')
  } catch (error) {
    console.error('Markdown解析失败:', error)
    return content
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

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.super-admin-reports {
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
  color: #8b5cf6;
  border-bottom-color: #8b5cf6;
}

.count {
  background: #8b5cf6;
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

.approval-complete {
  background: #f0fdf4;
  border: 1px solid #22c55e;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
}

.approval-complete h4 {
  color: #16a34a;
  margin: 0 0 12px 0;
  font-size: 1rem;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.completed-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  background: #dcfce7;
  color: #166534;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  width: fit-content;
}

.approval-details {
  margin-top: 8px;
}

.approval-details p {
  margin: 4px 0;
  color: #15803d;
  font-size: 0.875rem;
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
  background: #8b5cf6;
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
  background: #7c3aed;
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

.approval-info {
  display: flex;
  align-items: center;
}

.approved-text {
  color: #10b981;
  font-weight: 500;
  font-size: 0.875rem;
}

.rejection-info {
  display: flex;
  align-items: center;
}

.rejected-text {
  color: #ef4444;
  font-weight: 500;
  font-size: 0.875rem;
}

/* 任务展示样式 */
.task-section {
  margin: 16px 0;
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
  border-left: 3px solid #3b82f6;
}

.task-section h4 {
  color: #1e40af;
  margin: 0 0 12px 0;
  font-size: 0.9rem;
  font-weight: 600;
}

.task-item {
  margin: 8px 0;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border-left: 2px solid #e5e7eb;
}

.task-item p {
  margin: 4px 0;
  font-size: 0.875rem;
  line-height: 1.4;
}

.task-item p strong {
  color: #374151;
  font-weight: 600;
}

.task-item.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.task-item.clickable:hover {
  background: #f8fafc;
  border-left-color: #3b82f6;
  transform: translateX(2px);
}

.ai-confidence {
  margin-top: 8px;
}

.ai-confidence small {
  color: #6b7280;
  font-size: 0.75rem;
}
</style>