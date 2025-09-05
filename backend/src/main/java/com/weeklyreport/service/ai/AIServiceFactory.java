package com.weeklyreport.service.ai;

import com.weeklyreport.config.AIConfig;
import com.weeklyreport.service.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating and managing AI service providers
 */
@Component
public class AIServiceFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(AIServiceFactory.class);
    
    private final AIConfig aiConfig;
    private final ApplicationContext applicationContext;
    private final Map<AIServiceType, AIServiceProvider> providerCache = new HashMap<>();
    
    @Autowired
    public AIServiceFactory(AIConfig aiConfig, ApplicationContext applicationContext) {
        this.aiConfig = aiConfig;
        this.applicationContext = applicationContext;
        initializeProviders();
    }
    
    /**
     * Get the default AI service provider
     */
    public AIServiceProvider getDefaultProvider() {
        try {
            AIServiceType defaultType = AIServiceType.fromCode(aiConfig.getDefaultProvider());
            return getProvider(defaultType);
        } catch (Exception e) {
            logger.warn("Failed to get default provider {}, falling back to available provider", 
                       aiConfig.getDefaultProvider(), e);
            return getAnyAvailableProvider();
        }
    }
    
    /**
     * Get AI service provider by type
     */
    public AIServiceProvider getProvider(AIServiceType type) {
        AIServiceProvider provider = providerCache.get(type);
        if (provider == null) {
            throw new AIServiceException("No provider found for type: " + type);
        }
        
        if (!provider.isAvailable()) {
            if (aiConfig.isEnableFallback()) {
                logger.warn("Provider {} is not available, attempting fallback", type);
                return getFallbackProvider(type);
            } else {
                throw new AIServiceException("Provider " + type + " is not available and fallback is disabled");
            }
        }
        
        return provider;
    }
    
    /**
     * Get provider by code string
     */
    public AIServiceProvider getProvider(String providerCode) {
        try {
            AIServiceType type = AIServiceType.fromCode(providerCode);
            return getProvider(type);
        } catch (IllegalArgumentException e) {
            throw new AIServiceException("Unknown provider code: " + providerCode, e);
        }
    }
    
    /**
     * Get all available providers
     */
    public Map<AIServiceType, AIServiceProvider> getAllProviders() {
        Map<AIServiceType, AIServiceProvider> availableProviders = new HashMap<>();
        for (Map.Entry<AIServiceType, AIServiceProvider> entry : providerCache.entrySet()) {
            if (entry.getValue().isAvailable()) {
                availableProviders.put(entry.getKey(), entry.getValue());
            }
        }
        return availableProviders;
    }
    
    /**
     * Get provider status information
     */
    public Map<String, Object> getProviderStatus() {
        Map<String, Object> status = new HashMap<>();
        
        for (Map.Entry<AIServiceType, AIServiceProvider> entry : providerCache.entrySet()) {
            AIServiceProvider provider = entry.getValue();
            Map<String, Object> providerStatus = new HashMap<>();
            providerStatus.put("available", provider.isAvailable());
            providerStatus.put("configurationStatus", provider.getConfigurationStatus());
            providerStatus.put("maxTokens", provider.getMaxTokens());
            providerStatus.put("providerName", provider.getProviderName());
            
            status.put(entry.getKey().getCode(), providerStatus);
        }
        
        status.put("defaultProvider", aiConfig.getDefaultProvider());
        status.put("fallbackEnabled", aiConfig.isEnableFallback());
        status.put("enabled", aiConfig.isEnabled());
        
        return status;
    }
    
    /**
     * Check if AI services are enabled and any provider is available
     */
    public boolean isAIEnabled() {
        if (!aiConfig.isEnabled()) {
            return false;
        }
        
        return providerCache.values().stream()
                .anyMatch(AIServiceProvider::isAvailable);
    }
    
    /**
     * Initialize all available providers
     */
    private void initializeProviders() {
        logger.info("Initializing AI service providers...");
        
        // Get all AIServiceProvider beans from application context
        Map<String, AIServiceProvider> providerBeans = applicationContext.getBeansOfType(AIServiceProvider.class);
        
        for (AIServiceProvider provider : providerBeans.values()) {
            AIServiceType type = provider.getServiceType();
            providerCache.put(type, provider);
            
            logger.info("Registered AI provider: {} - Status: {}", 
                       type.getDisplayName(), provider.getConfigurationStatus());
        }
        
        logger.info("Initialized {} AI service providers", providerCache.size());
    }
    
    /**
     * Get fallback provider when primary provider is not available
     */
    private AIServiceProvider getFallbackProvider(AIServiceType originalType) {
        // First try mock provider if available
        if (originalType != AIServiceType.MOCK) {
            AIServiceProvider mockProvider = providerCache.get(AIServiceType.MOCK);
            if (mockProvider != null && mockProvider.isAvailable()) {
                logger.info("Using mock provider as fallback for {}", originalType);
                return mockProvider;
            }
        }
        
        // Then try any other available provider
        AIServiceProvider fallback = getAnyAvailableProvider();
        if (fallback != null && fallback.getServiceType() != originalType) {
            logger.info("Using {} as fallback for {}", fallback.getServiceType(), originalType);
            return fallback;
        }
        
        throw new AIServiceException("No fallback provider available for " + originalType);
    }
    
    /**
     * Get any available provider
     */
    private AIServiceProvider getAnyAvailableProvider() {
        Optional<AIServiceProvider> availableProvider = providerCache.values().stream()
                .filter(AIServiceProvider::isAvailable)
                .findFirst();
        
        if (availableProvider.isPresent()) {
            return availableProvider.get();
        }
        
        throw new AIServiceException("No AI service providers are available");
    }
}