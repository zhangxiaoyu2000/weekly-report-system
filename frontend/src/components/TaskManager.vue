<template>
  <div class="task-manager bg-white dark:bg-gray-800 text-gray-900 dark:text-white">
    <div class="header">
      <h3 class="text-gray-900 dark:text-white">📝 日常性任务管理</h3>
      <div class="header-actions">
        <input 
          v-model="searchKeyword" 
          @input="searchTasks"
          type="text" 
          placeholder="搜索任务名称..."
          class="search-input bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
        >
        <button @click="showCreateForm = true" class="create-btn">
          ➕ 新建任务
        </button>
      </div>
    </div>

    <!-- 创建/编辑任务表单 -->
    <div v-if="showCreateForm" class="form-section">
      <h4>{{ editingTask ? '编辑任务' : '创建新任务' }}</h4>
      <form @submit.prevent="saveTask" class="task-form">
        <div class="form-group">
          <label>任务名称 *</label>
          <input 
            v-model="form.taskName" 
            type="text" 
            required 
            placeholder="请输入任务名称"
          >
        </div>

        <div class="form-group">
          <label>人员分配</label>
          <input 
            v-model="form.personnelAssignment" 
            type="text" 
            placeholder="负责人员"
          >
        </div>

        <div class="form-group">
          <label>时间线</label>
          <textarea 
            v-model="form.timeline" 
            placeholder="任务的时间安排"
            rows="2"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>量化指标 *</label>
            <textarea 
              v-model="form.quantitativeMetrics" 
              required
              placeholder="如何衡量任务完成质量和效果"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label>预期结果 *</label>
            <textarea 
              v-model="form.expectedResults" 
              required
              placeholder="预期达到的目标和成果"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? '保存中...' : (editingTask ? '更新任务' : '创建任务') }}
          </button>
          <button type="button" @click="cancelForm" :disabled="isLoading">
            取消
          </button>
        </div>
      </form>
    </div>

    <!-- 任务列表 -->
    <div v-if="!showCreateForm" class="tasks-section">
      <div class="tasks-header">
        <div class="tabs">
          <button 
            @click="currentTab = 'all'; loadTasks()"
            :class="['tab-btn', { active: currentTab === 'all' }]"
          >
            所有任务 ({{ allTasks.length }})
          </button>
          <button 
            @click="currentTab = 'my'; loadMyTasks()"
            :class="['tab-btn', { active: currentTab === 'my' }]"
          >
            我的任务 ({{ myTasks.length }})
          </button>
        </div>
        <button @click="loadTasks()" class="refresh-btn">
          🔄 刷新
        </button>
      </div>

      <div v-if="currentTasks.length > 0" class="tasks-grid">
        <div 
          v-for="task in currentTasks" 
          :key="task.id"
          class="task-card bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
        >
          <div class="card-header">
            <div class="task-title">
              <h5>{{ task.taskName }}</h5>
            </div>
            <div class="task-actions">
              <button 
                @click="editTask(task)" 
                class="action-btn edit"
                title="编辑任务"
              >
                ✏️
              </button>
              <button 
                @click="deleteTask(task.id)" 
                class="action-btn delete"
                title="删除任务"
              >
                🗑️
              </button>
            </div>
          </div>

          <div class="card-content">
            <div class="task-info">
              <div v-if="task.personnelAssignment" class="info-item">
                <strong>负责人：</strong>{{ task.personnelAssignment }}
              </div>
              <div v-if="task.timeline" class="info-item">
                <strong>时间线：</strong>{{ truncateText(task.timeline, 50) }}
              </div>
              <div class="info-item">
                <strong>量化指标：</strong>{{ truncateText(task.quantitativeMetrics, 80) }}
              </div>
              <div class="info-item">
                <strong>预期结果：</strong>{{ truncateText(task.expectedResults, 80) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p v-if="currentTab === 'all'">暂无任务数据</p>
        <p v-else>您还没有创建任何任务</p>
        <small>创建您的第一个日常性任务</small>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { taskAPI } from '@/services/api'

// 任务接口定义
interface Task {
  id?: number
  taskName: string
  taskType: 'ROUTINE' | 'DEVELOPMENT'
  personnelAssignment?: string
  timeline?: string
  quantitativeMetrics?: string
  expectedResults?: string
  actualResults?: string
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

// Props and Emits
const emit = defineEmits(['task-selected'])

// 响应式数据
const isLoading = ref(false)
const showCreateForm = ref(false)
const editingTask = ref<Task | null>(null)
const currentTab = ref<'all' | 'my'>('all')
const searchKeyword = ref('')
const allTasks = ref<Task[]>([])
const myTasks = ref<Task[]>([])

// 表单数据
const form = reactive({
  taskName: '',
  taskType: 'ROUTINE' as const,
  personnelAssignment: '',
  timeline: '',
  quantitativeMetrics: '',
  expectedResults: '',
})

// 计算属性
const currentTasks = computed(() => {
  let tasks = currentTab.value === 'all' ? allTasks.value : myTasks.value
  
  if (searchKeyword.value) {
    tasks = tasks.filter(task => 
      task.taskName.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  
  return tasks
})

// 加载所有任务
const loadTasks = async () => {
  try {
    const response = await taskAPI.list()
    if (response.success) {
      allTasks.value = response.data.content || response.data
      console.log('✅ 获取任务列表成功:', allTasks.value.length, '个任务')
    } else {
      console.error('❌ 获取任务列表失败:', response.message)
      // 如果API调用失败，显示空列表
      allTasks.value = []
    }
  } catch (error) {
    console.error('❌ 获取任务列表异常:', error)
    // 如果API调用异常，显示空列表
    allTasks.value = []
  }
}

// 加载我的任务
const loadMyTasks = async () => {
  try {
    const response = await taskAPI.getMyTasks()
    if (response.success) {
      myTasks.value = response.data
      console.log('✅ 获取我的任务成功:', myTasks.value.length, '个任务')
    } else {
      console.error('❌ 获取我的任务失败:', response.message)
      myTasks.value = []
    }
  } catch (error) {
    console.error('❌ 获取我的任务异常:', error)
    myTasks.value = []
  }
}

// 搜索任务
const searchTasks = async () => {
  // 搜索逻辑已在计算属性中实现
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    taskName: '',
    taskType: 'ROUTINE' as const,
    personnelAssignment: '',
    timeline: '',
    quantitativeMetrics: '',
    expectedResults: '',
  })
}

// 取消表单
const cancelForm = () => {
  showCreateForm.value = false
  editingTask.value = null
  resetForm()
}

// 编辑任务
const editTask = (task: Task) => {
  editingTask.value = task
  Object.assign(form, {
    taskName: task.taskName,
    taskType: task.taskType,
    personnelAssignment: task.personnelAssignment || '',
    timeline: task.timeline || '',
    quantitativeMetrics: task.quantitativeMetrics || '',
    expectedResults: task.expectedResults || '',
  })
  showCreateForm.value = true
}

// 保存任务
const saveTask = async () => {
  isLoading.value = true
  
  try {
    const taskData = {
      taskName: form.taskName,
      personnelAssignment: form.personnelAssignment,
      timeline: form.timeline,
      quantitativeMetrics: form.quantitativeMetrics,
      expectedResults: form.expectedResults,
      taskType: form.taskType
    }
    
    if (editingTask.value) {
      // 更新任务 - 调用真实的API
      console.log('更新任务:', taskData)
      const response = await taskAPI.update(editingTask.value.id!, taskData)
      if (response.success) {
        console.log('✅ 任务更新成功:', response.data)
        alert('✅ 任务更新成功！')
      } else {
        throw new Error(response.message || '更新失败')
      }
    } else {
      // 创建新任务
      const response = await taskAPI.create(taskData)
      if (response.success) {
        console.log('✅ 创建任务成功:', response.data)
        alert('✅ 任务创建成功！')
      } else {
        throw new Error(response.message || '创建失败')
      }
    }
    
    cancelForm()
    await loadTasks()
    await loadMyTasks()
  } catch (error) {
    console.error('❌ 操作失败:', error)
    alert('❌ 操作失败：' + (error instanceof Error ? error.message : '请稍后重试'))
  } finally {
    isLoading.value = false
  }
}

// 删除任务
const deleteTask = async (taskId: number) => {
  if (!confirm('确定要删除此任务吗？此操作不可恢复。')) {
    return
  }

  try {
    const response = await taskAPI.delete(taskId)
    if (response.success) {
      console.log('✅ 任务删除成功:', taskId)
      alert('✅ 任务删除成功！')
      await loadTasks()
      await loadMyTasks()
    } else {
      throw new Error(response.message || '删除失败')
    }
  } catch (error) {
    console.error('❌ 删除任务失败:', error)
    alert('❌ 删除失败：' + (error instanceof Error ? error.message : '请稍后重试'))
  }
}

// 获取优先级样式
const getPriorityClass = (priority: number) => {
  if (priority >= 8) return 'high'
  if (priority >= 5) return 'medium'
  return 'low'
}

// 获取任务类型文本
const getTaskTypeText = (taskType: string) => {
  const types = {
    'ROUTINE': '日常性任务',
    'DEVELOPMENT': '发展性任务'
  }
  return types[taskType] || taskType
}

// 获取报告部分文本
const getReportSectionText = (reportSection: string) => {
  const sections = {
    'THIS_WEEK_REPORT': '本周汇报',
    'NEXT_WEEK_PLAN': '下周计划'
  }
  return sections[reportSection] || reportSection
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

// 强制应用暗色主题样式
const forceApplyDarkStyles = () => {
  const isDark = document.querySelector('html').classList.contains('dark')
  
  if (isDark) {
    // 修复 tasks-header 边框
    const tasksHeaders = document.querySelectorAll('.tasks-header')
    tasksHeaders.forEach(header => {
      header.style.borderBottomColor = '#4b5563'
      header.style.borderBottom = '1px solid #4b5563'
    })
    
    // 修复 card-header 背景
    const cardHeaders = document.querySelectorAll('.card-header')
    cardHeaders.forEach(header => {
      header.style.backgroundColor = '#334155'
      header.style.borderBottomColor = '#475569'
      header.style.borderBottom = '1px solid #475569'
    })
    
    // 修复 task-card 边框
    const taskCards = document.querySelectorAll('.task-card')
    taskCards.forEach(card => {
      card.style.borderColor = '#374151'
      card.style.border = '1px solid #374151'
    })
    
    // 修复任务标题文字颜色
    const taskTitles = document.querySelectorAll('.task-title h5')
    taskTitles.forEach(title => {
      title.style.color = '#ffffff'
    })
    
    // 修复strong元素文字颜色
    const strongElements = document.querySelectorAll('.info-item strong')
    strongElements.forEach(strong => {
      strong.style.color = '#e5e7eb'
    })
    
    // 修复表单区域
    const formSections = document.querySelectorAll('.form-section')
    formSections.forEach(section => {
      section.style.backgroundColor = '#374151'
      section.style.color = '#ffffff'
    })
    
    // 修复表单标题
    const formTitles = document.querySelectorAll('.form-section h4')
    formTitles.forEach(title => {
      title.style.color = '#ffffff'
    })
    
    // 修复表单标签
    const formLabels = document.querySelectorAll('.form-group label')
    formLabels.forEach(label => {
      label.style.color = '#e5e7eb'
    })
    
    // 修复表单输入框
    const formInputs = document.querySelectorAll('.form-group input, .form-group textarea')
    formInputs.forEach(input => {
      input.style.backgroundColor = '#4b5563'
      input.style.borderColor = '#6b7280'
      input.style.color = '#ffffff'
    })
  }
}

// 监听表单显示状态变化
watch(showCreateForm, (newValue) => {
  if (newValue) {
    // 表单显示时，在下一个tick应用暗色主题修复
    nextTick(() => {
      setTimeout(forceApplyDarkStyles, 50)
    })
  }
})

// 组件挂载
onMounted(() => {
  loadTasks()
  loadMyTasks()
  
  // 初始应用暗色主题修复
  setTimeout(forceApplyDarkStyles, 100)
  
  // 监听主题变化
  const observer = new MutationObserver(() => {
    forceApplyDarkStyles()
  })
  
  const html = document.querySelector('html')
  if (html) {
    observer.observe(html, {
      attributes: true,
      attributeFilter: ['class']
    })
  }
})
</script>

<style scoped>
.task-manager {
  max-width: 100%;
  padding: 20px;
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
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 200px;
}

:global(.dark) .search-input {
  background: #374151 !important;
  border-color: #4b5563 !important;
  color: #ffffff !important;
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

/* 表单区域 */
.form-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

:global(.dark) .form-section {
  background: #374151 !important;
}

.form-section h4 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 16px;
}

:global(.dark) .form-section h4 {
  color: #ffffff !important;
}

.task-form {
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

:global(.dark) .form-group label {
  color: #e5e7eb !important;
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

:global(.dark) .form-group input,
:global(.dark) .form-group textarea,
:global(.dark) .form-group select {
  background: #374151 !important;
  border-color: #4b5563 !important;
  color: #ffffff !important;
}

:global(.dark) .form-group input::placeholder,
:global(.dark) .form-group textarea::placeholder {
  color: #9ca3af !important;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
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

/* 任务区域 */
.tasks-section {
  border-radius: 8px;
  overflow: hidden;
}

:global(.dark) .tasks-section {
  background: #1f2937 !important;
}

.tasks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

:global(html.dark) .tasks-header {
  border-bottom-color: #4b5563 !important;
}

:global(.dark) .tasks-header {
  border-bottom: 1px solid #4b5563 !important;
}

.tabs {
  display: flex;
  gap: 5px;
}

.tab-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

:global(.dark) .tab-btn {
  background: #374151 !important;
  border-color: #4b5563 !important;
  color: #d1d5db !important;
}

.tab-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.tab-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.refresh-btn {
  padding: 8px 12px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

/* 任务网格 */
.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
  padding: 20px;
}

.task-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

:global(.dark) .task-card {
  background: #1e293b !important;
  border-color: #374151 !important;
  color: #ffffff !important;
}

:global(html.dark) .task-card {
  background: #1e293b !important;
  border: 1px solid #374151 !important;
  color: #ffffff !important;
}

.task-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

:global(.dark) .card-header {
  background: #334155 !important;
  border-bottom-color: #475569 !important;
}

:global(html.dark) .card-header {
  background: #334155 !important;
  border-bottom: 1px solid #475569 !important;
}

.task-title {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
}

.task-title h5 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

:global(.dark) .task-title h5 {
  color: #ffffff !important;
}

.priority-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  width: fit-content;
}

.priority-badge.high {
  background: #ffebee;
  color: #d32f2f;
}

.priority-badge.medium {
  background: #fff3e0;
  color: #f57c00;
}

.priority-badge.low {
  background: #e8f5e8;
  color: #388e3c;
}

.task-actions {
  display: flex;
  gap: 5px;
}

.action-btn {
  padding: 6px 8px;
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

.action-btn:hover {
  opacity: 0.8;
}

.card-content {
  padding: 15px;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
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

:global(.dark) .info-item strong {
  color: #e5e7eb !important;
}

.task-dates {
  border-top: 1px solid #eee;
  padding-top: 10px;
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
  
  .search-input {
    width: auto;
    flex: 1;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .tasks-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .tabs {
    justify-content: center;
    width: 100%;
  }
  
  .tasks-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .task-actions {
    flex-direction: column;
    gap: 3px;
  }
}
</style>