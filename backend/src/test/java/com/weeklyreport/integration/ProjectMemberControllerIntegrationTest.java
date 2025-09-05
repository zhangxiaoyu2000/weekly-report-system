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
 * Integration tests for Project Member Management API endpoints
 * Tests member operations, role management, and permission controls
 * 
 * Note: These tests are designed to work with the ProjectMember API implementation
 * from Stream B. They include placeholder assertions that will be activated
 * once the actual ProjectMemberController is implemented.
 */
@DisplayName("Project Member API Integration Tests")
public class ProjectMemberControllerIntegrationTest extends ProjectIntegrationTestBase {

    @Nested
    @DisplayName("Project Member CRUD Operations")
    class ProjectMemberCrudOperations {

        @Test
        @DisplayName("Should add member to project with valid data")
        void shouldAddMemberToProject() throws Exception {
            // Create a test project first
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Add member to project
            MvcResult result = mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.id").exists())
                    .andExpected(jsonPath("$.userId").value(1))
                    .andExpected(jsonPath("$.role").value("MEMBER"))
                    .andExpected(jsonPath("$.permissions").isArray())
                    .andExpected(jsonPath("$.joinedAt").exists())
                    .andReturn();

            // Verify the member data
            String response = result.getResponse().getContentAsString();
            assertMemberResponse(response, projectTestData.getValidProjectMemberRequest());
        }

