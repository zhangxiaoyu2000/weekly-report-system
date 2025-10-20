package com.weeklyreport.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * 周报待管理员审核事件
 * 当周报进入待管理员审核状态时触发，通知所有管理员
 */
public class WeeklyReportPendingAdminReviewEvent extends ApplicationEvent {

    private final Long weeklyReportId;
    private final String weeklyReportTitle;
    private final String reportWeek;
    private final Long reportAuthorId;
    private final String reportAuthorName;
    private final Long projectId;
    private final String projectName;

    public WeeklyReportPendingAdminReviewEvent(Object source, Long weeklyReportId, String weeklyReportTitle,
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
        return "WeeklyReportPendingAdminReviewEvent{" +
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