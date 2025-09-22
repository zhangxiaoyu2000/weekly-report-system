<template>
  <div class="routine-task-manager">
    <div class="task-header">
      <h2>日常任务管理</h2>
      <button @click="showCreateDialog = true" class="btn-primary">
        <i class="icon-plus"></i>
        创建任务
      </button>
    </div>

    <!-- 任务列表 -->
    <div class="task-list">
      <div class="task-filters">
        <select v-model="selectedTaskType" @change="loadTasks">
          <option value="">全部类型</option>
          <option value="DAILY">每日任务</option>
          <option value="WEEKLY">每周任务</option>
          <option value="MONTHLY">每月任务</option>
        </select>
        
        <select v-model="selectedStatus" @change="loadTasks">
          <option value="">全部状态</option>
          <option value="ACTIVE">激活</option>
          <option value="COMPLETED">已完成</option>
          <option value="INACTIVE">未激活</option>
        </select>
        
        <input 
          v-model="searchKeyword" 
          @input="searchTasks"
          type="text" 
          placeholder="搜索任务..."
          class="search-input"
        />
      </div>

      <div class="task-cards">
        <div 
          v-for="task in tasks" 
          :key="task.id" 
          class="task-card"
          :class="{ 
            'completed': task.status === 'COMPLETED',
            'inactive': task.status === 'INACTIVE' 
          }"
        >
          <div class="task-card-header">
            <h3>{{ task.taskName }}</h3>
            <div class="task-actions">
              <button 
                v-if="task.status === 'ACTIVE'" 
                @click="completeTask(task.id)"
                class="btn-success"
                title="完成任务"
              >
                <i class="icon-check"></i>
              </button>
              <button 
                v-if="task.status === 'COMPLETED'" 
                @click="activateTask(task.id)"
                class="btn-warning"
                title="重新激活"
              >
                <i class="icon-refresh"></i>
              </button>
              <button 
                @click="editTask(task)" 
                class="btn-info"
                title="编辑任务"
              >
                <i class="icon-edit"></i>
              </button>
              <button 
                @click="deleteTask(task.id)" 
                class="btn-danger"
                title="删除任务"
              >
                <i class="icon-trash"></i>
              </button>
            </div>
          </div>
          
          <div class="task-card-body">
            <p class="task-description">{{ task.taskDescription || '暂无描述' }}</p>
            
            <div class="task-meta">
              <span class="task-type">{{ getTaskTypeLabel(task.taskType) }}</span>
              <span class="task-priority" :class="`priority-${task.priority.toLowerCase()}`">
                {{ getPriorityLabel(task.priority) }}
              </span>
              <span class="task-status" :class="`status-${task.status.toLowerCase()}`">
                {{ getStatusLabel(task.status) }}
              </span>
            </div>
            
            <div class="task-dates">
              <small>创建时间: {{ formatDate(task.createdAt) }}</small>
              <small v-if="task.completedAt">完成时间: {{ formatDate(task.completedAt) }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage <= 0"
          class="btn-secondary"
        >
          上一页
        </button>
        
        <span class="page-info">
          第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页
        </span>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage >= totalPages - 1"
          class="btn-secondary"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 创建/编辑任务对话框 -->
    <div v-if="showCreateDialog || showEditDialog" class="modal-overlay" @click="closeDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showCreateDialog ? '创建任务' : '编辑任务' }}</h3>
          <button @click="closeDialog" class="btn-close">&times;</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="submitTask">
            <div class="form-group">
              <label for="taskName">任务名称 *</label>
              <input 
                id="taskName"
                v-model="taskForm.taskName" 
                type="text" 
                required
                maxlength="255"
                class="form-control"
              />
            </div>
            
            <div class="form-group">
              <label for="taskDescription">任务描述</label>
              <textarea 
                id="taskDescription"
                v-model="taskForm.taskDescription" 
                rows="3"
                class="form-control"
              ></textarea>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="taskType">任务类型 *</label>
                <select id="taskType" v-model="taskForm.taskType" required class="form-control">
                  <option value="DAILY">每日任务</option>
                  <option value="WEEKLY">每周任务</option>
                  <option value="MONTHLY">每月任务</option>
                </select>
              </div>
              
              <div class="form-group">
                <label for="priority">优先级</label>
                <select id="priority" v-model="taskForm.priority" class="form-control">
                  <option value="LOW">低</option>
                  <option value="NORMAL">普通</option>
                  <option value="HIGH">高</option>
                  <option value="URGENT">紧急</option>
                </select>
              </div>
            </div>
            
            <div class="form-group">
              <label for="expectedDuration">预期完成时间（分钟）</label>
              <input 
                id="expectedDuration"
                v-model.number="taskForm.expectedDuration" 
                type="number" 
                min="1"
                class="form-control"
              />
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeDialog" class="btn-secondary">
                取消
              </button>
              <button type="submit" class="btn-primary" :disabled="isSubmitting">
                {{ isSubmitting ? '保存中...' : (showCreateDialog ? '创建' : '更新') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useAuth } from '@/stores/auth'
import api from '@/services/api'

// 状态管理
const authStore = useAuth()
const tasks = ref([])
const loading = ref(false)
const isSubmitting = ref(false)

// 分页状态
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)

// 过滤状态
const selectedTaskType = ref('')
const selectedStatus = ref('')
const searchKeyword = ref('')

// 对话框状态
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingTask = ref(null)

// 表单数据
const taskForm = reactive({
  taskName: '',
  taskDescription: '',
  taskType: 'DAILY',
  priority: 'NORMAL',
  expectedDuration: null
})

// 生命周期
onMounted(() => {
  loadTasks()
})

// 监听器
watch([selectedTaskType, selectedStatus], () => {
  currentPage.value = 0
  loadTasks()
})

// 方法
const loadTasks = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: 'createdAt',
      sortDir: 'desc'
    }

    const response = await api.get('/routine-tasks', { params })
    
    if (response.data.success) {
      tasks.value = response.data.data.content
      totalPages.value = response.data.data.totalPages
    }
  } catch (error) {
    console.error('Failed to load tasks:', error)
    // TODO: Show error message
  } finally {
    loading.value = false
  }
}

