package com.weeklyreport.dto.auth;

import com.weeklyreport.entity.User;
import com.weeklyreport.validation.PasswordMatching;
import jakarta.validation.constraints.*;

/**
 * Registration request DTO - 严格按照User.java简化设计
 */
@PasswordMatching(passwordField = "password", confirmPasswordField = "confirmPassword")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;                    // #用户名

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;                       // #邮箱

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;                    // #密码（简化验证规则）

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;             // 确认密码

    @NotNull(message = "Role cannot be null")
    private User.Role role = User.Role.MANAGER; // #角色（系统角色分为主管，管理员和超级管理员）

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password, String confirmPassword, User.Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.role = role;
    }

    // Getters and Setters
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }


    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    // Custom validation method
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
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

    public Long getDepartmentId() {
        return null; // 简化版本中不支持部门
    }

    public void setDepartmentId(Long departmentId) {
        // 简化版本中忽略
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                ", role=" + role +
                '}';
    }
}