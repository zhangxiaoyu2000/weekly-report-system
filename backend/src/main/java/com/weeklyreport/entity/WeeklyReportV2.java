package com.weeklyreport.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结构化周报实体类 V2
 * 支持JSON格式的结构化内容存储
 */
@Entity
@Table(name = "weekly_reports_v2")
public class WeeklyReportV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "用户不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "周报标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "周报周次不能为空")
    @Size(max = 50, message = "周次长度不能超过50字符")
    @Column(name = "report_week", nullable = false)
    private String reportWeek; // 中文格式：几月第几周（周几）

    @NotNull(message = "结构化内容不能为空")
    @Column(name = "content", nullable = false, columnDefinition = "JSON")
    private String content; // JSON格式存储结构化内容

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes; // 其他备注

    @Column(name = "development_opportunities", columnDefinition = "TEXT")
    private String developmentOpportunities; // 可发展性清单

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;

    // AI分析相关字段
    @Column(name = "ai_analysis_passed")
    private Boolean aiAnalysisPassed;

    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    private String aiAnalysisResult;

    // 审批相关字段
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_reviewer_id")
    private User adminReviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_reviewer_id")
    private User superAdminReviewer;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // 时间字段
    @NotNull(message = "周开始日期不能为空")
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @NotNull(message = "周结束日期不能为空")
    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 枚举定义
    public enum ReportStatus {
        @JsonProperty("DRAFT")
        DRAFT("草稿"),
        @JsonProperty("SUBMITTED")
        SUBMITTED("已提交"),
        @JsonProperty("PENDING_AI")
        PENDING_AI("等待AI分析"),
        @JsonProperty("PENDING_ADMIN")
        PENDING_ADMIN("等待管理员审批"),
        @JsonProperty("PENDING_SUPER_ADMIN")
        PENDING_SUPER_ADMIN("等待超级管理员审批"),
        @JsonProperty("APPROVED")
        APPROVED("已通过"),
        @JsonProperty("REJECTED")
        REJECTED("已拒绝");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // JPA 生命周期回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 构造函数
    public WeeklyReportV2() {}

    public WeeklyReportV2(User user, String title, String reportWeek, String content,
                         LocalDate weekStart, LocalDate weekEnd) {
        this.user = user;
        this.title = title;
        this.reportWeek = reportWeek;
        this.content = content;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Boolean getAiAnalysisPassed() {
        return aiAnalysisPassed;
    }

    public void setAiAnalysisPassed(Boolean aiAnalysisPassed) {
        this.aiAnalysisPassed = aiAnalysisPassed;
    }

    public String getAiAnalysisResult() {
        return aiAnalysisResult;
    }

    public void setAiAnalysisResult(String aiAnalysisResult) {
        this.aiAnalysisResult = aiAnalysisResult;
    }

    public User getAdminReviewer() {
        return adminReviewer;
    }

    public void setAdminReviewer(User adminReviewer) {
        this.adminReviewer = adminReviewer;
    }

    public User getSuperAdminReviewer() {
        return superAdminReviewer;
    }

    public void setSuperAdminReviewer(User superAdminReviewer) {
        this.superAdminReviewer = superAdminReviewer;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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

    // 辅助方法
    public boolean isDraft() {
        return status == ReportStatus.DRAFT;
    }

    public boolean isSubmitted() {
        return status == ReportStatus.SUBMITTED || 
               status == ReportStatus.PENDING_AI ||
               status == ReportStatus.PENDING_ADMIN ||
               status == ReportStatus.PENDING_SUPER_ADMIN;
    }

    public boolean isApproved() {
        return status == ReportStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == ReportStatus.REJECTED;
    }

    public boolean canEdit() {
        return status == ReportStatus.DRAFT || status == ReportStatus.REJECTED;
    }

    public void submit() {
        if (isDraft()) {
            this.status = ReportStatus.SUBMITTED;
            this.submittedAt = LocalDateTime.now();
        }
    }

    public void approve(User reviewer) {
        this.status = ReportStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        if (reviewer.getRole() == User.Role.ADMIN) {
            this.adminReviewer = reviewer;
        } else if (reviewer.getRole() == User.Role.SUPER_ADMIN) {
            this.superAdminReviewer = reviewer;
        }
    }

    public void reject(User reviewer, String reason) {
        this.status = ReportStatus.REJECTED;
        this.rejectionReason = reason;
        this.reviewedAt = LocalDateTime.now();
        if (reviewer.getRole() == User.Role.ADMIN) {
            this.adminReviewer = reviewer;
        } else if (reviewer.getRole() == User.Role.SUPER_ADMIN) {
            this.superAdminReviewer = reviewer;
        }
    }

    @Override
    public String toString() {
        return "WeeklyReportV2{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", status=" + status +
                ", weekStart=" + weekStart +
                ", weekEnd=" + weekEnd +
                ", createdAt=" + createdAt +
                '}';
    }
}