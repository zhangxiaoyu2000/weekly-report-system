<template>
  <div class="weekly-report-form-v2">
    <div class="form-header">
      <h2>{{ isEditing ? '编辑周报' : '创建周报' }}</h2>
      <div class="form-actions">
        <button @click="saveDraft" class="btn-secondary" :disabled="isSaving">
          <i class="icon-save"></i>
          保存草稿
        </button>
        <button @click="submitReport" class="btn-primary" :disabled="isSaving || !isValidForm">
          <i class="icon-send"></i>
          提交审批
        </button>
      </div>
    </div>

    <form @submit.prevent="submitReport" class="report-form">
      <!-- 基本信息 -->
      <div class="form-section">
        <h3>基本信息</h3>
        <div class="form-group">
          <label for="title">周报标题 *</label>
          <input 
            id="title"
            v-model="reportForm.title" 
            type="text" 
            required
            maxlength="255"
            class="form-control"
            placeholder="请输入周报标题"
          />
        </div>
        
        <div class="form-group">
          <label for="reportWeek">报告周期</label>
          <input 
            id="reportWeek"
            v-model="reportForm.reportWeek" 
            type="text" 
            readonly
            class="form-control"
            :value="getCurrentWeekDisplay()"
          />
        </div>
      </div>

      <!-- 本周工作汇报 -->
      <div class="form-section">
        <h3>本周工作汇报</h3>
        
        <!-- 日常性任务 -->
        <div class="task-section">
          <h4>日常性任务</h4>
          <div class="task-list">
            <div 
              v-for="(task, index) in reportForm.content.Routine_tasks" 
              :key="`routine-${index}`"
              class="task-item"
            >
              <div class="task-header">
                <select 
                  v-model="task.task_id" 
                  @change="onRoutineTaskSelect(task, index)"
                  class="form-control task-select"
                  required
                >
                  <option value="">选择日常任务</option>
                  <option 
                    v-for="availableTask in availableRoutineTasks" 
                    :key="availableTask.id"
                    :value="availableTask.id"
                  >
                    {{ availableTask.taskName }} ({{ getTaskTypeLabel(availableTask.taskType) }})
                  </option>
                </select>
                <button 
                  @click="removeRoutineTask(index)" 
                  type="button" 
                  class="btn-remove"
                  title="删除任务"
                >
                  <i class="icon-trash"></i>
                </button>
              </div>
              
              <div v-if="task.task_id" class="task-details">
                <div class="form-group">
                  <label>实际结果 *</label>
                  <textarea 
                    v-model="task.actual_result" 
                    rows="3"
                    required
                    class="form-control"
                    placeholder="请描述实际完成情况和结果"
                  ></textarea>
                </div>
                
                <div class="form-group">
                  <label>结果差异分析</label>
                  <textarea 
                    v-model="task.AnalysisofResultDifferences" 
                    rows="2"
                    class="form-control"
                    placeholder="如有差异，请分析原因"
                  ></textarea>
                </div>
              </div>
            </div>
            
            <button 
              @click="addRoutineTask" 
              type="button" 
              class="btn-add-task"
            >
              <i class="icon-plus"></i>
              添加日常任务
            </button>
          </div>
        </div>

        <!-- 发展性任务 -->
        <div class="task-section">
          <h4>发展性任务</h4>
          <div class="task-list">
            <div 
              v-for="(task, index) in reportForm.content.Developmental_tasks" 
              :key="`dev-${index}`"
              class="task-item"
            >
              <div class="task-header">
                <div class="project-phase-selector">
                  <select 
                    v-model="task.project_id" 
                    @change="onProjectSelect(task, index)"
                    class="form-control"
                    required
                  >
                    <option value="">选择项目</option>
                    <option 
                      v-for="project in availableProjects" 
                      :key="project.id"
                      :value="project.id"
                    >
                      {{ project.projectName }}
                    </option>
                  </select>
                  
                  <select 
                    v-model="task.phase_id" 
                    :disabled="!task.project_id"
                    class="form-control"
                    required
                  >
                    <option value="">选择阶段</option>
                    <option 
                      v-for="phase in getProjectPhases(task.project_id)" 
                      :key="phase.id"
                      :value="phase.id"
                    >
                      {{ phase.phaseName }}
                    </option>
                  </select>
                </div>
                
                <button 
                  @click="removeDevelopmentalTask(index)" 
                  type="button" 
                  class="btn-remove"
                  title="删除任务"
                >
                  <i class="icon-trash"></i>
                </button>
              </div>
              
              <div v-if="task.project_id && task.phase_id" class="task-details">
                <div class="form-group">
                  <label>实际结果 *</label>
                  <textarea 
                    v-model="task.actual_result" 
                    rows="3"
                    required
                    class="form-control"
                    placeholder="请描述实际完成情况和结果"
                  ></textarea>
                </div>
                
                <div class="form-group">
                  <label>结果差异分析</label>
                  <textarea 
                    v-model="task.AnalysisofResultDifferences" 
                    rows="2"
                    class="form-control"
                    placeholder="如有差异，请分析原因"
                  ></textarea>
                </div>
              </div>
            </div>
            
            <button 
              @click="addDevelopmentalTask" 
              type="button" 
              class="btn-add-task"
            >
              <i class="icon-plus"></i>
              添加发展性任务
            </button>
          </div>
        </div>
      </div>

      <!-- 下周工作规划 -->
      <div class="form-section">
        <h3>下周工作规划</h3>
        
        <!-- 下周日常性任务 -->
        <div class="task-section">
          <h4>日常性任务</h4>
          <div class="task-list">
            <div 
              v-for="(task, index) in reportForm.nextWeekPlan.Routine_tasks" 
              :key="`next-routine-${index}`"
              class="task-item simple"
            >
              <select 
                v-model="task.task_id" 
                class="form-control task-select"
                required
              >
                <option value="">选择日常任务</option>
                <option 
                  v-for="availableTask in availableRoutineTasks" 
                  :key="availableTask.id"
                  :value="availableTask.id"
                >
                  {{ availableTask.taskName }} ({{ getTaskTypeLabel(availableTask.taskType) }})
                </option>
              </select>
              
              <button 
                @click="removeNextWeekRoutineTask(index)" 
                type="button" 
                class="btn-remove"
                title="删除任务"
              >
                <i class="icon-trash"></i>
              </button>
            </div>
            
            <button 
              @click="addNextWeekRoutineTask" 
              type="button" 
              class="btn-add-task"
            >
              <i class="icon-plus"></i>
              添加下周日常任务
            </button>
          </div>
        </div>

        <!-- 下周发展性任务 -->
        <div class="task-section">
          <h4>发展性任务</h4>
          <div class="task-list">
            <div 
              v-for="(task, index) in reportForm.nextWeekPlan.Developmental_tasks" 
              :key="`next-dev-${index}`"
              class="task-item simple"
            >
              <div class="project-phase-selector">
                <select 
                  v-model="task.project_id" 
                  @change="onNextWeekProjectSelect(task, index)"
                  class="form-control"
                  required
                >
                  <option value="">选择项目</option>
                  <option 
                    v-for="project in availableProjects" 
                    :key="project.id"
                    :value="project.id"
                  >
                    {{ project.projectName }}
                  </option>
                </select>
                
                <select 
                  v-model="task.phase_id" 
                  :disabled="!task.project_id"
                  class="form-control"
                  required
                >
                  <option value="">选择阶段</option>
                  <option 
                    v-for="phase in getProjectPhases(task.project_id)" 
                    :key="phase.id"
                    :value="phase.id"
                  >
                    {{ phase.phaseName }}
                  </option>
                </select>
              </div>
              
              <button 
                @click="removeNextWeekDevelopmentalTask(index)" 
                type="button" 
                class="btn-remove"
                title="删除任务"
              >
                <i class="icon-trash"></i>
              </button>
            </div>
            
            <button 
              @click="addNextWeekDevelopmentalTask" 
              type="button" 
              class="btn-add-task"
            >
              <i class="icon-plus"></i>
              添加下周发展性任务
            </button>
          </div>
        </div>
      </div>

      <!-- 其他信息 -->
      <div class="form-section">
        <h3>其他信息</h3>
        
        <div class="form-group">
          <label for="additionalNotes">其他备注</label>
          <textarea 
            id="additionalNotes"
            v-model="reportForm.additionalNotes" 
            rows="3"
            class="form-control"
            placeholder="其他需要说明的事项"
          ></textarea>
        </div>
        
        <div class="form-group">
          <label for="developmentOpportunities">可发展性清单</label>
          <textarea 
            id="developmentOpportunities"
            v-model="reportForm.developmentOpportunities" 
            rows="3"
            class="form-control"
            placeholder="个人或团队发展机会和建议"
          ></textarea>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuth } from '@/stores/auth'
