package com.weeklyreport.dto.project;

import jakarta.validation.constraints.*;

/**
 * DTO for creating new project phases - 严格按照ProjectPhase.java设计
 */
public class ProjectPhaseCreateRequest {

    // projectId在创建项目时由后端自动设置，请求中不需要提供
    private Long projectId;                         // #关联项目ID

    @NotBlank(message = "Phase name cannot be blank")
    @Size(max = 200, message = "Phase name must not exceed 200 characters")
    private String phaseName;                       // #任务名称

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;                     // #阶段描述

    @Size(max = 500, message = "Assigned members must not exceed 500 characters")
    private String assignedMembers;                 // #负责成员

    @Size(max = 300, message = "Schedule must not exceed 300 characters")
    private String schedule;                        // #时间安排

    @Size(max = 5000, message = "Expected results must not exceed 5000 characters")
    private String expectedResults;                 // #预期结果

    // Constructors
    public ProjectPhaseCreateRequest() {}

    public ProjectPhaseCreateRequest(Long projectId, String phaseName) {
        this.projectId = projectId;
        this.phaseName = phaseName;
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedMembers() {
        return assignedMembers;
    }

    public void setAssignedMembers(String assignedMembers) {
        this.assignedMembers = assignedMembers;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    @Override
    public String toString() {
        return "ProjectPhaseCreateRequest{" +
                "projectId=" + projectId +
                ", phaseName='" + phaseName + '\'' +
                '}';
    }
}