const searchTasks = async () => {
  if (!searchKeyword.value.trim()) {
    loadTasks()
    return
  }

  try {
    const response = await api.get('/routine-tasks/search', {
      params: { keyword: searchKeyword.value }
    })
    
    if (response.data.success) {
      tasks.value = response.data.data
      totalPages.value = 1
      currentPage.value = 0
    }
  } catch (error) {
    console.error('Failed to search tasks:', error)
  }
}

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < totalPages.value) {
    currentPage.value = newPage
    loadTasks()
  }
}

const editTask = (task) => {
  editingTask.value = task
  taskForm.taskName = task.taskName
  taskForm.taskDescription = task.taskDescription || ''
  taskForm.taskType = task.taskType
  taskForm.priority = task.priority
  taskForm.expectedDuration = task.expectedDuration
  showEditDialog.value = true
}

const submitTask = async () => {
  try {
    isSubmitting.value = true
    
    if (showCreateDialog.value) {
      // 创建任务
      const response = await api.post('/routine-tasks', taskForm)
      if (response.data.success) {
        // TODO: Show success message
        loadTasks()
        closeDialog()
      }
    } else if (showEditDialog.value) {
      // 更新任务
      const response = await api.put(`/routine-tasks/${editingTask.value.id}`, taskForm)
      if (response.data.success) {
        // TODO: Show success message
        loadTasks()
        closeDialog()
      }
    }
  } catch (error) {
    console.error('Failed to submit task:', error)
    // TODO: Show error message
  } finally {
    isSubmitting.value = false
  }
}

const completeTask = async (taskId) => {
  try {
    const response = await api.post(`/routine-tasks/${taskId}/complete`)
    if (response.data.success) {
      // TODO: Show success message
      loadTasks()
    }
  } catch (error) {
    console.error('Failed to complete task:', error)
    // TODO: Show error message
  }
}

const activateTask = async (taskId) => {
  try {
    const response = await api.post(`/routine-tasks/${taskId}/activate`)
    if (response.data.success) {
      // TODO: Show success message
      loadTasks()
    }
  } catch (error) {
    console.error('Failed to activate task:', error)
    // TODO: Show error message
  }
}

const deleteTask = async (taskId) => {
  if (!confirm('确定要删除这个任务吗？')) {
    return
  }

  try {
    const response = await api.delete(`/routine-tasks/${taskId}`)
    if (response.data.success) {
      // TODO: Show success message
      loadTasks()
    }
  } catch (error) {
    console.error('Failed to delete task:', error)
    // TODO: Show error message
  }
}

const closeDialog = () => {
  showCreateDialog.value = false
  showEditDialog.value = false
  editingTask.value = null
  
  // 重置表单
  taskForm.taskName = ''
  taskForm.taskDescription = ''
  taskForm.taskType = 'DAILY'
  taskForm.priority = 'NORMAL'
  taskForm.expectedDuration = null
}

// 工具方法
const getTaskTypeLabel = (type) => {
  const labels = {
    'DAILY': '每日任务',
    'WEEKLY': '每周任务',
    'MONTHLY': '每月任务'
  }
  return labels[type] || type
}

const getPriorityLabel = (priority) => {
  const labels = {
    'LOW': '低',
    'NORMAL': '普通',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return labels[priority] || priority
}

const getStatusLabel = (status) => {
  const labels = {
    'ACTIVE': '激活',
    'COMPLETED': '已完成',
    'INACTIVE': '未激活'
  }
  return labels[status] || status
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.routine-task-manager {
  padding: 1rem;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.task-header h2 {
  margin: 0;
  color: #2c3e50;
}

.task-filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  align-items: center;
}

.task-filters select,
.search-input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
  flex: 1;
  max-width: 300px;
}

.task-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.task-card {
  background: white;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.task-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.task-card.completed {
  background-color: #f8f9fa;
  opacity: 0.8;
}

.task-card.inactive {
  background-color: #fff3cd;
}

.task-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.task-card-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #2c3e50;
  flex: 1;
}

.task-actions {
  display: flex;
  gap: 0.25rem;
}

.task-actions button {
  padding: 0.25rem 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
}

.task-description {
  color: #6c757d;
  margin-bottom: 0.75rem;
  font-size: 14px;
  line-height: 1.4;
}

.task-meta {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.task-meta span {
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.task-type {
  background-color: #e9ecef;
  color: #495057;
}

.priority-low { background-color: #d1ecf1; color: #0c5460; }
.priority-normal { background-color: #d4edda; color: #155724; }
.priority-high { background-color: #fff3cd; color: #856404; }
.priority-urgent { background-color: #f8d7da; color: #721c24; }

.status-active { background-color: #d4edda; color: #155724; }
.status-completed { background-color: #e2e3e5; color: #6c757d; }
.status-inactive { background-color: #fff3cd; color: #856404; }

.task-dates {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.task-dates small {
  color: #6c757d;
  font-size: 11px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.page-info {
  color: #6c757d;
  font-size: 14px;
}

/* 按钮样式 */
.btn-primary {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-secondary:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
}

.btn-warning {
  background-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #e0a800;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #dee2e6;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #000;
}

.modal-body {
  padding: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #2c3e50;
}

.form-control {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-cards {
    grid-template-columns: 1fr;
  }
  
  .task-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    width: 95%;
  }
}
</style>