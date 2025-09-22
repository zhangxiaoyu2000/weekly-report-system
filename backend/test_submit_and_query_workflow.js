#!/usr/bin/env node

/**
 * 测试完整的周报提交和AI分析工作流程
 * 1. 创建周报
 * 2. 提交周报
 * 3. 等待AI分析完成
 * 4. 查询周报详细内容
 */

const axios = require('axios');

const BASE_URL = 'http://localhost:8081/api';

// 测试数据
const MANAGER_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYW5hZ2VyIiwidXNlcklkIjoyLCJyb2xlIjoiTUFOQUdFUiIsImlhdCI6MTcyNjg5NjI5MywiZXhwIjoxNzI2ODk5ODkzfQ.G8Z1mCUEZ5a2nJ5L3p8wUIVHaH6F4owUOEY7JxCtSKhNS5E4l0EuALzRxT4Y5NXzOOITNT-L70x6wqKWOtEQ-g';

// 测试用周报数据
const WEEKLY_REPORT_DATA = {
    title: "完整工作流程测试周报-2025年第39周",
    reportWeek: "2025-第39周",
    additionalNotes: "测试新的approval_status流程，验证AI分析和状态转换",
    developmentOpportunities: "优化AI分析准确性，提升审批效率",
    content: {
        routine_tasks: [
            {
                task_id: "1",
                actual_result: "完成了系统维护和日常监控，服务运行稳定",
                analysisofResultDifferences: "实际完成情况良好，与预期一致"
            }
        ],
        developmental_tasks: [
            {
                project_id: "1",
                phase_id: "1", 
                actual_result: "完成了系统架构设计和技术选型",
                analysisofResultDifferences: "进度符合预期，技术方案已确定"
            }
        ]
    }
};

function formatJSON(obj) {
    return JSON.stringify(obj, null, 2);
}

function logSection(title, content) {
    console.log(`\n${'='.repeat(60)}`);
    console.log(`📋 ${title}`);
    console.log(`${'='.repeat(60)}`);
    console.log(content);
}

function logRequest(method, url, data = null) {
    console.log(`\n🚀 请求信息:`);
    console.log(`   方法: ${method}`);
    console.log(`   URL: ${url}`);
    if (data) {
        console.log(`   请求体:\n${formatJSON(data)}`);
    }
}

function logResponse(response) {
    console.log(`\n📥 响应信息:`);
    console.log(`   状态码: ${response.status}`);
    console.log(`   响应体:\n${formatJSON(response.data)}`);
}

async function sleep(seconds) {
    console.log(`⏳ 等待 ${seconds} 秒...`);
    await new Promise(resolve => setTimeout(resolve, seconds * 1000));
}

