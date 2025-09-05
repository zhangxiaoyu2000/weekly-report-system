package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Project entity representing projects in the system
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_name", columnList = "name"),
    @Index(name = "idx_project_status", columnList = "status"),
    @Index(name = "idx_project_created_by", columnList = "created_by"),
    @Index(name = "idx_project_department", columnList = "department_id"),
    @Index(name = "idx_project_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_project_deleted_at", columnList = "deleted_at")
})
@SQLDelete(sql = "UPDATE projects SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Project status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @NotNull(message = "Project priority cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100%")
    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    @Size(max = 200, message = "Tags must not exceed 200 characters")
    @Column(name = "tags", length = 200)
    private String tags;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "archived", nullable = false)
    private Boolean archived = false;

    // Relationships
    @NotNull(message = "Project creator cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // One-to-Many relationship with WeeklyReport
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<WeeklyReport> weeklyReports = new HashSet<>();

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Project status enum
    public enum ProjectStatus {
        PLANNING,       // 规划中
        ACTIVE,         // 进行中
        ON_HOLD,        // 暂停
        COMPLETED,      // 已完成
        CANCELLED       // 已取消
    }

    // Project priority enum
    public enum ProjectPriority {
        LOW,            // 低优先级
        MEDIUM,         // 中优先级
        HIGH,           // 高优先级
        URGENT          // 紧急
    }

    // Constructors
    public Project() {}

    public Project(String name, String description, User createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public ProjectPriority getPriority() {
        return priority;
    }

    public void setPriority(ProjectPriority priority) {
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<WeeklyReport> getWeeklyReports() {
        return weeklyReports;
    }

    public void setWeeklyReports(Set<WeeklyReport> weeklyReports) {
        this.weeklyReports = weeklyReports;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Utility methods
    public void addWeeklyReport(WeeklyReport report) {
        weeklyReports.add(report);
        report.setProject(this);
    }

    public void removeWeeklyReport(WeeklyReport report) {
        weeklyReports.remove(report);
        report.setProject(null);
    }

    public boolean isOverdue() {
        return endDate != null && LocalDate.now().isAfter(endDate) 
               && status != ProjectStatus.COMPLETED && status != ProjectStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == ProjectStatus.ACTIVE || status == ProjectStatus.PLANNING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", progress=" + progress +
                '}';
    }
}