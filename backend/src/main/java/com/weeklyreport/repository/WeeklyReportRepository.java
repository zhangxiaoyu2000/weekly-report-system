package com.weeklyreport.repository;

import com.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.entity.AIAnalysisResult;
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
 * Repository interface for WeeklyReport entity
 */
@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long>, JpaSpecificationExecutor<WeeklyReport> {

    // Find by user (updated for new entity structure) - authorId renamed to userId
    
    // 新增方法以支持V3重构
    List<WeeklyReport> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<WeeklyReport> findByApprovalStatusOrderByCreatedAtDesc(WeeklyReport.ApprovalStatus approvalStatus);

    // Find by report week
    List<WeeklyReport> findByReportWeek(String reportWeek);
    
    Optional<WeeklyReport> findByUserIdAndReportWeek(Long userId, String reportWeek);
    
    boolean existsByUserIdAndReportWeek(Long userId, String reportWeek);
    
    // Check for existing reports excluding draft status
    boolean existsByUserIdAndReportWeekAndApprovalStatusNot(Long userId, String reportWeek, WeeklyReport.ApprovalStatus approvalStatus);
    
    // Check for existing reports excluding draft status and specific report ID
    boolean existsByUserIdAndReportWeekAndApprovalStatusNotAndIdNot(Long userId, String reportWeek, WeeklyReport.ApprovalStatus approvalStatus, Long reportId);

    // 核心业务查询方法
    @Query("SELECT wr FROM WeeklyReport wr WHERE wr.userId = :userId ORDER BY wr.createdAt DESC")
    Page<WeeklyReport> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 获取用户周报列表（包含AI分析结果）- 修复重复数据问题
    // 使用LEFT JOIN但通过DISTINCT避免重复，并在Java层处理多个AI结果
    @Query("SELECT DISTINCT wr, ai " +
           "FROM WeeklyReport wr " +
           "LEFT JOIN AIAnalysisResult ai ON ai.reportId = wr.id AND ai.entityType = 'WEEKLY_REPORT' " +
           "WHERE wr.userId = :userId " +
           "ORDER BY wr.createdAt DESC, ai.completedAt DESC")
    List<Object[]> findByUserIdWithAIAnalysis(@Param("userId") Long userId);

    // 获取所有周报列表（包含AI分析结果）- 分页版本，修复重复数据问题
    @Query("SELECT DISTINCT wr, ai " +
           "FROM WeeklyReport wr " +
           "LEFT JOIN AIAnalysisResult ai ON ai.reportId = wr.id AND ai.entityType = 'WEEKLY_REPORT' " +
           "ORDER BY wr.createdAt DESC, ai.completedAt DESC")
    Page<Object[]> findAllWithAIAnalysis(Pageable pageable);

    // 获取所有周报列表（包含AI分析结果）- 非分页版本，修复重复数据问题
    @Query("SELECT DISTINCT wr, ai " +
           "FROM WeeklyReport wr " +
           "LEFT JOIN AIAnalysisResult ai ON ai.reportId = wr.id AND ai.entityType = 'WEEKLY_REPORT' " +
           "ORDER BY wr.createdAt DESC, ai.completedAt DESC")
    List<Object[]> findAllWithAIAnalysis();

    // 根据审批状态获取周报列表（包含AI分析结果）- 修复重复数据问题
    @Query("SELECT DISTINCT wr, ai " +
           "FROM WeeklyReport wr " +
           "LEFT JOIN AIAnalysisResult ai ON ai.reportId = wr.id AND ai.entityType = 'WEEKLY_REPORT' " +
           "WHERE wr.approvalStatus = :status " +
           "ORDER BY wr.createdAt DESC, ai.completedAt DESC")
    List<Object[]> findByApprovalStatusWithAIAnalysis(@Param("status") WeeklyReport.ApprovalStatus status);

    // 获取单个周报详情（包含AI分析结果）- 修复重复数据问题
    @Query("SELECT DISTINCT wr, ai " +
           "FROM WeeklyReport wr " +
           "LEFT JOIN AIAnalysisResult ai ON ai.reportId = wr.id AND ai.entityType = 'WEEKLY_REPORT' " +
           "WHERE wr.id = :reportId " +
           "ORDER BY ai.completedAt DESC")
    Optional<Object[]> findByIdWithAIAnalysis(@Param("reportId") Long reportId);

    // Find by approval status
    List<WeeklyReport> findByApprovalStatus(WeeklyReport.ApprovalStatus approvalStatus);
    
    Page<WeeklyReport> findByApprovalStatus(WeeklyReport.ApprovalStatus approvalStatus, Pageable pageable);
    
    List<WeeklyReport> findByApprovalStatusIn(List<WeeklyReport.ApprovalStatus> approvalStatuses);

    // Find by creation dates
    List<WeeklyReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by user and approval status - authorId renamed to userId
    List<WeeklyReport> findByUserIdAndApprovalStatus(Long userId, WeeklyReport.ApprovalStatus approvalStatus);
    
    Page<WeeklyReport> findByUserIdAndApprovalStatus(Long userId, WeeklyReport.ApprovalStatus approvalStatus, Pageable pageable);

    // Department queries removed as User entity no longer has department field

    // Template queries removed as WeeklyReport entity no longer has template field

    // Find by reviewer (admin_reviewer_id field)
    List<WeeklyReport> findByAdminReviewerId(Long adminReviewerId);
    
    List<WeeklyReport> findByAdminReviewerIdAndApprovalStatus(Long adminReviewerId, WeeklyReport.ApprovalStatus approvalStatus);

    // Search methods
    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.additionalNotes) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "r.approvalStatus = :approvalStatus")
    Page<WeeklyReport> searchByKeywordAndApprovalStatus(@Param("keyword") String keyword, 
                                                       @Param("approvalStatus") WeeklyReport.ApprovalStatus approvalStatus, 
                                                       Pageable pageable);

    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "r.userId = :userId AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.additionalNotes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<WeeklyReport> searchByUserAndKeyword(@Param("userId") Long userId, 
                                             @Param("keyword") String keyword);

    // Statistics and counts
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.approvalStatus = :approvalStatus")
    long countByApprovalStatus(@Param("approvalStatus") WeeklyReport.ApprovalStatus approvalStatus);
    
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // Department count removed as User entity no longer has department field

    // Note: Late reports functionality removed - isLate field not in database schema

    // Recent reports
    @Query("SELECT r FROM WeeklyReport r WHERE r.createdAt >= :sinceDate ORDER BY r.createdAt DESC")
    List<WeeklyReport> findRecentReports(@Param("sinceDate") LocalDateTime sinceDate);

    // Pending reviews
    @Query("SELECT r FROM WeeklyReport r WHERE r.approvalStatus IN ('SUBMITTED', 'AI_APPROVED') ORDER BY r.createdAt ASC")
    List<WeeklyReport> findPendingReviews();
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.approvalStatus IN ('SUBMITTED', 'AI_APPROVED') AND r.userId = :userId ORDER BY r.createdAt ASC")
    List<WeeklyReport> findPendingReviewsByUser(@Param("userId") Long userId);

    // Priority reports - removed as priority field is not in new schema

    // Reports with comments - temporarily disabled due to comments relationship issues
    /*
    @Query("SELECT DISTINCT r FROM WeeklyReport r JOIN r.comments c WHERE c.status = 'ACTIVE'")
    List<WeeklyReport> findReportsWithComments();
    
    @Query("SELECT r, COUNT(c) FROM WeeklyReport r LEFT JOIN r.comments c GROUP BY r ORDER BY COUNT(c) DESC")
    List<Object[]> findReportsOrderByCommentCount();
    */

    // Reporting and analytics
    @Query("SELECT r.approvalStatus, COUNT(r) FROM WeeklyReport r GROUP BY r.approvalStatus")
    List<Object[]> countReportsByApprovalStatus();
    
    @Query("SELECT r.reportWeek, COUNT(r) FROM WeeklyReport r GROUP BY r.reportWeek ORDER BY r.reportWeek DESC")
    List<Object[]> countReportsByWeek();
    
    // Department queries removed as User entity no longer has department field

    // User productivity analytics (simplified for new schema)
    @Query("SELECT r.userId, COUNT(r) FROM WeeklyReport r WHERE r.createdAt >= :startDate GROUP BY r.userId ORDER BY COUNT(r) DESC")
    List<Object[]> getUserProductivityStats(@Param("startDate") LocalDateTime startDate);

    // Missing reports functionality removed - need to check from User service

    // Complex filtering (simplified for new schema)
    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:approvalStatus IS NULL OR r.approvalStatus = :approvalStatus) AND " +
           "(:reportWeek IS NULL OR r.reportWeek = :reportWeek) " +
           "ORDER BY r.createdAt DESC")
    Page<WeeklyReport> findReportsWithFilters(@Param("userId") Long userId,
                                             @Param("approvalStatus") WeeklyReport.ApprovalStatus approvalStatus,
                                             @Param("reportWeek") String reportWeek,
                                             Pageable pageable);

    // Update operations
    @Query("UPDATE WeeklyReport r SET r.approvalStatus = :newApprovalStatus WHERE r.id IN :reportIds")
    int updateApprovalStatusBatch(@Param("reportIds") List<Long> reportIds, 
                                 @Param("newApprovalStatus") WeeklyReport.ApprovalStatus newApprovalStatus);
    
    // Note: markLateReports removed - isLate field not in database schema

    // Template usage analytics removed

    // Find most recent report by user
    @Query("SELECT r FROM WeeklyReport r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<WeeklyReport> findLatestReportsByUser(@Param("userId") Long userId);

    /**
     * Find reports with complex filters (simplified for new database schema)
     */
    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:approvalStatus IS NULL OR r.approvalStatus = :approvalStatus) AND " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:adminReviewerId IS NULL OR r.adminReviewerId = :adminReviewerId) AND " +
           "(:reportWeek IS NULL OR r.reportWeek = :reportWeek) AND " +
           "(:createdFrom IS NULL OR r.createdAt >= :createdFrom) AND " +
           "(:createdTo IS NULL OR r.createdAt <= :createdTo) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(r.additionalNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<WeeklyReport> findWithFilters(@Param("title") String title,
                                       @Param("approvalStatus") WeeklyReport.ApprovalStatus approvalStatus,
                                       @Param("userId") Long userId,
                                       @Param("adminReviewerId") Long adminReviewerId,
                                       @Param("reportWeek") String reportWeek,
                                       @Param("createdFrom") LocalDateTime createdFrom,
                                       @Param("createdTo") LocalDateTime createdTo,
                                       @Param("searchTerm") String searchTerm,
                                       Pageable pageable);

    /**
     * Find reports accessible to a user (simplified for new schema)
     */
    @Query("SELECT DISTINCT r FROM WeeklyReport r WHERE " +
           "r.userId = :userId OR " +
           "(:userRole = 'SUPER_ADMIN') OR " +
           "(:userRole = 'ADMIN')")
    Page<WeeklyReport> findAccessibleReports(@Param("userId") Long userId, 
                                            @Param("userRole") String userRole, 
                                            Pageable pageable);

    /**
     * Get global report statistics by approval status
     */
    @Query("SELECT r.approvalStatus, COUNT(r) FROM WeeklyReport r GROUP BY r.approvalStatus")
    List<Object[]> getGlobalReportStatsByApprovalStatus();

    /**
     * Get user-specific report statistics by approval status
     */
    @Query("SELECT r.approvalStatus, COUNT(r) FROM WeeklyReport r WHERE r.userId = :userId GROUP BY r.approvalStatus")
    List<Object[]> getUserReportStatsByApprovalStatus(@Param("userId") Long userId);
}