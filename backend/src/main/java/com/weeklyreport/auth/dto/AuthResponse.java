package com.weeklyreport.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.weeklyreport.user.entity.User;

import java.time.LocalDateTime;

/**
 * Authentication response DTO - 严格按照User.java简化设计
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // Token expiration time in seconds
    private UserInfo user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Static factory method for successful authentication
    public static AuthResponse success(String accessToken, String refreshToken, Long expiresIn, User user) {
        return new AuthResponse(accessToken, refreshToken, expiresIn, UserInfo.fromUser(user));
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    // 兼容性方法 - 用于测试接口
    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new UserInfo();
        }
        this.user.setId(userId);
    }
    
    public void setUsername(String username) {
        if (this.user == null) {
            this.user = new UserInfo();
        }
        this.user.setUsername(username);
    }
    
    public void setRole(User.Role role) {
        if (this.user == null) {
            this.user = new UserInfo();
        }
        this.user.setRole(role);
    }

    /**
     * User information DTO for authentication response - 简化版本
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;                        // #用户ID
        private String username;                // #用户名
        private String email;                   // #邮箱
        private User.Role role;                 // #角色（系统角色分为主管，管理员和超级管理员）
        private User.UserStatus status;         // #状态
        private LocalDateTime createdAt;        // 创建时间
        private LocalDateTime updatedAt;        // 更新时间
        
        // 兼容性字段
        private String fullName;                // 计算字段，供前端使用

        // Constructors
        public UserInfo() {}

        // Static factory method
        public static UserInfo fromUser(User user) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setRole(user.getRole());
            userInfo.setStatus(user.getStatus());
            userInfo.setCreatedAt(user.getCreatedAt());
            userInfo.setUpdatedAt(user.getUpdatedAt());
            
            // 兼容性字段
            userInfo.setFullName(user.getFullName()); // 在简化版本中，用username作为全名
            
            return userInfo;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        // 兼容性方法
        public String getFirstName() {
            return username; // 简化版本中用username代替
        }

        public void setFirstName(String firstName) {
            // 简化版本中忽略
        }

        public String getLastName() {
            return ""; // 简化版本中返回空字符串
        }

        public void setLastName(String lastName) {
            // 简化版本中忽略
        }

        public User.Role getRole() {
            return role;
        }

        public void setRole(User.Role role) {
            this.role = role;
        }

        public User.UserStatus getStatus() {
            return status;
        }

        public void setStatus(User.UserStatus status) {
            this.status = status;
        }

        public String getDepartmentName() {
            return null; // 简化版本中不支持部门
        }

        public void setDepartmentName(String departmentName) {
            // 简化版本中忽略
        }

        public LocalDateTime getLastLogin() {
            return null; // 简化版本中不支持最后登录时间
        }

        public void setLastLogin(LocalDateTime lastLogin) {
            // 简化版本中忽略
        }
        
        // 业务方法
        public boolean isManager() {
            return role == User.Role.MANAGER;
        }

        public boolean isAdmin() {
            return role == User.Role.ADMIN;
        }

        public boolean isSuperAdmin() {
            return role == User.Role.SUPER_ADMIN;
        }

        public boolean isActive() {
            return status == User.UserStatus.ACTIVE;
        }
    }
}