<template>
  <div class="task-list">
    <!-- 任务清单标题 -->
    <div class="header-section">
      <h3>✅ 任务清单（已审批项目）</h3>
      <p class="subtitle">这里显示所有审批通过的项目，您可以为它们创建周报</p>
    </div>

    <!-- 统计信息 -->
    <div class="stats-section" v-if="approvedProjects.length > 0">
      <div class="stat-card">
        <div class="stat-number">{{ approvedProjects.length }}</div>
        <div class="stat-label">已审批项目</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ totalReports }}</div>
        <div class="stat-label">总周报数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ activeProjects }}</div>
        <div class="stat-label">活跃项目</div>
      </div>
    </div>

    <!-- 项目卡片列表 -->
    <div class="projects-section">
      <div class="section-header">
        <h4>📋 项目列表</h4>
        <div class="view-options">
          <button 
            @click="viewMode = 'grid'"
            :class="['view-btn', { active: viewMode === 'grid' }]"
          >
            🔲 网格
          </button>
          <button 
            @click="viewMode = 'list'"
            :class="['view-btn', { active: viewMode === 'list' }]"
          >
            📋 列表
          </button>
        </div>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="projects-grid">
        <div 
          v-for="project in approvedProjects" 
          :key="project.id"
          class="project-card grid-card"
        >
          <div class="card-header">
            <h4>{{ project.projectName }}</h4>
            <span class="status-badge approved">已审批</span>
          </div>

          <div class="card-content">
            <div class="project-meta">
              <p><strong>📝 内容：</strong>{{ truncateText(project.projectContent, 80) }}</p>
              <p><strong>👥 成员：</strong>{{ truncateText(project.projectMembers, 60) }}</p>
              <p><strong>📊 关键指标：</strong>{{ truncateText(project.keyIndicators, 70) }}</p>
            </div>

            <div class="project-progress">
              <div class="progress-item">
                <span>预期结果：</span>
                <p>{{ truncateText(project.expectedResults, 80) }}</p>
              </div>
              
              <div v-if="project.actualResults" class="progress-item">
                <span>实际结果：</span>
                <p>{{ truncateText(project.actualResults, 80) }}</p>
              </div>
            </div>

            <div class="timeline-info">
              <p><strong>⏰ 时间线：</strong>{{ truncateText(project.timeline, 60) }}</p>
              <p><strong>🛑 止损点：</strong>{{ truncateText(project.stopLoss, 60) }}</p>
            </div>
          </div>

          <div class="card-footer">
            <div class="project-date">
              创建于：{{ formatDate(project.createdAt) }}
            </div>
            <div class="card-actions">
              <button 
                @click="viewProjectReports(project.id)" 
                class="action-btn primary"
              >
                📊 查看周报 ({{ getReportCount(project.id) }})
              </button>
              <button 
                @click="createReportForProject(project.id)" 
                class="action-btn success"
              >
                📝 创建周报
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="projects-list">
        <div 
          v-for="project in approvedProjects" 
          :key="project.id"
          class="project-item list-item"
        >
          <div class="item-main">
            <div class="item-header">
              <h4>{{ project.projectName }}</h4>
              <div class="item-meta">
                <span class="status-badge approved">已审批</span>
                <span class="date-info">{{ formatDate(project.createdAt) }}</span>
              </div>
            </div>

            <div class="item-content">
              <p class="project-description">{{ project.projectContent }}</p>
              
              <div class="project-details">
                <div class="detail-item">
                  <strong>👥 成员：</strong>{{ project.projectMembers }}
                </div>
                <div class="detail-item">
                  <strong>📊 关键指标：</strong>{{ project.keyIndicators }}
                </div>
                <div class="detail-item">
                  <strong>⏰ 时间线：</strong>{{ project.timeline }}
                </div>
              </div>
            </div>
          </div>

          <div class="item-actions">
            <button 
              @click="viewProjectReports(project.id)" 
              class="action-btn primary"
            >
              📊 周报 ({{ getReportCount(project.id) }})
            </button>
            <button 
              @click="createReportForProject(project.id)" 
              class="action-btn success"
            >
              📝 新建
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="approvedProjects.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h4>暂无已审批的项目</h4>
        <p>创建项目并通过AI分析和审批后，项目会出现在这里</p>
        <button @click="$emit('switch-to-projects')" class="create-project-btn">
          🚀 去创建项目
        </button>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="activity-section" v-if="recentReports.length > 0">
      <h4>📈 最近活动</h4>
      <div class="activity-list">
        <div 
          v-for="report in recentReports" 
          :key="report.id"
          class="activity-item"
        >
          <div class="activity-icon">📊</div>
          <div class="activity-content">
            <div class="activity-title">
              提交了 <strong>{{ report.project.projectName }}</strong> 的周报
            </div>
            <div class="activity-time">{{ formatDateTime(report.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { reportService } from '../services/api'

// Props
const props = defineProps<{
  approvedProjects: any[]
}>()

// Emits
const emit = defineEmits(['view-reports', 'create-report', 'switch-to-projects'])

// 响应式数据
const viewMode = ref('grid')
const recentReports = ref([])
const reportCounts = ref({})

// 计算属性
const totalReports = computed(() => {
  return Object.values(reportCounts.value).reduce((sum, count) => sum + count, 0)
})

const activeProjects = computed(() => {
  return props.approvedProjects.filter(project => 
    reportCounts.value[project.id] > 0
  ).length
})

// 查看项目周报
const viewProjectReports = (projectId: number) => {
  emit('view-reports', projectId)
}

// 为项目创建周报
const createReportForProject = (projectId: number) => {
  emit('create-report', projectId)
}

// 获取项目的周报数量
const getReportCount = (projectId: number) => {
  return reportCounts.value[projectId] || 0
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

// 加载周报数据
const loadReportsData = async () => {
  try {
    const response = await reportService.getMyReports()
    
    if (response.success) {
      const reports = response.data
      
      // 更新周报数量统计
      const counts = {}
      reports.forEach(report => {
        const projectId = report.project.id
        counts[projectId] = (counts[projectId] || 0) + 1
      })
      reportCounts.value = counts
      
      // 获取最近的5条周报
      recentReports.value = reports.slice(0, 5)
    }
  } catch (error) {
    console.error('Load reports data error:', error)
  }
}

// 组件挂载
onMounted(() => {
  loadReportsData()
})
</script>

<style scoped>
.task-list {
  max-width: 100%;
}

/* 头部区域 */
.header-section {
  text-align: center;
  margin-bottom: 30px;
}

.header-section h3 {
  color: #333;
  font-size: 24px;
  margin-bottom: 10px;
}

.subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

/* 统计区域 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 项目区域 */
.projects-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.section-header h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.view-options {
  display: flex;
  gap: 5px;
}

.view-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.view-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.view-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

/* 网格视图 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  padding: 20px;
}

.grid-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.grid-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.card-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
  flex: 1;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  margin-left: 10px;
}

.status-badge.approved { 
  background: #d4edda; 
  color: #155724; 
}

.card-content {
  padding: 15px;
}

.project-meta,
.project-progress,
.timeline-info {
  margin-bottom: 15px;
}

.project-meta p,
.timeline-info p {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.4;
}

.progress-item {
  margin-bottom: 10px;
}

.progress-item span {
  font-weight: 600;
  color: #555;
  font-size: 13px;
  display: block;
  margin-bottom: 3px;
}

.progress-item p {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.card-footer {
  padding: 15px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.project-date {
  color: #666;
  font-size: 12px;
  margin-bottom: 10px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  flex: 1;
}

.action-btn.primary { 
  background: #007bff; 
  color: white; 
}

.action-btn.success { 
  background: #28a745; 
  color: white; 
}

.action-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 列表视图 */
.projects-list {
  padding: 20px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: start;
  padding: 20px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.list-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.item-main {
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  margin-bottom: 10px;
}

.item-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-info {
  color: #666;
  font-size: 12px;
}

.project-description {
  color: #555;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 10px;
}

.project-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 8px;
}

.detail-item {
  font-size: 13px;
  color: #666;
}

.detail-item strong {
  color: #333;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 20px;
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.empty-state h4 {
  color: #333;
  margin-bottom: 10px;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 14px;
}

.create-project-btn {
  padding: 12px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.create-project-btn:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

/* 活动区域 */
.activity-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
}

.activity-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.activity-icon {
  font-size: 18px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
}

.activity-title {
  font-size: 14px;
  color: #333;
}

.activity-time {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: 1fr;
  }
  
  .projects-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .view-options {
    justify-content: center;
  }
  
  .list-item {
    flex-direction: column;
    gap: 15px;
  }
  
  .item-actions {
    flex-direction: row;
    margin-left: 0;
    width: 100%;
  }
  
  .project-details {
    grid-template-columns: 1fr;
  }
  
  .card-actions {
    flex-direction: column;
  }
}
</style>