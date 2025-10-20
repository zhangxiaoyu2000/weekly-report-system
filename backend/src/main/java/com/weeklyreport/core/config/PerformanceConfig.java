package com.weeklyreport.core.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 性能监控配置
 * 用于监控数据库查询性能，特别是检测N+1查询问题
 */
@Configuration
public class PerformanceConfig {

    /**
     * Hibernate性能监控配置
     * 在开发环境下启用查询统计和慢查询日志
     */
    @Bean
    @ConditionalOnProperty(name = "spring.jpa.show-sql", havingValue = "true")
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // 临时禁用统计信息收集以解决启动问题
            hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, false);
            
            // 启用查询注释，便于追踪查询来源
            hibernateProperties.put(AvailableSettings.USE_SQL_COMMENTS, true);
            
            // 格式化SQL以便阅读
            hibernateProperties.put(AvailableSettings.FORMAT_SQL, true);
            
            // 设置批量大小以优化批量操作
            hibernateProperties.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, 20);
            hibernateProperties.put(AvailableSettings.MAX_FETCH_DEPTH, 3);
        };
    }
}