package com.weeklyreport.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * AI建议请求DTO
 */
public class AISuggestionRequest {
    
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotBlank(message = "Context type cannot be blank")
    private String contextType; // PROJECT, WEEKLY_REPORT, TASK, GENERAL
    
    private Long contextId; // 相关实体ID（项目ID、周报ID等）
    
    @NotBlank(message = "Request type cannot be blank") 
    private String requestType; // IMPROVEMENT, OPTIMIZATION, NEXT_STEPS, RISK_MITIGATION
    
    private String currentContent; // 当前内容（周报内容、任务描述等）
    
    private List<String> specificAreas; // 特定关注领域
    
    private Map<String, Object> additionalContext; // 额外上下文信息
    
    private String priority; // LOW, MEDIUM, HIGH
    
    private Integer maxSuggestions; // 最大建议数量
    
    // 构造函数
    public AISuggestionRequest() {}
    
    public AISuggestionRequest(Long userId, String contextType, String requestType) {
        this.userId = userId;
        this.contextType = contextType;
        this.requestType = requestType;
    }
    
    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getContextType() {
        return contextType;
    }
    
    public void setContextType(String contextType) {
        this.contextType = contextType;
    }
    
    public Long getContextId() {
        return contextId;
    }
    
    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }
    
    public String getRequestType() {
        return requestType;
    }
    
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    
    public String getCurrentContent() {
        return currentContent;
    }
    
    public void setCurrentContent(String currentContent) {
        this.currentContent = currentContent;
    }
    
    public List<String> getSpecificAreas() {
        return specificAreas;
    }
    
    public void setSpecificAreas(List<String> specificAreas) {
        this.specificAreas = specificAreas;
    }
    
    public Map<String, Object> getAdditionalContext() {
        return additionalContext;
    }
    
    public void setAdditionalContext(Map<String, Object> additionalContext) {
        this.additionalContext = additionalContext;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public Integer getMaxSuggestions() {
        return maxSuggestions;
    }
    
    public void setMaxSuggestions(Integer maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
    }
    
    // Alias method for getContext
    public String getContext() {
        return this.contextType;
    }
    
    public void setContext(String context) {
        this.contextType = context;
    }
    
    @Override
    public String toString() {
        return "AISuggestionRequest{" +
                "userId=" + userId +
                ", contextType='" + contextType + '\'' +
                ", contextId=" + contextId +
                ", requestType='" + requestType + '\'' +
                ", currentContent='" + (currentContent != null ? currentContent.length() + " chars" : "null") + '\'' +
                ", specificAreas=" + specificAreas +
                ", priority='" + priority + '\'' +
                ", maxSuggestions=" + maxSuggestions +
                '}';
    }
}