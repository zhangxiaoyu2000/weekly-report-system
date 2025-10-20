package com.weeklyreport.weeklyreport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DevTaskReportDTO - 发展任务报告数据传输对象
 * 用于返回周报中发展任务的详细信息和执行结果
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DevTaskReportDTO {

    private Long projectId;
    private String projectName;
    private String projectDescription;
    private Long phasesId;
    private String phaseName;
    private String phaseDescription;
    private String assignedMembers;
    private String schedule;
    private String expectedResults;
    private String actualResults;
    private String resultDifferenceAnalysis;

    // Constructors
    public DevTaskReportDTO() {}

    public DevTaskReportDTO(Long projectId, String projectName, Long phasesId, String phaseName,
                           String actualResults, String resultDifferenceAnalysis) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.phasesId = phasesId;
        this.phaseName = phaseName;
        this.actualResults = actualResults;
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
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

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Long getPhasesId() {
        return phasesId;
    }

    public void setPhasesId(Long phasesId) {
        this.phasesId = phasesId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseDescription() {
        return phaseDescription;
    }

    public void setPhaseDescription(String phaseDescription) {
        this.phaseDescription = phaseDescription;
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

    @Override
    public String toString() {
        return "DevTaskReportDTO{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", phasesId=" + phasesId +
                ", phaseName='" + phaseName + '\'' +
                ", actualResults='" + actualResults + '\'' +
                ", resultDifferenceAnalysis='" + resultDifferenceAnalysis + '\'' +
                '}';
    }
}