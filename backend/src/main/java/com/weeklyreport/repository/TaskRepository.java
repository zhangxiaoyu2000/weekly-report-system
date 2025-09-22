package com.weeklyreport.repository;

import com.weeklyreport.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Task repository - 包含核心业务查询方法
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * 根据创建者ID查询任务（正确的字段名）
     */
    List<Task> findByCreatedBy(Long createdBy);
    
    /**
     * 根据创建者ID分页查询任务
     */
    Page<Task> findByCreatedBy(Long createdBy, Pageable pageable);
    
    /**
     * 根据创建者ID排序查询任务
     */
    List<Task> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    // taskType 字段已移除，相关查询方法已删除

    // 为TaskService添加核心业务查询方法
    @Query("SELECT t FROM Task t WHERE t.createdBy = :userId")
    Page<Task> findTasksByUserId(@Param("userId") Long userId, Pageable pageable);

    // 完成状态现在通过 TaskReport 关联表判断，不再基于 actualResults 字段
    @Query("SELECT t FROM Task t WHERE t.createdBy = :userId AND t.expectedResults IS NOT NULL")
    List<Task> findTasksByUserIdAndExpectedResults(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.expectedResults IS NULL OR t.expectedResults = ''")
    List<Task> findIncompleteTasks();

    @Query("SELECT t FROM Task t WHERE t.expectedResults IS NOT NULL AND DATE(t.updatedAt) BETWEEN :startDate AND :endDate")
    List<Task> findDefinedTasksByDateRange(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);

    // 简化的关联查询 - 按创建时间排序
    @Query("SELECT t FROM Task t WHERE t.createdBy = :createdBy ORDER BY t.createdAt ASC")
    List<Task> findByCreatedByOrderByCreatedAtAsc(@Param("createdBy") Long createdBy);

    // 统计方法 - 基于保留字段
    Long countByCreatedBy(Long createdBy);
    Long countByCreatedByAndExpectedResultsIsNotNull(Long createdBy);
}