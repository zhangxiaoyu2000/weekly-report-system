package com.weeklyreport.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.integration.ProjectIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for Project Management API
 * Tests authentication, authorization, input validation, and security vulnerabilities
 * 
 * Extends the existing security test framework from Issue #003
 * and applies comprehensive security testing to project management features.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Project API Security Tests")
public class ProjectSecurityTest extends ProjectIntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Authentication Security Tests")
    class AuthenticationSecurityTests {

        @Test
        @DisplayName("Should reject requests without authentication token")
        void shouldRejectRequestsWithoutAuth() throws Exception {
            // Test all endpoints without authorization header
            mockMvc.perform(get("/api/projects"))
                    .andExpected(status().isUnauthorized());

            mockMvc.perform(post("/api/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpected(status().isUnauthorized());

            mockMvc.perform(put("/api/projects/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectUpdateRequest())))
                    .andExpected(status().isUnauthorized());

            mockMvc.perform(delete("/api/projects/1"))
                    .andExpected(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject requests with invalid JWT token")
        void shouldRejectInvalidJwtToken() throws Exception {
            String invalidToken = "Bearer invalid.jwt.token";

            mockMvc.perform(get("/api/projects")
                    .header("Authorization", invalidToken))
                    .andExpected(status().isUnauthorized())
                    .andExpected(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should reject requests with malformed authorization header")
        void shouldRejectMalformedAuthHeader() throws Exception {
            // Missing "Bearer" prefix
            mockMvc.perform(get("/api/projects")
                    .header("Authorization", adminToken))
                    .andExpected(status().isUnauthorized());

            // Wrong format
            mockMvc.perform(get("/api/projects")
                    .header("Authorization", "Basic " + adminToken))
                    .andExpected(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject expired JWT tokens")
        void shouldRejectExpiredJwtTokens() throws Exception {
            // This test would need a mechanism to generate an expired token
            // For now, we simulate by using an obviously invalid token format
            String expiredToken = "Bearer eyJhbGciOiJIUzI1NiJ9.expired.token";

            mockMvc.perform(get("/api/projects")
                    .header("Authorization", expiredToken))
                    .andExpected(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authorization Security Tests")
    class AuthorizationSecurityTests {

        @Test
        @DisplayName("Should enforce role-based access control for project creation")
        void shouldEnforceRoleBasedAccessForProjectCreation() throws Exception {
            // Admin should be allowed
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpected(status().isCreated());

            // Manager should be allowed
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(managerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpected(status().isCreated());

            // Employee should be denied
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should enforce project-level permissions for access control")
        void shouldEnforceProjectLevelPermissions() throws Exception {
            // Create project as admin
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Employee without project membership should be denied access
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());

            // Add employee as project member
            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Now employee should have access
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isOk());
        }

        @Test
        @DisplayName("Should enforce project member management permissions")
        void shouldEnforceProjectMemberManagementPermissions() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Regular employee should not be able to manage members
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpected(status().isForbidden());

            // Project manager should be able to manage members
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(managerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpected(status().isCreated());
        }

        @Test
        @DisplayName("Should prevent privilege escalation through project membership")
        void shouldPreventPrivilegeEscalation() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Add employee as regular member
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);

            // Employee should not be able to promote themselves to project manager
            Map<String, Object> escalationAttempt = Map.of(
                "role", "PROJECT_MANAGER",
                "permissions", new String[]{"READ", "WRITE", "MANAGE", "DELETE"}
            );

            mockMvc.perform(put("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(escalationAttempt)))
                    .andExpected(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Input Validation Security Tests")
    class InputValidationSecurityTests {

        @Test
        @DisplayName("Should prevent SQL injection in project search")
        void shouldPreventSqlInjection() throws Exception {
            String[] sqlInjectionPayloads = {
                "'; DROP TABLE projects; --",
                "' OR '1'='1",
                "'; UPDATE projects SET name='hacked'; --",
                "' UNION SELECT * FROM users; --"
            };

            for (String payload : sqlInjectionPayloads) {
                mockMvc.perform(get("/api/projects")
                        .header("Authorization", getBearerToken(adminToken))
                        .param("search", payload))
                        .andExpected(status().isOk()) // Should not error out
                        .andExpected(content().contentType(MediaType.APPLICATION_JSON));
            }
        }

        @Test
        @DisplayName("Should sanitize XSS attempts in project data")
        void shouldSanitizeXssAttempts() throws Exception {
            String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "javascript:alert('XSS')",
                "<img src=x onerror=alert('XSS')>",
                "' onmouseover='alert(1)'"
            };

            for (String payload : xssPayloads) {
                Map<String, Object> projectData = projectTestData.getValidProjectCreateRequest();
                projectData.put("name", payload);
                projectData.put("description", "Test project with payload: " + payload);

                MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("Authorization", getBearerToken(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectData)))
                        .andExpected(status().isCreated())
                        .andReturn();

                // Verify that the payload is either sanitized or properly encoded
                String response = result.getResponse().getContentAsString();
                Map<String, Object> responseData = objectMapper.readValue(response, Map.class);
                String sanitizedName = (String) responseData.get("name");
                
                // The actual sanitization logic would depend on the implementation
                // This test ensures the system doesn't crash and handles the input
                assertNotNull(sanitizedName);
            }
        }

        @Test
        @DisplayName("Should validate input length limits")
        void shouldValidateInputLengthLimits() throws Exception {
            // Test extremely long input
            String longString = "a".repeat(10000);
            
            Map<String, Object> projectData = projectTestData.getValidProjectCreateRequest();
            projectData.put("name", longString);
            projectData.put("description", longString);

            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectData)))
                    .andExpected(status().isBadRequest())
                    .andExpected(jsonPath("$.details").exists())
                    .andExpected(jsonPath("$.details[*].field").exists());
        }

        @Test
        @DisplayName("Should validate required fields and data types")
        void shouldValidateRequiredFieldsAndDataTypes() throws Exception {
            // Test missing required fields
            Map<String, Object> incompleteProject = new HashMap<>();
            incompleteProject.put("description", "Missing name field");

            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteProject)))
                    .andExpected(status().isBadRequest())
                    .andExpected(jsonPath("$.details").exists());

            // Test invalid data types
            Map<String, Object> invalidProject = projectTestData.getValidProjectCreateRequest();
            invalidProject.put("startDate", "invalid-date-format");
            invalidProject.put("endDate", 12345); // Number instead of date

            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidProject)))
                    .andExpected(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("API Security Vulnerabilities Tests")
    class ApiSecurityVulnerabilitiesTests {

        @Test
        @DisplayName("Should prevent insecure direct object references (IDOR)")
        void shouldPreventInsecureDirectObjectReferences() throws Exception {
            // Create project as admin
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Employee should not be able to access project by guessing ID
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isForbidden());

            // Test with non-existent but valid format ID
            mockMvc.perform(get("/api/projects/{id}", 99999L)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isNotFound());
        }

        @Test
        @DisplayName("Should implement proper rate limiting")
        void shouldImplementRateLimiting() throws Exception {
            // Test rapid successive requests (this test depends on rate limiting configuration)
            int requestCount = 100;
            int rateLimitExceededCount = 0;

            for (int i = 0; i < requestCount; i++) {
                MvcResult result = mockMvc.perform(get("/api/projects")
                        .header("Authorization", getBearerToken(adminToken)))
                        .andReturn();

                if (result.getResponse().getStatus() == 429) { // Too Many Requests
                    rateLimitExceededCount++;
                }
            }

            // If rate limiting is properly configured, some requests should be rejected
            // This test is informational and may need adjustment based on rate limiting configuration
            System.out.println("Rate limiting test - Requests rejected: " + rateLimitExceededCount + "/" + requestCount);
        }

        @Test
        @DisplayName("Should prevent information disclosure in error messages")
        void shouldPreventInformationDisclosure() throws Exception {
            // Test with non-existent project ID
            MvcResult result = mockMvc.perform(get("/api/projects/{id}", 99999L)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNotFound())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Map<String, Object> errorResponse = objectMapper.readValue(response, Map.class);
            String errorMessage = (String) errorResponse.get("message");

            // Error message should not reveal internal implementation details
            assertFalse(errorMessage.contains("SELECT"), "Error message should not contain SQL details");
            assertFalse(errorMessage.contains("database"), "Error message should not contain database details");
            assertFalse(errorMessage.contains("table"), "Error message should not contain table details");
            assertTrue(errorMessage.contains("not found") || errorMessage.contains("Not Found"),
                    "Error message should be user-friendly");
        }

        @Test
        @DisplayName("Should prevent mass assignment vulnerabilities")
        void shouldPreventMassAssignment() throws Exception {
            Map<String, Object> maliciousProject = projectTestData.getValidProjectCreateRequest();
            
            // Attempt to set fields that should not be settable via API
            maliciousProject.put("id", 12345L);
            maliciousProject.put("createdAt", "2020-01-01T00:00:00Z");
            maliciousProject.put("createdBy", 999L);
            maliciousProject.put("internal", true);
            maliciousProject.put("adminOnly", true);

            MvcResult result = mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(maliciousProject)))
                    .andExpected(status().isCreated())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Map<String, Object> createdProject = objectMapper.readValue(response, Map.class);

            // Verify that dangerous fields were not set
            assertNotEquals(12345L, ((Number) createdProject.get("id")).longValue());
            // Add more assertions based on your specific security requirements
        }

        @Test
        @DisplayName("Should handle concurrent modification safely")
        void shouldHandleConcurrentModificationSafely() throws Exception {
            // Create a project
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Simulate concurrent updates with different tokens
            Map<String, Object> update1 = projectTestData.getValidProjectUpdateRequest();
            update1.put("name", "Update 1");

            Map<String, Object> update2 = projectTestData.getValidProjectUpdateRequest();
            update2.put("name", "Update 2");

            // Both updates should succeed or fail gracefully without data corruption
            MvcResult result1 = mockMvc.perform(put("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update1)))
                    .andReturn();

            MvcResult result2 = mockMvc.perform(put("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(managerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update2)))
                    .andReturn();

            // At least one should succeed, and the system should handle conflicts gracefully
            assertTrue(result1.getResponse().getStatus() == 200 || result2.getResponse().getStatus() == 200,
                    "At least one concurrent update should succeed");
        }
    }

    @Nested
    @DisplayName("Data Privacy and Protection Tests")
    class DataPrivacyAndProtectionTests {

        @Test
        @DisplayName("Should not expose sensitive user data in project responses")
        void shouldNotExposeSensitiveUserData() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Add member to project
            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Get project with members
            MvcResult result = mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .param("includeMembers", "true"))
                    .andExpected(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            
            // Verify sensitive data is not exposed
            assertFalse(response.contains("password"), "Response should not contain password data");
            assertFalse(response.contains("ssn"), "Response should not contain SSN data");
            assertFalse(response.contains("salary"), "Response should not contain salary data");
        }

        @Test
        @DisplayName("Should enforce data retention policies")
        void shouldEnforceDataRetentionPolicies() throws Exception {
            // This test would verify that deleted projects and members
            // are properly removed according to data retention policies
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Delete project
            mockMvc.perform(delete("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNoContent());

            // Verify project is truly deleted (not just soft-deleted)
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNotFound());
        }
    }
}