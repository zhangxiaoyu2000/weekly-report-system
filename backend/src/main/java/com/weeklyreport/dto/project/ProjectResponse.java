package com.weeklyreport.dto.project;

import com.weeklyreport.entity.Project;
import com.weeklyreport.dto.ai.AIAnalysisResultResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for project responses - 严格按照Project.java实体设计
 */
public class ProjectResponse {

    private Long id;
    private String name;                            // #项目名称
    private String description;                     // #项目内容
    private String members;                         // #项目成员
    private String expectedResults;                 // #预期结果
    private String timeline;                        // #时间线
    private String stopLoss;                        // #止损点
    private Long createdBy;                         // 创建者ID
    private String createdByUsername;               // 创建者用户名
    
    // 审批流程字段
    private Long aiAnalysisId;                      // AI分析结果ID
    private Long adminReviewerId;                   // 管理员审批人ID
    private Long superAdminReviewerId;              // 超级管理员审批人ID
    private String rejectionReason;                 // 拒绝理由
    private Project.ApprovalStatus approvalStatus;  // 审批状态
    
    // 时间戳字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 兼容性字段
    private Project.ProjectStatus status;           // 兼容ProjectStatus
    private Project.ProjectPriority priority;       // 兼容ProjectPriority
    private Integer progress;                       // 进度百分比
    
    // 阶段性任务列表
    private List<ProjectPhaseResponse> phases;      // #阶段性任务列表
    
    // AI分析结果
    private AIAnalysisResultResponse aiAnalysisResult;      // AI分析结果详情

    // Constructors
    public ProjectResponse() {}

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.members = project.getMembers();
        this.expectedResults = project.getExpectedResults();
        this.timeline = project.getTimeline();
        this.stopLoss = project.getStopLoss();
        this.createdBy = project.getCreatedBy();
        
        // 审批流程字段
        this.aiAnalysisId = project.getAiAnalysisId();
        this.adminReviewerId = project.getAdminReviewerId();
        this.superAdminReviewerId = project.getSuperAdminReviewerId();
        this.rejectionReason = project.getRejectionReason();
        this.approvalStatus = project.getApprovalStatus();
        
        // 时间戳字段
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        
        // 兼容性字段
        this.status = project.getStatus();
        this.priority = project.getPriority();
        this.progress = project.getProgress();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
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

    public Long getSuperAdminReviewerId() {
        return superAdminReviewerId;
    }

    public void setSuperAdminReviewerId(Long superAdminReviewerId) {
        this.superAdminReviewerId = superAdminReviewerId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Project.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Project.ApprovalStatus approvalStatus) {
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

    public Project.ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(Project.ProjectStatus status) {
        this.status = status;
    }

    public Project.ProjectPriority getPriority() {
        return priority;
    }

    public void setPriority(Project.ProjectPriority priority) {
        this.priority = priority;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public List<ProjectPhaseResponse> getPhases() {
        return phases;
    }

    public void setPhases(List<ProjectPhaseResponse> phases) {
        this.phases = phases;
    }

    public AIAnalysisResultResponse getAiAnalysisResult() {
        return aiAnalysisResult;
    }

    public void setAiAnalysisResult(AIAnalysisResultResponse aiAnalysisResult) {
        this.aiAnalysisResult = aiAnalysisResult;
    }

    // Helper methods
    public boolean isDraft() {
        return approvalStatus == Project.ApprovalStatus.AI_ANALYZING;
    }

    public boolean isSubmitted() {
        return approvalStatus == Project.ApprovalStatus.AI_ANALYZING;
    }

    public boolean isApproved() {
        return approvalStatus == Project.ApprovalStatus.ADMIN_APPROVED || 
               approvalStatus == Project.ApprovalStatus.SUPER_ADMIN_APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus == Project.ApprovalStatus.ADMIN_REJECTED || 
               approvalStatus == Project.ApprovalStatus.SUPER_ADMIN_REJECTED ||
               approvalStatus == Project.ApprovalStatus.AI_REJECTED;
    }

    @Override
    public String toString() {
        return "ProjectResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", approvalStatus=" + approvalStatus +
                ", createdBy=" + createdBy +
                '}';
    }
}