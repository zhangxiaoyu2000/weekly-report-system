package com.weeklyreport.notification.event;

/**
 * 强制提交事件
 * 触发时机：管理员强制提交项目（跳过正常审核流程）
 * 通知对象：所有管理员
 */
public class ForceSubmittedEvent extends ProjectNotificationEvent {

    private final String forceReason;

    public ForceSubmittedEvent(Long projectId, String projectName, Long triggerUserId, String forceReason) {
        super(projectId, projectName, triggerUserId);
        this.forceReason = forceReason;
    }

    public String getForceReason() {
        return forceReason;
    }
}