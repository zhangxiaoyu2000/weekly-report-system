package com.weeklyreport.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints
 * Tests login, registration, token refresh, and logout functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Authentication Integration Tests")
public class AuthenticationIntegrationTest extends BaseSecurityTest {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testSuccessfulLogin() throws Exception {
        // Test data
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testEmployee.getUsername());
        loginRequest.put("password", getRawPasswordByRole(User.Role.EMPLOYEE));
        
        // Perform login
        MvcResult result = mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpected(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value(testEmployee.getUsername()))
                .andExpect(jsonPath("$.data.user.email").value(testEmployee.getEmail()))
                .andExpect(jsonPath("$.data.user.role").value(User.Role.EMPLOYEE.toString()))
                .andReturn();
    }
    
    @Test
    @DisplayName("Should fail login with invalid credentials")
    void testFailedLoginWithInvalidCredentials() throws Exception {
        // Test data with wrong password
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testEmployee.getUsername());
        loginRequest.put("password", "wrongpassword");
        
        // Perform login
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Invalid credentials")));
    }
    
    @Test
    @DisplayName("Should fail login with non-existent user")
    void testFailedLoginWithNonExistentUser() throws Exception {
        // Test data with non-existent username
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistentuser");
        loginRequest.put("password", "anypassword");
        
        // Perform login
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("User not found")));
    }
    
    @Test
    @DisplayName("Should fail login with inactive user")
    void testFailedLoginWithInactiveUser() throws Exception {
        // Set user to inactive
        testEmployee.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(testEmployee);
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testEmployee.getUsername());
        loginRequest.put("password", getRawPasswordByRole(User.Role.EMPLOYEE));
        
        // Perform login
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Account is inactive")));
    }
    
    @Test
    @DisplayName("Should successfully register new user")
    void testSuccessfulRegistration() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "newuser");
        registerRequest.put("email", "newuser@test.com");
        registerRequest.put("password", "NewUser123!");
        registerRequest.put("fullName", "New User");
        registerRequest.put("employeeId", "EMP001");
        
        // Perform registration
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpected(status().isCreated())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.user.username").value("newuser"))
                .andExpected(jsonPath("$.data.user.email").value("newuser@test.com"))
                .andExpected(jsonPath("$.data.user.role").value(User.Role.EMPLOYEE.toString()));
    }
    
    @Test
    @DisplayName("Should fail registration with duplicate username")
    void testFailedRegistrationWithDuplicateUsername() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", testEmployee.getUsername()); // Existing username
        registerRequest.put("email", "newemail@test.com");
        registerRequest.put("password", "NewUser123!");
        registerRequest.put("fullName", "New User");
        
        // Perform registration
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpected(status().isBadRequest())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Username already exists")));
    }
    
    @Test
    @DisplayName("Should fail registration with invalid email format")
    void testFailedRegistrationWithInvalidEmail() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "newuser");
        registerRequest.put("email", "invalid-email"); // Invalid email format
        registerRequest.put("password", "NewUser123!");
        registerRequest.put("fullName", "New User");
        
        // Perform registration
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpected(status().isBadRequest())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Invalid email format")));
    }
    
    @Test
    @DisplayName("Should fail registration with weak password")
    void testFailedRegistrationWithWeakPassword() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "newuser");
        registerRequest.put("email", "newuser@test.com");
        registerRequest.put("password", "weak"); // Too weak password
        registerRequest.put("fullName", "New User");
        
        // Perform registration
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpected(status().isBadRequest())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Password must be at least 8 characters")));
    }
    
    @Test
    @DisplayName("Should successfully refresh token with valid refresh token")
    void testSuccessfulTokenRefresh() throws Exception {
        // First, perform login to get tokens
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testEmployee.getUsername());
        loginRequest.put("password", getRawPasswordByRole(User.Role.EMPLOYEE));
        
        MvcResult loginResult = mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        // Extract refresh token from login response
        String loginResponse = loginResult.getResponse().getContentAsString();
        // TODO: Parse refresh token from response once Stream B is implemented
        
        // Test token refresh
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "extracted_refresh_token");
        
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.token").exists())
                .andExpected(jsonPath("$.data.refreshToken").exists());
    }
    
    @Test
    @DisplayName("Should fail token refresh with invalid refresh token")
    void testFailedTokenRefreshWithInvalidToken() throws Exception {
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "invalid_refresh_token");
        
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_REFRESH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpected(status().isUnauthorized())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value(containsString("Invalid refresh token")));
    }
    
    @Test
    @DisplayName("Should successfully logout with valid token")
    void testSuccessfulLogout() throws Exception {
        // First login to get token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testEmployee.getUsername());
        loginRequest.put("password", getRawPasswordByRole(User.Role.EMPLOYEE));
        
        MvcResult loginResult = mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        // Extract token from login response
        String loginResponse = loginResult.getResponse().getContentAsString();
        // TODO: Parse JWT token from response once Stream B is implemented
        
        // Test logout
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGOUT)
                .header("Authorization", "Bearer extracted_jwt_token"))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("Successfully logged out"));
    }
    
    @Test
    @DisplayName("Should update last login time after successful login")
    void testLastLoginTimeUpdate() throws Exception {
        // Store original last login time
        java.time.LocalDateTime originalLastLogin = testEmployee.getLastLoginTime();
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testEmployee.getUsername());
        loginRequest.put("password", getRawPasswordByRole(User.Role.EMPLOYEE));
        
        // Perform login
        mockMvc.perform(post(SecurityTestConfig.TestEndpoints.AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpected(status().isOk());
        
        // Verify last login time was updated
        User updatedUser = userRepository.findById(testEmployee.getId()).orElseThrow();
        assert updatedUser.getLastLoginTime() != null;
        assert !updatedUser.getLastLoginTime().equals(originalLastLogin);
    }
}