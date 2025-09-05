package com.weeklyreport.controller;

import com.weeklyreport.dto.ApiResponse;
import com.weeklyreport.dto.project.*;
import com.weeklyreport.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Project operations
 * Handles CRUD operations for projects
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    /**
     * Create a new project
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Creating new project: {}", request.getName());

            // TODO: Extract user ID from JWT token
            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            ProjectResponse project = projectService.createProject(request, userId);
            logger.info("Project created successfully with ID: {}", project.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Project created successfully", project));

        } catch (IllegalArgumentException e) {
            logger.warn("Project creation failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Project creation error - {}", e.getMessage(), e);
            return error("Project creation failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing project
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Updating project: {}", id);

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            ProjectResponse project = projectService.updateProject(id, request, userId);
            logger.info("Project updated successfully: {}", project.getId());

            return success("Project updated successfully", project);

        } catch (IllegalArgumentException e) {
            logger.warn("Project update failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            logger.warn("Project update unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Project update error - {}", e.getMessage(), e);
            return error("Project update failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get project by ID
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Getting project: {}", id);

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            ProjectResponse project = projectService.getProject(id, userId);
            return success("Project retrieved successfully", project);

        } catch (IllegalArgumentException e) {
            logger.warn("Project retrieval failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Project access unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Project retrieval error - {}", e.getMessage(), e);
            return error("Project retrieval failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get projects with filtering and pagination
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ProjectListResponse>> getProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long createdById,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Getting projects with filters - page: {}, size: {}", page, size);

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            // Build filter request
            ProjectFilterRequest filter = new ProjectFilterRequest();
            filter.setName(name);
            filter.setDescription(description);
            filter.setStatus(status != null ? parseProjectStatus(status) : null);
            filter.setPriority(priority != null ? parseProjectPriority(priority) : null);
            filter.setCreatedById(createdById);
            filter.setDepartmentId(departmentId);
            filter.setIsPublic(isPublic);
            filter.setArchived(archived);
            filter.setTags(tags);
            filter.setPage(page);
            filter.setSize(size);
            filter.setSortBy(sortBy);
            filter.setSortDirection(sortDirection);

            ProjectListResponse projects = projectService.getProjects(filter, userId);
            return success("Projects retrieved successfully", projects);

        } catch (IllegalArgumentException e) {
            logger.warn("Project list retrieval failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Project list retrieval error - {}", e.getMessage(), e);
            return error("Project list retrieval failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete project
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProject(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Deleting project: {}", id);

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            projectService.deleteProject(id, userId);
            logger.info("Project deleted successfully: {}", id);

            return success("Project deleted successfully", "");

        } catch (IllegalArgumentException e) {
            logger.warn("Project deletion failed - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (SecurityException e) {
            logger.warn("Project deletion unauthorized - {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Project deletion error - {}", e.getMessage(), e);
            return error("Project deletion failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get project statistics
     * GET /api/projects/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<Object[]>>> getProjectStatistics(HttpServletRequest httpRequest) {
        try {
            logger.info("Getting project statistics");

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            List<Object[]> statistics = projectService.getProjectStatistics();
            return success("Project statistics retrieved successfully", statistics);

        } catch (Exception e) {
            logger.error("Project statistics error - {}", e.getMessage(), e);
            return error("Project statistics failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get overdue projects
     * GET /api/projects/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getOverdueProjects(HttpServletRequest httpRequest) {
        try {
            logger.info("Getting overdue projects");

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            List<ProjectResponse> overdueProjects = projectService.getOverdueProjects(userId);
            return success("Overdue projects retrieved successfully", overdueProjects);

        } catch (Exception e) {
            logger.error("Overdue projects error - {}", e.getMessage(), e);
            return error("Overdue projects retrieval failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get projects ending soon
     * GET /api/projects/ending-soon?days={days}
     */
    @GetMapping("/ending-soon")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsEndingSoon(
            @RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest httpRequest) {
        try {
            logger.info("Getting projects ending in {} days", days);

            Long userId = extractUserIdFromRequest(httpRequest);
            if (userId == null) {
                return error("Authentication required", HttpStatus.UNAUTHORIZED);
            }

            List<ProjectResponse> projectsEndingSoon = projectService.getProjectsEndingSoon(days, userId);
            return success("Projects ending soon retrieved successfully", projectsEndingSoon);

        } catch (Exception e) {
            logger.error("Projects ending soon error - {}", e.getMessage(), e);
            return error("Projects ending soon retrieval failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods

    /**
     * Extract user ID from JWT token in request
     * TODO: This will be properly implemented when authentication is integrated
     */
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        // TODO: Implement actual JWT token parsing
        // return jwtTokenProvider.getUserIdFromToken(token);
        return 1L; // Temporary placeholder - return admin user ID
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Parse project status from string
     */
    private com.weeklyreport.entity.Project.ProjectStatus parseProjectStatus(String status) {
        try {
            return com.weeklyreport.entity.Project.ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid project status: " + status);
        }
    }

    /**
     * Parse project priority from string
     */
    private com.weeklyreport.entity.Project.ProjectPriority parseProjectPriority(String priority) {
        try {
            return com.weeklyreport.entity.Project.ProjectPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid project priority: " + priority);
        }
    }
}