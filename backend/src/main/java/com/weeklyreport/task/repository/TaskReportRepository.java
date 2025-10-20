package com.weeklyreport.task.repository;

import com.weeklyreport.task.entity.TaskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TaskReport Repository - 日常任务与周报关联表数据访问层
 * 严格按照数据库设计.md第212-235行实现
 */
@Repository
public interface TaskReportRepository extends JpaRepository<TaskReport, TaskReport.TaskReportId> {

    /**
     * 根据周报ID查询所有关联的任务
     */
    @Query("SELECT tr FROM TaskReport tr JOIN FETCH tr.task WHERE tr.id.weeklyReportId = :weeklyReportId")
    List<TaskReport> findByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);

    /**
     * 根据任务ID查询所有关联的周报
     */
    @Query("SELECT tr FROM TaskReport tr JOIN FETCH tr.weeklyReport WHERE tr.id.taskId = :taskId")
    List<TaskReport> findByTaskId(@Param("taskId") Long taskId);

    /**
     * 检查任务是否已关联到周报
     */
    boolean existsByIdWeeklyReportIdAndIdTaskId(Long weeklyReportId, Long taskId);

    /**
     * 删除指定周报的所有任务关联
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskReport tr WHERE tr.id.weeklyReportId = :weeklyReportId")
    void deleteByIdWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);

    /**
     * 删除指定任务的所有周报关联
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskReport tr WHERE tr.id.taskId = :taskId")
    void deleteByIdTaskId(@Param("taskId") Long taskId);

    /**
     * 批量查询多个周报的任务关联
     */
    @Query("SELECT tr FROM TaskReport tr JOIN FETCH tr.task WHERE tr.id.weeklyReportId IN :weeklyReportIds")
    List<TaskReport> findByWeeklyReportIdIn(@Param("weeklyReportIds") List<Long> weeklyReportIds);
}