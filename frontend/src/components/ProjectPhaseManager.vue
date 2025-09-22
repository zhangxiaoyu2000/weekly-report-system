<template>
  <div class="project-phase-manager">
    <div class="header">
      <h3>📋 项目阶段管理</h3>
      <div class="header-actions">
        <select v-model="selectedProjectId" @change="loadPhases" class="project-select">
          <option value="">选择项目</option>
          <option v-for="project in projects" :key="project.id" :value="project.id">
            {{ project.projectName }}
          </option>
        </select>
        <button @click="showCreateForm = true" :disabled="!selectedProjectId" class="create-btn">
          ➕ 新建阶段
        </button>
      </div>
    </div>

    <!-- 创建阶段表单 -->
    <div v-if="showCreateForm" class="form-section">
      <h4>{{ editingPhase ? '编辑阶段' : '创建新阶段' }}</h4>
      <form @submit.prevent="savePhase" class="phase-form">
        <div class="form-row">
          <div class="form-group">
            <label>阶段名称 *</label>
            <input 
              v-model="form.phaseName" 
              type="text" 
              required 
              placeholder="请输入阶段名称"
            >
          </div>
          <div class="form-group">
            <label>阶段顺序</label>
            <input 
              v-model.number="form.phaseOrder" 
              type="number" 
              min="1"
              placeholder="自动排序"
            >
          </div>
        </div>

        <div class="form-group">
          <label>阶段描述</label>
          <textarea 
            v-model="form.phaseDescription" 
            placeholder="请描述此阶段的具体内容和目标"
            rows="3"
          ></textarea>
        </div>

        <div class="form-group">
          <label>分配成员</label>
          <input 
            v-model="form.assignedMembers" 
            type="text" 
            placeholder="负责此阶段的团队成员"
          >
        </div>

        <div class="form-group">
          <label>时间线</label>
          <textarea 
            v-model="form.timeline" 
            placeholder="此阶段的时间安排"
            rows="2"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>关键指标</label>
            <textarea 
              v-model="form.keyIndicators" 
              placeholder="此阶段的关键性指标"
              rows="2"
            ></textarea>
          </div>
          <div class="form-group">
            <label>预期结果</label>
            <textarea 
              v-model="form.estimatedResults" 
              placeholder="此阶段的预期结果"
              rows="2"
            ></textarea>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>开始日期</label>
            <input 
              v-model="form.startDate" 
              type="date"
            >
          </div>
          <div class="form-group">
            <label>结束日期</label>
            <input 
              v-model="form.endDate" 
              type="date"
            >
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? '保存中...' : (editingPhase ? '更新阶段' : '创建阶段') }}
          </button>
          <button type="button" @click="cancelForm" :disabled="isLoading">
            取消
          </button>
        </div>
      </form>
    </div>

    <!-- 阶段列表 -->
    <div v-if="selectedProjectId && !showCreateForm" class="phases-section">
      <div class="phases-header">
        <h4>{{ selectedProject?.projectName }} - 项目阶段</h4>
        <div class="status-filters">
          <button 
            v-for="filter in statusFilters" 
            :key="filter.value"
            @click="statusFilter = filter.value; loadPhases()"
            :class="['filter-btn', { active: statusFilter === filter.value }]"
          >
            {{ filter.label }}
          </button>
        </div>
      </div>

      <div v-if="phases.length > 0" class="phases-list">
        <div 
          v-for="(phase, index) in phases" 
          :key="phase.id"
          :class="['phase-card', getStatusClass(phase.status)]"
        >
          <div class="phase-header">
            <div class="phase-title">
              <span class="phase-order">{{ phase.phaseOrder || (index + 1) }}</span>
              <h5>{{ phase.phaseName }}</h5>
              <span :class="['status-badge', getStatusClass(phase.status)]">
                {{ getStatusText(phase.status) }}
              </span>
            </div>
            <div class="phase-actions">
              <button 
                @click="editPhase(phase)" 
                class="action-btn edit"
                :disabled="phase.status === 'COMPLETED'"
              >
                ✏️ 编辑
              </button>
              <select 
                :value="phase.status" 
                @change="updatePhaseStatus(phase.id, $event.target.value)"
                class="status-select"
              >
                <option value="PENDING">待开始</option>
                <option value="IN_PROGRESS">进行中</option>
                <option value="COMPLETED">已完成</option>
                <option value="CANCELLED">已取消</option>
              </select>
              <button 
                @click="deletePhase(phase.id)" 
                class="action-btn delete"
                :disabled="phase.status === 'IN_PROGRESS'"
              >
                🗑️ 删除
              </button>
            </div>
          </div>

          <div class="phase-content">
            <div class="info-grid">
              <div v-if="phase.phaseDescription" class="info-item">
                <strong>描述：</strong>{{ phase.phaseDescription }}
              </div>
              <div v-if="phase.assignedMembers" class="info-item">
                <strong>负责成员：</strong>{{ phase.assignedMembers }}
              </div>
              <div v-if="phase.timeline" class="info-item">
                <strong>时间线：</strong>{{ phase.timeline }}
              </div>
              <div v-if="phase.keyIndicators" class="info-item">
                <strong>关键指标：</strong>{{ phase.keyIndicators }}
              </div>
              <div v-if="phase.estimatedResults" class="info-item">
                <strong>预期结果：</strong>{{ phase.estimatedResults }}
              </div>
              <div v-if="phase.actualResults" class="info-item actual-results">
                <strong>实际结果：</strong>{{ phase.actualResults }}
              </div>
            </div>

            <div v-if="phase.startDate || phase.endDate || phase.completionDate" class="dates-info">
              <small class="date-item" v-if="phase.startDate">
                开始：{{ formatDate(phase.startDate) }}
              </small>
              <small class="date-item" v-if="phase.endDate">
                计划结束：{{ formatDate(phase.endDate) }}
              </small>
              <small class="date-item" v-if="phase.completionDate">
                实际完成：{{ formatDate(phase.completionDate) }}
              </small>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p v-if="!selectedProjectId">请先选择一个项目</p>
        <p v-else>该项目暂无阶段数据</p>
        <small v-if="selectedProjectId">为项目创建第一个阶段开始管理</small>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { projectPhaseAPI, simpleProjectAPI, type ProjectPhase, type SimpleProject } from '../services/api'

