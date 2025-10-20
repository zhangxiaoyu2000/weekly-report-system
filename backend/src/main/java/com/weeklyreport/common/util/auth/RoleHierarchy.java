package com.weeklyreport.common.util.auth;

import com.weeklyreport.user.entity.User;
import java.util.*;

/**
 * Utility class for managing role hierarchy and permissions
 * Supports SUPER_ADMIN, ADMIN and MANAGER roles
 */
public class RoleHierarchy {

    // Role hierarchy: higher roles inherit permissions of lower roles
    private static final Map<User.Role, Set<User.Role>> ROLE_HIERARCHY = new HashMap<>();

    // Role-based permissions
    private static final Map<User.Role, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        initializeRoleHierarchy();
        initializePermissions();
    }

    /**
     * Initialize the role hierarchy
     * Higher roles can perform actions of lower roles
     */
    private static void initializeRoleHierarchy() {
        // SUPER_ADMIN can act as any role
        ROLE_HIERARCHY.put(User.Role.SUPER_ADMIN, EnumSet.of(
            User.Role.SUPER_ADMIN,
            User.Role.ADMIN,
            User.Role.MANAGER
        ));

        // ADMIN can act as admin and manager
        ROLE_HIERARCHY.put(User.Role.ADMIN, EnumSet.of(
            User.Role.ADMIN,
            User.Role.MANAGER
        ));

        // MANAGER can only act as manager
        ROLE_HIERARCHY.put(User.Role.MANAGER, EnumSet.of(
            User.Role.MANAGER
        ));
    }

    /**
     * Initialize role permissions
     */
    private static void initializePermissions() {
        // SUPER_ADMIN permissions - ultimate system access
        ROLE_PERMISSIONS.put(User.Role.SUPER_ADMIN, EnumSet.of(
            Permission.MANAGE_USERS,
            Permission.MANAGE_DEPARTMENTS,
            Permission.MANAGE_SYSTEM,
            Permission.VIEW_ALL_REPORTS,
            Permission.APPROVE_REPORTS,
            Permission.FINAL_APPROVE_REPORTS,
            Permission.MANAGE_TEMPLATES,
            Permission.VIEW_ANALYTICS,
            Permission.MANAGE_ROLES,
            Permission.RESET_PASSWORDS,
            Permission.CREATE_REPORTS,
            Permission.VIEW_OWN_REPORTS,
            Permission.EDIT_OWN_REPORTS,
            Permission.VIEW_PROFILE,
            Permission.EDIT_PROFILE,
            Permission.CREATE_PROJECTS,
            Permission.APPROVE_PROJECTS,
            Permission.FINAL_APPROVE_PROJECTS
        ));

        // ADMIN permissions - full system access
        ROLE_PERMISSIONS.put(User.Role.ADMIN, EnumSet.of(
            Permission.MANAGE_USERS,
            Permission.MANAGE_DEPARTMENTS,
            Permission.MANAGE_SYSTEM,
            Permission.VIEW_ALL_REPORTS,
            Permission.APPROVE_REPORTS,
            Permission.MANAGE_TEMPLATES,
            Permission.VIEW_ANALYTICS,
            Permission.MANAGE_ROLES,
            Permission.RESET_PASSWORDS,
            Permission.CREATE_REPORTS,
            Permission.VIEW_OWN_REPORTS,
            Permission.EDIT_OWN_REPORTS,
            Permission.VIEW_PROFILE,
            Permission.EDIT_PROFILE,
            Permission.CREATE_PROJECTS,
            Permission.APPROVE_PROJECTS
        ));

        // MANAGER permissions - project and report management
        ROLE_PERMISSIONS.put(User.Role.MANAGER, EnumSet.of(
            Permission.CREATE_REPORTS,
            Permission.VIEW_OWN_REPORTS,
            Permission.EDIT_OWN_REPORTS,
            Permission.VIEW_PROFILE,
            Permission.EDIT_PROFILE,
            Permission.CREATE_PROJECTS,
            Permission.MANAGER_APPROVE_REPORTS,
            Permission.MANAGER_APPROVE_PROJECTS
        ));
    }

    /**
     * Check if a role can perform actions of another role
     */
    public static boolean canActAs(User.Role userRole, User.Role requiredRole) {
        Set<User.Role> allowedRoles = ROLE_HIERARCHY.get(userRole);
        return allowedRoles != null && allowedRoles.contains(requiredRole);
    }

    /**
     * Check if a role has a specific permission
     */
    public static boolean hasPermission(User.Role role, Permission permission) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Check if a user has a specific permission
     */
    public static boolean hasPermission(User user, Permission permission) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return hasPermission(user.getRole(), permission);
    }

    /**
     * Get all permissions for a role
     */
    public static Set<Permission> getPermissions(User.Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    /**
     * Check if user can manage another user based on role hierarchy
     */
    public static boolean canManageUser(User manager, User targetUser) {
        if (manager == null || targetUser == null ||
            manager.getRole() == null || targetUser.getRole() == null) {
            return false;
        }

        // Super admin can manage anyone
        if (manager.getRole() == User.Role.SUPER_ADMIN) {
            return true;
        }

        // Admin can manage anyone except super admin
        if (manager.getRole() == User.Role.ADMIN) {
            return targetUser.getRole() != User.Role.SUPER_ADMIN;
        }

        // Manager can only manage their own profile
        return manager.getId().equals(targetUser.getId());
    }

    /**
     * Check if user can view reports from a department
     */
    public static boolean canViewDepartmentReports(User user, Long departmentId) {
        if (user == null || user.getRole() == null) {
            return false;
        }

        // Super admin and admin can view all departments
        if (user.getRole() == User.Role.SUPER_ADMIN || user.getRole() == User.Role.ADMIN) {
            return true;
        }

        // 简化版本中不支持部门管理
        if (user.getRole() == User.Role.MANAGER) {
            return false; // 简化版本中管理员无部门限制
        }

        return false;
    }

    /**
     * Get the role level (lower number = higher authority)
     */
    public static int getRoleLevel(User.Role role) {
        return switch (role) {
            case SUPER_ADMIN -> 0;
            case ADMIN -> 1;
            case MANAGER -> 2;
            case EMPLOYEE -> 3;
        };
    }

    /**
     * Check if role1 has higher authority than role2
     */
    public static boolean hasHigherAuthority(User.Role role1, User.Role role2) {
        return getRoleLevel(role1) < getRoleLevel(role2);
    }

    /**
     * Check if users are in the same department
     */
    private static boolean isSameDepartment(User user1, User user2) {
        // 简化版本中不支持部门管理，始终返回false
        return false;
    }

    /**
     * Permission enumeration
     */
    public enum Permission {
        // System management
        MANAGE_SYSTEM,
        MANAGE_USERS,
        MANAGE_DEPARTMENTS,
        MANAGE_ROLES,
        RESET_PASSWORDS,

        // Report management
        CREATE_REPORTS,
        VIEW_OWN_REPORTS,
        EDIT_OWN_REPORTS,
        VIEW_ALL_REPORTS,
        APPROVE_REPORTS,

        // Project management
        CREATE_PROJECTS,
        APPROVE_PROJECTS,
        MANAGER_APPROVE_PROJECTS,
        FINAL_APPROVE_PROJECTS,

        // Report management by level
        MANAGER_APPROVE_REPORTS,
        FINAL_APPROVE_REPORTS,

        // Template management
        MANAGE_TEMPLATES,

        // Analytics and statistics
        VIEW_ANALYTICS,

        // User management
        VIEW_PROFILE,
        EDIT_PROFILE
    }
}
