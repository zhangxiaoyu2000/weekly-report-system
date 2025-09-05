package com.weeklyreport.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test configuration for security-related components
 * This configuration provides test-specific beans and settings for security testing
 */
@TestConfiguration
@ActiveProfiles("test")
public class SecurityTestConfig {
    
    /**
     * Password encoder for testing
     * Uses BCrypt with reduced strength for faster test execution
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        // Use lower strength for faster test execution
        return new BCryptPasswordEncoder(4);
    }
    
    /**
     * Test JWT secret key
     * Uses a fixed key for consistent test results
     */
    public static final String TEST_JWT_SECRET = "test-jwt-secret-key-for-weekly-report-system-testing-only";
    
    /**
     * Test JWT expiration time (shorter for testing)
     */
    public static final long TEST_JWT_EXPIRATION = 3600000; // 1 hour
    
    /**
     * Test refresh token expiration time
     */
    public static final long TEST_REFRESH_TOKEN_EXPIRATION = 7200000; // 2 hours
    
    /**
     * Test user credentials for different roles
     */
    public static class TestUsers {
        public static final String ADMIN_USERNAME = "test_admin";
        public static final String ADMIN_EMAIL = "admin@test.com";
        public static final String ADMIN_PASSWORD = "AdminTest123!";
        
        public static final String MANAGER_USERNAME = "test_manager";
        public static final String MANAGER_EMAIL = "manager@test.com";
        public static final String MANAGER_PASSWORD = "ManagerTest123!";
        
        public static final String EMPLOYEE_USERNAME = "test_employee";
        public static final String EMPLOYEE_EMAIL = "employee@test.com";
        public static final String EMPLOYEE_PASSWORD = "EmployeeTest123!";
        
        public static final String TEAM_LEADER_USERNAME = "test_leader";
        public static final String TEAM_LEADER_EMAIL = "leader@test.com";
        public static final String TEAM_LEADER_PASSWORD = "LeaderTest123!";
        
        public static final String HR_MANAGER_USERNAME = "test_hr";
        public static final String HR_MANAGER_EMAIL = "hr@test.com";
        public static final String HR_MANAGER_PASSWORD = "HRTest123!";
    }
    
    /**
     * Test API endpoints for security testing
     */
    public static class TestEndpoints {
        public static final String AUTH_LOGIN = "/api/auth/login";
        public static final String AUTH_REGISTER = "/api/auth/register";
        public static final String AUTH_REFRESH = "/api/auth/refresh";
        public static final String AUTH_LOGOUT = "/api/auth/logout";
        public static final String AUTH_PROFILE = "/api/auth/profile";
        
        public static final String USER_BASE = "/api/users";
        public static final String ADMIN_BASE = "/api/admin";
        public static final String REPORTS_BASE = "/api/reports";
    }
}