// 响应式数据
const isLoading = ref(false)
const showCreateForm = ref(false)
const editingPhase = ref<ProjectPhase | null>(null)
const selectedProjectId = ref<number | string>('')
const statusFilter = ref('ALL')
const projects = ref<SimpleProject[]>([])
const phases = ref<ProjectPhase[]>([])

// 表单数据
const form = reactive({
  phaseName: '',
  phaseDescription: '',
  assignedMembers: '',
  timeline: '',
  keyIndicators: '',
  estimatedResults: '',
  startDate: '',
  endDate: '',
  phaseOrder: null as number | null
})

// 状态过滤器
const statusFilters = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待开始' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

// 计算属性
const selectedProject = computed(() => {
  return projects.value.find(p => p.id === Number(selectedProjectId.value))
})

// 加载项目列表
const loadProjects = async () => {
  try {
    const response = await simpleProjectAPI.list('APPROVED')
    if (response.success) {
      projects.value = response.data
    }
  } catch (error) {
    console.error('Load projects error:', error)
  }
}

// 加载项目阶段
const loadPhases = async () => {
  if (!selectedProjectId.value) {
    phases.value = []
    return
  }

  try {
    const status = statusFilter.value === 'ALL' ? undefined : statusFilter.value
    const response = await projectPhaseAPI.getByProject(Number(selectedProjectId.value), status)
    if (response.success) {
      phases.value = response.data
    }
  } catch (error) {
    console.error('Load phases error:', error)
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    phaseName: '',
    phaseDescription: '',
    assignedMembers: '',
    timeline: '',
    keyIndicators: '',
    estimatedResults: '',
    startDate: '',
    endDate: '',
    phaseOrder: null
  })
}

// 显示创建表单
const showCreateFormHandler = () => {
  resetForm()
  editingPhase.value = null
  showCreateForm.value = true
}

// 取消表单
const cancelForm = () => {
  showCreateForm.value = false
  editingPhase.value = null
  resetForm()
}

