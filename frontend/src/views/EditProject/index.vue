<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center">
          <button
            @click="$router.go(-1)"
            class="mr-4 p-2 text-gray-400 hover:text-gray-600 dark:text-gray-300 dark:hover:text-white"
          >
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">修改项目</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">
              修改项目信息和阶段规划，重新提交或强行提交给管理员审核
            </p>
          </div>
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

      <!-- AI分析结果显示 -->
      <div v-if="originalProject?.aiAnalysisResult" class="mb-6 bg-yellow-50 border border-yellow-200 rounded-md p-4">
        <div class="flex">
          <svg class="h-5 w-5 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
          </svg>
          <div class="ml-3">
            <p class="text-sm font-medium text-yellow-800">🤖 AI分析反馈</p>
            <p class="text-sm text-yellow-700 mt-1 whitespace-pre-wrap">{{ originalProject.aiAnalysisResult }}</p>
          </div>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-8">
        <!-- 基本信息 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow px-6 py-6">
          <h2 class="text-lg font-medium text-gray-900 dark:text-white mb-6">基本信息</h2>
          
          <div class="space-y-6">
            <!-- 项目名称 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">项目名称 *</label>
              <input
                v-model="projectForm.projectName"
                type="text"
                required
                class="input"
                placeholder="请输入项目名称"
              />
            </div>

            <!-- 项目内容 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">项目内容 *</label>
              <textarea
                v-model="projectForm.projectContent"
                rows="6"
                required
                class="input"
                placeholder="详细描述项目的目标、范围和主要功能..."
              ></textarea>
            </div>

            <!-- 项目成员 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">项目成员 *</label>
              <input
                v-model="projectForm.projectMembers"
                type="text"
                required
                class="input"
                placeholder="项目团队成员，包括角色和职责"
              />
            </div>


            <!-- 预期结果 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">预期结果 *</label>
              <div class="mb-2">
                <span class="text-xs text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-900/30 px-2 py-1 rounded">
                  📊 提示：需要以量化指标形式填写（如：增加30%销售额、减少50%时间成本等）
                </span>
              </div>
              <textarea
                v-model="projectForm.expectedResults"
                rows="4"
                required
                class="input"
                placeholder="项目预期达成的具体结果和效果..."
              ></textarea>
            </div>

            <!-- 时间计划 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">时间计划 *</label>
              <input
                v-model="projectForm.timeline"
                type="text"
                required
                class="input"
                placeholder="项目的详细时间安排和里程碑"
              />
            </div>

            <!-- 止损点 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">止损点 *</label>
              <textarea
                v-model="projectForm.stopLoss"
                rows="3"
                required
                class="input"
                placeholder="项目风险控制和止损条件..."
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 项目阶段规划 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow px-6 py-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-medium text-gray-900 dark:text-white">项目阶段规划</h2>
            <button
              type="button"
              @click="addPhase"
              class="btn-secondary text-sm"
            >
              ➕ 添加阶段
            </button>
          </div>
          
          <div v-if="projectPhases.length === 0" class="text-center py-8 text-gray-500">
            <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012 2v2M7 7h10" />
            </svg>
            <p>暂无项目阶段</p>
            <p class="text-sm mt-1">点击"添加阶段"开始规划项目阶段</p>
          </div>
          
          <div v-else class="space-y-4">
            <div
              v-for="(phase, index) in projectPhases"
              :key="phase.tempId"
              class="border border-gray-200 rounded-lg p-4 relative"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-3">
                  <span class="bg-blue-100 text-blue-800 text-sm font-medium px-2.5 py-0.5 rounded-full">
                    阶段 {{ index + 1 }}
                  </span>
                  <input
                    v-model="phase.phaseName"
                    type="text"
                    placeholder="阶段名称"
                    class="text-lg font-medium bg-transparent border-none outline-none flex-1"
                  />
                </div>
                <button
                  type="button"
                  @click="removePhase(index)"
                  class="text-red-600 hover:text-red-800"
                >
                  <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- 阶段描述 -->
                <div class="md:col-span-2">
                  <label class="block text-sm font-medium text-gray-700 mb-1">阶段描述</label>
                  <textarea
                    v-model="phase.phaseDescription"
                    rows="2"
                    class="input text-sm"
                    placeholder="描述这个阶段的主要工作内容"
                  ></textarea>
                </div>

                <!-- 负责成员 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">负责成员</label>
                  <input
                    v-model="phase.assignedMembers"
                    type="text"
                    class="input text-sm"
                    placeholder="负责此阶段的团队成员"
                  />
                </div>

                <!-- 时间安排 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">时间安排</label>
                  <input
                    v-model="phase.timeline"
                    type="text"
                    class="input text-sm"
                    placeholder="如：4周，2025-01-01到2025-01-28"
                  />
                </div>


                <!-- 预期结果 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">预期结果</label>
                  <div class="mb-1">
                    <span class="text-xs text-blue-600">
                      📊 提示：需要以量化指标形式填写
                    </span>
                  </div>
                  <input
                    v-model="phase.estimatedResults"
                    type="text"
                    class="input text-sm"
                    placeholder="此阶段预期产出的量化成果（如：提升效率30%）"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-end space-x-4">
          <button
            type="button"
            @click="$router.go(-1)"
            :disabled="loading"
            class="btn-secondary"
          >
            取消
          </button>
          <button
            type="button"
            @click="handleForceSubmit"
            :disabled="loading"
            class="btn-warning"
          >
            <svg v-if="loading && submitType === 'force'" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <svg v-else class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            {{ loading && submitType === 'force' ? '强行提交中...' : '强行提交' }}
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="btn-primary"
          >
            <svg v-if="loading && submitType === 'resubmit'" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ loading && submitType === 'resubmit' ? '重新提交中...' : '重新提交' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { simpleProjectAPI, projectPhaseAPI } from '@/services/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const submitType = ref('')
