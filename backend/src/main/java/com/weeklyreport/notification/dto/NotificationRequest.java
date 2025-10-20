package com.weeklyreport.notification.dto;

import java.time.LocalDateTime;

/**
 * 通知请求DTO
 */
public class NotificationRequest {

    public enum NotificationType {
        AI_ANALYSIS_COMPLETED("AI分析完成"),
        PENDING_ADMIN_REVIEW("待管理员审核"),
        ADMIN_REJECTED("管理员拒绝"),
        ADMIN_APPROVED("管理员通过"),
        SUPER_ADMIN_REJECTED("超级管理员拒绝"),
        SUPER_ADMIN_APPROVED("超级管理员通过"),
        FORCE_SUBMITTED("强制提交"),
        
        // 周报相关通知类型
        WEEKLY_REPORT_SUBMITTED("周报提交成功"),
        WEEKLY_REPORT_AI_COMPLETED("周报AI分析完成"),
        WEEKLY_REPORT_AI_REJECTED("周报AI分析置信度不足"),
        WEEKLY_REPORT_PENDING_ADMIN_REVIEW("周报待管理员审核"),
        WEEKLY_REPORT_ADMIN_REJECTED("周报管理员拒绝"),
        WEEKLY_REPORT_ADMIN_APPROVED("周报管理员通过"),
        WEEKLY_REPORT_SUPERVISOR_FORCE_SUBMITTED("主管强制提交周报");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RecipientType {
        PROJECT_MANAGER("项目经理"),
        ALL_ADMINS("所有管理员"),
        ALL_SUPER_ADMINS("所有超级管理员"),
        SUPER_ADMINS_AND_MANAGER("超级管理员和项目经理"),
        ADMINS_AND_SUPER_ADMINS("管理员和超级管理员"),
        ALL_STAKEHOLDERS("所有相关人员"),
        
        // 周报相关接收者类型
        WEEKLY_REPORT_AUTHOR("周报提交者"),
        WEEKLY_REPORT_SUPERVISOR("周报提交者主管"),
        WEEKLY_REPORT_ADMINS("周报审核管理员");

        private final String description;

        RecipientType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private NotificationType notificationType;
    private RecipientType recipientType;
    private Long projectId;
    private String projectName;
    private Long triggerUserId;
    private String triggerUserName;
    private String rejectionReason;
    private LocalDateTime timestamp;
    private String projectOwnerName;
    
    // 周报相关字段
    private Long weeklyReportId;
    private String weeklyReportTitle;
    private String reportWeek;
    private Long reportAuthorId;
    private String reportAuthorName;
    private String reviewerName;

    // 私有构造函数，使用Builder模式
    private NotificationRequest() {}

    // Getters
    public NotificationType getNotificationType() { return notificationType; }
    public RecipientType getRecipientType() { return recipientType; }
    public Long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public Long getTriggerUserId() { return triggerUserId; }
    public String getTriggerUserName() { return triggerUserName; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getProjectOwnerName() { return projectOwnerName; }
    
    // 周报相关字段的Getters
    public Long getWeeklyReportId() { return weeklyReportId; }
    public String getWeeklyReportTitle() { return weeklyReportTitle; }
    public String getReportWeek() { return reportWeek; }
    public Long getReportAuthorId() { return reportAuthorId; }
    public String getReportAuthorName() { return reportAuthorName; }
    public String getReviewerName() { return reviewerName; }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificationRequest request = new NotificationRequest();

        public Builder notificationType(NotificationType notificationType) {
            request.notificationType = notificationType;
            return this;
        }

        public Builder recipientType(RecipientType recipientType) {
            request.recipientType = recipientType;
            return this;
        }

        public Builder projectId(Long projectId) {
            request.projectId = projectId;
            return this;
        }

        public Builder projectName(String projectName) {
            request.projectName = projectName;
            return this;
        }

        public Builder triggerUserId(Long triggerUserId) {
            request.triggerUserId = triggerUserId;
            return this;
        }

        public Builder triggerUserName(String triggerUserName) {
            request.triggerUserName = triggerUserName;
            return this;
        }

        public Builder rejectionReason(String rejectionReason) {
            request.rejectionReason = rejectionReason;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            request.timestamp = timestamp;
            return this;
        }

        public Builder projectOwnerName(String projectOwnerName) {
            request.projectOwnerName = projectOwnerName;
            return this;
        }

        // 周报相关字段的Builder方法
        public Builder weeklyReportId(Long weeklyReportId) {
            request.weeklyReportId = weeklyReportId;
            return this;
        }

        public Builder weeklyReportTitle(String weeklyReportTitle) {
            request.weeklyReportTitle = weeklyReportTitle;
            return this;
        }

        public Builder reportWeek(String reportWeek) {
            request.reportWeek = reportWeek;
            return this;
        }

        public Builder reportAuthorId(Long reportAuthorId) {
            request.reportAuthorId = reportAuthorId;
            return this;
        }

        public Builder reportAuthorName(String reportAuthorName) {
            request.reportAuthorName = reportAuthorName;
            return this;
        }

        public Builder reviewerName(String reviewerName) {
            request.reviewerName = reviewerName;
            return this;
        }

        public NotificationRequest build() {
            // 基本验证
            if (request.notificationType == null) {
                throw new IllegalArgumentException("通知类型不能为空");
            }
            if (request.recipientType == null) {
                throw new IllegalArgumentException("接收者类型不能为空");
            }

            // 项目ID验证：仅对项目相关通知类型要求必填
            boolean isProjectNotification = request.notificationType == NotificationType.AI_ANALYSIS_COMPLETED
                || request.notificationType == NotificationType.PENDING_ADMIN_REVIEW
                || request.notificationType == NotificationType.ADMIN_REJECTED
                || request.notificationType == NotificationType.ADMIN_APPROVED
                || request.notificationType == NotificationType.SUPER_ADMIN_REJECTED
                || request.notificationType == NotificationType.SUPER_ADMIN_APPROVED
                || request.notificationType == NotificationType.FORCE_SUBMITTED;

            if (isProjectNotification && request.projectId == null) {
                throw new IllegalArgumentException("项目相关通知的项目ID不能为空");
            }

            // 周报ID验证：周报相关通知类型要求必填
            boolean isWeeklyReportNotification = request.notificationType == NotificationType.WEEKLY_REPORT_SUBMITTED
                || request.notificationType == NotificationType.WEEKLY_REPORT_AI_COMPLETED
                || request.notificationType == NotificationType.WEEKLY_REPORT_PENDING_ADMIN_REVIEW
                || request.notificationType == NotificationType.WEEKLY_REPORT_ADMIN_REJECTED
                || request.notificationType == NotificationType.WEEKLY_REPORT_ADMIN_APPROVED;

            if (isWeeklyReportNotification && request.weeklyReportId == null) {
                throw new IllegalArgumentException("周报相关通知的周报ID不能为空");
            }

            if (request.timestamp == null) {
                request.timestamp = LocalDateTime.now();
            }

            return request;
        }
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "notificationType=" + notificationType +
                ", recipientType=" + recipientType +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", triggerUserId=" + triggerUserId +
                ", triggerUserName='" + triggerUserName + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", projectOwnerName='" + projectOwnerName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
