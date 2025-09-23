<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">编辑周报</h1>
            <p class="mt-2 text-sm text-gray-600">修改周报内容和任务清单</p>
          </div>
          <div class="flex items-center space-x-4">
            <button
              @click="router.back()"
              class="btn-secondary"
            >
              返回
            </button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="initialLoading" class="text-center py-8">
        <div class="inline-flex items-center">
          <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          加载周报数据中...
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="mb-6 bg-red-50 border border-red-200 rounded-md p-4">
        <div class="flex">
          <svg class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="ml-3">
            <p class="text-sm text-red-700">{{ error }}</p>
          </div>
        </div>
      </div>

      <div v-if="!initialLoading">
        <form @submit.prevent="submitReport" class="space-y-8">
          <!-- 基本信息 -->
          <div class="bg-white rounded-lg shadow px-6 py-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">基本信息</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">周报标题 *</label>
                <input
                  v-model="reportForm.title"
                  type="text"
                  required
                  class="input"
                  placeholder="请输入周报标题"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">报告周期 *</label>
                <input
                  v-model="reportForm.reportWeek"
                  type="date"
                  required
                  class="input"
                />
              </div>
            </div>
          </div>

          <!-- 本周汇报 -->
          <div class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">本周汇报</h2>
              <p class="text-sm text-gray-600">本周已完成或正在进行的工作任务</p>
            </div>

            <!-- 本周日常性任务 -->
            <div class="px-6 py-6 border-b border-gray-100">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-md font-medium text-gray-800">日常性任务</h3>
                <button
                  type="button"
                  @click="addTask('THIS_WEEK_REPORT', 'ROUTINE')"
                  class="btn-secondary text-sm"
                >
                  <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  添加任务
                </button>
              </div>

              <div class="space-y-4">
                <div
                  v-for="(task, index) in getTasksByType('THIS_WEEK_REPORT', 'ROUTINE')"
                  :key="task.tempId || task.id"
                  class="border border-gray-200 rounded-lg p-4 bg-gray-50"
                >
                  <TaskForm
                    v-model="tasks[tasks.indexOf(task)]"
                    :show-actual-results="true"
                    @remove="removeTask(task.tempId || task.id)"
                  />
                </div>
                
                <div v-if="getTasksByType('THIS_WEEK_REPORT', 'ROUTINE').length === 0" 
                     class="text-center py-8 text-gray-500">
                  暂无日常性任务，点击上方按钮添加
                </div>
              </div>
            </div>

            <!-- 本周发展性任务 -->
            <div class="px-6 py-6">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-md font-medium text-gray-800">发展性任务</h3>
                <button
                  type="button"
                  @click="addTask('THIS_WEEK_REPORT', 'DEVELOPMENT')"
                  class="btn-secondary text-sm"
                >
                  <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  添加任务
                </button>
              </div>

              <div class="space-y-4">
                <div
                  v-for="(task, index) in getTasksByType('THIS_WEEK_REPORT', 'DEVELOPMENT')"
                  :key="task.tempId || task.id"
                  class="border border-gray-200 rounded-lg p-4 bg-gray-50"
                >
                  <TaskForm
                    v-model="tasks[tasks.indexOf(task)]"
                    :show-actual-results="true"
                    @remove="removeTask(task.tempId || task.id)"
                  />
                </div>
                
                <div v-if="getTasksByType('THIS_WEEK_REPORT', 'DEVELOPMENT').length === 0" 
                     class="text-center py-8 text-gray-500">
                  暂无发展性任务，点击上方按钮添加
                </div>
              </div>
            </div>
          </div>

          <!-- 下周规划 -->
          <div class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-medium text-gray-900">下周规划</h2>
              <p class="text-sm text-gray-600">下周计划开展的工作任务</p>
            </div>

            <!-- 下周日常性任务 -->
            <div class="px-6 py-6 border-b border-gray-100">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-md font-medium text-gray-800">日常性任务</h3>
                <button
                  type="button"
                  @click="addTask('NEXT_WEEK_PLAN', 'ROUTINE')"
                  class="btn-secondary text-sm"
                >
                  <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  添加任务
                </button>
              </div>

              <div class="space-y-4">
                <div
                  v-for="(task, index) in getTasksByType('NEXT_WEEK_PLAN', 'ROUTINE')"
                  :key="task.tempId || task.id"
                  class="border border-gray-200 rounded-lg p-4 bg-gray-50"
                >
                  <TaskForm
                    v-model="tasks[tasks.indexOf(task)]"
                    :show-expected-results="true"
                    @remove="removeTask(task.tempId || task.id)"
                  />
                </div>
                
                <div v-if="getTasksByType('NEXT_WEEK_PLAN', 'ROUTINE').length === 0" 
                     class="text-center py-8 text-gray-500">
                  暂无日常性任务，点击上方按钮添加
                </div>
              </div>
            </div>

            <!-- 下周发展性任务 -->
            <div class="px-6 py-6">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-md font-medium text-gray-800">发展性任务</h3>
                <button
                  type="button"
                  @click="addTask('NEXT_WEEK_PLAN', 'DEVELOPMENT')"
                  class="btn-secondary text-sm"
                >
                  <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  添加任务
                </button>
              </div>

              <div class="space-y-4">
                <div
                  v-for="(task, index) in getTasksByType('NEXT_WEEK_PLAN', 'DEVELOPMENT')"
                  :key="task.tempId || task.id"
                  class="border border-gray-200 rounded-lg p-4 bg-gray-50"
                >
                  <TaskForm
                    v-model="tasks[tasks.indexOf(task)]"
                    :show-expected-results="true"
                    @remove="removeTask(task.tempId || task.id)"
                  />
                </div>
                
                <div v-if="getTasksByType('NEXT_WEEK_PLAN', 'DEVELOPMENT').length === 0" 
                     class="text-center py-8 text-gray-500">
                  暂无发展性任务，点击上方按钮添加
                </div>
              </div>
            </div>
          </div>

          <!-- 可发展性清单 -->
          <div class="bg-white rounded-lg shadow px-6 py-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">可发展性清单</h2>
            <textarea
              v-model="reportForm.developmentOpportunities"
              rows="4"
              class="input resize-none"
              placeholder="请填写工作中的发展机会和成长方向..."
            ></textarea>
          </div>

          <!-- 其他备注 -->
          <div class="bg-white rounded-lg shadow px-6 py-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">其他备注</h2>
            <textarea
              v-model="reportForm.additionalNotes"
              rows="4"
              class="input resize-none"
              placeholder="请填写其他需要补充的内容..."
            ></textarea>
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center justify-end space-x-4 bg-white rounded-lg shadow px-6 py-4">
            <button
              type="button"
              @click="saveDraft"
              :disabled="draftLoading || submitLoading"
              class="btn-secondary"
            >
              <svg v-if="draftLoading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ draftLoading ? '保存中...' : '保存草稿' }}
            </button>
            <button
              type="submit"
              :disabled="submitLoading || draftLoading"
              class="btn-primary"
            >
              <svg v-if="submitLoading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 814 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ submitLoading ? '提交中...' : '提交周报' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import TaskForm from '@/components/TaskForm.vue'
