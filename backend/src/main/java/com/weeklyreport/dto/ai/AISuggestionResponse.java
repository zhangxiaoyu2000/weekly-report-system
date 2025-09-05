package com.weeklyreport.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for AI suggestions
 */
public class AISuggestionResponse {

    private String suggestionId;
    
    private String context;
    
    private List<Suggestion> suggestions;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    private Integer totalSuggestions;
    
    private String confidence; // HIGH, MEDIUM, LOW
    
    private String language = "zh-CN";

    /**
     * Individual suggestion item
     */
    public static class Suggestion {
        private String title;
        private String description;
        private String category; // "improvement", "risk_mitigation", "optimization", etc.
        private String priority; // HIGH, MEDIUM, LOW
        private Integer confidenceScore; // 0-100
        private List<String> tags;
        private String actionType; // "immediate", "short_term", "long_term"

        // Constructors
        public Suggestion() {}

        public Suggestion(String title, String description, String category) {
            this.title = title;
            this.description = description;
            this.category = category;
        }

        // Getters and Setters
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

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public Integer getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(Integer confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        @Override
        public String toString() {
            return "Suggestion{" +
                    "title='" + title + '\'' +
                    ", category='" + category + '\'' +
                    ", priority='" + priority + '\'' +
                    ", confidenceScore=" + confidenceScore +
                    '}';
        }
    }

    // Constructors
    public AISuggestionResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public AISuggestionResponse(String suggestionId, String context) {
        this();
        this.suggestionId = suggestionId;
        this.context = context;
    }

    // Getters and Setters
    public String getSuggestionId() {
        return suggestionId;
    }

    public void setSuggestionId(String suggestionId) {
        this.suggestionId = suggestionId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
        this.totalSuggestions = suggestions != null ? suggestions.size() : 0;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Integer getTotalSuggestions() {
        return totalSuggestions;
    }

    public void setTotalSuggestions(Integer totalSuggestions) {
        this.totalSuggestions = totalSuggestions;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "AISuggestionResponse{" +
                "suggestionId='" + suggestionId + '\'' +
                ", context='" + context + '\'' +
                ", totalSuggestions=" + totalSuggestions +
                ", confidence='" + confidence + '\'' +
                ", generatedAt=" + generatedAt +
                '}';
    }
}