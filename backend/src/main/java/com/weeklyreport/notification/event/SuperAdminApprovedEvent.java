package com.weeklyreport.notification.event;

/**
 * 超级管理员通过事件
 * 触发时机：超级管理员通过项目审核
 * 通知对象：所有相关人员（项目经理、管理员、超级管理员）
 */
public class SuperAdminApprovedEvent extends ProjectNotificationEvent {

    public SuperAdminApprovedEvent(Long projectId, String projectName, Long triggerUserId) {
        super(projectId, projectName, triggerUserId);
    }
}