// 编辑阶段
const editPhase = (phase: ProjectPhase) => {
  editingPhase.value = phase
  Object.assign(form, {
    phaseName: phase.phaseName,
    phaseDescription: phase.phaseDescription || '',
    assignedMembers: phase.assignedMembers || '',
    timeline: phase.timeline || '',
    keyIndicators: phase.keyIndicators || '',
    estimatedResults: phase.estimatedResults || '',
    startDate: phase.startDate || '',
    endDate: phase.endDate || '',
    phaseOrder: phase.phaseOrder
  })
  showCreateForm.value = true
}

// 保存阶段
const savePhase = async () => {
  isLoading.value = true
  
  try {
    const phaseData = {
      ...form,
      projectId: Number(selectedProjectId.value)
    }

    let response
    if (editingPhase.value) {
      response = await projectPhaseAPI.update(editingPhase.value.id, phaseData)
    } else {
      response = await projectPhaseAPI.create(phaseData)
    }
    
    if (response.success) {
      alert(editingPhase.value ? '✅ 阶段更新成功！' : '✅ 阶段创建成功！')
      cancelForm()
      await loadPhases()
    } else {
      alert('❌ 操作失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  } finally {
    isLoading.value = false
  }
}

// 更新阶段状态
const updatePhaseStatus = async (phaseId: number, status: string) => {
  try {
    let actualResults = undefined
    if (status === 'COMPLETED') {
      actualResults = prompt('请输入实际结果（可选）：')
    }
    
    const response = await projectPhaseAPI.updateStatus(phaseId, status, actualResults || undefined)
    if (response.success) {
      alert('✅ 状态更新成功！')
      await loadPhases()
    } else {
      alert('❌ 状态更新失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  }
}

// 删除阶段
const deletePhase = async (phaseId: number) => {
  if (!confirm('确定要删除此阶段吗？此操作不可恢复。')) {
    return
  }

  try {
    const response = await projectPhaseAPI.delete(phaseId)
    if (response.success) {
      alert('✅ 阶段删除成功！')
      await loadPhases()
    } else {
      alert('❌ 删除失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  }
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  const statusClasses = {
    'PENDING': 'pending',
    'IN_PROGRESS': 'in-progress',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled'
  }
  return statusClasses[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusTexts = {
    'PENDING': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusTexts[status] || status
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

// 组件挂载
onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.project-phase-manager {
  max-width: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.project-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  font-size: 14px;
  min-width: 200px;
}

.create-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.create-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 表单区域 */
.form-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.form-section h4 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 16px;
}

.phase-form {
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

/* 阶段区域 */
.phases-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.phases-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  flex-wrap: wrap;
  gap: 15px;
}

.phases-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.status-filters {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 12px;
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

/* 阶段列表 */
.phases-list {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.phase-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.phase-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.phase-card.completed {
  opacity: 0.8;
  border-color: #28a745;
}

.phase-card.in-progress {
  border-color: #007bff;
}

.phase-card.pending {
  border-color: #ffc107;
}

.phase-card.cancelled {
  opacity: 0.6;
  border-color: #dc3545;
}

.phase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.phase-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.phase-order {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #007bff;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.phase-title h5 {
  margin: 0;
  color: #333;
  font-size: 16px;
  flex: 1;
}

.status-badge {
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
}

.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.in-progress { background: #d1ecf1; color: #0c5460; }
.status-badge.completed { background: #d4edda; color: #155724; }
.status-badge.cancelled { background: #f8d7da; color: #721c24; }

.phase-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.edit {
  background: #007bff;
  color: white;
}

.action-btn.delete {
  background: #dc3545;
  color: white;
}

.action-btn:hover:not(:disabled) {
  opacity: 0.8;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.status-select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
}

.phase-content {
  padding: 15px;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  font-size: 14px;
  line-height: 1.4;
}

.info-item strong {
  color: #555;
  display: inline-block;
  min-width: 80px;
}

.actual-results {
  background: #e8f4fd;
  padding: 8px;
  border-radius: 4px;
}

.dates-info {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.date-item {
  color: #666;
  font-size: 12px;
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
  .header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .project-select {
    min-width: auto;
    flex: 1;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .phases-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .status-filters {
    justify-content: center;
  }
  
  .phase-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .phase-actions {
    justify-content: center;
  }
  
  .dates-info {
    flex-direction: column;
    gap: 5px;
  }
}
</style>