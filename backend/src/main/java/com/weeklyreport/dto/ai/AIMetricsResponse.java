package com.weeklyreport.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for AI service metrics and monitoring data
 */
public class AIMetricsResponse {

    private String serviceStatus; // "healthy", "degraded", "unavailable"
    
    private Long totalRequests;
    
    private Long successfulRequests;
    
    private Long failedRequests;
    
    private Double successRate; // Percentage
    
    private Double averageResponseTime; // Milliseconds
    
    private Double p95ResponseTime; // 95th percentile response time
    
    private Long totalAnalysesCompleted;
    
    private Double averageAnalysisAccuracy; // Percentage based on user feedback
    
    private Map<String, Long> errorCounts; // Error type -> count mapping
    
    private Map<String, Double> providerPerformance; // AI provider -> performance score
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHealthCheck;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime metricsCollectedAt;
    
    private String timeRange; // "24h", "7d", "30d"
    
    // Current system load indicators
    private Integer activeAnalyses;
    
    private Integer queuedAnalyses;
    
    private Double systemLoad; // 0.0 - 1.0
    
    private Map<String, Object> resourceUsage; // CPU, memory, etc.

    // Constructors
    public AIMetricsResponse() {
        this.metricsCollectedAt = LocalDateTime.now();
    }

    public AIMetricsResponse(String serviceStatus) {
        this();
        this.serviceStatus = serviceStatus;
    }

    // Getters and Setters
    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getSuccessfulRequests() {
        return successfulRequests;
    }

    public void setSuccessfulRequests(Long successfulRequests) {
        this.successfulRequests = successfulRequests;
    }

    public Long getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(Long failedRequests) {
        this.failedRequests = failedRequests;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(Double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public Double getP95ResponseTime() {
        return p95ResponseTime;
    }

    public void setP95ResponseTime(Double p95ResponseTime) {
        this.p95ResponseTime = p95ResponseTime;
    }

    public Long getTotalAnalysesCompleted() {
        return totalAnalysesCompleted;
    }

    public void setTotalAnalysesCompleted(Long totalAnalysesCompleted) {
        this.totalAnalysesCompleted = totalAnalysesCompleted;
    }

    public Double getAverageAnalysisAccuracy() {
        return averageAnalysisAccuracy;
    }

    public void setAverageAnalysisAccuracy(Double averageAnalysisAccuracy) {
        this.averageAnalysisAccuracy = averageAnalysisAccuracy;
    }

    public Map<String, Long> getErrorCounts() {
        return errorCounts;
    }

    public void setErrorCounts(Map<String, Long> errorCounts) {
        this.errorCounts = errorCounts;
    }

    public Map<String, Double> getProviderPerformance() {
        return providerPerformance;
    }

    public void setProviderPerformance(Map<String, Double> providerPerformance) {
        this.providerPerformance = providerPerformance;
    }

    public LocalDateTime getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public LocalDateTime getMetricsCollectedAt() {
        return metricsCollectedAt;
    }

    public void setMetricsCollectedAt(LocalDateTime metricsCollectedAt) {
        this.metricsCollectedAt = metricsCollectedAt;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public Integer getActiveAnalyses() {
        return activeAnalyses;
    }

    public void setActiveAnalyses(Integer activeAnalyses) {
        this.activeAnalyses = activeAnalyses;
    }

    public Integer getQueuedAnalyses() {
        return queuedAnalyses;
    }

    public void setQueuedAnalyses(Integer queuedAnalyses) {
        this.queuedAnalyses = queuedAnalyses;
    }

    public Double getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(Double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public Map<String, Object> getResourceUsage() {
        return resourceUsage;
    }

    public void setResourceUsage(Map<String, Object> resourceUsage) {
        this.resourceUsage = resourceUsage;
    }

    // Helper methods
    public boolean isHealthy() {
        return "healthy".equals(serviceStatus);
    }

    public boolean isDegraded() {
        return "degraded".equals(serviceStatus);
    }

    public boolean isUnavailable() {
        return "unavailable".equals(serviceStatus);
    }

    @Override
    public String toString() {
        return "AIMetricsResponse{" +
                "serviceStatus='" + serviceStatus + '\'' +
                ", successRate=" + successRate +
                ", averageResponseTime=" + averageResponseTime +
                ", totalAnalysesCompleted=" + totalAnalysesCompleted +
                ", activeAnalyses=" + activeAnalyses +
                ", queuedAnalyses=" + queuedAnalyses +
                ", metricsCollectedAt=" + metricsCollectedAt +
                '}';
    }
}