import { 
  reportService, 
  weeklyReportAPI, 
  taskAPI, 
  type Task as APITask, 
  type WeeklyReport, 
  type CreateWeeklyReportRequest 
} from '@/services/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const submitLoading = ref(false)
const draftLoading = ref(false)
const initialLoading = ref(true)
const error = ref('')
const reportId = Number(route.params.id)

// 周报基本信息
const reportForm = reactive({
  title: '',
  reportWeek: '',
  developmentOpportunities: '',
  additionalNotes: ''
})

// 任务列表
const tasks = ref<Task[]>([])

// 任务数据结构
interface Task {
  id?: number
  tempId?: string
  taskName: string
  taskType: 'ROUTINE' | 'DEVELOPMENT'
  reportSection: 'THIS_WEEK_REPORT' | 'NEXT_WEEK_PLAN'
  personnelAssignment: string
  timeline: string
  quantitativeMetrics: string
  expectedResults: string
  actualResults: string
  resultDifferenceAnalysis: string
  stopLossPoint: string
  priority: number
  startDate: string
  dueDate: string
  simpleProjectId?: number
  projectPhaseId?: number
  taskTemplateId?: number
}

// 加载周报数据
async function loadReportData() {
  try {
    initialLoading.value = true
    error.value = ''
    
    console.log('加载周报数据，ID:', reportId)
    
    // 获取周报基本信息
    const reportResponse = await weeklyReportAPI.get(reportId)
    if (!reportResponse.success) {
      throw new Error(reportResponse.message)
    }
    
    const report = reportResponse.data
    console.log('加载的周报数据:', report)
    
    // 检查是否为可编辑状态（AI分析中或被拒绝后可以重新编辑）
    const editableStatuses = ['AI_ANALYZING', 'AI_REJECTED', 'ADMIN_REJECTED']
    if (!editableStatuses.includes(report.approvalStatus || report.status)) {
      throw new Error('只能编辑AI分析中、AI拒绝或管理员拒绝状态的周报')
    }
    
    // 填充基本信息
    reportForm.title = report.title
    reportForm.reportWeek = report.reportWeek
    reportForm.developmentOpportunities = report.developmentOpportunities || ''
    reportForm.additionalNotes = report.additionalNotes || ''
    
    // 从周报数据中重构任务列表
    tasks.value = []
    
    if (report.content) {
      console.log('解析周报content数据:', report.content)
      
      // 解析日常性任务 (本周汇报)
      if (report.content.routineTasks) {
        report.content.routineTasks.forEach(routineTask => {
          tasks.value.push({
            tempId: `routine_this_${routineTask.task_id}`,
            taskName: routineTask.taskDetails?.taskName || '日常性任务',
            taskType: 'ROUTINE',
            reportSection: 'THIS_WEEK_REPORT',
            personnelAssignment: routineTask.taskDetails?.personnelAssignment || '',
            timeline: routineTask.taskDetails?.timeline || '',
            quantitativeMetrics: routineTask.taskDetails?.quantitativeMetrics || '',
            expectedResults: routineTask.taskDetails?.expectedResults || '',
            actualResults: routineTask.actual_result || '',
            resultDifferenceAnalysis: routineTask.AnalysisofResultDifferences || '',
            stopLossPoint: '',
            priority: 5,
            startDate: '',
            dueDate: '',
            taskTemplateId: parseInt(routineTask.task_id) // 用于绑定dropdown
          })
        })
      }
      
      // 解析发展性任务 (本周汇报)
      if (report.content.developmentalTasks) {
        report.content.developmentalTasks.forEach(devTask => {
          tasks.value.push({
            tempId: `dev_this_${devTask.project_id}_${devTask.phase_id}`,
            taskName: devTask.projectDetails?.name || '发展性任务',
            taskType: 'DEVELOPMENT',
            reportSection: 'THIS_WEEK_REPORT',
            personnelAssignment: devTask.projectDetails?.members || '',
            timeline: devTask.projectDetails?.timeline || '',
            quantitativeMetrics: '',
            expectedResults: devTask.projectDetails?.expectedResults || '',
            actualResults: devTask.actual_result || '',
            resultDifferenceAnalysis: devTask.AnalysisofResultDifferences || '',
            stopLossPoint: devTask.projectDetails?.stopLoss || '',
            priority: 5,
            startDate: '',
            dueDate: '',
            simpleProjectId: parseInt(devTask.project_id), // 用于绑定项目dropdown
            projectPhaseId: parseInt(devTask.phase_id) // 用于绑定阶段dropdown
          })
        })
      }
    }
    
    // 解析下周规划任务
    if (report.nextWeekPlan) {
      console.log('解析周报nextWeekPlan数据:', report.nextWeekPlan)
      
      // 解析下周日常性任务
      if (report.nextWeekPlan.routineTasks) {
        report.nextWeekPlan.routineTasks.forEach(routineTask => {
          tasks.value.push({
            tempId: `routine_next_${routineTask.task_id}`,
            taskName: routineTask.taskDetails?.taskName || '日常性任务',
            taskType: 'ROUTINE',
            reportSection: 'NEXT_WEEK_PLAN',
            personnelAssignment: routineTask.taskDetails?.personnelAssignment || '',
            timeline: routineTask.taskDetails?.timeline || '',
            quantitativeMetrics: routineTask.taskDetails?.quantitativeMetrics || '',
            expectedResults: routineTask.taskDetails?.expectedResults || '',
            actualResults: '',
            resultDifferenceAnalysis: '',
            stopLossPoint: '',
            priority: 5,
            startDate: '',
            dueDate: '',
            taskTemplateId: parseInt(routineTask.task_id) // 用于绑定dropdown
          })
        })
      }
      
      // 解析下周发展性任务
      if (report.nextWeekPlan.developmentalTasks) {
        report.nextWeekPlan.developmentalTasks.forEach(devTask => {
          tasks.value.push({
            tempId: `dev_next_${devTask.project_id}_${devTask.phase_id}`,
            taskName: devTask.projectDetails?.name || '发展性任务',
            taskType: 'DEVELOPMENT',
            reportSection: 'NEXT_WEEK_PLAN',
            personnelAssignment: devTask.projectDetails?.members || '',
            timeline: devTask.projectDetails?.timeline || '',
            quantitativeMetrics: '',
            expectedResults: devTask.projectDetails?.expectedResults || '',
            actualResults: '',
            resultDifferenceAnalysis: '',
            stopLossPoint: devTask.projectDetails?.stopLoss || '',
            priority: 5,
            startDate: '',
            dueDate: '',
            simpleProjectId: parseInt(devTask.project_id), // 用于绑定项目dropdown
            projectPhaseId: parseInt(devTask.phase_id) // 用于绑定阶段dropdown
          })
        })
      }
    }
    
    console.log('解析完成的任务数据:', tasks.value.length, '个任务', tasks.value)
    
  } catch (err: any) {
    console.error('加载周报数据失败:', err)
    error.value = err.message || '加载周报数据失败'
  } finally {
    initialLoading.value = false
  }
}

