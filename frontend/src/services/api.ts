import { useAuthStore } from '@/stores/auth'

// 强制使用相对路径，利用Vite代理配置
const BASE_URL = '/api'

// 项目阶段接口
export interface ProjectPhase {
  id: number
  projectId: number
  phaseName: string
  // 前端期望的字段名称
  phaseDescription?: string
  assignedMembers?: string
  timeline?: string
  estimatedResults?: string
  // 后端字段名称（兼容性）
  description?: string
  schedule?: string
  expectedResults?: string
  actualResults?: string
  resultDifferenceAnalysis?: string
  completionPercentage?: number
  createdAt: string
  updatedAt: string
}

// 项目接口 - 匹配后端Project实体
export interface Project {
  id: number
  name: string
  description: string
  members: string
  expectedResults: string
  timeline: string
  stopLoss: string
  status: 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  approvalStatus: 'AI_ANALYZING' | 'AI_APPROVED' | 'AI_REJECTED' | 'ADMIN_REVIEWING' | 'ADMIN_APPROVED' | 'ADMIN_REJECTED' | 'SUPER_ADMIN_REVIEWING' | 'SUPER_ADMIN_APPROVED' | 'SUPER_ADMIN_REJECTED' | 'REJECTED' | 'FINAL_APPROVED'
  createdBy: number
  createdAt: string
  updatedAt: string
  submittedAt?: string
  phases?: ProjectPhase[]
}

// 任务接口 - 匹配后端Task实体
export interface Task {
  id?: number
  taskName: string
  personnelAssignment?: string
  timeline?: string
  quantitativeMetrics?: string
  expectedResults?: string
  actualResults?: string
  taskType: 'DEVELOPMENT' | 'ROUTINE'
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

// 周报接口 - 匹配后端WeeklyReport实体
export interface WeeklyReport {
  id?: number
  title: string
  content?: string
  workSummary?: string
  achievements?: string
  challenges?: string
  nextWeekPlan?: string
  developmentOpportunities?: string
  additionalNotes?: string
  reportWeek: string
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  status: 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED'
  approvalStatus: 'AI_ANALYZING' | 'AI_APPROVED' | 'AI_REJECTED' | 'ADMIN_REVIEWING' | 'ADMIN_APPROVED' | 'ADMIN_REJECTED' | 'SUPER_ADMIN_REVIEWING' | 'SUPER_ADMIN_APPROVED' | 'SUPER_ADMIN_REJECTED' | 'REJECTED' | 'FINAL_APPROVED'
  createdBy?: number
  createdAt?: string
  updatedAt?: string
  submittedAt?: string
  project?: Project
  // AI分析结果字段
  aiAnalysisId?: number
  aiAnalysisResult?: string
  aiConfidence?: number
  aiAnalysisStatus?: string
  aiAnalysisCompletedAt?: string
}

// HTTP请求工具函数
async function apiRequest<T>(url: string, options: RequestInit = {}): Promise<T> {
  const authStore = useAuthStore()
  
  try {
    // 从环境变量获取超时配置
    const timeout = parseInt(import.meta.env.VITE_API_TIMEOUT || '8000')
    
    // 使用Promise.race实现更稳定的超时控制
    const timeoutPromise = new Promise<never>((_, reject) => {
      setTimeout(() => reject(new Error('Request timeout - please check your network connection')), timeout)
    })
    
    const defaultOptions: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...(authStore.token ? { 'Authorization': `Bearer ${authStore.token}` } : {}),
        // 代理绕过相关头部
        ...(import.meta.env.VITE_BYPASS_PROXY === 'true' ? {
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0'
        } : {}),
      },
      // 如果启用了缓存禁用，添加cache控制
      ...(import.meta.env.VITE_DISABLE_CACHE === 'true' ? {
        cache: 'no-store'
      } : {}),
    }

