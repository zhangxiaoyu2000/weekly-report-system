package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.project.ProjectMemberRequest;
import com.weeklyreport.dto.project.ProjectMemberResponse;
import com.weeklyreport.dto.project.ProjectMemberRoleUpdateRequest;
import com.weeklyreport.dto.project.ProjectMemberStatsResponse;
import com.weeklyreport.security.CustomUserPrincipal;
import com.weeklyreport.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing project members
 */
@RestController
@RequestMapping("/api/projects/{projectId}/members")
@Tag(name = "Project Member Management", description = "APIs for managing project members")
public class ProjectMemberController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectMemberController.class);

    @Autowired
    private ProjectMemberService projectMemberService;

    /**
     * Get all members of a project
     */
    @GetMapping
    @Operation(summary = "Get project members", description = "Get all members of a project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Getting members for project ID: {}, activeOnly: {}", projectId, activeOnly);

        try {
            List<ProjectMemberResponse> members = activeOnly 
                ? projectMemberService.getActiveProjectMembers(projectId, currentUser)
                : projectMemberService.getProjectMembers(projectId, currentUser);

            return ResponseEntity.ok(ApiResponse.success(members, "Project members retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting project members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get project members", e.getMessage()));
        }
    }

    /**
     * Get project members with pagination
     */
    @GetMapping("/paged")
    @Operation(summary = "Get project members with pagination", description = "Get project members with pagination support")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectMemberResponse>>> getProjectMembersPaged(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Getting paged members for project ID: {}", projectId);

        try {
            Page<ProjectMemberResponse> membersPage = projectMemberService.getProjectMembers(projectId, pageable, currentUser);
            return ResponseEntity.ok(ApiResponse.success(membersPage, "Project members retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting paged project members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get project members", e.getMessage()));
        }
    }

    /**
     * Add a member to a project
     */
    @PostMapping
    @Operation(summary = "Add project member", description = "Add a new member to a project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Adding member to project ID: {}", projectId);

        try {
            ProjectMemberResponse member = projectMemberService.addProjectMember(projectId, request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(member, "Project member added successfully"));
        } catch (Exception e) {
            logger.error("Error adding project member: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to add project member", e.getMessage()));
        }
    }

    /**
     * Update a project member's role
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update member role", description = "Update a project member's role")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody ProjectMemberRoleUpdateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Updating member role for user {} in project {}", userId, projectId);

        try {
            ProjectMemberResponse member = projectMemberService.updateMemberRole(projectId, userId, request, currentUser);
            return ResponseEntity.ok(ApiResponse.success(member, "Member role updated successfully"));
        } catch (Exception e) {
            logger.error("Error updating member role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update member role", e.getMessage()));
        }
    }

    /**
     * Remove a member from a project
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove project member", description = "Remove a member from a project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Removing member {} from project {}", userId, projectId);

        try {
            projectMemberService.removeProjectMember(projectId, userId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
        } catch (Exception e) {
            logger.error("Error removing project member: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to remove project member", e.getMessage()));
        }
    }

    /**
     * Get project member statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get member statistics", description = "Get project member statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectMemberStatsResponse>> getProjectMemberStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Getting member statistics for project ID: {}", projectId);

        try {
            ProjectMemberStatsResponse stats = projectMemberService.getProjectMemberStats(projectId, currentUser);
            return ResponseEntity.ok(ApiResponse.success(stats, "Member statistics retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting member statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get member statistics", e.getMessage()));
        }
    }

    /**
     * Search project members
     */
    @GetMapping("/search")
    @Operation(summary = "Search project members", description = "Search project members by name, username, or email")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> searchProjectMembers(
            @PathVariable Long projectId,
            @RequestParam String q,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Searching members in project ID: {} with term: {}", projectId, q);

        try {
            List<ProjectMemberResponse> members = projectMemberService.searchProjectMembers(projectId, q, currentUser);
            return ResponseEntity.ok(ApiResponse.success(members, "Search completed successfully"));
        } catch (Exception e) {
            logger.error("Error searching project members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search project members", e.getMessage()));
        }
    }

    /**
     * Check if user is a member of the project
     */
    @GetMapping("/check/{userId}")
    @Operation(summary = "Check membership", description = "Check if a user is a member of the project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkMembership(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        
        logger.debug("Checking membership for user {} in project {}", userId, projectId);

        try {
            boolean isMember = projectMemberService.isProjectMember(projectId, userId);
            return ResponseEntity.ok(ApiResponse.success(isMember, "Membership check completed"));
        } catch (Exception e) {
            logger.error("Error checking membership: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check membership", e.getMessage()));
        }
    }

    /**
     * Check if user has management permission in the project
     */
    @GetMapping("/check/{userId}/management")
    @Operation(summary = "Check management permission", description = "Check if a user has management permission in the project")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> checkManagementPermission(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        
        logger.debug("Checking management permission for user {} in project {}", userId, projectId);

        try {
            boolean hasPermission = projectMemberService.hasManagementPermission(projectId, userId);
            return ResponseEntity.ok(ApiResponse.success(hasPermission, "Permission check completed"));
        } catch (Exception e) {
            logger.error("Error checking management permission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check management permission", e.getMessage()));
        }
    }
}

/**
 * Separate controller for user's project memberships
 */
@RestController
@RequestMapping("/api/users/{userId}/project-memberships")
@Tag(name = "User Project Memberships", description = "APIs for managing user's project memberships")
class UserProjectMembershipController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserProjectMembershipController.class);

    @Autowired
    private ProjectMemberService projectMemberService;

    /**
     * Get user's project memberships
     */
    @GetMapping
    @Operation(summary = "Get user project memberships", description = "Get all projects where user is a member")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getUserProjectMemberships(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        logger.debug("Getting project memberships for user ID: {}", userId);

        try {
            List<ProjectMemberResponse> memberships = projectMemberService.getUserProjectMemberships(userId, currentUser);
            return ResponseEntity.ok(ApiResponse.success(memberships, "Project memberships retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error getting project memberships: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get project memberships", e.getMessage()));
        }
    }
}