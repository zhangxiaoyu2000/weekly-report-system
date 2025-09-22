package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Task entity - 严格按照数据库设计.md第142-170行和error3.md要求重构
 * 
 * 删除的字段 (数据库设计.md 第161-169行)：
 * - stop_loss_point → 非日常任务字段
 * - progress, start_date, due_date, completion_date → 简化管理
 * - budget → 非日常任务字段
 * - is_completed, is_overdue → 通过actual_results判断
 * - priority → error3.md明确删除
 * - project_id, simple_project_id, project_phase_id, weekly_report_id → 使用关联表
 * - report_section → 使用关联表区分
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_created_by", columnList = "created_by")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                // #任务ID (error3.md要求)

    @NotBlank(message = "Task name cannot be blank")
    @Size(max = 200, message = "Task name must not exceed 200 characters")
    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;                        // #任务名称

    @Size(max = 100, message = "Personnel assignment must not exceed 100 characters")
    @Column(name = "personnel_assignment", length = 100)
    private String personnelAssignment;             // #人员分配

    @Size(max = 200, message = "Timeline must not exceed 200 characters")
    @Column(name = "timeline", length = 200)
    private String timeline;                        // #时间线

    @Size(max = 300, message = "Quantitative metrics must not exceed 300 characters")
    @Column(name = "quantitative_metrics", length = 300)
    private String quantitativeMetrics;             // #量化指标

    @Size(max = 500, message = "Expected results must not exceed 500 characters")
    @Column(name = "expected_results", length = 500)
    private String expectedResults;                 // #预期结果

    // actualResults, resultDifferenceAnalysis, taskType 已移至关联表 TaskReport 中存储

    @NotNull(message = "Creator cannot be null")
    @Column(name = "created_by", nullable = false)
    private Long createdBy;                         // 创建者ID

    // 移除项目阶段关联 - 按数据库设计.md简化设计，Task表不再直接关联项目阶段

    // 时间戳字段
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // 任务类型枚举
    public enum TaskType {
        DAILY,          // 日常性任务
        WEEKLY,         // 周期性任务
        MONTHLY,        // 月度任务
        ROUTINE,        // 日常性任务(与前端匹配)
        DEVELOPMENT     // 发展性任务(与前端匹配)
    }

    // 兼容性枚举 - ReportSection
    public enum ReportSection {
        THIS_WEEK,      // 本周汇报
        NEXT_WEEK,      // 下周规划
        ROUTINE,        // 日常任务
        DEVELOPMENT     // 发展任务
    }

    // Constructors
    public Task() {}

    public Task(String taskName, Long createdBy) {
        this.taskName = taskName;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getPersonnelAssignment() {
        return personnelAssignment;
    }

    public void setPersonnelAssignment(String personnelAssignment) {
        this.personnelAssignment = personnelAssignment;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getQuantitativeMetrics() {
        return quantitativeMetrics;
    }

    public void setQuantitativeMetrics(String quantitativeMetrics) {
        this.quantitativeMetrics = quantitativeMetrics;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    // actualResults, resultDifferenceAnalysis, taskType 的 getter/setter 已移除
    // 这些字段现在存储在 TaskReport 关联表中

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    // ProjectPhase关联已移除 - 按数据库设计.md简化设计

    // Business logic methods
    public boolean isCompleted() {
        // 任务完成状态现在通过 TaskReport 关联表判断
        return expectedResults != null && !expectedResults.trim().isEmpty();
    }

    // 添加核心业务方法
    public void updateOverdueStatus() {
        // 在简化版本中不需要复杂的过期逻辑
        // 可以基于actualResults判断是否完成
    }

    public void setProgress(Integer progress) {
        // 在简化版本中不支持progress字段
        // 通过actualResults的填写情况判断进度
    }

    public Integer getProgress() {
        // 基于完成度百分比返回进度
        return getCompletionPercentage();
    }

    public int getCompletionPercentage() {
        int totalFields = 5; // taskName, personnelAssignment, timeline, quantitativeMetrics, expectedResults
        int filledFields = 0;

        if (taskName != null && !taskName.trim().isEmpty()) filledFields++;
        if (personnelAssignment != null && !personnelAssignment.trim().isEmpty()) filledFields++;
        if (timeline != null && !timeline.trim().isEmpty()) filledFields++;
        if (quantitativeMetrics != null && !quantitativeMetrics.trim().isEmpty()) filledFields++;
        if (expectedResults != null && !expectedResults.trim().isEmpty()) filledFields++;

        return (filledFields * 100) / totalFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", createdBy=" + createdBy +
                '}';
    }
}