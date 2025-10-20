package com.weeklyreport.ai.exception;

/**
 * AI服务异常类
 * 统一处理AI模块相关的异常
 */
public class AIServiceException extends RuntimeException {
    
    private final String errorCode;
    private final Object data;
    
    /**
     * 构造函数
     * @param message 异常消息
     */
    public AIServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.data = null;
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param cause 异常原因
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.data = null;
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误代码
     */
    public AIServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误代码
     * @param cause 异常原因
     */
    public AIServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = null;
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误代码
     * @param data 附加数据
     */
    public AIServiceException(String message, String errorCode, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }
    
    /**
     * 构造函数
     * @param message 异常消息
     * @param errorCode 错误代码
     * @param data 附加数据
     * @param cause 异常原因
     */
    public AIServiceException(String message, String errorCode, Object data, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = data;
    }
    
    /**
     * 获取错误代码
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取附加数据
     * @return 附加数据
     */
    public Object getData() {
        return data;
    }
    
    // 预定义的异常类型
    
    /**
     * AI服务不可用异常
     */
    public static class AIServiceUnavailableException extends AIServiceException {
        public AIServiceUnavailableException(String message) {
            super(message, "AI_SERVICE_UNAVAILABLE");
        }
        
        public AIServiceUnavailableException(String message, Throwable cause) {
            super(message, "AI_SERVICE_UNAVAILABLE", cause);
        }
    }
    
    /**
     * AI分析失败异常
     */
    public static class AIAnalysisFailedException extends AIServiceException {
        public AIAnalysisFailedException(String message) {
            super(message, "AI_ANALYSIS_FAILED");
        }
        
        public AIAnalysisFailedException(String message, Throwable cause) {
            super(message, "AI_ANALYSIS_FAILED", cause);
        }
        
        public AIAnalysisFailedException(String message, Object data) {
            super(message, "AI_ANALYSIS_FAILED", data);
        }
    }
    
    /**
     * AI配置错误异常
     */
    public static class AIConfigurationException extends AIServiceException {
        public AIConfigurationException(String message) {
            super(message, "AI_CONFIGURATION_ERROR");
        }
        
        public AIConfigurationException(String message, Throwable cause) {
            super(message, "AI_CONFIGURATION_ERROR", cause);
        }
    }
    
    /**
     * AI请求限制异常
     */
    public static class AIRateLimitException extends AIServiceException {
        public AIRateLimitException(String message) {
            super(message, "AI_RATE_LIMIT_EXCEEDED");
        }
        
        public AIRateLimitException(String message, Object data) {
            super(message, "AI_RATE_LIMIT_EXCEEDED", data);
        }
    }
    
    /**
     * AI输入验证异常
     */
    public static class AIInputValidationException extends AIServiceException {
        public AIInputValidationException(String message) {
            super(message, "AI_INPUT_VALIDATION_ERROR");
        }
        
        public AIInputValidationException(String message, Object data) {
            super(message, "AI_INPUT_VALIDATION_ERROR", data);
        }
    }
    
    /**
     * AI超时异常
     */
    public static class AITimeoutException extends AIServiceException {
        public AITimeoutException(String message) {
            super(message, "AI_TIMEOUT");
        }
        
        public AITimeoutException(String message, Throwable cause) {
            super(message, "AI_TIMEOUT", cause);
        }
    }
}