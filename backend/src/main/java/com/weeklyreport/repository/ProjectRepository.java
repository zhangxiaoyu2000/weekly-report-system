package com.weeklyreport.repository;

import com.weeklyreport.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Project repository - 严格按照Project.java实体设计
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    
    // 基于Project.java实体字段的基础查询方法
    
    /**
     * 根据创建者ID查询项目
     */
    List<Project> findByCreatedBy(Long createdBy);
    
    /**
     * 根据创建者ID分页查询项目
     */
    Page<Project> findByCreatedBy(Long createdBy, Pageable pageable);
    
    /**
     * 根据创建者ID排序查询项目
     */
    List<Project> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    /**
     * 根据项目名称查询（精确匹配）
     */
    Optional<Project> findByName(String name);
    
    /**
     * 检查项目名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据项目名称模糊查询
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Project> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // 基于审批状态的查询 - 严格按照Project.ApprovalStatus枚举
    
    /**
     * 根据审批状态查询项目
     */
    List<Project> findByApprovalStatus(Project.ApprovalStatus approvalStatus);
    
    /**
     * 根据审批状态分页查询项目
     */
    Page<Project> findByApprovalStatus(Project.ApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * 根据创建者和审批状态查询项目
     */
    List<Project> findByCreatedByAndApprovalStatus(Long createdBy, Project.ApprovalStatus approvalStatus);
    
    /**
     * 根据创建者和审批状态分页查询项目
     */
    Page<Project> findByCreatedByAndApprovalStatus(Long createdBy, Project.ApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * 查询草稿状态的项目
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus = 'DRAFT'")
    List<Project> findDraftProjects();
    
    /**
     * 查询已提交等待审批的项目
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus = 'AI_ANALYZING'")
    List<Project> findSubmittedProjects();
    
    /**
     * 查询AI分析通过的项目
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus = 'AI_APPROVED'")
    List<Project> findAIApprovedProjects();
    
    /**
     * 查询等待管理员审批的项目
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus = 'ADMIN_REVIEWING'")
    List<Project> findPendingAdminReviewProjects();
    
    /**
     * 查询等待超级管理员审批的项目
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus = 'SUPER_ADMIN_REVIEWING'")
    List<Project> findPendingSuperAdminReviewProjects();
    
    /**
     * 查询已批准的项目（超级管理员批准和最终批准）
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus IN ('SUPER_ADMIN_APPROVED', 'FINAL_APPROVED')")
    List<Project> findApprovedProjects();
    
    /**
     * 查询被拒绝的项目
     */
    @Query("SELECT p FROM Project p WHERE p.approvalStatus IN ('AI_REJECTED', 'ADMIN_REJECTED', 'SUPER_ADMIN_REJECTED')")
    List<Project> findRejectedProjects();
    
    // 基于AI分析的查询
    
    /**
     * 根据AI分析ID查询项目
     */
    Optional<Project> findByAiAnalysisId(Long aiAnalysisId);
    
    /**
     * 查询有AI分析结果的项目
     */
    @Query("SELECT p FROM Project p WHERE p.aiAnalysisId IS NOT NULL")
    List<Project> findProjectsWithAIAnalysis();
    
    /**
     * 查询没有AI分析结果的项目
     */
    @Query("SELECT p FROM Project p WHERE p.aiAnalysisId IS NULL")
    List<Project> findProjectsWithoutAIAnalysis();
    
    // 基于审批人的查询
    
    /**
     * 根据管理员审批人ID查询项目
     */
    List<Project> findByAdminReviewerId(Long adminReviewerId);
    
    /**
     * 根据超级管理员审批人ID查询项目
     */
    List<Project> findBySuperAdminReviewerId(Long superAdminReviewerId);
    
    /**
     * 查询特定审批人负责的项目
     */
    @Query("SELECT p FROM Project p WHERE p.adminReviewerId = :reviewerId OR p.superAdminReviewerId = :reviewerId")
    List<Project> findByReviewerId(@Param("reviewerId") Long reviewerId);
    
    // 时间范围查询
    
    /**
     * 根据创建时间范围查询项目
     */
    List<Project> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据更新时间范围查询项目
     */
    List<Project> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查询最近创建的项目
     */
    @Query("SELECT p FROM Project p ORDER BY p.createdAt DESC")
    List<Project> findRecentProjects(Pageable pageable);
    
    // 搜索查询
    
    /**
     * 综合搜索（项目名称、描述、成员）
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.members) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Project> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 综合搜索分页
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.members) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据成员搜索项目
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.members) LIKE LOWER(CONCAT('%', :member, '%'))")
    List<Project> findByMembersContaining(@Param("member") String member);
    
    // 统计查询
    
    /**
     * 统计各审批状态的项目数量
     */
    @Query("SELECT p.approvalStatus, COUNT(p) FROM Project p GROUP BY p.approvalStatus")
    List<Object[]> countByApprovalStatus();
    
    /**
     * 统计创建者的项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdBy = :createdBy")
    long countByCreatedBy(@Param("createdBy") Long createdBy);
    
    /**
     * 统计特定状态的项目数量
     */
    long countByApprovalStatus(Project.ApprovalStatus approvalStatus);
    
    /**
     * 统计最近时间段的项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdAt >= :startDate")
    long countProjectsCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    // 批量操作
    
    /**
     * 批量更新审批状态
     */
    @Query("UPDATE Project p SET p.approvalStatus = :newStatus WHERE p.id IN :projectIds")
    int updateApprovalStatusBatch(@Param("projectIds") List<Long> projectIds, 
                                 @Param("newStatus") Project.ApprovalStatus newStatus);
    
    /**
     * 批量设置审批人
     */
    @Query("UPDATE Project p SET p.adminReviewerId = :reviewerId WHERE p.id IN :projectIds")
    int updateAdminReviewerBatch(@Param("projectIds") List<Long> projectIds, 
                                @Param("reviewerId") Long reviewerId);
    
    // JOIN查询优化
    
    /**
     * 通过JOIN查询获取项目详情及创建者用户名
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername " +
           "FROM Project p LEFT JOIN User u ON p.createdBy = u.id " +
           "WHERE p.id = :projectId")
    com.weeklyreport.repository.projection.ProjectWithCreatorProjection findProjectWithCreator(@Param("projectId") Long projectId);
    
    /**
     * 通过JOIN查询获取所有项目及创建者用户名（分页）
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername " +
           "FROM Project p LEFT JOIN User u ON p.createdBy = u.id")
    org.springframework.data.domain.Page<com.weeklyreport.repository.projection.ProjectWithCreatorProjection> findAllProjectsWithCreator(org.springframework.data.domain.Pageable pageable);
    
    // 复杂查询
    
    /**
     * 查询用户可见的项目（创建者或参与成员）
     */
    @Query("SELECT p FROM Project p WHERE p.createdBy = :userId OR LOWER(p.members) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<Project> findVisibleProjectsForUser(@Param("userId") Long userId, @Param("username") String username);
    
    /**
     * 根据多条件过滤项目
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(:createdBy IS NULL OR p.createdBy = :createdBy) AND " +
           "(:approvalStatus IS NULL OR p.approvalStatus = :approvalStatus) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Project> findProjectsWithFilters(@Param("createdBy") Long createdBy,
                                         @Param("approvalStatus") Project.ApprovalStatus approvalStatus,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);
    
    // 优化的多表联查方法
    
    /**
     * 获取待管理员审核项目的完整信息（包含用户信息和AI分析结果）
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus = 'ADMIN_REVIEWING' " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findPendingAdminReviewProjectsWithDetails();
    
    /**
     * 获取待超级管理员审核项目的完整信息（管理员已通过的项目）
     * 超级管理员待通过页面显示：管理员已审核通过，等待超级管理员审核的项目
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus = 'SUPER_ADMIN_REVIEWING' " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findPendingSuperAdminReviewProjectsWithDetails();
    
    /**
     * 获取管理员拒绝的项目（供超级管理员审核）
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus = 'ADMIN_REJECTED' " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findAdminRejectedProjectsWithDetails();

    /**
     * 查询所有已通过的项目（用于超级管理员）
     * 包含: ADMIN_APPROVED, SUPER_ADMIN_APPROVED, FINAL_APPROVED
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus IN ('SUPER_ADMIN_APPROVED', 'FINAL_APPROVED') " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findApprovedProjectsWithDetails();

    /**
     * 查询管理员可见的已通过项目（用于管理员）
     * 包含: SUPER_ADMIN_APPROVED, FINAL_APPROVED
     * 注意：ADMIN_APPROVED状态现在不存在，管理员审批后直接进入SUPER_ADMIN_REVIEWING
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus IN ('SUPER_ADMIN_APPROVED', 'FINAL_APPROVED') " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findAdminApprovedProjectsWithDetails();

    /**
     * 查询所有已拒绝的项目（用于超级管理员）
     * 只显示超级管理员拒绝的项目
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus IN ('SUPER_ADMIN_REJECTED') " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findAllRejectedProjectsWithDetails();

    /**
     * 查询管理员可见的已拒绝项目（用于管理员）
     * 包含: AI_REJECTED, ADMIN_REJECTED
     */
    @Query("SELECT p.id as id, p.name as name, p.description as description, p.members as members, " +
           "p.expectedResults as expectedResults, p.timeline as timeline, p.stopLoss as stopLoss, " +
           "p.createdBy as createdBy, p.aiAnalysisId as aiAnalysisId, p.adminReviewerId as adminReviewerId, " +
           "p.superAdminReviewerId as superAdminReviewerId, p.rejectionReason as rejectionReason, " +
           "p.approvalStatus as approvalStatus, p.createdAt as createdAt, p.updatedAt as updatedAt, " +
           "u.username as createdByUsername, u.username as createdByFullName, " +
           "adminReviewer.username as adminReviewerUsername, superAdminReviewer.username as superAdminReviewerUsername, " +
           "ai.id as aiResultId, ai.result as aiResult, ai.confidence as aiConfidence, " +
           "ai.modelVersion as aiModelVersion, ai.status as aiStatus, ai.analysisType as aiType, " +
           "ai.entityType as aiEntityType, ai.createdAt as aiCreatedAt " +
           "FROM Project p " +
           "LEFT JOIN User u ON p.createdBy = u.id " +
           "LEFT JOIN User adminReviewer ON p.adminReviewerId = adminReviewer.id " +
           "LEFT JOIN User superAdminReviewer ON p.superAdminReviewerId = superAdminReviewer.id " +
           "LEFT JOIN AIAnalysisResult ai ON p.aiAnalysisId = ai.id " +
           "WHERE p.approvalStatus IN ('ADMIN_REJECTED') " +
           "ORDER BY p.createdAt DESC")
    List<com.weeklyreport.repository.projection.ProjectDetailProjection> findAdminVisibleRejectedProjectsWithDetails();
}