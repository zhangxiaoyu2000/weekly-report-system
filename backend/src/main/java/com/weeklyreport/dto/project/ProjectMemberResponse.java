package com.weeklyreport.dto.project;

import com.weeklyreport.entity.ProjectMember;
import com.weeklyreport.entity.User;

import java.time.LocalDateTime;

/**
 * DTO for project member response
 */
public class ProjectMemberResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private UserInfo user;
    private ProjectMember.ProjectRole role;
    private ProjectMember.MemberStatus status;
    private LocalDateTime joinedDate;
    private LocalDateTime leftDate;
    private String notes;
    private Long invitedBy;
    private String invitedByName;

    // Nested class for user information
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String position;
        private String avatarUrl;

        public UserInfo() {}

        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.position = user.getPosition();
            this.avatarUrl = user.getAvatarUrl();
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

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    // Constructors
    public ProjectMemberResponse() {}

    public ProjectMemberResponse(ProjectMember projectMember) {
        this.id = projectMember.getId();
        this.projectId = projectMember.getProject().getId();
        this.projectName = projectMember.getProject().getName();
        this.user = new UserInfo(projectMember.getUser());
        this.role = projectMember.getRole();
        this.status = projectMember.getStatus();
        this.joinedDate = projectMember.getJoinedDate();
        this.leftDate = projectMember.getLeftDate();
        this.notes = projectMember.getNotes();
        this.invitedBy = projectMember.getInvitedBy();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public ProjectMember.ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectMember.ProjectRole role) {
        this.role = role;
    }

    public ProjectMember.MemberStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectMember.MemberStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    public LocalDateTime getLeftDate() {
        return leftDate;
    }

    public void setLeftDate(LocalDateTime leftDate) {
        this.leftDate = leftDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getInvitedByName() {
        return invitedByName;
    }

    public void setInvitedByName(String invitedByName) {
        this.invitedByName = invitedByName;
    }

    @Override
    public String toString() {
        return "ProjectMemberResponse{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", user=" + user +
                ", role=" + role +
                ", status=" + status +
                ", joinedDate=" + joinedDate +
                '}';
    }
}