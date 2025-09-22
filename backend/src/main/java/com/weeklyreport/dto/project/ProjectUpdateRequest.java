package com.weeklyreport.dto.project;

import com.weeklyreport.entity.Project;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for updating existing projects
 */
public class ProjectUpdateRequest {

    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 5000, message = "Members must not exceed 5000 characters")
    private String members;                         // #项目成员

    @Size(max = 5000, message = "Expected results must not exceed 5000 characters")
    private String expectedResults;                 // #预期结果

    @Size(max = 5000, message = "Timeline must not exceed 5000 characters")
    private String timeline;                        // #时间线

    @Size(max = 5000, message = "Stop loss must not exceed 5000 characters")
    private String stopLoss;                        // #止损点

    private Project.ProjectStatus status;

    private Project.ProjectPriority priority;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget must be non-negative")
    private BigDecimal budget;

    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100%")
    private Integer progress;

    @Size(max = 200, message = "Tags must not exceed 200 characters")
    private String tags;

    private Boolean isPublic;

    private Boolean archived;

    private Long departmentId;

    @Valid
    private List<ProjectPhaseCreateRequest> projectPhases;

    // Constructors
    public ProjectUpdateRequest() {}

    public ProjectUpdateRequest(String name, String description, Project.ProjectStatus status, Project.ProjectPriority priority) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
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

    public Project.ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(Project.ProjectStatus status) {
        this.status = status;
    }

    public Project.ProjectPriority getPriority() {
        return priority;
    }

    public void setPriority(Project.ProjectPriority priority) {
        this.priority = priority;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public List<ProjectPhaseCreateRequest> getProjectPhases() {
        return projectPhases;
    }

    public void setProjectPhases(List<ProjectPhaseCreateRequest> projectPhases) {
        this.projectPhases = projectPhases;
    }

    @Override
    public String toString() {
        return "ProjectUpdateRequest{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", progress=" + progress +
                '}';
    }
}