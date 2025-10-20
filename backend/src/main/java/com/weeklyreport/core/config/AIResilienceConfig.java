package com.weeklyreport.core.config;

import com.weeklyreport.ai.exception.AIServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for AI service resilience patterns
 */
@Configuration
@EnableRetry
public class AIResilienceConfig {
    
    /**
     * REST template with timeout configuration for AI service calls
     */
    @Bean("aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure timeouts
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "WeeklyReport-AI-Client/1.0");
            request.getHeaders().add("Accept", "application/json");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
    
    /**
     * Retry template for AI service calls
     */
    @Bean("aiRetryTemplate")
    public RetryTemplate aiRetryTemplate(AIConfig aiConfig) {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configure retry policy
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(ResourceAccessException.class, true);
        retryableExceptions.put(HttpServerErrorException.class, true);
        retryableExceptions.put(AIServiceException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(aiConfig.getMaxRetries(), retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Configure backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000); // 10 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}