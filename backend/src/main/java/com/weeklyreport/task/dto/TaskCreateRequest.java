package com.weeklyreport.task.dto;

import com.weeklyreport.task.entity.Task;
import jakarta.validation.constraints.*;

/**
 * DTO for creating new tasks - 严格按照Task.java设计
 */
public class TaskCreateRequest {

    @NotBlank(message = "Task name cannot be blank")
    private String taskName;                        // #任务名称

    private String personnelAssignment;             // #人员分配

    private String timeline;                        // #时间线


    private String expectedResults;                 // #预期结果

    // 创建者ID将由控制器自动设置，不需要客户端提供
    private Long createdBy;                         // 创建者ID

    // Constructors
    public TaskCreateRequest() {}

    public TaskCreateRequest(String taskName, Long createdBy) {
        this.taskName = taskName;
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


    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
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
                ", createdBy=" + createdBy +
                '}';
    }
}