        @Test
        @DisplayName("Should return validation error for invalid member data")
        void shouldReturnValidationErrorForInvalidMemberData() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Invalid member request (missing required fields)
            Map<String, Object> invalidMember = Map.of("role", "INVALID_ROLE");

            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidMember)))
                    .andExpected(status().isBadRequest())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.message").exists())
                    .andExpected(jsonPath("$.details").exists());
        }

        @Test
        @DisplayName("Should list all project members")
        void shouldListAllProjectMembers() throws Exception {
            // Create project and add multiple members
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            addTestProjectMember(adminToken, projectId, testEmployee.getId());
            addTestProjectMember(adminToken, projectId, testTeamLeader.getId());

            // List all members
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$").isArray())
                    .andExpected(jsonPath("$").isNotEmpty())
                    .andExpected(jsonPath("$[*].userId").exists())
                    .andExpected(jsonPath("$[*].role").exists())
                    .andExpected(jsonPath("$[*].permissions").exists());
        }

        @Test
        @DisplayName("Should get member details by ID")
        void shouldGetMemberDetailsById() throws Exception {
            // Create project and add member
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);

            // Get member details
            mockMvc.perform(get("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.id").value(memberId))
                    .andExpected(jsonPath("$.userId").exists())
                    .andExpected(jsonPath("$.role").exists())
                    .andExpected(jsonPath("$.user.username").exists())
                    .andExpected(jsonPath("$.user.fullName").exists());
        }

        @Test
        @DisplayName("Should update member role and permissions")
        void shouldUpdateMemberRoleAndPermissions() throws Exception {
            // Create project and add member
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);

            // Update member role
            mockMvc.perform(put("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberUpdateRequest())))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.id").value(memberId))
                    .andExpected(jsonPath("$.role").value("PROJECT_MANAGER"))
                    .andExpected(jsonPath("$.permissions").isArray());
        }

        @Test
        @DisplayName("Should remove member from project")
        void shouldRemoveMemberFromProject() throws Exception {
            // Create project and add member
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);

            // Remove member
            mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNoContent());

            // Verify member is removed
            mockMvc.perform(get("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Project Member Permission Controls")
    class ProjectMemberPermissionControls {

        @Test
        @DisplayName("Should deny member management for unauthorized users")
        void shouldDenyMemberManagementForUnauthorizedUsers() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Unauthorized request (no token)
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpected(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should only allow project managers to add members")
        void shouldOnlyAllowProjectManagersToAddMembers() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Regular employee should not be able to add members
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(employeeToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());

            // Manager should be able to add members
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(managerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpected(status().isCreated());
        }

        @Test
        @DisplayName("Should allow project members to view member list")
        void shouldAllowProjectMembersToViewMemberList() throws Exception {
            // Create project and add employee as member
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Employee should be able to view member list
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should deny member list access for non-project members")
        void shouldDenyMemberListAccessForNonMembers() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Employee is not a project member, should be denied access
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should only allow project managers to update member roles")
        void shouldOnlyAllowProjectManagersToUpdateRoles() throws Exception {
            // Create project and add members
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);
            
            // Add team leader as regular member (not project manager)
            addTestProjectMember(adminToken, projectId, testTeamLeader.getId());

            // Team leader should not be able to update member roles
            mockMvc.perform(put("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(teamLeaderToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberUpdateRequest())))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should only allow project managers to remove members")
        void shouldOnlyAllowProjectManagersToRemoveMembers() throws Exception {
            // Create project and add members
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);

            // Employee should not be able to remove members
            mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isForbidden())
                    .andExpected(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should allow members to leave project themselves")
        void shouldAllowMembersToLeaveProject() throws Exception {
            // Create project and add employee as member
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);
            
            MvcResult memberResult = addTestProjectMember(adminToken, projectId, testEmployee.getId());
            Long memberId = extractMemberIdFromResponse(memberResult);

            // Employee should be able to leave the project
            mockMvc.perform(delete("/api/projects/{projectId}/members/me", projectId)
                    .header("Authorization", getBearerToken(employeeToken)))
                    .andExpected(status().isNoContent());

            // Verify member is no longer in project
            mockMvc.perform(get("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Project Member Role Management")
    class ProjectMemberRoleManagement {

        @Test
        @DisplayName("Should assign PROJECT_MANAGER role correctly")
        void shouldAssignProjectManagerRole() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            Map<String, Object> managerRequest = Map.of(
                "userId", testManager.getId(),
                "role", "PROJECT_MANAGER",
                "permissions", new String[]{"READ", "WRITE", "MANAGE", "DELETE"}
            );

            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(managerRequest)))
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.role").value("PROJECT_MANAGER"))
                    .andExpected(jsonPath("$.permissions").isArray())
                    .andExpected(jsonPath("$.permissions", org.hamcrest.Matchers.hasItems("MANAGE", "DELETE")));
        }

        @Test
        @DisplayName("Should assign MEMBER role with limited permissions")
        void shouldAssignMemberRoleWithLimitedPermissions() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            Map<String, Object> memberRequest = Map.of(
                "userId", testEmployee.getId(),
                "role", "MEMBER",
                "permissions", new String[]{"READ", "WRITE"}
            );

            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(memberRequest)))
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.role").value("MEMBER"))
                    .andExpected(jsonPath("$.permissions").isArray())
                    .andExpected(jsonPath("$.permissions", org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItems("MANAGE", "DELETE"))));
        }

        @Test
        @DisplayName("Should assign OBSERVER role with read-only permissions")
        void shouldAssignObserverRoleWithReadOnlyPermissions() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            Map<String, Object> observerRequest = Map.of(
                "userId", testEmployee.getId(),
                "role", "OBSERVER",
                "permissions", new String[]{"READ"}
            );

            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(observerRequest)))
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.role").value("OBSERVER"))
                    .andExpected(jsonPath("$.permissions").isArray())
                    .andExpected(jsonPath("$.permissions", org.hamcrest.Matchers.hasSize(1)))
                    .andExpected(jsonPath("$.permissions[0]").value("READ"));
        }

        @Test
        @DisplayName("Should prevent duplicate member assignments")
        void shouldPreventDuplicateMemberAssignments() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Add member first time
            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Try to add same member again
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(projectTestData.getValidProjectMemberRequest())))
                    .andExpected(status().isConflict())
                    .andExpected(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already a member")));
        }
    }

    @Nested
    @DisplayName("Project Member Search and Filtering")
    class ProjectMemberSearchAndFiltering {

        @Test
        @DisplayName("Should filter members by role")
        void shouldFilterMembersByRole() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Add members with different roles
            addTestProjectMember(adminToken, projectId, testEmployee.getId());
            
            Map<String, Object> managerRequest = Map.of(
                "userId", testManager.getId(),
                "role", "PROJECT_MANAGER",
                "permissions", new String[]{"READ", "WRITE", "MANAGE"}
            );
            mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(managerRequest)))
                    .andExpected(status().isCreated());

            // Filter by role
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .param("role", "PROJECT_MANAGER"))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$").isArray())
                    .andExpected(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                    .andExpected(jsonPath("$[0].role").value("PROJECT_MANAGER"));
        }

        @Test
        @DisplayName("Should search members by username")
        void shouldSearchMembersByUsername() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            addTestProjectMember(adminToken, projectId, testEmployee.getId());

            // Search by username
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                    .header("Authorization", getBearerToken(adminToken))
                    .param("search", testEmployee.getUsername()))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$").isArray())
                    .andExpected(jsonPath("$[*].user.username", org.hamcrest.Matchers.hasItem(testEmployee.getUsername())));
        }

        @Test
        @DisplayName("Should get project member statistics")
        void shouldGetProjectMemberStatistics() throws Exception {
            MvcResult projectResult = createTestProject(adminToken);
            Long projectId = extractProjectIdFromResponse(projectResult);

            // Add members with different roles
            addTestProjectMember(adminToken, projectId, testEmployee.getId());
            addTestProjectMember(adminToken, projectId, testTeamLeader.getId());

            // Get member statistics
            mockMvc.perform(get("/api/projects/{projectId}/members/statistics", projectId)
                    .header("Authorization", getBearerToken(adminToken)))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.totalMembers").exists())
                    .andExpected(jsonPath("$.membersByRole").exists())
                    .andExpected(jsonPath("$.activeMembers").exists());
        }
    }
}