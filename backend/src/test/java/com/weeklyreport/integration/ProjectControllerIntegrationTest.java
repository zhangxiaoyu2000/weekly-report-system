package com.weeklyreport.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Project Management API endpoints
 * Tests all CRUD operations and permission controls for projects
 * 
 * Note: These tests are designed to work with the Project API implementation
 * from Stream A. They include placeholder assertions that will be activated
 * once the actual ProjectController is implemented.
 */
@DisplayName("Project API Integration Tests")
public class ProjectControllerIntegrationTest extends ProjectIntegrationTestBase {

    @Nested
    @DisplayName("Project CRUD Operations")
    class ProjectCrudOperations {

        @Test
        @DisplayName("Should create project with valid data when user has permission")
        void shouldCreateProjectWithValidData() throws Exception {
            // Test project creation with admin user
            MvcResult result = mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("Test Project"))
                    .andExpect(jsonPath("$.description").value("A test project for integration testing"))
                    .andExpect(jsonPath("$.status").value("PLANNING"))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andReturn();

            // Verify the created project data
            String response = result.getResponse().getContentAsString();
            assertProjectResponse(response, projectTestData.getValidProjectCreateRequest());
        }

        @Test
        @DisplayName("Should return validation error for invalid project data")
        void shouldReturnValidationErrorForInvalidData() throws Exception {
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getInvalidProjectRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.details[*].field").exists());
        }

        @Test
        @DisplayName("Should retrieve project by ID when user has access")
        void shouldRetrieveProjectById() throws Exception {
            // First create a project
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Then retrieve it
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpect(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.name").value("Test Project"))
                    .andExpect(jsonPath("$.status").value("PLANNING"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent project")
        void shouldReturn404ForNonExistentProject() throws Exception {
            mockMvc.perform(get("/api/projects/{id}", 99999L)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should update project with valid data")
        void shouldUpdateProjectWithValidData() throws Exception {
            // Create a project first
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Update the project
            mockMvc.perform(put("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.name").value("Updated Test Project"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should delete project when user has permission")
        void shouldDeleteProjectWithPermission() throws Exception {
            // Create a project first
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Delete the project
            mockMvc.perform(delete("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpect(status().isNoContent());

            // Verify project is deleted
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should list projects with pagination")
        void shouldListProjectsWithPagination() throws Exception {
            // Create multiple projects
            createTestProject(adminToken);
            createTestProject(adminToken);

            // List projects with pagination
            mockMvc.perform(get("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "name,asc"))
                    .andExpected(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page.size").value(10))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andExpect(jsonPath("$.page.totalElements").exists());
        }

        @Test
        @DisplayName("Should filter projects by status")
        void shouldFilterProjectsByStatus() throws Exception {
            // Create projects with different statuses
            createTestProject(adminToken);

            // Search by status
            mockMvc.perform(get("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .param("status", "PLANNING"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].status").value("PLANNING"));
        }
    }

    @Nested
    @DisplayName("Project Permission Controls")
    class ProjectPermissionControls {

        @Test
        @DisplayName("Should deny project creation for unauthorized user")
        void shouldDenyProjectCreationForUnauthorizedUser() throws Exception {
            mockMvc.perform(post("/api/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow managers to create projects")
        void shouldAllowManagersToCreateProjects() throws Exception {
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(managerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("Should deny project creation for regular employees")
        void shouldDenyProjectCreationForEmployees() throws Exception {
            mockMvc.perform(post("/api/projects")
                    .header("Authorization", getBearerToken(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectCreateRequest())))
                    .andExpected(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should allow project members to view project details")
        void shouldAllowProjectMembersToViewProject() throws Exception {
            // Create project as admin
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Add employee as project member
            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Employee should be able to view the project
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId));
        }

        @Test
        @DisplayName("Should deny project access for non-members")
        void shouldDenyProjectAccessForNonMembers() throws Exception {
            // Create project as admin (employee is not a member)
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Employee should not be able to view the project
            mockMvc.perform(get("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should only allow project managers to update projects")
        void shouldOnlyAllowProjectManagersToUpdate() throws Exception {
            // Create project as admin
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Add employee as regular member (not project manager)
            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Employee should not be able to update the project
            mockMvc.perform(put("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectUpdateRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should only allow admins and project managers to delete projects")
        void shouldOnlyAllowAuthorizedUsersToDelete() throws Exception {
            // Create project as admin
            MvcResult createResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(createResult);

            // Employee should not be able to delete the project
            mockMvc.perform(delete("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpect(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());

            // Admin should be able to delete the project
            mockMvc.perform(delete("/api/projects/{id}", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Project Search and Filtering")
    class ProjectSearchAndFiltering {

        @Test
        @DisplayName("Should search projects by name")
        void shouldSearchProjectsByName() throws Exception {
            // Create test project
            createTestProject(adminToken);

            mockMvc.perform(get("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .param("search", "Test"))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.content").isArray())
                    .andExpected(jsonPath("$.content[*].name").value("Test Project"));
        }

        @Test
        @DisplayName("Should filter projects by priority")
        void shouldFilterProjectsByPriority() throws Exception {
            createTestProject(adminToken);

            mockMvc.perform(get("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .param("priority", "MEDIUM"))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.content").isArray())
                    .andExpected(jsonPath("$.content[*].priority").value("MEDIUM"));
        }

        @Test
        @DisplayName("Should filter projects by date range")
        void shouldFilterProjectsByDateRange() throws Exception {
            createTestProject(adminToken);

            mockMvc.perform(get("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .param("startDate", "2025-01-01")
                    .param("endDate", "2025-12-31"))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should return empty results for no matching filters")
        void shouldReturnEmptyForNoMatches() throws Exception {
            mockMvc.perform(get("/api/projects")
                    .header("Authorization", getBearerToken(adminToken))
                    .param("status", "NON_EXISTENT_STATUS"))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.content").isEmpty())
                    .andExpected(jsonPath("$.page.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("Project Statistics and Analytics")
    class ProjectStatisticsAndAnalytics {

        @Test
        @DisplayName("Should get project statistics for admin")
        void shouldGetProjectStatisticsForAdmin() throws Exception {
            // Create some test projects
            createTestProject(adminToken);
            createTestProject(adminToken);

            mockMvc.perform(get("/api/projects/statistics")
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.totalProjects").exists())
                    .andExpected(jsonPath("$.activeProjects").exists())
                    .andExpected(jsonPath("$.completedProjects").exists())
                    .andExpected(jsonPath("$.projectsByStatus").exists());
        }

        @Test
        @DisplayName("Should deny statistics access for regular employees")
        void shouldDenyStatisticsAccessForEmployees() throws Exception {
            mockMvc.perform(get("/api/projects/statistics")
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should get project dashboard for manager")
        void shouldGetProjectDashboardForManager() throws Exception {
            mockMvc.perform(get("/api/projects/dashboard")
                    .header("Authorization", getBearerToken(managerToken)))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.myProjects").exists())
                    .andExpected(jsonPath("$.managedProjects").exists())
                    .andExpected(jsonPath("$.recentActivity").exists());
        }
    }
}