const originalProject = ref<any>(null)

const projectId = route.params.id

// 项目表单数据
const projectForm = reactive({
  projectName: '',
  projectContent: '',
  projectMembers: '',
  expectedResults: '',
  actualResults: '',
  timeline: '',
  stopLoss: ''
})

// 项目阶段数据
interface ProjectPhase {
  tempId?: string
  id?: number
  phaseName: string
  phaseDescription: string
  assignedMembers: string
  timeline: string
  estimatedResults: string
  startDate: string
  endDate: string
  phaseOrder?: number
}

const projectPhases = ref<ProjectPhase[]>([])

// 加载原项目数据
const loadProjectData = async () => {
  if (!projectId) return
  
  try {
    // 加载项目基本信息
    console.log('🔍 [EditProjectView] 开始加载项目数据，项目ID:', projectId)
    const projectResponse = await simpleProjectAPI.getById(Number(projectId))
    console.log('📤 [EditProjectView] simpleProjectAPI响应:', projectResponse)
    
    if (projectResponse.success && projectResponse.data) {
      originalProject.value = projectResponse.data
      console.log('📊 [EditProjectView] 项目数据字段检查:')
      console.log('  - projectName存在:', 'projectName' in projectResponse.data, '值:', projectResponse.data.projectName)
      console.log('  - projectContent存在:', 'projectContent' in projectResponse.data, '值:', projectResponse.data.projectContent)
      console.log('  - projectMembers存在:', 'projectMembers' in projectResponse.data, '值:', projectResponse.data.projectMembers)
      
      // 预填充表单数据
      projectForm.projectName = projectResponse.data.projectName
      projectForm.projectContent = projectResponse.data.projectContent
      projectForm.projectMembers = projectResponse.data.projectMembers
      projectForm.expectedResults = projectResponse.data.expectedResults
      projectForm.actualResults = projectResponse.data.actualResults || ''
      projectForm.timeline = projectResponse.data.timeline
      projectForm.stopLoss = projectResponse.data.stopLoss
      
      console.log('✅ [EditProjectView] 表单数据填充完成:', {
        projectName: projectForm.projectName,
        projectContent: projectForm.projectContent,
        projectMembers: projectForm.projectMembers
      })
    } else {
      console.log('❌ [EditProjectView] 项目数据加载失败:', projectResponse.message)
      error.value = '项目数据加载失败: ' + projectResponse.message
    }
    
    // 加载项目阶段
    await loadProjectPhases()
    
  } catch (err: any) {
    console.error('加载项目数据失败:', err)
    error.value = '加载项目数据失败: ' + err.message
  }
}

// 加载项目阶段
const loadProjectPhases = async () => {
  try {
    const response = await projectPhaseAPI.getByProject(Number(projectId))
    if (response.success && response.data) {
      projectPhases.value = response.data.map(phase => ({
        id: phase.id,
        tempId: phase.id.toString(),
        phaseName: phase.phaseName,
        phaseDescription: phase.phaseDescription || '',
        assignedMembers: phase.assignedMembers || '',
        timeline: phase.timeline || '',
        estimatedResults: phase.estimatedResults || '',
        startDate: phase.startDate || '',
        endDate: phase.endDate || '',
        phaseOrder: phase.phaseOrder
      }))
    }
  } catch (err: any) {
    console.warn('加载项目阶段失败:', err)
    // 阶段加载失败不影响项目编辑
  }
}

// 添加阶段
const addPhase = () => {
  const newPhase: ProjectPhase = {
    tempId: Date.now().toString(),
    phaseName: '',
    phaseDescription: '',
    assignedMembers: '',
    timeline: '',
      estimatedResults: '',
    startDate: '',
    endDate: ''
  }
  projectPhases.value.push(newPhase)
}

// 删除阶段
const removePhase = (index: number) => {
  if (confirm('确定要删除此阶段吗？')) {
    projectPhases.value.splice(index, 1)
  }
}

