<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">创建周报</h1>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">填写本周工作汇报和下周工作规划的任务清单</p>
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

      <!-- 成功提示 -->
      <div v-if="showSuccess" class="mb-6 bg-green-50 border border-green-200 rounded-md p-4">
        <div class="flex">
          <svg class="h-5 w-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="ml-3">
            <p class="text-sm text-green-700">{{ successMessage }}</p>
          </div>
        </div>
      </div>

      <form @submit.prevent="submitReport" class="space-y-8">
        <!-- 基本信息 -->
        <div class="bg-white rounded-lg shadow px-6 py-6">
          <h2 class="text-lg font-medium text-gray-900 mb-4">基本信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">周报标题 *</label>
              <input
                v-model="reportForm.title"
                type="text"
                required
                minlength="2"
                maxlength="200"
                class="input"
                placeholder="请输入周报标题（2-200个字符）"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">报告周期 *</label>
              <div class="bg-gray-50 border border-gray-300 rounded-md px-3 py-2">
                <div class="flex items-center justify-between">
                  <span class="text-gray-900 font-medium">{{ weekInfo }}</span>
                  <span class="text-sm text-gray-500">{{ reportForm.reportWeek }}</span>
                </div>
              </div>
              <p class="mt-1 text-xs text-gray-500">
                系统自动识别当前是{{ getCurrentMonth() }}第几周
              </p>
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
                :key="task.tempId"
                class="border border-gray-200 rounded-lg p-4 bg-gray-50"
              >
                <TaskForm
                  v-model="tasks[tasks.indexOf(task)]"
                  :show-actual-results="true"
                  @remove="removeTask(task.tempId)"
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
                :key="task.tempId"
                class="border border-gray-200 rounded-lg p-4 bg-gray-50"
              >
                <TaskForm
                  v-model="tasks[tasks.indexOf(task)]"
                  :show-actual-results="true"
                  @remove="removeTask(task.tempId)"
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
                :key="task.tempId"
                class="border border-gray-200 rounded-lg p-4 bg-gray-50"
              >
                <TaskForm
                  v-model="tasks[tasks.indexOf(task)]"
                  :show-actual-results="false"
                  @remove="removeTask(task.tempId)"
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
                :key="task.tempId"
                class="border border-gray-200 rounded-lg p-4 bg-gray-50"
              >
                <TaskForm
                  v-model="tasks[tasks.indexOf(task)]"
                  :show-actual-results="false"
                  @remove="removeTask(task.tempId)"
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
          <p class="text-sm text-gray-600 mb-4">基于本周工作表现，填写团队成员的可发展性机会和建议</p>
          <textarea
            v-model="reportForm.developmentOpportunities"
            rows="6"
            class="input"
            placeholder="例如：
1. 技能提升建议：某某同事在XX方面表现出色，建议进一步培养...
2. 责任扩展机会：某某同事可以承担更多XX类型的工作...
3. 跨部门协作：建议某某同事参与XX项目，提升协作能力..."
          ></textarea>
        </div>

        <!-- 其他备注 -->
        <div class="bg-white rounded-lg shadow px-6 py-6">
          <h2 class="text-lg font-medium text-gray-900 mb-4">其他备注</h2>
          <textarea
            v-model="reportForm.additionalNotes"
            rows="4"
            class="input"
            placeholder="其他需要说明的内容..."
          ></textarea>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-end space-x-4">
          <button
            type="button"
            @click="saveDraft"
            :disabled="saveDraftLoading || submitLoading"
            class="btn-secondary"
          >
            <svg v-if="saveDraftLoading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            保存草稿
          </button>
          <button
            type="submit"
            :disabled="saveDraftLoading || submitLoading"
            class="btn-primary"
          >
            <svg v-if="submitLoading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            提交周报
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import TaskForm from '@/components/TaskForm.vue'
import { reportService, weeklyReportAPI, type Task as APITask, type CreateWeeklyReportRequest } from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const saveDraftLoading = ref(false)
const submitLoading = ref(false)
const error = ref('')

// 成功信息
const successMessage = ref('')
const showSuccess = ref(false)

// 周数信息显示
const weekInfo = ref('')

// 周报基本信息
const reportForm = reactive({
  title: '',
  reportWeek: getCurrentMonday(), // 默认本周一
  additionalNotes: '',
  developmentOpportunities: '' // 可发展性清单
})

