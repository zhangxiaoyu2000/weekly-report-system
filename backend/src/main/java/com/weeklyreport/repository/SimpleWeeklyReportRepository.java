package com.weeklyreport.repository;

import com.weeklyreport.entity.SimpleProject;
import com.weeklyreport.entity.SimpleWeeklyReport;
import com.weeklyreport.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 简化周报数据访问接口
 */
@Repository
public interface SimpleWeeklyReportRepository extends JpaRepository<SimpleWeeklyReport, Long> {

    /**
     * 根据项目查找周报 (带项目和用户信息的fetch join)
     */
    @Query("SELECT r FROM SimpleWeeklyReport r JOIN FETCH r.project JOIN FETCH r.createdBy WHERE r.project = :project ORDER BY r.createdAt DESC")
    List<SimpleWeeklyReport> findByProjectOrderByCreatedAtDesc(@Param("project") SimpleProject project);

    /**
     * 根据创建者查找周报 (带项目和用户信息的fetch join)
     */
    @Query("SELECT r FROM SimpleWeeklyReport r JOIN FETCH r.project JOIN FETCH r.createdBy WHERE r.createdBy = :createdBy ORDER BY r.createdAt DESC")
    List<SimpleWeeklyReport> findByCreatedByOrderByCreatedAtDesc(@Param("createdBy") User createdBy);

    /**
     * 根据项目分页查询周报
     */
    Page<SimpleWeeklyReport> findByProjectOrderByCreatedAtDesc(SimpleProject project, Pageable pageable);

    /**
     * 根据创建者分页查询周报
     */
    Page<SimpleWeeklyReport> findByCreatedByOrderByCreatedAtDesc(User createdBy, Pageable pageable);

    /**
     * 统计项目的周报数量
     */
    long countByProject(SimpleProject project);

    /**
     * 统计用户的周报数量
     */
    long countByCreatedBy(User createdBy);

    /**
     * 查找所有周报 (带项目和用户信息的fetch join)
     */
    @Query("SELECT r FROM SimpleWeeklyReport r JOIN FETCH r.project JOIN FETCH r.createdBy ORDER BY r.createdAt DESC")
    List<SimpleWeeklyReport> findAllByOrderByCreatedAtDesc();
}