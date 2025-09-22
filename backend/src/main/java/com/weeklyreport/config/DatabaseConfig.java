package com.weeklyreport.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Database configuration class for transaction management and performance optimization.
 * 
 * This configuration class provides:
 * - Optimized transaction manager configuration
 * - Profile-specific transaction settings
 * - Database performance monitoring
 * 
 * @author Weekly Report System
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Primary transaction manager using JPA/Hibernate.
     * Provides full JPA transaction support with optimized settings.
     */
    @Bean
    @Primary
    @Qualifier("transactionManager")
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Performance optimizations - aligned with application.yml
        transactionManager.setDefaultTimeout(45); // 45 seconds unified timeout
        transactionManager.setFailEarlyOnGlobalRollbackOnly(true);
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }

    /**
     * JDBC transaction manager for non-JPA operations.
     * Used for performance-critical operations that don't require JPA features.
     */
    @Bean
    @Qualifier("jdbcTransactionManager")
    public PlatformTransactionManager jdbcTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.setDefaultTimeout(15); // Shorter timeout for JDBC operations
        return transactionManager;
    }

    /**
     * Default transaction manager annotation support.
     * This method is removed to avoid circular dependency with Flyway.
     * Spring will use the @Bean annotated transaction manager automatically.
     */

    /**
     * Development profile specific configuration.
     * More permissive settings for development and debugging.
     */
    @Configuration
    @Profile("dev")
    public static class DevelopmentDatabaseConfig {
        
        @Bean
        @Qualifier("devTransactionManager")
        public PlatformTransactionManager developmentTransactionManager(EntityManagerFactory entityManagerFactory) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory);
            
            // Development-friendly settings
            transactionManager.setDefaultTimeout(300); // 5 minutes for debugging
            transactionManager.setValidateExistingTransaction(true);
            transactionManager.setGlobalRollbackOnParticipationFailure(false); // More forgiving
            
            return transactionManager;
        }
    }

    /**
     * Production profile specific configuration.
     * Strict settings optimized for production performance and reliability.
     */
    @Configuration
    @Profile("prod")
    public static class ProductionDatabaseConfig {
        
        @Bean
        @Qualifier("prodTransactionManager")  
        public PlatformTransactionManager productionTransactionManager(EntityManagerFactory entityManagerFactory) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory);
            
            // Production-optimized settings
            transactionManager.setDefaultTimeout(10); // Strict 10 second timeout
            transactionManager.setFailEarlyOnGlobalRollbackOnly(true);
            transactionManager.setRollbackOnCommitFailure(true);
            transactionManager.setValidateExistingTransaction(true);
            transactionManager.setGlobalRollbackOnParticipationFailure(true); // Strict failure handling
            
            return transactionManager;
        }
    }

    /**
     * Test profile specific configuration.
     * Optimized for fast test execution with appropriate isolation levels.
     */
    @Configuration
    @Profile("test")
    public static class TestDatabaseConfig {
        
        @Bean
        @Qualifier("testTransactionManager")
        public PlatformTransactionManager testTransactionManager(EntityManagerFactory entityManagerFactory) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory);
            
            // Test-optimized settings
            transactionManager.setDefaultTimeout(5); // Fast timeout for tests
            transactionManager.setValidateExistingTransaction(false); // Skip validation for speed
            transactionManager.setFailEarlyOnGlobalRollbackOnly(false); // Allow test rollbacks
            
            return transactionManager;
        }
    }
}