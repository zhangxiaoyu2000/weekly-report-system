<template>
  <div class="project-manager">
    <!-- 项目创建表单 -->
    <div class="form-section">
      <h3>📋 创建新项目</h3>
      <form @submit.prevent="createProject" class="project-form">
        <div class="form-row">
          <div class="form-group">
            <label>项目名称 *</label>
            <input 
              v-model="form.projectName" 
              type="text" 
              required 
              placeholder="请输入项目名称"
            >
          </div>
        </div>

        <div class="form-group">
          <label>项目内容 *</label>
          <textarea 
            v-model="form.projectContent" 
            required 
            placeholder="请描述项目的具体内容和目标"
            rows="3"
          ></textarea>
        </div>

        <div class="form-group">
          <label>项目成员 *</label>
          <textarea 
            v-model="form.projectMembers" 
            required 
            placeholder="请列出项目团队成员"
            rows="2"
          ></textarea>
        </div>

        <div class="form-group">
          <label>关键性指标 *</label>
          <textarea 
            v-model="form.keyIndicators" 
            required 
            placeholder="请定义项目的关键性指标"
            rows="2"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>预期结果 *</label>
            <textarea 
              v-model="form.expectedResults" 
              required 
              placeholder="请描述项目的预期结果"
              rows="2"
            ></textarea>
          </div>
          <div class="form-group">
            <label>实际结果</label>
            <textarea 
              v-model="form.actualResults" 
              placeholder="可选：目前的实际结果"
              rows="2"
            ></textarea>
          </div>
        </div>

        <div class="form-group">
          <label>时间线 *</label>
          <textarea 
            v-model="form.timeline" 
            required 
            placeholder="请制定项目时间线"
            rows="2"
          ></textarea>
        </div>

        <div class="form-group">
          <label>止损点 *</label>
          <textarea 
            v-model="form.stopLoss" 
            required 
            placeholder="请定义项目的止损点"
            rows="2"
          ></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? '创建中...' : '🚀 创建项目' }}
          </button>
          <button type="button" @click="resetForm" :disabled="isLoading">
            清空表单
          </button>
        </div>
      </form>
    </div>

    <!-- 项目列表 -->
    <div class="list-section">
      <div class="list-header">
        <h3>📋 我的项目列表</h3>
        <div class="filter-buttons">
          <button 
            v-for="filter in statusFilters" 
            :key="filter.value"
            @click="currentFilter = filter.value; loadProjects()"
            :class="['filter-btn', { active: currentFilter === filter.value }]"
          >
            {{ filter.label }}
          </button>
          <button @click="loadProjects()" class="refresh-btn">
            🔄 刷新
          </button>
        </div>
      </div>

      <!-- 项目卡片 -->
      <div class="projects-grid" v-if="projects.length > 0">
        <div 
          v-for="project in projects" 
          :key="project.id" 
          class="project-card"
        >
          <div class="card-header">
            <h4>{{ project.projectName }}</h4>
            <span :class="['status-badge', getStatusClass(project.status)]">
              {{ getStatusText(project.status) }}
            </span>
          </div>
          
          <div class="card-content">
            <p><strong>内容：</strong>{{ truncateText(project.projectContent, 100) }}</p>
            <p><strong>成员：</strong>{{ truncateText(project.projectMembers, 60) }}</p>
            <p><strong>预期结果：</strong>{{ truncateText(project.expectedResults, 80) }}</p>
            <p class="create-time">
              创建时间：{{ formatDate(project.createdAt) }}
            </p>
            
            <!-- AI分析结果 -->
            <div v-if="project.aiAnalysisResult" class="ai-result">
              <strong>AI分析：</strong>{{ project.aiAnalysisResult }}
            </div>
          </div>

          <div class="card-actions">
            <button 
              v-if="project.status === 'PENDING_AI_ANALYSIS'" 
              @click="submitForApproval(project.id)"
              class="action-btn primary"
              :disabled="actionLoading"
            >
              📤 提交审批
            </button>
            
            <template v-if="project.status === 'PENDING_APPROVAL'">
              <button 
                @click="approveProject(project.id, true)"
                class="action-btn success"
                :disabled="actionLoading"
              >
                ✅ 审批通过
              </button>
              <button 
                @click="approveProject(project.id, false)"
                class="action-btn danger"
                :disabled="actionLoading"
              >
                ❌ 审批拒绝
              </button>
            </template>

            <button 
              v-if="project.status === 'APPROVED'"
              @click="viewReports(project.id)"
              class="action-btn info"
            >
              📊 查看周报
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>暂无项目数据</p>
        <small>创建您的第一个项目开始使用系统</small>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { projectService } from '../services/api'

