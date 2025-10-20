package com.weeklyreport.weeklyreport.service;

import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 周报状态管理器 - 统一状态转换入口
 *
 * 目标：
 * 1. 消除多路径并发更新状态的竞态条件
 * 2. 使用悲观锁保证状态更新的原子性
 * 3. 集中化状态转换逻辑和置信度验证
 */
@Service
public class WeeklyReportStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportStatusManager.class);

    @Value("${weekly-report.ai.confidence-threshold:0.7}")
    private double confidenceThreshold;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private AIAnalysisResultRepository aiAnalysisResultRepository;

    @Autowired
    private WeeklyReportNotificationService notificationService;

    /**
     * 处理AI分析结果 - 统一入口（使用悲观锁）
     *
     * @param reportId 周报ID
     * @param analysisResultId AI分析结果ID
     */
    @Transactional
    public void processAIAnalysisResult(Long reportId, Long analysisResultId) {
        logger.info("🔒 [状态管理器] 开始处理AI分析结果，周报ID: {}, 分析结果ID: {}", reportId, analysisResultId);

        // 1. 使用悲观锁加载周报（防止并发修改）
        WeeklyReport report = weeklyReportRepository.findByIdForUpdate(reportId)
            .orElseThrow(() -> new RuntimeException("周报不存在: " + reportId));

        // 2. 加载AI分析结果
        AIAnalysisResult result = aiAnalysisResultRepository.findById(analysisResultId)
            .orElseThrow(() -> new RuntimeException("AI分析结果不存在: " + analysisResultId));

        // 3. 记录状态转换前的状态
        WeeklyReport.ReportStatus currentStatus = report.getStatus();
        logger.info("📝 [状态管理器] 当前状态: {}, 分析状态: {}, 置信度: {}",
            currentStatus, result.getStatus(), result.getConfidence());

        // 4. 验证只能从AI_PROCESSING状态转换
        if (currentStatus != WeeklyReport.ReportStatus.AI_PROCESSING) {
            logger.warn("⚠️ [状态管理器] 周报ID {} 不在AI_PROCESSING状态，当前状态: {}, 跳过处理",
                reportId, currentStatus);
            return;
        }

        // 5. 设置AI分析结果ID
        report.setAiAnalysisId(analysisResultId);

        // 6. 统一判断逻辑
        if (shouldApprove(result)) {
            // AI分析通过 → PENDING_REVIEW
            approveByAI(report, result);
        } else {
            // AI分析未通过 → REJECTED
            rejectByAI(report, result);
        }

        // 7. 保存更新（悲观锁会在事务提交时释放）
        weeklyReportRepository.save(report);

        logger.info("✅ [状态管理器] 状态更新完成，周报ID: {}, 状态: {} → {}",
            reportId, currentStatus, report.getStatus());
    }

    /**
     * 判断是否应该批准
     */
    private boolean shouldApprove(AIAnalysisResult result) {
        if (result.getStatus() != AIAnalysisResult.AnalysisStatus.COMPLETED) {
            logger.info("🔍 [置信度检查] 分析状态: {}, 判定: 拒绝", result.getStatus());
            return false;
        }

        Double confidence = result.getConfidence();
        if (confidence == null) {
            logger.info("🔍 [置信度检查] 置信度为null, 判定: 拒绝");
            return false;
        }

        boolean approve = confidence >= confidenceThreshold;
        logger.info("🔍 [置信度检查] 置信度: {}, 阈值: {}, 判定: {}",
            confidence, confidenceThreshold, approve ? "通过" : "拒绝");

        return approve;
    }

    /**
     * AI批准流程
     */
    private void approveByAI(WeeklyReport report, AIAnalysisResult result) {
        Double confidence = result.getConfidence();
        logger.info("✅ [AI批准] 周报ID: {}, 置信度: {}", report.getId(), confidence);

        // 清除拒绝原因
        report.setRejectionReason(null);

        // 调用实体的状态转换方法
        try {
            report.aiApprove();
        } catch (IllegalStateException e) {
            // 理论上不应该发生，因为已经预先检查过了
            logger.error("❌ [AI批准] 状态转换失败: {}", e.getMessage());
            throw e;
        }

        // 发送通知
        try {
            notificationService.handleAIAnalysisCompleted(report.getId());
        } catch (Exception e) {
            logger.error("❌ [AI批准] 发送通知失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响状态更新
        }
    }

    /**
     * AI拒绝流程
     */
    private void rejectByAI(WeeklyReport report, AIAnalysisResult result) {
        Double confidence = result.getConfidence() != null ? result.getConfidence() : 0.0;
        String summary = result.getResult() != null ? result.getResult() : "AI分析建议请参考详情";

        logger.info("🚫 [AI拒绝] 周报ID: {}, 置信度: {}, 原因: {}",
            report.getId(), confidence, summary);

        // 设置详细的拒绝原因
        String reason = String.format(
            "AI分析置信度过低: %.0f%% (阈值: %.0f%%)。建议: %s",
            confidence * 100,
            confidenceThreshold * 100,
            summary
        );

        // 使用实体的状态转换方法
        report.aiReject(reason);
    }

    /**
     * 获取当前置信度阈值（用于外部查询）
     */
    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    /**
     * 检查周报状态是否可以接受AI分析结果
     */
    public boolean canProcessAIResult(Long reportId) {
        WeeklyReport report = weeklyReportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return false;
        }
        return report.getStatus() == WeeklyReport.ReportStatus.AI_PROCESSING;
    }
}
