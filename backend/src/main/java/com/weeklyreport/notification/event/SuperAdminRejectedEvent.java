package com.weeklyreport.notification.event;

/**
 * 超级管理员拒绝事件
 * 触发时机：超级管理员拒绝项目
 * 通知对象：项目经理
 */
public class SuperAdminRejectedEvent extends ProjectNotificationEvent {

    private final String rejectionReason;

    public SuperAdminRejectedEvent(Long projectId, String projectName, Long triggerUserId, String rejectionReason) {
        super(projectId, projectName, triggerUserId);
        this.rejectionReason = rejectionReason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}