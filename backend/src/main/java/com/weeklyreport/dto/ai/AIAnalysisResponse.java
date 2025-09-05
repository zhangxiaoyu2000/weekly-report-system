package com.weeklyreport.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for AI analysis results
 */
public class AIAnalysisResponse {

    private Long analysisId;
    
    private Long reportId;
    
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    private String summary;
    
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    
    private Double sentimentScore; // -1.0 to 1.0
    
    private List<String> keywords;
    
    private List<String> risks;
    
    private List<String> suggestions;
    
    private Map<String, Object> insights; // Additional structured insights
    
    private Integer confidenceScore; // 0-100
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisCompleteTime;
    
    private Long processingTimeMs;
    
    private String errorMessage;

    // Constructors
    public AIAnalysisResponse() {}

    public AIAnalysisResponse(Long analysisId, Long reportId, String status) {
        this.analysisId = analysisId;
        this.reportId = reportId;
        this.status = status;
    }

    // Builder pattern for easier construction
    public static class Builder {
        private AIAnalysisResponse response = new AIAnalysisResponse();

        public Builder analysisId(Long analysisId) {
            response.analysisId = analysisId;
            return this;
        }

        public Builder reportId(Long reportId) {
            response.reportId = reportId;
            return this;
        }

        public Builder status(String status) {
            response.status = status;
            return this;
        }

        public Builder summary(String summary) {
            response.summary = summary;
            return this;
        }

        public Builder sentiment(String sentiment, Double score) {
            response.sentiment = sentiment;
            response.sentimentScore = score;
            return this;
        }

        public Builder keywords(List<String> keywords) {
            response.keywords = keywords;
            return this;
        }

        public Builder risks(List<String> risks) {
            response.risks = risks;
            return this;
        }

        public Builder suggestions(List<String> suggestions) {
            response.suggestions = suggestions;
            return this;
        }

        public Builder insights(Map<String, Object> insights) {
            response.insights = insights;
            return this;
        }

        public Builder confidenceScore(Integer score) {
            response.confidenceScore = score;
            return this;
        }

        public Builder timing(LocalDateTime startTime, LocalDateTime endTime, Long processingMs) {
            response.analysisStartTime = startTime;
            response.analysisCompleteTime = endTime;
            response.processingTimeMs = processingMs;
            return this;
        }

        public Builder error(String errorMessage) {
            response.errorMessage = errorMessage;
            return this;
        }

        public AIAnalysisResponse build() {
            return response;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Map<String, Object> getInsights() {
        return insights;
    }

    public void setInsights(Map<String, Object> insights) {
        this.insights = insights;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Integer confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getAnalysisStartTime() {
        return analysisStartTime;
    }

    public void setAnalysisStartTime(LocalDateTime analysisStartTime) {
        this.analysisStartTime = analysisStartTime;
    }

    public LocalDateTime getAnalysisCompleteTime() {
        return analysisCompleteTime;
    }

    public void setAnalysisCompleteTime(LocalDateTime analysisCompleteTime) {
        this.analysisCompleteTime = analysisCompleteTime;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "AIAnalysisResponse{" +
                "analysisId=" + analysisId +
                ", reportId=" + reportId +
                ", status='" + status + '\'' +
                ", sentiment='" + sentiment + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}