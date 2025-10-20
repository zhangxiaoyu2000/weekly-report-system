package com.weeklyreport.common.util.auth;

import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Helper component for authorization checks in service methods
 */
@Component
public class AuthorizationHelper {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationHelper.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if current user can manage target user
     */
    public void checkUserManagementPermission(String currentUsername, Long targetUserId) {
        User currentUser = getCurrentUser(currentUsername);
        User targetUser = getTargetUser(targetUserId);

        if (!RoleHierarchy.canManageUser(currentUser, targetUser)) {
            logger.warn("Access denied: User {} cannot manage user {}",
                       currentUsername, targetUser.getUsername());
            throw new AccessDeniedException("访问被拒绝：无权管理此用户");
        }
    }

    /**
     * Check if current user can manage target user by username
     */
    public void checkUserManagementPermission(String currentUsername, String targetUsername) {
        User currentUser = getCurrentUser(currentUsername);
        User targetUser = userRepository.findByUsername(targetUsername)
            .orElseThrow(() -> new UsernameNotFoundException("Target user not found: " + targetUsername));

        if (!RoleHierarchy.canManageUser(currentUser, targetUser)) {
            logger.warn("Access denied: User {} cannot manage user {}",
                       currentUsername, targetUsername);
            throw new AccessDeniedException("访问被拒绝：无权管理此用户");
        }
    }

    /**
     * Check if current user can access department resources
     */
    public void checkDepartmentAccess(String currentUsername, Long departmentId) {
        User currentUser = getCurrentUser(currentUsername);

        if (!RoleHierarchy.canViewDepartmentReports(currentUser, departmentId)) {
            logger.warn("Access denied: User {} cannot access department {}",
                       currentUsername, departmentId);
            throw new AccessDeniedException("访问被拒绝：无权访问此部门");
        }
    }

    /**
     * Check if current user has specific permission
     */
    public void checkPermission(String currentUsername, RoleHierarchy.Permission permission) {
        User currentUser = getCurrentUser(currentUsername);

        if (!RoleHierarchy.hasPermission(currentUser, permission)) {
            logger.warn("Access denied: User {} lacks permission {}",
                       currentUsername, permission);
            throw new AccessDeniedException("访问被拒绝：权限不足");
        }
    }

    /**
     * Check if current user can perform admin operations
     */
    public void checkAdminPermission(String currentUsername) {
        checkPermission(currentUsername, RoleHierarchy.Permission.MANAGE_SYSTEM);
    }

    /**
     * Check if current user can manage users
     */
    public void checkUserManagementPermission(String currentUsername) {
        checkPermission(currentUsername, RoleHierarchy.Permission.MANAGE_USERS);
    }

    /**
     * Check if current user can manage departments
     */
    public void checkDepartmentManagementPermission(String currentUsername) {
        checkPermission(currentUsername, RoleHierarchy.Permission.MANAGE_DEPARTMENTS);
    }

    /**
     * Check if current user can view all reports
     */
    public void checkReportViewingPermission(String currentUsername) {
        checkPermission(currentUsername, RoleHierarchy.Permission.VIEW_ALL_REPORTS);
    }

    /**
     * Check if user can access their own resource or has management rights
     */
    public void checkResourceOwnershipOrManagement(String currentUsername, String resourceOwnerUsername) {
        // Allow access to own resources
        if (currentUsername.equals(resourceOwnerUsername)) {
            return;
        }

        // Check management permission for other users' resources
        checkUserManagementPermission(currentUsername, resourceOwnerUsername);
    }

    /**
     * Check if user can access their own resource by user ID or has management rights
     */
    public void checkResourceOwnershipOrManagement(String currentUsername, Long resourceOwnerId) {
        User currentUser = getCurrentUser(currentUsername);

        // Allow access to own resources
        if (currentUser.getId().equals(resourceOwnerId)) {
            return;
        }

        // Check management permission for other users' resources
        checkUserManagementPermission(currentUsername, resourceOwnerId);
    }

    /**
     * Check if user role is valid for operation
     */
    public void checkRoleValidation(User.Role currentRole, User.Role targetRole, String operation) {
        if (!RoleHierarchy.hasHigherAuthority(currentRole, targetRole) && currentRole != targetRole) {
            logger.warn("Access denied: Role {} cannot perform {} on role {}",
                       currentRole, operation, targetRole);
            throw new AccessDeniedException("访问被拒绝：不能对同级或更高级别的角色操作");
        }
    }

    /**
     * Check if user can assign/modify roles
     */
    public void checkRoleAssignmentPermission(String currentUsername, User.Role targetRole) {
        User currentUser = getCurrentUser(currentUsername);

        // Only admin can assign admin role
        if (targetRole == User.Role.ADMIN && currentUser.getRole() != User.Role.ADMIN) {
            logger.warn("Access denied: Only admin can assign admin role, current user: {}", currentUsername);
            throw new AccessDeniedException("访问被拒绝：只有管理员才能分配管理员角色");
        }

        // Only admin can assign roles in this simplified system

        // Admin can assign any role
        if (currentUser.getRole() == User.Role.ADMIN) {
            return;
        }

        logger.warn("Access denied: User {} cannot assign role {}", currentUsername, targetRole);
        throw new AccessDeniedException("访问被拒绝：无权分配此角色");
    }

    /**
     * Get current user from database
     */
    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Current user not found: " + username));
    }

    /**
     * Get target user from database
     */
    private User getTargetUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Target user not found with ID: " + userId));
    }

    /**
     * Check if user account is active and accessible
     */
    public void checkUserActive(User user) {
        if (!SecurityUtils.isUserActive(user)) {
            logger.warn("Access denied: User account is not active: {}", user.getUsername());
            throw new AccessDeniedException("访问被拒绝：用户账户未激活");
        }
    }

    /**
     * Validate user status for operations
     */
    public void validateUserStatus(User user, String operation) {
        if (SecurityUtils.isUserDeleted(user)) {
            logger.warn("Operation denied: User {} is deleted, operation: {}", user.getUsername(), operation);
            throw new IllegalStateException("不能对已删除的用户执行操作");
        }

        if (SecurityUtils.isUserLocked(user) && !"unlock".equals(operation)) {
            logger.warn("Operation denied: User {} is locked, operation: {}", user.getUsername(), operation);
            throw new IllegalStateException("不能对已锁定的用户执行操作");
        }
    }
}
