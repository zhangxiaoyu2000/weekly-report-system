<template>
  <div class="report-detail-page">
    <div class="page-header">
      <h1>周报详情</h1>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-card v-if="report">
      <div class="report-header">
        <h2>{{ report.title }}</h2>
        <el-tag :type="getStatusType(report.status)">
          {{ getStatusText(report.status) }}
        </el-tag>
      </div>
      
      <div class="report-meta">
        <p><strong>周次：</strong>{{ report.weekRange }}</p>
        <p><strong>创建时间：</strong>{{ formatDate(report.createdAt) }}</p>
        <p><strong>提交时间：</strong>{{ report.submittedAt ? formatDate(report.submittedAt) : '未提交' }}</p>
      </div>

      <div class="report-section">
        <h3>本周工作完成情况</h3>
        <div class="report-content" v-html="formatContent(report.workCompleted)"></div>
      </div>

      <div class="report-section">
        <h3>下周工作计划</h3>
        <div class="report-content" v-html="formatContent(report.workPlan)"></div>
      </div>

      <div class="report-section" v-if="report.problems">
        <h3>遇到的问题</h3>
        <div class="report-content" v-html="formatContent(report.problems)"></div>
      </div>

      <div class="report-section" v-if="report.assistance">
        <h3>需要协助的事项</h3>
        <div class="report-content" v-html="formatContent(report.assistance)"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const report = ref(null)

const getStatusType = (status) => {
  const statusMap = {
    draft: 'info',
    submitted: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    draft: '草稿',
    submitted: '已提交',
    approved: '已通过',
    rejected: '被拒绝'
  }
  return statusMap[status] || '未知'
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const formatContent = (content) => {
  return content ? content.replace(/\n/g, '<br>') : ''
}

onMounted(() => {
  // Mock report data
  report.value = {
    id: route.params.id,
    title: '2024年第36周工作周报',
    weekRange: '2024.09.02-2024.09.08',
    status: 'submitted',
    createdAt: '2024-09-02T10:00:00',
    submittedAt: '2024-09-02T18:00:00',
    workCompleted: `1. 完成了用户认证模块的开发\n2. 修复了数据导出功能的bug\n3. 参与了产品需求评审会议`,
    workPlan: `1. 开始开发周报管理功能\n2. 优化系统性能\n3. 编写单元测试`,
    problems: '在数据库查询优化方面遇到一些困难',
    assistance: '希望DBA同事协助优化慢查询问题'
  }
})
</script>

<style scoped>
.report-detail-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.report-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.report-meta {
  margin-bottom: 32px;
  color: #666;
}

.report-meta p {
  margin: 8px 0;
}

.report-section {
  margin-bottom: 32px;
}

.report-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.report-content {
  line-height: 1.6;
  color: #666;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  border-left: 4px solid #1890ff;
}
</style>