// 创建新任务
function createTask(reportSection: 'THIS_WEEK_REPORT' | 'NEXT_WEEK_PLAN', taskType: 'ROUTINE' | 'DEVELOPMENT'): Task {
  return {
    tempId: Date.now().toString() + Math.random().toString(36).substr(2, 9),
    taskName: '',
    taskType,
    reportSection,
    personnelAssignment: '',
    timeline: '',
    quantitativeMetrics: '',
    expectedResults: '',
    actualResults: '',
    resultDifferenceAnalysis: '',
    stopLossPoint: '',
    priority: 5,
    startDate: '',
    dueDate: '',
    simpleProjectId: undefined,
    projectPhaseId: undefined,
    taskTemplateId: undefined
  }
}

// 添加任务
function addTask(reportSection: 'THIS_WEEK_REPORT' | 'NEXT_WEEK_PLAN', taskType: 'ROUTINE' | 'DEVELOPMENT') {
  const newTask = createTask(reportSection, taskType)
  tasks.value.push(newTask)
}

// 删除任务
function removeTask(taskId: string | number) {
  const index = tasks.value.findIndex(task => 
    (task.tempId && task.tempId === taskId) || (task.id && task.id === taskId)
  )
  if (index !== -1) {
    tasks.value.splice(index, 1)
  }
}

