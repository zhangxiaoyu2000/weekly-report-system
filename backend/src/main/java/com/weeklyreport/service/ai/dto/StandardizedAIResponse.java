package com.weeklyreport.service.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized AI analysis response
 */
public class StandardizedAIResponse {
    
    private Boolean isPass;
    private String proposal;
    private Double confidence;
    private AnalysisDetails analysisDetails;
    private LocalDateTime timestamp;
    private String providerId;
    private Long processingTimeMs;
    
    public StandardizedAIResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public StandardizedAIResponse(Boolean isPass, String proposal) {
        this();
        this.isPass = isPass;
        this.proposal = proposal;
    }
    
    // Getters and setters
    public Boolean getIsPass() {
        return isPass;
    }
    
    public void setIsPass(Boolean isPass) {
        this.isPass = isPass;
    }
    
    public String getProposal() {
        return proposal;
    }
    
    public void setProposal(String proposal) {
        this.proposal = proposal;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public AnalysisDetails getAnalysisDetails() {
        return analysisDetails;
    }
    
    public void setAnalysisDetails(AnalysisDetails analysisDetails) {
        this.analysisDetails = analysisDetails;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    /**
     * Detailed analysis information
     */
    public static class AnalysisDetails {
        private Double feasibilityScore;
        private RiskLevel riskLevel;
        private List<String> keyIssues;
        private List<String> recommendations;
        private Map<String, Object> metrics;
        
        public AnalysisDetails() {}
        
        // Getters and setters
        public Double getFeasibilityScore() {
            return feasibilityScore;
        }
        
        public void setFeasibilityScore(Double feasibilityScore) {
            this.feasibilityScore = feasibilityScore;
        }
        
        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
        
        public void setRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }
        
        public List<String> getKeyIssues() {
            return keyIssues;
        }
        
        public void setKeyIssues(List<String> keyIssues) {
            this.keyIssues = keyIssues;
        }
        
        public List<String> getRecommendations() {
            return recommendations;
        }
        
        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
        
        public Map<String, Object> getMetrics() {
            return metrics;
        }
        
        public void setMetrics(Map<String, Object> metrics) {
            this.metrics = metrics;
        }
    }
    
    /**
     * Risk level enumeration
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中等风险"), 
        HIGH("高风险"),
        CRITICAL("严重风险");
        
        private final String displayName;
        
        RiskLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}