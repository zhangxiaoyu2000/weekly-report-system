#!/usr/bin/env node

const https = require('https');
const http = require('http');

// Configuration
const BASE_URL = 'http://localhost:8081/api';

// Test data  
const USER_CREDENTIALS = {
    usernameOrEmail: 'manager1',
    password: 'Manager123@'
};

// Test weekly report data specifically to trigger AI analysis
const WEEKLY_REPORT_DATA = {
    title: "AI分析日志测试周报-2025年第38周",
    reportWeek: "2025-38", 
    userId: 10004,
    additionalNotes: "这是一个专门用于测试AI分析日志输出的周报，包含丰富的内容供AI进行分析",
    developmentOpportunities: "测试AI分析功能的详细日志输出，验证分析结果是否正确记录",
    content: {
        routine_tasks: [
            {
                task_id: "1",
                actual_result: "完成了重要的数据库重构工作，优化了查询性能",
                analysisofResultDifferences: "实际执行比预期更顺利，提前两天完成"
            }
        ],
        developmental_tasks: [
            {
                project_id: "1",
                phase_id: "1",
                actual_result: "成功实现了AI分析功能的日志优化",
                analysisofResultDifferences: "增加了详细的JSON解析和结构化输出，超出了原始预期"
            }
        ]
    }
};

// Helper function for HTTP requests
function makeRequest(options, data = null) {
    return new Promise((resolve, reject) => {
        const protocol = options.protocol === 'https:' ? https : http;
        const req = protocol.request(options, (res) => {
            let body = '';
            res.setEncoding('utf8');
            res.on('data', (chunk) => {
                body += chunk;
            });
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(body);
                    resolve({ status: res.statusCode, headers: res.headers, data: parsed });
                } catch (e) {
                    resolve({ status: res.statusCode, headers: res.headers, data: body });
                }
            });
        });

        req.on('error', (e) => {
            reject(e);
        });

        if (data) {
            req.write(JSON.stringify(data));
        }
        req.end();
    });
}

// 登录用户
async function loginUser() {
    console.log('🔐 Step 1: 用户登录...');
    
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: '/api/auth/login',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    try {
        const response = await makeRequest(options, USER_CREDENTIALS);
        console.log(`   状态码: ${response.status}`);
        
        if (response.status === 200 && response.data.data && response.data.data.accessToken) {
            console.log('✅ 登录成功');
            console.log(`   👤 用户: ${response.data.data.user.username} (ID: ${response.data.data.user.id})`);
            return response.data.data.accessToken;
        } else {
            console.log('❌ 登录失败');
            return null;
        }
    } catch (error) {
        console.error('❌ 登录请求失败:', error.message);
        return null;
    }
}

// 创建周报
async function submitWeeklyReport(token) {
    console.log('\n📝 Step 2: 创建测试周报...');
    
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: '/api/weekly-reports',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };

    console.log('📤 发送的周报数据:');
    console.log(JSON.stringify(WEEKLY_REPORT_DATA, null, 2));

    try {
        const response = await makeRequest(options, WEEKLY_REPORT_DATA);
        console.log(`\n📥 创建周报响应:`);
        console.log(`   状态码: ${response.status}`);
        console.log(`   响应: ${JSON.stringify(response.data, null, 2)}`);
        
        if (response.status === 200 && response.data.data && response.data.data.id) {
            console.log('✅ 周报创建成功');
            const reportId = response.data.data.id;
            console.log(`   📊 初始状态: ${response.data.data.approvalStatus}`);
            console.log(`   📋 周报ID: ${reportId}`);
            return reportId;
        } else {
            console.log('❌ 周报创建失败');
            return null;
        }
    } catch (error) {
        console.error('❌ 周报创建请求失败:', error.message);
        return null;
    }
}

