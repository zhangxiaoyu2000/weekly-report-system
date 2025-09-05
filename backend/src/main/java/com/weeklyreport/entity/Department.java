package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Department entity representing organizational structure
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_department_code", columnList = "code"),
    @Index(name = "idx_department_parent", columnList = "parent_id")
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Department name cannot be blank")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Department code cannot be blank")
    @Size(max = 20, message = "Department code must not exceed 20 characters")
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Department level cannot be null")
    @Min(value = 1, message = "Department level must be at least 1")
    @Column(name = "level", nullable = false)
    private Integer level;

    @Size(max = 1000, message = "Path must not exceed 1000 characters")
    @Column(name = "path", length = 1000)
    private String path; // 层级路径，如：/1/2/3

    @NotNull(message = "Sort order cannot be null")
    @Min(value = 0, message = "Sort order must be non-negative")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    @Email(message = "Contact email should be valid")
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Self-referencing Many-to-One relationship (Parent Department)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    // Self-referencing One-to-Many relationship (Child Departments)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private Set<Department> children = new HashSet<>();

    // One-to-Many relationship with User
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    // One-to-Many relationship with Template
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Template> templates = new HashSet<>();

    // Department status enum
    public enum DepartmentStatus {
        ACTIVE,         // 活跃状态
        INACTIVE,       // 非活跃状态
        MERGED,         // 已合并
        DISSOLVED       // 已解散
    }

    // Constructors
    public Department() {}

    public Department(String name, String code, Integer level) {
        this.name = name;
        this.code = code;
        this.level = level;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public DepartmentStatus getStatus() {
        return status;
    }

    public void setStatus(DepartmentStatus status) {
        this.status = status;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public Department getParent() {
        return parent;
    }

    public void setParent(Department parent) {
        this.parent = parent;
        updateLevelAndPath();
    }

    public Set<Department> getChildren() {
        return children;
    }

    public void setChildren(Set<Department> children) {
        this.children = children;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<Template> templates) {
        this.templates = templates;
    }

    // Utility methods for managing hierarchical relationships
    public void addChild(Department child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Department child) {
        children.remove(child);
        child.setParent(null);
    }

    public void addUser(User user) {
        users.add(user);
        user.setDepartment(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setDepartment(null);
    }

    public void addTemplate(Template template) {
        templates.add(template);
        template.setDepartment(this);
    }

    public void removeTemplate(Template template) {
        templates.remove(template);
        template.setDepartment(null);
    }

    // Utility method to update level and path based on parent
    public void updateLevelAndPath() {
        if (parent == null) {
            this.level = 1;
            this.path = "/" + this.id;
        } else {
            this.level = parent.getLevel() + 1;
            this.path = parent.getPath() + "/" + this.id;
        }
    }

    // Check if this department is a root department
    public boolean isRoot() {
        return parent == null;
    }

    // Check if this department is a leaf department
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // Get all descendant departments
    public Set<Department> getAllDescendants() {
        Set<Department> descendants = new HashSet<>();
        for (Department child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    // Get the root department of this hierarchy
    public Department getRoot() {
        Department root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department)) return false;
        Department that = (Department) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", level=" + level +
                ", status=" + status +
                '}';
    }
}