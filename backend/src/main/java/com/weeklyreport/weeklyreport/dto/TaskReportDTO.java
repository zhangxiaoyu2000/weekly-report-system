package com.weeklyreport.weeklyreport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * TaskReportDTO - 日常任务报告数据传输对象
 * 用于返回周报中日常任务的详细信息和执行结果
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskReportDTO {

    private Long taskId;
    private String taskName;
    private String personnelAssignment;
    private String timeline;
    private String quantitativeMetrics;
    private String expectedResults;
    private String actualResults;
    private String resultDifferenceAnalysis;

    // Constructors
    public TaskReportDTO() {}

    public TaskReportDTO(Long taskId, String taskName, String actualResults, String resultDifferenceAnalysis) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.actualResults = actualResults;
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
    }

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getPersonnelAssignment() {
        return personnelAssignment;
    }

    public void setPersonnelAssignment(String personnelAssignment) {
        this.personnelAssignment = personnelAssignment;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getQuantitativeMetrics() {
        return quantitativeMetrics;
    }

    public void setQuantitativeMetrics(String quantitativeMetrics) {
        this.quantitativeMetrics = quantitativeMetrics;
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
        return "TaskReportDTO{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", actualResults='" + actualResults + '\'' +
                ", resultDifferenceAnalysis='" + resultDifferenceAnalysis + '\'' +
                '}';
    }
}