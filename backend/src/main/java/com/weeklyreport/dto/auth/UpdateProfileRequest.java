package com.weeklyreport.dto.auth;

import jakarta.validation.constraints.*;

/**
 * Update user profile request DTO
 */
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    private String employeeId;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phone;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    @Pattern(regexp = "^https?://.*", message = "Avatar URL must be a valid HTTP/HTTPS URL")
    private String avatarUrl;

    // Constructors
    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String fullName) {
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "fullName='" + fullName + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", phone='" + phone + '\'' +
                ", position='" + position + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}