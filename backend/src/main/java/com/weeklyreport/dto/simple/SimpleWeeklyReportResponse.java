package com.weeklyreport.dto.simple;

import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.SimpleWeeklyReport;
import com.weeklyreport.entity.User;

import java.time.LocalDateTime;

/**
 * 简化周报响应DTO，避免懒加载问题
 */
public class SimpleWeeklyReportResponse {
    
    private Long id;
    private String actualResults;
    private ProjectInfo project;
    private UserInfo createdBy;
    private LocalDateTime createdAt;
    
    // 内部项目信息类
    public static class ProjectInfo {
        private Long id;
        private String projectName;
        private String status;
        
        public ProjectInfo(SimpleProject project) {
            this.id = project.getId();
            this.projectName = project.getProjectName();
            this.status = project.getStatus().name();
        }

        // Getters
        public Long getId() { return id; }
        public String getProjectName() { return projectName; }
        public String getStatus() { return status; }
    }
    
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
    
    public SimpleWeeklyReportResponse(SimpleWeeklyReport report) {
        this.id = report.getId();
        this.actualResults = report.getActualResults();
        this.project = new ProjectInfo(report.getProject());
        this.createdBy = new UserInfo(report.getCreatedBy());
        this.createdAt = report.getCreatedAt();
    }

    // 兼容构造函数 - 用于从WeeklyReport转换
    public SimpleWeeklyReportResponse(Long id, SimpleProject project, String content, User user, LocalDateTime createdAt) {
        this.id = id;
        this.actualResults = content;
        this.project = new ProjectInfo(project);
        this.createdBy = user != null ? new UserInfo(user) : null;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getActualResults() { return actualResults; }
    public ProjectInfo getProject() { return project; }
    public UserInfo getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}