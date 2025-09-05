package com.weeklyreport.util.auth;

import com.weeklyreport.entity.User;

import java.util.*;

/**
 * Utility class for managing role hierarchy and permissions
 */
public class RoleHierarchy {

    // Role hierarchy: higher roles inherit permissions of lower roles
    private static final Map<User.Role, Set<User.Role>> ROLE_HIERARCHY = new HashMap<>();
    
    // Department-level permissions
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
        // ADMIN can act as any role
        ROLE_HIERARCHY.put(User.Role.ADMIN, EnumSet.of(
            User.Role.ADMIN,
            User.Role.HR_MANAGER,
            User.Role.DEPARTMENT_MANAGER,
            User.Role.TEAM_LEADER,
            User.Role.EMPLOYEE
        ));

        // HR_MANAGER can act as department manager and below
        ROLE_HIERARCHY.put(User.Role.HR_MANAGER, EnumSet.of(
            User.Role.HR_MANAGER,
            User.Role.DEPARTMENT_MANAGER,
            User.Role.TEAM_LEADER,
            User.Role.EMPLOYEE
        ));

        // DEPARTMENT_MANAGER can act as team leader and employee
        ROLE_HIERARCHY.put(User.Role.DEPARTMENT_MANAGER, EnumSet.of(
            User.Role.DEPARTMENT_MANAGER,
            User.Role.TEAM_LEADER,
            User.Role.EMPLOYEE
        ));

        // TEAM_LEADER can act as employee
        ROLE_HIERARCHY.put(User.Role.TEAM_LEADER, EnumSet.of(
            User.Role.TEAM_LEADER,
            User.Role.EMPLOYEE
        ));

        // EMPLOYEE can only act as employee
        ROLE_HIERARCHY.put(User.Role.EMPLOYEE, EnumSet.of(
            User.Role.EMPLOYEE
        ));
    }

    /**
     * Initialize role permissions
     */
    private static void initializePermissions() {
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
            Permission.RESET_PASSWORDS
        ));

        // HR_MANAGER permissions - user and department management
        ROLE_PERMISSIONS.put(User.Role.HR_MANAGER, EnumSet.of(
            Permission.MANAGE_USERS,
            Permission.MANAGE_DEPARTMENTS,
            Permission.VIEW_ALL_REPORTS,
            Permission.VIEW_ANALYTICS,
            Permission.RESET_PASSWORDS
        ));

        // DEPARTMENT_MANAGER permissions - department-level management
        ROLE_PERMISSIONS.put(User.Role.DEPARTMENT_MANAGER, EnumSet.of(
            Permission.VIEW_DEPARTMENT_REPORTS,
            Permission.APPROVE_REPORTS,
            Permission.VIEW_DEPARTMENT_ANALYTICS,
            Permission.MANAGE_DEPARTMENT_USERS
        ));

        // TEAM_LEADER permissions - team-level management
        ROLE_PERMISSIONS.put(User.Role.TEAM_LEADER, EnumSet.of(
            Permission.VIEW_TEAM_REPORTS,
            Permission.APPROVE_REPORTS,
            Permission.VIEW_TEAM_ANALYTICS
        ));

        // EMPLOYEE permissions - basic user permissions
        ROLE_PERMISSIONS.put(User.Role.EMPLOYEE, EnumSet.of(
            Permission.CREATE_REPORTS,
            Permission.VIEW_OWN_REPORTS,
            Permission.EDIT_OWN_REPORTS,
            Permission.VIEW_PROFILE,
            Permission.EDIT_PROFILE
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

        // Admin can manage anyone
        if (manager.getRole() == User.Role.ADMIN) {
            return true;
        }

        // HR can manage anyone except admin
        if (manager.getRole() == User.Role.HR_MANAGER) {
            return targetUser.getRole() != User.Role.ADMIN;
        }

        // Department manager can manage users in their department (except admin/HR)
        if (manager.getRole() == User.Role.DEPARTMENT_MANAGER) {
            return targetUser.getRole() != User.Role.ADMIN && 
                   targetUser.getRole() != User.Role.HR_MANAGER &&
                   isSameDepartment(manager, targetUser);
        }

        // Team leader can manage employees in their department
        if (manager.getRole() == User.Role.TEAM_LEADER) {
            return targetUser.getRole() == User.Role.EMPLOYEE &&
                   isSameDepartment(manager, targetUser);
        }

        return false;
    }

    /**
     * Check if user can view reports from a department
     */
    public static boolean canViewDepartmentReports(User user, Long departmentId) {
        if (user == null || user.getRole() == null) {
            return false;
        }

        // Admin and HR can view all departments
        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.HR_MANAGER) {
            return true;
        }

        // Department managers and team leaders can view their department
        if (user.getRole() == User.Role.DEPARTMENT_MANAGER || 
            user.getRole() == User.Role.TEAM_LEADER) {
            return user.getDepartment() != null && 
                   user.getDepartment().getId().equals(departmentId);
        }

        return false;
    }

    /**
     * Get the role level (lower number = higher authority)
     */
    public static int getRoleLevel(User.Role role) {
        return switch (role) {
            case ADMIN -> 1;
            case HR_MANAGER -> 2;
            case DEPARTMENT_MANAGER -> 3;
            case TEAM_LEADER -> 4;
            case EMPLOYEE -> 5;
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
        if (user1.getDepartment() == null || user2.getDepartment() == null) {
            return false;
        }
        return user1.getDepartment().getId().equals(user2.getDepartment().getId());
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
        VIEW_TEAM_REPORTS,
        VIEW_DEPARTMENT_REPORTS,
        VIEW_ALL_REPORTS,
        APPROVE_REPORTS,

        // Template management
        MANAGE_TEMPLATES,

        // Analytics and statistics
        VIEW_ANALYTICS,
        VIEW_TEAM_ANALYTICS,
        VIEW_DEPARTMENT_ANALYTICS,

        // User management
        MANAGE_DEPARTMENT_USERS,
        VIEW_PROFILE,
        EDIT_PROFILE
    }
}