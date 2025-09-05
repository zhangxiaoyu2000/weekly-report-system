package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AIAnalysisResult entity representing AI-generated analysis results for weekly reports
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

    @NotNull(message = "Weekly report cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private WeeklyReport weeklyReport;

    @NotNull(message = "Analysis type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false, length = 50)
    private AnalysisType analysisType;

    @NotBlank(message = "Analysis result cannot be blank")
    @Column(name = "result", nullable = false, columnDefinition = "TEXT")
    private String result;

    @DecimalMin(value = "0.0", message = "Confidence must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Confidence must be between 0 and 1")
    @Column(name = "confidence", precision = 3, scale = 2)
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

    public AIAnalysisResult(WeeklyReport weeklyReport, AnalysisType analysisType) {
        this.weeklyReport = weeklyReport;
        this.analysisType = analysisType;
        this.status = AnalysisStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WeeklyReport getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(WeeklyReport weeklyReport) {
        this.weeklyReport = weeklyReport;
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
                ", reportId=" + (weeklyReport != null ? weeklyReport.getId() : "null") +
                '}';
    }
}