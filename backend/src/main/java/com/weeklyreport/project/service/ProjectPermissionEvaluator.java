package com.weeklyreport.project.service;

import com.weeklyreport.core.security.CustomUserPrincipal;
import com.weeklyreport.project.entity.Project;
import com.weeklyreport.project.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Encapsulates project-related authorization rules so controllers can use
 * declarative security annotations instead of duplicating role checks.
 */
@Service("projectPermissionEvaluator")
public class ProjectPermissionEvaluator {

    private final ProjectRepository projectRepository;

    public ProjectPermissionEvaluator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Managers may view their own projects, while admins and super admins
     * can view any project.
     */
    public boolean canViewProject(Long projectId, Authentication authentication) {
        CustomUserPrincipal principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (hasAnyAuthority(principal, "ROLE_ADMIN", "ROLE_SUPER_ADMIN")) {
            return true;
        }

        if (!hasAuthority(principal, "ROLE_MANAGER")) {
            return false;
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return true;
        }

        return projectOpt.get().getCreatedBy().equals(principal.getId());
    }

    /**
     * Managers can modify their own projects. This mirrors the previous
     * controller-level checks that limited updates, deletes and submissions
     * to the original creator.
     */
    public boolean canModifyProject(Long projectId, Authentication authentication) {
        CustomUserPrincipal principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (!hasAuthority(principal, "ROLE_MANAGER")) {
            return false;
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return true;
        }

        return projectOpt.get().getCreatedBy().equals(principal.getId());
    }

    /**
     * Managers can manage phases of their own projects; admins and super admins
     * can manage phases for any project.
     */
    public boolean canManageProjectPhases(Long projectId, Authentication authentication) {
        CustomUserPrincipal principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (hasAnyAuthority(principal, "ROLE_ADMIN", "ROLE_SUPER_ADMIN")) {
            return true;
        }

        if (!hasAuthority(principal, "ROLE_MANAGER")) {
            return false;
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return true;
        }

        return projectOpt.get().getCreatedBy().equals(principal.getId());
    }

    /**
     * Admin-level rejection is restricted by review status: admins may reject
     * during their review window, while super admins handle later stages.
     */
    public boolean canRejectProject(Long projectId, Authentication authentication) {
        CustomUserPrincipal principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        boolean isAdmin = hasAuthority(principal, "ROLE_ADMIN");
        boolean isSuperAdmin = hasAuthority(principal, "ROLE_SUPER_ADMIN");
        if (!isAdmin && !isSuperAdmin) {
            return false;
        }

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return true;
        }

        Project project = projectOpt.get();
        Project.ApprovalStatus status = project.getApprovalStatus();

        if (isAdmin) {
            return status == Project.ApprovalStatus.ADMIN_REVIEWING ||
                   status == Project.ApprovalStatus.ADMIN_REJECTED;
        }

        if (isSuperAdmin) {
            return status == Project.ApprovalStatus.ADMIN_APPROVED ||
                   status == Project.ApprovalStatus.ADMIN_REJECTED ||
                   status == Project.ApprovalStatus.SUPER_ADMIN_REVIEWING;
        }

        return false;
    }

    private CustomUserPrincipal extractPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserPrincipal customUserPrincipal) {
            return customUserPrincipal;
        }
        return null;
    }

    private boolean hasAuthority(CustomUserPrincipal principal, String authority) {
        return principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority::equals);
    }

    private boolean hasAnyAuthority(CustomUserPrincipal principal, String... authorities) {
        return Arrays.stream(authorities).anyMatch(auth -> hasAuthority(principal, auth));
    }
}
