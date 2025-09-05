package com.weeklyreport.dto.project;

import com.weeklyreport.entity.Project;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;

/**
 * DTO for project filtering and search parameters
 */
public class ProjectFilterRequest {

    private String name;
    private String description;
    private Project.ProjectStatus status;
    private Project.ProjectPriority priority;
    private Long createdById;
    private Long departmentId;
    private Boolean isPublic;
    private Boolean archived;
    private String tags;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
    
    // Pagination parameters
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;
    
    // Sorting parameters
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    // Constructors
    public ProjectFilterRequest() {}

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

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDate getStartDateFrom() {
        return startDateFrom;
    }

    public void setStartDateFrom(LocalDate startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public LocalDate getStartDateTo() {
        return startDateTo;
    }

    public void setStartDateTo(LocalDate startDateTo) {
        this.startDateTo = startDateTo;
    }

    public LocalDate getEndDateFrom() {
        return endDateFrom;
    }

    public void setEndDateFrom(LocalDate endDateFrom) {
        this.endDateFrom = endDateFrom;
    }

    public LocalDate getEndDateTo() {
        return endDateTo;
    }

    public void setEndDateTo(LocalDate endDateTo) {
        this.endDateTo = endDateTo;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // Helper methods
    public boolean hasFilters() {
        return name != null || description != null || status != null || priority != null ||
               createdById != null || departmentId != null || isPublic != null || archived != null ||
               tags != null || startDateFrom != null || startDateTo != null ||
               endDateFrom != null || endDateTo != null;
    }

    public boolean isValidSortDirection() {
        return "ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection);
    }

    @Override
    public String toString() {
        return "ProjectFilterRequest{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}