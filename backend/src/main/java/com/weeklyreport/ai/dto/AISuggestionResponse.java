package com.weeklyreport.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI建议响应DTO
 */
public class AISuggestionResponse {
    
    private Long requestId;
    
    private String contextType;
    
    private Long contextId;
    
    private String requestType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    private List<Suggestion> suggestions;
    
    private String overallRecommendation;
    
    private Integer totalSuggestions;
    
    private String status; // SUCCESS, PARTIAL, FAILED
    
    private String suggestionId;
    
    private String confidence;
    
    // 构造函数
    public AISuggestionResponse() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public AISuggestionResponse(String contextType, Long contextId, String requestType) {
        this();
        this.contextType = contextType;
        this.contextId = contextId;
        this.requestType = requestType;
    }
    
    // Getter和Setter方法
    public Long getRequestId() {
        return requestId;
    }
    
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
        this.totalSuggestions = suggestions != null ? suggestions.size() : 0;
    }
    
    public String getOverallRecommendation() {
        return overallRecommendation;
    }
    
    public void setOverallRecommendation(String overallRecommendation) {
        this.overallRecommendation = overallRecommendation;
    }
    
    public Integer getTotalSuggestions() {
        return totalSuggestions;
    }
    
    public void setTotalSuggestions(Integer totalSuggestions) {
        this.totalSuggestions = totalSuggestions;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSuggestionId() {
        return suggestionId;
    }
    
    public void setSuggestionId(String suggestionId) {
        this.suggestionId = suggestionId;
    }
    
    public String getConfidence() {
        return confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
    
    /**
     * AI建议内嵌类
     */
    public static class Suggestion {
        private String id;
        private String type; // IMPROVEMENT, OPTIMIZATION, NEXT_STEP, WARNING
        private String title;
        private String description;
        private String priority; // LOW, MEDIUM, HIGH, CRITICAL
        private String category;
        private List<String> actionItems;
        private Double confidence; // 0.0-1.0
        private String reasoning;
        private List<String> relatedAreas;
        
        // 构造函数
        public Suggestion() {}
        
        public Suggestion(String type, String title, String description) {
            this.type = type;
            this.title = title;
            this.description = description;
        }
        
        public Suggestion(String type, String title, String description, String priority) {
            this(type, title, description);
            this.priority = priority;
        }
        
        // Getter和Setter方法
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getPriority() {
            return priority;
        }
        
        public void setPriority(String priority) {
            this.priority = priority;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public List<String> getActionItems() {
            return actionItems;
        }
        
        public void setActionItems(List<String> actionItems) {
            this.actionItems = actionItems;
        }
        
        public Double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }
        
        public String getReasoning() {
            return reasoning;
        }
        
        public void setReasoning(String reasoning) {
            this.reasoning = reasoning;
        }
        
        public List<String> getRelatedAreas() {
            return relatedAreas;
        }
        
        public void setRelatedAreas(List<String> relatedAreas) {
            this.relatedAreas = relatedAreas;
        }
    }
}