// Props and Emits
const emit = defineEmits(['project-created', 'project-updated'])

// 响应式数据
const isLoading = ref(false)
const actionLoading = ref(false)
const projects = ref([])
const currentFilter = ref('ALL')

// 表单数据
const form = reactive({
  projectName: '',
  projectContent: '',
  projectMembers: '',
  keyIndicators: '',
  expectedResults: '',
  actualResults: '',
  timeline: '',
  stopLoss: ''
})

// 状态过滤器
const statusFilters = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING_AI_ANALYSIS', label: '待AI分析' },
  { value: 'PENDING_APPROVAL', label: '待审批' },
  { value: 'APPROVED', label: '已审批' },
  { value: 'AI_REJECTED', label: 'AI不合格' }
]

// 创建项目
const createProject = async () => {
  isLoading.value = true
  
  try {
    const response = await projectService.createProject(form)
    
    if (response.success) {
      alert('✅ 项目创建成功！AI正在分析中...')
      resetForm()
      await loadProjects()
      emit('project-created', response.data)
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
  Object.keys(form).forEach(key => {
    form[key] = ''
  })
}

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await projectService.getMyProjects(currentFilter.value)
    
    if (response.success) {
      projects.value = response.data
    }
  } catch (error) {
    console.error('Load projects error:', error)
  }
}

// 提交审批
const submitForApproval = async (projectId: number) => {
  actionLoading.value = true
  
  try {
    const response = await projectService.submitForApproval(projectId)
    
    if (response.success) {
      alert('✅ 项目已提交审批')
      await loadProjects()
      emit('project-updated')
    } else {
      alert('❌ 提交失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  } finally {
    actionLoading.value = false
  }
}

// 审批项目
const approveProject = async (projectId: number, approved: boolean) => {
  actionLoading.value = true
  
  try {
    const response = await projectService.approveProject(projectId, approved)
    
    if (response.success) {
      alert(approved ? '✅ 项目审批通过' : '❌ 项目审批拒绝')
      await loadProjects()
      emit('project-updated')
    } else {
      alert('❌ 审批失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  } finally {
    actionLoading.value = false
  }
}

// 查看周报
const viewReports = (projectId: number) => {
  // 这里可以触发事件让父组件切换到周报页面
  emit('view-reports', projectId)
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  const statusClasses = {
    'PENDING_AI_ANALYSIS': 'pending',
    'AI_REJECTED': 'rejected',
    'PENDING_APPROVAL': 'approval',
    'APPROVED': 'approved'
  }
  return statusClasses[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusTexts = {
    'PENDING_AI_ANALYSIS': '待AI分析',
    'AI_REJECTED': 'AI不合格',
    'PENDING_APPROVAL': '待审批',
    'APPROVED': '审批通过'
  }
  return statusTexts[status] || status
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

// 组件挂载
onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-manager {
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

.project-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
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

.form-group input,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group input:focus,
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
  background: #007bff;
  color: white;
  flex: 1;
}

.form-actions button[type="submit"]:hover:not(:disabled) {
  background: #0056b3;
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
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  flex-wrap: wrap;
  gap: 10px;
}

.list-header h3 {
  color: #333;
  margin: 0;
  font-size: 18px;
}

.filter-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 16px;
  background: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.filter-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.refresh-btn {
  padding: 6px 12px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

/* 项目网格 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  padding: 20px;
}

.project-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.project-card:hover {
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

.status-badge.pending { background: #ffeaa7; color: #636e72; }
.status-badge.rejected { background: #fab1a0; color: #2d3436; }
.status-badge.approval { background: #74b9ff; color: white; }
.status-badge.approved { background: #00b894; color: white; }

.card-content {
  padding: 15px;
}

.card-content p {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.4;
}

.create-time {
  color: #999;
  font-size: 12px !important;
  margin-top: 10px;
}

.ai-result {
  background: #e8f4fd;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
  font-size: 13px;
}

.card-actions {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.primary { background: #007bff; color: white; }
.action-btn.success { background: #28a745; color: white; }
.action-btn.danger { background: #dc3545; color: white; }
.action-btn.info { background: #17a2b8; color: white; }

.action-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .list-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .filter-buttons {
    justify-content: center;
  }
  
  .projects-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .card-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    text-align: center;
  }
}
</style>