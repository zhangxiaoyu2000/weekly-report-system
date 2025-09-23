package com.weeklyreport.repository;

import com.weeklyreport.entity.AIAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AIAnalysisResult entity - 严格按照AIAnalysisResult.java实体设计
 */
@Repository
public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, Long>, JpaSpecificationExecutor<AIAnalysisResult> {

    // 基于AIAnalysisResult.java实体字段的查询方法（使用reportId字段，不是WeeklyReport对象关联）
    
    /**
     * 根据周报ID查询AI分析结果
     */
    List<AIAnalysisResult> findByReportId(Long reportId);
    
    /**
     * 根据周报ID分页查询AI分析结果
     */
    Page<AIAnalysisResult> findByReportId(Long reportId, Pageable pageable);

    // 基于分析类型的查询 - 严格按照AIAnalysisResult.AnalysisType枚举
    
    /**
     * 根据分析类型查询
     */
    List<AIAnalysisResult> findByAnalysisType(AIAnalysisResult.AnalysisType analysisType);
    
    /**
     * 根据周报ID和分析类型查询
     */
    List<AIAnalysisResult> findByReportIdAndAnalysisType(Long reportId, AIAnalysisResult.AnalysisType analysisType);
    
    /**
     * 获取最新的分析结果
     */
    Optional<AIAnalysisResult> findTopByReportIdAndAnalysisTypeOrderByCreatedAtDesc(Long reportId, AIAnalysisResult.AnalysisType analysisType);

    // 基于状态的查询 - 严格按照AIAnalysisResult.AnalysisStatus枚举
    
    /**
     * 根据状态查询
     */
    List<AIAnalysisResult> findByStatus(AIAnalysisResult.AnalysisStatus status);
    
    /**
     * 根据状态分页查询
     */
    Page<AIAnalysisResult> findByStatus(AIAnalysisResult.AnalysisStatus status, Pageable pageable);
    
    /**
     * 根据多个状态查询
     */
    List<AIAnalysisResult> findByStatusIn(List<AIAnalysisResult.AnalysisStatus> statuses);

    // 复合查询
    
    /**
     * 根据周报ID和状态查询
     */
    List<AIAnalysisResult> findByReportIdAndStatus(Long reportId, AIAnalysisResult.AnalysisStatus status);
    
    /**
     * 根据周报ID和多个状态查询
     */
    List<AIAnalysisResult> findByReportIdAndStatusIn(Long reportId, List<AIAnalysisResult.AnalysisStatus> statuses);

    // 时间范围查询
    
    /**
     * 根据创建时间范围查询
     */
    List<AIAnalysisResult> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据完成时间范围查询
     */
    List<AIAnalysisResult> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 查询分析任务状态
    
    /**
     * 查询等待处理的分析任务
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'PENDING' ORDER BY a.createdAt ASC")
    List<AIAnalysisResult> findPendingAnalysisTasks();
    
    /**
     * 按类型查询等待处理的分析任务
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'PENDING' AND a.analysisType = :analysisType ORDER BY a.createdAt ASC")
    List<AIAnalysisResult> findPendingAnalysisTasksByType(@Param("analysisType") AIAnalysisResult.AnalysisType analysisType);

    /**
     * 查询失败的分析任务
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'FAILED' ORDER BY a.updatedAt DESC")
    List<AIAnalysisResult> findFailedAnalysisTasks();
    
    /**
     * 查询指定时间后失败的分析任务
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'FAILED' AND a.createdAt >= :since ORDER BY a.updatedAt DESC")
    List<AIAnalysisResult> findFailedAnalysisTasksSince(@Param("since") LocalDateTime since);

    /**
     * 查询最近完成的分析
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' ORDER BY a.completedAt DESC")
    List<AIAnalysisResult> findRecentCompletedAnalysis(Pageable pageable);
    
    /**
     * 查询指定时间后完成的分析
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt >= :since ORDER BY a.completedAt DESC")
    List<AIAnalysisResult> findRecentCompletedAnalysisSince(@Param("since") LocalDateTime since);

    // 基于置信度的查询
    
    /**
     * 查询最小置信度以上的分析结果
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.confidence >= :minConfidence AND a.status = 'COMPLETED'")
    List<AIAnalysisResult> findByMinConfidence(@Param("minConfidence") Double minConfidence);
    
    /**
     * 查询置信度范围内的分析结果
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.confidence BETWEEN :minConfidence AND :maxConfidence AND a.status = 'COMPLETED'")
    List<AIAnalysisResult> findByConfidenceRange(@Param("minConfidence") Double minConfidence, 
                                                @Param("maxConfidence") Double maxConfidence);

    // 基于处理时间的查询
    
    /**
     * 查询处理时间过长的分析任务
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE a.processingTimeMs > :maxTimeMs AND a.status = 'COMPLETED'")
    List<AIAnalysisResult> findSlowAnalysisTasks(@Param("maxTimeMs") Long maxTimeMs);
    
    /**
     * 获取按类型的平均处理时间
     */
    @Query("SELECT AVG(a.processingTimeMs) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.analysisType = :analysisType")
    Double getAverageProcessingTimeByType(@Param("analysisType") AIAnalysisResult.AnalysisType analysisType);

    // 基于模型版本的查询
    
    /**
     * 根据模型版本查询
     */
    List<AIAnalysisResult> findByModelVersion(String modelVersion);
    
    /**
     * 获取模型版本使用统计
     */
    @Query("SELECT a.modelVersion, COUNT(a) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' GROUP BY a.modelVersion ORDER BY COUNT(a) DESC")
    List<Object[]> getModelVersionUsageStats();

    // 统计查询
    
    /**
     * 根据状态统计数量
     */
    @Query("SELECT COUNT(a) FROM AIAnalysisResult a WHERE a.status = :status")
    long countByStatus(@Param("status") AIAnalysisResult.AnalysisStatus status);
    
    /**
     * 按分析类型统计数量
     */
    @Query("SELECT a.analysisType, COUNT(a) FROM AIAnalysisResult a GROUP BY a.analysisType ORDER BY COUNT(a) DESC")
    List<Object[]> countByAnalysisType();
    
    /**
     * 按状态统计数量
     */
    @Query("SELECT a.status, COUNT(a) FROM AIAnalysisResult a GROUP BY a.status")
    List<Object[]> countByStatus();

    // 时间统计查询
    
    /**
     * 获取每日分析数量统计
     */
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM AIAnalysisResult a WHERE a.createdAt >= :since GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt) DESC")
    List<Object[]> getDailyAnalysisCount(@Param("since") LocalDateTime since);
    
    /**
     * 获取每日完成统计
     */
    @Query("SELECT DATE(a.completedAt), COUNT(a), AVG(a.processingTimeMs) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt >= :since GROUP BY DATE(a.completedAt) ORDER BY DATE(a.completedAt) DESC")
    List<Object[]> getDailyCompletionStats(@Param("since") LocalDateTime since);

    // 基于实体类型的查询（项目 vs 周报）
    
    /**
     * 根据实体类型查询分析结果
     */
    List<AIAnalysisResult> findByEntityType(AIAnalysisResult.EntityType entityType);
    
    /**
     * 根据reportId和实体类型查询（对于项目分析，reportId存储项目ID）
     */
    List<AIAnalysisResult> findByReportIdAndEntityType(Long reportId, AIAnalysisResult.EntityType entityType);
    
    /**
     * 获取项目的最新AI分析结果
     */
    Optional<AIAnalysisResult> findTopByReportIdAndEntityTypeOrderByCreatedAtDesc(Long reportId, AIAnalysisResult.EntityType entityType);
    
    /**
     * 获取项目的已完成AI分析结果
     */
    List<AIAnalysisResult> findByReportIdAndEntityTypeAndStatus(Long reportId, AIAnalysisResult.EntityType entityType, AIAnalysisResult.AnalysisStatus status);

    // 存在性检查
    
    /**
     * 检查是否存在指定报告和类型的分析
     */
    boolean existsByReportIdAndAnalysisType(Long reportId, AIAnalysisResult.AnalysisType analysisType);
    
    /**
     * 检查是否存在指定报告、类型和状态的分析
     */
    boolean existsByReportIdAndAnalysisTypeAndStatus(Long reportId, AIAnalysisResult.AnalysisType analysisType, AIAnalysisResult.AnalysisStatus status);

    // 复杂过滤查询
    
    /**
     * 根据多条件过滤分析结果
     */
    @Query("SELECT a FROM AIAnalysisResult a WHERE " +
           "(:reportId IS NULL OR a.reportId = :reportId) AND " +
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

    // 清理操作
    
    /**
     * 删除指定周报的所有AI分析结果 - 用于周报更新时清理旧的分析数据
     */
    @Query("DELETE FROM AIAnalysisResult a WHERE a.reportId = :reportId AND a.entityType = :entityType")
    int deleteByReportIdAndEntityType(@Param("reportId") Long reportId, @Param("entityType") AIAnalysisResult.EntityType entityType);
    
    /**
     * 删除指定周报的所有AI分析结果（简化版本）- 删除周报相关的所有AI分析
     */
    @Modifying
    @Query("DELETE FROM AIAnalysisResult a WHERE a.reportId = :reportId AND a.entityType = :entityType")
    int deleteByReportId(@Param("reportId") Long reportId, @Param("entityType") AIAnalysisResult.EntityType entityType);
    
    /**
     * 删除过期的已完成分析
     */
    @Modifying
    @Query("DELETE FROM AIAnalysisResult a WHERE a.status = :status AND a.completedAt < :cutoffDate")
    int deleteOldCompletedAnalysis(@Param("status") AIAnalysisResult.AnalysisStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 删除过期的失败分析
     */
    @Modifying
    @Query("DELETE FROM AIAnalysisResult a WHERE a.status = :status AND a.updatedAt < :cutoffDate")
    int deleteOldFailedAnalysis(@Param("status") AIAnalysisResult.AnalysisStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);

    // 批量更新操作
    
    /**
     * 批量更新状态
     */
    @Query("UPDATE AIAnalysisResult a SET a.status = :newStatus WHERE a.id IN :analysisIds")
    int updateStatusBatch(@Param("analysisIds") List<Long> analysisIds, 
                         @Param("newStatus") AIAnalysisResult.AnalysisStatus newStatus);
    
    /**
     * 取消过期的等待分析任务
     */
    @Query("UPDATE AIAnalysisResult a SET a.status = 'CANCELLED' WHERE a.status = 'PENDING' AND a.createdAt < :cutoffDate")
    int cancelStaleAnalysisTasks(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 性能监控
    
    /**
     * 获取按类型的性能统计
     */
    @Query("SELECT a.analysisType, COUNT(a), AVG(a.processingTimeMs), MIN(a.processingTimeMs), MAX(a.processingTimeMs) " +
           "FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.completedAt >= :since " +
           "GROUP BY a.analysisType ORDER BY AVG(a.processingTimeMs) DESC")
    List<Object[]> getPerformanceStatsByType(@Param("since") LocalDateTime since);
    
    /**
     * 获取按模型的置信度统计
     */
    @Query("SELECT a.modelVersion, AVG(a.confidence) FROM AIAnalysisResult a WHERE a.status = 'COMPLETED' AND a.confidence IS NOT NULL GROUP BY a.modelVersion ORDER BY AVG(a.confidence) DESC")
    List<Object[]> getConfidenceStatsByModel();
}