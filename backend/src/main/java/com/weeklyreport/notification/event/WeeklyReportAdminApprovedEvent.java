package com.weeklyreport.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * 周报管理员通过事件
 * 当管理员通过周报时触发，通知超级管理员和周报提交者
 */
public class WeeklyReportAdminApprovedEvent extends ApplicationEvent {

    private final Long weeklyReportId;
    private final String weeklyReportTitle;
    private final String reportWeek;
    private final Long reportAuthorId;
    private final String reportAuthorName;
    private final Long projectId;
    private final String projectName;
    private final Long reviewerId;
    private final String reviewerName;

    public WeeklyReportAdminApprovedEvent(Object source, Long weeklyReportId, String weeklyReportTitle,
                                         String reportWeek, Long reportAuthorId, String reportAuthorName,
                                         Long projectId, String projectName, Long reviewerId, String reviewerName) {
        super(source);
        this.weeklyReportId = weeklyReportId;
        this.weeklyReportTitle = weeklyReportTitle;
        this.reportWeek = reportWeek;
        this.reportAuthorId = reportAuthorId;
        this.reportAuthorName = reportAuthorName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
    }

    // Getters
    public Long getWeeklyReportId() { return weeklyReportId; }
    public String getWeeklyReportTitle() { return weeklyReportTitle; }
    public String getReportWeek() { return reportWeek; }
    public Long getReportAuthorId() { return reportAuthorId; }
    public String getReportAuthorName() { return reportAuthorName; }
    public Long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public Long getReviewerId() { return reviewerId; }
    public String getReviewerName() { return reviewerName; }

    @Override
    public String toString() {
        return "WeeklyReportAdminApprovedEvent{" +
                "weeklyReportId=" + weeklyReportId +
                ", weeklyReportTitle='" + weeklyReportTitle + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", reportAuthorId=" + reportAuthorId +
                ", reportAuthorName='" + reportAuthorName + '\'' +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", reviewerId=" + reviewerId +
                ", reviewerName='" + reviewerName + '\'' +
                '}';
    }
}