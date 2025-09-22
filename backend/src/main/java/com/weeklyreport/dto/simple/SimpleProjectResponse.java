package com.weeklyreport.dto.simple;

import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.User;

import java.time.LocalDateTime;

/**
 * 简化项目响应DTO，避免懒加载问题
 */
public class SimpleProjectResponse {
    
    private Long id;
    private String projectName;
    private String projectContent;
    private String projectMembers;
    private String expectedResults;
    private String actualResults;
    private String timeline;
    private String stopLoss;
    private String status;
    private String aiAnalysisResult;
    private UserInfo createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 审核人信息
    private UserInfo managerReviewer;
    private String managerReviewComment;
    private LocalDateTime managerReviewedAt;
    private UserInfo adminReviewer;
    private String adminReviewComment;
    private LocalDateTime adminReviewedAt;
    private UserInfo superAdminReviewer;
    private String superAdminReviewComment;
    private LocalDateTime superAdminReviewedAt;
    
    // 内部用户信息类
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        
        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.role = user.getRole().name();
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
    }
    
    public SimpleProjectResponse(SimpleProject project) {
        this.id = project.getId();
        this.projectName = project.getProjectName();
        this.projectContent = project.getProjectContent();
        this.projectMembers = project.getProjectMembers();
        this.expectedResults = project.getExpectedResults();
        this.actualResults = project.getActualResults();
        this.timeline = project.getTimeline();
        this.stopLoss = project.getStopLoss();
        this.status = project.getStatus().name();
        this.aiAnalysisResult = project.getAiAnalysisResult();
        this.createdBy = new UserInfo(project.getCreatedBy());
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        
        // 设置审核人信息
        this.managerReviewer = project.getManagerReviewer() != null ? new UserInfo(project.getManagerReviewer()) : null;
        this.managerReviewComment = project.getManagerReviewComment();
        this.managerReviewedAt = project.getManagerReviewedAt();
        this.adminReviewer = project.getAdminReviewer() != null ? new UserInfo(project.getAdminReviewer()) : null;
        this.adminReviewComment = project.getAdminReviewComment();
        this.adminReviewedAt = project.getAdminReviewedAt();
        this.superAdminReviewer = project.getSuperAdminReviewer() != null ? new UserInfo(project.getSuperAdminReviewer()) : null;
        this.superAdminReviewComment = project.getSuperAdminReviewComment();
        this.superAdminReviewedAt = project.getSuperAdminReviewedAt();
    }

    // Getters
    public Long getId() { return id; }
    public String getProjectName() { return projectName; }
    public String getProjectContent() { return projectContent; }
    public String getProjectMembers() { return projectMembers; }
    public String getExpectedResults() { return expectedResults; }
    public String getActualResults() { return actualResults; }
    public String getTimeline() { return timeline; }
    public String getStopLoss() { return stopLoss; }
    public String getStatus() { return status; }
    public String getAiAnalysisResult() { return aiAnalysisResult; }
    public UserInfo getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // 审核人信息 Getters
    public UserInfo getManagerReviewer() { return managerReviewer; }
    public String getManagerReviewComment() { return managerReviewComment; }
    public LocalDateTime getManagerReviewedAt() { return managerReviewedAt; }
    public UserInfo getAdminReviewer() { return adminReviewer; }
    public String getAdminReviewComment() { return adminReviewComment; }
    public LocalDateTime getAdminReviewedAt() { return adminReviewedAt; }
    public UserInfo getSuperAdminReviewer() { return superAdminReviewer; }
    public String getSuperAdminReviewComment() { return superAdminReviewComment; }
    public LocalDateTime getSuperAdminReviewedAt() { return superAdminReviewedAt; }
}