#!/usr/bin/env node

/**
 * AI分析输入输出数据完整演示
 * 展示从周报创建到AI分析完成的完整数据流
 */

const mysql = require('mysql2/promise');

// 模拟AI分析的完整输入数据
const mockAIAnalysisInput = {
    weeklyReportId: 42,
    analysisPrompt: `你是一位经验丰富的工作汇报审核专家，请评估以下周报的质量：

周报信息：
- 标题：AI分析输入输出演示周报-2025年第39周
- 报告周次：2025-第39周
- 内容：完成了系统架构优化和性能调优工作，日常任务执行良好（从任务关联表中获取）
- 额外说明：测试AI分析的完整输入输出数据展示功能
- 发展机会：学习新的AI集成技术，提升系统自动化水平

请从以下维度进行全面评估：
1. 工作内容的完整性和详细程度
2. 工作成果的具体性和可衡量性
3. 问题识别的准确性和深度
4. 下周规划的合理性和可行性
5. 整体表达的专业性和清晰度

请以结构化的JSON格式返回评估结果：
{
    "overallScore": 85,
    "isPass": true,
    "proposal": "详细的评估意见和改进建议，包含具体的改进方向",
    "qualityScore": 0.85,
    "riskLevel": "LOW",
    "suggestions": ["建议在日常任务执行中加强细节记录", "发展性任务的进度跟踪可以更加量化"],
    "improvementAreas": ["任务执行效率", "结果量化表述"],
    "positiveAspects": ["任务完成度较高", "工作态度积极"],
    "riskAssessment": "低风险，整体表现稳定",
    "detailedFeedback": {
        "routine_tasks": {"score": 80, "feedback": "日常任务完成质量良好"},
        "developmental_tasks": {"score": 90, "feedback": "发展性任务展现出良好的项目推进能力"},
        "planning_quality": {"score": 85, "feedback": "下周规划合理，目标明确"}
    }
}`,
    analysisType: "SUMMARY",
    context: "42",
    requestMetadata: {
        userId: 1,
        reportWeek: "2025-第39周",
        submissionTime: "2025-09-21T05:15:00.000Z",
        aiProvider: "deepseek",
        modelVersion: "deepseek-chat"
    }
};

// 模拟AI分析的完整输出数据
const mockAIAnalysisOutput = {
    analysisId: "ai-analysis-20250921-051500-42",
    providerUsed: "deepseek-chat",
    executionTime: 2.85, // 秒
    tokenUsage: {
        promptTokens: 1245,
        completionTokens: 892,
        totalTokens: 2137
    },
    result: `{
  "overallScore": 82,
  "isPass": true,
  "proposal": "该周报展现了良好的工作完成度和规划意识。工作内容涵盖了系统架构优化和性能调优等技术性工作，体现了较强的专业能力。额外说明部分明确了测试目的，发展机会识别合理。建议在下次汇报中加强量化指标的使用，如具体的性能提升百分比、优化前后的对比数据等。",
  "qualityScore": 0.82,
  "riskLevel": "LOW",
  "suggestions": [
    "建议在系统优化工作中提供具体的性能指标对比",
    "发展机会部分可以增加具体的学习计划和时间安排",
    "日常任务可以加强与项目目标的关联性描述"
  ],
  "improvementAreas": [
    "量化指标表述",
    "工作成果展示",
    "未来规划详细度"
  ],
  "positiveAspects": [
    "工作内容技术含量较高",
    "发展意识明确",
    "测试目的清晰",
    "整体表达专业"
  ],
  "riskAssessment": "低风险。工作完成质量稳定，发展方向明确，无明显问题点。建议保持当前工作节奏，适当加强成果量化。",
  "detailedFeedback": {
    "routine_tasks": {
      "score": 78,
      "feedback": "日常任务完成情况良好，但可以增加更多执行细节和具体成果"
    },
    "developmental_tasks": {
      "score": 85,
      "feedback": "系统架构优化工作体现了较强的技术能力，建议补充性能提升的具体数据"
    },
    "planning_quality": {
      "score": 83,
      "feedback": "发展规划方向正确，建议制定更具体的学习计划和里程碑"
    }
  }
}`,
    confidence: 0.87,
    qualityMetrics: {
        responseStructure: "excellent",
        jsonValidity: "valid",
        contentCompleteness: "high",
        recommendationQuality: "good"
    },
    processingSteps: [
        "1. 周报内容解析与结构化分析",
        "2. 工作完整性和具体性评估",
        "3. 专业性和清晰度检查",
        "4. 风险等级确定和改进建议生成",
        "5. JSON格式化和质量验证"
    ]
};

