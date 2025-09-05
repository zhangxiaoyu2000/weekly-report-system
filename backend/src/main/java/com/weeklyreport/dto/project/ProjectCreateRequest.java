package com.weeklyreport.dto.project;

import com.weeklyreport.entity.Project;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating new projects
 */
public class ProjectCreateRequest {

    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Project priority cannot be null")
    private Project.ProjectPriority priority;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Budget must be non-negative")
    private BigDecimal budget;

    @Size(max = 200, message = "Tags must not exceed 200 characters")
    private String tags;

    private Boolean isPublic = true;

    private Long departmentId;

    // Constructors
    public ProjectCreateRequest() {}

    public ProjectCreateRequest(String name, String description, Project.ProjectPriority priority) {
        this.name = name;
        this.description = description;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String toString() {
        return "ProjectCreateRequest{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}