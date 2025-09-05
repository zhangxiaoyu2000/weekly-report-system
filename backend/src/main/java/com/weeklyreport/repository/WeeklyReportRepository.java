package com.weeklyreport.repository;

import com.weeklyreport.entity.WeeklyReport;
import com.weeklyreport.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WeeklyReport entity
 */
@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long>, JpaSpecificationExecutor<WeeklyReport> {

    // Find by author
    List<WeeklyReport> findByAuthorId(Long authorId);
    
    Page<WeeklyReport> findByAuthorId(Long authorId, Pageable pageable);
    
    List<WeeklyReport> findByAuthor(User author);

    // Find by report week and year
    List<WeeklyReport> findByReportWeek(LocalDate reportWeek);
    
    List<WeeklyReport> findByYearAndWeekNumber(Integer year, Integer weekNumber);
    
    Optional<WeeklyReport> findByAuthorIdAndReportWeek(Long authorId, LocalDate reportWeek);
    
    boolean existsByAuthorIdAndReportWeek(Long authorId, LocalDate reportWeek);
    
    boolean existsByAuthorAndReportWeek(User author, LocalDate reportWeek);

    // Find by status
    List<WeeklyReport> findByStatus(WeeklyReport.ReportStatus status);
    
    Page<WeeklyReport> findByStatus(WeeklyReport.ReportStatus status, Pageable pageable);
    
    List<WeeklyReport> findByStatusIn(List<WeeklyReport.ReportStatus> statuses);

    // Find by date ranges
    List<WeeklyReport> findByReportWeekBetween(LocalDate startWeek, LocalDate endWeek);
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.reportWeek >= :startDate AND r.reportWeek <= :endDate ORDER BY r.reportWeek DESC")
    List<WeeklyReport> findByReportWeekRange(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    // Find by creation/submission dates
    List<WeeklyReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<WeeklyReport> findBySubmittedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by author and status
    List<WeeklyReport> findByAuthorIdAndStatus(Long authorId, WeeklyReport.ReportStatus status);
    
    Page<WeeklyReport> findByAuthorIdAndStatus(Long authorId, WeeklyReport.ReportStatus status, Pageable pageable);

    // Find by department (through user)
    @Query("SELECT r FROM WeeklyReport r WHERE r.author.department.id = :departmentId")
    List<WeeklyReport> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.author.department.id = :departmentId AND r.status = :status")
    List<WeeklyReport> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, 
                                                  @Param("status") WeeklyReport.ReportStatus status);
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.author.department.id = :departmentId AND r.reportWeek = :reportWeek")
    List<WeeklyReport> findByDepartmentIdAndReportWeek(@Param("departmentId") Long departmentId, 
                                                      @Param("reportWeek") LocalDate reportWeek);

    // Find by department hierarchy
    @Query("SELECT r FROM WeeklyReport r WHERE r.author.department.path LIKE CONCAT(:departmentPath, '%')")
    List<WeeklyReport> findByDepartmentHierarchy(@Param("departmentPath") String departmentPath);

    // Find by template
    List<WeeklyReport> findByTemplateId(Long templateId);
    
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.template.id = :templateId")
    long countByTemplateId(@Param("templateId") Long templateId);

    // Find by reviewer
    List<WeeklyReport> findByReviewerId(Long reviewerId);
    
    List<WeeklyReport> findByReviewerIdAndStatus(Long reviewerId, WeeklyReport.ReportStatus status);

    // Search methods
    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "r.status = :status")
    Page<WeeklyReport> searchByKeywordAndStatus(@Param("keyword") String keyword, 
                                               @Param("status") WeeklyReport.ReportStatus status, 
                                               Pageable pageable);

    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "r.author.id = :authorId AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<WeeklyReport> searchByAuthorAndKeyword(@Param("authorId") Long authorId, 
                                               @Param("keyword") String keyword);

    // Statistics and counts
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.status = :status")
    long countByStatus(@Param("status") WeeklyReport.ReportStatus status);
    
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.author.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.reportWeek = :reportWeek")
    long countByReportWeek(@Param("reportWeek") LocalDate reportWeek);

    // Late reports
    @Query("SELECT r FROM WeeklyReport r WHERE r.isLate = true")
    List<WeeklyReport> findLateReports();
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.isLate = true AND r.author.department.id = :departmentId")
    List<WeeklyReport> findLateReportsByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(r) FROM WeeklyReport r WHERE r.isLate = true AND r.reportWeek >= :startDate")
    long countLateReportsSince(@Param("startDate") LocalDate startDate);

    // Recent reports
    @Query("SELECT r FROM WeeklyReport r WHERE r.createdAt >= :sinceDate ORDER BY r.createdAt DESC")
    List<WeeklyReport> findRecentReports(@Param("sinceDate") LocalDateTime sinceDate);
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.submittedAt >= :sinceDate ORDER BY r.submittedAt DESC")
    List<WeeklyReport> findRecentlySubmittedReports(@Param("sinceDate") LocalDateTime sinceDate);

    // Pending reviews
    @Query("SELECT r FROM WeeklyReport r WHERE r.status IN ('SUBMITTED', 'UNDER_REVIEW') ORDER BY r.submittedAt ASC")
    List<WeeklyReport> findPendingReviews();
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.status IN ('SUBMITTED', 'UNDER_REVIEW') AND r.author.department.id = :departmentId ORDER BY r.submittedAt ASC")
    List<WeeklyReport> findPendingReviewsByDepartment(@Param("departmentId") Long departmentId);

    // Priority reports
    List<WeeklyReport> findByPriorityGreaterThanEqual(Integer priority);
    
    @Query("SELECT r FROM WeeklyReport r WHERE r.priority >= :minPriority AND r.status = :status ORDER BY r.priority DESC")
    List<WeeklyReport> findHighPriorityReportsByStatus(@Param("minPriority") Integer minPriority, 
                                                       @Param("status") WeeklyReport.ReportStatus status);

    // Reports with comments
    @Query("SELECT DISTINCT r FROM WeeklyReport r JOIN r.comments c WHERE c.status = 'ACTIVE'")
    List<WeeklyReport> findReportsWithComments();
    
    @Query("SELECT r, COUNT(c) FROM WeeklyReport r LEFT JOIN r.comments c GROUP BY r ORDER BY COUNT(c) DESC")
    List<Object[]> findReportsOrderByCommentCount();

    // Reporting and analytics
    @Query("SELECT r.status, COUNT(r) FROM WeeklyReport r GROUP BY r.status")
    List<Object[]> countReportsByStatus();
    
    @Query("SELECT r.year, r.weekNumber, COUNT(r) FROM WeeklyReport r GROUP BY r.year, r.weekNumber ORDER BY r.year DESC, r.weekNumber DESC")
    List<Object[]> countReportsByWeek();
    
    @Query("SELECT r.author.department.id, r.author.department.name, COUNT(r) FROM WeeklyReport r WHERE r.reportWeek >= :startDate GROUP BY r.author.department.id, r.author.department.name ORDER BY COUNT(r) DESC")
    List<Object[]> countReportsByDepartmentSince(@Param("startDate") LocalDate startDate);

    // User productivity analytics
    @Query("SELECT r.author.id, r.author.fullName, COUNT(r), AVG(r.wordCount) FROM WeeklyReport r WHERE r.reportWeek >= :startDate GROUP BY r.author.id, r.author.fullName ORDER BY COUNT(r) DESC")
    List<Object[]> getUserProductivityStats(@Param("startDate") LocalDate startDate);
    
    @Query("SELECT AVG(r.wordCount) FROM WeeklyReport r WHERE r.reportWeek >= :startDate")
    Double getAverageWordCountSince(@Param("startDate") LocalDate startDate);

    // Missing reports (users who should have submitted but didn't)
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.role = 'EMPLOYEE' AND NOT EXISTS (SELECT r FROM WeeklyReport r WHERE r.author = u AND r.reportWeek = :reportWeek)")
    List<User> findUsersWithMissingReports(@Param("reportWeek") LocalDate reportWeek);
    
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.department.id = :departmentId AND NOT EXISTS (SELECT r FROM WeeklyReport r WHERE r.author = u AND r.reportWeek = :reportWeek)")
    List<User> findUsersWithMissingReportsInDepartment(@Param("departmentId") Long departmentId, 
                                                       @Param("reportWeek") LocalDate reportWeek);

    // Complex filtering
    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "(:authorId IS NULL OR r.author.id = :authorId) AND " +
           "(:departmentId IS NULL OR r.author.department.id = :departmentId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startWeek IS NULL OR r.reportWeek >= :startWeek) AND " +
           "(:endWeek IS NULL OR r.reportWeek <= :endWeek) " +
           "ORDER BY r.reportWeek DESC, r.createdAt DESC")
    Page<WeeklyReport> findReportsWithFilters(@Param("authorId") Long authorId,
                                             @Param("departmentId") Long departmentId,
                                             @Param("status") WeeklyReport.ReportStatus status,
                                             @Param("startWeek") LocalDate startWeek,
                                             @Param("endWeek") LocalDate endWeek,
                                             Pageable pageable);

    // Update operations
    @Query("UPDATE WeeklyReport r SET r.status = :newStatus WHERE r.id IN :reportIds")
    int updateStatusBatch(@Param("reportIds") List<Long> reportIds, 
                         @Param("newStatus") WeeklyReport.ReportStatus newStatus);
    
    @Query("UPDATE WeeklyReport r SET r.isLate = true WHERE r.reportWeek < :cutoffDate AND r.status = 'DRAFT'")
    int markLateReports(@Param("cutoffDate") LocalDate cutoffDate);

    // Template usage analytics
    @Query("SELECT r.template.id, r.template.name, COUNT(r) FROM WeeklyReport r WHERE r.template IS NOT NULL GROUP BY r.template.id, r.template.name ORDER BY COUNT(r) DESC")
    List<Object[]> getTemplateUsageStats();

    // Find most recent report by author
    @Query("SELECT r FROM WeeklyReport r WHERE r.author.id = :authorId ORDER BY r.reportWeek DESC")
    List<WeeklyReport> findLatestReportsByAuthor(@Param("authorId") Long authorId);

    /**
     * Find reports with complex filters for WeeklyReportService
     */
    @Query("SELECT r FROM WeeklyReport r WHERE " +
           "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:authorId IS NULL OR r.author.id = :authorId) AND " +
           "(:reviewerId IS NULL OR r.reviewer.id = :reviewerId) AND " +
           "(:projectId IS NULL OR r.project.id = :projectId) AND " +
           "(:templateId IS NULL OR r.template.id = :templateId) AND " +
           "(:year IS NULL OR r.year = :year) AND " +
           "(:weekNumber IS NULL OR r.weekNumber = :weekNumber) AND " +
           "(:reportWeekFrom IS NULL OR r.reportWeek >= :reportWeekFrom) AND " +
           "(:reportWeekTo IS NULL OR r.reportWeek <= :reportWeekTo) AND " +
           "(:submittedFrom IS NULL OR r.submittedAt >= :submittedFrom) AND " +
           "(:submittedTo IS NULL OR r.submittedAt <= :submittedTo) AND " +
           "(:priorityMin IS NULL OR r.priority >= :priorityMin) AND " +
           "(:priorityMax IS NULL OR r.priority <= :priorityMax) AND " +
           "(:isLate IS NULL OR r.isLate = :isLate) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(r.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(r.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(r.workSummary) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(r.achievements) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<WeeklyReport> findWithFilters(@Param("title") String title,
                                       @Param("status") WeeklyReport.ReportStatus status,
                                       @Param("authorId") Long authorId,
                                       @Param("reviewerId") Long reviewerId,
                                       @Param("projectId") Long projectId,
                                       @Param("templateId") Long templateId,
                                       @Param("year") Integer year,
                                       @Param("weekNumber") Integer weekNumber,
                                       @Param("reportWeekFrom") LocalDate reportWeekFrom,
                                       @Param("reportWeekTo") LocalDate reportWeekTo,
                                       @Param("submittedFrom") LocalDate submittedFrom,
                                       @Param("submittedTo") LocalDate submittedTo,
                                       @Param("priorityMin") Integer priorityMin,
                                       @Param("priorityMax") Integer priorityMax,
                                       @Param("isLate") Boolean isLate,
                                       @Param("searchTerm") String searchTerm,
                                       Pageable pageable);

    /**
     * Find reports accessible to a user (own reports + reviewable reports for managers)
     */
    @Query("SELECT DISTINCT r FROM WeeklyReport r WHERE " +
           "r.author = :user OR " +
           "(:user.role IN ('ADMIN', 'HR_MANAGER', 'DEPARTMENT_MANAGER') AND " +
           " (r.author.department = :user.department OR :user.role IN ('ADMIN', 'HR_MANAGER')))")
    Page<WeeklyReport> findAccessibleReports(@Param("user") User user, Pageable pageable);

    /**
     * Get global report statistics by status
     */
    @Query("SELECT r.status, COUNT(r) FROM WeeklyReport r GROUP BY r.status")
    List<Object[]> getGlobalReportStatsByStatus();

    /**
     * Get user-specific report statistics by status
     */
    @Query("SELECT r.status, COUNT(r) FROM WeeklyReport r WHERE r.author = :user GROUP BY r.status")
    List<Object[]> getUserReportStatsByStatus(@Param("user") User user);
}