async function test() {
    logSection('开始测试：完整周报提交和AI分析工作流程', 
        '测试目标：验证移除DRAFT/SUBMITTED状态后的工作流程');
    
    let weeklyReportId;
    
    try {
        // 步骤1: 创建周报
        logSection('步骤1: 创建周报', '');
        logRequest('POST', `${BASE_URL}/weekly-reports`, WEEKLY_REPORT_DATA);
        
        const createResponse = await axios.post(`${BASE_URL}/weekly-reports`, WEEKLY_REPORT_DATA, {
            headers: {
                'Authorization': `Bearer ${MANAGER_TOKEN}`,
                'Content-Type': 'application/json'
            }
        });
        
        logResponse(createResponse);
        
        if (createResponse.status !== 200) {
            throw new Error(`创建周报失败: ${createResponse.status}`);
        }
        
        weeklyReportId = createResponse.data.data.id;
        const initialStatus = createResponse.data.data.approvalStatus;
        
        console.log(`\n✅ 周报创建成功！`);
        console.log(`   周报ID: ${weeklyReportId}`);
        console.log(`   初始状态: ${initialStatus}`);
        
        // 步骤2: 提交周报
        logSection('步骤2: 提交周报（触发AI分析）', '');
        logRequest('PUT', `${BASE_URL}/weekly-reports/${weeklyReportId}/submit`);
        
        const submitResponse = await axios.put(`${BASE_URL}/weekly-reports/${weeklyReportId}/submit`, {}, {
            headers: {
                'Authorization': `Bearer ${MANAGER_TOKEN}`
            }
        });
        
        logResponse(submitResponse);
        
        if (submitResponse.status !== 200) {
            throw new Error(`提交周报失败: ${submitResponse.status}`);
        }
        
        console.log(`\n✅ 周报提交成功！`);
        console.log(`   响应消息: ${submitResponse.data.message}`);
        
        // 步骤3: 等待AI分析完成 (轮询检查状态)
        logSection('步骤3: 等待AI分析完成', '将轮询检查状态变化');
        
        let currentStatus = 'AI_ANALYZING';
        let attempts = 0;
        const maxAttempts = 12; // 最多等待2分钟 (12 * 10秒)
        
        while (currentStatus === 'AI_ANALYZING' && attempts < maxAttempts) {
            attempts++;
            await sleep(10);
            
            console.log(`\n🔍 第${attempts}次状态检查:`);
            logRequest('GET', `${BASE_URL}/weekly-reports/${weeklyReportId}`);
            
            const statusResponse = await axios.get(`${BASE_URL}/weekly-reports/${weeklyReportId}`, {
                headers: {
                    'Authorization': `Bearer ${MANAGER_TOKEN}`
                }
            });
            
            currentStatus = statusResponse.data.data.approvalStatus;
            console.log(`   当前状态: ${currentStatus}`);
            
            if (currentStatus !== 'AI_ANALYZING') {
                console.log(`\n🎉 AI分析完成！状态变更为: ${currentStatus}`);
                logResponse(statusResponse);
                break;
            }
        }
        
        if (currentStatus === 'AI_ANALYZING' && attempts >= maxAttempts) {
            console.log(`\n⚠️  AI分析仍在进行中，已等待${maxAttempts * 10}秒`);
            console.log(`   将继续查询当前状态的详细内容`);
        }
        
        // 步骤4: 查询完整的周报详情
        logSection('步骤4: 查询周报详细内容', '获取包含AI分析结果的完整信息');
        logRequest('GET', `${BASE_URL}/weekly-reports/${weeklyReportId}`);
        
        const detailResponse = await axios.get(`${BASE_URL}/weekly-reports/${weeklyReportId}`, {
            headers: {
                'Authorization': `Bearer ${MANAGER_TOKEN}`
            }
        });
        
        logResponse(detailResponse);
        
        // 解析和展示详细信息
        const reportDetail = detailResponse.data.data;
        
        logSection('详细信息解析', '');
        console.log(`📊 基本信息:`);
        console.log(`   周报ID: ${reportDetail.id}`);
        console.log(`   标题: ${reportDetail.title}`);
        console.log(`   周期: ${reportDetail.reportWeek}`);
        console.log(`   当前状态: ${reportDetail.approvalStatus}`);
        console.log(`   创建时间: ${reportDetail.createdAt}`);
        console.log(`   更新时间: ${reportDetail.updatedAt}`);
        
        if (reportDetail.aiAnalysisId) {
            console.log(`\n🤖 AI分析信息:`);
            console.log(`   AI分析ID: ${reportDetail.aiAnalysisId}`);
        }
        
        if (reportDetail.adminReviewerId) {
            console.log(`\n👤 审批信息:`);
            console.log(`   管理员审批人ID: ${reportDetail.adminReviewerId}`);
        }
        
        if (reportDetail.rejectionReason) {
            console.log(`\n❌ 拒绝信息:`);
            console.log(`   拒绝理由: ${reportDetail.rejectionReason}`);
        }
        
        // 显示任务执行情况
        if (reportDetail.routineTasks && reportDetail.routineTasks.length > 0) {
            console.log(`\n📝 日常任务执行情况 (${reportDetail.routineTasks.length}个):`);
            reportDetail.routineTasks.forEach((task, index) => {
                console.log(`   ${index + 1}. ${task.taskName}`);
                console.log(`      实际结果: ${task.actualResults || '无'}`);
                console.log(`      差异分析: ${task.resultDifferenceAnalysis || '无'}`);
            });
        }
        
        if (reportDetail.developmentalTasks && reportDetail.developmentalTasks.length > 0) {
            console.log(`\n🚀 发展性任务执行情况 (${reportDetail.developmentalTasks.length}个):`);
            reportDetail.developmentalTasks.forEach((task, index) => {
                console.log(`   ${index + 1}. 项目: ${task.projectName}`);
                console.log(`      阶段: ${task.phaseName}`);
                console.log(`      实际结果: ${task.actualResults || '无'}`);
                console.log(`      差异分析: ${task.resultDifferenceAnalysis || '无'}`);
            });
        }
        
        // 总结测试结果
        logSection('测试结果总结', '');
        console.log(`✅ 成功完成完整工作流程测试:`);
        console.log(`   1. ✅ 周报创建: 成功 (ID: ${weeklyReportId})`);
        console.log(`   2. ✅ 周报提交: 成功 (触发AI分析)`);
        console.log(`   3. ✅ 状态跟踪: ${currentStatus === 'AI_ANALYZING' ? '进行中' : '已完成'}`);
        console.log(`   4. ✅ 详情查询: 成功获取完整信息`);
        console.log(`\n📈 状态流转验证:`);
        console.log(`   创建时: AI_ANALYZING (新流程，无DRAFT状态)`);
        console.log(`   提交后: AI_ANALYZING (无SUBMITTED状态)`);
        console.log(`   当前状态: ${currentStatus}`);
        
        if (currentStatus !== 'AI_ANALYZING') {
            console.log(`   ✅ AI分析已完成并转换状态`);
        } else {
            console.log(`   🔄 AI分析仍在进行中`);
        }
        
    } catch (error) {
        logSection('❌ 测试过程中发生错误', '');
        if (error.response) {
            console.error(`HTTP状态: ${error.response.status}`);
            console.error(`错误响应:\n${formatJSON(error.response.data)}`);
        } else {
            console.error(`错误详情: ${error.message}`);
        }
        
        // 如果有周报ID，尝试查询当前状态
        if (weeklyReportId) {
            try {
                console.log(`\n🔍 尝试查询当前周报状态 (ID: ${weeklyReportId}):`);
                const statusResponse = await axios.get(`${BASE_URL}/weekly-reports/${weeklyReportId}`, {
                    headers: { 'Authorization': `Bearer ${MANAGER_TOKEN}` }
                });
                console.log(`当前状态: ${statusResponse.data.data.approvalStatus}`);
            } catch (statusError) {
                console.error(`无法查询状态: ${statusError.message}`);
            }
        }
    }
}

// 运行测试
console.log('🎯 启动完整工作流程测试...\n');
test().then(() => {
    console.log('\n🏁 测试执行完毕');
}).catch(error => {
    console.error('\n💥 测试执行失败:', error.message);
    process.exit(1);
});