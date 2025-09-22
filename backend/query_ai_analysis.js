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

// 直接查询数据库中的AI分析结果
async function queryAIAnalysisResults(token) {
    console.log('🤖 查询AI分析结果详情...\n');
    
    // 方法1: 直接查询ai_analysis_results表中aiAnalysisId=99999的记录
    const mysqlQuery = `
        SELECT 
            id,
            report_id,
            analysis_type,
            analysis_status,
            suggestions,
            improvement_areas,
            positive_aspects,
            risk_assessment,
            overall_score,
            detailed_feedback,
            analyzed_at,
            created_at
        FROM ai_analysis_results 
        WHERE id = 99999
        LIMIT 1;
    `;
    
    console.log('📊 SQL查询语句:');
    console.log(mysqlQuery);
    console.log('');
    
    // 方法2: 如果有API端点来查询AI分析结果
    const options = {
        hostname: 'localhost',
        port: 8081,
        path: '/api/ai-analysis/99999',
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    };

    try {
        console.log('📡 尝试通过API查询AI分析结果...');
        const response = await makeRequest(options);
        console.log(`   状态码: ${response.status}`);
        
        if (response.status === 200 && response.data) {
            console.log('✅ 成功获取AI分析结果:');
            console.log(JSON.stringify(response.data, null, 2));
            return response.data;
        } else if (response.status === 404) {
            console.log('⚠️ AI分析API端点不存在，这是预期的');
        } else {
            console.log('❌ API查询失败:');
            console.log(JSON.stringify(response.data, null, 2));
        }
    } catch (error) {
        console.log('⚠️ API查询出错（可能端点不存在）:', error.message);
    }
    
    return null;
}

// 查询周报详情中的AI分析信息
async function queryReportWithAIAnalysis(token) {
    console.log('📋 查询最新周报的AI分析信息...\n');
    
    // 首先获取最新的周报
    const listOptions = {
        hostname: 'localhost',
        port: 8081,
        path: '/api/weekly-reports/my',
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    };

    try {
        const listResponse = await makeRequest(listOptions);
        console.log(`📥 周报列表查询状态码: ${listResponse.status}`);
        
        if (listResponse.status === 200 && listResponse.data.data && listResponse.data.data.length > 0) {
            const reports = listResponse.data.data;
            console.log(`📊 找到 ${reports.length} 个周报`);
            
            // 找到最新的已通过AI分析的周报
            const aiApprovedReport = reports.find(r => r.approvalStatus === 'AI_APPROVED' && r.aiAnalysisId);
            
            if (aiApprovedReport) {
                console.log(`\n🎯 找到AI分析通过的周报:`);
                console.log(`   📋 周报ID: ${aiApprovedReport.id}`);
                console.log(`   📝 标题: ${aiApprovedReport.title}`);
                console.log(`   📊 状态: ${aiApprovedReport.approvalStatus}`);
                console.log(`   🤖 AI分析ID: ${aiApprovedReport.aiAnalysisId}`);
                console.log(`   📅 创建时间: ${aiApprovedReport.createdAt}`);
                console.log(`   🔄 更新时间: ${aiApprovedReport.updatedAt}`);
                
                // 查询该周报的详细信息
                const detailOptions = {
                    hostname: 'localhost',
                    port: 8081,
                    path: `/api/weekly-reports/${aiApprovedReport.id}`,
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                };
                
                const detailResponse = await makeRequest(detailOptions);
                console.log(`\n📥 周报详情查询状态码: ${detailResponse.status}`);
                
                if (detailResponse.status === 200 && detailResponse.data.data) {
                    console.log('\n🔍 周报详情数据:');
                    console.log(JSON.stringify(detailResponse.data.data, null, 2));
                    
                    const detail = detailResponse.data.data;
                    if (detail.aiAnalysisId) {
                        console.log(`\n🤖 AI分析ID确认: ${detail.aiAnalysisId}`);
                        console.log('💡 说明: AI分析结果应该存储在 ai_analysis_results 表中');
                        console.log('📝 建议: 需要创建API端点来查询AI分析详情，或直接查询数据库');
                    }
                }
                
                return aiApprovedReport;
            } else {
                console.log('⚠️ 没有找到AI分析通过的周报');
            }
        } else {
            console.log('❌ 获取周报列表失败');
            console.log(JSON.stringify(listResponse.data, null, 2));
        }
    } catch (error) {
        console.error('❌ 查询周报失败:', error.message);
    }
    
    return null;
}

// 模拟AI分析结果内容（基于实际业务逻辑）
function simulateAIAnalysisContent() {
    console.log('\n🤖 模拟AI分析结果内容（基于aiAnalysisId=99999）:\n');
    
    const analysisResult = {
        id: 99999,
        analysisType: 'COMPREHENSIVE',
        analysisStatus: 'COMPLETED',
        overallScore: 85,
        suggestions: [
            '建议在日常任务执行中加强细节记录',
            '发展性任务的进度跟踪可以更加量化',
            '下周规划的时间安排需要更合理'
        ],
        improvementAreas: [
            '任务执行效率',
            '结果量化表述',
            '风险识别能力'
        ],
        positiveAspects: [
            '任务完成度较高',
            '工作态度积极',
            '团队协作良好'
        ],
        riskAssessment: '低风险，整体表现稳定',
        detailedFeedback: {
            routine_tasks: {
                score: 80,
                feedback: '日常任务完成质量良好，建议加强执行过程的记录'
            },
            developmental_tasks: {
                score: 90,
                feedback: '发展性任务展现出良好的项目推进能力'
            },
            planning_quality: {
                score: 85,
                feedback: '下周规划合理，目标明确'
            }
        },
        analyzedAt: '2025-09-21T10:30:00',
        createdAt: '2025-09-21T10:30:00'
    };
    
    console.log('📊 AI分析详细结果:');
    console.log(JSON.stringify(analysisResult, null, 2));
    
    console.log('\n🎯 AI分析总结:');
    console.log(`   📈 总体评分: ${analysisResult.overallScore}/100`);
    console.log(`   💡 主要建议: ${analysisResult.suggestions.join(', ')}`);
    console.log(`   ⚠️ 改进领域: ${analysisResult.improvementAreas.join(', ')}`);
    console.log(`   ✅ 积极方面: ${analysisResult.positiveAspects.join(', ')}`);
    console.log(`   🛡️ 风险评估: ${analysisResult.riskAssessment}`);
    
    return analysisResult;
}

// 登录用户
async function loginUser() {
    console.log('🔐 用户登录...');
    
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
            console.log(`   👤 用户: ${response.data.data.user.username}`);
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

// Main execution
async function main() {
    console.log('🔍 AI分析结果查询工具\n');
    
    try {
        // 登录
        const token = await loginUser();
        if (!token) {
            console.log('❌ 登录失败，无法继续');
            return;
        }
        
        // 查询AI分析结果
        await queryAIAnalysisResults(token);
        
        // 查询周报中的AI分析信息  
        await queryReportWithAIAnalysis(token);
        
        // 模拟显示AI分析内容
        simulateAIAnalysisContent();
        
        console.log('\n📋 数据库查询建议:');
        console.log('   💾 可以直接查询数据库中的AI分析结果:');
        console.log('   📊 SELECT * FROM ai_analysis_results WHERE id = 99999;');
        console.log('   🔗 或者建议创建专门的AI分析结果查询API端点');
        
    } catch (error) {
        console.error('❌ 执行出错:', error);
    }
}

// Run the query
main();