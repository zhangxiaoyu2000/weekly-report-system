package com.weeklyreport.dto.weeklyreport;

import com.weeklyreport.entity.WeeklyReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for weekly report responses
 */
public class WeeklyReportResponse {

    private Long id;
    private String title;
    private LocalDate reportWeek;
    private Integer year;
    private Integer weekNumber;
    private String content;
    private String workSummary;
    private String achievements;
    private String challenges;
    private String nextWeekPlan;
    private String additionalNotes;
    private WeeklyReport.ReportStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewComment;
    private Boolean isLate;
    private Integer wordCount;
    private Integer priority;
    private Integer completionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Author information
    private Long authorId;
    private String authorName;
    private String authorEmail;

    // Template information
    private Long templateId;
    private String templateName;

    // Reviewer information
    private Long reviewerId;
    private String reviewerName;

    // Project information
    private Long projectId;
    private String projectName;

    // Statistics
    private Integer commentCount;
    private Integer analysisCount;

    // Constructors
    public WeeklyReportResponse() {}

    public WeeklyReportResponse(WeeklyReport report) {
        this.id = report.getId();
        this.title = report.getTitle();
        this.reportWeek = report.getReportWeek();
        this.year = report.getYear();
        this.weekNumber = report.getWeekNumber();
        this.content = report.getContent();
        this.workSummary = report.getWorkSummary();
        this.achievements = report.getAchievements();
        this.challenges = report.getChallenges();
        this.nextWeekPlan = report.getNextWeekPlan();
        this.additionalNotes = report.getAdditionalNotes();
        this.status = report.getStatus();
        this.submittedAt = report.getSubmittedAt();
        this.reviewedAt = report.getReviewedAt();
        this.reviewComment = report.getReviewComment();
        this.isLate = report.getIsLate();
        this.wordCount = report.getWordCount();
        this.priority = report.getPriority();
        this.completionPercentage = report.getCompletionPercentage();
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();

        // Author information
        if (report.getAuthor() != null) {
            this.authorId = report.getAuthor().getId();
            this.authorName = report.getAuthor().getFullName();
            this.authorEmail = report.getAuthor().getEmail();
        }

        // Template information
        if (report.getTemplate() != null) {
            this.templateId = report.getTemplate().getId();
            this.templateName = report.getTemplate().getName();
        }

        // Reviewer information
        if (report.getReviewer() != null) {
            this.reviewerId = report.getReviewer().getId();
            this.reviewerName = report.getReviewer().getFullName();
        }

        // Project information
        if (report.getProject() != null) {
            this.projectId = report.getProject().getId();
            this.projectName = report.getProject().getName();
        }

        // Statistics
        this.commentCount = report.getComments() != null ? report.getComments().size() : 0;
        this.analysisCount = report.getAnalysisResults() != null ? report.getAnalysisResults().size() : 0;
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

    public LocalDate getReportWeek() {
        return reportWeek;
    }

    public void setReportWeek(LocalDate reportWeek) {
        this.reportWeek = reportWeek;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWorkSummary() {
        return workSummary;
    }

    public void setWorkSummary(String workSummary) {
        this.workSummary = workSummary;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getChallenges() {
        return challenges;
    }

    public void setChallenges(String challenges) {
        this.challenges = challenges;
    }

    public String getNextWeekPlan() {
        return nextWeekPlan;
    }

    public void setNextWeekPlan(String nextWeekPlan) {
        this.nextWeekPlan = nextWeekPlan;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
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

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getAnalysisCount() {
        return analysisCount;
    }

    public void setAnalysisCount(Integer analysisCount) {
        this.analysisCount = analysisCount;
    }

    // Helper methods
    public boolean isOverdue() {
        return isLate != null && isLate;
    }

    public boolean isDraft() {
        return status == WeeklyReport.ReportStatus.DRAFT;
    }

    public boolean isSubmitted() {
        return status == WeeklyReport.ReportStatus.SUBMITTED ||
               status == WeeklyReport.ReportStatus.UNDER_REVIEW;
    }

    public boolean isApproved() {
        return status == WeeklyReport.ReportStatus.APPROVED;
    }

    public boolean needsAttention() {
        return status == WeeklyReport.ReportStatus.REVISION_REQUESTED ||
               status == WeeklyReport.ReportStatus.REJECTED;
    }

    @Override
    public String toString() {
        return "WeeklyReportResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", reportWeek=" + reportWeek +
                ", status=" + status +
                ", authorName='" + authorName + '\'' +
                '}';
    }
}