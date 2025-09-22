#!/usr/bin/env node

/**
 * 周报状态筛选功能测试脚本
 * 演示管理员如何根据不同状态查看周报
 */

const baseUrl = 'http://localhost:8081/api';

// 测试用的超级管理员token
const token = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzdXBlcmFkbWluIiwicm9sZXMiOiJST0xFX1NVUEVSX0FETUlOIiwiaWF0IjoxNzU4NTI0ODE4LCJleHAiOjE3NTg1Mjg0MTgsInVzZXJJZCI6MTAwMDAsImZ1bGxOYW1lIjoic3VwZXJhZG1pbiIsImVtYWlsIjoic3VwZXJhZG1pbkB3ZWVrbHlyZXBvcnQuY29tIn0.o9DJySkNtmeQmzUSoYIL_1m6TLhWERZMcXigbJxdS6wCsw4uvPAZJHMx61wp_W7rgc0wOX9ktQm-oSq7SQQ2-w';

async function testStatusFiltering() {
    console.log('🔍 测试管理员周报审核状态筛选功能\n');

    // 测试不同状态的筛选
    const statuses = [
        'ADMIN_REVIEWING',   // 待审核
        'ADMIN_APPROVED',    // 已通过
        'ADMIN_REJECTED'     // 已拒绝
    ];

    for (const status of statuses) {
        try {
            const response = await fetch(`${baseUrl}/weekly-reports?status=${status}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const data = await response.json();
            
            if (data.success) {
                console.log(`📋 ${getStatusName(status)}: ${data.data.length} 个周报`);
                
                // 显示前3个周报的基本信息
                data.data.slice(0, 3).forEach((report, index) => {
                    console.log(`   ${index + 1}. ${report.title} (ID: ${report.id}, 用户: ${report.userId})`);
                    if (report.aiAnalysisResult) {
                        console.log(`      AI分析: ${report.aiAnalysisResult.substring(0, 50)}...`);
                    }
                });
            } else {
                console.log(`❌ ${getStatusName(status)}: 查询失败 - ${data.message}`);
            }
        } catch (error) {
            console.log(`❌ ${getStatusName(status)}: 请求错误 - ${error.message}`);
        }
        console.log('');
    }

    // 测试获取所有周报（无状态筛选）
    try {
        const response = await fetch(`${baseUrl}/weekly-reports`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const data = await response.json();
        if (data.success) {
            console.log(`📊 所有周报: ${data.data.length} 个周报`);
            
            // 按状态统计
            const statusCount = {};
            data.data.forEach(report => {
                statusCount[report.approvalStatus] = (statusCount[report.approvalStatus] || 0) + 1;
            });
            
            console.log('📈 状态分布:');
            Object.entries(statusCount).forEach(([status, count]) => {
                console.log(`   ${getStatusName(status)}: ${count} 个`);
            });
        }
    } catch (error) {
        console.log(`❌ 获取所有周报失败: ${error.message}`);
    }
}

function getStatusName(status) {
    const statusNames = {
        'ADMIN_REVIEWING': '待审核',
        'ADMIN_APPROVED': '已通过', 
        'ADMIN_REJECTED': '已拒绝',
        'AI_ANALYZING': 'AI分析中',
        'AI_APPROVED': 'AI已通过',
        'AI_REJECTED': 'AI已拒绝'
    };
    return statusNames[status] || status;
}

// 运行测试
testStatusFiltering().catch(console.error);