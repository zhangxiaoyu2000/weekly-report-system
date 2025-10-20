<template>
  <div class="approval-actions bg-white rounded-lg shadow-sm border p-6">
    <h3 class="text-lg font-semibold mb-4 text-gray-900">审批操作</h3>

    <!-- 当前用户可执行的操作 -->
    <div v-if="availableActions.length > 0" class="space-y-4">
      <!-- 审核操作 -->
      <div v-if="canReview" class="border rounded-lg p-4">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm font-medium text-gray-900">
            {{ getCurrentReviewTitle() }}
          </span>
          <span class="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded">
            {{ getCurrentUserRoleText() }}
          </span>
        </div>
        
        <!-- 审核意见输入 -->
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            审核意见 <span class="text-red-500">*</span>
          </label>
          <textarea
            v-model="reviewComment"
            rows="3"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="请输入审核意见..."
            :disabled="isProcessing"
          ></textarea>
        </div>
        
        <!-- 审核按钮 -->
        <div class="flex gap-3">
          <button
            @click="handleApproval(true)"
            :disabled="!reviewComment.trim() || isProcessing"
            class="flex-1 bg-green-500 text-white py-2 px-4 rounded-lg hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span v-if="isProcessing && pendingAction === 'approve'">处理中...</span>
            <span v-else>✅ 通过</span>
          </button>
          <button
            @click="handleApproval(false)"
            :disabled="!reviewComment.trim() || isProcessing"
            class="flex-1 bg-red-500 text-white py-2 px-4 rounded-lg hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span v-if="isProcessing && pendingAction === 'reject'">处理中...</span>
            <span v-else>❌ 拒绝</span>
          </button>
        </div>
      </div>

      <!-- AI分析操作 -->
      <div v-if="canTriggerAI" class="border rounded-lg p-4">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm font-medium text-gray-900">AI质量分析</span>
          <span class="text-xs text-gray-500 bg-blue-100 px-2 py-1 rounded">
            智能分析
          </span>
        </div>
        <p class="text-sm text-gray-600 mb-3">
          系统将使用AI分析周报内容质量，包括完整性、具体性和可执行性等维度。
        </p>
        <button
          @click="triggerAI"
          :disabled="isProcessing"
          class="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <span v-if="isProcessing && pendingAction === 'ai'">🤖 分析中...</span>
          <span v-else>🤖 开始AI分析</span>
        </button>
      </div>

      <!-- 重新提交操作 -->
      <div v-if="canResubmit" class="border rounded-lg p-4 bg-yellow-50 border-yellow-200">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm font-medium text-yellow-800">重新提交</span>
          <span class="text-xs text-yellow-600 bg-yellow-100 px-2 py-1 rounded">
            修改后重提
          </span>
        </div>
        <p class="text-sm text-yellow-700 mb-3">
          周报被拒绝，您可以根据审核意见进行修改后重新提交。
        </p>
        <button
          @click="goToEdit"
          class="w-full bg-yellow-500 text-white py-2 px-4 rounded-lg hover:bg-yellow-600 transition-colors"
        >
          📝 修改并重新提交
        </button>
      </div>
    </div>

    <!-- 无可用操作时的提示 -->
    <div v-else class="text-center py-8">
      <div class="text-gray-400 mb-2">
        <svg class="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z">
          </path>
        </svg>
      </div>
      <p class="text-gray-500">当前状态下没有可执行的操作</p>
      <p class="text-xs text-gray-400 mt-1">{{ getNoActionReason() }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReportApprovalActions',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      reviewComment: '',
      isProcessing: false,
      pendingAction: ''
    }
  },
  computed: {
    currentUser() {
      return this.$store.state.auth.user;
    },
    
    userRole() {
      return this.currentUser?.role;
    },
    
    canReview() {
      const status = this.report.status;
      const role = this.userRole;
      
      // 主管审核权限
      if (status === 'PENDING_MANAGER_REVIEW' && ['MANAGER', 'ADMIN', 'SUPER_ADMIN'].includes(role)) {
        return true;
      }
      
      // 管理员审核权限
      if (status === 'PENDING_ADMIN_REVIEW' && ['ADMIN', 'SUPER_ADMIN'].includes(role)) {
        return true;
      }
      
      // 超级管理员审核权限
      if (status === 'PENDING_SUPER_ADMIN_REVIEW' && role === 'SUPER_ADMIN') {
        return true;
      }
      
      return false;
    },
    
    canTriggerAI() {
      const status = this.report.status;
      const role = this.userRole;
      
      return ['MANAGER', 'ADMIN', 'SUPER_ADMIN'].includes(role) &&
             ['SUBMITTED', 'PENDING_MANAGER_REVIEW'].includes(status) &&
             !this.report.aiAnalyzedAt;
    },
    
    canResubmit() {
      const status = this.report.status;
      const isAuthor = this.currentUser?.id === this.report.authorId;
      
      return isAuthor && ['MANAGER_REJECTED', 'ADMIN_REJECTED', 'SUPER_ADMIN_REJECTED'].includes(status);
    },
    
    availableActions() {
      const actions = [];
      if (this.canReview) actions.push('review');
      if (this.canTriggerAI) actions.push('ai');
      if (this.canResubmit) actions.push('resubmit');
      return actions;
    }
  },
  methods: {
    getCurrentReviewTitle() {
      const status = this.report.status;
      
      if (status === 'PENDING_MANAGER_REVIEW') return '主管审核';
      if (status === 'PENDING_ADMIN_REVIEW') return '管理员审核';
      if (status === 'PENDING_SUPER_ADMIN_REVIEW') return '超级管理员审核';
      
      return '审核';
    },
    
    getCurrentUserRoleText() {
      const roleMap = {
        'MANAGER': '主管',
        'ADMIN': '管理员',
        'SUPER_ADMIN': '超级管理员'
      };
      
      return roleMap[this.userRole] || '用户';
    },
    
    async handleApproval(approved) {
      if (!this.reviewComment.trim()) {
        this.$message.warning('请输入审核意见');
        return;
      }
      
      this.isProcessing = true;
      this.pendingAction = approved ? 'approve' : 'reject';
      
      try {
        const endpoint = this.getReviewEndpoint();
        const response = await fetch(`/api/reports/${this.report.id}/${endpoint}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.$store.state.auth.token}`
          },
          body: JSON.stringify({
            approved: approved,
            comment: this.reviewComment
          })
        });
        
        if (response.ok) {
          const result = await response.json();
          this.$message.success(result.message || '审核操作完成');
          this.$emit('approval-completed', result.data);
          this.reviewComment = '';
        } else {
          const error = await response.json();
          throw new Error(error.message || '审核操作失败');
        }
      } catch (error) {
        console.error('Approval error:', error);
        this.$message.error('审核操作失败: ' + error.message);
      } finally {
        this.isProcessing = false;
        this.pendingAction = '';
      }
    },
    
    getReviewEndpoint() {
      const status = this.report.status;
      
      if (status === 'PENDING_MANAGER_REVIEW') return 'manager-review';
      if (status === 'PENDING_ADMIN_REVIEW') return 'admin-review';
      if (status === 'PENDING_SUPER_ADMIN_REVIEW') return 'superadmin-review';
      
      throw new Error('无效的审核状态');
    },
    
    async triggerAI() {
      this.isProcessing = true;
      this.pendingAction = 'ai';
      
      try {
        const response = await fetch(`/api/reports/${this.report.id}/trigger-ai-analysis`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.$store.state.auth.token}`
          }
        });
        
        if (response.ok) {
          this.$message.success('AI分析已启动，请稍后刷新查看结果');
          this.$emit('ai-triggered');
        } else {
          const error = await response.json();
          throw new Error(error.message || 'AI分析启动失败');
        }
      } catch (error) {
        console.error('AI trigger error:', error);
        this.$message.error('启动AI分析失败: ' + error.message);
      } finally {
        this.isProcessing = false;
        this.pendingAction = '';
      }
    },
    
    goToEdit() {
      this.$router.push(`/reports/${this.report.id}/edit`);
    },
    
    getNoActionReason() {
      const status = this.report.status;
      const role = this.userRole;
      
      if (status === 'AI_ANALYZING') return '周报还在草稿状态';
      if (status === 'AI_ANALYZING') return 'AI分析进行中，请稍候';
      if (status === 'APPROVED') return '周报已获得最终批准';
      if (status === 'PUBLISHED') return '周报已正式发布';
      
      if (!['MANAGER', 'ADMIN', 'SUPER_ADMIN'].includes(role)) {
        return '您没有审核权限';
      }
      
      return '当前状态下没有可执行的操作';
    }
  }
}
</script>

<style scoped>
.approval-actions {
  min-height: 200px;
}

button:disabled {
  cursor: not-allowed;
}

textarea:focus {
  outline: none;
}
</style>