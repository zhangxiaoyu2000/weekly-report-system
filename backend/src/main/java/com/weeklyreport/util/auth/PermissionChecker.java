package com.weeklyreport.util.auth;

import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.User;
import com.weeklyreport.security.CustomUserPrincipal;
import org.springframework.stereotype.Component;

/**
 * 权限检查工具类
 */
@Component
public class PermissionChecker {

    /**
     * 检查用户是否可以创建项目
     * 只有主管、管理员和超级管理员可以创建项目
     */
    public boolean canCreateProject(CustomUserPrincipal userPrincipal) {
        String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER");
    }

    /**
     * 检查用户是否可以查看项目详情
     * 项目创建者、管理员、超级管理员可以查看
     */
    public boolean canViewProject(SimpleProject project, CustomUserPrincipal userPrincipal) {
        // 项目创建者可以查看
        if (project.getCreatedBy().getId().equals(userPrincipal.getId())) {
            return true;
        }
        
        // 管理员和超级管理员可以查看所有项目
        String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_ADMIN");
    }

    /**
     * 检查用户是否可以审批项目
     * 管理员和超级管理员都可以审批项目
     */
    public boolean canApproveProject(CustomUserPrincipal userPrincipal) {
        String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_ADMIN");
    }

    /**
     * 检查用户是否可以创建周报
     * 项目创建者且项目已审批通过才能创建周报
     */
    public boolean canCreateWeeklyReport(SimpleProject project, CustomUserPrincipal userPrincipal) {
        // 必须是项目创建者
        if (!project.getCreatedBy().getId().equals(userPrincipal.getId())) {
            return false;
        }
        
        // 项目必须已审批通过
        return project.getStatus() == SimpleProject.ProjectStatus.APPROVED;
    }

    /**
     * 检查用户是否可以查看项目周报
     * 项目创建者、老板、HR可以查看
     */
    public boolean canViewProjectReports(SimpleProject project, CustomUserPrincipal userPrincipal) {
        return canViewProject(project, userPrincipal);
    }

    /**
     * 检查用户是否可以查看所有周报
     * 管理员和超级管理员可以查看所有周报
     */
    public boolean canViewAllReports(CustomUserPrincipal userPrincipal) {
        String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_ADMIN");
    }

    /**
     * 检查用户是否可以查看所有项目
     * 管理员和超级管理员可以查看所有项目
     */
    public boolean canViewAllProjects(CustomUserPrincipal userPrincipal) {
        String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_SUPER_ADMIN") || role.equals("ROLE_ADMIN");
    }

    /**
     * 获取用户角色的中文名称
     */
    public String getRoleDisplayName(User.Role role) {
        switch (role) {
            case SUPER_ADMIN:
                return "超级管理员";
            case ADMIN:
                return "管理员";
            case MANAGER:
                return "主管";
            default:
                return role.name();
        }
    }

    /**
     * 检查用户角色级别
     * 返回角色级别，数字越小级别越高
     */
    public int getRoleLevel(User.Role role) {
        switch (role) {
            case SUPER_ADMIN:
                return 0; // 最高级别
            case ADMIN:
                return 1; // 次高级别
            case MANAGER:
                return 2; // 第三级别
            default:
                return 999;
        }
    }
}