import { useRouter } from 'vue-router'
import api from '@/services/api'

// Props
const props = defineProps({
  reportId: {
    type: [String, Number],
    default: null
  }
})

// 状态管理
const authStore = useAuth()
const router = useRouter()
const isEditing = computed(() => !!props.reportId)
const isSaving = ref(false)

// 数据源
const availableRoutineTasks = ref([])
const availableProjects = ref([])
const projectPhases = ref({}) // 项目ID -> 阶段列表的映射

// 表单数据
const reportForm = reactive({
  title: '',
  reportWeek: '',
  content: {
    Routine_tasks: [],
    Developmental_tasks: []
  },
  nextWeekPlan: {
    Routine_tasks: [],
    Developmental_tasks: []
  },
  additionalNotes: '',
  developmentOpportunities: ''
})

// 表单验证
const isValidForm = computed(() => {
  if (!reportForm.title.trim()) return false
  
  // 检查本周汇报的任务是否都有实际结果
  const currentWeekValid = reportForm.content.Routine_tasks.every(task => 
    task.task_id && task.actual_result?.trim()
  ) && reportForm.content.Developmental_tasks.every(task => 
    task.project_id && task.phase_id && task.actual_result?.trim()
  )
  
  // 检查下周规划的任务是否都选择了
  const nextWeekValid = reportForm.nextWeekPlan.Routine_tasks.every(task => 
    task.task_id
  ) && reportForm.nextWeekPlan.Developmental_tasks.every(task => 
    task.project_id && task.phase_id
  )
  
  return currentWeekValid && nextWeekValid
})

