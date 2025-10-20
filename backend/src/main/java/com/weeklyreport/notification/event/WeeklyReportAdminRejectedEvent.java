package com.weeklyreport.notification.event;

import org.springframework.context.ApplicationEvent;

/**
 * 周报管理员拒绝事件
 * 当管理员拒绝周报时触发，通知周报提交者和主管
 */
public class WeeklyReportAdminRejectedEvent extends ApplicationEvent {

    private final Long weeklyReportId;
    private final String weeklyReportTitle;
    private final String reportWeek;
    private final Long reportAuthorId;
    private final String reportAuthorName;
    private final Long projectId;
    private final String projectName;
    private final String rejectionReason;
    private final Long reviewerId;
    private final String reviewerName;

    public WeeklyReportAdminRejectedEvent(Object source, Long weeklyReportId, String weeklyReportTitle,
                                         String reportWeek, Long reportAuthorId, String reportAuthorName,
                                         Long projectId, String projectName, String rejectionReason,
                                         Long reviewerId, String reviewerName) {
        super(source);
        this.weeklyReportId = weeklyReportId;
        this.weeklyReportTitle = weeklyReportTitle;
        this.reportWeek = reportWeek;
        this.reportAuthorId = reportAuthorId;
        this.reportAuthorName = reportAuthorName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.rejectionReason = rejectionReason;
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
    public String getRejectionReason() { return rejectionReason; }
    public Long getReviewerId() { return reviewerId; }
    public String getReviewerName() { return reviewerName; }

    @Override
    public String toString() {
        return "WeeklyReportAdminRejectedEvent{" +
                "weeklyReportId=" + weeklyReportId +
                ", weeklyReportTitle='" + weeklyReportTitle + '\'' +
                ", reportWeek='" + reportWeek + '\'' +
                ", reportAuthorId=" + reportAuthorId +
                ", reportAuthorName='" + reportAuthorName + '\'' +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", reviewerId=" + reviewerId +
                ", reviewerName='" + reviewerName + '\'' +
                '}';
    }
}