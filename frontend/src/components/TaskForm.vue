<template>
  <div class="task-form">
    <!-- 任务标题和删除按钮 -->
    <div class="flex items-center justify-between mb-4">
      <input
        v-model="localTask.taskName"
        type="text"
        placeholder="任务名称"
        class="text-lg font-medium bg-transparent border-none p-0 flex-1 focus:ring-0 focus:outline-none"
        :class="{ 'text-red-500': !localTask.taskName.trim() }"
      />
      <button
        type="button"
        @click="$emit('remove')"
        class="text-red-500 hover:text-red-700 ml-2"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
      </button>
    </div>

    <!-- 日常性任务选择 -->
    <div v-if="localTask.taskType === 'ROUTINE'" class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-1">选择日常性任务 *</label>
      <select 
        v-model="selectedRoutineTaskId"
        @change="onRoutineTaskChange"
        class="input text-sm"
        required
      >
        <option value="">请选择日常性任务</option>
        <option 
          v-for="task in availableRoutineTasks" 
          :key="task.id" 
          :value="task.id"
        >
          {{ task.taskName }}
        </option>
      </select>
      <small v-if="selectedRoutineTask" class="text-gray-600 block mt-1">
        {{ selectedRoutineTask.quantitativeMetrics ? `指标: ${selectedRoutineTask.quantitativeMetrics}` : '日常性任务已选择，任务信息已自动填入' }}
      </small>
    </div>

    <!-- 发展性任务项目选择 -->
    <div v-if="localTask.taskType === 'DEVELOPMENT'" class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-1">关联项目 *</label>
      <select 
        v-model="selectedProjectId"
        @change="onProjectChange"
        class="input text-sm"
        required
      >
        <option value="">请选择项目</option>
        <option 
          v-for="project in availableProjects" 
          :key="project.id" 
          :value="project.id"
        >
          {{ project.projectName }}
        </option>
      </select>
    </div>

    <!-- 发展性任务阶段选择 -->
    <div v-if="localTask.taskType === 'DEVELOPMENT' && selectedProjectId" class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-1">关联阶段性任务</label>
      <select 
        v-model="selectedPhaseId"
        @change="onPhaseChange"
        class="input text-sm"
      >
        <option value="">请选择阶段</option>
        <option 
          v-for="phase in availablePhases" 
          :key="phase.id" 
          :value="phase.id"
          :disabled="phase.status === 'COMPLETED'"
          :class="{ 'text-gray-400': phase.status === 'COMPLETED' }"
        >
          {{ phase.phaseOrder ? `${phase.phaseOrder}. ` : '' }}{{ phase.phaseName }}
          {{ phase.status === 'COMPLETED' ? ' (已完成)' : '' }}
        </option>
      </select>
      <small v-if="selectedPhase" class="text-gray-600 block mt-1">
        阶段状态：{{ getPhaseStatusText(selectedPhase.status) }}
        {{ selectedPhase.phaseDescription ? ` - ${selectedPhase.phaseDescription}` : '' }}
      </small>
    </div>


    <!-- 详细信息显示区域 -->
    <div class="mt-4 space-y-4">
      <!-- 日常性任务的详细信息显示 -->
      <template v-if="localTask.taskType === 'ROUTINE' && selectedRoutineTask">
        <!-- 显示日常性任务的所有属性（只读表单） -->
        <div class="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-lg p-4">
          <h4 class="text-sm font-medium text-blue-800 dark:text-blue-200 mb-3">📋 日常性任务信息</h4>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-gray-700 dark:text-gray-300">任务名称：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedRoutineTask.taskName }}</span>
            </div>
            <div v-if="selectedRoutineTask.personnelAssignment">
              <span class="font-medium text-gray-700 dark:text-gray-300">负责人：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedRoutineTask.personnelAssignment }}</span>
            </div>
            <div v-if="selectedRoutineTask.timeline">
              <span class="font-medium text-gray-700 dark:text-gray-300">时间线：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedRoutineTask.timeline }}</span>
            </div>
            <div v-if="selectedRoutineTask.quantitativeMetrics">
              <span class="font-medium text-gray-700 dark:text-gray-300">量化指标：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedRoutineTask.quantitativeMetrics }}</span>
            </div>
            <div v-if="selectedRoutineTask.expectedResults" class="md:col-span-2">
              <span class="font-medium text-gray-700 dark:text-gray-300">预期结果：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedRoutineTask.expectedResults }}</span>
            </div>
          </div>
        </div>

        <!-- 仅本周汇报时显示用户填写区域 -->
        <div v-if="showActualResults" class="space-y-4">
          <!-- 实际结果 (仅本周汇报显示) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">实际结果 *</label>
            <textarea
              v-model="localTask.actualResults"
              rows="3"
              class="input text-sm"
              placeholder="请填写实际完成情况和成果"
              required
            ></textarea>
          </div>

          <!-- 结果差异分析 (仅本周汇报显示) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">结果差异分析 *</label>
            <textarea
              v-model="localTask.resultDifferenceAnalysis"
              rows="3"
              class="input text-sm"
              placeholder="请分析预估与实际结果的差异原因"
              required
            ></textarea>
          </div>
        </div>
      </template>

      <!-- 发展性任务的详细信息显示 -->
      <template v-else-if="localTask.taskType === 'DEVELOPMENT'">
        <!-- 显示选中项目的所有属性（只读表单） -->
        <div v-if="selectedProject" class="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-700 rounded-lg p-4">
          <h4 class="text-sm font-medium text-green-800 dark:text-green-200 mb-3">🚀 项目信息</h4>
          
          <div class="grid grid-cols-1 gap-4 text-sm">
            <div>
              <span class="font-medium text-gray-700 dark:text-gray-300">项目名称：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedProject.projectName }}</span>
            </div>
            <div v-if="selectedProject.projectContent">
              <span class="font-medium text-gray-700 dark:text-gray-300">项目内容：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedProject.projectContent }}</div>
            </div>
            <div v-if="selectedProject.projectMembers">
              <span class="font-medium text-gray-700 dark:text-gray-300">项目成员：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedProject.projectMembers }}</span>
            </div>
            <div v-if="selectedProject.keyIndicators">
              <span class="font-medium text-gray-700 dark:text-gray-300">关键指标：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedProject.keyIndicators }}</div>
            </div>
            <div v-if="selectedProject.expectedResults">
              <span class="font-medium text-gray-700 dark:text-gray-300">预期结果：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedProject.expectedResults }}</div>
            </div>
            <div v-if="selectedProject.timeline">
              <span class="font-medium text-gray-700 dark:text-gray-300">时间线：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedProject.timeline }}</div>
            </div>
            <div v-if="selectedProject.stopLoss">
              <span class="font-medium text-gray-700 dark:text-gray-300">止损点：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedProject.stopLoss }}</div>
            </div>
          </div>
        </div>

        <!-- 显示选中阶段的所有属性（只读表单） -->
        <div v-if="selectedPhase" class="bg-purple-50 dark:bg-purple-900/20 border border-purple-200 dark:border-purple-700 rounded-lg p-4">
          <h4 class="text-sm font-medium text-purple-800 dark:text-purple-200 mb-3">⚡ 阶段信息</h4>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-gray-700 dark:text-gray-300">阶段名称：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedPhase.phaseName }}</span>
            </div>
            <div v-if="selectedPhase.phaseOrder">
              <span class="font-medium text-gray-700 dark:text-gray-300">阶段顺序：</span>
              <span class="text-gray-600 dark:text-gray-400">第 {{ selectedPhase.phaseOrder }} 阶段</span>
            </div>
            <div v-if="selectedPhase.phaseDescription" class="md:col-span-2">
              <span class="font-medium text-gray-700 dark:text-gray-300">阶段描述：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedPhase.phaseDescription }}</div>
            </div>
            <div v-if="selectedPhase.assignedMembers">
              <span class="font-medium text-gray-700 dark:text-gray-300">分配成员：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedPhase.assignedMembers }}</span>
            </div>
            <div v-if="selectedPhase.timeline">
              <span class="font-medium text-gray-700 dark:text-gray-300">时间线：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ selectedPhase.timeline }}</span>
            </div>
            <div v-if="selectedPhase.keyIndicators" class="md:col-span-2">
              <span class="font-medium text-gray-700 dark:text-gray-300">关键指标：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedPhase.keyIndicators }}</div>
            </div>
            <div v-if="selectedPhase.estimatedResults" class="md:col-span-2">
              <span class="font-medium text-gray-700 dark:text-gray-300">预期结果：</span>
              <div class="text-gray-600 dark:text-gray-400 whitespace-pre-wrap">{{ selectedPhase.estimatedResults }}</div>
            </div>
            <div>
              <span class="font-medium text-gray-700 dark:text-gray-300">状态：</span>
              <span class="text-gray-600 dark:text-gray-400">{{ getPhaseStatusText(selectedPhase.status) }}</span>
            </div>
          </div>
        </div>

        <!-- 仅本周汇报且已选择项目时显示用户填写区域 -->
        <div v-if="selectedProject && showActualResults" class="space-y-4">
          <!-- 实际结果 (仅本周汇报显示) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">实际结果 *</label>
            <textarea
              v-model="localTask.actualResults"
              rows="3"
              class="input text-sm"
              placeholder="请填写实际完成情况和成果"
              required
            ></textarea>
          </div>

          <!-- 结果差异分析 (仅本周汇报显示) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">结果差异分析 *</label>
            <textarea
              v-model="localTask.resultDifferenceAnalysis"
              rows="3"
              class="input text-sm"
              placeholder="请分析预估与实际结果的差异原因"
              required
            ></textarea>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref, onMounted } from 'vue'
