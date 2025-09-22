package com.weeklyreport.repository.projection;

import com.weeklyreport.entity.Project;
import com.weeklyreport.entity.AIAnalysisResult;
import java.time.LocalDateTime;

/**
 * 项目详细信息投影接口
 * 包含项目基本信息、创建者用户名、AI分析结果等完整信息
 */
public interface ProjectDetailProjection {
    
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
    
    // 关联查询字段
    String getCreatedByUsername();  // 来自 User 表
    String getCreatedByFullName();  // 来自 User 表
    
    // 审核人信息字段
    String getAdminReviewerUsername();      // 管理员审核人用户名
    String getSuperAdminReviewerUsername(); // 超级管理员审核人用户名
    
    // AI分析结果字段（来自AI分析表）
    Long getAiResultId();
    String getAiResult();
    Double getAiConfidence();
    String getAiModelVersion();
    AIAnalysisResult.AnalysisStatus getAiStatus();
    AIAnalysisResult.AnalysisType getAiType();
    AIAnalysisResult.EntityType getAiEntityType();
    LocalDateTime getAiCreatedAt();
}