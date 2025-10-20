package com.weeklyreport.notification.event;

/**
 * 待管理员审核事件
 * 触发时机：项目状态更改为待管理员审核
 * 通知对象：所有管理员
 */
public class PendingAdminReviewEvent extends ProjectNotificationEvent {

    public PendingAdminReviewEvent(Long projectId, String projectName, Long triggerUserId) {
        super(projectId, projectName, triggerUserId);
    }
}