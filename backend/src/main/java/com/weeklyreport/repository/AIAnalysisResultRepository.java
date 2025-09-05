package com.weeklyreport.repository;

import com.weeklyreport.entity.AIAnalysisResult;
import com.weeklyreport.entity.WeeklyReport;
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
 * Repository interface for AIAnalysisResult entity
 */
@Repository
public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, Long>, JpaSpecificationExecutor<AIAnalysisResult> {

    // Find by weekly report
    List<AIAnalysisResult> findByWeeklyReportId(Long reportId);
    
    Page<AIAnalysisResult> findByWeeklyReportId(Long reportId, Pageable pageable);
    
    List<AIAnalysisResult> findByWeeklyReport(WeeklyReport weeklyReport);

    // Find by analysis type
    List<AIAnalysisResult> findByAnalysisType(AIAnalysisResult.AnalysisType analysisType);
    
    List<AIAnalysisResult> findByWeeklyReportIdAndAnalysisType(Long reportId, AIAnalysisResult.AnalysisType analysisType);
    
    Optional<AIAnalysisResult> findTopByWeeklyReportIdAndAnalysisTypeOrderByCreatedAtDesc(Long reportId, AIAnalysisResult.AnalysisType analysisType);

    // Find by status
    List<AIAnalysisResult> findByStatus(AIAnalysisResult.AnalysisStatus status);
    
    Page<AIAnalysisResult> findByStatus(AIAnalysisResult.AnalysisStatus status, Pageable pageable);
    
    List<AIAnalysisResult> findByStatusIn(List<AIAnalysisResult.AnalysisStatus> statuses);

    // Find by report and status
    List<AIAnalysisResult> findByWeeklyReportIdAndStatus(Long reportId, AIAnalysisResult.AnalysisStatus status);
    
    List<AIAnalysisResult> findByWeeklyReportIdAndStatusIn(Long reportId, List<AIAnalysisResult.AnalysisStatus> statuses);

    // Find by date ranges
    List<AIAnalysisResult> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<AIAnalysisResult> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by author (through weekly report)
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.weeklyReport.author.id = :authorId")
    List<AIAnalysisResult> findByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.weeklyReport.author.id = :authorId AND a.analysisType = :analysisType")
    List<AIAnalysisResult> findByAuthorIdAndAnalysisType(@Param("authorId") Long authorId, 
                                                        @Param("analysisType") AIAnalysisResult.AnalysisType analysisType);

    // Find by department (through weekly report author)
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.weeklyReport.author.department.id = :departmentId")
    List<AIAnalysisResult> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.weeklyReport.author.department.id = :departmentId AND a.status = :status")
    List<AIAnalysisResult> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, 
                                                      @Param("status") AIAnalysisResult.AnalysisStatus status);

    // Find pending analysis tasks
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'PENDING' ORDER BY a.createdAt ASC")
    List<AIAnalysisResult> findPendingAnalysisTasks();
    
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'PENDING' AND a.analysisType = :analysisType ORDER BY a.createdAt ASC")
    List<AIAnalysisResult> findPendingAnalysisTasksByType(@Param("analysisType") AIAnalysisResult.AnalysisType analysisType);

    // Find failed analysis tasks
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'FAILED' ORDER BY a.updatedAt DESC")
    List<AIAnalysisResult> findFailedAnalysisTasks();
    
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'FAILED' AND a.createdAt >= :since ORDER BY a.updatedAt DESC")
    List<AIAnalysisResult> findFailedAnalysisTasksSince(@Param("since") LocalDateTime since);

    // Find recent completed analysis
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' ORDER BY a.completedAt DESC")
    List<AIAnalysisResult> findRecentCompletedAnalysis(Pageable pageable);
    
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt >= :since ORDER BY a.completedAt DESC")
    List<AIAnalysisResult> findRecentCompletedAnalysisSince(@Param("since") LocalDateTime since);

    // Find by confidence range
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.confidence >= :minConfidence AND a.status = 'COMPLETED'")
    List<AIAnalysisResult> findByMinConfidence(@Param("minConfidence") Double minConfidence);
    
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.confidence BETWEEN :minConfidence AND :maxConfidence AND a.status = 'COMPLETED'")
    List<AIAnalysisResult> findByConfidenceRange(@Param("minConfidence") Double minConfidence, 
                                                @Param("maxConfidence") Double maxConfidence);

    // Find by processing time
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.processingTimeMs > :maxTimeMs AND a.status = 'COMPLETED'")
    List<AIAnalysisResult> findSlowAnalysisTasks(@Param("maxTimeMs") Long maxTimeMs);
    
    @Query("SELECT AVG(a.processingTimeMs) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.analysisType = :analysisType")
    Double getAverageProcessingTimeByType(@Param("analysisType") AIAnalysisResult.AnalysisType analysisType);

    // Find by model version
    List<AIAnalysisResult> findByModelVersion(String modelVersion);
    
    @Query("SELECT a.modelVersion, COUNT(a) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' GROUP BY a.modelVersion ORDER BY COUNT(a) DESC")
    List<Object[]> getModelVersionUsageStats();

    // Statistics and counts
    @Query("SELECT COUNT(a) FROM AIAnalysisResult a WHERE a.status = :status")
    long countByStatus(@Param("status") AIAnalysisResult.AnalysisStatus status);
    
    @Query("SELECT a.analysisType, COUNT(a) FROM AIAnalysisResult a GROUP BY a.analysisType ORDER BY COUNT(a) DESC")
    List<Object[]> countByAnalysisType();
    
    @Query("SELECT a.status, COUNT(a) FROM AIAnalysisResult a GROUP BY a.status")
    List<Object[]> countByStatus();

    // Daily analysis statistics
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM AIAnalysisResult a WHERE a.createdAt >= :since GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt) DESC")
    List<Object[]> getDailyAnalysisCount(@Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(a.completedAt), COUNT(a), AVG(a.processingTimeMs) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt >= :since GROUP BY DATE(a.completedAt) ORDER BY DATE(a.completedAt) DESC")
    List<Object[]> getDailyCompletionStats(@Param("since") LocalDateTime since);

    // Analysis type usage by author
    @Query("SELECT a.weeklyReport.author.id, a.analysisType, COUNT(a) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' GROUP BY a.weeklyReport.author.id, a.analysisType ORDER BY COUNT(a) DESC")
    List<Object[]> getAnalysisUsageByAuthor();

    // Check if analysis exists for report and type
    boolean existsByWeeklyReportIdAndAnalysisType(Long reportId, AIAnalysisResult.AnalysisType analysisType);
    
    boolean existsByWeeklyReportIdAndAnalysisTypeAndStatus(Long reportId, AIAnalysisResult.AnalysisType analysisType, AIAnalysisResult.AnalysisStatus status);

    // Complex filtering
    @Query("SELECT a FROM AIAnalysisResult a WHERE " +
           "(:reportId IS NULL OR a.weeklyReport.id = :reportId) AND " +
           "(:analysisType IS NULL OR a.analysisType = :analysisType) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:minConfidence IS NULL OR a.confidence >= :minConfidence) AND " +
           "(:maxProcessingTime IS NULL OR a.processingTimeMs <= :maxProcessingTime) " +
           "ORDER BY a.createdAt DESC")
    Page<AIAnalysisResult> findAnalysisWithFilters(@Param("reportId") Long reportId,
                                                  @Param("analysisType") AIAnalysisResult.AnalysisType analysisType,
                                                  @Param("status") AIAnalysisResult.AnalysisStatus status,
                                                  @Param("minConfidence") Double minConfidence,
                                                  @Param("maxProcessingTime") Long maxProcessingTime,
                                                  Pageable pageable);

    // Delete old completed analysis (cleanup)
    @Query("DELETE FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt < :cutoffDate")
    int deleteOldCompletedAnalysis(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("DELETE FROM AIAnalysisResult a WHERE a.status = 'FAILED' AND a.updatedAt < :cutoffDate")
    int deleteOldFailedAnalysis(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Update status batch operations
    @Query("UPDATE AIAnalysisResult a SET a.status = :newStatus WHERE a.id IN :analysisIds")
    int updateStatusBatch(@Param("analysisIds") List<Long> analysisIds, 
                         @Param("newStatus") AIAnalysisResult.AnalysisStatus newStatus);
    
    @Query("UPDATE AIAnalysisResult a SET a.status = 'CANCELLED' WHERE a.status = 'PENDING' AND a.createdAt < :cutoffDate")
    int cancelStaleAnalysisTasks(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Performance monitoring
    @Query("SELECT a.analysisType, COUNT(a), AVG(a.processingTimeMs), MIN(a.processingTimeMs), MAX(a.processingTimeMs) " +
           "FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt >= :since " +
           "GROUP BY a.analysisType ORDER BY AVG(a.processingTimeMs) DESC")
    List<Object[]> getPerformanceStatsByType(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.modelVersion, AVG(a.confidence) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.confidence IS NOT NULL GROUP BY a.modelVersion ORDER BY AVG(a.confidence) DESC")
    List<Object[]> getConfidenceStatsByModel();
}