package com.weeklyreport.notification.event;

/**
 * AI分析完成事件
 * 触发时机：AI分析任务完成后
 * 通知对象：项目经理
 */
public class AIAnalysisCompletedEvent extends ProjectNotificationEvent {

    private final String analysisResult;

    public AIAnalysisCompletedEvent(Long projectId, String projectName, Long triggerUserId, String analysisResult) {
        super(projectId, projectName, triggerUserId);
        this.analysisResult = analysisResult;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }
}