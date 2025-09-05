package com.weeklyreport.service;

import com.weeklyreport.dto.project.ProjectMemberRequest;
import com.weeklyreport.dto.project.ProjectMemberResponse;
import com.weeklyreport.dto.project.ProjectMemberRoleUpdateRequest;
import com.weeklyreport.dto.project.ProjectMemberStatsResponse;
import com.weeklyreport.entity.Project;
import com.weeklyreport.entity.ProjectMember;
import com.weeklyreport.entity.User;
import com.weeklyreport.exception.UserManagementException;
import com.weeklyreport.repository.ProjectMemberRepository;
import com.weeklyreport.repository.ProjectRepository;
import com.weeklyreport.repository.UserRepository;
import com.weeklyreport.security.CustomUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing project members
 */
@Service
@Transactional
public class ProjectMemberService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectMemberService.class);

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all members of a project
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, CustomUserPrincipal currentUser) {
        logger.debug("Getting members for project ID: {}", projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active members of a project
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getActiveProjectMembers(Long projectId, CustomUserPrincipal currentUser) {
        logger.debug("Getting active members for project ID: {}", projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        List<ProjectMember> members = projectMemberRepository.findActiveByProjectId(projectId);
        return members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get project members with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProjectMemberResponse> getProjectMembers(Long projectId, Pageable pageable, CustomUserPrincipal currentUser) {
        logger.debug("Getting members for project ID: {} with pagination", projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        Page<ProjectMember> membersPage = projectMemberRepository.findByProject(project, pageable);
        List<ProjectMemberResponse> responses = membersPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, membersPage.getTotalElements());
    }

    /**
     * Add a member to a project
     */
    public ProjectMemberResponse addProjectMember(Long projectId, ProjectMemberRequest request, CustomUserPrincipal currentUser) {
        logger.debug("Adding member {} to project ID: {}", request.getUserId(), projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectManagementPermission(project, currentUser);

        User user = getUserOrThrow(request.getUserId());
        
        // Check if user is already a member
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new UserManagementException("User is already a member of this project");
        }

        // Create and save project member
        ProjectMember projectMember = new ProjectMember(project, user, request.getRole(), currentUser.getId());
        projectMember.setNotes(request.getNotes());
        projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);

        projectMember = projectMemberRepository.save(projectMember);
        
        logger.info("Added user {} to project {} with role {}", user.getUsername(), project.getName(), request.getRole());
        return convertToResponse(projectMember);
    }

    /**
     * Update a project member's role
     */
    public ProjectMemberResponse updateMemberRole(Long projectId, Long userId, ProjectMemberRoleUpdateRequest request, CustomUserPrincipal currentUser) {
        logger.debug("Updating role for user {} in project ID: {}", userId, projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectManagementPermission(project, currentUser);

        ProjectMember projectMember = getProjectMemberOrThrow(projectId, userId);
        
        // Prevent removing the last project manager
        if (projectMember.getRole() == ProjectMember.ProjectRole.PROJECT_MANAGER && 
            request.getRole() != ProjectMember.ProjectRole.PROJECT_MANAGER) {
            long managerCount = projectMemberRepository.countMembersByRole(project, ProjectMember.ProjectRole.PROJECT_MANAGER);
            if (managerCount <= 1) {
                throw new UserManagementException("Cannot remove the last project manager");
            }
        }

        projectMember.setRole(request.getRole());
        if (request.getNotes() != null) {
            projectMember.setNotes(request.getNotes());
        }
        
        projectMember = projectMemberRepository.save(projectMember);
        
        logger.info("Updated role for user {} in project {} to {}", userId, project.getName(), request.getRole());
        return convertToResponse(projectMember);
    }

    /**
     * Remove a member from a project
     */
    public void removeProjectMember(Long projectId, Long userId, CustomUserPrincipal currentUser) {
        logger.debug("Removing user {} from project ID: {}", userId, projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectManagementPermission(project, currentUser);

        ProjectMember projectMember = getProjectMemberOrThrow(projectId, userId);
        
        // Prevent removing the last project manager
        if (projectMember.getRole() == ProjectMember.ProjectRole.PROJECT_MANAGER) {
            long managerCount = projectMemberRepository.countMembersByRole(project, ProjectMember.ProjectRole.PROJECT_MANAGER);
            if (managerCount <= 1) {
                throw new UserManagementException("Cannot remove the last project manager");
            }
        }

        // Soft delete - update status and left date
        projectMember.setStatus(ProjectMember.MemberStatus.REMOVED);
        projectMember.setLeftDate(LocalDateTime.now());
        projectMemberRepository.save(projectMember);
        
        logger.info("Removed user {} from project {}", userId, project.getName());
    }

    /**
     * Get project member statistics
     */
    @Transactional(readOnly = true)
    public ProjectMemberStatsResponse getProjectMemberStats(Long projectId, CustomUserPrincipal currentUser) {
        logger.debug("Getting member statistics for project ID: {}", projectId);

        Project project = getProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        ProjectMemberStatsResponse stats = new ProjectMemberStatsResponse(projectId, project.getName());
        
        List<ProjectMember> allMembers = projectMemberRepository.findByProjectId(projectId);
        
        stats.setTotalMembers(allMembers.size());
        stats.setActiveMembers(allMembers.stream()
                .filter(m -> m.getStatus() == ProjectMember.MemberStatus.ACTIVE)
                .count());
        stats.setInactiveMembers(allMembers.stream()
                .filter(m -> m.getStatus() == ProjectMember.MemberStatus.INACTIVE)
                .count());
        stats.setInvitedMembers(allMembers.stream()
                .filter(m -> m.getStatus() == ProjectMember.MemberStatus.INVITED)
                .count());
        stats.setRemovedMembers(allMembers.stream()
                .filter(m -> m.getStatus() == ProjectMember.MemberStatus.REMOVED)
                .count());

        // Get member counts by role
        Map<ProjectMember.ProjectRole, Long> membersByRole = allMembers.stream()
                .filter(m -> m.getStatus() == ProjectMember.MemberStatus.ACTIVE)
                .collect(Collectors.groupingBy(ProjectMember::getRole, Collectors.counting()));
        
        stats.setMembersByRole(membersByRole);

        return stats;
    }

    /**
     * Check if user is a member of the project
     */
    @Transactional(readOnly = true)
    public boolean isProjectMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    /**
     * Check if user has management permission in the project
     */
    @Transactional(readOnly = true)
    public boolean hasManagementPermission(Long projectId, Long userId) {
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (memberOpt.isEmpty()) {
            return false;
        }
        
        ProjectMember member = memberOpt.get();
        return member.hasManagementPermission() && member.getStatus() == ProjectMember.MemberStatus.ACTIVE;
    }

    /**
     * Get projects where user is a member
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getUserProjectMemberships(Long userId, CustomUserPrincipal currentUser) {
        logger.debug("Getting project memberships for user ID: {}", userId);

        // Users can only view their own memberships unless they are admin
        if (!currentUser.getId().equals(userId) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }

        User user = getUserOrThrow(userId);
        List<ProjectMember> memberships = projectMemberRepository.findActiveByUser(user);
        
        return memberships.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search project members
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> searchProjectMembers(Long projectId, String searchTerm, CustomUserPrincipal currentUser) {
        logger.debug("Searching members in project ID: {} with term: {}", projectId, searchTerm);

        Project project = getProjectOrThrow(projectId);
        validateProjectAccess(project, currentUser);

        List<ProjectMember> members = projectMemberRepository.searchMembers(project, searchTerm);
        return members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new UserManagementException("Project not found with ID: " + projectId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserManagementException("User not found with ID: " + userId));
    }

    private ProjectMember getProjectMemberOrThrow(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new UserManagementException("User is not a member of this project"));
    }

    private void validateProjectAccess(Project project, CustomUserPrincipal currentUser) {
        // Project owner, admin, or project members can access
        if (project.getOwner().getId().equals(currentUser.getId()) ||
            currentUser.isAdmin() ||
            isProjectMember(project.getId(), currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("Access denied to project");
    }

    private void validateProjectManagementPermission(Project project, CustomUserPrincipal currentUser) {
        // Project owner, admin, or members with management permission can manage
        if (project.getOwner().getId().equals(currentUser.getId()) ||
            currentUser.isAdmin() ||
            hasManagementPermission(project.getId(), currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("Access denied - insufficient permissions to manage project members");
    }

    private ProjectMemberResponse convertToResponse(ProjectMember projectMember) {
        ProjectMemberResponse response = new ProjectMemberResponse(projectMember);
        
        // Set invited by name if available
        if (projectMember.getInvitedBy() != null) {
            try {
                User inviter = userRepository.findById(projectMember.getInvitedBy()).orElse(null);
                if (inviter != null) {
                    response.setInvitedByName(inviter.getFullName());
                }
            } catch (Exception e) {
                logger.warn("Could not fetch inviter name for member ID: {}", projectMember.getId());
            }
        }
        
        return response;
    }
}