    const fetchPromise = fetch(`${BASE_URL}${url}`, {
      ...defaultOptions,
      ...options,
      headers: {
        ...defaultOptions.headers,
        ...options.headers,
      },
    })

    const response = await Promise.race([fetchPromise, timeoutPromise])

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Network error' }))
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    // 处理不同类型的网络错误
    if (error instanceof Error) {
      if (error.message.includes('Request timeout')) {
        throw new Error('Request timeout - please check your network connection')
      } else if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
        throw new Error('Unable to connect to server - please check if the backend service is running or proxy settings')
      } else if (error.message.includes('ERR_CONNECTION_REFUSED') ||
                 error.message.includes('net::ERR_CONNECTION_REFUSED') ||
                 error.message.includes('fetch')) {
        throw new Error('Unable to connect to server - please check if the backend service is running')
      }
    }
    
    throw error
  }
}

// 认证API服务
export const authAPI = {
  // 用户登录
  async login(credentials: { usernameOrEmail: string; password: string }): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    })
  },

  // 用户注册
  async register(userData: { username: string; password: string; confirmPassword: string; email: string; fullName: string; role?: string }): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    })
  },

  // 修改密码
  async changePassword(data: { currentPassword: string; newPassword: string; confirmNewPassword: string }): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  }
}

// 用户管理API服务
export const userAPI = {
  // 获取用户列表
  async list(page: number = 0, size: number = 100): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users?page=${page}&size=${size}`)
  },

  // 创建用户 - 管理员专用
  async create(userData: { 
    username: string; 
    password: string; 
    confirmPassword: string;
    email: string; 
    role?: string 
  }): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>('/users', {
      method: 'POST',
      body: JSON.stringify(userData),
    })
  },

  // 更新用户信息
  async update(userId: number, userData: { 
    username?: string; 
    email?: string; 
    role?: string 
  }): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(userData),
    })
  },

  // 启用用户
  async enable(userId: number): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users/${userId}/enable`, {
      method: 'PUT',
    })
  },

  // 禁用用户
  async disable(userId: number): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users/${userId}/disable`, {
      method: 'PUT',
    })
  },

  // 删除用户
  async delete(userId: number): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users/${userId}`, {
      method: 'DELETE',
    })
  },

  // 重置用户密码
  async resetPassword(userId: number, newPassword: string): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users/${userId}/reset-password?newPassword=${encodeURIComponent(newPassword)}`, {
      method: 'POST',
    })
  },

  // 搜索用户
  async searchUsers(keyword: string, page: number = 0, size: number = 10): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/users/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`)
  }
}

