<template>
  <div class="approval-history bg-white rounded-lg shadow-sm border p-6">
    <h3 class="text-lg font-semibold mb-4 text-gray-900 flex items-center">
      <span class="mr-2">📋</span>
      审批历史
    </h3>

    <div class="relative">
      <!-- 时间轴线 -->
      <div class="absolute left-6 top-0 bottom-0 w-0.5 bg-gray-200"></div>

      <!-- 时间轴节点 -->
      <div class="space-y-6">
        <!-- 创建记录 -->
        <div class="relative flex items-start">
          <div class="flex-shrink-0 w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center text-white font-semibold relative z-10">
            📝
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">周报创建</div>
            <div class="text-sm text-gray-500">
              由 {{ report.authorName }} 创建
            </div>
            <div class="text-xs text-gray-400">{{ formatTime(report.createdAt) }}</div>
          </div>
        </div>

        <!-- 提交记录 -->
        <div v-if="report.submittedAt" class="relative flex items-start">
          <div class="flex-shrink-0 w-12 h-12 bg-green-500 rounded-full flex items-center justify-center text-white font-semibold relative z-10">
            ✅
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">周报提交</div>
            <div class="text-sm text-gray-500">提交进入审批流程</div>
            <div class="text-xs text-gray-400">{{ formatTime(report.submittedAt) }}</div>
          </div>
        </div>

        <!-- AI分析记录 -->
        <div v-if="report.aiAnalyzedAt" class="relative flex items-start">
          <div 
            class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold relative z-10"
            :class="getAIAnalysisIconClass()"
          >
            🤖
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">AI质量分析</div>
            <div class="text-sm text-gray-500">
              质量评分: {{ report.aiQualityScore ? report.aiQualityScore.toFixed(1) : 'N/A' }}/10
              <span class="ml-2">风险等级: {{ getRiskLevelText() }}</span>
            </div>
            <div v-if="report.aiAnalysisResult" class="text-sm text-gray-600 mt-1 bg-gray-100 rounded p-2 max-w-md">
              {{ report.aiAnalysisResult.substring(0, 100) }}{{ report.aiAnalysisResult.length > 100 ? '...' : '' }}
            </div>
            <div class="text-xs text-gray-400">{{ formatTime(report.aiAnalyzedAt) }}</div>
          </div>
        </div>

        <!-- 主管审核记录 -->
        <div v-if="report.managerReviewedAt" class="relative flex items-start">
          <div 
            class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold relative z-10"
            :class="getManagerReviewIconClass()"
          >
            👤
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">主管审核</div>
            <div class="text-sm text-gray-500">
              审核人: {{ report.managerReviewerName || '未知' }}
            </div>
            <div v-if="report.managerReviewComment" class="text-sm text-gray-600 mt-1 bg-gray-100 rounded p-2 max-w-md">
              {{ report.managerReviewComment }}
            </div>
            <div class="text-xs text-gray-400">{{ formatTime(report.managerReviewedAt) }}</div>
          </div>
        </div>

        <!-- 管理员审核记录 -->
        <div v-if="report.adminReviewedAt" class="relative flex items-start">
          <div 
            class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold relative z-10"
            :class="getAdminReviewIconClass()"
          >
            🛡️
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">管理员审核</div>
            <div class="text-sm text-gray-500">
              审核人: {{ report.adminReviewerName || '未知' }}
            </div>
            <div v-if="report.adminReviewComment" class="text-sm text-gray-600 mt-1 bg-gray-100 rounded p-2 max-w-md">
              {{ report.adminReviewComment }}
            </div>
            <div class="text-xs text-gray-400">{{ formatTime(report.adminReviewedAt) }}</div>
          </div>
        </div>

        <!-- 超级管理员审核记录 -->
        <div v-if="report.superAdminReviewedAt" class="relative flex items-start">
          <div 
            class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold relative z-10"
            :class="getSuperAdminReviewIconClass()"
          >
            👑
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">超级管理员审核</div>
            <div class="text-sm text-gray-500">
              审核人: {{ report.superAdminReviewerName || '未知' }}
            </div>
            <div v-if="report.superAdminReviewComment" class="text-sm text-gray-600 mt-1 bg-gray-100 rounded p-2 max-w-md">
              {{ report.superAdminReviewComment }}
            </div>
            <div class="text-xs text-gray-400">{{ formatTime(report.superAdminReviewedAt) }}</div>
          </div>
        </div>

        <!-- 最终状态 -->
        <div v-if="isFinalStatus" class="relative flex items-start">
          <div 
            class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold relative z-10"
            :class="getFinalStatusIconClass()"
          >
            {{ getFinalStatusIcon() }}
          </div>
          <div class="ml-4 min-w-0 flex-1">
            <div class="text-sm font-medium text-gray-900">{{ getFinalStatusTitle() }}</div>
            <div class="text-sm text-gray-500">{{ getFinalStatusDescription() }}</div>
            <div class="text-xs text-gray-400">{{ formatTime(report.updatedAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ApprovalHistoryTimeline',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  computed: {
    isFinalStatus() {
      return ['APPROVED', 'PUBLISHED', 'MANAGER_REJECTED', 'ADMIN_REJECTED', 'SUPER_ADMIN_REJECTED'].includes(this.report.status);
    }
  },
  methods: {
    getAIAnalysisIconClass() {
      if (this.report.aiQualityScore && this.report.aiQualityScore >= 7.0) {
        return 'bg-green-500';
      } else if (this.report.aiQualityScore && this.report.aiQualityScore >= 5.0) {
        return 'bg-yellow-500';
      }
      return 'bg-red-500';
    },
    
    getManagerReviewIconClass() {
      if (this.report.status === 'MANAGER_REJECTED') return 'bg-red-500';
      return 'bg-green-500';
    },
    
    getAdminReviewIconClass() {
      if (this.report.status === 'ADMIN_REJECTED') return 'bg-red-500';
      return 'bg-green-500';
    },
    
    getSuperAdminReviewIconClass() {
      if (this.report.status === 'SUPER_ADMIN_REJECTED') return 'bg-red-500';
      return 'bg-green-500';
    },
    
    getFinalStatusIconClass() {
      if (['APPROVED', 'PUBLISHED'].includes(this.report.status)) return 'bg-green-500';
      return 'bg-red-500';
    },
    
    getFinalStatusIcon() {
      if (['APPROVED', 'PUBLISHED'].includes(this.report.status)) return '🎉';
      return '❌';
    },
    
    getFinalStatusTitle() {
      const statusMap = {
        'APPROVED': '审批完成',
        'PUBLISHED': '正式发布',
        'MANAGER_REJECTED': '主管拒绝',
        'ADMIN_REJECTED': '管理员拒绝',
        'SUPER_ADMIN_REJECTED': '超级管理员拒绝'
      };
      
      return statusMap[this.report.status] || '流程结束';
    },
    
    getFinalStatusDescription() {
      if (['APPROVED', 'PUBLISHED'].includes(this.report.status)) {
        return '周报已通过所有审核，流程圆满完成';
      }
      return '周报被拒绝，可查看审核意见后重新提交';
    },
    
    getRiskLevelText() {
      const risk = this.report.aiRiskLevel;
      const riskMap = {
        'CRITICAL': '严重',
        'HIGH': '高风险',
        'MEDIUM': '中等',
        'LOW': '低风险'
      };
      
      return riskMap[risk] || '未知';
    },
    
    formatTime(dateTime) {
      if (!dateTime) return '';
      return new Date(dateTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
}
</script>

<style scoped>
.approval-history {
  max-height: 600px;
  overflow-y: auto;
}

.relative .absolute {
  content: '';
}

/* 时间轴样式优化 */
.approval-step {
  transition: all 0.3s ease;
}

.approval-step:hover {
  transform: translateY(-2px);
}
</style>