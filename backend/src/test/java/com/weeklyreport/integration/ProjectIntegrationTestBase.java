package com.weeklyreport.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.security.BaseSecurityTest;
import com.weeklyreport.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for project management API integration tests
 * Provides common setup, utilities, and test data for project-related integration tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ProjectIntegrationTestBase extends BaseSecurityTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    // JWT tokens for different user roles
    protected String adminToken;
    protected String managerToken;
    protected String employeeToken;
    protected String teamLeaderToken;
    protected String hrManagerToken;

    // Test project data
    protected ProjectTestData projectTestData;

    @BeforeEach
    void setUpProjectIntegrationTest() throws Exception {
        // Generate JWT tokens for all test users
        adminToken = generateJwtToken(testAdmin.getUsername());
        managerToken = generateJwtToken(testManager.getUsername());
        employeeToken = generateJwtToken(testEmployee.getUsername());
        teamLeaderToken = generateJwtToken(testTeamLeader.getUsername());
        hrManagerToken = generateJwtToken(testHrManager.getUsername());

        // Initialize test data
        projectTestData = new ProjectTestData();
    }

    /**
     * Generate JWT token for a user via authentication endpoint
     */
    protected String generateJwtToken(String username) throws Exception {
        // Get the raw password for the user
        String password = switch (username) {
            case "testadmin" -> getRawPasswordByRole(testAdmin.getRole());
            case "testmanager" -> getRawPasswordByRole(testManager.getRole());
            case "testemployee" -> getRawPasswordByRole(testEmployee.getRole());
            case "testteamleader" -> getRawPasswordByRole(testTeamLeader.getRole());
            case "testhrmanager" -> getRawPasswordByRole(testHrManager.getRole());
            default -> throw new IllegalArgumentException("Unknown username: " + username);
        };

        // Authenticate and get token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return (String) responseMap.get("token");
    }

    /**
     * Get authorization header with JWT token
     */
    protected String getBearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * Test data factory for project-related entities
     */
    public static class ProjectTestData {
        
        public Map<String, Object> getValidProjectCreateRequest() {
            Map<String, Object> project = new HashMap<>();
            project.put("name", "Test Project");
            project.put("description", "A test project for integration testing");
            project.put("status", "PLANNING");
            project.put("priority", "MEDIUM");
            project.put("startDate", "2025-01-01");
            project.put("endDate", "2025-12-31");
            return project;
        }

        public Map<String, Object> getValidProjectUpdateRequest() {
            Map<String, Object> project = new HashMap<>();
            project.put("name", "Updated Test Project");
            project.put("description", "An updated test project description");
            project.put("status", "ACTIVE");
            project.put("priority", "HIGH");
            project.put("startDate", "2025-01-01");
            project.put("endDate", "2025-12-31");
            return project;
        }

        public Map<String, Object> getInvalidProjectRequest() {
            Map<String, Object> project = new HashMap<>();
            // Missing required fields to trigger validation errors
            project.put("description", "Invalid project without name");
            return project;
        }

        public Map<String, Object> getValidProjectMemberRequest() {
            Map<String, Object> member = new HashMap<>();
            member.put("userId", 1L);
            member.put("role", "MEMBER");
            member.put("permissions", new String[]{"READ", "WRITE"});
            return member;
        }

        public Map<String, Object> getValidProjectMemberUpdateRequest() {
            Map<String, Object> member = new HashMap<>();
            member.put("role", "PROJECT_MANAGER");
            member.put("permissions", new String[]{"READ", "WRITE", "MANAGE"});
            return member;
        }

        public Map<String, Object> getProjectSearchRequest() {
            Map<String, Object> search = new HashMap<>();
            search.put("status", "ACTIVE");
            search.put("priority", "HIGH");
            search.put("page", 0);
            search.put("size", 10);
            search.put("sort", "name,asc");
            return search;
        }
    }

    /**
     * Helper method to create test project via API
     */
    protected MvcResult createTestProject(String token) throws Exception {
        return mockMvc.perform(post("/api/projects")
                .header("Authorization", getBearerToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                .andExpected(status().isCreated())
                .andReturn();
    }

    /**
     * Helper method to create test project member via API
     */
    protected MvcResult addTestProjectMember(String token, Long projectId, Long userId) throws Exception {
        Map<String, Object> memberRequest = projectTestData.getValidProjectMemberRequest();
        memberRequest.put("userId", userId);
        
        return mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                .header("Authorization", getBearerToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    /**
     * Extract project ID from creation response
     */
    protected Long extractProjectIdFromResponse(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return ((Number) responseMap.get("id")).longValue();
    }

    /**
     * Extract member ID from creation response
     */
    protected Long extractMemberIdFromResponse(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return ((Number) responseMap.get("id")).longValue();
    }

    /**
     * Assert response contains expected project fields
     */
    protected void assertProjectResponse(String response, Map<String, Object> expectedData) throws Exception {
        Map<String, Object> actualData = objectMapper.readValue(response, Map.class);
        
        if (expectedData.containsKey("name")) {
            assert actualData.get("name").equals(expectedData.get("name"));
        }
        if (expectedData.containsKey("description")) {
            assert actualData.get("description").equals(expectedData.get("description"));
        }
        if (expectedData.containsKey("status")) {
            assert actualData.get("status").equals(expectedData.get("status"));
        }
        if (expectedData.containsKey("priority")) {
            assert actualData.get("priority").equals(expectedData.get("priority"));
        }
    }

    /**
     * Assert response contains expected member fields
     */
    protected void assertMemberResponse(String response, Map<String, Object> expectedData) throws Exception {
        Map<String, Object> actualData = objectMapper.readValue(response, Map.class);
        
        if (expectedData.containsKey("userId")) {
            assert actualData.get("userId").equals(expectedData.get("userId"));
        }
        if (expectedData.containsKey("role")) {
            assert actualData.get("role").equals(expectedData.get("role"));
        }
        if (expectedData.containsKey("permissions")) {
            // Compare permissions arrays
            assert actualData.get("permissions") != null;
        }
    }
}