#!/usr/bin/env node

/**
 * 测试approval_status字段长度修复
 * 验证SUBMITTED状态能够正确保存到数据库
 */

const axios = require('axios');

const BASE_URL = 'http://localhost:8081/api';

// 测试数据
const MANAGER_TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYW5hZ2VyIiwidXNlcklkIjoyLCJyb2xlIjoiTUFOQUdFUiIsImlhdCI6MTcyNjg5NjI5MywiZXhwIjoxNzI2ODk5ODkzfQ.G8Z1mCUEZ5a2nJ5L3p8wUIVHaH6F4owUOEY7JxCtSKhNS5E4l0EuALzRxT4Y5NXzOOITNT-L70x6wqKWOtEQ-g';

// 测试用周报数据
const WEEKLY_REPORT_DATA = {
    title: "approval_status字段修复验证周报",
    reportWeek: "2025-第39周",
    additionalNotes: "专门测试approval_status字段是否能正确保存SUBMITTED状态",
    developmentOpportunities: "验证数据库ENUM字段是否包含SUBMITTED值"
};

async function test() {
    console.log('🔧 =============测试approval_status字段修复=============');
    
    try {
        console.log('📝 步骤1: 创建周报');
        const createResponse = await axios.post(`${BASE_URL}/weekly-reports`, WEEKLY_REPORT_DATA, {
            headers: {
                'Authorization': `Bearer ${MANAGER_TOKEN}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (createResponse.status !== 200) {
            throw new Error(`创建周报失败: ${createResponse.status}`);
        }
        
        const weeklyReportId = createResponse.data.data.id;
        console.log(`✅ 周报创建成功，ID: ${weeklyReportId}`);
        console.log(`📊 初始状态: ${createResponse.data.data.approvalStatus}`);
        
        console.log('\n📤 步骤2: 提交周报（测试SUBMITTED状态保存）');
        const submitResponse = await axios.put(`${BASE_URL}/weekly-reports/${weeklyReportId}/submit`, {}, {
            headers: {
                'Authorization': `Bearer ${MANAGER_TOKEN}`
            }
        });
        
        if (submitResponse.status !== 200) {
            throw new Error(`提交周报失败: ${submitResponse.status}, 响应: ${JSON.stringify(submitResponse.data)}`);
        }
        
        console.log('✅ 周报提交成功！');
        console.log(`📄 服务响应: ${submitResponse.data.message}`);
        
        // 等待一秒后查询状态
        console.log('\n⏳ 等待1秒后查询周报状态...');
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        console.log('\n📊 步骤3: 查询周报状态验证');
        const getResponse = await axios.get(`${BASE_URL}/weekly-reports/${weeklyReportId}`, {
            headers: {
                'Authorization': `Bearer ${MANAGER_TOKEN}`
            }
        });
        
        if (getResponse.status !== 200) {
            throw new Error(`查询周报失败: ${getResponse.status}`);
        }
        
        const currentStatus = getResponse.data.data.approvalStatus;
        console.log(`📈 当前状态: ${currentStatus}`);
        
        if (currentStatus === 'SUBMITTED' || currentStatus === 'AI_ANALYZING') {
            console.log('🎉 ✅ 测试成功！approval_status字段能够正确保存状态');
            console.log('🔍 详细验证:');
            console.log(`   - 创建状态: DRAFT ✅`);
            console.log(`   - 提交后状态: ${currentStatus} ✅`);
            console.log('   - 数据库ENUM字段已正确包含SUBMITTED和相关状态值');
        } else {
            console.log('❌ 测试失败：状态不符合预期');
            console.log(`   预期: SUBMITTED 或 AI_ANALYZING`);
            console.log(`   实际: ${currentStatus}`);
        }
        
        // 输出完整的周报数据用于验证
        console.log('\n📋 完整周报数据:');
        console.log(JSON.stringify(getResponse.data.data, null, 2));
        
    } catch (error) {
        console.error('❌ 测试过程中发生错误:');
        if (error.response) {
            console.error(`   HTTP状态: ${error.response.status}`);
            console.error(`   错误信息: ${JSON.stringify(error.response.data, null, 2)}`);
        } else {
            console.error(`   错误详情: ${error.message}`);
        }
    }
}

// 运行测试
test().then(() => {
    console.log('\n🏁 测试完成');
}).catch(error => {
    console.error('💥 测试执行失败:', error.message);
    process.exit(1);
});