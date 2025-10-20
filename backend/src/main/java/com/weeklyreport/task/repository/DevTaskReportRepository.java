package com.weeklyreport.task.repository;

import com.weeklyreport.task.entity.DevTaskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 开发任务报告Repository
 */
@Repository
public interface DevTaskReportRepository extends JpaRepository<DevTaskReport, Long> {

    /**
     * 根据周报ID查找开发任务报告
     */
    @Query("SELECT dtr FROM DevTaskReport dtr WHERE dtr.weeklyReportId = :weeklyReportId")
    List<DevTaskReport> findByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);

    /**
     * 根据项目ID查找开发任务报告
     */
    @Query("SELECT dtr FROM DevTaskReport dtr WHERE dtr.projectId = :projectId")
    List<DevTaskReport> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据周报ID和项目ID查找开发任务报告
     */
    @Query("SELECT dtr FROM DevTaskReport dtr WHERE dtr.weeklyReportId = :weeklyReportId AND dtr.projectId = :projectId")
    List<DevTaskReport> findByWeeklyReportIdAndProjectId(@Param("weeklyReportId") Long weeklyReportId, 
                                                        @Param("projectId") Long projectId);

    /**
     * 删除指定周报的所有开发任务报告
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DevTaskReport dtr WHERE dtr.weeklyReportId = :weeklyReportId")
    void deleteByWeeklyReportId(@Param("weeklyReportId") Long weeklyReportId);
}