// 获取当前周的周一日期
function getCurrentMonday() {
  const today = new Date()
  const dayOfWeek = today.getDay() // 0=周日, 1=周一, ..., 6=周六
  
  // 计算到周一的偏移量
  let mondayOffset
  if (dayOfWeek === 0) {
    // 如果今天是周日，上一个周一是6天前
    mondayOffset = -6
  } else {
    // 如果今天是周一到周六，计算到本周周一的偏移量
    mondayOffset = 1 - dayOfWeek
  }
  
  const monday = new Date(today)
  monday.setDate(today.getDate() + mondayOffset)
  return monday.toISOString().split('T')[0]
}

// 获取当前月份名称
function getCurrentMonth() {
  return new Date().toLocaleDateString('zh-CN', { month: 'long' })
}

// 计算当前日期是当月第几周
function getCurrentWeekOfMonth() {
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  
  // 获取当月第一天
  const firstDay = new Date(year, month, 1)
  
  // 计算第一天是星期几 (0=周日, 1=周一, ..., 6=周六)
  const firstDayOfWeek = firstDay.getDay()
  
  // 计算当月第一个周一的日期
  let firstMondayDate = 1
  if (firstDayOfWeek === 0) { // 如果第一天是周日
    firstMondayDate = 2
  } else if (firstDayOfWeek !== 1) { // 如果第一天不是周一
    firstMondayDate = 8 - firstDayOfWeek + 1
  }
  
  // 获取今天是当月第几天
  const todayDate = today.getDate()
  
  let weekNumber = 1
  
  // 如果今天在第一个周一之前，属于第1周
  if (todayDate < firstMondayDate) {
    weekNumber = 1
  } else {
    // 计算从第一个周一开始是第几周
    weekNumber = Math.floor((todayDate - firstMondayDate) / 7) + 2
  }
  
  // 格式化显示
  const monthName = today.toLocaleDateString('zh-CN', { month: 'long' })
  const dayName = today.toLocaleDateString('zh-CN', { weekday: 'short' })
  
  return `${monthName}第${weekNumber}周 (${dayName})`
}

// 初始化周数信息
function initializeWeekInfo() {
  weekInfo.value = getCurrentWeekOfMonth()
}

// 任务列表
const tasks = ref<Task[]>([])

// 任务数据结构
interface Task {
  tempId: string
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
  taskTemplateId?: number // 关联的任务模板ID
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
    priority: 2, // 2 = NORMAL priority (0=LOW, 1=NORMAL, 2=HIGH, 3=URGENT)
    startDate: '',
    dueDate: '',
    simpleProjectId: undefined,
    taskTemplateId: undefined
  }
}

// 添加任务
function addTask(reportSection: 'THIS_WEEK_REPORT' | 'NEXT_WEEK_PLAN', taskType: 'ROUTINE' | 'DEVELOPMENT') {
  const newTask = createTask(reportSection, taskType)
  tasks.value.push(newTask)
}

