package com.weeklyreport.dto.project;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * DTO for creating new projects - 严格按照数据库设计.md和Project.java要求
 */
public class ProjectCreateRequest {

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String name;                            // #项目名称

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;                     // #项目内容

    @Size(max = 5000, message = "Members must not exceed 5000 characters")
    private String members;                         // #项目成员

    @Size(max = 5000, message = "Expected results must not exceed 5000 characters")
    private String expectedResults;                 // #预期结果

    @Size(max = 5000, message = "Timeline must not exceed 5000 characters")
    private String timeline;                        // #时间线

    @Size(max = 5000, message = "Stop loss must not exceed 5000 characters")
    private String stopLoss;                        // #止损点

    @Valid
    private List<ProjectPhaseCreateRequest> phases; // #阶段性任务列表

    // Constructors
    public ProjectCreateRequest() {}

    public ProjectCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }

    public List<ProjectPhaseCreateRequest> getPhases() {
        return phases;
    }

    public void setPhases(List<ProjectPhaseCreateRequest> phases) {
        this.phases = phases;
    }

    @Override
    public String toString() {
        return "ProjectCreateRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", phases=" + (phases != null ? phases.size() : 0) + " phases" +
                '}';
    }
}