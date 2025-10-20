package com.weeklyreport.notification.test;

import com.weeklyreport.notification.service.NotificationService;
import com.weeklyreport.notification.event.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

/**
 * 通知测试控制器 - 用于测试邮件通知功能
 */
@RestController
@RequestMapping("/test/notification")
public class NotificationTestController {

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    public NotificationTestController(ApplicationEventPublisher eventPublisher, 
                                    NotificationService notificationService) {
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    /**
     * 测试AI分析完成通知
     */
    @PostMapping("/ai-analysis-completed/{projectId}")
    public String testAIAnalysisCompleted(@PathVariable Long projectId) {
        try {
            AIAnalysisCompletedEvent event = new AIAnalysisCompletedEvent(
                projectId, 
                "测试项目", 
                1L, 
                "AI分析已完成"
            );
            eventPublisher.publishEvent(event);
            return "AI分析完成通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 测试待管理员审核通知
     */
    @PostMapping("/pending-admin-review/{projectId}")
    public String testPendingAdminReview(@PathVariable Long projectId) {
        try {
            PendingAdminReviewEvent event = new PendingAdminReviewEvent(
                projectId, 
                "测试项目", 
                1L
            );
            eventPublisher.publishEvent(event);
            return "待管理员审核通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 测试管理员拒绝通知
     */
    @PostMapping("/admin-rejected/{projectId}")
    public String testAdminRejected(@PathVariable Long projectId) {
        try {
            AdminRejectedEvent event = new AdminRejectedEvent(
                projectId, 
                "测试项目", 
                2L, 
                "项目方案需要进一步完善"
            );
            eventPublisher.publishEvent(event);
            return "管理员拒绝通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 测试管理员通过通知
     */
    @PostMapping("/admin-approved/{projectId}")
    public String testAdminApproved(@PathVariable Long projectId) {
        try {
            AdminApprovedEvent event = new AdminApprovedEvent(
                projectId, 
                "测试项目", 
                2L
            );
            eventPublisher.publishEvent(event);
            return "管理员通过通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 测试超级管理员拒绝通知
     */
    @PostMapping("/super-admin-rejected/{projectId}")
    public String testSuperAdminRejected(@PathVariable Long projectId) {
        try {
            SuperAdminRejectedEvent event = new SuperAdminRejectedEvent(
                projectId, 
                "测试项目", 
                3L, 
                "项目预算超出限制"
            );
            eventPublisher.publishEvent(event);
            return "超级管理员拒绝通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 测试超级管理员通过通知
     */
    @PostMapping("/super-admin-approved/{projectId}")
    public String testSuperAdminApproved(@PathVariable Long projectId) {
        try {
            SuperAdminApprovedEvent event = new SuperAdminApprovedEvent(
                projectId, 
                "测试项目", 
                3L
            );
            eventPublisher.publishEvent(event);
            return "超级管理员通过通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 测试强制提交通知
     */
    @PostMapping("/force-submitted/{projectId}")
    public String testForceSubmitted(@PathVariable Long projectId) {
        try {
            ForceSubmittedEvent event = new ForceSubmittedEvent(
                projectId, 
                "测试项目", 
                2L, 
                "紧急项目需要快速启动"
            );
            eventPublisher.publishEvent(event);
            return "强制提交通知已发送";
        } catch (Exception e) {
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 获取通知系统状态
     */
    @GetMapping("/status")
    public String getStatus() {
        return "通知系统运行正常";
    }
}