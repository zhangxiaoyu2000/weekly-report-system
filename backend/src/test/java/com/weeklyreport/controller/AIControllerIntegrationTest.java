package com.weeklyreport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.dto.ai.*;
import com.weeklyreport.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * Integration tests for AI Controller
 * Tests the complete request/response flow for AI functionality
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class AIControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // These will be mocked until actual services are implemented
    // @MockBean
    // private AIAnalysisService mockAIAnalysisService;
    // 
    // @MockBean
    // private AISuggestionService mockAISuggestionService;
    // 
    // @MockBean
    // private AIProjectInsightService mockAIProjectInsightService;

    private String jwtToken;
    private Long testReportId = 1L;
    private Long testProjectId = 1L;

    @BeforeEach
    public void setUp() {
        // Create a valid JWT token for testing
        // jwtToken = jwtTokenProvider.createToken("testuser", Arrays.asList("ROLE_USER"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAnalyzeReportEndpoint() throws Exception {
        // Prepare test request
        AIAnalysisRequest request = new AIAnalysisRequest(testReportId);
        request.setAnalysisTypes(Arrays.asList("summary", "sentiment", "keywords"));
        request.setAnalysisLanguage("zh-CN");
        request.setIncludeDetails(true);

        // Perform request and verify response
        mockMvc.perform(post("/api/ai/analyze-report/{reportId}", testReportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(testReportId))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.message").value("AI analysis started successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAnalysisResultEndpoint() throws Exception {
        // Perform request and verify response
        mockMvc.perform(get("/api/ai/analysis/{reportId}", testReportId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportId").value(testReportId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Analysis result retrieved successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGenerateSuggestionsEndpoint() throws Exception {
        // Prepare test request
        AISuggestionRequest request = new AISuggestionRequest();
        request.setUserId(1L); // This will be overridden by the controller
        request.setContext("report_improvement");
        request.setUserInput("如何提高周报质量？");
        request.setFocusAreas(Arrays.asList("productivity", "communication"));
        request.setMaxSuggestions(5);

        // Perform request and verify response
        mockMvc.perform(post("/api/ai/generate-suggestions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.context").value("report_improvement"))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions.length()").value(2))
                .andExpect(jsonPath("$.message").value("Suggestions generated successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetProjectInsightsEndpoint() throws Exception {
        // Perform request and verify response
        mockMvc.perform(get("/api/ai/project-insights/{projectId}", testProjectId)
                .param("includeComparisons", "true")
                .param("includePredictions", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(testProjectId))
                .andExpect(jsonPath("$.data.projectName").value("Mock Project"))
                .andExpect(jsonPath("$.data.progressInsight").exists())
                .andExpect(jsonPath("$.data.progressInsight.completionPercentage").value(65.0))
                .andExpect(jsonPath("$.message").value("Project insights generated successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAIHealthCheckEndpoint() throws Exception {
        // Perform request and verify response
        mockMvc.perform(get("/api/ai/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("healthy"))
                .andExpect(jsonPath("$.data.ai_service").value("operational"))
                .andExpect(jsonPath("$.message").value("AI service is healthy"));
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        // Test without authentication
        mockMvc.perform(get("/api/ai/analysis/{reportId}", testReportId))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testInvalidReportIdValidation() throws Exception {
        // Test with invalid report ID (negative number)
        mockMvc.perform(get("/api/ai/analysis/{reportId}", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testInvalidAnalysisRequestValidation() throws Exception {
        // Test with invalid analysis request (missing required fields)
        AIAnalysisRequest invalidRequest = new AIAnalysisRequest();
        // Don't set reportId to trigger validation error

        mockMvc.perform(post("/api/ai/analyze-report/{reportId}", testReportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testInvalidSuggestionRequestValidation() throws Exception {
        // Test with invalid suggestion request (missing context)
        AISuggestionRequest invalidRequest = new AISuggestionRequest();
        invalidRequest.setUserId(1L);
        // Don't set context to trigger validation error

        mockMvc.perform(post("/api/ai/generate-suggestions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testRateLimiting() throws Exception {
        // TODO: Implement rate limiting tests once rate limiting is added
        // This test will verify:
        // 1. API rate limits are enforced
        // 2. Rate limit headers are returned
        // 3. Rate limit exceeded responses are proper
        
        // Placeholder test for now
        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testConcurrentRequests() throws Exception {
        // TODO: Implement concurrent request handling tests
        // This test will verify:
        // 1. Multiple concurrent requests are handled properly
        // 2. No race conditions occur
        // 3. System remains stable under load
        
        // Placeholder test for now
        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk());
    }

    // Helper methods for test setup and data creation

    private AIAnalysisRequest createValidAnalysisRequest(Long reportId) {
        AIAnalysisRequest request = new AIAnalysisRequest(reportId);
        request.setAnalysisTypes(Arrays.asList("summary", "sentiment"));
        request.setAnalysisLanguage("zh-CN");
        return request;
    }

    private AISuggestionRequest createValidSuggestionRequest() {
        AISuggestionRequest request = new AISuggestionRequest(1L, "report_improvement");
        request.setUserInput("测试用户输入");
        request.setMaxSuggestions(3);
        return request;
    }

    private void verifyAnalysisResponseStructure() {
        // Will verify that analysis responses have the correct structure
    }

    private void verifySuggestionResponseStructure() {
        // Will verify that suggestion responses have the correct structure
    }
}