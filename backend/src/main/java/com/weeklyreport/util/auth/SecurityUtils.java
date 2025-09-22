package com.weeklyreport.util.auth;

import com.weeklyreport.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Security utility class for authentication and authorization checks
 */
public class SecurityUtils {

    /**
     * Get the current authenticated user's username
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        } else if (principal instanceof String username) {
            return Optional.of(username);
        }
        
        return Optional.empty();
    }

    /**
     * Get the current authentication object
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Check if the current user is authenticated
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication().isPresent();
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentAuthentication()
            .map(auth -> auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)))
            .orElse(false);
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        return getCurrentAuthentication()
            .map(auth -> {
                for (String role : roles) {
                    if (auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role))) {
                        return true;
                    }
                }
                return false;
            })
            .orElse(false);
    }

    /**
     * Check if the current user has admin role
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user has HR manager role
     */
    public static boolean isHR() {
        return hasRole("HR_MANAGER");
    }

    /**
     * Check if the current user has department manager role
     */
    public static boolean isDepartmentManager() {
        return hasRole("DEPARTMENT_MANAGER");
    }

    /**
     * Check if the current user has team leader role
     */
    public static boolean isTeamLeader() {
        return hasRole("TEAM_LEADER");
    }

    /**
     * Check if the current user has employee role
     */
    public static boolean isEmployee() {
        return hasRole("EMPLOYEE");
    }

    /**
     * Check if the current user is admin or HR manager
     */
    public static boolean isAdminOrHR() {
        return hasAnyRole("ADMIN", "HR_MANAGER");
    }

    /**
     * Check if the current user is a manager (dept manager or team leader)
     */
    public static boolean isManager() {
        return hasAnyRole("DEPARTMENT_MANAGER", "TEAM_LEADER");
    }

    /**
     * Check if the current user can manage users
     */
    public static boolean canManageUsers() {
        return hasAnyRole("ADMIN", "HR_MANAGER");
    }

    /**
     * Check if the current user can view all reports
     */
    public static boolean canViewAllReports() {
        return hasAnyRole("ADMIN", "HR_MANAGER");
    }

    /**
     * Check if the current user can manage departments
     */
    public static boolean canManageDepartments() {
        return hasAnyRole("ADMIN", "HR_MANAGER");
    }

    /**
     * Check if user can access resource based on user role and resource constraints
     */
    public static boolean canAccessResource(User currentUser, User resourceOwner) {
        if (currentUser == null || resourceOwner == null) {
            return false;
        }

        // Users can always access their own resources
        if (currentUser.getId().equals(resourceOwner.getId())) {
            return true;
        }

        // Check if current user can manage the resource owner
        return RoleHierarchy.canManageUser(currentUser, resourceOwner);
    }

    /**
     * Check if user can access department-level resources
     */
    public static boolean canAccessDepartment(User user, Long departmentId) {
        if (user == null || departmentId == null) {
            return false;
        }

        return RoleHierarchy.canViewDepartmentReports(user, departmentId);
    }

    /**
     * Check if user has a specific permission
     */
    public static boolean hasPermission(User user, RoleHierarchy.Permission permission) {
        return RoleHierarchy.hasPermission(user, permission);
    }


    /**
     * Get role-based Spring Security authorities
     */
    public static String getRoleAuthority(User.Role role) {
        return "ROLE_" + role.name();
    }

    /**
     * Validate if user status allows access
     */
    public static boolean isUserActive(User user) {
        return user != null && user.getStatus() == User.UserStatus.ACTIVE;
    }

    /**
     * Check if user account is locked (simplified: same as inactive)
     */
    public static boolean isUserLocked(User user) {
        return user != null && user.getStatus() == User.UserStatus.INACTIVE;
    }

    /**
     * Check if user is deleted (simplified: same as inactive)
     */
    public static boolean isUserDeleted(User user) {
        return user != null && user.getStatus() == User.UserStatus.INACTIVE;
    }

    /**
     * Check if password change is required
     * This can be extended to check for password expiry policies
     */
    public static boolean isPasswordChangeRequired(User user) {
        // For now, return false. This can be extended with password policy logic
        return false;
    }

    /**
     * Get user display name for security context
     */
    public static String getDisplayName(User user) {
        if (user == null) {
            return "Unknown User";
        }
        return user.getFullName() != null ? user.getFullName() : user.getUsername();
    }
}