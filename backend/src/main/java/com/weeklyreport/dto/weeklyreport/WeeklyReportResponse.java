package com.weeklyreport.dto.weeklyreport;

import com.weeklyreport.entity.WeeklyReport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Response DTO for WeeklyReport entity - 严格按照WeeklyReport.java设计
 */
public class WeeklyReportResponse {

    private Long id;
    private Long userId;                            // #提交周报的用户ID
    private String title;                           // #周报标题
    private String reportWeek;                      // 几月第几周（周几）
    private String additionalNotes;                 // #其他备注
    private String developmentOpportunities;        // #可发展性清单
    
    // 审批流程字段
    private Long aiAnalysisId;                      // AI分析结果id（外键）
    private Long adminReviewerId;                   // 管理员审批人ID
    private String rejectionReason;                 // 拒绝理由
    private WeeklyReport.ApprovalStatus approvalStatus; // 审批状态
    
    // 时间戳字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 兼容性字段
    private String content;                         // 关联表汇总内容
    private String summary;                         // 摘要
    private WeeklyReport.ReportStatus status;       // 兼容ReportStatus
    private LocalDateTime submittedAt;              // 提交时间
    private LocalDateTime reviewedAt;               // 审核时间
    private String reviewComment;                   // 审核意见
    private Integer priority;                       // 优先级
    
    // 日期相关兼容字段
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Integer year;
    private Integer weekNumber;
    
    // 用户信息
    private String authorName;
    private String authorEmail;
    
    // 审核人信息
    private String reviewerName;

    // Constructors
    public WeeklyReportResponse() {}

    public WeeklyReportResponse(WeeklyReport report) {
        this.id = report.getId();
        this.userId = report.getUserId();
        this.title = report.getTitle();
        this.reportWeek = report.getReportWeek();
        this.additionalNotes = report.getAdditionalNotes();
        this.developmentOpportunities = report.getDevelopmentOpportunities();
        
        // 审批流程字段
        this.aiAnalysisId = report.getAiAnalysisId();
        this.adminReviewerId = report.getAdminReviewerId();
        this.rejectionReason = report.getRejectionReason();
        this.approvalStatus = report.getApprovalStatus();
        
        // 时间戳字段
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
        
        // 兼容性字段
        this.content = report.getContent();
        this.summary = report.getSummary();
        this.status = report.getStatus();
        this.submittedAt = report.getApprovalStatus() != WeeklyReport.ApprovalStatus.AI_ANALYZING ? 
            report.getCreatedAt() : null;
        this.reviewedAt = report.isApproved() ? report.getUpdatedAt() : null;
        this.reviewComment = report.getRejectionReason();
        this.priority = 2; // 默认中等优先级
        
        // 日期兼容处理
        if (report.getReportWeek() != null) {
            LocalDate estimatedDate = report.getCreatedAt() != null ? 
                report.getCreatedAt().toLocalDate() : LocalDate.now();
            this.weekStart = estimatedDate.with(java.time.DayOfWeek.MONDAY);
            this.weekEnd = estimatedDate.with(java.time.DayOfWeek.SUNDAY);
            this.year = estimatedDate.getYear();
            this.weekNumber = estimatedDate.get(WeekFields.of(Locale.CHINA).weekOfYear());
        }
        
        // 用户信息
        this.authorName = "User-" + report.getUserId();
        this.authorEmail = "user" + report.getUserId() + "@example.com";
        
        // 审核人信息
        this.reviewerName = report.getAdminReviewerId() != null ? 
            "Admin-" + report.getAdminReviewerId() : null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReportWeek() {
        return reportWeek;
    }

    public void setReportWeek(String reportWeek) {
        this.reportWeek = reportWeek;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
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

    public WeeklyReport.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(WeeklyReport.ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public WeeklyReport.ReportStatus getStatus() {
        return status;
    }

    public void setStatus(WeeklyReport.ReportStatus status) {
        this.status = status;
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

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    // Helper methods
    public boolean isDraft() {
        return approvalStatus == WeeklyReport.ApprovalStatus.AI_ANALYZING;
    }

    public boolean isSubmitted() {
        return approvalStatus == WeeklyReport.ApprovalStatus.ADMIN_REVIEWING || 
               approvalStatus == WeeklyReport.ApprovalStatus.ADMIN_APPROVED;
    }

    public boolean isApproved() {
        return approvalStatus == WeeklyReport.ApprovalStatus.ADMIN_APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus == WeeklyReport.ApprovalStatus.AI_REJECTED ||
               approvalStatus == WeeklyReport.ApprovalStatus.ADMIN_REJECTED;
    }

    // AI Analysis fields getters and setters
    // AI分析相关字段已移除，通过AIAnalysisResult实体关联获取
}