import { simpleProjectAPI, projectPhaseAPI, taskAPI, type ProjectPhase } from '@/services/api'

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
  projectPhaseId?: number
  taskTemplateId?: number
  taskTemplateId?: number // 关联的任务模板ID
}

interface SimpleProject {
  id: number
  projectName: string
  projectContent: string
  projectMembers: string
  keyIndicators: string
  expectedResults: string
  timeline: string
  stopLoss: string
  status: string
}

const props = defineProps<{
  modelValue: Task
  showActualResults?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Task): void
  (e: 'remove'): void
}>()

// 项目相关数据
const availableProjects = ref<SimpleProject[]>([])
const selectedProjectId = ref<number | string>('')
const selectedProject = ref<SimpleProject | null>(null)

// 已有任务相关数据  
const availableTemplates = ref<APITask[]>([])
const selectedTemplateId = ref<number | string>('')
const selectedTemplate = ref<APITask | null>(null)

// 日常性任务相关数据
const availableRoutineTasks = ref<APITask[]>([])
const selectedRoutineTaskId = ref<number | string>('')
const selectedRoutineTask = ref<APITask | null>(null)

// 阶段相关数据
const availablePhases = ref<ProjectPhase[]>([])
const selectedPhaseId = ref<number | string>('')
const selectedPhase = ref<ProjectPhase | null>(null)

