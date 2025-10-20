package com.weeklyreport.task.entity;

import com.weeklyreport.weeklyreport.entity.WeeklyReport;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TaskReport entity - 日常任务与周报关联表
 * 严格按照数据库设计.md第212-235行和后端修改指导.md第181-250行实现
 * 
 * 对应error3.md第153-156行的task_reports表设计：
 * task_reports:{
 *     #周报ID 主键
 *     #任务ID 主键
 *     #实际结果
 *     #结果差异分析
 * }
 */
@Entity
@Table(name = "task_reports")
public class TaskReport {

    @EmbeddedId
    private TaskReportId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("weeklyReportId")
    @JoinColumn(name = "weekly_report_id")
    private WeeklyReport weeklyReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    // 新增字段：实际结果和结果差异分析
    @Column(name = "actual_results", columnDefinition = "TEXT")
    private String actualResults;

    @Column(name = "result_difference_analysis", columnDefinition = "TEXT")
    private String resultDifferenceAnalysis;

    // 新增字段：区分本周汇报和下周规划
    @Column(name = "is_week", nullable = false, insertable = false, updatable = false)
    private Boolean isWeek = true;  // true=本周汇报, false=下周规划

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public TaskReport() {}

    public TaskReport(WeeklyReport weeklyReport, Task task) {
        this.weeklyReport = weeklyReport;
        this.task = task;
        this.isWeek = true; // 默认为本周汇报
        this.id = new TaskReportId(weeklyReport.getId(), task.getId(), this.isWeek);
    }

    public TaskReport(WeeklyReport weeklyReport, Task task, String actualResults, String resultDifferenceAnalysis) {
        this.weeklyReport = weeklyReport;
        this.task = task;
        this.actualResults = actualResults;
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
        this.isWeek = true; // 默认为本周汇报
        this.id = new TaskReportId(weeklyReport.getId(), task.getId(), this.isWeek);
    }

    public TaskReport(WeeklyReport weeklyReport, Task task, String actualResults, String resultDifferenceAnalysis, Boolean isWeek) {
        this.weeklyReport = weeklyReport;
        this.task = task;
        this.actualResults = actualResults;
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
        this.isWeek = isWeek;
        this.id = new TaskReportId(weeklyReport.getId(), task.getId(), this.isWeek);
    }

    // Getters and Setters
    public TaskReportId getId() {
        return id;
    }

    public void setId(TaskReportId id) {
        this.id = id;
    }

    public WeeklyReport getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(WeeklyReport weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getActualResults() {
        return actualResults;
    }

    public void setActualResults(String actualResults) {
        this.actualResults = actualResults;
    }

    public String getResultDifferenceAnalysis() {
        return resultDifferenceAnalysis;
    }

    public void setResultDifferenceAnalysis(String resultDifferenceAnalysis) {
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
    }

    public Boolean getIsWeek() {
        return isWeek;
    }

    public void setIsWeek(Boolean isWeek) {
        this.isWeek = isWeek;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskReport)) return false;
        TaskReport that = (TaskReport) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TaskReport{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 复合主键类 - 周报ID + 任务ID + 是否本周（区分本周汇报和下周规划）
     * 修复主键约束冲突：同一任务可以同时出现在本周汇报和下周规划中
     */
    @Embeddable
    public static class TaskReportId implements Serializable {
        
        @Column(name = "weekly_report_id")
        private Long weeklyReportId;    // #周报ID 主键

        @Column(name = "task_id")
        private Long taskId;            // #任务ID 主键

        @Column(name = "is_week")
        private Boolean isWeek;         // #是否本周 主键 - 区分本周汇报(true)和下周规划(false)

        // Constructors
        public TaskReportId() {}

        public TaskReportId(Long weeklyReportId, Long taskId) {
            this.weeklyReportId = weeklyReportId;
            this.taskId = taskId;
            this.isWeek = true; // 默认为本周汇报
        }

        public TaskReportId(Long weeklyReportId, Long taskId, Boolean isWeek) {
            this.weeklyReportId = weeklyReportId;
            this.taskId = taskId;
            this.isWeek = isWeek;
        }

        // Getters and Setters
        public Long getWeeklyReportId() {
            return weeklyReportId;
        }

        public void setWeeklyReportId(Long weeklyReportId) {
            this.weeklyReportId = weeklyReportId;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public Boolean getIsWeek() {
            return isWeek;
        }

        public void setIsWeek(Boolean isWeek) {
            this.isWeek = isWeek;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TaskReportId)) return false;
            TaskReportId that = (TaskReportId) o;
            return Objects.equals(weeklyReportId, that.weeklyReportId) &&
                   Objects.equals(taskId, that.taskId) &&
                   Objects.equals(isWeek, that.isWeek);
        }

        @Override
        public int hashCode() {
            return Objects.hash(weeklyReportId, taskId, isWeek);
        }

        @Override
        public String toString() {
            return "TaskReportId{" +
                    "weeklyReportId=" + weeklyReportId +
                    ", taskId=" + taskId +
                    ", isWeek=" + isWeek +
                    '}';
        }
    }
}