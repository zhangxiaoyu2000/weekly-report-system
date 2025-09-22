package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ProjectPhase entity - 严格按照数据库设计.md第142-154行要求重构
 * 
 * 字段映射 (数据库设计.md要求):
 * - phase_name → 任务名称 (VARCHAR(200))
 * - description → 阶段描述 (TEXT)
 * - assigned_members → 负责成员 (VARCHAR(500))
 * - schedule → 时间安排 (VARCHAR(300))
 * - expected_results → 预期结果 (TEXT)
 * - project_id → 关联项目ID (BIGINT)
 * - actual_results → 实际结果 (TEXT)
 * - result_difference_analysis → 结果差异分析 (TEXT)
 * 
 * 删除的字段: phase_description, timeline, estimated_results, status, start_date, end_date, completion_date, phase_order
 */
@Entity
@Table(name = "project_phases", indexes = {
    @Index(name = "idx_project_phase_project", columnList = "project_id"),
    @Index(name = "idx_project_phase_name", columnList = "phase_name")
})
public class ProjectPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                // #任务ID (error3.md要求)

    @NotNull(message = "Project ID cannot be null")
    @Column(name = "project_id", nullable = false)
    private Long projectId;                         // #关联项目ID (error3.md要求)

    @NotBlank(message = "Task name cannot be blank")
    @Size(max = 200, message = "Task name must not exceed 200 characters")
    @Column(name = "phase_name", nullable = false, length = 200)
    private String phaseName;                       // #任务名称

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;                     // #阶段描述

    @Size(max = 500, message = "Assigned members must not exceed 500 characters")
    @Column(name = "assigned_members", length = 500)
    private String assignedMembers;                 // #负责成员

    @Size(max = 300, message = "Schedule must not exceed 300 characters")
    @Column(name = "schedule", length = 300)
    private String schedule;                        // #时间安排

    @Column(name = "expected_results", columnDefinition = "TEXT")
    private String expectedResults;                 // #预期结果

    // actualResults, resultDifferenceAnalysis 已移至关联表 DevTaskReport 中存储

    // 时间戳字段 - 按数据库设计.md第154-155行要求
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ProjectPhase() {}

    public ProjectPhase(Long projectId, String phaseName) {
        this.projectId = projectId;
        this.phaseName = phaseName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedMembers() {
        return assignedMembers;
    }

    public void setAssignedMembers(String assignedMembers) {
        this.assignedMembers = assignedMembers;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    // actualResults, resultDifferenceAnalysis 的 getter/setter 已移除
    // 这些字段现在存储在 DevTaskReport 关联表中

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

    // 兼容性方法 - 支持旧代码
    public Project getProject() {
        return null; // 简化版本中不直接关联Project对象
    }

    public void setProject(Project project) {
        if (project != null) {
            this.projectId = project.getId();
        }
    }

    public String getPhaseDescription() {
        return description; // 映射到新的description字段
    }

    public void setPhaseDescription(String phaseDescription) {
        this.description = phaseDescription;
    }

    public String getTimeline() {
        return schedule; // 映射到新的schedule字段
    }

    public void setTimeline(String timeline) {
        this.schedule = timeline;
    }

    public String getEstimatedResults() {
        return expectedResults; // 映射到新的expectedResults字段
    }

    public void setEstimatedResults(String estimatedResults) {
        this.expectedResults = estimatedResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectPhase)) return false;
        ProjectPhase that = (ProjectPhase) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProjectPhase{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", phaseName='" + phaseName + '\'' +
                '}';
    }
}