package com.weeklyreport.dto.ai;

import com.weeklyreport.entity.AIAnalysisResult;
import java.time.LocalDateTime;

/**
 * AI分析结果响应DTO - 只包含必要的数据库字段
 * 避免返回计算字段和状态检查方法
 */
public class AIAnalysisResultResponse {
    
    private Long id;
    private String entityType;
    private Long entityId;        // 对应reportId字段
    private String analysisType;
    private String status;
    private String result;
    private Double confidence;
    private String modelVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AIAnalysisResultResponse() {}

    public AIAnalysisResultResponse(AIAnalysisResult aiResult) {
        if (aiResult != null) {
            this.id = aiResult.getId();
            this.entityType = aiResult.getEntityType() != null ? aiResult.getEntityType().toString() : null;
            this.entityId = aiResult.getReportId();
            this.analysisType = aiResult.getAnalysisType() != null ? aiResult.getAnalysisType().toString() : null;
            this.status = aiResult.getStatus() != null ? aiResult.getStatus().toString() : null;
            this.result = aiResult.getResult();
            this.confidence = aiResult.getConfidence();
            this.modelVersion = aiResult.getModelVersion();
            this.createdAt = aiResult.getCreatedAt();
            this.updatedAt = aiResult.getUpdatedAt();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
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
}