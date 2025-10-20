<template>
  <div class="super-admin-projects bg-gray-50 dark:bg-gray-900 min-h-screen">
    <div class="header">
      <h1>项目管理</h1>
      <p class="subtitle">超级管理员项目管理界面 - 查看所有项目状态</p>
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
        <p>当前没有符合条件的项目</p>
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

            <!-- 审批流程进度 -->
            <div class="approval-progress">
              <h4>📋 审批进度</h4>
              <div class="progress-timeline">
                <div :class="['progress-step', { 
                  completed: hasAIAnalysis(project), 
                  active: isAIActive(project),
                  rejected: project.status === 'AI_REJECTED'
                }]">
                  <div class="step-marker">1</div>
                  <div class="step-content">
                    <span class="step-title">AI分析</span>
                    <span class="step-status">{{ getAIStatus(project) }}</span>
                  </div>
                </div>

                <div :class="['progress-step', { 
                  completed: hasAdminApproval(project), 
                  active: isAdminActive(project),
                  rejected: project.status === 'ADMIN_REJECTED'
                }]">
                  <div class="step-marker">2</div>
                  <div class="step-content">
                    <span class="step-title">管理员审核</span>
                    <span class="step-status">{{ getAdminStatus(project) }}</span>
                    <span v-if="project.adminReviewedAt" class="step-time">{{ formatDate(project.adminReviewedAt) }}</span>
                    
                    <!-- 管理员拒绝理由 -->
                    <div v-if="project.status === 'ADMIN_REJECTED'" 
                         class="mt-2 p-2 bg-red-100 dark:bg-red-900 rounded text-sm">
                      <span class="font-medium text-red-700 dark:text-red-300">拒绝理由:</span>
                      <span class="text-red-600 dark:text-red-400 ml-1">{{ project.adminReviewComment || '未提供拒绝原因' }}</span>
                    </div>
                  </div>
                </div>

                <div :class="['progress-step', { 
                  completed: hasSuperAdminApproval(project), 
                  active: isSuperAdminActive(project),
                  rejected: project.status === 'SUPER_ADMIN_REJECTED'
                }]">
                  <div class="step-marker">3</div>
                  <div class="step-content">
                    <span class="step-title">超级管理员审核</span>
                    <span class="step-status">{{ getSuperAdminStatus(project) }}</span>
                    <span v-if="project.superAdminReviewedAt" class="step-time">{{ formatDate(project.superAdminReviewedAt) }}</span>
                    
                    <!-- 超级管理员拒绝理由 -->
                    <div v-if="project.status === 'SUPER_ADMIN_REJECTED'" 
                         class="mt-2 p-2 bg-red-100 dark:bg-red-900 rounded text-sm">
                      <span class="font-medium text-red-700 dark:text-red-300">拒绝理由:</span>
                      <span class="text-red-600 dark:text-red-400 ml-1">{{ project.superAdminReviewComment || '未提供拒绝原因' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-actions">
            <div class="creator-info">
              <span class="creator">创建者: {{ project.createdByUsername || '未知用户' }}</span>
              <span class="create-time">{{ formatDate(project.createdAt) }}</span>
            </div>
            
            <div v-if="canFinalApprove(project.approvalStatus)" class="action-buttons">
              <button 
                @click="approveProject(project.id, true)"
                class="approve-btn"
              >
                ✅ 最终批准
              </button>
              <button 
                @click="openRejectModal(project)"
                class="reject-btn"
              >
                ❌ 最终拒绝
              </button>
            </div>
            
            <div v-else-if="project.approvalStatus === 'FINAL_APPROVED'" class="approval-info">
              <span class="approved-text">✅ 项目已批准立项</span>
            </div>
            
            <div v-else-if="project.approvalStatus.includes('REJECTED')" class="rejection-info">
              <span class="rejected-text">❌ 项目已被拒绝</span>
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
import { ref, computed, onMounted } from 'vue'
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
      label: '待通过', 
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

const isPending = (status: string) => {
  // 待通过页面：只显示管理员审核通过的项目（等待超级管理员审核）
  return status === 'SUPER_ADMIN_REVIEWING'
}

const isRejected = (status: string) => {
  // 已拒绝页面：只显示超级管理员拒绝的项目
  return status === 'SUPER_ADMIN_REJECTED'
}

const getTabLabel = (tabKey: string) => {
  const tabMap = {
    'pending': '待通过审核',
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

const canFinalApprove = (approvalStatus: string) => {
  return approvalStatus === 'SUPER_ADMIN_REVIEWING'
}

const hasManagerApproval = (project: any) => {
  return project.managerReviewer && !project.status.includes('MANAGER_REJECTED')
}

const hasAIAnalysis = (project: any) => {
  return project.aiAnalysisResult && !project.status.includes('AI_REJECTED')
}

const hasAdminApproval = (project: any) => {
  // 管理员审核完成的条件：状态已经超越管理员审核阶段
  const approvalStatus = project.approvalStatus
  return approvalStatus === 'SUPER_ADMIN_REVIEWING' ||
         approvalStatus === 'SUPER_ADMIN_APPROVED' ||
         approvalStatus === 'FINAL_APPROVED' ||
         approvalStatus === 'SUPER_ADMIN_REJECTED'
}

const hasSuperAdminApproval = (project: any) => {
  const approvalStatus = project.approvalStatus
  return approvalStatus === 'SUPER_ADMIN_APPROVED' || approvalStatus === 'FINAL_APPROVED'
}

// Active state functions for better progress visualization
const isManagerActive = (project: any) => {
  return project.status === 'PENDING_MANAGER_REVIEW' || 
         (project.status === 'SUBMITTED' && !project.managerReviewer)
}

const isAIActive = (project: any) => {
  return project.status === 'PENDING_AI_ANALYSIS'
}

const isAdminActive = (project: any) => {
  return project.approvalStatus === 'ADMIN_REVIEWING'
}

const isSuperAdminActive = (project: any) => {
  return project.approvalStatus === 'SUPER_ADMIN_REVIEWING'
}

const getManagerStatus = (project: any) => {
  if (project.status === 'MANAGER_REJECTED') return '已拒绝'
  if (project.managerReviewer) return '已通过'
  if (project.status === 'PENDING_MANAGER_REVIEW' || 
      (project.status === 'SUBMITTED' && !project.managerReviewer)) return '待审核'
  return '待处理'
}

const getAIStatus = (project: any) => {
  if (project.status === 'AI_REJECTED') return '分析不通过'
  if (project.aiAnalysisResult && !project.status.includes('PENDING_AI_ANALYSIS')) return '分析完成'
  if (project.status === 'PENDING_AI_ANALYSIS') return '分析中'
  return '等待分析'
}

const getAdminStatus = (project: any) => {
  const approvalStatus = project.approvalStatus
  
  if (approvalStatus === 'ADMIN_REJECTED' && project.adminReviewerUsername) {
    return `${project.adminReviewerUsername}已拒绝`
  }
  if (approvalStatus === 'ADMIN_REJECTED') return '已拒绝'
  
  // 如果状态是待超级管理员审核、已批准或超级管理员拒绝，说明管理员已经审核完成
  if ((approvalStatus === 'SUPER_ADMIN_REVIEWING' || 
      approvalStatus === 'SUPER_ADMIN_APPROVED' || 
      approvalStatus === 'FINAL_APPROVED' ||
      approvalStatus === 'SUPER_ADMIN_REJECTED') && project.adminReviewerUsername) {
    return `${project.adminReviewerUsername}审核完成`
  }
  if (approvalStatus === 'SUPER_ADMIN_REVIEWING' || 
      approvalStatus === 'SUPER_ADMIN_APPROVED' || 
      approvalStatus === 'FINAL_APPROVED' ||
      approvalStatus === 'SUPER_ADMIN_REJECTED') return '审核完成'
  
  if (approvalStatus === 'ADMIN_REVIEWING') return '待审核'
  if (approvalStatus === 'AI_APPROVED') return '等待审核'
  return '等待AI分析'
}

const getSuperAdminStatus = (project: any) => {
  const approvalStatus = project.approvalStatus
  
  if (approvalStatus === 'SUPER_ADMIN_REJECTED' && project.superAdminReviewerUsername) {
    return `${project.superAdminReviewerUsername}已拒绝`
  }
  if (approvalStatus === 'SUPER_ADMIN_REJECTED') return '已拒绝'
  
  if ((approvalStatus === 'SUPER_ADMIN_APPROVED' || approvalStatus === 'FINAL_APPROVED') && project.superAdminReviewerUsername) {
    return `${project.superAdminReviewerUsername}已通过`
  }
  if (approvalStatus === 'SUPER_ADMIN_APPROVED' || approvalStatus === 'FINAL_APPROVED') return '已通过'
  
  if (approvalStatus === 'SUPER_ADMIN_REVIEWING') return '待审核'
  if (approvalStatus === 'ADMIN_REVIEWING' || approvalStatus === 'AI_APPROVED') return '等待审核'
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
  return 'text-red-600 font-semibold' // 很低置信度 - 红色
}

const approveProject = async (projectId: number, approved: boolean, customComment?: string) => {
  try {
    const comment = customComment || (approved ? '项目方案合理，同意进入下一步审核' : '项目方案需要修改，请重新提交')
    
    let endpoint, method, body
    if (approved) {
      endpoint = `/api/projects/${projectId}/final-approve`
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
      alert(approved ? '项目最终批准成功，已正式立项' : '项目已被最终拒绝')
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


const loadProjects = async (tabKey?: string) => {
  try {
    // 根据当前tab或传入的tabKey决定使用哪个接口
    const currentTab = tabKey || activeTab.value
    let endpoint = '/api/projects/pending-review' // 默认接口
    
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
import { watch } from 'vue'

watch(activeTab, (newTab) => {
  console.log(`Tab changed to: ${newTab}, reloading projects...`)
  loadProjects(newTab)
})

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.super-admin-projects {
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

.progress-step.rejected .step-marker {
  background: #ef4444;
  color: white;
}

.progress-step.rejected {
  background: #fef2f2;
  border-radius: 6px;
  padding: 12px;
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

.project-phases h4 {
  margin: 0 0 12px 0;
  color: #374151;
  font-size: 1rem;
  font-weight: 600;
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

.phase-details {
  display: flex;
  gap: 16px;
}

.phase-date {
  color: #9ca3af;
  font-size: 0.75rem;
}
</style>