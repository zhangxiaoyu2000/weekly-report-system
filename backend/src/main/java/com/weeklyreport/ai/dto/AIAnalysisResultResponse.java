package com.weeklyreport.ai.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.ai.entity.AIAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI分析结果响应DTO
 */
public class AIAnalysisResultResponse {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisResultResponse.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Long id;
    private AIAnalysisResult.EntityType entityType;
    private Long entityId;
    private AIAnalysisResult.AnalysisType analysisType;
    private AIAnalysisResult.AnalysisStatus status;
    private String result;  // 前端期望的字段名
    private Double confidence;  // 前端期望的字段名
    private String modelVersion;  // 前端期望显示的模型版本
    private Long processingTimeMs;  // 处理时间(毫秒)
    private Map<String, Object> metadata;  // 完整的分析结果JSON
    private Map<String, Object> analysisDetails; // 解析后的核心分析字段
    private Double feasibilityScore;
    private String riskLevel;
    private List<String> keyRisks;
    private List<String> strengths;
    private List<String> recommendations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AIAnalysisResultResponse() {}

    public AIAnalysisResultResponse(AIAnalysisResult aiResult) {
        this.id = aiResult.getId();
        this.entityType = aiResult.getEntityType();
        this.entityId = aiResult.getReportId();
        this.analysisType = aiResult.getAnalysisType();
        this.status = aiResult.getStatus();
        this.result = aiResult.getResult();
        this.confidence = aiResult.getConfidence();
        this.modelVersion = aiResult.getModelVersion();
        this.processingTimeMs = aiResult.getProcessingTimeMs();
        this.metadata = parseMetadata(aiResult.getMetadata());
        extractAnalysisInsights();
        this.createdAt = aiResult.getCreatedAt();
        this.updatedAt = aiResult.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AIAnalysisResult.EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(AIAnalysisResult.EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public AIAnalysisResult.AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AIAnalysisResult.AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public AIAnalysisResult.AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AIAnalysisResult.AnalysisStatus status) {
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

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        extractAnalysisInsights();
    }

    public Map<String, Object> getAnalysisDetails() {
        return analysisDetails;
    }

    public void setAnalysisDetails(Map<String, Object> analysisDetails) {
        this.analysisDetails = analysisDetails;
    }

    public Double getFeasibilityScore() {
        return feasibilityScore;
    }

    public void setFeasibilityScore(Double feasibilityScore) {
        this.feasibilityScore = feasibilityScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getKeyRisks() {
        return keyRisks;
    }

    public void setKeyRisks(List<String> keyRisks) {
        this.keyRisks = keyRisks;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
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

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("Failed to parse AI analysis metadata: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", metadataJson);
            fallback.put("parseError", e.getMessage());
            return fallback;
        }
    }

    private void extractAnalysisInsights() {
        this.analysisDetails = null;
        this.feasibilityScore = null;
        this.riskLevel = null;
        this.keyRisks = null;
        this.strengths = null;
        this.recommendations = null;

        if (metadata == null || metadata.isEmpty()) {
            return;
        }

        Object analysisNode = metadata.get("aiAnalysis");
        if (analysisNode instanceof Map<?, ?> rawAnalysis) {
            Map<String, Object> normalized = new HashMap<>();
            rawAnalysis.forEach((key, value) -> {
                if (key != null) {
                    normalized.put(key.toString(), value);
                }
            });
            this.analysisDetails = Collections.unmodifiableMap(normalized);

            this.feasibilityScore = extractDouble(normalized.getOrDefault("feasibility_score", normalized.get("feasibilityScore")));

            Object riskValue = normalized.getOrDefault("risk_level", normalized.get("riskLevel"));
            this.riskLevel = riskValue != null ? riskValue.toString() : null;

            List<String> riskList = asStringList(normalized.getOrDefault("key_risks", normalized.get("keyRisks")));
            this.keyRisks = riskList != null ? riskList : Collections.emptyList();

            List<String> strengthList = asStringList(normalized.get("strengths"));
            this.strengths = strengthList != null ? strengthList : Collections.emptyList();

            List<String> recommendationList = asStringList(normalized.get("recommendations"));
            this.recommendations = recommendationList != null ? recommendationList : Collections.emptyList();
        }
    }

    private Double extractDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                .map(item -> item != null ? item.toString() : null)
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toList());
        }
        if (value instanceof String text) {
            if (text.isBlank()) {
                return Collections.emptyList();
            }
            List<String> single = new ArrayList<>(1);
            single.add(text);
            return single;
        }
        if (value instanceof Map<?, ?> map) {
            return map.values().stream()
                .map(item -> item != null ? item.toString() : null)
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toList());
        }
        return null;
    }
}
