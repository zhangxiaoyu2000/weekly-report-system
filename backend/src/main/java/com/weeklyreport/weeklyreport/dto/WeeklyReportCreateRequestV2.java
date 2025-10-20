package com.weeklyreport.weeklyreport.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

/**
 * DTO for creating new weekly reports - V2 Structure
 * Based on error3.md requirements for structured task references
 */
public class WeeklyReportCreateRequestV2 {

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title; // 周报标题

    @NotBlank(message = "Report week cannot be blank")
    @Size(max = 50, message = "Report week must not exceed 50 characters")
    private String reportWeek; // 周报日期 format: "几月第几周（周几）"

    @Valid
    @NotNull(message = "Content cannot be null")
    private WeeklyReportTaskReference.WeeklyReportContent content; // 结构化内容

    @Size(max = 1000, message = "Additional notes must not exceed 1000 characters")
    private String additionalNotes; // 其他备注

    @Size(max = 2000, message = "Development opportunities must not exceed 2000 characters")
    private String developmentOpportunities; // 可发展性清单

    // Status field to support draft/submitted states
    private String status;

    // Constructors
    public WeeklyReportCreateRequestV2() {
        this.content = new WeeklyReportTaskReference.WeeklyReportContent();
    }

    public WeeklyReportCreateRequestV2(String title, String reportWeek) {
        this.title = title;
        this.reportWeek = reportWeek;
        this.content = new WeeklyReportTaskReference.WeeklyReportContent();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReportWeek() {
        return reportWeek;
    }

    public void setReportWeek(String reportWeek) {
        this.reportWeek = reportWeek;
    }

    public WeeklyReportTaskReference.WeeklyReportContent getContent() {
        return content;
    }

    public void setContent(WeeklyReportTaskReference.WeeklyReportContent content) {
        this.content = content != null ? content : new WeeklyReportTaskReference.WeeklyReportContent();
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getDevelopmentOpportunities() {
        return developmentOpportunities;
    }

    public void setDevelopmentOpportunities(String developmentOpportunities) {
        this.developmentOpportunities = developmentOpportunities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods for easier access to task references
    public java.util.List<WeeklyReportTaskReference.RoutineTaskReference> getThisWeekRoutineTasks() {
        return this.content.getThisWeekReport().getRoutineTasks();
    }

    public java.util.List<WeeklyReportTaskReference.DevelopmentTaskReference> getThisWeekDevelopmentTasks() {
        return this.content.getThisWeekReport().getDevelopmentTasks();
    }

    public java.util.List<WeeklyReportTaskReference.RoutineTaskReference> getNextWeekRoutineTasks() {
        return this.content.getNextWeekPlan().getRoutineTasks();
    }

    public java.util.List<WeeklyReportTaskReference.DevelopmentTaskReference> getNextWeekDevelopmentTasks() {
        return this.content.getNextWeekPlan().getDevelopmentTasks();
    }

    @Override
    public String toString() {
        return "WeeklyReportCreateRequestV2{" +
                "title='" + title + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}