package com.weeklyreport.notification.service;

import com.weeklyreport.common.util.EmailSenderUtil;
import com.weeklyreport.notification.dto.NotificationRequest;
import com.weeklyreport.notification.event.*;
import com.weeklyreport.project.entity.Project;
import com.weeklyreport.project.repository.ProjectRepository;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 通知服务 - 处理项目和周报审核流程中的邮件通知
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final EmailSenderUtil emailSenderUtil;
    private final NotificationRecipientService recipientService;
    private final EmailTemplateService templateService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public NotificationService(EmailSenderUtil emailSenderUtil,
                             NotificationRecipientService recipientService,
                             EmailTemplateService templateService,
                             UserRepository userRepository,
                             ProjectRepository projectRepository) {
        this.emailSenderUtil = emailSenderUtil;
        this.recipientService = recipientService;
        this.templateService = templateService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * 异步发送通知邮件
     */
    @Async
    public CompletableFuture<Void> sendNotificationAsync(NotificationRequest request) {
        try {
            sendNotification(request);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private String resolveUserDisplayName(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
            .filter(User::isActive)
            .map(User::getUsername)
            .orElse(null);
    }

    private Long resolveProjectOwnerId(Long projectId) {
        if (projectId == null) {
            return null;
        }
        return projectRepository.findById(projectId)
            .map(Project::getCreatedBy)
            .orElse(null);
    }

    private String resolveProjectOwnerName(Long projectId) {
        Long ownerId = resolveProjectOwnerId(projectId);
        if (ownerId == null) {
            return null;
        }
        return resolveUserDisplayName(ownerId);
    }

    /**
     * 发送通知邮件
     */
    public void sendNotification(NotificationRequest request) {
        try {
            logger.info("📧 开始发送通知: type={}, recipientType={}, projectId={}",
                request.getNotificationType(), request.getRecipientType(), request.getProjectId());

            // 获取邮件接收者列表 (支持周报ID)
            List<String> recipients = recipientService.getRecipients(
                request.getRecipientType(),
                request.getProjectId(),
                request.getWeeklyReportId());

            logger.info("📧 找到收件人: count={}, emails={}", recipients.size(), recipients);

            if (recipients.isEmpty()) {
                logger.warn("No recipients found for notification type: {} and project: {}",
                    request.getRecipientType(), request.getProjectId());
                return;
            }

            // 生成邮件模板
            logger.info("📧 开始生成邮件模板...");
            String subject = templateService.generateSubject(request);
            String htmlContent = templateService.generateHtmlContent(request);
            logger.info("📧 邮件模板生成完成: subject={}", subject);

            // 发送HTML邮件
            logger.info("📧 开始调用邮件发送工具...");
            emailSenderUtil.sendHtml(recipients, subject, htmlContent);
            logger.info("📧 邮件发送工具调用完成");

            logger.info("Successfully sent {} notification to {} recipients for project {}",
                request.getNotificationType(), recipients.size(), request.getProjectId());

        } catch (Exception e) {
            logger.error("Failed to send notification for project {}: {}",
                request.getProjectId(), e.getMessage(), e);
            throw e;
        }
    }

    // ========== 项目审核流程事件监听器 ==========

    /**
     * AI分析完成 → 通知项目经理
     */
    @EventListener
    @Async
    public void handleAIAnalysisCompleted(AIAnalysisCompletedEvent event) {
        logger.info("Handling AI analysis completed event for project {}", event.getProjectId());
        
        NotificationRequest request = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.AI_ANALYSIS_COMPLETED)
            .recipientType(NotificationRequest.RecipientType.PROJECT_MANAGER)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .triggerUserId(event.getTriggerUserId())
            .triggerUserName(resolveUserDisplayName(event.getTriggerUserId()))
            .projectOwnerName(resolveProjectOwnerName(event.getProjectId()))
            .timestamp(LocalDateTime.now())
            .build();

        sendNotificationAsync(request);
    }

    /**
     * 待管理员审核 → 通知所有管理员
     */
    @EventListener
    @Async
    public void handlePendingAdminReview(PendingAdminReviewEvent event) {
        logger.info("Handling pending admin review event for project {}", event.getProjectId());
        
        NotificationRequest request = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.PENDING_ADMIN_REVIEW)
            .recipientType(NotificationRequest.RecipientType.ALL_ADMINS)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .triggerUserId(event.getTriggerUserId())
            .triggerUserName(resolveUserDisplayName(event.getTriggerUserId()))
            .projectOwnerName(resolveProjectOwnerName(event.getProjectId()))
            .timestamp(LocalDateTime.now())
            .build();

        sendNotificationAsync(request);
    }

    /**
     * 管理员拒绝 → 通知项目经理
     */
    @EventListener
    @Async
    public void handleAdminRejected(AdminRejectedEvent event) {
        try {
            logger.info("=== 开始处理管理员拒绝事件 ===");
            logger.info("项目ID: {}, 项目名: {}, 触发用户ID: {}, 拒绝原因: {}",
                event.getProjectId(), event.getProjectName(), event.getTriggerUserId(), event.getRejectionReason());

            String reviewerName = resolveUserDisplayName(event.getTriggerUserId());
            String projectOwnerName = resolveProjectOwnerName(event.getProjectId());

            NotificationRequest request = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.ADMIN_REJECTED)
                .recipientType(NotificationRequest.RecipientType.PROJECT_MANAGER)
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .rejectionReason(event.getRejectionReason())
                .triggerUserId(event.getTriggerUserId())
                .triggerUserName(reviewerName)
                .projectOwnerName(projectOwnerName)
                .reviewerName(reviewerName)
                .timestamp(LocalDateTime.now())
                .build();

            logger.info("构建通知请求完成，开始异步发送邮件...");
            sendNotificationAsync(request).exceptionally(ex -> {
                logger.error("❌ 发送管理员拒绝邮件失败 - 项目ID: {}, 错误: {}",
                    event.getProjectId(), ex.getMessage(), ex);
                return null;
            });

            logger.info("=== 管理员拒绝事件处理完成 ===");
        } catch (Exception e) {
            logger.error("❌ 处理管理员拒绝事件异常 - 项目ID: {}, 错误: {}",
                event.getProjectId(), e.getMessage(), e);
        }
    }

    /**
     * 管理员通过 → 通知超级管理员和项目经理
     */
    @EventListener
    @Async
    public void handleAdminApproved(AdminApprovedEvent event) {
        try {
            logger.info("=== 开始处理管理员审批通过事件 ===");
            logger.info("项目ID: {}, 项目名: {}, 触发用户ID: {}",
                event.getProjectId(), event.getProjectName(), event.getTriggerUserId());

            String reviewerName = resolveUserDisplayName(event.getTriggerUserId());
            String projectOwnerName = resolveProjectOwnerName(event.getProjectId());
            LocalDateTime timestamp = LocalDateTime.now();

            NotificationRequest superAdminNotice = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.ADMIN_APPROVED)
                .recipientType(NotificationRequest.RecipientType.ALL_SUPER_ADMINS)
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .triggerUserId(event.getTriggerUserId())
                .triggerUserName(reviewerName)
                .projectOwnerName(projectOwnerName)
                .reviewerName(reviewerName)
                .timestamp(timestamp)
                .build();

            NotificationRequest managerNotice = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.ADMIN_APPROVED)
                .recipientType(NotificationRequest.RecipientType.PROJECT_MANAGER)
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .triggerUserId(event.getTriggerUserId())
                .triggerUserName(reviewerName)
                .projectOwnerName(projectOwnerName)
                .reviewerName(reviewerName)
                .timestamp(timestamp)
                .build();

            logger.info("构建通知请求完成，开始异步发送邮件（超级管理员、项目经理）...");
            sendNotificationAsync(superAdminNotice).exceptionally(ex -> {
                logger.error("❌ 发送管理员审批通过邮件失败（超级管理员） - 项目ID: {}, 错误: {}",
                    event.getProjectId(), ex.getMessage(), ex);
                return null;
            });

            sendNotificationAsync(managerNotice).exceptionally(ex -> {
                logger.error("❌ 发送管理员审批通过邮件失败（项目经理） - 项目ID: {}, 错误: {}",
                    event.getProjectId(), ex.getMessage(), ex);
                return null;
            });

            logger.info("=== 管理员审批通过事件处理完成 ===");
        } catch (Exception e) {
            logger.error("❌ 处理管理员审批通过事件异常 - 项目ID: {}, 错误: {}",
                event.getProjectId(), e.getMessage(), e);
        }
    }

    /**
     * 超级管理员拒绝 → 通知项目经理
     */
    @EventListener
    @Async
    public void handleSuperAdminRejected(SuperAdminRejectedEvent event) {
        try {
            logger.info("=== 开始处理超级管理员拒绝事件 ===");
            logger.info("项目ID: {}, 项目名: {}, 触发用户ID: {}, 拒绝原因: {}",
                event.getProjectId(), event.getProjectName(), event.getTriggerUserId(), event.getRejectionReason());

            String reviewerName = resolveUserDisplayName(event.getTriggerUserId());
            String projectOwnerName = resolveProjectOwnerName(event.getProjectId());

            NotificationRequest request = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.SUPER_ADMIN_REJECTED)
                .recipientType(NotificationRequest.RecipientType.PROJECT_MANAGER)
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .rejectionReason(event.getRejectionReason())
                .triggerUserId(event.getTriggerUserId())
                .triggerUserName(reviewerName)
                .projectOwnerName(projectOwnerName)
                .reviewerName(reviewerName)
                .timestamp(LocalDateTime.now())
                .build();

            logger.info("构建通知请求完成，开始异步发送邮件...");
            sendNotificationAsync(request).exceptionally(ex -> {
                logger.error("❌ 发送超级管理员拒绝邮件失败 - 项目ID: {}, 错误: {}",
                    event.getProjectId(), ex.getMessage(), ex);
                return null;
            });

            logger.info("=== 超级管理员拒绝事件处理完成 ===");
        } catch (Exception e) {
            logger.error("❌ 处理超级管理员拒绝事件异常 - 项目ID: {}, 错误: {}",
                event.getProjectId(), e.getMessage(), e);
        }
    }

    /**
     * 超级管理员通过 → 通知所有相关人员
     */
    @EventListener
    @Async
    public void handleSuperAdminApproved(SuperAdminApprovedEvent event) {
        try {
            logger.info("=== 开始处理超级管理员审批通过事件 ===");
            logger.info("项目ID: {}, 项目名: {}, 触发用户ID: {}",
                event.getProjectId(), event.getProjectName(), event.getTriggerUserId());

            LocalDateTime timestamp = LocalDateTime.now();
            String reviewerName = resolveUserDisplayName(event.getTriggerUserId());
            String projectOwnerName = resolveProjectOwnerName(event.getProjectId());

            NotificationRequest managerNotice = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.SUPER_ADMIN_APPROVED)
                .recipientType(NotificationRequest.RecipientType.PROJECT_MANAGER)
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .triggerUserId(event.getTriggerUserId())
                .triggerUserName(reviewerName)
                .projectOwnerName(projectOwnerName)
                .reviewerName(reviewerName)
                .timestamp(timestamp)
                .build();

            NotificationRequest adminNotice = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.SUPER_ADMIN_APPROVED)
                .recipientType(NotificationRequest.RecipientType.ADMINS_AND_SUPER_ADMINS)
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .triggerUserId(event.getTriggerUserId())
                .triggerUserName(reviewerName)
                .projectOwnerName(projectOwnerName)
                .reviewerName(reviewerName)
                .timestamp(timestamp)
                .build();

            logger.info("构建通知请求完成，开始异步发送邮件（项目经理+管理员）...");
            sendNotificationAsync(managerNotice).exceptionally(ex -> {
                logger.error("❌ 发送超级管理员审批通过邮件失败（项目经理） - 项目ID: {}, 错误: {}",
                    event.getProjectId(), ex.getMessage(), ex);
                return null;
            });

            sendNotificationAsync(adminNotice).exceptionally(ex -> {
                logger.error("❌ 发送超级管理员审批通过邮件失败（管理员） - 项目ID: {}, 错误: {}",
                    event.getProjectId(), ex.getMessage(), ex);
                return null;
            });

            logger.info("=== 超级管理员审批通过事件处理完成 ===");
        } catch (Exception e) {
            logger.error("❌ 处理超级管理员审批通过事件异常 - 项目ID: {}, 错误: {}",
                event.getProjectId(), e.getMessage(), e);
        }
    }

    /**
     * 强制提交 → 通知所有管理员
     */
    @EventListener
    @Async
    public void handleForceSubmitted(ForceSubmittedEvent event) {
        logger.info("Handling force submitted event for project {}", event.getProjectId());
        
        NotificationRequest request = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.FORCE_SUBMITTED)
            .recipientType(NotificationRequest.RecipientType.ALL_ADMINS)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .triggerUserId(event.getTriggerUserId())
            .triggerUserName(resolveUserDisplayName(event.getTriggerUserId()))
            .projectOwnerName(resolveProjectOwnerName(event.getProjectId()))
            .timestamp(LocalDateTime.now())
            .build();

        sendNotificationAsync(request);
    }

    // ========== 周报审核流程事件监听器 ==========

    /**
     * 周报提交 → 通知周报提交者和主管
     */
    @EventListener
    @Async
    public void handleWeeklyReportSubmitted(WeeklyReportSubmittedEvent event) {
        logger.info("📧 处理周报提交事件，周报ID: {}", event.getWeeklyReportId());

        try {
            LocalDateTime timestamp = LocalDateTime.now();

            // 通知提交者（周报已成功提交）
            NotificationRequest authorNotice = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_SUBMITTED)
                .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_AUTHOR)
                .weeklyReportId(event.getWeeklyReportId())
                .weeklyReportTitle(event.getWeeklyReportTitle())
                .reportWeek(event.getReportWeek())
                .reportAuthorId(event.getReportAuthorId())
                .reportAuthorName(event.getReportAuthorName())
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .timestamp(timestamp)
                .build();

            // 通知主管（有新周报提交）
            NotificationRequest supervisorNotice = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_SUBMITTED)
                .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_SUPERVISOR)
                .weeklyReportId(event.getWeeklyReportId())
                .weeklyReportTitle(event.getWeeklyReportTitle())
                .reportWeek(event.getReportWeek())
                .reportAuthorId(event.getReportAuthorId())
                .reportAuthorName(event.getReportAuthorName())
                .projectId(event.getProjectId())
                .projectName(event.getProjectName())
                .timestamp(timestamp)
                .build();

            logger.info("📧 开始发送周报提交邮件（提交者+主管）...");

            sendNotificationAsync(authorNotice).exceptionally(ex -> {
                logger.error("❌ 发送周报提交邮件失败（提交者） - 周报ID: {}, 错误: {}",
                    event.getWeeklyReportId(), ex.getMessage(), ex);
                return null;
            });

            sendNotificationAsync(supervisorNotice).exceptionally(ex -> {
                logger.error("❌ 发送周报提交邮件失败（主管） - 周报ID: {}, 错误: {}",
                    event.getWeeklyReportId(), ex.getMessage(), ex);
                return null;
            });

            logger.info("✅ 周报提交事件处理完成，周报ID: {}", event.getWeeklyReportId());
        } catch (Exception e) {
            logger.error("❌ 处理周报提交事件异常 - 周报ID: {}, 错误: {}",
                event.getWeeklyReportId(), e.getMessage(), e);
        }
    }

    /**
     * 周报AI分析拒绝 → 通知提交者
     */
    @EventListener
    @Async
    public void handleWeeklyReportAIRejected(WeeklyReportAIRejectedEvent event) {
        logger.info("❌ 处理周报AI拒绝事件，周报ID: {}", event.getWeeklyReportId());

        try {
            NotificationRequest request = NotificationRequest.builder()
                .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_AI_REJECTED)
                .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_AUTHOR)
                .weeklyReportId(event.getWeeklyReportId())
                .weeklyReportTitle(event.getWeeklyReportTitle())
                .reportWeek(event.getReportWeek())
                .reportAuthorId(event.getReportAuthorId())
                .reportAuthorName(event.getReportAuthorName())
                .rejectionReason(event.getRejectionReason())
                .timestamp(LocalDateTime.now())
                .build();

            sendNotificationAsync(request).exceptionally(ex -> {
                logger.error("❌ 发送周报AI拒绝邮件失败 - 周报ID: {}, 错误: {}",
                    event.getWeeklyReportId(), ex.getMessage(), ex);
                return null;
            });

            logger.info("✅ 周报AI拒绝事件处理完成，周报ID: {}", event.getWeeklyReportId());
        } catch (Exception e) {
            logger.error("❌ 处理周报AI拒绝事件异常 - 周报ID: {}, 错误: {}",
                event.getWeeklyReportId(), e.getMessage(), e);
        }
    }

    /**
     * 周报AI分析完成 → 通知主管
     */
    @EventListener
    @Async
    public void handleWeeklyReportAICompleted(WeeklyReportAICompletedEvent event) {
        logger.info("📊 处理周报AI分析完成事件，周报ID: {}", event.getWeeklyReportId());
        
        NotificationRequest request = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_AI_COMPLETED)
            .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_SUPERVISOR)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .weeklyReportId(event.getWeeklyReportId())
            .weeklyReportTitle(event.getWeeklyReportTitle())
            .reportWeek(event.getReportWeek())
            .reportAuthorId(event.getReportAuthorId())
            .reportAuthorName(event.getReportAuthorName())
            .triggerUserId(event.getReportAuthorId())
            .timestamp(LocalDateTime.now())
            .build();

        sendNotificationAsync(request);
    }

    /**
     * 周报待管理员审核 → 通知所有管理员
     */
    @EventListener
    @Async
    public void handleWeeklyReportPendingAdminReview(WeeklyReportPendingAdminReviewEvent event) {
        logger.info("📋 处理周报待管理员审核事件，周报ID: {}", event.getWeeklyReportId());
        
        NotificationRequest request = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_PENDING_ADMIN_REVIEW)
            .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_ADMINS)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .weeklyReportId(event.getWeeklyReportId())
            .weeklyReportTitle(event.getWeeklyReportTitle())
            .reportWeek(event.getReportWeek())
            .reportAuthorId(event.getReportAuthorId())
            .reportAuthorName(event.getReportAuthorName())
            .triggerUserId(event.getReportAuthorId())
            .timestamp(LocalDateTime.now())
            .build();

        sendNotificationAsync(request);
    }

    /**
     * 周报管理员拒绝 → 通知周报提交者主管
     */
    @EventListener
    @Async
    public void handleWeeklyReportAdminRejected(WeeklyReportAdminRejectedEvent event) {
        logger.info("❌ 处理周报管理员拒绝事件，周报ID: {}", event.getWeeklyReportId());
        
        NotificationRequest request = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_ADMIN_REJECTED)
            .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_SUPERVISOR)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .weeklyReportId(event.getWeeklyReportId())
            .weeklyReportTitle(event.getWeeklyReportTitle())
            .reportWeek(event.getReportWeek())
            .reportAuthorId(event.getReportAuthorId())
            .reportAuthorName(event.getReportAuthorName())
            .rejectionReason(event.getRejectionReason())
            .reviewerName(event.getReviewerName())
            .triggerUserId(event.getReviewerId())
            .timestamp(LocalDateTime.now())
            .build();

        sendNotificationAsync(request);
    }

    /**
     * 周报管理员通过 → 通知超级管理员和周报提交者
     */
    @EventListener
    @Async
    public void handleWeeklyReportAdminApproved(WeeklyReportAdminApprovedEvent event) {
        logger.info("✅ 处理周报管理员通过事件，周报ID: {}", event.getWeeklyReportId());
        LocalDateTime timestamp = LocalDateTime.now();

        // 通知超级管理员
        NotificationRequest superAdminNotice = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_ADMIN_APPROVED)
            .recipientType(NotificationRequest.RecipientType.ALL_SUPER_ADMINS)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .weeklyReportId(event.getWeeklyReportId())
            .weeklyReportTitle(event.getWeeklyReportTitle())
            .reportWeek(event.getReportWeek())
            .reportAuthorId(event.getReportAuthorId())
            .reportAuthorName(event.getReportAuthorName())
            .reviewerName(event.getReviewerName())
            .triggerUserId(event.getReviewerId())
            .timestamp(timestamp)
            .build();

        // 通知周报提交者
        NotificationRequest authorNotice = NotificationRequest.builder()
            .notificationType(NotificationRequest.NotificationType.WEEKLY_REPORT_ADMIN_APPROVED)
            .recipientType(NotificationRequest.RecipientType.WEEKLY_REPORT_AUTHOR)
            .projectId(event.getProjectId())
            .projectName(event.getProjectName())
            .weeklyReportId(event.getWeeklyReportId())
            .weeklyReportTitle(event.getWeeklyReportTitle())
            .reportWeek(event.getReportWeek())
            .reportAuthorId(event.getReportAuthorId())
            .reportAuthorName(event.getReportAuthorName())
            .reviewerName(event.getReviewerName())
            .triggerUserId(event.getReviewerId())
            .timestamp(timestamp)
            .build();

        sendNotificationAsync(superAdminNotice);
        sendNotificationAsync(authorNotice);
    }
}
