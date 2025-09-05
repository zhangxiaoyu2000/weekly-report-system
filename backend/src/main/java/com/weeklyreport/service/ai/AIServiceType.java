package com.weeklyreport.service.ai;

/**
 * Enumeration of supported AI service providers
 */
public enum AIServiceType {
    /**
     * OpenAI service (GPT-3.5, GPT-4)
     */
    OPENAI("openai", "OpenAI GPT Service"),
    
    /**
     * Anthropic Claude service
     */
    ANTHROPIC("anthropic", "Anthropic Claude Service"),
    
    /**
     * Local AI model service
     */
    LOCAL("local", "Local AI Model Service"),
    
    /**
     * Mock AI service for testing
     */
    MOCK("mock", "Mock AI Service for Testing");
    
    private final String code;
    private final String displayName;
    
    AIServiceType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Find AI service type by code
     */
    public static AIServiceType fromCode(String code) {
        for (AIServiceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AI service type: " + code);
    }
}