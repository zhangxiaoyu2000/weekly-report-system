package com.weeklyreport.weeklyreport.service;

import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import com.weeklyreport.ai.service.AIAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 周报AI分析服务 - 核心业务服务
 * 负责AI分析流程：周报内容分析、质量评分、风险评估
 */
@Service
@Transactional
public class WeeklyReportAIService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportAIService.class);

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Value("${weekly-report.ai.confidence-threshold:0.7}")
    private double weeklyReportConfidenceThreshold;

    /**
     * 启动AI分析流程
     */
    public void startAIAnalysis(Long reportId) {
        logger.info("开始AI分析周报: {}", reportId);
        
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        if (!report.isAIProcessing()) {
            throw new RuntimeException("只能分析正在AI处理中的周报，当前状态: " + report.getStatus());
        }

        try {
            // 调用AI分析服务
            com.weeklyreport.ai.entity.AIAnalysisResult analysisResult = 
                aiAnalysisService.analyzeWeeklyReportSync(report);
            
            // 解析AI分析结果并更新周报
            processAIResult(report, analysisResult);
            
            // 保存更新后的周报
            weeklyReportRepository.save(report);
            
            logger.info("AI分析完成，周报ID: {}", reportId);
            
        } catch (Exception e) {
            logger.error("AI分析失败，周报ID: {}", reportId, e);
            report.aiReject("AI分析失败: " + e.getMessage());
            weeklyReportRepository.save(report);
            throw new RuntimeException("AI分析失败", e);
        }
    }

    /**
     * 处理AI分析结果
     */
    private void processAIResult(WeeklyReport report, com.weeklyreport.ai.entity.AIAnalysisResult analysisResult) {
        // 设置AI分析结果ID - AI分析时间等信息存储在AIAnalysisResult实体中
        report.setAiAnalysisId(analysisResult.getId());

        // 统一格式的调试日志
        Double confidence = analysisResult.getConfidence() != null ? analysisResult.getConfidence() : 0.0;
        boolean completed = analysisResult.getStatus() == com.weeklyreport.ai.entity.AIAnalysisResult.AnalysisStatus.COMPLETED;
        boolean shouldApprove = completed && analysisResult.getConfidence() != null &&
            analysisResult.getConfidence() >= weeklyReportConfidenceThreshold;

        String logPattern = "🔍[状态检查] 周报ID={}, 状态={}, 置信度={}, 阈值={}, 决策={}, 触发点={}";
        String decision = shouldApprove ? "APPROVE" : "REJECT";
        logger.info(logPattern, report.getId(),
            report.getStatus(),
            confidence, weeklyReportConfidenceThreshold, decision,
            "WeeklyReportAIService.processAIResult");

        // 根据AI分析结果决定审批状态 (统一使用0.7阈值)
        if (shouldApprove) {
            report.aiApprove();
            logger.info("✅ AI分析通过，置信度: {}，已转入待审核", analysisResult.getConfidence());
        } else {
            String rejectionReason = String.format(
                "AI分析置信度过低: %.0f%%，需要改进内容质量 (阈值: %.0f%%)",
                confidence * 100,
                weeklyReportConfidenceThreshold * 100
            );
            report.aiReject(rejectionReason);
            logger.info("🚫 AI分析置信度不足: {}，已拒绝", confidence);
        }
    }

    /**
     * 计算内容质量评分 - 备用方法，通常由AI服务提供评分
     */
    private double calculateQualityScore(WeeklyReport report) {
        double score = 0.0;
        
        // 标题质量 (20%)
        if (report.getTitle() != null && report.getTitle().length() >= 5) {
            score += 0.2;
        }
        
        // 内容完整性 (40%)
        String content = report.getContent();
        if (content != null && content.length() >= 50) {
            score += 0.4;
            // 内容越丰富，得分越高
            if (content.length() >= 200) {
                score += 0.1;
            }
        }
        
        // 发展性任务 (20%)
        if (report.getDevelopmentOpportunities() != null && 
            report.getDevelopmentOpportunities().length() >= 20) {
            score += 0.2;
        }
        
        // 任务报告完整性 (20%)
        if (report.getTaskReports() != null && !report.getTaskReports().isEmpty()) {
            score += 0.2;
        }
        
        return Math.min(1.0, score); // 确保不超过1.0
    }

    /**
     * 检查周报是否需要AI分析
     */
    public boolean needsAIAnalysis(WeeklyReport report) {
        return report.isAIProcessing() && report.getAiAnalysisId() == null;
    }

    /**
     * 获取AI分析状态 - 基于4状态系统
     */
    public String getAIAnalysisStatus(Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 使用4状态系统的状态判断
        return switch (report.getStatus()) {
            case DRAFT -> "草稿，待提交";
            case AI_PROCESSING -> report.getAiAnalysisId() == null ? "等待AI分析" : "AI分析中";
            case ADMIN_REVIEWING -> "AI分析通过，等待管理员审核";
            case APPROVED -> "管理员审核已通过";
            case REJECTED -> "已拒绝：" + (report.getRejectionReason() != null ? report.getRejectionReason() : "未提供原因");
        };
    }
}
