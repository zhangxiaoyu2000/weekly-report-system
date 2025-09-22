<template>
  <div class="approval-history bg-gray-50 dark:bg-gray-900 min-h-screen">
    <div class="header">
      <h1>审批历史</h1>
      <p class="subtitle">查看所有项目和周报的审批记录</p>
    </div>

    <div class="filters">
      <div class="filter-group">
        <label>类型</label>
        <select v-model="filterType">
          <option value="all">全部</option>
          <option value="project">项目</option>
          <option value="report">周报</option>
        </select>
      </div>
      <div class="filter-group">
        <label>状态</label>
        <select v-model="filterStatus">
          <option value="all">全部状态</option>
          <option value="approved">已批准</option>
          <option value="rejected">已拒绝</option>
          <option value="pending">待审批</option>
        </select>
      </div>
      <div class="filter-group">
        <label>时间范围</label>
        <select v-model="filterTime">
          <option value="all">全部时间</option>
          <option value="week">最近一周</option>
          <option value="month">最近一月</option>
          <option value="quarter">最近三月</option>
        </select>
      </div>
    </div>

    <div class="content">
      <div v-if="filteredHistory.length === 0" class="no-data">
        暂无审批记录
      </div>
      <div v-else class="history-list">
        <div 
          v-for="item in filteredHistory" 
          :key="`${item.type}-${item.id}`"
          class="history-card"
        >
          <div class="card-header">
            <div class="title-section">
              <h3>{{ item.title || item.projectName }}</h3>
              <span class="type-badge">{{ item.type === 'project' ? '项目' : '周报' }}</span>
            </div>
            <span :class="['status', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </span>
          </div>
          
          <div class="card-content">
            <div class="timeline">
              <div class="timeline-item">
                <div class="timeline-marker submitted"></div>
                <div class="timeline-content">
                  <h4>提交</h4>
                  <p>{{ formatDate(item.createdAt) }}</p>
                  <p class="submitter">提交人: {{ item.authorName }}</p>
                </div>
              </div>

              <div v-if="item.managerReviewer" class="timeline-item">
                <div class="timeline-marker manager"></div>
                <div class="timeline-content">
                  <h4>主管审核</h4>
                  <p>{{ formatDate(item.managerReviewedAt) }}</p>
                  <p class="reviewer">审核人: {{ item.managerReviewer }}</p>
                  <p v-if="item.managerReviewComment" class="comment">{{ item.managerReviewComment }}</p>
                </div>
              </div>

              <div v-if="item.aiAnalysisResult" class="timeline-item">
                <div class="timeline-marker ai"></div>
                <div class="timeline-content">
                  <h4>AI分析</h4>
                  <p class="ai-result">{{ item.aiAnalysisResult }}</p>
                </div>
              </div>

              <div v-if="item.adminReviewer" class="timeline-item">
                <div class="timeline-marker admin"></div>
                <div class="timeline-content">
                  <h4>管理员审核</h4>
                  <p>{{ formatDate(item.adminReviewedAt) }}</p>
                  <p class="reviewer">审核人: {{ item.adminReviewer }}</p>
                  <p v-if="item.adminReviewComment" class="comment">{{ item.adminReviewComment }}</p>
                </div>
              </div>

              <div v-if="item.superAdminReviewer" class="timeline-item">
                <div class="timeline-marker super-admin"></div>
                <div class="timeline-content">
                  <h4>超级管理员审核</h4>
                  <p>{{ formatDate(item.superAdminReviewedAt) }}</p>
                  <p class="reviewer">审核人: {{ item.superAdminReviewer }}</p>
                  <p v-if="item.superAdminReviewComment" class="comment">{{ item.superAdminReviewComment }}</p>
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
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { weeklyReportAPI } from '@/services/api'

const authStore = useAuthStore()

const filterType = ref('all')
const filterStatus = ref('all')
const filterTime = ref('all')

const allHistory = ref([])

