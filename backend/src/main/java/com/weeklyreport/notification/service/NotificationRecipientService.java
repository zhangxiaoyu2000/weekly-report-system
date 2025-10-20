package com.weeklyreport.notification.service;

import com.weeklyreport.notification.dto.NotificationRequest;
import com.weeklyreport.user.entity.User;
import com.weeklyreport.user.repository.UserRepository;
import com.weeklyreport.project.repository.ProjectRepository;
import com.weeklyreport.weeklyreport.repository.WeeklyReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知接收者服务 - 根据角色和项目获取邮件接收者列表
 */
@Service
public class NotificationRecipientService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRecipientService.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    public NotificationRecipientService(UserRepository userRepository, 
                                      ProjectRepository projectRepository,
                                      WeeklyReportRepository weeklyReportRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
    }

    /**
     * 根据接收者类型和项目ID获取邮件接收者列表
     */
    public List<String> getRecipients(NotificationRequest.RecipientType recipientType, Long projectId) {
        return getRecipients(recipientType, projectId, null);
    }

    /**
     * 根据接收者类型、项目ID和周报ID获取邮件接收者列表
     */
    public List<String> getRecipients(NotificationRequest.RecipientType recipientType, Long projectId, Long weeklyReportId) {
        try {
            switch (recipientType) {
                case PROJECT_MANAGER:
                    return getProjectManagerEmails(projectId);
                    
                case ALL_ADMINS:
                    return getAllAdminEmails();
                    
                case ALL_SUPER_ADMINS:
                    return getAllSuperAdminEmails();
                    
                case SUPER_ADMINS_AND_MANAGER:
                    List<String> emails = new ArrayList<>();
                    emails.addAll(getAllSuperAdminEmails());
                    emails.addAll(getProjectManagerEmails(projectId));
                    return emails.stream().distinct().collect(Collectors.toList());

                case ADMINS_AND_SUPER_ADMINS:
                    return getAdminsAndSuperAdmins();
                    
                case ALL_STAKEHOLDERS:
                    return getAllStakeholderEmails(projectId);
                    
                // 周报相关接收者类型
                case WEEKLY_REPORT_AUTHOR:
                    return getWeeklyReportAuthorEmail(weeklyReportId);
                    
                case WEEKLY_REPORT_SUPERVISOR:
                    return getWeeklyReportSupervisorEmail(weeklyReportId);
                    
                case WEEKLY_REPORT_ADMINS:
                    return getAllAdminEmails();
                    
                default:
                    logger.warn("未知的接收者类型: {}", recipientType);
                    return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("获取接收者邮件列表失败，接收者类型: {}，项目ID: {}", recipientType, projectId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取项目经理邮箱
     * 注意：这里假设项目表中有created_by字段指向项目创建者（项目经理）
     */
    private List<String> getProjectManagerEmails(Long projectId) {
        if (projectId == null) {
            logger.warn("项目ID为空，无法获取项目经理邮箱");
            return Collections.emptyList();
        }

        try {
            // 查找项目创建者作为项目经理
            return projectRepository.findById(projectId)
                .map(project -> {
                    Long createdBy = project.getCreatedBy();
                    if (createdBy != null) {
                        return userRepository.findById(createdBy)
                            .filter(User::isActive)
                            .map(user -> Collections.singletonList(user.getEmail()))
                            .orElse(Collections.emptyList());
                    }
                    return Collections.<String>emptyList();
                })
                .orElse(Collections.emptyList());
        } catch (Exception e) {
            logger.error("查找项目经理失败，项目ID: {}", projectId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有管理员邮箱
     */
    private List<String> getAllAdminEmails() {
        try {
            return userRepository.findByRoleAndStatus(User.Role.ADMIN, User.UserStatus.ACTIVE)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("查找所有管理员失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有超级管理员邮箱
     */
    private List<String> getAllSuperAdminEmails() {
        try {
            return userRepository.findByRoleAndStatus(User.Role.SUPER_ADMIN, User.UserStatus.ACTIVE)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("查找所有超级管理员失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有相关人员邮箱（项目经理 + 管理员 + 超级管理员）
     */
    private List<String> getAllStakeholderEmails(Long projectId) {
        try {
            List<String> emails = new ArrayList<>();
            
            // 添加项目经理
            emails.addAll(getProjectManagerEmails(projectId));
            
            // 添加所有管理员
            emails.addAll(getAllAdminEmails());
            
            // 添加所有超级管理员
            emails.addAll(getAllSuperAdminEmails());
            
            // 去重并返回
            return emails.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取所有相关人员邮箱失败，项目ID: {}", projectId, e);
            return Collections.emptyList();
        }
    }

    private List<String> getAdminsAndSuperAdmins() {
        try {
            List<String> emails = new ArrayList<>();
            emails.addAll(getAllAdminEmails());
            emails.addAll(getAllSuperAdminEmails());
            return emails.stream().distinct().collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("查找管理员和超级管理员失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取周报提交者邮箱
     */
    private List<String> getWeeklyReportAuthorEmail(Long weeklyReportId) {
        if (weeklyReportId == null) {
            logger.warn("周报ID为空，无法获取周报提交者邮箱");
            return Collections.emptyList();
        }

        try {
            return weeklyReportRepository.findById(weeklyReportId)
                .map(report -> {
                    Long userId = report.getUserId();
                    if (userId != null) {
                        return userRepository.findById(userId)
                            .filter(User::isActive)
                            .map(user -> Collections.singletonList(user.getEmail()))
                            .orElse(Collections.emptyList());
                    }
                    return Collections.<String>emptyList();
                })
                .orElse(Collections.emptyList());
        } catch (Exception e) {
            logger.error("查找周报提交者失败，周报ID: {}", weeklyReportId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取周报提交者的主管邮箱
     * 注意：这里假设用户表中有supervisor_id字段或者通过项目关系确定主管
     */
    private List<String> getWeeklyReportSupervisorEmail(Long weeklyReportId) {
        if (weeklyReportId == null) {
            logger.warn("周报ID为空，无法获取周报提交者主管邮箱");
            return Collections.emptyList();
        }

        try {
            return weeklyReportRepository.findById(weeklyReportId)
                .map(report -> {
                    Long userId = report.getUserId();
                    if (userId != null) {
                        return userRepository.findById(userId)
                            .filter(User::isActive)
                            .map(user -> {
                                // 方案1: 查找用户的直接主管 (如果User实体有supervisorId字段)
                                // Long supervisorId = user.getSupervisorId();
                                // if (supervisorId != null) {
                                //     return userRepository.findById(supervisorId)
                                //         .filter(User::isActive)
                                //         .map(supervisor -> Collections.singletonList(supervisor.getEmail()))
                                //         .orElse(Collections.emptyList());
                                // }
                                
                                // 方案2: 临时方案 - 发送给所有管理员 (在没有明确主管关系时)
                                logger.info("周报ID: {} 的提交者主管关系未定义，发送给所有管理员", weeklyReportId);
                                return getAllAdminEmails();
                            })
                            .orElse(Collections.emptyList());
                    }
                    return Collections.<String>emptyList();
                })
                .orElse(Collections.emptyList());
        } catch (Exception e) {
            logger.error("查找周报提交者主管失败，周报ID: {}", weeklyReportId, e);
            return Collections.emptyList();
        }
    }
}
