package com.weeklyreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;

/**
 * Configuration properties for AI services
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@EnableAsync
@Validated
public class AIConfig {
    
    /**
     * Default AI service provider to use
     */
    @NotBlank
    private String defaultProvider = "openai";
    
    /**
     * Whether AI services are enabled
     */
    private boolean enabled = true;
    
    /**
     * Fallback to mock service if primary service fails
     */
    private boolean enableFallback = true;
    
    /**
     * Maximum retry attempts for AI service calls
     */
    @Positive
    private int maxRetries = 3;
    
    /**
     * Timeout for AI service calls in milliseconds
     */
    @Positive
    private long timeoutMs = 30000; // 30 seconds
    
    /**
     * OpenAI configuration
     */
    @Valid
    @NotNull
    private OpenAIConfig openai = new OpenAIConfig();
    
    /**
     * Anthropic configuration
     */
    @Valid
    @NotNull
    private AnthropicConfig anthropic = new AnthropicConfig();
    
    /**
     * DeepSeek configuration
     */
    @Valid
    @NotNull
    private DeepSeekConfig deepseek = new DeepSeekConfig();
    
    /**
     * Local AI configuration
     */
    @Valid
    @NotNull
    private LocalAIConfig local = new LocalAIConfig();
    
    /**
     * Mock AI configuration
     */
    @Valid
    @NotNull
    private MockAIConfig mock = new MockAIConfig();
    
    // Getters and setters
    public String getDefaultProvider() {
        return defaultProvider;
    }
    
    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnableFallback() {
        return enableFallback;
    }
    
    public void setEnableFallback(boolean enableFallback) {
        this.enableFallback = enableFallback;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public long getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public OpenAIConfig getOpenai() {
        return openai;
    }
    
    public void setOpenai(OpenAIConfig openai) {
        this.openai = openai;
    }
    
    public AnthropicConfig getAnthropic() {
        return anthropic;
    }
    
    public void setAnthropic(AnthropicConfig anthropic) {
        this.anthropic = anthropic;
    }
    
    public LocalAIConfig getLocal() {
        return local;
    }
    
    public void setLocal(LocalAIConfig local) {
        this.local = local;
    }
    
    public DeepSeekConfig getDeepseek() {
        return deepseek;
    }
    
    public void setDeepseek(DeepSeekConfig deepseek) {
        this.deepseek = deepseek;
    }
    
    public MockAIConfig getMock() {
        return mock;
    }
    
    public void setMock(MockAIConfig mock) {
        this.mock = mock;
    }
    
    /**
     * OpenAI specific configuration
     */
    public static class OpenAIConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-3.5-turbo";
        private double temperature = 0.7;
        private int maxTokens = 4096;
        private boolean enabled = false;
        
        // Getters and setters
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * Anthropic specific configuration
     */
    public static class AnthropicConfig {
        private String apiKey;
        private String baseUrl = "https://api.anthropic.com/v1";
        private String model = "claude-3-sonnet-20240229";
        private double temperature = 0.7;
        private int maxTokens = 4096;
        private boolean enabled = false;
        
        // Getters and setters
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * Local AI configuration
     */
    public static class LocalAIConfig {
        private String endpoint = "http://localhost:8000";
        private String model = "local-model";
        private double temperature = 0.7;
        private int maxTokens = 2048;
        private boolean enabled = false;
        
        // Getters and setters
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * Mock AI configuration for testing
     */
    public static class MockAIConfig {
        private boolean enabled = true;
        private long simulatedDelay = 1000; // 1 second
        private double confidence = 0.95;
        
        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public long getSimulatedDelay() {
            return simulatedDelay;
        }
        
        public void setSimulatedDelay(long simulatedDelay) {
            this.simulatedDelay = simulatedDelay;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
    
    /**
     * DeepSeek specific configuration
     */
    public static class DeepSeekConfig {
        private String apiKey;
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private double temperature = 0.7;
        private int maxTokens = 2000;
        private boolean enabled = true;
        
        // Getters and setters
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}