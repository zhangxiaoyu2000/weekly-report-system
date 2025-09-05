package com.weeklyreport.dto.project;

import com.weeklyreport.entity.Project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for project responses
 */
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Project.ProjectStatus status;
    private Project.ProjectPriority priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private Integer progress;
    private String tags;
    private Boolean isPublic;
    private Boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Creator information
    private Long createdById;
    private String createdByName;
    
    // Department information
    private Long departmentId;
    private String departmentName;

    // Constructors
    public ProjectResponse() {}

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus();
        this.priority = project.getPriority();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.budget = project.getBudget();
        this.progress = project.getProgress();
        this.tags = project.getTags();
        this.isPublic = project.getIsPublic();
        this.archived = project.getArchived();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
        
        if (project.getCreatedBy() != null) {
            this.createdById = project.getCreatedBy().getId();
            this.createdByName = project.getCreatedBy().getFullName();
        }
        
        if (project.getDepartment() != null) {
            this.departmentId = project.getDepartment().getId();
            this.departmentName = project.getDepartment().getName();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // Helper methods
    public boolean isOverdue() {
        return endDate != null && LocalDate.now().isAfter(endDate) 
               && status != Project.ProjectStatus.COMPLETED && status != Project.ProjectStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == Project.ProjectStatus.ACTIVE || status == Project.ProjectStatus.PLANNING;
    }

    @Override
    public String toString() {
        return "ProjectResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", progress=" + progress +
                '}';
    }
}