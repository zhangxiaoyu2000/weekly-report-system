package com.weeklyreport.notification.service;

import com.weeklyreport.notification.dto.NotificationRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 邮件模板服务 - 生成不同类型通知的邮件主题和内容
 */
@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成邮件主题
     */
    public String generateSubject(NotificationRequest request) {
        String projectName = request.getProjectName() != null ? request.getProjectName() : "项目#" + request.getProjectId();
        
        switch (request.getNotificationType()) {
            case AI_ANALYSIS_COMPLETED:
                return String.format("【项目AI分析完成】%s - 请查看分析结果", projectName);
                
            case PENDING_ADMIN_REVIEW:
                return String.format("【待审核】项目 %s 需要管理员审核", projectName);
                
            case ADMIN_REJECTED:
                return String.format("【审核被拒】项目 %s 已被管理员拒绝", projectName);
                
            case ADMIN_APPROVED:
                return String.format("【管理员通过】项目 %s 已通过管理员审核", projectName);
                
            case SUPER_ADMIN_REJECTED:
                return String.format("【最终审核被拒】项目 %s 已被超级管理员拒绝", projectName);
                
            case SUPER_ADMIN_APPROVED:
                return String.format("【项目通过】项目 %s 已通过所有审核", projectName);
                
            case FORCE_SUBMITTED:
                return String.format("【强制提交】项目 %s 已被强制提交", projectName);
                
            // 周报相关邮件主题
            case WEEKLY_REPORT_SUBMITTED:
                return String.format("【周报提交成功】%s 的周报已提交审核",
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工");

            case WEEKLY_REPORT_AI_COMPLETED:
                return String.format("【周报AI分析完成】%s - %s 需要审核",
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工",
                    request.getReportWeek());

            case WEEKLY_REPORT_AI_REJECTED:
                return String.format("【周报需要修改】%s 的周报AI分析置信度不足",
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工");

            case WEEKLY_REPORT_SUPERVISOR_FORCE_SUBMITTED:
                return String.format("【主管强制提交周报】%s 的周报已被强制提交",
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工");
                
            case WEEKLY_REPORT_PENDING_ADMIN_REVIEW:
                return String.format("【周报待审核】%s 的周报需要管理员审核", 
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工");
                
            case WEEKLY_REPORT_ADMIN_REJECTED:
                return String.format("【周报被拒绝】%s 的周报被管理员拒绝", 
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工");
                
            case WEEKLY_REPORT_ADMIN_APPROVED:
                return String.format("【周报已通过】%s 的周报已通过管理员审核", 
                    request.getReportAuthorName() != null ? request.getReportAuthorName() : "员工");
                
            default:
                return String.format("【系统通知】项目 %s 状态更新", projectName);
        }
    }

    /**
     * 生成HTML邮件内容
     */
    public String generateHtmlContent(NotificationRequest request) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang='zh-CN'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>项目审核通知</title>");
        html.append("<style>");
        html.append(getEmailStyles());
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // 邮件头部
        html.append(generateHeader(request));

        // 邮件内容
        html.append(generateContent(request));

        // 邮件脚部
        html.append(generateFooter(request));

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String getEmailStyles() {
        return """
            body { font-family: 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #212529; margin: 0; padding: 24px; background-color: #f5f7fa; }
            .container { max-width: 640px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e9ecef; box-shadow: 0 6px 24px rgba(15, 23, 42, 0.08); overflow: hidden; }
            .header { padding: 24px; border-bottom: 1px solid #e9ecef; text-align: left; background: #ffffff; }
            .header h1 { margin: 0; font-size: 20px; font-weight: 600; color: #212529; }
            .header p { margin: 8px 0 0; font-size: 14px; color: #6c757d; }
            .content { padding: 24px; }
            .project-info { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; padding: 16px; margin: 16px 0 20px; }
            .project-info h3 { margin: 0 0 12px; font-size: 15px; color: #212529; }
            .project-info p { margin: 6px 0; color: #495057; }
            .project-info strong { display: inline-block; min-width: 70px; }
            .status-badge { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
            .status-success { background: #d1f2d9; color: #1e7e34; }
            .status-danger { background: #fde2e1; color: #c12d2c; }
            .status-warning { background: #fff2cd; color: #9a6b16; }
            .status-info { background: #d8ecff; color: #0a58ca; }
            .action-button { display: inline-block; margin-top: 12px; padding: 10px 18px; background: #0d6efd; color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 14px; }
            .action-button:hover { background: #0b5ed7; }
            .timestamp { color: #6c757d; font-size: 12px; margin-top: 24px; }
            .footer { padding: 16px 24px 24px; border-top: 1px solid #e9ecef; background: #fafafa; text-align: center; font-size: 12px; color: #6c757d; }
            """;
    }

    private String generateHeader(NotificationRequest request) {
        return String.format("""
            <div class="header">
                <h1>周报管理系统通知</h1>
                <p>%s</p>
            </div>
            """, request.getNotificationType().getDescription());
    }

    private String generateContent(NotificationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("<div class='content'>");

        // 根据通知类型决定是否显示项目信息
        boolean isWeeklyReportNotification = request.getNotificationType() == NotificationRequest.NotificationType.WEEKLY_REPORT_SUBMITTED
            || request.getNotificationType() == NotificationRequest.NotificationType.WEEKLY_REPORT_AI_COMPLETED
            || request.getNotificationType() == NotificationRequest.NotificationType.WEEKLY_REPORT_PENDING_ADMIN_REVIEW
            || request.getNotificationType() == NotificationRequest.NotificationType.WEEKLY_REPORT_ADMIN_REJECTED
            || request.getNotificationType() == NotificationRequest.NotificationType.WEEKLY_REPORT_ADMIN_APPROVED;

        // 仅项目通知显示项目信息，周报通知在具体内容中显示周报信息
        if (!isWeeklyReportNotification) {
            content.append(generateProjectInfo(request));
        }

        // 根据通知类型生成具体内容
        content.append(generateTypeSpecificContent(request));

        // 操作按钮
        content.append(generateActionButtons(request));

        // 时间戳
        content.append(generateTimestamp(request));

        content.append("</div>");
        return content.toString();
    }

    private String generateProjectInfo(NotificationRequest request) {
        String projectName = request.getProjectName() != null ? request.getProjectName() : "项目#" + request.getProjectId();
        String ownerName = safeName(request.getProjectOwnerName(), null);
        String reviewerName = safeName(request.getReviewerName(), null);

        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"project-info\">");
        builder.append("<h3>📁 项目信息</h3>");
        builder.append(String.format("<p><strong>项目名称：</strong>%s</p>", projectName));
        builder.append(String.format("<p><strong>项目ID：</strong>%s</p>", request.getProjectId()));
        builder.append(String.format("<p><strong>状态：</strong>%s</p>", generateStatusBadge(request.getNotificationType())));
        if (ownerName != null) {
            builder.append(String.format("<p><strong>项目提交者：</strong>%s</p>", ownerName));
        }
        if (reviewerName != null) {
            builder.append(String.format("<p><strong>最近审核人：</strong>%s</p>", reviewerName));
        }
        builder.append("</div>");
        return builder.toString();
    }

    private String generateTypeSpecificContent(NotificationRequest request) {
        switch (request.getNotificationType()) {
            case AI_ANALYSIS_COMPLETED:
                String analysisOwner = safeName(request.getProjectOwnerName(), "项目负责人");
                return String.format("""
                    <p>AI分析已完成，%s 的项目最新分析报告已经生成。</p>
                    <p>请登录系统查看分析摘要并根据建议同步调整项目计划。</p>
                    """, analysisOwner);
                    
            case PENDING_ADMIN_REVIEW:
                String submitterName = safeName(request.getProjectOwnerName(), "项目提交者");
                return String.format("""
                    <p>来自 %s 的项目已提交管理员审批。</p>
                    <p>请登录系统完成审核并给出处理结果。</p>
                    """, submitterName);
                    
            case ADMIN_REJECTED:
                String adminReason = request.getRejectionReason() != null ? request.getRejectionReason() : "未提供具体原因";
                String adminName = safeName(request.getReviewerName(), "管理员");
                return String.format("""
                    <p>❌ 很抱歉，管理员 %s 未能通过您的项目申请。</p>
                    <div class="project-info">
                        <h3>拒绝原因</h3>
                        <p>%s</p>
                    </div>
                    <p>请根据反馈意见修改方案后重新提交。</p>
                    """, adminName, adminReason);
                    
            case ADMIN_APPROVED:
                String approvedAdmin = safeName(request.getReviewerName(), "管理员");
                String ownerDisplay = safeName(request.getProjectOwnerName(), "项目提交者");
                if (request.getRecipientType() == NotificationRequest.RecipientType.ALL_SUPER_ADMINS) {
                    return String.format("""
                        <p>📝 %s 的项目已经由管理员 %s 审核通过。</p>
                        <p>请尽快登录系统完成最终立项审批。</p>
                        """, ownerDisplay, approvedAdmin);
                }
                if (request.getRecipientType() == NotificationRequest.RecipientType.PROJECT_MANAGER) {
                    return String.format("""
                        <p>✅ 管理员 %s 已审批通过您的项目，现已进入超级管理员终审阶段。</p>
                        <p>如需补充资料，请提前准备并关注后续审核进度。</p>
                        """, approvedAdmin);
                }
                return "<p>管理员已完成审核，项目进入超级管理员终审流程。</p>";
                    
            case SUPER_ADMIN_REJECTED:
                String superAdminReason = request.getRejectionReason() != null ? request.getRejectionReason() : "未提供具体原因";
                String superAdminName = safeName(request.getReviewerName(), "超级管理员");
                return String.format("""
                    <p>❌ 很抱歉，超级管理员 %s 未通过您的项目最终审核。</p>
                    <div class="project-info">
                        <h3>拒绝原因</h3>
                        <p>%s</p>
                    </div>
                    <p>请根据反馈意见重新评估项目方案后再行提交。</p>
                    """, superAdminName, superAdminReason);
                    
            case SUPER_ADMIN_APPROVED:
                String finalApprover = safeName(request.getReviewerName(), "超级管理员");
                String finalOwner = safeName(request.getProjectOwnerName(), "项目提交者");
                if (request.getRecipientType() == NotificationRequest.RecipientType.PROJECT_MANAGER) {
                    return String.format("""
                        <p>🎉 恭喜！超级管理员 %s 已最终批准您的项目，项目正式立项。</p>
                        <p>请安排项目启动会议，并按照计划提交后续周报。</p>
                        """, finalApprover);
                }
                if (request.getRecipientType() == NotificationRequest.RecipientType.ADMINS_AND_SUPER_ADMINS) {
                    return String.format("""
                        <p>🎉 %s 的项目已由超级管理员 %s 审核通过，现已正式立项。</p>
                        <p>请在系统内确认相关配置并关注项目执行进度。</p>
                        """, finalOwner, finalApprover);
                }
                return "<p>项目已完成全部审批流程，现已进入正式立项阶段。</p>";
                    
            case FORCE_SUBMITTED:
                String forceOwner = safeName(request.getProjectOwnerName(), "项目提交者");
                String forceActor = safeName(request.getTriggerUserName(), forceOwner);
                return String.format("""
                    <p>⚡ %s 的项目已由 %s 发起AI强制提交。</p>
                    <p>请尽快登录系统完成审核并确认后续处理。</p>
                    """, forceOwner, forceActor);
                    
            // 周报相关通知内容
            case WEEKLY_REPORT_SUBMITTED:
                if (request.getRecipientType() == NotificationRequest.RecipientType.WEEKLY_REPORT_AUTHOR) {
                    return String.format("""
                        <p>✅ 您的周报已成功提交，现已进入审核流程。</p>
                        <div class="project-info">
                            <h3>📋 周报信息</h3>
                            <p><strong>周报标题：</strong>%s</p>
                            <p><strong>报告周期：</strong>%s</p>
                            <p><strong>提交时间：</strong>刚刚</p>
                        </div>
                        <p>系统正在对您的周报进行AI分析，分析完成后将通知您的主管进行审核。</p>
                        <p>您可以在系统中查看周报状态，审核结果将通过邮件通知您。</p>
                        """,
                        request.getWeeklyReportTitle(),
                        request.getReportWeek());
                }
                if (request.getRecipientType() == NotificationRequest.RecipientType.WEEKLY_REPORT_SUPERVISOR) {
                    return String.format("""
                        <p>📋 %s 提交了新的周报，AI分析正在进行中。</p>
                        <div class="project-info">
                            <h3>📋 周报信息</h3>
                            <p><strong>周报标题：</strong>%s</p>
                            <p><strong>报告周期：</strong>%s</p>
                            <p><strong>提交人：</strong>%s</p>
                        </div>
                        <p>系统正在进行AI分析，分析完成后您将收到审核通知。</p>
                        <p>请关注后续邮件通知，及时进行周报审核。</p>
                        """,
                        request.getReportAuthorName(),
                        request.getWeeklyReportTitle(),
                        request.getReportWeek(),
                        request.getReportAuthorName());
                }
                return "<p>周报已提交成功，AI分析正在进行中。</p>";

            case WEEKLY_REPORT_AI_COMPLETED:
                return String.format("""
                    <p>📊 %s 的周报AI分析已完成，请及时进行审核。</p>
                    <div class="project-info">
                        <h3>📋 周报信息</h3>
                        <p><strong>周报标题：</strong>%s</p>
                        <p><strong>报告周期：</strong>%s</p>
                        <p><strong>提交人：</strong>%s</p>
                    </div>
                    <p>请登录系统查看AI分析结果并进行下一步审核操作。</p>
                    """,
                    request.getReportAuthorName(),
                    request.getWeeklyReportTitle(),
                    request.getReportWeek(),
                    request.getReportAuthorName());

            case WEEKLY_REPORT_AI_REJECTED:
                String aiRejectionReason = request.getRejectionReason() != null ? request.getRejectionReason() : "AI分析置信度未达到要求";
                return String.format("""
                    <p>❌ 您的周报AI分析置信度不足，需要修改后重新提交。</p>
                    <div class="project-info">
                        <h3>📋 周报信息</h3>
                        <p><strong>周报标题：</strong>%s</p>
                        <p><strong>报告周期：</strong>%s</p>
                    </div>
                    <div class="project-info">
                        <h3>AI分析结果</h3>
                        <p>%s</p>
                    </div>
                    <p>请根据AI分析建议完善周报内容后重新提交。</p>
                    """,
                    request.getWeeklyReportTitle(),
                    request.getReportWeek(),
                    aiRejectionReason);

            case WEEKLY_REPORT_SUPERVISOR_FORCE_SUBMITTED:
                return String.format("""
                    <p>⚡ 主管已强制提交 %s 的周报，请及时处理。</p>
                    <div class="project-info">
                        <h3>📋 周报信息</h3>
                        <p><strong>周报标题：</strong>%s</p>
                        <p><strong>报告周期：</strong>%s</p>
                        <p><strong>提交人：</strong>%s</p>
                        <p><strong>操作人：</strong>%s</p>
                    </div>
                    <p>周报已跳过正常流程直接进入审核，请尽快处理。</p>
                    """,
                    request.getReportAuthorName(),
                    request.getWeeklyReportTitle(),
                    request.getReportWeek(),
                    request.getReportAuthorName(),
                    request.getTriggerUserName() != null ? request.getTriggerUserName() : "主管");
                    
            case WEEKLY_REPORT_PENDING_ADMIN_REVIEW:
                return String.format("""
                    <p>📋 有新的周报需要管理员审核，请及时处理。</p>
                    <div class="project-info">
                        <h3>📋 周报信息</h3>
                        <p><strong>周报标题：</strong>%s</p>
                        <p><strong>报告周期：</strong>%s</p>
                        <p><strong>提交人：</strong>%s</p>
                    </div>
                    <p>请登录系统查看周报详情并完成审核。</p>
                    """, 
                    request.getWeeklyReportTitle(),
                    request.getReportWeek(),
                    request.getReportAuthorName());
                    
            case WEEKLY_REPORT_ADMIN_REJECTED:
                String weeklyRejectReason = request.getRejectionReason() != null ? request.getRejectionReason() : "未提供具体原因";
                return String.format("""
                    <p>❌ %s 的周报已被管理员拒绝，请及时处理。</p>
                    <div class="project-info">
                        <h3>📋 周报信息</h3>
                        <p><strong>周报标题：</strong>%s</p>
                        <p><strong>报告周期：</strong>%s</p>
                        <p><strong>提交人：</strong>%s</p>
                        <p><strong>审核人：</strong>%s</p>
                    </div>
                    <div class="project-info">
                        <h3>拒绝原因</h3>
                        <p>%s</p>
                    </div>
                    <p>请指导员工根据反馈意见修改周报并重新提交。</p>
                    """, 
                    request.getReportAuthorName(),
                    request.getWeeklyReportTitle(),
                    request.getReportWeek(),
                    request.getReportAuthorName(),
                    request.getReviewerName(),
                    weeklyRejectReason);
                    
            case WEEKLY_REPORT_ADMIN_APPROVED:
                if (request.getRecipientType() == NotificationRequest.RecipientType.ALL_SUPER_ADMINS) {
                    return String.format("""
                        <p>✅ %s 的周报已通过管理员审核，请查阅。</p>
                        <div class="project-info">
                            <h3>📋 周报信息</h3>
                            <p><strong>周报标题：</strong>%s</p>
                            <p><strong>报告周期：</strong>%s</p>
                            <p><strong>提交人：</strong>%s</p>
                            <p><strong>审核人：</strong>%s</p>
                        </div>
                        <p>周报已通过 %s 的管理员审核，请及时查阅周报内容。</p>
                        """, 
                        request.getReportAuthorName(),
                        request.getWeeklyReportTitle(),
                        request.getReportWeek(),
                        request.getReportAuthorName(),
                        request.getReviewerName(),
                        request.getReviewerName());
                }
                if (request.getRecipientType() == NotificationRequest.RecipientType.WEEKLY_REPORT_AUTHOR) {
                    return String.format("""
                        <p>🎉 恭喜！您的周报已通过管理员审核。</p>
                        <div class="project-info">
                            <h3>📋 周报信息</h3>
                            <p><strong>周报标题：</strong>%s</p>
                            <p><strong>报告周期：</strong>%s</p>
                            <p><strong>审核人：</strong>%s</p>
                        </div>
                        <p>您的周报已通过 %s 的审核，请继续保持良好的工作记录。</p>
                        """, 
                        request.getWeeklyReportTitle(),
                        request.getReportWeek(),
                        request.getReviewerName(),
                        request.getReviewerName());
                }
                return "<p>周报已通过管理员审核。</p>";
                    
            default:
                return "<p>项目状态已更新，请登录系统查看详情。</p>";
        }
    }

    private String safeName(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String generateStatusBadge(NotificationRequest.NotificationType type) {
        switch (type) {
            case AI_ANALYSIS_COMPLETED:
                return "<span class='status-badge status-info'>分析完成</span>";
            case PENDING_ADMIN_REVIEW:
                return "<span class='status-badge status-warning'>待审核</span>";
            case ADMIN_REJECTED:
            case SUPER_ADMIN_REJECTED:
                return "<span class='status-badge status-danger'>已拒绝</span>";
            case ADMIN_APPROVED:
            case SUPER_ADMIN_APPROVED:
                return "<span class='status-badge status-success'>已通过</span>";
            case FORCE_SUBMITTED:
                return "<span class='status-badge status-warning'>强制提交</span>";
            default:
                return "<span class='status-badge status-info'>状态更新</span>";
        }
    }

    private String generateActionButtons(NotificationRequest request) {
        // 根据不同的通知类型生成不同的操作按钮
        switch (request.getNotificationType()) {
            case AI_ANALYSIS_COMPLETED:
            case PENDING_ADMIN_REVIEW:
            case ADMIN_APPROVED:
                return """
                    <p>
                        <a href="#" class="action-button">前往周报管理系统</a>
                    </p>
                    """;
            default:
                return """
                    <p>
                        <a href="#" class="action-button">前往周报管理系统</a>
                    </p>
                    """;
        }
    }

    private String generateTimestamp(NotificationRequest request) {
        return String.format("""
            <div class="timestamp">
                <p>📅 通知时间：%s</p>
            </div>
            """, request.getTimestamp().format(DATE_TIME_FORMATTER));
    }

    private String generateFooter(NotificationRequest request) {
        return """
            <div class="footer">
                <p>此邮件由周报管理系统自动发送，请勿直接回复。</p>
                <p>如有问题，请登录系统联系管理员。</p>
            </div>
            """;
    }
}
