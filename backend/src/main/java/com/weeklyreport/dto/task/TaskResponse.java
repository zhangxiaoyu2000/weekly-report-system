package com.weeklyreport.dto.task;

import com.weeklyreport.entity.Task;
import java.time.LocalDateTime;

/**
 * Response DTO for task - 严格按照Task.java设计
 */
public class TaskResponse {

    private Long id;                                // #任务ID
    private String taskName;                        // #任务名称
    private String personnelAssignment;             // #人员分配
    private String timeline;                        // #时间线
    private String quantitativeMetrics;             // #量化指标
    private String expectedResults;                 // #预期结果
    private String actualResults;                   // #实际结果
    private String resultDifferenceAnalysis;        // #结果差异分析
    private Task.TaskType taskType;                 // 任务类型
    private Long createdBy;                         // 创建者ID
    private LocalDateTime createdAt;                // 创建时间
    private LocalDateTime updatedAt;                // 更新时间
    
    // Derived fields
    private Integer progress;                       // 进度百分比
    private boolean completed;                      // 是否完成
    private String reportSection;                   // 报告部分 (THIS_WEEK_REPORT/NEXT_WEEK_PLAN)
    private String taskTypeString;                  // 任务类型字符串 (ROUTINE/DEVELOPMENT)
    private Long projectId;                         // 项目ID (发展性任务)
    private Long projectPhaseId;                    // 项目阶段ID (发展性任务)

    // Constructors
    public TaskResponse() {}

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.taskName = task.getTaskName();
        this.personnelAssignment = task.getPersonnelAssignment();
        this.timeline = task.getTimeline();
        this.quantitativeMetrics = task.getQuantitativeMetrics();
        this.expectedResults = task.getExpectedResults();
        this.actualResults = null; // 已移至DevTaskReport表
        this.resultDifferenceAnalysis = null; // 已移至DevTaskReport表
        this.taskType = null; // TaskType字段已删除
        this.createdBy = task.getCreatedBy();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        
        // Derived fields
        this.progress = task.getProgress();
        this.completed = task.isCompleted();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Task.TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(Task.TaskType taskType) {
        this.taskType = taskType;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getReportSection() {
        return reportSection;
    }

    public void setReportSection(String reportSection) {
        this.reportSection = reportSection;
    }

    public String getTaskTypeString() {
        return taskTypeString;
    }

    public void setTaskTypeString(String taskTypeString) {
        this.taskTypeString = taskTypeString;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getProjectPhaseId() {
        return projectPhaseId;
    }

    public void setProjectPhaseId(Long projectPhaseId) {
        this.projectPhaseId = projectPhaseId;
    }

    // Helper methods
    public String getTaskTypeName() {
        if (taskType == null) return null;
        switch (taskType) {
            case DAILY: return "日常性任务";
            case WEEKLY: return "周期性任务";
            case MONTHLY: return "月度任务";
            default: return taskType.name();
        }
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", taskType=" + taskType +
                ", progress=" + progress +
                ", completed=" + completed +
                '}';
    }
}