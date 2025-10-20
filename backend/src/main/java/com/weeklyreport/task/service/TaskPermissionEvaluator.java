package com.weeklyreport.task.service;

import com.weeklyreport.core.security.CustomUserPrincipal;
import com.weeklyreport.task.entity.Task;
import com.weeklyreport.task.repository.TaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Centralizes task-related permission checks so controllers can rely on
 * Spring Security annotations instead of manual role comparisons.
 */
@Service("taskPermissionEvaluator")
public class TaskPermissionEvaluator {

    private final TaskRepository taskRepository;

    public TaskPermissionEvaluator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Determines whether the current authenticated user may view a task.
     * Managers may only view tasks they created, while admins and super admins
     * can view any task.
     */
    public boolean canViewTask(Long taskId, Authentication authentication) {
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

        // Allow controller to return 404 when the task doesn't exist.
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return true;
        }

        return taskOpt.get().getCreatedBy().equals(principal.getId());
    }

    /**
     * Determines whether the current authenticated user may update or delete a task.
     * Only the creating manager may modify a task in this simplified workflow.
     */
    public boolean canManageTask(Long taskId, Authentication authentication) {
        CustomUserPrincipal principal = extractPrincipal(authentication);
        if (principal == null || !hasAuthority(principal, "ROLE_MANAGER")) {
            return false;
        }

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return true;
        }

        return taskOpt.get().getCreatedBy().equals(principal.getId());
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
