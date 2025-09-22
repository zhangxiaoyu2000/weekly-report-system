#!/usr/bin/env node

const http = require('http');

// Helper function to make HTTP requests
function makeRequest(options, data = null) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => {
                body += chunk;
            });
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    headers: res.headers,
                    body: body
                });
            });
        });

        req.on('error', (err) => {
            reject(err);
        });

        if (data) {
            req.write(data);
        }
        req.end();
    });
}

async function testInterface(name, options, data = null, expectedStatus = [200, 201, 204]) {
    try {
        const result = await makeRequest(options, data);
        const success = expectedStatus.includes(result.statusCode);
        
        console.log(`${success ? '✅' : '❌'} ${name}: ${result.statusCode}`);
        
        if (!success && result.body) {
            try {
                const parsed = JSON.parse(result.body);
                const errorMsg = parsed.message || parsed.error || 'Unknown error';
                console.log(`   ❌ Error: ${errorMsg.substring(0, 80)}${errorMsg.length > 80 ? '...' : ''}`);
            } catch {
                const truncated = result.body.length > 80 ? 
                    result.body.substring(0, 80) + '...' : result.body;
                console.log(`   ❌ Error: ${truncated}`);
            }
        }
        
        return { success, status: result.statusCode, body: result.body };
    } catch (error) {
        console.log(`❌ ${name}: Connection Error - ${error.message}`);
        return { success: false, status: 0, body: error.message };
    }
}

