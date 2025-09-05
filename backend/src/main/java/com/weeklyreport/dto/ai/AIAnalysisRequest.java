package com.weeklyreport.dto.ai;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * Request DTO for AI analysis operations
 */
public class AIAnalysisRequest {

    @NotNull(message = "Report ID is required")
    @Positive(message = "Report ID must be positive")
    private Long reportId;

    private List<String> analysisTypes; // e.g., ["summary", "sentiment", "keywords", "risks"]
    
    private String analysisLanguage = "zh-CN"; // Default to Chinese
    
    private Boolean includeDetails = false;

    // Constructors
    public AIAnalysisRequest() {}

    public AIAnalysisRequest(Long reportId) {
        this.reportId = reportId;
    }

    public AIAnalysisRequest(Long reportId, List<String> analysisTypes) {
        this.reportId = reportId;
        this.analysisTypes = analysisTypes;
    }

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public List<String> getAnalysisTypes() {
        return analysisTypes;
    }

    public void setAnalysisTypes(List<String> analysisTypes) {
        this.analysisTypes = analysisTypes;
    }

    public String getAnalysisLanguage() {
        return analysisLanguage;
    }

    public void setAnalysisLanguage(String analysisLanguage) {
        this.analysisLanguage = analysisLanguage;
    }

    public Boolean getIncludeDetails() {
        return includeDetails;
    }

    public void setIncludeDetails(Boolean includeDetails) {
        this.includeDetails = includeDetails;
    }

    @Override
    public String toString() {
        return "AIAnalysisRequest{" +
                "reportId=" + reportId +
                ", analysisTypes=" + analysisTypes +
                ", analysisLanguage='" + analysisLanguage + '\'' +
                ", includeDetails=" + includeDetails +
                '}';
    }
}