// 提交周报（触发AI分析）
async function submitForApproval(token, reportId) {
    console.log('\n🚀 Step 3: 提交审批（触发AI分析）...');
    console.log('⚠️ 注意: 这将触发AI分析，请查看服务端日志以获取AI分析结果详情');
    
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: `/api/weekly-reports/${reportId}/submit`,
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };

    try {
        const response = await makeRequest(options);
        console.log(`\n📥 提交审批响应:`);
        console.log(`   状态码: ${response.status}`);
        console.log(`   响应: ${JSON.stringify(response.data, null, 2)}`);
        
        if (response.status === 200) {
            console.log('✅ 周报提交审批成功');
            console.log('🤖 AI分析应该正在进行中...');
            console.log('💡 请查看服务端日志文件以获取详细的AI分析结果');
            return true;
        } else {
            console.log('❌ 周报提交审批失败');
            return false;
        }
    } catch (error) {
        console.error('❌ 提交审批请求失败:', error.message);
        return false;
    }
}

// 等待AI分析完成并检查结果
async function waitAndCheckResult(token, reportId) {
    console.log('\n⏳ Step 4: 等待AI分析完成...');
    
    // 等待几秒让AI分析完成
    console.log('   等待15秒让AI分析完成...');
    await new Promise(resolve => setTimeout(resolve, 15000));
    
    // 检查周报状态
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: `/api/weekly-reports/${reportId}`,
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    };

    try {
        const response = await makeRequest(options);
        console.log(`\n📥 检查结果响应:`);
        console.log(`   状态码: ${response.status}`);
        
        if (response.status === 200 && response.data.data) {
            const report = response.data.data;
            console.log('✅ 获取周报状态成功');
            console.log(`   📊 当前状态: ${report.approvalStatus}`);
            console.log(`   🤖 AI分析ID: ${report.aiAnalysisId || '未设置'}`);
            console.log(`   📋 周报ID: ${report.id}`);
            console.log(`   📝 标题: ${report.title}`);
            console.log(`   🔄 更新时间: ${report.updatedAt}`);
            
            if (report.aiAnalysisId) {
                console.log(`\n🎯 AI分析ID已生成: ${report.aiAnalysisId}`);
                console.log('📋 详细的AI分析结果应该已经输出在服务端日志中');
                console.log('🔍 建议查看服务端控制台或日志文件获取完整的AI分析内容');
            } else {
                console.log('⚠️ AI分析ID尚未生成，可能分析仍在进行中');
            }
            
            return true;
        } else {
            console.log('❌ 获取周报状态失败');
            return false;
        }
    } catch (error) {
        console.error('❌ 检查结果请求失败:', error.message);
        return false;
    }
}

// Main test execution
async function runAIAnalysisTest() {
    console.log('🧪 开始AI分析日志测试...\n');
    console.log('🎯 目标: 触发AI分析并通过日志查看详细的分析结果');
    console.log('📋 说明: AI分析结果将以详细日志的形式输出在服务端');
    console.log('══════════════════════════════════════════════════════════\n');
    
    try {
        // Step 1: Login
        const token = await loginUser();
        if (!token) {
            console.log('❌ 测试中止：登录失败');
            return;
        }

        // Step 2: Submit weekly report
        const reportId = await submitWeeklyReport(token);
        if (!reportId) {
            console.log('❌ 测试中止：周报创建失败');
            return;
        }

        // Step 3: Submit for approval (triggers AI analysis)
        const submitted = await submitForApproval(token, reportId);
        if (!submitted) {
            console.log('❌ 测试中止：提交审批失败');
            return;
        }

        // Step 4: Wait and check result
        const checkSuccess = await waitAndCheckResult(token, reportId);
        if (!checkSuccess) {
            console.log('❌ 测试中止：检查结果失败');
            return;
        }

        console.log('\n🎉 AI分析测试完成！');
        console.log('\n📊 测试总结:');
        console.log('   ✅ 周报创建: 成功');
        console.log('   ✅ 提交审批: 成功（触发AI分析）');
        console.log('   ✅ 状态检查: 成功');
        console.log('\n💡 下一步:');
        console.log('   🔍 查看服务端日志以获取详细的AI分析结果');
        console.log('   📋 日志中应包含完整的AI分析JSON结果和解析的关键信息');
        console.log('   🤖 搜索日志中的 "🤖" 标记来快速定位AI分析相关内容');

    } catch (error) {
        console.error('❌ 测试执行错误:', error);
    }
}

// Run the test
runAIAnalysisTest();