// 删除任务
function removeTask(tempId: string) {
  const index = tasks.value.findIndex(task => task.tempId === tempId)
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

// 根据任务数据生成周报字段
function generateReportFieldsFromTasks() {
  const allTasks = tasks.value
  
  // 分离本周和下周任务
  const thisWeekTasks = allTasks.filter(task => task.reportSection === 'THIS_WEEK_REPORT')
  const nextWeekTasks = allTasks.filter(task => task.reportSection === 'NEXT_WEEK_PLAN')
  
  // 生成工作总结
  let workSummary = ''
  if (thisWeekTasks.length > 0) {
    workSummary = thisWeekTasks.map(task => `${task.taskName}: ${task.actualResults || '已完成'}`).join('\n')
  }
  
  // 生成成就
  let achievements = ''
  const completedTasks = thisWeekTasks.filter(task => task.actualResults)
  if (completedTasks.length > 0) {
    achievements = completedTasks.map(task => `完成${task.taskName} - ${task.actualResults}`).join('\n')
  }
  
  // 生成挑战
  let challenges = ''
  const challengeTasks = thisWeekTasks.filter(task => task.resultDifferenceAnalysis)
  if (challengeTasks.length > 0) {
    challenges = challengeTasks.map(task => `${task.taskName}: ${task.resultDifferenceAnalysis}`).join('\n')
  }
  
  // 生成下周计划
  let nextWeekPlan = ''
  if (nextWeekTasks.length > 0) {
    nextWeekPlan = nextWeekTasks.map(task => `${task.taskName}: ${task.expectedResults || '计划执行'}`).join('\n')
  }
  
  return {
    workSummary,
    achievements,
    challenges,
    nextWeekPlan
  }
}

// 根据任务数据生成周报内容
function generateContentFromTasks() {
  const allTasks = tasks.value
  if (!allTasks || allTasks.length === 0) {
    return reportForm.title || '本周工作汇报'
  }
  
  let content = '# 本周工作汇报\n\n'
  
  // 本周完成的任务
  const thisWeekTasks = allTasks.filter(task => task.reportSection === 'THIS_WEEK_REPORT')
  const thisWeekRoutineTasks = thisWeekTasks.filter(task => task.taskType === 'ROUTINE')
  const thisWeekDevTasks = thisWeekTasks.filter(task => task.taskType === 'DEVELOPMENT')
  
  if (thisWeekTasks.length > 0) {
    content += '## 本周已完成工作\n\n'
    
    if (thisWeekRoutineTasks.length > 0) {
      content += '### 日常性任务\n\n'
      thisWeekRoutineTasks.forEach((task, index) => {
        content += `${index + 1}. **${task.taskName}**\n`
        if (task.quantitativeMetrics) {
          content += `   - 关键指标: ${task.quantitativeMetrics}\n`
        }
        if (task.actualResults) {
          content += `   - 实际结果: ${task.actualResults}\n`
        }
        if (task.resultDifferenceAnalysis) {
          content += `   - 结果差异分析: ${task.resultDifferenceAnalysis}\n`
        }
        content += '\n'
      })
    }
    
    if (thisWeekDevTasks.length > 0) {
      content += '### 发展性任务\n\n'
      thisWeekDevTasks.forEach((task, index) => {
        content += `${index + 1}. **${task.taskName}**\n`
        if (task.quantitativeMetrics) {
          content += `   - 关键指标: ${task.quantitativeMetrics}\n`
        }
        if (task.actualResults) {
          content += `   - 实际结果: ${task.actualResults}\n`
        }
        if (task.resultDifferenceAnalysis) {
          content += `   - 结果差异分析: ${task.resultDifferenceAnalysis}\n`
        }
        content += '\n'
      })
    }
  }
  
  // 下周计划的任务
  const nextWeekTasks = allTasks.filter(task => task.reportSection === 'NEXT_WEEK_PLAN')
  const nextWeekRoutineTasks = nextWeekTasks.filter(task => task.taskType === 'ROUTINE')
  const nextWeekDevTasks = nextWeekTasks.filter(task => task.taskType === 'DEVELOPMENT')
  
  if (nextWeekTasks.length > 0) {
    content += '## 下周工作计划\n\n'
    
    if (nextWeekRoutineTasks.length > 0) {
      content += '### 日常性任务\n\n'
      nextWeekRoutineTasks.forEach((task, index) => {
        content += `${index + 1}. **${task.taskName}**\n`
        if (task.expectedResults) {
          content += `   - 预期结果: ${task.expectedResults}\n`
        }
        if (task.timeline) {
          content += `   - 时间安排: ${task.timeline}\n`
        }
        if (task.stopLossPoint) {
          content += `   - 止损点: ${task.stopLossPoint}\n`
        }
        content += '\n'
      })
    }
    
    if (nextWeekDevTasks.length > 0) {
      content += '### 发展性任务\n\n'
      nextWeekDevTasks.forEach((task, index) => {
        content += `${index + 1}. **${task.taskName}**\n`
        if (task.expectedResults) {
          content += `   - 预期结果: ${task.expectedResults}\n`
        }
        if (task.timeline) {
          content += `   - 时间安排: ${task.timeline}\n`
        }
        if (task.stopLossPoint) {
          content += `   - 止损点: ${task.stopLossPoint}\n`
        }
        content += '\n'
      })
    }
  }
  
  return content.trim()
}

// 保存草稿
async function saveDraft() {
  if (!validateBasicInfo()) return

  saveDraftLoading.value = true
  error.value = ''

  try {
    // 准备周报数据 - 保存为草稿（仅包含实际存储的字段）
    const reportData: CreateWeeklyReportRequest = {
      title: reportForm.title,
      reportWeek: reportForm.reportWeek,
      content: generateContentFromTasks(), // 基于任务生成内容
      additionalNotes: reportForm.additionalNotes,
      developmentOpportunities: reportForm.developmentOpportunities,
      priority: 2 // NORMAL priority
      // 不设置status，后端默认创建为草稿状态
    }

    console.log('保存草稿...', { reportData, tasks: tasks.value })
    
    // 调用 API 保存周报和任务 - 保存为草稿
    const report = await reportService.createWithTasks(reportData, tasks.value, true)
    
    console.log('草稿保存成功:', report)
    showSuccessMessage('草稿保存成功！')
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/app/reports')
    }, 2000)
    
  } catch (err: any) {
    console.error('保存草稿失败:', err)
    error.value = err.message || '保存失败，请重试'
  } finally {
    saveDraftLoading.value = false
  }
}

