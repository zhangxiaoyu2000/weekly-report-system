package com.weeklyreport.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * 周报提交事件
 * 当用户提交周报进入审核流程时触发
 */
public class WeeklyReportSubmittedEvent extends ApplicationEvent {

    private final Long weeklyReportId;
    private final String weeklyReportTitle;
    private final String reportWeek;
    private final Long reportAuthorId;
    private final String reportAuthorName;
    private final Long projectId;
    private final String projectName;

    public WeeklyReportSubmittedEvent(Object source, Long weeklyReportId, String weeklyReportTitle,
                                     String reportWeek, Long reportAuthorId, String reportAuthorName,
                                     Long projectId, String projectName) {
        super(source);
        this.weeklyReportId = weeklyReportId;
        this.weeklyReportTitle = weeklyReportTitle;
        this.reportWeek = reportWeek;
        this.reportAuthorId = reportAuthorId;
        this.reportAuthorName = reportAuthorName;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // Getters
    public Long getWeeklyReportId() { return weeklyReportId; }
    public String getWeeklyReportTitle() { return weeklyReportTitle; }
    public String getReportWeek() { return reportWeek; }
    public Long getReportAuthorId() { return reportAuthorId; }
    public String getReportAuthorName() { return reportAuthorName; }
    public Long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }

    @Override
    public String toString() {
        return "WeeklyReportSubmittedEvent{" +
                "weeklyReportId=" + weeklyReportId +
                ", weeklyReportTitle='" + weeklyReportTitle + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", reportAuthorId=" + reportAuthorId +
                ", reportAuthorName='" + reportAuthorName + '\'' +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                '}';
    }
}