const localTask = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 已有任务选择变化处理
const onTemplateChange = () => {
  if (!selectedTemplateId.value) {
    selectedTemplate.value = null
    return
  }

  const existingTask = availableTemplates.value.find(t => t.id === Number(selectedTemplateId.value))
  if (existingTask && localTask.value.taskType === 'ROUTINE') {
    selectedTemplate.value = existingTask
    
    // 从已有任务复制信息到当前任务
    const updatedTask = {
      ...localTask.value,
      taskName: existingTask.taskName || localTask.value.taskName,
      personnelAssignment: existingTask.personnelAssignment || localTask.value.personnelAssignment,
      timeline: existingTask.timeline || localTask.value.timeline,
      quantitativeMetrics: existingTask.quantitativeMetrics || localTask.value.quantitativeMetrics,
      expectedResults: existingTask.expectedResults || localTask.value.expectedResults,
      priority: existingTask.priority || localTask.value.priority
    }
    
    localTask.value = updatedTask
  }
}

// 清除任务选择
const clearTemplate = () => {
  selectedTemplateId.value = ''
  selectedTemplate.value = null
  
  const updatedTask = {
    ...localTask.value,
    taskTemplateId: undefined
  }
  
  localTask.value = updatedTask
}

// 日常性任务选择变化处理
const onRoutineTaskChange = () => {
  if (!selectedRoutineTaskId.value) {
    selectedRoutineTask.value = null
    return
  }

  const routineTask = availableRoutineTasks.value.find(t => t.id === Number(selectedRoutineTaskId.value))
  if (routineTask && localTask.value.taskType === 'ROUTINE') {
    selectedRoutineTask.value = routineTask
    
    // 更新任务信息，从选中的已有任务复制信息
    const updatedTask = {
      ...localTask.value,
      taskName: routineTask.taskName,
      personnelAssignment: routineTask.personnelAssignment,
      timeline: routineTask.timeline,
      quantitativeMetrics: routineTask.quantitativeMetrics,
      expectedResults: routineTask.expectedResults,
      priority: routineTask.priority,
      taskTemplateId: routineTask.id // ⭐ 保存真实的数据库任务ID
    }

    
    localTask.value = updatedTask
  }
}

