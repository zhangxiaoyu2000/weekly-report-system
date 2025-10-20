package com.weeklyreport.project.dto;

import com.weeklyreport.project.entity.ProjectPhase;
import java.time.LocalDateTime;

/**
 * Response DTO for project phase - 严格按照ProjectPhase.java设计
 */
public class ProjectPhaseResponse {

    private Long id;                                // #任务ID
    private Long projectId;                         // #关联项目ID
    private String phaseName;                       // #任务名称
    private String description;                     // #阶段描述
    private String assignedMembers;                 // #负责成员
    private String schedule;                        // #时间安排
    private String expectedResults;                 // #预期结果
    private String actualResults;                   // #实际结果
    private String resultDifferenceAnalysis;        // #结果差异分析
    private LocalDateTime createdAt;                // 创建时间
    private LocalDateTime updatedAt;                // 更新时间

    // Constructors
    public ProjectPhaseResponse() {}

    public ProjectPhaseResponse(ProjectPhase phase) {
        this.id = phase.getId();
        this.projectId = phase.getProjectId();
        this.phaseName = phase.getPhaseName();
        this.description = phase.getDescription();
        this.assignedMembers = phase.getAssignedMembers();
        this.schedule = phase.getSchedule();
        this.expectedResults = phase.getExpectedResults();
        // actualResults 和 resultDifferenceAnalysis 已移至 DevTaskReport
        this.actualResults = null; // TODO: 从关联的DevTaskReport获取
        this.resultDifferenceAnalysis = null; // TODO: 从关联的DevTaskReport获取
        this.createdAt = phase.getCreatedAt();
        this.updatedAt = phase.getUpdatedAt();
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

    public String getActualResults() {
        return actualResults;
    }

    public void setActualResults(String actualResults) {
        this.actualResults = actualResults;
    }

    public String getResultDifferenceAnalysis() {
        return resultDifferenceAnalysis;
    }

    public void setResultDifferenceAnalysis(String resultDifferenceAnalysis) {
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
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

    // Helper methods
    public boolean isCompleted() {
        return actualResults != null && !actualResults.trim().isEmpty();
    }

    public int getCompletionPercentage() {
        int totalFields = 7; // phaseName, description, assignedMembers, schedule, expectedResults, actualResults, resultDifferenceAnalysis
        int filledFields = 0;

        if (phaseName != null && !phaseName.trim().isEmpty()) filledFields++;
        if (description != null && !description.trim().isEmpty()) filledFields++;
        if (assignedMembers != null && !assignedMembers.trim().isEmpty()) filledFields++;
        if (schedule != null && !schedule.trim().isEmpty()) filledFields++;
        if (expectedResults != null && !expectedResults.trim().isEmpty()) filledFields++;
        if (actualResults != null && !actualResults.trim().isEmpty()) filledFields++;
        if (resultDifferenceAnalysis != null && !resultDifferenceAnalysis.trim().isEmpty()) filledFields++;

        return (filledFields * 100) / totalFields;
    }

    @Override
    public String toString() {
        return "ProjectPhaseResponse{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", phaseName='" + phaseName + '\'' +
                '}';
    }
}