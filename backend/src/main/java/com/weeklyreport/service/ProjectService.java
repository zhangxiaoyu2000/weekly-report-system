package com.weeklyreport.service;

import com.weeklyreport.dto.project.*;
import com.weeklyreport.entity.Department;
import com.weeklyreport.entity.Project;
import com.weeklyreport.entity.User;
import com.weeklyreport.repository.DepartmentRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Project operations
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Create a new project
     */
    public ProjectResponse createProject(ProjectCreateRequest request, Long creatorId) {
        logger.info("Creating new project: {} for user: {}", request.getName(), creatorId);

        // Validate creator exists
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        // Validate project name uniqueness
        if (projectRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), null)) {
            throw new IllegalArgumentException("Project name already exists");
        }

        // Validate department if provided
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }

        // Validate date logic
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
        }

        // Create project entity
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setPriority(request.getPriority() != null ? request.getPriority() : Project.ProjectPriority.MEDIUM);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBudget(request.getBudget());
        project.setTags(request.getTags());
        project.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        project.setCreatedBy(creator);
        project.setDepartment(department);

        // Save project
        Project savedProject = projectRepository.save(project);
        logger.info("Project created successfully with ID: {}", savedProject.getId());

        return new ProjectResponse(savedProject);
    }

    /**
     * Update an existing project
     */
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, Long userId) {
        logger.info("Updating project: {} by user: {}", projectId, userId);

        // Find existing project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Check if user has permission to update (creator or admin)
        if (!canUserModifyProject(project, userId)) {
            throw new SecurityException("User not authorized to update this project");
        }

        // Validate project name uniqueness (excluding current project)
        if (projectRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), projectId)) {
            throw new IllegalArgumentException("Project name already exists");
        }

        // Validate department if provided
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }

        // Validate date logic
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
        }

        // Update project fields
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setPriority(request.getPriority());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBudget(request.getBudget());
        project.setProgress(request.getProgress() != null ? request.getProgress() : project.getProgress());
        project.setTags(request.getTags());
        project.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : project.getIsPublic());
        project.setArchived(request.getArchived() != null ? request.getArchived() : project.getArchived());
        project.setDepartment(department);

        // Save updated project
        Project savedProject = projectRepository.save(project);
        logger.info("Project updated successfully: {}", savedProject.getId());

        return new ProjectResponse(savedProject);
    }

    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId, Long userId) {
        logger.info("Getting project: {} for user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Check if user has permission to view project
        if (!canUserViewProject(project, userId)) {
            throw new SecurityException("User not authorized to view this project");
        }

        return new ProjectResponse(project);
    }

    /**
     * Get projects with filtering and pagination
     */
    @Transactional(readOnly = true)
    public ProjectListResponse getProjects(ProjectFilterRequest filter, Long userId) {
        logger.info("Getting projects with filter for user: {}", userId);

        // Validate filter parameters
        validateFilterRequest(filter);

        // Create pageable
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Get user for permission checks
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Build query based on filters
        Page<Project> projectPage;
        if (filter.hasFilters()) {
            projectPage = projectRepository.findWithFilters(
                    filter.getName(),
                    filter.getStatus(),
                    filter.getPriority(),
                    filter.getCreatedById() != null ? userRepository.findById(filter.getCreatedById()).orElse(null) : null,
                    filter.getDepartmentId(),
                    filter.getIsPublic(),
                    filter.getArchived(),
                    pageable
            );
        } else {
            // Default: show accessible projects for user
            projectPage = projectRepository.findAccessibleProjects(user, pageable);
        }

        // Convert to response DTOs
        List<ProjectResponse> projectResponses = projectPage.getContent()
                .stream()
                .filter(project -> canUserViewProject(project, userId))
                .map(ProjectResponse::new)
                .collect(Collectors.toList());

        return new ProjectListResponse(projectResponses, projectPage);
    }

    /**
     * Delete project (soft delete)
     */
    public void deleteProject(Long projectId, Long userId) {
        logger.info("Deleting project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Check if user has permission to delete (creator or admin)
        if (!canUserModifyProject(project, userId)) {
            throw new SecurityException("User not authorized to delete this project");
        }

        // Soft delete (using @SQLDelete annotation)
        projectRepository.delete(project);
        logger.info("Project deleted successfully: {}", projectId);
    }

    /**
     * Get project statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getProjectStatistics() {
        logger.info("Getting project statistics");
        return projectRepository.getProjectStatsByStatus();
    }

    /**
     * Get overdue projects
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getOverdueProjects(Long userId) {
        logger.info("Getting overdue projects for user: {}", userId);
        
        List<Project> overdueProjects = projectRepository.findOverdueProjects(LocalDate.now());
        
        return overdueProjects.stream()
                .filter(project -> canUserViewProject(project, userId))
                .map(ProjectResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get projects ending soon
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsEndingSoon(int days, Long userId) {
        logger.info("Getting projects ending in {} days for user: {}", days, userId);
        
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.plusDays(days);
        
        List<Project> projectsEndingSoon = projectRepository.findProjectsEndingSoon(currentDate, endDate);
        
        return projectsEndingSoon.stream()
                .filter(project -> canUserViewProject(project, userId))
                .map(ProjectResponse::new)
                .collect(Collectors.toList());
    }

    // Helper methods

    private boolean canUserViewProject(Project project, Long userId) {
        // User can view if:
        // 1. Project is public
        // 2. User is the creator
        // 3. User is admin (check user role)
        if (project.getIsPublic()) {
            return true;
        }
        
        if (project.getCreatedBy().getId().equals(userId)) {
            return true;
        }
        
        // Check if user is admin
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.HR_MANAGER;
        }
        
        return false;
    }

    private boolean canUserModifyProject(Project project, Long userId) {
        // User can modify if:
        // 1. User is the creator
        // 2. User is admin
        if (project.getCreatedBy().getId().equals(userId)) {
            return true;
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.HR_MANAGER;
        }
        
        return false;
    }

    private void validateFilterRequest(ProjectFilterRequest filter) {
        if (!filter.isValidSortDirection()) {
            throw new IllegalArgumentException("Invalid sort direction. Use ASC or DESC");
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        // Validate sortBy field
        String[] validSortFields = {"id", "name", "status", "priority", "progress", "createdAt", "updatedAt", "startDate", "endDate"};
        boolean isValidField = false;
        for (String field : validSortFields) {
            if (field.equals(sortBy)) {
                isValidField = true;
                break;
            }
        }
        
        if (!isValidField) {
            sortBy = "createdAt"; // default
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        return Sort.by(direction, sortBy);
    }
}