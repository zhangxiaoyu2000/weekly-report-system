package com.weeklyreport.dto.ai;

import com.weeklyreport.entity.AIAnalysisResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for AI analysis operations - 严格按照AIAnalysisResult.java设计
 */
public class AIAnalysisRequest {

    @NotNull(message = "Report ID is required")
    @Positive(message = "Report ID must be positive")
    private Long reportId;                              // 对应weekly_reports表ID

    private AIAnalysisResult.AnalysisType analysisType; // 分析类型
    
    private String analysisLanguage = "zh-CN";          // 分析语言
    
    private Boolean includeDetails = false;             // 是否包含详细信息

    // Constructors
    public AIAnalysisRequest() {}

    public AIAnalysisRequest(Long reportId) {
        this.reportId = reportId;
    }

    public AIAnalysisRequest(Long reportId, AIAnalysisResult.AnalysisType analysisType) {
        this.reportId = reportId;
        this.analysisType = analysisType;
    }

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public AIAnalysisResult.AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AIAnalysisResult.AnalysisType analysisType) {
        this.analysisType = analysisType;
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
                ", analysisType=" + analysisType +
                ", analysisLanguage='" + analysisLanguage + '\'' +
                ", includeDetails=" + includeDetails +
                '}';
    }
}