package com.weeklyreport.weeklyreport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weeklyreport.task.entity.TaskReport;
import com.weeklyreport.task.entity.DevTaskReport;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 周报实体 - 5状态系统（简化版）
 *
 * 状态流程：
 * DRAFT → AI_PROCESSING → PENDING_REVIEW → APPROVED
 *              ↓               ↓
 *          REJECTED ← ← ← REJECTED
 */
@Entity
@Table(name = "weekly_reports", indexes = {
    @Index(name = "idx_weekly_report_user", columnList = "user_id"),
    @Index(name = "idx_weekly_report_week", columnList = "report_week"),
    @Index(name = "idx_weekly_report_status", columnList = "status")
})
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Title cannot be blank")
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @NotBlank(message = "Report week cannot be blank")
    @Size(max = 50, message = "Report week must not exceed 50 characters")
    @Column(name = "report_week", nullable = false, length = 50)
    private String reportWeek;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "development_opportunities", columnDefinition = "TEXT")
    private String developmentOpportunities;

    // ============= 单一状态字段 =============

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.DRAFT;

    // ============= 审核信息 =============

    @Column(name = "ai_analysis_id")
    private Long aiAnalysisId;

    @Column(name = "admin_reviewer_id")
    private Long adminReviewerId;

    // ============= 拒绝信息 =============

    @Enumerated(EnumType.STRING)
    @Column(name = "rejected_by", length = 20)
    private RejectedBy rejectedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    // ============= 时间戳 =============

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // ============= 关联关系 =============

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TaskReport> taskReports = new ArrayList<>();

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DevTaskReport> devTaskReports = new ArrayList<>();

    // ============= 状态枚举定义 =============

    /**
     * 周报状态枚举（4状态系统）
     * 状态流程：DRAFT → AI_PROCESSING → ADMIN_REVIEWING → APPROVED
     *                        ↓               ↓
     *                    REJECTED ← ← ← REJECTED
     */
    public enum ReportStatus {
        DRAFT("草稿"),
        AI_PROCESSING("AI分析中"),
        ADMIN_REVIEWING("管理员审核中"),
        APPROVED("已通过"),
        REJECTED("已拒绝");

        private final String displayName;

        ReportStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 拒绝者枚举
     */
    public enum RejectedBy {
        AI("AI系统"),
        ADMIN("管理员"),
        SUPER_ADMIN("超级管理员");

        private final String displayName;

        RejectedBy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ============= 构造函数 =============

    public WeeklyReport() {}

    public WeeklyReport(Long userId, String title, String reportWeek) {
        this.userId = userId;
        this.title = title;
        this.reportWeek = reportWeek;
        this.status = ReportStatus.DRAFT;
    }

    // ============= Getters and Setters =============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReportWeek() {
        return reportWeek;
    }

    public void setReportWeek(String reportWeek) {
        this.reportWeek = reportWeek;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getDevelopmentOpportunities() {
        return developmentOpportunities;
    }

    public void setDevelopmentOpportunities(String developmentOpportunities) {
        this.developmentOpportunities = developmentOpportunities;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Long getAiAnalysisId() {
        return aiAnalysisId;
    }

    public void setAiAnalysisId(Long aiAnalysisId) {
        this.aiAnalysisId = aiAnalysisId;
    }

    public Long getAdminReviewerId() {
        return adminReviewerId;
    }

    public void setAdminReviewerId(Long adminReviewerId) {
        this.adminReviewerId = adminReviewerId;
    }

    public RejectedBy getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(RejectedBy rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public List<TaskReport> getTaskReports() {
        return taskReports;
    }

    public void setTaskReports(List<TaskReport> taskReports) {
        this.taskReports = taskReports;
    }

    public List<DevTaskReport> getDevTaskReports() {
        return devTaskReports;
    }

    public void setDevTaskReports(List<DevTaskReport> devTaskReports) {
        this.devTaskReports = devTaskReports;
    }

    // ============= 业务方法 =============

    /**
     * 获取周报内容摘要
     */
    public String getContent() {
        StringBuilder content = new StringBuilder();
        if (title != null && !title.trim().isEmpty()) {
            content.append(title);
        }
        if (additionalNotes != null && !additionalNotes.trim().isEmpty()) {
            if (content.length() > 0) {
                content.append(" - ");
            }
            content.append(additionalNotes);
        }
        if (developmentOpportunities != null && !developmentOpportunities.trim().isEmpty()) {
            if (content.length() > 0) {
                content.append(" | ");
            }
            content.append("发展机会: ").append(developmentOpportunities);
        }
        return content.toString();
    }

    /**
     * 获取摘要
     */
    public String getSummary() {
        return this.title;
    }

    // ============= 状态判断方法 =============

    /**
     * 是否为草稿
     */
    public boolean isDraft() {
        return status == ReportStatus.DRAFT;
    }

    /**
     * 是否正在AI分析
     */
    public boolean isAIProcessing() {
        return status == ReportStatus.AI_PROCESSING;
    }

    /**
     * 是否待审核（管理员审核）
     */
    public boolean isPendingReview() {
        return status == ReportStatus.ADMIN_REVIEWING;
    }

    /**
     * 是否管理员审核中
     */
    public boolean isAdminReviewing() {
        return status == ReportStatus.ADMIN_REVIEWING;
    }

    /**
     * 是否已通过
     */
    public boolean isApproved() {
        return status == ReportStatus.APPROVED;
    }

    /**
     * 是否已拒绝
     */
    public boolean isRejected() {
        return status == ReportStatus.REJECTED;
    }

    /**
     * 是否可编辑（草稿或已拒绝）
     */
    public boolean isEditable() {
        return status == ReportStatus.DRAFT || status == ReportStatus.REJECTED;
    }

    // ============= 状态转换方法 =============

    /**
     * 保存草稿（仅DRAFT状态可用）
     */
    public void saveDraft() {
        if (status != ReportStatus.DRAFT) {
            throw new IllegalStateException(
                String.format("只能保存草稿状态的周报，当前状态: %s", status)
            );
        }
        // 状态保持 DRAFT
    }

    /**
     * 提交审核（从DRAFT或REJECTED提交）
     * 直接进入AI_PROCESSING状态
     */
    public void submit() {
        if (status != ReportStatus.DRAFT && status != ReportStatus.REJECTED) {
            throw new IllegalStateException(
                String.format("只能提交草稿或已拒绝的周报，当前状态: %s", status)
            );
        }
        this.status = ReportStatus.AI_PROCESSING;
        this.submittedAt = LocalDateTime.now();

        // 清除旧的拒绝信息（如果是重新提交）
        this.rejectedBy = null;
        this.rejectionReason = null;
        this.rejectedAt = null;
    }

    /**
     * AI分析通过 - 转到管理员审核
     */
    public void aiApprove() {
        if (status != ReportStatus.AI_PROCESSING) {
            throw new IllegalStateException(
                String.format("只能完成AI分析中的周报，当前状态: %s", status)
            );
        }
        this.status = ReportStatus.ADMIN_REVIEWING;
    }

    /**
     * AI分析拒绝
     */
    public void aiReject(String reason) {
        if (status != ReportStatus.AI_PROCESSING) {
            throw new IllegalStateException(
                String.format("只能拒绝AI分析中的周报，当前状态: %s", status)
            );
        }
        this.status = ReportStatus.REJECTED;
        this.rejectedBy = RejectedBy.AI;
        this.rejectionReason = reason;
        this.rejectedAt = LocalDateTime.now();
    }

    /**
     * 管理员审核通过 - 直接批准
     */
    public void adminApprove(Long reviewerId) {
        if (status != ReportStatus.ADMIN_REVIEWING) {
            throw new IllegalStateException(
                String.format("只能审核管理员审核中的周报，当前状态: %s", status)
            );
        }
        this.status = ReportStatus.APPROVED;
        this.adminReviewerId = reviewerId;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 兼容旧方法 - 管理员审批通过
     * @deprecated 请使用 adminApprove()
     */
    @Deprecated
    public void approve(Long reviewerId) {
        adminApprove(reviewerId);
    }

    /**
     * 管理员审核拒绝
     */
    public void reject(Long reviewerId, String reason, boolean isSuperAdmin) {
        if (status != ReportStatus.ADMIN_REVIEWING) {
            throw new IllegalStateException(
                String.format("只能拒绝待审核状态的周报，当前状态: %s", status)
            );
        }
        this.status = ReportStatus.REJECTED;
        this.rejectedBy = isSuperAdmin ? RejectedBy.SUPER_ADMIN : RejectedBy.ADMIN;
        this.rejectionReason = reason;
        this.rejectedAt = LocalDateTime.now();
        this.adminReviewerId = reviewerId;
    }

    /**
     * 更新内容（DRAFT或REJECTED状态下）
     * 状态保持不变
     */
    public void updateContent() {
        if (!isEditable()) {
            throw new IllegalStateException(
                String.format("只能编辑草稿或已拒绝状态的周报，当前状态: %s", status)
            );
        }
        // 状态保持不变：DRAFT → DRAFT, REJECTED → REJECTED
    }

    // ============= 关系管理方法 =============

    @JsonIgnore
    public void addTaskReport(TaskReport taskReport) {
        if (taskReports != null) {
            taskReports.add(taskReport);
            taskReport.setWeeklyReport(this);
        }
    }

    @JsonIgnore
    public void removeTaskReport(TaskReport taskReport) {
        if (taskReports != null) {
            taskReports.remove(taskReport);
            taskReport.setWeeklyReport(null);
        }
    }

    @JsonIgnore
    public void addDevTaskReport(DevTaskReport devTaskReport) {
        if (devTaskReports != null) {
            devTaskReports.add(devTaskReport);
            devTaskReport.setWeeklyReport(this);
        }
    }

    @JsonIgnore
    public void removeDevTaskReport(DevTaskReport devTaskReport) {
        if (devTaskReports != null) {
            devTaskReports.remove(devTaskReport);
            devTaskReport.setWeeklyReport(null);
        }
    }

    // ============= equals/hashCode/toString =============

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeeklyReport)) return false;
        WeeklyReport that = (WeeklyReport) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WeeklyReport{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", status=" + status +
                '}';
    }
}
