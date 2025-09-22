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
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">创建项目</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">填写项目详细信息，系统将进行AI分析</p>
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

      <!-- AI分析进度提示 -->
      <div v-if="aiAnalysisInProgress" class="mb-6 bg-blue-50 border border-blue-200 rounded-md p-4">
        <div class="flex items-center">
          <svg class="animate-spin h-5 w-5 text-blue-500 mr-3" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <div>
            <p class="text-sm font-medium text-blue-800">🤖 DeepSeek AI正在分析项目</p>
            <p class="text-sm text-blue-600">{{ aiAnalysisStatus }}</p>
          </div>
        </div>
      </div>

      <!-- AI分析结果提示 -->
      <div v-if="aiAnalysisResult" 
           class="mb-6 rounded-md p-4"
           :class="aiAnalysisResult.success ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'">
        <div class="flex">
          <svg v-if="aiAnalysisResult.success" class="h-5 w-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <svg v-else class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          <div class="ml-3">
            <p class="text-sm font-medium" :class="aiAnalysisResult.success ? 'text-green-800' : 'text-red-800'">
              {{ aiAnalysisResult.message }}
            </p>
            <p v-if="aiAnalysisResult.details" class="text-sm mt-1" :class="aiAnalysisResult.success ? 'text-green-600' : 'text-red-600'">
              {{ aiAnalysisResult.details }}
            </p>
          </div>
        </div>
        <div class="mt-4 flex justify-end space-x-3">
          <button
            @click="router.push(`/app/projects/${currentProjectId}`)"
            class="btn-primary"
          >
            查看项目详情
          </button>
          <button
            @click="router.push('/app/projects')"
            class="btn-secondary"
          >
            返回项目列表
          </button>
        </div>
      </div>

      <form v-if="!aiAnalysisInProgress && !aiAnalysisResult" @submit.prevent="submitProject" class="space-y-8">
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
                rows="4"
                required
                class="input"
                placeholder="请详细描述项目的背景、目标和主要内容"
              ></textarea>
            </div>

            <!-- 项目成员 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">项目成员 *</label>
              <textarea
                v-model="projectForm.projectMembers"
                rows="3"
                required
                class="input"
                placeholder="请列出参与项目的团队成员及其角色"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 项目规划 -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow px-6 py-6">
          <h2 class="text-lg font-medium text-gray-900 dark:text-white mb-6">项目规划</h2>
          
          <div class="space-y-6">

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
                rows="3"
                required
                class="input"
                placeholder="请以量化指标形式描述项目预期达到的具体成果（如：提升效率30%、减少成本50%等）"
              ></textarea>
            </div>

            <!-- 时间线 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">时间线 *</label>
              <textarea
                v-model="projectForm.timeline"
                rows="3"
                required
                class="input"
                placeholder="请制定详细的项目时间安排和里程碑"
              ></textarea>
            </div>

            <!-- 止损点 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">止损点 *</label>
              <textarea
                v-model="projectForm.stopLoss"
                rows="3"
                required
                class="input"
                placeholder="请设定项目的风险控制措施和终止条件"
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
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
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
                    class="text-lg font-medium bg-transparent border-none p-0 flex-1 focus:ring-0 focus:outline-none"
                    required
                  />
                </div>
                <button
                  type="button"
                  @click="removePhase(index)"
                  class="text-red-500 hover:text-red-700"
                >
                  <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- 阶段描述 -->
                <div class="col-span-full">
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">阶段描述</label>
                  <textarea
                    v-model="phase.description"
                    rows="2"
                    class="input text-sm"
                    placeholder="描述此阶段的具体内容和目标"
                  ></textarea>
                </div>

                <!-- 负责成员 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">负责成员</label>
                  <input
                    v-model="phase.assignedMembers"
                    type="text"
                    class="input text-sm"
                    placeholder="负责此阶段的团队成员"
                  />
                </div>

                <!-- 时间安排 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">时间安排</label>
                  <input
                    v-model="phase.schedule"
                    type="text"
                    class="input text-sm"
                    placeholder="此阶段的时间安排"
                  />
                </div>


                <!-- 预期结果 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">预期结果</label>
                  <div class="mb-1">
                    <span class="text-xs text-blue-600 dark:text-blue-400">
                      📊 提示：需要以量化指标形式填写
                    </span>
                  </div>
                  <textarea
                    v-model="phase.expectedResults"
                    rows="2"
                    class="input text-sm"
                    placeholder="此阶段的预期结果"
                  ></textarea>
                </div>

              </div>
            </div>
          </div>
        </div>

        <!-- 提示信息 -->
        <div class="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-md p-4">
          <div class="flex">
            <svg class="h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800 dark:text-blue-200">提示</h3>
              <div class="mt-2 text-sm text-blue-700 dark:text-blue-300">
                <p>项目提交后将进行AI智能分析，通过分析的项目将进入审批流程。请确保填写内容详细、准确。</p>
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
            type="submit"
            :disabled="loading"
            class="btn-primary"
          >
            <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ loading ? '创建中...' : '创建项目' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { projectAPI } from '@/services/api'

