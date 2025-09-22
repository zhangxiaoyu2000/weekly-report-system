package com.weeklyreport.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weeklyreport.entity.AIAnalysisResult;
import java.time.LocalDateTime;

/**
 * Response DTO for AI analysis results - 严格按照AIAnalysisResult.java设计
 */
public class AIAnalysisResponse {

    private Long id;                                        // 分析结果ID
    private Long reportId;                                  // 对应周报ID
    private AIAnalysisResult.AnalysisType analysisType;     // 分析类型
    private AIAnalysisResult.AnalysisStatus analysisStatus; // 分析状态
    private String analysisContent;                         // 分析内容
    private Integer qualityScore;                           // 质量评分
    private String improvementSuggestions;                  // 改进建议
    private Long analyzedBy;                                // 分析人员ID
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analyzedAt;                       // 分析时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;                        // 创建时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;                        // 更新时间

    // Constructors
    public AIAnalysisResponse() {}

    public AIAnalysisResponse(Long id, Long reportId, AIAnalysisResult.AnalysisStatus status) {
        this.id = id;
        this.reportId = reportId;
        this.analysisStatus = status;
    }
    
    public AIAnalysisResponse(AIAnalysisResult result) {
        this.id = result.getId();
        this.reportId = result.getReportId();
        this.analysisType = result.getAnalysisType();
        this.analysisStatus = result.getStatus();  // 直接映射status字段
        this.analysisContent = result.getResult();  // 直接映射result字段
        this.qualityScore = result.getConfidence() != null ? 
            (int)(result.getConfidence() * 100) : null;  // confidence转换为百分比
        this.improvementSuggestions = result.getErrorMessage();
        this.analyzedBy = null; // 简化版本中不包含此字段
        this.analyzedAt = result.getCompletedAt();
        this.createdAt = result.getCreatedAt();
        this.updatedAt = result.getUpdatedAt();
    }

    // Helper methods
    public boolean isCompleted() {
        return analysisStatus == AIAnalysisResult.AnalysisStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return analysisStatus == AIAnalysisResult.AnalysisStatus.PENDING;
    }
    
    public boolean isFailed() {
        return analysisStatus == AIAnalysisResult.AnalysisStatus.FAILED;
    }

    // Getters and Setters
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

    public AIAnalysisResult.AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AIAnalysisResult.AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public AIAnalysisResult.AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(AIAnalysisResult.AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
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

    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(String improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    public Long getAnalyzedBy() {
        return analyzedBy;
    }

    public void setAnalyzedBy(Long analyzedBy) {
        this.analyzedBy = analyzedBy;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AIAnalysisResponse{" +
                "id=" + id +
                ", reportId=" + reportId +
                ", analysisType=" + analysisType +
                ", analysisStatus=" + analysisStatus +
                ", qualityScore=" + qualityScore +
                '}';
    }
}