package com.weeklyreport.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for database configuration.
 * Tests transaction manager setup, datasource configuration, and profile-specific settings.
 */
@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager transactionManager;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull();
        assertThat(transactionManager).isNotNull();
    }

    @Test
    void dataSourceIsConfigured() {
        assertThat(dataSource).isNotNull();
        // Verify it's HikariCP (default in Spring Boot)
        assertThat(dataSource.getClass().getName()).contains("Hikari");
    }

    @Test
    void transactionManagerIsConfigured() {
        assertThat(transactionManager).isNotNull();
        assertThat(transactionManager.getClass().getSimpleName()).isEqualTo("JpaTransactionManager");
    }
}