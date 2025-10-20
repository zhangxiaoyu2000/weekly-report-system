<template>
  <div class="approval-progress bg-white rounded-lg shadow-sm border p-6">
    <h3 class="text-lg font-semibold mb-4 text-gray-900">审批进度</h3>
    
    <div class="flex items-center justify-between relative">
      <!-- 进度线 -->
      <div class="absolute top-6 left-0 right-0 h-0.5 bg-gray-200 z-0"></div>
      <div 
        class="absolute top-6 left-0 h-0.5 bg-blue-500 z-0 transition-all duration-500"
        :style="{ width: progressPercentage + '%' }"
      ></div>

      <!-- AI分析步骤 -->
      <div class="approval-step flex flex-col items-center relative z-10">
        <div 
          class="step-circle w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold mb-2"
          :class="getStepClass('ai')"
        >
          <span v-if="getStepClass('ai').includes('completed')">✓</span>
          <span v-else-if="getStepClass('ai').includes('current')">🤖</span>
          <span v-else>1</span>
        </div>
        <div class="text-center">
          <div class="text-sm font-medium text-gray-900">AI分析</div>
          <div class="text-xs text-gray-500 mt-1">{{ getAIStepText() }}</div>
          <div v-if="report.aiAnalyzedAt" class="text-xs text-gray-400">
            {{ formatTime(report.aiAnalyzedAt) }}
          </div>
        </div>
      </div>

      <!-- 管理员审核步骤 -->
      <div class="approval-step flex flex-col items-center relative z-10">
        <div 
          class="step-circle w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold mb-2"
          :class="getStepClass('admin')"
        >
          <span v-if="getStepClass('admin').includes('completed')">✓</span>
          <span v-else-if="getStepClass('admin').includes('current')">🛡️</span>
          <span v-else>2</span>
        </div>
        <div class="text-center">
          <div class="text-sm font-medium text-gray-900">管理员审核</div>
          <div class="text-xs text-gray-500 mt-1">{{ getAdminStepText() }}</div>
          <div v-if="report.adminReviewedAt" class="text-xs text-gray-400">
            {{ formatTime(report.adminReviewedAt) }}
          </div>
        </div>
      </div>

      <!-- 超级管理员审核步骤 -->
      <div class="approval-step flex flex-col items-center relative z-10">
        <div 
          class="step-circle w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold mb-2"
          :class="getStepClass('superadmin')"
        >
          <span v-if="getStepClass('superadmin').includes('completed')">✓</span>
          <span v-else-if="getStepClass('superadmin').includes('current')">👑</span>
          <span v-else>3</span>
        </div>
        <div class="text-center">
          <div class="text-sm font-medium text-gray-900">超级管理员</div>
          <div class="text-xs text-gray-500 mt-1">{{ getSuperAdminStepText() }}</div>
          <div v-if="report.superAdminReviewedAt" class="text-xs text-gray-400">
            {{ formatTime(report.superAdminReviewedAt) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 当前状态说明 -->
    <div class="mt-6 p-4 rounded-lg" :class="getStatusBgClass()">
      <div class="flex items-center">
        <div class="text-sm font-medium" :class="getStatusTextClass()">
          {{ getStatusDescription() }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReportApprovalProgress',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  computed: {
    progressPercentage() {
      const status = this.report.status;
      
      if (['AI_ANALYZING'].includes(status)) return 0;
      if (['AI_ANALYZING'].includes(status)) return 30;
      if (['PENDING_ADMIN_REVIEW'].includes(status)) return 60;
      if (['ADMIN_REJECTED'].includes(status)) return 45;
      if (['PENDING_SUPER_ADMIN_REVIEW'].includes(status)) return 85;
      if (['SUPER_ADMIN_REJECTED'].includes(status)) return 70;
      if (['APPROVED', 'PUBLISHED'].includes(status)) return 100;
      
      return 0;
    }
  },
  methods: {
    getStepClass(step) {
      const status = this.report.status;
      
      switch (step) {
        case 'ai':
          if (this.report.aiAnalyzedAt) return 'bg-green-500 completed';
          if (status === 'AI_ANALYZING') return 'bg-blue-500 current';
          return 'bg-gray-300 pending';
          
        case 'admin':
          if (this.report.adminReviewedAt) return 'bg-green-500 completed';
          if (status === 'PENDING_ADMIN_REVIEW') return 'bg-blue-500 current';
          if (status === 'ADMIN_REJECTED') return 'bg-red-500 rejected';
          return 'bg-gray-300 pending';
          
        case 'superadmin':
          if (this.report.superAdminReviewedAt) return 'bg-green-500 completed';
          if (status === 'PENDING_SUPER_ADMIN_REVIEW') return 'bg-blue-500 current';
          if (status === 'SUPER_ADMIN_REJECTED') return 'bg-red-500 rejected';
          if (status === 'APPROVED' || status === 'PUBLISHED') return 'bg-green-500 completed';
          return 'bg-gray-300 pending';
          
        default:
          return 'bg-gray-300 pending';
      }
    },
    
    getAIStepText() {
      if (this.report.aiAnalyzedAt) {
        if (this.report.aiQualityScore) {
          return `质量评分: ${this.report.aiQualityScore}/10`;
        }
        return '分析完成';
      }
      if (this.report.status === 'AI_ANALYZING') return '分析中...';
      return '等待分析';
    },
    
    getManagerStepText() {
      if (this.report.managerReviewedAt) {
        return this.report.managerReviewerName || '已审核';
      }
      if (this.report.status === 'PENDING_MANAGER_REVIEW') return '等待审核';
      if (this.report.status === 'MANAGER_REJECTED') return '已拒绝';
      return '待审核';
    },
    
    getAdminStepText() {
      if (this.report.adminReviewedAt) {
        return this.report.adminReviewerName || '已审核';
      }
      if (this.report.status === 'PENDING_ADMIN_REVIEW') return '等待审核';
      if (this.report.status === 'ADMIN_REJECTED') return '已拒绝';
      return '待审核';
    },
    
    getSuperAdminStepText() {
      if (this.report.superAdminReviewedAt) {
        return this.report.superAdminReviewerName || '已审核';
      }
      if (this.report.status === 'PENDING_SUPER_ADMIN_REVIEW') return '等待审核';
      if (this.report.status === 'SUPER_ADMIN_REJECTED') return '已拒绝';
      if (this.report.status === 'APPROVED' || this.report.status === 'PUBLISHED') return '已批准';
      return '待审核';
    },
    
    getStatusDescription() {
      const status = this.report.status;
      const statusMap = {
        'DRAFT': '周报还在草稿状态，请完成后提交',
        'SUBMITTED': '周报已提交，准备进入AI分析',
        'AI_ANALYZING': 'AI正在分析周报质量，请稍候...',
        'PENDING_ADMIN_REVIEW': 'AI分析完成，等待管理员审核中',
        'ADMIN_REJECTED': '管理员审核未通过，请查看反馈意见后重新提交',
        'PENDING_SUPER_ADMIN_REVIEW': '管理员审核通过，等待超级管理员最终审核',
        'SUPER_ADMIN_REJECTED': '超级管理员审核未通过，请查看反馈意见后重新提交',
        'APPROVED': '所有审核已通过，周报获得批准',
        'PUBLISHED': '周报已正式发布'
      };
      
      return statusMap[status] || '状态未知';
    },
    
    getStatusBgClass() {
      const status = this.report.status;
      
      if (['ADMIN_REJECTED', 'SUPER_ADMIN_REJECTED'].includes(status)) {
        return 'bg-red-50 border border-red-200';
      }
      if (['PENDING_ADMIN_REVIEW', 'PENDING_SUPER_ADMIN_REVIEW'].includes(status)) {
        return 'bg-yellow-50 border border-yellow-200';
      }
      if (['AI_ANALYZING'].includes(status)) {
        return 'bg-blue-50 border border-blue-200';
      }
      if (['APPROVED', 'PUBLISHED'].includes(status)) {
        return 'bg-green-50 border border-green-200';
      }
      
      return 'bg-gray-50 border border-gray-200';
    },
    
    getStatusTextClass() {
      const status = this.report.status;
      
      if (['ADMIN_REJECTED', 'SUPER_ADMIN_REJECTED'].includes(status)) {
        return 'text-red-700';
      }
      if (['PENDING_ADMIN_REVIEW', 'PENDING_SUPER_ADMIN_REVIEW'].includes(status)) {
        return 'text-yellow-700';
      }
      if (['AI_ANALYZING'].includes(status)) {
        return 'text-blue-700';
      }
      if (['APPROVED', 'PUBLISHED'].includes(status)) {
        return 'text-green-700';
      }
      
      return 'text-gray-700';
    },
    
    formatTime(dateTime) {
      if (!dateTime) return '';
      return new Date(dateTime).toLocaleString('zh-CN');
    }
  }
}
</script>

<style scoped>
.step-circle {
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.step-circle.current {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.approval-step {
  min-width: 120px;
}
</style>