const filteredHistory = computed(() => {
  let filtered = [...allHistory.value]
  
  if (filterType.value !== 'all') {
    filtered = filtered.filter(item => item.type === filterType.value)
  }
  
  if (filterStatus.value !== 'all') {
    filtered = filtered.filter(item => {
      if (filterStatus.value === 'approved') {
        return item.status === 'APPROVED'
      } else if (filterStatus.value === 'rejected') {
        return item.status.includes('REJECTED')
      } else if (filterStatus.value === 'pending') {
        return item.status.includes('PENDING')
      }
      return true
    })
  }
  
  if (filterTime.value !== 'all') {
    const now = new Date()
    const timeLimit = new Date()
    
    switch (filterTime.value) {
      case 'week':
        timeLimit.setDate(now.getDate() - 7)
        break
      case 'month':
        timeLimit.setMonth(now.getMonth() - 1)
        break
      case 'quarter':
        timeLimit.setMonth(now.getMonth() - 3)
        break
    }
    
    filtered = filtered.filter(item => new Date(item.createdAt) >= timeLimit)
  }
  
  return filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
})

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUBMITTED': '已提交',
    'PENDING_MANAGER_REVIEW': '待主管审核',
    'MANAGER_REJECTED': '主管拒绝',
    'AI_ANALYZING': 'AI分析中',
    'PENDING_ADMIN_REVIEW': '待管理员审核',
    'ADMIN_REJECTED': '管理员拒绝',
    'PENDING_SUPER_ADMIN_REVIEW': '待超级管理员审核',
    'SUPER_ADMIN_REJECTED': '超级管理员拒绝',
    'APPROVED': '已批准',
    'PUBLISHED': '已发布'
  }
  return statusMap[status] || status
}

const getStatusClass = (status: string) => {
  if (status === 'APPROVED') return 'approved'
  if (status.includes('REJECTED')) return 'rejected'
  if (status.includes('PENDING')) return 'pending'
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

const loadHistory = async () => {
  try {
    // Load projects
    const projectsResponse = await fetch('/api/simple/projects', {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    const projects = []
    if (projectsResponse.ok) {
      const result = await projectsResponse.json()
      if (result.success) {
        projects.push(...result.data.map((project: any) => ({
          ...project,
          type: 'project'
        })))
      }
    }

    // Load reports using API service
    const reports = []
    try {
      const reportsResult = await weeklyReportAPI.list()
      if (reportsResult.success && reportsResult.data) {
        reports.push(...reportsResult.data.map((report: any) => ({
          ...report,
          type: 'report'
        })))
        console.log('加载到的周报数据:', reportsResult.data)
      }
    } catch (error) {
      console.error('加载周报失败:', error)
    }

    allHistory.value = [...projects, ...reports]
  } catch (error) {
    console.error('加载审批历史失败:', error)
  }
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.approval-history {
  padding: 24px;
  max-width: 1200px;
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

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.no-data {
  text-align: center;
  color: #6b7280;
  padding: 48px;
  font-size: 1.1rem;
}

.history-list {
  display: grid;
  gap: 16px;
}

.history-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.title-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.type-badge {
  background: #3b82f6;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status.approved {
  background: #d1fae5;
  color: #065f46;
}

.status.rejected {
  background: #fee2e2;
  color: #991b1b;
}

.status.pending {
  background: #fef3c7;
  color: #92400e;
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e5e7eb;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
}

.timeline-marker {
  position: absolute;
  left: -30px;
  top: 4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #e5e7eb;
}

.timeline-marker.submitted {
  background: #6b7280;
}

.timeline-marker.manager {
  background: #f59e0b;
}

.timeline-marker.ai {
  background: #3b82f6;
}

.timeline-marker.admin {
  background: #10b981;
}

.timeline-marker.super-admin {
  background: #8b5cf6;
}

.timeline-content h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.timeline-content p {
  margin: 2px 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.submitter, .reviewer {
  font-weight: 500;
  color: #374151;
}

.comment {
  background: #f3f4f6;
  padding: 8px;
  border-radius: 4px;
  margin-top: 4px;
  color: #374151 !important;
}

.ai-result {
  background: #eff6ff;
  padding: 8px;
  border-radius: 4px;
  color: #1e40af !important;
  font-weight: 500;
}
</style>