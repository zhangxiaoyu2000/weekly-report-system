package com.weeklyreport.notification.event;

/**
 * 管理员通过事件
 * 触发时机：管理员通过项目审核
 * 通知对象：超级管理员和项目经理
 */
public class AdminApprovedEvent extends ProjectNotificationEvent {

    public AdminApprovedEvent(Long projectId, String projectName, Long triggerUserId) {
        super(projectId, projectName, triggerUserId);
    }
}