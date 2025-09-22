<template>
  <div class="container mx-auto px-4 py-6">
    <div v-if="loading" class="text-center py-8">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-gray-600">加载中...</p>
    </div>

    <div v-else-if="error" class="text-center py-8">
      <div class="text-red-500 mb-4">❌ {{ error }}</div>
      <button 
        @click="loadReport" 
        class="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600"
      >
        重新加载
      </button>
    </div>

    <div v-else-if="report" class="max-w-6xl mx-auto">
      <!-- 头部信息 -->
      <div class="bg-white rounded-lg shadow-sm border p-6 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ report.title }}</h1>
            <div class="flex items-center gap-4 text-sm text-gray-600">
              <span>📅 {{ formatWeek(report.reportWeek) }}</span>
              <span>✍️ {{ report.authorName }}</span>
              <span>📊 {{ report.wordCount }} 字</span>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span 
              class="px-3 py-1 rounded-full text-sm font-medium"
              :class="getStatusBadgeClass(report.status)"
            >
              {{ getStatusText(report.status) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 三个主要组件 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- 审批进度 -->
        <ReportApprovalProgress 
          :report="report" 
          class="lg:col-span-2"
        />
        
        <!-- AI分析结果 -->
        <AIAnalysisResult 
          :report="report"
          @ai-analysis-triggered="handleAITriggered"
        />
        
        <!-- 审批操作 -->
        <ReportApprovalActions
          :report="report"
          @approval-completed="handleApprovalCompleted"
          @ai-triggered="handleAITriggered"
        />
      </div>

      <!-- 周报内容 -->
      <div class="bg-white rounded-lg shadow-sm border p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4 text-gray-900">周报内容</h3>
        
        <div class="space-y-6">
          <div v-if="report.workSummary">
            <h4 class="font-medium text-gray-900 mb-2">工作总结</h4>
            <div class="text-gray-700 whitespace-pre-wrap bg-gray-50 rounded p-3">{{ report.workSummary }}</div>
          </div>
          
          <div v-if="report.achievements">
            <h4 class="font-medium text-gray-900 mb-2">主要成果</h4>
            <div class="text-gray-700 whitespace-pre-wrap bg-gray-50 rounded p-3">{{ report.achievements }}</div>
          </div>
          
          <div v-if="report.challenges">
            <h4 class="font-medium text-gray-900 mb-2">遇到挑战</h4>
            <div class="text-gray-700 whitespace-pre-wrap bg-gray-50 rounded p-3">{{ report.challenges }}</div>
          </div>
          
          <div v-if="report.nextWeekPlan">
            <h4 class="font-medium text-gray-900 mb-2">下周计划</h4>
            <div class="text-gray-700 whitespace-pre-wrap bg-gray-50 rounded p-3">{{ report.nextWeekPlan }}</div>
          </div>
          
          <div v-if="report.additionalNotes">
            <h4 class="font-medium text-gray-900 mb-2">备注说明</h4>
            <div class="text-gray-700 whitespace-pre-wrap bg-gray-50 rounded p-3">{{ report.additionalNotes }}</div>
          </div>
          
          <div v-if="report.content">
            <h4 class="font-medium text-gray-900 mb-2">详细内容</h4>
            <div class="text-gray-700 whitespace-pre-wrap bg-gray-50 rounded p-3">{{ report.content }}</div>
          </div>
        </div>
      </div>

      <!-- 审批历史 -->
      <ApprovalHistoryTimeline :report="report" />
    </div>

    <!-- 返回按钮 -->
    <div class="fixed bottom-6 right-6">
      <button
        @click="goBack"
        class="bg-gray-500 text-white px-6 py-3 rounded-full shadow-lg hover:bg-gray-600 transition-colors"
      >
        ← 返回列表
      </button>
    </div>
  </div>
</template>

<script>
import ReportApprovalProgress from '@/components/ReportApprovalProgress.vue'
import AIAnalysisResult from '@/components/AIAnalysisResult.vue'
import ApprovalHistoryTimeline from '@/components/ApprovalHistoryTimeline.vue'
import ReportApprovalActions from '@/components/ReportApprovalActions.vue'

export default {
  name: 'ReportDetailView',
  components: {
    ReportApprovalProgress,
    AIAnalysisResult,
    ApprovalHistoryTimeline,
    ReportApprovalActions
  },
  data() {
    return {
      report: null,
      loading: false,
      error: null
    }
  },
  async mounted() {
    await this.loadReport()
  },
  methods: {
    async loadReport() {
      this.loading = true
      this.error = null
      
      try {
        const reportId = this.$route.params.id
        const response = await fetch(`/api/reports/${reportId}`, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.auth.token}`
          }
        })
        
        if (response.ok) {
          const result = await response.json()
          // 直接使用API返回的数据，显示主管提交的原始内容
          this.report = result.data
        } else {
          throw new Error('加载周报详情失败')
        }
      } catch (error) {
        console.error('Load report error:', error)
        this.error = error.message
      } finally {
        this.loading = false
      }
    },
    
    async handleApprovalCompleted(updatedReport) {
      // 更新本地数据
      this.report = updatedReport
      
      // 可以选择重新加载完整数据
      await this.loadReport()
    },
    
    async handleAITriggered() {
      this.$message.info('AI分析已启动，页面将在5秒后自动刷新')
      
      // 5秒后自动刷新
      setTimeout(() => {
        this.loadReport()
      }, 5000)
    },
    
    getStatusBadgeClass(status) {
      const statusClasses = {
        'DRAFT': 'bg-gray-100 text-gray-800',
        'SUBMITTED': 'bg-blue-100 text-blue-800',
        'AI_ANALYZING': 'bg-purple-100 text-purple-800',
        'PENDING_MANAGER_REVIEW': 'bg-yellow-100 text-yellow-800',
        'MANAGER_REJECTED': 'bg-red-100 text-red-800',
        'PENDING_ADMIN_REVIEW': 'bg-orange-100 text-orange-800',
        'ADMIN_REJECTED': 'bg-red-100 text-red-800',
        'PENDING_SUPER_ADMIN_REVIEW': 'bg-indigo-100 text-indigo-800',
        'SUPER_ADMIN_REJECTED': 'bg-red-100 text-red-800',
        'APPROVED': 'bg-green-100 text-green-800',
        'PUBLISHED': 'bg-emerald-100 text-emerald-800'
      }
      
      return statusClasses[status] || 'bg-gray-100 text-gray-800'
    },
    
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'SUBMITTED': '已提交',
        'AI_ANALYZING': 'AI分析中',
        'PENDING_MANAGER_REVIEW': '待主管审核',
        'MANAGER_REJECTED': '主管拒绝',
        'PENDING_ADMIN_REVIEW': '待管理员审核',
        'ADMIN_REJECTED': '管理员拒绝',
        'PENDING_SUPER_ADMIN_REVIEW': '待超管审核',
        'SUPER_ADMIN_REJECTED': '超管拒绝',
        'APPROVED': '已批准',
        'PUBLISHED': '已发布'
      }
      
      return statusMap[status] || '未知状态'
    },
    
    formatWeek(date) {
      if (!date) return ''
      const d = new Date(date)
      return d.toLocaleDateString('zh-CN') + ' 周'
    },
    
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.container {
  min-height: calc(100vh - 64px);
}
</style>