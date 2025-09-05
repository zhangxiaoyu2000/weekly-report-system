package com.weeklyreport.service.ai;

import com.weeklyreport.config.AIConfig;
import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main AI analysis service that coordinates different AI providers
 */
@Service
public class AIAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);
    
    private final AIServiceFactory aiServiceFactory;
    private final AIConfig aiConfig;
    private final RetryTemplate retryTemplate;
    
    @Autowired
    public AIAnalysisService(AIServiceFactory aiServiceFactory, 
                           AIConfig aiConfig,
                           @Qualifier("aiRetryTemplate") RetryTemplate retryTemplate) {
        this.aiServiceFactory = aiServiceFactory;
        this.aiConfig = aiConfig;
        this.retryTemplate = retryTemplate;
    }
    
    /**
     * Perform AI analysis with automatic provider selection and retry logic
     */
    @Retryable(
        value = {AIServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AIAnalysisResponse analyzeContent(AIAnalysisRequest request) throws AIServiceException {
        return analyzeContent(request, null);
    }
    
    /**
     * Perform AI analysis with specific provider
     */
    @Retryable(
        value = {AIServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AIAnalysisResponse analyzeContent(AIAnalysisRequest request, String providerCode) 
            throws AIServiceException {
        
        if (!aiConfig.isEnabled()) {
            throw new AIServiceException("AI services are disabled");
        }
        
        logger.info("Starting AI analysis - Type: {}, Provider: {}", 
                   request.getAnalysisType(), providerCode != null ? providerCode : "default");
        
        return retryTemplate.execute(context -> {
            try {
                AIServiceProvider provider = providerCode != null 
                    ? aiServiceFactory.getProvider(providerCode)
                    : aiServiceFactory.getDefaultProvider();
                
                AIAnalysisResponse response = provider.analyze(request);
                
                logger.info("AI analysis completed successfully - ID: {}, Provider: {}", 
                           response.getAnalysisId(), response.getProviderUsed());
                
                return response;
                
            } catch (AIServiceException e) {
                logger.error("AI analysis attempt {} failed: {}", 
                           context.getRetryCount() + 1, e.getMessage());
                throw e;
            }
        });
    }
    
    /**
     * Perform AI analysis asynchronously
     */
    public CompletableFuture<AIAnalysisResponse> analyzeContentAsync(AIAnalysisRequest request) {
        return analyzeContentAsync(request, null);
    }
    
    /**
     * Perform AI analysis asynchronously with specific provider
     */
    public CompletableFuture<AIAnalysisResponse> analyzeContentAsync(AIAnalysisRequest request, 
                                                                    String providerCode) {
        if (!aiConfig.isEnabled()) {
            CompletableFuture<AIAnalysisResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new AIServiceException("AI services are disabled"));
            return future;
        }
        
        logger.info("Starting async AI analysis - Type: {}, Provider: {}", 
                   request.getAnalysisType(), providerCode != null ? providerCode : "default");
        
        try {
            AIServiceProvider provider = providerCode != null 
                ? aiServiceFactory.getProvider(providerCode)
                : aiServiceFactory.getDefaultProvider();
            
            return provider.analyzeAsync(request)
                .whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        logger.error("Async AI analysis failed", throwable);
                    } else {
                        logger.info("Async AI analysis completed - ID: {}, Provider: {}", 
                                   response.getAnalysisId(), response.getProviderUsed());
                    }
                });
                
        } catch (Exception e) {
            logger.error("Failed to start async AI analysis", e);
            CompletableFuture<AIAnalysisResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new AIServiceException("Failed to start async analysis", e));
            return future;
        }
    }
    
    /**
     * Get available AI providers and their status
     */
    public Map<String, Object> getProvidersStatus() {
        return aiServiceFactory.getProviderStatus();
    }
    
    /**
     * Check if AI services are available
     */
    public boolean isAIAvailable() {
        return aiConfig.isEnabled() && aiServiceFactory.isAIEnabled();
    }
    
    /**
     * Get cost estimate for analysis
     */
    public double getCostEstimate(String content, String providerCode) {
        try {
            AIServiceProvider provider = providerCode != null 
                ? aiServiceFactory.getProvider(providerCode)
                : aiServiceFactory.getDefaultProvider();
            
            return provider.getCostEstimate(content);
        } catch (Exception e) {
            logger.warn("Failed to get cost estimate: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Validate analysis request
     */
    public void validateRequest(AIAnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Analysis request cannot be null");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (request.getAnalysisType() == null) {
            throw new IllegalArgumentException("Analysis type cannot be null");
        }
    }
}