package com.weeklyreport.project.repository.projection;

import com.weeklyreport.project.entity.Project;
import java.time.LocalDateTime;

/**
 * 项目信息包含创建者用户名的投影接口
 * 用于通过JOIN查询一次性获取项目和创建者信息
 */
public interface ProjectWithCreatorProjection {
    
    // 项目基本信息
    Long getId();
    String getName();
    String getDescription();
    String getMembers();
    String getExpectedResults();
    String getTimeline();
    String getStopLoss();
    Long getCreatedBy();
    
    // 审批流程字段
    Long getAiAnalysisId();
    Long getAdminReviewerId();
    Long getSuperAdminReviewerId();
    String getRejectionReason();
    Project.ApprovalStatus getApprovalStatus();
    
    // 时间戳字段
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    
    // 创建者用户名（来自JOIN查询）
    String getCreatedByUsername();
}