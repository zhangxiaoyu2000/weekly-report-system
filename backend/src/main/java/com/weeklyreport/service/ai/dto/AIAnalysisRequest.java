package com.weeklyreport.service.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for AI analysis
 */
public class AIAnalysisRequest {
    
    @NotBlank(message = "Content cannot be blank")
    private String content;
    
    @NotNull(message = "Analysis type cannot be null")
    private AnalysisType analysisType;
    
    private Map<String, Object> parameters;
    
    private String context;
    
    public AIAnalysisRequest() {}
    
    public AIAnalysisRequest(String content, AnalysisType analysisType) {
        this.content = content;
        this.analysisType = analysisType;
    }
    
    // Getters and setters
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public AnalysisType getAnalysisType() {
        return analysisType;
    }
    
    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    /**
     * Types of AI analysis
     */
    public enum AnalysisType {
        SUMMARY,           // Generate summary
        SENTIMENT,         // Sentiment analysis
        KEYWORDS,          // Extract key words/phrases
        RISK_ASSESSMENT,   // Project risk assessment
        SUGGESTIONS,       // Generate improvement suggestions
        PROGRESS_PREDICTION, // Predict project progress
        PROJECT_EVALUATION // Project evaluation
    }
}