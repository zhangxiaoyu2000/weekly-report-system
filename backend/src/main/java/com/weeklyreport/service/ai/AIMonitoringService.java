package com.weeklyreport.service.ai;

import com.weeklyreport.dto.ai.AIMetricsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI Monitoring Service
 * Tracks performance metrics, health status, and system statistics for AI functionality
 */
@Service
public class AIMonitoringService implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(AIMonitoringService.class);

    // Metrics storage
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalAnalysesCompleted = new AtomicLong(0);
    
    private final AtomicReference<Double> averageResponseTime = new AtomicReference<>(0.0);
    private final AtomicReference<Double> p95ResponseTime = new AtomicReference<>(0.0);
    private final AtomicReference<Double> averageAnalysisAccuracy = new AtomicReference<>(0.0);
    
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, Double> providerPerformance = new ConcurrentHashMap<>();
    
    private final AtomicReference<LocalDateTime> lastHealthCheck = new AtomicReference<>(LocalDateTime.now());
    private final AtomicReference<String> serviceStatus = new AtomicReference<>("healthy");
    
    private final AtomicLong activeAnalyses = new AtomicLong(0);
    private final AtomicLong queuedAnalyses = new AtomicLong(0);
    
    // Performance tracking
    private final Map<Long, Long> requestStartTimes = new ConcurrentHashMap<>();
    private final Map<Long, Long> responseTimes = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        logger.info("AI Monitoring Service initialized");
        
        // Initialize provider performance tracking
        providerPerformance.put("openai", 95.0);
        providerPerformance.put("azure", 92.0);
        providerPerformance.put("local", 88.0);
        
        // Initialize error counters
        errorCounts.put("network_error", new AtomicLong(0));
        errorCounts.put("api_limit_exceeded", new AtomicLong(0));
        errorCounts.put("invalid_request", new AtomicLong(0));
        errorCounts.put("timeout", new AtomicLong(0));
        errorCounts.put("service_unavailable", new AtomicLong(0));
    }

    /**
     * Record the start of an AI request
     */
    public void recordRequestStart(Long requestId) {
        totalRequests.incrementAndGet();
        requestStartTimes.put(requestId, System.currentTimeMillis());
        logger.debug("AI request {} started", requestId);
    }

    /**
     * Record successful completion of an AI request
     */
    public void recordRequestSuccess(Long requestId) {
        successfulRequests.incrementAndGet();
        recordRequestEnd(requestId);
        updateServiceStatus();
        logger.debug("AI request {} completed successfully", requestId);
    }

    /**
     * Record failed AI request
     */
    public void recordRequestFailure(Long requestId, String errorType) {
        failedRequests.incrementAndGet();
        recordRequestEnd(requestId);
        
        // Increment specific error counter
        errorCounts.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
        
        updateServiceStatus();
        logger.warn("AI request {} failed with error type: {}", requestId, errorType);
    }

    /**
     * Record completion of an analysis task
     */
    public void recordAnalysisCompleted(Double accuracyScore) {
        totalAnalysesCompleted.incrementAndGet();
        
        if (accuracyScore != null) {
            // Update running average of analysis accuracy
            double currentAccuracy = averageAnalysisAccuracy.get();
            long completedCount = totalAnalysesCompleted.get();
            double newAccuracy = ((currentAccuracy * (completedCount - 1)) + accuracyScore) / completedCount;
            averageAnalysisAccuracy.set(newAccuracy);
        }
        
        logger.debug("Analysis completed with accuracy score: {}", accuracyScore);
    }

    /**
     * Update active analysis count
     */
    public void setActiveAnalyses(long count) {
        activeAnalyses.set(count);
    }

    /**
     * Update queued analysis count
     */
    public void setQueuedAnalyses(long count) {
        queuedAnalyses.set(count);
    }

    /**
     * Update provider performance score
     */
    public void updateProviderPerformance(String provider, double performanceScore) {
        providerPerformance.put(provider, performanceScore);
        logger.debug("Updated performance score for provider {}: {}", provider, performanceScore);
    }

    /**
     * Get comprehensive AI metrics
     */
    public AIMetricsResponse getMetrics(String timeRange) {
        AIMetricsResponse metrics = new AIMetricsResponse(serviceStatus.get());
        
        // Request statistics
        metrics.setTotalRequests(totalRequests.get());
        metrics.setSuccessfulRequests(successfulRequests.get());
        metrics.setFailedRequests(failedRequests.get());
        
        // Calculate success rate
        long total = totalRequests.get();
        if (total > 0) {
            double successRate = (successfulRequests.get() * 100.0) / total;
            metrics.setSuccessRate(Math.round(successRate * 100.0) / 100.0);
        } else {
            metrics.setSuccessRate(100.0);
        }
        
        // Performance metrics
        metrics.setAverageResponseTime(averageResponseTime.get());
        metrics.setP95ResponseTime(p95ResponseTime.get());
        metrics.setTotalAnalysesCompleted(totalAnalysesCompleted.get());
        metrics.setAverageAnalysisAccuracy(averageAnalysisAccuracy.get());
        
        // Error statistics
        Map<String, Long> currentErrorCounts = new HashMap<>();
        errorCounts.forEach((errorType, count) -> currentErrorCounts.put(errorType, count.get()));
        metrics.setErrorCounts(currentErrorCounts);
        
        // Provider performance
        metrics.setProviderPerformance(new HashMap<>(providerPerformance));
        
        // System status
        metrics.setLastHealthCheck(lastHealthCheck.get());
        metrics.setTimeRange(timeRange != null ? timeRange : "24h");
        metrics.setActiveAnalyses(Math.toIntExact(activeAnalyses.get()));
        metrics.setQueuedAnalyses(Math.toIntExact(queuedAnalyses.get()));
        
        // System load (mock implementation)
        metrics.setSystemLoad(calculateSystemLoad());
        metrics.setResourceUsage(getResourceUsage());
        
        return metrics;
    }

    /**
     * Perform health check for AI services
     */
    public boolean performHealthCheck() {
        logger.debug("Performing AI service health check");
        
        try {
            // TODO: Implement actual health checks for AI providers
            // This would include:
            // 1. Check connectivity to AI service endpoints
            // 2. Verify API keys and authentication
            // 3. Test with lightweight request
            // 4. Check response times and error rates
            
            // Mock health check logic
            boolean isHealthy = calculateHealthStatus();
            
            lastHealthCheck.set(LocalDateTime.now());
            
            if (isHealthy) {
                serviceStatus.set("healthy");
                logger.info("AI service health check passed");
                return true;
            } else {
                serviceStatus.set("degraded");
                logger.warn("AI service health check indicates degraded performance");
                return false;
            }
            
        } catch (Exception e) {
            serviceStatus.set("unavailable");
            logger.error("AI service health check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Spring Boot Actuator Health Indicator implementation
     */
    @Override
    public Health health() {
        boolean isHealthy = performHealthCheck();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("service_status", serviceStatus.get())
                    .withDetail("total_requests", totalRequests.get())
                    .withDetail("success_rate", calculateSuccessRate())
                    .withDetail("active_analyses", activeAnalyses.get())
                    .withDetail("last_check", lastHealthCheck.get())
                    .build();
        } else {
            return Health.down()
                    .withDetail("service_status", serviceStatus.get())
                    .withDetail("error_rate", calculateErrorRate())
                    .withDetail("last_error", getLastError())
                    .withDetail("last_check", lastHealthCheck.get())
                    .build();
        }
    }

    /**
     * Reset all metrics (useful for testing)
     */
    public void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalAnalysesCompleted.set(0);
        averageResponseTime.set(0.0);
        p95ResponseTime.set(0.0);
        averageAnalysisAccuracy.set(0.0);
        activeAnalyses.set(0);
        queuedAnalyses.set(0);
        
        errorCounts.values().forEach(counter -> counter.set(0));
        requestStartTimes.clear();
        responseTimes.clear();
        
        serviceStatus.set("healthy");
        logger.info("AI monitoring metrics reset");
    }

    // Private helper methods

    private void recordRequestEnd(Long requestId) {
        Long startTime = requestStartTimes.remove(requestId);
        if (startTime != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            responseTimes.put(requestId, responseTime);
            updateAverageResponseTime(responseTime);
        }
    }

    private void updateAverageResponseTime(long responseTime) {
        // Update running average response time
        double currentAverage = averageResponseTime.get();
        long totalRequests = this.totalRequests.get();
        double newAverage = ((currentAverage * (totalRequests - 1)) + responseTime) / totalRequests;
        averageResponseTime.set(newAverage);
        
        // Calculate P95 response time (simplified implementation)
        updateP95ResponseTime();
    }

    private void updateP95ResponseTime() {
        if (responseTimes.size() >= 20) { // Only calculate if we have enough samples
            long[] times = responseTimes.values().stream().mapToLong(Long::longValue).sorted().toArray();
            int p95Index = (int) Math.ceil(times.length * 0.95) - 1;
            p95ResponseTime.set((double) times[Math.max(0, p95Index)]);
        }
    }

    private void updateServiceStatus() {
        double successRate = calculateSuccessRate();
        long totalReqs = totalRequests.get();
        
        if (totalReqs == 0) {
            serviceStatus.set("healthy");
        } else if (successRate >= 95.0) {
            serviceStatus.set("healthy");
        } else if (successRate >= 80.0) {
            serviceStatus.set("degraded");
        } else {
            serviceStatus.set("unavailable");
        }
    }

    private boolean calculateHealthStatus() {
        // Mock health calculation based on success rate and response time
        double successRate = calculateSuccessRate();
        double avgResponseTime = averageResponseTime.get();
        
        return successRate >= 80.0 && avgResponseTime < 10000; // 10 second threshold
    }

    private double calculateSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) return 100.0;
        return (successfulRequests.get() * 100.0) / total;
    }

    private double calculateErrorRate() {
        long total = totalRequests.get();
        if (total == 0) return 0.0;
        return (failedRequests.get() * 100.0) / total;
    }

    private String getLastError() {
        // Find the most frequent error type
        return errorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                .map(Map.Entry::getKey)
                .orElse("none");
    }

    private double calculateSystemLoad() {
        // Mock system load calculation
        long active = activeAnalyses.get();
        long queued = queuedAnalyses.get();
        return Math.min(1.0, (active + queued) / 100.0);
    }

    private Map<String, Object> getResourceUsage() {
        // Mock resource usage data
        Map<String, Object> usage = new HashMap<>();
        usage.put("cpu_usage", Math.random() * 100);
        usage.put("memory_usage", Math.random() * 100);
        usage.put("disk_usage", Math.random() * 100);
        usage.put("network_io", Math.random() * 1000);
        return usage;
    }
}