// 根据类型获取任务
function getTasksByType(reportSection: 'THIS_WEEK_REPORT' | 'NEXT_WEEK_PLAN', taskType: 'ROUTINE' | 'DEVELOPMENT') {
  return tasks.value.filter(task => 
    task.reportSection === reportSection && task.taskType === taskType
  )
}

// 验证基本信息
function validateBasicInfo() {
  if (!reportForm.title.trim()) {
    error.value = '请输入周报标题'
    return false
  }
  if (!reportForm.reportWeek) {
    error.value = '请选择报告周期'
    return false
  }
  return true
}

// 根据任务数据生成标准的ContentDTO和NextWeekPlanDTO格式
function generateStructuredContent() {
  const allTasks = tasks.value
  
  // 构建本周汇报content
  const thisWeekTasks = allTasks.filter(task => task.reportSection === 'THIS_WEEK_REPORT')
  const routineTasks = thisWeekTasks.filter(task => task.taskType === 'ROUTINE')
  const developmentTasks = thisWeekTasks.filter(task => task.taskType === 'DEVELOPMENT')
  
  const content = {
    routineTasks: routineTasks.map(task => ({
      task_id: String(task.taskTemplateId || ''),
      actual_result: task.actualResults || '',
      AnalysisofResultDifferences: task.resultDifferenceAnalysis || ''
    })),
    developmentalTasks: developmentTasks.map(task => ({
      project_id: String(task.simpleProjectId || ''),
      phase_id: String(task.projectPhaseId || ''),
      actual_result: task.actualResults || '',
      AnalysisofResultDifferences: task.resultDifferenceAnalysis || ''
    }))
  }
  
  // 构建下周规划nextWeekPlan
  const nextWeekTasks = allTasks.filter(task => task.reportSection === 'NEXT_WEEK_PLAN')
  const nextWeekRoutineTasks = nextWeekTasks.filter(task => task.taskType === 'ROUTINE')
  const nextWeekDevelopmentTasks = nextWeekTasks.filter(task => task.taskType === 'DEVELOPMENT')
  
  const nextWeekPlan = {
    routineTasks: nextWeekRoutineTasks.map(task => ({
      task_id: String(task.taskTemplateId || '')
    })),
    developmentalTasks: nextWeekDevelopmentTasks.map(task => ({
      project_id: String(task.simpleProjectId || ''),
      phase_id: String(task.projectPhaseId || '')
    }))
  }
  
  return { content, nextWeekPlan }
}

