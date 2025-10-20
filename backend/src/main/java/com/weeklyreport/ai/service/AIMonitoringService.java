package com.weeklyreport.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI监控服务
 * 监控AI服务的性能和状态
 */
@Service
public class AIMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIMonitoringService.class);
    
    // 监控指标
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    
    // 性能统计
    private final Map<String, AtomicLong> requestsByType = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> responseTimesByType = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();
    
    // 状态信息
    private LocalDateTime lastRequestTime;
    private LocalDateTime serviceStartTime;
    
    /**
     * 构造函数
     */
    public AIMonitoringService() {
        this.serviceStartTime = LocalDateTime.now();
        logger.info("AI Monitoring Service initialized at {}", serviceStartTime);
    }
    
    /**
     * 记录请求开始
     * @param requestType 请求类型
     * @return 请求ID（用于跟踪）
     */
    public String recordRequestStart(String requestType) {
        String requestId = generateRequestId();
        
        totalRequests.incrementAndGet();
        activeRequests.incrementAndGet();
        requestsByType.computeIfAbsent(requestType, k -> new AtomicLong(0)).incrementAndGet();
        lastRequestTime = LocalDateTime.now();
        
        logger.debug("AI request started - ID: {}, Type: {}, Active: {}", 
                    requestId, requestType, activeRequests.get());
        
        return requestId;
    }
    
    /**
     * 记录请求完成
     * @param requestId 请求ID
     * @param requestType 请求类型
     * @param responseTimeMs 响应时间（毫秒）
     * @param success 是否成功
     */
    public void recordRequestComplete(String requestId, String requestType, long responseTimeMs, boolean success) {
        activeRequests.decrementAndGet();
        
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
            errorsByType.computeIfAbsent(requestType, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        responseTimesByType.computeIfAbsent(requestType, k -> new AtomicLong(0)).addAndGet(responseTimeMs);
        
        logger.debug("AI request completed - ID: {}, Type: {}, Time: {}ms, Success: {}, Active: {}", 
                    requestId, requestType, responseTimeMs, success, activeRequests.get());
    }
    
    /**
     * 记录请求成功
     * @param requestId 请求ID
     */
    public void recordRequestSuccess(Long requestId) {
        recordRequestSuccess(String.valueOf(requestId));
    }
    
    /**
     * 记录请求成功
     * @param requestId 请求ID
     */
    public void recordRequestSuccess(String requestId) {
        successfulRequests.incrementAndGet();
        logger.debug("AI request succeeded - ID: {}", requestId);
    }
    
    /**
     * 记录请求失败
     * @param requestId 请求ID
     * @param errorMessage 错误消息
     */
    public void recordRequestFailure(Long requestId, String errorMessage) {
        recordRequestFailure(String.valueOf(requestId), errorMessage);
    }
    
    /**
     * 记录请求失败
     * @param requestId 请求ID
     * @param errorMessage 错误消息
     */
    public void recordRequestFailure(String requestId, String errorMessage) {
        failedRequests.incrementAndGet();
        logger.debug("AI request failed - ID: {}, Error: {}", requestId, errorMessage);
    }
    
    /**
     * 记录错误
     * @param requestType 请求类型
     * @param error 错误信息
     */
    public void recordError(String requestType, String error) {
        failedRequests.incrementAndGet();
        errorsByType.computeIfAbsent(requestType, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.error("AI service error - Type: {}, Error: {}", requestType, error);
    }
    
    /**
     * 获取监控指标
     * @return 监控指标Map
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());
        metrics.put("activeRequests", activeRequests.get());
        metrics.put("successRate", calculateSuccessRate());
        metrics.put("requestsByType", requestsByType);
        metrics.put("errorsByType", errorsByType);
        metrics.put("averageResponseTimes", calculateAverageResponseTimes());
        metrics.put("serviceUptime", getServiceUptime());
        metrics.put("lastRequestTime", lastRequestTime);
        
        return metrics;
    }
    
    /**
     * 获取健康状态
     * @return 健康状态信息
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new ConcurrentHashMap<>();
        
        double successRate = calculateSuccessRate();
        int activeCount = activeRequests.get();
        
        // 判断健康状态
        String status = "UP";
        if (successRate < 0.8) {
            status = "DOWN";
        } else if (successRate < 0.9 || activeCount > 10) {
            status = "WARN";
        }
        
        health.put("status", status);
        health.put("successRate", successRate);
        health.put("activeRequests", activeCount);
        health.put("uptime", getServiceUptime());
        
        return health;
    }
    
    /**
     * 重置统计信息
     */
    public void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        activeRequests.set(0);
        requestsByType.clear();
        responseTimesByType.clear();
        errorsByType.clear();
        serviceStartTime = LocalDateTime.now();
        lastRequestTime = null;
        
        logger.info("AI monitoring metrics reset at {}", serviceStartTime);
    }
    
    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) return 1.0;
        
        return (double) successfulRequests.get() / total;
    }
    
    /**
     * 计算平均响应时间
     */
    private Map<String, Double> calculateAverageResponseTimes() {
        Map<String, Double> averages = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, AtomicLong> entry : responseTimesByType.entrySet()) {
            String type = entry.getKey();
            long totalTime = entry.getValue().get();
            long requestCount = requestsByType.getOrDefault(type, new AtomicLong(0)).get();
            
            if (requestCount > 0) {
                averages.put(type, (double) totalTime / requestCount);
            }
        }
        
        return averages;
    }
    
    /**
     * 获取服务运行时间
     */
    private long getServiceUptime() {
        return java.time.Duration.between(serviceStartTime, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "AI-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}