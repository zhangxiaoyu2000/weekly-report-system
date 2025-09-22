package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 简化的项目实体 - 只包含核心业务字段
 */
@Entity
@Table(name = "simple_projects")
public class SimpleProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Column(name = "project_name", nullable = false)
    private String projectName;

    @NotBlank(message = "项目内容不能为空")
    @Column(name = "project_content", columnDefinition = "TEXT")
    private String projectContent;

    @NotBlank(message = "项目成员不能为空")
    @Column(name = "project_members", columnDefinition = "TEXT")
    private String projectMembers;


    @NotBlank(message = "预期结果不能为空，需要以量化指标形式填写")
    @Column(name = "expected_results", columnDefinition = "TEXT")
    private String expectedResults;

    @Column(name = "actual_results", columnDefinition = "TEXT")
    private String actualResults;

    @NotBlank(message = "时间线不能为空")
    @Column(name = "timeline", columnDefinition = "TEXT")
    private String timeline;

    @NotBlank(message = "止损点不能为空")
    @Column(name = "stop_loss", columnDefinition = "TEXT")
    private String stopLoss;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.PENDING_AI_ANALYSIS;

    @Column(name = "ai_analysis_result", columnDefinition = "TEXT")
    private String aiAnalysisResult;
    
    // Enhanced AI analysis fields
    @Column(name = "ai_confidence")
    private Double aiConfidence;
    
    @Column(name = "ai_feasibility_score")
    private Double aiFeasibilityScore;
    
    @Column(name = "ai_risk_level", length = 20)
    private String aiRiskLevel;
    
    @Column(name = "ai_provider_used", length = 50)
    private String aiProviderUsed;
    
    @Column(name = "ai_processing_time_ms")
    private Long aiProcessingTimeMs;
    
    @Column(name = "ai_analyzed_at")
    private LocalDateTime aiAnalyzedAt;
    
    @Column(name = "ai_key_issues", columnDefinition = "JSON")
    private String aiKeyIssues;
    
    @Column(name = "ai_recommendations", columnDefinition = "JSON")
    private String aiRecommendations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_reviewer_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User managerReviewer;

    @Column(name = "manager_review_comment", columnDefinition = "TEXT")
    private String managerReviewComment;

    @Column(name = "manager_reviewed_at")
    private LocalDateTime managerReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_reviewer_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User adminReviewer;

    @Column(name = "admin_review_comment", columnDefinition = "TEXT")
    private String adminReviewComment;

    @Column(name = "admin_reviewed_at")
    private LocalDateTime adminReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_reviewer_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User superAdminReviewer;

    @Column(name = "super_admin_review_comment", columnDefinition = "TEXT")
    private String superAdminReviewComment;

    @Column(name = "super_admin_reviewed_at")
    private LocalDateTime superAdminReviewedAt;

    @NotNull(message = "创建者不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<SimpleWeeklyReport> weeklyReports = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 项目状态枚举
    public enum ProjectStatus {
        SUBMITTED("已提交"),
        PENDING_MANAGER_REVIEW("待主管审核"),
        MANAGER_REJECTED("主管拒绝"),
        PENDING_AI_ANALYSIS("待AI分析"),
        AI_REJECTED("AI不合格"),
        PENDING_ADMIN_REVIEW("待管理员审核"),
        ADMIN_REJECTED("管理员拒绝"),
        PENDING_SUPER_ADMIN_REVIEW("待超级管理员审核"),
        SUPER_ADMIN_REJECTED("超级管理员拒绝"),
        APPROVED("最终批准");

        private final String displayName;

        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 构造函数
    public SimpleProject() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectContent() {
        return projectContent;
    }

    public void setProjectContent(String projectContent) {
        this.projectContent = projectContent;
    }

    public String getProjectMembers() {
        return projectMembers;
    }

    public void setProjectMembers(String projectMembers) {
        this.projectMembers = projectMembers;
    }


    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    public String getActualResults() {
        return actualResults;
    }

    public void setActualResults(String actualResults) {
        this.actualResults = actualResults;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public String getAiAnalysisResult() {
        return aiAnalysisResult;
    }

    public void setAiAnalysisResult(String aiAnalysisResult) {
        this.aiAnalysisResult = aiAnalysisResult;
    }

    public User getManagerReviewer() {
        return managerReviewer;
    }

    public void setManagerReviewer(User managerReviewer) {
        this.managerReviewer = managerReviewer;
    }

    public String getManagerReviewComment() {
        return managerReviewComment;
    }

    public void setManagerReviewComment(String managerReviewComment) {
        this.managerReviewComment = managerReviewComment;
    }

    public LocalDateTime getManagerReviewedAt() {
        return managerReviewedAt;
    }

    public void setManagerReviewedAt(LocalDateTime managerReviewedAt) {
        this.managerReviewedAt = managerReviewedAt;
    }

    public User getAdminReviewer() {
        return adminReviewer;
    }

    public void setAdminReviewer(User adminReviewer) {
        this.adminReviewer = adminReviewer;
    }

    public String getAdminReviewComment() {
        return adminReviewComment;
    }

    public void setAdminReviewComment(String adminReviewComment) {
        this.adminReviewComment = adminReviewComment;
    }

    public LocalDateTime getAdminReviewedAt() {
        return adminReviewedAt;
    }

    public void setAdminReviewedAt(LocalDateTime adminReviewedAt) {
        this.adminReviewedAt = adminReviewedAt;
    }

    public User getSuperAdminReviewer() {
        return superAdminReviewer;
    }

    public void setSuperAdminReviewer(User superAdminReviewer) {
        this.superAdminReviewer = superAdminReviewer;
    }

    public String getSuperAdminReviewComment() {
        return superAdminReviewComment;
    }

    public void setSuperAdminReviewComment(String superAdminReviewComment) {
        this.superAdminReviewComment = superAdminReviewComment;
    }

    public LocalDateTime getSuperAdminReviewedAt() {
        return superAdminReviewedAt;
    }

    public void setSuperAdminReviewedAt(LocalDateTime superAdminReviewedAt) {
        this.superAdminReviewedAt = superAdminReviewedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<SimpleWeeklyReport> getWeeklyReports() {
        return weeklyReports;
    }

    public void setWeeklyReports(List<SimpleWeeklyReport> weeklyReports) {
        this.weeklyReports = weeklyReports;
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

    // Enhanced AI analysis fields getters and setters
    public Double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(Double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public Double getAiFeasibilityScore() {
        return aiFeasibilityScore;
    }

    public void setAiFeasibilityScore(Double aiFeasibilityScore) {
        this.aiFeasibilityScore = aiFeasibilityScore;
    }

    public String getAiRiskLevel() {
        return aiRiskLevel;
    }

    public void setAiRiskLevel(String aiRiskLevel) {
        this.aiRiskLevel = aiRiskLevel;
    }

    public String getAiProviderUsed() {
        return aiProviderUsed;
    }

    public void setAiProviderUsed(String aiProviderUsed) {
        this.aiProviderUsed = aiProviderUsed;
    }

    public Long getAiProcessingTimeMs() {
        return aiProcessingTimeMs;
    }

    public void setAiProcessingTimeMs(Long aiProcessingTimeMs) {
        this.aiProcessingTimeMs = aiProcessingTimeMs;
    }

    public LocalDateTime getAiAnalyzedAt() {
        return aiAnalyzedAt;
    }

    public void setAiAnalyzedAt(LocalDateTime aiAnalyzedAt) {
        this.aiAnalyzedAt = aiAnalyzedAt;
    }

    public String getAiKeyIssues() {
        return aiKeyIssues;
    }

    public void setAiKeyIssues(String aiKeyIssues) {
        this.aiKeyIssues = aiKeyIssues;
    }

    public String getAiRecommendations() {
        return aiRecommendations;
    }

    public void setAiRecommendations(String aiRecommendations) {
        this.aiRecommendations = aiRecommendations;
    }
}