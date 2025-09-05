package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Template entity representing weekly report templates
 */
@Entity
@Table(name = "templates", indexes = {
    @Index(name = "idx_template_department", columnList = "department_id"),
    @Index(name = "idx_template_name", columnList = "name"),
    @Index(name = "idx_template_status", columnList = "status")
})
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Template name cannot be blank")
    @Size(max = 100, message = "Template name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Template type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TemplateType type = TemplateType.DEPARTMENT;

    @NotNull(message = "Template status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TemplateStatus status = TemplateStatus.ACTIVE;

    @NotNull(message = "Template content cannot be null")
    @Column(name = "content", nullable = false, columnDefinition = "JSON")
    private String content; // JSON格式存储模板配置

    @Column(name = "default_title", length = 200)
    private String defaultTitle; // 默认标题模板

    @NotNull(message = "Version cannot be null")
    @Min(value = 1, message = "Version must be at least 1")
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order must be non-negative")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_default")
    private Boolean isDefault = false; // 是否为默认模板

    @Column(name = "is_required")
    private Boolean isRequired = false; // 是否必填

    @Column(name = "usage_count")
    private Integer usageCount = 0; // 使用次数统计

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-One relationship with Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // Many-to-One relationship with User (Creator)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // One-to-Many relationship with WeeklyReport
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    private Set<WeeklyReport> weeklyReports = new HashSet<>();

    // Template type enum
    public enum TemplateType {
        SYSTEM,         // 系统模板（全局）
        DEPARTMENT,     // 部门模板
        PERSONAL        // 个人模板
    }

    // Template status enum
    public enum TemplateStatus {
        ACTIVE,         // 激活状态
        INACTIVE,       // 非激活状态
        DEPRECATED,     // 已弃用
        DRAFT           // 草稿状态
    }

    // Constructors
    public Template() {}

    public Template(String name, String content, TemplateType type) {
        this.name = name;
        this.content = content;
        this.type = type;
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

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public void setStatus(TemplateStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Set<WeeklyReport> getWeeklyReports() {
        return weeklyReports;
    }

    public void setWeeklyReports(Set<WeeklyReport> weeklyReports) {
        this.weeklyReports = weeklyReports;
    }

    // Utility methods for managing relationships
    public void addWeeklyReport(WeeklyReport report) {
        weeklyReports.add(report);
        report.setTemplate(this);
        incrementUsageCount();
    }

    public void removeWeeklyReport(WeeklyReport report) {
        weeklyReports.remove(report);
        report.setTemplate(null);
    }

    // Business logic methods
    public void incrementUsageCount() {
        if (this.usageCount == null) {
            this.usageCount = 1;
        } else {
            this.usageCount++;
        }
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    public void activate() {
        this.status = TemplateStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = TemplateStatus.INACTIVE;
    }

    public void deprecate() {
        this.status = TemplateStatus.DEPRECATED;
    }

    public Template createNewVersion() {
        Template newTemplate = new Template();
        newTemplate.setName(this.name);
        newTemplate.setDescription(this.description);
        newTemplate.setType(this.type);
        newTemplate.setContent(this.content);
        newTemplate.setDefaultTitle(this.defaultTitle);
        newTemplate.setVersion(this.version + 1);
        newTemplate.setSortOrder(this.sortOrder);
        newTemplate.setDepartment(this.department);
        newTemplate.setCreatedBy(this.createdBy);
        newTemplate.setStatus(TemplateStatus.DRAFT);
        return newTemplate;
    }

    // Check if template is editable
    public boolean isEditable() {
        return status == TemplateStatus.DRAFT || status == TemplateStatus.ACTIVE;
    }

    // Check if template is usable for creating reports
    public boolean isUsable() {
        return status == TemplateStatus.ACTIVE;
    }

    // Get template scope description
    public String getScopeDescription() {
        switch (type) {
            case SYSTEM:
                return "System-wide template";
            case DEPARTMENT:
                return department != null ? 
                    "Department template for " + department.getName() : 
                    "Department template";
            case PERSONAL:
                return createdBy != null ? 
                    "Personal template by " + createdBy.getFullName() : 
                    "Personal template";
            default:
                return "Unknown scope";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template)) return false;
        Template template = (Template) o;
        return id != null && id.equals(template.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", version=" + version +
                '}';
    }
}