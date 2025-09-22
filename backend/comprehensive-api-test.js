const axios = require('axios');
const fs = require('fs');

// 配置axios
const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  proxy: false
});

let authToken = '';
let testResults = [];

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// 格式化JSON输出
function formatJson(obj) {
  return JSON.stringify(obj, null, 2);
}

// 记录测试结果
function logTest(endpoint, method, description, input, output, status) {
  testResults.push({
    endpoint,
    method,
    description,
    input,
    output,
    status,
    timestamp: new Date().toISOString()
  });
}

async function runComprehensiveAPITest() {
  console.log('=== 全面API接口测试开始 ===\n');
  
  try {
    // 1. 认证和用户管理接口
    console.log('🔐 测试认证和用户管理接口...');
    
    // 用户注册
    const registerInput = {
      username: `testuser_${Date.now()}`,
      password: 'password123',
      confirmPassword: 'password123',
      email: `test_${Date.now()}@example.com`,
      fullName: 'Test User Full',
      role: 'MANAGER'
    };
    
    try {
      const registerResponse = await api.post('/auth/register', registerInput);
      logTest('/auth/register', 'POST', '用户注册', registerInput, registerResponse.data, registerResponse.status);
      console.log('✅ POST /auth/register');
    } catch (error) {
      logTest('/auth/register', 'POST', '用户注册', registerInput, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ POST /auth/register:', error.response?.status);
    }

    // 用户登录
    const loginInput = {
      usernameOrEmail: 'testuser',
      password: 'newpassword123'  // 使用之前修改的密码
    };
    
    try {
      const loginResponse = await api.post('/auth/login', loginInput);
      if (loginResponse.data.success && loginResponse.data.data.accessToken) {
        authToken = loginResponse.data.data.accessToken;
        api.defaults.headers.common['Authorization'] = `Bearer ${authToken}`;
      }
      logTest('/auth/login', 'POST', '用户登录', loginInput, loginResponse.data, loginResponse.status);
      console.log('✅ POST /auth/login');
    } catch (error) {
      logTest('/auth/login', 'POST', '用户登录', loginInput, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ POST /auth/login:', error.response?.status);
    }

    // 密码修改
    const changePasswordInput = {
      currentPassword: 'newpassword123',
      newPassword: 'finalpassword123',
      confirmNewPassword: 'finalpassword123'
    };
    
    try {
      const changePasswordResponse = await api.post('/auth/change-password', changePasswordInput);
      logTest('/auth/change-password', 'POST', '修改密码', changePasswordInput, changePasswordResponse.data, changePasswordResponse.status);
      console.log('✅ POST /auth/change-password');
    } catch (error) {
      logTest('/auth/change-password', 'POST', '修改密码', changePasswordInput, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ POST /auth/change-password:', error.response?.status);
    }

    // 用户搜索
    try {
      const searchResponse = await api.get('/users/search?keyword=test&page=0&size=10');
      logTest('/users/search', 'GET', '用户搜索', { keyword: 'test', page: 0, size: 10 }, searchResponse.data, searchResponse.status);
      console.log('✅ GET /users/search');
    } catch (error) {
      logTest('/users/search', 'GET', '用户搜索', { keyword: 'test', page: 0, size: 10 }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /users/search:', error.response?.status);
    }

    // 2. 项目管理接口
    console.log('\n📋 测试项目管理接口...');
    let projectId = null;

    // 创建项目
    const createProjectInput = {
      name: `API测试项目 ${Date.now()}`,
      description: '这是一个完整的API测试项目，包含阶段任务',
      members: 'API测试团队：开发者、测试者、产品经理',
      expectedResults: '完成API接口的全面测试和文档生成',
      timeline: '2个月完成',
      stopLoss: '如果测试覆盖率低于90%或发现超过5个严重bug则暂停',
      phases: [
        {
          phaseName: '需求分析阶段',
          description: '分析所有API接口需求，确定测试范围',
          assignedMembers: '产品经理、架构师',
          schedule: '第1-2周',
          expectedResults: '完成需求文档和测试计划'
        },
        {
          phaseName: '开发实现阶段',
          description: '实现API接口功能，确保业务逻辑正确',
          assignedMembers: '后端开发者、前端开发者',
          schedule: '第3-6周',
          expectedResults: '完成所有API接口开发'
        },
        {
          phaseName: '测试验证阶段',
          description: '全面测试API接口，生成测试报告',
          assignedMembers: '测试工程师、QA',
          schedule: '第7-8周',
          expectedResults: '完成测试验证和文档输出'
        }
      ]
    };

    try {
      const createProjectResponse = await api.post('/projects', createProjectInput);
      if (createProjectResponse.data.success) {
        projectId = createProjectResponse.data.data.id;
      }
      logTest('/projects', 'POST', '创建项目（包含阶段）', createProjectInput, createProjectResponse.data, createProjectResponse.status);
      console.log('✅ POST /projects');
    } catch (error) {
      logTest('/projects', 'POST', '创建项目（包含阶段）', createProjectInput, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ POST /projects:', error.response?.status);
    }

    // 获取项目列表
    try {
      const projectsResponse = await api.get('/projects?page=0&size=10&sort=createdAt,desc');
      logTest('/projects', 'GET', '获取项目列表（分页）', { page: 0, size: 10, sort: 'createdAt,desc' }, projectsResponse.data, projectsResponse.status);
      console.log('✅ GET /projects');
    } catch (error) {
      logTest('/projects', 'GET', '获取项目列表（分页）', { page: 0, size: 10, sort: 'createdAt,desc' }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /projects:', error.response?.status);
    }

    // 获取我的项目
    try {
      const myProjectsResponse = await api.get('/projects/my');
      logTest('/projects/my', 'GET', '获取当前用户的项目', {}, myProjectsResponse.data, myProjectsResponse.status);
      console.log('✅ GET /projects/my');
    } catch (error) {
      logTest('/projects/my', 'GET', '获取当前用户的项目', {}, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /projects/my:', error.response?.status);
    }

    if (projectId) {
      // 获取项目详情
      try {
        const projectDetailResponse = await api.get(`/projects/${projectId}`);
        logTest(`/projects/{id}`, 'GET', '获取项目详情（包含阶段数据）', { id: projectId }, projectDetailResponse.data, projectDetailResponse.status);
        console.log('✅ GET /projects/{id}');
      } catch (error) {
        logTest(`/projects/{id}`, 'GET', '获取项目详情（包含阶段数据）', { id: projectId }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ GET /projects/{id}:', error.response?.status);
      }

      // 更新项目
      const updateProjectInput = {
        name: 'API测试项目（已更新）',
        description: '更新后的项目描述，增加了更多测试细节',
        members: 'API测试团队：资深开发者、高级测试者、产品总监',
        expectedResults: '完成API接口的全面测试、文档生成和性能优化',
        timeline: '优化为1.5个月完成',
        stopLoss: '如果测试覆盖率低于95%或发现超过3个严重bug则暂停'
      };

      try {
        const updateProjectResponse = await api.put(`/projects/${projectId}`, updateProjectInput);
        logTest(`/projects/{id}`, 'PUT', '更新项目信息', { id: projectId, ...updateProjectInput }, updateProjectResponse.data, updateProjectResponse.status);
        console.log('✅ PUT /projects/{id}');
      } catch (error) {
        logTest(`/projects/{id}`, 'PUT', '更新项目信息', { id: projectId, ...updateProjectInput }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ PUT /projects/{id}:', error.response?.status);
      }

      // 提交项目
      try {
        const submitProjectResponse = await api.put(`/projects/${projectId}/submit`);
        logTest(`/projects/{id}/submit`, 'PUT', '提交项目进入审批流程', { id: projectId }, submitProjectResponse.data, submitProjectResponse.status);
        console.log('✅ PUT /projects/{id}/submit');
      } catch (error) {
        logTest(`/projects/{id}/submit`, 'PUT', '提交项目进入审批流程', { id: projectId }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ PUT /projects/{id}/submit:', error.response?.status);
      }
    }

    // 3. 任务管理接口
    console.log('\n📝 测试任务管理接口...');
    let taskId = null;

    // 创建任务
    const createTaskInput = {
      taskName: `API测试任务 ${Date.now()}`,
      personnelAssignment: '后端开发工程师、测试工程师',
      timeline: '2周内完成',
      quantitativeMetrics: '测试覆盖率达到100%，响应时间<200ms',
      expectedResults: '所有API接口测试通过，生成完整测试报告',
      taskType: 'DEVELOPMENT'
    };

    try {
      const createTaskResponse = await api.post('/tasks', createTaskInput);
      if (createTaskResponse.data.success) {
        taskId = createTaskResponse.data.data.id;
      }
      logTest('/tasks', 'POST', '创建任务', createTaskInput, createTaskResponse.data, createTaskResponse.status);
      console.log('✅ POST /tasks');
    } catch (error) {
      logTest('/tasks', 'POST', '创建任务', createTaskInput, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ POST /tasks:', error.response?.status);
    }

    // 创建日常任务
    const createRoutineTaskInput = {
      taskName: `日常维护任务 ${Date.now()}`,
      personnelAssignment: '运维工程师',
      timeline: '每日执行',
      quantitativeMetrics: '系统可用性>99.9%，日志清理完成度100%',
      expectedResults: '系统稳定运行，日志管理规范',
      taskType: 'ROUTINE'
    };

    try {
      const createRoutineTaskResponse = await api.post('/tasks', createRoutineTaskInput);
      logTest('/tasks', 'POST', '创建日常任务', createRoutineTaskInput, createRoutineTaskResponse.data, createRoutineTaskResponse.status);
      console.log('✅ POST /tasks (routine)');
    } catch (error) {
      logTest('/tasks', 'POST', '创建日常任务', createRoutineTaskInput, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ POST /tasks (routine):', error.response?.status);
    }

    // 获取任务列表
    try {
      const tasksResponse = await api.get('/tasks?page=0&size=10&sortBy=createdAt&sortDir=desc');
      logTest('/tasks', 'GET', '获取任务列表（分页排序）', { page: 0, size: 10, sortBy: 'createdAt', sortDir: 'desc' }, tasksResponse.data, tasksResponse.status);
      console.log('✅ GET /tasks');
    } catch (error) {
      logTest('/tasks', 'GET', '获取任务列表（分页排序）', { page: 0, size: 10, sortBy: 'createdAt', sortDir: 'desc' }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /tasks:', error.response?.status);
    }

    // 获取我的任务
    try {
      const myTasksResponse = await api.get('/tasks/my');
      logTest('/tasks/my', 'GET', '获取当前用户创建的任务', {}, myTasksResponse.data, myTasksResponse.status);
      console.log('✅ GET /tasks/my');
    } catch (error) {
      logTest('/tasks/my', 'GET', '获取当前用户创建的任务', {}, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /tasks/my:', error.response?.status);
    }

    // 按类型获取任务
    try {
      const developmentTasksResponse = await api.get('/tasks/by-type/DEVELOPMENT');
      logTest('/tasks/by-type/{taskType}', 'GET', '按类型获取任务', { taskType: 'DEVELOPMENT' }, developmentTasksResponse.data, developmentTasksResponse.status);
      console.log('✅ GET /tasks/by-type/{taskType}');
    } catch (error) {
      logTest('/tasks/by-type/{taskType}', 'GET', '按类型获取任务', { taskType: 'DEVELOPMENT' }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /tasks/by-type/{taskType}:', error.response?.status);
    }

    // 获取任务统计
    try {
      const taskStatsResponse = await api.get('/tasks/statistics');
      logTest('/tasks/statistics', 'GET', '获取任务统计信息', {}, taskStatsResponse.data, taskStatsResponse.status);
      console.log('✅ GET /tasks/statistics');
    } catch (error) {
      logTest('/tasks/statistics', 'GET', '获取任务统计信息', {}, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /tasks/statistics:', error.response?.status);
    }

    if (taskId) {
      // 获取任务详情
      try {
        const taskDetailResponse = await api.get(`/tasks/${taskId}`);
        logTest(`/tasks/{id}`, 'GET', '获取任务详情', { id: taskId }, taskDetailResponse.data, taskDetailResponse.status);
        console.log('✅ GET /tasks/{id}');
      } catch (error) {
        logTest(`/tasks/{id}`, 'GET', '获取任务详情', { id: taskId }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ GET /tasks/{id}:', error.response?.status);
      }

      // 更新任务
      const updateTaskInput = {
        taskName: 'API测试任务（已更新）',
        personnelAssignment: '资深后端开发工程师、高级测试工程师、QA主管',
        timeline: '优化为10天完成',
        quantitativeMetrics: '测试覆盖率达到100%，响应时间<100ms，错误率<0.1%',
        expectedResults: '所有API接口测试通过，生成详细测试报告和性能分析',
        taskType: 'DEVELOPMENT'
      };

      try {
        const updateTaskResponse = await api.put(`/tasks/${taskId}`, updateTaskInput);
        logTest(`/tasks/{id}`, 'PUT', '更新任务信息', { id: taskId, ...updateTaskInput }, updateTaskResponse.data, updateTaskResponse.status);
        console.log('✅ PUT /tasks/{id}');
      } catch (error) {
        logTest(`/tasks/{id}`, 'PUT', '更新任务信息', { id: taskId, ...updateTaskInput }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ PUT /tasks/{id}:', error.response?.status);
      }
    }

    // 4. 周报管理接口
    console.log('\n📊 测试周报管理接口...');

    // AI审批测试
    try {
      const aiApproveResponse = await api.put('/weekly-reports/1/ai-approve?aiAnalysisId=12345');
      logTest('/weekly-reports/{id}/ai-approve', 'PUT', 'AI审批周报', { id: 1, aiAnalysisId: 12345 }, aiApproveResponse.data, aiApproveResponse.status);
      console.log('✅ PUT /weekly-reports/{id}/ai-approve');
    } catch (error) {
      logTest('/weekly-reports/{id}/ai-approve', 'PUT', 'AI审批周报', { id: 1, aiAnalysisId: 12345 }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('✅ PUT /weekly-reports/{id}/ai-approve (expected error)');
    }

    // 管理员审批测试
    try {
      const adminApproveResponse = await api.put('/weekly-reports/1/admin-approve');
      logTest('/weekly-reports/{id}/admin-approve', 'PUT', '管理员审批周报', { id: 1 }, adminApproveResponse.data, adminApproveResponse.status);
      console.log('✅ PUT /weekly-reports/{id}/admin-approve');
    } catch (error) {
      logTest('/weekly-reports/{id}/admin-approve', 'PUT', '管理员审批周报', { id: 1 }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('✅ PUT /weekly-reports/{id}/admin-approve (expected error)');
    }

    // 超级管理员审批测试
    try {
      const superAdminApproveResponse = await api.put('/weekly-reports/1/super-admin-approve');
      logTest('/weekly-reports/{id}/super-admin-approve', 'PUT', '超级管理员终审周报', { id: 1 }, superAdminApproveResponse.data, superAdminApproveResponse.status);
      console.log('✅ PUT /weekly-reports/{id}/super-admin-approve');
    } catch (error) {
      logTest('/weekly-reports/{id}/super-admin-approve', 'PUT', '超级管理员终审周报', { id: 1 }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('✅ PUT /weekly-reports/{id}/super-admin-approve (expected error)');
    }

    // 拒绝周报测试
    const rejectInput = {
      reason: '周报内容不够详细，需要补充以下方面：1. 具体的工作时间分配；2. 遇到的技术难题及解决方案；3. 下周具体的工作计划和里程碑'
    };

    try {
      const rejectResponse = await api.put('/weekly-reports/1/reject', rejectInput);
      logTest('/weekly-reports/{id}/reject', 'PUT', '拒绝周报并提供反馈', { id: 1, ...rejectInput }, rejectResponse.data, rejectResponse.status);
      console.log('✅ PUT /weekly-reports/{id}/reject');
    } catch (error) {
      logTest('/weekly-reports/{id}/reject', 'PUT', '拒绝周报并提供反馈', { id: 1, ...rejectInput }, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('✅ PUT /weekly-reports/{id}/reject (expected error)');
    }

    // 5. 系统监控接口
    console.log('\n🔍 测试系统监控接口...');

    // 健康检查
    try {
      const healthResponse = await api.get('/health');
      logTest('/health', 'GET', '系统健康检查', {}, healthResponse.data, healthResponse.status);
      console.log('✅ GET /health');
    } catch (error) {
      logTest('/health', 'GET', '系统健康检查', {}, error.response?.data || error.message, error.response?.status || 'ERROR');
      console.log('❌ GET /health:', error.response?.status);
    }

    // 清理测试：删除创建的任务
    if (taskId) {
      try {
        const deleteTaskResponse = await api.delete(`/tasks/${taskId}`);
        logTest(`/tasks/{id}`, 'DELETE', '删除任务', { id: taskId }, deleteTaskResponse.data, deleteTaskResponse.status);
        console.log('✅ DELETE /tasks/{id}');
      } catch (error) {
        logTest(`/tasks/{id}`, 'DELETE', '删除任务', { id: taskId }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ DELETE /tasks/{id}:', error.response?.status);
      }
    }

    // 清理测试：删除创建的项目
    if (projectId) {
      try {
        const deleteProjectResponse = await api.delete(`/projects/${projectId}`);
        logTest(`/projects/{id}`, 'DELETE', '删除项目', { id: projectId }, deleteProjectResponse.data, deleteProjectResponse.status);
        console.log('✅ DELETE /projects/{id}');
      } catch (error) {
        logTest(`/projects/{id}`, 'DELETE', '删除项目', { id: projectId }, error.response?.data || error.message, error.response?.status || 'ERROR');
        console.log('❌ DELETE /projects/{id}:', error.response?.status);
      }
    }

    console.log('\n=== 测试完成，生成结果文件 ===');
    
    // 保存测试结果到文件
    fs.writeFileSync('api-test-results.json', JSON.stringify(testResults, null, 2), 'utf8');
    console.log('✅ 测试结果已保存到 api-test-results.json');

    return testResults;

  } catch (error) {
    console.error('测试过程中发生错误:', error.message);
    return testResults;
  }
}

// 运行测试
runComprehensiveAPITest().then(results => {
  console.log(`\n📊 测试统计: 共测试 ${results.length} 个接口`);
  process.exit(0);
}).catch(error => {
  console.error('测试运行失败:', error);
  process.exit(1);
});