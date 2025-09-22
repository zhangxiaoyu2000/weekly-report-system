package com.weeklyreport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Project entity - 严格按照数据库设计.md第82-115行和error3.md要求重构
 * 
 * 字段映射 (数据库设计.md 第88-102行)：
 * - project_name → name
 * - project_content → description  
 * - project_members → members
 * 
 * 删除的字段包括：actual_results, status, ai_analysis_result, 复杂审批字段等
 * 保留核心审批字段：ai_analysis_id, admin_reviewer_id, super_admin_reviewer_id, rejection_reason, approval_status
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_name", columnList = "name"),
    @Index(name = "idx_project_approval_status", columnList = "approval_status"),
    @Index(name = "idx_project_created_by", columnList = "created_by")
})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                // #项目ID (error3.md要求)

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;                            // #项目名称 (原project_name字段)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;                     // #项目内容 (原project_content字段)

    @Column(name = "members", columnDefinition = "TEXT")
    private String members;                         // #项目成员 (原project_members字段)

    @Column(name = "expected_results", columnDefinition = "TEXT")
    private String expectedResults;                 // #预期结果

    @Column(name = "timeline", columnDefinition = "TEXT")
    private String timeline;                        // #时间线

    @Column(name = "stop_loss", columnDefinition = "TEXT")
    private String stopLoss;                        // #止损点（可选）

    @NotNull(message = "Project creator cannot be null")
    @Column(name = "created_by", nullable = false)
    private Long createdBy;                         // 创建者ID

    // 审批流程字段 (按数据库设计.md要求)
    @Column(name = "ai_analysis_id")
    private Long aiAnalysisId;                      // AI分析结果ID（外键）

    @Column(name = "admin_reviewer_id")
    private Long adminReviewerId;                   // 管理员审批人ID

    @Column(name = "super_admin_reviewer_id")
    private Long superAdminReviewerId;              // 超级管理员审批人ID

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;                 // 拒绝理由

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 30, nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.AI_ANALYZING; // 审批状态

    // 时间戳字段
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 简化版本：不使用JPA关联关系，通过Repository查询获取ProjectPhase
    // 相关ProjectPhase可以通过ProjectPhaseRepository.findByProjectId(Long projectId)获取

    // 审批状态枚举 (完整的三级审批流程 - 按数据库设计.md第108-121行)
    public enum ApprovalStatus {
        AI_ANALYZING,            // AI分析中
        AI_APPROVED,             // AI分析通过
        AI_REJECTED,             // AI分析拒绝
        ADMIN_REVIEWING,         // 管理员审核中
        ADMIN_APPROVED,          // 管理员审核通过
        ADMIN_REJECTED,          // 管理员审核拒绝
        SUPER_ADMIN_REVIEWING,   // 超级管理员审核中
        SUPER_ADMIN_APPROVED,    // 超级管理员审核通过
        SUPER_ADMIN_REJECTED,    // 超级管理员审核拒绝
        FINAL_APPROVED           // 最终批准
    }

    // 项目状态枚举 - 兼容性
    public enum ProjectStatus {
        ACTIVE,                 // 进行中
        COMPLETED,              // 已完成
        CANCELLED,              // 已取消
        ON_HOLD                 // 暂停
    }

    // 项目优先级枚举 - 兼容性
    public enum ProjectPriority {
        LOW,                    // 低优先级
        MEDIUM,                 // 中优先级
        HIGH,                   // 高优先级
        URGENT                  // 紧急
    }

    // Constructors
    public Project() {}

    public Project(String name, String description, Long createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.approvalStatus = ApprovalStatus.AI_ANALYZING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(String expectedResults) {
        this.expectedResults = expectedResults;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getAiAnalysisId() {
        return aiAnalysisId;
    }

    public void setAiAnalysisId(Long aiAnalysisId) {
        this.aiAnalysisId = aiAnalysisId;
    }

    public Long getAdminReviewerId() {
        return adminReviewerId;
    }

    public void setAdminReviewerId(Long adminReviewerId) {
        this.adminReviewerId = adminReviewerId;
    }

    public Long getSuperAdminReviewerId() {
        return superAdminReviewerId;
    }

    public void setSuperAdminReviewerId(Long superAdminReviewerId) {
        this.superAdminReviewerId = superAdminReviewerId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 简化版本：项目阶段管理通过Repository操作
    // 使用 ProjectPhaseRepository.save(projectPhase) 创建阶段
    // 使用 ProjectPhaseRepository.findByProjectId(projectId) 查询阶段

    // Business logic methods
    public void submit() {
        this.approvalStatus = ApprovalStatus.AI_ANALYZING;
    }

    public void aiApprove() {
        this.approvalStatus = ApprovalStatus.AI_APPROVED;
    }

    public void adminApprove(Long adminId) {
        this.adminReviewerId = adminId;
        // 管理员审批通过后，应该进入超级管理员审核阶段
        this.approvalStatus = ApprovalStatus.SUPER_ADMIN_REVIEWING;
    }

    public void superAdminApprove(Long superAdminId) {
        this.superAdminReviewerId = superAdminId;
        this.approvalStatus = ApprovalStatus.SUPER_ADMIN_APPROVED;
    }

    public void reject(Long reviewerId, String reason) {
        if (this.approvalStatus == ApprovalStatus.AI_APPROVED) {
            this.adminReviewerId = reviewerId;
            this.approvalStatus = ApprovalStatus.ADMIN_REJECTED;
        } else {
            this.superAdminReviewerId = reviewerId;
            this.approvalStatus = ApprovalStatus.SUPER_ADMIN_REJECTED;
        }
        this.rejectionReason = reason;
    }

    /**
     * 管理员拒绝项目
     */
    public void adminReject(Long adminId, String reason) {
        this.adminReviewerId = adminId;
        this.approvalStatus = ApprovalStatus.ADMIN_REJECTED;
        this.rejectionReason = reason;
    }

    /**
     * 超级管理员拒绝项目
     */
    public void superAdminReject(Long superAdminId, String reason) {
        this.superAdminReviewerId = superAdminId;
        this.approvalStatus = ApprovalStatus.SUPER_ADMIN_REJECTED;
        this.rejectionReason = reason;
    }

    public boolean isDraft() {
        return approvalStatus == ApprovalStatus.AI_ANALYZING;
    }

    public boolean isSubmitted() {
        return approvalStatus == ApprovalStatus.AI_ANALYZING;
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.SUPER_ADMIN_APPROVED || 
               approvalStatus == ApprovalStatus.FINAL_APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus == ApprovalStatus.ADMIN_REJECTED || 
               approvalStatus == ApprovalStatus.SUPER_ADMIN_REJECTED ||
               approvalStatus == ApprovalStatus.AI_REJECTED;
    }

    // 简化版本中不支持项目阶段管理

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // 业务方法 - 直接支持DTO所需字段
    public ProjectStatus getStatus() {
        // 使用approval_status映射到ProjectStatus
        switch (this.approvalStatus) {
            case AI_ANALYZING: case AI_APPROVED: case ADMIN_REVIEWING: case SUPER_ADMIN_REVIEWING: return ProjectStatus.ACTIVE;
            case SUPER_ADMIN_APPROVED: case FINAL_APPROVED: return ProjectStatus.COMPLETED;
            case AI_REJECTED: case ADMIN_REJECTED: case SUPER_ADMIN_REJECTED: return ProjectStatus.ON_HOLD;
            default: return ProjectStatus.ACTIVE;
        }
    }
    
    public void setStatus(ProjectStatus status) {
        // ProjectStatus is calculated from approvalStatus, so this is a no-op
        // Included for compatibility
    }

    public ProjectPriority getPriority() {
        return ProjectPriority.MEDIUM; // 简化版本默认中等优先级
    }

    public java.time.LocalDate getStartDate() {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }

    public java.time.LocalDate getEndDate() {
        return null; // 简化版本中不支持
    }

    public java.math.BigDecimal getBudget() {
        return null; // 简化版本中不支持
    }

    public Integer getProgress() {
        // 基于审批状态计算进度
        switch (this.approvalStatus) {
            case AI_ANALYZING: return 15;
            case AI_APPROVED: return 40;
            case ADMIN_REVIEWING: return 60;
            case SUPER_ADMIN_REVIEWING: return 80;
            case SUPER_ADMIN_APPROVED: return 95;
            case FINAL_APPROVED: return 100;
            case AI_REJECTED: case ADMIN_REJECTED: case SUPER_ADMIN_REJECTED: return 0;
            default: return 0;
        }
    }
    
    public void setProgress(int progress) {
        // Progress is calculated from approvalStatus, so this is a no-op
        // Included for compatibility
    }

    public String getTags() {
        return null; // 简化版本中不支持
    }

    public Boolean getIsPublic() {
        return false; // 简化版本默认私有
    }

    public Boolean getArchived() {
        return false; // 简化版本默认未归档
    }

    public Object getDepartment() {
        return null; // 简化版本中不支持部门
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", approvalStatus=" + approvalStatus +
                ", createdBy=" + createdBy +
                '}';
    }
}