<template>
  <div class="report-manager">
    <!-- 周报创建表单 -->
    <div class="form-section">
      <h3>📊 创建周报</h3>
      <form @submit.prevent="createReport" class="report-form">
        <div class="form-group">
          <label>选择项目 *</label>
          <select v-model="form.projectId" required>
            <option value="">请选择已审批的项目</option>
            <option 
              v-for="project in approvedProjects" 
              :key="project.id" 
              :value="project.id"
            >
              {{ project.projectName }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>关键性指标 *</label>
          <textarea 
            v-model="form.keyIndicators" 
            required 
            placeholder="本周项目关键性指标情况"
            rows="4"
          ></textarea>
        </div>

        <div class="form-group">
          <label>实际结果 *</label>
          <textarea 
            v-model="form.actualResults" 
            required 
            placeholder="本周项目实际结果"
            rows="4"
          ></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="isLoading || !form.projectId">
            {{ isLoading ? '提交中...' : '📝 提交周报' }}
          </button>
          <button type="button" @click="resetForm" :disabled="isLoading">
            清空表单
          </button>
        </div>
      </form>
    </div>

    <!-- 周报列表 -->
    <div class="list-section">
      <div class="list-header">
        <h3>📄 我的周报列表</h3>
        <div class="actions">
          <button @click="loadReports()" class="refresh-btn">
            🔄 刷新
          </button>
        </div>
      </div>

      <!-- 周报卡片 -->
      <div class="reports-list" v-if="reports.length > 0">
        <div 
          v-for="report in reports" 
          :key="report.id" 
          class="report-card"
        >
          <div class="card-header">
            <div class="report-info">
              <h4>周报 #{{ report.id }}</h4>
              <span class="project-name">{{ report.project.projectName }}</span>
            </div>
            <div class="report-date">
              {{ formatDate(report.createdAt) }}
            </div>
          </div>
          
          <div class="card-content">
            <div class="content-section">
              <h5>📊 关键性指标</h5>
              <p>{{ report.keyIndicators }}</p>
            </div>
            
            <div class="content-section">
              <h5>📈 实际结果</h5>
              <p>{{ report.actualResults }}</p>
            </div>
          </div>

          <div class="card-footer">
            <small class="text-muted">
              创建时间：{{ formatDateTime(report.createdAt) }}
            </small>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>暂无周报数据</p>
        <small>为已审批的项目创建第一份周报</small>
      </div>
    </div>

    <!-- 按项目查看周报 -->
    <div class="project-reports-section" v-if="approvedProjects.length > 0">
      <h3>📋 按项目查看周报</h3>
      <div class="project-tabs">
        <button 
          v-for="project in approvedProjects" 
          :key="project.id"
          @click="loadProjectReports(project.id)"
          :class="['project-tab', { active: selectedProjectId === project.id }]"
        >
          {{ project.projectName }}
          <span class="report-count" v-if="projectReportCounts[project.id]">
            ({{ projectReportCounts[project.id] }})
          </span>
        </button>
      </div>

      <!-- 选中项目的周报 -->
      <div v-if="selectedProjectReports.length > 0" class="project-reports">
        <h4>{{ getSelectedProjectName() }} 的周报历史</h4>
        <div class="timeline">
          <div 
            v-for="report in selectedProjectReports" 
            :key="report.id"
            class="timeline-item"
          >
            <div class="timeline-marker"></div>
            <div class="timeline-content">
              <div class="timeline-header">
                <strong>周报 #{{ report.id }}</strong>
                <span class="timeline-date">{{ formatDate(report.createdAt) }}</span>
              </div>
              <div class="timeline-body">
                <p><strong>关键指标：</strong>{{ report.keyIndicators }}</p>
                <p><strong>实际结果：</strong>{{ report.actualResults }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { reportService, projectService } from '../services/api'

// Props
const props = defineProps<{
  approvedProjects: any[]
}>()

// Emits
const emit = defineEmits(['report-created'])

// 响应式数据
const isLoading = ref(false)
const reports = ref([])
const selectedProjectId = ref(null)
const selectedProjectReports = ref([])
const projectReportCounts = ref({})

// 表单数据
const form = reactive({
  projectId: '',
  keyIndicators: '',
  actualResults: ''
})

// 创建周报
const createReport = async () => {
  isLoading.value = true
  
  try {
    const reportData = {
      projectId: parseInt(form.projectId),
      keyIndicators: form.keyIndicators,
      actualResults: form.actualResults
    }
    
    const response = await reportService.createReport(reportData)
    
    if (response.success) {
      alert('✅ 周报创建成功！')
      resetForm()
      await loadReports()
      emit('report-created', response.data)
      
      // 如果当前选中了项目，重新加载该项目的周报
      if (selectedProjectId.value) {
        loadProjectReports(selectedProjectId.value)
      }
    } else {
      alert('❌ 创建失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  } finally {
    isLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.projectId = ''
  form.keyIndicators = ''
  form.actualResults = ''
}

// 加载所有周报
const loadReports = async () => {
  try {
    const response = await reportService.getMyReports()
    
    if (response.success) {
      reports.value = response.data
      updateProjectReportCounts()
    }
  } catch (error) {
    console.error('Load reports error:', error)
  }
}

// 加载特定项目的周报
const loadProjectReports = async (projectId: number) => {
  selectedProjectId.value = projectId
  
  try {
    const response = await projectService.getProjectReports(projectId)
    
    if (response.success) {
      selectedProjectReports.value = response.data
    }
  } catch (error) {
    console.error('Load project reports error:', error)
    selectedProjectReports.value = []
  }
}

// 更新项目周报数量
const updateProjectReportCounts = () => {
  const counts = {}
  reports.value.forEach(report => {
    const projectId = report.project.id
    counts[projectId] = (counts[projectId] || 0) + 1
  })
  projectReportCounts.value = counts
}

// 获取选中项目名称
const getSelectedProjectName = () => {
  if (!selectedProjectId.value) return ''
  const project = props.approvedProjects.find(p => p.id === selectedProjectId.value)
  return project ? project.projectName : ''
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

// 组件挂载
onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.report-manager {
  max-width: 100%;
}

/* 表单区域 */
.form-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.form-section h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 18px;
}

.report-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.form-actions button {
  padding: 12px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.form-actions button[type="submit"] {
  background: #28a745;
  color: white;
  flex: 1;
}

.form-actions button[type="submit"]:hover:not(:disabled) {
  background: #218838;
}

.form-actions button[type="button"] {
  background: #6c757d;
  color: white;
  flex: 0 0 auto;
}

.form-actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 列表区域 */
.list-section {
  background: white;
  border-radius: 8px;
  margin-bottom: 30px;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.list-header h3 {
  color: #333;
  margin: 0;
  font-size: 18px;
}

.refresh-btn {
  padding: 8px 16px;
  background: #17a2b8;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.refresh-btn:hover {
  background: #138496;
}

/* 周报卡片 */
.reports-list {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.report-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.report-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.report-info h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.project-name {
  color: #007bff;
  font-size: 14px;
  font-weight: 500;
}

.report-date {
  color: #666;
  font-size: 14px;
}

.card-content {
  padding: 15px;
}

.content-section {
  margin-bottom: 15px;
}

.content-section:last-child {
  margin-bottom: 0;
}

.content-section h5 {
  margin: 0 0 8px 0;
  color: #555;
  font-size: 14px;
}

.content-section p {
  margin: 0;
  line-height: 1.5;
  font-size: 14px;
}

.card-footer {
  padding: 10px 15px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.text-muted {
  color: #6c757d;
  font-size: 12px;
}

/* 按项目查看周报 */
.project-reports-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.project-reports-section h3 {
  padding: 20px;
  margin: 0;
  color: #333;
  font-size: 18px;
  border-bottom: 1px solid #eee;
}

.project-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.project-tab {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: white;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.project-tab:hover {
  border-color: #007bff;
  color: #007bff;
}

.project-tab.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.report-count {
  margin-left: 5px;
  font-size: 11px;
}

.project-reports {
  padding: 20px;
}

.project-reports h4 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 16px;
}

/* 时间线 */
.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #dee2e6;
}

.timeline-item {
  position: relative;
  padding-bottom: 25px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-marker {
  position: absolute;
  left: -12px;
  top: 5px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #007bff;
  border: 3px solid white;
  box-shadow: 0 0 0 2px #dee2e6;
}

.timeline-content {
  margin-left: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.timeline-header strong {
  color: #333;
  font-size: 14px;
}

.timeline-date {
  color: #666;
  font-size: 12px;
}

.timeline-body p {
  margin: 5px 0;
  font-size: 13px;
  line-height: 1.4;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 5px;
}

.empty-state small {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: start;
  }
  
  .project-tabs {
    flex-direction: column;
  }
  
  .project-tab {
    border-radius: 4px;
    text-align: center;
  }
  
  .timeline {
    padding-left: 15px;
  }
  
  .timeline-marker {
    left: -8px;
  }
  
  .timeline-content {
    margin-left: 15px;
  }
}
</style>