package com.weeklyreport.ai.dto;

import com.weeklyreport.ai.entity.AIAnalysisResult;

/**
 * AI分析响应DTO
 */
public class AIAnalysisResponse {
    
    private Long id;
    private Long reportId;
    private String analysisResult;
    private String analysisContent;
    private Double confidenceScore;
    private Integer qualityScore;
    private String status;
    private com.weeklyreport.ai.entity.AIAnalysisResult.AnalysisStatus analysisStatus;
    
    public AIAnalysisResponse() {}
    
    public AIAnalysisResponse(AIAnalysisResult result) {
        this.id = result.getId();
        this.reportId = result.getReportId();
        this.analysisResult = result.getResult();
        this.analysisContent = result.getResult();
        this.confidenceScore = result.getConfidenceScore();
        this.qualityScore = result.getConfidenceScore() != null ? (int)(result.getConfidenceScore() * 100) : 0;
        this.status = result.getStatus().name();
        this.analysisStatus = result.getStatus();
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getReportId() {
        return reportId;
    }
    
    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }
    
    public String getAnalysisContent() {
        return analysisContent;
    }
    
    public void setAnalysisContent(String analysisContent) {
        this.analysisContent = analysisContent;
    }
    
    public Integer getQualityScore() {
        return qualityScore;
    }
    
    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }
    
    public com.weeklyreport.ai.entity.AIAnalysisResult.AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }
    
    public void setAnalysisStatus(com.weeklyreport.ai.entity.AIAnalysisResult.AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
    }
}