// 项目管理API服务
export const projectAPI = {
  // 创建项目
  async create(projectData: {
    name: string;
    description: string;
    members: string;
    expectedResults: string;
    timeline: string;
    stopLoss: string;
    phases?: Array<{
      phaseName: string;
      description: string;
      assignedMembers?: string;
      schedule?: string;
      expected_results?: string;
    }>;
  }): Promise<{ success: boolean; data: Project; message: string }> {
    return await apiRequest<{ success: boolean; data: Project; message: string }>('/projects', {
      method: 'POST',
      body: JSON.stringify(projectData),
    })
  },

  // 获取项目列表（分页）
  async list(page: number = 0, size: number = 10, sort: string = 'createdAt,desc'): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/projects?page=${page}&size=${size}&sort=${sort}`)
  },

  // 获取当前用户的项目
  async getMyProjects(): Promise<{ success: boolean; data: Project[]; message: string }> {
    return await apiRequest<{ success: boolean; data: Project[]; message: string }>('/projects/my')
  },

  // 获取项目详情
  async get(id: number): Promise<{ success: boolean; data: Project; message: string }> {
    return await apiRequest<{ success: boolean; data: Project; message: string }>(`/projects/${id}`)
  },

  // 获取项目详情（别名方法，用于兼容性）
  async getById(id: number): Promise<{ success: boolean; data: Project; message: string }> {
    return this.get(id)
  },

  // 更新项目
  async update(id: number, projectData: {
    name: string;
    description: string;
    members: string;
    expectedResults: string;
    timeline: string;
    stopLoss: string;
    phases?: Array<{
      phaseName: string;
      description: string;
      assignedMembers?: string;
      schedule?: string;
      expected_results?: string;
    }>;
  }): Promise<{ success: boolean; data: Project; message: string }> {
    return await apiRequest<{ success: boolean; data: Project; message: string }>(`/projects/${id}`, {
      method: 'PUT',
      body: JSON.stringify(projectData),
    })
  },

  // 提交项目
  async submit(id: number): Promise<{ success: boolean; data: Project; message: string }> {
    return await apiRequest<{ success: boolean; data: Project; message: string }>(`/projects/${id}/submit`, {
      method: 'PUT',
    })
  },

  // 删除项目
  async delete(id: number): Promise<{ success: boolean; message: string }> {
    return await apiRequest<{ success: boolean; message: string }>(`/projects/${id}`, {
      method: 'DELETE',
    })
  }
}

// 任务管理API服务
export const taskAPI = {
  // 创建任务
  async create(taskData: {
    taskName: string;
    personnelAssignment?: string;
    timeline?: string;
    quantitativeMetrics?: string;
    expectedResults?: string;
    taskType: 'DEVELOPMENT' | 'ROUTINE';
  }): Promise<{ success: boolean; data: Task; message: string }> {
    return await apiRequest<{ success: boolean; data: Task; message: string }>('/tasks', {
      method: 'POST',
      body: JSON.stringify(taskData),
    })
  },

  // 获取任务列表（分页排序）
  async list(page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDir: string = 'desc'): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>(`/tasks?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`)
  },

  // 获取当前用户创建的任务
  async getMyTasks(): Promise<{ success: boolean; data: Task[]; message: string }> {
    return await apiRequest<{ success: boolean; data: Task[]; message: string }>('/tasks/my')
  },

  // 按类型获取任务
  async getByType(taskType: 'DEVELOPMENT' | 'ROUTINE'): Promise<{ success: boolean; data: Task[]; message: string }> {
    return await apiRequest<{ success: boolean; data: Task[]; message: string }>(`/tasks/by-type/${taskType}`)
  },

  // 获取任务统计信息
  async getStatistics(): Promise<{ success: boolean; data: any; message: string }> {
    return await apiRequest<{ success: boolean; data: any; message: string }>('/tasks/statistics')
  },

  // 获取任务详情
  async get(id: number): Promise<{ success: boolean; data: Task; message: string }> {
    return await apiRequest<{ success: boolean; data: Task; message: string }>(`/tasks/${id}`)
  },

  // 更新任务
  async update(id: number, taskData: {
    taskName: string;
    personnelAssignment?: string;
    timeline?: string;
    quantitativeMetrics?: string;
    expectedResults?: string;
    taskType: 'DEVELOPMENT' | 'ROUTINE';
  }): Promise<{ success: boolean; data: Task; message: string }> {
    return await apiRequest<{ success: boolean; data: Task; message: string }>(`/tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(taskData),
    })
  },

  // 删除任务
  async delete(id: number): Promise<{ success: boolean; message: string }> {
    return await apiRequest<{ success: boolean; message: string }>(`/tasks/${id}`, {
      method: 'DELETE',
    })
  },

  // 获取可选择的例行任务（用于周报创建）
  // 注意：由于后端架构改变，Task表不再有taskType字段
  // 任务类型通过关联表区分：TaskReport(日常) vs DevTaskReport(发展)
  async getSelectableRoutineTasks(): Promise<{ success: boolean; data: Task[]; message: string }> {
    try {
      const response = await this.getMyTasks()
      if (response.success) {
        // 后端Task表没有taskType字段，返回所有任务供用户选择
        // 在周报系统中，任务类型通过存储在不同关联表来区分
        return {
          success: true,
          data: response.data,
          message: `获取当前用户任务成功: ${response.data.length} 个任务`
        }
      }
      return response
    } catch (error) {
      console.error('获取用户任务失败:', error)
      return {
        success: false,
        data: [],
        message: '获取用户任务失败: ' + (error as Error).message
      }
    }
  },

  // 获取可选择的发展性任务（用于周报创建） 
  // 注意：由于后端架构改变，这里实际上也是获取用户的所有任务
  async getSelectableDevelopmentTasks(): Promise<{ success: boolean; data: Task[]; message: string }> {
    try {
      const response = await this.getMyTasks()
      if (response.success) {
        // 后端Task表没有taskType字段，返回所有任务供用户选择
        // 发展性任务通过项目关联来区分
        return {
          success: true,
          data: response.data,
          message: `获取当前用户任务成功: ${response.data.length} 个任务`
        }
      }
      return response
    } catch (error) {
      console.error('获取用户任务失败:', error)
      return {
        success: false,
        data: [],
        message: '获取用户任务失败: ' + (error as Error).message
      }
    }
  },

  // 获取我的任务列表（用于编辑周报时的下拉选择）
  async getMy(): Promise<{ success: boolean; data: Task[]; message: string }> {
    return await apiRequest<{ success: boolean; data: Task[]; message: string }>('/tasks/my', {
      method: 'GET'
    })
  }
}