// 保存草稿
async function saveDraft() {
  if (!validateBasicInfo()) return
  
  draftLoading.value = true
  error.value = ''
  
  try {
    // 根据任务数据生成标准的ContentDTO和NextWeekPlanDTO格式
    const { content, nextWeekPlan } = generateStructuredContent()
    
    const reportData = {
      title: reportForm.title,
      reportWeek: reportForm.reportWeek,
      content: content,
      nextWeekPlan: nextWeekPlan,
      developmentOpportunities: reportForm.developmentOpportunities,
      additionalNotes: reportForm.additionalNotes
    }
    
    console.log('更新周报草稿...', { reportData, tasks: tasks.value })
    
    const updateResponse = await weeklyReportAPI.update(reportId, reportData)
    if (!updateResponse.success) {
      throw new Error(updateResponse.message)
    }
    
    // TODO: 更新任务（需要后端支持任务的增删改）
    // 这里暂时跳过任务同步，因为需要后端API支持
    console.log('注意：任务更新功能待实现')
    
    console.log('周报草稿保存成功')
    
    // 显示成功消息
    const successMessage = document.createElement('div')
    successMessage.className = 'fixed top-4 right-4 bg-green-500 text-white px-4 py-2 rounded shadow-lg z-50'
    successMessage.textContent = '草稿保存成功！'
    document.body.appendChild(successMessage)
    setTimeout(() => {
      document.body.removeChild(successMessage)
    }, 3000)
    
  } catch (err: any) {
    console.error('保存草稿失败:', err)
    error.value = err.message || '保存草稿失败'
  } finally {
    draftLoading.value = false
  }
}

// 提交周报
async function submitReport() {
  if (!validateBasicInfo()) return
  
  submitLoading.value = true
  error.value = ''
  
  try {
    // 根据任务数据生成标准的ContentDTO和NextWeekPlanDTO格式
    const { content, nextWeekPlan } = generateStructuredContent()
    
    const reportData = {
      title: reportForm.title,
      reportWeek: reportForm.reportWeek,
      content: content,
      nextWeekPlan: nextWeekPlan,
      developmentOpportunities: reportForm.developmentOpportunities,
      additionalNotes: reportForm.additionalNotes
    }
    
    console.log('更新并提交周报...', { reportData })
    
    // 更新周报内容
    console.log('步骤1: 更新周报内容...')
    const updateResponse = await weeklyReportAPI.update(reportId, reportData)
    if (!updateResponse.success) {
      throw new Error('更新周报内容失败: ' + updateResponse.message)
    }
    console.log('步骤1完成: 周报内容更新成功')
    
    // 然后提交周报
    console.log('步骤2: 提交周报...')
    const submitResponse = await weeklyReportAPI.submit(reportId)
    console.log('步骤2响应:', submitResponse)
    
    if (submitResponse.success) {
      console.log('周报提交成功')
      
      // 显示成功消息
      const successMessage = document.createElement('div')
      successMessage.className = 'fixed top-4 right-4 bg-green-500 text-white px-4 py-2 rounded shadow-lg z-50'
      successMessage.textContent = '周报提交成功！'
      document.body.appendChild(successMessage)
      
      // 2秒后跳转
      setTimeout(() => {
        document.body.removeChild(successMessage)
        router.push('/app/reports')
      }, 2000)
    } else {
      throw new Error(submitResponse.message)
    }
    
  } catch (err: any) {
    console.error('提交周报失败:', err)
    error.value = err.message || '提交周报失败'
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadReportData()
})
</script>

<style scoped>
.input {
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm;
}

.btn-primary {
  @apply bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out inline-flex items-center;
}

.btn-secondary {
  @apply bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out inline-flex items-center;
}
</style>