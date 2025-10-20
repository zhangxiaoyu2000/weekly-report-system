package com.weeklyreport.filemanagement.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * MinIO配置类
 * 配置MinIO客户端连接和相关参数
 */
@Configuration
public class MinIOConfig {

    private final MinIOProperties minIOProperties;

    public MinIOConfig(MinIOProperties minIOProperties) {
        this.minIOProperties = minIOProperties;
    }

    /**
     * 配置MinIO客户端
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minIOProperties.getEndpoint())
                .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
                .build();
    }

    /**
     * MinIO配置属性类
     */
    @Component
    @ConfigurationProperties(prefix = "minio")
    public static class MinIOProperties {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucketName = "weekly-reports";
        private boolean autoCreateBucket = true;
        private long maxFileSize = 100 * 1024 * 1024; // 100MB
        private String[] allowedContentTypes = {
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "application/pdf",
            "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "text/csv",
            "application/zip", "application/x-rar-compressed"
        };
        private int urlExpirySeconds = 7 * 24 * 60 * 60; // 7天

        // Getters and Setters
        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public boolean isAutoCreateBucket() {
            return autoCreateBucket;
        }

        public void setAutoCreateBucket(boolean autoCreateBucket) {
            this.autoCreateBucket = autoCreateBucket;
        }

        public long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public String[] getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(String[] allowedContentTypes) {
            this.allowedContentTypes = allowedContentTypes;
        }

        public int getUrlExpirySeconds() {
            return urlExpirySeconds;
        }

        public void setUrlExpirySeconds(int urlExpirySeconds) {
            this.urlExpirySeconds = urlExpirySeconds;
        }
    }
}