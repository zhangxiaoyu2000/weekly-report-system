package com.weeklyreport.repository;

import com.weeklyreport.entity.User;
import com.weeklyreport.entity.WeeklyReportV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 结构化周报V2数据访问层
 */
@Repository
public interface WeeklyReportV2Repository extends JpaRepository<WeeklyReportV2, Long> {

    /**
     * 根据用户查找周报
     */
    List<WeeklyReportV2> findByUser(User user);

    /**
     * 根据用户分页查找周报
     */
    Page<WeeklyReportV2> findByUser(User user, Pageable pageable);

    /**
     * 根据状态查找周报
     */
    List<WeeklyReportV2> findByStatus(WeeklyReportV2.ReportStatus status);

    /**
     * 根据状态分页查找周报
     */
    Page<WeeklyReportV2> findByStatus(WeeklyReportV2.ReportStatus status, Pageable pageable);

    /**
     * 根据用户和状态查找周报
     */
    List<WeeklyReportV2> findByUserAndStatus(User user, WeeklyReportV2.ReportStatus status);

    /**
     * 查找指定用户指定周的周报
     */
    Optional<WeeklyReportV2> findByUserAndWeekStart(User user, LocalDate weekStart);

    /**
     * 检查指定用户指定周是否已有周报
     */
    boolean existsByUserAndWeekStart(User user, LocalDate weekStart);

    /**
     * 根据周开始日期范围查找周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.weekStart BETWEEN :startDate AND :endDate")
    List<WeeklyReportV2> findByWeekStartBetween(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    /**
     * 根据用户和周开始日期范围查找周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.user = :user AND wr.weekStart BETWEEN :startDate AND :endDate")
    List<WeeklyReportV2> findByUserAndWeekStartBetween(@Param("user") User user,
                                                      @Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 查找需要AI分析的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.status = 'PENDING_AI' ORDER BY wr.submittedAt ASC")
    List<WeeklyReportV2> findReportsPendingAIAnalysis();

    /**
     * 查找需要管理员审批的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.status = 'PENDING_ADMIN' ORDER BY wr.submittedAt ASC")
    List<WeeklyReportV2> findReportsPendingAdminReview();

    /**
     * 查找需要超级管理员审批的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.status = 'PENDING_SUPER_ADMIN' ORDER BY wr.submittedAt ASC")
    List<WeeklyReportV2> findReportsPendingSuperAdminReview();

    /**
     * 查找指定审批人审批的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.adminReviewer = :reviewer OR wr.superAdminReviewer = :reviewer")
    List<WeeklyReportV2> findReportsReviewedBy(@Param("reviewer") User reviewer);

    /**
     * 查找指定时间范围内提交的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.submittedAt BETWEEN :startTime AND :endTime")
    List<WeeklyReportV2> findBySubmittedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定用户的周报数量
     */
    @Query("SELECT COUNT(wr) FROM WeeklyReportV2 wr WHERE wr.user = :user")
    long countByUser(@Param("user") User user);

    /**
     * 统计指定用户指定状态的周报数量
     */
    @Query("SELECT COUNT(wr) FROM WeeklyReportV2 wr WHERE wr.user = :user AND wr.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") WeeklyReportV2.ReportStatus status);

    /**
     * 获取用户最新的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.user = :user ORDER BY wr.weekStart DESC")
    Page<WeeklyReportV2> findLatestByUser(@Param("user") User user, Pageable pageable);

    /**
     * 查找所有已提交但未审批的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.status IN ('SUBMITTED', 'PENDING_AI', 'PENDING_ADMIN', 'PENDING_SUPER_ADMIN') ORDER BY wr.submittedAt ASC")
    List<WeeklyReportV2> findAllPendingReports();

    /**
     * 根据周报周次模糊查询
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.reportWeek LIKE CONCAT('%', :keyword, '%')")
    List<WeeklyReportV2> findByReportWeekContaining(@Param("keyword") String keyword);

    /**
     * 根据标题模糊查询
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.title LIKE CONCAT('%', :keyword, '%')")
    List<WeeklyReportV2> findByTitleContaining(@Param("keyword") String keyword);

    /**
     * 综合搜索：根据标题或周次查询
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.title LIKE CONCAT('%', :keyword, '%') OR wr.reportWeek LIKE CONCAT('%', :keyword, '%')")
    List<WeeklyReportV2> findByTitleOrReportWeekContaining(@Param("keyword") String keyword);

    /**
     * 查找AI分析通过的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.aiAnalysisPassed = true")
    List<WeeklyReportV2> findReportsPassedAIAnalysis();

    /**
     * 查找AI分析未通过的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.aiAnalysisPassed = false")
    List<WeeklyReportV2> findReportsFailedAIAnalysis();

    /**
     * 获取用户周报统计信息
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN wr.status = 'DRAFT' THEN 1 END) as draftReports, " +
           "COUNT(CASE WHEN wr.status = 'APPROVED' THEN 1 END) as approvedReports, " +
           "COUNT(CASE WHEN wr.status = 'REJECTED' THEN 1 END) as rejectedReports, " +
           "COUNT(CASE WHEN wr.status IN ('SUBMITTED', 'PENDING_AI', 'PENDING_ADMIN', 'PENDING_SUPER_ADMIN') THEN 1 END) as pendingReports, " +
           "COUNT(wr) as totalReports " +
           "FROM WeeklyReportV2 wr WHERE wr.user = :user")
    Object[] getReportStatisticsByUser(@Param("user") User user);

    /**
     * 查找本周需要创建周报的用户（还没有本周周报的用户）
     */
    @Query("SELECT u FROM User u WHERE u.id NOT IN (" +
           "SELECT wr.user.id FROM WeeklyReportV2 wr WHERE wr.weekStart = :weekStart)")
    List<User> findUsersWithoutReportForWeek(@Param("weekStart") LocalDate weekStart);

    /**
     * 根据ID和用户查找周报（用于权限验证）
     */
    Optional<WeeklyReportV2> findByIdAndUser(Long id, User user);

    /**
     * 查找指定用户最近N个周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.user = :user ORDER BY wr.weekStart DESC")
    List<WeeklyReportV2> findRecentReportsByUser(@Param("user") User user, Pageable pageable);

    /**
     * 查找逾期未提交的草稿周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.status = 'DRAFT' AND wr.weekEnd < :currentDate")
    List<WeeklyReportV2> findOverdueDraftReports(@Param("currentDate") LocalDate currentDate);

    /**
     * 查找待处理时间超过指定天数的周报
     */
    @Query("SELECT wr FROM WeeklyReportV2 wr WHERE wr.status IN ('PENDING_AI', 'PENDING_ADMIN', 'PENDING_SUPER_ADMIN') " +
           "AND wr.submittedAt < :threshold")
    List<WeeklyReportV2> findOverduePendingReports(@Param("threshold") LocalDateTime threshold);
}