package com.weeklyreport.dto.weeklyreport;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO for creating new weekly reports
 */
public class WeeklyReportCreateRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @NotNull(message = "Report week cannot be null")
    private LocalDate reportWeek;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @Size(max = 2000, message = "Work summary must not exceed 2000 characters")
    private String workSummary;

    @Size(max = 2000, message = "Achievements must not exceed 2000 characters")
    private String achievements;

    @Size(max = 2000, message = "Challenges must not exceed 2000 characters")
    private String challenges;

    @Size(max = 2000, message = "Next week plan must not exceed 2000 characters")
    private String nextWeekPlan;

    @Size(max = 1000, message = "Additional notes must not exceed 1000 characters")
    private String additionalNotes;

    @Min(value = 1, message = "Priority must be between 1 and 10")
    @Max(value = 10, message = "Priority must be between 1 and 10")
    private Integer priority = 5;

    private Long templateId;

    private Long projectId;

    // Constructors
    public WeeklyReportCreateRequest() {}

    public WeeklyReportCreateRequest(String title, LocalDate reportWeek, String content) {
        this.title = title;
        this.reportWeek = reportWeek;
        this.content = content;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReportWeek() {
        return reportWeek;
    }

    public void setReportWeek(LocalDate reportWeek) {
        this.reportWeek = reportWeek;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWorkSummary() {
        return workSummary;
    }

    public void setWorkSummary(String workSummary) {
        this.workSummary = workSummary;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getChallenges() {
        return challenges;
    }

    public void setChallenges(String challenges) {
        this.challenges = challenges;
    }

    public String getNextWeekPlan() {
        return nextWeekPlan;
    }

    public void setNextWeekPlan(String nextWeekPlan) {
        this.nextWeekPlan = nextWeekPlan;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "WeeklyReportCreateRequest{" +
                "title='" + title + '\'' +
                ", reportWeek=" + reportWeek +
                ", priority=" + priority +
                '}';
    }
}