// 周报管理API服务
export const weeklyReportAPI = {
  // 获取周报列表（支持状态过滤）
  async list(status?: string): Promise<{ success: boolean; data: WeeklyReport[]; message: string }> {
    const url = status ? `/weekly-reports?status=${status}` : '/weekly-reports'
    return await apiRequest<{ success: boolean; data: WeeklyReport[]; message: string }>(url, {
      method: 'GET'
    })
  },

  // AI审批周报
  async aiApprove(id: number, aiAnalysisId?: string): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    const url = aiAnalysisId ? `/weekly-reports/${id}/ai-approve?aiAnalysisId=${aiAnalysisId}` : `/weekly-reports/${id}/ai-approve`
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(url, {
      method: 'PUT',
    })
  },

  // 管理员审批周报
  async adminApprove(id: number): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(`/weekly-reports/${id}/admin-approve`, {
      method: 'PUT',
    })
  },

  // 超级管理员终审周报
  async superAdminApprove(id: number): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(`/weekly-reports/${id}/super-admin-approve`, {
      method: 'PUT',
    })
  },

  // 拒绝周报
  async reject(id: number, reason: string): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(`/weekly-reports/${id}/reject`, {
      method: 'PUT',
      body: JSON.stringify({ reason }),
    })
  },

  // 获取单个周报详情
  async get(id: number): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(`/weekly-reports/${id}`, {
      method: 'GET',
    })
  },

  // 更新周报
  async update(id: number, data: Partial<WeeklyReport>): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(`/weekly-reports/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  },

  // 提交周报进入审批流程
  async submit(id: number): Promise<{ success: boolean; data: WeeklyReport; message: string }> {
    return await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>(`/weekly-reports/${id}/submit`, {
      method: 'PUT',
    })
  }
}

// 系统监控API服务
export const systemAPI = {
  // 系统健康检查
  async healthCheck(): Promise<any> {
    return await apiRequest<any>('/health')
  }
}

// 保留原有的API结构用于向后兼容（旧的组件可能还在使用这些）
export const simpleProjectAPI = {
  // 获取已批准的项目列表（兼容性接口，实际调用新的项目API）
  async getApprovedProjects(): Promise<{ success: boolean; data: any[]; message: string }> {
    try {
      const response = await projectAPI.getMyProjects()
      if (response.success) {
        // 过滤出最终批准的项目用于周报创建
        // 只有FINAL_APPROVED状态的项目才能在周报的发展性任务中使用
        const approvedProjects = response.data
          .filter(project => project.approvalStatus === 'FINAL_APPROVED')
          .map(project => ({
            id: project.id,
            projectName: project.name,
            projectContent: project.description,
            projectMembers: project.members,
            expectedResults: project.expectedResults,
            timeline: project.timeline,
            stopLoss: project.stopLoss,
            status: 'APPROVED' as const,
            approvalStatus: project.approvalStatus,
            createdAt: project.createdAt,
            phases: project.phases || []
          }))
        
        return {
          success: true,
          data: approvedProjects,
          message: 'Projects retrieved successfully'
        }
      }
      return response
    } catch (error) {
      console.warn('API调用失败，使用模拟数据:', error)
      return {
        success: true,
        data: [],
        message: '获取项目列表成功'
      }
    }
  },

  // 获取项目详情（兼容性接口，实际调用新的项目API）
  async getById(id: number): Promise<{ success: boolean; data: any; message: string }> {
    try {
      const response = await projectAPI.get(id)
      if (response.success) {
        // 转换为旧格式以保持向后兼容
        const project = response.data
        return {
          success: true,
          data: {
            id: project.id,
            projectName: project.name,
            projectContent: project.description,
            projectMembers: project.members,
            expectedResults: project.expectedResults,
            timeline: project.timeline,
            stopLoss: project.stopLoss,
            approvalStatus: project.approvalStatus,
            aiAnalysisResult: project.aiAnalysisResult?.result || project.rejectionReason,
            createdAt: project.createdAt,
            updatedAt: project.updatedAt,
            createdBy: project.createdBy
          },
          message: 'Project retrieved successfully'
        }
      }
      return response
    } catch (error) {
      console.error('获取项目详情失败:', error)
      return {
        success: false,
        data: null,
        message: '获取项目详情失败: ' + (error as Error).message
      }
    }
  },

  // 强行提交项目（兼容性接口）
  async forceSubmit(id: number): Promise<{ success: boolean; data?: any; message: string }> {
    try {
      const response = await apiRequest<{ success: boolean; data: any; message: string }>(`/simple/projects/${id}/force-submit`, {
        method: 'POST',
      })
      return response
    } catch (error) {
      console.error('强行提交失败:', error)
      return {
        success: false,
        message: '强行提交失败: ' + (error as Error).message
      }
    }
  }
}

export const projectPhaseAPI = {
  // 获取项目的所有阶段（兼容性接口）
  async getByProject(projectId: number): Promise<{ success: boolean; data: ProjectPhase[]; message: string }> {
    try {
      const projectResponse = await projectAPI.get(projectId)
      if (projectResponse.success && projectResponse.data.phases) {
        // 映射后端字段到前端期望的字段名称 - 确保使用后端期望的字段名
        const mappedPhases = projectResponse.data.phases.map((phase: any) => ({
          id: phase.id,
          projectId: phase.projectId,
          phaseName: phase.phaseName,
          // 使用后端期望的字段名（主要字段）
          description: phase.description || phase.phaseDescription || '',
          assignedMembers: phase.assignedMembers || '',
          schedule: phase.schedule || phase.timeline || '',
          expected_results: phase.expected_results || phase.expectedResults || phase.estimatedResults || '',
          // 为了向后兼容，保留前端期望的字段名作为别名
          phaseDescription: phase.description || phase.phaseDescription || '',
          timeline: phase.schedule || phase.timeline || '',
          estimatedResults: phase.expected_results || phase.expectedResults || phase.estimatedResults || '',
          expectedResults: phase.expected_results || phase.expectedResults || phase.estimatedResults || '',
          createdAt: phase.createdAt,
          updatedAt: phase.updatedAt
        }))
        
        return {
          success: true,
          data: mappedPhases,
          message: 'Project phases retrieved successfully'
        }
      }
      return {
        success: true,
        data: [],
        message: 'No phases found for this project'
      }
    } catch (error) {
      console.warn('获取项目阶段失败:', error)
      return {
        success: true,
        data: [],
        message: 'Failed to get project phases'
      }
    }
  },

  // 获取可选择的项目阶段（用于周报创建）
  async getSelectablePhases(projectId: number): Promise<{ success: boolean; data: ProjectPhase[]; message: string }> {
    try {
      return await this.getByProject(projectId)
    } catch (error) {
      console.error('获取可选择的项目阶段失败:', error)
      return {
        success: false,
        data: [],
        message: '获取项目阶段失败: ' + (error as Error).message
      }
    }
  }
}

// 组合服务：周报相关操作（保持兼容性）
export const reportService = {
  // 获取包含任务的完整周报列表
  async getListWithTasks(): Promise<WeeklyReport[]> {
    try {
      console.log('📥 Fetching reports with tasks...')
      
      // 调用新的后端API获取包含深度查询数据的周报列表
      const response = await apiRequest<{ success: boolean; data: any[]; message: string }>('/weekly-reports/my', {
        method: 'GET'
      })
      
      if (response.success) {
        console.log('✅ Reports fetched successfully:', response.data)
        
        // 🐛 DEBUG: 检查第一个报告的数据结构
        if (response.data.length > 0) {
          const firstReport = response.data[0]
          console.log('🐛 First report structure:', firstReport)
          console.log('🐛 Content:', firstReport.content)
          console.log('🐛 Content.Routine_tasks:', firstReport.content?.Routine_tasks)
          console.log('🐛 Content.Developmental_tasks:', firstReport.content?.Developmental_tasks)
        }
        
        // 将后端数据转换为前端期望的格式
        const transformedReports = response.data.map(report => {
          // 提取所有任务并添加相应的标识
          const tasks: any[] = []
          
          // 处理本周汇报的日常性任务
          if (report.content?.routineTasks) {
            report.content.routineTasks.forEach((routineTask: any) => {
              tasks.push({
                id: `routine_${routineTask.task_id}_this_week`,
                taskType: 'ROUTINE',
                reportSection: 'THIS_WEEK_REPORT',
                taskName: routineTask.taskDetails?.taskName || '未知任务',
                actualResults: routineTask.actual_result || '',
                resultDifferenceAnalysis: routineTask.AnalysisofResultDifferences || '',
                taskTemplateId: routineTask.task_id,
                personnelAssignment: routineTask.taskDetails?.personnelAssignment || '',
                timeline: routineTask.taskDetails?.timeline || '',
                quantitativeMetrics: routineTask.taskDetails?.quantitativeMetrics || '',
                expectedResults: routineTask.taskDetails?.expectedResults || ''
              })
            })
          }
          
          // 处理本周汇报的发展性任务
          if (report.content?.developmentalTasks) {
            report.content.developmentalTasks.forEach((devTask: any) => {
              tasks.push({
                id: `dev_${devTask.project_id}_${devTask.phase_id}_this_week`,
                taskType: 'DEVELOPMENT', 
                reportSection: 'THIS_WEEK_REPORT',
                taskName: `${devTask.projectDetails?.projectName || '未知项目'} - ${devTask.phaseDetails?.phaseName || '未知阶段'}`,
                actualResults: devTask.actual_result || '',
                resultDifferenceAnalysis: devTask.AnalysisofResultDifferences || '',
                simpleProjectId: devTask.project_id,
                projectPhaseId: devTask.phase_id,
                simpleProject: devTask.projectDetails ? {
                  id: devTask.project_id,
                  projectName: devTask.projectDetails.projectName,
                  projectContent: devTask.projectDetails.projectContent,
                  projectMembers: devTask.projectDetails.projectMembers,
                  expectedResults: devTask.projectDetails.expectedResults,
                  timeline: devTask.projectDetails.timeline,
                  stopLoss: devTask.projectDetails.stopLoss
                } : null,
                projectPhase: devTask.phaseDetails ? {
                  id: devTask.phase_id,
                  phaseName: devTask.phaseDetails.phaseName,
                  phaseDescription: devTask.phaseDetails.phaseDescription,
                  assignedMembers: devTask.phaseDetails.assignedMembers,
                  timeline: devTask.phaseDetails.timeline,
                  estimatedResults: devTask.phaseDetails.estimatedResults
                } : null
              })
            })
          }
          
          // 处理下周规划的任务（如果有的话）
          if (report.nextWeekPlan?.routineTasks) {
            report.nextWeekPlan.routineTasks.forEach((routineTask: any) => {
              tasks.push({
                id: `routine_${routineTask.task_id}_next_week`,
                taskType: 'ROUTINE',
                reportSection: 'NEXT_WEEK_PLAN',
                taskName: routineTask.taskDetails?.taskName || '未知任务',
                expectedResults: routineTask.taskDetails?.expectedResults || '',
                taskTemplateId: routineTask.task_id,
                personnelAssignment: routineTask.taskDetails?.personnelAssignment || '',
                timeline: routineTask.taskDetails?.timeline || '',
                quantitativeMetrics: routineTask.taskDetails?.quantitativeMetrics || ''
              })
            })
          }
          
          if (report.nextWeekPlan?.developmentalTasks) {
            report.nextWeekPlan.developmentalTasks.forEach((devTask: any) => {
              tasks.push({
                id: `dev_${devTask.project_id}_${devTask.phase_id}_next_week`,
                taskType: 'DEVELOPMENT',
                reportSection: 'NEXT_WEEK_PLAN', 
                taskName: `${devTask.projectDetails?.projectName || '未知项目'} - ${devTask.phaseDetails?.phaseName || '未知阶段'}`,
                expectedResults: devTask.phaseDetails?.estimatedResults || '',
                simpleProjectId: devTask.project_id,
                projectPhaseId: devTask.phase_id,
                simpleProject: devTask.projectDetails,
                projectPhase: devTask.phaseDetails
              })
            })
          }
          
          return {
            id: report.id,
            title: report.title,
            reportWeek: report.reportWeek,
            status: report.approvalStatus,
            createdAt: report.createdAt,
            updatedAt: report.updatedAt,
            additionalNotes: report.additionalNotes,
            developmentOpportunities: report.developmentOpportunities,
            tasks: tasks,
            // AI分析结果字段
            aiAnalysisId: report.aiAnalysisId,
            aiAnalysisResult: report.aiAnalysisResult,
            aiConfidence: report.aiConfidence,
            aiAnalysisStatus: report.aiAnalysisStatus,
            aiAnalysisCompletedAt: report.aiAnalysisCompletedAt
          }
        })
        
        return transformedReports
      } else {
        console.warn('⚠️ Failed to fetch reports:', response.message)
        return []
      }
    } catch (error) {
      console.error('❌ Error fetching reports with tasks:', error)
      throw error
    }
  },

  // 创建完整的周报
  async createWithTasks(reportData: any, tasks: any[], isDraft: boolean = false): Promise<WeeklyReport> {
    try {
      console.log('🚀 Creating weekly report with tasks...', { reportData, tasks })
      
      // 分离本周汇报和下周规划任务
      const thisWeekTasks = tasks.filter(task => task.reportSection === 'THIS_WEEK_REPORT')
      const nextWeekTasks = tasks.filter(task => task.reportSection === 'NEXT_WEEK_PLAN')
      
      // 构建WeeklyReportCreateRequest格式的数据
      const weeklyReportRequest = {
        title: reportData.title || `周报-${new Date().toLocaleDateString()}`,
        reportWeek: reportData.reportWeek || `${new Date().toLocaleDateString()}周`,
        content: {
          routineTasks: thisWeekTasks
            .filter(task => task.taskType === 'ROUTINE')
            .filter(task => task.taskTemplateId) // 只包含有真实数据库ID的任务
            .map(task => ({
              task_id: String(task.taskTemplateId), // 直接使用数据库ID，不回退到临时ID
              actual_result: task.actualResults || '',
              AnalysisofResultDifferences: task.resultDifferenceAnalysis || ''
            })),
          developmentalTasks: thisWeekTasks
            .filter(task => task.taskType === 'DEVELOPMENT') 
            .filter(task => task.simpleProjectId && task.projectPhaseId) // 只包含有真实项目和阶段ID的任务
            .map(task => ({
              project_id: String(task.simpleProjectId), // 直接使用项目ID
              phase_id: String(task.projectPhaseId), // 直接使用阶段ID
              actual_result: task.actualResults || '',
              AnalysisofResultDifferences: task.resultDifferenceAnalysis || ''
            }))
        },
        nextWeekPlan: {
          routineTasks: nextWeekTasks
            .filter(task => task.taskType === 'ROUTINE')
            .filter(task => task.taskTemplateId) // 只包含有真实数据库ID的任务
            .map(task => ({
              task_id: String(task.taskTemplateId) // 直接使用数据库ID
            })),
          developmentalTasks: nextWeekTasks
            .filter(task => task.taskType === 'DEVELOPMENT')
            .filter(task => task.simpleProjectId && task.projectPhaseId) // 只包含有真实项目和阶段ID的任务
            .map(task => ({
              project_id: String(task.simpleProjectId), // 直接使用项目ID
              phase_id: String(task.projectPhaseId) // 直接使用阶段ID
            }))
        },
        additionalNotes: reportData.additionalNotes || '',
        developmentOpportunities: reportData.developmentOpportunities || ''
      }
      
      console.log('📤 Sending weekly report request:', weeklyReportRequest)
      
      // 调用后端API创建周报
      const response = await apiRequest<{ success: boolean; data: WeeklyReport; message: string }>('/weekly-reports', {
        method: 'POST',
        body: JSON.stringify(weeklyReportRequest),
      })
      
      if (response.success) {
        console.log('✅ Weekly report created successfully:', response.data)
        
        // 注意：后端在创建时已经自动触发AI分析，无需额外调用submit接口
        if (!isDraft) {
          console.log('✅ Weekly report created and AI analysis triggered automatically')
        }
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to create weekly report')
      }
    } catch (error) {
      console.error('❌ Error creating report with tasks:', error)
      throw error
    }
  },

  // 强行提交周报 - 当AI拒绝时，强行提交到管理员审核
  async forceSubmit(reportId: number): Promise<void> {
    try {
      console.log('🚀 Force submitting weekly report:', reportId)
      
      const response = await apiRequest<{ success: boolean; message: string }>(`/weekly-reports/${reportId}/force-submit`, {
        method: 'PUT'
      })
      
      if (response.success) {
        console.log('✅ Weekly report force submitted successfully:', response.message)
      } else {
        throw new Error(response.message || 'Failed to force submit weekly report')
      }
    } catch (error) {
      console.error('❌ Error force submitting weekly report:', error)
      throw error
    }
  }
}

// 创建通用的api对象，供组件使用
export const api = {
  get: <T>(url: string) => apiRequest<T>(url, { method: 'GET' }),
  post: <T>(url: string, data?: any) => apiRequest<T>(url, { 
    method: 'POST', 
    body: data ? JSON.stringify(data) : undefined 
  }),
  put: <T>(url: string, data?: any) => apiRequest<T>(url, { 
    method: 'PUT', 
    body: data ? JSON.stringify(data) : undefined 
  }),
  delete: <T>(url: string) => apiRequest<T>(url, { method: 'DELETE' })
}

export default {
  authAPI,
  userAPI,
  projectAPI,
  taskAPI,
  weeklyReportAPI,
  systemAPI,
  // 保留向后兼容性
  reportService,
  simpleProjectAPI,
  projectPhaseAPI
}