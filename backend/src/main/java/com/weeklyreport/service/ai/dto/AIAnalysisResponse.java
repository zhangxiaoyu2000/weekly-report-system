package com.weeklyreport.service.ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for AI analysis
 */
public class AIAnalysisResponse {
    
    private String analysisId;
    private AIAnalysisRequest.AnalysisType analysisType;
    private String result;
    private Double confidence;
    private List<String> keywords;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private String providerUsed;
    private Long processingTimeMs;
    
    public AIAnalysisResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AIAnalysisResponse(String result, AIAnalysisRequest.AnalysisType analysisType) {
        this();
        this.result = result;
        this.analysisType = analysisType;
    }
    
    // Getters and setters
    public String getAnalysisId() {
        return analysisId;
    }
    
    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }
    
    public AIAnalysisRequest.AnalysisType getAnalysisType() {
        return analysisType;
    }
    
    public void setAnalysisType(AIAnalysisRequest.AnalysisType analysisType) {
        this.analysisType = analysisType;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getProviderUsed() {
        return providerUsed;
    }
    
    public void setProviderUsed(String providerUsed) {
        this.providerUsed = providerUsed;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}