// 项目选择变化处理
const onProjectChange = async () => {
  // 清除之前选择的阶段
  selectedPhaseId.value = ''
  selectedPhase.value = null
  availablePhases.value = []
  
  if (!selectedProjectId.value) {
    selectedProject.value = null
    return
  }

  const project = availableProjects.value.find(p => p.id === Number(selectedProjectId.value))
  if (project && localTask.value.taskType === 'DEVELOPMENT') {
    selectedProject.value = project
    
    // 更新任务的项目ID
    const updatedTask = {
      ...localTask.value,
      simpleProjectId: project.id // ⭐ 保存真实的数据库项目ID
    }
    localTask.value = updatedTask
    
    // 加载项目阶段
    await fetchProjectPhases(project.id)
  }
}

// 阶段选择变化处理
const onPhaseChange = () => {
  if (!selectedPhaseId.value) {
    selectedPhase.value = null
    return
  }

  const phase = availablePhases.value.find(p => p.id === Number(selectedPhaseId.value))
  if (phase && localTask.value.taskType === 'DEVELOPMENT') {
    selectedPhase.value = phase
    
    // 更新任务信息
    const updatedTask = {
      ...localTask.value,
      taskName: phase.phaseName,
      projectPhaseId: phase.id,
      simpleProjectId: phase.projectId
    }

    // 预填充阶段字段
    if (phase.assignedMembers) {
      updatedTask.personnelAssignment = phase.assignedMembers
    }
    if (phase.timeline) {
      updatedTask.timeline = phase.timeline
    }
    if (phase.keyIndicators) {
      updatedTask.quantitativeMetrics = phase.keyIndicators
    }
    if (phase.estimatedResults) {
      updatedTask.expectedResults = phase.estimatedResults
    }
    
    localTask.value = updatedTask
  }
}

// 获取可用项目列表
const fetchAvailableProjects = async () => {
  try {
    const response = await simpleProjectAPI.getApprovedProjects()
    if (response.success) {
      availableProjects.value = response.data
      console.log('✅ 获取项目列表成功:', response.data.length, '个项目')
    } else {
      console.error('❌ 获取项目列表失败:', response.message)
    }
  } catch (error) {
    console.error('❌ 获取项目列表异常:', error)
  }
}

// 获取可选择的例行任务列表
const fetchAvailableTemplates = async () => {
  try {
    const response = await taskAPI.getSelectableRoutineTasks()
    if (response.success) {
      availableTemplates.value = response.data
      console.log('✅ 获取例行任务列表成功:', response.data.length, '个任务')
    } else {
      console.error('❌ 获取例行任务列表失败:', response.message)
    }
  } catch (error) {
    console.error('❌ 获取例行任务列表异常:', error)
  }
}