// 生命周期
onMounted(async () => {
  await loadData()
  if (isEditing.value) {
    await loadReportData()
  } else {
    initializeForm()
  }
})

// 方法
const loadData = async () => {
  try {
    const [routineTasksRes, projectsRes] = await Promise.all([
      api.get('/routine-tasks/for-weekly-report'),
      api.get('/projects') // 需要创建获取用户可访问项目的接口
    ])
    
    if (routineTasksRes.data.success) {
      availableRoutineTasks.value = routineTasksRes.data.data
    }
    
    if (projectsRes.data.success) {
      availableProjects.value = projectsRes.data.data
      
      // 预加载所有项目的阶段信息
      for (const project of availableProjects.value) {
        await loadProjectPhases(project.id)
      }
    }
  } catch (error) {
    console.error('Failed to load data:', error)
    // TODO: Show error message
  }
}

const loadProjectPhases = async (projectId) => {
  try {
    const response = await api.get(`/projects/${projectId}/phases`)
    if (response.data.success) {
      projectPhases.value[projectId] = response.data.data
    }
  } catch (error) {
    console.error(`Failed to load phases for project ${projectId}:`, error)
  }
}

const loadReportData = async () => {
  try {
    const response = await api.get(`/weekly-reports/v2/${props.reportId}`)
    if (response.data.success) {
      const data = response.data.data
      Object.assign(reportForm, data)
    }
  } catch (error) {
    console.error('Failed to load report data:', error)
    // TODO: Show error message
  }
}

const initializeForm = () => {
  reportForm.reportWeek = getCurrentWeekDisplay()
  addRoutineTask()
  addDevelopmentalTask()
  addNextWeekRoutineTask()
  addNextWeekDevelopmentalTask()
}

const getCurrentWeekDisplay = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  
  // 计算当前是本月第几周
  const firstDay = new Date(year, now.getMonth(), 1)
  const firstDayOfWeek = firstDay.getDay()
  const dayOfMonth = now.getDate()
  const weekNumber = Math.ceil((dayOfMonth + firstDayOfWeek) / 7)
  
  // 获取星期几
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const dayOfWeek = dayNames[now.getDay()]
  
  return `${month}月第${weekNumber}周（${dayOfWeek}）`
}

// 任务管理方法
const addRoutineTask = () => {
  reportForm.content.Routine_tasks.push({
    task_id: '',
    actual_result: '',
    AnalysisofResultDifferences: ''
  })
}

const removeRoutineTask = (index) => {
  reportForm.content.Routine_tasks.splice(index, 1)
}

const addDevelopmentalTask = () => {
  reportForm.content.Developmental_tasks.push({
    project_id: '',
    phase_id: '',
    actual_result: '',
    AnalysisofResultDifferences: ''
  })
}

const removeDevelopmentalTask = (index) => {
  reportForm.content.Developmental_tasks.splice(index, 1)
}

