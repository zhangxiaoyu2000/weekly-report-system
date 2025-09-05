package com.weeklyreport.dto.project;

import com.weeklyreport.entity.ProjectMember;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for adding a project member
 */
public class ProjectMemberRequest {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Role cannot be null")
    private ProjectMember.ProjectRole role;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Constructors
    public ProjectMemberRequest() {}

    public ProjectMemberRequest(Long userId, ProjectMember.ProjectRole role) {
        this.userId = userId;
        this.role = role;
    }

    public ProjectMemberRequest(Long userId, ProjectMember.ProjectRole role, String notes) {
        this.userId = userId;
        this.role = role;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ProjectMember.ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectMember.ProjectRole role) {
        this.role = role;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ProjectMemberRequest{" +
                "userId=" + userId +
                ", role=" + role +
                ", notes='" + notes + '\'' +
                '}';
    }
}