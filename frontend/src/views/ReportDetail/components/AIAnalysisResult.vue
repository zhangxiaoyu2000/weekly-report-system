<template>
  <div class="ai-analysis-result bg-white rounded-lg shadow-sm border p-6">
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-gray-900 flex items-center">
        <span class="mr-2">🤖</span>
        AI质量分析
      </h3>
      <div v-if="report.aiAnalyzedAt" class="text-sm text-gray-500">
        分析时间: {{ formatTime(report.aiAnalyzedAt) }}
      </div>
    </div>

    <!-- AI分析状态 -->
    <div v-if="!hasAIAnalysis" class="text-center py-8">
      <div class="text-gray-400 mb-2">
        <svg class="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z">
          </path>
        </svg>
      </div>
      <p class="text-gray-500">{{ getAIStatusText() }}</p>
      
      <!-- 手动触发AI分析按钮 -->
      <button
        v-if="canTriggerAI"
        @click="triggerAIAnalysis"
        :disabled="isTriggering"
        class="mt-4 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ isTriggering ? '分析中...' : '开始AI分析' }}
      </button>
    </div>

    <!-- AI分析结果 -->
    <div v-else class="space-y-4">
      <!-- 质量评分卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-blue-50 rounded-lg p-4 border border-blue-200">
          <div class="text-blue-800 text-sm font-medium">质量评分</div>
          <div class="text-2xl font-bold text-blue-900 mt-1">
            {{ report.aiQualityScore ? report.aiQualityScore.toFixed(1) : 'N/A' }}/10
          </div>
          <div class="text-blue-600 text-xs mt-1">{{ getQualityLevel() }}</div>
        </div>
        
        <div class="bg-purple-50 rounded-lg p-4 border border-purple-200">
          <div class="text-purple-800 text-sm font-medium">置信度</div>
          <div class="text-2xl font-bold text-purple-900 mt-1">
            {{ report.aiConfidence ? (report.aiConfidence * 100).toFixed(0) : 'N/A' }}%
          </div>
          <div class="text-purple-600 text-xs mt-1">AI分析可信度</div>
        </div>
        
        <div class="rounded-lg p-4 border" :class="getRiskLevelClass()">
          <div class="text-sm font-medium">风险等级</div>
          <div class="text-2xl font-bold mt-1">
            {{ getRiskLevelText() }}
          </div>
          <div class="text-xs mt-1">{{ getRiskDescription() }}</div>
        </div>
      </div>

      <!-- AI分析详情 -->
      <div v-if="report.aiAnalysisResult" class="bg-gray-50 rounded-lg p-4">
        <h4 class="font-medium text-gray-900 mb-2">分析详情</h4>
        <div class="text-gray-700 text-sm whitespace-pre-wrap">{{ report.aiAnalysisResult }}</div>
      </div>

      <!-- 关键问题 -->
      <div v-if="parsedKeyIssues.length > 0" class="bg-red-50 rounded-lg p-4 border border-red-200">
        <h4 class="font-medium text-red-800 mb-2 flex items-center">
          <span class="mr-2">⚠️</span>
          发现的问题
        </h4>
        <ul class="text-red-700 text-sm space-y-1">
          <li v-for="(issue, index) in parsedKeyIssues" :key="index" class="flex items-start">
            <span class="mr-2 text-red-500">•</span>
            {{ issue }}
          </li>
        </ul>
      </div>

      <!-- 改进建议 -->
      <div v-if="parsedRecommendations.length > 0" class="bg-green-50 rounded-lg p-4 border border-green-200">
        <h4 class="font-medium text-green-800 mb-2 flex items-center">
          <span class="mr-2">💡</span>
          改进建议
        </h4>
        <ul class="text-green-700 text-sm space-y-1">
          <li v-for="(recommendation, index) in parsedRecommendations" :key="index" class="flex items-start">
            <span class="mr-2 text-green-500">•</span>
            {{ recommendation }}
          </li>
        </ul>
      </div>

      <!-- 处理时间 -->
      <div v-if="report.aiProcessingTimeMs" class="text-xs text-gray-500 text-right">
        处理时间: {{ report.aiProcessingTimeMs }}ms | 
        服务商: {{ report.aiProviderUsed || 'DeepSeek' }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AIAnalysisResult',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      isTriggering: false
    }
  },
  computed: {
    hasAIAnalysis() {
      return this.report.aiAnalyzedAt && this.report.aiAnalysisResult;
    },
    
    canTriggerAI() {
      // 只有管理员及以上权限且周报处于合适状态时才能触发AI分析
      const userRole = this.$store.state.auth.user?.role;
      const allowedRoles = ['MANAGER', 'ADMIN', 'SUPER_ADMIN'];
      const allowedStatuses = ['SUBMITTED', 'PENDING_MANAGER_REVIEW'];
      
      return allowedRoles.includes(userRole) && 
             allowedStatuses.includes(this.report.status);
    },
    
    parsedKeyIssues() {
      if (!this.report.aiKeyIssues) return [];
      try {
        return JSON.parse(this.report.aiKeyIssues);
      } catch (e) {
        return [];
      }
    },
    
    parsedRecommendations() {
      if (!this.report.aiRecommendations) return [];
      try {
        return JSON.parse(this.report.aiRecommendations);
      } catch (e) {
        return [];
      }
    }
  },
  methods: {
    getAIStatusText() {
      if (this.report.status === 'AI_ANALYZING') return 'AI正在分析中...';
      if (!this.report.aiAnalyzedAt) return '尚未进行AI分析';
      return 'AI分析失败';
    },
    
    getQualityLevel() {
      const score = this.report.aiQualityScore;
      if (!score) return '';
      
      if (score >= 8.5) return '优秀';
      if (score >= 7.0) return '良好';
      if (score >= 5.5) return '合格';
      return '需改进';
    },
    
    getRiskLevelClass() {
      const risk = this.report.aiRiskLevel;
      
      if (risk === 'CRITICAL') return 'bg-red-100 border-red-300 text-red-800';
      if (risk === 'HIGH') return 'bg-orange-100 border-orange-300 text-orange-800';
      if (risk === 'MEDIUM') return 'bg-yellow-100 border-yellow-300 text-yellow-800';
      return 'bg-green-100 border-green-300 text-green-800';
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
    
    getRiskDescription() {
      const risk = this.report.aiRiskLevel;
      const descMap = {
        'CRITICAL': '需立即处理',
        'HIGH': '需重点关注',
        'MEDIUM': '建议优化',
        'LOW': '风险较小'
      };
      
      return descMap[risk] || '';
    },
    
    async triggerAIAnalysis() {
      this.isTriggering = true;
      
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
          // 可以emit事件通知父组件刷新数据
          this.$emit('ai-analysis-triggered');
        } else {
          throw new Error('启动AI分析失败');
        }
      } catch (error) {
        console.error('AI analysis trigger error:', error);
        this.$message.error('启动AI分析失败: ' + error.message);
      } finally {
        this.isTriggering = false;
      }
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