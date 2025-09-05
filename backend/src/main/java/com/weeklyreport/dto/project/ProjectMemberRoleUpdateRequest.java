package com.weeklyreport.dto.project;

import com.weeklyreport.entity.ProjectMember;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a project member's role
 */
public class ProjectMemberRoleUpdateRequest {

    @NotNull(message = "Role cannot be null")
    private ProjectMember.ProjectRole role;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Constructors
    public ProjectMemberRoleUpdateRequest() {}

    public ProjectMemberRoleUpdateRequest(ProjectMember.ProjectRole role) {
        this.role = role;
    }

    public ProjectMemberRoleUpdateRequest(ProjectMember.ProjectRole role, String notes) {
        this.role = role;
        this.notes = notes;
    }

    // Getters and Setters
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
        return "ProjectMemberRoleUpdateRequest{" +
                "role=" + role +
                ", notes='" + notes + '\'' +
                '}';
    }
}