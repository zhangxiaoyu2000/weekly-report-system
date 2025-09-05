package com.weeklyreport.dto.project;

import com.weeklyreport.entity.ProjectMember;

import java.util.Map;

/**
 * DTO for project member statistics
 */
public class ProjectMemberStatsResponse {

    private Long projectId;
    private String projectName;
    private long totalMembers;
    private long activeMembers;
    private long inactiveMembers;
    private long invitedMembers;
    private long removedMembers;
    private Map<ProjectMember.ProjectRole, Long> membersByRole;

    // Constructors
    public ProjectMemberStatsResponse() {}

    public ProjectMemberStatsResponse(Long projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // Getters and Setters
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

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(long activeMembers) {
        this.activeMembers = activeMembers;
    }

    public long getInactiveMembers() {
        return inactiveMembers;
    }

    public void setInactiveMembers(long inactiveMembers) {
        this.inactiveMembers = inactiveMembers;
    }

    public long getInvitedMembers() {
        return invitedMembers;
    }

    public void setInvitedMembers(long invitedMembers) {
        this.invitedMembers = invitedMembers;
    }

    public long getRemovedMembers() {
        return removedMembers;
    }

    public void setRemovedMembers(long removedMembers) {
        this.removedMembers = removedMembers;
    }

    public Map<ProjectMember.ProjectRole, Long> getMembersByRole() {
        return membersByRole;
    }

    public void setMembersByRole(Map<ProjectMember.ProjectRole, Long> membersByRole) {
        this.membersByRole = membersByRole;
    }

    @Override
    public String toString() {
        return "ProjectMemberStatsResponse{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", totalMembers=" + totalMembers +
                ", activeMembers=" + activeMembers +
                ", membersByRole=" + membersByRole +
                '}';
    }
}