// 提交周报
async function submitReport() {
  if (!validateForm()) return

  submitLoading.value = true
  error.value = ''

  try {
    // 准备周报数据 - 提交时创建为已提交状态（仅包含实际存储的字段）
    const reportData: CreateWeeklyReportRequest = {
      title: reportForm.title,
      reportWeek: reportForm.reportWeek,
      content: generateContentFromTasks(), // 基于任务生成内容
      additionalNotes: reportForm.additionalNotes,
      developmentOpportunities: reportForm.developmentOpportunities,
      priority: 2 // NORMAL priority
      // 不设置status，让后端决定状态或通过submit API设置
    }

    console.log('创建并提交周报...', { reportData, tasks: tasks.value })
    
    // 创建周报和任务（非草稿模式，createWithTasks会自动提交）
    const report = await reportService.createWithTasks(reportData, tasks.value, false)
    console.log('✅ 周报创建并提交成功，ID:', report.id)
    
    // 显示成功提示
    showSuccessMessage('周报提交成功，已进入审批流程！')
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/app/reports')
    }, 2000)
    
  } catch (err: any) {
    console.error('提交周报失败:', err)
    showSuccess.value = false // 隐藏成功提示
    error.value = err.message || '创建/提交失败，请重试'
  } finally {
    submitLoading.value = false
  }
}

// 验证基本信息
function validateBasicInfo() {
  if (!reportForm.title.trim()) {
    error.value = '请输入周报标题'
    return false
  }
  
  if (reportForm.title.trim().length < 2) {
    error.value = '周报标题至少需要2个字符'
    return false
  }
  
  if (reportForm.title.trim().length > 200) {
    error.value = '周报标题不能超过200个字符'
    return false
  }
  
  if (!reportForm.reportWeek) {
    error.value = '请选择报告周期'
    return false
  }
  
  return true
}

// 验证表单
function validateForm() {
  if (!validateBasicInfo()) return false
  
  // 验证所有任务都有必填字段（如果有任务的话）
  for (const task of tasks.value) {
    if (!task.taskName.trim()) {
      error.value = '请填写任务名称'
      return false
    }
  }
  
  return true
}

// 初始化时添加一些默认任务（已移除自动调用，用户需要手动点击添加按钮）
function initializeDefaultTasks() {
  addTask('THIS_WEEK_REPORT', 'ROUTINE')
  addTask('THIS_WEEK_REPORT', 'DEVELOPMENT')
  addTask('NEXT_WEEK_PLAN', 'ROUTINE')
  addTask('NEXT_WEEK_PLAN', 'DEVELOPMENT')
}

// 显示成功消息
function showSuccessMessage(message: string) {
  successMessage.value = message
  showSuccess.value = true
  error.value = '' // 清除错误信息
  
  // 3秒后自动隐藏
  setTimeout(() => {
    showSuccess.value = false
    successMessage.value = ''
  }, 3000)
}

// 组件挂载时不再自动初始化任务，用户需要手动添加

// 组件挂载时初始化周数信息
onMounted(() => {
  initializeWeekInfo()
})
</script>

<style scoped>
.input {
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-primary-500 focus:border-primary-500;
}


.btn-primary {
  @apply bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out;
}

.btn-secondary {
  @apply bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out;
}
</style>