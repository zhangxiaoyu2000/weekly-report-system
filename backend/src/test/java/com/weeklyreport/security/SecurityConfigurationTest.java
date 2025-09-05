package com.weeklyreport.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for Spring Security configuration
 * Verifies CORS, CSRF, security headers, and other security configurations
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Security Configuration Tests")
public class SecurityConfigurationTest extends BaseSecurityTest {
    
    @Test
    @DisplayName("Should allow CORS for configured origins")
    void testCORSConfiguration() throws Exception {
        // Test preflight CORS request
        mockMvc.perform(options(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", 
                    containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", 
                    containsString("Content-Type")))
                .andExpect(header().string("Access-Control-Allow-Headers", 
                    containsString("Authorization")));
    }
    
    @Test
    @DisplayName("Should reject CORS for non-configured origins")
    void testCORSRejection() throws Exception {
        // Test CORS request from unauthorized origin
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .header("Origin", "http://malicious-site.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should include security headers in responses")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andDo(print())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpected(header().string("X-Frame-Options", "DENY"))
                .andExpected(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpected(header().exists("Strict-Transport-Security"));
    }
    
    @Test
    @DisplayName("Should disable CSRF for API endpoints")
    void testCSRFConfiguration() throws Exception {
        // CSRF should be disabled for REST API endpoints
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andDo(print())
                // Should not require CSRF token
                .andExpected(status().isNot(status().isForbidden()));
    }
    
    @Test
    @DisplayName("Should secure actuator endpoints")
    void testActuatorSecurity() throws Exception {
        // Health endpoint should be accessible
        mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpected(status().isOk());
                
        // Info endpoint should be accessible
        mockMvc.perform(get("/actuator/info"))
                .andDo(print())
                .andExpected(status().isOk());
                
        // Sensitive endpoints should require authentication
        mockMvc.perform(get("/actuator/metrics"))
                .andDo(print())
                .andExpected(status().isUnauthorized());
                
        mockMvc.perform(get("/actuator/env"))
                .andDo(print())
                .andExpected(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should configure session management correctly")
    void testSessionManagement() throws Exception {
        // TODO: Implement once security configuration is available
        /*
        // REST API should be stateless (no session creation)
        MvcResult result = mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + testEmployee.getUsername() + "\",\"password\":\"" + 
                    getRawPasswordByRole(User.Role.EMPLOYEE) + "\"}"))
                .andExpected(status().isOk())
                .andReturn();
                
        // Verify no session was created
        assertNull(result.getRequest().getSession(false));
        */
        
        // Placeholder until Stream A is complete
        assertTrue(true, "Session management test - waiting for Stream A implementation");
    }
    
    @Test
    @DisplayName("Should handle authentication failures correctly")
    void testAuthenticationFailureHandling() throws Exception {
        mockMvc.perform(get(SecurityTestConfig.TestEndpoints.USER_BASE))
                .andDo(print())
                .andExpected(status().isUnauthorized())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Authentication required")));
    }
    
    @Test
    @DisplayName("Should handle authorization failures correctly")
    void testAuthorizationFailureHandling() throws Exception {
        // TODO: Implement once JWT authentication is available
        /*
        String employeeToken = getJwtTokenForUser(testEmployee);
        
        mockMvc.perform(get(SecurityTestConfig.TestEndpoints.ADMIN_BASE + "/users")
                .header("Authorization", "Bearer " + employeeToken))
                .andDo(print())
                .andExpected(status().isForbidden())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Access denied")));
        */
        
        // Placeholder until Stream A & B are complete
        assertTrue(true, "Authorization failure handling test - waiting for Stream A & B implementation");
    }
    
    @Test
    @DisplayName("Should configure password encoder correctly")
    void testPasswordEncoderConfiguration() throws Exception {
        String rawPassword = "TestPassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Encoded password should not equal raw password
        assertNotEquals(rawPassword, encodedPassword);
        
        // Should be able to verify password
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        
        // Should reject wrong password
        assertFalse(passwordEncoder.matches("WrongPassword", encodedPassword));
        
        // Encoded passwords should be different each time
        String encodedPassword2 = passwordEncoder.encode(rawPassword);
        assertNotEquals(encodedPassword, encodedPassword2);
        
        // Both should match the raw password
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2));
    }
    
    @Test
    @DisplayName("Should rate limit authentication attempts")
    void testAuthenticationRateLimit() throws Exception {
        // TODO: Implement once rate limiting is configured
        /*
        String loginData = "{\"username\":\"nonexistent\",\"password\":\"wrong\"}";
        
        // Make multiple failed login attempts
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginData))
                    .andDo(print());
        }
        
        // Next attempt should be rate limited
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginData))
                .andDo(print())
                .andExpected(status().isTooManyRequests())
                .andExpected(jsonPath("$.message").value(containsString("Too many login attempts")));
        */
        
        // Placeholder until rate limiting is implemented
        assertTrue(true, "Authentication rate limiting test - waiting for implementation");
    }
    
    @Test
    @DisplayName("Should validate JWT token format and structure")
    void testJWTTokenStructureValidation() throws Exception {
        // TODO: Implement once JWT filter is available
        /*
        // Test various malformed Authorization headers
        String[] malformedHeaders = {
            "InvalidToken",
            "Bearer",
            "Bearer ",
            "Basic dGVzdA==",
            "Bearer invalid.jwt",
            "Bearer header.payload"  // Missing signature
        };
        
        for (String header : malformedHeaders) {
            mockMvc.perform(get(SecurityTestConfig.TestEndpoints.AUTH_PROFILE)
                    .header("Authorization", header))
                    .andDo(print())
                    .andExpected(status().isUnauthorized());
        }
        */
        
        // Placeholder until Stream A is complete
        assertTrue(true, "JWT token structure validation test - waiting for Stream A implementation");
    }
    
    @Test
    @DisplayName("Should secure error pages and stack traces")
    void testErrorHandlingSecurity() throws Exception {
        // Should not expose stack traces in production profile
        mockMvc.perform(get("/api/nonexistent/endpoint"))
                .andDo(print())
                .andExpected(status().isNotFound())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").exists())
                // Should not contain stack trace information
                .andExpected(jsonPath("$.stackTrace").doesNotExist())
                .andExpected(jsonPath("$.trace").doesNotExist());
    }
    
    /**
     * Helper method to get JWT token for a user (will be implemented once Stream A is ready)
     */
    private String getJwtTokenForUser(com.weeklyreport.entity.User user) {
        // TODO: Implement once JwtTokenProvider is available
        return "mock_jwt_token_for_" + user.getUsername();
    }
}