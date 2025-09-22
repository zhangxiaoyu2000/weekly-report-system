package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity - 严格按照数据库设计.md第59-81行简化
 * 
 * 删除的字段 (数据库设计.md 第74-81行)：
 * - first_name, last_name → 简化为username
 * - department_id → 不需要部门管理
 * - employee_id, position, phone, avatar_url → 非核心字段
 * - last_login, last_login_time → 重复字段
 * - deleted_at → 使用status管理
 * - full_name → 计算字段，非必要
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_status", columnList = "status")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                // #用户ID (error3.md要求)

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;                        // #用户名

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;                           // #邮箱

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false, length = 255)
    private String password;                        // #密码

    @NotNull(message = "Role cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.MANAGER;               // #角色 (系统角色分为主管，管理员和超级管理员)

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE; // #状态

    // 时间戳字段
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 用户角色枚举 - 按照CLAUDE.md项目说明中的角色分类
    public enum Role {
        MANAGER,        // 主管 - 主要负责项目创建并提交，周报创建并提交
        ADMIN,          // 管理员 - 主要负责项目和周报的审核
        SUPER_ADMIN,    // 超级管理员 - 主要负责项目和周报的创建和审核
        EMPLOYEE        // 员工 - 兼容性角色
    }

    // 用户状态枚举
    public enum UserStatus {
        ACTIVE,         // 激活状态 - 可以正常登录和使用系统
        INACTIVE        // 非激活状态 - 禁止登录，账户被禁用
    }

    // Constructors
    public User() {}

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = UserStatus.ACTIVE;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
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

    // Business logic methods
    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean canCreateProjects() {
        return role == Role.MANAGER || role == Role.SUPER_ADMIN;
    }

    public boolean canReviewProjects() {
        return role == Role.ADMIN || role == Role.SUPER_ADMIN;
    }

    public boolean canCreateWeeklyReports() {
        return role == Role.MANAGER || role == Role.ADMIN || role == Role.SUPER_ADMIN;
    }

    public boolean canReviewWeeklyReports() {
        return role == Role.ADMIN || role == Role.SUPER_ADMIN;
    }

    // 兼容性方法 - 为了支持旧代码
    public String getFullName() {
        return username; // 在简化版本中，用username作为全名
    }

    public String getEmployeeId() {
        return null; // 简化版本中不支持员工ID
    }

    public String getPosition() {
        return null; // 简化版本中不支持职位
    }

    public String getPhone() {
        return null; // 简化版本中不支持电话
    }

    public String getAvatarUrl() {
        return null; // 简化版本中不支持头像
    }

    public LocalDateTime getLastLoginTime() {
        return null; // 简化版本中不支持最后登录时间
    }

    public Object getDepartment() {
        return null; // 简化版本中不支持部门
    }

    // 更多兼容性方法
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

    public LocalDateTime getLastLogin() {
        return null; // 简化版本中不支持
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        // 简化版本中忽略
    }

    public void setDepartment(Object department) {
        // 简化版本中不支持部门关联
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}