async function demonstrateAIAnalysisFlow() {
    console.log('🤖 ===============AI分析输入输出完整演示===============');
    
    const connection = await mysql.createConnection({
        host: '127.0.0.1',
        port: 3307,
        user: 'root',
        password: 'rootpass123',
        database: 'qr_auth_dev'
    });
    
    try {
        // 步骤1: 创建演示周报
        console.log('\n📝 步骤1: 创建演示周报');
        const [insertResult] = await connection.execute(`
            INSERT INTO weekly_reports (
                user_id, title, report_week, additional_notes, 
                development_opportunities, approval_status,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
        `, [
            1, // user_id (admin)
            'AI分析输入输出演示周报-2025年第39周',
            '2025-第39周',
            '测试AI分析的完整输入输出数据展示功能',
            '学习新的AI集成技术，提升系统自动化水平',
            'AI_ANALYZING'
        ]);
        
        const weeklyReportId = insertResult.insertId;
        mockAIAnalysisInput.weeklyReportId = weeklyReportId;
        console.log(`✅ 周报创建成功，ID: ${weeklyReportId}`);
        
        // 步骤2: 展示AI分析输入数据
        console.log('\n🔍 步骤2: AI分析输入数据展示');
        console.log('==================输入数据结构==================');
        console.log('📊 基础信息:');
        console.log(`   周报ID: ${mockAIAnalysisInput.weeklyReportId}`);
        console.log(`   分析类型: ${mockAIAnalysisInput.analysisType}`);
        console.log(`   用户ID: ${mockAIAnalysisInput.requestMetadata.userId}`);
        console.log(`   报告周次: ${mockAIAnalysisInput.requestMetadata.reportWeek}`);
        console.log(`   AI提供商: ${mockAIAnalysisInput.requestMetadata.aiProvider}`);
        console.log(`   模型版本: ${mockAIAnalysisInput.requestMetadata.modelVersion}`);
        
        console.log('\n📝 完整分析提示词:');
        console.log('---BEGIN PROMPT---');
        console.log(mockAIAnalysisInput.analysisPrompt);
        console.log('---END PROMPT---');
        
        console.log(`\n📏 提示词统计:`);
        console.log(`   字符数: ${mockAIAnalysisInput.analysisPrompt.length}`);
        console.log(`   行数: ${mockAIAnalysisInput.analysisPrompt.split('\n').length}`);
        console.log(`   预估token数: ~${Math.ceil(mockAIAnalysisInput.analysisPrompt.length / 2.5)}`);
        
        // 步骤3: 模拟AI分析过程
        console.log('\n⚙️ 步骤3: AI分析处理过程');
        console.log('==================AI分析执行==================');
        
        for (let i = 0; i < mockAIAnalysisOutput.processingSteps.length; i++) {
            const step = mockAIAnalysisOutput.processingSteps[i];
            console.log(`🔄 ${step}`);
            await new Promise(resolve => setTimeout(resolve, 500)); // 模拟处理时间
        }
        
        console.log(`⏱️ 分析耗时: ${mockAIAnalysisOutput.executionTime}秒`);
        console.log(`🎯 置信度: ${mockAIAnalysisOutput.confidence * 100}%`);
        
        // 步骤4: 展示AI分析输出数据
        console.log('\n📊 步骤4: AI分析输出数据展示');
        console.log('==================输出数据结构==================');
        
        console.log('🔧 技术元数据:');
        console.log(`   分析ID: ${mockAIAnalysisOutput.analysisId}`);
        console.log(`   使用模型: ${mockAIAnalysisOutput.providerUsed}`);
        console.log(`   执行时间: ${mockAIAnalysisOutput.executionTime}秒`);
        console.log(`   置信度: ${mockAIAnalysisOutput.confidence}`);
        
        console.log('\n📈 Token使用统计:');
        console.log(`   提示词Token: ${mockAIAnalysisOutput.tokenUsage.promptTokens}`);
        console.log(`   生成Token: ${mockAIAnalysisOutput.tokenUsage.completionTokens}`);
        console.log(`   总Token: ${mockAIAnalysisOutput.tokenUsage.totalTokens}`);
        
        console.log('\n🎯 质量指标:');
        console.log(`   响应结构: ${mockAIAnalysisOutput.qualityMetrics.responseStructure}`);
        console.log(`   JSON有效性: ${mockAIAnalysisOutput.qualityMetrics.jsonValidity}`);
        console.log(`   内容完整性: ${mockAIAnalysisOutput.qualityMetrics.contentCompleteness}`);
        console.log(`   建议质量: ${mockAIAnalysisOutput.qualityMetrics.recommendationQuality}`);
        
        console.log('\n📋 完整AI分析结果:');
        console.log('---BEGIN AI RESULT---');
        console.log(mockAIAnalysisOutput.result);
        console.log('---END AI RESULT---');
        
        // 步骤5: 解析并展示结构化结果
        console.log('\n🧮 步骤5: 结构化结果解析');
        console.log('==================结果解析==================');
        
        try {
            const parsedResult = JSON.parse(mockAIAnalysisOutput.result);
            
            console.log('📊 核心评估指标:');
            console.log(`   ✅ 总体评分: ${parsedResult.overallScore}/100`);
            console.log(`   ✅ 是否通过: ${parsedResult.isPass ? '通过' : '不通过'}`);
            console.log(`   ✅ 质量评分: ${parsedResult.qualityScore}`);
            console.log(`   ✅ 风险等级: ${parsedResult.riskLevel}`);
            
            console.log('\n💡 改进建议:');
            parsedResult.suggestions.forEach((suggestion, index) => {
                console.log(`   ${index + 1}. ${suggestion}`);
            });
            
            console.log('\n⚠️ 需要改进的方面:');
            parsedResult.improvementAreas.forEach((area, index) => {
                console.log(`   ${index + 1}. ${area}`);
            });
            
            console.log('\n🌟 积极方面:');
            parsedResult.positiveAspects.forEach((aspect, index) => {
                console.log(`   ${index + 1}. ${aspect}`);
            });
            
            console.log('\n🔍 详细反馈:');
            Object.entries(parsedResult.detailedFeedback).forEach(([category, feedback]) => {
                console.log(`   📌 ${category}:`);
                console.log(`      评分: ${feedback.score}/100`);
                console.log(`      反馈: ${feedback.feedback}`);
            });
            
        } catch (parseError) {
            console.log('❌ JSON解析失败:', parseError.message);
        }
        
        // 步骤6: 数据库存储演示
        console.log('\n💾 步骤6: 数据库存储演示');
        console.log('==================数据持久化==================');
        
        // 创建AI分析结果记录
        const [aiResultInsert] = await connection.execute(`
            INSERT INTO ai_analysis_results (
                report_id, analysis_type, result, confidence, 
                model_version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, NOW(), NOW())
        `, [
            weeklyReportId,
            'SUMMARY',
            mockAIAnalysisOutput.result,
            mockAIAnalysisOutput.confidence,
            mockAIAnalysisOutput.providerUsed
        ]);
        
        const aiAnalysisId = aiResultInsert.insertId;
        console.log(`✅ AI分析结果已存储，ID: ${aiAnalysisId}`);
        
        // 更新周报状态
        await connection.execute(`
            UPDATE weekly_reports 
            SET approval_status = 'AI_APPROVED', ai_analysis_id = ?
            WHERE id = ?
        `, [aiAnalysisId, weeklyReportId]);
        
        console.log(`✅ 周报状态已更新: AI_ANALYZING → AI_APPROVED`);
        
        // 步骤7: 验证完整数据流
        console.log('\n🔎 步骤7: 验证完整数据流');
        console.log('==================数据流验证==================');
        
        const [weeklyReportQuery] = await connection.execute(`
            SELECT wr.*, air.result as ai_result, air.confidence, air.model_version
            FROM weekly_reports wr
            LEFT JOIN ai_analysis_results air ON wr.ai_analysis_id = air.id
            WHERE wr.id = ?
        `, [weeklyReportId]);
        
        if (weeklyReportQuery.length > 0) {
            const report = weeklyReportQuery[0];
            console.log('📋 完整数据流验证:');
            console.log(`   周报ID: ${report.id}`);
            console.log(`   标题: ${report.title}`);
            console.log(`   状态: ${report.approval_status}`);
            console.log(`   AI分析ID: ${report.ai_analysis_id}`);
            console.log(`   AI模型: ${report.model_version}`);
            console.log(`   置信度: ${report.confidence}`);
            console.log(`   结果长度: ${report.ai_result ? report.ai_result.length : 0} 字符`);
        }
        
        // 清理测试数据
        await connection.execute("DELETE FROM ai_analysis_results WHERE id = ?", [aiAnalysisId]);
        await connection.execute("DELETE FROM weekly_reports WHERE id = ?", [weeklyReportId]);
        console.log(`\n🧹 测试数据已清理`);
        
        // 步骤8: 总结
        console.log('\n🎉 步骤8: 完整流程总结');
        console.log('==================流程总结==================');
        console.log('✅ 输入数据结构: 周报信息 + 分析参数 + 元数据');
        console.log('✅ AI处理过程: 5步结构化分析流程');
        console.log('✅ 输出数据结构: 评分 + 建议 + 详细反馈 + 技术元数据');
        console.log('✅ 数据持久化: 数据库存储 + 状态更新');
        console.log('✅ 质量保证: 置信度评估 + JSON验证 + 结构完整性检查');
        
        console.log('\n📊 关键数据指标:');
        console.log(`   输入提示词长度: ${mockAIAnalysisInput.analysisPrompt.length} 字符`);
        console.log(`   输出结果长度: ${mockAIAnalysisOutput.result.length} 字符`);
        console.log(`   处理时间: ${mockAIAnalysisOutput.executionTime} 秒`);
        console.log(`   Token消耗: ${mockAIAnalysisOutput.tokenUsage.totalTokens}`);
        console.log(`   分析置信度: ${mockAIAnalysisOutput.confidence * 100}%`);
        
    } finally {
        await connection.end();
    }
}

// 运行演示
demonstrateAIAnalysisFlow().then(() => {
    console.log('\n🏁 AI分析输入输出演示完成');
    console.log('💡 现在您可以看到AI分析的完整数据流，包括:');
    console.log('   - 输入: 结构化的分析提示词 + 元数据');
    console.log('   - 处理: 5步分析流程 + 质量控制');
    console.log('   - 输出: JSON格式的评估结果 + 技术指标');
    console.log('   - 存储: 数据库持久化 + 状态管理');
}).catch(error => {
    console.error('\n💥 演示失败:', error.message);
    process.exit(1);
});