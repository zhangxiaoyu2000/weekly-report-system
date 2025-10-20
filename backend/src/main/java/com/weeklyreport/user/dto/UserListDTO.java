package com.weeklyreport.user.dto;

import com.weeklyreport.user.entity.User;
import java.time.LocalDateTime;

/**
 * 用户列表DTO - 用于优化用户列表查询性能
 * 只包含必要的字段，避免加载关联数据
 */
public class UserListDTO {
    
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String position;
    private String role;
    private String status;
    private String departmentName;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    
    // 构造函数
    public UserListDTO() {}
    
    public UserListDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.position = ""; // Position not available in simplified entity
        this.role = user.getRole() != null ? user.getRole().name() : null;
        this.status = user.getStatus() != null ? user.getStatus().name() : null;
        this.lastLoginTime = user.getLastLogin(); // Use lastLogin instead of lastLoginTime
        this.createdAt = user.getCreatedAt();
        // 注意：不加载department关联，避免懒加载查询
    }
    
    // 带部门名称的构造函数（用于JOIN查询）
    public UserListDTO(Long id, String username, String fullName, String email, 
                      String position, String role, String status, String departmentName,
                      LocalDateTime lastLoginTime, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.position = position;
        this.role = role;
        this.status = status;
        this.departmentName = departmentName;
        this.lastLoginTime = lastLoginTime;
        this.createdAt = createdAt;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}