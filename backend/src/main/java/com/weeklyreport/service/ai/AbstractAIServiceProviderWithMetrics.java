package com.weeklyreport.service.ai;

import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;
import com.weeklyreport.service.ai.monitoring.AIMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for AI service providers with metrics integration
 */
public abstract class AbstractAIServiceProviderWithMetrics implements AIServiceProvider {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired(required = false)
    protected AIMetricsService metricsService;
    
    @Override
    @Async("aiTaskExecutor")
    public CompletableFuture<AIAnalysisResponse> analyzeAsync(AIAnalysisRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Starting async AI analysis for type: {}", request.getAnalysisType());
                AIAnalysisResponse response = analyze(request);
                logger.debug("Completed async AI analysis with ID: {}", response.getAnalysisId());
                return response;
            } catch (Exception e) {
                logger.error("Async AI analysis failed", e);
                throw new AIServiceException("Async analysis failed: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public AIAnalysisResponse analyze(AIAnalysisRequest request) throws AIServiceException {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting AI analysis - Type: {}, Provider: {}", 
                       request.getAnalysisType(), getProviderName());
            
            // Validate request
            validateRequest(request);
            
            // Check service availability
            if (!isAvailable()) {
                throw new AIServiceException("AI service is not available: " + getProviderName());
            }
            
            // Perform the actual analysis
            AIAnalysisResponse response = performAnalysis(request);
            
            // Set common response metadata
            response.setAnalysisId(UUID.randomUUID().toString());
            response.setProviderUsed(getProviderName());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            logger.info("AI analysis completed - ID: {}, Time: {}ms", 
                       response.getAnalysisId(), response.getProcessingTimeMs());
            
            // Record success metrics
            if (metricsService != null) {
                metricsService.recordSuccess(getServiceType(), request, response);
            }
            
            return response;
            
        } catch (AIServiceException e) {
            logger.error("AI analysis failed for provider {}: {}", getProviderName(), e.getMessage());
            
            // Record error metrics
            if (metricsService != null) {
                metricsService.recordError(getServiceType(), request, e.getMessage(), 
                                         System.currentTimeMillis() - startTime);
            }
            
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during AI analysis", e);
            
            // Record error metrics
            if (metricsService != null) {
                metricsService.recordError(getServiceType(), request, e.getMessage(), 
                                         System.currentTimeMillis() - startTime);
            }
            
            throw new AIServiceException("Analysis failed due to unexpected error", e, 
                                       getProviderName(), "UNEXPECTED_ERROR");
        }
    }
    
    /**
     * Validate the analysis request
     */
    protected void validateRequest(AIAnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Analysis request cannot be null");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (request.getAnalysisType() == null) {
            throw new IllegalArgumentException("Analysis type cannot be null");
        }
        
        // Check content length against provider limits
        if (request.getContent().length() > getMaxTokens() * 4) { // Rough estimate: 4 chars per token
            throw new IllegalArgumentException("Content exceeds maximum token limit for " + getProviderName());
        }
    }
    
    /**
     * Perform the actual analysis - to be implemented by concrete providers
     */
    protected abstract AIAnalysisResponse performAnalysis(AIAnalysisRequest request) throws AIServiceException;
}