async function fullSystemAudit() {
    console.log('=== 系统接口全面审计 ===\n');
    
    let successCount = 0;
    let totalCount = 0;
    let accessToken = null;
    let userId = null;

    // 1. 获取认证令牌
    console.log('📝 1. 认证系统测试:');
    const loginResult = await testInterface(
        '用户登录', 
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/login',
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        },
        JSON.stringify({ usernameOrEmail: 'admin', password: 'admin123' })
    );
    
    totalCount++;
    if (loginResult.success) {
        successCount++;
        try {
            const loginData = JSON.parse(loginResult.body);
            accessToken = loginData.data.accessToken;
            userId = loginData.data.user.id;
            console.log(`   🔑 获得访问令牌，用户ID: ${userId}`);
        } catch (e) {
            console.log('   ⚠️ 无法提取访问令牌');
        }
    }

    // 用户注册测试
    const registerResult = await testInterface(
        '用户注册',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/register',
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        },
        JSON.stringify({
            username: 'testuser' + Date.now(),
            email: 'test' + Date.now() + '@example.com',
            password: 'password123',
            confirmPassword: 'password123',
            role: 'MANAGER'
        }),
        [201]
    );
    totalCount++;
    if (registerResult.success) successCount++;

    // 退出登录测试
    if (accessToken) {
        const logoutResult = await testInterface(
            '用户退出',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/auth/logout',
                method: 'POST',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (logoutResult.success) successCount++;
    }

    // 检查用户名可用性
    const checkUsernameResult = await testInterface(
        '检查用户名可用性',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/check-username?username=testcheck',
            method: 'GET'
        }
    );
    totalCount++;
    if (checkUsernameResult.success) successCount++;

    // 检查邮箱可用性
    const checkEmailResult = await testInterface(
        '检查邮箱可用性',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/auth/check-email?email=test@check.com',
            method: 'GET'
        }
    );
    totalCount++;
    if (checkEmailResult.success) successCount++;

    console.log('\n👥 2. 用户管理测试:');
    if (accessToken) {
        // 获取用户资料
        const profileResult = await testInterface(
            '获取个人资料',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users/profile',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (profileResult.success) successCount++;

        // 更新用户资料
        const updateProfileResult = await testInterface(
            '更新个人资料',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users/profile',
                method: 'PUT',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                firstName: 'UpdatedFirst',
                lastName: 'UpdatedLast'
            })
        );
        totalCount++;
        if (updateProfileResult.success) successCount++;

        // 获取用户列表
        const userListResult = await testInterface(
            '获取用户列表',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (userListResult.success) successCount++;

        // 搜索用户
        const userSearchResult = await testInterface(
            '搜索用户',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users/search?keyword=admin',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (userSearchResult.success) successCount++;

        // 快速获取用户列表
        const fastUserListResult = await testInterface(
            '快速获取用户列表',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/users/fast',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (fastUserListResult.success) successCount++;

        // 根据用户ID获取用户
        if (userId) {
            const getUserResult = await testInterface(
                '根据ID获取用户',
                {
                    hostname: 'localhost',
                    port: 8081,
                    path: `/api/users/${userId}`,
                    method: 'GET',
                    headers: { 'Authorization': `Bearer ${accessToken}` }
                }
            );
            totalCount++;
            if (getUserResult.success) successCount++;
        }
    }

    console.log('\n📋 3. 任务管理测试:');
    if (accessToken && userId) {
        // 获取任务列表
        const taskListResult = await testInterface(
            '获取任务列表',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (taskListResult.success) successCount++;

        // 创建任务
        const createTaskResult = await testInterface(
            '创建任务',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                taskName: `测试任务 ${Date.now()}`,
                taskType: 'DEVELOPMENT',
                createdBy: userId,
                personnelAssignment: '测试人员',
                timeline: '1周'
            }),
            [201]
        );
        totalCount++;
        if (createTaskResult.success) successCount++;

        // 获取我的任务
        const myTasksResult = await testInterface(
            '获取我的任务',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks/my',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (myTasksResult.success) successCount++;

        // 按类型获取任务
        const tasksByTypeResult = await testInterface(
            '按类型获取任务',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/tasks/by-type/DEVELOPMENT',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (tasksByTypeResult.success) successCount++;
    }

    console.log('\n🤖 4. AI服务测试:');
    if (accessToken) {
        // AI健康检查
        const aiHealthResult = await testInterface(
            'AI健康检查',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/health',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (aiHealthResult.success) successCount++;

        // AI指标
        const aiMetricsResult = await testInterface(
            'AI服务指标',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/metrics',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (aiMetricsResult.success) successCount++;

        // AI生成建议
        const aiSuggestionsResult = await testInterface(
            'AI生成建议',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/generate-suggestions',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                reportId: 1,
                analysisType: 'SUMMARY'
            })
        );
        totalCount++;
        if (aiSuggestionsResult.success) successCount++;

        // AI分析周报
        const aiAnalyzeReportResult = await testInterface(
            'AI分析周报',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/analyze-report/1',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                analysisType: 'SUMMARY'
            })
        );
        totalCount++;
        if (aiAnalyzeReportResult.success) successCount++;

        // 获取AI分析结果
        const aiAnalysisResult = await testInterface(
            '获取AI分析结果',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/analysis-result/1',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (aiAnalysisResult.success) successCount++;

        // AI项目洞察
        const aiProjectInsightResult = await testInterface(
            'AI项目洞察',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/project-insight/1',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (aiProjectInsightResult.success) successCount++;

        // AI项目分析
        const aiProjectAnalysisResult = await testInterface(
            'AI项目分析',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/analyze/project',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                projectId: 1,
                analysisType: 'SUMMARY'
            })
        );
        totalCount++;
        if (aiProjectAnalysisResult.success) successCount++;

        // AI周报分析
        const aiWeeklyAnalysisResult = await testInterface(
            'AI周报分析',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/ai/analyze/weekly-report',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                reportId: 1,
                analysisType: 'SUMMARY'
            })
        );
        totalCount++;
        if (aiWeeklyAnalysisResult.success) successCount++;
    }

    console.log('\n📊 5. 项目管理测试:');
    if (accessToken && userId) {
        // 获取项目列表
        const projectListResult = await testInterface(
            '获取项目列表',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/projects',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (projectListResult.success) successCount++;

        // 创建项目
        const createProjectResult = await testInterface(
            '创建项目',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/projects',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                name: `测试项目 ${Date.now()}`,
                description: '这是一个测试项目',
                members: '测试成员',
                expectedResults: '预期结果',
                timeline: '3个月',
                stopLoss: '如果预算超支50%则暂停'
            }),
            [201]
        );
        totalCount++;
        if (createProjectResult.success) successCount++;

        // 获取项目详情
        const getProjectResult = await testInterface(
            '获取项目详情',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/projects/1',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            },
            null,
            [200, 404] // 404也是正常的，表示项目不存在
        );
        totalCount++;
        if (getProjectResult.success) successCount++;
    }

    console.log('\n📝 6. 周报管理测试:');
    if (accessToken && userId) {
        // 获取我的周报
        const myReportsResult = await testInterface(
            '获取我的周报',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports/my',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (myReportsResult.success) successCount++;

        // 获取待审批周报
        const pendingReportsResult = await testInterface(
            '获取待审批周报',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports/pending',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (pendingReportsResult.success) successCount++;

        // 创建周报
        const createReportResult = await testInterface(
            '创建周报',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports',
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                }
            },
            JSON.stringify({
                title: `测试周报 ${Date.now()}`,
                startDate: '2025-09-16',
                endDate: '2025-09-20',
                content: {
                    thisWeekSummary: '本周工作总结',
                    nextWeekPlan: '下周工作计划',
                    routineTasks: [],
                    developmentTasks: []
                }
            }),
            [200, 201]
        );
        totalCount++;
        if (createReportResult.success) successCount++;

        // 获取周报详情
        const getReportResult = await testInterface(
            '获取周报详情',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/weekly-reports/1',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            },
            null,
            [200, 404] // 404也是正常的
        );
        totalCount++;
        if (getReportResult.success) successCount++;
    }

    console.log('\n🏥 7. 健康检查测试:');
    // 基础健康检查
    const healthResult = await testInterface(
        '基础健康检查',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/health',
            method: 'GET'
        }
    );
    totalCount++;
    if (healthResult.success) successCount++;

    // 认证健康检查
    if (accessToken) {
        const authHealthResult = await testInterface(
            '认证健康检查',
            {
                hostname: 'localhost',
                port: 8081,
                path: '/api/health/authenticated',
                method: 'GET',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );
        totalCount++;
        if (authHealthResult.success) successCount++;
    }

    console.log('\n🔧 8. 测试和调试接口:');
    // 简单测试接口
    const simpleHelloResult = await testInterface(
        '简单问候接口',
        {
            hostname: 'localhost',
            port: 8081,
            path: '/api/simple/hello',
            method: 'GET'
        }
    );
    totalCount++;
    if (simpleHelloResult.success) successCount++;

    console.log('\n📈 === 最终统计结果 ===');
    console.log(`总接口数: ${totalCount}`);
    console.log(`成功接口: ${successCount}`);
    console.log(`失败接口: ${totalCount - successCount}`);
    console.log(`成功率: ${((successCount / totalCount) * 100).toFixed(1)}%`);
    console.log(`不可用接口数: ${totalCount - successCount}`);

    if (totalCount - successCount > 0) {
        console.log('\n❌ 不可用接口列表:');
        console.log('详见上方测试结果中标记为 ❌ 的接口');
    } else {
        console.log('\n🎉 所有接口均可正常使用!');
    }

    console.log('\n=== 审计完成 ===');
}

fullSystemAudit();