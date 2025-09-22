package com.weeklyreport.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DevTaskReport entity - 发展任务与周报关联表
 * 严格按照数据库设计.md V28迁移后的结构实现
 * 
 * 对应V28迁移后的dev_task_reports表设计：
 * dev_task_reports:{
 *     #id 主键 (AUTO_INCREMENT)
 *     #weekly_report_id 外键
 *     #project_id 外键
 *     #phases_id 外键 (关联project_phases表)
 *     #actual_results 实际结果
 *     #result_difference_analysis 结果差异分析
 *     #created_at 创建时间
 * }
 */
@Entity
@Table(name = "dev_task_reports", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_dev_task_reports_unique", 
           columnNames = {"weekly_report_id", "project_id", "phases_id", "is_week"}))
public class DevTaskReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weekly_report_id", nullable = false)
    private Long weeklyReportId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "phases_id", nullable = false)
    private Long phasesId;  // 修正字段名，对应project_phases.id

    // 新增字段：实际结果和结果差异分析
    @Column(name = "actual_results", columnDefinition = "TEXT")
    private String actualResults;

    @Column(name = "result_difference_analysis", columnDefinition = "TEXT")
    private String resultDifferenceAnalysis;

    // 新增字段：区分本周汇报和下周规划
    @Column(name = "is_week", nullable = false)
    private Boolean isWeek = true;  // true=本周汇报, false=下周规划

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 关联关系 - 使用insertable=false, updatable=false避免重复更新
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_report_id", insertable = false, updatable = false)
    private WeeklyReport weeklyReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phases_id", insertable = false, updatable = false)
    private ProjectPhase projectPhase;

    // Constructors
    public DevTaskReport() {}

    public DevTaskReport(Long weeklyReportId, Long projectId, Long phasesId) {
        this.weeklyReportId = weeklyReportId;
        this.projectId = projectId;
        this.phasesId = phasesId;
    }

    public DevTaskReport(Long weeklyReportId, Long projectId, Long phasesId, 
                        String actualResults, String resultDifferenceAnalysis) {
        this.weeklyReportId = weeklyReportId;
        this.projectId = projectId;
        this.phasesId = phasesId;
        this.actualResults = actualResults;
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
        this.isWeek = true; // 默认为本周汇报
    }

    public DevTaskReport(Long weeklyReportId, Long projectId, Long phasesId, 
                        String actualResults, String resultDifferenceAnalysis, Boolean isWeek) {
        this.weeklyReportId = weeklyReportId;
        this.projectId = projectId;
        this.phasesId = phasesId;
        this.actualResults = actualResults;
        this.resultDifferenceAnalysis = resultDifferenceAnalysis;
        this.isWeek = isWeek;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getPhasesId() {
        return phasesId;
    }

    public void setPhasesId(Long phasesId) {
        this.phasesId = phasesId;
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

    public WeeklyReport getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(WeeklyReport weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ProjectPhase getProjectPhase() {
        return projectPhase;
    }

    public void setProjectPhase(ProjectPhase projectPhase) {
        this.projectPhase = projectPhase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DevTaskReport)) return false;
        DevTaskReport that = (DevTaskReport) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DevTaskReport{" +
                "id=" + id +
                ", weeklyReportId=" + weeklyReportId +
                ", projectId=" + projectId +
                ", phasesId=" + phasesId +
                ", createdAt=" + createdAt +
                '}';
    }
}