const router = useRouter()

const loading = ref(false)
const error = ref('')
const aiAnalysisInProgress = ref(false)
const aiAnalysisStatus = ref('')
const aiAnalysisResult = ref(null)
const currentProjectId = ref(null)

// 项目表单数据
const projectForm = reactive({
  projectName: '',
  projectContent: '',
  projectMembers: '',
  expectedResults: '',
  timeline: '',
  stopLoss: ''
})

// 项目阶段数据
interface ProjectPhase {
  tempId: string
  phaseName: string
  description: string
  assignedMembers: string
  schedule: string
  expectedResults: string
  startDate: string
  endDate: string
}

const projectPhases = ref<ProjectPhase[]>([])

// 添加阶段
const addPhase = () => {
  const newPhase: ProjectPhase = {
    tempId: Date.now().toString(),
    phaseName: '',
    description: '',
    assignedMembers: '',
    schedule: '',
    expectedResults: '',
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

// 提交项目
const submitProject = async () => {
  // 验证表单
  if (!validateForm()) return

  loading.value = true
  error.value = ''

  try {
    console.log('🚀 创建项目...', projectForm)
    
    // Transform frontend form data to backend API format
    const projectData = {
      name: projectForm.projectName,  // Map projectName to name
      description: projectForm.projectContent,  // Map projectContent to description
      members: projectForm.projectMembers,  // Map projectMembers to members
      expectedResults: projectForm.expectedResults,
      timeline: projectForm.timeline,
      stopLoss: projectForm.stopLoss,
      phases: projectPhases.value.map((phase, index) => ({
        phaseName: phase.phaseName,
        description: phase.description,
        assignedMembers: phase.assignedMembers,
        schedule: phase.schedule,
        expected_results: phase.expectedResults,
        startDate: phase.startDate,
        endDate: phase.endDate,
        phaseOrder: index + 1
      })).filter(phase => phase.phaseName.trim())  // Only include phases with names
    }
    
    const response = await projectAPI.create(projectData)
    
    if (response.success) {
      console.log('✅ 项目创建成功:', response.data)
      
      // 显示AI分析进度，等待分析完成
      const projectId = response.data.id
      await showAIAnalysisProgress(projectId)
      
      router.push(`/app/projects/${projectId}`)
    } else {
      throw new Error(response.message || '创建项目失败')
    }
    
  } catch (err: any) {
    console.error('❌ 创建项目失败:', err)
    error.value = err.message || '创建失败，请重试'
  } finally {
    loading.value = false
  }
}

// AI分析进度显示
const showAIAnalysisProgress = async (projectId: number) => {
  currentProjectId.value = projectId
  aiAnalysisInProgress.value = true
  aiAnalysisStatus.value = 'AI分析中，请稍候...'
  
  try {
    // 轮询检查AI分析状态
    const maxAttempts = 20  // 最多等待40秒
    let attempts = 0
    
    while (attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 2000)) // 每2秒检查一次
      attempts++
      
      try {
        // 获取项目状态
        const projectResponse = await projectAPI.getById(projectId)
        
        if (projectResponse.success && projectResponse.data) {
          const project = projectResponse.data
          
          if (project.approvalStatus === 'AI_ANALYZING') {
            aiAnalysisStatus.value = `AI分析中... (${attempts * 2}秒)`
            continue
          }
          
          if (project.approvalStatus === 'ADMIN_REVIEWING') {
            aiAnalysisStatus.value = '✅ AI分析通过！进入管理员审核'
            aiAnalysisResult.value = { success: true, message: '项目通过AI分析' }
            break
          }
          
          if (project.approvalStatus === 'AI_REJECTED') {
            aiAnalysisStatus.value = '❌ AI分析未通过'
            aiAnalysisResult.value = { 
              success: false, 
              message: '项目未通过AI分析',
              details: project.aiAnalysisResult 
            }
            break
          }
        }
      } catch (pollError) {
        console.warn('轮询项目状态失败:', pollError)
      }
    }
    
    if (attempts >= maxAttempts) {
      aiAnalysisStatus.value = '⏱️ AI分析超时，请手动刷新查看结果'
    }
    
  } catch (error) {
    console.error('AI分析进度监控失败:', error)
    aiAnalysisStatus.value = '❌ 无法获取AI分析状态'
  } finally {
    aiAnalysisInProgress.value = false
  }
}

// Note: Project phases are now created as part of the main project creation request
// The createProjectPhases function has been removed as phases are included in projectAPI.create()

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
    error.value = '请输入时间线'
    return false
  }
  
  if (!projectForm.stopLoss.trim()) {
    error.value = '请输入止损点'
    return false
  }
  
  return true
}
</script>

<style scoped>
.input {
  @apply block w-full rounded-md border-gray-300 shadow-sm focus:ring-blue-500 focus:border-blue-500;
}

.btn-primary {
  @apply bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out disabled:opacity-50 disabled:cursor-not-allowed;
}

.btn-secondary {
  @apply bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-150 ease-in-out disabled:opacity-50 disabled:cursor-not-allowed;
}
</style>