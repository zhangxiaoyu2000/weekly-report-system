package com.weeklyreport.service.ai;

import com.weeklyreport.service.ai.dto.AIAnalysisRequest;
import com.weeklyreport.service.ai.dto.AIAnalysisResponse;
import com.weeklyreport.service.ai.exception.AIServiceException;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract interface for AI service providers
 */
public interface AIServiceProvider {
    
    /**
     * Get the AI service type supported by this provider
     */
    AIServiceType getServiceType();
    
    /**
     * Check if the service is available and configured properly
     */
    boolean isAvailable();
    
    /**
     * Perform AI analysis synchronously
     * 
     * @param request Analysis request
     * @return Analysis response
     * @throws AIServiceException if analysis fails
     */
    AIAnalysisResponse analyze(AIAnalysisRequest request) throws AIServiceException;
    
    /**
     * Perform AI analysis asynchronously
     * 
     * @param request Analysis request
     * @return CompletableFuture with analysis response
     */
    CompletableFuture<AIAnalysisResponse> analyzeAsync(AIAnalysisRequest request);
    
    /**
     * Get provider-specific configuration status
     */
    String getConfigurationStatus();
    
    /**
     * Get the display name of this provider
     */
    default String getProviderName() {
        return getServiceType().getDisplayName();
    }
    
    /**
     * Get the maximum tokens supported by this provider
     */
    int getMaxTokens();
    
    /**
     * Get the cost estimate for analysis (in USD cents)
     */
    default double getCostEstimate(String content) {
        return 0.0; // Default implementation returns 0
    }
}