// 重新提交项目
const handleSubmit = async () => {
  if (!validateForm()) return
  
  loading.value = true
  submitType.value = 'resubmit'
  error.value = ''

  try {
    console.log('🚀 [EditProjectView] 开始重新提交项目')
    console.log('📊 [EditProjectView] 当前表单数据:', {
      projectName: projectForm.projectName,
      projectContent: projectForm.projectContent,
      projectMembers: projectForm.projectMembers,
      projectNameType: typeof projectForm.projectName,
      projectContentType: typeof projectForm.projectContent,
      projectMembersType: typeof projectForm.projectMembers
    })
    
    // 构建完整的重新提交数据，包含项目基本信息和阶段信息
    const resubmitData = {
      name: projectForm.projectName,
      description: projectForm.projectContent,
      members: projectForm.projectMembers,
      expectedResults: projectForm.expectedResults,
      timeline: projectForm.timeline,
      stopLoss: projectForm.stopLoss,
      projectPhases: projectPhases.value
        .filter(phase => phase.phaseName.trim()) // 只提交有名称的阶段
        .map(phase => ({
          phaseName: phase.phaseName,
          description: phase.phaseDescription,
          assignedMembers: phase.assignedMembers,
          schedule: phase.timeline,
          expectedResults: phase.estimatedResults
        }))
    }
    
    console.log('📤 [EditProjectView] 重新提交数据:', resubmitData)
    console.log('📊 [EditProjectView] 字段类型检查:')
    Object.entries(resubmitData).forEach(([key, value]) => {
      if (key !== 'projectPhases') {
        console.log(`  - ${key}: ${typeof value} = "${value}"`)
        if (typeof value !== 'string') {
          console.warn(`⚠️ ${key}字段类型不是string，这可能导致验证错误`)
        }
      } else {
        console.log(`  - ${key}: Array(${value.length})`)
      }
    })
    
    const response = await fetch(`/api/simple/projects/${projectId}/resubmit`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`
      },
      body: JSON.stringify(resubmitData)
    })
    
    const result = await response.json()
    
    if (result.success) {
      alert('项目重新提交成功！正在重新进行AI分析...')
      router.push('/app/projects')
    } else {
      throw new Error(result.message || '重新提交失败')
    }
    
  } catch (err: any) {
    console.error('重新提交失败:', err)
    error.value = err.message || '重新提交失败，请重试'
  } finally {
    loading.value = false
    submitType.value = ''
  }
}

// 强行提交给管理员
const handleForceSubmit = async () => {
  if (!confirm('确定要强行提交给管理员吗？这将跳过AI重新分析，直接进入管理员审核环节。')) {
    return
  }
  
  loading.value = true
  submitType.value = 'force'
  error.value = ''

  try {
    // 构建完整的项目数据
    const resubmitData = {
      name: projectForm.projectName,
      description: projectForm.projectContent,
      members: projectForm.projectMembers,
      expectedResults: projectForm.expectedResults,
      timeline: projectForm.timeline,
      stopLoss: projectForm.stopLoss,
      projectPhases: projectPhases.value
        .filter(phase => phase.phaseName.trim()) // 只提交有名称的阶段
        .map(phase => ({
          phaseName: phase.phaseName,
          description: phase.phaseDescription,
          assignedMembers: phase.assignedMembers,
          schedule: phase.timeline,
          expectedResults: phase.estimatedResults
        }))
    }
    
    // 先更新项目内容
    const resubmitResponse = await fetch(`/api/simple/projects/${projectId}/resubmit`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`
      },
      body: JSON.stringify(resubmitData)
    })
    
    const resubmitResult = await resubmitResponse.json()
    if (!resubmitResult.success) {
      throw new Error(resubmitResult.message || '更新项目失败')
    }
    
    // 然后强行提交给管理员
    const response = await simpleProjectAPI.forceSubmit(Number(projectId))
    
    if (response.success) {
      alert('项目已强行提交给管理员审核！')
      router.push('/app/projects')
    } else {
      throw new Error(response.message || '强行提交失败')
    }
    
  } catch (err: any) {
    console.error('强行提交失败:', err)
    error.value = err.message || '强行提交失败，请重试'
  } finally {
    loading.value = false
    submitType.value = ''
  }
}


// 验证表单
const validateForm = () => {
  if (!projectForm.projectName.trim()) {
    error.value = '请输入项目名称'
    return false
  }
  
  if (!projectForm.projectContent.trim()) {
    error.value = '请输入项目内容'
    return false
  }
  
  if (!projectForm.projectMembers.trim()) {
    error.value = '请输入项目成员'
    return false
  }
  
  if (!projectForm.expectedResults.trim()) {
    error.value = '请输入预期结果（需要以量化指标形式）'
    return false
  }
  
  if (!projectForm.expectedResults.trim()) {
    error.value = '请输入预期结果'
    return false
  }
  
  if (!projectForm.timeline.trim()) {
    error.value = '请输入时间计划'
    return false
  }
  
  if (!projectForm.stopLoss.trim()) {
    error.value = '请输入止损点'
    return false
  }
  
  return true
}

onMounted(() => {
  loadProjectData()
})
</script>

<style scoped>
.input {
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white;
}

.btn-primary {
  @apply bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out disabled:opacity-50;
}

.btn-secondary {
  @apply bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out disabled:opacity-50;
}

.btn-warning {
  @apply bg-orange-600 hover:bg-orange-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out disabled:opacity-50;
}
</style>