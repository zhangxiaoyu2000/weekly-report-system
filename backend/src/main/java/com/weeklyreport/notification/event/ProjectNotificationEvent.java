package com.weeklyreport.notification.event;

import java.time.LocalDateTime;

/**
 * 项目通知事件基类
 */
public abstract class ProjectNotificationEvent {
    
    private final Long projectId;
    private final String projectName;
    private final Long triggerUserId;
    private final LocalDateTime timestamp;

    protected ProjectNotificationEvent(Long projectId, String projectName, Long triggerUserId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.triggerUserId = triggerUserId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", triggerUserId=" + triggerUserId +
                ", timestamp=" + timestamp +
                '}';
    }
}