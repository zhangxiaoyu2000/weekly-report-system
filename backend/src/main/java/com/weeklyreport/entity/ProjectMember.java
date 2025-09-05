package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ProjectMember entity representing the many-to-many relationship between Project and User
 * with additional role information
 */
@Entity
@Table(name = "project_members", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_project_member", 
           columnNames = {"project_id", "user_id"}
       ),
       indexes = {
           @Index(name = "idx_project_member_project", columnList = "project_id"),
           @Index(name = "idx_project_member_user", columnList = "user_id"),
           @Index(name = "idx_project_member_role", columnList = "role"),
           @Index(name = "idx_project_member_status", columnList = "status")
       })
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Role cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private ProjectRole role = ProjectRole.MEMBER;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "joined_date", nullable = false, updatable = false)
    private LocalDateTime joinedDate;

    @Column(name = "left_date")
    private LocalDateTime leftDate;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "invited_by")
    private Long invitedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    // Project role enum
    public enum ProjectRole {
        PROJECT_MANAGER,    // 项目经理 - 完全管理权限
        TECH_LEAD,         // 技术负责人 - 技术决策权限
        DEVELOPER,         // 开发人员 - 开发权限
        TESTER,           // 测试人员 - 测试权限
        DESIGNER,         // 设计师 - 设计权限
        ANALYST,          // 业务分析师 - 分析权限
        MEMBER,           // 普通成员 - 基本权限
        OBSERVER          // 观察者 - 只读权限
    }

    // Member status enum
    public enum MemberStatus {
        ACTIVE,           // 活跃成员
        INACTIVE,         // 非活跃成员
        INVITED,          // 已邀请待确认
        REMOVED           // 已移除
    }

    // Constructors
    public ProjectMember() {}

    public ProjectMember(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    public ProjectMember(Project project, User user, ProjectRole role, Long invitedBy) {
        this.project = project;
        this.user = user;
        this.role = role;
        this.invitedBy = invitedBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    public LocalDateTime getLeftDate() {
        return leftDate;
    }

    public void setLeftDate(LocalDateTime leftDate) {
        this.leftDate = leftDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility methods for role checking
    public boolean hasManagementPermission() {
        return role == ProjectRole.PROJECT_MANAGER;
    }

    public boolean hasWritePermission() {
        return role != ProjectRole.OBSERVER && status == MemberStatus.ACTIVE;
    }

    public boolean hasReadPermission() {
        return status == MemberStatus.ACTIVE || status == MemberStatus.INACTIVE;
    }

    public boolean canManageMembers() {
        return role == ProjectRole.PROJECT_MANAGER || role == ProjectRole.TECH_LEAD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectMember)) return false;
        ProjectMember that = (ProjectMember) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProjectMember{" +
                "id=" + id +
                ", role=" + role +
                ", status=" + status +
                ", joinedDate=" + joinedDate +
                '}';
    }
}