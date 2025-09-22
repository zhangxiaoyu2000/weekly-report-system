package com.weeklyreport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Weekly Report entity - 严格按照error3.md要求重构
 * 删除多余字段：workSummary, achievements, challenges, nextWeekPlan, priority等
 * 采用关联表模式替代JSON存储
 * 
 * 数据库设计参考：doc/数据库设计.md 第175-210行
 * 后端修改指导：doc/后端修改指导.md 第40-178行
 */
@Entity
@Table(name = "weekly_reports", indexes = {
    @Index(name = "idx_weekly_report_user", columnList = "user_id"),
    @Index(name = "idx_weekly_report_week", columnList = "report_week"),
    @Index(name = "idx_weekly_report_status", columnList = "approval_status")
})
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private Long userId;                    // # 提交周报的用户ID (error3.md要求)

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;                   // # 周报标题 (error3.md要求)

    @NotBlank(message = "Report week cannot be blank")
    @Size(max = 50, message = "Report week must not exceed 50 characters")
    @Column(name = "report_week", nullable = false, length = 50)
    private String reportWeek;              // 几月第几周（周几） (error3.md要求)

    @Size(max = 2000, message = "Additional notes must not exceed 2000 characters")
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;         // # 其他备注 (error3.md要求)

    @Size(max = 2000, message = "Development opportunities must not exceed 2000 characters")
    @Column(name = "development_opportunities", columnDefinition = "TEXT")
    private String developmentOpportunities; // # 可发展性清单 (error3.md要求)

    // 审批流程字段 (按error3.md要求)
    @Column(name = "ai_analysis_id")
    private Long aiAnalysisId;              // AI分析结果id（外键）

    @Column(name = "admin_reviewer_id")
    private Long adminReviewerId;           // 管理员审批人ID

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;         // 拒绝理由

    // AI分析结果通过aiAnalysisId关联到ai_analysis_results表

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20, nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.AI_ANALYZING; // 审批状态

    // 时间戳字段
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 关联表映射 - 替代JSON存储 (按数据库设计.md和后端修改指导.md要求)
    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TaskReport> taskReports = new ArrayList<>();     // 日常任务关联

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DevTaskReport> devTaskReports = new ArrayList<>(); // 发展任务关联

    // 审批状态枚举 (AI分析→管理员审核→完成)
    public enum ApprovalStatus {
        AI_ANALYZING,           // AI分析中
        AI_REJECTED,            // AI分析不通过
        ADMIN_REVIEWING,        // 管理员审核中 (AI通过后的状态)
        ADMIN_APPROVED,         // 管理员审核通过，最终状态
        ADMIN_REJECTED          // 管理员审核不通过
    }

    // 兼容性枚举 - ReportStatus
    public enum ReportStatus {
        DRAFT,              // 草稿
        SUBMITTED,          // 已提交
        IN_REVIEW,          // 审核中
        APPROVED,           // 已批准
        REJECTED            // 已拒绝
    }

    // 兼容性枚举 - ReportPriority (为了支持旧代码)
    public enum ReportPriority {
        LOW(1),             // 低优先级
        MEDIUM(2),          // 中优先级
        HIGH(3),            // 高优先级
        URGENT(4);          // 紧急

        private final int value;

        ReportPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ReportPriority fromValue(int value) {
            for (ReportPriority priority : values()) {
                if (priority.value == value) {
                    return priority;
                }
            }
            return MEDIUM; // 默认中优先级
        }
    }

    // Constructors
    public WeeklyReport() {}

    public WeeklyReport(Long userId, String title, String reportWeek) {
        this.userId = userId;
        this.title = title;
        this.reportWeek = reportWeek;
        this.approvalStatus = ApprovalStatus.AI_ANALYZING;
    }

    // Getters and Setters
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
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

    // AI分析结果通过关联表AIAnalysisResult获取

    // 核心业务方法 - 获取工作内容 (安全版本，不触发懒加载)
    public String getContent() {
        // 安全版本：只使用非懒加载字段
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

    // 核心业务方法 - 获取状态
    public ReportStatus getStatus() {
        switch (this.approvalStatus) {
            case AI_ANALYZING: return ReportStatus.IN_REVIEW;
            case ADMIN_REVIEWING: return ReportStatus.IN_REVIEW;
            case ADMIN_APPROVED: return ReportStatus.APPROVED;
            case AI_REJECTED: case ADMIN_REJECTED: return ReportStatus.REJECTED;
            default: return ReportStatus.IN_REVIEW;
        }
    }

    // 核心业务方法 - 获取摘要
    public String getSummary() {
        return this.title; // 用title作为摘要
    }

    // 兼容性方法 - Template关联 (简化版本中不支持)
    public void setTemplate(Object template) {
        // 简化版本中不支持Template关联，忽略此方法
    }

    // Business logic methods
    public void submit() {
        // 直接进入AI分析状态，因为不再有SUBMITTED状态
        this.approvalStatus = ApprovalStatus.AI_ANALYZING;
    }

    public void aiApprove() {
        // AI通过后，状态转换为管理员审核中
        this.approvalStatus = ApprovalStatus.ADMIN_REVIEWING;
    }

    public void adminApprove(Long adminId) {
        this.adminReviewerId = adminId;
        this.approvalStatus = ApprovalStatus.ADMIN_APPROVED;
    }


    public void reject(Long adminId, String reason) {
        this.adminReviewerId = adminId;
        this.rejectionReason = reason;
        this.approvalStatus = ApprovalStatus.ADMIN_REJECTED;
    }

    public boolean isDraft() {
        // 由于没有DRAFT状态，检查是否为初始状态（AI_ANALYZING）
        return approvalStatus == ApprovalStatus.AI_ANALYZING;
    }

    public boolean isSubmitted() {
        // 检查是否已经进入分析流程
        return approvalStatus != null;
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.ADMIN_APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus == ApprovalStatus.AI_REJECTED || approvalStatus == ApprovalStatus.ADMIN_REJECTED;
    }

    // Utility methods for managing relationships - 简化版本中暂时禁用以避免懒加载问题
    @JsonIgnore
    public void addTaskReport(TaskReport taskReport) {
        // 暂时禁用以避免懒加载问题
        if (taskReports != null) {
            taskReports.add(taskReport);
            taskReport.setWeeklyReport(this);
        }
    }

    @JsonIgnore
    public void removeTaskReport(TaskReport taskReport) {
        // 暂时禁用以避免懒加载问题
        if (taskReports != null) {
            taskReports.remove(taskReport);
            taskReport.setWeeklyReport(null);
        }
    }

    @JsonIgnore
    public void addDevTaskReport(DevTaskReport devTaskReport) {
        // 暂时禁用以避免懒加载问题
        if (devTaskReports != null) {
            devTaskReports.add(devTaskReport);
            devTaskReport.setWeeklyReport(this);
        }
    }

    @JsonIgnore
    public void removeDevTaskReport(DevTaskReport devTaskReport) {
        // 暂时禁用以避免懒加载问题
        if (devTaskReports != null) {
            devTaskReports.remove(devTaskReport);
            devTaskReport.setWeeklyReport(null);
        }
    }

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
                ", approvalStatus=" + approvalStatus +
                '}';
    }
}