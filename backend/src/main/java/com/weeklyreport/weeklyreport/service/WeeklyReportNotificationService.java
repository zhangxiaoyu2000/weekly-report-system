package com.weeklyreport.weeklyreport.service;

import com.weeklyreport.notification.event.*;
import com.weeklyreport.ai.entity.AIAnalysisResult;
import com.weeklyreport.ai.repository.AIAnalysisResultRepository;
import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 周报通知服务
 * 负责在周报状态变更时发送通知事件
 */
@Service
@Transactional
public class WeeklyReportNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportNotificationService.class);

    private final WeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AIAnalysisResultRepository aiAnalysisResultRepository;

    @Value("${weekly-report.ai.confidence-threshold:0.7}")
    private double weeklyReportConfidenceThreshold;

    public WeeklyReportNotificationService(WeeklyReportRepository weeklyReportRepository,
                                          UserRepository userRepository,
                                          ApplicationEventPublisher eventPublisher,
                                          AIAnalysisResultRepository aiAnalysisResultRepository) {
        this.weeklyReportRepository = weeklyReportRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.aiAnalysisResultRepository = aiAnalysisResultRepository;
    }

    /**
     * 处理AI分析完成，更新状态并发送通知
     */
    public void handleAIAnalysisCompleted(Long weeklyReportId) {
        logger.info("📧 处理周报AI分析完成通知，周报ID: {}", weeklyReportId);
        
        try {
            WeeklyReport report = weeklyReportRepository.findById(weeklyReportId).orElse(null);
            if (report == null) {
                logger.error("❌ 周报不存在，ID: {}", weeklyReportId);
                return;
            }

            AIAnalysisResult analysisResult = fetchLatestAnalysisResult(report);
            if (analysisResult == null) {
                logger.warn("⚠️ 未找到周报{}的AI分析结果，保持当前状态: {}", weeklyReportId, report.getStatus());
                return;
            }

            double confidence = analysisResult.getConfidence() != null ? analysisResult.getConfidence() : 0.0;
            boolean completed = analysisResult.getStatus() == AIAnalysisResult.AnalysisStatus.COMPLETED;

            // 统一格式的调试日志
            String logPattern = "🔍[状态检查] 周报ID={}, 当前状态={}, 置信度={}, 阈值={}, 决策={}, 触发点={}";
            String decision = (completed && confidence >= weeklyReportConfidenceThreshold) ? "APPROVE" : "REJECT";
            logger.info(logPattern, weeklyReportId, report.getStatus(),
                confidence, weeklyReportConfidenceThreshold, decision,
                "WeeklyReportNotificationService.handleAIAnalysisCompleted");

            if (!completed || confidence < weeklyReportConfidenceThreshold) {
                ensureRejectedStatus(report, analysisResult, confidence);
                return;
            }

            if (!report.isPendingReview()) {
                report.aiApprove();
                weeklyReportRepository.save(report);
                logger.info("✅ 周报状态已更新为PENDING_REVIEW，ID: {}, 置信度: {}", weeklyReportId, confidence);
            }

            // 获取用户信息
            User author = userRepository.findById(report.getUserId()).orElse(null);
            String authorName = author != null ? author.getUsername() : "用户" + report.getUserId();

            // 发送AI分析完成事件（通知主管）
            WeeklyReportAICompletedEvent aiCompletedEvent = new WeeklyReportAICompletedEvent(
                this,
                report.getId(),
                report.getTitle(),
                report.getReportWeek(),
                report.getUserId(),
                authorName,
                null, // 项目ID - 待实现项目关联
                "默认项目" // 项目名称 - 待实现项目关联
            );
            eventPublisher.publishEvent(aiCompletedEvent);
            logger.info("📧 AI分析完成事件已发送，周报ID: {}", weeklyReportId);

            // 发送待管理员审核事件（通知所有管理员）
            WeeklyReportPendingAdminReviewEvent pendingReviewEvent = new WeeklyReportPendingAdminReviewEvent(
                this,
                report.getId(),
                report.getTitle(),
                report.getReportWeek(),
                report.getUserId(),
                authorName,
                null, // 项目ID
                "默认项目" // 项目名称
            );
            eventPublisher.publishEvent(pendingReviewEvent);
            logger.info("📧 待管理员审核事件已发送，周报ID: {}", weeklyReportId);

        } catch (Exception e) {
            logger.error("❌ 处理AI分析完成通知失败，周报ID: {}", weeklyReportId, e);
        }
    }

    private AIAnalysisResult fetchLatestAnalysisResult(WeeklyReport report) {
        Long analysisId = report.getAiAnalysisId();
        if (analysisId == null) {
            return null;
        }
        return aiAnalysisResultRepository.findById(analysisId).orElse(null);
    }

    private void ensureRejectedStatus(WeeklyReport report, AIAnalysisResult analysisResult, double confidence) {
        // 设置详细的拒绝原因
        String suggestion = analysisResult.getResult() != null ? analysisResult.getResult() : "请完善汇报内容后重新提交";
        String rejectionReason = String.format(
            "AI分析置信度过低(%.0f%%)，低于阈值(%.0f%%)。建议: %s",
            confidence * 100,
            weeklyReportConfidenceThreshold * 100,
            suggestion
        );

        // 检查是否需要更新状态（避免重复更新）
        boolean needsStatusUpdate = !report.isRejected() ||
            report.getRejectionReason() == null ||
            report.getRejectionReason().isBlank();

        if (needsStatusUpdate) {
            // 使用实体的aiReject方法
            report.aiReject(rejectionReason);
            weeklyReportRepository.save(report);
            logger.info("🚫 周报ID {} AI分析置信度不足({})，已拒绝", report.getId(), confidence);
        } else {
            logger.info("ℹ️ 周报ID {} 已处于拒绝状态，置信度: {}", report.getId(), confidence);
        }

        // 总是发送AI拒绝通知给提交者（即使已拒绝过）
        try {
            User author = userRepository.findById(report.getUserId()).orElse(null);
            String authorName = author != null ? author.getUsername() : "用户" + report.getUserId();

            WeeklyReportAIRejectedEvent rejectedEvent = new WeeklyReportAIRejectedEvent(
                this,
                report.getId(),
                report.getTitle(),
                report.getReportWeek(),
                report.getUserId(),
                authorName,
                rejectionReason
            );
            eventPublisher.publishEvent(rejectedEvent);
            logger.info("📧 AI拒绝通知事件已发送，周报ID: {}", report.getId());
        } catch (Exception e) {
            logger.error("❌ 发送AI拒绝通知失败，周报ID: {}", report.getId(), e);
        }
    }

    /**
     * 处理管理员拒绝周报
     */
    public void handleAdminRejected(Long weeklyReportId, String rejectionReason, Long reviewerId) {
        logger.info("📧 处理周报管理员拒绝通知，周报ID: {}", weeklyReportId);
        
        try {
            WeeklyReport report = weeklyReportRepository.findById(weeklyReportId).orElse(null);
            if (report == null) {
                logger.error("❌ 周报不存在，ID: {}", weeklyReportId);
                return;
            }

            // 获取用户信息
            User author = userRepository.findById(report.getUserId()).orElse(null);
            String authorName = author != null ? author.getUsername() : "用户" + report.getUserId();
            
            User reviewer = userRepository.findById(reviewerId).orElse(null);
            String reviewerName = reviewer != null ? reviewer.getUsername() : "管理员" + reviewerId;

            // 发送管理员拒绝事件
            WeeklyReportAdminRejectedEvent rejectedEvent = new WeeklyReportAdminRejectedEvent(
                this,
                report.getId(),
                report.getTitle(),
                report.getReportWeek(),
                report.getUserId(),
                authorName,
                null, // 项目ID
                "默认项目", // 项目名称
                rejectionReason,
                reviewerId,
                reviewerName
            );
            eventPublisher.publishEvent(rejectedEvent);
            logger.info("📧 管理员拒绝事件已发送，周报ID: {}", weeklyReportId);

        } catch (Exception e) {
            logger.error("❌ 处理管理员拒绝通知失败，周报ID: {}", weeklyReportId, e);
        }
    }

    /**
     * 处理管理员通过周报
     */
    public void handleAdminApproved(Long weeklyReportId, Long reviewerId) {
        logger.info("📧 处理周报管理员通过通知，周报ID: {}", weeklyReportId);
        
        try {
            WeeklyReport report = weeklyReportRepository.findById(weeklyReportId).orElse(null);
            if (report == null) {
                logger.error("❌ 周报不存在，ID: {}", weeklyReportId);
                return;
            }

            // 获取用户信息
            User author = userRepository.findById(report.getUserId()).orElse(null);
            String authorName = author != null ? author.getUsername() : "用户" + report.getUserId();
            
            User reviewer = userRepository.findById(reviewerId).orElse(null);
            String reviewerName = reviewer != null ? reviewer.getUsername() : "管理员" + reviewerId;

            // 发送管理员通过事件
            WeeklyReportAdminApprovedEvent approvedEvent = new WeeklyReportAdminApprovedEvent(
                this,
                report.getId(),
                report.getTitle(),
                report.getReportWeek(),
                report.getUserId(),
                authorName,
                null, // 项目ID
                "默认项目", // 项目名称
                reviewerId,
                reviewerName
            );
            eventPublisher.publishEvent(approvedEvent);
            logger.info("📧 管理员通过事件已发送，周报ID: {}", weeklyReportId);

        } catch (Exception e) {
            logger.error("❌ 处理管理员通过通知失败，周报ID: {}", weeklyReportId, e);
        }
    }

    /**
     * 处理周报提交（发送通知给作者确认提交成功）
     */
    public void handleWeeklyReportSubmitted(Long weeklyReportId, Long userId) {
        logger.info("📧 处理周报提交通知，周报ID: {}, 用户ID: {}", weeklyReportId, userId);

        try {
            WeeklyReport report = weeklyReportRepository.findById(weeklyReportId).orElse(null);
            if (report == null) {
                logger.error("❌ 周报不存在，ID: {}", weeklyReportId);
                return;
            }

            // 获取用户信息
            User author = userRepository.findById(userId).orElse(null);
            String authorName = author != null ? author.getUsername() : "用户" + userId;

            // 发送周报提交事件（通知作者和主管）
            WeeklyReportSubmittedEvent submittedEvent = new WeeklyReportSubmittedEvent(
                this,
                report.getId(),
                report.getTitle(),
                report.getReportWeek(),
                userId,
                authorName,
                null, // 项目ID - 待实现项目关联
                "默认项目" // 项目名称 - 待实现项目关联
            );
            eventPublisher.publishEvent(submittedEvent);
            logger.info("📧 周报提交通知事件已发送，周报ID: {}", weeklyReportId);

        } catch (Exception e) {
            logger.error("❌ 处理周报提交通知失败，周报ID: {}", weeklyReportId, e);
        }
    }

    /**
     * 获取用户显示名称
     */
    private String getUserDisplayName(Long userId) {
        if (userId == null) {
            return "未知用户";
        }

        return userRepository.findById(userId)
            .map(user -> user.getUsername())
            .orElse("用户" + userId);
    }
}
