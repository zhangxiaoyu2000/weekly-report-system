package com.weeklyreport.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * 周报AI分析完成事件
 * 当周报AI分析完成时触发，通知主管进行审核
 */
public class WeeklyReportAICompletedEvent extends ApplicationEvent {

    private final Long weeklyReportId;
    private final String weeklyReportTitle;
    private final String reportWeek;
    private final Long reportAuthorId;
    private final String reportAuthorName;
    private final Long projectId;
    private final String projectName;

    public WeeklyReportAICompletedEvent(Object source, Long weeklyReportId, String weeklyReportTitle, 
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
        return "WeeklyReportAICompletedEvent{" +
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