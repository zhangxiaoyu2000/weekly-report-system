package com.weeklyreport.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * AI分析请求DTO
 */
public class AIAnalysisRequest {
    
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotNull(message = "Entity ID cannot be null")
    @Positive(message = "Entity ID must be positive")
    private Long entityId;
    
    @NotBlank(message = "Entity type cannot be blank")
    private String entityType; // WEEKLY_REPORT, PROJECT, TASK, USER
    
    @NotBlank(message = "Analysis type cannot be blank")
    private String analysisType; // PERFORMANCE, QUALITY, PRODUCTIVITY, RISK, COMPREHENSIVE
    
    private String content; // 待分析的内容
    
    private List<String> focusAreas; // 重点分析领域
    
    private Map<String, Object> parameters; // 分析参数
    
    private String priority; // LOW, MEDIUM, HIGH
    
    private Boolean asyncMode; // 是否异步处理
    
    private String callbackUrl; // 回调URL（异步模式）
    
    // 构造函数
    public AIAnalysisRequest() {
        this.asyncMode = false;
    }
    
    public AIAnalysisRequest(Long entityId) {
        this();
        this.entityId = entityId;
        this.entityType = "WEEKLY_REPORT";
        this.analysisType = "QUALITY";
    }
    
    public AIAnalysisRequest(Long userId, Long entityId, String entityType, String analysisType) {
        this();
        this.userId = userId;
        this.entityId = entityId;
        this.entityType = entityType;
        this.analysisType = analysisType;
    }
    
    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getAnalysisType() {
        return analysisType;
    }
    
    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<String> getFocusAreas() {
        return focusAreas;
    }
    
    public void setFocusAreas(List<String> focusAreas) {
        this.focusAreas = focusAreas;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public Boolean getAsyncMode() {
        return asyncMode;
    }
    
    public void setAsyncMode(Boolean asyncMode) {
        this.asyncMode = asyncMode;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    @Override
    public String toString() {
        return "AIAnalysisRequest{" +
                "userId=" + userId +
                ", entityId=" + entityId +
                ", entityType='" + entityType + '\'' +
                ", analysisType='" + analysisType + '\'' +
                ", content='" + (content != null ? content.length() + " chars" : "null") + '\'' +
                ", focusAreas=" + focusAreas +
                ", priority='" + priority + '\'' +
                ", asyncMode=" + asyncMode +
                '}';
    }
}