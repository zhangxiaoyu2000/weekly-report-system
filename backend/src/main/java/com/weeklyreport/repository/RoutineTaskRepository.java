package com.weeklyreport.repository;

import com.weeklyreport.entity.RoutineTask;
import com.weeklyreport.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日常性任务数据访问层
 */
@Repository
public interface RoutineTaskRepository extends JpaRepository<RoutineTask, Long> {

    /**
     * 根据创建者查找任务
     */
    List<RoutineTask> findByCreatedBy(User createdBy);

    /**
     * 根据分配用户查找任务
     */
    List<RoutineTask> findByAssignedTo(User assignedTo);

    /**
     * 根据状态查找任务
     */
    List<RoutineTask> findByStatus(RoutineTask.TaskStatus status);

    /**
     * 根据任务类型查找任务
     */
    List<RoutineTask> findByTaskType(RoutineTask.TaskType taskType);

    /**
     * 根据分配用户和状态查找任务
     */
    List<RoutineTask> findByAssignedToAndStatus(User assignedTo, RoutineTask.TaskStatus status);

    /**
     * 根据分配用户和任务类型查找任务
     */
    List<RoutineTask> findByAssignedToAndTaskType(User assignedTo, RoutineTask.TaskType taskType);

    /**
     * 分页查询分配给指定用户的任务
     */
    Page<RoutineTask> findByAssignedTo(User assignedTo, Pageable pageable);

    /**
     * 分页查询指定用户创建的任务
     */
    Page<RoutineTask> findByCreatedBy(User createdBy, Pageable pageable);

    /**
     * 查找指定用户的活跃任务
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.assignedTo = :user AND rt.status = 'ACTIVE' ORDER BY rt.priority DESC, rt.createdAt ASC")
    List<RoutineTask> findActiveTasksByUser(@Param("user") User user);

    /**
     * 查找指定用户按优先级排序的活跃任务
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.assignedTo = :user AND rt.status = 'ACTIVE' ORDER BY " +
           "CASE rt.priority " +
           "WHEN 'URGENT' THEN 1 " +
           "WHEN 'HIGH' THEN 2 " +
           "WHEN 'NORMAL' THEN 3 " +
           "WHEN 'LOW' THEN 4 " +
           "END, rt.createdAt ASC")
    List<RoutineTask> findActiveTasksByUserOrderedByPriority(@Param("user") User user);

    /**
     * 查找指定时间范围内创建的任务
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.createdAt BETWEEN :startDate AND :endDate")
    List<RoutineTask> findTasksCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 查找指定时间范围内完成的任务
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.completedAt BETWEEN :startDate AND :endDate")
    List<RoutineTask> findTasksCompletedBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定用户的任务数量
     */
    @Query("SELECT COUNT(rt) FROM RoutineTask rt WHERE rt.assignedTo = :user")
    long countTasksByUser(@Param("user") User user);

    /**
     * 统计指定用户指定状态的任务数量
     */
    @Query("SELECT COUNT(rt) FROM RoutineTask rt WHERE rt.assignedTo = :user AND rt.status = :status")
    long countTasksByUserAndStatus(@Param("user") User user, @Param("status") RoutineTask.TaskStatus status);

    /**
     * 根据任务名称模糊查询
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.taskName LIKE CONCAT('%', :keyword, '%') OR rt.taskDescription LIKE CONCAT('%', :keyword, '%')")
    List<RoutineTask> findByTaskNameOrDescriptionContaining(@Param("keyword") String keyword);

    /**
     * 查找即将到期的任务（根据创建时间和预期完成时间计算）
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.status = 'ACTIVE' AND " +
           "rt.expectedDuration IS NOT NULL AND " +
           "TIMESTAMPDIFF(MINUTE, rt.createdAt, NOW()) >= rt.expectedDuration * 0.8")
    List<RoutineTask> findOverdueOrNearDueTasks();

    /**
     * 获取用户的任务统计信息
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN rt.status = 'ACTIVE' THEN 1 END) as activeTasks, " +
           "COUNT(CASE WHEN rt.status = 'COMPLETED' THEN 1 END) as completedTasks, " +
           "COUNT(CASE WHEN rt.status = 'INACTIVE' THEN 1 END) as inactiveTasks, " +
           "COUNT(rt) as totalTasks " +
           "FROM RoutineTask rt WHERE rt.assignedTo = :user")
    Object[] getTaskStatisticsByUser(@Param("user") User user);

    /**
     * 查找可以用于周报的活跃任务
     */
    @Query("SELECT rt FROM RoutineTask rt WHERE rt.assignedTo = :user AND rt.status = 'ACTIVE' " +
           "ORDER BY rt.taskType, rt.priority DESC, rt.taskName")
    List<RoutineTask> findTasksForWeeklyReport(@Param("user") User user);

    /**
     * 检查指定用户是否已有指定名称的任务
     */
    boolean existsByAssignedToAndTaskNameIgnoreCase(User assignedTo, String taskName);

    /**
     * 根据ID和分配用户查找任务（用于权限验证）
     */
    Optional<RoutineTask> findByIdAndAssignedTo(Long id, User assignedTo);

    /**
     * 根据ID和创建者查找任务（用于权限验证）
     */
    Optional<RoutineTask> findByIdAndCreatedBy(Long id, User createdBy);
}