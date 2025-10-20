package com.weeklyreport.ai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AIAnalysisResult entity - 严格按照数据库设计.md第40-57行要求
 * 
 * 核心字段映射:
 * - report_id BIGINT NOT NULL (简单外键，不是对象关联)
 * - analysis_type VARCHAR(50) NOT NULL
 * - result TEXT NOT NULL
 * - confidence DECIMAL(3,2) 
 * - status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
 * - processing_time_ms BIGINT
 * - model_version VARCHAR(100)
 * - parameters TEXT
 * - error_message TEXT
 * - metadata JSON
 * - created_at, updated_at, completed_at TIMESTAMP
 */
@Entity
@Table(name = "ai_analysis_results", indexes = {
    @Index(name = "idx_analysis_report", columnList = "report_id"),
    @Index(name = "idx_analysis_type", columnList = "analysis_type"),
    @Index(name = "idx_analysis_status", columnList = "status"),
    @Index(name = "idx_analysis_created", columnList = "created_at")
})
public class AIAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Report ID cannot be null")
    @Column(name = "report_id", nullable = false)
    private Long reportId;                          // 实体ID (项目ID或周报ID)

    @NotNull(message = "Entity type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType = EntityType.WEEKLY_REPORT;  // 关联的实体类型

    @NotNull(message = "Analysis type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false, length = 50)
    private AnalysisType analysisType;

    @NotBlank(message = "Analysis result cannot be blank")
    @Column(name = "result", nullable = false, columnDefinition = "TEXT")
    private String result;

    @DecimalMin(value = "0.0", message = "Confidence must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Confidence must be between 0 and 1")
    @Column(name = "confidence")
    private Double confidence;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs; // 处理耗时（毫秒）

    @Column(name = "model_version", length = 100)
    private String modelVersion; // AI模型版本

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters; // JSON格式的分析参数

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 错误信息（失败时）

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // 额外的元数据，JSON格式

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Entity type enum
    public enum EntityType {
        PROJECT,       // 项目
        WEEKLY_REPORT  // 周报
    }

    // Analysis type enum
    public enum AnalysisType {
        SUMMARY,          // 内容摘要
        KEYWORDS,         // 关键词提取
        SENTIMENT,        // 情感分析
        RISK_ASSESSMENT,  // 风险评估
        SUGGESTIONS,      // 智能建议
        PROGRESS_ANALYSIS, // 进度分析
        WORKLOAD_ANALYSIS, // 工作量分析
        COLLABORATION_ANALYSIS, // 协作分析
        TREND_PREDICTION, // 趋势预测
        COMPLETENESS_CHECK // 完整性检查
    }

    // Analysis status enum
    public enum AnalysisStatus {
        PENDING,    // 等待处理
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED,     // 处理失败
        CANCELLED   // 已取消
    }

    // Constructors
    public AIAnalysisResult() {}

    public AIAnalysisResult(Long reportId, AnalysisType analysisType) {
        this.reportId = reportId;
        this.analysisType = analysisType;
        this.status = AnalysisStatus.PENDING;
        this.entityType = EntityType.WEEKLY_REPORT; // 默认为周报
    }

    public AIAnalysisResult(Long reportId, AnalysisType analysisType, EntityType entityType) {
        this.reportId = reportId;
        this.analysisType = analysisType;
        this.entityType = entityType;
        this.status = AnalysisStatus.PENDING;
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

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
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

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
        if (status == AnalysisStatus.COMPLETED && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    // Business logic methods
    public void markAsCompleted(String result, Double confidence) {
        this.result = result;
        this.confidence = confidence;
        this.status = AnalysisStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = AnalysisStatus.FAILED;
    }

    public void startProcessing() {
        this.status = AnalysisStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return status == AnalysisStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == AnalysisStatus.FAILED;
    }

    public boolean isPending() {
        return status == AnalysisStatus.PENDING;
    }

    public boolean isProcessing() {
        return status == AnalysisStatus.PROCESSING;
    }

    // Calculate processing duration if completed
    public Long getProcessingDurationMs() {
        if (completedAt != null && createdAt != null) {
            return java.time.Duration.between(createdAt, completedAt).toMillis();
        }
        return null;
    }
    
    // Alias methods for compatibility
    public Double getConfidenceScore() {
        return confidence;
    }
    
    public String getResultData() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AIAnalysisResult)) return false;
        AIAnalysisResult that = (AIAnalysisResult) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AIAnalysisResult{" +
                "id=" + id +
                ", analysisType=" + analysisType +
                ", status=" + status +
                ", confidence=" + confidence +
                ", reportId=" + reportId +
                '}';
    }
}