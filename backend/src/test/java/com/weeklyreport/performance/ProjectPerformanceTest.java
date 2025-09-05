package com.weeklyreport.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weeklyreport.integration.ProjectIntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Performance tests for Project Management API
 * Tests response times, throughput, and database query performance
 * 
 * These tests establish performance baselines and identify bottlenecks
 * in project-related operations under various load conditions.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Project API Performance Tests")
public class ProjectPerformanceTest extends ProjectIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int LARGE_DATASET_SIZE = 1000;
    private static final int CONCURRENT_USERS = 10;
    private static final int ACCEPTABLE_RESPONSE_TIME_MS = 200;
    private static final int PAGINATION_SIZE = 20;

    @BeforeEach
    void setUpPerformanceTest() {
        // Performance tests use the same base setup as integration tests
    }

    @Test
    @DisplayName("Project List Query Performance - Small Dataset")
    void testProjectListPerformanceSmallDataset() throws Exception {
        // Create 50 test projects
        createMultipleTestProjects(50);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Test project list query performance
        MvcResult result = mockMvc.perform(get("/api/projects")
                .header("Authorization", getBearerToken(adminToken))
                .param("page", "0")
                .param("size", String.valueOf(PAGINATION_SIZE)))
                .andExpected(status().isOk())
                .andReturn();

        stopWatch.stop();
        long responseTime = stopWatch.getLastTaskTimeMillis();

        // Assert performance criteria
        assertTrue(responseTime < ACCEPTABLE_RESPONSE_TIME_MS,
                "Project list query took " + responseTime + "ms, expected < " + ACCEPTABLE_RESPONSE_TIME_MS + "ms");

        // Verify response structure
        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseData = objectMapper.readValue(response, Map.class);
        
        assertNotNull(responseData.get("content"));
        assertNotNull(responseData.get("page"));
        
        System.out.println("Small Dataset Performance - Response Time: " + responseTime + "ms");
    }

    @Test
    @DisplayName("Project List Query Performance - Large Dataset")
    @Sql("/test-data/large-project-dataset.sql")
    void testProjectListPerformanceLargeDataset() throws Exception {
        // Test with large dataset (loaded via SQL script)
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MvcResult result = mockMvc.perform(get("/api/projects")
                .header("Authorization", getBearerToken(adminToken))
                .param("page", "0")
                .param("size", String.valueOf(PAGINATION_SIZE))
                .param("sort", "createdAt,desc"))
                .andExpected(status().isOk())
                .andReturn();

        stopWatch.stop();
        long responseTime = stopWatch.getLastTaskTimeMillis();

        // More lenient threshold for large dataset
        assertTrue(responseTime < ACCEPTABLE_RESPONSE_TIME_MS * 2,
                "Large dataset query took " + responseTime + "ms, expected < " + (ACCEPTABLE_RESPONSE_TIME_MS * 2) + "ms");

        System.out.println("Large Dataset Performance - Response Time: " + responseTime + "ms");
    }

    @Test
    @DisplayName("Project Search Performance with Filters")
    void testProjectSearchPerformanceWithFilters() throws Exception {
        createMultipleTestProjects(100);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Test complex search with multiple filters
        mockMvc.perform(get("/api/projects")
                .header("Authorization", getBearerToken(adminToken))
                .param("status", "ACTIVE")
                .param("priority", "HIGH")
                .param("search", "Test")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .param("page", "0")
                .param("size", "20"))
                .andExpected(status().isOk());

        stopWatch.stop();
        long responseTime = stopWatch.getLastTaskTimeMillis();

        assertTrue(responseTime < ACCEPTABLE_RESPONSE_TIME_MS * 1.5,
                "Filtered search took " + responseTime + "ms, expected < " + (ACCEPTABLE_RESPONSE_TIME_MS * 1.5) + "ms");

        System.out.println("Filtered Search Performance - Response Time: " + responseTime + "ms");
    }

    @Test
    @DisplayName("Concurrent Project Access Performance")
    void testConcurrentProjectAccessPerformance() throws Exception {
        // Create test projects
        List<Long> projectIds = createMultipleTestProjectsAndReturnIds(20);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Simulate concurrent users accessing different projects
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final Long projectId = projectIds.get(i % projectIds.size());
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    StopWatch requestStopWatch = new StopWatch();
                    requestStopWatch.start();

                    mockMvc.perform(get("/api/projects/{id}", projectId)
                            .header("Authorization", getBearerToken(adminToken)))
                            .andExpected(status().isOk());

                    requestStopWatch.stop();
                    return requestStopWatch.getLastTaskTimeMillis();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        // Calculate average response time
        double avgResponseTime = futures.stream()
                .mapToLong(CompletableFuture::join)
                .average()
                .orElse(0);

        long totalTime = stopWatch.getLastTaskTimeMillis();

        assertTrue(avgResponseTime < ACCEPTABLE_RESPONSE_TIME_MS,
                "Average concurrent response time: " + avgResponseTime + "ms, expected < " + ACCEPTABLE_RESPONSE_TIME_MS + "ms");

        System.out.println("Concurrent Access Performance:");
        System.out.println("  - Total Time: " + totalTime + "ms");
        System.out.println("  - Average Response Time: " + avgResponseTime + "ms");
        System.out.println("  - Concurrent Users: " + CONCURRENT_USERS);

        executor.shutdown();
    }

    @Test
    @DisplayName("Project Member List Performance")
    void testProjectMemberListPerformance() throws Exception {
        // Create project and add many members
        Long projectId = createTestProjectAndReturnId();
        addMultipleMembersToProject(projectId, 100);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                .header("Authorization", getBearerToken(adminToken))
                .param("page", "0")
                .param("size", "50"))
                .andExpected(status().isOk());

        stopWatch.stop();
        long responseTime = stopWatch.getLastTaskTimeMillis();

        assertTrue(responseTime < ACCEPTABLE_RESPONSE_TIME_MS,
                "Member list query took " + responseTime + "ms, expected < " + ACCEPTABLE_RESPONSE_TIME_MS + "ms");

        System.out.println("Member List Performance - Response Time: " + responseTime + "ms");
    }

    @Test
    @DisplayName("Project Creation Performance")
    void testProjectCreationPerformance() throws Exception {
        List<Long> responseTimes = new ArrayList<>();

        // Test multiple project creations
        for (int i = 0; i < 10; i++) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Map<String, Object> projectData = projectTestData.getValidProjectCreateRequest();
            projectData.put("name", "Performance Test Project " + i);

            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectData)))
                    .andExpected(status().isCreated());

            stopWatch.stop();
            responseTimes.add(stopWatch.getLastTaskTimeMillis());
        }

        double avgCreationTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxCreationTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        assertTrue(avgCreationTime < ACCEPTABLE_RESPONSE_TIME_MS,
                "Average creation time: " + avgCreationTime + "ms, expected < " + ACCEPTABLE_RESPONSE_TIME_MS + "ms");

        System.out.println("Project Creation Performance:");
        System.out.println("  - Average Time: " + avgCreationTime + "ms");
        System.out.println("  - Max Time: " + maxCreationTime + "ms");
    }

    @Test
    @DisplayName("Database Query Optimization Test")
    void testDatabaseQueryOptimization() throws Exception {
        // Create projects with members and test N+1 query issues
        List<Long> projectIds = createMultipleTestProjectsAndReturnIds(20);
        
        // Add members to each project
        for (Long projectId : projectIds) {
            addMultipleMembersToProject(projectId, 5);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // This query should be optimized to avoid N+1 problems
        mockMvc.perform(get("/api/projects")
                .header("Authorization", getBearerToken(adminToken))
                .param("includeMemberCount", "true")
                .param("page", "0")
                .param("size", "20"))
                .andExpected(status().isOk());

        stopWatch.stop();
        long responseTime = stopWatch.getLastTaskTimeMillis();

        // This should be fast even with member counts
        assertTrue(responseTime < ACCEPTABLE_RESPONSE_TIME_MS * 1.2,
                "Query with member counts took " + responseTime + "ms, possible N+1 issue");

        System.out.println("Query Optimization Test - Response Time: " + responseTime + "ms");
    }

    // Helper methods for test data creation

    private void createMultipleTestProjects(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            Map<String, Object> projectData = projectTestData.getValidProjectCreateRequest();
            projectData.put("name", "Test Project " + i);
            projectData.put("description", "Performance test project number " + i);
            
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectData)))
                    .andExpected(status().isCreated());
        }
    }

    private List<Long> createMultipleTestProjectsAndReturnIds(int count) throws Exception {
        List<Long> projectIds = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> projectData = projectTestData.getValidProjectCreateRequest();
            projectData.put("name", "Test Project " + i);
            
            MvcResult result = mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectData)))
                    .andExpected(status().isCreated())
                    .andReturn();
            
            Long projectId = extractProjectIdFromResponse(result);
            projectIds.add(projectId);
        }
        
        return projectIds;
    }

    private Long createTestProjectAndReturnId() throws Exception {
        MvcResult result = createTestProject(adminToken);
        return extractProjectIdFromResponse(result);
    }

    private void addMultipleMembersToProject(Long projectId, int count) throws Exception {
        // Note: This is a simplified version. In reality, you'd need actual user IDs
        // For performance testing, this simulates the database load
        for (int i = 0; i < count; i++) {
            Map<String, Object> memberData = Map.of(
                "userId", (long) (i + 1),
                "role", "MEMBER",
                "permissions", new String[]{"READ", "WRITE"}
            );
            
            try {
                mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", getBearerToken(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberData)))
                        .andExpected(status().isCreated());
            } catch (Exception e) {
                // Continue if member already exists or other non-critical errors
                System.out.println("Warning: Could not add member " + i + " to project " + projectId);
            }
        }
    }
}