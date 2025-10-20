<template>
  <div class="project-approval bg-gray-50 dark:bg-gray-900 min-h-screen">
    <div class="header">
      <h1>项目审核</h1>
      <p class="subtitle">管理员项目审核界面 - 三级审批工作流</p>
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
      <div v-if="filteredProjects.length === 0" class="no-data">
        <div class="no-data-icon">📋</div>
        <h3>暂无{{ getTabLabel(activeTab) }}项目</h3>
        <p>当前没有需要处理的项目</p>
      </div>
      
      <div v-else class="projects-grid">
        <div 
          v-for="project in filteredProjects" 
          :key="project.id"
          class="project-card"
        >
          <div class="card-header">
            <h3>{{ project.name }}</h3>
            <span :class="['status', getStatusClass(project.approvalStatus)]">
              {{ getStatusText(project) }}
            </span>
          </div>
          
          <div class="card-content">
            <div class="info-section">
              <p><strong>项目内容:</strong> {{ project.description }}</p>
              <p><strong>项目成员:</strong> {{ project.members }}</p>
              <p><strong>预期结果:</strong> {{ project.expectedResults }}</p>
              <p><strong>时间计划:</strong> {{ project.timeline }}</p>
              <p><strong>止损点:</strong> {{ project.stopLoss }}</p>
            </div>

            <!-- 项目阶段性任务 -->
            <div v-if="project.phases && project.phases.length > 0" class="project-phases">
              <h4>📋 项目阶段性任务</h4>
              <div class="phases-list">
                <div 
                  v-for="phase in project.phases" 
                  :key="phase.id"
                  class="phase-item"
                >
                  <div class="phase-header">
                    <span class="phase-name">{{ phase.phaseName }}</span>
                  </div>
                  <div class="phase-content">
                    <p v-if="phase.description" class="phase-description">
                      <strong>阶段描述:</strong> {{ phase.description }}
                    </p>
                    <p v-if="phase.assignedMembers" class="phase-members">
                      <strong>负责成员:</strong> {{ phase.assignedMembers }}
                    </p>
                    <p v-if="phase.schedule" class="phase-schedule">
                      <strong>时间安排:</strong> {{ phase.schedule }}
                    </p>
                    <p v-if="phase.expectedResults" class="phase-expected">
                      <strong>预期结果:</strong> {{ phase.expectedResults }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="project.aiAnalysisResult" class="ai-analysis">
              <h4>🤖 AI分析结果</h4>
              <div class="ai-content">
                <div v-if="typeof project.aiAnalysisResult === 'string'" v-html="renderMarkdown(project.aiAnalysisResult)"></div>
                <div v-else>
                  <div class="mb-2 prose prose-sm max-w-none" v-html="renderMarkdown(project.aiAnalysisResult.result)"></div>
                  <div class="text-xs text-gray-500">
                    <span v-if="project.aiAnalysisResult.confidence" :class="getConfidenceClass(project.aiAnalysisResult.confidence)">
                      置信度: {{ Math.round(project.aiAnalysisResult.confidence * 100) }}%
                      <span v-if="project.aiAnalysisResult.confidence >= 0.7" class="ml-1">✅</span>
                      <span v-else class="ml-1">❌</span>
                    </span>
                    <span v-if="project.aiAnalysisResult.modelVersion" class="ml-4 text-gray-500">模型: {{ project.aiAnalysisResult.modelVersion }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 审批历史 -->
            <div v-if="hasApprovalHistory(project)" class="approval-history">
              <h4>📝 审批历史</h4>
              <div class="history-timeline">
                <div v-if="project.managerReviewer" class="history-item">
                  <span class="history-role">{{ project.managerReviewer.fullName }}审核</span>
                  <span :class="['history-result', getReviewResultClass(project.status, 'manager')]">
                    {{ getReviewResultText(project.status, 'manager') }}
                  </span>
                  <span class="history-comment">{{ project.managerReviewComment || '无备注' }}</span>
                  <span class="history-time">{{ formatDate(project.managerReviewedAt) }}</span>
                </div>
                
                <div v-if="project.adminReviewer" class="history-item">
                  <span class="history-role">{{ project.adminReviewer.fullName }}审核</span>
                  <span :class="['history-result', getReviewResultClass(project.status, 'admin')]">
                    {{ getReviewResultText(project.status, 'admin') }}
                  </span>
                  <span class="history-comment">{{ project.adminReviewComment || '无备注' }}</span>
                  <span class="history-time">{{ formatDate(project.adminReviewedAt) }}</span>
                </div>

                <div v-if="project.superAdminReviewer" class="history-item">
                  <span class="history-role">{{ project.superAdminReviewer.fullName }}审核</span>
                  <span :class="['history-result', getReviewResultClass(project.status, 'superAdmin')]">
                    {{ getReviewResultText(project.status, 'superAdmin') }}
                  </span>
                  <span class="history-comment">{{ project.superAdminReviewComment || '无备注' }}</span>
                  <span class="history-time">{{ formatDate(project.superAdminReviewedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-actions">
            <div class="creator-info">
              <span class="creator">创建者: {{ project.createdByUsername || '未知用户' }}</span>
              <span class="create-time">{{ formatDate(project.createdAt) }}</span>
            </div>
            
            <div v-if="canApprove(project.approvalStatus)" class="action-buttons">
              <button 
                @click="approveProject(project.id, true)"
                class="approve-btn"
              >
                ✅ 批准通过
              </button>
              <button 
                @click="openRejectModal(project)"
                class="reject-btn"
              >
                ❌ 拒绝申请
              </button>
            </div>
            
            <div v-else-if="project.status === 'PENDING_SUPER_ADMIN_REVIEW'" class="pending-info">
              <span class="pending-text">⏳ 等待超级管理员最终审批</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝理由模态框 -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>拒绝项目</h3>
          <button @click="closeRejectModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="project-info">
            <p><strong>项目名称:</strong> {{ selectedProject?.name }}</p>
            <p><strong>创建者:</strong> {{ selectedProject?.createdByUsername || '未知用户' }}</p>
          </div>
          
          <div class="reject-reason">
            <label for="rejectReason">请填写拒绝理由 <span class="required">*</span></label>
            <textarea
              id="rejectReason"
              v-model="rejectReason"
              rows="4"
              placeholder="请详细说明拒绝该项目的原因..."
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

const authStore = useAuthStore()

const activeTab = ref('pending')
const allProjects = ref([])
const projectPhases = ref(new Map()) // Store project phases by project ID

// 拒绝模态框相关状态
const showRejectModal = ref(false)
const selectedProject = ref(null)
const rejectReason = ref('')
const rejecting = ref(false)

const tabs = computed(() => {
  // 现在每个tab都有独立的数据，所以count就是当前tab的项目数量
  return [
    { 
      key: 'pending', 
      label: '待审批', 
      count: activeTab.value === 'pending' ? allProjects.value.length : 0
    },
    { 
      key: 'approved', 
      label: '已通过', 
      count: activeTab.value === 'approved' ? allProjects.value.length : 0
    },
    { 
      key: 'rejected', 
      label: '已拒绝', 
      count: activeTab.value === 'rejected' ? allProjects.value.length : 0
    }
  ]
})

const filteredProjects = computed(() => {
  // 现在每个tab都直接从对应的接口获取数据，不需要过滤
  return allProjects.value
})

const getTabLabel = (tabKey: string) => {
  const tabMap = {
    'pending': '待审批',
    'approved': '已通过', 
    'rejected': '已拒绝'
  }
  return tabMap[tabKey] || ''
}

const getStatusText = (project: any) => {
  const approvalStatus = project.approvalStatus
  
  // 对于拒绝状态，显示审核人姓名 + 拒绝
  if (approvalStatus === 'ADMIN_REJECTED' && project.adminReviewerUsername) {
    return `${project.adminReviewerUsername}拒绝`
  }
  if (approvalStatus === 'SUPER_ADMIN_REJECTED' && project.superAdminReviewerUsername) {
    return `${project.superAdminReviewerUsername}拒绝`
  }
  
  // 对于批准状态，显示审核人姓名 + 批准
  if (approvalStatus === 'ADMIN_APPROVED' && project.adminReviewerUsername) {
    return `${project.adminReviewerUsername}批准`
  }
  if (approvalStatus === 'SUPER_ADMIN_APPROVED' && project.superAdminReviewerUsername) {
    return `${project.superAdminReviewerUsername}批准`
  }
  if (approvalStatus === 'FINAL_APPROVED' && project.superAdminReviewerUsername) {
    return `${project.superAdminReviewerUsername}批准`
  }
  
  // 默认状态文本
  const statusMap = {
    'AI_ANALYZING': 'AI分析中',
    'AI_APPROVED': 'AI分析通过',
    'AI_REJECTED': 'AI分析不通过',
    'ADMIN_REVIEWING': '待管理员审核',
    'ADMIN_APPROVED': '管理员通过',
    'ADMIN_REJECTED': '管理员拒绝',
    'SUPER_ADMIN_REVIEWING': '待超级管理员审核',
    'SUPER_ADMIN_APPROVED': '超级管理员通过',
    'SUPER_ADMIN_REJECTED': '超级管理员拒绝',
    'FINAL_APPROVED': '最终批准'
  }
  return statusMap[approvalStatus] || approvalStatus
}

const getStatusClass = (approvalStatus: string) => {
  if (approvalStatus === 'FINAL_APPROVED' || approvalStatus.includes('APPROVED')) return 'approved'
  if (approvalStatus.includes('REJECTED')) return 'rejected'
  if (approvalStatus.includes('REVIEWING') || approvalStatus === 'AI_ANALYZING') return 'pending'
  return 'default'
}

const getPhaseStatusText = (status: string) => {
  const statusMap = {
    'PLANNING': '规划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getPhaseStatusClass = (status: string) => {
  switch (status) {
    case 'PLANNING': return 'planning'
    case 'IN_PROGRESS': return 'in-progress'
    case 'COMPLETED': return 'completed'
    case 'CANCELLED': return 'cancelled'
    default: return 'default'
  }
}

const canApprove = (approvalStatus: string) => {
  return approvalStatus === 'ADMIN_REVIEWING'
}

const hasApprovalHistory = (project: any) => {
  return project.managerReviewer || project.adminReviewer || project.superAdminReviewer
}

// 获取审核结果文本
const getReviewResultText = (status: string, reviewerType: string) => {
  switch (reviewerType) {
    case 'manager':
      return status === 'MANAGER_REJECTED' ? '拒绝' : '通过'
    case 'admin':
      return status === 'ADMIN_REJECTED' ? '拒绝' : '通过'
    case 'superAdmin':
      return status === 'SUPER_ADMIN_REJECTED' ? '拒绝' : '通过'
    default:
      return '通过'
  }
}

// 获取审核结果样式类
const getReviewResultClass = (status: string, reviewerType: string) => {
  switch (reviewerType) {
    case 'manager':
      return status === 'MANAGER_REJECTED' ? 'rejected' : 'approved'
    case 'admin':
      return status === 'ADMIN_REJECTED' ? 'rejected' : 'approved'
    case 'superAdmin':
      return status === 'SUPER_ADMIN_REJECTED' ? 'rejected' : 'approved'
    default:
      return 'approved'
  }
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

// 渲染Markdown内容为HTML
const renderMarkdown = computed(() => {
  return (content: string) => {
    if (!content) return ''
    marked.setOptions({
      breaks: true,
      gfm: true,
    })
    return marked(content)
  }
})

// 获取置信度颜色样式 (基于0.7阈值)
const getConfidenceClass = (confidence: number) => {
  if (confidence >= 0.8) return 'text-green-600 font-semibold' // 高置信度 - 绿色
  if (confidence >= 0.7) return 'text-blue-600 font-semibold'  // 通过阈值 - 蓝色  
  if (confidence >= 0.5) return 'text-orange-600 font-semibold' // 中等置信度 - 橙色
  return 'text-red-600 font-semibold' // 低置信度 - 红色
}

const approveProject = async (projectId: number, approved: boolean, customComment?: string) => {
  try {
    const comment = customComment || (approved ? '项目方案合理，同意进入下一步审核' : '项目方案需要修改，请重新提交')
    
    let endpoint, method, body
    if (approved) {
      endpoint = `/api/projects/${projectId}/admin-approve`
      method = 'PUT'
      body = null
    } else {
      endpoint = `/api/projects/${projectId}/reject`
      method = 'PUT'
      body = JSON.stringify(comment)
    }
    
    const response = await fetch(endpoint, {
      method: method,
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      },
      body: body
    })

    if (response.ok) {
      alert(approved ? '项目审批通过，已提交给超级管理员' : '项目已拒绝')
      loadProjects()
    } else {
      const error = await response.json()
      alert('操作失败: ' + error.message)
    }
  } catch (error) {
    console.error('审批操作失败:', error)
    alert('操作失败，请稍后重试')
  }
}

// 打开拒绝模态框
const openRejectModal = (project: any) => {
  selectedProject.value = project
  rejectReason.value = ''
  showRejectModal.value = true
}

// 关闭拒绝模态框
const closeRejectModal = () => {
  showRejectModal.value = false
  selectedProject.value = null
  rejectReason.value = ''
  rejecting.value = false
}

// 确认拒绝项目
const confirmReject = async () => {
  if (!selectedProject.value || !rejectReason.value.trim()) {
    alert('请填写拒绝理由')
    return
  }
  
  rejecting.value = true
  try {
    await approveProject(selectedProject.value.id, false, rejectReason.value.trim())
    closeRejectModal()
  } catch (error) {
    console.error('拒绝项目失败:', error)
  } finally {
    rejecting.value = false
  }
}

const loadProjectPhases = async (projectId: number) => {
  try {
    // 通过项目详情接口获取阶段数据
    const response = await fetch(`/api/projects/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    if (response.ok) {
      const result = await response.json()
      if (result.success && result.data.phases) {
        projectPhases.value.set(projectId, result.data.phases)
      }
    }
  } catch (error) {
    console.error('加载项目阶段失败:', error)
  }
}

const loadProjects = async (tabKey?: string) => {
  try {
    // 根据当前tab或传入的tabKey决定使用哪个接口
    const currentTab = tabKey || activeTab.value
    let endpoint = '/api/projects/pending' // 默认接口
    
    switch (currentTab) {
      case 'pending':
        endpoint = '/api/projects/pending-review'
        break
      case 'approved':
        endpoint = '/api/projects/approved'
        break
      case 'rejected':
        endpoint = '/api/projects/rejected'
        break
      default:
        endpoint = '/api/projects/pending-review'
    }
    
    console.log(`Loading projects for tab: ${currentTab}, using endpoint: ${endpoint}`)
    
    const response = await fetch(endpoint, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    if (response.ok) {
      const result = await response.json()
      if (result.success) {
        allProjects.value = result.data.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        )
        
        console.log(`Successfully loaded ${result.data.length} projects for ${currentTab} tab`)
        // Note: phases and aiAnalysisResult are now included in the backend response
      }
    } else {
      console.error(`Failed to load projects: ${response.status} ${response.statusText}`)
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

// 监听tab切换，自动重新加载数据
watch(activeTab, (newTab) => {
  console.log(`Tab changed to: ${newTab}, reloading projects...`)
  loadProjects(newTab)
})

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-approval {
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

html.dark .header h1 {
  color: #ffffff;
}

.subtitle {
  color: #6b7280;
  font-size: 1rem;
}

html.dark .subtitle {
  color: #d1d5db;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

:global(.dark) .tabs {
  border-bottom-color: #374151;
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

:global(.dark) .tab-button {
  color: #d1d5db;
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

.projects-grid {
  display: grid;
  gap: 20px;
}

.project-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s;
}

html.dark .project-card {
  background: #1e293b !important;
  border-color: #374151 !important;
  color: #ffffff;
}

.project-card:hover {
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

html.dark .card-header h3 {
  color: #ffffff;
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

html.dark .info-section p {
  color: #d1d5db;
}

.ai-analysis {
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
}

html.dark .ai-analysis {
  background: #1e3a8a;
  border-color: #3b82f6;
}

.ai-analysis h4 {
  color: #0369a1;
  margin: 0 0 12px 0;
  font-size: 1rem;
}

html.dark .ai-analysis h4 {
  color: #93c5fd;
}

.ai-content p {
  color: #1e40af !important;
  margin: 0;
  font-weight: 500;
}

html.dark .ai-content p {
  color: #bfdbfe !important;
}

.approval-history {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
}

.approval-history h4 {
  color: #374151;
  margin: 0 0 12px 0;
  font-size: 1rem;
}

.history-timeline {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: grid;
  grid-template-columns: 80px 60px 1fr 120px;
  gap: 12px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.history-item:last-child {
  border-bottom: none;
}

.history-role {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.history-result.approved {
  background: #d1fae5;
  color: #065f46;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  text-align: center;
}

.history-result.rejected {
  background: #fee2e2;
  color: #991b1b;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  text-align: center;
}

.history-comment {
  color: #6b7280;
  font-size: 0.875rem;
}

.history-time {
  color: #9ca3af;
  font-size: 0.75rem;
  text-align: right;
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

html.dark .creator {
  color: #e5e7eb;
}

.create-time {
  color: #6b7280;
  font-size: 0.75rem;
}

html.dark .create-time {
  color: #d1d5db;
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

.pending-info {
  display: flex;
  align-items: center;
}

.pending-text {
  color: #f59e0b;
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

.project-info {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.project-info p {
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

/* 项目阶段样式 */
.project-phases {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 16px;
  margin: 16px 0;
}

html.dark .project-phases {
  background: #374151;
  border-color: #4b5563;
}

.project-phases h4 {
  margin: 0 0 12px 0;
  color: #374151;
  font-size: 1rem;
  font-weight: 600;
}

html.dark .project-phases h4 {
  color: #ffffff;
}

.phases-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.phase-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 12px;
}

html.dark .phase-item {
  background: #4b5563;
  border-color: #6b7280;
}

.phase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.phase-name {
  font-weight: 500;
  color: #374151;
}

html.dark .phase-name {
  color: #ffffff;
}

.phase-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.phase-status.planning {
  background: #fef3c7;
  color: #92400e;
}

.phase-status.in-progress {
  background: #dbeafe;
  color: #1e40af;
}

.phase-status.completed {
  background: #d1fae5;
  color: #065f46;
}

.phase-status.cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.phase-description {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 4px 0 8px 0;
}

html.dark .phase-description {
  color: #d1d5db;
}

.phase-details {
  display: flex;
  gap: 16px;
}

.phase-date {
  color: #9ca3af;
  font-size: 0.75rem;
}
</style>