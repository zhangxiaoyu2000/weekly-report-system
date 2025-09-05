package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * WeeklyReport entity representing weekly reports submitted by users
 */
@Entity
@Table(name = "weekly_reports", indexes = {
    @Index(name = "idx_report_author", columnList = "author_id"),
    @Index(name = "idx_report_week", columnList = "report_week"),
    @Index(name = "idx_report_status", columnList = "status"),
    @Index(name = "idx_report_template", columnList = "template_id")
})
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull(message = "Report week cannot be null")
    @Column(name = "report_week", nullable = false)
    private LocalDate reportWeek; // 周报所属周的开始日期（周一）

    @NotNull(message = "Year cannot be null")
    @Min(value = 2020, message = "Year must be at least 2020")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull(message = "Week number cannot be null")
    @Min(value = 1, message = "Week number must be between 1 and 53")
    @Max(value = 53, message = "Week number must be between 1 and 53")
    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "work_summary", columnDefinition = "TEXT")
    private String workSummary; // 本周工作总结

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements; // 本周主要成果

    @Column(name = "challenges", columnDefinition = "TEXT")
    private String challenges; // 遇到的挑战和问题

    @Column(name = "next_week_plan", columnDefinition = "TEXT")
    private String nextWeekPlan; // 下周工作计划

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes; // 其他备注

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Size(max = 1000, message = "Review comment must not exceed 1000 characters")
    @Column(name = "review_comment", length = 1000)
    private String reviewComment; // 审核意见

    @Column(name = "is_late")
    private Boolean isLate = false; // 是否迟交

    @Column(name = "word_count")
    private Integer wordCount = 0; // 字数统计

    @Min(value = 0, message = "Priority must be non-negative")
    @Max(value = 10, message = "Priority must not exceed 10")
    @Column(name = "priority")
    private Integer priority = 5; // 优先级，1-10

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-One relationship with User (Author)
    @NotNull(message = "Author cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Many-to-One relationship with Template
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    // Many-to-One relationship with User (Reviewer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // One-to-Many relationship with Comment
    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private Set<Comment> comments = new HashSet<>();

    // One-to-Many relationship with AIAnalysisResult
    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private Set<AIAnalysisResult> analysisResults = new HashSet<>();

    // Report status enum
    public enum ReportStatus {
        DRAFT,          // 草稿状态
        SUBMITTED,      // 已提交
        UNDER_REVIEW,   // 审核中
        APPROVED,       // 已通过
        REJECTED,       // 已拒绝
        REVISION_REQUESTED, // 需要修改
        ARCHIVED        // 已归档
    }

    // Constructors
    public WeeklyReport() {}

    public WeeklyReport(String title, LocalDate reportWeek, User author) {
        this.title = title;
        this.reportWeek = reportWeek;
        this.author = author;
        // 计算年份和周数
        calculateYearAndWeek();
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
        calculateYearAndWeek();
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
        calculateWordCount();
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

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<AIAnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(Set<AIAnalysisResult> analysisResults) {
        this.analysisResults = analysisResults;
    }

    // Utility methods for managing relationships
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setWeeklyReport(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setWeeklyReport(null);
    }

    public void addAnalysisResult(AIAnalysisResult analysisResult) {
        analysisResults.add(analysisResult);
        analysisResult.setWeeklyReport(this);
    }

    public void removeAnalysisResult(AIAnalysisResult analysisResult) {
        analysisResults.remove(analysisResult);
        analysisResult.setWeeklyReport(null);
    }

    // Business logic methods
    public void submit() {
        if (this.status == ReportStatus.DRAFT) {
            this.status = ReportStatus.SUBMITTED;
            this.submittedAt = LocalDateTime.now();
        }
    }

    public void approve(User reviewer, String comment) {
        this.status = ReportStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = comment;
    }

    public void reject(User reviewer, String comment) {
        this.status = ReportStatus.REJECTED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = comment;
    }

    public void requestRevision(User reviewer, String comment) {
        this.status = ReportStatus.REVISION_REQUESTED;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = comment;
    }

    // Calculate year and week number from report week date
    private void calculateYearAndWeek() {
        if (reportWeek != null) {
            this.year = reportWeek.getYear();
            // 使用ISO周计算
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
            this.weekNumber = reportWeek.get(weekFields.weekOfYear());
        }
    }

    // Calculate word count from content
    private void calculateWordCount() {
        if (content != null) {
            // 简单的字数统计，移除空格和标点符号
            this.wordCount = content.replaceAll("\\s+|\\p{Punct}", "").length();
        }
    }

    // Check if the report is overdue
    public boolean isOverdue() {
        if (status == ReportStatus.DRAFT && reportWeek != null) {
            LocalDate deadline = reportWeek.plusDays(7); // 假设截止时间为下周一
            return LocalDate.now().isAfter(deadline);
        }
        return false;
    }

    // Get the completion percentage based on filled fields
    public int getCompletionPercentage() {
        int totalFields = 6; // content, workSummary, achievements, challenges, nextWeekPlan, additionalNotes
        int filledFields = 0;

        if (content != null && !content.trim().isEmpty()) filledFields++;
        if (workSummary != null && !workSummary.trim().isEmpty()) filledFields++;
        if (achievements != null && !achievements.trim().isEmpty()) filledFields++;
        if (challenges != null && !challenges.trim().isEmpty()) filledFields++;
        if (nextWeekPlan != null && !nextWeekPlan.trim().isEmpty()) filledFields++;
        if (additionalNotes != null && !additionalNotes.trim().isEmpty()) filledFields++;

        return (filledFields * 100) / totalFields;
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
                ", title='" + title + '\'' +
                ", reportWeek=" + reportWeek +
                ", status=" + status +
                ", author=" + (author != null ? author.getFullName() : "null") +
                '}';
    }
}