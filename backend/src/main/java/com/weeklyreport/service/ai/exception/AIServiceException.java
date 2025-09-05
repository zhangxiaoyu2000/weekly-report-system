package com.weeklyreport.service.ai.exception;

/**
 * Exception thrown by AI services
 */
public class AIServiceException extends RuntimeException {
    
    private final String providerName;
    private final String errorCode;
    
    public AIServiceException(String message) {
        super(message);
        this.providerName = null;
        this.errorCode = null;
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.providerName = null;
        this.errorCode = null;
    }
    
    public AIServiceException(String message, String providerName, String errorCode) {
        super(message);
        this.providerName = providerName;
        this.errorCode = errorCode;
    }
    
    public AIServiceException(String message, Throwable cause, String providerName, String errorCode) {
        super(message, cause);
        this.providerName = providerName;
        this.errorCode = errorCode;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}