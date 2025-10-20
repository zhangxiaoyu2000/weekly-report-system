package com.weeklyreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 验证配置 - 可通过配置文件控制验证策略
 */
@Configuration
@ConfigurationProperties(prefix = "app.validation")
public class ValidationConfig {
    
    /**
     * 是否启用字段长度验证
     */
    private boolean enableFieldLengthValidation = false;
    
    /**
     * 是否启用严格验证模式
     */
    private boolean strictMode = false;
    
    /**
     * 默认文本字段最大长度（当启用长度验证时使用）
     */
    private int defaultTextMaxLength = 5000;
    
    /**
     * 是否在生产环境启用验证
     */
    private boolean enableInProduction = true;

    // Getters and Setters
    public boolean isEnableFieldLengthValidation() {
        return enableFieldLengthValidation;
    }

    public void setEnableFieldLengthValidation(boolean enableFieldLengthValidation) {
        this.enableFieldLengthValidation = enableFieldLengthValidation;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public int getDefaultTextMaxLength() {
        return defaultTextMaxLength;
    }

    public void setDefaultTextMaxLength(int defaultTextMaxLength) {
        this.defaultTextMaxLength = defaultTextMaxLength;
    }

    public boolean isEnableInProduction() {
        return enableInProduction;
    }

    public void setEnableInProduction(boolean enableInProduction) {
        this.enableInProduction = enableInProduction;
    }
}