const addNextWeekRoutineTask = () => {
  reportForm.nextWeekPlan.Routine_tasks.push({
    task_id: ''
  })
}

const removeNextWeekRoutineTask = (index) => {
  reportForm.nextWeekPlan.Routine_tasks.splice(index, 1)
}

const addNextWeekDevelopmentalTask = () => {
  reportForm.nextWeekPlan.Developmental_tasks.push({
    project_id: '',
    phase_id: ''
  })
}

const removeNextWeekDevelopmentalTask = (index) => {
  reportForm.nextWeekPlan.Developmental_tasks.splice(index, 1)
}

// 选择器事件处理
const onRoutineTaskSelect = (task, index) => {
  // 可以在这里添加任务选择后的逻辑
}

const onProjectSelect = async (task, index) => {
  // 重置阶段选择
  task.phase_id = ''
  
  if (task.project_id && !projectPhases.value[task.project_id]) {
    await loadProjectPhases(task.project_id)
  }
}

const onNextWeekProjectSelect = async (task, index) => {
  // 重置阶段选择
  task.phase_id = ''
  
  if (task.project_id && !projectPhases.value[task.project_id]) {
    await loadProjectPhases(task.project_id)
  }
}

const getProjectPhases = (projectId) => {
  return projectPhases.value[projectId] || []
}

// 工具方法
const getTaskTypeLabel = (type) => {
  const labels = {
    'DAILY': '每日',
    'WEEKLY': '每周',
    'MONTHLY': '每月'
  }
  return labels[type] || type
}

// 保存和提交方法
const saveDraft = async () => {
  try {
    isSaving.value = true
    
    const payload = {
      userid: authStore.user.id,
      ...reportForm
    }
    
    let response
    if (isEditing.value) {
      response = await api.put(`/weekly-reports/v2/${props.reportId}`, payload)
    } else {
      response = await api.post('/weekly-reports/v2', payload)
    }
    
    if (response.data.success) {
      // TODO: Show success message
      if (!isEditing.value) {
        router.push(`/reports/edit/${response.data.data.id}`)
      }
    }
  } catch (error) {
    console.error('Failed to save draft:', error)
    // TODO: Show error message
  } finally {
    isSaving.value = false
  }
}

const submitReport = async () => {
  if (!isValidForm.value) {
    // TODO: Show validation error message
    return
  }
  
  try {
    isSaving.value = true
    
    // 先保存草稿
    await saveDraft()
    
    // 然后提交审批
    const response = await api.post(`/weekly-reports/v2/${props.reportId || reportForm.id}/submit`)
    
    if (response.data.success) {
      // TODO: Show success message
      router.push('/reports')
    }
  } catch (error) {
    console.error('Failed to submit report:', error)
    // TODO: Show error message
  } finally {
    isSaving.value = false
  }
}
</script>

<style scoped>
.weekly-report-form-v2 {
  padding: 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e1e5e9;
}

.form-header h2 {
  margin: 0;
  color: #2c3e50;
}

.form-actions {
  display: flex;
  gap: 0.5rem;
}

.form-section {
  background: white;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-section h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.2rem;
  border-bottom: 2px solid #3498db;
  padding-bottom: 0.5rem;
}

.task-section {
  margin-bottom: 2rem;
}

.task-section h4 {
  margin: 0 0 1rem 0;
  color: #34495e;
  font-size: 1rem;
  font-weight: 600;
}

.task-item {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.task-item.simple {
  padding: 0.75rem;
}

.task-header {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.task-item.simple .task-header {
  margin-bottom: 0;
}

.task-select {
  flex: 1;
}

.project-phase-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  flex: 1;
}

.task-details {
  padding-top: 1rem;
  border-top: 1px solid #dee2e6;
}

.btn-remove {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.5rem;
  cursor: pointer;
  font-size: 12px;
  min-width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-remove:hover {
  background: #c82333;
}

.btn-add-task {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.btn-add-task:hover {
  background: #218838;
}

.form-group {
  margin-bottom: 1rem;
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

.form-control:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
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
  display: flex;
  align-items: center;
  gap: 0.5rem;
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
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-secondary:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .project-phase-selector {
    grid-template-columns: 1fr;
  }
  
  .task-header {
    flex-direction: column;
  }
  
  .form-actions {
    justify-content: stretch;
  }
  
  .form-actions button {
    flex: 1;
  }
}
</style>