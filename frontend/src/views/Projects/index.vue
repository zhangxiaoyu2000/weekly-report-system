<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <!-- 页面标题和标签切换 -->
      <div class="mb-8">
        <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">项目管理中心</h1>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">管理项目和日常性任务</p>
          </div>
          
          <!-- 标签切换 -->
          <div class="flex space-x-1 bg-gray-200 dark:bg-gray-700 rounded-lg p-1">
            <button
              @click="currentTab = 'projects'"
              :class="[
                'px-4 py-2 text-sm font-medium rounded-md transition-all',
                currentTab === 'projects'
                  ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-white shadow-sm'
                  : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
              ]"
            >
              📋 项目管理
            </button>
            <button
              @click="currentTab = 'tasks'"
              :class="[
                'px-4 py-2 text-sm font-medium rounded-md transition-all',
                currentTab === 'tasks'
                  ? 'bg-white dark:bg-gray-600 text-gray-900 dark:text-white shadow-sm'
                  : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white'
              ]"
            >
              📝 任务管理
            </button>
          </div>
        </div>
      </div>

      <!-- 标签内容 -->
      <div class="tab-content">
        <!-- 项目管理标签 -->
        <div v-if="currentTab === 'projects'">
          <div class="mb-6 flex justify-between items-center">
            <div class="flex space-x-4">
              <button
                v-for="status in statusFilters"
                :key="status.value"
                @click="currentStatus = status.value"
                :class="[
                  'px-4 py-2 rounded-md text-sm font-medium transition duration-150 ease-in-out',
                  currentStatus === status.value
                    ? 'bg-blue-100 text-blue-700 border border-blue-300'
                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                ]"
              >
                {{ status.label }}
                <span v-if="status.count !== undefined" class="ml-1 text-xs">
                  ({{ status.count }})
                </span>
              </button>
            </div>
            <router-link
              to="/app/projects/create"
              class="btn-primary"
            >
              <svg class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              创建项目
            </router-link>
          </div>

          <!-- 项目列表 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="project in filteredProjects"
              :key="project.id"
              class="bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-md transition-shadow cursor-pointer"
              @click="viewProject(project)"
            >
              <div class="p-6">
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center">
                    <div
                      :class="[
                        'w-3 h-3 rounded-full mr-2',
                        getStatusColor(project.approvalStatus)
                      ]"
                    ></div>
                    <span class="text-sm font-medium text-gray-500 dark:text-gray-400">
                      {{ getStatusText(project) }}
                    </span>
                  </div>
                  <div class="text-xs text-gray-400 dark:text-gray-500">
                    {{ formatDate(project.createdAt) }}
                  </div>
                </div>

                <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                  {{ project.name }}
                </h3>
                
                <p class="text-sm text-gray-600 dark:text-gray-300 mb-4 line-clamp-3">
                  {{ project.description }}
                </p>

                <div class="border-t pt-4">
                  <div class="flex items-center text-sm text-gray-500 dark:text-gray-400 mb-2">
                    <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                    {{ project.members }}
                  </div>
                  <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
                    <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {{ project.timeline }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredProjects.length === 0" class="col-span-full text-center py-12">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">暂无项目</h3>
              <p class="mt-1 text-sm text-gray-500">开始创建您的第一个项目吧</p>
            </div>
          </div>
        </div>

        <!-- 任务管理标签 -->
        <div v-else-if="currentTab === 'tasks'" class="bg-white dark:bg-gray-800 rounded-lg shadow">
          <TaskManager />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { projectAPI, type Project } from '@/services/api'
import TaskManager from '@/components/TaskManager.vue'

const router = useRouter()

// 数据状态
const currentTab = ref('projects')
const projects = ref<Project[]>([])
const currentStatus = ref('ALL')
const loading = ref(false)

// 状态过滤器
const statusFilters = computed(() => [
  { label: '全部', value: 'ALL', count: projects.value.length },
  { label: '待AI分析', value: 'AI_ANALYZING', count: projects.value.filter(p => p.approvalStatus === 'AI_ANALYZING').length },
  { label: 'AI已通过', value: 'AI_APPROVED', count: projects.value.filter(p => p.approvalStatus === 'AI_APPROVED').length },
  { label: 'AI不合格', value: 'AI_REJECTED', count: projects.value.filter(p => p.approvalStatus === 'AI_REJECTED').length },
  { label: '待管理员审核', value: 'ADMIN_REVIEWING', count: projects.value.filter(p => p.approvalStatus === 'ADMIN_REVIEWING').length },
  { label: '待超管审核', value: 'SUPER_ADMIN_REVIEWING', count: projects.value.filter(p => p.approvalStatus === 'SUPER_ADMIN_REVIEWING').length },
  { label: '已批准', value: 'FINAL_APPROVED', count: projects.value.filter(p => p.approvalStatus === 'FINAL_APPROVED').length },
  { label: '已拒绝', value: 'REJECTED', count: projects.value.filter(p => p.approvalStatus?.includes('REJECTED')).length }
])

// 过滤后的项目
const filteredProjects = computed(() => {
  if (currentStatus.value === 'ALL') {
    return projects.value
  }
  
  if (currentStatus.value === 'REJECTED') {
    return projects.value.filter(project => project.approvalStatus?.includes('REJECTED'))
  }
  
  return projects.value.filter(project => project.approvalStatus === currentStatus.value)
})

// 获取状态颜色
const getStatusColor = (approvalStatus: string) => {
  if (approvalStatus?.includes('REJECTED')) {
    return 'bg-red-400'
  }
  
  if (approvalStatus?.includes('REVIEWING') || approvalStatus?.includes('ANALYZING')) {
    return 'bg-blue-400'
  }
  
  const colors = {
    'AI_ANALYZING': 'bg-yellow-400',
    'AI_APPROVED': 'bg-blue-400',
    'FINAL_APPROVED': 'bg-green-400'
  }
  return colors[approvalStatus as keyof typeof colors] || 'bg-gray-400'
}

// 获取状态文本 - 项目卡片显示角色+状态
const getStatusText = (project: Project) => {
  const approvalStatus = project.approvalStatus
  
  // 项目卡片显示角色+状态，不显示具体审核人姓名
  const specificTexts = {
    'AI_ANALYZING': '待AI分析',
    'AI_APPROVED': 'AI已通过',
    'AI_REJECTED': 'AI不合格',
    'ADMIN_REVIEWING': '待管理员审核',
    'ADMIN_APPROVED': '管理员已审核',
    'ADMIN_REJECTED': '管理员拒绝',
    'SUPER_ADMIN_REVIEWING': '待超级管理员审核',
    'SUPER_ADMIN_APPROVED': '超级管理员已审核',
    'SUPER_ADMIN_REJECTED': '超级管理员拒绝',
    'REJECTED': '已拒绝',
    'FINAL_APPROVED': '已批准'
  }
  
  return specificTexts[approvalStatus as keyof typeof specificTexts] || approvalStatus
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 查看项目详情
const viewProject = (project: Project) => {
  router.push(`/app/projects/${project.id}`)
}

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await projectAPI.getMyProjects()
    if (response.success) {
      projects.value = response.data
      console.log('✅ 获取项目列表成功:', response.data.length, '个项目')
    } else {
      console.error('❌ 获取项目列表失败:', response.message)
    }
  } catch (error) {
    console.error('❌ 获取项目列表异常:', error)
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.btn-primary {
  @apply inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150 ease-in-out;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>