package com.weeklyreport.auth.dto;

import jakarta.validation.constraints.*;

/**
 * Update user profile request DTO
 */
public class UpdateProfileRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

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

    public UpdateProfileRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public UpdateProfileRequest(String fullName) {
        this.fullName = fullName;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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