// 获取可用的例行任务列表  
const fetchAvailableRoutineTasks = async () => {
  try {
    const response = await taskAPI.getSelectableRoutineTasks()
    if (response.success) {
      // 注意：由于后端架构变化，Task表不再有taskType字段
      // 任务类型通过关联表区分，这里直接使用所有返回的任务
      availableRoutineTasks.value = response.data
      console.log('✅ 获取日常性任务列表成功:', availableRoutineTasks.value.length, '个任务')
    } else {
      console.error('❌ 获取例行任务列表失败:', response.message)
    }
  } catch (error) {
    console.error('❌ 获取例行任务列表异常:', error)
  }
}

// 获取项目阶段列表
const fetchProjectPhases = async (projectId: number) => {
  try {
    const response = await projectPhaseAPI.getSelectablePhases(projectId)
    if (response.success) {
      availablePhases.value = response.data
      console.log('✅ 获取项目阶段成功:', response.data.length, '个阶段')
    } else {
      console.error('❌ 获取项目阶段失败:', response.message)
    }
  } catch (error) {
    console.error('❌ 获取项目阶段异常:', error)
  }
}

// 获取阶段状态文本
const getPhaseStatusText = (status: string) => {
  const statusTexts: Record<string, string> = {
    'PENDING': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusTexts[status] || status
}

// 监听本地任务变化，触发父组件更新
watch(localTask, (newValue) => {
  emit('update:modelValue', newValue)
}, { deep: true })

// 组件挂载时获取数据
onMounted(async () => {
  console.log('🔄 TaskForm mounted with task:', localTask.value)
  
  if (localTask.value.taskType === 'ROUTINE') {
    // 日常性任务：获取可选择的日常性任务列表
    await fetchAvailableRoutineTasks()
    
    // 如果任务已有关联的任务模板，设置选中状态
    if (localTask.value.taskTemplateId) {
      selectedRoutineTaskId.value = localTask.value.taskTemplateId
      const routineTask = availableRoutineTasks.value.find(t => t.id === localTask.value.taskTemplateId)
      if (routineTask) {
        selectedRoutineTask.value = routineTask
      }
    }
  } else if (localTask.value.taskType === 'DEVELOPMENT') {
    // 发展性任务：获取项目列表
    await fetchAvailableProjects()
    
    console.log('📊 Task simpleProjectId:', localTask.value.simpleProjectId)
    console.log('📊 Task projectPhaseId:', localTask.value.projectPhaseId)
    console.log('📊 Available projects:', availableProjects.value.map(p => ({ id: p.id, name: p.projectName })))
    
    // 如果任务已有关联项目，设置选中状态
    if (localTask.value.simpleProjectId) {
      selectedProjectId.value = localTask.value.simpleProjectId
      const project = availableProjects.value.find(p => p.id === localTask.value.simpleProjectId)
      console.log('🎯 Found project for ID', localTask.value.simpleProjectId, ':', project)
      
      if (project) {
        selectedProject.value = project
        console.log('✅ Project selected:', project.projectName)
        
        // 加载项目阶段
        await fetchProjectPhases(project.id)
        
        // 如果任务已有关联阶段，设置选中状态
        if (localTask.value.projectPhaseId) {
          selectedPhaseId.value = localTask.value.projectPhaseId
          const phase = availablePhases.value.find(p => p.id === localTask.value.projectPhaseId)
          console.log('🎯 Found phase for ID', localTask.value.projectPhaseId, ':', phase)
          
          if (phase) {
            selectedPhase.value = phase
            console.log('✅ Phase selected:', phase.phaseName)
          }
        }
      } else {
        console.log('❌ Project not found for ID:', localTask.value.simpleProjectId)
      }
    } else {
      console.log('📝 No project association found for task')
    }
  }
})
</script>

<style scoped>
.task-form {
  /* 自定义样式 */
}

.input {
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-primary-500 focus:border-primary-500;
}
</style>