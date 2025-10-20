package com.weeklyreport.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * 周报AI分析拒绝事件
 * 当周报AI分析置信度不足时触发，通知提交者修改
 */
public class WeeklyReportAIRejectedEvent extends ApplicationEvent {

    private final Long weeklyReportId;
    private final String weeklyReportTitle;
    private final String reportWeek;
    private final Long reportAuthorId;
    private final String reportAuthorName;
    private final String rejectionReason;

    public WeeklyReportAIRejectedEvent(Object source, Long weeklyReportId, String weeklyReportTitle,
                                      String reportWeek, Long reportAuthorId, String reportAuthorName,
                                      String rejectionReason) {
        super(source);
        this.weeklyReportId = weeklyReportId;
        this.weeklyReportTitle = weeklyReportTitle;
        this.reportWeek = reportWeek;
        this.reportAuthorId = reportAuthorId;
        this.reportAuthorName = reportAuthorName;
        this.rejectionReason = rejectionReason;
    }

    // Getters
    public Long getWeeklyReportId() { return weeklyReportId; }
    public String getWeeklyReportTitle() { return weeklyReportTitle; }
    public String getReportWeek() { return reportWeek; }
    public Long getReportAuthorId() { return reportAuthorId; }
    public String getReportAuthorName() { return reportAuthorName; }
    public String getRejectionReason() { return rejectionReason; }

    @Override
    public String toString() {
        return "WeeklyReportAIRejectedEvent{" +
                "weeklyReportId=" + weeklyReportId +
                ", weeklyReportTitle='" + weeklyReportTitle + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", reportAuthorId=" + reportAuthorId +
                ", reportAuthorName='" + reportAuthorName + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}
