package com.weeklyreport.service.ai.monitoring;

import com.weeklyreport.service.ai.AIServiceType;
import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for collecting and managing AI service metrics
 */
@Service
public class AIMetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIMetricsService.class);
    
    // Metrics storage
    private final Map<AIServiceType, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<AIServiceType, AtomicInteger> successCounts = new ConcurrentHashMap<>();
    private final Map<AIServiceType, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final Map<AIServiceType, AtomicLong> totalProcessingTime = new ConcurrentHashMap<>();
    private final Map<AIAnalysisRequest.AnalysisType, AtomicInteger> analysisTypeCounts = new ConcurrentHashMap<>();
    private final List<AIAnalysisEvent> recentEvents = new ArrayList<>();
    
    private final Object eventsLock = new Object();
    
    /**
     * Record a successful AI analysis
     */
    public void recordSuccess(AIServiceType serviceType, AIAnalysisRequest request, 
                             AIAnalysisResponse response) {
        // Increment counters
        requestCounts.computeIfAbsent(serviceType, k -> new AtomicInteger(0)).incrementAndGet();
        successCounts.computeIfAbsent(serviceType, k -> new AtomicInteger(0)).incrementAndGet();
        analysisTypeCounts.computeIfAbsent(request.getAnalysisType(), k -> new AtomicInteger(0))
                         .incrementAndGet();
        
        // Record processing time
        if (response.getProcessingTimeMs() != null) {
            totalProcessingTime.computeIfAbsent(serviceType, k -> new AtomicLong(0))
                              .addAndGet(response.getProcessingTimeMs());
        }
        
        // Record event
        recordEvent(new AIAnalysisEvent(
            serviceType, 
            request.getAnalysisType(), 
            true, 
            response.getProcessingTimeMs(),
            null,
            response.getAnalysisId()
        ));
        
        logger.debug("Recorded successful AI analysis - Provider: {}, Type: {}, Time: {}ms", 
                    serviceType, request.getAnalysisType(), response.getProcessingTimeMs());
    }
    
    /**
     * Record a failed AI analysis
     */
    public void recordError(AIServiceType serviceType, AIAnalysisRequest request, 
                           String errorMessage, Long processingTime) {
        // Increment counters
        requestCounts.computeIfAbsent(serviceType, k -> new AtomicInteger(0)).incrementAndGet();
        errorCounts.computeIfAbsent(serviceType, k -> new AtomicInteger(0)).incrementAndGet();
        
        if (processingTime != null) {
            totalProcessingTime.computeIfAbsent(serviceType, k -> new AtomicLong(0))
                              .addAndGet(processingTime);
        }
        
        // Record event
        recordEvent(new AIAnalysisEvent(
            serviceType, 
            request.getAnalysisType(), 
            false, 
            processingTime,
            errorMessage,
            null
        ));
        
        logger.warn("Recorded failed AI analysis - Provider: {}, Type: {}, Error: {}", 
                   serviceType, request.getAnalysisType(), errorMessage);
    }
    
    /**
     * Get comprehensive metrics summary
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Overall totals
        int totalRequests = requestCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        int totalSuccesses = successCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        int totalErrors = errorCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        
        summary.put("totalRequests", totalRequests);
        summary.put("totalSuccesses", totalSuccesses);
        summary.put("totalErrors", totalErrors);
        summary.put("successRate", totalRequests > 0 ? (double) totalSuccesses / totalRequests : 0.0);
        
        // Per-provider metrics
        Map<String, Object> providerMetrics = new HashMap<>();
        for (AIServiceType serviceType : AIServiceType.values()) {
            Map<String, Object> providerStats = new HashMap<>();
            
            int requests = requestCounts.getOrDefault(serviceType, new AtomicInteger(0)).get();
            int successes = successCounts.getOrDefault(serviceType, new AtomicInteger(0)).get();
            int errors = errorCounts.getOrDefault(serviceType, new AtomicInteger(0)).get();
            long totalTime = totalProcessingTime.getOrDefault(serviceType, new AtomicLong(0)).get();
            
            providerStats.put("requests", requests);
            providerStats.put("successes", successes);
            providerStats.put("errors", errors);
            providerStats.put("successRate", requests > 0 ? (double) successes / requests : 0.0);
            providerStats.put("averageProcessingTime", successes > 0 ? (double) totalTime / successes : 0.0);
            
            providerMetrics.put(serviceType.getCode(), providerStats);
        }
        summary.put("providerMetrics", providerMetrics);
        
        // Analysis type metrics
        Map<String, Object> analysisTypeMetrics = new HashMap<>();
        for (AIAnalysisRequest.AnalysisType analysisType : AIAnalysisRequest.AnalysisType.values()) {
            int count = analysisTypeCounts.getOrDefault(analysisType, new AtomicInteger(0)).get();
            analysisTypeMetrics.put(analysisType.name(), count);
        }
        summary.put("analysisTypeMetrics", analysisTypeMetrics);
        
        // Recent events summary
        synchronized (eventsLock) {
            summary.put("recentEventsCount", recentEvents.size());
            
            // Last 10 events
            List<Map<String, Object>> recentEventsSummary = new ArrayList<>();
            int start = Math.max(0, recentEvents.size() - 10);
            for (int i = start; i < recentEvents.size(); i++) {
                AIAnalysisEvent event = recentEvents.get(i);
                Map<String, Object> eventSummary = new HashMap<>();
                eventSummary.put("timestamp", event.getTimestamp());
                eventSummary.put("serviceType", event.getServiceType().getCode());
                eventSummary.put("analysisType", event.getAnalysisType().name());
                eventSummary.put("success", event.isSuccess());
                eventSummary.put("processingTime", event.getProcessingTime());
                if (!event.isSuccess()) {
                    eventSummary.put("errorMessage", event.getErrorMessage());
                }
                recentEventsSummary.add(eventSummary);
            }
            summary.put("recentEvents", recentEventsSummary);
        }
        
        summary.put("collectedAt", LocalDateTime.now());
        
        return summary;
    }
    
    /**
     * Get metrics for a specific provider
     */
    public Map<String, Object> getProviderMetrics(AIServiceType serviceType) {
        Map<String, Object> metrics = new HashMap<>();
        
        int requests = requestCounts.getOrDefault(serviceType, new AtomicInteger(0)).get();
        int successes = successCounts.getOrDefault(serviceType, new AtomicInteger(0)).get();
        int errors = errorCounts.getOrDefault(serviceType, new AtomicInteger(0)).get();
        long totalTime = totalProcessingTime.getOrDefault(serviceType, new AtomicLong(0)).get();
        
        metrics.put("requests", requests);
        metrics.put("successes", successes);
        metrics.put("errors", errors);
        metrics.put("successRate", requests > 0 ? (double) successes / requests : 0.0);
        metrics.put("averageProcessingTime", successes > 0 ? (double) totalTime / successes : 0.0);
        metrics.put("totalProcessingTime", totalTime);
        
        return metrics;
    }
    
    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        requestCounts.clear();
        successCounts.clear();
        errorCounts.clear();
        totalProcessingTime.clear();
        analysisTypeCounts.clear();
        
        synchronized (eventsLock) {
            recentEvents.clear();
        }
        
        logger.info("AI metrics have been reset");
    }
    
    /**
     * Get health status based on recent performance
     */
    public String getHealthStatus() {
        synchronized (eventsLock) {
            if (recentEvents.isEmpty()) {
                return "NO_DATA";
            }
            
            // Check last 10 events or events from last hour
            LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            List<AIAnalysisEvent> recentRelevantEvents = recentEvents.stream()
                    .filter(event -> event.getTimestamp().isAfter(oneHourAgo))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            
            if (recentRelevantEvents.isEmpty()) {
                return "NO_RECENT_DATA";
            }
            
            long successCount = recentRelevantEvents.stream().mapToLong(event -> event.isSuccess() ? 1 : 0).sum();
            double successRate = (double) successCount / recentRelevantEvents.size();
            
            if (successRate >= 0.95) {
                return "HEALTHY";
            } else if (successRate >= 0.80) {
                return "DEGRADED";
            } else {
                return "UNHEALTHY";
            }
        }
    }
    
    /**
     * Record an analysis event
     */
    private void recordEvent(AIAnalysisEvent event) {
        synchronized (eventsLock) {
            recentEvents.add(event);
            
            // Keep only last 1000 events to prevent memory issues
            if (recentEvents.size() > 1000) {
                recentEvents.subList(0, recentEvents.size() - 1000).clear();
            }
        }
    }
    
    /**
     * AI Analysis Event
     */
    private static class AIAnalysisEvent {
        private final LocalDateTime timestamp;
        private final AIServiceType serviceType;
        private final AIAnalysisRequest.AnalysisType analysisType;
        private final boolean success;
        private final Long processingTime;
        private final String errorMessage;
        private final String analysisId;
        
        public AIAnalysisEvent(AIServiceType serviceType, AIAnalysisRequest.AnalysisType analysisType,
                              boolean success, Long processingTime, String errorMessage, String analysisId) {
            this.timestamp = LocalDateTime.now();
            this.serviceType = serviceType;
            this.analysisType = analysisType;
            this.success = success;
            this.processingTime = processingTime;
            this.errorMessage = errorMessage;
            this.analysisId = analysisId;
        }
        
        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public AIServiceType getServiceType() { return serviceType; }
        public AIAnalysisRequest.AnalysisType getAnalysisType() { return analysisType; }
        public boolean isSuccess() { return success; }
        public Long getProcessingTime() { return processingTime; }
        public String getErrorMessage() { return errorMessage; }
        public String getAnalysisId() { return analysisId; }
    }
}