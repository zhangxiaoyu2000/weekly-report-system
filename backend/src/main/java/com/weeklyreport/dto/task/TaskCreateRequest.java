package com.weeklyreport.dto.task;

import com.weeklyreport.entity.Task;
import jakarta.validation.constraints.*;

/**
 * DTO for creating new tasks - 严格按照Task.java设计
 */
public class TaskCreateRequest {

    @NotBlank(message = "Task name cannot be blank")
    @Size(max = 200, message = "Task name must not exceed 200 characters")
    private String taskName;                        // #任务名称

    @Size(max = 100, message = "Personnel assignment must not exceed 100 characters")
    private String personnelAssignment;             // #人员分配

    @Size(max = 200, message = "Timeline must not exceed 200 characters")
    private String timeline;                        // #时间线

    @Size(max = 300, message = "Quantitative metrics must not exceed 300 characters")
    private String quantitativeMetrics;             // #量化指标

    @Size(max = 500, message = "Expected results must not exceed 500 characters")
    private String expectedResults;                 // #预期结果

    @NotNull(message = "Task type cannot be null")
    private Task.TaskType taskType;                 // 任务类型

    // 创建者ID将由控制器自动设置，不需要客户端提供
    private Long createdBy;                         // 创建者ID

    // Constructors
    public TaskCreateRequest() {}

    public TaskCreateRequest(String taskName, Task.TaskType taskType, Long createdBy) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.createdBy = createdBy;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "TaskCreateRequest{" +
                "taskName='" + taskName + '\'' +
                ", taskType=" + taskType +
                ", createdBy=" + createdBy +
                '}';
    }
}