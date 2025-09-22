package com.weeklyreport.repository;

import com.weeklyreport.entity.DevTaskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DevTaskReport Repository - 发展任务与周报关联表数据访问层
 * 更新后的结构：使用独立主键 + phases_id字段
 */
@Repository
public interface DevTaskReportRepository extends JpaRepository<DevTaskReport, Long> {

    /**
     * 根据周报ID查询所有关联的发展任务
     */
    @Query("SELECT dtr FROM DevTaskReport dtr " +
           "JOIN FETCH dtr.project " +
           "JOIN FETCH dtr.projectPhase " +
           "WHERE dtr.weeklyReportId = :weeklyReportId")
    List<DevTaskReport> findByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);

    /**
     * 根据项目ID查询所有关联的周报
     */
    @Query("SELECT dtr FROM DevTaskReport dtr JOIN FETCH dtr.weeklyReport WHERE dtr.projectId = :projectId")
    List<DevTaskReport> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目阶段ID查询所有关联的周报
     */
    @Query("SELECT dtr FROM DevTaskReport dtr JOIN FETCH dtr.weeklyReport WHERE dtr.phasesId = :phasesId")
    List<DevTaskReport> findByPhasesId(@Param("phasesId") Long phasesId);

    /**
     * 根据项目ID和阶段ID查询所有关联的周报
     */
    @Query("SELECT dtr FROM DevTaskReport dtr JOIN FETCH dtr.weeklyReport " +
           "WHERE dtr.projectId = :projectId AND dtr.phasesId = :phasesId")
    List<DevTaskReport> findByProjectIdAndPhasesId(@Param("projectId") Long projectId, 
                                                   @Param("phasesId") Long phasesId);

    /**
     * 检查发展任务是否已关联到周报
     */
    boolean existsByWeeklyReportIdAndProjectIdAndPhasesId(Long weeklyReportId, 
                                                          Long projectId, 
                                                          Long phasesId);

    /**
     * 删除指定周报的所有发展任务关联
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DevTaskReport dtr WHERE dtr.weeklyReportId = :weeklyReportId")
    void deleteByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);

    /**
     * 删除指定项目的所有周报关联
     */
    @Modifying
    @Query("DELETE FROM DevTaskReport dtr WHERE dtr.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 删除指定项目阶段的所有周报关联
     */
    @Modifying
    @Query("DELETE FROM DevTaskReport dtr WHERE dtr.phasesId = :phasesId")
    void deleteByPhasesId(@Param("phasesId") Long phasesId);

    /**
     * 批量查询多个周报的发展任务关联
     */
    @Query("SELECT dtr FROM DevTaskReport dtr " +
           "JOIN FETCH dtr.project " +
           "JOIN FETCH dtr.projectPhase " +
           "WHERE dtr.weeklyReportId IN :weeklyReportIds")
    List<DevTaskReport> findByWeeklyReportIdIn(@Param("weeklyReportIds") List<Long> weeklyReportIds);

    /**
     * 查询指定用户的所有发展任务关联（通过周报关联）
     */
    @Query("SELECT dtr FROM DevTaskReport dtr " +
           "JOIN FETCH dtr.weeklyReport wr " +
           "JOIN FETCH dtr.project " +
           "JOIN FETCH dtr.projectPhase " +
           "WHERE wr.userId = :userId")
    List<DevTaskReport> findByUserId(@Param("userId") Long userId);

    /**
     * 根据周报ID和项目ID查询发展任务关联
     */
    @Query("SELECT dtr FROM DevTaskReport dtr " +
           "JOIN FETCH dtr.project " +
           "JOIN FETCH dtr.projectPhase " +
           "WHERE dtr.weeklyReportId = :weeklyReportId AND dtr.projectId = :projectId")
    List<DevTaskReport> findByWeeklyReportIdAndProjectId(@Param("weeklyReportId") Long weeklyReportId,
                                                         @Param("projectId") Long projectId);
}